package com.brife.news.service;

import com.brife.news.dto.RawArticleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ArticleClusteringService {

    private static final int MIN_WORD_LENGTH = 2;
    private static final int MIN_INTERSECTION_SIZE = 2;
    private static final int MAX_TOPICS_PER_BATCH = 3;
    private static final int MAX_ARTICLES_PER_CLUSTER = 10;
    private static final int MAX_ARTICLES_PER_PRESS = 2;

    private static final Set<String> BASE_STOP_WORDS = Set.of(
            "것", "수", "등", "및", "에서", "으로", "에게", "이번", "지난", "올해",
            "논의", "대응", "개최", "강화", "추진", "발표", "관련", "협력",
            "진행", "예정", "시작", "확대", "지원", "위해", "통해", "대한", "내용"
    );

    // 카테고리에서 여러 토픽을 뽑을 때 씀. 클러스터 추출 → 제거 → 반복.
    public List<List<RawArticleDto>> clusterAll(List<RawArticleDto> articles, String keyword, int minClusterSize) {
        List<List<RawArticleDto>> result = new ArrayList<>();
        List<RawArticleDto> remaining = new ArrayList<>(articles);

        while (remaining.size() >= minClusterSize && result.size() < MAX_TOPICS_PER_BATCH) {
            List<RawArticleDto> fullCluster = findBestCluster(remaining, keyword);
            if (fullCluster.size() < minClusterSize) break;

            remaining.removeAll(fullCluster);
            List<RawArticleDto> selected = limitSources(fullCluster);
            if (selected.size() >= minClusterSize) {
                result.add(selected);
            } else {
                log.info("[Clustering] 언론사 중복 제거 후 기사 부족 ({}개 < 최소 {}개) - keyword={}",
                        selected.size(), minClusterSize, keyword);
            }
        }

        log.info("[Clustering] {}건 → {}개 토픽 추출 (keyword={})",
                articles.size(), result.size(), keyword);
        return result;
    }

    // keyword 불용어
    public List<RawArticleDto> cluster(List<RawArticleDto> articles, String keyword, int minClusterSize) {
        if (articles == null || articles.isEmpty()) return Collections.emptyList();

        List<RawArticleDto> bestCluster = findBestCluster(articles, keyword);
        if (bestCluster.size() < minClusterSize) {
            log.info("[Clustering] 동일 토픽 기사 부족 ({}개 < 최소 {}개) - keyword={} 합성 스킵",
                    bestCluster.size(), minClusterSize, keyword);
            return Collections.emptyList();
        }

        List<RawArticleDto> selected = limitSources(bestCluster);
        if (selected.size() < minClusterSize) {
            log.info("[Clustering] 언론사 중복 제거 후 기사 부족 ({}개 < 최소 {}개) - keyword={} 합성 스킵",
                    selected.size(), minClusterSize, keyword);
            return Collections.emptyList();
        }

        log.info("[Clustering] {}건 → {}건 (동일 토픽, keyword={})",
                articles.size(), selected.size(), keyword);
        return selected;
    }

    private List<RawArticleDto> findBestCluster(List<RawArticleDto> articles, String keyword) {

        Set<String> stopWords = new HashSet<>(BASE_STOP_WORDS);
        if (keyword != null && !keyword.isBlank()) {
            Arrays.stream(keyword.split("\\|"))
                    .flatMap(part -> Arrays.stream(part.trim().split("\\s+")))
                    .map(String::strip)
                    .filter(token -> !token.isBlank())
                    .forEach(stopWords::add);
        }

        List<Set<String>> eventWords = articles.stream()
                .<Set<String>>map(article -> new HashSet<>(tokenize(article.getTitle(), stopWords)))
                .toList();

        List<RawArticleDto> bestCluster = Collections.emptyList();

        for (int p = 0; p < articles.size(); p++) {
            Set<String> pivot = eventWords.get(p);
            if (pivot.isEmpty()) continue;

            List<Integer> clusterIndexes = new ArrayList<>();
            clusterIndexes.add(p);

            for (int i = 0; i < articles.size(); i++) {
                if (i == p) continue;
                if (intersectionSize(eventWords.get(i), pivot) >= MIN_INTERSECTION_SIZE) {
                    clusterIndexes.add(i);
                }
            }

            List<RawArticleDto> cluster = enforceCohesion(articles, eventWords, clusterIndexes);
            if (cluster.size() > bestCluster.size()) {
                bestCluster = new ArrayList<>(cluster);
            }
        }

        return bestCluster;
    }

    private List<RawArticleDto> enforceCohesion(List<RawArticleDto> articles,
                                                List<Set<String>> eventWords,
                                                List<Integer> clusterIndexes) {
        if (clusterIndexes.size() <= 2) {
            return clusterIndexes.stream().map(articles::get).toList();
        }

        int medoidIndex = clusterIndexes.stream()
                .max(Comparator.comparingInt(candidate -> clusterIndexes.stream()
                        .filter(other -> !other.equals(candidate))
                        .mapToInt(other -> intersectionSize(eventWords.get(candidate), eventWords.get(other)))
                        .sum()))
                .orElse(clusterIndexes.getFirst());

        List<Integer> cohesiveIndexes = new ArrayList<>();
        cohesiveIndexes.add(medoidIndex);
        for (Integer candidate : clusterIndexes) {
            if (candidate == medoidIndex) continue;
            if (intersectionSize(eventWords.get(candidate), eventWords.get(medoidIndex))
                    < MIN_INTERSECTION_SIZE) continue;

            boolean connectedToAnotherMember = clusterIndexes.stream()
                    .filter(other -> !other.equals(candidate) && other != medoidIndex)
                    .anyMatch(other -> intersectionSize(eventWords.get(candidate), eventWords.get(other))
                            >= MIN_INTERSECTION_SIZE);
            if (connectedToAnotherMember) cohesiveIndexes.add(candidate);
        }
        return cohesiveIndexes.stream().map(articles::get).toList();
    }

    private int intersectionSize(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size();
    }

    private List<RawArticleDto> limitSources(List<RawArticleDto> cluster) {
        Map<String, Integer> pressCounts = new HashMap<>();
        return cluster.stream()
                .sorted(Comparator.comparing(
                        RawArticleDto::getPubDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(article -> {
                    String pressKey = resolvePressKey(article);
                    int count = pressCounts.getOrDefault(pressKey, 0);
                    if (count >= MAX_ARTICLES_PER_PRESS) return false;
                    pressCounts.put(pressKey, count + 1);
                    return true;
                })
                .limit(MAX_ARTICLES_PER_CLUSTER)
                .toList();
    }

    private String resolvePressKey(RawArticleDto article) {
        if (article.getPressName() != null && !article.getPressName().isBlank()) {
            return article.getPressName().strip().toLowerCase(Locale.ROOT);
        }
        try {
            String host = URI.create(article.getSourceUrl()).getHost();
            return host != null ? host.toLowerCase(Locale.ROOT) : article.getSourceUrl();
        } catch (Exception e) {
            return article.getSourceUrl();
        }
    }

    private Set<String> tokenize(String title, Set<String> stopWords) {
        if (title == null || title.isBlank()) return Collections.emptySet();
        return Arrays.stream(title.split("[\\s·…,]+"))
                .map(w -> w.replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", ""))
                .filter(w -> w.length() >= MIN_WORD_LENGTH && !stopWords.contains(w))
                .collect(Collectors.toSet());
    }
}

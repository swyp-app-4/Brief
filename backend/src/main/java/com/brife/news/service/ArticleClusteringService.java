package com.brife.news.service;

import com.brife.news.dto.RawArticleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ArticleClusteringService {

    private static final int MIN_CLUSTER_SIZE = 3;
    private static final int MIN_WORD_LENGTH = 2;
    private static final int MIN_INTERSECTION_SIZE = 2;
    private static final int MAX_TOPICS_PER_BATCH = 3;

    private static final Set<String> BASE_STOP_WORDS = Set.of(
            "것", "수", "등", "및", "에서", "으로", "에게", "이번", "지난", "올해",
            "논의", "대응", "개최", "강화", "추진", "발표", "관련", "협력",
            "진행", "예정", "시작", "확대", "지원", "위해", "통해", "대한"
    );

    // 카테고리에서 여러 토픽을 뽑을 때 씀. 클러스터 추출 → 제거 → 반복.
    public List<List<RawArticleDto>> clusterAll(List<RawArticleDto> articles, String keyword) {
        List<List<RawArticleDto>> result = new ArrayList<>();
        List<RawArticleDto> remaining = new ArrayList<>(articles);

        while (remaining.size() >= MIN_CLUSTER_SIZE && result.size() < MAX_TOPICS_PER_BATCH) {
            List<RawArticleDto> best = cluster(remaining, keyword);
            if (best.isEmpty()) break;
            result.add(best);
            remaining.removeAll(best);
        }

        log.info("[Clustering] {}건 → {}개 토픽 추출 (keyword={})",
                articles.size(), result.size(), keyword);
        return result;
    }

    // keyword 불용어
    public List<RawArticleDto> cluster(List<RawArticleDto> articles, String keyword) {
        if (articles == null || articles.isEmpty()) return Collections.emptyList();

        Set<String> stopWords = new HashSet<>(BASE_STOP_WORDS);
        if (keyword != null && !keyword.isBlank()) {
            stopWords.add(keyword.trim());
        }

        List<Set<String>> titleWords = articles.stream()
                .map(a -> tokenize(a.getTitle(), stopWords))
                .toList();

        List<RawArticleDto> bestCluster = Collections.emptyList();

        for (int p = 0; p < articles.size(); p++) {
            Set<String> pivot = titleWords.get(p);
            if (pivot.isEmpty()) continue;

            List<RawArticleDto> cluster = new ArrayList<>();
            cluster.add(articles.get(p));

            for (int i = 0; i < articles.size(); i++) {
                if (i == p) continue;
                Set<String> intersection = new HashSet<>(titleWords.get(i));
                intersection.retainAll(pivot);
                if (intersection.size() >= MIN_INTERSECTION_SIZE) {
                    cluster.add(articles.get(i));
                }
            }

            if (cluster.size() > bestCluster.size()) {
                bestCluster = new ArrayList<>(cluster);
            }
        }

        if (bestCluster.size() < MIN_CLUSTER_SIZE) {
            log.info("[Clustering] 동일 토픽 기사 부족 ({}개) - keyword={} 합성 스킵",
                    bestCluster.size(), keyword);
            return Collections.emptyList();
        }

        log.info("[Clustering] {}건 → {}건 (동일 토픽, keyword={})",
                articles.size(), bestCluster.size(), keyword);
        return bestCluster;
    }

    private Set<String> tokenize(String title, Set<String> stopWords) {
        if (title == null || title.isBlank()) return Collections.emptySet();
        return Arrays.stream(title.split("[\\s·…,]+"))
                .map(w -> w.replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", ""))
                .filter(w -> w.length() >= MIN_WORD_LENGTH && !stopWords.contains(w))
                .collect(Collectors.toSet());
    }
}

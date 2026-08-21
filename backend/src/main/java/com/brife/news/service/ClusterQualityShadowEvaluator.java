package com.brife.news.service;

import com.brife.news.batch.NewsBatchMetrics;
import com.brife.news.dto.RawArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterQualityShadowEvaluator {

    private static final double SHADOW_THRESHOLD = 0.65;
    private static final double MIN_TITLE_JACCARD = 0.35;
    private static final int DESCRIPTION_LIMIT = 240;
    private static final Set<String> STOP_WORDS = Set.of(
            "것", "수", "등", "및", "에서", "으로", "에게", "이번", "지난", "올해",
            "관련", "대한", "통해", "위해", "뉴스", "속보", "단독", "종합", "전망");
    private static final Set<String> ENTITY_STOP_WORDS = Set.of(
            "정부", "시장", "지원", "정책", "확대", "발표", "예정", "대응", "강화",
            "추진", "결정", "논의", "가능", "급등", "상승", "하락", "증가", "감소");

    private final NewsBatchMetrics batchMetrics;

    public Evaluation evaluate(List<RawArticleDto> articles, String keyword) {
        if (articles == null || articles.size() < 2) {
            return new Evaluation(true, articles == null ? 0 : articles.size(), 0, 1.0, 1.0);
        }

        Set<String> queryTokens = tokenize(keyword, Collections.emptySet());
        List<Set<String>> titleTokens = articles.stream()
                .map(article -> tokenize(article.getTitle(), queryTokens))
                .toList();
        int medoidIndex = findMedoid(titleTokens);

        int rejectedArticles = 0;
        double minScore = 1.0;
        double totalScore = 0.0;
        int evaluatedArticles = 0;

        for (int i = 0; i < articles.size(); i++) {
            if (i == medoidIndex) continue;
            PairScore pairScore = score(
                    articles.get(medoidIndex), titleTokens.get(medoidIndex),
                    articles.get(i), titleTokens.get(i));
            boolean accepted = pairScore.score() >= SHADOW_THRESHOLD
                    && pairScore.titleJaccard() >= MIN_TITLE_JACCARD
                    && pairScore.hasCommonEntity();
            if (!accepted) rejectedArticles++;
            minScore = Math.min(minScore, pairScore.score());
            totalScore += pairScore.score();
            evaluatedArticles++;
        }

        double avgScore = evaluatedArticles == 0 ? 1.0 : totalScore / evaluatedArticles;
        boolean wouldAccept = rejectedArticles == 0;
        batchMetrics.recordClusterShadowEvaluation(articles.size(), rejectedArticles, wouldAccept);
        log.info("[ClusteringShadow] keyword={}, articles={}, wouldAccept={}, rejectedArticles={}, minScore={}, avgScore={}",
                keyword, articles.size(), wouldAccept, rejectedArticles,
                format(minScore), format(avgScore));
        return new Evaluation(wouldAccept, articles.size(), rejectedArticles, minScore, avgScore);
    }

    private int findMedoid(List<Set<String>> tokenSets) {
        return java.util.stream.IntStream.range(0, tokenSets.size())
                .boxed()
                .max(Comparator.comparingDouble(candidate -> java.util.stream.IntStream.range(0, tokenSets.size())
                        .filter(other -> other != candidate)
                        .mapToDouble(other -> jaccard(tokenSets.get(candidate), tokenSets.get(other)))
                        .sum()))
                .orElse(0);
    }

    private PairScore score(RawArticleDto left, Set<String> leftTitleTokens,
                            RawArticleDto right, Set<String> rightTitleTokens) {
        double titleJaccard = jaccard(leftTitleTokens, rightTitleTokens);
        Set<String> leftEntities = new HashSet<>(leftTitleTokens);
        Set<String> rightEntities = new HashSet<>(rightTitleTokens);
        leftEntities.removeAll(ENTITY_STOP_WORDS);
        rightEntities.removeAll(ENTITY_STOP_WORDS);
        boolean hasCommonEntity = intersectionSize(leftEntities, rightEntities) > 0;
        double entityOverlap = overlapCoefficient(leftEntities, rightEntities);
        double descriptionJaccard = jaccard(
                tokenize(firstChars(left.getDescription()), Collections.emptySet()),
                tokenize(firstChars(right.getDescription()), Collections.emptySet()));
        double titleTrigram = trigramSimilarity(left.getTitle(), right.getTitle());
        double timeScore = timeProximity(left.getPubDate(), right.getPubDate());
        double score = titleJaccard * 0.35
                + entityOverlap * 0.30
                + descriptionJaccard * 0.15
                + titleTrigram * 0.10
                + timeScore * 0.10;
        return new PairScore(score, titleJaccard, hasCommonEntity);
    }

    private Set<String> tokenize(String text, Set<String> additionalStopWords) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .map(String::strip)
                .filter(token -> token.length() >= 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .filter(token -> !additionalStopWords.contains(token))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String firstChars(String text) {
        if (text == null) return "";
        return text.substring(0, Math.min(DESCRIPTION_LIMIT, text.length()));
    }

    private double jaccard(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        return (double) intersectionSize(left, right) / union.size();
    }

    private double overlapCoefficient(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        return (double) intersectionSize(left, right) / Math.min(left.size(), right.size());
    }

    private int intersectionSize(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size();
    }

    private double trigramSimilarity(String left, String right) {
        Set<String> leftTrigrams = trigrams(left);
        Set<String> rightTrigrams = trigrams(right);
        if (leftTrigrams.isEmpty() || rightTrigrams.isEmpty()) return 0.0;
        return (2.0 * intersectionSize(leftTrigrams, rightTrigrams))
                / (leftTrigrams.size() + rightTrigrams.size());
    }

    private Set<String> trigrams(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
        if (normalized.isEmpty()) return Set.of();
        if (normalized.length() < 3) return Set.of(normalized);
        Set<String> result = new HashSet<>();
        for (int i = 0; i <= normalized.length() - 3; i++) {
            result.add(normalized.substring(i, i + 3));
        }
        return result;
    }

    private double timeProximity(LocalDateTime left, LocalDateTime right) {
        if (left == null || right == null) return 0.5;
        long hours = Math.abs(Duration.between(left, right).toHours());
        if (hours <= 3) return 1.0;
        if (hours <= 6) return 0.8;
        if (hours <= 12) return 0.5;
        if (hours <= 24) return 0.2;
        return 0.0;
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    public record Evaluation(boolean wouldAccept, int articleCount, int rejectedArticles,
                             double minScore, double avgScore) {
    }

    private record PairScore(double score, double titleJaccard, boolean hasCommonEntity) {
    }
}

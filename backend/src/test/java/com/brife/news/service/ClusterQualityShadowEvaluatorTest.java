package com.brife.news.service;

import com.brife.news.batch.NewsBatchMetrics;
import com.brife.news.dto.RawArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterQualityShadowEvaluatorTest {

    private NewsBatchMetrics metrics;
    private ClusterQualityShadowEvaluator evaluator;

    @BeforeEach
    void setUp() {
        metrics = new NewsBatchMetrics();
        evaluator = new ClusterQualityShadowEvaluator(metrics);
    }

    @Test
    void acceptsCoherentSingleEventCluster() {
        List<RawArticleDto> articles = List.of(
                article("한국은행 기준금리 동결 결정", "한국은행이 기준금리를 동결했다", 0),
                article("한국은행 기준금리 동결 유지", "한국은행이 기준금리를 동결했다", 1),
                article("한국은행 기준금리 동결 영향", "한국은행이 기준금리를 동결했다", 2));

        ClusterQualityShadowEvaluator.Evaluation result = evaluator.evaluate(articles, "금리");

        assertThat(result.wouldAccept()).isTrue();
        assertThat(metrics.getShadowEvaluatedClusterCount()).isEqualTo(1);
        assertThat(metrics.getShadowRejectedClusterCount()).isZero();
    }

    @Test
    void rejectsBroadTopicClusterInShadowOnly() {
        List<RawArticleDto> articles = List.of(
                article("서울 시민 환율 급등", "정부가 시장 안정 정책을 추진한다", 0),
                article("서울 시민 지역화폐 지급", "정부가 시장 안정 정책을 추진한다", 1),
                article("서울 시민 요양보험 인상", "정부가 시장 안정 정책을 추진한다", 2));

        ClusterQualityShadowEvaluator.Evaluation result = evaluator.evaluate(articles, "경제");

        assertThat(result.wouldAccept()).isFalse();
        assertThat(result.rejectedArticles()).isGreaterThan(0);
        assertThat(metrics.getShadowRejectedClusterCount()).isEqualTo(1);
    }

    @Test
    void resetClearsShadowMetrics() {
        evaluator.evaluate(List.of(
                article("한국은행 기준금리 동결", "기준금리 동결", 0),
                article("한국은행 기준금리 동결", "기준금리 동결", 1)), "금리");

        metrics.reset();

        assertThat(metrics.getShadowEvaluatedClusterCount()).isZero();
        assertThat(metrics.getShadowEvaluatedArticleCount()).isZero();
        assertThat(metrics.getShadowRejectedArticleCount()).isZero();
    }

    private RawArticleDto article(String title, String description, long hoursAfter) {
        return RawArticleDto.builder()
                .title(title)
                .description(description)
                .sourceUrl("https://example.com/" + title.hashCode())
                .naverUrl("https://n.news.naver.com/" + title.hashCode())
                .pressName("press-" + title.hashCode())
                .pubDate(LocalDateTime.of(2026, 8, 21, 9, 0).plusHours(hoursAfter))
                .build();
    }
}

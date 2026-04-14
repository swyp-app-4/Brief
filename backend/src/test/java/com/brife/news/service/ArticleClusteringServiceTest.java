package com.brife.news.service;

import com.brife.news.dto.RawArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleClusteringServiceTest {

    private ArticleClusteringService clusteringService;

    @BeforeEach
    void setUp() {
        clusteringService = new ArticleClusteringService();
    }

    private RawArticleDto article(String title) {
        return RawArticleDto.builder()
                .title(title)
                .description("내용")
                .sourceUrl("https://example.com")
                .naverUrl("https://n.news.naver.com/article/001")
                .build();
    }

    // ── cluster() 정상 케이스 ────────────────────────────────────────────────────

    @Test
    @DisplayName("단어 3개 이상 겹치는 기사 3개 이상 → 클러스터 반환")
    void cluster_returns_when_enough_articles() {
        List<RawArticleDto> articles = List.of(
                article("금리 인상 한국은행 결정"),
                article("한국은행 금리 인상 충격"),
                article("금리 인상 기준금리 한국은행")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "경제", 3);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("클러스터 크기는 최대 10개로 제한")
    void cluster_max_size_is_10() {
        List<RawArticleDto> articles = List.of(
                article("양자 컴퓨터 IBM 개발 성공"),
                article("양자 컴퓨터 IBM 상용화 돌파"),
                article("양자 컴퓨터 IBM 기술 혁신"),
                article("IBM 양자 컴퓨터 세계 최초"),
                article("양자 컴퓨터 IBM 투자 결정"),
                article("양자 컴퓨터 IBM 특허 취득"),
                article("양자 컴퓨터 IBM 칩셋 공개"),
                article("IBM 양자 컴퓨터 실험 성공"),
                article("양자 컴퓨터 IBM 글로벌 협약"),
                article("양자 컴퓨터 IBM 연구소 설립"),
                article("양자 컴퓨터 IBM 플랫폼 출시")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "양자", 3);

        assertThat(result).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("여러 클러스터 후보 중 가장 큰 클러스터 선택")
    void cluster_selects_largest_cluster() {
        List<RawArticleDto> articles = List.of(
                // 클러스터A: "반도체 삼성 HBM" 3개
                article("반도체 삼성 HBM 투자"),
                article("삼성 반도체 HBM 증설"),
                article("반도체 삼성 HBM 실적"),
                // 클러스터B: "부동산 대출 규제" 2개 → 3개 미달
                article("부동산 대출 규제 우려"),
                article("대출 부동산 규제 심화")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "경제", 3);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(a -> a.getTitle().contains("반도체") || a.getTitle().contains("삼성"));
    }

    @Test
    @DisplayName("검색 키워드 자체는 불용어 처리 → 키워드만 겹쳐도 동일 토픽으로 판단하지 않음")
    void cluster_ignores_keyword_as_stopword() {
        List<RawArticleDto> articles = List.of(
                article("경제 성장률 상승"),
                article("경제 무역 흑자"),
                article("경제 소비자 물가")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "경제", 3);

        assertThat(result).isEmpty();
    }

    // ── cluster() 미달 케이스 ────────────────────────────────────────────────────

    @Test
    @DisplayName("동일 토픽 기사 3개 미만 → 빈 리스트 반환")
    void cluster_returns_empty_when_fewer_than_3() {
        List<RawArticleDto> articles = List.of(
                article("금리 인상 한국은행 결정"),
                article("한국은행 금리 인상 충격")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "경제", 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단어 겹침이 2개 이하 → 클러스터 형성 안 됨 → 빈 리스트")
    void cluster_returns_empty_when_intersection_below_threshold() {
        List<RawArticleDto> articles = List.of(
                article("삼성전자 실적 호조"),
                article("LG전자 매출 증가"),
                article("SK하이닉스 영업이익 상승")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "전자", 3);

        assertThat(result).isEmpty();
    }

    // ── cluster() 경계·엣지 케이스 ───────────────────────────────────────────────

    @Test
    @DisplayName("빈 리스트 입력 → 빈 리스트 반환")
    void cluster_returns_empty_for_empty_input() {
        List<RawArticleDto> result = clusteringService.cluster(Collections.emptyList(), "경제", 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 입력 → 빈 리스트 반환")
    void cluster_returns_empty_for_null_input() {
        List<RawArticleDto> result = clusteringService.cluster(null, "경제", 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("keyword가 null이어도 예외 없이 동작")
    void cluster_handles_null_keyword() {
        List<RawArticleDto> articles = List.of(
                article("반도체 삼성 HBM 투자"),
                article("삼성 반도체 HBM 증설"),
                article("반도체 삼성 HBM 실적")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, null, 3);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("제목이 빈 기사는 pivot으로 사용되지 않음")
    void cluster_skips_empty_title_as_pivot() {
        List<RawArticleDto> articles = new ArrayList<>();
        articles.add(article(""));
        articles.add(article("반도체 삼성 HBM 투자"));
        articles.add(article("삼성 반도체 HBM 증설"));
        articles.add(article("반도체 삼성 HBM 실적"));

        List<RawArticleDto> result = clusteringService.cluster(articles, "경제", 3);

        assertThat(result).hasSize(3);
        assertThat(result).noneMatch(a -> a.getTitle().isBlank());
    }

    @Test
    @DisplayName("기사 3개 + 단어 3개 겹침 → 최소 통과 케이스")
    void cluster_passes_minimum_threshold() {
        List<RawArticleDto> articles = List.of(
                article("한국은행 기준금리 동결 결정"),
                article("기준금리 동결 한국은행 충격"),
                article("한국은행 기준금리 동결 유지")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "금리", 3);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("기본 불용어(것, 수 등)는 클러스터링에서 제외")
    void cluster_ignores_base_stop_words() {
        List<RawArticleDto> articles = List.of(
                article("이것 수출 증가"),
                article("이것 수입 감소"),
                article("이것 무역 흑자")
        );

        List<RawArticleDto> result = clusteringService.cluster(articles, "무역", 3);

        assertThat(result).isEmpty();
    }

    // ── clusterAll() 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("두 토픽이 섞인 기사 → clusterAll()이 2개 클러스터 반환")
    void clusterAll_returns_multiple_clusters() {
        List<RawArticleDto> articles = List.of(
                // 토픽A: 반도체 HBM 삼성
                article("반도체 HBM 삼성 투자"),
                article("삼성 반도체 HBM 증설"),
                article("반도체 HBM 삼성 실적"),
                // 토픽B: 한국은행 기준금리 동결
                article("한국은행 기준금리 동결 결정"),
                article("기준금리 동결 한국은행 충격"),
                article("한국은행 기준금리 동결 유지")
        );

        List<List<RawArticleDto>> result = clusteringService.clusterAll(articles, "경제", 3);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).hasSize(3);
        assertThat(result.get(1)).hasSize(3);
    }

    @Test
    @DisplayName("클러스터 하나만 가능한 경우 → clusterAll()이 1개 반환")
    void clusterAll_returns_single_cluster_when_only_one_possible() {
        List<RawArticleDto> articles = List.of(
                article("한국은행 기준금리 동결 결정"),
                article("기준금리 동결 한국은행 충격"),
                article("한국은행 기준금리 동결 유지"),
                article("관련없는 완전히 다른 기사"),
                article("또다른 무관한 내용의 기사")
        );

        List<List<RawArticleDto>> result = clusteringService.clusterAll(articles, "경제", 3);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("클러스터링 불가 기사만 있으면 → clusterAll()이 빈 리스트 반환")
    void clusterAll_returns_empty_when_no_cluster_possible() {
        List<RawArticleDto> articles = List.of(
                article("삼성전자 실적 호조"),
                article("LG전자 매출 증가"),
                article("SK하이닉스 영업이익 상승")
        );

        List<List<RawArticleDto>> result = clusteringService.clusterAll(articles, "전자", 3);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 리스트 입력 → clusterAll()이 빈 리스트 반환")
    void clusterAll_returns_empty_for_empty_input() {
        List<List<RawArticleDto>> result = clusteringService.clusterAll(Collections.emptyList(), "경제", 3);

        assertThat(result).isEmpty();
    }
}

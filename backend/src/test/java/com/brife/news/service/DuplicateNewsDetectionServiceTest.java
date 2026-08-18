package com.brife.news.service;

import com.brife.news.dto.SynthesisResult;
import com.brife.news.repository.DuplicateNewsCandidate;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateNewsDetectionServiceTest {

    @Mock
    private SummarizedNewsRepository summarizedNewsRepository;
    @Mock
    private NewsEmbeddingRepository newsEmbeddingRepository;
    @InjectMocks
    private DuplicateNewsDetectionService service;

    @Test
    void blocksOnlyHighConfidenceDuplicateWithSameNumbers() {
        SynthesisResult result = result("태풍으로 항공편 120편 결항", "제주공항에서 항공편 120편이 결항됐습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.98, 0.96,
                "태풍으로 항공편 120편 결항",
                "제주공항에서 항공편 120편이 결항됐습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isTrue();
    }

    @Test
    void keepsBreakingUpdateWhenNumbersChanged() {
        SynthesisResult result = result("태풍으로 항공편 180편 결항", "제주공항에서 항공편 180편이 결항됐습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.98, 0.96,
                "태풍으로 항공편 120편 결항",
                "제주공항에서 항공편 120편이 결항됐습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isFalse();
    }

    @Test
    void blocksSameStoryInReviewRangeAfterAdditionalChecks() {
        SynthesisResult result = result("한국은행 기준금리 동결", "한국은행이 기준금리를 동결했습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.89, 0.98, 0.96,
                "한국은행 기준금리 동결",
                "한국은행이 기준금리를 동결했습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isTrue();
    }

    @Test
    void blocksTitleLedDuplicateEvenWhenSummariesAreReworded() {
        SynthesisResult result = result(
                "삼성전자, 콜 오브 듀티 신작과 맞손 게이밍 시장 정조준",
                "삼성전자가 게임 신작과 협업해 모니터 시장에서 공동 마케팅을 진행합니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.36, 0.59, 0.34,
                "삼성전자, 콜 오브 듀티 신작과 손잡고 게이밍 시장 공략 강화",
                "삼성전자가 콜 오브 듀티 신작을 계기로 게이밍 제품 홍보를 확대합니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isTrue();
    }

    @Test
    void keepsTitleLedCandidateWhenCoreEventTokensDiffer() {
        SynthesisResult result = result(
                "삼성전자, 반도체 공장 신규 투자 발표",
                "삼성전자가 반도체 생산시설 투자 계획을 발표했습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.40, 0.58, 0.42,
                "삼성전자, 갤럭시 신제품 공개",
                "삼성전자가 새로운 갤럭시 스마트폰을 공개했습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isFalse();
    }

    @Test
    void keepsFollowUpWhenEventStateChanged() {
        SynthesisResult result = result("정부, 주택 정책 확정", "정부가 주택 공급 정책을 확정했습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.97, 0.98, 0.96,
                "정부, 주택 정책 검토",
                "정부가 주택 공급 정책을 검토하고 있습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(result.getTitle()), eq(result.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of(candidate));

        assertThat(service.isDuplicateWithoutNewInformation(result)).isFalse();
    }

    @Test
    void blocksDuplicateGeneratedEarlierInSameBatch() {
        SynthesisResult first = result("AI 반도체 수출 120억 달러 기록",
                "AI 반도체 수출이 120억 달러를 기록했습니다.");
        SynthesisResult duplicate = result("AI 반도체 수출 120억 달러 기록",
                "AI 반도체 수출이 120억 달러를 기록했습니다.");
        when(summarizedNewsRepository.findDuplicateCandidates(
                eq(first.getTitle()), eq(first.getSummary()), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThat(service.isDuplicateWithoutNewInformation(first)).isFalse();
        assertThat(service.isDuplicateWithoutNewInformation(duplicate)).isTrue();
    }

    @Test
    void treatsRewordedTitlesAsSameRecommendationEvent() {
        boolean sameEvent = service.representsSameEvent(
                49407L,
                "놀란 감독 오디세이, 개봉 13일 만에 500만 돌파 천만 향해 질주",
                "오디세이가 개봉 13일 만에 누적 관객 500만 명을 돌파했습니다.",
                49311L,
                "놀란 감독 오디세이, 개봉 13일 만에 500만 돌파 흥행 질주",
                "오디세이는 개봉 13일 만에 500만 관객을 기록했습니다."
        );

        assertThat(sameEvent).isTrue();
    }

    @Test
    void usesEmbeddingForSameEventReportedFromDifferentAngles() {
        when(newsEmbeddingRepository.findCosineSimilarity(49447L, 49434L)).thenReturn(0.9178);

        boolean sameEvent = service.representsSameEvent(
                49447L,
                "거제 통영 물폭탄에 도로 마비, 산사태로 1명 사망",
                "폭우로 도로가 통제되고 산사태 인명 피해가 발생했습니다.",
                49434L,
                "경남 거제 통영, 역대급 폭우 강타 극한호우 우려",
                "거제와 통영에 기록적인 폭우가 내려 피해가 이어졌습니다."
        );

        assertThat(sameEvent).isTrue();
    }

    @Test
    void usesEmbeddingForSameProductStoryWithDifferentWording() {
        when(newsEmbeddingRepository.findCosineSimilarity(49388L, 49302L)).thenReturn(0.9643);

        boolean sameEvent = service.representsSameEvent(
                49388L,
                "삼성전자, 콜 오브 듀티 신작과 맞손 게이밍 시장 정조준",
                "삼성전자가 신작 게임과 협업해 게이밍 시장을 공략합니다.",
                49302L,
                "삼성전자, 콜 오브 듀티 신작과 손잡고 게이밍 시장 공략 강화",
                "삼성전자가 콜 오브 듀티 신작과 마케팅 협력을 진행합니다."
        );

        assertThat(sameEvent).isTrue();
    }

    @Test
    void keepsDifferentStoriesWhenEmbeddingSimilarityIsBelowSameEventThreshold() {
        when(newsEmbeddingRepository.findCosineSimilarity(10L, 11L)).thenReturn(0.84);

        boolean sameEvent = service.representsSameEvent(
                10L,
                "서울 아파트 거래량 증가, 매수 심리 회복",
                "서울 아파트 거래량이 증가했습니다.",
                11L,
                "서울 아파트 분양가 상승, 청약 부담 확대",
                "서울 아파트 분양 가격이 상승했습니다."
        );

        assertThat(sameEvent).isFalse();
    }

    @Test
    void keepsDifferentEventsThatOnlyShareCompanyName() {
        boolean sameEvent = service.representsSameEvent(
                1L,
                "삼성전자, 반도체 공장 신규 투자 발표",
                "삼성전자가 반도체 생산시설 투자 계획을 발표했습니다.",
                2L,
                "삼성전자, 갤럭시 신제품 공개",
                "삼성전자가 새로운 갤럭시 스마트폰을 공개했습니다."
        );

        assertThat(sameEvent).isFalse();
    }

    private SynthesisResult result(String title, String summary) {
        SynthesisResult result = new SynthesisResult();
        result.setTitle(title);
        result.setSummary(summary);
        return result;
    }

    private DuplicateNewsCandidate candidate(double titleSimilarity, double summarySimilarity,
                                             String title, String summary) {
        return candidate(0.96, titleSimilarity, summarySimilarity, title, summary);
    }

    private DuplicateNewsCandidate candidate(double overallSimilarity,
                                             double titleSimilarity, double summarySimilarity,
                                             String title, String summary) {
        DuplicateNewsCandidate candidate = mock(DuplicateNewsCandidate.class);
        lenient().when(candidate.getId()).thenReturn(1L);
        lenient().when(candidate.getTitle()).thenReturn(title);
        lenient().when(candidate.getSummary()).thenReturn(summary);
        lenient().when(candidate.getOverallSimilarity()).thenReturn(overallSimilarity);
        lenient().when(candidate.getTitleSimilarity()).thenReturn(titleSimilarity);
        lenient().when(candidate.getSummarySimilarity()).thenReturn(summarySimilarity);
        return candidate;
    }
}

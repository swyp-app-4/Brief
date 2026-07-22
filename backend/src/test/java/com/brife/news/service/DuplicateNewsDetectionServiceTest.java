package com.brife.news.service;

import com.brife.news.dto.SynthesisResult;
import com.brife.news.repository.DuplicateNewsCandidate;
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
    void keepsSimilarStoryInReviewRangeAsNewArticle() {
        SynthesisResult result = result("한국은행 기준금리 동결", "한국은행이 기준금리를 동결했습니다.");
        DuplicateNewsCandidate candidate = candidate(
                0.89, 0.98, 0.96,
                "한국은행 기준금리 동결",
                "한국은행이 기준금리를 동결했습니다.");
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

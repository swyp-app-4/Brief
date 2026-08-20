package com.brife.news.service;

import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SynthesisResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SynthesisGroundingValidatorTest {

    private final SynthesisGroundingValidator validator = new SynthesisGroundingValidator();

    @Test
    void acceptsOneBasedIndexesAndFactsSupportedBySelectedArticles() {
        SynthesisResult result = result("120편 결항", "항공편 120편이 취소됐습니다.", List.of(1, 2));
        result.setSections(List.of(
                section("핵심", "제주공항에서 120편이 취소됐습니다.", List.of(1)),
                section("영향", "태풍으로 운항이 중단됐습니다.", List.of(1, 2)),
                section("전망", "운항 재개 일정은 아직 정해지지 않았습니다.", List.of(2))));
        List<RawArticleDto> articles = List.of(
                article("제주공항 120편 결항", "태풍으로 항공편 120편이 취소됐고 운항이 중단됐습니다."),
                article("제주공항 운항 재개 미정", "운항 재개 일정은 아직 정해지지 않았습니다."));

        assertThat(validator.findFailureReason(result, articles)).isNull();
        assertThat(validator.selectRelevantArticles(result, articles)).containsExactlyElementsOf(articles);
    }

    @Test
    void rejectsUnsupportedNumber() {
        SynthesisResult result = result("180편 결항", "항공편 180편이 취소됐습니다.", List.of(1, 2));
        result.setSections(List.of(
                section("핵심", "항공편이 취소됐습니다.", List.of(1)),
                section("영향", "운항이 중단됐습니다.", List.of(1)),
                section("전망", "재개 일정은 미정입니다.", List.of(2))));
        List<RawArticleDto> articles = List.of(
                article("제주공항 120편 결항", "항공편 120편이 취소됐습니다."),
                article("제주공항 운항 중단", "재개 일정은 미정입니다."));

        assertThat(validator.findFailureReason(result, articles))
                .contains("숫자가 근거 기사에 없습니다")
                .contains("180편");
    }

    @Test
    void rejectsSectionSupportOutsideRelevantArticles() {
        SynthesisResult result = result("제목", "요약", List.of(1, 2));
        result.setSections(List.of(
                section("핵심", "내용", List.of(1)),
                section("영향", "내용", List.of(3)),
                section("전망", "내용", List.of(2))));

        assertThat(validator.findFailureReason(result, List.of(
                article("하나", "내용"), article("둘", "내용"), article("셋", "내용"))))
                .contains("relevantArticleIndexes에 포함되지 않습니다");
    }

    private SynthesisResult result(String title, String summary, List<Integer> relevantIndexes) {
        SynthesisResult result = new SynthesisResult();
        result.setCategoryRelevant(true);
        result.setTitle(title);
        result.setSummary(summary);
        result.setRelevantArticleIndexes(relevantIndexes);
        return result;
    }

    private SectionDto section(String heading, String content, List<Integer> supports) {
        SectionDto section = new SectionDto(heading, content);
        section.setSupportingArticleIndexes(supports);
        return section;
    }

    private RawArticleDto article(String title, String description) {
        return RawArticleDto.builder().title(title).description(description).build();
    }
}

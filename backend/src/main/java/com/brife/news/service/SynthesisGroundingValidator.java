package com.brife.news.service;

import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.exception.InvalidSynthesisResultException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Component
public class SynthesisGroundingValidator {

    private static final int MIN_RELEVANT_ARTICLES = 2;
    private static final Pattern NUMBER_WITH_UNIT = Pattern.compile(
            "\\d[\\d,]*(?:\\.\\d+)?\\s*(?:%|퍼센트|명|건|원|달러|단계|회|개|편|위|점|년|월|일|시간|분|조|억|만)?");
    private static final Set<String> MATERIAL_STATE_WORDS = Set.of(
            "확정", "승인", "사망", "구속", "기소", "취소", "철회", "체결", "타결", "중단", "재개");

    public String findFailureReason(SynthesisResult result, List<RawArticleDto> articles) {
        if (articles == null || articles.isEmpty()) {
            return "근거로 사용할 원문 기사가 없습니다.";
        }

        List<Integer> relevantIndexes = result.getRelevantArticleIndexes();
        String indexFailure = validateIndexes(
                relevantIndexes, articles.size(), Math.min(MIN_RELEVANT_ARTICLES, articles.size()),
                "relevantArticleIndexes");
        if (indexFailure != null) return indexFailure;

        Set<Integer> relevantSet = new LinkedHashSet<>(relevantIndexes);
        for (int i = 0; i < result.getSections().size(); i++) {
            SectionDto section = result.getSections().get(i);
            String supportFailure = validateIndexes(
                    section.getSupportingArticleIndexes(), articles.size(), 1,
                    "섹션 " + (i + 1) + " supportingArticleIndexes");
            if (supportFailure != null) return supportFailure;
            if (!relevantSet.containsAll(section.getSupportingArticleIndexes())) {
                return "섹션 " + (i + 1) + "의 근거 기사가 relevantArticleIndexes에 포함되지 않습니다.";
            }
        }

        String summaryEvidence = joinedArticleText(selectArticles(articles, relevantIndexes));
        String factFailure = findUnsupportedFact(
                safe(result.getTitle()) + " " + safe(result.getSummary()), summaryEvidence, "제목·요약");
        if (factFailure != null) return factFailure;

        for (int i = 0; i < result.getSections().size(); i++) {
            SectionDto section = result.getSections().get(i);
            String sectionEvidence = joinedArticleText(
                    selectArticles(articles, section.getSupportingArticleIndexes()));
            factFailure = findUnsupportedFact(section.getContent(), sectionEvidence, "섹션 " + (i + 1));
            if (factFailure != null) return factFailure;
        }
        return null;
    }

    public List<RawArticleDto> selectRelevantArticles(SynthesisResult result, List<RawArticleDto> articles) {
        return selectArticles(articles, result.getRelevantArticleIndexes());
    }

    public GroundedSynthesis ground(SynthesisResult result, List<RawArticleDto> articles) {
        String failureReason = findFailureReason(result, articles);
        if (failureReason != null) {
            throw new InvalidSynthesisResultException(failureReason);
        }

        List<Integer> originalIndexes = result.getRelevantArticleIndexes().stream()
                .sorted()
                .toList();
        Map<Integer, Integer> remappedIndexes = new LinkedHashMap<>();
        for (int i = 0; i < originalIndexes.size(); i++) {
            remappedIndexes.put(originalIndexes.get(i), i + 1);
        }

        for (SectionDto section : result.getSections()) {
            List<Integer> remappedSupports = section.getSupportingArticleIndexes().stream()
                    .map(index -> {
                        Integer remapped = remappedIndexes.get(index);
                        if (remapped == null) {
                            throw new InvalidSynthesisResultException(
                                    "섹션 근거 기사가 relevantArticleIndexes에 포함되지 않습니다. index=" + index);
                        }
                        return remapped;
                    })
                    .toList();
            section.setSupportingArticleIndexes(remappedSupports);
        }

        List<RawArticleDto> relevantArticles = selectArticles(articles, originalIndexes);
        result.setRelevantArticleIndexes(
                IntStream.rangeClosed(1, relevantArticles.size()).boxed().toList());

        String remappedFailureReason = findFailureReason(result, relevantArticles);
        if (remappedFailureReason != null) {
            throw new InvalidSynthesisResultException(
                    "근거 기사 인덱스 재매핑 후 검증 실패: " + remappedFailureReason);
        }
        return new GroundedSynthesis(result, relevantArticles);
    }

    private String validateIndexes(List<Integer> indexes, int articleCount, int minimum, String fieldName) {
        if (indexes == null || indexes.size() < minimum) {
            return fieldName + "는 최소 " + minimum + "개가 필요합니다.";
        }
        Set<Integer> unique = new LinkedHashSet<>(indexes);
        if (unique.size() != indexes.size()) {
            return fieldName + "에 중복 인덱스가 있습니다.";
        }
        if (indexes.stream().anyMatch(index -> index == null || index < 1 || index > articleCount)) {
            return fieldName + "에 범위를 벗어난 인덱스가 있습니다. articleCount=" + articleCount;
        }
        return null;
    }

    private String findUnsupportedFact(String generatedText, String evidenceText, String location) {
        Set<String> generatedNumbers = extractNumbers(generatedText);
        Set<String> evidenceNumbers = extractNumbers(evidenceText);
        Set<String> unsupportedNumbers = new LinkedHashSet<>(generatedNumbers);
        unsupportedNumbers.removeAll(evidenceNumbers);
        if (!unsupportedNumbers.isEmpty()) {
            return location + "의 숫자가 근거 기사에 없습니다: " + unsupportedNumbers;
        }

        Set<String> unsupportedStates = extractMaterialStates(generatedText);
        unsupportedStates.removeAll(extractMaterialStates(evidenceText));
        if (!unsupportedStates.isEmpty()) {
            return location + "의 중요 상태어가 근거 기사에 없습니다: " + unsupportedStates;
        }
        return null;
    }

    private Set<String> extractNumbers(String text) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = NUMBER_WITH_UNIT.matcher(safe(text));
        while (matcher.find()) {
            String normalized = matcher.group().toLowerCase(Locale.ROOT)
                    .replace(",", "")
                    .replaceAll("\\s+", "");
            if (!normalized.isBlank()) result.add(normalized);
        }
        return result;
    }

    private Set<String> extractMaterialStates(String text) {
        Set<String> result = new LinkedHashSet<>();
        String safeText = safe(text);
        MATERIAL_STATE_WORDS.stream().filter(safeText::contains).forEach(result::add);
        return result;
    }

    private List<RawArticleDto> selectArticles(List<RawArticleDto> articles, List<Integer> indexes) {
        List<RawArticleDto> selected = new ArrayList<>(indexes.size());
        indexes.stream().sorted().forEach(index -> selected.add(articles.get(index - 1)));
        return List.copyOf(selected);
    }

    private String joinedArticleText(List<RawArticleDto> articles) {
        return articles.stream()
                .map(article -> safe(article.getTitle()) + " " + safe(article.getDescription()))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    public record GroundedSynthesis(
            SynthesisResult result,
            List<RawArticleDto> relevantArticles
    ) {
    }
}

package com.brife.news.service;

import com.brife.news.dto.SynthesisResult;
import com.brife.news.repository.DuplicateNewsCandidate;
import com.brife.news.repository.SummarizedNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateNewsDetectionService {

    private static final int LOOKBACK_HOURS = 48;
    private static final double REVIEW_SIMILARITY = 0.85;
    private static final double DUPLICATE_SIMILARITY = 0.92;
    private static final double MIN_TITLE_SIMILARITY = 0.90;
    private static final double MIN_SUMMARY_SIMILARITY = 0.88;
    private static final double MIN_CORE_TOKEN_OVERLAP = 0.70;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:[.,]\\d+)*%?");
    private static final Set<String> STATE_WORDS = Set.of(
            "예정", "검토", "추진", "발표", "확정", "결정", "체결", "승인",
            "취소", "철회", "중단", "재개", "발생", "연기", "구속", "기소",
            "타결", "사망", "부상", "증가", "감소", "상승", "하락", "동결",
            "인상", "인하", "출시", "공개", "종료", "시작");
    private static final Set<String> CORE_STOP_WORDS = Set.of(
            "관련", "대한", "통해", "위해", "이번", "지난", "오늘", "내일", "올해",
            "뉴스", "속보", "단독", "종합", "일부", "전망", "가능성", "본격");

    private final SummarizedNewsRepository summarizedNewsRepository;
    private final List<AcceptedNews> acceptedInCurrentBatch = new ArrayList<>();

    public synchronized boolean isDuplicateWithoutNewInformation(SynthesisResult result) {
        if (result == null || isBlank(result.getTitle()) || isBlank(result.getSummary())) return false;

        for (AcceptedNews accepted : acceptedInCurrentBatch) {
            if (isDuplicateContent(
                    result.getTitle(), result.getSummary(), accepted.title(), accepted.summary(),
                    trigramSimilarity(result.getTitle() + " " + result.getSummary(),
                            accepted.title() + " " + accepted.summary()),
                    trigramSimilarity(result.getTitle(), accepted.title()),
                    trigramSimilarity(result.getSummary(), accepted.summary()))) {
                log.info("[Duplicate] Duplicate generated in current batch was blocked");
                return true;
            }
        }

        for (DuplicateNewsCandidate candidate : summarizedNewsRepository.findDuplicateCandidates(
                result.getTitle(), result.getSummary(), LocalDateTime.now().minusHours(LOOKBACK_HOURS))) {
            double overallSimilarity = candidate.getOverallSimilarity();
            if (overallSimilarity < REVIEW_SIMILARITY) continue;
            if (overallSimilarity < DUPLICATE_SIMILARITY) {
                log.debug("[Duplicate] Similar event kept as a new article - existingNewsId={}, similarity={}",
                        candidate.getId(), overallSimilarity);
                continue;
            }
            if (isDuplicateContent(
                    result.getTitle(), result.getSummary(), candidate.getTitle(), candidate.getSummary(),
                    overallSimilarity, candidate.getTitleSimilarity(), candidate.getSummarySimilarity())) {
                log.info("[Duplicate] Duplicate content blocked - existingNewsId={}, overallSimilarity={}, "
                                + "titleSimilarity={}, summarySimilarity={}",
                        candidate.getId(), overallSimilarity, candidate.getTitleSimilarity(),
                        candidate.getSummarySimilarity());
                return true;
            }
        }

        acceptedInCurrentBatch.add(new AcceptedNews(result.getTitle(), result.getSummary()));
        return false;
    }

    public synchronized void resetBatchCandidates() {
        acceptedInCurrentBatch.clear();
    }

    public boolean representsSameEvent(String leftTitle, String leftSummary,
                                       String rightTitle, String rightSummary) {
        if (isBlank(leftTitle) || isBlank(rightTitle)) return false;
        double titleSimilarity = trigramSimilarity(leftTitle, rightTitle);
        double overallSimilarity = trigramSimilarity(
                leftTitle + " " + safe(leftSummary), rightTitle + " " + safe(rightSummary));
        double tokenOverlap = overlapCoefficient(extractCoreTokens(leftTitle), extractCoreTokens(rightTitle));
        return titleSimilarity >= 0.88 || (overallSimilarity >= 0.80 && tokenOverlap >= 0.65);
    }

    private boolean isDuplicateContent(String newTitle, String newSummary,
                                       String existingTitle, String existingSummary,
                                       double overallSimilarity, double titleSimilarity,
                                       double summarySimilarity) {
        if (overallSimilarity < DUPLICATE_SIMILARITY) return false;
        return titleSimilarity >= MIN_TITLE_SIMILARITY
                && summarySimilarity >= MIN_SUMMARY_SIMILARITY
                && overlapCoefficient(extractCoreTokens(newTitle), extractCoreTokens(existingTitle))
                        >= MIN_CORE_TOKEN_OVERLAP
                && extractNumbers(newTitle + " " + newSummary)
                        .equals(extractNumbers(existingTitle + " " + existingSummary))
                && extractStateWords(newTitle + " " + newSummary)
                        .equals(extractStateWords(existingTitle + " " + existingSummary));
    }

    private Set<String> extractNumbers(String text) {
        Set<String> numbers = new TreeSet<>();
        Matcher matcher = NUMBER_PATTERN.matcher(safe(text));
        while (matcher.find()) numbers.add(matcher.group());
        return numbers;
    }

    private Set<String> extractStateWords(String text) {
        String safeText = safe(text);
        return STATE_WORDS.stream()
                .filter(safeText::contains)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Set<String> extractCoreTokens(String title) {
        return Arrays.stream(safe(title).toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .map(String::strip)
                .filter(token -> token.length() >= 2)
                .filter(token -> !CORE_STOP_WORDS.contains(token))
                .filter(token -> !STATE_WORDS.contains(token))
                .filter(token -> !NUMBER_PATTERN.matcher(token).matches())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private double overlapCoefficient(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return (double) intersection.size() / Math.min(left.size(), right.size());
    }

    private double trigramSimilarity(String left, String right) {
        Set<String> leftTrigrams = trigrams(left);
        Set<String> rightTrigrams = trigrams(right);
        if (leftTrigrams.isEmpty() || rightTrigrams.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(leftTrigrams);
        intersection.retainAll(rightTrigrams);
        return (2.0 * intersection.size()) / (leftTrigrams.size() + rightTrigrams.size());
    }

    private Set<String> trigrams(String text) {
        String normalized = safe(text).toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
        if (normalized.isEmpty()) return Set.of();
        if (normalized.length() < 3) return Set.of(normalized);
        Set<String> result = new HashSet<>();
        for (int i = 0; i <= normalized.length() - 3; i++) {
            result.add(normalized.substring(i, i + 3));
        }
        return result;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private record AcceptedNews(String title, String summary) {
    }
}

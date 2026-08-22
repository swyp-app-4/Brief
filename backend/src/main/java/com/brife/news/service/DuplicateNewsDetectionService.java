package com.brife.news.service;

import com.brife.news.dto.SynthesisResult;
import com.brife.news.batch.NewsBatchMetrics;
import com.brife.news.repository.DuplicateNewsCandidate;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SemanticDuplicateCandidate;
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
import java.util.Map;
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
    private static final int SEMANTIC_CANDIDATE_LIMIT = 10;
    private static final double REVIEW_SIMILARITY = 0.85;
    private static final double DUPLICATE_SIMILARITY = 0.92;
    private static final double SEMANTIC_HIGH_CONFIDENCE = 0.92;
    private static final double SEMANTIC_HIGH_TITLE_SIMILARITY = 0.32;
    private static final double SEMANTIC_REVIEW_TITLE_SIMILARITY = 0.50;
    private static final double SEMANTIC_REVIEW_TOKEN_OVERLAP = 0.55;
    private static final double MIN_TITLE_SIMILARITY = 0.90;
    private static final double MIN_SUMMARY_SIMILARITY = 0.88;
    private static final double MIN_CORE_TOKEN_OVERLAP = 0.70;
    private static final double REVIEW_TITLE_SIMILARITY = 0.70;
    private static final double REVIEW_SUMMARY_SIMILARITY = 0.75;
    private static final double REVIEW_CORE_TOKEN_OVERLAP = 0.65;
    private static final double TITLE_LED_SIMILARITY = 0.55;
    private static final double TITLE_LED_SUMMARY_SIMILARITY = 0.30;
    private static final double TITLE_LED_CORE_TOKEN_OVERLAP = 0.70;
    private static final double SAME_BATCH_TITLE_SIMILARITY = 0.55;
    private static final double SAME_BATCH_CORE_TOKEN_OVERLAP = 0.55;
    private static final int SAME_BATCH_MIN_SHARED_TOKENS = 2;
    private static final double RECOMMENDATION_TITLE_SIMILARITY = 0.72;
    private static final double RECOMMENDATION_OVERALL_SIMILARITY = 0.68;
    private static final double RECOMMENDATION_TOKEN_OVERLAP = 0.45;
    private static final double RECOMMENDATION_EMBEDDING_SIMILARITY = 0.90;
    private static final int MIN_SHARED_RECOMMENDATION_TOKENS = 2;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:[.,]\\d+)*%?");
    private static final Pattern CALENDAR_NUMBER_PATTERN = Pattern.compile(
            "\\d{4}년|(?<!\\d)\\d{1,2}월|(?<!\\d)\\d{1,2}일|(?<!\\d)\\d{1,2}시(?:\\s*\\d{1,2}분)?");
    private static final Set<String> STATE_WORDS = Set.of(
            "예정", "검토", "추진", "발표", "확정", "결정", "체결", "승인",
            "취소", "철회", "중단", "재개", "발생", "연기", "구속", "기소",
            "타결", "사망", "부상", "증가", "감소", "상승", "하락", "동결",
            "인상", "인하", "출시", "공개", "종료", "시작");
    private static final Set<String> CORE_STOP_WORDS = Set.of(
            "관련", "대한", "통해", "위해", "이번", "지난", "오늘", "내일", "올해",
            "뉴스", "속보", "단독", "종합", "일부", "전망", "가능성", "본격");

    private final SummarizedNewsRepository summarizedNewsRepository;
    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final SemanticDuplicatePolicy semanticDuplicatePolicy;
    private final NewsBatchMetrics batchMetrics;
    private final List<AcceptedNews> acceptedInCurrentBatch = new ArrayList<>();

    public boolean isDuplicateWithoutNewInformation(SynthesisResult result) {
        return isDuplicateWithoutNewInformation(result, null);
    }

    public synchronized boolean isDuplicateWithoutNewInformation(
            SynthesisResult result, float[] documentEmbedding) {
        if (result == null || isBlank(result.getTitle()) || isBlank(result.getSummary())) return false;

        for (AcceptedNews accepted : acceptedInCurrentBatch) {
            boolean stateChanged = hasMaterialStateChange(
                    result.getTitle(), result.getSummary(), accepted.title(), accepted.summary());
            if ((!stateChanged && isSameBatchHeadlineDuplicate(result.getTitle(), accepted.title()))
                    || isDuplicateContent(
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

        SemanticEvaluation semanticEvaluation = evaluateSemanticDuplicate(result, documentEmbedding);
        if (semanticEvaluation.evaluated()) {
            boolean blocked = semanticEvaluation.wouldBlock()
                    && semanticDuplicatePolicy.isBlockingEnabled();
            batchMetrics.recordSemanticDuplicateEvaluation(
                    semanticEvaluation.wouldBlock(), blocked, semanticEvaluation.followUpAllowed());

            if (semanticEvaluation.candidate() != null) {
                log.info("[SemanticDuplicateShadow] candidateNewsId={}, similarity={}, wouldBlock={}, "
                                + "followUpAllowed={}, blockingEnabled={}, title={}",
                        semanticEvaluation.candidate().id(),
                        String.format("%.3f", semanticEvaluation.candidate().similarity()),
                        semanticEvaluation.wouldBlock(),
                        semanticEvaluation.followUpAllowed(),
                        semanticDuplicatePolicy.isBlockingEnabled(),
                        result.getTitle());
            }
            if (blocked) {
                log.info("[SemanticDuplicate] Duplicate content blocked - existingNewsId={}, similarity={}",
                        semanticEvaluation.candidate().id(), semanticEvaluation.candidate().similarity());
                return true;
            }
        }

        acceptedInCurrentBatch.add(new AcceptedNews(
                result.getTitle(), result.getSummary(), copy(documentEmbedding)));
        return false;
    }

    private SemanticEvaluation evaluateSemanticDuplicate(
            SynthesisResult result, float[] documentEmbedding) {
        if (documentEmbedding == null || !semanticDuplicatePolicy.isEnabled()) {
            return SemanticEvaluation.notEvaluated();
        }

        SemanticMatch closestFollowUp = null;
        for (AcceptedNews accepted : acceptedInCurrentBatch) {
            Double similarity = cosineSimilarity(documentEmbedding, accepted.embedding());
            if (similarity == null || similarity < REVIEW_SIMILARITY) continue;
            SemanticMatch match = new SemanticMatch(
                    null, accepted.title(), accepted.summary(), similarity);
            SemanticCandidateDecision decision = evaluateSemanticCandidate(result, match);
            if (decision.wouldBlock()) return SemanticEvaluation.duplicate(match);
            if (decision.followUpAllowed()
                    && (closestFollowUp == null || match.similarity() > closestFollowUp.similarity())) {
                closestFollowUp = match;
            }
        }

        List<SemanticDuplicateCandidate> candidates =
                newsEmbeddingRepository.findRecentDuplicateCandidatesByVector(
                        documentEmbedding,
                        LocalDateTime.now().minusHours(LOOKBACK_HOURS),
                        REVIEW_SIMILARITY,
                        SEMANTIC_CANDIDATE_LIMIT);
        for (SemanticDuplicateCandidate candidate : candidates) {
            SemanticMatch match = new SemanticMatch(
                    candidate.id(), candidate.title(), candidate.summary(), candidate.similarity());
            SemanticCandidateDecision decision = evaluateSemanticCandidate(result, match);
            if (decision.wouldBlock()) return SemanticEvaluation.duplicate(match);
            if (decision.followUpAllowed()
                    && (closestFollowUp == null || match.similarity() > closestFollowUp.similarity())) {
                closestFollowUp = match;
            }
        }

        return closestFollowUp == null
                ? SemanticEvaluation.noDuplicate()
                : SemanticEvaluation.followUp(closestFollowUp);
    }

    private SemanticCandidateDecision evaluateSemanticCandidate(
            SynthesisResult result, SemanticMatch candidate) {
        if (hasSemanticMaterialChange(
                result.getTitle(), candidate.title())) {
            return SemanticCandidateDecision.followUp();
        }

        Set<String> newTokens = extractCoreTokens(result.getTitle());
        Set<String> existingTokens = extractCoreTokens(candidate.title());
        int sharedTokens = sharedTokenCount(newTokens, existingTokens);
        double tokenOverlap = overlapCoefficient(newTokens, existingTokens);
        double titleSimilarity = trigramSimilarity(result.getTitle(), candidate.title());

        if (candidate.similarity() >= SEMANTIC_HIGH_CONFIDENCE) {
            boolean sameEventEvidence = titleSimilarity >= SEMANTIC_HIGH_TITLE_SIMILARITY
                    || (sharedTokens >= 3 && tokenOverlap >= SEMANTIC_REVIEW_TOKEN_OVERLAP);
            return sameEventEvidence
                    ? SemanticCandidateDecision.duplicate()
                    : SemanticCandidateDecision.unrelated();
        }

        boolean sameEventEvidence = titleSimilarity >= SEMANTIC_REVIEW_TITLE_SIMILARITY
                && sharedTokens >= SAME_BATCH_MIN_SHARED_TOKENS
                && tokenOverlap >= SEMANTIC_REVIEW_TOKEN_OVERLAP;
        return sameEventEvidence
                ? SemanticCandidateDecision.duplicate()
                : SemanticCandidateDecision.unrelated();
    }

    private boolean hasSemanticMaterialChange(String newTitle, String existingTitle) {
        Set<String> newNumbers = extractMaterialTitleNumbers(newTitle);
        Set<String> existingNumbers = extractMaterialTitleNumbers(existingTitle);
        if (hasNewDistinctSignal(newNumbers, existingNumbers)) return true;

        Set<String> newStates = extractStateWords(newTitle);
        Set<String> existingStates = extractStateWords(existingTitle);
        boolean tentativeToFinal = containsAny(existingStates, "검토", "추진", "예정")
                && containsAny(newStates, "확정", "결정", "승인", "체결", "타결",
                "취소", "철회", "중단", "재개", "시작");
        boolean incidentUpdated = existingStates.contains("발생")
                && containsAny(newStates, "사망", "부상", "구속", "기소");
        return tentativeToFinal || incidentUpdated;
    }

    private Set<String> extractMaterialTitleNumbers(String title) {
        return extractNumbers(CALENDAR_NUMBER_PATTERN.matcher(safe(title)).replaceAll(" "));
    }

    private boolean containsAny(Set<String> values, String... candidates) {
        return Arrays.stream(candidates).anyMatch(values::contains);
    }

    private float[] copy(float[] embedding) {
        return embedding == null ? null : Arrays.copyOf(embedding, embedding.length);
    }

    private boolean isSameBatchHeadlineDuplicate(String newTitle, String acceptedTitle) {
        String normalizedNewTitle = normalizeTitle(newTitle);
        String normalizedAcceptedTitle = normalizeTitle(acceptedTitle);
        if (!normalizedNewTitle.isBlank() && normalizedNewTitle.equals(normalizedAcceptedTitle)) {
            return true;
        }

        Set<String> newTokens = extractCoreTokens(newTitle);
        Set<String> acceptedTokens = extractCoreTokens(acceptedTitle);
        return trigramSimilarity(newTitle, acceptedTitle) >= SAME_BATCH_TITLE_SIMILARITY
                && sharedTokenCount(newTokens, acceptedTokens) >= SAME_BATCH_MIN_SHARED_TOKENS
                && overlapCoefficient(newTokens, acceptedTokens) >= SAME_BATCH_CORE_TOKEN_OVERLAP;
    }

    public synchronized void resetBatchCandidates() {
        acceptedInCurrentBatch.clear();
    }

    public boolean representsSameEvent(Long leftNewsId, String leftTitle, String leftSummary,
                                       Long rightNewsId, String rightTitle, String rightSummary) {
        return representsSameEvent(leftNewsId, leftTitle, leftSummary,
                rightNewsId, rightTitle, rightSummary, null);
    }

    public boolean representsSameEvent(Long leftNewsId, String leftTitle, String leftSummary,
                                       Long rightNewsId, String rightTitle, String rightSummary,
                                       Map<Long, float[]> embeddingCache) {
        if (isBlank(leftTitle) || isBlank(rightTitle)) return false;

        Set<String> leftTokens = extractCoreTokens(leftTitle);
        Set<String> rightTokens = extractCoreTokens(rightTitle);
        double titleSimilarity = trigramSimilarity(leftTitle, rightTitle);
        double overallSimilarity = trigramSimilarity(
                leftTitle + " " + safe(leftSummary), rightTitle + " " + safe(rightSummary));
        double tokenOverlap = overlapCoefficient(leftTokens, rightTokens);

        if (titleSimilarity >= RECOMMENDATION_TITLE_SIMILARITY) {
            return true;
        }
        if (overallSimilarity >= RECOMMENDATION_OVERALL_SIMILARITY
                && tokenOverlap >= RECOMMENDATION_TOKEN_OVERLAP) {
            return true;
        }
        if (sharedTokenCount(leftTokens, rightTokens) < MIN_SHARED_RECOMMENDATION_TOKENS) {
            return false;
        }

        Double embeddingSimilarity = embeddingCache == null
                ? newsEmbeddingRepository.findCosineSimilarity(leftNewsId, rightNewsId)
                : cosineSimilarity(embeddingCache.get(leftNewsId), embeddingCache.get(rightNewsId));
        return embeddingSimilarity != null
                && embeddingSimilarity >= RECOMMENDATION_EMBEDDING_SIMILARITY;
    }

    private Double cosineSimilarity(float[] left, float[] right) {
        if (left == null || right == null || left.length != right.length || left.length == 0) {
            return null;
        }
        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) return null;
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private int sharedTokenCount(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size();
    }

    private boolean isDuplicateContent(String newTitle, String newSummary,
                                       String existingTitle, String existingSummary,
                                       double overallSimilarity, double titleSimilarity,
                                       double summarySimilarity) {
        if (hasMaterialNumberChange(newTitle, newSummary, existingTitle, existingSummary)
                || hasMaterialStateChange(newTitle, newSummary, existingTitle, existingSummary)) {
            return false;
        }

        double coreTokenOverlap = overlapCoefficient(
                extractCoreTokens(newTitle), extractCoreTokens(existingTitle));

        if (normalizeTitle(newTitle).equals(normalizeTitle(existingTitle))
                && summarySimilarity >= TITLE_LED_SUMMARY_SIMILARITY) {
            return true;
        }

        if (overallSimilarity >= DUPLICATE_SIMILARITY) {
            return (titleSimilarity >= MIN_TITLE_SIMILARITY
                    && summarySimilarity >= MIN_SUMMARY_SIMILARITY)
                    || coreTokenOverlap >= MIN_CORE_TOKEN_OVERLAP;
        }

        if (overallSimilarity >= REVIEW_SIMILARITY) {
            return (titleSimilarity >= REVIEW_TITLE_SIMILARITY
                    && coreTokenOverlap >= REVIEW_CORE_TOKEN_OVERLAP)
                    || (summarySimilarity >= REVIEW_SUMMARY_SIMILARITY
                    && coreTokenOverlap >= REVIEW_CORE_TOKEN_OVERLAP);
        }

        return titleSimilarity >= TITLE_LED_SIMILARITY
                && summarySimilarity >= TITLE_LED_SUMMARY_SIMILARITY
                && coreTokenOverlap >= TITLE_LED_CORE_TOKEN_OVERLAP;
    }

    private boolean hasMaterialNumberChange(String newTitle, String newSummary,
                                            String existingTitle, String existingSummary) {
        Set<String> newTitleNumbers = extractNumbers(newTitle);
        Set<String> existingTitleNumbers = extractNumbers(existingTitle);
        if (hasNewDistinctSignal(newTitleNumbers, existingTitleNumbers)) {
            return true;
        }

        Set<String> newNumbers = extractNumbers(newTitle + " " + newSummary);
        Set<String> existingNumbers = extractNumbers(existingTitle + " " + existingSummary);
        return hasNewDistinctSignal(newNumbers, existingNumbers);
    }

    private boolean hasMaterialStateChange(String newTitle, String newSummary,
                                           String existingTitle, String existingSummary) {
        Set<String> newStates = extractStateWords(newTitle + " " + newSummary);
        Set<String> existingStates = extractStateWords(existingTitle + " " + existingSummary);
        return hasNewDistinctSignal(newStates, existingStates);
    }

    private boolean hasNewDistinctSignal(Set<String> newSignals, Set<String> existingSignals) {
        if (newSignals.isEmpty()) return false;
        if (existingSignals.isEmpty()) return true;
        return newSignals.stream().noneMatch(existingSignals::contains);
    }

    private String normalizeTitle(String title) {
        return safe(title).toLowerCase(Locale.ROOT)
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .strip()
                .replaceAll("\\s+", " ");
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

    private record AcceptedNews(String title, String summary, float[] embedding) {
    }

    private record SemanticMatch(Long id, String title, String summary, double similarity) {
    }

    private record SemanticEvaluation(
            boolean evaluated,
            boolean wouldBlock,
            boolean followUpAllowed,
            SemanticMatch candidate
    ) {
        private static SemanticEvaluation notEvaluated() {
            return new SemanticEvaluation(false, false, false, null);
        }

        private static SemanticEvaluation noDuplicate() {
            return new SemanticEvaluation(true, false, false, null);
        }

        private static SemanticEvaluation duplicate(SemanticMatch candidate) {
            return new SemanticEvaluation(true, true, false, candidate);
        }

        private static SemanticEvaluation followUp(SemanticMatch candidate) {
            return new SemanticEvaluation(true, false, true, candidate);
        }
    }

    private record SemanticCandidateDecision(boolean wouldBlock, boolean followUpAllowed) {
        private static SemanticCandidateDecision duplicate() {
            return new SemanticCandidateDecision(true, false);
        }

        private static SemanticCandidateDecision followUp() {
            return new SemanticCandidateDecision(false, true);
        }

        private static SemanticCandidateDecision unrelated() {
            return new SemanticCandidateDecision(false, false);
        }
    }
}

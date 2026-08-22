package com.brife.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.brife.news.batch.NewsBatchMetrics;
import com.brife.news.config.VertexAiProperties;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.exception.InvalidSynthesisResultException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SummarizationService {

    private static final int MAX_CHARS_PER_ARTICLE  = 8_000;
    private static final int MAX_CHARS_PER_CLUSTER  = 30_000;
    private static final int MIN_TOTAL_SECTION_LENGTH   = 900;
    private static final int MAX_TOTAL_SECTION_LENGTH   = 1200;
    private static final int LENGTH_INCREMENT_PER_SOURCE = 50;
    private static final int MIN_SECTION_CONTENT_LENGTH = 280;

    private static final String SYSTEM_PROMPT = """
            당신은 15년 차 경력의 대한민국 수석 뉴스 에디터입니다.
            여러 언론사의 기사를 종합하여 독자가 가장 궁금해할 내용을 담은 완벽한 뉴스 리포트를 작성해야 합니다.

            [절대 규칙]
            1. 100% 자연스러운 한국어만 사용. 한자·영어·일본어 혼용 및 할루시네이션 절대 금지.
            2. 반드시 아래 JSON 구조만 반환. 인사말·부연설명 일절 금지.
            {
              "categoryRelevant": true,
              "categoryReason": "...",
              "title": "...",
              "summary": "...",
              "relevantArticleIndexes": [1, 2],
              "sections": [
                { "heading": "...", "content": "...", "supportingArticleIndexes": [1] },
                { "heading": "...", "content": "...", "supportingArticleIndexes": [1, 2] },
                { "heading": "...", "content": "...", "supportingArticleIndexes": [2] }
              ]
            }

            [작성 가이드]
            1. categoryRelevant: 입력 기사의 중심 사건이 요청 카테고리에 속하면 true, 검색어가 부수적으로만 언급되면 false.
               categoryReason: 판단 근거를 한 문장으로 작성.

            2. title: 20~30자 내외. 포털 메인급 헤드라인 스타일. 단순 명사 나열 금지.
               예: "트럼프, 이란 군사작전 축소 시사… 휴전엔 선 그어"

            3. summary (4줄 요약):
               - 핵심 팩트 4가지를 각각 한 줄로. 줄 구분은 '\\n'.
               - 어미는 친절한 경어체(~됩니다, ~했습니다, ~예정입니다). 명사형 종결('~함', '~됨') 및 평어체('~했다') 금지.
               - 국내 파급 효과가 있으면 반드시 1줄 포함.

            4. relevantArticleIndexes:
               - 제목·요약·본문 작성에 실제로 사용한 기사 번호를 1부터 시작하는 정수로 작성.
               - 최소 2개 이상의 서로 다른 기사를 포함하고 관련 없는 기사는 제외.

            5. sections (본문 단락 - 반드시 3개):
               - 섹션 1 [핵심 상황과 배경] (고정): 이 뉴스의 핵심 팩트·배경·경위를 상세히 서술. 독자가 맥락 없이도 사건 전체를 파악할 수 있도록 육하원칙(누가·언제·어디서·무엇을·어떻게·왜)에 따라 빠짐없이 서술.
               - 섹션 2, 3 (자율 선택): 아래 7가지 앵글 중 이 뉴스 성격에 가장 잘 맞는 2가지를 골라 작성.
                 * [숨은 맥락]: 정치·사회 뉴스 - 왜 이런 일이 일어났나?
                 * [향후 전망]: 경제·정책 뉴스 - 내 생활에 어떤 영향을 미치나?
                 * [핵심 쟁점]: 논란·갈등 뉴스 - 양측의 입장은 무엇인가?
                 * [관전 포인트]: 스포츠·연예 뉴스 - 앞으로 무엇을 기대해야 하나?
                 * [TMI 지식]: IT·과학 뉴스 - 이 기술·현상의 원리는 무엇인가?
                 * [국제 맥락]: 외교·국제 뉴스 - 글로벌 흐름 속에서 어떤 의미인가?
                 * [생활 영향]: 부동산·금융·복지 뉴스 - 내 일상에 직접적으로 어떤 변화가 오나?
               - heading: 15~20자 내외 미니 헤드라인. 단순 명사구 금지.
               - 반드시 동사/형용사형 서술어(~하다, ~다) 또는 의문형(~일까?)으로 끝낼 것.
                 예: "동맹국에 호위 동참 압박하나?" (O) / "트럼프 발언" (X)
               - content: 7~9문장. 각 문장은 '\\n'으로 구분.
                 어투는 친절한 에디터 톤(~어요, ~됩니다, ~했습니다)으로 작성. 보고서체('~함', '~임') 금지.
                 뉴스 원문에 없는 내용을 지어내거나 추측하지 말 것.
               - supportingArticleIndexes: 해당 섹션의 사실을 직접 뒷받침하는 기사 번호를 1부터 시작하는 정수로 작성.
            """;

    private final VertexAiProperties vertexAiProperties;
    private final VertexAiTokenService vertexAiTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;
    private final NewsBatchMetrics batchMetrics;
    private final SynthesisGroundingValidator groundingValidator;

    public SummarizationService(VertexAiProperties vertexAiProperties,
                                VertexAiTokenService vertexAiTokenService,
                                @Qualifier("vertexAiRestTemplate") RestTemplate restTemplate,
                                ObjectMapper objectMapper,
                                RateLimiterRegistry rateLimiterRegistry,
                                NewsBatchMetrics batchMetrics,
                                SynthesisGroundingValidator groundingValidator) {
        this.vertexAiProperties = vertexAiProperties;
        this.vertexAiTokenService = vertexAiTokenService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiterRegistry.rateLimiter("gemini");
        this.batchMetrics = batchMetrics;
        this.groundingValidator = groundingValidator;
    }

    public SynthesisResult synthesize(String keyword, List<RawArticleDto> articles) throws Exception {
        return synthesize(keyword, keyword, articles);
    }

    public SynthesisResult synthesize(String categoryName, String keyword,
                                      List<RawArticleDto> articles) throws Exception {
        try {
            SynthesisResult result = RateLimiter.decorateCheckedSupplier(rateLimiter,
                    () -> callVertexAi(categoryName, keyword, articles, null)).get();

            if (result != null && !result.isCategoryRelevant()) return result;

            String reason = findQualityFailureReason(result, articles);
            if (reason != null) {
                log.warn("[Summarization] 품질 미달 - 1회 재시도. reason={}, keyword={}", reason, keyword);
                batchMetrics.incrementVertexRetryCount();
                try {
                    result = RateLimiter.decorateCheckedSupplier(rateLimiter,
                            () -> callVertexAi(categoryName, keyword, articles, reason)).get();
                } catch (RequestNotPermitted e) {
                    throw new InvalidSynthesisResultException(
                            "요약 품질 재시도 속도 제한 초과 - keyword=" + keyword, e);
                }

                if (result != null && !result.isCategoryRelevant()) return result;

                String retryFailureReason = findQualityFailureReason(result, articles);
                if (retryFailureReason != null) {
                    throw new InvalidSynthesisResultException("요약 품질 기준 재시도 실패 - keyword=" + keyword
                            + ", reason=" + retryFailureReason);
                }
            }

            return result;
        } catch (RequestNotPermitted e) {
            log.warn("[Summarization] Vertex AI 속도 제한 초과 - keyword={}", keyword);
            throw new RuntimeException("Vertex AI 속도 제한 초과", e);
        } catch (InvalidSynthesisResultException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private SynthesisResult callVertexAi(String categoryName, String keyword, List<RawArticleDto> articles,
                                         String qualityFailureReason) throws Exception {
        String userPrompt = buildUserPrompt(categoryName, keyword, articles, qualityFailureReason);

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", userPrompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json",
                        "responseSchema", buildResponseSchema()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(vertexAiTokenService.getAccessToken());

        ResponseEntity<String> response = restTemplate.exchange(
                java.net.URI.create(vertexAiProperties.getEndpointUrl()),
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class
        );
        batchMetrics.incrementVertexCallCount();

        String rawText = sanitizeJson(extractText(response.getBody()));
        return objectMapper.readValue(rawText, SynthesisResult.class);
    }

    private String buildUserPrompt(String categoryName, String keyword, List<RawArticleDto> articles,
                                   String qualityFailureReason) {
        int articleCount = articles.size();
        int perArticleLimit = articleCount > 0
                ? Math.min(MAX_CHARS_PER_ARTICLE, MAX_CHARS_PER_CLUSTER / articleCount)
                : MAX_CHARS_PER_ARTICLE;

        StringBuilder sb = new StringBuilder();

        if (qualityFailureReason != null) {
            sb.append("[이전 생성 실패 이유]\n")
              .append(qualityFailureReason)
              .append("\n위 문제를 해결하여 각 섹션을 더 풍부하게 재작성하세요.\n\n");
        }

        sb.append(String.format(
                "[요청 사항]\n요청 카테고리는 '%s', 수집 검색어는 '%s'입니다. 다음 %d개 기사의 중심 사건이 요청 카테고리에 실제로 해당하는지 먼저 판단하고, 가이드에 맞춰 JSON을 생성해주세요.\n\n[기사 데이터]\n",
                categoryName, keyword, articleCount));

        for (int i = 0; i < articleCount; i++) {
            RawArticleDto a = articles.get(i);
            String pressName = (a.getPressName() != null && !a.getPressName().isBlank())
                    ? a.getPressName() : "미상";
            String pubDate = (a.getPubDate() != null)
                    ? a.getPubDate().toLocalDate().toString() : "";
            String body = a.getDescription() != null ? a.getDescription() : "";
            String compressed = compressBody(body, perArticleLimit);

            sb.append(String.format("- 기사 %d 출처: %s", i + 1, pressName));
            if (!pubDate.isBlank()) sb.append(String.format(" (%s)", pubDate));
            sb.append("\n");
            sb.append(String.format("- 기사 %d 제목: %s\n", i + 1, a.getTitle()));
            sb.append(String.format("- 기사 %d 내용: %s\n\n", i + 1, compressed));
        }

        return sb.toString();
    }

    /**
     * 긴 본문을 lead(50%) / middle(20%) / tail(30%) 비율로 압축합니다.
     */
    private String compressBody(String body, int limit) {
        if (body.length() <= limit) return body;
        int leadLen   = (int)(limit * 0.50);
        int middleLen = (int)(limit * 0.20);
        int tailLen   = limit - leadLen - middleLen;

        String lead   = body.substring(0, leadLen);
        int midStart  = (body.length() - middleLen) / 2;
        String middle = body.substring(midStart, midStart + middleLen);
        String tail   = body.substring(body.length() - tailLen);

        return lead + " [...중략...] " + middle + " [...중략...] " + tail;
    }

    String findQualityFailureReason(SynthesisResult result) {
        if (result == null) return "결과가 null입니다.";
        if (result.getTitle() == null || result.getTitle().isBlank()) return "제목이 비어 있습니다.";
        if (result.getSummary() == null || result.getSummary().isBlank()) return "요약이 비어 있습니다.";

        List<String> summaryLines = Arrays.asList(result.getSummary().split("\\R", -1));
        if (summaryLines.size() != 4 || summaryLines.stream().anyMatch(String::isBlank)) {
            return String.format("요약 줄 수가 정확히 4줄이 아닙니다. lines=%d", summaryLines.size());
        }

        List<SectionDto> sections = result.getSections();
        if (sections == null || sections.size() != 3)
            return String.format("섹션 수(%d)가 정확히 3개가 아닙니다.", sections == null ? 0 : sections.size());

        for (int i = 0; i < sections.size(); i++) {
            SectionDto section = sections.get(i);
            if (section.getHeading() == null || section.getHeading().isBlank()) {
                return String.format("섹션 %d의 제목이 비어 있습니다.", i + 1);
            }
            if (section.getContent() == null || section.getContent().isBlank()) {
                return String.format("섹션 %d의 내용이 비어 있습니다.", i + 1);
            }
        }

        int totalLen = totalSectionContentLength(result);
        int minimumTotalLength = minimumTotalSectionLength(result);
        if (totalLen < minimumTotalLength)
            return String.format("전체 섹션 길이(%d자)가 근거 기사 수 기준 최소 길이(%d자)에 미달합니다.",
                    totalLen, minimumTotalLength);
        for (int i = 0; i < sections.size(); i++) {
            String content = sections.get(i).getContent();
            int len = content == null ? 0 : content.length();
            if (len < MIN_SECTION_CONTENT_LENGTH)
                return String.format("섹션 %d의 내용 길이(%d자)가 최소 기준(%d자)에 미달합니다.", i + 1, len, MIN_SECTION_CONTENT_LENGTH);
        }
        return null;
    }

    int minimumTotalSectionLength(SynthesisResult result) {
        int relevantSourceCount = result == null || result.getRelevantArticleIndexes() == null
                ? 0
                : (int) result.getRelevantArticleIndexes().stream()
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .count();
        int normalizedSourceCount = Math.max(2, Math.min(8, relevantSourceCount));
        return Math.min(MAX_TOTAL_SECTION_LENGTH,
                MIN_TOTAL_SECTION_LENGTH + (normalizedSourceCount - 2) * LENGTH_INCREMENT_PER_SOURCE);
    }

    private String findQualityFailureReason(SynthesisResult result, List<RawArticleDto> articles) {
        String formatFailure = findQualityFailureReason(result);
        if (formatFailure != null || result == null || !result.isCategoryRelevant()) {
            return formatFailure;
        }
        return groundingValidator.findFailureReason(result, articles);
    }

    private int totalSectionContentLength(SynthesisResult result) {
        List<SectionDto> sections = result.getSections();
        if (sections == null) return 0;
        return sections.stream()
                .mapToInt(s -> s.getContent() == null ? 0 : s.getContent().length())
                .sum();
    }

    private Map<String, Object> buildResponseSchema() {
        return Map.of(
                "type", "OBJECT",
                "required", List.of("categoryRelevant", "categoryReason", "title", "summary",
                        "relevantArticleIndexes", "sections"),
                "properties", Map.of(
                        "categoryRelevant", Map.of("type", "BOOLEAN"),
                        "categoryReason", Map.of("type", "STRING"),
                        "title",   Map.of("type", "STRING"),
                        "summary", Map.of("type", "STRING"),
                        "relevantArticleIndexes", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "INTEGER")
                        ),
                        "sections", Map.of(
                                "type", "ARRAY",
                                "minItems", 3,
                                "maxItems", 3,
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "required", List.of("heading", "content", "supportingArticleIndexes"),
                                        "properties", Map.of(
                                                "heading", Map.of("type", "STRING"),
                                                "content", Map.of("type", "STRING"),
                                                "supportingArticleIndexes", Map.of(
                                                        "type", "ARRAY",
                                                        "items", Map.of("type", "INTEGER")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private String sanitizeJson(String raw) {
        String text = raw.strip();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        return text.strip();
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty()) {
            throw new RuntimeException("Vertex AI 응답에 candidates가 없습니다. 콘텐츠 필터링 가능성: " + responseBody);
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (parts.isEmpty()) {
            throw new RuntimeException("Vertex AI 응답에 parts가 없습니다: " + responseBody);
        }
        return parts.get(0).path("text").asText();
    }
}

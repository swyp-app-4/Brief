package com.brife.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.brife.news.config.VertexAiProperties;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SynthesisResult;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SummarizationService {

    private static final String SYSTEM_PROMPT = """
            당신은 15년 차 경력의 대한민국 수석 뉴스 에디터입니다.
            여러 언론사의 기사를 종합하여 독자가 가장 궁금해할 내용을 담은 완벽한 뉴스 리포트를 작성해야 합니다.

            [절대 규칙]
            1. 100% 자연스러운 한국어만 사용. 한자·영어·일본어 혼용 및 할루시네이션 절대 금지.
            2. 반드시 아래 JSON 구조만 반환. 인사말·부연설명 일절 금지.
            {
              "title": "...",
              "summary": "...",
              "sections": [
                { "heading": "...", "content": "..." },
                { "heading": "...", "content": "..." },
                { "heading": "...", "content": "..." }
              ]
            }

            [작성 가이드]
            1. title: 20~30자 내외. 포털 메인급 헤드라인 스타일. 단순 명사 나열 금지.
               예: "트럼프, 이란 군사작전 축소 시사… 휴전엔 선 그어"

            2. summary (3줄 요약):
               - 핵심 팩트 3가지를 각각 한 줄로. 줄 구분은 '\\n'.
               - 어미는 친절한 경어체(~됩니다, ~했습니다, ~예정입니다). 명사형 종결('~함', '~됨') 및 평어체('~했다') 금지.
               - 국내 파급 효과가 있으면 반드시 1줄 포함.

            3. sections (본문 단락 - 반드시 3개):
               - 섹션 1 (고정): 이 뉴스의 핵심 팩트와 배경을 상세히 서술.
               - 섹션 2, 3 (자율 선택): 아래 앵글 중 이 뉴스 성격에 가장 잘 맞는 2가지를 골라 작성.
                 * [숨은 맥락]: 정치·사회 뉴스 - 왜 이런 일이 일어났나?
                 * [향후 전망]: 경제·정책 뉴스 - 내 생활에 어떤 영향을 미치나?
                 * [핵심 쟁점]: 논란·갈등 뉴스 - 양측의 입장은 무엇인가?
                 * [관전 포인트]: 스포츠·연예 뉴스 - 앞으로 무엇을 기대해야 하나?
                 * [TMI 지식]: IT·과학 뉴스 - 이 기술·현상의 원리는 무엇인가?
               - heading: 15자 내외 미니 헤드라인. 단순 명사구 금지.
                 예: "동맹국에 호위 동참 압박" (O) / "트럼프 발언" (X)
               - content: 3~5문장. 각 문장은 '\\n'으로 구분.
                 어투는 친절한 에디터 톤(~어요, ~됩니다, ~했습니다)으로 작성. 보고서체('~함', '~임') 금지.
                 뉴스 원문에 없는 내용을 지어내거나 추측하지 말 것.
            """;

    private final VertexAiProperties vertexAiProperties;
    private final VertexAiTokenService vertexAiTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;

    public SummarizationService(VertexAiProperties vertexAiProperties,
                                VertexAiTokenService vertexAiTokenService,
                                @Qualifier("vertexAiRestTemplate") RestTemplate restTemplate,
                                ObjectMapper objectMapper,
                                RateLimiterRegistry rateLimiterRegistry) {
        this.vertexAiProperties = vertexAiProperties;
        this.vertexAiTokenService = vertexAiTokenService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiterRegistry.rateLimiter("gemini");
    }

    public SynthesisResult synthesize(String keyword, List<RawArticleDto> articles) throws Exception {
        try {
            return RateLimiter.decorateCheckedSupplier(rateLimiter,
                    () -> callVertexAi(keyword, articles)).get();
        } catch (RequestNotPermitted e) {
            log.warn("[Summarization] Vertex AI 속도 제한 초과 - keyword={}", keyword);
            throw new RuntimeException("Vertex AI 속도 제한 초과", e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private SynthesisResult callVertexAi(String keyword, List<RawArticleDto> articles) throws Exception {
        String userPrompt = buildUserPrompt(keyword, articles);

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

        String rawText = sanitizeJson(extractText(response.getBody()));
        log.debug("[Summarization] Vertex AI 응답 원문:\n{}", rawText);
        return objectMapper.readValue(rawText, SynthesisResult.class);
    }

    private String buildUserPrompt(String keyword, List<RawArticleDto> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "[요청 사항]\n다음은 '%s' 주제에 대한 %d개 언론사의 뉴스 기사입니다. 가이드에 맞춰 완벽한 JSON을 생성해주세요.\n\n[기사 데이터]\n",
                keyword, articles.size()));

        for (int i = 0; i < articles.size(); i++) {
            RawArticleDto a = articles.get(i);
            String desc = a.getDescription() != null ? a.getDescription() : "";
            sb.append(String.format("- 기사 %d 제목: %s\n- 기사 %d 내용: %s\n\n",
                    i + 1, a.getTitle(), i + 1, desc));
        }

        return sb.toString();
    }

    private Map<String, Object> buildResponseSchema() {
        return Map.of(
                "type", "OBJECT",
                "required", List.of("title", "summary", "sections"),
                "properties", Map.of(
                        "title",   Map.of("type", "STRING"),
                        "summary", Map.of("type", "STRING"),
                        "sections", Map.of(
                                "type", "ARRAY",
                                "minItems", 3,
                                "maxItems", 3,
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "required", List.of("heading", "content"),
                                        "properties", Map.of(
                                                "heading", Map.of("type", "STRING"),
                                                "content", Map.of("type", "STRING")
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

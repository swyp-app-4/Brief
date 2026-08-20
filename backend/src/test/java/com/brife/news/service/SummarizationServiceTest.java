package com.brife.news.service;

import com.brife.news.batch.NewsBatchMetrics;
import com.brife.news.config.VertexAiProperties;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SynthesisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummarizationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;
    private SummarizationService service;

    @BeforeEach
    void setUp() throws Exception {
        VertexAiProperties properties = mock(VertexAiProperties.class);
        VertexAiTokenService tokenService = mock(VertexAiTokenService.class);
        restTemplate = mock(RestTemplate.class);
        when(properties.getEndpointUrl()).thenReturn("https://example.com/generate");
        when(tokenService.getAccessToken()).thenReturn("token");

        service = new SummarizationService(
                properties,
                tokenService,
                restTemplate,
                objectMapper,
                RateLimiterRegistry.ofDefaults(),
                new NewsBatchMetrics(),
                new SynthesisGroundingValidator());
    }

    @Test
    void retriesAndReturnsOnlyWhenSecondResultPassesAllQualityRules() throws Exception {
        SynthesisResult invalid = validResult();
        invalid.setSummary("첫 줄\n둘째 줄");
        SynthesisResult valid = validResult();

        when(restTemplate.exchange(any(java.net.URI.class), any(), any(), eq(String.class)))
                .thenReturn(vertexResponse(invalid), vertexResponse(valid));

        SynthesisResult result = service.synthesize("경제", "금리", List.of(article()));

        assertThat(result.getSummary().split("\\R")).hasSize(4);
        verify(restTemplate, org.mockito.Mockito.times(2))
                .exchange(any(java.net.URI.class), any(), any(), eq(String.class));
    }

    @Test
    void rejectsResultWhenRetryStillFailsQualityRules() throws Exception {
        SynthesisResult invalid = validResult();
        invalid.setSections(List.of(
                new SectionDto("제목1", "짧음"),
                new SectionDto("제목2", "짧음"),
                new SectionDto("제목3", "짧음")));

        when(restTemplate.exchange(any(java.net.URI.class), any(), any(), eq(String.class)))
                .thenReturn(vertexResponse(invalid), vertexResponse(invalid));

        assertThatThrownBy(() -> service.synthesize("경제", "금리", List.of(article())))
                .hasMessageContaining("요약 품질 기준 재시도 실패");
    }

    private ResponseEntity<String> vertexResponse(SynthesisResult result) throws Exception {
        String generatedJson = objectMapper.writeValueAsString(result);
        String responseJson = objectMapper.writeValueAsString(Map.of(
                "candidates", List.of(Map.of(
                        "content", Map.of("parts", List.of(Map.of("text", generatedJson)))))));
        return ResponseEntity.ok(responseJson);
    }

    private SynthesisResult validResult() {
        String content = "충분한 본문 내용입니다. ".repeat(30);
        SynthesisResult result = new SynthesisResult();
        result.setCategoryRelevant(true);
        result.setCategoryReason("경제 정책이 중심입니다.");
        result.setTitle("한국은행 기준금리 동결 결정");
        result.setSummary("첫 번째 핵심입니다.\n두 번째 핵심입니다.\n세 번째 핵심입니다.\n네 번째 핵심입니다.");
        result.setRelevantArticleIndexes(List.of(1));
        result.setSections(List.of(
                new SectionDto("핵심 상황을 살펴보다", content),
                new SectionDto("시장 영향을 분석하다", content),
                new SectionDto("향후 전망은 어떨까?", content)));
        result.getSections().forEach(section -> section.setSupportingArticleIndexes(List.of(1)));
        return result;
    }

    private RawArticleDto article() {
        return RawArticleDto.builder()
                .title("한국은행 기준금리 동결")
                .description("기사 본문")
                .sourceUrl("https://example.com/news")
                .naverUrl("https://n.news.naver.com/news")
                .pubDate(LocalDateTime.now())
                .pressName("테스트뉴스")
                .build();
    }
}

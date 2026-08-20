package com.brife.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final RateLimiter embeddingRateLimiter;
    private final SearchMetrics searchMetrics;

    public EmbeddingService(@Qualifier("vertexAiRestTemplate") RestTemplate restTemplate,
                            ObjectMapper objectMapper,
                            @Value("${gemini.api.key}") String apiKey,
                            RateLimiterRegistry rateLimiterRegistry,
                            SearchMetrics searchMetrics) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.embeddingRateLimiter = rateLimiterRegistry.rateLimiter("embedding");
        this.searchMetrics = searchMetrics;
    }

    public float[] embed(String text) throws Exception {
        return embedInternal(text, null);
    }

    public float[] embedForDocument(String text) throws Exception {
        return embedInternal(text, "RETRIEVAL_DOCUMENT");
    }

    public float[] embedForQuery(String text) throws Exception {
        return embedInternal(text, "RETRIEVAL_QUERY");
    }

    @Cacheable(value = "queryEmbeddings", key = "#text", sync = true)
    public float[] embedQueryCached(String text) {
        long startedAt = System.nanoTime();
        try {
            return embedForQuery(text);
        } catch (Exception e) {
            throw new RuntimeException("검색어 임베딩 생성 실패: " + e.getMessage(), e);
        } finally {
            searchMetrics.recordEmbeddingApi(startedAt);
        }
    }

    private float[] embedInternal(String text, String taskType) throws Exception {
        try {
            return RateLimiter.decorateCheckedSupplier(embeddingRateLimiter,
                    () -> callEmbeddingApi(text, taskType)).get();
        } catch (RequestNotPermitted e) {
            log.warn("[Embedding] Rate limit exceeded - taskType={}", taskType);
            throw new RuntimeException("임베딩 API 요청 한도 초과", e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private float[] callEmbeddingApi(String text, String taskType) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", Map.of("parts", List.of(Map.of("text", text))));
        body.put("outputDimensionality", 768);
        if (taskType != null) {
            body.put("taskType", taskType);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(String.format(EMBEDDING_URL, apiKey)),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        return parseEmbedding(response.getBody());
    }

    private float[] parseEmbedding(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode values = root.path("embedding").path("values");

        if (values.isMissingNode() || !values.isArray()) {
            throw new RuntimeException("임베딩 응답 파싱 실패: " + responseBody);
        }

        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = (float) values.get(i).asDouble();
        }
        return embedding;
    }
}

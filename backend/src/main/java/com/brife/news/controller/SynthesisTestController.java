package com.brife.news.controller;

import com.brife.news.config.NaverNewsProperties;
import com.brife.news.dto.NaverNewsResponse;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.scheduler.CrawlingScheduler;
import com.brife.news.service.ArticleClusteringService;
import com.brife.news.service.ArticleExtractorService;
import com.brife.news.service.SummarizationService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

// DB 저장 없이 수집~Gemini 합성까지 빠르게 테스트할 때 쓰는 컨트롤러.
// 나중에 삭제 예정.
@Profile("local")
@Slf4j
@RestController
@RequestMapping("/api/test")
public class SynthesisTestController {

    private final NaverNewsProperties naverProperties;
    private final ArticleExtractorService articleExtractor;
    private final ArticleClusteringService clusteringService;
    private final CrawlingScheduler crawlingScheduler;
    private final SummarizationService summarizationService;
    private final RestTemplate naverRestTemplate;

    public SynthesisTestController(NaverNewsProperties naverProperties,
                                   ArticleExtractorService articleExtractor,
                                   ArticleClusteringService clusteringService,
                                   CrawlingScheduler crawlingScheduler,
                                   SummarizationService summarizationService,
                                   @Qualifier("naverRestTemplate") RestTemplate naverRestTemplate) {
        this.naverProperties = naverProperties;
        this.articleExtractor = articleExtractor;
        this.clusteringService = clusteringService;
        this.crawlingScheduler = crawlingScheduler;
        this.summarizationService = summarizationService;
        this.naverRestTemplate = naverRestTemplate;
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> runBatch() {
        log.info("[Test] 배치 수동 실행 시작");
        try {
            crawlingScheduler.runCrawling();
            return ResponseEntity.ok(Map.of("result", "배치 실행 완료"));
        } catch (Exception e) {
            log.error("[Test] 배치 실행 실패", e);
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/synthesize")
    public ResponseEntity<Map<String, Object>> synthesize(
            @RequestParam(defaultValue = "경제") String keyword,
            @RequestParam(defaultValue = "30") int display) {

        log.info("[Test] 시작 - keyword={}, display={}", keyword, display);

        List<RawArticleDto> raw = fetchFromNaver(keyword, display);
        log.info("[Test] Naver 수집 완료 - {}건", raw.size());

        if (raw.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Naver API에서 기사를 가져오지 못했습니다."));
        }

        List<RawArticleDto> articles = clusteringService.cluster(raw, keyword);
        if (articles.isEmpty()) {
            return ResponseEntity.ok(Map.of("error",
                    "'" + keyword + "' 키워드로 동일 토픽 기사 3개 이상을 찾지 못했습니다."));
        }
        log.info("[Test] 클러스터링 완료 - {}건 → {}건", raw.size(), articles.size());

        SynthesisResult result;
        try {
            result = summarizationService.synthesize(keyword, articles);
            log.info("[Test] Vertex AI 완료 - title={}", result.getTitle());
        } catch (Exception e) {
            log.error("[Test] Vertex AI 실패", e);
            return ResponseEntity.ok(Map.of("error", "Vertex AI 호출 실패: " + e.getMessage()));
        }

        List<Map<String, String>> sources = articles.stream()
                .map(a -> Map.of(
                        "title", a.getTitle(),
                        "url", a.getSourceUrl(),
                        "bodyLength", a.getDescription().length() + "자"
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "keyword", keyword,
                "articleCount", articles.size(),
                "generatedTitle", result.getTitle(),
                "summary", result.getSummary(),
                "sections", result.getSections(),
                "sources", sources
        ));
    }

    // 여러 키워드 한번에 테스트. 순차 처리.
    @GetMapping("/multi")
    public ResponseEntity<List<Map<String, Object>>> multiSynthesize(
            @RequestParam(defaultValue = "코스피,AI,야구") String keywords,
            @RequestParam(defaultValue = "30") int display) {

        List<String> keywordList = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(k -> !k.isBlank())
                .toList();

        log.info("[Test/Multi] 시작 - keywords={}", keywordList);

        List<Map<String, Object>> results = new ArrayList<>();

        for (String keyword : keywordList) {
            log.info("[Test/Multi] 처리 중 - keyword={}", keyword);
            List<RawArticleDto> raw = fetchFromNaver(keyword, display);

            if (raw.isEmpty()) {
                results.add(Map.of("keyword", keyword, "error", "Naver 기사 없음"));
                continue;
            }

            List<RawArticleDto> articles = clusteringService.cluster(raw, keyword);
            if (articles.isEmpty()) {
                results.add(Map.of("keyword", keyword, "error", "클러스터링 실패 (유사 기사 부족)"));
                continue;
            }

            try {
                SynthesisResult result = summarizationService.synthesize(keyword, articles);
                List<Map<String, String>> sources = articles.stream()
                        .map(a -> Map.of(
                                "title", a.getTitle(),
                                "url", a.getSourceUrl(),
                                "bodyLength", a.getDescription().length() + "자"
                        ))
                        .toList();

                results.add(Map.of(
                        "keyword", keyword,
                        "articleCount", articles.size(),
                        "generatedTitle", result.getTitle(),
                        "summary", result.getSummary(),
                        "sections", result.getSections(),
                        "sources", sources
                ));
                log.info("[Test/Multi] 완료 - keyword={}, title={}", keyword, result.getTitle());
            } catch (Exception e) {
                log.error("[Test/Multi] Vertex AI 실패 - keyword={}", keyword, e);
                results.add(Map.of("keyword", keyword, "error", "Vertex AI 실패: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(results);
    }

    private List<RawArticleDto> fetchFromNaver(String keyword, int display) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverProperties.getClientId());
        headers.set("X-Naver-Client-Secret", naverProperties.getClientSecret());

        String url = UriComponentsBuilder.fromUriString(naverProperties.getNewsUrl())
                .queryParam("query", keyword)
                .queryParam("display", display)
                .queryParam("sort", "sim")
                .build()
                .toUriString();

        try {
            ResponseEntity<NaverNewsResponse> response = naverRestTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), NaverNewsResponse.class);

            if (response.getBody() == null || response.getBody().getItems() == null) {
                return Collections.emptyList();
            }

            return response.getBody().getItems().stream()
                    .map(this::toRawArticle)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("[Test] Naver API 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private RawArticleDto toRawArticle(NaverNewsResponse.NaverNewsItem item) {
        String cleanTitle = Jsoup.parse(item.getTitle()).text();
        String cleanDesc  = Jsoup.parse(item.getDescription()).text();

        String sourceUrl = (item.getOriginalLink() != null && !item.getOriginalLink().isBlank())
                ? item.getOriginalLink() : item.getLink();
        String naverUrl = item.getLink();

        String description = cleanDesc;

        try {
            Document doc = Jsoup.connect(sourceUrl)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://www.google.com")
                    .get();

            String body = articleExtractor.extractFromDoc(doc);
            if (!body.isBlank()) description = body;

            log.info("[Test] 본문 {}자 - {}", description.length(), cleanTitle);
        } catch (Exception e) {
            log.debug("[Test] 원문 접속 실패 - url={}", sourceUrl);
        }

        return RawArticleDto.builder()
                .title(cleanTitle)
                .description(description)
                .sourceUrl(sourceUrl)
                .naverUrl(naverUrl)
                .pubDate(LocalDateTime.now())
                .build();
    }
}

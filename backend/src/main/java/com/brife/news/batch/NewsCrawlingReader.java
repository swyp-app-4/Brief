package com.brife.news.batch;

import com.brife.news.config.NaverNewsProperties;
import com.brife.news.domain.Category;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.NaverNewsResponse;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.service.ArticleClusteringService;
import com.brife.news.service.ArticleExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;


@Slf4j
@Component
@StepScope
public class NewsCrawlingReader implements ItemReader<KeywordGroupDto> {

    private final NaverNewsProperties properties;
    private final RestTemplate restTemplate;
    private final ArticleExtractorService articleExtractor;
    private final ArticleClusteringService clusteringService;
    private final CategoryRepository categoryRepository;
    private final Executor crawlingExecutor;
    private final Retry naverRetry;

    private final Queue<KeywordGroupDto> queue = new ConcurrentLinkedQueue<>();
    private boolean initialized = false;

    public NewsCrawlingReader(NaverNewsProperties properties,
                              @Qualifier("naverRestTemplate") RestTemplate restTemplate,
                              ArticleExtractorService articleExtractor,
                              ArticleClusteringService clusteringService,
                              CategoryRepository categoryRepository,
                              @Qualifier("crawlingExecutor") Executor crawlingExecutor,
                              RetryRegistry retryRegistry) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.articleExtractor = articleExtractor;
        this.clusteringService = clusteringService;
        this.categoryRepository = categoryRepository;
        this.crawlingExecutor = crawlingExecutor;
        this.naverRetry = retryRegistry.retry("naver");
    }

    @Override
    public KeywordGroupDto read() {
        if (!initialized) {
            fetchAll();
            initialized = true;
        }
        return queue.poll();
    }

    private void fetchAll() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("[Reader] category 테이블이 비어있음. DB에 카테고리를 먼저 등록.");
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Category category : categories) {
            String keyword = category.getEffectiveQuery();
            int minClusterSize = resolveMinClusterSize(category.getName());
            CompletableFuture<Void> future = CompletableFuture
                    .supplyAsync(() -> clusteringService.clusterAll(fetchMerged(keyword), keyword, minClusterSize), crawlingExecutor)
                    .thenAccept(clusters -> {
                        for (List<RawArticleDto> articles : clusters) {
                            queue.add(KeywordGroupDto.builder()
                                    .categoryId(category.getId())
                                    .categoryName(category.getName())
                                    .keyword(keyword)
                                    .articles(articles)
                                    .build());
                        }
                        if (!clusters.isEmpty()) {
                            log.info("[Reader] category={}, keyword={}, {}개 토픽 수집",
                                    category.getName(), keyword, clusters.size());
                        }
                    })
                    .exceptionally(e -> {
                        log.error("[Reader] 수집 실패 - category={}, error={}", category.getName(), e.getMessage());
                        return null;
                    });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("[Reader] 총 {}개 키워드 그룹 큐 적재 완료", queue.size());
    }

    // 소분류별 클러스터 최소 기사 수 설정
    private int resolveMinClusterSize(String categoryName) {
        if (Set.of("사회일반", "세계일반", "국회/정당", "지역", "정치일반", "교육")
                .contains(categoryName)) return 6;
        if (Set.of("야구", "배구", "북한", "노동", "IT일반", "중기/벤처")
                .contains(categoryName)) return 5;
        if (Set.of("책", "아웃도어", "게임/리뷰", "e스포츠", "스포츠일반", "생활문화일반", "언론")
                .contains(categoryName)) return 3;
        return 4;
    }

    // 구분 키워드별 각각 호출 후 sourceUrl 기준 중복 제거해서 합침
    private List<RawArticleDto> fetchMerged(String query) {
        String[] keywords = query.split("\\|");
        if (keywords.length == 1) {
            return fetchFromNaver(query.trim());
        }
        Map<String, RawArticleDto> merged = new java.util.LinkedHashMap<>();
        for (String kw : keywords) {
            for (RawArticleDto article : fetchFromNaver(kw.trim())) {
                merged.putIfAbsent(article.getSourceUrl(), article);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<RawArticleDto> fetchFromNaver(String keyword) {
        try {
            return Retry.decorateCheckedSupplier(naverRetry, () -> doFetchFromNaver(keyword)).get();
        } catch (Throwable e) {
            log.warn("[Naver API] 재시도 후 최종 실패 - keyword={}, error={}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RawArticleDto> doFetchFromNaver(String keyword) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", properties.getClientId());
        headers.set("X-Naver-Client-Secret", properties.getClientSecret());

        // 최신순 100개 수집
        String url = UriComponentsBuilder.fromUriString(properties.getNewsUrl())
                .queryParam("query", keyword)
                .queryParam("display", 100)
                .queryParam("sort", "date")
                .build()
                .toUriString();

        ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), NaverNewsResponse.class);

        if (response.getBody() == null || response.getBody().getItems() == null) {
            return Collections.emptyList();
        }

        // 카테고리는 이미 병렬로 돌고 있어서 내부 파싱은 순차 처리 (풀 재사용 시 데드락 방지)
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return response.getBody().getItems().stream()
                .map(this::toRawArticle)
                .filter(Objects::nonNull)
                .filter(a -> a.getPubDate() != null && a.getPubDate().isAfter(since))
                .toList();
    }

    private RawArticleDto toRawArticle(NaverNewsResponse.NaverNewsItem item) {
        String cleanTitle = Jsoup.parse(item.getTitle()).text();
        String cleanDesc  = Jsoup.parse(item.getDescription()).text();
        String sourceUrl  = resolveSourceUrl(item);

        String body = cleanDesc;
        String pressName = "";
        try {
            Document doc = Jsoup.connect(sourceUrl)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://www.google.com")
                    .get();
            String extracted = articleExtractor.extractFromDoc(doc);
            if (!extracted.isBlank()) body = extracted;
            pressName = articleExtractor.extractPressName(doc);
        } catch (Exception e) {
            log.debug("[Reader] 원문 접속 실패 - url={}", sourceUrl);
        }

        return RawArticleDto.builder()
                .title(cleanTitle)
                .description(body)
                .sourceUrl(sourceUrl)
                .naverUrl(item.getLink())
                .pubDate(parseDate(item.getPubDate()))
                .pressName(pressName)
                .build();
    }

    // 네이버 URL로 대체
    private String resolveSourceUrl(NaverNewsResponse.NaverNewsItem item) {
        return (item.getOriginalLink() != null && !item.getOriginalLink().isBlank())
                ? item.getOriginalLink()
                : item.getLink();
    }

    private LocalDateTime parseDate(String pubDate) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return ZonedDateTime.parse(pubDate, fmt)
                    .withZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

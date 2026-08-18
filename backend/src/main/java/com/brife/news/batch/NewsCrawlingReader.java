package com.brife.news.batch;

import com.brife.news.config.NaverNewsProperties;
import com.brife.news.domain.Category;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.NaverNewsResponse;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
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
import java.util.concurrent.ConcurrentHashMap;
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
    private final RawNewsRepository rawNewsRepository;
    private final Executor crawlingExecutor;
    private final Retry naverRetry;
    private final NewsBatchMetrics batchMetrics;

    private final Queue<KeywordGroupDto> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, RawArticleDto> extractedArticleCache = new ConcurrentHashMap<>();
    private final Map<String, Object> extractionLocks = new ConcurrentHashMap<>();
    private boolean initialized = false;

    public NewsCrawlingReader(NaverNewsProperties properties,
                              @Qualifier("naverRestTemplate") RestTemplate restTemplate,
                              ArticleExtractorService articleExtractor,
                              ArticleClusteringService clusteringService,
                              CategoryRepository categoryRepository,
                              RawNewsRepository rawNewsRepository,
                              @Qualifier("crawlingExecutor") Executor crawlingExecutor,
                              RetryRegistry retryRegistry,
                              NewsBatchMetrics batchMetrics) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.articleExtractor = articleExtractor;
        this.clusteringService = clusteringService;
        this.categoryRepository = categoryRepository;
        this.rawNewsRepository = rawNewsRepository;
        this.crawlingExecutor = crawlingExecutor;
        this.naverRetry = retryRegistry.retry("naver");
        this.batchMetrics = batchMetrics;
    }

    @Override
    public synchronized KeywordGroupDto read() {
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
                        batchMetrics.addClusterCount(clusters.size());
                        for (List<RawArticleDto> articles : clusters) {
                            queue.add(KeywordGroupDto.builder()
                                    .categoryId(category.getId())
                                    .categoryName(category.getName())
                                    .keyword(keyword)
                                    .minClusterSize(minClusterSize)
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

    // 날짜·DB 중복을 먼저 제거한 뒤 남은 기사만 원문을 요청합니다.
    private List<RawArticleDto> fetchMerged(String query) {
        String[] keywords = query.split("\\|");
        Map<String, PendingArticle> merged = new LinkedHashMap<>();
        for (String kw : keywords) {
            for (PendingArticle article : fetchFromNaver(kw.trim())) {
                merged.putIfAbsent(article.sourceUrl(), article);
            }
        }

        List<String> naverUrls = merged.values().stream()
                .map(article -> article.item().getLink())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<String> existingUrls = naverUrls.isEmpty()
                ? Set.of()
                : rawNewsRepository.findExistingNaverUrls(naverUrls);
        List<String> sourceUrls = new ArrayList<>(merged.keySet());
        Set<String> existingSourceUrls = sourceUrls.isEmpty()
                ? Set.of()
                : rawNewsRepository.findExistingSourceUrls(sourceUrls);

        return merged.values().stream()
                .filter(article -> article.item().getLink() != null)
                .filter(article -> !existingUrls.contains(article.item().getLink()))
                .filter(article -> !existingSourceUrls.contains(article.sourceUrl()))
                .map(this::extractCached)
                .filter(Objects::nonNull)
                .toList();
    }

    private RawArticleDto extractCached(PendingArticle pending) {
        RawArticleDto cached = extractedArticleCache.get(pending.sourceUrl());
        if (cached != null) return cached;

        Object lock = extractionLocks.computeIfAbsent(pending.sourceUrl(), ignored -> new Object());
        synchronized (lock) {
            try {
                return extractedArticleCache.computeIfAbsent(pending.sourceUrl(), ignored -> toRawArticle(pending));
            } finally {
                extractionLocks.remove(pending.sourceUrl(), lock);
            }
        }
    }

    private List<PendingArticle> fetchFromNaver(String keyword) {
        try {
            return Retry.decorateCheckedSupplier(naverRetry, () -> doFetchFromNaver(keyword)).get();
        } catch (Throwable e) {
            log.warn("[Naver API] 재시도 후 최종 실패 - keyword={}, error={}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<PendingArticle> doFetchFromNaver(String keyword) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", properties.getClientId());
        headers.set("X-Naver-Client-Secret", properties.getClientSecret());

        // TODO: 테스트 후 100으로 복구
        String url = UriComponentsBuilder.fromUriString(properties.getNewsUrl())
                .queryParam("query", keyword)
                .queryParam("display", 50)
                .queryParam("sort", "date")
                .build()
                .toUriString();

        ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), NaverNewsResponse.class);

        batchMetrics.incrementNaverApiCallCount();
        if (response.getBody() == null || response.getBody().getItems() == null) {
            return Collections.emptyList();
        }
        batchMetrics.addFetchedArticleCount(response.getBody().getItems().size());

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return response.getBody().getItems().stream()
                .map(item -> toPendingArticle(item, since))
                .filter(Objects::nonNull)
                .toList();
    }

    private PendingArticle toPendingArticle(NaverNewsResponse.NaverNewsItem item, LocalDateTime since) {
        LocalDateTime pubDate = parseDate(item.getPubDate());
        if (pubDate == null || !pubDate.isAfter(since)) return null;
        String sourceUrl = resolveSourceUrl(item);
        if (sourceUrl == null || sourceUrl.isBlank()) return null;
        return new PendingArticle(item, sourceUrl, pubDate);
    }

    private RawArticleDto toRawArticle(PendingArticle pending) {
        NaverNewsResponse.NaverNewsItem item = pending.item();
        String cleanTitle = Jsoup.parse(item.getTitle()).text();
        String cleanDesc  = Jsoup.parse(item.getDescription()).text();
        String sourceUrl  = pending.sourceUrl();

        String body = cleanDesc;
        String pressName = "";
        boolean extractSuccess = false;
        try {
            Document doc = Jsoup.connect(sourceUrl)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://www.google.com")
                    .get();
            String extracted = articleExtractor.extractFromDoc(doc);
            if (!extracted.isBlank()) {
                body = extracted;
                extractSuccess = true;
            }
            pressName = articleExtractor.extractPressName(doc, sourceUrl);
        } catch (Exception e) {
            log.debug("[Reader] 원문 접속 실패 - url={}", sourceUrl);
            pressName = articleExtractor.resolvePressNameFromDomain(sourceUrl);
        }
        if (extractSuccess) {
            batchMetrics.incrementOriginalExtractSuccessCount();
        } else {
            batchMetrics.incrementOriginalExtractFallbackCount();
        }
        batchMetrics.addTotalOriginalTextLength(body.length());

        return RawArticleDto.builder()
                .title(cleanTitle)
                .description(body)
                .sourceUrl(sourceUrl)
                .naverUrl(item.getLink())
                .pubDate(pending.pubDate())
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
            log.debug("[Reader] 발행일 파싱 실패 - value={}", pubDate);
            return null;
        }
    }

    private record PendingArticle(NaverNewsResponse.NaverNewsItem item,
                                  String sourceUrl,
                                  LocalDateTime pubDate) {
    }
}

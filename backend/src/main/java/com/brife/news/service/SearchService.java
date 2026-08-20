package com.brife.news.service;

import com.brife.news.dto.NewsSearchResponse;
import com.brife.news.exception.InvalidSearchKeywordException;
import com.brife.news.repository.HybridNewsSearchRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final SummarizedNewsRepository summarizedNewsRepository;
    private final HybridNewsSearchRepository hybridSearchRepository;
    private final EmbeddingService embeddingService;
    private final SearchMetrics searchMetrics;

    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getSummarizedNewsByKeyword(String keyword, Pageable pageable) {
        long totalStartedAt = System.nanoTime();
        try {
            keyword = normalizeAndValidate(keyword);

            try {
                long embeddingStartedAt = System.nanoTime();
                float[] queryVector;
                try {
                    queryVector = embeddingService.embedQueryCached(keyword);
                } finally {
                    searchMetrics.recordEmbeddingLookup(embeddingStartedAt);
                }

                long databaseStartedAt = System.nanoTime();
                try {
                    return hybridSearchRepository.search(keyword, queryVector, pageable);
                } finally {
                    searchMetrics.recordHybridDatabase(databaseStartedAt);
                }
            } catch (Exception e) {
                searchMetrics.incrementFallback();
                log.warn("[Search] hybrid search failed, fallback to keyword search - keyword={}, error={}",
                        keyword, e.getMessage());
                long fallbackStartedAt = System.nanoTime();
                try {
                    return summarizedNewsRepository.searchByKeyword(keyword,
                                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                            .map(NewsSearchResponse::from);
                } finally {
                    searchMetrics.recordFallbackDatabase(fallbackStartedAt);
                }
            }
        } finally {
            searchMetrics.recordTotal(totalStartedAt);
        }
    }

    private String normalizeAndValidate(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) {
            throw new InvalidSearchKeywordException("검색어를 입력해주세요.");
        }

        String normalized = keyword.trim().replaceAll("\\s+", " ");
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidSearchKeywordException("검색어는 100자 이하로 입력해주세요.");
        }
        if (!normalized.matches("[가-힣a-zA-Z0-9 ]+")) {
            throw new InvalidSearchKeywordException("특수문자를 제외한 검색어를 입력해주세요.");
        }
        return normalized;
    }

    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getAllSummarizedNewsByPublishedDesc(Pageable pageable) {
        Pageable stablePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return summarizedNewsRepository.findLatest(stablePageable).map(NewsSearchResponse::from);
    }
}

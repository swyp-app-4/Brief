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
    private final SummarizedNewsRepository summarizedNewsRepository;
    private final HybridNewsSearchRepository hybridSearchRepository;
    private final EmbeddingService embeddingService;

    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getSummarizedNewsByKeyword(String keyword, Pageable pageable) {
        keyword = keyword.trim();
        if (keyword.isBlank()) {
            throw new InvalidSearchKeywordException("검색어를 입력해주세요.");
        }
        if (!keyword.matches("[가-힣a-zA-Z0-9 ]+")) {
            throw new InvalidSearchKeywordException("특수문자를 제외한 검색어를 입력해주세요.");
        }

        try {
            float[] queryVector = embeddingService.embedQueryCached(keyword);
            return hybridSearchRepository.search(keyword, queryVector, pageable);
        } catch (Exception e) {
            log.warn("[Search] hybrid search failed, fallback to keyword search - keyword={}, error={}",
                    keyword, e.getMessage());
            return summarizedNewsRepository.searchByKeyword(keyword,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                    .map(NewsSearchResponse::from);
        }
    }

    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getAllSummarizedNewsByPublishedDesc(Pageable pageable) {
        return summarizedNewsRepository.findAllBy(pageable).map(NewsSearchResponse::from);
    }
}

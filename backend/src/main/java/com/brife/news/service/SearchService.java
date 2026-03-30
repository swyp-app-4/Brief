package com.brife.news.service;

import com.brife.news.dto.NewsSearchResponse;
import com.brife.news.exception.InvalidSearchKeywordException;
import com.brife.news.repository.SummarizedNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SummarizedNewsRepository summarizedNewsRepository;

    // 검색 키워드 기반 조회(공백 및 특수문자는 예외 처리)
    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getSummarizedNewsByKeyword(String keyword, Pageable pageable) {
        keyword = keyword.trim();
        if(keyword.isBlank())
            throw new InvalidSearchKeywordException("검색어를 입력해주세요.");

        if(keyword.matches("[가-힣a-zA-Z0-9 ]+"))
            return summarizedNewsRepository.searchByKeyword(keyword,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())).map(NewsSearchResponse::from);
        else throw new InvalidSearchKeywordException("특수문자를 제외한 키워드로 입력해주세요.");
    }

    // 탐색 탭을 누르면 기본으로 전체 조회
    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> getAllSummarizedNewsByPublishedDesc(Pageable pageable) {
        return summarizedNewsRepository.findAllBy(pageable).map(NewsSearchResponse::from);
    }
}
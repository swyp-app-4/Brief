package com.brife.news.controller;

import com.brife.news.dto.NewsSearchResponse;
import com.brife.news.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="뉴스", description="뉴스 최신순 조회 및 검색 조회")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @Operation(summary = "키워드 검색 결과 조회", description = "탐색 탭에서 키워드로 검색 시 반환되는 뉴스 조회")
    @GetMapping("/search")
    public ResponseEntity<Slice<NewsSearchResponse>> searchNews(@Parameter(description = "검색어") @RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(searchService.getSummarizedNewsByKeyword(keyword, pageable));
    }

    @Operation(summary = "전체 뉴스 조회", description = "탐색 탭에서 최근 검색어가 생기기 전 초기 상태에서 전체 뉴스 조회")
    @GetMapping("/latest")
    public ResponseEntity<Slice<NewsSearchResponse>> getLatestNews(Pageable pageable) {
        return ResponseEntity.ok(searchService.getAllSummarizedNewsByPublishedDesc(pageable));
    }
}

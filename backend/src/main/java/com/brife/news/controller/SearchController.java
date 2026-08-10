package com.brife.news.controller;

import com.brife.news.dto.NewsSearchResponse;
import com.brife.news.dto.SearchSuggestionResponse;
import com.brife.news.service.SearchService;
import com.brife.news.service.SearchSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "뉴스", description = "뉴스 최신순 조회 및 검색")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final SearchSuggestionService searchSuggestionService;

    @Operation(summary = "뉴스 검색 결과 조회", description = "검색어 기반 하이브리드 검색 결과를 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<Slice<NewsSearchResponse>> searchNews(
            @Parameter(description = "검색어") @RequestParam String keyword,
            @ParameterObject @SortDefault(sort = "publishedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(searchService.getSummarizedNewsByKeyword(keyword, pageable));
    }

    @Operation(summary = "전체 뉴스 조회", description = "최근 뉴스 목록을 최신순으로 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<Slice<NewsSearchResponse>> getLatestNews(
            @ParameterObject @SortDefault.SortDefaults({
                    @SortDefault(sort = "publishedAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            }) Pageable pageable) {
        return ResponseEntity.ok(searchService.getAllSummarizedNewsByPublishedDesc(pageable));
    }

    @Operation(summary = "검색어 자동완성", description = "입력 검색어 기반 뉴스 제목 후보 최대 5개를 반환합니다.")
    @GetMapping("/search/suggestions")
    public ResponseEntity<SearchSuggestionResponse> getSearchSuggestions(
            @Parameter(description = "검색어 (2자 이상)") @RequestParam String keyword) {
        return ResponseEntity.ok(searchSuggestionService.getSuggestions(keyword));
    }
}

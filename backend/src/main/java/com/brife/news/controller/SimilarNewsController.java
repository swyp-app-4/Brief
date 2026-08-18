package com.brife.news.controller;

import com.brife.news.dto.SimilarNewsResponse;
import com.brife.news.service.SimilarNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "유사 뉴스", description = "임베딩 기반 유사 기사 추천")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class SimilarNewsController {

    private final SimilarNewsService similarNewsService;

    @Operation(summary = "유사 기사 추천", description = "해당 뉴스와 임베딩 유사도가 높은 기사 최대 5개를 반환합니다.")
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<SimilarNewsResponse>> getSimilarNews(@PathVariable Long id) {
        return ResponseEntity.ok(similarNewsService.getSimilarNews(id));
    }
}

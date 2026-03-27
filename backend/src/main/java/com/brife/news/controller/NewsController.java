package com.brife.news.controller;

import com.brife.news.dto.NewsDetailDto;
import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "위젯 뉴스", description = "관심사 기반 Top5 뉴스 위젯")
@Validated
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "관심사 기반 Top5 뉴스", description = "유저가 선택한 소분류 카테고리 기준 sourceCount Top5 뉴스 반환")
    @GetMapping("/top5/personal")
    public ResponseEntity<List<WidgetNewsDto>> getPersonalTop5(
            @RequestParam @NotEmpty @Size(max = 10) List<Long> categoryIds) {
        return ResponseEntity.ok(newsService.getTop5NewsByCategories(categoryIds));
    }

    @Operation(summary = "뉴스 상세 조회", description = "뉴스 ID로 제목, 3줄 요약, 본문 섹션 전체 반환")
    @GetMapping("/{id}")
    public ResponseEntity<NewsDetailDto> getNewsDetail(
            @Parameter(description = "뉴스 ID") @PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsDetail(id));
    }
}

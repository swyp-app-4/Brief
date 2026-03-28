package com.brife.news.controller;

import com.brife.news.dto.NewsDetailDto;
import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(
        summary = "관심사 기반 Top5 뉴스",
        description = "소분류(categoryIds), 대분류(groupIds) 혼합 가능. 최소 하나는 필수."
    )
    @GetMapping("/top5")
    public ResponseEntity<List<WidgetNewsDto>> getTop5(
            @RequestParam(required = false, defaultValue = "") @Size(max = 10) List<Long> categoryIds,
            @RequestParam(required = false, defaultValue = "") @Size(max = 10) List<Long> groupIds) {
        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(newsService.getTop5News(categoryIds, groupIds));
    }

    @Operation(summary = "뉴스 상세 조회", description = "뉴스 ID로 제목, 3줄 요약, 본문 섹션 전체 반환")
    @GetMapping("/{id}")
    public ResponseEntity<NewsDetailDto> getNewsDetail(
            @Parameter(description = "뉴스 ID") @PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsDetail(id));
    }
}

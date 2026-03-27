package com.brife.news.controller;

import com.brife.news.dto.CategoryGroupDetailResponse;
import com.brife.news.dto.CategoryGroupResponse;
import com.brife.news.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리", description = "온보딩용 카테고리 조회")
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "대분류 카테고리 조회", description = "온보딩에 사용할 6개의 고정 관심사 카테고리 목록을 반환")
    @GetMapping("/groups")
    public ResponseEntity<List<CategoryGroupResponse>> getCategoryGroups() {
        return ResponseEntity.ok(categoryService.getCategoryGroups());
    }

    @Operation(summary = "소분류 카테고리 조회", description = "온보딩에서 사용자가 선택한 대분류에 속한 소분류 카테고리 목록을 그룹별로 반환")
    @GetMapping("/groups/details")
    public ResponseEntity<List<CategoryGroupDetailResponse>> getCategoryGroupDetails(
            @Parameter(description = "대분류 카테고리의 PK 목록")
            @RequestParam @NotEmpty List<Long> groupIds) {
        return ResponseEntity.ok(categoryService.getCategoryGroupDetails(groupIds));
    }
}

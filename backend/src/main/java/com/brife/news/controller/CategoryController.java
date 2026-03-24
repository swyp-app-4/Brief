package com.brife.news.controller;

import com.brife.news.dto.CategoryGroupResponse;
import com.brife.news.dto.CategoryResponse;
import com.brife.news.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryGroupResponse>> getCategoryGroups() {
        return ResponseEntity.ok(categoryService.getOnBoardingCategoryGroups());
    }

    @GetMapping("/details")
    public ResponseEntity<List<CategoryResponse>> getCategories(@RequestParam List<Long> categoryGroupIds) {
        return ResponseEntity.ok(categoryService.getOnBoardingCategory(categoryGroupIds));
    }
}

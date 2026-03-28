// [카테고리 API] GET /categories/groups (대분류 전체), GET /categories/groups/details (소분류 그룹별).
package com.brife.category.controller;

import com.brife.category.dto.CategoryGroupDetailResponse;
import com.brife.category.dto.CategoryGroupResponse;
import com.brife.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/groups")
    public ResponseEntity<List<CategoryGroupResponse>> getAllGroups() {
        return ResponseEntity.ok(categoryService.getAllGroups());
    }

    @GetMapping("/groups/details")
    public ResponseEntity<List<CategoryGroupDetailResponse>> getCategoriesByGroups(
            @RequestParam List<Long> groupIds) {
        return ResponseEntity.ok(categoryService.getCategoriesByGroups(groupIds));
    }
}

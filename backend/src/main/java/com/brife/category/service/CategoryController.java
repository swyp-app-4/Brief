package com.brife.category.service;

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

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(categoryService.getCategoriesByGroup(groupId));
    }
}

// [DTO - 응답] 대분류 그룹 + 소속 소분류 목록 응답 (온보딩 소분류 선택 화면).
package com.brife.category.dto;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryGroupDetailResponse {

    private final Long id;
    private final String groupName;
    private final List<CategoryResponse> categories;

    public CategoryGroupDetailResponse(CategoryGroup group, List<Category> categories) {
        this.id = group.getId();
        this.groupName = group.getName();
        this.categories = categories.stream()
                .map(CategoryResponse::new)
                .toList();
    }
}

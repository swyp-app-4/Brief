package com.brife.news.dto;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;

import java.util.List;

public record CategoryGroupDetailResponse(Long id, String groupName, List<CategoryItem> categories) {

    public record CategoryItem(Long id, String name, String query) {}

    public static CategoryGroupDetailResponse from(CategoryGroup group, List<Category> categories) {
        List<CategoryItem> items = categories.stream()
                .map(c -> new CategoryItem(c.getId(), c.getName(), c.getEffectiveQuery()))
                .toList();
        return new CategoryGroupDetailResponse(group.getId(), group.getName(), items);
    }
}

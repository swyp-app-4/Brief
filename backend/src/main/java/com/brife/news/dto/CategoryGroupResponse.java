package com.brife.news.dto;

import com.brife.category.domain.CategoryGroup;

public record CategoryGroupResponse(Long id, String groupName) {
    public static CategoryGroupResponse from(CategoryGroup entity) {
        return new CategoryGroupResponse(entity.getId(), entity.getName());
    }
}

package com.brife.news.dto;

import com.brife.news.domain.CategoryGroup;

public record CategoryGroupResponse(Long id, String name) {
    public static CategoryGroupResponse from(CategoryGroup entity) {
        return new CategoryGroupResponse(entity.getId(), entity.getName());
    }
}
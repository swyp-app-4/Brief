package com.brife.news.dto;

import com.brife.news.domain.Category;

public record CategoryResponse(Long id, Long categoryGroupId, String name) {
    public static CategoryResponse from(Category entity) {
        return new CategoryResponse(entity.getId(), entity.getCategoryGroup().getId(), entity.getName());
    }
}
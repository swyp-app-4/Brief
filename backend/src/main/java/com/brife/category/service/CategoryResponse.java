package com.brife.category.service;

import com.brife.category.domain.Category;
import lombok.Getter;

@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final String query;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.query = category.getQuery();
    }
}

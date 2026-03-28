// [DTO - 응답] 소분류 카테고리 응답 (id, name, query).
package com.brife.category.dto;

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

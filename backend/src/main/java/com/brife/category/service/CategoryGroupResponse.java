package com.brife.category.service;

import com.brife.category.domain.CategoryGroup;
import lombok.Getter;

@Getter
public class CategoryGroupResponse {

    private final Long id;
    private final String groupName;

    public CategoryGroupResponse(CategoryGroup categoryGroup) {
        this.id = categoryGroup.getId();
        this.groupName = categoryGroup.getName();
    }
}

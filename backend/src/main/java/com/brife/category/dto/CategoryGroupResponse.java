// [DTO - 응답] 대분류 카테고리 응답 (id, groupName).
package com.brife.category.dto;

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

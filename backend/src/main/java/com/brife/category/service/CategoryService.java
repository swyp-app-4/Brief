// [카테고리 서비스] 대분류 전체 조회, 선택된 대분류 ID로 소분류 그룹핑 반환.
package com.brife.category.service;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;
import com.brife.category.dto.CategoryGroupDetailResponse;
import com.brife.category.dto.CategoryGroupResponse;
import com.brife.category.repository.CategoryGroupRepository;
import com.brife.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;

    public List<CategoryGroupResponse> getAllGroups() {
        return categoryGroupRepository.findAll().stream()
                .map(CategoryGroupResponse::new)
                .toList();
    }

    public List<CategoryGroupDetailResponse> getCategoriesByGroups(List<Long> groupIds) {
        List<CategoryGroup> groups = categoryGroupRepository.findAllById(groupIds);
        List<Category> categories = categoryRepository.findByCategoryGroupIdIn(groupIds);

        Map<Long, List<Category>> categoriesByGroupId = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getCategoryGroup().getId()));

        return groups.stream()
                .map(group -> new CategoryGroupDetailResponse(
                        group,
                        categoriesByGroupId.getOrDefault(group.getId(), List.of())
                ))
                .toList();
    }
}

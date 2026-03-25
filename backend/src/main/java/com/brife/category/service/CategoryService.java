package com.brife.category.service;

import com.brife.category.domain.CategoryGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<CategoryResponse> getCategoriesByGroup(Long groupId) {
        CategoryGroup group = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("대분류 없음: " + groupId));

        return categoryRepository.findByCategoryGroup(group).stream()
                .map(CategoryResponse::new)
                .toList();
    }
}

package com.brife.news.service;

import com.brife.news.dto.CategoryGroupResponse;
import com.brife.news.dto.CategoryResponse;
import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

    // 온보딩 화면 조회 시 대분류 6개를 반환
    @Transactional(readOnly = true)
    public List<CategoryGroupResponse> getOnBoardingCategoryGroups() {
        return categoryGroupRepository.findAll().stream()
                .map(CategoryGroupResponse::from)
                .toList();
    }

    // 온보딩 화면 조회 시 소분류 반환
    @Transactional(readOnly = true)
    public List<CategoryResponse> getOnBoardingCategory(List<Long> categoryGroupIds) {
        return categoryRepository.findByCategoryGroup_IdIn(categoryGroupIds).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
package com.brife.news.service;

import com.brife.category.domain.Category;
import com.brife.news.dto.CategoryGroupDetailResponse;
import com.brife.news.dto.CategoryGroupResponse;
import com.brife.news.repository.CategoryGroupRepository;
import com.brife.news.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

    // 온보딩 대분류 6개 반환
    @Transactional(readOnly = true)
    public List<CategoryGroupResponse> getCategoryGroups() {
        return categoryGroupRepository.findAll().stream()
                .map(CategoryGroupResponse::from)
                .toList();
    }

    // 선택한 대분류에 속한 소분류를 그룹별로 반환
    @Transactional(readOnly = true)
    public List<CategoryGroupDetailResponse> getCategoryGroupDetails(List<Long> groupIds) {
        List<Category> categories = categoryRepository.findWithGroupByCategoryGroup_IdIn(groupIds);

        Map<Long, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getCategoryGroup().getId()));

        // 요청한 groupId 순서 유지
        return groupIds.stream()
                .filter(grouped::containsKey)
                .map(groupId -> {
                    List<Category> cats = grouped.get(groupId);
                    return CategoryGroupDetailResponse.from(cats.get(0).getCategoryGroup(), cats);
                })
                .toList();
    }
}

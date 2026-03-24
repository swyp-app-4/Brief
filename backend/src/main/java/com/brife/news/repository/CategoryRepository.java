package com.brife.news.repository;

import com.brife.news.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 대분류에 속한 세부 카테고리를 포함한 Category 엔티티 반환
    List<Category> findByCategoryGroup_Id(Long categoryGroupId);
}
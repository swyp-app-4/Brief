package com.brife.news.repository;

import com.brife.category.domain.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 배치용: categoryGroup 조회 불필요
    List<Category> findByCategoryGroup_IdIn(List<Long> categoryGroupIds);

    // 온보딩용: categoryGroup 정보까지 한 번에 조회
    @EntityGraph(attributePaths = {"categoryGroup"})
    List<Category> findWithGroupByCategoryGroup_IdIn(List<Long> groupIds);
}
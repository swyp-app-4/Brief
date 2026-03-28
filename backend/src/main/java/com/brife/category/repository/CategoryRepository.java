// [레포지토리] Category JPA 레포지토리. 대분류 ID 목록으로 소분류 일괄 조회.
package com.brife.category.repository;

import com.brife.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByCategoryGroupIdIn(List<Long> groupIds);
}

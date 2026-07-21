package com.brife.news.repository;

import com.brife.news.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByCategoryGroup_IdIn(List<Long> categoryGroupIds);
}
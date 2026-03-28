package com.brife.news.repository;

import com.brife.news.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {
}
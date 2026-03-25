package com.brife.category.service;

import com.brife.category.domain.Category;
import com.brife.category.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByCategoryGroup(CategoryGroup categoryGroup);
}

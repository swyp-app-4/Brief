// [레포지토리] CategoryGroup JPA 레포지토리.
package com.brife.category.repository;

import com.brife.category.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {
}

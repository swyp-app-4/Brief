package com.brife.news.repository;

import com.brife.news.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // GroupName(대분류 6개) 속성 기준 중복 제거 후 반환
    @Query("SELECT DISTINCT c.groupName FROM Category c")
    List<Category> findDistinctByGroupNames();
}
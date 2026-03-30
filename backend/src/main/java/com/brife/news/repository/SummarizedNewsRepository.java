package com.brife.news.repository;

import com.brife.news.domain.SummarizedNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SummarizedNewsRepository extends JpaRepository<SummarizedNews, Long> {


    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    Optional<SummarizedNews> findWithCategoryById(Long id);

    @Query("SELECT MAX(s.createdAt) FROM SummarizedNews s")
    Optional<LocalDateTime> findMaxCreatedAt();

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop5ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
            List<Long> categoryIds, LocalDateTime since);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop5ByCategoryIdInOrderBySourceCountDesc(List<Long> categoryIds);

    @NativeQuery(value = """
            SELECT * FROM summarized_news
            WHERE title ILIKE CONCAT('%%', :keyword, '%%')
            OR summary ILIKE CONCAT('%%', :keyword, '%%')
            ORDER BY published_date DESC
            """)
    Slice<SummarizedNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Slice<SummarizedNews> findAllBy(Pageable pageable);
}

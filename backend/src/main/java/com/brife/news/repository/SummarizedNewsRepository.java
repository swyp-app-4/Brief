package com.brife.news.repository;

import com.brife.news.domain.SummarizedNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SummarizedNewsRepository extends JpaRepository<SummarizedNews, Long> {


    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    Optional<SummarizedNews> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop20ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
            List<Long> categoryIds, LocalDateTime since);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop20ByCategoryIdInOrderBySourceCountDesc(List<Long> categoryIds);

    @NativeQuery(value = """
            SELECT * FROM summarized_news
            WHERE is_summarized = true
              AND (title ILIKE CONCAT('%%', :keyword, '%%')
              OR summary ILIKE CONCAT('%%', :keyword, '%%'))
            ORDER BY published_date DESC
            """)
    Slice<SummarizedNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Slice<SummarizedNews> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<SummarizedNews> findAllByIdIn(List<Long> ids);
}

package com.brife.news.repository;

import com.brife.news.domain.SummarizedNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @EntityGraph(attributePaths = {"category"})
    List<SummarizedNews> findTop5ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
            List<Long> categoryIds, LocalDateTime since);

    @EntityGraph(attributePaths = {"category"})
    List<SummarizedNews> findTop5ByCategoryIdInOrderBySourceCountDesc(List<Long> categoryIds);


    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT s FROM SummarizedNews s " +
            "WHERE s.title LIKE %:keyword% OR s.summary LIKE %:keyword% " +
            "ORDER BY s.publishedDate DESC")
    Slice<SummarizedNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Slice<SummarizedNews> findAllByOrderByPublishedDateDesc(Pageable pageable);
}

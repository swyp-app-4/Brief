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

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop20ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
            List<Long> categoryIds, LocalDateTime since);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findTop20ByCategoryIdInOrderBySourceCountDesc(List<Long> categoryIds);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    @Query("""
            SELECT n FROM SummarizedNews n
            WHERE n.isSummarized = true
              AND n.category.id IN :categoryIds
              AND n.publishedAt >= :since
            ORDER BY n.sourceCount DESC, n.publishedAt DESC, n.id DESC
            """)
    List<SummarizedNews> findRecommendationCandidates(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    @Query("""
            SELECT n FROM SummarizedNews n
            WHERE n.isSummarized = true
            ORDER BY n.publishedAt DESC NULLS LAST, n.id DESC
            """)
    List<SummarizedNews> findLatestRecommendationCandidates(Pageable pageable);

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
    @Query("""
            SELECT n FROM SummarizedNews n
            WHERE n.isSummarized = true
            ORDER BY n.publishedAt DESC NULLS LAST, n.id DESC
            """)
    Slice<SummarizedNews> findLatest(Pageable pageable);

    @NativeQuery(value = """
            SELECT id,
                   title,
                   summary,
                   similarity(title || ' ' || summary, :title || ' ' || :summary) AS "overallSimilarity",
                   similarity(title, :title) AS "titleSimilarity",
                   similarity(summary, :summary) AS "summarySimilarity"
            FROM summarized_news
            WHERE is_summarized = true
              AND created_at >= :since
              AND (
                    similarity(title || ' ' || summary, :title || ' ' || :summary) >= 0.85
                    OR similarity(title, :title) >= 0.55
                  )
            ORDER BY GREATEST(
                    similarity(title || ' ' || summary, :title || ' ' || :summary),
                    similarity(title, :title)
                  ) DESC
            LIMIT 10
            """)
    List<DuplicateNewsCandidate> findDuplicateCandidates(
            @Param("title") String title,
            @Param("summary") String summary,
            @Param("since") LocalDateTime since);

    @EntityGraph(attributePaths = {"category", "category.categoryGroup"})
    List<SummarizedNews> findAllByIdIn(List<Long> ids);
}

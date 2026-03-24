package com.brife.news.repository;

import com.brife.news.domain.SummarizedNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SummarizedNewsRepository extends JpaRepository<SummarizedNews, Long> {
    // 제목 + 3줄 요약 동시 검색
    @NativeQuery(value = """
        SELECT * FROM summarized_news
        WHERE title ILIKE CONCAT('%%', :keyword, '%%')
        OR summary ILIKE CONCAT('%%', :keyword, '%%')
        ORDER BY published_date DESC
        """)
    Slice<SummarizedNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 전체 최신순 무한 스크롤
    Slice<SummarizedNews> findAllByOrderByPublishedDateDesc(Pageable pageable);

    // 뉴스 기사 상세 보기는 JpaRepository의 기본 메서드인 findById 사용
}
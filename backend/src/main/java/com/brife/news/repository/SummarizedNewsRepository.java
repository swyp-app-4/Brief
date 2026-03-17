package com.brife.news.repository;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.domain.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SummarizedNewsRepository extends JpaRepository<SummarizedNews, Long> {
    // 사용자 질의 -> 임베딩 API를 통해 백터화 -> 코사인 거리 기반 유사도로 pgvectorDB 조회 -> 검색 결과 반환
    @Query(
            value = "SELECT * FROM summarized_news " +
                    "WHERE embedding IS NOT NULL " +
                    "ORDER BY embedding <=> cast(:vector as vector)",
            countQuery = "SELECT COUNT(*) FROM summarized_news " +
                    "WHERE embedding IS NOT NULL",
            nativeQuery = true
    )
    Page<SummarizedNews> searchByEmbedding(@Param("vector") String vector, Pageable pageable);

    // 관심사 기반 Top5 뉴스 조회
    List<SummarizedNews> findTop5ByCategoryIdInOrderByPublishedDateDesc(List<Long> categoryIds);

    // 조회된 뉴스의 토픽을 공유하는 다른 SummarizedNews 반환
    List<SummarizedNews> findByTopicAndIdNot(Topic topic, Long id);

    // 뉴스 기사 상세 보기는 JpaRepository의 기본 메서드인 findById 사용
}
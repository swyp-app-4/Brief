package com.brife.news.repository;

import com.brife.news.domain.RawNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RawNewsRepository extends JpaRepository<RawNews, Long> {
    // 애플리케이션 레벨에서 URL 중복 검사
    boolean existsByNaverUrl(String naverUrl);

    @Query("SELECT r.sourceUrl FROM RawNews r WHERE r.summarizedNews.id = :summarizedNewsId")
    List<String> findSourceUrlsBySummarizedNewsId(@Param("summarizedNewsId") Long summarizedNewsId);
}
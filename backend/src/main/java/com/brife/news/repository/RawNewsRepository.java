package com.brife.news.repository;

import com.brife.news.domain.RawNews;
import com.brife.news.dto.NewsSourceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RawNewsRepository extends JpaRepository<RawNews, Long> {
    // 애플리케이션 레벨에서 URL 중복 검사
    boolean existsByNaverUrl(String naverUrl);

    @Query("SELECT new com.brife.news.dto.NewsSourceDto(r.title, r.sourceUrl) FROM RawNews r WHERE r.summarizedNews.id = :id")
    List<NewsSourceDto> findSourcesBySummarizedNewsId(@Param("id") Long id);
}
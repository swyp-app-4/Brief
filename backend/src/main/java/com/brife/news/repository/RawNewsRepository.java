package com.brife.news.repository;

import com.brife.news.domain.RawNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawNewsRepository extends JpaRepository<RawNews, Long> {
    // 애플리케이션 레벨에서 URL 중복 검사
    boolean existsByNaverUrl(String naverUrl);
}
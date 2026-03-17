package com.brife.news.repository;

import com.brife.news.domain.RawNews;
import com.brife.news.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawNewsRepository extends JpaRepository<RawNews, Long> {
    // 애플리케이션 레벨에서 URL 중복 검사
    boolean existsByNaverUrl(String naverUrl);

    // 특정 토픽에 연결된 원본 뉴스 개수
    // SELECT COUNT(r) FROM RawNews r WHERE r.topic = :topic
    int countByTopic(Topic topic);
}
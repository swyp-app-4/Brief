package com.brife.news.repository;

import com.brife.news.domain.Category;
import com.brife.news.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    // 카테고리 + 키워드 + 날짜 조합이 이미 존재하는지 여부 확인
    boolean existsByCategoryAndKeywordAndCreatedDate(Category category, String keyword, LocalDate createdDate);
}
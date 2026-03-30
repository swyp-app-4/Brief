package com.brife.news.dto;

import com.brife.news.domain.SummarizedNews;

import java.time.LocalDate;

public record NewsSearchResponse(Long id, String categoryName, String title, LocalDate publishedDate) {
    public static NewsSearchResponse from(SummarizedNews entity) {
        return new NewsSearchResponse(entity.getId(), entity.getCategory().getName(), entity.getTitle(), entity.getPublishedDate());
    }
}
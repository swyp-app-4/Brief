package com.brife.news.dto;

import com.brife.news.domain.SummarizedNews;

import java.time.LocalDate;

public record SimilarNewsResponse(
        Long id,
        String groupName,
        String categoryName,
        String title,
        LocalDate publishedDate
) {
    public static SimilarNewsResponse from(SummarizedNews entity) {
        return new SimilarNewsResponse(
                entity.getId(),
                entity.getCategory().getCategoryGroup().getName(),
                entity.getCategory().getName(),
                entity.getTitle(),
                entity.getPublishedDate()
        );
    }
}

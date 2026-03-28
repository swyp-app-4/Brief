package com.brife.news.dto;

import com.brife.news.domain.SummarizedNews;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class WidgetCardResponse {

    private final Long id;
    private final String title;
    private final String summary;       // 3줄 요약
    private final String thumbnailUrl;
    private final String categoryName;  // 카테고리명
    private final LocalDate publishedDate;

    public WidgetCardResponse(SummarizedNews news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.summary = news.getSummary();
        this.thumbnailUrl = news.getThumbnailUrl();
        this.categoryName = news.getCategory().getName();
        this.publishedDate = news.getPublishedDate();
    }
}
package com.brife.news.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RawArticleDto {
    private String title;
    private String description;
    private String sourceUrl;
    private String naverUrl;
    private LocalDateTime pubDate;
    private String pressName;
}

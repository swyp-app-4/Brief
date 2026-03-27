package com.brife.news.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class KeywordGroupDto {
    private Long categoryId;
    private String categoryName;
    private String keyword;
    private List<RawArticleDto> articles;
}

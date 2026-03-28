package com.brife.news.dto;

import com.brife.news.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProcessedNewsDto {
    private Category category;
    private List<RawArticleDto> newArticles;
    private SynthesisResult synthesisResult;
    private String sectionsJson;
    private int totalArticleCount;
    private LocalDate publishedDate;
}

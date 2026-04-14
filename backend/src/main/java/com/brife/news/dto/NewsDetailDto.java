package com.brife.news.dto;

import com.brife.news.domain.SummarizedNews;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


public record NewsDetailDto(
        Long id,
        String groupName,
        String categoryName,
        String title,
        List<String> summaryList,
        List<SectionResponseDto> sections,
        int sourceCount,
        LocalDate publishedDate
) {
    public static NewsDetailDto from(SummarizedNews news, List<SectionResponseDto> sections) {
        List<String> summaryList = List.of();
        if (news.getSummary() != null && !news.getSummary().isBlank()) {
            summaryList = Arrays.stream(news.getSummary().split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        return new NewsDetailDto(
                news.getId(),
                news.getCategory().getCategoryGroup().getName(),
                news.getCategory().getName(),
                news.getTitle(),
                summaryList,
                sections,
                news.getSourceCount(),
                news.getPublishedDate()
        );
    }
}

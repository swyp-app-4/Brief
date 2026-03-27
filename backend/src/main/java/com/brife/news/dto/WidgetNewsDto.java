package com.brife.news.dto;

import com.brife.news.domain.SummarizedNews;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class WidgetNewsDto {

    private Long id;
    private String categoryName;
    private String title;
    private List<String> summaryList;
    private String bodyPreview;
    private int sourceCount;
    private LocalDate publishedDate;

    public static WidgetNewsDto from(SummarizedNews news, String bodyPreview) {
        List<String> summaryList = List.of();
        if (news.getSummary() != null && !news.getSummary().isBlank()) {
            summaryList = Arrays.stream(news.getSummary().split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        return WidgetNewsDto.builder()
                .id(news.getId())
                .categoryName(news.getCategory().getName())
                .title(news.getTitle())
                .summaryList(summaryList)
                .bodyPreview(bodyPreview)
                .sourceCount(news.getSourceCount())
                .publishedDate(news.getPublishedDate())
                .build();
    }
}

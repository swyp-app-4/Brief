package com.brife.news.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.brife.news.domain.SummarizedNews;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Data
@Builder
public class NewsResponseDto {
    private Long id;
    private String categoryName;
    private String title;
    private String summary;              // 3줄 요약
    private List<SectionDto> sections;   // 소제목별 본문 섹션 (2~3개)
    private String thumbnailUrl;
    private int sourceCount;
    private int viewCount;
    private int saveCount;
    private boolean isSummarized;
    private LocalDate publishedDate;
    private LocalDateTime createdAt;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static NewsResponseDto from(SummarizedNews news) {
        List<SectionDto> sections = Collections.emptyList();
        if (news.getBody() != null && !news.getBody().isBlank()) {
            try {
                sections = MAPPER.readValue(news.getBody(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("[NewsResponseDto] sections 파싱 실패 - id={}", news.getId());
            }
        }

        return NewsResponseDto.builder()
                .id(news.getId())
                .categoryName(news.getCategory().getName())
                .title(news.getTitle())
                .summary(news.getSummary())
                .sections(sections)
                .thumbnailUrl(news.getThumbnailUrl())
                .sourceCount(news.getSourceCount())
                .viewCount(news.getViewCount())
                .saveCount(news.getSaveCount())
                .isSummarized(news.isSummarized())
                .publishedDate(news.getPublishedDate())
                .createdAt(news.getCreatedAt())
                .build();
    }
}

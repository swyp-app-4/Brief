package com.brife.news.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SectionDto {
    private String heading;
    private String content;
    private List<Integer> supportingArticleIndexes;

    public SectionDto(String heading, String content) {
        this.heading = heading;
        this.content = content;
    }
}

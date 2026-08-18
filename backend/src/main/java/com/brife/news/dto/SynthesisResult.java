package com.brife.news.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SynthesisResult {
    private boolean categoryRelevant;
    private String categoryReason;
    private String title;
    private String summary;
    private List<SectionDto> sections;
}

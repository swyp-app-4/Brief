package com.brife.news.dto;

import java.util.Arrays;
import java.util.List;

public record SectionResponseDto(
        String heading,
        List<String> contentList
) {
    public static SectionResponseDto from(SectionDto section) {
        if (section.getContent() == null || section.getContent().isBlank()) {
            return new SectionResponseDto(section.getHeading(), List.of());
        }
        List<String> lines = Arrays.stream(section.getContent().split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        return new SectionResponseDto(section.getHeading(), lines);
    }
}

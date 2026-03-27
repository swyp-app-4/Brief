package com.brife.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NaverNewsResponse {

    private int total;
    private int start;
    private int display;
    private List<NaverNewsItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class NaverNewsItem {

        private String title;

        @JsonProperty("originallink")
        private String originalLink;

        private String link;
        private String description;
        private String pubDate;
    }
}

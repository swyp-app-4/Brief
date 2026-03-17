package com.brife.news.domain;

public enum CategoryGroup {
    POLITICS("시사 정치"),
    ECONOMY("경제 제테크"),
    IT("IT 테크"),
    CULTURE("문화 예술"),
    ENTERTAINMENT("엔터 스포츠"),
    LIFESTYLE("라이프 스타일");

    private final String displayName;

    CategoryGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

package com.brife.news.dto;

import java.time.LocalDateTime;

public record NewsSourceDto(String title, String sourceUrl, String pressName, LocalDateTime publishedDate) {}

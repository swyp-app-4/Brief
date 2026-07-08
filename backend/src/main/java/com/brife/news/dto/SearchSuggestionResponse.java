package com.brife.news.dto;

import java.util.List;

public record SearchSuggestionResponse(String keyword, List<String> suggestions) {}

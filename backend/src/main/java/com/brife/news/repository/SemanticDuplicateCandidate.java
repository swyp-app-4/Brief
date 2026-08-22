package com.brife.news.repository;

public record SemanticDuplicateCandidate(
        Long id,
        String title,
        String summary,
        double similarity
) {
}

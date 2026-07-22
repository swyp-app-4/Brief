package com.brife.news.repository;

public interface DuplicateNewsCandidate {
    Long getId();
    String getTitle();
    String getSummary();
    double getOverallSimilarity();
    double getTitleSimilarity();
    double getSummarySimilarity();
}

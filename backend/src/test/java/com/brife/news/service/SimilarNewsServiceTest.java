package com.brife.news.service;

import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarNewsServiceTest {

    @Mock
    private NewsEmbeddingRepository newsEmbeddingRepository;

    @Mock
    private SummarizedNewsRepository summarizedNewsRepository;

    @InjectMocks
    private SimilarNewsService similarNewsService;

    @Test
    void returnsEmptyWhenTargetEmbeddingDoesNotExist() {
        when(newsEmbeddingRepository.existsByNewsId(100L)).thenReturn(false);

        assertThat(similarNewsService.getSimilarNews(100L)).isEmpty();

        verifyNoInteractions(summarizedNewsRepository);
    }

    @Test
    void requestsOnlyQualifiedRecentCandidates() {
        when(newsEmbeddingRepository.existsByNewsId(100L)).thenReturn(true);
        when(newsEmbeddingRepository.findSimilarNewsIds(100L, 0.72, 5)).thenReturn(List.of());

        assertThat(similarNewsService.getSimilarNews(100L)).isEmpty();

        verify(newsEmbeddingRepository).findSimilarNewsIds(100L, 0.72, 5);
        verifyNoInteractions(summarizedNewsRepository);
    }
}

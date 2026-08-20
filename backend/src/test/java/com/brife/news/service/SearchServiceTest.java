package com.brife.news.service;

import com.brife.news.repository.HybridNewsSearchRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SummarizedNewsRepository summarizedNewsRepository;
    @Mock
    private HybridNewsSearchRepository hybridSearchRepository;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private SearchMetrics searchMetrics;
    @InjectMocks
    private SearchService searchService;

    @Test
    void latestNewsUsesStablePublishedAtAndIdSort() {
        when(summarizedNewsRepository.findLatest(any(Pageable.class)))
                .thenReturn(new SliceImpl<>(List.of()));

        searchService.getAllSummarizedNewsByPublishedDesc(PageRequest.of(2, 15));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(summarizedNewsRepository).findLatest(captor.capture());
        Pageable actual = captor.getValue();

        assertThat(actual.getPageNumber()).isEqualTo(2);
        assertThat(actual.getPageSize()).isEqualTo(15);
        assertThat(actual.getSort().isUnsorted()).isTrue();
    }

    @Test
    void normalizesKeywordBeforeEmbeddingAndHybridSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        float[] vector = new float[768];
        when(embeddingService.embedQueryCached("삼성 전자")).thenReturn(vector);
        when(hybridSearchRepository.search("삼성 전자", vector, pageable))
                .thenReturn(new SliceImpl<>(List.of(), pageable, false));

        searchService.getSummarizedNewsByKeyword("  삼성   전자  ", pageable);

        verify(embeddingService).embedQueryCached("삼성 전자");
        verify(hybridSearchRepository).search("삼성 전자", vector, pageable);
    }

    @Test
    void fallsBackToKeywordSearchWhenEmbeddingFails() {
        Pageable pageable = PageRequest.of(0, 10);
        when(embeddingService.embedQueryCached("반도체"))
                .thenThrow(new RuntimeException("embedding unavailable"));
        when(summarizedNewsRepository.searchByKeyword(eq("반도체"), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(List.of(), pageable, false));

        searchService.getSummarizedNewsByKeyword("반도체", pageable);

        verify(searchMetrics).incrementFallback();
        verify(summarizedNewsRepository).searchByKeyword(eq("반도체"), any(Pageable.class));
        verify(hybridSearchRepository, never()).search(any(), any(), any());
    }

    @Test
    void rejectsKeywordLongerThanOneHundredCharacters() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> searchService.getSummarizedNewsByKeyword("가".repeat(101), pageable))
                .isInstanceOf(com.brife.news.exception.InvalidSearchKeywordException.class)
                .hasMessage("검색어는 100자 이하로 입력해주세요.");

        verify(embeddingService, never()).embedQueryCached(any());
    }
}

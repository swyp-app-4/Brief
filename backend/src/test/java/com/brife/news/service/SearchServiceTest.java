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
import static org.mockito.ArgumentMatchers.any;
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
}

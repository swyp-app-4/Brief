package com.brife.news.batch;

import com.brife.news.domain.Category;
import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.ProcessedNewsDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.news.service.EmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsCrawlingWriterTest {

    @Mock private RawNewsRepository rawNewsRepository;
    @Mock private SummarizedNewsRepository summarizedNewsRepository;
    @Mock private EmbeddingService embeddingService;
    @Mock private NewsEmbeddingRepository newsEmbeddingRepository;
    @Mock private NewsBatchMetrics batchMetrics;

    private NewsCrawlingWriter writer;

    @BeforeEach
    void setUp() {
        writer = new NewsCrawlingWriter(
                rawNewsRepository,
                summarizedNewsRepository,
                embeddingService,
                newsEmbeddingRepository,
                batchMetrics);
    }

    @Test
    void reusesDocumentEmbeddingCreatedByProcessor() throws Exception {
        float[] embedding = new float[] {1.0f, 0.5f};
        SummarizedNews saved = org.mockito.Mockito.mock(SummarizedNews.class);
        when(saved.getId()).thenReturn(10L);
        when(summarizedNewsRepository.save(any(SummarizedNews.class))).thenReturn(saved);

        writer.write(new Chunk<>(List.of(processedNews(embedding))));

        verify(newsEmbeddingRepository).save(10L, embedding);
        verify(embeddingService, never()).embedForDocument(any());
        verify(batchMetrics).incrementEmbeddingSuccessCount();
    }

    @Test
    void retriesEmbeddingInWriterWhenProcessorEmbeddingWasUnavailable() throws Exception {
        float[] embedding = new float[] {0.5f, 1.0f};
        SummarizedNews saved = org.mockito.Mockito.mock(SummarizedNews.class);
        when(saved.getId()).thenReturn(11L);
        when(saved.getTitle()).thenReturn("제목");
        when(saved.getSummary()).thenReturn("요약");
        when(summarizedNewsRepository.save(any(SummarizedNews.class))).thenReturn(saved);
        when(embeddingService.embedForDocument("제목 요약")).thenReturn(embedding);

        writer.write(new Chunk<>(List.of(processedNews(null))));

        verify(embeddingService).embedForDocument("제목 요약");
        verify(newsEmbeddingRepository).save(11L, embedding);
        verify(batchMetrics).incrementEmbeddingSuccessCount();
    }

    private ProcessedNewsDto processedNews(float[] embedding) {
        SynthesisResult synthesis = new SynthesisResult();
        synthesis.setTitle("제목");
        synthesis.setSummary("요약");
        return ProcessedNewsDto.builder()
                .category(Category.builder().name("경제").query("경제").build())
                .newArticles(List.of())
                .synthesisResult(synthesis)
                .sectionsJson("[]")
                .totalArticleCount(0)
                .publishedDate(LocalDate.now())
                .publishedAt(LocalDateTime.now())
                .documentEmbedding(embedding)
                .build();
    }
}

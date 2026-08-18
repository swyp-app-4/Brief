package com.brife.news.service;

import com.brife.news.domain.Category;
import com.brife.news.domain.CategoryGroup;
import com.brife.news.domain.SummarizedNews;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Test
    void returnsGroupAndCategoryNamesInSimilarityOrder() {
        SummarizedNews firstNews = mockNews(2L, "경제", "증권", "첫 번째 기사");
        SummarizedNews secondNews = mockNews(1L, "사회", "사건사고", "두 번째 기사");

        when(newsEmbeddingRepository.existsByNewsId(100L)).thenReturn(true);
        when(newsEmbeddingRepository.findSimilarNewsIds(100L, 0.72, 5)).thenReturn(List.of(2L, 1L));
        when(summarizedNewsRepository.findAllByIdIn(List.of(2L, 1L)))
                .thenReturn(List.of(secondNews, firstNews));

        var responses = similarNewsService.getSimilarNews(100L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(2L);
        assertThat(responses.get(0).groupName()).isEqualTo("경제");
        assertThat(responses.get(0).categoryName()).isEqualTo("증권");
        assertThat(responses.get(0).title()).isEqualTo("첫 번째 기사");
        assertThat(responses.get(0).publishedDate()).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(responses.get(1).id()).isEqualTo(1L);
    }

    private SummarizedNews mockNews(Long id, String groupName, String categoryName, String title) {
        CategoryGroup categoryGroup = org.mockito.Mockito.mock(CategoryGroup.class);
        Category category = org.mockito.Mockito.mock(Category.class);
        SummarizedNews news = org.mockito.Mockito.mock(SummarizedNews.class);

        when(categoryGroup.getName()).thenReturn(groupName);
        when(category.getCategoryGroup()).thenReturn(categoryGroup);
        when(category.getName()).thenReturn(categoryName);
        when(news.getId()).thenReturn(id);
        when(news.getCategory()).thenReturn(category);
        when(news.getTitle()).thenReturn(title);
        when(news.getPublishedDate()).thenReturn(LocalDate.of(2026, 8, 17));
        return news;
    }
}

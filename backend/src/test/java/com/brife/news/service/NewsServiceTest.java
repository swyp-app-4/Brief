package com.brife.news.service;

import com.brife.news.domain.Category;
import com.brife.news.domain.CategoryGroup;
import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.user.repository.UserInterestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock private SummarizedNewsRepository summarizedNewsRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserInterestRepository userInterestRepository;
    @Mock private RawNewsRepository rawNewsRepository;
    @Mock private NewsEmbeddingRepository newsEmbeddingRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private DuplicateNewsDetectionService duplicateNewsDetectionService;
    @InjectMocks private NewsService newsService;

    @Test
    void fillsFromSubcategoryThenParentThenLatestWithCategoryCap() {
        CategoryGroup group = group(1L, "Economy");
        Category selectedCategory = category(11L, "Finance", group);
        Category siblingCategory = category(12L, "Industry", group);
        Category otherCategory = category(21L, "Society", group(2L, "Society"));
        Category finalCategory = category(31L, "Science", group(3L, "Science"));

        when(categoryRepository.findAllById(List.of(11L))).thenReturn(List.of(selectedCategory));
        when(categoryRepository.findByCategoryGroup_IdIn(List.of(1L)))
                .thenReturn(List.of(selectedCategory, siblingCategory));

        SummarizedNews n1 = news(1L, selectedCategory);
        SummarizedNews n2 = news(2L, selectedCategory);
        SummarizedNews capped = news(3L, selectedCategory);
        SummarizedNews parent = news(4L, siblingCategory);
        SummarizedNews global1 = news(5L, otherCategory);
        SummarizedNews global2 = news(6L, finalCategory);

        when(summarizedNewsRepository.findRecommendationCandidates(
                eq(List.of(11L)), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(n1, n2, capped));
        when(summarizedNewsRepository.findRecommendationCandidates(
                eq(List.of(11L, 12L)), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(n1, n2, capped, parent));
        when(summarizedNewsRepository.findLatestRecommendationCandidates(
                any(Pageable.class)))
                .thenReturn(List.of(n1, n2, parent, global1, global2));

        List<WidgetNewsDto> result = newsService.getTop5News(List.of(11L), List.of());

        assertThat(result).extracting(WidgetNewsDto::getId)
                .containsExactly(1L, 2L, 4L, 5L, 6L);
    }

    @Test
    void skipsSameEventAndRefillsRecommendationToFiveNews() {
        CategoryGroup group = group(1L, "News");
        SummarizedNews first = news(1L, category(11L, "Culture", group),
                "오디세이 개봉 13일 만에 500만 돌파", "오디세이가 관객 500만 명을 돌파했습니다.");
        SummarizedNews duplicate = news(2L, category(12L, "Movie", group),
                "오디세이 500만 관객 돌파 흥행 질주", "오디세이가 개봉 13일 만에 500만 관객을 기록했습니다.");
        SummarizedNews third = news(3L, category(13L, "Economy", group));
        SummarizedNews fourth = news(4L, category(14L, "Society", group));
        SummarizedNews fifth = news(5L, category(15L, "Technology", group));
        SummarizedNews refill = news(6L, category(16L, "Sports", group));

        when(summarizedNewsRepository.findLatestRecommendationCandidates(any(Pageable.class)))
                .thenReturn(List.of(first, duplicate, third, fourth, fifth, refill));
        Long duplicateId = duplicate.getId();
        String duplicateTitle = duplicate.getTitle();
        String duplicateSummary = duplicate.getSummary();
        Long firstId = first.getId();
        String firstTitle = first.getTitle();
        String firstSummary = first.getSummary();
        when(duplicateNewsDetectionService.representsSameEvent(
                eq(duplicateId), eq(duplicateTitle), eq(duplicateSummary),
                eq(firstId), eq(firstTitle), eq(firstSummary), anyMap()))
                .thenReturn(true);

        List<WidgetNewsDto> result = newsService.getTop5News(List.of(), List.of());

        assertThat(result).extracting(WidgetNewsDto::getId)
                .containsExactly(1L, 3L, 4L, 5L, 6L);
    }

    @Test
    void movesAnchoredRecommendationToFirstWithoutDuplicatingIt() {
        List<WidgetNewsDto> recommendations = List.of(
                WidgetNewsDto.builder().id(1L).build(),
                WidgetNewsDto.builder().id(2L).build(),
                WidgetNewsDto.builder().id(3L).build()
        );

        List<WidgetNewsDto> result = newsService.prioritizeRecommendation(recommendations, 2L);

        assertThat(result).extracting(WidgetNewsDto::getId)
                .containsExactly(2L, 1L, 3L);
    }

    @Test
    void loadsMissingAnchorAndKeepsRecommendationSizeAtFive() {
        Category category = category(11L, "Culture", group(1L, "News"));
        SummarizedNews anchor = news(9L, category);
        when(anchor.isSummarized()).thenReturn(true);
        when(summarizedNewsRepository.findWithCategoryById(9L)).thenReturn(java.util.Optional.of(anchor));
        List<WidgetNewsDto> recommendations = List.of(
                WidgetNewsDto.builder().id(1L).build(),
                WidgetNewsDto.builder().id(2L).build(),
                WidgetNewsDto.builder().id(3L).build(),
                WidgetNewsDto.builder().id(4L).build(),
                WidgetNewsDto.builder().id(5L).build()
        );

        List<WidgetNewsDto> result = newsService.prioritizeRecommendation(recommendations, 9L);

        assertThat(result).extracting(WidgetNewsDto::getId)
                .containsExactly(9L, 1L, 2L, 3L, 4L);
    }

    private CategoryGroup group(Long id, String name) {
        CategoryGroup group = mock(CategoryGroup.class);
        lenient().when(group.getId()).thenReturn(id);
        lenient().when(group.getName()).thenReturn(name);
        return group;
    }

    private Category category(Long id, String name, CategoryGroup group) {
        Category category = mock(Category.class);
        lenient().when(category.getId()).thenReturn(id);
        lenient().when(category.getName()).thenReturn(name);
        lenient().when(category.getCategoryGroup()).thenReturn(group);
        return category;
    }

    private SummarizedNews news(Long id, Category category) {
        return news(id, category, "Title " + id, "Summary " + id);
    }

    private SummarizedNews news(Long id, Category category, String title, String summary) {
        SummarizedNews news = mock(SummarizedNews.class);
        lenient().when(news.getId()).thenReturn(id);
        lenient().when(news.getCategory()).thenReturn(category);
        lenient().when(news.getTitle()).thenReturn(title);
        lenient().when(news.getSummary()).thenReturn(summary);
        lenient().when(news.getPublishedDate()).thenReturn(LocalDate.now());
        return news;
    }
}

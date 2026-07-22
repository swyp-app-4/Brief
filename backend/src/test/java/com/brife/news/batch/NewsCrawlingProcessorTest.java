package com.brife.news.batch;

import com.brife.news.domain.Category;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.ProcessedNewsDto;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.exception.InvalidSynthesisResultException;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.service.DuplicateNewsDetectionService;
import com.brife.news.service.SummarizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsCrawlingProcessorTest {

    @Mock
    private SummarizationService summarizationService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RawNewsRepository rawNewsRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DuplicateNewsDetectionService duplicateNewsDetectionService;
    @Mock
    private NewsBatchMetrics batchMetrics;
    @InjectMocks
    private NewsCrawlingProcessor processor;

    @Test
    void skipsClusterWhenNewArticlesFallBelowMinimum() throws Exception {
        Category category = Category.builder().name("경제").query("경제").build();
        KeywordGroupDto group = group(List.of(article("1"), article("2"), article("3")), 3);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(rawNewsRepository.existsByNaverUrl("1")).thenReturn(true);

        ProcessedNewsDto result = processor.process(group);

        assertThat(result).isNull();
        verify(summarizationService, never()).synthesize(any(), anyList());
    }

    @Test
    void usesActuallyStoredArticleCountAsSourceCount() throws Exception {
        Category category = Category.builder().name("경제").query("경제").build();
        KeywordGroupDto group = group(
                List.of(article("old"), article("new-1"), article("new-2"), article("new-3")), 3);
        SynthesisResult synthesis = new SynthesisResult();
        synthesis.setCategoryRelevant(true);
        synthesis.setTitle("제목");
        synthesis.setSummary("요약");
        synthesis.setSections(List.of(new SectionDto("소제목", "본문")));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(rawNewsRepository.existsByNaverUrl("old")).thenReturn(true);
        when(summarizationService.synthesize(eq("경제"), eq("경제"), anyList())).thenReturn(synthesis);
        when(objectMapper.writeValueAsString(synthesis.getSections())).thenReturn("[]");

        ProcessedNewsDto result = processor.process(group);

        assertThat(result).isNotNull();
        assertThat(result.getNewArticles()).hasSize(3);
        assertThat(result.getTotalArticleCount()).isEqualTo(3);
        verify(summarizationService).synthesize(eq("경제"), eq("경제"), anyList());
    }

    @Test
    void skipsResultWhenCategoryIsNotRelevant() throws Exception {
        Category category = Category.builder().name("여행/레저").query("항공권").build();
        KeywordGroupDto group = KeywordGroupDto.builder()
                .categoryId(1L)
                .categoryName("여행/레저")
                .keyword("항공권")
                .minClusterSize(3)
                .articles(List.of(article("1"), article("2"), article("3")))
                .build();
        SynthesisResult synthesis = new SynthesisResult();
        synthesis.setCategoryRelevant(false);
        synthesis.setCategoryReason("게임 대회가 중심이고 항공권은 경품으로만 언급됩니다.");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(summarizationService.synthesize(eq("여행/레저"), eq("항공권"), anyList()))
                .thenReturn(synthesis);

        ProcessedNewsDto result = processor.process(group);

        assertThat(result).isNull();
        verifyNoInteractions(duplicateNewsDetectionService);
    }

    @Test
    void skipsOnlyCurrentTopicWhenSummaryRetryStillFails() throws Exception {
        Category category = Category.builder().name("경제").query("경제").build();
        KeywordGroupDto group = group(List.of(article("1"), article("2"), article("3")), 3);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(summarizationService.synthesize(eq("경제"), eq("경제"), anyList()))
                .thenThrow(new InvalidSynthesisResultException("4줄 요약 실패"));

        ProcessedNewsDto result = processor.process(group);

        assertThat(result).isNull();
        verifyNoInteractions(duplicateNewsDetectionService);
    }

    private KeywordGroupDto group(List<RawArticleDto> articles, int minClusterSize) {
        return KeywordGroupDto.builder()
                .categoryId(1L)
                .categoryName("경제")
                .keyword("경제")
                .minClusterSize(minClusterSize)
                .articles(articles)
                .build();
    }

    private RawArticleDto article(String naverUrl) {
        return RawArticleDto.builder()
                .title("기사 " + naverUrl)
                .description("본문")
                .sourceUrl("https://example.com/" + naverUrl)
                .naverUrl(naverUrl)
                .pubDate(LocalDateTime.now())
                .build();
    }
}

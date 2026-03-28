package com.brife.news.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.brife.news.domain.Category;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.ProcessedNewsDto;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.service.SummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCrawlingProcessor implements ItemProcessor<KeywordGroupDto, ProcessedNewsDto> {

    private final SummarizationService summarizationService;
    private final CategoryRepository categoryRepository;
    private final RawNewsRepository rawNewsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProcessedNewsDto process(KeywordGroupDto group) throws Exception {
        Category category = categoryRepository.findById(group.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("category 없음 id=" + group.getCategoryId()));

        List<RawArticleDto> newArticles = group.getArticles().stream()
                .filter(a -> a.getNaverUrl() == null || !rawNewsRepository.existsByNaverUrl(a.getNaverUrl()))
                .toList();

        if (newArticles.isEmpty()) {
            log.info("[Processor] 스킵 - 새로운 기사 없음. keyword={}", group.getKeyword());
            return null;
        }

        SynthesisResult result = summarizationService.synthesize(group.getKeyword(), newArticles);
        String sectionsJson;
        try {
            sectionsJson = objectMapper.writeValueAsString(result.getSections());
        } catch (Exception e) {
            throw new RuntimeException("sections JSON 직렬화 실패 - keyword=" + group.getKeyword(), e);
        }

        LocalDateTime publishedAt = newArticles.get(0).getPubDate() != null
                ? newArticles.get(0).getPubDate()
                : LocalDateTime.now();

        return ProcessedNewsDto.builder()
                .category(category)
                .newArticles(newArticles)
                .synthesisResult(result)
                .sectionsJson(sectionsJson)
                .totalArticleCount(group.getArticles().size())
                .publishedDate(publishedAt.toLocalDate())
                .publishedAt(publishedAt)
                .build();
    }
}

package com.brife.news.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.brife.news.domain.Category;
import com.brife.news.dto.KeywordGroupDto;
import com.brife.news.dto.ProcessedNewsDto;
import com.brife.news.dto.RawArticleDto;
import com.brife.news.dto.SynthesisResult;
import com.brife.news.exception.InvalidSynthesisResultException;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.service.DuplicateNewsDetectionService;
import com.brife.news.service.SummarizationService;
import com.brife.news.service.SynthesisGroundingValidator;
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
    private final DuplicateNewsDetectionService duplicateNewsDetectionService;
    private final SynthesisGroundingValidator groundingValidator;
    private final NewsBatchMetrics batchMetrics;

    @Override
    public ProcessedNewsDto process(KeywordGroupDto group) throws Exception {
        Category category = categoryRepository.findById(group.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("category 없음 id=" + group.getCategoryId()));

        List<RawArticleDto> newArticles = group.getArticles().stream()
                .filter(a -> a.getNaverUrl() == null || !rawNewsRepository.existsByNaverUrl(a.getNaverUrl()))
                .toList();

        if (newArticles.size() < group.getMinClusterSize()) {
            log.info("[Processor] 스킵 - 신규 기사 수 미달. keyword={}, newArticles={}, minimum={}",
                    group.getKeyword(), newArticles.size(), group.getMinClusterSize());
            batchMetrics.incrementSkippedClusterCount();
            return null;
        }

        SynthesisResult result;
        try {
            result = summarizationService.synthesize(
                    group.getCategoryName(), group.getKeyword(), newArticles);
        } catch (InvalidSynthesisResultException e) {
            log.warn("[Processor] 스킵 - 요약 품질 기준 미달. category={}, reason={}",
                    group.getCategoryName(), e.getMessage());
            batchMetrics.incrementSkippedClusterCount();
            return null;
        }
        if (!result.isCategoryRelevant()) {
            log.info("[Processor] 스킵 - 카테고리 부적합. category={}, reason={}",
                    group.getCategoryName(), result.getCategoryReason());
            batchMetrics.incrementSkippedClusterCount();
            return null;
        }
        if (duplicateNewsDetectionService.isDuplicateWithoutNewInformation(result)) {
            batchMetrics.incrementSkippedClusterCount();
            return null;
        }

        List<RawArticleDto> relevantArticles = groundingValidator.selectRelevantArticles(result, newArticles);

        String sectionsJson;
        try {
            sectionsJson = objectMapper.writeValueAsString(result.getSections());
        } catch (Exception e) {
            throw new RuntimeException("sections JSON 직렬화 실패 - keyword=" + group.getKeyword(), e);
        }

        LocalDateTime publishedAt = relevantArticles.get(0).getPubDate() != null
                ? relevantArticles.get(0).getPubDate()
                : LocalDateTime.now();

        return ProcessedNewsDto.builder()
                .category(category)
                .newArticles(relevantArticles)
                .synthesisResult(result)
                .sectionsJson(sectionsJson)
                .totalArticleCount(relevantArticles.size())
                .publishedDate(publishedAt.toLocalDate())
                .publishedAt(publishedAt)
                .build();
    }
}

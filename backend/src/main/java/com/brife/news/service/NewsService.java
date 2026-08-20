package com.brife.news.service;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.NewsDetailDto;
import com.brife.news.dto.NewsSourceDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SectionResponseDto;
import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.RawNewsRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.user.domain.UserInterest;
import com.brife.user.repository.UserInterestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private static final int BODY_PREVIEW_LENGTH = 150;
    private static final int RECOMMENDATION_SIZE = 5;
    private static final int MAX_PER_CATEGORY = 2;
    private static final int CANDIDATE_LIMIT = 100;
    private static final int RECOMMENDATION_LOOKBACK_HOURS = 48;

    private final SummarizedNewsRepository summarizedNewsRepository;
    private final CategoryRepository categoryRepository;
    private final UserInterestRepository userInterestRepository;
    private final RawNewsRepository rawNewsRepository;
    private final ObjectMapper objectMapper;
    private final DuplicateNewsDetectionService duplicateNewsDetectionService;

    public List<WidgetNewsDto> getTop5News(List<Long> categoryIds, List<Long> groupIds) {
        List<Long> selectedCategoryIds = categoryIds == null
                ? List.of()
                : categoryIds.stream().filter(Objects::nonNull).distinct().toList();
        Set<Long> parentGroupIds = new LinkedHashSet<>();
        if (groupIds != null) {
            groupIds.stream().filter(Objects::nonNull).forEach(parentGroupIds::add);
        }
        categoryRepository.findAllById(selectedCategoryIds).stream()
                .map(category -> category.getCategoryGroup().getId())
                .forEach(parentGroupIds::add);

        List<Long> parentCategoryIds = parentGroupIds.isEmpty()
                ? List.of()
                : categoryRepository.findByCategoryGroup_IdIn(new ArrayList<>(parentGroupIds)).stream()
                        .map(category -> category.getId())
                        .distinct()
                        .toList();

        LocalDateTime since = LocalDateTime.now().minusHours(RECOMMENDATION_LOOKBACK_HOURS);
        PageRequest page = PageRequest.of(0, CANDIDATE_LIMIT);
        List<SummarizedNews> selected = new ArrayList<>();
        Set<Long> usedIds = new LinkedHashSet<>();
        Map<Long, Integer> categoryCounts = new HashMap<>();

        if (!selectedCategoryIds.isEmpty()) {
            appendCandidates(selected, usedIds, categoryCounts,
                    summarizedNewsRepository.findRecommendationCandidates(selectedCategoryIds, since, page));
        }
        if (selected.size() < RECOMMENDATION_SIZE && !parentCategoryIds.isEmpty()) {
            appendCandidates(selected, usedIds, categoryCounts,
                    summarizedNewsRepository.findRecommendationCandidates(parentCategoryIds, since, page));
        }
        if (selected.size() < RECOMMENDATION_SIZE) {
            appendCandidates(selected, usedIds, categoryCounts,
                    summarizedNewsRepository.findLatestRecommendationCandidates(page));
        }

        return selected.stream()
                .map(news -> WidgetNewsDto.from(news, extractBodyPreview(news.getBody())))
                .toList();
    }

    @Cacheable(value = "top5News", key = "#userId", unless = "#result.isEmpty()")
    public List<WidgetNewsDto> getRecommendedNews(Long userId) {
        List<UserInterest> interests = userInterestRepository.findByUserId(userId);

        List<Long> categoryIds = interests.stream()
                .filter(interest -> interest.getCategory() != null)
                .map(interest -> interest.getCategory().getId())
                .toList();
        List<Long> groupIds = interests.stream()
                .filter(interest -> interest.getCategoryGroup() != null)
                .map(interest -> interest.getCategoryGroup().getId())
                .toList();

        return getTop5News(categoryIds, groupIds);
    }

    public List<WidgetNewsDto> prioritizeRecommendation(List<WidgetNewsDto> recommendations, Long anchorNewsId) {
        List<WidgetNewsDto> currentRecommendations = recommendations == null ? List.of() : recommendations;
        if (anchorNewsId == null) {
            return currentRecommendations;
        }

        WidgetNewsDto anchor = currentRecommendations.stream()
                .filter(news -> anchorNewsId.equals(news.getId()))
                .findFirst()
                .orElseGet(() -> summarizedNewsRepository.findWithCategoryById(anchorNewsId)
                        .filter(SummarizedNews::isSummarized)
                        .map(news -> WidgetNewsDto.from(news, extractBodyPreview(news.getBody())))
                        .orElse(null));
        if (anchor == null) {
            return currentRecommendations;
        }

        List<WidgetNewsDto> prioritized = new ArrayList<>(RECOMMENDATION_SIZE);
        prioritized.add(anchor);
        currentRecommendations.stream()
                .filter(news -> !anchorNewsId.equals(news.getId()))
                .limit(RECOMMENDATION_SIZE - 1L)
                .forEach(prioritized::add);
        return List.copyOf(prioritized);
    }

    public NewsDetailDto getNewsDetail(Long id) {
        SummarizedNews news = summarizedNewsRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 뉴스입니다. id=" + id));
        return NewsDetailDto.from(news, parseSections(news.getBody(), id));
    }

    public List<NewsSourceDto> getNewsSources(Long id) {
        if (!summarizedNewsRepository.existsById(id)) {
            throw new NoSuchElementException("존재하지 않는 뉴스입니다. id=" + id);
        }
        return rawNewsRepository.findSourcesBySummarizedNewsId(id);
    }

    private void appendCandidates(List<SummarizedNews> selected,
                                  Set<Long> usedIds,
                                  Map<Long, Integer> categoryCounts,
                                  List<SummarizedNews> candidates) {
        for (SummarizedNews candidate : candidates) {
            if (selected.size() >= RECOMMENDATION_SIZE) return;
            Long categoryId = candidate.getCategory().getId();
            if (usedIds.contains(candidate.getId())) continue;
            if (categoryCounts.getOrDefault(categoryId, 0) >= MAX_PER_CATEGORY) continue;
            boolean sameEvent = selected.stream().anyMatch(existing ->
                    duplicateNewsDetectionService.representsSameEvent(
                            candidate.getId(), candidate.getTitle(), candidate.getSummary(),
                            existing.getId(), existing.getTitle(), existing.getSummary()));
            if (sameEvent) continue;

            selected.add(candidate);
            usedIds.add(candidate.getId());
            categoryCounts.merge(categoryId, 1, Integer::sum);
        }
    }

    private String extractBodyPreview(String body) {
        if (body == null || body.isBlank()) return "";
        try {
            List<SectionDto> sections = objectMapper.readValue(body, new TypeReference<>() {});
            if (sections.isEmpty() || sections.getFirst().getContent() == null) return "";
            String content = sections.getFirst().getContent();
            return content.length() > BODY_PREVIEW_LENGTH
                    ? content.substring(0, BODY_PREVIEW_LENGTH)
                    : content;
        } catch (Exception e) {
            log.warn("[NewsService] bodyPreview 추출 실패");
            return "";
        }
    }

    private List<SectionResponseDto> parseSections(String body, Long id) {
        if (body == null || body.isBlank()) return List.of();
        try {
            List<SectionDto> sectionDtos = objectMapper.readValue(body, new TypeReference<>() {});
            return sectionDtos.stream().map(SectionResponseDto::from).toList();
        } catch (Exception e) {
            log.warn("[NewsService] sections 파싱 실패 - id={}", id);
            return List.of();
        }
    }
}

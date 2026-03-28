package com.brife.news.service;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.NewsDetailDto;
import com.brife.news.dto.SectionDto;
import com.brife.news.dto.SectionResponseDto;
import com.brife.news.dto.WidgetNewsDto;
import com.brife.news.repository.CategoryRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import com.brife.user.domain.UserInterest;
import com.brife.user.profile.UserInterestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private static final int BATCH_WINDOW_HOURS = 3;
    private static final int BODY_PREVIEW_LENGTH = 150;

    private final SummarizedNewsRepository summarizedNewsRepository;
    private final CategoryRepository categoryRepository;
    private final UserInterestRepository userInterestRepository;
    private final ObjectMapper objectMapper;

    // categoryIds, groupIds 혼합 지원 (둘 다 없으면 빈 리스트)
    public List<WidgetNewsDto> getTop5News(List<Long> categoryIds, List<Long> groupIds) {
        List<Long> mergedIds = new ArrayList<>(categoryIds);

        if (!groupIds.isEmpty()) {
            categoryRepository.findByCategoryGroup_IdIn(groupIds)
                    .stream()
                    .map(c -> c.getId())
                    .filter(id -> !mergedIds.contains(id))
                    .forEach(mergedIds::add);
        }

        if (mergedIds.isEmpty()) return List.of();

        List<WidgetNewsDto> result = summarizedNewsRepository.findMaxCreatedAt()
                .map(lastBatch -> summarizedNewsRepository
                        .findTop5ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
                                mergedIds, lastBatch.minusHours(BATCH_WINDOW_HOURS)))
                .orElse(List.of())
                .stream()
                .map(news -> WidgetNewsDto.from(news, extractBodyPreview(news.getBody())))
                .toList();

        if (result.size() < 5) {
            result = summarizedNewsRepository
                    .findTop5ByCategoryIdInOrderBySourceCountDesc(mergedIds)
                    .stream()
                    .map(news -> WidgetNewsDto.from(news, extractBodyPreview(news.getBody())))
                    .toList();
        }

        return result;
    }

    public List<WidgetNewsDto> getRecommendedNews(Long userId) {
        List<UserInterest> interests = userInterestRepository.findByUserId(userId);
        if (interests.isEmpty()) return List.of();

        List<Long> categoryIds = interests.stream()
                .filter(i -> i.getCategory() != null)
                .map(i -> i.getCategory().getId())
                .toList();

        List<Long> groupIds = interests.stream()
                .filter(i -> i.getCategoryGroup() != null)
                .map(i -> i.getCategoryGroup().getId())
                .toList();

        return getTop5News(categoryIds, groupIds);
    }

    public NewsDetailDto getNewsDetail(Long id) {
        SummarizedNews news = summarizedNewsRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 뉴스입니다. id=" + id));

        List<SectionResponseDto> sections = parseSections(news.getBody(), id);
        return NewsDetailDto.from(news, sections);
    }

    // 첫 번째 섹션 content 앞 150자
    private String extractBodyPreview(String body) {
        if (body == null || body.isBlank()) return "";
        try {
            List<SectionDto> sections = objectMapper.readValue(body, new TypeReference<>() {});
            if (sections.isEmpty() || sections.get(0).getContent() == null) return "";
            String content = sections.get(0).getContent();
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

package com.brife.news.service;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.NewsDetailDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final RawNewsRepository rawNewsRepository;
    private final ObjectMapper objectMapper;

    // categoryIds, groupIds 혼합 지원. 대분류별 최소 1개 보장 후 sourceCount 순으로 5개 채움
    public List<WidgetNewsDto> getTop5News(List<Long> categoryIds, List<Long> groupIds) {
        List<Long> mergedIds = new ArrayList<>(categoryIds);

        if (!groupIds.isEmpty()) {
            for (Long groupId : groupIds) {
                List<Long> subIds = categoryRepository.findByCategoryGroup_IdIn(List.of(groupId))
                        .stream().map(c -> c.getId()).toList();

                // 해당 대분류 하위에 선택된 소분류가 있으면 소분류만 적용, 없으면 대분류 전체 추가
                boolean hasSelectedSub = subIds.stream().anyMatch(categoryIds::contains);
                if (!hasSelectedSub) {
                    subIds.stream()
                            .filter(id -> !mergedIds.contains(id))
                            .forEach(mergedIds::add);
                }
            }
        }

        if (mergedIds.isEmpty()) return List.of();

        // 후보 풀 20개 확보
        List<SummarizedNews> candidates = summarizedNewsRepository.findMaxCreatedAt()
                .map(lastBatch -> summarizedNewsRepository
                        .findTop20ByCategoryIdInAndCreatedAtAfterOrderBySourceCountDesc(
                                mergedIds, lastBatch.minusHours(BATCH_WINDOW_HOURS)))
                .orElse(List.of());

        if (candidates.size() < 5) {
            candidates = summarizedNewsRepository.findTop20ByCategoryIdInOrderBySourceCountDesc(mergedIds);
        }

        // 대분류별 1개 보장 (sourceCount 높은 순으로 정렬된 candidates에서 그룹당 첫 번째)
        Map<Long, SummarizedNews> groupPicks = new LinkedHashMap<>();
        for (SummarizedNews news : candidates) {
            Long gId = news.getCategory().getCategoryGroup().getId();
            groupPicks.putIfAbsent(gId, news);
        }

        List<SummarizedNews> result = new ArrayList<>(groupPicks.values());
        Set<Long> usedIds = result.stream()
                .map(SummarizedNews::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 남은 슬롯을 sourceCount 순으로 채움
        candidates.stream()
                .filter(n -> !usedIds.contains(n.getId()))
                .limit(5 - result.size())
                .forEach(result::add);

        // 최대 5개 제한
        if (result.size() > 5) {
            result = result.subList(0, 5);
        }

        return result.stream()
                .map(news -> WidgetNewsDto.from(news, extractBodyPreview(news.getBody())))
                .toList();
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
        List<String> sourceUrls = rawNewsRepository.findSourceUrlsBySummarizedNewsId(id);
        return NewsDetailDto.from(news, sections, sourceUrls);
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

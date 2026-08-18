package com.brife.news.service;

import com.brife.news.domain.SummarizedNews;
import com.brife.news.dto.SimilarNewsResponse;
import com.brife.news.repository.NewsEmbeddingRepository;
import com.brife.news.repository.SummarizedNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarNewsService {

    private static final int SIMILAR_NEWS_LIMIT = 5;
    private static final double MIN_SIMILARITY = 0.72;

    private final NewsEmbeddingRepository newsEmbeddingRepository;
    private final SummarizedNewsRepository summarizedNewsRepository;

    @Transactional(readOnly = true)
    public List<SimilarNewsResponse> getSimilarNews(Long newsId) {
        if (!newsEmbeddingRepository.existsByNewsId(newsId)) {
            log.debug("[SimilarNews] embedding not found - newsId={}", newsId);
            return List.of();
        }

        List<Long> similarIds = newsEmbeddingRepository.findSimilarNewsIds(
                newsId, MIN_SIMILARITY, SIMILAR_NEWS_LIMIT);
        if (similarIds.isEmpty()) {
            return List.of();
        }

        Map<Long, SummarizedNews> newsMap = summarizedNewsRepository.findAllByIdIn(similarIds)
                .stream()
                .collect(Collectors.toMap(SummarizedNews::getId, Function.identity()));

        return similarIds.stream()
                .filter(newsMap::containsKey)
                .map(id -> SimilarNewsResponse.from(newsMap.get(id)))
                .collect(Collectors.toList());
    }
}

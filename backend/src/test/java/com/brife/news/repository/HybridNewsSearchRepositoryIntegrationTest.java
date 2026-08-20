package com.brife.news.repository;

import com.brife.news.dto.NewsSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
class HybridNewsSearchRepositoryIntegrationTest {

    @Autowired private HybridNewsSearchRepository repository;
    @Autowired private NewsEmbeddingRepository embeddingRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void searchesPgvectorAndKeywordCandidatesWithoutPageDuplicates() {
        Long newsId = jdbcTemplate.queryForObject(
                "SELECT news_id FROM news_embedding ORDER BY news_id DESC LIMIT 1", Long.class);
        Map<Long, float[]> embeddings = embeddingRepository.findEmbeddingsByNewsIds(List.of(newsId));
        float[] queryVector = embeddings.get(newsId);

        Slice<NewsSearchResponse> firstPage = repository.search(
                "대통령", queryVector, PageRequest.of(0, 10));
        Slice<NewsSearchResponse> secondPage = repository.search(
                "대통령", queryVector, PageRequest.of(1, 10));

        List<Long> firstIds = firstPage.getContent().stream().map(NewsSearchResponse::id).toList();
        List<Long> secondIds = secondPage.getContent().stream().map(NewsSearchResponse::id).toList();

        assertThat(firstIds).isNotEmpty().doesNotHaveDuplicates();
        assertThat(secondIds).doesNotHaveDuplicates();
        assertThat(new HashSet<>(firstIds)).doesNotContainAnyElementsOf(secondIds);
    }
}

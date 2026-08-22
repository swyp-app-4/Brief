package com.brife.news.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
class NewsEmbeddingRepositoryIntegrationTest {

    @Autowired private NewsEmbeddingRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void loadsMultiplePgvectorEmbeddingsInSingleQuery() {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT news_id FROM news_embedding ORDER BY news_id DESC LIMIT 2", Long.class);
        assertThat(ids).hasSize(2);

        Map<Long, float[]> embeddings = repository.findEmbeddingsByNewsIds(ids);

        assertThat(embeddings).containsOnlyKeys(ids);
        assertThat(embeddings.values()).allSatisfy(vector -> assertThat(vector).hasSize(768));
    }

    @Test
    void findsRecentSemanticDuplicateCandidatesByDocumentVector() {
        Long newsId = jdbcTemplate.queryForObject(
                """
                SELECT e.news_id
                FROM news_embedding e
                JOIN summarized_news n ON n.id = e.news_id
                ORDER BY n.created_at DESC
                LIMIT 1
                """, Long.class);
        LocalDateTime createdAt = jdbcTemplate.queryForObject(
                "SELECT created_at FROM summarized_news WHERE id = ?",
                LocalDateTime.class,
                newsId);
        float[] embedding = repository.findEmbeddingsByNewsIds(List.of(newsId)).get(newsId);

        List<SemanticDuplicateCandidate> candidates = repository.findRecentDuplicateCandidatesByVector(
                embedding, createdAt.minusSeconds(1), 0.99, 5);

        assertThat(candidates).isNotEmpty();
        assertThat(candidates.getFirst().id()).isEqualTo(newsId);
        assertThat(candidates.getFirst().similarity()).isGreaterThanOrEqualTo(0.99);
    }
}

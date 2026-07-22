package com.brife.news.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsEmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(Long newsId, float[] embedding) {
        String sql = """
                INSERT INTO news_embedding (news_id, embedding)
                VALUES (?, ?::vector)
                ON CONFLICT (news_id) DO UPDATE SET embedding = EXCLUDED.embedding
                """;
        jdbcTemplate.update(sql, newsId, toVectorString(embedding));
    }

    public List<Long> findSimilarNewsIds(Long newsId, double minSimilarity, int limit) {
        String sql = """
                SELECT ne.news_id
                FROM news_embedding ne
                JOIN news_embedding target ON target.news_id = ?
                JOIN summarized_news sn ON sn.id = ne.news_id
                WHERE ne.news_id != ?
                  AND sn.is_summarized = true
                  AND COALESCE(sn.published_at, sn.published_date::timestamp)
                      >= NOW() - INTERVAL '90 days'
                  AND 1.0 - (ne.embedding <=> target.embedding) >= ?
                ORDER BY ne.embedding <=> target.embedding
                LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql, Long.class, newsId, newsId, minSimilarity, limit);
    }

    public List<Long> findSimilarNewsIdsByVector(float[] embedding, int limit) {
        String sql = """
                SELECT news_id
                FROM news_embedding
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql, Long.class, toVectorString(embedding), limit);
    }

    public boolean existsByNewsId(Long newsId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM news_embedding WHERE news_id = ?",
                Integer.class, newsId);
        return count != null && count > 0;
    }

    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}

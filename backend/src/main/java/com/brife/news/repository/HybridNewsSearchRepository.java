package com.brife.news.repository;

import com.brife.news.dto.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HybridNewsSearchRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;

    private static final String HYBRID_SQL = """
            WITH semantic_candidates AS (
                SELECT ne.news_id,
                       1.0 - (ne.embedding <=> :queryVector::vector) AS semantic_score
                FROM news_embedding ne
                JOIN summarized_news sn_filter ON sn_filter.id = ne.news_id AND sn_filter.is_summarized = true
                ORDER BY ne.embedding <=> :queryVector::vector
                LIMIT :semanticLimit
            ),
            keyword_candidates AS (
                SELECT sn.id AS news_id,
                       GREATEST(
                           similarity(sn.title, :keyword),
                           CASE WHEN sn.summary ILIKE '%' || :keyword || '%' THEN 0.4 ELSE 0.0 END
                       ) AS keyword_score
                FROM summarized_news sn
                WHERE sn.is_summarized = true
                  AND (sn.title % :keyword
                       OR sn.title ILIKE '%' || :keyword || '%'
                       OR sn.summary ILIKE '%' || :keyword || '%')
                ORDER BY keyword_score DESC, sn.published_date DESC, sn.id DESC
                LIMIT :keywordLimit
            ),
            merged AS (
                SELECT
                    COALESCE(sc.news_id, kc.news_id) AS news_id,
                    COALESCE(sc.semantic_score, 0.0)  AS semantic_score,
                    COALESCE(kc.keyword_score, 0.0)   AS keyword_score
                FROM semantic_candidates sc
                FULL OUTER JOIN keyword_candidates kc ON sc.news_id = kc.news_id
            ),
            ranked AS (
                SELECT
                    sn.id,
                    sn.title,
                    sn.published_date,
                    sn.category_id,
                    m.semantic_score * 0.55 + m.keyword_score * 0.30 +
                    GREATEST(0.0, 1.0 - EXTRACT(EPOCH FROM (NOW() - COALESCE(sn.published_at, sn.published_date::timestamp))) / (30.0 * 86400)) * 0.15
                        AS final_score
                FROM merged m
                JOIN summarized_news sn ON sn.id = m.news_id
                WHERE sn.is_summarized = true
                  AND (m.semantic_score >= 0.62 OR m.keyword_score >= 0.25)
                ORDER BY final_score DESC, sn.published_date DESC, sn.id DESC
                LIMIT :limit OFFSET :offset
            )
            SELECT r.id, r.title, r.published_date, c.name AS category_name
            FROM ranked r
            JOIN category c ON c.id = r.category_id
            ORDER BY r.final_score DESC, r.published_date DESC, r.id DESC
            """;

    @Transactional(readOnly = true)
    public Slice<NewsSearchResponse> search(String keyword, float[] queryVector, Pageable pageable) {
        int fetchSize = pageable.getPageSize() + 1;
        int offset = (int) pageable.getOffset();
        // 깊은 페이지에서 후보가 잘리지 않도록 동적으로 계산 (최소 100, 최대 300)
        int semanticLimit = Math.min(Math.max(offset + pageable.getPageSize() + 1, 100), 300);
        int keywordLimit  = Math.min(Math.max(offset + pageable.getPageSize() + 1, 100), 300);

        // SET LOCAL은 현재 트랜잭션 내 커넥션에만 적용 → % 연산자 GIN 인덱스 필터 임계값 설정
        jdbc.execute("SET LOCAL pg_trgm.similarity_threshold = 0.25");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("queryVector", toVectorString(queryVector))
                .addValue("keyword", keyword)
                .addValue("semanticLimit", semanticLimit)
                .addValue("keywordLimit", keywordLimit)
                .addValue("limit", fetchSize)
                .addValue("offset", offset);

        List<NewsSearchResponse> rows = namedJdbc.query(HYBRID_SQL, params, (rs, rowNum) ->
                new NewsSearchResponse(
                        rs.getLong("id"),
                        rs.getString("category_name"),
                        rs.getString("title"),
                        rs.getDate("published_date").toLocalDate()
                )
        );

        boolean hasNext = rows.size() > pageable.getPageSize();
        if (hasNext) rows = rows.subList(0, pageable.getPageSize());

        return new SliceImpl<>(rows, pageable, hasNext);
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

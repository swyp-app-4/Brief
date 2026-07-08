package com.brife.news.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchSuggestionRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    private static final String SUGGESTION_SQL = """
            SELECT sn.title
            FROM summarized_news sn
            WHERE sn.is_summarized = true
              AND (
                sn.title ILIKE CONCAT(:keyword, '%')
                OR sn.title ILIKE CONCAT('%', :keyword, '%')
                OR sn.title % :keyword
              )
            GROUP BY sn.title
            ORDER BY
              CASE
                WHEN sn.title ILIKE CONCAT(:keyword, '%') THEN 0
                WHEN sn.title ILIKE CONCAT('%', :keyword, '%') THEN 1
                ELSE 2
              END,
              similarity(sn.title, :keyword) DESC,
              MAX(sn.published_date) DESC
            LIMIT :limit
            """;

    @Transactional(readOnly = true)
    public List<String> findTitleSuggestions(String keyword, int limit) {
        // SET LOCAL: 현재 트랜잭션 커넥션에만 적용 → % 연산자 GIN 인덱스 임계값 설정
        namedJdbc.getJdbcTemplate().execute("SET LOCAL pg_trgm.similarity_threshold = 0.25");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("keyword", keyword)
                .addValue("limit", limit);

        return namedJdbc.queryForList(SUGGESTION_SQL, params, String.class);
    }
}

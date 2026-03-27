package com.brife.news.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 후 검색 인덱스 생성 (Hibernate ddl-auto가 테이블 생성한 뒤 실행)
 *
 * - pg_trgm GIN 인덱스: 한국어 유사도 검색용
 * - pg_search BM25 인덱스: ParadeDB 전문 검색용 (pg_trgm 대비 더 정확한 관련도 랭킹)
 * - pgvector HNSW 인덱스: 유사 기사 검색용 (ANN 근사 최근접 이웃)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexConfig {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createSearchIndexes() {
        createTrgmIndexes();
        createVectorIndex();
        createBm25Index();
    }

    private void createTrgmIndexes() {
        try {
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_trgm_title
                    ON summarized_news USING GIN (title gin_trgm_ops)
                    """);
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_trgm_summary
                    ON summarized_news USING GIN (summary gin_trgm_ops)
                    """);
            log.info("[SearchIndex] pg_trgm GIN 인덱스 생성 완료");
        } catch (Exception e) {
            log.warn("[SearchIndex] pg_trgm 인덱스 생성 실패 (이미 존재하거나 확장 없음): {}", e.getMessage());
        }
    }

    private void createVectorIndex() {
        try {
            // HNSW 인덱스: 코사인 유사도 기반 ANN 검색 (DDL에 이미 정의, 앱 시작 시 보장)
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS idx_embedding_hnsw
                    ON summarized_news USING hnsw (embedding vector_cosine_ops)
                    WITH (m = 16, ef_construction = 64)
                    """);
            log.info("[SearchIndex] pgvector HNSW 인덱스 생성 완료");
        } catch (Exception e) {
            log.warn("[SearchIndex] HNSW 인덱스 생성 실패: {}", e.getMessage());
        }
    }

    private void createBm25Index() {
        try {
            // ParadeDB pg_search BM25 인덱스 (ICU 토크나이저로 한국어 지원)
            jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS summarized_news_bm25_idx
                    ON summarized_news
                    USING bm25 (id, title, summary)
                    WITH (
                        key_field = 'id',
                        text_fields = '{
                            "title":   {"tokenizer": {"type": "icu"}},
                            "summary": {"tokenizer": {"type": "icu"}}
                        }'
                    )
                    """);
            log.info("[SearchIndex] pg_search BM25 인덱스 생성 완료");
        } catch (Exception e) {
            log.warn("[SearchIndex] BM25 인덱스 생성 실패 (ParadeDB 미설치 시 무시): {}", e.getMessage());
        }
    }
}

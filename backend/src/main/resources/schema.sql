-- Hibernate DDL 실행 전에 필요한 익스텐션 활성화
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 뉴스 임베딩 테이블 (JPA vector 타입 미지원으로 직접 관리)
CREATE TABLE IF NOT EXISTS news_embedding (
    id         BIGSERIAL PRIMARY KEY,
    news_id    BIGINT NOT NULL UNIQUE,
    embedding  vector(768) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- HNSW 인덱스 (코사인 거리 기반 ANN 검색)
CREATE INDEX IF NOT EXISTS idx_news_embedding_hnsw
    ON news_embedding USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- GIN 인덱스 (pg_trgm 기반 텍스트 유사도 검색)
CREATE INDEX IF NOT EXISTS idx_summarized_news_title_trgm
    ON summarized_news USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_summarized_news_summary_trgm
    ON summarized_news USING gin (summary gin_trgm_ops);

-- Duplicate detection only compares recently generated news.
CREATE INDEX IF NOT EXISTS idx_summarized_news_created_at
    ON summarized_news (created_at DESC);

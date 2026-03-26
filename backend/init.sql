CREATE EXTENSION IF NOT EXISTS vector;

-- -- Full Text Search용 GIN 인덱스, MVP 이후 simple에서 한국어 형태소 분석기로 변환 예정
-- CREATE INDEX IF NOT EXISTS idx_summarized_news_fts
--     ON summarized_news
--     USING GIN (to_tsvector('simple', title || ' ' || summary));
CREATE EXTENSION IF NOT EXISTS vector;

-- -- Full Text Search용 GIN 인덱스, MVP 이후 simple에서 한국어 형태소 분석기로 변환 예정
-- CREATE INDEX IF NOT EXISTS idx_summarized_news_fts
--     ON summarized_news
--     USING GIN (to_tsvector('simple', title || ' ' || summary));

-- 테스트용 카테고리
INSERT INTO category (group_name, name, query)
VALUES ('IT 테크', 'AI', 'AI 인공지능');

-- 테스트용 토픽
INSERT INTO topic (category_id, keyword, article_count, created_date)
VALUES (1, 'AI 규제', 3, '2026-03-17');

-- 테스트용 요약 뉴스
INSERT INTO summarized_news (category_id, topic_id, title, summary, body, source_count, published_date, is_summarized)
VALUES
    (1, 1, '트럼프 AI 규제 완화 발표', 'AI 규제를 완화하겠다고 밝혔다. 기업들의 반응은 긍정적이다. 시장이 급등했다.', '본문 내용...', 3, '2026-03-17', true),
    (1, 1, 'EU AI 규제법 시행', '유럽연합이 AI 규제법을 시행했다. 빅테크 기업들이 반발하고 있다. 한국에도 영향이 예상된다.', '본문 내용...', 5, '2026-03-16', true),
    (1, 1, '삼성 AI 반도체 투자 확대', '삼성이 AI 반도체에 10조원을 투자한다. HBM 생산 라인을 증설할 계획이다. SK하이닉스와 경쟁이 심화된다.', '본문 내용...', 2, '2026-03-15', true);
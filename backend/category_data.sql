-- category 초기 데이터 (앱 첫 실행 후 한 번만 실행)
-- 대분류 6개 고정: 시사/정치, 경제/재테크, IT/테크, 문화/예술, 엔터/스포츠, 라이프/성장
-- 원칙: 띄어쓰기=AND(교집합)이므로 강력한 단일 키워드 1개만 사용. query=null이면 name을 검색어로 사용.

-- =============================================
-- 1. 대분류 (category_group)
-- =============================================
INSERT INTO category_group (name) VALUES
  ('시사/정치'),
  ('경제/재테크'),
  ('IT/테크'),
  ('문화/예술'),
  ('엔터/스포츠'),
  ('라이프/성장')
ON CONFLICT DO NOTHING;

-- =============================================
-- 2. 소분류 (category) - category_group FK 참조
-- =============================================
INSERT INTO category (category_group_id, name, query) VALUES

  -- 시사/정치 (16개)
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '청와대',       '대통령실'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '국회/정당',    '국회'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '북한',         '북한'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '행정',         '행정안전부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '국방/외교',    '국방부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '정치일반',     '정치'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '사건사고',     '사건사고'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '교육',         '교육부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '노동',         '노동조합'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '언론',         '방통위'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '환경',         '기후위기'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '인권/복지',    '복지부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '식품/의료',    '의료'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '지역',         '지자체'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '사회일반',     '사회'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '세계일반',     '국제'),

  -- 경제/재테크 (8개)
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '금융',       '금리'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '증권',       '코스피'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '산업/재계',  '삼성'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '중기/벤처',  '스타트업'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '부동산',     '아파트'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '글로벌경제', '연준'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '생활경제',   '물가'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '경제일반',   '경제'),

  -- IT/테크 (8개)
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '모바일',         '스마트폰'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '인터넷/SNS',     '카카오'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '통신/뉴미디어',  '넷플릭스'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), 'IT일반',         '인공지능'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '보안/해킹',      '사이버보안'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '컴퓨터',         '소프트웨어'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '게임/리뷰',      '게임'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '과학일반',       '우주'),

  -- 문화/예술 (4개)
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '공연/전시',    '뮤지컬'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '책',           '베스트셀러'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '종교',         '종교'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '영화',         '박스오피스'),

  -- 엔터/스포츠 (13개)
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '드라마',     '드라마'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '뮤직',       '케이팝'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '연예',       '연예인'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '야구',       '프로야구'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '해외야구',   '메이저리그'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '축구',       'K리그'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '해외축구',   '손흥민'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '농구',       'KBL'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '배구',       'V리그'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '골프',      '골프'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), 'e스포츠',    'LCK'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '아웃도어',   '등산'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '스포츠일반', '스포츠'),

  -- 라이프/성장 (8개)
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '건강정보',     '건강'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '자동차/시승기','전기차'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '도로/교통',    '교통'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '여행/레저',    '여행'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '음식/맛집',    '맛집'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '패션/뷰티',    '뷰티'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '날씨',         '기상청'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '생활문화일반', '반려동물')

ON CONFLICT DO NOTHING;

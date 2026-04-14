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
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '청와대',       '대통령실|대통령|용산'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '국회/정당',    '국회|더불어민주당|국민의힘'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '북한',         '북한|김정은'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '행정',         '행정안전부|정부부처'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '국방/외교',    '국방부|외교부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '정치일반',     '여야|정치권'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '사건사고',     '경찰|검찰|구속|압수수색|재판'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '교육',         '교육부|교육청'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '노동',         '노동조합|고용노동부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '언론',         '언론사|방송사|미디어'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '환경',         '기후위기|환경부'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '인권/복지',    '보건복지부|인권위'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '식품/의료',    '식약처|의료계'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '지역',         '지자체|지방의회'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '사회일반',     '저출산|고령화|인구감소'),
  ((SELECT id FROM category_group WHERE name = '시사/정치'), '세계일반',     '외신|특파원|유엔|UN'),

  -- 경제/재테크 (8개)
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '금융',       '금리|금융권|시중은행|대출'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '증권',       '코스피|코스닥|국내증시|상장'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '산업/재계',  '재계|영업이익|대기업|주주총회'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '중기/벤처',  '중소기업|스타트업|벤처기업'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '부동산',     '집값|청약|전세|부동산시장'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '글로벌경제', '미국 연준|뉴욕증시'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '생활경제',   '소비자물가|장바구니|공공요금|생활비'),
  ((SELECT id FROM category_group WHERE name = '경제/재테크'), '경제일반',   '한국은행|기재부|거시경제|무역수지'),

  -- IT/테크 (8개)
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '모바일',         '스마트폰|갤럭시|아이폰'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '인터넷/SNS',     '포털|카카오|네이버'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '통신/뉴미디어',  '이동통신|OTT'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), 'IT일반',         '인공지능|AI|빅데이터'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '보안/해킹',      '해킹|랜섬웨어|정보유출'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '컴퓨터',         '클라우드|소프트웨어|반도체'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '게임/리뷰',      '신작 게임|모바일게임'),
  ((SELECT id FROM category_group WHERE name = 'IT/테크'), '과학일반',       '연구진|과기정통부|우주항공'),

  -- 문화/예술 (4개)
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '공연/전시',    '공연|전시회|뮤지컬|클래식'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '책',           '신간|출판계|베스트셀러'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '종교',         '종교계|개신교|불교|천주교'),
  ((SELECT id FROM category_group WHERE name = '문화/예술'), '영화',         '극장가|개봉작|영화제|박스오피스'),

  -- 엔터/스포츠 (13개)
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '드라마',     '시청률|캐스팅|첫방|드라마'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '뮤직',       '컴백|신곡|음원차트|케이팝'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '연예',       '연예계|소속사|예능|배우'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '야구',       '프로야구|KBO'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '해외야구',   '메이저리그|MLB|오타니'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '축구',       'K리그|국가대표팀|축구협회'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '해외축구',   '프리미어리그|손흥민|이강인|김민재'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '농구',       '프로농구|KBL|WKBL'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '배구',       '프로배구|V리그'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '골프',       'PGA|LPGA|KLPGA|KPGA'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), 'e스포츠',    'e스포츠|LCK|페이커'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '아웃도어',   '캠핑|트레킹|아웃도어'),
  ((SELECT id FROM category_group WHERE name = '엔터/스포츠'), '스포츠일반', '대한체육회|올림픽|아시안게임'),

  -- 라이프/성장 (8개)
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '건강정보',     '전문의|질환|건강관리|영양소'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '자동차/시승기','신차|전기차|시승기|SUV'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '도로/교통',    '대중교통|고속도로|교통통제|한국도로공사'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '여행/레저',    '관광지|해외여행|항공권|호캉스'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '음식/맛집',    '외식업계|신제품|미쉐린|레시피'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '패션/뷰티',    '패션업계|화장품|컬렉션|스타일링'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '날씨',         '일기예보|날씨|미세먼지|기상청'),
  ((SELECT id FROM category_group WHERE name = '라이프/성장'), '생활문화일반', '1인가구|반려동물|라이프스타일|인테리어')

ON CONFLICT DO NOTHING;

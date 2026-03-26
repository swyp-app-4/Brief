package com.example.brife.data.local

import com.example.brife.feature.home.HomeNewsCardItem
import com.example.brife.feature.home.NewsSourceArticle

val shortsampleHomeNews = listOf(
    HomeNewsCardItem(
        category = "경제",
        subCategory = "금리/통화정책",
        title = "기준금리 동결 속 소비 회복 기대감 확대",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "한국은행이 기준금리를 동결하며 시장 안정 기대가 커지고 있다.",
            "소비와 투자 심리 회복 여부가 향후 핵심 변수로 작용한다.",
            "가계 부담 완화 여부가 경기 회복 속도를 좌우할 전망이다."
        ),
        insight = "금리 흐름은 대출, 소비, 투자 심리에 직접적인 영향을 미친다. 따라서 이번 이슈는 단순 금융 뉴스가 아니라 개인의 소비 전략과 자산관리에도 연결해서 해석할 필요가 있다."
    ),
    HomeNewsCardItem(
        category = "IT",
        subCategory = "AI/빅데이터",
        title = "생성형 AI 서비스 경쟁 본격화",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "글로벌 IT 기업들이 생성형 AI 기능을 서비스에 빠르게 도입 중이다.",
            "AI 기능이 플랫폼 경쟁력의 핵심 요소로 자리잡고 있다.",
            "사용자 경험 개선과 생산성 향상이 주요 경쟁 포인트다."
        ),
        insight = "생성형 AI는 단순 기능 추가를 넘어 플랫폼 락인 전략과 직결된다. 앞으로는 어떤 서비스가 더 자연스럽게 AI를 녹여내느냐가 경쟁력을 결정할 가능성이 높다."
    ),
    HomeNewsCardItem(
        category = "사회",
        subCategory = "주거/부동산",
        title = "청년 주거 지원 정책 체감도 격차 심화",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "청년 주거 지원 정책은 확대되고 있지만 체감도는 낮은 편이다.",
            "지역과 소득 조건에 따라 정책 접근성이 크게 차이난다.",
            "실제 혜택보다 신청 과정의 복잡성이 문제로 지적된다."
        ),
        insight = "정책의 효과는 단순 공급이 아니라 접근성과 체감도에서 결정된다. 따라서 정책 내용을 볼 때는 혜택뿐 아니라 신청 조건과 절차까지 함께 고려해야 한다."
    ),
    HomeNewsCardItem(
        category = "과학",
        subCategory = "우주/항공",
        title = "민간 중심 우주 산업 투자 확대 지속",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "민간 기업의 우주 산업 투자 규모가 지속적으로 증가하고 있다.",
            "위성, 발사체, 데이터 산업까지 영역이 확대되는 추세다.",
            "국가 주도에서 민간 중심 구조로 변화가 진행 중이다."
        ),
        insight = "우주 산업은 단순 기술 경쟁을 넘어 통신, 국방, 물류 등 다양한 산업과 연결된다. 장기적으로는 새로운 산업 생태계를 형성할 가능성이 크다."
    ),
    HomeNewsCardItem(
        category = "문화",
        subCategory = "미디어/콘텐츠",
        title = "숏폼 중심 뉴스 소비 패턴 강화",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "짧은 영상과 카드형 콘텐츠를 통한 뉴스 소비가 증가하고 있다.",
            "사용자들은 빠르고 간결한 정보 전달을 선호하는 경향을 보인다.",
            "기존 긴 기사 중심의 소비 방식은 점차 줄어드는 추세다."
        ),
        insight = "콘텐츠 형식이 바뀌면 정보 해석 방식도 함께 변화한다. 따라서 뉴스 소비에서는 단순 전달뿐 아니라 신뢰도와 맥락 유지가 중요한 요소로 작용한다."
    )
)




//.롱폼 용 데이터
val longsampleHomeNews = listOf(
    HomeNewsCardItem(
        category = "경제",
        subCategory = "금리/통화정책",
        title = "기준금리 동결 속 소비 회복 기대감 확대와 체감 경기 변화 가능성",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "한국은행이 기준금리를 동결하며 시장 안정 기대가 커지고 있다.",
            "소비와 투자 심리 회복 여부가 향후 핵심 변수로 작용한다.",
            "가계 부담 완화 여부가 경기 회복 속도를 좌우할 전망이다."
        ),
        insight = "금리 흐름은 대출, 소비, 투자 심리에 직접적인 영향을 미친다. 따라서 이번 이슈는 단순 금융 뉴스가 아니라 개인의 소비 전략과 자산관리에도 연결해서 해석할 필요가 있다.",
        companyName = "중앙일보",
        updatedAt = "2026년 03.25. 수요일 오전",
        relatedArticles = listOf(
            NewsSourceArticle(
                title = "한국은행, 기준금리 동결 기조 유지",
                content = "한국은행이 물가와 경기 상황을 종합적으로 고려해 기준금리를 동결했다. 시장은 향후 소비 심리 회복 가능성에 주목하고 있다."
            ),
            NewsSourceArticle(
                title = "가계 소비 회복 기대감 확대",
                content = "금리 부담 완화 기대 속에서 소비 회복 가능성이 제기되고 있다. 다만 실제 체감 경기 개선은 추가 지표를 통해 확인이 필요하다."
            ),
            NewsSourceArticle(
                title = "투자 심리 회복 여부가 핵심 변수",
                content = "전문가들은 향후 소비보다도 투자 심리의 회복 여부가 경기 흐름을 좌우할 수 있다고 보고 있다."
            )
        )
    ),
    HomeNewsCardItem(
        category = "IT",
        subCategory = "AI/빅데이터",
        title = "생성형 AI 서비스 경쟁 본격화, 플랫폼 간 차별화가 핵심으로 부상",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "글로벌 IT 기업들이 생성형 AI 기능을 서비스에 빠르게 도입 중이다.",
            "AI 기능이 플랫폼 경쟁력의 핵심 요소로 자리잡고 있다.",
            "사용자 경험 개선과 생산성 향상이 주요 경쟁 포인트다."
        ),
        insight = "생성형 AI는 단순 기능 추가를 넘어 플랫폼 락인 전략과 직결된다. 앞으로는 어떤 서비스가 더 자연스럽게 AI를 녹여내느냐가 경쟁력을 결정할 가능성이 높다.",
        companyName = "한겨레",
        updatedAt = "2026년 03.25. 수요일 오후",
        relatedArticles = listOf(
            NewsSourceArticle(
                title = "빅테크 기업, AI 기능 확대 경쟁",
                content = "주요 글로벌 플랫폼들이 생성형 AI 기반의 검색, 추천, 생산성 기능을 강화하며 경쟁 구도를 본격화하고 있다."
            ),
            NewsSourceArticle(
                title = "AI 서비스의 차별화 포인트는 UX",
                content = "전문가들은 단순 성능보다 사용자가 체감하는 편의성과 서비스 연결성이 핵심이라고 분석했다."
            ),
            NewsSourceArticle(
                title = "플랫폼 전략의 중심이 된 생성형 AI",
                content = "생성형 AI는 더 이상 실험적 기능이 아니라 주요 플랫폼의 핵심 전략 요소로 자리 잡고 있다."
            )
        )
    ),
    HomeNewsCardItem(
        category = "사회",
        subCategory = "주거/부동산",
        title = "청년 주거 지원 정책 체감도 격차 심화, 지역별 접근성 문제 제기",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "청년 주거 지원 정책은 확대되고 있지만 체감도는 낮은 편이다.",
            "지역과 소득 조건에 따라 정책 접근성이 크게 차이난다.",
            "실제 혜택보다 신청 과정의 복잡성이 문제로 지적된다."
        ),
        insight = "정책의 효과는 단순 공급이 아니라 접근성과 체감도에서 결정된다. 따라서 정책 내용을 볼 때는 혜택뿐 아니라 신청 조건과 절차까지 함께 고려해야 한다.",
        companyName = "경향신문",
        updatedAt = "2026년 03.25. 수요일 오전",
        relatedArticles = listOf(
            NewsSourceArticle(
                title = "청년층, 지원 정책 있지만 체감 낮아",
                content = "청년층 대상 주거 지원 정책이 확대되고 있지만 실제 수혜 경험은 제한적이라는 지적이 이어지고 있다."
            ),
            NewsSourceArticle(
                title = "지역별 주거 정책 접근성 차이",
                content = "대도시와 지방의 정책 정보 접근성과 공급 상황 차이로 인해 청년층의 체감도 격차가 커지고 있다."
            ),
            NewsSourceArticle(
                title = "복잡한 신청 절차도 문제",
                content = "지원 조건을 충족하더라도 복잡한 신청 절차와 정보 부족으로 인해 실질적 이용이 어렵다는 평가가 나온다."
            )
        )
    ),
    HomeNewsCardItem(
        category = "과학",
        subCategory = "우주/항공",
        title = "민간 중심 우주 산업 투자 확대 지속, 데이터 산업까지 확장",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "민간 기업의 우주 산업 투자 규모가 지속적으로 증가하고 있다.",
            "위성, 발사체, 데이터 산업까지 영역이 확대되는 추세다.",
            "국가 주도에서 민간 중심 구조로 변화가 진행 중이다."
        ),
        insight = "우주 산업은 단순 기술 경쟁을 넘어 통신, 국방, 물류 등 다양한 산업과 연결된다. 장기적으로는 새로운 산업 생태계를 형성할 가능성이 크다.",
        companyName = "조선일보",
        updatedAt = "2026년 03.25. 수요일 오후",
        relatedArticles = listOf(
            NewsSourceArticle(
                title = "민간 우주 산업 투자 증가세",
                content = "위성 발사와 우주 데이터 활용 시장 확대에 따라 민간 기업의 투자 규모가 꾸준히 커지고 있다."
            ),
            NewsSourceArticle(
                title = "우주 산업, 데이터 서비스로 확장",
                content = "기존 발사체 중심에서 위성 데이터 분석, 통신, 물류 보조 서비스 등으로 산업 영역이 넓어지고 있다."
            ),
            NewsSourceArticle(
                title = "정부 주도에서 민간 중심으로 이동",
                content = "세계 주요 국가에서 우주 산업의 구조가 정부 중심에서 민간 주도형으로 점진적으로 바뀌고 있다."
            )
        )
    ),
    HomeNewsCardItem(
        category = "문화",
        subCategory = "미디어/콘텐츠",
        title = "숏폼 중심 뉴스 소비 패턴 강화, 빠른 정보 전달 선호 뚜렷",
        notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
        summaryPoints = listOf(
            "짧은 영상과 카드형 콘텐츠를 통한 뉴스 소비가 증가하고 있다.",
            "사용자들은 빠르고 간결한 정보 전달을 선호하는 경향을 보인다.",
            "기존 긴 기사 중심의 소비 방식은 점차 줄어드는 추세다."
        ),
        insight = "콘텐츠 형식이 바뀌면 정보 해석 방식도 함께 변화한다. 따라서 뉴스 소비에서는 단순 전달뿐 아니라 신뢰도와 맥락 유지가 중요한 요소로 작용한다.",
        companyName = "동아일보",
        updatedAt = "2026년 03.25. 수요일 오전",
        relatedArticles = listOf(
            NewsSourceArticle(
                title = "숏폼 뉴스 소비 확대",
                content = "짧은 영상과 카드형 요약 콘텐츠가 뉴스 소비의 주요 방식으로 자리 잡고 있다."
            ),
            NewsSourceArticle(
                title = "빠른 전달 방식에 익숙해진 사용자",
                content = "사용자들은 긴 기사보다 핵심만 빠르게 전달하는 콘텐츠에 더 높은 선호를 보이고 있다."
            ),
            NewsSourceArticle(
                title = "뉴스 소비 방식 변화와 과제",
                content = "숏폼 중심 소비가 확산되면서 정보의 신뢰도와 맥락 유지가 새로운 과제로 떠오르고 있다."
            )
        )
    )
)
package com.brife.news.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleExtractorServiceTest {

    private ArticleExtractorService extractor;

    @BeforeEach
    void setUp() {
        extractor = new ArticleExtractorService();
    }

    // =============================================
    // 본문 선택자 테스트
    // =============================================

    @Test
    @DisplayName("네이버 뉴스 #dic_area 선택자로 본문 추출")
    void extractFromNaverDicArea() {
        String html = """
                <html><body>
                  <div id="dic_area">
                    이것은 테스트 기사 본문입니다. 충분히 긴 내용이 필요하므로 여러 문장을 추가합니다.
                    양자역학 연구팀이 새로운 실험 결과를 발표했습니다. 이번 연구는 국내 최초로 진행되었으며
                    학계의 큰 주목을 받고 있습니다. 앞으로의 연구 방향에 대해서도 활발한 논의가 이루어지고 있습니다.
                    연구진은 후속 실험을 통해 결과를 재검증하고 국제 학술지에 상세한 분석을 공개할 예정입니다.
                    관련 분야 전문가들은 산업 현장에 적용할 수 있는 가능성도 함께 검토하고 있습니다.
                  </div>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).isNotBlank();
        assertThat(result).contains("양자역학");
    }

    @Test
    @DisplayName("article 태그(HTML5 표준)로 본문 추출")
    void extractFromArticleTag() {
        String html = """
                <html><body>
                  <article>
                    인공지능 기술이 빠르게 발전하면서 다양한 산업 분야에 적용되고 있습니다.
                    특히 의료, 금융, 제조업 분야에서 눈에 띄는 성과를 보이고 있으며,
                    전문가들은 향후 5년 내 더욱 큰 변화가 있을 것으로 전망하고 있습니다.
                    정부도 관련 정책 마련에 적극 나서고 있어 귀추가 주목됩니다.
                    기업들도 전문 인력을 확보하고 데이터 기반 업무 체계를 도입하기 위한 투자를 확대하고 있습니다.
                    다만 개인정보 보호와 결과 검증을 위한 제도적 장치도 함께 마련해야 한다는 지적이 나옵니다.
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).isNotBlank();
        assertThat(result).contains("인공지능");
    }

    @Test
    @DisplayName("본문 선택자 없을 때 og:description fallback 사용")
    void fallbackToOgDescription() {
        String html = """
                <html>
                  <head>
                    <meta property="og:description" content="오늘 경제 지표가 발표되었습니다. 전문가들은 긍정적으로 평가했습니다."/>
                  </head>
                  <body><p>짧은 내용</p></body>
                </html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).isNotBlank();
        assertThat(result).contains("경제 지표");
    }

    @Test
    @DisplayName("본문이 100자 미만이면 해당 선택자 무시하고 다음 선택자 시도")
    void skipShortBody() {
        String html = """
                <html><body>
                  <div id="dic_area">짧은 본문</div>
                  <article>
                    이것은 충분히 긴 본문입니다. 기사 내용이 충분히 있어야 선택됩니다.
                    국내 경제 상황이 빠르게 변화하고 있으며 다양한 지표들이 이를 반영하고 있습니다.
                    전문가들은 앞으로의 전망에 대해 신중한 입장을 보이고 있습니다.
                    정부와 시장 참여자들은 추가 지표를 확인한 뒤 대응 방향을 결정할 예정입니다.
                    가계와 기업에 미치는 영향을 줄이기 위한 보완책도 함께 논의되고 있습니다.
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).contains("국내 경제");
    }

    // =============================================
    // 노이즈 제거 테스트
    // =============================================

    @Test
    @DisplayName("광고, 사진설명, 관련기사 등 노이즈 요소 제거")
    void removeNoiseElements() {
        String html = """
                <html><body>
                  <article>
                    <div class="ad">광고 배너입니다</div>
                    <div class="photo_desc">사진 설명입니다</div>
                    <div class="related_article">관련 기사 링크</div>
                    실제 기사 본문 내용입니다. 오늘 국회에서 중요한 법안이 통과되었습니다.
                    여야 의원들이 합의하여 처리된 이번 법안은 많은 시민들의 관심을 받고 있습니다.
                    전문가들도 이번 결정에 대해 긍정적인 평가를 내리고 있는 상황입니다.
                    법안 시행에 필요한 세부 기준은 관계 부처 협의를 거쳐 순차적으로 마련될 예정입니다.
                    국회는 제도 시행 과정에서 발생하는 문제를 지속해서 점검하겠다고 밝혔습니다.
                    <script>alert('악성스크립트')</script>
                    <nav>네비게이션 메뉴</nav>
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).doesNotContain("광고 배너");
        assertThat(result).doesNotContain("사진 설명");
        assertThat(result).doesNotContain("관련 기사");
        assertThat(result).doesNotContain("악성스크립트");
        assertThat(result).doesNotContain("네비게이션");
        assertThat(result).contains("국회");
    }

    // =============================================
    // clean() 정제 테스트
    // =============================================

    @Test
    @DisplayName("기자 이메일 주소 제거")
    void removeEmailAddress() {
        String html = """
                <html><body>
                  <article>
                    오늘 경제 지표가 발표되었습니다. 시장은 긍정적으로 반응했으며
                    투자자들의 관심이 높아지고 있습니다. 전문가들의 분석에 따르면
                    앞으로도 이런 추세가 지속될 것으로 보입니다. reporter@chosun.com
                    추가적인 문의는 위 이메일로 연락 바랍니다.
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).doesNotContain("reporter@chosun.com");
    }

    @Test
    @DisplayName("저작권 문구 제거 (ⓒ, ©, 무단전재)")
    void removeCopyrightText() {
        String html = """
                <html><body>
                  <article>
                    국내 주요 기업들이 올해 실적 발표를 앞두고 있습니다.
                    시장 전문가들은 대체로 긍정적인 전망을 내놓고 있으며,
                    투자자들의 기대감도 높아지고 있는 상황입니다.
                    여러 변수들을 종합적으로 고려해야 할 것으로 보입니다.
                    ⓒ 한국경제신문 무단전재 및 재배포 금지
                    © ChosunMedia 저작권자 무단배포 금지
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).doesNotContain("ⓒ");
        assertThat(result).doesNotContain("©");
        assertThat(result).doesNotContain("무단전재 및 재배포 금지");
    }

    @Test
    @DisplayName("▶ 언론사 구독 유도 문구 제거")
    void removeSubscriptionText() {
        String html = """
                <html><body>
                  <article>
                    정부가 새로운 부동산 정책을 발표했습니다. 이번 정책은 서민 주거
                    안정을 목표로 하고 있으며, 다양한 지원책이 포함되어 있습니다.
                    전문가들은 실효성에 대해 엇갈린 반응을 보이고 있습니다.
                    정부는 시장 상황을 지속해서 점검하고 필요할 경우 추가 대책을 마련하겠다고 설명했습니다.
                    정책 시행 이후 거래량과 주거 비용 변화가 주요 평가 기준이 될 것으로 예상됩니다.
                    ▶ 구독하고 더 많은 뉴스 보기
                    ▶ 조선일보 유튜브 채널 구독
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).doesNotContain("구독하고 더 많은 뉴스 보기");
        assertThat(result).doesNotContain("유튜브 채널 구독");
        assertThat(result).contains("부동산 정책");
    }

    // =============================================
    // 길이 제한 테스트
    // =============================================

    @Test
    @DisplayName("3000자 초과 본문은 3000자로 잘림")
    void truncateOver3000Chars() {
        String longBody = "가".repeat(4000);
        String html = "<html><body><article>" + longBody + "</article></body></html>";
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result.length()).isLessThanOrEqualTo(3000);
    }

    @Test
    @DisplayName("3000자 이하 본문은 그대로 반환")
    void keepUnder3000Chars() {
        String body = "정상적인 기사 본문입니다. ".repeat(50); // 약 600자
        String html = "<html><body><article>" + body + "</article></body></html>";
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result.length()).isLessThanOrEqualTo(3000);
        assertThat(result).isNotBlank();
    }

    // =============================================
    // 엣지 케이스
    // =============================================

    @Test
    @DisplayName("본문 선택자도 없고 og:description도 없으면 빈 문자열 반환")
    void returnEmptyWhenNoContent() {
        String html = "<html><body><p>짧음</p></body></html>";
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("연속 공백은 단일 공백으로 정리")
    void normalizeWhitespace() {
        String html = """
                <html><body>
                  <article>
                    첫번째   문장입니다.    두번째   문장입니다.   세번째 문장도 있습니다.
                    추가적인 내용이 더 있으며 충분히 긴 본문을 만들기 위해 작성합니다.
                    뉴스 기사의 본문은 일반적으로 이런 식으로 구성이 됩니다.
                  </article>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);
        String result = extractor.extractFromDoc(doc);

        assertThat(result).doesNotContain("   ");
    }

    @Test
    @DisplayName("언론사 메타 태그가 없으면 도메인으로 언론사명을 보완")
    void resolvePressNameFromDomain() {
        Document doc = Jsoup.parse("<html><head></head><body></body></html>");

        String result = extractor.extractPressName(doc, "https://www.osen.co.kr/article/123");

        assertThat(result).isEqualTo("OSEN");
    }
}

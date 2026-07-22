package com.brife.news.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class ArticleExtractorService {

    private static final int MAX_BODY_LENGTH = 3000;
    private static final int MIN_BODY_LENGTH = 200;

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36";

    private static final Map<String, String> PRESS_BY_DOMAIN = Map.ofEntries(
            Map.entry("osen.co.kr", "OSEN"),
            Map.entry("news.tvchosun.com", "TV조선"),
            Map.entry("biz.sbs.co.kr", "SBS Biz"),
            Map.entry("zdnet.co.kr", "ZDNet Korea"),
            Map.entry("stoo.com", "스포츠투데이"),
            Map.entry("megaeconomy.co.kr", "메가경제"),
            Map.entry("metroseoul.co.kr", "메트로신문"),
            Map.entry("joseilbo.com", "조세일보"),
            Map.entry("wowtv.co.kr", "한국경제TV"),
            Map.entry("ichannela.com", "채널A"),
            Map.entry("ohmynews.com", "오마이뉴스"),
            Map.entry("ekn.kr", "에너지경제"),
            Map.entry("pressian.com", "프레시안"),
            Map.entry("thisisgame.com", "디스이즈게임"),
            Map.entry("newsen.com", "뉴스엔"),
            Map.entry("naver.com", "네이버뉴스")
    );


    private static final List<String> BODY_SELECTORS = List.of(
        "#dic_area",             // 네이버 뉴스 (구버전)
        ".newsct_article",       // 네이버 뉴스 (신버전)
        "#article_body",         // 매일경제, 중앙일보
        "#articleBodyContents",  // 다음 뉴스
        ".article-txt",          // 연합뉴스
        ".article_body",         // 조선일보, 기타
        "#harmonyContainer",     // 카카오 뉴스
        ".article-content",      // 한겨레
        "#content-body",         // 동아일보
        ".news_body",            // 기타 언론사
        "article",               // HTML5 표준
        "[role=article]"         // 접근성 표준
    );

    // 본문 오염 요소
    private static final String NOISE_SELECTOR =
            "script, style, nav, header, footer, iframe, " +
            ".ad, .advertisement, .banner, " +
            ".end_photo_org, .photo_desc, .img_desc, " +
            ".related_article, .relation_wrap, " +
            ".reporter_area, .article_footer, " +
            "figure, figcaption";

    public String extract(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com")
                    .get();
            return extractFromDoc(doc);
        } catch (Exception e) {
            log.debug("[Extractor] 본문 추출 실패 - url={}, reason={}", url, e.getMessage());
            return "";
        }
    }

    public String extractPressName(Document doc) {
        return extractPressName(doc, null);
    }

    public String extractPressName(Document doc, String sourceUrl) {
        String siteName = doc.select("meta[property=og:site_name]").attr("content");
        if (isPlausiblePressName(siteName)) return siteName.trim();

        String publisher = doc.select("meta[property=article:publisher]").attr("content");
        if (isPlausiblePressName(publisher)) return publisher.trim();

        return resolvePressNameFromDomain(sourceUrl);
    }

    public String resolvePressNameFromDomain(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) return "";
        try {
            String host = URI.create(sourceUrl).getHost();
            if (host == null || host.isBlank()) return "";
            host = host.toLowerCase(Locale.ROOT);
            String normalizedHost = host.startsWith("www.") ? host.substring(4) : host;

            for (Map.Entry<String, String> entry : PRESS_BY_DOMAIN.entrySet()) {
                if (normalizedHost.equals(entry.getKey()) || normalizedHost.endsWith("." + entry.getKey())) {
                    return entry.getValue();
                }
            }
            return normalizedHost;
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private boolean isPlausiblePressName(String value) {
        if (value == null || value.isBlank()) return false;
        String trimmed = value.trim();
        return trimmed.length() <= 40
                && !trimmed.startsWith("http://")
                && !trimmed.startsWith("https://");
    }

    public String extractFromDoc(Document doc) {
        try {
            doc.select(NOISE_SELECTOR).remove();

            for (String selector : BODY_SELECTORS) {
                Element el = doc.selectFirst(selector);
                if (el != null) {
                    String text = el.text();
                    if (text.length() >= MIN_BODY_LENGTH) {
                        return clean(text);
                    }
                }
            }

            String ogDesc = doc.select("meta[property=og:description]").attr("content");
            if (!ogDesc.isBlank()) {
                return clean(ogDesc);
            }

            return "";

        } catch (Exception e) {
            log.debug("[Extractor] 본문 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    private String clean(String text) {
        String result = text
                .replaceAll("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}", "")
                .replaceAll("[ⓒ©]\\s*\\S+", "")
                .replaceAll("(?i)무단\\s*(전재|배포|복사|재배포).{0,30}금지", "")
                .replaceAll("(?i)저작권자.{0,20}무단.{0,20}금지", "")
                .replaceAll("▶[^\n]*", "")
                .replaceAll("\\[[가-힣a-zA-Z\\s]{1,20}\\]", "")
                .replaceAll("\\s{2,}", " ")
                .strip();

        return result.length() > MAX_BODY_LENGTH
                ? result.substring(0, MAX_BODY_LENGTH)
                : result;
    }
}

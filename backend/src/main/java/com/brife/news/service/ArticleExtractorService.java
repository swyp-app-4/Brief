package com.brife.news.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArticleExtractorService {

    private static final int MAX_BODY_LENGTH = 3000;
    private static final int MIN_BODY_LENGTH = 200;

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36";


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
        String siteName = doc.select("meta[property=og:site_name]").attr("content");
        if (!siteName.isBlank()) return siteName.trim();
        return "";
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

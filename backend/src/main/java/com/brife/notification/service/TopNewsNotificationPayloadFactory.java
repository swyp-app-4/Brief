package com.brife.notification.service;

import com.brife.notification.NewsNotificationSlot;
import com.brife.notification.dto.TopNewsNotificationItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TopNewsNotificationPayloadFactory {

    private static final int MAX_NEWS_COUNT = 5;
    private static final int MAX_TITLE_CODE_POINTS = 60;

    private final ObjectMapper objectMapper;

    public Map<String, String> create(NewsNotificationSlot slot, List<TopNewsNotificationItem> newsItems) {
        List<TopNewsNotificationItem> normalizedItems = normalize(newsItems);
        if (normalizedItems.isEmpty()) {
            throw new IllegalArgumentException("알림에 포함할 뉴스가 없습니다.");
        }

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("type", "TOP5_NEWS");
        payload.put("schemaVersion", "1");
        payload.put("destination", "HOME");
        payload.put("slot", slot.name());
        payload.put("title", slot.getNotificationTitle());
        payload.put("body", normalizedItems.stream()
                .map(item -> "▶ " + item.title())
                .reduce((left, right) -> left + "\n" + right)
                .orElse(""));
        payload.put("primaryNewsId", String.valueOf(normalizedItems.getFirst().newsId()));
        payload.put("newsItems", serialize(normalizedItems));
        return payload;
    }

    private List<TopNewsNotificationItem> normalize(List<TopNewsNotificationItem> newsItems) {
        if (newsItems == null) {
            return List.of();
        }
        return newsItems.stream()
                .filter(item -> item != null && item.newsId() != null
                        && item.title() != null && !item.title().isBlank())
                .limit(MAX_NEWS_COUNT)
                .map(item -> new TopNewsNotificationItem(item.newsId(), normalizeTitle(item.title())))
                .toList();
    }

    private String normalizeTitle(String title) {
        String normalized = title.replaceAll("\\s+", " ").trim();
        int codePointCount = normalized.codePointCount(0, normalized.length());
        if (codePointCount <= MAX_TITLE_CODE_POINTS) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, MAX_TITLE_CODE_POINTS - 1);
        return normalized.substring(0, endIndex) + "…";
    }

    private String serialize(List<TopNewsNotificationItem> newsItems) {
        try {
            return objectMapper.writeValueAsString(newsItems);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("뉴스 알림 payload 생성에 실패했습니다.", e);
        }
    }
}

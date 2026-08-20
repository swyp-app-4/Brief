package com.brife.notification.service;

import com.brife.notification.NewsNotificationSlot;
import com.brife.notification.dto.TopNewsNotificationItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TopNewsNotificationPayloadFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TopNewsNotificationPayloadFactory factory =
            new TopNewsNotificationPayloadFactory(objectMapper);

    @Test
    void createsOrderedTop5DataPayload() throws Exception {
        List<TopNewsNotificationItem> items = List.of(
                item(1L, "첫 뉴스"), item(2L, "둘째 뉴스"), item(3L, "셋째 뉴스"),
                item(4L, "넷째 뉴스"), item(5L, "다섯째 뉴스"), item(6L, "제외될 뉴스")
        );

        Map<String, String> payload = factory.create(NewsNotificationSlot.LUNCH, items);

        assertThat(payload)
                .containsEntry("type", "TOP5_NEWS")
                .containsEntry("schemaVersion", "1")
                .containsEntry("destination", "HOME")
                .containsEntry("slot", "LUNCH")
                .containsEntry("title", "점심 뉴스 TOP 5")
                .containsEntry("primaryNewsId", "1");
        assertThat(payload.get("body")).isEqualTo(
                "▶ 첫 뉴스\n▶ 둘째 뉴스\n▶ 셋째 뉴스\n▶ 넷째 뉴스\n▶ 다섯째 뉴스");

        List<Map<String, Object>> serializedItems = objectMapper.readValue(
                payload.get("newsItems"), new TypeReference<>() {});
        assertThat(serializedItems).extracting(item -> item.get("newsId"))
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(serializedItems).extracting(item -> item.get("title"))
                .containsExactly("첫 뉴스", "둘째 뉴스", "셋째 뉴스", "넷째 뉴스", "다섯째 뉴스");
    }

    @Test
    void normalizesWhitespaceAndTruncatesLongTitles() throws Exception {
        String longTitle = "가".repeat(80) + "\n뒤에 붙는 문장";

        Map<String, String> payload = factory.create(
                NewsNotificationSlot.MORNING,
                List.of(item(1L, longTitle))
        );

        List<Map<String, Object>> serializedItems = objectMapper.readValue(
                payload.get("newsItems"), new TypeReference<>() {});
        String title = (String) serializedItems.getFirst().get("title");
        assertThat(title.codePointCount(0, title.length())).isEqualTo(60);
        assertThat(title).endsWith("…").doesNotContain("\n");
    }

    private TopNewsNotificationItem item(Long id, String title) {
        return new TopNewsNotificationItem(id, title);
    }
}

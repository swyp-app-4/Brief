package com.brife.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewsNotificationSlot {

    MORNING("아침 뉴스 TOP 5"),
    LUNCH("점심 뉴스 TOP 5"),
    EVENING("저녁 뉴스 TOP 5"),
    NIGHT("오늘의 마무리 뉴스 TOP 5");

    private final String notificationTitle;
}

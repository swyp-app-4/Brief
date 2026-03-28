// [DTO - 응답] 알림 설정 응답 (전체/오전/점심/저녁/자기전 on/off 상태).
package com.brife.user.dto;

import com.brife.user.domain.UserNotificationSetting;
import lombok.Getter;

@Getter
public class NotificationSettingResponse {

    private final boolean notificationEnabled;
    private final boolean morningEnabled;
    private final boolean lunchEnabled;
    private final boolean eveningEnabled;
    private final boolean nightEnabled;

    public NotificationSettingResponse(UserNotificationSetting setting) {
        this.notificationEnabled = setting.isNotificationEnabled();
        this.morningEnabled = setting.isMorningEnabled();
        this.lunchEnabled = setting.isLunchEnabled();
        this.eveningEnabled = setting.isEveningEnabled();
        this.nightEnabled = setting.isNightEnabled();
    }
}

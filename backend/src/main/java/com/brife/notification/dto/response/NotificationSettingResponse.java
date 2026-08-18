package com.brife.notification.dto.response;

import com.brife.notification.entity.UserNotificationSetting;
import lombok.Getter;

@Getter
public class NotificationSettingResponse {
    private final boolean dailyNewsEnabled;
    private final boolean time8am;
    private final boolean time12pm;
    private final boolean time6pm;
    private final boolean time10pm;

    public NotificationSettingResponse(UserNotificationSetting setting) {
        this.dailyNewsEnabled = setting.isDailyNewsEnabled();
        this.time8am = setting.isTime8am();
        this.time12pm = setting.isTime12pm();
        this.time6pm = setting.isTime6pm();
        this.time10pm = setting.isTime10pm();
    }
}
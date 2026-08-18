package com.brife.notification.dto.request;

import lombok.Getter;

@Getter
public class NotificationSettingRequest {
    private boolean dailyNewsEnabled;
    private boolean time8am;
    private boolean time12pm;
    private boolean time6pm;
    private boolean time10pm;
}
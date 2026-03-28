// [DTO - 요청] 알림 설정 변경 요청 (전체/오전/점심/저녁/자기전 on/off).
package com.brife.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingRequest {

    private boolean notificationEnabled;
    private boolean morningEnabled;
    private boolean lunchEnabled;
    private boolean eveningEnabled;
    private boolean nightEnabled;
}

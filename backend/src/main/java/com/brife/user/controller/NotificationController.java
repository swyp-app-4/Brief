// [알림 설정 API] GET/PUT /users/me/notifications (알림 시간대별 on/off 조회 및 변경).
package com.brife.user.controller;

import com.brife.user.dto.NotificationSettingRequest;
import com.brife.user.dto.NotificationSettingResponse;
import com.brife.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationSettingResponse> getNotificationSetting(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationSetting(userId));
    }

    @PutMapping
    public ResponseEntity<NotificationSettingResponse> updateNotificationSetting(
            @AuthenticationPrincipal Long userId,
            @RequestBody NotificationSettingRequest request) {
        return ResponseEntity.ok(notificationService.updateNotificationSetting(userId, request));
    }
}

package com.brife.notification.Controller;

import com.brife.notification.dto.request.NotificationSettingRequest;
import com.brife.notification.dto.response.NotificationSettingResponse;
import com.brife.notification.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 설정")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> getSettings(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(notificationSettingService.getSettings(userId));
    }

    @Operation(summary = "알림 설정 수정")
    @PatchMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> updateSettings(
            Authentication authentication,
            @RequestBody NotificationSettingRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(notificationSettingService.updateSettings(userId, request));
    }
}
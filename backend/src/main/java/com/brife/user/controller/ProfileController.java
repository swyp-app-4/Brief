package com.brife.user.controller;

import com.brife.user.dto.InterestRequest;
import com.brife.user.dto.UserProfileResponse;
import com.brife.user.dto.UserProfileUpdate;
import com.brife.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 프로필 / 관심사")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @Operation(summary = "내 정보 수정")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal Long userId, @RequestBody UserProfileUpdate request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Long userId) {
        profileService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "관심사 저장 (온보딩)")
    @PostMapping("/me/interests")
    public ResponseEntity<Void> saveInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.saveInterests(userId, request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "관심사 재설정")
    @PutMapping("/me/interests")
    public ResponseEntity<Void> resetInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.resetInterests(userId, request);
        return ResponseEntity.ok().build();
    }
}

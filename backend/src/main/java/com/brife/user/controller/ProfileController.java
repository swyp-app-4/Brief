package com.brife.user.controller;

import com.brife.user.dto.InterestRequest;
import com.brife.user.dto.UserProfileResponse;
import com.brife.user.dto.UserProfileUpdate;
import com.brife.user.dto.WithdrawalRequest;
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
@Tag(name = "프로필", description = "내 프로필 조회/수정, 회원 탈퇴, 관심사 관리")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "내 프로필 조회", description = "인증된 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @Operation(summary = "내 프로필 수정", description = "인증된 사용자의 닉네임 등 프로필 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal Long userId, @RequestBody UserProfileUpdate request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @Operation(summary = "회원 탈퇴", description = "인증된 사용자를 탈퇴 처리합니다. Naver 사용자는 refresh token이 필요합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal Long userId,
            @RequestBody(required = false) WithdrawalRequest request) {
        profileService.deleteUser(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "관심사 저장", description = "온보딩 등에서 선택한 사용자의 관심사를 저장합니다.")
    @PostMapping("/me/interests")
    public ResponseEntity<Void> saveInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.saveInterests(userId, request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "관심사 재설정", description = "인증된 사용자의 기존 관심사를 새 관심사 목록으로 교체합니다.")
    @PutMapping("/me/interests")
    public ResponseEntity<Void> resetInterests(@AuthenticationPrincipal Long userId, @RequestBody InterestRequest request) {
        profileService.resetInterests(userId, request);
        return ResponseEntity.ok().build();
    }
}

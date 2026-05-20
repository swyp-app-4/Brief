package com.brife.user.controller;

import com.brife.user.dto.AuthResponse;
import com.brife.user.dto.TermsRequest;
import com.brife.user.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "소셜 로그인, 토큰 재발급, 로그아웃, 약관 동의")
public class AuthController {

    private final OAuthService oAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 accessToken으로 로그인하고 서비스 JWT를 발급합니다.")
    @PostMapping("/login/kakao")
    public ResponseEntity<AuthResponse> loginWithKakao(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithKakao(body.get("accessToken")));
    }

    @Operation(summary = "네이버 로그인", description = "네이버 accessToken으로 로그인하고 서비스 JWT를 발급합니다.")
    @PostMapping("/login/naver")
    public ResponseEntity<AuthResponse> loginWithNaver(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithNaver(body.get("accessToken")));
    }

    @Operation(summary = "구글 로그인", description = "구글 idToken으로 로그인하고 서비스 JWT를 발급합니다.")
    @PostMapping("/login/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithGoogle(body.get("idToken")));
    }

    @Operation(summary = "액세스 토큰 재발급", description = "refreshToken으로 새로운 accessToken을 발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String newAccessToken = oAuthService.refresh(body.get("refreshToken"));
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @Operation(summary = "로그아웃", description = "refreshToken을 만료 처리해 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        oAuthService.logout(body.get("refreshToken"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "약관 동의", description = "인증된 사용자의 필수/선택 약관 동의 상태를 저장합니다.")
    @PostMapping("/terms")
    public ResponseEntity<Void> agreeTerms(
            @AuthenticationPrincipal Long userId,
            @RequestBody TermsRequest request) {
        oAuthService.agreeTerms(userId, request);
        return ResponseEntity.ok().build();
    }
}

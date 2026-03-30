package com.brife.user.controller;

import com.brife.user.dto.AuthResponse;
import com.brife.user.dto.TermsRequest;
import com.brife.user.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login/kakao")
    public ResponseEntity<AuthResponse> loginWithKakao(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithKakao(body.get("accessToken")));
    }

    @PostMapping("/login/naver")
    public ResponseEntity<AuthResponse> loginWithNaver(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithNaver(body.get("accessToken")));
    }

    @PostMapping("/login/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.loginWithGoogle(body.get("idToken")));
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String newAccessToken = oAuthService.refresh(body.get("refreshToken"));
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        oAuthService.logout(body.get("refreshToken"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/terms")
    public ResponseEntity<Void> agreeTerms(
            @AuthenticationPrincipal Long userId,
            @RequestBody TermsRequest request) {
        oAuthService.agreeTerms(userId, request);
        return ResponseEntity.ok().build();
    }
}

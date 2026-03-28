// [DTO - 응답] 로그인 응답 (accessToken, refreshToken, isNewUser).
package com.brife.user.dto;

public record AuthResponse(String accessToken, String refreshToken, boolean isNewUser) {}

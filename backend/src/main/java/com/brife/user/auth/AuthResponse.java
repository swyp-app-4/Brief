package com.brife.user.auth;

public record AuthResponse(String accessToken, String refreshToken, boolean isNewUser) {}

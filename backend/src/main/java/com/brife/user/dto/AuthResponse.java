package com.brife.user.dto;

public record AuthResponse(String accessToken, String refreshToken, boolean isNewUser) {}

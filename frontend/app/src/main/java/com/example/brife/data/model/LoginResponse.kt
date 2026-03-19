package com.example.brife.data.model

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
)
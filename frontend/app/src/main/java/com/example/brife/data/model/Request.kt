package com.example.brife.data.model




data class InterestRequest(
    val categoryIds: List<Long>
)


data class SocialAccessTokenRequest(
    val accessToken: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class TermsRequest(
    val serviceTermsAgreed: Boolean,
    val privacyTermsAgreed: Boolean
)


data class ReissueRequest(
    val refreshToken: String
)



data class LogoutRequest(
    val refreshToken: String
)
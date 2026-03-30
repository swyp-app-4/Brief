package com.example.brife.data.model




// POST /users/me/interests (온보딩), PUT /users/me/interests (재설정)
data class InterestRequest(
    val categoryIds: List<Long>,
    val groupIds: List<Long>
)

// PATCH /users/me
data class UpdateProfileRequest(
    val nickname: String? = null,
    val profileImageUrl: String? = null
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
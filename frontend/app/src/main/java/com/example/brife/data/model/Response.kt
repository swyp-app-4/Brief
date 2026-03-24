package com.example.brife.data.model



data class ReissueResponse(
    val accessToken: String
)


data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
)


data class CategoryResponse(
    val id: Long,
    val groupName: String
)

data class SubCategoryResponse(
    val id: Long,
    val name: String,
    val parentCategoryId: Long,
    val parentCategoryName: String
)
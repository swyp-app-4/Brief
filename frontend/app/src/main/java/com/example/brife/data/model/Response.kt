package com.example.brife.data.model

import com.google.gson.annotations.SerializedName


data class ReissueResponse(
    val accessToken: String
)


data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
)


// GET /users/me 응답
data class UserProfileResponse(
    val id: Long,
    val nickname: String,
    val email: String,
    val profileImageUrl: String?
)

// GET /categories 응답: { "id": 0, "name": "string" }
data class CategoryResponse(
    val id: Long,
    @SerializedName("name") val groupName: String
)

// GET /categories/details 응답: { "id": 0, "categoryGroupId": 0, "name": "string" }
data class SubCategoryResponse(
    val id: Long,
    val name: String,
    @SerializedName("categoryGroupId") val parentCategoryId: Long,
    val parentCategoryName: String = ""
)

// GET /home/news/recommended 응답
data class RecommendedNewsResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("title") val title: String,
    @SerializedName("summaryList") val summaryList: List<String>,
    @SerializedName("bodyPreview") val bodyPreview: String,
    @SerializedName("sourceCount") val sourceCount: Int,
    @SerializedName("publishedDate") val publishedDate: String
)
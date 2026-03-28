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

// GET /news/top5, GET /home/news/recommended 공통 응답 구조
data class RecommendedNewsResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("title") val title: String,
    @SerializedName("summaryList") val summaryList: List<String>,
    @SerializedName("bodyPreview") val bodyPreview: String,
    @SerializedName("sourceCount") val sourceCount: Int,
    @SerializedName("publishedDate") val publishedDate: String
)

// GET /news/latest, GET /news/search 공통 응답 아이템
data class NewsListItem(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("title") val title: String,
    @SerializedName("publishedDate") val publishedDate: String
)

// 페이지네이션 응답 래퍼 (content만 사용, 1차)
data class NewsPageResponse(
    @SerializedName("content") val content: List<NewsListItem>,
    @SerializedName("last") val last: Boolean,
    @SerializedName("empty") val empty: Boolean
)

// GET /news/{id} 응답 — 2차 연동 예정
data class NewsDetailSection(
    @SerializedName("heading") val heading: String,
    @SerializedName("contentList") val contentList: List<String>
)

data class NewsDetailResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("title") val title: String,
    @SerializedName("summaryList") val summaryList: List<String>,
    @SerializedName("sections") val sections: List<NewsDetailSection>,
    @SerializedName("sourceCount") val sourceCount: Int,
    @SerializedName("publishedDate") val publishedDate: String
)
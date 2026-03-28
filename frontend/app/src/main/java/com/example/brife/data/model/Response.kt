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
package com.swyp.brife.data.model

import com.google.gson.annotations.SerializedName


data class ReissueResponse(
    val accessToken: String
)


data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
)

data class ApiErrorResponse(
    val code: String? = null,
    val message: String? = null
)


// GET /users/me 응답
data class UserProfileResponse(
    val id: Long,
    val nickname: String,
    val email: String,
    val profileImageUrl: String?
)

data class NotificationSettingsResponse(
    val dailyNewsEnabled: Boolean = false,
    val time8am: Boolean = false,
    val time12pm: Boolean = false,
    val time6pm: Boolean = false,
    val time10pm: Boolean = false
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
// 서버가 일부 필드를 반환하지 않을 경우 NPE 방지를 위해 모든 필드에 기본값 설정
data class RecommendedNewsResponse(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("groupName") val groupName: String = "",
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("summaryList") val summaryList: List<String> = emptyList(),
    @SerializedName("bodyPreview") val bodyPreview: String = "",
    @SerializedName("sourceCount") val sourceCount: Int = 0,
    @SerializedName("publishedDate") val publishedDate: String = ""
)




// GET /news/latest, GET /news/search 공통 응답 아이템
// 서버가 일부 필드를 반환하지 않을 경우 NPE 방지를 위해 모든 필드에 기본값 설정
data class NewsListItem(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("groupName") val groupName: String? = null,
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("publishedDate") val publishedDate: String = ""
)

// 페이지네이션 응답 래퍼 (content만 사용, 1차)
data class NewsPageResponse(
    @SerializedName("content") val content: List<NewsListItem> = emptyList(),
    @SerializedName("last") val last: Boolean = false,
    @SerializedName("empty") val empty: Boolean = true
)

// Archive 폴더 응답
// 서버가 일부 필드를 반환하지 않을 경우 NPE 방지를 위해 모든 필드에 기본값 설정
data class ArchiveFolderResponse(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("folderName") val folderName: String = "",
    @SerializedName("itemCount") val itemCount: Int = 0,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("favorite") val favorite: Boolean = false
)

// Archive 아이템 응답
data class ArchiveItemResponse(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("contentId") val contentId: Long = 0L,
    @SerializedName("savedAt") val savedAt: String = ""
)

// Archive 요청 바디
data class ArchiveStatsResponse(
    @SerializedName("totalCount") val totalCount: Int = 0,
    @SerializedName("weeklyCount") val weeklyCount: Int = 0,
    @SerializedName("folderCount") val folderCount: Int = 0
)

data class ArchiveSearchResponse(
    @SerializedName("folders") val folders: List<ArchiveFolderResponse> = emptyList(),
    @SerializedName("items") val items: List<ArchiveItemResponse> = emptyList()
)

data class CreateArchiveRequest(
    @SerializedName("folderName") val folderName: String
)

data class AddArchiveItemRequest(
    @SerializedName("contentId") val contentId: Long
)

// GET /news/{id} 응답 — 2차 연동 예정
data class NewsDetailSection(
    @SerializedName("heading") val heading: String = "",
    @SerializedName("contentList") val contentList: List<String> = emptyList()
)

data class NewsDetailResponse(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("groupName") val groupName: String = "",
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("summaryList") val summaryList: List<String> = emptyList(),
    @SerializedName("sections") val sections: List<NewsDetailSection> = emptyList(),
    @SerializedName("sourceCount") val sourceCount: Int = 0,
    @SerializedName("publishedDate") val publishedDate: String = ""
)


data class NewsSourceItemResponse(
    @SerializedName("title") val title: String = "",
    @SerializedName("sourceUrl") val sourceUrl: String = "",
    @SerializedName("publishedDate") val publishedDate: String = "",
    @SerializedName("pressName") val pressName: String = ""
)

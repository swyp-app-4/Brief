package com.example.brife.feature.profile

import androidx.annotation.DrawableRes
import com.example.brife.R

data class ProfileCategoryItem(
    val id: Long,
    val name: String,
    @DrawableRes val iconRes: Int
)

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val profileImageUrl: String? = null,
    @DrawableRes val profileImageRes: Int = R.drawable.img_profile_avatar,
    val interests: List<ProfileCategoryItem> = emptyList()
)

fun categoryItemFromId(id: Long): ProfileCategoryItem? = when (id) {
    1L -> ProfileCategoryItem(1L, "시사 정치", R.drawable.news_politics)
    2L -> ProfileCategoryItem(2L, "경제 재테크", R.drawable.economy)
    3L -> ProfileCategoryItem(3L, "IT 테크", R.drawable.ittech)
    4L -> ProfileCategoryItem(4L, "문화 예술", R.drawable.cultureart)
    5L -> ProfileCategoryItem(5L, "엔터 스포츠", R.drawable.entsports)
    6L -> ProfileCategoryItem(6L, "라이프 성장", R.drawable.lifegrowth)
    else -> null
}

fun profileImageUrlFromDrawableRes(@DrawableRes drawableRes: Int): String = when (drawableRes) {
    R.drawable.img_profile_avatar -> "img_profile_avatar"
    R.drawable.img_profile_economy -> "img_profile_economy"
    R.drawable.img_profile_entertainment -> "img_profile_entertainment"
    R.drawable.img_profile_life -> "img_profile_life"
    R.drawable.img_profile_tech -> "img_profile_tech"
    R.drawable.img_profile_politics -> "img_profile_politics"
    else -> "img_profile_avatar"
}

@DrawableRes
fun profileImageResFromUrl(profileImageUrl: String?): Int = when (profileImageUrl) {
    "img_profile_avatar" -> R.drawable.img_profile_avatar
    "img_profile_economy" -> R.drawable.img_profile_economy
    "img_profile_entertainment" -> R.drawable.img_profile_entertainment
    "img_profile_life" -> R.drawable.img_profile_life
    "img_profile_tech" -> R.drawable.img_profile_tech
    "img_profile_politics" -> R.drawable.img_profile_politics
    else -> R.drawable.img_profile_avatar
}

val mockGuestProfileState = ProfileUiState(
    isLoggedIn = false,
    profileImageRes = R.drawable.img_profile_avatar,
    interests = listOf(
        ProfileCategoryItem(1L, "시사 정치", R.drawable.news_politics),
        ProfileCategoryItem(2L, "경제 재테크", R.drawable.economy),
        ProfileCategoryItem(3L, "IT 테크", R.drawable.ittech)
    )
)

val mockLoggedInProfileState = ProfileUiState(
    isLoggedIn = true,
    userName = "홍길동",
    userEmail = "user@brife.com",
    profileImageUrl = "img_profile_avatar",
    profileImageRes = R.drawable.img_profile_avatar,
    interests = listOf(
        ProfileCategoryItem(1L, "시사 정치", R.drawable.news_politics),
        ProfileCategoryItem(2L, "경제 재테크", R.drawable.economy)
    )
)

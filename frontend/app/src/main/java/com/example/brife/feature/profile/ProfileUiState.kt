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
    val interests: List<ProfileCategoryItem> = emptyList()
)

// 비로그인 mock — 관심사 3개
val mockGuestProfileState = ProfileUiState(
    isLoggedIn = false,
    interests = listOf(
        ProfileCategoryItem(1L, "시사 정치", R.drawable.news_politics),
        ProfileCategoryItem(2L, "경제 재테크", R.drawable.economy),
        ProfileCategoryItem(3L, "IT 테크", R.drawable.ittech)
    )
)

// 로그인 mock — 관심사 2개
val mockLoggedInProfileState = ProfileUiState(
    isLoggedIn = true,
    userName = "홍길동",
    userEmail = "user@brife.com",
    interests = listOf(
        ProfileCategoryItem(1L, "시사 정치", R.drawable.news_politics),
        ProfileCategoryItem(2L, "경제 재테크", R.drawable.economy)
    )
)

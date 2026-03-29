package com.example.brife.feature.home

data class HomeUiState(
    val newsList: List<HomeNewsCardItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

package com.swyp.brife.feature.home

data class NewsSourceArticle(
    val title: String,
    val content: String
)

data class HomeNewsCardItem(
    val newsId: Long = 0L,
    val category: String,
    val subCategory: String = "",
    val imageRes: Int? = null,
    val title: String,
    val notice: String,
    val summaryPoints: List<String>,
    val insight: String,
    val companyName: String = "",
    val updatedAt: String = "",
    val relatedArticles: List<NewsSourceArticle> = emptyList(),
    val articleCount: Int,
)

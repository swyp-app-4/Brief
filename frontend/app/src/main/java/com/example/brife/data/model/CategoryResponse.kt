package com.example.brife.data.model

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
package com.example.brife.feature.home

import com.example.brife.R

object LongFormImageProvider {

    private val politics = listOf(
        R.drawable.longform_politics_1,
        R.drawable.longform_politics_2,
        R.drawable.longform_politics_3,
        R.drawable.longform_politics_4
    )
    private val economy = listOf(
        R.drawable.longform_economy_1,
        R.drawable.longform_economy_2,
        R.drawable.longform_economy_3,
        R.drawable.longform_economy_4
    )
    private val tech = listOf(
        R.drawable.longform_tech_1,
        R.drawable.longform_tech_2,
        R.drawable.longform_tech_3,
        R.drawable.longform_tech_4
    )
    private val culture = listOf(
        R.drawable.longform_culture_1,
        R.drawable.longform_culture_2,
        R.drawable.longform_culture_3,
        R.drawable.longform_culture_4
    )
    private val entertainment = listOf(
        R.drawable.longform_entertainment_1,
        R.drawable.longform_entertainment_2,
        R.drawable.longform_entertainment_3,
        R.drawable.longform_entertainment_4
    )
    private val life = listOf(
        R.drawable.longform_life_1,
        R.drawable.longform_life_2,
        R.drawable.longform_life_3,
        R.drawable.longform_life_4
    )

    // 구분자(·, /, 공백 등) 형식에 무관하게 contains로 매핑
    private fun imagesForCategory(category: String): List<Int> = when {
        category.contains("시사") || category.contains("정치") -> politics
        category.contains("경제") || category.contains("재테크") -> economy
        category.contains("IT") || category.contains("테크") -> tech
        category.contains("문화") || category.contains("예술") -> culture
        category.contains("엔터") || category.contains("스포츠") || category.contains("연예") -> entertainment
        category.contains("라이프") || category.contains("성장") -> life
        else -> life
    }

    /** 카테고리에 해당하는 이미지 리소스 ID를 랜덤으로 반환. 매칭 없으면 default 이미지 */
    fun getRandomImageRes(category: String): Int {
        return imagesForCategory(category).random()
    }

    /** index 기반으로 카테고리 이미지를 결정론적으로 반환. 같은 category+index면 항상 동일한 이미지 */
    fun getStableImageRes(category: String, index: Int): Int {
        val images = imagesForCategory(category)
        return images[kotlin.math.abs(index) % images.size]
    }
}

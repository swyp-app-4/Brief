package com.example.brife.feature.home

import com.example.brife.R

object LongFormImageProvider {

    private val categoryImageMap: Map<String, List< Int>> = mapOf(
        "시사 정치" to listOf(
            R.drawable.longform_politics_1,
            R.drawable.longform_politics_2,
            R.drawable.longform_politics_3,
            R.drawable.longform_politics_4
        ),
        "경제 재테크" to listOf(
            R.drawable.longform_economy_1,
            R.drawable.longform_economy_2,
            R.drawable.longform_economy_3,
            R.drawable.longform_economy_4
        ),
        "IT 테크" to listOf(
            R.drawable.longform_tech_1,
            R.drawable.longform_tech_2,
            R.drawable.longform_tech_3,
            R.drawable.longform_tech_4
        ),
        "문화 예술" to listOf(
            R.drawable.longform_culture_1,
            R.drawable.longform_culture_2,
            R.drawable.longform_culture_3,
            R.drawable.longform_culture_4
        ),
        "엔터 스포츠" to listOf(
            R.drawable.longform_entertainment_1,
            R.drawable.longform_entertainment_2,
            R.drawable.longform_entertainment_3,
            R.drawable.longform_entertainment_4
        ),
        "라이프 성장" to listOf(
            R.drawable.longform_life_1,
            R.drawable.longform_life_2,
            R.drawable.longform_life_3,
            R.drawable.longform_life_4
        )
    )

    /** 카테고리에 해당하는 이미지 리소스 ID를 랜덤으로 반환. 매칭 없으면 default 이미지 */
    fun getRandomImageRes(category: String): Int {
        return categoryImageMap[category]?.random() ?: R.drawable.homescreen_bg
    }
}

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

    /**
     * 소분류 기준 이미지 풀을 반환한다.
     * 매핑된 소분류가 없으면 null → 호출부에서 대분류 풀로 fallback.
     *
     * 주의: "건강정보일반"이 "건강정보"를 포함하므로 구체적인 조건을 먼저 체크.
     */
    private fun imagesForSubCategory(category: String, subCategory: String): List<Int>? {
        // 시사/정치, 경제/재테크, IT/테크는 대분류 매핑 유지 (소분류 분기 없음)
        return when {

            // ── 문화/예술 ────────────────────────────────────────────────────
            category.contains("문화") || category.contains("예술") -> when {
                subCategory.contains("공연") || subCategory.contains("전시") ->
                    listOf(
                        R.drawable.longform_culture_1,
                        R.drawable.longform_culture_3,
                        R.drawable.longform_entertainment_3
                    )
                subCategory.contains("영화") ->
                    listOf(R.drawable.longform_entertainment_1)

                subCategory.contains("책") ->
                    listOf(R.drawable.ic_longform_culture_book)

                subCategory.contains("종교") ->
                    listOf(
                        R.drawable.ic_longform_culture_religion_1,
                        R.drawable.ic_longform_culture_religion_2
                    )

                else -> null
            }

            // ── 엔터/스포츠 ──────────────────────────────────────────────────
            category.contains("엔터") || category.contains("스포츠") || category.contains("연예") -> when {
                subCategory.contains("드라마") ->
                    listOf(R.drawable.longform_entertainment_1)

                subCategory.contains("뮤직") ->
                    listOf(R.drawable.longform_entertainment_3)

                subCategory.contains("연예") ->
                    listOf(
                        R.drawable.longform_entertainment_1,
                        R.drawable.longform_entertainment_3
                    )

                // "해외축구"도 "축구"를 포함하므로 함께 처리
                subCategory.contains("축구") ->
                    listOf(R.drawable.longform_entertainment_2)

                // "해외야구"도 "야구"를 포함하므로 함께 처리
                subCategory.contains("야구") ->
                    listOf(R.drawable.ic_longform_entertainment_baseball)

                subCategory.contains("농구") ->
                    listOf(R.drawable.ic_longform_entertainment_basketball)

                subCategory.contains("배구") ->
                    listOf(R.drawable.ic_longform_entertainment_volleyball)

                subCategory.contains("골프") ->
                    listOf(R.drawable.ic_longform_entertainment_golf)

                subCategory.contains("e스포츠") ->
                    listOf(R.drawable.ic_longform_entertainment_esports)

                subCategory.contains("아웃도어") ->
                    listOf(R.drawable.ic_longform_entertainment_outdoor)

                subCategory.contains("스포츠일반") ->
                    listOf(R.drawable.ic_longform_entertainment_general)

                else -> null
            }

            // ── 라이프/성장 ──────────────────────────────────────────────────
            category.contains("라이프") || category.contains("성장") -> when {
                // 더 구체적인 조건 먼저
                subCategory.contains("건강정보일반") || subCategory.contains("생활문화일반") ->
                    listOf(R.drawable.longform_life_4)

                subCategory.contains("건강정보") ->
                    listOf(R.drawable.longform_life_4)

                subCategory.contains("자동차") || subCategory.contains("시승기") ->
                    listOf(R.drawable.ic_longform_life_car)

                subCategory.contains("도로") || subCategory.contains("교통") ->
                    listOf(R.drawable.ic_longform_life_traffic)

                subCategory.contains("여행") || subCategory.contains("레저") ->
                    listOf(R.drawable.ic_longform_life_travel)

                subCategory.contains("음식") || subCategory.contains("맛집") ->
                    listOf(R.drawable.ic_longform_life_food)

                subCategory.contains("패션") || subCategory.contains("뷰티") ->
                    listOf(R.drawable.ic_longform_life_fashion)

                subCategory.contains("날씨") ->
                    listOf(R.drawable.ic_longform_life_weather)

                else -> null
            }

            else -> null
        }
    }

    /**
     * newsId를 seed로 사용하여 항상 동일한 이미지를 반환한다.
     * - 소분류 매핑 있음 → 소분류 풀에서 결정론적 선택
     * - 소분류 매핑 없음 → 대분류 풀에서 결정론적 선택
     * - category/subCategory가 동일하면 재구성 시에도 같은 이미지 유지
     */
    fun getStableImageRes(category: String, subCategory: String, newsId: Long): Int {
        val images = imagesForSubCategory(category, subCategory) ?: imagesForCategory(category)
        return images[kotlin.math.abs(newsId).toInt() % images.size]
    }

    /** 기존 호환용 — 대분류 기준 index 결정론적 반환 */
    fun getStableImageRes(category: String, index: Int): Int {
        val images = imagesForCategory(category)
        return images[kotlin.math.abs(index) % images.size]
    }
}

package com.example.brife.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.data.model.SubCategoryResponse
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.CtaActive
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.InterestSelectedLight
import com.example.brife.ui.theme.PrimaryNormal

@Composable
fun OnboardingSubInterestScreen(
    uiState: OnboardingSubInterestUiState,
    onSubCategoryClick: (Long) -> Unit = {},
    onSkipClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        AppText(
            text = "어떤 주제가\n궁금하시나요?",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        AppText(
            text = "원하는 관심사를\n자유롭게 선택해주세요",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))


        when {
            uiState.isLoading -> {
                Text(
                    text = "소분류 카테고리 불러오는 중...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                // 대분류별 독립적인 펼침 상태 (categoryId 기준)
                val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    uiState.sections.forEach { section ->
                        SubCategorySectionView(
                            categoryName = section.categoryName,
                            iconRes = getInterestIconRes(section.categoryName),
                            subCategories = section.subCategories,
                            selectedIds = uiState.selectedSubCategoryIds,
                            onChipClick = onSubCategoryClick,
                            expanded = expandedMap[section.categoryId] == true,
                            onExpandToggle = {
                                expandedMap[section.categoryId] =
                                    !(expandedMap[section.categoryId] ?: false)
                            }
                        )
                    }
                }
            }
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSkipClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CtaDisabled,
                    contentColor = Color.White
                )
            ) {
                AppText(
                    text = "건너뛰기",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Button(
                onClick = onSubmitClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CtaActive,
                    contentColor = Color.White
                )
            ) {
                AppText(
                    text = if (uiState.isSubmitting) "저장 중..." else "다음",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SubCategorySectionView(
    categoryName: String,
    iconRes: Int,
    subCategories: List<SubCategoryResponse>,
    selectedIds: List<Long>,
    onChipClick: (Long) -> Unit,
    expanded: Boolean,
    onExpandToggle: () -> Unit
) {
    // 4개 미만이면 항상 전체 표시 (접기/펼치기 불필요)
    val needsCollapse = subCategories.size > 3
    val visibleItems = when {
        !needsCollapse -> subCategories
        expanded -> subCategories
        else -> subCategories.take(3)
    }
    val hiddenCount = subCategories.size - 3  // collapsed 상태에서 숨겨진 개수

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = categoryName,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                AppText(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (needsCollapse) {
                MoreChip(
                    text = "+N",
                    expanded = expanded,
                    onClick = onExpandToggle
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            visibleItems.forEach { subCategory ->
                SubCategoryChip(
                    text = subCategory.name,
                    selected = selectedIds.contains(subCategory.id),
                    onClick = { onChipClick(subCategory.id) }
                )
            }
        }
    }
}

@Composable
private fun SubCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) InterestSelectedLight else ComponentDefault,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 0.dp,
            color = if (selected) PrimaryNormal else androidx.compose.ui.graphics.Color.Transparent
        )
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

// +N 토글 버튼 — 접힘: ComponentDefault 배경 / 펼침: PrimaryNormal 배경(활성)
@Composable
private fun MoreChip(
    text: String,
    expanded: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (expanded) PrimaryNormal else ComponentDefault,
        border = BorderStroke(
            width = if (expanded) 0.dp else 0.dp,
            color = Color.Transparent
        )
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (expanded) Color.White else PrimaryNormal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

private fun getInterestIconRes(groupName: String): Int {
    return when (groupName) {
        "시사 정치", "시사/정치", "시사•정치" -> R.drawable.news_politics
        "경제 재테크", "경제/재테크", "경제•재테크" -> R.drawable.economy
        "IT 테크", "IT/테크", "IT•테크" -> R.drawable.ittech
        "문화 예술", "문화/예술", "문화•예술" -> R.drawable.cultureart
        "엔터 스포츠", "엔터/스포츠", "연예•스포츠", "엔터•스포츠" -> R.drawable.entsports
        "라이프 성장", "라이프/성장", "라이프•성장" -> R.drawable.lifegrowth
        else -> R.drawable.news_politics
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "소분류 선택 - 접힘 상태")
@Composable
fun OnboardingSubInterestScreenPreview() {
    OnboardingSubInterestScreen(
        uiState = OnboardingSubInterestUiState(
            sections = listOf(
                SubCategorySection(
                    categoryId = 1L,
                    categoryName = "시사 정치",
                    subCategories = listOf(
                        SubCategoryResponse(1L, "청와대", 1L, "시사 정치"),
                        SubCategoryResponse(2L, "국회/정당", 1L, "시사 정치"),
                        SubCategoryResponse(3L, "북한", 1L, "시사 정치"),
                        SubCategoryResponse(4L, "행정", 1L, "시사 정치"),
                        SubCategoryResponse(5L, "외교/국방", 1L, "시사 정치"),
                        SubCategoryResponse(6L, "사회", 1L, "시사 정치")
                    )
                ),
                SubCategorySection(
                    categoryId = 2L,
                    categoryName = "경제 재테크",
                    subCategories = listOf(
                        SubCategoryResponse(11L, "금융", 2L, "경제 재테크"),
                        SubCategoryResponse(12L, "증권", 2L, "경제 재테크"),
                        SubCategoryResponse(13L, "산업/재계", 2L, "경제 재테크"),
                        SubCategoryResponse(14L, "부동산", 2L, "경제 재테크"),
                        SubCategoryResponse(15L, "글로벌 경제", 2L, "경제 재테크")
                    )
                )
            ),
            selectedSubCategoryIds = listOf(2L, 12L),
            isSubmitting = false
        ),
        onSkipClick = {},
        onSubmitClick = {}
    )
}
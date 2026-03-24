package com.example.brife.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.data.model.CategoryResponse
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.OnboardingInterestRowCard
import com.example.brife.ui.component.PrimaryButton

@Composable
fun OnboardingInterestScreen(
    uiState: OnboardingUiState,
    onCategoryClick: (Long) -> Unit = {},
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

        Spacer(modifier = Modifier.height(20.dp))

        AppText(
            text = "원하는 관심사를\n최대 3개까지 선택해주세요",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Text(
                    text = "카테고리 불러오는 중...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(uiState.categories) { category ->
                        OnboardingInterestRowCard(
                            text = category.groupName,
                            iconRes = getInterestIconRes(category.groupName),
                            selected = uiState.selectedCategoryIds.contains(category.id),
                            onClick = { onCategoryClick(category.id) }
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

        Spacer(modifier = Modifier.height(10.dp))

        PrimaryButton(
            text = if (uiState.isSubmitting) "저장 중..." else "다음",
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getInterestIconRes(groupName: String): Int {
    return when (groupName) {
        "시사 정치", "시사•정치" -> R.drawable.news_politics
        "경제 재테크", "경제•재테크" -> R.drawable.economy
        "IT 테크", "IT•테크" -> R.drawable.ittech
        "문화 예술", "문화•예술" -> R.drawable.cultureart
        "엔터 스포츠", "연예•스포츠", "엔터•스포츠" -> R.drawable.entsports
        "라이프 성장", "라이프•성장" -> R.drawable.lifegrowth
        else -> R.drawable.news_politics
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingInterestScreenPreview() {
    MaterialTheme {
        OnboardingInterestScreen(
            uiState = OnboardingUiState(
                isLoading = false,
                categories = listOf(
                    CategoryResponse(1L, "시사 정치"),
                    CategoryResponse(2L, "경제 재테크"),
                    CategoryResponse(3L, "IT 테크"),
                    CategoryResponse(4L, "문화 예술"),
                    CategoryResponse(5L, "엔터 스포츠"),
                    CategoryResponse(6L, "라이프 성장")
                ),
                selectedCategoryIds = listOf(1L, 3L)
            )
        )
    }
}
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.InterestCard
import com.example.brife.ui.component.PrimaryButton

@Composable
fun OnboardingInterestScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {}
) {
    val interests = listOf(
        "시사•정치" to R.drawable.news_politics,
        "경제•재테크" to R.drawable.economy,
        "IT•테크" to R.drawable.ittech,
        "문화•예술" to R.drawable.cultureart,
        "연예•스포츠" to R.drawable.entsports,
        "라이프•성장" to R.drawable.lifegrowth
    )

    var selectedInterests by remember {
        mutableStateOf(setOf<String>())
    }

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
            text = "관심사를 바탕으로\n뉴스를 추천해드릴게요",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(60.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {

            items(interests) { (interest, iconRes) ->
                InterestCard(
                    text = interest,
                    iconRes = iconRes,
                    selected = interest in selectedInterests,
                    onClick = {
                        selectedInterests =
                            if (interest in selectedInterests) {
                                selectedInterests - interest
                            } else {
                                selectedInterests + interest
                            }
                    }
                )
            }
        }



        PrimaryButton(
            text = "다음",
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingInterestScreenPreview() {
    MaterialTheme {
        OnboardingInterestScreen()
    }
}
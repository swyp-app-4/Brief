package com.example.brife.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextCaption

@Composable
fun NewsLongScreen(
    item: HomeNewsCardItem,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(24.dp)) {
            AppText(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppText(
                text = item.notice,
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppText(
                text = "핵심 요약",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryNormal
            )

            Spacer(modifier = Modifier.height(16.dp))

            item.summaryPoints.forEach { point ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    AppText(text = "•", color = PrimaryNormal)
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = point,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(20.dp)
            ) {
                Column {
                    AppText(
                        text = "💡 브리프 인사이트",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppText(
                        text = item.insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
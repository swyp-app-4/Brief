package com.swyp.brife.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.TextBody

enum class ShareFlowStep {
    Closed,
    Platform,
    InstagramDestination,
    Template,
    Rendering
}

data class InstagramShareData(
    val newsId: Long,
    val category: String,
    val subCategory: String,
    val title: String,
    val summaryPoints: List<String>,
    val publishedDate: String,
    val imageRes: Int?
)

fun HomeNewsCardItem.toInstagramShareData(
    representativeImageRes: Int? = imageRes
): InstagramShareData = InstagramShareData(
    newsId = newsId,
    category = category,
    subCategory = subCategory,
    title = title,
    summaryPoints = summaryPoints,
    publishedDate = updatedAt,
    imageRes = representativeImageRes
)

@Composable
fun NewsShareFlowHost(
    step: ShareFlowStep,
    shareData: InstagramShareData?,
    onStepChange: (ShareFlowStep) -> Unit,
    onDismiss: () -> Unit,
    onOtherShareClick: (InstagramShareData) -> Unit
) {
    val data = shareData ?: return

    when (step) {
        ShareFlowStep.Platform -> SharePlatformBottomSheet(
            onDismissRequest = onDismiss,
            onInstagramClick = { onStepChange(ShareFlowStep.InstagramDestination) },
            onOtherShareClick = { onOtherShareClick(data) }
        )

        ShareFlowStep.InstagramDestination -> InstagramDestinationBottomSheet(
            onDismissRequest = onDismiss,
            onStoryClick = { onStepChange(ShareFlowStep.Template) }
        )

        ShareFlowStep.Closed,
        ShareFlowStep.Template,
        ShareFlowStep.Rendering -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharePlatformBottomSheet(
    onDismissRequest: () -> Unit,
    onInstagramClick: () -> Unit,
    onOtherShareClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        ShareOptionList(
            options = listOf(
                "Instagram" to onInstagramClick,
                "기타 공유" to onOtherShareClick
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstagramDestinationBottomSheet(
    onDismissRequest: () -> Unit,
    onStoryClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        ShareOptionList(
            options = listOf("스토리로 공유" to onStoryClick)
        )
    }
}

@Composable
private fun ShareOptionList(
    options: List<Pair<String, () -> Unit>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        options.forEach { (label, onClick) ->
            AppText(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextBody,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

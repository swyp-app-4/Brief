package com.swyp.brife.feature.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextTitle

enum class ShareFlowStep {
    Closed,
    Platform,
    InstagramDestination,
    Template,
    Rendering
}

enum class InstagramShareTemplate {
    Dark,
    Light,
    ImageHeader
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
    val context = LocalContext.current
    var selectedTemplate by remember(data.newsId) {
        mutableStateOf<InstagramShareTemplate?>(null)
    }

    LaunchedEffect(step, data.newsId) {
        if (step == ShareFlowStep.Platform) selectedTemplate = null
    }

    LaunchedEffect(step, data.newsId, selectedTemplate) {
        val template = selectedTemplate
        if (step == ShareFlowStep.Rendering && template != null) {
            captureTransparentComposableContent(
                context = context,
                width = 320.dp,
                height = 460.dp,
                onResult = { result ->
                    result.fold(
                        onSuccess = { bitmap ->
                            shareInstagramStorySticker(context, bitmap).fold(
                                onSuccess = { onDismiss() },
                                onFailure = {
                                    Toast.makeText(
                                        context,
                                        "Instagram 스토리를 열 수 없습니다. 기타 공유를 이용해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onStepChange(ShareFlowStep.Platform)
                                }
                            )
                        },
                        onFailure = {
                            Toast.makeText(
                                context,
                                "공유 이미지를 만들 수 없습니다. 기타 공유를 이용해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            onStepChange(ShareFlowStep.Platform)
                        }
                    )
                }
            ) {
                BrifeTheme {
                    InstagramShareCard(
                        template = template,
                        data = data,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

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

        ShareFlowStep.Template -> ShareTemplateBottomSheet(
            shareData = data,
            selectedTemplate = selectedTemplate,
            onTemplateSelected = { selectedTemplate = it },
            onDismissRequest = {
                selectedTemplate = null
                onStepChange(ShareFlowStep.InstagramDestination)
            },
            onNextClick = {
                if (selectedTemplate != null) onStepChange(ShareFlowStep.Rendering)
            }
        )

        ShareFlowStep.Closed,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareTemplateBottomSheet(
    shareData: InstagramShareData,
    selectedTemplate: InstagramShareTemplate?,
    onTemplateSelected: (InstagramShareTemplate) -> Unit,
    onDismissRequest: () -> Unit,
    onNextClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 24.dp, bottom = 20.dp)
        ) {
            AppText(
                text = "공유 카드 선택",
                style = MaterialTheme.typography.titleMedium,
                color = TextTitle,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppText(
                text = "인스타그램 스토리에 공유할 디자인을 선택해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
            ) {
                items(InstagramShareTemplate.entries) { template ->
                    ShareTemplatePreview(
                        template = template,
                        data = shareData,
                        isSelected = selectedTemplate == template,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "다음",
                onClick = onNextClick,
                enabled = selectedTemplate != null,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun ShareTemplatePreview(
    template: InstagramShareTemplate,
    data: InstagramShareData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(196.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(282.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) PrimaryNormal else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(4.dp)
                .clickable(onClick = onClick)
        ) {
            InstagramShareCard(
                template = template,
                data = data,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppText(
            text = template.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) PrimaryNormal else TextBody,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InstagramShareCard(
    template: InstagramShareTemplate,
    data: InstagramShareData,
    modifier: Modifier = Modifier
) {
    val cardModifier = modifier
        .height(460.dp)
        .padding(8.dp)

    when (template) {
        InstagramShareTemplate.Dark -> DarkInstagramShareCard(data, cardModifier)
        InstagramShareTemplate.Light -> LightInstagramShareCard(data, cardModifier)
        InstagramShareTemplate.ImageHeader -> ImageHeaderInstagramShareCard(data, cardModifier)
    }
}

@Composable
private fun DarkInstagramShareCard(data: InstagramShareData, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF292A2D))
            .border(1.6.dp, Color(0xFF1F1F1F))
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(PrimaryNormal)
        )
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            ShareTitleAndDate(
                data = data,
                titleColor = Color.White,
                dateColor = Color(0xFF70737C)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )
            Spacer(modifier = Modifier.height(18.dp))
            ShareSummarySection(
                summaryPoints = data.summaryPoints,
                titleColor = Color.White,
                pointColor = Color(0xFF989BA2),
                bulletColor = Color(0xFF989BA2)
            )
        }
    }
}

@Composable
private fun LightInstagramShareCard(data: InstagramShareData, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 26.dp)
    ) {
        ShareTitleAndDate(
            data = data,
            titleColor = Color(0xFF212225),
            dateColor = Color(0xFF70737C)
        )
        Spacer(modifier = Modifier.height(24.dp))
        ShareSummarySection(
            summaryPoints = data.summaryPoints,
            titleColor = Color(0xFF171719),
            pointColor = TextBody,
            bulletColor = PrimaryNormal
        )
    }
}

@Composable
private fun ImageHeaderInstagramShareCard(data: InstagramShareData, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Color(0xFFE8ECF2))
        ) {
            data.imageRes?.let { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                ShareTitleAndDate(
                    data = data,
                    titleColor = Color.White,
                    dateColor = Color(0xFFCDCED7)
                )
            }
        }
        ShareSummarySection(
            summaryPoints = data.summaryPoints,
            titleColor = Color(0xFF171719),
            pointColor = TextBody,
            bulletColor = PrimaryNormal,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)
        )
    }
}

@Composable
private fun ShareTitleAndDate(
    data: InstagramShareData,
    titleColor: Color,
    dateColor: Color
) {
    AppText(
        text = data.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = titleColor,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
    if (data.publishedDate.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        AppText(
            text = data.publishedDate,
            style = MaterialTheme.typography.bodySmall,
            color = dateColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShareSummarySection(
    summaryPoints: List<String>,
    titleColor: Color,
    pointColor: Color,
    bulletColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.brife_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            AppText(
                text = "간편요약",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ShareSummaryList(
            summaryPoints = summaryPoints,
            textColor = pointColor,
            bulletColor = bulletColor,
            maxLines = 2
        )
    }
}

@Composable
private fun ShareSummaryList(
    summaryPoints: List<String>,
    textColor: Color,
    bulletColor: Color,
    maxLines: Int
) {
    summaryPoints.take(4).forEach { summary ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
        ) {
            AppText(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = bulletColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            AppText(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private val InstagramShareTemplate.displayName: String
    get() = when (this) {
        InstagramShareTemplate.Dark -> "Dark"
        InstagramShareTemplate.Light -> "Light"
        InstagramShareTemplate.ImageHeader -> "ImageHeader"
    }

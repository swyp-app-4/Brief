package com.swyp.brife.feature.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextTitle

private data class ProfileImageOption(
    @DrawableRes val imageRes: Int,
    val categoryLabel: String
)

private val profileImageOptions = listOf(
    ProfileImageOption(R.drawable.img_home_art, "문화 · 예술"),
    ProfileImageOption(R.drawable.img_home_economy, "경제 · 재테크"),
    ProfileImageOption(R.drawable.img_home_entertainment, "엔터 · 스포츠"),
    ProfileImageOption(R.drawable.img_home_life, "라이프 · 성장"),
    ProfileImageOption(R.drawable.img_home_tech, "IT · 테크"),
    ProfileImageOption(R.drawable.img_home_politics, "시사 · 정치")
)

@Composable
fun ProfileEditScreen(
    username: String,
    @DrawableRes selectedImageRes: Int = R.drawable.img_profile_avatar,
    isGuestPreview: Boolean = false,
    onBackClick: () -> Unit = {},
    onSaveClick: (Int) -> Unit = {}
) {
    var currentSelectedImageRes by remember(selectedImageRes) {
        mutableStateOf(selectedImageRes.toHomeCharacterRes())
    }
    val selectedCategory = profileImageOptions
        .firstOrNull { it.imageRes == currentSelectedImageRes }
        ?.categoryLabel
        .orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF1F6F8), Color(0xFFD8ECFF))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.43f)
        ) {
            ProfileEditTopBar(
                onBackClick = onBackClick,
                onSaveClick = { onSaveClick(currentSelectedImageRes) },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 112.dp)
            ) {
                if (selectedCategory.isNotBlank()) {
                    AppText(
                        text = selectedCategory,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = PrimaryNormal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFC6E2FF))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                AppText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = PrimaryNormal)) {
                            append(username)
                        }
                        withStyle(SpanStyle(color = Color(0xFF212225))) {
                            append("님의 캐릭터")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextTitle
                )
            }

            Image(
                painter = painterResource(id = currentSelectedImageRes),
                contentDescription = "현재 선택한 프로필 캐릭터",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .size(164.dp)
            )
        }

        ProfileCharacterPanel(
            currentSelectedImageRes = currentSelectedImageRes,
            enabled = !isGuestPreview,
            onImageSelected = { currentSelectedImageRes = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.57f)
        )
    }
}

@Composable
private fun ProfileEditTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        }

        AppText(
            text = "프로필 설정",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextTitle,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .width(48.dp)
                .clickable(onClick = onSaveClick),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = "저장",
                style = MaterialTheme.typography.bodySmall,
                color = TextTitle
            )
        }
    }
}

@Composable
private fun ProfileCharacterPanel(
    @DrawableRes currentSelectedImageRes: Int,
    enabled: Boolean,
    onImageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(top = 24.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = "다른 여우 탐색하기",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF212225)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_pencil),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AppText(
                text = "내 취향을 대신할 여우를 선택해보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF70737C)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(profileImageOptions) { option ->
                ProfileImageGridItem(
                    imageRes = option.imageRes,
                    isSelected = option.imageRes == currentSelectedImageRes,
                    enabled = enabled,
                    onClick = { onImageSelected(option.imageRes) }
                )
            }
        }
    }
}

@Composable
private fun ProfileImageGridItem(
    @DrawableRes imageRes: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFFEDF5FF) else Color(0xFFF4F6F7)
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, PrimaryNormal, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "프로필 선택 캐릭터",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.18f
                    scaleY = 1.18f
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
    }
}

@DrawableRes
private fun Int.toHomeCharacterRes(): Int = when (this) {
    R.drawable.img_profile_avatar -> R.drawable.img_home_art
    R.drawable.img_profile_economy -> R.drawable.img_home_economy
    R.drawable.img_profile_entertainment -> R.drawable.img_home_entertainment
    R.drawable.img_profile_life -> R.drawable.img_home_life
    R.drawable.img_profile_tech -> R.drawable.img_home_tech
    R.drawable.img_profile_politics -> R.drawable.img_home_politics
    else -> this
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileEditScreenPreview() {
    BrifeTheme {
        ProfileEditScreen(
            username = "브리프",
            selectedImageRes = R.drawable.img_home_economy
        )
    }
}

package com.example.brife.feature.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BorderStrong
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.theme.TextTitle

private data class ProfileImageOption(
    @DrawableRes val imageRes: Int
)

private val profileImageOptions = listOf(
    ProfileImageOption(R.drawable.img_profile_avatar),
    ProfileImageOption(R.drawable.img_profile_economy),
    ProfileImageOption(R.drawable.img_profile_entertainment),
    ProfileImageOption(R.drawable.img_profile_life),
    ProfileImageOption(R.drawable.img_profile_tech),
    ProfileImageOption(R.drawable.img_profile_politics)
)

@Composable
fun ProfileEditScreen(
    username: String,
    @DrawableRes selectedImageRes: Int = R.drawable.img_profile_avatar,
    onBackClick: () -> Unit = {},
    onSaveClick: (Int) -> Unit = {}
) {
    var currentSelectedImageRes by remember(selectedImageRes) {
        mutableStateOf(selectedImageRes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ProfileEditTopBar(
            onBackClick = onBackClick,
            onSaveClick = { onSaveClick(currentSelectedImageRes) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(28.dp))

            Image(
                painter = painterResource(id = currentSelectedImageRes),
                contentDescription = "현재 선택한 프로필 이미지",
                modifier = Modifier.size(164.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            AppText(
                text = "${username}님\n프로필 이미지를 골라주세요",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextTitle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                userScrollEnabled = false,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(profileImageOptions) { option ->
                    ProfileImageGridItem(
                        imageRes = option.imageRes,
                        isSelected = option.imageRes == currentSelectedImageRes,
                        onClick = { currentSelectedImageRes = option.imageRes }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileEditTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
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
private fun ProfileImageGridItem(
    @DrawableRes imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryNormal else BorderStrong,
                shape = RoundedCornerShape(20.dp)
            )
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "프로필 선택 이미지",
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(CircleShape)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileEditScreenPreview() {
    BrifeTheme {
        ProfileEditScreen(
            username = "브리프",
            selectedImageRes = R.drawable.img_profile_economy
        )
    }
}


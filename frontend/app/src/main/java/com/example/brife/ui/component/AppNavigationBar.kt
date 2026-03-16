package com.example.brife.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R

@Composable
fun AppNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {

        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (selectedIndex == 0) R.drawable.ic_homeafter else R.drawable.ic_homebefore
                    ),
                    contentDescription = "홈",
                    tint = Color.Unspecified, // 원본 아이콘 색상 유지
                    modifier = Modifier.size(width = 24.dp, height = 45.dp)
                )
            },
            label = null, // 텍스트가 아이콘에 포함되어 있으므로 null
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent // 선택 시 배경 원형 제거
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (selectedIndex == 1) R.drawable.ic_searchafter else R.drawable.ic_seachbefore
                    ),
                    contentDescription = "검색",
                    tint = Color.Unspecified, // 원본 아이콘 색상 유지
                    modifier = Modifier.size(width = 24.dp, height = 45.dp)
                )
            },
            label = null, // 텍스트가 아이콘에 포함되어 있으므로 null
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent // 선택 시 배경 원형 제거
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) },
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (selectedIndex == 2) R.drawable.ic_keepafter else R.drawable.ic_keepbefore
                    ),
                    contentDescription = "보관함",
                    tint = Color.Unspecified, // 원본 아이콘 색상 유지
                    modifier = Modifier.size(width = 30.dp, height = 50.dp)
                )
            },
            label = null, // 텍스트가 아이콘에 포함되어 있으므로 null
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent // 선택 시 배경 원형 제거
            )
        )

        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = { onItemSelected(3) },
            icon = {
                Icon(
                    painter = painterResource(
                        id = if (selectedIndex == 3) R.drawable.ic_profileafter else R.drawable.ic_profilebefore
                    ),
                    contentDescription = "프로필",
                    tint = Color.Unspecified, // 원본 아이콘 색상 유지
                    modifier = Modifier.size(width = 30.dp, height = 50.dp)
                )
            },
            label = null, // 텍스트가 아이콘에 포함되어 있으므로 null
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent // 선택 시 배경 원형 제거
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationBarPreview() {
    AppNavigationBar(
        selectedIndex = 0,
        onItemSelected = {}
    )
}
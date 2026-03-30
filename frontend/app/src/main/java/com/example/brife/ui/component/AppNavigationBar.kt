package com.example.brife.ui.component

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) },
            icon = {
                // TODO: 아이콘 추가 예정
                // Icon(
                //     painter = painterResource(R.drawable.ic_home),
                //     contentDescription = "홈"
                // )
            },
            label = { Text("홈") }
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) },
            icon = {
                // TODO: 아이콘 추가 예정
                // Icon(
                //     painter = painterResource(R.drawable.ic_search),
                //     contentDescription = "검색"
                // )
            },
            label = { Text("검색") }
        )

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) },
            icon = {
                // TODO: 아이콘 추가 예정
                // Icon(
                //     painter = painterResource(R.drawable.ic_archive),
                //     contentDescription = "보관함"
                // )
            },
            label = { Text("보관함") }
        )

        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = { onItemSelected(3) },
            icon = {
                // TODO: 아이콘 추가 예정
                // Icon(
                //     painter = painterResource(R.drawable.ic_settings),
                //     contentDescription = "설정"
                // )
            },
            label = { Text("설정") }
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
package com.example.brife.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onSettingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.brife_logo),
                contentDescription = "Brife Logo",
                modifier = Modifier
//                    .padding(start = 8.dp)
                    .size(width = 60.dp, height = 20.dp) // 로고 크기 조정
            )
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = "Settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),

        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AppTopBarPreview() {
    AppTopBar()
}
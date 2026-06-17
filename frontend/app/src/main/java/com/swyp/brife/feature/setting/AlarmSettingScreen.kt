package com.swyp.brife.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.AppTopBar2
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryStrong
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextTitle

@Composable
fun AlarmSettingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = "알림 설정",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        var dailyNewsEnabled by rememberSaveable { mutableStateOf(false) }
        var morningEnabled by rememberSaveable { mutableStateOf(false) }
        var lunchEnabled by rememberSaveable { mutableStateOf(false) }
        var eveningEnabled by rememberSaveable { mutableStateOf(false) }
        var nightEnabled by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            DailyNewsAlarmItem(
                checked = dailyNewsEnabled,
                onCheckedChange = { dailyNewsEnabled = it }
            )

            AlarmTimeToggleItem(
                label = "오전 뉴스",
                time = "8:00 AM",
                checked = morningEnabled,
                onCheckedChange = { morningEnabled = it }
            )
            AlarmTimeToggleItem(
                label = "점심 뉴스",
                time = "12:00 PM",
                checked = lunchEnabled,
                onCheckedChange = { lunchEnabled = it }
            )
            AlarmTimeToggleItem(
                label = "저녁 뉴스",
                time = "6:00 PM",
                checked = eveningEnabled,
                onCheckedChange = { eveningEnabled = it }
            )
            AlarmTimeToggleItem(
                label = "자기전 뉴스",
                time = "10:00 PM",
                checked = nightEnabled,
                onCheckedChange = { nightEnabled = it }
            )
        }
    }
}

@Composable
private fun DailyNewsAlarmItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppText(
                text = "데일리 뉴스 알림",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextTitle
            )
            AppText(
                text = "관심사 뉴스 알림을 설정하고 받아보세요",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun AlarmTimeToggleItem(
    label: String,
    time: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextTitle
            )
            AppText(
                text = time,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryStrong,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(78.dp)
            )
        }

        AlarmOnOffTextButton(
            checked = checked,
            onClick = { onCheckedChange(!checked) }
        )
    }
}

@Composable
private fun AlarmOnOffTextButton(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppText(
        text = if (checked) " ON " else " Off ",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = if (checked) PrimaryStrong else TextBody,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlarmSettingScreenPreview() {
    BrifeTheme {
        AlarmSettingScreen(onBackClick = {})
    }
}

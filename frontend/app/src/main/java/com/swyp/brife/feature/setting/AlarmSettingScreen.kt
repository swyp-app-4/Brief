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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.swyp.brife.ui.theme.brifeColors

@Composable
fun AlarmSettingScreen(
    onBackClick: () -> Unit,
    dailyNewsEnabled: Boolean,
    time8am: Boolean,
    time12pm: Boolean,
    time6pm: Boolean,
    time10pm: Boolean,
    isLoading: Boolean,
    isSaving: Boolean,
    onDailyNewsChanged: (Boolean) -> Unit,
    onTime8amChanged: (Boolean) -> Unit,
    onTime12pmChanged: (Boolean) -> Unit,
    onTime6pmChanged: (Boolean) -> Unit,
    onTime10pmChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToggleEnabled = !isLoading && !isSaving

    Scaffold(
        containerColor = MaterialTheme.brifeColors.backgroundDefault,
        topBar = {
            AppTopBar2(
                title = "알림 설정",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.brifeColors.backgroundDefault)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            DailyNewsAlarmItem(
                checked = dailyNewsEnabled,
                enabled = isToggleEnabled,
                onCheckedChange = onDailyNewsChanged
            )

            AlarmTimeToggleItem(
                label = "오전 뉴스",
                time = "8:00 AM",
                checked = time8am,
                enabled = isToggleEnabled,
                onCheckedChange = onTime8amChanged
            )
            AlarmTimeToggleItem(
                label = "점심 뉴스",
                time = "12:00 PM",
                checked = time12pm,
                enabled = isToggleEnabled,
                onCheckedChange = onTime12pmChanged
            )
            AlarmTimeToggleItem(
                label = "저녁 뉴스",
                time = "6:00 PM",
                checked = time6pm,
                enabled = isToggleEnabled,
                onCheckedChange = onTime6pmChanged
            )
            AlarmTimeToggleItem(
                label = "자기전 뉴스",
                time = "10:00 PM",
                checked = time10pm,
                enabled = isToggleEnabled,
                onCheckedChange = onTime10pmChanged
            )
        }
    }
}

@Composable
private fun DailyNewsAlarmItem(
    checked: Boolean,
    enabled: Boolean,
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
                color = MaterialTheme.brifeColors.textTitle
            )
            AppText(
                text = "관심사 뉴스 알림을 설정하고 받아보세요",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun AlarmTimeToggleItem(
    label: String,
    time: String,
    checked: Boolean,
    enabled: Boolean,
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
                color = MaterialTheme.brifeColors.textTitle
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
            enabled = enabled,
            onClick = { onCheckedChange(!checked) }
        )
    }
}

@Composable
private fun AlarmOnOffTextButton(
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppText(
        text = if (checked) " ON " else " Off ",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = if (checked) PrimaryStrong else TextBody,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlarmSettingScreenPreview() {
    BrifeTheme {
        AlarmSettingScreen(
            onBackClick = {},
            dailyNewsEnabled = true,
            time8am = true,
            time12pm = true,
            time6pm = false,
            time10pm = false,
            isLoading = false,
            isSaving = false,
            onDailyNewsChanged = {},
            onTime8amChanged = {},
            onTime12pmChanged = {},
            onTime6pmChanged = {},
            onTime10pmChanged = {}
        )
    }
}

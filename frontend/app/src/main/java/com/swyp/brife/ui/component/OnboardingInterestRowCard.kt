package com.swyp.brife.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.theme.ComponentDefault
import com.swyp.brife.ui.theme.DarkBackground
import com.swyp.brife.ui.theme.DarkBlue300
import com.swyp.brife.ui.theme.DarkBlue700
import com.swyp.brife.ui.theme.DarkComponentDefault
import com.swyp.brife.ui.theme.InterestSelectedLight
import com.swyp.brife.ui.theme.PrimaryNormal

@Composable
fun OnboardingInterestRowCard(
    text: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val containerColor = when {
        isDarkTheme && selected -> DarkBlue300
        isDarkTheme -> DarkComponentDefault
        selected -> InterestSelectedLight
        else -> ComponentDefault
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = if (selected) {
            BorderStroke(2.dp, if (isDarkTheme) DarkBlue700 else PrimaryNormal)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            AppText(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

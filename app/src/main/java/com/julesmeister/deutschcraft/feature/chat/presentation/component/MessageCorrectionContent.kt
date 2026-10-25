package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MessageCorrectionContent(
    correction: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    bottom = 9.dp
                )
                .width(72.dp)
                .height(1.dp)
                .background(
                    contentColor.copy(
                        alpha = 0.16f
                    )
                )
        )

        Text(
            text = "✦ Besser",
            style =
                MaterialTheme.typography.labelSmall,
            fontWeight =
                FontWeight.Medium,
            color =
                contentColor.copy(
                    alpha = 0.50f
                ),
            textAlign = TextAlign.End
        )

        Text(
            text = correction,
            modifier = Modifier.padding(
                top = 3.dp
            ),
            style =
                MaterialTheme.typography.bodySmall,
            color =
                contentColor.copy(
                    alpha = 0.75f
                ),
            textAlign = TextAlign.End
        )
    }
}
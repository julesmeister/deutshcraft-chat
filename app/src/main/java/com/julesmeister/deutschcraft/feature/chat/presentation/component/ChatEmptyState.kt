package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun ChatEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DeutschLime),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                color = DeutschTextOnLime,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Beginne eine Unterhaltung",
            color = DeutschTextPrimary,
            style =
                MaterialTheme.typography
                    .titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text =
                "Schreibe eine Nachricht auf Deutsch. " +
                        "DeutschCraft antwortet, schlägt " +
                        "passende Antworten vor und verfolgt " +
                        "deinen Fortschritt.",
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .bodyMedium
        )
    }
}
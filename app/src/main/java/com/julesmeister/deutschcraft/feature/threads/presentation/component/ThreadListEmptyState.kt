package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@Composable
fun ThreadListLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator(
            color = DeutschLime,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun ThreadListEmptyState(
    onNewThreadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    DeutschLime.copy(
                        alpha = 0.14f
                    )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(34.dp),
                imageVector =
                    Icons.Outlined.Forum,
                contentDescription = null,
                tint = DeutschLime
            )
        }

        Text(
            modifier = Modifier.padding(
                top = 18.dp
            ),
            text = "Noch keine Unterhaltungen",
            color = DeutschTextPrimary,
            style =
                MaterialTheme.typography
                    .titleLarge,
            fontWeight =
                FontWeight.SemiBold
        )

        TextButton(
            modifier = Modifier.padding(
                top = 8.dp
            ),
            onClick = onNewThreadClick
        ) {
            Text(
                text = "Unterhaltung beginnen",
                color = DeutschLime
            )
        }
    }
}
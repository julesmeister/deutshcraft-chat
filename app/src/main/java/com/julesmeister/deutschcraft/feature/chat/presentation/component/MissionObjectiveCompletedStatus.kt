package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun MissionObjectiveCompletedStatus(
    eventId: String,
    objectiveTitle: String,
    modifier: Modifier = Modifier
) {
    val checkScale =
        remember(
            eventId
        ) {
            Animatable(
                initialValue = 0.35f
            )
        }

    val checkRotation =
        remember(
            eventId
        ) {
            Animatable(
                initialValue = -18f
            )
        }

    LaunchedEffect(eventId) {
        checkScale.snapTo(
            targetValue = 0.35f
        )

        checkRotation.snapTo(
            targetValue = -18f
        )

        checkRotation.animateTo(
            targetValue = 0f,
            animationSpec =
                tween(
                    durationMillis = 300,
                    easing =
                        FastOutSlowInEasing
                )
        )

        checkScale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = 0.58f,
                    stiffness = 520f
                )
        )
    }

    Row(
        modifier =
            modifier,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    scaleX =
                        checkScale.value

                    scaleY =
                        checkScale.value

                    rotationZ =
                        checkRotation.value
                }
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = "✓",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )
        }

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Column {
            Text(
                text =
                    "Ziel erreicht",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    objectiveTitle,
                modifier =
                    Modifier.padding(
                        top = 1.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}
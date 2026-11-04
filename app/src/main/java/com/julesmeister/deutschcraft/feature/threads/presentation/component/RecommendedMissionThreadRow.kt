package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschLimeDark
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
fun RecommendedMissionThreadRow(
    recommendation:
    MissionThreadRecommendation,
    isStarting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (isPressed && !isStarting) {
                PRESSED_SCALE
            } else {
                NORMAL_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.76f,
                stiffness =
                    650f
            ),
        label =
            "RecommendedMissionThreadScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX =
                    scale

                scaleY =
                    scale
            }
            .background(
                color =
                    DeutschLime
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                enabled =
                    !isStarting,
                onClick =
                    onClick
            )
            .padding(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        DeutschTextOnLime.copy(
                            alpha = 0.12f
                        )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            if (isStarting) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(22.dp),
                    color =
                        DeutschTextOnLime,
                    strokeWidth =
                        2.dp
                )
            } else {
                Text(
                    text =
                        recommendation
                            .opportunity
                            .mission
                            .icon,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 12.dp
                )
        ) {
            Text(
                text =
                    recommendation.title,
                color =
                    DeutschTextOnLime,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    recommendation.previewText,
                modifier =
                    Modifier.padding(
                        top = 4.dp
                    ),
                color =
                    DeutschTextOnLime.copy(
                        alpha = 0.74f
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Text(
            text =
                recommendation
                    .opportunity
                    .mission
                    .difficulty
                    .name,
            color =
                DeutschLimeDark,
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            fontWeight =
                FontWeight.Bold
        )
    }
}

private const val NORMAL_SCALE =
    1f

private const val PRESSED_SCALE =
    0.985f
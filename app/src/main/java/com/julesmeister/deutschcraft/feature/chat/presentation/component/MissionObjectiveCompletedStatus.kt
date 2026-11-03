// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/component/MissionObjectiveCompletedStatus.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
internal fun MissionObjectiveCompletedStatus(
    eventId: String,
    objectiveTitle: String,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val isLightTheme =
        colorScheme.background.luminance() >=
                LIGHT_THEME_LUMINANCE_THRESHOLD

    val completionAccent =
        if (isLightTheme) {
            colorScheme.primary
        } else {
            DeutschLime
        }

    val completionContainer =
        if (isLightTheme) {
            colorScheme.primaryContainer
        } else {
            DeutschLime.copy(
                alpha =
                    COMPLETION_ICON_BACKGROUND_ALPHA
            )
        }

    val completionIconColor =
        if (isLightTheme) {
            colorScheme.onPrimaryContainer
        } else {
            DeutschLime
        }

    var targetScale by
    remember(
        eventId
    ) {
        mutableFloatStateOf(
            COMPLETION_INITIAL_SCALE
        )
    }

    val animatedScale by
    animateFloatAsState(
        targetValue =
            targetScale,

        animationSpec =
            tween(
                durationMillis =
                    COMPLETION_SCALE_DURATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "MissionObjectiveCompletionScale"
    )

    LaunchedEffect(
        eventId
    ) {
        targetScale =
            COMPLETION_FINAL_SCALE
    }

    Row(
        modifier =
            modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        COMPLETION_ICON_SIZE
                    )
                    .graphicsLayer {
                        scaleX =
                            animatedScale

                        scaleY =
                            animatedScale
                    }
                    .clip(
                        RoundedCornerShape(
                            COMPLETION_ICON_CORNER_RADIUS
                        )
                    )
                    .background(
                        completionContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "✓",

                color =
                    completionIconColor,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.ExtraBold,

                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    COMPLETION_CONTENT_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    "Ziel erreicht",

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    completionAccent,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Start,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    objectiveTitle,

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    colorScheme.onSurfaceVariant,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                textAlign =
                    TextAlign.Start,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

private val COMPLETION_ICON_SIZE =
    32.dp

private val COMPLETION_ICON_CORNER_RADIUS =
    12.dp

private val COMPLETION_CONTENT_SPACING =
    8.dp

private const val COMPLETION_ICON_BACKGROUND_ALPHA =
    0.16f

private const val COMPLETION_INITIAL_SCALE =
    0.72f

private const val COMPLETION_FINAL_SCALE =
    1f

private const val COMPLETION_SCALE_DURATION_MILLIS =
    320

private const val LIGHT_THEME_LUMINANCE_THRESHOLD =
    0.5f
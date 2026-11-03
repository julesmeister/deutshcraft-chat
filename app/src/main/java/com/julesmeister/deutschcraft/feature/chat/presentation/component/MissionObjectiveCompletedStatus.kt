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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
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
                ChatDesignTokens.DarkLuminanceThreshold

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
                    ChatDesignTokens.ObjectiveCompletion.CompletionIconBackgroundAlpha
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
            ChatDesignTokens.ObjectiveCompletion.CompletionInitialScale
        )
    }

    val animatedScale by
    animateFloatAsState(
        targetValue =
            targetScale,

        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens.ObjectiveCompletion.CompletionScaleDurationMillis,

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
            ChatDesignTokens.ObjectiveCompletion.CompletionFinalScale
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
                        ChatDesignTokens.ObjectiveCompletion.CompletionIconSize
                    )
                    .graphicsLayer {
                        scaleX =
                            animatedScale

                        scaleY =
                            animatedScale
                    }
                    .clip(
                        RoundedCornerShape(
                            ChatDesignTokens.ObjectiveCompletion.CompletionIconCornerRadius
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
                    ChatDesignTokens.ObjectiveCompletion.CompletionContentSpacing
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

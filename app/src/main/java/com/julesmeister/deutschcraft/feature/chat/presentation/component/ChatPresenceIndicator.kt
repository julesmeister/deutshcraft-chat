package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
internal fun ChatPresenceIndicator(
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val isLightTheme =
        colors.background.luminance() >
                ChatDesignTokens.DarkLuminanceThreshold

    val onlineColor =
        if (isLightTheme) {
            ChatDesignTokens.PresenceIndicator.LightModeOnlineGreen
        } else {
            DeutschLime
        }

    val pulseColor =
        if (isLightTheme) {
            ChatDesignTokens.PresenceIndicator.LightModeOnlineHalo
        } else {
            DeutschLime
        }

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "EmilOnlinePulse"
        )

    val pulseProgress by
    infiniteTransition.animateFloat(
        initialValue =
            0f,

        targetValue =
            1f,

        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            ChatDesignTokens.PresenceIndicator.OnlinePulseDurationMillis,

                        easing =
                            FastOutSlowInEasing
                    ),

                repeatMode =
                    RepeatMode.Restart
            ),

        label =
            "EmilOnlinePulseProgress"
    )

    val haloRadiusProgress =
        ChatDesignTokens.PresenceIndicator.OnlineHaloStartScale +
                (
                        ChatDesignTokens.PresenceIndicator.OnlineHaloEndScale -
                                ChatDesignTokens.PresenceIndicator.OnlineHaloStartScale
                        ) *
                pulseProgress

    val haloAlpha =
        (
                ChatDesignTokens.PresenceIndicator.OnlineHaloStartAlpha *
                        (
                                1f -
                                        pulseProgress
                                )
                )
            .coerceIn(
                minimumValue =
                    0f,

                maximumValue =
                    ChatDesignTokens.PresenceIndicator.OnlineHaloStartAlpha
            )

    Box(
        modifier =
            modifier.size(
                ChatDesignTokens.PresenceIndicator.OnlineIndicatorContainerSize
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier =
                Modifier.size(
                    ChatDesignTokens.PresenceIndicator.OnlineIndicatorContainerSize
                )
        ) {
            val center =
                Offset(
                    x =
                        size.width /
                                2f,

                    y =
                        size.height /
                                2f
                )

            val coreRadius =
                ChatDesignTokens.PresenceIndicator.OnlineDotSize
                    .toPx() /
                        2f

            val maximumHaloRadius =
                ChatDesignTokens.PresenceIndicator.OnlineHaloMaxSize
                    .toPx() /
                        2f

            val currentHaloRadius =
                coreRadius +
                        (
                                maximumHaloRadius -
                                        coreRadius
                                ) *
                        haloRadiusProgress

            drawCircle(
                color =
                    pulseColor.copy(
                        alpha =
                            haloAlpha
                    ),

                radius =
                    currentHaloRadius,

                center =
                    center
            )

            drawCircle(
                color =
                    onlineColor.copy(
                        alpha =
                            ChatDesignTokens.PresenceIndicator.OnlineCoreGlowAlpha
                    ),

                radius =
                    coreRadius *
                            ChatDesignTokens.PresenceIndicator.OnlineCoreGlowScale,

                center =
                    center
            )

            drawCircle(
                color =
                    onlineColor,

                radius =
                    coreRadius,

                center =
                    center
            )

            drawCircle(
                color =
                    colors.background.copy(
                        alpha =
                            ChatDesignTokens.PresenceIndicator.OnlineCoreOutlineAlpha
                    ),

                radius =
                    coreRadius,

                center =
                    center,

                style =
                    androidx.compose.ui.graphics.drawscope.Stroke(
                        width =
                            ChatDesignTokens.PresenceIndicator.OnlineCoreOutlineWidth
                                .toPx()
                    )
            )

            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            ChatDesignTokens.PresenceIndicator.OnlineHighlightAlpha
                    ),

                radius =
                    coreRadius *
                            ChatDesignTokens.PresenceIndicator.OnlineHighlightRadiusFactor,

                center =
                    Offset(
                        x =
                            center.x -
                                    coreRadius *
                                    ChatDesignTokens.PresenceIndicator.OnlineHighlightOffsetFactor,

                        y =
                            center.y -
                                    coreRadius *
                                    ChatDesignTokens.PresenceIndicator.OnlineHighlightOffsetFactor
                    )
            )
        }
    }
}

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
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
internal fun ChatPresenceIndicator(
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val isLightTheme =
        colors.background.luminance() >
                LIGHT_THEME_LUMINANCE_THRESHOLD

    val onlineColor =
        if (isLightTheme) {
            LIGHT_MODE_ONLINE_GREEN
        } else {
            DeutschLime
        }

    val pulseColor =
        if (isLightTheme) {
            LIGHT_MODE_ONLINE_HALO
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
                            ONLINE_PULSE_DURATION_MILLIS,

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
        ONLINE_HALO_START_SCALE +
                (
                        ONLINE_HALO_END_SCALE -
                                ONLINE_HALO_START_SCALE
                        ) *
                pulseProgress

    val haloAlpha =
        (
                ONLINE_HALO_START_ALPHA *
                        (
                                1f -
                                        pulseProgress
                                )
                )
            .coerceIn(
                minimumValue =
                    0f,

                maximumValue =
                    ONLINE_HALO_START_ALPHA
            )

    Box(
        modifier =
            modifier.size(
                ONLINE_INDICATOR_CONTAINER_SIZE
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier =
                Modifier.size(
                    ONLINE_INDICATOR_CONTAINER_SIZE
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
                ONLINE_DOT_SIZE
                    .toPx() /
                        2f

            val maximumHaloRadius =
                ONLINE_HALO_MAX_SIZE
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
                            ONLINE_CORE_GLOW_ALPHA
                    ),

                radius =
                    coreRadius *
                            ONLINE_CORE_GLOW_SCALE,

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
                            ONLINE_CORE_OUTLINE_ALPHA
                    ),

                radius =
                    coreRadius,

                center =
                    center,

                style =
                    androidx.compose.ui.graphics.drawscope.Stroke(
                        width =
                            ONLINE_CORE_OUTLINE_WIDTH
                                .toPx()
                    )
            )

            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            ONLINE_HIGHLIGHT_ALPHA
                    ),

                radius =
                    coreRadius *
                            ONLINE_HIGHLIGHT_RADIUS_FACTOR,

                center =
                    Offset(
                        x =
                            center.x -
                                    coreRadius *
                                    ONLINE_HIGHLIGHT_OFFSET_FACTOR,

                        y =
                            center.y -
                                    coreRadius *
                                    ONLINE_HIGHLIGHT_OFFSET_FACTOR
                    )
            )
        }
    }
}

private val ONLINE_INDICATOR_CONTAINER_SIZE =
    18.dp

private val ONLINE_DOT_SIZE =
    7.dp

private val ONLINE_HALO_MAX_SIZE =
    17.dp

private val ONLINE_CORE_OUTLINE_WIDTH =
    0.65.dp

private val LIGHT_MODE_ONLINE_GREEN =
    Color(
        0xFF2A9D55
    )

private val LIGHT_MODE_ONLINE_HALO =
    Color(
        0xFF35AA61
    )

private const val LIGHT_THEME_LUMINANCE_THRESHOLD =
    0.5f

private const val ONLINE_PULSE_DURATION_MILLIS =
    1_800

private const val ONLINE_HALO_START_SCALE =
    0f

private const val ONLINE_HALO_END_SCALE =
    1f

private const val ONLINE_HALO_START_ALPHA =
    0.32f

private const val ONLINE_CORE_GLOW_ALPHA =
    0.16f

private const val ONLINE_CORE_GLOW_SCALE =
    1.45f

private const val ONLINE_CORE_OUTLINE_ALPHA =
    0.34f

private const val ONLINE_HIGHLIGHT_ALPHA =
    0.42f

private const val ONLINE_HIGHLIGHT_RADIUS_FACTOR =
    0.23f

private const val ONLINE_HIGHLIGHT_OFFSET_FACTOR =
    0.27f
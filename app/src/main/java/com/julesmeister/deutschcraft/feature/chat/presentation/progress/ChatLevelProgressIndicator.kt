// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressIndicator.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import kotlin.math.abs
import kotlin.math.pow
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
fun ChatLevelProgressIndicator(
    state: ChatLevelProgressUiState,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by
    remember {
        mutableStateOf(false)
    }

    val colors =
        MaterialTheme.colorScheme

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val targetProgress =
        state.safeReadinessPercentage / 100f

    val currentTargetProgress by
    rememberUpdatedState(targetProgress)

    val currentReduceAnimations by
    rememberUpdatedState(state.reduceAnimations)

    val animatedProgress = remember { Animatable(0f) }
    val ringRotation = remember { Animatable(0f) }
    val indicatorScale = remember { Animatable(ChatDesignTokens.LevelProgressIndicator.IndicatorInitialScale) }
    val coinRotationY = remember { Animatable(0f) }
    val centerColorBlend = remember { Animatable(0f) }
    val shimmerPhase = remember { Animatable(0f) }

    LaunchedEffect(
        targetProgress,
        state.reduceAnimations
    ) {
        if (state.reduceAnimations) {
            animatedProgress.snapTo(targetProgress)
            ringRotation.snapTo(0f)
            indicatorScale.snapTo(1f)
            coinRotationY.snapTo(0f)
            centerColorBlend.snapTo(ChatDesignTokens.LevelProgressIndicator.StaticCenterColorBlend)
            shimmerPhase.snapTo(0f)

            return@LaunchedEffect
        }

        animatedProgress.snapTo(0f)
        ringRotation.snapTo(0f)
        indicatorScale.snapTo(ChatDesignTokens.LevelProgressIndicator.IndicatorInitialScale)
        coinRotationY.snapTo(ChatDesignTokens.LevelProgressIndicator.InitialCoinRotationY)
        centerColorBlend.snapTo(0f)
        shimmerPhase.snapTo(0f)

        joinAll(
            launch {
                animatedProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec =
                        tween(
                            durationMillis = ChatDesignTokens.LevelProgressIndicator.InitialProgressAnimationMillis,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                ringRotation.animateTo(
                    targetValue = ChatDesignTokens.LevelProgressIndicator.FullRotationDegrees,
                    animationSpec =
                        tween(
                            durationMillis = ChatDesignTokens.LevelProgressIndicator.InitialProgressAnimationMillis,
                            easing = FastOutSlowInEasing
                        )
                )

                ringRotation.snapTo(0f)
            },
            launch {
                indicatorScale.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = ChatDesignTokens.LevelProgressIndicator.IndicatorEntranceAnimationMillis,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                coinRotationY.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        tween(
                            durationMillis = ChatDesignTokens.LevelProgressIndicator.InitialCoinAnimationMillis,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                centerColorBlend.animateTo(
                    targetValue = ChatDesignTokens.LevelProgressIndicator.RestingCenterColorBlend,
                    animationSpec =
                        tween(
                            durationMillis = ChatDesignTokens.LevelProgressIndicator.InitialCenterColorAnimationMillis,
                            easing = FastOutSlowInEasing
                        )
                )
            }
        )

        while (true) {
            delay(ChatDesignTokens.LevelProgressIndicator.ReplayIntervalMillis)

            if (currentReduceAnimations) {
                animatedProgress.snapTo(currentTargetProgress)
                continue
            }

            val replayTarget =
                currentTargetProgress

            animatedProgress.snapTo(
                (replayTarget - ChatDesignTokens.LevelProgressIndicator.ReplayProgressDistance)
                    .coerceAtLeast(0f)
            )
            ringRotation.snapTo(0f)
            indicatorScale.snapTo(ChatDesignTokens.LevelProgressIndicator.ReplayInitialScale)
            coinRotationY.snapTo(0f)
            centerColorBlend.snapTo(ChatDesignTokens.LevelProgressIndicator.RestingCenterColorBlend)
            shimmerPhase.snapTo(0f)

            joinAll(
                launch {
                    animatedProgress.animateTo(
                        targetValue = replayTarget,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.ReplayAnimationMillis,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    ringRotation.animateTo(
                        targetValue = ChatDesignTokens.LevelProgressIndicator.FullRotationDegrees,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.ReplayAnimationMillis,
                                easing = FastOutSlowInEasing
                            )
                    )

                    ringRotation.snapTo(0f)
                },
                launch {
                    indicatorScale.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.ReplayScaleAnimationMillis,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    coinRotationY.animateTo(
                        targetValue = ChatDesignTokens.LevelProgressIndicator.FullCoinRotationY,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.CoinFlipAnimationMillis,
                                easing = FastOutSlowInEasing
                            )
                    )

                    coinRotationY.snapTo(0f)
                },
                launch {
                    centerColorBlend.animateTo(
                        targetValue = ChatDesignTokens.LevelProgressIndicator.ActiveCenterColorBlend,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.CenterColorBloomMillis,
                                easing = FastOutSlowInEasing
                            )
                    )

                    centerColorBlend.animateTo(
                        targetValue = ChatDesignTokens.LevelProgressIndicator.RestingCenterColorBlend,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.CenterColorSettleMillis,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    shimmerPhase.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = ChatDesignTokens.LevelProgressIndicator.CoinShimmerAnimationMillis,
                                easing = LinearEasing
                            )
                    )

                    shimmerPhase.snapTo(0f)
                }
            )
        }
    }

    /*
     * The visual is shared between the real top-bar button and
     * the copy drawn inside the dropdown cradle, so both always
     * show the same animation frame.
     */
    val indicatorVisual: @Composable () -> Unit = {
        val ringColor =
            progressColor(animatedProgress.value)

        val centerTintTarget =
            lerp(
                start = ringColor,
                stop = colors.surfaceContainerHighest,
                fraction = ChatDesignTokens.LevelProgressIndicator.CenterTintSurfaceMix
            )

        val centerColor =
            lerp(
                start = colors.surfaceContainerHigh,
                stop = centerTintTarget,
                fraction = centerColorBlend.value.coerceIn(0f, 1f)
            )

        val centerContentColor =
            if (
                centerColor.relativeLuminance() >=
                ChatDesignTokens.LevelProgressIndicator.LightCenterLuminanceThreshold
            ) {
                colors.onSurface
            } else {
                Color.White
            }

        val normalizedCoinAngle =
            (
                    coinRotationY.value % ChatDesignTokens.LevelProgressIndicator.FullCoinRotationY +
                            ChatDesignTokens.LevelProgressIndicator.FullCoinRotationY
                    ) % ChatDesignTokens.LevelProgressIndicator.FullCoinRotationY

        val isBackFace =
            normalizedCoinAngle in
                    ChatDesignTokens.LevelProgressIndicator.CoinBackFaceStartDegrees..ChatDesignTokens.LevelProgressIndicator.CoinBackFaceEndDegrees

        val trackColor =
            colors.outlineVariant.copy(alpha = ChatDesignTokens.LevelProgressIndicator.IndicatorTrackAlpha)

        Box(
            modifier =
                Modifier
                    .size(ChatDesignTokens.LevelProgressIndicator.IndicatorSize)
                    .graphicsLayer {
                        scaleX = indicatorScale.value
                        scaleY = indicatorScale.value
                    },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth =
                    ChatDesignTokens.LevelProgressIndicator.IndicatorStrokeWidth.toPx()

                val inset =
                    strokeWidth / 2f

                val arcSize =
                    Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    )

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = ChatDesignTokens.LevelProgressIndicator.FullRotationDegrees,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                if (animatedProgress.value > 0f) {
                    rotate(
                        degrees = ringRotation.value,
                        pivot = center
                    ) {
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle =
                                ChatDesignTokens.LevelProgressIndicator.FullRotationDegrees * animatedProgress.value,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style =
                                Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                        )
                    }
                }
            }

            Box(
                modifier =
                    Modifier
                        .size(ChatDesignTokens.LevelProgressIndicator.IndicatorCenterSize)
                        .graphicsLayer {
                            rotationY = coinRotationY.value
                            cameraDistance = ChatDesignTokens.LevelProgressIndicator.CoinCameraDistance
                            scaleX = if (isBackFace) -1f else 1f
                        }
                        .clip(CircleShape)
                        .background(centerColor),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val shimmerVisibility =
                        (
                                1f -
                                        abs(shimmerPhase.value - ChatDesignTokens.LevelProgressIndicator.ShimmerMidpoint) * 2f
                                ).coerceIn(0f, 1f)

                    val shimmerCenter =
                        size.width *
                                (
                                        shimmerPhase.value * ChatDesignTokens.LevelProgressIndicator.ShimmerTravelDistance -
                                                ChatDesignTokens.LevelProgressIndicator.ShimmerStartOffset
                                        )

                    drawCircle(
                        color =
                            Color.White.copy(
                                alpha = ChatDesignTokens.LevelProgressIndicator.ShimmerMaxAlpha * shimmerVisibility
                            ),
                        radius = size.width * ChatDesignTokens.LevelProgressIndicator.ShimmerRadiusFactor,
                        center =
                            Offset(
                                x = shimmerCenter,
                                y = size.height * ChatDesignTokens.LevelProgressIndicator.ShimmerVerticalPosition
                            )
                    )
                }

                Text(
                    text = state.levelLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = centerContentColor,
                    maxLines = 1
                )
            }
        }
    }

    Box(
        modifier =
            modifier.size(ChatDesignTokens.LevelProgressIndicator.IndicatorTouchSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier =
                Modifier
                    .size(ChatDesignTokens.LevelProgressIndicator.IndicatorTouchSize)
                    .semantics {
                        contentDescription = state.accessibilityDescription
                        role = Role.Button
                    }
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        isDropdownExpanded = !isDropdownExpanded
                    },
            contentAlignment = Alignment.Center
        ) {
            indicatorVisual()
        }

        ChatLevelProgressDropdown(
            state = state,
            expanded = isDropdownExpanded,
            onDismiss = {
                isDropdownExpanded = false
            },
            anchorContent = indicatorVisual
        )
    }
}

private fun progressColor(
    progress: Float
): Color {
    val safeProgress =
        progress.coerceIn(0f, 1f)

    return if (safeProgress <= ChatDesignTokens.LevelProgress.ColorMidpoint) {
        lerp(
            start = ChatDesignTokens.LevelProgress.Orange,
            stop = ChatDesignTokens.LevelProgress.Amber,
            fraction = safeProgress / ChatDesignTokens.LevelProgress.ColorMidpoint
        )
    } else {
        lerp(
            start = ChatDesignTokens.LevelProgress.Amber,
            stop = ChatDesignTokens.LevelProgress.Green,
            fraction = (safeProgress - ChatDesignTokens.LevelProgress.ColorMidpoint) / ChatDesignTokens.LevelProgress.ColorMidpoint
        )
    }
}

private fun Color.relativeLuminance(): Float {
    fun linearizedChannel(
        value: Float
    ): Float {
        return if (value <= 0.03928f) {
            value / 12.92f
        } else {
            ((value + 0.055f) / 1.055f)
                .toDouble()
                .pow(2.4)
                .toFloat()
        }
    }

    return (
            0.2126f * linearizedChannel(red) +
                    0.7152f * linearizedChannel(green) +
                    0.0722f * linearizedChannel(blue)
            ).coerceIn(0f, 1f)
}

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

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
    val indicatorScale = remember { Animatable(INDICATOR_INITIAL_SCALE) }
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
            centerColorBlend.snapTo(STATIC_CENTER_COLOR_BLEND)
            shimmerPhase.snapTo(0f)

            return@LaunchedEffect
        }

        animatedProgress.snapTo(0f)
        ringRotation.snapTo(0f)
        indicatorScale.snapTo(INDICATOR_INITIAL_SCALE)
        coinRotationY.snapTo(INITIAL_COIN_ROTATION_Y)
        centerColorBlend.snapTo(0f)
        shimmerPhase.snapTo(0f)

        joinAll(
            launch {
                animatedProgress.animateTo(
                    targetValue = targetProgress,
                    animationSpec =
                        tween(
                            durationMillis = INITIAL_PROGRESS_ANIMATION_MILLIS,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                ringRotation.animateTo(
                    targetValue = FULL_ROTATION_DEGREES,
                    animationSpec =
                        tween(
                            durationMillis = INITIAL_PROGRESS_ANIMATION_MILLIS,
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
                            durationMillis = INDICATOR_ENTRANCE_ANIMATION_MILLIS,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                coinRotationY.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        tween(
                            durationMillis = INITIAL_COIN_ANIMATION_MILLIS,
                            easing = FastOutSlowInEasing
                        )
                )
            },
            launch {
                centerColorBlend.animateTo(
                    targetValue = RESTING_CENTER_COLOR_BLEND,
                    animationSpec =
                        tween(
                            durationMillis = INITIAL_CENTER_COLOR_ANIMATION_MILLIS,
                            easing = FastOutSlowInEasing
                        )
                )
            }
        )

        while (true) {
            delay(REPLAY_INTERVAL_MILLIS)

            if (currentReduceAnimations) {
                animatedProgress.snapTo(currentTargetProgress)
                continue
            }

            val replayTarget =
                currentTargetProgress

            animatedProgress.snapTo(
                (replayTarget - REPLAY_PROGRESS_DISTANCE)
                    .coerceAtLeast(0f)
            )
            ringRotation.snapTo(0f)
            indicatorScale.snapTo(REPLAY_INITIAL_SCALE)
            coinRotationY.snapTo(0f)
            centerColorBlend.snapTo(RESTING_CENTER_COLOR_BLEND)
            shimmerPhase.snapTo(0f)

            joinAll(
                launch {
                    animatedProgress.animateTo(
                        targetValue = replayTarget,
                        animationSpec =
                            tween(
                                durationMillis = REPLAY_ANIMATION_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    ringRotation.animateTo(
                        targetValue = FULL_ROTATION_DEGREES,
                        animationSpec =
                            tween(
                                durationMillis = REPLAY_ANIMATION_MILLIS,
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
                                durationMillis = REPLAY_SCALE_ANIMATION_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    coinRotationY.animateTo(
                        targetValue = FULL_COIN_ROTATION_Y,
                        animationSpec =
                            tween(
                                durationMillis = COIN_FLIP_ANIMATION_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                    )

                    coinRotationY.snapTo(0f)
                },
                launch {
                    centerColorBlend.animateTo(
                        targetValue = ACTIVE_CENTER_COLOR_BLEND,
                        animationSpec =
                            tween(
                                durationMillis = CENTER_COLOR_BLOOM_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                    )

                    centerColorBlend.animateTo(
                        targetValue = RESTING_CENTER_COLOR_BLEND,
                        animationSpec =
                            tween(
                                durationMillis = CENTER_COLOR_SETTLE_MILLIS,
                                easing = FastOutSlowInEasing
                            )
                    )
                },
                launch {
                    shimmerPhase.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = COIN_SHIMMER_ANIMATION_MILLIS,
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
                fraction = CENTER_TINT_SURFACE_MIX
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
                LIGHT_CENTER_LUMINANCE_THRESHOLD
            ) {
                colors.onSurface
            } else {
                Color.White
            }

        val normalizedCoinAngle =
            (
                    coinRotationY.value % FULL_COIN_ROTATION_Y +
                            FULL_COIN_ROTATION_Y
                    ) % FULL_COIN_ROTATION_Y

        val isBackFace =
            normalizedCoinAngle in
                    COIN_BACK_FACE_START_DEGREES..COIN_BACK_FACE_END_DEGREES

        val trackColor =
            colors.outlineVariant.copy(alpha = INDICATOR_TRACK_ALPHA)

        Box(
            modifier =
                Modifier
                    .size(INDICATOR_SIZE)
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
                    INDICATOR_STROKE_WIDTH.toPx()

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
                    sweepAngle = FULL_ROTATION_DEGREES,
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
                                FULL_ROTATION_DEGREES * animatedProgress.value,
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
                        .size(INDICATOR_CENTER_SIZE)
                        .graphicsLayer {
                            rotationY = coinRotationY.value
                            cameraDistance = COIN_CAMERA_DISTANCE
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
                                        abs(shimmerPhase.value - SHIMMER_MIDPOINT) * 2f
                                ).coerceIn(0f, 1f)

                    val shimmerCenter =
                        size.width *
                                (
                                        shimmerPhase.value * SHIMMER_TRAVEL_DISTANCE -
                                                SHIMMER_START_OFFSET
                                        )

                    drawCircle(
                        color =
                            Color.White.copy(
                                alpha = SHIMMER_MAX_ALPHA * shimmerVisibility
                            ),
                        radius = size.width * SHIMMER_RADIUS_FACTOR,
                        center =
                            Offset(
                                x = shimmerCenter,
                                y = size.height * SHIMMER_VERTICAL_POSITION
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
            modifier.size(INDICATOR_TOUCH_SIZE),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier =
                Modifier
                    .size(INDICATOR_TOUCH_SIZE)
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

    return if (safeProgress <= COLOR_MIDPOINT) {
        lerp(
            start = CHAT_PROGRESS_ORANGE,
            stop = CHAT_PROGRESS_AMBER,
            fraction = safeProgress / COLOR_MIDPOINT
        )
    } else {
        lerp(
            start = CHAT_PROGRESS_AMBER,
            stop = CHAT_PROGRESS_GREEN,
            fraction = (safeProgress - COLOR_MIDPOINT) / COLOR_MIDPOINT
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

internal val INDICATOR_TOUCH_SIZE = 48.dp

private val INDICATOR_SIZE = 40.dp
private val INDICATOR_CENTER_SIZE = 31.dp
private val INDICATOR_STROKE_WIDTH = 3.dp

private val CHAT_PROGRESS_ORANGE = Color(0xFFE87516)
private val CHAT_PROGRESS_AMBER = Color(0xFFE6B522)
private val CHAT_PROGRESS_GREEN = Color(0xFF2E9B57)

private const val COLOR_MIDPOINT = 0.5f
private const val FULL_ROTATION_DEGREES = 360f
private const val FULL_COIN_ROTATION_Y = 360f
private const val INITIAL_COIN_ROTATION_Y = -90f
private const val COIN_BACK_FACE_START_DEGREES = 90f
private const val COIN_BACK_FACE_END_DEGREES = 270f
private const val COIN_CAMERA_DISTANCE = 14f
private const val INDICATOR_TRACK_ALPHA = 0.55f
private const val INDICATOR_INITIAL_SCALE = 0.76f
private const val REPLAY_INITIAL_SCALE = 0.92f
private const val REPLAY_PROGRESS_DISTANCE = 0.12f
private const val STATIC_CENTER_COLOR_BLEND = 0.34f
private const val RESTING_CENTER_COLOR_BLEND = 0.23f
private const val ACTIVE_CENTER_COLOR_BLEND = 0.78f
private const val CENTER_TINT_SURFACE_MIX = 0.18f
private const val LIGHT_CENTER_LUMINANCE_THRESHOLD = 0.48f
private const val SHIMMER_TRAVEL_DISTANCE = 1.8f
private const val SHIMMER_START_OFFSET = 0.4f
private const val SHIMMER_RADIUS_FACTOR = 0.32f
private const val SHIMMER_VERTICAL_POSITION = 0.34f
private const val SHIMMER_MAX_ALPHA = 0.20f
private const val SHIMMER_MIDPOINT = 0.5f
private const val INDICATOR_ENTRANCE_ANIMATION_MILLIS = 500
private const val INITIAL_PROGRESS_ANIMATION_MILLIS = 1_300
private const val INITIAL_COIN_ANIMATION_MILLIS = 760
private const val INITIAL_CENTER_COLOR_ANIMATION_MILLIS = 1_100
private const val REPLAY_ANIMATION_MILLIS = 1_200
private const val REPLAY_SCALE_ANIMATION_MILLIS = 520
private const val COIN_FLIP_ANIMATION_MILLIS = 1_050
private const val CENTER_COLOR_BLOOM_MILLIS = 650
private const val CENTER_COLOR_SETTLE_MILLIS = 850
private const val COIN_SHIMMER_ANIMATION_MILLIS = 1_050
private const val REPLAY_INTERVAL_MILLIS = 10_000L
package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * Adds a subtle left-swipe interaction to the existing
 * recommended-mission thread row.
 *
 * Swipe left:
 * Request another mission after the row has returned to its
 * normal position.
 *
 * Tap:
 * Start the displayed mission.
 *
 * Swipe right:
 * No action.
 *
 * Important UX behavior:
 *
 * - The row does not disappear while the next recommendation
 *   is generated.
 * - The loading spinner begins only after the row is centered.
 * - The next recommendation replaces the old content with a
 *   subtle horizontal content transition.
 */
@Composable
fun SwipeableRecommendedMissionThreadRow(
    recommendation: MissionThreadRecommendation,
    isLoadingAnother: Boolean,
    isStarting: Boolean,
    onShowAnotherMission: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density =
        LocalDensity.current

    val hapticFeedback =
        LocalHapticFeedback.current

    val coroutineScope =
        rememberCoroutineScope()

    val currentShowAnotherMission by
    rememberUpdatedState(
        newValue =
            onShowAnotherMission
    )

    val currentStartMission by
    rememberUpdatedState(
        newValue =
            onStartMission
    )

    val swipeThresholdPx =
        with(density) {
            SWIPE_THRESHOLD.toPx()
        }

    val maximumLeftDragPx =
        with(density) {
            MAXIMUM_LEFT_DRAG.toPx()
        }

    val maximumRightDragPx =
        with(density) {
            MAXIMUM_RIGHT_DRAG.toPx()
        }

    /*
     * Animates the complete row during a gesture.
     */
    val animatedOffset =
        remember {
            Animatable(
                initialValue =
                    0f
            )
        }

    /*
     * Immediate finger-driven offset.
     */
    var dragOffset by
    remember {
        mutableFloatStateOf(
            0f
        )
    }

    /*
     * True while an accepted swipe is first returning to the
     * centered position.
     *
     * The recommendation request is not started until this
     * becomes false.
     */
    var isSettlingBeforeRefresh by
    remember {
        mutableStateOf(
            false
        )
    }

    var thresholdHapticPerformed by
    remember {
        mutableStateOf(
            false
        )
    }

    /*
     * Prevents overlapping return animations.
     */
    var returnAnimationJob by
    remember {
        mutableStateOf<Job?>(
            null
        )
    }

    val interactionsBlocked =
        isLoadingAnother ||
                isStarting ||
                isSettlingBeforeRefresh

    val displayedOffset =
        if (
            animatedOffset.isRunning ||
            isSettlingBeforeRefresh
        ) {
            animatedOffset.value
        } else {
            dragOffset
        }

    /*
     * Keep the row visually stable during loading.
     *
     * Do not reduce its scale or move it sideways. The
     * RecommendedMissionThreadRow already displays its
     * spinner through isStarting.
     */
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX =
                    displayedOffset
            }
            .pointerInput(
                recommendation,
                interactionsBlocked
            ) {
                if (interactionsBlocked) {
                    return@pointerInput
                }

                detectHorizontalDragGestures(
                    onDragStart = {
                        returnAnimationJob
                            ?.cancel()

                        returnAnimationJob =
                            null

                        thresholdHapticPerformed =
                            false
                    },
                    onHorizontalDrag = {
                            change,
                            dragAmount ->

                        change.consume()

                        dragOffset =
                            (
                                    dragOffset +
                                            dragAmount
                                    )
                                .coerceIn(
                                    minimumValue =
                                        -maximumLeftDragPx,
                                    maximumValue =
                                        maximumRightDragPx
                                )

                        val crossedLeftThreshold =
                            dragOffset <=
                                    -swipeThresholdPx

                        if (
                            crossedLeftThreshold &&
                            !thresholdHapticPerformed
                        ) {
                            hapticFeedback
                                .performHapticFeedback(
                                    HapticFeedbackType
                                        .LongPress
                                )

                            thresholdHapticPerformed =
                                true
                        } else if (
                            !crossedLeftThreshold
                        ) {
                            thresholdHapticPerformed =
                                false
                        }
                    },
                    onDragCancel = {
                        val currentOffset =
                            dragOffset

                        dragOffset =
                            0f

                        thresholdHapticPerformed =
                            false

                        returnAnimationJob =
                            animateBackToCenter(
                                coroutineScope =
                                    coroutineScope,
                                animatedOffset =
                                    animatedOffset,
                                currentOffset =
                                    currentOffset
                            )
                    },
                    onDragEnd = {
                        val currentOffset =
                            dragOffset

                        val acceptedLeftSwipe =
                            currentOffset <=
                                    -swipeThresholdPx

                        dragOffset =
                            0f

                        thresholdHapticPerformed =
                            false

                        if (acceptedLeftSwipe) {
                            /*
                             * First return the existing row
                             * completely to the center.
                             *
                             * Only after the animation finishes
                             * do we start loading another
                             * recommendation.
                             */
                            isSettlingBeforeRefresh =
                                true

                            returnAnimationJob =
                                coroutineScope.launch {
                                    animatedOffset.snapTo(
                                        targetValue =
                                            currentOffset
                                    )

                                    animatedOffset.animateTo(
                                        targetValue =
                                            0f,
                                        animationSpec =
                                            tween(
                                                durationMillis =
                                                    ACCEPTED_SWIPE_RETURN_DURATION_MILLIS,
                                                easing =
                                                    FastOutSlowInEasing
                                            )
                                    )

                                    /*
                                     * The row is now exactly at
                                     * x = 0.
                                     *
                                     * The loading state and its
                                     * spinner may safely begin.
                                     */
                                    isSettlingBeforeRefresh =
                                        false

                                    currentShowAnotherMission()
                                }
                        } else {
                            returnAnimationJob =
                                animateBackToCenter(
                                    coroutineScope =
                                        coroutineScope,
                                    animatedOffset =
                                        animatedOffset,
                                    currentOffset =
                                        currentOffset
                                )
                        }
                    }
                )
            }
    ) {
        /*
         * Only the recommendation content transitions.
         *
         * The thread-cell container remains fixed in place,
         * preventing the blank gap that appeared when the
         * entire cell previously left the viewport.
         */
        AnimatedContent(
            targetState =
                recommendation,
            modifier =
                Modifier.fillMaxWidth(),
            transitionSpec = {
                val enter =
                    slideInHorizontally(
                        initialOffsetX = { width ->
                            width /
                                    INCOMING_OFFSET_DIVISOR
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    RECOMMENDATION_CHANGE_DURATION_MILLIS,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ) +
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            RECOMMENDATION_CHANGE_DURATION_MILLIS
                                    )
                            )

                val exit =
                    slideOutHorizontally(
                        targetOffsetX = { width ->
                            -width /
                                    OUTGOING_OFFSET_DIVISOR
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    RECOMMENDATION_CHANGE_DURATION_MILLIS,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ) +
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            RECOMMENDATION_CHANGE_DURATION_MILLIS
                                    )
                            )

                enter togetherWith exit
            },
            contentKey = { item ->
                /*
                 * The full value distinguishes recommendations
                 * even if Qwen happens to reuse a title.
                 */
                item.hashCode()
            },
            label =
                "MissionRecommendationChange"
        ) { displayedRecommendation ->
            RecommendedMissionThreadRow(
                recommendation =
                    displayedRecommendation,
                isStarting =
                    isStarting ||
                            isLoadingAnother,
                onClick = {
                    if (!interactionsBlocked) {
                        currentStartMission()
                    }
                }
            )
        }
    }
}

/*
 * Returns the full row to x = 0.
 *
 * Animatable operations are suspend functions, so they run
 * inside the remembered Compose coroutine scope.
 */
private fun animateBackToCenter(
    coroutineScope: CoroutineScope,
    animatedOffset:
    Animatable<Float, AnimationVector1D>,
    currentOffset: Float
): Job {
    return coroutineScope.launch {
        animatedOffset.snapTo(
            targetValue =
                currentOffset
        )

        animatedOffset.animateTo(
            targetValue =
                0f,
            animationSpec =
                tween(
                    durationMillis =
                        CANCELLED_SWIPE_RETURN_DURATION_MILLIS,
                    easing =
                        FastOutSlowInEasing
                )
        )
    }
}

private val SWIPE_THRESHOLD =
    84.dp

private val MAXIMUM_LEFT_DRAG =
    260.dp

/*
 * Right movement is limited because it has no action.
 */
private val MAXIMUM_RIGHT_DRAG =
    24.dp

/*
 * Lower values create a larger horizontal content movement.
 *
 * The current values produce a subtle transition rather than
 * making the recommendation look like a separate page.
 */
private const val INCOMING_OFFSET_DIVISOR =
    12

private const val OUTGOING_OFFSET_DIVISOR =
    12

private const val ACCEPTED_SWIPE_RETURN_DURATION_MILLIS =
    170

private const val CANCELLED_SWIPE_RETURN_DURATION_MILLIS =
    220

private const val RECOMMENDATION_CHANGE_DURATION_MILLIS =
    260
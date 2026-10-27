package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.AdaptiveLearningFeedbackEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ChatTopBar(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier =
        Modifier,
    activeMissionTitle: String? =
        null,
    currentMissionObjective: String? =
        null,
    currentMissionGuidance: String? =
        null,
    missionProgressPercentage: Int =
        0,
    objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? =
        null,
    adaptiveLearningFeedbackEvent:
    AdaptiveLearningFeedbackEvent? =
        null,
    onObjectiveCompletionShown:
        (String) -> Unit =
        {},
    onAdaptiveLearningFeedbackShown:
        (String) -> Unit =
        {},

    /*
     * Called when the learner taps the active mission content.
     *
     * The callback eventually opens MissionGuidanceBottomSheet.
     */
    onMissionGuidanceClick:
        () -> Unit =
        {}
) {
    TopAppBar(
        modifier =
            modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .background,
                scrolledContainerColor =
                    MaterialTheme
                        .colorScheme
                        .background,
                titleContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                navigationIconContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            ),
        navigationIcon = {
            AnimatedChatTopBarIconButton(
                onClick =
                    onBackClick,
                modifier =
                    Modifier.padding(
                        start =
                            TOP_BAR_EDGE_PADDING
                    )
            ) {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Outlined
                            .ArrowBack,
                    contentDescription =
                        "Back"
                )
            }
        },
        title = {
            /*
             * AnimatedChatTopBarTitle owns the decision about
             * whether an active mission is currently visible.
             *
             * Therefore the click callback is passed down
             * instead of making the complete TopAppBar title
             * clickable here.
             *
             * This prevents:
             *
             * Emil ist online
             *
             * from opening an empty mission-guidance sheet.
             */
            AnimatedChatTopBarTitle(
                isAssistantTyping =
                    isAssistantTyping,
                isAssessing =
                    isAssessing,
                activeMissionTitle =
                    activeMissionTitle,
                currentMissionObjective =
                    currentMissionObjective,
                currentMissionGuidance =
                    currentMissionGuidance,
                missionProgressPercentage =
                    missionProgressPercentage,
                objectiveCompletionEvent =
                    objectiveCompletionEvent,
                adaptiveLearningFeedbackEvent =
                    adaptiveLearningFeedbackEvent,
                onObjectiveCompletionShown =
                    onObjectiveCompletionShown,
                onAdaptiveLearningFeedbackShown =
                    onAdaptiveLearningFeedbackShown,
                onMissionGuidanceClick =
                    onMissionGuidanceClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start =
                                TITLE_START_PADDING,
                            end =
                                TITLE_END_PADDING
                        )
            )
        },
        actions = {
            /*
             * Global progress, streak, settings, and thread
             * actions remain in the thread-list top bar.
             *
             * The chat top bar stays focused on:
             *
             * - Emil
             * - active mission
             * - assessment
             * - adaptive-learning feedback
             */
        }
    )
}

@Composable
private fun AnimatedChatTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier =
        Modifier,
    content:
    @Composable () -> Unit
) {
    var isPressed by
    remember {
        mutableStateOf(
            false
        )
    }

    val animatedScale by
    animateFloatAsState(
        targetValue =
            if (isPressed) {
                PRESSED_ICON_SCALE
            } else {
                NORMAL_ICON_SCALE
            },
        animationSpec =
            tween(
                durationMillis =
                    ICON_PRESS_ANIMATION_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ChatTopBarIconPressScale"
    )

    Box(
        modifier =
            modifier
                .size(
                    TOP_BAR_ICON_BUTTON_SIZE
                )
                .graphicsLayer {
                    scaleX =
                        animatedScale

                    scaleY =
                        animatedScale
                }
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        if (isPressed) {
                            MaterialTheme
                                .colorScheme
                                .surfaceContainerHigh
                        } else {
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer
                                .copy(
                                    alpha =
                                        NORMAL_BUTTON_ALPHA
                                )
                        }
                )
                .pointerInput(
                    onClick
                ) {
                    detectTapGestures(
                        onPress = {
                            isPressed =
                                true

                            try {
                                tryAwaitRelease()
                            } finally {
                                isPressed =
                                    false
                            }
                        },
                        onTap = {
                            onClick()
                        }
                    )
                },
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

private val TOP_BAR_ICON_BUTTON_SIZE =
    40.dp

private val TOP_BAR_EDGE_PADDING =
    8.dp

/*
 * Visual separation between the circular back button and
 * changing title content.
 */
private val TITLE_START_PADDING =
    8.dp

/*
 * No right-side chat actions currently exist, so mission
 * information can use most of the remaining width.
 */
private val TITLE_END_PADDING =
    16.dp

private const val NORMAL_ICON_SCALE =
    1f

private const val PRESSED_ICON_SCALE =
    0.9f

private const val NORMAL_BUTTON_ALPHA =
    0.68f

private const val ICON_PRESS_ANIMATION_DURATION_MILLIS =
    130
package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier,
    activeMissionTitle: String? = null,
    currentMissionObjective: String? = null,
    currentMissionGuidance: String? = null,
    missionProgressPercentage: Int = 0,
    objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? = null,
    adaptiveLearningFeedbackEvent:
    AdaptiveLearningFeedbackEvent? = null,
    onObjectiveCompletionShown:
        (String) -> Unit = {},
    onAdaptiveLearningFeedbackShown:
        (String) -> Unit = {},

    /*
     * True briefly after a message is copied.
     *
     * The normal title and subtitle remain visible.
     * The confirmation appears beside them.
     */
    showCopiedConfirmation: Boolean = false,

    onMissionGuidanceClick:
        () -> Unit = {}
) {
    TopAppBar(
        modifier =
            modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme.background,
                scrolledContainerColor =
                    MaterialTheme.colorScheme.background,
                titleContentColor =
                    MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor =
                    MaterialTheme.colorScheme.onBackground
            ),
        navigationIcon = {
            AnimatedChatTopBarIconButton(
                onClick = onBackClick,
                modifier =
                    Modifier.padding(
                        start = TOP_BAR_EDGE_PADDING
                    )
            ) {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Outlined
                            .ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = TITLE_START_PADDING,
                            end = TITLE_END_PADDING
                        ),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        COPY_CONFIRMATION_SPACING
                    )
            ) {
                /*
                 * Keep the normal title, subtitle, mission,
                 * assessment and adaptive-learning content.
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
                        Modifier.weight(1f)
                )

                /*
                 * Copy confirmation appears beside the
                 * existing title instead of replacing it.
                 */
                AnimatedVisibility(
                    visible =
                        showCopiedConfirmation,
                    enter =
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        COPY_CONFIRMATION_ENTER_MILLIS
                                )
                        ) +
                                expandHorizontally(
                                    animationSpec =
                                        spring(
                                            dampingRatio =
                                                Spring
                                                    .DampingRatioMediumBouncy,
                                            stiffness =
                                                Spring
                                                    .StiffnessMedium
                                        ),
                                    expandFrom =
                                        Alignment.End
                                ),
                    exit =
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        COPY_CONFIRMATION_EXIT_MILLIS
                                )
                        ) +
                                shrinkHorizontally(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                COPY_CONFIRMATION_EXIT_MILLIS
                                        ),
                                    shrinkTowards =
                                        Alignment.End
                                )
                ) {
                    TopBarCopyConfirmation()
                }
            }
        },
        actions = {}
    )
}

@Composable
private fun TopBarCopyConfirmation() {
    var animationStarted by
    remember {
        mutableStateOf(false)
    }

    val scale by
    animateFloatAsState(
        targetValue =
            if (animationStarted) {
                1f
            } else {
                COPY_CONFIRMATION_INITIAL_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    Spring.DampingRatioMediumBouncy,
                stiffness =
                    Spring.StiffnessMedium
            ),
        label =
            "TopBarCopyConfirmationScale"
    )

    val rotation by
    animateFloatAsState(
        targetValue =
            if (animationStarted) {
                0f
            } else {
                COPY_CONFIRMATION_INITIAL_ROTATION
            },
        animationSpec =
            spring(
                dampingRatio = 0.58f,
                stiffness = 440f
            ),
        label =
            "TopBarCopyConfirmationRotation"
    )

    LaunchedEffect(Unit) {
        animationStarted =
            true
    }

    Row(
        modifier =
            Modifier
                .graphicsLayer {
                    scaleX =
                        scale

                    scaleY =
                        scale

                    rotationZ =
                        rotation
                }
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
                )
                .padding(
                    start = 5.dp,
                    end = 9.dp,
                    top = 4.dp,
                    bottom = 4.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        COPY_CONFIRMATION_ICON_CONTAINER_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color.White
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Check,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        COPY_CONFIRMATION_CHECK_SIZE
                    ),
                tint =
                    Color.Black
            )
        }

        Text(
            text =
                "Copied",
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )
    }
}

@Composable
private fun AnimatedChatTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content:
    @Composable () -> Unit
) {
    var isPressed by
    remember {
        mutableStateOf(false)
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

private val TITLE_START_PADDING =
    8.dp

private val TITLE_END_PADDING =
    12.dp

private val COPY_CONFIRMATION_SPACING =
    6.dp

private val COPY_CONFIRMATION_ICON_CONTAINER_SIZE =
    22.dp

private val COPY_CONFIRMATION_CHECK_SIZE =
    13.dp

private const val NORMAL_ICON_SCALE =
    1f

private const val PRESSED_ICON_SCALE =
    0.9f

private const val NORMAL_BUTTON_ALPHA =
    0.68f

private const val ICON_PRESS_ANIMATION_DURATION_MILLIS =
    130

private const val COPY_CONFIRMATION_INITIAL_SCALE =
    0.55f

private const val COPY_CONFIRMATION_INITIAL_ROTATION =
    -10f

private const val COPY_CONFIRMATION_ENTER_MILLIS =
    160

private const val COPY_CONFIRMATION_EXIT_MILLIS =
    170
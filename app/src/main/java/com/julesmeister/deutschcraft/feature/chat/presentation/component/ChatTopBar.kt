package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    streakDays: Int,
    onBackClick: () -> Unit,
    onProgressClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearConversationClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeMissionTitle: String? = null,
    currentMissionObjective: String? = null,
    missionProgressPercentage: Int = 0,
    objectiveCompletionEvent:
    MissionObjectiveCompletionEvent? = null,
    onObjectiveCompletionShown:
        (String) -> Unit = {}
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        modifier =
            modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults
                .topAppBarColors(
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
                            .onBackground,
                    actionIconContentColor =
                        MaterialTheme
                            .colorScheme
                            .onBackground
                ),
        navigationIcon = {
            /*
             * Keeps the back button away from the physical
             * left edge of the screen.
             */
            AnimatedTopBarIconButton(
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
                        Icons.AutoMirrored
                            .Outlined
                            .ArrowBack,
                    contentDescription =
                        "Back"
                )
            }
        },
        title = {
            /*
             * TopAppBar aligns its title beside the
             * navigation button rather than centering it
             * across the full screen.
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
                missionProgressPercentage =
                    missionProgressPercentage,
                objectiveCompletionEvent =
                    objectiveCompletionEvent,
                onObjectiveCompletionShown =
                    onObjectiveCompletionShown,
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
            ProgressStreakButton(
                streakDays =
                    streakDays,
                onClick =
                    onProgressClick
            )

            /*
             * Explicit space prevents the progress pill
             * and menu button from appearing connected.
             */
            Spacer(
                modifier =
                    Modifier.width(
                        ACTION_SPACING
                    )
            )

            Box(
                modifier =
                    Modifier.padding(
                        end =
                            TOP_BAR_EDGE_PADDING
                    )
            ) {
                AnimatedTopBarIconButton(
                    onClick = {
                        isMenuExpanded = true
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.MoreVert,
                        contentDescription =
                            "More options"
                    )
                }

                DropdownMenu(
                    expanded =
                        isMenuExpanded,
                    onDismissRequest = {
                        isMenuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    "New conversation"
                            )
                        },
                        onClick = {
                            isMenuExpanded = false

                            onNewConversationClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    "Clear conversation"
                            )
                        },
                        onClick = {
                            isMenuExpanded = false

                            onClearConversationClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    "Settings"
                            )
                        },
                        onClick = {
                            isMenuExpanded = false

                            onSettingsClick()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun AnimatedTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember {
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
            "TopBarIconPressScale"
    )

    Box(
        modifier = modifier
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
                        isPressed = true

                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
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

/*
 * Left and right breathing room from the screen edges.
 */
private val TOP_BAR_EDGE_PADDING =
    8.dp

/*
 * Small visual gap between the back button and the
 * animated title area.
 */
private val TITLE_START_PADDING =
    8.dp

/*
 * Prevents long mission titles from crowding the progress
 * control.
 */
private val TITLE_END_PADDING =
    8.dp

/*
 * Separates the progress pill from the three-dot menu.
 */
private val ACTION_SPACING =
    6.dp

private const val NORMAL_ICON_SCALE =
    1f

private const val PRESSED_ICON_SCALE =
    0.9f

private const val NORMAL_BUTTON_ALPHA =
    0.68f

private const val ICON_PRESS_ANIMATION_DURATION_MILLIS =
    130
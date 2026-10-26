package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.animateFloatAsState

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
    missionProgressPercentage: Int = 0
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }

    CenterAlignedTopAppBar(
        modifier =
            modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors(
                    containerColor =
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
            AnimatedTopBarIconButton(
                onClick =
                    onBackClick,
                contentDescription =
                    "Back"
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Outlined
                            .ArrowBack,
                    contentDescription =
                        null
                )
            }
        },
        title = {
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
                    missionProgressPercentage
            )
        },
        actions = {
            ProgressStreakButton(
                streakDays =
                    streakDays,
                onClick =
                    onProgressClick,
                modifier =
                    Modifier.padding(
                        end = 4.dp
                    )
            )

            Box {
                AnimatedTopBarIconButton(
                    onClick = {
                        isMenuExpanded = true
                    },
                    contentDescription =
                        "More options"
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.MoreVert,
                        contentDescription =
                            null
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
    contentDescription: String,
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

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier = Modifier
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true

                        tryAwaitRelease()

                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                enabled =
                    false,
                role =
                    Role.Button,
                onClick = {
                    // Tap is handled by pointerInput.
                }
            ),
        contentAlignment =
            androidx.compose.ui.Alignment.Center
    ) {
        content()
    }
}

private val TOP_BAR_ICON_BUTTON_SIZE =
    40.dp

private const val NORMAL_ICON_SCALE =
    1f

private const val PRESSED_ICON_SCALE =
    0.88f

private const val NORMAL_BUTTON_ALPHA =
    0.68f

private const val ICON_PRESS_ANIMATION_DURATION_MILLIS =
    130
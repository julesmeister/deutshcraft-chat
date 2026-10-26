package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
                    onBackClick
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
                    onObjectiveCompletionShown
            )
        },
        actions = {
            ProgressStreakButton(
                streakDays =
                    streakDays,
                onClick =
                    onProgressClick
            )

            Box {
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
                            Text("New conversation")
                        },
                        onClick = {
                            isMenuExpanded = false
                            onNewConversationClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Clear conversation")
                        },
                        onClick = {
                            isMenuExpanded = false
                            onClearConversationClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Settings")
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
    content: @Composable () -> Unit
) {
    var isPressed by remember {
        mutableStateOf(false)
    }

    val scale by
    animateFloatAsState(
        targetValue =
            if (isPressed) {
                0.9f
            } else {
                1f
            },
        animationSpec =
            tween(
                durationMillis = 130,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "TopBarIconScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                MaterialTheme
                    .colorScheme
                    .surfaceContainer
                    .copy(alpha = 0.68f)
            )
            .pointerInput(Unit) {
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
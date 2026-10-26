package com.julesmeister.deutschcraft.feature.threads.presentation.component

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.component.ProgressStreakButton
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadListTopBar(
    streakDays: Int,
    onProgressClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by
    remember {
        mutableStateOf(false)
    }

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
                    DeutschTextPrimary,
                actionIconContentColor =
                    DeutschTextPrimary
            ),
        title = {
            Text(
                text =
                    "Unterhaltungen",
                color =
                    DeutschTextPrimary,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        },
        actions = {
            /*
             * Progress and streak information is global
             * learning information, so it belongs on the
             * thread-list dashboard instead of inside an
             * individual conversation.
             */
            ProgressStreakButton(
                streakDays =
                    streakDays,
                onClick =
                    onProgressClick
            )

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
                AnimatedThreadTopBarIconButton(
                    onClick = {
                        isMenuExpanded =
                            true
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
                        isMenuExpanded =
                            false
                    },
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    "New conversation"
                            )
                        },
                        onClick = {
                            isMenuExpanded =
                                false

                            onNewConversationClick()
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
                            isMenuExpanded =
                                false

                            onSettingsClick()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun AnimatedThreadTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
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
            "ThreadTopBarIconPressScale"
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
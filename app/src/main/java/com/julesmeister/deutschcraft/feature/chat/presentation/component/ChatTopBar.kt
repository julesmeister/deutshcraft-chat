// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/component/ChatTopBar.kt

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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.chat.presentation.AdaptiveLearningFeedbackEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.progress.ChatLevelProgressIndicator
import com.julesmeister.deutschcraft.feature.chat.presentation.progress.LocalChatLevelProgress
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens

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
    showCopiedConfirmation: Boolean =
        false,
    onMissionGuidanceClick:
        () -> Unit =
        {},
    isMissionExpanded: Boolean = false
) {
    val levelProgress =
        LocalChatLevelProgress.current

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
                        .onBackground,

                actionIconContentColor =
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
                            ChatDesignTokens.TopBar.TopBarEdgePadding
                    )
            ) {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Outlined
                            .ArrowBack,

                    contentDescription =
                        "Back",

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            }
        },

        title = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start =
                                ChatDesignTokens.TopBar.TitleStartPadding,

                            end =
                                ChatDesignTokens.TopBar.TitleEndPadding
                        ),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        ChatDesignTokens.TopBar.CopyConfirmationSpacing
                    )
            ) {
                AnimatedChatTopBarTitle(
                    isMissionExpanded = isMissionExpanded,
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
                        Modifier.weight(
                            1f
                        )
                )

                AnimatedVisibility(
                    visible =
                        showCopiedConfirmation,

                    enter =
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens.TopBar.CopyConfirmationEnterMillis
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
                                        ChatDesignTokens.TopBar.CopyConfirmationExitMillis
                                )
                        ) +
                                shrinkHorizontally(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                ChatDesignTokens.TopBar.CopyConfirmationExitMillis
                                        ),

                                    shrinkTowards =
                                        Alignment.End
                                )
                ) {
                    TopBarCopyConfirmation()
                }
            }
        },

        actions = {
            if (!activeMissionTitle.isNullOrBlank()) {
                IconButton(
                    onClick = onMissionGuidanceClick,
                    modifier = Modifier.size(ChatDesignTokens.TopBar.MissionExpandTouchSize)
                ) {
                    Icon(
                        imageVector = if (isMissionExpanded) Icons.Outlined.KeyboardArrowUp
                            else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (isMissionExpanded) "Collapse mission details"
                            else "Expand mission details",
                        modifier = Modifier.size(ChatDesignTokens.TopBar.MissionExpandIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = ChatDesignTokens.TopBar.MissionExpandIconAlpha
                        )
                    )
                }
            }
            ChatLevelProgressIndicator(
                state =
                    levelProgress,

                modifier =
                    Modifier.padding(
                        end =
                            ChatDesignTokens.TopBar.TopBarEdgePadding
                    )
            )
        }
    )
}

@Composable
private fun TopBarCopyConfirmation() {
    var animationStarted by
    remember {
        mutableStateOf(
            false
        )
    }

    val scale by
    animateFloatAsState(
        targetValue =
            if (animationStarted) {
                1f
            } else {
                ChatDesignTokens.TopBar.CopyConfirmationInitialScale
            },

        animationSpec =
            spring(
                dampingRatio =
                    Spring
                        .DampingRatioMediumBouncy,

                stiffness =
                    Spring
                        .StiffnessMedium
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
                ChatDesignTokens.TopBar.CopyConfirmationInitialRotation
            },

        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.TopBar.CopyConfirmationRotationDamping,

                stiffness =
                    ChatDesignTokens.TopBar.CopyConfirmationRotationStiffness
            ),

        label =
            "TopBarCopyConfirmationRotation"
    )

    LaunchedEffect(
        Unit
    ) {
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
                    RoundedCornerShape(
                        percent =
                            ChatDesignTokens.TopBar.FullCornerPercent
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
                )
                .padding(
                    start =
                        ChatDesignTokens.TopBar.CopyConfirmationStartPadding,

                    end =
                        ChatDesignTokens.TopBar.CopyConfirmationEndPadding,

                    top =
                        ChatDesignTokens.TopBar.CopyConfirmationVerticalPadding,

                    bottom =
                        ChatDesignTokens.TopBar.CopyConfirmationVerticalPadding
                ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                ChatDesignTokens.TopBar.CopyConfirmationContentSpacing
            )
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens.TopBar.CopyConfirmationIconContainerSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .onSurface
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .Check,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        ChatDesignTokens.TopBar.CopyConfirmationCheckSize
                    ),

                tint =
                    MaterialTheme
                        .colorScheme
                        .surface
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
    content: @Composable () -> Unit
) {
    var isPressed by
    remember {
        mutableStateOf(
            false
        )
    }

    val colors =
        MaterialTheme.colorScheme

    val isLightTheme =
        colors.background.luminance() >
                ChatDesignTokens.DarkLuminanceThreshold

    /*
     * Keep the filled circular button, but remove the outline.
     *
     * Light mode uses a stronger filled surface so the button
     * remains clearly separated from the pale top-bar
     * background without needing a border.
     *
     * Dark mode keeps the existing surface behavior.
     */
    val normalContainerColor =
        if (isLightTheme) {
            ChatDesignTokens.TopBar.LightModeBackButtonBackground
        } else {
            colors
                .surfaceContainer
                .copy(
                    alpha =
                        ChatDesignTokens.TopBar.NormalButtonAlpha
                )
        }

    val pressedContainerColor =
        if (isLightTheme) {
            ChatDesignTokens.TopBar.LightModeBackButtonPressedBackground
        } else {
            colors.surfaceContainerHighest
        }

    val animatedScale by
    animateFloatAsState(
        targetValue =
            if (isPressed) {
                ChatDesignTokens.TopBar.PressedIconScale
            } else {
                ChatDesignTokens.TopBar.NormalIconScale
            },

        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens.TopBar.IconPressAnimationDurationMillis,

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
                    ChatDesignTokens.TopBar.TopBarIconButtonSize
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
                            pressedContainerColor
                        } else {
                            normalContainerColor
                        }
                )
                .semantics {
                    role =
                        Role.Button
                }
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

package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import kotlinx.coroutines.delay

@Suppress("DEPRECATION")
@Composable
fun MessageBubble(
    message: ChatMessage,
    correction: String?,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean,
    hasAttachedPhrase: Boolean,
    onClick: () -> Unit,
    onMessageCopied: () -> Unit,

    /*
     * Reports the actual width of the visible source bubble.
     *
     * This does not include the copied indicator or surrounding
     * row spacing.
     */
    onBubbleWidthChanged: (Int) -> Unit,

    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender ==
                MessageSender.USER

    val clipboardManager =
        LocalClipboardManager.current

    var copyEventKey by
    remember(
        message.id
    ) {
        mutableIntStateOf(
            0
        )
    }

    var showCopiedIndicator by
    remember(
        message.id
    ) {
        mutableStateOf(
            false
        )
    }

    val colors =
        rememberMessageBubbleColors(
            isUserMessage =
                isUserMessage,
            isLatestUserMessage =
                isLatestUserMessage,
            isLatestAssistantMessage =
                isLatestAssistantMessage
        )

    val maxBubbleWidth =
        (
                LocalConfiguration
                    .current
                    .screenWidthDp.dp *
                        ChatDesignTokens.MessageBubble.MaximumBubbleWidthFraction
                )
            .coerceAtMost(
                ChatDesignTokens.MessageBubble.MaximumBubbleWidthCap
            )

    LaunchedEffect(
        copyEventKey
    ) {
        if (
            copyEventKey ==
            0
        ) {
            return@LaunchedEffect
        }

        showCopiedIndicator =
            true

        delay(
            ChatDesignTokens.MessageBubble.CopiedIndicatorDurationMillis
        )

        showCopiedIndicator =
            false
    }

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isUserMessage) {
            CopiedIndicator(
                visible =
                    showCopiedIndicator,
                isUserMessage =
                    true,
                animationKey =
                    copyEventKey
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.MessageBubble.CopiedIndicatorSpacing
                    )
            )
        }

        MessageBubbleContainer(
            shape =
                messageBubbleShape(
                    isUserMessage =
                        isUserMessage,
                    hasAttachedPhrase =
                        hasAttachedPhrase
                ),
            backgroundColor =
                colors.backgroundColor,
            borderColor =
                colors.borderColor,
            maxWidth =
                maxBubbleWidth,
            onClick =
                onClick,
            onBubbleWidthChanged =
                onBubbleWidthChanged,
            onLongClick = {
                val textToCopy =
                    message.copyablePrimaryText(
                        correction =
                            correction
                    )

                clipboardManager.setText(
                    AnnotatedString(
                        textToCopy
                    )
                )

                copyEventKey +=
                    1

                onMessageCopied()
            }
        ) {
            MessageBubbleContent(
                message =
                    message,
                correction =
                    correction,
                isUserMessage =
                    isUserMessage,
                contentColor =
                    colors.contentColor
            )
        }

        if (!isUserMessage) {
            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.MessageBubble.CopiedIndicatorSpacing
                    )
            )

            CopiedIndicator(
                visible =
                    showCopiedIndicator,
                isUserMessage =
                    false,
                animationKey =
                    copyEventKey
            )
        }
    }
}

@Composable
private fun CopiedIndicator(
    visible: Boolean,
    isUserMessage: Boolean,
    animationKey: Int
) {
    var animationStarted by
    remember(
        animationKey
    ) {
        mutableStateOf(
            false
        )
    }

    val badgeScale by
    animateFloatAsState(
        targetValue =
            if (
                visible &&
                animationStarted
            ) {
                1f
            } else {
                ChatDesignTokens.MessageBubble.CopiedBadgeInitialScale
            },
        animationSpec =
            spring(
                dampingRatio =
                    Spring
                        .DampingRatioMediumBouncy,
                stiffness =
                    Spring.StiffnessMedium
            ),
        label =
            "CopiedBadgeScale"
    )

    val badgeRotation by
    animateFloatAsState(
        targetValue =
            if (
                visible &&
                animationStarted
            ) {
                0f
            } else if (isUserMessage) {
                ChatDesignTokens.MessageBubble.CopiedBadgeInitialRotation
            } else {
                -ChatDesignTokens.MessageBubble.CopiedBadgeInitialRotation
            },
        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.MessageBubble.CopiedBadgeRotationDamping,
                stiffness =
                    ChatDesignTokens.MessageBubble.CopiedBadgeRotationStiffness
            ),
        label =
            "CopiedBadgeRotation"
    )

    val glowScale by
    animateFloatAsState(
        targetValue =
            if (
                visible &&
                animationStarted
            ) {
                ChatDesignTokens.MessageBubble.CopiedGlowFinalScale
            } else {
                ChatDesignTokens.MessageBubble.CopiedGlowInitialScale
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens.MessageBubble.CopiedGlowDurationMillis
            ),
        label =
            "CopiedBadgeGlowScale"
    )

    val glowAlpha by
    animateFloatAsState(
        targetValue =
            if (
                visible &&
                animationStarted
            ) {
                0f
            } else {
                ChatDesignTokens.MessageBubble.CopiedGlowInitialAlpha
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens.MessageBubble.CopiedGlowDurationMillis
            ),
        label =
            "CopiedBadgeGlowAlpha"
    )

    LaunchedEffect(
        visible,
        animationKey
    ) {
        if (visible) {
            withFrameNanos {
                animationStarted =
                    true
            }
        } else {
            animationStarted =
                false
        }
    }

    AnimatedVisibility(
        visible =
            visible,
        enter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.MessageBubble.CopiedIndicatorEnterMillis
                    )
            ) +
                    slideInHorizontally(
                        animationSpec =
                            spring(
                                dampingRatio =
                                    Spring
                                        .DampingRatioMediumBouncy,
                                stiffness =
                                    Spring
                                        .StiffnessMediumLow
                            ),
                        initialOffsetX = { fullWidth ->
                            if (isUserMessage) {
                                fullWidth
                            } else {
                                -fullWidth
                            }
                        }
                    ),
        exit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.MessageBubble.CopiedIndicatorExitMillis
                    )
            ) +
                    slideOutHorizontally(
                        animationSpec =
                            tween(
                                durationMillis =
                                    ChatDesignTokens.MessageBubble.CopiedIndicatorExitMillis
                            ),
                        targetOffsetX = { fullWidth ->
                            if (isUserMessage) {
                                fullWidth / 2
                            } else {
                                -fullWidth / 2
                            }
                        }
                    )
    ) {
        Box(
            modifier =
                Modifier.size(
                    ChatDesignTokens.MessageBubble.CopiedIndicatorContainerSize
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            ChatDesignTokens.MessageBubble.CopiedBadgeSize
                        )
                        .graphicsLayer {
                            scaleX =
                                glowScale

                            scaleY =
                                glowScale

                            alpha =
                                glowAlpha
                        }
                        .background(
                            color =
                                Color.White,
                            shape =
                                CircleShape
                        )
            )

            Box(
                modifier =
                    Modifier
                        .graphicsLayer {
                            scaleX =
                                badgeScale

                            scaleY =
                                badgeScale

                            rotationZ =
                                badgeRotation
                        }
                        .size(
                            ChatDesignTokens.MessageBubble.CopiedBadgeSize
                        )
                        .shadow(
                            elevation =
                                ChatDesignTokens.MessageBubble.CopiedBadgeElevation,
                            shape =
                                CircleShape,
                            clip =
                                false
                        )
                        .background(
                            color =
                                Color.White,
                            shape =
                                CircleShape
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
                        "Copied",
                    modifier =
                        Modifier.size(
                            ChatDesignTokens.MessageBubble.CopiedCheckSize
                        ),
                    tint =
                        Color.Black
                )
            }
        }
    }
}

private fun ChatMessage.copyablePrimaryText(
    correction: String?
): String {
    val hasVisibleBesserCorrection =
        sender ==
                MessageSender.USER &&
                !correction.isNullOrBlank() &&
                correction.trim() !=
                content.trim()

    return if (hasVisibleBesserCorrection) {
        correction.orEmpty()
    } else {
        content
    }
}

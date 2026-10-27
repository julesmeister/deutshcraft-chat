package com.julesmeister.deutschcraft.feature.besser.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BesserChallengeBubble(
    challenge: BesserChallengeUiModel,
    isUserMessage: Boolean,
    onImprove: () -> Unit,
    onHint: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape =
        if (isUserMessage) {
            RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 0.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth(
                    BESSER_CHALLENGE_WIDTH_FRACTION
                )
                .clip(
                    shape
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
                )
                .animateContentSize()
                .padding(
                    BESSER_CHALLENGE_PADDING
                )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            BESSER_ICON_CONTAINER_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                                .copy(
                                    alpha =
                                        BESSER_ICON_BACKGROUND_ALPHA
                                )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons
                            .Outlined
                            .AutoAwesome,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(
                            BESSER_ICON_SIZE
                        ),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        BESSER_HEADER_SPACING
                    )
            )

            Text(
                text =
                    "Besser Challenge",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }

        Text(
            text =
                challenge.instruction,
            modifier =
                Modifier.padding(
                    top =
                        BESSER_INSTRUCTION_TOP_PADDING
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        challenge.visibleHints.forEach { hint ->
            Text(
                text =
                    hint,
                modifier =
                    Modifier.padding(
                        top =
                            BESSER_HINT_TOP_PADDING
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Row(
            modifier =
                Modifier.padding(
                    top =
                        BESSER_ACTIONS_TOP_PADDING
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    BESSER_ACTION_SPACING
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            BesserChallengeAction(
                text =
                    "Improve",
                icon = {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .Edit,
                        contentDescription =
                            null
                    )
                },
                highlighted =
                    true,
                onClick =
                    onImprove
            )

            AnimatedVisibility(
                visible =
                    challenge.canRevealHint,
                enter =
                    fadeIn() +
                            slideInVertically {
                                it / 3
                            },
                exit =
                    fadeOut()
            ) {
                BesserChallengeAction(
                    text =
                        "Hint",
                    icon = {
                        Icon(
                            imageVector =
                                Icons
                                    .Outlined
                                    .Lightbulb,
                            contentDescription =
                                null
                        )
                    },
                    onClick =
                        onHint
                )
            }

            BesserChallengeAction(
                text =
                    "Skip",
                icon = {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .SkipNext,
                        contentDescription =
                            null
                    )
                },
                onClick =
                    onSkip
            )
        }
    }
}

@Composable
private fun BesserChallengeAction(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    val backgroundColor =
        if (highlighted) {
            MaterialTheme
                .colorScheme
                .primary
        } else {
            MaterialTheme
                .colorScheme
                .surfaceContainerHighest
        }

    val contentColor =
        if (highlighted) {
            MaterialTheme
                .colorScheme
                .onPrimary
        } else {
            MaterialTheme
                .colorScheme
                .onSurface
        }

    Row(
        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        BESSER_ACTION_RADIUS
                    )
                )
                .background(
                    backgroundColor
                )
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        BESSER_ACTION_HORIZONTAL_PADDING,
                    vertical =
                        BESSER_ACTION_VERTICAL_PADDING
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                BESSER_ACTION_ICON_SPACING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                    contentColor
        ) {
            Box(
                modifier =
                    Modifier.size(
                        BESSER_ACTION_ICON_SIZE
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                icon()
            }

            Text(
                text =
                    text,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    if (highlighted) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                color =
                    contentColor
            )
        }
    }
}

private const val BESSER_CHALLENGE_WIDTH_FRACTION =
    0.88f

private const val BESSER_ICON_BACKGROUND_ALPHA =
    0.14f

private val BESSER_CHALLENGE_PADDING =
    14.dp

private val BESSER_ICON_CONTAINER_SIZE =
    30.dp

private val BESSER_ICON_SIZE =
    17.dp

private val BESSER_HEADER_SPACING =
    8.dp

private val BESSER_INSTRUCTION_TOP_PADDING =
    10.dp

private val BESSER_HINT_TOP_PADDING =
    8.dp

private val BESSER_ACTIONS_TOP_PADDING =
    12.dp

private val BESSER_ACTION_SPACING =
    6.dp

private val BESSER_ACTION_RADIUS =
    50.dp

private val BESSER_ACTION_HORIZONTAL_PADDING =
    10.dp

private val BESSER_ACTION_VERTICAL_PADDING =
    7.dp

private val BESSER_ACTION_ICON_SPACING =
    5.dp

private val BESSER_ACTION_ICON_SIZE =
    16.dp
package com.julesmeister.deutschcraft.feature.besser.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSky
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnSky

@Composable
fun BesserChallengeBubble(
    challenge: BesserChallengeUiModel,
    isUserMessage: Boolean,
    sourceBubbleWidthPx: Int,
    onImprove: () -> Unit,
    onHint: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    isLast: Boolean = true
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val accent =
        if (isUserMessage) {
            DeutschLime
        } else {
            DeutschSky
        }

    val onAccent =
        if (isUserMessage) {
            DeutschTextOnLime
        } else {
            DeutschTextOnSky
        }

    var challengeBubbleWidthPx by
    remember(
        challenge.id
    ) {
        mutableIntStateOf(
            0
        )
    }

    val extendsBeyondSource =
        sourceBubbleWidthPx > 0 &&
                challengeBubbleWidthPx >
                sourceBubbleWidthPx +
                WIDTH_COMPARISON_TOLERANCE_PX

    val exposedTopCorner =
        if (extendsBeyondSource) {
            EXPOSED_TOP_CORNER_RADIUS
        } else {
            0.dp
        }

    val bottomCorner =
        if (isLast) {
            BOTTOM_CORNER_RADIUS
        } else {
            0.dp
        }

    /*
     * User challenges share their top-right attachment edge
     * with the user message.
     *
     * If the challenge grows farther left than its source,
     * only the newly exposed top-left corner becomes rounded.
     *
     * The rule is mirrored for assistant-side attachments.
     */
    val shape =
        if (isUserMessage) {
            RoundedCornerShape(
                topStart =
                    exposedTopCorner,
                topEnd =
                    0.dp,
                bottomStart =
                    bottomCorner,
                bottomEnd =
                    bottomCorner
            )
        } else {
            RoundedCornerShape(
                topStart =
                    0.dp,
                topEnd =
                    exposedTopCorner,
                bottomStart =
                    bottomCorner,
                bottomEnd =
                    bottomCorner
            )
        }

    val containerColor =
        accent
            .copy(
                alpha =
                    CONTAINER_TINT_ALPHA
            )
            .compositeOver(
                colorScheme.surfaceContainerHigh
            )

    val maxWidth =
        (
                LocalConfiguration
                    .current
                    .screenWidthDp.dp *
                        MAXIMUM_WIDTH_FRACTION
                )
            .coerceAtMost(
                MAXIMUM_WIDTH
            )

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(
                        max =
                            maxWidth
                    )
                    .onSizeChanged { size ->
                        if (
                            challengeBubbleWidthPx !=
                            size.width
                        ) {
                            challengeBubbleWidthPx =
                                size.width
                        }
                    }
                    .clip(
                        shape
                    )
                    .background(
                        containerColor
                    )
                    .animateContentSize(
                        animationSpec =
                            tween(
                                durationMillis =
                                    CONTENT_SIZE_ANIMATION_MILLIS
                            )
                    )
                    .padding(
                        start =
                            CONTENT_HORIZONTAL_PADDING,
                        end =
                            CONTENT_HORIZONTAL_PADDING,
                        top =
                            CONTENT_TOP_PADDING,
                        bottom =
                            CONTENT_BOTTOM_PADDING
                    )
        ) {
            /*
             * No decorative icon or heading.
             *
             * The instruction is the primary content.
             */
            Text(
                text =
                    challenge.instruction,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
                        .copy(
                            fontSize =
                                INSTRUCTION_FONT_SIZE,
                            lineHeight =
                                INSTRUCTION_LINE_HEIGHT
                        ),
                fontWeight =
                    FontWeight.Medium,
                color =
                    colorScheme.onSurface
            )

            AnimatedVisibility(
                visible =
                    challenge
                        .visibleHints
                        .isNotEmpty(),
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    DETAILS_FADE_IN_MILLIS
                            )
                    ) +
                            expandVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            DETAILS_EXPAND_MILLIS
                                    )
                            ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    DETAILS_FADE_OUT_MILLIS
                            )
                    ) +
                            shrinkVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            DETAILS_SHRINK_MILLIS
                                    )
                            )
            ) {
                Column(
                    modifier =
                        Modifier.padding(
                            top =
                                HINTS_TOP_PADDING
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            HINT_SPACING
                        )
                ) {
                    challenge
                        .visibleHints
                        .forEach { hint ->
                            Row(
                                verticalAlignment =
                                    Alignment.Top
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(
                                                top =
                                                    HINT_DOT_TOP_PADDING
                                            )
                                            .size(
                                                HINT_DOT_SIZE
                                            )
                                            .clip(
                                                CircleShape
                                            )
                                            .background(
                                                accent.copy(
                                                    alpha =
                                                        HINT_DOT_ALPHA
                                                )
                                            )
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            HINT_TEXT_SPACING
                                        )
                                )

                                Text(
                                    text =
                                        hint,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall
                                            .copy(
                                                fontSize =
                                                    HINT_FONT_SIZE,
                                                lineHeight =
                                                    HINT_LINE_HEIGHT
                                            ),
                                    color =
                                        colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                }
            }

            Row(
                modifier =
                    Modifier.padding(
                        top =
                            ACTIONS_TOP_PADDING
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        ACTION_SPACING
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BesserChallengeAction(
                    text =
                        "Verbessern",
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
                    backgroundColor =
                        DeutschLime,
                    contentColor =
                        DeutschTextOnLime,
                    onClick =
                        onImprove
                )

                AnimatedVisibility(
                    visible =
                        challenge.canRevealHint,
                    enter =
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ACTION_VISIBILITY_MILLIS
                                )
                        ),
                    exit =
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ACTION_VISIBILITY_MILLIS
                                )
                        )
                ) {
                    BesserChallengeAction(
                        text =
                            "Tipp",
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
                        backgroundColor =
                            colorScheme
                                .surfaceContainerHighest,
                        contentColor =
                            colorScheme.onSurface,
                        onClick =
                            onHint
                    )
                }

                BesserChallengeAction(
                    text =
                        "Überspringen",
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
                    backgroundColor =
                        Color.Transparent,
                    contentColor =
                        colorScheme
                            .onSurfaceVariant,
                    onClick =
                        onSkip
                )
            }
        }
    }
}

@Composable
private fun BesserChallengeAction(
    text: String,
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .clip(
                    CircleShape
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
                        ACTION_HORIZONTAL_PADDING,
                    vertical =
                        ACTION_VERTICAL_PADDING
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                ACTION_ICON_SPACING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                    contentColor
        ) {
            Box(
                modifier =
                    Modifier.size(
                        ACTION_ICON_SIZE
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
                        .labelSmall
                        .copy(
                            fontSize =
                                ACTION_FONT_SIZE
                        ),
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    contentColor,
                maxLines =
                    1
            )
        }
    }
}

private val BOTTOM_CORNER_RADIUS =
    14.dp

private val EXPOSED_TOP_CORNER_RADIUS =
    14.dp

private const val WIDTH_COMPARISON_TOLERANCE_PX =
    2

private const val MAXIMUM_WIDTH_FRACTION =
    0.72f

private val MAXIMUM_WIDTH =
    380.dp

private const val CONTAINER_TINT_ALPHA =
    0.06f

private val CONTENT_HORIZONTAL_PADDING =
    10.dp

private val CONTENT_TOP_PADDING =
    8.dp

private val CONTENT_BOTTOM_PADDING =
    8.dp

private const val CONTENT_SIZE_ANIMATION_MILLIS =
    220

private val INSTRUCTION_FONT_SIZE =
    12.sp

private val INSTRUCTION_LINE_HEIGHT =
    16.sp

private val HINTS_TOP_PADDING =
    7.dp

private val HINT_SPACING =
    4.dp

private val HINT_DOT_SIZE =
    5.dp

private val HINT_DOT_TOP_PADDING =
    5.dp

private const val HINT_DOT_ALPHA =
    0.75f

private val HINT_TEXT_SPACING =
    7.dp

private val HINT_FONT_SIZE =
    11.5.sp

private val HINT_LINE_HEIGHT =
    15.sp

private const val DETAILS_FADE_IN_MILLIS =
    160

private const val DETAILS_EXPAND_MILLIS =
    210

private const val DETAILS_FADE_OUT_MILLIS =
    120

private const val DETAILS_SHRINK_MILLIS =
    160

private val ACTIONS_TOP_PADDING =
    8.dp

private val ACTION_SPACING =
    4.dp

private val ACTION_HORIZONTAL_PADDING =
    8.dp

private val ACTION_VERTICAL_PADDING =
    5.dp

private val ACTION_ICON_SPACING =
    4.dp

private val ACTION_ICON_SIZE =
    13.dp

private val ACTION_FONT_SIZE =
    11.sp

private const val ACTION_VISIBILITY_MILLIS =
    160
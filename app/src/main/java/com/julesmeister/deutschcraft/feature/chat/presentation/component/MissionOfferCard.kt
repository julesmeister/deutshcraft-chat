package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

@Composable
internal fun MissionOfferCard(
    opportunity: MissionOpportunity,
    isStarting: Boolean,
    onStartClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * All colors come from MaterialTheme so this component
     * automatically follows DeutschCraftTheme.
     *
     * Dark:
     *     primary -> DeutschLime
     *
     * Light:
     *     primary -> DeutschLimeDark
     */
    val colorScheme =
        MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    CARD_SHAPE
                )
                .background(
                    color =
                        colorScheme
                            .surfaceContainerHigh
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                CARD_SIZE_ANIMATION_MILLIS,
                            easing =
                                FastOutSlowInEasing
                        )
                )
                .padding(
                    horizontal =
                        CARD_HORIZONTAL_PADDING,
                    vertical =
                        CARD_VERTICAL_PADDING
                )
    ) {
        /*
         * ===================================================
         * HEADER
         * ===================================================
         */

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            /*
             * Mission icon.
             */
            Text(
                text =
                    opportunity.mission.icon,

                modifier =
                    Modifier
                        .size(
                            ICON_CONTAINER_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            color =
                                colorScheme
                                    .primary
                                    .copy(
                                        alpha =
                                            ICON_BACKGROUND_ALPHA
                                    )
                        )
                        .padding(
                            ICON_PADDING
                        ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.width(
                        HEADER_HORIZONTAL_SPACING
                    )
            )

            /*
             * Mission label + title.
             */
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        MISSION_LABEL,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        colorScheme.primary
                )

                Text(
                    text =
                        opportunity
                            .mission
                            .title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        colorScheme.onSurface,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            /*
             * Difficulty badge.
             */
            Text(
                text =
                    opportunity
                        .mission
                        .difficulty
                        .name,

                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                percent =
                                    PILL_SHAPE_PERCENT
                            )
                        )
                        .background(
                            color =
                                colorScheme
                                    .primary
                                    .copy(
                                        alpha =
                                            DIFFICULTY_BACKGROUND_ALPHA
                                    )
                        )
                        .padding(
                            horizontal =
                                DIFFICULTY_HORIZONTAL_PADDING,
                            vertical =
                                DIFFICULTY_VERTICAL_PADDING
                        ),

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colorScheme.primary
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    DESCRIPTION_TOP_SPACING
                )
        )

        /*
         * ===================================================
         * DESCRIPTION
         * ===================================================
         */

        Text(
            text =
                opportunity
                    .mission
                    .description,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                colorScheme
                    .onSurfaceVariant,

            maxLines =
                DESCRIPTION_MAX_LINES,

            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    ACTIONS_TOP_SPACING
                )
        )

        /*
         * ===================================================
         * ACTIONS
         * ===================================================
         */

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.End,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            MissionOfferButton(
                text =
                    MAYBE_LATER_LABEL,

                enabled =
                    !isStarting,

                isPrimary =
                    false,

                onClick =
                    onDismissClick
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ACTION_SPACING
                    )
            )

            MissionOfferButton(
                text =
                    START_MISSION_LABEL,

                enabled =
                    !isStarting,

                isPrimary =
                    true,

                isLoading =
                    isStarting,

                onClick =
                    onStartClick
            )
        }
    }
}

/*
 * ===========================================================
 * MISSION OFFER BUTTON
 * ===========================================================
 */

@Composable
private fun MissionOfferButton(
    text: String,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                isPressed &&
                enabled
            ) {
                PRESSED_BUTTON_SCALE
            } else {
                NORMAL_BUTTON_SCALE
            },

        animationSpec =
            spring(
                dampingRatio =
                    BUTTON_DAMPING_RATIO,
                stiffness =
                    BUTTON_STIFFNESS
            ),

        label =
            "MissionOfferButtonScale"
    )

    val buttonBackground =
        when {
            /*
             * Active primary action.
             *
             * Dark:
             *     DeutschLime
             *
             * Light:
             *     DeutschLimeDark
             */
            isPrimary &&
                    enabled -> {
                colorScheme.primary
            }

            /*
             * Disabled primary action.
             */
            isPrimary -> {
                colorScheme
                    .primary
                    .copy(
                        alpha =
                            DISABLED_PRIMARY_ALPHA
                    )
            }

            /*
             * Secondary action.
             */
            else -> {
                colorScheme
                    .surfaceContainerHighest
            }
        }

    val buttonContentColor =
        if (isPrimary) {
            colorScheme.onPrimary
        } else {
            colorScheme.onSurface
        }

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX =
                        scale

                    scaleY =
                        scale
                }
                .clip(
                    RoundedCornerShape(
                        percent =
                            PILL_SHAPE_PERCENT
                    )
                )
                .background(
                    color =
                        buttonBackground
                )
                .clickable(
                    interactionSource =
                        interactionSource,

                    indication =
                        null,

                    enabled =
                        enabled,

                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        BUTTON_HORIZONTAL_PADDING,

                    vertical =
                        BUTTON_VERTICAL_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        LOADING_INDICATOR_SIZE
                    ),

                color =
                    colorScheme.onPrimary,

                strokeWidth =
                    LOADING_INDICATOR_STROKE_WIDTH
            )

            Spacer(
                modifier =
                    Modifier.width(
                        LOADING_TEXT_SPACING
                    )
            )
        }

        Text(
            text =
                text,

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            fontWeight =
                FontWeight.SemiBold,

            color =
                buttonContentColor
        )
    }
}

/*
 * ===========================================================
 * TEXT
 * ===========================================================
 */

private const val MISSION_LABEL =
    "Conversation mission"

private const val MAYBE_LATER_LABEL =
    "Maybe later"

private const val START_MISSION_LABEL =
    "Start mission"

/*
 * ===========================================================
 * CARD
 * ===========================================================
 */

private val CARD_SHAPE =
    RoundedCornerShape(
        22.dp
    )

private val CARD_ELEVATION =
    12.dp

private val CARD_HORIZONTAL_PADDING =
    16.dp

private val CARD_VERTICAL_PADDING =
    14.dp

/*
 * ===========================================================
 * HEADER
 * ===========================================================
 */

private val ICON_CONTAINER_SIZE =
    38.dp

private val ICON_PADDING =
    7.dp

private val HEADER_HORIZONTAL_SPACING =
    10.dp

private const val ICON_BACKGROUND_ALPHA =
    0.16f

/*
 * ===========================================================
 * DIFFICULTY
 * ===========================================================
 */

private const val PILL_SHAPE_PERCENT =
    50

private const val DIFFICULTY_BACKGROUND_ALPHA =
    0.14f

private val DIFFICULTY_HORIZONTAL_PADDING =
    9.dp

private val DIFFICULTY_VERTICAL_PADDING =
    5.dp

/*
 * ===========================================================
 * CONTENT
 * ===========================================================
 */

private val DESCRIPTION_TOP_SPACING =
    10.dp

private const val DESCRIPTION_MAX_LINES =
    3

private val ACTIONS_TOP_SPACING =
    12.dp

private val ACTION_SPACING =
    8.dp

/*
 * ===========================================================
 * BUTTON
 * ===========================================================
 */

private val BUTTON_HORIZONTAL_PADDING =
    14.dp

private val BUTTON_VERTICAL_PADDING =
    9.dp

private const val NORMAL_BUTTON_SCALE =
    1f

private const val PRESSED_BUTTON_SCALE =
    0.95f

private const val BUTTON_DAMPING_RATIO =
    0.74f

private const val BUTTON_STIFFNESS =
    620f

private const val DISABLED_PRIMARY_ALPHA =
    0.38f

/*
 * ===========================================================
 * LOADING
 * ===========================================================
 */

private val LOADING_INDICATOR_SIZE =
    16.dp

private val LOADING_INDICATOR_STROKE_WIDTH =
    2.dp

private val LOADING_TEXT_SPACING =
    7.dp

/*
 * ===========================================================
 * ANIMATION
 * ===========================================================
 */

private const val CARD_SIZE_ANIMATION_MILLIS =
    260
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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
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
                    ChatDesignTokens.MissionOffer.CardShape
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
                                ChatDesignTokens.MissionOffer.CardSizeAnimationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )
                .padding(
                    horizontal =
                        ChatDesignTokens.MissionOffer.CardHorizontalPadding,
                    vertical =
                        ChatDesignTokens.MissionOffer.CardVerticalPadding
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
                            ChatDesignTokens.MissionOffer.IconContainerSize
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
                                            ChatDesignTokens.MissionOffer.IconBackgroundAlpha
                                    )
                        )
                        .padding(
                            ChatDesignTokens.MissionOffer.IconPadding
                        ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.MissionOffer.HeaderHorizontalSpacing
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
                        ChatPresentationTokens.MissionOfferCard.MissionLabel,

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
                                    ChatDesignTokens.MissionOffer.PillShapePercent
                            )
                        )
                        .background(
                            color =
                                colorScheme
                                    .primary
                                    .copy(
                                        alpha =
                                            ChatDesignTokens.MissionOffer.DifficultyBackgroundAlpha
                                    )
                        )
                        .padding(
                            horizontal =
                                ChatDesignTokens.MissionOffer.DifficultyHorizontalPadding,
                            vertical =
                                ChatDesignTokens.MissionOffer.DifficultyVerticalPadding
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
                    ChatDesignTokens.MissionOffer.DescriptionTopSpacing
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
                ChatDesignTokens.MissionOffer.DescriptionMaxLines,

            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    ChatDesignTokens.MissionOffer.ActionsTopSpacing
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
                    ChatPresentationTokens.MissionOfferCard.MaybeLaterLabel,

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
                        ChatDesignTokens.MissionOffer.ActionSpacing
                    )
            )

            MissionOfferButton(
                text =
                    ChatPresentationTokens.MissionOfferCard.StartMissionLabel,

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
                ChatDesignTokens.MissionOffer.PressedButtonScale
            } else {
                ChatDesignTokens.MissionOffer.NormalButtonScale
            },

        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.MissionOffer.ButtonDampingRatio,
                stiffness =
                    ChatDesignTokens.MissionOffer.ButtonStiffness
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
                            ChatDesignTokens.MissionOffer.DisabledPrimaryAlpha
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
                            ChatDesignTokens.MissionOffer.PillShapePercent
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
                        ChatDesignTokens.MissionOffer.ButtonHorizontalPadding,

                    vertical =
                        ChatDesignTokens.MissionOffer.ButtonVerticalPadding
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        ChatDesignTokens.MissionOffer.LoadingIndicatorSize
                    ),

                color =
                    colorScheme.onPrimary,

                strokeWidth =
                    ChatDesignTokens.MissionOffer.LoadingIndicatorStrokeWidth
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.MissionOffer.LoadingTextSpacing
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

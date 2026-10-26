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
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
internal fun MissionOfferCard(
    opportunity: MissionOpportunity,
    isStarting: Boolean,
    onStartClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation =
                    CARD_ELEVATION,
                shape =
                    CARD_SHAPE,
                clip =
                    false
            )
            .clip(
                CARD_SHAPE
            )
            .background(
                color =
                    DeutschSurfaceHigh
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
                horizontal = 16.dp,
                vertical = 14.dp
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    opportunity.mission.icon,
                modifier = Modifier
                    .size(
                        ICON_CONTAINER_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            DeutschLime.copy(
                                alpha = 0.16f
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
                    Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "Conversation mission",
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DeutschLime
                )

                Text(
                    text =
                        opportunity.mission.title,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DeutschTextPrimary,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Text(
                text =
                    opportunity
                        .mission
                        .difficulty
                        .name,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            percent = 50
                        )
                    )
                    .background(
                        color =
                            DeutschLime.copy(
                                alpha = 0.14f
                            )
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    DeutschLime
            )
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Text(
            text =
                opportunity.mission.description,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                DeutschTextSecondary,
            maxLines =
                3,
            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

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
                    "Maybe later",
                enabled =
                    !isStarting,
                isPrimary =
                    false,
                onClick =
                    onDismissClick
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            MissionOfferButton(
                text =
                    "Start mission",
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

@Composable
private fun MissionOfferButton(
    text: String,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
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

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX =
                    scale

                scaleY =
                    scale
            }
            .clip(
                RoundedCornerShape(
                    percent = 50
                )
            )
            .background(
                color =
                    when {
                        isPrimary &&
                                enabled -> {
                            DeutschLime
                        }

                        isPrimary -> {
                            DeutschLime.copy(
                                alpha = 0.38f
                            )
                        }

                        else -> {
                            MaterialTheme
                                .colorScheme
                                .surfaceContainerHighest
                        }
                    }
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
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(16.dp),
                color =
                    DeutschTextOnLime,
                strokeWidth =
                    2.dp
            )

            Spacer(
                modifier =
                    Modifier.width(7.dp)
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
                if (isPrimary) {
                    DeutschTextOnLime
                } else {
                    DeutschTextPrimary
                }
        )
    }
}

private val CARD_SHAPE =
    RoundedCornerShape(22.dp)

private val CARD_ELEVATION =
    12.dp

private val ICON_CONTAINER_SIZE =
    38.dp

private val ICON_PADDING =
    7.dp

private const val CARD_SIZE_ANIMATION_MILLIS =
    260

private const val NORMAL_BUTTON_SCALE =
    1f

private const val PRESSED_BUTTON_SCALE =
    0.95f

private const val BUTTON_DAMPING_RATIO =
    0.74f

private const val BUTTON_STIFFNESS =
    620f
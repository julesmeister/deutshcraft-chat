package com.julesmeister.deutschcraft.feature.chat.presentation.component
import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessage,
    correction: String?,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender == MessageSender.USER

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "AssistantBubblePulse"
        )

    val pulseFraction by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            PULSE_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "AssistantBubblePulseFraction"
    )

    val regularUserColor =
        MaterialTheme.colorScheme.primaryContainer

    /*
     * Soft mint color used only for the most recent
     * learner message.
     */
    val latestUserColor =
        DeutschLime

    val latestUserContentColor =
        Color(0xFF17331A)

    val assistantStartColor =
        MaterialTheme.colorScheme.surfaceContainer

    val assistantPulseColor =
        MaterialTheme.colorScheme.surfaceContainerHigh

    val bubbleColor =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                latestUserColor
            }

            isUserMessage -> {
                regularUserColor
            }

            isLatestAssistantMessage -> {
                lerp(
                    start =
                        assistantStartColor,
                    stop =
                        assistantPulseColor,
                    fraction =
                        pulseFraction *
                                PULSE_COLOR_STRENGTH
                )
            }

            else -> {
                assistantStartColor
            }
        }

    val contentColor =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                latestUserContentColor
            }

            isUserMessage -> {
                MaterialTheme.colorScheme
                    .onPrimaryContainer
            }

            else -> {
                MaterialTheme.colorScheme
                    .onSurface
            }
        }


    val bubbleShape =
        if (isUserMessage) {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 6.dp,
                bottomEnd = 20.dp
            )
        }

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
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .widthIn(
                    min = MINIMUM_BUBBLE_WIDTH,
                    max = MAXIMUM_BUBBLE_WIDTH
                )
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio =
                                CONTENT_ANIMATION_DAMPING,
                            stiffness =
                                CONTENT_ANIMATION_STIFFNESS
                        )
                )
                .clip(bubbleShape)
                .background(
                    color = bubbleColor
                )
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = null,
                    onClick = onClick
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
        ) {
            Column(
                modifier =
                    Modifier.wrapContentWidth(),
                horizontalAlignment =
                    if (isUserMessage) {
                        Alignment.End
                    } else {
                        Alignment.Start
                    }
            ) {
                Text(
                    text =
                        message.content,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    color =
                        contentColor
                )

                MessageTimestamp(
                    timestamp =
                        message.timestamp,
                    contentColor =
                        contentColor,
                    modifier =
                        if (isUserMessage) {
                            Modifier.align(
                                Alignment.End
                            )
                        } else {
                            Modifier.align(
                                Alignment.Start
                            )
                        }
                )

                if (
                    isUserMessage &&
                    !correction.isNullOrBlank()
                ) {
                    MessageCorrectionContent(
                        correction =
                            correction,
                        contentColor =
                            contentColor,
                        modifier =
                            Modifier.align(
                                Alignment.End
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageTimestamp(
    timestamp: Long,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text =
            formatMessageTime(
                timestamp = timestamp
            ),
        modifier =
            modifier.padding(
                top = 4.dp
            ),
        style =
            MaterialTheme.typography.labelSmall,
        color =
            contentColor.copy(
                alpha = 0.65f
            )
    )
}

private fun formatMessageTime(
    timestamp: Long
): String {
    val formatter =
        DateTimeFormatter.ofPattern(
            TIME_FORMAT_PATTERN,
            Locale.getDefault()
        )

    return Instant
        .ofEpochMilli(timestamp)
        .atZone(
            ZoneId.systemDefault()
        )
        .format(formatter)
}

private val MINIMUM_BUBBLE_WIDTH =
    72.dp

private val MAXIMUM_BUBBLE_WIDTH =
    320.dp

private const val PULSE_DURATION_MILLIS =
    2_200

/*
 * Keeps the assistant pulse subtle.
 *
 * The transition animates between 0 and 1, but only
 * 45% of that color difference is applied.
 */
private const val PULSE_COLOR_STRENGTH =
    0.45f

private const val CONTENT_ANIMATION_DAMPING =
    0.82f

private const val CONTENT_ANIMATION_STIFFNESS =
    420f

private const val TIME_FORMAT_PATTERN =
    "HH:mm"
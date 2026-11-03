// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/component/AnimatedChatTopBarTitle.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import com.julesmeister.deutschcraft.feature.chat.presentation.AdaptiveLearningFeedbackEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import kotlinx.coroutines.delay

@Composable
internal fun AnimatedChatTopBarTitle(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    activeMissionTitle: String?,
    currentMissionObjective: String?,
    currentMissionGuidance: String?,
    missionProgressPercentage: Int,
    objectiveCompletionEvent: MissionObjectiveCompletionEvent?,
    adaptiveLearningFeedbackEvent: AdaptiveLearningFeedbackEvent?,
    onObjectiveCompletionShown: (String) -> Unit,
    onAdaptiveLearningFeedbackShown: (String) -> Unit,
    onMissionGuidanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val missionTitle =
        activeMissionTitle
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )

    val missionObjective =
        currentMissionObjective
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )

    val missionGuidance =
        currentMissionGuidance
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )

    LaunchedEffect(
        objectiveCompletionEvent?.eventId
    ) {
        val event =
            objectiveCompletionEvent
                ?: return@LaunchedEffect

        delay(
            ChatDesignTokens.AnimatedTopBarTitle.ObjectiveCompletionDisplayMillis
        )

        onObjectiveCompletionShown(
            event.eventId
        )
    }

    LaunchedEffect(
        adaptiveLearningFeedbackEvent?.eventId,
        objectiveCompletionEvent?.eventId
    ) {
        val event =
            adaptiveLearningFeedbackEvent
                ?: return@LaunchedEffect

        if (objectiveCompletionEvent != null) {
            return@LaunchedEffect
        }

        delay(
            ChatDesignTokens.AnimatedTopBarTitle.AdaptiveFeedbackDisplayMillis
        )

        onAdaptiveLearningFeedbackShown(
            event.eventId
        )
    }

    val temporaryStatus =
        when {
            objectiveCompletionEvent != null -> {
                ChatTopBarTemporaryStatus.ObjectiveCompleted(
                    eventId =
                        objectiveCompletionEvent.eventId,

                    objectiveTitle =
                        objectiveCompletionEvent.objectiveTitle
                )
            }

            adaptiveLearningFeedbackEvent != null -> {
                ChatTopBarTemporaryStatus.AdaptiveLearningFeedback(
                    eventId =
                        adaptiveLearningFeedbackEvent.eventId,

                    type =
                        adaptiveLearningFeedbackEvent.type,

                    message =
                        adaptiveLearningFeedbackEvent.message,

                    focusTitle =
                        adaptiveLearningFeedbackEvent.focusTitle
                )
            }

            isAssistantTyping -> {
                ChatTopBarTemporaryStatus.AssistantTyping
            }

            isAssessing -> {
                ChatTopBarTemporaryStatus.Assessing
            }

            else -> {
                null
            }
        }

    AnimatedContent(
        targetState =
            temporaryStatus,

        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        ChatDesignTokens.AnimatedTopBarTitle.TopBarContentMinimumHeight
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens.AnimatedTopBarTitle.StatusSizeAnimationMillis,

                            easing =
                                FastOutSlowInEasing
                        )
                ),

        transitionSpec = {
            val enterTransition =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens.AnimatedTopBarTitle.StatusEnterDurationMillis,

                            easing =
                                LinearOutSlowInEasing
                        )
                ) +
                        slideInVertically(
                            initialOffsetY = { height ->
                                height / 5
                            },

                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens.AnimatedTopBarTitle.StatusEnterDurationMillis,

                                    easing =
                                        FastOutSlowInEasing
                                )
                        ) +
                        scaleIn(
                            initialScale =
                                ChatDesignTokens.AnimatedTopBarTitle.StatusEnterScale
                        )

            val exitTransition =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens.AnimatedTopBarTitle.StatusExitDurationMillis
                        )
                ) +
                        slideOutVertically(
                            targetOffsetY = { height ->
                                -height / 5
                            },

                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens.AnimatedTopBarTitle.StatusExitDurationMillis
                                )
                        ) +
                        scaleOut(
                            targetScale =
                                ChatDesignTokens.AnimatedTopBarTitle.StatusExitScale
                        )

            enterTransition togetherWith
                    exitTransition
        },

        contentAlignment =
            Alignment.CenterStart,

        label =
            "ChatTopBarTemporaryStatus"
    ) { displayedStatus ->
        when (displayedStatus) {
            null -> {
                if (missionTitle == null) {
                    NormalStatus()
                } else {
                    MissionTopBarPager(
                        missionTitle =
                            missionTitle,

                        currentObjective =
                            missionObjective,

                        currentGuidance =
                            missionGuidance,

                        missionProgressPercentage =
                            missionProgressPercentage,

                        onMissionGuidanceClick =
                            onMissionGuidanceClick,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }

            ChatTopBarTemporaryStatus.AssistantTyping -> {
                SimpleStatus(
                    icon =
                        "•••",

                    title =
                        "Emil schreibt",

                    subtitle =
                        "Antwort wird vorbereitet"
                )
            }

            ChatTopBarTemporaryStatus.Assessing -> {
                SimpleStatus(
                    icon =
                        "🧠",

                    title =
                        "Skill Assessment",

                    subtitle =
                        "Fortschritt wird aktualisiert"
                )
            }

            is ChatTopBarTemporaryStatus.ObjectiveCompleted -> {
                MissionObjectiveCompletedStatus(
                    eventId =
                        displayedStatus.eventId,

                    objectiveTitle =
                        displayedStatus.objectiveTitle
                )
            }

            is ChatTopBarTemporaryStatus.AdaptiveLearningFeedback -> {
                AdaptiveLearningStatus(
                    type =
                        displayedStatus.type,

                    message =
                        displayedStatus.message,

                    focusTitle =
                        displayedStatus.focusTitle
                )
            }
        }
    }
}

@Composable
private fun AdaptiveLearningStatus(
    type: AdaptiveLearningFeedbackType,
    message: String,
    focusTitle: String?
) {
    val presentation =
        adaptiveFeedbackPresentation(
            type =
                type
        )

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens.AnimatedTopBarTitle.StatusIconSize
                    )
                    .clip(
                        RoundedCornerShape(
                            ChatDesignTokens.AnimatedTopBarTitle.StatusIconCornerRadius
                        )
                    )
                    .background(
                        presentation
                            .containerColor
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    presentation.icon,

                color =
                    presentation.iconContentColor,

                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    ChatDesignTokens.AnimatedTopBarTitle.StatusContentSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    focusTitle
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: presentation.title,

                modifier =
                    Modifier.fillMaxWidth(),

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    presentation.titleColor,

                textAlign =
                    TextAlign.Start,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            MarqueeSubtitle(
                text =
                    message
            )
        }
    }
}

@Composable
private fun adaptiveFeedbackPresentation(
    type: AdaptiveLearningFeedbackType
): AdaptiveFeedbackPresentation {
    val colorScheme =
        MaterialTheme.colorScheme

    val isLightTheme =
        colorScheme
            .background
            .luminance() >=
                ChatDesignTokens.DarkLuminanceThreshold

    val limeTitleColor =
        if (isLightTheme) {
            colorScheme.primary
        } else {
            DeutschLime
        }

    val limeContainerColor =
        if (isLightTheme) {
            colorScheme.primaryContainer
        } else {
            DeutschLime.copy(
                alpha =
                    ChatDesignTokens.AnimatedTopBarTitle.AdaptiveIconBackgroundAlpha
            )
        }

    val limeIconContentColor =
        if (isLightTheme) {
            colorScheme.onPrimaryContainer
        } else {
            DeutschLime
        }

    val tertiaryTitleColor =
        colorScheme.tertiary

    val tertiaryContainerColor =
        colorScheme.tertiary.copy(
            alpha =
                if (isLightTheme) {
                    ChatDesignTokens.AnimatedTopBarTitle.LightTertiaryContainerAlpha
                } else {
                    ChatDesignTokens.AnimatedTopBarTitle.AdaptiveIconBackgroundAlpha
                }
        )

    val tertiaryIconContentColor =
        if (isLightTheme) {
            colorScheme.onSurface
        } else {
            colorScheme.tertiary
        }

    return when (type) {
        AdaptiveLearningFeedbackType.GENTLE_REMINDER -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "💡",

                title =
                    "Learning focus",

                titleColor =
                    limeTitleColor,

                containerColor =
                    limeContainerColor,

                iconContentColor =
                    limeIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.REPEATED_MISTAKE -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "↻",

                title =
                    "Still practicing",

                titleColor =
                    tertiaryTitleColor,

                containerColor =
                    tertiaryContainerColor,

                iconContentColor =
                    tertiaryIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.PARTIAL_SUCCESS -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "◐",

                title =
                    "Almost there",

                titleColor =
                    limeTitleColor,

                containerColor =
                    limeContainerColor,

                iconContentColor =
                    limeIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.SUCCESS -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "✓",

                title =
                    "Nice use",

                titleColor =
                    limeTitleColor,

                containerColor =
                    limeContainerColor,

                iconContentColor =
                    limeIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.IMPROVING -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "✨",

                title =
                    "Improving",

                titleColor =
                    limeTitleColor,

                containerColor =
                    limeContainerColor,

                iconContentColor =
                    limeIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.MASTERED -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "🏆",

                title =
                    "Skill mastered",

                titleColor =
                    limeTitleColor,

                containerColor =
                    limeContainerColor,

                iconContentColor =
                    limeIconContentColor
            )
        }

        AdaptiveLearningFeedbackType.NEEDS_REVIEW -> {
            AdaptiveFeedbackPresentation(
                icon =
                    "↺",

                title =
                    "Quick review",

                titleColor =
                    tertiaryTitleColor,

                containerColor =
                    tertiaryContainerColor,

                iconContentColor =
                    tertiaryIconContentColor
            )
        }
    }
}

@Composable
private fun NormalStatus() {
    Column(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "DeutschCraft",

            modifier =
                Modifier.fillMaxWidth(),

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground,

            textAlign =
                TextAlign.Start,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )

        Row(
            modifier =
                Modifier.padding(
                    top =
                        ChatDesignTokens.AnimatedTopBarTitle.NormalSubtitleTopPadding
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            ChatPresenceIndicator()

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.AnimatedTopBarTitle.OnlineDotSpacing
                    )
            )

            Text(
                text =
                    "Emil ist online",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                maxLines =
                    1
            )
        }
    }
}

@Composable
private fun SimpleStatus(
    icon: String,
    title: String,
    subtitle: String
) {
    val colorScheme =
        MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ChatDesignTokens.AnimatedTopBarTitle.StatusIconSize
                    )
                    .clip(
                        RoundedCornerShape(
                            ChatDesignTokens.AnimatedTopBarTitle.StatusIconCornerRadius
                        )
                    )
                    .background(
                        colorScheme.primaryContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    icon,

                color =
                    colorScheme.onPrimaryContainer,

                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    ChatDesignTokens.AnimatedTopBarTitle.StatusContentSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    title,

                modifier =
                    Modifier.fillMaxWidth(),

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    colorScheme.onBackground,

                textAlign =
                    TextAlign.Start,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            MarqueeSubtitle(
                text =
                    subtitle
            )
        }
    }
}

@Composable
private fun MarqueeSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text =
            text,

        modifier =
            modifier
                .fillMaxWidth()
                .basicMarquee(
                    iterations =
                        Int.MAX_VALUE
                ),

        style =
            MaterialTheme
                .typography
                .labelSmall,

        color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,

        textAlign =
            TextAlign.Start,

        maxLines =
            1,

        softWrap =
            false,

        overflow =
            TextOverflow.Clip
    )
}

private data class AdaptiveFeedbackPresentation(
    val icon: String,
    val title: String,
    val titleColor: Color,
    val containerColor: Color,
    val iconContentColor: Color
)

private sealed interface ChatTopBarTemporaryStatus {

    data object AssistantTyping :
        ChatTopBarTemporaryStatus

    data object Assessing :
        ChatTopBarTemporaryStatus

    data class ObjectiveCompleted(
        val eventId: String,
        val objectiveTitle: String
    ) : ChatTopBarTemporaryStatus

    data class AdaptiveLearningFeedback(
        val eventId: String,
        val type: AdaptiveLearningFeedbackType,
        val message: String,
        val focusTitle: String?
    ) : ChatTopBarTemporaryStatus
}

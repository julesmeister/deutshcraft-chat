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
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import com.julesmeister.deutschcraft.feature.chat.presentation.AdaptiveLearningFeedbackEvent
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent
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
            OBJECTIVE_COMPLETION_DISPLAY_MILLIS
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
            ADAPTIVE_FEEDBACK_DISPLAY_MILLIS
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
                        TOP_BAR_CONTENT_MINIMUM_HEIGHT
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                STATUS_SIZE_ANIMATION_MILLIS,

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
                                STATUS_ENTER_DURATION_MILLIS,

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
                                        STATUS_ENTER_DURATION_MILLIS,

                                    easing =
                                        FastOutSlowInEasing
                                )
                        ) +
                        scaleIn(
                            initialScale =
                                STATUS_ENTER_SCALE
                        )

            val exitTransition =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                STATUS_EXIT_DURATION_MILLIS
                        )
                ) +
                        slideOutVertically(
                            targetOffsetY = { height ->
                                -height / 5
                            },

                            animationSpec =
                                tween(
                                    durationMillis =
                                        STATUS_EXIT_DURATION_MILLIS
                                )
                        ) +
                        scaleOut(
                            targetScale =
                                STATUS_EXIT_SCALE
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
                        STATUS_ICON_SIZE
                    )
                    .clip(
                        RoundedCornerShape(
                            STATUS_ICON_CORNER_RADIUS
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
                    STATUS_CONTENT_SPACING
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
                LIGHT_THEME_LUMINANCE_THRESHOLD

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
                    ADAPTIVE_ICON_BACKGROUND_ALPHA
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
                    LIGHT_TERTIARY_CONTAINER_ALPHA
                } else {
                    ADAPTIVE_ICON_BACKGROUND_ALPHA
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
                        NORMAL_SUBTITLE_TOP_PADDING
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            ChatPresenceIndicator()

            Spacer(
                modifier =
                    Modifier.width(
                        ONLINE_DOT_SPACING
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
                        STATUS_ICON_SIZE
                    )
                    .clip(
                        RoundedCornerShape(
                            STATUS_ICON_CORNER_RADIUS
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
                    STATUS_CONTENT_SPACING
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

private const val OBJECTIVE_COMPLETION_DISPLAY_MILLIS =
    1_650L

private const val ADAPTIVE_FEEDBACK_DISPLAY_MILLIS =
    3_000L

private val TOP_BAR_CONTENT_MINIMUM_HEIGHT =
    42.dp

private val ONLINE_DOT_SPACING =
    5.dp

private val NORMAL_SUBTITLE_TOP_PADDING =
    1.dp

private val STATUS_ICON_SIZE =
    32.dp

private val STATUS_ICON_CORNER_RADIUS =
    12.dp

private val STATUS_CONTENT_SPACING =
    8.dp

private const val ADAPTIVE_ICON_BACKGROUND_ALPHA =
    0.15f

private const val LIGHT_TERTIARY_CONTAINER_ALPHA =
    0.12f

private const val LIGHT_THEME_LUMINANCE_THRESHOLD =
    0.5f

private const val STATUS_SIZE_ANIMATION_MILLIS =
    380

private const val STATUS_ENTER_DURATION_MILLIS =
    360

private const val STATUS_EXIT_DURATION_MILLIS =
    220

private const val STATUS_ENTER_SCALE =
    0.97f

private const val STATUS_EXIT_SCALE =
    0.98f
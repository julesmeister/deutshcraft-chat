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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    objectiveCompletionEvent:
    MissionObjectiveCompletionEvent?,
    onObjectiveCompletionShown:
        (String) -> Unit,
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

    /*
     * Objective-completion feedback remains visible briefly
     * before it is consumed from ChatUiState.
     */
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

    val temporaryStatus =
        when {
            objectiveCompletionEvent != null -> {
                ChatTopBarTemporaryStatus
                    .ObjectiveCompleted(
                        eventId =
                            objectiveCompletionEvent.eventId,
                        objectiveTitle =
                            objectiveCompletionEvent
                                .objectiveTitle
                    )
            }

            isAssistantTyping -> {
                ChatTopBarTemporaryStatus
                    .AssistantTyping
            }

            isAssessing -> {
                ChatTopBarTemporaryStatus
                    .Assessing
            }

            else -> {
                null
            }
        }

    /*
     * Normal and mission content stay structurally stable.
     *
     * Temporary states such as typing, assessment, and
     * completion temporarily replace that content, then the
     * learner returns to the swipeable mission pager.
     */
    AnimatedContent(
        targetState =
            temporaryStatus,
        modifier = modifier
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
                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }

            ChatTopBarTemporaryStatus
                .AssistantTyping -> {

                SimpleStatus(
                    icon =
                        "•••",
                    title =
                        "Emil schreibt",
                    subtitle =
                        "Antwort wird vorbereitet"
                )
            }

            ChatTopBarTemporaryStatus
                .Assessing -> {

                SimpleStatus(
                    icon =
                        "🧠",
                    title =
                        "Skill Assessment",
                    subtitle =
                        "Fortschritt wird aktualisiert"
                )
            }

            is ChatTopBarTemporaryStatus
            .ObjectiveCompleted -> {

                MissionObjectiveCompletedStatus(
                    eventId =
                        displayedStatus.eventId,
                    objectiveTitle =
                        displayedStatus.objectiveTitle
                )
            }
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
            Box(
                modifier = Modifier
                    .size(
                        ONLINE_DOT_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            DeutschLime
                    )
            )

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
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(
                    STATUS_ICON_SIZE
                )
                .clip(
                    RoundedCornerShape(
                        STATUS_ICON_CORNER_RADIUS
                    )
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    icon,
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
                Modifier.weight(1f),
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
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    subtitle,
                modifier =
                    Modifier.fillMaxWidth(),
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
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

private sealed interface ChatTopBarTemporaryStatus {

    data object AssistantTyping :
        ChatTopBarTemporaryStatus

    data object Assessing :
        ChatTopBarTemporaryStatus

    data class ObjectiveCompleted(
        val eventId: String,
        val objectiveTitle: String
    ) : ChatTopBarTemporaryStatus
}

private const val OBJECTIVE_COMPLETION_DISPLAY_MILLIS =
    1_650L

private val TOP_BAR_CONTENT_MINIMUM_HEIGHT =
    42.dp

private val ONLINE_DOT_SIZE =
    7.dp

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
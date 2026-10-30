// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/LearningFocusPanel.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningFocusType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState

@Composable
internal fun LearningFocusPanel(
    focuses:
    List<LearningFocusProgressUiModel>,

    reinforcementFocusCount: Int,

    improvingFocusCount: Int,

    masteredFocusCount: Int,

    showEnglish: Boolean,

    colors:
    ProgressColors,

    modifier:
    Modifier =
        Modifier
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),

        shape =
            RoundedCornerShape(
                LEARNING_PANEL_RADIUS
            ),

        color =
            colors.learningPanelBackground,

        tonalElevation =
            0.dp,

        shadowElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    LEARNING_PANEL_PADDING
                )
        ) {
            LearningFocusHeader(
                showEnglish =
                    showEnglish,

                colors =
                    colors
            )

            Spacer(
                modifier =
                    Modifier.height(
                        LEARNING_SECTION_SPACING
                    )
            )

            if (focuses.isEmpty()) {
                LearningFocusEmptyState(
                    showEnglish =
                        showEnglish,

                    colors =
                        colors
                )
            } else {
                LearningFocusSummary(
                    reinforcementFocusCount =
                        reinforcementFocusCount,

                    improvingFocusCount =
                        improvingFocusCount,

                    masteredFocusCount =
                        masteredFocusCount,

                    showEnglish =
                        showEnglish,

                    colors =
                        colors
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            LEARNING_SECTION_SPACING
                        )
                )

                focuses.forEachIndexed { index, focus ->
                    LearningFocusRow(
                        focus =
                            focus,

                        showEnglish =
                            showEnglish,

                        colors =
                            colors
                    )

                    if (
                        index <
                        focuses.lastIndex
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    LEARNING_ROW_SPACING
                                )
                        )
                    }
                }

                Text(
                    text =
                        if (showEnglish) {
                            "From conversations and targeted missions"
                        } else {
                            "Aus Gesprächen und gezielten Missionen"
                        },

                    modifier =
                        Modifier.padding(
                            top =
                                LEARNING_FOOTER_TOP_PADDING
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun LearningFocusHeader(
    showEnglish: Boolean,
    colors: ProgressColors
) {
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
                        LEARNING_HEADER_ICON_SIZE
                    )
                    .clip(
                        RoundedCornerShape(
                            LEARNING_HEADER_ICON_RADIUS
                        )
                    )
                    .background(
                        colors.learningAccentContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .Psychology,

                contentDescription =
                    null,

                tint =
                    colors.learningAccent
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    LEARNING_HEADER_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    if (showEnglish) {
                        "Learning focus"
                    } else {
                        "Lernschwerpunkte"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    colors.textPrimary
            )

            Text(
                text =
                    if (showEnglish) {
                        "What DeutschCraft is noticing"
                    } else {
                        "Was DeutschCraft gerade erkennt"
                    },

                modifier =
                    Modifier.padding(
                        top =
                            LEARNING_HEADER_DESCRIPTION_PADDING
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    colors.textSecondary
            )
        }
    }
}

@Composable
private fun LearningFocusSummary(
    reinforcementFocusCount: Int,
    improvingFocusCount: Int,
    masteredFocusCount: Int,
    showEnglish: Boolean,
    colors: ProgressColors
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        LEARNING_SUMMARY_RADIUS
                    )
                )
                .background(
                    colors.learningSummaryBackground
                )
                .padding(
                    horizontal =
                        LEARNING_SUMMARY_HORIZONTAL_PADDING,

                    vertical =
                        LEARNING_SUMMARY_VERTICAL_PADDING
                ),

        horizontalArrangement =
            Arrangement.SpaceAround
    ) {
        LearningSummaryMetric(
            value =
                reinforcementFocusCount,

            label =
                if (showEnglish) {
                    "Focus"
                } else {
                    "Fokus"
                },

            colors =
                colors
        )

        LearningSummaryMetric(
            value =
                improvingFocusCount,

            label =
                if (showEnglish) {
                    "Improving"
                } else {
                    "Besser"
                },

            colors =
                colors
        )

        LearningSummaryMetric(
            value =
                masteredFocusCount,

            label =
                if (showEnglish) {
                    "Mastered"
                } else {
                    "Sicher"
                },

            colors =
                colors
        )
    }
}

@Composable
private fun LearningSummaryMetric(
    value: Int,
    label: String,
    colors: ProgressColors
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                value.toString(),

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            fontWeight =
                FontWeight.Bold,

            color =
                colors.learningAccent
        )

        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color =
                colors.textSecondary
        )
    }
}

@Composable
private fun LearningFocusRow(
    focus:
    LearningFocusProgressUiModel,

    showEnglish: Boolean,

    colors:
    ProgressColors
) {
    val animatedMastery by
    animateFloatAsState(
        targetValue =
            focus.safeMasteryPercentage /
                    100f,

        animationSpec =
            tween(
                durationMillis =
                    LEARNING_PROGRESS_ANIMATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "LearningFocus_${focus.focusKey}"
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        LEARNING_FOCUS_RADIUS
                    )
                )
                .background(
                    colors.learningItemBackground
                )
                .padding(
                    LEARNING_FOCUS_PADDING
                )
    ) {
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
                            LEARNING_FOCUS_ICON_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colors.learningAccentContainer
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        focus.masteryState.icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            LEARNING_FOCUS_ICON_CONTENT_SIZE
                        ),

                    tint =
                        colors.learningAccent
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        LEARNING_FOCUS_ICON_SPACING
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        focus.title,

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        colors.textPrimary,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Row(
                    modifier =
                        Modifier.padding(
                            top =
                                LEARNING_TYPE_TOP_PADDING
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            focus.type.displayName(
                                showEnglish =
                                    showEnglish
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            colors.textSecondary
                    )

                    Text(
                        text =
                            "  •  ",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            colors.textSecondary
                    )

                    Text(
                        text =
                            focus.masteryState.displayName(
                                showEnglish =
                                    showEnglish
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            colors.learningAccent
                    )
                }
            }

            Text(
                text =
                    "${focus.safeMasteryPercentage}%",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.learningAccent
            )
        }

        LinearProgressIndicator(
            progress = {
                animatedMastery
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            LEARNING_PROGRESS_TOP_PADDING
                    )
                    .height(
                        LEARNING_PROGRESS_HEIGHT
                    )
                    .clip(
                        CircleShape
                    ),

            color =
                colors.learningAccent,

            trackColor =
                colors.learningProgressTrack
        )

        Text(
            text =
                focus.evidenceDescription(
                    showEnglish =
                        showEnglish
                ),

            modifier =
                Modifier.padding(
                    top =
                        LEARNING_METADATA_TOP_PADDING
                ),

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color =
                colors.textSecondary
        )

        val guidance =
            focus
                .learnerGuidance
                .trim()
                .takeIf(
                    String::isNotBlank
                )

        if (guidance != null) {
            Text(
                text =
                    guidance,

                modifier =
                    Modifier.padding(
                        top =
                            LEARNING_GUIDANCE_TOP_PADDING
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    colors.textPrimary,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LearningFocusEmptyState(
    showEnglish: Boolean,
    colors: ProgressColors
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        LEARNING_FOCUS_RADIUS
                    )
                )
                .background(
                    colors.learningItemBackground
                )
                .padding(
                    LEARNING_FOCUS_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        LEARNING_FOCUS_ICON_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        colors.learningAccentContainer
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

                tint =
                    colors.learningAccent
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    LEARNING_EMPTY_ICON_SPACING
                )
        )

        Text(
            text =
                if (showEnglish) {
                    "Recurring learning patterns will appear here."
                } else {
                    "Wiederkehrende Lernmuster erscheinen hier."
                },

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                colors.textSecondary
        )
    }
}

private fun LearningFocusProgressUiModel.evidenceDescription(
    showEnglish: Boolean
): String {

    val evidenceLabel =
        if (showEnglish) {
            if (safeEvidenceCount == 1) {
                "1 observation"
            } else {
                "$safeEvidenceCount observations"
            }
        } else {
            if (safeEvidenceCount == 1) {
                "1 Beobachtung"
            } else {
                "$safeEvidenceCount Beobachtungen"
            }
        }

    if (safeConsecutiveSuccessCount <= 0) {
        return evidenceLabel
    }

    return if (showEnglish) {
        "$evidenceLabel  •  " +
                "$safeConsecutiveSuccessCount successful in a row"
    } else {
        "$evidenceLabel  •  " +
                "$safeConsecutiveSuccessCount erfolgreich in Folge"
    }
}

private fun LearningFocusType.displayName(
    showEnglish: Boolean
): String {

    return when (this) {
        LearningFocusType.GRAMMAR ->
            if (showEnglish) {
                "Grammar"
            } else {
                "Grammatik"
            }

        LearningFocusType.VOCABULARY ->
            if (showEnglish) {
                "Vocabulary"
            } else {
                "Wortschatz"
            }

        LearningFocusType.COMMUNICATION ->
            if (showEnglish) {
                "Communication"
            } else {
                "Kommunikation"
            }
    }
}

private fun MasteryState.displayName(
    showEnglish: Boolean
): String {

    return when (this) {
        MasteryState.OBSERVING ->
            if (showEnglish) {
                "Observing"
            } else {
                "Beobachtung"
            }

        MasteryState.STRUGGLING ->
            if (showEnglish) {
                "Needs attention"
            } else {
                "Aufmerksamkeit"
            }

        MasteryState.PRACTICING ->
            if (showEnglish) {
                "Practising"
            } else {
                "In Übung"
            }

        MasteryState.IMPROVING ->
            if (showEnglish) {
                "Improving"
            } else {
                "Verbessert"
            }

        MasteryState.MASTERED ->
            if (showEnglish) {
                "Mastered"
            } else {
                "Gemeistert"
            }

        MasteryState.NEEDS_REVIEW ->
            if (showEnglish) {
                "Needs review"
            } else {
                "Wiederholen"
            }
    }
}

private val MasteryState.icon:
        ImageVector
    get() =
        when (this) {
            MasteryState.OBSERVING ->
                Icons.Outlined.AutoAwesome

            MasteryState.STRUGGLING ->
                Icons.Outlined.Replay

            MasteryState.PRACTICING ->
                Icons.Outlined.Replay

            MasteryState.IMPROVING ->
                Icons.Outlined.TrendingUp

            MasteryState.MASTERED ->
                Icons.Outlined.CheckCircle

            MasteryState.NEEDS_REVIEW ->
                Icons.Outlined.Replay
        }

private val LEARNING_PANEL_RADIUS =
    28.dp

private val LEARNING_PANEL_PADDING =
    20.dp

private val LEARNING_HEADER_ICON_SIZE =
    46.dp

private val LEARNING_HEADER_ICON_RADIUS =
    15.dp

private val LEARNING_HEADER_SPACING =
    12.dp

private val LEARNING_HEADER_DESCRIPTION_PADDING =
    3.dp

private val LEARNING_SECTION_SPACING =
    18.dp

private val LEARNING_SUMMARY_RADIUS =
    18.dp

private val LEARNING_SUMMARY_HORIZONTAL_PADDING =
    12.dp

private val LEARNING_SUMMARY_VERTICAL_PADDING =
    14.dp

private val LEARNING_ROW_SPACING =
    10.dp

private val LEARNING_FOCUS_RADIUS =
    18.dp

private val LEARNING_FOCUS_PADDING =
    14.dp

private val LEARNING_FOCUS_ICON_SIZE =
    38.dp

private val LEARNING_FOCUS_ICON_CONTENT_SIZE =
    19.dp

private val LEARNING_FOCUS_ICON_SPACING =
    12.dp

private val LEARNING_TYPE_TOP_PADDING =
    3.dp

private val LEARNING_PROGRESS_TOP_PADDING =
    12.dp

private val LEARNING_PROGRESS_HEIGHT =
    6.dp

private val LEARNING_METADATA_TOP_PADDING =
    8.dp

private val LEARNING_GUIDANCE_TOP_PADDING =
    8.dp

private val LEARNING_EMPTY_ICON_SPACING =
    12.dp

private val LEARNING_FOOTER_TOP_PADDING =
    16.dp

private const val LEARNING_PROGRESS_ANIMATION_MILLIS =
    850
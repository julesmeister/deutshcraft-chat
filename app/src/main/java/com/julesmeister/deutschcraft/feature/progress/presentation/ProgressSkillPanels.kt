package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressConfidence
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSkillSummary
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary

@Composable
internal fun SkillBreakdownPanel(
    progress:
    ProgressSummary,

    showEnglish:
    Boolean,

    colors:
    ProgressColors,

    modifier:
    Modifier =
        Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                PANEL_RADIUS
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer,

        tonalElevation =
            0.dp,

        shadowElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    PANEL_PADDING
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons
                            .Outlined
                            .Insights,

                    contentDescription =
                        null,

                    tint =
                        colors.accent
                )

                AnimatedContent(
                    targetState =
                        showEnglish,

                    transitionSpec = {
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        LANGUAGE_ANIMATION_MILLIS
                                )
                        ) togetherWith
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                LANGUAGE_ANIMATION_MILLIS
                                        )
                                )
                    },

                    label =
                        "SkillBreakdownTitleLanguage"
                ) { english ->
                    Text(
                        text =
                            if (english) {
                                "Skills"
                            } else {
                                "Fähigkeiten"
                            },

                        modifier =
                            Modifier.padding(
                                start =
                                    PANEL_TITLE_SPACING
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            colors.textPrimary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        SKILL_LIST_TOP_SPACING
                    )
            )

            progress
                .skillSummaries
                .forEachIndexed { index, skill ->
                    SkillProgressRow(
                        skill =
                            skill,

                        showEnglish =
                            showEnglish,

                        colors =
                            colors
                    )

                    if (
                        index <
                        progress.skillSummaries.lastIndex
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    SKILL_ROW_SPACING
                                )
                        )
                    }
                }
        }
    }
}

@Composable
private fun SkillProgressRow(
    skill:
    ProgressSkillSummary,

    showEnglish:
    Boolean,

    colors:
    ProgressColors,

    modifier:
    Modifier =
        Modifier
) {
    val safeScore =
        skill.score.coerceIn(
            minimumValue =
                0,

            maximumValue =
                100
        )

    val animatedScore by
    animateFloatAsState(
        targetValue =
            safeScore /
                    100f,

        animationSpec =
            tween(
                durationMillis =
                    SKILL_PROGRESS_ANIMATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "SkillProgress_${skill.label}"
    )

    Column(
        modifier =
            modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState =
                    showEnglish,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    LANGUAGE_ANIMATION_MILLIS
                            )
                    ) togetherWith
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            LANGUAGE_ANIMATION_MILLIS
                                    )
                            )
                },

                label =
                    "SkillLabelLanguage_${skill.label}"
            ) { english ->
                Text(
                    text =
                        translatedSkillLabel(
                            germanLabel =
                                skill.label,

                            showEnglish =
                                english
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        colors.textPrimary
                )
            }

            Text(
                text =
                    "$safeScore%",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.textPrimary
            )
        }

        LinearProgressIndicator(
            progress = {
                animatedScore
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            SKILL_PROGRESS_TOP_PADDING
                    )
                    .height(
                        SKILL_PROGRESS_HEIGHT
                    )
                    .clip(
                        CircleShape
                    ),

            color =
                colors.accent,

            trackColor =
                colors.progressTrack
        )
    }
}

@Composable
internal fun ConfidencePanel(
    progress:
    ProgressSummary,

    showEnglish:
    Boolean,

    colors:
    ProgressColors,

    modifier:
    Modifier =
        Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                PANEL_RADIUS
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer,

        tonalElevation =
            0.dp,

        shadowElevation =
            0.dp
    ) {
        AnimatedContent(
            targetState =
                showEnglish,

            transitionSpec = {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                LANGUAGE_ANIMATION_MILLIS
                        )
                ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        LANGUAGE_ANIMATION_MILLIS
                                )
                        )
            },

            label =
                "ConfidencePanelLanguage"
        ) { english ->
            Column(
                modifier =
                    Modifier.padding(
                        PANEL_PADDING
                    )
            ) {
                Text(
                    text =
                        if (english) {
                            "Evidence quality"
                        } else {
                            "Evidenzqualität"
                        },

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    color =
                        colors.textSecondary
                )

                Text(
                    text =
                        confidenceName(
                            confidence =
                                progress.confidence,

                            showEnglish =
                                english
                        ),

                    modifier =
                        Modifier.padding(
                            top =
                                CONFIDENCE_NAME_TOP_PADDING
                        ),

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
                        confidenceDescription(
                            confidence =
                                progress.confidence,

                            showEnglish =
                                english
                        ),

                    modifier =
                        Modifier.padding(
                            top =
                                CONFIDENCE_DESCRIPTION_TOP_PADDING
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        colors.textSecondary
                )
            }
        }
    }
}

private fun translatedSkillLabel(
    germanLabel:
    String,

    showEnglish:
    Boolean
): String {
    if (!showEnglish) {
        return germanLabel
    }

    return when (
        germanLabel
            .trim()
            .lowercase()
    ) {
        "grammatik" ->
            "Grammar"

        "wortschatz" ->
            "Vocabulary"

        "kohärenz" ->
            "Coherence"

        "genauigkeit" ->
            "Accuracy"

        "sprachumfang" ->
            "Range"

        else ->
            germanLabel
    }
}

private fun confidenceName(
    confidence:
    ProgressConfidence,

    showEnglish:
    Boolean
): String {
    return when (confidence) {
        ProgressConfidence.NOT_ENOUGH_DATA ->
            if (showEnglish) {
                "Not enough data"
            } else {
                "Noch nicht genügend Daten"
            }

        ProgressConfidence.EARLY ->
            if (showEnglish) {
                "Early estimate"
            } else {
                "Frühe Einschätzung"
            }

        ProgressConfidence.DEVELOPING ->
            if (showEnglish) {
                "Developing estimate"
            } else {
                "Sich entwickelnde Einschätzung"
            }

        ProgressConfidence.RELIABLE ->
            if (showEnglish) {
                "Reliable"
            } else {
                "Zuverlässig"
            }
    }
}

private fun confidenceDescription(
    confidence:
    ProgressConfidence,

    showEnglish:
    Boolean
): String {
    return when (confidence) {
        ProgressConfidence.NOT_ENOUGH_DATA ->
            if (showEnglish) {
                "DeutschCraft needs more analyzed messages before your progress estimate becomes meaningful."
            } else {
                "DeutschCraft benötigt mehr analysierte Nachrichten, bevor deine Fortschrittseinschätzung aussagekräftig wird."
            }

        ProgressConfidence.EARLY ->
            if (showEnglish) {
                "Your current estimate is based on limited evidence and may still change noticeably."
            } else {
                "Deine aktuelle Einschätzung basiert noch auf begrenzter Evidenz und kann sich deutlich verändern."
            }

        ProgressConfidence.DEVELOPING ->
            if (showEnglish) {
                "Your progress picture is becoming more stable as DeutschCraft collects more evidence."
            } else {
                "Dein Fortschrittsbild wird stabiler, während DeutschCraft weitere Evidenz sammelt."
            }

        ProgressConfidence.RELIABLE ->
            if (showEnglish) {
                "Your progress estimate is supported by a substantial amount of analyzed evidence."
            } else {
                "Deine Fortschrittseinschätzung wird durch eine umfangreiche Menge analysierter Evidenz gestützt."
            }
    }
}

private val PANEL_RADIUS =
    28.dp

private val PANEL_PADDING =
    20.dp

private val PANEL_TITLE_SPACING =
    10.dp

private val SKILL_LIST_TOP_SPACING =
    20.dp

private val SKILL_ROW_SPACING =
    18.dp

private val SKILL_PROGRESS_TOP_PADDING =
    8.dp

private val SKILL_PROGRESS_HEIGHT =
    8.dp

private val CONFIDENCE_NAME_TOP_PADDING =
    4.dp

private val CONFIDENCE_DESCRIPTION_TOP_PADDING =
    8.dp

private const val SKILL_PROGRESS_ANIMATION_MILLIS =
    850

private const val LANGUAGE_ANIMATION_MILLIS =
    180
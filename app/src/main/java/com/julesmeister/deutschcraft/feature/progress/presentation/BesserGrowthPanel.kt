// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/BesserGrowthPanel.kt

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
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus

@Composable
internal fun BesserGrowthPanel(
    progress:
    BesserProgressUiModel,

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
                BESSER_PANEL_RADIUS
            ),

        color =
            colors.growthPanelBackground,

        tonalElevation =
            0.dp,

        shadowElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    BESSER_PANEL_PADDING
                )
        ) {
            BesserGrowthHeader(
                progress =
                    progress,

                showEnglish =
                    showEnglish,

                colors =
                    colors
            )

            Spacer(
                modifier =
                    Modifier.height(
                        BESSER_SECTION_SPACING
                    )
            )

            if (!progress.hasEvidence) {
                BesserEmptyEvidence(
                    showEnglish =
                        showEnglish,

                    colors =
                        colors
                )
            } else {
                BesserMasterySummary(
                    progress =
                        progress,

                    showEnglish =
                        showEnglish,

                    colors =
                        colors
                )

                if (
                    progress.categories.isNotEmpty()
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                BESSER_SECTION_SPACING
                            )
                    )

                    Text(
                        text =
                            if (showEnglish) {
                                "Communication areas"
                            } else {
                                "Kommunikationsbereiche"
                            },

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            colors.textSecondary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                BESSER_SUBSECTION_SPACING
                            )
                    )

                    progress.categories
                        .take(
                            CATEGORY_DISPLAY_LIMIT
                        )
                        .forEachIndexed { index, category ->
                            BesserCategoryProgressRow(
                                category =
                                    category,

                                showEnglish =
                                    showEnglish,

                                colors =
                                    colors
                            )

                            if (
                                index <
                                minOf(
                                    progress.categories.size,
                                    CATEGORY_DISPLAY_LIMIT
                                ) -
                                1
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            BESSER_ROW_SPACING
                                        )
                                )
                            }
                        }
                }

                if (progress.hasRecommendations) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                BESSER_SECTION_SPACING
                            )
                    )

                    Text(
                        text =
                            if (showEnglish) {
                                "Ready for next"
                            } else {
                                "Als Nächstes"
                            },

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            colors.textSecondary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                BESSER_SUBSECTION_SPACING
                            )
                    )

                    progress
                        .recommendedNextCapabilities
                        .forEachIndexed { index, capability ->
                            BesserNextCapabilityCard(
                                capability =
                                    capability,

                                showEnglish =
                                    showEnglish,

                                colors =
                                    colors
                            )

                            if (
                                index <
                                progress
                                    .recommendedNextCapabilities
                                    .lastIndex
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            BESSER_ROW_SPACING
                                        )
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun BesserGrowthHeader(
    progress:
    BesserProgressUiModel,

    showEnglish: Boolean,

    colors:
    ProgressColors
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
                        BESSER_HEADER_ICON_SIZE
                    )
                    .clip(
                        RoundedCornerShape(
                            BESSER_HEADER_ICON_RADIUS
                        )
                    )
                    .background(
                        colors.growthAccentContainer
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
                    colors.growthAccent
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    BESSER_HEADER_SPACING
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
                        "Communication growth"
                    } else {
                        "Kommunikative Entwicklung"
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
                        "Besser and targeted mission evidence"
                    } else {
                        "Evidenz aus Besser und gezielten Missionen"
                    },

                modifier =
                    Modifier.padding(
                        top =
                            BESSER_HEADER_DESCRIPTION_TOP_PADDING
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    colors.textSecondary
            )
        }

        if (progress.hasEvidence) {
            Text(
                text =
                    "${progress.safeOverallMasteryPercentage}%",

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.growthAccent
            )
        }
    }
}

@Composable
private fun BesserEmptyEvidence(
    showEnglish: Boolean,
    colors: ProgressColors
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        BESSER_INNER_RADIUS
                    )
                )
                .background(
                    colors.growthItemBackground
                )
                .padding(
                    BESSER_INNER_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        BESSER_EMPTY_ICON_CONTAINER_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        colors.growthAccentContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .School,

                contentDescription =
                    null,

                tint =
                    colors.growthAccent
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    BESSER_EMPTY_ICON_SPACING
                )
        )

        Text(
            text =
                if (showEnglish) {
                    "Besser practice and targeted missions " +
                            "will build your capability map."
                } else {
                    "Besser-Übungen und gezielte Missionen " +
                            "bauen deine Fähigkeitskarte auf."
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

@Composable
private fun BesserMasterySummary(
    progress:
    BesserProgressUiModel,

    showEnglish: Boolean,

    colors:
    ProgressColors
) {
    val animatedMastery by
    animateFloatAsState(
        targetValue =
            progress
                .safeOverallMasteryPercentage /
                    100f,

        animationSpec =
            tween(
                durationMillis =
                    BESSER_PROGRESS_ANIMATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "BesserOverallMastery"
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        BESSER_INNER_RADIUS
                    )
                )
                .background(
                    colors.growthItemBackground
                )
                .padding(
                    BESSER_INNER_PADDING
                )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    if (showEnglish) {
                        "Observed mastery"
                    } else {
                        "Beobachtete Beherrschung"
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.Medium,

                color =
                    colors.textPrimary
            )

            Text(
                text =
                    "${progress.safeOverallMasteryPercentage}%",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.growthAccent
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
                            BESSER_PROGRESS_TOP_PADDING
                    )
                    .height(
                        BESSER_PROGRESS_HEIGHT
                    )
                    .clip(
                        CircleShape
                    ),

            color =
                colors.growthAccent,

            trackColor =
                colors.growthProgressTrack
        )

        Spacer(
            modifier =
                Modifier.height(
                    BESSER_SUMMARY_SPACING
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceAround
        ) {
            BesserSummaryMetric(
                value =
                    progress.masteredCapabilityCount,

                label =
                    if (showEnglish) {
                        "Mastered"
                    } else {
                        "Sicher"
                    },

                colors =
                    colors
            )

            BesserSummaryMetric(
                value =
                    progress.practisingCapabilityCount,

                label =
                    if (showEnglish) {
                        "Practising"
                    } else {
                        "In Übung"
                    },

                colors =
                    colors
            )

            BesserSummaryMetric(
                value =
                    progress.developingCapabilityCount,

                label =
                    if (showEnglish) {
                        "Developing"
                    } else {
                        "Im Aufbau"
                    },

                colors =
                    colors
            )
        }
    }
}

@Composable
private fun BesserSummaryMetric(
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
                colors.growthAccent
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
private fun BesserCategoryProgressRow(
    category:
    BesserCategoryProgressUiModel,

    showEnglish: Boolean,

    colors:
    ProgressColors
) {
    val animatedProgress by
    animateFloatAsState(
        targetValue =
            category
                .safeMasteryPercentage /
                    100f,

        animationSpec =
            tween(
                durationMillis =
                    BESSER_PROGRESS_ANIMATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "BesserCategory_${category.category}"
    )

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    if (showEnglish) {
                        category.displayNameEnglish
                    } else {
                        category.displayNameGerman
                    },

                modifier =
                    Modifier.weight(
                        1f
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

            Text(
                text =
                    "${category.safeMasteryPercentage}%",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.growthAccent
            )
        }

        LinearProgressIndicator(
            progress = {
                animatedProgress
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            BESSER_CATEGORY_PROGRESS_TOP_PADDING
                    )
                    .height(
                        BESSER_CATEGORY_PROGRESS_HEIGHT
                    )
                    .clip(
                        CircleShape
                    ),

            color =
                colors.growthAccent,

            trackColor =
                colors.growthProgressTrack
        )

        Text(
            text =
                if (showEnglish) {
                    "${category.attemptedCapabilityCount} of " +
                            "${category.capabilityCount} observed"
                } else {
                    "${category.attemptedCapabilityCount} von " +
                            "${category.capabilityCount} beobachtet"
                },

            modifier =
                Modifier.padding(
                    top =
                        BESSER_CATEGORY_METADATA_TOP_PADDING
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

@Composable
private fun BesserNextCapabilityCard(
    capability:
    BesserCapabilityProgressUiModel,

    showEnglish: Boolean,

    colors:
    ProgressColors
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        BESSER_NEXT_CARD_RADIUS
                    )
                )
                .background(
                    colors.growthItemBackground
                )
                .padding(
                    BESSER_NEXT_CARD_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        BESSER_NEXT_ICON_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        colors.growthAccentContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    if (
                        capability
                            .isDirectSuccessorOfMasteredCapability
                    ) {
                        Icons
                            .Outlined
                            .Route
                    } else {
                        Icons
                            .Outlined
                            .CheckCircle
                    },

                contentDescription =
                    null,

                tint =
                    colors.growthAccent
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    BESSER_NEXT_ICON_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        capability.name,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

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

                Text(
                    text =
                        capability
                            .cefrLevel
                            .uppercase(),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors.growthAccent
                )
            }

            Text(
                text =
                    capability.description,

                modifier =
                    Modifier.padding(
                        top =
                            BESSER_NEXT_DESCRIPTION_TOP_PADDING
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    colors.textSecondary,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    capabilityStatusText(
                        capability =
                            capability,

                        showEnglish =
                            showEnglish
                    ),

                modifier =
                    Modifier.padding(
                        top =
                            BESSER_NEXT_STATUS_TOP_PADDING
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    colors.growthAccent
            )
        }
    }
}

private fun capabilityStatusText(
    capability:
    BesserCapabilityProgressUiModel,

    showEnglish: Boolean
): String {

    return when {
        capability
            .isDirectSuccessorOfMasteredCapability -> {
            if (showEnglish) {
                "Recommended next step"
            } else {
                "Empfohlener nächster Schritt"
            }
        }

        capability.progressionReady &&
                capability.hasPrerequisites -> {
            if (showEnglish) {
                "Ready to practise · " +
                        "${capability.safePrerequisiteMasteryPercentage}% " +
                        "prerequisite mastery"
            } else {
                "Bereit zum Üben · " +
                        "${capability.safePrerequisiteMasteryPercentage}% " +
                        "Voraussetzungsbeherrschung"
            }
        }

        capability.progressionReady -> {
            if (showEnglish) {
                "Ready to practise"
            } else {
                "Bereit zum Üben"
            }
        }

        else -> {
            if (showEnglish) {
                "Developing prerequisites"
            } else {
                "Voraussetzungen im Aufbau"
            }
        }
    }
}

private val BESSER_PANEL_RADIUS =
    28.dp

private val BESSER_PANEL_PADDING =
    20.dp

private val BESSER_HEADER_ICON_SIZE =
    46.dp

private val BESSER_HEADER_ICON_RADIUS =
    15.dp

private val BESSER_HEADER_SPACING =
    12.dp

private val BESSER_HEADER_DESCRIPTION_TOP_PADDING =
    3.dp

private val BESSER_SECTION_SPACING =
    20.dp

private val BESSER_SUBSECTION_SPACING =
    14.dp

private val BESSER_ROW_SPACING =
    16.dp

private val BESSER_INNER_RADIUS =
    18.dp

private val BESSER_INNER_PADDING =
    16.dp

private val BESSER_EMPTY_ICON_CONTAINER_SIZE =
    40.dp

private val BESSER_EMPTY_ICON_SPACING =
    12.dp

private val BESSER_PROGRESS_TOP_PADDING =
    10.dp

private val BESSER_PROGRESS_HEIGHT =
    7.dp

private val BESSER_SUMMARY_SPACING =
    16.dp

private val BESSER_CATEGORY_PROGRESS_TOP_PADDING =
    8.dp

private val BESSER_CATEGORY_PROGRESS_HEIGHT =
    6.dp

private val BESSER_CATEGORY_METADATA_TOP_PADDING =
    5.dp

private val BESSER_NEXT_CARD_RADIUS =
    18.dp

private val BESSER_NEXT_CARD_PADDING =
    14.dp

private val BESSER_NEXT_ICON_SIZE =
    40.dp

private val BESSER_NEXT_ICON_SPACING =
    12.dp

private val BESSER_NEXT_DESCRIPTION_TOP_PADDING =
    4.dp

private val BESSER_NEXT_STATUS_TOP_PADDING =
    8.dp

private const val BESSER_PROGRESS_ANIMATION_MILLIS =
    850

private const val CATEGORY_DISPLAY_LIMIT =
    5
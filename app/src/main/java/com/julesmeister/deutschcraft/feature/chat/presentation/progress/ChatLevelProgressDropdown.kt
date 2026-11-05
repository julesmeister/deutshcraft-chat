// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressDropdown.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.component.CradledDropdown

@Composable
internal fun ChatLevelProgressDropdown(
    state: ChatLevelProgressUiState,
    expanded: Boolean,
    onDismiss: () -> Unit,
    anchorContent: @Composable () -> Unit
) {
    CradledDropdown(
        expanded = expanded,
        onDismiss = onDismiss,
        anchorContent = anchorContent,
        anchorDescription = "Close level progress"
    ) {
        ProgressBentoContent(state)
    }
}

@Composable
private fun ColumnScope.ProgressBentoContent(
    state: ChatLevelProgressUiState
) {
    val accent =
        readinessColor(state.safeReadinessPercentage)

    val need =
        state.immediateNeed

    ProgressHeaderBento(
        state = state,
        accent = accent
    )

    if (need == null) {
        EmptyNeedBento(accent = accent)
    } else {
        PrimaryNeedBento(
            need = need,
            accent = accent
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChatDesignTokens.LevelProgressDropdown.BentoGap)
        ) {
            TeachingTargetBento(
                label = "SAY IT WITH",
                values = need.grammarTargets,
                accent = accent,
                modifier = Modifier.weight(1f)
            )

            TeachingTargetBento(
                label = "USE MORE",
                values = need.vocabularyTargets,
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        }

        ExampleBento(
            phrase = need.examplePhrase,
            accent = accent
        )
    }
}

@Composable
private fun ProgressHeaderBento(
    state: ChatLevelProgressUiState,
    accent: Color
) {
    val colors =
        MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ChatDesignTokens.LevelProgressDropdown.HeaderShape)
                .background(accent.copy(alpha = ChatDesignTokens.LevelProgressDropdown.HeaderBackgroundAlpha))
                .padding(ChatDesignTokens.LevelProgressDropdown.HeaderPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = state.progressTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ChatDesignTokens.LevelProgressDropdown.ReadinessTrackHeight)
                        .clip(CircleShape)
                        .background(
                            colors.surface.copy(alpha = ChatDesignTokens.LevelProgressDropdown.ReadinessTrackAlpha)
                        )
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(
                                fraction = state.safeReadinessPercentage / 100f
                            )
                            .height(ChatDesignTokens.LevelProgressDropdown.ReadinessTrackHeight)
                            .clip(CircleShape)
                            .background(accent)
                )
            }
        }

        Spacer(modifier = Modifier.width(ChatDesignTokens.LevelProgressDropdown.HeaderContentSpacing))

        Text(
            text =
                if (state.isHighestLevel) {
                    "C2"
                } else {
                    "${state.safeReadinessPercentage}%"
                },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
    }
}

@Composable
private fun PrimaryNeedBento(
    need: ChatImmediateLevelNeedUiModel,
    accent: Color
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ChatDesignTokens.LevelProgressDropdown.PrimaryNeedShape)
                .background(colors.surfaceContainer)
                .border(
                    width = ChatDesignTokens.LevelProgressDropdown.PrimaryNeedBorderWidth,
                    color = accent.copy(alpha = ChatDesignTokens.LevelProgressDropdown.PrimaryNeedBorderAlpha),
                    shape = ChatDesignTokens.LevelProgressDropdown.PrimaryNeedShape
                )
                .padding(ChatDesignTokens.LevelProgressDropdown.PrimaryNeedPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEXT BEST STEP",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            Text(
                text = need.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant
            )
        }

        Text(
            text = need.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Text(
            text = need.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(ChatDesignTokens.LevelProgressDropdown.WhyNowShape)
                    .background(accent.copy(alpha = ChatDesignTokens.LevelProgressDropdown.WhyNowBackgroundAlpha))
                    .padding(ChatDesignTokens.LevelProgressDropdown.WhyNowPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "WHY NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )

                Text(
                    text = need.whyNow,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun TeachingTargetBento(
    label: String,
    values: List<String>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .height(ChatDesignTokens.LevelProgressDropdown.TeachingBentoHeight)
                .clip(ChatDesignTokens.LevelProgressDropdown.SmallBentoShape)
                .background(colors.surfaceContainer)
                .padding(ChatDesignTokens.LevelProgressDropdown.SmallBentoPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        values
            .take(ChatDesignTokens.LevelProgressDropdown.MaximumTargetCount)
            .forEach { value ->
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(top = ChatDesignTokens.LevelProgressDropdown.TargetDotTopPadding)
                                .size(ChatDesignTokens.LevelProgressDropdown.TargetDotSize)
                                .clip(CircleShape)
                                .background(accent)
                    )

                    Spacer(modifier = Modifier.width(ChatDesignTokens.LevelProgressDropdown.TargetDotSpacing))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
    }
}

@Composable
private fun ExampleBento(
    phrase: String,
    accent: Color
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ChatDesignTokens.LevelProgressDropdown.ExampleShape)
                .background(accent.copy(alpha = ChatDesignTokens.LevelProgressDropdown.ExampleBackgroundAlpha))
                .padding(ChatDesignTokens.LevelProgressDropdown.ExamplePadding),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "TRY SAYING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        Text(
            text = "„$phrase“",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyNeedBento(
    accent: Color
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ChatDesignTokens.LevelProgressDropdown.PrimaryNeedShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(ChatDesignTokens.LevelProgressDropdown.PrimaryNeedPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "BUILDING YOUR NEXT STEP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        Text(
            text =
                "Keep chatting naturally. DeutschCraft is collecting " +
                        "enough evidence to identify one specific " +
                        "next-level requirement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun readinessColor(
    readiness: Int
): Color {
    val progress =
        readiness.coerceIn(0, 100) / 100f

    return if (progress <= ChatDesignTokens.LevelProgress.ColorMidpoint) {
        lerp(
            start = ChatDesignTokens.LevelProgress.Orange,
            stop = ChatDesignTokens.LevelProgress.Amber,
            fraction = progress / ChatDesignTokens.LevelProgress.ColorMidpoint
        )
    } else {
        lerp(
            start = ChatDesignTokens.LevelProgress.Amber,
            stop = ChatDesignTokens.LevelProgress.Green,
            fraction = (progress - ChatDesignTokens.LevelProgress.ColorMidpoint) / ChatDesignTokens.LevelProgress.ColorMidpoint
        )
    }
}

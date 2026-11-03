package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionGrammar
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionVocabulary
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceGoal
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.supportVisibility
import com.julesmeister.deutschcraft.feature.settings.presentation.LocalUserSettings
import com.julesmeister.deutschcraft.ui.theme.DeutschInk
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

private typealias DetailTokens = ChatDesignTokens.MissionDrawer

@Immutable
private data class MissionDetailColors(
    val accent: Color,
    val accentFill: Color,
    val onAccentFill: Color,
    val accentContainer: Color,
    val card: Color,
    val track: Color,
    val muted: Color
)

@Composable
private fun rememberMissionDetailColors(): MissionDetailColors {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < ChatDesignTokens.DarkLuminanceThreshold
    return MissionDetailColors(
        accent = if (isDark) DeutschLime else ChatDesignTokens.MissionLightAccent,
        accentFill = DeutschLime,
        onAccentFill = DeutschInk,
        accentContainer = DeutschLime.copy(
            alpha = if (isDark) DetailTokens.AccentContainerDarkAlpha else DetailTokens.AccentContainerLightAlpha
        ),
        card = scheme.surfaceContainer,
        track = scheme.surfaceContainerHighest,
        muted = scheme.onSurfaceVariant
    )
}

@Composable
internal fun MissionTopBarDetails(
    state: MissionGuidanceUiState,
    onRevealNextHint: () -> Unit,
    onRetryLanguageSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalUserSettings.current
    val support = state.supportVisibility(settings)
    val colors = rememberMissionDetailColors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(DetailTokens.ContentPadding),
        verticalArrangement = Arrangement.spacedBy(DetailTokens.SectionSpacing)
    ) {
        MissionHero(state, colors)
        CurrentGoalCard(state, colors)
        if (state.objectives.isNotEmpty()) {
            DetailSection("Mission goals", Icons.Outlined.Route, colors) {
                GoalTimeline(state.objectives, state.objectiveId, colors)
            }
        }
        if (support.hints && (state.revealedHints.isNotEmpty() || state.hasMoreHints)) {
            HintCard(state, colors, onRevealNextHint)
        }
        if (settings.showTargetVocabulary || settings.showExamplePhrases) {
            if (state.isLoadingLanguageSupport) LanguageSupportSkeleton(colors)
            state.languageSupportError?.let { ErrorCard(it, onRetryLanguageSupport) }
            state.languageSupport?.let { language ->
                if (settings.showTargetVocabulary && language.vocabulary.isNotEmpty()) {
                    DetailSection("Useful vocabulary", Icons.Outlined.Translate, colors) {
                        VocabularyCarousel(language.vocabulary, settings.showExamplePhrases, colors)
                    }
                }
                if (settings.showExamplePhrases && language.grammar.isNotEmpty()) {
                    DetailSection("Grammar for this mission", Icons.Outlined.AutoStories, colors) {
                        language.grammar.forEachIndexed { index, grammar -> GrammarCard(index + 1, grammar, colors) }
                    }
                }
            }
            if (settings.showTargetVocabulary && state.languageSupport == null && state.targetVocabulary.isNotEmpty()) {
                DetailSection("Current-goal vocabulary", Icons.Outlined.Translate, colors) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)
                    ) { state.targetVocabulary.forEach { WordChip(it, colors) } }
                }
            }
            if (settings.showExamplePhrases && state.examplePhrases.isNotEmpty()) {
                DetailSection("Current-goal examples", Icons.Outlined.FormatQuote, colors) {
                    state.examplePhrases.forEach { QuoteLine(it, colors) }
                }
            }
        }
        state.errorMessage?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MissionHero(state: MissionGuidanceUiState, colors: MissionDetailColors) {
    val completed = state.objectives.count { it.isCompleted }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DetailTokens.CardPadding)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(DetailTokens.ProgressRingSize)) {
            CircularProgressIndicator(
                progress = { state.safeProgressPercentage / 100f },
                modifier = Modifier.fillMaxSize(),
                color = colors.accentFill,
                trackColor = colors.track,
                strokeWidth = DetailTokens.ProgressRingStroke,
                strokeCap = StrokeCap.Round
            )
            Text(
                "${state.safeProgressPercentage}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Mission details",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                    modifier = Modifier.weight(1f)
                )
                state.cefrLevel?.let { LevelBadge(it.name, colors) }
            }
            if (state.missionTitle.isNotBlank()) {
                Text(
                    state.missionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.scenario.isNotBlank()) {
                Text(
                    state.scenario,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.objectives.isNotEmpty()) {
                Text(
                    "$completed of ${state.objectives.size} goals complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(level: String, colors: MissionDetailColors) {
    Text(
        level,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = colors.onAccentFill,
        modifier = Modifier
            .background(colors.accentFill, CircleShape)
            .padding(horizontal = DetailTokens.BadgeHorizontalPadding, vertical = DetailTokens.BadgeVerticalPadding)
    )
}

@Composable
private fun CurrentGoalCard(state: MissionGuidanceUiState, colors: MissionDetailColors) {
    Surface(
        color = colors.accentContainer,
        shape = RoundedCornerShape(DetailTokens.CardCornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DetailTokens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing / 2)
        ) {
            SectionHeader("Current goal", Icons.Outlined.Flag, colors)
            Text(state.objectiveTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (state.objectiveDescription.isNotBlank()) {
                Text(state.objectiveDescription, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    colors: MissionDetailColors,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)) {
        SectionHeader(title, icon, colors)
        content()
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, colors: MissionDetailColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing / 2)
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(DetailTokens.SectionIconSize))
        Text(title, style = MaterialTheme.typography.labelLarge, color = colors.accent, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoalTimeline(goals: List<MissionGuidanceGoal>, currentId: String?, colors: MissionDetailColors) {
    Column {
        goals.forEachIndexed { index, goal ->
            val isCurrent = goal.id == currentId && !goal.isCompleted
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(DetailTokens.StepDotSize).fillMaxHeight()
                ) {
                    StepDot(index + 1, goal.isCompleted, isCurrent, colors)
                    if (index < goals.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(DetailTokens.StepLineWidth)
                                .weight(1f)
                                .heightIn(min = DetailTokens.StepLineMinHeight)
                                .background(if (goal.isCompleted) colors.accentFill else colors.track)
                        )
                    }
                }
                Text(
                    goal.title,
                    style = if (isCurrent) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (goal.isCompleted || isCurrent) MaterialTheme.colorScheme.onSurface else colors.muted,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = DetailTokens.ItemSpacing + DetailTokens.ItemSpacing / 2)
                        .padding(vertical = DetailTokens.StepVerticalPadding)
                        .alpha(if (goal.isCompleted || isCurrent) 1f else DetailTokens.UpcomingStepAlpha)
                )
            }
        }
    }
}

@Composable
private fun StepDot(number: Int, completed: Boolean, current: Boolean, colors: MissionDetailColors) {
    val base = Modifier.size(DetailTokens.StepDotSize).background(
        when {
            completed -> colors.accentFill
            current -> colors.accentContainer
            else -> MaterialTheme.colorScheme.background
        },
        CircleShape
    )
    val bordered = if (completed) base else base.border(
        DetailTokens.StepLineWidth,
        if (current) colors.accent else colors.track,
        CircleShape
    )
    Box(contentAlignment = Alignment.Center, modifier = bordered) {
        if (completed) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = "Completed",
                tint = colors.onAccentFill,
                modifier = Modifier.size(DetailTokens.StepIconSize)
            )
        } else {
            Text(
                "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (current) colors.accent else colors.muted
            )
        }
    }
}

@Composable
private fun HintCard(state: MissionGuidanceUiState, colors: MissionDetailColors, onRevealNextHint: () -> Unit) {
    val total = state.coachingHints.size.coerceAtMost(MissionGuidanceUiState.MAXIMUM_HINT_COUNT)
    DetailCard(colors) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { SectionHeader("Hints", Icons.Outlined.Lightbulb, colors) }
            Text("${state.revealedHintCount}/$total", style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
        state.revealedHints.forEachIndexed { index, hint ->
            Row(horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)) {
                Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = colors.accent, fontWeight = FontWeight.Bold)
                Text(hint, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (state.hasMoreHints) {
            FilledTonalButton(onClick = onRevealNextHint, enabled = !state.isRevealingHint) {
                Text(if (state.revealedHints.isEmpty()) "Show a hint" else "Show another hint")
            }
        }
    }
}

@Composable
private fun VocabularyCarousel(words: List<MissionVocabulary>, showExamples: Boolean, colors: MissionDetailColors) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)
    ) {
        words.forEach { word ->
            Surface(
                color = colors.card,
                shape = RoundedCornerShape(DetailTokens.CardCornerRadius),
                modifier = Modifier.width(DetailTokens.VocabularyCardWidth).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(DetailTokens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing / 2)
                ) {
                    Text(word.word, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.accent)
                    Text(word.meaning, style = MaterialTheme.typography.bodySmall)
                    if (showExamples) {
                        Text(
                            word.example,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = colors.muted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarCard(number: Int, grammar: MissionGrammar, colors: MissionDetailColors) {
    Surface(
        color = colors.card,
        shape = RoundedCornerShape(DetailTokens.CardCornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DetailTokens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing + DetailTokens.ItemSpacing / 2)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(DetailTokens.GrammarBadgeSize)
                        .background(colors.accentContainer, RoundedCornerShape(DetailTokens.GrammarBadgeCornerRadius))
                ) {
                    Text(
                        "$number",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
                Text(
                    grammar.pattern,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(grammar.explanation, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(DetailTokens.CardCornerRadius / 2))
                    .padding(DetailTokens.ItemSpacing + DetailTokens.ItemSpacing / 2)
            ) {
                Icon(
                    Icons.Outlined.FormatQuote,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(DetailTokens.SectionIconSize)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Beispiel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                    Text(grammar.example, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
private fun QuoteLine(text: String, colors: MissionDetailColors) {
    Text(
        "„$text“",
        style = MaterialTheme.typography.bodySmall,
        fontStyle = FontStyle.Italic,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accentContainer, RoundedCornerShape(DetailTokens.CardCornerRadius / 2))
            .padding(horizontal = DetailTokens.ItemSpacing + DetailTokens.ItemSpacing / 2, vertical = DetailTokens.ItemSpacing)
    )
}

@Composable
private fun WordChip(text: String, colors: MissionDetailColors) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .background(colors.card, CircleShape)
            .padding(horizontal = DetailTokens.CardPadding, vertical = DetailTokens.ItemSpacing)
    )
}

@Composable
private fun DetailCard(colors: MissionDetailColors, content: @Composable () -> Unit) {
    Surface(color = colors.card, shape = RoundedCornerShape(DetailTokens.CardCornerRadius), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(DetailTokens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing)
        ) { content() }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(DetailTokens.CardCornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing),
            modifier = Modifier.padding(start = DetailTokens.CardPadding)
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(DetailTokens.SectionIconSize)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(DetailTokens.SectionIconSize))
                Spacer(Modifier.width(DetailTokens.ItemSpacing / 2))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LanguageSupportSkeleton(colors: MissionDetailColors) {
    val shimmerAlpha = if (LocalUserSettings.current.reduceAnimations) {
        DetailTokens.ShimmerMinAlpha
    } else {
        rememberInfiniteTransition(label = "mission-support-shimmer").animateFloat(
            initialValue = DetailTokens.ShimmerMinAlpha,
            targetValue = DetailTokens.ShimmerMaxAlpha,
            animationSpec = infiniteRepeatable(tween(DetailTokens.ShimmerMillis, easing = LinearEasing), RepeatMode.Reverse),
            label = "mission-support-shimmer-alpha"
        ).value
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing),
        modifier = Modifier.alpha(shimmerAlpha)
    ) {
        Text(
            "Preparing vocabulary and grammar for your level…",
            style = MaterialTheme.typography.labelMedium,
            color = colors.muted
        )
        repeat(DetailTokens.SkeletonCardCount) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DetailTokens.ItemSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DetailTokens.SkeletonCardHeight)
                    .background(colors.card, RoundedCornerShape(DetailTokens.CardCornerRadius))
                    .padding(DetailTokens.CardPadding)
            ) {
                SkeletonLine(colors, 1f)
                SkeletonLine(colors, DetailTokens.SkeletonShortLineFraction)
            }
        }
    }
}

@Composable
private fun SkeletonLine(colors: MissionDetailColors, widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(DetailTokens.SkeletonLineHeight)
            .background(colors.track, CircleShape)
    )
}

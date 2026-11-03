package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.supportVisibility
import com.julesmeister.deutschcraft.feature.settings.presentation.LocalUserSettings

@Composable
internal fun MissionTopBarDetails(
    state: MissionGuidanceUiState,
    onRevealNextHint: () -> Unit,
    onRetryLanguageSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalUserSettings.current
    val support = state.supportVisibility(settings)
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(ChatDesignTokens.TopBar.MissionDetailPadding),
        verticalArrangement = Arrangement.spacedBy(ChatDesignTokens.TopBar.MissionDetailSpacing)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailLabel("Mission details")
            Text(
                text = listOfNotNull(state.cefrLevel?.name, "${state.safeProgressPercentage}%").joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.scenario.isNotBlank()) DetailText(state.scenario)
        LinearProgressIndicator(
            progress = { state.safeProgressPercentage / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        DetailLabel("Current goal")
        Text(state.objectiveTitle, style = MaterialTheme.typography.titleSmall)
        if (state.objectiveDescription.isNotBlank()) DetailText(state.objectiveDescription)
        if (state.objectives.isNotEmpty()) {
            DetailLabel("Mission goals")
            state.objectives.forEachIndexed { index, goal ->
                DetailText(
                    "${if (goal.isCompleted) "✓" else "${index + 1}."} ${goal.title}",
                    current = goal.id == state.objectiveId
                )
            }
        }
        if (support.hints) {
            state.revealedHints.forEach { DetailText(it) }
            if (state.hasMoreHints) {
                TextButton(onClick = onRevealNextHint, enabled = !state.isRevealingHint) {
                    Text(if (state.revealedHints.isEmpty()) "Show a hint" else "Show another hint")
                }
            }
        }
        if (settings.showTargetVocabulary || settings.showExamplePhrases) {
            if (state.isLoadingLanguageSupport) {
                DetailText("Preparing language support for this mission…")
            }
            state.languageSupportError?.let { error ->
                TextButton(onClick = onRetryLanguageSupport) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            state.languageSupport?.let { language ->
                if (settings.showTargetVocabulary) {
                    DetailLabel("Useful vocabulary")
                    language.vocabulary.forEach { word ->
                        Text("${word.word} — ${word.meaning}", style = MaterialTheme.typography.bodyMedium)
                        if (settings.showExamplePhrases) DetailText(word.example)
                    }
                }
                if (settings.showExamplePhrases) {
                    DetailLabel("Grammar for this mission")
                    language.grammar.forEach { grammar ->
                        Text(grammar.pattern, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        DetailText(grammar.explanation)
                        DetailText(grammar.example)
                    }
                }
            }
            if (settings.showTargetVocabulary && state.languageSupport == null && state.targetVocabulary.isNotEmpty()) {
                DetailLabel("Current-goal vocabulary")
                state.targetVocabulary.forEach { DetailText(it) }
            }
            if (settings.showExamplePhrases && state.examplePhrases.isNotEmpty()) {
                DetailLabel("Current-goal examples")
                state.examplePhrases.forEach { DetailText(it) }
            }
        }
        state.errorMessage?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DetailText(text: String, current: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal,
        color = if (current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

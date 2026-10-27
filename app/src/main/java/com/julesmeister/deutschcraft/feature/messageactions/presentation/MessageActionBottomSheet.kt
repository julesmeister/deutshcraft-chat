package com.julesmeister.deutschcraft.feature.messageactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceAnalysisType
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.feature.phrasemarking.presentation.PhraseMarkingSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionBottomSheet(
    sentence: String,
    selectedAction: SentenceAnalysisType?,
    insight: SentenceInsight?,
    isLoading: Boolean,
    errorMessage: String?,
    onActionClick: (SentenceAnalysisType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,

    /*
     * True when sentence is the Besser version of a learner
     * message rather than the original text.
     */
    isCorrectedVersion: Boolean = false,

    /*
     * Phrases already marked on this message. They are
     * underlined in the highlighter.
     */
    markedPhrases: List<MarkedPhrase> = emptyList(),

    /*
     * Called when the learner highlights a phrase and picks
     * a mark type in the dialog.
     */
    onMarkPhrase: (phrase: String, type: MarkedPhraseType) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp
        ),
        containerColor =
            MaterialTheme.colorScheme.surfaceContainer,
        contentColor =
            MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(
                        width = 42.dp,
                        height = 4.dp
                    )
                    .background(
                        color = MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                            .copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            /*
             * Selectable sentence.
             *
             * Tap words to highlight a phrase, then mark it as
             * a difficult word or grammar to understand.
             */
            PhraseMarkingSection(
                sentence = sentence,
                isCorrectedVersion = isCorrectedVersion,
                markedPhrases = markedPhrases,
                onMark = onMarkPhrase
            )

            Text(
                text = "Learn from this sentence",
                modifier = Modifier.padding(
                    top = 24.dp,
                    bottom = 12.dp
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            MessageActionRow(
                title = "Translate",
                supportingText =
                    "See the meaning of the entire sentence.",
                isSelected =
                    selectedAction == SentenceAnalysisType.TRANSLATE,
                enabled = !isLoading,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = null
                    )
                },
                onClick = {
                    onActionClick(SentenceAnalysisType.TRANSLATE)
                }
            )

            MessageActionRow(
                title = "Explain",
                supportingText =
                    "Understand how the sentence expresses its meaning.",
                isSelected =
                    selectedAction == SentenceAnalysisType.EXPLAIN,
                enabled = !isLoading,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null
                    )
                },
                onClick = {
                    onActionClick(SentenceAnalysisType.EXPLAIN)
                }
            )

            MessageActionRow(
                title = "Rephrase",
                supportingText =
                    "See simpler, natural, and advanced alternatives.",
                isSelected =
                    selectedAction == SentenceAnalysisType.REPHRASE,
                enabled = !isLoading,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null
                    )
                },
                onClick = {
                    onActionClick(SentenceAnalysisType.REPHRASE)
                }
            )

            if (
                isLoading ||
                errorMessage != null ||
                (selectedAction != null && insight != null)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            when {
                isLoading -> {
                    MessageActionLoading()
                }

                errorMessage != null -> {
                    MessageActionError(message = errorMessage)
                }

                selectedAction != null && insight != null -> {
                    MessageActionResult(
                        selectedAction = selectedAction,
                        insight = insight
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageActionRow(
    title: String,
    supportingText: String,
    isSelected: Boolean,
    enabled: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val containerColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    val contentColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = contentColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )

                Text(
                    text = supportingText,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun MessageActionLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp
        )

        Text(
            text = "Analyzing sentence...",
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessageActionError(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun MessageActionResult(
    selectedAction: SentenceAnalysisType,
    insight: SentenceInsight
) {
    when (selectedAction) {
        SentenceAnalysisType.TRANSLATE -> {
            TranslationTab(translation = insight.translation)
        }

        SentenceAnalysisType.EXPLAIN -> {
            ExplanationTab(explanation = insight.explanation)
        }

        SentenceAnalysisType.REPHRASE -> {
            RephraseTab(insight = insight)
        }
    }
}
package com.julesmeister.deutschcraft.feature.phrasemarking.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

/*
 * Selectable study sentence.
 *
 * - Tap a word to select it
 * - Tap another word to extend the selection
 * - Tap inside the selection to clear it
 *
 * Once something is selected, two inline actions appear:
 *
 *   📖 Wort      🧩 Grammatik
 *
 * No dialog: marking takes two taps.
 */
@Composable
fun PhraseMarkingSection(
    sentence: String,
    isCorrectedVersion: Boolean,
    markedPhrases: List<MarkedPhrase>,
    onMark: (phrase: String, type: MarkedPhraseType) -> Unit,
    modifier: Modifier = Modifier
) {
    val words =
        remember(sentence) {
            WORD_REGEX
                .findAll(sentence)
                .map { match -> match.range }
                .toList()
        }

    var selection by
    remember(sentence) {
        mutableStateOf<IntRange?>(null)
    }

    val selectedPhrase: String? =
        selection?.let { range ->
            sentence.substring(
                words[range.first].first,
                words[range.last].last + 1
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DeutschSurfaceHigh)
                .border(
                    width = 0.75.dp,
                    color =
                        if (isCorrectedVersion) {
                            DeutschLime.copy(alpha = 0.22f)
                        } else {
                            DeutschTextSecondary.copy(alpha = 0.12f)
                        },
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            isCorrectedVersion = isCorrectedVersion,
            markedCount = markedPhrases.size
        )

        SelectablePhraseText(
            sentence = sentence,
            words = words,
            markedPhrases = markedPhrases.map { phrase -> phrase.phrase },
            selection = selection,
            onSelectionChange = { newSelection ->
                selection = newSelection
            }
        )

        AnimatedContent(
            targetState = selectedPhrase,
            transitionSpec = {
                (fadeIn(tween(220)) +
                        slideInVertically(tween(220)) { it / 4 }) togetherWith
                        fadeOut(tween(140))
            },
            label = "PhraseMarkingFooter"
        ) { phrase ->
            if (phrase == null) {
                Text(
                    text = "Tippe auf Wörter, die dir schwerfallen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeutschTextSecondary
                )
            } else {
                MarkActions(
                    phrase = phrase,
                    onMark = { type ->
                        onMark(phrase, type)
                        selection = null
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    isCorrectedVersion: Boolean,
    markedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =
                if (isCorrectedVersion) {
                    "✦ BESSER-VERSION"
                } else {
                    "SATZ"
                },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color =
                if (isCorrectedVersion) {
                    DeutschLime
                } else {
                    DeutschTextSecondary
                }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (markedCount > 0) {
            Text(
                text = "$markedCount markiert",
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DeutschLime.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = DeutschLime
            )
        }
    }
}

@Composable
private fun MarkActions(
    phrase: String,
    onMark: (MarkedPhraseType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "„$phrase“",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = DeutschTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarkActionButton(
                icon = "📖",
                label = "Wort",
                isPrimary = true,
                onClick = { onMark(MarkedPhraseType.DIFFICULT_WORD) },
                modifier = Modifier.weight(1f)
            )

            MarkActionButton(
                icon = "🧩",
                label = "Grammatik",
                isPrimary = false,
                onClick = { onMark(MarkedPhraseType.GRAMMAR) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MarkActionButton(
    icon: String,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier =
            modifier
                .clip(shape)
                .background(
                    if (isPrimary) {
                        DeutschLime
                    } else {
                        DeutschLime.copy(alpha = 0.10f)
                    }
                )
                .border(
                    width = 0.75.dp,
                    color =
                        if (isPrimary) {
                            DeutschLime
                        } else {
                            DeutschLime.copy(alpha = 0.35f)
                        },
                    shape = shape
                )
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color =
                if (isPrimary) {
                    DeutschTextOnLime
                } else {
                    DeutschLime
                }
        )
    }
}

@Composable
private fun SelectablePhraseText(
    sentence: String,
    words: List<IntRange>,
    markedPhrases: List<String>,
    selection: IntRange?,
    onSelectionChange: (IntRange?) -> Unit
) {
    var layoutResult by
    remember { mutableStateOf<TextLayoutResult?>(null) }

    val currentSelection by rememberUpdatedState(selection)
    val currentOnSelectionChange by rememberUpdatedState(onSelectionChange)

    val annotatedText =
        remember(sentence, words, markedPhrases, selection) {
            buildSentenceText(
                sentence = sentence,
                words = words,
                markedPhrases = markedPhrases,
                selection = selection
            )
        }

    Text(
        text = annotatedText,
        style =
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 30.sp
            ),
        color = DeutschTextPrimary,
        onTextLayout = { result -> layoutResult = result },
        modifier =
            Modifier.pointerInput(words) {
                detectTapGestures { position ->
                    val offset =
                        layoutResult
                            ?.getOffsetForPosition(position)
                            ?: return@detectTapGestures

                    val wordIndex =
                        words.indexOfFirst { range ->
                            offset >= range.first &&
                                    offset <= range.last + 1
                        }

                    currentOnSelectionChange(
                        if (wordIndex < 0) {
                            null
                        } else {
                            nextSelection(
                                current = currentSelection,
                                tappedIndex = wordIndex
                            )
                        }
                    )
                }
            }
    )
}

private fun nextSelection(
    current: IntRange?,
    tappedIndex: Int
): IntRange? {
    return when {
        current == null -> tappedIndex..tappedIndex
        tappedIndex in current -> null
        else -> {
            minOf(current.first, tappedIndex)..
                    maxOf(current.last, tappedIndex)
        }
    }
}

private fun buildSentenceText(
    sentence: String,
    words: List<IntRange>,
    markedPhrases: List<String>,
    selection: IntRange?
): AnnotatedString {
    return buildAnnotatedString {
        append(sentence)

        /*
         * Already-marked phrases: lime with an underline.
         */
        markedPhrases
            .filter(String::isNotBlank)
            .forEach { phrase ->
                var start = sentence.indexOf(phrase, ignoreCase = true)

                while (start >= 0) {
                    addStyle(
                        style =
                            SpanStyle(
                                color = DeutschLime,
                                textDecoration = TextDecoration.Underline
                            ),
                        start = start,
                        end = start + phrase.length
                    )

                    start =
                        sentence.indexOf(
                            string = phrase,
                            startIndex = start + phrase.length,
                            ignoreCase = true
                        )
                }
            }

        /*
         * Current selection: soft lime highlight.
         */
        if (selection != null) {
            addStyle(
                style =
                    SpanStyle(
                        background = DeutschLime.copy(alpha = 0.22f),
                        color = DeutschTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                start = words[selection.first].first,
                end = words[selection.last].last + 1
            )
        }
    }
}

private val WORD_REGEX =
    Regex("[\\p{L}\\p{N}'’-]+")
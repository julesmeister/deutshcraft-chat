package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun MessageBubbleContent(
    message: ChatMessage,
    correction: String?,
    isUserMessage: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val time =
        remember(message.timestamp) {
            formatMessageTime(
                timestamp = message.timestamp
            )
        }

    val timeStyle =
        MaterialTheme.typography.labelSmall

    val hasBesserCorrection =
        isUserMessage &&
                !correction.isNullOrBlank() &&
                correction.trim() != message.content.trim()

    val primaryText =
        if (hasBesserCorrection) {
            correction.orEmpty()
        } else {
            message.content
        }

    /*
     * The learner should repeatedly see the correct German.
     *
     * When Besser exists:
     *
     * - The corrected sentence becomes the primary text.
     * - The words changed by Besser are highlighted.
     *
     * Without Besser:
     *
     * - The ordinary message is shown normally.
     */
    val visiblePrimaryText =
        remember(
            primaryText,
            message.content,
            hasBesserCorrection,
            contentColor
        ) {
            if (hasBesserCorrection) {
                buildHighlightedCorrection(
                    original = message.content,
                    correction = primaryText,
                    baseColor = contentColor,
                    changedColor = contentColor,
                    highlightColor =
                        contentColor.copy(
                            alpha =
                                CORRECTION_HIGHLIGHT_ALPHA
                        )
                )
            } else {
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = contentColor
                        )
                    ) {
                        append(primaryText)
                    }
                }
            }
        }

    /*
     * Add invisible room for the timestamp.
     *
     * timeStyle.fontSize is read outside remember because
     * MaterialTheme values must be read in composition.
     */
    val primaryTextWithTimestampSpace =
        remember(
            visiblePrimaryText,
            time,
            timeStyle.fontSize
        ) {
            buildAnnotatedString {
                append(visiblePrimaryText)

                withStyle(
                    SpanStyle(
                        color = Color.Transparent,
                        fontSize = timeStyle.fontSize
                    )
                ) {
                    append(TIMESTAMP_RESERVATION_PREFIX)
                    append(time)
                }
            }
        }

    Column(
        modifier =
            modifier.wrapContentWidth(),
        horizontalAlignment =
            Alignment.Start
    ) {
        /*
         * ===================================================
         * PRIMARY SENTENCE
         * ===================================================
         *
         * This is the Besser version when a correction exists.
         * Corrected words are highlighted here.
         */
        Box {
            Text(
                text =
                    primaryTextWithTimestampSpace,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
                        .copy(
                            lineHeight =
                                MESSAGE_LINE_HEIGHT
                        ),
                color =
                    contentColor
            )

            Text(
                text =
                    time,
                modifier =
                    Modifier.align(
                        Alignment.BottomEnd
                    ),
                style =
                    timeStyle,
                color =
                    contentColor.copy(
                        alpha =
                            TIMESTAMP_ALPHA
                    )
            )
        }

        /*
         * ===================================================
         * ORIGINAL LEARNER SENTENCE
         * ===================================================
         *
         * The original sentence is displayed underneath as
         * quiet secondary context.
         *
         * No highlights are applied here.
         */
        if (hasBesserCorrection) {
            MessageCorrectionContent(
                original =
                    message.content,
                contentColor =
                    contentColor
            )
        }
    }
}

/*
 * ===========================================================
 * BESSER CORRECTION HIGHLIGHTING
 * ===========================================================
 *
 * Compares the original sentence with Besser.
 *
 * Words introduced or changed in the correction receive a
 * subtle background and stronger weight.
 *
 * Example:
 *
 * Original:
 *     Ich arbeite seit drei Jahr hier.
 *
 * Besser:
 *     Ich arbeite seit drei Jahren hier.
 *
 * Highlighted:
 *     Jahren
 */
private fun buildHighlightedCorrection(
    original: String,
    correction: String,
    baseColor: Color,
    changedColor: Color,
    highlightColor: Color
): AnnotatedString {
    val correctionTokens =
        tokenize(
            value = correction
        )

    val originalWords =
        tokenize(
            value = original
        )
            .filter { token ->
                !token.isWhitespace
            }
            .map { token ->
                token.normalized
            }

    val correctionWordIndices =
        correctionTokens
            .indices
            .filter { tokenIndex ->
                !correctionTokens[tokenIndex]
                    .isWhitespace
            }

    val correctionWords =
        correctionWordIndices
            .map { tokenIndex ->
                correctionTokens[tokenIndex]
                    .normalized
            }

    /*
     * These positions in the corrected sentence also exist
     * in the original sentence in the same sequence.
     */
    val unchangedCorrectionPositions =
        longestCommonSubsequenceSecondPositions(
            first = originalWords,
            second = correctionWords
        )

    /*
     * Convert word positions back into token positions so
     * whitespace remains untouched.
     */
    val changedTokenIndices =
        correctionWordIndices
            .filterIndexed { wordPosition, _ ->
                wordPosition !in
                        unchangedCorrectionPositions
            }
            .toSet()

    return buildAnnotatedString {
        correctionTokens.forEachIndexed { tokenIndex, token ->
            if (tokenIndex in changedTokenIndices) {
                withStyle(
                    SpanStyle(
                        color =
                            changedColor,
                        fontWeight =
                            FontWeight.SemiBold,
                        background =
                            highlightColor
                    )
                ) {
                    append(token.text)
                }
            } else {
                withStyle(
                    SpanStyle(
                        color =
                            baseColor
                    )
                ) {
                    append(token.text)
                }
            }
        }
    }
}

/*
 * Returns the positions in SECOND that belong to the longest
 * common subsequence shared with FIRST.
 */
private fun longestCommonSubsequenceSecondPositions(
    first: List<String>,
    second: List<String>
): Set<Int> {
    val rowCount =
        first.size

    val columnCount =
        second.size

    val lengths =
        Array(rowCount + 1) {
            IntArray(columnCount + 1)
        }

    for (row in rowCount - 1 downTo 0) {
        for (column in columnCount - 1 downTo 0) {
            lengths[row][column] =
                if (
                    first[row] ==
                    second[column]
                ) {
                    lengths[row + 1][column + 1] +
                            1
                } else {
                    maxOf(
                        lengths[row + 1][column],
                        lengths[row][column + 1]
                    )
                }
        }
    }

    val secondPositions =
        mutableSetOf<Int>()

    var row =
        0

    var column =
        0

    while (
        row < rowCount &&
        column < columnCount
    ) {
        when {
            first[row] == second[column] -> {
                secondPositions.add(column)

                row += 1
                column += 1
            }

            lengths[row + 1][column] >=
                    lengths[row][column + 1] -> {
                row += 1
            }

            else -> {
                column += 1
            }
        }
    }

    return secondPositions
}

private data class CorrectionToken(
    val text: String,
    val isWhitespace: Boolean
) {
    /*
     * Case and punctuation alone are ignored while comparing
     * words.
     */
    val normalized: String =
        text
            .lowercase()
            .replace(
                PUNCTUATION_REGEX,
                ""
            )
}

private fun tokenize(
    value: String
): List<CorrectionToken> {
    return TOKEN_REGEX
        .findAll(value)
        .map { match ->
            CorrectionToken(
                text =
                    match.value,
                isWhitespace =
                    match.value.isBlank()
            )
        }
        .toList()
}

private fun formatMessageTime(
    timestamp: Long
): String {
    return Instant
        .ofEpochMilli(timestamp)
        .atZone(
            ZoneId.systemDefault()
        )
        .format(
            DateTimeFormatter.ofPattern(
                TIME_FORMAT_PATTERN,
                Locale.getDefault()
            )
        )
}

private val TOKEN_REGEX =
    Regex("\\S+|\\s+")

private val PUNCTUATION_REGEX =
    Regex("[^\\p{L}\\p{N}]")

private const val TIMESTAMP_RESERVATION_PREFIX =
    "\u2002\u2002"

private const val TIME_FORMAT_PATTERN =
    "HH:mm"

private const val TIMESTAMP_ALPHA =
    0.55f

/*
 * Background strength applied to corrected Besser words.
 */
private const val CORRECTION_HIGHLIGHT_ALPHA =
    0.18f

private val MESSAGE_LINE_HEIGHT =
    22.sp
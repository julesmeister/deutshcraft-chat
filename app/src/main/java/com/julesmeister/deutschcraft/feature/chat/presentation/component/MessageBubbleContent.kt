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
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
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
        remember(
            message.timestamp
        ) {
            formatMessageTime(
                timestamp =
                    message.timestamp
            )
        }

    val timeStyle =
        MaterialTheme
            .typography
            .labelSmall

    val baseMessageStyle =
        MaterialTheme
            .typography
            .bodyLarge

    val scaledMessageStyle =
        baseMessageStyle.copy(
            lineHeight =
                ChatDesignTokens.BubbleContent.MessageLineHeight
        )

    val hasBesserCorrection =
        isUserMessage &&
                !correction.isNullOrBlank() &&
                correction.trim() !=
                message.content.trim()

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
     * - Words changed by Besser are highlighted.
     *
     * Without Besser:
     *
     * - The ordinary message appears normally.
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
                    original =
                        message.content,

                    correction =
                        primaryText,

                    baseColor =
                        contentColor,

                    changedColor =
                        contentColor,

                    highlightColor =
                        contentColor.copy(
                            alpha =
                                ChatDesignTokens.BubbleContent.CorrectionHighlightAlpha
                        )
                )
            } else {
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color =
                                contentColor
                        )
                    ) {
                        append(
                            primaryText
                        )
                    }
                }
            }
        }

    /*
     * Add invisible room for the timestamp.
     *
     * The timestamp itself remains fixed-size, so the reserved
     * space follows the fixed timestamp style as well.
     */
    val primaryTextWithTimestampSpace =
        remember(
            visiblePrimaryText,
            time,
            timeStyle.fontSize
        ) {
            buildAnnotatedString {
                append(
                    visiblePrimaryText
                )

                withStyle(
                    SpanStyle(
                        color =
                            Color.Transparent,

                        fontSize =
                            timeStyle.fontSize
                    )
                ) {
                    append(
                        ChatPresentationTokens.MessageBubbleContent.TimestampReservationPrefix
                    )

                    append(
                        time
                    )
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
         * This is the corrected Besser version when a
         * correction exists.
         */
        Box {
            Text(
                text =
                    primaryTextWithTimestampSpace,

                style =
                    scaledMessageStyle,

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
                            ChatDesignTokens.BubbleContent.TimestampAlpha
                    )
            )
        }

        /*
         * ===================================================
         * ORIGINAL LEARNER SENTENCE
         * ===================================================
         *
         * The original sentence remains quiet secondary
         * context when a corrected version exists.
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
 * Words introduced or changed in the corrected sentence
 * receive a subtle background and stronger font weight.
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
            value =
                correction
        )

    val originalWords =
        tokenize(
            value =
                original
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
                !correctionTokens[
                    tokenIndex
                ].isWhitespace
            }

    val correctionWords =
        correctionWordIndices
            .map { tokenIndex ->
                correctionTokens[
                    tokenIndex
                ].normalized
            }

    /*
     * Positions in the corrected sentence that also exist in
     * the original sentence in the same sequence.
     */
    val unchangedCorrectionPositions =
        longestCommonSubsequenceSecondPositions(
            first =
                originalWords,

            second =
                correctionWords
        )

    /*
     * Convert word positions back into token positions so
     * whitespace remains untouched.
     */
    val changedTokenIndices =
        correctionWordIndices
            .filterIndexed {
                    wordPosition,
                    _ ->

                wordPosition !in
                        unchangedCorrectionPositions
            }
            .toSet()

    return buildAnnotatedString {
        correctionTokens.forEachIndexed {
                tokenIndex,
                token ->

            if (
                tokenIndex in
                changedTokenIndices
            ) {
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
                    append(
                        token.text
                    )
                }
            } else {
                withStyle(
                    SpanStyle(
                        color =
                            baseColor
                    )
                ) {
                    append(
                        token.text
                    )
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
        Array(
            size =
                rowCount + 1
        ) {
            IntArray(
                size =
                    columnCount + 1
            )
        }

    for (
    row in
    rowCount - 1 downTo 0
    ) {
        for (
        column in
        columnCount - 1 downTo 0
        ) {
            lengths[row][column] =
                if (
                    first[row] ==
                    second[column]
                ) {
                    lengths[
                        row + 1
                    ][
                        column + 1
                    ] + 1
                } else {
                    maxOf(
                        lengths[
                            row + 1
                        ][
                            column
                        ],

                        lengths[
                            row
                        ][
                            column + 1
                        ]
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
            first[row] ==
                    second[column] -> {

                secondPositions.add(
                    column
                )

                row +=
                    1

                column +=
                    1
            }

            lengths[
                row + 1
            ][
                column
            ] >=
                    lengths[
                        row
                    ][
                        column + 1
                    ] -> {

                row +=
                    1
            }

            else -> {
                column +=
                    1
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
     * Case and punctuation alone are ignored during
     * comparison.
     */
    val normalized: String =
        text
            .lowercase()
            .replace(
                regex =
                    PUNCTUATION_REGEX,

                replacement =
                    ""
            )
}

private fun tokenize(
    value: String
): List<CorrectionToken> {
    return TOKEN_REGEX
        .findAll(
            input =
                value
        )
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
        .ofEpochMilli(
            timestamp
        )
        .atZone(
            ZoneId.systemDefault()
        )
        .format(
            DateTimeFormatter.ofPattern(
                ChatPresentationTokens.MessageBubbleContent.TimeFormatPattern,
                Locale.getDefault()
            )
        )
}

private val TOKEN_REGEX =
    Regex(
        "\\S+|\\s+"
    )

private val PUNCTUATION_REGEX =
    Regex(
        "[^\\p{L}\\p{N}]"
    )

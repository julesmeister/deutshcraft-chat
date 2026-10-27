package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Displays the learner's original sentence underneath the
 * corrected Besser sentence.
 *
 * The original sentence is:
 *
 * - smaller
 * - dimmer
 * - not highlighted
 * - expandable when long
 *
 * All correction highlighting belongs to the primary Besser
 * sentence above.
 */
@Composable
fun MessageCorrectionContent(
    original: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    var isExpanded by
    rememberSaveable(original) {
        mutableStateOf(false)
    }

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            modifier
                .padding(
                    top =
                        ORIGINAL_SECTION_TOP_PADDING
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null
                ) {
                    isExpanded =
                        !isExpanded
                }
    ) {
        /*
         * Label identifying this as what the learner
         * originally wrote.
         */
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    "✦",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontSize =
                    ORIGINAL_LABEL_ICON_SIZE,
                color =
                    contentColor.copy(
                        alpha =
                            ORIGINAL_LABEL_ALPHA
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ORIGINAL_LABEL_SPACING
                    )
            )

            Text(
                text =
                    "DEIN SATZ",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall
                        .copy(
                            fontSize =
                                ORIGINAL_LABEL_SIZE,
                            letterSpacing =
                                ORIGINAL_LABEL_LETTER_SPACING
                        ),
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    contentColor.copy(
                        alpha =
                            ORIGINAL_LABEL_ALPHA
                    )
            )
        }

        /*
         * Original sentence without highlighting.
         */
        Text(
            text =
                original,
            modifier =
                Modifier.padding(
                    top =
                        ORIGINAL_TEXT_TOP_PADDING
                ),
            style =
                MaterialTheme
                    .typography
                    .bodySmall
                    .copy(
                        fontSize =
                            ORIGINAL_TEXT_SIZE,
                        lineHeight =
                            ORIGINAL_LINE_HEIGHT
                    ),
            color =
                contentColor.copy(
                    alpha =
                        ORIGINAL_TEXT_ALPHA
                ),
            maxLines =
                if (isExpanded) {
                    Int.MAX_VALUE
                } else {
                    COLLAPSED_MAX_LINES
                },
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

private const val COLLAPSED_MAX_LINES =
    2

private const val ORIGINAL_TEXT_ALPHA =
    0.52f

private const val ORIGINAL_LABEL_ALPHA =
    0.48f

private val ORIGINAL_SECTION_TOP_PADDING =
    8.dp

private val ORIGINAL_LABEL_SPACING =
    4.dp

private val ORIGINAL_TEXT_TOP_PADDING =
    2.dp

private val ORIGINAL_LABEL_ICON_SIZE =
    9.sp

private val ORIGINAL_LABEL_SIZE =
    9.sp

private val ORIGINAL_LABEL_LETTER_SPACING =
    0.7.sp

private val ORIGINAL_TEXT_SIZE =
    12.sp

private val ORIGINAL_LINE_HEIGHT =
    16.sp
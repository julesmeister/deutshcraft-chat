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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens

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
                        ChatDesignTokens.CorrectionContent.OriginalSectionTopPadding
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
                    ChatDesignTokens.CorrectionContent.OriginalLabelIconSize,
                color =
                    contentColor.copy(
                        alpha =
                            ChatDesignTokens.CorrectionContent.OriginalLabelAlpha
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ChatDesignTokens.CorrectionContent.OriginalLabelSpacing
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
                                ChatDesignTokens.CorrectionContent.OriginalLabelSize,
                            letterSpacing =
                                ChatDesignTokens.CorrectionContent.OriginalLabelLetterSpacing
                        ),
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    contentColor.copy(
                        alpha =
                            ChatDesignTokens.CorrectionContent.OriginalLabelAlpha
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
                        ChatDesignTokens.CorrectionContent.OriginalTextTopPadding
                ),
            style =
                MaterialTheme
                    .typography
                    .bodySmall
                    .copy(
                        fontSize =
                            ChatDesignTokens.CorrectionContent.OriginalTextSize,
                        lineHeight =
                            ChatDesignTokens.CorrectionContent.OriginalLineHeight
                    ),
            color =
                contentColor.copy(
                    alpha =
                        ChatDesignTokens.CorrectionContent.OriginalTextAlpha
                ),
            maxLines =
                if (isExpanded) {
                    Int.MAX_VALUE
                } else {
                    ChatDesignTokens.CorrectionContent.CollapsedMaxLines
                },
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

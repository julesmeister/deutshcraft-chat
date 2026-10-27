package com.julesmeister.deutschcraft.feature.phrasemarking.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.ExplanationStatus
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSky
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

/*
 * Compact note attached flush under its source bubble.
 *
 *   ╭──────────────────────────────╮
 *   │ source bubble (rounded)      │
 *   ╰──────────────────────────────╯
 *   ┌──────────────────────────────┐  <- flat top, no gap
 *   │ 🧩 drei Jahre · three years ✕│
 *   ╰──────────────────────────────╯  <- rounded only if last
 *
 * Tap to expand the explanation and example.
 */
@Composable
fun MarkedPhraseBubble(
    phrase: MarkedPhrase,
    isUserMessage: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isLast: Boolean = true
) {
    var isExpanded by
    rememberSaveable(phrase.id) {
        mutableStateOf(false)
    }

    val accent =
        if (isUserMessage) DeutschLime else DeutschSky

    /*
     * Flat top edge always. Bottom corners round only on the
     * last note so stacked notes merge into one block.
     */
    val bottomCorner =
        if (isLast) BOTTOM_CORNER_RADIUS else 0.dp

    val shape =
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner
        )

    val containerColor =
        accent
            .copy(alpha = CONTAINER_TINT_ALPHA)
            .compositeOver(DeutschSurfaceHigh)

    val maxWidth =
        (LocalConfiguration.current.screenWidthDp.dp * 0.72f)
            .coerceAtMost(380.dp)

    val canExpand =
        phrase.status == ExplanationStatus.READY &&
                (!phrase.explanation.isNullOrBlank() ||
                        !phrase.example.isNullOrBlank())

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .clip(shape)
                    .background(containerColor)
                    .clickable(
                        enabled = canExpand,
                        interactionSource =
                            remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = !isExpanded
                    }
                    .animateContentSize(tween(220))
                    .padding(
                        start = 12.dp,
                        end = 4.dp,
                        top = 6.dp,
                        bottom = 7.dp
                    )
        ) {
            /*
             * One line: icon, phrase, and meaning.
             */
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append(phrase.type.icon)
                            append(" ")

                            withStyle(
                                SpanStyle(
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(phrase.phrase)
                            }

                            val secondary =
                                when (phrase.status) {
                                    ExplanationStatus.PENDING -> "…"
                                    ExplanationStatus.FAILED -> "gespeichert"
                                    ExplanationStatus.READY ->
                                        phrase.meaning.orEmpty()
                                }

                            if (secondary.isNotBlank()) {
                                withStyle(
                                    SpanStyle(
                                        color = DeutschTextSecondary
                                    )
                                ) {
                                    append("  ·  ")
                                    append(secondary)
                                }
                            }
                        },
                    modifier = Modifier.weight(1f, fill = false),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        ),
                    color = DeutschTextPrimary,
                    maxLines = if (isExpanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Markierung entfernen",
                        modifier = Modifier.size(12.dp),
                        tint = DeutschTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }

            /*
             * Details only when expanded.
             */
            AnimatedVisibility(
                visible = isExpanded && canExpand,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(180))
            ) {
                ExpandedDetails(
                    explanation = phrase.explanation,
                    example = phrase.example,
                    accent = accent
                )
            }
        }
    }
}

@Composable
private fun ExpandedDetails(
    explanation: String?,
    example: String?,
    accent: Color
) {
    Column(
        modifier = Modifier.padding(top = 4.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        explanation
            ?.takeIf(String::isNotBlank)
            ?.let { text ->
                Text(
                    text = text,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                    color = DeutschTextSecondary
                )
            }

        example
            ?.takeIf(String::isNotBlank)
            ?.let { text ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(accent.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "„$text“",
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                        fontStyle = FontStyle.Italic,
                        color = DeutschTextPrimary.copy(alpha = 0.82f)
                    )
                }
            }
    }
}

private val BOTTOM_CORNER_RADIUS = 14.dp

private const val CONTAINER_TINT_ALPHA = 0.06f
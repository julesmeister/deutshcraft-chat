package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary
import kotlin.math.roundToInt

/*
 * CEFR Bento header.
 *
 * Two surfaces only:
 *
 * 1. Neutral "Aktuelles Niveau" tile (top-left)
 * 2. ONE continuous lime L-shaped "Bereitschaft" surface
 *    (top-right + entire bottom row)
 *
 * The L is drawn as a single Shape, so there is no seam
 * between the top-right and the bottom row.
 */
@Composable
internal fun CefrLevelReadinessBento(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    val model =
        progress.toCefrBentoUiModel()

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BENTO_TOTAL_HEIGHT)
    ) {
        val currentTileWidth =
            (maxWidth - BENTO_GAP) * CURRENT_LEVEL_WIDTH_FRACTION

        /*
         * The notch is the area cut out of the lime surface.
         * It equals the current tile plus the gap around it.
         */
        val notchWidth =
            currentTileWidth + BENTO_GAP

        val notchHeight =
            BENTO_TOP_HEIGHT + BENTO_GAP

        val readinessShape =
            LShape(
                notchWidth = notchWidth,
                notchHeight = notchHeight,
                outerRadius = OUTER_RADIUS,
                innerRadius = INNER_RADIUS
            )

        /*
         * ===================================================
         * 1. ONE lime L-shaped readiness surface
         * ===================================================
         */
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(readinessShape)
                    .background(DeutschLime)
        )

        /*
         * ===================================================
         * 2. Neutral current-level tile in the notch
         * ===================================================
         */
        CurrentLevelTile(
            model = model,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .size(
                        width = currentTileWidth,
                        height = BENTO_TOP_HEIGHT
                    )
        )

        /*
         * ===================================================
         * 3. Readiness header (upper-right part of the L)
         * ===================================================
         */
        ReadinessHeader(
            model = model,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = notchWidth)
                    .size(
                        width = maxWidth - notchWidth,
                        height = BENTO_TOP_HEIGHT
                    )
        )

        /*
         * ===================================================
         * 4. Readiness progress (bottom row of the L)
         * ===================================================
         */
        ReadinessProgressRow(
            model = model,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(BENTO_TOTAL_HEIGHT - notchHeight)
        )
    }
}

/*
 * ===========================================================
 * CURRENT LEVEL (neutral)
 * ===========================================================
 */

@Composable
private fun CurrentLevelTile(
    model: CefrBentoUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(OUTER_RADIUS))
                .background(
                    MaterialTheme.colorScheme.surfaceContainer
                )
                .padding(TILE_PADDING),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "AKTUELLES\nNIVEAU",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DeutschTextSecondary
        )

        Column {
            Text(
                text = model.currentLevelCode,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = DeutschTextPrimary
            )

            Text(
                text = model.currentLevelName,
                style = MaterialTheme.typography.bodySmall,
                color = DeutschTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/*
 * ===========================================================
 * READINESS (lime, dark content)
 * ===========================================================
 */

@Composable
private fun ReadinessHeader(
    model: CefrBentoUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(TILE_PADDING),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "BEREITSCHAFT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ON_LIME_SECONDARY
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${model.readinessPercentage}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = ON_LIME_PRIMARY
            )

            Text(
                text =
                    model.nextLevelCode
                        ?.let { "für $it" }
                        ?: "Höchstes Niveau",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ON_LIME_SECONDARY
            )
        }
    }
}

@Composable
private fun ReadinessProgressRow(
    model: CefrBentoUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(
                horizontal = TILE_PADDING,
                vertical = PROGRESS_ROW_VERTICAL_PADDING
            ),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =
                    model.nextLevelCode
                        ?.let { "Fortschritt zu $it" }
                        ?: "Fortschritt",
                style = MaterialTheme.typography.labelMedium,
                color = ON_LIME_SECONDARY
            )

            Text(
                text = "${model.readinessPercentage}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ON_LIME_PRIMARY
            )
        }

        LinearProgressIndicator(
            progress = {
                model.readinessPercentage / 100f
            },
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(PROGRESS_BAR_HEIGHT)
                    .clip(RoundedCornerShape(50)),
            color = ON_LIME_PRIMARY,
            trackColor = ON_LIME_TRACK,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )

        Text(
            text = model.readinessMessage,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = ON_LIME_SECONDARY,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/*
 * ===========================================================
 * L SHAPE
 * ===========================================================
 *
 * Full rectangle with the top-left notch removed:
 *
 *         ╭───────────╮
 *         │           │
 * ╭───────╯           │
 * │                   │
 * ╰───────────────────╯
 *
 * Outer corners use outerRadius.
 * The notch's bottom-right corner uses innerRadius.
 */
private class LShape(
    private val notchWidth: Dp,
    private val notchHeight: Dp,
    private val outerRadius: Dp,
    private val innerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height

        val nw = with(density) { notchWidth.toPx() }
        val nh = with(density) { notchHeight.toPx() }
        val r = with(density) { outerRadius.toPx() }
        val ir = with(density) { innerRadius.toPx() }

        val path =
            Path().apply {
                // Top edge of the upper-right section.
                moveTo(nw + r, 0f)
                lineTo(w - r, 0f)

                // Top-right corner.
                arcTo(
                    rect = Rect(w - 2 * r, 0f, w, 2 * r),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Right edge, bottom-right corner.
                lineTo(w, h - r)
                arcTo(
                    rect = Rect(w - 2 * r, h - 2 * r, w, h),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Bottom edge, bottom-left corner.
                lineTo(r, h)
                arcTo(
                    rect = Rect(0f, h - 2 * r, 2 * r, h),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Left edge up to the bottom section's
                // top-left corner.
                lineTo(0f, nh + r)
                arcTo(
                    rect = Rect(0f, nh, 2 * r, nh + 2 * r),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Along the bottom of the notch.
                lineTo(nw - ir, nh)

                // Rounded inner corner of the notch.
                arcTo(
                    rect = Rect(nw - 2 * ir, nh - 2 * ir, nw, nh),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )

                // Up the right side of the notch to the
                // upper section's top-left corner.
                lineTo(nw, r)
                arcTo(
                    rect = Rect(nw, 0f, nw + 2 * r, 2 * r),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                close()
            }

        return Outline.Generic(path)
    }
}

/*
 * ===========================================================
 * UI MODEL
 * ===========================================================
 */

private data class CefrBentoUiModel(
    val currentLevelCode: String,
    val currentLevelName: String,
    val nextLevelCode: String?,
    val readinessPercentage: Int,
    val readinessMessage: String
)

/*
 * The ONLY place that reads ProgressSummary.
 *
 * ⚠ If this does not compile, change just the two property
 * reads marked below to match what your old
 * CurrentNiveauPanel / ReadinessPanel used.
 */
private fun ProgressSummary.toCefrBentoUiModel():
        CefrBentoUiModel {

    val safeReadinessPercentage =
        readinessPercentage.coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )

    val nextLevel =
        if (isHighestLevel) {
            null
        } else {
            nextLevelName
        }

    val currentLevelName =
        when (
            currentLevel.name.uppercase()
        ) {
            "A1" -> {
                "Anfänger"
            }

            "A2" -> {
                "Grundkenntnisse"
            }

            "B1" -> {
                "Mittelstufe"
            }

            "B2" -> {
                "Gute Mittelstufe"
            }

            "C1" -> {
                "Fortgeschritten"
            }

            "C2" -> {
                "Nahezu muttersprachlich"
            }

            else -> {
                currentLevel.name
            }
        }

    val readinessMessage =
        when {
            isHighestLevel -> {
                "Höchstes CEFR-Niveau erreicht"
            }

            safeReadinessPercentage <
                    25 -> {

                "Erste Schritte Richtung $nextLevelName"
            }

            safeReadinessPercentage <
                    50 -> {

                "Auf dem Weg zu $nextLevelName"
            }

            safeReadinessPercentage <
                    75 -> {

                "Gute Fortschritte zu $nextLevelName"
            }

            safeReadinessPercentage <
                    90 -> {

                "Fast bereit für $nextLevelName"
            }

            else -> {
                "Sehr nah an $nextLevelName"
            }
        }

    return CefrBentoUiModel(
        currentLevelCode =
            currentLevel.name.uppercase(),

        currentLevelName =
            currentLevelName,

        nextLevelCode =
            nextLevel,

        readinessPercentage =
            if (isHighestLevel) {
                100
            } else {
                safeReadinessPercentage
            },

        readinessMessage =
            readinessMessage
    )
}


/*
 * ===========================================================
 * GEOMETRY + COLORS
 * ===========================================================
 */

private const val CURRENT_LEVEL_WIDTH_FRACTION = 0.42f

private val BENTO_TOTAL_HEIGHT = 272.dp
private val BENTO_TOP_HEIGHT = 152.dp
private val BENTO_GAP = 8.dp

private val OUTER_RADIUS = 22.dp
private val INNER_RADIUS = 18.dp

private val TILE_PADDING = 18.dp
private val PROGRESS_ROW_VERTICAL_PADDING = 14.dp
private val PROGRESS_BAR_HEIGHT = 10.dp

/*
 * Dark content on lime.
 */
private val ON_LIME_PRIMARY = Color(0xFF111111)
private val ON_LIME_SECONDARY = Color(0xFF111111).copy(alpha = 0.68f)
private val ON_LIME_TRACK = Color(0xFF111111).copy(alpha = 0.14f)
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
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
internal fun CefrLevelReadinessBento(
    progress: ProgressSummary,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val model =
        progress.toCefrBentoUiModel(
            showEnglish =
                showEnglish
        )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    CefrBentoTotalHeight
                )
    ) {
        val currentTileWidth =
            (
                    maxWidth -
                            CefrBentoGap
                    ) *
                    CefrCurrentLevelWidthFraction

        val notchWidth =
            currentTileWidth +
                    CefrBentoGap

        val notchHeight =
            CefrBentoTopHeight +
                    CefrBentoGap

        val readinessShape =
            LShape(
                notchWidth =
                    notchWidth,
                notchHeight =
                    notchHeight,
                outerRadius =
                    CefrBentoOuterRadius,
                innerRadius =
                    CefrBentoInnerRadius
            )

        /*
         * ===================================================
         * CONTINUOUS LIME READINESS SURFACE
         * ===================================================
         */
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(
                        readinessShape
                    )
                    .background(
                        DeutschLime
                    )
        )

        /*
         * ===================================================
         * LIGHT CURRENT-LEVEL TILE
         * ===================================================
         *
         * Intentionally inverted:
         *
         * light background
         * dark primary text
         * softer dark secondary text
         */
        CurrentLevelTile(
            model =
                model,
            showEnglish =
                showEnglish,
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .size(
                        width =
                            currentTileWidth,
                        height =
                            CefrBentoTopHeight
                    )
        )

        /*
         * ===================================================
         * READINESS HEADER
         * ===================================================
         */
        ReadinessHeader(
            model =
                model,
            showEnglish =
                showEnglish,
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .offset(
                        x =
                            notchWidth
                    )
                    .size(
                        width =
                            maxWidth -
                                    notchWidth,
                        height =
                            CefrBentoTopHeight
                    )
        )

        /*
         * ===================================================
         * READINESS PROGRESS
         * ===================================================
         */
        ReadinessProgressRow(
            model =
                model,
            showEnglish =
                showEnglish,
            modifier =
                Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .fillMaxWidth()
                    .height(
                        CefrBentoTotalHeight -
                                notchHeight
                    )
        )
    }
}

/*
 * ===========================================================
 * CURRENT LEVEL
 * ===========================================================
 */

@Composable
private fun CurrentLevelTile(
    model: CefrBentoUiModel,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        CefrBentoOuterRadius
                    )
                )
                .background(
                    CefrCurrentLevelBackground
                )
                .padding(
                    CefrBentoTilePadding
                ),
        verticalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text =
                if (showEnglish) {
                    "CURRENT\nLEVEL"
                } else {
                    "AKTUELLES\nNIVEAU"
                },
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                CefrCurrentLevelTextSecondary
        )

        Column {
            Text(
                text =
                    model.currentLevelCode,
                style =
                    MaterialTheme
                        .typography
                        .displaySmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    CefrCurrentLevelTextPrimary
            )

            Text(
                text =
                    model.currentLevelName,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    CefrCurrentLevelTextSecondary,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

/*
 * ===========================================================
 * READINESS HEADER
 * ===========================================================
 */

@Composable
private fun ReadinessHeader(
    model: CefrBentoUiModel,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(
                CefrBentoTilePadding
            ),
        verticalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text =
                if (showEnglish) {
                    "READINESS"
                } else {
                    "BEREITSCHAFT"
                },
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                CefrOnLimeSecondary
        )

        Column(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.End
        ) {
            Text(
                text =
                    "${model.readinessPercentage}%",
                style =
                    MaterialTheme
                        .typography
                        .displayMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    CefrOnLimePrimary
            )

            Text(
                text =
                    model.nextLevelCode
                        ?.let { level ->
                            if (showEnglish) {
                                "for $level"
                            } else {
                                "für $level"
                            }
                        }
                        ?: if (showEnglish) {
                            "Highest level"
                        } else {
                            "Höchstes Niveau"
                        },
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    CefrOnLimeSecondary
            )
        }
    }
}

/*
 * ===========================================================
 * READINESS PROGRESS
 * ===========================================================
 */

@Composable
private fun ReadinessProgressRow(
    model: CefrBentoUiModel,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(
                horizontal =
                    CefrBentoTilePadding,
                vertical =
                    CefrProgressRowVerticalPadding
            ),
        verticalArrangement =
            Arrangement.Center
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    model.nextLevelCode
                        ?.let { level ->
                            if (showEnglish) {
                                "Progress to $level"
                            } else {
                                "Fortschritt zu $level"
                            }
                        }
                        ?: if (showEnglish) {
                            "Progress"
                        } else {
                            "Fortschritt"
                        },
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    CefrOnLimeSecondary
            )

            Text(
                text =
                    "${model.readinessPercentage}%",
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    CefrOnLimePrimary
            )
        }

        LinearProgressIndicator(
            progress = {
                model.readinessPercentage /
                        100f
            },
            modifier =
                Modifier
                    .padding(
                        top =
                            CefrProgressBarTopPadding
                    )
                    .fillMaxWidth()
                    .height(
                        CefrProgressBarHeight
                    )
                    .clip(
                        RoundedCornerShape(50)
                    ),
            color =
                CefrOnLimePrimary,
            trackColor =
                CefrOnLimeTrack,
            strokeCap =
                StrokeCap.Round,
            gapSize =
                0.dp,
            drawStopIndicator =
                {}
        )

        Text(
            text =
                model.readinessMessage,
            modifier =
                Modifier.padding(
                    top =
                        CefrReadinessMessageTopPadding
                ),
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                CefrOnLimeSecondary,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

/*
 * ===========================================================
 * L SHAPE
 * ===========================================================
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
        val w =
            size.width

        val h =
            size.height

        val nw =
            with(density) {
                notchWidth.toPx()
            }

        val nh =
            with(density) {
                notchHeight.toPx()
            }

        val r =
            with(density) {
                outerRadius.toPx()
            }

        val ir =
            with(density) {
                innerRadius.toPx()
            }

        val path =
            Path().apply {
                moveTo(
                    nw + r,
                    0f
                )

                lineTo(
                    w - r,
                    0f
                )

                arcTo(
                    rect =
                        Rect(
                            w - 2 * r,
                            0f,
                            w,
                            2 * r
                        ),
                    startAngleDegrees =
                        270f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    w,
                    h - r
                )

                arcTo(
                    rect =
                        Rect(
                            w - 2 * r,
                            h - 2 * r,
                            w,
                            h
                        ),
                    startAngleDegrees =
                        0f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    r,
                    h
                )

                arcTo(
                    rect =
                        Rect(
                            0f,
                            h - 2 * r,
                            2 * r,
                            h
                        ),
                    startAngleDegrees =
                        90f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    0f,
                    nh + r
                )

                arcTo(
                    rect =
                        Rect(
                            0f,
                            nh,
                            2 * r,
                            nh + 2 * r
                        ),
                    startAngleDegrees =
                        180f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    nw - ir,
                    nh
                )

                arcTo(
                    rect =
                        Rect(
                            nw - 2 * ir,
                            nh - 2 * ir,
                            nw,
                            nh
                        ),
                    startAngleDegrees =
                        90f,
                    sweepAngleDegrees =
                        -90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    nw,
                    r
                )

                arcTo(
                    rect =
                        Rect(
                            nw,
                            0f,
                            nw + 2 * r,
                            2 * r
                        ),
                    startAngleDegrees =
                        180f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                close()
            }

        return Outline.Generic(
            path
        )
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

private fun ProgressSummary.toCefrBentoUiModel(
    showEnglish: Boolean
): CefrBentoUiModel {
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

    val levelCode =
        currentLevel.name.uppercase()

    val currentLevelDisplayName =
        when (levelCode) {
            "A1" ->
                if (showEnglish) {
                    "Beginner"
                } else {
                    "Anfänger"
                }

            "A2" ->
                if (showEnglish) {
                    "Elementary"
                } else {
                    "Grundkenntnisse"
                }

            "B1" ->
                if (showEnglish) {
                    "Intermediate"
                } else {
                    "Mittelstufe"
                }

            "B2" ->
                if (showEnglish) {
                    "Upper intermediate"
                } else {
                    "Gute Mittelstufe"
                }

            "C1" ->
                if (showEnglish) {
                    "Advanced"
                } else {
                    "Fortgeschritten"
                }

            "C2" ->
                if (showEnglish) {
                    "Proficient"
                } else {
                    "Nahezu muttersprachlich"
                }

            else ->
                currentLevel.name
        }

    val readinessMessage =
        when {
            isHighestLevel -> {
                if (showEnglish) {
                    "Highest CEFR level reached"
                } else {
                    "Höchstes CEFR-Niveau erreicht"
                }
            }

            safeReadinessPercentage < 25 -> {
                if (showEnglish) {
                    "First steps toward $nextLevelName"
                } else {
                    "Erste Schritte Richtung $nextLevelName"
                }
            }

            safeReadinessPercentage < 50 -> {
                if (showEnglish) {
                    "On the way to $nextLevelName"
                } else {
                    "Auf dem Weg zu $nextLevelName"
                }
            }

            safeReadinessPercentage < 75 -> {
                if (showEnglish) {
                    "Good progress toward $nextLevelName"
                } else {
                    "Gute Fortschritte zu $nextLevelName"
                }
            }

            safeReadinessPercentage < 90 -> {
                if (showEnglish) {
                    "Almost ready for $nextLevelName"
                } else {
                    "Fast bereit für $nextLevelName"
                }
            }

            else -> {
                if (showEnglish) {
                    "Very close to $nextLevelName"
                } else {
                    "Sehr nah an $nextLevelName"
                }
            }
        }

    return CefrBentoUiModel(
        currentLevelCode =
            levelCode,
        currentLevelName =
            currentLevelDisplayName,
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
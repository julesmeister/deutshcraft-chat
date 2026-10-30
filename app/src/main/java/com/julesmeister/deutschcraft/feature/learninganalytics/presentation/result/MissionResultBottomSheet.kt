package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Displays finalized mission analytics.
 *
 * Layout:
 *
 * - Hero: animated score ring, mission title, CEFR pill
 * - Bento grid: six skill tiles with compact bars
 * - Evidence: compact stat chips
 * - Objectives: strong / practice-next lists
 * - Continue: full-width primary pill
 *
 * Purely presentational. All colors come from MaterialTheme so
 * the sheet follows light and dark mode automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionResultBottomSheet(
    state: MissionResultUiState,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.hasMissionResult) {
        return
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = SHEET_SHAPE,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SHEET_HORIZONTAL_PADDING)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING)
        ) {
            ResultHero(state = state)

            SkillBentoGrid(state = state)

            EvidenceSection(state = state)

            if (state.strongObjectiveTitles.isNotEmpty()) {
                ObjectiveSection(
                    title = "Strong",
                    objectiveTitles = state.strongObjectiveTitles,
                    isStrong = true
                )
            }

            if (state.improvementObjectiveTitles.isNotEmpty()) {
                ObjectiveSection(
                    title = "Practice next",
                    objectiveTitles = state.improvementObjectiveTitles,
                    isStrong = false
                )
            }

            ContinueButton(onClick = onContinue)
        }
    }
}

/*
 * ===========================================================
 * HERO
 * ===========================================================
 */

@Composable
private fun ResultHero(
    state: MissionResultUiState
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(CARD_SHAPE)
                .background(colorScheme.surfaceContainerHigh)
                .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MISSION COMPLETE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScoreRing(
            score = state.safeOverallScore,
            size = SCORE_RING_SIZE,
            strokeWidth = SCORE_RING_STROKE
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.missionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (state.cefrLevelLabel.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Pill(
                text = state.cefrLevelLabel,
                background = colorScheme.primary.copy(alpha = 0.14f),
                content = colorScheme.primary
            )
        }

        if (state.performanceMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.performanceMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreRing(
    score: Int,
    size: Dp,
    strokeWidth: Dp
) {
    val colorScheme = MaterialTheme.colorScheme

    val safeScore = score.coerceIn(0, 100)

    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
    }

    val progress by animateFloatAsState(
        targetValue = if (started) safeScore / 100f else 0f,
        animationSpec =
            tween(
                durationMillis = RING_ANIMATION_MILLIS,
                easing = FastOutSlowInEasing
            ),
        label = "MissionScoreRing"
    )

    val trackColor = colorScheme.primary.copy(alpha = 0.12f)
    val progressColor = colorScheme.primary

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize =
                Size(
                    width = this.size.width - strokePx,
                    height = this.size.height - strokePx
                )

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx)
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Text(
                text = "Overall",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/*
 * ===========================================================
 * SKILL BENTO GRID
 * ===========================================================
 */

private data class SkillTile(
    val label: String,
    val score: Int
)

@Composable
private fun SkillBentoGrid(
    state: MissionResultUiState
) {
    val tiles =
        listOf(
            SkillTile("Conversation", state.conversationScore),
            SkillTile("Independence", state.independenceScore),
            SkillTile("Grammar", state.grammarScore),
            SkillTile("Vocabulary", state.vocabularyScore),
            SkillTile("Interaction", state.interactionScore),
            SkillTile("Comprehension", state.comprehensionScore)
        )

    Column(verticalArrangement = Arrangement.spacedBy(TILE_SPACING)) {
        SectionLabel(text = "Performance")

        tiles.chunked(2).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TILE_SPACING)
            ) {
                rowTiles.forEach { tile ->
                    SkillTileCard(
                        tile = tile,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SkillTileCard(
    tile: SkillTile,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val safeScore = tile.score.coerceIn(0, 100)

    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
    }

    val progress by animateFloatAsState(
        targetValue = if (started) safeScore / 100f else 0f,
        animationSpec =
            tween(
                durationMillis = BAR_ANIMATION_MILLIS,
                easing = FastOutSlowInEasing
            ),
        label = "SkillTileProgress"
    )

    val accent = scoreAccent(safeScore)

    Column(
        modifier =
            modifier
                .clip(TILE_SHAPE)
                .background(colorScheme.surfaceContainerLow)
                .padding(14.dp)
    ) {
        Text(
            text = tile.label,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$safeScore%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress)
                        .height(BAR_HEIGHT)
                        .clip(CircleShape)
                        .background(accent)
            )
        }
    }
}

@Composable
private fun scoreAccent(
    score: Int
): Color {
    val colorScheme = MaterialTheme.colorScheme

    return when {
        score >= STRONG_SCORE_THRESHOLD -> colorScheme.primary
        score >= DEVELOPING_SCORE_THRESHOLD -> colorScheme.tertiary
        else -> colorScheme.error
    }
}

/*
 * ===========================================================
 * EVIDENCE
 * ===========================================================
 */

@Composable
private fun EvidenceSection(
    state: MissionResultUiState
) {
    val colorScheme = MaterialTheme.colorScheme

    val rows =
        buildList {
            add("Objectives" to state.objectiveProgressLabel)
            add("Hints" to state.supportSummary)
            add("Independent" to state.independentlyCompletedObjectiveCount.toString())
            add("Supported" to state.supportedObjectiveCount.toString())
            add("Directly assisted" to state.directlyAssistedObjectiveCount.toString())
            add("Besser corrections" to state.besserCorrectionCount.toString())

            if (state.besserCorrectionCount > 0) {
                add("Besser improvement" to state.resolvedCorrectionLabel)
            }

            add("Target vocabulary" to state.vocabularyProgressLabel)
        }

    Column(verticalArrangement = Arrangement.spacedBy(TILE_SPACING)) {
        SectionLabel(text = "Mission evidence")

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(CARD_SHAPE)
                    .background(colorScheme.surfaceContainerLow)
                    .padding(vertical = 6.dp)
        ) {
            rows.forEachIndexed { index, (label, value) ->
                EvidenceRow(
                    label = label,
                    value = value,
                    showDivider = index < rows.lastIndex
                )
            }
        }
    }
}

@Composable
private fun EvidenceRow(
    label: String,
    value: String,
    showDivider: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }

        if (showDivider) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(0.5.dp)
                        .background(colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
        }
    }
}

/*
 * ===========================================================
 * OBJECTIVES
 * ===========================================================
 */

@Composable
private fun ObjectiveSection(
    title: String,
    objectiveTitles: List<String>,
    isStrong: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    val accent =
        if (isStrong) colorScheme.primary else colorScheme.tertiary

    val marker =
        if (isStrong) "✓" else "→"

    Column(verticalArrangement = Arrangement.spacedBy(TILE_SPACING)) {
        SectionLabel(text = title)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(CARD_SHAPE)
                    .background(colorScheme.surfaceContainerLow)
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            objectiveTitles.forEach { objectiveTitle ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = marker,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = objectiveTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/*
 * ===========================================================
 * SHARED
 * ===========================================================
 */

@Composable
private fun SectionLabel(
    text: String
) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Pill(
    text: String,
    background: Color,
    content: Color
) {
    Text(
        text = text,
        modifier =
            Modifier
                .clip(CircleShape)
                .background(background)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = content
    )
}

@Composable
private fun ContinueButton(
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier =
            Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(CONTINUE_BUTTON_HEIGHT)
                .clip(CircleShape)
                .background(colorScheme.primary)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Continue",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onPrimary
        )
    }
}

/*
 * ===========================================================
 * TOKENS
 * ===========================================================
 */

private val SHEET_SHAPE =
    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

private val CARD_SHAPE =
    RoundedCornerShape(24.dp)

private val TILE_SHAPE =
    RoundedCornerShape(20.dp)

private val SHEET_HORIZONTAL_PADDING =
    18.dp

private val SECTION_SPACING =
    18.dp

private val TILE_SPACING =
    10.dp

private val SCORE_RING_SIZE =
    132.dp

private val SCORE_RING_STROKE =
    10.dp

private val BAR_HEIGHT =
    5.dp

private val CONTINUE_BUTTON_HEIGHT =
    52.dp

private const val RING_ANIMATION_MILLIS =
    900

private const val BAR_ANIMATION_MILLIS =
    700

private const val STRONG_SCORE_THRESHOLD =
    75

private const val DEVELOPING_SCORE_THRESHOLD =
    50
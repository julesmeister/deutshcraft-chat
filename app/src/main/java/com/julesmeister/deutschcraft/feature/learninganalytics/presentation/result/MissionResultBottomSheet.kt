// app/src/main/java/com/julesmeister/deutschcraft/feature/learninganalytics/presentation/result/MissionResultBottomSheet.kt

package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionResultBottomSheet(
    state:
    MissionResultUiState,

    onDismiss:
        () -> Unit,

    onContinue:
        () -> Unit,

    modifier:
    Modifier =
        Modifier
) {
    if (!state.hasMissionResult) {
        return
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded =
                true
        )

    ModalBottomSheet(
        onDismissRequest =
            onDismiss,

        sheetState =
            sheetState,

        modifier =
            modifier,

        shape =
            SHEET_SHAPE,

        containerColor =
            MaterialTheme
                .colorScheme
                .surface,

        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,

        tonalElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal =
                            SHEET_HORIZONTAL_PADDING
                    )
                    .padding(
                        bottom =
                            16.dp
                    )
                    .navigationBarsPadding(),

            verticalArrangement =
                Arrangement.spacedBy(
                    SECTION_SPACING
                )
        ) {
            ResultHero(
                state =
                    state
            )

            CommunicationGrowthSection(
                state =
                    state
            )

            if (
                state.hasStrongObjectives
            ) {
                ObjectiveSection(
                    title =
                        "What you demonstrated",

                    subtitle =
                        "Conversation goals you handled well.",

                    objectiveTitles =
                        state.strongObjectiveTitles,

                    style =
                        ObjectiveSectionStyle.SUCCESS
                )
            }

            if (
                state.hasDemonstratedVocabulary
            ) {
                VocabularySection(
                    title =
                        "Words you demonstrated",

                    subtitle =
                        state.demonstratedVocabularyLabel,

                    vocabulary =
                        state.correctlyUsedTargetVocabulary,

                    isPositive =
                        true
                )
            }

            if (
                state.hasImprovementObjectives
            ) {
                ObjectiveSection(
                    title =
                        "Keep working on",

                    subtitle =
                        "These skills will continue appearing " +
                                "naturally in future conversations.",

                    objectiveTitles =
                        state.improvementObjectiveTitles,

                    style =
                        ObjectiveSectionStyle.NEXT
                )
            }

            if (
                state.hasUnusedVocabulary
            ) {
                VocabularySection(
                    title =
                        "Try next time",

                    subtitle =
                        "Target language that did not appear yet.",

                    vocabulary =
                        state.unusedTargetVocabulary,

                    isPositive =
                        false
                )
            }

            SupportSection(
                state =
                    state
            )

            NextFocusSection(
                state =
                    state
            )

            ContinueButton(
                onClick =
                    onContinue
            )
        }
    }
}

@Composable
private fun ResultHero(
    state:
    MissionResultUiState
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    HERO_SHAPE
                )
                .background(
                    colors.surfaceContainerHigh
                )
                .padding(
                    horizontal =
                        20.dp,

                    vertical =
                        22.dp
                ),

        horizontalAlignment =
            Alignment.Start
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            COMPLETE_ICON_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colors.primary.copy(
                                alpha =
                                    0.14f
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        "✓",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors.primary
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "Conversation complete",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors.primary
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )

                Text(
                    text =
                        state.missionTitle,

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        colors.onSurface,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (
                state.cefrLevelLabel.isNotBlank()
            ) {
                ResultPill(
                    text =
                        state.cefrLevelLabel,

                    background =
                        colors.primary.copy(
                            alpha =
                                0.13f
                        ),

                    content =
                        colors.primary
                )
            }

            ResultPill(
                text =
                    state.overallGrowthLabel,

                background =
                    growthColor(
                        label =
                            state.overallGrowthLabel
                    )
                        .copy(
                            alpha =
                                0.13f
                        ),

                content =
                    growthColor(
                        label =
                            state.overallGrowthLabel
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )

        Text(
            text =
                state.missionCompletionLabel,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                colors.onSurfaceVariant
        )
    }
}

private data class GrowthRowModel(
    val label:
    String,

    val growthLabel:
    String
)

@Composable
private fun CommunicationGrowthSection(
    state:
    MissionResultUiState
) {
    val rows =
        listOf(
            GrowthRowModel(
                label =
                    "Conversation",

                growthLabel =
                    state.conversationGrowthLabel
            ),

            GrowthRowModel(
                label =
                    "Independence",

                growthLabel =
                    state.independenceGrowthLabel
            ),

            GrowthRowModel(
                label =
                    "Grammar",

                growthLabel =
                    state.grammarGrowthLabel
            ),

            GrowthRowModel(
                label =
                    "Vocabulary",

                growthLabel =
                    state.vocabularyGrowthLabel
            ),

            GrowthRowModel(
                label =
                    "Comprehension",

                growthLabel =
                    state.comprehensionGrowthLabel
            ),

            GrowthRowModel(
                label =
                    "Interaction",

                growthLabel =
                    state.interactionGrowthLabel
            )
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                SECTION_CONTENT_SPACING
            )
    ) {
        SectionHeader(
            title =
                "Communication growth",

            subtitle =
                "What this conversation showed about your German."
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        CARD_SHAPE
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerLow
                    )
        ) {
            rows.forEachIndexed {
                    index,
                    row ->

                GrowthRow(
                    model =
                        row,

                    showDivider =
                        index <
                                rows.lastIndex
                )
            }
        }
    }
}

@Composable
private fun GrowthRow(
    model:
    GrowthRowModel,

    showDivider:
    Boolean
) {
    val colors =
        MaterialTheme.colorScheme

    val accent =
        growthColor(
            label =
                model.growthLabel
        )

    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,

                        vertical =
                            13.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            GROWTH_DOT_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            accent
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        12.dp
                    )
            )

            Text(
                text =
                    model.label,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    colors.onSurface
            )

            Spacer(
                modifier =
                    Modifier.width(
                        12.dp
                    )
            )

            Text(
                text =
                    model.growthLabel,

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    accent
            )
        }

        if (showDivider) {
            DividerLine()
        }
    }
}

private enum class ObjectiveSectionStyle {
    SUCCESS,
    NEXT
}

@Composable
private fun ObjectiveSection(
    title:
    String,

    subtitle:
    String,

    objectiveTitles:
    List<String>,

    style:
    ObjectiveSectionStyle
) {
    val colors =
        MaterialTheme.colorScheme

    val accent =
        when (style) {
            ObjectiveSectionStyle.SUCCESS ->
                colors.primary

            ObjectiveSectionStyle.NEXT ->
                colors.tertiary
        }

    val marker =
        when (style) {
            ObjectiveSectionStyle.SUCCESS ->
                "✓"

            ObjectiveSectionStyle.NEXT ->
                "→"
        }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                SECTION_CONTENT_SPACING
            )
    ) {
        SectionHeader(
            title =
                title,

            subtitle =
                subtitle
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        CARD_SHAPE
                    )
                    .background(
                        colors.surfaceContainerLow
                    )
                    .padding(
                        12.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            objectiveTitles.forEach {
                    objectiveTitle ->

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(
                                    OBJECTIVE_MARKER_SIZE
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    accent.copy(
                                        alpha =
                                            0.13f
                                    )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                marker,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                accent
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                12.dp
                            )
                    )

                    Text(
                        text =
                            objectiveTitle,

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabularySection(
    title:
    String,

    subtitle:
    String,

    vocabulary:
    List<String>,

    isPositive:
    Boolean
) {
    val colors =
        MaterialTheme.colorScheme

    val accent =
        if (isPositive) {
            colors.primary
        } else {
            colors.tertiary
        }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                SECTION_CONTENT_SPACING
            )
    ) {
        SectionHeader(
            title =
                title,

            subtitle =
                subtitle
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        CARD_SHAPE
                    )
                    .background(
                        colors.surfaceContainerLow
                    )
                    .padding(
                        12.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    VOCABULARY_ROW_SPACING
                )
        ) {
            vocabulary
                .chunked(
                    VOCABULARY_ITEMS_PER_ROW
                )
                .forEach {
                        rowVocabulary ->

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                VOCABULARY_CHIP_SPACING
                            )
                    ) {
                        rowVocabulary.forEach {
                                vocabularyItem ->

                            VocabularyChip(
                                text =
                                    vocabularyItem,

                                accent =
                                    accent,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }

                        repeat(
                            VOCABULARY_ITEMS_PER_ROW -
                                    rowVocabulary.size
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun VocabularyChip(
    text:
    String,

    accent:
    Color,

    modifier:
    Modifier =
        Modifier
) {
    Text(
        text =
            text,

        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    accent.copy(
                        alpha =
                            0.11f
                    )
                )
                .padding(
                    horizontal =
                        10.dp,

                    vertical =
                        9.dp
                ),

        style =
            MaterialTheme
                .typography
                .bodySmall,

        fontWeight =
            FontWeight.Medium,

        color =
            MaterialTheme
                .colorScheme
                .onSurface,

        textAlign =
            TextAlign.Center,

        maxLines =
            2,

        overflow =
            TextOverflow.Ellipsis
    )
}

@Composable
private fun SupportSection(
    state:
    MissionResultUiState
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                SECTION_CONTENT_SPACING
            )
    ) {
        SectionHeader(
            title =
                "Support",

            subtitle =
                "How much help you needed to keep the conversation moving."
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        CARD_SHAPE
                    )
                    .background(
                        colors.surfaceContainerLow
                    )
        ) {
            SupportRow(
                label =
                    "Independence",

                value =
                    state.independenceSummary,

                showDivider =
                    true
            )

            SupportRow(
                label =
                    "Hints",

                value =
                    state.hintSummary,

                showDivider =
                    state.besserSummary !=
                            null
            )

            state.besserSummary?.let {
                    besserSummary ->

                SupportRow(
                    label =
                        "Besser",

                    value =
                        besserSummary,

                    showDivider =
                        false
                )
            }
        }
    }
}

@Composable
private fun SupportRow(
    label:
    String,

    value:
    String,

    showDivider:
    Boolean
) {
    val colors =
        MaterialTheme.colorScheme

    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,

                        vertical =
                            13.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    label,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    colors.onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.width(
                        16.dp
                    )
            )

            Text(
                text =
                    value,

                modifier =
                    Modifier.weight(
                        1.6f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.Medium,

                textAlign =
                    TextAlign.End,

                color =
                    colors.onSurface
            )
        }

        if (showDivider) {
            DividerLine()
        }
    }
}

@Composable
private fun NextFocusSection(
    state:
    MissionResultUiState
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    CARD_SHAPE
                )
                .background(
                    colors.secondaryContainer.copy(
                        alpha =
                            0.55f
                    )
                )
                .padding(
                    16.dp
                )
    ) {
        Text(
            text =
                "Next focus",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            fontWeight =
                FontWeight.Bold,

            color =
                colors.onSecondaryContainer
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                state.nextFocusMessage,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                colors
                    .onSecondaryContainer
                    .copy(
                        alpha =
                            0.86f
                    )
        )
    }
}

@Composable
private fun SectionHeader(
    title:
    String,

    subtitle:
    String
) {
    Column(
        modifier =
            Modifier.padding(
                horizontal =
                    4.dp
            )
    ) {
        Text(
            text =
                title,

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        Spacer(
            modifier =
                Modifier.height(
                    3.dp
                )
        )

        Text(
            text =
                subtitle,

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun ResultPill(
    text:
    String,

    background:
    Color,

    content:
    Color
) {
    Text(
        text =
            text,

        modifier =
            Modifier
                .clip(
                    CircleShape
                )
                .background(
                    background
                )
                .padding(
                    horizontal =
                        12.dp,

                    vertical =
                        6.dp
                ),

        style =
            MaterialTheme
                .typography
                .labelMedium,

        fontWeight =
            FontWeight.SemiBold,

        color =
            content
    )
}

@Composable
private fun DividerLine() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp
                )
                .height(
                    0.5.dp
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha =
                                0.55f
                        )
                )
    )
}

@Composable
private fun growthColor(
    label:
    String
): Color {

    val colors =
        MaterialTheme.colorScheme

    return when (label) {
        "Strong" ->
            colors.primary

        "Improving" ->
            colors.tertiary

        "Practising" ->
            colors.secondary

        else ->
            colors.onSurfaceVariant
    }
}

@Composable
private fun ContinueButton(
    onClick:
        () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Box(
        modifier =
            Modifier
                .padding(
                    top =
                        4.dp
                )
                .fillMaxWidth()
                .height(
                    CONTINUE_BUTTON_HEIGHT
                )
                .clip(
                    CircleShape
                )
                .background(
                    colors.primary
                )
                .clickable(
                    onClick =
                        onClick
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                "Continue",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            fontWeight =
                FontWeight.Bold,

            color =
                colors.onPrimary
        )
    }
}

private val SHEET_SHAPE =
    RoundedCornerShape(
        topStart =
            28.dp,

        topEnd =
            28.dp
    )

private val HERO_SHAPE =
    RoundedCornerShape(
        26.dp
    )

private val CARD_SHAPE =
    RoundedCornerShape(
        22.dp
    )

private val SHEET_HORIZONTAL_PADDING =
    18.dp

private val SECTION_SPACING =
    20.dp

private val SECTION_CONTENT_SPACING =
    10.dp

private val COMPLETE_ICON_SIZE =
    44.dp

private val GROWTH_DOT_SIZE =
    8.dp

private val OBJECTIVE_MARKER_SIZE =
    26.dp

private val CONTINUE_BUTTON_HEIGHT =
    52.dp

private val VOCABULARY_ROW_SPACING =
    8.dp

private val VOCABULARY_CHIP_SPACING =
    8.dp

private const val VOCABULARY_ITEMS_PER_ROW =
    2
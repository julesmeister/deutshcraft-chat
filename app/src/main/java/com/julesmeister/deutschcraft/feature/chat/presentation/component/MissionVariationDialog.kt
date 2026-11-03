// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/component/MissionVariationDialog.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationOption
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationUiState
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MissionVariationDialog(
    state: MissionVariationUiState,
    onVariationSelected: (MissionVariationType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.canShowPicker) {
        return
    }

    val colors =
        MaterialTheme.colorScheme

    BasicAlertDialog(
        onDismissRequest =
            onDismiss,

        modifier =
            modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            DIALOG_HORIZONTAL_MARGIN
                    )
                    .clip(
                        DIALOG_SHAPE
                    )
                    .background(
                        colors.surfaceContainerHigh
                    )
                    .padding(
                        DIALOG_CONTENT_PADDING
                    )
        ) {
            MissionVariationHeader(
                missionTitle =
                    state.context
                        ?.missionTitle
                        .orEmpty(),

                onDismiss =
                    onDismiss
            )

            Text(
                text =
                    "Wie möchtest du die Mission weiterführen?",

                modifier =
                    Modifier.padding(
                        top =
                            DIALOG_PROMPT_TOP_PADDING,

                        bottom =
                            DIALOG_PROMPT_BOTTOM_PADDING
                    ),

                color =
                    colors.onSurfaceVariant,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )

            state.options.forEachIndexed { index, option ->
                MissionVariationActionItem(
                    option =
                        option,

                    enabled =
                        !state.isStarting,

                    onClick = {
                        onVariationSelected(
                            option.type
                        )
                    },

                    modifier =
                        if (index == 0) {
                            Modifier
                        } else {
                            Modifier.padding(
                                top =
                                    ACTION_ITEM_SPACING
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun MissionVariationHeader(
    missionTitle: String,
    onDismiss: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

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
                        HEADER_ICON_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        DeutschLime
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .Replay,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        HEADER_ICON_CONTENT_SIZE
                    ),

                tint =
                    DeutschTextOnLime
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        horizontal =
                            HEADER_CONTENT_PADDING
                    )
        ) {
            Text(
                text =
                    "Noch eine Runde?",

                color =
                    colors.onSurface,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    missionTitle,

                modifier =
                    Modifier.padding(
                        top =
                            HEADER_SUBTITLE_TOP_PADDING
                    ),

                color =
                    colors.onSurfaceVariant,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Box(
            modifier =
                Modifier
                    .size(
                        CLOSE_BUTTON_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        colors.surfaceContainerHighest
                    )
                    .clickable(
                        onClick =
                            onDismiss
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .Close,

                contentDescription =
                    "Schließen",

                modifier =
                    Modifier.size(
                        CLOSE_ICON_SIZE
                    ),

                tint =
                    colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MissionVariationActionItem(
    option: MissionVariationOption,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val presentation =
        option.presentation(
            colors =
                colors
        )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    ACTION_ITEM_SHAPE
                )
                .clickable(
                    enabled =
                        enabled,

                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        ACTION_HORIZONTAL_PADDING,

                    vertical =
                        ACTION_VERTICAL_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                ACTION_CONTENT_SPACING
            )
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ACTION_ICON_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        presentation.iconBackground
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    presentation.icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        ACTION_ICON_CONTENT_SIZE
                    ),

                tint =
                    presentation.iconColor
            )
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    option.title,

                color =
                    colors.onSurface,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    option.description,

                modifier =
                    Modifier.padding(
                        top =
                            ACTION_DESCRIPTION_TOP_PADDING
                    ),

                color =
                    colors.onSurfaceVariant,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}

private data class VariationPresentation(
    val icon: ImageVector,
    val iconBackground: Color,
    val iconColor: Color
)

@Composable
private fun MissionVariationOption.presentation(
    colors:
    androidx.compose.material3.ColorScheme
): VariationPresentation {
    return when (type) {
        MissionVariationType.COMPLICATION ->
            VariationPresentation(
                icon =
                    Icons
                        .Outlined
                        .AutoAwesome,

                iconBackground =
                    DeutschLime,

                iconColor =
                    DeutschTextOnLime
            )

        MissionVariationType.NEW_VOCABULARY ->
            VariationPresentation(
                icon =
                    Icons
                        .Outlined
                        .Translate,

                iconBackground =
                    colors.secondaryContainer,

                iconColor =
                    colors.onSecondaryContainer
            )

        MissionVariationType.GRAMMAR_STRETCH ->
            VariationPresentation(
                icon =
                    Icons
                        .Outlined
                        .Psychology,

                iconBackground =
                    colors.tertiaryContainer,

                iconColor =
                    colors.onTertiaryContainer
            )
    }
}

private val DIALOG_HORIZONTAL_MARGIN =
    20.dp

private val DIALOG_CONTENT_PADDING =
    20.dp

private val DIALOG_SHAPE =
    RoundedCornerShape(
        28.dp
    )

private val DIALOG_PROMPT_TOP_PADDING =
    20.dp

private val DIALOG_PROMPT_BOTTOM_PADDING =
    10.dp

private val HEADER_ICON_SIZE =
    48.dp

private val HEADER_ICON_CONTENT_SIZE =
    23.dp

private val HEADER_CONTENT_PADDING =
    12.dp

private val HEADER_SUBTITLE_TOP_PADDING =
    3.dp

private val CLOSE_BUTTON_SIZE =
    38.dp

private val CLOSE_ICON_SIZE =
    19.dp

private val ACTION_ITEM_SHAPE =
    RoundedCornerShape(
        20.dp
    )

private val ACTION_ITEM_SPACING =
    8.dp

private val ACTION_HORIZONTAL_PADDING =
    4.dp

private val ACTION_VERTICAL_PADDING =
    10.dp

private val ACTION_CONTENT_SPACING =
    14.dp

private val ACTION_ICON_SIZE =
    46.dp

private val ACTION_ICON_CONTENT_SIZE =
    22.dp

private val ACTION_DESCRIPTION_TOP_PADDING =
    2.dp
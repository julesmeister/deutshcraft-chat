package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onAction: (SettingsUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier =
            modifier.fillMaxSize(),
        containerColor =
            MaterialTheme
                .colorScheme
                .background,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .background,
                        scrolledContainerColor =
                            MaterialTheme
                                .colorScheme
                                .background,
                        titleContentColor =
                            MaterialTheme
                                .colorScheme
                                .onBackground,
                        navigationIconContentColor =
                            MaterialTheme
                                .colorScheme
                                .onBackground
                    ),
                navigationIcon = {
                    Box(
                        modifier =
                            Modifier
                                .padding(
                                    start =
                                        TOP_BAR_PADDING
                                )
                                .size(
                                    TOP_BAR_BUTTON_SIZE
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceContainer
                                )
                                .clickable(
                                    role =
                                        Role.Button,
                                    onClick =
                                        onBackClick
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons
                                    .AutoMirrored
                                    .Outlined
                                    .ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text =
                            "Settings",
                        modifier =
                            Modifier.padding(
                                start =
                                    TOP_BAR_TITLE_START_PADDING
                            ),
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start =
                        SCREEN_HORIZONTAL_PADDING,
                    top =
                        innerPadding.calculateTopPadding() +
                                SCREEN_TOP_PADDING,
                    end =
                        SCREEN_HORIZONTAL_PADDING,
                    bottom =
                        SCREEN_BOTTOM_PADDING
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SECTION_SPACING
                )
        ) {
            item(
                key =
                    APPEARANCE_SECTION_KEY
            ) {
                SettingsSection(
                    title =
                        "Appearance"
                ) {
                    ThemeSettings(
                        selectedTheme =
                            settings.appTheme,
                        onThemeSelected = { theme ->
                            onAction(
                                SettingsUiAction.ThemeChanged(
                                    theme =
                                        theme
                                )
                            )
                        }
                    )

                    SettingsDivider()

                    FontScaleSettings(
                        selectedOption =
                            settings.fontScaleOption,
                        onOptionSelected = { option ->
                            onAction(
                                SettingsUiAction.FontScaleChanged(
                                    option =
                                        option
                                )
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Animation,
                                contentDescription =
                                    null
                            )
                        },
                        title =
                            "Reduce animations",
                        description =
                            "Use fewer animated transitions",
                        checked =
                            settings.reduceAnimations,
                        onCheckedChange = { enabled ->
                            onAction(
                                SettingsUiAction
                                    .ReduceAnimationsChanged(
                                        enabled =
                                            enabled
                                    )
                            )
                        }
                    )
                }
            }

            item(
                key =
                    LEARNING_SUPPORT_SECTION_KEY
            ) {
                SettingsSection(
                    title =
                        "Learning support"
                ) {
                    SettingsSwitchRow(
                        icon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Lightbulb,
                                contentDescription =
                                    null
                            )
                        },
                        title =
                            "Mission hints",
                        description =
                            "Show contextual AI coaching hints",
                        checked =
                            settings.showMissionHints,
                        onCheckedChange = { enabled ->
                            onAction(
                                SettingsUiAction
                                    .ShowMissionHintsChanged(
                                        enabled =
                                            enabled
                                    )
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .RecordVoiceOver,
                                contentDescription =
                                    null
                            )
                        },
                        title =
                            "Example phrases",
                        description =
                            "Show German sentence examples",
                        checked =
                            settings.showExamplePhrases,
                        onCheckedChange = { enabled ->
                            onAction(
                                SettingsUiAction
                                    .ShowExamplePhrasesChanged(
                                        enabled =
                                            enabled
                                    )
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Translate,
                                contentDescription =
                                    null
                            )
                        },
                        title =
                            "Target vocabulary",
                        description =
                            "Show useful words for objectives",
                        checked =
                            settings.showTargetVocabulary,
                        onCheckedChange = { enabled ->
                            onAction(
                                SettingsUiAction
                                    .ShowTargetVocabularyChanged(
                                        enabled =
                                            enabled
                                    )
                            )
                        }
                    )
                }
            }

            item(
                key =
                    ABOUT_SECTION_KEY
            ) {
                SettingsSection(
                    title =
                        "About"
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                SETTINGS_ROW_PADDING
                            )
                    ) {
                        Text(
                            text =
                                "DeutschCraft",
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

                        Text(
                            text =
                                "Conversation-first German learning",
                            modifier =
                                Modifier.padding(
                                    top =
                                        DESCRIPTION_TOP_PADDING
                                ),
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
            }
        }
    }
}

/*
 * ===========================================================
 * THEME
 * ===========================================================
 */

@Composable
private fun ThemeSettings(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        modifier =
            Modifier.padding(
                SETTINGS_ROW_PADDING
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SettingsIconContainer {
                Icon(
                    imageVector =
                        Icons
                            .Outlined
                            .Palette,
                    contentDescription =
                        null
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        SETTINGS_ICON_SPACING
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "Theme",
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                Text(
                    text =
                        "Choose DeutschCraft's appearance",
                    modifier =
                        Modifier.padding(
                            top =
                                DESCRIPTION_TOP_PADDING
                        ),
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

        Spacer(
            modifier =
                Modifier.height(
                    OPTION_LIST_TOP_SPACING
                )
        )

        AppTheme.entries.forEach { theme ->
            key(theme) {
                SettingsRadioOptionRow(
                    selected =
                        selectedTheme == theme,
                    title =
                        theme.displayName,
                    icon = {
                        Icon(
                            imageVector =
                                when (theme) {
                                    AppTheme.DARK ->
                                        Icons
                                            .Outlined
                                            .DarkMode

                                    AppTheme.LIGHT ->
                                        Icons
                                            .Outlined
                                            .LightMode
                                },
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(
                                    OPTION_ICON_SIZE
                                ),
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    },
                    onClick = {
                        if (selectedTheme != theme) {
                            onThemeSelected(
                                theme
                            )
                        }
                    }
                )
            }
        }
    }
}

/*
 * ===========================================================
 * FONT SCALE
 * ===========================================================
 */

@Composable
private fun FontScaleSettings(
    selectedOption: FontScaleOption,
    onOptionSelected: (FontScaleOption) -> Unit
) {
    Column(
        modifier =
            Modifier.padding(
                SETTINGS_ROW_PADDING
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SettingsIconContainer {
                Icon(
                    imageVector =
                        Icons
                            .Outlined
                            .TextFields,
                    contentDescription =
                        null
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        SETTINGS_ICON_SPACING
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "Font size",
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                Text(
                    text =
                        "Adjust text throughout the app",
                    modifier =
                        Modifier.padding(
                            top =
                                DESCRIPTION_TOP_PADDING
                        ),
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

        Spacer(
            modifier =
                Modifier.height(
                    OPTION_LIST_TOP_SPACING
                )
        )

        FontScaleOption.entries.forEach { option ->
            key(option) {
                SettingsRadioOptionRow(
                    selected =
                        selectedOption == option,
                    title =
                        option.displayName,
                    onClick = {
                        if (selectedOption != option) {
                            onOptionSelected(
                                option
                            )
                        }
                    }
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            PREVIEW_TOP_PADDING
                    )
                    .clip(
                        PREVIEW_SHAPE
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                    )
                    .padding(
                        PREVIEW_PADDING
                    )
        ) {
            Text(
                text =
                    "Wie geht es dir heute?",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
                        .copy(
                            fontSize =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                                    .fontSize *
                                        selectedOption.scale
                        ),
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )
        }
    }
}

/*
 * ===========================================================
 * SHARED RADIO OPTION
 * ===========================================================
 */

@Composable
private fun SettingsRadioOptionRow(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    OPTION_SHAPE
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    role =
                        Role.RadioButton,
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        OPTION_HORIZONTAL_PADDING,
                    vertical =
                        OPTION_VERTICAL_PADDING
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        RadioButton(
            selected =
                selected,
            onClick =
                null,
            colors =
                RadioButtonDefaults.colors(
                    selectedColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    unselectedColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
        )

        Spacer(
            modifier =
                Modifier.width(
                    OPTION_TEXT_SPACING
                )
        )

        if (icon != null) {
            icon()

            Spacer(
                modifier =
                    Modifier.width(
                        OPTION_ICON_TEXT_SPACING
                    )
            )
        }

        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )
    }
}

/*
 * ===========================================================
 * SECTION
 * ===========================================================
 */

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text =
                title,
            modifier =
                Modifier.padding(
                    start =
                        SECTION_TITLE_PADDING,
                    bottom =
                        SECTION_TITLE_BOTTOM_PADDING
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .primary,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.SemiBold
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        SETTINGS_SECTION_SHAPE
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainer
                    )
                    .padding(
                        vertical =
                            SECTION_VERTICAL_PADDING
                    )
        ) {
            content()
        }
    }
}

/*
 * ===========================================================
 * SWITCH ROW
 * ===========================================================
 */

@Composable
private fun SettingsSwitchRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    role =
                        Role.Switch
                ) {
                    onCheckedChange(
                        !checked
                    )
                }
                .padding(
                    SETTINGS_ROW_PADDING
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SettingsIconContainer(
            content =
                icon
        )

        Spacer(
            modifier =
                Modifier.width(
                    SETTINGS_ICON_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Text(
                text =
                    description,
                modifier =
                    Modifier.padding(
                        top =
                            DESCRIPTION_TOP_PADDING
                    ),
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

        Spacer(
            modifier =
                Modifier.width(
                    SWITCH_SPACING
                )
        )

        Switch(
            checked =
                checked,
            onCheckedChange =
                null,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor =
                        MaterialTheme
                            .colorScheme
                            .onPrimary,
                    checkedTrackColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    checkedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    uncheckedThumbColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    uncheckedTrackColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHighest,
                    uncheckedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .outline
                )
        )
    }
}

/*
 * ===========================================================
 * ICON CONTAINER
 * ===========================================================
 */

@Composable
private fun SettingsIconContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            Modifier
                .size(
                    SETTINGS_ICON_SIZE
                )
                .clip(
                    RoundedCornerShape(
                        SETTINGS_ICON_RADIUS
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha =
                                SETTINGS_ICON_BACKGROUND_ALPHA
                        )
                ),
        contentAlignment =
            Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                    MaterialTheme
                        .colorScheme
                        .primary
        ) {
            content()
        }
    }
}

/*
 * ===========================================================
 * DIVIDER
 * ===========================================================
 */

@Composable
private fun SettingsDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        DIVIDER_HORIZONTAL_PADDING
                )
                .height(
                    DIVIDER_HEIGHT
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha =
                                DIVIDER_ALPHA
                        )
                )
    )
}

/*
 * ===========================================================
 * LAZY COLUMN KEYS
 * ===========================================================
 */

private const val APPEARANCE_SECTION_KEY =
    "appearance"

private const val LEARNING_SUPPORT_SECTION_KEY =
    "learning_support"

private const val ABOUT_SECTION_KEY =
    "about"

/*
 * ===========================================================
 * SHAPES
 * ===========================================================
 */

private val SETTINGS_SECTION_SHAPE =
    RoundedCornerShape(
        26.dp
    )

private val OPTION_SHAPE =
    RoundedCornerShape(
        16.dp
    )

private val PREVIEW_SHAPE =
    RoundedCornerShape(
        18.dp
    )

/*
 * ===========================================================
 * LAYOUT
 * ===========================================================
 */

private val SCREEN_HORIZONTAL_PADDING =
    16.dp

private val SCREEN_TOP_PADDING =
    12.dp

private val SCREEN_BOTTOM_PADDING =
    40.dp

private val SECTION_SPACING =
    24.dp

private val SECTION_TITLE_PADDING =
    8.dp

private val SECTION_TITLE_BOTTOM_PADDING =
    8.dp

private val SECTION_VERTICAL_PADDING =
    6.dp

private val SETTINGS_ROW_PADDING =
    16.dp

private val SETTINGS_ICON_SIZE =
    40.dp

private val SETTINGS_ICON_RADIUS =
    14.dp

private val SETTINGS_ICON_SPACING =
    12.dp

private val DESCRIPTION_TOP_PADDING =
    2.dp

private val OPTION_LIST_TOP_SPACING =
    12.dp

private val OPTION_HORIZONTAL_PADDING =
    4.dp

private val OPTION_VERTICAL_PADDING =
    2.dp

private val OPTION_TEXT_SPACING =
    4.dp

private val OPTION_ICON_SIZE =
    20.dp

private val OPTION_ICON_TEXT_SPACING =
    8.dp

private val PREVIEW_TOP_PADDING =
    12.dp

private val PREVIEW_PADDING =
    16.dp

private val SWITCH_SPACING =
    8.dp

private val DIVIDER_HORIZONTAL_PADDING =
    16.dp

private val DIVIDER_HEIGHT =
    1.dp

private val TOP_BAR_PADDING =
    8.dp

private val TOP_BAR_BUTTON_SIZE =
    40.dp

private val TOP_BAR_TITLE_START_PADDING =
    8.dp

private const val SETTINGS_ICON_BACKGROUND_ALPHA =
    0.14f

private const val DIVIDER_ALPHA =
    0.45f
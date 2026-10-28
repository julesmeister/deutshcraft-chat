package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import com.julesmeister.deutschcraft.feature.settings.presentation.component.FontScalePicker
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsAboutCard
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsDivider
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsHeader
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsLabeledBlock
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsSection
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsSwitchRow
import com.julesmeister.deutschcraft.feature.settings.presentation.component.SettingsTopBar
import com.julesmeister.deutschcraft.feature.settings.presentation.component.ThemePicker
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onAction: (SettingsUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopBar(
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Tokens.ScreenHorizontalPadding,
                top = innerPadding.calculateTopPadding() + Tokens.ScreenTopPadding,
                end = Tokens.ScreenHorizontalPadding,
                bottom = Tokens.ScreenBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Tokens.SectionSpacing)
        ) {
            item(key = Tokens.HeaderKey) {
                SettingsHeader()
            }

            item(key = Tokens.AppearanceSectionKey) {
                SettingsSection(title = "Appearance") {
                    SettingsLabeledBlock(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        description = "Choose DeutschCraft's appearance"
                    ) {
                        ThemePicker(
                            selectedTheme = settings.appTheme,
                            onThemeSelected = { theme ->
                                onAction(SettingsUiAction.ThemeChanged(theme = theme))
                            }
                        )
                    }

                    SettingsDivider()

                    SettingsLabeledBlock(
                        icon = Icons.Outlined.TextFields,
                        title = "Font size",
                        description = "Adjust text throughout the app"
                    ) {
                        FontScalePicker(
                            selectedOption = settings.fontScaleOption,
                            onOptionSelected = { option ->
                                onAction(SettingsUiAction.FontScaleChanged(option = option))
                            }
                        )
                    }

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = Icons.Outlined.Animation,
                        title = "Reduce animations",
                        description = "Use fewer animated transitions",
                        checked = settings.reduceAnimations,
                        onCheckedChange = { enabled ->
                            onAction(SettingsUiAction.ReduceAnimationsChanged(enabled = enabled))
                        }
                    )
                }
            }

            item(key = Tokens.LearningSupportSectionKey) {
                SettingsSection(title = "Learning support") {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Lightbulb,
                        title = "Mission hints",
                        description = "Show contextual AI coaching hints",
                        checked = settings.showMissionHints,
                        onCheckedChange = { enabled ->
                            onAction(SettingsUiAction.ShowMissionHintsChanged(enabled = enabled))
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = Icons.Outlined.RecordVoiceOver,
                        title = "Example phrases",
                        description = "Show German sentence examples",
                        checked = settings.showExamplePhrases,
                        onCheckedChange = { enabled ->
                            onAction(SettingsUiAction.ShowExamplePhrasesChanged(enabled = enabled))
                        }
                    )

                    SettingsDivider()

                    SettingsSwitchRow(
                        icon = Icons.Outlined.Translate,
                        title = "Target vocabulary",
                        description = "Show useful words for objectives",
                        checked = settings.showTargetVocabulary,
                        onCheckedChange = { enabled ->
                            onAction(SettingsUiAction.ShowTargetVocabularyChanged(enabled = enabled))
                        }
                    )
                }
            }

            item(key = Tokens.AboutSectionKey) {
                SettingsSection(title = "About") {
                    SettingsAboutCard()
                }
            }
        }
    }
}
package com.julesmeister.deutschcraft.feature.settings.presentation

import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption
import com.julesmeister.deutschcraft.feature.settings.domain.ProgressLanguage

sealed interface SettingsUiAction {

    data class ThemeChanged(
        val theme: AppTheme
    ) : SettingsUiAction

    data class ProgressLanguageChanged(
        val language: ProgressLanguage
    ) : SettingsUiAction

    data class FontScaleChanged(
        val option: FontScaleOption
    ) : SettingsUiAction

    data class ReduceAnimationsChanged(
        val enabled: Boolean
    ) : SettingsUiAction

    data class ShowMissionHintsChanged(
        val enabled: Boolean
    ) : SettingsUiAction

    data class ShowExamplePhrasesChanged(
        val enabled: Boolean
    ) : SettingsUiAction

    data class ShowTargetVocabularyChanged(
        val enabled: Boolean
    ) : SettingsUiAction
}
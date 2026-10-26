package com.julesmeister.deutschcraft.feature.settings.presentation

import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption

sealed interface SettingsUiAction {

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
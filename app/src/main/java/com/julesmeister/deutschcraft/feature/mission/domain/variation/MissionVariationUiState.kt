// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/variation/MissionVariationUiState.kt

package com.julesmeister.deutschcraft.feature.mission.domain.variation

data class MissionVariationUiState(
    val context: MissionVariationContext? =
        null,

    val options: List<MissionVariationOption> =
        emptyList(),

    val isPickerVisible: Boolean =
        false,

    val isStarting: Boolean =
        false
) {

    val hasContext: Boolean
        get() =
            context != null

    val hasContinuation: Boolean
        get() =
            context != null

    val canShowPicker: Boolean
        get() =
            isPickerVisible &&
                    context != null &&
                    options.isNotEmpty()
}
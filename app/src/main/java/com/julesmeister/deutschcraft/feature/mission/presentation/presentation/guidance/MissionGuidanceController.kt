package com.julesmeister.deutschcraft.feature.mission.presentation.guidance

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionLanguageSupportRepository
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionLanguageSupportRequest
import com.julesmeister.deutschcraft.feature.settings.data.SettingsRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase.RecordMissionObjectiveHintUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.ActiveMissionConversationContext
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionConversationContextMapper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/*
 * Presentation controller for learner-facing mission help.
 *
 * Responsibilities:
 *
 * - Reflect the current active mission objective
 * - Expand and collapse mission header details
 * - Reveal coaching hints progressively
 * - Persist only hints explicitly revealed by the learner
 * - Restore already revealed hints from Room
 * - Toggle target vocabulary
 * - Toggle example phrases
 *
 * Vocabulary and examples do not currently count as coaching
 * hint usage.
 */
@Singleton
class MissionGuidanceController
@Inject constructor(
    private val recordMissionObjectiveHint:
    RecordMissionObjectiveHintUseCase,

    private val hintUsageRepository:
    MissionObjectiveHintUsageRepository,

    /*
     * Reuse the existing mission -> conversation-context
     * mapper.
     *
     * This prevents presentation code from duplicating the
     * mission mapping already used by Emil.
     */
    private val missionConversationContextMapper:
    MissionConversationContextMapper,

    private val languageSupportRepository: MissionLanguageSupportRepository,
    private val settingsRepository: SettingsRepository
) {
    private var languageSupportRequest: MissionLanguageSupportRequest? = null
    private var languageSupportJob: Job? = null

    private val _uiState =
        MutableStateFlow(
            MissionGuidanceUiState()
        )

    val uiState:
            StateFlow<MissionGuidanceUiState> =
        _uiState.asStateFlow()

    private var restoreJob:
            Job? =
        null

    private var revealJob:
            Job? =
        null

    /*
     * Lifecycle-aware scope supplied by ChatViewModel.
     *
     * The Room-backed mission observer itself is synchronous,
     * but restoring persisted hint usage requires a coroutine.
     */
    private var controllerScope:
            CoroutineScope? =
        null

    fun attachScope(
        scope:
        CoroutineScope
    ) {
        controllerScope =
            scope

        /*
         * If mission information arrived before the scope was
         * attached, restore its persisted hints now.
         */
        val state =
            _uiState.value

        if (state.hasActiveMission) {
            restoreRevealedHints(
                scope =
                    scope
            )
        }
    }

    /*
     * Entry point used by ChatSessionMissionController.
     *
     * ConversationMission remains the authoritative
     * Room-backed mission model.
     *
     * MissionConversationContextMapper converts it into the
     * conversation-facing model already shared with Emil.
     */
    fun updateMission(
        mission:
        ConversationMission
    ) {
        val context =
            missionConversationContextMapper.map(
                mission =
                    mission
            )

        if (context == null) {
            clear()

            return
        }

        val scope =
            controllerScope

        if (scope == null) {
            applyContextWithoutRestore(
                context =
                    context
            )
            updateMissionDetails(mission)

            return
        }

        updateMissionContext(
            context =
                context,
            scope =
                scope
        )
        updateMissionDetails(mission)
    }

    private fun updateMissionDetails(mission: ConversationMission) {
        val request = MissionLanguageSupportRequest(
            missionId = mission.id,
            templateId = mission.template.id,
            cefrLevel = CefrLevel.valueOf(mission.template.difficulty.name),
            title = mission.template.title,
            scenario = mission.template.scenario,
            learnerRole = mission.template.learnerRole,
            objectives = mission.template.objectives.sortedBy { it.sequenceNumber }
                .map { "${it.title}: ${it.description}" }
        )
        val changed = request != languageSupportRequest
        if (changed) {
            languageSupportJob?.cancel()
            languageSupportJob = null
            languageSupportRequest = request
        }
        _uiState.update { state ->
            state.copy(
                scenario = request.scenario,
                cefrLevel = request.cefrLevel,
                objectives = mission.objectiveProgress.sortedBy { it.objective.sequenceNumber }
                    .map { MissionGuidanceGoal(it.objective.id, it.objective.title, it.isCompleted) },
                languageSupport = if (changed) null else state.languageSupport,
                isLoadingLanguageSupport = if (changed) false else state.isLoadingLanguageSupport,
                languageSupportError = if (changed) null else state.languageSupportError
            )
        }
        if (_uiState.value.isVisible) {
            loadLanguageSupport()
        }
    }

    private fun loadLanguageSupport() {
        val request = languageSupportRequest ?: return
        val scope = controllerScope ?: return
        if (!_uiState.value.isVisible || _uiState.value.languageSupport != null ||
            _uiState.value.languageSupportError != null ||
            languageSupportJob?.isActive == true
        ) return
        _uiState.update { it.copy(isLoadingLanguageSupport = true, languageSupportError = null) }
        languageSupportJob = scope.launch {
            try {
                val settings = settingsRepository.settings.first()
                if (!settings.showTargetVocabulary && !settings.showExamplePhrases) {
                    _uiState.update { it.copy(isLoadingLanguageSupport = false) }
                    return@launch
                }
                val support = languageSupportRepository.load(request)
                if (request == languageSupportRequest) {
                    _uiState.update {
                        it.copy(languageSupport = support, isLoadingLanguageSupport = false)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                Log.e(CONTROLLER_TAG, "Mission language support could not be loaded.", exception)
                if (request == languageSupportRequest) {
                    _uiState.update {
                        it.copy(
                            isLoadingLanguageSupport = false,
                            languageSupportError = "Language support could not be loaded. Tap to retry."
                        )
                    }
                }
            }
        }
    }

    /*
     * Lower-level context API.
     *
     * This may also be used directly anywhere that already
     * owns ActiveMissionConversationContext.
     */
    fun updateMissionContext(
        context:
        ActiveMissionConversationContext?,
        scope:
        CoroutineScope
    ) {
        controllerScope =
            scope

        if (context == null) {
            clear()

            return
        }

        val currentState =
            _uiState.value

        val sameObjective =
            currentState.missionId ==
                    context.missionId &&
                    currentState.objectiveId ==
                    context.currentObjective.id

        if (sameObjective) {
            /*
             * Same objective:
             *
             * Refresh mission data but preserve:
             *
             * - sheet visibility
             * - revealed hints
             * - vocabulary expansion
             * - example expansion
             */
            _uiState.update { state ->
                state.copy(
                    missionTitle =
                        context
                            .title
                            .trim(),

                    objectiveTitle =
                        context
                            .currentObjective
                            .title
                            .trim(),

                    objectiveDescription =
                        context
                            .currentObjective
                            .description
                            .trim(),

                    progressPercentage =
                        context.safeProgressPercentage,

                    coachingHints =
                        context
                            .currentObjective
                            .coachingHints
                            .sanitizedHints(),

                    targetVocabulary =
                        context
                            .currentObjective
                            .targetVocabulary
                            .sanitizedVocabulary(),

                    examplePhrases =
                        context
                            .currentObjective
                            .examplePhrases
                            .sanitizedExamples()
                )
            }

            return
        }

        /*
         * A new objective became active.
         *
         * Keep the sheet open if the learner already had it
         * open, but reset objective-specific support state.
         */
        val wasVisible =
            currentState.isVisible && currentState.missionId == context.missionId

        restoreJob?.cancel()
        revealJob?.cancel()

        restoreJob =
            null

        revealJob =
            null

        _uiState.value =
            MissionGuidanceUiState
                .from(
                    context =
                        context
                )
                .copy(
                    isVisible =
                        wasVisible,
                    languageSupport = currentState.languageSupport,
                    isLoadingLanguageSupport = currentState.isLoadingLanguageSupport,
                    languageSupportError = currentState.languageSupportError
                )

        /*
         * Restore coaching hints previously revealed for this
         * exact mission/objective.
         */
        restoreRevealedHints(
            scope =
                scope
        )
    }

    /*
     * Expands mission header details and loads language support on demand.
     *
     * Tapping the mission top bar calls this method.
     */
    fun show() {
        _uiState.update { state ->
            if (!state.hasActiveMission) {
                state
            } else {
                state.copy(
                    isVisible =
                        true,
                    errorMessage =
                        null,
                    languageSupportError = null
                )
            }
        }
        loadLanguageSupport()
    }

    /*
     * Closes the sheet without forgetting learner assistance.
     *
     * Revealed coaching hints remain visible when the learner
     * reopens the sheet because those hints have already been
     * consumed and persisted.
     */
    fun dismiss() {
        _uiState.update { state ->
            state.copy(
                isVisible =
                    false,
                isVocabularyVisible =
                    false,
                isExamplesVisible =
                    false,
                errorMessage =
                    null
            )
        }
    }

    /*
     * Reveals exactly one additional coaching hint.
     *
     * Progression:
     *
     * hint index 0 -> assistance level 1
     * hint index 1 -> assistance level 2
     * hint index 2 -> assistance level 3
     */
    fun revealNextHint(
        scope:
        CoroutineScope
    ) {
        controllerScope =
            scope

        if (
            revealJob?.isActive ==
            true
        ) {
            return
        }

        val state =
            _uiState.value

        val missionId =
            state
                .missionId
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return

        val objectiveId =
            state
                .objectiveId
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return

        val hintIndex =
            state.nextHintIndex
                ?: return

        val hintText =
            state
                .coachingHints
                .getOrNull(
                    hintIndex
                )
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return

        _uiState.update { currentState ->
            currentState.copy(
                isRevealingHint =
                    true,
                errorMessage =
                    null
            )
        }

        revealJob =
            scope.launch {
                try {
                    /*
                     * Persist BEFORE exposing the coaching hint
                     * to the learner.
                     *
                     * This ensures independence analytics
                     * cannot lose learner-visible assistance.
                     */
                    recordMissionObjectiveHint(
                        conversationMissionId =
                            missionId,
                        objectiveId =
                            objectiveId,
                        hintIndex =
                            hintIndex,
                        hintText =
                            hintText
                    )

                    val latestState =
                        _uiState.value

                    /*
                     * The objective may have advanced while
                     * Room persistence was occurring.
                     */
                    if (
                        latestState.missionId !=
                        missionId ||
                        latestState.objectiveId !=
                        objectiveId
                    ) {
                        return@launch
                    }

                    val visibleHints =
                        latestState
                            .coachingHints
                            .take(
                                hintIndex +
                                        1
                            )

                    _uiState.update { currentState ->
                        if (
                            currentState.missionId !=
                            missionId ||
                            currentState.objectiveId !=
                            objectiveId
                        ) {
                            currentState
                        } else {
                            currentState.copy(
                                revealedHints =
                                    visibleHints,
                                isRevealingHint =
                                    false,
                                errorMessage =
                                    null
                            )
                        }
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        CONTROLLER_TAG,
                        "Mission hint could not be recorded.",
                        throwable
                    )

                    _uiState.update { currentState ->
                        if (
                            currentState.missionId !=
                            missionId ||
                            currentState.objectiveId !=
                            objectiveId
                        ) {
                            currentState
                        } else {
                            currentState.copy(
                                isRevealingHint =
                                    false,
                                errorMessage =
                                    HINT_ERROR
                            )
                        }
                    }
                } finally {
                    revealJob =
                        null
                }
            }
    }

    /*
     * Uses the lifecycle scope already attached by
     * ChatViewModel.
     */
    fun revealNextHint() {
        val scope =
            controllerScope
                ?: return

        revealNextHint(
            scope =
                scope
        )
    }

    fun toggleVocabulary() {
        _uiState.update { state ->
            if (!state.hasActiveMission) {
                state
            } else {
                state.copy(
                    isVocabularyVisible =
                        !state.isVocabularyVisible
                )
            }
        }
    }

    fun toggleExamples() {
        _uiState.update { state ->
            if (!state.hasActiveMission) {
                state
            } else {
                state.copy(
                    isExamplesVisible =
                        !state.isExamplesVisible
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { state ->
            state.copy(
                errorMessage =
                    null
            )
        }
    }

    /*
     * Completely removes guidance for the active mission.
     *
     * Called when the Room observer reports that there is no
     * active mission anymore.
     */
    fun clear() {
        languageSupportJob?.cancel()
        languageSupportJob = null
        languageSupportRequest = null
        restoreJob?.cancel()
        revealJob?.cancel()

        restoreJob =
            null

        revealJob =
            null

        _uiState.value =
            MissionGuidanceUiState()
    }

    /*
     * Cancels asynchronous work when the owning chat
     * lifecycle ends.
     */
    fun cancel() {
        languageSupportJob?.cancel()
        languageSupportJob = null
        restoreJob?.cancel()
        revealJob?.cancel()

        restoreJob =
            null

        revealJob =
            null

        controllerScope =
            null
    }

    /*
     * Mission updates may arrive before ChatViewModel has
     * attached its lifecycle scope.
     *
     * In that case update the presentation immediately, then
     * attachScope() will restore persisted hint history later.
     */
    private fun applyContextWithoutRestore(
        context:
        ActiveMissionConversationContext
    ) {
        val currentState =
            _uiState.value

        val sameObjective =
            currentState.missionId ==
                    context.missionId &&
                    currentState.objectiveId ==
                    context.currentObjective.id

        if (sameObjective) {
            _uiState.update { state ->
                state.copy(
                    missionTitle =
                        context
                            .title
                            .trim(),

                    objectiveTitle =
                        context
                            .currentObjective
                            .title
                            .trim(),

                    objectiveDescription =
                        context
                            .currentObjective
                            .description
                            .trim(),

                    progressPercentage =
                        context.safeProgressPercentage,

                    coachingHints =
                        context
                            .currentObjective
                            .coachingHints
                            .sanitizedHints(),

                    targetVocabulary =
                        context
                            .currentObjective
                            .targetVocabulary
                            .sanitizedVocabulary(),

                    examplePhrases =
                        context
                            .currentObjective
                            .examplePhrases
                            .sanitizedExamples()
                )
            }
        } else {
            val wasVisible =
                currentState.isVisible

            _uiState.value =
                MissionGuidanceUiState
                    .from(
                        context =
                            context
                    )
                    .copy(
                        isVisible =
                            wasVisible
                    )
        }
    }

    /*
     * Restores already persisted hint usage for the active
     * objective.
     */
    private fun restoreRevealedHints(
        scope:
        CoroutineScope
    ) {
        restoreJob?.cancel()

        val state =
            _uiState.value

        val missionId =
            state
                .missionId
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return

        val objectiveId =
            state
                .objectiveId
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return

        restoreJob =
            scope.launch {
                try {
                    val persistedUsage =
                        hintUsageRepository
                            .getObjectiveHintUsage(
                                conversationMissionId =
                                    missionId,
                                objectiveId =
                                    objectiveId
                            )

                    /*
                     * Persisted hint levels are 1, 2, and 3.
                     *
                     * Therefore the highest persisted level is
                     * also the number of progressive hints that
                     * should be visible.
                     */
                    val highestPersistedLevel =
                        persistedUsage
                            .maxOfOrNull { usage ->
                                usage.hintLevel
                            }
                            ?.coerceIn(
                                minimumValue =
                                    0,
                                maximumValue =
                                    MissionGuidanceUiState
                                        .MAXIMUM_HINT_COUNT
                            )
                            ?: 0

                    val latestState =
                        _uiState.value

                    /*
                     * Ignore stale restoration after an
                     * objective transition.
                     */
                    if (
                        latestState.missionId !=
                        missionId ||
                        latestState.objectiveId !=
                        objectiveId
                    ) {
                        return@launch
                    }

                    _uiState.update { currentState ->
                        if (
                            currentState.missionId !=
                            missionId ||
                            currentState.objectiveId !=
                            objectiveId
                        ) {
                            currentState
                        } else {
                            currentState.copy(
                                revealedHints =
                                    currentState
                                        .coachingHints
                                        .take(
                                            highestPersistedLevel
                                        ),
                                errorMessage =
                                    null
                            )
                        }
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    /*
                     * Restoration failure should not make the
                     * mission UI unusable.
                     */
                    Log.e(
                        CONTROLLER_TAG,
                        "Mission hint history could not be " +
                                "restored.",
                        throwable
                    )
                } finally {
                    restoreJob =
                        null
                }
            }
    }

    private fun List<String>.sanitizedHints():
            List<String> {
        return sanitizedValues(
            maximumSize =
                MissionGuidanceUiState
                    .MAXIMUM_HINT_COUNT
        )
    }

    private fun List<String>.sanitizedVocabulary():
            List<String> {
        return sanitizedValues(
            maximumSize =
                MAXIMUM_VOCABULARY_COUNT
        )
    }

    private fun List<String>.sanitizedExamples():
            List<String> {
        return sanitizedValues(
            maximumSize =
                MAXIMUM_EXAMPLE_COUNT
        )
    }

    private fun List<String>.sanitizedValues(
        maximumSize: Int
    ): List<String> {
        if (maximumSize <= 0) {
            return emptyList()
        }

        return asSequence()
            .map(
                String::trim
            )
            .filter(
                String::isNotBlank
            )
            .distinct()
            .take(
                maximumSize
            )
            .toList()
    }

    private companion object {
        const val CONTROLLER_TAG =
            "MissionGuidance"

        const val HINT_ERROR =
            "Der Hinweis konnte nicht geladen werden."

        const val MAXIMUM_VOCABULARY_COUNT =
            4

        const val MAXIMUM_EXAMPLE_COUNT =
            2
    }
}
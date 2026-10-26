package com.julesmeister.deutschcraft.feature.adaptivelearning.data

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningDao
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.adaptivelearning.AdaptiveLearningFocusEntity
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationSkill
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningFocusType
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.MasteryState
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalFirstAdaptiveLearningRepository
@Inject constructor(
    private val adaptiveLearningDao:
    AdaptiveLearningDao,
    private val clock:
    Clock
) : AdaptiveLearningRepository {

    override fun observeAllFocuses():
            Flow<List<AdaptiveLearningFocus>> {
        return adaptiveLearningDao
            .observeAllFocuses()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun observeReinforcementFocuses():
            Flow<List<AdaptiveLearningFocus>> {
        return adaptiveLearningDao
            .observeReinforcementFocuses()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun observeMasteredFocuses():
            Flow<List<AdaptiveLearningFocus>> {
        return adaptiveLearningDao
            .observeMasteredFocuses()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override fun observeFocusesByType(
        type: LearningFocusType
    ): Flow<List<AdaptiveLearningFocus>> {
        return adaptiveLearningDao
            .observeFocusesByType(
                type =
                    type.name
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun getFocusByKey(
        focusKey: String
    ): AdaptiveLearningFocus? {
        val normalizedFocusKey =
            focusKey.normalizeFocusKey()

        if (normalizedFocusKey.isBlank()) {
            return null
        }

        return adaptiveLearningDao
            .getFocusByKey(
                focusKey =
                    normalizedFocusKey
            )
            ?.toDomain()
    }

    override suspend fun getTopReinforcementFocuses(
        minimumConfidence: Float,
        limit: Int
    ): List<AdaptiveLearningFocus> {
        if (limit <= 0) {
            return emptyList()
        }

        return adaptiveLearningDao
            .getTopReinforcementFocuses(
                minimumConfidence =
                    minimumConfidence.coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            1f
                    ),
                limit =
                    limit
            )
            .map { entity ->
                entity.toDomain()
            }
    }

    override suspend fun saveFocus(
        focus: AdaptiveLearningFocus
    ) {
        val normalizedFocusKey =
            focus.focusKey.normalizeFocusKey()

        require(
            focus.id.isNotBlank()
        ) {
            "Adaptive-learning focus requires an ID."
        }

        require(
            normalizedFocusKey.isNotBlank()
        ) {
            "Adaptive-learning focus requires a focus key."
        }

        val now =
            clock.millis()

        /*
         * The stable focus key is authoritative.
         *
         * Search by key first so a newly generated ID cannot
         * replace or conflict with an existing focus that
         * represents the same learner pattern.
         */
        val existingEntity =
            adaptiveLearningDao.getFocusByKey(
                focusKey =
                    normalizedFocusKey
            )
                ?: adaptiveLearningDao.getFocusById(
                    focusId =
                        focus.id
                )

        val persistedFocusId =
            existingEntity?.id
                ?: focus.id

        val focusEntity =
            focus
                .copy(
                    id =
                        persistedFocusId,
                    focusKey =
                        normalizedFocusKey
                )
                .toEntity(
                    createdAt =
                        existingEntity?.createdAt
                            ?: now,
                    updatedAt =
                        now
                )

        adaptiveLearningDao.upsertFocus(
            focus =
                focusEntity
        )
    }

    override suspend fun recordEvidence(
        focus: AdaptiveLearningFocus,
        messageId: String,
        conversationId: String?,
        result: LearningEvidenceResult,
        confidence: Float,
        reason: String,
        learnerText: String,
        correctedText: String?,
        levelAppropriateText: String?,
        observedAt: Long
    ) {
        val normalizedFocusKey =
            focus.focusKey.normalizeFocusKey()

        require(
            focus.id.isNotBlank()
        ) {
            "Adaptive-learning evidence requires a focus ID."
        }

        require(
            normalizedFocusKey.isNotBlank()
        ) {
            "Adaptive-learning evidence requires a focus key."
        }

        require(
            messageId.isNotBlank()
        ) {
            "Adaptive-learning evidence requires a message ID."
        }

        require(
            learnerText.isNotBlank()
        ) {
            "Adaptive-learning evidence requires learner text."
        }

        val normalizedMessageId =
            messageId.trim()

        /*
         * Search using the stable focus key first.
         *
         * The caller may create a temporary UUID for a focus
         * before discovering that the same focus already
         * exists in Room.
         */
        val existingEntity =
            adaptiveLearningDao.getFocusByKey(
                focusKey =
                    normalizedFocusKey
            )
                ?: adaptiveLearningDao.getFocusById(
                    focusId =
                        focus.id
                )

        val persistedFocusId =
            existingEntity?.id
                ?: focus.id

        /*
         * Prevent the same learner message from contributing
         * evidence twice for the same persistent focus.
         */
        val evidenceAlreadyExists =
            adaptiveLearningDao.evidenceExists(
                focusId =
                    persistedFocusId,
                messageId =
                    normalizedMessageId
            )

        if (evidenceAlreadyExists) {
            return
        }

        val now =
            clock.millis()

        val focusEntity =
            focus
                .copy(
                    id =
                        persistedFocusId,
                    focusKey =
                        normalizedFocusKey
                )
                .toEntity(
                    createdAt =
                        existingEntity?.createdAt
                            ?: now,
                    updatedAt =
                        now
                )

        val evidenceEntity =
            AdaptiveLearningEvidenceEntity(
                id =
                    UUID.randomUUID().toString(),
                focusId =
                    persistedFocusId,
                messageId =
                    normalizedMessageId,
                conversationId =
                    conversationId.sanitizedNullableValue(),
                result =
                    result.name,
                confidence =
                    confidence.coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            1f
                    ),
                reason =
                    reason.trim(),
                learnerText =
                    learnerText.trim(),
                correctedText =
                    correctedText.sanitizedNullableValue(),
                levelAppropriateText =
                    levelAppropriateText
                        .sanitizedNullableValue(),
                observedAt =
                    observedAt.coerceAtLeast(
                        minimumValue =
                            0L
                    ),
                createdAt =
                    now
            )

        adaptiveLearningDao.saveFocusWithEvidence(
            focus =
                focusEntity,
            evidence =
                evidenceEntity
        )
    }

    override suspend fun clearAll() {
        adaptiveLearningDao
            .clearAdaptiveLearningData()
    }

    /*
     * Converts the domain focus into its persistent Room
     * representation.
     */
    private fun AdaptiveLearningFocus.toEntity(
        createdAt: Long,
        updatedAt: Long
    ): AdaptiveLearningFocusEntity {
        return AdaptiveLearningFocusEntity(
            id =
                id,
            focusKey =
                focusKey.normalizeFocusKey(),
            type =
                type.name,
            title =
                title.trim(),
            description =
                description.trim(),
            targetCefrLevel =
                targetCefrLevel.name,
            communicationSkill =
                communicationSkill?.name,
            masteryState =
                masteryState.name,
            evidenceCount =
                safeEvidenceCount,
            struggleCount =
                safeStruggleCount,
            successCount =
                safeSuccessCount,
            consecutiveSuccessCount =
                safeConsecutiveSuccessCount,
            confidence =
                safeConfidence,
            masteryScore =
                safeMasteryScore,
            reinforcementInstruction =
                reinforcementInstruction.trim(),
            learnerGuidance =
                learnerGuidance.trim(),
            firstObservedAt =
                firstObservedAt.coerceAtLeast(
                    minimumValue =
                        0L
                ),
            lastObservedAt =
                lastObservedAt.coerceAtLeast(
                    minimumValue =
                        0L
                ),
            lastPracticedAt =
                lastPracticedAt
                    ?.coerceAtLeast(
                        minimumValue =
                            0L
                    ),
            masteredAt =
                masteredAt
                    ?.coerceAtLeast(
                        minimumValue =
                            0L
                    ),
            createdAt =
                createdAt.coerceAtLeast(
                    minimumValue =
                        0L
                ),
            updatedAt =
                updatedAt.coerceAtLeast(
                    minimumValue =
                        0L
                )
        )
    }

    /*
     * Converts a stored Room entity back into its domain
     * representation.
     *
     * Safe enum parsing protects the application if an
     * unsupported stored enum name is encountered.
     */
    private fun AdaptiveLearningFocusEntity.toDomain():
            AdaptiveLearningFocus {
        return AdaptiveLearningFocus(
            id =
                id,
            focusKey =
                focusKey,
            type =
                enumValueOrDefault(
                    storedValue =
                        type,
                    defaultValue =
                        LearningFocusType.GRAMMAR
                ),
            title =
                title,
            description =
                description,
            targetCefrLevel =
                enumValueOrDefault(
                    storedValue =
                        targetCefrLevel,
                    defaultValue =
                        CefrLevel.A1
                ),
            communicationSkill =
                communicationSkill
                    ?.let { storedValue ->
                        enumValueOrNull<
                                CommunicationSkill
                                >(
                            storedValue =
                                storedValue
                        )
                    },
            masteryState =
                enumValueOrDefault(
                    storedValue =
                        masteryState,
                    defaultValue =
                        MasteryState.OBSERVING
                ),
            evidenceCount =
                evidenceCount.coerceAtLeast(
                    minimumValue =
                        0
                ),
            struggleCount =
                struggleCount.coerceAtLeast(
                    minimumValue =
                        0
                ),
            successCount =
                successCount.coerceAtLeast(
                    minimumValue =
                        0
                ),
            consecutiveSuccessCount =
                consecutiveSuccessCount.coerceAtLeast(
                    minimumValue =
                        0
                ),
            confidence =
                confidence.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                ),
            masteryScore =
                masteryScore.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                ),
            reinforcementInstruction =
                reinforcementInstruction,
            learnerGuidance =
                learnerGuidance,
            firstObservedAt =
                firstObservedAt.coerceAtLeast(
                    minimumValue =
                        0L
                ),
            lastObservedAt =
                lastObservedAt.coerceAtLeast(
                    minimumValue =
                        0L
                ),
            lastPracticedAt =
                lastPracticedAt,
            masteredAt =
                masteredAt
        )
    }

    /*
     * Produces stable keys such as:
     *
     * grammar:dative_articles
     * vocabulary:bestellen
     * communication:giving_reasons
     */
    private fun String.normalizeFocusKey():
            String {
        return trim()
            .lowercase()
            .replace(
                FOCUS_KEY_WHITESPACE_REGEX,
                "_"
            )
            .replace(
                FOCUS_KEY_INVALID_CHARACTER_REGEX,
                ""
            )
    }

    private fun String?.sanitizedNullableValue():
            String? {
        return this
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
    }

    private inline fun <reified T : Enum<T>>
            enumValueOrNull(
        storedValue: String
    ): T? {
        val normalizedStoredValue =
            storedValue
                .trim()
                .uppercase()

        return enumValues<T>()
            .firstOrNull { enumValue ->
                enumValue.name ==
                        normalizedStoredValue
            }
    }

    private inline fun <reified T : Enum<T>>
            enumValueOrDefault(
        storedValue: String,
        defaultValue: T
    ): T {
        return enumValueOrNull<T>(
            storedValue =
                storedValue
        ) ?: defaultValue
    }

    private companion object {
        val FOCUS_KEY_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val FOCUS_KEY_INVALID_CHARACTER_REGEX =
            Regex(
                "[^a-z0-9:_-]"
            )
    }
}
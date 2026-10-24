package com.julesmeister.deutschcraft.core.cefr.engine

import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentSource
import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.CefrSkill
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionDecision
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionState
import com.julesmeister.deutschcraft.core.cefr.model.SkillScore
import com.julesmeister.deutschcraft.core.cefr.rules.CefrProgressionPolicy
import com.julesmeister.deutschcraft.core.cefr.rules.CefrRuleProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class DefaultCefrAssessmentEngine @Inject constructor(
    private val ruleProvider: CefrRuleProvider,
    private val progressionPolicy: CefrProgressionPolicy
) : CefrAssessmentEngine {

    override fun assess(
        currentProfile: CefrProfile,
        newEvidence: AssessmentEvidence,
        recentEvidence: List<AssessmentEvidence>,
        activeDayCount: Int
    ): CefrAssessmentResult {
        require(
            newEvidence.assessedLevel ==
                    currentProfile.currentLevel
        ) {
            "Evidence level ${newEvidence.assessedLevel} " +
                    "does not match current profile level " +
                    "${currentProfile.currentLevel}"
        }

        validateEvidence(newEvidence)

        val evidenceWeight =
            getEvidenceWeight(newEvidence.source)

        val observedScores =
            createObservedSkillScores(newEvidence)

        val updatedScores =
            updateSkillScores(
                currentScores =
                    currentProfile.skillScores,
                observedScores = observedScores,
                evidenceWeight = evidenceWeight,
                source = newEvidence.source
            )

        val overallScore =
            calculateOverallScore(updatedScores)

        val updatedAssessedMessageCount =
            currentProfile.assessedMessageCount + 1

        val evidenceWindow = (
                recentEvidence + newEvidence
                )
            .distinctBy { evidence ->
                evidence.id
            }
            .sortedByDescending { evidence ->
                evidence.analyzedAt
            }
            .take(
                AssessmentConstants
                    .MAXIMUM_RECENT_EVIDENCE
            )

        val criticalErrorRate =
            calculateCriticalErrorRate(
                evidence = evidenceWindow
            )

        val requirements = ruleProvider
            .getDefinition(currentProfile.currentLevel)
            .progressionRequirements

        val qualifiesForProgression =
            qualifiesForProgressionAssessment(
                updatedScores = updatedScores,
                overallScore = overallScore,
                criticalErrorRate =
                    criticalErrorRate,
                minimumOverallScore =
                    requirements.minimumOverallScore,
                minimumSkillScore =
                    requirements
                        .minimumRequiredSkillScore,
                maximumCriticalErrorRate =
                    requirements
                        .maximumRecentCriticalErrorRate
            )

        val consecutiveQualifiedAssessments =
            if (qualifiesForProgression) {
                currentProfile
                    .consecutiveQualifiedAssessments + 1
            } else {
                0
            }

        val readinessPercentage =
            calculateReadinessPercentage(
                overallScore = overallScore,
                skillScores = updatedScores,
                assessedMessageCount =
                    updatedAssessedMessageCount,
                activeDayCount = activeDayCount,
                criticalErrorRate =
                    criticalErrorRate,
                currentProfile = currentProfile
            )

        val provisionalProfile =
            currentProfile.copy(
                skillScores = updatedScores,
                overallScore = overallScore,
                readinessPercentage =
                    readinessPercentage,
                assessedMessageCount =
                    updatedAssessedMessageCount,
                activeDayCount = activeDayCount,
                recentCriticalErrorRate =
                    criticalErrorRate,
                consecutiveQualifiedAssessments =
                    consecutiveQualifiedAssessments,
                progressionState =
                    ProgressionState.BUILDING_EVIDENCE,
                suggestedNextLevel = null,
                lastAssessedAt =
                    newEvidence.analyzedAt
            )

        val decision =
            progressionPolicy.evaluate(
                provisionalProfile
            )

        val finalProfile =
            applyProgressionDecision(
                profile = provisionalProfile,
                decision = decision
            )

        return CefrAssessmentResult(
            updatedProfile = finalProfile,
            updatedSkillScores =
                updatedScores.values.toList(),
            progressionDecision = decision
        )
    }

    private fun validateEvidence(
        evidence: AssessmentEvidence
    ) {
        require(evidence.grammarScore in 0..100)
        require(evidence.vocabularyScore in 0..100)
        require(evidence.coherenceScore in 0..100)
        require(evidence.accuracyScore in 0..100)
        require(evidence.rangeScore in 0..100)
    }

    private fun createObservedSkillScores(
        evidence: AssessmentEvidence
    ): Map<CefrSkill, Int> {
        val writtenInteractionScore =
            weightedAverage(
                listOf(
                    evidence.grammarScore to 0.20,
                    evidence.vocabularyScore to 0.20,
                    evidence.coherenceScore to 0.25,
                    evidence.accuracyScore to 0.20,
                    evidence.rangeScore to 0.15
                )
            )

        val fluencyScore =
            weightedAverage(
                listOf(
                    evidence.coherenceScore to 0.45,
                    evidence.rangeScore to 0.30,
                    evidence.accuracyScore to 0.25
                )
            )

        return mapOf(
            CefrSkill.GRAMMAR to
                    evidence.grammarScore,

            CefrSkill.VOCABULARY to
                    evidence.vocabularyScore,

            CefrSkill.COHERENCE to
                    evidence.coherenceScore,

            CefrSkill.ACCURACY to
                    evidence.accuracyScore,

            CefrSkill.RANGE to
                    evidence.rangeScore,

            CefrSkill.WRITTEN_INTERACTION to
                    writtenInteractionScore,

            CefrSkill.FLUENCY to
                    fluencyScore
        )
    }

    private fun updateSkillScores(
        currentScores: Map<CefrSkill, SkillScore>,
        observedScores: Map<CefrSkill, Int>,
        evidenceWeight: Double,
        source: AssessmentSource
    ): Map<CefrSkill, SkillScore> {
        val updated = currentScores.toMutableMap()

        observedScores.forEach {
                (skill, observedScore) ->

            val current = currentScores[skill]
                ?: SkillScore(
                    skill = skill,
                    score =
                        AssessmentConstants
                            .DEFAULT_INITIAL_SCORE,
                    evidenceCount = 0,
                    confidence =
                        AssessmentConstants
                            .DEFAULT_INITIAL_CONFIDENCE
                )

            val updatedEvidenceCount =
                current.evidenceCount + 1

            val updatedScore =
                calculateRollingScore(
                    previousScore = current.score,
                    previousEvidenceCount =
                        current.evidenceCount,
                    observedScore = observedScore,
                    evidenceWeight = evidenceWeight
                )

            updated[skill] = SkillScore(
                skill = skill,
                score = updatedScore,
                evidenceCount =
                    updatedEvidenceCount,
                confidence = calculateConfidence(
                    evidenceCount =
                        updatedEvidenceCount,
                    sourceReliability =
                        getSourceReliability(source)
                )
            )
        }

        /*
         * COMPREHENSION is intentionally not fabricated.
         *
         * A learner's outgoing text alone is not enough
         * to reliably assess comprehension. The existing
         * score is preserved until DeutschCraft gathers
         * suitable evidence through reply relevance,
         * comprehension challenges, or voice features.
         */
        return updated.toMap()
    }

    private fun calculateRollingScore(
        previousScore: Int,
        previousEvidenceCount: Int,
        observedScore: Int,
        evidenceWeight: Double
    ): Int {
        if (previousEvidenceCount == 0) {
            return observedScore.clampScore()
        }

        val previousWeight =
            min(
                previousEvidenceCount.toDouble(),
                40.0
            )

        val weightedPrevious =
            previousScore * previousWeight

        val weightedObservation =
            observedScore * evidenceWeight

        return (
                (
                        weightedPrevious +
                                weightedObservation
                        ) /
                        (
                                previousWeight +
                                        evidenceWeight
                                )
                )
            .roundToInt()
            .clampScore()
    }

    private fun calculateOverallScore(
        skillScores: Map<CefrSkill, SkillScore>
    ): Int {
        val values = buildList {
            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.GRAMMAR,
                weight = AssessmentConstants
                    .GRAMMAR_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.VOCABULARY,
                weight = AssessmentConstants
                    .VOCABULARY_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill =
                    CefrSkill.WRITTEN_INTERACTION,
                weight = AssessmentConstants
                    .WRITTEN_INTERACTION_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.FLUENCY,
                weight = AssessmentConstants
                    .FLUENCY_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.COHERENCE,
                weight = AssessmentConstants
                    .COHERENCE_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.ACCURACY,
                weight = AssessmentConstants
                    .ACCURACY_OVERALL_WEIGHT
            )

            addScoreIfAvailable(
                values = this,
                skillScores = skillScores,
                skill = CefrSkill.RANGE,
                weight = AssessmentConstants
                    .RANGE_OVERALL_WEIGHT
            )
        }

        return weightedAverage(values)
    }

    private fun addScoreIfAvailable(
        values: MutableList<Pair<Int, Double>>,
        skillScores: Map<CefrSkill, SkillScore>,
        skill: CefrSkill,
        weight: Double
    ) {
        val score = skillScores[skill]
            ?: return

        /*
         * Confidence affects how much a skill contributes.
         * Very new scores receive less influence.
         */
        val confidenceAdjustedWeight =
            weight * score.confidence.coerceAtLeast(0.10)

        values += score.score to
                confidenceAdjustedWeight
    }

    private fun calculateCriticalErrorRate(
        evidence: List<AssessmentEvidence>
    ): Double {
        if (evidence.isEmpty()) {
            return 0.0
        }

        val totalMistakes = evidence.sumOf {
                assessment ->
            assessment.mistakes.size
        }

        if (totalMistakes == 0) {
            return 0.0
        }

        val criticalMistakes = evidence.sumOf {
                assessment ->
            assessment.mistakes.count { mistake ->
                mistake.severity ==
                        MistakeSeverity.CRITICAL
            }
        }

        return (
                criticalMistakes.toDouble() /
                        totalMistakes.toDouble()
                )
            .coerceIn(0.0, 1.0)
    }

    private fun qualifiesForProgressionAssessment(
        updatedScores: Map<CefrSkill, SkillScore>,
        overallScore: Int,
        criticalErrorRate: Double,
        minimumOverallScore: Int,
        minimumSkillScore: Int,
        maximumCriticalErrorRate: Double
    ): Boolean {
        val assessedScores =
            updatedScores.values.filter { score ->
                score.evidenceCount > 0
            }

        if (assessedScores.isEmpty()) {
            return false
        }

        val skillsAreReady =
            assessedScores.all { skillScore ->
                skillScore.score >= minimumSkillScore &&
                        skillScore.confidence >=
                        AssessmentConstants
                            .MINIMUM_PROGRESSION_CONFIDENCE
            }

        return overallScore >= minimumOverallScore &&
                skillsAreReady &&
                criticalErrorRate <=
                maximumCriticalErrorRate
    }

    private fun calculateReadinessPercentage(
        overallScore: Int,
        skillScores: Map<CefrSkill, SkillScore>,
        assessedMessageCount: Int,
        activeDayCount: Int,
        criticalErrorRate: Double,
        currentProfile: CefrProfile
    ): Int {
        if (currentProfile.currentLevel.next() == null) {
            return 100
        }

        val requirements = ruleProvider
            .getDefinition(currentProfile.currentLevel)
            .progressionRequirements

        val overallProgress =
            ratio(
                current = overallScore.toDouble(),
                required = requirements
                    .minimumOverallScore
                    .toDouble()
            )

        val requiredSkillScores =
            skillScores.values
                .filter { score ->
                    score.skill !=
                            CefrSkill.COMPREHENSION &&
                            score.evidenceCount > 0
                }

        val skillProgress =
            if (requiredSkillScores.isEmpty()) {
                0.0
            } else {
                requiredSkillScores
                    .map { skillScore ->
                        val scoreRatio = ratio(
                            current =
                                skillScore.score.toDouble(),
                            required =
                                requirements
                                    .minimumRequiredSkillScore
                                    .toDouble()
                        )

                        val confidenceRatio = ratio(
                            current =
                                skillScore.confidence,
                            required =
                                AssessmentConstants
                                    .MINIMUM_PROGRESSION_CONFIDENCE
                        )

                        min(scoreRatio, confidenceRatio)
                    }
                    .average()
            }

        val messageProgress =
            ratio(
                current =
                    assessedMessageCount.toDouble(),
                required =
                    requirements
                        .minimumAssessedMessages
                        .toDouble()
            )

        val activeDayProgress =
            ratio(
                current = activeDayCount.toDouble(),
                required =
                    requirements
                        .minimumActiveDays
                        .toDouble()
            )

        val errorProgress =
            calculateErrorReadiness(
                currentErrorRate =
                    criticalErrorRate,
                maximumAllowedErrorRate =
                    requirements
                        .maximumRecentCriticalErrorRate
            )

        val readiness = (
                overallProgress *
                        AssessmentConstants
                            .SCORE_READINESS_WEIGHT +
                        skillProgress *
                        AssessmentConstants
                            .SKILL_READINESS_WEIGHT +
                        messageProgress *
                        AssessmentConstants
                            .MESSAGE_READINESS_WEIGHT +
                        activeDayProgress *
                        AssessmentConstants
                            .ACTIVE_DAY_READINESS_WEIGHT +
                        errorProgress *
                        AssessmentConstants
                            .ERROR_READINESS_WEIGHT
                )

        return (readiness * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    private fun calculateErrorReadiness(
        currentErrorRate: Double,
        maximumAllowedErrorRate: Double
    ): Double {
        if (currentErrorRate <=
            maximumAllowedErrorRate
        ) {
            return 1.0
        }

        if (currentErrorRate >= 1.0) {
            return 0.0
        }

        return (
                1.0 - (
                        currentErrorRate -
                                maximumAllowedErrorRate
                        ) /
                        (
                                1.0 -
                                        maximumAllowedErrorRate
                                )
                )
            .coerceIn(0.0, 1.0)
    }

    private fun ratio(
        current: Double,
        required: Double
    ): Double {
        if (required <= 0.0) {
            return 1.0
        }

        return (current / required)
            .coerceIn(0.0, 1.0)
    }

    private fun applyProgressionDecision(
        profile: CefrProfile,
        decision: ProgressionDecision
    ): CefrProfile {
        return when (decision) {
            is ProgressionDecision.Eligible -> {
                profile.copy(
                    progressionState =
                        ProgressionState.READY_FOR_REVIEW,
                    suggestedNextLevel =
                        decision.proposedLevel
                )
            }

            ProgressionDecision.MaximumLevelReached -> {
                profile.copy(
                    readinessPercentage = 100,
                    progressionState =
                        ProgressionState.MAXIMUM_LEVEL,
                    suggestedNextLevel = null
                )
            }

            is ProgressionDecision.NotEnoughEvidence -> {
                profile.copy(
                    progressionState =
                        ProgressionState.BUILDING_EVIDENCE,
                    suggestedNextLevel = null
                )
            }

            is ProgressionDecision
            .SkillsNeedImprovement -> {
                profile.copy(
                    progressionState =
                        ProgressionState.NOT_READY,
                    suggestedNextLevel = null
                )
            }

            is ProgressionDecision
            .MoreConsistencyRequired -> {
                profile.copy(
                    progressionState =
                        ProgressionState.BUILDING_EVIDENCE,
                    suggestedNextLevel = null
                )
            }
        }
    }

    private fun getEvidenceWeight(
        source: AssessmentSource
    ): Double {
        return when (source) {
            AssessmentSource.LOCAL_RULE_ENGINE ->
                AssessmentConstants
                    .LOCAL_EVIDENCE_WEIGHT

            AssessmentSource.REMOTE_AI ->
                AssessmentConstants
                    .REMOTE_AI_EVIDENCE_WEIGHT

            AssessmentSource.HYBRID ->
                AssessmentConstants
                    .HYBRID_EVIDENCE_WEIGHT
        }
    }

    private fun getSourceReliability(
        source: AssessmentSource
    ): Double {
        return when (source) {
            AssessmentSource.LOCAL_RULE_ENGINE ->
                0.70

            AssessmentSource.REMOTE_AI ->
                0.90

            AssessmentSource.HYBRID ->
                1.00
        }
    }
}
package com.julesmeister.deutschcraft.core.cefr.rules

import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionDecision
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultCefrProgressionPolicy @Inject constructor(
    private val ruleProvider: CefrRuleProvider
) : CefrProgressionPolicy {


    override fun evaluate(
        profile: CefrProfile
    ): ProgressionDecision {
        val nextLevel = profile.currentLevel.next()
            ?: return ProgressionDecision
                .MaximumLevelReached

        val requirements = ruleProvider
            .getDefinition(profile.currentLevel)
            .progressionRequirements

        if (
            profile.assessedMessageCount <
            requirements.minimumAssessedMessages
        ) {
            return ProgressionDecision.NotEnoughEvidence(
                currentMessages =
                    profile.assessedMessageCount,
                requiredMessages =
                    requirements.minimumAssessedMessages
            )
        }

        if (
            profile.activeDayCount <
            requirements.minimumActiveDays
        ) {
            return ProgressionDecision.NotEnoughEvidence(
                currentMessages =
                    profile.assessedMessageCount,
                requiredMessages =
                    requirements.minimumAssessedMessages
            )
        }

        val weakSkills = profile.skillScores.values
            .filter { skillScore ->
                skillScore.score <
                        requirements.minimumRequiredSkillScore ||
                        skillScore.confidence < 0.65
            }
            .map { skillScore ->
                skillScore.skill
            }

        if (weakSkills.isNotEmpty()) {
            return ProgressionDecision
                .SkillsNeedImprovement(weakSkills)
        }

        if (
            profile.overallScore <
            requirements.minimumOverallScore
        ) {
            return ProgressionDecision
                .SkillsNeedImprovement(
                    weakSkills =
                        profile.skillScores.keys.toList()
                )
        }

        if (
            profile.recentCriticalErrorRate >
            requirements.maximumRecentCriticalErrorRate
        ) {
            return ProgressionDecision
                .MoreConsistencyRequired(
                    currentQualifiedAssessments =
                        profile.consecutiveQualifiedAssessments,
                    requiredQualifiedAssessments =
                        requirements
                            .requiredConsecutiveQualifiedAssessments
                )
        }

        if (
            profile.consecutiveQualifiedAssessments <
            requirements
                .requiredConsecutiveQualifiedAssessments
        ) {
            return ProgressionDecision
                .MoreConsistencyRequired(
                    currentQualifiedAssessments =
                        profile.consecutiveQualifiedAssessments,
                    requiredQualifiedAssessments =
                        requirements
                            .requiredConsecutiveQualifiedAssessments
                )
        }

        return ProgressionDecision.Eligible(
            currentLevel = profile.currentLevel,
            proposedLevel = nextLevel
        )
    }
}
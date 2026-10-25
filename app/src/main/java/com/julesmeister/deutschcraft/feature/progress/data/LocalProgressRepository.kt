package com.julesmeister.deutschcraft.feature.progress.data

import com.julesmeister.deutschcraft.core.cefr.model.CefrSkill
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.relation.LearnerProfileWithSkills
import com.julesmeister.deutschcraft.feature.assessment.data.local.AssessmentLocalDataSource
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalProgressRepository @Inject constructor(
    private val learnerProfileDao: LearnerProfileDao,
    private val assessmentLocalDataSource:
    AssessmentLocalDataSource
) : ProgressRepository {

    override fun observeProgress():
            Flow<ProgressSummary?> {
        return learnerProfileDao
            .observeProfileWithSkills(
                DefaultLearnerProfile.LOCAL_PROFILE_ID
            )
            .map { relation ->
                relation?.let(
                    ::mapToProgressSummary
                )
            }
    }

    override suspend fun getProgress():
            ProgressSummary? {
        return learnerProfileDao
            .getProfileWithSkills(
                DefaultLearnerProfile.LOCAL_PROFILE_ID
            )
            ?.let(
                ::mapToProgressSummary
            )
    }

    override suspend fun ensureProfileExists() {
        assessmentLocalDataSource
            .createDefaultProfileIfMissing()
    }

    private fun mapToProgressSummary(
        relation: LearnerProfileWithSkills
    ): ProgressSummary {
        val profile = relation.profile

        val skills = relation.skillScores
            .associateBy { skillScore ->
                skillScore.skill
            }

        return ProgressSummary(
            currentLevel =
                profile.currentLevel,

            readinessPercentage =
                profile.readinessPercentage,

            grammarScore =
                skills[CefrSkill.GRAMMAR]
                    ?.score
                    ?: DEFAULT_SCORE,

            vocabularyScore =
                skills[CefrSkill.VOCABULARY]
                    ?.score
                    ?: DEFAULT_SCORE,

            coherenceScore =
                skills[CefrSkill.COHERENCE]
                    ?.score
                    ?: DEFAULT_SCORE,

            accuracyScore =
                skills[CefrSkill.ACCURACY]
                    ?.score
                    ?: DEFAULT_SCORE,

            rangeScore =
                skills[CefrSkill.RANGE]
                    ?.score
                    ?: DEFAULT_SCORE,

            overallScore =
                profile.overallScore,

            assessedMessageCount =
                profile.assessedMessageCount,

            activeDayCount =
                profile.activeDayCount
        )
    }

    private companion object {
        const val DEFAULT_SCORE = 0
    }
}
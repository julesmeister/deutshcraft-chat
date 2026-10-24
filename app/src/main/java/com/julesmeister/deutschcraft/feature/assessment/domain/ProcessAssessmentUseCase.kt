package com.julesmeister.deutschcraft.feature.assessment.domain

import com.julesmeister.deutschcraft.core.cefr.engine.AssessmentConstants
import com.julesmeister.deutschcraft.core.cefr.engine.CefrAssessmentEngine
import com.julesmeister.deutschcraft.core.cefr.engine.CefrAssessmentResult
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.core.cefr.rules.CefrRuleVersion
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.mapper.mergeIntoEntity
import com.julesmeister.deutschcraft.core.database.mapper.toDomain
import com.julesmeister.deutschcraft.core.database.mapper.toEntity
import com.julesmeister.deutschcraft.feature.assessment.data.local.AssessmentLocalDataSource
import javax.inject.Inject

class ProcessAssessmentUseCase @Inject constructor(
    private val assessmentEngine:
    CefrAssessmentEngine,
    private val learnerProfileDao:
    LearnerProfileDao,
    private val assessmentDao:
    AssessmentDao,
    private val localDataSource:
    AssessmentLocalDataSource
) {

    suspend operator fun invoke(
        evidence: AssessmentEvidence,
        activeDayCount: Int
    ): CefrAssessmentResult {
        localDataSource.createDefaultProfileIfMissing()

        val profileId =
            DefaultLearnerProfile.LOCAL_PROFILE_ID

        val existingProfileEntity =
            requireNotNull(
                learnerProfileDao.getProfile(
                    profileId
                )
            ) {
                "Learner profile was not created."
            }

        val currentProfile =
            requireNotNull(
                learnerProfileDao
                    .getProfileWithSkills(profileId)
                    ?.toDomain()
            ) {
                "Learner profile and skill scores " +
                        "could not be loaded."
            }

        val recentEvidence =
            assessmentDao
                .getRecentEvidenceWithDetails(
                    profileId = profileId,
                    limit = 30
                )
                .map { relation ->
                    relation.toDomain()
                }

        val result = assessmentEngine.assess(
            currentProfile = currentProfile,
            newEvidence = evidence,
            recentEvidence = recentEvidence,
            activeDayCount = activeDayCount
        )

        val now = System.currentTimeMillis()

        val updatedProfileEntity =
            result.updatedProfile.mergeIntoEntity(
                existingEntity =
                    existingProfileEntity,
                updatedAt = now,
                ruleVersion =
                    CefrRuleVersion.CURRENT
            )

        val skillScoreEntities =
            result.updatedSkillScores.map {
                    skillScore ->
                skillScore.toEntity(
                    profileId = profileId,
                    updatedAt = now
                )
            }

        localDataSource.saveAssessmentAndProfile(
            evidence = evidence,
            profile = updatedProfileEntity,
            skillScores = skillScoreEntities
        )

        return result
    }
}
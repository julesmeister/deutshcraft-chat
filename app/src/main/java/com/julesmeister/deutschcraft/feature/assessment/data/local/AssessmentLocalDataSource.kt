package com.julesmeister.deutschcraft.feature.assessment.data.local

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.CefrSkill
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity
import com.julesmeister.deutschcraft.core.database.mapper.toDomain
import com.julesmeister.deutschcraft.core.database.mapper.toEntity
import com.julesmeister.deutschcraft.core.database.mapper.toGrammarFeatureEntities
import com.julesmeister.deutschcraft.core.database.mapper.toMistakeEntities
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssessmentLocalDataSource @Inject constructor(
    private val database: DeutschCraftDatabase,
    private val learnerProfileDao: LearnerProfileDao,
    private val assessmentDao: AssessmentDao
) {

    fun observeProfile(): Flow<CefrProfile?> {
        return learnerProfileDao
            .observeProfileWithSkills(
                DefaultLearnerProfile.LOCAL_PROFILE_ID
            )
            .map { relation ->
                relation?.toDomain()
            }
    }

    suspend fun getProfile(): CefrProfile? {
        return learnerProfileDao
            .getProfileWithSkills(
                DefaultLearnerProfile.LOCAL_PROFILE_ID
            )
            ?.toDomain()
    }

    suspend fun createDefaultProfileIfMissing() {
        val profileId =
            DefaultLearnerProfile.LOCAL_PROFILE_ID

        val existingProfile =
            learnerProfileDao.getProfile(profileId)

        if (existingProfile != null) {
            return
        }

        val now = System.currentTimeMillis()

        learnerProfileDao.replaceProfileWithSkills(
            profile =
                DefaultLearnerProfile.createEntity(
                    currentTime = now
                ),
            skillScores =
                createInitialSkillScores(
                    profileId = profileId,
                    currentTime = now
                )
        )
    }
    suspend fun saveAssessmentAndProfile(
        evidence: AssessmentEvidence,
        profile: LearnerProfileEntity,
        skillScores: List<SkillScoreEntity>
    ) {
        database.withTransaction {
            assessmentDao.saveAssessment(
                evidence = evidence.toEntity(
                    profileId = profile.id
                ),
                grammarFeatures =
                    evidence
                        .toGrammarFeatureEntities(),
                mistakes =
                    evidence
                        .toMistakeEntities()
            )

            learnerProfileDao
                .replaceProfileWithSkills(
                    profile = profile,
                    skillScores = skillScores
                )
        }
    }

    fun createInitialSkillScores(
        profileId: String,
        currentTime: Long
    ): List<SkillScoreEntity> {
        return CefrSkill.entries.map { skill ->
            SkillScoreEntity(
                profileId = profileId,
                skill = skill,
                score = 0,
                evidenceCount = 0,
                confidence = 0.0,
                updatedAt = currentTime
            )
        }
    }

}
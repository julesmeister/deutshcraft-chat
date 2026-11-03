// app/src/main/java/com/julesmeister/deutschcraft/feature/assessment/domain/GetCurrentLearnerCefrLevelUseCase.kt

package com.julesmeister.deutschcraft.feature.assessment.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GetCurrentLearnerCefrLevelUseCase
@Inject constructor(
    private val learnerProfileDao:
    LearnerProfileDao
) {

    fun observe(): Flow<CefrLevel> =
        learnerProfileDao.observeProfile(DefaultLearnerProfile.LOCAL_PROFILE_ID)
            .map { it?.currentLevel ?: DEFAULT_CEFR_LEVEL }
            .distinctUntilChanged()

    suspend operator fun invoke():
            CefrLevel {

        return learnerProfileDao
            .getProfile(
                profileId =
                    DefaultLearnerProfile.LOCAL_PROFILE_ID
            )
            ?.currentLevel
            ?: DEFAULT_CEFR_LEVEL
    }

    private companion object {

        val DEFAULT_CEFR_LEVEL =
            CefrLevel.A1
    }
}
package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.mission.domain.catalog.DefaultMissionCatalog
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SeedMissionCatalogUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository,
    private val defaultMissionCatalog:
    DefaultMissionCatalog
) {

    private val seedMutex =
        Mutex()

    private var hasSeededDuringCurrentProcess =
        false

    suspend operator fun invoke() {
        if (hasSeededDuringCurrentProcess) {
            return
        }

        seedMutex.withLock {
            if (hasSeededDuringCurrentProcess) {
                return@withLock
            }

            seedMissingMissions()

            hasSeededDuringCurrentProcess =
                true
        }
    }

    private suspend fun seedMissingMissions() {
        defaultMissionCatalog
            .getAll()
            .forEach { missionTemplate ->
                seedMissionIfMissing(
                    missionTemplateId =
                        missionTemplate.id,
                    saveMission = {
                        missionRepository
                            .saveMissionTemplate(
                                template =
                                    missionTemplate
                            )
                    }
                )
            }
    }

    private suspend fun seedMissionIfMissing(
        missionTemplateId: String,
        saveMission: suspend () -> Unit
    ) {
        try {
            val existingMission =
                missionRepository
                    .getMissionTemplate(
                        missionTemplateId =
                            missionTemplateId
                    )

            if (existingMission != null) {
                return
            }

            saveMission()

            Log.d(
                SEEDER_TAG,
                "Seeded mission template: " +
                        missionTemplateId
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                SEEDER_TAG,
                "Could not seed mission template: " +
                        missionTemplateId,
                throwable
            )

            throw throwable
        }
    }

    private companion object {
        const val SEEDER_TAG =
            "MissionCatalogSeeder"
    }
}
// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/usecase/GetCapabilityMasteriesUseCase.kt

package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMastery
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryCalculator
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.MissionCapabilityEvidenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetCapabilityMasteriesUseCase
@Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository,

    private val missionCapabilityEvidenceRepository:
    MissionCapabilityEvidenceRepository,

    private val masteryCalculator:
    CapabilityMasteryCalculator
) {

    suspend operator fun invoke(
        capabilityIds:
        Collection<String>
    ): Map<String, CapabilityMastery> {

        val normalizedCapabilityIds =
            capabilityIds
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()

        if (normalizedCapabilityIds.isEmpty()) {
            return emptyMap()
        }

        val missionEvidenceByCapabilityId =
            missionCapabilityEvidenceRepository
                .getForCapabilities(
                    capabilityIds =
                        normalizedCapabilityIds
                )

        return buildMap {
            normalizedCapabilityIds.forEach { capabilityId ->
                val challenges =
                    besserChallengeRepository
                        .observeForCapability(
                            capabilityId =
                                capabilityId
                        )
                        .first()

                val mastery =
                    masteryCalculator.calculate(
                        capabilityId =
                            capabilityId,

                        challenges =
                            challenges,

                        missionEvidence =
                            missionEvidenceByCapabilityId[
                                capabilityId
                            ]
                                .orEmpty()
                    )

                put(
                    capabilityId,
                    mastery
                )
            }
        }
    }
}
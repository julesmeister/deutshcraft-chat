package com.julesmeister.deutschcraft.feature.mission.domain.catalog

import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMissionCatalog @Inject constructor() {

    fun getAll():
            List<MissionTemplate> {
        return listOf(
            RestaurantOrderingMission.create()
        )
    }
}
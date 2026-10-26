package com.julesmeister.deutschcraft.feature.mission.data.local

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun List<String>.toMissionObjectiveJson(
    json: Json
): String {
    val sanitizedValues =
        asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .toList()

    return json.encodeToString(
        value =
            sanitizedValues
    )
}

internal fun String.toMissionObjectiveStringList(
    json: Json
): List<String> {
    if (isBlank()) {
        return emptyList()
    }

    return try {
        json.decodeFromString<List<String>>(
            string =
                this
        )
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .toList()
    } catch (
        serializationException:
        SerializationException
    ) {
        emptyList()
    } catch (
        illegalArgumentException:
        IllegalArgumentException
    ) {
        emptyList()
    }
}
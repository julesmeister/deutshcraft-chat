package com.julesmeister.deutschcraft.core.cefr.model

enum class CefrLevel(
    val rank: Int,
    val displayName: String
) {
    A1(
        rank = 1,
        displayName = "A1 · Beginner"
    ),
    A2(
        rank = 2,
        displayName = "A2 · Elementary"
    ),
    B1(
        rank = 3,
        displayName = "B1 · Intermediate"
    ),
    B2(
        rank = 4,
        displayName = "B2 · Upper Intermediate"
    ),
    C1(
        rank = 5,
        displayName = "C1 · Advanced"
    ),
    C2(
        rank = 6,
        displayName = "C2 · Proficient"
    );

    fun next(): CefrLevel? {
        return entries.firstOrNull { level ->
            level.rank == rank + 1
        }
    }

    fun previous(): CefrLevel? {
        return entries.firstOrNull { level ->
            level.rank == rank - 1
        }
    }

    fun isAtLeast(other: CefrLevel): Boolean {
        return rank >= other.rank
    }
}
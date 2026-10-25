package com.julesmeister.deutschcraft.feature.archivedthreads.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

data class ArchivedThread(
    val id: String,
    val title: String,
    val previewText: String?,
    val cefrLevel: CefrLevel,
    val messageCount: Int,
    val updatedAt: Long
)
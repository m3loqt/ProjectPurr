package com.projectpurr.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rest_sessions")
data class RestSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationMillis: Long,
    val companionId: String,
    val companionName: String,
    val usedSilentMode: Boolean,
    val usedChestMode: Boolean,
    val timerOptionMinutes: Int?,
    val completedNaturally: Boolean,
)

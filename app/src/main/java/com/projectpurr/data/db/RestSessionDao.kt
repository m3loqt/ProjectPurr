package com.projectpurr.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RestSessionEntity)

    @Query("SELECT * FROM rest_sessions ORDER BY startedAtMillis DESC LIMIT 50")
    fun recentSessions(): Flow<List<RestSessionEntity>>

    @Query("DELETE FROM rest_sessions")
    suspend fun deleteAll()
}

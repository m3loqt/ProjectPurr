package com.projectpurr.data

import android.content.Context
import com.projectpurr.data.db.ProjectPurrDatabase
import com.projectpurr.data.db.RestSessionEntity
import kotlinx.coroutines.flow.Flow

class RestHistoryRepository(context: Context) {
    private val dao = ProjectPurrDatabase.getInstance(context).restSessionDao()

    val recentSessions: Flow<List<RestSessionEntity>> = dao.recentSessions()

    suspend fun save(session: RestSessionEntity) = dao.insert(session)

    suspend fun deleteAll() = dao.deleteAll()
}

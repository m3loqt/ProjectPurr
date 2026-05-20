package com.projectpurr.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RestSessionEntity::class], version = 1, exportSchema = false)
abstract class ProjectPurrDatabase : RoomDatabase() {
    abstract fun restSessionDao(): RestSessionDao

    companion object {
        @Volatile private var INSTANCE: ProjectPurrDatabase? = null

        fun getInstance(context: Context): ProjectPurrDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ProjectPurrDatabase::class.java,
                    "purr_database",
                ).build().also { INSTANCE = it }
            }
    }
}

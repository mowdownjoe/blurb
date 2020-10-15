package com.mowdowndevelopments.blurb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.database.entities.Story
import timber.log.Timber

@Database(entities = [Feed::class, Story::class], version = 1, exportSchema = false)
abstract class BlurbDb : RoomDatabase() {
    abstract fun blurbDao(): BlurbDao

    companion object {
        private val LOCK = Any()
        private const val DB_NAME = "blurb_reader"
        private lateinit var instance: BlurbDb
        @JvmStatic
        fun getInstance(context: Context): BlurbDb {
            synchronized(LOCK) {
                if (!::instance.isInitialized) {
                    Timber.d("Creating new database instance…")
                    instance = Room.databaseBuilder(context.applicationContext,
                            BlurbDb::class.java, DB_NAME).build()
                }
            }
            return instance
        }
    }
}
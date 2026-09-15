package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.MediaItem

@Database(entities = [MediaItem::class], version = 1, exportSchema = false)
abstract class AbhtrixDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: AbhtrixDatabase? = null

        fun getDatabase(context: Context): AbhtrixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbhtrixDatabase::class.java,
                    "abhtrix_streaming.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

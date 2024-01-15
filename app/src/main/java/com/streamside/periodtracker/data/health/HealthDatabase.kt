package com.streamside.periodtracker.data.health

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.streamside.periodtracker.data.period.DataConverters

@Database(entities = [Health::class], version = 1, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            val temp = INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
package com.streamside.periodtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Period::class], version = 1, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class PeriodDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao

    companion object {
        @Volatile
        private var INSTANCE: PeriodDatabase? = null

        fun getDatabase(context: Context): PeriodDatabase {
            val temp = INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeriodDatabase::class.java,
                    "period_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
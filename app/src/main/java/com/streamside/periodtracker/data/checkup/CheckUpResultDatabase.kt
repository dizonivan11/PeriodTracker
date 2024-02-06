package com.streamside.periodtracker.data.checkup

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.streamside.periodtracker.data.period.DataConverters

@Database(entities = [CheckUpResult::class], version = 1, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class CheckUpResultDatabase : RoomDatabase() {
    abstract fun checkUpResultDao(): CheckUpResultDao

    companion object {
        @Volatile
        private var INSTANCE: CheckUpResultDatabase? = null

        fun getDatabase(context: Context): CheckUpResultDatabase {
            val temp = INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheckUpResultDatabase::class.java,
                    "check_up_result_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
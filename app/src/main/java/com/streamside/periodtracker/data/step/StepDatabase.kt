package com.streamside.periodtracker.data.step

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.streamside.periodtracker.data.period.DataConverters

@Database(entities = [Step::class], version = 1, exportSchema = false)
@TypeConverters(DataConverters::class)
abstract class StepDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile
        private var INSTANCE: StepDatabase? = null

        fun getDatabase(context: Context): StepDatabase {
            val temp = INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StepDatabase::class.java,
                    "step_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
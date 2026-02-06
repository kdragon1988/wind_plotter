package com.example.windplotter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Mission::class, Sample::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun sampleDao(): SampleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wind_plotter_db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE missions ADD COLUMN sessionCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE missions ADD COLUMN lastMeasuredAt INTEGER")
                db.execSQL("ALTER TABLE samples ADD COLUMN sessionIndex INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}

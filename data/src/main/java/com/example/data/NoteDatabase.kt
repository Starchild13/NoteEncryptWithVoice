package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database abstract class for the NoteEncrypt application.
 * Manages the SQLite database instance and serves as the main access point to persistent note data.
 */
@Database(entities = [Note::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    /**
     * Abstract getter providing access to the Data Access Object ([NoteDao]).
     */
    abstract fun noteDao(): NoteDao

    companion object {
        // Volatile annotation ensures thread-safe atomic writes to INSTANCE across threads
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        /**
         * Returns the thread-safe singleton instance of [NoteDatabase].
         * Uses double-checked locking to prevent multiple database instances from being created.
         *
         * @param context Application context used for database initialization.
         * @return The active [NoteDatabase] instance.
         */
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                // Re-creates database tables if migrations are missing upon version increment
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


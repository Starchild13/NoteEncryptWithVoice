package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) for executing database operations on the `notes` table in Room.
 * Defines suspending functions to ensure non-blocking asynchronous execution off the UI thread.
 */
@Dao
interface NoteDao {

    /**
     * Inserts a new note into the database. If a note with the same primary key already exists,
     * it will be replaced using the [OnConflictStrategy.REPLACE] strategy.
     *
     * @param note The [Note] entity instance to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    /**
     * Updates an existing note record in the database matching its primary key.
     *
     * @param note The [Note] entity containing updated title, encrypted payload, or sort order.
     */
    @Update
    suspend fun update(note: Note)

    /**
     * Deletes a specific note record from the database.
     *
     * @param note The [Note] entity to be removed.
     */
    @Delete
    suspend fun delete(note: Note)

    /**
     * Retrieves all notes stored in the database, ordered primarily by `sort_order` ascending,
     * and secondarily by `id` ascending.
     *
     * @return A list of all stored [Note] entities.
     */
    @Query("SELECT * FROM notes ORDER BY sort_order ASC, id ASC")
    suspend fun getAllNotes(): List<Note>

    /**
     * Retrieves a single note record matching the specified integer ID.
     *
     * @param id The unique integer ID of the note.
     * @return The matching [Note] entity if found, or null if no record exists.
     */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?
}


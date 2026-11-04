package com.example.data

import androidx.room.*

/**
 * Data Access Object (DAO) for accessing the notes table in the Room database.
 * Provides asynchronous suspending methods for database operations.
 */
@Dao
interface NoteDao {

    /**
     * Inserts a note into the database. Replaces the note if a conflict occurs.
     *
     * @param note The [Note] entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    /**
     * Updates an existing note in the database.
     *
     * @param note The [Note] entity with updated values.
     */
    @Update
    suspend fun update(note: Note)

    /**
     * Deletes a note from the database.
     *
     * @param note The [Note] entity to delete.
     */
    @Delete
    suspend fun delete(note: Note)

    /**
     * Retrieves all notes stored in the database ordered by sort_order and id.
     *
     * @return A list of all [Note] entities currently stored.
     */
    @Query("SELECT * FROM notes ORDER BY sort_order ASC, id ASC")
    suspend fun getAllNotes(): List<Note>

    /**
     * Retrieves a note by its integer ID.
     */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?
}

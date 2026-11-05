package com.example.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class that abstracts data access operations for notes.
 * Mediates between the data layer ([NoteDao]) and the business domain / ViewModels.
 * All database operations are explicitly executed on the I/O coroutine dispatcher.
 *
 * @property noteDao The Data Access Object used for executing Room SQL queries.
 * @property ioDispatcher The coroutine dispatcher dedicated to background disk I/O tasks (defaults to [Dispatchers.IO]).
 */
class NoteRepository(
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Persists a new note into the Room database asynchronously on [Dispatchers.IO].
     *
     * @param note The [Note] entity to be saved.
     */
    suspend fun saveNote(note: Note) = withContext(ioDispatcher) {
        noteDao.insert(note)
    }

    /**
     * Updates an existing note in the database asynchronously on [Dispatchers.IO].
     *
     * @param note The updated [Note] entity.
     */
    suspend fun editNote(note: Note) = withContext(ioDispatcher) {
        noteDao.update(note)
    }

    /**
     * Retrieves all saved notes from the database ordered by sort index on [Dispatchers.IO].
     *
     * @return List of all stored [Note] entities.
     */
    suspend fun getAllNotes(): List<Note> = withContext(ioDispatcher) {
        noteDao.getAllNotes()
    }

    /**
     * Removes a note from the database asynchronously on [Dispatchers.IO].
     *
     * @param note The [Note] entity to delete.
     */
    suspend fun delete(note: Note) = withContext(ioDispatcher) {
        noteDao.delete(note)
    }

    /**
     * Updates the sort order positions of a list of notes according to their list index asynchronously on [Dispatchers.IO].
     *
     * @param notes The reordered list of [Note] entities.
     */
    suspend fun updateNotesOrder(notes: List<Note>) = withContext(ioDispatcher) {
        notes.forEachIndexed { index, note ->
            noteDao.update(note.copy(sortOrder = index))
        }
    }
}


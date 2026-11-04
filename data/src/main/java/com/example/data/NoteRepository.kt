package com.example.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class that abstracts access to the underlying data sources ([NoteDao]).
 * All queries and operations are explicitly dispatched to [Dispatchers.IO] for main-thread safety.
 *
 * @property noteDao The Data Access Object used for executing database queries.
 * @property ioDispatcher The dispatcher used for background I/O operations.
 */
class NoteRepository(
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun saveNote(note: Note) = withContext(ioDispatcher) {
        noteDao.insert(note)
    }

    suspend fun editNote(note: Note) = withContext(ioDispatcher) {
        noteDao.update(note)
    }

    suspend fun getAllNotes(): List<Note> = withContext(ioDispatcher) {
        noteDao.getAllNotes()
    }

    suspend fun delete(note: Note) = withContext(ioDispatcher) {
        noteDao.delete(note)
    }

    suspend fun updateNotesOrder(notes: List<Note>) = withContext(ioDispatcher) {
        notes.forEachIndexed { index, note ->
            noteDao.update(note.copy(sortOrder = index))
        }
    }
}

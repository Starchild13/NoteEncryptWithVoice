package com.example.noteencrypt

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.encryption.KeyStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Supported UI layout modes for displaying secure note cards.
 */
enum class NoteLayoutMode(val displayName: String, val icon: String) {
    LIST("List", "📋"),
    GRID("Grid", "🧱"),
    MAP("Map Pin", "📍"),
    MOUNTAIN("Mountain Peak", "⛰️"),
    RIVER("River Stream", "🌊"),
    WATERFALL("Waterfall", "💧")
}

/**
 * ViewModel managing UI state for notes, including asynchronous encryption/decryption,
 * card selection and reordering, and selecting creative embedded layout modes.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val noteRepository = NoteRepository(noteDao)

    private val _notes = mutableStateOf<List<Note>>(emptyList())
    val notes: State<List<Note>> get() = _notes

    private val _layoutMode = mutableStateOf(NoteLayoutMode.LIST)
    val layoutMode: State<NoteLayoutMode> get() = _layoutMode

    init {
        fetchNotes()
    }

    /**
     * Fetches all notes from repository on [Dispatchers.IO] and posts state to main thread.
     */
    fun fetchNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val allNotes = noteRepository.getAllNotes()
            withContext(Dispatchers.Main) {
                _notes.value = allNotes
            }
        }
    }

    /**
     * Sets the active layout view mode (List, Grid, Map, Mountain, River, Waterfall).
     */
    fun setLayoutMode(mode: NoteLayoutMode) {
        _layoutMode.value = mode
    }

    /**
     * Encrypts plain text note content asynchronously on [Dispatchers.IO] and saves it to the database.
     */
    fun saveNote(title: String, plainText: String) {
        if (plainText.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val encryptedPayload = KeyStoreHelper.encryptData(plainText) ?: return@launch
            val currentList = noteRepository.getAllNotes()
            val newNote = Note(
                title = title.ifBlank { "Untitled Note" },
                encryptedNote = encryptedPayload,
                sortOrder = currentList.size
            )
            noteRepository.saveNote(newNote)
            val updatedNotes = noteRepository.getAllNotes()
            withContext(Dispatchers.Main) {
                _notes.value = updatedNotes
            }
        }
    }

    /**
     * Edits an existing note's title and encrypted content asynchronously on [Dispatchers.IO].
     */
    fun editNote(noteId: String, updatedTitle: String, updatedPlainText: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentNotes = noteRepository.getAllNotes()
            val targetId = noteId.toIntOrNull()
            val noteToEdit = currentNotes.find { it.id == targetId }

            if (noteToEdit != null) {
                val encryptedUpdated = KeyStoreHelper.encryptData(updatedPlainText) ?: return@launch
                val updatedNote = noteToEdit.copy(
                    title = updatedTitle.ifBlank { "Untitled Note" },
                    encryptedNote = encryptedUpdated
                )
                noteRepository.editNote(updatedNote)
                val updatedNotes = noteRepository.getAllNotes()
                withContext(Dispatchers.Main) {
                    _notes.value = updatedNotes
                    onComplete()
                }
            }
        }
    }

    /**
     * Decrypts an encrypted note payload asynchronously off the main thread.
     */
    suspend fun decryptNote(encryptedNote: String): String? {
        return KeyStoreHelper.decryptData(encryptedNote)
    }

    /**
     * Moves a note card from [fromIndex] to [toIndex] asynchronously on [Dispatchers.IO].
     */
    fun moveNote(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = noteRepository.getAllNotes().toMutableList()
            if (fromIndex < 0 || fromIndex >= currentList.size || toIndex < 0 || toIndex >= currentList.size || fromIndex == toIndex) return@launch

            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)

            noteRepository.updateNotesOrder(currentList)
            val updatedNotes = noteRepository.getAllNotes()
            withContext(Dispatchers.Main) {
                _notes.value = updatedNotes
            }
        }
    }

    /**
     * Moves a note card up in the list hierarchy asynchronously on [Dispatchers.IO].
     */
    fun moveNoteUp(index: Int) {
        if (index > 0) moveNote(index, index - 1)
    }

    /**
     * Moves a note card down in the list hierarchy asynchronously on [Dispatchers.IO].
     */
    fun moveNoteDown(index: Int) {
        if (index >= 0) moveNote(index, index + 1)
    }

    /**
     * Deletes a note asynchronously on [Dispatchers.IO].
     */
    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(note)
            val updatedNotes = noteRepository.getAllNotes()
            withContext(Dispatchers.Main) {
                _notes.value = updatedNotes
            }
        }
    }
}

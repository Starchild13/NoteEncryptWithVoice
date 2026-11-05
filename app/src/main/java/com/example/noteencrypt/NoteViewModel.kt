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
 * Supported UI layout themes for displaying secure note cards and creative visual landscapes.
 *
 * @property displayName User-friendly label displayed on chips and headers.
 * @property icon Visual emoji icon representing the thematic layout.
 */
enum class NoteLayoutMode(val displayName: String, val icon: String) {
    /** Standard vertical single-column list of rectangular notepad sheet cards */
    LIST("List", "📋"),

    /** 2-column grid view of brick-tile shaped note cards */
    GRID("Grid", "🧱"),

    /** Vertical stream of map-pin shaped note cards */
    MAP("Map Pin", "📍"),

    /** Vertical layout of mountain-peak styled note cards */
    MOUNTAIN("Mountain Peak", "⛰️"),

    /** Gradient-accented river wave layout for note cards */
    RIVER("River Stream", "🌊"),

    /** Staggered 2-column grid layout of droplet-shaped note cards */
    WATERFALL("Waterfall", "💧")
}

/**
 * NoteViewModel manages the UI state and business logic for secure notes.
 * Handles asynchronous AES encryption/decryption using [KeyStoreHelper], database operations
 * via [NoteRepository], card reordering, and switching between immersive thematic layouts.
 *
 * @param application Application context supplied to [AndroidViewModel].
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // Room Database DAO reference
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    // Centralized Repository for data operations
    private val noteRepository = NoteRepository(noteDao)

    // Internal mutable state holding the list of secure notes
    private val _notes = mutableStateOf<List<Note>>(emptyList())

    /** Public read-only Compose State exposing the current list of [Note] entities */
    val notes: State<List<Note>> get() = _notes

    // Internal mutable state holding the active UI layout theme
    private val _layoutMode = mutableStateOf(NoteLayoutMode.LIST)

    /** Public read-only Compose State exposing the active [NoteLayoutMode] */
    val layoutMode: State<NoteLayoutMode> get() = _layoutMode

    init {
        // Load initial notes from Room database on initialization
        fetchNotes()
    }

    /**
     * Fetches all secure notes from the Room repository on [Dispatchers.IO]
     * and updates the Compose state on the main thread.
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
     * Sets the active thematic layout view mode (List, Grid, Map Pin, Mountain Peak, River Stream, Waterfall Cliff).
     *
     * @param mode The selected [NoteLayoutMode] option.
     */
    fun setLayoutMode(mode: NoteLayoutMode) {
        _layoutMode.value = mode
    }

    /**
     * Encrypts plain text note content asynchronously on [Dispatchers.IO] using [KeyStoreHelper]
     * and persists the new secure note to the database.
     *
     * @param title User-provided title for the note card.
     * @param plainText The sensitive note content to encrypt and save.
     */
    fun saveNote(title: String, plainText: String) {
        if (plainText.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // Encrypt plain text using hardware-backed AES/GCM cipher
            val encryptedPayload = KeyStoreHelper.encryptData(plainText) ?: return@launch
            val currentList = noteRepository.getAllNotes()

            // Construct new Note entity with initial sort order positioned at the end of the list
            val newNote = Note(
                title = title.ifBlank { "Untitled Note" },
                encryptedNote = encryptedPayload,
                sortOrder = currentList.size
            )

            // Save to database and reload state
            noteRepository.saveNote(newNote)
            val updatedNotes = noteRepository.getAllNotes()
            withContext(Dispatchers.Main) {
                _notes.value = updatedNotes
            }
        }
    }

    /**
     * Edits an existing note's title and re-encrypts updated plain text content
     * asynchronously on [Dispatchers.IO].
     *
     * @param noteId String representation of the target note ID.
     * @param updatedTitle New title for the note.
     * @param updatedPlainText Updated content string to re-encrypt.
     * @param onComplete Optional callback executed on the main thread after edit completion.
     */
    fun editNote(noteId: String, updatedTitle: String, updatedPlainText: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentNotes = noteRepository.getAllNotes()
            val targetId = noteId.toIntOrNull()
            val noteToEdit = currentNotes.find { it.id == targetId }

            if (noteToEdit != null) {
                // Re-encrypt updated text using KeyStore AES
                val encryptedUpdated = KeyStoreHelper.encryptData(updatedPlainText) ?: return@launch
                val updatedNote = noteToEdit.copy(
                    title = updatedTitle.ifBlank { "Untitled Note" },
                    encryptedNote = encryptedUpdated
                )

                // Persist edit changes to Room DB
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
     * Decrypts an encrypted note payload asynchronously off the main thread using [KeyStoreHelper].
     *
     * @param encryptedNote Base64-encoded encrypted payload string.
     * @return Decrypted plain text string, or null if decryption fails.
     */
    suspend fun decryptNote(encryptedNote: String): String? {
        return KeyStoreHelper.decryptData(encryptedNote)
    }

    /**
     * Reorders a note card from [fromIndex] to [toIndex] asynchronously on [Dispatchers.IO].
     *
     * @param fromIndex Original position index of the note card.
     * @param toIndex Target position index to place the note card.
     */
    fun moveNote(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = noteRepository.getAllNotes().toMutableList()
            if (fromIndex < 0 || fromIndex >= currentList.size || toIndex < 0 || toIndex >= currentList.size || fromIndex == toIndex) return@launch

            // Perform in-memory swap and update sortOrder indices in DB
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
     *
     * @param index Current position index of the note card.
     */
    fun moveNoteUp(index: Int) {
        if (index > 0) moveNote(index, index - 1)
    }

    /**
     * Moves a note card down in the list hierarchy asynchronously on [Dispatchers.IO].
     *
     * @param index Current position index of the note card.
     */
    fun moveNoteDown(index: Int) {
        if (index >= 0) moveNote(index, index + 1)
    }

    /**
     * Deletes a secure note asynchronously on [Dispatchers.IO].
     *
     * @param note The [Note] entity to remove from Room storage.
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


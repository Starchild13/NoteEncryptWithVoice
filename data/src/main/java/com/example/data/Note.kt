package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class representing a secure note stored within the Room SQLite database.
 *
 * Each note entity contains an auto-generated unique ID, a user-visible plain text title,
 * an encrypted payload string (Base64-encoded cipher text containing the Initialization Vector),
 * and a sort order integer for customizable card positioning.
 *
 * @property id The auto-incremented primary key of the note record in the database.
 * @property title The plain text title displayed on note cards. Defaults to "Untitled Note".
 * @property encryptedNote The AES/GCM encrypted content stored as a Base64-encoded string.
 * @property sortOrder Integer value indicating the order position of the note card in list views.
 */
@Entity(tableName = "notes")
data class Note(
    // Auto-generated primary key for unique note identification
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Plain text title of the note
    @ColumnInfo(name = "title") val title: String = "Untitled Note",

    // Encrypted string payload holding AES/GCM ciphertext and prepended Initialization Vector (IV)
    @ColumnInfo(name = "encrypted_note") val encryptedNote: String,

    // Relative position index used for custom drag-and-drop or tap reordering
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)


package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class representing a secure note stored in the Room database.
 *
 * @property id The primary key of the note.
 * @property title The plain text title of the note.
 * @property encryptedNote The encrypted content of the note stored as a Base64 encoded String.
 * @property sortOrder Integer value indicating the order position of the note card.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String = "Untitled Note",
    @ColumnInfo(name = "encrypted_note") val encryptedNote: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)

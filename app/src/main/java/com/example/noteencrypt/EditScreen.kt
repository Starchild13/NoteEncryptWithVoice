package com.example.noteencrypt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.encryption.KeyStoreHelper

/**
 * Composable screen for editing an existing note's title and decrypted content.
 * Decrypts note payload asynchronously using coroutines off the main thread.
 *
 * @param initialTitle The note's current title.
 * @param encryptedNote Encrypted content payload.
 * @param onSaveEdit Callback invoked with (updatedTitle, updatedPlainText).
 * @param onCancelEdit Callback invoked when canceling edit.
 */
@Composable
fun EditScreen(
    initialTitle: String,
    encryptedNote: String,
    onSaveEdit: (String, String) -> Unit,
    onCancelEdit: () -> Unit,
) {
    var titleText by remember { mutableStateOf(initialTitle) }
    var noteText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(encryptedNote) {
        val decrypted = KeyStoreHelper.decryptData(encryptedNote) ?: ""
        noteText = decrypted
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Edit Note",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Editable Note Title
        TextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Note Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Editable Note Content
            TextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    showError = false
                },
                label = { Text("Note Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = false,
                maxLines = 5
            )
        }

        if (showError) {
            Text(
                text = "Note content cannot be empty.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (!isLoading && noteText.isNotBlank()) {
                    onSaveEdit(titleText, noteText)
                } else if (!isLoading) {
                    showError = true
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCancelEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

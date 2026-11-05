package com.example.noteencrypt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.encryption.KeyStoreHelper
import java.util.Locale

/**
 * EditScreen provides a modern Material 3 interface for editing an existing note's title and decrypted content.
 * Supports playing voice memo audio if the note is an encrypted voice recording.
 *
 * @param initialTitle The current title of the note.
 * @param encryptedNote The encrypted payload string stored in the database.
 * @param onSaveEdit Callback invoked with the updated title and decrypted plain text.
 * @param onCancelEdit Callback invoked when the user cancels editing.
 */
@Composable
fun EditScreen(
    initialTitle: String,
    encryptedNote: String,
    onSaveEdit: (String, String) -> Unit,
    onCancelEdit: () -> Unit,
) {
    // Editable title state initialized with initialTitle argument
    var titleText by remember { mutableStateOf(initialTitle) }

    // Decrypted plain text note content state
    var noteText by remember { mutableStateOf("") }

    // Loading flag while AES decryption is in progress
    var isLoading by remember { mutableStateOf(true) }

    // Validation error state for empty content submissions
    var showError by remember { mutableStateOf(false) }

    // Playback state tracking active voice memo audio playback
    var isPlayingVoice by remember { mutableStateOf(false) }

    // Asynchronously decrypt note content off the UI thread when encryptedNote payload changes
    LaunchedEffect(encryptedNote) {
        val decrypted = KeyStoreHelper.decryptData(encryptedNote) ?: ""
        noteText = decrypted
        isLoading = false
    }

    // Safely release audio playback resources when navigating away from EditScreen
    DisposableEffect(Unit) {
        onDispose {
            VoiceMemoManager.stopPlaying()
        }
    }

    // Parse decrypted text payload to check if note contains voice recording metadata
    val voicePayload = remember(noteText) { VoiceMemoPayload.parse(noteText) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Header bar containing back button, screen title, and contextual subtitle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            VoiceMemoManager.stopPlaying()
                            onCancelEdit()
                        },
                        modifier = Modifier.padding(end = 14.dp)
                    ) {
                        Text("← Back")
                    }
                    Column {
                        Text(
                            text = if (voicePayload.isVoiceMemo) "Voice Memo 🎙️" else "Edit Note ✏️",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (voicePayload.isVoiceMemo) "Play recording & edit title" else "Update decrypted content & re-encrypt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Form Card Container
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Editable Note Title Field
                        TextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text("Note Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (isLoading) {
                            // Show loading spinner with decryption status notice
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Decrypting payload with KeyStore AES...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else if (voicePayload.isVoiceMemo) {
                            // Dedicated Voice Memo Audio Player Card
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎙️ Voice Audio Recording",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        // Formatted duration badge chip (mm:ss)
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    String.format(
                                                        Locale.getDefault(),
                                                        "%02d:%02d",
                                                        voicePayload.durationSeconds / 60,
                                                        voicePayload.durationSeconds % 60
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Audio Playback / Pause Action Button
                                    Button(
                                        onClick = {
                                            if (isPlayingVoice) {
                                                VoiceMemoManager.pausePlaying()
                                                isPlayingVoice = false
                                            } else {
                                                val started = VoiceMemoManager.startPlaying(voicePayload.audioPath) {
                                                    isPlayingVoice = false
                                                }
                                                if (started) isPlayingVoice = true
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isPlayingVoice) "⏸ Pause Audio" else "▶ Play Recording")
                                    }
                                }
                            }
                        } else {
                            // Standard Decrypted Plain Text Content Editing Field
                            TextField(
                                value = noteText,
                                onValueChange = {
                                    noteText = it
                                    showError = false
                                },
                                label = { Text("Decrypted Note Content") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                singleLine = false,
                                minLines = 4,
                                maxLines = 8,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Display validation error if user attempts to save empty content
                        if (showError) {
                            Text(
                                text = "Note content cannot be empty.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Form Action Buttons (Cancel / Save & Re-encrypt)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    VoiceMemoManager.stopPlaying()
                                    onCancelEdit()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    VoiceMemoManager.stopPlaying()
                                    if (!isLoading && noteText.isNotBlank()) {
                                        onSaveEdit(titleText, noteText)
                                    } else if (!isLoading) {
                                        showError = true
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save 🔒")
                            }
                        }
                    }
                }
            }
        }
    }
}


package com.example.noteencrypt

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteencrypt.ui.theme.NoteEncryptTheme

/**
 * MainActivity serves as the single-activity container and primary entry point for NoteEncrypt.
 *
 * It initializes the Jetpack Compose UI layout, sets up application-wide Material 3 theme styling,
 * and configures the Jetpack Navigation controller to route between key user flows:
 *
 * 1. [SecureNotesUI] ("secure_notes"):
 *    - Main dashboard displaying encrypted notes across 6 thematic layouts (List, Grid, Map, Mountain, River, Waterfall).
 *    - Integrated quick note creation form with inline voice memo recording/dictation.
 *    - Interactive card reordering, decryption viewing, and note deletion.
 *
 * 2. [TipsScreen] ("tips"):
 *    - Dedicated guide screen presenting note-taking best practices, security overview, voice dictation usage,
 *      and thematic layout instructions.
 *
 * 3. [EditScreen] ("edit_note/{noteId}/{title}/{encryptedNote}"):
 *    - Screen for decrypting and editing an existing note's title and plain text content using hardware-backed AES keys.
 */
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Jetpack Compose UI tree
        setContent {
            // Apply application Material 3 Theme (supports dynamic colors on Android 12+ and dark/light modes)
            NoteEncryptTheme {
                // Background surface providing consistent theme styling and safe drawing canvas
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Remember single NavController instance for orchestrating screen transitions
                    val navController = rememberNavController()

                    // Instantiate shared ViewModel across navigation graph destinations
                    val noteViewModel: NoteViewModel = viewModel()

                    // Observe reactive StateFlow/State values from NoteViewModel
                    val notes by noteViewModel.notes
                    val layoutMode by noteViewModel.layoutMode

                    // Navigation host managing screen routes and destination composables
                    NavHost(
                        navController = navController,
                        startDestination = "secure_notes"
                    ) {
                        // =========================================================================
                        // ROUTE 1: MAIN DASHBOARD SCREEN ("secure_notes")
                        // =========================================================================
                        composable("secure_notes") {
                            SecureNotesUI(
                                notes = notes,
                                layoutMode = layoutMode,
                                // Callback when user selects a different layout mode (e.g. LIST, GRID, MAP, etc.)
                                onSelectLayoutMode = { mode ->
                                    noteViewModel.setLayoutMode(mode)
                                },
                                // Callback to encrypt and persist a new note
                                onSaveNote = { title, plain ->
                                    noteViewModel.saveNote(title, plain)
                                },
                                // Callback to decrypt note payload asynchronously off main thread
                                onViewNote = { encryptedNote ->
                                    noteViewModel.decryptNote(encryptedNote)
                                },
                                navController = navController,
                                // Navigation action to open dedicated Tips & Guide screen
                                onOpenTips = {
                                    navController.navigate("tips")
                                },
                                // Navigation action to open Edit screen with URL-encoded arguments
                                onEditNote = { note ->
                                    val encodedId = Uri.encode(note.id.toString())
                                    val encodedTitle = Uri.encode(note.title)
                                    val encodedPayload = Uri.encode(note.encryptedNote)
                                    navController.navigate("edit_note/$encodedId/$encodedTitle/$encodedPayload")
                                },
                                // Callback to delete a note from Room database
                                onDeleteNote = { note ->
                                    noteViewModel.delete(note)
                                },
                                // Callback to reorder notes by moving a card from fromIndex to toIndex
                                onMoveNote = { fromIndex, toIndex ->
                                    noteViewModel.moveNote(fromIndex, toIndex)
                                }
                            )
                        }

                        // =========================================================================
                        // ROUTE 2: DEDICATED TIPS & GUIDE SCREEN ("tips")
                        // =========================================================================
                        composable("tips") {
                            TipsScreen(
                                onNavigateBack = {
                                    // Pop backstack to return to Dashboard, fallback to explicit navigation if empty
                                    if (!navController.popBackStack()) {
                                        navController.navigate("secure_notes") {
                                            popUpTo("secure_notes") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // =========================================================================
                        // ROUTE 3: EDIT NOTE SCREEN ("edit_note/{noteId}/{title}/{encryptedNote}")
                        // =========================================================================
                        composable(
                            route = "edit_note/{noteId}/{title}/{encryptedNote}",
                            arguments = listOf(
                                navArgument("noteId") { type = NavType.StringType },
                                navArgument("title") { type = NavType.StringType },
                                navArgument("encryptedNote") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            // Extract and URL-decode backstack arguments safely
                            val noteId = backStackEntry.arguments?.getString("noteId")?.let { Uri.decode(it) }.orEmpty()
                            val title = backStackEntry.arguments?.getString("title")?.let { Uri.decode(it) }.orEmpty()
                            val encrypted = backStackEntry.arguments?.getString("encryptedNote")?.let { Uri.decode(it) }.orEmpty()

                            EditScreen(
                                initialTitle = title,
                                encryptedNote = encrypted,
                                // Callback to re-encrypt updated text and update repository
                                onSaveEdit = { updatedTitle, updatedPlain ->
                                    noteViewModel.editNote(noteId, updatedTitle, updatedPlain) {
                                        navController.popBackStack()
                                    }
                                },
                                // Cancel editing and return to previous screen
                                onCancelEdit = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate("secure_notes") {
                                            popUpTo("secure_notes") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

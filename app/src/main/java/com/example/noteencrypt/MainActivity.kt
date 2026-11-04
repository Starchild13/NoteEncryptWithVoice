package com.example.noteencrypt

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Main Activity routing between SecureNotesUI (supporting List, Grid, Map, Mountain, River, Waterfall layouts)
 * and EditScreen.
 */
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val noteViewModel: NoteViewModel = viewModel()
            val notes by noteViewModel.notes
            val layoutMode by noteViewModel.layoutMode

            NavHost(navController, startDestination = "secure_notes") {
                // -- DASHBOARD SCREEN WITH MULTIPLE EXCITING LAYOUT OPTIONS --
                composable("secure_notes") {
                    SecureNotesUI(
                        notes = notes,
                        layoutMode = layoutMode,
                        onSelectLayoutMode = { mode -> noteViewModel.setLayoutMode(mode) },
                        onSaveNote = { title, plain ->
                            noteViewModel.saveNote(title, plain)
                        },
                        onViewNote = { encryptedNote ->
                            noteViewModel.decryptNote(encryptedNote)
                        },
                        navController = navController,
                        onEditNote = { note ->
                            val encodedId = Uri.encode(note.id.toString())
                            val encodedTitle = Uri.encode(note.title)
                            val encodedPayload = Uri.encode(note.encryptedNote)
                            navController.navigate("edit_note/$encodedId/$encodedTitle/$encodedPayload")
                        },
                        onDeleteNote = { note ->
                            noteViewModel.delete(note)
                        },
                        onMoveNote = { fromIndex, toIndex ->
                            noteViewModel.moveNote(fromIndex, toIndex)
                        }
                    )
                }

                // -- EDIT SCREEN --
                composable(
                    "edit_note/{noteId}/{title}/{encryptedNote}",
                    arguments = listOf(
                        navArgument("noteId") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("encryptedNote") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.let { Uri.decode(it) }.orEmpty()
                    val title = backStackEntry.arguments?.getString("title")?.let { Uri.decode(it) }.orEmpty()
                    val encrypted = backStackEntry.arguments?.getString("encryptedNote")?.let { Uri.decode(it) }.orEmpty()

                    EditScreen(
                        initialTitle = title,
                        encryptedNote = encrypted,
                        onSaveEdit = { updatedTitle, updatedPlain ->
                            noteViewModel.editNote(noteId, updatedTitle, updatedPlain) {
                                navController.popBackStack()
                            }
                        },
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

package com.example.noteencrypt

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed as staggeredItemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------------------------------
// THEMATIC LAYOUT CARD SHAPES
// Custom canvas path shapes for rendering note cards across 6 visual themes
// -------------------------------------------------------------------------------------

/**
 * Custom [GenericShape] representing a classic folded notepad paper sheet.
 * Used for rendering cards in the LIST layout theme.
 */
val NotepadSheetShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val fold = 24f
    val r = 12f
    moveTo(0f, r)
    quadraticTo(0f, 0f, r, 0f)
    lineTo(w - fold, 0f)
    lineTo(w, fold)
    lineTo(w, h - r)
    quadraticTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticTo(0f, h, 0f, h - r)
    close()
}

/**
 * Custom [GenericShape] representing a rounded brick tile block.
 * Used for rendering cards in the GRID layout theme.
 */
val BrickTileShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = 16f
    moveTo(r, 0f)
    lineTo(w - r, 0f)
    quadraticTo(w, 0f, w, r)
    lineTo(w, h - r)
    quadraticTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticTo(0f, h, 0f, h - r)
    lineTo(0f, r)
    quadraticTo(0f, 0f, r, 0f)
    close()
}

/**
 * Custom [GenericShape] representing a map pin location marker geometry.
 * Used for rendering cards in the MAP layout theme.
 */
val MapPinShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val pinHeadRadius = (w / 2f).coerceAtMost(h * 0.35f)
    val topCenterY = pinHeadRadius + 4f
    moveTo(w / 2f - pinHeadRadius, topCenterY)
    arcTo(
        rect = Rect(left = w / 2f - pinHeadRadius, top = 4f, right = w / 2f + pinHeadRadius, bottom = 4f + pinHeadRadius * 2f),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 180f,
        forceMoveTo = false
    )
    lineTo(w / 2f, h - 4f)
    close()
}

/**
 * Custom [GenericShape] representing a pointed mountain peak summit silhouette.
 * Used for rendering cards in the MOUNTAIN layout theme.
 */
val MountainPeakShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val peakH = 32f
    val r = 12f
    moveTo(0f, peakH + r)
    quadraticTo(0f, peakH, r, peakH)
    lineTo(w / 2f - 16f, 8f)
    quadraticTo(w / 2f, 0f, w / 2f + 16f, 8f)
    lineTo(w - r, peakH)
    quadraticTo(w, peakH, w, peakH + r)
    lineTo(w, h - r)
    quadraticTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticTo(0f, h, 0f, h - r)
    close()
}

/**
 * Custom [GenericShape] representing an undulating river stream wave contour.
 * Used for rendering cards in the RIVER layout theme.
 */
val RiverStreamShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val waveH = 20f
    moveTo(0f, waveH)
    cubicTo(w * 0.25f, 0f, w * 0.75f, waveH * 2f, w, waveH)
    lineTo(w, h - waveH)
    cubicTo(w * 0.75f, h, w * 0.25f, h - waveH * 2f, 0f, h - waveH)
    close()
}

/**
 * Custom [GenericShape] representing a cascading waterfall water drop silhouette.
 * Used for rendering cards in the WATERFALL layout theme.
 */
val WaterfallDropShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w / 2f, 4f)
    cubicTo(w * 0.95f, h * 0.2f, w, h * 0.6f, w, h - 16f)
    quadraticTo(w, h, w - 16f, h)
    lineTo(16f, h)
    quadraticTo(0f, h, 0f, h - 16f)
    cubicTo(0f, h * 0.6f, w * 0.05f, h * 0.2f, w / 2f, 4f)
    close()
}


/**
 * Animated voice audio waveform visualizer displaying 16 pulsating vertical bars during active audio playback.
 *
 * @param isPlaying Boolean indicating whether audio playback is currently active.
 * @param modifier Layout modifier applied to the waveform container.
 */
@Composable
fun VoiceWaveformVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val barHeights = List(16) { index ->
        if (isPlaying) {
            val animateFactor by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 250 + (index % 5) * 80, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            animateFactor
        } else {
            0.35f
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEach { heightFactor ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(heightFactor)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}

/**
 * Primary dashboard UI composable for NoteEncrypt.
 *
 * Displays:
 * 1. App header with title and quick navigation to Tips & Guide.
 * 2. Layout theme selector chip row (List, Grid, Map Pin, Mountain Peak, River Stream, Waterfall Cliff).
 * 3. Quick note creation form with inline voice memo recording.
 * 4. Active reordering target banner when a card is selected for moving.
 * 5. Dynamic grid/list rendering based on active [NoteLayoutMode].
 * 6. Decrypted note content popup dialog with audio playback visualizer.
 * 7. Voice memo recording dialog with live timer and pulsating mic indicator.
 *
 * @param notes Current list of [Note] entities.
 * @param layoutMode Currently active [NoteLayoutMode] visual theme.
 * @param onSelectLayoutMode Callback invoked when user selects a different layout mode.
 * @param onSaveNote Callback to encrypt and persist a new note.
 * @param onViewNote Suspend callback to decrypt note payload for viewing.
 * @param navController Navigation controller.
 * @param onOpenTips Callback to open the Tips & Guide screen.
 * @param onEditNote Callback to navigate to EditScreen for editing a note.
 * @param onDeleteNote Callback to delete a note.
 * @param onMoveNote Callback to reorder note card position from a starting index to a target index.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SecureNotesUI(

    notes: List<Note>,
    layoutMode: NoteLayoutMode,
    onSelectLayoutMode: (NoteLayoutMode) -> Unit,
    onSaveNote: (String, String) -> Unit,
    onViewNote: suspend (String) -> String?,
    navController: NavController,
    onOpenTips: () -> Unit = {},
    onEditNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onMoveNote: (fromIndex: Int, toIndex: Int) -> Unit
) {

    val context = LocalContext.current
    var titleText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var decryptedNote by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Voice Memo recording & playback states
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordSeconds by remember { mutableStateOf(0) }
    var voiceTitleText by remember { mutableStateOf("") }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Clean up audio playback when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            VoiceMemoManager.stopPlaying()
        }
    }

    // Voice recording timer effect
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceRecordSeconds = 0
            while (isRecordingVoice) {
                delay(1000L)
                voiceRecordSeconds++
            }
        }
    }

    val handleCardClick: (Int) -> Unit = { clickedIndex ->
        val currentSelected = selectedIndex
        when {
            currentSelected == null -> selectedIndex = clickedIndex
            currentSelected == clickedIndex -> selectedIndex = null
            else -> {
                onMoveNote(currentSelected, clickedIndex)
                selectedIndex = null
            }
        }
    }

    val handleViewNote: (Note) -> Unit = { note ->
        coroutineScope.launch {
            VoiceMemoManager.stopPlaying()
            isPlayingAudio = false
            decryptedNote = onViewNote(note.encryptedNote)
            showDialog = decryptedNote != null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // App Header with title and quick help / tips action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NoteEncrypt 🔒",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Encrypted Secure Notes & Ideas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onOpenTips,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("💡 Tips Screen", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Select Layout Theme:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NoteLayoutMode.entries.forEach { mode ->
                    val isSelected = mode == layoutMode
                    AssistChip(
                        onClick = { onSelectLayoutMode(mode) },
                        label = { Text("${mode.icon} ${mode.displayName}") },
                        colors = if (isSelected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        }
                    )
                }
            }

            // Quick Note Creator
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✍️ Create Note",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Note Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true
                    )

                    TextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Enter Note Content to Encrypt") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (noteText.isNotBlank()) {
                                    onSaveNote(titleText, noteText)
                                    titleText = ""
                                    noteText = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🔒 Encrypt & Save", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = {
                                voiceTitleText = if (titleText.isNotBlank()) titleText else "Voice Note " + SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
                                recordedFile = null
                                showVoiceDialog = true
                            }
                        ) {
                            Text("🎙️ Record Voice", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (selectedIndex != null && selectedIndex!! in notes.indices) {
                val selectedTitle = notes[selectedIndex!!].title
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Moving '$selectedTitle'. Tap target card to place.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        OutlinedButton(
                            onClick = { selectedIndex = null },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Text(
                text = "Saved Notes (${layoutMode.icon} ${layoutMode.displayName} Layout):",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (layoutMode) {
                    NoteLayoutMode.LIST -> RenderListLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                    NoteLayoutMode.GRID -> RenderGridLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                    NoteLayoutMode.MAP -> RenderMapLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                    NoteLayoutMode.MOUNTAIN -> RenderMountainLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                    NoteLayoutMode.RIVER -> RenderRiverLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                    NoteLayoutMode.WATERFALL -> RenderWaterfallLayout(notes, selectedIndex, handleCardClick, onEditNote, handleViewNote, onDeleteNote)
                }
            }

            // Decrypted Note View / Audio Player Dialog
            if (showDialog && decryptedNote != null) {
                val voicePayload = remember(decryptedNote) { VoiceMemoPayload.parse(decryptedNote!!) }

                AlertDialog(
                    onDismissRequest = {
                        VoiceMemoManager.stopPlaying()
                        isPlayingAudio = false
                        showDialog = false
                    },
                    title = {
                        Text(if (voicePayload.isVoiceMemo) "🎙️ Encrypted Voice Memo" else "🔓 Decrypted Note Content")
                    },
                    text = {
                        if (voicePayload.isVoiceMemo) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = voicePayload.title.ifBlank { "Voice Note" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            val minutes = voicePayload.durationSeconds / 60
                                            val seconds = voicePayload.durationSeconds % 60
                                            Text(
                                                "⏱️ %02d:%02d".format(minutes, seconds),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                VoiceWaveformVisualizer(isPlaying = isPlayingAudio)
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (isPlayingAudio) {
                                            VoiceMemoManager.pausePlaying()
                                            isPlayingAudio = false
                                        } else {
                                            val started = VoiceMemoManager.startPlaying(voicePayload.audioPath) {
                                                isPlayingAudio = false
                                            }
                                            if (started) isPlayingAudio = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (isPlayingAudio) "⏸ Pause Audio" else "▶ Play Recording")
                                }

                                if (voicePayload.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = voicePayload.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(decryptedNote!!)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                VoiceMemoManager.stopPlaying()
                                isPlayingAudio = false
                                showDialog = false
                            }
                        ) {
                            Text("Close")
                        }
                    }
                )
            }

            // Voice Memo Recording Dialog
            if (showVoiceDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isRecordingVoice) {
                            VoiceMemoManager.stopPlaying()
                            isPlayingAudio = false
                            showVoiceDialog = false
                        }
                    },
                    title = { Text("🎙️ Voice Memo Recording") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextField(
                                value = voiceTitleText,
                                onValueChange = { voiceTitleText = it },
                                label = { Text("Voice Memo Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                singleLine = true
                            )

                            if (isRecordingVoice) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val pulseAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = pulseAlpha)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val mins = voiceRecordSeconds / 60
                                    val secs = voiceRecordSeconds % 60
                                    Text(
                                        text = "%02d:%02d".format(mins, secs),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Recording voice memo... Speak into your microphone.", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        isRecordingVoice = false
                                        recordedFile = VoiceMemoManager.stopRecording()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Stop Recording ⏹")
                                }
                            } else {
                                if (recordedFile == null) {
                                    Button(
                                        onClick = {
                                            voiceRecordSeconds = 0
                                            recordedFile = VoiceMemoManager.startRecording(context)
                                            isRecordingVoice = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start Recording Voice Memo 🎙️")
                                    }
                                } else {
                                    // Voice Memo Recorded Preview State
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🎙️ Voice Recording Ready",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            VoiceWaveformVisualizer(isPlaying = isPlayingAudio)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val durationMins = voiceRecordSeconds / 60
                                            val durationSecs = voiceRecordSeconds % 60
                                            Text(
                                                text = "Duration: %02d:%02d".format(durationMins, durationSecs),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    if (isPlayingAudio) {
                                                        VoiceMemoManager.pausePlaying()
                                                        isPlayingAudio = false
                                                    } else {
                                                        val started = VoiceMemoManager.startPlaying(recordedFile!!.absolutePath) {
                                                            isPlayingAudio = false
                                                        }
                                                        if (started) isPlayingAudio = true
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(if (isPlayingAudio) "⏸ Pause Audio" else "▶ Play Recording")
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val filePath = recordedFile?.absolutePath ?: ""
                                            val payload = VoiceMemoPayload.format(filePath, voiceRecordSeconds, voiceTitleText)
                                            onSaveNote(voiceTitleText, payload)
                                            VoiceMemoManager.stopPlaying()
                                            isPlayingAudio = false
                                            recordedFile = null
                                            showVoiceDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Encrypt & Save Voice Memo 🔒")
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (!isRecordingVoice) {
                            OutlinedButton(
                                onClick = {
                                    VoiceMemoManager.stopPlaying()
                                    isPlayingAudio = false
                                    recordedFile = null
                                    showVoiceDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Renders the LIST layout theme using a vertical [LazyColumn] of notepad sheet cards.
 */
@Composable
private fun RenderListLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            NotepadCard(
                noteTitle = note.title,
                isSelected = isSelected,
                onCardClick = { onSelectCard(index) },
                onEdit = { onEditNote(note) },
                onView = { onViewNote(note) },
                onDelete = { onDeleteNote(note) }
            )
        }
    }
}

/**
 * Renders the GRID layout theme using a 2-column [LazyVerticalGrid] of brick tile cards.
 */
@Composable
private fun RenderGridLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridItemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, BrickTileShape)
                    .clickable { onSelectCard(index) },
                shape = BrickTileShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isSelected) "🎯 ${note.title}" else "🧱 ${note.title}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                }
            }
        }
    }
}

/**
 * Renders the MAP layout theme using map-pin shaped note cards.
 */
@Composable
private fun RenderMapLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, MapPinShape)
                    .clickable { onSelectCard(index) },
                shape = MapPinShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSelected) "🎯 ${note.title}" else "📍 ${note.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 2
                    )
                    ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                }
            }
        }
    }
}

/**
 * Renders the MOUNTAIN layout theme using mountain-peak styled note cards.
 */
@Composable
private fun RenderMountainLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, MountainPeakShape)
                    .clickable { onSelectCard(index) },
                shape = MountainPeakShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSelected) "🎯 ${note.title}" else "⛰️ ${note.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 2
                    )
                    ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                }
            }
        }
    }
}

/**
 * Renders the RIVER layout theme using wave-curved cards with horizontal gradient brushes.
 */
@Composable
private fun RenderRiverLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            val waveBrush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer
                )
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RiverStreamShape)
                    .clickable { onSelectCard(index) },
                shape = RiverStreamShape
            ) {
                Box(
                    modifier = Modifier
                        .background(waveBrush)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSelected) "🎯 ${note.title}" else "🌊 ${note.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2
                        )
                        ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                    }
                }
            }
        }
    }
}

/**
 * Renders the WATERFALL layout theme using a staggered vertical grid of waterdrop-shaped cards.
 */
@Composable
private fun RenderWaterfallLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        staggeredItemsIndexed(notes) { index, note ->
            val isSelected = selectedIndex == index
            val waterfallBrush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
            val minHeight = if (index % 2 == 0) 190.dp else 230.dp

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight)
                    .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.Transparent, WaterfallDropShape)
                    .clickable { onSelectCard(index) },
                shape = WaterfallDropShape
            ) {
                Box(modifier = Modifier.background(waterfallBrush).padding(top = 28.dp, bottom = 20.dp, start = 12.dp, end = 12.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSelected) "🎯 ${note.title}" else "💧 ${note.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 2
                        )
                        ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SHARED UI HELPER COMPONENTS
// -------------------------------------------------------------------------------------

/**
 * Shared horizontal row of action buttons (Move, Edit, View, Delete) displayed on every note card.
 *
 * @param isSelected Boolean indicating whether this card is currently selected for reordering.
 * @param onSelectMove Callback invoked to select/target card for positioning.
 * @param onEdit Callback invoked to open the Edit screen for this note.
 * @param onView Callback invoked to decrypt and view this note payload in a popup dialog.
 * @param onDelete Callback invoked to remove this note from Room storage.
 */
@Composable
private fun ActionRow(
    isSelected: Boolean,
    onSelectMove: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = onSelectMove,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(0.dp),
            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        ) {
            Text(if (isSelected) "🎯 Target" else "Move", style = MaterialTheme.typography.labelSmall)
        }
        Button(
            onClick = onEdit,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Edit", style = MaterialTheme.typography.labelSmall)
        }
        Button(
            onClick = onView,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("View", style = MaterialTheme.typography.labelSmall)
        }
        Button(
            onClick = onDelete,
            modifier = Modifier.weight(1f).height(34.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Del", style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Standard notepad sheet card component rendering a note title and action row.
 *
 * @param noteTitle Title string of the note.
 * @param shape Shape geometry used for card outline.
 * @param colors Card container and content colors.
 * @param isSelected Selection status for reordering.
 * @param onCardClick Card tap handler.
 * @param onEdit Edit action handler.
 * @param onView View action handler.
 * @param onDelete Delete action handler.
 */
@Composable
fun NotepadCard(
    noteTitle: String,
    shape: Shape = NotepadSheetShape,
    colors: CardColors = CardDefaults.cardColors(),
    isSelected: Boolean = false,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(if (isSelected) 3.dp else 0.dp, borderColor, shape)
            .clickable { onCardClick() },
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isSelected) "🎯 $noteTitle" else "📝 $noteTitle",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ActionRow(isSelected, onCardClick, onEdit, onView, onDelete)
        }
    }
}


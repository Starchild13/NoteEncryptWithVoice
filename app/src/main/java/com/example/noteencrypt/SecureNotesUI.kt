package com.example.noteencrypt

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------------------------------
// THEMATIC LAYOUT CARD SHAPES
// -------------------------------------------------------------------------------------

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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SecureNotesUI(
    notes: List<Note>,
    layoutMode: NoteLayoutMode,
    onSelectLayoutMode: (NoteLayoutMode) -> Unit,
    onSaveNote: (String, String) -> Unit,
    onViewNote: suspend (String) -> String?,
    navController: NavController,
    onEditNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onMoveNote: (fromIndex: Int, toIndex: Int) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var decryptedNote by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Guide and Voice Memo states
    var showGuide by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordSeconds by remember { mutableStateOf(0) }
    var voiceTitleText by remember { mutableStateOf("") }
    var translatedVoiceText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

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
            decryptedNote = onViewNote(note.encryptedNote)
            showDialog = decryptedNote != null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Header with title and quick help / voice memo actions
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showGuide = !showGuide },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (showGuide) "Hide Guide" else "💡 Tips", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = {
                        voiceTitleText = "Voice Memo " + SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
                        translatedVoiceText = ""
                        showVoiceDialog = true
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🎙️ Voice Memo", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible "How to Write Good Notes" Guide Card
        AnimatedVisibility(visible = showGuide) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 How to Write Good Notes & Jot Down Ideas",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Be Concise: Capture core bullet points and takeaways quickly.\n" +
                                "• Use Clear Titles: Make retrieval instant and intuitive.\n" +
                                "• Structure Thoughts: Group related items logically or reorder cards easily.\n" +
                                "• Encrypt Sensitive Ideas: Protect passwords, journals, and private thoughts with AES encryption.\n" +
                                "• Voice Memos: Record thoughts on-the-go and translate speech directly into secure encrypted notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
            Column(modifier = Modifier.padding(12.dp)) {
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
                        .padding(bottom = 8.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onSaveNote(titleText, noteText)
                            titleText = ""
                            noteText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Encrypt & Save Secure Note 🔒")
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

        // Decrypted Note Dialog
        if (showDialog && decryptedNote != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("🔓 Decrypted Note Content") },
                text = { Text(decryptedNote!!) },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Voice Memo Recording & Transcription Dialog
        if (showVoiceDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isRecordingVoice) showVoiceDialog = false
                },
                title = { Text("🎙️ Voice Memo & Speech Translation") },
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
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", voiceRecordSeconds / 60, voiceRecordSeconds % 60),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recording voice memo... Speak your ideas clearly.", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isRecordingVoice = false
                                    isTranslating = true
                                    coroutineScope.launch {
                                        delay(1500L) // Simulate speech-to-text translation processing
                                        translatedVoiceText = "Voice transcription: Ideas captured at " +
                                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) +
                                                ". This voice memo was recorded, translated to text, and ready for secure encryption."
                                        isTranslating = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Stop Recording")
                            }
                        } else if (isTranslating) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Translating voice audio to text...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            if (translatedVoiceText.isBlank()) {
                                Button(
                                    onClick = { isRecordingVoice = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Start Recording Voice Memo")
                                }
                            } else {
                                TextField(
                                    value = translatedVoiceText,
                                    onValueChange = { translatedVoiceText = it },
                                    label = { Text("Translated Text") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    singleLine = false
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (translatedVoiceText.isNotBlank()) {
                                            onSaveNote(voiceTitleText, translatedVoiceText)
                                            showVoiceDialog = false
                                        }
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
                    if (!isRecordingVoice && !isTranslating) {
                        Button(onClick = { showVoiceDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RenderListLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(notes) { index, note ->
            NotepadCard(
                noteTitle = note.title,
                shape = NotepadSheetShape,
                isSelected = index == selectedIndex,
                onCardClick = { onSelectCard(index) },
                onEdit = { onEditNote(note) },
                onView = { onViewNote(note) },
                onDelete = { onDeleteNote(note) }
            )
        }
    }
}

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
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridItemsIndexed(notes) { index, note ->
            NotepadCard(
                noteTitle = note.title,
                shape = BrickTileShape,
                isSelected = index == selectedIndex,
                onCardClick = { onSelectCard(index) },
                onEdit = { onEditNote(note) },
                onView = { onViewNote(note) },
                onDelete = { onDeleteNote(note) }
            )
        }
    }
}

@Composable
private fun RenderMapLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF5EA))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(notes) { index, note ->
                val isSelected = index == selectedIndex
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF8B5A2B)
                val isLeft = index % 2 == 0
                val startPad = if (isLeft) 16.dp else 90.dp
                val endPad = if (isLeft) 90.dp else 16.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(vertical = 6.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val roadWidth = 42.dp.toPx()
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), 0f)
                        val startX = if (index % 2 == 0) w * 0.75f else w * 0.25f
                        val endX = if (index % 2 == 0) w * 0.25f else w * 0.75f
                        val path = Path().apply {
                            moveTo(startX, 0f)
                            cubicTo(startX, h * 0.5f, endX, h * 0.5f, endX, h)
                        }
                        drawPath(path, Color(0xFF78909C), style = Stroke(width = roadWidth + 8.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(path, Color(0xFF37474F), style = Stroke(width = roadWidth, cap = StrokeCap.Round))
                        drawPath(path, Color(0xFFFFD54F), style = Stroke(width = 4.dp.toPx(), pathEffect = dashEffect, cap = StrokeCap.Round))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(start = startPad, end = endPad)
                            .border(if (isSelected) 3.dp else 1.dp, borderColor, MapPinShape)
                            .clickable { onSelectCard(index) },
                        shape = MapPinShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8DC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 26.dp, bottom = 36.dp, start = 12.dp, end = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = if (isSelected) "🎯 ${note.title}" else "📍 ${note.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2E8B57),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderMountainLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(notes) { index, note ->
            val total = notes.size
            val progress = if (total <= 1) 0f else index.toFloat() / (total - 1).toFloat()
            val horizontalPadding = ((1f - progress) * 44f).dp
            val isSelected = index == selectedIndex
            val brush = if (isSelected) Brush.linearGradient(colors = listOf(Color(0xFFE55D87), Color(0xFF5FC3E4))) else Brush.linearGradient(colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .border(if (isSelected) 3.dp else 0.dp, Color(0xFFFFD700), MountainPeakShape)
                    .clickable { onSelectCard(index) },
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 6.dp),
                shape = MountainPeakShape
            ) {
                Box(modifier = Modifier.background(brush).padding(top = 28.dp, bottom = 14.dp, start = 14.dp, end = 14.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                    }
                }
            }
        }
    }
}

/** Winding River Layout - Winding blue river background with river curve and waves as note shape, structured like the map */
@Composable
private fun RenderRiverLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2F1)) // Scenic river valley terrain background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(notes) { index, note ->
                val isSelected = index == selectedIndex
                val isLeft = index % 2 == 0
                val startPad = if (isLeft) 16.dp else 90.dp
                val endPad = if (isLeft) 90.dp else 16.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(vertical = 6.dp)
                ) {
                    // Winding river canvas path curving and waving across note cards
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val riverWidth = 52.dp.toPx()
                        val rippleEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f), 0f)
                        val startX = if (index % 2 == 0) w * 0.75f else w * 0.25f
                        val endX = if (index % 2 == 0) w * 0.25f else w * 0.75f
                        val path = Path().apply {
                            moveTo(startX, 0f)
                            cubicTo(startX, h * 0.5f, endX, h * 0.5f, endX, h)
                        }
                        drawPath(path, Color(0xFF80DEEA), style = Stroke(width = riverWidth + 14.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(path, Color(0xFF0288D1), style = Stroke(width = riverWidth, cap = StrokeCap.Round))
                        drawPath(path, Color(0xFFE0F7FA), style = Stroke(width = 4.dp.toPx(), pathEffect = rippleEffect, cap = StrokeCap.Round))
                    }

                    val riverGradient = if (isSelected) Brush.verticalGradient(colors = listOf(Color(0xFFFF512F), Color(0xFFDD2476))) else Brush.verticalGradient(colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(start = startPad, end = endPad)
                            .border(if (isSelected) 3.dp else 1.dp, Color.White, RiverStreamShape)
                            .clickable { onSelectCard(index) },
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 6.dp),
                        shape = RiverStreamShape
                    ) {
                        Box(modifier = Modifier.background(riverGradient).padding(top = 22.dp, bottom = 22.dp, start = 14.dp, end = 14.dp)) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isSelected) "🎯 ${note.title}" else "🌊 ${note.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                ActionRow(isSelected, { onSelectCard(index) }, { onEditNote(note) }, { onViewNote(note) }, { onDeleteNote(note) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Rocky Cliff Waterfall Layout - Dark rocky cliff with cascading streams of water and water drop notes coming down */
@Composable
private fun RenderWaterfallLayout(
    notes: List<Note>,
    selectedIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onEditNote: (Note) -> Unit,
    onViewNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF263238)) // Dark rocky cliff background
    ) {
        // Rocky cliff texture and cascading streams of water coming down
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Draw rocky cliff strata lines
            for (i in 0..16) {
                val y = (h / 16f) * i
                drawLine(
                    color = Color(0xFF37474F),
                    start = Offset(0f, y),
                    end = Offset(w, y + 16f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            // Cascading streams of water coming down the cliff
            val streamWidth = 10.dp.toPx()
            val splashDash = PathEffect.dashPathEffect(floatArrayOf(28f, 18f), 0f)
            val streamPositions = listOf(0.15f, 0.35f, 0.5f, 0.7f, 0.85f)
            for (pos in streamPositions) {
                val x = w * pos
                val path = Path().apply {
                    moveTo(x, 0f)
                    cubicTo(x - 10f, h * 0.3f, x + 10f, h * 0.7f, x, h)
                }
                drawPath(path, Color(0x6680DEEA), style = Stroke(width = streamWidth, cap = StrokeCap.Round))
                drawPath(path, Color(0xCCFFFFFF), style = Stroke(width = 3.dp.toPx(), pathEffect = splashDash, cap = StrokeCap.Round))
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 14.dp,
            contentPadding = PaddingValues(10.dp)
        ) {
            staggeredItemsIndexed(notes) { index, note ->
                val isSelected = index == selectedIndex
                val waterfallBrush = if (isSelected) Brush.verticalGradient(colors = listOf(Color(0xFFF857A6), Color(0xFFFF5858))) else Brush.verticalGradient(colors = listOf(Color(0xFF00E5FF), Color(0xFF0083B0)))
                val isEvenColumn = index % 2 == 0
                val topMargin = if (isEvenColumn) 0.dp else 32.dp
                val minHeight = if (index % 2 == 0) 190.dp else 230.dp

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(minHeight)
                        .padding(top = topMargin)
                        .border(if (isSelected) 3.dp else 1.dp, Color(0xFFE0F7FA), WaterfallDropShape)
                        .clickable { onSelectCard(index) },
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 6.dp),
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
}

// -------------------------------------------------------------------------------------
// SHARED UI HELPER COMPONENTS
// -------------------------------------------------------------------------------------

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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = noteTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isSelected) {
                    Text(
                        text = "🎯 Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActionRow(isSelected, onCardClick, onEdit, onView, onDelete)
        }
    }
}

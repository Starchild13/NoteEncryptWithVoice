package com.example.noteencrypt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TipsScreen displays a dedicated guide and best practices for writing effective,
 * secure, and organized notes using NoteEncrypt.
 *
 * @param onNavigateBack Callback invoked to pop backstack and return to the main dashboard.
 */
@Composable
fun TipsScreen(
    onNavigateBack: () -> Unit
) {
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with back navigation button & screen title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("← Back")
                    }
                    Column {
                        Text(
                            text = "💡 Note-Taking Guide",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Best practices for privacy, dictation & layout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Section 1: Note Organization & Structure
                TipCategoryCard(
                    title = "✍️ Write Clear & Concise Notes",
                    badgeText = "Organization",
                    tips = listOf(
                        TipDetail("Keep it Focused", "Capture core takeaways, key action items, and bullet points quickly."),
                        TipDetail("Descriptive Titles", "Name notes clearly to enable instant retrieval and quick recognition."),
                        TipDetail("Actionable Content", "Structure thoughts with clear next steps or reference points.")
                    )
                )

                // Section 2: Security & Encryption Mechanisms
                TipCategoryCard(
                    title = "🔒 Security & AES Encryption",
                    badgeText = "Privacy",
                    tips = listOf(
                        TipDetail("Protect Sensitive Data", "Store passwords, journals, and private thoughts with confidence."),
                        TipDetail("Hardware-Backed Keys", "AES keys are securely generated and isolated in Android KeyStore."),
                        TipDetail("Zero-Knowledge Memory", "Decryption occurs only in memory while actively viewing a note.")
                    )
                )

                // Section 3: Voice Memos & Dictation
                TipCategoryCard(
                    title = "🎙️ Voice Memos & Dictation",
                    badgeText = "Audio",
                    tips = listOf(
                        TipDetail("Hands-Free Recording", "Capture spontaneous ideas on-the-go without needing to type."),
                        TipDetail("Automatic Speech-to-Text", "Voice audio is transcribed directly into readable note text."),
                        TipDetail("Integrated Creation", "Insert speech transcriptions right into your note form or save direct.")
                    )
                )

                // Section 4: Thematic Visual Layouts & Custom Card Reordering
                TipCategoryCard(
                    title = "🎨 Thematic Layouts & Custom Ordering",
                    badgeText = "Layouts",
                    tips = listOf(
                        TipDetail("6 Visual Themes", "Switch effortlessly between List, Grid, Map, Mountain, River, and Waterfall views."),
                        TipDetail("Interactive Card Reordering", "Tap any note card to select it, then tap a target slot to move it."),
                        TipDetail("Custom Card Geometries", "Each layout theme renders cards with unique shapes and canvas paths.")
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Button returning to main dashboard
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Secure Notes 🔒", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Internal model representing a single tip item within a category card.
 */
private data class TipDetail(val headline: String, val description: String)

/**
 * Reusable card composable for displaying a category of tips with a title and badge chip.
 */
@Composable
private fun TipCategoryCard(
    title: String,
    badgeText: String,
    tips: List<TipDetail>
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                AssistChip(
                    onClick = {},
                    label = { Text(badgeText, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            tips.forEach { tip ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "• ${tip.headline}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}


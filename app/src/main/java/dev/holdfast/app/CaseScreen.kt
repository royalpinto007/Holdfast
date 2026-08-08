package dev.holdfast.app

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

/**
 * One case: the seal state, then the entries in the order they were sealed.
 *
 * The hero is the verdict, at display size, because it is the only question
 * anybody opens this screen to answer.
 */
@Composable
fun CaseScreen(
    vault: Vault,
    case: Case,
    onBack: () -> Unit,
    onChanged: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var current by remember(case.id, case.entries.size) { mutableStateOf(case) }
    var pendingPhoto by remember { mutableStateOf<File?>(null) }
    var noteFor by remember { mutableStateOf<File?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val verdict = remember(current) { verify(current, vault.photoHashes(current)) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val target = pendingPhoto
        if (ok && target != null && target.exists()) {
            noteFor = target
        } else {
            target?.delete()
            pendingPhoto = null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = Space.lg),
            verticalArrangement = Arrangement.spacedBy(Space.md),
            // Clears the sealing button, which floats over the list.
            contentPadding = PaddingValues(top = Space.sm, bottom = 148.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        val f = vault.writeExport(current)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", f)
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(send, "Send record"))
                    }) {
                        Icon(Icons.Rounded.IosShare, "Export",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.Delete, "Delete record",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { VerdictHero(case = current, verdict = verdict) }

            item {
                Text(
                    "SEALED ENTRIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.sm, start = Space.xs),
                )
            }

            if (current.entries.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(Corner.card),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Nothing sealed yet. The first photo you take starts the chain.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Space.xl),
                        )
                    }
                }
            }

            itemsIndexed(current.entries, key = { _, e -> e.id }) { index, entry ->
                EntryTile(
                    index = index,
                    entry = entry,
                    photo = entry.photoFile?.let { vault.photoFile(current.id, it) },
                    broken = (verdict as? Verdict.Broken)?.index == index,
                )
            }
        }

        Button(
            onClick = {
                val target = vault.newPhotoTarget(current.id, System.currentTimeMillis())
                pendingPhoto = target
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", target)
                camera.launch(uri)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(Space.xl)
                .heightIn(min = 58.dp),
            shape = RoundedCornerShape(Corner.chip),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Rounded.Add, null, Modifier.size(20.dp))
            Spacer(Modifier.width(Space.sm))
            Text("Seal a photo", style = MaterialTheme.typography.labelLarge)
        }
    }

    noteFor?.let { photo ->
        NoteDialog(
            onDismiss = {
                photo.delete()
                noteFor = null
                pendingPhoto = null
            },
            onSeal = { note, place ->
                current = vault.append(current, note, photo, place, System.currentTimeMillis())
                noteFor = null
                pendingPhoto = null
                onChanged()
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = RoundedCornerShape(Corner.card),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete this record?") },
            text = {
                Text(
                    "The photos and the chain go with it. Nothing is kept anywhere else, " +
                        "so this cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep") } },
        )
    }
}

@Composable
private fun VerdictHero(case: Case, verdict: Verdict) {
    val dark = MaterialTheme.colorScheme.background.let { it.red + it.green + it.blue < 1.2f }
    val broken = verdict is Verdict.Broken
    val tint = when {
        verdict is Verdict.Empty -> MaterialTheme.colorScheme.onSurfaceVariant
        broken -> if (dark) BreakRed else BreakRedDark
        else -> if (dark) SealGreen else SealGreenDark
    }

    Surface(
        shape = RoundedCornerShape(Corner.hero),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Space.xl), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            Text(
                case.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                when (verdict) {
                    is Verdict.Empty -> "Not started"
                    is Verdict.Broken -> "Chain broken"
                    Verdict.Intact -> "Chain intact"
                },
                style = MaterialTheme.typography.displaySmall,
                color = tint,
            )
            Text(
                when (verdict) {
                    is Verdict.Empty ->
                        "Seal the first photo and the chain begins."
                    is Verdict.Broken ->
                        "Entry ${verdict.index + 1} ${verdict.reason}. Everything after it is " +
                            "no longer covered by the seal."
                    Verdict.Intact ->
                        "All ${case.entries.size} entries still hash to what was sealed, in the " +
                            "order they were taken."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                MiniStat("ENTRIES", case.entries.size.toString(), Modifier.weight(1f))
                MiniStat(
                    "SPAN",
                    "${Stamp.spanDays(case.openedAt, case.entries.lastOrNull()?.at ?: case.openedAt)}d",
                    Modifier.weight(1f),
                )
                MiniStat("HEAD", shortHash(case.head), Modifier.weight(1.8f), mono = true)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier, mono: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Corner.tile),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(Space.md)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Space.xs))
            Text(
                value,
                style = if (mono) Mono else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EntryTile(index: Int, entry: Entry, photo: File?, broken: Boolean) {
    val dark = MaterialTheme.colorScheme.background.let { it.red + it.green + it.blue < 1.2f }
    val bitmap = remember(photo?.path) {
        photo?.takeIf { it.exists() }?.let { f ->
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeFile(f.path, opts)
            }.getOrNull()
        }
    }

    Surface(
        shape = RoundedCornerShape(Corner.card),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = entry.note.ifBlank { "Sealed photo" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(topStart = Corner.card, topEnd = Corner.card)),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(Space.xl), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(Space.md))
                    Text(
                        Stamp.full(entry.at),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = if (broken) Icons.Rounded.Warning else Icons.Rounded.Check,
                        contentDescription = if (broken) "Broken" else "Sealed",
                        tint = if (broken) {
                            if (dark) BreakRed else BreakRedDark
                        } else {
                            if (dark) SealGreen else SealGreenDark
                        },
                        modifier = Modifier.size(17.dp),
                    )
                }
                if (entry.note.isNotBlank()) {
                    Text(
                        entry.note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                entry.place?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // The hash is shown, always. A seal nobody can read is a logo.
                Text(
                    "seal ${shortHash(entry.hash)}  ←  ${shortHash(entry.prev)}",
                    style = Mono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NoteDialog(onDismiss: () -> Unit, onSeal: (String, String?) -> Unit) {
    var note by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Corner.card),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Seal this photo", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md), modifier = Modifier.imePadding()) {
                Text(
                    "Once sealed, the note and the time cannot be changed without it showing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Line(note, { note = it }, "What this shows", "Damp patch, bedroom ceiling")
                Line(place, { place = it }, "Where (optional)", "Flat 3B, Kochi")
            }
        },
        confirmButton = { TextButton(onClick = { onSeal(note, place) }) { Text("Seal") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Discard") } },
    )
}

@Composable
private fun Line(value: String, onChange: (String) -> Unit, label: String, hint: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(hint) },
        shape = RoundedCornerShape(Corner.tile),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
        ),
    )
}

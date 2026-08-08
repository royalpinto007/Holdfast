package dev.holdfast.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * What this app is, for somebody who has just opened it.
 *
 * The first version of this screen was three paragraphs in an empty state, and
 * it did not work: people read "sealed to the one before it" and still had no
 * idea what the app was for. Describing a hash chain in prose asks the reader
 * to take the claim on trust, which is the opposite of the point.
 *
 * So the explanation ends in a chain they can break themselves. The sample
 * below is three real sealed entries, and the buttons perform the actual
 * tampering: reword one, delete one, swap two. The verdict underneath comes
 * from the same verify() that runs on a real record. Nothing here is a mockup,
 * which is why it is convincing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntroScreen(onDone: () -> Unit, firstRun: Boolean) {
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
            contentPadding = PaddingValues(top = Space.xl, bottom = BottomActionInset),
        ) {
            item {
                Column(Modifier.padding(bottom = Space.sm)) {
                    Text(
                        "HOLDFAST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        "A photo proves what. It does not prove when.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Space.md))
                    Text(
                        "That gap is why deposits get kept and damaged deliveries get argued " +
                            "about. A date in a photo is just text, and anybody can type it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Numbered because this genuinely is a sequence: you do these in order.
            item { Step(1, "Photograph it", "A room at move-in, a parcel that arrived dented, work you finished today.") }
            item { Step(2, "Seal it", "The photo, your note and the time are locked to the entry before it.") }
            item { Step(3, "Hand it over", "Export a text file anybody can check, without installing this app.") }

            item { Spacer(Modifier.height(Space.sm)) }
            item { TamperDemo() }

            item {
                Text(
                    "Holdfast does not make anything court-admissible, and does not certify your " +
                        "clock against an outside authority. It makes tampering show. That is a " +
                        "smaller promise, and it is one the app keeps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.sm),
                )
            }
        }

        BottomAction(
            label = if (firstRun) "Start a record" else "Back",
            onClick = onDone,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun Step(number: Int, title: String, body: String) {
    Surface(
        shape = RoundedCornerShape(Corner.card),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Space.xl)) {
            Text(
                "$number",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp),
            )
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ── The part that does the explaining ─────────────────────────────────────── */

/** A three-entry sample chain, sealed the same way a real record is. */
private fun sampleChain(): List<Entry> {
    val start = 1_754_640_000_000L
    val notes = listOf(
        "Hallway wall, scuff above the skirting",
        "Bathroom ceiling, damp patch",
        "Kitchen worktop, burn mark",
    )
    var prev = GENESIS
    return notes.mapIndexed { i, note ->
        sealEntry(
            id = "demo$i",
            at = start + i * 600_000L,
            note = note,
            photoHash = null,
            photoFile = null,
            place = null,
            prev = prev,
        ).also { prev = it.hash }
    }
}

private enum class Tamper(val label: String) {
    None("Untouched"),
    Reword("Reword entry 2"),
    Remove("Delete entry 2"),
    Swap("Swap 2 and 3"),
}

private fun Tamper.apply(entries: List<Entry>): List<Entry> = when (this) {
    Tamper.None -> entries
    // The note changes but the hash it was sealed with does not, which is
    // exactly what editing a stored record looks like.
    Tamper.Reword -> entries.toMutableList().also {
        it[1] = it[1].copy(note = "Bathroom ceiling, no damage")
    }
    Tamper.Remove -> listOf(entries[0], entries[2])
    Tamper.Swap -> listOf(entries[0], entries[2], entries[1])
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TamperDemo() {
    val original = remember { sampleChain() }
    var tamper by remember { mutableStateOf(Tamper.None) }
    val entries = remember(tamper) { tamper.apply(original) }

    // The real verifier, on the tampered list. No shortcuts, or the demo would
    // be making a claim the product does not.
    val verdict = remember(entries) {
        verify(Case("demo", "Sample", "", original.first().at, entries))
    }
    val broken = verdict as? Verdict.Broken

    val dark = MaterialTheme.colorScheme.background.let { it.red + it.green + it.blue < 1.2f }
    val tint by animateColorAsState(
        if (broken != null) {
            if (dark) BreakRed else BreakRedDark
        } else {
            if (dark) SealGreen else SealGreenDark
        },
        label = "verdict",
    )

    Surface(
        shape = RoundedCornerShape(Corner.hero),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Space.xl), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            Column {
                Text(
                    "TRY BREAKING IT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Space.sm))
                Text(
                    "Three sealed entries. Change one and see what happens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                entries.forEachIndexed { i, entry ->
                    DemoEntry(i, entry, broken?.index == i)
                }
            }

            Column {
                Text(
                    if (broken != null) "Seal broken" else "Seal intact",
                    style = MaterialTheme.typography.headlineSmall,
                    color = tint,
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    if (broken != null) {
                        "Entry ${broken.index + 1} ${broken.reason}."
                    } else {
                        "Every entry still hashes to what was sealed."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                Tamper.entries.forEach { option ->
                    TamperChip(
                        label = if (option == Tamper.None) "Reset" else option.label,
                        selected = option == tamper,
                        onClick = { tamper = option },
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoEntry(index: Int, entry: Entry, broken: Boolean) {
    val dark = MaterialTheme.colorScheme.background.let { it.red + it.green + it.blue < 1.2f }
    val tint = if (broken) {
        if (dark) BreakRed else BreakRedDark
    } else {
        if (dark) SealGreen else SealGreenDark
    }
    Surface(
        shape = RoundedCornerShape(Corner.tile),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = Space.md, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(18.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    shortHash(entry.hash),
                    style = Mono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(Space.sm))
            Icon(
                imageVector = if (broken) Icons.Rounded.Warning else Icons.Rounded.Check,
                contentDescription = if (broken) "Broken here" else "Sealed",
                tint = tint,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun TamperChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Corner.chip),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        TextButton(onClick = onClick) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

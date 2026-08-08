package dev.holdfast.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HoldfastTheme { HoldfastApp() } }
    }
}

private sealed interface Screen {
    data object Intro : Screen
    data object Cases : Screen
    data class Detail(val caseId: String) : Screen
}

@Composable
private fun HoldfastApp() {
    val context = LocalContext.current
    val vault = remember { Vault(context) }
    // Whether the explainer has been seen. Local preferences, like everything
    // else here: there is no account to hang it off and nowhere to sync it to.
    val prefs = remember {
        context.getSharedPreferences("holdfast", android.content.Context.MODE_PRIVATE)
    }
    var explained by remember { mutableStateOf(prefs.getBoolean("explained", false)) }
    var screen by remember {
        mutableStateOf<Screen>(if (explained) Screen.Cases else Screen.Intro)
    }
    var cases by remember { mutableStateOf(vault.list()) }

    // Navigation here is a single `screen` value rather than a nav library, and
    // nothing was listening for the system back gesture, so back closed the app
    // from inside a record instead of returning to the list. It is handled per
    // screen, and deliberately not handled on Cases, where leaving the app is
    // what back should do.
    BackHandler(enabled = screen != Screen.Cases) {
        if (screen is Screen.Detail) cases = vault.list()
        screen = Screen.Cases
    }

    when (val s = screen) {
        Screen.Intro -> IntroScreen(
            firstRun = !explained,
            onDone = {
                prefs.edit().putBoolean("explained", true).apply()
                explained = true
                screen = Screen.Cases
            },
        )

        Screen.Cases -> CasesScreen(
            cases = cases,
            onOpen = { screen = Screen.Detail(it.id) },
            onExplain = { screen = Screen.Intro },
            onCreate = { title, what ->
                vault.create(title, what, System.currentTimeMillis())
                cases = vault.list()
            },
        )

        is Screen.Detail -> {
            val case = cases.firstOrNull { it.id == s.caseId }
            if (case == null) {
                screen = Screen.Cases
            } else {
                CaseScreen(
                    vault = vault,
                    case = case,
                    onBack = { screen = Screen.Cases; cases = vault.list() },
                    onChanged = { cases = vault.list() },
                    onDelete = {
                        vault.delete(case.id)
                        cases = vault.list()
                        screen = Screen.Cases
                    },
                )
            }
        }
    }
}

/* ── Cases: the bento home ─────────────────────────────────────────────────── */

@Composable
private fun CasesScreen(
    cases: List<Case>,
    onOpen: (Case) -> Unit,
    onExplain: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var creating by remember { mutableStateOf(false) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("HOLDFAST", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        // The explainer stays reachable. Somebody who skipped it
                        // on day one is exactly the person who needs it later.
                        TextButton(onClick = onExplain) {
                            Text(
                                "How this works",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        "A record that cannot be quietly changed.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Space.md))
                    Text(
                        "Photos sealed in order, so nothing can be reworded, removed or " +
                            "backdated later without it showing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (cases.isEmpty()) {
                item { EmptyState() }
            } else {
                items(cases, key = { it.id }) { case ->
                    CaseTile(case = case, onClick = { onOpen(case) })
                }
            }
        }

        BottomAction(
            label = "Start a record",
            icon = Icons.Rounded.Add,
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (creating) {
        NewCaseDialog(
            onDismiss = { creating = false },
            onCreate = { t, w -> onCreate(t, w); creating = false },
        )
    }
}

@Composable
private fun EmptyState() {
    Surface(
        shape = RoundedCornerShape(Corner.card),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Space.xl), verticalArrangement = Arrangement.spacedBy(Space.md)) {
            Text(
                "Nothing on record yet.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            // The explainer carries the argument now, so this says the one thing
            // it cannot: start before you need it, because afterwards is too late.
            Text(
                "Start one before you need it. A move-in walkthrough, a delivery that arrived " +
                    "damaged, work you finished on a date somebody may later dispute.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One case, as a bento tile.
 *
 * Area carries importance: the seal state gets the whole left column because it
 * is the only thing that matters at a glance, and the counts are small.
 */
@Composable
private fun CaseTile(case: Case, onClick: () -> Unit) {
    val intact = verify(case) !is Verdict.Broken
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Corner.card))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Corner.card),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(Space.xl), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        case.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        case.what.ifBlank { "No description" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SealPill(intact = intact, sealed = case.entries.isNotEmpty())
            }

            StatRow(
                listOf(
                    Stat("ENTRIES", case.entries.size.toString()),
                    Stat("OPENED", Stamp.day(case.openedAt).substringBeforeLast(' ')),
                    // "Head" is the right word for the end of a chain and the
                    // export still uses it, but nobody meets this app knowing
                    // that. On screen it is the seal.
                    Stat("SEAL", shortHash(case.head, head = 4, tail = 4), mono = true),
                ),
            )
        }
    }
}

@Composable
private fun SealPill(intact: Boolean, sealed: Boolean) {
    val dark = MaterialTheme.colorScheme.background.let { it.red + it.green + it.blue < 1.2f }
    val tint = when {
        !sealed -> MaterialTheme.colorScheme.onSurfaceVariant
        intact -> if (dark) SealGreen else SealGreenDark
        else -> if (dark) BreakRed else BreakRedDark
    }
    Surface(
        shape = RoundedCornerShape(Corner.chip),
        color = tint.copy(alpha = 0.14f),
    ) {
        Row(
            Modifier.padding(horizontal = Space.md, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = when {
                    !sealed -> Icons.Rounded.Lock
                    intact -> Icons.Rounded.Check
                    else -> Icons.Rounded.Warning
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = when {
                    !sealed -> "Empty"
                    intact -> "Intact"
                    else -> "Broken"
                },
                style = MaterialTheme.typography.labelLarge,
                color = tint,
            )
        }
    }
}

@Composable
private fun NewCaseDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var what by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Corner.card),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("New record", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md), modifier = Modifier.imePadding()) {
                Field(title, { title = it }, "What is it about", "Flat 3B, move-in")
                Field(what, { what = it }, "Why you are keeping it", "Deposit dispute, condition at handover")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onCreate(title, what) },
                enabled = title.isNotBlank(),
            ) { Text("Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, hint: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(hint) },
        singleLine = true,
        shape = RoundedCornerShape(Corner.tile),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
        ),
    )
}

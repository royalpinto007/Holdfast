package dev.holdfast.app

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * How large the sealed photos are drawn.
 *
 * A record of twenty entries at full size is a long scroll when all you want is
 * to find the one from the kitchen. Small turns the timeline into a list you
 * can skim; large is for actually looking at the damage.
 *
 * What does not change with size is the seal line and the verdict tick. They
 * are the reason the record exists, so shrinking the photo must never be a way
 * of accidentally hiding whether the entry still checks out.
 */
enum class PhotoSize(val label: String, val description: String) {
    Small("S", "Small photos, one line each"),
    Medium("M", "Medium photos"),
    Large("L", "Large photos"),
    ;

    companion object {
        private const val KEY = "photoSize"

        /** Large by default: a first-time reader should see the evidence, not a list. */
        fun load(context: Context): PhotoSize {
            val name = context
                .getSharedPreferences("holdfast", Context.MODE_PRIVATE)
                .getString(KEY, Large.name)
            return entries.firstOrNull { it.name == name } ?: Large
        }

        fun save(context: Context, size: PhotoSize) {
            context
                .getSharedPreferences("holdfast", Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, size.name)
                .apply()
        }
    }
}

/**
 * Three letters, not a menu.
 *
 * A dropdown would hide a choice people want to flip back and forth while
 * scanning a record, and an icon triple would need explaining. S, M and L are
 * already understood and cost one tap.
 */
@Composable
fun PhotoSizePicker(
    selected: PhotoSize,
    onSelect: (PhotoSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Corner.chip),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(Modifier.padding(3.dp)) {
            PhotoSize.entries.forEach { size ->
                val active = size == selected
                Surface(
                    shape = RoundedCornerShape(Corner.chip),
                    color = if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    onClick = { onSelect(size) },
                    modifier = Modifier.semantics { contentDescription = size.description },
                ) {
                    Text(
                        size.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(
                            horizontal = Space.md,
                            vertical = Space.sm,
                        ),
                    )
                }
            }
        }
    }
}

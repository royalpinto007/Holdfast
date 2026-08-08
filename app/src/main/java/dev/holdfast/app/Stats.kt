package dev.holdfast.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The small stat tiles that sit under a record's title.
 *
 * There used to be two of these, one per screen, and they drifted: different
 * column weights on each screen and a hash value set two points smaller than
 * the values beside it. The result was a row of three boxes with three widths,
 * three heights and three baselines, which is what a row of tiles must never
 * look like. One implementation now, used by both screens.
 *
 * Equal widths and a fixed height are the whole trick. The values are pinned to
 * the bottom of the tile so the label sits on one line across the row and every
 * value sits on another, whatever the value happens to say.
 */

data class Stat(val label: String, val value: String, val mono: Boolean = false)

/** Tall enough for an eyebrow, a gap and one line of value, and no taller. */
private val TileHeight = 74.dp

@Composable
fun StatRow(stats: List<Stat>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        stats.forEach { stat ->
            StatTile(stat, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(stat: Stat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(TileHeight),
        shape = RoundedCornerShape(Corner.tile),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.md),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stat.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stat.value,
                // MonoStat carries the same line height as titleMedium, so a
                // hash and a date land on the same baseline.
                style = if (stat.mono) MonoStat else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

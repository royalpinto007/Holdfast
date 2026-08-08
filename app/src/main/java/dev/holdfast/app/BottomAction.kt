package dev.holdfast.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The one action at the bottom of a screen.
 *
 * This was a floating pill over the list, and it sat on top of whatever text
 * happened to scroll under it: a note half covered by a white button reads as a
 * broken layout, not as a button that floats. Padding the end of the list only
 * fixed the last item, because the button floats over the middle of the list
 * too.
 *
 * So it is a bar, not a floating button. The bar is opaque, and the list is
 * padded to stop above it, so text is never partly covered. The gradient above
 * it lets content fade out rather than being sliced by a hard edge, which is
 * what a border would do and this design does not use borders.
 */

/** What the list underneath must reserve, so nothing ends up behind the bar. */
val BottomActionInset: Dp = 132.dp

@Composable
fun BottomAction(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ground = MaterialTheme.colorScheme.background
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, ground))),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(ground)
                .navigationBarsPadding()
                .padding(bottom = Space.lg),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.heightIn(min = 58.dp),
                shape = RoundedCornerShape(Corner.chip),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                icon?.let {
                    Icon(it, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(Space.sm))
                }
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

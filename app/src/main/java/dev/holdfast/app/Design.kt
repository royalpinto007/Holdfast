package dev.holdfast.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Holdfast, visually.
 *
 * The subject is a record that has not been touched, so the design is built on
 * two ideas and nothing else.
 *
 * Depth as hierarchy. Layers sit on each other in a fixed order and shadow only
 * appears where something is genuinely raised. There are no decorative borders,
 * because an outline on every box is what makes an app look a decade old.
 *
 * A bento layout. Asymmetric tiles of different sizes, so importance is carried
 * by area rather than by colour. That is the current direction for data-dense
 * mobile surfaces, and it suits this app: one enormous tile for the chain's
 * state, small ones for the counts.
 *
 * The palette is near-monochrome slate with a single signal green for "intact"
 * and a single amber-red for "broken". Those are the only two states that exist,
 * and they are the only two colours that appear.
 */

private val Ink = Color(0xFF0C0D10)
private val InkRaised = Color(0xFF15171C)
private val InkTile = Color(0xFF1C1F26)
private val InkTileHigh = Color(0xFF262A33)
private val Chalk = Color(0xFFF4F5F7)
private val ChalkDim = Color(0xFF9BA1AE)
private val Line = Color(0xFF2E333D)

private val Paper = Color(0xFFF7F7F8)
private val PaperRaised = Color(0xFFFFFFFF)
private val PaperTile = Color(0xFFEDEEF1)
private val PaperTileHigh = Color(0xFFE2E4E9)
private val Slate = Color(0xFF15171C)
private val SlateDim = Color(0xFF5F6673)
private val LineLight = Color(0xFFDFE1E6)

/** Intact, and broken. The two states the whole product is about. */
val SealGreen = Color(0xFF2FBF71)
val SealGreenDark = Color(0xFF1B7F4C)
val BreakRed = Color(0xFFE05260)
val BreakRedDark = Color(0xFFA12634)

private val DarkScheme = darkColorScheme(
    primary = Chalk,
    onPrimary = Ink,
    primaryContainer = InkTileHigh,
    onPrimaryContainer = Chalk,
    secondary = ChalkDim,
    onSecondary = Ink,
    background = Ink,
    onBackground = Chalk,
    surface = InkRaised,
    onSurface = Chalk,
    surfaceVariant = InkTile,
    onSurfaceVariant = ChalkDim,
    surfaceContainer = InkTile,
    surfaceContainerHigh = InkTileHigh,
    surfaceContainerHighest = Color(0xFF2F343E),
    surfaceContainerLow = Color(0xFF111318),
    outline = Line,
    outlineVariant = Color(0xFF23272F),
    error = BreakRed,
    onError = Color(0xFF3A0A11),
)

private val LightScheme = lightColorScheme(
    primary = Slate,
    onPrimary = Paper,
    primaryContainer = PaperTileHigh,
    onPrimaryContainer = Slate,
    secondary = SlateDim,
    onSecondary = Paper,
    background = Paper,
    onBackground = Slate,
    surface = PaperRaised,
    onSurface = Slate,
    surfaceVariant = PaperTile,
    onSurfaceVariant = SlateDim,
    surfaceContainer = PaperTile,
    surfaceContainerHigh = PaperTileHigh,
    surfaceContainerHighest = Color(0xFFD7DAE0),
    surfaceContainerLow = Color(0xFFF2F3F5),
    outline = LineLight,
    outlineVariant = Color(0xFFE9EAEE),
    error = BreakRedDark,
    onError = Paper,
)

object Space {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 22.dp
    val xxl: Dp = 32.dp
}

object Corner {
    val chip: Dp = 999.dp
    val tile: Dp = 24.dp
    val card: Dp = 30.dp
    val hero: Dp = 36.dp
}

private val HoldfastShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(Corner.tile),
    large = RoundedCornerShape(Corner.card),
    extraLarge = RoundedCornerShape(Corner.hero),
)

private val base = Typography()

/*
 * One heavy display size, then a fast drop to medium weights. The tightest
 * tracking is on the largest size, which is what makes big type read as
 * designed rather than merely large.
 */
val HoldfastType = base.copy(
    displayLarge = base.displayLarge.copy(
        fontSize = 60.sp, lineHeight = 60.sp, fontWeight = FontWeight.Bold,
        letterSpacing = (-2.4).sp, fontFamily = FontFamily.SansSerif,
    ),
    displayMedium = base.displayMedium.copy(
        fontSize = 42.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold,
        letterSpacing = (-1.6).sp, fontFamily = FontFamily.SansSerif,
    ),
    displaySmall = base.displaySmall.copy(
        fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.8).sp, fontFamily = FontFamily.SansSerif,
    ),
    headlineMedium = base.headlineMedium.copy(
        fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp, fontFamily = FontFamily.SansSerif,
    ),
    headlineSmall = base.headlineSmall.copy(
        fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp, fontFamily = FontFamily.SansSerif,
    ),
    titleLarge = base.titleLarge.copy(
        fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp, fontFamily = FontFamily.SansSerif,
    ),
    titleMedium = base.titleMedium.copy(
        fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp, fontFamily = FontFamily.SansSerif,
    ),
    bodyLarge = base.bodyLarge.copy(
        fontSize = 16.sp, lineHeight = 24.sp, fontFamily = FontFamily.SansSerif,
    ),
    bodyMedium = base.bodyMedium.copy(
        fontSize = 14.sp, lineHeight = 21.sp, fontFamily = FontFamily.SansSerif,
    ),
    labelLarge = base.labelLarge.copy(
        fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif,
    ),
    // Eyebrows. The only caps and the only tracking in the app.
    labelSmall = base.labelSmall.copy(
        fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp, fontFamily = FontFamily.SansSerif,
    ),
)

/** Hashes are compared by eye, so they are always monospaced. */
val Mono = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.sp,
)

@Composable
fun HoldfastTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = HoldfastType,
        shapes = HoldfastShapes,
        content = content,
    )
}

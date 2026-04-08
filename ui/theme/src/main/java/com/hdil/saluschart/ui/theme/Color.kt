package com.hdil.saluschart.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

object ChartColor {
    val Default = Color(0xFF3F51B5)
}

/**
 * Defines the color scheme applied to SalusChart composables.
 *
 * Pass a [SalusChartColorScheme] to [SalusChartTheme] to theme all charts within that scope,
 * or override individual color parameters on each chart composable for finer control.
 *
 * @param primary The primary accent color used for single-color chart elements (bars, lines, points).
 * @param secondary A secondary/neutral color used for contrast elements (e.g. "bad" range bars).
 * @param palette Ordered list of colors assigned to multi-color charts (stacked bars, pie slices, etc.).
 *   Charts cycle through this list when there are more data series than colors.
 */
data class SalusChartColorScheme(
    val primary: Color,
    val secondary: Color,
    val palette: List<Color>
) {
    companion object {
        /** Vibrant multi-hue palette covering the full color spectrum. */
        val Default = SalusChartColorScheme(
            primary = Color(0xFF3F51B5),
            secondary = Color(0xFFD6D6D6),
            palette = listOf(
                Color(0xFF3F51B5), // indigo
                Color(0xFFE91E63), // pink
                Color(0xFF4CAF50), // green
                Color(0xFFFF9800), // orange
                Color(0xFF9C27B0), // purple
                Color(0xFF00BCD4), // cyan
            )
        )

        /** Deep-sea blues and teals. */
        val Ocean = SalusChartColorScheme(
            primary = Color(0xFF006994),
            secondary = Color(0xFFB0D4E8),
            palette = listOf(
                Color(0xFF006994),
                Color(0xFF0099CC),
                Color(0xFF00BFFF),
                Color(0xFF48D1CC),
                Color(0xFF20B2AA),
                Color(0xFF008B8B),
            )
        )

        /** Warm oranges, reds, and yellows. */
        val Sunset = SalusChartColorScheme(
            primary = Color(0xFFFF6B35),
            secondary = Color(0xFFFFD3B5),
            palette = listOf(
                Color(0xFFFF6B35),
                Color(0xFFFF8C42),
                Color(0xFFFFAD00),
                Color(0xFFFF4D6D),
                Color(0xFFC9184A),
                Color(0xFFFFD60A),
            )
        )

        /** Nature-inspired greens. */
        val Forest = SalusChartColorScheme(
            primary = Color(0xFF2D6A4F),
            secondary = Color(0xFFB7E4C7),
            palette = listOf(
                Color(0xFF1B4332),
                Color(0xFF2D6A4F),
                Color(0xFF40916C),
                Color(0xFF52B788),
                Color(0xFF74C69D),
                Color(0xFF95D5B2),
            )
        )

        /** Soft purples and violets. */
        val Lavender = SalusChartColorScheme(
            primary = Color(0xFF7B2D8B),
            secondary = Color(0xFFD8B4E2),
            palette = listOf(
                Color(0xFF6A0080),
                Color(0xFF7B2D8B),
                Color(0xFF9B4DB5),
                Color(0xFFB26FD0),
                Color(0xFFC891E5),
                Color(0xFFAD1457),
            )
        )

        /** Grayscale palette for minimal or print-friendly charts. */
        val Monochrome = SalusChartColorScheme(
            primary = Color(0xFF212121),
            secondary = Color(0xFFBDBDBD),
            palette = listOf(
                Color(0xFF212121),
                Color(0xFF424242),
                Color(0xFF616161),
                Color(0xFF757575),
                Color(0xFF9E9E9E),
                Color(0xFFBDBDBD),
            )
        )

        /** Blush pinks and magentas. */
        val Rose = SalusChartColorScheme(
            primary = Color(0xFFE91E63),
            secondary = Color(0xFFF8BBD0),
            palette = listOf(
                Color(0xFFAD1457),
                Color(0xFFE91E63),
                Color(0xFFEC407A),
                Color(0xFFF06292),
                Color(0xFFF48FB1),
                Color(0xFFFF80AB),
            )
        )

        /** Cherry blossoms, fresh greens, and soft sky tones. */
        val Spring = SalusChartColorScheme(
            primary = Color(0xFFE8729A),
            secondary = Color(0xFFD4F0C0),
            palette = listOf(
                Color(0xFFE8729A), // cherry blossom pink
                Color(0xFF6DBF67), // fresh green
                Color(0xFF9BD4F5), // sky blue
                Color(0xFFF7B2C1), // petal blush
                Color(0xFFB5E48C), // light lime
                Color(0xFFC77DFF), // lilac
            )
        )

        /** Bright, saturated hues evoking sun, sea, and tropical fruit. */
        val Summer = SalusChartColorScheme(
            primary = Color(0xFFFF6B00),
            secondary = Color(0xFFFFE066),
            palette = listOf(
                Color(0xFFFF6B00), // vivid orange
                Color(0xFFFFD600), // sunshine yellow
                Color(0xFF00C2CB), // turquoise
                Color(0xFFFF3D71), // hot coral
                Color(0xFF00B96B), // tropical green
                Color(0xFF6C63FF), // electric violet
            )
        )

        /** Warm amber, burnt orange, and earthy reds and browns. */
        val Fall = SalusChartColorScheme(
            primary = Color(0xFFD2691E),
            secondary = Color(0xFFF5CBA7),
            palette = listOf(
                Color(0xFFD2691E), // burnt sienna
                Color(0xFFE07B39), // pumpkin
                Color(0xFFB5451B), // deep rust
                Color(0xFFF0A500), // amber
                Color(0xFF8B4513), // saddle brown
                Color(0xFFCD853F), // peru gold
            )
        )

        /** Icy blues, silvers, and muted teals. */
        val Winter = SalusChartColorScheme(
            primary = Color(0xFF4A90D9),
            secondary = Color(0xFFCFE8FF),
            palette = listOf(
                Color(0xFF4A90D9), // clear blue
                Color(0xFF90CAF9), // ice blue
                Color(0xFF78909C), // steel grey
                Color(0xFFB0BEC5), // silver
                Color(0xFF4DD0E1), // frost teal
                Color(0xFF7986CB), // periwinkle
            )
        )
    }
}

/**
 * Color utility helpers for generating chart palettes.
 */
object ColorUtils {

    /**
     * Generates a rainbow-like palette by sampling evenly spaced hues in HSL space.
     *
     * This is inspired by D3's "quantize + interpolateRainbow" style, but implemented
     * as a simple HSL hue sweep with fixed saturation/lightness.
     *
     * @param count Number of colors to generate.
     * @return A list of [Color] values. Returns an empty list if [count] <= 0.
     */
    fun rainbowPalette(count: Int): List<Color> {
        if (count <= 0) return emptyList()

        return List(count) { i ->
            val t = if (count > 1) (i + 0.5f) / count else 0f
            val hue = 360f * t
            hslToColor(hue, s = 1f, l = 0.5f)
        }
    }

    /**
     * Converts HSL (Hue/Saturation/Lightness) to a Compose [Color].
     *
     * @param h Hue in degrees [0..360).
     * @param s Saturation [0..1].
     * @param l Lightness [0..1].
     */
    private fun hslToColor(h: Float, s: Float, l: Float): Color {
        val c = (1f - abs(2 * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r1, g1, b1) = when {
            h < 60f  -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else     -> Triple(c, 0f, x)
        }

        val r = (r1 + m).coerceIn(0f, 1f)
        val g = (g1 + m).coerceIn(0f, 1f)
        val b = (b1 + m).coerceIn(0f, 1f)

        return Color(r, g, b, 1f)
    }
}
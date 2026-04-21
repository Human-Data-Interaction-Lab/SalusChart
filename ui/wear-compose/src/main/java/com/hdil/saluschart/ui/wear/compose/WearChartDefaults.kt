package com.hdil.saluschart.ui.wear.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.theme.LocalSalusChartColors

/**
 * Wear-first defaults for compact chart layouts.
 */
object WearChartDefaults {
    val ChartPadding: Dp = 12.dp
    val HeaderSpacing: Dp = 8.dp
    val SectionSpacing: Dp = 10.dp
    val MicroSpacing: Dp = 4.dp
    val MinimalChartHeight: Dp = 72.dp
    val RingStroke: Dp = 12.dp
    val RingGap: Dp = 6.dp
    val CompactChartHeight: Dp = 120.dp
    val SummaryChartHeight: Dp = 144.dp
    val CardCornerRadius: Dp = 22.dp

    @Composable
    fun palette(): List<Color> = LocalSalusChartColors.current.palette

    fun trackColor(color: Color): Color = color.copy(alpha = 0.22f)
    val CardBackground: Color = Color(0xFF1E1F24).copy(alpha = 0.88f)
}

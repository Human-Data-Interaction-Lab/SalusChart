package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearMinimalLadderChart(
    modifier: Modifier = Modifier,
    bandCount: Int = 3,
    selectedBandIndex: Int,
    markerRatio: Float,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    selectedColor: Color = Color(0xFF60A5FA),
    markerColor: Color = Color.White,
    bandHeight: Dp = 10.dp,
    bandGap: Dp = 4.dp,
    markerRadius: Dp = 3.dp,
    markerRingWidth: Dp = 2.dp,
    markerRingColor: Color = Color(0xFF0F172A)
) {
    val safeBandCount = bandCount.coerceAtLeast(1)
    val safeSelectedIndex = selectedBandIndex.coerceIn(0, safeBandCount - 1)
    val safeRatio = markerRatio.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height((bandHeight * safeBandCount) + (bandGap * (safeBandCount - 1)))
    ) {
        val bandHeightPx = bandHeight.toPx()
        val bandGapPx = bandGap.toPx()
        val markerRadiusPx = markerRadius.toPx()
        val markerRingPx = markerRingWidth.toPx()
        val totalHeight = safeBandCount * bandHeightPx + (safeBandCount - 1) * bandGapPx
        val startY = (size.height - totalHeight) / 2f
        val corner = CornerRadius(bandHeightPx / 2f, bandHeightPx / 2f)

        repeat(safeBandCount) { index ->
            val top = startY + index * (bandHeightPx + bandGapPx)
            drawRoundRect(
                color = if (index == safeSelectedIndex) selectedColor else trackColor,
                topLeft = Offset(0f, top),
                size = Size(size.width, bandHeightPx),
                cornerRadius = corner
            )

            if (index == safeSelectedIndex) {
                val center = Offset(size.width * safeRatio, top + bandHeightPx / 2f)
                drawCircle(
                    color = markerRingColor,
                    radius = markerRadiusPx + markerRingPx,
                    center = center
                )
                drawCircle(
                    color = markerColor,
                    radius = markerRadiusPx,
                    center = center
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalLadderChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Summer
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalLadderChart(
                bandCount = 4,
                selectedBandIndex = 1,
                markerRatio = 0.7f
            )
        }
    }
}

package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MinimalLadderChart(
    modifier: Modifier = Modifier,
    bandCount: Int = 3,
    selectedBandIndex: Int,
    markerRatio: Float, // 0f..1f
    trackColor: Color,
    selectedColor: Color,
    markerColor: Color,

    bandHeight: Dp = 10.dp,
    bandGap: Dp = 2.dp,
    cornerRadiusRatio: Float = 0.95f,

    markerRadius: Dp = 4.dp,
    markerRingWidth: Dp = 2.dp,
    markerRingColor: Color = Color.White,
) {
    val safeRatio = markerRatio.coerceIn(0f, 1f)
    val safeIndex = selectedBandIndex.coerceIn(0, bandCount - 1)

    Canvas(modifier = modifier) {
        val hPx = bandHeight.toPx()
        val gapPx = bandGap.toPx()

        val totalHeight = bandCount * hPx + (bandCount - 1) * gapPx
        val startY = (size.height - totalHeight) / 2f

        val width = size.width
        val corner = CornerRadius(
            x = hPx * cornerRadiusRatio,
            y = hPx * cornerRadiusRatio
        )

        repeat(bandCount) { index ->
            val y = startY + index * (hPx + gapPx)

            // Base track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, y),
                size = Size(width, hPx),
                cornerRadius = corner
            )

            // Selected band + marker
            if (index == safeIndex) {
                drawRoundRect(
                    color = selectedColor,
                    topLeft = Offset(0f, y),
                    size = Size(width, hPx),
                    cornerRadius = corner
                )

                val cx = width * safeRatio
                val cy = y + hPx / 2f

                val r = markerRadius.toPx()
                val ring = markerRingWidth.toPx()

                // white ring
                drawCircle(
                    color = markerRingColor,
                    radius = r + ring,
                    center = Offset(cx, cy)
                )
                // inner dot
                drawCircle(
                    color = markerColor,
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

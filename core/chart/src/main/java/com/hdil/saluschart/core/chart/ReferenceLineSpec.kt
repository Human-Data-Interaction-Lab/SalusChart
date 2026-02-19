package com.hdil.saluschart.core.chart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.LineStyle

data class ReferenceLineSpec(
    val y: Double,
    val color: Color,
    val strokeWidth: Dp = 2.dp,
    val style: LineStyle = LineStyle.DASHED,
    val label: String? = null,
    val labelBackground: Color? = null
)

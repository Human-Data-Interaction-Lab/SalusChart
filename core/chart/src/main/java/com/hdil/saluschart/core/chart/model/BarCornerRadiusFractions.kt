package com.hdil.saluschart.core.chart.model

import androidx.compose.runtime.Immutable

@Immutable
data class BarCornerRadiusFractions(
    val topStart: Float = 0f,
    val topEnd: Float = 0f,
    val bottomStart: Float = 0f,
    val bottomEnd: Float = 0f
)
package com.hdil.saluschart.data.provider

data class HorizontalStackedBarRow(
    val title: String,
    val unit: String,
    val total: Float,
    val target: Float? = null,
    val segments: List<Float>,
    val segmentLabels: List<String>
)
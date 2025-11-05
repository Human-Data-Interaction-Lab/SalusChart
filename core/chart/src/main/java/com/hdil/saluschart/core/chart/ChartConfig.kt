package com.hdil.saluschart.core.chart

// TODO: 매우 오래된 코드, 변경 또는 삭제 필요
data class ChartConfig(
    val chartType: ChartType = ChartType.LINE,
    val axis: Axis = Axis.XY,
    val showGrid: Boolean = true,
    val showLegend: Boolean = true,
    val showLabels: Boolean = true,
    val labelFormat: String = "%.2f",
    val animationEnabled: Boolean = true,
    val animationDuration: Long = 300L
) {
    enum class ChartType {
        LINE, BAR, PIE, AREA
    }

    enum class Axis {
        X, Y, XY
    }
}

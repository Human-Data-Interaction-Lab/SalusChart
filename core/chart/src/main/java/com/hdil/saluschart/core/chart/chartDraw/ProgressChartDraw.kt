package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.cos
import kotlin.math.sin

object ProgressChartDraw {
    
    /**
     * 프로그레스 마크를 그립니다 (도넛 또는 바 형태).
     * PieChart의 drawPieSection과 같은 패턴을 따릅니다.
     * 
     * @param drawScope 그리기 영역
     * @param data 프로그레스 차트 데이터 리스트
     * @param size 캔버스 크기
     * @param colors 각 프로그레스 인스턴스에 사용할 색상 목록
     * @param isDonut 도넛 형태로 그릴지 여부 (true: 도넛, false: 바)
     * @param strokeWidth 도넛일 경우 링의 두께
     * @param barHeight 바일 경우 각 바의 높이
     * @param backgroundAlpha 배경의 투명도
     */
    fun drawProgressMarks(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        colors: List<Color>,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        backgroundAlpha: Float = 0.1f,
        barSpacing: Float = 16f,
        topPadding: Float = 8f,
        cornerRadius: Float = barHeight / 2f
    ) {
        if (isDonut) {
            val (center, _, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size, data = data, strokeWidth = strokeWidth
            )

            fun lighten(c: Color, f: Float) = Color(
                (c.red + (1f - c.red) * f).coerceIn(0f, 1f),
                (c.green + (1f - c.green) * f).coerceIn(0f, 1f),
                (c.blue + (1f - c.blue) * f).coerceIn(0f, 1f),
                c.alpha
            )

            fun darken(c: Color, f: Float) = Color(
                (c.red * (1f - f)).coerceIn(0f, 1f),
                (c.green * (1f - f)).coerceIn(0f, 1f),
                (c.blue * (1f - f)).coerceIn(0f, 1f),
                c.alpha
            )

            data.forEachIndexed { idx, pt ->
                if (idx >= ringRadii.size) return@forEachIndexed

                val radius = ringRadii[idx]
                val base = colors.getOrElse(idx) { colors.first() }

                val ringRect = Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius
                )

                // 1) Background track
                drawScope.drawArc(
                    color = base.copy(alpha = backgroundAlpha),
                    startAngle = 0f,
                    sweepAngle = 359.9f,
                    useCenter = false,
                    topLeft = Offset(ringRect.left, ringRect.top),
                    size = Size(ringRect.width, ringRect.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // 2) Progress
                val raw = if (pt.max > 0.0) pt.current / pt.max else pt.progress
                val progress = raw.coerceIn(0.0, 2.0)
                if (progress <= 0.0) return@forEachIndexed

                val startAt = -90f

                fun arcGradient(radius: Float, sweep: Float, tailDark: Boolean): Brush {
                    val startRad = Math.toRadians(startAt.toDouble())
                    val endRad = Math.toRadians((startAt + sweep).toDouble())

                    val startPoint = Offset(
                        x = center.x + radius * cos(startRad).toFloat(),
                        y = center.y + radius * sin(startRad).toFloat()
                    )
                    val endPoint = Offset(
                        x = center.x + radius * cos(endRad).toFloat(),
                        y = center.y + radius * sin(endRad).toFloat()
                    )

                    val tailColor = if (tailDark) darken(base, 0.2f) else base
                    val midColor = base
                    val headColor = lighten(base, 0.35f)

                    return Brush.linearGradient(
                        colors = listOf(
                            tailColor,
                            midColor,
                            headColor
                        ),
                        start = startPoint,
                        end = endPoint
                    )
                }

                if (progress <= 1.0) {
                    // ---- 0–100%: single, softly-graded ring ----
                    val sweep = (progress * 360.0).toFloat()
                    val brush = arcGradient(radius, sweep, tailDark = true)

                    drawScope.drawArc(
                        brush = brush,
                        startAngle = startAt,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(ringRect.left, ringRect.top),
                        size = Size(ringRect.width, ringRect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    // ---- >100%: one full lap + extra lap on top (same radius) ----
                    // First lap: full ring in base color
                    drawScope.drawArc(
                        color = base,
                        startAngle = startAt,
                        sweepAngle = 359.9f,
                        useCenter = false,
                        topLeft = Offset(ringRect.left, ringRect.top),
                        size = Size(ringRect.width, ringRect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    // Extra portion (second lap): gradient arc with tail = base
                    val extraRatio = (progress - 1.0).coerceIn(0.0, 1.0)
                    if (extraRatio > 0.0) {
                        val extraSweep = (extraRatio * 360.0).toFloat()
                        val extraBrush = arcGradient(radius, extraSweep, tailDark = false)

                        drawScope.drawArc(
                            brush = extraBrush,
                            startAngle = startAt,
                            sweepAngle = extraSweep,
                            useCenter = false,
                            topLeft = Offset(ringRect.left, ringRect.top),
                            size = Size(ringRect.width, ringRect.height),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }

        } else {
            // Bars
            val leftPadding = 90f
            val rightPadding = 100f
            val trackWidth = (size.width - leftPadding - rightPadding).coerceAtLeast(0f)

            data.forEachIndexed { index, point ->
                val y = topPadding + index * (barHeight + barSpacing)
                val color = colors.getOrElse(index) { colors.first() }
                val progressWidth = trackWidth * point.progress.coerceIn(0.0, 1.0).toFloat()

                drawScope.drawRoundRect(
                    color = color.copy(alpha = backgroundAlpha),
                    topLeft = Offset(leftPadding, y),
                    size = Size(trackWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
                if (progressWidth > 0f) {
                    drawScope.drawRoundRect(
                        color = color,
                        topLeft = Offset(leftPadding, y),
                        size = Size(progressWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )
                }
            }
        }
    }

    /**
     * 프로그레스 라벨을 그립니다.
     * 
     * @param drawScope 그리기 영역
     * @param data 프로그레스 차트 데이터 리스트
     * @param size 캔버스 크기
     * @param isDonut 도넛 차트 여부
     * @param strokeWidth 도넛일 경우 링의 두께
     * @param barHeight 바일 경우 각 바의 높이
     * @param textSize 텍스트 크기
     */
    fun drawProgressLabels(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        textSize: Float = 32f,
        barSpacing: Float = 16f,
        topPadding: Float = 8f
    ) {
        if (isDonut) {
            val (center, maxRadius, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size,
                data = data,
                strokeWidth = strokeWidth
            )
            
            data.forEachIndexed { index, point ->
                val radius = ringRadii.getOrElse(index) { 0f }
                val labelPosition = ChartMath.Progress.computeLabelPosition(
                    center = center,
                    radius = radius,
                    isDonut = true,
                    point = point,
                )
                
                // 라벨 텍스트 그리기
                point.label?.let { label ->
                    drawScope.drawContext.canvas.nativeCanvas.drawText(
                        label,
                        labelPosition.x,
                        labelPosition.y,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }
        } else {
            val leftX = 64f
            
            data.forEachIndexed { index, point ->
                val centerY = topPadding + index * (barHeight + barSpacing) + barHeight / 2f
                point.label?.let { label ->
                    drawScope.drawContext.canvas.nativeCanvas.drawText(
                        label,
                        leftX,
                        centerY + textSize * 0.35f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
    
    /**
     * 프로그레스 값을 그립니다.
     * 
     * @param drawScope 그리기 영역
     * @param data 프로그레스 차트 데이터 리스트
     * @param size 캔버스 크기
     * @param isDonut 도넛 차트 여부
     * @param strokeWidth 도넛일 경우 링의 두께
     * @param barHeight 바일 경우 각 바의 높이
     * @param textSize 텍스트 크기
     */
    fun drawProgressValues(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        textSize: Float = 28f,
        isPercentage: Boolean,
        barSpacing: Float = 16f,
        topPadding: Float = 8f
    ) {
        if (isDonut) {
            val (center, maxRadius, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size,
                data = data,
                strokeWidth = strokeWidth
            )
            
            data.forEachIndexed { index, point ->
                val radius = ringRadii.getOrElse(index) { 0f }
                val valuePosition = ChartMath.Progress.computeValuePosition(
                    center = center,
                    radius = radius,
                    isDonut = true,
                    point = point
                )
                
                // 값 텍스트 생성
                val valueText = if (isPercentage) {
                    "${(point.percentage).toInt()}%"
                } else {
                    buildString {
                        append("${point.current.toInt()}")
                        point.unit?.let { append(" $it") }
                        append(" / ${point.max.toInt()}")
                        point.unit?.let { append(" $it") }
                    }
                }

                // 값 텍스트 그리기
                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    valuePosition.x,
                    valuePosition.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        } else {
            val rightX = size.width - 24f
            
            data.forEachIndexed { index, point ->
                val centerY = topPadding + index * (barHeight + barSpacing) + barHeight / 2f
                val valueText = if (isPercentage) {
                    "${point.percentage.toInt()}%"
                } else {
                    buildString {
                        append(point.current.toInt())
                        point.unit?.let { append(" $it") }
                        append(" / ${point.max.toInt()}")
                        point.unit?.let { append(" $it") }
                    }
                }
                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    rightX,
                    centerY + textSize * 0.35f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }
    }
    
    /**
     * 프로그레스 중앙 요약 정보를 그립니다 (도넛 차트 전용).
     * 
     * @param drawScope 그리기 영역
     * @param center 중심점
     * @param title 제목 텍스트
     * @param subtitle 부제목 텍스트
     * @param titleSize 제목 텍스트 크기
     * @param subtitleSize 부제목 텍스트 크기
     */
    fun drawProgressCenterInfo(
        drawScope: DrawScope,
        center: Offset,
        title: String = "Activity",
        subtitle: String = "Progress",
        titleSize: Float = 36f,
        subtitleSize: Float = 24f
    ) {
        // 제목 그리기
        drawScope.drawContext.canvas.nativeCanvas.drawText(
            title,
            center.x,
            center.y - 10f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = titleSize
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
        )
        
        // 부제목 그리기
        drawScope.drawContext.canvas.nativeCanvas.drawText(
            subtitle,
            center.x,
            center.y + 20f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = subtitleSize
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

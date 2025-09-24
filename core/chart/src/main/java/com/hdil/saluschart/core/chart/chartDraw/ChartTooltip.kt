package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartPoint
import kotlin.math.roundToInt

/**
 * 차트 툴팁을 표시하는 컴포저블
 *
 * @param chartPoint 표시할 데이터 포인트
 * @param unit 데이터 단위 (예: "kg", "lb", "bpm" 등)
 * @param backgroundColor 툴팁 배경색
 * @param textColor 텍스트 색상
 * @param customText 커스텀 툴팁 텍스트 (null이 아니면 기본 로직 대신 사용)
 * @param modifier 모디파이어
 */
@Composable
fun ChartTooltip(
    chartPoint: BaseChartPoint,
    unit: String = "",
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    customText: String? = null,
    color: Color = Color.Black,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                color = backgroundColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chartPoint.label?.let { label ->
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha =1f),
                    lineHeight = 16.sp
                )
            }
            // Custom text has the highest priority
            if (customText != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = customText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.9f),
                        lineHeight = 14.sp
                    )
                }
            } else {
                when (chartPoint) {
                    is com.hdil.saluschart.core.chart.RangeChartPoint -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = "${chartPoint.minPoint.y.roundToInt()}$unit ~ ${chartPoint.maxPoint.y.roundToInt()}$unit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    is com.hdil.saluschart.core.chart.StackedChartPoint -> {
                        chartPoint.segments.asReversed().forEachIndexed { index, segment ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = color,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (unit.isNotEmpty()) {
                                        "${segment.y.roundToInt()}$unit"
                                    } else {
                                        segment.y.roundToInt().toString()
                                           },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor.copy(alpha = 0.9f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    else -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = color,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = if (unit.isNotEmpty()) {
                                    "${chartPoint.y.roundToInt()}$unit"
                                } else {
                                    chartPoint.y.roundToInt().toString()
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

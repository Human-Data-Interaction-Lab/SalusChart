package com.hdil.saluschart.core.chart.chartDraw

import android.util.Log
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
import kotlin.math.abs

/**
 * 차트 툴팁을 표시하는 컴포저블
 *
 * @param chartPoint 표시할 데이터 포인트
 * @param backgroundColor 툴팁 배경색
 * @param textColor 텍스트 색상
 * @param modifier 모디파이어
 */
@Composable
fun ChartTooltip(
    chartPoint: BaseChartPoint,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
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
            when(chartPoint) {
                is com.hdil.saluschart.core.chart.StackedChartPoint -> {
                    chartPoint.values.asReversed().forEachIndexed { index, value ->
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
                                text = formatValueForDisplay(value),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                } else -> {
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
                            text = formatValueForDisplay(chartPoint.y),
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

/**
 * Float 값을 적절한 형태로 포맷팅
 * 정수값(소수점 이하가 0)인 경우 정수로 표시, 그렇지 않으면 Float로 표시
 */
private fun formatValueForDisplay(value: Float): String {
    return if (abs(value - value.toInt()) < 0.001f) {
        // 값이 실질적으로 정수인 경우 (부동소수점 오차 고려)
        value.toInt().toString()
    } else {
        value.toString()
    }
}

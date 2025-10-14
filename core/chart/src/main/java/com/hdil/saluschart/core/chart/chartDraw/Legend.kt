package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

/**
 * 차트 범례를 Composable로 표시합니다.
 *
 * @param modifier 모디파이어
 * @param labels 범례 항목 레이블 목록 (직접 제공된 경우)
 * @param chartData 차트 데이터 포인트 목록 (레이블을 추출할 경우)
 * @param colors 각 항목에 사용한 색상 목록
 * @param title 범례 제목 (기본값: null)
 * @param colorBoxSize 색상 상자 크기
 * @param textSize 텍스트 크기
 * @param spacing 항목 간 간격
 */
@Composable
fun ChartLegend(
    modifier: Modifier = Modifier,
    labels: List<String>? = null,
    chartData: List<ChartMark>? = null,
    position: LegendPosition = LegendPosition.BOTTOM,
    colors: List<Color>,
    title: String? = null,
    colorBoxSize: Dp = 12.dp,
    textSize: TextUnit = 12.sp,
    spacing: Dp = 8.dp
) {
    val legendLabels = labels ?: chartData?.mapIndexed { i, point ->
        point.label ?: "항목 ${i + 1}"
    } ?: emptyList()

    when (position) {
        LegendPosition.TOP, LegendPosition.BOTTOM -> {
            // 위/아래: 가로로 배치
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spacing * 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(legendLabels.size) { index ->
                    if (index < colors.size) {
                        LegendItem(
                            color = colors[index],
                            label = legendLabels[index],
                            colorBoxSize = colorBoxSize,
                            textSize = textSize,
                            spacing = spacing
                        )
                    }
                }
            }
        }
        LegendPosition.LEFT, LegendPosition.RIGHT -> {
            // 좌/우: 세로로 배치
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 범례 제목 (제공된 경우)
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = (textSize.value + 2).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 각 범례 항목
                legendLabels.forEachIndexed { index, label ->
                    if (index < colors.size) {
                        LegendItem(
                            color = colors[index],
                            label = label,
                            colorBoxSize = colorBoxSize,
                            textSize = textSize,
                            spacing = spacing
                        )
                    }
                }
            }
        }
    }
}

/**
 * 범례의 개별 항목을 Composable로 표시합니다.
 *
 * @param color 색상
 * @param label 레이블 텍스트
 * @param colorBoxSize 색상 상자 크기
 * @param textSize 텍스트 크기
 * @param spacing 상자와 텍스트 사이 간격
 */
@Composable
fun LegendItem(
    color: Color,
    label: String,
    colorBoxSize: Dp = 20.dp,
    textSize: TextUnit = 16.sp,
    spacing: Dp = 8.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // 색상 상자
        Box(
            modifier = Modifier
                .size(colorBoxSize)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // 레이블 텍스트
        Text(
            text = label,
            fontSize = textSize,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 범례 위치를 나타내는 enum class
 */
enum class LegendPosition {
    LEFT,   // 왼쪽
    RIGHT,  // 오른쪽
    TOP,    // 위쪽
    BOTTOM  // 아래쪽
}

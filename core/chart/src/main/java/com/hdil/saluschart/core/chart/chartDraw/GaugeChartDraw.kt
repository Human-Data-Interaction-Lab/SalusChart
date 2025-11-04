package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// TODO: 현재 Minimal Gauge Chart 제작 시 사용 중, 추후 Gauge chart (큰 버전) 제작 시 재사용 가능한 함수들
object GaugeChartDraw {
    /**
     * 범위 텍스트를 표시하는 컴포저블
     */
    @Composable
    fun RangeText(
        dataMin: Float,
        dataMax: Float,
        textColor: Color
    ) {
        Text(
            text = "${dataMin.toInt()}-${dataMax.toInt()}",
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    /**
     * 게이지 바를 그리는 컴포저블
     * @param dataMin 데이터 최소값
     * @param dataMax 데이터 최대값
     * @param containerMin 컨테이너(게이지 바를 감싸는 배경) 최소값
     * @param containerMax 컨테이너(게이지 바를 감싸는 배경) 최대값
     * @param containerColor 컨테이너 색상
     * @param rangeColor 범위 색상
     */
    @Composable
    fun GaugeBar(
        dataMin: Float,
        dataMax: Float,
        containerMin: Float,
        containerMax: Float,
        containerColor: Color,
        rangeColor: Color
    ) {
        val density = LocalDensity.current
        var containerWidth by remember { mutableStateOf(0.dp) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .onGloballyPositioned { coordinates ->
                    containerWidth = with(density) { coordinates.size.width.toDp() }
                }
        ) {
            val containerRange = containerMax - containerMin

            // 안전한 비율 계산
            val startRatio = if (containerRange > 0) {
                ((dataMin - containerMin) / containerRange).coerceIn(0f, 1f)
            } else 0f

            val endRatio = if (containerRange > 0) {
                ((dataMax - containerMin) / containerRange).coerceIn(0f, 1f)
            } else 0f

            val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

            // 컨테이너 바 (배경)
            ContainerBar(
                containerColor = containerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            // 범위 바 (실제 데이터 범위) - 컨테이너 내부에 정확히 배치
            if (widthRatio > 0f && containerWidth > 0.dp) {
                RangeBar(
                    rangeColor = rangeColor,
                    startOffset = containerWidth * startRatio,
                    barWidth = containerWidth * widthRatio,
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }

    /**
     * 컨테이너(배경) 바 컴포저블
     */
    @Composable
    private fun ContainerBar(
        containerColor: Color,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
        )
    }

    /**
     * 범위 바 컴포저블
     */
    @Composable
    private fun RangeBar(
        rangeColor: Color,
        startOffset: Dp,
        barWidth: Dp,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .width(barWidth)
                .offset(x = startOffset)
                .clip(RoundedCornerShape(8.dp))
                .background(rangeColor)
        )
    }
}

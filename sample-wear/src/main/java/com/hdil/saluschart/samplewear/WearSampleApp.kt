package com.hdil.saluschart.samplewear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import com.hdil.saluschart.ui.wear.compose.WearChartDefaults
import com.hdil.saluschart.ui.wear.compose.WearMinimalGaugeChart
import com.hdil.saluschart.ui.wear.compose.WearMinimalGaugeSegment
import com.hdil.saluschart.ui.wear.compose.WearMinimalLadderChart
import com.hdil.saluschart.ui.wear.compose.WearMinimalMultiSegmentGaugeChart
import com.hdil.saluschart.ui.wear.compose.WearPieChart
import com.hdil.saluschart.ui.wear.compose.WearProgressRing
import com.hdil.saluschart.ui.wear.compose.WearSleepStageChart
import java.time.Instant

private val SamplePrimaryText = Color(0xFFF5F7FF)
private val SampleSecondaryText = Color(0xFFD8DCE8)
private val SampleChipBackground = Color.White.copy(alpha = 0.06f)

@Composable
fun WearSampleApp() {
    val cardShape = RoundedCornerShape(WearChartDefaults.CardCornerRadius)
    val progressPercent = ((sampleProgressData.map { it.progress }.average() * 100).toInt()).coerceIn(0, 100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080A10),
                        Color(0xFF10131C),
                        Color(0xFF050608)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SampleHeader()

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "01",
                    title = "WearProgressRing",
                    subtitle = "activity progress"
                )
                WearProgressRing(
                    data = sampleProgressData,
                    titleColor = SamplePrimaryText,
                    showLabels = true,
                    showValues = true,
                    centerLabel = "$progressPercent%",
                    centerCaption = "closing rings",
                    centerLabelColor = SamplePrimaryText,
                    centerCaptionColor = SampleSecondaryText,
                    labelColor = SamplePrimaryText,
                    valueColor = SamplePrimaryText,
                    labelChipColor = SampleChipBackground
                )
            }

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "02",
                    title = "WearPieChart",
                    subtitle = "sleep mix summary"
                )
                WearPieChart(
                    data = samplePieData,
                    titleColor = SamplePrimaryText,
                    centerLabelColor = SampleSecondaryText,
                    centerValueColor = SamplePrimaryText,
                    showBreakdown = true,
                    breakdownItemCount = 3,
                    breakdownChipColor = SampleChipBackground
                )
            }

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "03",
                    title = "WearMinimalGaugeChart",
                    subtitle = "target range summary"
                )
                WearMinimalGaugeChart(
                    data = sampleGaugeRange,
                    containerMin = 40.0,
                    containerMax = 140.0,
                    rangeColor = Color(0xFFFF8A65)
                )
            }

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "04",
                    title = "WearMinimalMultiSegmentGaugeChart",
                    subtitle = "multi-zone gauge"
                )
                WearMinimalMultiSegmentGaugeChart(
                    segments = sampleGaugeSegments,
                    markerRatio = 0.62f
                )
            }

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "05",
                    title = "WearMinimalLadderChart",
                    subtitle = "banded fitness level"
                )
                WearMinimalLadderChart(
                    bandCount = 4,
                    selectedBandIndex = 1,
                    markerRatio = 0.74f,
                    selectedColor = Color(0xFF7DD3FC),
                    markerRingColor = Color(0xFF0B1220)
                )
            }

            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "06",
                    title = "WearSleepStageChart",
                    subtitle = "main chart parity"
                )
                WearSleepStageChart(
                    sleepSession = sampleSleepSession,
                    title = "Sleep Stage Analysis",
                    showTitle = false,
                    showYAxis = true,
                    showYAxisLabels = false,
                    showXAxis = true,
                    showXAxisLabels = false,
                    barHeightRatio = 0.48f,
                    yAxisFixedWidth = 30.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                )
            }

            /*
            WearCard(shape = cardShape) {
                SectionHeader(
                    index = "M",
                    title = "Minimal Wear Charts",
                    subtitle = "full minimal lineup"
                )
                MinimalChartGallery()
            }
            */
        }
    }
}

@Composable
private fun SampleHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "SalusChart Wear",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB5C7FF)
        )
        Text(
            text = "basic chart showcase",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SamplePrimaryText,
            textAlign = TextAlign.Center
        )
        Text(
            text = "review one component at a time",
            style = MaterialTheme.typography.bodySmall,
            color = SampleSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(
    index: String,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = index,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8FB8FF)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = SamplePrimaryText
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SampleSecondaryText
        )
    }
}

@Composable
private fun WearCard(
    shape: RoundedCornerShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(WearChartDefaults.CardBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

private val sampleProgressData = listOf(
    ProgressChartMark(x = 0.0, current = 510.0, max = 700.0, label = "Move", unit = "kcal"),
    ProgressChartMark(x = 1.0, current = 24.0, max = 30.0, label = "Exercise", unit = "min"),
    ProgressChartMark(x = 2.0, current = 10.0, max = 12.0, label = "Stand", unit = "hr")
)

private val samplePieData = listOf(
    ChartMark(x = 0.0, y = 47.0, label = "Deep"),
    ChartMark(x = 1.0, y = 102.0, label = "Light"),
    ChartMark(x = 2.0, y = 26.0, label = "REM")
)

private val sampleRangeData = listOf(
    RangeChartMark(0.0, ChartMark(0.0, 62.0), ChartMark(0.0, 118.0), label = "Mon"),
    RangeChartMark(1.0, ChartMark(1.0, 60.0), ChartMark(1.0, 121.0), label = "Tue"),
    RangeChartMark(2.0, ChartMark(2.0, 64.0), ChartMark(2.0, 116.0), label = "Wed"),
    RangeChartMark(3.0, ChartMark(3.0, 58.0), ChartMark(3.0, 124.0), label = "Thu"),
    RangeChartMark(4.0, ChartMark(4.0, 61.0), ChartMark(4.0, 119.0), label = "Fri")
)

private val sampleGaugeRange = RangeChartMark(
    x = 0.0,
    minPoint = ChartMark(0.0, 58.0),
    maxPoint = ChartMark(0.0, 104.0),
    label = "Target"
)

private val sampleGaugeSegments = listOf(
    WearMinimalGaugeSegment(0.25f, Color(0xFF60A5FA)),
    WearMinimalGaugeSegment(0.45f, Color(0xFF22C55E)),
    WearMinimalGaugeSegment(0.30f, Color(0xFFF97316))
)

private val sampleSleepSession = SleepSession(
    startTime = Instant.parse("2026-04-12T14:00:00Z"),
    endTime = Instant.parse("2026-04-12T21:18:00Z"),
    stages = listOf(
        SleepStage(Instant.parse("2026-04-12T14:00:00Z"), Instant.parse("2026-04-12T15:00:00Z"), SleepStageType.LIGHT),
        SleepStage(Instant.parse("2026-04-12T15:00:00Z"), Instant.parse("2026-04-12T16:20:00Z"), SleepStageType.DEEP),
        SleepStage(Instant.parse("2026-04-12T16:20:00Z"), Instant.parse("2026-04-12T17:45:00Z"), SleepStageType.LIGHT),
        SleepStage(Instant.parse("2026-04-12T17:45:00Z"), Instant.parse("2026-04-12T18:25:00Z"), SleepStageType.REM),
        SleepStage(Instant.parse("2026-04-12T18:25:00Z"), Instant.parse("2026-04-12T18:35:00Z"), SleepStageType.AWAKE),
        SleepStage(Instant.parse("2026-04-12T18:35:00Z"), Instant.parse("2026-04-12T21:18:00Z"), SleepStageType.LIGHT)
    )
)

@Preview(
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun WearSampleAppPreview() {
    WearSampleTheme {
        WearSampleApp()
    }
}

@Preview(
    device = "id:wearos_square",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun WearSampleAppSquarePreview() {
    WearSampleTheme {
        WearSampleApp()
    }
}

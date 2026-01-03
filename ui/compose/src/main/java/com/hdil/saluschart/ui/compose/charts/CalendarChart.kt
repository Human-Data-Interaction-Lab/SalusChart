package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * 캘린더 차트에 표시할 데이터 모델
 */
data class CalendarEntry(
    val date: LocalDate,
    val value: Float,
    val color: Color? = null,
    val rings: List<ProgressChartMark>? = null
)

enum class BubbleType {
    CIRCLE, // 단일 월 데이터
    RECTANGLE // 여러 월 데이터
}

enum class CellMarkerType {
    BUBBLE,
    MINI_RINGS
}

/**
 * 캘린더 차트를 표시하는 Composable 함수
 */
@Composable
fun CalendarChart(
    modifier: Modifier = Modifier,
    entries: List<CalendarEntry>,
    yearMonth: YearMonth = YearMonth.now(),
    markerType: CellMarkerType = CellMarkerType.BUBBLE,
    bubbleType: BubbleType = BubbleType.CIRCLE,
    maxBubbleSize: Float = 10f,
    minBubbleSize: Float = 6f,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    // 단일 월 데이터만 있으면 기존 CalendarChart 로직 사용
    SingleMonthCalendarChart(
        modifier = modifier,
        entries = entries,
        yearMonth = yearMonth,
        markerType = markerType,
        bubbleType = bubbleType,
        maxBubbleSize = maxBubbleSize,
        minBubbleSize = minBubbleSize,
        color = color,
    )
}

/**
 * 단일 월을 표시하는 캘린더 차트 (기존 CalendarChart 로직)
 */
@Composable
fun SingleMonthCalendarChart(
    modifier: Modifier = Modifier,
    entries: List<CalendarEntry>,
    yearMonth: YearMonth = YearMonth.now(),
    markerType: CellMarkerType = CellMarkerType.BUBBLE,
    bubbleType: BubbleType,
    maxBubbleSize: Float,
    minBubbleSize: Float,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val maxValue = entries.maxOfOrNull { it.value } ?: 1f
    val entriesByDate = entries.associateBy { it.date }
    val dayOfWeeks = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    )
    val (firstDayOfWeek, totalDays, weeks) = ChartMath.Calendar.computeCalendarMetrics(yearMonth)

    // Tooltip state
    var chartRootBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var tooltipPoint by remember { mutableStateOf<com.hdil.saluschart.core.chart.BaseChartMark?>(null) }
    var tooltipAnchor by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    var ringsTooltipLines by remember { mutableStateOf<List<String>?>(null) }
    var ringsTooltipTitle by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .onGloballyPositioned { chartRootBounds = it.boundsInWindow() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        tooltipPoint = null
                        tooltipAnchor = null
                        ringsTooltipLines = null
                        ringsTooltipTitle = null
                    }
                )
            }
    ) {
        Column(Modifier.fillMaxWidth()) {

            // Title
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())} ${yearMonth.year}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Week header
            Row(modifier = Modifier.fillMaxWidth()) {
                dayOfWeeks.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid
            Column {
                for (week in 0 until weeks) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        for (day in 0 until 7) {
                            val dayOfMonth = week * 7 + day - firstDayOfWeek + 1
                            val entry = if (dayOfMonth in 1..totalDays) {
                                entriesByDate[yearMonth.atDay(dayOfMonth)]
                            } else null

                            CalendarCellComposable(
                                modifier = Modifier.weight(1f).height(76.dp),
                                dayOfMonth = dayOfMonth,
                                totalDays = totalDays,
                                isWeekend = day == 0,
                                entry = entry,
                                maxValue = maxValue,
                                markerType = markerType,
                                bubbleType = bubbleType,
                                minBubbleSize = minBubbleSize,
                                maxBubbleSize = maxBubbleSize,
                                color = color,
                                yearMonth = yearMonth,
                                onTapDay = { date, bounds, tappedEntry, _ ->

                                    if (tappedEntry != null) {
                                        tooltipPoint = null
                                        tooltipAnchor = null

                                        if (markerType == CellMarkerType.MINI_RINGS) {
                                            val rings = tappedEntry.rings.orEmpty()
                                            val move = rings.getOrNull(0)
                                            val exercise = rings.getOrNull(1)
                                            val stand = rings.getOrNull(2)

                                            fun fmt(title: String, m: com.hdil.saluschart.core.chart.ProgressChartMark?): String {
                                                if (m == null) return "$title: -"
                                                val cur = m.current
                                                val max = m.max
                                                return "$title: ${cur.toInt()}/${max.toInt()}"
                                            }

                                            ringsTooltipTitle = date.toString() // or "Activity Rings"
                                            ringsTooltipLines = listOf(
                                                fmt("Move", move),
                                                fmt("Exercise", exercise),
                                                fmt("Stand", stand)
                                            )
                                            tooltipAnchor = bounds

                                            // Important: don't show the normal ChartTooltip
                                            tooltipPoint = null
                                            return@CalendarCellComposable
                                        } else {
                                            val rounded = tappedEntry.value.roundToInt().toDouble()
                                            tooltipPoint = com.hdil.saluschart.core.chart.ChartMark(
                                                x = date.dayOfMonth.toDouble(),
                                                y = rounded,
                                                label = date.toString()
                                            )
                                            tooltipAnchor = bounds
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tooltip
        val tip = tooltipPoint
        val anchor = tooltipAnchor
        val root = chartRootBounds
        val ringLines = ringsTooltipLines
        val ringTitle = ringsTooltipTitle

        if (anchor != null && root != null && (tip != null || ringLines != null)) {
            val density = LocalDensity.current
            val marginPx = with(density) { 8.dp.toPx() }

            val cx = (anchor.left + anchor.right) / 2f
            val bubbleBottomY = anchor.bottom

            val xLocal = cx - root.left
            val yLocal = bubbleBottomY - root.top + marginPx

            val width = tooltipSize.width.toFloat()
            val height = tooltipSize.height.toFloat()
            val hasSize = width > 0f && height > 0f

            val baseX = if (hasSize) xLocal - width / 2f else xLocal
            val baseY = yLocal

            val maxX = (root.width - width).coerceAtLeast(0f)
            val maxY = (root.height - height).coerceAtLeast(0f)

            val clampedX = baseX.coerceIn(0f, maxX)
            val clampedY = baseY.coerceIn(0f, maxY)

            val offset = IntOffset(clampedX.roundToInt(), clampedY.roundToInt())

            Box(
                Modifier
                    .zIndex(10f)
                    .offset { offset }
                    .onGloballyPositioned { coords -> tooltipSize = coords.size }
                    .graphicsLayer { alpha = if (hasSize) 1f else 0f }
            ) {
                if (ringLines != null && ringTitle != null) {
                    MultiLineTooltip(
                        title = ringTitle,
                        lines = ringLines,
                        color = color
                    )
                } else if (tip != null) {
                    com.hdil.saluschart.core.chart.chartDraw.ChartTooltip(
                        ChartMark = tip,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiLineTooltip(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF5A5A5A),
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            lines.forEach { line ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // bullet
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(color, shape = CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 개별 캘린더 셀을 Composable로 구현
 */
@Composable
private fun CalendarCellComposable(
    modifier: Modifier = Modifier,
    dayOfMonth: Int,
    totalDays: Int,
    isWeekend: Boolean,
    entry: CalendarEntry?,
    maxValue: Float,
    markerType: CellMarkerType,
    bubbleType: BubbleType,
    minBubbleSize: Float,
    maxBubbleSize: Float,
    color: Color,
    yearMonth: YearMonth,
    onTapDay: (LocalDate, Rect, CalendarEntry?, Float) -> Unit
) {
    var cellBounds by remember { mutableStateOf<Rect?>(null) }

    val bubbleRadiusDp: Float = remember(entry, maxValue, minBubbleSize, maxBubbleSize) {
        if (entry != null) {
            ChartMath.Calendar.calculateBubbleSize(
                value = entry.value,
                maxValue = maxValue,
                minSize = minBubbleSize,
                maxSize = maxBubbleSize
            )
        } else 0f
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .onGloballyPositioned { coords -> cellBounds = coords.boundsInWindow() }
            .pointerInput(dayOfMonth, entry) {
                detectTapGestures(onTap = {
                    if (dayOfMonth in 1..totalDays) {
                        cellBounds?.let {
                            onTapDay(yearMonth.atDay(dayOfMonth), it, entry, bubbleRadiusDp)
                        }
                    }
                })
            },
        contentAlignment = Alignment.Center
    ) {
        if (dayOfMonth !in 1..totalDays) return@Box
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = dayOfMonth.toString(),
                fontSize = 12.sp,
                color = if (isWeekend) Color.Red else Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (markerType) {
                    CellMarkerType.BUBBLE -> {
                        if (bubbleType == BubbleType.CIRCLE) {
                            entry?.let { dataEntry ->
                                val bubbleRadius = ChartMath.Calendar.calculateBubbleSize(
                                    value = dataEntry.value,
                                    maxValue = maxValue,
                                    minSize = minBubbleSize,
                                    maxSize = maxBubbleSize
                                )
                                val bubbleColor = ChartMath.Calendar.calculateBubbleColor(
                                    color = dataEntry.color ?: color,
                                    value = dataEntry.value,
                                    maxValue = maxValue,
                                    minSize = minBubbleSize,
                                    maxSize = maxBubbleSize
                                )

                                Box(
                                    modifier = Modifier
                                        .size((bubbleRadius * 2).dp)
                                        .clip(CircleShape)
                                        .background(bubbleColor)
                                )
                            }
                        } else {
                            entry?.let { dataEntry ->
                                val bubbleColor = ChartMath.Calendar.calculateBubbleColor(
                                    color = dataEntry.color ?: color,
                                    value = dataEntry.value,
                                    maxValue = maxValue,
                                    minSize = minBubbleSize,
                                    maxSize = maxBubbleSize
                                )
                                Box(
                                    modifier = Modifier
                                        .size((maxBubbleSize * 2).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bubbleColor)
                                )
                            }
                        }
                    }

                    CellMarkerType.MINI_RINGS -> {
                        val rings = entry?.rings
                        if (!rings.isNullOrEmpty()) {
                            val ringColors = listOf(
                                Color(0xFFE91E63),
                                Color(0xFF4CAF50),
                                Color(0xFF9C27B0),
                            )

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val density = LocalDensity.current

                                // Available space inside this marker box
                                val maxW = constraints.maxWidth.toFloat()
                                val maxH = constraints.maxHeight.toFloat()

                                // Keep some breathing room so rings don't touch edges
                                val paddingPx = with(density) { 2.dp.toPx() }

                                // Make ring as large as possible within the marker area
                                val ringSizePx = (min(maxW, maxH) - paddingPx * 2f).coerceAtLeast(0f)
                                val ringSizeDp = with(density) { ringSizePx.toDp() }

                                MiniActivityRings(
                                    modifier = Modifier.size(ringSizeDp),
                                    rings = rings,
                                    colors = ringColors,
                                    strokeWidth = with(density) { 3.2.dp.toPx() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 여러 월을 표시하는 캘린더 차트
 */
@Composable
fun PagedCalendarChart(
    modifier: Modifier = Modifier,
    initialYearMonth: YearMonth,
    entriesForMonth: (YearMonth) -> List<CalendarEntry>,
    bubbleType: BubbleType = BubbleType.CIRCLE,
    markerType: CellMarkerType = CellMarkerType.BUBBLE,
    maxBubbleSize: Float = 10f,
    minBubbleSize: Float = 6f,
    color: Color = MaterialTheme.colorScheme.primary,
    virtualSpanMonths: Int = 2400,
    onMonthChanged: ((YearMonth) -> Unit)? = null,
) {
    val anchor = virtualSpanMonths / 2
    val pagerState = rememberPagerState(initialPage = anchor, pageCount = { virtualSpanMonths })
    val chartType = ChartType.CALENDAR

    fun pageToYearMonth(page: Int): YearMonth =
        initialYearMonth.plusMonths((page - anchor).toLong())

    // compute the page index that corresponds to *today's* YearMonth
    val todayYm = remember { YearMonth.now() }
    val initialPage = remember(initialYearMonth) {
        val delta = ChronoUnit.MONTHS.between(initialYearMonth, todayYm).toInt()
        anchor + delta
    }

    // jump pager to today's month on first composition
    LaunchedEffect(initialPage) {
        pagerState.scrollToPage(initialPage)
    }

    LaunchedEffect(pagerState.currentPage) {
        onMonthChanged?.invoke(pageToYearMonth(pagerState.currentPage))
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 0.dp,
        ) { page ->
            val pageYm = pageToYearMonth(page)
            val entries = entriesForMonth(pageYm)

            SingleMonthCalendarChart(
                modifier = Modifier.fillMaxSize(),
                entries = entries,
                yearMonth = pageYm,
                markerType = markerType,
                bubbleType = bubbleType,
                maxBubbleSize = maxBubbleSize,
                minBubbleSize = minBubbleSize,
                color = color
            )
        }
    }
}
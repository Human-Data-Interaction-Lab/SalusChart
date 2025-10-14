package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark
import kotlin.math.sqrt

object LineChartMath {

    /**
     * Compute non-overlapping label anchors (in canvas px) for a polyline.
     *
     * @param points   polyline points in canvas px
     * @param values   y-values (used only for text length heuristics)
     * @param canvas   canvas size in px (ChartMetrics.chartWidth + paddingX, chartHeight)
     * @param textPx   font size in px (e.g., 12.sp.toPx())
     * @param padPx    padding around label rects in px
     * @param minGapToLinePx minimum distance to the line (vertically) in px
     */
    fun computeLabelAnchors(
        points: List<Offset>,
        values: List<Float>,
        canvas: Size,
        textPx: Float = 12f,
        padPx: Float = 4f,
        minGapToLinePx: Float = 6f,
        passes: Int = 6,
        strokeWidthPx: Float = 4f,
        edgeMarginPx: Float = 8f
    ): List<Offset> {
        if (points.isEmpty()) return emptyList()

        // add default knobs
        val minPointClearPx = 8f          // vertical clearance from the polyline at the point
        val minNeighborXGapPx = 14f       // min horizontal gap between adjacent labels
        val strokeClearPx = 6f            // small horizontal push to escape a steep stroke
        val topEdgeClearPx = 10f          // make sure the text isn’t kissing the top

        // --- Helpers ---
        fun labelSizePx(value: Float): Size {
            // rough width estimation: ~0.6em per character
            val s = if (value % 1f == 0f) value.toInt().toString() else value.toString()
            val w = (s.length * textPx * 0.6f) + padPx * 2
            val h = textPx + padPx * 2
            return Size(w, h)
        }

        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

        // y on the polyline at an x between pi and pi+1
        fun polylineYAt(x: Float, pi: Int): Float {
            val p0 = points[pi]
            val p1 = points[pi + 1]
            val t = ((x - p0.x) / (p1.x - p0.x)).coerceIn(0f, 1f)
            return lerp(p0.y, p1.y, t)
        }

        fun clampToCanvas(anchor: Offset, sz: Size): Offset {
            val x = anchor.x.coerceIn(0f, canvas.width - sz.width)
            val y = anchor.y.coerceIn(0f, canvas.height - sz.height)
            return Offset(x, y)
        }

        // --- constants for placement heuristics ---
        val TOP_SAFE_CLEAR_PX = textPx * 2.2f + padPx * 2        // more vertical room near top

        // --- 1) Initial anchors (handle peaks/valleys & steep segments smartly) ---
        val sizes = values.map { labelSizePx(it) }
        val anchors = MutableList(points.size) { Offset.Zero }

        // knobs
        val aboveOffsetPx = textPx + 10f         // vertical gap above line
        val belowOffsetPx = textPx + 10f         // vertical gap below line
        val peakExtraPx   = 12f                   // extra gap on real peaks/valleys
        val steepDxPx     = 10f                  // neighborhood Δx under this → treat as near-vertical
        val sideGapX      = 4f                   // horizontal gap when placing at side

        fun slopeTo(i: Int): Float =
            when (i) {
                0 -> points[1].y - points[0].y
                points.lastIndex -> points[i].y - points[i - 1].y
                else -> (points[i + 1].y - points[i - 1].y) * 0.5f
            }

        for (i in points.indices) {
            val p  = points[i]
            val sz = sizes[i]

            // local slopes (dy) to detect peak/valley and “direction”
            val sPrev = when (i) {
                0 -> 0f
                else -> p.y - points[i - 1].y
            }
            val sNext = when (i) {
                points.lastIndex -> 0f
                else -> points[i + 1].y - p.y
            }

            // is this a peak or a valley?  (sign change)
            val isPeak   = (sPrev < 0f && sNext > 0f)
            val isValley = (sPrev > 0f && sNext < 0f)

            // neighborhood Δx to decide near-vertical
            val dxNeighborhood = when (i) {
                0 -> kotlin.math.abs(points[1].x - p.x)
                points.lastIndex -> kotlin.math.abs(p.x - points[i - 1].x)
                else -> kotlin.math.abs(points[i + 1].x - points[i - 1].x)
            }
            val nearVertical = dxNeighborhood < steepDxPx

            val upSlope = slopeTo(i) < 0f   // negative dy ⇒ line going up to the right

            val anchor: Offset =
                when {
                    // 1) Real peak/valley: force above/below with extra padding
                    isPeak -> {
                        val y = p.y - (aboveOffsetPx + peakExtraPx) - sz.height * 0.5f
                        Offset(p.x - sz.width * 0.5f, y)
                    }
                    isValley -> {
                        val y = p.y + (belowOffsetPx + peakExtraPx) - sz.height * 0.5f
                        Offset(p.x - sz.width * 0.5f, y)
                    }

                    // 2) Near-vertical neighborhood: place label to the side
                    nearVertical -> {
                        // go left if up-slope (so we avoid the stroke), right if down-slope
                        val dirX = if (upSlope) -1f else +1f
                        val x = p.x + dirX * (sz.width * 0.5f + sideGapX)
                        val y = p.y - sz.height * 0.5f   // center vertically on the point
                        Offset(x, y)
                    }

                    // 3) Normal case: above for up-slope, below for down-slope
                    else -> {
                        val dy = if (upSlope) -aboveOffsetPx else +belowOffsetPx
                        Offset(p.x - sz.width * 0.5f, p.y + dy - sz.height * 0.5f)
                    }
                }

            anchors[i] = clampToCanvas(anchor, sz)
        }

        // --- 2) N passes of collision resolution (vertical preferred) ---
        repeat(passes) {
            // a) keep distance from the polyline segment around each point
            for (i in 0 until points.lastIndex) {
                val left  = min(points[i].x, points[i + 1].x)
                val right = max(points[i].x, points[i + 1].x)
                for (j in max(0, i - 1)..min(points.lastIndex, i + 1)) {
                    val sz = sizes[j]
                    var r = Rect(anchors[j], Size(sz.width, sz.height))
                    if (r.right > left && r.left < right) {
                        val cx = r.center.x
                        val yOnLine = polylineYAt(cx, i)
                        val above = r.center.y < yOnLine

                        // desired center position at least minGap from the line
                        val desired = if (above) yOnLine - minGapToLinePx - r.height / 2f
                        else       yOnLine + minGapToLinePx + r.height / 2f

                        // tweak: if we're inside the top safe zone, prefer pushing DOWN
                        val needsDown = r.top < TOP_SAFE_CLEAR_PX

                        val move =
                            if (above && r.center.y > desired) desired - r.center.y
                            else if (!above && r.center.y < desired) desired - r.center.y
                            else 0f

                        val finalDy = if (needsDown && move < 0f) -move else move
                        if (finalDy != 0f) {
                            r = r.translate(Offset(0f, finalDy))
                            anchors[j] = clampToCanvas(Offset(r.left, r.top), sz)
                        }
                    }
                }
            }

            // b) pairwise push apart overlapping labels (prefer vertical separation)
            for (a in anchors.indices) {
                for (b in a + 1 until anchors.size) {
                    val ra = Rect(anchors[a], sizes[a])
                    val rb = Rect(anchors[b], sizes[b])
                    if (ra.overlaps(rb)) {
                        val overlapX = min(ra.right, rb.right) - max(ra.left, rb.left)
                        val overlapY = min(ra.bottom, rb.bottom) - max(ra.top, rb.top)
                        if (overlapY >= overlapX) {
                            val push = (overlapY / 2f) + 2f
                            anchors[a] = clampToCanvas(anchors[a].copy(y = ra.top - push), sizes[a])
                            anchors[b] = clampToCanvas(anchors[b].copy(y = rb.top + push), sizes[b])
                        } else {
                            val push = (overlapX / 2f) + 2f
                            anchors[a] = clampToCanvas(anchors[a].copy(x = ra.left - push), sizes[a])
                            anchors[b] = clampToCanvas(anchors[b].copy(x = rb.left + push), sizes[b])
                        }
                    }
                }
            }
        }

        // --- 3) Tiny heuristics for the last stubborn cases ---

        // Helper
        fun len(v: Offset) = kotlin.math.sqrt(v.x*v.x + v.y*v.y)
        fun clamp(a: Float, lo: Float, hi: Float) = a.coerceIn(lo, hi)
        fun isPeak(i: Int): Boolean =
            i in 1 until points.lastIndex && points[i].y < points[i-1].y && points[i].y < points[i+1].y
        fun isValley(i: Int): Boolean =
            i in 1 until points.lastIndex && points[i].y > points[i-1].y && points[i].y > points[i+1].y

        // 3a) Sharp peak/valley: place strictly above (for peak) or below (for valley)
        //     so the label never sits on top of the vertex (your “60” case).
        val peakClearance = (textPx + padPx * 2) + 6f        // px
        for (i in points.indices) {
            if (i in 1 until points.lastIndex) {
                val p = points[i]
                // angle at the vertex
                val v0 = points[i-1] - p
                val v1 = points[i+1] - p
                val denom = (len(v0) * len(v1)).takeIf { it > 0f } ?: 1f
                val cos = clamp((v0.x * v1.x + v0.y * v1.y) / denom, -1f, 1f)
                val angleDeg = Math.toDegrees(acos(cos).toDouble()).toFloat()

                // "sharp" if angle < ~70°
                if (angleDeg < 70f && (isPeak(i) || isValley(i))) {
                    val sz = sizes[i]
                    val y = if (isPeak(i)) {
                        // above the point
                        (p.y - peakClearance - sz.height * 0.5f)
                    } else {
                        // below the point
                        (p.y + peakClearance - sz.height * 0.5f)
                    }
                    val x = p.x - sz.width * 0.5f
                    anchors[i] = clampToCanvas(Offset(x, y), sz)
                }
            }
        }

        // 3b) Very steep adjacent segment: add a small horizontal nudge away from it
        //     so rising/descending edges don’t hide the label (your “35 next to a steep edge” case).
        val steepSlope = 1.6f   // |dy/dx| threshold (tune 1.4~2.0)
        val nudgeX = 8f         // px
        for (i in points.indices) {
            val sz = sizes[i]
            var ax = anchors[i].x
            val ay = anchors[i].y
            // check left segment
            if (i > 0) {
                val dx = (points[i].x - points[i-1].x).coerceAtLeast(1e-3f)
                val dy = points[i].y - points[i-1].y
                if (kotlin.math.abs(dy / dx) > steepSlope) {
                    // If segment goes up (canvas y decreases), nudge right; else nudge left.
                    ax += if (dy < 0f) nudgeX else -nudgeX
                }
            }
            // check right segment
            if (i < points.lastIndex) {
                val dx = (points[i+1].x - points[i].x).coerceAtLeast(1e-3f)
                val dy = points[i+1].y - points[i].y
                if (kotlin.math.abs(dy / dx) > steepSlope) {
                    ax += if (dy < 0f) nudgeX else -nudgeX
                }
            }
            anchors[i] = clampToCanvas(Offset(ax, ay), sz)
        }

        // return top-left anchors (PointMarker will place Text at these)
        return anchors
    }

    /**
     * 라인 차트용 데이터 포인트를 화면 좌표로 변환합니다.
     * 인덱스 기반 X축 포지셔닝 (일대일 매핑)
     *
     * @param data 차트 데이터 포인트 목록
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @return 화면 좌표로 변환된 Offset 목록
     */
    fun mapLineToCanvasPoints(data: List<ChartMark>, size: Size, metrics: ChartMath.ChartMetrics): List<Offset> {
        val spacing = metrics.chartWidth / (data.size - 1)
        return data.mapIndexed { i, point ->
            val x = metrics.paddingX + i * spacing
            val y = metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight
            Offset(x, y)
        }
    }
}

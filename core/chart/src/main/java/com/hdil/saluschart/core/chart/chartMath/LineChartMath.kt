package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Math utilities for line charts.
 *
 * This object provides:
 * - Mapping line chart data points to canvas coordinates (index-based X positioning)
 * - Optional label-anchor computation for value labels along a polyline, attempting to avoid
 *   overlaps and keep labels away from the line (used by some chart renderers)
 *
 * Notes:
 * - Point mapping uses "slot-center" positioning:
 *   `x = paddingX + (i + 0.5) * (chartWidth / N)`.
 * - Label anchoring is a heuristic-based algorithm intended for readability rather than perfect
 *   optimal packing.
 */
object LineChartMath {

    // TODO: 이거 필요한지? (과거에 핋요 없다고 판단되어서 삭제했었음, 누가 다시 만들었지)
    /**
     * Computes non-overlapping label anchors (top-left positions in canvas px) for a polyline.
     *
     * The algorithm:
     * 1) Produces an initial placement (above/below/side) based on local slope and peak/valley
     * 2) Runs multiple passes to:
     *    - keep labels away from the polyline
     *    - resolve label-to-label overlaps (preferring vertical separation)
     * 3) Applies additional heuristics for sharp peaks/valleys and steep segments
     *
     * Returned anchors represent the **top-left** of each label rect. Callers should place the
     * Text at that top-left position.
     *
     * @param points Polyline points in canvas px.
     * @param values Y-values corresponding to points (used to estimate label width).
     * @param canvas Canvas size in px.
     * @param textPx Font size in px (e.g., 12.sp.toPx()).
     * @param padPx Padding around label rects in px.
     * @param minGapToLinePx Minimum distance to the line (vertically) in px.
     * @param passes Number of collision-resolution passes.
     * @param strokeWidthPx Stroke width in px (currently unused; kept for signature stability).
     * @param edgeMarginPx Edge margin in px (currently unused; kept for signature stability).
     * @return List of label anchors (top-left) in canvas px.
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

        // --- Helpers ---
        fun labelSizePx(value: Float): Size {
            // Rough width estimation: ~0.6em per character
            val s = if (value % 1f == 0f) value.toInt().toString() else value.toString()
            val w = (s.length * textPx * 0.6f) + padPx * 2
            val h = textPx + padPx * 2
            return Size(w, h)
        }

        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

        // y on the polyline at an x between points[pi] and points[pi+1]
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

        // --- placement constants ---
        val TOP_SAFE_CLEAR_PX = textPx * 2.2f + padPx * 2

        // --- 1) Initial anchors (peaks/valleys/steep segments) ---
        val sizes = values.map { labelSizePx(it) }
        val anchors = MutableList(points.size) { Offset.Zero }

        val aboveOffsetPx = textPx + 10f
        val belowOffsetPx = textPx + 10f
        val peakExtraPx = 12f
        val steepDxPx = 10f
        val sideGapX = 4f

        fun slopeTo(i: Int): Float =
            when (i) {
                0 -> points[1].y - points[0].y
                points.lastIndex -> points[i].y - points[i - 1].y
                else -> (points[i + 1].y - points[i - 1].y) * 0.5f
            }

        for (i in points.indices) {
            val p = points[i]
            val sz = sizes[i]

            val sPrev = if (i == 0) 0f else p.y - points[i - 1].y
            val sNext = if (i == points.lastIndex) 0f else points[i + 1].y - p.y

            val isPeak = (sPrev < 0f && sNext > 0f)
            val isValley = (sPrev > 0f && sNext < 0f)

            val dxNeighborhood = when (i) {
                0 -> kotlin.math.abs(points[1].x - p.x)
                points.lastIndex -> kotlin.math.abs(p.x - points[i - 1].x)
                else -> kotlin.math.abs(points[i + 1].x - points[i - 1].x)
            }
            val nearVertical = dxNeighborhood < steepDxPx

            // negative dy => line going up to the right (canvas y decreases)
            val upSlope = slopeTo(i) < 0f

            val anchor: Offset =
                when {
                    isPeak -> {
                        val y = p.y - (aboveOffsetPx + peakExtraPx) - sz.height * 0.5f
                        Offset(p.x - sz.width * 0.5f, y)
                    }

                    isValley -> {
                        val y = p.y + (belowOffsetPx + peakExtraPx) - sz.height * 0.5f
                        Offset(p.x - sz.width * 0.5f, y)
                    }

                    nearVertical -> {
                        val dirX = if (upSlope) -1f else +1f
                        val x = p.x + dirX * (sz.width * 0.5f + sideGapX)
                        val y = p.y - sz.height * 0.5f
                        Offset(x, y)
                    }

                    else -> {
                        val dy = if (upSlope) -aboveOffsetPx else +belowOffsetPx
                        Offset(p.x - sz.width * 0.5f, p.y + dy - sz.height * 0.5f)
                    }
                }

            anchors[i] = clampToCanvas(anchor, sz)
        }

        // --- 2) Collision resolution passes ---
        repeat(passes) {
            // a) keep distance from polyline around each segment
            for (i in 0 until points.lastIndex) {
                val left = min(points[i].x, points[i + 1].x)
                val right = max(points[i].x, points[i + 1].x)

                for (j in max(0, i - 1)..min(points.lastIndex, i + 1)) {
                    val sz = sizes[j]
                    var r = Rect(anchors[j], Size(sz.width, sz.height))

                    if (r.right > left && r.left < right) {
                        val cx = r.center.x
                        val yOnLine = polylineYAt(cx, i)
                        val above = r.center.y < yOnLine

                        val desired =
                            if (above) yOnLine - minGapToLinePx - r.height / 2f
                            else yOnLine + minGapToLinePx + r.height / 2f

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

            // b) push apart overlapping labels (prefer vertical separation)
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

        // --- 3) Extra heuristics for stubborn cases ---
        fun len(v: Offset) = sqrt(v.x * v.x + v.y * v.y)
        fun clamp(a: Float, lo: Float, hi: Float) = a.coerceIn(lo, hi)

        fun isPeak(i: Int): Boolean =
            i in 1 until points.lastIndex && points[i].y < points[i - 1].y && points[i].y < points[i + 1].y

        fun isValley(i: Int): Boolean =
            i in 1 until points.lastIndex && points[i].y > points[i - 1].y && points[i].y > points[i + 1].y

        // 3a) Sharp peak/valley: force strictly above/below.
        val peakClearance = (textPx + padPx * 2) + 6f
        for (i in points.indices) {
            if (i in 1 until points.lastIndex) {
                val p = points[i]

                val v0 = points[i - 1] - p
                val v1 = points[i + 1] - p
                val denom = (len(v0) * len(v1)).takeIf { it > 0f } ?: 1f

                val cosV = clamp((v0.x * v1.x + v0.y * v1.y) / denom, -1f, 1f)
                val angleDeg = Math.toDegrees(acos(cosV).toDouble()).toFloat()

                if (angleDeg < 70f && (isPeak(i) || isValley(i))) {
                    val sz = sizes[i]
                    val y = if (isPeak(i)) {
                        p.y - peakClearance - sz.height * 0.5f
                    } else {
                        p.y + peakClearance - sz.height * 0.5f
                    }
                    val x = p.x - sz.width * 0.5f
                    anchors[i] = clampToCanvas(Offset(x, y), sz)
                }
            }
        }

        // 3b) Very steep adjacent segment: nudge horizontally away from it.
        val steepSlope = 1.6f
        val nudgeX = 8f
        for (i in points.indices) {
            val sz = sizes[i]
            var ax = anchors[i].x
            val ay = anchors[i].y

            if (i > 0) {
                val dx = (points[i].x - points[i - 1].x).coerceAtLeast(1e-3f)
                val dy = points[i].y - points[i - 1].y
                if (kotlin.math.abs(dy / dx) > steepSlope) {
                    ax += if (dy < 0f) nudgeX else -nudgeX
                }
            }

            if (i < points.lastIndex) {
                val dx = (points[i + 1].x - points[i].x).coerceAtLeast(1e-3f)
                val dy = points[i + 1].y - points[i].y
                if (kotlin.math.abs(dy / dx) > steepSlope) {
                    ax += if (dy < 0f) nudgeX else -nudgeX
                }
            }

            anchors[i] = clampToCanvas(Offset(ax, ay), sz)
        }

        return anchors
    }

    /**
     * Maps line chart data points to canvas coordinates (px) using index-based positioning.
     *
     * X positioning:
     * - Points are distributed evenly across [metrics.chartWidth] in `N` slots and centered
     *   within each slot: `(i + 0.5) * spacing`.
     *
     * Y positioning:
     * - Values are normalized to the range [metrics.minY]..[metrics.maxY] and mapped to screen
     *   coordinates with the origin at the top.
     *
     * @param data Line chart points.
     * @param size Canvas size (currently unused; kept for API stability).
     * @param metrics Chart metrics (padding, width/height, Y range).
     * @return A list of [Offset] positions in canvas coordinates.
     */
    fun mapLineToCanvasPoints(
        data: List<ChartMark>,
        size: Size,
        metrics: ChartMath.ChartMetrics
    ): List<Offset> {
        val total = data.size
        if (total == 0) return emptyList()

        val spacing = metrics.chartWidth / total

        return data.mapIndexed { i, point ->
            val x = metrics.paddingX + (i + 0.5f) * spacing

            val normalized = ((point.y - metrics.minY) / (metrics.maxY - metrics.minY))
                .toFloat()
                .coerceIn(0f, 1f)

            val y = metrics.paddingY + metrics.chartHeight * (1f - normalized)

            Offset(x, y)
        }
    }
}
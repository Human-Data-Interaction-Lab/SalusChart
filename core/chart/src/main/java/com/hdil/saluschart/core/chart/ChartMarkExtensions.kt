package com.hdil.saluschart.core.chart

/**
 * Extension utilities for transforming a list of [ChartMark] into specialized mark types.
 *
 * These helpers are intended to make it easy to reuse the same base data structure ([ChartMark])
 * and convert it into:
 * - [RangeChartMark] for range bar charts (min/max span)
 * - [StackedChartMark] for stacked bar charts (multiple segments per x-position)
 *
 * Notes:
 * - Most transforms group by [ChartMark.x] and then build one output mark per x-group.
 * - Output is sorted by x ascending for stable rendering.
 */

/**
 * Transforms a list of [ChartMark] into [RangeChartMark] by grouping marks with the same `x`.
 *
 * For each x-group:
 * - [minValueSelector] chooses the min point (defaults to the smallest y in the group)
 * - [maxValueSelector] chooses the max point (defaults to the largest y in the group)
 * - The resulting [RangeChartMark.label] is taken from the first element in the group (if any)
 *
 * @param minValueSelector Chooses the "min" mark within an x-group.
 * @param maxValueSelector Chooses the "max" mark within an x-group.
 * @return One [RangeChartMark] per distinct x, sorted by x ascending.
 */
fun List<ChartMark>.toRangeChartMarksByXGroup(
    minValueSelector: (List<ChartMark>) -> ChartMark = { group ->
        group.minByOrNull { it.y } ?: group.first()
    },
    maxValueSelector: (List<ChartMark>) -> ChartMark = { group ->
        group.maxByOrNull { it.y } ?: group.first()
    }
): List<RangeChartMark> {
    if (isEmpty()) return emptyList()

    return groupBy { it.x }
        .map { (x, group) ->
            val minPoint = minValueSelector(group)
            val maxPoint = maxValueSelector(group)

            RangeChartMark(
                x = x,
                minPoint = minPoint,
                maxPoint = maxPoint,
                label = group.firstOrNull()?.label
            )
        }
        .sortedBy { it.x }
}

/**
 * Transforms a list of [ChartMark] into [StackedChartMark] by grouping marks with the same `x`.
 *
 * For each x-group:
 * - [segmentOrdering] determines the order of the stacked segments (defaults to y descending)
 * - The resulting [StackedChartMark.label] is taken from the first element in the group (if any)
 *
 * @param segmentOrdering Orders the segments within an x-group.
 * @return One [StackedChartMark] per distinct x, sorted by x ascending.
 */
fun List<ChartMark>.toStackedChartMarks(
    segmentOrdering: (List<ChartMark>) -> List<ChartMark> = { group ->
        group.sortedByDescending { it.y }
    }
): List<StackedChartMark> {
    if (isEmpty()) return emptyList()

    return groupBy { it.x }
        .map { (x, group) ->
            val segments = segmentOrdering(group)

            StackedChartMark(
                x = x,
                segments = segments,
                label = group.firstOrNull()?.label
            )
        }
        .sortedBy { it.x }
}

/**
 * Convenience transform for building [RangeChartMark] from sequential pairs.
 *
 * Assumes the list is structured as pairs:
 * `min1, max1, min2, max2, ...`
 *
 * If the last chunk is incomplete (size != 2), it is ignored.
 *
 * Note: This helper is currently not used by the library, but is kept as an optional utility.
 *
 * @return List of [RangeChartMark] created from each (min, max) pair.
 */
fun List<ChartMark>.toRangeChartMarksFromPairs(): List<RangeChartMark> {
    if (isEmpty()) return emptyList()

    return chunked(2).mapNotNull { pair ->
        if (pair.size == 2) {
            val (minPoint, maxPoint) = pair
            RangeChartMark(
                x = minPoint.x,
                minPoint = minPoint,
                maxPoint = maxPoint,
                label = minPoint.label
            )
        } else null
    }
}

/**
 * Convenience transform for building [StackedChartMark] from fixed-size groups.
 *
 * Assumes the list is already grouped by stack, in order:
 * e.g., with `groupSize = 3`:
 * `(seg1, seg2, seg3), (seg1, seg2, seg3), ...`
 *
 * If a chunk is incomplete (size != [groupSize]), it is ignored.
 *
 * Note: This helper is currently not used by the library, but is kept as an optional utility.
 *
 * @param groupSize Number of segments per stack.
 * @return List of [StackedChartMark] created from each group.
 */
fun List<ChartMark>.toStackedChartMarksFromGroups(groupSize: Int): List<StackedChartMark> {
    if (isEmpty()) return emptyList()

    return chunked(groupSize).mapNotNull { group ->
        if (group.size == groupSize) {
            val x = group.first().x
            val label = group.first().label

            StackedChartMark(
                x = x,
                segments = group,
                label = label
            )
        } else null
    }
}
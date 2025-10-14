package com.hdil.saluschart.core.chart

/**
 * ChartMark transformation utilities for converting to specialized chart types
 */

/**
 * Transforms a list of ChartMarks into RangeChartMarks
 * Groups ChartMarks by x-value and creates min/max pairs
 *
 * @param ChartMarks List of ChartMarks to transform
 * @param minValueSelector Function to select the minimum value from a group
 * @param maxValueSelector Function to select the maximum value from a group
 * @return List of RangeChartMarks
 */
fun List<ChartMark>.toRangeChartMarks(
    minValueSelector: (List<ChartMark>) -> ChartMark = { group -> group.minByOrNull { it.y } ?: group.first() },
    maxValueSelector: (List<ChartMark>) -> ChartMark = { group -> group.maxByOrNull { it.y } ?: group.first() }
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
 * Transforms a list of ChartMarks into StackedChartMarks
 * Groups ChartMarks by x-value and creates stacked segments
 *
 * @param ChartMarks List of ChartMarks to transform
 * @param segmentOrdering Function to determine the order of segments (default: by y-value descending)
 * @return List of StackedChartMarks
 */
fun List<ChartMark>.toStackedChartMarks(
    segmentOrdering: (List<ChartMark>) -> List<ChartMark> = { group -> group.sortedByDescending { it.y } }
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
 * Convenience function for creating RangeChartMarks from paired ChartMarks
 * Assumes ChartMarks are already paired (min, max) in sequence
 *
 * @param ChartMarks List of ChartMarks (should be in pairs: min1, max1, min2, max2, ...)
 * @return List of RangeChartMarks
 */
fun List<ChartMark>.toRangeChartMarksFromPairs(): List<RangeChartMark> {
    if (isEmpty()) return emptyList()

    return chunked(2).mapNotNull { pair ->
        if (pair.size == 2) {
            val (minPoint, maxPoint) = pair
            RangeChartMark(
                x = minPoint.x, // Use the x-value from the first point
                minPoint = minPoint,
                maxPoint = maxPoint,
                label = minPoint.label
            )
        } else null
    }
}

/**
 * Convenience function for creating StackedChartMarks from grouped ChartMarks
 * Assumes ChartMarks are already grouped by category (e.g., protein, fat, carbs)
 *
 * @param ChartMarks List of ChartMarks grouped by category
 * @param groupSize Number of segments per stack (e.g., 3 for protein, fat, carbs)
 * @return List of StackedChartMarks
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
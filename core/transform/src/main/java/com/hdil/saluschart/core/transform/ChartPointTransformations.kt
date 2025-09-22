package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.StackedChartPoint

/**
 * ChartPoint transformation utilities for converting to specialized chart types
 */

/**
 * Transforms a list of ChartPoints into RangeChartPoints
 * Groups ChartPoints by x-value and creates min/max pairs
 * 
 * @param chartPoints List of ChartPoints to transform
 * @param minValueSelector Function to select the minimum value from a group
 * @param maxValueSelector Function to select the maximum value from a group
 * @return List of RangeChartPoints
 */
fun List<ChartPoint>.toRangeChartPoints(
    minValueSelector: (List<ChartPoint>) -> ChartPoint = { group -> group.minByOrNull { it.y } ?: group.first() },
    maxValueSelector: (List<ChartPoint>) -> ChartPoint = { group -> group.maxByOrNull { it.y } ?: group.first() }
): List<RangeChartPoint> {
    if (isEmpty()) return emptyList()
    
    return groupBy { it.x }
        .map { (x, group) ->
            val minPoint = minValueSelector(group)
            val maxPoint = maxValueSelector(group)
            
            RangeChartPoint(
                x = x,
                minPoint = minPoint,
                maxPoint = maxPoint,
                label = group.firstOrNull()?.label
            )
        }
        .sortedBy { it.x }
}

/**
 * Transforms a list of ChartPoints into StackedChartPoints
 * Groups ChartPoints by x-value and creates stacked segments
 * 
 * @param chartPoints List of ChartPoints to transform
 * @param segmentOrdering Function to determine the order of segments (default: by y-value descending)
 * @return List of StackedChartPoints
 */
fun List<ChartPoint>.toStackedChartPoints(
    segmentOrdering: (List<ChartPoint>) -> List<ChartPoint> = { group -> group.sortedByDescending { it.y } }
): List<StackedChartPoint> {
    if (isEmpty()) return emptyList()
    
    return groupBy { it.x }
        .map { (x, group) ->
            val segments = segmentOrdering(group)
            
            StackedChartPoint(
                x = x,
                segments = segments,
                label = group.firstOrNull()?.label
            )
        }
        .sortedBy { it.x }
}

/**
 * Convenience function for creating RangeChartPoints from paired ChartPoints
 * Assumes ChartPoints are already paired (min, max) in sequence
 * 
 * @param chartPoints List of ChartPoints (should be in pairs: min1, max1, min2, max2, ...)
 * @return List of RangeChartPoints
 */
fun List<ChartPoint>.toRangeChartPointsFromPairs(): List<RangeChartPoint> {
    if (isEmpty()) return emptyList()
    
    return chunked(2).mapNotNull { pair ->
        if (pair.size == 2) {
            val (minPoint, maxPoint) = pair
            RangeChartPoint(
                x = minPoint.x, // Use the x-value from the first point
                minPoint = minPoint,
                maxPoint = maxPoint,
                label = minPoint.label
            )
        } else null
    }
}

/**
 * Convenience function for creating StackedChartPoints from grouped ChartPoints
 * Assumes ChartPoints are already grouped by category (e.g., protein, fat, carbs)
 * 
 * @param chartPoints List of ChartPoints grouped by category
 * @param groupSize Number of segments per stack (e.g., 3 for protein, fat, carbs)
 * @return List of StackedChartPoints
 */
fun List<ChartPoint>.toStackedChartPointsFromGroups(groupSize: Int): List<StackedChartPoint> {
    if (isEmpty()) return emptyList()
    
    return chunked(groupSize).mapNotNull { group ->
        if (group.size == groupSize) {
            val x = group.first().x
            val label = group.first().label
            
            StackedChartPoint(
                x = x,
                segments = group,
                label = label
            )
        } else null
    }
}


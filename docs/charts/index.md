# Charts Overview

SalusChart provides chart composables in two tiers:

- **Full charts** — titled, axes, legends, tooltips, paging/scrolling
- **Minimal charts** — compact, card-sized, no title or axes; suitable for dashboards

## Full charts

| Composable | Data type | Description |
|---|---|---|
| [BarChart](./bar-chart) | `List<ChartMark>` | Vertical bars |
| [LineChart](./line-chart) | `List<ChartMark>` | Connected line plot |
| [ScatterPlot](./scatter-plot) | `List<ChartMark>` | Individual data points |
| [RangeBarChart](./range-bar-chart) | `List<RangeChartMark>` | Min–max vertical bars |
| [HorizontalRangeBarChart](./horizontal-charts#horizontalrangebarchart) | `List<RangeChartMark>` | Min–max horizontal bars |
| [StackedBarChart](./stacked-bar-chart) | `List<ChartMark>` | Stacked segments per x |
| [HorizontalStackedBarChartList](./horizontal-charts#horizontalstackedbarchartlist) | custom | Horizontal stacked rows |
| [PieChart](./pie-chart) | `List<ChartMark>` | Pie or donut |
| [ProgressChart](./progress-chart) | `List<ProgressChartMark>` | Progress rings or bars |
| [SleepStageChart](./sleep-stage-chart) | `SleepSession` | Sleep stage timeline |
| [CalendarChart](./calendar-chart) | `List<CalendarEntry>` | Monthly heatmap |
| [PagedCalendarChart](./calendar-chart#pagedcalendarchart) | `List<CalendarEntry>` | Multi-month paged calendar |
| [MultiSegmentGaugeChart](./gauge-charts#multisegmentgaugechart) | `Float?` | Segmented gauge bar |
| [RangeGaugeChart](./gauge-charts#rangegaugechart) | range values | Gauge with min–max range |
| [MiniActivityRings](./minimal-charts#miniactivityrings) | `List<ProgressChartMark>` | Apple-style activity rings |

## Minimal charts

| Composable | Description |
|---|---|
| [MinimalBarChart](./minimal-charts#minimalbar) | Compact bar chart |
| [MinimalLineChart](./minimal-charts#minimalline) | Compact line chart |
| [MinimalRangeBarChart](./minimal-charts#minimalrangebar) | Compact range bar |
| [MinimalProgressBar](./minimal-charts#minimalprogress) | Single progress bar |
| [MinimalGaugeChart](./minimal-charts#minimalgauge) | Compact gauge |
| [MinimalMultiSegmentGauge](./minimal-charts#minimalmultisegment) | Compact segmented gauge |
| [MinimalSleepChart](./minimal-charts#minimalsleep) | Compact sleep timeline |
| [MinimalSleepStageChart](./minimal-charts#minimalsleepstage) | Compact sleep stage |
| [MinimalHorizontalStackedBar](./minimal-charts#minimalhorizontalstacked) | Compact stacked row |
| [MinimalLadderChart](./minimal-charts#minimalladder) | Compact ladder chart |

## Wear OS charts

See [Wear OS Charts](./wear-os-charts) for the `ui:wear-compose` module.

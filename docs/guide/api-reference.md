# API Reference

## Hosted API Reference

Hosted API reference is not available yet. Generate it locally with [Dokka](https://kotlinlang.org/docs/dokka-introduction.html).

This repository does not currently apply a Dokka Gradle plugin, so there is no checked-in `dokkaHtml` or `dokkaHtmlMultiModule` task to run yet. After Dokka is configured for the project, use the Gradle task name produced by that configuration to generate the local API site.

Track hosted API reference progress in the [SalusChart repository](https://github.com/HDIL-YS/SalusChart).

## Source reference

The chart composable signatures are documented inline with KDoc. To browse them:

| Module | Source path |
|---|---|
| ui:compose | `ui/compose/src/main/java/com/hdil/saluschart/ui/compose/charts/` |
| core:chart | `core/chart/src/main/java/com/hdil/saluschart/core/chart/` |
| data:model | `data/model/src/main/java/com/hdil/saluschart/data/model/` |
| ui:wear-compose | `ui/wear-compose/src/main/java/com/hdil/saluschart/ui/wear/compose/` |

## Key types quick reference

### Marks

| Type | Module | Use |
|---|---|---|
| `ChartMark` | `core:chart` | Single x/y point |
| `RangeChartMark` | `core:chart` | Min–max range at x |
| `ProgressChartMark` | `core:chart` | current/max progress item |
| `CalendarEntry` | `ui:compose` | One calendar day |
| `GaugeSegment` | `ui:compose` | Colored zone in gauge |

### Enums

| Type | Values |
|---|---|
| `InteractionType.Bar` | `BAR`, `TOUCH_AREA` |
| `InteractionType.Line` | `POINT`, `TOUCH_AREA` |
| `InteractionType.RangeBar` | `BAR`, `TOUCH_AREA` |
| `LegendPosition` | `TOP`, `BOTTOM`, `LEFT`, `RIGHT` |
| `YAxisPosition` | `LEFT`, `RIGHT` |
| `PointType` | `CIRCLE`, `SQUARE` |
| `CellMarkerType` | `BUBBLE`, `MINI_RINGS` |
| `BubbleType` | `CIRCLE`, `RECTANGLE` |
| `SleepStageType` | `AWAKE`, `REM`, `LIGHT`, `DEEP`, `UNKNOWN` |
| `AggregationType` | `SUM`, `AVERAGE`, `DURATION_SUM`, `MIN`, `MAX` |
| `TimeUnitGroup` | `HOUR`, `DAY`, `WEEK`, `MONTH` |
| `MassUnit` | `KILOGRAM`, `POUND`, `GRAM`, `OUNCE` |

# API Reference

## Generate API Reference

API reference is generated with [Dokka](https://kotlinlang.org/docs/dokka-introduction.html).

## Browse API Reference

[Open generated API reference](../api/index.html)

The API reference is served from `docs/public/api` in the VitePress site. Regenerate it after changing public Kotlin APIs.

Generate the multi-module API site locally:

```bash
./gradlew syncDokkaToVitePress
```

This task runs Dokka and copies `build/dokka/html` into `docs/public/api`.

## Documented modules

Dokka is configured for the public library modules:

- `:core:chart`
- `:core:transform`
- `:core:util`
- `:data:model`
- `:ui:compose`
- `:ui:theme`
- `:ui:wear-compose`

Demo and sample app modules are intentionally excluded from API reference generation.

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
| `AggregationType` | `SUM`, `DAILY_AVERAGE`, `DURATION_SUM`, `MIN_MAX` |
| `TimeUnitGroup` | `MINUTE`, `HOUR`, `DAY`, `WEEK`, `MONTH`, `YEAR` |
| `MassUnit` | `KILOGRAM`, `POUND`, `GRAM`, `OUNCE` |

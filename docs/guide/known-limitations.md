# Known Limitations

These notes describe current API boundaries and behavior to account for when building production screens.

## Versioning

Use the same SalusChart version for every artifact. Mixing versions is not recommended because modules share chart mark types, transform helpers, and drawing contracts.

See [Releases](./releases) for the latest version and Maven Central links.

## API reference

Dokka API generation is configured and can be run locally with `./gradlew dokkaGenerate`.

The generated HTML is not committed to the repository. Publish the generated `build/dokka/html` output alongside the VitePress site when hosted API docs are needed.

See [API Reference](./api-reference).

## Interaction callbacks

Not every chart exposes the same external callback surface.

| Feature | Current behavior |
|---|---|
| Built-in tooltips | Available on standard interactive charts |
| `onBarClick` | Available on `BarChart`, `RangeBarChart`, and `StackedBarChart` |
| `onRowClick` | Available on `HorizontalStackedBarChartList` |
| `onStageClick` | Available on `SleepStageChart` |
| `ReferenceLineSpec.onClick` | Available for interactive reference lines |
| External callbacks for `LineChart` / `ScatterPlot` point selection | Not exposed as public callbacks yet |

For coordinated dashboards, keep selection state in the parent when the chart exposes a callback. For charts without a public callback, use built-in tooltip behavior or build coordination around a surrounding control.

## Paging and scrolling

`windowSize` and `pageSize` are mutually exclusive. Passing both to supported charts throws an `IllegalArgumentException`.

Paging and scrolling are intended for time-series chart families such as bar, line, scatter, stacked bar, and range bar charts. Compact minimal charts and some specialized charts are designed for fixed-size summaries instead.

## Sparse health data

Sparse measurements should usually avoid gap filling. Filling missing values with zero can distort charts and min/max ranges.

Prefer `fillGaps = false` for:

- weight
- body fat
- skeletal muscle mass
- blood pressure
- blood glucose
- heart rate min/max ranges

Use `fillGaps = true` for dense activity data such as steps or exercise totals when a missing bucket should render as zero.

## Platform integrations

SalusChart does not request platform permissions or read health data directly. Apps are responsible for reading from Apple Health, Samsung Health, Wear OS, Health Connect, or a backend, then mapping records into `data:model`.

See [Platform Integrations](./platform-integrations).

## Wear OS

Wear OS charts are optimized for small circular screens, but not every phone chart has a one-to-one Wear equivalent. Prefer `Wear*` and `WearMinimal*` components for watch screens, and avoid long axis labels on small displays.

See [Wear OS Charts](../charts/wear-os-charts).

## Custom drawing internals

Parameters not exposed by the public API, such as some internal layout constants or drawing primitives, cannot be changed at runtime. If a screen needs behavior outside the exposed parameters, open an issue or fork the component.

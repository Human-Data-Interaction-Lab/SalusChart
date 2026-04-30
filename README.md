# SalusChart

A modular Jetpack Compose charting library for Android health apps.

**Latest version:** `0.1.8` — [Maven Central](https://central.sonatype.com/search?q=io.github.hdilys)

**[→ Documentation](https://hdil-ys.github.io/SalusChart/)**

## Quick install

```kotlin
// settings.gradle.kts
repositories { mavenCentral() }

// build.gradle.kts
dependencies {
    val salusVersion = "0.1.8"
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-ui-theme:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Chart types

20+ chart composables including bar, line, range bar, scatter, pie, progress rings, sleep stage, calendar heatmap, gauge, stacked bar, and compact minimal variants. Wear OS variants available in a separate module.

See the [chart reference](https://hdil-ys.github.io/SalusChart/charts/) for the full list.

## Modules

| Artifact | Purpose |
|---|---|
| `saluschart-ui-compose` | All chart composables |
| `saluschart-ui-theme` | `LocalSalusChartColors` theming |
| `saluschart-core-chart` | Chart math and draw primitives |
| `saluschart-core-transform` | Data aggregation |
| `saluschart-core-util` | Aggregation and time-unit enums |
| `saluschart-data-model` | Health data models |
| `saluschart-ui-wear-compose` | Wear OS charts |

## Demo

Clone and run the `:app` module:

```bash
./gradlew :app:installDebug
```

## Development

```bash
./gradlew build
./gradlew publishToMavenLocal   # test locally before publishing
```

## License

[Apache License 2.0](LICENSE)

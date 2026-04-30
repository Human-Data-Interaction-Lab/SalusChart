# Installation

SalusChart is published to **Maven Central**. All modules share the same version.

See [Releases](./releases) for the latest version, Maven Central links, and release notes.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hdilys/saluschart-ui-compose.svg)](https://central.sonatype.com/artifact/io.github.hdilys/saluschart-ui-compose)

In the examples below, replace `<latest>` with the latest SalusChart version:

```kotlin
val salusVersion = "<latest>"
```

## Modules

| Artifact | Purpose |
|---|---|
| `saluschart-ui-compose` | All Jetpack Compose chart composables |
| `saluschart-ui-theme` | `LocalSalusChartColors` theming system |
| `saluschart-core-chart` | Chart math, draw primitives, data marks |
| `saluschart-core-transform` | Data aggregation and transformation |
| `saluschart-core-util` | `AggregationType`, `TimeUnitGroup` enums |
| `saluschart-data-model` | Health data models (`SleepSession`, `Mass`, etc.) |

> `saluschart-ui-wear-compose` is the Wear OS module. See [Wear OS Charts](../charts/wear-os-charts) for setup.

## Recommended setup

For most apps, add these three:

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-ui-theme:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-transform:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-util:$salusVersion")
}
```

## Full setup (all modules)

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-ui-theme:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-chart:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-transform:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-util:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Wear OS

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-wear-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Version catalog

```toml
# gradle/libs.versions.toml
[versions]
saluschart = "<latest>"

[libraries]
saluschart-compose = { module = "io.github.hdilys:saluschart-ui-compose", version.ref = "saluschart" }
saluschart-theme   = { module = "io.github.hdilys:saluschart-ui-theme",   version.ref = "saluschart" }
saluschart-model   = { module = "io.github.hdilys:saluschart-data-model", version.ref = "saluschart" }
saluschart-transform = { module = "io.github.hdilys:saluschart-core-transform", version.ref = "saluschart" }
saluschart-util = { module = "io.github.hdilys:saluschart-core-util", version.ref = "saluschart" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.saluschart.compose)
    implementation(libs.saluschart.theme)
    implementation(libs.saluschart.model)
    implementation(libs.saluschart.transform)
    implementation(libs.saluschart.util)
}
```

## Requirements

- Android minSdk 30
- Kotlin 1.9+
- Jetpack Compose BOM 2024.x or later

## Versioning

Use one `salusVersion` value for all SalusChart modules. Mixing module versions is not recommended because chart marks, transform helpers, and UI components share internal model contracts.

See [Known Limitations](./known-limitations) for current API boundaries.

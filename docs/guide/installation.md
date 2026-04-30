# Installation

SalusChart is published to **Maven Central**. All modules share the same version.

**Current version:** `0.1.6`

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
    val salusVersion = "0.1.6"
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-ui-theme:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Full setup (all modules)

```kotlin
dependencies {
    val salusVersion = "0.1.6"
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
    val salusVersion = "0.1.6"
    implementation("io.github.hdilys:saluschart-ui-wear-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Version catalog

```toml
# gradle/libs.versions.toml
[versions]
saluschart = "0.1.6"

[libraries]
saluschart-compose = { module = "io.github.hdilys:saluschart-ui-compose", version.ref = "saluschart" }
saluschart-theme   = { module = "io.github.hdilys:saluschart-ui-theme",   version.ref = "saluschart" }
saluschart-model   = { module = "io.github.hdilys:saluschart-data-model", version.ref = "saluschart" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.saluschart.compose)
    implementation(libs.saluschart.theme)
    implementation(libs.saluschart.model)
}
```

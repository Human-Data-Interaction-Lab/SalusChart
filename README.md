# SalusChart

A modular Jetpack Compose charting library (health-focused) for Android.

**Latest version:** `0.1.0`

## Modules (published)

These artifacts are published to Maven Central:

- `io.github.hdilys:saluschart-core-chart:0.1.0`
- `io.github.hdilys:saluschart-core-transform:0.1.0`
- `io.github.hdilys:saluschart-core-util:0.1.0`
- `io.github.hdilys:saluschart-data-model:0.1.0`

## Installation

Add Maven Central (usually already present):

```kotlin
repositories {
    mavenCentral()
}
```

Add dependencies:

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-core-chart:0.1.0")
    implementation("io.github.hdilys:saluschart-core-transform:0.1.0")
    implementation("io.github.hdilys:saluschart-core-util:0.1.0")
    implementation("io.github.hdilys:saluschart-data-model:0.1.0")
}
```

## Quickstart

```kotlin
// Example dependency usage (pseudo)
val data: List<ChartMark> = ...
```

## Demo / Sample

This repository also contains a demo app (:app) and a sample module (:sample) for showcasing usage.
They are not published to Maven Central.

## Development

- Build: ./gradlew build
- Run the demo: open in Android Studio and run :app

## License

Apache License 2.0.

# Examples

Practical, copy-pasteable chart examples organized by scenario.

## In this section

- [Mobile Dashboard](./mobile-dashboard) — combining multiple charts in a health dashboard screen
- [Wear OS](./wear-os) — chart layouts for round watch faces
- [Apple Health Sample](./apple-health-sample) — Apple Health integration sample
- [Samsung Health Sample](./samsung-health-sample) — Samsung Health integration sample
- [Wear OS Sample](./wear-os-sample) — standalone Wear OS sample app

## External sample apps

Complete sample projects are available for common health data integrations:

- [Apple Health Sample](https://github.com/HDIL-YS/SalusChart-AppleHealth-Sample) — iOS HealthKit data integration sample
- [Samsung Health Sample](https://github.com/HDIL-YS/SalusChart-SamsungHealth-Sample) — Samsung Health data integration sample
- [Wear OS Sample](https://github.com/HDIL-YS/SalusChart-WearOS-Sample) — Wear OS app sample

## Demo app

The repository includes a full demo app under `:app` that shows every chart type. It is not published to Maven Central.

Clone and run:

```bash
git clone https://github.com/HDIL-YS/SalusChart.git
cd SalusChart
./gradlew :app:installDebug
```

The demo app launches with a chart selector listing 40+ example screens.

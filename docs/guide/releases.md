# Releases

## Latest version

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hdilys/saluschart-ui-compose.svg)](https://central.sonatype.com/artifact/io.github.hdilys/saluschart-ui-compose)

`0.1.6`

All SalusChart artifacts use the same version. Use one `salusVersion` value across every module in your app.

```kotlin
val salusVersion = "0.1.6"
```

## Maven Central

SalusChart is published on Maven Central:

- [io.github.hdilys on Maven Central](https://central.sonatype.com/search?q=io.github.hdilys)
- [saluschart-ui-compose](https://central.sonatype.com/artifact/io.github.hdilys/saluschart-ui-compose)
- [saluschart-ui-wear-compose](https://central.sonatype.com/artifact/io.github.hdilys/saluschart-ui-wear-compose)

## Release notes

The repository changelog is the source of truth for release history:

- [CHANGELOG.md](https://github.com/HDIL-YS/SalusChart/blob/main/CHANGELOG.md)

## Upgrade checklist

When upgrading SalusChart:

1. Update every SalusChart artifact to the same version.
2. Rebuild screens that use `core:transform`, because chart marks and transform helpers share model contracts.
3. Check [Known Limitations](./known-limitations) for behavior that may affect your chart or data flow.
4. Review the changelog before publishing an app update.

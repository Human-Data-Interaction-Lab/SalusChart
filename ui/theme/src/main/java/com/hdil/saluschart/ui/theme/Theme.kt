@file:JvmName("SalusChartColorProviderKt")

package com.hdil.saluschart.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that carries the active [SalusChartColorScheme] down the composition tree.
 *
 * Charts read from this local to resolve default colors when no explicit color is passed.
 * Use `CompositionLocalProvider(LocalSalusChartColors provides myScheme)` to apply a scheme,
 * or wrap your content with the app-level theme that provides this local.
 */
val LocalSalusChartColors = staticCompositionLocalOf { SalusChartColorScheme.Default }

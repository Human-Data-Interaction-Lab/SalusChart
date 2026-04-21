package com.hdil.saluschart.samplewear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.wear.compose.material3.MaterialTheme
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearSampleTheme(
    chartColorScheme: SalusChartColorScheme = SalusChartColorScheme.Summer,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSalusChartColors provides chartColorScheme) {
        MaterialTheme(content = content)
    }
}

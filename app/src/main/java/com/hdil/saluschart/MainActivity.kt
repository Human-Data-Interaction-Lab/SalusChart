package com.hdil.saluschart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.theme.SalusChartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusChartTheme {
                Surface {
                    var tab by remember { mutableIntStateOf(0) }
                    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
                        TabRow(selectedTabIndex = tab) {
                            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Examples") })
                            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Docs") })
                        }
                        when (tab) {
                            0 -> ExampleUI(modifier = Modifier.fillMaxSize())
                            1 -> DocsExamplesUI(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
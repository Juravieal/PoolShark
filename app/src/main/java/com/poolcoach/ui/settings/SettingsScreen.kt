package com.poolcoach.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poolcoach.domain.model.GameMode
import com.poolcoach.domain.model.TableSize

@Composable
fun SettingsScreen(
    currentTableSize: TableSize,
    currentGameMode: GameMode,
    onTableSizeChanged: (TableSize) -> Unit,
    onGameModeChanged: (GameMode) -> Unit,
    onRecalibrate: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Table Size", style = MaterialTheme.typography.titleMedium)
        TableSize.entries.forEach { size ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = size == currentTableSize, onClick = { onTableSizeChanged(size) })
                Text(size.name.replace("_", " "))
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Game Mode", style = MaterialTheme.typography.titleMedium)
        GameMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == currentGameMode, onClick = { onGameModeChanged(mode) })
                Text(mode.name.replace("_", "-"))
            }
        }
        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = onRecalibrate) { Text("Re-run Calibration") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("← Back") }
    }
}

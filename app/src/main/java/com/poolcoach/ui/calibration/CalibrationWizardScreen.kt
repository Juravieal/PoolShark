package com.poolcoach.ui.calibration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

enum class CalibrationStep { CORNER_SETUP, BREAK_SESSION, DONE }

@Composable
fun CalibrationWizardScreen(
    onCalibrationComplete: () -> Unit,
    viewModel: CalibrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.step) {
        CalibrationStep.CORNER_SETUP -> CornerSetupStep(
            onCornersSet = viewModel::onCornersSet
        )
        CalibrationStep.BREAK_SESSION -> BreakSessionStep(
            breaksCompleted = uiState.breaksCompleted,
            totalBreaks = 10,
            onBreakCaptured = viewModel::onBreakCaptured
        )
        CalibrationStep.DONE -> {
            LaunchedEffect(Unit) { onCalibrationComplete() }
        }
    }
}

@Composable
private fun CornerSetupStep(onCornersSet: (List<Pair<Float, Float>>) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Step 1: Corner Setup", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Tap the 4 corners of the table playing surface in order: TL → TR → BR → BL")
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            // Placeholder: auto-set dummy corners for development testing
            onCornersSet(listOf(100f to 100f, 900f to 100f, 900f to 500f, 100f to 500f))
        }) { Text("Corners Set — Continue") }
    }
}

@Composable
private fun BreakSessionStep(
    breaksCompleted: Int,
    totalBreaks: Int,
    onBreakCaptured: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Step 2: Break $totalBreaks Racks", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { breaksCompleted.toFloat() / totalBreaks },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("$breaksCompleted / $totalBreaks breaks captured")
        Spacer(Modifier.height(16.dp))
        Text("Break a rack. The app will auto-capture when balls stop moving.")
    }
}

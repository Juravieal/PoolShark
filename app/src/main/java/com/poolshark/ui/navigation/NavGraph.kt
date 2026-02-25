package com.poolshark.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.poolshark.domain.model.Shot
import com.poolshark.domain.model.TableState
import com.poolshark.ui.MainViewModel
import com.poolshark.ui.calibration.CalibrationWizardScreen
import com.poolshark.ui.camera.CameraScreen
import com.poolshark.ui.table.TableScreen
import com.poolshark.ui.animation.ShotAnimationScreen
import com.poolshark.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Calibration : Screen("calibration")
    object Camera : Screen("camera")
    object Table : Screen("table")
    object Animation : Screen("animation")
    object Settings : Screen("settings")
}

@Composable
fun PoolCoachNavGraph(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    val mainVm: MainViewModel = hiltViewModel()
    val settings by mainVm.settings.collectAsState()

    var tableState by remember { mutableStateOf<TableState?>(null) }
    var activeShot by remember { mutableStateOf<Shot?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) Screen.Calibration.route else Screen.Camera.route
    ) {
        composable(Screen.Calibration.route) {
            CalibrationWizardScreen(
                onCalibrationComplete = { navController.navigate(Screen.Camera.route) }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                onSnapshotReady = { bitmap ->
                    tableState = mainVm.processSnapshot(bitmap)
                    navController.navigate(Screen.Table.route)
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Table.route) {
            tableState?.let { ts ->
                TableScreen(
                    onAnimateShot = { shot ->
                        activeShot = shot
                        navController.navigate(Screen.Animation.route)
                    },
                    onBackToCamera = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Animation.route) {
            val shot = activeShot
            val ts = tableState
            if (shot != null && ts != null) {
                ShotAnimationScreen(
                    shot = shot,
                    tableState = ts,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                currentTableSize = settings.tableSize,
                currentGameMode = settings.gameMode,
                onTableSizeChanged = mainVm::onTableSizeChanged,
                onGameModeChanged = mainVm::onGameModeChanged,
                onRecalibrate = {
                    navController.navigate(Screen.Calibration.route) {
                        popUpTo(Screen.Camera.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

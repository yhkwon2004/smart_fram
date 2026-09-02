package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.SmartFarmViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SmartFarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartFarmApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartFarmApp(viewModel: SmartFarmViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val plant by viewModel.plant.collectAsState()
    val sensorTelemetry by viewModel.sensorTelemetry.collectAsState()
    val sensorHistory by viewModel.sensorHistory.collectAsState()
    val deviceState by viewModel.deviceState.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val points by viewModel.points.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val plantPhotos by viewModel.plantPhotos.collectAsState()
    val guardianSettings by viewModel.guardianSettings.collectAsState()

    AnimatedContent(
        targetState = uiState.currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.START -> {
                StartScreen(
                    onStartClick = { viewModel.navigateTo(AppScreen.MODE_SELECT) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppScreen.MODE_SELECT -> {
                ModeSelectScreen(
                    onSelectChildMode = { viewModel.navigateTo(AppScreen.CHILD_HOME) },
                    onSelectGuardianMode = { viewModel.requestGuardianAccess() },
                    onBackClick = { viewModel.navigateTo(AppScreen.START) },
                    isPinModalVisible = uiState.isPinModalVisible,
                    enteredPin = uiState.enteredPin,
                    pinError = uiState.pinError,
                    onPinDigitClick = { viewModel.enterPinDigit(it) },
                    onPinDeleteClick = { viewModel.deletePinDigit() },
                    onPinCancelClick = { viewModel.cancelPinEntry() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppScreen.CHILD_HOME -> {
                ChildHomeScreen(
                    plant = plant,
                    sensorTelemetry = sensorTelemetry,
                    deviceState = deviceState,
                    missions = missions,
                    badges = badges,
                    points = points,
                    plantPhotos = plantPhotos,
                    currentTab = uiState.childTab,
                    onTabSelected = { viewModel.setChildTab(it) },
                    onMenuClick = { viewModel.navigateTo(AppScreen.MODE_SELECT) },
                    activeMissionForDialog = uiState.activeMissionForDialog,
                    activePromptLevel = uiState.activeMissionPromptLevel,
                    onOpenMissionDialog = { viewModel.openMissionDialog(it) },
                    onCloseMissionDialog = { viewModel.closeMissionDialog() },
                    onSelectPromptLevel = { viewModel.setMissionPromptLevel(it) },
                    onSubmitMissionAnswer = { mId, optId -> viewModel.submitMissionAnswer(mId, optId) },
                    isWateringInProgress = uiState.isWateringInProgress,
                    lastWateringResult = uiState.lastWateringResult,
                    onRequestWatering = { promptLevel -> viewModel.triggerWateringRequest(promptLevel) },
                    onDismissWateringResult = { viewModel.dismissWateringResult() },
                    isPhotoModalVisible = uiState.isPhotoModalVisible,
                    onOpenPhotoModal = { viewModel.openPhotoModal() },
                    onDismissPhotoModal = { viewModel.dismissPhotoModal() },
                    onSavePhoto = { caption -> viewModel.savePhoto(caption) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppScreen.GUARDIAN_DASHBOARD -> {
                GuardianDashboardScreen(
                    plant = plant,
                    sensorTelemetry = sensorTelemetry,
                    sensorHistory = sensorHistory,
                    deviceState = deviceState,
                    activityLogs = activityLogs,
                    guardianSettings = guardianSettings,
                    currentTab = uiState.guardianTab,
                    onTabSelected = { viewModel.setGuardianTab(it) },
                    onBackToChildHome = { viewModel.navigateTo(AppScreen.CHILD_HOME) },
                    onUpdateSettings = { viewModel.updateGuardianSettings(it) },
                    onSetLedMode = { viewModel.setLedMode(it) },
                    onSetOledMessage = { viewModel.setOledMessage(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


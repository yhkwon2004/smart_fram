package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SmartFarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    START,
    MODE_SELECT,
    CHILD_HOME,
    GUARDIAN_DASHBOARD
}

enum class ChildTab {
    GARDEN,
    QUESTS,
    BADGES,
    PHOTOS
}

enum class GuardianTab {
    STATS,
    SENSORS,
    DEVICE,
    SETTINGS
}

data class SmartFarmUiState(
    val currentScreen: AppScreen = AppScreen.START,
    val childTab: ChildTab = ChildTab.GARDEN,
    val guardianTab: GuardianTab = GuardianTab.STATS,
    val isWateringInProgress: Boolean = false,
    val lastWateringResult: WateringActionResult? = null,
    val activeMissionForDialog: Mission? = null,
    val activeMissionPromptLevel: Int = 0,
    val isPinModalVisible: Boolean = false,
    val enteredPin: String = "",
    val pinError: String? = null,
    val celebrationBadge: Badge? = null,
    val isPhotoModalVisible: Boolean = false
)

class SmartFarmViewModel(
    private val repository: SmartFarmRepository = SmartFarmRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartFarmUiState())
    val uiState: StateFlow<SmartFarmUiState> = _uiState.asStateFlow()

    val plant: StateFlow<Plant> = repository.plant
    val sensorTelemetry: StateFlow<SensorTelemetry> = repository.sensorTelemetry
    val sensorHistory: StateFlow<List<SensorHistoryPoint>> = repository.sensorHistory
    val deviceState: StateFlow<Esp32DeviceState> = repository.deviceState
    val missions: StateFlow<List<Mission>> = repository.missions
    val badges: StateFlow<List<Badge>> = repository.badges
    val points: StateFlow<Int> = repository.points
    val activityLogs: StateFlow<List<ActivityLog>> = repository.activityLogs
    val plantPhotos: StateFlow<List<String>> = repository.plantPhotos
    val guardianSettings: StateFlow<GuardianSettings> = repository.guardianSettings

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setChildTab(tab: ChildTab) {
        _uiState.update { it.copy(childTab = tab) }
    }

    fun setGuardianTab(tab: GuardianTab) {
        _uiState.update { it.copy(guardianTab = tab) }
    }

    fun requestGuardianAccess() {
        val settings = guardianSettings.value
        if (settings.requirePinForGuardian) {
            _uiState.update { it.copy(isPinModalVisible = true, enteredPin = "", pinError = null) }
        } else {
            navigateTo(AppScreen.GUARDIAN_DASHBOARD)
        }
    }

    fun enterPinDigit(digit: String) {
        val current = _uiState.value.enteredPin
        if (current.length < 4) {
            val updated = current + digit
            _uiState.update { it.copy(enteredPin = updated, pinError = null) }
            if (updated.length == 4) {
                verifyPin(updated)
            }
        }
    }

    fun deletePinDigit() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = current.dropLast(1), pinError = null) }
        }
    }

    fun cancelPinEntry() {
        _uiState.update { it.copy(isPinModalVisible = false, enteredPin = "", pinError = null) }
    }

    private fun verifyPin(pin: String) {
        val expected = guardianSettings.value.pinCode
        if (pin == expected || pin == "0000") { // Fallback standard pin
            _uiState.update { it.copy(isPinModalVisible = false, enteredPin = "", pinError = null) }
            navigateTo(AppScreen.GUARDIAN_DASHBOARD)
        } else {
            _uiState.update { it.copy(enteredPin = "", pinError = "비밀번호가 맞지 않아요. 다시 입력해주세요.") }
        }
    }

    fun openMissionDialog(mission: Mission) {
        _uiState.update { it.copy(activeMissionForDialog = mission, activeMissionPromptLevel = 0) }
    }

    fun closeMissionDialog() {
        _uiState.update { it.copy(activeMissionForDialog = null) }
    }

    fun setMissionPromptLevel(level: Int) {
        _uiState.update { it.copy(activeMissionPromptLevel = level) }
    }

    fun submitMissionAnswer(missionId: String, optionId: String) {
        val promptLevel = _uiState.value.activeMissionPromptLevel
        repository.completeMission(missionId, optionId, promptLevel)
    }

    fun triggerWateringRequest(promptLevel: Int = 0) {
        if (_uiState.value.isWateringInProgress) return

        viewModelScope.launch {
            _uiState.update { it.copy(isWateringInProgress = true, lastWateringResult = null) }
            val result = repository.requestWatering("child_app", promptLevel)
            _uiState.update {
                it.copy(
                    isWateringInProgress = false,
                    lastWateringResult = result
                )
            }
        }
    }

    fun dismissWateringResult() {
        _uiState.update { it.copy(lastWateringResult = null) }
    }

    fun openPhotoModal() {
        _uiState.update { it.copy(isPhotoModalVisible = true) }
    }

    fun dismissPhotoModal() {
        _uiState.update { it.copy(isPhotoModalVisible = false) }
    }

    fun savePhoto(caption: String) {
        repository.addPlantPhoto(caption)
        dismissPhotoModal()
    }

    fun updateGuardianSettings(settings: GuardianSettings) {
        repository.updateGuardianSettings(settings)
    }

    fun setLedMode(mode: LedMode) {
        repository.updateLedMode(mode)
    }

    fun setOledMessage(msg: String) {
        repository.updateOledMessage(msg)
    }
}

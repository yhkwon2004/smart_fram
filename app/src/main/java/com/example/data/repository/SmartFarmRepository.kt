package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartFarmRepository(
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    // Current Plant
    private val _plant = MutableStateFlow(
        Plant(
            id = "plant_001",
            nickname = "초록이",
            profileId = "cherry_tomato",
            cropName = "방울토마토",
            stage = GrowthStage.GROWING,
            plantedDaysAgo = 18,
            healthPercent = 95,
            growthExp = 420,
            growthLevel = 3,
            mood = PlantMood.HAPPY
        )
    )
    val plant: StateFlow<Plant> = _plant.asStateFlow()

    // Sensor Telemetry
    private val _sensorTelemetry = MutableStateFlow(
        SensorTelemetry(
            soilMoisture = 44.5f,
            temperature = 23.4f,
            lightLux = 5800f,
            waterTankLevel = 75f,
            humidity = 58f
        )
    )
    val sensorTelemetry: StateFlow<SensorTelemetry> = _sensorTelemetry.asStateFlow()

    // Sensor History
    private val _sensorHistory = MutableStateFlow(generateInitialSensorHistory())
    val sensorHistory: StateFlow<List<SensorHistoryPoint>> = _sensorHistory.asStateFlow()

    // ESP32 Device State
    private val _deviceState = MutableStateFlow(
        Esp32DeviceState(
            deviceId = "ESP32-FARM-K01",
            isOnline = true,
            wifiRssi = -54,
            firmwareVersion = "v2.1.0-SafeGardener",
            pumpActive = false,
            fanActive = false,
            ledMode = LedMode.NORMAL,
            ledBrightness = 0.85f,
            ledEnabled = true,
            oledMessage = "🌱 잘 자라고 있어요!",
            lastWateredTimestamp = System.currentTimeMillis() - (1000 * 60 * 65)
        )
    )
    val deviceState: StateFlow<Esp32DeviceState> = _deviceState.asStateFlow()

    // Guardian Settings
    private val _guardianSettings = MutableStateFlow(GuardianSettings())
    val guardianSettings: StateFlow<GuardianSettings> = _guardianSettings.asStateFlow()

    // Badges
    private val _badges = MutableStateFlow(generateInitialBadges())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    // User Points
    private val _points = MutableStateFlow(140)
    val points: StateFlow<Int> = _points.asStateFlow()

    // Missions
    private val _missions = MutableStateFlow(generateMissionsForToday(1))
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    // Activity Logs
    private val _activityLogs = MutableStateFlow(generateInitialActivityLogs())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    // Photo Gallery
    private val _plantPhotos = MutableStateFlow(listOf("🌱 1일차: 새싹 돋음", "🌿 7일차: 잎이 4장!", "🪴 14일차: 키가 쑥 자랐어요"))
    val plantPhotos: StateFlow<List<String>> = _plantPhotos.asStateFlow()

    init {
        startHardwareSimulationLoop()
    }

    private fun startHardwareSimulationLoop() {
        externalScope.launch {
            while (true) {
                delay(3500)
                // Gentle continuous sensor fluctuations
                val current = _sensorTelemetry.value
                val driftSoil = (current.soilMoisture - 0.05f).coerceIn(15f, 95f)
                val driftTemp = (current.temperature + ((-10..10).random() * 0.05f)).coerceIn(18f, 32f)
                val driftLight = (current.lightLux + ((-50..50).random() * 10f)).coerceIn(1000f, 15000f)

                _sensorTelemetry.value = current.copy(
                    soilMoisture = (driftSoil * 10).toInt() / 10f,
                    temperature = (driftTemp * 10).toInt() / 10f,
                    lightLux = driftLight,
                    timestamp = System.currentTimeMillis()
                )

                // Update Plant mood based on sensor
                updatePlantMood(driftSoil, driftTemp)
            }
        }
    }

    private fun updatePlantMood(soil: Float, temp: Float) {
        val currentPlant = _plant.value
        val newMood = when {
            soil < 30f -> PlantMood.THIRSTY
            temp > 28.5f -> PlantMood.HOT
            temp < 18f -> PlantMood.COOL
            currentPlant.stage == GrowthStage.HARVEST -> PlantMood.HARVEST_READY
            else -> PlantMood.HAPPY
        }
        if (currentPlant.mood != newMood) {
            _plant.value = currentPlant.copy(mood = newMood)
            // Synchronize OLED display text with mood
            updateOledMessage(newMood.emoji + " " + newMood.messageKo)
        }
    }

    fun updateOledMessage(msg: String) {
        _deviceState.value = _deviceState.value.copy(oledMessage = msg)
    }

    fun updateLedMode(mode: LedMode) {
        _deviceState.value = _deviceState.value.copy(ledMode = mode)
    }

    fun updateGuardianSettings(newSettings: GuardianSettings) {
        _guardianSettings.value = newSettings
        if (newSettings.difficultyMode == DifficultyMode.MANUAL) {
            _missions.value = generateMissionsForToday(newSettings.manualLevel)
        }
    }

    // Water request with strict safety pipeline per prompt section 8
    suspend fun requestWatering(
        source: String = "child_app",
        promptLevel: Int = 0
    ): WateringActionResult {
        val currentSensor = _sensorTelemetry.value
        val currentDevice = _deviceState.value
        val settings = _guardianSettings.value
        val now = System.currentTimeMillis()

        val timeSinceLastWaterMin = (now - currentDevice.lastWateredTimestamp) / (1000 * 60)

        // Safety Rule 1: ESP32 Online check
        if (!currentDevice.isOnline) {
            return WateringActionResult(
                isSuccess = false,
                pumpRunSeconds = 0,
                soilBefore = currentSensor.soilMoisture,
                soilAfter = currentSensor.soilMoisture,
                userMessage = "기기 연결을 확인하는 중이에요. 잠시 후 다시 해봐요!",
                guardianLogReason = "ESP32 Offline - Water pump request blocked."
            )
        }

        // Safety Rule 2: Water Tank Level Check
        if (currentSensor.waterTankLevel < 15f) {
            updateLedMode(LedMode.ERROR)
            updateOledMessage("⚠️ 물통에 물을 채워주세요")
            return WateringActionResult(
                isSuccess = false,
                pumpRunSeconds = 0,
                soilBefore = currentSensor.soilMoisture,
                soilAfter = currentSensor.soilMoisture,
                userMessage = "물통에 물이 조금 부족해요. 보호자에게 물 채우기를 부탁했어요!",
                guardianLogReason = "Low Water Tank (<15%) - Pump dry run protection activated."
            )
        }

        // Safety Rule 3: Soil moisture already saturated (과습 방지)
        if (currentSensor.soilMoisture >= 65f) {
            updateOledMessage("🌱 지금은 물이 충분해요!")
            return WateringActionResult(
                isSuccess = false,
                pumpRunSeconds = 0,
                soilBefore = currentSensor.soilMoisture,
                soilAfter = currentSensor.soilMoisture,
                userMessage = "지금은 흙 속에 물이 충분해요! 촉촉해서 기분이 아주 좋대요 😊",
                guardianLogReason = "Soil moisture already sufficient (${currentSensor.soilMoisture}% >= 65%). Over-watering prevented."
            )
        }

        // Safety Rule 4: Minimum interval check (연속 관수 방지)
        if (timeSinceLastWaterMin < 10) {
            return WateringActionResult(
                isSuccess = false,
                pumpRunSeconds = 0,
                soilBefore = currentSensor.soilMoisture,
                soilAfter = currentSensor.soilMoisture,
                userMessage = "방금 시원하게 물을 마셨어요! 조금 쉬었다가 또 만나요.",
                guardianLogReason = "Minimum interval lockout (<10 min since last watering: ${timeSinceLastWaterMin}m)."
            )
        }

        // Condition Met: PUMP ON (Safe limited duration)
        val pumpSeconds = settings.waterSafetyMaxSec.coerceIn(2, 4)
        val soilBefore = currentSensor.soilMoisture
        val newSoil = (soilBefore + 18.0f).coerceAtMost(75.0f)
        val newWaterTank = (currentSensor.waterTankLevel - 4.0f).coerceAtLeast(10f)

        _deviceState.value = _deviceState.value.copy(
            pumpActive = true,
            ledMode = LedMode.MISSION_SUCCESS,
            oledMessage = "💧 시원한 물 주는 중...",
            lastWateredTimestamp = now
        )

        delay(pumpSeconds * 1000L)

        // Pump OFF & state commit
        _deviceState.value = _deviceState.value.copy(
            pumpActive = false,
            ledMode = LedMode.NORMAL,
            oledMessage = "✨ 물주기 완료! 고마워요!"
        )

        _sensorTelemetry.value = _sensorTelemetry.value.copy(
            soilMoisture = (newSoil * 10).toInt() / 10f,
            waterTankLevel = newWaterTank
        )

        // Increase Plant Exp
        val currentPlant = _plant.value
        val updatedExp = currentPlant.growthExp + 35
        val updatedHealth = (currentPlant.healthPercent + 5).coerceAtMost(100)
        _plant.value = currentPlant.copy(
            growthExp = updatedExp,
            healthPercent = updatedHealth,
            mood = PlantMood.HAPPY
        )

        // Add Points & Reward
        addPoints(15)
        unlockBadgeIfApplicable("badge_first_water")

        // Log Activity
        logActivity(
            missionId = "water_request_action",
            missionLevel = 4,
            activityType = "안전 관수 활동 (Watering)",
            startTime = now - (pumpSeconds * 1000L),
            endTime = System.currentTimeMillis(),
            durationSec = pumpSeconds + 4,
            completed = true,
            promptLevel = promptLevel,
            helpRequested = promptLevel >= 2,
            selfInitiated = promptLevel == 0,
            userAnswer = "물주기 요청",
            expectedAnswer = "물주기 실행",
            sensorBefore = soilBefore,
            sensorAfter = newSoil,
            rewardPoints = 15
        )

        return WateringActionResult(
            isSuccess = true,
            pumpRunSeconds = pumpSeconds,
            soilBefore = soilBefore,
            soilAfter = newSoil,
            userMessage = "💧 쏴아아~ 식물에게 시원한 물을 주었어요! 흙이 촉촉해졌어요.",
            guardianLogReason = "Watering successfully executed ($pumpSeconds s, soil: $soilBefore% -> $newSoil%).",
            ledAnimationTriggered = LedMode.MISSION_SUCCESS
        )
    }

    fun completeMission(
        missionId: String,
        selectedOptionId: String,
        promptLevel: Int = 0
    ) {
        val currentMissions = _missions.value.toMutableList()
        val index = currentMissions.indexOfFirst { it.id == missionId }
        if (index != -1) {
            val mission = currentMissions[index]
            val selectedOption = mission.options.find { it.id == selectedOptionId }
            val optionText = selectedOption?.text ?: ""

            val feedback = when (mission.level) {
                MissionLevel.LEVEL_1 -> "식물과 반갑게 인사했어요! 식물도 반가워해요 🌱"
                MissionLevel.LEVEL_2 -> "관찰을 참 잘했어요! 식물의 마음을 알아채주었어요 😊"
                MissionLevel.LEVEL_3 -> {
                    if (selectedOption?.isRecommendedForCurrentSensor == true) {
                        "좋아요! 센서 친구도 똑같이 알려주고 있어요 💧"
                    } else {
                        "식물은 아직 물이 충분해요. 조금 있다가 다시 확인해볼까요? 🌱"
                    }
                }
                MissionLevel.LEVEL_4 -> "오늘의 식물 돌봄 활동 완료! 멋진 정원사예요 ⭐"
            }

            val updated = mission.copy(
                isCompleted = true,
                selectedOptionId = selectedOptionId,
                feedbackMessage = feedback,
                promptLevel = promptLevel
            )
            currentMissions[index] = updated
            _missions.value = currentMissions

            // Add points
            addPoints(mission.rewardPoints)
            checkStreakBadges()

            // Update OLED & LED
            updateOledMessage("⭐ " + mission.title + " 완료!")
            updateLedMode(LedMode.MISSION_SUCCESS)

            // Log activity for Research & Guardian Dashboard
            logActivity(
                missionId = mission.id,
                missionLevel = mission.level.levelNum,
                activityType = mission.title,
                startTime = System.currentTimeMillis() - 25000,
                endTime = System.currentTimeMillis(),
                durationSec = 25,
                completed = true,
                promptLevel = promptLevel,
                helpRequested = promptLevel >= 2,
                selfInitiated = promptLevel == 0,
                userAnswer = optionText,
                expectedAnswer = mission.options.firstOrNull { it.isRecommendedForCurrentSensor }?.text ?: "",
                sensorBefore = _sensorTelemetry.value.soilMoisture,
                sensorAfter = _sensorTelemetry.value.soilMoisture,
                rewardPoints = mission.rewardPoints
            )

            // Auto Difficulty evaluation
            evaluateAdaptiveDifficulty()
        }
    }

    fun addPlantPhoto(title: String) {
        _plantPhotos.value = listOf(title) + _plantPhotos.value
        addPoints(10)
        logActivity(
            missionId = "photo_capture",
            missionLevel = 1,
            activityType = "식물 성장 사진 기록",
            startTime = System.currentTimeMillis() - 15000,
            endTime = System.currentTimeMillis(),
            durationSec = 15,
            completed = true,
            promptLevel = 0,
            helpRequested = false,
            selfInitiated = true,
            userAnswer = "사진 촬영 완료",
            expectedAnswer = "사진 촬영",
            sensorBefore = _sensorTelemetry.value.soilMoisture,
            sensorAfter = _sensorTelemetry.value.soilMoisture,
            rewardPoints = 10
        )
    }

    private fun addPoints(amount: Int) {
        _points.value = _points.value + amount
    }

    private fun checkStreakBadges() {
        if (_points.value >= 100) unlockBadgeIfApplicable("badge_point_100")
        if (_points.value >= 200) unlockBadgeIfApplicable("badge_point_200")
    }

    private fun unlockBadgeIfApplicable(badgeId: String) {
        val list = _badges.value.toMutableList()
        val idx = list.indexOfFirst { it.id == badgeId }
        if (idx != -1 && !list[idx].isUnlocked) {
            list[idx] = list[idx].copy(
                isUnlocked = true,
                unlockedDate = dateFormat.format(Date())
            )
            _badges.value = list
        }
    }

    private fun evaluateAdaptiveDifficulty() {
        val settings = _guardianSettings.value
        if (settings.difficultyMode != DifficultyMode.AUTO) return

        val recentLogs = _activityLogs.value.take(5)
        if (recentLogs.size >= 5) {
            val completionRate = recentLogs.count { it.completed }.toFloat() / recentLogs.size
            val avgPrompt = recentLogs.map { it.promptLevel }.average()

            var newLevel = settings.manualLevel
            if (completionRate >= 0.8f && avgPrompt <= 1.0) {
                newLevel = (newLevel + 1).coerceAtMost(4)
            } else if (completionRate <= 0.4f) {
                newLevel = (newLevel - 1).coerceAtLeast(1)
            }

            if (newLevel != settings.manualLevel) {
                _guardianSettings.value = settings.copy(manualLevel = newLevel)
            }
        }
    }

    private fun logActivity(
        missionId: String,
        missionLevel: Int,
        activityType: String,
        startTime: Long,
        endTime: Long,
        durationSec: Int,
        completed: Boolean,
        promptLevel: Int,
        helpRequested: Boolean,
        selfInitiated: Boolean,
        userAnswer: String,
        expectedAnswer: String,
        sensorBefore: Float,
        sensorAfter: Float,
        rewardPoints: Int
    ) {
        val log = ActivityLog(
            missionId = missionId,
            missionLevel = missionLevel,
            activityType = activityType,
            startTime = startTime,
            endTime = endTime,
            durationSec = durationSec,
            completed = completed,
            promptLevel = promptLevel,
            helpRequested = helpRequested,
            selfInitiated = selfInitiated,
            userAnswer = userAnswer,
            expectedAnswer = expectedAnswer,
            sensorBeforeSoil = sensorBefore,
            sensorAfterSoil = sensorAfter,
            rewardPoints = rewardPoints
        )
        _activityLogs.value = listOf(log) + _activityLogs.value
    }

    // Mock initial data generators
    private fun generateMissionsForToday(level: Int): List<Mission> {
        return listOf(
            Mission(
                id = "m1",
                level = MissionLevel.LEVEL_1,
                title = "우리 식물과 아침 눈맞춤",
                questionPrompt = "안녕 초록아! 오늘도 식물을 만나볼까요?",
                helperTip = "화분을 천천히 바라보며 손을 흔들어보세요.",
                options = listOf(
                    MissionOption("opt_1_1", "안녕! 확인했어요", "👋", true),
                    MissionOption("opt_1_2", "오늘도 잘 지내자", "💚", true)
                ),
                rewardPoints = 10,
                isCompleted = false
            ),
            Mission(
                id = "m2",
                level = MissionLevel.LEVEL_2,
                title = "오늘의 초록이 기분 알아보기",
                questionPrompt = "오늘 우리 식물은 어떻게 보이나요?",
                helperTip = "잎의 색과 줄기의 튼튼한 모습을 관찰해보세요.",
                options = listOf(
                    MissionOption("opt_2_1", "싱싱하고 건강해요", "😊", true),
                    MissionOption("opt_2_2", "조금 목이 마른가 봐요", "🤔", false)
                ),
                rewardPoints = 15,
                isCompleted = false
            ),
            Mission(
                id = "m3",
                level = MissionLevel.LEVEL_3,
                title = "센서 친구와 수분 체크하기",
                questionPrompt = "지금 식물에게 물이 필요할까요?",
                helperTip = "흙이 촉촉한지 눈으로 보고, 아래 버튼을 골라봐요.",
                options = listOf(
                    MissionOption("opt_3_1", "지금 물이 필요해요", "💧", true),
                    MissionOption("opt_3_2", "아직 흙이 촉촉해요", "🌱", false)
                ),
                rewardPoints = 20,
                isCompleted = false
            )
        )
    }

    private fun generateInitialBadges(): List<Badge> {
        return listOf(
            Badge("badge_first_meet", "첫 만남의 설렘", "스마트팜 식물과 처음 인사를 나눴어요", "🌱", "기본", true, "2026.08.15"),
            Badge("badge_first_water", "물 지킴이", "안전하게 첫 물주기를 완료했어요", "💧", "활동", true, "2026.08.16"),
            Badge("badge_steady", "꾸준한 정원사", "연속 3일 동안 식물을 돌보았어요", "🏅", "성취", true, "2026.08.18"),
            Badge("badge_leaf_friend", "초록 관찰자", "잎과 흙의 상태를 꼼꼼하게 관찰했어요", "🌿", "관찰", true, "2026.08.20"),
            Badge("badge_point_100", "별빛 백점 수호자", "활동 포인트 100점을 돌파했어요", "⭐", "보상", true, "2026.08.25"),
            Badge("badge_point_200", "정원 마스터", "활동 포인트 200점을 달성해요", "👑", "보상", false, null),
            Badge("badge_harvest_first", "기적의 수확", "첫 방울토마토 열매를 수확해요", "🍅", "수확", false, null)
        )
    }

    private fun generateInitialSensorHistory(): List<SensorHistoryPoint> {
        return listOf(
            SensorHistoryPoint("06:00", 38.0f, 21.2f, 1200f, 80f),
            SensorHistoryPoint("09:00", 36.5f, 22.8f, 5400f, 80f),
            SensorHistoryPoint("12:00", 35.0f, 25.1f, 9200f, 78f),
            SensorHistoryPoint("15:00", 52.0f, 24.6f, 7600f, 75f),
            SensorHistoryPoint("18:00", 49.0f, 23.5f, 4100f, 75f),
            SensorHistoryPoint("21:00", 46.5f, 22.1f, 800f, 75f),
            SensorHistoryPoint("현재", 44.5f, 23.4f, 5800f, 75f)
        )
    }

    private fun generateInitialActivityLogs(): List<ActivityLog> {
        val now = System.currentTimeMillis()
        return listOf(
            ActivityLog(
                activityId = "act_101",
                missionId = "m1_prev",
                missionLevel = 1,
                activityType = "식물 눈맞춤",
                startTime = now - (1000 * 60 * 180),
                endTime = now - (1000 * 60 * 179),
                durationSec = 45,
                completed = true,
                promptLevel = 0,
                helpRequested = false,
                selfInitiated = true,
                userAnswer = "안녕! 확인했어요",
                expectedAnswer = "안녕! 확인했어요",
                sensorBeforeSoil = 42.0f,
                sensorAfterSoil = 42.0f,
                rewardPoints = 10,
                createdAt = now - (1000 * 60 * 180)
            ),
            ActivityLog(
                activityId = "act_102",
                missionId = "m2_prev",
                missionLevel = 2,
                activityType = "식물 마음 읽기",
                startTime = now - (1000 * 60 * 120),
                endTime = now - (1000 * 60 * 118),
                durationSec = 78,
                completed = true,
                promptLevel = 1,
                helpRequested = false,
                selfInitiated = false,
                userAnswer = "싱싱하고 건강해요",
                expectedAnswer = "싱싱하고 건강해요",
                sensorBeforeSoil = 40.5f,
                sensorAfterSoil = 40.5f,
                rewardPoints = 15,
                createdAt = now - (1000 * 60 * 120)
            ),
            ActivityLog(
                activityId = "act_103",
                missionId = "water_act_prev",
                missionLevel = 4,
                activityType = "안전 물주기",
                startTime = now - (1000 * 60 * 65),
                endTime = now - (1000 * 60 * 64),
                durationSec = 52,
                completed = true,
                promptLevel = 0,
                helpRequested = false,
                selfInitiated = true,
                userAnswer = "물주기 버튼 클릭",
                expectedAnswer = "물주기 승인",
                sensorBeforeSoil = 28.0f,
                sensorAfterSoil = 46.0f,
                rewardPoints = 15,
                createdAt = now - (1000 * 60 * 65)
            )
        )
    }
}

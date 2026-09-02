package com.example.data.model

import java.util.UUID

enum class MissionLevel(val levelNum: Int, val titleKo: String, val badgeReward: String) {
    LEVEL_1(1, "1단계: 식물 눈맞춤", "🌱 호기심 씨앗"),
    LEVEL_2(2, "2단계: 식물 마음 읽기", "🌿 초록 관찰자"),
    LEVEL_3(3, "3단계: 센서 친구와 대화", "💧 똑똑한 정원사"),
    LEVEL_4(4, "4단계: 사랑의 물주기", "🪴 물 지킴이 마스터")
}

data class MissionOption(
    val id: String,
    val text: String,
    val emoji: String,
    val isRecommendedForCurrentSensor: Boolean = true
)

data class Mission(
    val id: String = UUID.randomUUID().toString(),
    val level: MissionLevel = MissionLevel.LEVEL_1,
    val title: String,
    val questionPrompt: String,
    val helperTip: String,
    val options: List<MissionOption>,
    val rewardPoints: Int = 10,
    val isCompleted: Boolean = false,
    val selectedOptionId: String? = null,
    val feedbackMessage: String? = null,
    val promptLevel: Int = 0 // 0=자발적, 1=알림후, 2=언어적도움, 3=직접도움
)

data class ActivityLog(
    val activityId: String = UUID.randomUUID().toString(),
    val childId: String = "child_hero_01",
    val childNickname: String = "민우",
    val plantId: String = "plant_001",
    val missionId: String,
    val missionLevel: Int,
    val activityType: String,
    val startTime: Long,
    val endTime: Long,
    val durationSec: Int,
    val completed: Boolean,
    val promptLevel: Int, // 0: 자발적, 1: 알림 후, 2: 보호자 언어 도움, 3: 직접 도움
    val helpRequested: Boolean,
    val selfInitiated: Boolean,
    val userAnswer: String,
    val expectedAnswer: String,
    val sensorBeforeSoil: Float,
    val sensorAfterSoil: Float,
    val rewardPoints: Int,
    val createdAt: Long = System.currentTimeMillis()
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)

enum class DifficultyMode {
    AUTO,
    MANUAL
}

data class GuardianSettings(
    val difficultyMode: DifficultyMode = DifficultyMode.AUTO,
    val manualLevel: Int = 2,
    val pinCode: String = "0000",
    val requirePinForGuardian: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val ledEnabled: Boolean = true,
    val ledBrightness: Float = 0.8f,
    val waterSafetyMaxSec: Int = 4,
    val minIntervalBetweenWateringMinutes: Int = 30,
    val emergencyAutoWateringEnabled: Boolean = true
)

data class WateringActionResult(
    val isSuccess: Boolean,
    val pumpRunSeconds: Int,
    val soilBefore: Float,
    val soilAfter: Float,
    val userMessage: String,
    val guardianLogReason: String,
    val ledAnimationTriggered: LedMode = LedMode.NORMAL
)

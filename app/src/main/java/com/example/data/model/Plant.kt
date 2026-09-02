package com.example.data.model

import java.util.UUID

enum class GrowthStage(val labelKo: String, val stepIndex: Int, val emoji: String) {
    GERMINATION("발아 (씨앗)", 0, "🌱"),
    SEEDLING("유묘 (새싹)", 1, "🌿"),
    GROWING("성장 (줄기와 잎)", 2, "🪴"),
    HARVEST("수확 (열매)", 3, "🍅")
}

enum class PlantMood(val emoji: String, val messageKo: String) {
    HAPPY("😊", "기분 좋게 쑥쑥 자라고 있어요!"),
    THIRSTY("💧", "목이 말라요. 시원한 물이 필요해요!"),
    HOT("☀️", "조금 더워요. 시원한 바람이 불면 좋겠어요."),
    COOL("❄️", "날씨가 쌀쌀해요. 따뜻하게 돌봐주세요."),
    HARVEST_READY("🎉", "열매가 탐스럽게 열렸어요! 수확할 시간이에요!"),
    SLEEPING("🌙", "쿨쿨... 식물도 밤에는 푹 자고 있어요.")
}

data class PlantProfile(
    val id: String,
    val name: String,
    val description: String,
    val germinationDays: Int,
    val seedlingDays: Int,
    val growingDays: Int,
    val harvestDays: Int,
    val targetSoilMin: Float,
    val targetSoilMax: Float,
    val targetTempMin: Float,
    val targetTempMax: Float,
    val targetLightMin: Float,
    val wateringIntervalSec: Int
)

data class Plant(
    val id: String = "plant_001",
    val nickname: String = "초록이",
    val profileId: String = "cherry_tomato",
    val cropName: String = "방울토마토",
    val stage: GrowthStage = GrowthStage.GROWING,
    val plantedDaysAgo: Int = 14,
    val healthPercent: Int = 92,
    val growthExp: Int = 340,
    val growthLevel: Int = 3,
    val mood: PlantMood = PlantMood.HAPPY
)

val DEFAULT_PLANT_PROFILES = listOf(
    PlantProfile(
        id = "cherry_tomato",
        name = "방울토마토",
        description = "빨갛고 달콤한 열매가 맺히는 인기 스마트팜 작물",
        germinationDays = 5,
        seedlingDays = 10,
        growingDays = 25,
        harvestDays = 40,
        targetSoilMin = 35.0f,
        targetSoilMax = 65.0f,
        targetTempMin = 20.0f,
        targetTempMax = 28.0f,
        targetLightMin = 4000.0f,
        wateringIntervalSec = 3600 * 6
    ),
    PlantProfile(
        id = "sweet_basil",
        name = "스위트 바질",
        description = "향기로운 잎이 돋아나는 친근한 허브",
        germinationDays = 4,
        seedlingDays = 8,
        growingDays = 20,
        harvestDays = 30,
        targetSoilMin = 40.0f,
        targetSoilMax = 70.0f,
        targetTempMin = 18.0f,
        targetTempMax = 26.0f,
        targetLightMin = 3500.0f,
        wateringIntervalSec = 3600 * 5
    ),
    PlantProfile(
        id = "crisp_lettuce",
        name = "버터헤드 상추",
        description = "수분 가득 부드러운 잎채소",
        germinationDays = 3,
        seedlingDays = 7,
        growingDays = 18,
        harvestDays = 28,
        targetSoilMin = 50.0f,
        targetSoilMax = 75.0f,
        targetTempMin = 15.0f,
        targetTempMax = 24.0f,
        targetLightMin = 3000.0f,
        wateringIntervalSec = 3600 * 4
    )
)

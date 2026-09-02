package com.example.data.model

enum class SensorStatus(val labelKo: String, val colorHex: Long) {
    NORMAL("정상", 0xFF43A047),
    WARNING("주의", 0xFFFFB703),
    DANGER("위험/조치필요", 0xFFE53935)
}

enum class LedMode(val labelKo: String, val description: String) {
    NORMAL("일반 (천천히 숨쉬기)", "은은한 녹색 브리딩 라이트"),
    MISSION_SUCCESS("미션 성공 (축하)", "반짝이는 무지개 빛"),
    WATER_NEEDED("수분 부족 알림", "부드러운 파란빛"),
    HARVEST("수확 축하", "화려한 골드 옐로우"),
    ERROR("주의 알림", "부드러운 주황빛 경고")
}

data class SensorTelemetry(
    val soilMoisture: Float = 46.5f,       // % (0~100)
    val temperature: Float = 23.8f,        // ℃
    val lightLux: Float = 6200.0f,         // Lux
    val waterTankLevel: Float = 78.0f,     // % (0~100)
    val humidity: Float = 55.0f,           // %
    val timestamp: Long = System.currentTimeMillis()
)

data class Esp32DeviceState(
    val deviceId: String = "ESP32-SMARTFARM-01",
    val isOnline: Boolean = true,
    val wifiRssi: Int = -58,               // dBm
    val firmwareVersion: String = "v1.4.2-SafeControl",
    val pumpActive: Boolean = false,
    val fanActive: Boolean = false,
    val ledMode: LedMode = LedMode.NORMAL,
    val ledBrightness: Float = 0.8f,
    val ledEnabled: Boolean = true,
    val oledMessage: String = "🌱 잘 자라고 있어요!",
    val lastWateredTimestamp: Long = System.currentTimeMillis() - 1000 * 60 * 45,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val sensorErrorCode: String? = null
)

data class SensorHistoryPoint(
    val timeLabel: String,
    val soilMoisture: Float,
    val temperature: Float,
    val lightLux: Float,
    val waterLevel: Float
)

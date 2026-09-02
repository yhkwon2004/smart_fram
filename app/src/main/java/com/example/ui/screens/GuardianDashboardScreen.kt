package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.viewmodel.GuardianTab
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GuardianDashboardScreen(
    plant: Plant,
    sensorTelemetry: SensorTelemetry,
    sensorHistory: List<SensorHistoryPoint>,
    deviceState: Esp32DeviceState,
    activityLogs: List<ActivityLog>,
    guardianSettings: GuardianSettings,
    currentTab: GuardianTab,
    onTabSelected: (GuardianTab) -> Unit,
    onBackToChildHome: () -> Unit,
    onUpdateSettings: (GuardianSettings) -> Unit,
    onSetLedMode: (LedMode) -> Unit,
    onSetOledMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBackToChildHome,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF1F5EB), CircleShape)
                                .testTag("guardian_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "아동 홈으로 돌아가기",
                                tint = Color(0xFF1B3B2B)
                            )
                        }

                        Column {
                            Text(
                                text = "보호자·교사 대시보드",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B3B2B)
                            )
                            Text(
                                text = "아동: 민우  •  작물: ${plant.cropName} (${plant.plantedDaysAgo}일째)",
                                fontSize = 12.sp,
                                color = Color(0xFF556B2F)
                            )
                        }
                    }

                    // Online Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (deviceState.isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (deviceState.isOnline) Color(0xFF43A047) else Color(0xFFE53935))
                            )
                            Text(
                                text = if (deviceState.isOnline) "ESP32 정상" else "오프라인",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deviceState.isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }

                // Sub Tabs (Scrollable or TabRow)
                ScrollableTabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = Color.White,
                    contentColor = Color(0xFF43A047),
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = currentTab == GuardianTab.STATS,
                        onClick = { onTabSelected(GuardianTab.STATS) },
                        text = { Text("📊 활동 & 연구", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == GuardianTab.SENSORS,
                        onClick = { onTabSelected(GuardianTab.SENSORS) },
                        text = { Text("💧 센서 모니터", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == GuardianTab.DEVICE,
                        onClick = { onTabSelected(GuardianTab.DEVICE) },
                        text = { Text("⚙️ ESP32 기기", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == GuardianTab.SETTINGS,
                        onClick = { onTabSelected(GuardianTab.SETTINGS) },
                        text = { Text("🌱 작물 & 난이도", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6FAF3))
                .padding(innerPadding)
        ) {
            when (currentTab) {
                GuardianTab.STATS -> {
                    GuardianStatsTab(activityLogs = activityLogs, settings = guardianSettings)
                }
                GuardianTab.SENSORS -> {
                    GuardianSensorsTab(sensor = sensorTelemetry, history = sensorHistory)
                }
                GuardianTab.DEVICE -> {
                    GuardianDeviceTab(
                        deviceState = deviceState,
                        onLedModeChange = onSetLedMode,
                        onOledMessageChange = onSetOledMessage
                    )
                }
                GuardianTab.SETTINGS -> {
                    GuardianSettingsTab(
                        plant = plant,
                        settings = guardianSettings,
                        onUpdateSettings = onUpdateSettings
                    )
                }
            }
        }
    }
}

// 1. Stats & Research Data Tab
@Composable
private fun GuardianStatsTab(
    activityLogs: List<ActivityLog>,
    settings: GuardianSettings
) {
    val dateFormat = remember { SimpleDateFormat("MM.dd HH:mm", Locale.KOREA) }
    val total = activityLogs.size.coerceAtLeast(1)
    val completedCount = activityLogs.count { it.completed }
    val selfInitiated = activityLogs.count { it.selfInitiated }
    val avgDurationSec = if (activityLogs.isNotEmpty()) activityLogs.map { it.durationSec }.average().toInt() else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "오늘 활동 완료율",
                    value = "${((completedCount.toFloat() / total) * 100).toInt()}%",
                    subtext = "총 ${total}회 중 ${completedCount}회 완료",
                    accentColor = Color(0xFF43A047),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "자발적 참여율",
                    value = "${((selfInitiated.toFloat() / total) * 100).toInt()}%",
                    subtext = "${selfInitiated}회 스스로 시작",
                    accentColor = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "평균 활동 시간",
                    value = "${avgDurationSec / 60}분 ${avgDurationSec % 60}초",
                    subtext = "충분한 탐색 시간 유지",
                    accentColor = Color(0xFFFFB703),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "적응형 난이도",
                    value = "Level ${settings.manualLevel}",
                    subtext = if (settings.difficultyMode == DifficultyMode.AUTO) "자동 조절 모드" else "수동 고정 모드",
                    accentColor = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Activity Prompt Ratio Chart
        item {
            ActivityPromptRatioCard(activityLogs = activityLogs)
        }

        // Structured Activity Log List for Research
        item {
            Text(
                text = "📋 상세 활동 기록 (연구 및 관찰 데이터)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )
        }

        items(activityLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lv.${log.missionLevel} ${log.activityType}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B3B2B)
                        )
                        Text(
                            text = dateFormat.format(Date(log.createdAt)),
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "• 소요시간: ${log.durationSec}초",
                            fontSize = 12.sp,
                            color = Color(0xFF556B2F)
                        )
                        val promptName = when (log.promptLevel) {
                            0 -> "자발적 (Level 0)"
                            1 -> "알림 후 (Level 1)"
                            2 -> "언어 도움 (Level 2)"
                            else -> "직접 도움 (Level 3)"
                        }
                        Text(
                            text = "• 도움수준: $promptName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5EB)
                    ) {
                        Text(
                            text = "응답: \"${log.userAnswer}\"  |  보상: +${log.rewardPoints}P  |  수분변화: ${log.sensorBeforeSoil}% ➔ ${log.sensorAfterSoil}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = Color(0xFF666666))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(text = subtext, fontSize = 11.sp, color = Color(0xFF888888))
        }
    }
}

// 2. Sensors Tab
@Composable
private fun GuardianSensorsTab(
    sensor: SensorTelemetry,
    history: List<SensorHistoryPoint>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🌱 실시간 스마트팜 센서 텔레메트리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )
        }

        item {
            SensorTelemetryCard(
                title = "토양 수분",
                value = "${sensor.soilMoisture}",
                unit = "%",
                status = if (sensor.soilMoisture in 35f..65f) SensorStatus.NORMAL else SensorStatus.WARNING,
                targetRangeText = "35% ~ 65%",
                progressFraction = sensor.soilMoisture / 100f,
                emoji = "💧",
                accentColor = Color(0xFF0288D1)
            )
        }

        item {
            SensorTelemetryCard(
                title = "온도",
                value = "${sensor.temperature}",
                unit = "℃",
                status = if (sensor.temperature in 18f..28f) SensorStatus.NORMAL else SensorStatus.WARNING,
                targetRangeText = "20℃ ~ 28℃",
                progressFraction = (sensor.temperature - 10f) / 30f,
                emoji = "🌡️",
                accentColor = Color(0xFFFB8C00)
            )
        }

        item {
            SensorTelemetryCard(
                title = "조도 (빛 밝기)",
                value = "${sensor.lightLux.toInt()}",
                unit = "Lux",
                status = if (sensor.lightLux >= 4000f) SensorStatus.NORMAL else SensorStatus.WARNING,
                targetRangeText = "4,000 ~ 12,000 Lux",
                progressFraction = sensor.lightLux / 12000f,
                emoji = "☀️",
                accentColor = Color(0xFFFFB703)
            )
        }

        item {
            SensorTelemetryCard(
                title = "물통 수위",
                value = "${sensor.waterTankLevel.toInt()}",
                unit = "%",
                status = if (sensor.waterTankLevel > 20f) SensorStatus.NORMAL else SensorStatus.DANGER,
                targetRangeText = "20% 이상 유지 필요",
                progressFraction = sensor.waterTankLevel / 100f,
                emoji = "🪣",
                accentColor = Color(0xFF43A047)
            )
        }

        item {
            SensorHistoryLineChart(points = history)
        }
    }
}

// 3. Hardware Device Tab
@Composable
private fun GuardianDeviceTab(
    deviceState: Esp32DeviceState,
    onLedModeChange: (LedMode) -> Unit,
    onOledMessageChange: (String) -> Unit
) {
    var oledInput by remember { mutableStateOf(deviceState.oledMessage) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "⚙️ ESP32 펌웨어 및 액추에이터 제어",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )
        }

        item {
            HardwareSimCard(
                deviceState = deviceState,
                onLedModeChange = onLedModeChange
            )
        }

        // OLED Message Customizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🖥️ OLED 화면 문구 변경",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )

                    OutlinedTextField(
                        value = oledInput,
                        onValueChange = { oledInput = it },
                        label = { Text("화면에 띄울 메시지 (식물 상태/칭찬)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { onOledMessageChange(oledInput) },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("OLED 전송")
                    }
                }
            }
        }

        // LED Mode Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "💡 라인 LED 피드백 모드 선택",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )

                    LedMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLedModeChange(mode) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = deviceState.ledMode == mode,
                                onClick = { onLedModeChange(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF43A047))
                            )
                            Column {
                                Text(
                                    text = mode.labelKo,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B3B2B)
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Plant Profiles & Settings Tab
@Composable
private fun GuardianSettingsTab(
    plant: Plant,
    settings: GuardianSettings,
    onUpdateSettings: (GuardianSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🌱 스마트팜 작물 프로파일 & 안전 설정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )
        }

        // Adaptive Difficulty Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🎯 아동 활동 난이도 조절 시스템",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("난이도 모드: ${if (settings.difficultyMode == DifficultyMode.AUTO) "자동 (Rule-based)" else "수동 고정"}")
                        Switch(
                            checked = settings.difficultyMode == DifficultyMode.AUTO,
                            onCheckedChange = { isAuto ->
                                onUpdateSettings(
                                    settings.copy(
                                        difficultyMode = if (isAuto) DifficultyMode.AUTO else DifficultyMode.MANUAL
                                    )
                                )
                            }
                        )
                    }

                    Text(
                        text = "현재 단계: Level ${settings.manualLevel} (1단계: 눈맞춤 ~ 4단계: 실제 관수)",
                        fontSize = 13.sp,
                        color = Color(0xFF556B2F)
                    )

                    Slider(
                        value = settings.manualLevel.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(manualLevel = it.toInt())) },
                        valueRange = 1f..4f,
                        steps = 2,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF43A047), activeTrackColor = Color(0xFF43A047))
                    )
                }
            }
        }

        // Safety Parameters Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🛡️ 관수 안전 잠금 장치",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("1회 최대 관수 시간 (${settings.waterSafetyMaxSec}초)", fontWeight = FontWeight.SemiBold)
                            Text("펌프 과열 및 과습 방지", fontSize = 12.sp, color = Color(0xFF757575))
                        }
                    }

                    Slider(
                        value = settings.waterSafetyMaxSec.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(waterSafetyMaxSec = it.toInt())) },
                        valueRange = 2f..6f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0288D1), activeTrackColor = Color(0xFF0288D1))
                    )
                }
            }
        }
    }
}

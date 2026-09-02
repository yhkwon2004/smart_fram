package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.viewmodel.ChildTab

@Composable
fun ChildHomeScreen(
    plant: Plant,
    sensorTelemetry: SensorTelemetry,
    deviceState: Esp32DeviceState,
    missions: List<Mission>,
    badges: List<Badge>,
    points: Int,
    plantPhotos: List<String>,
    currentTab: ChildTab,
    onTabSelected: (ChildTab) -> Unit,
    onMenuClick: () -> Unit,
    activeMissionForDialog: Mission?,
    activePromptLevel: Int,
    onOpenMissionDialog: (Mission) -> Unit,
    onCloseMissionDialog: () -> Unit,
    onSelectPromptLevel: (Int) -> Unit,
    onSubmitMissionAnswer: (String, String) -> Unit,
    isWateringInProgress: Boolean,
    lastWateringResult: WateringActionResult?,
    onRequestWatering: (Int) -> Unit,
    onDismissWateringResult: () -> Unit,
    isPhotoModalVisible: Boolean,
    onOpenPhotoModal: () -> Unit,
    onDismissPhotoModal: () -> Unit,
    onSavePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("child_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == ChildTab.GARDEN,
                    onClick = { onTabSelected(ChildTab.GARDEN) },
                    icon = { Text("🏡", fontSize = 22.sp) },
                    label = { Text("우리정원", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFDCEDC8)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == ChildTab.QUESTS,
                    onClick = { onTabSelected(ChildTab.QUESTS) },
                    icon = { Text("📋", fontSize = 22.sp) },
                    label = { Text("오늘미션", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFDCEDC8)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == ChildTab.BADGES,
                    onClick = { onTabSelected(ChildTab.BADGES) },
                    icon = { Text("🏅", fontSize = 22.sp) },
                    label = { Text("내 배지", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFDCEDC8)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == ChildTab.PHOTOS,
                    onClick = { onTabSelected(ChildTab.PHOTOS) },
                    icon = { Text("📸", fontSize = 22.sp) },
                    label = { Text("사진첩", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFDCEDC8)
                    )
                )
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
                ChildTab.GARDEN -> {
                    GardenTabContent(
                        plant = plant,
                        sensorTelemetry = sensorTelemetry,
                        deviceState = deviceState,
                        missions = missions,
                        points = points,
                        onMenuClick = onMenuClick,
                        onOpenMissionDialog = onOpenMissionDialog,
                        onRequestWatering = { onRequestWatering(0) },
                        onOpenPhotoModal = onOpenPhotoModal
                    )
                }
                ChildTab.QUESTS -> {
                    QuestsTabContent(
                        missions = missions,
                        onOpenMissionDialog = onOpenMissionDialog
                    )
                }
                ChildTab.BADGES -> {
                    BadgesTabContent(
                        badges = badges,
                        points = points
                    )
                }
                ChildTab.PHOTOS -> {
                    PhotosTabContent(
                        photos = plantPhotos,
                        onAddPhotoClick = onOpenPhotoModal
                    )
                }
            }
        }
    }

    // 1. Mission Dialog
    if (activeMissionForDialog != null) {
        MissionDetailDialog(
            mission = activeMissionForDialog,
            currentPromptLevel = activePromptLevel,
            onPromptLevelChange = onSelectPromptLevel,
            onOptionSelected = { optionId ->
                onSubmitMissionAnswer(activeMissionForDialog.id, optionId)
                onCloseMissionDialog()
            },
            onDismiss = onCloseMissionDialog
        )
    }

    // 2. Watering Pipeline Safe Execution Dialog
    if (isWateringInProgress || lastWateringResult != null) {
        WateringProgressDialog(
            isLoading = isWateringInProgress,
            result = lastWateringResult,
            onDismiss = onDismissWateringResult
        )
    }

    // 3. Photo Capture Dialog
    if (isPhotoModalVisible) {
        PhotoCaptureDialog(
            plantName = plant.nickname,
            onDismiss = onDismissPhotoModal,
            onSave = onSavePhoto
        )
    }
}

@Composable
private fun GardenTabContent(
    plant: Plant,
    sensorTelemetry: SensorTelemetry,
    deviceState: Esp32DeviceState,
    missions: List<Mission>,
    points: Int,
    onMenuClick: () -> Unit,
    onOpenMissionDialog: (Mission) -> Unit,
    onRequestWatering: () -> Unit,
    onOpenPhotoModal: () -> Unit
) {
    val completedCount = missions.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Landscape Banner
        item {
            GardenLandscapeBanner(
                plant = plant,
                completedMissionsCount = completedCount,
                totalMissionsCount = missions.size,
                points = points,
                onMenuClick = onMenuClick
            )
        }

        // Sprout Character & Mood Stage Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "${plant.stage.emoji} ${plant.stage.labelKo}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        // OLED Hardware Sync badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF43A047))
                            )
                            Text(
                                text = "스마트팜 연결됨",
                                fontSize = 12.sp,
                                color = Color(0xFF556B2F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Big Animated Avatar
                    PlantCharacterAvatar(
                        stage = plant.stage,
                        mood = plant.mood,
                        sizeDp = 170.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = plant.nickname,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B3B2B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mood message bubble
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF1F8E9),
                        border = BorderStroke(1.dp, Color(0xFFDCEDC8))
                    ) {
                        Text(
                            text = "${plant.mood.emoji} ${plant.mood.messageKo}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        // Section Title: 오늘의 활동
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌱 초록이 돌보기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B3B2B)
                )
            }
        }

        // 1. Plant Check / Greeting Action Button
        item {
            val firstMission = missions.firstOrNull()
            BigTouchButton(
                title = "식물 확인하기",
                subtitle = "초록이와 반갑게 눈을 맞추고 인사해요",
                emoji = "🌱",
                backgroundColor = Color(0xFFDCEDC8),
                contentColor = Color(0xFF1B5E20),
                badgeText = if (firstMission?.isCompleted == true) "완료됨 ✓" else "+10P",
                testTag = "action_plant_check",
                onClick = {
                    if (firstMission != null) onOpenMissionDialog(firstMission)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2. Water Request Action Button (Connected with ESP32 Safe Watering Pipeline)
        item {
            BigTouchButton(
                title = "물주기 (안전 관수)",
                subtitle = "흙 상태를 확인하고 시원한 물을 줘요",
                emoji = "💧",
                backgroundColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1),
                badgeText = "+15P",
                testTag = "action_water_request",
                onClick = onRequestWatering,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 3. Photo Capture Action Button
        item {
            BigTouchButton(
                title = "사진 남기기",
                subtitle = "오늘 쑥쑥 자란 초록이 모습을 간직해요",
                emoji = "📸",
                backgroundColor = Color(0xFFFFF3E0),
                contentColor = Color(0xFFE65100),
                badgeText = "+10P",
                testTag = "action_photo_capture",
                onClick = onOpenPhotoModal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun QuestsTabContent(
    missions: List<Mission>,
    onOpenMissionDialog: (Mission) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🎯 오늘의 식물 미션",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B3B2B)
                )
                Text(
                    text = "차근차근 하나씩 도전하며 멋진 정원사가 되어봐요!",
                    fontSize = 14.sp,
                    color = Color(0xFF4B6354)
                )
            }
        }

        items(missions) { mission ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMissionDialog(mission) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (mission.isCompleted) Color(0xFFF1F8E9) else Color.White
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (mission.isCompleted) Color(0xFF81C784) else Color(0xFFE0E0E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (mission.isCompleted) Color(0xFF43A047) else Color(0xFFE8F5E9)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mission.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "완료",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text(
                                text = "Lv.${mission.level.levelNum}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mission.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B3B2B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mission.questionPrompt,
                            fontSize = 13.sp,
                            color = Color(0xFF556B2F)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "+${mission.rewardPoints}P",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesTabContent(
    badges: List<Badge>,
    points: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF40833C))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "👑", fontSize = 36.sp)
                    Column {
                        Text(
                            text = "총 획득 별 포인트",
                            color = Color(0xFFE8F5E9),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${points} 포인트",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        items(badges) { badge ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (badge.isUnlocked) Color.White else Color(0xFFEEEEEE)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (badge.isUnlocked) Color(0xFFFEF3C7) else Color(0xFFE0E0E0)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                            fontSize = 28.sp
                        )
                    }

                    Text(
                        text = badge.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) Color(0xFF1B3B2B) else Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = badge.description,
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )

                    if (badge.isUnlocked && badge.unlockedDate != null) {
                        Text(
                            text = "획득: ${badge.unlockedDate}",
                            fontSize = 10.sp,
                            color = Color(0xFF43A047),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotosTabContent(
    photos: List<String>,
    onAddPhotoClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📸 식물 성장 앨범",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B3B2B)
                    )
                    Text(
                        text = "초록이가 쑥쑥 자란 소중한 순간들이에요.",
                        fontSize = 14.sp,
                        color = Color(0xFF556B2F)
                    )
                }

                Button(
                    onClick = onAddPhotoClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("사진 찍기")
                }
            }
        }

        items(photos) { photoCaption ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Photo Placeholder Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🪴", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "스마트팜 카메라 촬영본",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = photoCaption,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )
                }
            }
        }
    }
}

// 1. Mission Dialog with Levels 1..4
@Composable
private fun MissionDetailDialog(
    mission: Mission,
    currentPromptLevel: Int,
    onPromptLevelChange: (Int) -> Unit,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCEDC8)
                    ) {
                        Text(
                            text = mission.level.titleKo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "닫기", tint = Color(0xFF757575))
                    }
                }

                Text(
                    text = mission.questionPrompt,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B3B2B),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F8E9)
                ) {
                    Text(
                        text = "💡 ${mission.helperTip}",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF33691E),
                        textAlign = TextAlign.Center
                    )
                }

                // Big Choices (2~4 per screen)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    mission.options.forEach { option ->
                        Button(
                            onClick = { onOptionSelected(option.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = option.emoji, fontSize = 24.sp)
                                Text(
                                    text = option.text,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Safe Watering Pipeline Execution Modal
@Composable
private fun WateringProgressDialog(
    isLoading: Boolean,
    result: WateringActionResult?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    Text(
                        text = "스마트팜에 물주기 요청 중...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF0288D1),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(70.dp)
                        )
                        Text(text = "💧", fontSize = 28.sp)
                    }

                    Text(
                        text = "수위와 흙의 수분을 안전하게 확인하고 있어요.",
                        fontSize = 13.sp,
                        color = Color(0xFF556B2F),
                        textAlign = TextAlign.Center
                    )
                } else if (result != null) {
                    Text(
                        text = if (result.isSuccess) "물주기 성공! 🎉" else "식물 소식 📢",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isSuccess) Color(0xFF2E7D32) else Color(0xFF0369A1)
                    )

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(if (result.isSuccess) Color(0xFFDCEDC8) else Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (result.isSuccess) "🪴" else "🌱",
                            fontSize = 42.sp
                        )
                    }

                    Text(
                        text = result.userMessage,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B3B2B),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    if (result.isSuccess) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "토양 수분: ${result.soilBefore}% ➔ ${result.soilAfter}% 촉촉해졌어요!",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (result.isSuccess) Color(0xFF43A047) else Color(0xFF0288D1)
                        )
                    ) {
                        Text("확인했어요!", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. Photo Capture Dialog
@Composable
private fun PhotoCaptureDialog(
    plantName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var caption by remember { mutableStateOf("오늘도 건강한 ${plantName}의 모습") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📸 식물 성장 사진 남기기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B2B)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📷", fontSize = 44.sp)
                        Text(text = "찰칵! 예쁜 화분이 담겼어요", fontSize = 13.sp, color = Color(0xFF2E7D32))
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("사진 제목") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = { onSave(caption) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("저장하기", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

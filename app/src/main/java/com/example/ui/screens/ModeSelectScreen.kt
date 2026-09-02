package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.BigTouchButton

@Composable
fun ModeSelectScreen(
    onSelectChildMode: () -> Unit,
    onSelectGuardianMode: () -> Unit,
    onBackClick: () -> Unit,
    isPinModalVisible: Boolean,
    enteredPin: String,
    pinError: String?,
    onPinDigitClick: (String) -> Unit,
    onPinDeleteClick: () -> Unit,
    onPinCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFF1F8E9),
                        Color(0xFFDCEDC8)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .testTag("mode_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color(0xFF2E7D32)
            )
        }

        // Center Choices
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "누가 함께하나요?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B3B2B)
            )
            Text(
                text = "원하는 모드를 선택해주세요.",
                fontSize = 15.sp,
                color = Color(0xFF4B6354)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Child Mode Card
            BigTouchButton(
                title = "아동 모드",
                subtitle = "식물과 대화하고 물을 줘요",
                emoji = "🌱",
                backgroundColor = Color(0xFFC8E6C9),
                contentColor = Color(0xFF1B5E20),
                testTag = "child_mode_button",
                onClick = onSelectChildMode
            )

            // Guardian / Teacher Mode Card
            BigTouchButton(
                title = "보호자·교사 모드",
                subtitle = "활동 통계 및 스마트팜 센서 관리",
                emoji = "🔒",
                backgroundColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1),
                testTag = "guardian_mode_button",
                onClick = onSelectGuardianMode
            )
        }
    }

    // PIN Verification Modal
    if (isPinModalVisible) {
        Dialog(onDismissRequest = onPinCancelClick) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "보호자 인증",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B3B2B)
                        )
                        IconButton(onClick = onPinCancelClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = Color(0xFF757575)
                            )
                        }
                    }

                    Text(
                        text = "보호자 화면 접근을 위한 PIN 4자리를 입력해주세요.\n(기본값: 0000)",
                        fontSize = 13.sp,
                        color = Color(0xFF556B2F),
                        textAlign = TextAlign.Center
                    )

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { idx ->
                            val isFilled = idx < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) Color(0xFF43A047) else Color(0xFFE0E0E0))
                            )
                        }
                    }

                    if (pinError != null) {
                        Text(
                            text = pinError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE53935)
                        )
                    }

                    // Keypad 0-9
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val keyRows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "DEL")
                        )

                        keyRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFF1F5EB))
                                            .clickable {
                                                when (key) {
                                                    "C" -> onPinCancelClick()
                                                    "DEL" -> onPinDeleteClick()
                                                    else -> onPinDigitClick(key)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (key == "DEL") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "지우기",
                                                tint = Color(0xFF556B2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B3B2B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.data.model.SensorHistoryPoint

@Composable
fun ActivityPromptRatioCard(
    activityLogs: List<ActivityLog>,
    modifier: Modifier = Modifier
) {
    val total = activityLogs.size.coerceAtLeast(1)
    val selfInitiatedCount = activityLogs.count { it.promptLevel == 0 }
    val appNotificationCount = activityLogs.count { it.promptLevel == 1 }
    val verbalHelpCount = activityLogs.count { it.promptLevel == 2 }
    val directHelpCount = activityLogs.count { it.promptLevel == 3 }

    val selfRatio = (selfInitiatedCount.toFloat() / total)
    val notifyRatio = (appNotificationCount.toFloat() / total)
    val verbalRatio = (verbalHelpCount.toFloat() / total)
    val directRatio = (directHelpCount.toFloat() / total)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 활동 자발성 및 도움 수준 분석",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B2B)
            )

            Text(
                text = "아동의 자발적 참여 빈도와 보호자 도움 필요 수준을 추적합니다.",
                fontSize = 13.sp,
                color = Color(0xFF556B2F)
            )

            // Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFECEFF1))
            ) {
                if (selfRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(selfRatio)
                            .fillMaxHeight()
                            .background(Color(0xFF43A047))
                    )
                }
                if (notifyRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(notifyRatio)
                            .fillMaxHeight()
                            .background(Color(0xFF4EA8DE))
                    )
                }
                if (verbalRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(verbalRatio)
                            .fillMaxHeight()
                            .background(Color(0xFFFFB703))
                    )
                }
                if (directRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(directRatio)
                            .fillMaxHeight()
                            .background(Color(0xFFFF6B6B))
                    )
                }
            }

            // Legend rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegendItem("자발적 수행 (Level 0)", "${selfInitiatedCount}회 (${(selfRatio * 100).toInt()}%)", Color(0xFF43A047))
                LegendItem("앱 알림 후 수행 (Level 1)", "${appNotificationCount}회 (${(notifyRatio * 100).toInt()}%)", Color(0xFF4EA8DE))
                LegendItem("보호자 언어 도움 (Level 2)", "${verbalHelpCount}회 (${(verbalRatio * 100).toInt()}%)", Color(0xFFFFB703))
                LegendItem("직접적인 도움 (Level 3)", "${directHelpCount}회 (${(directRatio * 100).toInt()}%)", Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, countStr: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(text = label, fontSize = 13.sp, color = Color(0xFF333333))
        }
        Text(text = countStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
    }
}

@Composable
fun SensorHistoryLineChart(
    points: List<SensorHistoryPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 토양 수분 24시간 추이 (%)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B3B2B)
                )
                Text(
                    text = "적정: 35~65%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF43A047)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Safe Zone Background Strip (35% to 65%)
                    val safeTopY = h * (1f - (65f / 100f))
                    val safeBottomY = h * (1f - (35f / 100f))
                    drawRect(
                        color = Color(0xFFE8F5E9).copy(alpha = 0.7f),
                        topLeft = Offset(0f, safeTopY),
                        size = Size(w, safeBottomY - safeTopY)
                    )

                    // Grid Lines
                    drawLine(Color(0xFFE0E0E0), Offset(0f, h * 0.2f), Offset(w, h * 0.2f), strokeWidth = 1f)
                    drawLine(Color(0xFFE0E0E0), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
                    drawLine(Color(0xFFE0E0E0), Offset(0f, h * 0.8f), Offset(w, h * 0.8f), strokeWidth = 1f)

                    if (points.isNotEmpty()) {
                        val stepX = w / (points.size - 1).coerceAtLeast(1)
                        val linePath = Path()

                        points.forEachIndexed { i, point ->
                            val x = i * stepX
                            val y = h * (1f - (point.soilMoisture.coerceIn(0f, 100f) / 100f))

                            if (i == 0) {
                                linePath.moveTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                            }

                            // Data point circle
                            drawCircle(
                                color = Color(0xFF0288D1),
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = linePath,
                            color = Color(0xFF0288D1),
                            style = Stroke(width = 3.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach {
                    Text(text = it.timeLabel, fontSize = 11.sp, color = Color(0xFF888888))
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrowthStage
import com.example.data.model.Plant

@Composable
fun GardenLandscapeBanner(
    plant: Plant,
    completedMissionsCount: Int,
    totalMissionsCount: Int,
    points: Int,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // 1. Sky & Rolling Hills Nature Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky Gradient (Morning Pastel Mint-Sky)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFC8E6C9),
                        Color(0xFFE8F5E9),
                        Color(0xFFDCEDC8)
                    )
                )
            )

            // Warm Morning Sun (rising behind mountains)
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = 38f * sunPulse,
                center = Offset(w * 0.18f, h * 0.32f)
            )
            drawCircle(
                color = Color(0xFFFFE082).copy(alpha = 0.5f),
                radius = 48f * sunPulse,
                center = Offset(w * 0.18f, h * 0.32f)
            )

            // Distant Mountains (Deep Forest Green)
            val mountainPath = Path().apply {
                moveTo(0f, h * 0.48f)
                cubicTo(w * 0.2f, h * 0.35f, w * 0.35f, h * 0.32f, w * 0.52f, h * 0.42f)
                cubicTo(w * 0.7f, h * 0.34f, w * 0.85f, h * 0.38f, w, h * 0.46f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(mountainPath, color = Color(0xFF2E6F40))

            // Pine Trees on distant hill
            fun drawPineTree(x: Float, y: Float, treeH: Float) {
                // Trunk
                drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(x - 3f, y + treeH * 0.6f),
                    size = Size(6f, treeH * 0.4f)
                )
                // Needles layers
                val treePath = Path().apply {
                    moveTo(x, y)
                    lineTo(x + treeH * 0.35f, y + treeH * 0.35f)
                    lineTo(x + treeH * 0.18f, y + treeH * 0.35f)
                    lineTo(x + treeH * 0.45f, y + treeH * 0.65f)
                    lineTo(x + treeH * 0.22f, y + treeH * 0.65f)
                    lineTo(x + treeH * 0.5f, y + treeH * 0.9f)
                    lineTo(x - treeH * 0.5f, y + treeH * 0.9f)
                    lineTo(x - treeH * 0.22f, y + treeH * 0.65f)
                    lineTo(x - treeH * 0.45f, y + treeH * 0.65f)
                    lineTo(x - treeH * 0.18f, y + treeH * 0.35f)
                    lineTo(x - treeH * 0.35f, y + treeH * 0.35f)
                    close()
                }
                drawPath(treePath, color = Color(0xFF43A047))
            }

            drawPineTree(w * 0.12f, h * 0.32f, 48f)
            drawPineTree(w * 0.28f, h * 0.29f, 58f)
            drawPineTree(w * 0.72f, h * 0.28f, 62f)
            drawPineTree(w * 0.86f, h * 0.33f, 52f)
            drawPineTree(w * 0.96f, h * 0.35f, 44f)

            // Middle & Foreground Soft Green Hills
            val middleHill = Path().apply {
                moveTo(0f, h * 0.54f)
                cubicTo(w * 0.35f, h * 0.48f, w * 0.65f, h * 0.58f, w, h * 0.50f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(middleHill, color = Color(0xFF5EAA4A))

            val foregroundHill = Path().apply {
                moveTo(0f, h * 0.64f)
                cubicTo(w * 0.4f, h * 0.68f, w * 0.75f, h * 0.60f, w, h * 0.65f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(foregroundHill, color = Color(0xFF72BA54))
        }

        // 2. Top Bar with Menu & Points Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "메뉴 열기",
                    tint = Color(0xFF2E7D32)
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "포인트",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${points}P",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B3B2B)
                    )
                    Text(
                        text = "• Lv.${plant.growthLevel}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF43A047)
                    )
                }
            }
        }

        // 3. Adventure Quest Status Floating Card (Inspired by reference UI)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(4.dp, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF40833C).copy(alpha = 0.90f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🌱",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "우리 ${plant.nickname}와 함께 모험 중",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "재배 ${plant.plantedDaysAgo}일째",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE8F5E9)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Adventure Stepper Track
                val progressFraction = (completedMissionsCount.toFloat() / totalMissionsCount.coerceAtLeast(1))
                    .coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Background track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    )

                    // Active track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceAtLeast(0.08f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB703))
                    )

                    // Leading Lightning Badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB703)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "에너지",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Floating Child Sprout Character marker
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (280.dp * progressFraction).coerceIn(16.dp, 260.dp))
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌱", fontSize = 16.sp)
                    }

                    // Destination Goal
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍅", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

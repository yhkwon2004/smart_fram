package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrowthStage
import com.example.data.model.PlantMood
import kotlinx.coroutines.delay

@Composable
fun PlantCharacterAvatar(
    stage: GrowthStage,
    mood: PlantMood,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp,
    onClick: () -> Unit = {}
) {
    // Gentle breathing scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "plant_breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Gentle swaying leaf rotation
    val leafSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_sway"
    )

    // Periodic blinking state
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2800..4500).random().toLong())
            isBlinking = true
            delay(160)
            isBlinking = false
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = breatheScale
                scaleY = breatheScale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // 1. Flower Pot Base
            val potTopY = h * 0.58f
            val potBottomY = h * 0.88f
            val potTopWidth = w * 0.55f
            val potBottomWidth = w * 0.42f

            // Pot Shadow
            drawOval(
                color = Color(0x1F000000),
                topLeft = Offset(centerX - potBottomWidth * 0.6f, potBottomY - 4f),
                size = Size(potBottomWidth * 1.2f, 16f)
            )

            // Pot Body
            val potPath = Path().apply {
                moveTo(centerX - potTopWidth / 2f, potTopY)
                lineTo(centerX + potTopWidth / 2f, potTopY)
                lineTo(centerX + potBottomWidth / 2f, potBottomY)
                lineTo(centerX - potBottomWidth / 2f, potBottomY)
                close()
            }
            drawPath(potPath, color = Color(0xFFF3D5B5))

            // Pot Rim
            val rimHeight = 14f
            drawRoundRect(
                color = Color(0xFFE7BC91),
                topLeft = Offset(centerX - (potTopWidth * 0.54f), potTopY - rimHeight / 2f),
                size = Size(potTopWidth * 1.08f, rimHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )

            // Pot Badge/Heart
            drawCircle(
                color = Color(0xFFFF8A80),
                radius = 8f,
                center = Offset(centerX, potTopY + 26f)
            )

            // 2. Plant Stem & Foliage based on GrowthStage
            when (stage) {
                GrowthStage.GERMINATION -> {
                    // Cute Little Seedling Sprout
                    drawRoundRect(
                        color = Color(0xFF6A994E),
                        topLeft = Offset(centerX - 4f, potTopY - 24f),
                        size = Size(8f, 26f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                    // Two tiny cotyledon leaves
                    rotate(leafSway, pivot = Offset(centerX, potTopY - 24f)) {
                        drawOval(
                            color = Color(0xFFA7C957),
                            topLeft = Offset(centerX - 24f, potTopY - 36f),
                            size = Size(20f, 14f)
                        )
                        drawOval(
                            color = Color(0xFF80B918),
                            topLeft = Offset(centerX + 4f, potTopY - 36f),
                            size = Size(20f, 14f)
                        )
                    }
                }
                GrowthStage.SEEDLING, GrowthStage.GROWING, GrowthStage.HARVEST -> {
                    // Main Stem
                    drawRoundRect(
                        color = Color(0xFF43A047),
                        topLeft = Offset(centerX - 6f, potTopY - 50f),
                        size = Size(12f, 52f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Side Leaves
                    rotate(-leafSway * 1.5f, pivot = Offset(centerX - 10f, potTopY - 25f)) {
                        drawOval(
                            color = Color(0xFF66BB6A),
                            topLeft = Offset(centerX - 46f, potTopY - 38f),
                            size = Size(38f, 22f)
                        )
                    }
                    rotate(leafSway * 1.5f, pivot = Offset(centerX + 10f, potTopY - 32f)) {
                        drawOval(
                            color = Color(0xFF81C784),
                            topLeft = Offset(centerX + 8f, potTopY - 44f),
                            size = Size(38f, 22f)
                        )
                    }

                    // Harvest Tomato Berry for Harvest stage
                    if (stage == GrowthStage.HARVEST) {
                        drawCircle(
                            color = Color(0xFFE53935),
                            radius = 16f,
                            center = Offset(centerX + 32f, potTopY - 48f)
                        )
                        drawCircle(
                            color = Color(0xFFFFEB3B),
                            radius = 3f,
                            center = Offset(centerX + 26f, potTopY - 52f)
                        )
                    }
                }
            }

            // 3. Cute Head Character Bubble
            val headRadius = w * 0.23f
            val headCenter = Offset(centerX, potTopY - 60f)

            // Sprout leaves on top of head
            rotate(leafSway, pivot = headCenter) {
                val topLeaf = Path().apply {
                    moveTo(headCenter.x, headCenter.y - headRadius + 4f)
                    cubicTo(
                        headCenter.x - 22f, headCenter.y - headRadius - 32f,
                        headCenter.x + 8f, headCenter.y - headRadius - 38f,
                        headCenter.x, headCenter.y - headRadius + 4f
                    )
                }
                drawPath(topLeaf, color = Color(0xFF43A047))

                val rightLeaf = Path().apply {
                    moveTo(headCenter.x, headCenter.y - headRadius + 4f)
                    cubicTo(
                        headCenter.x + 28f, headCenter.y - headRadius - 26f,
                        headCenter.x + 12f, headCenter.y - headRadius - 12f,
                        headCenter.x, headCenter.y - headRadius + 4f
                    )
                }
                drawPath(rightLeaf, color = Color(0xFF81C784))
            }

            // Character Head Circle
            drawCircle(
                color = Color(0xFFE8F5E9),
                radius = headRadius,
                center = headCenter
            )
            drawCircle(
                color = Color(0xFF81C784),
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = 4f)
            )

            // Rosy Blushes
            drawOval(
                color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                topLeft = Offset(headCenter.x - headRadius * 0.72f, headCenter.y + 4f),
                size = Size(18f, 10f)
            )
            drawOval(
                color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                topLeft = Offset(headCenter.x + headRadius * 0.35f, headCenter.y + 4f),
                size = Size(18f, 10f)
            )

            // 4. Eyes & Mood Expression
            val eyeOffsetY = headCenter.y - 4f
            val eyeSpacing = headRadius * 0.42f

            if (isBlinking || mood == PlantMood.SLEEPING) {
                // Closed cute eyes (^ ^)
                val leftEyePath = Path().apply {
                    moveTo(headCenter.x - eyeSpacing - 8f, eyeOffsetY)
                    quadraticBezierTo(headCenter.x - eyeSpacing, eyeOffsetY - 6f, headCenter.x - eyeSpacing + 8f, eyeOffsetY)
                }
                val rightEyePath = Path().apply {
                    moveTo(headCenter.x + eyeSpacing - 8f, eyeOffsetY)
                    quadraticBezierTo(headCenter.x + eyeSpacing, eyeOffsetY - 6f, headCenter.x + eyeSpacing + 8f, eyeOffsetY)
                }
                drawPath(leftEyePath, color = Color(0xFF1B5E20), style = Stroke(width = 3.5f))
                drawPath(rightEyePath, color = Color(0xFF1B5E20), style = Stroke(width = 3.5f))
            } else {
                // Sparkling Eyes
                drawCircle(
                    color = Color(0xFF1B5E20),
                    radius = 6.5f,
                    center = Offset(headCenter.x - eyeSpacing, eyeOffsetY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.2f,
                    center = Offset(headCenter.x - eyeSpacing - 2f, eyeOffsetY - 2f)
                )

                drawCircle(
                    color = Color(0xFF1B5E20),
                    radius = 6.5f,
                    center = Offset(headCenter.x + eyeSpacing, eyeOffsetY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.2f,
                    center = Offset(headCenter.x + eyeSpacing - 2f, eyeOffsetY - 2f)
                )
            }

            // Smile / Mouth based on mood
            when (mood) {
                PlantMood.HAPPY, PlantMood.HARVEST_READY -> {
                    val mouth = Path().apply {
                        moveTo(headCenter.x - 9f, headCenter.y + 8f)
                        quadraticBezierTo(headCenter.x, headCenter.y + 18f, headCenter.x + 9f, headCenter.y + 8f)
                    }
                    drawPath(mouth, color = Color(0xFF1B5E20), style = Stroke(width = 3.5f))
                }
                PlantMood.THIRSTY -> {
                    // Thirsty open 'O' mouth & sweat droplet
                    drawOval(
                        color = Color(0xFF1B5E20),
                        topLeft = Offset(headCenter.x - 6f, headCenter.y + 8f),
                        size = Size(12f, 9f),
                        style = Stroke(width = 3f)
                    )
                    // Sweat droplet
                    val droplet = Path().apply {
                        moveTo(headCenter.x + headRadius * 0.75f, headCenter.y - 18f)
                        cubicTo(
                            headCenter.x + headRadius * 0.75f + 8f, headCenter.y - 6f,
                            headCenter.x + headRadius * 0.75f - 8f, headCenter.y - 6f,
                            headCenter.x + headRadius * 0.75f, headCenter.y - 18f
                        )
                    }
                    drawPath(droplet, color = Color(0xFF0288D1))
                }
                PlantMood.HOT -> {
                    // Wavy mouth
                    val mouth = Path().apply {
                        moveTo(headCenter.x - 8f, headCenter.y + 12f)
                        lineTo(headCenter.x, headCenter.y + 8f)
                        lineTo(headCenter.x + 8f, headCenter.y + 12f)
                    }
                    drawPath(mouth, color = Color(0xFFE65100), style = Stroke(width = 3f))
                }
                else -> {
                    val mouth = Path().apply {
                        moveTo(headCenter.x - 7f, headCenter.y + 10f)
                        quadraticBezierTo(headCenter.x, headCenter.y + 15f, headCenter.x + 7f, headCenter.y + 10f)
                    }
                    drawPath(mouth, color = Color(0xFF1B5E20), style = Stroke(width = 3f))
                }
            }
        }
    }
}

package com.example.carpetneed.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val unrollProgress = remember { Animatable(0.2f) }

    LaunchedEffect(Unit) {
        unrollProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
        delay(250)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CarpetNeed",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Professional Flooring Utility",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subtle carpet roll unrolling graphic
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val currentW = w * unrollProgress.value

                    // Draw unrolled carpet rug
                    drawRect(
                        color = Color(0xFFFEF3C7),
                        topLeft = Offset(0f, h * 0.15f),
                        size = Size(currentW, h * 0.7f)
                    )

                    drawRect(
                        color = Color(0xFFD97706),
                        topLeft = Offset(0f, h * 0.15f),
                        size = Size(currentW, h * 0.7f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw carpet roll cylinder at right edge
                    val rollRadius = h * 0.35f
                    drawOval(
                        color = Color(0xFFB45309),
                        topLeft = Offset(currentW - 14.dp.toPx(), h * 0.15f),
                        size = Size(18.dp.toPx(), h * 0.7f)
                    )

                    // Center line on carpet
                    drawLine(
                        color = Color(0xFFD97706).copy(alpha = 0.5f),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(currentW - 14.dp.toPx(), h * 0.5f),
                        strokeWidth = 1.5f.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Room: 5 × 4 m",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Carpet: 5.00 m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

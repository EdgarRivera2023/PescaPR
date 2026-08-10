package com.bradmir.pescapr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RelojMareasCircular(valor: Float, nextTime: String = "") {
    val valorAnimado by animateFloatAsState(targetValue = valor, animationSpec = tween(durationMillis = 1000))

    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        if (nextTime.isNotEmpty()) {
            val alignment = if (valor < 0.5f) Alignment.TopCenter else Alignment.BottomCenter
            Text(
                text = nextTime,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(alignment).padding(vertical = 4.dp)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val strokeWidth = 10f
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Arcos de fondo (según posiciones de reloj)
            // Verde: 10 a 1 o'clock
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = 210f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            // Naranja: 1 a 3 o'clock
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = 300f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            // Rojo: 3 a 8 o'clock
            drawArc(
                color = Color(0xFFF44336),
                startAngle = 0f,
                sweepAngle = 150f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            // Naranja: 8 a 10 o'clock
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = 150f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )

            val angle = 90f + (valorAnimado * 360f)
            val angleRad = Math.toRadians(angle.toDouble())
            val lineLength = radius * 0.8f
            val endX = center.x + lineLength * cos(angleRad).toFloat()
            val endY = center.y + lineLength * sin(angleRad).toFloat()

            drawLine(color = Color.DarkGray, start = center, end = Offset(endX, endY), strokeWidth = 6f, cap = StrokeCap.Round)
            drawCircle(Color.DarkGray, radius = 8f, center = center)
        }
    }
}

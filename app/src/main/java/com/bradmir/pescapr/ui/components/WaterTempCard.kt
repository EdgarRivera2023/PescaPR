package com.bradmir.pescapr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.utils.ThermalTrend
import com.bradmir.pescapr.utils.ThermalTrendResult
import java.util.Locale

@Composable
fun WaterTempCard(
    isPro: Boolean,
    currentWaterTemp: Float?,
    trendResult: ThermalTrendResult?,
    onUpgradeClick: () -> Unit = {},
) {
    if (!isPro) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Temperatura del Agua & Tendencia Térmica",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Desbloquea tendencias térmicas de 7 días y temperatura del agua en tiempo real con PescaPR Pro.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Actualizar a Pro", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Temperatura del Agua",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Temperatura del Agua",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "PRO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentWaterTemp?.let { "${String.format(Locale.US, "%.1f", it)}°F" } ?: "N/A",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if ((trendResult?.averageTemp ?: 0f) > 0f) {
                            Text(
                                text = "Promedio 7d: ${String.format(Locale.US, "%.1f", trendResult?.averageTemp)}°F",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    val (trendIcon, trendColor, trendLabel) = when (trendResult?.trend) {
                        ThermalTrend.WARMING -> Triple(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            Color(0xFFE53935),
                            "En Calentamiento"
                        )
                        ThermalTrend.COOLING -> Triple(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            Color(0xFF1E88E5),
                            "En Enfriamiento"
                        )
                        ThermalTrend.STABLE -> Triple(
                            Icons.AutoMirrored.Filled.TrendingFlat,
                            Color.Gray,
                            "Estable"
                        )
                        ThermalTrend.INSUFFICIENT_DATA, null -> Triple(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            Color.Gray,
                            "Datos Insuficientes"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = trendLabel,
                            tint = trendColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = trendLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }

                val desc = trendResult?.description
                if (!desc.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

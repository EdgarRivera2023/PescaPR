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
import com.bradmir.pescapr.R
import androidx.compose.ui.res.stringResource
import java.util.Locale

@Composable
fun WaterTempCard(
    isPro: Boolean,
    currentWaterTemp: Float?,
    trendResult: ThermalTrendResult?,
    ambientAirTempF: Float? = null,
    onUpgradeClick: () -> Unit = {},
) {
    if (!isPro) return

    val effectiveTemp = when {
        currentWaterTemp != null && currentWaterTemp > 0f -> currentWaterTemp
        ambientAirTempF != null && ambientAirTempF > 0f -> ambientAirTempF - 2.0f
        else -> null
    }
    val isEstimated = (currentWaterTemp == null || currentWaterTemp <= 0f) && effectiveTemp != null

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
                            contentDescription = stringResource(R.string.water_temp_title),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.water_temp_title),
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
                            text = effectiveTemp?.let { "${String.format(Locale.US, "%.1f", it)}°F" } ?: stringResource(R.string.not_available),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isEstimated) {
                            Text(
                                text = stringResource(R.string.water_temp_estimated),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if ((trendResult?.averageTemp ?: 0f) > 0f) {
                            Text(
                                text = stringResource(R.string.water_temp_average_7d, String.format(Locale.US, "%.1f", trendResult?.averageTemp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    val (trendIcon, trendColor, trendLabel) = when (trendResult?.trend) {
                        ThermalTrend.WARMING -> Triple(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            Color(0xFFE53935),
                            stringResource(R.string.water_temp_warming)
                        )
                        ThermalTrend.COOLING -> Triple(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            Color(0xFF1E88E5),
                            stringResource(R.string.water_temp_cooling)
                        )
                        ThermalTrend.STABLE -> Triple(
                            Icons.AutoMirrored.Filled.TrendingFlat,
                            Color.Gray,
                            stringResource(R.string.water_temp_stable)
                        )
                        ThermalTrend.INSUFFICIENT_DATA, null -> Triple(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            Color.Gray,
                            stringResource(R.string.water_temp_insufficient)
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

package com.bradmir.pescapr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.data.GoldenDayPrediction
import com.bradmir.pescapr.data.ProSwellMetrics
import com.bradmir.pescapr.data.isGoldenDay

@Composable
fun ProSwellCard(metrics: ProSwellMetrics?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.4f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Water, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Métricas Pro Marejada", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = CircleShape
                ) {
                    Text(
                        text = "SCORE: ${metrics?.score ?: "-"}/10",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SwellInfoItem(label = "Altura", value = "${metrics?.heightFt ?: "-"} ft")
                SwellInfoItem(label = "Periodo", value = "${metrics?.periodSec ?: "-"} sec")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val rotation = (metrics?.directionDeg ?: 0f)
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Dirección",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text("Dirección", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SwellInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun GoldenDayBanner(
    swellMetrics: ProSwellMetrics?,
    modifier: Modifier = Modifier
) {
    if (swellMetrics == null || !swellMetrics.isGoldenDay) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Día Dorado",
                tint = Color(0xFF3E2723),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "¡Día Dorado de Pesca! Condiciones óptimas de oleaje y período.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
        }
    }
}

enum class ProFeatureType {
    PLANIFICADOR,
    TEMP_TENDENCIA,
    MAREJADAS,
    MORFOLOGIA
}

@Composable
fun ProFeaturePaywallDialog(
    feature: ProFeatureType,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val (title, description, icon) = when (feature) {
        ProFeatureType.PLANIFICADOR -> Triple(
            "Planificador de Pesca Pro",
            "El Planificador de Pesca analiza el alineamiento de oleaje, período y viento para predecir los mejores días de picada y Días Dorados en las costas de Puerto Rico.",
            Icons.Default.Star
        )
        ProFeatureType.TEMP_TENDENCIA -> Triple(
            "Temp & Tendencia del Agua",
            "Obtén la temperatura del agua en tiempo real y el análisis de tendencia térmica de 7 días para predecir el comportamiento de los peces.",
            Icons.Default.Thermostat
        )
        ProFeatureType.MAREJADAS -> Triple(
            "Métricas Pro de Marejadas",
            "Accede a datos precisos de altura de olas en pies, período de marejada en segundos y dirección del oleaje en tiempo real.",
            Icons.Default.Water
        )
        ProFeatureType.MORFOLOGIA -> Triple(
            "Morfología Costera Pro",
            "Identifica fosa de orilla, canales profundos, arrecifes y comederos sumergidos en las costas de Puerto Rico con PescaPR Pro.",
            Icons.Default.Layers
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onUpgradeClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Obtener PescaPR Pro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ProFeatureActionButtons(
    selectedFeature: ProFeatureType? = null,
    onFeatureClick: (ProFeatureType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Funciones Pro para este Spot",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        val planificadorSelected = selectedFeature == ProFeatureType.PLANIFICADOR
        if (planificadorSelected) {
            FilledTonalButton(
                onClick = { onFeatureClick(ProFeatureType.PLANIFICADOR) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Planificador de Pesca", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = { onFeatureClick(ProFeatureType.PLANIFICADOR) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Planificador de Pesca", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        val tempSelected = selectedFeature == ProFeatureType.TEMP_TENDENCIA
        if (tempSelected) {
            FilledTonalButton(
                onClick = { onFeatureClick(ProFeatureType.TEMP_TENDENCIA) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Thermostat, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Temp & Tendencia del Agua", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = { onFeatureClick(ProFeatureType.TEMP_TENDENCIA) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Thermostat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Temp & Tendencia del Agua", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        val marejadasSelected = selectedFeature == ProFeatureType.MAREJADAS
        if (marejadasSelected) {
            FilledTonalButton(
                onClick = { onFeatureClick(ProFeatureType.MAREJADAS) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Water, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Métricas de Marejadas", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = { onFeatureClick(ProFeatureType.MAREJADAS) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Water, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Métricas de Marejadas", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun GoldenDayPlannerCard(
    swellMetrics: ProSwellMetrics?,
    modifier: Modifier = Modifier
) {
    val heightFt = swellMetrics?.heightFt?.toDouble() ?: 0.0
    val periodSec = swellMetrics?.periodSec?.toDouble() ?: 0.0
    val score = com.bradmir.pescapr.data.calculatePlannerScore(heightFt, periodSec)
    val explanation = com.bradmir.pescapr.data.getGoldenDayExplanation(heightFt, periodSec)
    val isGolden = com.bradmir.pescapr.data.isGoldenDay(heightFt, periodSec)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGolden) Color(0xFFFFF8E1) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isGolden) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isGolden) Color(0xFFF57F17) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Planificador de Pesca",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = if (isGolden) Color(0xFFFFD700) else MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "SCORE: $score/10",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isGolden) Color(0xFF3E2723) else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isGolden) {
                GoldenDayBanner(swellMetrics = swellMetrics)
                Spacer(Modifier.height(12.dp))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Análisis de Condiciones de la Costa:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Período", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", periodSec)}s",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Altura Olas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", heightFt)}ft",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenDayPlannerSheet(
    predictions: List<GoldenDayPrediction>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Planificador de Pesca (30 Días)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ventanas de Marea Dorada (Marea alta dentro de ±120 min de Amanecer/Atardecer)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (predictions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                ) {
                    Text(
                        text = "No se detectaron Coincidencias de Marea Dorada en los próximos 30 días.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(predictions) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.date,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3E2723)
                                    )

                                    Surface(
                                        color = Color(0xFFFFD700),
                                        shape = CircleShape
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (item.windowType == "Amanecer") Icons.Default.WbSunny else Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFF3E2723),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${item.windowType} Dorado",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3E2723)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Marea Alta: ${item.highTideTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4E342E)
                                    )
                                    Text(
                                        text = "Sol: ${item.sunEventTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4E342E)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D4037)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF8D6E63),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Consejo Pro: Revisa las 'Métricas de Marejadas' para ver altura y período en esa fecha.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8D6E63)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

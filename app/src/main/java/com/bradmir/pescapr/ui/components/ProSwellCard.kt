package com.bradmir.pescapr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.data.ProSwellMetrics

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
                    Text("Métricas Pro Swell", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

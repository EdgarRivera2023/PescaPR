package com.bradmir.pescapr.ui.records

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.AppDatabase
import com.bradmir.pescapr.BuildConfig
import com.bradmir.pescapr.RecordEntity
import com.bradmir.pescapr.RecordPesca
import com.bradmir.pescapr.data.CatchRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun generarAIPatternInsights(records: List<RecordPesca>): String = withContext(Dispatchers.IO) {
    try {
        val aiKey = BuildConfig.GEMINI_API_KEY
        if (aiKey.isBlank()) return@withContext "Error: API Key faltante"
        if (records.isEmpty()) return@withContext "No tienes suficientes capturas para generar un análisis."

        val historyText = StringBuilder("HISTORIAL DE CAPTURAS:\n")
        records.forEach { r ->
            historyText.append("- ${r.nombrePez} en ${r.lugar} el ${r.fecha}. Clima: ${r.climaTemp}, Viento: ${r.climaWind}, Marea: ${r.climaTide}\n")
        }

        val prompt = """
            $historyText

            TAREA:
            Eres un experto analista de pesca en Puerto Rico.
            Analiza el historial de capturas arriba y encuentra patrones de éxito.
            Identifica:
            1. Mejores momentos del día o condiciones climáticas para ciertas especies.
            2. Lugares más productivos.
            3. Recomendaciones para futuras salidas basadas en estos datos.

            Responde de forma concisa y motivadora en español, usando bullets.
        """.trimIndent()

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = aiKey
        )

        val response = generativeModel.generateContent(prompt)
        response.text ?: "No se pudo generar el análisis en este momento."
    } catch (e: Exception) {
        e.printStackTrace()
        "Error analizando patrones: ${e.message}"
    }
}

// --- TAB 4: MIS RÉCORDS ---
@Composable
fun PantallaRecordsTab(database: AppDatabase, repository: CatchRepository, onIrALugar: (String) -> Unit = {}) {
    val context = LocalContext.current
    val recordDao = remember { database.recordDao() }
    val coroutineScope = rememberCoroutineScope()
    val records = remember { mutableStateListOf<RecordPesca>() }
    val spots = remember { mutableStateMapOf<String, String>() } // spotId -> spotNombre

    // Estado para Edición
    var recordParaEditar by remember { mutableStateOf<RecordPesca?>(null) }
    var mostrarDialogoEdit by remember { mutableStateOf(false) }

    // Estado para AI Insights
    var aiInsightsResult by remember { mutableStateOf("") }
    var analizandoAI by remember { mutableStateOf(false) }
    var mostrarDialogoAI by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        database.spotDao().getAllSpots().collect { entities ->
            entities.forEach { spots[it.id.toString()] = it.nombre }
        }
    }

    LaunchedEffect(Unit) {
        repository.localRecords.collect { entities ->
            records.clear()
            records.addAll(entities.map { entity ->
                RecordPesca(
                    id = entity.id.toString(),
                    nombrePez = entity.nombrePez,
                    peso = entity.peso,
                    longitud = entity.longitud,
                    lugar = entity.lugar,
                    fecha = entity.fecha,
                    spotId = entity.spotId.toString(),
                    fishId = entity.fishId,
                    fotosUrls = entity.fotosUrls
                )
            })
        }
    }

    val groupedRecords = records.groupBy { it.nombrePez }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Diario Privado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text("Sincronizado", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                } else {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    FirebaseAuth.getInstance().signInAnonymously().await()
                                    Toast.makeText(context, "Sincronización activada", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al activar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Activar Sincronización (Pro)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        analizandoAI = true
                        mostrarDialogoAI = true
                        aiInsightsResult = generarAIPatternInsights(records)
                        analizandoAI = false
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Insights", tint = MaterialTheme.colorScheme.secondary)
            }
        }

        HorizontalDivider()

        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes capturas registradas.", color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedRecords.forEach { (especie, capturas) ->
                    var expandido by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = { expandido = !expandido }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Waves, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Text(especie, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text("${capturas.size} capturas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }

                            if (expandido) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(thickness = 0.5.dp)
                                Spacer(Modifier.height(8.dp))

                                capturas.forEach { record ->
                                    val isSpotValid = record.spotId.isNotBlank() && record.spotId != "0"
                                    val nombreLugar = if (isSpotValid) spots[record.spotId] ?: record.lugar else record.lugar
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(
                                            modifier = if (isSpotValid) {
                                                Modifier.clickable { onIrALugar(record.spotId) }
                                            } else Modifier
                                        ) {
                                            Text(nombreLugar, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(record.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${record.peso} | ${record.longitud}", style = MaterialTheme.typography.bodySmall)
                                            Row {
                                                if (isSpotValid) {
                                                    IconButton(onClick = {
                                                        onIrALugar(record.spotId)
                                                    }, modifier = Modifier.size(24.dp)) {
                                                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                                    }
                                                }
                                                IconButton(onClick = {
                                                    recordParaEditar = record
                                                    mostrarDialogoEdit = true
                                                }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                }
                                                IconButton(onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            val idInt = record.id.toIntOrNull() ?: 0
                                                            val spotIdInt = record.spotId.toIntOrNull() ?: 0
                                                            val entity = RecordEntity(
                                                                id = idInt,
                                                                nombrePez = record.nombrePez,
                                                                peso = record.peso,
                                                                longitud = record.longitud,
                                                                lugar = record.lugar,
                                                                fecha = record.fecha,
                                                                fotosUrls = record.fotosUrls,
                                                                spotId = spotIdInt,
                                                                fishId = record.fishId,
                                                                climaTemp = record.climaTemp,
                                                                climaWind = record.climaWind,
                                                                climaPressure = record.climaPressure,
                                                                climaTide = record.climaTide
                                                            )
                                                            recordDao.deleteRecord(entity)
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
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
    }

    if (mostrarDialogoAI) {
        AlertDialog(
            onDismissRequest = { if (!analizandoAI) mostrarDialogoAI = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("AI Insights")
                }
            },
            text = {
                if (analizandoAI) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Text(aiInsightsResult, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoAI = false }, enabled = !analizandoAI) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (mostrarDialogoEdit && recordParaEditar != null) {
        var nombrePez by remember { mutableStateOf("") }
        var peso by remember { mutableStateOf("") }
        var longitud by remember { mutableStateOf("") }
        var guardando by remember { mutableStateOf(false) }

        LaunchedEffect(recordParaEditar) {
            nombrePez = recordParaEditar?.nombrePez ?: ""
            peso = recordParaEditar?.peso ?: ""
            longitud = recordParaEditar?.longitud ?: ""
        }

        AlertDialog(
            onDismissRequest = { if (!guardando) mostrarDialogoEdit = false },
            title = { Text("Editar Captura") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nombrePez, onValueChange = { nombrePez = it }, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = longitud, onValueChange = { longitud = it }, label = { Text("Longitud") }, modifier = Modifier.weight(1f))
                    }
                    if (guardando) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val currentRecord = recordParaEditar
                    if (currentRecord != null) {
                        guardando = true
                        coroutineScope.launch {
                            try {
                                val idInt = currentRecord.id.toIntOrNull() ?: 0
                                val spotIdInt = currentRecord.spotId.toIntOrNull() ?: 0
                                val entity = RecordEntity(
                                    id = idInt,
                                    nombrePez = nombrePez,
                                    peso = peso,
                                    longitud = longitud,
                                    lugar = currentRecord.lugar,
                                    fecha = currentRecord.fecha,
                                    fotosUrls = currentRecord.fotosUrls,
                                    spotId = spotIdInt,
                                    fishId = currentRecord.fishId
                                )
                                recordDao.updateRecord(entity)
                                guardando = false
                                mostrarDialogoEdit = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                                guardando = false
                            }
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEdit = false }) { Text("Cancelar") }
            }
        )
    }
}

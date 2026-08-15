package com.bradmir.pescapr.ui.identificador

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.BuildConfig
import com.bradmir.pescapr.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class ResultadoIdentificacion(
    val nombreComun: String = "",
    val nombreCientifico: String = "",
    val nombreIngles: String = "",
    val regulacionComercial: String = "",
    val regulacionRecreativa: String = "",
    val caracteristicas: String = "",
    val certeza: String = "0%",
    val esError: Boolean = false
)

@Composable
fun PantallaIdentificadorYRegulacionesTab() {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance("pescapr") }
    val coroutineScope = rememberCoroutineScope()

    var bitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
    var analizando by remember { mutableStateOf(false) }
    var analizadoCompleto by remember { mutableStateOf(false) }
    val identifierInitialState = stringResource(R.string.identifier_initial_state)
    var datosIdentificacion by remember { mutableStateOf(ResultadoIdentificacion(nombreComun = identifierInitialState)) }

    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { bitmapSeleccionado = bitmap; analizadoCompleto = false }
    }
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) { bitmapSeleccionado = bitmap; analizadoCompleto = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.identifier_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            if (bitmapSeleccionado == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text(stringResource(R.string.identifier_photo_instruction), color = Color.Gray)
                }
            } else {
                Image(bitmap = bitmapSeleccionado!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            if (analizando) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { camaraLauncher.launch(null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.identifier_take_photo))
            }
            OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Collections, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.identifier_choose_gallery))
            }
        }

        Button(
            onClick = {
                bitmapSeleccionado?.let { img ->
                    coroutineScope.launch {
                        analizando = true
                        analizadoCompleto = false
                        // Matching logic
                        datosIdentificacion = ejecutarMatchingConFichas(db, img)
                        analizando = false
                        analizadoCompleto = true
                    }
                }
            },
            enabled = bitmapSeleccionado != null && !analizando,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.identifier_validate))
        }

        AnimatedVisibility(visible = analizadoCompleto && !analizando) {
            ResultadosFichaMatchCard(datosIdentificacion, db)
        }
    }
}

@Composable
fun ResultadosFichaMatchCard(datos: ResultadoIdentificacion, db: FirebaseFirestore) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var reportado by remember { mutableStateOf(false) }

    val colorEstatus = Color(0xFF4CAF50) // Color neutral o basado en regulación

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colorEstatus.copy(0.1f)), border = BorderStroke(1.dp, colorEstatus)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.identifier_result_title), fontWeight = FontWeight.Black, color = colorEstatus)
                Text(stringResource(R.string.identifier_confidence, datos.certeza), style = MaterialTheme.typography.bodySmall)
            }
            Text(text = datos.nombreComun, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (datos.nombreIngles.isNotBlank()) {
                Text(text = stringResource(R.string.identifier_english_name, datos.nombreIngles), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            }
            Text(text = datos.nombreCientifico, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            HorizontalDivider(thickness = 0.5.dp, color = colorEstatus.copy(0.3f))

            Text(stringResource(R.string.identifier_commercial_regulation), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = datos.regulacionComercial, style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.identifier_recreational_regulation), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = datos.regulacionRecreativa, style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.identifier_characteristics), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = datos.caracteristicas, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            if (!reportado) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            db.collection("reportes_error").add(hashMapOf(
                                "nombreDetectado" to datos.nombreComun,
                                "certeza" to datos.certeza,
                                "timestamp" to System.currentTimeMillis()
                            )).await()
                            Toast.makeText(context, context.getString(R.string.identifier_report_sent), Toast.LENGTH_SHORT).show()
                            reportado = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Error, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.identifier_report_incorrect))
                }
            } else {
                Text(stringResource(R.string.identifier_report_thanks), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

suspend fun ejecutarMatchingConFichas(db: FirebaseFirestore, userImage: Bitmap): ResultadoIdentificacion = withContext(Dispatchers.IO) {
    try {
        val aiKey = BuildConfig.GEMINI_API_KEY
        if (aiKey.isBlank()) return@withContext ResultadoIdentificacion(nombreComun = "Error: API Key faltante", esError = true)

        val fichasSnap = db.collection("fichas_peces").get().await()

        if (fichasSnap.isEmpty) {
            return@withContext ResultadoIdentificacion("No hay fichas de referencia", esError = true)
        }

        // 1. Construir contexto de fichas descriptivo
        val contextText = StringBuilder("ESTAS SON LAS FICHAS OFICIALES DE REFERENCIA:\n")

        fichasSnap.documents.forEachIndexed { index, doc ->
            val nombre = doc.getString("nombreComun") ?: "Pez $index"
            val nombreIng = doc.getString("nombreIngles") ?: ""
            val caracs = (doc.get("caracteristicas") as? List<*>)?.joinToString("\n- ") ?: ""
            contextText.append("""
                Ficha #$index ($nombre):
                - English: $nombreIng
                - Científico: ${doc.getString("nombreCientifico")}
                - Reg. Comercial: ${doc.getString("regulacionComercial")}
                - Reg. Recreativa: ${doc.getString("regulacionRecreativa")}
                - Características:
                - $caracs
                - Confundido con: ${doc.getString("puedeSerConfundidoCon")}
            """.trimIndent() + "\n")
        }

        val fullPrompt = """
            $contextText

            TAREA:
            Analiza la imagen de la captura del usuario.
            Compara sus rasgos físicos (forma, color, aletas) con las FICHAS OFICIALES descritas arriba.

            Determina cuál ficha corresponde al pez de la foto.
            Si el pez NO coincide con ninguna ficha, indica que es una especie desconocida para este compendio.

            Responde ÚNICAMENTE en este formato JSON:
            {
              "nombreComun": "Nombre Común",
              "nombreIngles": "English Name",
              "nombreCientifico": "Científico",
              "regulacionComercial": "Regulación Comercial según la ficha",
              "regulacionRecreativa": "Regulación Recreativa según la ficha",
              "caracteristicas": "Resumen de características coincidentes",
              "certeza": "X%"
            }
        """.trimIndent()

        // 2. Usar el SDK oficial de Gemini con el nombre de modelo detectado en tu consola
        // Usamos gemini-2.5-flash según la información de tu panel de AI Studio
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = aiKey
        )

        val inputContent = content {
            image(userImage)
            text(fullPrompt)
        }

        val response = generativeModel.generateContent(inputContent)
        val textResponse = response.text ?: ""

        // Limpiar JSON por si Gemini añade bloques de código
        val cleanJson = textResponse.replace("```json", "").replace("```", "").trim()

        if (cleanJson.startsWith("{")) {
            com.google.gson.Gson().fromJson(cleanJson, ResultadoIdentificacion::class.java)
        } else {
            ResultadoIdentificacion(nombreComun = "Error: Respuesta no es JSON", esError = true)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        ResultadoIdentificacion(
            nombreComun = "Error de Identificación",
            nombreCientifico = e.localizedMessage ?: "Fallo de conexión",
            esError = true
        )
    }
}

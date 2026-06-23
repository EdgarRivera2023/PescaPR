package com.bradmir.pescapr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// --- 1. MODELOS DE DATOS ---
data class PuntoPesca(
    val id: String = "",
    val coordenada: LatLng = LatLng(0.0, 0.0),
    val nombre: String = "",
    val descripcion: String = "",
    val fotosUrls: List<String> = emptyList()
)

data class WeatherResponse(val main: MainData, val wind: WindData)
data class MainData(val temp: Float, val pressure: Int)
data class WindData(val speed: Float)

data class ResultadoIdentificacion(
    val nombreComun: String = "Identificando...",
    val nombreLocalPr: String = "",
    val nombreCientifico: String = "",
    val regulacionDrna: String = "",
    val certeza: String = "0%",
    val esError: Boolean = false
)

// --- RETROFIT: SERVICIOS ---
interface WeatherService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial",
        @Query("lang") lang: String = "es"
    ): WeatherResponse
}

data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String? = null, val inline_data: GeminiInlineData? = null)
data class GeminiInlineData(val mime_type: String, val data: String)

data class GeminiResponse(val candidates: List<GeminiCandidate>?)
data class GeminiCandidate(val content: GeminiContentResponse?)
data class GeminiContentResponse(val parts: List<GeminiPartResponse>?)
data class GeminiPartResponse(val text: String?)

interface GeminiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- 2. ACTIVIDAD PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MainTabsScreen()
            }
        }
    }
}

// --- 3. PANTALLA PRINCIPAL CON TABS ---
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen() {
    val tabs = listOf("Mapa", "Identificador & Reg.", "Mis Récords")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val idLogo = context.resources.getIdentifier("logo_small", "drawable", context.packageName)

            if (idLogo != 0) {
                Image(
                    painter = painterResource(id = idLogo),
                    contentDescription = "Logo PescaPR",
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.SetMeal,
                    contentDescription = "Logo Temporal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "PescaPR",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> PantallaMapaTab()
                1 -> PantallaIdentificadorYRegulacionesTab()
                2 -> PantallaRecordsTab()
            }
        }
    }
}

// --- 4. CONTENIDO DE CADA PESTAÑA ---
@Composable
fun PantallaMapaTab() {
    MapaPescapr()
}

@Composable
fun PantallaIdentificadorYRegulacionesTab() {
    val context = LocalContext.current
    val db = remember { Firebase.firestore }

    var bitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
    var analizando by remember { mutableStateOf<Boolean>(false) }
    var analizadoCompleto by remember { mutableStateOf<Boolean>(false) }
    var guardandoRecord by remember { mutableStateOf<Boolean>(false) }
    val coroutineScope = rememberCoroutineScope()

    var datosIdentificacion by remember { mutableStateOf(ResultadoIdentificacion()) }

    var mostrarCorreccionManual by remember { mutableStateOf(false) }
    var textoCorreccion by remember { mutableStateOf("") }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapSeleccionado = bitmap
            analizadoCompleto = false
            mostrarCorreccionManual = false
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    bitmapSeleccionado = bitmap
                    analizadoCompleto = false
                    mostrarCorreccionManual = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Identificador & Ley de Pesca",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmapSeleccionado == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toma una foto para verificar regulaciones", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Image(
                    bitmap = bitmapSeleccionado!!.asImageBitmap(),
                    contentDescription = "Captura cargada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (analizando) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { camaraLauncher.launch(null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cámara")
            }
            OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Collections, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }

        Button(
            onClick = {
                bitmapSeleccionado?.let { img ->
                    coroutineScope.launch {
                        analizando = true
                        analizadoCompleto = false
                        mostrarCorreccionManual = false

                        val resultado = ejecutarAnalisisDePezIA(img)

                        datosIdentificacion = resultado
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
            Icon(Icons.Default.Psychology, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear y Validar Ley")
        }

        AnimatedVisibility(visible = analizadoCompleto && !analizando) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val colorTarjeta = if (datosIdentificacion.esError) MaterialTheme.colorScheme.errorContainer.copy(0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(0.2f)
                val colorBorde = if (datosIdentificacion.esError) Color(0xFFF44336).copy(0.3f) else MaterialTheme.colorScheme.primary.copy(0.3f)
                val colorTextoTitulo = if (datosIdentificacion.esError) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorTarjeta),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colorBorde)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("REGULACIÓN DETECTADA", style = MaterialTheme.typography.labelMedium, color = colorTextoTitulo, fontWeight = FontWeight.Bold)
                            Text("Certeza: ${datosIdentificacion.certeza}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = colorBorde)

                        Text(text = datosIdentificacion.nombreComun, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (datosIdentificacion.nombreLocalPr.isNotBlank()) {
                            Text(
                                text = "En PR: ${datosIdentificacion.nombreLocalPr}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(text = datosIdentificacion.nombreCientifico, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (datosIdentificacion.esError) Color(0xFFF8D7DA) else MaterialTheme.colorScheme.primaryContainer.copy(0.4f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (datosIdentificacion.esError) Icons.Default.Gavel else Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (datosIdentificacion.esError) Color(0xFF721C24) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = datosIdentificacion.regulacionDrna,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (datosIdentificacion.esError) Color(0xFF721C24) else Color.Unspecified,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!datosIdentificacion.esError) {
                    Button(
                        onClick = {
                            guardandoRecord = true
                            val recordData = hashMapOf(
                                "nombreComun" to datosIdentificacion.nombreComun,
                                "nombreLocalPr" to datosIdentificacion.nombreLocalPr,
                                "nombreCientifico" to datosIdentificacion.nombreCientifico,
                                "regulacion" to datosIdentificacion.regulacionDrna,
                                "timestamp" to System.currentTimeMillis()
                            )

                            db.collection("mis_records")
                                .add(recordData)
                                .addOnSuccessListener {
                                    guardandoRecord = false
                                    Toast.makeText(context, "¡Récord guardado exitosamente!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    guardandoRecord = false
                                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !guardandoRecord
                    ) {
                        if (guardandoRecord) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar en Mis Récords")
                        }
                    }

                    TextButton(
                        onClick = { mostrarCorreccionManual = !mostrarCorreccionManual },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿La IA se equivocó? Corregir manualmente")
                    }
                }

                AnimatedVisibility(visible = mostrarCorreccionManual) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Corrección Manual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Ingresa el nombre común o científico para buscar fotos reales en Google y verificar.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            OutlinedTextField(
                                value = textoCorreccion,
                                onValueChange = { textoCorreccion = it },
                                label = { Text("Nombre del pez") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (textoCorreccion.isNotBlank()) {
                                            val query = Uri.encode("$textoCorreccion pez Puerto Rico")
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?tbm=isch&q=$query"))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver Fotos", style = MaterialTheme.typography.labelMedium)
                                }

                                Button(
                                    onClick = {
                                        if (textoCorreccion.isNotBlank()) {
                                            datosIdentificacion = datosIdentificacion.copy(
                                                nombreComun = textoCorreccion,
                                                nombreLocalPr = "Corregido Manualmente",
                                                nombreCientifico = "Especie no verificada por IA",
                                                certeza = "100% (Usuario)",
                                                regulacionDrna = "Por favor, verifica el compendio oficial del DRNA para esta especie."
                                            )
                                            mostrarCorreccionManual = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aplicar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Regulaciones Generales del DRNA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Especies Protegidas en PR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Text("Prohibida totalmente la captura y desembarco de Tiburón Tintorera, Tiburón Ballena y Manatíes.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Zonas de Reserva Natural", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("La pesca dentro del Canal de Luis Peña en Culebra y la Isla de Mona cuenta con restricciones especiales de captura y liberación.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// --- ENGINE DE ANÁLISIS ---
suspend fun ejecutarAnalisisDePezIA(imagen: Bitmap): ResultadoIdentificacion = withContext(Dispatchers.IO) {
    try {
        val aiKey = BuildConfig.GEMINI_API_KEY
        if (aiKey.isBlank()) {
            return@withContext ResultadoIdentificacion("Llave de API vacía", "", "", "La propiedad no se inyectó.", "0%", true)
        }

        val stream = ByteArrayOutputStream()
        imagen.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageBase64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GeminiService::class.java)

        val prompt = """
            Analiza esta imagen de pesca capturada en Puerto Rico. Identifica el pez y devuelve la información EXACTAMENTE en este formato de 5 líneas, sin saludos, ni asteriscos, ni markdown adicionales:
            Linea 1: Nombre Común (ej: French Grunt)
            Linea 2: Nombre coloquial o slang usado por pescadores en Puerto Rico (ej: Ronco Amarillo, Arrayado, etc.)
            Linea 3: Nombre Científico
            Linea 4: Regulaciones de tamaño mínimo y cuotas del DRNA aplicables a esta especie de forma concisa.
            Linea 5: Porcentaje estimado de certeza visual (ej: 95%)
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inline_data = GeminiInlineData("image/jpeg", imageBase64))
                    )
                )
            )
        )

        val response = service.generateContent("gemini-2.5-flash", aiKey, requestBody)
        val textoGenerado = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        val lineas = textoGenerado.lines().map { it.trim() }.filter { it.isNotEmpty() }

        if (lineas.size >= 5) {
            ResultadoIdentificacion(
                nombreComun = lineas[0].substringAfter(":").trim(),
                nombreLocalPr = lineas[1].substringAfter(":").trim(),
                nombreCientifico = lineas[2].substringAfter(":").trim(),
                regulacionDrna = lineas[3].substringAfter(":").trim(),
                certeza = lineas[4].substringAfter(":").trim(),
                esError = false
            )
        } else {
            ResultadoIdentificacion(
                nombreComun = "Especie Reconocida",
                nombreLocalPr = "",
                regulacionDrna = textoGenerado.ifEmpty { "Verificar medidas en el compendio general de ley." },
                certeza = "90%",
                esError = false
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ResultadoIdentificacion(
            nombreComun = "Fallo de Conexión REST",
            nombreLocalPr = "",
            regulacionDrna = "Error de Servidor: ${e.message}\nVerifica tu señal.",
            certeza = "0%",
            esError = true
        )
    }
}

@Composable
fun PantallaRecordsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mis Récords Personales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir récord", tint = MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalDivider()

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Chillo Ojo de Buey", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Lugar: Guayama, PR", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text("18.5 lbs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- 5. LÓGICA DEL MAPA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaPescapr() {
    val context = LocalContext.current
    val db = remember { Firebase.firestore }
    val storage = remember { Firebase.storage }
    val storageRef = remember { storage.reference }
    val coroutineScope = rememberCoroutineScope()

    val misPuntosDePesca = remember { mutableStateListOf<PuntoPesca>() }
    val weatherApiKey = BuildConfig.OPENWEATHER_API_KEY

    var datosClima by remember { mutableStateOf<WeatherResponse?>(null) }
    val weatherService = remember {
        Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherService::class.java)
    }

    var tipoDeMapaActual by remember { mutableStateOf(MapType.SATELLITE) }
    var mostrarSheetInfo by remember { mutableStateOf(false) }
    var puntoSeleccionado by remember { mutableStateOf<PuntoPesca?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val estadoMareaSimulado by remember { mutableStateOf(0.7f) }

    var subiendoFoto by remember { mutableStateOf(false) }
    var mostrarOpcionesFoto by remember { mutableStateOf(false) }

    var urlImagenParaEditar by remember { mutableStateOf("") }
    var indiceImagenParaEditar by remember { mutableStateOf(-1) }

    val procesarYSubirFoto: (Bitmap) -> Unit = { bitmap ->
        if (puntoSeleccionado != null) {
            subiendoFoto = true
            coroutineScope.launch {
                try {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                    val datosImagen = baos.toByteArray()

                    val fotoId = UUID.randomUUID().toString()
                    val referenciaFoto = storageRef.child("spots/${puntoSeleccionado!!.id}/$fotoId.jpg")

                    referenciaFoto.putBytes(datosImagen)
                        .addOnSuccessListener {
                            referenciaFoto.downloadUrl.addOnSuccessListener { uri ->
                                val urlDescargada = uri.toString()

                                val fotosActualizadas = puntoSeleccionado!!.fotosUrls + urlDescargada
                                db.collection("spots").document(puntoSeleccionado!!.id)
                                    .update("fotosUrls", fotosActualizadas)
                                    .addOnSuccessListener {
                                        subiendoFoto = false
                                        Toast.makeText(context, "¡Captura añadida exitosamente!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        subiendoFoto = false
                                        Toast.makeText(context, "Error en Base de Datos: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            subiendoFoto = false
                            Toast.makeText(context, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } catch (e: Exception) {
                    subiendoFoto = false
                    Toast.makeText(context, "Falla en procesamiento: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val mapaCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            procesarYSubirFoto(bitmap)
        }
    }

    val mapaGaleriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    procesarYSubirFoto(bitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando imagen de galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(puntoSeleccionado) {
        puntoSeleccionado?.let { spot ->
            try { datosClima = weatherService.getWeather(spot.coordenada.latitude, spot.coordenada.longitude, weatherApiKey) }
            catch (e: Exception) { datosClima = null }
        }
    }

    DisposableEffect(Unit) {
        val listener = db.collection("spots").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            val puntos = snapshot?.documents?.mapNotNull { doc ->
                val urls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                PuntoPesca(doc.id, LatLng(doc.getDouble("latitud") ?: 0.0, doc.getDouble("longitud") ?: 0.0), doc.getString("nombre") ?: "", doc.getString("descripcion") ?: "", urls)
            } ?: emptyList()
            misPuntosDePesca.clear()
            misPuntosDePesca.addAll(puntos)
            puntoSeleccionado?.let { actual -> puntoSeleccionado = puntos.find { it.id == actual.id } }
        }

        onDispose {
            listener.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(18.2208, -66.5901), 9f) },
            properties = MapProperties(isMyLocationEnabled = true, mapType = tipoDeMapaActual)
        ) {
            misPuntosDePesca.forEach { spot ->
                Marker(
                    state = MarkerState(position = spot.coordenada),
                    onClick = { puntoSeleccionado = spot; mostrarSheetInfo = true; true },
                    icon = remember(context) {
                        try {
                            val resId = context.resources.getIdentifier("pin_pescapr", "drawable", context.packageName)
                            if (resId != 0) {
                                val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                                if (bitmap != null) {
                                    val scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
                                    BitmapDescriptorFactory.fromBitmap(scaled)
                                } else null
                            } else null
                        } catch(e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                )
            }
        }

        if (mostrarSheetInfo) {
            ModalBottomSheet(onDismissRequest = { mostrarSheetInfo = false }, sheetState = sheetState) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().fillMaxHeight(0.85f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = puntoSeleccionado?.nombre ?: "", style = MaterialTheme.typography.headlineSmall)
                    Text(text = puntoSeleccionado?.descripcion ?: "", color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    datosClima?.let { clima ->
                        val pressureInHg = clima.main.pressure * 0.02953
                        val pressureFormatted = String.format(Locale.US, "%.2f", pressureInHg)
                        val colorPresion = if (pressureInHg in 29.70..30.40) Color(0xFF4CAF50) else Color(0xFFF44336)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                                WeatherInfoItem(Icons.Default.Thermostat, "${clima.main.temp.toInt()}°F", "Temperatura")
                                WeatherInfoItem(Icons.Default.Air, "${clima.wind.speed.toInt()} mph", "Viento")
                                WeatherInfoItem(Icons.Default.Speed, "$pressureFormatted inHg", "Presión barométrica", tintOverride = colorPresion)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                ManometroMareasVerticalRojoVerde(valor = estadoMareaSimulado)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "MAREA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(text = if(estadoMareaSimulado > 0.5) "Alta / Subiendo" else "Baja / Bajando", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        if (subiendoFoto) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subiendo...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        } else {
                            ActionBtn(Icons.Default.AddAPhoto, "Añadir") {
                                mostrarOpcionesFoto = true
                            }
                        }
                        ActionBtn(Icons.Default.Edit, "Editar", Color(0xFFFFA000)) { }
                        ActionBtn(Icons.Default.Delete, "Borrar", Color.Red) { }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val fotos = puntoSeleccionado?.fotosUrls ?: emptyList()

                        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(300.dp), userScrollEnabled = false) {
                            itemsIndexed(fotos) { index, url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            urlImagenParaEditar = url
                                            indiceImagenParaEditar = index
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mostrarOpcionesFoto) {
            AlertDialog(
                onDismissRequest = { mostrarOpcionesFoto = false },
                title = { Text(text = "Añadir Captura") },
                text = { Text(text = "¿Desde dónde deseas añadir la foto?") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarOpcionesFoto = false
                        mapaCamaraLauncher.launch(null)
                    }) {
                        Text("Cámara")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mostrarOpcionesFoto = false
                        mapaGaleriaLauncher.launch("image/*")
                    }) {
                        Text("Galería")
                    }
                }
            )
        }

        if (urlImagenParaEditar.isNotBlank()) {
            Dialog(
                onDismissRequest = { urlImagenParaEditar = "" },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    var bitmapEditando by remember { mutableStateOf<Bitmap?>(null) }
                    var cargandoBitmap by remember { mutableStateOf(true) }
                    var guardandoEdicion by remember { mutableStateOf(false) }

                    LaunchedEffect(urlImagenParaEditar) {
                        cargandoBitmap = true
                        withContext(Dispatchers.IO) {
                            try {
                                val loader = ImageLoader(context)
                                val request = ImageRequest.Builder(context)
                                    .data(urlImagenParaEditar)
                                    .allowHardware(false)
                                    .build()
                                val result = loader.execute(request)
                                if (result is SuccessResult) {
                                    bitmapEditando = (result.drawable as? BitmapDrawable)?.bitmap
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                cargandoBitmap = false
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (cargandoBitmap) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
                        } else {
                            bitmapEditando?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Editando captura",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } ?: Text("Error cargando imagen", color = Color.White, modifier = Modifier.align(Alignment.Center))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { urlImagenParaEditar = "" }, enabled = !guardandoEdicion) {
                                Text("Cancelar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text("Visor & Editor", color = Color.White, fontWeight = FontWeight.Bold)

                            Button(
                                onClick = {
                                    bitmapEditando?.let { bmp ->
                                        guardandoEdicion = true
                                        coroutineScope.launch {
                                            try {
                                                val baos = ByteArrayOutputStream()
                                                bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                                                val datosImagen = baos.toByteArray()

                                                val fotoId = UUID.randomUUID().toString()
                                                val referenciaFoto = storageRef.child("spots/${puntoSeleccionado!!.id}/$fotoId.jpg")

                                                referenciaFoto.putBytes(datosImagen)
                                                    .addOnSuccessListener {
                                                        referenciaFoto.downloadUrl.addOnSuccessListener { uri ->
                                                            val urlDescargada = uri.toString()

                                                            val fotosActualizadas = puntoSeleccionado!!.fotosUrls.toMutableList()
                                                            if (indiceImagenParaEditar in fotosActualizadas.indices) {
                                                                fotosActualizadas[indiceImagenParaEditar] = urlDescargada
                                                            } else {
                                                                fotosActualizadas.add(urlDescargada)
                                                            }

                                                            db.collection("spots").document(puntoSeleccionado!!.id)
                                                                .update("fotosUrls", fotosActualizadas)
                                                                .addOnSuccessListener {
                                                                    guardandoEdicion = false
                                                                    urlImagenParaEditar = ""
                                                                    Toast.makeText(context, "¡Cambios guardados con éxito!", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .addOnFailureListener {
                                                                    guardandoEdicion = false
                                                                    Toast.makeText(context, "Error al actualizar base de datos", Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        guardandoEdicion = false
                                                        Toast.makeText(context, "Error de red en Storage: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                            } catch (e: Exception) {
                                                guardandoEdicion = false
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = !guardandoEdicion && bitmapEditando != null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (guardandoEdicion) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Guardar")
                                }
                            }
                        }

                        if (!cargandoBitmap && !guardandoEdicion && bitmapEditando != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .navigationBarsPadding()
                                    .padding(top = 24.dp, bottom = 36.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val matrix = Matrix().apply { postRotate(90f) }
                                        bitmapEditando = Bitmap.createBitmap(
                                            bitmapEditando!!, 0, 0,
                                            bitmapEditando!!.width, bitmapEditando!!.height,
                                            matrix, true
                                        )
                                    },
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rotar 90°")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val source = bitmapEditando!!
                                        val ladoMinimo = minOf(source.width, source.height)
                                        val xOffset = (source.width - ladoMinimo) / 2
                                        val yOffset = (source.height - ladoMinimo) / 2
                                        bitmapEditando = Bitmap.createBitmap(source, xOffset, yOffset, ladoMinimo, ladoMinimo)
                                    },
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cortar Cuadrado (1:1)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 6. DETALLES VISUALES ADICIONALES ---
@Composable
fun ManometroMareasVerticalRojoVerde(valor: Float) {
    val valorAnimado by animateFloatAsState(targetValue = valor, animationSpec = tween(durationMillis = 1000))

    Box(modifier = Modifier.size(80.dp, 120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 15f
            val canvasWidth = size.width
            val canvasHeight = size.height
            val rectSize = Size(canvasWidth * 2f, canvasHeight)
            val topLeftOffset = Offset(-canvasWidth, 0f)

            drawArc(color = Color(0xFFF44336), startAngle = 90f, sweepAngle = 60f, useCenter = false, topLeft = topLeftOffset, size = rectSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            drawArc(color = Color(0xFFFFC107), startAngle = 150f, sweepAngle = 60f, useCenter = false, topLeft = topLeftOffset, size = rectSize, style = Stroke(strokeWidth))
            drawArc(color = Color(0xFF4CAF50), startAngle = 210f, sweepAngle = 60f, useCenter = false, topLeft = topLeftOffset, size = rectSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            val centerPoint = Offset(canvasWidth, canvasHeight / 2f)
            val angle = 90f + (valorAnimado * 180f)
            val angleRad = Math.toRadians(angle.toDouble())
            val lineLength = canvasWidth * 0.8f
            val endX = centerPoint.x + lineLength * cos(angleRad).toFloat()
            val endY = centerPoint.y + lineLength * sin(angleRad).toFloat()

            drawLine(Color(0xFF333333), centerPoint, Offset(endX, endY), 7f, StrokeCap.Round)
            drawCircle(Color(0xFF333333), 8f, centerPoint)
        }
    }
}

@Composable
fun WeatherInfoItem(icon: ImageVector, value: String, label: String, tintOverride: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = tintOverride ?: MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tintOverride ?: Color.Unspecified)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ActionBtn(icon: ImageVector, label: String, color: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, null, tint = color) }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
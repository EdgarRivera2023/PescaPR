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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.scale
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.maps.android.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bradmir.pescapr.data.*
import com.bradmir.pescapr.network.MarineWeatherService
import com.bradmir.pescapr.ui.components.ProSwellCard
import com.bradmir.pescapr.ui.components.PaywallDialog
import com.bradmir.pescapr.ui.viewmodels.MapViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// --- 1. MODELOS DE DATOS ---

data class FichaPez(
    val id: String = "",
    val nombreCientifico: String = "",
    val nombreComun: String = "", // Común y locales
    val nombreIngles: String = "",
    val regulacionComercial: String = "",
    val regulacionRecreativa: String = "",
    val caracteristicas: List<String> = emptyList(),
    val puedeSerConfundidoCon: String = "",
    val fotosUrls: List<String> = emptyList()
)

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

data class RecordPesca(
    val id: String = "0", // Local Room ID as String
    val nombrePez: String = "",
    val peso: String = "",
    val longitud: String = "",
    val lugar: String = "",
    val fecha: String = "",
    val fotosUrls: List<String> = emptyList(),
    val spotId: String = "0", // Local Spot ID as String
    val fishId: String? = null,
    val climaTemp: String = "",
    val climaWind: String = "",
    val climaPressure: String = "",
    val climaTide: String = ""
)

// --- NOAA TIDES MODELS ---
data class TidePrediction(
    val t: String, // Time
    val v: String, // Value
    val type: String // H or L
)
data class NoaaTideResponse(val predictions: List<TidePrediction>?)

// --- SUNRISE SUNSET MODELS ---
data class SunriseSunsetResponse(val days: List<DayData>?, val status: String)
data class DayData(val date: String, val sunrise: String, val sunset: String)

data class GoldenPeak(
    val date: String,
    val time: String,
    val type: String // "Amanecer" or "Atardecer"
)

data class TideStation(val id: String, val lat: Double, val lon: Double, val name: String)

val NOAA_STATIONS_PR = listOf(
    TideStation("9755371", 18.459, -66.116, "San Juan"),
    TideStation("9753216", 18.335, -65.631, "Fajardo"),
    TideStation("9759110", 17.971, -67.045, "Magueyes Island"),
    TideStation("9752235", 18.301, -65.302, "Culebra"),
    TideStation("9752695", 18.093, -65.470, "Vieques"),
    TideStation("9754980", 17.970, -66.617, "Ponce"),
    TideStation("9759394", 18.220, -67.158, "Mayaguez"),
    TideStation("9757811", 18.480, -66.701, "Arecibo")
)

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

interface NoaaTideService {
    @GET("api/prod/datagetter")
    suspend fun getTidePredictions(
        @Query("date") date: String? = null,
        @Query("begin_date") beginDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("station") station: String,
        @Query("product") product: String = "predictions",
        @Query("datum") datum: String = "mllw",
        @Query("units") units: String = "english",
        @Query("time_zone") timeZone: String = "lst_ldt",
        @Query("format") format: String = "json",
        @Query("interval") interval: String = "hilo"
    ): NoaaTideResponse
}

interface SunriseSunsetService {
    @GET("v2")
    suspend fun getRange(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("date_start") start: String,
        @Query("date_end") end: String
    ): SunriseSunsetResponse
}

// --- 2. ACTIVIDAD PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MainTabsScreen(database)
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, folder: String): String? {
    return try {
        val filename = "${UUID.randomUUID()}.jpg"
        val directory = File(context.filesDir, folder)
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(database: AppDatabase) {
    val tabs = listOf("Mapa", "Identificar", "Guía Official", "Diario Privado")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // --- GATED ACCESS (PRO TIER - SINGLE SOURCE OF TRUTH) ---
    val subscriptionManager = remember { SubscriptionManager(context) }
    val isUserPro by subscriptionManager.isProUser.collectAsState()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            subscriptionManager.checkSubscriptionStatus(userId)
        }
    }

    // Repositories
    val firestore = remember { FirebaseFirestore.getInstance("pescapr") }
    val catchRepository = remember { 
        CatchRepository(database.recordDao(), firestore) 
    }
    val spotRepository = remember {
        SpotRepository(firestore)
    }
    
    // Foco para navegación desde Récords -> Mapa
    var spotIdAFocar by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoAcercaDe by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                Image(painter = painterResource(id = R.drawable.logo_small), contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "PescaPR", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { mostrarDialogoAcercaDe = true }) {
                Icon(Icons.Default.Info, contentDescription = "Acerca de", tint = MaterialTheme.colorScheme.primary)
            }
        }

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f), userScrollEnabled = false) { page ->
            when (page) {
                0 -> PantallaMapaTab(database = database, repository = catchRepository, spotRepository = spotRepository, subscriptionManager = subscriptionManager, userId = userId, isPro = isUserPro, spotIdAFocar = spotIdAFocar, onFocoLogrado = { spotIdAFocar = null })
                1 -> PantallaIdentificadorYRegulacionesTab()
                2 -> PantallaGuiaOficialTab()
                3 -> PantallaRecordsTab(database = database, repository = catchRepository, onIrALugar = { id -> 
                    spotIdAFocar = id
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                })
            }
        }
    }

    if (mostrarDialogoAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAcercaDe = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(id = R.drawable.logo_small), contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Acerca de PescaPR")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Versión ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Tu compañero de pesca en Puerto Rico. Identificación de especies con IA, mapa de spots y registro de capturas.")
                        Text("Desarrollado por: Bradmir Consulting / Edgar Rivera", style = MaterialTheme.typography.labelSmall)
                        Text("Potenciado por Google Gemini AI.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    HorizontalDivider()
                    
                    Text("Notas de Versión", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    // v2.1.5
                    VersionNote(
                        version = "v2.1.5 - 7/26/2026",
                        changes = listOf(
                            "Publicación Estática de Spots: Guardado en Firestore con atribución userId explícita y cero rastreo en tiempo real.",
                            "Terminología UI: Actualización global de 'Swell' a 'Marejada'."
                        )
                    )

                    // v2.1.4
                    VersionNote(
                        version = "v2.1.4 - 7/26/2026",
                        changes = listOf(
                            "Pantalla Paywall Pro: Interfaz de usuario para suscripciones y beneficios de PescaPR Pro."
                        )
                    )

                    // v2.1.3
                    VersionNote(
                        version = "v2.1.3 - 7/26/2026",
                        changes = listOf(
                            "SubscriptionManager: Control de estado Pro en tiempo real y sincronización de derechos con Firestore.",
                            "Manejo de Compras: Soporte para verificación asíncrona de suscripciones y flujo de facturación."
                        )
                    )

                    // v2.1.2
                    VersionNote(
                        version = "v2.1.2 - 7/26/2026",
                        changes = listOf(
                            "Google Play Billing v7: Integración de dependencias ktx de facturación oficial de Google Play."
                        )
                    )

                    // v2.1.1
                    VersionNote(
                        version = "v2.1.1 - 7/25/2026",
                        changes = listOf(
                            "Red de Pines de la Comunidad: Filtrado por rol según estado de suscripción Pro.",
                            "Consultas Optimizadas: Lectura bajo demanda con botón de refresco manual para ahorrar lecturas de Firestore."
                        )
                    )

                    // v2.1.0
                    VersionNote(
                        version = "v2.1.0 - 7/25/2026",
                        changes = listOf(
                            "Modelo PuntoPesca: Asociación de spots al ID de usuario en Firestore y Room.",
                            "Migración Room: Actualización no destructiva de base de datos de v1 a v2."
                        )
                    )

                    // v2.0.0
                    VersionNote(
                        version = "v2.0.0 - 7/24/2026",
                        changes = listOf(
                            "Diario Privado Pro: Sincronización automática en la nube.",
                            "AI Pattern Matcher: Análisis inteligente de tus capturas para encontrar patrones de éxito.",
                            "Métricas Pro Marejada (v1.9): Optimizadas para costa."
                        )
                    )

                    // v1.9
                    VersionNote(
                        version = "v1.9.0 - 7/24/2026",
                        changes = listOf(
                            "Pro Feature: Métricas de Marejada y Oleaje en tiempo real.",
                            "Optimización: Carga paralela de datos meteorológicos y marinos.",
                            "Arquitectura: Refactorización estructural para módulos Pro."
                        )
                    )

                    // v1.8
                    VersionNote(
                        version = "v1.8 - 7/23/2026 ",
                        changes = listOf(
                            "Nueva Función: Capacidad de búsqueda en Guia Oficial.",
                            "Manómetro de mareas se actualiza al pulsarlo."
                        )
                    )
                    // v1.7
                    VersionNote(
                        version = "v1.7 - 7/22/2026",
                        changes = listOf(
                            "Mejoras de Guia Oficial: Ordenado por nombre científico.",
                            "Identificación de Picos de Oro: Mejor dia de pesca en los próximos 30 días."
                        )
                    )
                    // v1.6
                    VersionNote(
                        version = "v1.6 - 7/21/2026",
                        changes = listOf(
                            "Mejoras de Mapa: Vistas de alta resolución al hacer zoom, corregida.",
                            "Mejoras de Signos Vitales del Spot: Manómetro de mareas más preciso."
                        )
                    )
                    // v1.5
                    VersionNote(
                        version = "v1.5 - 7/17/2026",
                        changes = listOf(
                            "Mejoras de Guía Oficial: Tamaños de imágenes mejoradas para una mejor vista.",
                            "Pines de Comunidad: Sección de pines privados y pines púbicos en el mapa."
                        )
                    )
                    // v1.4
                    VersionNote(
                        version = "v1.4 - 7/16/2026",
                        changes = listOf(
                            "Mejoras de Estabilidad: Protecciones añadidas para evitar cierres inesperados al guardar datos.",
                            "Recuperación Automática: El app ahora puede recuperarse de errores en la base de datos sin quedar bloqueada.",
                            "Seguridad de Datos: Manejo mejorado de información interna para mayor fluidez."
                        )
                    )

                    // v1.3
                    VersionNote(
                        version = "v1.3 - 7/15/2026",
                        changes = listOf(
                            "Privacidad Total: Spots y récords ahora se guardan localmente (Room).",
                            "Bitácora Personal: Cada usuario tiene sus propios datos privados.",
                            "Fotos Locales: Las fotos se guardan en el dispositivo (sin internet).",
                            "Persistencia: Datos protegidos durante actualizaciones del app."
                        )
                    )

                    // v1.2
                    VersionNote(
                        version = "v1.2 - 7/4/2026",
                        changes = listOf(
                            "Integración avanzada de capturas con el Mapa.",
                            "Registro de clima automático (Temp, Viento, Presión, Marea).",
                            "Gestión de fotos del spot (límite de 4 fotos).",
                            "Navegación directa de Récords a ubicación en Mapa."
                        )
                    )

                    // v1.1
                    VersionNote(
                        version = "v1.1 - 6/26/2026",
                        changes = listOf(
                            "Nueva sección 'Acerca de' con notas de versión.",
                            "Mejoras en la fluidez de la interfaz de usuario.",
                            "Corrección de errores en la base de datos Firestore."
                        )
                    )

                    // v1.0
                    VersionNote(
                        version = "v1.0 - 5/8/2026",
                        changes = listOf(
                            "Identificación de peces con Inteligencia Artificial.",
                            "Mapa de spots de pesca compartidos.",
                            "Guía oficial de regulaciones autogestionada."
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                Button(onClick = { mostrarDialogoAcercaDe = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun VersionNote(version: String, changes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(version, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        changes.forEach { change ->
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text("• ", fontWeight = FontWeight.Bold)
                Text(change, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// --- TAB 1: MAPA ---
@Composable
fun PantallaMapaTab(
    database: AppDatabase,
    repository: CatchRepository,
    spotRepository: SpotRepository,
    subscriptionManager: SubscriptionManager,
    userId: String,
    isPro: Boolean,
    spotIdAFocar: String? = null,
    onFocoLogrado: () -> Unit = {}
) {
    MapaPescapr(
        database = database,
        repository = repository,
        spotRepository = spotRepository,
        subscriptionManager = subscriptionManager,
        userId = userId,
        isPro = isPro,
        spotIdAFocar = spotIdAFocar,
        onFocoLogrado = onFocoLogrado
    )
}

// --- TAB 2: IDENTIFICADOR (Matching with Cards) ---
@Composable
fun PantallaIdentificadorYRegulacionesTab() {
    val context = LocalContext.current
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    val coroutineScope = rememberCoroutineScope()

    var bitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
    var analizando by remember { mutableStateOf(false) }
    var analizadoCompleto by remember { mutableStateOf(false) }
    var datosIdentificacion by remember { mutableStateOf(ResultadoIdentificacion(nombreComun = "Inicie identificación")) }

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
        Text("Validador de Captura", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            if (bitmapSeleccionado == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Toma una foto de tu pez", color = Color.Gray)
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
                Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Cámara")
            }
            OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Collections, null); Spacer(Modifier.width(8.dp)); Text("Galería")
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
            Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("Validar contra Guía Oficial")
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
                Text("RESULTADO", fontWeight = FontWeight.Black, color = colorEstatus)
                Text("Certeza: ${datos.certeza}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = datos.nombreComun, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (datos.nombreIngles.isNotBlank()) {
                Text(text = "English: ${datos.nombreIngles}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            }
            Text(text = datos.nombreCientifico, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            HorizontalDivider(thickness = 0.5.dp, color = colorEstatus.copy(0.3f))
            
            Text("Regulación Comercial:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = datos.regulacionComercial, style = MaterialTheme.typography.bodyMedium)
            
            Text("Regulación Recreativa:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Text(text = datos.regulacionRecreativa, style = MaterialTheme.typography.bodyMedium)

            Text("Características:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
                            Toast.makeText(context, "Reporte enviado al desarrollador", Toast.LENGTH_SHORT).show()
                            reportado = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Error, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reportar información incorrecta")
                }
            } else {
                Text("¡Gracias por tu reporte! Revisaremos la ficha pronto.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// --- TAB 3: GUÍA OFICIAL (User creates these) ---
@Composable
fun PantallaGuiaOficialTab() {
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    val storage = remember { FirebaseStorage.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- SEGURIDAD: MODO DESARROLLADOR ---
    var esDeveloper by remember { mutableStateOf(false) } 

    val fichas = remember { mutableStateListOf<FichaPez>() }
    var mostrarDialogoNueva by remember { mutableStateOf(false) }
    var fichaParaEditar by remember { mutableStateOf<FichaPez?>(null) }
    var subiendo by remember { mutableStateOf(false) }
    
    // Estado para ver detalles (Read-only)
    var fichaSeleccionadaDetalle by remember { mutableStateOf<FichaPez?>(null) }

    // Campos del formulario (estables fuera del bloque del diálogo)
    var nombreCientifico by remember { mutableStateOf("") }
    var nombreComun by remember { mutableStateOf("") }
    var nombreIngles by remember { mutableStateOf("") }
    var regulacionComercial by remember { mutableStateOf("") }
    var regulacionRecreativa by remember { mutableStateOf("") }
    var caracteristicasRaw by remember { mutableStateOf("") }
    var puedeSerConfundidoCon by remember { mutableStateOf("") }
    val bitmapsNuevos = remember { mutableStateListOf<Bitmap>() }
    val urlsExistentes = remember { mutableStateListOf<String>() }

    var searchQuery by remember { mutableStateOf("") }
    val filteredFichas = remember(searchQuery, fichas.size) {
        if (searchQuery.isBlank()) fichas
        else {
            fichas.filter { 
                it.nombreComun.contains(searchQuery, ignoreCase = true) ||
                it.nombreCientifico.contains(searchQuery, ignoreCase = true) ||
                it.nombreIngles.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Sincronizar campos cuando se abre para editar
    LaunchedEffect(mostrarDialogoNueva) {
        if (mostrarDialogoNueva) {
            if (fichaParaEditar != null) {
                nombreCientifico = fichaParaEditar!!.nombreCientifico
                nombreComun = fichaParaEditar!!.nombreComun
                nombreIngles = fichaParaEditar!!.nombreIngles
                regulacionComercial = fichaParaEditar!!.regulacionComercial
                regulacionRecreativa = fichaParaEditar!!.regulacionRecreativa
                caracteristicasRaw = fichaParaEditar!!.caracteristicas.joinToString("\n")
                puedeSerConfundidoCon = fichaParaEditar!!.puedeSerConfundidoCon
                urlsExistentes.clear()
                urlsExistentes.addAll(fichaParaEditar!!.fotosUrls)
            } else {
                nombreCientifico = ""; nombreComun = ""; nombreIngles = ""; regulacionComercial = ""; regulacionRecreativa = ""
                caracteristicasRaw = ""; puedeSerConfundidoCon = ""; urlsExistentes.clear()
            }
            bitmapsNuevos.clear()
        }
    }

    DisposableEffect(Unit) {
        val listener = db.collection("fichas_peces").addSnapshotListener { snap, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }
            val listaFichas = snap?.documents?.mapNotNull { doc ->
                FichaPez(
                    id = doc.id,
                    nombreCientifico = doc.getString("nombreCientifico") ?: "",
                    nombreComun = doc.getString("nombreComun") ?: "",
                    nombreIngles = doc.getString("nombreIngles") ?: "",
                    regulacionComercial = doc.getString("regulacionComercial") ?: "",
                    regulacionRecreativa = doc.getString("regulacionRecreativa") ?: "",
                    caracteristicas = (doc.get("caracteristicas") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    puedeSerConfundidoCon = doc.getString("puedeSerConfundidoCon") ?: "",
                    fotosUrls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                )
            }?.sortedBy { it.nombreCientifico } ?: emptyList()
            
            fichas.clear()
            fichas.addAll(listaFichas)
        }
        onDispose { listener?.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Guía Oficial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            if (BuildConfig.DEBUG) {
                TextButton(onClick = { esDeveloper = !esDeveloper }) {
                    Text(if(esDeveloper) "Admin ON" else "Modo Vista")
                }
            }

            if (esDeveloper) {
                IconButton(onClick = { 
                    fichaParaEditar = null
                    mostrarDialogoNueva = true 
                }) {
                    Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
        }
        
        if (esDeveloper) {
            Text("Estás en modo edición. Los cambios afectan al identificador de todos los usuarios.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        // Barra de Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nombre o especie...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(16.dp))

        if (filteredFichas.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("No se encontraron resultados para \"$searchQuery\"", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredFichas) { ficha ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clickable {
                        if (esDeveloper) {
                            fichaParaEditar = ficha
                            mostrarDialogoNueva = true
                        } else {
                            fichaSeleccionadaDetalle = ficha
                        }
                    }, 
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box {
                        AsyncImage(model = ficha.fotosUrls.firstOrNull(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                            Text(ficha.nombreComun, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(ficha.nombreCientifico, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                            Text("${ficha.fotosUrls.size} fotos", color = Color.Yellow, style = MaterialTheme.typography.labelSmall)
                        }
                        if (esDeveloper) {
                            IconButton(onClick = { db.collection("fichas_peces").document(ficha.id).delete() }, modifier = Modifier.align(Alignment.TopEnd)) {
                                Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (esDeveloper) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Text("Reportes de Error de Usuarios", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            
            // Sección para ver reportes (simplificada)
            var reportesCount by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) {
                db.collection("reportes_error").addSnapshotListener { s, _ -> reportesCount = s?.size() ?: 0 }
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.2f))) {
                Text("Tienes $reportesCount reportes pendientes por revisar.", modifier = Modifier.padding(16.dp))
            }
        }
    }

    if (mostrarDialogoNueva) {
        var urlImagenParaEditar by remember { mutableStateOf("") }
        var bitmapParaEditar by remember { mutableStateOf<Bitmap?>(null) }
        var indiceImagenParaEditar by remember { mutableStateOf(-1) }
        var esNuevaImagenParaEditar by remember { mutableStateOf(false) }
        
        val multiPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.forEach { uri ->
                val stream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream)?.let { bitmapsNuevos.add(it) }
            }
        }

        AlertDialog(
            onDismissRequest = { if(!subiendo) mostrarDialogoNueva = false },
            title = { Text(if (fichaParaEditar == null) "Nueva Ficha de Referencia" else "Editar Ficha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Fotos:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { multiPickerLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddAPhoto, null)
                        }
                        // Mostrar URLs existentes
                        urlsExistentes.forEachIndexed { index, url ->
                            Box(modifier = Modifier.size(80.dp).clickable { 
                                urlImagenParaEditar = url
                                bitmapParaEditar = null
                                indiceImagenParaEditar = index
                                esNuevaImagenParaEditar = false
                            }) {
                                AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                if (index == 0) {
                                    Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).background(Color.Black.copy(0.5f), CircleShape))
                                }
                            }
                        }
                        // Mostrar bitmaps nuevos
                        bitmapsNuevos.forEachIndexed { index, bmp ->
                            Box(modifier = Modifier.size(80.dp).clickable { 
                                bitmapParaEditar = bmp
                                urlImagenParaEditar = ""
                                indiceImagenParaEditar = index
                                esNuevaImagenParaEditar = true
                            }) {
                                Image(bitmap = bmp.asImageBitmap(), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                IconButton(onClick = { bitmapsNuevos.remove(bmp) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.5f), CircleShape)) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                    OutlinedTextField(value = nombreCientifico, onValueChange = { nombreCientifico = it }, label = { Text("Nombre Científico") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nombreComun, onValueChange = { nombreComun = it }, label = { Text("Nombre Común / Locales") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nombreIngles, onValueChange = { nombreIngles = it }, label = { Text("English Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = regulacionComercial, onValueChange = { regulacionComercial = it }, label = { Text("Regulación Comercial") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = regulacionRecreativa, onValueChange = { regulacionRecreativa = it }, label = { Text("Regulación Recreativa") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = caracteristicasRaw, onValueChange = { caracteristicasRaw = it }, label = { Text("Características (una por línea)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = puedeSerConfundidoCon, onValueChange = { puedeSerConfundidoCon = it }, label = { Text("Puede ser confundido con") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    subiendo = true
                    coroutineScope.launch {
                        try {
                            // 1. Subir fotos nuevas en paralelo
                            val nuevasUrls = mutableListOf<String>()
                            if (bitmapsNuevos.isNotEmpty()) {
                                withTimeout(40000) {
                                    val deferreds = bitmapsNuevos.map { bmp ->
                                        async(Dispatchers.IO) {
                                            val ref = storage.reference.child("fichas/${UUID.randomUUID()}.jpg")
                                            val baos = ByteArrayOutputStream()
                                            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                                            ref.putBytes(baos.toByteArray()).await()
                                            ref.downloadUrl.await().toString()
                                        }
                                    }
                                    nuevasUrls.addAll(deferreds.awaitAll())
                                }
                            }
                            
                            val totalUrls = urlsExistentes + nuevasUrls
                            val data = hashMapOf(
                                "nombreCientifico" to nombreCientifico,
                                "nombreComun" to nombreComun,
                                "nombreIngles" to nombreIngles,
                                "regulacionComercial" to regulacionComercial,
                                "regulacionRecreativa" to regulacionRecreativa,
                                "caracteristicas" to caracteristicasRaw.lines().filter { it.isNotBlank() },
                                "puedeSerConfundidoCon" to puedeSerConfundidoCon,
                                "fotosUrls" to totalUrls
                            )

                            // 2. Guardar en Firestore con un timeout generoso
                            withTimeout(25000) {
                                val docRef = if (fichaParaEditar == null) {
                                    db.collection("fichas_peces").document()
                                } else {
                                    db.collection("fichas_peces").document(fichaParaEditar!!.id)
                                }
                                docRef.set(data).await()
                            }
                            
                            // 3. Éxito: Cerrar diálogo
                            withContext(Dispatchers.Main) {
                                mostrarDialogoNueva = false
                                fichaParaEditar = null
                                Toast.makeText(context, "¡Guardado con éxito!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val msg = if (e is TimeoutCancellationException) "Error: Tiempo de espera agotado (Red lenta)" 
                                     else "Error: ${e.localizedMessage}"
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                // Si falló pero el usuario dice que se creó, quizás cerrar el diálogo igual?
                                // Por ahora lo dejamos abierto para que reintente si gusta.
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                subiendo = false
                            }
                        }
                    }
                }, enabled = !subiendo && nombreCientifico.isNotBlank()) {
                    if (subiendo) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(if (fichaParaEditar == null) "Crear Ficha" else "Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNueva = false }, enabled = !subiendo) {
                    Text("Cancelar")
                }
            }
        )

        // --- SUB-DIÁLOGO: EDITOR DE IMAGEN ---
        if (urlImagenParaEditar.isNotBlank() || bitmapParaEditar != null) {
            Dialog(onDismissRequest = { urlImagenParaEditar = ""; bitmapParaEditar = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    var bitmapEditando by remember { mutableStateOf<Bitmap?>(null) }
                    var cargandoBitmap by remember { mutableStateOf(true) }
                    var modoRecorte by remember { mutableStateOf(false) }
                    var rectRecorte by remember { mutableStateOf(Rect(0.1f, 0.1f, 0.9f, 0.9f)) }

                    LaunchedEffect(urlImagenParaEditar, bitmapParaEditar) {
                        cargandoBitmap = true
                        if (bitmapParaEditar != null) {
                            bitmapEditando = bitmapParaEditar
                        } else {
                            withContext(Dispatchers.IO) {
                                try {
                                    val loader = ImageLoader(context)
                                    val request = ImageRequest.Builder(context).data(urlImagenParaEditar).allowHardware(false).build()
                                    val result = loader.execute(request)
                                    if (result is SuccessResult) {
                                        bitmapEditando = (result.drawable as? BitmapDrawable)?.bitmap
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                        cargandoBitmap = false
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (cargandoBitmap) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                        } else {
                            bitmapEditando?.let { bmp ->
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Image(bitmap = bmp.asImageBitmap(), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                    
                                    if (modoRecorte) {
                                        var dragMode by remember { mutableStateOf(0) } // 0: None, 1: Move, 2: ResizeBR, 3: ResizeTL
                                        
                                        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    val touchX = offset.x / size.width
                                                    val touchY = offset.y / size.height
                                                    
                                                    dragMode = when {
                                                        (touchX - rectRecorte.right).let { it * it } + (touchY - rectRecorte.bottom).let { it * it } < 0.01 -> 2 // BottomRight
                                                        (touchX - rectRecorte.left).let { it * it } + (touchY - rectRecorte.top).let { it * it } < 0.01 -> 3 // TopLeft
                                                        rectRecorte.contains(Offset(touchX, touchY)) -> 1 // Move
                                                        else -> 0
                                                    }
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val deltaX = dragAmount.x / size.width
                                                    val deltaY = dragAmount.y / size.height

                                                    rectRecorte = when (dragMode) {
                                                        1 -> { // Mover
                                                            Rect(
                                                                (rectRecorte.left + deltaX).coerceIn(0f, 1f - rectRecorte.width),
                                                                (rectRecorte.top + deltaY).coerceIn(0f, 1f - rectRecorte.height),
                                                                (rectRecorte.right + deltaX).coerceIn(rectRecorte.width, 1f),
                                                                (rectRecorte.bottom + deltaY).coerceIn(rectRecorte.height, 1f)
                                                            )
                                                        }
                                                        2 -> { // Redimensionar Bottom-Right
                                                            Rect(
                                                                rectRecorte.left,
                                                                rectRecorte.top,
                                                                (rectRecorte.right + deltaX).coerceIn(rectRecorte.left + 0.05f, 1f),
                                                                (rectRecorte.bottom + deltaY).coerceIn(rectRecorte.top + 0.05f, 1f)
                                                            )
                                                        }
                                                        3 -> { // Redimensionar Top-Left
                                                            Rect(
                                                                (rectRecorte.left + deltaX).coerceIn(0f, rectRecorte.right - 0.05f),
                                                                (rectRecorte.top + deltaY).coerceIn(0f, rectRecorte.bottom - 0.05f),
                                                                rectRecorte.right,
                                                                rectRecorte.bottom
                                                            )
                                                        }
                                                        else -> rectRecorte
                                                    }
                                                }
                                            )
                                        }) {
                                            // Sombra exterior (dibujada en 4 bloques para evitar Clear/transparencia)
                                            val rectPx = Rect(rectRecorte.left * size.width, rectRecorte.top * size.height, rectRecorte.right * size.width, rectRecorte.bottom * size.height)
                                            val shadowColor = Color.Black.copy(alpha = 0.5f)

                                            // Arriba
                                            drawRect(shadowColor, topLeft = Offset(0f, 0f), size = Size(size.width, rectPx.top))
                                            // Abajo
                                            drawRect(shadowColor, topLeft = Offset(0f, rectPx.bottom), size = Size(size.width, size.height - rectPx.bottom))
                                            // Izquierda
                                            drawRect(shadowColor, topLeft = Offset(0f, rectPx.top), size = Size(rectPx.left, rectPx.height))
                                            // Derecha
                                            drawRect(shadowColor, topLeft = Offset(rectPx.right, rectPx.top), size = Size(size.width - rectPx.right, rectPx.height))
                                            
                                            // Borde del cuadro
                                            drawRect(Color.White, topLeft = rectPx.topLeft, size = rectPx.size, style = Stroke(2.dp.toPx()))
                                            
                                            // Esquinas de agarre
                                            drawCircle(Color.White, radius = 10.dp.toPx(), center = rectPx.bottomRight)
                                            drawCircle(Color.White, radius = 10.dp.toPx(), center = rectPx.topLeft)
                                        }
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { 
                                    val matrix = Matrix().apply { postRotate(90f) }
                                    bitmapEditando?.let { bitmapEditando = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true) }
                                }) { Icon(Icons.Default.RotateRight, null); Text("Rotar") }
                                
                                Button(onClick = { 
                                    if (modoRecorte) {
                                        // APLICAR RECORTE REAL
                                        bitmapEditando?.let { bmp ->
                                            val left = (rectRecorte.left * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                                            val top = (rectRecorte.top * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                                            val width = (rectRecorte.width * bmp.width).toInt().coerceIn(1, bmp.width - left)
                                            val height = (rectRecorte.height * bmp.height).toInt().coerceIn(1, bmp.height - top)
                                            
                                            val cropped = Bitmap.createBitmap(bmp, left, top, width, height)
                                            if (esNuevaImagenParaEditar) {
                                                bitmapsNuevos[indiceImagenParaEditar] = cropped
                                            } else {
                                                // Si era una URL existente, ahora la tratamos como nueva local para que se suba el cambio
                                                urlsExistentes.removeAt(indiceImagenParaEditar)
                                                bitmapsNuevos.add(cropped)
                                            }
                                            urlImagenParaEditar = ""; bitmapParaEditar = null
                                            Toast.makeText(context, "Imagen recortada", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    modoRecorte = !modoRecorte 
                                }, colors = ButtonDefaults.buttonColors(containerColor = if(modoRecorte) Color.Yellow else MaterialTheme.colorScheme.primary, contentColor = if(modoRecorte) Color.Black else Color.White)) { 
                                    Icon(if(modoRecorte) Icons.Default.Check else Icons.Default.Crop, null)
                                    Text(if(modoRecorte) "Confirmar Zoom" else "Zoom/Crop") 
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(onClick = { 
                                    if (esNuevaImagenParaEditar) {
                                        // No se puede reordenar directamente con URLs, hay que mover el bitmap al inicio
                                        val current = bitmapsNuevos[indiceImagenParaEditar]
                                        bitmapsNuevos.removeAt(indiceImagenParaEditar)
                                        bitmapsNuevos.add(0, current)
                                    } else {
                                        val current = urlsExistentes[indiceImagenParaEditar]
                                        urlsExistentes.removeAt(indiceImagenParaEditar)
                                        urlsExistentes.add(0, current)
                                    }
                                    urlImagenParaEditar = ""; bitmapParaEditar = null
                                    Toast.makeText(context, "Portada seleccionada", Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Default.Star, null); Text("Thumbnail") }

                                OutlinedButton(onClick = { urlImagenParaEditar = ""; bitmapParaEditar = null }, border = BorderStroke(1.dp, Color.White)) { Text("Cerrar", color = Color.White) }

                                IconButton(onClick = { 
                                    if (esNuevaImagenParaEditar) bitmapsNuevos.removeAt(indiceImagenParaEditar)
                                    else urlsExistentes.removeAt(indiceImagenParaEditar)
                                    urlImagenParaEditar = ""; bitmapParaEditar = null
                                }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (fichaSeleccionadaDetalle != null) {
        val ficha = fichaSeleccionadaDetalle!!
        Dialog(onDismissRequest = { fichaSeleccionadaDetalle = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // Header con botón cerrar
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        if (ficha.fotosUrls.isNotEmpty()) {
                            PhotoCarousel(urls = ficha.fotosUrls, modifier = Modifier.fillMaxSize())
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            }
                        }
                        
                        IconButton(onClick = { fichaSeleccionadaDetalle = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(0.5f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(ficha.nombreComun, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            if (ficha.nombreIngles.isNotBlank()) {
                                Text(ficha.nombreIngles, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(ficha.nombreCientifico, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }

                        HorizontalDivider()

                        // Regulaciones
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Recreativa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    Text(ficha.regulacionRecreativa, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Comercial", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    Text(ficha.regulacionComercial, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        // Características
                        if (ficha.caracteristicas.isNotEmpty()) {
                            Text("Características Clave", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            ficha.caracteristicas.forEach { char ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(start = 8.dp)) {
                                    Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(char, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Confusión
                        if (ficha.puedeSerConfundidoCon.isNotBlank()) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Se puede confundir con:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                        Text(ficha.puedeSerConfundidoCon, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// --- ENGINE: MATCHING CON GEMINI ---
suspend fun findGoldenPeaks(
    tideService: NoaaTideService,
    sunService: SunriseSunsetService,
    lat: Double,
    lon: Double,
    stationId: String
): List<GoldenPeak> = withContext(Dispatchers.IO) {
    val peaks = mutableListOf<GoldenPeak>()
    val cal = java.util.Calendar.getInstance()
    val sdfNoaa = java.text.SimpleDateFormat("yyyyMMdd", Locale.US)
    val sdfSun = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfTideInput = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val sdfSunInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    val sdfOutput = java.text.SimpleDateFormat("h:mm a", Locale.US)
    val sdfDateOutput = java.text.SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "PR"))

    val startStrNoaa = sdfNoaa.format(cal.time)
    val startStrSun = sdfSun.format(cal.time)
    
    cal.add(java.util.Calendar.DAY_OF_YEAR, 30)
    val endStrNoaa = sdfNoaa.format(cal.time)
    val endStrSun = sdfSun.format(cal.time)

    try {
        val tideRes = tideService.getTidePredictions(beginDate = startStrNoaa, endDate = endStrNoaa, station = stationId)
        val sunRes = sunService.getRange(lat = lat, lng = lon, start = startStrSun, end = endStrSun)

        val highTides = tideRes.predictions?.filter { it.type == "H" } ?: emptyList()
        val days = sunRes.days ?: emptyList()

        highTides.forEach { tide ->
            val tideDate = try { sdfTideInput.parse(tide.t) } catch(e: Exception) { null } ?: return@forEach
            
            // Buscar datos de sol para este día
            val tideDayStr = sdfSun.format(tideDate)
            val dayData = days.find { it.date == tideDayStr } ?: return@forEach
            
            val sunriseDate = try { sdfSunInput.parse(dayData.sunrise) } catch(e: Exception) { null }
            val sunsetDate = try { sdfSunInput.parse(dayData.sunset) } catch(e: Exception) { null }
            
            if (sunriseDate != null && Math.abs(tideDate.time - sunriseDate.time) < 90 * 60 * 1000) {
                peaks.add(GoldenPeak(sdfDateOutput.format(tideDate).replaceFirstChar { it.uppercase() }, sdfOutput.format(tideDate), "Amanecer"))
            } else if (sunsetDate != null && Math.abs(tideDate.time - sunsetDate.time) < 90 * 60 * 1000) {
                peaks.add(GoldenPeak(sdfDateOutput.format(tideDate).replaceFirstChar { it.uppercase() }, sdfOutput.format(tideDate), "Atardecer"))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    return@withContext peaks
}

fun findNearestTideStation(lat: Double, lon: Double): TideStation {
    return NOAA_STATIONS_PR.minByOrNull { station ->
        val dLat = Math.toRadians(station.lat - lat)
        val dLon = Math.toRadians(station.lon - lon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(station.lat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        6371 * c // Distance in km
    } ?: NOAA_STATIONS_PR[0]
}

fun calculateCoastalScore(heightFt: Float, periodSec: Float): Int {
    var score = 5 // Base score
    
    // Period is king for surf casting
    if (periodSec > 10) score += 3
    else if (periodSec > 7) score += 1
    else score -= 2
    
    // Height management
    if (heightFt in 2.0..5.0) score += 2 // Ideal height
    else if (heightFt > 8.0) score -= 3 // Too rough
    else if (heightFt < 1.0) score -= 1 // Too flat
    
    return score.coerceIn(1, 10)
}

fun calculateTideFactor(predictions: List<TidePrediction>): Triple<Float, String, String> {
    if (predictions.size < 2) return Triple(0.5f, "Sin datos", "")
    
    val now = java.util.Date()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val timeFormatter = java.text.SimpleDateFormat("h:mm a", Locale.US)
    
    // Sort predictions by time
    val sorted = predictions.mapNotNull { pred ->
        try { sdf.parse(pred.t)?.let { it to pred } } catch (e: Exception) { null }
    }.sortedBy { it.first }
    
    // Find where "now" sits
    for (i in 0 until sorted.size - 1) {
        val (p1Time, p1Data) = sorted[i]
        val (p2Time, _) = sorted[i+1]
        
        if (now.after(p1Time) && now.before(p2Time)) {
            val totalTime = p2Time.time - p1Time.time
            val elapsedTime = now.time - p1Time.time
            val progress = elapsedTime.toFloat() / totalTime.toFloat()
            
            val formattedNextTime = timeFormatter.format(p2Time)
            
            // Rising: Map 0.0-1.0 progress to 0.0-0.5 needle factor
            // Falling: Map 0.0-1.0 progress to 0.5-1.0 needle factor
            return if (p1Data.type == "L") {
                Triple((progress * 0.5f), "Subiendo (${(progress * 100).toInt()}%)", formattedNextTime)
            } else {
                Triple((0.5f + progress * 0.5f), "Bajando (${(progress * 100).toInt()}%)", formattedNextTime)
            }
        }
    }
    
    // If not between, use the closest or last state
    return Triple(0.5f, "Estable", "")
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
                                    val nombreLugar = if (record.spotId.isNotEmpty()) spots[record.spotId] ?: record.lugar else record.lugar
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(nombreLugar, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(record.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${record.peso} | ${record.longitud}", style = MaterialTheme.typography.bodySmall)
                                            Row {
                                                if (record.spotId.isNotEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaPescapr(
    database: AppDatabase,
    repository: CatchRepository,
    spotRepository: SpotRepository,
    subscriptionManager: SubscriptionManager? = null,
    userId: String,
    isPro: Boolean,
    spotIdAFocar: String? = null,
    onFocoLogrado: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    var mostrarPaywallDialog by remember { mutableStateOf(false) }

    if (mostrarPaywallDialog && subscriptionManager != null) {
        var productDetails by remember { mutableStateOf<com.android.billingclient.api.ProductDetails?>(null) }
        var cargandoPaywall by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            subscriptionManager.queryProductDetails("pescapr_pro_monthly") { details ->
                productDetails = details
                cargandoPaywall = false
            }
        }

        PaywallDialog(
            productDetails = productDetails,
            isLoading = cargandoPaywall,
            onSubscribeClicked = {
                val activity = context as? android.app.Activity
                if (activity != null && productDetails != null) {
                    subscriptionManager.launchBillingFlow(activity, productDetails!!)
                }
            },
            onDismiss = { mostrarPaywallDialog = false }
        )
    }
    
    val viewModel: MapViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MapViewModel(spotRepository) as T
        }
    })

    val pinesComunidad by viewModel.pinesComunidad.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val spotDao = remember { database.spotDao() }
    val recordDao = remember { database.recordDao() }
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        hasLocationPermission = p[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) { if (!hasLocationPermission) launcher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)) }

    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(18.2208, -66.5901), 9f) }

    val misPuntos = remember { mutableStateListOf<PuntoPesca>() }
    var verPinesComunidad by remember { mutableStateOf(false) }
    var spotSeleccionado by remember { mutableStateOf<PuntoPesca?>(null) }
    var mostrarSheet by remember { mutableStateOf(false) }

    LaunchedEffect(verPinesComunidad) {
        if (verPinesComunidad && pinesComunidad.isEmpty()) {
            viewModel.refreshPins(userId, isPro)
        }
    }

    // --- NUEVOS PUNTOS ---
    var mostrarDialogoNuevoPunto by remember { mutableStateOf(false) }
    var nuevaCoordenada by remember { mutableStateOf<LatLng?>(null) }
    var nombreNuevoPunto by remember { mutableStateOf("") }
    var descripcionNuevoPunto by remember { mutableStateOf("") }
    var guardandoPunto by remember { mutableStateOf(false) }

    var subiendoFotoSpot by remember { mutableStateOf(false) }
    var fotoAmpliadaUrl by remember { mutableStateOf<String?>(null) }

    // --- CAPTURAS EN EL SPOT ---
    val capturasSpot = remember { mutableStateListOf<RecordPesca>() }
    var mostrarDialogoCaptura by remember { mutableStateOf(false) }
    var cargandoCapturas by remember { mutableStateOf(false) }
    val fichasGuia = remember { mutableStateListOf<FichaPez>() }

    // --- NUEVAS CAPTURAS ---
    var recordParaEditarCaptura by remember { mutableStateOf<RecordPesca?>(null) }
    var nombrePezCaptura by remember { mutableStateOf("") }
    var pesoCaptura by remember { mutableStateOf("") }
    var longitudCaptura by remember { mutableStateOf("") }
    var fichaSeleccionada by remember { mutableStateOf<FichaPez?>(null) }
    val bitmapsCaptura = remember { mutableStateListOf<Bitmap>() }
    var guardandoCaptura by remember { mutableStateOf(false) }
    var analizandoIA by remember { mutableStateOf(false) }
    var expandedGuia by remember { mutableStateOf(false) }

    // Clima
    var datosClima by remember { mutableStateOf<WeatherResponse?>(null) }
    var cargandoClima by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarDialogoCaptura) {
        if (mostrarDialogoCaptura) {
            nombrePezCaptura = recordParaEditarCaptura?.nombrePez ?: ""
            pesoCaptura = recordParaEditarCaptura?.peso ?: ""
            longitudCaptura = recordParaEditarCaptura?.longitud ?: ""
            fichaSeleccionada = fichasGuia.find { it.id == recordParaEditarCaptura?.fishId }
            bitmapsCaptura.clear()
        }
    }

    LaunchedEffect(Unit) {
        db.collection("fichas_peces").addSnapshotListener { snap, _ ->
            val listaFichas = snap?.documents?.mapNotNull { doc ->
                FichaPez(
                    id = doc.id,
                    nombreComun = doc.getString("nombreComun") ?: "",
                    nombreCientifico = doc.getString("nombreCientifico") ?: "",
                    fotosUrls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                )
            }?.sortedBy { it.nombreCientifico } ?: emptyList()
            
            fichasGuia.clear()
            fichasGuia.addAll(listaFichas)
        }
    }

    LaunchedEffect(spotSeleccionado) {
        spotSeleccionado?.let { spot ->
            val spotIdInt = spot.id.toIntOrNull() ?: return@let
            cargandoCapturas = true
            recordDao.getRecordsBySpot(spotIdInt).collect { entities ->
                capturasSpot.clear()
                capturasSpot.addAll(entities.map { entity ->
                    RecordPesca(
                        id = entity.id.toString(),
                        nombrePez = entity.nombrePez,
                        peso = entity.peso,
                        longitud = entity.longitud,
                        fecha = entity.fecha,
                        fotosUrls = entity.fotosUrls,
                        climaTemp = entity.climaTemp,
                        climaWind = entity.climaWind,
                        climaPressure = entity.climaPressure,
                        climaTide = entity.climaTide,
                        spotId = entity.spotId.toString(),
                        fishId = entity.fishId
                    )
                })
                cargandoCapturas = false
            }
        }
    }
    
    val weatherService = remember {
        Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherService::class.java)
    }

    val tideService = remember {
        Retrofit.Builder().baseUrl("https://api.tidesandcurrents.noaa.gov/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(NoaaTideService::class.java)
    }

    val sunService = remember {
        Retrofit.Builder().baseUrl("https://api.sunrise-sunset.org/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(SunriseSunsetService::class.java)
    }

    val marineService = remember {
        Retrofit.Builder().baseUrl("https://marine-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(MarineWeatherService::class.java)
    }

    var tideFactor by remember { mutableStateOf(0.5f) }
    var tideDescription by remember { mutableStateOf("Selecciona un spot") }
    var nextTideTime by remember { mutableStateOf("") }

    var swellMetrics by remember { mutableStateOf<ProSwellMetrics?>(null) }
    var cargandoSwell by remember { mutableStateOf(false) }
    var swellProbePointsAttempted by remember { mutableIntStateOf(0) }
    var swellProbeHits by remember { mutableIntStateOf(0) }
    var lastSwellError by remember { mutableStateOf("") }

    var goldenPeaks by remember { mutableStateOf<List<GoldenPeak>>(emptyList()) }
    var calculandoPicos by remember { mutableStateOf(false) }
    var mostrarPicosDialog by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(spotSeleccionado, refreshKey) {
        spotSeleccionado?.let { spot ->
            // Fetch Weather and Swell in Parallel
            launch {
                cargandoClima = true
                try {
                    datosClima = weatherService.getWeather(
                        spot.coordenada.latitude, 
                        spot.coordenada.longitude, 
                        BuildConfig.OPENWEATHER_API_KEY
                    )
                } catch (e: Exception) {
                    android.util.Log.e("PescaPR", "Weather Fetch Error: ${e.message}")
                    datosClima = null
                } finally {
                    cargandoClima = false
                }
            }

            launch {
                cargandoSwell = true
                swellProbeHits = 0
                lastSwellError = ""
                try {
                    val radii = listOf(0.04, 0.1, 0.2, 0.4, 0.6) // More offshore points
                    val searchPoints = mutableListOf<Pair<Double, Double>>()
                    searchPoints.add(spot.coordenada.latitude to spot.coordenada.longitude)
                    for (r in radii) {
                        searchPoints.add(spot.coordenada.latitude + r to spot.coordenada.longitude)
                        searchPoints.add(spot.coordenada.latitude - r to spot.coordenada.longitude)
                        searchPoints.add(spot.coordenada.latitude to spot.coordenada.longitude + r)
                        searchPoints.add(spot.coordenada.latitude to spot.coordenada.longitude - r)
                    }
                    // Filter duplicate or redundant points if any
                    val uniquePoints = searchPoints.distinct()
                    swellProbePointsAttempted = uniquePoints.size

                    val deferreds = uniquePoints.map { point ->
                        async {
                            try {
                                // Attempt with default (sea selection)
                                val response = marineService.getSwellData(point.first, point.second)
                                val hourly = response.hourly
                                if (hourly == null || hourly.waveHeight.isEmpty()) {
                                    null
                                } else {
                                    val cal = java.util.Calendar.getInstance()
                                    val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                                    val safeHour = currentHour.coerceIn(0, hourly.waveHeight.size - 1)
                                    
                                    if (hourly.waveHeight[safeHour] != null) {
                                        point to response
                                    } else null
                                }
                            } catch (e: Exception) { 
                                lastSwellError = e.message ?: "Unknown Error"
                                null 
                            }
                        }
                    }

                    val results = deferreds.awaitAll().filterNotNull()
                    swellProbeHits = results.size
                    
                    if (results.isNotEmpty()) {
                        val bestResult = results.maxByOrNull { (_, res) -> 
                            val h = res.hourly?.waveHeight?.getOrNull(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) ?: 0f
                            h
                        }
                        
                        bestResult?.let { (_, swellRes) ->
                            val hourly = swellRes.hourly!!
                            val cal = java.util.Calendar.getInstance()
                            val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                            val safeHour = currentHour.coerceIn(0, hourly.waveHeight.size - 1)
                            
                            val height = hourly.waveHeight[safeHour] ?: 0f
                            val period = hourly.wavePeriod[safeHour] ?: 0f
                            val direction = hourly.waveDirection[safeHour] ?: 0f
                            
                            swellMetrics = ProSwellMetrics(
                                heightFt = height,
                                periodSec = period,
                                directionDeg = direction,
                                score = calculateCoastalScore(height, period)
                            )
                        }
                    } else {
                        swellMetrics = null
                    }
                } catch (e: Exception) {
                    lastSwellError = e.message ?: "Master error"
                    swellMetrics = null
                } finally {
                    cargandoSwell = false
                }
            }

            // Tides (Parallelized)
            launch {
                try {
                    val station = findNearestTideStation(spot.coordenada.latitude, spot.coordenada.longitude)
                    val response = tideService.getTidePredictions(station = station.id, date = "today")
                    response.predictions?.let { preds ->
                        val (factor, desc, time) = calculateTideFactor(preds)
                        tideFactor = factor
                        tideDescription = desc
                        nextTideTime = time
                    } ?: run {
                        tideDescription = "Sin predicciones"
                    }
                } catch (e: Exception) {
                    tideDescription = "Error de red"
                    nextTideTime = ""
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        spotDao.getAllSpots().collect { entities ->
            misPuntos.clear()
            misPuntos.addAll(entities.map { entity ->
                PuntoPesca(
                    id = entity.id.toString(),
                    latitude = entity.latitud,
                    longitude = entity.longitud,
                    nombre = entity.nombre,
                    descripcion = entity.descripcion,
                    fotosUrls = entity.fotosUrls,
                    userId = entity.userId
                )
            })
        }
    }

    LaunchedEffect(spotIdAFocar, misPuntos.size) {
        if (spotIdAFocar != null && misPuntos.isNotEmpty()) {
            val spot = misPuntos.find { it.id == spotIdAFocar }
            if (spot != null) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(spot.coordenada, 15f)
                )
                spotSeleccionado = spot
                mostrarSheet = true
                onFocoLogrado()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.SATELLITE,
                mapStyleOptions = if (isPro) {
                    try {
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.coastal_morphology_style)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else null
            ),
            onMapLongClick = { latLng ->
                nuevaCoordenada = latLng
                nombreNuevoPunto = ""
                descripcionNuevoPunto = ""
                mostrarDialogoNuevoPunto = true
            }
        ) {
            val listaAMostrar = if (verPinesComunidad) pinesComunidad else misPuntos
            listaAMostrar.forEach { spot ->
                Marker(
                    state = MarkerState(position = spot.coordenada),
                    title = spot.nombre,
                    onClick = { spotSeleccionado = spot; mostrarSheet = true; true },
                    icon = if (verPinesComunidad) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    } else {
                        remember(context) {
                            try {
                                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pin_pescapr)
                                if (bitmap != null) {
                                    val scaled = bitmap.scale(100, 100, true)
                                    BitmapDescriptorFactory.fromBitmap(scaled)
                                } else null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(0.9f), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            TextButton(onClick = { verPinesComunidad = false }) {
                Text("Mis Pines", color = if (!verPinesComunidad) MaterialTheme.colorScheme.primary else Color.Gray)
            }
            TextButton(
                onClick = { if (isPro) verPinesComunidad = true },
                enabled = true // Clickable to show teaser or disabled
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Comunidad", color = if (verPinesComunidad) MaterialTheme.colorScheme.primary else Color.Gray)
                    if (!isPro) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    }
                }
            }

            if (verPinesComunidad) {
                IconButton(
                    onClick = { viewModel.refreshPins(userId, isPro) },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (mostrarPicosDialog) {
        DialogoPicosDeOro(peaks = goldenPeaks) { mostrarPicosDialog = false }
    }

    if (mostrarSheet) {
        val photoPickerLauncherSpot = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                coroutineScope.launch {
                    subiendoFotoSpot = true
                    try {
                        val stream = context.contentResolver.openInputStream(it)
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            val localPath = saveImageToInternalStorage(context, bmp, "spots")
                            if (localPath != null) {
                                val spotActual = misPuntos.find { it.id == spotSeleccionado?.id } ?: spotSeleccionado
                                if (spotActual != null) {
                                    val currentFotos = spotActual.fotosUrls.toMutableList()
                                    currentFotos.add(localPath)
                                    
                                    val spotIdInt = spotActual.id.toIntOrNull() ?: 0
                                    val entity = SpotEntity(
                                        id = spotIdInt,
                                        nombre = spotActual.nombre,
                                        descripcion = spotActual.descripcion,
                                        latitud = spotActual.coordenada.latitude,
                                        longitud = spotActual.coordenada.longitude,
                                        fotosUrls = currentFotos
                                    )
                                    spotDao.updateSpot(entity)
                                    Toast.makeText(context, "Foto añadida localmente", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error al guardar foto", Toast.LENGTH_SHORT).show()
                    } finally {
                        subiendoFotoSpot = false
                    }
                }
            }
        }

        ModalBottomSheet(onDismissRequest = { mostrarSheet = false }) {
            val spotActual = misPuntos.find { it.id == spotSeleccionado?.id } ?: spotSeleccionado
            
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight(0.85f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(spotActual?.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(spotActual?.descripcion ?: "", color = Color.Gray)
                
                Spacer(Modifier.height(16.dp))

                if (cargandoClima) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text("Cargando clima...", style = MaterialTheme.typography.labelSmall)
                } else if (datosClima != null) {
                    val clima = datosClima!!
                    val pressureInHg = clima.main.pressure * 0.02953
                    val pressureFormatted = String.format(Locale.US, "%.2f", pressureInHg)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signos Vitales del Spot",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = {
                                spotActual?.let { spot ->
                                    coroutineScope.launch {
                                        calculandoPicos = true
                                        val station = findNearestTideStation(spot.coordenada.latitude, spot.coordenada.longitude)
                                        goldenPeaks = findGoldenPeaks(tideService, sunService, spot.coordenada.latitude, spot.coordenada.longitude, station.id)
                                        calculandoPicos = false
                                        mostrarPicosDialog = true
                                    }
                                }
                            },
                            enabled = !calculandoPicos
                        ) {
                            if (calculandoPicos) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, "Encontrar Picos de Oro", tint = Color(0xFFFFD700))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                            WeatherInfoItem(Icons.Default.Thermostat, "${clima.main.temp.toInt()}°F", "Temperatura")
                            WeatherInfoItem(Icons.Default.Air, "${clima.wind.speed.toInt()} mph", "Viento")
                            WeatherInfoItem(Icons.Default.Speed, "$pressureFormatted inHg", "Presión")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { 
                                        refreshKey++
                                        Toast.makeText(context, "Actualizando condiciones...", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                RelojMareasCircular(valor = tideFactor, nextTime = nextTideTime)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("MAREA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(tideDescription, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }

                    if (isPro) {
                        Spacer(Modifier.height(16.dp))
                        if (cargandoSwell) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                        } else if (swellMetrics != null) {
                            ProSwellCard(metrics = swellMetrics!!)
                            Text(
                                "Debug Marejada: Probes=$swellProbePointsAttempted, Hits=$swellProbeHits",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        } else {
                            // Mostrar mensaje de que el spot no es costero o no hay datos
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Métricas de Marejada solo disponibles en áreas costeras.",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Debug: Probes=$swellProbePointsAttempted, Hits=$swellProbeHits, Error=$lastSwellError",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(16.dp))
                        ProFeatureTeaser(
                            title = "Métricas de Marejada & Oleaje",
                            description = "Desbloquea altura de olas, periodo y dirección en tiempo real con PescaPR Pro.",
                            onUpgradeClick = { mostrarPaywallDialog = true }
                        )
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.1f))) {
                        Text("Información de clima no disponible", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                val fotos = spotActual?.fotosUrls ?: emptyList()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Fotos del Spot (${fotos.size}/4)", style = MaterialTheme.typography.titleSmall)
                    if (fotos.size < 4) {
                        IconButton(onClick = { photoPickerLauncherSpot.launch("image/*") }, enabled = !subiendoFotoSpot) {
                            if (subiendoFotoSpot) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            else Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (fotos.isNotEmpty()) {
                    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(fotos) { url ->
                            Box {
                                AsyncImage(
                                    model = url, 
                                    contentDescription = null, 
                                    modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable { fotoAmpliadaUrl = url }, 
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                if (spotActual != null) {
                                                    val currentFotos = spotActual.fotosUrls.toMutableList()
                                                    currentFotos.remove(url)
                                                    
                                                    val spotIdInt = spotActual.id.toIntOrNull() ?: 0
                                                    val entity = SpotEntity(
                                                        id = spotIdInt,
                                                        nombre = spotActual.nombre,
                                                        descripcion = spotActual.descripcion,
                                                        latitud = spotActual.coordenada.latitude,
                                                        longitud = spotActual.coordenada.longitude,
                                                        fotosUrls = currentFotos
                                                    )
                                                    spotDao.updateSpot(entity)
                                                    
                                                    // Opcional: Eliminar el archivo físico
                                                    try { File(url).delete() } catch (e: Exception) {}
                                                    
                                                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd).background(Color.White.copy(0.7f), CircleShape).padding(2.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                }
                            }
                        }
                    }
                } else {
                    Text("No hay fotos de esta ubicación.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Capturas en esta ubicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { mostrarDialogoCaptura = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Registrar")
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (cargandoCapturas) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (capturasSpot.isEmpty()) {
                    Text("No hay capturas registradas aquí aún.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        capturasSpot.forEach { record ->
                            var verDetallesCaptura by remember { mutableStateOf(false) }
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { verDetallesCaptura = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (record.fotosUrls.isNotEmpty()) {
                                        AsyncImage(model = record.fotosUrls.first(), contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    } else {
                                        Box(modifier = Modifier.size(50.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Image, null, tint = Color.Gray)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(record.nombrePez, fontWeight = FontWeight.Bold)
                                        Text("${record.peso} | ${record.longitud}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    
                                    Row {
                                        IconButton(onClick = {
                                            recordParaEditarCaptura = record
                                            mostrarDialogoCaptura = true
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
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
                                                    
                                                    // Opcional: Eliminar fotos locales
                                                    record.fotosUrls.forEach { path -> try { File(path).delete() } catch (e: Exception) {} }
                                                    
                                                    Toast.makeText(context, "Captura eliminada", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }

                            if (verDetallesCaptura) {
                                Dialog(onDismissRequest = { verDetallesCaptura = false }) {
                                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Detalles de la Captura", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(16.dp))
                                            
                                            if (record.fotosUrls.isNotEmpty()) {
                                                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(150.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    items(record.fotosUrls) { url ->
                                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                                    }
                                                }
                                                Spacer(Modifier.height(16.dp))
                                            }

                                            Text(record.nombrePez, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Text("Peso: ${record.peso}", style = MaterialTheme.typography.bodyMedium)
                                                Text("Longitud: ${record.longitud}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Text("Fecha: ${record.fecha}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            
                                            Spacer(Modifier.height(16.dp))
                                            HorizontalDivider()
                                            Spacer(Modifier.height(16.dp))
                                            
                                            Text("Condiciones al momento de captura:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(8.dp))
                                            
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                WeatherInfoItem(Icons.Default.Thermostat, record.climaTemp, "Temperatura", tintOverride = MaterialTheme.colorScheme.secondary)
                                                WeatherInfoItem(Icons.Default.Air, record.climaWind, "Viento", tintOverride = MaterialTheme.colorScheme.secondary)
                                                WeatherInfoItem(Icons.Default.Speed, record.climaPressure, "Presión", tintOverride = MaterialTheme.colorScheme.secondary)
                                                WeatherInfoItem(Icons.Default.Water, record.climaTide, "Marea", tintOverride = MaterialTheme.colorScheme.secondary)
                                            }

                                            Spacer(Modifier.height(24.dp))
                                            Button(onClick = { verDetallesCaptura = false }, modifier = Modifier.fillMaxWidth()) {
                                                Text("Cerrar")
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

    if (mostrarDialogoNuevoPunto) {
        AlertDialog(
            onDismissRequest = { if (!guardandoPunto) mostrarDialogoNuevoPunto = false },
            title = { Text("Nuevo Punto de Pesca") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNuevoPunto,
                        onValueChange = { nombreNuevoPunto = it },
                        label = { Text("Nombre del Spot") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descripcionNuevoPunto,
                        onValueChange = { descripcionNuevoPunto = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    if (guardandoPunto) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val coords = nuevaCoordenada
                        if (nombreNuevoPunto.isNotBlank() && coords != null) {
                            coroutineScope.launch {
                                guardandoPunto = true
                                try {
                                    val currentUid = userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
                                    val entity = SpotEntity(
                                        nombre = nombreNuevoPunto,
                                        descripcion = descripcionNuevoPunto,
                                        latitud = coords.latitude,
                                        longitud = coords.longitude,
                                        fotosUrls = emptyList(),
                                        userId = currentUid
                                    )
                                    spotDao.insertSpot(entity)
                                    
                                    // Subir a la comunidad con atribución userId de forma estática
                                    val nuevoSpot = PuntoPesca(
                                        latitude = coords.latitude,
                                        longitude = coords.longitude,
                                        nombre = nombreNuevoPunto,
                                        descripcion = descripcionNuevoPunto,
                                        userId = currentUid
                                    )
                                    viewModel.shareSpotToCommunity(nuevoSpot)

                                    mostrarDialogoNuevoPunto = false
                                    Toast.makeText(context, "Spot guardado localmente", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    guardandoPunto = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !guardandoPunto
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevoPunto = false }, enabled = !guardandoPunto) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoCaptura) {
        val multiPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.forEach { uri ->
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                bmp?.let { bitmapsCaptura.add(it) }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!guardandoCaptura) mostrarDialogoCaptura = false },
            title = { Text(if (recordParaEditarCaptura == null) "Registrar Captura" else "Editar Captura") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ubicación: ${spotSeleccionado?.nombre}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    
                    Button(
                        onClick = { multiPickerLauncher.launch("image/*") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        enabled = !analizandoIA && !guardandoCaptura
                    ) {
                        Icon(Icons.Default.AddAPhoto, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir Fotos")
                    }

                    if (bitmapsCaptura.isNotEmpty()) {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            bitmapsCaptura.forEachIndexed { index, bmp ->
                                Box {
                                    Image(bitmap = bmp.asImageBitmap(), null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { bitmapsCaptura.removeAt(index) }, 
                                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.White.copy(0.7f), CircleShape),
                                        enabled = !analizandoIA && !guardandoCaptura
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    analizandoIA = true
                                    val result = ejecutarMatchingConFichas(db, bitmapsCaptura.first())
                                    if (!result.esError) {
                                        val match = fichasGuia.find { it.nombreComun == result.nombreComun }
                                        if (match != null) {
                                            fichaSeleccionada = match
                                            nombrePezCaptura = match.nombreComun
                                        } else {
                                            nombrePezCaptura = result.nombreComun
                                            fichaSeleccionada = null
                                        }
                                        Toast.makeText(context, "Pez identificado: ${result.nombreComun}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error IA: ${result.nombreComun}", Toast.LENGTH_SHORT).show()
                                    }
                                    analizandoIA = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !analizandoIA && !guardandoCaptura,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            if (analizandoIA) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onTertiary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Psychology, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Identificar con IA")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Selección de Pez de la Guía
                        Box {
                            OutlinedTextField(
                                value = if (fichaSeleccionada != null) fichaSeleccionada!!.nombreComun else nombrePezCaptura,
                                onValueChange = { nombrePezCaptura = it; fichaSeleccionada = null },
                                label = { Text("Especie (Guía Oficial o Manual)") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !analizandoIA && !guardandoCaptura,
                                trailingIcon = {
                                    IconButton(onClick = { expandedGuia = true }, enabled = !analizandoIA && !guardandoCaptura) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )
                            DropdownMenu(expanded = expandedGuia, onDismissRequest = { expandedGuia = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                                fichasGuia.forEach { ficha ->
                                    DropdownMenuItem(
                                        text = { Column { Text(ficha.nombreComun, fontWeight = FontWeight.Bold); Text(ficha.nombreCientifico, style = MaterialTheme.typography.labelSmall) } },
                                        onClick = { fichaSeleccionada = ficha; expandedGuia = false }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pesoCaptura, 
                                onValueChange = { pesoCaptura = it }, 
                                label = { Text("Peso (lb/oz)") }, 
                                modifier = Modifier.weight(1f),
                                enabled = !analizandoIA && !guardandoCaptura
                            )
                            OutlinedTextField(
                                value = longitudCaptura, 
                                onValueChange = { longitudCaptura = it }, 
                                label = { Text("Longitud (pulg)") }, 
                                modifier = Modifier.weight(1f),
                                enabled = !analizandoIA && !guardandoCaptura
                            )
                        }
                    }

                    if (guardandoCaptura) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Guardando y subiendo fotos...", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalNombre = if (fichaSeleccionada != null) fichaSeleccionada!!.nombreComun else nombrePezCaptura
                        if (finalNombre.isNotBlank() && spotSeleccionado != null) {
                            coroutineScope.launch {
                                guardandoCaptura = true
                                try {
                                    val uploadedUrls = mutableListOf<String>()
                                    bitmapsCaptura.forEach { bmp ->
                                        val localPath = saveImageToInternalStorage(context, bmp, "capturas")
                                        if (localPath != null) uploadedUrls.add(localPath)
                                    }

                                    val clima = datosClima
                                    val pressureInHg = clima?.main?.pressure?.let { String.format(Locale.US, "%.2f", it.toDouble() * 0.02953) } ?: "N/A"
                                    
                                    val spotIdInt = spotSeleccionado?.id?.toIntOrNull() ?: 0
                                    val recordIdInt = recordParaEditarCaptura?.id?.toIntOrNull() ?: 0

                                    val entity = RecordEntity(
                                        id = recordIdInt,
                                        nombrePez = finalNombre,
                                        fishId = fichaSeleccionada?.id,
                                        spotId = spotIdInt,
                                        peso = pesoCaptura,
                                        longitud = longitudCaptura,
                                        fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date()),
                                        fotosUrls = if (recordParaEditarCaptura != null) recordParaEditarCaptura!!.fotosUrls + uploadedUrls else uploadedUrls,
                                        climaTemp = (clima?.main?.temp?.toInt()?.toString()?.plus("°F") ?: "N/A"),
                                        climaWind = (clima?.wind?.speed?.toInt()?.toString()?.plus(" mph") ?: "N/A"),
                                        climaPressure = "$pressureInHg inHg",
                                        climaTide = tideDescription,
                                        lugar = spotSeleccionado?.nombre ?: "Ubicación desconocida"
                                    )

                                    if (recordParaEditarCaptura == null) {
                                        repository.saveCatch(entity, isPro)
                                    } else {
                                        recordDao.updateRecord(entity)
                                    }
                                    
                                    bitmapsCaptura.clear()
                                    nombrePezCaptura = ""; pesoCaptura = ""; longitudCaptura = ""; fichaSeleccionada = null
                                    recordParaEditarCaptura = null
                                    mostrarDialogoCaptura = false
                                    Toast.makeText(context, "Captura registrada localmente", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    guardandoCaptura = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Indica el nombre del pez", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !guardandoCaptura && !analizandoIA && (if (fichaSeleccionada != null) fichaSeleccionada!!.nombreComun else nombrePezCaptura).isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCaptura = false }, enabled = !guardandoCaptura) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (fotoAmpliadaUrl != null) {
        Dialog(onDismissRequest = { fotoAmpliadaUrl = null }) {
            Box(modifier = Modifier.fillMaxSize().clickable { fotoAmpliadaUrl = null }, contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = fotoAmpliadaUrl, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)), 
                    contentScale = ContentScale.Fit
                )
            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCarousel(
    urls: List<String>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { urls.size })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urls[page])
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto ${page + 1} de ${urls.size}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.logo_small),
                error = painterResource(id = R.drawable.logo_small)
            )
        }

        if (urls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(urls.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val color by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        label = "dotColor"
                    )
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 12.dp else 6.dp,
                        label = "dotWidth"
                    )

                    Box(
                        modifier = Modifier
                            .size(width = dotWidth, height = 6.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun ProFeatureTeaser(title: String, description: String, onUpgradeClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.2f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Actualizar a Pro", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun DialogoPicosDeOro(peaks: List<GoldenPeak>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                Text("Picos de Oro (Próximos 30 días)")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Text(
                    "Días donde la marea alta coincide con el amanecer o atardecer (+/- 90 min).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (peaks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron picos ideales en este periodo.", textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(peaks) { peak ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(peak.date, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(peak.time, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    
                                    Surface(
                                        color = if (peak.type == "Amanecer") Color(0xFFFFE082) else Color(0xFFFFCCBC),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            peak.type, 
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    "Datos astronómicos por sunrise-sunset.org",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Entendido")
            }
        }
    )
}

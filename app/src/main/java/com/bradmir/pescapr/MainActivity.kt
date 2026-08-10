package com.bradmir.pescapr

import android.content.Context
import com.bradmir.pescapr.data.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.UrlTileProvider
import android.util.Log
import java.net.URL
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.maps.android.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bradmir.pescapr.data.*
import com.bradmir.pescapr.network.MarineWeatherService
import com.bradmir.pescapr.ui.about.AboutDialog
import com.bradmir.pescapr.ui.CoastalMorphologyLayer
import com.bradmir.pescapr.ui.identificador.PantallaIdentificadorYRegulacionesTab
import com.bradmir.pescapr.ui.identificador.ejecutarMatchingConFichas
import com.bradmir.pescapr.ui.guia.PantallaGuiaOficialTab
import com.bradmir.pescapr.ui.components.GoldenDayBanner
import com.bradmir.pescapr.ui.components.GoldenDayPlannerCard
import com.bradmir.pescapr.ui.components.GoldenDayPlannerSheet
import com.bradmir.pescapr.ui.components.PaywallDialog
import com.bradmir.pescapr.ui.components.ProFeatureActionButtons
import com.bradmir.pescapr.ui.components.ProFeaturePaywallDialog
import com.bradmir.pescapr.ui.components.ProFeatureType
import com.bradmir.pescapr.ui.components.ProSwellCard
import com.bradmir.pescapr.ui.components.WaterTempCard
import com.bradmir.pescapr.ui.viewmodels.MapViewModel
import com.bradmir.pescapr.utils.CachedTileProvider
import com.bradmir.pescapr.utils.TileCacheManager
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
    val fotosUrls: List<String> = emptyList(),
    val localThumbResName: String = ""
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
    var currentScreen by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- GATED ACCESS (PRO TIER - SINGLE SOURCE OF TRUTH) ---
    val subscriptionManager = remember { SubscriptionManager(context) }
    val isUserPro by subscriptionManager.isProUser.collectAsState()

    val userIdState = produceState(initialValue = FirebaseAuth.getInstance().currentUser?.uid ?: "") {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { a ->
            value = a.currentUser?.uid ?: ""
        }
        auth.addAuthStateListener(listener)
        awaitDispose {
            auth.removeAuthStateListener(listener)
        }
    }
    val userId by userIdState

    // --- SILENT ANONYMOUS AUTHENTICATION ON STARTUP ---
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_small),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .combinedClickable(
                                onLongClick = {
                                    subscriptionManager.toggleDebugProState()
                                    if (BuildConfig.DEBUG) {
                                        Toast.makeText(
                                            context,
                                            "Debug: Pro Tier set to ${subscriptionManager.isProUser.value}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onClick = {}
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PescaPR Pro",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUserPro) "Plan Pro Activo" else "Plan Gratuito",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUserPro) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Mapa") },
                    selected = currentScreen == 0,
                    onClick = {
                        currentScreen = 0
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Identificador y Regulaciones") },
                    selected = currentScreen == 1,
                    onClick = {
                        currentScreen = 1
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("Guía Oficial") },
                    selected = currentScreen == 2,
                    onClick = {
                        currentScreen = 2
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Diario Privado / Records") },
                    selected = currentScreen == 3,
                    onClick = {
                        currentScreen = 3
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Acerca de") },
                    label = { Text("Acerca de PescaPR") },
                    selected = mostrarDialogoAcercaDe,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        mostrarDialogoAcercaDe = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val brandName = if (isUserPro) "PescaPR Pro" else "PescaPR"

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_small),
                                contentDescription = "PescaPR Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                subscriptionManager.toggleDebugProState()
                                                if (BuildConfig.DEBUG) {
                                                    Toast.makeText(
                                                        context,
                                                        "Debug: Pro Tier set to ${subscriptionManager.isProUser.value}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                    }
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = brandName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú Principal")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentScreen) {
                    0 -> PantallaMapaTab(
                        database = database,
                        repository = catchRepository,
                        spotRepository = spotRepository,
                        subscriptionManager = subscriptionManager,
                        userId = userId,
                        isPro = isUserPro,
                        spotIdAFocar = spotIdAFocar,
                        onFocoLogrado = { spotIdAFocar = null }
                    )
                    1 -> PantallaIdentificadorYRegulacionesTab()
                    2 -> PantallaGuiaOficialTab()
                    3 -> PantallaRecordsTab(
                        database = database,
                        repository = catchRepository,
                        onIrALugar = { id ->
                            spotIdAFocar = id
                            currentScreen = 0
                        }
                    )
                }
            }
        }
    }

    if (mostrarDialogoAcercaDe) {
        AboutDialog(onDismiss = { mostrarDialogoAcercaDe = false })
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
    var showMorphologyLayer by remember { mutableStateOf(false) }
    var activePaywallFeature by remember { mutableStateOf<ProFeatureType?>(null) }
    var mostrarPaywallSheet by remember { mutableStateOf(false) }

    com.bradmir.pescapr.ui.MapaPescapr(
        database = database,
        repository = repository,
        spotRepository = spotRepository,
        subscriptionManager = subscriptionManager,
        userId = userId,
        isPro = isPro,
        spotIdAFocar = spotIdAFocar,
        onFocoLogrado = onFocoLogrado,
        showMorphologyLayer = showMorphologyLayer,
        onToggleMorphology = { showMorphologyLayer = !showMorphologyLayer },
        onTriggerPaywall = { featureType -> activePaywallFeature = featureType }
    )

    activePaywallFeature?.let { feature ->
        ProFeaturePaywallDialog(
            feature = feature,
            onDismiss = { activePaywallFeature = null },
            onUpgradeClick = {
                activePaywallFeature = null
                mostrarPaywallSheet = true
            }
        )
    }

    if (mostrarPaywallSheet) {
        val context = LocalContext.current
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
            onDismiss = { mostrarPaywallSheet = false }
        )
    }
}

// --- TAB 2: IDENTIFICADOR (Matching with Cards) ---
// --- TAB 3: GUÍA OFICIAL (User creates these) ---

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
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MapViewModel(spotRepository, context.applicationContext) as T
        }
    })

      val pinesComunidad by viewModel.pinesComunidad.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()

    Log.d("MapStateCircuit", "UI Recomposed - isPro: $isPro")

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
    var showMorphologyLayer by remember { mutableStateOf(false) }
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
    var activePaywallFeature by remember { mutableStateOf<ProFeatureType?>(null) }
    var expandedProFeature by remember { mutableStateOf<ProFeatureType?>(null) }
    var mostrar30DayPlannerSheet by remember { mutableStateOf(false) }
    var goldenTide30DayList by remember { mutableStateOf<List<GoldenDayPrediction>>(emptyList()) }

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
                    firestoreId = entity.firestoreId,
                    latitude = entity.latitud,
                    longitude = entity.longitud,
                    nombre = entity.nombre,
                    descripcion = entity.descripcion,
                    fotosUrls = entity.fotosUrls,
                    approvedPhotos = entity.approvedPhotos,
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
        AnimatedVisibility(
            visible = isOffline,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
                .padding(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Modo Off-line - Mostrando puntos guardados localmente",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.SATELLITE,
      mapStyleOptions = null
            ),
            uiSettings = MapUiSettings(
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = true,
                rotationGesturesEnabled = true,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            ),
                  onMapLongClick = { latLng ->
            nuevaCoordenada = latLng
            nombreNuevoPunto = ""
            descripcionNuevoPunto = ""
            mostrarDialogoNuevoPunto = true
        }
    ) {
        CoastalMorphologyLayer(enabled = isPro && showMorphologyLayer)

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
                .padding(top = 12.dp)
                .zIndex(5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { verPinesComunidad = false },
                        shape = RoundedCornerShape(20.dp),
                        color = if (!verPinesComunidad) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Text(
                            text = "Mis Spots",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (!verPinesComunidad) FontWeight.Bold else FontWeight.Normal,
                            color = if (!verPinesComunidad) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = {
                            if (isPro) {
                                verPinesComunidad = true
                            } else {
                                mostrarPaywallDialog = true
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (verPinesComunidad) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Comunidad",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (verPinesComunidad) FontWeight.Bold else FontWeight.Normal,
                                color = if (verPinesComunidad) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isPro) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Pro",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (verPinesComunidad) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                onClick = {
                    if (isPro) {
                        showMorphologyLayer = !showMorphologyLayer
                    } else {
                        activePaywallFeature = ProFeatureType.MORFOLOGIA
                    }
                },
                shape = CircleShape,
                color = if (showMorphologyLayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Morfología Costera",
                        tint = if (showMorphologyLayer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
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
                    fotosUrls = currentFotos,
                    userId = spotActual.userId,
                    firestoreId = spotActual.firestoreId,
                    approvedPhotos = spotActual.approvedPhotos
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

                    Spacer(Modifier.height(16.dp))
                    ProFeatureActionButtons(
                        selectedFeature = if (isPro) expandedProFeature else null,
                        onFeatureClick = { feature ->
                            if (isPro) {
                                if (feature == ProFeatureType.PLANIFICADOR) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val list = generate30DayGoldenTideWindows(
                                            spotActual?.latitude ?: 18.2208,
                                            spotActual?.longitude ?: -66.5901
                                        )
                                        withContext(Dispatchers.Main) {
                                            goldenTide30DayList = list
                                            mostrar30DayPlannerSheet = true
                                        }
                                    }
                                } else {
                                    expandedProFeature = if (expandedProFeature == feature) null else feature
                                }
                            } else {
                                activePaywallFeature = feature
                            }
                        }
                    )

                    if (isPro && expandedProFeature != null) {
                        Spacer(Modifier.height(12.dp))
                        when (expandedProFeature) {
                            ProFeatureType.TEMP_TENDENCIA -> {
                                WaterTempCard(
                                    isPro = true,
                                    currentWaterTemp = null,
                                    trendResult = null,
                                    ambientAirTempF = datosClima?.main?.temp?.toFloat()
                                )
                            }
                            ProFeatureType.MAREJADAS -> {
                                if (cargandoSwell) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                                } else if (swellMetrics != null) {
                                    ProSwellCard(metrics = swellMetrics!!)
                                } else {
                                    Text(
                                        "Métricas de Marejada solo disponibles en áreas costeras.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }

                    if (mostrar30DayPlannerSheet) {
                        GoldenDayPlannerSheet(
                            predictions = goldenTide30DayList,
                            onDismiss = { mostrar30DayPlannerSheet = false }
                        )
                    }

                    activePaywallFeature?.let { feature ->
                        ProFeaturePaywallDialog(
                            feature = feature,
                            onDismiss = { activePaywallFeature = null },
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
                                                        fotosUrls = currentFotos,
                                                        userId = spotActual.userId,
                                                        firestoreId = spotActual.firestoreId,
                                                        approvedPhotos = spotActual.approvedPhotos
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
                                    val nuevoSpot = PuntoPesca(
                                        latitude = coords.latitude,
                                        longitude = coords.longitude,
                                        nombre = nombreNuevoPunto,
                                        descripcion = descripcionNuevoPunto,
                                        userId = currentUid
                                    )
                                    val syncedFirestoreId = viewModel.shareSpotToCommunity(nuevoSpot) ?: ""
                                    val entity = SpotEntity(
                                        nombre = nombreNuevoPunto,
                                        descripcion = descripcionNuevoPunto,
                                        latitud = coords.latitude,
                                        longitud = coords.longitude,
                                        fotosUrls = emptyList(),
                                        userId = currentUid,
                                        firestoreId = syncedFirestoreId,
                                        approvedPhotos = emptyList()
                                    )
                                    spotDao.insertSpot(entity)

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
}

@Composable
fun ActionBtn(icon: ImageVector, label: String, color: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, null, tint = color) }
        Text(label, style = MaterialTheme.typography.labelSmall)
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

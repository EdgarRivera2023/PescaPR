package com.bradmir.pescapr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.compose.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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

// --- 2. ACTIVIDAD PRINCIPAL ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MapaPescapr()
            }
        }
    }
}

// --- 3. LÓGICA DE FIREBASE ---

fun procesarYSubirFoto(bitmap: Bitmap, punto: PuntoPesca, storage: StorageReference, db: FirebaseFirestore, context: Context) {
    try {
        val baos = ByteArrayOutputStream()
        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
        val scaled = Bitmap.createScaledBitmap(bitmap, 1024, (1024 * aspectRatio).toInt(), true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val data = baos.toByteArray()
        val nombreFoto = "foto_${UUID.randomUUID()}.jpg"
        val fotoRef = storage.child("fotos_pesca/${punto.id}/$nombreFoto")
        fotoRef.putBytes(data).addOnSuccessListener {
            fotoRef.downloadUrl.addOnSuccessListener { uri ->
                db.collection("spots").document(punto.id).update("fotosUrls", FieldValue.arrayUnion(uri.toString()))
            }
        }
    } catch (e: Exception) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
}

fun borrarFotoIndividual(url: String, punto: PuntoPesca, db: FirebaseFirestore, storage: FirebaseStorage, context: Context) {
    db.collection("spots").document(punto.id).update("fotosUrls", FieldValue.arrayRemove(url)).addOnSuccessListener {
        try { storage.getReferenceFromUrl(url).delete() } catch (e: Exception) { }
    }
}

// --- 4. UI PRINCIPAL ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MapaPescapr() {
    val context = LocalContext.current
    val db = remember { Firebase.firestore }
    val storage = remember { Firebase.storage }
    val storageRef = remember { storage.reference }
    val misPuntosDePesca = remember { mutableStateListOf<PuntoPesca>() }

    val weatherApiKey = BuildConfig.OPENWEATHER_API_KEY

    var datosClima by remember { mutableStateOf<WeatherResponse?>(null) }
    val weatherService = remember {
        Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherService::class.java)
    }

    var tipoDeMapaActual by remember { mutableStateOf(MapType.SATELLITE) }
    var mostrarSheetInfo by remember { mutableStateOf(false) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var mostrarDialogoEdicion by remember { mutableStateOf(false) }
    var mostrarConfirmacionBorrarPunto by remember { mutableStateOf(false) }
    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var puntoSeleccionado by remember { mutableStateOf<PuntoPesca?>(null) }
    var coordenadaTemporal by remember { mutableStateOf<LatLng?>(null) }
    var textoNombre by remember { mutableStateOf("") }
    var textoDescription by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    // SIMULACIÓN DE MAREA (0.0 = Baja/Rojo, 1.0 = Alta/Verde)
    val estadoMareaSimulado by remember { mutableStateOf(0.7f) }

    LaunchedEffect(puntoSeleccionado) {
        puntoSeleccionado?.let { spot ->
            try { datosClima = weatherService.getWeather(spot.coordenada.latitude, spot.coordenada.longitude, weatherApiKey) }
            catch (e: Exception) { datosClima = null }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(15)) { uris ->
        uris.forEach { uri ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                bitmap?.let { if (puntoSeleccionado != null) procesarYSubirFoto(it, puntoSeleccionado!!, storageRef, db, context) }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { it?.let { if (puntoSeleccionado != null) procesarYSubirFoto(it, puntoSeleccionado!!, storageRef, db, context) } }

    LaunchedEffect(Unit) {
        db.collection("spots").addSnapshotListener { snapshot, _ ->
            val puntos = snapshot?.documents?.mapNotNull { doc ->
                val urls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                PuntoPesca(doc.id, LatLng(doc.getDouble("latitud") ?: 0.0, doc.getDouble("longitud") ?: 0.0), doc.getString("nombre") ?: "", doc.getString("descripcion") ?: "", urls)
            } ?: emptyList()
            misPuntosDePesca.clear(); misPuntosDePesca.addAll(puntos)
            puntoSeleccionado?.let { actual -> puntoSeleccionado = puntos.find { it.id == actual.id } }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(18.2208, -66.5901), 9f) },
            properties = MapProperties(isMyLocationEnabled = true, mapType = tipoDeMapaActual),
            onMapLongClick = { coordenadaTemporal = it; textoNombre = ""; textoDescription = ""; mostrarDialogoNuevo = true }
        ) {
            misPuntosDePesca.forEach { spot ->
                Marker(
                    state = MarkerState(position = spot.coordenada),
                    icon = try {
                        val b = BitmapFactory.decodeResource(context.resources, R.drawable.pin_small)
                        BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(b, 110, 110, false))
                    } catch(e: Exception) { null },
                    onClick = { puntoSeleccionado = spot; mostrarSheetInfo = true; true }
                )
            }
        }

        if (mostrarSheetInfo) {
            var fotoGrande by remember { mutableStateOf<String?>(null) }
            var fotoBorrar by remember { mutableStateOf<String?>(null) }

            ModalBottomSheet(onDismissRequest = { mostrarSheetInfo = false; fotoGrande = null }, sheetState = sheetState) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().fillMaxHeight(0.85f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = puntoSeleccionado?.nombre ?: "", style = MaterialTheme.typography.headlineSmall)
                    Text(text = puntoSeleccionado?.descripcion ?: "", color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- SECCIÓN DE SENSORES REORGANIZADA ---
                    datosClima?.let { clima ->
                        val pressureInHg = clima.main.pressure * 0.02953
                        val pressureFormatted = String.format(Locale.US, "%.2f", pressureInHg)

                        val colorPresion = when {
                            pressureInHg in 29.70..30.40 -> Color(0xFF4CAF50) // Verde: Ideal
                            pressureInHg < 29.70 || pressureInHg > 30.40 -> Color(0xFFF44336) // Rojo: Difícil
                            else -> Color(0xFFFFC107) // Amarillo
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.4f))
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sensores Numéricos (Temp, Viento, Presión)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                WeatherInfoItem(Icons.Default.Thermostat, "${clima.main.temp.toInt()}°F", "Temp")
                                WeatherInfoItem(Icons.Default.Air, "${clima.wind.speed.toInt()} mph", "Viento")
                                WeatherInfoItem(Icons.Default.Speed, "$pressureFormatted inHg", "Presión", tintOverride = colorPresion)
                            }

                            // Manómetro Visual de MAREAS
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                ManometroMareasArco(valor = estadoMareaSimulado)
                                Text(text = "MAREA", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(text = if(estadoMareaSimulado > 0.5) "Alta/Subiendo" else "Baja/Bajando", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ActionBtn(Icons.Default.AddAPhoto, "Añadir") { mostrarOpcionesFoto = true }
                        ActionBtn(Icons.Default.Edit, "Editar", Color(0xFFFFA000)) { textoNombre = puntoSeleccionado?.nombre ?: ""; textoDescription = puntoSeleccionado?.descripcion ?: ""; mostrarSheetInfo = false; mostrarDialogoEdicion = true }
                        ActionBtn(Icons.Default.Delete, "Borrar", Color.Red) { mostrarConfirmacionBorrarPunto = true }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Spacer(modifier = Modifier.height(10.dp))
                        fotoGrande?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).clickable { fotoGrande = null }, contentScale = ContentScale.Fit) }
                        val fotos = puntoSeleccionado?.fotosUrls ?: emptyList()
                        if (fotos.isNotEmpty()) {
                            Box(modifier = Modifier.height(((fotos.size + 3) / 4 * 100).dp)) {
                                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) {
                                    items(fotos) { url ->
                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.padding(2.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).combinedClickable(onClick = { fotoGrande = url }, onLongClick = { fotoBorrar = url }), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // ... (Diálogos Nuevo, Editar, Borrar se mantienen igual)
    }
}

// --- 5. COMPONENTES VISUALES ---

@Composable
fun ManometroMareasArco(valor: Float) {
    Box(
        modifier = Modifier
            .size(100.dp, 60.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12f
            val canvasWidth = size.width
            val canvasHeight = size.height

            val rectSize = Size(canvasWidth, canvasHeight * 2)
            val topLeft = Offset(0f, 0f)

            // Rojo (Marea Baja)
            drawArc(
                color = Color(0xFFF44336),
                startAngle = 180f, sweepAngle = 60f, useCenter = false,
                topLeft = topLeft, size = rectSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            // Amarillo
            drawArc(
                color = Color(0xFFFFC107),
                startAngle = 240f, sweepAngle = 60f, useCenter = false,
                topLeft = topLeft, size = rectSize,
                style = Stroke(strokeWidth)
            )
            // Verde (Marea Alta)
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = 300f, sweepAngle = 60f, useCenter = false,
                topLeft = topLeft, size = rectSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Aguja girando desde el centro de la base
            val center = Offset(canvasWidth / 2, canvasHeight)
            val angle = 180f + (valor * 180f)
            val angleRad = Math.toRadians(angle.toDouble())
            val lineLength = canvasWidth * 0.45f

            val endX = center.x + lineLength * cos(angleRad).toFloat()
            val endY = center.y + lineLength * sin(angleRad).toFloat()

            drawLine(Color.Black, center, Offset(endX, endY), strokeWidth = 6f, cap = StrokeCap.Round)
            drawCircle(Color.Black, radius = 7f, center = center)
        }
    }
}

@Composable
fun WeatherInfoItem(icon: ImageVector, value: String, label: String, tintOverride: Color? = null) {
    val tint = tintOverride ?: MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.labelLarge, color = if(tintOverride != null) tint else Color.Unspecified)
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

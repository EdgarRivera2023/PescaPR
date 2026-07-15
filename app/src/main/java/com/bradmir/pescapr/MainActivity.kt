package com.bradmir.pescapr

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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.maps.android.compose.*
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MainTabsScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen() {
    val tabs = listOf("Mapa", "Identificador", "Guía Official", "Mis Récords")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.logo_small), contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "PescaPR", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                0 -> PantallaMapaTab()
                1 -> PantallaIdentificadorYRegulacionesTab()
                2 -> PantallaGuiaOficialTab()
                3 -> PantallaRecordsTab()
            }
        }
    }
}

// --- TAB 1: MAPA ---
@Composable
fun PantallaMapaTab() {
    MapaPescapr()
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
            fichas.clear()
            snap?.documents?.forEach { doc ->
                fichas.add(FichaPez(
                    id = doc.id,
                    nombreCientifico = doc.getString("nombreCientifico") ?: "",
                    nombreComun = doc.getString("nombreComun") ?: "",
                    nombreIngles = doc.getString("nombreIngles") ?: "",
                    regulacionComercial = doc.getString("regulacionComercial") ?: "",
                    regulacionRecreativa = doc.getString("regulacionRecreativa") ?: "",
                    caracteristicas = (doc.get("caracteristicas") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    puedeSerConfundidoCon = doc.getString("puedeSerConfundidoCon") ?: "",
                    fotosUrls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                ))
            }
        }
        onDispose { listener?.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Guía Oficial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            TextButton(onClick = { esDeveloper = !esDeveloper }) {
                Text(if(esDeveloper) "Admin ON" else "Modo Vista")
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

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fichas) { ficha ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clickable(enabled = esDeveloper) {
                        fichaParaEditar = ficha
                        mostrarDialogoNueva = true
                    }, 
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box {
                        AsyncImage(model = ficha.fotosUrls.firstOrNull(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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

}

// --- ENGINE: MATCHING CON GEMINI ---
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

// --- TAB 4: MIS RÉCORDS ---
@Composable
fun PantallaRecordsTab() {
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    val records = remember { mutableStateListOf<Map<String, Any>>() }

    LaunchedEffect(Unit) {
        db.collection("mis_records").orderBy("timestamp").addSnapshotListener { snap, _ ->
            records.clear()
            snap?.documents?.forEach { doc -> records.add(doc.data ?: emptyMap()) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mis Capturas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        records.forEach { record ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(record["nombre"].toString(), fontWeight = FontWeight.Bold)
                        Text(record["estatus"].toString(), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun RelojMareasCircular(valor: Float) {
    val valorAnimado by animateFloatAsState(targetValue = valor, animationSpec = tween(durationMillis = 1000))

    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10f
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Arco de fondo
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFF44336),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            drawCircle(Color(0xFF4CAF50), radius = 6f, center = Offset(center.x, center.y - radius))
            drawCircle(Color(0xFFF44336), radius = 6f, center = Offset(center.x, center.y + radius))

            val angle = -90f + (valorAnimado * 360f)
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
fun MapaPescapr() {
    val context = LocalContext.current
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    val storage = remember { FirebaseStorage.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        hasLocationPermission = p[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) { if (!hasLocationPermission) launcher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)) }

    val misPuntos = remember { mutableStateListOf<PuntoPesca>() }
    var spotSeleccionado by remember { mutableStateOf<PuntoPesca?>(null) }
    var mostrarSheet by remember { mutableStateOf(false) }

    // Clima
    var datosClima by remember { mutableStateOf<WeatherResponse?>(null) }
    var cargandoClima by remember { mutableStateOf(false) }
    
    val weatherService = remember {
        Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherService::class.java)
    }

    LaunchedEffect(spotSeleccionado) {
        spotSeleccionado?.let { spot ->
            cargandoClima = true
            try {
                datosClima = weatherService.getWeather(
                    spot.coordenada.latitude, 
                    spot.coordenada.longitude, 
                    BuildConfig.OPENWEATHER_API_KEY
                )
            } catch (e: Exception) {
                e.printStackTrace()
                datosClima = null
            } finally {
                cargandoClima = false
            }
        }
    }

    DisposableEffect(Unit) {
        val l = db.collection("spots").addSnapshotListener { snap, _ ->
            misPuntos.clear()
            snap?.documents?.mapNotNull { doc ->
                val urls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                PuntoPesca(doc.id, LatLng(doc.getDouble("latitud") ?: 0.0, doc.getDouble("longitud") ?: 0.0), doc.getString("nombre") ?: "", doc.getString("descripcion") ?: "", urls)
            }?.let { misPuntos.addAll(it) }
        }
        onDispose { l?.remove() }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(18.2208, -66.5901), 9f) },
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission, mapType = MapType.SATELLITE)
    ) {
        misPuntos.forEach { spot ->
            Marker(
                state = MarkerState(position = spot.coordenada), 
                title = spot.nombre, 
                onClick = { spotSeleccionado = spot; mostrarSheet = true; true },
                icon = remember(context) {
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
            )
        }
    }

    if (mostrarSheet) {
        ModalBottomSheet(onDismissRequest = { mostrarSheet = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(spotSeleccionado?.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(spotSeleccionado?.descripcion ?: "", color = Color.Gray)
                
                Spacer(Modifier.height(16.dp))

                if (cargandoClima) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text("Cargando clima...", style = MaterialTheme.typography.labelSmall)
                } else if (datosClima != null) {
                    val clima = datosClima!!
                    val pressureInHg = clima.main.pressure * 0.02953
                    val pressureFormatted = String.format(Locale.US, "%.2f", pressureInHg)
                    
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
                            RelojMareasCircular(valor = 0.7f)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("MAREA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text("Subiendo", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.1f))) {
                        Text("Información de clima no disponible", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                val fotos = spotSeleccionado?.fotosUrls ?: emptyList()
                if (fotos.isNotEmpty()) {
                    Text("Fotos del Spot", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(Modifier.height(8.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(fotos) { url ->
                            AsyncImage(model = url, contentDescription = null, modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        }
                    }
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

package com.bradmir.pescapr.ui

import android.content.Context
import com.bradmir.pescapr.data.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bradmir.pescapr.AppDatabase
import com.bradmir.pescapr.BuildConfig
import com.bradmir.pescapr.FichaPez
import com.bradmir.pescapr.R
import com.bradmir.pescapr.RecordEntity
import com.bradmir.pescapr.RecordPesca
import com.bradmir.pescapr.SpotEntity
import com.bradmir.pescapr.NoaaTideService
import com.bradmir.pescapr.RelojMareasCircular
import com.bradmir.pescapr.WeatherService
import com.bradmir.pescapr.calculateTideFactor
import com.bradmir.pescapr.findNearestTideStation
import com.bradmir.pescapr.saveImageToInternalStorage
import com.bradmir.pescapr.data.CatchRepository
import com.bradmir.pescapr.data.ProSwellMetrics
import com.bradmir.pescapr.data.PuntoPesca
import com.bradmir.pescapr.data.SpotRepository
import com.bradmir.pescapr.data.SubscriptionManager
import com.bradmir.pescapr.data.WeatherResponse
import com.bradmir.pescapr.network.MarineWeatherService
import com.bradmir.pescapr.ui.components.GoldenDayBanner
import com.bradmir.pescapr.ui.components.GoldenDayPlannerCard
import com.bradmir.pescapr.ui.components.GoldenDayPlannerSheet
import com.bradmir.pescapr.ui.components.PaywallDialog
import com.bradmir.pescapr.ui.components.ProFeatureActionButtons
import com.bradmir.pescapr.ui.components.ProFeaturePaywallDialog
import com.bradmir.pescapr.ui.components.ProFeatureType
import com.bradmir.pescapr.ui.components.ProSwellCard
import com.bradmir.pescapr.ui.components.WaterTempCard
import com.bradmir.pescapr.ui.identificador.ejecutarMatchingConFichas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import com.bradmir.pescapr.ui.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.UUID

fun shareSpotToCommunity(viewModel: MapViewModel, spot: PuntoPesca, coroutineScope: kotlinx.coroutines.CoroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)) {
    val uid = spot.userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    coroutineScope.launch {
        viewModel.shareSpotToCommunity(spot.copy(userId = uid))
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
    onFocoLogrado: () -> Unit = {},
    showMorphologyLayer: Boolean = false,
    onToggleMorphology: () -> Unit = {},
    onTriggerPaywall: (ProFeatureType) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance("pescapr") }
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
  val isOffline by viewModel.isOffline.collectAsState()

  Log.d("MapStateCircuit", "UI Recomposed - isPro: $isPro")



    val spotDao = remember { database.spotDao() }
    val recordDao = remember { database.recordDao() }
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        hasLocationPermission = p[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) { if (!hasLocationPermission) launcher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)) }

    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(18.2208, -66.5901), 9f) }

    val misPuntos = remember { mutableStateListOf<PuntoPesca>() }
    var verPinesComunidad by remember { mutableStateOf(false) }
    var spotSeleccionado by remember { mutableStateOf<PuntoPesca?>(null) }
    var mostrarSheet by remember { mutableStateOf(false) }
    val morphologyEnabled = isPro && showMorphologyLayer
    val morphologyData = rememberCoastalMorphologyData(morphologyEnabled)
    var selectedMorphologyFeature by remember { mutableStateOf<MorphologyFeatureMetadata?>(null) }

    LaunchedEffect(morphologyEnabled) {
        if (!morphologyEnabled) selectedMorphologyFeature = null
    }
    LaunchedEffect(morphologyData) {
        selectedMorphologyFeature = null
    }

    var customPinIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                com.google.android.gms.maps.MapsInitializer.initialize(context.applicationContext)
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pin_pescapr)
                if (bitmap != null) {
                    val scaled = bitmap.scale(100, 100, true)
                    val descriptor = BitmapDescriptorFactory.fromBitmap(scaled)
                    withContext(Dispatchers.Main) {
                        customPinIcon = descriptor
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    // Clima & Mareas
    var datosClima by remember { mutableStateOf<WeatherResponse?>(null) }
    var cargandoClima by remember { mutableStateOf(false) }
    var tideFactor by remember { mutableFloatStateOf(0.5f) }
    var tideDescription by remember { mutableStateOf("Cargando...") }
    var nextTideTime by remember { mutableStateOf("") }

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

            fichasGuia.clear()
            fichasGuia.addAll(listaFichas)
        }
    }

    val spotPhotoRepository = remember { SpotPhotoRepository() }
    var userPendingSubmission by remember { mutableStateOf<SpotPhotoSubmission?>(null) }

    val photoPickerLauncherSpot = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && spotSeleccionado != null) {
            subiendoFotoSpot = true
            coroutineScope.launch {
                val currentUid = userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

                var targetFirestoreId = spotSeleccionado!!.firestoreId
                if (targetFirestoreId.isBlank()) {
                    val spotToSync = spotSeleccionado!!.copy(userId = currentUid)
                    val syncedId = viewModel.shareSpotToCommunity(spotToSync)
                    if (!syncedId.isNullOrBlank()) {
                        targetFirestoreId = syncedId
                        val localIntId = spotSeleccionado!!.id.toIntOrNull()
                        if (localIntId != null) {
                            val existingEntity = spotDao.getSpotById(localIntId)
                            if (existingEntity != null) {
                                spotDao.updateSpot(existingEntity.copy(firestoreId = syncedId))
                            }
                        }
                        spotSeleccionado = spotSeleccionado!!.copy(firestoreId = syncedId)
                    }
                }

                if (targetFirestoreId.isBlank()) {
                    subiendoFotoSpot = false
                    Toast.makeText(context, "No se pudo vincular el spot con la comunidad en Firestore.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val result = spotPhotoRepository.submitSpotPhoto(
                    context = context,
                    spotId = targetFirestoreId,
                    imageUri = uri,
                    userId = currentUid
                )
                subiendoFotoSpot = false
                result.fold(
                    onSuccess = {
                        Toast.makeText(context, "Foto propuesta enviada a revisión", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            userPendingSubmission = spotPhotoRepository.getUserPendingSubmissionForSpot(targetFirestoreId, currentUid)
                        }
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Error al enviar propuesta: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    LaunchedEffect(spotSeleccionado?.id, spotSeleccionado?.firestoreId, userId) {
        val currentUid = userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
        val targetId = spotSeleccionado?.firestoreId?.ifBlank { null }
        if (targetId != null && currentUid.isNotBlank()) {
            userPendingSubmission = spotPhotoRepository.getUserPendingSubmissionForSpot(targetId, currentUid)
            
            // Refresh approvedPhotos from Firestore for this specific spot
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val doc = db.collection("spots").document(targetId).get().await()
                    if (doc.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val rawApproved = doc.get("approvedPhotos") as? List<Map<String, Any>> ?: emptyList()
                        val approvedList = rawApproved.map { map ->
                            ApprovedSpotPhoto(
                                photoId = map["photoId"] as? String ?: "",
                                downloadUrl = map["downloadUrl"] as? String ?: "",
                                displayOrder = (map["displayOrder"] as? Long)?.toInt() ?: 0
                            )
                        }
                        
                        // Update local Room entity if it exists
                        val spotIdInt = spotSeleccionado?.id?.toIntOrNull()
                        if (spotIdInt != null) {
                            val existing = spotDao.getSpotById(spotIdInt)
                            if (existing != null) {
                                spotDao.updateSpot(existing.copy(approvedPhotos = approvedList))
                            }
                        }
                        
                        // If spotSeleccionado is still the same, update its state
                        withContext(Dispatchers.Main) {
                            if (spotSeleccionado?.firestoreId == targetId) {
                                spotSeleccionado = spotSeleccionado?.copy(approvedPhotos = approvedList)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MapaPescapr", "Error refreshing spot approved photos", e)
                }
            }
        } else {
            userPendingSubmission = null
        }
    }

    LaunchedEffect(spotSeleccionado) {
        if (spotSeleccionado != null) {
            val spotId = spotSeleccionado!!.id
            val spotIntId = spotId.toIntOrNull() ?: 0
            cargandoCapturas = true
            cargandoClima = true
            capturasSpot.clear()

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    recordDao.getRecordsBySpot(spotIntId).collect { localEntities ->
                        val localList = localEntities.map { entity ->
                            RecordPesca(
                                id = entity.id.toString(),
                                spotId = entity.spotId.toString(),
                                nombrePez = entity.nombrePez,
                                peso = entity.peso,
                                longitud = entity.longitud,
                                lugar = entity.lugar,
                                fecha = entity.fecha,
                                fotosUrls = entity.fotosUrls,
                                climaTemp = entity.climaTemp,
                                climaWind = entity.climaWind,
                                climaPressure = entity.climaPressure,
                                climaTide = entity.climaTide
                            )
                        }

                        withContext(Dispatchers.Main) {
                            capturasSpot.clear()
                            capturasSpot.addAll(localList)
                            cargandoCapturas = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) { cargandoCapturas = false }
                }
            }

            coroutineScope.launch(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.OPENWEATHER_API_KEY
                if (apiKey.isNotBlank()) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.openweathermap.org/data/2.5/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val service = retrofit.create(WeatherService::class.java)
                    val response = service.getWeather(
                        spotSeleccionado!!.latitude,
                        spotSeleccionado!!.longitude,
                        apiKey
                    )
                    withContext(Dispatchers.Main) {
                        datosClima = response
                        cargandoClima = false
                    }
                } else {
                    withContext(Dispatchers.Main) { cargandoClima = false }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { cargandoClima = false }
            }
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tideRetrofit = Retrofit.Builder()
                    .baseUrl("https://api.tidesandcurrents.noaa.gov/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val tideService = tideRetrofit.create(NoaaTideService::class.java)
                val station = findNearestTideStation(
                    spotSeleccionado!!.latitude,
                    spotSeleccionado!!.longitude
                )
                val response = tideService.getTidePredictions(station = station.id, date = "today")
                response.predictions?.let { preds ->
                    val (factor, desc, time) = calculateTideFactor(preds)
                    withContext(Dispatchers.Main) {
                        tideFactor = factor
                        tideDescription = desc
                        nextTideTime = time
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        tideDescription = "Sin predicciones"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tideDescription = "Error de mareas"
                }
            }
        }
        }
    }

    LaunchedEffect(Unit) {
        spotDao.getAllSpots().collect { entities ->
            val listaLocal = entities.map {
                PuntoPesca(
                    id = it.id.toString(),
                    firestoreId = it.firestoreId,
                    userId = it.userId,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    latitude = it.latitud,
                    longitude = it.longitud,
                    fotosUrls = it.fotosUrls,
                    approvedPhotos = it.approvedPhotos
                )
            }
            misPuntos.clear()
            misPuntos.addAll(listaLocal)
        }
    }

    LaunchedEffect(Unit) {
        if (userId.isNotBlank()) {
            try {
                val snap = db.collection("spots")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val listaRemota = snap.documents.map { doc ->
                    val fotos = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val rawApproved = doc.get("approvedPhotos") as? List<Map<String, Any>> ?: emptyList()
                    val approvedList = rawApproved.map { map ->
                        ApprovedSpotPhoto(
                            photoId = map["photoId"] as? String ?: "",
                            downloadUrl = map["downloadUrl"] as? String ?: "",
                            displayOrder = (map["displayOrder"] as? Long)?.toInt() ?: 0
                        )
                    }

                    PuntoPesca(
                        id = doc.id,
                        firestoreId = doc.id,
                        userId = doc.getString("userId") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        latitude = doc.getDouble("lat") ?: 0.0,
                        longitude = doc.getDouble("lng") ?: 0.0,
                        fotosUrls = fotos,
                        approvedPhotos = approvedList
                    )
                }

                listaRemota.forEach { spot ->
                    spotDao.upsertFirestoreSpot(
                        SpotEntity(
                            nombre = spot.nombre,
                            descripcion = spot.descripcion,
                            latitud = spot.latitude,
                            longitud = spot.longitude,
                            fotosUrls = spot.fotosUrls,
                            userId = spot.userId,
                            firestoreId = spot.firestoreId,
                            approvedPhotos = spot.approvedPhotos
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(spotIdAFocar, misPuntos.size, pinesComunidad.size) {
        if (spotIdAFocar != null) {
            val todos = misPuntos + pinesComunidad
            val spot = todos.find { it.id == spotIdAFocar }
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
        },
        onMapClick = { latLng ->
            if (morphologyEnabled) {
                selectedMorphologyFeature = findMorphologyFeatureAt(latLng, morphologyData)
                if (selectedMorphologyFeature != null) {
                    mostrarSheet = false
                    spotSeleccionado = null
                }
            } else {
                selectedMorphologyFeature = null
            }
        }
    ) {
        CoastalMorphologyLayerContent(enabled = morphologyEnabled, data = morphologyData)

    val listaAMostrar = if (verPinesComunidad) pinesComunidad else misPuntos
    listaAMostrar.forEach { spot ->
      key(spot.id) {
        val markerState = rememberMarkerState(key = spot.id, position = spot.coordenada)
        Marker(
          state = markerState,
          title = spot.nombre,
          snippet = spot.descripcion.ifBlank { "Toca para ver detalles" },
          onClick = {
            selectedMorphologyFeature = null
            false
          },
          onInfoWindowClick = {
            selectedMorphologyFeature = null
            spotSeleccionado = spot
            mostrarSheet = true
          },
          icon = if (verPinesComunidad) {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
          } else {
            customPinIcon
          }
        )
      }
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
                    onToggleMorphology()
                } else {
                    onTriggerPaywall(ProFeatureType.MORFOLOGIA)
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
}

    if (mostrarDialogoNuevoPunto && nuevaCoordenada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevoPunto = false },
            title = { Text("Nuevo Spot de Pesca") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNuevoPunto,
                        onValueChange = { nombreNuevoPunto = it },
                        label = { Text("Nombre del Spot") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcionNuevoPunto,
                        onValueChange = { descripcionNuevoPunto = it },
                        label = { Text("Notas / Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
            Button(
                onClick = {
                    if (nombreNuevoPunto.isNotBlank() && !guardandoPunto && nuevaCoordenada != null) {
                        guardandoPunto = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val currentUid = userId.ifBlank {
                            FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        }
                        val coords = nuevaCoordenada!!
                        var nuevo = PuntoPesca(
                            id = UUID.randomUUID().toString(),
                            userId = currentUid,
                            nombre = nombreNuevoPunto,
                            descripcion = descripcionNuevoPunto,
                            latitude = coords.latitude,
                            longitude = coords.longitude
                        )

                        // Immediately synchronize to Community to establish canonical firestoreId
                        val generatedFirestoreId = viewModel.shareSpotToCommunity(nuevo) ?: ""
                        if (generatedFirestoreId.isNotBlank()) {
                            nuevo = nuevo.copy(firestoreId = generatedFirestoreId)
                        }

                        spotDao.insertSpot(
                            SpotEntity(
                                nombre = nuevo.nombre,
                                descripcion = nuevo.descripcion,
                                latitud = nuevo.latitude,
                                longitud = nuevo.longitude,
                                fotosUrls = emptyList(),
                                userId = currentUid,
                                firestoreId = generatedFirestoreId,
                                approvedPhotos = emptyList()
                            )
                        )

                                withContext(Dispatchers.Main) {
                                    misPuntos.add(nuevo)
                                    Toast.makeText(context, "Spot guardado localmente", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    guardandoPunto = false
                                    mostrarDialogoNuevoPunto = false
                                }
                            }
                        }
                    }
                },
                enabled = !guardandoPunto && nombreNuevoPunto.isNotBlank()
            ) {
                if (guardandoPunto) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Guardar Spot")
            }
        },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevoPunto = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarSheet && spotSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = { mostrarSheet = false }
        ) {
            val spotActual = misPuntos.find { it.id == spotSeleccionado?.id } ?: spotSeleccionado
            val fotos = spotActual?.displayPhotoUrls ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = spotActual?.nombre ?: "Spot de Pesca",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (spotActual?.descripcion?.isNotBlank() == true) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = spotActual.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Clima & Mareas
                if (cargandoClima) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cargando clima y mareas...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (datosClima != null) {
                    val clima = datosClima!!
                    val pressureInHg = clima.main.pressure * 0.02953
                    val pressureFormatted = String.format(Locale.US, "%.2f", pressureInHg)

                    Text(
                        text = "Signos Vitales del Spot",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            WeatherInfoItem(
                                icon = Icons.Default.Thermostat,
                                value = "${clima.main.temp.toInt()}°F",
                                label = "Temperatura"
                            )
                            WeatherInfoItem(
                                icon = Icons.Default.Air,
                                value = "${clima.wind.speed.toInt()} mph",
                                label = "Viento"
                            )
                            WeatherInfoItem(
                                icon = Icons.Default.Speed,
                                value = "$pressureFormatted inHg",
                                label = "Presión"
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
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
                }

                // Water Temperature Card
                var trendResult by remember { mutableStateOf<com.bradmir.pescapr.utils.ThermalTrendResult?>(null) }
                var lastTemp by remember { mutableStateOf<Float?>(null) }
                var activePaywallFeature by remember { mutableStateOf<ProFeatureType?>(null) }
                var expandedProFeature by remember { mutableStateOf<ProFeatureType?>(null) }
                var mostrar30DayPlannerSheet by remember { mutableStateOf(false) }
                var goldenTide30DayList by remember { mutableStateOf<List<com.bradmir.pescapr.data.GoldenDayPrediction>>(emptyList()) }

                LaunchedEffect(spotActual) {
                    if (spotActual != null && isPro) {
                        try {
                            val service = Retrofit.Builder()
                                .baseUrl("https://marine-api.open-meteo.com/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                                .create(MarineWeatherService::class.java)

                            val response = service.getSwellData(
                                lat = spotActual.latitude,
                                lon = spotActual.longitude,
                                hourly = "temperature_2m",
                                lengthUnit = "imperial",
                                timezone = "America/Puerto_Rico",
                                forecastDays = 7
                            )

                            val temps = response.hourly?.waveHeight?.filterNotNull() ?: emptyList()
                            if (temps.isNotEmpty()) {
                                lastTemp = temps.lastOrNull()
                                trendResult = com.bradmir.pescapr.utils.ThermalTrendUtils.calculate7DayThermalTrend(temps)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                ProFeatureActionButtons(
                    selectedFeature = if (isPro) expandedProFeature else null,
                    onFeatureClick = { feature ->
                        if (isPro) {
                            if (feature == ProFeatureType.PLANIFICADOR) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val list = com.bradmir.pescapr.data.generate30DayGoldenTideWindows(
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
                                currentWaterTemp = lastTemp,
                                trendResult = trendResult,
                                ambientAirTempF = datosClima?.main?.temp?.toFloat(),
                                onUpgradeClick = { mostrarPaywallDialog = true }
                            )
                        }
                        ProFeatureType.MAREJADAS -> {
                            ProSwellCardContainer(
                                isPro = true,
                                lat = spotActual?.latitude ?: 0.0,
                                lon = spotActual?.longitude ?: 0.0,
                                onUpgradeClick = { mostrarPaywallDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fotos del Spot (${fotos.size}/4)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (userPendingSubmission != null) {
                            Text(
                                text = "Tu foto está en revisión. Una vez aprobada, se verá aquí.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                softWrap = true
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (subiendoFotoSpot) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else if (userPendingSubmission != null) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = spotPhotoRepository.withdrawPendingSubmission(userPendingSubmission!!)
                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(context, "Propuesta retirada", Toast.LENGTH_SHORT).show()
                                                userPendingSubmission = null
                                            },
                                            onFailure = { err ->
                                                Toast.makeText(context, "Error al cancelar propuesta: ${err.message}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.widthIn(min = 96.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else if (fotos.size < 4) {
                            TextButton(
                                onClick = { photoPickerLauncherSpot.launch("image/*") }
                            ) {
                                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Proponer Foto", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                if (fotos.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fotos) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { fotoAmpliadaUrl = url },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capturas en esta ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = { mostrarDialogoCaptura = true }) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { verDetallesCaptura = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (record.fotosUrls.isNotEmpty()) {
                                        AsyncImage(
                                            model = record.fotosUrls.firstOrNull(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Image, null, tint = Color.Gray)
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(record.nombrePez, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${record.peso} | ${record.longitud}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(record.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }

                            if (verDetallesCaptura) {
                                androidx.compose.ui.window.Dialog(onDismissRequest = { verDetallesCaptura = false }) {
                                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.Start) {
                                            Text("Detalles de la Captura", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(16.dp))

                                            if (record.fotosUrls.isNotEmpty()) {
                                                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(150.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                            Text("Temperatura: ${record.climaTemp.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                                            Text("Viento: ${record.climaWind.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                                            Text("Presión: ${record.climaPressure.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                                            Text("Marea: ${record.climaTide.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)

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

    selectedMorphologyFeature?.let { feature ->
        ModalBottomSheet(
            onDismissRequest = { selectedMorphologyFeature = null }
        ) {
            MorphologyMetadataSheet(feature)
        }
    }

    if (fotoAmpliadaUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { fotoAmpliadaUrl = null }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fotoAmpliadaUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fotoAmpliadaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun MorphologyMetadataSheet(feature: MorphologyFeatureMetadata) {
    val rows = buildList {
        feature.structureType?.let { add("Tipo de estructura" to morphologyLabel(it)) }
        feature.bottomType?.let { add("Tipo de fondo" to morphologyLabel(it)) }
        feature.targetSpecies?.let { add("Especies objetivo" to it) }
        feature.bestTide?.let { add("Mejor marea" to morphologyLabel(it)) }
        (feature.fishingStrategyEs ?: feature.fishingStrategyEn)?.let { add("Estrategia" to it) }
        (feature.hazardsEs ?: feature.hazardsEn)?.let { add("Peligros" to it) }
        feature.notes?.let { add("Notas" to it) }
        feature.geometrySource?.let { add("Fuente de geometría" to morphologyLabel(it)) }
        feature.fishingSource?.let { add("Fuente de pesca" to morphologyLabel(it)) }
        feature.geometryConfidence?.let { add("Confianza de geometría" to morphologyLabel(it)) }
        feature.fishingConfidence?.let { add("Confianza de pesca" to morphologyLabel(it)) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = feature.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        feature.nameEn
            ?.takeIf { it != feature.displayName }
            ?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        if (rows.isEmpty()) {
            Text(
                text = "No hay detalles adicionales disponibles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rows.forEach { (label, value) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun morphologyLabel(value: String): String = value
    .lowercase(Locale.ROOT)
    .replace('_', ' ')
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

@Composable
fun WeatherInfoItem(
    icon: ImageVector,
    value: String,
    label: String,
    tintOverride: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tintOverride ?: MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tintOverride ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProSwellCardContainer(
    isPro: Boolean,
    lat: Double,
    lon: Double,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swellMetrics by remember { mutableStateOf<ProSwellMetrics?>(null) }

    LaunchedEffect(lat, lon, isPro) {
        if (isPro) {
            try {
                val service = Retrofit.Builder()
                    .baseUrl("https://marine-api.open-meteo.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MarineWeatherService::class.java)

                val response = service.getSwellData(
                    lat = lat,
                    lon = lon,
                    hourly = "wave_height,wave_period,wave_direction",
                    lengthUnit = "imperial",
                    timezone = "America/Puerto_Rico",
                    forecastDays = 1
                )

                val heights = response.hourly?.waveHeight?.filterNotNull() ?: emptyList()
                val periods = response.hourly?.wavePeriod?.filterNotNull() ?: emptyList()
                val dirs = response.hourly?.waveDirection?.filterNotNull() ?: emptyList()

                if (heights.isNotEmpty() && periods.isNotEmpty() && dirs.isNotEmpty()) {
                    swellMetrics = ProSwellMetrics(
                        heightFt = heights.firstOrNull() ?: 0f,
                        periodSec = periods.firstOrNull() ?: 0f,
                        directionDeg = dirs.firstOrNull() ?: 0f,
                        score = 0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(modifier = modifier) {
        GoldenDayBanner(swellMetrics = swellMetrics)
        Spacer(modifier = Modifier.height(8.dp))
        ProSwellCard(metrics = swellMetrics)
    }
}

/**
 * Composable for rendering cached or bundled GeoJSON coastal morphology using Maps Compose.
 */
@Composable
private fun rememberCoastalMorphologyData(enabled: Boolean): MorphologyParsedData {
    val context = LocalContext.current
    val repository = remember(context.applicationContext) {
        CoastalMorphologyRepository(context.applicationContext)
    }
    val repositoryState by repository.state.collectAsState()

    LaunchedEffect(enabled, repository) {
        if (enabled) repository.loadAndRefresh()
    }
    return if (enabled) repositoryState.data else MorphologyParsedData()
}

@Composable
fun CoastalMorphologyLayer(enabled: Boolean) {
    val data = rememberCoastalMorphologyData(enabled)
    CoastalMorphologyLayerContent(enabled, data)
}

@Composable
private fun CoastalMorphologyLayerContent(enabled: Boolean, data: MorphologyParsedData) {
    if (!enabled) return

    data.polygons.forEach { poly ->
        key(poly.id) {
            Polygon(
                points = poly.outerBoundary,
                holes = poly.holes,
                fillColor = androidx.compose.ui.graphics.Color(0x3C00FFFF), // Cyan (60 alpha)
                strokeColor = androidx.compose.ui.graphics.Color(0xFF00FFFF), // Cyan
                strokeWidth = 3f,
                clickable = false
            )
        }
    }

    data.lines.forEach { line ->
        key(line.id) {
            Polyline(
                points = line.points,
                color = androidx.compose.ui.graphics.Color(0xFF00FFFF), // Cyan
                width = 6f,
                clickable = false
            )
        }
    }
}

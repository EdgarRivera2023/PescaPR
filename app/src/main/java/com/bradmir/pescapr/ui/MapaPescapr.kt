package com.bradmir.pescapr.ui

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
import com.bradmir.pescapr.WeatherService
import com.bradmir.pescapr.data.CatchRepository
import com.bradmir.pescapr.data.ProSwellMetrics
import com.bradmir.pescapr.data.PuntoPesca
import com.bradmir.pescapr.data.SpotRepository
import com.bradmir.pescapr.data.SubscriptionManager
import com.bradmir.pescapr.data.WeatherResponse
import com.bradmir.pescapr.network.MarineWeatherService
import com.bradmir.pescapr.ui.components.PaywallDialog
import com.bradmir.pescapr.ui.components.ProSwellCard
import com.bradmir.pescapr.ui.components.WaterTempCard
import com.bradmir.pescapr.ui.viewmodels.MapViewModel
import com.bradmir.pescapr.utils.CachedTileProvider
import com.bradmir.pescapr.utils.TileCacheManager
import com.google.android.gms.maps.CameraUpdateFactory
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

fun shareSpotToCommunity(viewModel: MapViewModel, spot: PuntoPesca) {
    val uid = spot.userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    viewModel.shareSpotToCommunity(spot.copy(userId = uid))
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
  val isMorphologyLayerEnabled by viewModel.isMorphologyLayerEnabled.collectAsState()

  Log.d("MapStateCircuit", "UI Recomposed - isPro: $isPro")

  val tileCacheManager = remember(context) { TileCacheManager(context.applicationContext) }
  val bathymetryTileProvider = remember(isPro, tileCacheManager) {
    if (!isPro) null
    else CachedTileProvider(tileCacheManager = tileCacheManager, minZoom = 1)
  }

  val coastalMorphologyStyle = remember(context) {
    try {
      MapStyleOptions.loadRawResourceStyle(context, R.raw.coastal_morphology_style)
    } catch (e: Exception) {
      Log.e("MapStyle", "Error loading coastal morphology style", e)
      null
    }
  }

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

    val photoPickerLauncherSpot = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && spotSeleccionado != null) {
            subiendoFotoSpot = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                    val photoRef = storageRef.child("spots/${spotSeleccionado!!.id}/${UUID.randomUUID()}.jpg")

                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val baos = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val data = baos.toByteArray()

                    photoRef.putBytes(data).await()
                    val downloadUrl = photoRef.downloadUrl.await().toString()

                    val targetSpot = misPuntos.find { it.id == spotSeleccionado?.id }
                    if (targetSpot != null) {
                        val fotosActuales = targetSpot.fotosUrls.toMutableList()
                        fotosActuales.add(downloadUrl)

                        db.collection("spots").document(targetSpot.id)
                            .update("fotosUrls", fotosActuales).await()

                        spotDao.insertSpot(
                            SpotEntity(
                                nombre = targetSpot.nombre,
                                descripcion = targetSpot.descripcion,
                                latitud = targetSpot.latitude,
                                longitud = targetSpot.longitude,
                                fotosUrls = fotosActuales,
                                userId = targetSpot.userId
                            )
                        )

                        withContext(Dispatchers.Main) {
                            val updatedSpot = targetSpot.copy(fotosUrls = fotosActuales)
                            val idx = misPuntos.indexOfFirst { it.id == targetSpot.id }
                            if (idx != -1) misPuntos[idx] = updatedSpot
                            spotSeleccionado = updatedSpot
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    withContext(Dispatchers.Main) { subiendoFotoSpot = false }
                }
            }
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
        }
    }

    LaunchedEffect(Unit) {
        spotDao.getAllSpots().collect { entities ->
            val listaLocal = entities.map {
                PuntoPesca(
                    id = it.id.toString(),
                    userId = it.userId,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    latitude = it.latitud,
                    longitude = it.longitud,
                    fotosUrls = it.fotosUrls
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
                    PuntoPesca(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        latitude = doc.getDouble("lat") ?: 0.0,
                        longitude = doc.getDouble("lng") ?: 0.0,
                        fotosUrls = fotos
                    )
                }

                listaRemota.forEach { spot ->
                    spotDao.insertSpot(
                        SpotEntity(
                            nombre = spot.nombre,
                            descripcion = spot.descripcion,
                            latitud = spot.latitude,
                            longitud = spot.longitude,
                            fotosUrls = spot.fotosUrls,
                            userId = spot.userId
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
        Text(
            text = "DEBUG - isPro: $isPro",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(100f)
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

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
      mapType = if (isPro) MapType.HYBRID else MapType.NORMAL,
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
        if (isPro && isMorphologyLayerEnabled && bathymetryTileProvider != null) {
      TileOverlay(
        tileProvider = bathymetryTileProvider,
        transparency = 0.25f,
        zIndex = 100f
      )
    }

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
              } else {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
              }
            } catch (e: Exception) {
              BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            }
          }
        }
      )
    }
  }

  Row(
    modifier = Modifier
      .align(Alignment.TopEnd)
      .padding(top = 16.dp, end = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    FilterChip(
      selected = isMorphologyLayerEnabled,
      onClick = {
        if (!viewModel.toggleMorphologyLayer(isPro)) {
          mostrarPaywallDialog = true
        }
      },
      label = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Morfología")
          if (!isPro) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
          }
        }
      }
    )

    FilterChip(
      selected = verPinesComunidad,
      onClick = {
        if (isPro) {
          verPinesComunidad = !verPinesComunidad
        } else {
          mostrarPaywallDialog = true
        }
      },
      label = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(if (verPinesComunidad) "Comunidad" else "Mis Spots")
          if (!isPro) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
          }
        }
      }
    )
  }

  ProSwellCardContainer(
            isPro = isPro,
            lat = cameraPositionState.position.target.latitude,
            lon = cameraPositionState.position.target.longitude,
            onUpgradeClick = { mostrarPaywallDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .zIndex(5f)
        )
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
                        if (nombreNuevoPunto.isNotBlank() && userId.isNotBlank()) {
                            guardandoPunto = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val idLocal = UUID.randomUUID().toString()
                                    val nuevo = PuntoPesca(
                                        id = idLocal,
                                        userId = userId,
                                        nombre = nombreNuevoPunto,
                                        descripcion = descripcionNuevoPunto,
                                        latitude = nuevaCoordenada!!.latitude,
                                        longitude = nuevaCoordenada!!.longitude
                                    )

                                    spotDao.insertSpot(
                                        SpotEntity(
                                            nombre = nuevo.nombre,
                                            descripcion = nuevo.descripcion,
                                            latitud = nuevo.latitude,
                                            longitud = nuevo.longitude,
                                            fotosUrls = emptyList(),
                                            userId = userId
                                        )
                                    )

                                    withContext(Dispatchers.Main) {
                                        misPuntos.add(nuevo)
                                    }

                                    db.collection("spots").document(idLocal).set(
                                        hashMapOf(
                                            "userId" to userId,
                                            "nombre" to nuevo.nombre,
                                            "descripcion" to nuevo.descripcion,
                                            "lat" to nuevo.latitude,
                                            "lng" to nuevo.longitude,
                                            "fotosUrls" to emptyList<String>()
                                        )
                                    ).await()
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
            val fotos = spotActual?.fotosUrls ?: emptyList()

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

                // Water Temperature Card
                var trendResult by remember { mutableStateOf<com.bradmir.pescapr.utils.ThermalTrendResult?>(null) }
                var lastTemp by remember { mutableStateOf<Float?>(null) }

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

                WaterTempCard(
                    isPro = isPro,
                    currentWaterTemp = lastTemp,
                    trendResult = trendResult,
                    onUpgradeClick = { mostrarPaywallDialog = true }
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fotos del Spot (${fotos.size}/4)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (fotos.size < 4 && spotActual?.userId == userId) {
                        IconButton(onClick = { photoPickerLauncherSpot.launch("image/*") }) {
                            if (subiendoFotoSpot) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(record.nombrePez, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${record.peso} | ${record.longitud}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(record.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    Box(modifier = modifier) {
        ProSwellCard(metrics = swellMetrics)
    }
}

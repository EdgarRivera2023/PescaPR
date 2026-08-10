package com.bradmir.pescapr

import com.bradmir.pescapr.data.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.URL
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.maps.android.compose.*
import com.bradmir.pescapr.data.*
import com.bradmir.pescapr.ui.about.AboutDialog
import com.bradmir.pescapr.ui.identificador.PantallaIdentificadorYRegulacionesTab
import com.bradmir.pescapr.ui.guia.PantallaGuiaOficialTab
import com.bradmir.pescapr.ui.records.PantallaRecordsTab
import com.bradmir.pescapr.ui.components.GoldenDayBanner
import com.bradmir.pescapr.ui.components.GoldenDayPlannerCard
import com.bradmir.pescapr.ui.components.PaywallDialog
import com.bradmir.pescapr.ui.components.ProFeaturePaywallDialog
import com.bradmir.pescapr.ui.components.ProFeatureType
import com.bradmir.pescapr.utils.CachedTileProvider
import com.bradmir.pescapr.utils.TileCacheManager
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

// --- 1. MODELOS DE DATOS ---

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

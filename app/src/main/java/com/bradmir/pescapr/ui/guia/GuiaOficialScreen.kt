package com.bradmir.pescapr.ui.guia

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bradmir.pescapr.BuildConfig
import com.bradmir.pescapr.FichaPez
import com.bradmir.pescapr.R
import com.bradmir.pescapr.data.OfficialGuideRepository
import com.bradmir.pescapr.data.SpotPhotoRepository
import com.bradmir.pescapr.data.SpotPhotoSubmission
import com.bradmir.pescapr.ui.viewmodels.OfficialGuideViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

@Composable
fun PantallaGuiaOficialTab() {
    val context = LocalContext.current
    val viewModel: OfficialGuideViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository = OfficialGuideRepository(context.applicationContext)
                return OfficialGuideViewModel(repository) as T
            }
        }
    )
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance("pescapr") }
    val storage = remember { FirebaseStorage.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    // --- SEGURIDAD: MODO DESARROLLADOR ---
    var esDeveloper by remember { mutableStateOf(false) }

    val fichas by viewModel.fichas.collectAsState()
    var mostrarDialogoNueva by remember { mutableStateOf(false) }
    var mostrarModeracionFotos by remember { mutableStateOf(false) }
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
    val filteredFichas = remember(searchQuery, fichas) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Guía Oficial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (BuildConfig.DEBUG) {
                    TextButton(onClick = { esDeveloper = !esDeveloper }) {
                        Text(if(esDeveloper) "Admin ON" else "Modo Vista")
                    }
                }

                if (esDeveloper) {
                    Button(onClick = { mostrarModeracionFotos = true }) {
                        Text("Moderación Fotos")
                    }

                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val jsonString = com.google.gson.Gson().toJson(fichas)
                            jsonString.chunked(3000).forEach { chunk ->
                                android.util.Log.d("PescaPR_Export", chunk)
                            }
                        }
                    }) {
                        Text("Export JSON")
                    }

                    IconButton(onClick = {
                        fichaParaEditar = null
                        mostrarDialogoNueva = true
                    }) {
                        Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
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
                        val resId = remember(ficha.localThumbResName) {
                            if (ficha.localThumbResName.isNotBlank()) {
                                context.resources.getIdentifier(ficha.localThumbResName, "drawable", context.packageName)
                            } else 0
                        }

                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (ficha.fotosUrls.firstOrNull()?.isNotBlank() == true) {
                            AsyncImage(
                                model = ficha.fotosUrls.firstOrNull(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

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

    if (mostrarModeracionFotos) {
        AdminPhotoModerationDialog(onDismiss = { mostrarModeracionFotos = false })
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
                                if (fichaParaEditar == null) {
                                    db.collection("fichas_peces").document().set(data).await()
                                } else {
                                    db.collection("fichas_peces")
                                        .document(fichaParaEditar!!.id)
                                        .set(data, SetOptions.merge())
                                        .await()
                                }
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
fun AdminPhotoModerationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val spotPhotoRepository = remember { SpotPhotoRepository() }
    val pendingSubmissions = remember { mutableStateListOf<SpotPhotoSubmission>() }
    var cargando by remember { mutableStateOf(true) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"

    LaunchedEffect(Unit) {
        cargando = true
        val list = spotPhotoRepository.getPendingSubmissionsForAdmin()
        pendingSubmissions.clear()
        pendingSubmissions.addAll(list)
        cargando = false
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Moderación de Fotos (${pendingSubmissions.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (pendingSubmissions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay fotos pendientes de revisión", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingSubmissions) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = item.downloadUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Spot: ${item.spotId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Usuario: ${item.submittedByUserId.take(8)}...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Column {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val res = spotPhotoRepository.approveSubmissionTransaction(item.spotId, item, currentUid)
                                                    res.fold(
                                                        onSuccess = {
                                                            Toast.makeText(context, "Foto aprobada exitosamente", Toast.LENGTH_SHORT).show()
                                                            pendingSubmissions.remove(item)
                                                        },
                                                        onFailure = { err ->
                                                            Toast.makeText(context, err.message ?: "Error al aprobar", Toast.LENGTH_LONG).show()
                                                        }
                                                    )
                                                }
                                            },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Aprobar", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val res = spotPhotoRepository.rejectSubmission(item, currentUid)
                                                    res.fold(
                                                        onSuccess = {
                                                            Toast.makeText(context, "Foto rechazada", Toast.LENGTH_SHORT).show()
                                                            pendingSubmissions.remove(item)
                                                        },
                                                        onFailure = { err ->
                                                            Toast.makeText(context, err.message ?: "Error al rechazar", Toast.LENGTH_LONG).show()
                                                        }
                                                    )
                                                }
                                            },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Rechazar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
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

package com.bradmir.pescapr.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SpotRepository(private val db: FirebaseFirestore) {

    private var cachedPins: List<PuntoPesca> = emptyList()

    suspend fun fetchCommunityPins(userId: String, isPro: Boolean): List<PuntoPesca> = withContext(Dispatchers.IO) {
        try {
            val collection = db.collection("spots")
            val query = if (isPro) {
                collection.limit(50)
            } else {
                collection.whereEqualTo("userId", userId)
            }

            val snapshot = query.get().await()
            val fetchedPins = snapshot.documents.mapNotNull { doc ->
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
                latitude = doc.getDouble("lat") ?: 0.0,
                longitude = doc.getDouble("lng") ?: 0.0,
                nombre = doc.getString("nombre") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                fotosUrls = (doc.get("fotosUrls") as? List<*>)?.map { it.toString() } ?: emptyList(),
                approvedPhotos = approvedList,
                userId = doc.getString("userId") ?: ""
            )
        }
        if (fetchedPins.isNotEmpty()) {
            cachedPins = fetchedPins
        }
        fetchedPins.ifEmpty { cachedPins }
    } catch (e: Exception) {
        e.printStackTrace()
        cachedPins
    }
}

suspend fun shareSpotToCommunity(spot: PuntoPesca): String? = withContext(Dispatchers.IO) {
    try {
        if (spot.firestoreId.isNotBlank()) {
            return@withContext spot.firestoreId
        }
        if (spot.userId.isBlank()) {
            android.util.Log.e("SpotRepository", "Cannot share spot to community: userId is empty for spot '${spot.nombre}'")
            return@withContext null
        }

        // --- ENHANCED DUP-PREVENTION & LEGACY SPOT MATCHING ---
        val userSpotDocs = db.collection("spots")
            .whereEqualTo("userId", spot.userId)
            .get()
            .await()

        val normalizedInputName = spot.nombre.trim().lowercase()
        val coordinateTolerance = 0.0005 // Approx. 50-55 meters

        val matchingCandidates = userSpotDocs.documents.filter { doc ->
            val docLat = doc.getDouble("lat") ?: 0.0
            val docLng = doc.getDouble("lng") ?: 0.0
            val latDiff = Math.abs(docLat - spot.latitude)
            val lngDiff = Math.abs(docLng - spot.longitude)
            val isCoordsClose = latDiff < coordinateTolerance && lngDiff < coordinateTolerance
            
            val docName = (doc.getString("nombre") ?: "").trim().lowercase()
            val isNameMatching = docName == normalizedInputName || normalizedInputName.isBlank() || docName.isBlank()

            isCoordsClose && isNameMatching
        }

        if (matchingCandidates.size == 1) {
            val matchedId = matchingCandidates.first().id
            android.util.Log.d("SpotRepository", "Found legacy Firestore spot match '$matchedId' for local spot '${spot.nombre}'")
            return@withContext matchedId
        } else if (matchingCandidates.size > 1) {
            android.util.Log.w("SpotRepository", "Ambiguous spot match: Found ${matchingCandidates.size} candidates for spot '${spot.nombre}' near (${spot.latitude}, ${spot.longitude}). Failing safely.")
            return@withContext null
        }

        val spotData = hashMapOf(
            "lat" to spot.latitude,
            "lng" to spot.longitude,
            "nombre" to spot.nombre,
            "descripcion" to spot.descripcion,
            "userId" to spot.userId
        )
        val docRef = db.collection("spots").add(spotData).await()
        android.util.Log.d("SpotRepository", "Created new Firestore spot document '${docRef.id}' for '${spot.nombre}'")
        return@withContext docRef.id
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}
}

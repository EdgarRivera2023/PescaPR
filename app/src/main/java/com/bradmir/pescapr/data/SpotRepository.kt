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
                PuntoPesca(
                    id = doc.id,
                    latitude = doc.getDouble("lat") ?: 0.0,
                    longitude = doc.getDouble("lng") ?: 0.0,
                    nombre = doc.getString("nombre") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    fotosUrls = emptyList(),
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

    suspend fun shareSpotToCommunity(spot: PuntoPesca) = withContext(Dispatchers.IO) {
        try {
            if (spot.userId.isBlank()) {
                android.util.Log.e("SpotRepository", "Cannot share spot to community: userId is empty for spot '${spot.nombre}'")
                return@withContext
            }
            val spotData = hashMapOf(
                "lat" to spot.latitude,
                "lng" to spot.longitude,
                "nombre" to spot.nombre,
                "descripcion" to spot.descripcion,
                "userId" to spot.userId
            )
            db.collection("spots").add(spotData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

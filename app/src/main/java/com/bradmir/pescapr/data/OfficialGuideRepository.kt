package com.bradmir.pescapr.data

import android.content.Context
import android.util.Log
import com.bradmir.pescapr.data.model.FichaPez
import com.bradmir.pescapr.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal data class OfficialGuideDocument(
    val id: String,
    val fields: Map<String, Any?>
)

internal fun mapOfficialGuideDocument(
    document: OfficialGuideDocument,
    bundledById: Map<String, FichaPez>
): FichaPez? {
    if (document.id.isBlank()) return null

    val scientificName = document.fields["nombreCientifico"] as? String ?: ""
    val commonName = document.fields["nombreComun"] as? String ?: ""
    if (scientificName.isBlank() && commonName.isBlank()) return null

    fun stringList(field: String): List<String> =
        (document.fields[field] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

    val bundled = bundledById[document.id]
    val firestoreThumb = document.fields["localThumbResName"] as? String

    return FichaPez(
        id = document.id,
        nombreCientifico = scientificName,
        nombreComun = commonName,
        nombreIngles = document.fields["nombreIngles"] as? String ?: "",
        regulacionComercial = document.fields["regulacionComercial"] as? String ?: "",
        regulacionRecreativa = document.fields["regulacionRecreativa"] as? String ?: "",
        caracteristicas = stringList("caracteristicas"),
        puedeSerConfundidoCon = document.fields["puedeSerConfundidoCon"] as? String ?: "",
        fotosUrls = stringList("fotosUrls"),
        localThumbResName = firestoreThumb?.takeIf { it.isNotBlank() }
            ?: bundled?.localThumbResName.orEmpty()
    )
}

internal class OfficialGuideSnapshotReducer(
    private val bundled: List<FichaPez>
) {
    private var lastUsableFirestore: List<FichaPez> = emptyList()

    fun onSnapshot(firestore: List<FichaPez>): List<FichaPez> {
        if (firestore.isNotEmpty()) {
            lastUsableFirestore = firestore
            return firestore
        }
        return lastUsableFirestore.ifEmpty { bundled }
    }

    fun onError(): List<FichaPez> = lastUsableFirestore.ifEmpty { bundled }
}

class OfficialGuideRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance("pescapr")
) {
    private val gson = Gson()
    private val bundledGuide: List<FichaPez> by lazy { loadBundledGuide() }

    fun observeOfficialGuide(): Flow<List<FichaPez>> = callbackFlow {
        val bundled = bundledGuide
        val bundledById = bundled.associateBy(FichaPez::id)
        val reducer = OfficialGuideSnapshotReducer(bundled)

        trySend(bundled)

        val listener = firestore.collection("fichas_peces").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing Firestore guide; retaining available guide", error)
                trySend(reducer.onError())
                return@addSnapshotListener
            }

            val firestoreGuide = snapshot?.documents.orEmpty()
                .mapNotNull { document ->
                    mapOfficialGuideDocument(
                        OfficialGuideDocument(document.id, document.data.orEmpty()),
                        bundledById
                    )
                }
                .sortedBy { it.nombreCientifico }

            trySend(reducer.onSnapshot(firestoreGuide))
        }

        awaitClose { listener.remove() }
    }

    private fun loadBundledGuide(): List<FichaPez> = try {
        val inputStream = context.resources.openRawResource(R.raw.oficial_guide)
        InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
            val type = object : TypeToken<List<FichaPez>>() {}.type
            gson.fromJson<List<FichaPez>>(reader, type).orEmpty()
                .sortedBy { it.nombreCientifico }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error reading bundled official guide", e)
        emptyList()
    }

    private companion object {
        const val TAG = "OfficialGuideRepo"
    }
}

package com.bradmir.pescapr.data

import com.bradmir.pescapr.RecordDao
import com.bradmir.pescapr.RecordEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class CatchRepository(
    private val recordDao: RecordDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val localRecords: Flow<List<RecordEntity>> = recordDao.getAllRecords()

    suspend fun saveCatch(record: RecordEntity, isPro: Boolean) {
        // 1. Save locally always
        recordDao.insertRecord(record)

        // 2. If Pro and Logged in, sync to Firestore
        val user = auth.currentUser
        if (isPro && user != null) {
            try {
                val catchLog = CatchLog(
                    userId = user.uid,
                    fishName = record.nombrePez,
                    weight = record.peso,
                    length = record.longitud,
                    spotName = record.lugar,
                    date = record.fecha,
                    timestamp = record.timestamp,
                    photosUrls = record.fotosUrls,
                    environmentalData = EnvironmentalMetadata(
                        temp = record.climaTemp,
                        pressure = record.climaPressure,
                        windSpeed = record.climaWind,
                        tidePhase = record.climaTide
                    ),
                    isPrivate = true
                )
                firestore.collection("users")
                    .document(user.uid)
                    .collection("private_logs")
                    .add(catchLog)
                    .await()
            } catch (e: Exception) {
                // Log error or handle retry logic
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteCatch(record: RecordEntity) {
        recordDao.deleteRecord(record)
        // Note: For a full sync, we'd need a reference ID to delete from Firestore too.
        // This is a simplified version for now.
    }
}

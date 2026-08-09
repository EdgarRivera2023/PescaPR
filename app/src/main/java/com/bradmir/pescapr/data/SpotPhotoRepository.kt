package com.bradmir.pescapr.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class SpotPhotoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("pescapr"),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private suspend fun ensureAnonymousUserId(providedUserId: String): String {
        if (providedUserId.isNotBlank()) return providedUserId

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        var currentUser = auth.currentUser
        if (currentUser == null) {
            try {
                currentUser = auth.signInAnonymously().await().user
            } catch (e: Exception) {
                Log.e("SpotPhotoRepository", "Failed on-demand anonymous sign in", e)
            }
        }
        return currentUser?.uid ?: ""
    }

    suspend fun submitSpotPhoto(
        context: Context,
        spotId: String,
        imageUri: Uri,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val activeUserId = ensureAnonymousUserId(userId)
        if (activeUserId.isBlank()) {
            return@withContext Result.failure(Exception("No se pudo enviar la foto en este momento. Inténtalo de nuevo."))
        }
        if (spotId.isBlank()) {
            return@withContext Result.failure(Exception("Identificador de spot no válido."))
        }

        try {
            // 1. Check pending submissions count for this spot
            val pendingDocs = db.collection("spots")
                .document(spotId)
                .collection("photo_submissions")
                .whereEqualTo("status", PhotoSubmissionStatus.PENDING.name)
                .get()
                .await()

            if (pendingDocs.size() >= 3) {
                return@withContext Result.failure(Exception("Este spot ya tiene el límite máximo de 3 propuestas pendientes en revisión."))
            }

            // 2. Compress image
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(Exception("No se pudo procesar la imagen seleccionada."))

            val baos = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val bytes = baos.toByteArray()

            // 3. Upload to Storage
            val photoId = UUID.randomUUID().toString()
            val storagePath = "spots/$spotId/submissions/$photoId.jpg"
            val photoRef = storage.reference.child(storagePath)

            photoRef.putBytes(bytes).await()
            val downloadUrl = photoRef.downloadUrl.await().toString()

            // 4. Create Firestore submission document
            val submission = SpotPhotoSubmission(
                photoId = photoId,
                spotId = spotId,
                storagePath = storagePath,
                downloadUrl = downloadUrl,
                submittedByUserId = activeUserId,
                submittedAt = System.currentTimeMillis(),
                status = PhotoSubmissionStatus.PENDING
            )

            try {
                db.collection("spots")
                    .document(spotId)
                    .collection("photo_submissions")
                    .document(photoId)
                    .set(
                        hashMapOf(
                            "photoId" to submission.photoId,
                            "spotId" to submission.spotId,
                            "storagePath" to submission.storagePath,
                            "downloadUrl" to submission.downloadUrl,
                            "submittedByUserId" to submission.submittedByUserId,
                            "submittedAt" to submission.submittedAt,
                            "status" to submission.status.name
                        )
                    )
                    .await()

                Result.success(Unit)
            } catch (firestoreError: Exception) {
                // Rollback Storage file if Firestore creation failed
                try {
                    photoRef.delete().await()
                } catch (e: Exception) {
                    Log.e("SpotPhotoRepository", "Failed to cleanup Storage file after Firestore error", e)
                }
                Result.failure(firestoreError)
            }
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error submitting spot photo", e)
            Result.failure(e)
        }
    }

    suspend fun getUserPendingSubmissionForSpot(spotId: String, userId: String): SpotPhotoSubmission? = withContext(Dispatchers.IO) {
        if (spotId.isBlank() || userId.isBlank()) return@withContext null
        try {
            val docs = db.collection("spots")
                .document(spotId)
                .collection("photo_submissions")
                .whereEqualTo("submittedByUserId", userId)
                .whereEqualTo("status", PhotoSubmissionStatus.PENDING.name)
                .get()
                .await()

            val doc = docs.documents.firstOrNull() ?: return@withContext null
            SpotPhotoSubmission(
                photoId = doc.getString("photoId") ?: doc.id,
                spotId = doc.getString("spotId") ?: spotId,
                storagePath = doc.getString("storagePath") ?: "",
                downloadUrl = doc.getString("downloadUrl") ?: "",
                submittedByUserId = doc.getString("submittedByUserId") ?: userId,
                submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
                status = PhotoSubmissionStatus.PENDING
            )
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error fetching user pending submission", e)
            null
        }
    }

    suspend fun withdrawPendingSubmission(submission: SpotPhotoSubmission): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (submission.spotId.isNotBlank() && submission.photoId.isNotBlank()) {
                db.collection("spots")
                    .document(submission.spotId)
                    .collection("photo_submissions")
                    .document(submission.photoId)
                    .delete()
                    .await()
            }
            if (submission.storagePath.isNotBlank()) {
                try {
                    storage.reference.child(submission.storagePath).delete().await()
                } catch (e: Exception) {
                    Log.e("SpotPhotoRepository", "Error deleting storage file for withdrawn submission", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error withdrawing pending submission", e)
            Result.failure(e)
        }
    }

    suspend fun getPendingSubmissionsForAdmin(): List<SpotPhotoSubmission> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = db.collectionGroup("photo_submissions")
                .whereEqualTo("status", PhotoSubmissionStatus.PENDING.name)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                SpotPhotoSubmission(
                    photoId = doc.getString("photoId") ?: doc.id,
                    spotId = doc.getString("spotId") ?: "",
                    storagePath = doc.getString("storagePath") ?: "",
                    downloadUrl = doc.getString("downloadUrl") ?: "",
                    submittedByUserId = doc.getString("submittedByUserId") ?: "",
                    submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
                    status = PhotoSubmissionStatus.PENDING
                )
            }
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error getting pending submissions for admin", e)
            emptyList()
        }
    }

    suspend fun approveSubmissionTransaction(
        spotId: String,
        submission: SpotPhotoSubmission,
        adminUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.runTransaction { transaction ->
                val spotRef = db.collection("spots").document(spotId)
                val submissionRef = spotRef.collection("photo_submissions").document(submission.photoId)

                val spotSnapshot = transaction.get(spotRef)
                if (!spotSnapshot.exists()) {
                    throw IllegalStateException("El spot ($spotId) no existe en Firestore. Esta propuesta es huérfana y debe ser rechazada.")
                }
                @Suppress("UNCHECKED_CAST")
                val rawApproved = spotSnapshot.get("approvedPhotos") as? List<Map<String, Any>> ?: emptyList()

                if (rawApproved.size >= 4) {
                    throw IllegalStateException("Este spot ya tiene 4 fotos aprobadas. Remueve una foto existente para aprobar una nueva.")
                }

                val newApprovedMap = hashMapOf(
                    "photoId" to submission.photoId,
                    "downloadUrl" to submission.downloadUrl,
                    "displayOrder" to rawApproved.size
                )
                val updatedApproved = rawApproved + newApprovedMap

                transaction.update(spotRef, "approvedPhotos", updatedApproved)
                transaction.update(
                    submissionRef,
                    mapOf(
                        "status" to PhotoSubmissionStatus.APPROVED.name,
                        "approvedByUserId" to adminUid,
                        "approvedAt" to System.currentTimeMillis()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error in approveSubmissionTransaction", e)
            Result.failure(e)
        }
    }

    suspend fun rejectSubmission(
        submission: SpotPhotoSubmission,
        adminUid: String,
        reason: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (submission.spotId.isNotBlank() && submission.photoId.isNotBlank()) {
                db.collection("spots")
                    .document(submission.spotId)
                    .collection("photo_submissions")
                    .document(submission.photoId)
                    .update(
                        mapOf(
                            "status" to PhotoSubmissionStatus.REJECTED.name,
                            "rejectedAt" to System.currentTimeMillis(),
                            "rejectionReason" to (reason ?: "")
                        )
                    )
                    .await()
            }

            if (submission.storagePath.isNotBlank()) {
                try {
                    storage.reference.child(submission.storagePath).delete().await()
                } catch (e: Exception) {
                    Log.e("SpotPhotoRepository", "Error deleting rejected photo storage file", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error rejecting submission", e)
            Result.failure(e)
        }
    }

    suspend fun removeApprovedPhoto(
        spotId: String,
        photoId: String,
        downloadUrl: String,
        storagePath: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.runTransaction { transaction ->
                val spotRef = db.collection("spots").document(spotId)
                val spotSnapshot = transaction.get(spotRef)
                @Suppress("UNCHECKED_CAST")
                val rawApproved = spotSnapshot.get("approvedPhotos") as? List<Map<String, Any>> ?: emptyList()

                val updatedApproved = rawApproved.filterNot {
                    (it["photoId"] as? String) == photoId || (it["downloadUrl"] as? String) == downloadUrl
                }

                transaction.update(spotRef, "approvedPhotos", updatedApproved)
            }.await()

            if (!storagePath.isNullOrBlank()) {
                try {
                    storage.reference.child(storagePath).delete().await()
                } catch (e: Exception) {
                    Log.e("SpotPhotoRepository", "Error deleting storage file for removed approved photo", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SpotPhotoRepository", "Error removing approved photo", e)
            Result.failure(e)
        }
    }
}

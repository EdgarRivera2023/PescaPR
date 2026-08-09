package com.bradmir.pescapr.data

enum class PhotoSubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class ApprovedSpotPhoto(
    val photoId: String = "",
    val downloadUrl: String = "",
    val displayOrder: Int = 0
)

data class SpotPhotoSubmission(
    val photoId: String = "",
    val spotId: String = "",
    val storagePath: String = "",
    val downloadUrl: String = "",
    val submittedByUserId: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: PhotoSubmissionStatus = PhotoSubmissionStatus.PENDING,
    val approvedByUserId: String? = null,
    val approvedAt: Long? = null,
    val rejectedAt: Long? = null,
    val rejectionReason: String? = null
)

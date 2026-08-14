package com.bradmir.pescapr.data.contribution

/** Backend-neutral contracts only. No collection or persistence implementation is attached. */
data class ContributionConsent(
    val consentId: String,
    val consentVersion: String,
    val acceptedAtEpochMillis: Long,
    val ownershipConfirmed: Boolean,
    val mlTrainingAllowed: Boolean,
    val publicDisplayAllowed: Boolean = false,
    val marketingAllowed: Boolean = false,
    val withdrawnAtEpochMillis: Long? = null
) {
    val isWithdrawn: Boolean get() = withdrawnAtEpochMillis != null
}

enum class ContributionStatus {
    DRAFT,
    SUBMITTED,
    RIGHTS_REVIEW,
    PRIVACY_REVIEW,
    LABEL_REVIEW,
    QUALITY_REVIEW,
    SPECIALIST_PENDING,
    DATASET_REVIEW,
    APPROVED,
    REJECTED,
    WITHDRAWAL_PENDING,
    WITHDRAWN,
    EXCLUDED,
    ARCHIVED
}

enum class ReviewState { NOT_REVIEWED, PENDING, APPROVED, REJECTED }

enum class PrivacyFlag {
    EXIF_GPS_PRESENT,
    FACE_VISIBLE,
    POSSIBLE_MINOR,
    LICENSE_PLATE_VISIBLE,
    BOAT_REGISTRATION_VISIBLE,
    DOCUMENT_VISIBLE,
    OTHER_IDENTIFYING_CONTENT
}

enum class PrivacyFindingState { OPEN, RESOLVED, ACCEPTED_NOT_APPLICABLE }

data class PrivacyFinding(
    val flag: PrivacyFlag,
    val state: PrivacyFindingState,
    val notes: String? = null
)

data class ContributionSubmission(
    val submissionId: String,
    val contributorUserId: String,
    val personalSourceReference: String? = null,
    val quarantinedAssetReference: String,
    val provisionalFichaPezId: String?,
    val approvedFichaPezId: String? = null,
    val consentId: String,
    val consentVersion: String,
    val submittedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val status: ContributionStatus,
    val rightsReviewState: ReviewState = ReviewState.NOT_REVIEWED,
    val privacyReviewState: ReviewState = ReviewState.NOT_REVIEWED,
    val labelReviewState: ReviewState = ReviewState.NOT_REVIEWED,
    val qualityReviewState: ReviewState = ReviewState.NOT_REVIEWED,
    val privacyFindings: List<PrivacyFinding> = emptyList(),
    val derivativeGroupId: String? = null,
    val sourceGroupId: String? = null,
    val sessionGroupId: String? = null,
    val individualFishGroupId: String? = null,
    val contributorGroupId: String? = null,
    val withdrawnAtEpochMillis: Long? = null,
    val excludedAtEpochMillis: Long? = null,
    val exclusionReason: String? = null
)

enum class TrainingEligibilityState { PENDING, ELIGIBLE, EXCLUDED, WITHDRAWN }

data class TrainingAsset(
    val trainingAssetId: String,
    val sourceSubmissionId: String,
    val sanitizedObjectReference: String,
    val approvedFichaPezId: String,
    val sha256: String,
    val perceptualHash64: String? = null,
    val derivativeGroupId: String,
    val sessionGroupId: String?,
    val individualFishGroupId: String?,
    val contributorGroupId: String,
    val approvedAtEpochMillis: Long,
    val eligibilityState: TrainingEligibilityState,
    val excludedAtEpochMillis: Long? = null,
    val exclusionReason: String? = null
)

enum class ContributionReviewType { RIGHTS, PRIVACY, LABEL, QUALITY, SPECIALIST, DATASET }
enum class ContributionReviewDecision { APPROVED, REJECTED, CORRECTION_PROPOSED, ESCALATED, RESOLVED }

data class ContributionReviewRecord(
    val reviewId: String,
    val submissionId: String,
    val reviewType: ContributionReviewType,
    val reviewerId: String,
    val reviewedAtEpochMillis: Long,
    val decision: ContributionReviewDecision,
    val proposedFichaPezId: String? = null,
    val reasonCode: String? = null,
    val notes: String? = null
)

enum class ContributionActorType { CONTRIBUTOR, REVIEWER, SYSTEM, DATASET_APPROVER }
enum class ContributionEventType {
    SUBMITTED,
    REVIEW_RECORDED,
    STATUS_CHANGED,
    LABEL_CORRECTED,
    CONSENT_WITHDRAWN,
    ASSET_APPROVED,
    ASSET_EXCLUDED,
    SNAPSHOT_INCLUDED
}

data class ContributionLifecycleEvent(
    val eventId: String,
    val submissionId: String,
    val occurredAtEpochMillis: Long,
    val actorId: String,
    val actorType: ContributionActorType,
    val eventType: ContributionEventType,
    val previousStatus: ContributionStatus? = null,
    val newStatus: ContributionStatus? = null,
    val reasonCode: String? = null,
    val consentId: String? = null,
    val consentVersion: String? = null
)

enum class DatasetPartition { TRAIN, VALIDATION, TEST }

data class DatasetSnapshotMembership(
    val snapshotId: String,
    val trainingAssetId: String,
    val fichaPezId: String,
    val partition: DatasetPartition,
    val includedAtEpochMillis: Long,
    val snapshotSchemaVersion: String,
    val eligibilityStateAtInclusion: TrainingEligibilityState,
    val contentSha256: String
)

data class ModelTrainingRunProvenance(
    val trainingRunId: String,
    val datasetSnapshotId: String,
    val modelVersion: String,
    val preprocessingVersion: String,
    val classifierManifestSha256: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val outputArtifactReference: String? = null,
    val outputArtifactSha256: String? = null
)

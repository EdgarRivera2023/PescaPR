package com.bradmir.pescapr.data.contribution

enum class ModerationReasonCode {
    RIGHTS_UNCLEAR,
    THIRD_PARTY_COPYRIGHT,
    PRIVACY_FACE,
    PRIVACY_MINOR,
    PRIVACY_PLATE,
    LABEL_UNCERTAIN,
    LABEL_WRONG_SPECIES,
    QUALITY_BLUR,
    QUALITY_OCCLUDED,
    DUPLICATE,
    CORRELATED_SEQUENCE,
    AI_GENERATED,
    SCREENSHOT,
    NON_FISH,
    MALICIOUS_OR_INVALID_MEDIA,
    WITHDRAWAL_REQUESTED,
    OTHER
}

data class ModerationAggregate(
    val submission: ContributionSubmission,
    val consent: ContributionConsent,
    val trainingAsset: TrainingAsset? = null,
    val revision: Long = 0,
    val reviews: List<ContributionReviewRecord> = emptyList(),
    val events: List<ContributionLifecycleEvent> = emptyList()
)

sealed interface ModerationCommand {
    val commandId: String
    val submissionId: String
    val actorId: String
    val occurredAtEpochMillis: Long
    val expectedStatus: ContributionStatus
    val expectedRevision: Long
    val reasonCode: ModerationReasonCode?
    val notes: String?
}

data class SubmitForReviewCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class ApproveRightsCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class ResolvePrivacyFindingCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    val privacyFlag: PrivacyFlag,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class ApprovePrivacyCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class ApproveLabelCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    val approvedFichaPezId: String,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class CorrectLabelCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    val correctedFichaPezId: String,
    override val reasonCode: ModerationReasonCode = ModerationReasonCode.LABEL_WRONG_SPECIES,
    override val notes: String? = null
) : ModerationCommand

data class ApproveQualityCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class SendToSpecialistCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode = ModerationReasonCode.LABEL_UNCERTAIN,
    override val notes: String? = null
) : ModerationCommand

data class ApproveForDatasetCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode? = null, override val notes: String? = null
) : ModerationCommand

data class RejectContributionCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode, override val notes: String? = null
) : ModerationCommand

data class ExcludeContributionCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode, override val notes: String? = null
) : ModerationCommand

data class InitiateWithdrawalCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode = ModerationReasonCode.WITHDRAWAL_REQUESTED,
    override val notes: String? = null
) : ModerationCommand

data class CompleteWithdrawalCommand(
    override val commandId: String, override val submissionId: String,
    override val actorId: String, override val occurredAtEpochMillis: Long,
    override val expectedStatus: ContributionStatus, override val expectedRevision: Long,
    override val reasonCode: ModerationReasonCode = ModerationReasonCode.WITHDRAWAL_REQUESTED,
    override val notes: String? = null
) : ModerationCommand

enum class ModerationCommandError {
    SUBMISSION_ID_MISMATCH,
    STALE_STATUS,
    STALE_REVISION,
    TERMINAL_STATE,
    INVALID_TRANSITION,
    INVALID_COMMAND_FOR_STATE,
    OPEN_PRIVACY_FINDINGS,
    UNKNOWN_FICHA_PEZ_ID,
    MISSING_TRAINING_ASSET,
    CONSENT_VERSION_INVALID,
    DATASET_PREREQUISITES_INCOMPLETE
}

sealed interface ModerationResult {
    data class Accepted(
        val aggregate: ModerationAggregate,
        val generatedReviews: List<ContributionReviewRecord>,
        val generatedEvents: List<ContributionLifecycleEvent>
    ) : ModerationResult

    data class Rejected(
        val errors: Set<ModerationCommandError>,
        val validationErrors: Set<ContributionValidationError> = emptySet()
    ) : ModerationResult
}

class ContributionModerationEngine(
    private val validFichaPezIds: Set<String>,
    private val consentRegistry: ConsentVersionRegistry
) {
    fun apply(aggregate: ModerationAggregate, command: ModerationCommand): ModerationResult {
        val guardErrors = guard(aggregate, command)
        if (guardErrors.isNotEmpty()) return ModerationResult.Rejected(guardErrors)

        return when (command) {
            is SubmitForReviewCommand -> submitForReview(aggregate, command)
            is ApproveRightsCommand -> approveReview(
                aggregate, command, ContributionReviewType.RIGHTS,
                ContributionStatus.PRIVACY_REVIEW
            ) { it.copy(rightsReviewState = ReviewState.APPROVED) }
            is ResolvePrivacyFindingCommand -> resolvePrivacy(aggregate, command)
            is ApprovePrivacyCommand -> approvePrivacy(aggregate, command)
            is ApproveLabelCommand -> approveLabel(aggregate, command, command.approvedFichaPezId, false)
            is CorrectLabelCommand -> approveLabel(aggregate, command, command.correctedFichaPezId, true)
            is ApproveQualityCommand -> approveReview(
                aggregate, command, ContributionReviewType.QUALITY,
                ContributionStatus.DATASET_REVIEW
            ) { it.copy(qualityReviewState = ReviewState.APPROVED) }
            is SendToSpecialistCommand -> sendToSpecialist(aggregate, command)
            is ApproveForDatasetCommand -> approveDataset(aggregate, command)
            is RejectContributionCommand -> reject(aggregate, command)
            is ExcludeContributionCommand -> exclude(aggregate, command)
            is InitiateWithdrawalCommand -> transition(
                aggregate, command, ContributionStatus.WITHDRAWAL_PENDING
            )
            is CompleteWithdrawalCommand -> completeWithdrawal(aggregate, command)
        }
    }

    private fun guard(
        aggregate: ModerationAggregate,
        command: ModerationCommand
    ): Set<ModerationCommandError> = buildSet {
        if (command.submissionId != aggregate.submission.submissionId) {
            add(ModerationCommandError.SUBMISSION_ID_MISMATCH)
        }
        if (command.expectedStatus != aggregate.submission.status) add(ModerationCommandError.STALE_STATUS)
        if (command.expectedRevision != aggregate.revision) add(ModerationCommandError.STALE_REVISION)
        if (aggregate.submission.status in setOf(
                ContributionStatus.REJECTED, ContributionStatus.WITHDRAWN,
                ContributionStatus.EXCLUDED, ContributionStatus.ARCHIVED
            )
        ) add(ModerationCommandError.TERMINAL_STATE)
    }

    private fun approvePrivacy(
        aggregate: ModerationAggregate,
        command: ApprovePrivacyCommand
    ): ModerationResult {
        if (aggregate.submission.status != ContributionStatus.PRIVACY_REVIEW) return invalidState()
        if (aggregate.submission.privacyFindings.any { it.state == PrivacyFindingState.OPEN }) {
            return ModerationResult.Rejected(setOf(ModerationCommandError.OPEN_PRIVACY_FINDINGS))
        }
        return approveReview(
            aggregate, command, ContributionReviewType.PRIVACY, ContributionStatus.LABEL_REVIEW
        ) { it.copy(privacyReviewState = ReviewState.APPROVED) }
    }

    private fun submitForReview(
        aggregate: ModerationAggregate,
        command: SubmitForReviewCommand
    ): ModerationResult {
        val errors = FishTrainingContributionRules.consentRegistryErrors(
            aggregate.consent, consentRegistry
        ).toMutableSet()
        if (aggregate.submission.consentId != aggregate.consent.consentId ||
            aggregate.submission.consentVersion != aggregate.consent.consentVersion
        ) errors += ContributionValidationError.CONSENT_REFERENCE_MISMATCH
        if (!aggregate.consent.ownershipConfirmed) {
            errors += ContributionValidationError.OWNERSHIP_NOT_CONFIRMED
        }
        if (!aggregate.consent.mlTrainingAllowed) {
            errors += ContributionValidationError.ML_TRAINING_NOT_ALLOWED
        }
        if (aggregate.consent.isWithdrawn) errors += ContributionValidationError.CONSENT_WITHDRAWN
        if (errors.isNotEmpty()) {
            return ModerationResult.Rejected(
                setOf(ModerationCommandError.CONSENT_VERSION_INVALID), errors
            )
        }
        return transition(aggregate, command, ContributionStatus.SUBMITTED)
    }

    private fun resolvePrivacy(
        aggregate: ModerationAggregate,
        command: ResolvePrivacyFindingCommand
    ): ModerationResult {
        if (aggregate.submission.status != ContributionStatus.PRIVACY_REVIEW) return invalidState()
        val updated = aggregate.submission.copy(
            privacyFindings = aggregate.submission.privacyFindings.map {
                if (it.flag == command.privacyFlag && it.state == PrivacyFindingState.OPEN) {
                    it.copy(state = PrivacyFindingState.RESOLVED, notes = command.notes ?: it.notes)
                } else it
            }
        )
        return accepted(aggregate, command, updated, reviewType = ContributionReviewType.PRIVACY,
            decision = ContributionReviewDecision.RESOLVED)
    }

    private fun approveLabel(
        aggregate: ModerationAggregate,
        command: ModerationCommand,
        fichaPezId: String,
        correction: Boolean
    ): ModerationResult {
        if (aggregate.submission.status !in setOf(
                ContributionStatus.LABEL_REVIEW, ContributionStatus.SPECIALIST_PENDING
            )
        ) return invalidState()
        if (fichaPezId !in validFichaPezIds) {
            return ModerationResult.Rejected(setOf(ModerationCommandError.UNKNOWN_FICHA_PEZ_ID))
        }
        val updated = aggregate.submission.copy(
            approvedFichaPezId = fichaPezId,
            labelReviewState = ReviewState.APPROVED,
            status = ContributionStatus.QUALITY_REVIEW
        )
        return accepted(
            aggregate, command, updated, ContributionReviewType.LABEL,
            if (correction) ContributionReviewDecision.CORRECTION_PROPOSED else ContributionReviewDecision.APPROVED,
            proposedFichaPezId = fichaPezId,
            eventType = if (correction) ContributionEventType.LABEL_CORRECTED else ContributionEventType.REVIEW_RECORDED
        )
    }

    private fun sendToSpecialist(
        aggregate: ModerationAggregate,
        command: SendToSpecialistCommand
    ): ModerationResult {
        if (aggregate.submission.status != ContributionStatus.LABEL_REVIEW) return invalidState()
        val updated = aggregate.submission.copy(status = ContributionStatus.SPECIALIST_PENDING)
        return accepted(
            aggregate, command, updated, ContributionReviewType.SPECIALIST,
            ContributionReviewDecision.ESCALATED
        )
    }

    private fun approveDataset(
        aggregate: ModerationAggregate,
        command: ApproveForDatasetCommand
    ): ModerationResult {
        if (aggregate.submission.status != ContributionStatus.DATASET_REVIEW) return invalidState()
        val asset = aggregate.trainingAsset
            ?: return ModerationResult.Rejected(setOf(ModerationCommandError.MISSING_TRAINING_ASSET))
        val registryErrors = FishTrainingContributionRules.consentRegistryErrors(
            aggregate.consent, consentRegistry
        )
        val approvedSubmission = aggregate.submission.copy(status = ContributionStatus.APPROVED)
        val eligibleAsset = asset.copy(
            approvedFichaPezId = approvedSubmission.approvedFichaPezId.orEmpty(),
            approvedAtEpochMillis = command.occurredAtEpochMillis,
            eligibilityState = TrainingEligibilityState.ELIGIBLE
        )
        val eligibilityErrors = FishTrainingContributionRules.trainingAssetErrors(
            eligibleAsset, approvedSubmission, aggregate.consent, validFichaPezIds
        ) + registryErrors
        if (eligibilityErrors.isNotEmpty()) {
            return ModerationResult.Rejected(
                setOf(
                    if (registryErrors.isEmpty()) ModerationCommandError.DATASET_PREREQUISITES_INCOMPLETE
                    else ModerationCommandError.CONSENT_VERSION_INVALID
                ),
                eligibilityErrors
            )
        }
        return accepted(
            aggregate.copy(trainingAsset = eligibleAsset), command, approvedSubmission,
            ContributionReviewType.DATASET, ContributionReviewDecision.APPROVED,
            eventType = ContributionEventType.ASSET_APPROVED
        )
    }

    private fun reject(
        aggregate: ModerationAggregate,
        command: RejectContributionCommand
    ): ModerationResult {
        if (!ContributionStatusTransitions.isAllowed(aggregate.submission.status, ContributionStatus.REJECTED)) {
            return invalidTransition()
        }
        val updated = aggregate.submission.copy(
            status = ContributionStatus.REJECTED,
            exclusionReason = command.reasonCode.name
        )
        return accepted(
            aggregate, command, updated,
            reviewType = reviewTypeFor(aggregate.submission.status),
            decision = ContributionReviewDecision.REJECTED
        )
    }

    private fun exclude(
        aggregate: ModerationAggregate,
        command: ExcludeContributionCommand
    ): ModerationResult {
        if (!ContributionStatusTransitions.isAllowed(aggregate.submission.status, ContributionStatus.EXCLUDED)) {
            return invalidTransition()
        }
        val updated = aggregate.submission.copy(
            status = ContributionStatus.EXCLUDED,
            excludedAtEpochMillis = command.occurredAtEpochMillis,
            exclusionReason = command.reasonCode.name
        )
        val asset = aggregate.trainingAsset?.copy(
            eligibilityState = TrainingEligibilityState.EXCLUDED,
            excludedAtEpochMillis = command.occurredAtEpochMillis,
            exclusionReason = command.reasonCode.name
        )
        return accepted(
            aggregate.copy(trainingAsset = asset), command, updated,
            eventType = ContributionEventType.ASSET_EXCLUDED
        )
    }

    private fun completeWithdrawal(
        aggregate: ModerationAggregate,
        command: CompleteWithdrawalCommand
    ): ModerationResult {
        if (aggregate.submission.status != ContributionStatus.WITHDRAWAL_PENDING) return invalidState()
        val updated = aggregate.submission.copy(
            status = ContributionStatus.WITHDRAWN,
            withdrawnAtEpochMillis = command.occurredAtEpochMillis
        )
        val asset = aggregate.trainingAsset?.copy(eligibilityState = TrainingEligibilityState.WITHDRAWN)
        return accepted(
            aggregate.copy(
                consent = aggregate.consent.copy(withdrawnAtEpochMillis = command.occurredAtEpochMillis),
                trainingAsset = asset
            ),
            command, updated, eventType = ContributionEventType.CONSENT_WITHDRAWN
        )
    }

    private fun transition(
        aggregate: ModerationAggregate,
        command: ModerationCommand,
        next: ContributionStatus
    ): ModerationResult {
        if (!ContributionStatusTransitions.isAllowed(aggregate.submission.status, next)) {
            return invalidTransition()
        }
        return accepted(aggregate, command, aggregate.submission.copy(status = next))
    }

    private fun approveReview(
        aggregate: ModerationAggregate,
        command: ModerationCommand,
        reviewType: ContributionReviewType,
        nextStatus: ContributionStatus,
        update: (ContributionSubmission) -> ContributionSubmission
    ): ModerationResult {
        val allowedCurrent = when (reviewType) {
            ContributionReviewType.RIGHTS -> setOf(ContributionStatus.SUBMITTED, ContributionStatus.RIGHTS_REVIEW)
            ContributionReviewType.QUALITY -> setOf(ContributionStatus.QUALITY_REVIEW)
            else -> emptySet()
        }
        if (aggregate.submission.status !in allowedCurrent) return invalidState()
        val updated = update(aggregate.submission).copy(status = nextStatus)
        return accepted(aggregate, command, updated, reviewType, ContributionReviewDecision.APPROVED)
    }

    private fun accepted(
        aggregate: ModerationAggregate,
        command: ModerationCommand,
        submission: ContributionSubmission,
        reviewType: ContributionReviewType? = null,
        decision: ContributionReviewDecision = ContributionReviewDecision.RESOLVED,
        proposedFichaPezId: String? = null,
        eventType: ContributionEventType = ContributionEventType.STATUS_CHANGED
    ): ModerationResult.Accepted {
        val review = reviewType?.let {
            ContributionReviewRecord(
                reviewId = "review:${command.commandId}",
                submissionId = command.submissionId,
                reviewType = it,
                reviewerId = command.actorId,
                reviewedAtEpochMillis = command.occurredAtEpochMillis,
                decision = decision,
                proposedFichaPezId = proposedFichaPezId,
                reasonCode = command.reasonCode?.name,
                notes = command.notes
            )
        }
        val event = ContributionLifecycleEvent(
            eventId = "event:${command.commandId}",
            submissionId = command.submissionId,
            occurredAtEpochMillis = command.occurredAtEpochMillis,
            actorId = command.actorId,
            actorType = ContributionActorType.REVIEWER,
            eventType = eventType,
            previousStatus = aggregate.submission.status,
            newStatus = submission.status,
            reasonCode = command.reasonCode?.name,
            consentId = aggregate.consent.consentId,
            consentVersion = aggregate.consent.consentVersion
        )
        val reviews = listOfNotNull(review)
        val next = aggregate.copy(
            submission = submission.copy(updatedAtEpochMillis = command.occurredAtEpochMillis),
            revision = aggregate.revision + 1,
            reviews = aggregate.reviews + reviews,
            events = aggregate.events + event
        )
        return ModerationResult.Accepted(next, reviews, listOf(event))
    }

    private fun invalidState() = ModerationResult.Rejected(
        setOf(ModerationCommandError.INVALID_COMMAND_FOR_STATE)
    )

    private fun invalidTransition() = ModerationResult.Rejected(
        setOf(ModerationCommandError.INVALID_TRANSITION)
    )

    private fun reviewTypeFor(status: ContributionStatus): ContributionReviewType? = when (status) {
        ContributionStatus.RIGHTS_REVIEW, ContributionStatus.SUBMITTED -> ContributionReviewType.RIGHTS
        ContributionStatus.PRIVACY_REVIEW -> ContributionReviewType.PRIVACY
        ContributionStatus.LABEL_REVIEW -> ContributionReviewType.LABEL
        ContributionStatus.QUALITY_REVIEW -> ContributionReviewType.QUALITY
        ContributionStatus.SPECIALIST_PENDING -> ContributionReviewType.SPECIALIST
        ContributionStatus.DATASET_REVIEW, ContributionStatus.APPROVED -> ContributionReviewType.DATASET
        else -> null
    }
}

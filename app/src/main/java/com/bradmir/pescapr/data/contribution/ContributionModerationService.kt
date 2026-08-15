package com.bradmir.pescapr.data.contribution

data class ContributionModerationRequest(
    val principal: ContributionPrincipal,
    val command: ModerationCommand
)

sealed interface ContributionModerationServiceResult {
    data class Success(val accepted: ModerationResult.Accepted) : ContributionModerationServiceResult
    data class AuthorizationDenied(
        val denial: ContributionAuthorizationDecision.Denied
    ) : ContributionModerationServiceResult
    data object ContributionNotFound : ContributionModerationServiceResult
    data class ModerationRejected(
        val rejection: ModerationResult.Rejected
    ) : ContributionModerationServiceResult
    data class StorageConflict(
        val failure: ContributionReplaceResult
    ) : ContributionModerationServiceResult
    data class StorageInvariantFailure(
        val failure: ContributionReplaceResult
    ) : ContributionModerationServiceResult
}

/**
 * Backend-neutral coordinator. A future store must make replace an atomic compare-and-replace.
 * Conflicts are returned to the caller and are never retried here.
 */
class ContributionModerationService(
    private val authorizationPolicy: ContributionAuthorizationPolicy,
    private val aggregateStore: ContributionAggregateStore,
    private val moderationEngine: ContributionModerationEngine
) {
    fun execute(request: ContributionModerationRequest): ContributionModerationServiceResult {
        val action = actionFor(request.command)
        val requirement = ContributionAuthorizationRequirements.forAction(action)
        if (requirement.capability !in request.principal.capabilities) {
            return denied(ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED)
        }
        if (request.command.actorId != request.principal.id.value) {
            return denied(ContributionAuthorizationDenial.ACTOR_ID_MISMATCH)
        }

        val id = ContributionId(request.command.submissionId)
        val current = aggregateStore.find(id)
            ?: return ContributionModerationServiceResult.ContributionNotFound
        val authorization = authorizationPolicy.decide(
            request.principal,
            action,
            ContributionAuthorizationContext(
                ownerPrincipalId = ContributionPrincipalId(current.submission.contributorUserId)
            )
        )
        if (authorization is ContributionAuthorizationDecision.Denied) {
            return ContributionModerationServiceResult.AuthorizationDenied(authorization)
        }

        return when (val moderation = moderationEngine.apply(current, request.command)) {
            is ModerationResult.Rejected ->
                ContributionModerationServiceResult.ModerationRejected(moderation)
            is ModerationResult.Accepted -> replace(id, current.revision, moderation)
        }
    }

    private fun replace(
        id: ContributionId,
        expectedRevision: Long,
        accepted: ModerationResult.Accepted
    ): ContributionModerationServiceResult = when (
        val replacement = aggregateStore.replace(id, expectedRevision, accepted.aggregate)
    ) {
        is ContributionReplaceResult.Replaced -> ContributionModerationServiceResult.Success(accepted)
        is ContributionReplaceResult.StaleRevision, ContributionReplaceResult.NotFound ->
            ContributionModerationServiceResult.StorageConflict(replacement)
        ContributionReplaceResult.InvalidNextRevision,
        ContributionReplaceResult.HistoryWouldBeRewritten ->
            ContributionModerationServiceResult.StorageInvariantFailure(replacement)
    }

    private fun actionFor(command: ModerationCommand): ContributionAction = when (command) {
        is SubmitForReviewCommand -> ContributionAction.SUBMIT_OWN_CONTRIBUTION
        is ApproveLabelCommand, is CorrectLabelCommand ->
            if (command.expectedStatus == ContributionStatus.SPECIALIST_PENDING) {
                ContributionAction.SPECIALIST_LABEL_REVIEW
            } else {
                ContributionAction.MODERATE_CONTRIBUTION
            }
        is ApproveForDatasetCommand -> ContributionAction.APPROVE_DATASET
        is InitiateWithdrawalCommand -> ContributionAction.INITIATE_OWN_WITHDRAWAL
        is CompleteWithdrawalCommand -> ContributionAction.COMPLETE_WITHDRAWAL
        is ExcludeContributionCommand -> ContributionAction.ADMINISTRATIVE_EXCLUSION
        is ApproveRightsCommand,
        is ResolvePrivacyFindingCommand,
        is ApprovePrivacyCommand,
        is ApproveQualityCommand,
        is SendToSpecialistCommand,
        is RejectContributionCommand -> ContributionAction.MODERATE_CONTRIBUTION
    }

    private fun denied(
        reason: ContributionAuthorizationDenial
    ) = ContributionModerationServiceResult.AuthorizationDenied(
        ContributionAuthorizationDecision.Denied(reason)
    )
}

package com.bradmir.pescapr.data.contribution

@JvmInline value class ContributionPrincipalId(val value: String)

data class ContributionPrincipal(
    val id: ContributionPrincipalId,
    val capabilities: Set<ContributionCapability> = emptySet()
)

enum class ContributionCapability {
    READ_OWN_CONTRIBUTION,
    INITIATE_OWN_WITHDRAWAL,
    MODERATE_CONTRIBUTION,
    SPECIALIST_LABEL_REVIEW,
    APPROVE_DATASET,
    COMPLETE_WITHDRAWAL,
    ADMINISTRATIVE_EXCLUSION,
    READ_SENSITIVE_QUARANTINE,
    READ_SANITIZED_TRAINING_ASSET,
    INSPECT_DATASET_PROVENANCE,
    AUTHORIZE_PUBLIC_DISPLAY,
    AUTHORIZE_ML_TRAINING
}

enum class ContributionAction {
    READ_OWN_CONTRIBUTION,
    INITIATE_OWN_WITHDRAWAL,
    MODERATE_CONTRIBUTION,
    SPECIALIST_LABEL_REVIEW,
    APPROVE_DATASET,
    COMPLETE_WITHDRAWAL,
    ADMINISTRATIVE_EXCLUSION,
    READ_SENSITIVE_QUARANTINE,
    READ_SANITIZED_TRAINING_ASSET,
    INSPECT_DATASET_PROVENANCE,
    USE_FOR_PUBLIC_DISPLAY,
    USE_FOR_ML_TRAINING
}

data class ContributionAuthorizationContext(
    val ownerPrincipalId: ContributionPrincipalId? = null,
    val consent: ContributionConsent? = null
)

sealed interface ContributionAuthorizationDecision {
    data object Allowed : ContributionAuthorizationDecision
    data class Denied(val reason: ContributionAuthorizationDenial) : ContributionAuthorizationDecision
}

enum class ContributionAuthorizationDenial {
    CAPABILITY_NOT_GRANTED,
    NOT_RESOURCE_OWNER,
    CONSENT_PERMISSION_NOT_GRANTED
}

interface ContributionAuthorizationPolicy {
    fun decide(
        principal: ContributionPrincipal,
        action: ContributionAction,
        context: ContributionAuthorizationContext = ContributionAuthorizationContext()
    ): ContributionAuthorizationDecision
}

/** Explicit-grant, deny-by-default policy with no authentication-provider assumptions. */
class CapabilityContributionAuthorizationPolicy : ContributionAuthorizationPolicy {
    override fun decide(
        principal: ContributionPrincipal,
        action: ContributionAction,
        context: ContributionAuthorizationContext
    ): ContributionAuthorizationDecision {
        val required = requiredCapability(action)
        if (required !in principal.capabilities) {
            return ContributionAuthorizationDecision.Denied(
                ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED
            )
        }
        if (action in ownerOnlyActions && context.ownerPrincipalId != principal.id) {
            return ContributionAuthorizationDecision.Denied(
                ContributionAuthorizationDenial.NOT_RESOURCE_OWNER
            )
        }
        val consentAllows = when (action) {
            ContributionAction.USE_FOR_PUBLIC_DISPLAY -> context.consent?.publicDisplayAllowed == true
            ContributionAction.USE_FOR_ML_TRAINING -> context.consent?.mlTrainingAllowed == true
            else -> true
        }
        if (!consentAllows) {
            return ContributionAuthorizationDecision.Denied(
                ContributionAuthorizationDenial.CONSENT_PERMISSION_NOT_GRANTED
            )
        }
        return ContributionAuthorizationDecision.Allowed
    }

    private fun requiredCapability(action: ContributionAction): ContributionCapability = when (action) {
        ContributionAction.READ_OWN_CONTRIBUTION -> ContributionCapability.READ_OWN_CONTRIBUTION
        ContributionAction.INITIATE_OWN_WITHDRAWAL -> ContributionCapability.INITIATE_OWN_WITHDRAWAL
        ContributionAction.MODERATE_CONTRIBUTION -> ContributionCapability.MODERATE_CONTRIBUTION
        ContributionAction.SPECIALIST_LABEL_REVIEW -> ContributionCapability.SPECIALIST_LABEL_REVIEW
        ContributionAction.APPROVE_DATASET -> ContributionCapability.APPROVE_DATASET
        ContributionAction.COMPLETE_WITHDRAWAL -> ContributionCapability.COMPLETE_WITHDRAWAL
        ContributionAction.ADMINISTRATIVE_EXCLUSION -> ContributionCapability.ADMINISTRATIVE_EXCLUSION
        ContributionAction.READ_SENSITIVE_QUARANTINE -> ContributionCapability.READ_SENSITIVE_QUARANTINE
        ContributionAction.READ_SANITIZED_TRAINING_ASSET ->
            ContributionCapability.READ_SANITIZED_TRAINING_ASSET
        ContributionAction.INSPECT_DATASET_PROVENANCE -> ContributionCapability.INSPECT_DATASET_PROVENANCE
        ContributionAction.USE_FOR_PUBLIC_DISPLAY -> ContributionCapability.AUTHORIZE_PUBLIC_DISPLAY
        ContributionAction.USE_FOR_ML_TRAINING -> ContributionCapability.AUTHORIZE_ML_TRAINING
    }

    private companion object {
        val ownerOnlyActions = setOf(
            ContributionAction.READ_OWN_CONTRIBUTION,
            ContributionAction.INITIATE_OWN_WITHDRAWAL
        )
    }
}

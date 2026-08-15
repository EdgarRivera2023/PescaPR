package com.bradmir.pescapr.data.contribution

@JvmInline value class ContributionPrincipalId(val value: String)

data class ContributionPrincipal(
    val id: ContributionPrincipalId,
    val capabilities: Set<ContributionCapability> = emptySet()
)

enum class ContributionCapability {
    SUBMIT_OWN_CONTRIBUTION,
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
    SUBMIT_OWN_CONTRIBUTION,
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
    CONSENT_PERMISSION_NOT_GRANTED,
    ACTOR_ID_MISMATCH
}

data class ContributionAuthorizationRequirement(
    val capability: ContributionCapability,
    val requiresOwnership: Boolean = false
)

object ContributionAuthorizationRequirements {
    fun forAction(action: ContributionAction): ContributionAuthorizationRequirement = when (action) {
        ContributionAction.SUBMIT_OWN_CONTRIBUTION -> requirement(
            ContributionCapability.SUBMIT_OWN_CONTRIBUTION, requiresOwnership = true
        )
        ContributionAction.READ_OWN_CONTRIBUTION -> requirement(
            ContributionCapability.READ_OWN_CONTRIBUTION, requiresOwnership = true
        )
        ContributionAction.INITIATE_OWN_WITHDRAWAL -> requirement(
            ContributionCapability.INITIATE_OWN_WITHDRAWAL, requiresOwnership = true
        )
        ContributionAction.MODERATE_CONTRIBUTION -> requirement(ContributionCapability.MODERATE_CONTRIBUTION)
        ContributionAction.SPECIALIST_LABEL_REVIEW -> requirement(
            ContributionCapability.SPECIALIST_LABEL_REVIEW
        )
        ContributionAction.APPROVE_DATASET -> requirement(ContributionCapability.APPROVE_DATASET)
        ContributionAction.COMPLETE_WITHDRAWAL -> requirement(ContributionCapability.COMPLETE_WITHDRAWAL)
        ContributionAction.ADMINISTRATIVE_EXCLUSION -> requirement(
            ContributionCapability.ADMINISTRATIVE_EXCLUSION
        )
        ContributionAction.READ_SENSITIVE_QUARANTINE -> requirement(
            ContributionCapability.READ_SENSITIVE_QUARANTINE
        )
        ContributionAction.READ_SANITIZED_TRAINING_ASSET -> requirement(
            ContributionCapability.READ_SANITIZED_TRAINING_ASSET
        )
        ContributionAction.INSPECT_DATASET_PROVENANCE -> requirement(
            ContributionCapability.INSPECT_DATASET_PROVENANCE
        )
        ContributionAction.USE_FOR_PUBLIC_DISPLAY -> requirement(
            ContributionCapability.AUTHORIZE_PUBLIC_DISPLAY
        )
        ContributionAction.USE_FOR_ML_TRAINING -> requirement(ContributionCapability.AUTHORIZE_ML_TRAINING)
    }

    private fun requirement(
        capability: ContributionCapability,
        requiresOwnership: Boolean = false
    ) = ContributionAuthorizationRequirement(capability, requiresOwnership)
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
        val requirement = ContributionAuthorizationRequirements.forAction(action)
        if (requirement.capability !in principal.capabilities) {
            return ContributionAuthorizationDecision.Denied(
                ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED
            )
        }
        if (requirement.requiresOwnership && context.ownerPrincipalId != principal.id) {
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

}

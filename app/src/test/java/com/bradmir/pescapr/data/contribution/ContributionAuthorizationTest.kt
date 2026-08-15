package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Test

class ContributionAuthorizationTest {
    private val policy = CapabilityContributionAuthorizationPolicy()
    private val ownerId = ContributionPrincipalId("synthetic-owner")

    @Test
    fun `explicitly granted capability is allowed`() {
        val principal = principal(ContributionCapability.MODERATE_CONTRIBUTION)

        assertEquals(
            ContributionAuthorizationDecision.Allowed,
            policy.decide(principal, ContributionAction.MODERATE_CONTRIBUTION)
        )
    }

    @Test
    fun `missing capability is denied`() {
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            principal(), ContributionAction.MODERATE_CONTRIBUTION
        )
    }

    @Test
    fun `ownership alone grants neither moderation nor withdrawal`() {
        val context = ContributionAuthorizationContext(ownerPrincipalId = ownerId)

        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            principal(), ContributionAction.MODERATE_CONTRIBUTION, context
        )
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            principal(), ContributionAction.INITIATE_OWN_WITHDRAWAL, context
        )
    }

    @Test
    fun `own withdrawal requires both capability and matching owner`() {
        val principal = principal(ContributionCapability.INITIATE_OWN_WITHDRAWAL)

        assertDenied(
            ContributionAuthorizationDenial.NOT_RESOURCE_OWNER,
            principal, ContributionAction.INITIATE_OWN_WITHDRAWAL,
            ContributionAuthorizationContext(ContributionPrincipalId("another-owner"))
        )
        assertEquals(
            ContributionAuthorizationDecision.Allowed,
            policy.decide(
                principal, ContributionAction.INITIATE_OWN_WITHDRAWAL,
                ContributionAuthorizationContext(ownerId)
            )
        )
    }

    @Test
    fun `moderator does not automatically receive dataset approval`() {
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            principal(ContributionCapability.MODERATE_CONTRIBUTION),
            ContributionAction.APPROVE_DATASET
        )
    }

    @Test
    fun `dataset approver can authorize dataset approval`() {
        assertEquals(
            ContributionAuthorizationDecision.Allowed,
            policy.decide(
                principal(ContributionCapability.APPROVE_DATASET),
                ContributionAction.APPROVE_DATASET
            )
        )
    }

    @Test
    fun `quarantine access requires its stronger explicit capability`() {
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            principal(ContributionCapability.READ_SANITIZED_TRAINING_ASSET),
            ContributionAction.READ_SENSITIVE_QUARANTINE
        )
    }

    @Test
    fun `public display permission grants neither training use nor asset access`() {
        val consent = consent(mlTraining = false, publicDisplay = true)
        val displayPrincipal = principal(ContributionCapability.AUTHORIZE_PUBLIC_DISPLAY)

        assertEquals(
            ContributionAuthorizationDecision.Allowed,
            policy.decide(
                displayPrincipal, ContributionAction.USE_FOR_PUBLIC_DISPLAY,
                ContributionAuthorizationContext(consent = consent)
            )
        )
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            displayPrincipal, ContributionAction.READ_SANITIZED_TRAINING_ASSET
        )
        assertDenied(
            ContributionAuthorizationDenial.CAPABILITY_NOT_GRANTED,
            displayPrincipal, ContributionAction.USE_FOR_ML_TRAINING,
            ContributionAuthorizationContext(consent = consent)
        )
    }

    @Test
    fun `ML consent does not grant public display authority`() {
        val consent = consent(mlTraining = true, publicDisplay = false)

        assertDenied(
            ContributionAuthorizationDenial.CONSENT_PERMISSION_NOT_GRANTED,
            principal(ContributionCapability.AUTHORIZE_PUBLIC_DISPLAY),
            ContributionAction.USE_FOR_PUBLIC_DISPLAY,
            ContributionAuthorizationContext(consent = consent)
        )
        assertEquals(
            ContributionAuthorizationDecision.Allowed,
            policy.decide(
                principal(ContributionCapability.AUTHORIZE_ML_TRAINING),
                ContributionAction.USE_FOR_ML_TRAINING,
                ContributionAuthorizationContext(consent = consent)
            )
        )
    }

    private fun principal(vararg capabilities: ContributionCapability) =
        ContributionPrincipal(ownerId, capabilities.toSet())

    private fun consent(mlTraining: Boolean, publicDisplay: Boolean) = ContributionConsent(
        consentId = "synthetic-consent",
        consentVersion = "synthetic-version",
        consentLocale = "en-US",
        acceptedAtEpochMillis = 100,
        ownershipConfirmed = true,
        mlTrainingAllowed = mlTraining,
        publicDisplayAllowed = publicDisplay
    )

    private fun assertDenied(
        reason: ContributionAuthorizationDenial,
        principal: ContributionPrincipal,
        action: ContributionAction,
        context: ContributionAuthorizationContext = ContributionAuthorizationContext()
    ) {
        assertEquals(
            ContributionAuthorizationDecision.Denied(reason),
            policy.decide(principal, action, context)
        )
    }
}

package com.bradmir.pescapr.data.contribution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentVersionRegistryTest {
    @Test(expected = IllegalArgumentException::class)
    fun `duplicate version ID is rejected`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(draft("draft-v1"))
        registry.register(draft("draft-v1"))
    }

    @Test
    fun `draft and legal review versions are not selectable`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(draft("draft-v1"))
        registry.register(draft("review-v1").copy(status = ConsentVersionStatus.LEGAL_REVIEW))
        assertNull(registry.currentSelectable("en-US", 10_000))
        assertFalse(registry.wasValidAtAcceptance(consent("draft-v1", "en-US", 4_000)))
        assertFalse(registry.wasValidAtAcceptance(consent("review-v1", "en-US", 4_000)))
    }

    @Test
    fun `approved synthetic fixture is selectable by locale`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(approved("approved-en", "en-US"))
        registry.register(approved("approved-es", "es-PR"))
        assertEquals("approved-es", registry.currentSelectable("es-PR", 10_000)?.consentVersionId)
        assertNull(registry.currentSelectable("fr-FR", 10_000))
    }

    @Test
    fun `retired version is not selectable but remains historically valid`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(approved("retired-v1", "en-US").copy(
            status = ConsentVersionStatus.RETIRED,
            selectableForNewContributions = false,
            retiredAtEpochMillis = 5_000
        ))
        assertNull(registry.currentSelectable("en-US", 10_000))
        assertTrue(registry.wasValidAtAcceptance(consent("retired-v1", "en-US", 4_000)))
        assertFalse(registry.wasValidAtAcceptance(consent("retired-v1", "en-US", 6_000)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `conflicting current versions for locale are rejected`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(approved("approved-v1", "en-US"))
        registry.register(approved("approved-v2", "en-US"))
    }

    @Test
    fun `empty production-style registry exposes no approved version`() {
        val registry: ConsentVersionRegistry = InMemoryConsentVersionRegistry()
        assertNull(registry.currentSelectable("en-US", 10_000))
    }

    @Test
    fun `registry validation rejects unknown and mismatched locale`() {
        val registry = InMemoryConsentVersionRegistry()
        registry.register(approved("approved-v1", "en-US"))
        val unknown = FishTrainingContributionRules.consentRegistryErrors(
            consent("missing", "en-US", 4_000), registry
        )
        val mismatch = FishTrainingContributionRules.consentRegistryErrors(
            consent("approved-v1", "es-PR", 4_000), registry
        )
        assertTrue(unknown.contains(ContributionValidationError.CONSENT_VERSION_NOT_FOUND))
        assertTrue(mismatch.contains(ContributionValidationError.CONSENT_LOCALE_MISMATCH))
    }

    private fun draft(id: String) = ConsentVersionMetadata(
        consentVersionId = id,
        status = ConsentVersionStatus.DRAFT,
        effectiveFromEpochMillis = 1_000,
        locale = "en-US",
        contentIdentifier = "fixture://consent/$id",
        contentSha256 = "a".repeat(64)
    )

    private fun approved(id: String, locale: String) = ConsentVersionMetadata(
        consentVersionId = id,
        status = ConsentVersionStatus.APPROVED,
        effectiveFromEpochMillis = 1_000,
        approvedAtEpochMillis = 900,
        locale = locale,
        contentIdentifier = "fixture://consent/$id",
        contentSha256 = "b".repeat(64),
        selectableForNewContributions = true
    )

    private fun consent(version: String, locale: String, acceptedAt: Long) = ContributionConsent(
        consentId = "consent-fixture",
        consentVersion = version,
        consentLocale = locale,
        acceptedAtEpochMillis = acceptedAt,
        ownershipConfirmed = true,
        mlTrainingAllowed = true
    )
}

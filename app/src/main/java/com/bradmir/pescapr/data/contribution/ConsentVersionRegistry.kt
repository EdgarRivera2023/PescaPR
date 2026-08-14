package com.bradmir.pescapr.data.contribution

enum class ConsentVersionStatus { DRAFT, LEGAL_REVIEW, APPROVED, RETIRED }

data class ConsentVersionMetadata(
    val consentVersionId: String,
    val status: ConsentVersionStatus,
    val effectiveFromEpochMillis: Long,
    val approvedAtEpochMillis: Long? = null,
    val retiredAtEpochMillis: Long? = null,
    val locale: String,
    val contentIdentifier: String,
    val contentSha256: String,
    val selectableForNewContributions: Boolean = false
) {
    fun isSelectableAt(atEpochMillis: Long): Boolean =
        status == ConsentVersionStatus.APPROVED &&
            selectableForNewContributions &&
            approvedAtEpochMillis != null &&
            approvedAtEpochMillis <= atEpochMillis &&
            effectiveFromEpochMillis <= atEpochMillis &&
            (retiredAtEpochMillis == null || atEpochMillis < retiredAtEpochMillis)

    fun wasValidAt(acceptedAtEpochMillis: Long): Boolean =
        approvedAtEpochMillis != null &&
            approvedAtEpochMillis <= acceptedAtEpochMillis &&
            effectiveFromEpochMillis <= acceptedAtEpochMillis &&
            (retiredAtEpochMillis == null || acceptedAtEpochMillis < retiredAtEpochMillis)
}

interface ConsentVersionRegistry {
    fun resolve(consentVersionId: String): ConsentVersionMetadata?
    fun currentSelectable(locale: String, atEpochMillis: Long): ConsentVersionMetadata?
    fun wasValidAtAcceptance(consent: ContributionConsent): Boolean
}

/** Empty by default. Test/development callers must explicitly register synthetic metadata. */
class InMemoryConsentVersionRegistry : ConsentVersionRegistry {
    private val versions = linkedMapOf<String, ConsentVersionMetadata>()

    fun register(version: ConsentVersionMetadata) {
        require(version.consentVersionId.isNotBlank()) { "Consent version ID must not be blank" }
        require(version.locale.isNotBlank()) { "Consent locale must not be blank" }
        require(Regex("^[0-9a-fA-F]{64}$").matches(version.contentSha256)) {
            "Consent content SHA-256 must contain 64 hexadecimal characters"
        }
        require(version.consentVersionId !in versions) {
            "Duplicate consent version ID: ${version.consentVersionId}"
        }
        if (version.status != ConsentVersionStatus.APPROVED) {
            require(!version.selectableForNewContributions) {
                "Only an APPROVED consent version may be selectable"
            }
        }
        if (version.status == ConsentVersionStatus.APPROVED) {
            require(version.approvedAtEpochMillis != null) {
                "APPROVED consent versions require an approval timestamp"
            }
        }
        if (version.status == ConsentVersionStatus.RETIRED) {
            require(version.approvedAtEpochMillis != null && version.retiredAtEpochMillis != null) {
                "RETIRED consent versions require approval and retirement timestamps"
            }
            require(!version.selectableForNewContributions) {
                "RETIRED consent versions cannot be selectable"
            }
        }
        if (version.selectableForNewContributions) {
            require(versions.values.none {
                it.locale == version.locale && it.status == ConsentVersionStatus.APPROVED &&
                    it.selectableForNewContributions && rangesOverlap(it, version)
            }) { "Conflicting selectable consent version for locale ${version.locale}" }
        }
        versions[version.consentVersionId] = version
    }

    override fun resolve(consentVersionId: String): ConsentVersionMetadata? = versions[consentVersionId]

    override fun currentSelectable(locale: String, atEpochMillis: Long): ConsentVersionMetadata? =
        versions.values.singleOrNull { it.locale == locale && it.isSelectableAt(atEpochMillis) }

    override fun wasValidAtAcceptance(consent: ContributionConsent): Boolean {
        val version = resolve(consent.consentVersion) ?: return false
        return version.locale == consent.consentLocale && version.wasValidAt(consent.acceptedAtEpochMillis)
    }

    private fun rangesOverlap(a: ConsentVersionMetadata, b: ConsentVersionMetadata): Boolean {
        val aEnd = a.retiredAtEpochMillis ?: Long.MAX_VALUE
        val bEnd = b.retiredAtEpochMillis ?: Long.MAX_VALUE
        return a.effectiveFromEpochMillis < bEnd && b.effectiveFromEpochMillis < aEnd
    }
}

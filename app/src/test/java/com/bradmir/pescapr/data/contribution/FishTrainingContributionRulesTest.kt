package com.bradmir.pescapr.data.contribution

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FishTrainingContributionRulesTest {
    private val validIds by lazy { loadManifestIds() }
    private val fishId = "RO2iuTVLAX11dy3aNgdf"

    @Test
    fun `valid private training contribution is eligible without display or marketing grants`() {
        val fixture = validFixture()
        assertFalse(fixture.consent.publicDisplayAllowed)
        assertFalse(fixture.consent.marketingAllowed)
        assertTrue(eligible(fixture))
    }

    @Test
    fun `public display or marketing grants never substitute for ML permission`() {
        val fixture = validFixture().let {
            it.copy(consent = it.consent.copy(
                mlTrainingAllowed = false,
                publicDisplayAllowed = true,
                marketingAllowed = true
            ))
        }
        assertFalse(eligible(fixture))
        assertTrue(errors(fixture).contains(ContributionValidationError.ML_TRAINING_NOT_ALLOWED))
    }

    @Test
    fun `withdrawn contribution is never eligible`() {
        val fixture = validFixture().let {
            it.copy(
                consent = it.consent.copy(withdrawnAtEpochMillis = 2_000),
                submission = it.submission.copy(
                    status = ContributionStatus.WITHDRAWN,
                    withdrawnAtEpochMillis = 2_000
                ),
                asset = it.asset.copy(eligibilityState = TrainingEligibilityState.WITHDRAWN)
            )
        }
        assertFalse(eligible(fixture))
        assertTrue(errors(fixture).contains(ContributionValidationError.CONSENT_WITHDRAWN))
        assertTrue(errors(fixture).contains(ContributionValidationError.SUBMISSION_WITHDRAWN_OR_EXCLUDED))
    }

    @Test
    fun `label pending fixture is not eligible`() {
        val fixture = validFixture().let {
            it.copy(submission = it.submission.copy(
                status = ContributionStatus.LABEL_REVIEW,
                approvedFichaPezId = null,
                labelReviewState = ReviewState.PENDING
            ))
        }
        assertFalse(eligible(fixture))
        assertTrue(errors(fixture).contains(ContributionValidationError.LABEL_NOT_APPROVED))
        assertTrue(errors(fixture).contains(ContributionValidationError.MISSING_APPROVED_FICHA_PEZ_ID))
    }

    @Test
    fun `open privacy finding blocks otherwise valid contribution`() {
        val fixture = validFixture().let {
            it.copy(submission = it.submission.copy(
                status = ContributionStatus.PRIVACY_REVIEW,
                privacyReviewState = ReviewState.PENDING,
                privacyFindings = listOf(PrivacyFinding(PrivacyFlag.FACE_VISIBLE, PrivacyFindingState.OPEN))
            ))
        }
        assertFalse(eligible(fixture))
        assertTrue(errors(fixture).contains(ContributionValidationError.UNRESOLVED_PRIVACY_FINDING))
    }

    @Test
    fun `unknown canonical ID and mismatched asset are rejected`() {
        val fixture = validFixture().let {
            it.copy(
                submission = it.submission.copy(approvedFichaPezId = "not-in-frozen-manifest"),
                asset = it.asset.copy(approvedFichaPezId = fishId)
            )
        }
        val errors = errors(fixture)
        assertTrue(errors.contains(ContributionValidationError.UNKNOWN_FICHA_PEZ_ID))
        assertTrue(errors.contains(ContributionValidationError.ASSET_LABEL_MISMATCH))
    }

    @Test
    fun `approved state without explicit consent is detected`() {
        val fixture = validFixture().let {
            it.copy(consent = it.consent.copy(ownershipConfirmed = false, mlTrainingAllowed = false))
        }
        val errors = errors(fixture)
        assertTrue(errors.contains(ContributionValidationError.OWNERSHIP_NOT_CONFIRMED))
        assertTrue(errors.contains(ContributionValidationError.ML_TRAINING_NOT_ALLOWED))
    }

    @Test
    fun `training asset without canonical ID is rejected`() {
        val fixture = validFixture().let {
            it.copy(asset = it.asset.copy(approvedFichaPezId = ""))
        }
        assertFalse(eligible(fixture))
        assertTrue(errors(fixture).contains(ContributionValidationError.MISSING_APPROVED_FICHA_PEZ_ID))
    }

    @Test
    fun `lifecycle policy allows review flow and blocks terminal restoration`() {
        assertTrue(ContributionStatusTransitions.isAllowed(ContributionStatus.DRAFT, ContributionStatus.SUBMITTED))
        assertTrue(ContributionStatusTransitions.isAllowed(ContributionStatus.SUBMITTED, ContributionStatus.RIGHTS_REVIEW))
        assertTrue(ContributionStatusTransitions.isAllowed(ContributionStatus.RIGHTS_REVIEW, ContributionStatus.REJECTED))
        assertTrue(ContributionStatusTransitions.isAllowed(ContributionStatus.DATASET_REVIEW, ContributionStatus.APPROVED))
        assertTrue(ContributionStatusTransitions.isAllowed(ContributionStatus.APPROVED, ContributionStatus.WITHDRAWN))
        assertFalse(ContributionStatusTransitions.isAllowed(ContributionStatus.WITHDRAWN, ContributionStatus.APPROVED))
        assertFalse(ContributionStatusTransitions.isAllowed(ContributionStatus.ARCHIVED, ContributionStatus.SUBMITTED))
    }

    @Test
    fun `contracts preserve values through data class copy round trip`() {
        val fixture = validFixture()
        assertEquals(fixture.consent, fixture.consent.copy())
        assertEquals(fixture.submission, fixture.submission.copy())
        assertEquals(fixture.asset, fixture.asset.copy())
    }

    private fun eligible(fixture: Fixture): Boolean = FishTrainingContributionRules.isTrainingEligible(
        fixture.asset, fixture.submission, fixture.consent, validIds
    )

    private fun errors(fixture: Fixture): Set<ContributionValidationError> =
        FishTrainingContributionRules.trainingAssetErrors(
            fixture.asset, fixture.submission, fixture.consent, validIds
        )

    private fun validFixture(): Fixture {
        val consent = ContributionConsent(
            consentId = "consent-fixture-1",
            consentVersion = "fish-training-consent-test-v1",
            consentLocale = "en-US",
            acceptedAtEpochMillis = 1_000,
            ownershipConfirmed = true,
            mlTrainingAllowed = true
        )
        val submission = ContributionSubmission(
            submissionId = "submission-fixture-1",
            contributorUserId = "user-fixture-1",
            quarantinedAssetReference = "fixture://quarantine/source",
            provisionalFichaPezId = fishId,
            approvedFichaPezId = fishId,
            consentId = consent.consentId,
            consentVersion = consent.consentVersion,
            submittedAtEpochMillis = 1_100,
            updatedAtEpochMillis = 1_900,
            status = ContributionStatus.APPROVED,
            rightsReviewState = ReviewState.APPROVED,
            privacyReviewState = ReviewState.APPROVED,
            labelReviewState = ReviewState.APPROVED,
            qualityReviewState = ReviewState.APPROVED,
            derivativeGroupId = "derivative-fixture-1",
            sourceGroupId = "source-fixture-1",
            sessionGroupId = "session-fixture-1",
            individualFishGroupId = "fish-fixture-1",
            contributorGroupId = "contributor-fixture-1"
        )
        val asset = TrainingAsset(
            trainingAssetId = "asset-fixture-1",
            sourceSubmissionId = submission.submissionId,
            sanitizedObjectReference = "fixture://sanitized/asset",
            approvedFichaPezId = fishId,
            sha256 = "a".repeat(64),
            derivativeGroupId = "derivative-fixture-1",
            sessionGroupId = "session-fixture-1",
            individualFishGroupId = "fish-fixture-1",
            contributorGroupId = "contributor-fixture-1",
            approvedAtEpochMillis = 2_000,
            eligibilityState = TrainingEligibilityState.ELIGIBLE
        )
        return Fixture(consent, submission, asset)
    }

    private fun loadManifestIds(): Set<String> {
        val workingDirectory = Paths.get(System.getProperty("user.dir"))
        val candidates = listOf(
            workingDirectory.resolve("src/main/assets/fish_classifier_manifest.json"),
            workingDirectory.resolve("app/src/main/assets/fish_classifier_manifest.json")
        )
        val path = candidates.firstOrNull(Files::isRegularFile)
            ?: error("Could not locate frozen classifier manifest")
        val manifest = JSONObject(String(Files.readAllBytes(path), StandardCharsets.UTF_8))
        val classes = manifest.getJSONArray("classes")
        return buildSet {
            repeat(classes.length()) { add(classes.getJSONObject(it).getString("fichaPezId")) }
        }
    }

    private data class Fixture(
        val consent: ContributionConsent,
        val submission: ContributionSubmission,
        val asset: TrainingAsset
    )
}

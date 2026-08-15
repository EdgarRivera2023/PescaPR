package com.bradmir.pescapr.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FishIdentifierEvaluationPolicyTest {
    private val ids = setOf("class-a", "class-b", "class-c")
    private val policy = FishIdentifierEvaluationPolicies.PROVISIONAL_V1.copy(
        supportedFichaPezIds = ids,
        minimumIndependentLockedTestGroupsPerClass = 2,
        preferredIndependentLockedTestGroupsPerClass = 3,
        minimumIndependentGroupsPerConfusionSlice = 3,
        minimumUnsupportedFishGroups = 3,
        minimumNonFishGroups = 3,
        confusionSlices = listOf(FishIdentifierConfusionSlice("similar", ids))
    )
    private val gate = FishIdentifierEvaluationGate()

    @Test fun `synthetic metrics satisfying every hard gate pass`() {
        assertEquals(FishIdentifierEvaluationDecisionStatus.PASS, evaluate().status)
    }

    @Test fun `top1 below hard minimum fails`() {
        assertFails(
            FishIdentifierEvaluationReason.TOP1_BELOW_MINIMUM,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(top1Accuracy = .84)
        )
    }

    @Test fun `top3 below hard minimum fails`() {
        assertFails(
            FishIdentifierEvaluationReason.TOP3_BELOW_MINIMUM,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(top3Accuracy = .96)
        )
    }

    @Test fun `strong overall accuracy cannot hide bad class recall`() {
        val locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).let { base ->
            base.copy(
                top1Accuracy = .99,
                classMetrics = base.classMetrics.map {
                    if (it.fichaPezId == "class-b") it.copy(recall = .40) else it
                }
            )
        }
        assertFails(FishIdentifierEvaluationReason.CLASS_RECALL_BELOW_MINIMUM, locked = locked)
    }

    @Test fun `failing confusion slice fails evaluation`() {
        val locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).let {
            it.copy(confusionSliceMetrics = it.confusionSliceMetrics.map { slice ->
                slice.copy(top1Accuracy = .60)
            })
        }
        assertFails(FishIdentifierEvaluationReason.CONFUSION_SLICE_FAILURE, locked = locked)
    }

    @Test fun `excessive OOD false acceptance fails`() {
        assertFails(
            FishIdentifierEvaluationReason.OOD_FALSE_ACCEPT_RATE_TOO_HIGH,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(
                oodFalseAcceptanceRate = .10
            )
        )
    }

    @Test fun `unsupported fish rejection weakness fails`() {
        assertFails(
            FishIdentifierEvaluationReason.UNSUPPORTED_FISH_REJECTION_FAILURE,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(
                unsupportedFishFalseAcceptanceRate = .08,
                unsupportedFishRejectionRate = .85
            )
        )
    }

    @Test fun `non fish rejection weakness fails`() {
        assertFails(
            FishIdentifierEvaluationReason.NON_FISH_REJECTION_FAILURE,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(
                nonFishFalseAcceptanceRate = .04,
                nonFishRejectionRate = .90
            )
        )
    }

    @Test fun `insufficient independent class coverage is insufficient evidence`() {
        val locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).let { base ->
            base.copy(classMetrics = base.classMetrics.map {
                if (it.fichaPezId == "class-c") it.copy(independentGroupCount = 1) else it
            })
        }
        val decision = evaluate(locked = locked)
        assertEquals(FishIdentifierEvaluationDecisionStatus.INSUFFICIENT_EVIDENCE, decision.status)
        assertTrue(FishIdentifierEvaluationReason.INSUFFICIENT_CLASS_COVERAGE in decision.reasons)
    }

    @Test fun `preferred target miss is advisory and does not fail`() {
        val decision = evaluate(locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(
            top1Accuracy = .87
        ))
        assertEquals(FishIdentifierEvaluationDecisionStatus.PASS, decision.status)
        assertTrue(FishIdentifierEvaluationAdvisory.TOP1_PREFERRED_TARGET_MISSED in decision.advisories)
    }

    @Test fun `locked test regression fails acceptable absolute metrics`() {
        val decision = evaluate(
            development = metrics(FishIdentifierEvaluationSplit.DEVELOPMENT_VALIDATION).copy(
                top1Accuracy = .96
            ),
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(top1Accuracy = .88)
        )
        assertEquals(FishIdentifierEvaluationDecisionStatus.FAIL, decision.status)
        assertTrue(FishIdentifierEvaluationReason.LOCKED_TEST_REGRESSION in decision.reasons)
    }

    @Test fun `policy and artifact versions are preserved in decision`() {
        val decision = evaluate()
        assertEquals(binding(), decision.binding)
        assertEquals(policy.policyVersion, decision.binding.evaluationPolicyVersion)
    }

    @Test fun `reordering metric records does not change decision`() {
        val original = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST)
        val reordered = original.copy(
            classMetrics = original.classMetrics.reversed(),
            confusionSliceMetrics = original.confusionSliceMetrics.reversed()
        )
        assertEquals(evaluate(locked = original), evaluate(locked = reordered))
    }

    @Test fun `duplicate class metrics are rejected explicitly`() {
        val locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).let {
            it.copy(classMetrics = it.classMetrics + it.classMetrics.first().copy(recall = .10))
        }
        val decision = evaluate(locked = locked)
        assertEquals(FishIdentifierEvaluationDecisionStatus.INVALID_INPUT, decision.status)
        assertTrue(FishIdentifierEvaluationReason.DUPLICATE_CLASS_METRIC in decision.reasons)
    }

    @Test fun `calibration failure is a hard gate`() {
        assertFails(
            FishIdentifierEvaluationReason.CALIBRATION_FAILURE,
            locked = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST).copy(
                expectedCalibrationError = .08
            )
        )
    }

    @Test fun `policy defines six required confusion slices for frozen catalog`() {
        val production = FishIdentifierEvaluationPolicies.PROVISIONAL_V1
        assertEquals(39, production.supportedFichaPezIds.size)
        assertEquals(
            setOf("snappers", "groupers", "amberjacks", "mackerels", "barracudas", "boxfish"),
            production.confusionSlices.map { it.sliceId }.toSet()
        )
    }

    @Test fun `FI-A8 production threshold remains unselected`() {
        val decision = evaluate()
        assertEquals("UNSELECTED", decision.binding.thresholdPolicyVersion)
        assertFalse(decision.binding.thresholdPolicyVersion.any(Char::isDigit))
    }

    private fun evaluate(
        development: FishIdentifierMeasuredMetrics = metrics(
            FishIdentifierEvaluationSplit.DEVELOPMENT_VALIDATION
        ),
        locked: FishIdentifierMeasuredMetrics = metrics(FishIdentifierEvaluationSplit.LOCKED_TEST)
    ) = gate.evaluate(policy, FishIdentifierEvaluationEvidence(binding(), development, locked))

    private fun assertFails(
        reason: FishIdentifierEvaluationReason,
        locked: FishIdentifierMeasuredMetrics
    ) {
        val decision = evaluate(locked = locked)
        assertEquals(FishIdentifierEvaluationDecisionStatus.FAIL, decision.status)
        assertTrue(reason in decision.reasons)
    }

    private fun binding() = FishIdentifierEvaluationBinding(
        evaluationPolicyVersion = policy.policyVersion,
        modelVersion = "synthetic-model-v1",
        classifierManifestVersion = policy.classifierManifestVersion,
        datasetSnapshotVersion = "synthetic-snapshot-v1",
        thresholdPolicyVersion = "UNSELECTED"
    )

    private fun metrics(split: FishIdentifierEvaluationSplit) = FishIdentifierMeasuredMetrics(
        split = split,
        top1Accuracy = .92,
        top3Accuracy = .99,
        macroRecall = .90,
        macroPrecision = .90,
        supportedAcceptanceRate = .88,
        supportedAmbiguousRate = .08,
        oodFalseAcceptanceRate = .01,
        unsupportedFishFalseAcceptanceRate = .01,
        unsupportedFishRejectionRate = .97,
        nonFishFalseAcceptanceRate = .005,
        nonFishRejectionRate = .99,
        expectedCalibrationError = .02,
        unsupportedFishIndependentGroupCount = 3,
        nonFishIndependentGroupCount = 3,
        classMetrics = ids.map { FishIdentifierClassMetric(it, .85, .85, 3) },
        confusionSliceMetrics = listOf(
            FishIdentifierConfusionSliceMetric("similar", .86, .99, .04, 3)
        )
    )
}

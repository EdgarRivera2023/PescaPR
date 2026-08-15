package com.bradmir.pescapr.data

data class MinimumMetricTarget(val hardMinimum: Double, val preferred: Double)
data class MaximumMetricTarget(val hardMaximum: Double, val preferred: Double)

data class FishIdentifierConfusionSlice(
    val sliceId: String,
    val fichaPezIds: Set<String>
)

data class FishIdentifierEvaluationPolicy(
    val policyVersion: String,
    val classifierManifestVersion: String,
    val supportedFichaPezIds: Set<String>,
    val top1Accuracy: MinimumMetricTarget,
    val top3Accuracy: MinimumMetricTarget,
    val macroRecall: MinimumMetricTarget,
    val macroPrecision: MinimumMetricTarget,
    val perClassRecall: MinimumMetricTarget,
    val perClassPrecision: MinimumMetricTarget,
    val supportedAmbiguousRate: MaximumMetricTarget,
    val oodFalseAcceptanceRate: MaximumMetricTarget,
    val unsupportedFishFalseAcceptanceRate: MaximumMetricTarget,
    val unsupportedFishRejectionRate: MinimumMetricTarget,
    val nonFishFalseAcceptanceRate: MaximumMetricTarget,
    val nonFishRejectionRate: MinimumMetricTarget,
    val expectedCalibrationError: MaximumMetricTarget,
    val confusionSliceTop1Accuracy: MinimumMetricTarget,
    val confusionSliceTop3Accuracy: MinimumMetricTarget,
    val confusionSliceIncorrectAcceptanceRate: MaximumMetricTarget,
    val minimumIndependentLockedTestGroupsPerClass: Int,
    val preferredIndependentLockedTestGroupsPerClass: Int,
    val minimumIndependentGroupsPerConfusionSlice: Int,
    val minimumUnsupportedFishGroups: Int,
    val minimumNonFishGroups: Int,
    val maximumLockedTestTop1Regression: Double,
    val confusionSlices: List<FishIdentifierConfusionSlice>
)

enum class FishIdentifierEvaluationSplit { DEVELOPMENT_VALIDATION, LOCKED_TEST }

data class FishIdentifierClassMetric(
    val fichaPezId: String,
    val recall: Double,
    val precision: Double,
    val independentGroupCount: Int
)

data class FishIdentifierConfusionSliceMetric(
    val sliceId: String,
    val top1Accuracy: Double,
    val top3Accuracy: Double,
    val incorrectAcceptedRate: Double,
    val independentGroupCount: Int
)

data class FishIdentifierMeasuredMetrics(
    val split: FishIdentifierEvaluationSplit,
    val top1Accuracy: Double,
    val top3Accuracy: Double,
    val macroRecall: Double,
    val macroPrecision: Double,
    val supportedAcceptanceRate: Double,
    val supportedAmbiguousRate: Double,
    val oodFalseAcceptanceRate: Double,
    val unsupportedFishFalseAcceptanceRate: Double,
    val unsupportedFishRejectionRate: Double,
    val nonFishFalseAcceptanceRate: Double,
    val nonFishRejectionRate: Double,
    val expectedCalibrationError: Double,
    val unsupportedFishIndependentGroupCount: Int,
    val nonFishIndependentGroupCount: Int,
    val classMetrics: List<FishIdentifierClassMetric>,
    val confusionSliceMetrics: List<FishIdentifierConfusionSliceMetric>
)

data class FishIdentifierEvaluationBinding(
    val evaluationPolicyVersion: String,
    val modelVersion: String,
    val classifierManifestVersion: String,
    val datasetSnapshotVersion: String,
    val thresholdPolicyVersion: String
)

data class FishIdentifierEvaluationEvidence(
    val binding: FishIdentifierEvaluationBinding,
    val developmentValidation: FishIdentifierMeasuredMetrics,
    val lockedTest: FishIdentifierMeasuredMetrics
)

enum class FishIdentifierEvaluationDecisionStatus { PASS, FAIL, INSUFFICIENT_EVIDENCE, INVALID_INPUT }

enum class FishIdentifierEvaluationReason {
    POLICY_VERSION_MISMATCH,
    CLASSIFIER_MANIFEST_VERSION_MISMATCH,
    SPLIT_MISMATCH,
    DUPLICATE_CLASS_METRIC,
    DUPLICATE_CONFUSION_SLICE_METRIC,
    CLASS_METRIC_SET_MISMATCH,
    CONFUSION_SLICE_SET_MISMATCH,
    METRIC_OUT_OF_RANGE,
    TOP1_BELOW_MINIMUM,
    TOP3_BELOW_MINIMUM,
    MACRO_RECALL_BELOW_MINIMUM,
    MACRO_PRECISION_BELOW_MINIMUM,
    CLASS_RECALL_BELOW_MINIMUM,
    CLASS_PRECISION_BELOW_MINIMUM,
    CONFUSION_SLICE_FAILURE,
    OOD_FALSE_ACCEPT_RATE_TOO_HIGH,
    UNSUPPORTED_FISH_REJECTION_FAILURE,
    NON_FISH_REJECTION_FAILURE,
    AMBIGUOUS_RATE_TOO_HIGH,
    CALIBRATION_FAILURE,
    INSUFFICIENT_CLASS_COVERAGE,
    INSUFFICIENT_CONFUSION_SLICE_COVERAGE,
    INSUFFICIENT_OOD_COVERAGE,
    LOCKED_TEST_REGRESSION
}

enum class FishIdentifierEvaluationAdvisory {
    TOP1_PREFERRED_TARGET_MISSED,
    TOP3_PREFERRED_TARGET_MISSED,
    MACRO_PREFERRED_TARGET_MISSED,
    CLASS_PREFERRED_TARGET_MISSED,
    REJECTION_PREFERRED_TARGET_MISSED,
    CALIBRATION_PREFERRED_TARGET_MISSED,
    CLASS_COVERAGE_PREFERRED_TARGET_MISSED
}

data class FishIdentifierEvaluationDecision(
    val status: FishIdentifierEvaluationDecisionStatus,
    val binding: FishIdentifierEvaluationBinding,
    val reasons: Set<FishIdentifierEvaluationReason>,
    val advisories: Set<FishIdentifierEvaluationAdvisory>
)

class FishIdentifierEvaluationGate {
    fun evaluate(
        policy: FishIdentifierEvaluationPolicy,
        evidence: FishIdentifierEvaluationEvidence
    ): FishIdentifierEvaluationDecision {
        val reasons = linkedSetOf<FishIdentifierEvaluationReason>()
        val advisories = linkedSetOf<FishIdentifierEvaluationAdvisory>()
        validateStructure(policy, evidence, reasons)
        if (reasons.any { it in invalidReasons }) {
            return decision(FishIdentifierEvaluationDecisionStatus.INVALID_INPUT, evidence, reasons, advisories)
        }

        evaluateLockedTest(policy, evidence.lockedTest, reasons, advisories)
        if (evidence.developmentValidation.top1Accuracy - evidence.lockedTest.top1Accuracy >
            policy.maximumLockedTestTop1Regression + 1e-12
        ) reasons += FishIdentifierEvaluationReason.LOCKED_TEST_REGRESSION

        val status = when {
            reasons.any { it in insufficientEvidenceReasons } ->
                FishIdentifierEvaluationDecisionStatus.INSUFFICIENT_EVIDENCE
            reasons.isNotEmpty() -> FishIdentifierEvaluationDecisionStatus.FAIL
            else -> FishIdentifierEvaluationDecisionStatus.PASS
        }
        return decision(status, evidence, reasons, advisories)
    }

    private fun validateStructure(
        policy: FishIdentifierEvaluationPolicy,
        evidence: FishIdentifierEvaluationEvidence,
        reasons: MutableSet<FishIdentifierEvaluationReason>
    ) {
        if (evidence.binding.evaluationPolicyVersion != policy.policyVersion) {
            reasons += FishIdentifierEvaluationReason.POLICY_VERSION_MISMATCH
        }
        if (evidence.binding.classifierManifestVersion != policy.classifierManifestVersion) {
            reasons += FishIdentifierEvaluationReason.CLASSIFIER_MANIFEST_VERSION_MISMATCH
        }
        if (evidence.developmentValidation.split != FishIdentifierEvaluationSplit.DEVELOPMENT_VALIDATION ||
            evidence.lockedTest.split != FishIdentifierEvaluationSplit.LOCKED_TEST
        ) reasons += FishIdentifierEvaluationReason.SPLIT_MISMATCH
        for (metrics in listOf(evidence.developmentValidation, evidence.lockedTest)) {
            if (metrics.classMetrics.map { it.fichaPezId }.size !=
                metrics.classMetrics.map { it.fichaPezId }.toSet().size
            ) reasons += FishIdentifierEvaluationReason.DUPLICATE_CLASS_METRIC
            if (metrics.confusionSliceMetrics.map { it.sliceId }.size !=
                metrics.confusionSliceMetrics.map { it.sliceId }.toSet().size
            ) reasons += FishIdentifierEvaluationReason.DUPLICATE_CONFUSION_SLICE_METRIC
            if (metrics.classMetrics.map { it.fichaPezId }.toSet() != policy.supportedFichaPezIds) {
                reasons += FishIdentifierEvaluationReason.CLASS_METRIC_SET_MISMATCH
            }
            if (metrics.confusionSliceMetrics.map { it.sliceId }.toSet() !=
                policy.confusionSlices.map { it.sliceId }.toSet()
            ) reasons += FishIdentifierEvaluationReason.CONFUSION_SLICE_SET_MISMATCH
            if (!allRates(metrics).all { it in 0.0..1.0 }) {
                reasons += FishIdentifierEvaluationReason.METRIC_OUT_OF_RANGE
            }
        }
    }

    private fun evaluateLockedTest(
        policy: FishIdentifierEvaluationPolicy,
        metrics: FishIdentifierMeasuredMetrics,
        reasons: MutableSet<FishIdentifierEvaluationReason>,
        advisories: MutableSet<FishIdentifierEvaluationAdvisory>
    ) {
        minimum(metrics.top1Accuracy, policy.top1Accuracy,
            FishIdentifierEvaluationReason.TOP1_BELOW_MINIMUM,
            FishIdentifierEvaluationAdvisory.TOP1_PREFERRED_TARGET_MISSED, reasons, advisories)
        minimum(metrics.top3Accuracy, policy.top3Accuracy,
            FishIdentifierEvaluationReason.TOP3_BELOW_MINIMUM,
            FishIdentifierEvaluationAdvisory.TOP3_PREFERRED_TARGET_MISSED, reasons, advisories)
        minimum(metrics.macroRecall, policy.macroRecall,
            FishIdentifierEvaluationReason.MACRO_RECALL_BELOW_MINIMUM,
            FishIdentifierEvaluationAdvisory.MACRO_PREFERRED_TARGET_MISSED, reasons, advisories)
        minimum(metrics.macroPrecision, policy.macroPrecision,
            FishIdentifierEvaluationReason.MACRO_PRECISION_BELOW_MINIMUM,
            FishIdentifierEvaluationAdvisory.MACRO_PREFERRED_TARGET_MISSED, reasons, advisories)
        if (metrics.classMetrics.any { it.recall < policy.perClassRecall.hardMinimum }) {
            reasons += FishIdentifierEvaluationReason.CLASS_RECALL_BELOW_MINIMUM
        }
        if (metrics.classMetrics.any { it.precision < policy.perClassPrecision.hardMinimum }) {
            reasons += FishIdentifierEvaluationReason.CLASS_PRECISION_BELOW_MINIMUM
        }
        if (metrics.classMetrics.any {
                it.recall < policy.perClassRecall.preferred || it.precision < policy.perClassPrecision.preferred
            }
        ) advisories += FishIdentifierEvaluationAdvisory.CLASS_PREFERRED_TARGET_MISSED
        if (metrics.classMetrics.any { it.independentGroupCount < policy.minimumIndependentLockedTestGroupsPerClass }) {
            reasons += FishIdentifierEvaluationReason.INSUFFICIENT_CLASS_COVERAGE
        }
        if (metrics.classMetrics.any { it.independentGroupCount < policy.preferredIndependentLockedTestGroupsPerClass }) {
            advisories += FishIdentifierEvaluationAdvisory.CLASS_COVERAGE_PREFERRED_TARGET_MISSED
        }
        if (metrics.confusionSliceMetrics.any {
                it.top1Accuracy < policy.confusionSliceTop1Accuracy.hardMinimum ||
                    it.top3Accuracy < policy.confusionSliceTop3Accuracy.hardMinimum ||
                    it.incorrectAcceptedRate > policy.confusionSliceIncorrectAcceptanceRate.hardMaximum
            }
        ) reasons += FishIdentifierEvaluationReason.CONFUSION_SLICE_FAILURE
        if (metrics.confusionSliceMetrics.any {
                it.independentGroupCount < policy.minimumIndependentGroupsPerConfusionSlice
            }
        ) reasons += FishIdentifierEvaluationReason.INSUFFICIENT_CONFUSION_SLICE_COVERAGE
        maximum(metrics.oodFalseAcceptanceRate, policy.oodFalseAcceptanceRate,
            FishIdentifierEvaluationReason.OOD_FALSE_ACCEPT_RATE_TOO_HIGH,
            FishIdentifierEvaluationAdvisory.REJECTION_PREFERRED_TARGET_MISSED, reasons, advisories)
        if (metrics.unsupportedFishFalseAcceptanceRate > policy.unsupportedFishFalseAcceptanceRate.hardMaximum ||
            metrics.unsupportedFishRejectionRate < policy.unsupportedFishRejectionRate.hardMinimum
        ) reasons += FishIdentifierEvaluationReason.UNSUPPORTED_FISH_REJECTION_FAILURE
        if (metrics.nonFishFalseAcceptanceRate > policy.nonFishFalseAcceptanceRate.hardMaximum ||
            metrics.nonFishRejectionRate < policy.nonFishRejectionRate.hardMinimum
        ) reasons += FishIdentifierEvaluationReason.NON_FISH_REJECTION_FAILURE
        maximum(metrics.supportedAmbiguousRate, policy.supportedAmbiguousRate,
            FishIdentifierEvaluationReason.AMBIGUOUS_RATE_TOO_HIGH,
            FishIdentifierEvaluationAdvisory.REJECTION_PREFERRED_TARGET_MISSED, reasons, advisories)
        maximum(metrics.expectedCalibrationError, policy.expectedCalibrationError,
            FishIdentifierEvaluationReason.CALIBRATION_FAILURE,
            FishIdentifierEvaluationAdvisory.CALIBRATION_PREFERRED_TARGET_MISSED, reasons, advisories)
        if (metrics.unsupportedFishIndependentGroupCount < policy.minimumUnsupportedFishGroups ||
            metrics.nonFishIndependentGroupCount < policy.minimumNonFishGroups
        ) reasons += FishIdentifierEvaluationReason.INSUFFICIENT_OOD_COVERAGE
    }

    private fun minimum(value: Double, target: MinimumMetricTarget,
        failure: FishIdentifierEvaluationReason, advisory: FishIdentifierEvaluationAdvisory,
        reasons: MutableSet<FishIdentifierEvaluationReason>,
        advisories: MutableSet<FishIdentifierEvaluationAdvisory>
    ) { if (value < target.hardMinimum) reasons += failure else if (value < target.preferred) advisories += advisory }

    private fun maximum(value: Double, target: MaximumMetricTarget,
        failure: FishIdentifierEvaluationReason, advisory: FishIdentifierEvaluationAdvisory,
        reasons: MutableSet<FishIdentifierEvaluationReason>,
        advisories: MutableSet<FishIdentifierEvaluationAdvisory>
    ) { if (value > target.hardMaximum) reasons += failure else if (value > target.preferred) advisories += advisory }

    private fun allRates(metrics: FishIdentifierMeasuredMetrics): List<Double> = listOf(
        metrics.top1Accuracy, metrics.top3Accuracy, metrics.macroRecall, metrics.macroPrecision,
        metrics.supportedAcceptanceRate, metrics.supportedAmbiguousRate, metrics.oodFalseAcceptanceRate,
        metrics.unsupportedFishFalseAcceptanceRate, metrics.unsupportedFishRejectionRate,
        metrics.nonFishFalseAcceptanceRate, metrics.nonFishRejectionRate, metrics.expectedCalibrationError
    ) + metrics.classMetrics.flatMap { listOf(it.recall, it.precision) } +
        metrics.confusionSliceMetrics.flatMap { listOf(it.top1Accuracy, it.top3Accuracy, it.incorrectAcceptedRate) }

    private fun decision(status: FishIdentifierEvaluationDecisionStatus,
        evidence: FishIdentifierEvaluationEvidence, reasons: Set<FishIdentifierEvaluationReason>,
        advisories: Set<FishIdentifierEvaluationAdvisory>
    ) = FishIdentifierEvaluationDecision(status, evidence.binding, reasons.toSet(), advisories.toSet())

    private companion object {
        val invalidReasons = setOf(
            FishIdentifierEvaluationReason.POLICY_VERSION_MISMATCH,
            FishIdentifierEvaluationReason.CLASSIFIER_MANIFEST_VERSION_MISMATCH,
            FishIdentifierEvaluationReason.SPLIT_MISMATCH,
            FishIdentifierEvaluationReason.DUPLICATE_CLASS_METRIC,
            FishIdentifierEvaluationReason.DUPLICATE_CONFUSION_SLICE_METRIC,
            FishIdentifierEvaluationReason.CLASS_METRIC_SET_MISMATCH,
            FishIdentifierEvaluationReason.CONFUSION_SLICE_SET_MISMATCH,
            FishIdentifierEvaluationReason.METRIC_OUT_OF_RANGE
        )
        val insufficientEvidenceReasons = setOf(
            FishIdentifierEvaluationReason.INSUFFICIENT_CLASS_COVERAGE,
            FishIdentifierEvaluationReason.INSUFFICIENT_CONFUSION_SLICE_COVERAGE,
            FishIdentifierEvaluationReason.INSUFFICIENT_OOD_COVERAGE
        )
    }
}

object FishIdentifierEvaluationPolicies {
    /** Provisional engineering gate; no target is claimed as achieved. */
    val PROVISIONAL_V1 = FishIdentifierEvaluationPolicy(
        policyVersion = "fish-identifier-evaluation-policy-v1",
        classifierManifestVersion = "1.0.0",
        supportedFichaPezIds = setOf(
            "0SCZ4miCcNiVY684bCwg", "0hjc5oWRBLg9dyujatSy", "1s69lkvuYEYoQkL6esVp",
            "3qAJ1d8NdETc7HAsvJtg", "5SkWhUQgG6JuanpfSLUT", "AYGYpLjkS9LEv7AXVAuk",
            "CIfxxdN70JcakCqA0IxY", "Hjr9sFSdUEW1RVpR09mV", "IBd1JsryE7jTh1tpyCN8",
            "J4JKaRrOWzuHfxq9ihKM", "MjsvzQbyRzyWqtjGYSBM", "NZjA3AcJ6gb2ddsUNOPw",
            "OF8bIJWNGjtMOJnLeVgf", "PM6TqnpdmFQMMDQFoQAy", "PtbnNsBSRDwJzqGwvmv9",
            "RO2iuTVLAX11dy3aNgdf", "SQ7eid3h0Fk2ToVnnqm6", "TvWu2VyCwDofI4RfOmmU",
            "V39KoLAZkg0MBjiWaa46", "VL21Dl6MaY4SDJkmvIIz", "WSIwTi77Bdy2KEUtE26k",
            "WaSqNZuItzfXizCKyei7", "XTLHUX6xHya0BOisyR6E", "Ya1VhjdpdBABqWClLAnW",
            "bUROGweaABz6GRUedycl", "cEgkcDR0JUI8GdxEx5LA", "eBZEv2F3RUvtST6fx0cK",
            "ejX0Cx9YYxsmOQTJb8kK", "fZog3T6cou99saWzsQyE", "giJGGBQxEBmzjTMhZsPg",
            "iOXBIJjjwRw2FELFnSe1", "oH2T6KbHxVuRYK0EgI5D", "osXhShrxuuFLdr0ftgmb",
            "pFJ38O9TeYjWUt2n8XRS", "pS0UEezomaklOqZsflTt", "ptq705ot5CnYod63Xs8E",
            "qDlhElFdSz5UOHDkU8Pe", "u1JpMvcENOy98cd31Za5", "wk7kjNwc7FzD9WT3c3Ui"
        ),
        top1Accuracy = MinimumMetricTarget(.85, .90), top3Accuracy = MinimumMetricTarget(.97, .99),
        macroRecall = MinimumMetricTarget(.82, .88), macroPrecision = MinimumMetricTarget(.82, .88),
        perClassRecall = MinimumMetricTarget(.70, .80), perClassPrecision = MinimumMetricTarget(.70, .80),
        supportedAmbiguousRate = MaximumMetricTarget(.20, .12),
        oodFalseAcceptanceRate = MaximumMetricTarget(.05, .02),
        unsupportedFishFalseAcceptanceRate = MaximumMetricTarget(.05, .02),
        unsupportedFishRejectionRate = MinimumMetricTarget(.90, .95),
        nonFishFalseAcceptanceRate = MaximumMetricTarget(.02, .01),
        nonFishRejectionRate = MinimumMetricTarget(.95, .98),
        expectedCalibrationError = MaximumMetricTarget(.05, .03),
        confusionSliceTop1Accuracy = MinimumMetricTarget(.75, .85),
        confusionSliceTop3Accuracy = MinimumMetricTarget(.95, .98),
        confusionSliceIncorrectAcceptanceRate = MaximumMetricTarget(.10, .05),
        minimumIndependentLockedTestGroupsPerClass = 30,
        preferredIndependentLockedTestGroupsPerClass = 50,
        minimumIndependentGroupsPerConfusionSlice = 60,
        minimumUnsupportedFishGroups = 200, minimumNonFishGroups = 200,
        maximumLockedTestTop1Regression = .05,
        confusionSlices = listOf(
            FishIdentifierConfusionSlice("snappers", setOf("3qAJ1d8NdETc7HAsvJtg", "J4JKaRrOWzuHfxq9ihKM", "NZjA3AcJ6gb2ddsUNOPw", "VL21Dl6MaY4SDJkmvIIz", "WaSqNZuItzfXizCKyei7", "XTLHUX6xHya0BOisyR6E", "eBZEv2F3RUvtST6fx0cK", "fZog3T6cou99saWzsQyE", "giJGGBQxEBmzjTMhZsPg", "oH2T6KbHxVuRYK0EgI5D")),
            FishIdentifierConfusionSlice("groupers", setOf("1s69lkvuYEYoQkL6esVp", "CIfxxdN70JcakCqA0IxY", "SQ7eid3h0Fk2ToVnnqm6", "TvWu2VyCwDofI4RfOmmU", "WSIwTi77Bdy2KEUtE26k", "pFJ38O9TeYjWUt2n8XRS", "pS0UEezomaklOqZsflTt", "wk7kjNwc7FzD9WT3c3Ui")),
            FishIdentifierConfusionSlice("amberjacks", setOf("iOXBIJjjwRw2FELFnSe1", "u1JpMvcENOy98cd31Za5")),
            FishIdentifierConfusionSlice("mackerels", setOf("0SCZ4miCcNiVY684bCwg", "OF8bIJWNGjtMOJnLeVgf", "V39KoLAZkg0MBjiWaa46")),
            FishIdentifierConfusionSlice("barracudas", setOf("PM6TqnpdmFQMMDQFoQAy", "Ya1VhjdpdBABqWClLAnW", "ejX0Cx9YYxsmOQTJb8kK")),
            FishIdentifierConfusionSlice("boxfish", setOf("0hjc5oWRBLg9dyujatSy", "Hjr9sFSdUEW1RVpR09mV", "IBd1JsryE7jTh1tpyCN8", "bUROGweaABz6GRUedycl", "qDlhElFdSz5UOHDkU8Pe"))
        )
    )
}

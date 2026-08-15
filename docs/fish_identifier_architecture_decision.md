# Fish Identifier mobile architecture decision

**Roadmap:** FI-B.1  
**Policy status:** selected for the first controlled 39-class experiment; not production approval  
**Decision version:** `fish-identifier-architecture-decision-v1`  
**Date:** 2026-08-15

## 1. Decision in brief

Use **MobileNetV3-Large at an initial 224 x 224 input** as the preferred backbone for the first
controlled 39-class transfer-learning experiment. Use **EfficientNet-Lite0 at 224 x 224** as the
fallback/comparator if MobileNetV3-Large misses FI-A.9 accuracy, confusion, export, latency, memory,
or quantization requirements. Keep MobileNetV2 as a reproducibility baseline, not as the preferred
production-oriented candidate.

This is an experiment ordering decision. It does not select a trained model, Android runtime,
confidence threshold, input preprocessing contract, or production architecture permanently.

## 2. Constraints and decision criteria

The future model must classify the frozen 39 Guía Oficial classes and emit ordinary ranked class
scores/logits that can enter the existing FI-A.8 evaluation schema. It must run fully offline on
ordinary modern Android phones, preserve the ordered `FichaPez.id` manifest, and leave sufficient
compute and memory headroom for camera/UI work. Guía Oficial remains authoritative for names,
regulations, aliases, characteristics, and display data.

Architecture comparisons are judged on transfer-learning suitability, expected accuracy/feature
capacity, parameter and compute scale, memory and latency potential, quantization/export support,
Android deployment maturity, reputable pretrained weights, implementation complexity, and
reproducibility. ImageNet results and vendor latency figures are context only; they are not PescaPR
accuracy or device guarantees and are not directly comparable across sources.

## 3. Candidates

| Candidate | Role | Strengths | Risks / reason it is not the first choice |
|---|---|---|---|
| **MobileNetV3-Large, 224** | Preferred first 39-class experiment | Designed with mobile hardware-aware search; the Large variant offers more feature capacity than Small for body markings, fins, and head detail while retaining a mobile-oriented operator set. Keras provides a standard ImageNet-pretrained implementation. | Actual field accuracy, quantized accuracy, and latency on PescaPR devices are unknown. Large may exceed the lower device budget; verify rather than assume. |
| **EfficientNet-Lite0, 224** | Fallback and controlled comparator | Explicitly designed for mobile/IoT; the official TensorFlow model card/checkpoint reports mobile CPU/GPU/EdgeTPU variants and post-training INT8 paths. | Its published numbers use Pixel 4 and a particular checkpoint/converter; they do not predict PescaPR results. Export/checkpoint tooling must be pinned and reproduced. |
| **MobileNetV2, 224** | Reproducibility baseline | Mature depthwise-separable design, conventional transfer-learning workflow, broad implementation familiarity, and a useful lower-complexity reference for diagnosing data/pipeline problems. | Older accuracy/efficiency tradeoff than V3 in the original mobile comparison; lower feature capacity may expose fine-grained confusion. |

MobileNetV3-Small is deliberately not the first candidate: it remains a later size/latency probe if
Large and Lite0 are too expensive. Larger EfficientNet variants, server-oriented CNNs, and mobile
transformer/NAS surveys are out of scope until evidence justifies them.

## 4. Why MobileNetV3-Large is first

The MobileNetV3 paper describes hardware-aware search and NetAdapt for mobile targets and reports
the Large/Small variants as mobile classification models. That is relevant architectural evidence,
not a PescaPR benchmark. Large is selected over Small because species discrimination can depend on
small markings, fin structure, coloration, and head shape; the first experiment should test useful
capacity before optimizing for the smallest possible model. It is selected over MobileNetV2 because
V2 is retained as a baseline while V3 is the more current member of the same deployment-friendly
family. EfficientNet-Lite0 remains a serious comparator with a documented edge focus.

This choice must be revisited if the first controlled run shows a class/confusion failure under
FI-A.9, unacceptable lower/mid-range latency or memory, materially worse quantized accuracy, export
operator incompatibility, or no meaningful advantage over Lite0/V2 after group-safe evaluation.

## 5. Input-resolution hypothesis

Start the controlled comparison at **224 x 224**, with a narrow later resolution probe of **192 and
256** only if the approved data supports it. Preserve the fish body and diagnostic context using a
single versioned resize/crop/letterbox contract; do not silently change preprocessing between
architectures. A larger input may retain useful markings but increases compute and memory, while a
smaller input may erase fine species evidence. Resolution is therefore an experiment variable and
must be judged by FI-A.9 per-class/confusion metrics plus device benchmarks, not by speed alone.

## 6. Transfer-learning plan for later FI-B.2

Use reputable ImageNet-pretrained weights for the selected backbone, replace the classifier head with
39 outputs in the frozen manifest order, and emit the ordinary score vector required by FI-A.8.
Begin with a frozen-backbone/head-only stage, then consider low-learning-rate unfreezing of later
backbone blocks only when validation evidence shows stable underfitting. All preprocessing,
checkpoint, random seed, model version, and manifest checksums must be recorded. The limited,
incomplete dataset makes transfer learning preferable to random initialization, but it does not
remove the need for independent groups, rights approval, or locked-TEST discipline.

The existing three-class micro-POC document may still use MobileNetV2 as its deliberately simple
pipeline baseline. That is a scoped reproducibility experiment and does not override this 39-class
FI-B.1 ordering decision.

## 7. Quantization and runtime neutrality

Evaluate a float32 baseline first, then compare float16 and full-integer INT8 exports when a real
candidate model exists. Record model size, output agreement, FI-A.9 metrics, latency, peak memory,
and hardware-delegate behavior for each variant. Quantization is not selected from theoretical size
savings: an INT8 model must retain acceptable per-class, confusion-slice, OOD, and calibration
behavior.

The decision is about a neural-network family, not a permanent Android runtime. TensorFlow/LiteRT is
the expected future export direction because the candidates have established mobile-oriented paths,
but this slice adds no runtime dependency and no model artifact. Any future runtime must preserve the
39-output ordering and ordinary ranked scores.

## 8. Android benchmark plan

Once an actual exported model exists, benchmark identical preprocessing and single-image inference
on at least:

1. a representative lower/mid-range Android phone;
2. a current mainstream Android phone; and
3. the available Samsung device, if it represents a distinct relevant device tier.

Measure cold and warmed inference latency, p50/p95 latency, peak memory, model file size, startup
cost, battery/thermal behavior for a short controlled run, CPU-only behavior, and available GPU/NN
delegate behavior. Report Android OS, chipset, delegate, thread count, input resolution, precision,
and build/runtime versions. Do not infer device performance from Pixel 4 or desktop benchmarks.

## 9. FI-A.8 and FI-A.9 relationship

The selected backbone must produce ranked scores/logits that can feed FI-A.8 without choosing its
rejection threshold. The future candidate is acceptable only when it satisfies the versioned FI-A.9
gates: top-1/top-3, every-class and macro metrics, six confusion slices, unsupported-fish/non-fish
rejection, ambiguity, calibration, independent locked-TEST coverage, and locked-TEST regression.
Android size/latency/memory are additional engineering gates; ImageNet accuracy alone cannot pass
the decision.

## 10. Explicit non-goals and revisit triggers

This record does not train, download data, acquire images, export a model, add LiteRT/TFLite or other
Android inference dependencies, modify the 39-class manifest, select an FI-A.8 threshold, change
Identifier UI, or restart FI-CONTRIB. It does not create a dataset or claim production readiness.

Revisit the decision through a new version if approved data reveals a different capacity/regularity
need, FI-A.9 confusion or OOD gates fail, target-device measurements violate the app budget,
quantization materially harms safety metrics, or the chosen export/runtime path loses support.

## 11. Primary sources

- MobileNetV3 paper: [Searching for MobileNetV3](https://arxiv.org/abs/1905.02244) (hardware-aware mobile design and Large/Small family).
- TensorFlow Keras API: [MobileNetV3 and MobileNet application models](https://www.tensorflow.org/api_docs/python/tf/keras/applications) (official pretrained implementation surface).
- TensorFlow edge model documentation: [EfficientNet-Lite model card and checkpoints](https://github.com/tensorflow/tpu/tree/master/models/official/efficientnet/lite) (edge variants, published reference measurements, and INT8 export paths).
- Google AI Edge: [LiteRT Android and delegate documentation](https://ai.google.dev/edge/litert) (future Android runtime/delegate considerations).


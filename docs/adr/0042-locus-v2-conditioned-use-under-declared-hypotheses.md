# ADR 0042 — Locus V2 conditioned use under declared hypotheses

- Status: **PROPOSED — PENDING AUTHOR REVIEW**. Nothing in this ADR is approved, and no
  implementation of it is authorized. `selfApproved=false`.
- Date: 2026-11-21.
- Authorization of the design work: author instruction of 2026-11-21, «Ejecución secuencial
  tras R1», block 3A. The author decided that the capability is to be designed completely,
  for presentation and for consumption; the instruction states that the recipe of §10.4 of
  the R1 report is not approved literally.
- Base: `9ccefdcfe922d8c0e329bdeec30c12fc058de463` plus the post-R1 repairs of the branch
  `maintenance/post-r1-locus-repairs-and-conditioned-use`.
- Design: [architecture and source analysis](../architecture/post_r1_locus_conditioned_use_design.md).
- Candidate contract: [conditioned use](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md).
- Author decisions: [decision sheet](../validation/post_r1_conditioned_use_author_decision_sheet.md).

## Context

A Locus V2 can be published with a faithful reconstruction slice and no certified
component. Its existence structure is then `NOT_ESTABLISHED`, it is drawn as the sampled
runs of the exploratory evidence, and every consumer that needs a component refuses it:
`Point(L, u)` fails with `OUTSIDE_COMPONENTS`, a length between positions is not available,
and an intersection publishes no solution. That refusal conforms to
`locus-v2-existence-components.md` §15.8. The same battery works on a `COMPLETE` locus, where
consumers already publish results of lower grades on other axes (`ESTIMATED` lengths,
`VERIFIED_UNCERTIFIED` intersections).

The existence axis is therefore the only one without a usable grade between exploratory
evidence and a certificate. The nature `NUMERICAL_UNDER_HYPOTHESES` is named in §15.1 and in
`LocusEvidenceNature2D` and has no producer and no consumer contract. The author's criterion
of 2026-11-21 is that an object that exists and is sufficiently defined must be presented
and used as such, with a declared lower grade, and AGENTS §1 principle 8 requires that any
approximation be explicit, tolerance-controlled, reproducible and distinguishable from an
exact result.

## Decision (proposed)

1. **A conditioned evidence structure, owned by the producer.** A reconstructible Locus V2
   may publish, beside its existence structure and never inside it, a *conditioned evidence*
   of nature `NUMERICAL_UNDER_HYPOTHESES`: a canonical dyadic partition of the declared
   driver domain into *conditioned spans* and typed *bands*, computed by a versioned
   traversal policy from evaluations of the faithful slice and from the root testimony that
   the slice already holds. It is revision evidence: derived per revision and policy, never
   persisted, never identity, and never part of the semantic snapshot.

2. **Eight independent axes, no single ladder.** Reconstruction fidelity, point validity,
   interval evidence of existence and continuity, numeric guarantee, incidence integrity,
   identity and continuation of selection, coverage, and currentness of definition and of
   evidence stay separate facts. `EXPLORATORY → CONDITIONED → LOCAL_CERTIFIED → COMPLETE` names
   only the interval-evidence axis. A result states the grade of each axis that it used.

3. **Hypotheses are declared, never implied.** A conditioned span asserts what was checked
   at its nodes and states, as recorded hypotheses of the policy, what was assumed between
   them. A finite traversal does not detect every event, an observed slope is not a Lipschitz
   bound, and density never promotes a span to a certificate. `§15.1` stays true: a finite
   exploration never establishes level 3 or 4.

4. **Failure is not absence.** A parameter where the evaluation failed for a computational
   cause, a work limit, an authority refusal or an unreproduced selection belongs to an
   unresolved band. A conditioned absence needs positive evidence of an eligible geometric
   cause at both ends and inside the band.

5. **One contract per consumer.** Each consumer states the premises it reads, the grade it
   publishes, the hypotheses it inherits and its typed refusals. A consumer that requires a
   certificate keeps failing closed. A result is graded by the premises it actually used, not
   by the weakest evidence of the locus.

6. **Addresses name a parameter, not a mesh.** A point on a conditioned region persists a
   parameter address without a component selector, in a new versioned codec. A persisted
   component selector keeps its meaning and is never reinterpreted. Refinement, promotion
   and withdrawal of evidence change neither the address nor the identity of the point.

7. **Presentation is distinguishable and exports are explicit.** A conditioned region is
   drawn continuously and differently from a certified component; the product never shows it
   as complete. A format exports it only when it can carry the fidelity class and the
   hypothesis; otherwise the omission is diagnosed. Zoom, DPI and viewport are never
   evidence.

8. **Certification stays direct, and conditioned evidence feeds it.** A producer that can be
   certified is certified as today; the conditioned evidence is not a mandatory step. The
   spans and bands are proof plans: K2 (composition with a certified source through a
   certified reparametrization) and K1 (certified root families) promote regions to
   certified components by proof, in bounded increments approved separately.

## Alternatives considered

| Alternative | Why it is not proposed |
|---|---|
| Keep the refusal and wait for K1 | Leaves existing, well-defined objects unusable for an unbounded time; contradicts the author's criterion |
| Admit `EXPLORATORY` evidence to consumers | The exploration has no declared hypothesis, no contradiction rule and no consumer contract; §15.1 forbids it expressly |
| Promote dense sampling to `LOCAL_CERTIFIED` | A false certificate; violates §15.1 and AGENTS principle 8 |
| A document-wide «relaxed» authority | The document authority (ADR 0040) decides relations, not existence; one switch would lower every guarantee at once |
| The R1 §10.4 recipe literally | It names a «verified component» whose properties are hypotheses, lets a margin estimated from slopes act as a bound, treats undetected events as bounded by the minimum step, lets narrow bands vanish from totals and assumes no format change; the instruction asks to close those problems |

## Consequences

- Positive: a faithful uncertified locus is drawn as a curve and admits points, lengths and
  local intersections with an explicit, propagated grade; strict consumers are unchanged.
- Cost: a new evidence owner and lifecycle in the kernel, a new address codec, a consumer
  matrix to maintain, and a traversal whose cost is charged once per revision. The post-R1
  repairs make revisions stable under driver motion, which is what makes that cost
  acceptable.
- Risk: a conditioned span can be wrong where an event falls between nodes. The risk is
  stated to the user, bounded by nothing but the declared hypotheses, and removed only by
  promotion.
- Compatibility: documents that persist a parameter address are not read as defined points
  by earlier versions; they fail closed there with a typed cause. No existing document
  changes its results.

## Status of the normative texts

The amendments of the candidate contract to `locus-v2-existence-components.md`,
`locus-v2-public-surface.md`, `locus-v2-presentation.md`, `locus-v2-metrics.md`,
`locus-v2-intersections.md` and the export specifications are proposed texts. None is
applied to those specifications by this ADR.

# PRE-G9B-CU2 — Locus V2 existence certification, continuity and representation

**CANONICAL PROMPT — PREPARATION CANDIDATE — PENDING AUTHOR REVIEW — IMPLEMENTATION NOT
AUTHORIZED.**

- **Origin.** Prepared by the `PRE-G9B-CU2` preparation that the author authorized on
  2026-10-20 ([`CU1` closeout record](../../../docs/validation/pre_g9b_cu1_closeout_record.md) §7).
  Preparation base: `27bf652f176e57ced3807b82afe14fc00b7586a9` (`CU1` documentary closeout).
  Revised by the author's focused review P1–P5 of 2026-10-20, on the published preparation
  candidate `60d546441dcca5355edfe296ee8a0a521f8a3565`, and by the author's coherence closeout
  C1–C3 of 2026-10-20, on the published focused-review candidate
  `cbb7ea8ce379d8d74072f4d12dcd4443265eb7ac`.
- **Normative candidate.** [`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
  amendments A2.1-R to A2.6, and
  [ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) (PROPOSED).
- **Design.** [implementation strategy](../../../docs/architecture/pre_g9b_cu2_implementation_strategy.md),
  steps T0–T13; [hybrid certification design](../../../docs/architecture/pre_g9b_cu2_hybrid_certification_design.md);
  [oriented length event design](../../../docs/architecture/pre_g9b_cu2_oriented_length_event_design.md);
  [consumption and representation](../../../docs/architecture/pre_g9b_cu2_consumption_and_representation.md).
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu2_validation_matrix.md),
  gates GC-1–GC-8.
- **Analysis.** [author model certification analysis](../../../docs/architecture/pre_g9b_cu2_author_model_certification_analysis.md),
  including the critical bite (§3.8) and the folds of `e_1` (§4.3).
- **Decisions.** [`CU2` decision sheet](../../../docs/validation/pre_g9b_cu2_author_decision_sheet.md).
  Choices marked **[D-CU2-n]** or **[E-n]** apply only as the author decides them.

Nothing here authorizes implementation.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`, as proposed in
**[D-CU2-12]** and frozen by the implementation authorization.

Replace the compatibility existence certificate of Locus V2 with the hybrid certification of
amendments A2.1-R and A2.2-R:

- four evidence levels, with exploratory evidence that never gains authority, and
  completeness only as a certified cover;
- continuity of the traced map as a component requirement;
- arcs on periodic domains with distinct one-sided ends, and one closure authority;
- existence, evaluation and representation kept as distinct facts;
- the class v3 construction program, with the selection read from the algorithm's state
  **[D-CU2-17]**, typed box outcomes and certified transitions;
- the exact algebraic certificate of first-level predicates, which certifies multiple roots
  such as the critical bite value **[D-CU2-16]**;
- effective outputs at coincident roots: the label state of the outputs in the selection
  state, and an unresolved band where the inherited rule suppresses an unlabelled second
  output **[D-CU2-18]**;
- point components as an entity of their own, declared and not drawn **[D-CU2-19]**;
- the exact revision fingerprint **[D-CU2-20]**;
- structural totality and periodicity, and folds certified by structural symmetry with
  second-order jets **[D-CU2-6]**.

Add the structural certificate of a metric function and its `ORIGIN_WRAP` event with two
one-sided ends (A2.3-R), the publication rule (H01), structural component lineage with
selector resolution by demonstrated correspondence **[E-3]**, the migration of every consumer
to the existence structure (A2.5), and the render and export topology, tessellation policy v2
and declared fidelity (A2.4-R, A2.6).

The phase corrects B07, H01, B10.2 and B06 against gates GC-1–GC-6 and GC-8. It fails closed:
what it cannot prove stays `NOT_ESTABLISHED` or `UNRESOLVED`, and a class-3 length is never a
rigorous input of a certificate.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the `CU2` implementation authorization **[D-CU2-13]**;
it is expected to be the candidate of the focused review or its author-approved closeout
descendant. Moving branch names are context only.

Entry gate:

- exact base identity and a clean worktree;
- local, tracking and live remote `main` as recorded by the authorization;
- the approved texts of A2.1-R to A2.6, ADR 0038 and the decisions **[D-CU2-1]** to
  **[D-CU2-20]** and **[E-1]** to **[E-3]**;
- development runs of `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` and
  `-Phase PRE-G9B-CU1` accepted on the base.

Otherwise stop.

## Authority and evidence hierarchy

1. `AGENTS.md`: §1.8 (no silent approximation), §9 (design before code), §11.2–§11.4,
   §14 and §16.
2. Current code and tests.
3. The approved specifications: `locus-v2-existence-components.md`,
   `locus-v2-certified-construction-model.md`, `locus-v2-semantics.md`,
   `locus-v2-metrics.md`, `locus-v2-presentation.md`, `locus-v2-intersections.md`,
   `locus-v2-extended-intersections.md`, `locus-v2-point-interaction.md`,
   `locus-v2-similarity-transformations.md`, `locus-v2-public-surface.md` and
   `dxf-curve-fidelity-and-approximation.md`.
4. ADRs 0007, 0013, 0014, 0022, 0027, 0028, 0029 and 0037, and ADR 0038 once accepted.
5. The `CU0` and `CU1` closeout records and the `CU0` geometric reference baseline
   (`preserve` and `pins`).
6. The preparation documents named above. They are evidence and design, not authority
   over the approved specifications.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

**Specifications (step T0, first).** Incorporate the approved texts into:

- `locus-v2-existence-components.md`, v1.1;
- `locus-v2-certified-construction-model.md`, v1.2 (`certified-construction-program/v3`);
- `locus-v2-metrics.md`, v1.6;
- `locus-v2-presentation.md`, version 2;
- `locus-v2-semantics.md`, v1.3;
- the scope notes of A2.5 in the intersection, point-interaction, similarity and
  public-surface specifications;
- the DXF specification, and a durable LaTeX export specification **[D-CU2-11]**.

In the same step, set ADR 0038 to the status the author decides.

**Kernel**, under `source/shared/common/src/main/java/org/geocedg/common/kernel/`:

- `locus/`:
  - `LocusExistenceStructure2D`: arcs with one-sided ends, point components, boundary kinds,
    singular parameters, typed coverage causes, the nature of each proof and the closure
    query; removal of `completeCompatibility` in step T11;
  - `ReconstructionSliceFingerprint2D`: the exact binary64 fingerprint, only as decided in
    **[D-CU2-20]**;
  - `LocusBranch2D` and `LocusV2Factory`: the explicit certificates of **[D-CU2-7]**;
  - `LocusComponentLineage2D` and `LocusLineage2D`: structural lineage;
  - `LocusPrincipalBranchSelector2D` and `LocusInstrumentation2D`;
  - new classes for the hybrid certifier, the exact algebraic certificate (exact rationals and
    polynomials, real-root structure, first-level predicates, degeneration evidence), the
    structural-symmetry fold certificate and the exploratory evidence.
- `locus/intersection/`:
  - `ConstructionIntervalModel2D` and the certified interval model family: the class v3
    capture, enclosures, typed box outcomes and second-order jets, with v1 and v2 programs
    unchanged;
  - `SelectedRootContinuousDomainCertifier2D`, as decided in **[D-CU2-9]**;
  - `IntersectionEndpointProvenanceResolver2D`, `LocusIntersectionSolver2D` and the pair
    solver, only to read the existence structure (arc phase frame, seam quarantine on closed
    periodic components). No solver or tolerance change.
- `locus/metric/`: route resolver, engine, position binder and the periodic-cycle tests of
  the capabilities, reading the existence structure; the structural certificate and the
  `ORIGIN_WRAP` event, with its one-sided ends, in the rich result.
- `locus/interaction/LocusPointInteractionResolver2D`: reading the existence structure.
- `algos/`: `AlgoDependentPointLocusV2` (hybrid certifier and publication rule),
  `AlgoLocusV2`, `AlgoNestedLocusV2`, `AlgoLocusSimilarityTransform2D`, `AlgoSplineV2`,
  `AlgoSegmentPathLocusV2`, `AlgoSemanticLocusPoint2D`, and `AlgoLocusMetricV2` and
  `AlgoLocusBetweenMetricV2` only to expose the event.
- `geos/GeoLocusV2`: publication state and instrumentation accessors only.

**Inherited classes:** read-only accessors of the selection state in
`org/geogebra/common/kernel/algos/AlgoIntersectLineConic` and `AlgoIntersectSingle`,
registered as modifications **[D-CU2-17]**; the label state is read through the public
`GeoElement.isLabelSet`. No change of their behaviour, including the suppression of an
unlabelled second output in `AlgoIntersect.avoidDoubleTangentPoint`.

**Rendering and export:**

- `source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D`:
  render topology, policy v2, singular vertices and declared fidelity **[D-CU2-10]**, with
  point components declared and not drawn **[D-CU2-19]**;
- `source/shared/common/src/main/java/org/geocedg/common/export/`:
  `SemanticCurveExportAdapter2D` and `AdaptiveCurveApproximationBuilder2D`;
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/`: the Locus V2 path of
  `LatexSemanticExportSupport` and of the PGF, PSTricks and Asymptote exporters;
- the display of exploratory evidence in unresolved bands, only as decided in **[E-1]**.

**Tests:**

- new focused, adversarial and regression tests per the validation matrix, including the
  integer critical configuration and its neighbours;
- the declared conformance updates of existing tests inventoried in strategy §5;
- the inversion of `pins.B07`, `pins.H01`, `pins.B06` and `pins.B10.2` in the `CU0` tests
  and baseline, through strategy §5 with a re-baseline record **[D-CU2-14]**.

**Infrastructure and documentation:**

- the `PRE-G9B-CU2` phase and its selections, inventory, registry pin and coverage pins,
  through the official mechanisms;
- `docs/upstream/modified-files.yml` entries;
- the `certification` and `tessellation` scenarios of the `tools/benchmark/pre-g9b-cu/`
  harness;
- the corpus manifest wording, only as decided in **[D-CU2-15]**;
- the candidate report and its machine-readable mirror;
- the status and evidence of the `CU2` methods in the scientific transfer register
  ([`pre_g9b_cu_scientific_methods_editorial_handoff_plan.md`](../../../docs/architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md)),
  without editorial text;
- roadmap and consolidated-plan status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**Owned by `CU3`:**

- structural points, tangent-root materialization and tangency classification; a certified
  singular parameter, fold or point component never creates a point, and point components
  are not drawn beyond **[D-CU2-19]**;
- intersection solvers, residual tolerances and the intersection engine (H02, H06);
- the seam-root observation F-CU2-5;
- new public tools.

**Owned by `CU1` and closed:**

- the values, traversal and same-position policies, order and direction of `LocusLength`;
- the metric guarantee classes, the estimator and the estimate contract;
- any use of a class-3 value as a rigorous input.

**Other packages and areas:**

- any user-interface change beyond **[E-1]**;
- the document format, and any rewrite of persisted selectors or user objects such as
  `text2`;
- the durable dependency projection of ADR 0029;
- inferring a selection, a correspondence or a continuity by proximity, label, index or
  coordinates;
- writing the selection state of inherited algorithms, or any other change of inherited
  classes beyond registered read-only accessors;
- the continuous selection of coincident roots in the slice and any change of the inherited
  rule that suppresses an unlabelled second output **[D-CU2-18]**; substituting an undefined
  output by its certified limit value or by another output;
- tolerances that turn a near-tangency into a tangency, and the symbolic capture of
  driver-independent relations;
- the legacy GeoGebra interval package, which must not be used as a certificate;
- the compatibility certificate as a fallback for any locus;
- M08 and M09;
- the book: the author's editorial mandate E1 is a track obligation for `CU10`, not part of
  `CU2`;
- any `CU0` `preserve` value beyond its tolerance.

## Architectural placement

Existence, continuity, closure, selection identity and lineage belong to the shared Java
kernel (`AGENTS.md` §4). Rendering and export approximations derive from the kernel
certificate, declare their fidelity and never decide topology. No geometric truth moves to the
user interface, an exporter or a script.

## Required design/specification

The approved texts of A2.1-R to A2.6, incorporated in step T0 before any code, and ADR 0038.
If an implementation step needs a rule that these texts do not state, stop and ask the
author.

## Geometric invariants and degeneracies

The following hold:

- existence, continuity and boundaries are certified only by rigorous means, never by
  sampling, pointwise validity or rendering;
- in this phase a multiple root is certified only by the exact algebraic certificate of a
  first-level predicate; simple-root isolation never certifies a multiplicity, and a
  near-tangency is never declared a tangency by tolerance;
- every certified fact records the nature of its proof (exact, rigorous interval, numerical
  under hypotheses, exploratory or none); only the first two certify;
- the captured binary64 constants are the semantic reference: a tangency computed with
  rounding is certified as its rounded configuration;
- a double zero of a validity predicate without sign change keeps existence and continuity;
  it is a certified singular parameter, not a boundary;
- a component is never declared continuous, nor its locus `COMPLETE`, only because the
  geometric equation has a double root when the indexed output that defines the traced point
  is undefined there; a certified limit value is evidence, never an output, and no output
  identity is replaced by another because of a spatial coincidence;
- a point component is never an interval or arc of positive length and never a singular
  parameter;
- the revision fingerprint is exact; no certificate is reused for a state that it does not
  distinguish;
- a fold is certified only by its five separate proofs; an enclosure of `F′` that contains zero
  certifies nothing;
- a curve is closed only when its component is a certified closed periodic component; the
  periodicity of the driver never closes it, and the two ends of an open full-length arc are
  never identified;
- a structural discontinuity with valid evaluations on both sides is a boundary;
- end inclusion follows the semantics of the boundary predicate;
- a computational failure is never certified nonexistence, a valid evaluation is never
  existence, and certified existence is never a guarantee of coordinates;
- certificates are deterministic per revision and policy and independent of view, zoom, DPI
  and query order;
- the durable identity of a result, the semantic identity of its selection and the lineage of
  a component are distinct; identity never follows coordinates, list order, endpoint bits or
  proximity;
- `LocusLength` semantics do not change.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused development, in this order:

1. each step T1–T12 of the strategy with its matrix rows green;
2. Checkstyle and the upstream boundary;
3. the official JUnit inventory update from executed evidence, with the registry pin
   re-pinned;
4. development runs of `INFRA_UNIT` and of the three `PHASE` selections.

Acceptance, on the frozen, clean candidate commit:

- `verify.ps1 -Profile STATIC`;
- `verify.ps1 -Profile INFRA_UNIT`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`;
- `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<new>`, with the
  console redirected outside the repository.

Then the `certification` and `tessellation` benchmarks before and after, with counters.
`FINAL` is not required by the class.

The executable authority is the active recovery contract of `verification-levels.md`
together with `verify.ps1` and `verification-registry.json`. Report exact commands,
exit codes, run identifiers, result hashes and log paths. Gates GC-1–GC-6 and GC-8 are
acceptance criteria of the phase; GC-7, the time of GC-8 and the external DXF check are
informative.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must:

- approve the normative proposal, ADR 0038 and the decisions **[D-CU2-1]** to
  **[D-CU2-20]** and **[E-1]** to **[E-3]**;
- name the exact base commit and tree;
- authorize the implementation.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only. No push, merge, tag, release or binary without a separate author
instruction naming the exact accepted commit.

## Compatibility and serialization

No document-format change: certificates, exploratory evidence, metric events and selection
states are recomputed and not serialized. Persisted component selectors resolve by
demonstrated structural correspondence and are never rewritten; unresolved ones carry a typed
cause. Drawn and exported vertices change as declared conformance. Exports of `e_2` become
open. `LocusLength` values and the coordinates of `e_2` do not change.

## Integration obligations

- Register new source and test files, and the read-only accessors in inherited classes, in
  `docs/upstream/modified-files.yml`.
- Keep `final.shared` and `final.desktop` green without weakened tests.
- Update the inventory only through the official updater with executed evidence.
- Record every changed numerical reference in the re-baseline record.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review. The candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`). The
author's closeout record is the sole authority for approval.

## Required artifacts

- the incorporated specification amendments and the ADR status;
- the tests and the re-baseline record;
- benchmark evidence before and after, including the cost of the exact certificate;
- the candidate report and its mirror, with:
  - the gate values;
  - the verification evidence (`STATIC`, `INFRA_UNIT`, the three `PHASE` runs and
    `INTEGRATION`);
  - the bootstrap-impact outcome (expected: no change);
  - `GUIDE_IMPACT` (expected: none in `CU2`; the guide belongs to `CU9`);
- the `CU2` rows of the scientific transfer register, with status and evidence;
- roadmap and plan status.

## Stop conditions

The stop conditions of strategy §4 apply, in particular:

- the class v3 does not certify a locus of the author's model where the analysis predicts
  it, within the approved budget ranges;
- the exact algebraic certificate does not apply to a predicate that the author's model
  needs, or exhausts its approved budget;
- the evaluator slice does not reproduce the live selection state in the author's model, or
  reproducing it would need writing into inherited classes;
- a locus of the author's model depends on an unlabelled second output at a coincident root,
  the slice does not reproduce the label state, or continuity would need substituting an
  output **[D-CU2-18]**;
- the revision fingerprint does not distinguish an input change and **[D-CU2-20]** does not
  authorize its correction;
- a consumer needs to materialize or draw a point component beyond **[D-CU2-19]**;
- certifying `e_2` would need the value of a length;
- resolving a persisted selector would need rewriting persisted content or associating it by
  containment without a demonstrated correspondence;
- a correction needs a change to a `CU1` or `CU3` contract;
- a `preserve` value moves beyond its tolerance, or a numerical reference would change
  without a re-baseline record;
- an existing test could only pass by weakening it;
- base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage,
  or a missing author decision.

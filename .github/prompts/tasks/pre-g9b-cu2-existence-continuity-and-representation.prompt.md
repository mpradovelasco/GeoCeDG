# PRE-G9B-CU2 — Locus V2 existence certification, continuity and representation

**CANONICAL PROMPT — PREPARATION CANDIDATE — PENDING AUTHOR REVIEW — IMPLEMENTATION NOT
AUTHORIZED.**

- **Origin.** Prepared by the `PRE-G9B-CU2` preparation that the author authorized on
  2026-10-20 ([`CU1` closeout record](../../../docs/validation/pre_g9b_cu1_closeout_record.md) §7).
  Preparation base: `27bf652f176e57ced3807b82afe14fc00b7586a9` (`CU1` documentary closeout).
- **Normative candidate.** [`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
  amendments A2.1-R to A2.6, and
  [ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) (PROPOSED).
- **Design.** [implementation strategy](../../../docs/architecture/pre_g9b_cu2_implementation_strategy.md),
  steps T0–T13; [hybrid certification design](../../../docs/architecture/pre_g9b_cu2_hybrid_certification_design.md);
  [oriented length event design](../../../docs/architecture/pre_g9b_cu2_oriented_length_event_design.md);
  [consumption and representation](../../../docs/architecture/pre_g9b_cu2_consumption_and_representation.md).
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu2_validation_matrix.md),
  gates GC-1–GC-7.
- **Analysis.** [author model certification analysis](../../../docs/architecture/pre_g9b_cu2_author_model_certification_analysis.md).
- **Decisions.** [`CU2` decision sheet](../../../docs/validation/pre_g9b_cu2_author_decision_sheet.md).
  Choices marked **[D-CU2-n]** or **[E-n]** apply only as the author decides them.

Nothing here authorizes implementation.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`, as proposed in
**[D-CU2-12]** and frozen by the implementation authorization.

Replace the compatibility existence certificate of Locus V2 with the hybrid certification of
amendments A2.1-R and A2.2-R:

- four evidence levels, with exploratory evidence that never gains authority;
- continuity of the traced map as a component requirement;
- arcs on periodic domains and one closure authority;
- computational undefinedness kept apart from certified nonexistence;
- the class v3 construction program, with typed box outcomes and certified transitions;
- structural totality and periodicity, and second-order jets for folds **[D-CU2-6]**.

Add the structural certificate of a metric function and its `ORIGIN_WRAP` event
(A2.3-R), the publication rule (H01), structural component lineage with selector migration
**[E-3]**, the migration of every consumer to the existence structure (A2.5), and the render
and export topology and tessellation policy v2 (A2.4-R, A2.6).

The phase corrects B07, H01, B10.2 and B06 against gates GC-1–GC-6. It fails closed: what it
cannot prove stays `NOT_ESTABLISHED` or `UNRESOLVED`, and a class-3 length is never a rigorous
input of a certificate.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the `CU2` implementation authorization **[D-CU2-13]**;
it is expected to be this preparation candidate or its author-approved closeout
descendant. Moving branch names are context only.

Entry gate:

- exact base identity and a clean worktree;
- local, tracking and live remote `main` as recorded by the authorization;
- the approved texts of A2.1-R to A2.6, ADR 0038 and the decisions **[D-CU2-1]** to
  **[D-CU2-15]** and **[E-1]** to **[E-3]**;
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
  - `LocusExistenceStructure2D`: arcs, boundary kinds, typed coverage causes and the
    closure query; removal of `completeCompatibility` in step T11;
  - `LocusBranch2D` and `LocusV2Factory`: the explicit certificates of **[D-CU2-7]**;
  - `LocusComponentLineage2D` and `LocusLineage2D`: structural lineage;
  - `LocusPrincipalBranchSelector2D` and `LocusInstrumentation2D`;
  - new classes for the hybrid certifier and the exploratory evidence.
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
  `ORIGIN_WRAP` event in the rich result.
- `locus/interaction/LocusPointInteractionResolver2D`: reading the existence structure.
- `algos/`: `AlgoDependentPointLocusV2` (hybrid certifier and publication rule),
  `AlgoLocusV2`, `AlgoNestedLocusV2`, `AlgoLocusSimilarityTransform2D`, `AlgoSplineV2`,
  `AlgoSegmentPathLocusV2`, `AlgoSemanticLocusPoint2D`, and `AlgoLocusMetricV2` and
  `AlgoLocusBetweenMetricV2` only to expose the event.
- `geos/GeoLocusV2`: publication state and instrumentation accessors only.

**Rendering and export:**

- `source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D`:
  render topology and policy v2 **[D-CU2-10]**;
- `source/shared/common/src/main/java/org/geocedg/common/export/`:
  `SemanticCurveExportAdapter2D` and `AdaptiveCurveApproximationBuilder2D`;
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/`: the Locus V2 path of
  `LatexSemanticExportSupport` and of the PGF, PSTricks and Asymptote exporters;
- the display of exploratory evidence in unresolved bands, only as decided in **[E-1]**.

**Tests:**

- new focused, adversarial and regression tests per the validation matrix;
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
- roadmap and consolidated-plan status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**Owned by `CU3`:**

- structural points, tangent-root materialization and tangency classification;
- intersection solvers, residual tolerances and the intersection engine (H02, H06);
- the seam-root observation F-CU2-5;
- new public tools.

**Owned by `CU1` and closed:**

- the values, traversal and same-position policies, order and direction of `LocusLength`;
- the metric guarantee classes, the estimator and the estimate contract;
- any use of a class-3 value as a rigorous input.

**Other packages and areas:**

- any user-interface change beyond **[E-1]**;
- the document format, beyond the lazy rewrite of component selectors **[E-3]**;
- the durable dependency projection of ADR 0029;
- the legacy GeoGebra interval package, which must not be used as a certificate;
- inherited upstream classes beyond registered read-only accessors;
- the compatibility certificate as a fallback for any locus;
- M08 and M09;
- the book;
- any `CU0` `preserve` value beyond its tolerance.

## Architectural placement

Existence, continuity, closure and lineage belong to the shared Java kernel (`AGENTS.md`
§4). Rendering and export approximations derive from the kernel certificate and never decide
topology. No geometric truth moves to the user interface, an exporter or a script.

## Required design/specification

The approved texts of A2.1-R to A2.6, incorporated in step T0 before any code, and ADR 0038.
If an implementation step needs a rule that these texts do not state, stop and ask the
author.

## Geometric invariants and degeneracies

The following hold:

- existence, continuity and boundaries are certified only by rigorous means, never by
  sampling, pointwise validity or rendering;
- a curve is closed only when its component is a certified closed periodic component; the
  periodicity of the driver never closes it;
- a structural discontinuity with valid evaluations on both sides is a boundary;
- end inclusion follows the semantics of the boundary predicate;
- a double root of a validity predicate (the critical bite value) stays an unresolved band;
- a computational failure is never certified nonexistence;
- certificates are deterministic per revision and policy and independent of view, zoom and
  DPI;
- component identity follows structural lineage, never coordinates, list order or endpoint
  bits;
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
exit codes, run identifiers, result hashes and log paths. Gates GC-1–GC-6 are acceptance
criteria of the phase; GC-7 and the external DXF check are informative.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must:

- approve the normative proposal, ADR 0038 and the decisions **[D-CU2-1]** to
  **[D-CU2-15]** and **[E-1]** to **[E-3]**;
- name the exact base commit and tree;
- authorize the implementation.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only. No push, merge, tag, release or binary without a separate author
instruction naming the exact accepted commit.

## Compatibility and serialization

No document-format change: certificates, exploratory evidence and metric events are
recomputed and not serialized. Persisted component selectors are resolved by unique
containment and rewritten lazily on save; ambiguous ones become unresolved with a typed
cause. Drawn and exported vertices change as declared conformance. Exports of `e_2` become
open. `LocusLength` values and the coordinates of `e_2` do not change.

## Integration obligations

- Register new source and test files in `docs/upstream/modified-files.yml`.
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
- benchmark evidence before and after;
- the candidate report and its mirror, with:
  - the gate values;
  - the verification evidence (`STATIC`, `INFRA_UNIT`, the three `PHASE` runs and
    `INTEGRATION`);
  - the bootstrap-impact outcome (expected: no change);
  - `GUIDE_IMPACT` (expected: none in `CU2`; the guide belongs to `CU9`);
- roadmap and plan status.

## Stop conditions

The stop conditions of strategy §4 apply, in particular:

- the class v3 does not certify a locus of the author's model where the analysis predicts
  it, within the approved budget ranges;
- certifying `e_2` would need the value of a length;
- a correction needs a change to a `CU1` or `CU3` contract;
- a `preserve` value moves beyond its tolerance, or a numerical reference would change
  without a re-baseline record;
- an existing test could only pass by weakening it;
- base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage,
  or a missing author decision.

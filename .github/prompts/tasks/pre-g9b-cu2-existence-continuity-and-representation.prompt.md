# PRE-G9B-CU2 — Locus V2 existence certification, continuity and representation

**CANONICAL PROMPT — ARCHITECTURE AUTHOR APPROVED (2026-10-21) — IMPLEMENTATION AUTHORIZED,
CONDITIONED, ON THE EXACT BASE `070af6f9` — `CU2` IS NOT APPROVED UNTIL THE AUTHOR REVIEWS
THE FROZEN TECHNICAL CANDIDATE.**

- **Origin.** Prepared by the `PRE-G9B-CU2` preparation that the author authorized on
  2026-10-20 ([`CU1` closeout record](../../../docs/validation/pre_g9b_cu1_closeout_record.md) §7).
  Revised by the author's focused review P1–P5 and coherence closeout C1–C3 of 2026-10-20.
  Updated at step T0 by the author's instruction "GeoCeDG — PRE-G9B-CU2. Aprobación autoral de
  la arquitectura y autorización condicionada de implementación" of 2026-10-21
  ([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)),
  which approves the architecture with the mandatory precision D-CU2-20-R and authorizes the
  implementation.
- **Normative authority.** Amendments A2.1-R to A2.6 of the
  [`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
  incorporated at T0 into `locus-v2-existence-components.md` 1.1 (§15),
  `locus-v2-certified-construction-model.md` 1.2 (§10), `locus-v2-metrics.md` 1.6 (§26),
  `locus-v2-semantics.md` 1.3, `locus-v2-presentation.md` 2, the A2.5 scope notes,
  `dxf-curve-fidelity-and-approximation.md` 1.2 (§10) and the new
  `latex-semantic-curve-export.md`; and
  [ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md),
  accepted as architecture.
- **Design.** [implementation strategy](../../../docs/architecture/pre_g9b_cu2_implementation_strategy.md),
  steps T0–T13; [hybrid certification design](../../../docs/architecture/pre_g9b_cu2_hybrid_certification_design.md),
  including the D-CU2-20-R lifecycle (§8.1);
  [oriented length event design](../../../docs/architecture/pre_g9b_cu2_oriented_length_event_design.md);
  [consumption and representation](../../../docs/architecture/pre_g9b_cu2_consumption_and_representation.md).
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu2_validation_matrix.md),
  gates GC-1–GC-8; the critical and exact synthetic cases are mandatory tests.
- **Analysis.** [author model certification analysis](../../../docs/architecture/pre_g9b_cu2_author_model_certification_analysis.md),
  including the critical bite (§3.8), the effective outputs (§3.9, with the corrected
  attribution of F-CU2-12) and the folds of `e_1` (§4.3).
- **Decisions.** Markers **[D-CU2-n]**, **[D-CU2-20-R]** and **[E-n]** point to the author's
  dispositions in the approval record; the
  [`CU2` decision sheet](../../../docs/validation/pre_g9b_cu2_author_decision_sheet.md) keeps the
  questions as they were presented.

The implementation is authorized only within the scope, order and stop conditions below and in
the approval record. Nothing here authorizes publication, promotion, closeout or author
approval.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE` **[D-CU2-12]**.

Correct B07, B10.2, H01 and B06, with the normative and kernel capabilities they need, by
replacing the compatibility existence certificate of Locus V2 with the hybrid certification of
amendments A2.1-R and A2.2-R **[D-CU2-1]**, **[D-CU2-2]**:

- four evidence levels, with exploratory evidence that never gains authority, and
  completeness only as a certified cover;
- continuity of the traced map as a component requirement;
- arcs on periodic domains with distinct one-sided ends, and one closure authority;
- existence, evaluation and representation kept as distinct facts;
- the class v3 construction program, with the selection read from the algorithm's state
  **[D-CU2-17]**, typed box outcomes and certified transitions;
- the exact algebraic certificate of first-level predicates, which certifies multiple roots
  such as the critical bite value and the approved exact synthetic configurations
  **[D-CU2-16]**;
- effective outputs at coincident roots: the label state of the outputs in the selection
  state, and an unresolved band where the inherited rule suppresses an unlabelled second
  output **[D-CU2-18]**;
- point components as an entity of their own, declared and not drawn **[D-CU2-19]**;
- the exact, versioned revision fingerprint and the reconstruction fidelity of the evaluator
  slice **[D-CU2-20-R]**, an early blocking gate: characterized in T1 and corrected in T3,
  before any certificate of T4 is valid or published;
- structural totality and periodicity, and folds certified by structural symmetry with
  second-order jets **[D-CU2-6]**.

Add the structural certificate of a metric function and its `ORIGIN_WRAP` event with two
one-sided ends (A2.3-R, **[D-CU2-3]**), the publication rule (H01) with the empty-cover rule
**[D-CU2-8]**, structural component lineage with selector resolution by demonstrated
correspondence **[E-3]**, the migration of every consumer to the existence structure (A2.5,
**[E-2]**), and the render and export topology, tessellation policy v2 and declared fidelity
(A2.4-R, A2.6, **[D-CU2-4]**, **[D-CU2-10]**, **[E-1]**).

Gates GC-1–GC-6 and GC-8 are acceptance criteria. The phase fails closed: what it cannot prove
stays `NOT_ESTABLISHED` or `UNRESOLVED`, and a class-3 length is never a rigorous input of a
certificate.

<!-- geocedg-field: implementation_base -->
## Implementation base

Exact base **[D-CU2-13]**: `070af6f902cd0be291247e73efb5629ef2a39eba`, tree
`49a659d8b334aae3f69c4db000cc842204e9a85f` (the published `phase/pre-g9b-cu2-preparation`
candidate). Approved `CU1` closeout base: `27bf652f176e57ced3807b82afe14fc00b7586a9`. Local
implementation branch `phase/pre-g9b-cu2-implementation`, created from the exact SHA. Moving
branch names are context only.

Entry gate (recorded in the approval record §1):

- exact base identity, ancestry and a clean worktree;
- the published preparation branch at the base; local and live remote `main` without an
  unexamined divergence;
- `AGENTS.md` and the current repository authority read;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` and `-Phase PRE-G9B-CU1` accepted on the base.

Otherwise stop.

## Authority and evidence hierarchy

1. `AGENTS.md`: §1.8 (no silent approximation), §9 (design before code), §11.2–§11.4,
   §14 and §16.
2. Current code and tests.
3. The approved specifications: `locus-v2-existence-components.md`,
   `locus-v2-certified-construction-model.md`, `locus-v2-semantics.md`,
   `locus-v2-metrics.md`, `locus-v2-presentation.md`, `locus-v2-intersections.md`,
   `locus-v2-extended-intersections.md`, `locus-v2-point-interaction.md`,
   `locus-v2-similarity-transformations.md`, `locus-v2-public-surface.md`,
   `dxf-curve-fidelity-and-approximation.md` and `latex-semantic-curve-export.md`.
4. ADRs 0007, 0013, 0014, 0022, 0027, 0028, 0029, 0037 and 0038.
5. The `CU2` approval record, the `CU0` and `CU1` closeout records and the `CU0` geometric
   reference baseline (`preserve` and `pins`).
6. The preparation documents named above. They are evidence and design, not authority
   over the approved specifications.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

**Specifications (step T0, first).** Incorporate the approved texts and reconcile versions, as
listed above; set ADR 0038 to `ACCEPTED — AUTHOR APPROVED` as an architectural decision; record
the author's decision; update this prompt. The only amendment of a `CU1` contract is the
conformance amendment of D-CU2-20-R in `locus-v2-semantics.md`.

**Kernel**, under `source/shared/common/src/main/java/org/geocedg/common/kernel/`:

- `locus/`:
  - `LocusExistenceStructure2D`: arcs with one-sided ends, point components, boundary kinds,
    singular parameters, typed coverage causes, the nature of each proof and the closure
    query; removal of `completeCompatibility` in step T11;
  - `ReconstructionSliceFingerprint2D` and `ReconstructibleLocusEvaluator2D`: the versioned
    exact fingerprint and the reconstruction fidelity validation, with admitted restoration and
    explicit failure **[D-CU2-20-R]**;
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
registered as minimal upstream modifications **[D-CU2-17]**; the label state is read through
the public `GeoElement.isLabelSet`. No change of their behaviour, including the suppression of
an unlabelled second output in `AlgoIntersect.avoidDoubleTangentPoint`.

**Rendering and export:**

- `source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D`:
  render topology, policy v2, singular vertices and declared fidelity **[D-CU2-10]**, with
  point components declared and not drawn **[D-CU2-19]**;
- `source/shared/common/src/main/java/org/geocedg/common/export/`:
  `SemanticCurveExportAdapter2D` and `AdaptiveCurveApproximationBuilder2D`;
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/`: the Locus V2 path of
  `LatexSemanticExportSupport` and of the PGF, PSTricks and Asymptote exporters
  **[D-CU2-11]**;
- the display of exploratory evidence in unresolved bands **[E-1]**.

**Tests:**

- new focused, adversarial and regression tests per the validation matrix, including the
  critical bite of the model, the exact synthetic configurations and their neighbours;
- the declared conformance updates of existing tests inventoried in strategy §5;
- the inversion of `pins.B07`, `pins.H01`, `pins.B06` and `pins.B10.2` in the `CU0` tests
  and baseline, through strategy §5 with a re-baseline record **[D-CU2-14]**.

**Infrastructure and documentation:**

- the `PRE-G9B-CU2` phase and its selections, inventory, registry pin and coverage pins,
  through the official mechanisms;
- `docs/upstream/modified-files.yml` entries;
- the `certification` and `tessellation` scenarios of the `tools/benchmark/pre-g9b-cu/`
  harness;
- the corpus manifest wording **[D-CU2-15]**, in step T12, without changing the model or its
  hashes;
- the candidate report and its machine-readable mirror;
- the status and evidence of the `CU2` methods in the scientific transfer register
  ([`pre_g9b_cu_scientific_methods_editorial_handoff_plan.md`](../../../docs/architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md)),
  without editorial text and without promoting a method without code, tests and verification;
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
- any use of a class-3 value as a rigorous input;
- any change of `CU1` contracts beyond the D-CU2-20-R conformance amendment.

**Other packages and areas:**

- `CU3`–`CU10`;
- any user-interface change beyond **[E-1]**;
- the document format and durable identities beyond this authorization, and any rewrite of
  persisted selectors or user objects such as `text2`;
- the durable dependency projection of ADR 0029;
- inferring a selection, a correspondence or a continuity by proximity, label, index or
  coordinates;
- writing the selection state of inherited algorithms, or any other change of inherited
  classes beyond registered read-only accessors;
- the continuous selection of coincident roots in the slice and any change of the inherited
  rule that suppresses an unlabelled second output **[D-CU2-18]**; substituting an undefined
  output by its certified limit value or by another output;
- general algebraic extensions that class v3 does not need;
- tolerances that turn a near-tangency into a tangency, and the symbolic capture of
  driver-independent relations;
- the legacy GeoGebra interval package, which must not be used as a certificate;
- the compatibility certificate as a fallback for any locus;
- original models; M08 and M09;
- the book `geocedg_book`: the author's editorial mandate E1 is a track obligation for `CU10`;
- promotion to `main`, tags, releases, binaries and product publication;
- any `CU0` `preserve` value beyond its tolerance.

## Architectural placement

Existence, continuity, closure, selection identity, lineage, the revision fingerprint and the
reconstruction fidelity belong to the shared Java kernel (`AGENTS.md` §4). Rendering and export
approximations derive from the kernel certificate, declare their fidelity and never decide
topology. No geometric truth moves to the user interface, an exporter or a script.

## Required design/specification

The approved texts, incorporated in step T0 before any code, ADR 0038, and the D-CU2-20-R
lifecycle of the hybrid design §8.1, written before any code of T1–T3. If an implementation
step needs a rule that these texts do not state, stop and ask the author.

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
- the revision fingerprint is exact and versioned; the slice reproduces its authoritative
  inputs exactly or fails explicitly; no certificate is reused for a state that the
  fingerprint does not distinguish;
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

1. each step T1–T12 of the strategy with its matrix rows green, the D-CU2-20-R rows complete
   before any T4 certificate is relied upon;
2. Checkstyle and the upstream boundary;
3. the official JUnit inventory update from executed evidence, with the registry pin
   re-pinned;
4. development runs of `INFRA_UNIT` and of the three `PHASE` selections.

Acceptance, on the frozen, clean candidate commit, each run with its own identity and evidence:

- `verify.ps1 -Profile STATIC`;
- `verify.ps1 -Profile INFRA_UNIT`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`;
- `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<new>`, with the
  console redirected outside the repository.

Then the `certification` and `tessellation` benchmarks before and after, with counters.
`FINAL` is not required by the class. If the regression perimeter outgrows the class, stop and
present a `VERIFICATION_ESCALATION_REQUEST`; never redefine the class.

The executable authority is the active recovery contract of `verification-levels.md`
together with `verify.ps1` and `verification-registry.json`. Report exact commands,
exit codes, run identifiers, result hashes and log paths. Gates GC-1–GC-6 and GC-8 are
acceptance criteria of the phase; GC-7, the time of GC-8 and the external DXF check are
informative.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's approval record of 2026-10-21 approves the architecture, accepts ADR 0038 as an
architectural decision, disposes **[D-CU2-1]** to **[D-CU2-19]**, **[D-CU2-20-R]** and
**[E-1]** to **[E-3]**, names the exact base and authorizes this implementation. The promotion
of `CU2` to `PASS — AUTHOR APPROVED` needs a new author review of the frozen technical
candidate, its verification and the corresponding smoke. Technical verification never implies
author approval, and no closeout is executed automatically.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only, on `phase/pre-g9b-cu2-implementation`. No push, merge, tag, release or
binary without a separate author instruction naming the exact accepted commit.

## Compatibility and serialization

No document-format change: certificates, exploratory evidence, metric events, fingerprints and
selection states are recomputed and not serialized. Persisted component selectors resolve by
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

Stop with a frozen technical candidate on the local branch, pending author review. The
candidate report records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).
The author's closeout record is the sole authority for approval; `PRE-G9B-CU2 = PASS — AUTHOR
APPROVED` is never self-declared.

## Required artifacts

The thirteen deliverables of the approval record §11, in particular:

- the exact technical commit, tree and local branch;
- the correspondence matrix between the approved decisions and the implementation;
- the incorporated specification amendments and the ADR status;
- the code and tests, with the architectural justification of their placement;
- the D-CU2-20-R evidence (exact fingerprint and faithful reconstruction);
- the certification of the author's model (critical configuration, bite, seams, folds, `e_2`)
  and of the synthetic and adversarial cases;
- benchmark and gate evidence GC-1 to GC-8 before and after, including the cost of the exact
  certificate, with deterministic counters and informative times;
- the re-baseline record of the inverted pins and the preserved baselines;
- the authenticated `STATIC`, `INFRA_UNIT`, three `PHASE` and `INTEGRATION` results;
- limitations, unresolved degeneracies and new observations;
- the `CU2` rows of the scientific transfer register, with status and evidence;
- the candidate report and its machine-readable mirror, with the workspace and remote state;
- roadmap and plan status;
- the bootstrap-impact outcome (expected: no change) and `GUIDE_IMPACT` (expected: none in
  `CU2`; the guide belongs to `CU9`).

## Stop conditions

The stop conditions of strategy §4 and §4.1 and the twelve additional conditions of the approval
record §9 apply, in particular:

- the class v3 does not certify a mandatory case of the corpus where the analysis predicts
  it, within the approved budget ranges;
- the exact algebraic certificate does not apply to a predicate that the author's model
  needs, or exhausts its approved budget, or the critical certification would need an
  unproven mathematical claim;
- the evaluator slice does not reproduce the live selection state in the author's model, or
  reproducing it would need writing into inherited classes;
- a locus of the author's model depends on an unlabelled second output at a coincident root,
  the slice does not reproduce the label state, or continuity would need substituting an
  output **[D-CU2-18]**;
- snapshot exactness or reconstruction fidelity cannot be guaranteed with the authorized
  modifications, a one-ulp change does not invalidate a semantically different state, or the
  correction would need a persistence, inherited-behaviour or durable-identity change
  **[D-CU2-20-R]**;
- a consumer needs to materialize or draw a point component beyond **[D-CU2-19]**, or to
  infer identity by proximity, index or coordinates;
- certifying `e_2` would need the value of a length;
- resolving a persisted selector would need rewriting persisted content or associating it by
  containment without a demonstrated correspondence;
- a correction needs a change to a `CU1` contract beyond D-CU2-20-R or a capability reserved
  to `CU3`;
- approved norms conflict in a way that cannot be reconciled within the authorized scope;
- a `preserve` value moves beyond its tolerance, or a numerical reference would change
  without a re-baseline record;
- an existing test could only pass by weakening it;
- the required verification exceeds the approved class;
- base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage,
  or a missing author decision.

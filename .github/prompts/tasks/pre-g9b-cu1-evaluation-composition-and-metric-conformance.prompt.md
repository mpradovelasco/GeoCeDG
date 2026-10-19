# PRE-G9B-CU1 — Locus V2 evaluation, nested composition and metric conformance

**CANONICAL PROMPT — PREPARATION CANDIDATE — PENDING AUTHOR REVIEW — IMPLEMENTATION NOT
AUTHORIZED.**

- **Origin.** Prepared by the `PRE-G9B-CU1` normative and technical preparation that the
  author authorized on 2026-10-20 ([`CU0` closeout record](../../../docs/validation/pre_g9b_cu0_closeout_record.md) §7).
  Preparation base: `df675e9ee42e1a4caac0e3bf2d74ab2d30450581` (`CU0` documentary closeout).
- **Normative candidate.** [`CU1` normative proposal](../../../docs/architecture/pre_g9b_cu1_normative_proposal.md),
  amendments A1.1-R and A1.2-R, and
  [ADR 0037](../../../docs/adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md) (PROPOSED).
- **Design.** [correction strategy](../../../docs/architecture/pre_g9b_cu1_correction_strategy.md),
  steps S0–S10.
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu1_validation_matrix.md),
  gates G1–G8.
- **Analysis.** [metric aliasing analysis](../../../docs/architecture/pre_g9b_cu1_metric_aliasing_analysis.md).
- **Decisions.** [`CU1` decision sheet](../../../docs/validation/pre_g9b_cu1_author_decision_sheet.md).
  Choices marked **[D-CU1-n]** apply only as the author decides them.

Nothing here authorizes implementation.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`, as approved in
decision C1-3 and frozen by the implementation authorization.

Make Locus V2 evaluation conform to amendment A1.1-R:

- stable semantic revision under driver-only moves, through an exact fingerprint of the
  slice inputs;
- one slice build or synchronization per revision;
- point evaluation limited to the driver-dependent subgraph;
- nested definitions that keep their revision;
- slice-scoped child sessions with shared cycle detection and work budget;
- public nested work counters;
- local between-position queries.

Make the Locus V2 metric conform to policy v2 (amendment A1.2-R):

- four guarantee classes;
- structural capability;
- certified enclosure from existing first-derivative interval models;
- estimator v2;
- tolerance coherence;
- scalar and DAG admissibility;
- memoized typed failures.

The phase corrects B10.1, H03, H04, H10, H11 and H12 against gates G1–G7, without false
guarantees: an estimate is always declared as an estimate.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the `CU1` implementation authorization **[D-CU1-11]**;
it is expected to be this preparation candidate or its author-approved closeout
descendant. Moving branch names are context only.

Entry gate:

- exact base identity and a clean worktree;
- local, tracking and live remote `main` as recorded by the authorization;
- the approved texts of A1.1-R and A1.2-R and the decisions **[D-CU1-1]** to
  **[D-CU1-12]**;
- a development run of `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0` accepted on the
  base.

Otherwise stop.

## Authority and evidence hierarchy

1. `AGENTS.md`: §1.8 (no silent approximation), §9 (design before code), §11.2–§11.4,
   §14 and §16.
2. Current code and tests.
3. The approved specifications: `locus-v2-semantics.md`, `locus-v2-metrics.md`,
   `locus-v2-public-surface.md`, `locus-v2-certified-construction-model.md` and
   `locus-v2-existence-components.md`.
4. ADRs 0007, 0013, 0022 and 0027, and ADR 0037 once accepted.
5. The `CU0` closeout record and the `CU0` geometric reference baseline (`preserve`
   and `pins`).
6. The preparation documents named above. They are evidence and design, not authority
   over the approved specifications.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

**Specifications (step S0, first).** Incorporate the approved A1.1-R and A1.2-R texts
into:

- `locus-v2-semantics.md`, v1.2;
- `locus-v2-metrics.md`, v1.5, with §25 and the adjustments to §10, §12, §13 and §15;
- a conformance note in `locus-v2-public-surface.md`.

In the same step, correct the stale status line (`metrics.md:721-722`) and the stale line
reference (`public-surface.md:398`), and set ADR 0037 to the status the author decides.

**Kernel**, under `source/shared/common/src/main/java/org/geocedg/common/kernel/`:

- `algos/`:
  - `AlgoDependentPointLocusV2`: snapshot fingerprint, and the evaluator rebuild moved
    out of `isSemanticPublicationReady`;
  - `AlgoLocusV2`;
  - `AlgoLocusMetricV2`, `AlgoLocusBetweenMetricV2` and `AlgoLocusMetricScalarAdapter`:
    policy v2 and admissibility.
- `locus/`:
  - `ReconstructibleLocusEvaluator2D`: slice lifecycle and child session;
  - `LocusEvaluationSession2D`: child sessions;
  - `LocusInstrumentation2D` and its snapshot;
  - `LocusDefinition2D` and `SemanticGeneratorDescriptor1D`, only for the snapshot.
- `locus/metric/`:
  - policy v2 and estimator v2;
  - a new structural capability;
  - a new certified-enclosure capability;
  - aggregation of classes, admissibility, the negative cache, work evidence,
    diagnostics (`CONSISTENCY_NOT_ESTABLISHED`, `PARAMETER_RESOLUTION_REACHED`) and
    instrumentation.
- A minimal public read-only facade over
  `locus/intersection/CertifiedIntervalCurveModel2D` and its two models, plus an
  outward square root. Neither the contract of those models nor the operations they
  admit may change.
- `geos/GeoLocusV2` and `geos/GeoLocusMetricResult`: instrumentation accessors only;
  display of the guarantee only as decided in **[D-CU1-3]**.

**Tests:**

- new focused, adversarial and regression tests per the validation matrix;
- the declared conformance updates of existing tests listed in strategy §4, including
  `LocusV2PointDrivenCreationRegressionTest.java:58-66` **[D-CU1-8]**;
- the inversion of `pins.B10.1`, `pins.H03` and `pins.H10_H11` in the `CU0` tests and
  baseline, through strategy §4 with a re-baseline record **[D-CU1-9]**.

**Infrastructure and documentation:**

- the `PRE-G9B-CU1` phase and its selections, inventory, registry pin and coverage pins,
  through the official mechanisms;
- `docs/upstream/modified-files.yml` entries;
- the counters in the `tools/benchmark/pre-g9b-cu/` harness;
- the candidate report and its machine-readable mirror;
- roadmap and consolidated-plan status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

**Owned by `CU2`:**

- existence certification and the compatibility certificate;
- the publication rule (H01);
- the topology and wrap event of `e_2` (B10.2);
- tessellation (B06);
- the certified construction model contract and any new admitted operation (class v3).

**Owned by `CU3`:**

- intersection solvers and residual tolerances (H02, H06);
- tangency classification or materialization.

**Other packages and areas:**

- M08 and M09;
- any user-interface change beyond **[D-CU1-3]**;
- serialization, the XML format and persisted policies, which stay unchanged;
- the legacy GeoGebra interval package, which must not be used as a certificate;
- inherited upstream classes outside the listed GeoCeDG kernel paths;
- the book;
- any `CU0` `preserve` value beyond its tolerance.

## Architectural placement

Locus metric semantics, revisions, evaluation and composition belong to the shared Java
kernel (`AGENTS.md` §4). No geometric truth moves to the user interface, an exporter or a
script.

## Required design/specification

The approved A1.1-R and A1.2-R texts, incorporated in step S0 before any code, and ADR
0037. If an implementation step needs a rule that these texts do not state, stop and ask
the author.

## Geometric invariants and degeneracies

The following hold:

- length is the semantic total variation (`metrics.md` §2) and is independent of view,
  zoom and DPI;
- cumulative length is monotone on each oriented branch;
- results are deterministic per revision and policy;
- classes and relative errors are invariant under a similarity of the construction;
- an enclosure model's refused or degenerate boxes stay unresolved and are never
  certified;
- isolated points and collapsed images have length 0.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused development, in this order:

1. each step S1–S9 of the strategy with its matrix rows green;
2. Checkstyle and the upstream boundary;
3. the official JUnit inventory update from executed evidence, with the registry pin
   re-pinned;
4. development runs of `INFRA_UNIT` and of the two `PHASE` selections.

Acceptance, on the frozen, clean candidate commit:

- `verify.ps1 -Profile STATIC`;
- `verify.ps1 -Profile INFRA_UNIT`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`;
- `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<new>`, with the
  console redirected outside the repository.

Then the `composition` benchmark before and after, with counters. `FINAL` is not required
by the class.

The executable authority is the active recovery contract of `verification-levels.md`
together with `verify.ps1` and `verification-registry.json`. Report exact commands,
exit codes, run identifiers, result hashes and log paths. Gates G1–G7 are acceptance
criteria of the phase; G8 is informative.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must:

- approve the revised metric policy and the decisions **[D-CU1-1]** to **[D-CU1-12]**;
- name the exact base commit and tree;
- authorize the implementation.

Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only. No push, merge, tag, release or binary without a separate author
instruction naming the exact accepted commit.

## Compatibility and serialization

No document-format change: metric policies and results are recomputed and are not
serialized. Displayed lengths may change by the correction of the H12 bias (about `1e-7`
in the author's model), within the `CU0` `preserve` tolerances. `Length(L)` may become
undefined only where it now publishes an estimate above tolerance, and then it carries a
typed cause.

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
  - the verification evidence (`STATIC`, `INFRA_UNIT`, both `PHASE` runs and
    `INTEGRATION`);
  - the bootstrap-impact outcome (expected: no change);
  - `GUIDE_IMPACT` (expected: none in `CU1`; the guide belongs to `CU9`);
- roadmap and plan status.

## Stop conditions

- A correction needs a change to a `CU2` or `CU3` contract: stop and present the
  dependency, without widening scope.
- A `preserve` value moves beyond its tolerance, or a numerical reference would change
  without a re-baseline record.
- Driver-only moves cannot be made revision-stable without stale results (V-REV-2
  fails).
- A guarantee class would be claimed beyond its evidence.
- Base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage,
  or a missing author decision.

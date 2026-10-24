# PRE-G9B-CU3 — Locus V2 structural points and certified contact evidence

**CANONICAL PROMPT — DESIGN AUTHOR APPROVED (2026-10-24) — IMPLEMENTATION AUTHORIZED IN TWO
INCREMENTS WITH BLOCKING GATES, ON THE EXACT BASE `80d72a50` — `CU3` IS NOT APPROVED UNTIL THE
AUTHOR REVIEWS THE FROZEN TECHNICAL CANDIDATE.**

- **Origin.** Created at step T0 by the author's instruction "PRE-G9B-CU3 — Aprobación autoral del
  diseño e implementación por incrementos" of 2026-10-24
  ([approval record](../../../docs/validation/pre_g9b_cu3_author_design_approval_record.md)), which
  approves the design of the `CU3` preparation, its focal scientific review and its final
  scientific coherence check
  ([preparation report](../../../docs/validation/pre_g9b_cu3_preparation_candidate_report.md)), and
  authorizes the implementation.
- **Normative authority.** Amendments A3.0 to A3.5 of the
  [`CU3` normative proposal](../../../docs/architecture/pre_g9b_cu3_normative_proposal.md),
  incorporated at T0 into `locus-v2-intersections.md` 1.3 ("PRE-G9B-CU3 amendment"),
  `locus-v2-certified-construction-model.md` 1.3 (§11, with the target authority of §11.3 and the
  conformance conditions of M3 in §11.4), `locus-v2-public-surface.md` §21,
  `locus-v2-point-interaction.md` §18, `locus-v2-existence-components.md` §16 and
  `g9-spatial-projection-semantics.md` §8.3; and
  [ADR 0039](../../../docs/adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md),
  accepted as architecture.
- **Design.** [implementation strategy](../../../docs/architecture/pre_g9b_cu3_implementation_strategy.md),
  steps T0–T13 and gates G-M3, G-A, G-B and G-final;
  [Stage A](../../../docs/architecture/pre_g9b_cu3_stage_a_structural_points_design.md);
  [Stage B](../../../docs/architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md);
  [normalization, admissibility and axes](../../../docs/architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md).
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu3_validation_matrix.md),
  with gate G-M3-1 to G-M3-4 and counters GC3-1 to GC3-5.
- **Analysis.** [initial state and capability analysis](../../../docs/architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md),
  including the model theorem (§5.3), the configuration of exact evaluation (§5.7) and the
  arithmetic exactness of the captured constants (§5.8).
- **Decisions.** Markers **[D-CU3-n]** point to the author's dispositions in the approval record;
  the [`CU3` decision sheet](../../../docs/validation/pre_g9b_cu3_author_decision_sheet.md) keeps
  the questions as they were presented.

The implementation is authorized only within the scope, order and stop conditions below and in
the approval record. Nothing here authorizes publication, promotion, closeout or author
approval.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE` **[D-CU3-14]**.

Close B08/B09 (the three mandatory contacts `Intersect(e_1, n)`, `Intersect(e_1, c_1)` and
`Intersect(o_1, h_2)`, and the seam contact of `e_1`), H02, H06 and the findings H-CU3-1 to
H-CU3-4 of the preparation, with the kernel capabilities they need, in two increments
**[D-CU3-19]**:

- **`CU3-A`** (I-1 to I-6):
  - rigorous enclosures of the contact residual and of its first two derivatives;
  - explicit evidence grades, with the G8B-R1 admissibility preserved **[D-CU3-6]**;
  - an additional certified acceptance path for H02;
  - the conflict rule, based only on rigorous root-count certificates **[D-CU3-13]**;
  - the contact taxonomy, including geometric folds **[D-CU3-5]**, **[D-CU3-18]**;
  - exact evaluation M3 in the decision configuration `Σ_L⁺` **[D-CU3-4]**, subject to the
    early blocking gate G-M3 and its target-authority check G-M3-4;
  - `Point(L, D)` and `Intersect(L, r, D)` by driver correspondence **[D-CU3-1]** to
    **[D-CU3-3]**;
  - the axes as reserved references with stable identity **[D-CU3-8]**;
  - compatibility (alternative (a)) and fail-closed diagnostics **[D-CU3-7]**, **[D-CU3-12]**.
- **`CU3-B`** (I-7), authorized conditionally and not executable until gate G-A passes:
  driverless materialization of contacts with established existence (classes 1 and 2), the
  versioned tangent-contact germ, identity and lifecycle, and the preimage pairs of a certified
  fold **[D-CU3-11]**.

A class-3 contact is never materialized as an intersection. The phase fails closed: what it
cannot prove stays not established, with a typed cause. A certification failure is never turned
into a silent approximation.

<!-- geocedg-field: implementation_base -->
## Implementation base

Exact base **[D-CU3-14]**: `80d72a50557a82a7a9d501a26252d1758ac6d94d`, tree
`7625ec35dbced93dd56a11ff775ce7a0f91ce62d` (the published `phase/pre-g9b-cu3-preparation`
candidate). `main` = `e6c908f2203cd8feb5d4938013e16b212e0fe016` (`CU2` closeout). Local
implementation branch `phase/pre-g9b-cu3-implementation`, created from the exact SHA. Moving
branch names are context only.

Entry gate (recorded in the approval record §1):

- exact base identity, ancestry and a clean worktree;
- the published preparation branch at the base; local and live remote `main` without an
  unexamined divergence;
- `AGENTS.md` and the current repository authority read;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`, `-Phase PRE-G9B-CU1` and
  `-Phase PRE-G9B-CU2` accepted on the base.

Otherwise stop.

## Authority and evidence hierarchy

1. `AGENTS.md`: §1.8 (no silent approximation), §9 (design before code), §11.3–§11.6, §14 and
   §16.
2. Current code and tests.
3. The approved specifications: `locus-v2-intersections.md`,
   `locus-v2-extended-intersections.md`, `locus-v2-certified-construction-model.md`,
   `locus-v2-existence-components.md`, `locus-v2-semantics.md`, `locus-v2-public-surface.md`,
   `locus-v2-point-interaction.md` and `g9-spatial-projection-semantics.md`.
4. ADRs 0008, 0010, 0013, 0017, 0019, 0027, 0038 and 0039.
5. The `CU3` approval record, the `CU0`, `CU1` and `CU2` closeout records and the `CU0` geometric
   reference baseline (`preserve` and `pins`).
6. The preparation documents named above. They are evidence and design, not authority over the
   approved specifications.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

**Specifications (step T0, first).** Incorporate the approved texts as new amendment sections,
without rewriting the approved sections of earlier phases; set ADR 0039 to
`ACCEPTED — DESIGN AUTHOR APPROVED`; add "Amended by" header lines to ADRs 0008, 0010 and 0017;
record the author's decision; create this prompt.

**Gate G-M3 (step T2, before any product code of the second incidence).** A prototype of M3 in
the benchmark harness of `tools/benchmark/pre-g9b-cu/`, with focused probes and its report, under
the conditions of the approval record §4. No product code of M3 or of `Intersect(L, r, D)` exists
before G-M3 passes.

**Kernel**, under `source/shared/common/src/main/java/org/geocedg/common/kernel/`:

- `locus/intersection/`: residual enclosures and their first two derivatives composed with the
  class-v3 model, certified critical points, root-count certificates, evidence grades, the
  certified admission path, the conflict rule and its trigger, the contact taxonomy and contact
  order, the emission of the declared rejection diagnostics; `LocusIntersectionPolicy2D`, the
  adaptive and polynomial capabilities, `PublicTargetIntersectionCapability2D`,
  `PublicIntersectionRootIdentityResolver2D` and `LocusIntersectionTokenLedger2D` only as the
  strategy states (a new ledger state format only in `CU3-B`); the target adapters unchanged in
  their contract text;
- `locus/exact/`: exact evaluation M3 (tower of quadratic extensions, exact signs, exact jets,
  budget and counters), after G-M3;
- `locus/`: capture of the decision configuration `Σ_L⁺`, reusing the class-v3 program unchanged;
- `algos/`: a new algorithm for `Point(L, D)` and `Intersect(L, r, D)`; the inherited command
  processors `CmdPoint` and `CmdIntersect` only to dispatch the new forms;
- `kernel/spatial/identity/`: the reserved axis references, under option A of **[D-CU3-8]**.

**Inherited classes:** only the minimum dispatch in the command processors and the localization
of the new syntax, registered in `docs/upstream/modified-files.yml`. No change of inherited
algorithm behaviour or selection state.

**Desktop:** the inspector rows of grades, contact classes, configurations and causes, and
messages. No geometric logic.

**Tests:** focused, adversarial and regression tests per the validation matrix; the inversion of
`pins.B08_B09` (with the seam reformulation **[D-CU3-10]**), `pins.H02` and `pins.H06` with a
re-baseline record; declared conformance updates of existing tests inventoried in step T1.

**Infrastructure and documentation:** the `PRE-G9B-CU3` phase and its selections, the JUnit
inventory, the registry pins, `docs/upstream/modified-files.yml`, the benchmark scenarios of the
harness, the candidate report and its machine-readable mirror, the `CU3` rows of the scientific
transfer register E1, and the roadmap and consolidated-plan status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- The public materialization of critical points without proved incidence; a class-3 contact as
  an intersection.
- Materialization on point components; C22, C23 and C40 **[D-CU3-9]**.
- Changes to the semantics of locus pairs beyond the approved text; the pair normalization.
- Amendment C3 of the `CU2` capture convention, and any change to approved `CU0`–`CU2`
  contracts, certificates, constants or components.
- Redefining the exact meaning of a target, its geometric authority or its binary64
  coefficients to make an incidence hold; using a deep joint recomputation `Σ_B` as authority.
- Tolerances that depend on presentation, view, zoom or DPI; configurable tolerances (H-1).
- Inferring identity, correspondence, selection or incidence from coordinates, tolerances,
  proximity, indices, labels or list order.
- Changing the residual-adapter contract text inside persisted tokens, or the formats of existing
  ledger states.
- `CU4`–`CU10`, `G9B` and any other phase.
- The book `geocedg_book`.
- Publication to `main`, tags, releases and distribution packages.

## Architectural placement

Evidence grades, contact classes, root counts, exact evaluation, structural points and the
identity of the reserved axes belong to the shared Java kernel (`AGENTS.md` §4). Probes, benches
and symbolic cross-checks stay outside it as evidence. No geometric truth moves to the user
interface, an exporter or a script.

## Required design/specification

The approved texts, incorporated in step T0 before any code, and ADR 0039. If an implementation
step needs a rule that these texts do not state, stop and ask the author.

## Geometric invariants and degeneracies

- Interval enclosures certify the existence of some root in a bracket and strict inequalities;
  they never decide an equality at a prescribed parameter or point (A3.0).
- An incidence at a prescribed point is established only by term identity under driver
  substitution or by exact evaluation, and only against the authoritative target (§11.3 of the
  certified construction model).
- The authoritative object of a structural point is `L` in its `CU2` captured configuration;
  `Intersect(L, r, D)` materializes the same exact point as `Point(L, D)`.
- A locally verified solution is withdrawn or reclassified only on a root-count certificate that
  really contradicts it; a certified critical point or a class 3 alone never does.
- Global completeness stays independent of local admissibility.
- Every result records the nature of its evidence: certified, numerical under hypotheses,
  representation approximation, limitation or not established.
- Results are deterministic per revision and independent of view, zoom, DPI and query order.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused development, in this order:

1. step T2: gate G-M3 with its report, its counters and its proposed budget, presented to the
   author for a single decision when the budget needs approval;
2. each step T3–T11 with its matrix rows green, gates G-A and G-B in their steps;
3. Checkstyle and the upstream boundary;
4. the official JUnit inventory update from executed evidence, with the registry pin re-pinned;
5. development runs of `INFRA_UNIT` and of the four `PHASE` selections.

Acceptance (G-final), on the frozen, clean candidate commit, each run with its own identity and
evidence:

- `verify.ps1 -Profile STATIC`;
- `verify.ps1 -Profile INFRA_UNIT`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU3`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`;
- `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\<new>`, with the console
  redirected outside the repository.

Then the benchmarks GC3-1 to GC3-5 before and after, with counters. `FINAL` is not required by
the class. If the regression perimeter outgrows the class, stop and present a
`VERIFICATION_ESCALATION_REQUEST`; never redefine the class. Report exact commands, exit codes,
run identifiers, result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's approval record of 2026-10-24 approves the design, accepts ADR 0039 as an
architectural decision, disposes **[D-CU3-1]** to **[D-CU3-19]**, names the exact base and
authorizes this implementation in two increments. Autonomous execution of the planned steps is
authorized without intermediate consultation for minor decisions of implementation,
organization, tests or documentation. The promotion of `CU3` to `PASS — AUTHOR APPROVED` needs a
new author decision on the frozen technical candidate, its verification and the author's manual
check. Technical verification never implies author approval, and no closeout is executed
automatically.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only, on `phase/pre-g9b-cu3-implementation`. No push, merge, tag, release or
package without a separate author instruction naming the exact accepted commit.

## Compatibility and serialization

`Point(L, D)` and `Intersect(L, r, D)` serialize as ordinary commands with their durable
identities; no parameter, certificate, configuration or grade is persisted. Current readers
reject the new forms and fail closed (P10), with a migration note. Existing transverse tokens keep
resolving while their meaning holds; solutions contradicted by a root-count certificate lose their
admissibility with a typed cause and are never reassociated. New capabilities use new versioned
formats.

## Integration obligations

- Register new source and test files, and every inherited-class change, in
  `docs/upstream/modified-files.yml`.
- Keep `final.shared` and `final.desktop` green without weakened tests.
- Update the inventory only through the official updater with executed evidence.
- Record every changed numerical reference and every inverted pin in a re-baseline record.

## Acceptance and closeout

Stop with a frozen technical candidate on the local branch, pending author review. If gate G-B
fails, stop `CU3-B` and present `CU3-A` as a verified partial candidate. The candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`). The author's
closeout record is the sole authority for approval; `PRE-G9B-CU3 = PASS — AUTHOR APPROVED` is
never self-declared.

## Required artifacts

The single review package of the approval record §11: exact Git identity, executed decisions,
results of G-M3, G-A, G-B and G-final, evidence of tests and benchmarks, compatibility and
migration, limitations and future work, book and repository state, and the manual checks left to
the author; with the bootstrap-impact outcome (expected: no change) and `GUIDE_IMPACT` (expected:
none in `CU3`; the guide belongs to `CU9`).

## Stop conditions

Stop and present the evidence when:

- gate G-M3 fails any of its criteria, including the target authority G-M3-4; then stop the
  dependent parts and present alternatives R-1, R-2, R-3 and C3, none authorized automatically;
- the proposed M3 budget needs author approval; present it with the G-M3 result for one
  decision;
- a correction would need a change to an approved contract of `CU0`–`CU2`, to the geometric
  authority of a target or to the `CU2` capture convention;
- a normative or identity incompatibility appears;
- a mandatory guarantee cannot be satisfied;
- an existing test could only pass by weakening it, or a `preserve` value moves beyond its
  tolerance, or a numerical reference would change without a re-baseline record;
- the required verification exceeds the approved class;
- base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage, or a
  missing author decision.

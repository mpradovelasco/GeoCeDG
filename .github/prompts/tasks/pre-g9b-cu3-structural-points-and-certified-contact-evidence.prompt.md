# PRE-G9B-CU3 — Locus V2 structural points and certified contact evidence

**CANONICAL PROMPT — CONSTRUCTIVE AUTHORITY ARCHITECTURE AND NORMATIVE TEXTS AUTHOR APPROVED
(2026-10-25) — IMPLEMENTATION RESUMED FROM STEP T2 IN INCREMENTS E0 TO E4 WITH BLOCKING GATES, ON
THE EXACT BASE `3cd0f710` — `CU3` IS NOT APPROVED UNTIL THE AUTHOR REVIEWS THE FROZEN TECHNICAL
CANDIDATE.**

- **Origin.** Created at step T0 by the author's instruction "PRE-G9B-CU3 — Aprobación autoral del
  diseño e implementación por incrementos" of 2026-10-24
  ([design approval record](../../../docs/validation/pre_g9b_cu3_author_design_approval_record.md)).
  The implementation stopped at step T2 because gate G-M3 failed on the authority of the target
  (G-M3-4; [gate report](../../../docs/validation/pre_g9b_cu3_m3_gate_report.md)). Updated at
  increment E0 by the author's instruction "PRE-G9B-CU3 — Aprobación autoral de la autoridad
  constructiva e implementación por incrementos" of 2026-10-25
  ([constructive authority approval record](../../../docs/validation/pre_g9b_cu3_constructive_authority_approval_record.md)),
  which approves the combination A2+B2+C+D as normative architecture, decisions D-AUT-1 to
  D-AUT-12 with the conciliation of 2026-10-25, ADR 0040, texts AUT-1 to AUT-7 and the increments
  E0 to E4 with their gates, and resumes `CU3` from T2.
- **Normative authority.** Under `constructive-authority/v1`:
  [`locus-v2-constructive-authority.md`](../../../geocedg/specs/locus/locus-v2-constructive-authority.md)
  1.0 (AUT-1, AUT-2, AUT-3, AUT-5 and AUT-6); the constructive authority amendments (version 1.4)
  of `locus-v2-intersections.md` (AUT-7) and `locus-v2-certified-construction-model.md` §12 (AUT-4
  and AUT-7); the consumer note of `locus-v2-existence-components.md` §17; and
  [ADR 0040](../../../docs/adr/0040-constructive-geometric-authority-and-relation-integrity.md),
  accepted as architecture. Under `legacy-representation-authority/v1` and in every document that
  has not adopted the policy: amendments A3.0 to A3.5 of the
  [`CU3` normative proposal](../../../docs/architecture/pre_g9b_cu3_normative_proposal.md),
  incorporated at T0 into `locus-v2-intersections.md` 1.3,
  `locus-v2-certified-construction-model.md` 1.3 (§11), `locus-v2-public-surface.md` §21,
  `locus-v2-point-interaction.md` §18,
  `locus-v2-existence-components.md` §16 and `g9-spatial-projection-semantics.md` §8.3, and
  [ADR 0039](../../../docs/adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md).
- **Design.** [constructive authority normative proposal](../../../docs/architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md),
  §6 (increments E0 to E4, gates G-AUT-0 to G-AUT-10, autonomy and stop conditions) and §13 (single
  consolidated recommendation); [implementation strategy](../../../docs/architecture/pre_g9b_cu3_implementation_strategy.md)
  for the pieces I-1 to I-7 that the increments contain;
  [Stage A](../../../docs/architecture/pre_g9b_cu3_stage_a_structural_points_design.md);
  [Stage B](../../../docs/architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md);
  [normalization, admissibility and axes](../../../docs/architecture/pre_g9b_cu3_residual_normalization_and_axes_design.md).
- **Validation.** [validation matrix](../../../docs/validation/pre_g9b_cu3_validation_matrix.md),
  §1 to §5 and the V-AUT rows of §6, mandatory since 2026-10-25; rows that decide in `Σ_L⁺` or
  against the binary64 authority of a target are read with AUT-7 under the constructive policy.
- **Analysis and evidence.** [initial state and capability analysis](../../../docs/architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md);
  [constructive authority study](../../../docs/architecture/pre_g9b_cu3_constructive_authority_study.md);
  the research prototypes `PreG9BCu3M3*`, `PreG9BCu3AuthorityStudyProbe` and
  `PreG9BCu3ConstructiveIdentityProbe` under `tools/benchmark/pre-g9b-cu/cu3-java/`. Prototype
  results are evidence, never a verified product implementation.
- **Decisions.** Markers **[D-CU3-n]** point to the 2026-10-24 design approval record; markers
  **[D-AUT-n]** point to the 2026-10-25 approval record; D-AUT-11 revises D-CU3-3, -4, -5, -6,
  -7, -10 and -13 and G-M3-4 under the constructive policy.

The implementation is authorized only within the scope, order and stop conditions below and in
the approval records. Nothing here authorizes publication, promotion, closeout or author
approval.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE` **[D-CU3-14]**, **[D-AUT-10]**.

Close B08/B09 (the three mandatory contacts `Intersect(e_1, n)`, `Intersect(e_1, c_1)` and
`Intersect(o_1, h_2)`, and the seam contact of `e_1`), H02, H06 and the findings H-CU3-1 to
H-CU3-4 under the constructive geometric authority, in increments with blocking gates:

- **E0. Contracts.** Incorporate the approved texts as new sections, without rewriting approved
  sections; record the decision; update this prompt; no product code.
- **E1. Queryable integrity.** `RelationIntegrity`, evidence records, propagation by premises,
  consumer requirements and integrity queries; the constructive evaluator with the class table,
  the semantics of points on paths, declared substitutes and product exact evaluation with its
  budget; the policy record with fail-closed older readers and the explicit adoption event with
  its impact report; rigorous enclosures of the constructive values of captured constants and,
  with them, the transfer of the `CU2` certificates that the new constructions use; the relaxed
  policy version 1 with `SUBSTITUTE_GENERIC` and the re-qualification of G8B-R1 at adoption; the
  inherited decisions of the class table; `Point(L, D)` and `Intersect(L, r, D)` under the
  constructive authority; and the pieces I-1 to I-6 of `CU3-A` that the matrix requires (gate
  G-A).
- **E2. Strict certification.** The constructive identity certificate v1 in the product, with the
  structural key `cic-structural-key/v1` by durable identity; promotion of `RELAXED` to `STRICT`
  and demotion by contradiction without new identity; contact order by exact jets.
- **E3. Conflict and Stage B.** The parity certificate by exact symmetry in the conflict rule; the
  reclassification of the seam with proof and the inversion of `pins.B08_B09`; `CU3-B`
  (driverless materialization) under the constructive authority (gate G-B).
- **E4. Closeout.** Freeze; `INTEGRATED_PHASE` verification (G-final); candidate report; manual
  check left to the author.

The mandatory objective is that both incidences of every intersection reach `STRICT` when the
construction and its conditions justify it. A class-3 contact is never materialized as an
intersection. The phase fails closed: what it cannot prove stays `NOT_ESTABLISHED`, with a typed
cause. A certification failure is never turned into a silent approximation.

<!-- geocedg-field: implementation_base -->
## Implementation base

Exact base **[D-AUT-10]**: `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, tree
`9e4984db1dec327e47afeec2c1072f78c6e9cc49` (the published conciliation of
`phase/pre-g9b-cu3-authority-study`). The local implementation branch
`phase/pre-g9b-cu3-implementation`, stopped at `1a0903aa`, advances to the base by fast-forward,
so the evidence of gate G-M3 stays in its history. `main` =
`e6c908f2203cd8feb5d4938013e16b212e0fe016` (`CU2` closeout). Moving branch names are context only.

Entry gate (recorded in the 2026-10-25 approval record §1):

- exact base identity, ancestry from `1a0903aa` and a clean worktree;
- the published branch at the base; local and live remote `main` without an unexamined
  divergence;
- `AGENTS.md` and the current repository authority read;
- `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`, `-Phase PRE-G9B-CU1` and
  `-Phase PRE-G9B-CU2` accepted on the base.

Otherwise stop.

## Authority and evidence hierarchy

1. `AGENTS.md`: §1 (in particular §1.8, no silent approximation), §9 (design before code),
   §11.3–§11.6, §14 and §16.
2. Current code and tests.
3. The approved specifications: `locus-v2-constructive-authority.md`, `locus-v2-intersections.md`,
   `locus-v2-extended-intersections.md`, `locus-v2-certified-construction-model.md`,
   `locus-v2-existence-components.md`, `locus-v2-semantics.md`, `locus-v2-public-surface.md`,
   `locus-v2-point-interaction.md` and `g9-spatial-projection-semantics.md`.
4. ADRs 0008, 0010, 0013, 0017, 0019, 0027, 0028, 0029, 0037, 0038, 0039 and 0040.
5. The two `CU3` approval records, the `CU0`, `CU1` and `CU2` closeout records and the `CU0`
   geometric reference baseline (`preserve` and `pins`).
6. The preparation, study and consolidation documents named above. They are evidence and design,
   not authority over the approved specifications.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

**Contracts (increment E0, first).** Incorporate the approved texts as new sections, without
rewriting the approved sections of earlier phases: the new specification
`locus-v2-constructive-authority.md`; the version 1.4 sections of `locus-v2-intersections.md` and
`locus-v2-certified-construction-model.md`; the consumer note of `locus-v2-existence-components.md`;
ADR 0040 accepted as architecture; "Amended by" header lines in ADRs 0008, 0013, 0017, 0028, 0037,
0038 and 0039; record the decision; update this prompt and the status of the affected documents.

**Kernel**, under `source/shared/common/src/main/java/org/geocedg/common/kernel/`:

- a new package for the constructive authority (policy, class table, constructive evaluation,
  integrity, relaxed admission, transfer records, constructive identity certificates and their
  structural keys), reusing the class-v3 program, the exact arithmetic of `locus/exact/` and the
  interval models of `locus/intersection/` without changing their contracts;
- `locus/intersection/`: residual enclosures and their first two derivatives, evidence grades, the
  certified admission path, the conflict rule and its trigger, the contact taxonomy, contact order
  and the parity certificate, as the matrix requires; `LocusIntersectionPolicy2D`, the adaptive
  and polynomial capabilities, `PublicTargetIntersectionCapability2D`,
  `PublicIntersectionRootIdentityResolver2D` and `LocusIntersectionTokenLedger2D` only as the
  increments require (a new ledger state format only for `CU3-B`); the target adapters unchanged
  in their contract text;
- `locus/exact/`: product exact evaluation (tower of quadratic extensions, exact signs, exact jets,
  budget and counters) and the multivariate rational-function arithmetic of the certificate;
- `algos/`: a new algorithm for `Point(L, D)` and `Intersect(L, r, D)`; the inherited command
  processors `CmdPoint` and `CmdIntersect` only to dispatch the new forms;
- `spatial/identity/`: the authority policy record of the document and its codec, the adoption
  event and its impact report, and the reserved axis references under option A of **[D-CU3-8]**.

**Inherited classes:** only the minimum dispatch in the command processors, the localization of
the new syntax and the minimum hooks that persistence and Undo of the policy record need, each
registered in `docs/upstream/modified-files.yml`. No change of inherited algorithm behaviour or
selection state.

**Desktop:** the inspector rows of integrity, premises, grades, contact classes and causes, the
explicit adoption action with its impact report, and messages. No geometric logic.

**Tests:** focused, adversarial and regression tests per the validation matrix, including the V-AUT
rows; the inversion of `pins.B08_B09` (with the seam reformulation **[D-CU3-10]**, revised by
D-AUT-11), `pins.H02` and `pins.H06` with a re-baseline record; declared conformance updates of
existing tests.

**Infrastructure and documentation:** the `PRE-G9B-CU3` phase and its selections, the JUnit
inventory, the registry pins, `docs/upstream/modified-files.yml`, the benchmark scenarios of the
harness, the candidate report and its machine-readable mirror, the `CU3` rows of the scientific
transfer register E1 (including the planned `CU10` exposition), and the roadmap and
consolidated-plan status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Promotion of the constructive authority to the default policy; a document that changes policy
  without the explicit adoption event; a mixed document.
- Changes to the semantics of inherited commands, or integrity derived from their verdicts.
- A general extension of the constructive identity certificate beyond its approved class.
- The public materialization of critical points without proved incidence; a class-3 contact as
  an intersection; `RELAXED` presented as a proof or used as a premise of `STRICT`.
- Materialization on point components; C22, C23 and C40 **[D-CU3-9]**.
- Changes to the semantics of locus pairs beyond the approved text; the pair normalization.
- Changes to the `CU2` capture convention, to the text, fingerprint or keys of `CU2` certificates,
  and to approved `CU0`–`CU2` contracts outside the approved amendments.
- Redefining the exact meaning of a target to make an incidence hold; using a deep joint
  recomputation `Σ_B` as authority; deciding an incidence against a reconstruction chosen for it.
- Tolerances that depend on presentation, view, zoom or DPI; configurable tolerances (H-1);
  replacing a failed proof by a tolerance.
- Inferring identity, correspondence, selection or incidence from coordinates, tolerances,
  proximity, indices, labels, list order or runtime object references.
- Changing the residual-adapter contract text inside persisted tokens, the formats of existing
  ledger states, or any identity, token, selector or lineage key.
- Adopting a versioned budget that the contracts leave to the author without the author's
  decision.
- `CU4`–`CU10`, `G9B` and any other phase.
- The book `geocedg_book`.
- Publication to `main`, tags, releases and distribution packages.

## Architectural placement

The constructive authority, the class table, relation integrity, relaxed admission, transfer
records, constructive identity certificates, evidence grades, contact classes, root counts, exact
evaluation, structural points and the identity of the reserved axes belong to the shared Java
kernel (`AGENTS.md` §4). The policy record belongs to the persisted document model. Probes, benches
and symbolic cross-checks stay outside the kernel as evidence. No geometric truth moves to the user
interface, an exporter or a script.

## Required design/specification

The approved texts, incorporated in increment E0 before any code, ADR 0039 and ADR 0040. If an
implementation step needs a rule that these texts do not state, stop and ask the author.

## Geometric invariants and degeneracies

- Under `constructive-authority/v1`, every object has one geometric meaning, its ideal
  construction; every numerical state is a representation, and the meaning of a target is never
  redefined to make an incidence hold.
- Constructive identity, the integrity of relations and the precision of representations are
  independent dimensions. Integrity propagates only through the premises each proof uses.
- `RELAXED` is a controlled computational admission, never a proof of an exact incidence; no
  relation rigorously contradicted under the authority in force is admitted as `RELAXED`.
- No relation acquires `STRICT` through a decision that is not rigorously established, nor from a
  finite sample.
- A transfer of a `CU2` certificate needs transfer enclosures that contain the ideal geometric
  values, including those that come from points on paths, trigonometric constructions and objects
  outside the exact class; an error estimate or a numerically wide box never replaces that proof.
  Without an established transfer, `P ∈ L` is not published as `STRICT`.
- Interval enclosures certify the existence of some root in a bracket and strict inequalities;
  they never decide an equality at a prescribed parameter or point (A3.0).
- A locally verified solution is withdrawn or reclassified only on a rigorous certificate that
  really contradicts it; a certified critical point or a class 3 alone never does.
- Global completeness stays independent of local admissibility.
- Every result records the nature of its evidence. Results are deterministic per revision and
  independent of view, zoom, DPI and query order.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused development, in this order:

1. each increment with its matrix rows green and its gates: G-AUT-0 in E0; G-AUT-1, G-AUT-3 to
   G-AUT-8 and G-A in E1; G-AUT-2, G-AUT-8 to G-AUT-10 in E2; G-B in E3;
2. the versioned budgets that the contracts leave to the author, presented with their
   deterministic counters for one decision before the code that needs them;
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

Then the benchmarks GC3-1 to GC3-5 and the cost counters of the constructive evaluation and of the
certificate. `FINAL` is not required by the class. If the regression perimeter outgrows the class,
stop and present a `VERIFICATION_ESCALATION_REQUEST`; never redefine the class. Report exact
commands, exit codes, run identifiers, result hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's approval record of 2026-10-25 approves the constructive authority architecture and its
normative texts, disposes **[D-AUT-1]** to **[D-AUT-12]**, names the exact base and authorizes the
autonomous execution of E1, E2, E3 and E4 after E0, respecting their dependencies, budgets and stop
conditions, without intermediate consultation for minor decisions of implementation,
organization, tests or documentation. The promotion of `CU3` to `PASS — AUTHOR APPROVED` needs a
new author decision on the frozen technical candidate, its verification and the author's manual
check. Technical verification never implies author approval, and no closeout is executed
automatically.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Local commits only, on `phase/pre-g9b-cu3-implementation`. The publication foreseen by D-AUT-12 is
already executed and authorizes no further publication. No push, merge, tag, release or package
without a separate author instruction naming the exact accepted commit.

## Compatibility and serialization

Old documents keep their authority and admissibility until an explicit adoption; new documents
also start under the legacy policy during `CU3`. The adoption is explicit, atomic, undoable and
reported before it is applied. The policy record is the only new persisted datum; integrity,
transfer records, certificates and their evidence are never persisted. Readers that do not support
the record fail closed and load nothing. `Point(L, D)` and `Intersect(L, r, D)` serialize as
ordinary commands with their durable identities; no parameter, certificate, configuration, grade or
integrity is persisted, and current readers reject the new forms and fail closed (P10), with a
migration note. No identity, token, selector or lineage key is modified or reassociated. `CU2`
certificates keep their full authority over their captured configuration; a constructive transfer is
new and separate evidence. New capabilities use new versioned formats.

## Integration obligations

- Register new source and test files, and every inherited-class change, in
  `docs/upstream/modified-files.yml`.
- Keep `final.shared` and `final.desktop` green without weakened tests.
- Update the inventory only through the official updater with executed evidence.
- Record every changed numerical reference and every inverted pin in a re-baseline record.

## Acceptance and closeout

Stop with a frozen technical candidate on the local branch, pending author review. If E2 does not
pass its gates, E1 may be presented as a verified partial candidate and `CU3` stays incomplete. If
gate G-B fails, `CU3-B` stops without lowering any evidence. The candidate report records invariant
facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`). The author's closeout record is the
sole authority for approval; `PRE-G9B-CU3 = PASS — AUTHOR APPROVED` is never self-declared.

## Required artifacts

The single review package of the 2026-10-25 approval record §13: exact Git identity; the state of
E0 to E4 and of every gate; results and rigorous evidence; the state of both incidences of the five
contacts; compatibility and persistence; degeneracies and limitations; computational cost and
approved budgets; the manual checks left to the author; with the bootstrap-impact outcome (expected:
no change) and `GUIDE_IMPACT` (expected: none in `CU3`; the guide belongs to `CU9`).

## Stop conditions

Stop and present the evidence when:

- a blocking gate does not pass;
- a scientific contradiction appears;
- a change to an approved contract outside AUT-1 to AUT-7 would be needed, including the `CU2`
  capture convention or the geometric authority of a target under the legacy policy;
- an identity could be reassociated or lost;
- an incompatibility with earlier documents appears;
- a new normative budget needs approval;
- a mandatory guarantee cannot be demonstrated;
- an existing test could only pass by weakening it, or a `preserve` value moves beyond its
  tolerance, or a numerical reference would change without a re-baseline record;
- the required verification exceeds the approved class;
- base identity, a dirty worktree, remote divergence, incomplete or untrusted coverage, or a
  missing author decision.

Failed tests are never replaced by tolerances, and the geometric authority is never reinterpreted
silently.

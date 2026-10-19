# PRE-G9B-CU0 — author closeout record

```text
PRE-G9B-CU0                    = PASS — AUTHOR APPROVED
AUTHOR SMOKE                   = NOT PERFORMED — NOT APPLICABLE
                                 (no productive change; review of the deliverables and evidence)
APPROVED TECHNICAL CANDIDATE   = 636a0070b6f4e32a8f270c25cfa70d9bd3b284b7
                                 tree 8fac2cae5c799fe7da215641c2a94ae95044a84d
VERIFICATION RECORD DESCENDANT = d684e74050988a3051a1692bfcf0651440b1567a
                                 tree 6c59bdbfbee7a9dbba1b4d0a0f45e290d500c811
HISTORICAL BASE                = 48a0775e4f2f122f5e2614c62b5773777752ccd1 (main = origin/main)
VERIFICATION CLASS             = INTEGRATED_PHASE: PHASE PRE-G9B-CU0 + INTEGRATION, ACCEPTED / COMPLETE
FINAL / RECEIPT                = NOT REQUIRED BY THE CLASS — NONE CLAIMED
CU0 DESIGN CANDIDATES          = CANDIDATE — NOT NORMATIVE (analysis D, proposals E–H, amendments A1.1–A3.4)
C1-0 / C1-1 / C1-3 / C1-4      = APPROVED
C1-2                           = APPROVED WITH DESIGN CONDITION (A1.2 revised before implementation)
PRE-G9B-CU1                    = NORMATIVE AND TECHNICAL PREPARATION AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-CU2 … CU10             = NOT AUTHORIZED
PUBLICATION / TAG / main       = NOT AUTHORIZED
BOOK                           = UNCHANGED — PAUSED AFTER BOOK-FIGPOL-PILOT
selfApproved                   = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-CU0. Aprobación autoral,
cierre y autorización de preparación de CU1" (author decision of 2026-10-20). The author
reviewed the candidate, its deliverables published on the review branch and an independent
technical analysis of them. This record is the sole authority for the author approval of
`CU0`.

Under the frozen-candidate rule, the following keep the states they recorded when they were
written and are not amended:

- the [candidate report](pre_g9b_cu0_baseline_candidate_report.md);
- the [verification record](pre_g9b_cu0_verification_record.md);
- the [decision sheet](pre_g9b_cu0_author_decision_sheet.md);
- the design and amendment documents of the candidate.

This closeout descends linearly from the verification-record descendant. It changes only
documentation and its machine-readable mirror,
[`pre-g9b-cu0-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu0-closeout.json),
and it reruns no acceptance profile.

## 1. Author decision

| Field | Value |
|---|---|
| Instruction | "GeoCeDG — PRE-G9B-CU0. Aprobación autoral, cierre y autorización de preparación de CU1", author decision of 2026-10-20 |
| Decision | `CU0` approved as `PASS — AUTHOR APPROVED`, with the precisions transcribed in §3 to §6 |
| Approval scope | the exact reviewed content: technical candidate `636a0070` and its documentary descendant `d684e740`; no further change inside `CU0` is authorized |
| Accepted deliverables | characterization, regression corpus, reproducible benchmark, versioned tests, windowed reproduction of B02/B03, guide coverage matrix, consolidated plan and architectural proposals |
| Accepted evidence | `STATIC` 3/3, `INFRA_UNIT` 22/22, `PHASE -Phase PRE-G9B-CU0` 2/2 and `INTEGRATION` 19/19, all `ACCEPTED / COMPLETE` (§2) |
| Standing diagnostics | `diagnostic.governance` and `diagnostic.historical-consistency` are kept without reinterpretation |
| Author smoke | not claimed. `CU0` introduced no productive change, and the review was based on its deliverables and technical evidence |
| Normative status | approving `CU0` does not make its candidate designs approved specifications; analysis D, proposals E to H and amendments A1.1 to A3.4 remain candidates |
| Separation | the author accepts the separation between the technical candidate and its documentary descendant. The verifications belong to `636a0070`; `d684e740` records their evidence and changes no executable input |
| Authorized next work | bounded normative and technical preparation of `CU1` (§7); implementation of `CU1`, productive code changes, functional metric changes and `CU2`–`CU10` are not authorized |
| Not authorized | publication, tags, changes to `main`, any book change |

## 2. Approved identities and authentication (2026-10-20)

| Item | Value | Check |
|---|---|---|
| technical candidate | `636a0070b6f4e32a8f270c25cfa70d9bd3b284b7`, tree `8fac2cae5c799fe7da215641c2a94ae95044a84d` | exact; parent `444ad46b1c18a6101dae68a75a8d7a5f92ed83fe` (local planning candidate, tree `1aff549ccf3c0deff5581ee7942d566bd5cc4e19`) |
| verification-record descendant | `d684e74050988a3051a1692bfcf0651440b1567a`, tree `6c59bdbfbee7a9dbba1b4d0a0f45e290d500c811` | direct child of the candidate; delta of four documents: the verification record (added), the candidate report, the decision sheet and the roadmap. No code, test, registry, inventory or evidence path |
| historical base | `48a0775e4f2f122f5e2614c62b5773777752ccd1`, tree `75ebf03f089a5effa02daa4a68be360b7499fdd9` | parent of `444ad46b`; equal to local `main`, `origin/main` and live remote `main` |
| ancestry | `48a0775e` → `444ad46b` → `636a0070` → `d684e740` | linear; `git merge-base --is-ancestor` confirmed each step |
| delta base → candidate | 38 files: the two planning documents of `444ad46b` plus the 36 files of `636a0070` | no source change outside `src/test`; 9 test sources added and registered in `docs/upstream/modified-files.yml` |
| `git diff --check` | `48a0775e..d684e740` | exit 0 |
| review branch | `phase/pre-g9b-cu0-candidate` | live remote ref equal to `d684e740` |
| worktree | clean before this closeout | — |

Each result was re-authenticated on 2026-10-20 with `Read-VerificationJson` and
`Get-VerificationResultHash`. For every run the recorded and recomputed hashes were equal,
`run_state` and the run journal were `COMPLETED`, and the candidate commit and tree were the
exact identities above.

| Check | Commit | `run_id` | `result_hash` | Result |
|---|---|---|---|---|
| `STATIC` | `636a0070` | `verification-3ebfbcdd2be443da830df7036e1f1d84` | `c58fe1613e755818ea5e700963cc10d263338059b9bed1ae569d36f35f6f03bf` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |
| `INFRA_UNIT` | `636a0070` | `verification-0ee52198f76e4633b2405ee4dfb9bb50` | `0b678cd3ec7fd8b3bc84e3a95c435e73bbd9b047f6a1b5d42945fff35808906d` | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU0` | `636a0070` | `verification-2567deb5439140978837d7be1b9d6f46` | `61204b2fcfac3b1a27efa87a2eb1efa762f7b7c19eb789cf31feb4d4cd2d9334` | `ACCEPTED / COMPLETE`, 2/2; 21 tests, 0 failures, 0 skipped |
| `INTEGRATION` | `636a0070` | `verification-439437f91e4a4d358e0825c5ad1880c9` | `1a3816eede3fecbd02895183e082b29fa37d72bb0811dcf76259504aec482dfc` | `ACCEPTED / COMPLETE`, 19/19; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; Desktop 2174 tests (1 skipped), shared 6992 (10 skipped), 0 failures; standing diagnostics only |
| `STATIC` | `d684e740` | `verification-a74e57b005e14c09b6ec25933f1647ac` | `50a0240d835f618562ce0a9cd2bc0f8082a10c52657f591af5294241a897e7e5` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |

The registered class `INTEGRATED_PHASE` requires the registered `PHASE` selection plus
`INTEGRATION` on the same exact commit and tree; both are present. `FINAL` is not required
for this class, and no receipt is expected or claimed. `tools/agent/phase-closeout.ps1
-Action INSPECT` therefore does not apply, because it inspects a `FINAL` receipt. The
precedent is the `PRE-G9B-R6-plus-B` closeout.

No required evidence is missing, so the closeout proceeds. The evidence is kept in the
ignored directories `artifacts/agent/pre-g9b-cu0-{static,infra-unit,phase,integration}-636a0070/`
and `artifacts/agent/pre-g9b-cu0-record-static-d684e740/`.

The two standing diagnostics (`diagnostic.governance` = `DIAGNOSTIC_FINDING`,
`diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`) are recorded as reported and
are not reinterpreted.

## 3. Decisions C1-0 to C1-4

| Id | Author disposition |
|---|---|
| C1-0 | Approved. `CU0` becomes `PASS — AUTHOR APPROVED`, subject to this documentary closeout. The base of the `CU1` preparation is `d684e740` or its authorized and verified closeout descendant; this closeout commit is that descendant |
| C1-1 | Approved. The main `CU1` performance criteria are deterministic counters: index rebuilds, slice constructions, revisions and nested evaluations. Drag and load times are complementary indicators, never substitutes for the semantic criteria |
| C1-2 | Approved with a design condition. `CU1` includes B10.1, H03, H04 and the metric defects H10, H11 and H12. The candidate metric policy A1.2 must be revised before implementation, under the precision of §4 |
| C1-3 | Approved. Planned verification class: `INTEGRATED_PHASE`, with its own `PHASE` and `INTEGRATION` under the live registry |
| C1-4 | Approved. The geometric `preserve` references stay as regression evidence, separate from the `pins` that characterize defects; a `pin` is inverted or replaced only by an acceptance test. No numerical reference is changed silently |

## 4. Binding precision on H10: aliasing in `LocusLength`

The author adopts the following finding of the independent analysis. Amendment A1.2
proposed a minimum subdivision depth plus agreement of two consecutive levels. That is
necessary, but it does not guarantee a correct arc length for a general evaluator.

- For `f(x) = sin(2πkx)` on `[0, 1]`, a finite mesh can sample only zeros of `f` and estimate
  the length as 1, while the true length is much larger. Two successive refinements can give
  the same wrong estimate.
- In general, a finite number of point evaluations cannot certify the length of an
  arbitrarily oscillating curve without additional restrictions on its regularity or
  complexity.

Design requirement for the revised A1.2 (to be prepared in `CU1` preparation and approved
before implementation). The revised policy must:

1. Distinguish four result classes:
   1. length computed by a structural or analytic capability with demonstrable guarantees;
   2. length obtained numerically with a justified error bound;
   3. length estimated by refinement without sufficient guarantee to certify the error;
   4. computation not established, because of an exhausted budget, a degeneration or
      insufficient information.
2. Investigate the capabilities actually available in GeoCeDG to obtain additional
   guarantees:
   - variation, derivative or curvature bounds where they exist;
   - the structure of the analytic providers and of SplineV2;
   - error estimates justified by explicit hypotheses;
   - adaptive refinement and consistency checks;
   - deterministic budgets;
   - explicit treatment of uncertainty.
3. Introduce no false precision guarantee based only on the agreement of two chord sums.
4. Never publish a length whose error is not sufficiently established with a guarantee
   higher than the available evidence.
5. State explicitly under which conditions a result may admissibly feed another construction
   of the DAG.
6. Avoid turning every general metric into an undefined value unnecessarily. The policy
   distinguishes an estimate usable under an explicit contract from a certified result,
   with no silent approximation.

H11 and H12 are investigated jointly:

- metric budget exhaustion for circles of moderate radius;
- absolute and relative tolerances;
- distribution of the error budget over subintervals;
- adequacy of the budgets to the necessary work;
- discrepancy between declared precision and actual error;
- consistency between the rich result and the scalar adapter.

No fixed budget is claimed to resolve every regular curve of arbitrary complexity. The
proposal must resolve the characterized examples and set honest limits for the general case.

## 5. `CU1` scope (confirmed)

The scientific scope of the consolidated plan is kept.

**Evaluation and composition:**

- stability of semantic revisions;
- removal of rebuilds per point query;
- correct reuse of isolated constructions and metric indexes;
- propagation of evaluation sessions;
- cycle detection and control of nested work;
- reproducible instrumentation.

**Metrics:**

- correction of the H10 aliasing;
- treatment of budget exhaustion (H11);
- coherence of errors and tolerances (H12);
- preservation of valid semantic results;
- correct cache invalidation.

**Outside `CU1`:**

- existence-component certification (`CU2`);
- any change to the topology of `e_2`;
- tangency materialization (`CU3`);
- the new M08 and M09 functions;
- interface improvements;
- editorial changes to the book.

If a `CU1` correction requires changing a contract owned by `CU2` or `CU3`, the work stops and
presents the dependency, without widening the scope implicitly.

## 6. Observations registered for later packages

These observations do not block `CU1`. Each must be incorporated into the design of its
package, and nothing is implemented now.

| Id | Package | Observation |
|---|---|---|
| `OBS-CU0-CU3-TANGENT-EXISTENCE` | `CU3` | Certifying an isolated critical point `t*` with `h'(t*) = 0`, `h''(t*) ≠ 0` and `abs(h(t*)) ≤ ε` does not necessarily prove that `h` has a root. Four classes must be distinguished strictly: a structurally established contact, a tangency whose existence is certified, a numerical approximation to a contact, and a critical point without intersection. The authoritative materialization of `TANGENT_CERTIFIED_ISOLATED` as formulated in decision F-2 is **not approved** until that distinction is resolved. The objective of materializing the three contacts of the author's model is kept, without turning tolerated approximations into exact incidences |
| `OBS-CU0-CU6-M07-SELECTION-IDENTITY` | `CU6` | A label may introduce a textual reference into the command, but it does not prove the identity of a previously selected object. The design must check that the selection remains valid after Undo, Redo or rebuild, and request a new selection when continuity cannot be established. An object that later acquires the same label is never reassociated silently |

## 7. Work authorized after this closeout

The author authorizes only a bounded normative and technical preparation of `CU1`.

**Tasks:**

1. Inspect the relevant Locus V2 evaluation, metric, revision and composition contracts and
   code.
2. Revise A1.1 and especially A1.2 under §4.
3. Propose a technically realizable metric policy with explicit guarantees and a correct
   treatment of numerical insufficiency.
4. State which changes are conformance with approved contracts and which require a
   normative extension.
5. Prepare the necessary specification or ADR drafts, without presenting them as approved.
6. Define focused tests and adversarial cases that cannot be passed merely by increasing
   the minimum sampling depth.
7. Set the acceptance criteria and performance counters of `CU1`.
8. Prepare a canonical prompt for the bounded implementation of `CU1`, with exact base,
   scope, exclusions, validation and stop conditions.

**Deliverables:**

- this closeout record;
- the revised `CU1` normative proposal;
- the metric aliasing analysis;
- the B10.1/H03/H04 and H10/H11/H12 correction strategy;
- the validation matrix and acceptance criteria;
- the canonical `CU1` implementation prompt;
- a candidate report with the author decisions still pending.

The agent may create a local candidate commit with the authorized documentary and
preparation changes and run the applicable static verification. Not authorized: productive
code changes, implementation of `CU1`, functional metric changes, and `CU2`–`CU10`. The next
author decision is the approval of the revised metric policy and the explicit authorization
of the `CU1` implementation on an exact base commit.

## 8. Book

`geocedg_book` stays intact and paused after BOOK-FIGPOL-PILOT. On 2026-10-20 its `HEAD` was
`78fbc88e9e12346b89d6ded411eaae94de2284b9`, equal to its `origin/main`, with a clean
worktree. No baseline update, structure, chapter, figure or editorial-planning change is
authorized. The editorial update comes after the closeout, approval and publication of the
product, under a separate authorization.

## 9. Status updates in this closeout

- Roadmap 5.60:
  - the state, last closed phase, last executed phase and next gate rows;
  - the `PRE-G9B-CU` track section.
- [Consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md): status rows,
  the decisions of 2026-10-20 (§2.1), and the `CU0`, `CU1`, `CU3` and `CU6` sections.

No historical closeout record or receipt is modified. No code, test, numerical reference,
scientific contract, registry, inventory or prompt file is changed.

## 10. Publication boundary and authorization state

This closeout commit is local. It is not pushed, tagged or merged; `main` is unchanged. The
published review branch `phase/pre-g9b-cu0-candidate` stays at `d684e740`. Its identity is
its own commit, which this record cannot name; the `CU1` preparation candidate reports it.

```text
PRE-G9B-CU (framework)  = PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS (2026-10-19)
PRE-G9B-CU0             = PASS — AUTHOR APPROVED (2026-10-20)
PRE-G9B-CU1             = PREPARATION AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-CU2 … CU10      = NOT AUTHORIZED
G9B                     = NOT AUTHORIZED
publication / tag / main = NOT AUTHORIZED
```

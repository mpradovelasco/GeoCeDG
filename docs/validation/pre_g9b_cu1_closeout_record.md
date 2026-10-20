# PRE-G9B-CU1 — author closeout record

```text
PRE-G9B-CU1                     = PASS — AUTHOR APPROVED
AUTHOR SMOKE                    = PASS (manual smoke declared by the author in the instruction of 2026-10-20)
APPROVED TECHNICAL CANDIDATE    = 143f5d3793fc645b7c79503d7717997716637be2
                                  tree 4de3cd7150fe0ec12607c1ab2a9697134f9de453
VERIFICATION RECORD DESCENDANT  = 2fe8ed198e1861a5b0e4558e8088fe20901249e4
                                  tree c9df612e0d55d310a964a397ce0108d63abaf135
SUPERSEDED CANDIDATE            = 8ee196679e0a6ab101eee0a4e60682efe3f11be0 and its record 508412e2;
                                  its two rejected INTEGRATION runs stay historical evidence
AUTHORIZED BASE                 = e66ae4c06007f9aba47436a0c03f36608f0f4fd5 (CU1 preparation candidate)
HISTORICAL BASE                 = 48a0775e4f2f122f5e2614c62b5773777752ccd1 (main = origin/main)
VERIFICATION CLASS              = INTEGRATED_PHASE: STATIC, INFRA_UNIT, PHASE PRE-G9B-CU1, PHASE PRE-G9B-CU0
                                  and INTEGRATION, ACCEPTED / COMPLETE on 143f5d37
FINAL / RECEIPT                 = NOT REQUIRED BY THE CLASS — NONE CLAIMED
D-CU1-R1 / R2 / R4              = APPROVED / ADOPT / ADOPT
D-CU1-R3                        = ACCEPT WITH RECORDED LIMITATION
D-CU1-R5                        = COMPLETED (review branch published; no promotion to main)
D-CU1-R6                        = PASS
METRIC CONTRACT v1.5 §25        = PART OF THE APPROVED CU1 CONTRACT, §25.12 INCLUDED
PRE-G9B-CU2                     = PREPARATION AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-CU3 … CU10              = NOT AUTHORIZED
PUBLICATION                     = THIS CLOSEOUT ON THE REVIEW BRANCH ONLY; main, TAGS, RELEASES AND BINARIES NOT AUTHORIZED
BOOK                            = UNCHANGED
selfApproved                    = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-CU. CU1 Final Author
Approval, Documentary Closeout and CU2 Preparation Authorization" (author decision of
2026-10-20). The author reviewed the `CU1` technical candidate, its implementation reports,
its verifications and an independent technical review of the published review branch, and
declared a satisfactory manual smoke of `CU1`. This record is the sole authority for the
author approval of `CU1`.

Under the frozen-candidate rule, the following keep the states they recorded when they were
written and are not amended:

- the [implementation candidate report](pre_g9b_cu1_implementation_candidate_report.md);
- the [verification record](pre_g9b_cu1_verification_record.md) of the superseded candidate
  `8ee19667`, with its two rejected `INTEGRATION` runs;
- the [corrective candidate report](pre_g9b_cu1_corrective_candidate_report.md) and the
  [corrective verification record](pre_g9b_cu1_corrective_verification_record.md);
- the documents of the `CU1` preparation: the
  [preparation report](pre_g9b_cu1_preparation_candidate_report.md), the
  [validation matrix](pre_g9b_cu1_validation_matrix.md), the
  [decision sheet](pre_g9b_cu1_author_decision_sheet.md) and the architecture documents.

This closeout descends linearly from the verification-record descendant `2fe8ed19`. It
changes only documentation, status lines of the `CU1` normative texts and its
machine-readable mirror,
[`pre-g9b-cu1-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu1-closeout.json).
It reruns no acceptance profile.

## 1. Author decision

| Field | Value |
|---|---|
| Instruction | "GeoCeDG — PRE-G9B-CU. CU1 Final Author Approval, Documentary Closeout and CU2 Preparation Authorization", author decision of 2026-10-20 |
| Decision | `CU1` approved as `PASS — AUTHOR APPROVED`, with the dispositions of §3 to §6 |
| Approval scope | the exact corrective technical candidate `143f5d37`; its descendant `2fe8ed19` only records the verification evidence and does not replace the identity of the verified candidate |
| Author smoke | `PASS`. The manual smoke is the author's declaration in the instruction; this record adds no smoke step, result or evidence beyond that declaration |
| Accepted evidence | the five acceptance runs on `143f5d37`, all `ACCEPTED / COMPLETE` (§2) |
| Accepted results | the performance improvements, the stability of revisions, the reuse of isolated constructions and metric indexes, and the new metric guarantee capabilities |
| Corrected within the `CU1` contract | B10.1, H03, H04, H10, H11 and H12 |
| Test correction | D-CU1-R6, the correction of the ISO A test without any productive code change, is approved |
| Historical evidence | the two rejected `INTEGRATION` runs of the superseded candidate stay as historical evidence; they are neither deleted nor reinterpreted |
| Standing diagnostics | `diagnostic.governance` and `diagnostic.historical-consistency` are kept without reinterpretation |
| Authorized next work | the architectural, normative and technical preparation of `CU2` (§7); no productive `CU2` code |
| Not authorized | promotion to `main`, tags, releases, binaries, any book change |

## 2. Approved identities and authentication (2026-10-20)

| Item | Value | Check |
|---|---|---|
| approved technical candidate | `143f5d3793fc645b7c79503d7717997716637be2`, tree `4de3cd7150fe0ec12607c1ab2a9697134f9de453` | exact; parent `508412e2f38d744bd2b333acd5754a77b7861e52` |
| verification-record descendant | `2fe8ed198e1861a5b0e4558e8088fe20901249e4`, tree `c9df612e0d55d310a964a397ce0108d63abaf135` | direct child of the candidate; delta of three documents: the corrective verification record (added), the corrective report and the roadmap. No code, test, registry, inventory or evidence path |
| superseded candidate | `8ee196679e0a6ab101eee0a4e60682efe3f11be0`, tree `f3a895843cfc5c7e0ab124b544e4ff845bcef08f`; record `508412e2`, tree `696c5eb17ddfe8b625434e504e3cdd13934c36b8` | the corrective candidate differs from it only by its record, the corrective report, the ISO A test, a manifest note, a report link and the roadmap; no product source changes |
| specification commit (S0) | `435ed78c547abf12642561c3b56a209527be535b`, tree `f7b60f4b45a1f47dd970bcdcd31f1895849caeab` | parent of `8ee19667` |
| authorized base | `e66ae4c06007f9aba47436a0c03f36608f0f4fd5`, tree `c90ab8fb61648f231cb383b900af6b4b1e622f8f` | the reviewed `CU1` preparation candidate, child of the `CU0` closeout `df675e9e` |
| historical base | `48a0775e4f2f122f5e2614c62b5773777752ccd1`, tree `75ebf03f089a5effa02daa4a68be360b7499fdd9` | equal to local `main`, `origin/main` and live remote `main` |
| ancestry | `48a0775e` → `444ad46b` → `636a0070` → `d684e740` → `df675e9e` → `e66ae4c0` → `435ed78c` → `8ee19667` → `508412e2` → `143f5d37` → `2fe8ed19` | linear, single-parent; `git merge-base --is-ancestor` confirmed |
| delta authorized base → candidate | 68 files, 7 386 insertions and 187 deletions | 41 files under `source/` outside `src/test` (4 244 insertions, 96 deletions), all registered in `docs/upstream/modified-files.yml` |
| `git diff --check` | `48a0775e..2fe8ed19` and `e66ae4c0..143f5d37` | exit 0 |
| review branch | `phase/pre-g9b-cu1-implementation` | live remote ref equal to `2fe8ed19` before this closeout |
| worktree | clean before this closeout | — |

Each result was re-authenticated on 2026-10-20 with `Read-VerificationJson` and
`Get-VerificationResultHash`. For every run the recorded and recomputed hashes were equal,
`run_state` and the run journal were `COMPLETED`, and the candidate commit and tree were the
exact identities above.

| Check | Commit | `run_id` | `result_hash` | Result |
|---|---|---|---|---|
| `STATIC` | `143f5d37` | `verification-842a984fd13745b2a401396b55e42a7d` | `446832167e558f549091b6b4fa9e79a4b23f84c985aec6cfb487fcc9ad9b9d0f` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |
| `INFRA_UNIT` | `143f5d37` | `verification-1bc73db300fe4fd0ba85054d79561554` | `f9eda832c7dae76742747cbfd651813c9c40224123b1f474f8b9aa7ee7feca0c` | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU1` | `143f5d37` | `verification-9acf4bcb65254f0389e197dff95cc178` | `f04f0a5cd9470009c75ac4af803350ce1cdfc893475e040958a5f3d7dd6251f8` | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared` 57 tests and `pre-g9b-cu1.desktop` 20 tests, 0 failures, 0 skipped |
| `PHASE -Phase PRE-G9B-CU0` | `143f5d37` | `verification-53c51a5f709b4d6393706b9721c8b022` | `e9d0ec1031b8ef52abae175c0ad0a894cd2b122be1718b69ee45ccdc7022e210` | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop` 21 tests with the B10.1, H03 and H10/H11 pins inverted, 0 failures |
| `INTEGRATION` | `143f5d37` | `verification-22c2ccd338434539baa10311565a0d47` | `aa6ebf5af42c0e87c34bb0c0558adf6634a351acf9967ceb23370fdb18abc32d` | `ACCEPTED / COMPLETE`, 19/19; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; Desktop 2179 tests (1 skipped), shared 7015 (10 skipped), 0 failures; standing diagnostics only |
| `STATIC` | `2fe8ed19` | `verification-91d6da880af942bd8c5338029b2bcfb1` | `80826286b22358c845609971302ea6377e440c5d4b93dff58b1e8c6bed1a00bc` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |

Historical evidence of the superseded candidate, authenticated in the same way and kept
unchanged, not counted as acceptance:

| Check | Commit | `run_id` | `result_hash` | Result |
|---|---|---|---|---|
| `INTEGRATION` | `8ee19667` | `verification-eb1ff68a9d5d410ca74b887d2cfebe00` | `9b6a821580b2773fd75217fb8fab3aaea1a799ad75b26bf2f017d5a4a03aa115` | `REJECTED_SEMANTIC / COMPLETE`, 18/19 |
| `INTEGRATION` | `8ee19667` | `verification-c126f174db774b5a9c7ddca1e4382689` | `7f9a12f44ca5e5ff1b3241450b241fcd2a7644e53e9e5c9d002eed0bbbe8c799` | `REJECTED_SEMANTIC / COMPLETE`, 18/19 |

The registered class `INTEGRATED_PHASE` requires the registered `PHASE` selections plus
`INTEGRATION` on the same exact commit and tree; all are present on `143f5d37`. `FINAL` is
not required for this class and is reserved for `CU10`; no receipt is expected or claimed.
`tools/agent/phase-closeout.ps1 -Action INSPECT` therefore does not apply, because it
inspects a `FINAL` receipt. The precedent is the `PRE-G9B-CU0` closeout.

No required evidence is missing, so the closeout proceeds. The evidence is kept in the
ignored directories `artifacts/agent/pre-g9b-cu1-r6-*-143f5d37/`,
`artifacts/agent/pre-g9b-cu1-r6-record-static-2fe8ed19/`,
`artifacts/agent/pre-g9b-cu1-integration-8ee19667/` and
`artifacts/agent/pre-g9b-cu1-run2-integration-8ee19667/`.

## 3. Accepted technical results

| Item | Disposition |
|---|---|
| B10.1 | corrected: a point query of `e_2` reuses the nested copy and its metric states (9 nested evaluator calls and 0 rebuilds, against 31 000–55 000 and 2 in `CU0`) |
| H03 | corrected: a pure driver move keeps the revision and the metric states of `o_1` |
| H04 | corrected: a full rebuild of `o_1` uses about 7.5 % of the metric budget, against 64 % in `CU0` |
| H10 | corrected: the lengths of `y = sin(2πkx)` on `[0, 1]` are defined and close to their independent references; no chord is reported as an arc length |
| H11 | corrected: Locus V2 circles of radius 8 and 20 have structural lengths `2πr` |
| H12 | corrected: the length of `o_1` lies 1.7·10⁻¹¹ from the independent reference, against 1.1·10⁻⁷ in `CU0`; errors are compared with the requested tolerance |
| Performance | accepted as measured by deterministic counters (decision C1-1); times stay informative |
| D-CU1-R6 | accepted: the ISO A tool scenario waits for its asynchronous undo store; no productive code changed |

The `preserve` references of the `CU0` baseline are reproduced within their tolerances, and
the baseline file is unchanged.

## 4. Decisions D-CU1-R1 to D-CU1-R6

| Id | Author disposition |
|---|---|
| D-CU1-R1 | **APPROVED.** The exact technical candidate `143f5d37` is approved, with a satisfactory author smoke |
| D-CU1-R2 | **ADOPT.** §25.12 of `locus-v2-metrics.md` v1.5 is part of the `CU1` contract. The adoption expressly preserves the distinction between structural length, certified enclosure, numerical estimate and result not established. The hypotheses of an estimator are not by themselves a mathematical certification |
| D-CU1-R3 | **ACCEPT WITH RECORDED LIMITATION.** `L-CU1-TRANSLATION-RESOLUTION` is accepted provisionally as a numerical limitation of binary64, not as a geometric property. The behaviour must keep failing explicitly when no admissible estimate can be established. The debt is registered for later treatment (§6), without arbitrary tolerances or tolerances that depend on the absolute position |
| D-CU1-R4 | **ADOPT.** The versioned budget of 16 384 boxes of the certified-enclosure capability is adopted. It is a computational limit, not a guarantee that any curve can be certified within it |
| D-CU1-R5 | **COMPLETED.** The review branch was published on GitHub at `2fe8ed19`. This does not authorize the promotion to `main` |
| D-CU1-R6 | **PASS.** The correction of the ISO A test and its new complete verification are accepted |

## 5. Normative status after this closeout

| Text | Status |
|---|---|
| `locus-v2-metrics.md` v1.5, §25 (amendment A1.2-R with precisions N1–N5) | `PASS — AUTHOR APPROVED` with `CU1`; §25.12 adopted by D-CU1-R2 |
| `locus-v2-semantics.md` v1.2 amendment (A1.1-R) | `PASS — AUTHOR APPROVED` with `CU1` |
| ADR 0037 | accepted architecture; its `CU1` implementation is `PASS — AUTHOR APPROVED` |
| `locus-v2-public-surface.md` `CU1` conformance note | unchanged |

This closeout reconciles only the status lines that said the `CU1` text was pending author
review: the headers of `locus-v2-metrics.md` and `locus-v2-semantics.md`, the status of
ADR 0037 and the opening paragraph of §25.12, which now records the adoption and the
precision of D-CU1-R2. No normative rule, numerical policy, constant or reference changes.

## 6. Limitations and debts

Registered and kept traceable without any functional change. The classes are: accepted
limitation, corrective debt, and question owned by `CU2` or a later package. Accepting a
limitation does not mean that it must stay in the product indefinitely.

| Id | Class | Owner | Disposition |
|---|---|---|---|
| `L-CU1-TRANSLATION-RESOLUTION` | accepted limitation (provisional, numerical, binary64) and corrective debt | `PRE-G9B-CU` track: a metric investigation to be planned; its disposition is due at the latest in the `CU10` limitations report | a unit curve translated by about 10⁶ fails closed in class 3 because coordinate rounding exceeds the leaf shares; class 1 is not affected. The geometric length is invariant under translation, so the investigation targets that invariance; no arbitrary or absolute-position tolerance may be introduced, and the explicit failure is kept until then |
| `L-CU1-CLASS2-REACH` | accepted limitation (computational) | `CU2` evaluates it within the extension of the certified-construction model, only where its own scope needs it; otherwise a later metric package | with `τ = 10⁻⁹·S`, class 2 is not reached for curves with appreciable total turning (about `ΘS³/τ` boxes); the enclosure is kept as rigorous evidence |
| `L-CU1-AFFINE-FORMS` | accepted limitation | `CU2` (certified-construction model) only if strictly needed for its scope; otherwise a later package | the affine certificate of the provider recognizes only direct forms; other affine expressions are measured in class 3 |
| `L-CU1-REVERSAL-DENSITY` | accepted limitation (recorded estimator hypothesis) | a later corrective package, if observed | two direction reversals between consecutive samples of the minimum partition are not located; the hypotheses say so |
| `L-CU1-E2-LENGTH` | question owned by `CU2` | `CU2` (B10.2, oriented-length structural event) | the length of `e_2` is not established in class 1 or 2; its oriented-length jump belongs to `CU2` |
| `L-CU1-IMPROPER-EVALUATOR` | accepted limitation (pre-existing) | a later metric package | improper components without a derivative keep the earlier `LIMIT_NOT_ESTABLISHED` failure |
| `OBS-CU1-UNDO-ASYNC-STORE-WINDOW` | observation of inherited upstream behaviour | a separate corrective proposal on upstream code, only if the author deems it relevant; not authorized | an Undo requested before the store of the last action lands acts on the consolidated history (window of about 80 ms without load); non-blocking |
| `OBS-CU1-UNDO-TEMP-FILES` | corrective debt (test hygiene) | test and verification infrastructure; disposition at the latest in `CU10` | scenario JVMs end with `Runtime.halt`, so `GeoGebraUndoInfo*.ggb` temporary files are not deleted; nothing is deleted or changed now; non-blocking |
| `OBS-CU1-ISOA-UNDO-STORE-RACE` | closed | — | corrected by D-CU1-R6 in `143f5d37` |

## 7. Work authorized after this closeout: `CU2` preparation

The author authorizes only the architectural, normative and technical preparation of
`PRE-G9B-CU2`: inspection, complementary characterization, architectural design, normative
proposals and implementation planning. It does not authorize productive `CU2` code. The
preparation starts from the exact approved closeout of `CU1`, whose full identity it
records.

**Scientific objective.** Resolve the existence, continuity, certification, topology and
representation problems of Locus V2 characterized in `CU0`: B07 (gaps and disappearing
components), B10.2 (false closure of `e_2`), H01 (publication depending on the current driver
position) and B06 (insufficient tessellation and visual ripple). The preparation uses the
existing proposals — the [hybrid certification proposal](../architecture/pre_g9b_cu0_hybrid_certification_proposal.md),
the [oriented-length analysis](../architecture/pre_g9b_cu0_oriented_length_analysis.md) and
the [candidate normative amendments](../architecture/pre_g9b_cu0_candidate_normative_amendments.md),
especially A2.1–A2.4 — and does not repeat completed characterization unnecessarily.

**Binding requirements of the instruction:**

1. Hybrid certification DA-1: a precise normative proposal that separates point evaluation,
   exploratory evidence, locally certified components and global completeness, preserving
   ADR 0027 and the independence between local admissibility and global completeness.
   Certification never derives only from sampling; exploratory evidence never acquires
   semantic authority because it looks convincing.
2. The certification of the author's model is explained constructively and mathematically:
   `o`, `v`, `w` and `a_1` (especially the transition from penetration to bite), `o_1` and
   `e_1` in their existence domains, and `e_2`, including its dependence on the oriented
   length. The chain `o_1 → LocusLength → d_2 → e_2` is preserved. A `CU1` length of class
   `ESTIMATED` never becomes rigorous evidence for certifying a `CU2` component
   automatically; any additional structural or interval capability is defined explicitly.
3. The discontinuity of `e_2` is specified as a structural event of the oriented length,
   distinguishing the discontinuity of the length traversal, a real discontinuity of the
   construction, the cut of a periodic parametrization, continuity between component ends
   and artificial joins produced by rendering. The semantics of `LocusLength` do not change
   silently, and `e_2` is not closed merely because its driver is periodic.
4. Consumption rules of the new existence structure for rendering, metrics, intersections,
   semantic points and membership, composition of dependent loci, and DXF and LaTeX
   exports. The shared kernel keeps the geometric authority; presentation and export
   approximations never change the semantic topology. Durable identity, component lineage,
   revision, currentness and DAG propagation are preserved.
5. `CU2` prepares the existence and continuity conditions that `CU3` needs, without
   implementing new structural points, tangent-root materialization, intersection-engine
   changes reserved to `CU3` or new public intersection tools.

**Deliverables:** complementary characterization only where needed; consolidated design of
the hybrid certification; normative proposal for existence, continuity and components;
proposed extension of the certified-construction model; design of the oriented-length
structural event; certificate consumption rules; Locus V2 representation and tessellation
strategy; test matrix and acceptance criteria, including the author's models, degenerations
and topological changes; minimal implementation strategy with dependencies and stop
conditions; candidate canonical prompt for executing `CU2`; sheet of pending author
decisions. The normative proposals stay candidates until specifically authorized. The
preparation may be a local documentary commit on its own branch, with `STATIC` and the
pertinent checks. Neither `CU2` implementation nor a `PASS — AUTHOR APPROVED` declaration of
the preparation is authorized without a later author decision.

## 8. Book

`geocedg_book` stays intact. On 2026-10-20 its `HEAD` was
`78fbc88e9e12346b89d6ded411eaae94de2284b9`, equal to its `origin/main`, with a clean
worktree. No change is authorized.

## 9. Status updates in this closeout

- Roadmap 5.63: the version and date, the state, last closed phase, last executed phase and
  next gate rows, and the `PRE-G9B-CU` track section.
- [Consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md): status rows,
  the decisions of this instruction (§2.2), and the `CU1` and `CU2` sections.
- Status lines of the `CU1` normative texts (§5): `locus-v2-metrics.md` header and §25.12,
  `locus-v2-semantics.md` header, ADR 0037 status.

No historical closeout record, verification record or receipt is modified. No code, test,
numerical policy, geometric reference, registry, inventory or prompt file is changed.

## 10. Publication boundary and authorization state

The author authorizes publishing this documentary closeout only on the review branch
`phase/pre-g9b-cu1-implementation`, with a normal push, after its verification and
ancestry are checked. The record cannot name its own commit; the `CU2` preparation records
it. The consolidated publication of the product is reserved for the closeout decision of
`CU10`, unless a later author authorization changes that plan.

```text
PRE-G9B-CU (framework)   = PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS (2026-10-19)
PRE-G9B-CU0              = PASS — AUTHOR APPROVED (2026-10-20)
PRE-G9B-CU1              = PASS — AUTHOR APPROVED (2026-10-20); AUTHOR SMOKE = PASS
PRE-G9B-CU2              = PREPARATION AUTHORIZED — IMPLEMENTATION NOT AUTHORIZED
PRE-G9B-CU3 … CU10       = NOT AUTHORIZED
G9B                      = NOT AUTHORIZED
review branch            = AUTHORIZED FOR THIS CLOSEOUT (normal push)
main / tag / release     = NOT AUTHORIZED
```

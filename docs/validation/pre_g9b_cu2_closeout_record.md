# PRE-G9B-CU2 — author closeout record

```text
PRE-G9B-CU2                     = PASS — AUTHOR APPROVED
AUTHOR MANUAL CHECK (SMOKE)     = PASS (declared by the author in the instruction of 2026-10-24)
APPROVED TECHNICAL CANDIDATE    = 6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb
                                  tree 3dfcc575c45ec9ec660199d86d166d89e32851c9
VERIFICATION RECORD DESCENDANT  = 098ec04f7ac72b691a989796637b3ff681e6e3de
                                  tree a1012756d8e1ffa9bd5b74475c5fe10dc60a94f5
SUPERSEDED CANDIDATES           = 39741a0b (T13, record 5d286850) and 2856f246 (corrective, record
                                  c8d4fd39); their runs stay historical evidence
AUTHORIZED BASE                 = 070af6f902cd0be291247e73efb5629ef2a39eba (CU2 preparation)
HISTORICAL BASE                 = 48a0775e4f2f122f5e2614c62b5773777752ccd1 (main = origin/main)
VERIFICATION CLASS              = INTEGRATED_PHASE: STATIC, INFRA_UNIT, PHASE PRE-G9B-CU2, PRE-G9B-CU1
                                  and PRE-G9B-CU0, INTEGRATION, ACCEPTED / COMPLETE on 6ff1ddf7
FINAL / RECEIPT                 = NOT REQUIRED BY THE CLASS — NONE CLAIMED
C-1 … C-12                      = APPROVED (2026-10-23) AND APPLIED
LIMITATIONS (FINAL PACKAGE §8)  = ACCEPTED; L-CU2-INDEPENDENT-EXPRESSION-STATE ONLY OUTSIDE CLASS v3
FUTURE WORK                     = RECORDED — NOT AUTHORIZED — NOT CU3 TASKS
PRE-G9B-CU3 … CU10              = NOT AUTHORIZED
INTEGRATION INTO main           = AUTHORIZED FOR THIS CLOSEOUT (exact-SHA, non-force fast-forward)
TAGS / RELEASES / PACKAGES      = NONE
BOOK                            = UNCHANGED
selfApproved                    = false
```

This record transcribes the author instruction "PRE-G9B-CU2 — Aprobación autoral, cierre e
integración" (author decision of 2026-10-24). After an independent audit of the published review
branch `phase/pre-g9b-cu2-corrective` and a review of the
[final review package](pre_g9b_cu2_final_review_package.md), the author approved the technical
and scientific closeout of `CU2`. The approval was conditioned on the author's satisfactory manual
check; the author confirmed it expressly in the same message ("Confirmo el éxito del smoke de
autor"). This record is the sole authority for the author approval of `CU2`. Automated results
are evidence; they are not the author's approval.

Under the frozen-candidate rule, the following keep the states they recorded when they were
written and are not amended: the [preparation report](pre_g9b_cu2_preparation_candidate_report.md),
the [architecture approval record](pre_g9b_cu2_author_architecture_approval_record.md), the
[T13 candidate report](pre_g9b_cu2_implementation_candidate_report.md) and its
[verification record](pre_g9b_cu2_verification_record.md), the
[traceability review](pre_g9b_cu2_traceability_review.md), the records of
[R6](pre_g9b_cu2_r6_correction_record.md) and [R7](pre_g9b_cu2_r7_correction_record.md), the
[R5 gap inventory](pre_g9b_cu2_r5_gap_inventory.md), the
[corrective autonomy record](pre_g9b_cu2_corrective_autonomy_record.md), the
[validation matrix](pre_g9b_cu2_validation_matrix.md) and the
[final review package](pre_g9b_cu2_final_review_package.md).

This closeout descends linearly from the verification-record descendant `098ec04f`. It changes
only documentation, status lines of the `CU2` normative texts and of ADR 0038, the E1 register
and its machine-readable mirror,
[`pre-g9b-cu2-closeout.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu2-closeout.json). It
reruns no acceptance profile and changes no product, test, inventory or registry file.

## 1. Author decision

| Field | Value |
|---|---|
| Instruction | "PRE-G9B-CU2 — Aprobación autoral, cierre e integración", author decision of 2026-10-24 |
| Decision | `CU2` approved as `PASS — AUTHOR APPROVED` (technical and scientific closeout) |
| Condition | the author's satisfactory manual check, confirmed expressly by the author in the same message before this decision was applied |
| Approval scope | the exact technical candidate `6ff1ddf7`; its descendant `098ec04f` only records the verification evidence and does not replace the identity of the verified candidate |
| Accepted evidence | the six acceptance runs on `6ff1ddf7`, all `ACCEPTED / COMPLETE` on their first run (§2); the benches and V-DXF-4 of the [final review package](pre_g9b_cu2_final_review_package.md) §6 |
| Independent audit | of the branch published at `098ec04f` on 2026-10-24, run by the author with external assistance; it is the author's input, not repository evidence |
| Limitations | the limitations of §8 of the final review package are accepted (§6) |
| Future work | the proposed improvements stay future work, not authorized and not converted into `CU3` tasks (§6) |
| Scientific transfer | the `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` dossier is kept up to date, with the transversal principle of §7 |
| Integration | after this closeout is verified: integration into `main` by the authorized procedure, without rewriting history or modifying the verified product, and publication of the closeout (§10) |
| Not authorized | `CU3` or any later phase, releases, redistribution packages, any change to `geocedg_book` |

## 2. Approved identities and authentication (2026-10-24)

| Item | Value | Check |
|---|---|---|
| approved technical candidate | `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb`, tree `3dfcc575c45ec9ec660199d86d166d89e32851c9` | exact; parent `e3d01b9bd31bdbbd0015a1025387afa031c10af7` |
| verification-record descendant | `098ec04f7ac72b691a989796637b3ff681e6e3de`, tree `a1012756d8e1ffa9bd5b74475c5fe10dc60a94f5` | direct child of the candidate; delta of three documents (final review package, consolidated plan, roadmap); no code, test, registry, inventory or evidence path |
| corrective candidate (superseded) | `2856f246c61bd51916932262002cdff31e61b565`, tree `95d9f408e65c3a3cda7181043521a78e30b292ec`; record `c8d4fd39ce62016e9ed18a4904ad83237a7a941d`, tree `d689e7ad82ad4ea4452d817594e7a007979d635d` | ancestor of the approved candidate |
| T13 candidate (superseded) | `39741a0b44850a32258c2c73370fdc0842b8c666`, tree `0bfeb76da3ad3a470b9de61eec0650ea0895b3aa`; record `5d2868500841d478e77465894b87e794ee6404c9`, tree `9cb5a8a8e15cde91966a83ac56e347c25f02ede5` | ancestor of the approved candidate |
| authorized base | `070af6f902cd0be291247e73efb5629ef2a39eba`, tree `49a659d8b334aae3f69c4db000cc842204e9a85f` | the published `CU2` preparation; descends from the `CU1` closeout `27bf652f` |
| historical base | `48a0775e4f2f122f5e2614c62b5773777752ccd1`, tree `75ebf03f089a5effa02daa4a68be360b7499fdd9` | equal to local `main`, `origin/main` and live remote `main` before this closeout |
| ancestry | `48a0775e` → `CU0` and `CU1` closeouts → `070af6f9` → 53 commits → `6ff1ddf7` → `098ec04f` | linear, no merge commit (68 commits from `48a0775e`); `git merge-base --is-ancestor` confirmed |
| delta authorized base → candidate | 258 files, 52 445 insertions and 1 854 deletions | 113 product files under `source/**/src/main` (21 442 insertions, 1 087 deletions), all registered in `docs/upstream/modified-files.yml` (1 162 entries; `Assert-GeoCeDGUpstreamBoundary` correct); 99 test files |
| `git diff --check` | `070af6f9..6ff1ddf7` and `48a0775e..098ec04f` | exit 0 with the repository's canonical policy (`blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol`); Git's default policy also reports the CR of CRLF lines added to the known mixed-EOL files `menu*.properties` and `LocusSimilarityEvaluator2D.java` |
| review branch | `phase/pre-g9b-cu2-corrective` | live remote ref equal to `098ec04f` before this closeout (published on 2026-10-24, normal push, no tags) |
| worktree | clean before this closeout | — |

Each result was re-authenticated on 2026-10-24 with `Read-VerificationJson` and
`Get-VerificationResultHash`. For every run the recorded and recomputed hashes were equal,
`run_state` and the run journal were `COMPLETED`, and the candidate commit and tree were the exact
identities above.

| Check | Commit | `run_id` | `result_hash` | Result |
|---|---|---|---|---|
| `STATIC` | `6ff1ddf7` | `verification-b60eca30025f4379b08eafffe518f309` | `577cf92058b7cfd6d480ee0b9e393620e85c532d89f521575b62eb07b6976b86` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |
| `INFRA_UNIT` | `6ff1ddf7` | `verification-3d7571b1f9e440d4adc69246bdc05bdc` | `9c10cd42da3ce56ed138f43907a80051319de132ff47f06901dd338f1f21e2e7` | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU2` | `6ff1ddf7` | `verification-6d0dc9163e5642889799b50f8927ad11` | `0c5be979d6d750fd1b340500ce56b9c9b962e6a5f39725878988b11293e5ad41` | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu2.shared` 242 tests, `pre-g9b-cu2.desktop` 72 tests, 0 failures, 0 skipped |
| `PHASE -Phase PRE-G9B-CU1` | `6ff1ddf7` | `verification-e6d45e0ae7b24affbfb14d5477ca9a87` | `32acd12d5b54c11be92a7d435e36cf1cdffa3dba1a993d0422524b8b7944e063` | `ACCEPTED / COMPLETE`, 4/4; 57 and 20 tests, 0 failures |
| `PHASE -Phase PRE-G9B-CU0` | `6ff1ddf7` | `verification-e7b66154af944b39aacdac9bda39cbd4` | `ec7d02aed2da1d9b365daa75693493bdfc9536d5b6d12189bd3e7b0caad315af` | `ACCEPTED / COMPLETE`, 2/2; 21 tests, 0 failures |
| `INTEGRATION` | `6ff1ddf7` | `verification-a4b0007148a1467ba119058870f024cb` | `57989175d345dc4a27f87c7306f279ac3e036bb221e1fc0237a4c6137233a428` | `ACCEPTED / COMPLETE`, 19/19; plan `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea`; Desktop 2 251 tests (1 skipped), shared 7 257 (10 skipped), 0 failures; standing diagnostics only |
| `STATIC` | `098ec04f` | `verification-b3f2be9025ea4893a4723fec5c5ecd50` | `7ec2f7aa39d7753b5fc665f8eb67b1115b8b7b86458bd8b3fff4bfde1782ca1c` | `ACCEPTED / COMPLETE`, 3/3; standing diagnostics only |

The plans of the six runs are those of T13 and of `2856f246`. The standing diagnostics are
`diagnostic.governance` = `DIAGNOSTIC_FINDING` and `diagnostic.historical-consistency` =
`DIAGNOSTIC_UNAVAILABLE`, kept without reinterpretation.

Historical evidence of the superseded candidates, authenticated in the same way and kept
unchanged, not counted as acceptance of the approved candidate:

| Check | Commit | `run_id` | `result_hash` | Result |
|---|---|---|---|---|
| `STATIC` | `39741a0b` | `verification-5aec340a96ea459a94852ef1319fbe7b` | `762b122ab0228fcee3b330fe2fe70ca744d7421d3fe7b6fa8e435db0665b9a83` | `ACCEPTED / COMPLETE` |
| `INFRA_UNIT` | `39741a0b` | `verification-4a3db31225c944788661fb24ba18792a` | `2e150ac2a9eb52abfe2133fb390fb97aedb4f63c23ec2fd61aea2684d8e1e78f` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU2` | `39741a0b` | `verification-d2957d6ec5f14599adc817e8c65666a9` | `7ad1e9ccb0c943d779662004e510408edc4d543b18a551b180984d39b7b8a6db` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU1` | `39741a0b` | `verification-898e709c055948cf9847b858556057d5` | `b37a5b9679de17992a437afa0ad2a8f5c464f4b704630483f62484f4a3ed3755` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU0` | `39741a0b` | `verification-b9f68b8246814024890195be3a9d4670` | `964c55c3f571e7d2dcb85a34f4632bfd3d07ddb3fd222c6d44026d09605620f6` | `ACCEPTED / COMPLETE` |
| `INTEGRATION` | `39741a0b` | `verification-7660aed90fdd45719122441e580150fb` | `223e7f8ca73c5729ae1e5f95efba37099912d7875e60889f72873dcade48ebba` | `REJECTED_SEMANTIC / COMPLETE`, 18/19 (pre-existing off-EDT toolbar race, `OBS-CU2-OFF-EDT-TOOLBAR-RACE`) |
| `INTEGRATION` | `39741a0b` | `verification-a22f468137434ef283dcfaee88f102f0` | `9eb4b9fd30c4f20df134d060412d851852f1c4e4770ab780aa4ba28f9ceb9f87` | `ACCEPTED / COMPLETE`, 19/19 |
| `STATIC` | `2856f246` | `verification-4750c53b651f48d085264099ea63a1d4` | `5576fdb3b27f72ad7b5f29c27727302d4d895c1ed97492877a27e1c86831dd07` | `ACCEPTED / COMPLETE` |
| `INFRA_UNIT` | `2856f246` | `verification-c666c886bea04619a5d9e4a4cacfef3b` | `46a39cf3d018a232783a2aae7f113bd814dedd84734195195b5409ad0b81039d` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU2` | `2856f246` | `verification-012a2eff0a0f4240aef201f6c94af738` | `47ab34e8ace6da40f54f2a0ee61023fb6d734803e49a5771b7ba48cfd4d08cf0` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU1` | `2856f246` | `verification-0cc38b330cd542c1bf506f7fece317e4` | `f61b9b2d3d55edba8b5bab439ddbb2b43bb4acc9898c7d5ff4fb6aa0ecb4a262` | `ACCEPTED / COMPLETE` |
| `PHASE -Phase PRE-G9B-CU0` | `2856f246` | `verification-a43bf15d7dc2466cbce771687051e209` | `efdcf68cba58173d424e9ed81f0552520327b6a9cd8cec458ec25aaa59d51cef` | `ACCEPTED / COMPLETE` |
| `INTEGRATION` | `2856f246` | `verification-d0679cf451004c2fb43c42be229b9fb3` | `41409661a1b16687f6a3deddc4622734c3102df08a5c2c61a58f7d42a96b22ec` | `ACCEPTED / COMPLETE`, 19/19 |

The registered class `INTEGRATED_PHASE` (decision D-CU2-12) requires the `PHASE` selections of
`CU2`, `CU1` and `CU0` plus `INTEGRATION` on the same exact commit and tree; all are present on
`6ff1ddf7`. `FINAL` is not required for this class and is reserved for `CU10`; no receipt is
expected or claimed, so `tools/agent/phase-closeout.ps1 -Action INSPECT`, which inspects a
`FINAL` receipt, does not apply. The precedents are the `CU0` and `CU1` closeouts. The evidence is
kept in the ignored directories `artifacts/agent/pre-g9b-cu2f-*-6ff1ddf7/`,
`artifacts/agent/pre-g9b-cu2f-docs-static-098ec04f/`, `artifacts/agent/pre-g9b-cu2a-*-2856f246/`
and `artifacts/agent/pre-g9b-cu2-*-39741a0b/`, and the benches in
`artifacts/benchmarks/pre-g9b-cu/cu2-after-*-6ff1ddf7/`.

## 3. Accepted technical and scientific results

| Item | Disposition |
|---|---|
| B07, H01, B10.2, B06 | corrected; the `CU0` pins of these defects are inverted (D-CU2-14, step T12) and pass in `PHASE -Phase PRE-G9B-CU0` |
| Hybrid existence certification | class v3 programs, exact algebraic certificate on captured binary64 constants (D-CU2-16), certified transitions, point components, folds by exact symmetry, structural totality, published certificates with a single closure authority (ADR 0038) |
| `e_2` | one open arc of full length from `t_N = 0`, `COMPLETE`, cut by the certified structural event `ORIGIN_WRAP` of the oriented length; the chain `o_1 → LocusLength → d_2 → e_2` stays valid |
| Selection and fidelity | read-only selection accessors (D-CU2-17), selection settlement, exact fingerprint v2 and reconstruction fidelity (D-CU2-20-R) |
| Corrections after T13 | H-T13-A (R6), H-T13-B (R7, rigorous band), H-T13-C classified as a computational failure (R7), H-CU2A-1 to H-CU2A-5 (corrective mandate), H-CU2A-6 to H-CU2A-11 and R7-A to R7-D (focal scientific review); each H correction has an adversarial test that fails without it |
| Conformity | 134 matrix rows; no mandatory row with an unasserted element; rows not verified by tests follow their verification route or an explicit author decision ([final review package](pre_g9b_cu2_final_review_package.md) §4) |
| References and benches | `CU0` `preserve` references and `CU1` selections accepted; certification, tessellation and references benches equal leaf by leaf to those of `2856f246`; V-DXF-4 with AutoCAD Core Console without load or `AUDIT` error |

## 4. Decisions applied in `CU2`

| Id | Disposition and authority |
|---|---|
| D-CU2-1 to D-CU2-19, D-CU2-20-R, E-1 to E-3 | architecture and conditional implementation authorization of 2026-10-21 ([approval record](pre_g9b_cu2_author_architecture_approval_record.md)) |
| D-CU2-T11-R1, H10 (option A), V-CON-6 (option 2) | author resolutions of 2026-10-21 and 2026-10-22, applied in T11 and T12 |
| D-CU2-T13-R1, R2, R4, R6, R7 | corrective descendant decisions of 2026-10-22 and 2026-10-23 (records of R6 and R7) |
| D-CU2-T13-R3 | disposed by the T13-R1/R2 authorization: both T13 `INTEGRATION` runs kept |
| D-CU2-T13-R5 | replaced by the mandate of controlled corrective autonomy of 2026-10-23 |
| C-1 to C-12 | consolidated author resolution of 2026-10-23, applied as recorded in the [final review package](pre_g9b_cu2_final_review_package.md) §2 |

## 5. Normative status after this closeout

| Text | Status |
|---|---|
| `locus-v2-existence-components.md` v1.1, §15 (A2.1-R, with H10, V-CON-6 and D-CU2-T13-R1) | `PASS — AUTHOR APPROVED` with `CU2` |
| `locus-v2-certified-construction-model.md` v1.2, §10 (A2.2-R, with §10.10 and the precision C-8) | `PASS — AUTHOR APPROVED` with `CU2` |
| `locus-v2-metrics.md` v1.6, §26 (A2.3-R and the metric part of A2.5, with §26.5) | `PASS — AUTHOR APPROVED` with `CU2` |
| `locus-v2-semantics.md` v1.3 (publication, exact fingerprint, reconstruction fidelity, `DIMENSION_CHANGED`) | `PASS — AUTHOR APPROVED` with `CU2` |
| `locus-v2-presentation.md` version 2 (A2.4) | `PASS — AUTHOR APPROVED` with `CU2` |
| `dxf-curve-fidelity-and-approximation.md` 1.2, §10 | `PASS — AUTHOR APPROVED` with `CU2` |
| `latex-semantic-curve-export.md` 1.0, §5 (A2.6) | `PASS — AUTHOR APPROVED` with `CU2` |
| scope notes A2.5 (`locus-v2-intersections.md`, `locus-v2-extended-intersections.md`, `locus-v2-point-interaction.md` §17, `locus-v2-similarity-transformations.md`) and A2.1-R (`locus-v2-public-surface.md`) | `PASS — AUTHOR APPROVED` with `CU2` |
| ADR 0038 | accepted architecture; its `CU2` implementation `PASS — AUTHOR APPROVED` |

This closeout reconciles only the status lines and the sentences that said that the `CU2` text
belonged to the technical candidate pending author review. No normative rule, numerical policy,
constant, reference, persisted format or durable identity changes.

## 6. Limitations and future work

The limitations of §8 of the [final review package](pre_g9b_cu2_final_review_package.md) are
accepted with the disposition recorded there. None certifies anything that is not proved or
withdraws an approved guarantee: they fail closed or are evidence without authority. Accepting a
limitation does not mean that it must stay in the product indefinitely.

| Id | Class | Disposition |
|---|---|---|
| `L-CU2-SQRT-END-LENGTH` | accepted computational limitation (C-10) | the total length of an arc with square-root ends is `NOT_ESTABLISHED` with `WORK_BUDGET_EXHAUSTED`; the `CU1` metric contract is unchanged |
| `L-CU2-PERIODIC-CANONICALIZATION` | accepted limitation (C-11), with B-2 | persisted addresses unchanged; included ends are evaluated at their own parameter |
| `L-CU2-CLOSED-PERIODIC-SEAM` | accepted limitation (C-12) | inherited membership near the seam kept; `CU0` and `CU1` references unchanged |
| `L-CU2-INDEPENDENT-EXPRESSION-STATE` | accepted limitation, only in the scope described: independent elements defined by an expression, outside class v3 (no certificate) | a one-ulp programmatic change of a literal without original text is not distinguished by the fingerprint or the fidelity; user-written literals keep their text |
| F-CU2-1 and the residual risk of H-CU2A-8 | documented inherited behaviour and numerical availability limitation | never published as existence; where existence is certified, queries fail closed |
| Residual limitations of R7, with R7-E | accepted with R7 | see the distinction below |
| `OBS-CU2-OFF-EDT-TOOLBAR-RACE` | pre-existing Desktop test race | did not occur in the `INTEGRATION` runs of `2856f246` and `6ff1ddf7` |
| Earlier limitations of T13, `CU1` questions and `CU3` items | as recorded in the final review package §8 | unchanged |

**Proved bounds versus observed coverage (R7-E).** The band of the inherited line–circle
computation is bounded a priori: its predicate and rounding bounds are mathematical results and do
not depend on sampling. The band closure, by contrast, joins cells by their cause; two distinct
bands of that cause closer than `0,0245` rad would be joined. That is a loss of local certified
coverage, conservative but larger than the `10⁻¹⁰` to `10⁻⁹` rad observed experimentally in the
R7 record. The observed widths are experimental evidence, not a bound, and they are not presented
as one.

**Future work, not authorized.** The change-of-variable estimator for square-root ends (C-10),
the improvements of metrics, canonicalization and compositions, the selection identity of
composite certificates (O-CU2A-7), the deduplication of turn marks near the seam (O-CU2A-8), two
coincident metric events in one slice (O-CU2A-9), the exact state of expression literals in a
later fingerprint version, and the other residual observations stay recorded as future work.
They are not authorized and are not `CU3` tasks; any of them needs a specific author decision.

## 7. Scientific transfer to `CU10`

The [E1 register](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md) moves to
version 5: the methods implemented by `CU2` are recorded as approved, and §3 adds the transversal
principle 8 for the future book: the distinction between geometric integrity and numerical
precision. Existence, constructive identity, topology, dependencies and mathematical evidence are
preserved and are not subordinated to numerical availability or to the fidelity of an approximate
representation. The principle is related to SM-A-5 and SM-E-6 and to the examples of `CU2`,
especially V-POINT-4 (a forced failure of the limit form leaves the point component with its
certified existence and an unavailable position). The `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF`
dossier of `CU10` (§7 of the register) now lists the transversal principles. No editorial text is
written.

## 8. Book

`geocedg_book` stays intact. On 2026-10-24 its `HEAD` was
`78fbc88e9e12346b89d6ded411eaae94de2284b9`, equal to its `origin/main`, with a clean worktree. No
change is authorized.

## 9. Status updates in this closeout

- Status lines of the `CU2` normative texts and of ADR 0038 (§5).
- E1 register version 5 (§7).
- [Consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md): status rows, the
  decisions of this instruction (§2.6), the `CU2` section and §9.
- Roadmap: the state, last closed phase, last executed phase and next gate rows, and the
  `PRE-G9B-CU` track section.
- This record and its mirror.

No historical closeout record, verification record, review package or receipt is modified. No
code, test, numerical policy, geometric reference, registry, inventory or prompt file is changed.

## 10. Integration, publication and phase boundary

The author authorizes, after this closeout is verified: fast-forwarding the review branch
`phase/pre-g9b-cu2-corrective` to this closeout, and integrating it into `main` by an exact-SHA,
non-force fast-forward from `48a0775e`, without rewriting history and without modifying the
verified product. This integration also brings the approved `CU0` and `CU1` closeouts, which are
ancestors of the `CU2` branch, into `main`; it replaces, for this point of the track, the plan of
the `CU1` closeout to reserve the consolidated publication to `CU10`. The record cannot name its
own commit; the publication report and the next phase record it.

```text
PRE-G9B-CU (framework)   = PLANNING FRAMEWORK — AUTHOR APPROVED WITH AMENDMENTS (2026-10-19)
PRE-G9B-CU0              = PASS — AUTHOR APPROVED (2026-10-20)
PRE-G9B-CU1              = PASS — AUTHOR APPROVED (2026-10-20)
PRE-G9B-CU2              = PASS — AUTHOR APPROVED (2026-10-24); AUTHOR MANUAL CHECK = PASS
PRE-G9B-CU3 … CU10       = NOT AUTHORIZED (CU3 needs its own author decision)
G9B                      = NOT AUTHORIZED
review branch / main     = AUTHORIZED FOR THIS CLOSEOUT (non-force fast-forward)
tags / releases / packages = NOT CREATED
book                     = UNCHANGED
```

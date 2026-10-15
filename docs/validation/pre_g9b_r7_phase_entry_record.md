# PRE-G9B-R7 — phase-entry record and reconciled execution contract

```text
PRE-G9B-R7                 = EXECUTION AUTHORIZED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
PRE-G9B-R6-plus            = PASS — AUTHOR APPROVED — PUBLISHED
CHANGE_ROUTE               = ORDINARY
VERIFICATION_CLASS         = GLOBAL_IMPACT        (frozen by the author at phase start)
PRIMARY_ACCEPTANCE_PROFILE = FINAL
CLOSEOUT_MODE              = AUTHOR_OPERATED
IMPLEMENTATION_BASE        = b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513
IMPLEMENTATION_TREE        = 3aef929b0b5939854d9ccd736a919b496172a30e
BRANCH                     = phase/pre-g9b-r7-readiness-closeout (local; not pushed)
G11 / G12 / G15            = PENDING — ROADMAP SCOPE REVIEW
ENH-B-MANUAL-EXPORT-AREA-DRAG = OPEN — OWNER G15
G9B / G9C / G9U2 / productive G10 = NOT AUTHORIZED
PUBLICATION / PUBLIC RELEASE = NOT AUTHORIZED
selfApproved               = false
```

This record transcribes the author instruction "GeoCeDG — PRE-G9B-R7 Integrated PRE-G9B
Readiness, Technical-Debt Reconciliation, Roadmap Revision and Final Verification"
(2026-10-15) and the author's addendum of the same message on distribution-status and
version references (§6). It records the verified entry state, the frozen execution inputs
and the predecessor matrix. It authorizes nothing beyond the instruction and approves
nothing. Machine-readable mirror:
[`pre-g9b-r7-phase-entry.json`](../../geocedg/validation/pre-g9b-r7/pre-g9b-r7-phase-entry.json),
which also holds every verified commit and tree of §3.

## 1. Authorization and boundaries

| Field | Value |
|---|---|
| Authorized | execution and technical verification of `PRE-G9B-R7` as one integrated task: prompt reconciliation, phase-history authentication, debt reconciliation, G11/G12/G15 review, readiness assessment, `FINAL` on an immutable candidate, author-review package |
| Not authorized | approval of the R7 candidate; product, kernel, geometry, serialization, Desktop, build, packaging or global verification-infrastructure change; implementation of G11, G12, G15, `ENH-B-MANUAL-EXPORT-AREA-DRAG`, G9B, G9C, G9U2 or productive G10; commercial-profile activation; contact with GeoGebra or OpenGeoProver; push, merge, tag, release or publication |
| Author smoke | required; executed and reported by the author separately |
| Next | author review of the exact candidate, the smoke, the debt and roadmap decisions and a separate closeout and publication authorization |

## 2. Entry verification (2026-10-15)

| Check | Result |
|---|---|
| `HEAD` and tree | `b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513`, tree `3aef929b0b5939854d9ccd736a919b496172a30e` — exact |
| local `main`, `origin/main`, live remote `main` (`git ls-remote`) | all `b9bdb1e119fcc6d8f5bcdf3e1dea43c1a9a64513` |
| worktree and index | clean |
| ancestry | `48b182b8` (R0 closeout) `..` base: 213 commits, 0 merges, linear |
| G-R1 technical candidate T' `da1a0952` | ancestor of the base; the base is the documentary G closeout (only documentation and mirrors changed since T') |
| R7 branch | `phase/pre-g9b-r7-readiness-closeout`, created at the exact base |
| `PRE-G9B-R0` author decisions | `AD-R0-1` to `AD-R0-9` all decided on 2026-09-17; none unresolved |
| `FINAL` plan at the base | `3d8752b91eb96bffa3cfae1d6f54378ccfa34451642d0172f1cdb73f01743517`, 68 nodes, coverage `COMPLETE` (`-PlanOnly`) |

Read before any change: `AGENTS.md`, `CLAUDE.md`, the canonical governance and verification
prompts, the roadmap, the staged design, the R0 matrix, the R6 closeout, the R6-plus G
closeout and structured register, the mini-track plan, the G11/G12/G15 reconciliation, the
live part of `verification-levels.md`, `documentation-maintenance.md`, `verify.ps1` and the
registry.

## 3. Verified predecessor matrix

Every identity below was verified with `git cat-file -t`, `git rev-parse <commit>^{tree}` and
`git merge-base --is-ancestor <commit> b9bdb1e1`: 104 identities, 102 approved ones are
ancestors of the base; the two non-ancestors are so by design (the superseded G candidate T
`dd3937cf` and the historical-suite source candidate `2f1b091e`, whose replay `20351316` is
on `main`). Statuses are the verbatim statuses of the closeout records; approval is never
inferred from ancestry. Full SHAs and trees are in the mirror.

| Phase | Approved candidate | Closeout | Recorded status | Class; accepted evidence |
|---|---|---|---|---|
| R0 | `4161df2d` | `48b182b8` (evidence commit; no separate closeout record) | `PASS — AUTHOR APPROVED` (with required planning change) | `DOCUMENTATION_STATUS_ONLY`; `STATIC` `verification-8098bf52…` |
| R1 | `36c8d754` | `8cc42981` | `AUTHOR APPROVED FOR PROMOTION — RETAINED PRE-EXISTING BASELINE DEFECT`; `passClaimed = false` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`; `FINAL` `verification-8763e250…` **`REJECTED_SEMANTIC`**, coverage `COMPLETE` 40/40, **no receipt**; `STATIC`, `INFRA_UNIT` accepted (see §3.1) |
| R1-R1 | `183fe18d` | `51a6b096` | `PASS — AUTHOR APPROVED` | `BOUNDED_PHASE`; `PHASE G9U1` `verification-0908f631…` 13/13 |
| R2 | `f451f603` | `a222b82a` | `PASS — AUTHOR APPROVED` | `INTEGRATED_PHASE`; `PHASE` ×2 + `INTEGRATION` `verification-df533754…` |
| R2-E0/E1 | `60cb1a2a` (corrective descendant of `3d13ea72`) | `ef120a54` | `PASS — AUTHOR APPROVED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` `verification-0370524a…`; the `Ri` debt policy |
| R3 | `89fab9d1` (on `PRE-G9B-R3-R1`) | `89d481b1` | `PASS — AUTHOR APPROVED` | `GLOBAL_IMPACT` (escalation approved); `FINAL` `verification-f363f882…` 40/40, receipt `c4d9a8bf…` |
| R3-C1 | `aeb40e16` | `68169dc0` | `PASS — AUTHOR APPROVED` | `GLOBAL_IMPACT`; `FINAL` `verification-1c5cc16a…`, receipt `b078e1b8…` |
| R3-C1-U1 | `c595adc5` | `78134aa8` | `PASS — AUTHOR APPROVED` | `GLOBAL_IMPACT`; `FINAL` `verification-bff47880…`, receipt `e48ca697…` |
| R3-X1 + GOV-R | T `8ab7dddc`, GOV-R adoption `8e4218db`, reconciliation R `a624e4ad` | `f96373ac` | `PASS — AUTHOR APPROVED` (both) | `GLOBAL_IMPACT`; source `FINAL` `verification-ecdb8e48…` **`REJECTED_VERIFICATION_CORE / UNTRUSTED`**; reconciliation receipt `7a4c4825…` (not a `FINAL` receipt); `R3-X2 = NOT NEEDED` |
| Input maintenance | `a6f5c5fc` | `107a9847` | `PASS — AUTHOR APPROVED` | `GLOBAL_IMPACT`; `FINAL` `verification-9cce80aa…`, receipt `e633c952…` |
| Historical-suite maintenance | replay `20351316` (source `2f1b091e`) | `38708442` | `PASS — AUTHOR APPROVED` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`; `INFRA_UNIT` + `STATIC` |
| Generated-state maintenance | `d8a63d49` | `62b1281b` | `PASS — AUTHOR APPROVED` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE` (global: active leaf added); `FINAL` `verification-8fc55499…` 41/41, receipt `69feae5c…` |
| R4 | `13d1192b` (corrective descendant of `558c15c9`) | `cfed55fa` | `PASS — AUTHOR APPROVED` | `BOUNDED_PHASE`; `PHASE` `verification-ffc495c4…` |
| R5-A | `4804f505` | `4389b30b` | `PASS — AUTHOR APPROVED` | `BOUNDED_PHASE`; `PHASE` + `STATIC` |
| ADR 0031 | `c459de9a` | `e16697d9` | `ACCEPTED — AUTHOR APPROVED` | `DOCUMENTATION_STATUS_ONLY`; `STATIC` |
| Dot/XML maintenance | `4f9bc944` | `504a19f7` | `PASS — AUTHOR APPROVED` | `GLOBAL_IMPACT`; `FINAL` `verification-7e9764bc…` 41/41, receipt `d1de410f…` |
| R5-B | `a1d96746` (`R5-B-R2`) | `e7401734` | `PASS — AUTHOR APPROVED` | `INTEGRATED_PHASE`; `INTEGRATION` `verification-c23ef3bd…` 19/19 |
| R6 | `4333d032` (+ `D_R6_DOC` `dd486b59`, no product delta) | `9b8bc5b1` | `PASS — AUTHOR APPROVED` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`; `INFRA_UNIT` + `STATIC` + `PHASE` |
| R6-plus planning | `147dac8d` (+ `af2aa113`) | `babf20c7` | `PLANNING PASS — AUTHOR APPROVED` | `DOCUMENTATION_STATUS_ONLY` |
| R6-plus P0 | `6b7fd5de` | `562e2bb1` | `PASS — AUTHOR APPROVED — PUBLISHED` | `DOCUMENTATION_STATUS_ONLY` |
| A-1 | `2a71133a` | `d32ad608` | `PASS — AUTHOR APPROVED — PUBLISHED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` |
| B | `848206c4` | `0ef616bb` | `PASS — AUTHOR APPROVED — PUBLISHED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` |
| D0 | `8383a153` | `3268f9b9` | `PASS — AUTHOR APPROVED — PUBLISHED` | `DOCUMENTATION_STATUS_ONLY`; `STATIC` |
| D1 | `6e1d8459` | `2941ddf2` | `PASS — AUTHOR APPROVED — PUBLISHED` | `GLOBAL_IMPACT`; `FINAL` `verification-9cf1a0af…` 41/41, receipt `bbbdb5b3…` |
| A-2 | `06a2ea6b` (revision 3) | `e6135028` | `PASS — AUTHOR APPROVED — PUBLISHED` | `GLOBAL_IMPACT`; `FINAL` `verification-814f89fa…` 41/41, receipt `b492d484…` (last `FINAL` before R7, 2026-10-04) |
| C | `6248bf0f` | `39eb05bc` | `PASS — AUTHOR APPROVED — PUBLISHED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` |
| C-X1 | `eabe6368` | `23b971f5` | `PASS — AUTHOR APPROVED — PUBLISHED` | `BOUNDED_PHASE` |
| E1-L | `5f34d181` | `47b39e5f` | `PASS — AUTHOR APPROVED — PUBLISHED` | `BOUNDED_PHASE` |
| E1-P (E1 aggregate) | `dcda4075` | `fdc1ade6` | `PASS — AUTHOR APPROVED — PUBLISHED` | `OPERATIONAL_VERIFICATION_INFRASTRUCTURE`, `PHASE_LOCAL`; `PHASE`, `INFRA_UNIT`, `PACKAGING` ×3 |
| E1-X1; E1-P-X1 | `b12ab32c`; `44b90156` | `7d132ec5`; `dc63b0e5` | `PASS — AUTHOR APPROVED — PUBLISHED` | `BOUNDED_PHASE` |
| E2 | `3a724661` | `86fdc908` | `PASS — AUTHOR APPROVED — PUBLISHED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` |
| POST-E2 P1, P1-R1, P1-R2, P2, P2-R1, P3, P4 | `02d84945`, `2237df0a`, `6dde194e`, `216964dc`, `38647c79`, `1e8714a9`, `06835fdf` | `64d25ba8`, `0bb34739`, `b6747a1b`, `32c3efc4`, `9bdcdf83`, `2b3fafe3`, `13e08ac1` | `PASS — AUTHOR APPROVED` (P1, P1-R1, P2 as characterizations; P2-R1 and P4 by conditional approval, conditions verified) | `BOUNDED_PHASE` or `INTEGRATED_PHASE` per record |
| E3 (E3-R1) | `7cd50116` | `31286a23` | `PASS — AUTHOR APPROVED` (published later) | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION`; Asymptote label `UNAVAILABLE — NOT CLAIMED PASS` |
| POST-E3-INTEGRATION | `00b525fd` | (its own record) | `AUTHOR APPROVED — PUBLISHED` per the plan and the G closeout §6 (see §3.3) | — |
| POST-E3 PSTricks C1 | `22a215d8` | `bde2827b` | `PASS — AUTHOR APPROVED — PUBLISHED` | `BOUNDED_PHASE` |
| F1 | `1cea3c89` | `24985f14` | `PASS — AUTHOR APPROVED — PUBLISHED` (excludes routes X1–X10 and Graphics View 2) | `BOUNDED_PHASE` |
| F2 (F2-R1) | `488f1e99` | `e70718fe` | `PASS — AUTHOR APPROVED — PUBLISHED` | `BOUNDED_PHASE` |
| F3 (F3-R1, F3-R2) | `2d7b2cae` | `847bb20a` | `PASS — AUTHOR APPROVED — PUBLISHED` | `INTEGRATED_PHASE`; `PHASE` + `INTEGRATION` |
| F4-D; F4-A; F4 aggregate | `6214d15a`; `6baa66a5`; — | `e968245e`; `07424dee`; `19f8f206` | `PASS — AUTHOR APPROVED — PUBLISHED` (F4-D, F4-A); `COMPLETE — AUTHOR APPROVED` (F4; F4-B closed for AutoCAD 2026; F4-C deferred as `TD-PDF-1`) | `INTEGRATED_PHASE` (F4-D); `BOUNDED_PHASE` (F4-A) |
| G (design; G-R1 T') | design `caf740a1`; T' `da1a0952` | design `ceb5a7e9`; closeout `b9bdb1e1` | `PRE-G9B-R6-plus-G = PASS — AUTHOR APPROVED`; `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED`; published as `main` `b9bdb1e1` on 2026-10-15 | `INTEGRATED_PHASE`; `STATIC`, `INFRA_UNIT`, `PHASE`, `INTEGRATION`, `PACKAGING` ×3 on T'; "`FINAL` was not run"; superseded T `dd3937cf` |
| **R7** | — | — | `EXECUTION AUTHORIZED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW` | `GLOBAL_IMPACT`; `FINAL` on R7's own candidate |

### 3.1 Distinct facts that are not merged

- **`PRE-G9B-R1`.** Its single `FINAL` `verification-8763e250d1be413f83e54429b5923ca3`
  (plan `defe8ab4…`, result `c7e50789…`) reached `COMPLETE` coverage (40/40) with acceptance
  **`REJECTED_SEMANTIC`** and **no receipt**. The only violation was
  `PostP1BilingualUserGuideTest.everySelectedResourceResolvesToTheDocumentOfThatLanguage`,
  causally proven pre-existing (`BASELINE-GUIDE-RESOURCE-LF-MATERIALIZATION`). The author then
  recorded "an explicit author exception to the normal green-FINAL closeout requirement",
  which "does not change the recorded FINAL verdict"; "technical acceptance was not
  established, and none is claimed". R7 keeps all three facts: the rejected verdict, the
  author exception and the absence of technical acceptance. A successful R7 `FINAL` does not
  change them retroactively.
- **`PRE-G9B-R3-X1`.** Its source `FINAL` `verification-ecdb8e48…` stays
  `REJECTED_VERIFICATION_CORE / UNTRUSTED` (stale `final.shared` inventory). The author
  approved the exact pair T + R through `GOV-R`'s separate reconciliation receipt
  `7a4c4825…`, which is not a `FINAL` receipt. R7 inherits neither as its own acceptance.
- **Approved phases with retained verification limitations** (E3 Asymptote label, F1
  exclusions, the R6-plus accepted limitations) are approvals, not technically accepted
  `FINAL` results of R7.

### 3.2 Global verification-infrastructure changes in the track

| Phase | Change |
|---|---|
| `PRE-G9B-R1` `36c8d754` | `tools/agent/verify.ps1` imports the receipt module and emits a `FINAL` receipt only on a completed, accepted, complete run; static-contract catalog pin |
| `GOV-R` `8e4218db` | post-run deterministic catalog reconciliation mechanism and receipt schema |
| Generated-state maintenance `d8a63d49` | active leaf `infra.repository-generated-state`; `FINAL` plan 67 → 68 nodes (`defe8ab4…` → `3d8752b9…`) |

These justify the author's frozen `GLOBAL_IMPACT` class: the accumulated candidate carries
global verification infrastructure that only `FINAL` exercises completely, and no `FINAL`
has run since A-2 (2026-10-04); B, C, E1–E3, POST-E2/POST-E3, F1–F4 and G were accepted on
`PHASE` and `INTEGRATION`.

### 3.3 Lineage observations (no blocker)

1. R0 has no separate closeout record; its approval is written in the amended R0 candidate
   report and its evidence commit `48b182b8`.
2. POST-E3-INTEGRATION `00b525fd`: its own record keeps its pre-approval text ("pending
   author review"); the approval and publication are recorded by the mini-track plan
   (`22a215d8`) and by the author-approved G closeout §6, which lists it as
   `PASS — AUTHOR APPROVED — PUBLISHED`. R7 relies on the G closeout.
3. The R1 closeout mirror's `publicationState` predates its promotion; the roadmap records the
   publication at `8cc42981`.
4. The G closeout record, the plan and the roadmap said "unpublished"; the closeout is `main`
   `b9bdb1e1` since 2026-10-15 (`git ls-remote`; ignored publication audit). R7 registers the
   publication in the living roadmap and plan only; the closeout record is not edited.

### 3.4 Obligations inherited by R7

From the R6 and G closeouts and the `Ri` policy: an owner and disposition for
`TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`; the reconciliation of the R-track ledger;
P-12 and the pinned remainder of P-13; the O-1/O-2 follow-up; owner decisions for the
author-accountable R6-plus rows at the R7 checkpoint; the G11/G12/G15 review; the R7 author
smoke. They are discharged or proposed in the
[debt reconciliation](pre_g9b_r7_debt_reconciliation.md), the
[roadmap review](../architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md) and the
[author-review sheet](pre_g9b_r7_author_review_sheet.md).

## 4. Reconciled execution contract

The canonical prompt
[`pre-g9b-r7-readiness-closeout.prompt.md`](../../.github/prompts/tasks/pre-g9b-r7-readiness-closeout.prompt.md)
was amended first, as the author authorized ("R7 prompt-specific documentary
reconciliation required to record these inputs and the current approved predecessor
states"), following the `PRE-G9B-R6` precedent of amending a stub into the execution
contract of its own instance:

- blob `be941a71454b85abf21a55f8f141b88b44a12281` → the R7 candidate's blob (reported with
  the candidate);
- resolved: `IMPLEMENTATION_BASE`, `IMPLEMENTATION_TREE`, `AUTHORIZED_PRIOR_PHASES` (the
  enumeration of §3), the frozen class and acceptance profile, the closeout mode;
- the stub's historical sentence "no `PRE-G9B-R` phase after `R0` has been authorized" is
  marked superseded by the later author-approved closeouts and is not a stop condition; its
  dependency `PRE-G9B-R6-plus = PASS — AUTHOR APPROVED` is recorded as satisfied;
- scope, forbidden scope and stop conditions restate the author instruction, including the
  packaging-documentation addendum; G11, G12 and G15 are named in the forbidden scope;
- `Test-PromptContractDocument` (profile `task`): seven fields present, unique and ordered;
  `execution_safe = true`. No competing prompt authority is created.

## 5. Candidate scope (allowed tracked changes)

Documentation, roadmap and machine-readable mirrors only:

| Path | Change |
|---|---|
| `.github/prompts/tasks/pre-g9b-r7-readiness-closeout.prompt.md` | execution-contract amendment (§4) |
| `docs/validation/pre_g9b_r7_phase_entry_record.md` | this record (A, B) |
| `docs/validation/pre_g9b_r7_debt_reconciliation.md` | consolidated debt reconciliation (C) |
| `docs/architecture/pre_g9b_r7_g11_g12_g15_roadmap_review.md` | roadmap-review package (D) |
| `docs/validation/pre_g9b_r7_readiness_report.md` | integrated readiness report (E) |
| `docs/validation/pre_g9b_r7_candidate_report.md` | frozen candidate report (F) |
| `docs/validation/pre_g9b_r7_author_review_sheet.md` | author-review sheet and smoke checklist (H) |
| `geocedg/validation/pre-g9b-r7/*.json` | mirrors of the entry, the debt reconciliation and the candidate evidence |
| `docs/roadmap/geocedg_roadmap.md` | factual status and reference corrections |
| `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md` | publication state of the global R6-plus closeout (living plan) |
| packaging and licensing living documentation (§6) | distribution-status and version references |

No source, test, build, packaging-input, registry, schema, inventory or verifier file
changes; no historical closeout record or accepted evidence changes.

## 6. Author addendum: distribution-status and version references

The author's addendum asks to "reconcile obsolete distribution-status and version
references in `windows-packaging.md`, `geocedg_profile_nc.md` and related living
documentation", preserving `INTERNAL` as the explicit repository default, recognizing `NC`
as the approved non-commercial packaging profile, and distinguishing package construction
from authorization to publish a public release, without modifying packaging behaviour or
historical licensing evidence.

Current truth (unchanged by R7): `packaging/windows/package.yml` sets
`"default_profile": "INTERNAL"` and version `1.1.0` / `2026-10-15`; three profiles exist
(`INTERNAL` internal evaluation, not redistributable; `NC` public non-commercial,
redistributable licence condition, approved by `PRE-G9B-D1` and packaged since
`PRE-G9B-P1`; `COMMERCIAL` refused before any build work); publication and public release
are not authorized.

Edited living documents: `geocedg/specs/packaging/windows-packaging.md` (status lines, build
step 4, `-RequireArtifacts` wording, release gate), `docs/licensing/geocedg_profile_nc.md`,
`docs/licensing/component-matrix.md` (current-state paragraph only),
`docs/licensing/commercial-policy.md` (factual status note only, not the policy),
`docs/developer/geocedg_developer_guide.md`, `docs/developer/repository_map.md`,
`docs/developer/geocedg_operations_manual.md` (package file names and window title follow
the current version; the composition list includes the curated GGT library; the D1 audit
closure) and the roadmap §9 status sentence. Every string asserted by
`PostP1BilingualUserGuideTest`, `PreG9BR6PlusGProductMetadataTest`,
`PreG9BR6PlusE1LCuratedLibraryTest`, `PreG9BP1PublicSurfaceTest` and the documentation
diagnostic is preserved.

Deliberately unchanged: `packaging/windows/package.yml` (its top-level
`public_redistribution` value is the `INTERNAL` default's record and a pinned packaging
input), the packaged notices `INTERNAL_EVALUATION_ONLY.txt` and `NC_DISTRIBUTION_NOTICE.txt`,
`NOTICE.md`, `THIRD_PARTY.md`, `LICENSE`, `LICENSES/**`, the dated D1 evidence, every
`docs/validation/**` record, the historical section of the component matrix, ADR 0004 and
the superseded quick guide. Observations for the author (not edited): the `INTERNAL`
notice's stated reason predates D1; `LICENSES/UNRESOLVED.md` carries the `INTERNAL` marker
and also ships in `NC` packages; `NOTICE.md` and `THIRD_PARTY.md` miss a space in
"profile-neutral.The" (packaged bytes).

## 7. Impact declarations

```text
PRODUCT_PHASE_EFFECT          = NONE
SERIALIZATION_IMPACT          = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = NONE (no verifier, registry, schema, inventory or pin change)
BOOTSTRAP IMPACT              = NO CHANGE REQUIRED (no workstation prerequisite changes)
GUIDE_IMPACT                  = NONE
GUIDE_JUSTIFICATION           = no observable behaviour, command, installation path or exported
                                format changes; the bilingual user guides already state 1.1.0,
                                the distribution conditions and the profile-aware condition
DEVELOPER_DOCUMENTATION       = UPDATED (developer guide, repository map, operations manual:
                                distribution-status and version references only)
```

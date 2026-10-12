# PRE-G9B-R6-plus-F2 / F2-R1 joint closeout record

```text
PRE-G9B-R6-plus-F2        = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-F2-R1     = PASS — AUTHOR APPROVED — PUBLISHED
AUTHOR_SMOKE              = PASS (author confirmation)
APPROVED IMPLEMENTATION   = 488f1e991fee641b9ec521059edc318df7271bdd
                            tree 9673d9afca483dbd8129c92c76639711596cf13b
BOUNDED_PHASE             = ACCEPTED / COMPLETE (PHASE PRE-G9B-R6-PLUS-F2-R1, 3/3)
F2/F2-R1 SPECIFICATION    = NORMATIVE / AUTHOR APPROVED
F2 ORIGINAL CANDIDATE     = 6af85ee3 — FROZEN — HISTORICAL EVIDENCE
FR-2 HISTORICAL BLOCKER   = RESOLVED BY AUTHOR-APPROVED O1 REPLACEMENT
OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS
                          = OPEN — NON-BLOCKING FOR F2/F2-R1 — CAUSAL ATTRIBUTION NOT
                            ESTABLISHED
F3                        = PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
G                         = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (F2 and F2-R1 only, implementation 488f1e99)
passClaimed               = true    (F2 and F2-R1 only)
implementationAuthorized  = false   (F3, F4, G and every open observation)

candidateFrozen           = true
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE    (closeout; BOUNDED_PHASE accepted by PHASE)
closeoutClass             = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
```

This record preserves the joint author decision of 2026-10-12 on
`PRE-G9B-R6-plus-F2` and its corrective successor `PRE-G9B-R6-plus-F2-R1`,
given in writing in the session ("GeoCeDG — PRE-G9B-R6-plus-F2-F2R1-AUTHOR-CLOSEOUT —
Joint Author Approval, Normative Promotion and Controlled Publication"; AUTHOR
AUTHORIZED — JOINT F2/F2-R1 CLOSEOUT AND CONTROLLED PUBLICATION). It is the
sole authority for the author approval of F2 and F2-R1. Its machine-readable
mirror is
[`pre-g9b-r6-plus-f2-f2r1-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-f2r1-closeout.json).

Earlier states are recorded, and not amended, in: the F2
[candidate report](pre_g9b_r6_plus_f2_candidate_report.md), the
[F2 author-decision record](pre_g9b_r6_plus_f2_author_decisions_record.md) v1.1,
the [F2 reconciled design](../architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md),
the F2-R1 [characterization report](pre_g9b_r6_plus_f2_r1_activation_characterization_report.md),
[alternatives candidate](../architecture/pre_g9b_r6_plus_f2_r1_activation_alternatives_candidate.md),
[dual-activation design](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md),
[F2-R1 author-decision record](pre_g9b_r6_plus_f2_r1_author_decisions_record.md) v1.3,
[feasibility gate report](pre_g9b_r6_plus_f2_r1_feasibility_gate_report.md),
[revised `FR-2` gate report](pre_g9b_r6_plus_f2_r1_fr2_o1_gate_report.md) and
F2-R1 [candidate report](pre_g9b_r6_plus_f2_r1_candidate_report.md). They remain
historical evidence.

## 1. Entry verification

```text
PUBLISHED BASE        = 6d9bafb04235a71c8dfadef9631cd6057964b1f9
                        tree 6bc65715cd37f5872b804eea7949d2f9fea96092
                        (local main = origin/main = live remote main)
LINEAR CHAIN          = 6d9bafb0 → bdb9b6a9 → 6af85ee3 → b7faad3a → d630b8dd →
                        e13bfbd2 → de5c2677 → 7027cf93 → fdb55179 → 72b14a9b →
                        488f1e99 (one parent each; every tree re-resolved)
WORKTREE              = clean
WIP BRANCH            = wip/pre-g9b-r6-plus-f2-r1-fr2-blocked (654f7021) — not in the
                        chain; not merged, not published
```

| Commit | Role | Tree |
|---|---|---|
| `bdb9b6a94cf95ccebd1beb2f3f31f2bab523c75a` | F2 authorization | `674fcfd479e377f70f511ddd22eb95136add0252` |
| `6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873` | original F2 technical candidate | `3d412a801024411ebfcbaf8469691ec00f7c4fee` |
| `b7faad3af2cbce0c2254cbdc7366c96a3df5ad48` | F2-R1 activation characterization | `980207638c8bd045621879b2a28f407104b08bc4` |
| `d630b8dd3ace0cafd28f9896a8964778dd346e2e` | F2-R1 design reconciliation | `3ed8c9689f11a7a877eaae08c1b97f7fbaf54c12` |
| `e13bfbd231700019a8f81c115b22a5bb8c44f7bb` | F2-R1 approved design closeout | `2e8ee23addebcb05c4505c30e1fa593c8a3d4fa7` |
| `de5c267767693d1051d7089cf4d19cc7d4bfe7a4` | F2-R1 implementation authorization | `31e20c2607cc821e0dc968f829f2f5ca5a4406e2` |
| `7027cf9357571298fe335343d01bd258f3c190f9` | original `FR-2` feasibility blocker | `d259cb5db3b98da26926faa6768c307f27817b97` |
| `fdb5517970d7fcf94f6fea43c2a6078f83ab19e4` | revised `FR-2` author disposition | `42303f97d2b274809cc4bea83643bfdb2910a2ba` |
| `72b14a9bff4cd1bf66ab58f248b3a2a9ef4fcac8` | revised feasibility gate | `402b2d532134bc0a2a3c10a692913560bf32dba0` |
| `488f1e991fee641b9ec521059edc318df7271bdd` | final approved technical candidate | `9673d9afca483dbd8129c92c76639711596cf13b` |

## 2. Author smoke (mandatory gate)

The author confirms `AUTHOR_SMOKE = PASS` on the F2-R1 candidate (smoke
checklist of the F2-R1 candidate report §6). The agent did not perform the
smoke and makes no claim beyond the author's confirmation.

## 3. Author decision

The author approves the final F2 implementation, incorporating the F2-R1
corrective dual-activation contract, as implemented by `488f1e99`. The approval
covers:

1. independent session-only reference memories for Parallel and Perpendicular;
2. exact-instance currentness, validity, suspension and lifecycle;
3. normal tool activation without memory participation;
4. Ctrl-enabled interactive reference memory on the supported Windows toolbar
   routes;
5. preservation of stored references during normal constructions;
6. safe switching between `NORMAL` and `MEMORY_ENABLED`;
7. scripting operating exclusively under the ordinary `NORMAL` behaviour;
8. the existing, unchanged `GgbAPID` and JavaScript sandbox;
9. the localized memory-mode status indication;
10. Angle with Given Size hidden by default on the approved tool route.

The author accepts the documented platform and interaction limitations (§6)
without treating unverified behaviour as supported. No additional functional
scope is authorized. The original F2 candidate `6af85ee3` stays frozen
historical evidence; the accepted implementation of F2 is its F2-R1 corrective
successor, not a second independent product implementation.

## 4. Accepted evidence

Original F2 candidate (historical; establishes the original baseline, does not
replace the corrective acceptance):

| Profile | Run | Verdict | Candidate / tree |
|---|---|---|---|
| `PHASE PRE-G9B-R6-PLUS-F2` | `verification-ac975f8c79ec4d58ba018ca4eb2a4cbd` | `ACCEPTED / COMPLETE` 2/2 | `6af85ee3` / `3d412a80` |
| `STATIC` | `verification-49e939c13b71429bbcf246ca08480c3f` | `ACCEPTED / COMPLETE` 3/3 | `6af85ee3` / `3d412a80` |
| `INFRA_UNIT` | `verification-2a4c7afa3282477ca3412c0a8de49f33` | `ACCEPTED / COMPLETE` 22/22 | `6af85ee3` / `3d412a80` |

F2-R1 corrective candidate (acceptance of the approved implementation):

| Profile | Run | Verdict | Result hash |
|---|---|---|---|
| `PHASE PRE-G9B-R6-PLUS-F2-R1` | `verification-3a1ac4a636834a32b11baf7d71fbbbd9` | `ACCEPTED / COMPLETE` 3/3 (`compile.desktop.semantic`, `workspace-profile.product`, `junit.desktop.pre-g9b-r6-plus-f2-r1.semantic` — 301 tests) | `9a9fa6d5d91c1b179019c7757f50b2afa95fc0eb5c94e73bc3ae04151168060d` |
| `STATIC` | `verification-d21879b09b5e471ea05ca9fc5aa612df` | `ACCEPTED / COMPLETE` 3/3 (standing `diagnostic.governance` finding, `diagnostic.historical-consistency` unavailable) | `8f895d2213cdfa67317464b7228d70d015a6cb51d9957bc3f8f74adf3c5adb05` |
| `INFRA_UNIT` | `verification-19556b4d051c4b50b291e57b9b298daa` | `ACCEPTED / COMPLETE` 22/22 | `468520626bc6f87fca1a4c5d19d7697582240b7c36652724ead6a130d39618ec` |

All three results name candidate `488f1e991fee641b9ec521059edc318df7271bdd`
and tree `9673d9afca483dbd8129c92c76639711596cf13b`; receipts in
`artifacts/agent/pre-g9b-r6-plus-f2-r1-{phase,static,infra-unit}/`.

Full Desktop execution (`final.desktop`, executed producer, third run): 2039
tests, 0 failures, 1 skipped (evidence SHA-256
`1feb880d6cdd0df6064b187ed5ce0c86ad3db02205210a69e436b4c4e092ef19`). It ran on
the working tree before the candidate commit; every source, test and profile
file of `488f1e99` predates that run, and only documentation, the regenerated
JUnit catalog, the registry pin and the coverage pins were written afterwards
(the catalog from this run's evidence). `PHASE` re-executed the 301-test F2-R1
selection on the exact commit. No heavy verification is repeated for this
documentary closeout.

## 5. Specification promotion

[`geocedg/specs/ui/authoring-memory.md`](../../geocedg/specs/ui/authoring-memory.md)
moves from `PROPOSED` to `NORMATIVE / AUTHOR APPROVED`, version `1.1`, without
any new requirement. It governs the two independent reference memories,
admissibility and exact-instance currentness, suspension and invalidation,
replacement only after a certified successful construction, the `NORMAL` and
`MEMORY_ENABLED` activation contracts, interactive-only memory activation,
scripting compatibility, same-tool reactivation, cancellation of incomplete
gestures, language changes, the transient status indication, Angle with Given
Size visibility, the Graphics View 1 scope and the documented limitations; it
adds no serialization, command, geometric algorithm or kernel identity
semantics. The status block, the approval scope, §0 and the §10 heading are
updated; no requirement text changes. The F2 and F2-R1 design records are
preserved unchanged. No ADR is required.

## 6. Observations and limitations

```text
FR-2 HISTORICAL BLOCKER = RESOLVED BY AUTHOR-APPROVED O1 REPLACEMENT
```

The failed `FR-2` attempt — a GeoCeDG subclass of `GgbAPID` returned by
`AppGeoCeDG.getGgbApi()`, rejected on every document load by the Rhino class
shutter (`CallJavaScript.SandboxClassShutter`, `ScriptUtil.ALLOWED`) — stays
historical evidence ([feasibility gate report](pre_g9b_r6_plus_f2_r1_feasibility_gate_report.md),
`7027cf93`; the work-in-progress branch is preserved and not published). The
replacement keeps the existing API and uses the GeoCeDG-owned application
activation seam `AppGeoCeDG.setMode(int)` (record v1.3 §7; revised gate
`72b14a9b`). The sandbox is not changed.

```text
OBS-R6PLUS-F2R1-FULL-SUITE-MODAL-ERROR-DIALOGS
    = OPEN — NON-BLOCKING FOR F2/F2-R1 — CAUSAL ATTRIBUTION NOT ESTABLISHED
```

During the F2-R1 inventory regeneration, the first two executed `final.desktop`
runs failed only in `PreG9BR6PlusF2R1DualActivationTest` (run 1: 17 failures,
including the two status-bar order tests then not yet adapted; run 2: 15). A
diagnostic run of the complete Desktop task showed four visible
application-modal "GeoGebra - Error" dialogs and an empty event trace when the
class ran; AWT modality then discards posted events to other windows. Which
earlier classes leave these dialogs is not established; the `org.geocedg.desktop`
classes alone did not reproduce it. The accepted test-harness correction —
the class's own window excluded from application modality
(`APPLICATION_EXCLUDE`) and every toolbar activation asserted to fire the
native button, with a diagnostic on failure — is documented in the F2-R1
candidate report §2; run 3 passed (2039 tests, 0 failures). Evidence, preserved
in `artifacts/agent/pre-g9b-r6-plus-f2-r1-producer-evidence/` (with the
diagnostic logs):

| Run | `final.desktop` | evidence SHA-256 | F2-R1 class XML SHA-256 |
|---|---|---|---|
| 1 (failed) | 2039 tests, 17 failures, 1 skipped | `e1382a5e5aa8f9fb1d99587c83b891d55ca623eeaef902562813b32bab242bfd` | `af6476a6f812aa754401006e1c714c173a6c0661334b7cbce58f134aca936744` |
| 2 (failed) | 2039 tests, 15 failures, 1 skipped | `30f84a0911dffae0fcc523cd719cb221f8322bf2b7a6f94cd03c39131c13f3f1` | `37d241e85ed567b65d82fbe23e86d9fc6afc02f0b6f2a7cf28c58bdbf8338e3b` |
| 3 (accepted) | 2039 tests, 0 failures, 1 skipped | `1feb880d6cdd0df6064b187ed5ce0c86ad3db02205210a69e436b4c4e092ef19` | `73187671d26c912ef46cb29e59b77f735b56cae191c38ff4ee7a5ff7285a9b3d` |

No test-infrastructure correction is initiated by this closeout; a correction
needs its own characterization and authorization.

Accepted limitations, not claimed as supported: macOS (Command) and Linux
(Ctrl) modifiers; undocked toolbars; window-focus and modal-dialog activation
routes; Ctrl+Space (no activation); operating-system-level Ctrl+click beyond the
author smoke; Graphics View 2 and Classic (inherited behaviour); any scripting
or API activation of the memory (scripting is `NORMAL` only); the residual risk
that a future inherited single-argument `setMode(int)` caller with a variable
Parallel/Perpendicular mode would count as a genuine `NORMAL` request.
Unchanged open observations of the mini-track keep their states.

## 7. Closeout verification and publication

Closeout class `DOCUMENTATION_STATUS_ONLY`: this record and its mirror, the
specification promotion, roadmap 5.21 and the mini-track plan; no product,
test, catalog or prompt change. Acceptance by official `STATIC` on the
documentary successor commit (reported outside this record, which cannot name
its own commit).

The author authorizes publication of the complete verified chain
`6d9bafb0` → `bdb9b6a9` → `6af85ee3` → `b7faad3a` → `d630b8dd` → `e13bfbd2` →
`de5c2677` → `7027cf93` → `fdb55179` → `72b14a9b` → `488f1e99` → this closeout
commit by an ordinary non-force fast-forward of `main`, after the publication
gate (published base, linear history, candidate identity, receipts,
documentary-only closeout, clean worktree, `main`/`origin/main`/live remote
agreement, no other product commit). No amend, rebase, squash or rewrite; no
tag, release, installer, binary or version change. The work-in-progress branch
is not published.

## 8. Next activity

```text
F3   = PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED
       (next planned subphase; needs its own preparation, author decisions and
       implementation authorization; planned class INTEGRATED_PHASE)
F4   = PLANNING INTENT RECORDED — IMPLEMENTATION NOT AUTHORIZED (unchanged)
G    = NOT AUTHORIZED (unchanged)
```

No subsequent phase advances automatically. The states of every other phase,
enhancement and observation are unchanged.

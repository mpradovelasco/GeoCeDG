# PRE-G9B-R6-plus-F4-A author approval and closeout record

```text
PRE-G9B-R6-plus-F4-A      = PASS — AUTHOR APPROVED (with scope amendment 01, E5; one
                            approved implementation)
AUTHOR_SMOKE              = PASS (author confirmation; not performed by the agent)
APPROVED IMPLEMENTATION   = 6baa66a584ceef1973c854fb63b00aa7070708b4
                            tree bfc22c5dff6a1dcb7a371030c77f695a1a6b1925
                            parent 72f6c5af24bd1651e19764c8bdb02814949bb4b9
BOUNDED_PHASE             = ACCEPTED / COMPLETE (PHASE PRE-G9B-R6-PLUS-F4-A, INFRA_UNIT
                            22/22 and STATIC on the approved implementation)
ACCEPTED LIMITATIONS      = F4-A-L1, F4-A-L2, F4-A-L3 (§6)
OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT
                          = CLOSED — FULFILLED BY F4-A (6baa66a5), with F4-A-L1
F4-A STOP                 = e7b235dc — HISTORICAL EVIDENCE
F4-A WIP                  = 9b755225 — UNVERIFIED — NOT IN THE APPROVED LINEAGE
F4-D                      = PASS — AUTHOR APPROVED — PUBLISHED (unchanged)
F4-B                      = CLOSED FOR AUTOCAD 2026 (unchanged)
F4-C                      = DEFERRED — TD-PDF-1 (unchanged)
OBS-F4P-FREEHEP-XREF-SIZE = OPEN — INDEPENDENT OBSERVATION (unchanged)
F4-A PUBLICATION          = NOT AUTHORIZED
F4 AGGREGATE CLOSEOUT     = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
```

```text
selfApproved              = false
authorApproved            = true    (F4-A with scope amendment 01 only, implementation 6baa66a5)
passClaimed               = true    (F4-A only)
implementationAuthorized  = false   (no further change)
candidateMutated          = false
PRODUCT CHANGE            = NONE    (closeout)
newHeavyVerification      = NONE
closeoutClass             = DOCUMENTATION_STATUS_ONLY (acceptance STATIC)
```

## 1. Author instruction and scope

On 2026-10-14 the author authorized "GeoCeDG — PRE-G9B-R6-plus-F4-A AUTHOR APPROVAL AND
CLOSEOUT". The author confirms that the interactive F4-A smoke completed successfully
(`AUTHOR_SMOKE = PASS`) and explicitly approves the technical candidate `6baa66a5` (tree
`bfc22c5d`, parent `72f6c5af`). The approval covers F4-A with scope amendment 01 and the
E5 implementation:
- numeric line-thickness input in the compact GeoCeDG style-bar popup, in Graphics 1 and
  Graphics 2;
- the field immediately right of the existing slider, with the line-type table kept;
- slider synchronization, the existing ranges and validation;
- Undo/Redo and persistence;
- Properties-field compatibility;
- Classic preserved;
- architecture A1 without inherited source modifications.

The author also disposes of the known limitations (§6). The authorization covers this
documentary closeout only, with no publication. This record is the sole authority for the
F4-A approval; the agent records the decision and does not approve its own work. Mirror:
[`pre-g9b-r6-plus-f4-a-author-closeout.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-a-author-closeout.json).

## 2. Entry verification and lineage

```text
PUBLISHED BASE  = e968245ef329a86108df81a49cbdb05601cefe9e (live remote main, read 2026-10-14)
LINEAR CHAIN    = e968245e → 1b944afb → e7b235dc → 72f6c5af → 6baa66a5 (one parent each)
WIP 9b755225    = not an ancestor of 6baa66a5
WORKTREE        = clean; no stash
```

| Commit | Role |
|---|---|
| `1b944afbc88ef46402c20fccd49eea5e66d64371` | Operation 1: F4-D published status, direct-publication preference (STATIC `verification-51ca8c0a54174edcb3faa66cd105f790`) |
| `e7b235dca9afc68f051faefa133e2db585bfea4c` | F4-A focus gate STOP (historical; STATIC `verification-0e240e69f18c423eb0a5e03ce0c30432`) |
| `72f6c5af24bd1651e19764c8bdb02814949bb4b9` | scope amendment 01 (STATIC `verification-70b649a2a5f74a9fbc0a5b8e07ab252a`) |
| `6baa66a584ceef1973c854fb63b00aa7070708b4` | **technical candidate — approved implementation** |

Historical evidence kept unchanged:
- the [stop report](pre_g9b_r6_plus_f4_a_focus_gate_stop_report.md) and the
  [amendment record](pre_g9b_r6_plus_f4_a_scope_amendment_01_record.md);
- the [candidate report](pre_g9b_r6_plus_f4_a_candidate_report.md);
- WIP branch `wip/pre-g9b-r6-plus-f4-a-focus-blocked` (`9b755225`);
- the evidence folders `gate-evidence/` (`SHA256SUMS.txt` `3c432fcd…`) and
  `e5-gate-evidence/` (`7b64623c…`), both re-checked unchanged.

## 3. Accepted verification evidence

Re-read on 2026-10-14. Each `result_hash` was recomputed with `Get-VerificationResultHash`
over `Read-VerificationJson` and is equal to the stored one. All three runs name candidate
`6baa66a5`, tree `bfc22c5d` and `run_state COMPLETED`:

| Profile | Run | Verdict | Result hash |
|---|---|---|---|
| `PHASE PRE-G9B-R6-PLUS-F4-A` | `verification-528e53e44fa1400e933ead8cff52f4e5` | `ACCEPTED / COMPLETE`; `compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-f4-a.semantic` (159 identities) | `183dc178abf6e107f9447126f8c38fbd9a881ef33695a3cdd70f24d4251a0198` |
| `INFRA_UNIT` | `verification-0ea5a0c6b4e84831a6bb017088df73a8` | `ACCEPTED / COMPLETE` 22/22 | `1e6b1eccd78506f1e27511c57825a047479fbc047d5e102d910759c861d2ccd0` |
| `STATIC` | `verification-63e2bb4e88b441d3b4184ecbcfb449b4` | `ACCEPTED / COMPLETE` 3/3; standing governance finding and historical-consistency unavailable | `587f9d2936a39e27595261654f7de30343c398b6ec54d09b8a82740d2ed47ed5` |

Receipts: `artifacts/agent/f4a-amend01-phase/`, `f4a-amend01-infra-unit/` and
`f4a-candidate-static/`.

The native-input gate (`F4-A E5 FOCUS FEASIBILITY = PASS`, Temurin 25.0.4, FlatLaf 3.7) is
in `artifacts/pre-g9b-r6-plus-f4-a/e5-gate-evidence/`. Its provenance is the probe sources,
init scripts and logs, and the gated product blob identities, which are equal to the
candidate's.

## 4. Author smoke

```text
AUTHOR_SMOKE = PASS
```

The author confirms that the interactive F4-A author smoke completed successfully
(checklist: candidate report §10). This is an independent author confirmation. The agent
did not perform it, and the agent's automated real-window gate is a separate fact.

## 5. Approved implementation scope

As in the candidate report §2. `GeoCeDGEuclidianStyleBar` (A1) for Graphics 1 and
Graphics 2:
- the inherited slider sits in a GeoCeDG row with the numeric field at its right;
- that one popup is light-weight without the FlatLaf drop shadow;
- tooltip dismissal on opening, with a single in-place re-show;
- the field is enabled only where the keyboard reaches it;
- closing the popup ends a pending entry as focus loss does in Properties;
- one undo step per accepted entry.

No inherited source change and no global Swing or FlatLaf setting.

## 6. Accepted limitations (author disposition)

| Id | Limitation | Accepted conditions | Evidence |
|---|---|---|---|
| **F4-A-L1** — restricted numeric-entry availability | when another application-owned window overlaps the popup region and Swing selects a heavy-weight popup, the numeric editor is disabled | (1) visibly disabled; (2) cannot receive or process keyboard input; (3) line-type table operational; (4) slider operational; (5) normal editing resumes when the light-weight popup can be used | gate `owned.overlap.path` (disabled, no focus after a native click) and `owned.overlap.after.path` (light-weight, editing resumes); candidate report §3, §9 (`F4A-R-HEAVYWEIGHT-RESTRICTED`) |
| **F4-A-L2** — popup redraw | a single popup redraw when opening after a visible tooltip | the popup remains usable; no geometric or editing operation is duplicated (one undo step per accepted value) | gate `tooltip.hover.path`; diagnostics `f4a-e5-diag-*`; `F4A-R-TOOLTIP-RESHOW` |
| **F4-A-L3** — validated runtime | the real-window gate was validated with Temurin 25.0.4 and FlatLaf 3.7 | a relevant runtime or look-and-feel change requires renewed compatibility verification | gate logs; `F4A-R-LAF-VERSION` |

Universal availability of the numeric editor is not claimed. These limitations are
accepted characteristics of the implementation and authorize no further change.

## 7. Debt and observation dispositions

| Id | Disposition |
|---|---|
| `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` | **CLOSED — FULFILLED BY F4-A** (`6baa66a5`), with F4-A-L1 |
| `F4R-POPUP-FOCUS` | **CLOSED** — realized at the first gate (STOP `e7b235dc`); resolved by scope amendment 01 (E5), with F4-A-L1 |
| `F4R-LINETYPE-COUPLING` | retained, accepted (`DQ-F4-4`): inherited coupling, unchanged |
| `F4R-DEFAULTS-UNDO` | retained, documented: inherited, unchanged |
| `F4R-CLASSIC-LEAK` | **CLOSED — HELD** (Classic unchanged: in-JVM tests and real-window gate item 13) |
| `TD-PDF-1` (F4-C) | DEFERRED, unchanged |
| `TD-PDF-2`, `TD-PDF-3` (F4-D) | CLOSED, unchanged (F4-D closeout `e968245e`) |
| `OBS-F4P-FREEHEP-XREF-SIZE` | OPEN — independent observation, unchanged |

## 8. Closeout verification

Closeout class `DOCUMENTATION_STATUS_ONLY` covers:
- this record and its mirror;
- the F4 risk and debt register §7;
- roadmap 5.49 and the mini-track plan.

There is no product, test, build, specification, registry, inventory or accepted-evidence
change. Acceptance is by the official `STATIC` on this documentary successor of
`6baa66a5`, reported outside this record. PHASE and INFRA_UNIT are not repeated.

## 9. Publication readiness

```text
F4-A PUBLICATION = NOT AUTHORIZED
```

Assessment only, no action.
- **Target:** `main` of `mpradovelasco/GeoCeDG`.
- **Candidate chain:** `e968245e` → `1b944afb` → `e7b235dc` → `72f6c5af` → `6baa66a5` →
  this closeout.
- **Fast-forward:** the chain is linear, descends from the live remote `main` `e968245e`
  and contains no WIP or unrelated commit, so an ordinary non-force fast-forward would be
  possible after this closeout is accepted by `STATIC`.
- **Requirements:** a separate author authorization naming the exact closeout commit, a
  fresh read of the live remote `main` and the publication gate. The recorded preference
  for direct fast-forward selects only the mechanism.
- **Remaining F4 work:** the F4 aggregate closeout is not authorized.

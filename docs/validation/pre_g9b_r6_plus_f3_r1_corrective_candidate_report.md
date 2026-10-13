# PRE-G9B-R6-plus-F3-R1 — smoke-failure correction: corrective technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F3-R1-SMOKE-FAILURE-CORRECTION
AUTHORIZATION             = AUTHOR AUTHORIZED — BOUNDED CORRECTIVE IMPLEMENTATION (2026-10-13)
ORIGINAL F3 CANDIDATE     = 165471154cd16e8875994a21eb49fbc2f828216b
                            tree 8345c54df55452ffc598c431db28e67298076589
                            parent bc1c9ac6f6801e88437824ddcb981cb54d17eeb0
                            FROZEN — HISTORICAL EVIDENCE; its PHASE
                            verification-792ae35041c34102a0ea4f388e9b5541, INTEGRATION
                            verification-edbcafddf4294d0998fe711156d55c61 and STATIC
                            verification-c08f82ede9e343a79d88b36436c09e9a stay bound to it and
                            do not certify F3-R1
AUTHOR_SMOKE (F3)         = FAIL — BLOCKING (historical evidence)
BRANCH                    = phase/pre-g9b-r6-plus-f3-r1 (local, from 16547115)
VERIFICATION_CLASS        = INTEGRATED_PHASE (unchanged F3 contract; phase PRE-G9B-R6-PLUS-F3,
                            selection pre-g9b-r6-plus-f3.desktop extended additively)
TECHNICAL CANDIDATE       = the commit that contains this report (frozen); its identity and
                            the acceptance runs are reported outside this file
AUTHOR_SMOKE (F3-R1)      = PENDING
PRODUCT CLOSEOUT          = NOT AUTHORIZED; DEBT-R6PLUS-FILE-INSERT-SURFACE stays open
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false; authorApproved = false; passClaimed = false
```

Root causes: [root-cause report](pre_g9b_r6_plus_f3_r1_smoke_failure_root_cause_report.md);
findings: [risk and observation reconciliation](pre_g9b_r6_plus_f3_r1_risk_and_observation_reconciliation.md);
machine-readable mirror:
[`pre-g9b-r6-plus-f3-r1-corrective-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-r1-corrective-candidate-evidence.json);
original candidate report (unchanged): [F3 candidate report](pre_g9b_r6_plus_f3_candidate_report.md);
specification: [`document-insertion.md`](../../geocedg/specs/ui/document-insertion.md) v1.1
(`PROPOSED`).

## 1. Entry gate (2026-10-13)

| Check | Result |
|---|---|
| original candidate commit, tree, parent | `16547115…`, `8345c54d…`, `bc1c9ac6…` |
| linear ancestry from published `main` | `e70718fe` → … → `108d9cb6` → `bc1c9ac6` → `16547115` (11 commits) |
| `main`, `origin/main`, live remote `main` | `e70718fe` |
| receipts | PHASE, INTEGRATION and STATIC `ACCEPTED / COMPLETE`, bound to `16547115`/`8345c54d` |
| worktree | clean |
| original implementation, report, design, F3 selection | present; phase `PRE-G9B-R6-PLUS-F3` registered |
| changes after the original candidate | none |
| result | **PASS** |

## 2. Product changes

| Path | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java` | `newInsertionHelper` records the active window; `releaseInsertionHelper` guards each cleanup step separately, unregisters every hidden `GeoGebraFrame` of the helper, clears its current file and restores the previously active window |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGDocumentInsertion.java` | closure check accepts the target's built-in axis only where the source used the same axis; rejections keep stage and exception |
| `apps/geocedg/application-profile.yml` | text `Insert.Reject.Internal` (EN/ES) |

No change to `source/shared/**`, inherited GeoGebra classes (`GeoGebraFrame`,
`GuiManagerD`, `AppD`, `LoadFileListener`), Classic, the admission matrix, recovery,
isolation, accounting or serialization.

## 3. Behaviour before and after

| Smoke finding | Original candidate | F3-R1 |
|---|---|---|
| SF-F3-1 CircleM → Insert LocusV2 | rejected: "the trial insertion failed (closure f)" | inserted; the line and intersection bound to the target's axes; one undo step; UUID kept; persists |
| SF-F3-2 Open Recent | redirected to a hidden helper window holding the source | loads into the user window |
| SF-F3-3 title after New | `GeoCeDG 1.0.0 (1)` | `GeoCeDG 1.0.0` |
| SF-F3-4 exit | registered hidden windows prevent `AppD.exit(0)`; the process and Gradle wait | the process exits after the last window closes |
| SF-F3-5 both directions | +3 hidden windows per insertion attempt | none |

## 4. Tests

| Requirement | Test | Before (original) | After |
|---|---|---|---|
| F3R1-L1 | `PreG9BR6PlusF3R1LifecycleTest.f3r1l1…` | FAIL (1 helper window) | pass |
| F3R1-L2, F3R1-R2 | `f3r1l2…` | FAIL (3 helper windows) | pass |
| F3R1-L3 | `f3r1l3…` (insert, reject, Apply Styles × 3) | FAIL (15 helper windows) | pass |
| F3R1-R1, F3R1-R3 | `f3r1l1…`, `f3r1r3…` (New, Open Recent, Save, reopen) | FAIL (6 windows instead of 3) | pass |
| F3R1-A1 | `f3r1a1…` (the author's LocusV2 construction into the CircleM construction) | FAIL ("closure f") | pass |
| F3R1-A2 | `f3r1a2…` (closure still rejects a true outside input and a swapped axis) | FAIL (the valid same-axis case returned `closure L`) | pass |
| F3R1-X1 | `f3r1x1…` (post-commit fault with reload, garbage source, macro conflict, serious failure with isolation and Reopen) | FAIL | pass |
| F3R1-E1 | `PreG9BR6PlusF3R1WindowProcessTest` (child JVM, real `GeoCeDG.main` window, insert, Open Recent, New, real window close, both directions) | FAIL (process alive, watchdog exit 3; frames 1 → 4; title "(1)"; Open Recent not loaded) | pass (exit 0) |

The "Before" column is the run of the two test classes added to a `git archive` of
`16547115` (no product change): 9 of 9 failed. The fixtures rebuild the author's
constructions by commands (the user documents are not committed); the real files
were exercised by the scratch probe of the root-cause report on both trees.

## 5. Stop conditions

| Condition | Result | Evidence |
|---|---|---|
| FR-F3-1 … FR-F3-8 | retained; no accounting, history, snapshot, isolation, EDT, identity or compatibility code changed; FR-F3-7 strengthened (closure pairing per axis) | the full F3 selection re-run; `f3r1a1`, `f3r1a2`, `f3r1x1` |
| FR-F3R1-1 auxiliary lifecycle | PASS — released through public GeoCeDG-owned code, no shared or Classic change | `f3r1l1`–`f3r1l3`, `f3r1x1`, E1 |
| FR-F3R1-2 Open Recent integrity | PASS | `f3r1l1`, `f3r1l2`, `f3r1r3`, E1 |
| FR-F3R1-3 Locus V2 admission | PASS — SUPPORTED — CORRECTED within the approved `P4`; verification not weakened | `f3r1a1`, `f3r1a2` |
| FR-F3R1-4 application termination | PASS — exit 0 after the real window close in a child JVM | E1 (both directions) |
| FR-F3R1-5 regression integrity | PASS (automated); see §6 | §6 |

## 6. Verification

Development (not acceptance): the extended phase selection and Checkstyle (no findings
in changed files), upstream boundary OK; official producers refreshed the inventory
(`artifacts/agent/pre-g9b-r6-plus-f3-r1-inventory/`): `pre-g9b-r6-plus-f3.desktop` 467
tests and `final.desktop` 2094 tests, 0 failed; development `INFRA_UNIT`
verification-0fca21aaf32449d8ad1a7cb5a4659b38 and `STATIC`
verification-88ede42214454ab2b53e956d80f64598 `ACCEPTED / COMPLETE`. The frozen PHASE, INTEGRATION and
STATIC runs of this commit are reported outside this artifact; the original
candidate's receipts are not reused.

## 7. Author smoke checklist (F3-R1, not performed by the agent)

Start with `.\gradlew.bat :desktop:desktop:runGeoCeDG`, using copies of your documents.

1. Open `CircleM.cedg`; File → Insert → Insert File… → `LocusV2.cedg`: inserted;
   conflicting names renamed; the locus traces as in LocusV2; one Undo removes it all.
2. Open `LocusV2.cedg`; insert `CircleM.cedg`: inserted with renamed labels.
3. After each of 1 and 2 (save or discard first): File → Open Recent → each of the two
   documents loads into the same window; File → New shows the title `GeoCeDG 1.0.0`
   with no `(1)`; File → Open… loads normally; Save and reopen.
4. Repeat an insertion three times (and one rejected insertion, e.g. a document with a
   script): Window behaviour and titles stay normal; no extra window appears.
5. A rejected insertion names its reason (stage and cause).
6. Close GeoCeDG with the window's close button: the Gradle command returns to the
   prompt without Ctrl+C.
7. Re-check the original F3 checklist items you consider affected (labels, macros,
   Apply Styles, clipboard, Undo/Redo).

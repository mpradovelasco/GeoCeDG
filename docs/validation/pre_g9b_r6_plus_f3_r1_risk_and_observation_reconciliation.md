# PRE-G9B-R6-plus-F3-R1 — risk and observation reconciliation

- Task: `PRE-G9B-R6-plus-F3-R1-SMOKE-FAILURE-CORRECTION` (author authorization of
  2026-10-13)
- Original candidate: `165471154cd16e8875994a21eb49fbc2f828216b` (frozen; its
  automated receipts unchanged and not reused)
- Evidence: [root-cause report](pre_g9b_r6_plus_f3_r1_smoke_failure_root_cause_report.md);
  [corrective candidate report](pre_g9b_r6_plus_f3_r1_corrective_candidate_report.md)
- `selfApproved = false`

## 1. Blocking findings against the original F3 candidate

| Finding | Smoke | Root cause | Status on `16547115` |
|---|---|---|---|
| `F3-SF-HELPER-WINDOW-LEAK` | SF-F3-2, -3, -4, -5 | RC-1: hidden helper windows stay registered | **BLOCKING — CONFIRMED** (runtime evidence) |
| `F3-SF-LOCUSV2-AXIS-CLOSURE` | SF-F3-1 | RC-2: the closure check treated the built-in axes as outside objects | **BLOCKING — CONFIRMED** |
| `F3-SF-GENERIC-DIAGNOSIS` | SF-F3-1 | RC-3: unexpected exceptions reported without stage or cause | CONFIRMED (non-blocking) |

The original candidate's PHASE, INTEGRATION and STATIC receipts remain valid for what
they checked; they did not cover shown-window lifecycle, which the author smoke did.

## 2. Reconciliation against the F3-R1 candidate

| Finding | Status on the F3-R1 candidate | Evidence |
|---|---|---|
| `F3-SF-HELPER-WINDOW-LEAK` | **CORRECTED — TECHNICAL EVIDENCE** (author smoke pending) | `PreG9BR6PlusF3R1LifecycleTest`, `PreG9BR6PlusF3R1WindowProcessTest`; real-file probe |
| `F3-SF-LOCUSV2-AXIS-CLOSURE` | **CORRECTED — SUPPORTED** within `P4` (no matrix change) | `f3r1a1`, `f3r1a2`; real-file probe |
| `F3-SF-GENERIC-DIAGNOSIS` | CORRECTED | `GeoCeDGDocumentInsertion.describe` |

## 3. Observations

- `OBS-F3R1-TEST-LIFECYCLE-MASKING`: the shared test lifecycle extension disposes every
  frame created by a test, which hid the helper-window leak from the embedded F3 tests;
  the F3-R1 tests count windows inside the test and add a child-JVM shown-window test.
- `OBS-F3R1-LAUNCHER-ROUTE`: termination is verified on `GeoCeDG.main` in a child JVM
  with the test launcher; the `runGeoCeDG` Gradle route is an author-smoke item.
- The F3 observations `OBS-F3-PROTECTED-LABELS` and `OBS-F3-TEXT-THICKNESS` are
  unchanged.

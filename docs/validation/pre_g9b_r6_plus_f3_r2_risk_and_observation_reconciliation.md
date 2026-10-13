# PRE-G9B-R6-plus-F3-R2 — risk and observation reconciliation

- Task: `PRE-G9B-R6-plus-F3-R2-APPLY-STYLES-CORRECTION` (author authorization of
  2026-10-13)
- Corrective base: F3-R1 candidate `c4dffe60e5721bfeba21b2c59390286b5382d855` (frozen;
  its PHASE `verification-667d449952904a3d91ba14d7381e9a1c`, INTEGRATION
  `verification-9bfa9937f43b429186a53c94048a9ce5` and STATIC
  `verification-db91f2428ebf4b25846bc200b2ea4d1e` receipts unchanged and not reused)
- Evidence: [root-cause report](pre_g9b_r6_plus_f3_r2_apply_styles_root_cause_report.md);
  [corrective candidate report](pre_g9b_r6_plus_f3_r2_corrective_candidate_report.md)
- `selfApproved = false`

## 1. Author smoke findings against the F3-R1 candidate

| Finding | Smoke | Root cause | Status on `c4dffe60` |
|---|---|---|---|
| `F3R2-T0-STALE-SELECTION` | "ineffective in one direction" | RC-T0-1: objects of a document closed by Open, Open Recent or New, left in the inherited selection, were taken as the scope | **BLOCKING — CONFIRMED** (runtime evidence, real files) |
| `F3R2-T0-CONTRACT-DISCREPANCY` | segment colour, point shape and circle not taken from the source objects | RC-T0-2: `P5` transfers the source's construction defaults; the author expected the styles of corresponding source objects | **BLOCKING FOR THE AUTHOR'S EXPECTATION — CONTRACT-CORRECT BEHAVIOUR — AUTHOR DECISION REQUIRED** |
| `F3R2-T0-MISLEADING-HELP` | — | RC-T0-3: short help "Apply the object styles of a GeoCeDG document" | CONFIRMED (non-blocking) |

The F3 T0 author smoke stays **FAIL** as historical evidence against F3-R1. The F3-R1
lifecycle smoke (PASS, author observation) remains valid for what it covered. The
F3-R1 automated receipts remain valid for what they checked; they did not cover a
selection made before Open, Open Recent or New.

## 2. Reconciliation against the F3-R2 candidate

| Finding | Status on the F3-R2 candidate | Evidence |
|---|---|---|
| `F3R2-T0-STALE-SELECTION` | **CORRECTED — TECHNICAL EVIDENCE** (author smoke pending) | `PreG9BR6PlusF3R2StylesTest.f3r2s1`, `f3r2s2` (fail on `c4dffe60`, pass on F3-R2); real-file probe |
| `F3R2-T0-CONTRACT-DISCREPANCY` | **OPEN — NOT CHANGED** (`P5` unchanged; stop condition FR-F3R2-6 for the per-object expectation) | root-cause report §3, §5; `f3r2m1` pins the approved semantics |
| `F3R2-T0-MISLEADING-HELP` | CORRECTED (short and long help, user guide EN/ES) | `apps/geocedg/application-profile.yml`; guides §4.2 |

The discrepancy is not converted into an accepted observation: it stays open until
the author decides between keeping `P5` and commissioning a new style contract.

## 3. Observations

- `OBS-F3R2-INHERITED-STALE-SELECTION`: the inherited selection keeps the objects of a
  closed document after Open, Open Recent and New (GeoCeDG and Classic). Apply Styles
  no longer depends on it; other consumers (Delete, copy, properties, style bar) were
  not assessed and are unchanged.
- `OBS-F3R2-PARTIAL-RESTYLE`: a runtime exception inside the restyle loop would be
  reported as "nothing changed" after earlier objects were restyled without an undo
  step; never observed; pre-existing; unchanged.
- `OBS-F3R2-SILENT-NO-OP`: an application that changes nothing shows no message
  (approved no-op behaviour); with the author's documents, Direction A leaves the
  circle unchanged without any indication.
- `OBS-F3R2-SHARED-UUID`: the two author documents carry the same document UUID; T0
  neither reads nor changes it.
- The F3 observations `OBS-F3-PROTECTED-LABELS`, `OBS-F3-TEXT-THICKNESS` and the F3-R1
  observations are unchanged.

# PRE-G9B-R6-plus-F3-R2 — Apply Styles correction: corrective technical candidate report

```text
TASK                      = PRE-G9B-R6-plus-F3-R2-APPLY-STYLES-CORRECTION
AUTHORIZATION             = AUTHOR AUTHORIZED — BOUNDED CORRECTIVE IMPLEMENTATION OF T0
                            (2026-10-13)
CORRECTIVE BASE           = c4dffe60e5721bfeba21b2c59390286b5382d855
                            tree 8c9dd493db2bcd50ae3679c8375277252f1c5424
                            parent 165471154cd16e8875994a21eb49fbc2f828216b
                            F3-R1 CANDIDATE — FROZEN — HISTORICAL EVIDENCE; its PHASE
                            verification-667d449952904a3d91ba14d7381e9a1c, INTEGRATION
                            verification-9bfa9937f43b429186a53c94048a9ce5 and STATIC
                            verification-db91f2428ebf4b25846bc200b2ea4d1e stay bound to it
                            and do not certify F3-R2
F3 ORIGINAL CANDIDATE     = 165471154cd16e8875994a21eb49fbc2f828216b — FROZEN — HISTORICAL
F3 T0 AUTHOR SMOKE        = FAIL — HISTORICAL, BLOCKING FOR F3-R1 CLOSEOUT
F3-R1 LIFECYCLE SMOKE     = PASS — AUTHOR OBSERVATION (unchanged)
BRANCH                    = phase/pre-g9b-r6-plus-f3-r2 (local, from c4dffe60)
VERIFICATION_CLASS        = INTEGRATED_PHASE (phase PRE-G9B-R6-PLUS-F3; selection
                            pre-g9b-r6-plus-f3.desktop extended additively)
TECHNICAL CANDIDATE       = the commit that contains this report (frozen); its identity and
                            the acceptance runs are reported outside this file
P5_CONTRACT               = UNCHANGED; DISCREPANCY BETWEEN P5 AND THE AUTHOR'S EXPECTATION
                            REQUIRES AN AUTHOR DECISION (F3R2-T0-CONTRACT-DISCREPANCY)
AUTHOR_SMOKE (F3-R2)      = PENDING
PRODUCT CLOSEOUT          = NOT AUTHORIZED; DEBT-R6PLUS-FILE-INSERT-SURFACE stays open
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false; authorApproved = false; passClaimed = false
```

Root causes: [root-cause report](pre_g9b_r6_plus_f3_r2_apply_styles_root_cause_report.md);
findings: [risk and observation reconciliation](pre_g9b_r6_plus_f3_r2_risk_and_observation_reconciliation.md);
machine-readable mirror:
[`pre-g9b-r6-plus-f3-r2-corrective-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-r2-corrective-candidate-evidence.json);
F3-R1 report (unchanged): [F3-R1 corrective candidate report](pre_g9b_r6_plus_f3_r1_corrective_candidate_report.md);
specification: [`document-insertion.md`](../../geocedg/specs/ui/document-insertion.md) v1.2
(`PROPOSED`).

## 1. Entry gate (2026-10-13)

| Check | Result |
|---|---|
| HEAD, tree, parent | `c4dffe60…`, `8c9dd493…`, `16547115…` |
| linear ancestry from the F3 design closeout | `108d9cb6` → `bc1c9ac6` → `16547115` → `c4dffe60` (3 commits, no merges) |
| `main`, `origin/main`, live remote `main` | `e70718fe` |
| worktree | clean |
| F3-R1 receipts | PHASE, INTEGRATION and STATIC `ACCEPTED / COMPLETE`, bound to `c4dffe60`/`8c9dd493` |
| F3-R1 implementation and corrective reports, approved `P5` | present ([normative amendment proposals](pre_g9b_r6_plus_f3_normative_amendment_proposals.md) `P5`; [author design closeout record](pre_g9b_r6_plus_f3_author_design_closeout_record.md) Q5) |
| F3 phase selection and inventory | `pre-g9b-r6-plus-f3.desktop`, 467 identities |
| FR-F3-1 to FR-F3-8 | binding |
| changes after the F3-R1 candidate | none |
| result | **PASS** |

## 2. Product changes

| Path | Change |
|---|---|
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGStyleApplication.java` | the scope keeps a selected object only while the drawing's kernel resolves its label to that object (`scope`); with none left, every object; nothing else changed |
| `apps/geocedg/application-profile.yml` | Apply Styles short and long help (EN/ES): "default object styles"; the styles that document gives to new objects, not those of its existing objects |
| `docs/user/geocedg_user_guide_en.md`, `…_es.md` | the same clarification in §4.2 |

No change to `source/shared/**`, inherited GeoGebra classes (the selection manager and
the Open, Open Recent and New routes keep their behaviour), Classic, the six properties,
the exclusions, the insertion R, L1, M1, D1, Undo/recovery, isolation or serialization.

## 3. Behaviour before and after (author documents, product window)

| Case | F3-R1 | F3-R2 |
|---|---|---|
| Direction A, nothing or `f`, `B`, `c` selected | 5 / 2 objects restyled to the source defaults | unchanged (correct under `P5`) |
| Direction B, fresh selection state | 4 objects restyled | unchanged |
| Direction B after selecting in the closed document, then Open / Open Recent / New | success reported, **0 objects restyled**, nothing visible | **4 objects restyled**, one undo step |
| detached objects differing from the source defaults | restyled invisibly; undo step stored, document unsaved | never restyled; no undo step; saved state kept |
| segment colour red, point cross, circle blue/thin from the source **objects** | not transferred | not transferred: outside `P5` — author decision |

## 4. Tests

`PreG9BR6PlusF3R2StylesTest` (5 tests). Fixtures rebuild the author's construction by
commands (the user documents are not committed); the style source has non-factory
construction defaults for points (dark green, size 7, cross), segments (red,
thickness 2, long dashes) and conics (blue, thickness 2, dotted, fill opacity 0.25), and
magenta objects of thickness 9 that `P5` must not read.

| Requirement | Test | On `c4dffe60` | F3-R2 |
|---|---|---|---|
| T0-R2-01 segment colour; -02 thickness; -03 point colour; -04 point shape; -05 circle colour; -06 circle line properties and fill opacity; -07 default → non-default; -08 non-default → default; §7.2 every direction; persistence; source files unchanged; geometry, definitions, layers, UUID, units unchanged | `f3r2m1…` | pass | pass |
| T0-R2-09 mixed selection, unselected unchanged, excluded Locus V2 unchanged; T0-R2-10 Undo and Redo of every property | `f3r2m2…` | pass | pass |
| repaint (pixels of the new colours, removed by Undo, without a forced drawable update); repeated application is a no-op with no second undo step | `f3r2r1…` | pass | pass |
| RC-T0-1: selection left by Open, Open Recent and New is not a selection; every object restyled; the detached object untouched | `f3r2s1…` | **FAIL** (`expected: <6> but was: <1>`) | pass |
| RC-T0-1: no invisible undo step or unsaved state from detached objects; a live object selected next to a detached one is the only one restyled | `f3r2s2…` | **FAIL** (`expected: <0> but was: <1>`) | pass |
| no auxiliary window left | every application in the five tests (window count) and `f3r1l3` | pass | pass |
| single-object selection; invalid source; property whitelist | `f3t02`, `f3t03`, `f3t01` (F3, unchanged) | pass | pass |

"On `c4dffe60`" is the run of the new class added to a `git archive` of the corrective
base, without product change. Under `P5` the correspondence is the default type; every
admitted type has a construction default in every native document, so "missing
correspondence" reduces to the excluded types (`f3r2m2`, `f3t01`).

## 5. Stop conditions

| Condition | Result | Evidence |
|---|---|---|
| FR-F3-1 … FR-F3-8 | retained; no accounting, history, snapshot, isolation, EDT, identity or compatibility code changed; Apply Styles still stores one undo step only when something changed | full F3 selection |
| FR-F3R2-1 source-value integrity | PASS — the helper's defaults equal the source's `geogebra_defaults2d.xml` | real-file probe; `f3r2m1` |
| FR-F3R2-2 correspondence integrity | PASS under `P5` (by default type; unique) | root-cause report §2 |
| FR-F3R2-3 complete approved transfer | FAIL on `c4dffe60` (success claimed, nothing transferred: RC-T0-1); PASS on F3-R2 | `f3r2s1`, `f3r2s2` |
| FR-F3R2-4 geometry and identity | PASS | `f3r2m1`, `f3r2m2` |
| FR-F3R2-5 compatibility and regression | PASS (automated); no shared or kernel change | §6 |
| FR-F3R2-6 contract discrepancy | **TRIGGERED for the per-object expectation**: not implemented; `P5` unchanged; author decision required (§8) | root-cause report §5 RC-T0-2 |

## 6. Verification

Development (not acceptance): the new class on the corrective base and on F3-R2 (§4);
Checkstyle without findings in the changed files; official producers
(`artifacts/agent/pre-g9b-r6-plus-f3-r2-inventory/`) refreshed the inventory through the
official updater. The frozen PHASE, INTEGRATION and STATIC runs of this commit are
reported outside this artifact; the F3 and F3-R1 receipts are not reused.

## 7. Author smoke checklist (F3-R2, not performed by the agent)

Start with `.\gradlew.bat :desktop:desktop:runGeoCeDG` and use copies of the documents.
Under `P5` the expected values are the **default styles** of the chosen document; the
two author documents have the factory defaults (free points (77,77,255) size 5 dot;
segments and circles black, thickness 5, full line, no fill).

1. Open `EstilosDestino.cedg`; select the segment `f`, the point `B` and the circle `c`.
2. File → Insert → Apply Styles… → `EstilosOrigen.cedg`.
3. Segment: black, thickness 5 (was 8), full line. Point `B`: colour (77,77,255), dot,
   size 5. Circle: unchanged (it already has the default style). `A`, `C`, `D`
   unchanged. The red segment and cross point of EstilosOrigen are object styles and
   are not applied (open decision, §8).
4. Edit → Undo: every change disappears; Redo: they return.
5. Save as a disposable copy, reopen: the styles persist.
6. Reverse: open `EstilosOrigen.cedg` (select objects in the previous document first,
   to exercise RC-T0-1), select nothing, Apply Styles from `EstilosDestino.cedg`: `A`
   and `B` become (77,77,255) dots, `c` black thickness 5, `f` black thickness 5; `C`
   and `D` unchanged; one Undo restores everything.
7. Optional, non-factory defaults: Apply Styles from
   `artifacts/pre-g9b-r6-plus-f3-r2/smoke/EstilosPredeterminadosPrueba.cedg`
   (generated, not committed): points dark green crosses size 7, segments red
   thickness 2 long dashes, circles blue thickness 2 dotted with light fill.
8. Negative: select a Locus V2 result or a native dimension with other objects: it
   keeps its style.
9. New, Open, Open Recent and closing the application behave as after F3-R1.

## 8. Author decision required

`F3R2-T0-CONTRACT-DISCREPANCY`: the smoke expected Apply Styles to copy the styles of
the corresponding objects of the source document (segment `f` → segment `f`); approved
`P5` copies the source's construction defaults by object type. One of:

- **Keep `P5`** (as clarified in the help and guide): review and smoke this candidate
  against `P5`; a style source is then a document whose default styles were set.
- **Replace or extend `P5`** with a per-object style contract: a new author-approved
  design (correspondence rule — label, type, definition —, ambiguous and unmatched
  objects, whether defaults remain the fallback, exclusions, undo), then a new
  implementation task. This candidate's scope correction would remain needed.

# PRE-G9B-R6-plus-F4 — risk and debt register (preparation)

- Activity: `PRE-G9B-R6-plus-F4-PREP` (author authorization of 2026-10-13), on
  `847bb20a593e8092eabf80bd9d7c62ed7c21de08`
- Status: **PREPARATION REGISTER** — proposed dispositions; no author decision, no
  product verification
- Evidence: [characterization report](pre_g9b_r6_plus_f4_preparation_characterization_report.md);
  decisions: [author-decision preparation](pre_g9b_r6_plus_f4_author_decision_preparation_record.md)
- `selfApproved = false`

## 1. Planning-intent items

| Item | State before | Evidence now | Proposed disposition | Owner |
|---|---|---|---|---|
| `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` (F4-A) | ENHANCEMENT — ACCEPTED NON-BLOCKING — IMPLEMENTATION NOT AUTHORIZED | host, component and seams characterized; keyboard focus in the popup `NOT ESTABLISHED` | **INCLUDE — IMPLEMENTATION CANDIDATE** (`DQ-F4-1`–`-4`) | F4 |
| DXF compatibility recheck with AutoCAD 2026 (F4-B) | planning intent; `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` resolved by E3-R1 for AutoCAD 2026 | AutoCAD 2026 re-check on `847bb20a`: eight G5 families, both routes, AUDIT clean | **CLOSE — ALREADY RESOLVED** (AutoCAD 2026); other releases `NOT RECHECKED — COVERAGE GAP`; optional author **VALIDATION ONLY** (`DQ-F4-6`) | closed by F4 closeout if approved |
| `TD-PDF-1` / `OBS-POST-E2-P1-R1-PDF-TEXT-SIZE` (F4-C) | OPEN — CHARACTERIZATION REQUIRED — NOT AUTHORIZED | characterized: S3 world-scaled texts (model units), view-pixel labels; no approved contract violated | **DEFER — NOT IN F4**; reclassify `CHARACTERIZED — CONTRACT DESIGN REQUIRED — NOT AUTHORIZED` (`DQ-F4-8`) | contract-design task to be named by the author; otherwise G register |
| `TD-PDF-2` / `OBS-POST-E2-P1-R1-PDF-EMBEDDED-FONTS-FAILS` (F4-D) | OPEN — UNCONFIRMED IN PRODUCT — HEADLESS TEST ENVIRONMENT OBSERVATION — NOT AUTHORIZED | `PRODUCT DEFECT CONFIRMED` (windowed JVM; GeoCeDG and Classic); root cause upstream `c4fd03ce`; one-line correction feasible in scratch | **INCLUDE — IMPLEMENTATION CANDIDATE** (`DQ-F4-5`); reclassify `CONFIRMED — INHERITED PRODUCT DEFECT — OWNED BY F4` (`DQ-F4-9`) | F4 |

## 2. Implementation risks (if F4 is authorized as recommended)

| Risk | Workstream | Mitigation / gate | Stop |
|---|---|---|---|
| `F4R-POPUP-FOCUS`: the non-focusable popup blocks typing or closes on focus | F4-A | `FG-F4A-1` in the real window first; fallback F-1 only (`DQ-F4-3`) | yes, beyond F-1 |
| `F4R-LINETYPE-COUPLING`: an entered thickness also re-applies the selected line type (inherited `applyLineStyle`) | F4-A | accepted and documented (`DQ-F4-4`); tests assert parity with the slider | no |
| `F4R-DEFAULTS-UNDO`: in a creation mode the style bar edits construction defaults outside the undo history (inherited) | F4-A | documented limitation, unchanged | no |
| `F4R-CLASSIC-LEAK`: the style-bar hook or the popup change reaches Classic | F4-A | `forProduct` gate; Classic test asserting no field and identical popup | yes |
| `F4R-EMBED-FIDELITY`: embedded Type 1 glyphs differ visually (spacing, hinting) or some reader rejects them | F4-D | PDF structure checks, Ghostscript render, author smoke in a PDF reader | report; author disposition |
| `F4R-SHAPES-UNCHANGED`: the correction alters a currently successful export | F4-D | byte-identical fingerprints of text-as-shapes PDF, SVG, EMF, PNG (the corrected method is unreachable on successful paths today) | yes |
| `F4R-FAILED-EXPORT-REPORTING`: other font failures after the correction leave an empty file or a generic message | F4-D | tests of failure reporting; no claim of success | report |

## 3. Debts and observations touched

| Id | Proposed state | Note |
|---|---|---|
| `DEBT-R6PLUS-FILE-INSERT-SURFACE` and `DEFER-F3-*` | unchanged | F3 debts are not moved into F4 |
| `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` | unchanged (resolved for AutoCAD 2026; other versions not rechecked) | not reopened |
| `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET` | unchanged | outside F4 |
| `OBS-F4P-FREEHEP-XREF-SIZE` | **new — REGISTERED — NOT AUTHORIZED** | Ghostscript repairs "incorrect xref size" in every FreeHEP PDF, text-as-shapes included; pre-existing |
| `OBS-F4P-S3-SPEC-HEADER` | **new — REGISTERED** | S3 specification header still says candidate pending review |
| `OBS-F4P-ADR0035-CONSEQUENCE-LINE` | **new — REGISTERED** | ADR 0035 Consequences line superseded by its amendment |

## 4. Validation obligations of a future implementation

- F4-A: real-window gate `FG-F4A-1`; field present only in GeoCeDG; value follows the
  slider, selection, creation-mode defaults and polygon minimum; accepted entries apply
  through the slider with one undo step; refusals; Undo/Redo; save and reopen; Classic
  without field; Properties field unchanged; author smoke.
- F4-D: embedded-font PDF on the physical and legacy routes and in Classic, with an
  embedded font program and correct text; failure reporting; byte identity of every
  currently successful output; physical stroke and marker sizes unchanged; author smoke
  in a PDF reader.
- Both: upstream boundary registration, Checkstyle, the registered phase selection,
  `INTEGRATION`.

## 5. F4-D implementation updates (2026-10-14)

Recorded by the [F4-D implementation stop report](pre_g9b_r6_plus_f4_d_implementation_stop_report.md)
and [scope amendment 01](pre_g9b_r6_plus_f4_d_scope_amendment_01_record.md); the sections
above stay as the preparation record.

| Id | State | Relation and evidence | Owner |
|---|---|---|---|
| `TD-PDF-2` | `CONFIRMED — INHERITED PRODUCT DEFECT — OWNED BY F4-D`; **OPEN** until the corrected technical candidate passes the required verification and the author accepts it | `AbstractCharTable.toName(char)` self-call from upstream `c4fd03ce`; reproduced on the base in all four routes | F4-D |
| `TD-PDF-3` | **new — CONFIRMED — INHERITED PRODUCT DEFECT — OWNED BY F4-D (scope amendment 01)**; **OPEN** under the same condition | the inherited FreeHEP `CharstringEncoder.move()` wrote no moveto for a zero displacement, so 43 of 230 embedded Type 1 glyphs had no subpath start (Adobe Type 1 Font Format 1.1, §6.4) and rendered with a slanted left stem in Ghostscript and Poppler; unchanged since the import; **latent behind `TD-PDF-2`**: no embedded-font PDF could be written while `TD-PDF-2` held, so it surfaced only once `TD-PDF-2` was corrected | F4-D |
| `F4R-EMBED-FIDELITY` (§2) | realized as `TD-PDF-3` | the risk anticipated here materialized in the glyph outlines, not in spacing or reader rejection | F4-D |
| `OBS-F4P-FREEHEP-XREF-SIZE` | unchanged — independent observation, not in F4-D | Ghostscript reports it on every FreeHEP PDF, embedded and text as shapes alike | — |

## 6. F4-D author closeout (2026-10-14)

Recorded by the [F4-D author closeout record](pre_g9b_r6_plus_f4_d_author_closeout_record.md):
F4-D `PASS — AUTHOR APPROVED`, `AUTHOR_SMOKE = PASS`, on the approved implementation
`6214d15a0bc986cf1d16f178c67fd76bf6e54bc2` (tree `47b634b1885a9b5650af73369caa97a955bf22f2`).
Sections 1–5 stay as the historical record.

| Id | State | Resolution reference | Owner |
|---|---|---|---|
| `TD-PDF-2` | **CLOSED — RESOLVED BY F4-D** | `AbstractCharTable.java` blob `52c0ff23` in `6214d15a`; evidence of §5 and the candidate report | — |
| `TD-PDF-3` | **CLOSED — RESOLVED BY F4-D SCOPE AMENDMENT 01**; relation to `TD-PDF-2` kept (latent behind it) | `CharstringEncoder.java` blob `e6d6c6f7` in `6214d15a`; `PreG9BR6PlusF4DType1OutlineTest`; external evidence | — |
| `F4R-EMBED-FIDELITY` | **CLOSED** — realized as `TD-PDF-3`, resolved with it | as `TD-PDF-3` | — |
| `F4R-SHAPES-UNCHANGED` | **CLOSED — HELD** | obligations 6–8 | — |
| `F4R-FAILED-EXPORT-REPORTING` | **CLOSED FOR F4-D — NOT REALIZED**; the inherited generic failure reporting is unchanged and outside the approved obligations; no debt opened | obligations 1–2 | — |
| `OBS-F4P-FREEHEP-XREF-SIZE` | **OPEN — UNRESOLVED — INDEPENDENT OBSERVATION**; not caused or changed by F4-D; proposed for the G structured register; not authorized | candidate report §6 | G register (proposed) |
| F4-A risks of §2 | unchanged; F4-A not authorized, after F4-D publication (`DQ-F4-10`) | — | F4-A |

## 7. F4-A author closeout (2026-10-14)

Recorded by the [F4-A author closeout record](pre_g9b_r6_plus_f4_a_author_closeout_record.md):
F4-A `PASS — AUTHOR APPROVED`, `AUTHOR_SMOKE = PASS`, on the approved implementation
`6baa66a584ceef1973c854fb63b00aa7070708b4` (tree `bfc22c5dff6a1dcb7a371030c77f695a1a6b1925`),
after the [focus gate stop](pre_g9b_r6_plus_f4_a_focus_gate_stop_report.md) and
[scope amendment 01](pre_g9b_r6_plus_f4_a_scope_amendment_01_record.md). Sections 1–6 stay
as the historical record.

| Id | State | Evidence / reference |
|---|---|---|
| `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` | **CLOSED — FULFILLED BY F4-A** (`6baa66a5`), with F4-A-L1 | candidate report; real-window gate `F4-A E5 FOCUS FEASIBILITY = PASS` |
| `F4R-POPUP-FOCUS` | **CLOSED** — realized at the first gate (STOP `e7b235dc`), resolved by scope amendment 01 (E5) | stop report; e5 gate evidence |
| `F4R-LINETYPE-COUPLING` | retained, accepted (`DQ-F4-4`), inherited and unchanged | candidate report §9 |
| `F4R-DEFAULTS-UNDO` | retained, documented, inherited and unchanged | candidate report §9 |
| `F4R-CLASSIC-LEAK` | **CLOSED — HELD** | `classicStyleBarsHaveNoField`; gate item 13 |
| **F4-A-L1** restricted numeric-entry availability | **ACCEPTED LIMITATION** — editor disabled when another application-owned window overlaps the popup and Swing selects a heavy-weight popup; accepted only while it is visibly disabled, takes no keyboard input, the table and slider work, and editing resumes with a light-weight popup | gate `owned.overlap.path`, `owned.overlap.after.path` |
| **F4-A-L2** popup redraw | **ACCEPTED LIMITATION** — one redraw when opening after a visible tooltip; popup usable, no duplicated operation | gate `tooltip.hover.path`; `f4a-e5-diag-*` |
| **F4-A-L3** validated runtime | **ACCEPTED LIMITATION** — validated with Temurin 25.0.4 and FlatLaf 3.7; a relevant runtime or look-and-feel change requires renewed compatibility verification | e5 gate evidence |
| `TD-PDF-1` (F4-C) | DEFERRED, unchanged | §1 |
| `OBS-F4P-FREEHEP-XREF-SIZE` | OPEN — independent observation, unchanged | §6 |

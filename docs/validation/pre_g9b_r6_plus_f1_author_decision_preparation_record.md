# PRE-G9B-R6-plus-F1 — author-decision preparation record

- Activity: `PRE-G9B-R6-plus-F1-PREP` (author instruction of 2026-10-11)
- Status: **QUESTIONS PREPARED — ALL PENDING — NO DECISION RECORDED**
- Base: published `main` `bde2827bb21e4a28e080db6a65b4abc892071cf8`, tree
  `1b74102cea5151b8a6c571cb36b96b33af6fb3c8`
- Evidence: [characterization report](pre_g9b_r6_plus_f1_preparation_characterization_report.md)
  (`K0`–`K17`); design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f1_orientation_cue_reconciled_design_candidate.md)
- Prior author decisions on F1: **none**. `AQ-F1` (mini-track plan §13, `P0`
  report) is an open question whose P0 recommendation (global, screen-only) is
  not an author decision.

This record formulates questions and recommendations. It decides nothing. A
recommendation is the agent's proposal; only an explicit author instruction
disposes of a question. `selfApproved = false`.

Each entry gives: question, alternatives, evidence, recommendation,
consequences, affected tests (IDs of the design candidate §10).

## DQ-F1-1 — Presentation scope

**Question.** Is the cue a global option (every eligible object, one switch) or
a per-object presentation attribute?

| Alternative | Consequence |
|---|---|
| **G — global (recommended; P0 recommendation)** | one `USER_PREFERENCE` (or `SESSION`, `DQ-F1-6`); documents unchanged; `BOUNDED_PHASE` possible |
| O — per object | a new style attribute in the document XML and in undo snapshots; reader and writer changes; older GeoCeDG and Classic drop or reject it; `GLOBAL_IMPACT`; outside this preparation's envelope |
| G+O — global switch plus per-object opt-out | as O |

Evidence: report §K13 (store, key convention), §K16; `AGENTS.md` §17.
Affected tests: `T-F1-PREFERENCE`, `T-F1-NO-MUTATION`.

## DQ-F1-2 — View coverage

**Question.** Graphics View 1 only, or Graphics Views 1 and 2?

| Alternative | Consequence |
|---|---|
| **V1 — GV1 only (recommended)** | seam S1 alone; GV2 is outside both GeoCeDG perspectives (report §K10) |
| V12a — GV1 + GV2 through a GeoCeDG GV2 view class (S2) | `GuiManagerGeoCeDG` overrides `GuiManager3D.newEuclidianView(…, viewId)`; GV2 changes class (from `EuclidianViewFor3DD`) and needs its own regression (3D-for-2D behavior, controller, export and print of GV2); still Desktop-only |
| V12b — every 2D view through the drawables (S3) | shared upstream drawables; reaches exports (see `DQ-F1-3`); escalates |

Evidence: report §K8, §K10. Affected tests: `T-F1-VIEWS`, `T-F1-EXPORT`.

## DQ-F1-3 — Export participation

**Question.** Is the cue screen-only, or part of exports?

| Alternative | Consequence |
|---|---|
| **X0 — screen-only (recommended; P0 recommendation)** | S1 is screen-only by construction: no export, print, clipboard, CLI, LaTeX or DXF route calls `paint` (report §K8); test by byte identity |
| X1 — picture exports and print only | needs S3 or GeoCeDG hooks in `PictureExportService` and print; the cue then also appears in clipboard and CLI images; LaTeX and DXF unaffected unless separately designed |
| X2 — every export including LaTeX/DXF | per-format arrow semantics; exporter changes; well outside a bounded phase |

Evidence: report §K8 table; DQ-F1-2. Affected tests: `T-F1-EXPORT`.

## DQ-F1-4 — Visual design

**Question.** Arrowhead style, size, colour and positioning.

| Item | Proposal | Alternatives |
|---|---|---|
| shape | isosceles triangle, tip forward | open V (like `RotatedArrow`, segment decorations) |
| hollowness | 1 px outline, interior filled with the view background | outline only (the line shows inside) |
| size | `L = 10 px`, `W = 4 px` × view style scale, zoom-independent | proportional to line thickness (like `ARROW_OUTLINE`, report §K9) |
| colour | object colour and line opacity | fixed neutral colour; theme colour |
| segment anchor | screen midpoint, or midpoint of the visible chord | 2/3 point (avoids the label/decoration zone) |
| ray anchor | start + `3L` on the visible chord, else nearest point to the view centre | P0 "near the start" (undefined off screen) |
| line anchor | point of the visible chord nearest the view centre | P0 wording (can fall outside the view) |
| rendering helper | small GeoCeDG-owned Desktop path builder | public upstream `getArrow` (shared change, rejected for bounded) |

Evidence: report §K9, §K12; design §5–§6. Affected tests:
`T-F1-PLACEMENT`, `T-SMOKE`.

## DQ-F1-5 — Eligibility

**Question.** Which construction routes may display a cue, and what happens
for unknown or convention-dependent orientations?

Classes (report §K6): `O-CONSTRUCTIVE` (`Line/Segment/Ray(A,B)`, `Ray(A,v)`,
`Line(P,v)`, polygon edges, segment images), `O-INHERITED` (parallel,
perpendicular, perpendicular bisector, two-line angle bisectors, tangent
parallel to a line, affine images), `O-CONFIGURATION` (three-point angle
bisector; tangent from an external point — both discontinuous at degenerate
configurations), `O-REPRESENTATION` (independent and dependent equation lines,
tangent at a point of a conic, tangent to a function, parallel/perpendicular to
a function, asymptotes, polars), `O-UNKNOWN`.

| Alternative | Consequence |
|---|---|
| E-A — every defined line | truthful about the kernel; shows senses that depend only on how an equation was typed (`y = 2x+1` vs `2x − y + 1 = 0`, report §K4 R15/R15b) or on a conic's equation sign (R12d) |
| **E-B — constructive + inherited, fail-closed (recommended)** | an arrow appears only where reversing the user's inputs reverses it; unknown routes show nothing; affine images and macros inherit; sources of class representation keep their images excluded |
| E-C — E-B + configuration | adds the bisector and external tangents with their jumps (D12–D18) |
| E-D — E-B + parametric independent lines | relies on a user-changeable display form (report §K6 (b)) |

Sub-questions: (a) macros inherit the inner route (recommended yes);
(b) transforms inherit the source class (recommended yes, affine maps only;
projective `O-UNKNOWN`); (c) the perpendicular "+90°" rule counts as inherited
(recommended yes: deterministic kernel convention, reverses with its
reference). Evidence: report §K4–§K7. Affected tests: `T-F1-ELIGIBILITY`,
`T-F1-SENSE`, `T-F1-REVERSAL`, `T-F1-DISCONTINUITY`.

## DQ-F1-6 — Preference lifecycle

**Question.** State class, key, initial default, persistence, relation to the
portable GeoCeDG settings and behavior on New and Open.

| Item | Proposal | Alternatives |
|---|---|---|
| class | `USER_PREFERENCE` | `SESSION` (lost on restart; same as the existing view toggles, report §K13) |
| key | `geocedg.view.orientation-cue.v1` (P0 proposal, convention `geocedg.<area>.<name>.v1`) | another area name |
| values | `shown` / `hidden`; invalid → default; read never writes | `true` / `false` |
| default | **hidden** | shown |
| persistence | portable properties, written on window close or Save Settings | immediate write |
| New / Open / undo / redo | unaffected; never in document, macro or preferences XML | — |
| multiple windows | one value per process store; a change repaints every window | per window |
| Classic | never read | — |

Evidence: report §K13; `unit-system.md` §17.2. Affected tests:
`T-F1-PREFERENCE`, `T-F1-NO-MUTATION`, `T-F1-CLASSIC`.

## DQ-F1-7 — Visibility and overlap

**Question.** Suppression rules for hidden objects, degenerate geometry,
limited view extent and existing arrow decorations.

Proposed (design §4): no cue for objects without a drawable, not
`isEuclidianVisible`, not visible in the view, on a hidden layer, with
thickness 0, undefined, with non-finite coefficients or a zero direction, on a
logarithmic axis, or whose visible chord is shorter than `4L`; no cue on a
segment that carries a midpoint arrow decoration (`DECORATION_SEGMENT_ONE_ARROW`,
`TWO_ARROWS`, `THREE_ARROWS`); arrow ending styles do **not** suppress the cue;
labels may overlap (accepted). Auxiliary objects follow their normal drawing.

| Alternative | Consequence |
|---|---|
| **as proposed (recommended)** | no conflicting arrows at the same place; endings keep their user meaning at the ends |
| also suppress with arrow endings | no cue on decorated segments at all |
| never suppress for decorations | two arrows near the midpoint, possibly opposite |

Evidence: report §K7, §K9, §K11, §K12. Affected tests: `T-F1-VISIBILITY`,
`T-F1-DEGENERATE`, `T-F1-PLACEMENT`.

## DQ-F1-8 — Architecture and verification

**Question.** The smallest correct implementation layer and the verification
class.

| Alternative | Class |
|---|---|
| **S1 — GV1 `paint` overlay in `GeoCeDGEuclidianView` (recommended)** | `BOUNDED_PHASE`, one registered `PHASE` `PRE-G9B-R6-PLUS-F1` |
| S2 — S1 + GeoCeDG GV2 class | `BOUNDED_PHASE` with a wider Desktop regression, or `INTEGRATED_PHASE` if the author wants `INTEGRATION` coverage of GV2 |
| S3 — shared drawables | `INTEGRATED_PHASE` at least (shared upstream code, export reach, Classic gating) |
| S4 — kernel object/command | rejected |

No kernel change is needed: the sense is already shared semantics consumed
read-only (report §K3, `AGENTS.md` §4). Escalation triggers: export
participation, S3, per-object persistence, kernel change, new upstream
`AppD`/`OptionsLayoutD` hook. Affected tests: all; registration of the phase
selection.

Interaction with `DQ-F1-9`: a new profile action (U1, U3) also edits
declarative verification inputs (the profile allow-list of
`workspace-profile-validation.ps1` and its static-contract pin). No earlier
`BOUNDED_PHASE` with a catalog change was found (`E1-L` kept 125 actions). The
author decides whether those edits stay inside `BOUNDED_PHASE` (recommended:
yes, they are exercised by the registered `PHASE` through
`workspace-profile.product`) or move the class to `INTEGRATED_PHASE`. U2 alone
keeps the bounded envelope without them.

## DQ-F1-9 — UI surface and catalog

**Question.** Where does the user switch the cue?

| Alternative | Consequence |
|---|---|
| U1 — checkbox action in the View or Options menu | catalog 130 → 131: `GeoCeDGProfile` count, eleven test assertions in eight classes, `GeoCeDGActionRegistry` target, `workspace-profile-validation.ps1` approved ids and the static-contract re-pin (report §K14); discoverable like axes/grid |
| U2 — row in an existing GeoCeDG-owned Layout & Presentation panel | no catalog or upstream change; less discoverable |
| **U3 — both (recommended)** | U1's consequences; one preference behind both |
| U4 — new Layout & Presentation panel | upstream `AppD`/`OptionsLayoutD` hook and `modified-files.yml`; not recommended |

Evidence: report §K13, §K14. Affected tests: `T-F1-PROFILE`,
`T-F1-PREFERENCE`.

## Disposition table

| Id | Recommendation | Author disposition |
|---|---|---|
| `DQ-F1-1` | G (global) | PENDING |
| `DQ-F1-2` | V1 (GV1 only) | PENDING |
| `DQ-F1-3` | X0 (screen-only) | PENDING |
| `DQ-F1-4` | design §5–§6 values | PENDING |
| `DQ-F1-5` | E-B, sub-questions (a)–(c) yes | PENDING |
| `DQ-F1-6` | `USER_PREFERENCE`, `geocedg.view.orientation-cue.v1`, `shown`/`hidden`, default hidden | PENDING |
| `DQ-F1-7` | as proposed | PENDING |
| `DQ-F1-8` | S1, `BOUNDED_PHASE` | PENDING |
| `DQ-F1-9` | U3 | PENDING |

```text
AUTHOR_DECISION  = NOT_RECORDED_IN_THIS_ARTIFACT
F1               = PREPARED — NOT AUTHORIZED
selfApproved     = false
```

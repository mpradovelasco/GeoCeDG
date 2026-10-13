# PRE-G9B-R6-plus-F4-A — compact style-bar numeric thickness entry

**CANONICAL PROMPT — DESIGN AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED.** Prepared by
`PRE-G9B-R6-plus-F4-AUTHOR-DECISION-RECONCILIATION` (author instruction of 2026-10-13)
on `089fc2205300559c572c12f0cd32326abd0c2b5e`; successor, for this track, of the
superseded combined prompt
[`pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md`](pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md).
The design is `AUTHOR APPROVED` (F4 final author design approval of 2026-10-13 on
`83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9`; choices marked **[DQ-F4-n]** follow the
approved decisions of the
[F4 author-decision reconciliation](../../../docs/validation/pre_g9b_r6_plus_f4_author_decision_reconciliation.md)
as recorded in the
[F4 author design closeout record](../../../docs/validation/pre_g9b_r6_plus_f4_author_design_closeout_record.md)).
Nothing here authorizes implementation, and approval of track F4-D never authorizes
this track.

Evidence and design:
[F4 characterization report](../../../docs/validation/pre_g9b_r6_plus_f4_preparation_characterization_report.md) §4;
[two-track design successor](../../../docs/architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md) §2.

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Add the approved numeric line-thickness field of Object
Properties → Style (`GeoCeDGLineThicknessField`, POST-E2-P1-R2) to the line-style popup of
the GeoCeDG Graphics style bar, synchronized with its slider, on the same property, range
and validation, with one undo step per accepted entry
(`OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`). Proposed `VERIFICATION_CLASS =
BOUNDED_PHASE` (architecture A1) **[DQ-F4-7]**, frozen only by the implementation
authorization; under A2 the class must be reassessed (`INTEGRATED_PHASE` proposed).

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the F4-A implementation authorization (author
direction on **[DQ-F4-10]**: the published closeout of track F4-D; F4-A starts only after
F4-D is closed and published). Entry gate: exact identity; clean
worktree; local, tracking and live remote `main`; the F4 decisions taken for this track;
`EuclidianStyleBarD.java`, `PopupMenuButtonD.java`, `EuclidianViewD.java` and
`GuiManager3D.java` byte-identical to the GeoGebra baseline `9b93256b` (else stop and
reconcile).

**Feasibility gate (first step, before tests are written against it)** **[DQ-F4-3]**: in
the real GeoCeDG window (`GeoCeDG.main`, isolated settings), with the field attached, show
with native input (`java.awt.Robot`) that (1) the field receives keyboard focus, (2)
digits can be entered and edited, (3) Enter commits, Escape cancels and focus loss
commits or restores as the Properties field does, (4) the popup stays usable, (5) the
slider still works, (6) no keyboard accelerator fires while typing, (7) Classic is
unchanged. A synthetic dispatched-event probe alone does not pass the gate. The only
admissible fallback is a GeoCeDG-owned focusability of the popup that contains the field,
reached through the component ancestry while the field is attached. Otherwise:
`STOP — F4-A FOCUS FEASIBILITY BLOCKED` — do not change the architecture.

## Authority and evidence hierarchy

`AGENTS.md`; current code; the POST-E2-P1-R2 closeout (approved Properties field); the
F4 decisions; the evidence above.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Architecture **A1** **[DQ-F4-2]**: a new GeoCeDG subclass of `EuclidianStyleBarD` that,
after every `createButtons()`, attaches a fresh `GeoCeDGLineThicknessField` bound to
`btnLineStyle.getMySlider()` through `addPopupMenuItem`; `GeoCeDGEuclidianView`
overriding `newEuclidianStyleBar()`; a `GuiManagerGeoCeDG` override of
`newEuclidianView` returning an `EuclidianViewFor3DD` subclass that only overrides its
style-bar factory (Graphics 2; author direction on **[DQ-F4-2]**: Graphics 1 and
Graphics 2); a small generalization of
`GeoCeDGLineThicknessField` only if the style-bar context needs it (no behaviour change
for the Properties field); EN/ES text in `apps/geocedg/application-profile.yml`; the user
guide sentences (EN/ES); tests; the phase selection `pre-g9b-r6-plus-f4-a.desktop`,
inventory and pins through the official mechanisms; `docs/upstream/modified-files.yml`
entries for new test and source files; the candidate report and mirror; roadmap and
mini-track status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Any inherited source change (`EuclidianStyleBarD`, `PopupMenuButtonD`, `SliderUtil`,
`EuclidianStyleBarStatic`, `PropertiesPanelD`, `GuiManager3D`, `EuclidianViewD`)
unless the author authorizes A2; the thickness property, range, minimum, serialization or
`applyLineStyle` semantics; construction-default handling; the Properties field
behaviour; plane and 3D style bars; Classic; geometry, kernel, serialization; text sizes;
FreeHEP (track F4-D); DXF; F3 and earlier phases; G.

## Architectural placement

GeoCeDG Desktop presentation in the application layer (`AGENTS.md` §4): no semantic,
identity, persistence or DAG effect.

## Required design/specification

The two-track design successor §2 as decided by the author; the user guide documents the
field. No specification or ADR change.

## Geometric invariants and degeneracies

Not applicable: no coordinate, definition, dependency, label, layer or unit change.

## Compatibility and serialization

No serialized field or preference. Classic constructs neither the GeoCeDG views nor the
GeoCeDG GUI manager and is unchanged.

## Integration obligations

Register new source and test files; keep the upstream boundary green; update the guide
(EN/ES) and any profile pins.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused tests: the field exists once in the Graphics 1 and the Graphics 2 style-bar popup and is re-attached after a language and a font-size change; Classic's
popup has no field; the field follows the slider value, the selection, a creation mode's
defaults and the polygon minimum 0; refused entries (`""`, `abc`, `2.5`, `-1`, `0` where
the minimum is 1, `14`, `7x`) restore the value; an accepted entry equals moving the
slider (same targets including multi-selection, same line type) and stores exactly one
undo step; Undo, Redo, save and reopen; the Properties field and
`PostE2P1R2SmokeFollowUpTest` unchanged. Then Checkstyle, the upstream boundary, the
official `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4-A` on the frozen candidate
(plus `INTEGRATION` only if the frozen class requires it), `INFRA_UNIT` (the
phase-selection and inventory registration) and `STATIC`. The executable authority is the
active recovery contract of `verification-levels.md` with `verify.ps1` and
`verification-registry.json`; its archived §12.8 is design history only. Author smoke:
typing a thickness in the style-bar popup on Windows, Enter, Escape, focus loss, Undo,
Redo, save and reopen, in Graphics 1 and Graphics 2.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation; the F4 design approval of 2026-10-13
approved `DQ-F4-2`, `-3`, `-4`, `-7` and `-10` but authorized no implementation. A
separate author instruction, after the F4-D publication, must name the exact base commit
and tree and freeze the class. Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary. Publication only through a separate author
closeout naming the exact accepted commit.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review; the candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).

## Required artifacts

Gate evidence; candidate report and mirror; PHASE (and, if required, INTEGRATION) and
STATIC receipts; author smoke checklist; roadmap and mini-track status.

## Stop conditions

- `STOP — F4-A FOCUS FEASIBILITY BLOCKED` (gate fails and the fallback does not work).
- An inherited change appears necessary (A2 not authorized).
- Classic, the Properties field, the slider or the line-type table would change.
- Base identity, worktree, remote divergence, incomplete or untrusted coverage, or a
  missing author decision.

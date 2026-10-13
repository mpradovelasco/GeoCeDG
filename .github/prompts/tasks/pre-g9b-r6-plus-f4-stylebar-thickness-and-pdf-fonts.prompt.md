# PRE-G9B-R6-plus-F4 — compact style-bar thickness entry and embedded-font PDF correction

**SUPERSEDED — PREPARATION EVIDENCE — NOT EXECUTABLE.** Superseded on 2026-10-13 by
`PRE-G9B-R6-plus-F4-AUTHOR-DECISION-RECONCILIATION`, which splits F4 into two
independently authorizable tracks:
[F4-A](pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md) and
[F4-D](pre-g9b-r6-plus-f4d-freehep-embedded-fonts.prompt.md)
([reconciliation](../../../docs/validation/pre_g9b_r6_plus_f4_author_decision_reconciliation.md)).
The text below is kept unchanged as the preparation record.

**CANONICAL PROMPT — PREPARED — NOT AUTHORIZED.** Prepared by `PRE-G9B-R6-plus-F4-PREP`
(author authorization of 2026-10-13) on the published F3 closeout
`847bb20a593e8092eabf80bd9d7c62ed7c21de08`. The design it implements is
`PROPOSED — PENDING AUTHOR REVIEW`; every choice marked **[DQ-F4-n]** depends on a
pending author decision of the
[F4 author-decision preparation](../../../docs/validation/pre_g9b_r6_plus_f4_author_decision_preparation_record.md).
Where a decision is still open, this prompt does not select an option: the
implementation authorization must state the chosen option, or the work stops.

Governing evidence and design:
[F4 preparation and characterization report](../../../docs/validation/pre_g9b_r6_plus_f4_preparation_characterization_report.md),
[F4 reconciled design candidate](../../../docs/architecture/pre_g9b_r6_plus_f4_reconciled_design_candidate.md),
[F4 risk and debt register](../../../docs/validation/pre_g9b_r6_plus_f4_risk_and_debt_register.md).

<!-- geocedg-field: objective -->
## Objective

`CHANGE_ROUTE = ORDINARY`. Implement, as one verifiable candidate pending author
review, the parts of F4 that the author includes **[DQ-F4-1]**:

- **F4-A** — the numeric line-thickness field of Object Properties → Style
  (`GeoCeDGLineThicknessField`, POST-E2-P1-R2) also in the compact line-style popup of
  the Graphics style bar, synchronized with its slider, on the same property and range,
  with one undo step per accepted entry (`OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`);
- **F4-D** — the embedded-font PDF route (`TD-PDF-2`): `AbstractCharTable.toName(char)`
  recurses forever since upstream `c4fd03ce`; restore the delegation to
  `toName(Character.valueOf(c))` **[DQ-F4-5]**.

F4-B (DXF/AutoCAD) and F4-C (`TD-PDF-1`) carry no product work in this prompt
**[DQ-F4-6, DQ-F4-8]**. `VERIFICATION_CLASS` is proposed as `INTEGRATED_PHASE` (registered
phase `PRE-G9B-R6-PLUS-F4` + `INTEGRATION`) **[DQ-F4-7]** and is frozen only by the
implementation authorization.

<!-- geocedg-field: implementation_base -->
## Implementation base

The exact commit and tree named by the future implementation authorization; never a
moving branch. Entry gate before product code: exact base identity; clean worktree;
local, tracking and live remote `main`; the F4 author decisions recorded for the scope
actually authorized; the F3 baseline unchanged; `AbstractCharTable.java`,
`EuclidianStyleBarD.java` and `PopupMenuButtonD.java` still byte-identical to the
GeoGebra baseline `9b93256b` (otherwise stop and reconcile).

Technical gate `FG-F4A-1` (first step of F4-A, before any test is written against
it): in the real GeoCeDG window (`GeoCeDG.main`, isolated settings), add the field to
the line-style popup and establish that it receives keyboard focus, typing and Enter
without closing the popup. If not, apply only the fallback admitted by **[DQ-F4-3]** or
stop.

## Authority and evidence hierarchy

`AGENTS.md`; current code and build; `geocedg/specs/export/physical-pdf-style-sizes.md`
and ADR 0035 (unchanged); `geocedg/specs/ui/construction-text-view-scaling.md`
(unchanged); `docs/validation/post_e2_p1_r2_closeout_record.md` (the approved Properties
field); the F4 author decisions; the F4 evidence above.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

- F4-A: `GeoCeDGLineThicknessField` reuse (generalize only its name or tooltip key if
  the style-bar context requires it); one profile-gated host call in the inherited
  `EuclidianStyleBarD` **[DQ-F4-2 = A-1]**, or a GeoCeDG style-bar subclass installed by
  the Graphics 1 and Graphics 2 factories **[DQ-F4-2 = A-2]**; the fallback of
  **[DQ-F4-3]** in `PopupMenuButtonD` if needed; EN/ES text in
  `apps/geocedg/application-profile.yml`; the user guide sentence for the style bar.
- F4-D: the single line of `org/freehep/graphics2d/font/AbstractCharTable.java`.
- Tests, registration of a phase selection `pre-g9b-r6-plus-f4.desktop`, inventory and
  pins through the official mechanisms, `docs/upstream/modified-files.yml` entries, the
  F4 candidate report and its JSON mirror, roadmap and mini-track status.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

Shared kernel, geometry, serialization format, `GeoElement` thickness semantics or
range; the inherited `applyLineStyle` coupling (line type applied with the thickness);
construction-default handling; the Properties field behaviour; any text size, label
offset or `DrawText` change (`TD-PDF-1` stays outside); physical stroke and marker
sizes; any other FreeHEP change (including the xref observation
`OBS-F4P-FREEHEP-XREF-SIZE`); the picture dialog options; DXF, SVG, EMF, PNG, LaTeX
exporters; Classic behaviour other than the inherent correction of its embedded-font
PDF; F3 and earlier phases; G.

## Architectural placement

F4-A is Desktop presentation in the GeoCeDG application layer, attached to the
inherited style bar through a profile gate (`forProduct`); F4-D is inherited vendored
export code in the upstream Desktop tree. Neither needs kernel semantics, identity,
persistence or DAG integration (`AGENTS.md` §4).

## Required design/specification

The F4 reconciled design candidate as decided by the author. No specification change
is required for F4-D; F4-A extends the POST-E2-P1-R2 field description in the user
guide only. No ADR.

## Geometric invariants and degeneracies

Not applicable to geometry: no coordinate, definition, dependency, label, layer or unit
changes. Line thickness keeps its existing domain (integer, per-type minimum, 13 as the
UI maximum).

## Compatibility and serialization

No serialized field, preference or format change. Classic shows no field and its
popup is unchanged. Every export that succeeds today stays byte-identical; the corrected
method is reachable only on paths that today always end in `StackOverflowError`.

## Integration obligations

Register the inherited modifications in `docs/upstream/modified-files.yml`; keep the
upstream boundary check green; register new test classes and the phase selection;
update the user guide (EN/ES) for F4-A.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focused tests (names illustrative): the field is present in the GeoCeDG style-bar
popup only once and absent in Classic; it follows the slider value, the selection, a
creation mode's defaults and the polygon minimum 0; refused entries (`""`, `abc`, `2.5`,
`-1`, `0` where the minimum is 1, `14`, `7x`) restore the value; an accepted entry
equals moving the slider (same targets, same line type) and stores exactly one undo
step; Undo, Redo, save and reopen; the Properties field unchanged. Embedded-font PDF on
the physical route, the legacy route and Classic: no exception, a non-empty file, an
embedded font program, the text drawn; text-as-shapes PDF, SVG, EMF and PNG byte-identical
to the base; physical stroke and marker sizes unchanged; a failing export is never
reported as success. Then Checkstyle, the upstream boundary, the official
`verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4` and `-Profile INTEGRATION` on the
frozen candidate (class as frozen by the authorization), and `STATIC`. Author smoke:
keyboard entry in the style-bar popup (Windows), and an embedded-font PDF opened in a
PDF reader.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file authorizes implementation. A separate author instruction must name
the exact base, the decisions taken on `DQ-F4-1` to `DQ-F4-9`, and the frozen class.
Technical verification never implies author approval.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

No push, merge, tag, release or binary is authorized. Publication requires a separate
author closeout naming the exact accepted commit.

## Acceptance and closeout

Stop with a frozen technical candidate pending author review. The candidate report
records invariant facts only (`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`).

## Required artifacts

F4 candidate report and JSON mirror; test evidence; receipts of PHASE, INTEGRATION and
STATIC; roadmap and mini-track status; the author smoke checklist.

## Stop conditions

- `FG-F4A-1` fails and the **[DQ-F4-3]** fallback is not admitted or does not work.
- The style-bar hook reaches Classic or changes the slider, the line-type table or the
  Properties field.
- The FreeHEP correction changes any currently successful export, or embedded-font PDFs
  are still invalid, empty or misreported.
- Any change outside the allowed scope appears necessary (kernel, `DrawText`, text
  sizes, other FreeHEP code, exporters, dialog options).
- Base identity, worktree, remote divergence, incomplete or untrusted coverage, or a
  missing author decision.

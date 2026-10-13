# PRE-G9B-R6-plus-F4 — two-track design successor candidate

| Field | Value |
|---|---|
| Status | **AUTHOR APPROVED — DESIGN** (F4 final author design approval of 2026-10-13 on `83f07931afb3a1c7fbbdba268d7da5fb2d8b3cf9`, tree `d4e6989c585e6839d22eaffc84df783c09a0c065`; [author design closeout record](../validation/pre_g9b_r6_plus_f4_author_design_closeout_record.md)); earlier amended 2026-10-13 by the final author reconciliation amendment (author directions on `DQ-F4-2` and `DQ-F4-10`, `DQ-F4-7` reconciled with the executable verification authority) |
| Supersedes | [reconciled design candidate](pre_g9b_r6_plus_f4_reconciled_design_candidate.md) of `PRE-G9B-R6-plus-F4-PREP` (kept unchanged as preparation evidence) |
| Prepared by | `PRE-G9B-R6-plus-F4-AUTHOR-DECISION-RECONCILIATION` (author instruction of 2026-10-13) on `089fc2205300559c572c12f0cd32326abd0c2b5e` |
| Decisions | [author-decision reconciliation](../validation/pre_g9b_r6_plus_f4_author_decision_reconciliation.md) (`DQ-F4-1` to `DQ-F4-10`, author approved 2026-10-13) |
| Implementation | **NOT AUTHORIZED** — F4-D first, then F4-A after F4-D publication, each by its own author instruction naming the exact base |

## 1. Common F4 scope

| Item | Proposed disposition |
|---|---|
| F4-A compact style-bar thickness entry | track **F4-A**, implementation candidate |
| F4-B DXF / AutoCAD | **CLOSE — RESOLVED FOR AUTOCAD 2026**; other versions a coverage limitation; no product change |
| F4-C `TD-PDF-1` | **DEFER** — new physical text presentation contract required; F4 changes no text size, label offset or S3 rule |
| F4-D `TD-PDF-2` | track **F4-D**, implementation candidate |

Common invariants: no geometry, DAG, identity, unit, layer or serialization change; no
shared-kernel change; no change to approved functionality (Properties field, physical
stroke and marker sizes, S3, DXF, F3).

## 2. Track F4-A — compact style-bar thickness entry

**Architecture A1 (GeoCeDG-owned).** A GeoCeDG subclass of the inherited
`EuclidianStyleBarD` overrides the protected `createButtons()`: after `super`, it adds a
`GeoCeDGLineThicknessField` bound to `btnLineStyle.getMySlider()` (storing undo through
the application) to the line-style popup with `addPopupMenuItem`. It is installed by
`GeoCeDGEuclidianView.newEuclidianStyleBar()` (Graphics 1) and by a `GuiManagerGeoCeDG`
override of the protected `GuiManager3D.newEuclidianView` that returns an
`EuclidianViewFor3DD` subclass overriding only its style-bar factory (Graphics 2;
author direction on `DQ-F4-2`: both Graphics 1 and Graphics 2). No inherited source
changes.

**Lifecycle.** `createButtons()` runs inside the inherited constructor and again on
language and font changes, recreating `btnLineStyle`; the subclass re-attaches a fresh
field each time and uses no subclass state during construction.

**Behaviour.** Exactly the approved Properties field (`DQ-F4-4`): integers within the
slider range, Enter or focus loss, refusal restores, one undo step; an accepted entry
equals moving the slider (same targets, multi-selection and creation-mode defaults, the
inherited line-type coupling).

**Feasibility gate.** Real-window gate first (`DQ-F4-3`): focus, digits and editing,
commit / cancel / focus loss, popup usable, slider functional, no accelerator conflict,
Classic unchanged, shown with native input (`java.awt.Robot`) and confirmed by the author
smoke. Only fallback: GeoCeDG-owned focusability of the containing popup through the
component ancestry. Otherwise `STOP — F4-A FOCUS FEASIBILITY BLOCKED`.

**Class.** Proposed `BOUNDED_PHASE`: registered PHASE, with `INFRA_UNIT` and `STATIC` for
the phase-selection registration. Alternative A2 (profile-gated inherited hook) would be
`INTEGRATED_PHASE`; it is not the author's direction.

## 3. Track F4-D — embedded-font PDF correction

**Change.** `AbstractCharTable.toName(char)` delegates to
`toName(Character.valueOf(c))` (the delegation broken by upstream `c4fd03ce`); one line,
registered as an inherited modification with provenance; an explicitly considered
exception to the minimal-upstream-change policy; no FreeHEP update, replacement or
refactoring, no dialog or exporter change, no correction of the xref observation.

**Perimeter.** Reachable only from `PDFGraphics2D.writeString` → `showCharacterCodes` →
`FontUtilities.showString` → `toEncoding(char)` → `toName(char)`, i.e. PDF text written
as text, from the GeoCeDG picture route and the Classic picture dialog; FreeHEP is used
only by the Desktop module. On the inspected paths every execution that reaches the line
fails today and no frame swallows the error (reconciliation §3); identity of the declared
outputs with the base (after the existing fingerprint normalization) is verified by
tests, and no wider guarantee is claimed.

**Verification obligations.** The nine obligations of the reconciliation §6 (no
`StackOverflowError`; complete PDF; font program embedded; glyphs correct; reader
compatibility beyond Ghostscript; text-as-shapes PDFs identical after normalization; Classic; SVG; no
other PDF change).

**Class.** `INTEGRATED_PHASE`, supported by the executable registry (`DQ-F4-7`;
reconciliation §4): registered PHASE with the PDF and export suites + INTEGRATION (which
holds every product and export node of FINAL), `INFRA_UNIT` and `STATIC` for the
registration; the raster and reader obligations, enforced by no profile (FINAL included),
by focused external evidence and the author smoke.

## 4. Sequencing, publication and aggregate closeout (`DQ-F4-10`)

Author direction (2026-10-13): F4-D, then F4-A on the published F4-D closeout, then a
final documentary aggregate F4 closeout. Each track: its own
implementation authorization (exact base, frozen class), technical candidate, evidence,
author smoke, author closeout and controlled publication. A final documentary F4
aggregate closeout records F4 complete. Approval or publication of one track never
authorizes the other; if F4-A's gate blocks, F4 closes without it only by explicit author
decision.

## 5. Deferred work

`TD-PDF-1` (minimum design questions in the reconciliation, `DQ-F4-8`); AutoCAD versions
other than 2026; `OBS-F4P-FREEHEP-XREF-SIZE`, `OBS-F4P-S3-SPEC-HEADER`,
`OBS-F4P-ADR0035-CONSEQUENCE-LINE` (G register).

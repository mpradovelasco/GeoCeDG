# PRE-G9B-R6-plus-F4 — reconciled design candidate

| Field | Value |
|---|---|
| Status | **PROPOSED — PENDING AUTHOR REVIEW** (no part is an author decision) |
| Phase | `PRE-G9B-R6-plus-F4` (planning intent of 2026-10-09, POST-E2-P4 closeout) |
| Prepared by | `PRE-G9B-R6-plus-F4-PREP` (author authorization of 2026-10-13) on `847bb20a593e8092eabf80bd9d7c62ed7c21de08` |
| Evidence | [preparation and characterization report](../validation/pre_g9b_r6_plus_f4_preparation_characterization_report.md) |
| Decisions to take | [author-decision preparation](../validation/pre_g9b_r6_plus_f4_author_decision_preparation_record.md) (`DQ-F4-1` to `DQ-F4-9`) |
| Implementation | **NOT AUTHORIZED** |

## 1. Recommended scope

| Item | Recommendation | Product change |
|---|---|---|
| F4-A compact style-bar thickness input | **INCLUDE — IMPLEMENTATION CANDIDATE** | yes, Desktop presentation |
| F4-B DXF compatibility with AutoCAD | **CLOSE — ALREADY RESOLVED** (AutoCAD 2026); other versions `NOT RECHECKED — COVERAGE GAP`, optional author **VALIDATION ONLY** | none |
| F4-C `TD-PDF-1` physical PDF text size | **DEFER — NOT IN F4**: no defect against an approved contract; a physical text-size contract needs its own design decision | none in F4 |
| F4-D `TD-PDF-2` embedded-font PDF | **INCLUDE — IMPLEMENTATION CANDIDATE** (confirmed inherited defect, one line) | yes, inherited vendored code |

Product work of the recommended F4: F4-A and F4-D. Everything else is documentary
disposition.

## 2. F4-A — numeric entry in the compact thickness selector

### 2.1 Required user-facing behaviour

- The Graphics style bar's line-style popup keeps its line-type table and its slider
  and gains the numeric field already used in Object Properties → Style
  (`GeoCeDGLineThicknessField`), beside or below the slider.
- The field shows the slider value and follows it (selection changes, creation-mode
  defaults, polygon minimum 0, maximum 13).
- Whole numbers within the slider range are accepted on Enter or focus loss; other
  input (empty, decimals, signs, letters, out of range) is refused and the shown value
  is restored. No locale-specific decimal parsing exists because no decimals are
  allowed (identical to the approved Properties field).
- An accepted entry changes the targets exactly as moving the slider to that value:
  same property (`GeoElement` line thickness), same targets, same inherited coupling
  (the line type selected in the popup is applied too), and stores exactly one undo
  step.
- Persistence is the existing `<lineStyle thickness=…>`; no new state, preference or
  serialized field.
- Classic and every non-GeoCeDG configuration are unchanged (`forProduct` returns
  `null`).

### 2.2 Design alternatives (author decision `DQ-F4-2`)

| Option | Mechanism | Inherited diff | Coverage | Assessment |
|---|---|---|---|---|
| **A-1 (recommended)** | one profile-gated call in the inherited `EuclidianStyleBarD.createButtons()` that adds `GeoCeDGLineThicknessField.forProduct(app, btnLineStyle.getMySlider(), app::storeUndoInfo)` to the popup through `addPopupMenuItem` | a few lines, registered in `docs/upstream/modified-files.yml`, like the approved `PropertiesPanelD` host of POST-E2-P1-R2 | every Graphics style bar of the product (Graphics, Graphics 2, plane and 3D bars that extend it) | smallest coherent change; same pattern as the approved Properties field |
| A-2 | a GeoCeDG subclass of `EuclidianStyleBarD` returned by `GeoCeDGEuclidianView.newEuclidianStyleBar()` and by a new `GuiManagerGeoCeDG` override of the Graphics 2 factory | none | Graphics 1 and 2 only | no inherited diff, more GeoCeDG code and two factory overrides |
| A-3 | defer | — | — | the accepted enhancement stays open |

### 2.3 Feasibility gate and fallback (`DQ-F4-3`)

`FG-F4A-1`: the popup and its button are non-focusable (`PopupMenuButtonD.java:155`,
`159`). The implementation must first establish, in the real window, that the field
receives keyboard focus, typing and Enter without closing the popup. If it does not,
the only admissible fallback is a profile-gated focusability of that one popup while
the GeoCeDG field is present (inherited `PopupMenuButtonD`, registered); any wider
change stops for the author.

### 2.4 Invariants

Geometry, definitions, layers, labels, serialization format, the Properties field, the
slider behaviour, the inherited undo boundary of a slider drag and every Classic surface
stay unchanged. Known inherited limitation, not changed: in a creation mode the style
bar edits construction defaults, which the undo history does not contain.

## 3. F4-D — embedded-font PDF

### 3.1 Correction (`DQ-F4-5`)

| Option | Change | Effect |
|---|---|---|
| **D-1 (recommended)** | `AbstractCharTable.toName(char c)` returns `toName(Character.valueOf(c))` (the semantics before upstream `c4fd03ce`, without the deprecated constructor); registered inherited modification | the embedded-font route of GeoCeDG and Classic works; every currently successful output is unchanged by construction, because the corrected method is reached only on paths that today always end in `StackOverflowError` |
| D-2 | GeoCeDG-gated removal of the "Export text as shapes" choice for PDF (always shapes) | no failure, no embedded fonts in GeoCeDG; Classic keeps the failure; inherited dialog change |
| D-3 | defer | the user-reachable failure remains |

Feasibility of D-1 is established in a scratch copy (characterization report §7).

### 3.2 Invariants

Text-as-shapes PDFs, SVG, EMF, PNG, LaTeX and DXF outputs are byte-identical; the
physical style sizes (`physical-pdf-style-sizes.md`) are unchanged; no text-size change
(TD-PDF-1 stays outside). A failed export must not be reported as success.

## 4. F4-B — disposition only

Close the planning item: AutoCAD 2026 compatibility is author-verified for the ISO A
sheet and re-confirmed by the agent for every G5 entity family and both routes on
`847bb20a`. AutoCAD 2024 and other releases are a recorded coverage gap; a validation
needs software controlled by the author. No exporter change; no `SPLINE`, text or new
export semantics.

## 5. F4-C — options for a later contract (not F4)

| Option | Scope | Footprint |
|---|---|---|
| C-0 | keep both rules; explain them in the guide | documentation |
| C-1 | physical size for object labels (and their offsets) on the physical PDF route only | GeoCeDG export layer (`ExportViewport` label-font overrides) |
| C-2 | C-1 plus a physical size for construction texts on the physical PDF route | shared presentation hook in `DrawText`; the PDF would no longer show the screen's text-to-geometry proportion (S3) |
| C-3 | redefine the S3 reference for documents with a physical unit (text size defined on paper at the drawing scale) | screen and export; S3 redesign |

Recommendation: register `TD-PDF-1` as `CHARACTERIZED — CONTRACT DESIGN REQUIRED` and
decide the contract outside F4.

## 6. Architecture and verification

Nothing belongs in the shared kernel. F4-A is Desktop presentation (GeoCeDG component,
profile-gated inherited host); F4-D is inherited vendored export code. Proposed
structure (`DQ-F4-7`): **one F4 implementation of F4-A and F4-D, class
`INTEGRATED_PHASE`** (both touch inherited code shared with Classic; INTEGRATION runs
the complete Desktop and Classic suites), with a new registered phase selection and
author smoke for real keyboard focus in the popup and for opening an embedded-font PDF
in a PDF reader. Alternative: two `BOUNDED_PHASE` tracks (F4-1 style bar, F4-2
embedded fonts). No class is frozen by this preparation.

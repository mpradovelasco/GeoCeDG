# PRE-G9B-R6-plus-F4 — preparation and characterization report

```text
TASK                 = PRE-G9B-R6-plus-F4-PREP (author authorization of 2026-10-13)
CLASS                = DOCUMENTATION_STATUS_ONLY (acceptance STATIC), with disposable
                       scratch probes outside the tree
PUBLISHED BASE       = P_F3_CLOSEOUT 847bb20a593e8092eabf80bd9d7c62ed7c21de08
                       tree 10846ff491572f5c557f3c2b7fadb3a6a1053f0a
F3                   = PASS — AUTHOR APPROVED — PUBLISHED (remote main confirmed = 847bb20a)
F4                   = PLANNING INTENT RECORDED — PREPARED — DESIGN PROPOSED — PENDING
                       AUTHOR REVIEW — IMPLEMENTATION NOT AUTHORIZED
PRODUCT CHANGE       = NONE
selfApproved         = false; implementationAuthorized = false;
publicationAuthorized = false
```

Companion documents: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f4_reconciled_design_candidate.md)
(`PROPOSED — PENDING AUTHOR REVIEW`);
[author-decision preparation](pre_g9b_r6_plus_f4_author_decision_preparation_record.md);
[risk and debt register](pre_g9b_r6_plus_f4_risk_and_debt_register.md); canonical
prompt [`pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-f4-stylebar-thickness-and-pdf-fonts.prompt.md)
(`PREPARED — NOT AUTHORIZED`); machine-readable mirror
[`pre-g9b-r6-plus-f4-preparation.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-preparation.json).

## 1. Entry gate and F3 publication reconciliation

| Check | Result |
|---|---|
| local `main`, `origin/main`, live remote `main` (fetched) | `847bb20a593e8092eabf80bd9d7c62ed7c21de08`, tree `10846ff491572f5c557f3c2b7fadb3a6a1053f0a` |
| F3 closeout ancestry | `2d7b2cae` (tree `667308b9…`) is the parent of `847bb20a`; F3 `16547115` (tree `8345c54d…`) and F3-R1 `c4dffe60` (tree `8c9dd493…`) unchanged |
| F3 closeout STATIC | `verification-f919cf565f0e4a2da3128d8387210200` `ACCEPTED / COMPLETE`, candidate `847bb20a` |
| F3 specification and record | `geocedg/specs/ui/document-insertion.md` `NORMATIVE / AUTHOR APPROVED`; [F3/F3-R1/F3-R2 closeout record](pre_g9b_r6_plus_f3_f3r1_f3r2_closeout_record.md) |
| worktree | clean |
| F4 authorization | none beyond the planning intent: no F4 prompt in `.github/prompts/tasks/`, no F4 record in `docs/validation/` |
| result | **PASS**; branch `phase/pre-g9b-r6-plus-f4-prep` created at `847bb20a` |

The F3 closeout record states closeout acceptance only (it cannot name its own
publication). The publication is confirmed here: remote `main` = `847bb20a`, reached
by the authorized non-force fast-forward from `e70718fe`. The living roadmap and the
mini-track plan now record F3 as `PASS — AUTHOR APPROVED — PUBLISHED`; the F3 closeout
record and its JSON mirror are unchanged.

## 2. F4 planning authority

The only F4 authority is the author's planning note of 2026-10-09
([POST-E2-P4 closeout record](post_e2_p4_closeout_record.md), "Planning notes
recorded by the author"; mirror `geocedg/validation/post-e2/post-e2-p4-closeout.json`):
F4 "will be proposed between `F3` and `G`; intended ownership: compact style-bar
numeric line-thickness entry (`OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT`),
DXF compatibility recheck with AutoCAD 2026, `TD-PDF-1` (physical text size) and
`TD-PDF-2` (embedded-font PDF behaviour)"; state `PLANNING INTENT RECORDED —
IMPLEMENTATION NOT AUTHORIZED`; "no implementation contract or verification class is
defined". The F3 handoff (§13 of the F3 closeout record) restates it and requires that
resolved AutoCAD 2026 evidence be distinguished from other versions. No other debt is
assigned to F4.

| Item | Source of the item | Recorded state before this preparation |
|---|---|---|
| F4-A | [POST-E2-P1-R2 closeout record](post_e2_p1_r2_closeout_record.md) | `ENHANCEMENT — ACCEPTED NON-BLOCKING — IMPLEMENTATION NOT AUTHORIZED` |
| F4-B | [E3 closeout record](pre_g9b_r6_plus_e3_closeout_record.md) | `OBS-R6PLUS-DXF-AUTOCAD-CONTAINER-REJECTION` `RESOLVED BY E3-R1 FOR AUTOCAD 2026 — AUTHOR CONFIRMED`; other versions not rechecked |
| F4-C | [POST-E2-P1-R1 closeout record](post_e2_p1_r1_closeout_record.md) | `TD-PDF-1` `OPEN — CHARACTERIZATION REQUIRED — NOT AUTHORIZED` |
| F4-D | same | `TD-PDF-2` `OPEN — UNCONFIRMED IN PRODUCT — HEADLESS TEST ENVIRONMENT OBSERVATION — NOT AUTHORIZED` |

## 3. Method

Read-only inspection of the published base and disposable scratch probes: JUnit
classes added to the Desktop test source set by a Gradle init script from the session
scratchpad (never tracked), test JVM Java 17.0.10 on Windows 11, **not headless**
(`GraphicsEnvironment.isHeadless() = false`). PDF measurement reuses the P1-R1
interpreter (`PostE2P1R1PdfStrokeWidthCharacterizationTest.interpret`): graphics-state
stack, concatenated matrices, fills by colour; glyph ink is measured on the
text-as-shapes route as the bounds of the glyph fills of each object's own colour, and
the nominal font size on the embedded route as `Tf` times the text and transformation
matrices. DXF files were opened with the installed AutoCAD 2026 core console
(`accoreconsole.exe` W.164.0.0) with `AUDIT`. Probe sources and outputs are kept,
untracked, in `artifacts/pre-g9b-r6-plus-f4-prep/` (gitignored). The probe classes were
removed from the build output afterwards.

## 4. F4-A — compact style-bar thickness input

**Verified current behaviour.**

| Element | Fact | Source |
|---|---|---|
| compact control | inherited `EuclidianStyleBarD.btnLineStyle`, a `PopupMenuButtonD` with a line-type table and a lazily created `JSlider(0,100)` | `EuclidianStyleBarD.java:579-633`; `PopupMenuButtonD.java:342-364`, `447-452` |
| range | slider minimum 1 (0 when every target allows 0, e.g. polygons), maximum `GeoElement.MAX_LINE_WIDTH` = 13, ticks without labels; no numeric display | `EuclidianStyleBarD.java:605`, `635-639`; `GeoElement.java:144` |
| apply path | slider change → `PopupMenuButtonD.stateChanged` → `fireActionPerformed` → `EuclidianStyleBarD.processSource` → `EuclidianStyleBarStatic.applyLineStyle(index, size, app, targets)`, which sets line type **and** thickness | `EuclidianStyleBarStatic.java:400-417` |
| targets | selection (move mode) or the creation mode's construction default geos plus just-created geos | `EuclidianStyleBarSelection.java:104-153` |
| undo | one `storeUndoInfo` per press-to-release gesture (`SliderUtil`); thickness-only changes store no step per tick | `PopupMenuButtonD.java:350`; `SliderUtil.java:31-47`; `EuclidianStyleBarStatic.java:406-412` |
| selection sync | `updateStyleBar`/`updateButtons` → `btnLineStyle.update(geos)`: value of the first geo, listener detached; no mixed-value display | `EuclidianStyleBarD.java:300-386`, `584-614` |
| property authority | `GeoElement.setLineThickness` (`max(0, th)`, no upper clamp; minimum per type via `getMinimumLineThickness`); polygons and lists propagate; points and texts have no line properties | `GeoElement.java:4793-4841`; `GeoPolygon.java:1207-1231`, `1700-1703` |
| persistence | `<lineStyle thickness=…>`; read without clamp | `GeoElement.java:4748-4764`; `ConsElementXMLHandler.java:1401-1405` |
| existing numeric field | `GeoCeDGLineThicknessField` (POST-E2-P1-R2): `JSpinner` bound to a host `JSlider`, integer regex `[0-9]{1,4}` within the slider range, Enter or focus loss commits, refused entries revert, one undo point per accepted entry; product-gated by `forProduct` (`AppConfigGeoCeDG`), hosted by the inherited `PropertiesPanelD` through a 38-line registered modification | `GeoCeDGLineThicknessField.java:44-182`; `PropertiesPanelD.java:2152-2157`; `docs/upstream/modified-files.yml` |
| Classic | `EuclidianStyleBarD` and `PopupMenuButtonD` are byte-identical to the baseline `9b93256b` and shared by Classic and GeoCeDG; the field factory returns `null` for Classic | `git diff 9b93256b..847bb20a` empty for both files |
| seams | `EuclidianViewD.newEuclidianStyleBar()` (protected; Graphics 1 via `GeoCeDGEuclidianView`, Graphics 2 via the inherited `GuiManager3D.newEuclidianView`); `PopupMenuButtonD.addPopupMenuItem(JComponent)` and `getMySlider()` are public | `EuclidianViewD.java:882-885`; `GuiManager3D.java:257-265`; `PopupMenuButtonD.java:277`, `447` |
| tests | the Properties field: `PostE2P1R2SmokeFollowUpTest` (field beside the slider, refusals, one undo point, Classic has none); no Desktop style-bar test | `PostE2P1R2SmokeFollowUpTest.java:138-238` |
| localization | field tooltip and accessible name `Properties.LineThickness.Field` (EN/ES) in the application profile; style-bar tooltip `stylebar.LineStyle` | `application-profile.yml:5579-5582` |

**Not established.** The style-bar popup and its button are created non-focusable
(`PopupMenuButtonD.java:155`, `159`: `setFocusable(false)` on the button and on the
`JPopupMenu`). Whether a text field added to that popup receives real keyboard focus
(typing, Enter, IME) cannot be established by in-process event dispatch; it is the
first feasibility gate of an implementation (`FG-F4A-1`).

**Inherited couplings the enhancement would inherit.** A thickness change through the
style bar also re-applies the line type selected in the popup table to every target
(`applyLineStyle`); in a creation mode the targets are construction defaults, whose
changes are outside the undo XML (`MyXMLio.getUndoXML`) and persist in
`geogebra_defaults2d.xml`.

**Result.** Work remains: an accepted enhancement with a reusable GeoCeDG component and
an exact host; no defect. Recommendation: **INCLUDE — IMPLEMENTATION CANDIDATE**.

## 5. F4-B — DXF compatibility with AutoCAD

**Accepted evidence.** The G5 container written since G5 was refused by AutoCAD 2024
("Error in APPID Table", C phase) and AutoCAD 2026 (E3 smoke). E3-R1 added the
complete AC1015 container (tables VPORT, LTYPE, LAYER with plot-style pointers, STYLE,
VIEW, UCS, APPID, DIMSTYLE, BLOCK_RECORD; BLOCKS; OBJECTS; handles below `0x100`;
entities unchanged) to the shared writer used by the neutral G5 route and the extended
G9X1 route ([E3-R1 candidate report](pre_g9b_r6_plus_e3_r1_candidate_report.md) §2;
`geometry-export-foundation.md`, amendment `AUTHOR APPROVED — IMPLEMENTED AND
VERIFIED`). The agent opened three samples in AutoCAD 2026 (AUDIT clean); the author
confirmed that the corrected ISO A sheet DXF opens in AutoCAD 2026 with correct geometry
and margins ([E3 closeout record](pre_g9b_r6_plus_e3_closeout_record.md)). The G5 entity
families are `POINT`, `LINE`, `RAY`, `XLINE`, `CIRCLE`, `ARC`, `ELLIPSE`, `LWPOLYLINE`
([ADR 0005](../adr/0005-neutral-2d-geometry-export.md)); DXF writes no text (approved
limitation).

**Re-check on the published base (agent, AutoCAD 2026 W.164.0.0, `AUDIT`).**

| Case (`847bb20a`) | Route | Entities written | AutoCAD 2026 |
|---|---|---|---|
| point, segment, circle, arc, ellipse (mm) | neutral and extended (byte-identical) | `POINT` ×2, `LINE`, `CIRCLE`, `ARC`, `ELLIPSE` | opened, 6 entities, AUDIT 0 found / 0 fixed |
| polygon | both (byte-identical) | `LWPOLYLINE` | opened, 1 entity, AUDIT clean |
| line and ray | both (byte-identical) | `XLINE`, `RAY` | opened, 2 entities, AUDIT clean |
| ISO A sheet 1:5 with segment and circle | both (byte-identical) | `POINT` ×2, `LWPOLYLINE` ×2, `LINE`, `CIRCLE` | opened, 6 entities, AUDIT clean |
| function graph `If(0<=x<=40, …)` | neutral: no entity; extended: refused by the strict preflight | — | the empty container opens, AUDIT clean |

Every file declares `$ACADVER = AC1015`; the files were not modified by AutoCAD. All
eight G5 entity families now have AutoCAD 2026 evidence; the function graph is outside
the exported families (pre-existing G5/G9X1 scope, not a container question).

**Classification.**

| Statement | Class |
|---|---|
| AutoCAD 2026 opens GeoCeDG DXF (ISO A sheet) | `RESOLVED — AUTHOR-VERIFIED` |
| AutoCAD 2026 opens every G5 entity family through both routes on `847bb20a` | `SUPPORTED BY EXISTING AUTOMATED EVIDENCE` (agent re-check) |
| AutoCAD 2024 (refused before E3-R1), other releases, AutoCAD LT, DWG TrueView, other readers | `NOT RECHECKED — COVERAGE GAP` (only AutoCAD 2026 is installed on this workstation; no defect inferred) |
| open DXF defect | none reproduced |

**Result.** The F4-B objective ("DXF compatibility recheck with AutoCAD 2026") is met
by the accepted evidence plus this re-check; no implementation is needed.
Recommendation: **CLOSE — ALREADY RESOLVED** (AutoCAD 2026), with other versions
recorded as a coverage gap and an optional author-controlled **VALIDATION ONLY** if
another version matters.

## 6. F4-C — physical PDF text size (`TD-PDF-1`)

**Contracts.** No approved specification defines a physical text size.
`physical-pdf-style-sizes.md` §4 excludes "Text and labels of every kind, including
label positions and offsets, the dimension value and its placement (`D-R2-7`)", and §6
keeps the zoom-dependent rule for excluded sizes; ADR 0035 decision 4 does the same.
`construction-text-view-scaling.md` (S3) scales world-relative construction texts with
the view: `fEffective = fLogical · xscale / SCALE_STANDARD` (`SCALE_STANDARD = 50`), and
notes that platform font rasterization may quantize very small sizes. The unit system
(§13) defines the physical output of model lengths only.

**Mechanism (source).** The physical PDF route paints the export viewport with
`pointsPerPixel = printingScale · 72 / (2.54 · xscale)` and rescales only style sizes
(`PictureExportService.writePDF:166-198`). Construction texts and dimension values go
through `DrawText.getFontSize` (S3 rule, `DrawText.java:328-340`); object labels use the
view's label fonts in view pixels.

**Measurements (`847bb20a`; glyph ink height in mm on the text-as-shapes route;
default application font 16 px, text multiplier 1).**

| Unit, drawing scale, zoom (px/unit) | PDF pt per view px | segment label `HH` | text `HHH` | dimension value |
|---|---|---|---|---|
| mm, 1:1, 2 | 1.41732 | 4.297 | 0.360 | 0.368 |
| mm, 1:1, 8 | 0.35433 | 1.074 | 0.268 | 0.274 |
| mm, 1:1, 32 | 0.08858 | 0.268 | 0.224 | 0.229 |
| cm, 1:1, 80 | 0.35433 | 1.074 | 2.326 | 2.365 |
| m, 1:1, 8000 | 0.35433 | 1.074 | 229.063 | 233.906 |
| mm, 1:10, 8 | 0.03543 | 0.107 | 0.027 | 0.027 |
| mm, 2:1, 8 | 0.70866 | 2.147 | 0.535 | 0.547 |
| no unit (legacy), 8 | 0.35433 | 1.074 | 0.268 | 0.274 |

The historical P1-R1 values (0.3675 / 0.27375 / 0.22875 mm at zoom 2 / 8 / 32, mm 1:1;
`geocedg/validation/post-e2/post-e2-p1-r1-measurements.json`) are reproduced. With the
TD-PDF-2 correction applied in a scratch copy (§7), the embedded route reports the
nominal sizes directly; for example, the largest font of the m document is 907.087 pt
= 320.000 mm, the font of the construction text.

**Diagnosis.**

1. **Construction texts and dimension values follow the approved S3 rule**: their font
   is `fLogical / 50` model units (0.32 units for 16 px), so the physical size is
   `0.32 · (model unit length) · (drawing scale)`: a 0.32 mm font (0.22–0.23 mm cap
   height) in a mm document at 1:1, 3.2 mm in cm and 320 mm in m; it does not depend on
   the zoom, except for the platform quantization of very small effective fonts at low
   zoom (0.36 mm at zoom 2), which S3 records.
2. **Object labels follow the inherited view-pixel rule**: about 8.6 view pixels of cap
   height times `pointsPerPixel`, i.e. inversely proportional to the zoom and
   proportional to the drawing scale (4.3 mm at zoom 2, 0.27 mm at zoom 32 for mm 1:1).
   P1-R1 did not measure labels (its objects had labels hidden).
3. Both rules are consistent with the screen (WYSIWYG relative to the geometry) and with
   the approved contracts; neither yields a defined physical text size. TD-PDF-1 is
   therefore **no defect against an approved contract**: it is a missing contract — the
   same kind of gap that strokes had before POST-E2-P1-R2. The "extremely small text" is
   the S3 rule in a millimetre document.
4. The behaviour is GeoCeDG-specific for construction texts (S3) and inherited for labels
   (GeoGebra print scaling).

**Correction footprint if the author defines a contract.** Labels: the GeoCeDG-owned
`ExportViewport` already overrides the shared physical-scale hooks added by
POST-E2-P1-R2 (`getPhysicalStyleScale`, `getPhysicalMarkerScale`) and could override the
public, non-final label-font getters (`EuclidianView.getFontPoint`, `getFontLine`, …);
label offsets are view-pixel offsets and would need the same rule. Construction texts:
`DrawText.getFontSize` (shared presentation code already modified by S3) would need an
export-viewport hook, and a physical text size on the PDF only would break the S3
proportion between text and geometry that the screen shows; changing S3 itself would
change the screen. None of this is kernel semantics. Recommendation: **DEFER — NOT IN F4**
pending an author decision on a physical text-size contract
(`CHARACTERIZED — CONTRACT DESIGN REQUIRED`).

## 7. F4-D — embedded-font PDF (`TD-PDF-2`)

**Reproduction (`847bb20a`, windowed JVM).**

| Route | Result |
|---|---|
| GeoCeDG physical PDF, `textAsShapes = false`, every case of §6 (mm, cm, m, 1:10, 2:1, legacy) | `StackOverflowError` at `org.freehep.graphics2d.font.AbstractCharTable.toName(AbstractCharTable.java:35)`, recursing |
| failed file | left on disk with 0 bytes |
| same, scene without any text (one unlabeled segment), mm and legacy | succeeds (2381 / 2379 bytes) |
| Classic `GraphicExportDialog.exportPDF`, `textAsShapes = true` | succeeds (12591 bytes) |
| Classic, `textAsShapes = false` | `StackOverflowError`, same frame; 0-byte file |
| GeoCeDG SVG with embedded fonts | succeeds |

**Root cause.** `AbstractCharTable.toName(char c)` reads `return toName(c);`, which calls
itself. The upstream GeoGebra commit `c4fd03ce52c8f6d124a17c09892e9ed181d15236`
(2025-12-17, "Fix Java 17 warnings") replaced `return toName(new Character(c));` with
`return toName(c);`. The commit is an ancestor of the GeoCeDG baseline `9b93256b`, and
the file is byte-identical to the baseline: the defect is **inherited** vendored FreeHEP
code in the upstream Desktop source tree. Any drawn glyph on the embedded-font route
reaches it (`Lookup.toName(char)` → table `toName(char)`); the text-as-shapes route never
calls it.

**Reachability.** The picture dialog (`File → Graphics View as Picture… → PDF`, used by
GeoCeDG through its picture route) has one option, "Export text as shapes" (default
checked, re-checked when PDF is selected; `GraphicExportDialog.java:109`, `271-321`).
Unchecking it selects the embedded-font route; the dialog catches the `Error` and shows
only "Save file failed" (`GraphicExportDialog.java:713-716`). The command-line route
always writes text as shapes (`PictureExportCommandLine.java:106`).

**Feasibility of the minimal correction (scratch copy only).** In a disposable
`git archive` of `847bb20a` with the one line changed to
`return toName(Character.valueOf(c));` (the pre-`c4fd03ce` semantics without the
deprecated constructor), every case of §6 and Classic produce embedded-font PDFs (about
59 KB, one embedded Type 1 font program, `/FontFile` present); Ghostscript 10.02.1
renders them, with the text drawn as glyphs. Ghostscript's only diagnostic, "incorrect
xref size … repaired", appears identically for today's text-as-shapes PDFs: a
pre-existing FreeHEP trailer property, independent of fonts (new observation
`OBS-F4P-FREEHEP-XREF-SIZE`). Visual quality of embedded glyphs (spacing, hinting) and
other PDF readers were not assessed.

**Not reproduced.** The historical headless observations — failure "even for a scene
without text" and a `NullPointerException` on the Classic route — were not reproduced
in the windowed JVM (the no-text scene succeeds; Classic fails with the same
`StackOverflowError`). Their exact headless conditions were not recorded.

**Classification: `PRODUCT DEFECT CONFIRMED`** — inherited upstream defect, reachable in
the GeoCeDG product and in Classic, environment-independent (not headless-only).
Recommendation: **INCLUDE — IMPLEMENTATION CANDIDATE** (one-line inherited correction,
subject to the author's authorization of an inherited change).

## 8. Architectural ownership

| Workstream | Owner | Kernel | Shared code | Inherited code | Classic effect |
|---|---|---|---|---|---|
| F4-A | GeoCeDG Desktop (`GeoCeDGLineThicknessField` reuse) | none | none | one profile-gated host hook in `EuclidianStyleBarD` (or a GeoCeDG style-bar subclass); possibly the popup focusability (`FG-F4A-1`) | none (gated) |
| F4-B | documentary closure; optional external validation | none | none | none | none |
| F4-C | not owned by F4 (contract design first); later: GeoCeDG export layer for labels, shared presentation hook for texts | none | possibly `DrawText` (presentation) | — | none intended |
| F4-D | inherited vendored FreeHEP (`AbstractCharTable`), registered as an upstream modification | none | none | one line | corrected identically (Classic shares the writer) |

No workstream needs geometric semantics, durable identity, persistence or DAG
integration; nothing belongs in the shared kernel.

## 9. Observations found by this preparation

- `OBS-F4P-FREEHEP-XREF-SIZE`: Ghostscript reports "incorrect xref size" (repaired) on
  every FreeHEP PDF, text-as-shapes included; pre-existing; not characterized further.
- `OBS-F4P-S3-SPEC-HEADER`: `construction-text-view-scaling.md` still reads
  `IMPLEMENTATION AUTHORIZED — CANDIDATE PENDING REVIEW`, while the roadmap records S3
  as `PASS — AUTHOR APPROVED`.
- `OBS-F4P-ADR0035-CONSEQUENCE-LINE`: ADR 0035 Consequences still state point markers
  of `1.6 · pointSize` pt; its own amendment and the specification (§2.5) supersede it.

None is changed here: approved documents are not edited by this preparation.

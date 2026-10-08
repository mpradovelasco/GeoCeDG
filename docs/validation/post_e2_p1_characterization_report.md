# POST-E2-P1 — dimension Gap/Overshoot and physical units: characterization report

```text
TECHNICAL_CANDIDATE_STATE = CHARACTERIZATION CANDIDATE, FROZEN with the commit that
                            contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P1
ACTIVITY                  = CHARACTERIZATION ONLY
VERIFICATION_CLASS        = BOUNDED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P1
BASELINE                  = ed3332b5f06c83e63788b32a79fe703b13aa4375
                            tree 582c87ea843f5d375eb7ba6d0db2b72828edb711
                            (POST-E2 planning closeout, published main)
PRODUCT CHANGE            = NONE
SERIALIZATION CHANGE      = NONE
CLASSIFICATION            = P1-INTENDED-PHYSICAL-CAPTURE + P1-DEFAULT-UX-LIMITATION
CONVERSION DEFECT         = NOT FOUND
implementationAuthorized  = false
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written; it carries no author
approval. The candidate commit cannot name itself, so its identity and its `PHASE`
run are reported outside this file. Authority:
[planning author decision](post_e2_planning_author_decision_record.md);
[planning proposal](../architecture/post_e2_planning_proposal.md) (P1);
[E2 closeout record](pre_g9b_r6_plus_e2_closeout_record.md) (`OBS-E2-1`).
Machine-readable mirror:
[`post-e2-p1-characterization-evidence.json`](../../geocedg/validation/post-e2/post-e2-p1-characterization-evidence.json);
measured rows:
[`post-e2-p1-measurements.json`](../../geocedg/validation/post-e2/post-e2-p1-measurements.json).

Statements are labelled **[observed]** (measured by this characterization),
**[code]** (confirmed by reading the source), **[hypothesis]** or
**[recommendation]**.

## 1. Baseline and sources

- Baseline `ed3332b5` (published `main`); approved `E2` candidate `3a724661`
  (tree `00b5230d`); original `E2` candidate `878ff66b`.
- **[code]** The capture path is unchanged since the original `E2` candidate:
  `git diff 878ff66b ed3332b5` touches `GeoCeDGDimensionTools.java` only to add
  the B1 line-thickness preference after creation; `captureMagnitudes`,
  `DrawingScale` and `DimensionFigure2D` are identical. No earlier published
  baseline has native dimensions, so a regression between published baselines is
  excluded at code level.

## 2. Execution path from the gesture to the stored values [code]

| Step | Input quantity (unit) | Conversion | Output quantity |
|---|---|---|---|
| 1. placement click (`GeoCeDGEuclidianController` → `GeoCeDGDimensionTools.create`, `GeoCeDGDimensionTools.java:96`) | mouse location (integer screen px) | view inverse transform | click point `C` (model units) |
| 2. `captureMagnitudes(state, drawingScale, xscale, yscale)` (`:70-81`), physical unit | paper targets 2 mm, 1 mm (`PAPER_OVERSHOOT_MM`, `PAPER_GAP_MM`, `:34`, `:36`) | `paperMetresPerModelUnit = f(c) · a / b` (`f` from `UnitState.effectiveConstructionMetresPerUnit`, `UnitToken`: mm 0.001, cm 0.01, m 1); `o = 0.002 / (f · a/b)`, `g = 0.001 / (f · a/b)` | `Overshoot`, `Gap` (model units) |
| 2'. same, unspecified unit | 8 px, 4 px (`VIEW_*_PX`, `:38`, `:40`) | `o = 8 / sqrt(xscale · yscale)`, `g = 4 / sqrt(...)` | `Overshoot`, `Gap` (model units) |
| 3. hidden labelled free numbers (`parameter`, `:159`) and the ordinary command | model values | none | independent `GeoNumeric` inputs 4 and 5 (5 and 6 for linear) |
| 4. `offset = <C − A, n̂>` (`AlgoNativeDimension.offsetThrough`) | `C` (model) | projection | `Offset` (model units) |
| 5. kernel geometry (`DimensionFigure2D.extension`, `:120-127`) | `Gap`, `Overshoot`, `Offset` (model) | `ext_P = Segment(P + σ g n̂, P' + σ o n̂)` | extension lines (model) |

`DrawingScale` `a:b` means paper length / model physical length `= a/b`
(`DrawingScale.java`; session state of the window, default `1:1`, reset on New
and Open). The capture runs once; nothing reads the unit state, the drawing
scale or the view afterwards (native-dimensions §8.3).

## 3. Analytical reference

`g_model = 0.001 / (f(c) · a/b)`, `o_model = 0.002 / (f(c) · a/b)`.

**[observed]** `captureMagnitudes` equals the formula to 1e-12 relative for
`mm`, `cm`, `m` and the scales `1:1`, `1:10`, `1:2`, `2:1`, `5:1`; the
numerator/denominator interpretation is `a/b` as specified. Reference values
are reproduced exactly:

| unit | Gap 1:1 | Overshoot 1:1 | Gap 1:10 | Overshoot 1:10 |
|---|---:|---:|---:|---:|
| mm | 1 | 2 | 10 | 20 |
| cm | 0.1 | 0.2 | 1 | 2 |
| m | 0.001 | 0.002 | 0.01 | 0.02 |

## 4. Reproduction procedure

Committed test
`source/desktop/desktop/src/test/java/org/geocedg/desktop/PostE2P1DimensionCaptureCharacterizationTest.java`
(JUnit selection `post-e2-p1.desktop`, phase `POST-E2-P1`). It creates
dimensions through the real tool route (`GeoCeDGEuclidianController.processMode`
with the Aligned and Linear Dimension modes: point, point, [x-axis], placement
click), sets the view with `setCoordSystem` (800×600 px panel), the unit state
with `DocumentUnitSystem.replace` and the session drawing scale with
`setDrawingScale`. With `GEOCEDG_POST_E2_P1_REPORT=<file>` it writes the
measured rows; the committed
[`post-e2-p1-measurements.json`](../../geocedg/validation/post-e2/post-e2-p1-measurements.json)
was produced that way on this tree.

## 5. Experiment P1-A — same numerical geometry

`A=(0,0)`, `B=(40,0)`, placement at offset 30, units `mm`/`cm`/`m`/unspecified,
scales `1:1`/`1:10`, zoom 5, 20 and 50 px per model unit (50 is GeoGebra's
default). **[observed]**

| unit | scale | Gap / Overshoot (model) | object represented | Gap / physical length | screen Gap / Overshoot at 5 · 20 · 50 px/unit |
|---|---|---|---|---:|---|
| mm | 1:1 | 1 / 2 | 40 mm | 2.5 % | 5/10 · 20/40 · **50/100 px** |
| mm | 1:10 | 10 / 20 | 40 mm | 25 % | 50/100 · 200/400 · **500/1000 px** |
| cm | 1:1 | 0.1 / 0.2 | 40 cm | 0.25 % | 0.5/1 · 2/4 · 5/10 px |
| cm | 1:10 | 1 / 2 | 40 cm | 2.5 % | 5/10 · 20/40 · 50/100 px |
| m | 1:1 | 0.001 / 0.002 | 40 m | 0.0025 % | 0.005/0.01 · 0.02/0.04 · 0.05/0.1 px |
| m | 1:10 | 0.01 / 0.02 | 40 m | 0.025 % | 0.05/0.1 · 0.2/0.4 · 0.5/1 px |
| unspecified | any | 4/z, 8/z | no physical meaning | — | 4/8 px at every zoom |

Every physical case gives exactly 1 mm and 2 mm on paper at its drawing scale;
the zoom never enters the capture. With the same numbers, `mm` represents an
object 10× smaller than `cm` and 1000× smaller than `m`, so the same paper-sized
marks are 10× and 1000× larger relative to the numerical geometry, and at a
given zoom 10× and 1000× larger on screen.

## 6. Experiment P1-B — physically equivalent geometry

40 mm as `B=(40,0)` (mm), `(4,0)` (cm), `(0.04,0)` (m); the same paper offset
(8 mm); scales `1:1`/`1:10`; viewport normalized to 1, 4 and 10 px per physical
model millimetre (px per model unit = k · 1000 · f); Aligned and Linear tools.
**[observed]**

- Paper sizes: 1.000 mm and 2.000 mm in all 36 cases (3 units × 2 scales × 3
  zooms × 2 tools), equal across units to 1e-9.
- Screen sizes under the normalized viewport: identical across units, Gap =
  `k · b/a` px and Overshoot = `2 k · b/a` px (1/2, 4/8, 10/20 px at `1:1`;
  10/20, 40/80, 100/200 px at `1:10`).
- Linear Dimension captures the same values as Aligned Dimension in every case:
  the capture is shared (`create`), not tool-specific.

Physically equivalent drawings therefore look identical in every unit. The
difference observed by the author is not a unit effect.

## 7. Unspecified-unit fallback

**[observed]** 8 px and 4 px at the creation zoom (0.8/1.6 at 5 px/unit,
0.2/0.4 at 20, 0.08/0.16 at 50), independent of the drawing scale, captured once.
No physical acceptance applies to these cases.

## 8. Screen space

**[observed]** Screen distances equal the model values times the view scale
(view transform measured from the extension and dimension-line endpoints).
**[code]** GeoGebra's default view is 50 px per model unit whatever the unit (`EuclidianView.SCALE_STANDARD = 50`).
At a nominal 96 dpi (1 mm = 3.78 px; display DPI not measured), that default
shows one model millimetre of an `mm` document as 50 px ≈ 13 mm on screen
(about 13× magnification of the physical model), one model centimetre of a `cm`
document as ≈ 1.3× and one metre of an `m` document as ≈ 0.013×. Paper-sized
marks are scaled by the same factor: about 13 mm and 26 mm on screen in `mm`
at `1:1` (130 mm and 260 mm at `1:10`), 1.3 mm and 2.6 mm in `cm`, invisible
in `m`.

## 9. Export (PDF and PGF/TikZ)

Physically equivalent cases (40 mm in `mm`, `cm`, `m`; `1:1`, `1:10`), export
area defined manually, visible references AB and a line at the dimension-line
height. PDF: the FreeHEP content stream is decoded (ASCII85, Flate), the device
coordinates of the stroked references and extension lines are converted
through the page transform (pt per device unit) to mm. PGF: the coordinates are
converted through `x=<n>cm`. **[observed]**

| case | PDF Gap / Overshoot (mm) | PDF resolution (mm) | PGF Gap / Overshoot (mm) | PGF resolution (mm) |
|---|---|---:|---|---:|
| mm, cm, m at 1:1 | 1.000 / 2.000 | 0.00025 | 1.000 / 2.000 | 5.5e-11 |
| mm, cm, m at 1:10 | 1.000 / 2.000 | 0.00025 | 1.000 / 2.000 | 5.5e-11 |

Deviations from 1 mm / 2 mm are below 1e-8 mm. Both methods support the
0.01 mm tolerance: the PDF writer prints five significant figures (resolution
from the printed decimals, 0.00025–0.001 mm here), the LaTeX writers twelve
(`GeoGebraExport.format`). Screenshots and screen pixels are not used.

**Separate observation (outside the P1 root cause).** The PDF stroke width of
the extension lines is one device unit, and the device resolution of the PDF
route follows the view zoom at export time: 1.0 mm at 1 px/unit, 0.25 mm at 4,
0.10 mm at 10 (`mm`, `1:1`), while the paper geometry stays 1 mm / 2 mm. With
round caps the visible ink gap is the geometric gap minus half the stroke width.
PGF writes a fixed `line width=0.8pt` (0.28 mm). Proposed for registration as
`OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM`; not investigated
further and not attributed to `E2`.

## 10. Creation-time and persistence invariants

**[observed]** For a dimension created in `mm` `1:1` (Gap 1, Overshoot 2):
zoom change, presentation-unit change, construction-unit change to `cm` and
drawing-scale change to `1:10` leave both values unchanged (no retroactive
conversion); a new dimension created afterwards in `cm` `1:10` captures its own
values (1 and 2); both parameters are independent labelled numbers (no
dependency on view, DPI or scale); save as `.cedg` and reopen under another zoom
and drawing scale restores 1 and 2 with the same labels.

## 11. Classification

```text
P1-INTENDED-PHYSICAL-CAPTURE  = CONFIRMED
P1-DEFAULT-UX-LIMITATION      = CONFIRMED
P1-CONVERSION-DEFECT          = NOT FOUND
P1-EXPORT-OR-VIEW-DISCREPANCY = NOT FOUND for Gap/Overshoot geometry
                                (separate PDF stroke-width observation, section 9)
```

- Reproduction: `constructionUnit = mm`, GeoGebra's default zoom (50 px per
  model unit) or any zoom that magnifies the physical model, and especially a
  drawing scale `1:b` with `b > 1`; tool-created dimensions.
- Observed values: Gap 1 and Overshoot 2 model units at `1:1` (10 and 20 at
  `1:10`), i.e. 50 px and 100 px on screen at the default zoom.
- Expected (analytical): identical; differences below 1e-12 relative.
- Code-level cause: the capture is paper-space (1 mm / 2 mm at the drawing
  scale) and deliberately independent of the zoom (native-dimensions §8.3,
  `DQ-E2-11`), while the default view scale is a unit-agnostic number of pixels
  per model unit. For `mm` documents that default magnifies the physical model
  about 13× on screen, so paper-sized marks look large; for `cm` about 1.3× and
  for `m` it reduces.
- Where the disproportion arises: **large on screen at the current zoom** and
  **large relative to the numerical geometry** when the same numbers are read as
  millimetres; **not** large on the exported paper (exactly 1 mm / 2 mm), and
  **not** a conversion error. Relative to the physical object, 1 mm / 2 mm are
  the specified marks; they are proportionally large only for physically small
  objects (25 % of a 40 mm object at `1:10`, where the object is 4 mm on paper).
- Affected layer: GeoCeDG Desktop creation defaults (`GeoCeDGDimensionTools`)
  against the inherited default view scale; kernel, units, persistence and
  exports are correct.
- Scope: tool-created native dimensions in physical documents; typed commands
  take explicit arguments (default 0) and are unaffected.

## 12. Design alternatives (design only; not implemented)

| Criterion | A — preserve, document | B — configurable paper defaults | C — alternative creation-time policy |
|---|---|---|---|
| geometric semantics | unchanged | unchanged (still paper targets captured once) | changes the meaning of the default (screen- or length-proportional) |
| traceability | unchanged | unchanged; the captured values remain visible numbers | the rule must be explained per dimension |
| parameter visibility | unchanged | unchanged | unchanged |
| unit conversion | unchanged | unchanged formula, user targets | proportional rules bypass the physical targets |
| DAG | none | none | none if captured once |
| persistence | none | application preference only (like B1 thickness) | none, or a policy preference |
| compatibility | full | full; existing dimensions untouched | full for existing; new behavior differs |
| Desktop GUI | guide text | two spinners in Layout & Presentation → Dimension presentation | a policy selector |
| exports | unchanged | unchanged (paper sizes = chosen targets) | sizes no longer fixed on paper |
| verification | STATIC | `BOUNDED_PHASE` (Desktop tools and preferences) | `INTEGRATED_PHASE` (normative change) |

**[recommendation]** The current behavior is correct; the problem is a
default/presentation one. Least invasive:

1. Option A now: one user-guide sentence (§9.5) saying that Gap and Overshoot
   are paper sizes at the drawing scale, so at a zoom that magnifies the model
   (typical for `mm` documents at the default zoom) they look large on screen and
   are exact on paper.
2. If the author wants control, Option B: two creation preferences (paper Gap
   and Overshoot in mm, defaults 1 and 2), captured once by the tools exactly as
   today, applied to new tool-created dimensions only. It needs an amendment of
   native-dimensions §8.3 (defaults become configurable), no kernel, XML or
   command change.
3. Option C is not recommended: a screen-proportional rule reintroduces a
   viewport dependency at creation for physical documents and contradicts the
   physical targets of `DQ-E2-11`; a length-proportional rule departs from
   drafting practice.

A view-level aid (for example a physical 1:1 zoom action) would address the
on-screen magnification for every object, not only dimensions; it belongs to a
separate view workstream.

**Proposed owner and class for Option B:** GeoCeDG Desktop
(`GeoCeDGDimensionTools`, `GeoCeDGDimensionPreferences`,
`GeoCeDGDimensionPresentationPanel`, the user guide), `BOUNDED_PHASE` with
`PHASE` acceptance, plus the native-dimensions §8.3 amendment as an author
decision.

## 13. Open questions for the author

1. Accept the classification `P1-INTENDED-PHYSICAL-CAPTURE` +
   `P1-DEFAULT-UX-LIMITATION` (no defect)?
2. Option A only, or Option B (configurable paper Gap/Overshoot)? If B: range,
   step, and application preference (recommended) versus per document.
3. Register `OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM` for a separate
   export characterization?
4. Consider a physical-zoom view aid in a separate view workstream?
5. Proceed to P2 characterization (next in the approved order)?

## 14. Verification

Committed changes: the characterization test, its JUnit selection and phase
registration (`POST-E2-P1`: compile shared/Desktop and
`junit.desktop.post-e2-p1.semantic`), the inventory refreshed in-session by the
official updater from passing producers (discovery 6046 shared / 1910 Desktop;
`post-e2-p1.desktop` 6; `final.desktop` 1898 → 1904), the registry
`junit_inventory` pin `e8d1f122…` → `f11e9c72…` and coverage literals (45
selections, 52 PHASE selections, `post-e2-p1.desktop` 6), the upstream
register entry, this report, its evidence JSON and the measured rows, and the
roadmap status. No product source, kernel, XML, command or E2 fixture changed.
`PHASE -Phase POST-E2-P1` runs on the frozen commit; its identity is reported
outside this file.

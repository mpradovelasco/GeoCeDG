# PRE-G9B-R6-plus-E2 — DQ-E2-6 focal characterization addendum (aligned dimension value)

```text
ARTIFACT_KIND             = DOCUMENTARY PREPARATION EVIDENCE (focal addendum)
TECHNICAL_CANDIDATE_STATE = FROZEN with the documentary candidate that contains it
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
AUTHORITY                 = author instruction of 2026-10-07, Part B
DOCUMENTARY BASE          = T_R6PLUS_E2_PREP 6ad853c618da8f07a279b266c83002c145c6292c
                            (product tree identical to P_R6PLUS_E1_P_X1 dc63b0e5)
RESULT                    = DQ-E2-6 ALIGNED NORMATIVE-STYLE TEXT — FEASIBLE WITH β
                            (existing output types + one bounded presentation/drawable seam;
                            no new GeoElement, no new XML element, no new semantic identity)
IMPLEMENTATION            = NOT AUTHORIZED
selfApproved              = false
passClaimed               = false
```

This addendum extends, and does not rewrite, the
[E2 preparation characterization report](pre_g9b_r6_plus_e2_preparation_characterization_report.md)
(its `K9` remains the record of what the base offers). The author requirement is
`DQ-E2-6` of the [author-decision record](pre_g9b_r6_plus_e2_author_decisions_record.md).
Machine-readable mirror:
[`pre-g9b-r6-plus-e2-dq6-aligned-text-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-dq6-aligned-text-evidence.json).
Path abbreviations as in the report.

## A1. Base capabilities re-inspected (PROVEN FROM SOURCE)

- `GeoText` has a start point, horizontal and vertical alignment
  (`common/kernel/geos/GeoText.java:123-124`, `:340-373`, `:1614-1636`) and no
  rotation property.
- `DrawText.update` computes the screen anchor from the start point, measures
  the label rectangle by drawing once, then applies `AlignDrawText` and stores
  the real-world bounding box (`common/euclidian/draw/DrawText.java:87-170`);
  `draw` paints at the aligned, axis-aligned rectangle (`:213-243`); hit testing
  uses that rectangle (`:263-276`).
- `AlignDrawText` centres the axis-aligned rectangle with fixed pixel biases
  (`MARGIN = 6`: `+3` px horizontally, `+7` px vertically for LaTeX;
  `AlignDrawText.java:43-93`).
- The LaTeX renderer supports `\rotatebox{θ}` with an origin option and
  computes the rotated box's axis-aligned bounds
  (`source/shared/renderer-base/src/main/java/com/himamis/retex/renderer/share/RotateBox.java:103-140`),
  plus `\phantom`, `\vphantom`, `\raisebox`, `\smash` and `array`
  (`…/share/commands/`).
- Every text drawable is created in one place:
  `EuclidianDraw` `case TEXT: d = new DrawText(ev, text)`
  (`common/euclidian/EuclidianDraw.java:331-333`).
- The three LaTeX exporters place a text through the overridable
  `GeoGebraExport.drawText(GeoText)` (`common/export/pstricks/GeoGebraExport.java:470`,
  `:771`) and a segment through `drawGeoSegment` (`GeoGebraToPgf.java:2318`);
  GeoCeDG already subclasses all three
  (`geocedg-desktop/export/GeoCeDGGeoGebraToPgf.java`, `…ToPstricks.java`,
  `…ToAsymptote.java`).

## A2. Reading-orientation rule

For the screen projection `P1 → P2` of the dimension line (y up, degrees):

```text
φ = atan2(P2y − P1y, P2x − P1x)                undirected line angle
θ = φ − 180 · ⌈(φ − 90) / 180⌉   ∈ (−90°, 90°]   readable half-plane
text direction = (cos θ, sin θ);  text up = (−sin θ, cos θ)
```

`θ` is invariant under `A ↔ B` (the two orientations differ by 180°). A vertical
line reads bottom to top (`θ = 90°`), the standard rule for vertical dimensions.
At the vertical boundary the reading direction, and with it the side on which
the value sits, changes by 180° (measured at 89°, 90°, 91°): a presentation-only
discontinuity inherent to any "never upside down" rule; it changes no
construction state, value or geometry.

## A3. Candidates probed

All candidates use the β construction (two extension segments, the dimension
segment, the value) and a `GeoText` anchored at the midpoint of the dimension
line, built with existing commands.

| ID | Mechanism | New type or XML |
|---|---|---|
| C1 | `Text("\rotatebox{θ}{\text{V}}", M, false, true, 0, 0)`: rotated LaTeX, start point, centred alignment | none |
| C2 | C1 with a stacked phantom row `\begin{array}{c}\text{V}\\\phantom{\text{V}}\end{array}` to lift the value off the line | none |
| C2b | C2 with a row gap `\\[0.5em]` | none |
| C4 | plain `Text(V, M, false, false, 0, 0)` painted by the existing `DrawText` through a transform: rotation `θ` about the screen anchor and a typographic lift of half the label height plus 2 px along "text up" (emulation of a bounded drawable seam) | none |

`θ` for C1–C2b is the model-coordinate angle computed by the construction
(`th = phd − 180·ceil((phd − 90)/180)`); for C4 it is computed from the screen
projection of the dimension line.

## A4. Method (PROVEN BY PROBE P6)

Scratch probe `E2PrepAlignedTextProbeTest` (two runs; scratchpad only; removed
afterwards with its build leftovers), command as in the report §2. One
`AppGeoCeDG` per test method, reset with `clearConstruction`; `JOptionPane`
statically mocked. View 800 × 600 px, 50 px per unit, axes and grid off; the
dimension segment blue, 5 px; extension segments green; the value red; every
other object hidden. Each case is painted through `EuclidianView.paint` into a
raster and measured on the red pixels:

- **parallelism** `par`: principal axis of the red pixels against the screen
  line (mod 180°). The horizontal reference itself measures −1.3° (C4) and
  −2.2° (C2), the bias of the glyph shapes, so `|par| ≤ 3.5°` is read as
  parallel;
- **longitudinal centring** `lon`: centroid minus line midpoint along the line,
  in px;
- **clearance**: red pixels within line half-width + 0.5 px of the dimension
  segment (`crossLine`) or within 2 px of an extension segment (`crossExt`), and
  the lowest red pixel along "text up" (`upMin`);
- **reading orientation**: overlap of the case with the `θ = 0` reference
  rotated by `θ` versus `θ + 180°`.

Cases: the angle sweep 0°, 30°, 44°, 45°, 46°, 89°, 90°, 91°, 135°, 179°, each
with `A → B` and `B → A`; zoom 100 px/unit; resized view 1000 × 700; values
`8 mm`, `120 mm`, `0.12 m`, `12345.67 mm`, `4.5 usm`; negative offset at 30° and
120°; anisotropic axes 50 × 100 px/unit; a 1-unit dimension with
`12345.67 mm`; and a dynamic sequence (move `A`, move `B` past the vertical,
offset −2, XML round trip through `setXML`, value change `120 mm` → `0.12 m`).

## A5. Results

| Property | C1 | C2 / C2b | C4 |
|---|---|---|---|
| parallel, isotropic view (`|par|`) | ≤ 3.20° | ≤ 3.24° | ≤ 2.36° |
| parallel, anisotropic 50 × 100 | — | **−21.03°** (model angle ≠ drawn angle) | −1.14° (`θ = 49.11°` from the drawn line) |
| longitudinal centring (`|lon|`) | ≤ 4.71 px | ≤ 4.74 px | ≤ 1.91 px |
| line crosses the characters | **always** (84–113 px) | **yes** at the default zoom (12–46 px over the sweep, up to 51 px anisotropic); 0–1 px only near the vertical; none at zoom 100 | **never** (0 px in all 37 measured cases) |
| extension lines cross the characters | no | no, except the 1-unit dimension with a long value (17 px) | no (0 px in all cases) |
| clearance `upMin` (default zoom) | text centred on the line | 0.8–2.7 px against a 2.5 px half-width | ≥ 12.0 px |
| reading orientation | readable except one inconclusive score at 0° | readable; scores inconclusive at exactly 0° and 90° (0.60/0.65, 0.52/0.67), where the rotation is exactly 0° and 90° | readable in every scored case (1.00 against ≤ 0.77) |
| `A ↔ B` | same `θ`, `lon` mirrored | same | same |
| zoom, resize, value width, unit suffix, negative side | — | as above; clearance only at zoom 100 | all pass |
| move `A`, move `B` past the vertical, offset change | — | `θ` follows the geometry; crossing remains | `θ` follows the geometry; no crossing |
| XML round trip | — | `θ` and string identical; the text string is not in XML | `θ` and string identical |
| value change `120 mm` → `0.12 m` | — | re-centred, still crossed (11 px) | re-centred (`lon` 0.28 px), parallel, no crossing |
| value string | LaTeX markup | LaTeX markup (`\rotatebox{…}{\begin{array}…}`) | the plain value (`120 mm`) |

C2b and C2 are pixel-identical: the renderer does not honour the `\\[0.5em]`
row gap, so the LaTeX-only route offers no tunable clearance through that
syntax.

## A6. Export observations (PROVEN BY PROBE P6)

| Exporter | C2 / C2b | C4 at the base (no adaptation) |
|---|---|---|
| PGF/TikZ | `node[anchor=north west] {$\rotatebox{30}{…}$}`: rotation kept, centring lost; the preamble loads `pgfplots` and `mathrsfs` but not `amsmath`, which `\text` needs | `node[anchor=north west] {120 mm}`: no rotation, no centring |
| PSTricks | `\rput[tl](…){$\red{\rotatebox{30}{…}}$}`: rotation kept, centring lost; no `amsmath` in the preamble | `\rput[tl](…){\red{120 mm}}`: no rotation, no centring |
| Asymptote | `label("$\rotatebox{30}{…}$", …, SE…)`: rotation kept, centring lost; `amsmath` loaded | `label("120 mm", …, SE…)`: no rotation, no centring |
| picture export | through the drawables (`K12`): C1/C2 as on screen | through the drawables once the seam exists (INFERRED; the export viewport uses the same drawable classes) |

In every case the value text and its unit suffix are preserved; the arrow
endings are lost in the three LaTeX dialects (`K12`). DXF stays under the
accepted `DQ-E2-9` limitation (segments only, text excluded with a diagnostic).

## A7. Conclusion

- **C1** fails "not crossed by the dimension line" everywhere.
- **C2 / C2b** (existing types and no code) satisfy parallelism, centring and
  reading orientation on isotropic views but fail clearance at ordinary zoom,
  follow the model angle instead of the drawn figure on anisotropic views, put
  LaTeX markup into the value string, and need a LaTeX package the PGF and
  PSTricks preambles do not load. Rejected.
- **C4** satisfies every measured property of `DQ-E2-6`. It needs no new
  `GeoElement`, XML element or semantic identity: the text output stays an
  ordinary dependent `GeoText` holding the plain value string, and its rotation
  and lift are derived at draw time from the parent algorithm's dimension line,
  never stored. It does need one bounded presentation seam.

```text
DQ-E2-6   = ALIGNED NORMATIVE-STYLE TEXT — FEASIBLE WITH β (C4 seam)
DQ-E2-1   = β remains approved
verificationClass = INTEGRATED_PHASE remains approved
```

## A8. The bounded seam C4 requires (implementation preparation, not authorized)

1. A GeoCeDG interface in the shared kernel (for example
   `org.geocedg.common.kernel.dimension.DimensionTextGeometry`) implemented by
   the two dimension algorithms, giving their text output's dimension-line end
   points in model coordinates and the overshoot side. No new geo type.
2. In `EuclidianDraw` `case TEXT`, one routing change: a GeoCeDG drawable
   (`DrawDimensionText extends DrawText`) only when the text's parent algorithm
   implements that interface; every other text keeps `DrawText` unchanged
   (Classic and existing documents unaffected).
3. `DrawDimensionText`: screen angle from the projected end points, the rule of
   §A2, rotation about the screen anchor, a typographic lift of half the label
   height plus a fixed clearance in px; when the projected value is longer than
   the dimension line, the lift also clears the overshoot tips (so extension
   lines never cross the characters); hit testing, highlight, background fill
   and the `Corner` bounding box on the rotated rectangle.
4. The three GeoCeDG LaTeX exporters override `drawText` and
   `drawGeoSegment` **for dimension outputs only**: rotation by the export
   angle (computed with the export's `xunit`/`yunit`), centred anchoring with
   the same lift (for example PGF `node[rotate=θ, anchor=south]`, PSTricks
   `\rput[b]{θ}`, Asymptote `label(rotate(θ)*…)`), and the arrow endings of the
   dimension segment. Other segments and texts keep their current output.
5. Obligations that the probe did not execute and the implementation must
   prove: picture export at several DPI and scales through the export
   viewport; hit testing and selection of the rotated value; the `Corner`
   bounding box; undo and redo through the ordinary snapshot route; the LaTeX
   adaptation compiled or at least syntax-checked for each dialect.

The seam changes no serialization, no reader, no undo snapshot and no kernel
semantics; the shared-renderer change is gated to `E2` outputs. It therefore
stays within `INTEGRATED_PHASE`. A change that alters how any non-dimension
text is drawn, hit or exported is an escalation condition.

## A9. Residual observations

| Identifier | Status | Owner proposed |
|---|---|---|
| `OBS-R6PLUS-E2-READING-FLIP-AT-VERTICAL` | presentation-only 180° change of reading direction and value side at the vertical (§A2) | `E2` (document in the specification and guides) |
| `OBS-R6PLUS-E2-VALUE-LONGER-THAN-DIMENSION-LINE` | a value longer than its dimension line can meet the extension tips; handled by the lift rule of §A8.3; outside placement of the value is not in `E2` | `E2` |
| `OBS-R6PLUS-E2-LATEX-TEXT-PACKAGE` | `\text` in exported LaTeX needs `amsmath`, absent from the PGF and PSTricks preambles; C4 emits plain text and avoids it | `E2` (avoid LaTeX markup in the value) |
| `OBS-R6PLUS-E2-DXF-DIMENSION-SUBSET` | durable `E2` limitation per `DQ-E2-9`: DXF carries the dimension line and the two extension lines as `LINE`; no arrowheads, no value, no `DIMENSION` entity | `E2` (record in the specification and guides) |

## A10. Tracked delta and cleanup

Documentation, evidence and prompt only (listed in the JSON mirror). The probe
class lived in the session scratchpad; after the runs the Desktop test classes
were recompiled without it and every probe class and result file was removed
from `source/desktop/desktop/build`.

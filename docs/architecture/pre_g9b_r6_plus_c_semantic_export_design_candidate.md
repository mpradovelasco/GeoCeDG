# PRE-G9B-R6-plus-C — 2D export completion and semantic curve exporters: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `C` needs its own
  explicit author authorization, after `A`, `B` and `D1`.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  findings 7, 9, 10 and probe P5 (rotated text in PGF)

Path abbreviations as in the P0 report (`common/`, `geocedg-common/`).

## 1. Author-fixed scope

`LocusV2` and `SplineV2` in the applicable LaTeX and vector exporters, from
semantic geometry:

```text
semantic/parametric geometry != render tessellation
export adapter consumes semantic geometry; the kernel is not an export formatter
```

`C` also completes the 2D export contract: engineering scale (`1:n`, `n:1`)
replaces "Scale in cm", DXF carries the unit header, and every exporter agrees
on one `ExportArea` (`B`), one unit contract (`D1`) and one effective-visibility
rule (`A`).

## 2. What the base does

- `GeoLocusV2 extends GeoElement` and deliberately implements neither `Path` nor
  `GeoLocus` (`geocedg-common/kernel/geos/GeoLocusV2.java:28-33`). The LaTeX
  dispatcher therefore falls to "Export: unsupported GeoElement" at debug level
  and emits nothing (`common/export/pstricks/GeoGebraExport.java:453-460`).
  PSTricks, PGF/TikZ and Asymptote silently omit every Locus V2 and Spline V2.
- Picture exporters render Locus V2 from its screen tessellation, 0.75 view
  pixels (`DrawLocusV2`, `LocusRenderPolicy2D.java:20-23`), so print accuracy
  depends on zoom. `B`'s export viewport re-tessellates at export resolution.
- DXF already exports Locus V2 semantically, in model coordinates, as one
  approximate `LWPOLYLINE` per certified valid branch component with explicit
  tolerance and a sidecar manifest
  (`geocedg-common/export/G9X1GeometryExportAdapter.java:242-243`, `:288-475`;
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/export/DxfPairedOutputWriter.java:34`, `:154-161`).
  This is the model for `C`.
- `SplineV2` produces a `GeoLocusV2` (`AlgoSplineV2 extends AlgoLocusV2`) and
  carries a viewport-independent piecewise-polynomial model: knots, per-span
  coefficients, degree up to 12 (`geocedg-common/kernel/spline/SplinePolynomialModel2D.java:20-31`).
  The model states that rounded power spans are "derived evaluation data, not
  exact coefficient authority".
- LaTeX bounds come from the selection rectangle or the visible view, never
  from `Export_1`/`Export_2` (`GeoGebraExport.java:240-258`). Elements are
  filtered by `isEuclidianVisible()` (`:356-357`) and iterated in construction
  order.
- PGF exports rotated text as a `\rotatebox` inside a node (probe P5).

## 3. Candidate: one semantic curve adapter for LaTeX

- A read-only `SemanticCurveExportAdapter` in the shared GeoCeDG export package.
  It produces, for each `GeoLocusV2`, the same certified component
  approximations that the DXF G9X1 adapter produces, through
  `AdaptiveCurveApproximationBuilder2D`, in **model coordinates**, with an
  explicit model-unit tolerance. It is clipped to the `ExportArea` world bounds,
  never to the view.
- Per-format writers:
  - PGF/TikZ: `\draw plot coordinates {…}`, one path per component;
  - PSTricks: `\psline` (or `\pscurve` only if `C` proves fidelity);
  - Asymptote: `draw((x,y)--…)`.
  Each writer emits a comment with the tolerance and `APPROXIMATE` exactness.
- The `GeoGebraExport` dispatcher gains one branch for `GeoLocusV2` that calls
  the adapter. Formatting stays in the writers; the kernel only answers
  semantic evaluation.
- **Option (`AQ-C1`):** exact cubic Bézier output for `SplineV2` of degree ≤ 3
  in PGF (`.. controls ..`), converted from the polynomial model. Recommended:
  **not** in `C`. The model disclaims exact-coefficient authority for its
  rounded power spans, and one approximation contract for all Locus V2 curves
  keeps DXF, LaTeX and picture output consistent.
- Open domains and invalid components fail explicitly, on the
  `MISSING_DOMAIN` precedent (`AdaptiveCurveApproximationBuilder2D.java:257-259`),
  with a visible export report rather than a silent omission.

## 4. Engineering scale and units

- Picture and LaTeX dialogs show a scale `1:n` / `n:1` (`drawingScale`) instead
  of "units = … cm" (`desktop/export/PrintScalePanel.java:118`, `:140-145`).
  Paper size follows `L·k(c→mm)/n`, which is invariant under an equivalent
  `presentationUnit` change (`AQ-U1`).
- LaTeX `xunit = yunit = k(c→cm)/n` cm per model unit.
- DXF `$INSUNITS` from `constructionUnit` (`DxfExporter.java:108-110`); `0`
  while unspecified. The export foundation amendment is `D0`'s.
- With an unspecified unit, engineering-scale export follows `D0`'s `AQ-U6`
  decision, never an inferred `cm`.

## 5. Export area and visibility

- LaTeX frames initialize their bounds from `ExportArea`. Editing a bound
  writes a `MANUAL` producer, not the selection rectangle (`B`).
- Effective visibility (`A`): LaTeX excludes effectively hidden objects
  (recommended, `AQ-L3`). For DXF, `C` decides between omitting hidden-layer
  objects and writing them on their layer with the layer **off** in the
  `LAYER` table. Objects hidden individually keep the current `60 = 1`
  behavior (`DxfExporter.java:300-306`), and `OBS-R6P-DXF-VISIBLE-CONTRACT`
  (the "visible geometry" contract name versus hidden objects exported) is
  resolved in `C`.

## 6. Serialization and compatibility

None in documents. Output files change: LaTeX gains curves it silently omitted,
and DXF gains a unit header when a unit is declared.

## 7. Verification-class recommendation

`INTEGRATED_PHASE` (registered `PHASE` plus `INTEGRATION`): the shared LaTeX
export package, the DXF header under an amended accepted specification, and
agreement of three LaTeX exporters, the picture exporters and DXF on one area,
one unit contract and one visibility rule. It would escalate if the export area
or scale became document-serialized.

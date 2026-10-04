# PRE-G9B-R6-plus-C author-smoke package

Prepared documents and one read-only inspection helper for the author smoke of
`PRE-G9B-R6-plus-C` (2D export completion). The checklist itself is §18 of the
[candidate report](../../../docs/validation/pre_g9b_r6_plus_c_candidate_report.md).
Nothing here is acceptance evidence, and the agent performs no author smoke.

The documents were built through the product API and written by the native
`.cedg` writer of the candidate; none was edited by hand. They carry no drawing
scale, tolerance or export area of their own: those are session values chosen
during the smoke (`DQ-C7`, `DQ-C13`). `Export_1`/`Export_2` define the explicit
export area of the documents that contain them.

| Fixture | Construction unit | Content |
|---|---|---|
| `fixtures/c-physical-cm.cedg` | `cm` | 10 × 5 rectangle, radius-2 circle, 10-unit ruler; area 18 × 8 units |
| `fixtures/c-physical-cm-presentation-mm.cedg` | `cm`, presentation `mm` | the same model; identical physical sizes expected |
| `fixtures/c-units-mm.cedg` | `mm` | the same model; DXF `$INSUNITS 4` |
| `fixtures/c-units-m.cedg` | `m` | the same model; DXF `$INSUNITS 6` |
| `fixtures/c-units-usm-inch.cedg` | `usm` 0.0254 m (`inch`, `in`) | the same model; DXF `$INSUNITS 0`, warning and paired sidecar |
| `fixtures/c-unspecified.cedg` | none | the same model; device mode, DXF `$INSUNITS 0` |
| `fixtures/c-layers-and-area.cedg` | `cm` | objects on layers 0, 3 and 12, layer 3 hidden in the document, one individually hidden circle, a crossing segment, a line, `far` outside the area 7 × 9 units |
| `fixtures/c-semantic-curves.cedg` | `cm` | Locus V2 `L` (two components) and `C` (closed), Spline V2 `S3` and `S5`, legacy `Locus` |
| `../pre-g9b-s1-dxf/original/TestExport1.cedg` | as stored | Locus V2 `m` with locally certified components and global coverage not established |

`inspect-export.ps1` prints, for exported files, the facts the checklist asks
about: DXF `$INSUNITS`, GeoCeDG `999` comments, the `LAYER` table (`62 < 0` is
OFF), entities per layer and with group `60 = 1`; the sidecar schema, units,
layers, export area and outcomes; the EMF `rclFrame` in millimetres; the PDF
`MediaBox`; SVG `width`, `height` and `viewBox`; PNG pixels and DPI; the
GeoCeDG comment and unit lines of LaTeX output.

```powershell
pwsh -NoProfile -File models\regression\pre-g9b-r6-plus-c-export-completion\inspect-export.ps1 out.dxf out.dxf.manifest.json out.emf out.pdf out.svg out.png out.tex
```

# PRE-G9B-R6-plus-B author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS
DECISION_DATE            = 2026-10-02
DECIDED_ON_BASE          = d32ad608ba8821bc18c9d4dc783c1b700a165ff2
                           tree 9b123be7ed8c7176b5a7e19f74be5946f0164c8b
                           (P_R6PLUS_A1, published A-1 closeout)

selfApproved             = false
implementationAuthorized = false   (B and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the author decisions of 2026-10-02 for
`PRE-G9B-R6-plus-B`. It is the authority for them. Its machine-readable mirror
is
[`pre-g9b-r6-plus-b-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-b-author-decisions.json).

The author stated that these decisions **do not authorize implementation**.
They answer the open decisions `AQ-X1` to `AQ-X7` of the
[P0 candidate report](pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15. Every other open decision of that report stays open. The frozen P0
candidate report and the
[export-area design candidate](../architecture/pre_g9b_r6_plus_b_export_area_design_candidate.md)
are not amended. Where this record differs from that candidate, this record
prevails.

The same instruction authorized only the documentary preparation of the
canonical `B` prompt, this record and the status and decomposition updates
they require.

## Decisions

### `AQ-X1` — exact and complete rendering

The primary strategy is an **offscreen export viewport** whose geometry and
coordinate system represent the `ExportArea` at the output resolution. It must
produce:

- the exact rectangle;
- zero outside padding;
- complete content even when the area lies partly or wholly outside the
  visible viewport;
- an appropriate recomputation and rendering, for the export viewport, of
  points, functions, axes, grid, labels, legacy loci and Locus V2;
- SVG based on the export area, not on the visible viewport.

Silently substituting this architecture with distributed per-drawable
modifications is not authorized. If the offscreen viewport is not viable
without a material widening of the shared renderer or of the verification
class, the future work of `B` stops and requests escalation.

### `AQ-X2` — `ExportArea` authority

```text
precedence = explicitly activated producer > Export_1/Export_2 > visible viewport
```

- The upstream `selectionRectangle` is never an `ExportArea` authority and
  never silently displaces `Export_1`/`Export_2`.
- `ExportArea` is `SESSION`: it is not serialized, it is not part of undo, and
  it resets on New and Open. After New or Open, `Export_1`/`Export_2` act again
  automatically when both valid points exist.
- Producers implemented in `B`: `EXPORT_POINTS` and `MANUAL`.
- `ISO_A_SELECTION` and `ISO_A_BORDER` belong to `E3` (`AQ-X7`).

### `AQ-X3` — export surface and hidden routes

The GeoCeDG surface exposes:

| Entry | Content |
|---|---|
| `Graphics View as Picture…` | PNG, PDF, SVG and EMF/EMF+ |
| `DXF…` | DXF |
| `PSTricks…` | PSTricks |
| `PGF/TikZ…` | PGF/TikZ |
| `Asymptote…` | Asymptote |
| `Print Preview` | print preview |

Not exposed: STL, Collada, HTML Collada, Dynamic Worksheet / worksheet upload,
Animated GIF.

| Route | Decision |
|---|---|
| `Ctrl+Shift+U` | the GeoCeDG Picture surface |
| `Ctrl+Shift+C` | the same Picture surface / graphics-clipboard service; no second direct route with its own authority |
| `Ctrl+Shift+W` | consumed; no Worksheet |
| `Ctrl+Shift+M` | consumed; no HTML5 export |
| `Ctrl+Shift+D` | GeoCeDG DXF only; the observed upstream double effect is removed |
| `Ctrl+Shift+B` | kept: it copies the document as Base64 and is not a graphics export |

The v1 fallback also filters STL, Collada, HTML Collada and Worksheet. A
fallback mode that reopens an expressly excluded surface is not accepted.

### `AQ-X4` — clipboard, GIF and Print Preview

- Print Preview stays and obeys the `ExportArea` authority.
- The graphics clipboard stays as a consumer of the same Picture/export
  service and of the same `ExportArea`, not as a second mechanism.
- Animated GIF is out of this generation.

### `AQ-X5` — command line

- `--export` stays supported in GeoCeDG and uses the same export service and
  `ExportArea`.
- `--exportAnimation` is rejected explicitly while Animated GIF stays out.

A command-line route with a different area authority is not accepted.

### `AQ-X6` — `ExportImage` and the API

GGBScript `ExportImage` and the relevant graphics APIs obey the same
`ExportArea` authority. `ExportImage` becomes a **GeoCeDG-modified command**
and enters the GGBScript compatibility matrix and its tests.

### `AQ-X7` — ISO producers

`ISO_A_SELECTION` and `ISO_A_BORDER` are not implemented in `B`. They are
delivered in `E3`, after the `D1` unit contract.

## Inherited author-smoke observation

`B` incorporates as an explicit requirement the residual observation of the
[A-1 closeout record](pre_g9b_r6_plus_a1_closeout_record.md):

```text
OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS
preview effective visibility == final export effective visibility
```

The `Graphics View as Picture` preview respects the `A-1` hidden layers exactly
as the final file does. The case observed in `A-1`, where the preview shows a
hidden layer and the final PNG does not, is corrected in `B`.

`OBS-A1-BACKGROUND-IMAGE-LAYER` is re-characterized by `B` with respect to
preview and export. `B` does not silently change the general layer semantics of
the normal view to resolve it. If resolving it requires widening that
semantics, `B` documents it and defers it, or requests an author decision.

## Scope boundary between `B` and `C`

`B` may expose PSTricks, PGF/TikZ, Asymptote and DXF in the File/Export
surface, but it does not anticipate `C`. The following stay in `C`:

- the final integration of the LaTeX exporters with `ExportArea`;
- the DXF policy for hidden layers and clipping by `ExportArea`;
- semantic export of Locus V2 / Spline V2 to PSTricks, PGF/TikZ and Asymptote;
- the unit and engineering-scale contract that depends on `D1`.

`B` documents the temporary inconsistency that remains between Picture and
those exporters.

## Other requirements fixed by the author

`B` must cover at least: exact dimensions without the current `+2`; areas
inside the viewport, larger than the viewport and completely disjoint from it;
PNG, PDF, SVG and EMF/EMF+; no integer truncation of the PDF frame where it
affects the exactness of the area; points; lines, segments, rays and vectors;
conics; polygons; functions; axes; grid; labels and text; legacy Locus; Locus V2
rendered at export resolution; hidden layers; preview = final;
`selectionRectangle` ignored as an authority; the `Export_1`/`Export_2`
fallback; the lifecycle of the `MANUAL` producer; an `ExportArea` display
overlay that never appears in the export; shortcuts; Print Preview; clipboard;
command line; `ExportImage`; the relevant APIs; the v1 fallback; absence of
serialization; compatibility of legacy documents.

The `ExportArea` overlay is transient presentation, not geometry and not a
construction object.

## Verification class proposed for authorization

```text
VERIFICATION_CLASS  = INTEGRATED_PHASE        (frozen as a proposal; formally
                                               frozen when B is authorized)
PLANNED_ACCEPTANCE  = registered PHASE -Phase PRE-G9B-R6-plus-B
                      + INTEGRATION
                      on exactly the same commit and tree
FINAL               = not run, unless a prior stop or escalation requires it
RECLASSIFY_IF       = any serialization of ExportArea, or any unplanned
                      global widening of the renderer: stop and reclassify
                      before continuing
```

Log-root rule, correcting the lesson of `A-1` from the start:

```text
PHASE and STATIC:      -LogDirectory may be a fresh root outside the repository
INTEGRATION and FINAL: -LogDirectory must be artifacts/agent/<fresh> inside the
                       repository
console logs:          outside artifacts/
```

## What stays open

- The additional decisions the preparation identified, listed in the
  [canonical `B` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-b-export-surface-and-export-area.prompt.md)
  under *Decisions requested before authorization*. The prompt fixes a default
  contract for each. The authorizing instruction may confirm or replace it.
- `AQ-L7` (3) before `A-2`; the DXF hidden-layer policy in `C`.
- Every other open decision of the P0 candidate report §15.

The candidate action identifiers, the `MANUAL` definition gesture, the
treatment of screen-anchored objects, the per-view scope of the producers and
the status messages are contracts of the prepared prompt, not author
decisions.

## Authorization state

```text
PRE-G9B-R6-plus-P0   = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-1  = PASS — AUTHOR APPROVED — PUBLISHED   (AUTHOR_SMOKE = PASS)
PRE-G9B-R6-plus-B    = PREPARED — NOT AUTHORIZED   (next operational subphase;
                       canonical prompt prepared)
PRE-G9B-R6-plus-D0, D1, A-2, C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R7           = DESIGNED — NOT AUTHORIZED
                       BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                  = NOT AUTHORIZED
```

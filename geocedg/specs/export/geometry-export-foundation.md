# G5 neutral 2D geometry export and DXF contract

- Status: Experimental
- Feature: `cedg.export.dxf.2d`
- Decision: `docs/adr/0005-neutral-2d-geometry-export.md`
- Format profile: ASCII DXF AC1015, model space, Cartesian XY, `z = 0`
- Amended by `PRE-G9B-R6-plus-C` (2026-10-04, technical candidate pending
  author review): units, hidden layers, the explicit export area and the G5
  PASS clause below, under the author decisions `DQ-C2`, `DQ-C3`, `DQ-C11`,
  `DQ-C13` and `DQ-C17`

## Boundary

```text
resolved 2D GeoElements
  -> GeoElementGeometryExportAdapter
  -> immutable GeometryExportModel
  -> DxfExporter
  -> DXF group-code stream
```

Only the adapter may depend on GeoGebra geometry classes. The neutral model and
DXF writer are read-only export services; they do not add algorithms, change
construction dependencies, solve geometry, mutate `GeoElement`, or serialize
new `.ggb` state.

## Model contract

Every neutral entity records:

- a deterministic source identifier scoped to the construction revision;
- source type and optional label;
- normalized layer;
- RGB color and current object visibility;
- geometric entity type and world-coordinate parameters;
- exact or approximate representation status;
- optional approximation tolerance.

The model records selection mode, source coordinate system, source unit, target
unit, the document export context (unit state, persistent hidden-layer set and
consumed export area), the sources and certified components outside an explicit
export area, and diagnostics for all skipped or typed-outside-population
objects. A complete-construction request applies `geocedg-dxf-geometric-2d/v1`
before adaptation; current selection remains unfiltered. The coordinate system
is `GEOGEBRA_CARTESIAN_2D_WORLD` with the identity transform; the unit is the
effective construction unit (below).

## G5 type policy

| GeoGebra source | Neutral geometry | DXF | G5 policy |
|---|---|---|---|
| finite 2D point | point | `POINT` | exact |
| segment | bounded line | `LINE` | exact |
| ray | origin + unit direction | `RAY` | exact |
| line | point + unit direction | `XLINE` | exact |
| circle | center + radius | `CIRCLE` | exact |
| circular arc | center + radius + oriented bounds | `ARC` | exact |
| ellipse | center + major vector + ratio | `ELLIPSE` | exact |
| elliptic arc | ellipse plus parameter interval | `ELLIPSE` | exact |
| polygon | ordered closed vertices | `LWPOLYLINE` | exact boundary |
| polyline | ordered vertices | `LWPOLYLINE` | exact |
| sector | none | none | unsupported |
| parabola/hyperbola/degenerate conic | none | none | unsupported |
| function/parametric/implicit curve | none | none | unsupported |
| legacy `Locus` | none | none | unsupported |
| text/image/widget/3D object | none | none | unsupported |

Polygon fill is not exported. When a polygon and its generated side segments
are in the same input set, the polygon boundary is authoritative and those
generated segments are suppressed to prevent duplicate geometry.

## Infinite geometry

`GeoLine` and `GeoRay` map to `XLINE` and `RAY`. Their directions are
normalized. View limits are never read. There is no viewport-clipped mode and
no geometric clipping of any family (below).

## Explicit export area (`PRE-G9B-R6-plus-C`, `DQ-C13`, choice `B1`)

The DXF consumes the single session `ExportArea` of the active 2D Graphics view
(`PRE-G9B-R6-plus-B`) by participation, never by clipping, under the versioned
rule `geocedg-export-area-participation-b1/v1`:

- only the explicit producers `MANUAL`, `EXPORT_POINTS_EXPLICIT` and
  `EXPORT_POINTS_AUTOMATIC` define a closed participation area `R`; the
  `VISIBLE_VIEWPORT` fallback is no DXF boundary, so the export keeps its
  model-space population and never depends on the zoom;
- a source, entity or certified component participates when its exported
  geometric support meets `R` and is then emitted whole, with its exact entity
  family and parameters; complete-construction and current-selection requests
  apply the same rule to their candidates;
- exact entities are decided by closed-form predicates on their boundaries,
  never on filled regions: points by closed inclusion; segments, polyline and
  polygon edges, rays and lines by exact separating-axis sign tests; circles by
  exact squared distances; circular and elliptic arcs by closed-form edge
  intersections in binary64 with ties resolved toward participation;
- a Locus V2 or Spline V2 certified component is left out only when the
  kernel's ADR 0028 certified interval curve model proves it disjoint from `R`
  (outward enclosures over a deterministic bisection with a bounded budget);
  without a certified model, or without a proof within the budget, it
  participates whole; approximable sources without a certified model
  participate whole;
- a non-participating source or component is an `OUTSIDE_EXPORT_AREA`
  population outcome, never unsupported, invalid or a fidelity reduction; it is
  counted in preflight and the report, recorded in the sidecar when one is
  written, stated by a DXF header comment (group `999`), and the explicit area
  enters the staleness check; area filtering alone never makes the sidecar
  mandatory; a missing semantic domain is never replaced by the area.

## Units, scale, and coordinate system

The document/application unit contract is the
[GeoCeDG unit system](../units/unit-system.md) §15.2, implemented by
`PRE-G9B-R6-plus-C` (`DQ-C11`). The neutral model carries the effective
construction unit and, for `usm`, its binary64 factor; coordinates are always
written numerically unchanged with the identity transform:

| Effective construction unit | Neutral unit | DXF `$INSUNITS` (group 70) | Sidecar |
|---|---|---|---|
| `UNSPECIFIED_MODEL_UNIT` | `UNITLESS` | `0` | when fidelity requires it; `unitless`, no factor |
| `mm` | `MM` | `4` | when fidelity requires it; unit metadata `mm` |
| `cm` | `CM` | `5` | when fidelity requires it; unit metadata `cm` |
| `m` | `M` | `6` | when fidelity requires it; unit metadata `m` |
| `usm` | `USM` | `0`, with a custom-unit header comment and warning | **mandatory**, with the token `usm` and the canonical `metersPerUnit` |

The codes are those of the Autodesk DXF header reference. `$MEASUREMENT` and
`$LUNITS` are not written. `presentationUnit`, zoom, DPI, export image scale,
printing scale, the drawing scale and window bounds are excluded from the
service API; screen scale is never reinterpreted as model scale. The unit
state, the hidden-layer set and the explicit export area enter the staleness
check of a pending preflight. The default exact (G5) Desktop flow refuses a
`usm` document, because it writes no sidecar.

## Layers and style

The exported population is the requested geometric population: individually
hidden objects and objects on hidden GeoCeDG layers are included; their hidden
states are transported, never used to drop geometry (`DQ-C2`, `DQ-C3`).

- layer `0` -> DXF `0`;
- nonzero layer `n` -> DXF `GEOCEDG_L<n>`;
- a GeoCeDG layer in the persistent hidden-layer set (`PRE-G9B-R6-plus-A-2`) ->
  its DXF `LAYER` record is written OFF (negative color group `62 = -7`); its
  objects stay in the file on that layer;
- object RGB -> DXF true-color group `420`;
- individually hidden object -> DXF visibility group `60 = 1`; an object both
  hidden and on a hidden layer carries both mechanisms;
- construction-revision source identifier -> DXF comment group `999`;
- line thickness, point size, fill, opacity, and dash style are not transported
  in G5.

This mapping preserves the current flat integer as minimal metadata. It is not
the future hierarchical GeoCeDG layer architecture.

## Diagnostics and failure

Undefined, non-finite, 3D, degenerate, and unsupported objects produce a
diagnostic tied to their source identifier. The writer rejects non-finite
coordinates and approximate entities without a positive finite tolerance.
The Desktop controller writes no file when the neutral model contains zero
exportable entities.

Lists, numeric parameters, rich Locus intersection results and Text have no
approved geometric DXF source role in the version-one complete population.
They are excluded before strict geometry preflight with a distinct typed
diagnostic. Unknown or genuinely geometric unsupported families are not
silently excluded and continue to block a strict complete request.

## Determinism and validation

Entity order is source construction/selection order after deterministic
polygon-side suppression. Layer declarations and handles are deterministic.
The DXF contains no timestamps. Validation compares parsed entities and their
geometry, not only bytes.

A G5 PASS requires:

- exact entity counts and types;
- coordinate, radius, angle, and polyline-closure invariants;
- `$ACADVER = AC1015` and `$INSUNITS = 0` for a document with
  `UNSPECIFIED_MODEL_UNIT` (the G5 corpus), otherwise the mapped code of the
  effective construction unit;
- deterministic layer/style mapping;
- semantic equality across zoom changes, also when no explicit export area
  exists (`VISIBLE_VIEWPORT`);
- explicit unsupported diagnostics;
- GUI export in GeoCeDG and unchanged Classic availability.

No tolerance beyond floating-point text round-trip is permitted for exact G5
entities. G5 defines no approximate source adapter.

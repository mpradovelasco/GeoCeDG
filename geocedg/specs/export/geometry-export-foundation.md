# G5 neutral 2D geometry export and DXF contract

- Status: Experimental
- Feature: `cedg.export.dxf.2d`
- Decision: `docs/adr/0005-neutral-2d-geometry-export.md`
- Format profile: ASCII DXF AC1015, model space, Cartesian XY, `z = 0`

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
unit, and diagnostics for all skipped objects. G5 uses the coordinate system
`GEOGEBRA_CARTESIAN_2D_WORLD`, identity transform, and `UNITLESS` units.

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
normalized. View limits are never read. G5 has no viewport-clipped mode.

## Units, scale, and coordinate system

GeoGebra Classic constructions do not provide a G5-approved physical model
unit. The source unit is therefore `UNITLESS`, the identity transform is used,
and DXF `$INSUNITS` is `0`. Zoom, DPI, export image scale, printing scale, and
window bounds are excluded from the service API.

Future physical-unit support requires an explicit document/application
contract. It must not reinterpret screen scale as model scale.

## Layers and style

- layer `0` -> DXF `0`;
- nonzero layer `n` -> DXF `GEOCEDG_L<n>`;
- object RGB -> DXF true-color group `420`;
- hidden object -> DXF visibility group `60 = 1`;
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

## Determinism and validation

Entity order is source construction/selection order after deterministic
polygon-side suppression. Layer declarations and handles are deterministic.
The DXF contains no timestamps. Validation compares parsed entities and their
geometry, not only bytes.

A G5 PASS requires:

- exact entity counts and types;
- coordinate, radius, angle, and polyline-closure invariants;
- `$ACADVER = AC1015` and `$INSUNITS = 0`;
- deterministic layer/style mapping;
- semantic equality across zoom changes;
- explicit unsupported diagnostics;
- GUI export in GeoCeDG and unchanged Classic availability.

No tolerance beyond floating-point text round-trip is permitted for exact G5
entities. G5 defines no approximate source adapter.

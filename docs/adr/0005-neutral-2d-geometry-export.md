# ADR 0005: neutral 2D geometry export boundary and DXF AC1015

- Status: **Accepted for G5 experimental implementation**
- Date: 2026-08-10
- Scope: read-only 2D export, DXF encoding, and GeoCeDG Desktop access

## Context

The pinned GeoGebra baseline has no DXF exporter. Its PNG, SVG, PDF, and EMF
paths repaint a `EuclidianView`; PDF also combines printing scale and view
scale. PSTricks, PGF, and Asymptote exporters inspect construction objects but
remain coupled to view bounds and format-specific decisions. STL and Collada
are 3D renderer exports. None is a neutral, world-coordinate geometry model
suitable as the authority for a reusable GeoCeDG format pipeline.

G5 must export already-resolved 2D objects without changing their kernel
meaning, their dependency graph, or `.ggb` serialization. It must also keep
screen zoom and viewport bounds out of model geometry.

## Decision

1. The shared GeoCeDG export package provides an immutable
   `GeometryExportModel`. A `GeoElementGeometryExportAdapter` is the only G5
   component allowed to inspect GeoGebra geometry classes.
2. Format exporters consume only that neutral model. `DxfExporter` has no
   dependency on `GeoElement`, `Kernel`, views, dialogs, or Desktop classes.
3. G5 exports model-space Cartesian coordinates with `z = 0`. Source and DXF
   units are explicitly `unitless`; `$INSUNITS` is `0`. No physical unit or
   drawing scale is inferred.
4. The first format is deterministic ASCII DXF AC1015 (AutoCAD 2000). This
   version supports `LWPOLYLINE`, `RAY`, `XLINE`, and `ELLIPSE` while retaining
   a small pair-based writer. Autodesk records `AC1015` as AutoCAD 2000 and
   `$INSUNITS = 0` as unitless in its
   [DXF header reference](https://help.autodesk.com/cloudhelp/2021/ENU/AutoCAD-DXF/files/GUID-A85E8E67-27CD-4C59-BE61-4DC9FADBE74A.htm).
5. Exact G5 mappings are `POINT`, `LINE`, `RAY`, `XLINE`, `CIRCLE`, `ARC`,
   `ELLIPSE`, and `LWPOLYLINE`. Infinite objects use DXF-native entities and
   are never clipped to the current viewport.
6. General functions, parametric curves, implicit curves, text, images,
   sectors, unsupported conics, 3D objects, and legacy `Locus` are reported as
   unsupported. G5 emits no polygonal approximation. The model nevertheless
   carries an exact/approximate field so later approved adapters can add an
   explicit tolerance without changing the DXF writer boundary.
7. The initial selection modes are the complete labeled 2D construction and
   the explicit current selection. Selection is resolved before adaptation;
   it is not geometric logic.
8. The current integer GeoGebra layer maps deterministically to DXF layer `0`
   or `GEOCEDG_L<n>`. True RGB color and object visibility are transported.
   A DXF comment records the construction-revision source identifier for
   validation provenance. Line thickness and dash style are not converted
   because their current values are presentation/pixel conventions, not
   physical geometry.
9. A GeoCeDG-only menu invokes a Desktop controller that chooses selection
   mode and destination, writes the DXF, and presents every skipped-object
   diagnostic. The upstream Classic menu remains unchanged.
10. No third-party DXF dependency is introduced. Structural and geometric
    tests parse the emitted group-code pairs semantically.

## Consequences

- Future SVG, PDF, or other geometric exporters can consume the same neutral
  representation without reinterpreting GeoGebra objects.
- Zoom, DPI, printing scale, and viewport changes cannot affect a G5 DXF.
- Unsupported geometry is explicit and cannot silently become a polyline.
- Source identifiers are deterministic within a construction revision. They
  use construction position plus serialized label; G5 does not claim a new
  persistent object-identity contract.
- G5 layer mapping is a compatibility adapter, not the future GeoCeDG layer
  architecture.
- The writer is intentionally limited to 2D model space and unitless input.

## Rejected alternatives

### Repaint the Graphics view into DXF

Rejected because drawables, clipping, viewport, DPI, and screen tessellation
are presentation data rather than geometric authority.

### Extend the PSTricks exporter directly

Rejected because it combines object inspection, view bounds, approximation,
and format emission. Reusing it would preserve the coupling G5 is intended to
remove.

### Convert every curve to a polyline

Rejected because it would hide approximation and would make legacy `Locus`
samples appear semantically exact before Locus V2.

### Add a large DXF library

Rejected because the selected entity set and group-code structure are small,
and no external dependency is needed for deterministic emission or testing.

### Persist export metadata in `.ggb`

Rejected because G5 requires no change to construction serialization. Export
metadata belongs to the generated model and artifact evidence.

## Validation

- adapter tests for every supported family and every explicit unsupported
  family;
- semantic DXF parsing of entity type, coordinates, dimensions, units, layers,
  color, visibility, and source correspondence;
- equality of semantic output before and after zoom changes;
- focused G5 verifier subordinate to `tools/agent/verify.ps1`;
- GeoCeDG GUI/manual export and Classic launch regression;
- shared/Desktop build, manifests, packaging composition, and whitespace gates.

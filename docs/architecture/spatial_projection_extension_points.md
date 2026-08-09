# Spatial and projection extension points

Status: source characterization; no spatial implementation authorized
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`

The research proposal at
`docs/architecture/proposed_spatial_projection_semantics.md` is
non-normative. This document first records what the pinned kernel and views
actually provide, then identifies a least-invasive future boundary.

## Existing 3D kernel model

3D semantics are compiled in `source/shared/common`, primarily under
`org/geogebra/common/geogebra3D/kernel3D`. Representative concrete types are:

| Category | Current classes and source anchors |
|---|---|
| Point/vector | `GeoPoint3D` (`geos/GeoPoint3D.java:84`), `GeoVector3D` (`geos/GeoVector3D.java:65`) |
| Infinite/limited linework | `GeoLine3D` (`geos/GeoLine3D.java:41`), `GeoRay3D` (`:41`), `GeoSegment3D` (`:53`), based on `GeoCoordSys1D` |
| Plane | `GeoPlane3D` (`geos/GeoPlane3D.java:67-68`) and `GeoPlane3DConstant` (`:34`) |
| Curves | `GeoConic3D` (`geos/GeoConic3D.java:48-49`), `GeoCurveCartesian3D` (`:55`), `GeoLocus3D` (`:35`), `GeoPolyLine3D` (`:43`) |
| Surfaces | `GeoSurfaceCartesian3D` (`geos/GeoSurfaceCartesian3D.java:59`) and implicit-surface support in the shared kernel |
| Quadrics | `GeoQuadric3D` (`geos/GeoQuadric3D.java:80`), `GeoQuadric3DLimited` (`:59`), `GeoQuadric3DPart` (`:41`) |
| Composite/boundary-like objects | `GeoPolygon3D` (`geos/GeoPolygon3D.java:58`), `GeoPolyhedron` (`:84`), `GeoPolyhedronNet` (`:32`) |

`Kernel3D` extends `Kernel` and supplies the 3D XML handler/manager
(`source/shared/common/src/main/java/org/geogebra/common/geogebra3D/kernel3D/Kernel3D.java:96,132-162`).
`Manager3D` implements the shared `Manager3DInterface`
(`geogebra3D/kernel3D/algos/Manager3D.java:112` and
`kernel/Manager3DInterface.java:64`), while `AlgoDispatcher3D` extends the 2D
dispatcher (`geogebra3D/kernel3D/algos/AlgoDispatcher3D.java:59`). Constructive
algorithms for points, lines, planes, conics, surfaces, quadrics,
intersections, polyhedra, and transformations live beside them under
`kernel3D/algos`.

**Finding:** upstream already has substantial native 3D geometry and a normal
dependency-graph representation. A future CeDG spatial layer should compose or
extend these contracts; it should not create a second numerical 3D kernel.

## Coordinates and projection operations

The reusable mathematical primitives are in
`source/shared/common/src/main/java/org/geogebra/common/kernel/matrix`:

- `CoordSys` represents coordinate systems (`CoordSys.java:28`) and owns an
  orthonormal matrix used in plane projections (`:425,780,826`);
- `CoordMatrix4x4` represents homogeneous 4x4 transforms
  (`CoordMatrix4x4.java:27`);
- `Coords.projectPlane()` maps a point to global and in-plane coordinates
  (`Coords.java:1134-1303`);
- `projectPlaneThruV()` projects through an explicit direction
  (`Coords.java:1423-1465`);
- `projectLine()` projects to a line and optionally returns parameters
  (`Coords.java:1653-1712`).

These are general algebraic operators. They do not by themselves establish
that a plane/frame is a named CeDG projection frame, that a projected geo is
bound to a spatial geo, or that a set of views is sufficient for
reconstruction.

## 3D view and renderer boundary

`EuclidianView3D` is the common 3D view owner
(`source/shared/common/src/main/java/org/geogebra/common/geogebra3D/euclidian3D/EuclidianView3D.java:165`).
It owns a `Renderer` (`:214,440-450`), maps scene and screen coordinates
(`:784-821`), and switches orthographic, perspective, glasses, or oblique
camera projection (`:3771-3973`). The common abstract renderer begins at
`geogebra3D/euclidian3D/openGL/Renderer.java:52`; Desktop supplies the JOGL
implementation in `source/desktop/jogl2`.

The view maps kernel geos to `Drawable3D` instances in
`EuclidianView3D.newDrawable()` (`:605-770`) and receives add/remove/update
notifications (`:549-600,1542-1662`). Renderer hits become view hits at
`:1988-2049`; selection itself remains centralized in `SelectionManager`.

**Adapter boundary:** a future kernel-owned spatial object should expose or
derive ordinary 3D kernel geos that this existing `GeoElement -> Drawable3D`
boundary can consume. The view must not keep an independently editable copy or
become the authority for projection/reconstruction.

## Current plane views and conversion-like mechanisms

`ViewCreator` is a small interface for a 3D coordinate system that can create
or remove a 2D view
(`source/shared/common/src/main/java/org/geogebra/common/kernel/kernelND/ViewCreator.java:27-33`).
It is implemented by current plane-like types including:

- `GeoPlane3D.createView2D()`/`removeView2D()`
  (`geogebra3D/kernel3D/geos/GeoPlane3D.java:645-674`);
- `GeoConic3D` (`GeoConic3D.java:594-624`);
- `GeoPolygon3D` (`GeoPolygon3D.java:621-670`).

`EuclidianViewForPlaneCompanion` maps the plane coordinate system through the
3D screen matrix (`source/shared/common/src/main/java/org/geogebra/common/geogebra3D/euclidianForPlane/EuclidianViewForPlaneCompanion.java:253-266`).
Its XML writes `<viewId plane="...">` using `plane.getLabelSimple()` at line
521. `MyXMLHandler3D` reloads those settings by looking up the same plane label
(`source/shared/common/src/main/java/org/geogebra/common/geogebra3D/io/MyXMLHandler3D.java:435-445`).

`AttachCopyToView` is not a spatial-projection binding. Its algorithm copies a
transformable input and registers for Euclidian-view changes
(`source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoAttachCopyToView.java:42-45,97-124`).
It derives a projective transform from real-world and screen corners through
the selected Euclidian settings (`:158-210`). That is useful presentation
infrastructure but is viewport-coupled and creates another geo.

## View registration, membership, and persistence

`Kernel` attaches `View` observers and sends construction geos to them
(`source/shared/common/src/main/java/org/geogebra/common/kernel/Kernel.java:174,3703-3771`).
`GeoElement` records presentation membership through `viewFlags` and a coarse
3D visibility state (`source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoElement.java:278,312,5702-5760`).
These flags answer where a geo is displayed, not which 2D geos are projections
of which 3D geo.

Construction XML is inside `geogebra.xml` in the GGB archive
(`source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java:46-58`).
An ordinary geo opens as `<element type="..." label="...">`
(`kernel/geos/GeoElement.java:4495-4510`). Algorithms persist commands or
expressions with input/output references, and then labeled output geos
(`kernel/algos/AlgoElement.java:1291-1401`). `MyXMLio` can put a unique `id` on
the GGB document header (`MyXMLio.java:224-270`); that is a file ID, not a stable
ID for each geometric object.

Repository searches for `UUID`, `stableId`, `ProjectionBinding`, and an object
ID field on `GeoElement` found no durable per-object identity/projection
contract. The current plane-view link itself demonstrates label-based lookup.

## Gap analysis

The pinned kernel does **not** contain a durable, typed, serializable relation
from one 3D `GeoElement` to one or more 2D projected `GeoElement`s. In
particular, current facilities do not record:

- stable object and projection-frame IDs;
- defining/derived/auxiliary/analysis/presentation roles;
- type-specific view sufficiency or non-degeneration rules;
- explicit correspondence between projected curve points/parameters;
- reconstruction and reprojection residuals/status;
- dynamic ambiguity, underdetermination, or inconsistency certificates.

Labels, creation order, layer, view membership, and screen coincidence are
therefore insufficient and must not be promoted into spatial identity.

## Least-invasive future insertion point

Subject to an accepted spatial specification/ADR, the least-invasive boundary
is a shared-kernel semantic layer integrated with the existing construction
graph and XML, adjacent to (not inside) frontend view state:

1. stable identity and typed binding/certificate data participate in
   `Construction`/`AlgoElement` dependencies;
2. projection frames use `CoordSys`, `CoordMatrix4x4`, and `Coords` operators
   but add explicit semantic role, orientation, domain, and version;
3. projected 2D and spatial 3D representations remain ordinary or compatible
   kernel geos, referenced by stable IDs rather than labels;
4. serialization extends the shared XML handler with a versioned relation
   schema and deterministic legacy behavior;
5. `EuclidianView3D` and 2D views consume derived kernel representations via
   their existing drawable adapters;
6. the initial 3D bridge is one-way from semantic authority to view; any
   bidirectional editing requires a separate propagation policy.

This placement matches the repository rule that identity, dependency,
reconstruction, and serialization belong in the shared Java kernel. It does
not decide whether the eventual spatial identity is a new `GeoElement`, a
semantic aggregate, or a hybrid; that question remains explicitly open in the
research proposal (`proposed_spatial_projection_semantics.md:183-190`).

## Required pre-implementation evidence

Before adding classes, produce an accepted specification and ADR covering:

- exact versus numerical projection semantics and tolerance ownership;
- stable ID generation, copy/paste, macro, undo, merge, and legacy behavior;
- type-specific sufficiency and degeneration rules;
- dynamic dependency and invalid-state propagation;
- XML versioning and round-trip compatibility;
- one-way 3D adapter behavior;
- analytic reconstruction/reprojection tests for the canonical cases in
  `AGENTS.md`.

No `SpatialObject3D`, projection binding, certificate, or adapter is created in
the baseline task.

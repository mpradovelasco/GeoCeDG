# G9 spatial and canonical-projection scientific traceability

| Field | Value |
|---|---|
| Status | **G9P TRACEABILITY — AUTHOR APPROVED / IMPLEMENTATION NOT STARTED** |
| Scope | spatial identity, canonical orthographic projection, primitive reconstruction, lifecycle, and later composed boundaries |
| Software authority | current source, repository instructions, normative specifications, and Accepted ADRs |
| Historical evidence | CeDG 2022 spatial-association proof of concept and curated G9P workflows |
| Productive evidence | none; G9 spatial implementation has not started |
| Date | 2026-08-16 |

## 1. Evidence policy

The scientific corpus establishes why orthographic representations must be
associated with spatial objects and why CeDG construction provenance matters.
It does not define production IDs, XML spelling, Java package names, numerical
tolerances, rank thresholds, or a generic reconstruction solver.

Current source and approved contracts remain the software authority. The
historical scripts and models are research/workflow evidence and regression
inputs. Their label conventions, object names, list layouts, embedded macros,
screen organization, and sampled loci are not normative semantics or numeric
oracles.

## 2. Sources inspected

### 2.1 Historical spatial-association chapter

| Field | Value |
|---|---|
| Catalog ID | `cedg.reference.spatial-association-2022` |
| Source | [`2022_CINIE_Dykinson_Asoc vistas y objetos 3D e.pdf`](../references/cedg/spatial-association/2022_CINIE_Dykinson_Asoc%20vistas%20y%20objetos%203D%20e.pdf) |
| Citation | M. Prado-Velasco and L. Garcia-Ruesgas, “Asociacion entre vistas ortograficas y objetos 3D en CeDG: prueba de concepto,” 2022 |
| SHA-256 | `0aa729586297bd33cd21e180be6174928b96503dca0e63191b4e4e49c93293c3` |
| Relevant pages | PDF pages 12–17, corresponding to printed pages 61–66 |
| Rights | Restricted local research source; no republication permission recorded |

The relevant pages provide the following evidence:

- printed page 61 motivates associating orthographic views with spatial
  objects and introduces the proof-of-concept design;
- printed page 62 shows the initial model and list-owned organization, including
  the object/view association lists and initialization hook;
- printed pages 63–65 show the `ggbOnInit`, `registerAddListener`, `onAdd`, and
  `searchAssociatedList` workflow, with association discovery mediated through
  JavaScript, lists, and object names; and
- printed page 66 shows the resulting model and identifies the proof of concept
  as a basis for a later Java-integrated solution.

This establishes that a hierarchical association is useful and that new views
should follow object membership dynamically. It also exposes the production
gaps: identity is name/list based, lifecycle is listener/script based,
canonical sufficiency is not type-specific, and no kernel-owned serializable
certificate distinguishes underdetermination, ambiguity, inconsistency, or
degeneration.

The strings `ListObjectF`, `ListAssociations3DViews`, `ObjectViewCouple`,
`ggbOnInit`, `onAdd`, and `searchAssociatedList` are therefore preserved as
searchable historical evidence and possible regression fixture vocabulary.
They are not proposed public APIs.

### 2.2 Curated G9P workflow models

| Workflow source | SHA-256 | Spatial/projection evidence | Limitation |
|---|---|---|---|
| [`geocedg-reference-general-construction-workflow.ggb`](../references/cedg/models/g9p/geocedg-reference-general-construction-workflow.ggb) | `738e3edcf44e10f0c07b846d1f53127c4d8450cf0ad209ea9a0e3e8cc2c36a2e` | A hemispherical tank and three cylindrical legs are defined and manipulated through orthographic constructions, changes of position, incidence, conics, and rotations. | Spatial points/projections use conventional 2D construction and labels; no durable typed 3D association exists. Not a coordinate oracle for G9. |
| [`geocedg-reference-locus-cylindrical-graft-development.ggb`](../references/cedg/models/g9p/geocedg-reference-locus-cylindrical-graft-development.ggb) | `9e220a695a4b1ee2bf60adc77872133e8074740443ae659de1404539de8141f2` | Two projections of spatial intersection points generate projected intersection loci; a dependent developed curve transports constructive lengths. | Uses legacy sampled loci and historical label/macro conventions. No common typed spatial-curve binding is persisted. |
| [`geocedg-reference-locus-focal-sphere-illumination.ggb`](../references/cedg/models/g9p/geocedg-reference-locus-focal-sphere-illumination.ggb) | `baec7131aa95676864457d1602f73f8fef3ce37674fc0e48b28efd4feac204a0` | Projection-plane changes and paired moving points represent a sphere–cone intersection and illumination boundary. | Legacy locus samples/labels do not prove spatial correspondence, sufficiency, or numeric G9 answers. |
| [`geocedg-reference-locus-truncated-cone-cylinder-connections.ggb`](../references/cedg/models/g9p/geocedg-reference-locus-truncated-cone-cylinder-connections.ggb) | `18c6fad4d53fc3bb03a1546021a0677656fded2c853b7ec198312b96ee55e155` | Profile, vertical, and horizontal constructions use corresponding generatrices and moving points to define projected surface-intersection curves. | Correspondence is procedural/human-readable, not a serialized typed spatial-curve relation. |
| [`geocedg-reference-workflows-notes.md`](../references/cedg/models/g9p/geocedg-reference-workflows-notes.md) | `d30f08d8ff278e40fbf65e26a030969d129c56e5e5bda8f18b5fdee760a8b6a0` | Author-supplied intent, construction order, view use, projection workflow, and locus/development meaning. | Requirements evidence only; conversation-like explanation is below current source and approved contracts. |

The four models collectively demonstrate that paired projections, auxiliary
views, corresponding generators/points, and construction order carry semantic
meaning. They do not carry stable spatial IDs or type-specific canonical
certificates. Their labels may help a human interpret the archive, but using
those labels to auto-associate G9 objects would contradict the evidence policy.

They also distinguish the intrinsic projection problem from its common
dihedral-diagram realization. Folds, transported generators, profile views and
changes of projection plane depend on line-of-ground/orientation relationships
among views. Those relationships are human-readable in the current drawings
but are not persisted as a typed `ProjectionSystem` or
`ProjectionDiagramMap`. Visible placement is evidence of the requirement, not
a migration oracle.

### 2.3 Current-source evidence

| Source area | Established fact | G9 implication |
|---|---|---|
| `kernel/algos/ConstructionElement.java` | `ceID` supports creation ordering and is allocated at runtime. | Add a distinct durable identity; do not serialize/reinterpret `ceID`. |
| `kernel/Construction.java`, `kernel/algos/AlgoElement.java` | The normal construction graph owns dependencies, updates, replacement, and ordering. | Reconstruction and derived projections must be normal-DAG algorithms. |
| `kernel/Construction.java` replacement/redefine seams plus XML/undo reload | Host replacement can target and rewire a construction element, while reload may rebuild Java instances; labels remain mutable display names. | Durable continuity needs an explicit target-based provider/type/schema/role predicate, not Java reference or label equality. |
| `geogebra3D/kernel3D/geos` and `geogebra3D/kernel3D/algos` | Native 3D point, line, plane, conic, curve, surface, quadric, polygon, and polyhedron infrastructure exists. | Reuse/adapt the native kernel; do not build an independent spatial numeric kernel. |
| `kernel/matrix/CoordSys.java`, `CoordMatrix4x4.java`, `Coords.java` | Coordinate-system and projection algebra exists; `CoordSys.getPoint(...)` maps intrinsic plane coordinates into 3D. | Compose it behind semantic frames and diagram maps; these operations alone do not supply frame/system identity, hinge consistency or sufficiency. |
| `geogebra3D/euclidian3D/EuclidianView3D.java` | Kernel geos become derived drawables; camera and screen transforms belong to the view. | One-way derived view adapter; hard-zero camera/screen authority reads. |
| `geogebra3D/euclidianForPlane/EuclidianViewForPlaneCompanion.java`, `io/MyXMLHandler3D.java`, `geogebra3D/main/App3DCompanion.java` | Existing plane-view mirror/rotation is selected from the 3D camera/screen matrix and serialized as UI state; the plane view is resolved using the plane label. | Retain legacy behavior, but never reuse its camera-derived transform or label-keyed XML as `delta_i`, a system relation, or semantic binding identity. |
| `io/MyXMLio.java`, `io/MyXMLHandler.java`, `io/ConsElementXMLHandler.java` | Construction XML/reload is the persistence boundary and supports undo/load workflows. | IDs and relations must live in versioned construction serialization with two-stage resolution. |

## 3. Requirement-to-evidence-to-validation matrix

| G9 requirement | Supporting evidence | Proposed validation |
|---|---|---|
| Stable spatial identity distinct from visible names | Historical PoC needs name/list association; curated workflows rely on label conventions; labels are mutable in source | `A1-ID-*`, rename/label reuse, delete/recreate, zero label-fallback counter |
| Intrinsic projection distinct from common CeDG diagram placement | Curated workflows fold sections, transport generators, use profile views and change projection planes; current plane-view transform is camera/UI state | persist `ProjectionSystem`/map/relation IDs; analytic `q -> p -> q`; hard-zero viewport/camera authority |
| Line-of-ground, fold orientation and auxiliary-frame provenance | Graft, truncated-connection and illumination workflows depend on folds and projection-plane changes | oriented-hinge equality, change-of-plane relation, inconsistent/degenerate-system tests |
| Explicit object–projection relation | PoC structures associations; workflow notes describe paired projections and corresponding moving points | versioned frame/binding records, role/type checks, save/reopen graph equality |
| Association participates in dynamic construction | PoC add listener follows object creation; workflows emphasize Construction Protocol and dependent projection procedures | normal-DAG `A2-DYN-*`; hidden-graph counter hard zero |
| Type-specific canonical sufficiency | PoC associates views but does not prove uniqueness; descriptive workflows contain collapsed/projecting positions | analytic point/line/plane/circle/curve rank and degeneration matrices |
| One edit authority per revision | Projection-defined workflows coexist with upstream editable native 3D geos | `A2-AUTH-*`; mixed-role/cycle rejection; explicit future mode transition |
| Projection-defined first | G9P workflows construct spatial meaning from orthographic projections | G9A2 point pilot driven only by `DEFINING` projections; 3D is derived |
| No stale spatial geometry after failure | Dynamic CeDG procedures traverse singular/inconsistent configurations | valid -> invalid -> recovery traces with stale-publication hard zero |
| Stable lifecycle across save/undo/copy/redefine | Historical script/list state and mutable labels cannot provide it; current XML/copy/redefine seams show need | complete G9A1/G9A3 lifecycle state machines |
| Redefine continuity distinct from replacement | Host target/rewrite behavior does not itself prove semantic sameness; XML/undo can restore a new Java instance with the same serialized identity | recomputation same ID; compatible target-based redefine atomic transfer; true/incompatible replacement and delete/recreate fresh; copy fresh closure; undo/reopen restore |
| Curves require parameter/topology correspondence | Graft, cone/cylinder, and focal models generate paired locus projections from corresponding moving construction points | spatial-curve common-parameter tests; no sample-index or coordinate dedup |
| Derived development retains provenance | Graft model transports generatrices/lengths into a flat dependent locus | later binding provenance/role trace; do not treat developed drawing as a spatial projection automatically |
| Projection semantics independent of viewport | Current renderer/camera is separate from kernel projection algebra | zoom/DPI/camera/style matrix; presentation authority counters hard zero |
| Diagram-coordinate gauge does not alter sufficiency | A coherent drafting diagram may be translated/rotated without changing intrinsic spatial constraints | transform every `delta_m` and bound `p_m` by one model-coordinate gauge; recover identical intrinsic observations/certificate result |
| Composite boundaries preserve CeDG construction | Tank/legs and surface-intersection workflows are composed from explicit primitives/relations | later G9C tetrahedron/cylinder/cone/oriented-boundary cases; no opaque feature tree |

## 4. Scientific definition traced

The normative primitive-schema design translates descriptive-geometry sufficiency into
explicit mathematical predicates:

- `pi_i` maps a spatial value into intrinsic frame coordinates, while
  `delta_m` maps that intrinsic representation into the common CeDG diagram;
- canonical sufficiency is evaluated after `q_i=delta_m^{-1}(p_m)`, and a valid
  common-diagram gauge cannot change the recovered `q_i`;
- a declared hinge maps to one common oriented diagram line from both frames,
  and a change of plane is an explicit auxiliary-frame relation;

- a point is the consistent unique intersection of lift lines in sufficient
  independent frames;
- a line is the intersection of independent planes lifted from non-collapsed
  projected lines, supplemented when a view collapses the direction;
- segment/ray/vector meaning additionally requires endpoint, origin,
  orientation, or magnitude correspondence;
- a plane is reconstructed from sufficient constructive primitives, not a
  visible patch;
- a circle/conic includes its support plane and intrinsic type/parameters, not
  silhouettes alone; and
- a spatial curve requires a shared semantic parameter or equivalent explicit
  correspondence across projected branches/components.

These derivations are software requirements based on standard projective and
linear-algebra reasoning. The historical sources motivate them but do not
serve as their proof or prescribe numeric thresholds.

## 5. Historical PoC regression policy

A later test may construct a small native equivalent of the PoC hierarchy:

```text
one spatial object
  -> two defining projection bindings
  -> one auxiliary binding
  -> one derived projection added after object creation
```

The expected regression is semantic behavior—stable association, dynamic
addition through a normal transaction, correct role, save/reopen, and no stale
geometry—not identical list names, JavaScript text, object labels, or screen
layout. The original restricted PDF and any extracted figures are never copied
into generated public artifacts.

## 6. Curated-model regression policy

The G9P `.ggb` files remain immutable research inputs. Before any use in a
productive regression:

1. pin source hash and rights/provenance;
2. load unassociated and confirm unchanged legacy behavior;
3. create any spatial association explicitly in a derived test fixture;
4. state which projection correspondence is asserted and why it is sufficient;
5. use independent analytic fixtures for numeric expected values; and
6. keep legacy loci, embedded scripts, macros, labels, and rendered curves out
   of the G9 geometric authority.

Automatic migration based on names such as primed/unprimed projections is
specifically forbidden. Human-readable conventions can guide fixture design
but cannot execute the association.

## 7. Assumptions, derivations, and unresolved questions

### Evidence-backed findings

- Hierarchical spatial/view association is a real CeDG workflow need.
- Current research/workflow artifacts express it through labels, lists,
  scripts, and paired constructions rather than a durable kernel relation.
- Current workflows require geometric relations among views, while current
  productive plane-view placement remains camera/label-based UI state.
- Upstream already has the geometric and DAG substrate needed for a
  least-invasive semantic layer.

### Author-approved architectural derivations

- durable IDs plus a construction registry are required by rename/copy/undo
  semantics;
- durable projection-system, diagram-map and frame-relation records are needed
  to express `p=delta(pi(x))` without using visible layout;
- intrinsic sufficiency is invariant under one coherent common-diagram gauge,
  but changing one semantic map or hinge is a true construction revision;
- projection-defined edit authority is the narrowest first CeDG pilot;
- point reconstruction is the smallest sufficient analytic pilot; and
- the G9A1/A2/A3 split isolates persistence, geometry, and lifecycle risk.

### Implementation-phase decisions

- exact ID/XML encoding (the GeoCeDG Classic native-preservation policy is
  approved; only encoding/mechanics remain);
- exact initial XML spelling and author-approved admitted map families for the
  projection-system records;
- explicit replacement identity-transfer UX/API;
- numeric rank/conditioning policy and certified versus estimated evidence;
- authority-mode transition after the projection-defined pilot; and
- primitive/composite promotion order after G9A3.

## 8. Rights and redistribution boundary

The spatial-association PDF is cataloged as restricted. It may be read for
local research and cited through its catalog record, but no figure, page image,
or substantial text is republished by this traceability document. The G9P GGB
models are supplied repository references whose licensing/provenance must be
respected independently; their inclusion here does not authorize packaging or
redistribution.

Generated numeric or structural test fixtures must be newly authored,
minimal, and documented. Do not extract sampled coordinates, JavaScript bodies,
or screenshots from restricted sources as expected constants when analytic
construction can be stated independently.

## 9. Promotion boundary

This matrix supports the author-approved G9P design. It does not authorize G9A1
and does not claim a spatial test result. The governing specification is
normative.
Productive evidence must be generated by the separately invoked phase verifiers
and reviewed at each author gate. G9B primitives, G9C composed objects, and any
public workspace/command remain designed future work.

The hard semantic dependency for G9B is the accepted G9A3 spatial foundation;
G9B does not depend on completion of G9U1 or any other GUI client. G9U2 may
consume projection-system semantics only after its approved global gate.

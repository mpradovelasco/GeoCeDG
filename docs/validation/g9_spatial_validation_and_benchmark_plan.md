# G9 spatial semantics validation and benchmark plan

| Field | Value |
|---|---|
| Status | **AUTHOR-APPROVED VALIDATION DESIGN / NOT EXECUTED** |
| Scope | G9A1 identity/persistence, G9A2 point pilot, G9A3 lifecycle hardening; forward plan for G9B/G9C |
| Authority | Normative spatial specification and Accepted ADRs 0010/0011 |
| Evidence classes | static, analytic fake-first, productive shared-kernel, saved-file round trip, real desktop/view smoke |
| Productive result | none; this plan records gates only |

## 1. Validation principles

1. Validate the source of truth, not a screenshot, renderer, serialized cache,
   label convention, or previous report.
2. Establish durable identity/lifecycle before evaluating spatial reconstruction.
3. Use analytic world-coordinate references for primitive reconstruction and
   reprojection.
4. Record independent state axes; do not reduce results to defined/undefined.
5. Assert hard zero for forbidden authority and stale-current publication.
6. Prefer deterministic functional work counters to elapsed time. Timing is
   informational and environment-stamped.
7. Exercise every saved-file promise through actual save/reopen or the canonical
   shared XML round-trip path; inspecting emitted text alone is insufficient.
8. Run focused validation first, then the repository verification authority.
9. Classify runtime evidence as static, fake-first, skipped, or real. Do not
   imply that a fake analytic fixture proves desktop persistence or view wiring.

## 2. Evidence layers

| Layer | Purpose | Minimum evidence |
|---|---|---|
| Static source/contract | package placement, forbidden dependencies, XML schema/version shape | source scan, architecture assertions, deterministic schema fixture |
| Analytic fake-first | isolate math/status policy before shared-kernel wiring | independent exact/high-precision fixtures and normalized expected records |
| Shared-kernel unit | normal DAG, identity registry, schemas, atomic publication | focused Gradle tests through repository wrappers |
| Serialization integration | save/reopen, undo, copy/paste, malformed/future input | real XML/GGB round trips with identity graph comparison |
| Desktop/view smoke | one-way derived 3D adapter and no direct-authority edit | real desktop runtime when available; otherwise explicitly skipped |
| Canonical CeDG regression | compatibility and later scientific workflows | approved model manifest; never legacy labels/samples as numeric oracle |

## 3. Functional instrumentation

Every run emits a normalized counter/result record. Suggested counter names are
conceptual; productive names must be stable and versioned.

### 3.1 Identity and registry

Kind breakdowns include the conceptual records `ProjectionSystem`,
`ProjectionDiagramMap`, and `ProjectionFrameRelation`; productive counter names
must follow the accepted implementation names without merging those kinds.

```text
persistentIdAllocations
persistentIdRestores
persistentIdRemaps
projectionSystemIdRestores
projectionDiagramMapIdRestores
projectionFrameRelationIdRestores
registryInsertions
registryLookups
registryCollisions
unresolvedStableReferences
labelFallbackLookups                 HARD ZERO
coordinateAssociationAttempts       HARD ZERO
creationOrderAssociationAttempts    HARD ZERO
xmlPositionAssociationAttempts      HARD ZERO
outputIndexAssociationAttempts      HARD ZERO
javaReferenceIdentityAssumptions    HARD ZERO
visibleDiagramAssociationAttempts   HARD ZERO
```

### 3.2 Lifecycle transactions

```text
copyClosureObjects
copyClosureInternalReferences
copyClosureExternalReferences
copyTransactionCommits
copyTransactionRollbacks
semanticReplacementCommits
semanticReplacementRollbacks
compatibleRedefinitionRetains
compatibleRedefinitionNoOps
compatibleRedefinitionRejects
trueReplacementNewIds
missingTargetContextRetains         HARD ZERO
multiOutputIdentityMappingRejects
definitionRevisionIncrements
topologyRevisionIncrements
deleteInvalidations
undoIdentityRestores
reopenIdentityRestores
```

### 3.3 Semantic evaluation

```text
frameEvaluations
projectionSystemEvaluations
diagramMapForwardEvaluations
diagramMapInverseEvaluations
hingeConsistencyEvaluations
changeOfPlaneConsistencyEvaluations
projectionSystemCertificatePublications
projectionSystemCertificateRejections
reconstructionAttempts
rankEvaluations
candidateObjectsBuilt
reprojectionEvaluations
certificatePublications
failurePublications
supersededCandidateRejections
stalePayloadPublications             HARD ZERO
mixedAuthorityRevisionPublications  HARD ZERO
hiddenGraphRecomputations            HARD ZERO
```

### 3.4 Forbidden presentation authority

```text
renderCacheReads          HARD ZERO
rendererReads             HARD ZERO
viewportReads             HARD ZERO
screenCoordinateReads     HARD ZERO
dpiReads                  HARD ZERO
cameraTransformReads      HARD ZERO
layerOrVisibilityReads    HARD ZERO
```

Counters are broken down by geo/object/frame/system/diagram-map/frame-relation/
binding kind and accompanied by the complete
object/system/map/relation/frame/binding/geo/schema/policy revision tuple,
state axes, diagnostics, and deterministic termination reason. A hard-zero
violation fails the gate even if coordinates look correct.

## 4. G9A1 gate — durable identity and persistence substrate

Before adding G9 identity code, retain the focused host baseline:

- run `RedefineTest` unchanged (the R1 characterization run executed 55 tests,
  zero skipped/failures/errors);
- use test-private probes to force the `set`, soft-redefine, new-instance, and
  XML-rebuild paths and record old/new Java reference, label, command, parent
  algorithm, sibling outputs, construction order, and post-undo lookup; and
- treat those observations only as routing evidence. No expected durable-ID
  result may be derived from the observed reference or label.

### 4.1 Identity allocation and registry cases

| ID | Scenario | Required result |
|---|---|---|
| `A1-ID-01` | Assign identity to one participating independent geo | one opaque ID, one registry entry, no label lookup |
| `A1-ID-02` | Assign identities to dependent geos and semantic stubs | unique kind-correct entries; normal dependencies unchanged |
| `A1-ID-03` | Rename repeatedly, including label reuse elsewhere | every existing ID preserved; zero relation changes |
| `A1-ID-04` | Attempt duplicate registration | atomic structured collision; no partial entry |
| `A1-ID-05` | Change coordinates/type-compatible values | same IDs; value revision only |
| `A1-ID-06` | Delete then recreate same label | old ID retired, new ID allocated, no inherited relation |
| `A1-ID-07` | Register inert projection system, two frames/maps and one frame relation | unique kind-correct system/map/relation IDs and complete reference stubs; no semantic evaluation |
| `A1-ID-08` | Rename geos used by a system map/hinge | all system/map/relation references and revisions unchanged |
| `A1-ID-09` | Reorder equal-dependency Construction Protocol steps | all durable IDs and semantic revisions unchanged |
| `A1-ID-10` | Attempt cross-kind reuse of the same serialized ID | deterministic kind-aware collision or rejection; no ambiguous lookup |

### 4.2 Save/reopen and versioning

| ID | Scenario | Required result |
|---|---|---|
| `A1-XML-01` | Empty/legacy construction | byte/semantic compatibility; no spatial IDs synthesized |
| `A1-XML-02` | Complete identity graph save/reopen | same IDs and relation topology after two-stage resolution |
| `A1-XML-03` | Relations appear before referenced geos in fixture | resolved in stage two, deterministic order-independent result |
| `A1-XML-04` | Missing reference | explicit broken/undefined record; no label repair |
| `A1-XML-05` | Duplicate native-file ID | deterministic rejection/atomic failure per approved policy |
| `A1-XML-06` | Unknown future version | explicit unsupported result and approved preservation behavior |
| `A1-XML-07` | Malformed ID/role/type | structured parse/validation diagnostic; no partial current graph |
| `A1-XML-08` | Two repeated reopen cycles | identity graph and normalized XML remain deterministic |
| `A1-XML-09` | System/map/relation records precede frames and constructive geos | all stubs register in stage one and resolve by kind in stage two; no visible-layout lookup |
| `A1-XML-10` | Binding resolves a frame but names a missing system/map/relation | explicit broken semantic closure; no partial certificate or fallback |
| `A1-XML-11` | Undo/redo reload reconstructs new Java instances | exact durable identity/revision graph restored; `ceID` and object references are ignored |

### 4.3 Copy, undo, macro, and redefine

| ID | Scenario | Required result |
|---|---|---|
| `A1-COPY-01` | Copy complete dependent closure once | all copied IDs fresh; every internal reference remapped |
| `A1-COPY-02` | Paste same closure repeatedly | disjoint identity closures; identical topology shape |
| `A1-COPY-03` | Incomplete closure/external reference | approved explicit reference behavior or atomic rejection |
| `A1-COPY-04` | Copy between constructions | no source registry reference survives |
| `A1-COPY-05` | Copy a projection system closure | fresh frame/system/map/relation/binding/geo IDs; every internal endpoint remapped |
| `A1-COPY-06` | Copy an object in the same construction while explicitly sharing its system | object/binding/geos fresh; declared external system/map IDs retained with provenance |
| `A1-COPY-07` | Copy an object cross-document without its required system closure | atomic failure; no live source reference or partial destination graph |
| `A1-MACRO-01` | Invoke one semantic template twice | fresh identity closure per invocation |
| `A1-UNDO-01` | create/rename/delete/undo/redo | exact prior IDs restored at each snapshot |
| `A1-REDEF-A1` | ordinary value/upstream recomputation | same IDs; value/source revision only; normal DAG recomputation |
| `A1-REDEF-A2` | rename, order move, or definition text that provider proves a no-op | same IDs; no geometric definition/topology revision |
| `A1-REDEF-B1` | explicit target, one stable output role, compatible family/schema/authority/role and topology-preserving change; host `set` path | target ID retained; definition revision and dependent recomputation exactly once; topology revision unchanged |
| `A1-REDEF-B2` | same compatible transaction through host soft-redefine path | result equal to B1; Java instance behavior has no semantic effect |
| `A1-REDEF-B3` | same compatible transaction through new-instance or XML-rebuild path | target ID retained; registry current-instance pointer and DAG/bindings switched atomically |
| `A1-REDEF-C1` | explicit true semantic replacement | old ID retired, fresh ID allocated, old bindings invalidated |
| `A1-REDEF-D1` | type/schema/authority/output-role incompatible redefine | atomic `REJECT`, or fresh class-C identity only when explicit replacement was selected |
| `A1-REDEF-E1` | delete then recreate same label/type/coordinates | fresh ID; no inherited binding; undo restores the old snapshot ID |
| `A1-REDEF-F1` | copy/duplicate and two macro invocations | disjoint fresh closures with explicit copy lineage |
| `A1-REDEF-G1` | save/reopen and undo/redo across B/C/E | exact serialized IDs/revisions restored despite new Java instances |
| `A1-REDEF-HOST-01` | same-label expression with no old-target identity context | no `RETAIN`; fresh replacement or explicit rejection |
| `A1-REDEF-HOST-02` | host-compatible `GeoClass`/command but provider-incompatible schema or role | no `RETAIN` |
| `A1-REDEF-HOST-03` | multi-output/cardinality change without stable provider output roles | explicit unsupported/rejection; no label/index mapping |

### 4.4 G9A1 pass criteria

- all admitted identity/lifecycle cases pass in shared-kernel and saved-file
  paths, including each host replacement branch reachable without broad source
  change;
- normalized identity graphs are deterministic across reruns;
- geo/object/frame/system/map/relation/binding stubs survive XML, undo and copy
  with the specified restore/remap behavior;
- label/coordinate/order/XML-position/output-index/Java-reference/visible-layout
  association counters are zero;
- no reconstruction or spatial-view behavior exists;
- legacy files load unassociated; and
- author reviews the XML, collision, copy-closure, and redefine evidence before
  G9A2 is invoked.

G9O1 completion is recommended before this work operationally but is not a hard
semantic pass condition for G9A1.

## 5. G9A2 gate — semantic core and point pilot

### 5.1 Projection-system and diagram-map oracle

Use exact or exactly representable frames and oriented 2D isometries/unit
similarities. A bound diagram point is `p = delta(q)` and reconstruction must
recover intrinsic `q = delta^-1(p)` before applying the frame projection model.
The oracle is independent of any viewport transform.

| ID | Scenario | Required result |
|---|---|---|
| `A2-SYS-01` | oriented isometry `q -> p -> q` | exact round trip, declared orientation and units, `CONSISTENT` system certificate |
| `A2-SYS-02` | declared unit similarity with fixed scale | within fixed numeric policy; scale appears in certificate evidence |
| `A2-SYS-03` | reflected/folded frame use | determinant sign and declared fold side preserved; no implicit orientation choice |
| `A2-SYS-04` | two intersecting frame planes with consistent hinge maps | both intrinsic hinge lines map to the same oriented diagram line |
| `A2-SYS-05` | hinge line/orientation/fold-side inconsistency | `INCONSISTENT` system certificate; no object reconstruction attempt |
| `A2-SYS-06` | parallel frame planes claim a finite hinge | deterministic `DEGENERATE`/`UNDEFINED` relation by approved predicate |
| `A2-SYS-07` | typed auxiliary change-of-plane frame and parent relation | relation evaluates through the normal DAG; frame-use role remains independent of object binding role |
| `A2-SYS-08` | common diagram gauge transform applied coherently to maps and points | identical recovered intrinsic data, object certificate and spatial result |
| `A2-SYS-09` | singular map, zero scale, incompatible units, or unadmitted affine family | deterministic `DEGENERATE`, `UNDEFINED`, or capability `UNSUPPORTED`; no inverse or spatial payload |
| `A2-SYS-10` | mutate one map/relation revision | system revision increments; only referencing object certificates invalidate and recompute |
| `A2-SYS-11` | vary zoom/pan/DPI/window/2D-view transform with fixed semantic records | identical system certificate and zero viewport/screen reads |

### 5.2 Independent analytic point oracle

Build test-private fixtures from rational or exactly representable frames and
points. The oracle constructs lift-line intersections or solves `A x=b`
independently from the candidate implementation. Near-rank cases use a
documented high-precision implementation and fixed normalization; simply
rerunning candidate code with more iterations is not independent.

| ID | Scenario | Expected certificate |
|---|---|---|
| `A2-POINT-01` | general point, two independent orthographic frames | `VALID` and exact/within-policy reprojection |
| `A2-POINT-02` | point on one projection plane | `VALID`; no false degeneration |
| `A2-POINT-03` | one defining view | `UNDERDETERMINED` |
| `A2-POINT-04` | repeated/parallel equivalent frames | `UNDERDETERMINED` |
| `A2-POINT-05` | three consistent overdetermining views | `VALID`; every residual reported |
| `A2-POINT-06` | incompatible lift lines | `INCONSISTENT_PROJECTIONS` |
| `A2-POINT-07` | discrete multi-candidate fixture if supported | `AMBIGUOUS`, no arbitrary selection |
| `A2-POINT-08` | undefined projected point/frame | `UNDEFINED` |
| `A2-POINT-09` | singular/invalid frame direction or basis | `DEGENERATE`/`UNDEFINED` by predicate |
| `A2-POINT-10` | near-rank boundary on both sides of threshold | deterministic state, rank evidence, no tolerance flapping |
| `A2-POINT-11` | same intrinsic projections embedded by two coherent common-diagram gauges | same reconstructed spatial point and intrinsic reprojection; diagram coordinates change only by the declared gauge |
| `A2-POINT-12` | binding names valid frame but wrong system/map context | deterministic broken/inconsistent result; no direct interpretation of common-diagram coordinates as intrinsic coordinates |

### 5.3 Dynamic and DAG traces

| ID | Trace | Required behavior |
|---|---|---|
| `A2-DYN-01` | valid -> inconsistent -> valid | same object/binding IDs; no payload while inconsistent; deterministic recovery |
| `A2-DYN-02` | full rank -> rank deficient -> full rank | explicit state transition and same identity when topology permits |
| `A2-DYN-03` | defining point deleted | normal dependent invalidation; no rebind to same-label geo |
| `A2-DYN-04` | rapid two-input revisions | superseded candidate rejected; only matching revision published |
| `A2-DYN-05` | downstream consumer | one normal-DAG update per publication; undefined propagation on failure |
| `A2-AUTH-01` | attempt to edit derived 3D point | rejected/routed without changing authority |
| `A2-AUTH-02` | attempt mixed defining/derived roles | cycle/authority validation failure |

### 5.4 Independence matrix

For a fixed construction revision, repeat all point results after changing one
presentation variable at a time:

```text
zoom; pan; DPI; application window size; 2D view membership; 3D camera;
perspective/orthographic renderer mode; labels; layers; visibility; style;
toolbar/workspace; construction creation order where dependencies are equal
```

Certificate bytes/normalized records, reconstructed world coordinates, and
functional counters must be equal. Relevant forbidden-authority counters are
zero.

### 5.5 Persistence and one-way view

- save/reopen a valid, inconsistent, and rank-deficient point;
- undo/redo through each transition;
- copy/paste the complete point/frame/binding closure and prove new IDs plus
  equal geometry, including system/map/relation closure or an explicitly
  permitted same-construction shared-system reference;
- verify that derived `GeoPoint3D` identity association is stable without label
  dependence;
- verify current derived geo becomes undefined/withdrawn on failure; and
- perform a real desktop 3D-view smoke test when runtime is available, stating
  `SKIPPED` otherwise.

### 5.6 G9A2 pass criteria

- all projection-system/map/relation and analytic
  point/status/intrinsic-reprojection/diagram-reprojection cases pass;
- normal-DAG and atomic-publication traces pass;
- hard-zero counters remain zero;
- lifecycle cases remain green;
- one-way view evidence is real or explicitly skipped without claiming the
  view gate; and
- no primitive beyond point and no public command/UI is introduced.

## 6. G9A3 gate — lifecycle and compatibility hardening

Run a state-machine suite over operations rather than isolated examples. At
minimum combine:

```text
create -> rename -> add binding -> invalidate -> save/reopen -> recover
create -> copy closure -> re-role copied binding -> undo -> redo -> delete
create -> compatible target redefine -> save/reopen -> true replacement -> undo
create system -> mutate map -> redefine hinge support -> copy subset -> undo
legacy load -> explicit association -> copy -> feature disabled -> reopen
```

Vary:

- zero, one, two, and three defining bindings;
- add/remove/re-role binding, diagram-map, and frame-relation transactions;
- valid, underdetermined, inconsistent, degenerate, and undefined states;
- internal versus declared external copy references, complete versus partial
  projection-system closure, and same- versus cross-construction destination;
- duplicate, missing, malformed, and future-version IDs;
- label collisions and label reuse after deletion;
- no-op, `set`, soft-redefine, new-instance, and XML-rebuild host branches;
- compatible, incompatible, true-replacement and missing-target-context intent;
- multi-output stable provider roles, sibling deletion, and output-cardinality
  changes;
- GeoCeDG/GeoCeDG Classic native load/save/reopen paths with identical semantic
  IDs/records and zero downgrade, plus characterized unsupported external-open
  fixtures; and
- recovery after an invalid interval with and without topology continuity.

Pass requires exact expected identity graphs at every step, bounded registry
state after delete/undo cycles, no dangling/rebound relation, and deterministic
normalized XML/results on repeated runs. Every compatible redefine must produce
the same semantic outcome regardless of host instance branch; every unsupported
mapping must roll back without a partial registry/DAG switch.

## 7. Forward G9B primitive validation matrix

These cases design later gates; they are not G9A authorization.
They depend semantically on accepted G9A3 spatial infrastructure, not on G9U1
frontend completion.

| Type | General cases | Required degeneracies/correspondence |
|---|---|---|
| Line | two non-collapsed frames; overdetermined third view | line perpendicular to each plane; collapsed-only view; parallel/coincident/inconsistent lift planes; orientation conflict |
| Segment | corresponding endpoints and common interval | swapped endpoint in one view; zero length; one/both collapsed views; endpoint deletion |
| Ray | origin plus oriented supporting line | unoriented two-solution case; zero direction; collapsed projection |
| Vector | free and bound vector in independent frames | zero vector; magnitude/orientation inconsistency; anchor confusion |
| Plane | three points; two incident lines; point+normal; approved traces | collinear points; coincident/skew lines; zero normal; general/projecting plane |
| Circle | support plane+center+radius; three points; general-plane projection | edge-on collapse; zero radius; collinear points; inconsistent projected conics |
| Conic | known support plane/homographies; typed intrinsic form | rank/type change; imaginary locus; hyperbola branch mismatch; singular homography |
| Spatial curve | two projections with shared semantic parameter | no/many-to-one correspondence; invalid gaps; self-intersection; seam; unbounded branch; branch creation/loss |

For each family, assert reconstruction/reprojection consistency, zoom/DPI/label
independence, stable identities while topology is unchanged, explicit failures,
serialization round trip, dynamic recovery, and no stale spatial result.
An unknown or deliberately unimplemented family, schema/provider version, frame
family or correspondence contract must instead publish capability `UNSUPPORTED`,
certificate `NOT_EVALUATED`, deterministic diagnostics and no current payload.

## 8. Forward G9C composed-object matrix

Only after primitive acceptance:

- tetrahedron reconstructed from sufficient projections;
- cylinder and cone from defining centers/axes/vertices/directrices/radii;
- composite oriented boundary with vertices, edges, support surfaces, faces,
  loops, incidence, and adjacency;
- open boundary claimed as closed;
- nonmanifold edge, inconsistent orientation, missing correspondence;
- dynamic closure loss/recovery; and
- serialization/copy round trip with stable component identity.

Silhouettes alone are never a canonical primitive definition. G9C must count
topology/incidence evaluations and keep CAD feature-tree operations out of the
authority model.

## 9. Benchmark dimensions and work budgets

Characterize before fixing production ceilings:

### Identity/persistence dimensions

```text
participating geos:       1, 10, 100, 1_000
frames:                    1, 2, 4, 10
projection systems:        1, 2, 10, 100
diagram maps per system:   1, 2, 4, 10
frame relations per system:0, 1, 3, 10
bindings per object:       1, 2, 4, 10
objects:                   1, 10, 100, 1_000
copy closure size:         1, 10, 100, 1_000
undo/reopen cycles:        1, 3, 10, 100
unresolved/collision rate: 0, 1, 10 percent in invalid fixtures
```

### Reconstruction dimensions

```text
defining views:            1, 2, 3, 10
consumers per object:      0, 1, 10, 100
source revisions:          no-op, value, definition, topology, frame,
                           system, map, relation, binding-set
rank regime:               well-conditioned, near-threshold, deficient
```

Report operation counts and retained state after removal. Suggested provisional
invariants—not numeric ceilings—are:

- registry resolution is linear in registered records plus references;
- a system/map/relation revision invalidates only certificates whose recorded
  revision tuple references it;
- one source revision causes at most one current candidate publication per
  affected object;
- derived consumers do not trigger new reconstruction for the same revision;
- deletion returns retained registry/certificate state to the documented live
  set; and
- no work scales with render tessellation, viewport size, DPI, or frame rate.

G9A characterization proposes measured ceilings with margin. Author approval
is required before ceilings become normative.

## 10. Equality oracle and deterministic evidence

For repeated strategies/runs compare:

- complete identity graph with remap provenance;
- object/system/map/relation/frame/binding/geo/schema/policy revision tuple;
- all independent state axes;
- reconstructed coordinates/invariants under the approved exact/numeric oracle;
- per-binding reprojection residuals and guarantees;
- diagnostics and implicated input IDs;
- current payload presence/absence;
- operation/retained-state counters; and
- normalized XML or machine-result hashes where applicable.

Wall-clock variance cannot change pass/fail. Random ID bytes differ when fresh
allocation is required, so deterministic comparisons use graph isomorphism and
recorded remap relations unless the approved ID generator itself promises a
fixed fixture seed.

## 11. Evidence package

Each productive phase should save under a phase-specific generated artifact
directory:

```text
environment and exact repository state
authority/spec/ADR hashes
fixture and policy manifests
normalized result/counter JSON
raw test logs and exit codes
analytic/high-precision reference source, runtime, precision and hashes
round-trip input/output hashes and normalized graph comparison
skipped real-runtime checks and reasons
rejected strategies and threshold sweeps
```

Generated artifacts are evidence, not source authority. The repository wrappers
under `tools/agent/` remain the executable authority. A phase report may claim
success only when its saved-file validation command completed successfully.

## 12. Global stop conditions

Stop and return to author review when:

- any relationship needs labels, coordinates, screen state, or creation order;
- any compatible identity retention depends on Java reference, host type/command
  compatibility, XML position, or output ordinal rather than explicit target
  intent and provider-declared stable role;
- an intrinsic/diagram conversion reads viewport state or silently treats
  common-diagram coordinates as frame-intrinsic coordinates;
- numeric rank/residual policy is nondeterministic or scale-undefined;
- invalid current inputs leave a valid payload visible;
- copy/reopen/undo changes identity contrary to the lifecycle table;
- an evaluator creates a hidden update graph or a second geometric authority;
- Classic or legacy behavior changes without an approved compatibility policy;
- real runtime is unavailable for a required promotion gate; or
- phase scope expands to a later primitive/composite/product surface.

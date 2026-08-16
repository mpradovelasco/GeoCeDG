# GeoCeDG developer guide

- Status: current-state first edition
- Baseline: GeoGebra 5.4.928.0 at
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- G9 designs in this guide: normative/author-approved, not productive; G9O1 authorized and not started

## Purpose and boundary

GeoCeDG is a source-based GeoGebra fork for computer-extended descriptive
geometry. Preserve explicit construction dependencies, semantic parameters,
projection coherence, degeneracy states and exact/approximate distinctions. Do
not replace this model with an opaque CAD feature tree.

Read `AGENTS.md` before changing the repository. Accepted specs/ADRs, source,
tests and serialization contracts outrank this guide.

## Repository and modules

Use `docs/developer/repository_map.md` for ownership. The main implementation
areas are:

- `source/shared/common`: shared kernel and GeoCeDG semantic/export code;
- `source/shared/common-jre`: shared JRE tests and scientific probes;
- `source/desktop/desktop`: GeoCeDG Desktop/profile and developer laboratory;
- `apps/geocedg`, `geocedg/features`, `geocedg/resources`: product contracts;
- `geocedg/specs`, `docs/adr`, `docs/architecture`: durable design authority;
- `tools/agent`: focused and composed verification;
- `packaging` and `tools/release`: internal Windows packaging.

GeoCeDG-owned Java uses `org.geocedg`. Changes to upstream namespaces must be
minimal and registered in `docs/upstream/modified-files.yml`.

## Ownership classes

Repository work distinguishes GeoCeDG-native source, modified upstream source,
unchanged upstream reference, generated output and third-party/restricted
material. The normative bundle vocabulary names these
`GEOCEDG_NATIVE`, `UPSTREAM_MODIFIED`, `UPSTREAM_UNCHANGED_REFERENCE`,
`GENERATED` and `THIRD_PARTY_OR_RESTRICTED`. Classification requires the
pinned baseline, modified-file inventory, history and explicit exclusions; a
directory name alone is not proof. Preserve upstream notices, register every
upstream edit and never treat generated reports or restricted assets as source.

## Toolchain, build and launch

The current Windows contract requires Git, PowerShell 7, JDK 22 for Gradle and
JDK 25 for Desktop packaging/runtime composition. Use only `gradlew.bat`.

```powershell
.\tools\agent\verify.ps1
.\gradlew.bat :desktop:desktop:runGeoCeDG
.\gradlew.bat :desktop:desktop:run
```

The second launch is GeoCeDG; the third is the unchanged Classic diagnostic.
For the internal V2 laboratory use the existing script under `tools/locus-v2/`.
See `README.md` and `packaging/windows/README.md` for setup and packaging
prerequisites.

Packaging uses `tools/release/build-windows-package.ps1`. App-image/ZIP/MSI/EXE
success is technical evidence only; public redistribution remains blocked
pending license and asset approval.

## Verification

`tools/agent/verify.ps1` is composed executable authority. Use the narrow
feature verifier first, preserve its log path/exit code, then run the composed
gate when the task requires it. Do not translate environment, permissions or
external-runtime failures into product changes.

Historical G7/G8 evidence is verified from the fixed `geocedg-g8-pass` tag;
living documents are current-HEAD checks. Never update a historical hash
manifest to accommodate later prose edits.

Focused Java tests live primarily in `source/shared/common-jre`; frontend tests
live with the Desktop module. A passing focused test is evidence for its stated
scope, not automatic approval, packaging success or public feature maturity.

## Kernel extension process

1. Define objects, domains, orientation, invariants, degeneracies and exactness.
2. Audit existing kernel classes, command dispatch, persistence and frontends.
3. Approve spec/ADR and validation design.
4. Implement the smallest shared-kernel change using normal `AlgoElement`
   dependencies and explicit `setInputOutput()`.
5. Register commands/localization only when a public surface is authorized.
6. Define copy, undo, serialization and compatibility before exposing a saved
   public object.
7. Add focused analytic, dynamic, degeneracy and deterministic tests.

A render/view/export layer must not become geometric authority.

## Commands, algorithms and GeoElements

Public commands follow upstream `Commands` registration, dispatcher/factory,
`CommandProcessor`, algorithm and localization paths. Rich internal Locus V2
metric/intersection classes currently bypass no public dispatcher because no
public V2 command is authorized. `GeoLocusV2`, metric and intersection result
geos are internal/nonpersistent and deliberately reject unsupported lifecycle
operations.

## Frontend/profile

`apps/geocedg/application-profile.yml` declares the conservative default
perspective/toolbar. Desktop compiles it through GeoCeDG profile classes. Saved
`.ggb` layouts may restore their own toolbar/perspective. Feature manifest
membership is metadata; do not assume it is a complete runtime flag service.

## Persistence and compatibility

Legacy `.ggb` behavior must remain unchanged unless a versioned migration is
approved. New semantic objects require stable IDs, semantic version, reconstructible
inputs, copy/undo/delete behavior and XML round-trip tests before public use.
Current V2 facilities have no such public contract.

G9P-R1 characterizes redefine as a transaction, not one host operation. The
current kernel may mutate an existing geo/algo, replace one Java instance, or
rebuild the construction from XML. A future durable ID therefore follows the
explicit target and an approved provider/type/schema/role compatibility
predicate. Labels, coordinates, construction indices, XML position and Java
reference equality cannot transfer identity. Recompute preserves identity;
compatible redefine may preserve it atomically; true or incompatible
replacement, delete/recreate and copy allocate fresh IDs; undo/reopen restore
the serialized identity graph.

## Approved projection-system and generator boundaries

An individual `ProjectionFrame` owns intrinsic projection geometry. The
normative G9 projection system additionally owns geometric diagram maps
`p_i = delta_i(pi_i(x))`, frame relations, hinges/change-of-plane provenance
and their revisions. These maps use model coordinates and remain independent
of the camera, Euclidian viewport and saved plane-view transform. G9B consumes
this shared-kernel model; no workspace is a semantic prerequisite.

The normative public Locus surface uses a typed one-dimensional semantic
generator rather than treating “slider” as a mathematical type. An admitted
scalar provider declares one true driving coordinate/domain and the map to its
dependent scalar state. A support-point provider declares a semantic preimage
on segment, circle, circular arc or one Locus V2 branch/component. Nested loci
remain ordinary DAG dependencies; session reentry detection is defense in
depth, not a hidden graph. Durable preimage address and revision/currentness
binding are separate records.

G9P does not freeze the mapped-scalar command spelling. G9U0 must inspect actual
GeoGebra overload/localization/XML conventions, compare alternatives and present
the selected public surface for author review while preserving this semantic
contract. The rich metric result remains authority; standard total
`Length[GeoLocusV2]` is required only as its scalar-admissibility-guarded child,
and legacy `Length[GeoLocus]` is unchanged.

The approved GeoCeDG Classic diagnostic policy preserves/recomputes/saves/
reopens supported native V2/rich/spatial types and IDs/tokens/bindings under the
same kernel while creation UI is disabled. External upstream distributions that
do not know those persisted types are outside the guarantee; G9A3/U0 must test
the unsupported-open boundary and never implement lossy downgrade.

## DXF

The G5 path is `GeoElementGeometryExportAdapter -> GeometryExportModel ->
DxfExporter`. It exports exact resolved 2D entities in unitless world
coordinates and reports unsupported objects. The exporter is read-only and
view-independent. The normative G9X1 contract permits approved explicit
approximation, but it is not implemented. Sidecars are mandatory for every
fidelity reduction and optional for all-exact output; partial export rejects by
default, and any future partial option requires explicit intent, warning and a
sidecar. Unbounded non-native curves require an explicit semantic domain.

## Scientific references and closeout

Scientific sources motivate requirements but do not choose algorithms or
tolerances. Use `docs/references/cedg/catalog.yml` and the
traceability documents; preserve PDF hashes/provenance.

At closeout report inspected/changed files, architectural layer, semantic and
compatibility effects, verifier command/exit/log, skipped checks and risks.
Commit intentionally, create an annotated phase tag only after author approval,
and fast-forward promotion branches without rewriting shared history.

## Approved G9 architecture

G9P designed operational bundles, public V2 exposure, extended DXF, workspaces
and spatial/projection semantics. The six specifications are normative and ADR
0010–0015 are Accepted. G9O1 alone is authorized and not started; all other
productive phases remain future-gated and unauthorized.

Phase documents distinguish hard semantic/contract dependencies, recommended
execution predecessors and global/release gates. G9O1 is recommended first but
is not a semantic prerequisite of G9A1. After G9A3, G9B/G9C can progress
without G9U1; X1 can consume internal semantic snapshots without a hard U0
dependency, while the recommended product schedule still runs U0 before X1 and
integrates both in U1/global closeout.

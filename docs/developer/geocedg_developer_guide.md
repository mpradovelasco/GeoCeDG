# GeoCeDG developer guide

- Status: current-state first edition
- Baseline: GeoGebra 5.4.928.0 at
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- G9 state in this guide: G9O1, G9A1–G9A3/G9A, G9U0/G9U0-R1 and G9X1
  `PASS — AUTHOR APPROVED`; G9U0-R2 planning and implementation also
  `PASS — AUTHOR APPROVED` after the bounded R2-L11 correction and accepted
  interactive re-smoke; G9U0-R3 is also `PASS — AUTHOR APPROVED` after its
  bounded long-token layout correction and author re-smoke; G9U0-R4 is also
  `PASS — AUTHOR APPROVED` after three retained intermediate smoke failures,
  replacement automated PASS and final four-root/point-reactivation re-smokes
  `PASS`. G9U0-R5 design and implementation are `PASS — AUTHOR APPROVED` after
  the final dynamic-factor characterization. G9S1 is also `PASS — AUTHOR
  APPROVED` using Option B after the partial-length re-smoke; G9U1 remains unexecuted;
  G9U1/G9B/G9C remain unauthorized
- G9U0-R2 closeout: `selfApproved=false`, `authorApproved=true`,
  `passClaimed=true`; installed MSI/registry smoke `NOT_REQUESTED`

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
- `tools/knowledge`: deterministic source/knowledge bundle generation, artifact
  verification and disposable Git fixtures;
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

G9U0-R2 uses the same operational architecture. Its focused verifier is
`tools/agent/verify-g9u0-r2-product-refinement.ps1`, and its paired composed
block runs after G9X1 and before any future G9U1 block. The R2-L11 source/fixture
correction invalidated every prior tuple; replacement focused, historical,
ancillary and composed evidence now passes. Run the shared and Desktop
test filters as separate Gradle invocations; a global `--tests` filter must not
make either module seek the other module's classes. Two identical focused runs
compare normalized archive-entry/XML evidence and canonical summaries only
after `R2-R01`–`R06` have been recorded separately.

G9U0-R3 follows that same paired architecture with
`tools/agent/verify-g9u0-r3-public-locus-ui-hardening.ps1`. Its composed block
runs after R2 and before any future G9U1 gate. Each focused execution runs 20
R3 cases plus 17 existing Desktop profile/runtime/localization/tool-surface
cases; the deterministic summary hashes only the four R3 productive/test source
paths and test/scenario outcomes. Generated logs remain ignored. The author
smoke is a separate required gate and is not passed by the verifier.

G9U0-R4 adds the paired candidate verifier
`tools/agent/verify-g9u0-r4-intersection-admissibility-continuation.ps1` after
R3. Its shared-kernel resolver runs after numeric isolation/refinement and
before the existing atomic token-ledger commit. A first current, transverse,
locally isolated, unambiguous non-pair root receives a fresh opaque allocation;
global completeness stays orthogonal. The first author smoke confirmed initial
materialization but found one point undefined after regular motion. The
corrective contract makes current-snapshot deterministic selection authoritative.
Component lineage plus typed oriented contact germ form the base selector. When
that base selector repeats, ADR 0017 permits only an intrinsic phase/rank induced
by pairwise-disjoint canonical intervals in the explicit oriented component;
orientation, domain kind and verified group cardinality complete the selector
context. A unique selector resolves one exact ledger-v4 token. Canonical
parameter bits remain revision evidence, not identity; previous/current matching
is topology/continuity diagnostics only. Solver/list/UI order, parameter
equality, Cartesian coordinates, movement history and render state remain
forbidden. Periodic prior-token reuse uses an adaptive intrinsic phase-tube/cell
certificate separated from the durable selector; `component span / 256` is not
topology evidence. Same-rank tubes must remain disjoint inside their cyclic root
cells. Insufficient/nonunique cyclic evidence durably quarantines the group,
unique offset zero releases/reactivates and proved nonzero offset retires;
ordinary UI-sized regular motion remains independent of update granularity.

`LocusIntersectionTokenLedger2D` exports v4, imports canonical phase-v3 and
authentic pre-phase v2/v1, and distinguishes active, claimed-active,
claimed-dormant and periodic-quarantine allocations. `AlgoLocusIntersectionPointV2` retains/releases the
exact materialized claim; `GeoLocusIntersectionResult` validates or authorized-
copy-rebases a current-or-dormant token. During ordinary absence the existing
`GeoPoint` is undefined, then normal kernel recomputation reactivates that same
object only when the same selector again resolves uniquely. It never retargets
or creates a new point. `LocusIntersectionResult2D` indexes unique locally
point-admissible tokens for exact lookup. An unresolved candidate suppresses
admissibility only on its own semantic component. Canonical contact orientation
makes a line, conic or regular polynomial's equivalent nonzero
homogeneous/projective representation invisible to root binding, while an
oriented ray deliberately retains its direction as semantic evidence.

The current source declares 27 public-kernel, 28 ledger and three Desktop
native-archive methods (58 total), including all 24 solver permutations,
direct/incremental/reverse/reopen path independence, the author-sized direct
update, 2→4→2 same-point recurrence/no-creation, active/dormant reference counts,
dormant copy rebase, explicit permanent retirement, versioned import,
orientation/cardinality barriers, selective periodic monodromy and topology
negatives. The byte-exact four-root fixture proves four unique intrinsic
selectors and four materializable exact tokens without relaxing its estimated-
error or `NOT_ESTABLISHED` completeness evidence. The historical replacement
composed run exposed the missing localized value for
`DETERMINISTIC_SELECTION_ESTABLISHED`; adding the same key
to the base, English and Spanish bundles is a bounded resource correction with
no kernel-semantic effect. The current candidate inventory is 54 paths,
including 31 under `source/`. Checkpoint `4ef2c9d` and its 50-method summary are
pre-current-correction evidence. Current focused A and B each pass 58/58 with an
exact normalized-summary match at SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`;
the composed authority exits 0 with
`All GeoCeDG verification gates passed.`. Automation did not approve the phase;
the author separately accepted both final re-smokes.

Periodic `q`/`r` persistence is covered at the ledger recompute,
canonical-export/import and exact-copy seams. The third Desktop test instead
covers native `.cedg` dormant `2 -> 4 -> 2` same-point reactivation and reopen
after reactivation; it does not claim a native periodic-quarantine round trip.
This missing evidence is tracked as nonblocking risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP`, due for G9U1 revisit and explicit
resolution/disposition by global G9 closeout; it is not an implicit R5
dependency.

The incremental selector/reactivation seam is `O(R log R + P)` for `R` current
roots and `P` existing materialized bindings: one canonical root ordering plus
direct selector lookup, with no solve per child and no movement-history replay.
The legacy whole-transition diagnostic may remain `O(R²)` in the worst case;
it is non-authoritative.

G9U0-R5 design and implementation are `PASS — AUTHOR APPROVED`.
`AlgoLocusSimilarityTransform2D`
publishes a new first-class `GeoLocusV2` with its own durable identity and
normal dependencies on the source plus the ordinary transform inputs.
`LocusSimilarityEvaluator2D` evaluates the source first at the unchanged
branch/parameter address, propagates source invalidity, and only then maps the
finite point through immutable `LocusSimilarityTransform2D` data. The seven
ordinary 2D forms are translation, rotation about the origin or a point,
reflection in a line or point (`Reflect`/`Mirror` aliases), and uniform dilation
about the origin or a point.

For finite `k=0`, the output retains the source domain classification
(`FINITE` or `UNBOUNDED`) and adds `COLLAPSED_IMAGE`; it is not converted to an
unparameterized point. That semantic property is the proof used by the metric
seam to return exact zero on valid collapsed components, while invalid source
addresses and gaps remain invalid. Reflection normalizes homogeneous line
coefficients with scale-safe finite arithmetic. Axis/plane/spatial-center 3D
routes fail closed before generic host routing. Participation and redefine use
one exception-safe rollback seam, so an R5 transform rejected before its own
publication leaves no attached R5 algorithm, ID registration or partially
published R5 output. This does not change upstream retention of a successful
nested subcommand when a later unrelated outer command fails.
Transformed intersections use the R4 identity system in a new transformed
source-pair context and therefore mint new selectors/tokens rather than reusing
source-query tokens. R5 does not add `Path`, rendered-sample
authority, G9U1 workspace behavior or candidate markers.

The final byte-exact dynamic-dilation fixture proves that slider-driven
`GeoNumeric.setValue()`, explicit editing of the existing Algebra-row numeric,
repeated positive/negative/zero transitions and save/reopen all recompute the
same live R5 parent. Free input `k=0.25` is rejected upstream of R5 as
`REDEFINE_CONTEXT_MISSING` by the accepted G9A redefine contract; that rejection
is atomic and did not reproduce construction corruption. The author accepted
this as a nonblocking product-UX limitation. R5 deliberately does not broaden
G9A. Future G9U1 design must investigate a compatible-redefine command-context
seam in which a label locates an explicitly intended current object only for the
transaction, never supplies durable identity, and ambiguous, absent or
incompatible targets fail closed with normal undo/redo and persistence.

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
`CommandProcessor`, algorithm and localization paths. G9U0 exposes the
author-approved experimental/default-off public Locus V2 creation, metric and
intersection surface with durable generator/result/token persistence.
`GeoLocusV2` remains non-`Path`; G9U0-R2 adds only ordinary
GeoElement presentation capability and derived-render coverage. It does not
move metrics, intersections or identity into the drawable.
The host-level `updatePresentationRepaint()` seam defaults to the historical
`updateRepaint()` behavior; R2 uses a `GeoLocusV2` override only to prevent
ordinary Properties changes from cascading into semantic dependents. Do not
use that hook as a second style or revision model.
The R2-L11 correction lets `LocusRenderCache2D` reach a periodic presentation
seam only for one component equal to both declared branch/provider domains on
a periodic branch/provider. This full-period predicate must not close an
ordinary half-open interval or bridge disconnected components. The exact
author fixture and fixed/adaptive negative controls are operational authority.

## Frontend/profile

`apps/geocedg/application-profile.yml` declares the conservative default
perspective/toolbar. Desktop compiles it through GeoCeDG profile classes. Saved
layouts may restore their own toolbar/perspective. Feature manifest membership
is metadata; do not assume it is a complete runtime flag service. G9U1
workspace/profile schema v2 remains unauthorized and is not part of R2.

`GeoGebraMenuBar.updateFonts()` clears every top-level menu so upstream
`BaseMenu` instances can rebuild lazily. `GeoCeDGMenuBar` is an ordinary
`JMenu`; R3 therefore repopulates it after the inherited clear using the same
single `populateProductMenu()` method used at initialization. Do not add a
second action registry or intersection flag. The sole public V2 opt-in remains
`--enableLocusV2=true`.

The exact-token materialization dialog creates an auxiliary `GeoText` because
the token is a reconstructible input of `AlgoLocusIntersectionPointV2`. R3
keeps that normal DAG/XML dependency and sets only Euclidian visibility false at
creation. Do not replace it with coordinates/indexes, change token generation,
or use layers as semantic state. Candidate-marker overlays remain future G9U1
presentation and are absent from R3.

After the first complete R3 composed run passed, a prospective post-R3 prompt
captured the future operational contract. R5 preserves that file and the
original G9P prompt as unchanged history. Its frozen post-R5 successor is
`.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r5.prompt.md`
(canonical-LF SHA-256
`0b96f571932144f9a99f7681938edf756c8999cb847b61095f82430068e96389`).
The definitive prospective candidate is now
`.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md`
(canonical-LF SHA-256
`45e919c5415f7824fd6d18e01ed46f8f23871adcc6fc0821d653caa82d8c6dad`).
It remains unexecuted/not authorized and supersedes the post-R5 prompt only
after G9S1 PASS plus separate authorization of this exact hash. It keeps
candidate markers as non-persistent active-result
overlays derived from current deterministic root/evidence authority, fixes the
existing host Continuity option OFF in GeoCeDG while retaining configurable
  Classic behavior, consumes the exact kernel token bound to the intrinsic
  phase/rank selector without computing a UI rank. It requires create-one,
  create-selected and create-all actions, one coherent undo transaction per
  multi-point action, and an inspector session that can remain open and identify
  already materialized choices. Optional auto-materialization remains one explicit
visible frontend transaction after a user-requested Intersect action. Kernel
recompute/load/topology changes never create DAG nodes. It retains the eleven
professional action groups and reserves
`geocedg.brand.topbar` and `geocedg.brand.startup` as distinct logical roles in
the existing asset-provenance seam. Their intended future author sources are
`helixTopBar.png` and `helixSnapshot.png`; R3 integrates neither. The startup
source may produce application/package derivatives only after small-size and
platform suitability checks. No icon, palette, workspace or marker
implementation is part of R3.

## Persistence and compatibility

Legacy `.ggb` compatibility behavior must remain non-destructive unless a
versioned migration is approved. New semantic objects require stable IDs,
semantic version, reconstructible inputs, copy/undo/delete behavior and XML
round-trip tests before public use. G9U0/G9A provide those public persistence
contracts for their approved experimental objects.

Accepted ADR 0016 and the native-document specification define `.cedg` as the
GeoCeDG-native filename identity while retaining the validated ZIP/XML
machinery and `app="classic"`; `.ggb` remains compatibility input. The
G9U0-R2 implementation contains Desktop routing, corrupt-input preflight,
transactional publication/rollback, Classic preservation and Windows
installer-only `.cedg` association. Replacement focused A/B, historical,
ancillary and composed evidence for the R2-L11 correction passes, and the author
accepted the interactive re-smoke. A real installed MSI/registry probe was not
requested and must not be inferred from static packaging validation.
Do not infer geometric identity from a filename or offer `.ggb` as native Save
output.

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

G9P did not freeze the mapped-scalar command spelling; G9U0 subsequently
selected and author-approved the public surface against actual
overload/localization/XML conventions. The rich metric result remains authority; standard total
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
view-independent. The author-approved G9X1 implementation permits approved
explicit approximation behind its experimental/default-off gate. Sidecars are mandatory for every
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

## Deterministic source and knowledge bundles

Generate only from the Git-index inventory and a declared profile. A clean tree
is required by default:

```powershell
.\tools\knowledge\build-knowledge-bundle.ps1 -Profile operational
.\tools\knowledge\verify-knowledge-bundle.ps1 `
  -BundleDirectory artifacts\knowledge\operational-<bundle-id>
.\tools\agent\verify-knowledge-bundles.ps1
```

The generator prints the exact bundle directory. Use `-AllowDirty` only for
explicit diagnostic evidence; the manifest records staged, unstaged and
untracked hashes and marks the result `NON_RELEASE_EVIDENCE`. Do not add a
generated bundle to Git. The independent verifier rejects stale `HEAD`, changed
dirty state, unsafe paths, membership/ownership drift, invalid continuation
topology, archive metadata drift and budget violations.

Profiles `source`, `knowledge`, `locus-v2`, `governance`, `frontend-dxf`,
`spatial-g9` and `operational` are declared in the normative profile catalog.
Generated/restricted material cannot be admitted by those ownership lists.

## Approved G9 architecture

G9P designed operational bundles, public V2 exposure, extended DXF, workspaces
and spatial/projection semantics. G9O1, G9A1–G9A3/G9A, G9U0/G9U0-R1 and G9X1
are **PASS — AUTHOR APPROVED**. ADR 0010–0016 are Accepted; the R2 Locus
presentation and native-document specs are normative. G9U0-R2 planning and
implementation are author-approved. Its original R2-L11 smoke failure remains
historical evidence; the bounded correction, all automated evidence including
composed verification, and the author re-smoke pass.
`selfApproved=false`, `authorApproved=true`, and `passClaimed=true`.
G9U0-R3 is separately `PASS — AUTHOR APPROVED` after the initial smoke exposed
the long-token width defect, replacement automation passed and the author
re-smoke accepted the correction; `selfApproved=false`, `authorApproved=true`,
`passClaimed=true`.

R3 keeps opaque exact tokens out of layout-sizing labels as well as out of
ordinary Graphics presentation. The rich-result chooser renders a localized,
snapshot-only solution ordinal plus contact classification, but retains and
passes the complete token unchanged. The ordinal is not persisted, is not
continuation identity and is never command input; the diagnostic text may show
the exact token only inside its wrapping/scrolling bounded viewport.

G9U0-R4 is `PASS — AUTHOR APPROVED`.
The closed phase corrects the false first-publication deadlock in the public rich
intersection path while retaining the accepted Option B distinction between
local point admissibility and global enumeration completeness. It introduces
no new target solver, frontend identity rule or marker. Historical smoke 1
confirmed initial admissibility but invalidated a child during regular motion;
historical smoke 2 exposed two repeated base-selector pairs among four otherwise
admissible roots. ADR 0017 then authorized intrinsic oriented-domain phase/rank
inside such a collision group. A third historical smoke passed Case A and the
initial four-selector/token/materialization state, then failed ordinary
regular-motion persistence because the fixed transition-span guard was not
topology evidence.

The current adaptive phase-tube and ledger-v4 correction preserves ordinary
motion, selectively fails a proved true seam, keeps an ordinary claimed absence
dormant and retains insufficient periodic evidence in durable quarantine until
offset-zero release or proved nonzero retirement. Existing-point reactivation is an automatic kernel lifecycle
event under the same selector; new-point auto-materialization remains absent and
may only be a future explicit G9U1 frontend transaction consuming current tokens.
Unresolved evidence remains component-scoped; equivalent homogeneous or
projective target forms preserve binding, whereas ray direction remains semantic.
The source declares 58 methods and a 54-path/31-source inventory. Checkpoint
`4ef2c9d` is pre-current-correction. Replacement focused A/B pass 58/58 with
exact canonical SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`,
and composed verification passes; the final four-root regular-motion and
active/dormant/reactivation author re-smokes both pass. No
`DETERMINISTIC_LOCAL` certification relaxation was
implemented. `selfApproved=false`, `authorApproved=true` and
`passClaimed=true`.

G9U0-R5 design and productive similarity transformations are `PASS — AUTHOR
APPROVED`; `selfApproved=false`, `authorApproved=true`, `passClaimed=true`.
The accepted manual-smoke disposition is `PASS WITH G9A FREE-INPUT LIMITATION
CHARACTERIZED`. G9U1 remains unexecuted and unauthorized.

G9S1 is an author-approved product gate. Option B
introduces a new experimental `SplineV2` parent that publishes a new
`GeoLocusV2` with an explicit oriented domain, stable semantic spans/knots and
normal source dependencies. Classic `Spline` is not modified or migrated.
The output consumes the existing Locus V2 Point, rich-length, guarded scalar
`Length`, rich-intersection, R4 token/reactivation, R5 transform and persistence
authorities. Spline-aware
code may contribute span bounds, local polynomial evaluation and canonical
knot ownership below those authorities; it must not add parallel public
metric/token systems or infer identity from a Java array slot, solver order,
coordinates or render samples.

One-sided polynomial targets are composed with each provider span and partitioned
at recursively isolated derivative roots. Only an established transverse local
cell may enter the existing R4 selector/token ledger; evidence remains floating
and global completeness remains `NOT_ESTABLISHED`. Bounded functions retain the
existing general rich fallback. The polynomial pair capability is intentionally
rich-only: deterministic Bernstein-hull boxes and dual Newton provide diagnostic
finite/overlap/work evidence, but no interval-rounded rectangle+uniqueness
certificate, public continuation key, active ledger allocation or materialized
pair point. Do not let Desktop/G9U1 infer the missing pair selector from order or
coordinates.

Metric consumers split at provider knots and reuse deterministic adaptive
Simpson integration over the analytic semantic derivative. Knots, coefficients,
bounds and solver cells are derived from the ordinary list/degree/optional-weight
dependencies and are not separately serialized.

The partial scalar surface is `Length(L,P,Q)`; `LocusLength(L,P,Q)` remains the
rich, deliberately nonnumeric result. The former is built only as a hidden
reconstructible rich between-position query followed by
`AlgoLocusMetricScalarAdapter`. Evaluator-only sources establish their route
evidence by bounded adaptive evaluation on the exact semantic interval. They do
not interpolate a complete-component error estimate, use render samples or
weaken `isScalarAdmissible()`. Invalid endpoint provenance or transient
source/endpoint invalidity publishes undefined scalar state and recovers by
normal DAG recomputation.

The approved design and API are
[documented here](../architecture/g9s1_semantic_spline_2d_capability.md) and
[here](semantic_spline_2d_api.md). ADR 0018 is `Accepted`, the spec is
normative and all numerical certification/completeness claims must
match the selected source-backed method and focused evidence. Missing
scholarly support remains an explicit research requirement, never an invented
citation. The retained periodic-quarantine round-trip risk remains open.

The R5 `k=0.25` free-input limitation remains G9A
`REDEFINE_CONTEXT_MISSING`; G9S1 does not broaden redefine or use labels as
identity. The post-G9S1 G9U1 design retains the atomic compatible-redefine UX
requirement.

Phase documents distinguish hard semantic/contract dependencies, recommended
execution predecessors and global/release gates. G9O1 is recommended first but
is not a semantic prerequisite of G9A1. After G9A3, G9B/G9C can progress
without G9U1. The product schedule is G9U0-R1 plus G9X1 → G9U0-R2 → G9U0-R3 →
  G9U0-R4 → BOOK-P1 → G9U0-R5 → G9S1 → G9U1. BOOK-P1 is an independent editorial
  checkpoint, not a software dependency. R3, R4, R5 and G9S1 are closed. G9U1
  still requires its own execution authorization.
G9B/G9C remain semantically independent,
G9U2 remains globally blocked and productive G10 remains unauthorized.

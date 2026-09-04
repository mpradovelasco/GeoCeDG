# GeoCeDG - Repository Agent Instructions

## 0. Status and purpose

This file is the root authority for automated agents working in the GeoCeDG repository.

GeoCeDG is a source-based fork of GeoGebra oriented to Computer-Extended Descriptive Geometry (CeDG). It is not a conventional CAD product. Its purpose is to provide a dynamic geometric kernel and a dedicated application for explicit descriptive-geometry constructions, orthographic projections, construction dependencies, locus-based procedures, surface intersections, developments, measurements, validation, and controlled interoperability.

This document applies to the whole repository unless a more specific `AGENTS.md` exists in a subdirectory. A nested file may add stricter local rules, but it may not weaken the geometric, licensing, compatibility, provenance, or validation rules defined here.

## 1. Fundamental CeDG principles

Every design and implementation shall preserve all of the following:

1. **Constructive traceability.** A result must remain linked to the explicit sequence of geometric constructions that produces it.
2. **Explicit parameterization.** Continuous and discrete parameters, their domains, and their validity ranges must be represented explicitly.
3. **Procedure/result separation.** A construction procedure is not interchangeable with its rendered or sampled result.
4. **Geometric validity.** Algorithms must define their preconditions, invariants, degeneracies, and failure states.
5. **Degeneration control.** Tangencies, coincident entities, discontinuities, branch creation or loss, unbounded components, and near-singular configurations must be handled deliberately.
6. **Projection coherence.** Related projections and spatial entities must remain mutually consistent.
7. **Dynamic dependency.** Changes in inputs must propagate through the kernel dependency graph without hidden recomputation outside that graph.
8. **No silent approximation.** Any numerical or discrete approximation must be explicit, tolerance-controlled, reproducible, and distinguishable from an exact or symbolic result.
9. **CeDG is not CAD.** Do not replace construction-based geometry with a feature tree, solid-first workflow, or generic CAD abstraction merely because it is familiar.
10. **Spatial identity and projection sufficiency.** A 3D object and its projections must be linked by stable, typed, serializable relations. Never infer spatial identity from labels, creation order, or visual coincidence, and never declare a projection set defining without type-specific sufficiency and non-degeneration checks.

## 2. Authority hierarchy

When sources disagree, use this order:

1. Current repository code, tests, build configuration, and serialization contracts.
2. Approved specifications and Architecture Decision Records under `docs/architecture/`, `docs/adr/`, and `geocedg/specs/`.
3. Canonical CeDG models, analytic invariants, and approved regression baselines under `models/` and `geocedg/validation/`.
4. Current official upstream GeoGebra documentation and the exact upstream commit recorded in `docs/upstream/BASELINE_COMMIT.txt`.
5. CeDG papers and books recorded in `docs/research/`.
6. Generated reports, screenshots, binaries, exports, or previous agent summaries.
7. Conversation history and informal notes.

Generated artifacts are evidence, not source authority. Never infer source semantics from a binary, screenshot, installer, or generated report when the governing source is available.

## 3. Repository boundaries

### 3.1 Upstream source

The inherited GeoGebra tree remains in its upstream layout. Do not perform broad package renames, mass formatting, file moves, or cosmetic rewrites in upstream-owned paths.

Primary upstream areas include:

- `source/shared/common`: shared kernel, commands, algorithms, geometry objects, serialization, and application contracts.
- `source/shared/*`: shared supporting modules.
- `source/desktop/*`: desktop application and Swing/AWT frontend.
- `source/web/*`: web frontend and related modules.
- `gradle/`, `settings.gradle.kts`, and existing build logic.
- `doc/dev/`: upstream development documentation.

Changes inside these paths must be the minimum necessary and must be justified by an approved specification or ADR.

### 3.2 GeoCeDG-owned durable sources

GeoCeDG-specific durable sources belong in:

- `geocedg/specs/`: command, object, spatial, projection, visibility, drawing, performance, UI, export, and serialization specifications.
- `geocedg/features/`: stable and experimental feature manifests.
- `geocedg/resources/`: GeoCeDG-owned icons, styles, translations, and branding.
- `geocedg/validation/`: invariant definitions, tolerance policies, analytic references, and validation baselines.
- `docs/architecture/`: architectural maps and contracts.
- `docs/adr/`: accepted decisions and alternatives.
- `docs/research/`: research-to-requirement traceability.
- `docs/roadmap/`: phase gates and status.
- `models/`: canonical, research, legacy, and regression `.ggb`, `.ggt`, scripts, and manifests.
- `tools/agent/`: deterministic agent entry points and validation orchestration.
- `tools/build/`: reproducible build helpers.
- `tools/release/`: packaging and release helpers.
- `tools/benchmark/` and `benchmarks/`: reproducible performance harnesses and model suites.
- `packaging/`: platform-specific `jpackage` resources and installer definitions.
- `.github/prompts/`: canonical task and review prompt files.
- `ai-shell/prompts/`: short interaction profiles that reference canonical prompts.
- `apps/`: product contracts and future clients; geometric semantics do not live here.
- `python/`: future CeDG DSL, orchestration, analysis, and validation layers.
- `artifacts/`: generated outputs; ignored by Git except explicit manifests.

## 4. Architectural placement rule

Before coding, classify the requested capability.

### Implement in the Java kernel only when the capability:

- changes the semantic meaning of a geometric object;
- must participate in the construction dependency graph;
- requires internal dynamic evaluation;
- affects path/region membership or geometric incidence;
- affects stable object identity or serialization;
- must be consumed correctly by multiple frontends; or
- cannot be implemented correctly through a read-only external service.

### Implement outside the kernel when the capability is:

- a composed algorithm over existing valid primitives;
- orchestration, batch analysis, validation, or reporting;
- a GUI layout or product profile;
- an installer or release concern;
- an import/export adapter;
- a research prototype whose semantics are not yet approved; or
- a notebook/DSL/workbench feature.

When uncertain, document both options in an ADR and recommend the least invasive sustainable option.

### Current placement decisions

- GeoCeDG application profile, perspectives, toolbar organization, and feature selection: desktop/application layer.
- Own branding, icons, styles, and translations: GeoCeDG resources and application layer.
- Own installer: packaging/release layer.
- Prompt files and operational automation: `.github/prompts/`, `ai-shell/prompts/`, and `tools/agent/`.
- DXF export: export service outside the geometric kernel, consuming a read-only geometry export model.
- Locus metric semantics, parameterization, path behavior, length, incidence, and intersections: Java kernel.
- 3D object identity, projection bindings, canonical projection schemas, reconstruction, and projective boundary semantics: shared Java kernel/semantic layer.
- Geometric visibility in a projection: shared Java projection service integrated with spatial objects and dependencies.
- Hierarchical layers, locking, print/export state, and named view state: shared document/application model, not geometric truth.
- Zoom and navigation: frontend/view layer; never metric authority.
- 3D-view conversion: shared adapter from kernel-owned spatial semantics to the existing 3D view; the view is not an independent authority.
- PDF/SVG sheet generation: document/export service outside the geometric kernel.
- Performance: cross-cutting instrumentation from baseline; optimization only after profiling.
- Locus and spatial-semantics research benchmarks and numerical validation: external validation layer plus kernel tests.
- CeDG DSL and high-level model orchestration: Python, only after spatial semantics are available.
- Workbench: client of Java/Python services; no independent geometric truth.

## 5. Feature maturity and non-permanent additions

Every GeoCeDG addition has one maturity state:

- `legacy`: existing CeDG tool/model imported for preservation and characterization;
- `research`: exploratory implementation without a stable public contract;
- `experimental`: integrated behind a feature flag with tests and a written specification;
- `stable`: public, documented, backward-compatible, and included in default GeoCeDG behavior;
- `deprecated`: retained only for compatibility with a documented migration path.

Do not make a research or experimental tool permanent merely by adding it to the default toolbar.

Existing GeoGebraScript tools, `.ggt` tools, buttons, scripts, and models shall initially be imported under `models/legacy/` with a manifest containing:

- source/provenance;
- author and date if known;
- required GeoGebra version;
- input and output objects;
- known validity range;
- known degeneracies;
- expected numerical metrics;
- whether it is loaded by default;
- replacement or promotion candidate.

The default GeoCeDG frontend must be generated from a feature/profile manifest. It shall not be assembled by scattered hard-coded changes to individual buttons.

## 6. Frontend and product profile

Create a dedicated GeoCeDG application profile rather than deleting or mutating the classic application globally.

The profile shall define, at minimum:

- product/application code and preference namespace;
- default perspective and dock layout;
- default visible views;
- CeDG toolbar categories;
- command and tool filters;
- feature flags;
- GeoCeDG branding and resources;
- default numeric/tolerance policy;
- optional access to a diagnostic "Upstream Classic" perspective.

Recommended CeDG toolbar categories:

1. Selection and construction control
2. Projections and view transformations
3. Points, lines, planes, and incidence
4. Spatial objects and projection associations
5. Curves and surfaces
6. Intersections and loci
7. Developments and true magnitudes
8. Measurement and validation
9. Layers, visibility, and presentation
10. Automation and user tools
11. Import/export

A "CeDG Laboratory" mode may load experimental `.ggt`/script bundles without changing the stable default profile.

## 7. Licensing and provenance

The current upstream source code is EUPL-1.2 licensed. Other GeoGebra components have separate terms. In particular, installers, language files, documentation, UI images, styles, icons, logos, and trademarks must not be assumed to be covered by the source-code license.

Required repository records:

- `LICENSE` and `LICENSES/`;
- `NOTICE.md`;
- `THIRD_PARTY.md`;
- `docs/licensing/component-matrix.md`;
- `geocedg/resources/assets-manifest.yml`;
- exact upstream repository URL and commit;
- a record of modified upstream files and the purpose of each modification.

Rules:

- Preserve upstream copyright notices.
- Mark modifications as required by the EUPL.
- Use the name **GeoCeDG**, not GeoGebra, for the redistributed fork.
- Do not ship the upstream installer.
- Do not ship upstream trademarks or non-code assets in a supposedly unrestricted distribution unless the applicable license is accepted and documented.
- Prefer GeoCeDG-owned icons, styles, translations, and installer resources.
- Treat commercial/public redistribution as a release gate requiring a fresh license audit.
- Do not provide legal conclusions; record facts and unresolved questions for human review.

## 8. Upstream synchronization

`upstream` is the official GeoGebra repository. `origin` is the GeoCeDG repository.

The exact starting commit must be recorded in:

- `docs/upstream/BASELINE_COMMIT.txt`;
- `UPSTREAM.md`;
- an annotated Git tag.

Do not track an unpinned moving state in release documentation.

Recommended branches:

- `main`: releasable GeoCeDG states only;
- `integration`: approved feature integration;
- `feature/<name>`: isolated work;
- `sync/geogebra-YYYYMMDD`: temporary upstream synchronization;
- `research/<name>`: non-product experiments.

Upstream synchronization must be performed through a dedicated branch and pull request. Before merging, run the full validation authority in `tools/agent/verify.*`.

Never use force-push, history rewriting, or a destructive reset on shared branches without explicit human instruction.

## 9. Design-before-code protocol

Before implementation, produce or update:

1. **Geometric definition**
   - objects, domains, branches, orientation, and dependencies;
   - exact versus approximate semantics;
   - invariants and degeneracies.

2. **Upstream impact analysis**
   - existing classes and interfaces;
   - serialization and backward compatibility;
   - command dispatch;
   - rendering versus semantic data;
   - expected frontend effects.

3. **Extension design**
   - public and internal APIs;
   - cache/invalidation behavior;
   - numerical tolerance policy;
   - migration strategy;
   - rejected alternatives.

4. **Validation design**
   - analytic reference cases;
   - canonical CeDG models;
   - dynamic and degeneracy cases;
   - performance limits;
   - deterministic output requirements.

Only then implement.

## 10. Command development

Follow the upstream command architecture rather than bypassing it:

- command/localization registration;
- algorithm class in the shared kernel;
- explicit `setInputOutput()` and dependency registration;
- `compute()` with defined invalid states;
- command processor and dispatcher integration;
- serialization behavior;
- unit and integration tests;
- GeoCeDG specification and user-facing documentation.

A command must not read the screen scale, viewport, or rendering state unless its documented purpose is visual.

## 11. Locus V2 - mandatory semantic contract

### 11.1 Mathematical definition

A locus is not identical to a sampled polyline.

Let the driver domain be

\[
\Omega = \bigcup_{j=1}^{m} I_j,
\]

where each \(I_j\) is an oriented parameter interval or path component. Let the dependent construction define

\[
F_j : I_j \setminus D_j \rightarrow \mathbb{R}^2,
\]

where \(D_j\) contains undefined or invalid parameter values. The geometric locus is

\[
L = \bigcup_{j=1}^{m} F_j(I_j \setminus D_j).
\]

The finite sample used for display or numerical acceleration is a derived approximation of \(L\), not the identity of \(L\).

### 11.2 Required separation

Locus V2 shall separate:

- `LocusDefinition2D`: driver, domain, construction/evaluator, branches, orientation, and invalid intervals;
- `LocusEvaluator2D`: deterministic evaluation \(F_j(\omega)\), optionally derivatives/tangents and symbolic metadata;
- `LocusMetricIndex`: world-coordinate adaptive partition, cumulative length, error estimates, branch bounds, and spatial index;
- `LocusRenderCache`: screen-oriented tessellation/simplification derived from semantic data;
- `LocusIntersectionSolver`: root isolation and refinement against supported objects.

Rendering data must never be the authority for length, incidence, or intersection.

### 11.3 Parameterization

The canonical path parameter shall be tied to the driver/domain parameter, not to the index of a sampled point.

A compatibility adapter may expose legacy sample-index behavior when loading old constructions, but new Locus V2 objects must serialize an explicit semantic version and parameterization contract.

### 11.4 Length

For an absolutely continuous branch,

\[
s_j(a,b) = \int_a^b \|F'_j(\omega)\|\,d\omega.
\]

If an analytic derivative/integral is unavailable, use adaptive world-coordinate numerical evaluation with a documented absolute/relative tolerance and error estimate. Never describe a sampled chord sum as exact.

Experimental command/API first:

- `LocusLength[L]`
- `LocusLength[L, A, B]`
- optional explicit branch/orientation/tolerance form

After the contract is validated, integrate the stable behavior into the standard measurement command family.

Semantics must define:

- open versus closed loci;
- directed versus shortest arc;
- multiple branches;
- points with multiple preimages at a self-intersection;
- unbounded branches;
- discontinuities;
- zero-length and point loci;
- undefined endpoints;
- dynamic branch creation or loss.

A point constructed *on* a locus should retain its semantic branch and parameter. An arbitrary coincident point is ambiguous when multiple preimages exist and must not be silently assigned.

### 11.5 Intersections

For an implicit target \(G(x,y)=0\), intersections on branch \(j\) solve

\[
h_j(\omega) = G(F_j(\omega)) = 0.
\]

For a parametric target \(Q(v)\), solve

\[
F_j(\omega) - Q(v) = 0.
\]

Use:

1. broad-phase branch bounds/spatial index;
2. candidate isolation with adaptive semantic sampling;
3. narrow-phase refinement in the original parameters;
4. residual and tolerance verification;
5. stable dynamic identity based on branch and parameter, not list order.

A sign-change test alone is insufficient because tangencies may have even-multiplicity roots. Include distance/residual minima and derivative-aware or interval-safe handling.

Staged object support:

1. line, segment, ray, circle, and conic;
2. function and implicit curve;
3. locus-locus and generic path;
4. overlapping/coincident components with an explicit overlap result policy.

### 11.6 Compatibility feature flag

Locus V2 shall initially be guarded by a feature flag such as `cedg.locus.v2`.

Required comparison modes:

- legacy rendering/semantics;
- V2 semantic evaluation;
- dual-run diagnostic comparison.

No old `.ggb` file may silently change result without a compatibility decision, migration note, and regression evidence.

## 12. Spatial object and canonical projection contract

### 12.1 Spatial identity

The spatial object is the stable semantic identity. A projection is a first-class geometric representation bound to that identity.

Minimum concepts:

- `SpatialObject3D`: stable ID, type, parameters, construction dependencies, validity domain, representation status, and projection set;
- `ProjectionFrame`: geometric plane/frame and projection operator, independent of viewport state;
- `ProjectionBinding`: typed relation from one spatial object to one or more 2D kernel objects in a frame;
- `CanonicalProjectionSchema<T>`: versioned type-specific rules for sufficiency, correspondence, reconstruction, and degeneration;
- `CanonicalProjectionCertificate`: dynamic result for a concrete object and construction revision;
- `ProjectiveBoundaryObject3D`: composition of primitives, spatial curves, supporting surfaces, oriented faces, incidence, adjacency, and optional closed boundary.

Do not use display labels, object names, creation order, layer membership, or screen proximity as spatial identity.

### 12.2 Projection roles

Every binding shall declare one role:

- `defining`: participates in reconstruction/canonical sufficiency;
- `derived`: generated from the spatial object;
- `auxiliary`: supports a descriptive-geometry procedure;
- `analysis`: generated for validation or measurement;
- `presentation`: layout-only representation.

A binding shall also record the projection frame, provenance, exact/numerical/discrete status, branch or topology correspondence when applicable, and validity state.

### 12.3 Canonical sufficiency

Let `X_T` be the valid configuration space of type `T`, and let the known projection operators be `Pi = {pi_i}`. The combined map is

\[
\Phi_{T,\Pi}(x)=(\pi_1(x),\ldots,\pi_k(x)).
\]

A set is canonical on a declared domain only when:

1. equality of all projected data implies equality up to an explicitly allowed geometric equivalence;
2. an explicit constructive reconstruction exists;
3. reconstruction followed by reprojection reproduces the input data within the declared exactness/tolerance policy;
4. type-specific non-degeneration and correspondence predicates hold;
5. the reconstruction and its dependencies live in the kernel graph.

The number of views alone is never a sufficiency test.

Examples:

- a point normally requires two known non-parallel orthographic frames;
- a line perpendicular to a projection plane collapses to a point in that view, so another non-collapsed view or equivalent defining primitive is mandatory;
- a plane is defined through sufficient primitives such as three non-collinear points or two incident lines;
- two projected curves define a spatial curve only with explicit point/parameter correspondence; a common Locus V2 parameter is a valid correspondence;
- cylinders, cones, spheres, and other primitives should be canonicalized through their defining centers, axes, vertices, directrices, radii, or equivalent constructions rather than silhouettes alone.

### 12.4 Complex objects and solids

A CeDG complex object is an aggregation of spatial primitives and constructive relations. A solid may be represented through a closed oriented projective boundary:

- vertices and spatial curves/edges;
- supporting surfaces;
- faces with oriented boundary loops;
- incidence and adjacency;
- projection bindings for components and faces;
- closure, orientation, manifold, and validity diagnostics.

This may reuse useful B-Rep concepts, but the authority remains the CeDG construction, dependencies, and bound projections. Do not introduce an opaque CAD feature tree or solid-first workflow as the semantic source.

### 12.5 Dynamic states

The canonical certificate shall expose at least:

- `VALID`;
- `UNDERDETERMINED`;
- `AMBIGUOUS`;
- `INCONSISTENT_PROJECTIONS`;
- `DEGENERATE`;
- `UNDEFINED`.

A change through a singular position updates the state and diagnostics; it must not silently retain stale spatial geometry.

### 12.6 Serialization and compatibility

New objects and bindings must serialize stable IDs, semantic versions, frame definitions, roles, correspondence metadata, and certificate inputs. Old `.ggb` constructions remain unassociated unless an explicit migration or user association is performed.

The initial implementation shall be guarded by a feature flag such as `cedg.spatial.semantics`. The previous JavaScript/list proof of concept is a research reference and regression case, not production persistence.

### 12.7 3D view authority

The first 3D bridge is one-way: kernel-owned `SpatialObject3D` data generate 3D view objects. The 3D view is derived and diagnostic. Reprojection shall be used to check coherence.

Do not maintain an independently editable duplicate of the geometry. Bidirectional 3D editing requires a separate approved policy for propagating changes back into explicit CeDG constructions.

## 13. DXF export

DXF export is an interoperability adapter, not a geometric kernel.

Define a read-only `GeometryExportModel` containing:

- stable object identifier;
- type and dimensionality;
- coordinates/parameters in explicit units;
- layer, name, visibility, and style metadata;
- construction provenance;
- exact/parametric representation when available;
- approximation tolerance and error metadata when not.

Initial scope: 2D CeDG projections and developments.

Suggested entity mapping:

- point -> `POINT`;
- finite linework -> `LINE`;
- circle/arc -> `CIRCLE`/`ARC`;
- polygon/polyline -> `LWPOLYLINE`;
- suitable parametric curve -> `SPLINE`;
- general Locus V2 -> parametric export when representable, otherwise adaptive polyline with explicit tolerance metadata.

Do not embed new geometric solving logic in the exporter. Python may validate files and perform batch conversion, but the live application export should consume kernel-owned geometry through a stable read-only adapter.

## 14. Validation authority

`tools/agent/verify.*` is the executable authority. Prompts and reports must call it rather than duplicating commands ad hoc.

Use the DEV, PHASE, COMPOSED and FULL definitions in `geocedg/specs/operations/verification-levels.md`. Default `verify.ps1` is COMPOSED; DEV is never acceptance evidence. Every task identifies its required level and regression perimeter. Changes to test selection, caching, parallelization, verifier orchestration, bootstrap or numerical baselines are verification-infrastructure changes and require FULL evidence; narrower success does not waive a required FULL gate. The only evidence-preserving verifier-repair exception is the explicitly author-authorized, exhaustively proven identity/provenance correction in section 11 of that specification and ADR 0024; failure of any required equivalence proof restores the normal FULL requirement.

Minimum gates:

- upstream baseline build;
- desktop launch smoke test;
- shared kernel tests;
- GeoCeDG unit and integration tests;
- serialization round trip;
- canonical model regression;
- deterministic rerun;
- license/asset inventory;
- packaging smoke test when packaging changes;
- `git diff --check`.

### Locus V2 invariant suite

Include at least:

- line segment;
- circle and closed orientation;
- ellipse;
- parabola;
- smooth transcendental curve;
- cusp;
- self-intersection;
- multiple disconnected branches;
- discontinuity;
- near tangency;
- coincident/overlap case;
- unbounded branch;
- branch creation/loss under parameter change.

Required properties:

- length and intersections are invariant under zoom, viewport, and DPI;
- cumulative length is monotone on each oriented branch;
- reported intersection residuals satisfy tolerance;
- dynamic intersection identities are stable when topology is unchanged;
- results are deterministic for a fixed construction revision and tolerance;
- invalid/degenerate cases produce explicit status, not stale geometry.

### Spatial/projection invariant suite

Include at least:

- general point and point on a projection plane;
- general line and line perpendicular to each projection plane;
- general and projecting planes;
- circle in a general plane;
- spatial curve with a common parameter across two projections;
- tetrahedron reconstructed from orthographic views;
- cylinder and cone from defining primitives;
- composite oriented boundary object;
- dynamic passage through degeneration;
- inconsistent and underdetermined view sets;
- serialization round trip with stable IDs.

Required properties:

- reconstruction/reprojection consistency;
- certificate independence from zoom, DPI, labels, and layer state;
- stable identity while topology is unchanged;
- deterministic ambiguity and degeneration reporting;
- no stale spatial result after a failed certificate;
- compatibility with legacy unassociated constructions.

### Canonical CeDG regression models

At minimum preserve and characterize:

- cone-cylinder LSIM intersection;
- focal illumination of a sphere;
- oblique cone development using a spherical support curve;
- cylindrical and conical polygonal elbows with discrete ferrule count;
- developable helicoid;
- convolute with proper and non-proper regression-edge zones.

## 15. Prompt-file and operational system

Canonical prompt files belong in `.github/prompts/`.

Recommended structure:

- `.github/prompts/canonical/`: shared governing prompts;
- `.github/prompts/tasks/`: executable task prompts;
- `.github/prompts/reviews/`: audit/review prompts;
- `ai-shell/prompts/`: lightweight `ask`, `plan`, `verify`, `refactor`, and `architect` profiles referencing canonical prompts.

A task prompt must state:

- objective;
- authority hierarchy;
- exact scope and forbidden scope;
- affected architectural layer;
- required specification/ADR;
- invariants and compatibility constraints;
- required commands/gates;
- expected artifacts;
- stop conditions.

Prompt files must reference durable specs. They must not become a second copy of geometric truth.

## 16. Change execution rules

For each task:

1. Inspect repository status and relevant authority.
2. Record assumptions and unresolved contradictions.
3. Produce design before code.
4. Make the smallest coherent change.
5. Add or update tests with the implementation.
6. Run the relevant verification authority.
7. Report:
   - files changed;
   - architectural layer;
   - semantic effect;
   - compatibility effect;
   - test evidence;
   - unresolved risks;
   - generated artifacts and their provenance.

Review bootstrap impact whenever a change affects workstation prerequisites or another assumption consumed by Windows bootstrap. Record the explicit updated/no-change outcome and substantive rationale required by `geocedg/specs/operations/verification-levels.md`, together with infrastructure-impact and required-level evidence. Require justified review, not arbitrary bootstrap edits. Preserve the existing `GUIDE_IMPACT` protocol; technical PASS never implies author approval.

Stop and report rather than guess when:

- source licensing is unclear;
- a requested behavior changes serialized semantics without a migration plan;
- a result cannot be made deterministic;
- a geometric ambiguity has no approved policy;
- the baseline build is not reproducible;
- a proposed change would place geometric truth in a GUI, exporter, script, or generated artifact.

## 17. Prohibited actions

Do not:

- treat CeDG as generic CAD;
- replace explicit constructions with opaque approximations;
- use viewport-dependent samples as metric truth;
- infer a 3D object/projection association from labels, visual overlap, creation order, or layer membership;
- accept a projection set as canonical without a type-specific sufficiency certificate;
- make the 3D view an independent geometric authority;
- conflate object existence, geometric visibility, and layer/UI visibility;
- optimize without a reproducible benchmark and profile;
- promote experimental tools to stable/default behavior without approval;
- duplicate prompt or specification authority;
- edit generated artifacts as if they were sources;
- mix GeoCeDG branding with upstream trademarks;
- redistribute the upstream installer;
- perform broad upstream refactors without a demonstrated need;
- suppress a failing test, geometric degeneracy, or license warning;
- claim exactness without a mathematical basis;
- modify unrelated files to make a patch appear cleaner.

## 18. First repository mission

The first agent task after the human creates and clones the fork is **baseline characterization and scaffolding only**.

The agent shall:

1. record repository state, upstream remote, commit, toolchain, and Gradle project map;
2. reproduce the official desktop build/run path;
3. identify the exact desktop launcher, application configuration, perspective, toolbar, command, kernel, Locus, intersection, serialization, and packaging extension points;
4. identify existing 3D object types, coordinate systems, projection/view transforms, 2D/3D adapters, layer/style/visibility models, print/export paths, and benchmark insertion points;
5. create only the approved GeoCeDG directory scaffold and durable documentation;
6. inventory code versus non-code assets and their licenses;
7. import no legacy CeDG tool until the manifest format exists;
8. modify no Locus or spatial-object semantics and add no product feature;
9. produce a deterministic baseline report and executable verification entry point.

The baseline gate must pass before frontend, installer, DXF, Locus, spatial semantics, layers, visibility, 3D bridge, publication, or performance optimization begins.

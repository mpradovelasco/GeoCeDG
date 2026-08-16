# GeoCeDG — G9P Integrated Analysis, Predesign and Canonical Roadmap Package

## Objective

Execute **G9P — Integrated G9 analysis and predesign**.

G9P is the authoritative design-entry phase after the global author-approved
closeout of G8.

Current expected project state:

G8 =
PASS — AUTHOR APPROVED

G9 DESIGN =
AUTHORIZED
NOT STARTED

G9 IMPLEMENTATION =
NOT AUTHORIZED

The purpose of G9P is to design, characterize and organize the next GeoCeDG
development period before any productive G9 implementation begins.

G9P must jointly analyze and coordinate:

1. native spatial CeDG semantics;
2. association between spatial objects and their 2D projections;
3. public/user-facing activation of the internal G6–G8 Locus V2 capabilities;
4. the mature GeoCeDG Construction workspace;
5. the future post-G9 Dihedral Procedures workspace;
6. extended DXF export for non-basic curves, including Locus V2;
7. user, mathematical, developer and agent-operation documentation;
8. deterministic source/knowledge bundles for use as project knowledge;
9. the detailed roadmap and all canonical future prompts.

This is a **design, audit and bounded-characterization phase only**.

G9P may add:

- specifications;
- ADRs in Proposed state;
- architecture documents;
- validation and benchmark plans;
- canonical future prompts;
- documentation and guides;
- deterministic manifests;
- narrowly scoped test-private characterization probes;
- read-only analysis utilities used only for evidence.

G9P must not add:

- productive spatial kernel implementation;
- productive public Locus V2 commands;
- productive GUI workspace implementation;
- productive extended DXF implementation;
- productive knowledge-bundle generator;
- G9 spatial GeoElements or algorithms;
- public Path behavior;
- new persistence formats;
- G9A/G9B/G9C implementation.

Do not commit, push, promote, tag or create later implementation branches in this task.

Finish with a complete author-decision package.

---

# 1. Repository authority

Work from the repository root.

Read and obey the repository-defined authority hierarchy. At minimum inspect:

1. `AGENTS.md`;
2. every applicable canonical governance, planning and verification prompt;
3. the living roadmap;
4. current user guide;
5. all existing developer guides;
6. all existing prompt/agent-operation guides;
7. GeoCeDG frontend architecture and profile manifest;
8. current G2/G3/G4/G5 frontend, legacy-tool, packaging and DXF artifacts;
9. normative G6 Locus V2 semantic specification;
10. Accepted ADR 0006;
11. normative G7 metric specification;
12. Accepted ADR 0007;
13. normative G8 intersection specification;
14. Accepted ADR 0008;
15. Accepted ADR 0009;
16. G8A/G8B/G8C design and implementation reports;
17. actual current productive Java source;
18. current DXF source and tests;
19. current GeoGebra command/tool/dispatcher/persistence architecture;
20. current `apps/geocedg/application-profile.yml` or its actual successor;
21. the modified-file ownership inventory;
22. the versioned CeDG scientific catalog;
23. the existing legacy `Templatev7.ggb`;
24. the author-provided G9P reference models, screenshot and notes.

Do not treat previous chat text as repository authority.

Do not invent an absent file such as `.github/copilot-instructions.md`.
If a referenced category has no current file, report that fact and use the
actual repository authority.

---

# 2. Preflight and entry gates

Before editing, report:

- repository root;
- current branch;
- local `HEAD`;
- `main`;
- `origin/main`;
- current branch upstream;
- ahead/behind relationship;
- working-tree state;
- GeoGebra baseline version and commit;
- `geocedg-g8-pass` tag object and peeled target;
- confirmation that G8 is closed as `PASS — AUTHOR APPROVED`;
- confirmation that G9 productive implementation is absent;
- confirmation that the G6–G8 focused and composed authorities reproduce.

Expected working branch:

`feature/g9-spatial-semantics-design`

The branch may contain one clean, published author-input commit containing only
the G9P reference assets described in Section 4.

Accept that state as a valid G9P entry state if:

- the branch is clean;
- its upstream is published at the same commit;
- all differences from `main` are limited to the authorized immutable reference
  assets;
- the G8 baseline remains an ancestor;
- no productive source has changed.

If the working tree contains unrelated changes, stop and report them.

Run the planning-appropriate baseline verification required by current repository
authority. Reproduce, at minimum, the focused G8 authority and composed verifier
at the level required by the canonical verification contract.

Do not weaken any verifier.

---

# 3. Fundamental CeDG principles

All G9P designs must preserve:

1. constructive traceability;
2. explicit parameterization;
3. separation between procedure and result;
4. geometric validity;
5. explicit degenerations;
6. consistency between projections;
7. normal dynamic dependency propagation;
8. representation-independent geometry;
9. no confusion between CeDG and conventional CAD.

CeDG spatial semantics must be projection-aware and constructive.

The 3D representation must not become a second independent geometric truth.

A visual 3D view may be derived from the same spatial/projection semantics, but
it must not replace or silently contradict the defining CeDG projections.

---

# 4. Immutable G9P author reference assets

Expected directory:

`docs/references/cedg/models/g9p/`

Expected author-provided files:

1. `geocedg-reference-general-construction-workflow.ggb`
2. `geocedg-reference-locus-cylindrical-graft-development.ggb`
3. `geocedg-reference-locus-truncated-cone-cylinder-connections.ggb`
4. `geocedg-reference-locus-focal-sphere-illumination.ggb`
5. `geocedg-reference-construction-workspace.png`
6. `geocedg-reference-workflows-notes.md`

Also inspect the existing repository copy of `Templatev7.ggb`.

Treat all author-provided assets as immutable reference evidence.

Do not:

- resave the `.ggb` files;
- rewrite embedded XML;
- normalize them through GeoGebra;
- modify the PNG;
- rewrite the author note except for separately proposed documentation corrections.

Create separate metadata/manifests where needed.

For each asset record:

- path;
- SHA-256;
- size;
- provenance;
- purpose;
- relevant workflow;
- expected role in G9P;
- whether it is legacy, reference-current or explanatory evidence.

The three Locus/development models are complementary reference cases and must
be analyzed separately and comparatively. Do not collapse them into a single
generic workflow.

If any expected file is absent, continue with the available evidence but report
the missing input and its impact. Do not fabricate it.

---

# 5. GGB reference-model analysis protocol

Analyze all supplied `.ggb` files as structured GeoGebra archives.

Inspect, where present:

- `geogebra.xml`;
- macro/tool definitions;
- toolbar configuration;
- construction protocol;
- views and layout;
- algebra objects;
- sliders, buttons, checkboxes and input boxes;
- scripts;
- JavaScript;
- GeoGebraScript;
- embedded resources;
- labels and object types;
- commands used;
- dependencies;
- layers and visibility;
- custom tools;
- saved preferences relevant to workflow.

Do not infer semantics only from labels.

Produce, for every reference model:

1. object-family inventory;
2. command inventory;
3. custom-tool inventory;
4. toolbar-group inventory;
5. script inventory;
6. view/layout inventory;
7. dependency and construction-depth summary;
8. workflow sequence;
9. frequently repeated operations;
10. manual workarounds;
11. model-specific tools;
12. reusable generic tools;
13. sampled/legacy Locus workarounds;
14. candidates replaced by G6–G8 native capability;
15. candidates requiring G9 spatial semantics;
16. candidates that should remain experimental or model-local.

Produce a cross-model comparison:

reference models
    -> common operations
    -> common toolbar needs
    -> model-specific operations
    -> current GeoCeDG mapping
    -> missing native capability
    -> proposed future phase

Create a matrix with at least:

| Legacy/reference operation | Current implementation |
| Current native equivalent | Proposed UI placement |
| Kernel/public command need | G9 dependency |
| Keep / replace / defer / reject |

Use the supplied screenshot as visual evidence of expected density, grouping and
workflow—not as a pixel-perfect GUI specification.

---

# 6. G9P workstreams

Organize the design package into coordinated workstreams.

Use stable internal identifiers or repository-consistent equivalents:

- G9P-S — Spatial semantics
- G9P-U — User-facing command/tool/workspace architecture
- G9P-X — Extended DXF export
- G9P-D — Documentation architecture
- G9P-O — Operational bundles and agent workflow

The workstreams must be coordinated, but their future productive implementations
must remain separately gated.

Do not design a monolithic G9 implementation phase.

---

# PART A — G9P-S: SPATIAL SEMANTICS

# 7. Audit the existing spatial and projection architecture

Inspect the actual current GeoGebra and GeoCeDG source for:

- 2D and 3D GeoElement identity;
- construction ownership;
- output dependencies;
- 3D coordinate representations;
- projections;
- views;
- coordinate systems;
- Euclidian settings;
- transformations;
- serialization;
- copy/set semantics;
- update/remove behavior;
- labels versus stable identifiers;
- XML factories;
- algorithms that relate 2D and 3D objects;
- existing 3D incidence/reconstruction support.

Inspect all prior CeDG proof-of-concept material concerning association between
objects and views.

Do not copy legacy structures such as label-based association lists directly into
the final design.

Identify:

- what can be reused;
- what must be extended;
- what belongs in the shared kernel;
- what belongs in GeoCeDG-specific packages;
- what must remain outside the kernel.

---

# 8. Core G9 semantic model

Design the smallest sustainable kernel-semantic model supporting CeDG spatial
objects defined through projections.

At minimum evaluate the need for concepts equivalent to:

- `SpatialObject3D`
- `SpatialObjectId`
- `SpatialRevision`
- `ProjectionFrame`
- `ProjectionFrameId`
- `ProjectionRole`
- `ProjectionBinding`
- `ProjectionBindingId`
- `ProjectionSet`
- `CanonicalProjectionSchema<T>`
- `ProjectionSufficiencyResult`
- `SpatialReconstructionResult`
- `ReprojectionResult`
- `ProjectionConsistencyReport`
- `SpatialDegeneracy`
- `SpatialGuarantee`
- `SpatialLifecycleStatus`

Do not force these exact Java names.

Define mathematical semantics before implementation classes.

For object family \(T\), spatial state \(x\), and selected projection frames
\(\Pi=(\pi_1,\ldots,\pi_k)\), formalize:

\[
\Phi_{T,\Pi}(x)
=
\bigl(\pi_1(x),\ldots,\pi_k(x)\bigr).
\]

Define precisely when a projection set is:

- sufficient;
- underdetermined;
- ambiguous;
- inconsistent;
- degenerate.

Candidate closed states include:

- `VALID`
- `UNDERDETERMINED`
- `AMBIGUOUS`
- `INCONSISTENT`
- `DEGENERATE`
- `UNSUPPORTED`

Determine the final taxonomy from evidence.

---

# 9. Spatial authority and edit direction

G9P must explicitly decide the authority model.

Evaluate at least:

## Projection-defined authority

Defining projections are edited; the spatial object is reconstructed; derived
projections and 3D views are reprojections.

## Spatial-object authority

The spatial object is edited; all projections are derived.

## Hybrid authority

Certain projection bindings are defining while other views are derived, with
explicit transition rules.

Recommend the model most coherent with CeDG.

No implicit bidirectional authority loop is permitted.

The design must prevent:

- oscillatory updates;
- hidden circular dependencies;
- simultaneous contradictory authorities;
- repair by coordinate proximity;
- silent selection of one inconsistent projection.

Every edit must have explicit authority provenance.

---

# 10. Identity, revisions and lifecycle

Define:

- durable spatial identity;
- projection-binding identity;
- frame identity;
- constructive lineage;
- revision-scoped evidence;
- topology revisions;
- stale states;
- invalidation;
- recovery;
- copy/clone behavior;
- deletion behavior;
- undo/redo behavior.

Preserve the distinction:

durable identity
    != label
    != coordinate
    != object index
    != projection order
    != XML position
    != revision

Projection bindings must use stable identifiers, not labels.

If an object is temporarily underdetermined or inconsistent, the system must
preserve truthful lifecycle state and must not fabricate a spatial solution.

---

# 11. Primitive projection schemas

Design the future G9B coverage matrix.

At minimum consider:

- point;
- line;
- segment;
- ray;
- vector;
- plane;
- circle;
- conic;
- supported spatial curve representation.

For every primitive define:

- minimum defining projections;
- allowable frames;
- degeneracies;
- ambiguous configurations;
- reconstruction equations;
- reprojection equations;
- exact/estimated/unsupported guarantees;
- consistency invariants;
- dynamic test cases.

Do not claim one fixed number of projections for all configurations.

For example, distinguish generic from degenerate positions relative to projection
frames.

---

# 12. Composed spatial objects

Design the future G9C boundary.

At minimum consider:

- spatial point collections;
- spatial curves/arcs;
- edges;
- oriented loops;
- faces;
- supporting surfaces;
- ruled surfaces;
- developable surfaces;
- polyhedral objects;
- incidence;
- adjacency;
- orientation;
- boundary ownership;
- connected components.

CeDG is not required to implement a CAD B-Rep clone.

Design only the structures needed for constructive descriptive-geometry models,
projection consistency and downstream procedures.

Explicitly identify what remains outside G9.

---

# 13. Persistence and migration

Audit actual `.ggb` persistence and identify the minimum safe design for:

- spatial IDs;
- frame IDs;
- binding IDs;
- defining/derived roles;
- schema version;
- lifecycle state;
- reopening;
- legacy files without bindings;
- copy/paste;
- undo/redo;
- XML compatibility.

Determine whether G9A should be split into:

- in-memory semantics;
- persistence/migration hardening;

or another evidence-supported subdivision.

Do not authorize persistence implementation during G9P.

---

# PART B — G9P-U: PUBLIC CAPABILITIES AND GUI

# 14. Audit the current GeoCeDG GUI

Inspect actual current behavior rather than assuming it is minimal.

Audit:

- launcher;
- application profile;
- perspective;
- toolbar groups;
- visible tools;
- menus;
- command availability;
- DXF menu/action;
- feature flags;
- experimental laboratory;
- Classic diagnostic path;
- localization;
- help/status text;
- saved preferences;
- workspace switching architecture.

Compare:

1. current GeoCeDG GUI;
2. current Classic GUI;
3. `Templatev7`;
4. author reference models;
5. author screenshot;
6. target workflow described in the note.

Produce a current-state and gap report.

---

# 15. Workspace terminology

Avoid using the ambiguous internal word `mode` where GeoGebra already uses mode
IDs for tools.

Design user-facing **workspaces**, **perspectives** or another repository-consistent
term.

At minimum design:

1. `CeDG Construction` workspace;
2. `CeDG Dihedral Procedures` workspace;
3. preservation of a Classic diagnostic workspace/path.

Final names remain an author decision.

Changing workspace must not change geometric truth or silently reinterpret
existing objects.

---

# 16. CeDG Construction workspace

Design a non-minimal professional CeDG construction workspace.

Requirements:

- preserve current GeoCeDG visual identity and frontend architecture;
- use the current application-profile mechanism as the single source of truth;
- extend the manifest/schema rather than adding duplicate hard-coded toolbar
  strings;
- use `Templatev7` and reference models as workflow evidence;
- replace sampled legacy tools with native capabilities where available;
- keep model-specific macros outside the stable core;
- expose G6–G8 capabilities coherently;
- keep all commands available through algebra input even when not visible as tools.

Design a proposed toolbar grouping.

Evaluate at least the following conceptual groups:

1. selection, inspection and construction;
2. points, lines, segments, rays and vectors;
3. relations and 2D incidence;
4. conics, functions and curves;
5. Locus V2;
6. intersections;
7. metrics, length and validation;
8. transformations and manual projection procedures;
9. CeDG constructions and developments;
10. import/export.

Do not adopt these exact groups without comparing them to actual source,
Templatev7 and the reference models.

Produce:

- toolbar group matrix;
- command/tool mapping;
- feature maturity;
- selection contract;
- help/status text requirements;
- icon strategy;
- localization impact;
- persistence dependency;
- validation plan.

---

# 17. Locus V2 user-facing activation

G6–G8 internal kernel capability does not automatically constitute a user-facing
feature.

Design a separately gated phase for public/internal-app activation.

Audit and decide:

- how a user creates a Locus V2;
- required command syntax or overload;
- command processor;
- dispatcher integration;
- toolbar creation mode;
- argument validation;
- labels;
- undo/redo;
- copy/set behavior;
- deletion;
- persistence;
- reopening;
- help and localization;
- feature flags;
- internal versus public maturity.

Do not invent command names without auditing current GeoGebra naming and overload
conventions.

Evaluate whether to extend existing commands or introduce GeoCeDG-specific
commands for:

- Locus V2 creation;
- total semantic length;
- partial semantic length;
- rich intersection result;
- token-selected intersection point;
- diagnostics.

Produce alternatives and recommendation.

---

# 18. Locus V2 tools required in the Construction workspace

The future workspace must provide visible access to:

- Locus V2 creation;
- Locus V2 length;
- supported partial length where semantically defined;
- intersection between Locus V2 and every G8-supported target family;
- Locus V2 × Locus V2;
- result inspection;
- selection of an admissible solution token;
- creation of the derived dynamic point.

The general intersection tool must accept Locus V2.

Do not create a parallel Locus-specific intersection framework if the general
intersect tool can be extended safely.

Design behavior for:

- empty complete result;
- finite complete result;
- finite result with `NOT_ESTABLISHED` completeness;
- inadmissible locally unisolated roots;
- several admissible roots;
- tangent roots;
- overlap;
- mixed finite + overlap;
- stale or ambiguous continuation;
- work-limit result.

Graphical proximity may help the user choose among already established solutions,
but it must never define durable identity.

After selection, identity must be token-based.

---

# 19. CeDG Dihedral Procedures workspace

Design this workspace now, but keep its implementation blocked until after G9
spatial semantics are author-approved.

Requirements:

- reduced visible 2D tool set;
- all algebra commands remain available;
- procedure-oriented menus/tools;
- direct selection of spatial semantic objects;
- use of `ProjectionBinding` and `ProjectionFrame`;
- explicit constructive steps;
- no opaque CAD-like operation.

Design at least:

- change of projection plane;
- rotation;
- folding/abatimiento;
- true magnitude;
- auxiliary projecting plane;
- section;
- derived projection;
- reconstruction;
- reprojection and consistency inspection.

For a procedure such as plane folding, the user should be able to select the
spatial plane rather than manually selecting every projection.

Conceptual flow:

selected SpatialPlane
    -> resolve defining projection bindings
    -> select/reference hinge and destination frame
    -> construct explicit CeDG procedure
    -> generate auxiliary entities
    -> produce true magnitude / derived projection
    -> preserve dependencies and provenance
    -> issue validity certificate

Determine:

- minimum required G9 types;
- defaults;
- ambiguity dialogs;
- advanced options;
- generated-object ownership;
- construction-protocol visibility;
- undo/redo;
- persistence;
- tests.

This workspace is blocked on G9 global PASS unless evidence justifies a narrower
post-G9A pilot.

---

# PART C — G9P-X: EXTENDED DXF

# 20. Audit the existing DXF implementation

Inspect actual current source and determine:

- DXF version emitted;
- writer architecture;
- DTO/export model;
- menu/action integration;
- units;
- layers;
- styles;
- exact supported entities;
- unsupported entities;
- error handling;
- deterministic behavior;
- tests;
- current treatment of Locus legacy and Locus V2.

Do not rely only on G5 documentation.

Produce an exact source-backed capability matrix.

---

# 21. DXF fidelity model

Design an explicit export-fidelity taxonomy:

- `EXACT`
- `APPROXIMATE`
- `UNSUPPORTED`
- `INVALID`

or repository-equivalent names.

For every GeoCeDG-supported 2D curve family determine:

- DXF native entity;
- exactness;
- approximation requirement;
- domain requirement;
- component handling;
- discontinuity handling;
- error contract;
- warnings.

Include:

- line;
- segment;
- ray;
- circle;
- arc;
- ellipse;
- parabola;
- hyperbola;
- functions;
- regular polynomial implicit curves;
- Locus V2;
- multi-branch Locus V2;
- multi-component Locus V2;
- periodic curves;
- unbounded curves;
- upstream parametric curves where applicable.

---

# 22. Exact versus approximate DXF

Preserve the GeoCeDG model as geometric authority.

When DXF cannot encode the source curve exactly, design an ephemeral
export-only approximation:

authoritative curve
    -> semantic evaluator
    -> export approximation builder
    -> read-only export DTO
    -> DXF entity

The approximation must never:

- become a `GeoElement`;
- enter `Construction`;
- appear in the construction protocol;
- be persisted in `.ggb`;
- participate in dependencies;
- use render tessellation;
- depend on viewport, zoom or DPI.

Temporary means temporary within the export model only.

---

# 23. Approximation strategies

Audit the actual DXF target and compare:

- adaptive polyline;
- `LWPOLYLINE`;
- `POLYLINE`;
- `SPLINE`;
- rational/NURBS conic representation where supported;
- exact conic entities where supported.

Do not assume `SPLINE` is exact.

For each strategy define:

- model-coordinate error;
- chordal error;
- tangent/curvature criteria;
- maximum segment/control-point count;
- discontinuity separation;
- closed/periodic behavior;
- deterministic subdivision;
- work limits;
- failure state.

Recommend a baseline.

Adaptive polyline may be the conservative certified baseline unless evidence
supports a stronger interoperable spline contract.

---

# 24. Locus V2 DXF design

For Locus V2:

- export each valid branch/component independently;
- never connect invalid-domain gaps;
- preserve periodic closure only when established;
- retain constructive multiplicity where meaningful;
- use semantic domains;
- reject unsupported unbounded export unless an explicit geometric export domain
  is supplied;
- do not use render cache;
- report approximation status.

Design a sidecar manifest such as:

`<drawing>.dxf.manifest.json`

containing:

- repository commit;
- source Geo ID;
- source family;
- source revision;
- branch/component;
- DXF entity;
- fidelity;
- method;
- requested tolerance;
- achieved or estimated error;
- segment/control-point count;
- source parameter interval;
- warnings;
- omitted components;
- work-limit outcomes.

The DXF UI must clearly warn when approximate entities exist.

---

# PART D — G9P-D: DOCUMENTATION

# 25. Audit current documentation

Inventory and classify:

- user guides;
- developer guides;
- mathematical references;
- architecture;
- API docs;
- prompt guides;
- operational guides;
- install/build guides;
- roadmap;
- specs;
- ADRs.

Identify:

- duplicates;
- gaps;
- stale statements;
- user-observable capabilities not documented;
- internal-only capabilities presented as public;
- future capabilities presented as available;
- mathematical content duplicated inconsistently.

---

# 26. Main user guide

Maintain the current GeoCeDG user guide as the primary entry point.

It must remain useful for:

- installation;
- execution;
- workspaces;
- GUI;
- commands/tools;
- Locus V2;
- metrics and intersections;
- DXF;
- saving/reopening;
- feature maturity;
- limitations;
- examples;
- architecture overview;
- mathematical references.

During G9P, update it only for current implemented state and current design status.

Do not present unimplemented G9 capabilities as available.

Add or formalize a documentation-impact contract:

- update guide;
- or record `GUIDE_IMPACT = NONE` with justification.

---

# 27. Mathematical reference

Determine whether detailed mathematics remains sustainable inside the main user
guide.

If not, create or specify a linked mathematical reference.

It should explain, at minimum:

- CeDG constructive model;
- Locus V2 semantic branches/components;
- length as total variation;
- intersections:

\[
G(F(t))=0
\]

and:

\[
F(t)=Q(u);
\]

- completeness;
- individual admissibility;
- semantic identity;
- exact versus approximate curves;
- projection frames;
- projection sufficiency;
- spatial reconstruction;
- ambiguity;
- degeneracy;
- consistency.

Normative mathematics remains in specs.

The user mathematical reference explains and illustrates it without silently
changing it.

---

# 28. Developer guide

If no equivalent complete guide exists, create:

`docs/developer/geocedg_developer_guide.md`

If an equivalent exists, extend it instead of duplicating it.

Produce a usable current-state first edition covering:

- project purpose and CeDG/non-CAD boundary;
- repository layout;
- modules;
- GeoGebra baseline;
- ownership classes;
- toolchain;
- build;
- launch;
- packaging;
- tests;
- canonical verifiers;
- kernel extension process;
- command lifecycle;
- algorithms and GeoElements;
- frontend/profile manifest;
- persistence;
- DXF;
- modified-file governance;
- scientific references;
- branches, commits and tags;
- closeout workflow;
- future G9 architecture.

Future G9 material must be marked as design/proposed.

---

# 29. Agent prompt usage guide

If no equivalent guide exists, create:

`docs/developer/geocedg_agent_prompt_guide.md`

If an equivalent exists, extend it.

Produce a usable guide covering:

- authority hierarchy;
- canonical versus launch prompts;
- new-thread startup;
- prompt discovery;
- canonical LF hashing;
- branch selection;
- entry gates;
- characterization phases;
- implementation phases;
- author review;
- closeout;
- fast-forward promotion;
- tags;
- next-phase branches;
- no-commit review candidates;
- handling environmental sandbox restrictions;
- evidence/logging;
- stop conditions;
- avoiding duplicated contracts;
- use of knowledge bundles.

Include concise launch examples.

Do not describe nonexistent tools as available.

---

# 30. Documentation traceability

Create a matrix:

capability
    -> specification
    -> ADR
    -> architecture
    -> API/developer guide
    -> user guide
    -> canonical prompt
    -> verifier
    -> evidence

Recommend automated checks where sustainable.

---

# PART E — G9P-O: KNOWLEDGE BUNDLES

# 31. Knowledge-bundle objective

Design a deterministic operational capability that generates bundles containing
GeoCeDG-owned or GeoCeDG-modified source and the context required to use those
sources as project knowledge.

The generator belongs outside the geometric kernel.

Do not implement the productive generator during G9P.

Produce the complete specification and canonical implementation prompt.

---

# 32. Ownership classification

Design source classification using:

1. GeoCeDG-native paths/namespaces;
2. modified-file inventory;
3. Git comparison with the GeoGebra baseline;
4. file history;
5. explicit exclusions.

Candidate classes:

- `GEOCEDG_NATIVE`
- `UPSTREAM_MODIFIED`
- `UPSTREAM_UNCHANGED_REFERENCE`
- `GENERATED`
- `THIRD_PARTY_OR_RESTRICTED`

Default source bundles include the first two.

Do not rely solely on folder names.

---

# 33. Bundle profiles

Design at least:

## Source bundle

Contains:

- GeoCeDG-native source;
- upstream-modified source;
- related focused tests;
- related feature/profile files;
- relevant verification wrappers;
- source provenance and patches.

## Knowledge bundle

Contains:

- `AGENTS.md`;
- roadmap;
- specs;
- ADRs;
- architecture;
- API docs;
- user/developer/prompt guides;
- canonical prompts;
- current validation summaries;
- scientific catalog/index;
- source maps;
- reading order.

Consider thematic bundles:

- governance;
- frontend and DXF;
- Locus V2;
- spatial G9;
- operational layer.

Do not create one uncontrolled monolithic file.

---

# 34. Bundle manifest

Design a versioned machine-readable manifest.

At minimum include:

- schema version;
- generator version;
- repository;
- branch;
- commit;
- dirty state;
- baseline version/commit;
- generation configuration;
- deterministic bundle ID;
- source path;
- ownership class;
- language;
- encoding;
- line range;
- current SHA-256;
- baseline blob SHA;
- change type;
- related spec;
- related ADR;
- related phase;
- related tests;
- license/provenance;
- ordering metadata.

For upstream-modified files, design inclusion of:

- current complete file;
- baseline identity;
- concise change summary;
- optional unified diff.

---

# 35. Determinism and freshness

Require:

- deterministic ordering;
- UTF-8;
- LF canonical form;
- stable archives;
- no variable timestamp in deterministic content;
- same commit/configuration -> same hashes;
- clean tree by default;
- explicit dirty-tree mode with diff and warning;
- freshness check against current `HEAD`;
- rejection of stale claimed bundles;
- size/file/token budgets;
- boundary-preserving chunking;
- no split inside a class unless unavoidable;
- continuation identifiers where chunks are necessary.

Design a verifier:

`tools/agent/verify-knowledge-bundles.ps1`

and a generator location such as:

`tools/knowledge/`

The final paths must follow actual repository conventions.

---

# 36. Bundle exclusions

Exclude by default:

- `.git`;
- build outputs;
- `.gradle`;
- installers;
- caches;
- logs;
- temporary files;
- secrets;
- local settings;
- absolute user paths;
- third-party source not required;
- restricted assets;
- large PDFs already separately cataloged.

Scientific PDFs should normally be referenced by catalog metadata and hashes,
not duplicated into every bundle.

---

# 37. Operational implementation phase

Design a separately gated future phase, provisionally:

`G9O1 — GeoCeDG deterministic source and knowledge bundles`

This phase should also harden the developer and agent-operation documentation
where required.

Do not implement G9O1 now.

---

# 38. Roadmap sequencing

G9P must recommend the exact post-G9P sequence.

Evaluate at least these candidate orders.

## Sequence A

G9P
 -> G9O1
 -> G9U0
 -> G9X1
 -> G9U1
 -> G9A
 -> G9B
 -> G9C
 -> G9 global closeout
 -> G9U2

## Sequence B

G9P
 -> G9O1
 -> G9A
 -> G9U0
 -> G9X1
 -> G9U1
 -> G9B
 -> G9C
 -> G9 global closeout
 -> G9U2

The deciding issue is whether public/persistent Locus V2 objects and tools must be
coordinated with G9 identity/persistence foundations.

Inspect actual code and recommend the sustainable order.

Parallelization may be proposed only where file boundaries, semantic authority and
verification gates remain independent.

---

# 39. Future phase definitions

At minimum design future phases equivalent to:

- G9O1 — source/knowledge bundles and operational guides
- G9U0 — Locus V2 user-facing command/tool/persistence surface
- G9X1 — extended exact/approximate DXF curve export
- G9U1 — CeDG Construction workspace
- G9A — spatial identity and projection bindings
- G9B — canonical primitive projection schemas
- G9C — composed spatial/projective objects
- G9U2 — post-G9 Dihedral Procedures workspace

Determine whether G9A must be subdivided.

Do not assume the provisional identifiers are final.

Update the roadmap with final proposed phase identifiers, dependencies, entry gates,
exit gates and author-review gates.

All implementation phases remain unauthorized.

---

# 40. Canonical future prompts

Create canonical future prompts for every approved proposed phase.

Each prompt must:

- follow repository prompt conventions;
- reference specs/ADRs rather than duplicate them unnecessarily;
- define branch expectation;
- define entry gates;
- define exact scope;
- define explicit exclusions;
- define editable boundaries;
- define productive versus test-private permissions;
- define validation;
- define evidence;
- define full verification;
- define stop conditions;
- prohibit automatic self-approval;
- remain unexecuted.

At minimum produce prompts corresponding to:

1. operational bundles/guides;
2. Locus V2 public surface;
3. extended DXF;
4. Construction workspace;
5. G9A;
6. G9B;
7. G9C;
8. post-G9 Dihedral Procedures workspace.

If G9A is subdivided, produce separate prompts.

Report exact canonical paths and canonical LF SHA-256 values.

Also version the current G9P task prompt under the repository canonical task-prompt
convention if repository practice requires executed task prompts to be retained.

---

# 41. Proposed specifications and ADRs

Produce proposed specifications for:

- G9 spatial semantics;
- projection frames/bindings;
- projection sufficiency and reconstruction;
- public Locus V2 command/tool surface;
- GUI workspaces;
- DXF fidelity and approximation;
- knowledge bundles;
- documentation maintenance.

Avoid over-fragmentation.

Determine which matters require ADRs.

Potential architectural decisions include:

- spatial authority/edit direction;
- binding identity and lifecycle;
- persistence split;
- workspace manifest architecture;
- public Locus V2 surface;
- DXF approximation boundary;
- bundle ownership/determinism.

All new specs and ADRs remain:

`PROPOSED / NOT NORMATIVE`

and:

`Proposed`

until explicit author review.

---

# 42. Characterization probes

Narrow test-private probes are authorized only to answer design questions that
source inspection cannot settle.

Examples:

- current toolbar/profile behavior;
- command/dispatcher overload resolution;
- GGB load/persistence behavior;
- DXF entity writer support;
- SPLINE interoperability;
- projection identity/lifecycle behavior;
- XML ID possibilities;
- deterministic bundle chunking prototype;
- model workflow extraction.

No productive source implementation.

Every probe must be:

- deterministic;
- versioned;
- reproducible;
- explicitly marked characterization-only;
- included in a focused G9P verifier.

---

# 43. Required design artifacts

Produce a reviewable G9P package containing the functional equivalent of:

1. integrated G9P plan;
2. G9 spatial semantic model;
3. G9 architecture;
4. projection-sufficiency model;
5. primitive capability matrix;
6. composed-object boundary;
7. persistence/migration analysis;
8. current GUI audit;
9. reference GGB workflow report;
10. legacy/reference-tool migration matrix;
11. Construction workspace design;
12. Dihedral Procedures workspace design;
13. Locus V2 public-surface design;
14. extended DXF specification;
15. DXF capability/fidelity matrix;
16. documentation architecture;
17. updated user guide;
18. developer guide;
19. agent prompt guide;
20. knowledge-bundle specification;
21. bundle schema/config design;
22. upstream impact maps;
23. validation matrices;
24. functional-counter/benchmark plans;
25. scientific traceability;
26. roadmap update;
27. proposed ADRs;
28. canonical future prompts;
29. G9P integrity manifest;
30. focused G9P verifier.

Do not create fake content merely to satisfy this list.
If existing artifacts already fulfill an item, extend/reference them instead of
duplicating them.

---

# 44. Author decision package

Finish with an explicit author-decision table.

At minimum include decisions on:

## Spatial semantics

- final G9A/G9B/G9C subdivision;
- spatial authority/edit direction;
- durable IDs;
- projection-binding lifecycle;
- defining versus derived projections;
- revision model;
- persistence split;
- migration;
- primitive sufficiency;
- ambiguity/degeneration states;
- composed-object boundary.

## GUI/public surface

- final workspace names;
- Construction workspace groups;
- public Locus V2 command strategy;
- `Length` overload versus dedicated command;
- `Intersect` integration;
- result-selection UX;
- persistence requirement;
- Classic diagnostic boundary;
- post-G9 workspace gate.

## DXF

- exact/approximate taxonomy;
- supported exact entities;
- approximation baseline;
- SPLINE versus polyline;
- error contract;
- sidecar manifest;
- unbounded-domain policy.

## Documentation

- guide structure;
- mathematical-reference split;
- documentation-impact gate;
- ownership of user/developer/prompt guides.

## Bundles

- profiles;
- ownership classes;
- manifest;
- deterministic format;
- dirty-tree policy;
- thematic bundle boundaries;
- implementation order.

## Roadmap

- final phase sequence;
- dependencies;
- which phase executes first;
- G9U2 gate.

For every decision include:

- evidence;
- alternatives;
- recommendation;
- impact if rejected;
- required gate.

Do not silently make final normative author decisions.

---

# 45. Verification

Run all planning/design verification required by current repository authority.

At minimum, where applicable:

- operational verifier;
- focused G8 authority;
- G9P focused verifier;
- prompt/static/schema validation;
- Markdown link validation;
- manifest/hash validation;
- GGB input hash validation;
- composed verifier;
- `git diff --check`;
- `git diff --cached --check`.

The final composed verifier must run without `-SkipBuild` unless current authority
explicitly permits otherwise.

Confirm:

- no productive G9 source;
- no productive GUI changes;
- no productive DXF changes;
- no productive bundle generator;
- no public command implementation;
- no new persistence implementation;
- no G9 tests presented as productive capability;
- no generated outputs tracked.

Do not weaken verification.

---

# 46. Stop conditions

Stop for author review rather than forcing a design if:

- spatial authority cannot be made acyclic;
- projection identity would depend on labels;
- bindings cannot survive copy/save/reopen coherently;
- public Locus V2 tools would create nonpersistent stable-looking objects;
- the general Intersect tool cannot safely preserve token identity;
- DXF approximation would depend on render tessellation;
- DXF fidelity cannot be reported honestly;
- non-basic curve export would require hidden construction GeoElements;
- workspace design requires duplicated hard-coded toolbar authorities;
- procedural tools would become opaque CAD operations;
- bundle ownership cannot be derived reproducibly;
- bundle generation would include restricted third-party material by default;
- guides would duplicate normative specs inconsistently;
- G9 implementation would be required merely to finish G9P.

Prefer narrower explicit contracts.

---

# 47. Final disposition

Do not self-authorize implementation.

If G9P completes successfully, finish with the functional equivalent of:

G8 =
PASS — AUTHOR APPROVED

G9P =
DESIGN AND CHARACTERIZATION COMPLETE
AWAITING AUTHOR REVIEW

G9 SPATIAL SPEC =
PROPOSED / NOT NORMATIVE

G9 ADRs =
PROPOSED

G9A / G9B / G9C =
DESIGNED
NOT AUTHORIZED

G9O1 =
DESIGNED
NOT AUTHORIZED

G9U0 =
DESIGNED
NOT AUTHORIZED

G9X1 =
DESIGNED
NOT AUTHORIZED

G9U1 =
DESIGNED
NOT AUTHORIZED

G9U2 =
DESIGNED
BLOCKED ON THE APPROVED G9 GATE

G9 PRODUCTIVE IMPLEMENTATION =
NOT STARTED

Return:

- repository state;
- reference-input audit;
- recommended roadmap;
- spatial architecture;
- workspace architecture;
- public Locus V2 surface;
- DXF architecture;
- documentation architecture;
- bundle architecture;
- created/modified artifacts;
- characterization evidence;
- exact verification results;
- canonical prompt paths and hashes;
- full author-decision package;
- blockers.

Do not commit, push, promote, tag or execute any future canonical prompt.
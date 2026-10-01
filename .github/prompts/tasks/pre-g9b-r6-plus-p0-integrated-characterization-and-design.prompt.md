# PRE-G9B-R6-plus-P0 — integrated characterization and normative design

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR CHARACTERIZATION AND DESIGN
EXECUTION ONLY.**

The author's explicit instruction of 2026-10-01 names `PRE-G9B-R6-plus-P0`, its
exact implementation base (the published `PRE-G9B-R6-plus` planning closeout
`P_R6PLUS_PLAN`) and the planning approval it depends on. That instruction also
authorizes this amendment of the canonical prompt, before any other tracked
edit, so that it becomes the executable contract of the phase. The amendment
replaces the proposed future prompt created by the planning task (blob
`2c51ca73e6a1ccbd04d41885f8d4f86b69dd1578`), whose base fields were unresolved.
It keeps every author-fixed scope requirement, every author-fixed unit semantic
and every forbidden-scope rule of that prompt. This file is an execution
contract, not a second policy document.

```text
PRE-G9B-R6-plus-P0 =
AUTHORIZED FOR CHARACTERIZATION AND DESIGN EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true
passClaimed              = false
PHASE_KIND               = CHARACTERIZATION AND DESIGN CANDIDATES — NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-R6-plus PLANNING = PASS — AUTHOR APPROVED — PUBLISHED
DEPENDS_ON_PHASE         = PRE-G9B-R6 = PASS — AUTHOR APPROVED — PUBLISHED
```

`implementationAuthorized = true` authorizes only the characterization and
design execution defined below. It authorizes no product implementation and no
later `PRE-G9B-R6-plus` subphase. `authorApproved = false` means that the design
candidate this phase produces has not been author-approved. Technical
verification never creates author approval. The phase stops with one exact,
immutable design candidate pending author review.

`P0` is the first executable task of the mini-track. "Normative design" in its
title means design **candidates** for normative contracts. `P0` accepts none of
them, implements nothing, and its own technical result never becomes author
approval.

<!-- geocedg-field: objective -->
## Objective

Characterize, exhaustively and from source, every capability the author placed
in `PRE-G9B-R6-plus`. Then turn that evidence into design candidates, an
architecture-owner decision and a persistence decision for every new state, so
that every later subphase can be authorized against an exact contract instead
of an assumption.

The mini-track and its decomposition are recorded in the
[mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md)
and its evidence in the
[planning candidate report](../../../docs/validation/pre_g9b_r6_plus_planning_candidate_report.md).
Two parts of the plan are **author-fixed** and `P0` may not correct them: the
scope (plan §2, summarized below) and the unit semantics (plan §4). Everything
else in the plan is **planning input**: pre-characterization gathered at
`P_R6` to make the decomposition credible. `P0` must re-establish every such
finding it relies on from source at its own base, and must report a correction
rather than inherit a wrong statement.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = DOCUMENTATION_STATUS_ONLY
frozenAtPhaseStart   = true
PRODUCT_PHASE_EFFECT = NONE
PLANNED_ACCEPTANCE   = STATIC on the exact frozen P0 candidate;
                       no PHASE, no INTEGRATION, no FINAL
```

`DOCUMENTATION_STATUS_ONLY` is correct only while `P0` touches
characterization, documentation, design and planning paths. If the work would
need a change to `source/**`, `apps/**`, `packaging/**`, any product resource,
`geocedg/specs/operations/verification-*`, `prompt-contracts.json` or any
`tools/agent/**` script, or would otherwise change executable, product,
serialization, verification-infrastructure or build behavior, stop and report a
`VERIFICATION_ESCALATION_REQUEST` instead of widening scope.

### Author-fixed scope (plan §2)

1. **Layers.** Explicit working layer, changeable at any time; new objects use
   it; working layer in the status bar; Algebra View `Sort by Layer` kept;
   layers hidden and shown from the Algebra View without overwriting object
   visibility, so that `effectiveVisible(object) = objectVisible(object) AND
   NOT layerHidden(object.layer)`. Author targets:
   `AUTHOR TARGET = working layer not restricted by the current 0–9 UX/domain`
   and `working-layer toolbar entry = GeoCeDG mode compatible with the Move
   group`. The current upstream domain is 0..9 with several dependent
   consumers, so design the least invasive correct extension, determine its
   real compatibility and verification impact, and confirm the exact mode
   design. Do not presume the widening harmless.
2. **Status bar.** An extensible GeoCeDG status bar showing at least working
   layer, construction unit and presentation unit. It is presentation state,
   never geometric authority. It is complementary, may gain interaction if
   recommended, and does not replace the Move-group tool.
3. **Export surface.** Expose Graphics View as Picture (PNG, PDF, SVG,
   EMF/EMF+), DXF, PSTricks, PGF/TikZ and Asymptote. Do not expose STL,
   Collada, HTML Collada or Dynamic Worksheet as Web Page. File actions to
   define and display the export area. Author-supplied requirement for PNG,
   PDF, SVG and EMF(+) with an `Export_1`/`Export_2` area: the export must be
   exactly the rectangle the points define, without the small outside border
   currently observed, and must be the complete rectangle even where it lies
   outside the visible viewport, where current export may clip to visible
   content.
4. **`ExportArea`.** One authority with candidate producers `Export_1`/
   `Export_2`, a manual rectangle, an ISO A selection and `IsoABorder`; an
   explicit area is exported completely even outside the viewport; the visible
   view is the fallback only without an explicit area. Author preference:
   session lifetime.
5. **Semantic export.** `LocusV2` and `SplineV2` in the applicable LaTeX and
   vector exporters, from semantic geometry, never from render tessellation.
6. **Units.** `constructionUnit`, `presentationUnit` and physical export as
   fixed in plan §4, including `AQ-U1 = RESOLVED — AUTHOR CONFIRMED`:
   `presentationUnit` expresses and converts dimensional and export
   quantities, `drawingScale` is a dimensionless ratio, and physical output
   size is invariant under an equivalent presentation-unit change (10 cm at
   1:2 = 5 cm = 50 mm on output; 100 mm at 1:2 = 50 mm). `presentationUnit` is
   never a second geometric scale factor.
7. **GGT library.** Curated library with provenance, installer integration and
   GeoCeDG-owned, reproducibly maintained icons; the author intends to ship the
   approved library, so packaging support is a required design target; style
   memory if sustainable, otherwise black at 3 pt. The repository structure
   (requested `tools/ggtfiles/` versus the `models/legacy/` provenance
   conventions) is open design work (`AQ-G1`): design a provenance-safe and
   packaging-safe solution, never chosen by convenience. Author-supplied
   requirement for `sheetISOland`/`sheetISOvert`, applied only in their future
   authorized phase: hide the outer dotted rectangle and hide all corner
   points.
8. **Native dimensions.** `DirectDimension` and `AxisDimension` (placement,
   help, unit conversion, traceable and movable placement, no far-away
   placement, centred figure, deterministic above/left side, parallel text
   where feasible, no hidden unit multiplier).
9. **`IsoABorder`.** Native, menu `Construction → Annotations and Media`, no
   toolbar button; inputs upper-left point, ISO A size, orientation, drawing
   scale; may configure the common `ExportArea` after a yes/no question.
10. **Orientation display.** Optional discreet cue (preferably a hollow arrow)
    of the kernel direction of a line, segment or ray; never from the viewport.
    At least one accessor exposes the opposite direction for lines without an
    end point, so establish the semantic orientation authority for each
    supported object type before `F1`; never choose an accessor by its name.
11. **Authoring refinements.** `Angle with Given Size` creates its angle hidden;
    independent remembered references for the parallel and perpendicular tools
    (point first reuses; reference first then point replaces and remembers;
    first use asks).
12. **Help.** A final integrated help phase.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
babf20c7c03d0454e2da6760bf5d1fc3fa546f6b          (P_R6PLUS_PLAN, published planning closeout)

IMPLEMENTATION_BASE_TREE =
3f7abded9163324a3142ad7446fb4e6c85407809

BASE_IDENTITY =
P_R6PLUS_PLAN

PRE-G9B-R6-plus PLANNING =
PASS — AUTHOR APPROVED — PUBLISHED
```

Both fields are frozen by the authorizing instruction of 2026-10-01. A moving
branch is not a base, and the phase is not rebased onto any later commit
without a new author instruction.

Planning provenance, carried by the linear published history
`P_R6 → T_R6PLUS_PLAN → D_R6PLUS_PLAN → P_R6PLUS_PLAN`:

```text
T_R6PLUS_PLAN = 147dac8d838df9ee62c0ae9c4fa296b28b9b9b8f   (planning candidate)
D_R6PLUS_PLAN = af2aa1133620e49d68ac469ee8d10d273498cd95   (documentary reconciliation)
P_R6PLUS_PLAN = babf20c7c03d0454e2da6760bf5d1fc3fa546f6b   (planning closeout)
```

Published provenance from which `PRE-G9B-R6-plus` planning began, recorded as
provenance only and **not** as this phase's implementation identity:

```text
P_R6       = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
P_R6_TREE  = f5c8c9f0cc76982170c6282a1242d2a5d07a0897
PRE-G9B-R6 = PASS — AUTHOR APPROVED — PUBLISHED
```

At entry, after a fresh fetch, verify that local `main`, `origin/main` and the
live remote `main` equal `IMPLEMENTATION_BASE`, that its tree equals
`IMPLEMENTATION_BASE_TREE`, that the tracked worktree is clean, that no `P0`
implementation already exists and that no remote change is unexpected. If any
identity differs, stop. The preferred local branch is
`phase/pre-g9b-r6-plus-p0-characterization-design`; it is never pushed.

## Authority and evidence hierarchy

1. Current code, tests, build configuration and serialization at the frozen
   base.
2. `AGENTS.md`, the canonical
   [governance](../canonical/governance.prompt.md) and
   [verification](../canonical/verification.prompt.md) prompts.
3. The accepted specifications and ADRs, and the canonical models and approved
   baselines under `models/` and `geocedg/validation/` (`AGENTS.md` §2). In
   particular:
   [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md),
   [DXF curve fidelity](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md),
   [application profile](../../../geocedg/specs/ui/application-profile.md),
   [CeDG workspaces](../../../geocedg/specs/ui/cedg-workspaces.md),
   [native document identity](../../../geocedg/specs/ui/native-document-identity.md),
   [controlled legacy integration](../../../geocedg/specs/legacy/controlled-integration.md),
   [Windows packaging](../../../geocedg/specs/packaging/windows-packaging.md),
   [ADR 0031](../../../docs/adr/0031-canonical-english-command-surface-compatibility.md),
   the `PRE-G9B-R6` GGBScript capability matrix
   (`geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`) with its
   completeness gate, and the G3 legacy package `models/legacy/template-v7/`.
4. The live verification and documentation-maintenance contracts:
   `geocedg/specs/operations/verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1` and
   `geocedg/specs/operations/documentation-maintenance.md`.
5. The living [roadmap](../../../docs/roadmap/geocedg_roadmap.md) and published
   phase closeouts, including the
   [`PRE-G9B-R6` closeout](../../../docs/validation/pre_g9b_r6_closeout_record.md).
6. The pinned upstream GeoGebra source recorded in
   `docs/upstream/BASELINE_COMMIT.txt`.
7. Local author-provided `PRE-G9B-R6-plus` material (next section).
8. The mini-track plan outside §2 and §4, the planning candidate report,
   previous agent reports and conversation summaries.

This order is the author's instruction for this track. The author-fixed scope
and unit semantics bind `P0` as scope. Technical statements in any report,
including the local author report and the plan, are checked against source
before use.

## Local author input

```text
LOCAL_AUTHOR_INPUT = artifacts/author-input/g9b-r6-plus/
```

The directory is ignored by Git under `/artifacts/*`. It is author-provided
planning and reference material: never commit it, move it, rename it, rewrite
it or copy it into a product path.

`P0` must:

- inventory every file with its name, size, role and SHA-256, and compare the
  result with the planning-time inventory in the planning candidate report §3.
  Report every added, removed or changed file explicitly;
- inspect `ReportExport.md` and classify each technical claim as
  `CONFIRMED`, `CORRECTED`, `NOT REPRODUCED` or `NOT DETERMINABLE FROM SOURCE`,
  citing the governing source line;
- inspect every supplied `.ggt` non-destructively, by copying it into the
  session scratchpad and unpacking the copy there. Record its archive entries,
  macro definitions, ordered inputs and outputs, construction commands, fixed
  styles, hard-coded constants and icon presence, and confirm or correct the
  planning finding that each macro is construction-equivalent to the
  corresponding macro in `models/legacy/template-v7/`;
- distinguish **author intent** (what the author wants a later phase to build)
  from **proven current behavior** (what the file or product does today);
- treat each `.ggt` as a behavioral and provenance reference, not as an approved
  product asset;
- at the end of the phase, verify that every file is unchanged from the entry
  inventory. If a difference appears, distinguish an author-side change made
  during the session from an agent-side mutation; agent-side mutation is
  forbidden.

Two author requirements are **already supplied** and are scope, not missing
input (author-fixed scope items 3 and 7; plan §2):

- **Export clipping and padding (`AQ-X1`).** For PNG, PDF, SVG and EMF(+) with
  an `Export_1`/`Export_2` area, the current export appears to include a small
  border outside the exact rectangle, and may clip to visible content when part
  of the rectangle lies outside the viewport. The required behavior is exactly
  the defined rectangle, completely, independently of viewport visibility.
  Reproduce and characterize both observations against source and runtime.
- **Sheet tools (`AQ-G3`).** In their future authorized phase, `sheetISOland`
  and `sheetISOvert` hide the outer dotted rectangle and all corner points.
  Characterize what that means for each macro's outputs; do not modify them.

Do not ask the author to supply these facts again.

**Provenance and licensing gate.** Geometric equivalence of the supplied macros
with `models/legacy/template-v7/` does not imply redistribution permission, and
no agent makes a legal conclusion. Keep distinct: the macro behavior used as a
reference, the macro source and its provenance, GeoCeDG-owned replacement
icons, and a redistributable packaged asset.

If an expected author-input file is missing, report that specific missing input
and continue with every part of `P0` that repository or upstream evidence can
establish. Never invent the behavior of a missing tool.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

`P0` may perform only:

- exhaustive characterization of current source, tests, build, serialization,
  profile and packaging, and of the pinned upstream source;
- inspection of the local author input as defined above;
- scratch probes, including headless or windowed runtime probes, executed from
  the session scratchpad and never written into tracked source paths or
  `artifacts/`. A probe result is characterization evidence, not acceptance
  evidence;
- read-only `.ggt` unpacking into the scratchpad;
- design and specification **candidates**, each marked proposed and not
  normative until the author accepts it. Expected locations are
  `docs/architecture/`, `docs/adr/` (status `PROPOSED`) and, for contract
  candidates, `geocedg/specs/` with an explicit candidate status;
- architecture-owner and persistence decisions, as candidates;
- the verification plan for every later subphase, as a recommendation;
- roadmap reconciliation limited to status (`CHARACTERIZATION / DESIGN IN
  PROGRESS`, then `TECHNICAL DESIGN CANDIDATE PENDING AUTHOR REVIEW`) and to
  corrections the evidence forces, without marking anything authorized,
  approved or `PASS`;
- canonical recommendations for each later subphase prompt, recorded in the
  report only. The authorizing instruction permits editing exactly one prompt
  file, this canonical `P0` prompt, and only to make it executable and freeze
  its exact base. It does not authorize creating or editing the canonical
  prompt of `A`, `B`, `D0`, `D1`, `C`, `E1`, `E2`, `E3`, `F1`, `F2` or `G`;
  those are generated only after author review of the `P0` design candidate.

```text
PRODUCT_PHASE_EFFECT = NONE
```

### Probes and checks assigned to `P0`

The plan leaves these open for `P0` to settle by probe or exact reading. Each
must end with a recorded result:

1. whether `Ctrl+Shift+U`, `Ctrl+Shift+C`, `Ctrl+Shift+W` (worksheet upload),
   `Ctrl+Shift+M` and `Ctrl+Shift+B` fire in GeoCeDG, and whether
   `Ctrl+Shift+D` both opens the DXF dialog and toggles "selection allowed";
   also the command-line `--export` options, the scripting export API and what
   a v1-profile fallback exposes (plan §6.1);
2. the exported files for an `Export_1`/`Export_2` area that fits the viewport
   and for one larger than or outside it, per picture format (PNG, PDF, SVG and
   EMF/EMF+ where executable), reproducing the author-reported outside border
   and viewport clipping, and
   whether a leftover selection rectangle crops a later export (plan §2,
   §6.2, §6.3);
3. overriding `getMaxLayerUsed` for the working layer: the cap at 8 on the
   defaults path, the uncapped macro path, and objects rebuilt during load and
   undo (plan §5.2);
4. whether the user-tool library rejects the sheet tools' empty
   `ggbscript onUpdate` (plan §8.2);
5. rotated or line-parallel text support for dimension figures (plan §8.5);
6. remembered-reference lifecycle across undo, redo, deletion, redefinition,
   New and Open, and the gesture conflict with point-on-path creation
   (plan §9.2);
7. how the SVG writer groups output by layer, as part of the layer-domain
   finding (plan §5.1).

The pre-existing observations recorded in the planning candidate report §7
(hidden or excluded export shortcut routes, the `Ctrl+Shift+D` conflict, the DXF
documentation and state mismatches, the Locus V2 save warning that is never
invoked, and the stale action count) stay observations. `P0` may classify them
and recommend an owner for each. It must not turn any of them into product
scope or fix one silently.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

`P0` must not perform:

- any product implementation or source behavior change, including under
  `source/**`, `apps/**`, `packaging/**` and product resources;
- any serialization or document-format change;
- copying a `.ggt` into a product, model or tool path, including
  `models/legacy/` and the planned GGT library location;
- generating final product icons;
- any menu, toolbar, profile or feature-manifest change;
- any exporter fix;
- any new tool or command;
- any new unit metadata or unit behavior;
- any layer change;
- creating or editing any prompt file other than this canonical `P0` prompt,
  including the canonical prompts of the later subphases;
- publication of any kind.

It must also not: edit `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md` or
`ai-shell/prompts/**`; edit `geocedg/specs/operations/verification-*`,
`prompt-contracts.json` or `tools/agent/**`; register a verification phase or
a schema; mark any gate, phase, specification or ADR approved, `PASS` or
authorized; correct the author-fixed scope or unit semantics; or start any later
`PRE-G9B-R6-plus` subphase, `PRE-G9B-R7` or `G9B`.

## Architectural placement

`P0` itself is characterization, design and planning. It produces no product
capability and no geometric semantics.

For **every** planned feature, `P0` must state explicitly whether it belongs in
the shared Java kernel or outside it, by the rule of `AGENTS.md` §4, which
governs this summary. In brief: the kernel only when correctness needs semantic
geometric meaning, persistent identity or shared serialization, participation
in the dependency graph, internal dynamic evaluation, incidence or path/region
behavior, coherent consumption by several frontends, or cannot be achieved
through a read-only external service; outside it for GUI orchestration, session
authoring state, export formatting, menu or profile presentation, packaging,
icons and resources, help and analysis. No semantics may be duplicated in
Desktop or Python. The `AGENTS.md` §4 placement decisions apply, notably:
layers and print/export state are shared document/application model, not
geometric truth; zoom and navigation are never metric authority; DXF and
sheet/PDF generation are export services outside the kernel.

## Required design/specification

`P0` produces design candidates; it accepts none of them. The minimum set is:

1. an integrated characterization and design report under `docs/validation/`;
2. an architecture-owner matrix and a persistence matrix covering every state in
   the persistence section below;
3. a layer contract candidate (subphase `A`);
4. an export-surface and `ExportArea` contract candidate (subphase `B`);
5. the inputs that subphase `D0` needs to write the unit-system ADR and
   normative specification: the complete unit inventory, the persistence
   recommendation and the compatibility evidence. `P0` does not pre-empt `D0`'s
   normative decision;
6. design candidates for the semantic-curve exporters (`C`), the GGT library and
   icon ownership (`E1`), the native dimension tools (`E2`), `IsoABorder`
   (`E3`), orientation display (`F1`) and authoring memory (`F2`);
7. the verification-class recommendation for each later subphase, with the
   concrete impact facts that select it;
8. machine-readable evidence under `geocedg/validation/pre-g9b-r6-plus/` for the
   author-input inventory and the persistence matrix: plain JSON with a
   `schemaVersion`, as in
   `geocedg/validation/pre-g9b-r0/pre-g9b-r0-disposition.json`, with no new
   schema and no static-contract registration;
9. a corrected mini-track plan and roadmap status, if the evidence forces a
   change to a boundary or a dependency outside the author-fixed parts.

## Required findings

The report must answer each question below explicitly, with exact
`path:line` evidence, and must mark each answer `PROVEN FROM SOURCE`,
`PROVEN BY PROBE`, `INFERRED` or `UNKNOWN`. `UNKNOWN` is acceptable only with
the reason and the probe or reading that would settle it.

1. The actual upstream layer numeric domain and every assumption that depends on
   it: clamping, sorting, hit testing, selection sentinels, UI controls, XML
   read and write, scripting commands, SVG and DXF layer naming, and the 3D
   view. Then the least invasive correct extension that meets the author target
   (working layer not restricted to 0..9), its compatibility impact on legacy
   documents and older builds, and the verification class it implies for `A`,
   including escalation beyond `INTEGRATED_PHASE` if shared or serialization
   assumptions must change.
2. The ownership and persistence of the working layer.
3. The ownership and persistence of the hidden-layer set.
4. The exact current visibility decision path for painting an object, and where
   `effectiveVisible(object) = objectVisible(object) AND NOT
   layerHidden(object.layer)` can be evaluated without overwriting individual
   object visibility or changing what scripts and commands read as visibility.
5. The exact export-menu filtering architecture: upstream menu, GeoCeDG menu
   projection, the action registry and the profile.
6. The exact source, lifetime and persistence of `selectionRectangle`.
7. The relation of `Export_1` and `Export_2` to `selectionRectangle`, per
   exporter family.
8. Whether the author-reported clipping and padding behaviors (`AQ-X1`)
   reproduce, per format, for an `Export_1`/`Export_2` area that fits the
   viewport and for one larger than or outside it, proved by a scratch probe
   on the exported file, with the source cause of each.
9. The current scale and unit implementation of each exporter family.
10. Every legacy use of centimetres, physical scale, export unit and print
    scale, and whether each is only output-oriented. Classify each as a
    construction semantic unit, a presentation unit, a physical output or
    device unit, or a dimensionless number.
11. A suitable persistence owner for `constructionUnit`.
12. A suitable owner and default for `presentationUnit`.
13. The exact compatibility behavior for `unspecified/model-unit`, including
    legacy `.cedg` documents, Classic `.ggb` documents and copy/paste between
    documents with different units.
14. How dimensional annotations participate in the dependency graph today
    (author tools) and how native dimensions would.
15. Whether GGT style selection or memory (line type, color, thickness,
    remembered last style) is technically sustainable. If it is not, record that
    the author fallback of black at 3 pt applies.
16. The precise behavior and provenance of every author-supplied GGT, and the
    effect of the author-required `sheetISOland`/`sheetISOvert` modifications
    (`AQ-G3`: hide the outer dotted rectangle and all corner points) on each
    macro's outputs. Do not modify either tool.
17. The exact orientation semantics the kernel assigns to a line, a segment and
    a ray for each construction route (two points, parallel, perpendicular,
    point and vector, command input, redefinition), where that orientation is
    already observable, and the semantic orientation authority (accessor or
    derivation) for each supported type, given that `getDirectionInD3` is
    reversed for lines without an end point.
18. The current mode implementation of the parallel and perpendicular tools:
    accepted selection orders, selection lifetime and the least invasive hook
    for a remembered reference.
19. The impact of every planned feature on serialization.
20. The impact on the application profile, its action-catalog pin, the feature
    manifests and packaging.
21. The appropriate verification class for every later subphase.

## Persistence matrix

Classify every new state as exactly one of `DOCUMENT_SEMANTIC`,
`DOCUMENT_PRESENTATION`, `SESSION`, `USER_PREFERENCE` or `DERIVED_TRANSIENT`.
Evaluate at minimum:

```text
constructionUnit
presentationUnit
workingLayer
hiddenLayers
ExportArea
orientation-display option
parallel lastReference
perpendicular lastReference
GGT remembered style
dimension placement parameter
IsoABorder export linkage
```

The author fixed one classification and one compatibility rule. Treat both as
constraints, open only to a demonstrated compatibility blocker:

```text
classification:     constructionUnit = DOCUMENT_SEMANTIC
compatibility rule: a legacy document without constructionUnit
                    loads as UNSPECIFIED_MODEL_UNIT
```

For every other entry, give the candidate classification, the evidence, the
rejected alternatives and the serialization and verification consequence. Do
not infer a final classification from the planning documents where the author
has not fixed it. The author's current preference for `ExportArea` is
`SESSION` unless stronger source or user-experience evidence points elsewhere.

## Geometric invariants and degeneracies

`P0` changes no geometry. Its designs must preserve the author-fixed unit
semantics of the mini-track plan §4, which governs if this summary differs from
it. In particular, changing `constructionUnit` must not change coordinates,
authoritative numerical geometry, DAG topology, dependencies, semantic
identities, LocusV2 branch or component identity, SplineV2 semantics, parameter
domains, construction order or object identity. It is not geometric scaling, a
coordinate transformation or a global redefine. Changing `presentationUnit`
must not alter geometry or the physical interpretation of the model.

Designs must also preserve these separations:

```text
explicit ExportArea  != viewport
zoom / camera / screen clipping != export authority
render tessellation  != geometric authority
orientation cue      derives from kernel semantics, never from the viewport
```

Each design states its degeneracies: an empty or inverted area, a non-finite
coordinate, a zero-length or undefined line, a hidden working layer, an
undefined dimension, and an unspecified unit on either side of a conversion.

## Compatibility and serialization

`P0` changes no serialization. Its candidates must state, for every state they
add, whether it serializes, where, how a legacy document without it loads, how
a Classic `.ggb` loads, how an older GeoCeDG build treats it, and what migration
(if any) is needed. Never infer `cm` for a legacy document merely because an
exporter uses centimetres internally. Do not silently widen layer serialization
or any array-index or range assumption.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Characterization uses focused reading and scratch probes. Probes are not
acceptance evidence and must not run a heavy campaign as a side effect.

Acceptance for the frozen `DOCUMENTATION_STATUS_ONLY` class:

1. focused work and scratch probes;
2. before freezing: JSON validity of every new machine-readable file,
   resolution of every repository-relative Markdown link the phase adds,
   validation of this amended prompt (below), `git diff --check`, proof that no
   forbidden or product path changed, and the final local-input integrity
   check against the entry inventory;
3. one clean immutable candidate commit whose parent is
   `IMPLEMENTATION_BASE`, staging only authorized `P0` paths;
4. `tools/agent/verify.ps1 -Profile STATIC -LogDirectory <new external root>` on
   that exact candidate, with the verifier console redirected outside
   `artifacts/`; the required result is `ACCEPTED / COMPLETE` for the
   applicable `STATIC` plan;
5. `git diff --check`.

This prompt is changed under the explicit authorization above. Validate it,
after amendment and again before freezing, with `Test-PromptContractDocument`
from `tools/agent/prompt-contract-parser.psm1` and the `task` profile of
`geocedg/specs/operations/prompt-contracts.json`.

No `PHASE`, `INTEGRATION` or `FINAL` run is part of `P0`. Report the profile,
exact command, exit code, report path, acceptance verdict, coverage verdict,
diagnostic count and execution identity of every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's explicit instruction of 2026-10-01 names `PRE-G9B-R6-plus-P0` and
its exact base above. It authorizes only the characterization and design scope
of this file, the amendment of this prompt, scratch probes, the design
artifacts, one local frozen candidate and its `STATIC` verification.

`P0` authorizes nothing that follows it. Subphases `A`, `B`, `D0`, `D1`, `C`,
`E1`, `E2`, `E3`, `F1`, `F2` and `G` each need a separate explicit author
authorization naming an exact base. A design candidate produced by `P0` does
not become normative until the author accepts it explicitly. `PRE-G9B-R7`,
`G9B`, `G9C`, `G9U2`, productive `G10`, further `G12`, `PROFILE COMMERCIAL`,
exact DXF `SPLINE`, STL and Collada support stay unauthorized.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

The authorization covers a local branch and one clean local candidate commit.
Push, branch publication, merge, promotion to `main`,
rebase, squash, amend after freeze, force push, tag, release and binary
publication are forbidden. Each needs a separate explicit author instruction
naming the exact candidate SHA. Acceptance evidence never grants publication
authority.

## Acceptance and closeout

`P0` stops with one design candidate pending author review. Author approval is an
explicit decision naming the exact accepted commit. A frozen candidate artifact
records only invariant facts:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

The candidate is never amended after freeze. If a correction is needed
afterwards, stop and report rather than create an unrequested descendant.

## Required artifacts

- the integrated characterization and design report, with the author-input
  inventory and SHA-256s, the `ReportExport.md` claim ledger, the per-GGT
  characterization, the answers to the required findings, the probe results,
  both matrices, the unit inventory and the verification recommendation per
  subphase;
- the design candidates listed under Required design/specification;
- the machine-readable evidence under `geocedg/validation/pre-g9b-r6-plus/`;
- the exact `STATIC` command, exit code and log path, and `git diff --check`;
- `PRODUCT_PHASE_EFFECT = NONE`, `VERIFICATION_INFRASTRUCTURE_IMPACT`,
  `BOOTSTRAP IMPACT` and `GUIDE_IMPACT` declarations;
- an explicit list of questions that need an author decision before each later
  subphase can be authorized, reconciling the plan's `AQ-*` list.

## Stop conditions

Stop and report rather than guess when:

- any entry identity differs from `IMPLEMENTATION_BASE` or
  `IMPLEMENTATION_BASE_TREE`;
- this prompt cannot be reconciled to the authorizing instruction without
  changing the authorized `P0` scope;
- a required finding would need a product, serialization, profile or packaging
  change to establish, or `P0` itself would need a serialization change;
- the author-fixed unit semantics cannot be preserved, or `constructionUnit =
  DOCUMENT_SEMANTIC` meets a demonstrated compatibility blocker. Report the
  blocker with evidence; do not choose a replacement rule;
- the layer extension for the author's arbitrary-layer target would change
  serialized semantics without a migration plan. Report the design and its
  classification for author decision; do not choose a lossy rule;
- a GGT's rights or provenance are unclear for an intended distribution;
- a design would place geometric truth in a GUI, exporter, script, generated
  artifact or icon;
- an `ExportArea` design would create a second hidden export-area mechanism or
  several competing authorities;
- the work needs a verification level above `DOCUMENTATION_STATUS_ONLY`;
- the local author input was modified by the agent;
- source evidence contradicts an author requirement in a way that requires an
  author choice.

A missing author input is **not** a stop condition: report it and continue with
every part that repository or upstream evidence can establish.

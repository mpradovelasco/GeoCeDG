# PRE-G9B-R6-plus-P0 — integrated characterization and normative design

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created by the `PRE-G9B-R6-plus` planning task, which the author authorized on
2026-10-01 for planning and governance work only. That task froze a local
planning candidate pending author review. The existence of this file is not
authorization. Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-P0`, names the exact published commit and tree to start from,
and replaces the unresolved base fields below. The planning authorization of the
mini-track does not extend to `P0`.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = CHARACTERIZATION AND DESIGN CANDIDATES — NO PRODUCT CHANGE
DEPENDS_ON               = author approval of the PRE-G9B-R6-plus planning
                           candidate, named by exact SHA
DEPENDS_ON_PHASE         = PRE-G9B-R6 = PASS — AUTHOR APPROVED — PUBLISHED
```

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
CHANGE_ROUTE        = ORDINARY
VERIFICATION_CLASS  = DOCUMENTATION_STATUS_ONLY   (proposed)
frozenAtPhaseStart  = REQUIRED AT AUTHORIZATION
PLANNED_ACCEPTANCE  = STATIC on the frozen candidate; no FINAL
```

`DOCUMENTATION_STATUS_ONLY` is correct only while `P0` touches documentation,
design and planning paths. If the work would need a change to
`geocedg/specs/operations/verification-*`, `prompt-contracts.json`, any
`tools/agent/**` script or any `source/**` path, stop and report a
`VERIFICATION_ESCALATION_REQUEST` instead of widening scope.

### Author-fixed scope (plan §2)

1. **Layers.** Explicit working layer, changeable at any time; new objects use
   it; a working-layer tool with the Move tools unless characterization gives a
   compelling reason otherwise; working layer in the status bar; Algebra View
   `Sort by Layer` kept; layers hidden and shown from the Algebra View without
   overwriting object visibility, so that `effectiveVisible(object) =
   objectVisible(object) AND NOT layerHidden(object.layer)`. Characterize the
   numeric domain before promising arbitrary layer numbers.
2. **Status bar.** An extensible GeoCeDG status bar showing at least working
   layer, construction unit and presentation unit. It is presentation state,
   never geometric authority.
3. **Export surface.** Expose Graphics View as Picture (PNG, PDF, SVG,
   EMF/EMF+), DXF, PSTricks, PGF/TikZ and Asymptote. Do not expose STL,
   Collada, HTML Collada or Dynamic Worksheet as Web Page. Correct
   explicit-area padding and clipping where source confirms them. File actions
   to define and display the export area.
4. **`ExportArea`.** One authority with candidate producers `Export_1`/
   `Export_2`, a manual rectangle, an ISO A selection and `IsoABorder`; an
   explicit area is exported completely even outside the viewport; the visible
   view is the fallback only without an explicit area. Author preference:
   session lifetime.
5. **Semantic export.** `LocusV2` and `SplineV2` in the applicable LaTeX and
   vector exporters, from semantic geometry, never from render tessellation.
6. **Units.** `constructionUnit`, `presentationUnit` and physical export as
   fixed in plan §4.
7. **GGT library.** Curated library with provenance, eventual installer
   integration and GeoCeDG-owned, reproducibly maintained icons; style memory
   if sustainable, otherwise black at 3 pt.
8. **Native dimensions.** `DirectDimension` and `AxisDimension` (placement,
   help, unit conversion, traceable and movable placement, no far-away
   placement, centred figure, deterministic above/left side, parallel text
   where feasible, no hidden unit multiplier).
9. **`IsoABorder`.** Native, menu `Construction → Annotations and Media`, no
   toolbar button; inputs upper-left point, ISO A size, orientation, drawing
   scale; may configure the common `ExportArea` after a yes/no question.
10. **Orientation display.** Optional discreet cue (preferably a hollow arrow)
    of the kernel direction of a line, segment or ray; never from the viewport.
11. **Authoring refinements.** `Angle with Given Size` creates its angle hidden;
    independent remembered references for the parallel and perpendicular tools
    (point first reuses; reference first then point replaces and remembers;
    first use asks).
12. **Help.** A final integrated help phase.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE      = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
IMPLEMENTATION_BASE_TREE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. The planning
candidate that created this file cannot name its own commit. Its exact identity
is recorded in the author's review and closeout record, not here.

Published provenance from which `PRE-G9B-R6-plus` planning began, recorded as
provenance only and **not** as this phase's implementation identity:

```text
P_R6       = 9b8bc5b10a7ef61da0095853ea4c572ca06bfa72
P_R6_TREE  = f5c8c9f0cc76982170c6282a1242d2a5d07a0897
PRE-G9B-R6 = PASS — AUTHOR APPROVED — PUBLISHED
```

At entry, verify that local `main`, `origin/main` and the live remote `main`
equal the commit the author names, that the tracked worktree is clean, and that
no remote change is unexpected. **Execution is forbidden while either base
field remains unresolved.** If any identity differs, stop.

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
  product asset.

Two author inputs that the scope refers to were **not supplied** with the
planning instruction: the concrete clipping and padding observations
(`AQ-X1`) and the list of requested `sheetISOland`/`sheetISOvert`
modifications (`AQ-G3`). Use them if the authorizing instruction supplies them.
Otherwise characterize clipping and padding with generic probes, leave the
author-specific comparison open, and record the sheet modifications as an open
author input. Never invent either.

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
- roadmap reconciliation limited to status and to corrections the evidence
  forces, without marking anything authorized or approved;
- canonical recommendations for each later subphase prompt. Creating or editing
  prompt files is allowed only under `.github/prompts/tasks/pre-g9b-r6-plus-*`
  and only if the authorizing instruction explicitly includes it.

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
2. the exported files for an explicit area larger than or outside the
   viewport, per picture format including SVG, and whether a leftover selection
   rectangle crops a later export (plan §6.2, §6.3);
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
`PROVEN BY PROBE` or `INFERRED`. "Unknown" is acceptable only with the reason
and the probe that would settle it.

1. The actual upstream layer numeric domain and every assumption that depends on
   it: clamping, sorting, hit testing, selection sentinels, UI controls, XML
   read and write, scripting commands, SVG and DXF layer naming, and the 3D
   view.
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
8. Whether clipping and padding defects reproduce, including an explicit area
   larger than, or outside, the visible viewport, proved by a scratch probe on
   the exported file; and, if the author supplies observations (`AQ-X1`),
   whether those reproduce.
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
16. The precise behavior and provenance of every author-supplied GGT, and, if
    the author supplies them (`AQ-G3`), the requested modifications to
    `sheetISOland` and `sheetISOvert`. Do not modify either tool.
17. The exact orientation semantics the kernel assigns to a line, a segment and
    a ray for each construction route (two points, parallel, perpendicular,
    point and vector, command input, redefinition) and where that orientation is
    already observable.
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

Acceptance for the proposed `DOCUMENTATION_STATUS_ONLY` class:

1. focused work and scratch probes;
2. one clean immutable candidate commit;
3. `tools/agent/verify.ps1 -Profile STATIC -LogDirectory <new external root>` on
   that exact candidate, with the verifier console redirected outside
   `artifacts/`;
4. JSON validity of every new machine-readable file, and resolution of every
   repository-relative Markdown link the phase adds;
5. `git diff --check`.

If a prompt file is changed under an explicit authorization, also validate each
changed task prompt with `Test-PromptContractDocument` from
`tools/agent/prompt-contract-parser.psm1` and the `task` profile of
`geocedg/specs/operations/prompt-contracts.json`.

No `PHASE`, `INTEGRATION` or `FINAL` run is part of `P0`. Report the profile,
exact command, exit code, report path, acceptance verdict, coverage verdict,
diagnostic count and execution identity of every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Execution requires a new explicit author
instruction naming `PRE-G9B-R6-plus-P0` and its exact base. That instruction
authorizes only the characterization and design scope above.

`P0` authorizes nothing that follows it. Subphases `A`, `B`, `D0`, `D1`, `C`,
`E1`, `E2`, `E3`, `F1`, `F2` and `G` each need a separate explicit author
authorization naming an exact base. A design candidate produced by `P0` does
not become normative until the author accepts it explicitly. `PRE-G9B-R7`,
`G9B`, `G9C`, `G9U2`, productive `G10`, further `G12`, `PROFILE COMMERCIAL`,
exact DXF `SPLINE`, STL and Collada support stay unauthorized.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Once `P0` is authorized, that authorization covers a local branch and one clean
local candidate commit. Push, branch publication, merge, promotion to `main`,
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

- `IMPLEMENTATION_BASE` or `IMPLEMENTATION_BASE_TREE` remains unresolved, or any
  entry identity differs;
- a required finding would need a product, serialization, profile or packaging
  change to establish;
- the author-fixed unit semantics cannot be preserved, or `constructionUnit =
  DOCUMENT_SEMANTIC` meets a demonstrated compatibility blocker. Report the
  blocker with evidence; do not choose a replacement rule;
- arbitrary layer numbers would require widening serialization or range
  assumptions without an author decision;
- a GGT's rights or provenance are unclear;
- a design would place geometric truth in a GUI, exporter, script, generated
  artifact or icon;
- an `ExportArea` design would create a second hidden export-area mechanism;
- the work needs a verification level above `DOCUMENTATION_STATUS_ONLY`.

A missing author input is **not** a stop condition: report it and continue with
every part that repository or upstream evidence can establish.

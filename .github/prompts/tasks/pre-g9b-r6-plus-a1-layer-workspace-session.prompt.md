# PRE-G9B-R6-plus-A-1 — layer workspace with session-only layer state

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-02. That
instruction fixed the decisions for `PRE-G9B-R6-plus-A`, split `A` into `A-1`
and `A-2`, and named the exact published base below. It stated that these
decisions **do not yet authorize implementation**. The decisions are recorded,
versioned, in the
[A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-A-1` and confirms or replaces the base below. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `P0` precedent. This file is an
execution contract, not a second policy document: the design input is the
`P0` [layer-workspace design candidate](../../../docs/architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md),
as amended by the author decisions.

```text
PRE-G9B-R6-plus-A-1 =
PREPARED — NOT AUTHORIZED

selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — DESKTOP SESSION AND VIEW PRESENTATION
DEPENDS_ON               = PRE-G9B-R6-plus-P0 = PASS — AUTHOR APPROVED — PUBLISHED
NEXT_SUBPHASE            = PRE-G9B-R6-plus-B
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review.

<!-- geocedg-field: objective -->
## Objective

Give GeoCeDG an operational layer workspace within the existing `0..9` layer
domain:

- an explicit session working layer that every interactive creation route uses;
- a one-shot working-layer mode in the Move group;
- a status bar that shows the working layer;
- numeric `Sort by Layer` order in the Algebra View, with a per-layer eye;
- effective hiding of whole layers in the normal views, through painting and
  hit testing, held as **session-only** state, without writing object
  visibility.

```text
effectiveVisible(object) = objectVisible(object) AND NOT layerHidden(object.layer)
```

`A-1` widens no domain, adds no serialization and changes no exporter. Domain
widening to `0..99` and hidden-layer persistence belong to `A-2`. The SVG
export route belongs to `B`. The LaTeX and DXF exporters belong to `C`.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = INTEGRATED_PHASE        (author-accepted on 2026-10-02;
                                                frozen at authorization)
frozenAtPhaseStart   = REQUIRED AT AUTHORIZATION
PLANNED_ACCEPTANCE   = registered PHASE  -Phase PRE-G9B-R6-plus-A-1
                       + INTEGRATION
                       both on the same exact frozen candidate; no FINAL
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` defines
`INTEGRATED_PHASE` as "PHASE; add COMPOSED only when the frozen plan identifies
concrete additional integration coverage", and maps legacy `COMPOSED` to the
`INTEGRATION` profile. This plan identifies that coverage: the change touches
every interactive creation route (tools, Algebra Input, scripts and `Execute`,
macro outputs, Locus V2 and Spline V2), the shared `EuclidianView` painting and
hit testing used by Graphics 1 and Graphics 2 and by every export that reuses
that painting, the Algebra View, the toolbar profile and the application
layout. The acceptance gate is therefore both the registered PHASE selection
and `INTEGRATION`; neither substitutes for the other. The class is not
downgraded because single edits are small. If the implemented change would
touch shared serialization, the `<layer>` domain, a document element, the undo
XML or the preferences XML, it has left `A-1`: stop and report a
`VERIFICATION_ESCALATION_REQUEST` rather than escalate silently.

### Author decisions this prompt implements

The author decisions of 2026-10-02, recorded in the
[A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md),
which is their authority:

| ID | Decision (substance) | Effect in `A-1` |
|---|---|---|
| `AQ-L1a` | GeoCeDG layer domain `0..99` for the first extension; `L_MAX = 99` is a product/configuration limit, not a permanent CeDG restriction; Classic keeps `0..9`. | none; `A-2` |
| `AQ-L1b` | `A` is split into `A-1` and `A-2`. | this prompt |
| `AQ-L1c` | Loss of forward compatibility with older Classic and GeoCeDG readers is accepted and documented: layers above 9 may be clamped to 9 and lost on re-save. | none; `A-2` documents it |
| `AQ-L2` | `workingLayer` is `SESSION`. New starts at layer 0. Initialization after Open preserves the document and never silently modifies persisted visibility state. | implemented |
| `AQ-L3` | Hidden layers are conceptually `DOCUMENT_PRESENTATION`, document-wide, persistent and not undoable. `A-1` implements the mechanism with `SESSION` state; `A-2` adds persistence. Objects on hidden layers are excluded from LaTeX. The DXF policy is resolved in `C`. | mechanism and `SESSION` state; LaTeX exclusion is implemented in `C` |
| `AQ-L4` | The working-layer mode is one-shot: it returns to Move after a valid selection. | implemented |
| `AQ-L5` | Upstream `ShowLayer` and `HideLayer` stay unchanged and keep acting on object visibility. | unchanged, regression-tested |
| `AQ-L7` | See below. | implemented for session state; Open × persistent hidden layers deferred to `A-2` |
| `AQ-L8` | Paste keeps the copied objects' layers; it does not move them to the working layer. | implemented, regression-tested |

**`AQ-L7` — the working layer and hidden layers.**

1. The working layer cannot be hidden through normal interaction.
2. If the user selects as working layer a layer that is currently hidden, that
   layer is shown and becomes the working layer.
3. The interaction between Open and persistent hidden layers is deferred to
   `A-2`. It is resolved explicitly by the author before `A-2`, never by
   silently changing presentation on load. `A-1` has no persistent hidden
   layers, so the case cannot arise in `A-1`.

No other open decision of the
[P0 candidate report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15 is resolved by this prompt or by `A-1`.

### Contract details fixed by this prompt

Where the author decisions leave a detail open, this prompt adopts the `P0`
design candidate. Each item below is a contract of the prepared prompt, not an
author decision, and the authorizing instruction may change it.

- **Working layer after Open.** It is set to the highest layer used by a
  drawable object of the opened document (`0` for a document without
  drawables). Choosing it only reads the document. Open writes no object
  layer, object visibility or other document state, and the opened document
  saves byte-identically if the user changes nothing.
- **Working layer through undo, redo and redefinition.** Unchanged. A rebuild
  never reads or resets it.
- **Hidden-layer set (`SESSION`).** Empty after New and after Open. Unchanged
  by undo, redo and redefinition. Never written to the document XML, the undo
  XML, the macro XML, the clipboard or the preferences. Toggling it creates no
  undo point and does not mark the document modified.
- **How `AQ-L7` (1) is enforced.** The eye of the working layer is disabled in
  the Algebra View, so no normal interaction can hide it. Selecting a hidden
  layer as working layer, by any route (mode, chooser, status-bar segment),
  removes it from the hidden set.
- **Mode gestures.** In the working-layer mode, a click on a hittable object
  adopts its layer and returns to Move. A click on empty space opens a numeric
  chooser bounded by `0..9`; a valid choice sets the working layer and returns
  to Move. Cancel or Esc leaves the working layer unchanged.
- **Status bar.** One segment, `Layer: n`, localized for at least `en` and
  `es`. A click on it opens the same chooser. The segment API is extensible for
  `D1`, which adds unit segments; `A-1` adds no unit segment.

### Temporary export inconsistency between `A-1` and `B`/`C`

`A-1` changes no exporter. Exports that reuse the shared view painting
(`EuclidianView.exportPaint` and the view image) inherit the painting hook
without any exporter change: PNG, PDF, EMF, print and the graphics-view image
on the clipboard omit objects on hidden layers. Exporters with their own path
do not:

| Export | Behavior for an object on a hidden layer after `A-1` | Owner of the final behavior |
|---|---|---|
| PNG, PDF, EMF, print, graphics image copy | omitted, inherited from painting | `B` confirms within the export surface |
| SVG (own loop over the drawable list, `GraphicExportDialog`) | **still written**, as at the base | `B` |
| PGF/TikZ, PSTricks, Asymptote | **still written**, as at the base | `C` (exclusion already decided, `AQ-L3`) |
| DXF | **still written**, as at the base | `C` (policy open) |

Until `B` and `C` close, the same construction with a hidden layer can
therefore produce a picture without the object and an SVG, LaTeX or DXF file
with it. The phase documents this inconsistency in its candidate report, in the
living user and developer documentation it touches, and in its author-smoke
checklist. It does not correct it.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
562e2bb1e77b249b9c97c2e6e06e8b123d6de900          (P_R6PLUS_P0, published P0 closeout)

IMPLEMENTATION_BASE_TREE =
9f578093f2f3cba481f5630fe827fd3ef08cff61

P0_STATE =
PASS — AUTHOR APPROVED — PUBLISHED

P0_APPROVED_TECHNICAL_CANDIDATE =
6b7fd5de343b6556b384e71a9345907e7944d1e5          (T_R6PLUS_P0)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal `IMPLEMENTATION_BASE`, its tree equals
`IMPLEMENTATION_BASE_TREE`, and the worktree is clean. If the documentary
commit that publishes this prompt and the author-decision record is published
first, the authorizing instruction names that commit instead; this prompt
assumes no later base on its own. The phase works on a new local branch from
the named commit and is not rebased onto any later commit without a new author
instruction. Between `P_R6PLUS_PLAN` (`babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`)
and this base only documentation changed, so the `P0` source citations still
apply; the phase re-establishes every one it relies on before using it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at the base.
3. The [A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md).
4. Design input, re-characterized before use: the `P0` layer-workspace design
   candidate §2 and §4–§9, the
   [P0 report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
   findings 1–4 and probe P3, and the
   [P0 closeout record](../../../docs/validation/pre_g9b_r6_plus_p0_closeout_record.md).
   Candidate §3 (domain) and the persistence part of §7 belong to `A-2`;
   candidate §7 items 3 (SVG) and 4 (LaTeX, DXF) belong to `B` and `C`. None of
   them is implemented here.
5. Generated artifacts, earlier reports and previous agent output are
   evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Working layer and creation

- An `AppGeoCeDG`-owned working-layer session service (Desktop session state;
  not geometric truth, `AGENTS.md` §4).
- One new overridable app hook for the layer of a newly created object. Its
  default returns exactly the upstream value. It is called from
  `ConstructionDefaults.setMaxLayerUsed` and `AlgoMacro.createOutputObjects`.
  `AppGeoCeDG` returns the working layer for interactive creation only. While a
  file, an undo or redo point or a redefinition rebuild is read, it returns the
  upstream value, so a rebuilt document is unchanged. Classic and Web keep the
  upstream behavior.
- The GeoCeDG Locus V2 family applies the same hook when it creates outputs.
  This closes `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` for new
  objects only. Stored layers of existing documents are not rewritten.
- Routes covered: tools, Algebra Input, GGBScript and nested `Execute`, macro
  outputs, Locus V2 and Spline V2. Paste is not a creation route and keeps the
  copied layers (`AQ-L8`).

### Working-layer mode

- New GeoCeDG mode `MODE_WORKING_LAYER = 141`, after re-checking at the base
  that `141` is free, on the `MODE_ORDERED_LIST = 140` precedent: the constant,
  name keys, `menu*.properties` strings for at least `en` and `es`, an
  `upstream-mode` profile action with `audited_numeric_id`, controller dispatch
  in `GeoCeDGEuclidianController.switchModeForProcessMode`, and an owned icon.
- The action is placed in the native `edit-selection` group after
  `construction.move` and `construction.move-rotate`. The action-count pin
  (115 → 116 at the base), the `edit-selection` toolbar pins, the group-order
  pins and the tests that assert them change together and only by that one
  action. The stale workspace action count in
  `geocedg/specs/ui/cedg-workspaces.md` may be corrected to the new truth.

### Status bar

- A GeoCeDG status-bar component in the `SOUTH` slot of the application panel,
  re-added by an `AppGeoCeDG` override of `updateApplicationLayout`, because
  the upstream method clears the side panels on every layout change. It is
  presentation state only and the kernel never reads it.

### Hidden layers and effective visibility in the normal views

- One presentation predicate, `isLayerShown(layer)`, backed by the session
  hidden-layer set. In `A-1` it is consulted only by:
  1. painting, through a protected hook in the shared `EuclidianView` drawing
     of the drawable list (the drawing reached from `drawObjects`), so that
     Graphics 1 and Graphics 2 obey it;
  2. hit testing, through a post-filter of the `EuclidianView.setHits`
     results, so that objects on hidden layers are not hittable by any mode.
- The hook lives at the view's drawing of the drawable list, not inside
  `Drawable` or any exporter, so the SVG, LaTeX and DXF paths stay unchanged.
- The predicate is **not** placed in `isVisibleInThisView`, which also feeds
  slope-field and ODE geometry.
- The Algebra View `Sort by Layer` order becomes numeric. Each layer group gets
  an eye that toggles the layer's hidden state and a label that shows it,
  through a `GeoCeDGAlgebraView` renderer override and click listener.
- A change of the hidden-layer set repaints the affected views; no drawable is
  lost or duplicated by a toggle.

### Supporting changes

- Tests for every obligation below, registered in
  `docs/upstream/modified-files.yml` where they live under `source/`.
- Registration of the upstream files modified by this phase in
  `docs/upstream/modified-files.yml`, with the narrowest correct rationale.
- The registered `PRE-G9B-R6-plus-A-1` PHASE selection in the typed
  verification registry, covering every focal obligation below, with the
  JUnit inventory and registry-shape pins it needs, through the existing
  mechanisms and the official updater.
- Living documentation that describes the new behavior, including the
  temporary export inconsistency (developer and user guidance, the workspace
  specification), plus the candidate report and its machine-readable evidence.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any change to the layer domain: `GeoElement.setLayer`, the clamp,
  `EuclidianStyleConstants.MAX_LAYERS`, `App.updateMaxLayerUsed`, the
  properties layer models, the 3D renderer layer coding and the XML read path
  stay as at the base (`A-2`).
- Any new serialization: no document element, no XML attribute, no undo-XML
  content, no preferences key, no macro or clipboard content for the working
  layer or the hidden-layer set (`A-2` for hidden layers; the working layer is
  never serialized).
- Any write of object visibility, object layer or other document state by the
  working layer, the hidden-layer set, the mode, the chooser, the status bar or
  Open initialization.
- `ShowLayer`, `HideLayer`, `GgbAPI.setLayerVisible`, `IsVisible`, the
  `<show object>` flag, the Algebra View marble and the Show/Hide tool: their
  behavior stays unchanged (`AQ-L5`).
- Paste moving objects to the working layer (`AQ-L8`).
- Any exporter change. The SVG export route, including its loop over the
  drawable list in `GraphicExportDialog`, belongs to `B`. The PGF/TikZ,
  PSTricks and Asymptote exporters, including their shared object-visibility
  test, and the LaTeX exclusion decided in `AQ-L3` belong to `C`. DXF stays
  entirely outside `A-1`; its hidden-layer policy is completed in `C`. The
  export-area, export-surface, unit and scale behavior of every exporter stays
  as at the base.
- Resolving the interaction between Open and persistent hidden layers
  (`AQ-L7` (3)); it belongs to `A-2`.
- Every other subphase (`A-2`, `B`, `D0`, `D1`, `C`, `E1`, `E2`, `E3`, `F1`,
  `F2`, `G`), `PRE-G9B-R7` and `G9B`.
- Classic and Web behavior, and the Classic diagnostic session.
- Changes to the verifier, its schemas, `prompt-contracts.json`, the
  governance layer or `tools/agent/**`, beyond registry and inventory data
  updated through existing mechanisms.
- Any commit, move, rename, rewrite or copy of `artifacts/author-input/**`.

## Architectural placement

- Working layer, hidden-layer set, mode and status bar: Desktop GeoCeDG
  session and presentation (`geocedg-desktop`). They are not geometric truth.
- The creation hook: a minimal overridable seam in shared `App`, with an
  upstream-identical default, because the layer of a new object is chosen in
  shared kernel code. It carries no CeDG policy; `AppGeoCeDG` supplies it.
- The painting and hit-test hooks: minimal protected seams in shared
  `EuclidianView`, with upstream-identical defaults, because Graphics 2 is an
  upstream view class and must obey the same predicate.

Each shared seam is the narrowest that serves the behavior and is recorded in
`docs/upstream/modified-files.yml`. If an owner is wrong at the base, stop and
report instead of choosing another layer.

## Required design/specification

The `P0` design candidate, amended by the author-decision record, is the
design. Before implementation, the phase records in its candidate report any
place where the base contradicts the candidate, with evidence, and the
correction it adopts within this scope. A contradiction that would change
scope, owner, serialization or class stops the phase.

## Geometric invariants and degeneracies

`A-1` changes no geometric definition, algorithm, dependency or numeric value.
For every construction, the kernel state, the construction XML and the computed
values are identical with and without hidden layers and for any working layer,
except the layer of newly created objects.

| Case | Required behavior |
|---|---|
| object visible, layer hidden | not painted in Graphics 1 or 2, not hittable; object visibility, marble and `IsVisible` unchanged |
| object hidden, layer shown | as at the base |
| every layer hidden | empty views; the status bar still shows the working layer |
| working layer hidden | impossible through normal interaction (`AQ-L7` (1)) |
| hidden layer selected as working layer | shown, then working layer (`AQ-L7` (2)) |
| object without a drawable (numeric) | painting unaffected; `Sort by Layer` grouping as at the base |
| dependent numeric without a `<layer>` tag, reloaded or undone | the upstream value, never the working layer |
| slope field, ODE integral on a hidden layer | geometry computed as at the base |
| Locus V2 or Spline V2 created interactively | on the working layer |
| paste | copied layers kept |
| chooser value outside `0..9` | refused or bounded to `0..9`; never silently wraps |

## Compatibility and serialization

No serialization change. Documents saved by `A-1` are byte-identical to the
base for the same construction, apart from the layers chosen for new objects.
Legacy `.cedg` and `.ggb` documents open and re-save unchanged. Older GeoCeDG
builds and Classic read `A-1` documents unchanged. No feature flag, migration or
format version is introduced. The forward-compatibility loss accepted in
`AQ-L1c` does not arise in `A-1`, because the domain stays `0..9`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation, and the registered PHASE selection
executes all of them:

| ID | Obligation |
|---|---|
| `T-WL-ROUTES` | tools, Algebra Input, GGBScript, nested `Execute`, macro outputs, Locus V2 and Spline V2 create on the working layer |
| `T-WL-REBUILD` | Open, undo, redo and redefinition rebuild stored layers unchanged; a dependent numeric without a `<layer>` tag never takes the working layer |
| `T-WL-LIFECYCLE` | New → 0; Open → highest drawable layer of the document; undo, redo and redefinition leave it unchanged; Open writes nothing and an unchanged document re-saves byte-identically |
| `T-PASTE` | paste keeps copied layers for every working layer |
| `T-MODE` | mode 141 dispatch; click on an object adopts its layer and returns to Move; empty-space chooser bounded to `0..9`; cancel and Esc leave the layer unchanged |
| `T-PROFILE` | the action count, `edit-selection` and group-order pins change by exactly one action; Classic profile unchanged |
| `T-STATUS` | the status bar survives every layout rebuild; `Layer: n` tracks every working-layer change; `en`/`es` strings |
| `T-AV-ORDER` | numeric `Sort by Layer` order |
| `T-AV-EYE` | the eye toggles the session state; the working layer's eye is disabled |
| `T-L7` | the working layer cannot be hidden by any normal interaction; selecting a hidden layer as working layer, by every route, shows it and makes it the working layer |
| `T-HIDE-PAINT` | Graphics 1 and Graphics 2 omit objects on hidden layers; showing the layer restores them; no drawable lost or duplicated |
| `T-HIDE-HIT` | objects on hidden layers are not hittable by any mode |
| `T-HIDE-STATE` | toggles create no undo point, do not mark the document modified, and write nothing to the document, undo, macro, clipboard or preferences XML; New and Open clear the set; undo and redo keep it |
| `T-HIDE-GEOMETRY` | slope-field and ODE results identical with their layer hidden |
| `T-OBJECT-VISIBILITY` | `ShowLayer`, `HideLayer`, `setLayerVisible`, `IsVisible`, `<show object>`, the marble and the Show/Hide tool behave as at the base |
| `T-CLASSIC` | upstream defaults of every new shared seam reproduce base behavior |
| `T-EXPORT-INHERITED` | PNG and PDF exported through the shared painting omit objects on hidden layers, with no exporter file changed |
| `T-EXPORT-INTERIM` | SVG, the three LaTeX exporters and DXF produce the same output as at the base for a construction with a hidden layer (records the `B`/`C` boundary) |
| `T-SMOKE` | an explicit author-smoke checklist, including the temporary export inconsistency; the agent does not perform author smoke |

Desktop tests mock `JOptionPane`, inject the spatial-redefine presentation
where a redefine dialog could open, and wait for the asynchronous undo store.

Then the adjacent creation, macro, Locus V2, Spline V2, undo, XML, toolbar
profile, Algebra View and export regressions; Checkstyle; the
upstream-boundary validation; the JUnit inventory and registry pins through the
official updater with executed selection evidence; and `git diff --check`.

Acceptance reproduces the `INTEGRATED_PHASE` contract of
`verification-levels.md` §12.8: the registered PHASE selection, plus COMPOSED
(`INTEGRATION`) because this plan names concrete additional integration
coverage. On one frozen clean immutable candidate, run both:

```text
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-A-1 -LogDirectory <fresh root outside artifacts/>
tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <fresh root outside artifacts/>
```

Both must be `ACCEPTED / COMPLETE` on the same exact commit and tree. The
PHASE run authenticates the phase-named obligations; `INTEGRATION` adds the
repository-level integration coverage. Neither substitutes for the other, a
narrow focal or DEV PASS substitutes for neither, and neither is repeated for
an unchanged candidate. `FINAL` is not run; if the implemented scope would need
it, the phase stops first. Report exact commands, exit codes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. The author decisions it records do not
authorize implementation. Execution requires a new explicit author instruction
naming `PRE-G9B-R6-plus-A-1` and its exact base. That instruction would
authorize only the scope above: local implementation, local commits, technical
verification and one frozen technical candidate for author review and smoke.

`A-1` authorizes nothing that follows it. The accepted order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`. `A-2` needs its own
canonical prompt and authorization, and before it the author resolves the
interaction between Open and persistent hidden layers (`AQ-L7` (3)). `B`, `D0`,
`D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B`
stay unauthorized. Author approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Once authorized: a local branch from the base and local commits only. Push,
branch publication, merge, promotion to `main`, rebase, squash, amend after
freeze, force push, tag, release and binary publication are forbidden; each
needs a separate explicit author instruction naming the exact candidate SHA.
Acceptance evidence never grants publication authority.

## Acceptance and closeout

`A-1` stops with one technically verified candidate pending author review and
author smoke. Author approval is an explicit decision naming the exact accepted
commit. The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. Closeout and publication are separately
authorized documentary steps.

## Required artifacts

- The implementation, tests and registrations above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-characterized citations and any correction to the design candidate; the
  obligation-to-test map; the temporary export inconsistency table; the
  Open-initialization behavior and its byte-identity evidence; the list of new
  shared seams with their upstream-identical defaults; residual risks and
  observations; the author-smoke checklist.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale, the
  verification-infrastructure-impact assessment, `GUIDE_IMPACT` with paths,
  and the exact PHASE and `INTEGRATION` commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- the change would need a domain change, any serialization, an undo-XML or
  preferences write, or a write to object visibility or object layers;
- a rebuild (Open, undo, redo, redefinition) cannot be distinguished from
  interactive creation through the hook without a wider kernel change;
- effective hiding cannot be placed without `isVisibleInThisView`, without
  changing geometry, or without touching an exporter;
- Graphics 2 cannot obey the predicate within the allowed seams;
- mode `141` is taken, or the profile delta exceeds one action;
- `ShowLayer`, `HideLayer` or paste would change behavior;
- Open initialization would modify the document or its visibility state;
- the registered PHASE selection cannot be created through existing
  mechanisms;
- current governance requires a verification class other than
  `INTEGRATED_PHASE`;
- the work would belong to `A-2`, `B`, `C`, `D0`, `D1` or later.

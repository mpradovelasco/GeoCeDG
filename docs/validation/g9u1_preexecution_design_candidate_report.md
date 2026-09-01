# G9U1 pre-execution design candidate report

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Phase: G9U1 — CeDG Construction workspace
- Productive implementation: **NOT STARTED / NOT AUTHORIZED**
- Planning branch: `feature/g9u1-construction-workspace-planning`
- Published predecessor: G9S1 `PASS — AUTHOR APPROVED` at
  `de33f3a80102adb051aaa7547a72b7e97409c58c`
- Proposed mandatory kernel prerequisite: G9U0-R6 — Semantic Locus Point
  Interaction Support, **NOT AUTHORIZED / NOT IMPLEMENTED**
- Retained risk: `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED`

## 1. Stage-B entry authority

Stage B began only after G9S1 had been closed, published and independently
verified from clean `main`. The G9S1 closeout is recorded in the
[G9S1 report](g9s1_semantic_spline_2d_capability_candidate_report.md) and the
annotated `geocedg-g9s1-pass` tag peels to
`de33f3a80102adb051aaa7547a72b7e97409c58c`.

Post-promotion verification from that clean commit established:

- G9S1 focused authority: **37/37 PASS**;
- G9U0-R5 focused authority: **46/46 PASS**;
- G9U0-R4 focused authority: **58/58 PASS**;
- full `tools/agent/verify.ps1`: exit 0 with
  `All GeoCeDG verification gates passed.`; and
- `HEAD = main = origin/main = direct remote origin/main` at the published
  G9S1 commit before the planning branch was created.

The final accepted metric smoke used the actual generator branch key
`generator.main`; Spline V2 total/partial values were `4`/`2`, and the ordinary
Locus V2 partial value was `2`. The formerly suggested
`scalar-locus/main` key was an erroneous smoke instruction, not a product
defect.

## 2. Predecessor prompt and definitive candidate

The file
[`g9u1-construction-workspace-after-g9s1.prompt.md`](../../.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md)
was an unexecuted, not-authorized prospective prompt. It inherited the
post-R5 workspace contract and incorporated G9S1, but it did not yet reconcile
the complete professional action inventory, all author-observed interaction
defects, the semantic Point prerequisite, the 98-scenario authority or the
single schema-v2 action-catalog design.

The predecessor canonical-LF SHA-256 is:

```text
6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4
```

The same path now holds the definitive design candidate. It prospectively
supersedes that immediate draft only after the author approves its exact hash.
Earlier prompt files remain historical evidence.

```text
predecessorPromptCanonicalLfSha256 = 6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4
definitivePromptCanonicalLfSha256 = 2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322
designEvidenceCanonicalLfSha256 = b22938b5fe56bfff07c807d268d3c6941bb6b4cfbb855b536e489aa878530739
```

No instruction in the definitive candidate authorizes its execution. A future
implementation must verify the approved hash and satisfy G9U0-R6 before any
productive mutation.

## 3. Authority consolidation and live-v1 preservation

Accepted [ADR 0012](../adr/0012-manifest-defined-geocedg-workspaces.md) remains
the architectural decision: one declarative manifest compiles the GeoCeDG
workspace. Its eleven broad professional action families remain authoritative.
The author's eighteen functional groups are reconciled as operational clusters
under those families, not as a replacement taxonomy.

The planning candidate introduces a strict
[schema-v2 candidate](../../geocedg/specs/ui/application-profile-v2.candidate.schema.json)
and one complete
[schema-v2 instance](../../geocedg/specs/ui/application-profile-v2.candidate.yml).
The instance declares 110 stable action records, eighteen clusters and their
mapping to the eleven accepted families. A future authorized implementation
must promote exactly one action catalog and make menus, toolbars, flyouts,
context actions, inspectors, help and unavailable reasons refer to its stable
IDs. It must not keep a second hard-coded menu or toolbar authority.

The current product authorities remain unchanged:

- `apps/geocedg/application-profile.yml` is still schema v1, Git blob
  `fc2a3ebd128fc79ca76840bc391598221bfa02c6`;
- `geocedg/specs/ui/application-profile.schema.json` is still schema v1, Git
  blob `a4e3718cf287e6a3d303a0a94489662fb8040ef5`; and
- neither live file is modified by this planning candidate.

The candidate therefore describes a future migration and compilation boundary;
it does not change observable workspace behavior.

## 4. Architectural classification

| Concern | Owning future layer | Authority boundary |
|---|---|---|
| semantic inverse address for interactive Point | separate G9U0-R6 shared-kernel gate | typed zero/one/many semantic-address result; no render/proximity identity |
| deterministic intersections, tokens and reactivation | existing R4/R5/G9S1 shared kernel | consumed unchanged by G9U1 |
| stroke hit, Algebra UI, markers, inspector, definition, zoom and accessibility | Desktop/frontend | presentation or explicit transaction only |
| action membership, grouping, defaults and unavailable policy | workspace/profile | single schema-v2 manifest authority |
| command discovery, syntax, help, GGBScript and EN/ES | existing host command/localization registries | no parallel parser, dictionary or script implementation |
| Continuity, workspace, language and view state | existing host setting/preference/document seams | presentation/product policy cannot alter geometry |
| Spline V2 × Spline V2 point identity | future separate kernel capability if ever authorized | rich-only in G9U1 |
| broader view history, fit/layer/named-view/precise-scale work | G12 | not pulled into G9U1 |

Frontend state never owns geometric evaluation, semantic addresses, metric
evidence, root solving, deterministic selectors, tokens, DAG identity or
reactivation.

## 5. Complete workspace reconciliation

The [workspace completeness matrix](g9u1_workspace_completeness_matrix.md)
contains the exact action-level review. It covers all eleven accepted broad
families through these eighteen operational clusters:

1. selection / move / inspect;
2. point construction;
3. linear geometry;
4. parameters and drivers;
5. relations and intersections;
6. circles / conics / curves;
7. Locus V2;
8. Spline V2;
9. metrics / validation;
10. similarity transformations;
11. authorized manual projection grouping;
12. presentation / visibility / style;
13. navigation / zoom;
14. document lifecycle;
15. automation / scripting;
16. authorized import / export;
17. help / command discovery; and
18. construction history / definition inspection.

The CeDG procedures/developments broad family remains visible only through a
truthful disabled-with-reason contract while G9U2 is blocked. The manual
projection cluster merely regroups existing ordinary actions; it adds no
projection semantics. Template-v7 macros, circle inversion, spatial/3D tools,
PDF/SVG sheet export, broad CAD import and uncertified spline-pair point
creation are explicitly deferred or outside scope rather than exposed as dead
buttons.

Four host-characterization items remain implementation entry checks: the
usefulness of the Desktop Select mode, the ordinary Properties/Scripting route,
the single frontend seam used to expose all SplineV2 forms, and whether the
inherited Image action is usable without unreviewed assets. Each has a
fail-closed disposition in the matrix.

The full menu/toolbar/command/help/GGBScript/localization/persistence review is
maintained in the
[command/tool consistency matrix](g9u1_command_tool_consistency_matrix.md), not
duplicated here.

## 6. Author-observed defects and selected seams

| Observation or usability requirement | Characterized cause/boundary | Definitive future contract |
|---|---|---|
| closed Locus V2 is selected well inside its enclosed region | `DrawLocusV2` currently lets the closed presentation path participate in area-like intersection hit testing | use the ordinary drawable selection-stroke seam; interior is negative, near-stroke is positive; zoom/DPI/style remain presentation only |
| ordinary Point cannot create/drag a semantic point on Locus V2, Spline V2 or transformed variants | forward `Point(L,branch,u)` exists, but no approved deterministic inverse semantic-address resolver exists | mandatory separate G9U0-R6; G9U1 consumes its typed result and never invents a preimage from pixels/proximity |
| rich intersections need discoverable multi-point workflow | R3 exposes the inspector, while candidate markers/multi-materialization are not yet implemented | active-result transient markers; create one/selected/all by current exact tokens; persistent session; coherent compound undo |
| historical `ZoomWindow` is absent from a complete Construction workspace | the host already has rectangle-selection/zoom infrastructure; broader navigation was scheduled in G12 | pull only bounded `ZoomWindow` plus ordinary current-seam zoom/pan/reset/show-all into G9U1; retain history/fit/layer/named views/precise scale in G12 |
| command hints/help/script parity is incomplete for the full semantic surface | action, command and help exposure are not yet reconciled through one registry | canonical host autocomplete/syntax/help and command processor; audit Algebra and GGBScript with identical gates |
| product language choice is too broad for current GeoCeDG-owned completeness | upstream locale corpus and product support policy are different concerns | product offers English/Spanish only, fallback English; do not delete corpus; Classic retains upstream choices |
| a complete Algebra expression can publish during preview and Enter then duplicates it | GeoCeDG command participation does not uniformly stop at the host nonproductive preview boundary; `CmdSplineV2` is a concrete audit target | preview creates zero geos/IDs/XML/undo/DAG state; one explicit Enter creates one transaction; Escape creates none |
| semantic object definition is hidden unless the whole Algebra view changes mode | existing global description mode is not sufficient object-level inspection | bounded read-only definition inspection through Algebra/Properties/context affordance; editing remains G9A-gated |
| Algebra description menu does not mark its active mode | the menu state is not bound to the existing Algebra-style authority | one host radio/check group reflects Value/Definition/Description immediately across rebuild and locale refresh; no second preference |
| free input `k=0.25` cannot update an intended existing numeric | current G9A rejects absent redefine context as `REDEFINE_CONTEXT_MISSING` | future UI may use a label only to locate the intended current object, then must use the G9A compatibility predicate and atomic transaction; label never becomes durable identity |
| opaque tokens previously caused oversized UI | R3 already corrected token/layout coupling | keep compact labels, accessible names, wrapped diagnostics and bounded dialogs as regression requirements |
| earlier product-menu rebuild could erase actions | R3 already established one population lifecycle | retain R3 menu lifecycle tests while replacing placement with the future single action catalog |

The detailed normative interaction contract is
[`g9u1-construction-interaction.md`](../../geocedg/specs/ui/g9u1-construction-interaction.md).

## 7. Mandatory proposed G9U0-R6 prerequisite

Interactive Point creation is not frontend-only. A click can select a
presentation stroke, but it cannot truthfully choose branch, component,
parameter, periodic preimage or transformed-source address. The current kernel
has an exact forward semantic point command but no approved inverse query.

The candidate therefore proposes:

```text
G9U0-R6 — SEMANTIC LOCUS POINT INTERACTION SUPPORT
status = PROPOSED MANDATORY KERNEL PREREQUISITE — NOT AUTHORIZED
```

R6 must return a bounded, deterministic, typed zero/one/many set of evidenced
semantic addresses. It must cover ambiguity, self-intersection, periodic seams,
spline knots, transformed loci and the R5 collapsed image without treating
screen coordinates, rendering, nearest distance, output order or movement
history as identity. Its tolerance, work budget and drag transition policy need
separate author-approved design and implementation.

The definitive G9U1 prompt stops before productive work unless R6 (or an
explicitly author-approved equivalent separate kernel gate) is
`PASS — AUTHOR APPROVED`. This planning task neither authorizes nor implements
R6.

## 8. Determinism and product/Classic boundary

CeDG Construction locks the existing host `Continuity` setting to `OFF` as a
product invariant. Product startup, prior preferences, workspace switching,
restart, `.cedg` load, compatibility `.ggb` load and restored UI state must not
reactivate it. The product settings UI must not permit enabling it. Existing
kernel/XML/preference fields remain the sole authority; no GeoCeDG-only
continuity field is introduced.

The separate GeoCeDG Classic diagnostic route retains upstream Continuity and
language configurability. It preserves supported `.cedg` objects without
gaining experimental creation authority, the Construction workspace or full
GeoCeDG branding. Product restrictions do not silently mutate Classic.

The same boundary applies to experimental command discovery, profile-specific
actions and preservation: loading an existing object is not authorization to
advertise or create it.

## 9. Rich intersection and materialization contract

The rich result remains non-Euclidian semantic authority. Candidate markers
are overlays for the active/selected result only and correspond only to current
locally admissible deterministic exact tokens. They have no `GeoElement`, XML,
DAG, Construction Protocol, undo node, semantic revision or identity of their
own. Proximity may select among already-authoritative markers; it cannot create,
continue or identify a root.

The future inspector remains open after materialization and supports:

- one selected exact-token point;
- several selected exact-token points; and
- all currently eligible exact-token points.

Already-materialized choices are identified without exposing opaque token text
as the UI label. A multi-create action is explicit, visible, atomic and one
coherent undo transaction. Cancel creates nothing. Kernel recompute may
reactivate an already-existing dormant point, but it never creates a new point.
Any initial auto-materialization preference is a separate explicit frontend
transaction; later topology changes only change markers and existing-point
definedness.

Spline V2 × Spline V2 remains **rich-result-only** under current G9S1 authority.
It has no symmetric certified unique pair selector, so G9U1 must offer neither
selectable candidate markers nor point materialization for those candidates.
This is a documented nonblocking limitation. Strengthening it requires a
separate kernel phase, never frontend proximity.

## 10. Algebra, commands, help and localization

Algebra preview is speculative and nonproductive. Parsing, autocomplete and
bounded syntax feedback may run, but no productive argument helper, output,
durable identity, token, XML state or undo entry may be published before
explicit commit. One Enter/action creates exactly one normal transaction;
Escape, replacement, failed commit and loss of focus leave Construction
unchanged.

Definition inspection is read-only and separately accessible for Locus V2,
Spline V2 and R5 transforms. It may show a reconstructible normalized command,
but must not make dependent semantic objects editable. Any actual redefine uses
only G9A compatibility and atomicity.

All deliberately exposed commands share the ordinary host command dictionary,
syntax registry, localized help and command processor across Algebra Input and
GGBScript. This includes LocusV2, SplineV2, Point semantic forms, Length/
LocusLength, Intersect and all seven R5 similarity forms including
Reflect/Mirror aliases. `--enableLocusV2=true` remains the only V2 opt-in;
there is no spline-, intersection- or transformation-specific flag.

English and Spanish must have complete names, tooltips, syntax, short help,
unavailable reasons and error messages, with deterministic English fallback.
Semantic serialization remains locale-independent. Classic keeps the pinned
upstream language surface.

## 11. Navigation reconciliation

G9U1 pulls forward only the view functionality necessary for a usable
Construction workspace:

- `ZoomWindow` through the existing rectangle/view seam, exposed in menu,
  toolbar and keyboard activation;
- ordinary current-seam pan, zoom in/out, standard view and show-all where
  the host characterization confirms them; and
- cancel, zero-area, focus and selection-reset behavior that mutates no
  Construction state.

Zoom remains frontend/view authority. Viewport, DPI, rectangle bounds and
cursor location never influence metrics, intersections, tokens, certificates
or semantic addresses.

`ZoomPrevious`, `FitSelection`, `FitLayer`, named views, precise scale and the
broader keyboard/view-history family remain G12. This avoids duplicating future
roadmap scope.

## 12. Documents, preferences, accessibility, errors and performance

The future workspace preserves the R2 document state machine: `.cedg` is native,
`.ggb` is compatibility input, normal Save never silently overwrites a source
`.ggb`, failed open is transactional, and shared ZIP/XML with `app="classic"`
remains unchanged. Workspace, layout, panel, language and ordinary view state
are preferences/presentation only and never alter the Construction.

The implementation must retain keyboard navigation, explicit Enter commit,
Escape cancellation, logical focus order, accessible names/tooltips,
non-color-only status, normal/high-DPI layout and reachable dialog controls.
Brand roles `geocedg.brand.topbar` and `geocedg.brand.startup` remain separate,
provenance-controlled resources. No substitute logo is generated, and Classic
remains visually distinct.

Errors are typed, localized and atomic. The product distinguishes invalid
input, unsupported operation, ambiguous semantic solution,
incomplete/not-established evidence and internal failure where bounded useful
diagnostics exist. It does not expose raw token strings or collapse every
condition into `Please check your input`.

Markers and inspector views reuse one current rich-result snapshot. They do not
solve per marker or per materialized point, replay trajectories or maintain
unbounded history. R4 selector lookup remains characterized around
`O(R log R + P)` for `R` roots and `P` existing bindings and must be reverified
at implementation time. View/profile/style/language changes trigger zero
semantic evaluations. R6 separately owns and instruments inverse-query budgets.

## 13. Validation authority and manual smoke

The public validation matrix now contains exactly **98 G9U1 scenarios**:

- 71 previously planned workspace/profile/R5/G9S1/determinism/localization/
  visual/intersection/brand/action scenarios; and
- 27 reconciled scenarios: `U1-C01`–`U1-C20`, `U1-Q01`–`U1-Q05` and
  `U1-P01`–`U1-P02`.

The machine inventory
[`g9u1-preexecution-scenarios.json`](../../geocedg/validation/g9u1/g9u1-preexecution-scenarios.json)
maps all 98 IDs. The future implementation must execute every public row and
every row of both supporting matrices, run its focused verifier twice with an
identical canonical summary, and preserve G9U0/R1/R2/R3/R4/R5/G9S1, G9A,
G9X1, G5–G8, Classic, legacy/scientific Locus, packaging and full composed
authority.

The future smoke in the command/tool matrix covers new/open/save `.cedg`, basic
2D construction, Locus V2 and Spline V2, semantic Point creation/drag,
intersections, markers, one/selected/all materialization, persistent inspector,
lengths, transforms, ZoomWindow, EN/ES help, GGBScript, preview/commit,
definition, redefine, undo/reopen, accessibility/branding and Classic. It is
author evidence only and cannot be self-approved.

The planning-only verifier
[`verify-g9u1-preexecution-design.ps1`](../../tools/agent/verify-g9u1-preexecution-design.ps1)
checks the design bundle. It is deliberately not inserted into the productive
composed verifier before author approval and does not run unimplemented product
scenarios. Two final planning runs pass with byte-identical logs, SHA-256
`65851350d533e264bd0d9583689d4c7c2db877a5a12662c1c3bb079f74d59ea3`:
17 exact paths, 11 broad families, 18 operational clusters, 110 stable actions,
98/98 scenario IDs, 16 canonical-LF authority hashes and 11 relative-link
documents. Both `git diff --check` and the empty-index cached check pass.

## 14. Periodic risk and retained limitations

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**. Existing
R4 evidence covers tri-state recompute, ledger export/import, copy, ordinary
dormant recurrence and native reopen, but not one dedicated native `.cedg`
round trip that persists the quarantine state itself. G9U1 validation must
attempt a real native archive with quarantine established through normal
product behavior, then prove unresolved preservation, unique-zero reactivation
and proved-nonzero retirement. It may close the risk only with that evidence;
otherwise explicit author disposition remains mandatory before global G9
closeout.

Other retained limits are:

- Spline V2 × Spline V2 point materialization remains rich-only;
- G9U0-R6 is proposed but neither designed to PASS nor authorized;
- the author-provided branding assets and final palette require provenance and
  visual review; and
- several inherited action seams must be confirmed at implementation entry and
  omitted fail-closed if unavailable.

## 15. Open author decisions

The following choices intentionally remain for author review before productive
G9U1 authorization:

1. whether to approve G9U0-R6 as the mandatory separate kernel phase, including
   its inverse-query tolerance, work budget and semantic drag-transition policy;
2. the exact initial normal/floating state of Properties;
3. the final visual palette/accent and acceptance of the two supplied brand
   roles after asset provenance review;
4. the final product wording for locked Continuity and inverse-address
   ambiguity;
5. whether unsupported upstream languages are hidden or displayed unavailable
   with reason (only English and Spanish are supported); and
6. the final disposition of host-characterization items such as Select mode,
   the Properties/Scripting route, SplineV2 action focus and inherited Image
   asset suitability.

Approval of this planning candidate must remain separate from authorization to
execute its prompt.

## 16. Exact planning artifact boundary

The definitive Stage-B candidate is confined to these planning, specification,
validation and operational files:

1. `.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md`;
2. `docs/architecture/cedg_workspace_architecture.md`;
3. `docs/roadmap/geocedg_roadmap.md`;
4. `docs/validation/g9_documentation_bundle_traceability.md`;
5. `docs/validation/g9_public_workspace_validation_matrix.md`;
6. `docs/validation/g9u1_command_tool_consistency_matrix.md`;
7. `docs/validation/g9u1_workspace_completeness_matrix.md`;
8. `docs/validation/g9u1_preexecution_design_candidate_report.md`;
9. `geocedg/specs/README.md`;
10. `geocedg/specs/ui/cedg-workspaces.md`;
11. `geocedg/specs/ui/application-profile-v2.candidate.schema.json`;
12. `geocedg/specs/ui/application-profile-v2.candidate.yml`;
13. `geocedg/specs/ui/g9u1-construction-interaction.md`;
14. `geocedg/validation/g9u1/g9u1-preexecution-scenarios.json`;
15. `geocedg/validation/g9u1/g9u1-preexecution-design-evidence.json`;
16. `geocedg/validation/g9u1/g9u1-preexecution-design-evidence.sha256`; and
17. `tools/agent/verify-g9u1-preexecution-design.ps1`.

There are no paths under `source/`, `source/desktop/`, `packaging/` or the live
profile/schema in this boundary. No Java, Desktop, command, XML, persistence,
renderer, packaging or product behavior has been changed. No generated logs or
artifacts belong in the tracked candidate.

## 17. Terminal declaration

```text
G9S1 = PASS — AUTHOR APPROVED
G9S1 machine status = PASS_AUTHOR_APPROVED

G9U0-R6 = PROPOSED MANDATORY KERNEL PREREQUISITE — NOT AUTHORIZED

G9U1 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
G9U1 machine status = DESIGN_CANDIDATE_PENDING_AUTHOR_REVIEW
implementationStarted = false
implementationAuthorized = false
selfApproved = false
authorApproved = false
passClaimed = false

G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

This planning candidate does not execute G9U1, does not authorize G9U0-R6 and
cannot be promoted to implementation authority without explicit author review
of the definitive prompt hash and the open decisions above.

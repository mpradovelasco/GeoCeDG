# G9U1 post-R6 approved design report

- Status: **DESIGN PASS — AUTHOR APPROVED — POST-R6 RECONCILED**
- Phase: G9U1 — CeDG Construction workspace
- Productive implementation: **NOT STARTED / NOT AUTHORIZED**
- Planning branch: `feature/g9u1-construction-workspace-planning-after-r6`
- Published predecessors: G9S1 `PASS — AUTHOR APPROVED` at
  `de33f3a80102adb051aaa7547a72b7e97409c58c`, followed by G9U0-R6
  `PASS — AUTHOR APPROVED` at
  `3942af594e4507e479f2c75019cef62e3d9fea6f`
- Published R6 tag: `geocedg-g9u0-r6-pass`, annotated object
  `2ec953c5e32203b3fc5e8ab3ad48e6e2e698239e`, peeling to the R6 commit
- Protected pre-R6 design checkpoint:
  `857de6628489bda0b65a5ba5145e62ca0795fc32`, preserved unchanged
- Retained risk: `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED`

The author approved this exact reconciled design as `G9U1 DESIGN = PASS —
AUTHOR APPROVED`. That decision protects planning authority only:
`implementationStarted = false`, `implementationAuthorized = false`,
`selfApproved = false`, `authorApprovedDesign = true` and
`passClaimedImplementation = false`.

## 1. Post-R6 entry authority

This successor reconciliation began only after G9S1 and G9U0-R6 had been
closed, published and independently verified from clean `main`. The G9S1
closeout is recorded in the
[G9S1 report](g9s1_semantic_spline_2d_capability_candidate_report.md) and the
annotated `geocedg-g9s1-pass` tag peels to
`de33f3a80102adb051aaa7547a72b7e97409c58c`.

R6 is the published shared-kernel semantic-point interaction authority. Its
annotated `geocedg-g9u0-r6-pass` tag peels to
`3942af594e4507e479f2c75019cef62e3d9fea6f`. The author accepted the kernel/
test-host diagnostic surface; `manualGuiSmoke = DEFERRED TO G9U1 BY DESIGN`
and `kernelDiagnosticAcceptance = PASS`. The protected pre-R6 planning branch
and checkpoint remain historical evidence and are not rewritten or merged into
R6.

Post-promotion verification from that clean commit established:

- G9S1 focused authority: **37/37 PASS**;
- G9U0-R5 focused authority: **46/46 PASS**;
- G9U0-R4 focused authority: **58/58 PASS**;
- R6 focused authority: **55/55 PASS** in both deterministic runs, canonical
  summary SHA-256
  `7aaed6a558bf6f86ec93a5b45eb74155d45e66b52b47c373a9ad32f43b156cc9`;
- full post-R6 `tools/agent/verify.ps1`: exit 0 with
  `All GeoCeDG verification gates passed.`; and
- `HEAD = main = origin/main = direct remote origin/main` at the published R6
  commit before the successor planning branch was created.

The final accepted metric smoke used the actual generator branch key
`generator.main`; Spline V2 total/partial values were `4`/`2`, and the ordinary
Locus V2 partial value was `2`. The formerly suggested
`scalar-locus/main` key was an erroneous smoke instruction, not a product
defect.

## 2. Protected predecessor and post-R6 canonical candidate

The historical file
[`g9u1-construction-workspace-after-g9s1.prompt.md`](../../.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md)
remains the post-G9S1 authority on `main`. The protected pre-R6 checkpoint then
prepared a complete 17-path design candidate but necessarily described R6 as a
provisional prerequisite. That checkpoint remains immutable at
`857de6628489bda0b65a5ba5145e62ca0795fc32`.

The historical post-G9S1 entry-prompt canonical-LF SHA-256 is:

```text
6451f15d5e0ecb9cadf8e17160a41606b5c8c27924455d1ee08326cad9b74fb4
```

The post-R6 successor is instead
[`g9u1-construction-workspace-after-g9u0-r6.prompt.md`](../../.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r6.prompt.md).
It consumes the actual published R6 contract and, following the author's design
approval, prospectively supersedes the protected candidate. Every earlier
prompt and the protected checkpoint remain historical evidence.

```text
protectedPreR6PromptCanonicalLfSha256 = 2319df211f5ea17880b7041844122afca0f2ddced4c6db1fabddce0d53dfa322
definitivePromptCanonicalLfSha256 = 561546019efc1e1d5e4367ddde73e9a2b0a0d767343eb9348b46d9e9c06f12df
designEvidenceCanonicalLfSha256 = 461a28b6388f8778a4a9b71806a431ea0172febdca7aed1d7086d2ca6d2d90d4
```

No instruction in the definitive prompt authorizes its execution. A future
implementation must verify the approved hash and both published prerequisite
tags before any productive mutation.

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
| semantic inverse address for interactive Point | published G9U0-R6 shared kernel | `LocusPointInteractionQuery2D` -> `LocusPointInteractionResolver2D.resolve(...)` -> typed result/candidates; no render/proximity identity |
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
| ordinary Point cannot yet create/drag a semantic point on Locus V2, Spline V2 or transformed variants | R6 now supplies the approved deterministic inverse resolver and public create/move operations, but deliberately has no productive Desktop consumer | G9U1 adds frontend stroke hit, typed result handling, ambiguity chooser and drag orchestration only; it never invents a preimage from pixels/proximity |
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

## 7. Published R6 contract consumed by G9U1

R6 has closed the kernel prerequisite. G9U1 must consume these actual shared-
kernel seams and must not preserve provisional or imaginary API names:

```text
LocusPointInteractionQuery2D(
    GeoLocusV2 source,
    double targetX,
    double targetY,
    LocusPointInteractionPolicy2D policy)

LocusPointInteractionQuery2D(...,
    LocusSemanticAddress2D currentAddress)

new LocusPointInteractionResolver2D().resolve(query)
    -> LocusPointInteractionResult2D

LocusV2PublicOperations.createInteractiveSemanticPoint(
    Construction, String, GeoLocusV2,
    LocusPointInteractionCandidate2D)

LocusV2PublicOperations.moveInteractiveSemanticPoint(
    GeoPoint, double, double,
    LocusPointInteractionPolicy2D)
```

The result statuses are
`NO_ADMISSIBLE_PREIMAGE`, `UNIQUE_ADMISSIBLE_PREIMAGE`,
`MULTIPLE_SEMANTIC_PREIMAGES`, `UNRESOLVED_NUMERICAL_SEARCH`,
`INVALID_SOURCE`, `DEGENERATE_SOURCE_IMAGE`, and
`UNSUPPORTED_CAPABILITY`. Creation is gated by the typed result; the frontend
cannot manufacture a candidate or substitute an index/coordinate.

The future Point-tool flow is therefore exact:

```text
frontend stroke hit on one semantic curve
 -> world/geometric interaction target
 -> R6 query and resolver
 -> UNIQUE: create through the R6 public construction seam
 -> MULTIPLE: deterministic chooser, then create the selected R6 candidate
 -> every other status: create nothing and show truthful localized feedback
```

Dragging an existing interaction-owned point invokes the R6 move operation.
The same `GeoPoint`, durable ID, source, branch/component and normal DAG parent
remain; ambiguity or unresolved evidence fails closed without allocating a
replacement or silently retargeting.

R6 proves closed-Spline seam movement approximately `u=0.98 -> 0.02` with
intrinsic `periodicLift=1`, reverse crossing, no duplicate seam candidate and
direct/incremental path independence. The exact persisted IEEE-754 semantic
direction bits remain authority; G9U1 must not reconstruct and recanonicalize
`canonical + lift * period`. It presents ordinary seam crossing without making
`periodicLift` routine UI jargon.

R6 also supplies transformed-source resolution. Point creation/drag applies to
invertible R5 Translate, Rotate, Reflect/Mirror, positive dilation and negative
dilation outputs. At `k=0 COLLAPSED_IMAGE`, a new click receives the typed
degenerate/ambiguous outcome and creates no arbitrary point. An existing
interaction-owned point retains its exact semantic direction and recovers as
the same point when the transform becomes nonzero again.

GGBScript remains deliberately distinct from pointer interaction. It uses the
exact command form such as `Point(L,"branch",u)` when the semantic address is
known; no synthetic mouse-position script API is required. R4 intersection
tokens and R6 Point-on-Locus candidates are likewise separate authorities.

The post-R6 completeness audit finds no further shared-kernel prerequisite for
the intended G9U1 Point workflow. All remaining work is frontend/profile/help/
localization orchestration over R6. This statement does not broaden the current
SplineV2 × SplineV2 rich-only limitation.

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

The Point toolbar interaction is not a new command grammar. Mouse creation and
drag consume R6 typed candidates, while Algebra/GGBScript retain exact semantic
forms such as `Point(L,"branch",u)`. Help and the command/tool matrix must make
that boundary explicit: scripting neither accepts pointer coordinates nor
invokes the ambiguity chooser.

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
input, unsupported operation, multiple semantic preimages, unresolved
numerical search, degenerate source image, incomplete/not-established evidence
and internal failure where bounded useful diagnostics exist. English and
Spanish Point-tool help, chooser text and fail-closed feedback consume the R6
status meanings without exposing implementation objects. It does not expose
raw token strings or collapse every condition into `Please check your input`.

Markers and inspector views reuse one current rich-result snapshot. They do not
solve per marker or per materialized point, replay trajectories or maintain
unbounded history. R4 selector lookup remains characterized around
`O(R log R + P)` for `R` roots and `P` existing bindings and must be reverified
at implementation time. View/profile/style/language changes trigger zero
semantic evaluations. R6 separately owns and instruments inverse-query budgets.

## 13. Validation authority and manual smoke

The post-R6 public validation matrix contains exactly **118 G9U1 scenarios**:

- the protected 98 workspace/profile/R5/G9S1/determinism/localization/visual/
  intersection/brand/action/command/risk/performance scenarios; and
- twenty post-R6 Point interaction scenarios `U1-PNT-01`–`U1-PNT-20` covering
  straight Locus V2, Spline V2, stroke/interior hit boundaries, unique create,
  deterministic chooser/cancel, drag, periodic seam, transformed sources,
  negative and zero dilation, persistence, undo/redo, copy/remap, DPI/zoom
  independence and proof that no frontend inverse fallback exists.

The machine inventory
[`g9u1-preexecution-scenarios.json`](../../geocedg/validation/g9u1/g9u1-preexecution-scenarios.json)
maps all 118 IDs. The future implementation must execute every public row and
every row of both supporting matrices, run its focused verifier twice with an
identical canonical summary, and preserve G9U0/R1/R2/R3/R4/R5/G9S1, G9A,
G9X1, G5–G8, Classic, legacy/scientific Locus, packaging and full composed
authority.

The future smoke in the command/tool matrix is also the first productive GUI
acceptance of R6. It covers Point-tool click/create/drag on Locus V2 and
Spline V2, periodic seam crossing, self-intersection chooser and cancel,
transformed source, negative dilation, `k=0`, recovery, save/reopen and undo,
plus the pre-R6 workspace/intersection/materialization/navigation/language/
accessibility/Classic checks. It is author evidence only and cannot be
self-approved.

The planning-only verifier
[`verify-g9u1-preexecution-design.ps1`](../../tools/agent/verify-g9u1-preexecution-design.ps1)
checks the design bundle. It is deliberately not inserted into the productive
composed verifier because it is planning-only and does not run unimplemented product
scenarios. The final post-R6 planning verifier must pass twice with identical
canonical output: 18 exact planning paths, 11 broad families, 18 operational
clusters, 110 stable actions, 118/118 scenario IDs, exact canonical-LF
authority hashes and valid relative links. Both `git diff --check` and the
empty-index cached check must pass.

The author-approval closeout reran that verifier as A/B. Both runs passed with
identical raw summary-log SHA-256
`4a3c37c27bc1b64c3e555d0b38baf680a5fa4ccd45c9776de8603ae53f9284e2`.

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
- the author-provided branding assets and final palette require provenance and
  visual review; and
- several inherited action seams must be confirmed at implementation entry and
  omitted fail-closed if unavailable.

## 15. Deferred implementation-entry decisions

The author approved the complete post-R6 design while intentionally deferring
these implementation-entry and final visual-review choices. None authorizes
productive G9U1 execution:

1. the exact initial normal/floating state of Properties;
2. the final visual palette/accent and acceptance of the two supplied brand
   roles after asset provenance review;
3. the final product wording for locked Continuity and R6 ambiguity/
   unresolved/degenerate feedback;
4. whether unsupported upstream languages are hidden or displayed unavailable
   with reason (only English and Spanish are supported); and
5. the final disposition of host-characterization items such as Select mode,
   the Properties/Scripting route, SplineV2 action focus and inherited Image
   asset suitability.

Design approval remains separate from authorization to execute its prompt.

## 16. Exact planning artifact boundary

The definitive Stage-B candidate is confined to these planning, specification,
validation and operational files. The first path is the byte-exact protected
prompt carried forward from checkpoint `857de662...`; it is historical authority,
not a rewritten post-R6 contract:

1. `.github/prompts/tasks/g9u1-construction-workspace-after-g9s1.prompt.md`;
2. `.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r6.prompt.md`;
3. `docs/architecture/cedg_workspace_architecture.md`;
4. `docs/roadmap/geocedg_roadmap.md`;
5. `docs/validation/g9_documentation_bundle_traceability.md`;
6. `docs/validation/g9_public_workspace_validation_matrix.md`;
7. `docs/validation/g9u1_command_tool_consistency_matrix.md`;
8. `docs/validation/g9u1_workspace_completeness_matrix.md`;
9. `docs/validation/g9u1_preexecution_design_candidate_report.md`;
10. `geocedg/specs/README.md`;
11. `geocedg/specs/ui/cedg-workspaces.md`;
12. `geocedg/specs/ui/application-profile-v2.candidate.schema.json`;
13. `geocedg/specs/ui/application-profile-v2.candidate.yml`;
14. `geocedg/specs/ui/g9u1-construction-interaction.md`;
15. `geocedg/validation/g9u1/g9u1-preexecution-scenarios.json`;
16. `geocedg/validation/g9u1/g9u1-preexecution-design-evidence.json`;
17. `geocedg/validation/g9u1/g9u1-preexecution-design-evidence.sha256`; and
18. `tools/agent/verify-g9u1-preexecution-design.ps1`.

There are no paths under `source/`, `source/desktop/`, `packaging/` or the live
profile/schema in this boundary. No Java, Desktop, command, XML, persistence,
renderer, packaging or product behavior has been changed. No generated logs or
artifacts belong in the tracked candidate.

## 17. Terminal declaration

```text
G9S1 = PASS — AUTHOR APPROVED
G9S1 machine status = PASS_AUTHOR_APPROVED

G9U0-R6 = PASS — AUTHOR APPROVED
G9U0-R6 commit = 3942af594e4507e479f2c75019cef62e3d9fea6f
G9U0-R6 tag = geocedg-g9u0-r6-pass

G9U1 DESIGN = PASS — AUTHOR APPROVED
G9U1 design reconciliation = POST-R6 RECONCILED
G9U1 machine status = DESIGN_PASS_AUTHOR_APPROVED
G9U1 IMPLEMENTATION = NOT AUTHORIZED / NOT STARTED
implementationStarted = false
implementationAuthorized = false
selfApproved = false
authorApprovedDesign = true
passClaimedImplementation = false

G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

This author-approved design does not execute or authorize G9U1 and cannot be
promoted to implementation authority without separate explicit author
authorization. The complete gap audit found no further kernel prerequisite for
the intended Point workflow. A separate verification-performance/governance
task may run from clean published `main` without implying G9U1 authorization.

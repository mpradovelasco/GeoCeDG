# G9U1 frontend review — menu, toolbar and editing affordances

- Status: **FRONTEND REVIEW DESIGN / IMPLEMENTATION CANDIDATE**; not author PASS.
- Reviewed baseline: `b492194082f1adc9f981d85d92a58ef57490196f`.
- Authority: author review round 1; [workspace contract](../../geocedg/specs/ui/cedg-workspaces.md),
  [workspace architecture](../architecture/cedg_workspace_architecture.md),
  [ADR 0012](../adr/0012-manifest-defined-geocedg-workspaces.md).
- Layer: Desktop application/presentation only. No new geometric, identity,
  compatible-redefine or document-schema authority.

## Before / after design, recorded before productive changes

| Concern | Baseline source finding | Bounded successor design | Preserved invariant |
|---|---|---|---|
| Primary menu | `GeoCeDGMenuBar` already extends host `GeoGebraMenuBar` / Swing `JMenuBar`, with five manifest sections. A second, much more prominent eleven-family button strip duplicates that discovery route below the native toolbar. | Keep the normal application menu as the broad route; remove the duplicate family-button strip. Expose ordinary File/Edit/View organization with manifest references and existing host dispatch. | One schema-v2 registry; no parallel menu engine or native command catalog. |
| Toolbar | `GeoCeDGProfile.compileActionToolbar` puts all toolbar **and overflow** mode actions into native flyouts (66 modes). Non-mode Spline/ZoomWindow actions depend on the family palette. | Compile only curated `toolbar_action_ids`; keep lower-frequency overflow actions in menus. Add only compact non-mode registry controls/flyouts needed by the declared toolbar placements. | Every toolbar action has a discoverable menu and help route; menu population is greater than or equal to toolbar population. |
| Taxonomy | Eleven families, eighteen clusters, 110 stable actions. | Retain all three counts; placement is a presentation refinement. Dynamic user tools are not new stable action IDs. | No silent scope reduction or duplicate catalog. |
| Templatev7 | Curation records 24 macros and the historical seven custom groups; locus list workarounds, procedure and presentation tools are mixed. | Use frequency/grouping evidence only; prefer native LocusV2/SplineV2/Length. Low-frequency administrative/presentation tasks stay in menus. | No macro/icon copying or promotion of sampled metrics, spatial procedures or G9U2 semantics. |
| Ordinary Algebra edit | `AlgebraControllerD` calls the host `startEditItem` when the object's existing editability flag is true. | Retain ordinary editable behavior and the existing explicit-edit transaction. | G9A, not a frontend type guess, decides identity preservation. |
| Ordinary noneditable image/pen | The baseline intercepted every noneditable object, including ordinary image/pen objects, as a semantic definition. | Limit semantic read-only inspection to the three actual GeoCeDG semantic families. Ordinary special objects return to the unchanged host Properties route. | No ordinary Classic affordance is disabled and no kernel editability flag changes. |
| Semantic definition | Noneditable objects open `GeoCeDGDefinitionInspector` on double-click, but host `ObjectNameModel` hides their Properties definition row. | Show an explicitly read-only definition and explanation in Properties as well as the inspector. | Definition visibility is not edit permission; no model predicate or persistence meaning changes. |
| Context duplication | Host context supplies Properties; GeoCeDG repeats Properties and two definition action aliases in a product submenu. | Suppress the duplicated host Properties item and collapse the definition aliases to one context route; preserve rich-result inspection/materialization. | Same registry action instances, current selection and feature availability. |
| Public metric provenance | Scalar adapter can present a hidden rich-parent label rather than the user's operation. | Separate metric presentation investigation owns a public provenance adapter while preserving the actual parent DAG. | No fabricated dependency and no replacement numerical integration. |

The full Classic menu is deliberately not copied by blindly calling
`super.initMenubar()`: its unrestricted languages, diagnostic/product branding
and external routes are not the approved GeoCeDG profile. Ordinary host actions
are retained through the existing registry dispatch targets and native Swing
menu infrastructure; Classic's own menu class remains unchanged.

## Implemented presentation delta

The successor renders six normal application menus: **File, Edit,
Construction, View, Automation, Help** (complete EN/ES localization). Edit
reuses the existing selection/control cluster; no action or cluster was added.
View contains the manifest-derived Reapply workspace menu and exposes the
Document-layout condition through its accessible description. Automation
contains the dynamic User Tools submenu, with the same existing
`automation.manage-user-tools` action rather than a duplicate management entry.

The native toolbar now has **32 unique real modes**, organized by the existing
family flyouts, plus **two** shared-registry non-mode controls: SplineV2 and
ZoomWindow. Thus the product toolbar presents **34 stable actions**, while the
normal menu exposes **110**. The catalog still has **66 native-mode actions**;
the lower-frequency modes were not deleted. Delete and Show/Hide are deliberately
not permanent toolbar choices. The eleven-family button strip is removed.
Pinned user macros occupy a separate dynamic application-tool group and never
change those stable product counts.

### Fresh-start perspective chooser observation

The agent's real GUI launch with fresh settings still displayed the inherited
right-side **GeoGebra Classic** perspective chooser over Construction. The source
route is `GeoGebraFrame` startup → `AppD.showPopUps` → the deferred protected
`showPerspectivePopup` hook → `DockBar.showPopup` / `PerspectivePanel`. This was
not an author acceptance result.

The bounded correction overrides only `AppGeoCeDG.showPerspectivePopup`: the
already selected declarative Construction workspace no longer automatically
opens that Classic chooser. The inherited `AppD` callback and separately launched
Classic application remain unchanged. It does not disable dialogs generally or
change the construction/perspective serialization contract. The new focused test
`constructionStartupDoesNotOpenClassicChooserButClassicStillDoes` checks the
deferred product callback and an inherited Classic positive control. Its execution
and the corrected real GUI observation belong to the subsequent root validation,
not to the prior launch evidence.

Properties now displays the existing LocusV2/rich-result definition as
read-only with the same EN/ES explanation used by the definition inspector.
Synthetic Enter and focus-loss events are rejected at that read-only field.
Ordinary object fields and the G9A compatibility provider are unchanged.
The context menu retains one definition action, rich inspection and applicable
materialization; the duplicate product Properties route is removed because the
host object menu already supplies it.

## Algebra / Properties / redefine matrix

This matrix describes the current source contract, not a blanket permission to
edit semantic objects. “Host edit” means the existing `isAlgebraViewEditable`,
`isRedefineable`, protection and explicit-edit checks still apply. `changeGeoElement`
and the G9A compatibility provider remain the only identity decision authority.
An allowed edit is not a promise that arbitrary replacement preserves identity.

| Object family | Algebra double-click | Context / definition visibility | Properties | Editable redefine / compatible redefine | Identity effect | Undo/redo expectation |
|---|---|---|---|---|---|---|
| Ordinary free Classic object | Host in-place edit where changeable; host special editor for text/image families | Host context plus one optional read-only definition route | Existing value/style/label controls | Existing host explicit-edit seam; G9A predicate where registered | Compatible edit preserves approved identity; incompatible replacement uses existing new-identity policy | One successful edit follows host/G9A undo; inspection adds none |
| Ordinary dependent Classic object | Host redefine dialog where redefineable | Host context; readable defining expression | Existing editable definition and presentation | Existing explicit-edit/G9A transaction | Never infer sameness from retained label | Undo restores the prior dependency/identity graph |
| Independent parameter / slider | Host numeric edit / slider UI | Read current value and definition | Existing value, slider bounds and style | Existing numeric compatible redefine; slider `setValue` remains ordinary recompute | Same numeric identity for approved compatible edits; source dependents recompute | Existing numeric edits/transactions remain undoable |
| LocusV2 | Read-only definition inspector (`isAlgebraViewEditable=false`) | One definition route; semantic/rich inspection where applicable | Style/name/caption plus visible **read-only** definition in successor | Arbitrary direct redefine is not enabled; edit defining generator inputs instead | Source identity/domain remains kernel-owned | Inspection/style does not replace source; input edits use existing history |
| SplineV2 | Same GeoLocusV2 noneditable contract | Same definition route; source points/degree remain inspectable | Same read-only semantic definition and ordinary style | Direct compatible spline-definition editing needs a separately approved predicate; not invented here | Structural spline/source identity unchanged by inspection | Edit defining point coordinates through normal transactions |
| R5-transformed semantic curve | Read-only definition inspector | Show original transformation expression / input provenance | Read-only definition and style | Edit center/vector/angle/factor inputs using their existing contracts | New transformed-source identity remains distinct from original | Existing dynamic input undo/recovery, including `k=0` |
| Interaction-owned semantic point | Ordinary GeoPoint host editability is retained; drag uses R6 | Definition may be inspected without publishing an edit | Existing point/style controls; no frontend identity inference | Any explicit redefine is submitted to existing G9A provider; it is not the R6 drag operation | R6 drag preserves same point/address ownership; arbitrary replacement is **not** promised compatible | Drag uses existing compound history; redefine uses G9A history |
| Materialized exact-token point | Ordinary GeoPoint host edit path where permitted | Read defining token-parent relation; use rich inspector for solution evidence | Existing point/style controls | Existing explicit-edit transaction only; no new token-retarget shortcut | Recompute/dormancy/reactivation retain token-owned point; replacement must not masquerade as retarget | Existing token graph restored by kernel undo/reopen |
| Rich metric / intersection result | Read-only definition inspector (`isAlgebraViewEditable=false`) | Read-only definition plus typed rich inspector | Read-only definition and presentation where supported | No arbitrary direct redefine enabled | Rich status/evidence/token ownership remain kernel truth | Read-only views create no history entry |
| Scalar child of rich result | Ordinary GeoNumeric dependent host editability, subject to G9A | Public operation must be understandable; advanced view may disclose rich parent | Public definition presentation, existing value/style controls | Existing explicit-edit/G9A seam; no “edit integration result” semantics | Numerical value is recomputed from actual rich parent; arbitrary scalar replacement is not identity continuity | Existing approved transaction/adapter history |
| Visible spatial semantic object | Honor actual host editability/protection; no new object-family permissions | Existing typed read-only inspection if available | Existing supported presentation only | No new spatial compatible-redefine predicate or projection workflow | Spatial IDs/bindings/certificates remain authoritative | Existing supported history only; no G9B/G9C/G9U2 expansion |

### Exact source seams audited

- `GeoCeDGMenuBar.initMenubar/populateProductMenu/createItem`: real menu projection
  and shared `Action` instances.
- `GeoCeDGWorkspaceController.createActionPalette/createContextMenu` and
  `GeoCeDGToolbarContainer.buildGui`: duplicate palette and context placement.
- `GeoCeDGProfile.compileActionToolbar/validatePlacements`: toolbar compiler and
  full action/cluster reference checks.
- `AlgebraControllerD.checkDoubleClick`, `AlgebraViewD.startEditItem`:
  noneditable inspection versus ordinary host edit.
- `ObjectNameModel.updateProperties`, `NamePanelD.updateGUI/updateDefinition`,
  `RedefineInputHandler.processInput`: definition visibility, frontend field
  enablement and existing host explicit-edit transaction.
- `GeoCeDGAlgebraInputSubmission.submit`: explicit input label locates the current
  target; the compatibility predicate/transaction is not bypassed.
- `GeoElement.isAlgebraViewEditable/isRedefineable` and overrides in
  `GeoLocusV2`, `GeoLocusMetricResult`, `GeoLocusIntersectionResult`.
- `ConstructionGeoRedefineProvider`: public semantic output/role classification;
  inspected only, not changed by this review.

## Validation obligations and limitations

Focused tests must prove real application-menu attachment/rebuild, toolbar
subset and menu/help reachability, shared action identity/availability,
noneditable Properties/inspection purity and ordinary edit preservation.
Properties must not apply a read-only definition on Enter or focus loss. Existing
G9A tests remain authority for compatible versus incompatible identity effects.
No frontend test may manufacture a new compatibility rule.

Manual visual/DPI review and the author's re-smoke remain distinct from source
inspection and test-host evidence. This design record does not assert that a
manual review has passed. Native save/undo/reopen and the author's
`Length(b,A,C)` defect are investigated separately; this menu/editing review
does not sanitize or alter the supplied document.

The focused implementation checks are in `G9U1WorkspaceSurfaceTest`,
`G9U1ProfileCompilerTest`, and `G9U1DefinitionAffordanceTest`; execution results
belong to the final source-cohort evidence, not this before/after design table.
`git diff --check` completed successfully during development. No Gradle or
acceptance execution was performed by the frontend review subtask independently
of the root verifier process.

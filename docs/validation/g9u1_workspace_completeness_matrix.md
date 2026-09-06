# G9U1 CeDG Construction workspace completeness matrix

- Status: **DESIGN PASS — AUTHOR APPROVED — POST-R1 RECONCILED**
- Phase: G9U1 pre-execution scope reconciliation
- Product implementation: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Runtime manifest: `apps/geocedg/application-profile.yml`, live schema v2
- Historical design schema/instance:
  `geocedg/specs/ui/application-profile-v2.candidate.schema.json` and
  `geocedg/specs/ui/application-profile-v2.candidate.yml`
- Accepted architectural authority:
  `docs/adr/0012-manifest-defined-geocedg-workspaces.md`
- Published semantic Point interaction authority: G9U0-R6
  `PASS — AUTHOR APPROVED`, commit
  `3942af594e4507e479f2c75019cef62e3d9fea6f`, tag
  `geocedg-g9u0-r6-pass`

## 1. Purpose and authority boundary

This matrix reconciles the accepted eleven broad professional workspace
families with the author's eighteen operational clusters. The families remain
the stable conceptual and toolbar taxonomy. The clusters define discoverability
across toolbar flyouts, overflow, menus, context actions, inspectors, settings,
and view chrome.

The candidate manifest contains 110 stable action declarations and eighteen
cluster declarations. An action is declared exactly once; a cluster may
reference the same action ID on another surface. Reuse is intentional. It must
not compile to duplicated toolbar modes or a second action implementation.

The live candidate implements this matrix through the same profile authority.
The final presentation-polish delta changes only its toolbar/name/resource
projection; command dispatch, geometry and persistence remain unchanged.

The protected pre-R6 design checkpoint
`857de6628489bda0b65a5ba5145e62ca0795fc32` remains immutable. This post-R6
matrix consumes the actual published query/resolver/candidate/create/move API;
it does not preserve the earlier provisional R6 model. The complete gap audit
finds no further shared-kernel prerequisite for the intended Point workflow.

## 2. Eleven broad families and eighteen operational clusters

| Order | Operational cluster | Accepted broad family | Boundary |
|---:|---|---|---|
| 1 | Selection / move / inspect | Inspect and construct | normal selection/edit control |
| 2 | Point construction | Inspect and construct | ordinary point tools gain the approved semantic-curve selection contract |
| 3 | Linear geometry | Linear geometry | inherited 2D primitives |
| 4 | Parameters and drivers | Parameters and drivers | inherited parameters plus explicit driver interaction |
| 5 | Relations and intersections | Relations and intersections | inherited relations plus rich-result presentation/materialization |
| 6 | Circles / conics / curves | Circles, conics and curves | inherited supported 2D families |
| 7 | Locus V2 | Locus V2 and semantic Spline V2 | feature-gated Locus V2 actions |
| 8 | SplineV2 | Locus V2 and semantic Spline V2 | feature-gated post-G9S1 action |
| 9 | Metrics / validation | Metrics and validation | inherited measurements plus rich semantic lengths |
| 10 | Similarity transformations | Transformations and manual projections | ordinary transform actions using R5 kernel authority |
| 11 | Authorized manual projection | Transformations and manual projections | task/menu regrouping of existing ordinary actions only |
| 12 | Presentation / visibility / style | Presentation and document | presentation only; never geometric authority |
| 13 | Navigation / zoom | Presentation and document | viewport only |
| 14 | Document lifecycle | Presentation and document | approved R2 native/compatibility policy |
| 15 | Automation / scripting | Automation and import/export | host input/tools plus controlled diagnostic route |
| 16 | Authorized import / export | Automation and import/export | `.ggb` compatibility open plus approved DXF |
| 17 | Help / command discovery | Presentation and document | EN/ES product discovery and Classic diagnostic route |
| 18 | Construction history / definition | Inspect and construct | view-only definition/protocol access |

The ninth broad family, **CeDG procedures and developments**, remains present
but disabled with a truthful G9U2 reason. Cluster 11 may reference ordinary
line, projection-helper, metric, and transform actions already defined in other
clusters. It does not implement projection semantics. The future Dihedral
Procedures workspace switch is disabled; it is not a dead executable button.

## 3. Availability and placement vocabulary

### 3.1 Availability profiles

| Profile | GeoCeDG | Feature-off policy | Classic | External upstream |
|---|---|---|---|---|
| `host-inherited` | available | not applicable | inherited | inherited |
| `host-contextual` | selection-dependent | hidden when inapplicable | inherited | inherited |
| `locus-v2-product` | requires `cedg.locus.v2` | hidden | creation hidden; preservation unchanged | unsupported native types |
| `locus-v2-contextual` | requires feature plus current admissible selection/result | hidden | hidden | unsupported |
| `product-only` | available | not applicable | hidden | unsupported |
| `product-contextual` | current-selection dependent | hidden | hidden | unsupported |
| `document-product` | approved `.cedg` native / `.ggb` compatibility contract | fail closed | Classic preserves `.cedg` but retains its own new-document identity | upstream boundary unchanged |
| `dxf-product` | requires `cedg.export.dxf.2d` | hidden | hidden | unsupported |
| `diagnostic-route` | explicit external/isolated route | unavailable when prerequisites fail | diagnostic only | unsupported |
| `gated-g9u2` | disabled with localized reason | disabled | hidden | unsupported |

`cedg.locus.v2` is the only semantic-curve public opt-in. No SplineV2,
intersection, transformation, marker, or materialization runtime flag is added.

### 3.2 Surface boundaries

- **Toolbar:** frequent constructive modes only, grouped by the eleven broad
  families.
- **Overflow/flyout:** less frequent but supported constructive modes.
- **Menu:** document, help, navigation, grouped manual procedures, and other
  discoverability actions.
- **Context:** current-object/current-result actions only.
- **Inspector:** exact-token materialization actions.
- **Settings/view chrome:** preferences and presentation state only.
- **Deferred:** no active action declaration unless a truthful
  `disabled-with-reason` or `diagnostic-route-only` contract exists.

## 4. Complete stable action inventory

The exact action fields, symbolic targets, audited numeric mode IDs, selection
contract references, effect profiles, and command-surface profiles live in the
candidate manifest. This table is the human-review completeness and gap
classification.

### 4.1 Selection, move, and inspect

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `construction.move` | `MODE_MOVE(0)` | toolbar | MUST | existing |
| `construction.move-rotate` | `MODE_MOVE_ROTATE(39)` | toolbar | SHOULD | existing; grouped with Move |
| `construction.select` | `MODE_SELECT(77)` | overflow | SHOULD | characterize supported Desktop behavior; otherwise remove visibly |
| `construction.attach-detach` | `MODE_ATTACH_DETACH(67)` | toolbar | SHOULD | existing; grouped with Point/intersection tools |
| `edit.delete` | `MODE_DELETE(6)` | toolbar/menu | MUST | normal undo |
| `edit.undo` | `host.edit.undo` | toolbar/menu | MUST | host history |
| `edit.redo` | `host.edit.redo` | toolbar/menu | MUST | host history |

### 4.2 Point construction

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `construction.point` | `MODE_POINT(1)` | toolbar | MUST | frontend stroke hit -> geometric target -> published R6 typed inverse resolver -> unique create or explicit chooser |
| `construction.point-on-object` | `MODE_POINT_ON_OBJECT(501)` | toolbar | MUST | same R6 semantic-curve contract; no generic `Path` conformance or proximity fallback |

Both actions retain ordinary Classic operands. GeoCeDG semantic-curve placement
requires the approved feature. `LocusPointInteractionQuery2D` and
`LocusPointInteractionResolver2D.resolve(...)` return typed none/unique/
multiple/unresolved/invalid/degenerate/unsupported outcomes. Unique creation
uses `LocusV2PublicOperations.createInteractiveSemanticPoint(...)`; multiple
semantic preimages require a deterministic accessible ambiguity chooser; every other
non-unique status creates nothing with truthful EN/ES feedback. Dragging calls
`LocusV2PublicOperations.moveInteractiveSemanticPoint(...)`, retains the same
point/ID/source and exact branch/component/address, and fails closed without a
replacement or Cartesian retargeting.

The ordinary Point action already exists in the 110-action catalog. R6
integration changes its future selection behavior; it does not add another
Point action or a second interaction-point model. The exact command and
GGBScript form remains `Point(L,"branch",u)` (or its current equivalent exact
address grammar) and never accepts synthetic pointer coordinates.

The future chooser presents branch/component, semantic location, local span or
orientation and a transient graphical highlight where useful, but passes the
selected R6 candidate unchanged. Display order is not identity; Cancel creates
nothing. Closed-curve interior is not a hit, while a stroke hit merely decides
whether to query R6. Zoom, DPI and stroke thickness never enter the semantic
address.

The same two actions cover general/scalar/point-driven/periodic Locus V2,
Spline V2 and invertible R5 transforms. Closed-Spline seam drag must retain the
same point and exact persisted semantic direction bits in both directions,
without duplicate seam candidates. At negative dilation the transformed source
remains ordinary; at `k=0 COLLAPSED_IMAGE` a new click creates nothing while an
existing interaction-owned point retains its semantic direction and recovers
as the same point when nonzero geometry returns. Save/reopen, undo/redo and
supported copy/remap preserve the exact R6 source/address graph.

### 4.3 Linear geometry

| Stable action IDs | Exact targets | Placement | Disposition |
|---|---|---|---|
| `construction.line`, `construction.segment`, `construction.ray`, `construction.vector`, `construction.fixed-segment`, `construction.vector-from-point` | existing modes `2`, `15`, `18`, `7`, `45`, `37` | toolbar: Lines and vectors | MUST |
| `construction.polygon`, `construction.polyline`, `construction.regular-polygon`, `construction.rigid-polygon`, `construction.vector-polygon` | existing modes `16`, `65`, `51`, `64`, `70` | toolbar: Polygons | MUST |
| `construction.parallel-line`, `construction.perpendicular-line`, `construction.midpoint`, `construction.perpendicular-bisector`, `construction.angle-bisector` | existing modes `3`, `4`, `19`, `8`, `9` | toolbar: Derived constructions | MUST |

All are inherited host semantics. Workspace membership changes discovery only.

### 4.4 Parameters and drivers

| Stable action ID | Target | Placement | Disposition |
|---|---|---|---|
| `parameter.slider` | `MODE_SLIDER(25)` | toolbar | MUST |
| `parameter.fixed-angle` | `MODE_ANGLE_FIXED(46)` | toolbar | SHOULD |
| `parameter.checkbox` | `MODE_SHOW_HIDE_CHECKBOX(52)` | toolbar | SHOULD |
| `parameter.button` | `MODE_BUTTON_ACTION(60)` | toolbar | SHOULD |
| `parameter.input-box` | `MODE_TEXTFIELD_ACTION(61)` | toolbar | SHOULD |
| `parameter.animation-toggle` | existing object context animation | context | SHOULD |

The Algebra Input preview/commit and G9A-compatible redefine contracts are
cross-cutting requirements below; they are not duplicate construction actions.

### 4.5 Relations and intersections

| Stable action ID | Target | Placement | Disposition | Boundary |
|---|---|---|---|---|
| `relation.intersect` | `MODE_INTERSECT(5)` / ordinary `Intersect` dispatch | toolbar | MUST | rich result remains kernel authority |
| `relation.tangent` | `MODE_TANGENTS(13)` | toolbar | MUST | inherited |
| `relation.compare` | `MODE_RELATION(14)` | overflow | MUST | read-only relation |
| `relation.polar-diameter` | `MODE_POLAR_DIAMETER(44)` | overflow | SHOULD | inherited |
| `result.inspect-rich` | current R3 inspector route | menu/context | MUST | feature/current-result dependent |
| `result.show-candidate-markers` | new presentation overlay preference | menu/settings | MUST | active result, current deterministic tokens only |
| `result.materialize-selected` | exact selected token | inspector/context | MUST | one ordinary point, one undo |
| `result.materialize-multiple` | selected exact tokens | inspector | MUST | one explicit compound undo |
| `result.materialize-all-eligible` | all current eligible exact tokens | inspector | MUST | one explicit compound undo |
| `result.auto-materialize-initial` | explicit frontend policy | settings | MUST | initial visible/undoable action only; no recompute creation |

SplineV2×SplineV2 consumes R1's certified singleton transverse-germ subset:
existing intersection/materialization actions use individually admissible exact
tokens; same-germ collisions and other insufficiently certified cases stay rich-only.
Existing dormant point reactivation remains kernel/DAG recomputation and is
not new-point auto-materialization.

### 4.6 Circles, conics, and curves

| Stable action IDs | Exact targets | Placement | Disposition |
|---|---|---|---|
| `curve.circle-two-points`, `curve.circle-three-points`, `curve.circle-center-radius` | modes `10`, `11`, `34` | toolbar | MUST |
| `curve.arc-center`, `curve.conic-five-points`, `curve.ellipse`, `curve.parabola` | modes `20`, `12`, `55`, `57` | toolbar | MUST |
| `curve.compass`, `curve.semicircle`, `curve.arc-circumcircle` | modes `53`, `24`, `22` | overflow | SHOULD |
| `curve.sector-center`, `curve.sector-circumcircle`, `curve.hyperbola` | modes `21`, `23`, `56` | overflow | SHOULD |

Circle inversion (`MODE_MIRROR_AT_CIRCLE`) is deliberately absent; it is not an
R5 similarity transformation.

### 4.7 Locus V2

| Stable action ID | Target | Placement | Disposition | Boundary |
|---|---|---|---|---|
| `semantic.locus-v2.create` | `MODE_LOCUS_V2(133)` | toolbar/menu | MUST | `cedg.locus.v2` |
| `semantic.locus-v2.point-explicit` | `MODE_LOCUS_V2_POINT(134)` | toolbar/menu | MUST | retained exact-address helper in Semantic Curves; ordinary Point consumes R6 typed preimages instead of duplicating this command |
| `semantic.curve.inspect-definition` | read-only semantic definition action | context/menu | MUST | shared with SplineV2 and definition cluster |

Legacy `MODE_LOCUS(47)` is not exposed as Locus V2 authority.

### 4.8 SplineV2

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `semantic.spline-v2.create` | canonical `SplineV2` command | toolbar/menu | MUST | select/focus one existing frontend command seam; expose all four forms and three-point minimum; no second parser |
| `semantic.curve.inspect-definition` | shared read-only semantic definition action | context/menu | MUST | feature-gated |

Autocomplete, explicit help, Algebra Input, and GGBScript must share command
availability. Classic `Spline` remains unchanged.

### 4.9 Metrics and validation

| Stable action ID | Target | Placement | Disposition |
|---|---|---|---|
| `measure.angle` | `MODE_ANGLE(36)` | toolbar | MUST |
| `measure.distance-length` | `MODE_DISTANCE(38)` | toolbar | MUST |
| `measure.area` | `MODE_AREA(49)` | overflow | MUST |
| `measure.slope` | `MODE_SLOPE(50)` | overflow | SHOULD |
| `measure.locus-v2-total-length` | `MODE_LOCUS_V2_LENGTH(135)` | toolbar/menu | MUST |
| `measure.locus-v2-partial-length` | `MODE_LOCUS_V2_LENGTH_BETWEEN(136)` | toolbar/menu | MUST |
| `result.inspect-rich` | shared rich-result inspector | menu | MUST |

Rich metric authority and guarded scalar adapters remain unchanged.

### 4.10 Similarity transformations

| Stable action ID | Target | Placement | Disposition |
|---|---|---|---|
| `transform.reflect-point` | `MODE_MIRROR_AT_POINT(29)` | toolbar | MUST |
| `transform.reflect-line` | `MODE_MIRROR_AT_LINE(30)` | toolbar | MUST |
| `transform.translate-vector` | `MODE_TRANSLATE_BY_VECTOR(31)` | toolbar | MUST |
| `transform.rotate-angle` | `MODE_ROTATE_BY_ANGLE(32)` | toolbar | MUST |
| `transform.dilate-point` | `MODE_DILATE_FROM_POINT(33)` | toolbar | MUST |

The actions remain available for inherited objects. Selecting a GeoLocusV2
uses the R5 kernel only when approved runtime creation policy permits it.
Transformed objects get new identity/tokens; no source-token reuse. Future
Point interaction on the output resolves through R6 in the transformed source
context, not by inverse-transforming a click in the frontend.

### 4.11 Authorized manual projection grouping

This menu/task cluster references, but does not redeclare:

`construction.line`, `construction.parallel-line`,
`construction.perpendicular-line`, `construction.midpoint`,
`transform.reflect-point`, `transform.reflect-line`,
`transform.translate-vector`, `transform.rotate-angle`, `measure.angle`,
`measure.distance-length`, and `presentation.show-hide-object`.

`workspace.cedg-dihedral-procedures` is a disabled-with-reason workspace switch.
It remains DEFERRED pending global G9 and explicit G9U2 authorization. There are
no active projection, fold, true-magnitude, section, or development procedures.

### 4.12 Presentation, visibility, and style

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `presentation.show-hide-object` | `MODE_SHOW_HIDE_OBJECT(27)` | toolbar/menu | MUST | presentation only |
| `presentation.show-hide-label` | `MODE_SHOW_HIDE_LABEL(28)` | overflow | MUST | presentation only |
| `presentation.copy-style` | `MODE_COPY_VISUAL_STYLE(35)` | toolbar | MUST | navigation flyout; zero semantic revision |
| `presentation.text` | `MODE_TEXT(17)` | overflow | SHOULD | inherited |
| `presentation.image` | `MODE_IMAGE(26)` | overflow | SHOULD | characterize asset/licensing behavior; defer if new unreviewed assets would be required |
| `presentation.axes-toggle` | existing host view action | menu/settings | MUST | viewport only |
| `presentation.grid-toggle` | existing host view action | menu/settings | MUST | viewport only |
| `view.properties` | existing Properties view | menu/context | MUST | shared action |

### 4.13 Navigation and zoom

| Stable action ID | Target | Placement | Disposition | Boundary |
|---|---|---|---|---|
| `navigation.pan-view` | `MODE_TRANSLATE_VIEW(40)` | toolbar | MUST | viewport only |
| `navigation.zoom-in` | `MODE_ZOOM_IN(41)` | toolbar/menu | MUST | viewport only |
| `navigation.zoom-out` | `MODE_ZOOM_OUT(42)` | toolbar/menu | MUST | viewport only |
| `navigation.zoom-window` | new GeoCeDG frontend rectangle action | toolbar/menu/keyboard | MUST | no semantic state |
| `navigation.standard-view` | existing `standardView` host action | menu/context | SHOULD | existing |
| `navigation.show-all-objects` | existing `showAllObjects` host action | menu/context | SHOULD | existing |

These six actions are the bounded G9U1 navigation set. No separate later
navigation phase is silently executed.

### 4.14 Document lifecycle

| Stable action ID | Target | Placement | Disposition | Boundary |
|---|---|---|---|---|
| `document.new` | existing new-document action | File menu | MUST | `.cedg` native identity |
| `document.open` | existing open action | File menu/import grouping | MUST | `.cedg` native plus `.ggb` compatibility input |
| `document.open-recent` | existing recent action | File menu | MUST | native/compatibility policy |
| `document.save` | existing save action | File menu | MUST | never silently overwrite compatibility `.ggb` |
| `document.save-as` | existing save-as action | File menu | MUST | defaults `.cedg` |
| `document.print-preview` | existing print preview | File menu | SHOULD | presentation/export only |
| `document.close` | existing close action | File menu | MUST | host lifecycle |

Classic preserves supported `.cedg` documents but does not adopt GeoCeDG's
default new-document identity.

### 4.15 Automation and scripting

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `automation.algebra-input-toggle` | existing Algebra Input view action | menu/view | MUST | preview/commit contract required |
| `automation.manage-user-tools` | existing user-tool manager | menu | SHOULD | never auto-load Templatev7 |
| `automation.object-scripting` | existing Properties/Scripting tab if a stable route exists | context | SHOULD | host-characterization required; no parallel script editor |
| `automation.legacy-laboratory` | isolated controlled Laboratory route | menu | DEFERRED / diagnostic-only | feature/provenance gates |

GGBScript is primarily a command-surface property of each command, not a fake
toolbar action. Semantic command gates, syntax, localized lookup, and atomic
failure must match Algebra Input.

### 4.16 Authorized import and export

| Stable action ID | Target | Placement | Disposition | Boundary |
|---|---|---|---|---|
| `document.open` | shared document open | File/import grouping | MUST | owns `.ggb` compatibility input; no duplicate importer |
| `export.dxf-2d` | existing GeoCeDG DXF dialog | menu | MUST | `cedg.export.dxf.2d`; extended fidelity stays within the same exporter |

PDF/SVG sheet production, arbitrary CAD import, and legacy macro packaging are
not authorized here.

### 4.17 Help and command discovery

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `help.input-panel` | existing Input Help | help/view | MUST | EN/ES |
| `help.command-list` | existing command list | help/menu | MUST | command filters must also govern explicit help |
| `help.contextual-action` | new manifest-driven contextual help | context/status | MUST | one action registry |
| `help.user-guide` | GeoCeDG user guide route | help/menu | MUST | validated living guide |
| `help.keyboard-shortcuts` | existing shortcut help | help/menu | SHOULD | include ZoomWindow and workspace keys |
| `help.about-geocedg` | GeoCeDG About route | help/menu | MUST | product identity |
| `diagnostic.open-classic` | isolated Classic launcher | help/menu | MUST | separate process/preferences |
| `settings.product-language` | GeoCeDG EN/ES selector | settings | MUST | retain upstream corpus; Classic remains unrestricted |

Every visible action must resolve name, short help, long help, status, and error
keys in English and Spanish. Feature-off autocomplete and explicit help must
not advertise unavailable creation.

For `construction.point` and `construction.point-on-object`, both languages
must cover semantic-curve stroke selection, unique creation, the multiple-
preimage chooser, Cancel, drag, unresolved search and degenerate source
feedback. Keyboard navigation and accessible names are mandatory. Help must
also distinguish mouse interaction through R6 from the exact Algebra/GGBScript
semantic-address command.

### 4.18 Construction history and definition inspection

| Stable action ID | Target | Placement | Disposition | Gap / prerequisite |
|---|---|---|---|---|
| `view.construction-protocol` | existing Construction Protocol view | menu/view | MUST | view-only |
| `view.construction-navigation` | existing protocol navigation | menu/view | SHOULD | presentation state |
| `view.properties` | existing Properties view | menu/context | MUST | shared action |
| `inspect.definition` | new read-only definition route | context/double-click | MUST | must not redefine or globally change Algebra description |
| `semantic.curve.inspect-definition` | shared semantic curve definition view | context | MUST | no render-sample authority |
| `algebra.description.value` | `AlgebraStyle.VALUE` | Algebra style menu | MUST | exclusive checked current state |
| `algebra.description.description` | `AlgebraStyle.DESCRIPTION` | Algebra style menu | MUST | exclusive checked current state |
| `algebra.description.definition` | `AlgebraStyle.DEFINITION` | Algebra style menu | MUST | exclusive checked current state |

Desktop-unavailable Algebra styles are not shown as false options. The current
menu's checked state must compare with the active Algebra style, not the tree
sort mode.

## 5. Cross-cutting G9U1 gaps and prerequisites

| Requirement | Disposition | Prerequisite / fail-closed rule |
|---|---|---|
| `Continuity = OFF` | MUST | reuse and lock the existing host setting in GeoCeDG; preferences, restart, workspace, `.cedg`, and `.ggb` cannot enable it; Classic remains configurable |
| Stroke-only LocusV2/SplineV2 hit testing | MUST | selection belongs to visible stroke/tolerance; closed interior is a negative hit; rendering never becomes semantic authority |
| Semantic Point placement/drag | MUST | consume published R6 typed candidates; UNIQUE creates/updates, MULTIPLE requires explicit chooser, all other statuses fail closed; exact branch/component/address, no generic `Path` |
| Periodic seam Point drag | MUST | same point/ID/source crosses the canonical seam in both directions using exact persisted direction bits; no duplicate seam candidate; unresolved leaves the point unchanged |
| Transformed and collapsed Point interaction | MUST | R6 resolves R5 transformed sources; negative dilation works normally; `k=0` creates no arbitrary new point and preserves/recover existing exact direction |
| Point command/tool/GGBScript boundary | MUST | toolbar/menu mouse interaction consumes R6; exact `Point(L,"branch",u)` remains command/script authority; no synthetic mouse API or frontend inverse fallback |
| Candidate markers | MUST | active selected rich result only; presentation overlay, no GeoElement/XML/DAG/undo identity |
| Persistent multi-materialization inspector | MUST | create selected/multiple/all; already-created choices marked; inspector remains open; opaque token never sizes UI |
| Algebra Input preview/commit | MUST | typing/preview creates zero productive objects/IDs/undo/XML; Enter is one explicit transaction; Escape creates zero |
| G9A free-input compatible redefine | MUST | label locates intended current object only; approved compatibility predicate and atomic transaction preserve identity; ambiguous/incompatible fail closed |
| GGBScript consistency | MUST | same command dispatch, feature policy, identity, undo, persistence, and errors as Algebra Input |
| EN/ES product language profile | MUST | product offers only EN/ES; upstream resources remain; Classic behavior retained |
| Product/Classic isolation | MUST | workspace, branding, Continuity lock, semantic creation, markers, and materialization remain product-only |
| Branding roles | MUST | `geocedg.brand.topbar` and `geocedg.brand.startup`; author provenance and deterministic derived assets; no fabricated logo |
| Accessibility, keyboard, DPI | MUST | keyboard operation, focus, accessible names, contrast, bounded dialogs, normal scaling |
| Responsiveness | MUST | marker/result lookup uses current resolved roots; no new solve per point or event-thread blocking |
| Periodic quarantine round trip | MUST validation disposition | `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open until actual native archive lifecycle evidence passes |
| SplineV2×SplineV2 materialization | MUST HAVE within R1 subset | existing one/selected/all actions consume current certified singleton-germ tokens; no new action |

Mandatory entry authority includes clean PASS-tagged G9U0/R1/R2/R3/R4/R5,
G9S1 and G9U0-R6 descendants, ADRs 0012/0016/0017/0018/0019, the current
normative workspace, document, Locus, transformation, spline, interaction,
identity, and persistence contracts, and a still-explicit disposition of the
R4 periodic risk. R6 satisfies the final kernel prerequisite found by the
workspace audit; no additional kernel gate is required for the intended Point
workflow.

## 6. Deferred and excluded inventory

| Item | Disposition | Reason / required authority |
|---|---|---|
| Legacy `MODE_LOCUS(47)` | Classic-only compatibility | sampled Locus is not V2 semantic authority |
| `MODE_MIRROR_AT_CIRCLE(54)` | DEFERRED | inversion is outside R5 similarity transforms |
| Templatev7 macros `100001..100024` | diagnostic-only | legacy reference; each needs provenance/scientific review and independent promotion |
| CeDG projection/fold/true-magnitude/section/development procedures | NEW KERNEL / G9U2 | global G9 and explicit G9U2 authorization required |
| 3D/spatial creation modes | NEW KERNEL | no G9B/G9C authorization |
| Generic pair materialization beyond R1 | NEW KERNEL | only current R1 certified singleton-germ slots are in scope; no generic expansion |
| PDF/SVG sheet export and broad CAD import | DEFERRED | no approved phase |
| FitLine, complex-number, freehand, pen, media, statistics, CAS, spreadsheet default modes | DEFERRED pending usability audit | upstream or Templatev7 presence alone is not CeDG workspace authority |

## 7. Unresolved bounded host-characterization decisions

These are implementation-entry questions, not permission to create speculative
actions:

1. **Desktop selection mode:** prove whether `MODE_SELECT(77)` is a supported,
   useful Desktop gesture distinct from `MODE_MOVE`. If not, remove it from
   visible overflow and classify it deferred.
2. **Object scripting route:** prove a stable action can open the selected
   object's existing Properties/Scripting tab. Otherwise expose scripting only
   through Properties and do not create a standalone button.
3. **SplineV2 creation surface:** choose an existing task/dialog or focus the
   canonical Algebra Input with localized syntax help. Never duplicate command
   parsing.
4. **Image action assets:** confirm inherited `MODE_IMAGE` exposure adds no
   unreviewed branding/packaging asset. Otherwise keep it deferred.

## 8. Planning validation obligations

The future static design verifier must prove:

- schema and JSON-compatible candidate syntax;
- exactly eleven broad families and eighteen operational clusters;
- unique action, family, cluster, workspace, feature, and route IDs;
- all cluster/action/policy/feature/diagnostic references resolve;
- all 110 actions are placed at least once;
- no duplicate emitted toolbar mode;
- each visible action has a truthful availability, feature, Classic, help,
  localization, icon/fallback, effect, undo, and future test contract;
- live `apps/geocedg/application-profile.yml` remains schema v1 and byte
  unchanged during planning;
- no productive Java/Desktop/profile behavior is changed;
- every required future scenario, including `U1-PNT-01`–`U1-PNT-20` and the
  actual periodic-quarantine native round trip, is represented in the G9U1
  validation matrix; and
- exactly 138 public scenarios remain unique and complete while the action
  catalog remains exactly 110 actions.

## 9. Terminal governance state

```text
G9U1 DESIGN =
PASS — AUTHOR APPROVED

POST-R6 RECONCILED = true
POST-R1 RECONCILED = true

G9U1 IMPLEMENTATION =
IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW

implementationStarted = true
implementationAuthorized = true
selfApproved = false
authorApprovedDesign = true
passClaimedImplementation = false

G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```


## Post-R1 reconciliation authority

The approved post-R6 checkpoint `00982e7e148a634cd57ed928f322774df267d5e3`
remains immutable. This successor consumes published G9S1-R1 without changing
R6 Point semantics or adding an action/family. The current conditional author
authorization is satisfied by the no-material-novelty audit. Implementation has
not started at this planning freeze and is never self-approved. See the
[published R1 pair-consumer contract](../../geocedg/specs/ui/g9u1-construction-interaction.md#published-g9s1-r1-pair-consumer-contract)
and the 20 additive `U1-PAIR` rows (138 total). The R4 periodic risk remains
OPEN / TRACKED; an inconclusive native experiment without a real kernel or
persistence violation does not block this candidate under the author's explicit
disposition. No other workspace scope is expanded.

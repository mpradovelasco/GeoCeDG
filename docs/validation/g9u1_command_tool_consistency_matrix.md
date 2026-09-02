# G9U1 command/tool consistency matrix

- Status: **DESIGN PASS — AUTHOR APPROVED — G9U1 IMPLEMENTATION NOT AUTHORIZED**
- Phase: G9U1 — CeDG Construction workspace
- Required predecessors: G9U0, G9U0-R1, G9X1, G9U0-R2, G9U0-R3,
  G9U0-R4, G9U0-R5, G9S1 and G9U0-R6 sealed
  `PASS — AUTHOR APPROVED`
- Published R6 authority: commit
  `3942af594e4507e479f2c75019cef62e3d9fea6f`, annotated tag
  `geocedg-g9u0-r6-pass`
- Protected pre-R6 checkpoint:
  `857de6628489bda0b65a5ba5145e62ca0795fc32`, unchanged
- Canonical future prompt:
  [`g9u1-construction-workspace-after-g9u0-r6.prompt.md`](../../.github/prompts/tasks/g9u1-construction-workspace-after-g9u0-r6.prompt.md)
- Normative workspace contract:
  [`cedg-workspaces.md`](../../geocedg/specs/ui/cedg-workspaces.md)
- Executable scenario authority:
  [`g9_public_workspace_validation_matrix.md`](g9_public_workspace_validation_matrix.md),
  exactly **118 G9U1 rows**

This document reconciles the complete future workspace/action surface. It is
planning evidence, not productive G9U1 implementation and not a second command
registry. Stable action IDs, placement and unavailable policy must be declared
once in the future schema-v2 application manifest. Command parsing, geometry,
metrics, intersections, selectors, tokens and reactivation remain shared-kernel
authority.

R6 is no longer provisional. Its published query/resolver/candidate and
create/move seams remove the last kernel prerequisite found by the workspace
gap audit. G9U1 adds only the frontend, help and localization consumers defined
below; SplineV2 × SplineV2 materialization remains a separate rich-only limit,
not a missing prerequisite for the intended workspace.

## 1. Cross-surface invariants

1. One manifest action declaration supplies every toolbar, menu, overflow,
   contextual-help and result-action placement.
2. Workspace membership affects discoverability only. It never changes command
   dispatch, feature policy, Construction, XML, undo or semantic evaluation.
3. Algebra input, autocomplete, explicit command help and GGBScript must agree
   on the same command name, syntax, runtime gate and atomic failure.
4. `--enableLocusV2=true` remains the only public V2 opt-in. No separate
   spline, intersection or transform flag is permitted.
5. GeoCeDG Construction locks the existing host `Continuity = OFF`. The
   separate Classic diagnostic route retains upstream configurability.
6. Presentation markers consume current exact-token authority only. They do not
   discover, order, continue or persist semantic identity.
7. Kernel recompute may reactivate an already-existing exact-token point but
   never creates a new GeoPoint. Creation of new points is an explicit,
   visible, undoable frontend transaction.
8. Feature-off and Classic file-loading preservation never make experimental
   creation discoverable after loading.
9. R6 semantic-preimage candidates drive Point-tool creation and drag; R4 rich-
   result tokens drive intersection markers/materialization. Neither authority
   may be substituted for the other.
10. Pointer interaction is not a GGBScript grammar. Scripted semantic points
    use exact forms such as `Point(L,"branch",u)`; no screen/mouse-position API
    is introduced.

## 2. Complete functional-cluster coverage

The following 18 clusters cover all eleven professional Construction groups and
the seven cross-cutting action families required to keep command, tool,
document and product policy coherent.

| Cluster | Functional family | Required action/authority reconciliation | Required result |
|---|---|---|---|
| F01 | profile, schema and workspace control | schema v2, deterministic v1 migration, workspace chooser, dock/layout compiler, preference adapter | one valid manifest authority; switching and migration cause zero semantic mutation |
| F02 | Inspect and construct | Move, point, point-on-object, R6 ambiguity chooser/drag orchestration, Algebra/Protocol/Properties toggles, undo/redo, object and rich-result inspection | stable focus/selection grammar; R6 typed preimages; normal kernel transactions |
| F03 | Linear geometry | point, line, segment, ray, vector, parallel, perpendicular and midpoint families | inherited command/mode semantics; deterministic manifest order and help |
| F04 | Parameters and drivers | numeric parameters, sliders, scalar domains/maps, true-driver roles and point generators | explicit dependencies; `k=...` compatible redefine uses the approved G9A transaction |
| F05 | Relations and intersections | ordinary Intersect, tangent/relation, rich-result inspector and exact-token point actions | rich result remains authority; no frontend solving or identity |
| F06 | Circles, conics and curves | circle/conic families, inherited tangents, approved curve/function entry and Classic `Spline` | inherited behavior preserved; semantic `SplineV2` remains a separate contract |
| F07 | Locus V2 and semantic Spline V2 | Locus V2 creation, Spline V2 public forms, exact command Point-on-Locus plus Point-tool create/drag through R6, and semantic inspection | new semantic `GeoLocusV2` objects only; no sampled/render authority |
| F08 | Metrics and validation | rich/guarded total and partial length, typed status/certificate/diagnostic inspection | rich result computed once; scalar remains a guarded child |
| F09 | Transformations and manual projections | Translate, Rotate, Reflect/Mirror, Dilate and approved manual 2D projection helpers | ordinary command processors; transformed V2 remains first-class semantic source |
| F10 | CeDG procedures/developments placeholders | separately gated procedure/development families | visible unavailable policy only; no unauthorized G9B/G9C/G9U2 semantics |
| F11 | Presentation and document | show/hide, style, labels, views, layers where approved, Save/Save As/open/recent/direct-open | presentation does not alter geometry; one application-owned `.cedg`/`.ggb` state machine |
| F12 | Automation and import/export | scripts/macros where approved, GGBScript, DXF and document/import/export actions | explicit feature policy and provenance; no second geometry authority |
| F13 | command discovery, help, script and localization | command dictionary, autocomplete, explicit syntax, EN/ES localization, GGBScript localization/delocalization | one syntax/gate across every entry surface; no stale cache after language refresh |
| F14 | deterministic product policy and redefine | Continuity lock, preference/load precedence, `k=...` compatible/incompatible/ambiguous redefine | existing host setting and approved atomic G9A identity predicate only |
| F15 | current rich-result markers | active-result overlay, marker preference, accessible evidence classes and hit testing | current admissible exact tokens only; no GeoElement/XML/DAG/undo/token of marker |
| F16 | inspector and exact-token materialization | create one, selected subset, all eligible, persistent inspector and optional initial auto transaction | one point per exact token; coherent compound undo; cancel creates nothing |
| F17 | visual identity, branding and accessibility | professional grouping, themes/accents, `geocedg.brand.topbar`, `geocedg.brand.startup`, keyboard/contrast/DPI | GeoCeDG visibly distinct from Classic; visual state causes zero Construction mutation |
| F18 | compatibility, persistence, risk and performance | feature-off/Classic preservation, native/compatibility document policy, periodic quarantine, deterministic work evidence | fail-closed compatibility, native lifecycle evidence and bounded work without repeated solves |

## 3. Required action-record fields

Every schema-v2 action record and every validation row that consumes it must
resolve these fields:

| Field | Contract |
|---|---|
| stable action ID | one durable manifest identifier; never a raw toolbar string or semantic identity |
| action kind | inherited mode, command, product action, view toggle, result inspector or diagnostic route |
| symbolic target | audited command/mode/controller authority; no duplicated hard-coded implementation |
| syntax or selection grammar | exact argument/selection slots, order, allowed types and cancel behavior |
| result/procedure kind | ordinary GeoElement, rich result, presentation overlay, view action or external adapter |
| maturity and feature requirements | independent feature IDs and one declared unavailable policy |
| localization/help | name, short help, long help, status, error and blocked-reason keys |
| icon/branding reference | GeoCeDG-owned logical asset ID or explicit accessible text fallback |
| Algebra/autocomplete/help contract | whether the underlying command is discoverable and its exact syntax |
| GGBScript contract | same dispatcher/gate for command-backed actions, or explicitly not applicable |
| product/Classic behavior | GeoCeDG ON, GeoCeDG OFF, fork Classic preservation/creation and external-upstream boundary |
| persistence/undo | whether the action writes Construction/XML/undo or profile preferences only |
| marker/materialization consequence | current-token eligibility, rich-only boundary or not applicable |
| failure atomicity | typed/localized fail-closed behavior and rollback boundary |
| validation evidence | focused scenario IDs, deterministic rerun and manual-review obligation |

## 4. Command and tool consistency matrix

The “stable action role” is a required manifest role. This planning matrix does
not freeze an arbitrary concrete ID before schema-v2 implementation.

| Capability | Stable action role | Public command/action authority | Exact grammar or selection contract | Kernel result/authority | Feature and unavailable policy | Construction placement and UX consequence | Validation |
|---|---|---|---|---|---|---|---|
| semantic spline, default | create semantic spline | `SplineV2` | `SplineV2({points})`; 3–32 finite 2D points | new semantic `GeoLocusV2`; default degree 3 | existing Locus V2 opt-in only | Locus V2 and semantic Spline V2; creates no point | U1-C01..C09, U1-S1-01 |
| semantic spline, degree | create semantic spline | `SplineV2` | `SplineV2({points}, degree)` | semantic `GeoLocusV2`; integral bounded degree | same | same | U1-C03, C06, C09 |
| semantic spline, weighted | create semantic spline | `SplineV2` | `SplineV2({points}, degree, weightFunction)` | semantic `GeoLocusV2`; positive finite increment weights | same | same | U1-C03, C07, C09 |
| semantic spline, point wrapping | create semantic spline | `SplineV2` | `SplineV2(A,B,C,...)`; at least three points | same semantic result family and default degree | same; help must expose the real minimum | same | U1-C03, C06, C10 |
| legacy spline control | inherited curve action | `Spline` | unchanged upstream overloads | upstream curve result, never `GeoLocusV2` | no V2 flag | Circles, conics and curves; no G9S1 token semantics | U1-C05, C13, C20 |
| semantic Locus V2 creation | create semantic locus | approved `LocusV2` authority | approved G9U0 scalar/point-generator forms | semantic `GeoLocusV2` | existing Locus V2 opt-in | Locus V2 and semantic Spline V2; no automatic point | U1-W15, C14, C18 |
| exact semantic Point command | create semantic point from known address | approved `Point` semantic-address command | `Point(L,"branch",u)` or the current exact branch/component/address form | ordinary `GeoPoint` with semantic parentage | V2 gate for a new operation | Algebra and GGBScript exact command; menu/help/EN/ES syntax; no pointer simulation and not token materialization | U1-W15, U1-PNT-01..02 |
| Point-tool semantic-curve query | ordinary Point / Point-on-Object modes | frontend stroke hit -> world target -> `LocusPointInteractionQuery2D` -> `LocusPointInteractionResolver2D.resolve(...)` | selected `GeoLocusV2`, finite world target and policy; current address also supplied for drag | typed `LocusPointInteractionResult2D` and candidates | existing V2 opt-in; feature-off unavailable | Point toolbar/menu modes; closed interior is not a hit; stroke tolerance is frontend-only; localized help/status in EN/ES | U1-PNT-01..06, U1-PNT-19..20 |
| unique Point-tool creation | create one interaction-owned semantic point | `LocusV2PublicOperations.createInteractiveSemanticPoint(...)` | exact R6 candidate from `UNIQUE_ADMISSIBLE_PREIMAGE` only | same ordinary `GeoPoint` model with durable source/branch/component/address and normal DAG parent | current uniquely admissible R6 result only | one visible undoable creation; click coordinate discarded as identity; save/reopen/copy supported | U1-PNT-01..02, U1-PNT-08, U1-PNT-16..18 |
| semantic-preimage ambiguity chooser | transient deterministic chooser | consume candidates only from `MULTIPLE_SEMANTIC_PREIMAGES` | exact candidate objects; branch/component, semantic location, span/orientation and transient highlight as presentation | selected R6 candidate is passed unchanged to the creation seam | no candidate on unresolved/invalid/degenerate/unsupported status | keyboard chooser with EN/ES accessible name/help; Cancel creates nothing; displayed order is never identity | U1-PNT-07..09 |
| interaction-owned semantic drag | update existing point | `LocusV2PublicOperations.moveInteractiveSemanticPoint(...)` | existing interaction-owned `GeoPoint`, world target and policy/current address | same point/ID/source, updated exact semantic address when uniquely resolved | fail closed on ambiguous/unresolved/invalid/degenerate result | Move/Point drag gesture; no replacement point or proximity fallback; normal undo/redo | U1-PNT-03..04, U1-PNT-17 |
| periodic seam drag | update same closed semantic point | same R6 move seam over canonical periodic source | geometric target crossing the seam in both directions | same point/ID/source/component; canonical parameter wraps and exact direction bits remain authoritative | unique seam continuation only; unresolved control leaves point unchanged | ordinary drag feedback; no duplicate seam candidate and no routine `periodicLift` jargon | U1-PNT-10 |
| transformed-source Point interaction | create/drag on R5 transformed semantic source | same R6 query/create/move seams | Translate, Rotate, Reflect/Mirror, positive or negative Dilate output | point belongs to transformed `GeoLocusV2`; new source context | invertible current source only | ordinary Point tool and drag; no source-token reuse | U1-PNT-11..13 |
| collapsed-image Point behavior | truthful degenerate handling | R6 `DEGENERATE_SOURCE_IMAGE`/typed result at `k=0` | new click or existing interaction-owned point on `COLLAPSED_IMAGE` | new click creates nothing; existing point retains exact semantic direction and recovers as same point after nonzero restoration | no arbitrary preimage selection | localized non-error/degenerate feedback; no chooser fabricated from coordinates | U1-PNT-14..15 |
| semantic Point lifecycle | persist/copy/remap/undo interaction-owned point | existing R6 semantic parent/address serialization and host transactions | exact source binding, branch/component and semantic direction | same durable identity graph after save/reopen; normal copy/remap and undo/redo | fail closed if exact provenance cannot reconstruct | no screen target persisted; help exposes supported lifecycle | U1-PNT-16..18 |
| total rich metric | inspect total metric | `LocusLength(L)` | one semantic source | rich nonnumeric metric authority | existing V2 policy | Metrics and validation; no marker/point | U1-W15 |
| total guarded scalar | create total scalar | `Length(L)` | one semantic source | guarded ordinary scalar child | existing V2 policy | Metrics and validation | U1-W15 |
| partial rich metric | inspect partial metric | `LocusLength(L,P,Q)` | exact same-source semantic endpoints | rich partial metric authority | existing V2 policy | Metrics and validation | U1-W15 |
| partial guarded scalar | create partial scalar | `Length(L,P,Q)` | same approved endpoints | guarded scalar child of rich authority | existing V2 policy | Metrics and validation | U1-W15 |
| one-sided rich intersection | create/inspect rich intersection | `Intersect(L,T)` | semantic Locus/Spline V2 plus supported target | rich intersection result | existing V2 policy | Relations and intersections; only current point-admissible exact tokens get selectable markers | U1-I01..I14, U1-S1-02 |
| piecewise-polynomial pair intersection | inspect rich pair result | `Intersect(L1,L2)` | supported semantic piecewise-polynomial pair | symmetric rich-only diagnostic result | existing V2 policy | inspectable evidence; zero selectable markers and zero create-one/selected/all | U1-S1-06 |
| translation | translate semantic curve | `Translate(L,v)` | semantic source and supported vector | new semantic transformed `GeoLocusV2` | V2 branch requires existing opt-in | Transformations and manual projections; fresh transformed-query tokens | U1-R5-01..04, C15 |
| rotation about origin | rotate semantic curve | `Rotate(L,angle)` | finite supported angle | new semantic transformed `GeoLocusV2` | same | same | U1-R5-01..04, C15 |
| rotation about center | rotate semantic curve | `Rotate(L,angle,center)` | finite angle and supported center | new semantic transformed `GeoLocusV2` | same | same | U1-R5-01..04, C15 |
| point reflection | reflect semantic curve | `Reflect/Mirror(L,point)` | supported finite point | new semantic transformed `GeoLocusV2` | same; aliases share one authority | same | U1-R5-01..04, C15 |
| line reflection | reflect semantic curve | `Reflect/Mirror(L,line)` | supported finite line | new semantic transformed `GeoLocusV2` | same; aliases share one authority | same | U1-R5-01..04, C15 |
| origin dilation | dilate semantic curve | `Dilate(L,k)` | finite scalar, including `k=0` | semantic transformed Locus; `COLLAPSED_IMAGE` at zero | same | collapsed state fabricates no isolated token/marker | U1-R5-01, R5-05, C15 |
| centered dilation | dilate semantic curve | `Dilate(L,k,center)` | finite scalar and supported center | same | same | same | U1-R5-01, R5-05, C15 |
| compatible free-input redefine | redefine compatible numeric | ordinary assignment through approved G9A seam | `k=0.25` with one explicit compatible command-context target | atomic identity-preserving redefine only when compatibility succeeds | product UX; Classic characterized separately | Parameters and drivers; label locates only, never owns identity | U1-R5-06, U1-D01..08 |
| rich-result inspector | inspect rich result | frontend consumer of selected rich result | Algebra-selected result, unique fallback or deterministic chooser | reads kernel rich result/current tokens only | visible only under approved policy | persistent keyboard-accessible session; compact labels only | U1-I01, I12, P02 |
| active-result markers | show intersection candidates | frontend overlay | active/selected rich result only | presentation over current exact-token authority | default ON in Construction; explicit OFF allowed | no GeoElement/XML/DAG/undo/copy/token | U1-I01..I04, I07..I10 |
| create selected point | materialize selected token | exact-token frontend transaction | one selected current admissible token | one ordinary exact-token `GeoPoint` | only while current token is eligible | one undoable point; inspector stays open | U1-I05, I12 |
| create selected points | materialize selected tokens | exact-token compound transaction | explicit subset of distinct current admissible tokens | one point per selected token | same | one coherent compound undo; no ordinal identity | U1-I13 |
| create all eligible points | materialize all eligible | exact-token compound transaction | all current eligible tokens only | one point per eligible token | same | one coherent compound undo; no re-solving | U1-I06 |
| initial auto-materialization | explicit auto policy/action | visible frontend transaction immediately after rich-query creation | explicit opt-in; current eligible tokens only | ordinary points created by one frontend transaction | never kernel recompute/load/later-root creation | undoable initial transaction; later roots markers only | U1-I09, I10 |
| existing-point reactivation | no new UI creation action | kernel exact-selector recompute | already-existing exact-token point only | same point may become active after dormancy | independent of workspace and marker preference | no new DAG node and no frontend transaction | U1-I10, I11 |
| inspect and construct controls | inherited/product action roles | Move, Point, view toggles, undo/redo, Protocol/Properties and object inspection | typed mode/view selections and explicit cancel | ordinary existing frontend/kernel authorities | declared once per action | Inspect and construct; workspace switch cancels unfinished tool and returns to Move | U1-W01..W06, U1-G01..G03 |
| linear geometry controls | inherited mode roles | line, segment, ray, vector, parallel, perpendicular and midpoint commands/modes | existing upstream selection grammars | existing upstream GeoElements/algorithms | independently inherited feature policy | Linear geometry; deterministic grouping/overflow | U1-W02..W06, U1-G01..G02 |
| parameter/driver controls | inherited/product action roles | numeric/slider input and approved scalar/point generator actions | explicit numeric/domain/support selections | ordinary DAG parameters and approved V2 providers | independent maturity gates | Parameters and drivers; no hidden polling | U1-W02..W06, U1-G01..G02 |
| circle/conic/curve controls | inherited mode/command roles | approved circle, conic, tangent, function/curve and Classic Spline authorities | existing upstream grammars | existing objects plus separately declared Spline V2 | independent feature policies | Circles, conics and curves; no Classic/V2 conflation | U1-W02..W06, U1-C20, U1-G01..G02 |
| gated CeDG procedure controls | unavailable placeholder roles | future procedure/development actions | no productive selection grammar before their gates | no output before authorization | hidden or disabled with localized reason | procedures/developments group remains professional but fail-closed | U1-S03, U1-W04, U1-G01..G02 |
| presentation/document controls | product/document action roles | style/show-hide/view plus Save/Save As/open/recent/direct-open | existing presentation and R2 document contracts | no geometric semantics from workspace state | document policy independent of workspace | Presentation and document; `.cedg` native/`.ggb` compatibility | U1-W05, W07..W17, U1-B02 |
| automation/import/export controls | script/export action roles | GGBScript, approved automation and DXF/import/export actions | each independent command/adapter contract | normal command DAG or read-only export model | each independent feature/provenance policy | Automation and import/export; no second geometry authority | U1-C08..C13, U1-G01..G02 |
| Classic diagnostic route | diagnostic-route role | separate Classic process/profile | explicit process launch; never an in-process workspace switch | shared persistence/recompute, separate UI/preferences | no experimental creation authority | visible diagnostic route; Classic identity retained | U1-W11, U1-D08, U1-C05, C13, C20 |
| visual identity and brand consumers | theme/brand roles | frontend theme plus `geocedg.brand.topbar` and `geocedg.brand.startup` | presentation/resource resolution only | no kernel result | author provenance/suitability gate | distinct accessible GeoCeDG chrome; Classic remains distinct | U1-B01..B03, U1-A01..A04 |

## 5. Required post-G9U1 product/Classic behavior

| Concern | GeoCeDG Construction, V2 ON | GeoCeDG, V2 OFF | GeoCeDG Classic diagnostic | External upstream |
|---|---|---|---|---|
| preference namespace | `geocedg` | `geocedg` | separate Classic namespace | upstream-owned |
| `SplineV2` autocomplete | visible once | hidden/unavailable | hidden/unavailable | unsupported |
| explicit `SplineV2` help | complete four-form help | unavailable consistently with dispatch | no misleading creation offer | unsupported |
| Algebra `SplineV2` creation | allowed | atomic rejection | atomic rejection | unsupported |
| GGBScript `SplineV2` creation | allowed | atomic rejection | atomic rejection | unsupported |
| exact `Point(L,"branch",u)` command/GGBScript | allowed under current semantic command contract | V2 creation unavailable | preservation only; no experimental creation offer | unsupported GeoCeDG semantic source |
| Point-tool click/drag on semantic curve | R6-backed unique create/move plus explicit ambiguity chooser | unavailable for new semantic interaction | upstream Point behavior only; no R6 product chooser | unsupported |
| upstream `Spline` | unchanged | unchanged | unchanged | upstream behavior |
| open existing `.cedg` with V2/Spline | native load/recompute | preservation load/recompute | preserve/recompute without creation enablement | unsupported-open boundary |
| save supported `.cedg` | native Save/Save As | native preservation | preserve supported native document; not Classic default-new identity | no GeoCeDG guarantee |
| V2 transform creation | allowed through ordinary commands | V2 operand rejected for a new operation | V2 operand rejected for a new operation | unsupported |
| persisted transformed V2 | reconstruct/recompute | reconstruct/recompute | reconstruct/recompute | unsupported |
| workspace | schema-v2 Construction | same product profile with unavailable actions | no in-process Construction workspace | not applicable |
| inspector/markers/materialization | current-token frontend UI | unavailable for new V2 queries | no Construction overlay/materialization UI | not applicable |
| R6 interaction point persistence | semantic source/address persists; save/reopen/undo/copy use exact provenance | preservation/recompute only | supported document preservation without product interaction | unsupported |
| Continuity | existing host setting locked OFF | locked OFF | upstream configurable | upstream policy |
| document identity | `.cedg` native, `.ggb` compatibility input | same | preserves `.cedg`; Classic default unchanged | `.cedg` unsupported |
| archive internals | current ZIP/XML and `app="classic"` | same | same preservation contract | not evidence of support |
| branding | GeoCeDG topbar/startup roles | same product identity | distinct Classic diagnostic identity | upstream branding |
| workspace/layout persistence | GeoCeDG preferences only | same | separate preferences | not applicable |
| marker/theme/chrome state in XML | never | never | never | not applicable |

## 6. Command/help and current discoverability closeout

G9U1 must close these bounded public-surface inconsistencies without changing
Spline V2 semantics:

- current localized syntax displays only two explicit point placeholders before
  the varargs ellipsis even though the processor requires at least three;
- explicit syntax lookup can return before consulting runtime command filters
  when CAS is allowed, so explicit help can disagree with feature-off discovery;
- the current hard-coded GeoCeDG menu exposes Locus V2 creation, point, metrics
  and inspector but no Spline V2 or command-backed R5 action.

The schema-v2 action registry must correct those inconsistencies through one
command/help/action policy. It must not create a second parser, duplicate command
catalog or alternate transform tool authority.

The post-R6 registry must additionally make every intended semantic Point cell
explicit: Point/Point-on-Object toolbar and menu availability, exact Algebra
command, exact GGBScript form, mouse query/create/drag, ambiguity chooser,
save/reopen, undo/redo, copy/remap, help, and English/Spanish localization. The
mouse path consumes R6 candidates; the command/script path consumes an explicit
semantic address. No empty cell may be filled by an implicit proximity fallback.

## 7. Periodic-quarantine native-roundtrip closure

The tracked
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` risk is closed only by an actual
native-archive lifecycle. Ledger `exportState()/importState()`, copy and
nonperiodic dormant/reactivated archive tests remain supporting evidence but
cannot substitute for it.

The executable fixture shall:

1. create one public periodic intersection group and materialize at least one
   exact-token point while its selector is current;
2. reach periodic quarantine through the real resolver/recompute path, with the
   allocation serialized as `PERIODIC_QUARANTINE` or
   `CLAIMED_PERIODIC_QUARANTINE`;
3. record Construction size, persistent point IDs, exact tokens, deterministic
   selectors and canonical ledger state;
4. Save As native `.cedg` while periodic evidence remains
   insufficient/nonunique;
5. reopen without first changing geometry and prove identical quarantine
   authority, dormant existing points, zero current marker/materialization
   eligibility and zero new GeoPoint;
6. repeat one unresolved native round trip;
7. fork byte-identical archived seeds:
   - unique offset zero releases quarantine and reactivates the same existing
     point/token/ID;
   - proved unique nonzero offset retires the old allocation and never retargets
     it;
   - absent/nonunique evidence remains quarantined;
8. prove feature-off and Classic routes preserve the same native authority
   without enabling creation; and
9. prove no child point causes another global solve and no coordinate,
   proximity, output order or movement history participates.

Only after this passes may the canonical risk record move to resolved. If the
test cannot be completed, the risk remains `OPEN / TRACKED` and requires
explicit author disposition by global G9 closeout.

## 8. Compact G9U1 author smoke

1. Launch GeoCeDG with `--enableLocusV2=true`. Confirm CeDG Construction opens
   with its professional eleven groups, bottom input/help, reviewed GeoCeDG
   visual identity and Continuity visibly locked OFF.
2. Switch English/Spanish and trigger normal font/UI refresh. Confirm menus,
   groups, autocomplete and contextual help remain populated and consistent.
3. Create three points and use `SplineV2(A,B,C)` plus the equivalent point-list
   form. Confirm both semantic splines behave equivalently and help clearly
   communicates the three-point minimum.
4. Use the ordinary Point tool to click the stroke of a straight Locus V2 and a
   Spline V2. Confirm each click creates one interaction-owned semantic point;
   clicking only inside a closed curve creates nothing.
5. Drag both points. Cross a closed Spline V2 seam in both directions and
   confirm the same point/ID/source persists with no duplicate. Return to the
   same final target directly and incrementally and confirm the same semantic
   state.
6. Click a self-intersection. Confirm the deterministic semantic-preimage
   chooser appears, keyboard selection creates the exact chosen candidate, and
   Cancel creates nothing. Confirm EN/ES chooser, unresolved and degenerate
   feedback.
7. Transform one Locus/Spline source by translation, rotation, reflection,
   positive and negative dilation and create/drag a point on the transformed
   source. At `k=0`, confirm a new click creates no arbitrary point while an
   existing point recovers as the same point after restoring nonzero `k`.
8. Invoke the exact Algebra/GGBScript semantic form
   `Point(L,"branch",u)`. Confirm it needs no synthetic mouse API and produces
   the same semantic parent/address model through the command contract.
9. Use total/partial length and one supported one-sided intersection. Select
   the rich result. Confirm only the active result shows current
   admissible markers. Open the inspector by keyboard.
10. In one inspector session create one point, several selected points and all
   remaining eligible points. Confirm already-created choices are identified,
   Cancel creates nothing, opaque tokens do not size the dialog and each
   multi-create is one coherent undo action.
11. Inspect a supported spline×spline rich-only result. Confirm its evidence is
   readable but it offers no selectable marker or create-one/selected/all
   action.
12. Exercise compatible free-input `k=0.25`, undo/redo, `k=0` and recovery.
   Confirm the compatible numeric identity graph is preserved and the collapsed
   image creates no fabricated isolated marker.
13. Test auto-materialization OFF and explicitly ON. Confirm ON creates one
   visible undoable frontend transaction only; later recompute/root appearance
   never creates a new point, while an already-existing dormant point may
   reactivate.
14. Save/reopen `.cedg`; confirm interaction-owned semantic points, their exact
    source/address, drag behavior, intersection tokens and recompute persist.
    Exercise undo/redo and supported copy/remap. Change zoom/DPI and confirm
    semantic identity does not change.
15. Launch the separate Classic diagnostic route. Confirm Classic `Spline` and
    Continuity configurability remain upstream-compatible, supported `.cedg`
    objects preserve, and new `SplineV2` creation is unavailable.
16. Review topbar/startup branding roles, contrast, keyboard focus and normal/
    high-DPI scaling. If the periodic-quarantine fixture is present, also open
    it and confirm its dormant/nonselectable state before the automated
    zero/nonzero disposition branches.

The manual smoke is author evidence only. An agent may prepare it but may not
self-approve G9U1.

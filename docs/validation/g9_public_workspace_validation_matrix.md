# G9 public Locus, product/document refinement and workspace validation matrix

- Status: **G9U0-R2 PASS — AUTHOR APPROVED; G9U0-R3 PASS — AUTHOR APPROVED;
  G9U1 DESIGNED — NOT AUTHORIZED**
- Date: 2026-08-28
- Scope: author-approved/historical G9U0 public Locus V2 evidence;
  author-approved G9U0-R2 product/document-refinement design and implementation;
  bounded G9U0-R3 public-UI exposure hardening; future G9U1
  Construction workspace; and the
  post-G9 G9U2 Dihedral Procedures workspace
- G9U0-R2 implementation: **PASS — AUTHOR APPROVED**;
  G9U0-R3: **PASS — AUTHOR APPROVED**;
  G9U1/G9U2 implementation authorized: **no**

The G9U0 rows remain the design source behind separately frozen, author-approved
G9U0/G9U0-R1 execution evidence. The author separately invoked the canonical
R2 prompt; the `R2-*` rows govern its closed implementation. Replacement focused A/B,
historical, packaging, Checkstyle, diff and composed results pass after the R2-L11
correction; the author accepted the corrective re-smoke. The original
author-smoke failure is preserved. These rows do not imply workspace switching,
Dihedral procedures, an installed MSI/registry smoke or public redistribution.

## 1. Entry gates

| ID | Gate | Required evidence | Failure disposition |
|---|---|---|---|
| E-01 | G6 semantic baseline | focused G6 authority passes without changed semantic baselines | stop G9U0 |
| E-02 | G7 metric baseline | G7A/G7B focused authorities and scientific references pass | stop metric publication |
| E-03 | G8 intersection baseline | G8A/B/C1/C2 focused authorities and author-approved evidence pass | stop intersection publication |
| E-04 | composed repository | current composed authority passes from clean tree | classify environment vs regression before code |
| E-05 | design approval | relevant specs normative and ADRs accepted by author | satisfied by G9P closeout; phase authorization remains separate |
| E-06 | identity/lifecycle | durable general object identity and XML lifecycle contract approved | no persistent V2/public tool |
| E-07 | immutable references | hashes in `g9p-reference-inputs.json` match raw bytes | stop; do not resave inputs |
| E-08 | workspace dependency | G9U0 PASS before public Locus actions enter Construction | disable actions |
| E-09 | procedure dependency | global G9 PASS before G9U2, absent narrower explicit approval | workspace remains unavailable |
| E-10 | pre-G9U1 product/document refinement | G9U0-R2 implementation, focused/composed evidence and manual author smoke closed `PASS — AUTHOR APPROVED` | G9U1 remains `DESIGNED — NOT AUTHORIZED`; implementation PASS still requires a separate U1 authorization |
| E-11 | public Locus UI exposure | G9U0-R3 focused/deterministic/historical/composed evidence plus manual author smoke closed `PASS — AUTHOR APPROVED` | G9U1 remains `DESIGNED — NOT AUTHORIZED`; R3 candidate cannot authorize it |

G9U1 is a GUI client/organization phase for already authorized actions. It has
no semantic dependency on G9B, and G9B must not depend on G9U1. The
author-approved R2 implementation and a future author-approved R3 closeout are
product-entry prerequisites for U1, not G9B dependencies. G9U2 is
the workspace that consumes approved G9 spatial/projection-system semantics and
therefore remains behind the global G9 gate.

## 2. G9U0 creation, command and dependency tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U0-C01 | legacy command unchanged | load/execute representative `Locus[Q,P]` under GeoCeDG and Classic | legacy class, algorithm, XML and results unchanged |
| U0-C02 | explicit V2 command | inspect actual GeoGebra overload/localization/XML conventions, compare alternatives, then parse the G9U0-selected author-reviewable point/scalar spelling and normalize to typed \(\mathcal{G}:D\to S\) | exactly one reconstructible V2 parent; no legacy fallback and no G9P example treated as frozen syntax |
| U0-C03 | feature-off command | invoke from algebra input with `cedg.locus.v2=false` | localized unavailable result; no construction mutation |
| U0-C04 | feature-off file load | open an approved V2 fixture while interactive creation is off | policy-defined preserve/recompute behavior; no command-filter loss |
| U0-C05 | argument/provider matrix | scalar identity/map forms and point on segment/circle/arc/V2; include bare support, generic Path, legacy locus and ambiguous driver | accepted matrix exact; every rejection typed/localized |
| U0-C06 | scalar semantic domain | bounded visible/hidden slider, free numeric, dependent expression and mapped scalar; endpoint/orientation/periodic/discontinuity variants | explicit true coordinate/domain/mapping; slider visibility irrelevant; no illegal dependent mutation |
| U0-C07 | point-support providers | constrained point with registered segment/circle/arc/V2 support inputs; reverse, collapse, seam, branch/component variants | canonical preimage/orientation/continuation or typed degeneration |
| U0-C08 | dependency completeness | mutate each external input in the captured construction slice | exactly one normal dependency recompute; output current |
| U0-C09 | evaluator restoration | inject undefined evaluation/exception/work limit during driver transaction | live construction/driver restored deterministically |
| U0-C10 | render independence | vary zoom, pan, DPI, graphics size and render sampling | semantic revision/value/intersections unchanged |
| U0-C11 | command identity | inspect Construction Protocol/XML parent | real reconstructible command/algo identity, not `Algos.Expression` |
| U0-C12 | label independence | relabel source/driver/locus | durable identity and semantic binding unchanged |
| U0-C13 | duplicate archive UUID | load fixtures sharing the reference UUID | no cross-document identity collision |
| U0-C14 | branch topology change | dynamic create/loss/split/merge fixtures | semantic revision/lineage/status deterministic; no stale child |

## 3. G9U0 R1 one-dimensional generator suite

These 22 rows are explicit entry evidence, not coverage inferred from other
tests. The IDs preserve the numbered cases required by R1.

| ID | Required case | Test/probe | Expected evidence |
|---|---|---|---|
| U0-G01 | bounded scalar slider | evaluate identical construction with visible and hidden bounded slider | same explicit domain, semantic identity/revision and results; no `isSlider()` authority |
| U0-G02 | free scalar with explicit semantic domain | free numeric plus finite oriented domain, including excluded endpoint | accepted without GUI slider state; endpoint policy serialized |
| U0-G03 | dependent scalar expression | `t=f(s)` with registered external parameter | evaluator varies isolated `s`; normal graph computes `t,Q`; live dependent geo is never assigned |
| U0-G04 | scalar mapped from another driving parameter | non-injective `t=s^2`, periodic map and discontinuous map with explicit components | `s` remains the address; equal state/coordinate does not merge preimages; discontinuity truthful |
| U0-G05 | point on finite segment | endpoints, reverse, collapse and interior move | canonical `[0,1]` address and typed degeneration |
| U0-G06 | point on circle | complete traversal in both orientations | angular fundamental domain, stable support identity and explicit seam |
| U0-G07 | point on circular arc | positive/negative orientation, endpoints, wrap representation and collapsed arc | support/start/extent inputs reconstruct; local coordinate and degeneration deterministic |
| U0-G08 | point on Locus V2 | explicit branch/component position on current source | ordinary point retains durable source preimage plus revision binding; no generic Path |
| U0-G09 | `LocusV2 -> point -> LocusV2` | construct outer dependent locus from bound source point and mutate source inputs | all edges in normal DAG; outer revision invalidates/recomputes coherently |
| U0-G10 | nested Locus V2 depth greater than one | at least three acyclic source/point/locus levels and several branches | supported within work policy; deterministic traversal/evaluation order |
| U0-G11 | self-intersection, two preimages | choose both parameters where `F(s1)=F(s2)`, `s1 != s2` | two durable addresses and two selectable points despite identical coordinate |
| U0-G12 | disconnected components | bind points in two valid components, then recompute | component evidence distinct; no Cartesian hopping |
| U0-G13 | branch loss | remove bound branch/component dynamically | point and dependent outer locus undefined/noncurrent; no stale value |
| U0-G14 | periodic seam traversal | cross the chosen circle/V2 seam repeatedly and reopen | canonical evaluation plus persisted lift/wrap and seam direction preserve preimage |
| U0-G15 | continuation ambiguity | deterministic split/merge/provider-change fixture | explicit ambiguous continuation; point and downstream locus do not retarget |
| U0-G16 | direct dependency cycle rejection | attempt source locus to bound point back to same locus during create/redefine/load | ordinary Construction DAG rejects before semantic evaluation |
| U0-G17 | indirect dependency cycle rejection | attempt `L1 -> P1 -> L2 -> P2 -> L1` | ordinary transitive DAG rejects; no hidden generator graph |
| U0-G18 | save/reopen | round trip every scalar/support provider, including nested periodic point | generator mapping/domain and durable preimage restored; revision binding recomputed |
| U0-G19 | copy | copy nested locus/point/query subtree within and across constructions | owned durable IDs rewritten, declared external support policy honored, no collision |
| U0-G20 | undo/redo | create/move/redefine/delete nested generator and undo/redo | same operation identity restored and continuation/result deterministic |
| U0-G21 | deterministic nested-query counters | sweep depth \(d\), semantic queries \(q\), branches and cache limits twice | bounded declared `q*d`-style semantic calls, stable build/hit/eviction/order counters, no hidden quadratic work |
| U0-G22 | no render/sample/viewport authority | change zoom, pan, DPI, tessellation, render visibility and render cache state | zero semantic render/sample reads; identical generator, metric/intersection and continuation evidence |

## 4. G9U0 semantic-position and metric tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U0-M01 | explicit semantic point | create `Point[L,branch,t]` or approved spelling and use it as outer-locus generator | ordinary point plus durable preimage address and revision/component/continuation binding |
| U0-M02 | self-intersection ambiguity | click/create at one Cartesian point with multiple preimages | user selects preimage; no coordinate-only binding |
| U0-M03 | stale position | change provider version/topology/periodic seam | point/metric/outer locus reports stale or ambiguous; no nearest repair |
| U0-M04 | total rich length | public rich total query on segment, circle, ellipse and analytic fixtures | value, coverage, rectifiability, method, guarantee, error and work visible |
| U0-M05 | partial rich length | same branch, cross component, cross branch, reversed orientation, periodic wrap | route/traversal follows normative G7 policy exactly |
| U0-M06 | guarded standard scalar | exercise every `isScalarAdmissible()` predicate through approved `Length[GeoLocusV2]` surface | child/reused rich query; number only for finite+complete+success+rectifiable+admissible guarantee/traversal; no second calculation |
| U0-M07 | nonrectifiable/infinite | cusp/improper/unbounded/nonrectifiable fixtures | rich result truthful; scalar undefined |
| U0-M08 | incomplete/error status | deterministic work/limit/error fixtures | rich result retained; no exact claim or silent finite scalar |
| U0-M09 | zoom/DPI invariant metric | metric before/after view changes | bitwise/deterministically equivalent semantic result per policy |
| U0-M10 | legacy Length | `Length[GeoLocus]` regression | retained sample-count compatibility, documented as legacy only |
| U0-M11 | developed/direct metric distinction | graft-style isometric development fixture | direct semantic length and derived-development measurement identified separately |

## 5. G9U0 intersection and token tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U0-I01 | general command integration | V2 with line, segment, ray, circle, ellipse, parabola, hyperbola, bounded function, regular polynomial implicit and V2 | one rich result for each approved G8 family |
| U0-I02 | baseline dispatch unchanged | representative non-V2 pairs through command and mode `5` | existing output types/counts/selection behavior unchanged |
| U0-I03 | empty complete | certified empty case | defined rich `EMPTY/COMPLETE`; zero point actions |
| U0-I04 | finite complete | transverse one/many-root cases | complete solution set, exact candidate tokens and residual evidence |
| U0-I05 | incomplete/not established | bounded deterministic coverage-limit case | known solutions labelled incomplete; only locally admissible tokens enabled |
| U0-I06 | unisolated root | local isolation not established | inspector retains evidence; point action disabled |
| U0-I07 | tangent | analytic and near-tangent cases | tangent/contact evidence; even-multiplicity root not lost |
| U0-I08 | overlap | exact/suspected/unsupported overlap fixtures | typed overlap, no fabricated point list |
| U0-I09 | mixed finite+overlap | fixture containing both | isolated tokens selectable; overlap remains separate |
| U0-I10 | work limit | deterministic operation-budget exhaustion | `WORK_LIMIT_REACHED`, work evidence, no stale/partial exact claim |
| U0-I11 | token overload | materialize point from exact token | child stores token, depends on rich result, never solves |
| U0-I12 | duplicate/unknown token | malformed, absent and duplicate token probes | point undefined; typed diagnostic |
| U0-I13 | proximity (historical G9P design row; not implemented by G9U0) | future click near two admissible presentation markers, then change view | G9U0 established opaque exact-token identity only; graphical proximity/marker ranking belongs to G9U1 and may never establish identity |
| U0-I14 | stable continuation | perturb source without topology change | same token/point identity and correct coordinate update |
| U0-I15 | ambiguous continuation | merge/split/equidistant continuation fixture | old point undefined; no automatic retarget |
| U0-I16 | revision mismatch | stale result/point update ordering probe | prior payload unavailable before work; no stale coordinate frame |
| U0-I17 | source order | `Intersect[L1,L2]` and reversed order | canonical pair identity and documented orientation mapping |
| U0-I18 | unsupported/unbounded | unbounded V2 pair, unsupported target/domain | typed unsupported result; no viewport-dependent truncation |

## 6. G9U0 lifecycle, persistence and compatibility tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U0-P01 | save/reopen locus | XML round trip for every scalar map and segment/circle/arc/V2 point provider | durable generator/locus IDs, true driver/support, mapping/domain and policy restored; result recomputed |
| U0-P02 | save/reopen metric | total and partial rich results | query/positions/policy restored; cached numeric snapshot not authority |
| U0-P03 | save/reopen intersection | every target family and V2 pair | source identities/policy/lineage restored; current result recomputed |
| U0-P04 | save/reopen bound/token point | semantic preimage and exact-token points under stable/topology-changed fixtures | established binding/token updates; ambiguous/non-established point and dependents undefined |
| U0-P05 | deterministic rerun | open/save/recompute twice from identical bytes/policy | deterministic semantic evidence and XML canonical fields |
| U0-P06 | copy | duplicate locus with metric/intersection/point children | new locus ID; internal references rewritten; original unaffected |
| U0-P07 | copy across construction | insert/copy into another document | no ID collision or cross-construction reference |
| U0-P08 | set/assignment | approved set cases and invalid types | explicit safe semantics; no ordinary workflow exception |
| U0-P09 | delete source | delete locus/driver/target/rich result | children removed/undefined per contract; no stale payload/cache leak |
| U0-P10 | undo/redo | create, select token, delete, undo and redo | same restored operation identity; deterministic result/point |
| U0-P11 | relabel/style/layer | mutate presentation metadata | semantic identity/revision unaffected unless true input changed |
| U0-P12 | old-file compatibility | open all four immutable G9P GGBs and canonical old Locus files | no auto migration; legacy outputs preserved |
| U0-P13 | GeoCeDG Classic path | open/save/reopen native V2, rich result and bound/token-point files in the fork Classic launcher | same native types, IDs/tokens/bindings and kernel recomputation; creation unavailable; zero legacy/list/coordinate downgrade |
| U0-P14 | unsupported external upstream | save V2-bearing file for interchange and characterize open in an upstream distribution without the persisted types | explicit unsupported compatibility boundary/warning; no silent sampled or legacy conversion workaround |
| U0-P15 | unknown semantic version | synthetic future-version fixture | preserved unsupported state; no guessed reconstruction |
| U0-P16 | corrupt/partial XML | missing ID/provider/token/query inputs | deterministic load diagnostic; no fabricated identity |

## 7. Runtime feature, localization, help and tool tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U0-F01 | one runtime decision | compare algebra command, toolbar, menu/help and file-load policy for each flag state | no contradictory availability |
| U0-F02 | default off | clean preferences/installation | no experimental creation action or command availability beyond declared policy |
| U0-F03 | Classic separation | same flags in GeoCeDG and GeoCeDG Classic | profile-specific creation policy; native preservation/load policy explicit and semantically identical |
| U0-F04 | Laboratory/dual run | diagnostic activation | visibly experimental; stable defaults unchanged |
| U0-L01 | command localization | every new syntax/argument/error key in approved locales | no hard-coded public strings; fallback diagnostics explicit |
| U0-L02 | status localization | every rich status and selection slot | correct typed placeholders and accessible text |
| U0-L03 | icon provenance | inspect action/icon manifest | GeoCeDG-owned/reviewed asset or text fallback; no copied Template icon |
| U0-L04 | contextual help | legacy Locus versus V2 commands/tools | semantics and compatibility distinction explicit |
| U0-T01 | creation selection | valid/invalid order, preview, cancel and workspace switch mid-selection | declared grammar; no partial construction |
| U0-T01A | point-on-Locus selection | choose each branch/preimage/component, including identical Cartesian candidates and seam | exact semantic address committed; no render vertex or coordinate-only point |
| U0-T02 | general Intersect selection | all old types plus every V2 family | old paths delegate unchanged; V2 creates rich result |
| U0-T03 | result chooser accessibility | keyboard, mouse, screen-reader labels, several/tangent/overlap cases | exact token selection available without color/proximity alone |

## 8. G9U0-R2 product/document-refinement tests — PASS — AUTHOR APPROVED

These rows are **AUTHOR-APPROVED DESIGN AND IMPLEMENTATION EVIDENCE**. They do
not alter the author-approved G9U0/G9U0-R1
evidence. The original author smoke failed R2-L11; its exact reproduction and
bounded correction extend that row without changing the frozen 31+31 counts.
Replacement focused A/B, supporting histories, ancillary static rows and
composed verification pass; the corrective re-smoke and implementation are
`PASS — AUTHOR APPROVED`.

### 8.1 Locus V2 ordinary presentation and render continuity

| ID | Requirement | Candidate test/probe | Expected evidence |
|---|---|---|---|
| R2-L01 | color | set several ordinary object colors through Properties/style API and repaint | ordinary `GeoElement` color used by `DrawLocusV2`; exact durable ID, semantic revision, domain and results unchanged |
| R2-L02 | line thickness | set minimum/default/large supported thickness and repaint at several scales | ordinary thickness/stroke used; no semantic/cache publication and no centerline discontinuity |
| R2-L03 | line type | exercise continuous and supported dashed/dotted types | ordinary line type persists; intentional dash gaps are identified as stroke presentation, never semantic components |
| R2-L04 | Properties exposure | select public V2 alone and in compatible multi-selection | ordinary line-style controls available through the host capability; no generic `Path` or unsupported controls exposed |
| R2-L05 | presentation persistence | set color/thickness/type, ordinary visibility and applicable label presentation; save native `.cedg` and reopen twice | exact supported persistent presentation fields restored through normal XML; same reconstructed semantic parent and durable identities |
| R2-L06 | copy | supported copy within/across constructions after styling | ordinary visual style copied; owned durable IDs remapped per G9U0; original semantics/style unaffected |
| R2-L07 | undo/redo | change each undoable presentation property, undo and redo where the host records it | presentation restored deterministically; no semantic revision, metric/intersection recompute or new operation identity |
| R2-L08 | semantic invariance | snapshot identity/revision/generator/domain/branches/components/metrics/intersections/solution tokens/semantic DAG, mutate every presentation property, compare | all semantic evidence identical; render/style counters only may change |
| R2-L09 | crossing line | capture fixed-policy render data, add/move/style/delete a crossing line and repeat | same locus semantic revision, vertices and subpath markers; ordinary z-order/overdraw may cover pixels but creates no gap |
| R2-L10 | crossing circle/conic | repeat R2-L09 with a circle and each supported conic crossing one/multiple times | crossing count and target style have zero authority over Locus render topology or semantic intersections |
| R2-L11 | no artificial renderer subpath | preserve the exact author-smoke `locusFromMidpoint.cedg` bytes/SHA; prove its single half-open complete periodic component reaches the seam under fixed and adaptive policies before/after real line/circle/conic crossings; retain minimized nonperiodic-open and disconnected controls | full-period render predicate requires periodic provider + `PERIODIC` branch + exactly one component equal to both declared branch/provider domains; first/last presentation points coincide with one subpath and unchanged identity/revision/fingerprints; no closure of an ordinary open interval, merger of disconnected components or crossing-induced gap |
| R2-L12 | genuine discontinuity preserved | disconnected valid components plus open/unbounded clipping, solid/dashed strokes and overdraw | real components/invalid gaps retain distinct subpaths; clipping, overdraw and dash gaps remain separately diagnosed |
| R2-L13 | ordinary show/hide | toggle visibility through ordinary object/UI paths, including save/reopen and undo/redo where applicable | existing `GeoElement` visibility authority used; hidden state affects presentation only and semantic evidence is unchanged |
| R2-L14 | applicable label presentation | exercise supported label visibility/mode/style, copy and native round trip | ordinary label authority used where applicable; no label-derived identity, branch or semantic revision |
| R2-L15 | selection/highlight | select, deselect, multi-select and hover/highlight at crossings and discontinuities | normal transient highlight path used; no persisted selection authority, semantic mutation or render-subpath change |

### 8.2 Native `.cedg` and `.ggb` compatibility-input behavior

| ID | Requirement | Candidate test/probe | Expected evidence |
|---|---|---|---|
| R2-D01 | default Save | save a new unsaved construction | native Save As invoked; filename ends in one lowercase `.cedg` |
| R2-D02 | Save As and omitted suffix | Save As from new/native/input documents with explicit `.cedg`, no suffix and conflicting suffix | `.cedg` is the only native target; omitted suffix appended once; conflicting suffix handled visibly, never silently rewritten |
| R2-D03 | native reopen | save `.cedg`, close/reopen and save again | same ZIP/XML semantics, current native path and full construction restored |
| R2-D04 | mixed-case host handling | open `.CEDG`/mixed-case variants and test overwrite collision on Windows | case-insensitive input/collision handling; new output normalized to `.cedg` |
| R2-D05 | recent/direct-open surface | chooser, recent menu, drag/drop, command line and Windows shell open for `.cedg` and `.ggb` | all routes classify source consistently and delegate to the validated reader |
| R2-D06 | legacy `.ggb` open | open canonical old GGBs, G9P references and current GeoCeDG GGB fixtures | compatibility input loads without auto migration; legacy/current semantics preserved |
| R2-D07 | non-destructive `.ggb` to native save | open `.ggb`, invoke Save and select a distinct `.cedg` | native Save As required; successful target becomes current; no `.ggb` overwrite path |
| R2-D08 | source unchanged / cancel / failure | hash source before and after successful transition, cancel and injected write failure | exact source bytes/path unchanged; cancellation/failure leaves live construction and source classification usable |
| R2-D09 | corrupt, live-parse-failing or undo-commit-failing `.cedg` | truncated ZIP, missing `geogebra.xml`, malformed XML and unsupported semantic version; plus a preflight-admitted archive forced to fail during live parse and at the prepared undo-baseline commit seam | localized fail-closed diagnostic; no partial construction, fabricated ID or file rewrite; the prior live construction, file/path, saved/recent/loading state and complete undo/redo history are restored before publication; failed prepared-baseline cleanup is nonthrowing and stale asynchronous stores cannot republish old history |
| R2-D10 | Locus V2 persistence | native round trip of scalar/support/nested/periodic/disconnected V2 fixtures plus styles | full approved generator/preimage/branch semantics and visual style restored |
| R2-D11 | rich result/token persistence | native round trip of metric result, intersection result and exact-token point under stable/stale topology | reconstructible inputs/tokens restored and recomputed; no cached snapshot or coordinate/list downgrade |
| R2-D12 | G9 durable identity persistence | native round trip of G9A geo/spatial/frame/system/map/relation/binding corpus | exact durable identities, roles, revisions and broken-reference states follow G9A contracts |
| R2-D13 | extension is not semantics | copy identical archive bytes under `.ggb` and `.cedg`, then inspect load state without migration | suffix changes only I/O classification; no type, ID, feature or XML semantic inference |
| R2-D14 | GeoCeDG Classic preservation | direct-open/save/reopen supported `.cedg` plus compatibility `.ggb` in the separate fork Classic process | opened `.cedg` remains `.cedg` and native types/IDs/tokens are preserved; creation/preferences remain isolated; accepting `.cedg` does not change Classic's default new-document identity |
| R2-D15 | external upstream boundary | characterize `.cedg` and `.ggb` with GeoCeDG-only types in controlled upstream reader where permitted | explicit unsupported boundary; no automatic rename, flattening, legacy-locus or coordinate/list conversion |
| R2-D16 | archive/app code unchanged | inspect normalized entry inventory, canonical XML header and semantic versions across native reruns | validated entries/XML retained; `app="classic"`; no filename-derived format branch |
| R2-D17 | Windows packaging/association | static package profile plus MSI/EXE registry probe where explicitly built; app-image/ZIP inspection | Windows installers associate `.cedg` with a GeoCeDG-owned ProgID; no GeoCeDG `.ggb` claim; portable artifacts add no association; the JDK-required internal unregistered MIME input is not document semantics or a cross-platform claim; no non-Windows validation claim |

`R2-D10`–`D12` and `D14` are not satisfied by their short Desktop routing
markers alone. The scenario inventory binds `D10` to G9U0 persistence `p01`/
`p05` plus the two R2 native-style reopen methods; `D11` to G9U0 `p02`, `p03`,
`p04` and atomic `p16`; `D12` to G9A3 native `compat01`, XML `xml10` and the
full `R2-R05` spatial corpus; and `D14` to the Desktop Classic round trip,
G9U0 `p13` and G9A3 native `compat02`/`compat03`.

### 8.3 Required regression gates

| ID | Required authority | Candidate command responsibility | Pass condition |
|---|---|---|---|
| R2-R01 | G9U0-R1 | run its focused verifier from the accepted descendant state | all R1 Desktop/shared cases and frozen boundary pass unchanged |
| R2-R02 | historical G9U0 | run the frozen public-surface authority, including persistence/copy/Classic | all historical scenarios pass; evidence/report remains unmodified |
| R2-R03 | G9X1 | run extended-DXF focused authority | all exact/approximate/sidecar/G5 regressions pass; no document/style coupling |
| R2-R04 | G5 | execute exact DXF corpus | exact mappings and external service boundary unchanged |
| R2-R05 | relevant G9A | run A1–A3 identity/persistence/lifecycle authorities applicable to native round trip | IDs, copy, redefine, migration and Classic behavior unchanged |
| R2-R06 | legacy Locus | run legacy command/XML/render/save corpus | legacy type, sampled behavior, style, open/save and dispatch unchanged |
| R2-R07 | composed repository | run `tools/agent/verify.ps1` after focused deterministic reruns | every existing gate plus R2 integration passes without `-SkipBuild` for candidate closeout |

### 8.4 Deterministic rerun and evidence

The candidate focused authority is
`tools/agent/verify-g9u0-r2-product-refinement.ps1`. It must run twice from the
same fixture bytes and policy, writing separate log directories, and compare:

- semantic/durable IDs and revisions;
- branch/component and fixed-policy render-subpath evidence;
- supported visual-style XML;
- canonical XML plus normalized archive-entry content hashes;
- `.ggb` source hash before/after native save;
- typed I/O transitions and diagnostics; and
- exact scenario/test/counter totals.

ZIP byte identity is not claimed unless timestamps and all archive metadata are
separately normalized and verified. The required durable evidence paths
are under `geocedg/validation/g9u0-r2/`; generated run logs remain below
`artifacts/g9u0-r2/`.

The paired composed integration is present after the existing G9X1 block and
before any future G9U1 block. It uses the existing paired-artifact and
`OPEN_PENDING_IMPLEMENTATION_FREEZE`/`FROZEN` pattern, rejects partial
integration and never interpret a scaffold PASS as a productive phase PASS.

### 8.5 Manual author smoke and corrective re-smoke record

The author already executed this checklist once: all reported areas except
R2-L11 passed, and R2-L11 produced the preserved visible-gap reproduction.
That attempt remains failed evidence. After every replacement automated tuple
was clean, the author repeated the focused continuity observations and accepted
the complete applicable smoke/re-smoke.

| Step | Author action | Observation to record |
|---|---|---|
| M-01 | create and dynamically move the approved circle-driven Locus V2 workflow | existing public creation/lifecycle remains healthy |
| M-02 | use Properties to change color, thickness, line type, show/hide and applicable label presentation; exercise selection/highlight | controls/presentation are ordinary and responsive; no semantic result changes |
| M-03 | cross the locus with a line and a circle/conic, then move all objects | no artificial gap/component/subpath; ordinary overdraw/dashes identified correctly |
| M-04 | inspect a genuine disconnected-component fixture | real discontinuity remains visibly and diagnostically distinct |
| M-05 | copy the styled locus and undo/redo style changes where applicable | style lifecycle correct; semantic identity policy preserved |
| M-06 | Save As with no suffix, reopen `.cedg` and inspect V2/rich/token/G9 identity/style | native round trip complete |
| M-07 | open a copied `.ggb`, invoke Save to `.cedg`, cancel once and complete once | Save As transition visible and original source hash/path unchanged |
| M-08 | open/save/reopen `.cedg` in GeoCeDG Classic and try a corrupt `.cedg` | no downgrade or creation enablement; Classic new-document default unchanged; corrupt load does not replace the live document |
| M-09 | when explicitly available, install/test MSI or EXE shell association | `.cedg` opens GeoCeDG; GeoCeDG has not claimed `.ggb` |

The author marked M-01 through M-08 passed. M-09 was `NOT_REQUESTED`; no installed
MSI/registry result is inferred. `selfApproved=false`, `authorApproved=true`,
`passClaimed=true`.

### 8.6 G9U0-R3 public-UI exposure hardening

These rows govern the bounded R3 implementation candidate. They do not alter
the frozen G9U0/R1/R2 evidence and do not implement G9U1 candidate markers.

| ID | Requirement | Focused test/probe | Expected evidence |
|---|---|---|---|
| R3-M01 | init/full rebuild | initialize and reinitialize real GeoCeDG menubar | exactly one populated GeoCeDG menu |
| R3-M02 | one font update | call inherited Desktop font lifecycle once | all approved items remain visible/enabled |
| R3-M03 | repeated font updates | call lifecycle repeatedly | idempotent population; no loss/duplication |
| R3-M04 | localization refresh | switch locale through normal app path | localized action text rebuilt; menu nonempty |
| R3-M05 | feature ON | launch with only `--enableLocusV2=true` | five approved Locus actions plus independent DXF action |
| R3-M06 | feature OFF | launch without V2 opt-in | approved unavailable policy; no hidden intersection flag |
| R3-I01 | selected rich result | click actual menu inspector with Algebra-selected intersection result | controller/dialog route reached for selected result |
| R3-I02 | sole unselected result | one rich result in Construction, no selection | result discovered and inspected |
| R3-I03 | several results | several rich results, no selected authority | deterministic chooser behavior |
| R3-I04 | cancel | cancel admissible-token dialog | zero point and token-child creation |
| R3-I05 | explicit accept | accept one admissible exact token | exactly one ordinary point; rich+token exact inputs |
| R3-I06 | recompute | perturb source under established continuation | existing token point recomputes; no re-solving/fallback |
| R3-I07 | undo/redo | materialize, undo and redo | normal Construction lifecycle |
| R3-I08 | metric support | invoke same inspector for metric rich result | existing metric diagnostic preserved |
| R3-I09 | no result | invoke inspector without rich result | existing typed message preserved |
| R3-I10 | bounded long-token chooser | inspect two admissible solutions with deliberately long valid exact tokens | compact localized labels remain distinct/accessibly named and preferred width is independent of token length |
| R3-I11 | exact-token/presentation separation | select the second compact entry, materialize, then save/reopen | complete selected token is used and persisted unchanged; transient ordinal/label is neither identity nor XML data |
| R3-T01 | auxiliary presentation | inspect materialized token helper | auxiliary exact `GeoText` is not Euclidian-visible |
| R3-T02 | native persistence | save/reopen `.cedg` with token point | exact hidden dependency reconstructed |
| R3-T03 | copy/remap | closure-copy result/token/point | approved remapping and hidden presentation preserved |
| R3-N01 | no automatic point | execute `Intersect(L,T)` only | zero persistent point children |
| R3-N02 | no marker scope | inspect rich result/Construction after R3 workflow | no drawable rich result or candidate-marker GeoElement |

The focused verifier executes these 22 cases plus 17 existing profile,
runtime-feature, localization/help and tool-surface cases. Two runs must produce
byte-identical canonical summaries. G9U0, R1, R2, G9X1, G5, relevant G9A,
legacy Locus, Checkstyle, Git diff checks and composed verification must pass.
The author smoke was functionally passing but exposed the dialog-width defect;
the bounded correction requires a separate author re-review.

## 9. G9U1 workspace schema and behavior tests

| ID | Requirement | Test/probe | Expected evidence |
|---|---|---|---|
| U1-S01 | schema v2 | validate accepted/rejected fixtures, unknown fields, duplicate IDs | deterministic strict validation |
| U1-S02 | v1 migration | load current profile v1 through in-memory adapter twice | same Construction definition/order; no file write |
| U1-S03 | action references | missing, duplicate, cyclic-feature and wrong-kind fixtures | fail closed with exact diagnostic |
| U1-S04 | single authority | static scan/profile tests | no duplicate hard-coded product toolbar/menu strings |
| U1-S05 | symbolic modes | resolve every upstream action target | ID maps to audited constant; future actions do not reserve arbitrary IDs |
| U1-W01 | Construction defaults | fresh profile visual/structural test | Graphics, Algebra, Protocol, bottom input/help; Properties policy as approved |
| U1-W02 | group order/mapping | inspect emitted toolbar/menu | exact approved groups/action IDs and no duplicate action |
| U1-W03 | algebra availability | run hidden toolbar commands through input | independently authorized command remains available |
| U1-W04 | feature-disabled action | Construction with V2 flag off | hidden/disabled behavior matches manifest and command gate |
| U1-W05 | workspace switch purity | switch repeatedly with populated construction | zero construction changes, zero undo entries, zero semantic evaluations |
| U1-W06 | active selection | switch during multi-slot tool | transaction cancelled, Move active, existing geometry unchanged |
| U1-W07 | per-workspace preferences | customize docks, restart, alternate workspaces | correct isolated layout restored deterministically |
| U1-W08 | unknown workspace preference | inject removed/future ID | localized fallback to Construction; no semantic effect |
| U1-W09 | document layout | open native `.cedg` plus each immutable reference `.ggb` compatibility input | identified transient Document layout; product manifest and document semantics unchanged |
| U1-W10 | reapply workspace | reapply Construction after native or compatibility document layout | construction/object/style bytes and dependencies unchanged; no save-format transition caused by workspace switching |
| U1-W11 | GeoCeDG Classic route | invoke diagnostic action with controlled settings and supported `.cedg`/`.ggb` semantic files | separate process/profile/preferences; current GeoCeDG state unchanged; opened `.cedg` and native semantic types/IDs preserved with no downgrade; accepting `.cedg` does not change Classic's default new-document identity |
| U1-W12 | Laboratory route | open Templatev7 explicitly | hash/ingest checks; legacy toolbar remains document context only |
| U1-W13 | startup manifest failure | invalid packaged v2 fixture | explicit fallback to last accepted v1; no hidden toolbar authority |
| U1-W14 | switch rollback | inject view/menu compilation failure | old workspace restored; construction untouched |
| U1-W15 | public V2 action coverage | inspect Construction action catalog after G9U0 PASS | creation, rich/guarded length, general V2 intersection, token point and supported point-on-Locus actions present as GUI clients only |
| U1-W16 | no G9B coupling | compile/activate Construction with G9B unavailable but already authorized nonspatial actions available | workspace works; no G9B dependency or semantic fallback |
| U1-W17 | consume R2 document policy | exercise Save/open actions before/after workspace switches on native and compatibility inputs | one application-owned `.cedg`/`.ggb` state machine; manifest does not duplicate or override it |
| U1-L01 | workspace localization | names/groups/help/blocked reasons in approved locales | no product literal drift; deterministic fallback |
| U1-L02 | icon/accessibility | missing icon and all action focus paths | text fallback, accessible name, keyboard navigation |
| U1-V01 | visual density | reference-size and small/high-DPI layouts | groups usable without giant embedded icons; overflow deterministic |
| U1-V02 | screenshot comparison | human review against supplied evidence | workflow/panel access accepted; no pixel-copy requirement |
| U1-I01 | active rich-result markers | select one rich intersection result containing finite admissible, stale, unresolved, overlap-only and inadmissible states | only currently finite + point-admissible solutions of the active result receive default-on transient markers |
| U1-I02 | inactive-result suppression | retain several rich results and change Algebra/Graphics selection | only the active/selected result contributes markers; inactive history does not pollute Graphics |
| U1-I03 | marker presentation purity | inspect Construction, XML, Protocol, undo and copy closure before/after marker display | marker is no GeoElement, ID, XML, DAG/Protocol/undo/copy object and causes zero semantic revision |
| U1-I04 | exact marker selection | click/rank markers under zoom/DPI/view changes and close solutions | hit/rank preselects an already admissible exact token; coordinate/order/proximity never establishes identity |
| U1-I05 | explicit one-point materialization | confirm one selected admissible marker/token, then undo/redo | exactly one ordinary exact-token point; normal single user action; no re-solving or fallback |
| U1-I06 | explicit create-all | invoke optional create-all on mixed current result, then undo/redo | exactly all and only currently point-admissible tokens materialize through exact dependencies; never automatic |
| U1-I07 | marker preference | toggle active-result marker preference and restart workspace | Construction default is ON; explicit OFF suppresses overlay only and changes no Construction state |
| U1-B01 | Construction visual identity | compare normal Construction and separate Classic diagnostic route | restrained professional identity is immediately distinguishable; Classic retains diagnostic identity |
| U1-B02 | theme/accent purity | change supported theme/accent/workspace style with populated Construction | zero Construction/DAG/semantic/undo updates; presentation colors are never geometric authority |
| U1-B03 | accessibility/scaling | contrast audit plus normal DPI/scaling and keyboard paths | accepted contrast, readable status/help, stable focus/order and usable professional grouping |
| U1-A01 | top-bar role provenance | resolve `geocedg.brand.topbar` and supplied `helixTopBar.png` from the asset manifest | one canonical source filename/hash/dimensions/format-alpha/provenance authority; frontend chrome only; explicit fallback if absent |
| U1-A02 | startup role provenance and suitability | resolve `geocedg.brand.startup` and supplied `helixSnapshot.png`; inspect every claimed icon size | one canonical source filename/hash/dimensions/format-alpha/provenance authority; 16x16/32x32 suitability proved rather than assumed |
| U1-A03 | deterministic brand derivatives | regenerate required startup/application Desktop PNG sizes and Windows ICO twice | identical bytes/hashes from recorded tool/version/options; no hand-copied packaging duplicates or upstream substitute |
| U1-A04 | brand consumers | inspect top bar separately from startup/frame and, where claimed, app package and `.cedg` association | each logical role resolves only in its approved seam; `.ggb` unclaimed; Classic distinction retained |
| U1-G01 | complete professional groups | compile the manifest/action registry and all placements | all eleven approved action families represented with stable IDs and deterministic group/overflow order |
| U1-G02 | one action authority | scan profile/compiler/menu/toolbar plus dispatch tests | localization/help/feature policy resolves from one registry; no duplicate hard-coded product action catalog |
| U1-G03 | R3 lifecycle retention | init, repeated font refresh and language refresh, then invoke actual inspector | product menu and rich inspector remain populated, localized, visible and enabled according to feature policy |

## 10. G9U2 blocked procedure tests

These tests cannot execute until G9 spatial semantics are productive and
author-approved.

| ID | Requirement | Future test | Expected evidence |
|---|---|---|---|
| U2-01 | gate | try to activate before G9 PASS | workspace unavailable with localized dependency reason |
| U2-02 | typed projection-system selection | select spatial plane in a ProjectionSystem with several defining/auxiliary frames | resolve stable system/frame/binding IDs and roles; typed ambiguity; no label or visible-placement inference |
| U2-03 | fold | intrinsic frame coordinates, diagram map and declared line-of-ground/hinge under generic/degenerate cases | hinge resolved from persisted system relation; explicit auxiliaries, true magnitude, provenance and certificate |
| U2-04 | projection change | point/line/plane/object across current/new frames | defining/derived roles, frame relation, intrinsic-to-diagram map and reprojection consistency preserved |
| U2-04A | diagram independence | move/reflow view or diagram embedding without changing intrinsic projection-system semantics | no spatial reinterpretation; only approved map change affects diagram coordinates |
| U2-05 | procedure protocol | inspect one-action construction | every explicit generated step visible and owned |
| U2-06 | lifecycle | copy/delete/undo/redo/reopen procedure | stable identities, no stale spatial or projected results |
| U2-07 | 3D authority | edit/view derived 3D representation | view remains derived; no independent truth loop |
| U2-08 | workspace purity | switch Construction ↔ Procedures | no geometric recompute caused by switch alone |

## 11. Scientific regression suite

Public commands must rerun the existing G6-G8 invariant suites through the
public algorithms, not substitute screenshots. Add public/persistent fixtures
for:

- segment, circle/closed orientation, ellipse, parabola and smooth
  transcendental curves;
- free/dependent/mapped scalar generators and point generators on segment,
  circle, circular arc and Locus V2;
- cusp, self-intersection, multiple disconnected branches and discontinuity;
- near tangency, exact tangent, overlap and mixed finite/overlap;
- unbounded branch and explicit unsupported unbounded pair;
- branch creation/loss and continuation ambiguity;
- the three distinct author Locus workflows: graft development, truncated-cone
  dual connection and focal-sphere illumination.

The immutable reference GGBs remain legacy compatibility fixtures. New
canonical V2 models must be separately authored from approved public commands;
the reference archives must not be resaved as a shortcut.

## 12. Functional counters and benchmarks

Count-based evidence is normative where timing is not deterministic. Wall time
is characterization only.

| Benchmark | Matrix | Required counters/assertions |
|---|---|---|
| scalar generator mapping | identity/non-injective/periodic/discontinuous maps; 1, 10 and 100 external inputs | mapping calls, normal dependency updates, definition publications, restored transactions; zero writes to dependent live state |
| point-support providers | segment/circle/arc/V2, components/seams and support topology sweep | canonicalization, preimage/binding, continuation/ambiguity and invalidation counters |
| public creation dependency slice | chain depths 1, 10, 30 and mixed scalar/support inputs | dependency updates, evaluator calls, revision publications, restored transactions |
| dynamic recompute | 1, 10, 100 metric/intersection consumers | one coherent source revision; cache build/hit/eviction; no quadratic hidden update without evidence |
| nested V2 | depths 1, 2, 5 and 10; semantic queries \(q\), branch/component and cache-limit sweep | declared bounded `q*d`-style source/transform evaluations, cache build/hit/eviction, continuation/cycle events, work-limit status and deterministic order |
| total/partial metric | analytic and evaluator-only branches across tolerance sweep | evaluations, accepted/refined intervals, error guarantee, work counts |
| intersections | each G8 family, root count/contact/topology sweep | broad candidates, evaluations, refinements, residual checks, continuation events |
| public save/reopen | increasing object/result/token-point populations | XML bytes, load/recompute counts, deterministic identity restoration |
| workspace switch | empty and large constructions, 100 switches | zero kernel updates, zero Locus evaluations, UI compilation counts only |
| toolbar/layout | approved DPI/window-size matrix | deterministic grouping/overflow; no icon-size regression |

Every semantic benchmark asserts render-evaluation counters remain unchanged
unless rendering itself is the benchmark. Viewport, zoom and DPI changes must
not affect metric/intersection results.

## 13. Phase exit criteria

### G9U0 PASS candidate

- All applicable U0 rows pass on a clean tree.
- Every U0-G01 through U0-G22 generator case passes, including normal-DAG
  cycle rejection and deterministic nested counters.
- Old `.ggb` and legacy command behavior are unchanged.
- Creation, copy, deletion, undo/redo and save/reopen are complete.
- Every rich state is user-visible and every scalar/point child is guarded.
- Runtime flags and Classic policy are coherent.
- Focused and composed authorities pass without weakened G6-G8 evidence.
- Author explicitly approves command names and compatibility behavior.

### G9U0-R2 implementation PASS gate

- Accepted ADR 0016 and both normative R2 specifications were approved during
  planning closeout before any productive edit, and a separate author
  instruction invoked the implementation prompt.
- Every `R2-L01`–`R2-L15`, `R2-D01`–`R2-D17` and `R2-R01`–`R2-R07` row passes.
- Style changes preserve durable identity, semantic/topology revision, domains,
  branches/components, metrics and intersections.
- Unrelated crossings preserve fixed-policy render vertices/subpaths, while
  genuine components/discontinuities remain separate.
- `.cedg` is the native Save/open identity; `.ggb` source bytes remain unchanged
  through native save; the archive/XML and `app="classic"` contract remain.
- GeoCeDG Classic preserves opened `.cedg` without downgrade, creation
  enablement or default-new-document change; Windows packaging claims only the
  approved `.cedg` association and does not freeze an unjustified MIME value.
- Two focused executions produce matching canonical evidence and the composed
  authority passes without weakening older gates.
- User/developer/packaging guide impact is completed only for implemented,
  validated behavior and historical reports remain unchanged.
- The original failed author smoke remains evidence; the corrective author
  re-smoke passed and the author explicitly closed
  `G9U0-R2 IMPLEMENTATION = PASS — AUTHOR APPROVED` with
  `selfApproved=false`, `authorApproved=true` and `passClaimed=true`.

### G9U1 PASS candidate

- G9U0-R2 and G9U0-R3 are already `PASS — AUTHOR APPROVED`, and a separate
  author decision has authorized the exact post-R3 successor prompt; neither
  predecessor PASS alone is sufficient.
- Schema v2 and deterministic v1 migration pass.
- Construction workspace mapping, panel workflow, localization/icons,
  preferences, Document layout and Classic/Laboratory boundaries pass.
- Native `.cedg` and compatibility `.ggb` layout/open/save behavior follows the
  application-owned R2 policy and is not duplicated in the workspace manifest.
- Workspace switching produces zero semantic mutations.
- Only already approved public actions are enabled.
- Active-result markers are transient presentation over already admissible
  exact tokens; explicit one/all-point materialization is exact and undoable,
  no marker is persisted, and no point is created automatically.
- The eleven professional groups, accessible GeoCeDG-versus-Classic visual
  identity and distinct `geocedg.brand.topbar` / `geocedg.brand.startup`
  provenance seams pass automated and author review. If frontend/packaging
  branding is claimed, both required author-provided sources are present and
  validated rather than fabricated, and small startup-derived icons are proved
  suitable rather than assumed.
- G9U1 remains a presentation/interaction client: it neither requires G9B nor
  becomes a prerequisite of G9B.
- Human visual/accessibility review accepts professional density and workflow.

### G9U2 entry

G9 global PASS and explicit author authorization are mandatory unless the author
approves a separately specified narrower post-G9A pilot. Merely completing the
workspace manifest does not satisfy this gate.

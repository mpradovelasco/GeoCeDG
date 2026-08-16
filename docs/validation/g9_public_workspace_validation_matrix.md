# G9 public Locus and workspace validation matrix

- Status: **AUTHOR-APPROVED VALIDATION DESIGN / NOT EXECUTED**
- Date: 2026-08-16
- Scope: future G9U0 public Locus V2, G9U1 Construction workspace, and the
  post-G9 G9U2 Dihedral Procedures workspace
- Productive implementation authorized: **no**

This matrix defines future evidence. It does not claim that public commands,
persistence, runtime workspace switching, or Dihedral procedures exist.

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

G9U1 is a GUI client/organization phase for already authorized actions. It has
no semantic dependency on G9B, and G9B must not depend on G9U1. G9U2 is the
workspace that consumes approved G9 spatial/projection-system semantics and
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
| U0-I13 | proximity | click near two admissible markers, then change view | proximity ranks UI only; serialized token unchanged |
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

## 8. G9U1 workspace schema and behavior tests

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
| U1-W09 | document layout | open each immutable reference GGB | identified transient Document layout; product manifest unchanged |
| U1-W10 | reapply workspace | reapply Construction after document layout | construction/object/style bytes and dependencies unchanged |
| U1-W11 | GeoCeDG Classic route | invoke diagnostic action with controlled settings and a supported semantic file | separate process/profile/preferences; current GeoCeDG state unchanged; native semantic types/IDs preserved with no downgrade |
| U1-W12 | Laboratory route | open Templatev7 explicitly | hash/ingest checks; legacy toolbar remains document context only |
| U1-W13 | startup manifest failure | invalid packaged v2 fixture | explicit fallback to last accepted v1; no hidden toolbar authority |
| U1-W14 | switch rollback | inject view/menu compilation failure | old workspace restored; construction untouched |
| U1-W15 | public V2 action coverage | inspect Construction action catalog after G9U0 PASS | creation, rich/guarded length, general V2 intersection, token point and supported point-on-Locus actions present as GUI clients only |
| U1-W16 | no G9B coupling | compile/activate Construction with G9B unavailable but already authorized nonspatial actions available | workspace works; no G9B dependency or semantic fallback |
| U1-L01 | workspace localization | names/groups/help/blocked reasons in approved locales | no product literal drift; deterministic fallback |
| U1-L02 | icon/accessibility | missing icon and all action focus paths | text fallback, accessible name, keyboard navigation |
| U1-V01 | visual density | reference-size and small/high-DPI layouts | groups usable without giant embedded icons; overflow deterministic |
| U1-V02 | screenshot comparison | human review against supplied evidence | workflow/panel access accepted; no pixel-copy requirement |

## 9. G9U2 blocked procedure tests

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

## 10. Scientific regression suite

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

## 11. Functional counters and benchmarks

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

## 12. Phase exit criteria

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

### G9U1 PASS candidate

- Schema v2 and deterministic v1 migration pass.
- Construction workspace mapping, panel workflow, localization/icons,
  preferences, Document layout and Classic/Laboratory boundaries pass.
- Workspace switching produces zero semantic mutations.
- Only already approved public actions are enabled.
- G9U1 remains a presentation/interaction client: it neither requires G9B nor
  becomes a prerequisite of G9B.
- Human visual/accessibility review accepts professional density and workflow.

### G9U2 entry

G9 global PASS and explicit author authorization are mandatory unless the author
approves a separately specified narrower post-G9A pilot. Merely completing the
workspace manifest does not satisfy this gate.

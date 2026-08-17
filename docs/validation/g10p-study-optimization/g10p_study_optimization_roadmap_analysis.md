# G10P geometric-functional study and optimization roadmap analysis

| Field | Value |
|---|---|
| Disposition | **PASS — AUTHOR APPROVED** |
| Normative status | **APPROVED PLANNING / CHARACTERIZATION; NOT A PRODUCT IMPLEMENTATION SPECIFICATION** |
| Date | 2026-08-17 |
| Planning branch | `planning/g10p-study-optimization` |
| Approved anchor | `02e97ecc9a2e53aece913f7004c50c17fcc663e6` |
| G9A1 authority | `PASS — AUTHOR APPROVED`; annotated tag `geocedg-g9a1-pass` |
| Product effect | None; planning and roadmap only |
| User-guide impact | `NONE`; no command, UI, workflow, persistence, or enabled capability changes |

This document characterizes the author-approved post-G9 planning direction for
study architecture. It is planning evidence, not an implementation
specification or ADR. Its approval closes G10P only: it does not execute G9A2,
modify G9A1, or authorize any G9 or G10 productive phase.

## 1. Entry authority and repository state

The task entered on the clean prepared branch
`feature/g9a2-spatial-semantic-point-pilot`, apart from the author-supplied
canonical inputs listed below. That branch was not modified. The repository was
switched through `main` and this dedicated planning branch was created from the
published G9A1 anchor.

| Authority | Observed value |
|---|---|
| Repository root | `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG` |
| Entry branch | `feature/g9a2-spatial-semantic-point-pilot` |
| Planning branch | `planning/g10p-study-optimization` |
| `HEAD`, `main`, `origin/main` | `02e97ecc9a2e53aece913f7004c50c17fcc663e6` |
| G9A1 tag object | `9b125d9e4d23ff8ce68ce0ad9c16e30a8de338c7` |
| G9A1 peeled tag commit | `02e97ecc9a2e53aece913f7004c50c17fcc663e6` |
| Entry staged/tracked changes | none |
| Entry untracked inputs | eight files, all below `docs/validation/g10p-study-optimization/models/` |
| Unrelated entry paths | none |

The sealed G9A1 candidate report/evidence still says “pending author review.”
That is historical candidate evidence and was not rewritten. Current G9A1
approval is established by the published commit, annotated tag, living roadmap,
and current architecture records.

### 1.1 Current G9 and later-phase authority

| Phase | Current authority |
|---|---|
| G9P-R1 / G9P | `PASS — AUTHOR APPROVED` |
| G9O1 | `PASS — AUTHOR APPROVED` |
| G9A1 | `PASS — AUTHOR APPROVED` |
| G9A2 / G9A3 | `DESIGNED — NOT AUTHORIZED` |
| G9U0 / G9X1 / G9U1 | `DESIGNED — NOT AUTHORIZED` |
| G9B / G9C | `DESIGNED — NOT AUTHORIZED` |
| G9U2 | `DESIGNED — BLOCKED ON THE APPROVED G9 GATE` |
| G9 spatial solving | `NOT STARTED` |
| G10 productive work | `NOT AUTHORIZED — NOT STARTED` |
| G11–G16 | `NOT STARTED`; their detailed roadmap entries remain `PENDING` |

G10 currently combines a 3D DSL, orchestration, studies, and a workbench. G16
owns system-wide performance/scalability based on accumulated profiles. This
analysis refines G10 without changing G9 and keeps mathematical design
optimization distinct from G16 software-performance optimization.

### 1.2 Authorities inspected

- current code, tests, build, XML, copy, undo, redefine, event/script, headless,
  macro-kernel, and Locus V2 evaluation paths;
- `AGENTS.md`;
- `geocedg/specs/spatial/g9-spatial-projection-semantics.md`;
- ADR 0010–0015, especially ADR 0010/0011/0012;
- `docs/architecture/g9p_integrated_plan.md` and
  `docs/validation/g9p_author_decisions.md`;
- the G9A1 prompt, architecture, report, machine evidence, published closeout
  commit, and annotated tag;
- all live G9 future prompts and the living roadmap;
- the eight supplied `.ggb`/`.ggt` ZIP/XML artifacts without extraction back
  into, or mutation of, the source directory.

No canonical G10 or G16 task prompt currently exists.

## 2. Canonical model inventory

The minimum four-class corpus gate passes. SHA-256 values were computed before
branch switching, after read-only runtime characterization, and at closeout.
The directory spelling `dyscrete-elbows` is author-supplied and remains
unchanged. The author confirms that all eight inputs are author-supplied,
author-owned canonical research models and explicitly authorizes their
inclusion, versioning, and publication. The adjacent
[`canonical-models.yml`](canonical-models.yml) records immutable-byte
provenance and pilot roles; inclusion does not make them Java-kernel fixtures.

| Canonical input | SHA-256 | Intended role |
|---|---|---|
| `models/ArticulatedDoor.ggb` | `b6f3fdbc944164db3225ec2368f8be2c2305d4257c1be688aa79d0cb4ab77d2a` | design versus operating trajectory and force responses |
| `models/SphericValve.ggb` | `4aeed2b948087b1b9c4284b8420a76ebc9d3072b63829101ee31dee5e50fe4d8` | geometry-only predecessor/control |
| `models/SphericValveV2.ggb` | `e8411083e8d30274cc6a1db18cccc89f9a760064b312e662ac1f04556073ed58` | direct functional-response study without optimization |
| `models/TruncatedCone.ggb` | `ce1beeda01a4f1593ff75132338f739187560c803c1b8e6cf01d7c31c58504a0` | inverse design and bounded scalar optimization |
| `models/dyscrete-elbows/coneElbow_3V.ggb` | `8addf7b7099a5297bc64d551a10c59216ca4c5fe5c22caab92b016582b461a23` | conical discrete/continuous development case |
| `models/dyscrete-elbows/coneElbow.ggt` | `36e601e3a810f421f4f92499c0a1601edbf76691219514cddec0a294605c950e` | conical elbow construction template |
| `models/dyscrete-elbows/CylElbow_3V.ggb` | `413f812ef99552fea1b085c8de9d0d5cf3e69492ce40ca83016439ec3d01307f` | cylindrical discrete/continuous development case |
| `models/dyscrete-elbows/CylElbow.ggt` | `7dffa0db11a5024300dbdb87ec7cd9c86c9d020faf3b288e046e2f33804cf24e` | cylindrical elbow construction template |

All inputs are Classic 2D constructions/templates. None contains a
`geocedgSpatial` section or `geocedgId` attachment. Labels below are therefore
observational handles only; they are not acceptable future study identity.

### 2.1 Truncated cone / hopper

Observed construction: 137 elements, 112 commands, four loci, no 3D-view XML.

| Concern | Observed evidence |
|---|---|
| Candidate design inputs | `rbCono=6` in `[0,8]`, `hVert=11` in `[0,20]`, `rCil=3.73` in `[0,rbCono]`; `α=65°` in `[0,90°]` may instead be an inverse-design variable |
| Derived geometry | `hCil=Distance[D_1,F_1]`; the fixed straight-generator construction makes `hCil=K(R-r)` in the admitted nondegenerate case |
| Functional outputs | `Vcil=π rCil² hCil`; `Acil=π rCil²`; `γ(α)` is represented by a locus |
| Current objective | maximize `Vcil` over `rCil` |
| Current optimizer | `rbCono` On Update script: `SetValue(rCil_M, Maximize(Vcil, rCil))` |
| Visualization/orchestration | loci of volume, area, height versus `rCil`, and `γ` versus `α` |
| Explicit constraints/status | none beyond slider metadata and ordinary undefined geometry |

For fixed nondegenerate geometry with `K>0`, the independent reference is

\[
V(r)=\pi K(Rr^2-r^3),\qquad r^\star=\frac{2R}{3}.
\]

At `R=6`, the reference gives `r*=4`; the stored script result is
`4.000000014718043`, an absolute difference of about `1.47e-8`. This is strong
future scalar-validation evidence, not a general global guarantee outside the
declared fixed geometry/domain. Degenerate endpoints include zero base/height,
collapsed `rCil` interval, zero-height/zero-volume boundary cases, and line
intersection loss near extreme `α`.

Future separation:

- geometry authority: cone/cylinder construction, intersections, height, and
  angular relations;
- study authority: choose `rCil` or `α` as a study variable, define an inverse
  target or objective, invoke a solver, and classify the resulting guarantee.

### 2.2 Articulated door

Observed construction: 310 elements, 249 commands, 61 intersections, six loci,
and no scripts.

| Concern | Observed evidence |
|---|---|
| Principal design candidate | `lpaso=4.55`, range `[2.8,7.4]` |
| Operating variable | numeric-degree `α=47.81`, range `[0.01,179.99]`; rotations consume `α°` |
| Other possible geometry inputs | free points `A`, `Q_2`, `O_2`, `C`, `F_1`, `E_2`, `V_3`; their study role/domain is undeclared |
| Functional quantities | `Fa_v=Width/Width_0`, `Fa=Fa_v/cos(φ)`, `Fa_t=Fa cos(γ)`, `Fa_n=Fa cos(θ)`, with `Width_0=6.9` |
| Saved outputs | `Width≈7.93888333`, `Fa≈6.47457946`, `Fa_t≈6.14081251`, `Fa_n≈1.69906036` |
| Current analysis | three response loci versus `lpaso` and three versus `α` |
| Objective/constraints | no threshold, torque/weight objective, critical-state selector, or trajectory constraint is serialized |

The model demonstrates why `d` and `u` must be distinct. A future manifest must
state the admitted operating interval and functional assumptions. The unguarded
division by `cos(φ)`, many intersection paths, and near-0/180-degree states
require explicit invalidity diagnostics. A finite locus/sample cannot establish

\[
g(d,\alpha)\leq 0\quad\text{for every }\alpha\in U.
\]

No optimum or active constraint is asserted from this file. Those remain author
decisions backed by a future independent reference.

### 2.3 Spherical-valve functional response

`SphericValve.ggb` is the geometry-only control: 238 elements, 207 commands,
six geometric loci, eight dimensional/position sliders plus `α`, and no
functional response or optimizer.

`SphericValveV2.ggb` has 254 elements, 218 commands, and narrows the operating
angle to `[0,90°]`. It adds:

```text
D_effx = Distance[Q_2,S_2]
D_eff  = If[v·w < 0, 0, If[v·w < D_effx, v·w, D_effx]]
Q_n    = (α·180/π, (D_eff/D_effx)^4)
Locus[Q_n, α]
```

At the saved state, `D_effx≈1.7`, `D_eff≈0.6255168264`, and
`Q_n≈0.01832990868`. This is the direct-analysis pilot: no optimization is
needed. The clamp and fourth-power law belong to the declared functional model
and assumptions, not to geometric truth. Units, physical provenance, and an
independent reference are absent. The response locus is a visualization, not
the identity of the response function.

### 2.4 Polygonal elbows and discrete topology

The conical model exposes continuous candidates `D_1`, `Relbow`, `ElbowAngle`,
and `Dfrac`, plus integer-like `nFerrules` in `[2,20]`. The cylindrical model
uses `D_1`, `Relbow`, `ElbowAngle`, and `nFerrules` in `[2,30]`. `tref` and
index sliders are primarily inspection/presentation controls, not established
design variables.

Both constructions change list/cardinality/topology with `nFerrules` through
`Sequence` bounds. The `.ggt` inputs are label based:

- conical: `A,B,C,ElbowAngle,nFerrules,Dfrac,D,g`, with 34 outputs;
- cylindrical: `A,B,C,ElbowAngle,nFerrules,D,g`, with 33 outputs.

Current analysis uses nested loci, point-list extraction, `postLocus` heuristic
filtering, and `listLength` chord sums. These are sampled approximations without
an error certificate. The saved conical circumference/development comparisons
agree to about `4e-13` and `6e-13`, while one cylindrical sampled versus
ellipse-based length differs by about `0.0020449` (`8.86e-6` relative), with no
bound explaining the discrepancy.

Important invalid combinations include zero diameter/radius, full-turn or
tangent/coincident geometry, and a cylindrical `nFerrules=2` boundary for which
an expression requests a second entry from a one-entry `ListPlanes`. The
ordinary numeric slider with step one is not an integer semantic domain.
`RemoveUndefined` can also hide failed components rather than publish a study
invalidity state.

The elbow corpus demonstrates discrete topology and mixed-variable comparison;
it contains no objective or optimization. Embedded export button scripts and a
hard-coded historical output path are UI side effects and must not run in an
isolated study evaluation.

### 2.5 Read-only runtime/freshness characterization

Each input was passed by absolute path to the existing production desktop load
path through `:desktop:desktop:runGeoCeDG`; no new harness was added. A file was
considered structurally accepted when the loader reached its normal running
state without a document-load failure. The process was then stopped explicitly;
the resulting launcher exit `1` records that operator termination, not a failed
load. No document was saved.

| Input | Runtime/freshness observation |
|---|---|
| `TruncatedCone.ggb` | Accepted. The construction and its Locus/script-based scalar response loaded; this corroborates the direct/inverse/scalar pilot and the requirement to suppress scripts during isolated evaluation. |
| `ArticulatedDoor.ggb` | Accepted. Its Locus-driven updates were active. Desktop Algebra-tree resize notifications emitted `NullPointerException`/`ArrayIndexOutOfBoundsException` diagnostics after load; the document remained running. This is a current UI/load observation, not a contradiction of the design/operating-variable pilot. |
| `SphericValve.ggb` | Accepted without a document-load exception; the geometric-response control characterization remains consistent. |
| `SphericValveV2.ggb` | Accepted without a document-load exception; the additional direct functional-response Locus remains consistent with the analysis-only pilot. |
| `dyscrete-elbows/coneElbow_3V.ggb` | Accepted. The current loader reported embedded-image, 3D-XML/control-character, and legacy conic-tag compatibility diagnostics; scripts/buttons and the topology-changing discrete construction were present. |
| `dyscrete-elbows/CylElbow_3V.ggb` | Accepted with the same class of legacy compatibility diagnostics; the discrete/topology-changing planning role remains supported. |
| `dyscrete-elbows/coneElbow.ggt` | Accepted through the template load path; it remains a label-bound provenance/reference template, not durable study identity. |
| `dyscrete-elbows/CylElbow.ggt` | Accepted through the template load path; it remains a label-bound provenance/reference template, not durable study identity. |

ZIP/XML inspection corroborates six Locus commands and no scripts for the door,
six/seven Locus commands and no scripts for the valve pair, four Locus commands
and two scripts for the truncated cone, and five scripts plus two buttons in
each elbow document. Neither template contains scripts. No input contains G9
durable identity. The existing loader has no safe parameterized reporting mode
for querying arbitrary external documents headlessly, so saved numeric values
and entity counts remain static XML observations; no product harness was built
to overstate runtime coverage. These limits do not materially contradict the
planning conclusions.

## 3. Observed limitations

### 3.1 Conceptual

- no explicit design/operating/scenario roles;
- UI slider limits stand in for, but do not define, semantic domains or units;
- geometry, functional assumptions, orchestration, and visualization are mixed;
- no general study identity, definition revision, provenance, or currentness;
- no separation of geometric validity, functional feasibility, termination,
  coverage, and optimality guarantee;
- no explicit trajectory-wide constraint semantics.

### 3.2 Architectural

- all current model references are labels and none has G9 durable IDs;
- hopper optimization is an update-script side effect in the live document;
- there is no production, isolated whole-construction evaluation service;
- no construction-wide monotone revision token currently exists;
- no atomic durable-ID multi-parameter apply transaction exists;
- XML/undo reload is app-coupled and is not a speculative transaction;
- generic partial graph copies risk a second dependency graph or label repair.

### 3.3 Numerical

- loci/samples are used where a scalar evaluator or continuous constraint is
  required;
- elbow chord sums have no tolerance/error guarantee;
- a finite sample does not prove continuous-domain feasibility or a global
  extremum;
- invalid intersections/singular formulas lack aggregate status evidence;
- random/CAS/script behavior lacks an explicit study determinism policy.

### 3.4 Performance

- expensive geometry can be recomputed from interactive slider/update events;
- response loci build many samples even when only one scalar is needed;
- candidate evaluation may trigger view notification/render work;
- identical candidates have no revision-safe study cache;
- repeated XML reconstruction is correct but potentially expensive;
- no study-specific counters exist for DAG evaluations, render, scripts,
  cancellation, memory, or cache behavior.

### 3.5 UI and scripts

- On Update currently invokes `Maximize` and mutates a result geo;
- elbow buttons perform file export and use presentation-only controls;
- animations/manual index sliders drive exploration;
- ordinary event listeners and object scripts are broader than
  `EvalInfo.withScripting(false)`;
- silently suppressing a script on which an output depends would produce a
  false “valid” evaluation.

## 4. Formal study model

Let a study definition bind to one coherent construction snapshot/revision
`C_r` and declare:

\[
d\in D,\qquad u\in U,\qquad e\in E,
\]

where `d` are continuous, integer/discrete, or finite categorical design
variables; `u` describes operation/configuration of a fixed design; and `e`
selects scenarios/environmental assumptions.

The authoritative construction evaluates

\[
G(C_r,d,u,e)
\]

through the normal GeoCeDG/GeoGebra dependency DAG. An external functional
model consumes that result:

\[
y(d,u,e)=F(G(C_r,d,u,e),d,u,e;a),
\]

where `a` is an explicit set of physical assumptions, numerical reductions, or
surrogates. The required separation is

\[
\text{authoritative geometry}\ne\text{functional model}\ne\text{solver}.
\]

Equality and inequality constraints are

\[
h_i(d,u,e)=0,\qquad g_j(d,u,e)\leq0.
\]

For a trajectory/scenario requirement, the feasible design set is

\[
\mathcal F=\left\{d\in D:\begin{array}{l}
G(C_r,d,u,e)\text{ is geometrically valid},\\
h_i(d,u,e)=0,\\
g_j(d,u,e)\leq0\quad\forall(u,e)\in U\times E
\end{array}\right\}.
\]

An optimization request is then

\[
d^\star\in\operatorname*{arg\,min}_{d\in\mathcal F}J(d)
\]

or the corresponding maximization. A solver termination at one candidate does
not establish membership in `F`, local optimality, or global optimality.

Every evaluation result must bind at least the source snapshot/currentness
token, study-definition revision, canonical candidate/scenario, output values,
geometric state, functional evidence, numerical/coverage evidence, termination,
warnings, and guarantee. Changing the source or study definition makes prior
results stale; it does not silently reinterpret them.

## 5. Kernel versus external layers

| Capability | Shared Java construction/kernel | External DSL/runtime | Workbench | Reporting/export | Reason |
|---|---:|---:|---:|---:|---|
| durable object/parameter identity | owner | reference | browse | record | lifecycle authority belongs to G9/kernel |
| geometric evaluation/validity/DAG | owner | consume | inspect | report | never duplicate geometry |
| spatial frames/bindings/certificates | owner | consume | inspect | report | G9 semantic authority |
| Locus metrics/intersections | owner | query | inspect | report | sampling/render is not metric authority |
| coherent source token/currentness | owner | compare/store | display | record | correctness requires construction state |
| isolated candidate execution | minimum shared service | orchestrate | request/cancel | record | must run the normal DAG without live mutation |
| atomic multi-parameter Apply | minimum shared transaction | request | explicit confirmation | record provenance | identity/undo/currentness are document semantics |
| study variables/domains/units | validate referenced capability | owner | edit | record | study declaration is not geometry |
| functional assumptions/quantities | expose inputs only | owner | edit/inspect | record | physics/surrogates remain explicit and replaceable |
| objectives/constraints/scenarios | no solver semantics | owner | edit | record | orchestration concern |
| sweeps/solvers/worst-case/Pareto | none | owner via adapters | execute/cancel | record | generic optimization stays outside kernel |
| plots/comparisons | no authority | provide result data | owner | owner | presentation/client responsibility |

The shared seam must be cross-frontend, but the study runtime must not depend on
a particular GUI workspace. The workbench consumes services and never gates
kernel or runtime correctness.

## 6. Deterministic evaluation architecture

### 6.1 Reusable observations

- `Construction.updateConstruction(false)` and multi-root
  `GeoElement.updateCascade(...)` execute the normal DAG; the latter can union
  dependents so they update once, but neither is an atomic study transaction.
- App/Construction XML and undo snapshots can reconstruct a graph, but parsing
  clears/mutates app state and generic failures do not receive universal
  rollback.
- there is no whole-construction copy constructor; `copyInternal` is per geo;
- `AppCommon` is explicitly test-oriented, owns a nominal view, uses dummy CAS
  by default, mutates global prototypes/logger, and lacks a disposal contract;
- `MacroKernel` is separate but delegates viewport/CAS state and can resolve
  parent variables by label, so it is not a safe generic study sandbox;
- Locus V2 provides useful bounded/disposable/revision-coherent cache and work-
  limit patterns, but not a whole-construction evaluator;
- event dispatch, object GGBScript, JavaScript, and global listeners have
  overlapping controls; blocking only update scripts is insufficient;
- G9A1 record revisions are provider-owned identity/redefine evidence, not a
  general construction revision.

### 6.2 Alternatives

| Alternative | Correctness/isolation | Cost | Principal risk | Disposition |
|---|---|---|---|---|
| separate headless document loaded from immutable source snapshot | strongest; independent construction and discard boundary | startup/memory and XML load | current headless classes need production lifecycle/script/CAS hardening | **recommended first direction** |
| reusable isolated sandbox reset between candidates | potentially strong after proof | amortized startup | state leakage, reset equivalence, cache/script/random history | later optimization after reference equivalence |
| MacroKernel/selected dependency closure | normal algorithms but partial isolation | lower candidate cost | label/parent/CAS/view coupling and incomplete closure | characterization only; not first authority |
| live document with snapshot/rollback per candidate | weak isolation | low setup | listeners, scripts, undo, partial failure, visible mutation | reject as evaluation engine |
| independent Python/subgraph evaluator | separate process | variable | second geometric graph and semantic drift | prohibited |

### 6.3 Recommended direction

G10A should first establish a kernel-thread-confined, disposable, headless
document/session loaded from one immutable source snapshot. It should:

1. verify a coherent source token before the session is created;
2. resolve study inputs/outputs through durable IDs, never labels;
3. prevalidate and stage a complete candidate vector;
4. update the isolated model through the normal DAG with rendering and external
   listeners absent;
5. capture typed outputs, geometry/certificate state, work evidence, and
   warnings;
6. bind the result to source, study, policy, candidate, and scenario revisions;
7. discard the session or prove an exact reset before reuse.

This correctness-first implementation becomes the reference oracle. Pooling,
incremental reset, selected-subgraph evaluation, or caches require measured
equivalence against it.

### 6.4 Script, event, random, and cancellation policy

Scripts and external listeners do not run by default. If a requested output
depends on an On Update/GGBScript side effect, the result is
`NOT_EVALUABLE`/unsupported until that logic is represented as an explicit,
pure study function or approved construction dependency. The hopper’s current
`Maximize` handler and elbow export buttons are therefore orchestration/UI, not
candidate-evaluation semantics.

The isolated session must not register the live app event dispatcher, script
manager, file/browser handlers, undo history, or view/render listeners. Loading
XML must not be allowed to re-enable scripts over the evaluation policy.

Random values require one explicit policy: reject nondeterministic studies,
freeze captured random outputs, or use a declared reproducible seed. CAS policy
and library versions are also result provenance. General DAG algorithms lack a
cooperative cancellation contract; initial cancellation may stop before/after
a candidate and discard the session, while hard limits must be reported as
`LIMIT_REACHED`, not claimed as immediate cancellation.

## 7. Explicit result application

Evaluation never mutates the authoritative document. A future explicit
`Apply(d*)` boundary must:

1. compare the result’s coherent source token and study revision with the live
   document;
2. resolve every target by G9 durable ID and revalidate type/domain/authority;
3. prevalidate all values before the first mutation;
4. stage direct parameter changes and perform one multi-root normal-DAG update;
5. suppress intermediate external notifications and publish one documented
   committed update;
6. create one undo checkpoint and restore the complete pre-apply state on any
   failure;
7. preserve or replace identity only under the G9 lifecycle contract;
8. record prior/applied values, study provenance, and guarantee without making
   a result certificate geometric authority.

G9A1 supplies durable identity; G9A3 is the minimum lifecycle dependency for a
general atomic multi-target mutation; the relevant G9A2/B/C certificates govern
spatial validity. The current `GgbAPI.setValue` and command evaluation are
label-based, single/sequential, and non-atomic, so they are not this seam.

## 8. Proposed G10 decomposition and dependencies

Effort estimates are preliminary engineering bands, not schedules or
authorization: `S≤2`, `M=3–6`, `L=7–12`, and `XL>12` person-weeks, excluding
author review and upstream/toolchain delays.

| Phase | Objective and scope | Non-goals | Dependencies and gate | Artifacts and validation | Layer | Effort |
|---|---|---|---|---|---|---:|
| G10P | taxonomy, formal model, corpus, evaluation/apply alternatives, status/persistence/performance plan | no product or normative API | published G9A1 + corpus; author review closes planning | this analysis; later spec/ADR/prompts/evidence | documentation/validation | M |
| G10A | isolated deterministic evaluation, coherent source token, side-effect policy, limits/cancellation, atomic Apply seam | no solver, DSL, UI, or study XML | global G9 PASS + approved G10P + separate spec/ADR/prompt | reference-vs-repeat equivalence, live-nonmutation, script/render-zero, apply rollback tests | shared Java document/construction service | XL |
| G10B | solver-neutral external study schema/DSL for durable references, domains, units, assumptions, outputs/objectives/constraints | no geometric graph or solver implementation | G10A contract + approved schema/prompt | parser/schema round trip, ID resolution, stale-source and invalid-domain fixtures | external runtime | L |
| G10C1 | direct analysis, sweeps, inverse scalar design, bounded scalar optimization, finite discrete/simple mixed search, worst-case search | no generic global/MINLP/Pareto claims | G10A+B PASS + solver-adapter contract | four pilots, independent references, deterministic artifacts, guarantee tests | external orchestration/adapters | L |
| G10C2 | multidimensional, Pareto, mixed-integer, uncertainty, robust/sensitivity, justified certified/global methods | no kernel optimizer or automatic differentiation by convenience | C1 evidence + separate author gate | family-specific benchmarks/references and guarantee evidence | external runtime; optional narrow kernel seam only if proved | XL |
| G10U | configure/run/cancel/inspect/plot/compare and explicitly apply | no geometry, solver authority, or hidden apply | C1 PASS + client contract; independent of one workspace | workflow/accessibility/invalid-result/currentness/apply-confirmation tests | workbench/client | L |
| G10R | validate minimal pipeline and study-specific performance budgets | no system-wide optimization | A/B/C1/U evidence | corpus reruns, profiles, counters, cache/cancel/live-state gates | validation/benchmark | M |

Dependency graph:

```text
G9A1 PASS --> G10P planning candidate

G9A1 --> G9A2 --> G9A3 --> remaining G9 tracks --> G9 GLOBAL PASS
                                                        |
approved G10P ------------------------------------------+
                                                        v
G10A --> G10B --> G10C1 --> G10U --> G10R --> minimal G10 v1 closeout
                    +--> G10C2 (separate later gate; not required for v1)
```

Additional typed dependencies:

- external durable references require explicit G9A1 participation;
- general atomic Apply requires G9A3;
- spatial objectives/constraints require the relevant G9A2/B/C certificate;
- public persisted Locus V2 studies require G9U0, while their mathematics may
  consume the already approved internal G6–G8 metric/intersection contracts;
- G10 runtime correctness never depends on G10U or a specific GUI.

G10P can be reviewed during G9 because it changes no product. Productive G10A
or later work waits for global `G9 PASS — AUTHOR APPROVED`, revalidation of the
final host seams, and a separately invoked canonical prompt.

## 9. Canonical pilots and future acceptance evidence

| Pilot | Architectural dimension | Future acceptance authority/evidence |
|---|---|---|
| truncated cone | direct response, inverse target, bounded scalar optimum | kernel geometry; independent `r*=2R/3` for admitted fixed case; exact/numerical tolerance and invalid endpoints; no locus required for scalar solve |
| articulated door | distinct `d`/`u`, trajectory constraint, worst case | canonical model plus author-approved physical assumptions/reference; report sampled versus established continuous feasibility separately |
| spherical valve V2 | analysis without optimization | reproduce `D_eff`, clamp, `Q_n(α)` and response; functional law/provenance explicit; model geometry validity independent |
| discrete elbows | integer domain, topology/cardinality change, mixed search | enumerate allowed `nFerrules`; reject invalid combinations; preserve construction outputs; compare alternatives without hiding undefined members |

Every future assertion must name its authority, expected result, units,
tolerance, exact/numerical/sampled/certified nature, and local/global scope.

The author approves these canonical roles for planning: `TruncatedCone.ggb` is
the inverse scalar design and bounded scalar optimization pilot where an
independent analytical reference is verified; `ArticulatedDoor.ggb` separates
design and operating variables and is the future trajectory/worst-case pilot;
`SphericValveV2.ggb` is direct functional response without optimization and
`SphericValve.ggb` its geometry/control counterpart; both `_3V.ggb` elbow files
are discrete-variable and future mixed continuous/discrete pilots; and the two
`.ggt` files are provenance/reference templates demonstrating current
label-bound practice. These roles confer no extra semantic authority on labels.

## 10. Result semantics

One generic `SUCCESS` is prohibited. The author approves the following
independent conceptual axes; concrete Java/Python identifiers remain deferred
to later normative G10 specifications:

| Axis | Candidate values | Interpretation |
|---|---|---|
| source currentness | `CURRENT`, `STALE_SOURCE`, `STUDY_CHANGED` | whether result still applies to source/study revisions |
| geometric state | G9 certificate/state values where applicable; otherwise `VALID`, `DEGENERATE`, `UNDEFINED`, `INCONSISTENT` | construction/certificate outcome, not feasibility |
| functional feasibility | `FEASIBLE`, `INFEASIBLE`, `NOT_EVALUABLE`, `NOT_ESTABLISHED` | declared functional constraints only |
| domain coverage | `POINT_EVALUATED`, `FINITE_SAMPLE`, `BOUNDED_INTERVAL_ESTABLISHED`, `INCOMPLETE` | strength over operating/scenario domain |
| solver termination | `CONVERGED`, `LIMIT_REACHED`, `CANCELLED`, `FAILED`, `NOT_APPLICABLE` | why orchestration stopped |
| optimality guarantee | `EVALUATED_CANDIDATE`, `SAMPLED_FEASIBILITY_ONLY`, `LOCAL_OPTIMUM`, `GLOBAL_OPTIMUM_NOT_ESTABLISHED`, `GLOBAL_OPTIMUM_ESTABLISHED`, `NOT_APPLICABLE` | claim strength, never inferred from convergence |
| numeric evidence | G9-style `EXACT`, `NUMERICAL`, `DISCRETE` plus bound/estimate/unresolved evidence | how values and error claims were produced |

An analysis study may have `NOT_APPLICABLE` solver/optimality states. A
trajectory sample cannot emit `BOUNDED_INTERVAL_ESTABLISHED`. A local solver
that converges emits neither global feasibility nor global optimality unless an
independent method proves it.

In particular:

```text
valid candidate
!= feasible design
!= local optimum
!= established global optimum
```

## 11. Study-performance plan

Future G10A/G10R baselines must measure at least:

- session creation/source-load time and candidate-only time (`p50`, `p95`,
  worst observed) per canonical pilot;
- candidate throughput and normal-DAG algorithm/update counts;
- render/view notifications, external event callbacks, and scripts (all zero
  during default evaluation);
- peak and retained memory, session disposal, and leak-free repeated batches;
- deterministic result/evidence hashes across repeated runs;
- invalid-candidate cost and diagnostic completeness;
- cancellation request-to-stop/discard latency and completed work count;
- cache hits/misses/evictions, maximum entries/bytes, and stale-key rejection;
- exact pre/post live-document XML, undo position, construction step, selection,
  and current-file equivalence.

A conceptual cache key may include

```text
(SourceSnapshotToken,
 StudyDefinitionRevision,
 CanonicalCandidateVector,
 Scenario,
 EvaluationPolicy/engine/numeric versions)
```

There is no authoritative `ConstructionRevision` today. G10P approves the
requirement for a coherent source-revision/currentness token, but deliberately
does not prescribe its implementation. G10A must design it after global G9
completion and reinspection of the then-current kernel. Candidate values require
canonical units and number encoding. Caches are bounded, session-owned,
revision-safe, and invalidated rather than heuristically repaired.

G10R owns performance of the study pipeline. G16 remains responsible for
system-wide profiling and evidence-backed optimization across the product.

## 12. Persistence recommendation

### Option A — external study artifacts

A versioned external manifest references a source `.ggb` hash/snapshot token
and explicit G9 durable IDs, and stores study variables, domains, units,
assumptions, outputs, objectives, constraints, solver/policy versions, and
result provenance.

Advantages:

- preserves geometry/study/solver separation;
- avoids changing kernel XML/copy/undo/redefine/migration contracts;
- evolves with the external runtime and supports batch/CI artifacts;
- allows results and large traces to remain outside the authoritative document.

Required failure: if an intended parameter/output has no durable participating
ID, binding fails explicitly. It never falls back to the current label.

### Option B — study objects inside `.ggb`

This could integrate study identity with document copy, undo, macros, and
currentness, but it would add a new authoritative XML lifecycle. Current ZIP
loading has no generic safe unknown-entry extension, and embedding study XML
would require versioning, migration, collision/copy/redefine rules, Classic
behavior, and another accepted ADR/spec.

**Recommendation for G10C1:** Option A. Defer Option B until evidence shows
that study identity itself must participate in the authoritative construction
lifecycle. Do not modify G9 persistence to make a convenient container.

## 13. Roadmap changes made

Roadmap version 3.31:

1. records G10P as `PASS — AUTHOR APPROVED` for planning/characterization only
   while retaining G9A2 as the next productive authorization gate;
2. adds the external study layer and minimum shared evaluation/Apply boundary
   to the architecture/placement table;
3. separates G10R study-pipeline performance from G16 platform performance and
   from mathematical design optimization;
4. splits the consolidated G9/G10–G16 status row so no proposed product phase
   appears started or authorized;
5. refines G10 into P/A/B/C1/C2/U/R without renumbering G11–G16;
6. records the global-G9 implementation gate, typed dependencies, external
   solver boundary, status semantics, and minimal v1 closeout;
7. adds risks for a duplicated external geometric graph and overstated
   feasibility/optimality guarantees;
8. extends the pending G16 target outcome to include the G10 study capability.

No G9 phase scope/status/dependency, historical evidence, prompt, accepted ADR,
user guide, production source, or serialization contract was changed.

## 14. Risks and open decisions

### Resolved by G10P closeout

- author approval of the initial pilot roles and planning decomposition;
- author-supplied/owned provenance and repository publication authorization for
  the immutable eight-file corpus, with hashes recorded in the manifest;
- read-only load/freshness characterization of every `.ggb` and `.ggt` input;
- conceptual multi-axis result semantics, with concrete identifiers deferred;
- the requirement for a coherent source/currentness token, with its design
  deliberately deferred to G10A.

### Must resolve before G10A implementation

- exact units, operating/scenario roles, objectives, constraints, admissible
  domains, functional-law provenance, and independent references needed by the
  selected first pilot;
- G10A evaluation alternative selection, script/random/CAS policy, and
  acceptance budgets;
- production headless session lifecycle and safe full-source loading;
- coherent source snapshot/currentness token;
- durable-ID eligibility for selected inputs/outputs without legacy inference;
- zero-listener/script/render policy and detection of script-dependent outputs;
- deterministic random/CAS policy;
- cooperative limit/cancellation semantics versus discard-only cancellation;
- atomic multi-root Apply, undo, rollback, and notification contract;
- memory/performance baselines and sandbox reset-equivalence policy;
- final G9 host-seam revalidation and global G9 approval.

The corpus is planning/validation evidence, not a Java-kernel fixture set or a
default product asset. Embedded legacy images/macros remain subject to the
repository's separate factual asset/trademark audit before any binary release;
G10P makes no legal conclusion and does not promote them to product resources.

### May remain open for G10C2 or later

- multidimensional/global/certified solver families;
- mixed-integer and robust/uncertainty strategy selection;
- derivatives or automatic differentiation;
- distributed/parallel candidate evaluation;
- inside-`.ggb` study persistence;
- advanced Pareto/sensitivity visualization;
- partial-subgraph or pooled-session optimization after equivalence evidence.

## 15. Verification evidence

Final author-closeout verification completed on 2026-08-17:

| Command/check | Exit/result |
|---|---|
| `./tools/agent/verify-repository-state.ps1` | `0`; main, work-branch, detached-HEAD, and G10P roadmap phase parsing passed |
| `./tools/agent/verify-operational.ps1` | `0`; operational, text-hygiene, YAML/schema, and upstream-boundary contracts passed |
| `./tools/agent/verify-g9p-design.ps1 -LogDirectory artifacts/g10p-study-optimization/closeout/g9p-frozen` | `0`; frozen G9P/R1 authority and Markdown checks passed |
| `./tools/agent/verify-g9a1-spatial-identity.ps1 -SkipBuild -LogDirectory artifacts/g10p-study-optimization/closeout/g9a1-static` | `0`; sealed static evidence reports 117/117 and confirms G9A2 not authorized/executed |
| managed `./tools/agent/verify.ps1 -SkipBuild -KeepBuildOutputs -LogDirectory artifacts/g10p-study-optimization/closeout/composed-static` | `0`; all composed documentation/static/governance gates passed |
| production desktop loader, eight absolute model/template paths | each reached normal running state; section 2.5 records diagnostics and the deliberate operator stop; no save occurred |
| canonical model SHA-256 recheck after runtime | eight of eight match section 2 and `canonical-models.yml`; no input byte changed |
| G9A1 prompt/evidence canonical-LF hashes | `50c665a399b7b6290b8dcf86cc2326bb78202d85d7b52b130fd8ebf2980127e1` / `e5191804969285779d3f31fc0117a819d3176274640aa6a5a5aa817c3be464c7`, exact expected values |
| `git diff --check` / `git diff --cached --check` | `0` / `0` |

Final precommit inventory is one tracked roadmap modification, the new analysis
and provenance manifest, and the eight intentional canonical inputs (eleven
paths total); the index is empty. No production source, prompt, specification,
ADR, user guide, historical G9 evidence, or serialization artifact changed. No
generated bundle is tracked or appears in the change set. Relative-link/status
checks passed, and the user guide was reviewed: no observable user capability
or workflow changed, so no user-guide edit is required.

No product build or Gradle test suite was required: the task is
documentation/planning only, and the composed authority was deliberately
invoked with `-SkipBuild`. Runtime characterization used the existing launcher;
its compile tasks were up to date. The composed check ran under the managed
permission boundary required by its environment; no repository file was changed
to suppress a gate.

## 16. Recommendation

The roadmap direction is coherent, preserves the G9/G10 boundary, and is
**PASS — AUTHOR APPROVED** as planning/characterization only. The approved
corpus and phase decomposition are future design/validation direction; no
productive G10 implementation is authorized, and G9A2 was not executed.

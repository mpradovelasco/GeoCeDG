# G6 Locus V2 validation matrix

| Field | Value |
|---|---|
| Status | **G6A PASS — AUTHOR APPROVED**; approved validation input for future G6B |
| Governing model | [Locus V2 semantic model](../architecture/locus_v2_semantic_model.md) |
| Execution plan | [G6 Locus V2 plan](../roadmap/g6_locus_v2_plan.md) |
| Date | 2026-08-11 |

This matrix relates requirements to executed G6A evidence and future G6B gates.
G6A created only test-private fixtures and read-only legacy probes. Values and
architecture were approved at G6A closeout. G6B remains not started.

## 1. Tolerance classes

No screen tolerance is a geometric tolerance. The author accepts the
uncertified G6B comparison envelope
`max(1e-12*max(1,S), 64*ulp(max(1,S)))` after Level-A residual measurements.
`S` is a documented characteristic geometric scale for each case; it must not
depend on zoom, DPI, viewport or absolute distance from the origin.
Provider-specific domain values and absolute timing budgets remain separate.
The versioned authority is
[`tolerance-policy.yml`](../../geocedg/validation/locus-v2/tolerance-policy.yml).

| Symbol | Owner | Meaning | Forbidden reuse |
|---|---|---|---|
| `eps_eval_abs`, `eps_eval_rel` | G6A/G6B | World-coordinate error of deterministic point evaluation and analytic residual | Pixel deviation, length error, root residual |
| `eps_domain` | G6A/G6B | Parameter endpoint/classification tolerance, specific to a driver descriptor | Coordinate equality or branch proximity |
| `eps_topology` | G6A/G6B | Case-specific predicate margin for declaring a topology/degeneration state | Generic floating equality |
| `eps_render_px` | G6B | View-specific tessellation deviation in pixels | Domain, evaluator, metric or intersection truth |
| `eps_metric_abs`, `eps_metric_rel` | G7 | Future integration/length error | G6 render/evaluator |
| `eps_root`, `eps_residual` | G8 | Future parameter isolation and geometric intersection residual | G6 render/evaluator |

For scale `S`, the approved formula above is used. Each case must document how
`S` is derived from characteristic geometry and test the envelope against
translated/scaled variants. A tolerance may not be selected solely to make a
failing fixture pass. `eps_domain`, `eps_render_px`, G7 metric tolerances and G8
intersection tolerances are independent policies.

## 2. Level A — analytic simple cases

| ID / case | Definition and domain | Property | Expected evidence | Phase | Tolerance |
|---|---|---|---|---|---|
| `A-LINE` | `F(t)=(t,2t+1)`, explicit `[-2,2]` numeric driver | Constructive relation and orientation | For fixed endpoint/midpoint/random deterministic `t`, `y-2x-1=0`; increasing `t` preserves declared orientation | G6A reference; G6B unit/integration | `eps_eval_*` |
| `A-CIRCLE` | `F(t)=(3+2 cos t,-1+2 sin t)`, periodic `[-pi,pi)` | Closed endpoint equivalence without duplicate semantic identity | Radius is 2; domain is one periodic branch; `t=-pi` and limit at `pi` agree geometrically but follow declared endpoint policy | G6A; G6B | `eps_eval_*`, `eps_domain` |
| `A-ELLIPSE` | `F(t)=(1+3 cos t,2+2 sin t)`, periodic | Analytic relation and non-circular axes | `((x-1)/3)^2+((y-2)/2)^2=1`; one branch, orientation stable | G6A; G6B | `eps_eval_*` |
| `A-PARABOLA` | `F(t)=(t,t^2)`, native unbounded domain; finite render window declared separately | Unbounded semantic evaluation independent of rendering interval | Evaluations at fixed finite `t` satisfy `y=x^2`; domain remains unbounded under zoom | G6A; G6B point evaluator/render-boundary test | `eps_eval_*` |
| `A-TRANSCENDENTAL` | `F(t)=(t,sin t)`, construction-owned `[-2pi,2pi]` | Deterministic numeric evaluation | Fixed values and residual `y-sin(x)=0`; no view-derived function interval | G6A; G6B | `eps_eval_*` |

G6B must exercise both supported driver families across Level A: at least one
case with a point-on-path driver and one with an explicit numeric driver. The
same analytic formula implemented only as a standalone mock is insufficient
evidence for dependency-slice integration.

## 3. Level B — topology and degeneration

| ID / case | Fixture | Property | Expected result | Phase | Tolerance/status |
|---|---|---|---|---|---|
| `B-MULTI` | Two declared semantic branches `F0(t)=(t,t^2)` and `F1(t)=(t,-t^2)`, `t in [-1,1]` | Explicit branch identity | Two stable keys survive recompute and crossing at the origin; sample/order changes do not swap them | G6A; G6B | `eps_eval_*` plus exact key equality |
| `B-CLOSED` | `A-CIRCLE` | Periodicity and orientation | One periodic branch; no false split at the seam; reverse parameterization is distinguishable | G6A; G6B | `eps_domain` |
| `B-SELF-X` | `F(t)=(sin t,sin 2t)`, `t in [0,2pi)` | Multiple preimages | Origin evaluations at distinct parameters retain different semantic addresses on the same branch | G6A; G6B | `eps_eval_*`; distinct `t` |
| `B-CUSP` | Semicubical parabola `F(t)=(t^2,t^3)`, `[-1,1]` | Valid but potentially singular point | `t=0` has evaluation status `VALID`; regularity is `SINGULAR` only with differential evidence, otherwise `UNKNOWN`; no branch split | G6A; G6B validity; derivative detail optional | `eps_eval_*`, regularity metadata |
| `B-DISCONTINUITY` | `F(t)=(t,1/t)`, components `(-infinity,0)` and `(0,+infinity)` | Invalid parameter and render break | `t=0` is out/undefined; two domain components; no line crosses the discontinuity | G6A; G6B | `OUT_OF_DOMAIN` or approved boundary status |
| `B-UNBOUNDED` | `A-PARABOLA` and a line/ray path | Infinite domain | Fixed finite evaluations are stable; changing viewport changes only render coverage | G6A; G6B | Exact domain flags, `eps_eval_*` |
| `B-EMPTY` | Formal fixture below with `c>1`, or `a<0` in the lineage path | Empty valid definition/branch set | `EMPTY_DOMAIN`; prior evaluations/render state absent, descriptors remain diagnosable | G6A; G6B | Definition status `EMPTY_DOMAIN` |
| `B-ISOLATED` | Formal fixture below with `c=1` | Isolated valid driver states | Components `{−1}` and `{1}` are zero-dimensional; no fabricated interval and no automatic new branch key | G6A; G6B | Exact component topology |
| `B-SPLIT-MERGE` | Two-parameter formal fixture below | Distinguish branch lineage from valid-component topology | Typed trunk/child split and merge follow the provider rule; component splitting alone preserves each branch key | G6A policy; G6B integration | Exact keys/lineage, `eps_topology` only for numeric predicates |
| `B-COLLAPSED` | `F(t)=P0` on a nonempty interval | Parameter multiplicity | Branch/domain retained with property `COLLAPSED_IMAGE`; evaluation remains valid and is not reduced silently to one unparameterized point | G6A; G6B | Exact branch property |
| `B-DEPENDENCY-UNDEF` | Intermediate construction undefined on a declared subinterval | Dependency validity | Evaluator returns no coordinate there; render emits a break; recovery after input change has a new revision | G6A; G6B | `DEPENDENCY_UNDEFINED` |
| `B-NONFINITE` | Controlled evaluator yields NaN/infinity | Numeric validity | No point/cache insertion and no stale coordinate | G6A; G6B | `NON_FINITE` |
| `B-HISTORY` | Baseline constructions affected by `kernel.isContinuous()` | Determinism category | Classify as pointwise, canonical continuation with explicit anchor/orientation/rule, or unsupported; fresh/warmed/shuffled queries agree for both supported categories | G6A mandatory characterization; only supported subset in G6B | `eps_eval_*` and exact category/status |

### 3.1 Formal `B-SPLIT-MERGE` fixture

G6A encoded this deterministic two-control family. It deliberately
separates branch lineage from valid-domain topology.

For declared driver domain `Omega=[-1,1]`, let

\[
V_c=\{t\in[-1,1]\mid t^2\ge c\}.
\]

The provider's branch family is:

\[
\begin{aligned}
a<0 &: \quad B_a=\varnothing,\\
a=0 &: \quad F_{root}(t;a)=(t,0),\\
a>0 &: \quad F_{+}(t;a)=(t,\sqrt a),\qquad
                 F_{-}(t;a)=(t,-\sqrt a),
\end{aligned}
\]

with each extant branch evaluated on `V_c`. Provider keys are deterministic:
`root`, `root/+` and `root/-`. Its typed rule records `root -> {root/+,
root/-}` as `SPLIT` when `a` crosses from zero to positive, and the reverse as
`MERGED`; it does not derive keys from coordinates or samples.

Two independent state paths are required:

1. With `a=0`, vary `c=-1/4 -> 1/4 -> 1 -> 5/4`. The same `root` branch has,
   respectively, one interval, two intervals, two isolated valid components and
   an empty valid subset. No `SPLIT` lineage event occurs.
2. With `c=-1/4`, vary `a=0 -> 1 -> 0 -> -1 -> 0`. This exercises typed
   split, merge, disappearance and deterministic recovery of `root` after
   reappearance.

This fixture has analytic predicates and requires no proximity matching or
numerical root isolation. G6B may refine implementation names, but must not
merge component topology back into branch identity.

## 4. Nested Locus V2 composition

These fixtures isolate semantic composition from CeDG construction complexity.
Let `L1(s)=(s,s^2)` for `s in [-1,1]`. Define

```text
L2(t) = A2(t, L1(t/2)),       A2(t,(x,y)) = (x+t, y+1)
L3(u) = A3(u, L2(-u)),        A3(u,(x,y)) = (x, y+u)
```

so independent analytic references are
`L2(t)=(3t/2,t^2/4+1)` and
`L3(u)=(-3u/2,u^2/4+1+u)`. Higher levels use versioned affine maps of the same
form and an independently evaluated reference composition.

| ID | Dependency depth | Required evidence | Phase |
|---|---:|---|---|
| `NESTED-1` | 1 | Direct semantic evaluation of `L1`; no render-cache access | G6A reference; G6B |
| `NESTED-2` | 2 | `L2` consumes `L1` by semantic branch/domain/evaluator/revision metadata only | G6A strategy probe; G6B |
| `NESTED-3` | 3 | Correct `L3`, coherent keys/revisions, no full upstream-locus or per-query slice regeneration | G6A strategy probe; G6B PASS requirement |
| `NESTED-5` | 5 | Same invariants and useful depth-scaling evidence when fixture cost permits | G6A stress; G6B only if approved budget includes it |

The author accepted a reproducible real pair after the synthetic run:
`InterCilConoOblique.ggb` captures the three-level pathological transition and
`InterCilConoObliqueTwoLevels.ggb` is the working two-level comparison. Their
hashes and manifests are registered under `models/legacy/`; they are legacy
evidence, not V2 semantic authority or redistribution-approved assets.

## 5. Level C — real CeDG cases and phase assignment

Remote models remain optional evidence; they are never downloaded as a build
requirement. Local scientific sources and legacy artifacts retain provenance.

| ID / scientific case | Local/public evidence | G6A obligation | G6B minimum | Later owner |
|---|---|---|---|---|
| `C-CONE-CYLINDER` | LSIM and Symmetry papers; public model `ngdveaz8`; local author-supplied `InterCilConoOblique` pair | Characterize driver, leaves, topology and legacy two-/three-level locus/perimeter cascade | Small internal typed three-level fixture traced to both originals; originals remain manual/scientific legacy comparison and no native intersection or G7 metric is required | G8 owns native intersection and tangency validation |
| `C-FOCAL-SPHERE-CONE` | Public model `xcf3g4uu`; CeDG book/corpus relation | Identify parameters, branches and focal construction invariants | Optional smoke case only after source model is curated locally | G8 owns intersection correctness |
| `C-CYLINDER-DEVELOPMENT` | Book/development literature; public model `wsp9ktrq` | Characterize generatrix parameter and flattened-curve dependencies | One dependency-recompute pilot if a small deterministic local case exists | G7 owns developed-curve metric evidence |
| `C-OBLIQUE-CONE` | Tool/development study; public model `zfcgazam`; legacy length/post-processing tools | Record three-locus concatenation, coverage gaps, chord error and domain restriction; define non-sample invariants | Required legacy benchmark; V2 pilot only if deterministic dependency slice is approved | G7 owns length; G8 owns intersection aspects |
| `C-DISCRETE-ELBOW` | Discrete-model papers; public `wsp9ktrq`/`tptusqqm` | Separate integer ferrule count from continuous locus parameter and map topology changes | Required dynamic invalidation case with two small integer values; no optimization | G7 metric comparison; later model validation |
| `C-DEVELOPABLE` | `DevelopableRuledSurfaces_Rev.pdf` and related development paper | Distinguish locus semantics from the source's declared discrete `acc` approximation and regression-edge zones | Benchmark/unsupported diagnostic unless deterministic minimal case is curated | G7 metric and G8 intersection as relevant |

G6A characterizes all six Level C families. G6B is not required to make all
six productive. It must trace its small internal nested fixture to the local
cone-cylinder pair, retain the originals as manual legacy comparisons, include
one approved V2 dependency-chain case and one discrete topology
invalidation case; any additional pilot needs a curated local manifest and
deterministic expected evidence.

## 6. Cross-cutting G6B invariants

| ID | Property | Procedure | PASS condition |
|---|---|---|---|
| `I-GEOMETRY` | Constructive relation | Evaluate approved parameters and independently compute analytic/construction residuals | Every valid result satisfies its case relation within `eps_eval_*`; statuses match invalid cases |
| `I-ZOOM` | Semantic zoom invariance | Evaluate the same `(locus,revision,branchKey,t)` before/after at least two large zoom and viewport changes | Declared/valid domains, branch keys, revision, states, coordinates and quality metadata are equal within world tolerance |
| `I-RENDER` | Render may adapt | Capture tessellation metadata at the same two views | Vertex/segment count may differ; semantic snapshot and evaluations do not |
| `I-DETERMINISM` | Same query, same result | Query fixed semantic parameters repeatedly, in forward/reverse/shuffled order and fresh/warmed sessions, with cache enabled/disabled | Semantically equal results and diagnostics for pointwise and canonical-continuation providers |
| `I-DEPENDENCY` | Graph propagation | Change one source input, recompute, then restore it | Exactly one new local revision per published recompute; derived caches invalidated; restored geometry coherent; no stale result |
| `I-BRANCH-ID` | Stable branch identity | Recompute without topology change and vary valid-component topology, zoom/style/labels | Keys and semantic orientation unchanged unless the formal provider declares a typed branch event; lineage follows the formal fixture |
| `I-NESTED-COMPOSITION` | V2-on-V2 semantics | Run `NESTED-1/2/3` with outer parameter sets, session/cache enabled and disabled, then modify the innermost source | Correct reference geometry; coherent revisions/keys; no upstream render/cache or whole-locus tessellation; no dependency-slice rebuild per downstream point; equal cached/uncached result; correct chain invalidation |
| `I-NO-SAMPLE-AUTHORITY` | Semantic API isolation | Static/API test plus deliberate render-cache eviction | Evaluator and future-facing model expose no render list; results remain available after eviction |
| `I-FEATURE` | Experimental modes | Run `LEGACY`, `V2`, `DUAL` under GeoCeDG and Classic defaults | Classic remains legacy; V2 is opt-in; dual report labels V1 as sampled evidence |
| `I-LEGACY-LOAD` | Compatibility | Load representative legacy `.ggb`, including curated Template-derived case | Existing command/object behavior unchanged; no automatic migration or V2 XML |
| `I-NO-PERSISTENCE` | Restricted G6B boundary | Save a construction containing only legacy objects; inspect V2 factory behavior | No unversioned V2 XML is written; attempted unsupported persistence is explicit |
| `I-G5-BOUNDARY` | Export separation | Invoke G5 adapter against legacy and the experimental V2 entity | Legacy remains unsupported; V2 remains unsupported in G6; no render points enter DXF |

## 7. Legacy characterization evidence recorded in G6A

G6A saved results, toolchain, construction provenance and commands for:

1. sample count and sampled coordinates across at least three zoom/view states;
2. `PathParameter` public normalized value versus native internal parameter for
   segment, circle/ellipse, line, ray, parabola, hyperbola and explicit-domain
   curve/function cases;
3. perimeter/chord result across zoom for one analytic locus;
4. forward/reverse/shuffled traversal on selected continuous constructions;
5. timeout/partial-result behavior under a controlled stress case;
6. `Templatev7` tool formulas (`postLocus`, `listLength` / publication role
   `locusLength`, `listLength12` / publication role `locusLength12`) and the
   oblique-cone limitations, without modifying the original artifact;
7. branch/topology observations in cone-cylinder and discrete cases.
8. the author's legacy two-level nested locus case and, if reproducible, a
   three-level case, with dependency-slice build/reset/update counts,
   sample/`Path` access, repeated calls and render/view activity measured
   separately.

This evidence belongs under `geocedg/validation/` or a G6A validation report
according to the approved operational contract. Screenshots may supplement but
never replace saved geometric/parameter evidence.

## 8. Phase gates represented by this matrix

### G6A matrix gate

- all cases have an approved owner and expected property;
- all G6B cases have a reproducible construction source;
- every tolerance has a recorded derivation and unit;
- legacy characterization evidence is repeatable;
- unsupported/history-dependent cases are explicit;
- the formal topology fixture verifies branch/component separation and lineage;
- nested legacy evidence and V2 strategy comparison are reproducible;
- the normative semantic contract and accepted ADR record the author-approved
  architecture and compatibility boundary.

### G6B matrix gate

- every required Level A and selected Level B row passes;
- selected Level C pilots pass their limited G6B obligations;
- all cross-cutting invariants pass on both cache states where applicable;
- `NESTED-1` through `NESTED-3` and `I-NESTED-COMPOSITION` pass within the
  G6A-approved scaling budget;
- legacy/Classic tests pass unchanged;
- no G7 metric, G8 intersection, G9 spatial or DXF-locus behavior is claimed.

## 9. Regression artifact shape for execution

G6B should use the existing regression conventions with one manifest per case
containing:

```text
case id and maturity
source/provenance and construction hash
driver-provider version, semantic parameter contract and native mapping if any
branch keys, declared domains and valid-domain components
parameters to evaluate
defining invariants
expected definition/evaluation statuses, properties, regularity and lineage
determinism category and four quality axes including numeric guarantee
nested dependency identities/revisions when applicable
tolerance identifiers and units
phase owner
```

Expected results should compare semantic fields and analytic residuals, not
serialized render vertices or byte-for-byte `.ggb` output. Any remote public
model remains `build_dependency: false` until explicitly curated.

## 10. G6A execution disposition

| Evidence family | Executed result | Review disposition |
|---|---|---|
| Level A analytic | Line, circle, ellipse, parabola and transcendental test-private evaluators agreed for forward/reverse/shuffled orders; residuals were at most `1e-12` | Reference formulas and scale-aware uncertified comparison envelope accepted |
| Branch/components | Formal fixture traversed one interval, two intervals, isolated components, empty domain and reappearance while preserving `fixture.sheet.main` | Branch/component separation accepted; persisted fixture uses normative keys `root`, `root/+`, `root/-` |
| Lineage | Analytic manifest defines split, merge, disappearance and deterministic reappearance independently of component topology | Accepted; G6B must implement typed events rather than infer them |
| Legacy zoom | Same circle construction produced 277 versus 160 samples and different chord sums at two views | Confirms render/view samples cannot be V2 authority |
| Legacy multibranch | Hyperbola probe produced 798 samples and 100 `MOVE_TO` markers | Characterizes sample-list breaks; it does not supply V2 branch identity |
| Legacy nested, synthetic | Levels 1/2/3/5 completed; downstream drivers use `PathMoverLocus`; downstream slices did not clone upstream `AlgoLocus` | Useful control showing that nesting alone does not imply the reported failure |
| Legacy nested, real CeDG | Two-level control measured approximately 125–127 ms; pathological state before `Flatten` approximately 31.9 ms; creations approximately 6.03/5.95/5.67 s, undefined after the 500 ms guard; post-attempt recompute approximately 21.0 s | Accepted complementary control/pathological evidence; the repeated macro-slice `updateCascade` mechanism is specific to the measured models |
| Semantic nested | Depths 1/2/3/5 used exactly 5/10/15/25 calls for five outer queries; recursive and flattened references agreed | Recursive semantic composition accepted; flattening deferred pending profiling |
| Scoped session | Repeated depth-3 queries fell from 18 to 9 calls with three hits and identical results; active-key cycle detected | Scoped full-key bounded memoization and explicit active-key cycle guard accepted |
| Level C CeDG | Six families classified; the hash-pinned cone-cylinder pair is executable while the remaining cases stay static/metadata evidence | Originals accepted as legacy evidence; G6B uses a smaller internal typed fixture traced to them |

Executable evidence:

- `LegacyLocusCharacterizationTest` — six pinned-baseline cases;
- `LocusV2SemanticCharacterizationTest` — five test-private semantic fixtures;
- [`g6a-characterization-baseline.yml`](../../geocedg/validation/locus-v2/g6a-characterization-baseline.yml);
- [`scientific-pilots.yml`](../../geocedg/validation/locus-v2/scientific-pilots.yml).

The matrix is an accepted input to the future G6B gate. G6B is `NOT STARTED`
and may not substitute missing provenance with a remote URL or screenshot.

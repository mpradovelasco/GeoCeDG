# G6A — Locus V2 mathematical and semantic characterization report

| Field | Result |
|---|---|
| Gate | G6A — mathematical and semantic characterization |
| Branch | `feature/g6a-locus-v2-characterization` |
| Starting commit | `7d8d902bb5816a4270231a9215d58043499d58f0` |
| Upstream baseline | GeoGebra 5.4.928.0 / `9b93256b7df401ff056c37b502d82df4d72b1522` |
| Product implementation | **None** |
| Semantic contract | **APPROVED AS NORMATIVE G6 SEMANTIC CONTRACT** |
| ADR 0006 | **Accepted** |
| G6A result | **PASS — AUTHOR APPROVED** |
| G6B | **NOT STARTED** |

## 1. Scope and boundary

G6A executed the author-approved working hypothesis as a characterization
phase. It inspected the pinned kernel, scientific corpus and legacy evidence;
added read-only/test-private probes; executed analytic, topology and nested
fixtures; established a semantic/tolerance contract; completed the type/dispatch
impact audit; and integrated a focused verifier beneath `verify.ps1`.

No productive `GeoLocusV2`, algorithm, drawable, command, feature flag,
serialization, public `Path`, length, intersection, DXF locus export or spatial
semantics was added. Legacy `GeoLocus`, Classic behavior and `.ggb` meaning are
unchanged.

## 2. Authorities and scientific evidence

The work followed `AGENTS.md`, the living roadmap, the author-reviewed G6 plan,
accepted ADR/specs, G0–G5 reports, pinned source, the CeDG reference catalog and
curated legacy artifacts.

Scientific characterization used the local 2023 CeDG book and cataloged work
on LSIM/focal illumination, cone-cylinder symmetry and flattening, locus tools
and oblique-cone development, general developable surfaces, and discrete elbow
models. The evidence supports these requirements:

- a constructive locus may have several leaves and disconnected valid pieces;
- one parameter may carry constructive correspondence across projected views;
- historical “exact” claims describe constructive fidelity and do not imply
  exact arithmetic for sampled `double` coordinates;
- `postLocus` and legacy length tools filter/sum a sampled list and expose gaps,
  chord error and scaling costs;
- discrete integer model parameters and continuous locus parameters must remain
  distinct; and
- published discretizations such as a fixed number of generatrices are derived
  approximations, not Locus V2 identity.

The 2023 book's account of `PathParameter(Locus)` differs from the current
pinned baseline. That is recorded as version-context evidence, not silently
reconciled: the baseline currently exposes sample-index interpolation, while
both accounts demonstrate that a sampled traversal coordinate is unsuitable as
universal semantic identity.

`Templatev7.ggb` remained unchanged at SHA-256
`f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`.
It provides tool/macro evidence, including `postLocus`. The author subsequently
supplied two separate executable scientific constructions, now preserved
byte-for-byte as controlled legacy evidence:

- `InterCilConoOblique.ggb`, SHA-256
  `b1cb614f1a4c414144fbff29349ddebda92d1026acb4c535990a2895c589fa27`;
- `InterCilConoObliqueTwoLevels.ggb`, SHA-256
  `587328a8e5b6474aee3169bb6af2fe2a711e98e000a423a96bba6e38274fb2b6`.

They are structurally different and were characterized independently. Their
public redistribution remains blocked pending rights/asset review. Unselected
public models remain metadata-only and are not build dependencies.

## 3. Exact pinned-baseline map

The legacy runtime path is:

```text
CmdLocus
  -> AlgoDispatcher.locus(...)
  -> AlgoLocus / AlgoLocusSlider
  -> AlgoLocusND / AlgoLocusSliderND
  -> cloned MacroKernel dependency slice
  -> PathMover + repeated updateCascade
  -> GeoLocusND.myPointList
  -> PathMoverLocus / GeoLocusND Path behavior
  -> DrawLocus graphical path
```

Important constraints verified in source:

- sampling uses Euclidian scales, 5-pixel thresholds, a 500 ms per-step budget
  and `GeoLocusND.MAX_PATH_RUNS = 10`;
- `euclidianViewUpdate()` can recompute the locus after view changes;
- `GeoLocusND` publishes path range `[0, sampleCount-1]` and interpolates
  stored sample segments;
- `PathMoverLocus` reads the sample list and its `MOVE_TO` markers;
- current `Length[locus]` reports sample cardinality and perimeter uses sampled
  chord sums;
- `DrawLocus` renders from the same sample authority; and
- XML type `locus` maps to `GeoLocus`.

The exact source/class/method inventory is in the
[upstream impact map](../architecture/locus_v2_upstream_impact.md).

## 4. Legacy characterization results

Six synthetic focused tests plus four scientific-model tests characterize
existing behavior without patching it. The expensive button-equivalent probe
is opt-in and its results are stored as evidence rather than imposed on every
acceptance run.

| Observation | Result |
|---|---|
| Circle locus after view change | 277 samples at `[-5,5]`; 160 at `[-100,100]` |
| Same sampled chord sum | `20.565979779489272`; `20.565121501408928` |
| Function native path domain | changed from `[-5,5]` to `[-100,100]` with the view |
| Native path ranges | segment `[0,1]`; ray `[0,+infinity)`; line/parabola unbounded; circle/ellipse `[-pi,pi]`; hyperbola `[-1,3]` with encoded branch ranges |
| Hyperbola | 798 samples, including 100 `MOVE_TO` entries |
| Dependency chain depth 10/50/200 | slices contained 14/54/204 algorithms; creation 17.601/23.051/63.769 ms |
| Nested locus depths 1/2/3 | upstream sample counts 160/154/198; downstream uses `PathMoverLocus` |
| Nested downstream slices | two algorithms/four original elements; no cloned upstream `AlgoLocus` |
| Median nested recompute depth 1/2/3/5 | 0.559/0.946/1.268/1.211 ms; informational |
| Stored `InterCilConoOblique` | 10 loci; `loc10` slice has 44 algorithms, including one inner locus and one sampled `Perimeter` |
| Stored `TwoLevels` comparison | 13 loci; `loc11` slice has 53 algorithms, including one inner locus and one sampled `Perimeter`; measured source/driver recomputes about 127/125 ms |
| Original `Flatten` loci | each outer slice contains two inner loci and two sampled `Perimeter` algorithms; 61/73/73 slice algorithms |
| `Flatten` creation | about 6.03/5.95/5.67 s in the recorded run; all three outputs undefined after the legacy per-step timeout |
| Before/after third level | source recompute about 31.9 ms before `Flatten`, about 21.0 s after the three attempted loci |

The small controlled `Point(Locus)` fixture remained functional through depth
five because each downstream slice contained only its local point/expression
algorithms and traversed the upstream sample path. It was not representative of
the supplied CeDG model.

The scientific pair reproduces the author's observation and identifies its
mechanism. The working two-level states contain one inner legacy locus and one
`AlgoPerimeterLocus` in the outer macro slice. The `Flatten` command adds a
third locus level whose macro slice contains both inner loci and both sampled
perimeters. `AlgoLocusSliderND.pcopyUpdateCascade()` executes that slice for
each outer parameter sample; measured steps exceeded its 500 ms budget, logged
`AlgoLocusSlider: max time exceeded`, and returned all three loci undefined.

This is a demonstrated legacy mechanism, not an assumption that every nested
locus has the same cost. It directly supports the V2 prohibition against
whole-upstream-locus regeneration during downstream semantic evaluation.

## 5. Normative mathematical contract

The normative contract defines a branch `j` with declared domain `Omega_j`,
valid subset `V_j` represented by `validDomainComponents[]`, and evaluator

```text
F_(l,r,j) : V_j -> R^2.
```

`branchKey` identifies a constructive solution/sheet, not a connected
component or sample group. Semantic address `(locus, revision, branchKey, t)`
is distinct from its Cartesian image and preserves multiple preimages.

The semantic parameter is supplied by a versioned provider. It may equal a
native GeoGebra parameter only when that provider declares and proves the
mapping suitable/stable. Normalized `[0,1]` traversal is not identity. The
planned G6B minimum is an explicit numeric-domain provider and narrowly
approved segment/circle/ellipse path providers; view-derived function domains
are excluded.

The model separates definition status, branch/domain properties, evaluation
status, optional regularity and typed lineage. It also separates construction
fidelity, evaluation method, representation role and numeric guarantee. A
`double` analytic evaluation is recorded as
`FLOATING_POINT_UNCERTIFIED`, not exact arithmetic.

The author-approved uncertified comparison envelope is

```text
max(1e-12 * max(1,S), 64 * ulp(max(1,S))).
```

It is a validation envelope, not a certified geometric error bound. `S` is a
case-documented characteristic geometric scale and cannot depend on zoom, DPI,
viewport or absolute distance from the origin. Domain, render, future metric
and future intersection tolerances remain separate.

## 6. Formal topology and determinism evidence

The persisted two-control fixture distinguishes:

- one interval, two intervals, isolated points and empty valid subset for the
  same branch identity; and
- real provider-declared split, merge, disappearance and deterministic
  reappearance events with keys `root`, `root/+` and `root/-`.

The test-private executable form preserved `fixture.sheet.main` while valid
components changed and separated domain-component split from branch lifecycle.

Line, circle, ellipse, parabola and transcendental references produced equal
parameter-to-point maps for forward, reverse and shuffled queries with analytic
residuals no greater than `1e-12`. These are pointwise-deterministic references.
Canonical continuation remains supported conceptually only with an explicit
anchor, orientation and query-history-independent continuation rule;
unapproved cases return `UNSUPPORTED_NONDETERMINISM`.

## 7. Nested semantic strategy

Test-private recursive and flattened reference evaluators agreed at depths
1/2/3/5. For five outer queries both used exactly 5/10/15/25 level calls,
matching `outerQueries * depth`. With six repeated outer queries at depth three,
a full-key scoped session reduced calls from 18 to 9 with three hits and
unchanged results. Re-entering an active semantic key was detected.

The author accepts recursive semantic evaluators with a scoped shared session
as the minimum G6B strategy. Its key includes locus identity, semantic revision,
branch key and provider-canonical parameter. Controlled DAG flattening is
deferred because the reference produced no scaling benefit and would increase
coupling. G6B must additionally prove normal-DAG invalidation and no per-query
slice build against productive code.

The accepted functional budgets are:

- controlled pointwise calls equal outer queries times depth;
- an exact key is evaluated at most once per eligible session;
- cached/uncached geometry and status are equal;
- slice preparation occurs at most once per definition/revision, never per
  point query; and
- downstream semantic evaluation performs no render access, tessellation or
  whole-upstream-locus regeneration.

Absolute timing and memory budgets remain informational pending productive G6B
measurement.

The closeout also records a forward requirement for G7. Any future metric
consumed by a downstream construction must be scoped to the locus semantic
revision, consume V2 semantic data rather than render samples or sampled chord
sums, avoid recomputing the complete metric for each downstream query while
the upstream revision is unchanged, and use caching/invalidation coherent with
the normal kernel DAG. This is architecture-only evidence; G7 was not started.

## 8. `GeoClass` and minimum-impact recommendation

Reusing `GeoClass.LOCUS` would enter legacy casts/defaults and expose sampled
`Path`, length/perimeter, command, XML and 3D assumptions. The audit covers
`EuclidianDraw`, `EuclidianView3D`, plane view, `ConstructionDefaults`,
`GeoElement` predicates/display, `CmdLength`, `CmdFirst`, `CmdPerimeter`,
`CmdLocus`, ODE consumers, `GeoFactory`, G5 export and `DrawablesTest`.

The author accepts a distinct V2 classification appended to the enum so every
existing ordinal remains stable. G6B must add one explicit 2D drawable route,
keep `isGeoLocus()` and `isGeoLocusable()` false, remain outside legacy
`Path`, metrics, commands, XML and 3D dispatch, and use an internal factory
only. No enum or production source was changed during G6A.

## 9. Scientific pilots and exclusions

All required Level-C families have terminology, branch/topology observations
and phase ownership in
[`scientific-pilots.yml`](../../geocedg/validation/locus-v2/scientific-pilots.yml).
The author accepts the local hash-pinned cone-cylinder pair with distinct
roles: `InterCilConoObliqueTwoLevels.ggb` is the functional two-level control,
and `InterCilConoOblique.ggb` is the pathological third-level `Flatten`
reference. They are manual/scientific legacy evidence, not V2 semantic
authority, and their redistribution status remains blocked.

Because G6B has no public command or persistence, it will use a sufficiently
small internal typed three-level reproduction traced explicitly to both
originals. The fixture must prove composition, inner-source invalidation, no
render/sample dependency, no whole-upstream-locus regeneration and accepted
functional scaling. It need not implement G7 `Perimeter` semantics.

`BM-OBLIQUE-CONE` (development), the real discrete case and a real concatenated
workflow remain documented exclusions. No remote model was downloaded
indiscriminately.

## 10. Files created or modified

Durable G6A additions:

- normative semantic contract under `geocedg/specs/locus/`;
- topology, tolerance, scientific-pilot and measured-baseline records under
  `geocedg/validation/locus-v2/`;
- three characterization test classes under `common-jre/src/test`;
- two controlled legacy model packages with manifests, immutable originals and
  deterministic inventories under `models/legacy/`;
- subordinate `tools/agent/verify-locus-v2.ps1`;
- this report.

Updated planning/operational evidence:

- semantic model, upstream impact map, validation matrix, benchmark plan, G6
  plan, living roadmap, user guide, Accepted ADR 0006 and G6B prompt;
- `verify.ps1` and `verify-operational.ps1`, preserving composed authority; and
- the upstream-tree modification manifest, recording the three test-only files.

No production Java, Gradle configuration, manifest with product behavior,
toolbar, serialization or packaging logic changed.

## 11. Validation evidence

Recorded focused command:

```powershell
.\gradlew.bat -p source/shared :common-jre:test `
  --tests org.geocedg.common.locus.LegacyLocusCharacterizationTest `
  --tests org.geocedg.common.locus.LocusV2SemanticCharacterizationTest `
  --tests org.geocedg.common.locus.LegacyCeDGScientificModelCharacterizationTest `
  --no-build-cache --no-daemon --max-workers=1 --no-problems-report --info
```

Normal result: `PASS`, exit code 0, 15 tests, zero failures/errors/skips. The
separate opt-in scientific run executed the button-equivalent case and recorded
the three expected legacy timeout/undefined results without treating them as a
V2 success criterion. Gradle was
launched by JDK 22.0.2; common-jre compiled/tested with Corretto 17.0.10. A
documented Windows sandbox worker-access restriction required running Gradle in
the normal user environment. `--no-problems-report` avoids an independent
Gradle report move collision observed after mixed sandbox/user runs.

Final focused/composed validation and cleanup results are recorded at closeout:

| Gate | Result |
|---|---|
| `tools/agent/verify-locus-v2.ps1` | `PASS`, exit 0; 6 legacy + 5 semantic + 4 scientific-model tests, zero failures/skips and no checkstyle violations; log `%TEMP%\geocedg-verify-locus-v2\g6a-locus-v2-gradle.log` |
| `tools/agent/verify-operational.ps1` | `PASS`, exit 0; 24 upstream-tree files registered after adding the three test-only probes |
| `tools/agent/verify.ps1 -RunBenchmarks` | `PASS`, exit 0 in 475.3 s; G1–G6A gates, shared/Desktop builds/tests/checkstyle, transactional cleanup and frontend regression passed; logs `%TEMP%\geocedg-verify` |
| informational benchmark | `verify-operational` median 1233.727 ms, range 1233.200–1265.290 ms; within 5000 ms informational threshold; evidence `%TEMP%\geocedg-verify\operational-benchmark.json` |
| `git diff --check` | `PASS` inside the composed authority; repeated after final report edit |
| generated outputs / residual processes | final count: 0 repository-local generated directories and 0 Java/GeoCeDG processes |

During initial characterization, a process audit found one Gradle daemon and
three Java compiler workers left by a sandboxed wrapper timeout; their
PIDs/command lines were verified as task-owned and they were stopped. During
author closeout, the first sandboxed focused run instead failed on denied
Kotlin-daemon user-state access and restored its repository outputs; the same
gate then passed in the normal user environment. The final closeout audit found
zero generated repository directories and zero target Java/GeoCeDG processes.
These are environment observations, not product-code defects.

## 12. Author-approved closeout

The second review accepts the normative semantic contract, ADR 0006, the
distinct appended V2 classification, recursive semantic evaluators plus a
scoped shared evaluation session, the deterministic functional budgets, the
uncertified scale-aware comparison envelope and the distinct evidence roles of
the two cone-cylinder originals.

This closeout changes documentation, contracts, test-private characterization
and operational verification only. It does not authorize an implementation
commit, and it does not create a product `GeoLocusV2`.

**G6A = PASS — AUTHOR APPROVED.**

**ADR 0006 = ACCEPTED.**

**G6B = NOT STARTED. G7, G8 and G9 were not started.**

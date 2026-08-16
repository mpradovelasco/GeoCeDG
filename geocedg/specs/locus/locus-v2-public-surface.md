# Public Locus V2 command, tool and persistence surface

- Status: **NORMATIVE / AUTHOR APPROVED**
- Phase: G9P design approved; productive G9U0 implementation is not authorized
- Internal prerequisites: G6, G7 and G8 are author-approved
- Related Accepted ADR: `docs/adr/0013-public-locus-v2-surface-and-token-selection.md`
- Existing normative semantics remain in `locus-v2-semantics.md`,
  `locus-v2-metrics.md`, `locus-v2-intersections.md`, and
  `locus-v2-extended-intersections.md`

## 1. Purpose and non-goals

G6-G8 provide an internal semantic locus, rich metric results, rich
intersections, and exact-token point consumption. They deliberately provide no
public command, public tool, generic `Path`, or persistence contract. This
contract defines the smallest coherent path to an experimental public surface
without changing legacy `Locus` behavior.

This contract does not:

- authorize implementation;
- make Locus V2 stable or default-on;
- implement generic `Path` behavior;
- identify a locus with its render/sample polyline;
- migrate an old `GeoLocus` automatically;
- broaden G8 target families;
- make incomplete or uncertified results exact;
- make a workspace or GUI inspector semantic authority.

## 2. Audited implementation gap

The following current facts are entry conditions, not missing documentation:

- `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/Commands.java:112,120,148`
  registers only existing `Intersect`, `Length` and legacy `Locus` names.
  `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/BasicCommandProcessorFactory.java:38-39,112-113,154-155`
  routes them to existing processors.
- `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdLocus.java:31-33,47-95,107-121`
  accepts a dependent point plus a
  path point/slider and always calls the legacy locus dispatcher.
- `source/shared/common/src/main/java/org/geogebra/common/kernel/algos/AlgoLengthLocus.java:72-76`
  reports legacy sampled point count through `getPointLength()`. Redirecting
  `Length[legacy locus]` would silently change semantics.
- `source/shared/common/src/main/java/org/geogebra/common/kernel/commands/CmdIntersect.java:86-132,142-608`
  has no V2 branch. Its existing numeric
  index and initial-point overloads do not provide durable V2 solution identity.
- `source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusV2Factory.java:24`
  explicitly calls itself an internal/test creation seam with no command or
  persistence registration.
- `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSegmentPathLocusV2.java:31-51`
  receives an injected Java
  `LocusPathPointFunction2D`; it does not receive/reconstruct a user dependent
  point and its construction slice.
- `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusV2.java:61-64`
  identifies itself as `Algos.Expression`, not a reconstructible public command.
- `source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java:166-178,225-228`
  rejects copy/set and emits no XML.
  `GeoLocusMetricResult` and `GeoLocusIntersectionResult` likewise clear
  revision payloads on copy/set and emit no XML.
- `source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java:220`
  currently creates transient `opaque-root-N` tokens. They are valid internal
  revision evidence, not yet a cross-save token format.
- `geocedg/features/experimental.yml:26-30` declares `cedg.locus.v2`
  default-off, but there is no runtime service enforcing that declaration.

Consequently G9U0 must implement evaluator reconstruction, durable identity,
lifecycle and persistence before adding a user-facing creation tool. Command
wiring alone would create stable-looking objects that cannot survive ordinary
copy/save/reopen behavior and is a stop condition.

## 3. Public-surface invariants

The public surface must preserve:

- legacy `Locus[Q,P]`, mode `47`, old XML and old `Length[GeoLocus]` behavior;
- explicit driver/domain, branch, orientation, invalid interval and semantic
  revision;
- world/model-coordinate evaluation independent of viewport, zoom and DPI;
- separation of semantic evaluator, metric index, intersection solver and
  render cache;
- normal kernel dependencies and invalidation;
- rich status as the authority; scalar/point outputs are guarded adapters;
- one-dimensional generator identity by an explicit semantic coordinate/domain,
  provider kind/version and durable inputs—not by slider visibility, a display
  label, a sampled vertex or the current state value;
- semantic preimage identity by durable generator/support identity, branch
  lineage, provider version, canonical parameter and periodic lift/seam evidence,
  kept distinct from the revision-scoped component and continuation binding;
  finite-intersection identity remains its opaque exact solution token and
  approved continuation context—not label, list index, coordinate or proximity;
- no stale point or number after invalidation, work limit, ambiguous
  continuation or revision mismatch;
- truthful exact/estimated/unsupported distinctions.

## 4. Typed one-dimensional generator contract

### 4.1 Semantic abstraction

The public creation path must normalize every accepted input to a typed
one-dimensional semantic generator

\[
\mathcal{G}:D\rightarrow S,
\]

where \(D\) is an explicit oriented one-dimensional domain and \(S\) is the
typed state supplied to the dependent construction. A provisional internal name
such as `SemanticGenerator1D<S>` is descriptive only; this contract does not
freeze a Java or public object name.

The immutable generator descriptor/snapshot must contain:

- a durable generator identity or durable parent-algorithm output-slot identity;
- provider kind and semantic version;
- exactly one declared driving coordinate and its durable input identity;
- finite intervals or periodic fundamental domain, endpoint inclusion,
  orientation, discontinuities and valid components;
- canonicalization and periodic seam policy;
- the typed coordinate-to-state mapping and every registered external
  dependency;
- branch/component policy where the support has topology;
- semantic/topology revision, currentness and typed status; and
- a reconstructible evaluator signature/version, where a signature may support
  caching but is never identity.

The initial provider registry is closed and versioned. Adding a later typed
support family must not change the core generator semantics. A generic public
`Path`, legacy `GeoLocus`, arbitrary curve, render polyline or fitted sample is
not an implicit generator.

### 4.2 Scalar-state family

For a scalar state,

\[
s\in D,\qquad t=m(s;\alpha),\qquad Q=Q(t(s)),
\]

`s` is the true semantic coordinate, `t` is the state seen by the dependent
construction and \(\alpha\) denotes registered external parameters. The initial
contract admits:

- a bounded slider, but only through its numeric value and explicit domain;
- a free numeric object with an explicit semantic domain;
- a dependent numeric expression;
- a function of another declared scalar driver; and
- another deterministic algebraically derived scalar state whose mapping and
  dependency slice are reconstructible.

Slider presentation is not semantic identity:
`GeoNumeric.isSlider()` currently means independent and Euclidian-visible
(`source/shared/common/src/main/java/org/geogebra/common/kernel/geos/GeoNumeric.java:406-408`).
Hiding a slider must therefore leave the generator unchanged. A dependent `t`
is never mutated as though it were free. The evaluator varies only `s` inside
the isolated reconstructible transaction and lets normal dependencies compute
`t` and `Q`.

The address remains the coordinate in \(D\), not the resulting value of `t`.
Consequently a non-injective map such as \(t=s^2\) does not merge the preimages
\(s\) and \(-s\). A bare dependent numeric with several possible free ancestors,
no declared `s`, or no explicit domain is ambiguous and rejected. Random,
history-dependent, side-effecting or nonreconstructible mappings are
unsupported.

### 4.3 Semantic-support-point family

The initial point-state family admits a point constrained through a typed,
versioned provider to exactly one of:

- a finite segment, with canonical coordinate in `[0,1]`, ordered endpoint
  identities and explicit endpoint/reversal policy;
- a circle, with an oriented angular fundamental domain and explicit seam;
- a circular arc, with durable support/start/end/orientation inputs and a
  canonical local coordinate mapped to its angular extent; or
- one supported Locus V2 branch/component policy, using the source semantic
  evaluator and not its render cache.

The point, its support and all provider inputs are normal algorithm inputs. A
bare support is insufficient because it does not identify the constrained point
on which `Q` depends. Collapsed supports, unknown provider versions, generic
curves and points constrained to more than one eligible support fail with typed
status.

### 4.4 Public creation alternatives

The semantic model is independent of final command spelling:

| Alternative | Compatibility | Advantages | Problems | Recommendation |
|---|---|---|---|---|
| Extend `Locus[Q,...]` and choose V2 by profile/flag | weak | familiar syntax | identical text changes meaning; legacy mode/XML become ambiguous | reject during experimental maturity |
| `LocusV2[Q,G]`, where `G` is a typed generator declaration/binding | strong | one normalized form; explicit semantic boundary | requires a usable declaration surface and new command registration | preferred conceptual surface; final spelling open |
| Typed overloads such as `LocusV2[Q,P]` for an unambiguous supported point and `LocusV2[Q,t,s,D]` for scalar state/true driver/domain | strong | avoids a new public generator geo | larger argument matrix; mapping/domain errors must stay explicit | acceptable concrete surface |
| `SemanticLocus[...]` | strong | semantic rather than versioned name | introduces new terminology | naming alternative |

`Q` must depend transitively on the admitted state (`P` or `t`), and the
captured mapping must depend on the declared true driver `s` where those differ.
A label match, coordinate change or arbitrary ancestor search is insufficient.
No scalar-mapping command spelling is frozen by G9P. G9U0 must inspect the
actual GeoGebra command, overload, localization, and serialization conventions;
evaluate the alternatives above; preserve the normative generator semantics;
and present its selected public command surface for author review before that
surface is frozen. None of the spellings above is an implemented command.

## 5. Production creation evaluator

### 5.1 Required architecture

G9U0 needs a reconstructible parent algorithm, provisionally described as
`AlgoDependentPointLocusV2`, and a production evaluator contract. Exact Java
names are implementation choices. The parent algorithm must register:

- the dependent point `Q`;
- the typed generator descriptor and its state object;
- the true scalar driver and mapping inputs, or the constrained point and typed
  support inputs;
- every external input in the evaluator's dependency slice;
- the domain/orientation/periodic/branch/component policy;
- semantic, provider and evaluator versions; and
- durable locus and generator identities supplied by the approved general
  identity layer.

The evaluator must be reconstructible from serialized kernel inputs. It may not
serialize an opaque Java lambda, infer dependencies from labels, consult render
samples, or hide recomputation outside the kernel graph. Any isolated evaluation
transaction used to vary the driver must preserve normal update semantics,
restore live construction state deterministically, and be characterized against
the upstream legacy-locus evaluation mechanism.

An evaluator signature may help cache equivalence, but it is not object identity
or serialization authority.

### 5.2 Initial generator scope

The initial public design includes every scalar-state and semantic-support-point
family in §4.2-§4.3: bounded slider, free scalar with declared domain, dependent
scalar expression or explicit scalar map, and a point on a segment, circle,
circular arc or supported Locus V2 branch/component. This is one semantic
abstraction with closed typed providers, not a sequence of unrelated
type-specific exceptions.

Existing G6 providers already demonstrate versioned finite and periodic domains
and stable segment/circle families
(`LocusDriverDomainProvider2D.java`,
`ExplicitNumericDomainProvider2D.java`, and
`StablePathDomainProvider2D.java`). The live segment algorithm and injected Java
function remain characterization only; productive circle, arc and Locus-support
parents plus reconstructible evaluation are still required. The host
`GeoConicPart` normalized path coordinate may inform a typed circular-arc
adapter, but generic `PathParameter` is not declared universal semantic
identity.

The serialized parent retains the public command inputs and records provider
kind/version, durable true-driver or support identity, scalar mapping,
support-specific defining inputs, canonical domain/orientation/periodicity,
branch/component policy and versioned dependency-slice IDs. Loading reconstructs
those relations by durable IDs and normal algorithm inputs, never by labels,
XML order, coordinates or proximity.

Creation rejects a `Q` whose declared dependency slice omits the admitted state,
a scalar mapping whose true driver/domain is ambiguous, a constrained point
without exactly one approved provider, an unsupported discontinuity or
unbounded domain, a nonreconstructible/side-effecting evaluator, or a direct or
indirect cycle. Rejection is typed; no case is clipped to the viewport or
downgraded to legacy sampling.

### 5.3 Invalid and dynamic states

Creation must publish explicit definition status for empty/invalid domain,
undefined or nonfinite state, discontinuity, branch creation/loss, ambiguous
continuation and work limits. Typed creation/load errors must distinguish at
least:

- unsupported generator or support kind;
- missing/invalid domain or unknown provider version;
- ambiguous true scalar driver or nonreconstructible mapping/dependency slice;
- declared-state/dependency mismatch;
- direct or indirect dependency cycle;
- stale source position, missing branch/component or source revision;
- ambiguous component continuation or periodic seam state; and
- undefined/nonfinite evaluation or deterministic work-limit exhaustion.

An equivalent recompute may retain semantic revision according to the G6
contract; a domain, support topology, mapping or evaluator change advances it.
Failure invalidates derived point/metric/intersection evidence before new work
begins.

## 6. Semantic positions and partial length

### 6.1 Durable address versus revision binding

A public point-on-Locus endpoint/generator must separate:

1. a durable semantic preimage address: source Locus ID, provider version,
   branch lineage, canonical semantic parameter and, for periodic domains,
   periodic lift/wrap plus seam side or approach direction; from
2. a revision binding: concrete source semantic revision, resolved
   revision-scoped component, continuation status and evaluated Cartesian point.

The current `LocusSemanticPosition2D` supplies locus identity, branch, provider
version and canonical parameter, while `MetricPositionBinding2D` adds revision,
component and status. G9U0 must extend/compose those seams for durable
component-lineage and periodic-continuation evidence; a revision-scoped metric
component key alone is not durable identity. The Cartesian point is evidence
derived from the binding, never its identity.

If \(F(s_1)=F(s_2)\) with \(s_1\ne s_2\), both addresses remain distinct at a
self-intersection, repeated traversal or periodic seam. An arbitrary coincident
point is insufficient.

### 6.2 Point construction and Locus support

Recommended initial position construction:

```text
A = Point[L, branchKey, canonicalParameter]
```

This is a dedicated V2 overload/algorithm producing an ordinary `GeoPoint`; it
does not make V2 a generic upstream `Path`. Its parent algorithm serializes the
durable address and revision binding, depends normally on `L`, and resolves the
point through the source semantic evaluator. A graphical point-on-locus tool may
solve/rank supported semantic preimages for selection, but persists the chosen
address rather than a render vertex or nearest coordinate. Ambiguity requires
user choice.

Alternative dedicated spelling such as `LocusPoint[...]` remains an author
decision. A two-argument `Point[L,t]` is not recommended for multibranch loci.

That ordinary point is an admitted semantic-support generator for another
Locus V2:

```text
L1 -> PointOn(L1) -> dependent Q -> L2
```

The outer evaluator recursively uses the immutable source definition in the
same scoped evaluation session; it does not sample `L1`. The source locus,
point parent, dependent slice and outer locus remain ordinary algorithm inputs.
Existing `AlgoNestedLocusV2` and the G6 evaluation session demonstrate
productive acyclic semantic nesting, but no public point parent, inverse/drag
resolver or persistence contract exists yet.

### 6.3 Continuation, invalidation and periodicity

Continuation requires explicit provider/branch/component lineage. If a source
component persists unambiguously, the revision binding may be re-established
while retaining the durable address. If a component disappears, splits/merges
ambiguously, crosses an unresolvable seam or changes provider contract, the point
becomes undefined/noncurrent and every dependent outer locus publishes a
truthful invalid/undefined revision. Nearest-Cartesian repair is prohibited.

Periodic evaluation canonicalizes into the declared fundamental domain.
Interactive continuation additionally retains the lift/wrap count and seam
approach/direction so reopen or movement does not arbitrarily switch preimages.
Moving the point within an unchanged support does not by itself redefine the
generator contract; support/domain/mapping/external-parameter changes follow
normal revision rules.

### 6.4 Nesting and cycle rejection

Every dependency edge is registered through the ordinary `AlgoElement`/
Construction DAG: source locus to point parent, point/state to dependent
construction, and dependent inputs to the outer locus. Creation, redefine and
load reject direct or indirect cycles such as
`L1 -> P1 -> L2 -> ... -> L1` through normal kernel dependency checks. There
is no second generator graph.

The scoped G6 active-evaluation-key reentry check remains defense in depth for
corrupt or internal callbacks; it is not the authority for accepting a graph.
Acyclic nesting of depth greater than one remains supported and must retain
deterministic evaluation/work counters.

## 7. Length alternatives and recommendation

| Alternative | Result | Assessment |
|---|---|---|
| Publish only `Length[L]` | numeric | familiar, but hides coverage/rectifiability/error and is easily confused with legacy sample count |
| Experimental `LocusLength[...]` | rich `GeoLocusMetricResult` | explicit semantic boundary and preserves G7 evidence |
| Authoritative `LocusLength[...]` plus guarded standard `Length[GeoLocusV2]` child | rich query plus scalar-admissibility-guarded consumer | preserves diagnostics and ordinary measurement syntax without a second calculation |

Recommended experimental commands:

```text
M = LocusLength[L]
M = LocusLength[L, A, B]
```

`M` is the authoritative rich, nonnumeric result shown in Algebra and the
result inspector. It
exposes value kind, coverage, computation status, rectifiability, traversal,
method, error/guarantee, contributions, diagnostics, revision and work evidence.

G9U0 must expose the standard total `Length[GeoLocusV2]` operation only as a
guarded scalar child of or internal reuse of the reconstructible rich query.
The scalar algorithm must
never calculate directly or read samples. It publishes a finite number only
when `LocusMetricResult2D.isScalarAdmissible()` is true
(`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricResult2D.java:125-142`): finite value, complete coverage, success,
rectifiable, target reached/wrapped when applicable, and a guarantee other than
`FLOATING_POINT_UNCERTIFIED`. Otherwise it is undefined and links to the rich
diagnostic result. No sampled chord sum is described as exact.

If the rich result is not scalar-admissible, the standard scalar result is
undefined or uses the repository-consistent typed failure behavior. A future
between-position standard overload remains separately gated. Legacy
`Length[GeoLocus]` stays unchanged in every case.

`A` and `B` must be current semantic positions on `L`; mismatched locus,
provider version, branch or stale revision produces an explicit invalid query.

## 8. General Intersect integration

The existing command and general Intersect tool should be extended for V2
operands rather than creating a parallel Locus-intersection framework.

Recommended syntax:

```text
R = Intersect[L, T]
R = Intersect[L1, L2]
```

When either operand is V2, `R` is a rich `GeoLocusIntersectionResult`. Existing
non-V2 dispatch and return types remain unchanged. Supported G8 target families
are exactly:

- line, segment, ray and circle;
- ellipse, parabola and hyperbola;
- bounded function graph with explicit finite domain;
- regular polynomial implicit curve within the approved G8 contract;
- Locus V2 × Locus V2 on finite components or periodic fundamental domains.

Unbounded V2×V2, generic paths, arbitrary implicit topology and overlapping
policies beyond G8 remain unsupported.
`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTargets2D.java:104-169`
is the current closed adapter boundary; the `TargetFamily` enum is at
`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSemanticMetadata2D.java:45-49`.

## 9. Exact-token derived point

Recommended materialization syntax:

```text
P = Intersect[R, "opaque-token"]
```

The first argument's rich-result type makes this overload distinct from
existing three-argument numeric-index/initial-point forms. An alternative
`IntersectionPoint[R,token]` command is acceptable if author review finds the
overload confusing. `Element[R,n]` or list order is rejected.

Here **exact token** means exact equality of the opaque, algorithm-owned semantic
root token under the approved source-pair/constructive/branch/topology
continuation context. It is not computed from and does not contain current
source/provider revisions, component binding, canonical parameter, isolating
interval, coordinate, residual or solver evidence; those remain revision-scoped
localization and admissibility evidence associated with the token. “Exact” does
not claim exact numeric arithmetic. Coordinates may be numerical and carry the
applicable fidelity, residual and guarantee evidence. The point parent algorithm
stores that token and rich-result dependency. It never solves, selects another
root, or repairs by proximity. Current internal
behavior already follows this rule in
`AlgoLocusIntersectionPointV2.java:23-59`. G9U0 must replace transient token
generation with a durable lineage/serialization policy before public save.

Graphical proximity may rank already established, locally admissible candidates
for the chooser. Once the user selects one, only the token is authoritative.

## 10. Rich intersection state behavior

Point admissibility is local and deliberately independent of global
completeness
(`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionResult2D.java:106-134`).
The UI must not flatten
the following states:

| Rich result | Inspector/tool behavior | Derived point behavior |
|---|---|---|
| `SUCCESS`, `COMPLETE`, `EMPTY` | defined rich result; explicit zero solutions | no token, no point |
| `SUCCESS`, `COMPLETE`, `FINITE` | show every verified solution/token and contact evidence | exact selected admissible token creates/updates point |
| `SUCCESS`, `NOT_ESTABLISHED` or `INCOMPLETE`, finite solutions | warn that global coverage is not established; show known solutions | a locally isolated/current/unambiguous token may still create a point; provenance retains incompleteness |
| locally unisolated root | show candidate and isolation diagnostic as non-admissible | point remains undefined |
| several admissible roots | chooser shows semantic branch/component/contact evidence; proximity may preselect | persist chosen token, never ordinal |
| tangent root | display `TANGENT_ESTABLISHED` or undetermined contact honestly | admissible only if local isolation and identity checks pass |
| `OVERLAP` / `INFINITELY_MANY` / `UNSUPPORTED_OVERLAP` | show typed overlap evidence/policy; no fabricated discrete list | no point from overlap alone |
| `MIXED_FINITE_OVERLAP` | show both overlap and isolated finite contributions | isolated admissible tokens may create points; overlap does not |
| `NON_CURRENT` / revision mismatch | mark stale/noncurrent and request recompute | existing point becomes undefined; no stale coordinates |
| `AMBIGUOUS_CONTINUATION`, `IDENTITY_DISCONTINUITY`, or no continuation | show lineage event and require explicit reselection if a new token is admissible | never retarget automatically |
| `WORK_LIMIT_REACHED` or numerical failure | show work/status/partial evidence; offer explicit retry policy when allowed | no point unless the published result independently contains a current admissible solution under the approved contract |
| unsupported target/domain | show typed reason | no point |

The exact admissibility checks include successful computation, finite or mixed
finite geometry, currentness, supported evidence, a unique matching token,
locally established isolation, admissible identity status, explicit continuation
key and matching revision/lineage
(`source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionResult2D.java:115-160`).

## 11. Tool and inspector behavior

### Creation tool

The Locus V2 tool requests the dependent point and then either an unambiguous
supported point generator or the scalar state/true driver/domain roles required
by the chosen overload. It previews the explicit domain, periodicity,
branches/components and mapping, then creates one labelled V2 object through the
public processor. Invalid or ambiguous generator state is rejected before
construction commit.

A separate supported point-on-Locus V2 action selects the semantic
branch/component/preimage and creates the ordinary bound point described in
§6. It may then be selected as the point generator of another V2. The action
does not turn `GeoLocusV2` into generic `Path`.

### Length tool

The tool accepts one V2 for total length or a V2 plus two semantic-position
points for partial length. It creates a rich result and opens/focuses its
inspector. Arbitrary coincident points are rejected with a preimage-ambiguity
message. The standard `Length[GeoLocusV2]` action creates or
reuses the same authoritative rich query and exposes only its guarded scalar
child.

### General Intersect tool

Existing mode `5` accepts a V2 and any supported G8 target, including another
V2. It creates the rich result first. Candidate markers are presentation only.
Selecting a marker creates a separate exact-token point dependency. Existing
line/conic/function/etc. behavior delegates unchanged to the upstream
controller.

### Result inspector

The inspector is the recommended diagnostic surface. It shows rich typed fields
and offers only admissible actions. A new string/list `Diagnostics` command is
not recommended until a stable typed diagnostic schema has independent value.

## 12. Command registration and runtime gates

An approved implementation would require:

- command enum/localization/help registration for the chosen new names;
- processors and dispatcher/factory integration;
- real algorithm command identifiers rather than `Algos.Expression`;
- GeoCeDG controller actions for V2 creation, supported point-on-Locus
  preimage selection and V2-aware extension of general Intersect mode `5`;
- a runtime feature service consuming `cedg.locus.v2`;
- consistent command, tool, menu/help and existing-file load policy.

The umbrella feature remains `cedg.locus.v2`, experimental and default-off.
Implementations may derive subcapabilities for create/metric/intersection, but
no subflag may bypass the umbrella or its dependencies. Dual-run diagnostics
remain internal/Laboratory behavior.

The workspace only exposes actions; it does not enable the command. Algebra
input and toolbar creation must consult the same runtime decision.

## 13. Lifecycle and persistence prerequisites

G9U0 cannot pass until the following are normative and implemented:

- durable locus identity distinct from label, coordinate, archive UUID,
  construction order and revision;
- reconstructible parent algorithm, typed generator descriptor and evaluator
  inputs;
- serialized semantic/provider versions, true scalar driver and mapping or
  point-support inputs, domain/orientation/periodicity, branch/component
  contract and policy;
- semantic point persistence that separates durable preimage address
  (including periodic lift/seam evidence) from source-revision/component/
  continuation binding;
- copy/duplicate semantics: user duplicate receives a new durable locus ID and
  generator ID as applicable, with internally rewritten point/query/token
  identities while external referenced supports follow the approved copy policy;
- `set`/assignment semantics or an explicit public prohibition handled without
  throwing during ordinary application workflows;
- undo/redo restores the same construction identity for the restored operation;
- deletion invalidates/removes dependents without stale metric/intersection
  payload;
- serialization of rich-query inputs/policies and selected token lineage;
- deterministic recomputation on reopen before any numeric/point output is
  considered current;
- normal-DAG cycle validation during creation, redefine and load, with no hidden
  generator dependency graph;
- no serialized render cache or numeric snapshot as semantic authority.

Rich metric/intersection results may persist reconstructible queries and policy;
their revision-bound computed snapshots are recomputed on load. A point on V2
persists its semantic preimage/continuation evidence, and a token-selected point
persists its exact durable token/lineage evidence. If recomputation cannot
re-establish either identity, the point and its dependents are undefined with a
diagnostic; no coordinate repair occurs.

These needs favor a minimal shared general identity/lifecycle foundation before
G9U0 (Sequence B). Creating export-only or Locus-only fake stable IDs is not
recommended.

## 14. Classic and old-file compatibility

- Old `.ggb` files remain unassociated legacy constructions. No load-time
  migration from `GeoLocus` to V2 occurs.
- Legacy `Locus`, mode `47`, and `Length[GeoLocus]` retain existing behavior.
- In the GeoCeDG Classic diagnostic launcher/path, persistent V2 objects and
  rich intersection/metric results remain their native types. Their semantic
  IDs, tokens, bindings, supported save/reopen behavior, and recomputation use
  the same GeoCeDG kernel semantics. The diagnostic path exposes no creation
  action and rejects/filters interactive V2 creation.
- File loading must not depend on an interactive command filter. The shared XML
  factory reconstructs an already supported object even when creation is off.
- Saving a V2-bearing file warns that an external upstream GeoGebra
  distribution without the GeoCeDG semantic extension is outside the
  compatibility guarantee and may not interpret the new object. G9U0/G9A3 must
  characterize that unsupported-open behavior.
- A failed or unknown semantic version must preserve the file where possible
  and expose an unsupported object state; it must not convert to a sampled
  legacy Locus or coordinate/list approximation silently. Unsupported external
  open behavior must never be "repaired" through a lossy conversion.

## 15. Localization, help and icons

Every proposed command/action/result state needs GeoCeDG-owned localization
keys for syntax, argument roles, invalid domain, ambiguous preimage,
completeness, isolation, overlap, continuation, currentness and work limits.
Hard-coded experimental type strings in current internal geos are not sufficient
for public use.

Icons must be GeoCeDG-owned, registered with rights metadata, have accessible
names, and support text fallback. The embedded legacy locus icon is evidence
only. Context help must distinguish legacy Locus and semantic Locus V2.

## 16. Phase and promotion gates

Recommended order:

1. shared durable identity/lifecycle/persistence foundation;
2. G9U0 typed scalar/support generators, point-on-Locus nesting, creation
   evaluator, commands, rich result inspectors and lifecycle;
3. G9U0 experimental tool integration and Classic compatibility;
4. G9X1 may consume public/persistent V2 read-only after G9U0 PASS;
5. G9U1 exposes approved actions in CeDG Construction;
6. stable promotion only after compatibility, scientific, performance,
   accessibility/localization and author-review gates.

Passing G6-G8 proves internal semantics, not these public gates. The explicit
22-case generator/nesting/cycle/lifecycle suite and other required tests are
listed in `docs/validation/g9_public_workspace_validation_matrix.md`.

## 17. Approved decisions and G9U0-owned API choices

| Decision | Recommendation | Main alternative | Rejection impact/gate |
|---|---|---|---|
| Creation name | `LocusV2` | `SemanticLocus`; overload `Locus` | overload requires explicit legacy migration/compatibility proof |
| Generator abstraction | typed \(\mathcal{G}:D\to S\) normalized descriptor; no public generic `Path` | enumerate unrelated driver classes | enumeration cannot express mapped scalar state, nesting or provider evolution coherently |
| Scalar surface | explicit state `t`, true driver `s`, domain `D` and reconstructible mapping | infer a driver from arbitrary ancestors or slider visibility | inference is ambiguous and presentation-dependent |
| Initial point supports | constrained point on finite segment, circle, circular arc or supported Locus V2 branch/component | segment-only pilot or unrestricted curves | segment-only fails author workflows; generic curves lack an approved semantic provider |
| Creation arguments | preserve the typed generator contract while G9U0 evaluates actual GeoGebra overload conventions and presents an author-reviewable surface | freeze an arbitrary G9P spelling | command/localization/serialization choice is owned by G9U0 |
| Position syntax | dedicated V2 `Point[L,branch,t]` or `LocusPoint`, persisting durable preimage plus revision binding | coordinate-only `Point[L]` | resolve syntax and graphical preimage chooser before freeze |
| Continuation | branch/component/provider lineage plus periodic lift/seam; undefined on ambiguity | nearest Cartesian repair | repair silently changes constructive identity |
| Metric authority | rich `LocusLength` remains authoritative | scalar-only command | scalar-only hides coverage and guarantee |
| Standard scalar | expose guarded `Length[GeoLocusV2]` by reusing/depending on the rich query | defer or calculate directly | total scalar adapter is approved; direct calculation is prohibited |
| Intersection | general `Intersect[L,T]` returns rich result | Locus-specific command | parallel framework requires stronger justification |
| Token point | `Intersect[R,"token"]` | `IntersectionPoint[R,token]` | decide before serialization syntax freezes |
| Candidate UX | rich result always; explicit token selection | auto-point when one solution | auto behavior needs protocol/identity tests |
| Diagnostics | Properties/result inspector | diagnostics command | command needs separate typed schema |
| Classic | GeoCeDG diagnostic path preserves/recomputes native V2/rich types and IDs with creation disabled; external upstream open is unsupported | reject or silently downgrade V2 files | G9A3/U0 compatibility corpus and explicit messaging required |
| Identity order | shared identity/lifecycle before U0 | Locus-local IDs | local IDs create later migration/debt and block DXF provenance |
| Feature maturity | experimental/default-off until complete lifecycle | enable by default | default-on requires stable promotion evidence |

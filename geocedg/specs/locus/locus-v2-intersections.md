# Locus V2 2D intersection semantics

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT NORMATIVE** |
| Phase | G8 planning; G8A and G8B not started |
| Scope | Internal two-dimensional Locus V2 intersection semantics |
| Product state | Experimental, internal, disabled by default |
| Approval | Explicit author approval required before this specification may become normative |

This proposal defines the questions G8A must characterize. It states no
implemented behavior and authorizes no productive source change.

## 1. Governing principles

An intersection result is derived from the semantic construction, not from a
drawn approximation. The following remain authoritative:

- `GeoLocusV2` identity and current `LocusDefinition2D` revision;
- provider-owned branch keys, valid components, orientation, periodicity, and
  canonical semantic parameters;
- deterministic semantic evaluation `F_j(t)` and its quality evidence;
- the second object's actual kernel-owned equation, parameterization, or
  incidence contract; and
- normal Construction dependency and invalidation order.

The following are never intersection authority:

- `LocusRenderCache2D`, render vertices, screen polylines, or tessellation;
- legacy `GeoLocus.myPointList` or any legacy locus sample;
- viewport, zoom, DPI, screen scale, or pixel tolerance;
- graphical proximity, labels, creation order, or output order; and
- G7 metric component state or cumulative-length partitions.

A sampled semantic partition may be a broad-phase accelerator only after the
author approves its characterized safety contract. Every candidate must still
be refined and verified against the semantic evaluator and the target's
authority.

## 2. Mathematical object

For one Locus V2 branch `j`, let

\[
F_j : V_j \longrightarrow \mathbb{R}^2,
\qquad
V_j=\bigcup_k V_{j,k},
\]

where each `V_{j,k}` is a provider-declared valid semantic-parameter component.
Intersection is solved component by component. A discontinuity or invalid gap
is not crossed by a numerical bracket and does not create an implicit geometric
connection.

### 2.1 Locus versus an analytic/incidence target

For a target object `O`, an approved target adapter supplies an authoritative
residual or incidence predicate. When a scalar residual exists,

\[
h_{j,O}(t)=R_O(F_j(t))=0,
\qquad t\in V_{j,k}.
\]

`R_O` includes a declared normalization policy. Multiplying an equation by a
nonzero scalar must not change acceptance, classification, or completeness.

The initial target authorities are proposed as follows:

| Target | Authoritative geometric form | Additional membership rule |
|---|---|---|
| line | normalized homogeneous equation `a x + b y + c = 0` from the current `GeoLine` coefficients | none beyond full-line incidence |
| segment | the support-line residual | current `GeoSegment` finite parameter/endpoints and limited-path policy |
| ray | the support-line residual | current `GeoRay` one-sided parameter and limited-path policy |
| circle | specialized, normalized circle equation derived from the current `GeoConic` state | full-circle incidence and defined/nondegenerate type |
| supported full conic | homogeneous quadratic `p^T A p = 0` from the current `GeoConic` matrix | current conic type/degeneration policy |

The target adapter must distinguish support-curve incidence from membership in
a limited object. A root on the support line but outside a segment or ray is not
an intersection with that limited object.

### 2.2 Parametric versus parametric

For semantic curves

\[
F(t),\qquad Q(u),
\]

an intersection solves

\[
F(t)-Q(u)=0
\]

over both valid semantic domains. The result must retain both source parameters
and source identities. This is a two-parameter problem with dual topology and
overlap semantics; it is deferred from the minimum G8B candidate.

### 2.3 Implicit curves

For an implicit curve with authoritative kernel expression

\[
G(x,y)=0,
\]

the locus equation is

\[
G(F_j(t))=0.
\]

The current `GeoImplicit` interface supplies `evaluateImplicitCurve(x,y)`,
`derivativeX`, `derivativeY`, and polynomial coefficients when available.
`GeoImplicitCurve` can also evaluate a non-polynomial expression when the
coefficient array is absent. These are real authority seams, but they do not by
themselves establish residual normalization, root-set completeness, or a
bounded solver. Polynomial and non-polynomial implicit curves therefore remain
Level-C characterization targets. G8 must not manufacture an implicit
conversion for unrelated objects.

### 2.4 Functions

For an explicit function with a construction-owned domain, a candidate residual
is `y-f(x)`. The current `GeoFunction.getMinParameter()` and
`getMaxParameter()` are view-clipped, so they are not an acceptable semantic
domain. Functions remain deferred until a type-specific, view-independent
domain and discontinuity contract is approved.

## 3. Query model

The proposed immutable query records at least:

```text
source locus runtime identity
source semantic revision
target runtime identity and target kind
intersection algorithm/policy version
root-isolation policy
residual/tangency/deduplication/continuation policy versions
deterministic work budget
requested support/completeness level
```

The target runtime identity is scoped to the active Construction. Public or
persistent target identity is outside G8. The query algorithm owns a monotonic
input/topology revision for coherent publication because ordinary analytic
GeoElements do not expose a semantic revision equivalent to `GeoLocusV2`.

## 4. Rich result model

The proposed result is a closed immutable set value, conceptually
`LocusIntersectionResult2D`, with independent axes rather than a nullable list
of points.

### 4.1 Query-level axes

| Axis | Proposed values | Meaning |
|---|---|---|
| computation | `SUCCESS`, `INVALID_INPUT`, `UNSUPPORTED`, `NUMERICAL_FAILURE`, `WORK_LIMIT_REACHED` | whether the requested computation executed under its contract |
| coverage | `COMPLETE`, `PARTIAL`, `NOT_ESTABLISHED` | whether the complete valid source domain was resolved |
| geometry kind | `EMPTY`, `FINITE`, `OVERLAP`, `INFINITE`, `UNRESOLVED` | established geometric shape of the result set |
| currentness | `CURRENT`, `NON_CURRENT` | whether the payload is bound to the publisher's current input revision |
| support level | `EXACT`, `CERTIFIED`, `VERIFIED_UNCERTIFIED`, `UNSUPPORTED` | strength of the intersection claim, separate from coordinate guarantee |

The final names require G8A review. The following combinations are mandatory:

- `EMPTY` requires `COMPLETE` coverage;
- `FINITE` with `PARTIAL` coverage means a verified subset, not the complete
  result set;
- `OVERLAP` and `INFINITE` require affirmative evidence and carry no arbitrary
  sampled substitute;
- `WORK_LIMIT_REACHED` and unresolved tangency cannot be presented as `EMPTY`;
- an invalid or unsupported input carries no stale solution list; and
- a result may be structurally defined and diagnostic even when it cannot
  yield ordinary point outputs.

### 4.2 Source and provenance fields

Every result records:

- source locus identity and semantic revision;
- target runtime identity, target kind, and coherent input revision;
- branch/component coverage attempted and completed;
- algorithm, target-adapter, policy, and tolerance versions;
- aggregate support and G6 `NumericGuarantee` values;
- deterministic work consumed versus allowed;
- cache/index mode and cache-disabled equivalence provenance; and
- immutable typed diagnostics.

### 4.3 Solution value

Each finite solution records at least:

```text
root continuation token
topology epoch and parent/child root lineage
locus identity and source semantic revision
branch key
resolved valid-component binding
provider-canonical semantic parameter
optional isolating semantic-parameter interval
target parameter when the target is parametric
evaluated finite world coordinate
raw and normalized residual evidence
absolute/relative residual policy used
root-isolation and refinement method
iteration/evaluation evidence
contact-order classification and its evidence
domain-location classification
source regularity
support level and G6 NumericGuarantee
currentness and typed diagnostics
```

Coordinates exist only for a verified finite solution. NaN, infinity, negative
sentinels, magic multiplicity values, and null-as-state are prohibited.

## 5. Classification taxonomy

Classification is orthogonal; one enum must not collapse contact, domain
location, regularity, and result-set geometry.

### 5.1 Contact order

- `TRANSVERSE`: a simple crossing is established;
- `TANGENT`: an even or otherwise non-transverse contact is established;
- `HIGHER_MULTIPLE`: order greater than the accepted tangent baseline is
  established by analytic, differential, or certified evidence;
- `UNKNOWN_MULTIPLICITY`: a root is verified but order is not defensible.

No tangent or higher-multiple classification follows from a small residual
alone. Sign-change isolation is insufficient for even-multiplicity roots.

### 5.2 Domain location

- `INTERIOR`;
- `INCLUDED_ENDPOINT`;
- `PERIODIC_SEAM`; and
- `ISOLATED_COMPONENT` when the provider declares a zero-dimensional valid
  component.

An open endpoint is not a root merely because a limiting residual is small.
Limit intersections require a separately approved limit contract.

### 5.3 Non-solution and set-level cases

- a discontinuity or invalid-domain boundary is a typed diagnostic and
  isolation boundary;
- a collapsed branch intersecting the target may represent infinitely many
  constructive preimages at one coordinate and must not be silently reduced to
  one ordinary root;
- coincident/overlapping geometry is a positive set-level result;
- ordinary absence is `EMPTY` only after complete coverage; and
- unsupported or unresolved numerical cases remain distinct from absence.

## 6. Dynamic root identity

### 6.1 Identity context

A root continuation token is durable only within the active nonpersistent G8
algorithm and its source pair. Its binding contains:

```text
source-pair identity
topology epoch
branch key and valid-component context
provider-canonical root parameter or isolating interval
optional target parameter
lineage relation to previous/current tokens
```

The current locus revision and target/input revision establish currentness; they
are not, by themselves, the durable token. Labels, output indices, coordinates,
render order, and nearest-neighbour screen/world matching are excluded.

### 6.2 Continuation without topology change

Continuation may preserve a token only when evidence establishes the same
isolated semantic root: compatible source branch/component lineage, overlapping
or predictably continued isolation intervals in semantic parameter space,
unchanged target family, and successful refinement/verification at the current
revision. Parameter distance is governed by a versioned continuation policy.
Cartesian nearness may be logged but cannot decide identity.

### 6.3 Topology events

The proposed event policy is:

| Event | Identity effect |
|---|---|
| two simple roots merge at tangency | parents terminate; one new tangent-event token records `MERGED` lineage |
| tangent root splits into two | parent terminates; two new tokens record `SPLIT` lineage |
| root crosses a provider periodic seam | preserve one token using canonical seam equivalence and a lifted continuation coordinate |
| root reaches an included component boundary | preserve through the boundary event only while the root remains valid; record boundary classification |
| root crosses an invalid gap/open boundary | terminate; any later root is new unless approved provider lineage proves a semantic continuation |
| source branch splits/merges | follow explicit G6 branch lineage, then create corresponding root lineage; no guessed association |
| source becomes undefined | current result becomes a coherent failure/non-current payload; no old coordinate survives as current |
| source recovers | reuse a token only if the approved continuation evidence spans the event; otherwise create a new topology epoch |

G8A must test and the author must approve this policy. Unsupported topology
families return an explicit state instead of fabricated continuity.

## 7. Numerical capability and evidence

### 7.1 Capability hierarchy

The proposed preference order is:

1. exact symbolic/arithmetic intersection capability;
2. certified interval residual/bounds capability;
3. deterministic differential capability with safeguarded isolation and
   refinement;
4. deterministic evaluator-only adaptive capability; and
5. unsupported.

An exact target equation does not upgrade floating-point locus evaluations.
The result directly reuses
`LocusSemanticMetadata2D.NumericGuarantee` for coordinate/numeric assurance and
records a separate intersection support/coverage level.

### 7.2 Isolation and refinement

G8A must compare:

- interval/subdivision isolation over each valid semantic component;
- sign-change brackets for odd roots;
- stationary-point, residual-minimum, derivative, or interval-Newton evidence
  for even/multiple roots;
- safeguarded Newton/Brent-style refinement where its preconditions hold;
- endpoint and periodic-seam checks under provider policy; and
- optional semantic bounding boxes or spatial indexes as broad phase only.

Every candidate is re-evaluated through a coherent
`LocusEvaluationSession2D`, checked against limited-target membership, and
verified with normalized residual evidence. A candidate rejected at verification
is diagnostic evidence, not a hidden point.

### 7.3 Completeness

An evaluator-only adaptive scan may find and verify roots but cannot claim that
none were missed unless it has an approved bound/certificate covering the
component. Therefore:

- verified individual roots may be returned with `PARTIAL` coverage;
- `EMPTY` requires complete isolation evidence;
- tangency candidates that cannot be resolved yield `UNRESOLVED`; and
- work exhaustion yields `WORK_LIMIT_REACHED` with consumed-work evidence.

## 8. Tolerance policy

G8 uses a versioned intersection-specific policy. It must not reuse G6 domain
epsilon, the G6 validation envelope, G7 metric tolerances, render tolerances,
pixel tolerances, or `Kernel.MIN_PRECISION` without measured justification.

G8A must characterize at least:

- semantic parameter/root-isolation tolerance;
- normalized absolute residual tolerance;
- normalized relative residual tolerance;
- tangency/derivative or stationary-evidence threshold;
- semantic-parameter deduplication tolerance;
- topology-continuation tolerance; and
- optional coordinate-consistency tolerance used only as verification
  evidence, never identity.

No numerical default becomes normative before measured evidence and author
approval. The policy key includes every result-affecting quantity and its
version.

## 9. Deterministic work and state

Every query has independent finite maxima for:

- semantic evaluations;
- derivative evaluations;
- candidate intervals/boxes;
- isolation subdivisions and depth;
- refinement iterations;
- residual evaluations;
- candidate/verified-solution count;
- continuation operations; and
- retained index entries/bytes when an index is enabled.

Exhausting one limit returns a typed result. It never silently truncates a
complete set. Wall-clock time is diagnostic only unless a future author-approved
runner-specific policy says otherwise.

## 10. Dependency and lifecycle contract

- The intersection algorithm registers the locus and target as normal inputs.
- At recompute start, the rich output drops currentness of the previous payload
  before private work begins.
- Private builders publish one complete immutable success/failure payload
  atomically; exceptions publish no partial candidate/index state.
- Undefined inputs, topology revisions, policy changes, removal, undo/redo,
  copy, and assignment follow explicit tested semantics.
- No callback-only dependency, static/global cache, foreign Construction
  reference, or obsolete-revision payload is retained.
- Ordinary point outputs, if approved, are bounded derived views keyed by root
  tokens. Extra slots become undefined; slot association never defines root
  identity.

## 11. Cache/index contract

The default proposal is query-local isolation state plus the existing bounded
semantic evaluation session. No G7 metric cache is reused.

If G8A proves cross-query reuse necessary, a dedicated intersection owner must:

- be scoped to one active source locus and Construction;
- key state by source/target identities and revisions, branch/component,
  adapter/capability/algorithm versions, full tolerance policy, topology epoch,
  and work budget;
- retain only immutable isolation/bounds state, never query results, point
  outputs, or root identity history;
- bound entries and deterministic eviction;
- invalidate obsolete source/target revisions before reuse;
- release state when the final consumer or source is removed; and
- produce the same full rich result with cache/index disabled.

Root continuation history is separately bounded by the current output/root
capacity and topology epoch. It is not an unbounded cache.

## 12. Compatibility boundary

Until a separate author decision:

- legacy `GeoLocus` and Classic intersections are unchanged;
- `GeoLocusV2` remains experimental/internal and disabled by default;
- no existing `.ggb` is migrated or changes meaning;
- no public command, dispatcher route, `Path`, or point-on-locus API is added;
- no XML, `GeoFactory`, serialization, or migration is added;
- no 3D/G9, G5 export, Python DSL, or frontend behavior is added; and
- G8 internal result support does not imply public incidence support for every
  GeoGebra curve.

## 13. Open approval gate

This proposal becomes normative only after G8A evidence resolves and the author
explicitly approves at least: result/public-point architecture, root identity
and topology events, tangency/multiplicity evidence, overlap semantics,
capability hierarchy, tolerance values, isolation/refinement strategy, cache
ownership, work/output bounds, minimum target families, deferred Level C, and
public/persistence boundaries.

```text
G8 SPEC = PROPOSED / NOT NORMATIVE
G8A = NOT STARTED
G8B = NOT STARTED
```

# Locus V2 2D intersection semantics

| Field | Value |
|---|---|
| Status | **PROPOSED — NOT NORMATIVE** |
| Phase | G8 planning `PASS — AUTHOR APPROVED`; G8A authorized/not started; G8B not authorized |
| Scope | Internal two-dimensional Locus V2 intersection semantics |
| Product state | Experimental, internal, disabled by default |
| Approval | Explicit author approval required before this specification may become normative |

This proposal defines the questions the separately authorized G8A must
characterize. Planning approval does not make this specification normative,
accept ADR 0008, execute G8A, or authorize productive source change.

## 1. Governing principles

### 1.1 Fundamental CeDG requirement

Locus V2 must participate as a first-class geometric entity in intersection
and incidence operations with each supported family of ordinary 2D geometric
objects. Every finite solution must be semantically identifiable, and that
identity must participate correctly in normal dynamic dependencies when the
construction changes.

This is a structural CeDG requirement because locus-defined projection curves
are genuine intermediate geometric results used by later descriptive-geometry
procedures:

```text
CeDG construction
    -> Locus V2 geometric projection
    -> native intersection with another 2D geometric entity
    -> identified intersection solution(s)
    -> downstream CeDG construction
    -> normal dynamic propagation
```

A solution may serve as a stable input to later construction steps whenever
geometric continuation is unambiguous. It must not be reduced to an anonymous
coordinate computed at one revision. Constructive traceability,
branch/component provenance, semantic parameterization, solution identity,
dynamic update, topology changes, and degenerations remain explicit. This
requirement does not imply broad initial family support; each target family is
promoted separately after evidence.

### 1.2 Geometric authority

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
| completeness | `COMPLETE`, `INCOMPLETE`, `NOT_ESTABLISHED` | whether every solution in the supported semantic query domain has been accounted for |
| geometry kind | `EMPTY`, `FINITE`, `OVERLAP`, `INFINITELY_MANY`, `UNSUPPORTED_OVERLAP`, `UNRESOLVED` | established geometric shape of the result set |
| currentness | `CURRENT`, `NON_CURRENT` | whether the payload is bound to the publisher's current input revision |
| support level | `EXACT`, `CERTIFIED`, `VERIFIED_UNCERTIFIED`, `UNSUPPORTED` | strength of the intersection claim, separate from coordinate guarantee |

The final names require G8A review. The following combinations are mandatory:

- `EMPTY` requires `COMPLETE` completeness;
- `FINITE` with `INCOMPLETE` means a verified subset, not the complete
  result set;
- `FINITE` with `NOT_ESTABLISHED` means the capability has not determined
  whether other solutions exist;
- `OVERLAP` and `INFINITELY_MANY` require affirmative evidence and carry no
  arbitrary sampled substitute;
- `UNSUPPORTED_OVERLAP` records a detected overlap whose complete geometry
  cannot be resolved under the current capability;
- `WORK_LIMIT_REACHED` and unresolved tangency cannot be presented as `EMPTY`;
- an invalid or unsupported input carries no stale solution list; and
- a result may be structurally defined and diagnostic even when it cannot
  yield ordinary point outputs.

### 4.2 Source and provenance fields

Every result records:

- source locus identity and semantic revision;
- target runtime identity, target kind, and coherent input revision;
- branch/component search coverage attempted and completed as evidence for the
  independent completeness axis;
- algorithm, target-adapter, policy, and tolerance versions;
- aggregate support and G6 `NumericGuarantee` values;
- deterministic work consumed versus allowed;
- cache/index mode and cache-disabled equivalence provenance; and
- immutable typed diagnostics.

### 4.3 Solution value

Each finite solution records at least:

```text
root continuation token
identity/continuation status
topology epoch and established or candidate parent/child root lineage
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

### 5.1 Contact and multiplicity

Contact classification and multiplicity evidence are separate:

- `TRANSVERSE_ESTABLISHED`: transverse crossing is established;
- `TANGENT_ESTABLISHED`: non-transverse/tangent contact is established even if
  its exact multiplicity is not;
- `CONTACT_UNDETERMINED`: the root is verified but contact class is not; and
- multiplicity is either an established positive integer/order with its
  analytic, differential, or certified evidence, or `NOT_ESTABLISHED`.

`HIGHER_MULTIPLE` may be a derived classification only when the relevant order
is established. Neither tangency nor multiplicity follows from a small
residual alone. Sign-change isolation is insufficient for even-multiplicity
roots, and uncertainty must not become a false transverse or no-root result.

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
- ordinary absence is `EMPTY` only with `COMPLETE` completeness; and
- unsupported or unresolved numerical cases remain distinct from absence.

## 6. Dynamic root identity

### 6.1 Identity context

A root continuation token is durable only within the active nonpersistent G8
algorithm and its source pair. The candidate durable/continuation information
contains:

```text
source-pair identity
constructive intersection lineage
applicable branch lineage
topology/continuation context
explicit continuation relation when established
```

The separate revision-scoped numerical/localization evidence contains:

```text
current locus and target/input revisions
resolved valid-component binding for that revision
provider-canonical root parameter
isolating semantic-parameter interval
optional target parameter
residual and tolerance evidence
solver/refinement state or certificate
```

A root isolating interval is localization/certification evidence for a
revision. It is not, by itself, fundamental durable identity. An equivalent
monotone reparameterization must not create a new geometric intersection merely
because the parameter value or isolating interval changes. Labels, output
indices, coordinates, render order, and nearest-neighbour screen/world matching
are excluded.

The proposed per-root identity status is closed and independent of numeric
validity, with values equivalent to `CONTINUATION_ESTABLISHED`,
`NEW_TOPOLOGICAL_SOLUTION`, `AMBIGUOUS_CONTINUATION`,
`IDENTITY_DISCONTINUITY`, and `NOT_ESTABLISHED`. G8A may refine the names, but it
must not omit the ambiguous/not-established outcomes.

### 6.2 Continuation without topology change

Continuation may preserve a token only when evidence establishes the same
semantic root through compatible source/branch lineage, topology context,
successful current-revision refinement/verification, and a proven continuation
relation. Mapped or predictably continued intervals and semantic parameters may
support that relation, but are revision-scoped evidence rather than identity.
G8A must characterize ordinary source motion, equivalent monotone
reparameterization, allowed orientation reversal, and periodic-seam
representation. Cartesian nearness may be logged but cannot decide identity.

### 6.3 Topology events

The following merge/split policy is a strong **G8A hypothesis**, not an
approved universal identity semantic:

| Event | Identity effect |
|---|---|
| two simple roots merge at tangency | candidate: parents terminate; one tangent-event token records `MERGED` lineage when robustly established |
| tangent root splits into two | candidate: parent terminates; two tokens record `SPLIT` lineage when child correspondence is robustly established |
| root crosses a provider periodic seam | preserve one token using canonical seam equivalence and a lifted continuation coordinate |
| root reaches an included component boundary | preserve through the boundary event only while the root remains valid; record boundary classification |
| root crosses an invalid gap/open boundary | terminate; any later root is new unless approved provider lineage proves a semantic continuation |
| source branch splits/merges | follow explicit G6 branch lineage, then create corresponding root lineage; no guessed association |
| source becomes undefined | current result becomes a coherent failure/non-current payload; no old coordinate survives as current |
| source recovers | reuse a token only if the approved continuation evidence spans the event; otherwise create a new topology epoch |

G8A must test `2 -> 1 -> 2` and reverse traversal, symmetric cases with
intrinsically ambiguous child correspondence, periodic-seam interaction, and
branch/component changes near the same event. Preserve identity when
continuation is geometrically unambiguous; otherwise expose ambiguity or an
identity discontinuity. If universal genealogy fails, G8A must recommend a
narrower rigorous contract. Unsupported topology families return an explicit
state instead of fabricated continuity.

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
records separate intersection support and completeness levels.

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

Completeness answers a different question from per-root validity: has every
intersection satisfying the query over the supported semantic domain been
accounted for? Solver convergence and several verified roots do not establish
that no additional root was missed.

- `COMPLETE` requires approved exhaustive isolation/exclusion evidence over the
  full supported query domain. A complete empty result is valid.
- `INCOMPLETE` means at least one returned root may be individually verified,
  but known unprocessed/unresolved domain or candidates prevent an exhaustive
  set claim.
- `NOT_ESTABLISHED` means the available capability cannot determine whether the
  set is exhaustive.
- tangency candidates that cannot be resolved yield `UNRESOLVED`, never empty;
  and
- work exhaustion records consumed work and cannot silently truncate a
  complete set.

G8A must characterize completeness independently for tangencies,
evaluator-only methods, unbounded domains, difficult multiple roots, and
unsupported/incomplete broad phase. Every strategy reports verified-root count,
completeness value, and the method/evidence that established or failed to
establish it. Any later scalar or point projection must preserve or reject
incomplete/not-established set semantics rather than hide them.

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

The author-approved starting point is query-local isolation state plus the
existing bounded semantic evaluation session. No G7 metric cache is reused.

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

The author has approved the planning architecture: rich immutable set plus a
normal-DAG nonnumeric rich Geo, optional derived ordinary points, query-local
first computation, the preferred core-four family scope, and the public/API
boundaries. This proposal becomes normative only after G8A evidence resolves
and a second author review explicitly approves at least the exact root identity
invariance and topology/genealogy contract, completeness establishment rules,
tangency/multiplicity evidence, overlap taxonomy, capability hierarchy,
tolerance values, isolation/refinement strategy, any cache ownership,
work/output bounds, final target families, and any Level C promotion.

```text
G8 SPEC = PROPOSED / NOT NORMATIVE
G8A = AUTHORIZED / NOT STARTED
G8B = NOT AUTHORIZED / BLOCKED ON G8A PASS — AUTHOR APPROVED
```

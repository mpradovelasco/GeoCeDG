# Locus V2 2D intersection semantics

| Field | Value |
|---|---|
| Status | **NORMATIVE / AUTHOR-APPROVED R1 BASE; G9U0-R4 DETERMINISTIC-SELECTION REFINEMENT — PASS — AUTHOR APPROVED** |
| Version | `1.2` with bounded G9U0-R4 refinement |
| Phase | G8 planning, G8A, G8B-R1, G8B, G8C design, G8C1 and G8C2 `PASS — AUTHOR APPROVED`; G8 global `PASS — AUTHOR APPROVED`; G9U0-R4 `PASS — AUTHOR APPROVED` after retained failed intermediate smokes and final four-root/reactivation re-smokes `PASS` |
| Scope | Internal two-dimensional Locus V2 intersection semantics |
| Product state | Experimental, internal, disabled by default |
| Approval | G8 base author-approved on 2026-08-14/15; the author made deterministic semantic selection authoritative for R4, accepted bounded intrinsic phase/rank on 2026-08-29 and approved final R4 after both final re-smokes |
| Architecture decision | Accepted ADR 0008 with R1 clarification; Accepted ADR 0017 for the bounded public single-Locus phase/rank selector |

This normative contract incorporates the G8A characterization evidence and
the final author decisions D1–D17. The separately invoked G8B task produced an
internal implementation within the boundary below. The final author review
approves both that minimum kernel and the author-directed G8B-R1 normative
clarification of selected-solution admissibility. Approval does not make the
behavior public or close the remaining extended G8 families.

The G9U0-R4 candidate adds only the bounded public current-snapshot selection
refinement in section 6.2, its tolerance clarification in section 8.2 and its
public token lifecycle in section 10. Those paragraphs record the author's
deterministic policy and Accepted ADR 0017; implementation is `PASS — AUTHOR
APPROVED`. The four-root correction adds an intrinsic oriented
domain phase/rank only to a repeated base selector; its adaptive periodic
phase-tube/cell certificate is separate revision-scoped topology evidence. The
correction neither admits extrinsic enumeration order nor weakens numerical,
topology or ambiguity evidence.

G9U0-R4 is `PASS — AUTHOR APPROVED`. G9U0-R5 and G9U1 remain unexecuted;
this refinement does not authorize either successor.

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

A sampled semantic partition may be a broad-phase accelerator only when its
conservative safety contract is established. Every candidate must still
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

The initial target authorities are as follows:

| Target | Authoritative geometric form | Additional membership rule |
|---|---|---|
| line | normalized homogeneous equation `a x + b y + c = 0` from the current `GeoLine` coefficients | none beyond full-line incidence |
| segment | the support-line residual | current `GeoSegment` finite parameter/endpoints and limited-path policy |
| ray | the support-line residual | current `GeoRay` one-sided parameter and limited-path policy |
| circle | signed model-distance-equivalent residual derived from the current, verified nondegenerate `GeoConic` circle state | full-circle incidence |

The target adapter must distinguish support-curve incidence from membership in
a limited object. A root on the support line but outside a segment or ray is not
an intersection with that limited object. Full conics are deferred from the
minimum G8B family together with functions, general implicit curves and
locus–locus intersections.

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
bounded solver. The author-approved G8C1 extension now defines a regular,
finite-coefficient polynomial subset; singular and nonpolynomial/general
implicit curves remain deferred. G8 must not manufacture an implicit conversion
for unrelated objects.

### 2.4 Functions

For an explicit function with a construction-owned domain, a possible residual
is `y-f(x)`. The current `GeoFunction.getMinParameter()` and
`getMaxParameter()` are view-clipped, so they are not an acceptable semantic
domain. The author-approved G8C1 extension therefore supports only real
`GeoFunction` targets with an explicit finite semantic domain and established
valid components; unrestricted/view-bounded functions remain deferred.

## 3. Query model

The immutable query records at least:

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

The result is a closed immutable set value, conceptually
`LocusIntersectionResult2D`, with independent axes rather than a nullable list
of points.

### 4.1 Query-level axes

| Axis | Values | Meaning |
|---|---|---|
| computation | `SUCCESS`, `INVALID_INPUT`, `UNSUPPORTED`, `NUMERICAL_FAILURE`, `WORK_LIMIT_REACHED` | whether the requested computation executed under its contract |
| completeness | `COMPLETE`, `INCOMPLETE`, `NOT_ESTABLISHED` | whether every solution in the supported semantic query domain has been accounted for |
| geometry kind | `EMPTY`, `FINITE`, `OVERLAP`, `INFINITELY_MANY`, `UNSUPPORTED_OVERLAP`, `UNRESOLVED` | established geometric shape of the result set |
| currentness | `CURRENT`, `NON_CURRENT` | whether the payload is bound to the publisher's current input revision |
| support level | `EXACT`, `CERTIFIED`, `VERIFIED_UNCERTIFIED`, `UNSUPPORTED` | strength of the intersection claim, separate from coordinate guarantee |

The following combinations are mandatory:

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
local semantic-root isolation status (`ESTABLISHED` or `NOT_ESTABLISHED`)
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

When transverse contact orientation participates in the G9U0-R4 current-root
germ, it is a canonical geometric orientation, not the arbitrary sign of one
algebraic representation. Equivalent nonzero scalar representations preserve
the germ for an unoriented line/segment, a central conic, a parabola and a
regular polynomial implicit curve. Ray direction remains semantic and is not
canonicalized away as unoriented support-line freedom.

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

An internal G8 continuation token remains bounded to its active algorithm and
source pair. A public G9U0 exact token is durable through its owning rich result
and persisted token ledger. In both cases the semantic identity context
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

The durable selector/token binding and the current topology certificate are
separate axes. The selector says which semantic root an opaque allocation names;
the current certificate says whether that allocation is admissible in this
revision. Failure to establish current admissibility does not authorize another
root to acquire the token.

The per-root identity status is closed and independent of numeric validity,
with values equivalent to `CONTINUATION_ESTABLISHED`,
`DETERMINISTIC_SELECTION_ESTABLISHED`, `NEW_TOPOLOGICAL_SOLUTION`,
`AMBIGUOUS_CONTINUATION`, `IDENTITY_DISCONTINUITY`, and `NOT_ESTABLISHED`.

### 6.2 Continuation without topology change

Internal G8 continuation may preserve a token only through its approved
explicit semantic relation. Mapped intervals and semantic parameters may
support that relation, but remain revision-scoped evidence rather than
identity. G8A characterized ordinary source motion, equivalent monotone
reparameterization, allowed orientation reversal and periodic seams.

For the public one-parameter G9U0-R4 subset, current deterministic selection is
authoritative. Every eligible current root first defines a base selector from
its stable semantic component lineage plus a typed oriented transverse
current-root germ. The germ carries the contact-indicator identity and
established orientation. When that base selector is already unique, it remains
the complete root selector and acquires no ordinal dimension.

Only when two or more eligible roots share that exact base selector may the
resolver enrich it with the intrinsic phase/rank authorized by ADR 0017. The
complete enriched selector binds:

```text
stable branch/component lineage
+ typed transverse germ/collision class
+ declared semantic orientation
+ periodic/nonperiodic domain kind
+ verified collision-group cardinality
+ intrinsic oriented phase/rank
```

The rank is induced by the explicit oriented one-dimensional semantic
component. Canonical root parameters and pairwise disjoint local isolating
intervals prove that ordering, but their raw values and bits are not durable
identity material. Solver discovery order, output/list position, construction
or XML index, Cartesian left/right order and UI `Solution N` remain
non-authoritative. The ordinal alone is never identity. The token ledger
separately binds the result owner, source pair, constructive lineage, topology
context, provider/parameter contract and target contract.

Contact orientation is canonical under legitimate nonzero scalar changes of
an equivalent target representation. An unoriented line or segment fixes the
sign from a deterministic support-normal convention. A central conic fixes it
from the center level, a parabola from the nonzero quadratic trace, and a
regular polynomial implicit curve from the leading nonzero monomial ordered by
total degree then x degree. Exact zero padding does not affect that polynomial
rule. A ray retains its directed support orientation because reversing a ray
is a semantic change, not an equivalent representation.

On a nonperiodic component, phase rank starts at the declared component start
and follows the declared orientation. On a complete periodic component, the
explicit oriented fundamental interval is the deterministic phase frame and
its half-open endpoints represent one semantic parameter. A repeated-selector
interval that reaches that seam is not assigned by a heuristic. The affected
ranked identity becomes non-current. Insufficient intrinsic cyclic evidence
places its complete prior token group in durable periodic quarantine. A later
unique cyclic offset zero releases/reuses that group; a unique proved nonzero
offset reports a typed discontinuity and permanently retires it; absent or
multiple offsets keep it quarantined. This is the explicit monodromy boundary: R4
does not add Cartesian proximity, a mutable winding counter or movement history
to manufacture a globally continuous circle labeling.

Only a complete base or enriched selector unique among the current published
finite roots for that constructive result may allocate or resolve a durable
token. Unique selectors are processed in canonical selector order. On first
publication they receive distinct opaque ledger allocations and report
`NEW_TOPOLOGICAL_SOLUTION` / `APPEARED`, without claiming historical
continuation. On recomputation, the same exact ledger binding reports
`DETERMINISTIC_SELECTION_ESTABLISHED` even when its semantic parameter has
moved. Exact parameter equality across revisions is not required.

Previous/current root comparison may provide bounded continuity and topology
diagnostics. It may confirm that deterministic selection was also continuous,
but it cannot select, mint, transfer or replace a token. Thus the same final
regular snapshot has the same binding after direct, incremental,
forward/reverse or save/reopen histories. Determinism remains authoritative
when a continuity heuristic would disagree.

There is one conservative prior-reuse guard for a complete ranked periodic
collision group. Current reuse of any old token requires an adaptive intrinsic
periodic phase-tube/cell certificate. Prior and current canonical phases and
disjoint isolating intervals must define nonintersecting same-rank tubes inside
the nearest cyclic root gaps, yielding a complete bijection with the same
complete selectors. A fixed component fraction such as `span / 256`, an
isolation subdivision count and one UI update size are not topology evidence.
A missing, ambiguous, incomplete or non-bijective certificate fails closed into
durable periodic quarantine. A unique offset zero releases the exact group, a
proved unique nonzero cyclic selector shift permanently retires only the
affected ranked group, and absent/nonunique offset evidence retains quarantine;
an unaffected group remains current. The certificate still does not select the
current root and never becomes ledger identity. For an ordinary nonperiodic
group, absence of the bounded relation is not identity authority and does not
override a unique current-snapshot selector.

If the repeated base-selector group does not have coherent component,
orientation and domain-kind evidence, pairwise disjoint isolating intervals or
one verified cardinality, it remains ambiguous and fails closed with
non-durable revision-local handles. Collision-group membership/cardinality
change, orientation or component redefinition, tangency, merge/split, overlap,
root-local unresolved evidence, non-isolation and a proved periodic rank
rotation are permanent identity barriers. A rotation that merely cannot be
excluded because the relation is missing, ambiguous, budget-exhausted,
incomplete or non-bijective is a fail-closed quarantine condition, not evidence
that a rotation occurred. No prior token is transferred to whichever root later
occupies the same integer rank.

Each unresolved candidate records its exact semantic component. One on the
solution's resolved valid component is a local veto; one on another component
affects global/work evidence but does not veto the independently established
root. Global enumeration status remains a separate axis.

The current R4 author smoke has two cases. Case A, the canonical two-root
midpoint/circle construction, passed. Case B's byte-exact four-solution
construction initially publishes four current, transverse, locally isolated
roots with `ESTIMATED_ERROR`, zero unresolved candidates and global
`NOT_ESTABLISHED`. All share one semantic component; two have the positive
oriented circle-residual germ and two the negative germ. Each repeated
base-selector pair has disjoint semantic isolating intervals, so the authorized
intrinsic phase dimension produces four unique complete selectors, four exact
opaque tokens and four initially materialized points. Case B then failed during
ordinary regular motion because the former fixed `span / 256` relation rejected
one valid root displacement in each group. The adaptive phase-tube certificate
is the bounded correction; author re-smoke remains pending. No part of this
chronology establishes a stronger numerical certificate or global completeness.

A materialization policy MUST NOT override a duplicate complete selector,
absent exact token or ambiguous lineage. Bounded adaptive refinement may
truthfully strengthen local numerical/isolation evidence, but R4 adds no weaker
certification/materialization tier. The four-root correction is an identity
refinement, not certification relaxation.

Public ledger persistence uses strict canonical format v4 with a separate
current-root selector binding and explicit claimed-active/claimed-dormant plus
periodic-quarantine lifecycle status. `PERIODIC_QUARANTINE` (`q`) retains
complete unclaimed group evidence; `CLAIMED_PERIODIC_QUARANTINE` (`r`) retains
that evidence for an existing materialized point. Neither is current or
point-admissible. Canonical format v3 phase state remains importable, while v1
and v2 remain importable only for authentic pre-phase state. Intrinsic
phase/rank bindings are legal in v3 and v4; claimed/quarantine lifecycle states
are legal only in v4. A v3 phase state falsely relabeled as v2, or a v4 claimed/quarantined state
relabeled as an older version, is rejected rather than silently interpreted
under an earlier schema. An older exact token is preserved only under one
compatible semantic binding; otherwise migration fails closed. The external
`locus-root/v3` token envelope remains unchanged, and forged, duplicate,
incomplete or noncanonical bindings fail closed.

Legacy singleton token material is pre-phase by contract and MUST NOT be bound
to an intrinsic-phase selector. A runtime migration request for such a selector
allocates fresh opaque material rather than reusing the singleton. A persisted
snapshot manipulated to attach that legacy material to an intrinsic-phase
selector is rejected on import. This restriction is independent of the
separate versioned phase and claimed-lifecycle rejection rules.

### 6.3 Topology events

G8A rejected universal merge/split genealogy. The normative policy is to
publish explicit topology/identity events and candidate relations, while
preserving an existing token only when continuation is uniquely established:

| Event | Identity effect |
|---|---|
| two simple roots merge at tangency | parents terminate; one tangent-event token records the candidate parent set when robustly established |
| tangent root splits into two | the tangent-event token terminates; new child tokens record the candidate parent relation when robustly established |
| unique-base-selector root crosses a provider periodic seam | preserve one token only through the already approved canonical seam equivalence |
| repeated-base-selector phase interval reaches/crosses the periodic seam | quarantine while the cyclic offset is insufficient/nonunique; release only on unique offset zero; permanently retire only on proved unique nonzero offset; never rotate by proximity or winding history |
| root reaches an included component boundary | preserve through the boundary event only while the root remains valid; record boundary classification |
| root crosses an invalid gap/open boundary | terminate; any later root is new unless approved provider lineage proves a semantic continuation |
| source branch splits/merges | follow explicit G6 branch lineage, then create corresponding root lineage; no guessed association |
| source becomes undefined | current result becomes a coherent failure/non-current payload; no old coordinate survives as current |
| source recovers | reuse a token only if the approved continuation evidence spans the event; otherwise create a new topology epoch |

When merge/split evidence names several candidate parents, the implementation
MUST canonicalize the parent records once by opaque parent token and derive both
the published token list and the corresponding continuation-key list from that
same ordered record sequence. It MUST NOT sort tokens and keys independently or
lose their association. This ordering makes diagnostics deterministic; it does
not select a continuation or make token order semantic identity.

G8A tested `2 -> 1 -> 2` and reverse traversal, symmetric cases with
intrinsically ambiguous child correspondence, periodic-seam interaction, and
branch/component changes near the same event. Universal child genealogy
failed the symmetric/reverse evidence. The contract therefore uses new
topology-event tokens, candidate parent/child sets and explicit ambiguity
or discontinuity unless one semantic continuation is uniquely established.
Unsupported topology families return an explicit state instead of fabricated
continuity.

## 7. Numerical capability and evidence

### 7.1 Capability hierarchy

The author-approved capability order is:

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

G8A compared:

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
  set is exhaustive. It does not invalidate an individually established
  solution.
- tangency candidates that cannot be resolved yield `UNRESOLVED`, never empty;
  and
- work exhaustion records consumed work and cannot silently truncate a
  complete set.

Global intersection completeness and individual solution admissibility are
orthogonal. A finite current solution may be consumed as a dynamic point when
that solution is independently verified, semantically identified, locally
isolated under the current capability, revision-coherent and unambiguous under
the lifecycle/continuation contract, even if the parent result is `INCOMPLETE`
or `NOT_ESTABLISHED`. Such a point does not imply or advertise global
completeness of the parent query. Its rich-result dependency remains the source
of the parent identity, revisions, per-solution guarantee and completeness
provenance.

Residual satisfaction alone does not establish local isolation or durable
identity. The per-solution revision evidence therefore reports local isolation
separately from the numeric isolating interval. `ESTABLISHED` means that the
current finite semantic preimage is locally distinguishable sufficiently for
the claimed token/continuation status; `NOT_ESTABLISHED` keeps the point
consumer undefined even when the coordinate passes residual verification.

G8A characterized completeness independently for tangencies,
evaluator-only methods, unbounded domains, difficult multiple roots, and
unsupported/incomplete broad phase. Every strategy reports verified-root count,
completeness value, and the method/evidence that established or failed to
establish it. Any derived projection must preserve incomplete/not-established
set provenance rather than hide or strengthen it.

## 8. Tolerance and normalization policy

G8 uses the versioned policy `g8b-initial-normalized/v1`, derived from the
measured G8A candidate `g8a-measured-candidate/v1`. It must not reuse G6 domain
epsilon, the G6 validation envelope, G7 metric tolerances, render tolerances,
pixel tolerances, or `Kernel.MIN_PRECISION`. Every result-affecting quantity,
its units/normalization, provider/adapter applicability and version participate
in policy identity.

### 8.1 Target residual

Every target adapter supplies either:

1. a normalized geometric residual with a documented common meaning,
   preferably signed or unsigned model-coordinate distance; or
2. a target-family-specific typed residual and matching typed tolerance when a
   correct common distance residual is unavailable.

The residual evidence records raw value, normalization scale and provenance,
normalized value, quantity kind and units. Multiplying an authoritative
implicit equation by any nonzero scalar must not change root acceptance,
classification or completeness. A value from one residual quantity may never
be compared against a tolerance belonging to another.

For the minimum core, line/segment/ray use signed perpendicular distance to
the support line plus separate limited-object membership. A verified circle
uses a signed radial-distance-equivalent residual derived from its actual
`GeoConic` state. Undefined or degenerate targets produce typed non-success
rather than an algebraically convenient substitute.

Absolute and relative residual checks operate only on compatible normalized
quantities. Any characteristic geometric scale used by the relative check is
translation-invariant, documented by the adapter/query and independent of
viewport and coordinate-origin offset.

### 8.2 Semantic-parameter quantities

Root-isolation, semantic deduplication and continuation tolerances live in the
provider's semantic parameter space. They are not Euclidean distances. Their
meaning is bound to the provider/version and its declared parameter units or
normalization. A provider for which that meaning cannot be established is
unsupported under this policy rather than assigned a universal scalar.

Deduplication merges duplicate candidate evidence for one semantic preimage;
it never merges distinct preimages merely because coordinates are close.
Continuation tolerance and a bounded semantic-chart tube may support G8
continuation or R4 continuity diagnostics. They do not participate in the R4
current selector and cannot establish, change or transfer durable identity.
R4 token resolution requires one unique current semantic selector and exact
ledger validation of its persisted binding. Parameter equality across revisions
is never required for ordinary regular root motion.

For a complete ranked periodic group, the R4 topology certificate is adaptive
to its intrinsic phase configuration. Its bound comes from the prior/current
cyclic root gaps and disjoint isolating intervals, not from a fixed fraction of
the component domain or a solver work ceiling. In particular, `span / 256` and
`maximumIsolationSubdivisions` are not semantic transition evidence. Tube
intersection, an absent/nonunique same-rank mapping or otherwise insufficient
evidence quarantines the affected complete group durably. A later unique
offset-zero mapping releases it; a proved unique nonzero cyclic shift retires
it permanently. These outcomes are current topology-certificate results, not
changes to the durable selector.

For a repeated base selector, canonical semantic parameters and disjoint
isolating intervals may establish only the intrinsic oriented phase/rank
defined in §6.2. The rank and collision cardinality enter the complete selector;
the parameter and interval values themselves remain revision evidence. This
bounded use of semantic order does not authorize solver enumeration order or a
coordinate/proximity fallback.

### 8.3 Tangency quantity

The tangency threshold applies only to a documented normalized contact
indicator. For a regular source and a model-distance residual, the preferred
first-order indicator is the absolute derivative with respect to source arc
length, equivalently the target-normal/source-tangent directional factor. It
is invariant to ordinary monotone parameter scaling. Raw derivatives of
differently scaled equations or unnormalized semantic parameters are not
comparable. Singular sources or capabilities without the required normalized
indicator must use analytic/certified evidence or report contact/multiplicity
as not established.

The signed orientation used by the R4 germ is normalized separately from the
indicator magnitude: line/segment, central conic/parabola and regular
polynomial implicit adapters use the canonical representation rules in §6.2.
Multiplying one of those equivalent equations by a legitimate nonzero scalar
must preserve the selector. Ray direction remains intentional semantic input.

### 8.4 Initial versioned values

The author approves the measured values below as initial G8B defaults only
where the quantity has the normalized meaning characterized above:

| Quantity | Initial value | Meaning |
|---|---:|---|
| root parameter | `1e-12` | provider-declared semantic parameter units |
| absolute residual | `2e-12` | compatible normalized residual units |
| relative residual | `2e-12` | dimensionless multiplier of a documented geometric scale |
| tangency threshold | `1e-10` | compatible normalized contact-indicator units |
| semantic deduplication | `4e-12` | provider-declared semantic parameter units |
| semantic continuation | `1e-8` | revision-scoped prediction evidence in provider units |
| coordinate verification | `4e-12` | model-coordinate consistency only |

If an adapter/provider normalization changes the numerical interpretation,
G8B uses and validates the corresponding normalized equivalent rather than
copying the raw value. Coordinate verification is never identity evidence.

## 9. Deterministic work and state

The author provisionally approves these initial G8B deterministic ceilings,
whose provenance is the G8A evidence:

| Work dimension | Initial ceiling |
|---|---:|
| semantic evaluations | `32768` |
| semantic derivative evaluations | `16384` |
| target evaluations | `32768` |
| candidate intervals/boxes | `8192` |
| isolation subdivisions | `8192` |
| isolation depth | `40` |
| refinement iterations per candidate | `80` |
| residual verifications | `1024` |
| candidates | `512` |
| continuation comparisons | `4096` |
| published finite solutions | `256` |
| retained intersection-index entries | `0` |
| retained topology epochs | `2` |

Every query therefore has independent finite maxima for:

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
complete set. These are implementation-policy defaults, not universal
mathematical constants. Wall-clock time is diagnostic only unless a future
author-approved runner-specific policy says otherwise.

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
- G8B includes one internal derived point consumer selected by a semantic root
  token. It owns no solving or identity authority, follows normal DAG
  dependencies, becomes coherently undefined for absent, stale or ambiguous
  continuation, never retargets to another root, and may recover only when the
  same token is current again under the approved continuation contract.
- The solution-local admissibility predicate requires a successful current
  `FINITE` result, a unique matching token, supported target/evaluator
  authority, coherent source/revision/branch/component bindings, independent
  residual and target-membership verification, `ESTABLISHED` local isolation,
  an explicit semantic continuation key, and identity status
  `NEW_TOPOLOGICAL_SOLUTION`, `DETERMINISTIC_SELECTION_ESTABLISHED` or
  `CONTINUATION_ESTABLISHED`. Global completeness is not a veto. Unresolved
  candidates are component-scoped: the solution is blocked by unresolved
  evidence on its own resolved valid component, while evidence on another
  component remains global/work information and does not veto that
  independently established local root.
- Root existence, deterministic identity, local numerical/topological evidence,
  global completeness and policy eligibility are distinct result axes. An
  exact opaque token means exact semantic identity matching; it does not claim
  exact arithmetic or a certified numerical error bound. Current public
  materialization may therefore consume a `VERIFIED_UNCERTIFIED` /
  `ESTIMATED_ERROR` root when all existing local-isolation and identity
  requirements are satisfied.
- A repeated base selector may satisfy the identity requirement only through
  the complete ADR-0017 selector: component, germ, orientation, domain kind,
  verified collision cardinality and intrinsic phase/rank. Cardinality,
  topology, orientation or periodic-seam ambiguity invalidates rather than
  retargets ranked tokens.
- A materialized point claims its exact selector/token allocation. When that
  allocation is not current but has not crossed an intrinsically
  non-reactivatable barrier, the ledger may retain it as claimed dormant. The
  same `GeoPoint` is then undefined and may reactivate only when the same
  complete selector again resolves uniquely with current point admissibility.
  Reactivation does not create a new point, retarget the existing point or
  search by coordinate/order/proximity.
- Native `.cedg` lifecycle coverage for R4 reconstructs the same existing point
  through dormant `2 -> 4 -> 2` recurrence and reopen after reactivation. This
  is separate from the ledger-level periodic-quarantine round trip and does not
  itself claim a native `q`/`r` quarantine fixture.
- For a complete ranked periodic group, unavailable or nonunique cyclic evidence
  retains the complete prior selector/token group as non-current periodic
  quarantine. Quarantine survives ledger recompute, canonical export/import and
  exact-provenance copy while a materialized claim needs it. A later unique cyclic offset zero releases and
  may reactivate the same point; a proved unique nonzero offset permanently
  retires the old group; absent/nonunique evidence remains quarantined. Copy
  preserves quarantine but cannot certify its release, and releasing the final
  claim prunes the retained group.
- Absence, non-current evidence, unsupported or unverified candidates,
  residual/membership rejection, overlap-kind results, ambiguous or
  discontinuous identity, computation failure and work-limit failure make the
  selected point undefined. The consumer never searches coordinates or
  retargets by result order.
- `Intersect(L,T)` continues to create only the rich result. R4 performs no
  automatic point creation. Future G9U1 marker, create-one/create-all and
  opted-in auto-materialization workflows may consume only current admissible
  tokens and remain frontend transactions; dormant tokens and later recompute
  never trigger point creation.
- The incremental current selector map and existing-point reactivation require
  `O(R log R + P)` additional work for `R` current roots and `P` materialized
  token bindings, with no extra global solve per child and no movement-history
  replay. Existing whole-transition diagnostics may remain `O(R^2)` in the
  worst case; they remain non-authoritative.

## 11. Cache/index contract

The author-approved starting point is query-local isolation state plus the
existing bounded semantic evaluation session. No G7 metric cache is reused.

G8B has no shared intersection owner or index. Any later proposal requires new
measured evidence and separate architectural approval, and must:

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

The following bullets record the original G8 compatibility boundary. Later
author-approved G9U0 product exposure and the bounded G9U0-R4 token-ledger
refinement supersede only the explicitly named public/persistence portions;
the mathematical and Classic-compatibility boundaries remain unchanged:

- legacy `GeoLocus` and Classic intersections are unchanged;
- `GeoLocusV2` remains experimental/internal and disabled by default;
- no existing `.ggb` is migrated or changes meaning;
- no public command, dispatcher route, `Path`, or point-on-locus API is added;
- G8 itself added no XML, `GeoFactory`, serialization, or migration; G9U0-R4
  changes only the public token-ledger attribute to canonical format v4,
  imports canonical v3 and compatible v1/v2 states under the strict rules in
  §6.2, and leaves the external token envelope unchanged, without a new
  archive/geometric-object format or `GeoFactory` path;
- no 3D/G9, G5 export, Python DSL, or frontend behavior is added; and
- G8 internal result support does not imply public incidence support for every
  GeoGebra curve.

## 13. Author-approved G8B conformance profile

The [G8A report](../../../docs/validation/g8a_locus_v2_intersection_characterization_report.md)
and [traceability matrix](../../../docs/validation/g8a_locus_v2_intersection_traceability_matrix.md)
provide the measured evidence incorporated by the author into this contract:

- minimum family: line, segment, ray and circle;
- full conics, functions, implicit curves and locus–locus deferred;
- analytic/certified capabilities first; evaluator-only and broad-phase methods
  may find candidates but cannot claim completeness without separate coverage
  proof;
- query-local state, zero retained intersection-index entries, and no G7 metric
  state;
- durable identity fields separated from revision-scoped parameter, interval,
  component, residual and certificate evidence;
- no universal merge/split descendant inheritance; and
- a rich Geo as authority plus the required internal token-selected derived
  point consumer.

The G8A measured tolerance source is `g8a-measured-candidate/v1`; the approved
implementation policy is `g8b-initial-normalized/v1` with the normalization
contract in Section 8:

```text
root parameter = 1e-12
absolute normalized residual = 2e-12
relative normalized residual = 2e-12
tangency threshold = 1e-10
semantic deduplication = 4e-12
semantic continuation = 1e-8
coordinate verification = 4e-12
```

Initial deterministic ceilings are 32768 semantic evaluations, 16384
semantic derivative evaluations, 32768 target evaluations, 8192 candidate
intervals, 8192 subdivisions, depth 40, 80 refinement iterations per
candidate, 1024 residual verifications, 512 candidates, 4096 continuation
comparisons, 256 published finite solutions, zero retained intersection-index
entries and two retained topology epochs. They are provisional versioned G8B
implementation defaults, not mathematical constants.

## 14. Author-approved phase disposition

The author accepts the rich-result/rich-Geo authority, required internal
token-selected point consumer, independent completeness axis, typed overlap,
capability hierarchy, normalized tolerance policy, provisional deterministic
budgets, narrow semantic continuation contract, rejection of universal
merge/split genealogy, query-local state and the line/segment/ray/circle
minimum. Those extended families remain outside G8B. G8C1 author-approves
nondegenerate ellipse/parabola/hyperbola, explicitly bounded real functions and
regular finite-coefficient polynomial implicit targets. G8C2 author-approves
Locus V2 × Locus V2 over finite components and approved periodic fundamental
domains. Public command, generic `Path`, XML/persistence, legacy/Classic, 3D,
G9 implementation and Python boundaries remain closed.

The canonical G8B prompt has been executed and the focused author-directed R1
refinement has been applied. Its internal implementation and evidence package
are `PASS — AUTHOR APPROVED`. The implementation conforms by
keeping evaluator-only discovery at `NOT_ESTABLISHED`; complete finite/empty
claims require an authoritative analytic/certified capability
whose declared component coverage and candidates are independently verified.
Individually verified roots additionally require established local isolation
and unambiguous semantic identity before point consumption. G8C design is
`PASS — AUTHOR APPROVED`. The extension specification makes both the G8C1
one-parameter subset and the G8C2 Locus V2 × Locus V2 contract normative. G8C1
records an author-approved internal kernel. The G8C2 execution under Accepted
ADR 0009 is also `PASS — AUTHOR APPROVED`; together the approved subphases close
G8 globally. No public or other deferred boundary is opened by this closeout.

```text
G8 SPEC = NORMATIVE / AUTHOR-APPROVED R1 REFINEMENT APPLIED
G8A = PASS — AUTHOR APPROVED
ADR 0008 = ACCEPTED — R1 CLARIFICATION APPLIED
G8B-R1 = PASS — AUTHOR APPROVED
G8B = PASS — AUTHOR APPROVED
G8 PRODUCTIVE IMPLEMENTATION = INTERNAL MINIMUM KERNEL — AUTHOR APPROVED
G8C DESIGN = PASS — AUTHOR APPROVED
G8C1 = PASS — AUTHOR APPROVED
G8C2 CONTRACT = NORMATIVE — AUTHOR APPROVED
ADR 0009 = ACCEPTED
G8C2 = PASS — AUTHOR APPROVED
G8 = PASS — AUTHOR APPROVED
G9 DESIGN = AUTHORIZED — NOT STARTED
G9 IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

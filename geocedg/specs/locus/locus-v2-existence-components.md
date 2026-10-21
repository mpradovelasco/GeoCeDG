# Locus V2 existence and continuous-valid components

| Field | Value |
|---|---|
| Status | **NORMATIVE — AUTHOR APPROVED** (v1.0); **PRE-G9B-CU2 v1.1 AMENDMENT (§15) — ARCHITECTURE AUTHOR APPROVED (2026-10-21); THE INCORPORATED TEXT BELONGS TO THE CU2 TECHNICAL CANDIDATE, PENDING AUTHOR REVIEW** |
| Version | `1.1` |
| Phase | `PRE-G9B-S1-R2`; amended by `PRE-G9B-CU2` (§15) |
| Scope | Shared-kernel Locus V2 existence, continuity and completeness evidence |
| Parent | [Locus V2 semantic contract](locus-v2-semantics.md) |
| Decision | [Accepted ADR 0027](../../../docs/adr/0027-locus-v2-existence-and-continuous-valid-components.md); [ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), accepted as architecture on 2026-10-21 |
| Product implementation | v1.0: **I1--I3 PASS — AUTHOR APPROVED**; v1.1: `CU2` technical candidate, pending author review |

## 1. Purpose

This specification specializes the approved G6 statement

```text
declared driver domain != necessarily valid locus domain
```

for dependent constructions whose current evaluator can lose validity inside a
nominal provider interval. It does not change legacy `Locus` or add a render-
derived domain.

For one locus identity `l`, semantic revision `r`, and stable branch key `b`,
the kernel shall distinguish:

```text
D_canonical(l,r,b)
D_exist(l,r,b)
C_continuous(l,r,b)
completeness(l,r,b)
```

The canonical domain is provider authority. The other three values are current
semantic evidence about the complete dependent construction.

## 2. Canonical driver domain

`D_canonical` is the explicit oriented parameter interval declared by the
versioned generator/provider. It defines which parameter states are meaningful
for the driver and how periodic canonicalization is performed. It does not
assert that every dependency needed to evaluate the locus exists throughout the
interval.

A periodic canonical interval such as `[-pi,pi)` remains half-open. Periodicity
may canonicalize `pi` to `-pi`; it does not fill an internal interval on which a
dependent construction is undefined.

## 3. Existence coverage

The existence domain is the subset of `D_canonical` on which the complete
dependent locus construction is semantically evaluable. The current certificate
shall represent three disjoint forms of coverage:

1. `CERTIFIED_EXISTS`: interval-wide existence has been established;
2. `CERTIFIED_DOES_NOT_EXIST`: interval-wide nonexistence/undefinedness has been
   established with a typed semantic reason; and
3. `UNRESOLVED`: neither assertion has been established.

The union of all `CERTIFIED_EXISTS` intervals is the currently certified
`D_exist`. It may be empty, one interval, or a finite union of intervals.
Open, closed and half-open endpoints are explicit. The interval set is ordered
only by canonical oriented parameter order for deterministic traversal and
serialization; order is not component identity.

`DEPENDENCY_UNDEFINED`, structurally excluded root, and provider-outside-domain
are possible typed nonexistence reasons. `EVALUATION_FAILED`, work exhaustion,
unsupported nondeterminism, or insufficient proof normally yield `UNRESOLVED`,
not certified nonexistence.

## 4. Certified continuous-valid component

A certified continuous-valid component is stronger than a pointwise existence
interval. It contains:

```text
branchKey
oriented interval with explicit endpoint policy
source locus identity and semantic revision
provider/certifier contract version
interval-wide existence evidence
same-construction / selected-root continuation evidence
left and right boundary evidence
maximality = ESTABLISHED | LOCAL_CERTIFIED_SUBINTERVAL
optional established lineage relation
numeric guarantee and deterministic work evidence
```

For an interval `I`, certification means:

- `I` is contained in `D_canonical`;
- every parameter in its certified interior has a valid semantic evaluation;
- the same constructive branch and, where applicable, the same exact selected-
  root selector remain continuously identifiable through `I`;
- no certified invalid or unresolved internal gap is crossed;
- endpoint inclusion follows the semantic predicate, not an epsilon convenience;
- the evidence is deterministic and bound to the exact source/dependency
  revisions; and
- no coordinate, visual sample, viewport or query order establishes identity.

An interval isolated only as a conservative valid interior may be published as
`LOCAL_CERTIFIED_SUBINTERVAL`. It must not be called a maximal connected
component, and its unresolved boundary bands remain explicit. A maximal
component requires authoritative boundary and adjacent-coverage evidence.

## 5. Completeness

The branch certificate exposes exactly these initial global states:

- `COMPLETE`: the certified existence, certified nonexistence and boundary
  evidence cover the complete canonical domain, and every continuous-valid
  component is maximal under that evidence;
- `NOT_ESTABLISHED`: at least one canonical-domain region, boundary, finiteness
  claim, or continuation claim remains unresolved.

`NOT_ESTABLISHED` is not the same as `EMPTY_DOMAIN`, `UNDEFINED`, or
`INCOMPLETE` geometry. It preserves useful local certificates without claiming
that all locus components are known.

A complete finite decomposition is an ordered finite set of nonoverlapping,
oriented maximal components. The kernel must return `NOT_ESTABLISHED` when it
cannot prove finiteness or coverage; it must not approximate a finite set by
stopping a sampler.

## 6. Root-dependent interval evidence

When a dependent point is selected from `LocusIntersectionResult`, the exact
root token and its source-pair/selector evidence remain the authority for which
root is requested. They do not by themselves prove interval existence.

An interval-level root certificate must establish, for a selected root `q`:

```text
selected semantic root q
is valid, unique under its approved selector, and continuously identifiable
through driver interval I
for source/dependency semantic revision vector R
```

The certificate shall reuse:

- exact root token ownership and materialized selection;
- source-pair identity and provider/target contract signatures;
- stable branch/component lineage where already established;
- typed local-isolation, multiplicity and ambiguity evidence; and
- explicit continuation relations.

It shall add interval-wide existence/uniqueness and boundary coverage. It shall
not allocate a token, select another root, compare Cartesian coordinates, or
infer continuation from solver/list order. The current root germ and numerical
isolating interval remain revision evidence and are not promoted alone to an
interval certificate.

Temporary loss of the selected root leaves the durable selector/token and locus
identity intact. A later occurrence belongs to the same continuous component
lineage only when interval evidence spans the event; otherwise it is a distinct
current component or has no established cross-gap lineage.

## 7. Boundary authority

Boundary certification shall use the first applicable authority:

1. provider/dependency structural or event boundary;
2. certified selected-root existence/continuation interval;
3. analytic or exact boundary;
4. deterministic interval method with a certified semantic predicate; or
5. bounded numerical isolation driven by that certified predicate.

Point evaluations may seed or falsify a candidate interval, but `VALID` samples
do not prove the interval between them. A failed sample does not locate an exact
boundary. Render tessellation and screen geometry are prohibited.

A numerical boundary result records its enclosure, method, guarantee and work.
If the exact maximal boundary cannot be certified, the kernel may publish only a
safe interior subinterval and must leave the boundary enclosure unresolved.
It may not round the enclosure outward into an invalid region or inward while
claiming maximal/global completeness.

Work exhaustion, nonfinite arithmetic and an uncertifiable predicate produce
`NOT_ESTABLISHED`; they never fabricate a component.

## 8. Identity, lineage and lifecycle

The persistent `GeoLocusV2` identity and stable branch key retain their current
meaning. A current continuous-valid component initially has only a revision-
local semantic address:

```text
(locus persistent identity, semantic revision, branch key,
 certificate contract/version, component evidence key)
```

Component ordinal, interval ordering and numeric boundary equality are not
durable identity. `LocusComponentLineage2D` remains valid for existing
provider-declared structural components. A dynamically certified component may
receive cross-revision lineage only from an explicit provider/certifier
transition that is unique and reconstructible.

Changes in existence coverage or its certificate are semantic-content changes
and advance the locus semantic revision under the existing lifecycle. Point
queries do not advance it. A certificate and every consumer result are current
only for the exact locus/dependency revision vector and policy version.

Save/reopen, undo/redo, copy/remap and compatible redefine reconstruct the
certificate from durable inputs and versioned policies. Runtime partitions and
numeric caches are not persisted as authority. Old files without R2 evidence
load with no inferred component lineage; a producer may re-establish a truthful
certificate from its existing reconstructible inputs. Otherwise it publishes
`NOT_ESTABLISHED`.

## 9. Producer and certifier contract

The selected architecture is hybrid:

```text
generator/provider
  -> canonical domain + orientation + periodicity
  -> structural boundaries and provider-owned interval facts
  -> dependency/root interval capabilities
shared LocusContinuousDomainCertifier2D
  -> immutable LocusExistenceStructure2D for one semantic revision
```

The candidate implements these roles with `LocusExistenceStructure2D` and the
bounded selected-root interval capability. Direct analytic/static
producers may provide a complete certificate without numerical discovery.
Dependent producers delegate the common coverage assembly and validation to the
shared certifier. A producer unable to supply interval proof remains usable for
point evaluation but exposes `NOT_ESTABLISHED` component completeness.

`SemanticGeneratorDescriptor1D` continues to describe the generator and
canonical domain. Periodicity validation must cease requiring existence
components to equal the whole canonical interval. Reconstructible dependency
IDs and revision signatures remain inputs to currentness; no hidden graph is
introduced.

v1.1 note: no class named `LocusContinuousDomainCertifier2D` was implemented.
The shared certifier role is realised by the hybrid certifier of §15.7.

## 10. Migration of `getValidDomainComponents()`

The existing accessor is overloaded: producers often use it as nominal domain,
while consumers treat it as complete continuous-valid coverage. R2 adds
explicit accessors for the new existence structure and continuous components.

Migration order:

1. introduce certificate values and lift producers whose interval-wide validity
   is already established to `COMPLETE`;
2. expose dependent producers as `NOT_ESTABLISHED` until interval proof exists;
3. migrate every consumer to an explicit completeness/admissibility policy;
4. retain `getValidDomainComponents()` as a compatibility view only when the
   complete continuous-valid set is established; and
5. deprecate or narrow the old accessor only after no consumer can interpret
   local/unknown coverage as complete.

No implementation may silently return locally certified intervals through the
legacy accessor to a global consumer.

v1.1 note: §15.7 retires the compatibility certificate and §15.8 replaces the
legacy accessor as a topology authority for every consumer.

## 11. Consumer admissibility

| Consumer | Local certified component with global `NOT_ESTABLISHED` | Complete claim |
|---|---|---|
| Rendering | Allowed; render only certified intervals and show no completeness claim | Not required |
| Component-local metric | Allowed for an explicit component/address | Result remains component-local |
| Total locus metric | Not sufficient | Requires `COMPLETE` |
| Intersection | Locally verified roots allowed; no empty/exhaustive claim | Empty/all-roots requires complete domain coverage plus solver completeness |
| Exact point/path address | Allowed if current address lies in one certified component and lineage/currentness hold | Global nearest/route selection requires sufficient complete coverage |
| Nested LocusV2 | May consume local intervals only while propagating incompleteness | Cannot promote to complete downstream domain |
| Analysis | Allowed when completeness metadata is retained | Consumer-specific |
| DXF | Allowed only as explicitly incomplete semantic fidelity with mandatory sidecar | Complete-locus wording requires `COMPLETE` |

v1.1 note: §15.8 refines this table for arcs, one-sided ends and point components.

## 12. DXF read-only contract

G9X1 consumes each finite certified
continuous interval independently:

```text
Locus V2 source
  -> branch
  -> certified continuous-valid component
  -> export-only adaptive approximation
  -> one neutral component
  -> one LWPOLYLINE
  -> fidelity evidence
```

No entity crosses a certified-invalid or unresolved interval. Periodic closure
is allowed only for one component certified as a complete periodic component,
not merely because the provider is periodic.

The sidecar records source persistent identity, branch, revision-local component
address and established lineage if any, certified interval and endpoint policy,
source semantic revision, approximation evidence, and global domain-completeness
state. `NOT_ESTABLISHED` must be visible in preflight and the sidecar. It must
not be represented as complete output.

## 13. Author witness `m`

For `m = LocusV2(U,R)`, `[-pi,pi)` remains the correct canonical periodic
driver domain. The currently published component is not a certified existence
component because the bound selected point `S`, and therefore `U`, is undefined
through an internal negative interval. Existing evaluations establish counter-
examples to whole-domain validity but not the final boundaries or number of
components.

The candidate supplies an interval-level certificate for the exact selected
root token for `S` on conservative subintervals as `R` traverses its driver
domain. It uses structural SplineV2 spans plus outward interval predicates;
transition cells remain unresolved. Thus useful local components coexist with
global `NOT_ESTABLISHED`, and no boundary is hard-coded from samples or drawing.

## 14. Non-goals and stop conditions

R2 does not authorize or define:

- root selection by coordinate proximity, enumeration order or plotting;
- a general symbolic solver for arbitrary dependent constructions;
- dense sampling as proof;
- persistence of runtime samples as semantic authority;
- DXF native `SPLINE`, DXF `TEXT`, partial-output policy, S2–P1 or G9B+;
- a second dependency graph; or
- automatic durable identities for dynamic components.

Implementation must stop for a new author decision if the witness requires a
general redesign of root-token identity, if no interval predicate can certify
its selected-root family, or if local-incomplete consumption cannot preserve
the explicit completeness axis.

## 15. PRE-G9B-CU2 amendment (v1.1): hybrid certification

Normative text of amendment A2.1-R and of the consumer contract of A2.5 of the
[`CU2` normative proposal](../../../docs/architecture/pre_g9b_cu2_normative_proposal.md),
approved as architecture by the author on 2026-10-21 together with
[ADR 0038](../../../docs/adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md)
([approval record](../../../docs/validation/pre_g9b_cu2_author_architecture_approval_record.md)).
It extends sections 2 to 5, 7, 8, 11 and 12 and keeps ADR 0027 unchanged, including the
independence of local admissibility from global completeness. The text belongs to the `CU2`
technical candidate until the author reviews that candidate. The construction class, the
transitions and the exact algebraic certificate are in
[`locus-v2-certified-construction-model.md`](locus-v2-certified-construction-model.md) §10;
publication and the exact fingerprint are in [`locus-v2-semantics.md`](locus-v2-semantics.md).

### 15.1 Evidence levels and nature of evidence

Locus V2 existence evidence has four levels:

1. `POINT_VALID`: one valid evaluation at one parameter. It implies nothing about any
   interval.
2. `EXPLORATORY`: sampled valid runs, validity transitions bracketed by bisection and
   suspected jumps, with method, policy version and work recorded. It is a separate typed
   structure, derived per revision and policy, not persisted and not part of the semantic
   snapshot. It is never promoted to a component, a completeness claim or an identity, it
   has no authority over the dependency graph, and it is never exported.
3. `LOCAL_CERTIFIED`: a certified component (§15.2), maximal or a local subinterval with
   explicit unresolved bands.
4. `COMPLETE`: a certified cover of the whole canonical domain by certified components,
   certified nonexistence and certified boundaries, with finitely many components. The union
   of the certified sets is the whole domain; completeness is never inferred from explored
   boxes being valid or from not having found other components.

A finite exploration never establishes level 3 or 4.

Each certified fact records the nature of its proof: exact symbolic or algebraic proof,
rigorous interval arithmetic, numerical verification under stated hypotheses, exploratory
evidence, or none. Only the first two certify. A numerical verification under hypotheses is
an estimate under its own contract, exploratory evidence has no authority, and the absence of
evidence yields `UNRESOLVED` or `NOT_ESTABLISHED`.

### 15.2 Certified component

A parameter set `I` is a certified component of a branch when every validity predicate of
its construction is certified true on `I`, every captured selection is the same rule on `I`,
the traced map is continuous on `I`, and each end of `I` is a declared domain end, a certified
transition, a structural event or the edge of an unresolved band. A structural discontinuity
with valid evaluations on both sides is a boundary, never an interior point. A certified
singular parameter, an even-multiplicity zero of a validity predicate that keeps its sign, is
interior to the component. End inclusion follows the semantics of the boundary predicate, not
pointwise evaluation. A certified component is an interval or arc of positive length, or a
point component (§15.4).

A component is never declared continuous, nor its locus `COMPLETE`, only because the
geometric equation has a double root when the indexed output that defines the traced point is
undefined at that parameter (`locus-v2-certified-construction-model.md` §10.3).

### 15.3 Arcs on periodic domains and one-sided ends

On a periodic canonical domain a component is an arc given by its canonical start, its length
in `(0, period]` and the inclusion of each end. An arc of full length whose traced map is
certified continuous at its closing point is a closed periodic component; any other arc,
including a full-length arc with a structural discontinuity at its closing point, is open.
Each arc carries its arc coordinate `s` in `[0, length]` from its start. A canonical parameter
`t` belongs to the arc when an included coordinate `s = t - start (mod period)` exists; on a
full-length arc with one included end this coordinate is unique. Component order is canonical
start order and is never identity.

The two ends of a full-length open arc share a canonical parameter and are distinct ends: each
has its side, its inclusion, its one-sided limit and its boundary source. The excluded end is
never the image of a canonical parameter; it is reached only through an explicit one-sided
limit query. Canonicalization never identifies the two ends, and continuity between them is
never inferred from coinciding coordinates or from a periodic driver.

Closure is decided only by the certificate. The provider's periodicity never closes a
component, and the single closure authority of every consumer is the certificate's own
closed-periodic flag (§15.8).

### 15.4 Point components

When a certificate proves that existence reduces to an isolated parameter `t*`, for example an
even-multiplicity zero of a validity predicate that is nonpositive on both sides, the set
`{t*}` is a point component: a kind of component of its own, never an interval or arc of
positive length. It carries a certified enclosure of `t*`, which in general is not a binary64
value, and the structural source of the isolated root (operation, predicate and root). Its
inclusion is closed. Its continuity is trivial and asserts no continuation and no direction.
It is maximal when certified nonexistence bounds it on both sides, and local otherwise; a cover
by point components, other components and certified nonexistence is `COMPLETE`. Its lineage is
its branch key and its structural source; appearance, disappearance and change of dimension
(point to arc, when the even root splits into two simple roots that bound an arc, and arc to
point) are typed lineage transitions. Its length is zero and it contributes zero to every
total; a route exists only within it and measures zero. Membership and selection use its
structural address, never equality with a binary64 parameter, and its existence never depends
on a binary64 parameter that represents its address exactly. Its position is a certified
enclosure of the limit form where one exists; otherwise it exists with its position
unavailable. It is never represented as a segment or a curve, and a consumer that does not
support point components declares the limitation and fails explicitly. A point component is
never confused with a certified singular parameter, which is interior to a component of
positive length. It creates no public point object and materializes no intersection. In this
version point components are declared in results and in the inspector, with their evidence
and lineage, and are not drawn.

### 15.5 Existence, evaluation and representation

Coverage `CERTIFIED_DOES_NOT_EXIST` requires a validity predicate certified false, or a
dependency whose own certificate establishes nonexistence. A dependency that is undefined
because its computation failed, for example a metric of class `NOT_ESTABLISHED` or an estimate
above its tolerance, yields `UNRESOLVED` coverage when the failure decides existence, and only
an `EVALUATION_FAILED` point status with a computational cause when existence is already
certified. Mathematical existence and certified topology (stated by the certificate),
numerical availability (the evaluator) and representation availability (rendering and export)
are distinct facts, and none is inferred from another: a valid evaluation never proves
existence, a failed one never proves nonexistence, and certified existence is never a
numerical guarantee of coordinates. Where the certificate establishes nonexistence, a point
returned by the evaluator is not published as valid.

A certified cover that is empty is `COMPLETE` with no component; the definition remains valid
(`locus-v2-semantics.md`, section "PRE-G9B-CU2 amendment").

### 15.6 Identities, lineage and selectors

The durable identity of a construction result, the semantic identity of its selection and the
lineage of a component are distinct. The semantic identity of a selection is the rule that maps
the inputs to the selected result: the algorithm and its version, the raw root order and the
algorithm's selection state; it is an input of the revision. The lineage of a component is its
branch key together with the ordered sources of its boundaries:
`(durable operation identity, predicate kind, side)` for a certified transition,
`(event source, side)` for a structural event, or the declared domain end. Moving a boundary
without changing its source keeps the lineage. Appearance, disappearance, split and merge of
components are typed lineage transitions. No revision, recompute or temporary change of
existence rebuilds an association by label, index, coordinates or proximity. A persisted
component selector resolves only through a demonstrated structural correspondence: equality of
the decomposition, or a recorded lineage transition from the cited component under the same
selection rule. Unique containment of its parameter validates that correspondence and never
creates one. Without it the selector is unresolved and its point undefined with a typed cause.
Resolution never rewrites the persisted selector.

This replaces, for certified components, the revision-local address of §8: component ordinal,
interval ordering and boundary bits remain non-identity.

### 15.7 Producer and certifier

Dependent producers publish the certificate of the hybrid certifier, contract
`locus-existence/hybrid/v1`, computed once per published revision and shared by every consumer.
A locus that cannot be certified is `NOT_ESTABLISHED`, with or without local components; it
never falls back to a compatibility certificate. Static and analytic producers publish an
explicit provider certificate only when their own contract proves existence and continuity on
the whole declared domain, and the certificate cites that contract. The compatibility
certificate `locus-existence/provider-compatibility/v1` is retired.

### 15.8 Consumer admissibility

Every consumer reads the existence structure. Local consumers accept a certified component
containing their parameters; global consumers require `COMPLETE`. Operations that need
continuity or a route require a certified component, even a local one; pointwise validity is
admitted only for operations that need an isolated point. Periodic semantics (full cycle, wrap
to start, seam quarantine, lift and seam side) apply only to a certified closed periodic
component; the ends of an open arc are addressed through their one-sided limits. The
compatibility view of valid domain components never decides topology.

A point component contributes zero to totals and to routes. A consumer that addresses a
component through a binary64 parameter (semantic points, interaction, metric positions,
principal branch) cannot select it and fails explicitly with a typed cause. An intersection
never reports `EMPTY` or a complete set while it has not excluded a point component of its
input rigorously, and a dependent locus whose support has a point component declares the
limitation and is not `COMPLETE`. No consumer treats a point component as an arc.

DXF and LaTeX exports follow the topology of the certificate:
[`dxf-curve-fidelity-and-approximation.md`](../export/dxf-curve-fidelity-and-approximation.md)
section "PRE-G9B-CU2 amendment" and
[`latex-semantic-curve-export.md`](../export/latex-semantic-curve-export.md).

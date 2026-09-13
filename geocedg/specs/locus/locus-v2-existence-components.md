# Locus V2 existence and continuous-valid components

| Field | Value |
|---|---|
| Status | **NORMATIVE — AUTHOR APPROVED** |
| Version | `1.0` |
| Phase | `PRE-G9B-S1-R2` |
| Scope | Shared-kernel Locus V2 existence, continuity and completeness evidence |
| Parent | [Locus V2 semantic contract](locus-v2-semantics.md) |
| Decision | [Accepted ADR 0027](../../../docs/adr/0027-locus-v2-existence-and-continuous-valid-components.md) |
| Product implementation | **I1--I3 PASS — AUTHOR APPROVED** |

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

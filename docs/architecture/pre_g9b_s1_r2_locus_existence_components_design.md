# PRE-G9B-S1-R2 — Locus V2 existence/component certification design

| Field | Value |
|---|---|
| Status | **DESIGN CANDIDATE — PENDING AUTHOR REVIEW** |
| Entry | `e5260fc7dd4924f0599ac3ac2700025bf5e48330` (`PRE-G9B-S1-R1` partial candidate) |
| Parent characterization | `48312ceb070b014b68218b3ca5f5c09249c62748` |
| Product implementation | **NOT AUTHORIZED / NOT STARTED** |
| R1 population work | **PRESERVED — PARTIAL IMPLEMENTATION CANDIDATE** |
| Parent S1 | **BLOCKED — SEMANTIC DESIGN IN PROGRESS** |
| Self approval | `false` |

This design responds to the author-approved semantic direction that a canonical
driver domain and a Locus V2 existence domain are different. It does not modify
the R1 implementation of `geocedg-dxf-geometric-2d/v1` and does not begin S2,
S3, S4, D1, P1, G9B, G9C or G9U2.

The normative candidate is
[Locus V2 existence and continuous-valid components](../../geocedg/specs/locus/locus-v2-existence-components.md),
and the selected architectural alternative is recorded in
[proposed ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md).

## 1. Repository characterization

### 1.1 Present branch/domain contract

`LocusBranch2D` currently stores one `declaredDriverDomain` and one list called
`validDomainComponents`. The list:

- gates every call to `LocusDefinition2D.evaluate` before the evaluator runs;
- participates in the branch semantic signature, equality and revision change;
- is used to create `LocusComponentLineage2D` keys from exact interval bits;
- has no completeness state; and
- is consumed as if it were the complete usable domain by rendering, metrics,
  intersections, public point operations, nested LocusV2 and G9X1 export.

This is truthful for direct producers that know their function exists on the
declared interval. It is not sufficient for a reconstructible dependent
construction.

The direct architectural answers are therefore:

```text
CURRENT_VALID_COMPONENT_INTENT = interval-wide semantic evaluation is valid
CURRENT_DEPENDENT_PRODUCER_BEHAVIOR = nominal/provider domain only
CONTINUITY_SEGMENTATION_ELSEWHERE = none
m_CONSISTENCY = violates the intended valid-component guarantee
```

`AlgoDependentPointLocusV2` currently obtains a domain from
`LocusV2DomainDescriptor`, fixed segment/circle/arc support, or an upstream
LocusV2 branch. It publishes that list directly into `LocusBranch2D`.
`SemanticGeneratorDescriptor1D` repeats it and, for a periodic provider,
requires exactly one component equal to the complete half-open declared domain.
That validation conflates canonical periodic parameterization with dependent
construction existence.

`ReconstructibleLocusEvaluator2D` applies one canonical driver parameter to an
isolated copy of the relevant construction slice and reports a typed pointwise
result. It correctly reports `DEPENDENCY_UNDEFINED` if the dependent point is
undefined or nonfinite. It exposes no interval-wide existence proof.

### 1.2 Actual consumers and current risk

| Consumer | Current use of `validDomainComponents` | Risk if local/unknown intervals are returned silently |
|---|---|---|
| `LocusRenderCache2D` | tessellates every component and infers whole-period closure | draws across or closes unresolved domain |
| `LocusMetricEngine2D` and route/binding classes | aggregate components as the locus metric domain | reports a partial sum as total length or resolves an invalid route |
| intersection capabilities/contexts/solvers | enumerate components as the supported query domain | overstates root-set completeness or an empty result |
| `LocusPointInteractionResolver2D`, `AlgoSemanticLocusPoint2D` | searches/binds point addresses in components | treats partial search as a global semantic path |
| `AlgoDependentPointLocusV2` and similarity transforms | propagate upstream components | promotes incomplete support to complete downstream domain |
| `LocusPrincipalBranchSelector2D`, public operations and spline occurrence resolver | choose or enumerate semantic components | component ordinal/order can accidentally become authority |
| `G9X1GeometryExportAdapter` | approximates each component and detects full-period closure | emits a complete-locus claim or bridges an unresolved gap |

The migration must therefore be shared and consumer-explicit. Adding a second
list only in G9X1 is rejected.

### 1.3 Existing root evidence

The inspected root machinery establishes useful but narrower facts:

- `IntersectionRootIdentity2D` owns the durable token, source-pair identity,
  constructive lineage, stable selector context and identity status;
- `IntersectionRootAddressProof2D` captures provider/target contracts and one
  exact current semantic parameter;
- `IntersectionRootRevisionEvidence2D` captures one current parameter,
  component, isolating interval, local isolation, residual, numeric guarantee
  and optional root germ;
- `LocusIntersectionContinuation2D` compares previous and current construction
  revisions under an explicit continuation key and exact public address proof;
- `AlgoLocusIntersectionPointV2` consumes one exact token and becomes undefined
  rather than retargeting it; and
- existing intersection results separate local solution admissibility from
  global root-set completeness.

These contracts prevent coordinate/order retargeting. They do not prove that a
selected root exists and is uniquely continuable for every parameter in a
driver interval. The current root germ is explicitly revision evidence, not a
durable identity or interval certificate. R2 must extend evidence, not reinterpret
the current ledger.

## 2. Witness `m`

The versioned author fixture contains an exact selected-root dependency:

```text
rich LocusV2 intersection result
  -> exact token-selected point S
  -> U = Midpoint(S,R)
  -> m = LocusV2(U,R)
```

The provider and branch are `circle-point/v1` and `generator.main`. The canonical
domain `[-pi,pi)` is correct and periodic. The current producer also claims it
as one valid component. Direct evaluation establishes:

| Driver parameter | Current semantic result |
|---:|---|
| `-pi` | `VALID` |
| `pi` | canonicalized to `-pi`, then `VALID` |
| `0` | `VALID` |
| `pi/2` | `VALID` |
| `-pi/2` | `DEPENDENCY_UNDEFINED` |

A wider internal negative interval is also dependency-undefined. `S` is absent
there under its exact selector, so `U` is undefined. This demonstrates:

```text
canonical domain = [-pi,pi)                 TRUE
continuous existence on all [-pi,pi)        FALSE
final valid-component boundaries/count      NOT ESTABLISHED
```

The samples disprove the current whole-domain assertion but do not certify the
remaining intervals. No final numerical boundary belongs in this design.

## 3. Selected semantic model

The model is a revision-bound `LocusExistenceStructure2D` per branch, containing
four separate axes:

1. canonical driver domain and orientation;
2. certified existence/nonexistence/unresolved coverage;
3. certified continuous-valid components; and
4. global decomposition completeness.

The names are proposed implementation names, not new authority by themselves.
The exact contract is in the normative candidate.

### 3.1 Coverage partition

Coverage intervals carry a typed state and evidence. They are disjoint and
canonically ordered for deterministic traversal only. When completeness is
`COMPLETE`, their union covers the canonical domain with compatible endpoint
ownership. When completeness is `NOT_ESTABLISHED`, uncovered or explicitly
unresolved regions remain visible.

An existence interval proves the dependent construction is evaluable throughout
that interval. It does not by itself prove that the same selected root or
constructive sheet continues throughout it.

### 3.2 Continuous component certificate

`LocusContinuousValidComponent2D` binds an oriented interval to:

- locus persistent identity and exact semantic revision;
- stable branch key and provider/certifier versions;
- interval-wide existence proof;
- same-root/same-construction continuation proof where needed;
- endpoint and adjacent-region evidence;
- deterministic method, numeric guarantee and bounded work;
- maximality (`ESTABLISHED` or conservative local subinterval); and
- optional cross-revision lineage established independently of list position.

The component is revision-local by default. A stable `branchKey` remains the
durable branch identity. The present `LocusComponentLineage2D` continues to
identify provider-declared structural components; it cannot be applied
automatically to numerically isolated intervals because changing endpoint bits
would invent identity changes.

### 3.3 Completeness

Only `COMPLETE` and `NOT_ESTABLISHED` are needed initially. `COMPLETE` requires
complete canonical-domain coverage, established component maximality and a
finite set. `NOT_ESTABLISHED` keeps every valid local certificate but blocks a
global assertion.

No empty result or finite count can be inferred merely because a bounded search
stopped.

## 4. Root-continuation certificate

The new interval capability is conceptually:

```text
SelectedRootIntervalCertificate2D {
  root selector/token ownership
  source-pair and provider/target contracts
  driver interval and endpoint policy
  source/dependency revision vector
  interval-wide root existence and uniqueness proof
  branch/sheet continuation proof
  boundary evidence
  ambiguity/multiplicity exclusions
  guarantee, method, work and completeness
}
```

It is produced by the root-owning semantic capability, not by the dependent
point or exporter. It may use the existing exact token and root selector as the
identity being certified. It cannot create a token, switch to a nearby root, or
use previous point coordinates. At tangency, merge/split, multiplicity or
unsupported parameter-family evidence, it returns not established.

For `m`, the missing evidence is an interval family certificate for the exact
root selected by `S` as driver `R` moves. The existing one-state isolating
interval and revision-to-revision continuation are necessary inputs but not
sufficient proof. The proposed implementation must inspect whether the involved
SplineV2/line intersection capability can expose certified parameter-family
existence and uniqueness. If it cannot do so without a general root-identity
redesign, implementation stops rather than falling back to sampling.

## 5. Boundary-certification strategy

The shared certifier processes evidence in this order:

1. exact provider boundaries and dependency event surfaces already represented
   by the construction;
2. selected-root interval existence/continuation boundaries;
3. analytic/exact boundaries from an approved provider;
4. certified interval predicates proving existence or nonexistence over a box;
5. deterministic bounded numerical isolation driven by those predicates.

Point evaluations may reject a proposed interval and guide subdivision, but
they do not certify the unsampled interior. A numerical boundary must retain an
outward enclosure and guarantee. When a maximal boundary cannot be established,
the certifier may publish a conservative interior interval as local evidence,
leaving the enclosure unresolved and global completeness not established.

This admits useful, truthful results without pretending that a sampled plot is
the locus domain.

## 6. Producer architecture comparison

| Alternative | Strength | Defect / cost | Disposition |
|---|---|---|---|
| A. Generator-owned | direct providers can publish exact components cheaply | duplicates coverage assembly/root rules across generators; inconsistent consumer evidence | rejected as the general architecture; retained as an evidence source |
| B. Shared certifier only | one validation/completeness contract | a generic certifier cannot invent provider events or root-family semantics from point evaluation | rejected alone |
| C. Hybrid | generator owns canonical/structural facts; root capabilities own interval proofs; shared certifier validates and assembles one current structure | requires bounded shared interfaces and consumer migration | **selected** |

The normal Construction DAG remains dependency and invalidation authority. The
certifier is a deterministic semantic service invoked while publishing a Locus
definition; it is not a second graph or an export service.

## 7. `getValidDomainComponents()` migration

The accessor cannot acquire a new partial meaning in place. Existing total
metrics and complete intersection searches would silently overclaim.

Implementation sequence:

1. add the existence/completeness model alongside the existing accessor;
2. translate present direct/static providers to complete certificates only when
   their contracts prove interval-wide validity;
3. publish `NOT_ESTABLISHED` for dependent producers that have only nominal
   components;
4. migrate every consumer to an explicit policy;
5. make the legacy accessor a complete-only compatibility view; and
6. deprecate/narrow it only after all callers are migrated and old-file behavior
   is covered.

The semantic signature includes the certificate contract, coverage/completeness
and revision-relevant evidence. This preserves current revision evolution:
changed semantic coverage advances the definition revision; point queries and
render caches do not.

## 8. Consumer policy

### Rendering

Render each certified component or conservative local subinterval. Do not join
unresolved gaps or claim complete coverage. Render caches remain viewport-
dependent derivatives and never become certificate evidence.

### Metrics

An explicit component-local length/position may use a current certified
component. `Length[locus]` and any other total/global value require complete
domain decomposition plus the existing metric completeness/rectifiability
evidence. A known-components subtotal is separately typed and cannot masquerade
as total length.

### Intersections

Solvers operate only over certified continuous components. Existing per-root
local admissibility remains usable with result completeness `INCOMPLETE` or
`NOT_ESTABLISHED`. A global empty/all-roots claim requires both domain
completeness and solver completeness. Root tokens remain unchanged.

### Point/path interaction

An exact current semantic address may be evaluated within a certified component.
Nearest-point/global search, principal-component inference, closed traversal and
routes crossing unresolved coverage require sufficient global evidence and fail
closed otherwise.

### Nested loci and transformations

They carry the source coverage and completeness axis. A downstream producer may
transform certified intervals and lineage evidence, but it may not strengthen
`NOT_ESTABLISHED` to `COMPLETE` without independent proof.

### DXF

G9X1 approximates finite components independently and records one handle/domain
entry per output. It never bridges gaps. With global `NOT_ESTABLISHED`, a future
R2 implementation may publish locally certified components only under the new
explicit incomplete-semantic-fidelity status and mandatory sidecar. Preflight
and the sidecar must say that the output is not the complete locus. This is not
G9X1 partial-output-by-omission: every component claimed by the selected local
certificate is still strict and must succeed atomically.

The sidecar records locus identity, branch, revision-local component address,
any established lineage, exact interval/openness, semantic revision, guarantee,
work and global decomposition state.

## 9. Persistence and lifecycle

- `GeoLocusV2` persistent identity and branch keys remain stable.
- Certificates are immutable and current only for their source/dependency
  revision vector and policy version.
- A temporary undefined interval changes current coverage, not locus identity.
- Recurrence across a gap does not join components; lineage is reused only by an
  explicit interval continuation relation.
- Save/reopen and undo/redo reconstruct from existing durable dependencies,
  tokens, provider versions and policies. Numeric caches and sampled partitions
  are not persisted as authority.
- Copy/remap allocates identity according to existing G9A rules, then recomputes
  its certificate from remapped inputs.
- Compatible redefine follows G9A3 revision/currentness semantics; it does not
  preserve stale coverage for continuity.
- Old files remain valid. They receive no fabricated dynamic component lineage;
  inability to establish current proof yields `NOT_ESTABLISHED`.

## 10. Proposed implementation slices

No slice is authorized by this design candidate.

### R2-I1 — semantic values and complete-only migration seam

- add immutable coverage/component/boundary/completeness values;
- attach one revision-bound structure to each branch;
- lift truthful direct providers to `COMPLETE`;
- separate periodic canonical-domain validation in
  `SemanticGeneratorDescriptor1D` from existence coverage;
- keep dependent producers fail-closed as `NOT_ESTABLISHED`; and
- add model, revision, serialization/reopen and compatibility tests.

### R2-I2 — selected-root interval capability and `m`

- add the interval certificate interface at the root-owning shared capability;
- reuse exact token/selector/source-pair/lineage evidence;
- add deterministic boundary/coverage assembly and work limits;
- integrate `AlgoLocusIntersectionPointV2` dependency evidence with the hybrid
  certifier without letting the dependent point solve roots; and
- derive the `m` component set from certificate output, never hard-code sampled
  boundaries.

This slice stops if the actual SplineV2/line family cannot certify interval-wide
root existence without a broader root-identity redesign.

### R2-I3 — consumer migration and S1 completion

- migrate render, metrics, intersections, point/path, nested/transform and
  Spline occurrence consumers to their explicit policies;
- make G9X1 emit separate components and completeness-aware sidecar evidence;
- preserve R1 typed geometric population and strict selected-source behavior;
- update Desktop preflight wording; and
- run R2 plus combined S1 verification before author smoke.

Splitting implementation prevents a consumer from seeing local components
through the old complete-domain contract.

## 11. Compatibility matrix

| Area | Expected effect |
|---|---|
| Legacy `GeoLocus` / Classic | none |
| Existing direct LocusV2 providers | equivalent complete certificate after truthful lift |
| Dependent LocusV2 | explicit `NOT_ESTABLISHED` until interval evidence exists; no false whole-domain validity |
| Persistent identity | unchanged |
| Semantic revisions/currentness | coverage changes are semantic; stale certificates rejected |
| Intersection tokens | unchanged; interval certificate refers to, never replaces, the exact selector |
| Metrics/intersections | local results remain possible; global claims require domain completeness |
| Rendering | may show local certified geometry without authority/completeness claim |
| G9X1 | independent finite component export; explicit global incompleteness |
| SplineV2 | unchanged unless it consumes a LocusV2 with incomplete coverage, which must propagate |
| Old `.ggb`/`.cedg` | no inferred identity or persisted sampled partition |
| R1 geometric population | unchanged (`geocedg-dxf-geometric-2d/v1`) |

## 12. Decisions and limits at author review

The design is internally coherent and does not require coordinate or sampling
authority. The following choices are presented for author approval as one R2
contract:

1. hybrid producer/shared-certifier architecture;
2. revision-local dynamic components with optional proved lineage;
3. complete-only migration behavior for `getValidDomainComponents()`; and
4. locally certified DXF output under an explicit globally-incomplete fidelity
   status and mandatory sidecar.

No final numerical decomposition for `m` is asserted. Implementation remains
conditional on an interval-aware selected-root capability. If that family
cannot provide one within the bounded design, R2-I2 must return for a separate
semantic decision rather than approximate or redesign root identity silently.

`GUIDE_IMPACT=NO` for this design-only candidate: it changes no observable
product behavior. A later implementation is expected to update DXF guidance
because local certified output and global completeness become visible.
`BOOTSTRAP_IMPACT=NO` and `VERIFICATION_INFRASTRUCTURE_IMPACT=NO`: no runtime,
workstation prerequisite, registry, schema or executable verifier changes here.
The required validation level for this candidate is documentation/static only.

```text
PRE-G9B-S1-R2 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
PRE-G9B-S1-R1 = PARTIAL IMPLEMENTATION CANDIDATE
PRE-G9B-S1 = BLOCKED — SEMANTIC DESIGN IN PROGRESS
selfApproved = false
```

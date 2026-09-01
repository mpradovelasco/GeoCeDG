# G9S1 semantic Spline V2 implementation closeout

- Status: **PASS — AUTHOR APPROVED**
- Phase: G9S1
- implementationStarted: true
- selfApproved: false
- authorApproved: true
- passClaimed: true
- manualAuthorSmoke: **PASS**

## 1. Entry authority

Implementation began on
`feature/g9s1-semantic-spline-2d-capability` from clean published R5 main:

`5952dfdbd238e71e598f4d2ca92c3e03437df41c`

At entry `HEAD`, `main`, `origin/main` and direct remote `origin/main` were equal.
The annotated `geocedg-g9u0-r5-pass` object
`3712595fe2b168ba494379b6b3f0051e4122cfae` peels to that same commit. R5 is
`PASS — AUTHOR APPROVED`; its accepted manual-smoke disposition remains `PASS
WITH G9A FREE-INPUT LIMITATION CHARACTERIZED`.

The cross-phase risk
`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains **OPEN / TRACKED**. G9S1
does not claim its dedicated native round trip and does not close it.

## 2. Classic characterization and decision

Classic `Spline` builds an upstream Cartesian curve. The inspected algorithm
solves its interpolation family and compiles the polynomial pieces into
conditional coordinate expressions over a normalized parameter. The resulting
public curve does not expose provider-owned, reconstructible spans/knots or
canonical knot ownership to Locus V2 metric/intersection/token consumers.

The candidate implements approved Option B: a separate experimental `SplineV2`
parent publishes a new first-class `GeoLocusV2`. Classic `Spline`, its command
processor, public result type, legacy XML and ordinary consumers remain
unchanged. There is no implicit migration or generic `Path` conformance.

## 3. Productive implementation

### 3.1 Public family and semantic parent

Under the existing `--enableLocusV2=true` opt-in, `SplineV2` accepts the
localized command forms:

- `SplineV2[point-list]` (default degree 3);
- `SplineV2[point-list, degree]`;
- `SplineV2[point-list, degree, weight-function]`; and
- the documented host point-list wrapping form `SplineV2[P1,P2,...]`.

The public-dispatch regression covers the minimum three-point wrapping form
explicitly and proves it has the same default-degree semantics as
`SplineV2[{P1,P2,P3}]`; the three-argument structured
`[point-list,degree,weight-function]` overload remains distinct.

The bounded candidate accepts 3–32 finite 2D points, integral degree 3 through
`min(point-count, 12)` and a dense system of at most 512 unknowns. Each weight
increment must be positive and finite. Invalid/nonfinite/singular inputs publish
undefined/fail-closed state and recover through ordinary dependency recompute.

The parent keeps the list, degree and optional weight as exact normal-DAG inputs.
It publishes one new durable Locus V2 identity, one semantic branch, normalized
increasing domain `[0,1]`, one polynomial span per consecutive input pair and
provider-owned canonical knots. Equal first/last input points select a periodic
half-open domain. Interior knots are owned on the right; render vertices never
become spline authority.

### 3.2 Evaluation, metric and transformations

The immutable provider exposes analytic value/first-derivative evaluation and
the reconstructible polynomial span partition. Point-on-Locus uses the existing
semantic branch/address authority. Total and partial rich length split at knots
and reuse deterministic adaptive Simpson integration over the analytic
derivative, with the existing tolerance/error/work reporting.

The initial author smoke exposed one remaining public partial-length defect. The rich
`LocusLength(L,P,Q)` object was intentionally nonnumeric in Algebra and already
carried the correct finite value for the SplineV2 control, but evaluator-only
Locus V2 subarcs still used component arc-coordinate interpolation with
`FLOATING_POINT_UNCERTIFIED` evidence. That evidence is truthfully rejected by
the scalar-admissibility guard, and `CmdLength` had no three-argument
`GeoLocusV2` route. The replacement candidate establishes route-local adaptive
evaluator evidence over the exact semantic interval, then adds only the normal
guarded DAG:

```text
GeoLocusV2 + exact semantic P/Q
  -> hidden rich between-position metric
  -> existing scalar adapter
  -> GeoNumeric Length(L,P,Q)
```

`Length(...)` is therefore the ordinary scalar surface while
`LocusLength(...)` remains the rich status/evidence/diagnostic surface. No
integration is duplicated in `CmdLength`; invalid or mismatched endpoint
provenance and temporary source/endpoint invalidity make the scalar undefined
without retaining a stale value.

The first composed replacement run exposed a real G7B contract regression:
the initial patch had made every evaluator-only subarc use the new route-local
evidence path. The bounded correction scopes direct refinement to the explicit
G9S1 public-command capability. The historical generic capability still treats
component arc-coordinate interpolation as uncertified, while the G9S1 route
computes its own value and evidence over the exact semantic interval. The G7B
62-test productive authority plus its 3-test laboratory gate pass with that
separation.

The reduced replacement re-smoke then passed. For the canonical spline control,
`Length(S)=4` and `Length(S,P,Q)=2`. For the ordinary public Locus V2 control,
the actual generator branch key is `generator.main` and
`Length(L,LP,LQ)=2`. The earlier suggested key `scalar-locus/main` was an
incorrect smoke instruction, not a product defect.

All seven R5 similarity command forms consume the result through the common
`GeoLocusV2` seam. The transformed image receives a new durable identity,
retains the source semantic parameter/domain/orientation and propagates
polynomial/differential capability. Intersection selectors/tokens of transformed
queries are new source-pair identities.

### 3.3 One-sided rich intersections

For line, segment, ray, circle, supported conic and regular polynomial implicit
targets, a polynomial-target seam composes the target equation with each source
span. Recursive derivative-root partitioning plus deterministic
bisection/refinement discovers sign-changing and even-contact candidates.
Canonical knot/seam ownership removes equivalent duplicates.

The published arithmetic guarantee remains estimated floating error and global
completeness remains `NOT_ESTABLISHED`. A transverse root with established local
isolation enters the existing R4 deterministic selector/token ledger and can be
materialized exactly by identity. Tangent/nonisolated roots, zero-polynomial
overlap, unresolved candidates and exhausted work limits fail closed. Bounded
functions continue through the existing general rich capability rather than
being mislabeled polynomial-certified.

### 3.4 Pair rich-result boundary

For two piecewise-polynomial semantic loci, the candidate adds canonical
span-pair Bernstein-hull rejection, bounded subdivision and safeguarded
dual-parameter Newton refinement. It is symmetric under caller operand swap and
deduplicates knot/seam representations.

This pair capability is deliberately **rich-only**. Floating boxes do not prove
exhaustive interval-rounded rectangle coverage or unique pair isolation. Pair
candidates therefore carry no public continuation key, create no active ledger
allocation and are not point-materializable. Tangency/overlap/budget cases
remain typed fail-closed evidence. A future materializable pair extension needs
a separate symmetric rectangle+uniqueness certificate and selector; G9S1 does
not invent one-sided rank, coordinate or enumeration identity.

### 3.5 Persistence and lifecycle

Normal command inputs and existing Locus V2 identity/style/XML authority
reconstruct the model; coefficients, span bounds, solver cells and render data
are derived and not independently serialized. Candidate coverage includes
native `.cedg` reopen with downstream total metric, partial rich-parent/scalar
DAG and exact-token point; copy/remap and undo/redo of the partial metric;
dynamic source/endpoint/degree/target updates; and feature-off plus Classic
preservation without experimental creation authority.

## 4. Numerical and efficiency characterization

The implementation uses floating power-basis interpolation with scaled partial
pivoting/backward-error rejection; one-sided polynomial composition with
recursive derivative-root partitioning and bisection; pair Bernstein-hull
subdivision plus dual Newton; and the existing adaptive Simpson metric seam.

No exact arithmetic, outward-rounded interval certificate, Sturm/Descartes
global count, pair uniqueness or symbolic arc-length claim is made.
Instrumentation adds polynomial spans examined/rejected and raw root candidates
to existing evaluation/subdivision/refinement/pair counters. The source test
inventory declares 37 focal methods: 25 shared public/semantic cases, three
one-sided efficiency cases, seven pair-boundary/covariance cases and two
Desktop native persistence cases. Focused A and deterministic B each execute
37/37 with Checkstyle clean; their canonical summaries must match exactly.

The detailed method disposition and still-missing scholarly support are in
[the numerical-method review](../research/g9s1_semantic_spline_numerical_methods.md).
No citation has been fabricated.

## 5. Validation state

The candidate requires:

- focused G9S1 A and byte-equivalent normalized deterministic B;
- one-sided polynomial, pair-rich-only, efficiency and native Desktop tests;
- Classic spline controls, full Locus/G6–G8 and G9U0/R1/R2/R3/R4/R5;
- G9A, G9X1, G5, Checkstyle and the static G9S1 verifier;
- `git diff --check`, `git diff --cached --check`; and
- full `tools/agent/verify.ps1` ending with
  `All GeoCeDG verification gates passed.`

Canonical summary hashes, final path inventory and composed logs remain ignored
generated evidence rather than self-referential durable inputs. The frozen
candidate passed both focused runs and the full composed verifier. The initial
author smoke passed every characterized area except partial length; the reduced
replacement re-smoke then passed and supplied the author approval recorded here.

## 6. Compatibility, prospective UX and retained risks

- Classic `Spline` and every non-V2 command route remain unchanged.
- The existing Locus V2 opt-in is the only feature gate; there is no separate
  spline/intersection flag.
- G9U1 was not executed. Its prospective post-G9S1 prompt consumes only current
  exact one-sided tokens; pair-rich-only candidates cannot produce markers or
  materialization actions until kernel authority is strengthened separately.
- R5's free-input `k=0.25` rejection remains current G9A
  `REDEFINE_CONTEXT_MISSING`, not a dilation or spline defect. Future G9U1 may
  investigate compatible redefine only through the atomic G9A seam; labels do
  not become durable identity.
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains open/tracked for G9U1 or
  explicit global-G9 disposition.
- Scholarly sources for method-specific numerical theorems remain research
  requirements.

## 7. Terminal declaration

```text
G9S1 = PASS — AUTHOR APPROVED
implementationStarted = true
selfApproved = false
authorApproved = true
passClaimed = true
manualAuthorSmoke = PASS

G9U0-R5 = PASS — AUTHOR APPROVED
G9U1 = DESIGNED — NOT AUTHORIZED
G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
```

This report cannot be changed to PASS without explicit author approval.

# G9S1-R1 structural spline continuity prerequisite

- State: author-authorized bounded prerequisite correction; **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
- `implementationComplete=true`; `manualAuthorSmoke=PENDING`;
  `selfApproved=false`; `authorApprovedPhase=false`; `passClaimed=false`.
  Corrections A/B resolve the quintic-admission and false-transverse regressions.
  Fresh PHASE A/B, COMPOSED and mandatory FULL clean completed with exit 0;
  historical failed cohorts are preserved and DEV does not establish acceptance.
- The subsequent author decision authorizes corrections A/B only, retaining the
  structurally continuous model and unchanged acceptance predicates. Current
  technical completion is backed by fresh source-cohort execution, not author
  phase approval.
- No phase PASS, commit, publication or G9U1 implementation is authorized.
- Preserved predecessor: [scientific blocker report](../validation/g9s1_r1_implementation_blocker_report.md)
  and its immutable evidence. The 40-path entry at `109f077fc5e2a40bcde45d3271eb928ee66fdfcc`
  remains intact; the protected G9U1 checkpoint is `00982e7e148a634cd57ed928f322774df267d5e3`.
- Authority: current source, ADR 0018, approved D2/ADR 0021, and the author's
  structural-knot continuation instruction. This note preserves the design-before-code
  record and the final implemented contract.

## 1. Defect and authority boundary

The native three-point cubic produced independently rounded power spans whose
exact values at 1/2 were +2^-55 and -2^-55. Canonical right ownership does not
make their real-polynomial limits agree. The prior negative diagnostic/evidence
must not be reclassified as success. No epsilon gluing or proximity root merge
is permissible.

The new model must define a structurally continuous real spline with numerical
coefficients; it does not claim exact interpolation arithmetic. Rounded expanded
double spans are derived acceleration data, not the interval proof authority.

## 2. Same spline space; open boundary equations

Let N be the input point count, d the degree, M=N-1 the spans and m=N-2 the
simple interior knots in the unchanged normalized domain. Use the truncated
power form, independently for x and y:

```text
S(t) = sum(p=0..d) a_p t^p + sum(j=1..m) b_j (t-k_j)_+^d.
```

Every hinge has zero jets of orders 0..d-1 at its activation. Conversely, for
any piecewise degree-d C^(d-1) function, subtract the first polynomial and then
successive d-th derivative jumps divided by d!, times the hinges; the remainder vanishes on
each span. Thus the representation is injective and spans exactly this space,
with N+d-1 free coordinates. This is a space equivalence, not a claim that every
choice of interpolation/boundary data yields a nonsingular well-conditioned
system.

The standard spline-space characterization and truncated powers are supported
by de Boor, [B(asic)-Spline Basics, section 5, Theorem 5](https://ftp.cs.wisc.edu/Approx/bsplbasic.pdf).
The following boundary mapping is derived from the actual GeoCeDG source, not
attributed to a standard natural-spline theorem.

Preserve ALL current open equations:
- N interpolation rows;
- first-span derivative order d-1 at t=0 equals zero;
- last-span derivative order d-1 at t=1 equals zero;
- for offset=2..d-2, derivative order d-1 of span M-offset evaluated at
  knot k_(offset-1) equals zero.

The last rows may evaluate the *polynomial extension of a selected span* outside
that span. Their active hinges must be fixed by span ownership, not by the
evaluation t. Replacing these with conventional natural/not-a-knot conditions
would change the family and is forbidden. Reduced dimension is N+d-1 rather
than M(d+1). The original 3..32 point, 3..min(N,12) degree and <=512 legacy
work-policy admission are retained.

The first resumed DEV (65 cases, 61 passing) rejected the open degree-12
system before representation creation. The bounded numerical response is
column equilibration after the existing row scaling: with
`c_j=max_i |A_ij| > 0`, solve `A diag(1/c) z=b` and recover
`x=diag(1/c) z`. This invertible variable change preserves the equations,
structural basis and original backward-error test; the pivot threshold is not
relaxed. Reduced dimension does not imply good conditioning. A genuinely
singular source-specific boundary problem must still fail closed.

The later high-degree diagnostic also distinguishes numerical inadmission from
singularity. Reconstructing the published independent-span solve and its
unchanged original-equation backward guard rejects the tested nonlinear open
degree-12 sinusoidal data, and the tested 17-point uniformly parameterized exact
cubic data. The latter reduced system has exact rank 28; this does not ensure
that the finite-precision solve passes the required backward check. These remain
explicit historical baseline/pre-correction rejection controls, not suppressed
positives or a claim of mathematical singularity. The newly authorized
higher-precision fallback may legitimately admit them only after the unchanged
original equations and admission predicates pass. Preserve their historical
rejection evidence rather than require perpetual rejection of a valid input.

The positive degree-12 perimeter is the admitted open straight source and
admitted nonlinear periodic source. An admitted nonlinear degree-7 source
additionally compares against independently solved original equations at two
higher precisions. A universal forward-error bound of 1e-8 for all high-degree
inputs, or universal numerical admission of all finite point/degree inputs,
was not part of the published contract. The unsuccessful intermediate
three-step solve-refinement experiment is not retained. Column equilibration,
the original equations and the existing backward guard remain; no required
positive relies on that removed experiment.

## 3. Periodic seam: exact structural elimination

A closed image has the same interior form, with periodic jets at 0~1. Treat
a0 and the m hinges as free coordinates. Descending r=d-1..0, solve exactly:

```text
a_(r+1) = -( sum(p=r+2..d) binomial(p,r)*a_p
              + binomial(d,r)*sum(j) b_j*(1-k_j)^(d-r) )/(r+1).
```

This follows by subtracting S^(r)(0)/r! from S^(r)(1)/r!. The triangular
diagonal r+1 is nonzero. It therefore parameterizes exactly the periodic
subspace; dimension is N-1. Solve its N-1 independent interpolation rows,
then revalidate the original complete interpolation/boundary problem.
No Bernoulli/B-spline family is substituted.

Implementation holds exact finite numerators A_p=D*a_p, B_j=D*b_j over
D=d! (<=479001600). Starting with binary64 free coefficients interpreted
exactly, or bounded retained decimal coefficients under correction A,
descending elimination preserves exact finite decimal numerators:
A_p has the needed (p-1)! divisibility in the represented coefficient ring. Each division by
r+1 terminates exactly; no MathContext rounding may establish the seam.
Exact rational expansion with common D therefore preserves all jets through
d-1, including for rounded/non-dyadic-as-mathematical-input binary64 knots.

The host's approximate GeoPoint.isEqual endpoint classification is insufficient
for exact periodic C0. If it classifies a closed source but the finite endpoint
coordinates are unequal as real binary64 values (signed zero normalized), reject
with a specific inconsistent-closure diagnostic. Do not average endpoints or
silently turn an open input into a closed source. Identical source points and
exactly equal finite coordinates retain closure.

## 4. Selected numerical implementation and alternatives

First implement direct solving in the reduced structural basis, using the
existing deterministic scaled-pivot/backward-error policy. Independently
revalidate original interpolation, fixed-span boundary rows, finiteness and
source parameter validity. Numerical residual <=existing 1e-9 policy is an
engineering validity condition ONLY; it never establishes knot equality.

A post-solve projection from old spans was considered but is not selected:
it would retain the large independent solve, need an explicit projection
definition and revalidation, and introduce avoidable geometry-displacement
ambiguity. Shared jets/exact elimination are algebraically equivalent; the
common-denominator structural form is the smallest bounded exact periodic
bridge identified. A general B-spline/NURBS subsystem and broad exact linear
algebra dependency are outside scope.

Poor conditioning, inconsistent data, nonfinite arithmetic or work exhaustion
fails closed. Do not increase tolerances to admit a degree/fixture. Record any
new admission/regression limitation as a blocker, not a hidden scope reduction.

## 5. Kernel seam and rigorous interval bridge

Keep SplinePolynomialModel2D as the immutable derived authority. Native create
uses structural free coordinates and exact numerator expansion. Preserve the
private old coefficient constructor solely for existing diagnostic fixtures;
it has no inferred structural guarantee. Proposed narrow model accessors are
exact span coefficient numerators, common denominator and structural continuity
order. The final names must match implemented source.

Ordinary approximate evaluation/derivatives may use rounded spans as before.
R1 instead encloses numerator/D exactly: obtain a finite approximation, compare
its exact binary64 value times D with the numerator, choose outward endpoints
and independently verify enclosure. If even adjacent representable endpoints
cannot enclose it, fail closed. Interval polynomial/Jacobian operations then
enclose the exact structural expansion. Never reinterpret rounded caches as
exact glue. Captured R5 transformations remain outward-enclosed in original
composition order. No certificate comes from render data or old broad rejection.

Interior/shared seam charts may use structural C^(d-1); legacy exact diagnostic
spans still require actual jet equality. Adjacent discoveries represent the same
root only through canonical ownership and certified uniqueness relation, never
parameter or Cartesian closeness. Original Krawczyk existence/uniqueness and
class-specific component-product coverage remain required.

The bounded certifier first attempts the expanded neighborhood, then the
original computational cell if expansion introduces an uncertifiable critical
point. Both paths require the same smoothness, sign-definite germ and strict
outward Krawczyk inclusion; the stored uniqueness region is the region actually
proved. If neither certifies a root, an outward Krawczyk image disjoint from the
original cell proves that cell empty: every zero in the cell must belong to
that image. This exclusion is not existence proof and never assigns identity.
All attempts/refinements share the existing work caps; no depth, tolerance or
admission threshold is increased. The nearby-root positive and the closer
transverse depth-exhaustion negative remain separate regressions. Trying the
original cell alone was insufficient in the recorded intermediate diagnostic.

### 5.1 Author-authorized correction A: bounded construction precision

The 25-point degree-5 historical positive exposed cancellation in the reduced
global-basis solve; exact structural continuity alone does not make the
computed interpolation acceptable. Retain the same equations, knots, family,
domain and exact jet construction. A successful binary64 admission remains the
fast path. Only numerical failure may enter a deterministic finite precision
ladder, using exact binary64 input values, explicit decimal rounding and fixed
operation/pivot order. The selected plan uses 48, 80 and 112 decimal digits;
the maximum is a fail-closed cap, not permission to keep increasing precision.
The implemented policy uses `HALF_EVEN`, at most 10,000,000 counted construction
operations, and policy identifier `spline-structural-precision/v1`. These are
arithmetic-work counts, not elapsed time or a bit-complexity bound.

Each attempted representation must pass the original defining-equation,
backward-error, finiteness, structural and periodic checks without tolerance
inflation. Consecutive sufficiently precise constructions must establish the
declared canonical-coefficient stability, not coordinate proximity. Prefer
canonical binary64 free coefficients when they satisfy those checks. If that
cast is demonstrably insufficient, bounded higher-precision derived structural
authority is permitted, with exact expansion and certificates enclosing that
actual authority. Current canonical retained precision is 32 digits after
agreement of 48/80 working digits or 64 after agreement of 80/112; coefficient
arrays must match exactly after the declared canonicalization. Exact singleton
defining rows may restore an exact terminating quotient; this is not near-zero
snapping and all original rows are revalidated. It must reconstruct from
ordinary command inputs and must not serialize coefficients, caches or
precision history.

Expose fast/fallback construction, attempted precision, work and failure-reason
evidence without using it as durable identity. Genuinely singular/invalid data
and exhausted precision/work remain rejected. The dedicated current tests,
including the formerly passing quintic and unchanged invalid-input guards,
pass within the completed technical gates; author phase approval remains pending.

### 5.2 Author-authorized correction B: structural univariate proof

For a `REGULAR_POLYNOMIAL_IMPLICIT` target Q whose stored coefficient matrix is
the exact target input, and authenticated SplineV2 (including supported R5
composition), discovery may still use a floating composed power
polynomial. Such discovery cannot establish the multiplicity or transverse
classification of the different structural function. Before public identity or
ledger allocation, evaluate outward interval extensions of
`g(I)=Q(S(I))` and `g'(I)=gradient(Q)(S(I)) dot S'(I)` from the actual structural
source. Independent rounding of the composed polynomial is not proof authority.
The bounded correction does not certify line/conic/circle adapters through
their expanded matrices. In particular, a circle's polynomial expansion is
rounded derived data from its radial center/radius authority; the historical
circle path remains unchanged, rather than certifying that different function.

If zero is excluded from g(I), publish no verified root from that interval.
A simple/transverse candidate requires a rigorous existence/uniqueness test
(the selected plan uses scalar interval Krawczyk) and exclusion of zero from
g'(I). Inconclusive, derivative-ambiguous or multiple contacts remain rich-only;
an empty Krawczyk intersection is exclusion, not existence. Typed current proof
must survive discovery-to-publication boundaries without heuristic deduplication
or index identity. Do not repair false classification by suppressing ledger
tokens after the fact.

The retained uncertified rich-contact path additionally requires either an
actual stationary proposal from the existing floating derivative partition or
an exact native structural boundary-zero witness, together with compatible
structural value/derivative enclosures. `PolynomialRootIsolation2D`
records that discovery origin and preserves it through its existing candidate
deduplication; the flag proves neither existence, multiplicity nor identity.
It prevents an unrelated side-refinement artifact from acquiring the same
rich-contact provenance. Singular implicit contacts may remain estimated rich
evidence without a simple-root certificate or exact multiplicity claim.

The boundary alternative evaluates Q(S(knot)) using the native structural
numerator/denominator authority exactly; a rounded coordinate equality is not a
witness. It is deliberately unavailable for transformed boundary shortcuts,
whose ordinary outward interval path remains in force. A boundary zero can
establish that contact value without proving multiplicity, isolation or token
eligibility. Counters distinguish boundary witness checks and exact zeros from
stationary discovery and interval simple-root proofs.

Current certified proof is preserved through the public target adapter. The
oriented germ uses the existing canonical target orientation and the certified
derivative sign, not a pointwise derivative epsilon. Separate included open
endpoints and periodic lifted charts retain their actual semantic boundaries;
canonicalization must outward-enclose lifted proof regions and use certified
root equivalence, not ordinary parameter-distance deduplication. A deliberately
seeded below-dedup-distance test validates that certificate/publication seam;
it is not evidence of floating discovery completeness at that separation.

The historical `(x^2-1)^2+y` contact control must retain its two genuine contacts
without four spurious transverse materializable splits. Do not square-free,
factor away or perturb the multiplicity to manufacture a simple root. All
refinement/depth/work limits are explicit and deterministic; unresolved work
fails closed. This correction neither invents general multiplicity certification
nor changes pair singleton-germ semantics. Its technical gates are complete;
author phase approval remains pending.

## 6. Signatures, persistence and compatibility

Bump model/evaluator/polynomial capability content signatures for the corrected
representation. Preserve source durable ID, stable branch/component/provider
parameter contract and normal command dependencies. Revisions capture changes.
Do not persist coefficients, interval queues, boxes, seeds or render caches.

Old files rebuild from their ordinary SplineV2 inputs; exact source/point
associations must not be invented. Current token proof is revalidated against
new current source evidence; structural selector identity excludes coefficients.
Keep ledger v5 pair discrimination and R4 one-dimensional identity separate.
Classic Spline source/behaviour is untouched. Test old XML, native archives,
copy/remap, undo/redo, rename, valid/invalid recovery and R6 exact addresses.

The native lifecycle audit also exposed an output-serialization boundary:
`GeoLocusV2.getXML` formerly omitted a staged replacement element with no live
locus ID, even while the existing G9A transaction supplied an exact temporary
serialization ID. The bounded correction consults
`SpatialIdentityRegistry.getPersistentGeoIdForSerialization(this)`, the same
lexical overlay used by the inherited identity attribute. It neither attaches a
live ID nor promotes an unauthenticated diagnostic locus. Compatible-redefine
predicates, explicit incompatible replacement semantics, atomic transaction and
copy authority are unchanged; no G9A implementation is broadened. Native
controls cover replacement without consumers, with a rich-only result, with a
materialized pair point, and with an ordinary semantic point. The initial
unsupported-redefine hypothesis was disproven by tracing this earlier seam.

## 7. Validation and stop boundary

First preserve the old knot discrepancy as a fixed exact historical diagnostic,
then add structural model tests for degrees 3/intermediate/12, sizes 3..32,
asymmetric and binary64-rounded knots, coordinate scales, all jets and periodic
elimination. Require real native both/one-source knot roots, nearby roots,
seams, transformed/negative-dilation cases and repeated-germ negatives.

Resume ALL R1 positives/negatives, symmetric selector, path independence,
lifecycle and persistence. Do not stop at the first corrected knot.
The corrective DEV also exposed a historical test-presentation mismatch in
`G9S1SemanticSpline2DTest.roundedX`: after the existing `Math.rint` 1e-9 grid,
Java `List<Double>.equals` distinguished -0.0 from +0.0 despite the required
three roots and distinct tokens being present. The bounded test-only correction
canonicalizes zero only in that rounded comparison helper. It changes no
product coordinate, semantic direction/parameter bit, tolerance, geometry or
identity assertion. The old failed output remains historical evidence; exact
persisted R6 semantic-direction bits are not subject to this presentation rule.
DEV is diagnostic; complete PHASE A/B, COMPOSED and FULL-CleanBuild according
to the current ADR 0020 source-cohort contract before technical completion.
No concurrent source writers during builds.

The final 76-path tested cohort completes [PHASE A](../../artifacts/g9s1-r1-numerical-corrections/phase-a-02/verification-result.json)
and [PHASE B](../../artifacts/g9s1-r1-numerical-corrections/phase-b-02/verification-result.json)
with 192/192 tests each and canonical summary SHA-256
`59793eef3641d8c93b012998c0a795fb949f5cefdb54d30ebcd337542ab155dd`.
[COMPOSED](../../artifacts/g9s1-r1-numerical-corrections/composed-02/verification-result.json)
passes 1281/1281; [FULL clean](../../artifacts/g9s1-r1-numerical-corrections/full-clean/verification-result.json)
records 7781 tests, 7770 passed, 11 upstream skips and zero failures. All four
receipts exit 0 and distinguish technical gates from author approval. These
results replace no historical failed evidence. The manual author smoke is pending.

VERIFICATION_INFRASTRUCTURE_IMPACT=UPDATE_REQUIRED (existing R1 phase integration).
BOOTSTRAP IMPACT — NO CHANGE REQUIRED: existing Java and exact-decimal support;
no new runtime/toolchain/dependency or workstation assumption. Recheck if this changes.
GUIDE_IMPACT=UPDATED: representation, proof boundary and public pair scope need
living developer/user guidance after validation.

Failure of required periodic structure, certificate, lifecycle or historical
scientific gates remains a real blocker. G9U1 stays unimplemented/unauthorized.
G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP remains OPEN / TRACKED and independent.

## 8. Source-derived work and materialized-child boundary

This is a code-level cost characterization, not measured performance or a
completed validation result. Source locations below refer to methods, so a
later report must recheck them against its final source snapshot.

- [AlgoLocusLocusIntersectionV2.compute](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java)
  calls the pair solver once for a rich-result recomputation.
  [LocusPairIntersectionSolver2D.intersect](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusPairIntersectionSolver2D.java)
  performs discovery/verification, then invokes the public pair publisher once.
  [PublicSplinePairRootIdentityResolver2D.publish](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PublicSplinePairRootIdentityResolver2D.java)
  obtains one query-local interval certificate, sorts structural classes, and
  binds eligible singleton classes. This work is not repeated by each point.
- [AlgoLocusIntersectionPointV2.compute](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java)
  reads the exact token, resolves its current/retained binding, and either sets
  coordinates from the already-published solution or sets the existing point
  undefined. It does not allocate a GeoPoint, call the pair solver/certifier,
  evaluate either source independently or replay a trajectory. GeoPoint
  allocation is in the constructor only. Unchanged claims return immediately
  from `synchronizeMaterializedClaim`.
- [GeoLocusIntersectionResult.findExactPointAdmissibleSolution](../../source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java)
  uses ledger validation followed by
  [LocusIntersectionResult2D.findPointAdmissibleSolution](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionResult2D.java).
  The immutable snapshot builds its admissible-token map once and removes
  duplicate keys fail-closed. The ledger's `Snapshot.validatedEntry` is likewise
  a token-map lookup. Ordinary unchanged child updates therefore cost expected
  O(P) for P materialized children at fixed selector/token size.

Do not extend the earlier one-dimensional R4 complexity slogan to the complete
new pair solver. Let S be the initial span-product box count, B the visited box
count, R the total certified roots, C the structural germ-class count, and E the
retained ledger entries. In
[SplinePairIntervalCertification2D.ComponentProof](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/SplinePairIntervalCertification2D.java),
`coveredByRoot` scans roots for each box/unresolved leaf, `addRoot` scans prior
roots, and `classify` may compare every pair of roots. A conservative
combinatorial bound for this certificate/publishing layer is
`O((S+B)R + R^2 + C log C + CR + E log E + P)`, in addition to ordinary discovery,
model capture and interval-polynomial arithmetic. It is not an end-to-end
bit-complexity bound: degree, spans, exact numerator size and transformation
composition affect arithmetic cost.

The pair ledger builds its current slot map once and resolves each certified
slot by map lookup. Commit visits current solutions and retained entries;
`Snapshot` sorts all E entries, including dormant claims, not only the R current
roots. For a pair-only query E is bounded by current allocations plus distinct
existing point claims; it is not a store of every past root. First claim/release
can rebuild a snapshot, and immediate closure-copy rebase can scan retained
entries. Those setup/copy paths must not be described as constant-time ordinary
recompute. See [LocusIntersectionTokenLedger2D](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java),
`Evaluation`, `resolveCurrentPairRoot`, `commit`, `Snapshot`, and
`rebaseCopiedRetainedToken`.

The current measurable certificate counters are `Result.getBoxesVisited()` and
`Result.getKrawczykAttempts()`, published as `PAIR_CERTIFICATION_WORK`
diagnostics. Component-pair, parameter-box, box-depth, root-publication and
Krawczyk-attempt limits remain explicit. These counters do not count every
comparison, every arithmetic operation or elapsed time. Structural tests also
record reduced/legacy dimensions and exact-numerator size/hash; a smaller
dimension alone is not a speedup measurement. Final evidence must distinguish
these counters, source-derived bounds and any actual timings. Scenario R1-L09
combines this inspected call chain with executed no-new-point lifecycle
assertions; a point-count assertion alone is not proof of zero solver calls.

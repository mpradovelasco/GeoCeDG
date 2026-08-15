# G8C2 Locus V2 × Locus V2 intersection kernel report

**Status: PASS — AUTHOR APPROVED**

**Execution date:** 2026-08-15
**Entry commit:** `a585591afd073ec390ffc34a532d390472277150`
**Branch:** `feature/g8c2-locus-v2-locus-v2-intersections`

## 1. Disposition

The authorized G8C2 task produced an internal, experimental, nonpersistent
two-semantic-source intersection kernel. It implements the normative Locus V2
× Locus V2 contract and Accepted ADR 0009 without changing the G8B or G8C1
authorities. On 2026-08-15 the author approved decisions D1–D13, closed G8C2
and closed the global G8 gate. This approval does not make the capability
public and authorizes only a future, separately executed G9 design task.

The implemented semantic chain is:

```text
GeoLocusV2 A + GeoLocusV2 B
    -> canonical two-source query and coherent evaluator session
    -> branch/component product and query-local parameter boxes
    -> dual-parameter refinement
    -> independent evaluation of F(t) and Q(u)
    -> immutable rich intersection result
    -> existing normal-DAG rich Geo
    -> existing strict token-selected point consumer
```

## 2. Productive architecture

`AlgoLocusLocusIntersectionV2` registers both semantic loci and any explicit
capability dependencies in the normal construction DAG. It captures both
definitions/revisions before solving and publishes one coherent rich snapshot.
Failure or budget exhaustion never exposes an old finite result as current.

`LocusPairIntersectionQuery2D` separates caller order from a canonical unordered
source-pair identity. `LocusPairIdentity2D` hashes framed semantic lineage only;
coordinates, parameter values, intervals, revisions, result order, and
completeness never enter the token.

`LocusPairIntersectionSolver2D` reuses the G8 rich-result authority and owns:

- exhaustive enumeration of declared branch/component products within budgets;
- capability selection and query-local evaluator fallback;
- semantic parameter-pair deduplication;
- independent two-sided semantic evaluation and model-coordinate residual;
- normalized tangent determinant where both tangents are regular;
- typed pair-local isolation, revision, overlap, and guarantee evidence;
- complete/incomplete/not-established set semantics;
- atomic success, unsupported, work-limit, and numerical-failure publication.

The fallback `EvaluatorPairIntersectionCapability2D` samples semantic parameter
components only as a nonauthoritative broad phase, visits segment-product boxes,
and applies safeguarded dual Newton refinement. Every returned candidate is
reevaluated and residual-checked by the solver. The fallback never establishes
global completeness, local uniqueness, multiplicity, or overlap from sampling.

## 3. Result, isolation, tangency, and overlap

Each finite solution adds ordered pair evidence:

- locus identities and current semantic revisions on both sides;
- branch/component bindings and semantic parameters;
- two isolating intervals (a revision-scoped parameter rectangle);
- local coverage/uniqueness/method/guarantee;
- both evaluated semantic points and `||F(t)-Q(u)||`;
- normalized tangent determinant when available;
- solver method and numeric guarantee.

`LocalIsolationStatus.ESTABLISHED` requires an analytic/certified capability to
provide exhaustive local rectangle and uniqueness evidence. Small residual or
Newton convergence alone remains `NOT_ESTABLISHED`. Consequently evaluator-only
roots are valid finite geometric results but not point-admissible. Option B is
preserved: an isolated, verified, identified root from an authoritative
capability is point-admissible under `COMPLETE`, `INCOMPLETE`, or
`NOT_ESTABLISHED` parent completeness.

A normalized determinant with magnitude above the versioned threshold
establishes transverse contact. Near zero never establishes multiplicity by
itself. Tangency/multiplicity are published only when analytic or certified
candidate evidence supports them; otherwise classification is undetermined.

Overlap is represented only by typed rich evidence. Established overlap needs
an authoritative component relation/parameter map. Evaluator agreement can
produce `OVERLAP_SUSPECTED_NOT_ESTABLISHED` only. Full, partial, reverse,
repeated-traversal, unsupported, and mixed finite-plus-overlap cases retain
their distinct result semantics; no arbitrary finite samples are manufactured.

## 4. Identity and lifecycle

The canonical source pair makes operand reversal geometrically symmetric while
`LocusPairIntersectionEvidence2D.reversed()` preserves the reversible ordered
view and determinant sign. Equal coordinates from different branch/component
or parameter-pair lineages keep different tokens.

Continuation compares one explicit semantic continuation key in the same
constructive branch-pair/topology context. It does not use coordinates or list
order. One source change, both-source change, discovery/reordering, absence and
recovery, periodic seam representation, merge/split candidates, topology
ambiguity, overlap entry/exit, and exception recovery have focused traces.
Overlap clears the finite continuation baseline; leaving overlap creates a new
identity unless a later explicit policy establishes a relation. No universal
merge/split genealogy is imposed.

## 5. Domains, completeness, and guarantees

The initial candidate supports products of declared finite components and
approved periodic fundamental domains. An empty semantic operand is a complete
empty result. Unbounded components are typed unsupported; no viewport or
arbitrary window can establish completeness.

The evaluator fallback reports `NOT_ESTABLISHED` completeness. An injected
analytic/certified capability may report `COMPLETE` only while identifying all
covered component-pair keys. Missing coverage downgrades a complete claim.
Failed verification of an exhaustive candidate downgrades completeness to
`INCOMPLETE`.

The two-source residual is the Euclidean norm between independently evaluated
semantic points and therefore has model-coordinate length units. It is not an
isolating certificate. Pair parameter tolerances remain provider-specific on
each axis. The tangent determinant is dimensionless after normalization by
both tangent norms.

## 6. Work and state

The versioned policy `g8c2-pair-initial/v1` bounds:

| Quantity | Limit |
|---|---:|
| branch pairs | 256 |
| component pairs | 1,024 |
| parameter boxes | 32,768 |
| box depth | 16 |
| candidate boxes | 4,096 |
| pair refinements | 1,024 |
| iterations per refinement | 80 |
| Jacobian evaluations | 16,384 |
| overlap checks | 4,096 |
| continuation comparisons | 4,096 |
| finite outputs | 256 |
| retained pair entries | 0 |

The representative evaluator-only line crossing records:

```text
semantic evaluations                 120
semantic derivative evaluations       18
residual verifications                  1
verified roots                          1
branch pairs                            1
component pairs                         1
parameter boxes                     1,024
boxes rejected                      1,020
candidate boxes                         4
pair refinements                        4
refinement iterations                   8
Jacobian evaluations                    9
overlap checks                          1
pair continuation comparisons           0
retained pair entries                    0
```

The 1/10/100 query sweep is deterministic. One hundred point consumers add no
solver evaluations. A 2×3 product reports six components; 33×33 publishes a
coherent `WORK_LIMIT_REACHED` result. All render/sample/viewport/pixel/metric
authority and whole-locus-regeneration counters are zero. No shared owner,
index, global cache, or G7 metric state is introduced.

## 7. Validation evidence

The three focused suites contain 34 tests:

| Suite | Tests | Purpose |
|---|---:|---|
| `G8C2LocusPairKernelTest` | 16 | geometry, pair evidence, source symmetry, isolation, tangency, overlap, completeness, domains |
| `G8C2LocusPairLifecycleTest` | 10 | two-source updates, tokens, merge/split, overlap transition, atomicity, depth 1/2/3, seam, viewport invariance |
| `G8C2LocusPairFunctionalBenchmarkTest` | 8 | 1/10/100, 100 consumers, deterministic vector, component combinatorics, work limit, reparameterization, scale/translation |

Independent references are reproduced from
`geocedg/validation/locus-v2/g8c/generate_extended_intersection_references.py`
using CPython 3.12.13, mpmath 1.4.1, and 80 decimal digits. The preserved output
contains transverse line, circle pair, tangent pair, reverse overlap,
source-order symmetry, and constructive-multiplicity formulas. It is evidence,
not kernel authority.

Full command results and final log locations are recorded in the machine-
readable G8C2 evidence after the authoritative verification run.

## 8. Compatibility and scope audit

The candidate adds no public command, dispatcher overload, generic `Path`,
point-on-Locus API, XML/persistence/factory registration, migration, legacy
`GeoLocus`, Classic algorithm behavior, frontend, 3D, Python DSL, or G9
behavior. No `GeoClass` addition is required. Existing G8B/G8C1 target adapters
and public behavior remain unchanged.

## 9. Author closeout decisions

| Decision | Implementation evidence | Final disposition |
|---|---|---|
| D1 — Rich-result reuse | One result hierarchy and existing rich Geo/point consumer; pair evidence is additive | **APPROVED** |
| D2 — Canonical unordered source pair | Operand-reversal test preserves token and reverses ordered evidence | **APPROVED** |
| D3 — Pair solver boundary | Dedicated dual-parameter solver; shared lifecycle/result only | **APPROVED** |
| D4 — Evaluator fallback guarantee | Independently verified roots, but no local/global proof claims | **APPROVED** |
| D5 — Option B | Isolated authoritative roots consumable under all completeness states | **APPROVED** |
| D6 — Local pair isolation | Exhaustive rectangle plus uniqueness basis; residual/Newton insufficient | **APPROVED** |
| D7 — Tangency | Normalized determinant plus analytic/certified evidence; no threshold-only multiplicity | **APPROVED** |
| D8 — Overlap | Established/suspected/unsupported and mixed typed outcomes; no sampling authority | **APPROVED** |
| D9 — Constructive identity | Semantic token/key/lineage only; overlap and ambiguous merge/split break continuation | **APPROVED** |
| D10 — Domain scope | Finite components and periodic fundamental domains; unbounded rejected | **APPROVED** |
| D11 — State | Query-local, bounded, zero retained entries | **APPROVED** |
| D12 — Budgets | Initial versioned limits and measured counter vector above | **APPROVED AS INITIAL VERSIONED DEFAULTS** |
| D13 — G8C2 closeout | All focused, compatibility and composed gates pass | **PASS — AUTHOR APPROVED** |

## 10. Global G8 closeout

The author confirms that the approved G8 sequence now covers the required
native 2D incidence architecture: G8A characterization, G8B basic targets,
G8C1 extended one-parameter targets and G8C2 Locus V2 × Locus V2. Rich
solutions can feed the strict token-selected dynamic point consumer whenever
local identity and continuation are established.

Degenerate conics, unrestricted functions, singular/general implicit curves,
unbounded locus products and unresolved general overlap remain explicit typed
unsupported or not-established boundaries. They are not accidental omissions
and do not imply universal curve support.

Final phase disposition:

```text
G8C2 = PASS — AUTHOR APPROVED
G8 = PASS — AUTHOR APPROVED
G9 DESIGN = AUTHORIZED — NOT STARTED
G9 IMPLEMENTATION = NOT AUTHORIZED — NOT STARTED
```

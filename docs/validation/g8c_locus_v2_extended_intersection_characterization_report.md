# G8C extended Locus V2 intersection characterization report

| Field | Value |
|---|---|
| Status | **PASS — AUTHOR APPROVED** |
| Baseline | `3531db7838426305c505c291b1d614aa6df5175c` (`geocedg-g8b-pass`) |
| Productive G8C code | **NONE** |
| Test-private probes | 32 |
| Approved phases | G8C1 one-parameter targets; G8C2 locus-locus |
| Characterization date | 2026-08-14 |
| Author-review date | 2026-08-15 |

## 1. Entry gates

The task started on clean published branch
`feature/g8c-locus-v2-extended-intersections-design`. `HEAD`, local `main`,
`origin/main`, the branch upstream and the peeled `geocedg-g8b-pass` target all
resolved to `3531db7838426305c505c291b1d614aa6df5175c`; ahead/behind was `0/0`.
The remote refs were refreshed without merge, rebase, reset or history rewrite.

G8B reproduced before edits through its focused static/final-evidence gate:
49 test contracts and 16 Markdown authorities passed. No productive G8C source,
public command, Path, persistence, 3D or G9 implementation existed.

The repository contains no `.github/copilot-instructions.md`; the expected file
was reported rather than invented. Root `AGENTS.md`, canonical governance/
verification prompts and the task-specific authorities therefore governed.

## 2. Source audit findings

### Conics

`GeoConicND` exposes canonical conic type, degeneracy, symmetric matrix,
translation/eigen/axis data and direct evaluation. Ellipse, parabola and
hyperbola are coherent nondegenerate targets. Degenerate conics are a closed set
of materially different point/line/empty subtypes and should not pass through a
generic smooth adapter.

Raw quadratic values change under algebraically equivalent scaling. Both Java
probes and symbolic references show `G/||grad G||` cancels nonzero scale at
regular points and supports a typed first-order normal residual. It is not exact
distance. Existing `AlgoIntersectConics`/line/polynomial algorithms are useful
mathematical precedents but mix Classic output permutation, coordinate tolerance
or view-scale behavior unsuitable for G8 authority.

### Functions

`GeoFunction` exposes evaluation, derivatives and explicit interval metadata.
Unrestricted path parameter bounds can follow Euclidian view bounds; they are
forbidden coverage authority. The probe confirmed an explicit interval remains
stable while path bounds change with the view.

`y-f(x)` is a dimensionally meaningful vertical graph residual, not Euclidean
distance. Dividing by `sqrt(1+f'(x)^2)` yields a first-order normal estimate when
the derivative is finite. Poles and nonfinite evaluations are invalid-domain
barriers. A conditional probe also exposed upstream tolerance around a branch
boundary, reinforcing the requirement for explicit valid components and no
gap bridging.

### Implicit curves

`GeoImplicit` exposes coefficients, evaluation and gradient. `GeoImplicitCurve`
also represents nonpolynomial expressions, while factor/component data are not
a stable public adapter contract. Existing display/intersection paths may use
view bounds, sampled loci, coordinate deduplication or fixed tolerances and
cannot establish G8 completeness/identity.

The honest first subset is finite-coefficient polynomial curves at regular
roots. `G/||grad G||` is scale invariant there. At a cusp/singular root both
gradient components vanish; the regular residual is undefined, so residual-only
point promotion is forbidden. Nonpolynomial/general implicit support is
deferred.

### Locus V2 × Locus V2

One `LocusEvaluationSession2D` can coherently bind and evaluate two current
semantic locus revisions. `LocusInterval2D` rejects infinite endpoints; current
G6 authority therefore supports finite components and periodic fundamental
domains, not unbounded pair completeness.

Pair geometry needs rectangles in `(t,u)`, two-sided residuals, normalized
Jacobian/tangent evidence, branch/component products and source-order symmetry.
A small residual or converged Newton iteration is not local uniqueness. Matching
samples can only mark suspected overlap. A canonical unordered source pair plus
ordered reversible evidence preserves `A∩B=B∩A` without losing parameter
provenance.

## 3. Characterization execution

The test-private package contains:

- `G8CExtendedTargetCharacterizationTest`: 13 upstream authority, domain,
  normalization, singularity and capability tests;
- `G8CLocusLocusCharacterizationTest`: 13 dual-source session, symmetry,
  identity, isolation, tangency, overlap, periodicity and Option B tests;
- `G8CDesignFunctionalBenchmarkTest`: 6 deterministic pair-space,
  component-product, repeated-query and nested-depth tests;
- `G8CCharacterizationSupport`: test-only formulas/types/counters.

After one deliberate fixture correction prompted by the observed conditional
comparison tolerance, all **32/32** tests and `checkstyleTest` passed. The probes
retain zero state, report zero render/legacy/viewport/G7-index authority, and do
not contain a productive solver.

The independent generator uses CPython 3.12.13, mpmath 1.4.1 and 80 decimal
digits. It records analytic conic/function/implicit roots, singular limitations,
two-parametric transverse/tangent/circle cases, source-order symmetry,
reverse-parameter overlap and equal-coordinate constructive multiplicity.

## 4. Performance characterization

The pair-box probe visits exactly `n^2` rectangles for an `n × n` grid and
rejects most boxes in the crossing-line fixture. This demonstrates the explicit
quadratic combinatorial risk; it is not a solver endorsement. Component-pair
count is the exact Cartesian product of valid components.

At nesting depths 1, 2 and 3, ten pair queries execute exactly `20 * depth`
evaluator calls across two chains, with zero whole-locus regeneration and zero
render evaluation. Repeated 1/10/100 query probes are deterministic and retain
zero entries. These measurements support query-local-first state and do not
justify a shared owner/index.

## 5. Resulting design

### G8C1

Reuse the G8B one-dimensional solver/lifecycle through typed adapters for:

- nondegenerate ellipse, parabola and hyperbola;
- explicitly bounded real `GeoFunction` graphs with invalid-domain splitting;
- regular finite-coefficient polynomial implicit curves.

Each adapter separates candidate level from typed independent verification.

### G8C2

Use a separate dual-parameter solver over semantic component rectangles. Reuse
the G8B result, rich Geo, point consumer, completeness/Option B, token and
atomic lifecycle. Canonicalize geometric source identity as an unordered pair;
keep ordered `(t,u)` and rectangle data as revision evidence. Require local
coverage plus uniqueness; use normalized tangent determinant only within the
classification hierarchy. Represent overlap as established/suspected/
unsupported rich evidence, never samples.

## 6. Scientific findings

The versioned CeDG corpus confirms that locus-defined curves are constructive
intermediate results, LSIM curves can have multiple leaves and topology changes,
focal illumination crosses secant/tangent/empty regimes, and downstream
flattening/incidence consumes prior curve results. Historical nested/sampled
Locus behavior documents cost and authority limitations but supplies no G8C
numeric constants.

## 7. Author decisions

The complete decision table is in the G8C design plan. On 2026-08-15 the author:

1. approved the G8C1/G8C2 subdivision;
2. approved the typed G8C1 subsets and residual meanings as normative;
3. retained pair identity, local isolation, overlap and source symmetry as the
   proposed G8C2 contract rather than prematurely making them normative;
4. kept ADR 0009 Proposed and G8C2 blocked until G8C1 is author-approved;
5. retained query-local state; and
6. required both phases before global G8 closeout/G9 entry.

## 8. Scope audit

No productive G8C `src/main` file, command, public API, Path, XML/persistence,
legacy `GeoLocus`, Classic, 3D, G9, global cache or shared owner was added.
The current G8B contract and historical G8A evidence were not rewritten.

## 9. Disposition

```text
G8C DESIGN = PASS — AUTHOR APPROVED
G8C1 = AUTHORIZED — NOT STARTED
G8C2 = NOT AUTHORIZED — NOT STARTED
G8 = IN PROGRESS
G9 = NOT STARTED
```

# G8C target-family capability matrix

**Status: G8C1 rows implemented and author-approved; G8C2 rows proposed/not
implemented**

| Family | Actual kernel authority | Domain authority | Candidate/refinement capability | Verification residual | Derivative/bounds | First productive recommendation |
|---|---|---|---|---|---|---|
| Circle | G8B circle adapter | Finite locus components + circle | Existing analytic/numeric hierarchy | G8B normalized radial contract | Analytic | Already G8B; regression only |
| Ellipse | Nondegenerate `GeoConicND` canonical type/matrix/axes | Finite locus components; whole conic | Quadratic level; analytic conic capability where isolated safely | Regular first-order normal `G/||grad G||`; not exact distance | Analytic gradient; canonical axes/bounds | **G8C1 required** |
| Parabola | Nondegenerate `GeoConicND` | Finite locus components; whole conic | Quadratic level; unbounded target is acceptable because source domain controls search | Same regular typed residual | Analytic gradient/canonical data | **G8C1 required** |
| Hyperbola | Nondegenerate `GeoConicND` | Finite locus components; both conic branches | Quadratic level with branch-aware diagnostics | Same regular typed residual | Analytic gradient/canonical data | **G8C1 required** |
| Degenerate conic | Typed conic subtypes (point, lines, double/parallel lines, empty) | Subtype-dependent | Not one smooth-conic problem | Subtype-specific only | Degenerate gradient/axes | Defer; do not route through smooth adapter |
| Bounded polynomial function | `GeoFunction` + explicit interval | Explicit finite interval, split at invalid values | One-dimensional level `y-f(x)`; polynomial capability may strengthen completeness | Vertical model-length residual; optional first-order normal estimate | Derivative available when valid; semantic bounds must be computed, not view-derived | **G8C1 required** |
| Bounded trig/rational function | `GeoFunction` + explicit interval | Finite interval/components; poles/nonfinite values are barriers | Adaptive/derivative-aware with weaker coverage unless certified | Same typed function residual | Derivative may be valid locally | **G8C1 required**, truthful completeness |
| Piecewise/conditional function | `GeoFunction`, conditional expression | Explicit interval plus branch/discontinuity analysis | Evaluator-only unless branch metadata available | Same, only on valid side | Boundary semantics/tolerances require explicit diagnostics | Supported only when components/boundaries are established; otherwise unsupported |
| Unrestricted function | `GeoFunction`; path limits may use view | No finite semantic search domain | View-window search forbidden | N/A | N/A | Defer/unsupported |
| Regular polynomial implicit | `GeoImplicitCurve.getCoeff`, evaluation, derivatives | Whole target; finite source controls search | Polynomial level/gradient; analytic/certified capability where available | Regular first-order normal `G/||grad G||` | Gradient; no stable public factor/component contract | **G8C1 required typed subset** |
| Singular polynomial implicit | Polynomial authority but zero gradient | Source finite; singular target point/component | Needs higher-order/certified local analysis | Regular residual undefined | Gradient insufficient | Defer promotion; explicit unsupported/undetermined |
| Nonpolynomial implicit expression | Evaluation/derivatives but no polynomial coefficient authority | No closed normalization/component contract | Existing display/intersection paths may be view/sample dependent | No generally sound common normalization characterized | Local derivative only | Defer; do not call “general implicit” |
| Locus V2 × Locus V2, finite components | Two semantic evaluators/revisions/branches/components | Product of declared finite components | Parameter rectangles, semantic bounds, 2-D refinement | `||F(t)-Q(u)||` in model coordinates | Two tangents/Jacobian where available | **G8C2 required** |
| Periodic × periodic/bounded | Semantic fundamental domains and seam canonicalization | Canonical fundamental-domain product | Same with seam equivalence/dedup | Same | Tangents optional; seam evidence | **G8C2 required** |
| Any unbounded locus component | Current `LocusInterval2D` rejects infinite endpoints | Not representable | Arbitrary window/viewport forbidden | N/A | N/A | Unsupported pending separate G6 design |

## Guarantee interpretation

- **Exact/analytic**: formula or authoritative closed-form capability establishes
  the stated claim over the declared domain.
- **Certified**: interval/box or equivalent proof establishes isolation,
  exclusion, uniqueness, or coverage.
- **Estimated**: typed numerical error estimate with explicit assumptions.
- **Floating-point uncertified**: independently verified finite result but no
  exhaustive or proof-level claim.
- **Unsupported**: capability or normalization is absent; no fabricated result.

No row broadens the public API. “G8C1 required” is the author-approved internal
scope and is implemented by the author-approved internal kernel. The default adaptive
capability provides verified-but-uncertified local evidence and does not claim
global completeness. G8C2 rows remain proposed, not authorized and not
implemented.

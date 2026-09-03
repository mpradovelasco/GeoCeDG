# G9S1-R1 structural spline numerical-method clarification

- State: author-authorized prerequisite design; **IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**.
- `implementationComplete=true`; `manualAuthorSmoke=PENDING`;
  `selfApproved=false`; `authorApprovedPhase=false`; `passClaimed=false`.
  The earlier quintic and implicit tangency failures remain historical real
  regressions, not inherited rejections. Corrections A/B resolve both under the
  unchanged contract; fresh PHASE A/B, COMPOSED and FULL clean completed with exit 0.
- Bounded higher-precision construction and direct structural univariate
  certification are author authorized and technically validated, not author phase approved.
  The representation and original admission/proof standards remain.
- Successor to the preserved [D2 research record](g9s1_semantic_spline_numerical_methods.md),
  which is not edited or rehashed by this clarification.
- Decision: [ADR 0022](../adr/0022-structural-spline-continuity.md).
- Concrete equations and representation: [structural design](../architecture/g9s1_r1_structural_spline_continuity.md).
- Historical failure: [native-knot blocker](../validation/g9s1_r1_implementation_blocker_report.md).

## 1. Scholarly support and attribution

Carl de Boor, [B(asic)-Spline Basics](https://ftp.cs.wisc.edu/Approx/bsplbasic.pdf),
section 5, Theorem 5 and its explicit truncated-power basis (5.6), supplies the
standard piecewise-polynomial/spline-space characterization. Equations
(5.3)–(5.5) establish the preceding truncated-power membership relations.
With order d+1 and simple interior knots, the required smoothness
is C^(d-1). This supports the choice of representation, not the correctness of
GeoCeDG's specific interpolation boundary rows, numerical solver, periodic
elimination code or interval implementation. Source inspected 2026-09-03.

No theorem of unconditional interpolation solvability is imported. The following
dimension/equation mapping is a repository-specific derivation requiring direct
code and regression verification. No final scientific PASS is claimed here.

## 2. Open space and original defining equations

For N source points and m=N-2 simple interior knots, use one degree-d polynomial
and m hinges `(t-k_j)_+^d`. Each hinge and its derivatives through d-1 vanish at
activation, so continuity holds for every coefficient vector. Conversely,
successive degree-d derivative jumps recover the hinge coefficients uniquely
from any C^(d-1) piecewise degree-d spline. Removing those jumps leaves one global
polynomial. This gives a bijective representation of dimension `N+d-1`.

The original independent-span space has `(N-1)(d+1)` coefficients. Eliminating
its `(N-2)d` continuity constraints leaves the same `N+d-1` degrees of freedom.
The new system retains N interpolation equations and d-1 original boundary
conditions. Some inherited higher-degree boundary rows evaluate the extension
of a selected span polynomial, not the globally active piece at the evaluation
parameter. The reduced-basis row must therefore activate hinges by selected
span. Changing that convention would change the family.

The dimension count alone proves neither full rank nor good conditioning.
Scaled pivoting, original-equation backward residual, finite arithmetic and
admission/work checks remain independent obligations. Do not claim exact
interpolation or raise the existing residual tolerance to conceal a failure.

The high-degree characterization compares the current solve with a faithful
reconstruction of the published independent-span solve, including its original
backward-error guard. Both reject the exercised nonlinear open degree-12
sinusoidal inputs and the 17-point uniform exact-cubic input. For the latter,
exact rank 28 establishes nonsingularity of the reduced equations, not successful
finite-precision admission. The regressions retain those historical rejections
explicitly. They must not be recast as mathematical singularities or removed
merely to obtain a green result. The subsequently authorized bounded precision
fallback may recover a valid input if all original admission predicates pass;
continued numerical rejection is not itself a mathematical requirement.

Positive degree-12 coverage instead uses admitted open straight and nonlinear
periodic sources. A separately admitted nonlinear degree-7 source is checked
against an original-equation oracle at two higher precisions. Universal
forward agreement within 1e-8, or admission of every finite degree-12 input,
was an unsupported test assumption rather than inherited semantic authority.
The intermediate three-step numerical solve-refinement experiment was
unsuccessful and is not retained; bounded column equilibration preserves the
equations and the unchanged backward guard. This distinction concerns numerical
admission only: every admitted structural source still requires exact shared
jets and the independent rigorous interval bridge.

## 3. Periodic subspace is a separate construction

Impose equality of endpoint jets of orders 0 through d-1. For the normalized
fundamental interval, subtract the jets at 0 from those at 1 and eliminate
polynomial coefficients in descending derivative order. Each diagonal is a
nonzero integer r+1, so this is an exact triangular parameterization of the
periodic subspace, leaving `N-1` free coordinates. The complete recurrence and
common-denominator construction belong to the linked architecture, not a second
independently maintained formula here.

Binary64 input coordinates and knots denote exact dyadic inputs to the problem.
Free coefficients may be canonical binary64 values or the bounded retained
decimal values of the authorized precision fallback. Exact finite numerator
arithmetic over a common denominator
must retain the eliminated relations; decimal rounding with a MathContext may
not establish equality. Periodic interpolation is then a numerical solve inside
that exact subspace, followed by independent validation of the original problem.
The duplicate endpoint equation is redundant only for exactly equal finite
endpoints. Approximate geometric endpoint equality is insufficient.

This structural seam is distinct from monodromy of intersection labels and from
the retained R4 quarantine archive risk. Correct periodic source jets do not
prove a globally unambiguous pair selector or close that persistence risk.

## 4. Structural function, numerical evaluation and proof

Three layers must remain distinguishable:

| Layer | Authority and claim |
|---|---|
| Structural model | A real spline defined by numerical free coefficients plus exact shared relations; exact jet continuity of that represented function. |
| Ordinary evaluation/discovery | Approximate floating evaluation or expanded cache; reported numerical error and residual remain necessary. |
| Interval certificate | Outward enclosure of the structural coefficients/function/Jacobian and actual R5 composition; never an enclosure of a different rounded-cache function. |

A numerator/denominator coefficient enclosure must compare its candidate binary64
endpoint exactly with the represented rational and round outward. Interval
arithmetic and inclusion predicates then retain their usual existence/uniqueness
hypotheses. A small residual, a finite sample or nearly equal jet is not such a
proof. Legacy diagnostic independent spans receive no automatic structural flag.

## 5. Alternatives, performance and validation obligations

### Current bounded arithmetic/certification authorization

The current correction keeps structural continuity and the original spline
family. A bounded higher-precision solve addresses arithmetic construction,
not geometry or identity. Exact binary64 inputs are represented as their actual
values, with deterministic finite precision policy and unchanged original
admission. Stability of successive adequate representations is separate from
backward residual; neither is established by visual or Cartesian proximity.

The one-sided implicit correction is restricted to stored exact coefficient
authority of `REGULAR_POLYNOMIAL_IMPLICIT`, not radial circle expansions or
all polynomial adapters. It separates floating discovery from proof of
`Q(S(u))=0`. Outward enclosures of the actual structural composition and
its chain-rule derivative support rigorous exclusion and simple-root proofs.
A derivative enclosure containing zero cannot establish transversality. The
four false split candidates near two double contacts must not become roots
through small floating residual alone; genuine contacts remain truthful rich
evidence without invented multiplicity certification. The source-backed design
and new tests must demonstrate these obligations before a completion claim.

No new scholarly theorem or general multiplicity claim is asserted by this
bounded clarification. The existing interval inclusion/exclusion hypotheses
remain necessary; the architecture and actual source determine whether the
implemented enclosures satisfy them. Research design does not replace tests.

Direct reduced solving is selected because it avoids the independent-span
solve plus a separately justified projection. Shared-jet or equivalent bases
are possible only if they preserve the same equations and rigorous bridge.
A new general spline family or broad exact-linear-algebra dependency is outside
scope. At N=32,d=12 the open coordinate dimension is 43 versus 403 independent
span coefficients; this is a dimension calculation, not a measured speedup.
Record actual pivot/work counts, construction cost, interval enclosure cost and
pair coverage budgets before performance claims.

Required evidence includes the native failed-knot regression; all jets through
d-1; degree/size/weight/coordinate-scale controls; exact periodic closure;
near-knot distinct roots; both/one-source knot roots; R5 composition and negative
dilation; current-source certificate coherence; and historical Spline, metrics,
R6 interaction, persistence and Classic compatibility. Research-backed design
does not replace PHASE/COMPOSED/FULL saved execution evidence.

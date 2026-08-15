# G8C1 extended-target intersection traceability matrix

**Status: PASS — AUTHOR APPROVED**

| Requirement | Productive authority | Verification evidence |
|---|---|---|
| Closed ellipse/parabola/hyperbola support | `LocusIntersectionTargets2D`, `NondegenerateConicIntersectionTarget2D` | kernel tests: family support, secant, tangent, near tangent, transformed conic, parabola/hyperbola |
| Degenerate conics rejected | typed `IntersectionTargetSupport2D` | unsupported-subtype tests |
| Explicit finite real function domain | `BoundedFunctionGraphIntersectionTarget2D`, `IntersectionTargetDomain2D` | polynomial/trig, endpoint, pole, conditional-gap and dynamic-target tests |
| Regular finite polynomial implicit subset | `RegularPolynomialImplicitIntersectionTarget2D` | scale invariance, regular roots/components and singular rejection tests |
| Normalized residual meanings | typed `IntersectionResidualContract2D` and `TargetResidualEvaluation2D` | conic/implicit scaling and function vertical-residual tests; independent reference manifest |
| No sign-change-only tangency | `ExtendedTargetIntersectionCapability2D` local-minimum refinement and normalized contact | ellipse/function/implicit even-root tests |
| Completeness remains set-level | candidate capability returns `NOT_ESTABLISHED`; injected capabilities preserve stronger evidence | false-completeness and complete-empty tests |
| Option B preserved | existing rich result and token consumer | point defined from a locally established root with `NOT_ESTABLISHED` parent |
| Local isolation is solution-local | semantic bracket plus regular transverse evidence | unique-root admissibility, tangent non-isolation and close-root tests |
| Durable identity is not coordinate/order/parameter | existing token/continuation model plus explicit component-local keys | new-root, reordering, equal-coordinate-preimage, reparameterization tests |
| Atomic current publication | `AlgoLocusIntersectionV2` and existing rich Geo publication | exception, undefined and recovery tests |
| Normal DAG propagation | existing rich algorithm + token point consumer | source/target motion and downstream depth 1/2/3 tests |
| Query-local bounded work | extended capability, policy and instrumentation | 1/10/100 queries, 100 consumers and exact counter snapshot |
| Forbidden authorities remain zero | no render/sample/viewport/G7 dependencies | static verifier plus functional zero counters |
| Compatibility boundary | no dispatcher/Path/XML/legacy/Classic/3D/G9 edits | diff scope audit and composed verifier |
| Independent numerical provenance | versioned G8C reference generator and manifest | regeneration at CPython 3.12.13, mpmath 1.4.1, 80 dps |

The author approved D1–D6 on 2026-08-15. G8C2 requirements remain traceable
only to the proposed design package and ADR 0009. Nothing in this matrix claims
a two-parameter implementation.

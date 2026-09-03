# ADR 0022: Structural continuity of the semantic SplineV2 model

- Status: **Accepted bounded design — AUTHOR AUTHORIZED; IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW**
- Date: 2026-09-03
- Scope: prerequisite correction inside G9S1-R1, not a new phase or phase PASS
- Decision owner: GeoCeDG author
- Clarifies the representation boundary of [ADR 0018](0018-semantic-spline-2d-capability.md)
  and the proof hypotheses of [ADR 0021](0021-spline-pair-singleton-germ-materialization.md).
- `implementationAuthorized=true`; `selfApproved=false`;
  `authorApprovedPhase=false`; `passClaimed=false`.
- `implementationComplete=true`; `manualAuthorSmoke=PENDING`. Corrections A/B
  resolve the numerical-admission and false-transverse regressions under the
  unchanged contract. Fresh PHASE A/B, COMPOSED and FULL clean completed with
  exit 0; exact continuity or DEV alone is not sufficient acceptance evidence.
- `correctionAAuthorized=true`; `correctionBAuthorized=true`. The subsequent
  author decision authorizes the bounded arithmetic and one-sided certification
  corrections below; it grants neither phase PASS nor weaker acceptance.

## Context

The preserved [native-knot blocker](../validation/g9s1_r1_implementation_blocker_report.md)
demonstrated that independent rounded power spans need not define an exactly
continuous real function: the mandatory native cubic had unequal exact limits
at its shared knot. A tolerance check or canonical right ownership cannot supply
the missing continuity hypothesis for interval certification. That failure and
its saved evidence remain historical authority; they are not rewritten as PASS.

The author now authorizes correcting the semantic representation, then completing
the unchanged bounded pair-materialization contract. This is not authorization
to relax local proof, introduce a new interpolation family, change Classic
Spline, or promote unrestricted pair-sheet materialization.

## Decision

1. Represent the same degree-d, simple-knot spline space through structural
   truncated powers, or an exactly equivalent shared-coefficient representation.
   Native simple interior knots have C^(d-1) continuity by construction, not by
   comparing independently rounded limits.
2. Select the direct reduced-basis solve. Preserve the existing source-backed
   interpolation and boundary equations, including rows that evaluate a chosen
   span's polynomial extension outside that span. Do not silently replace these
   with a conventional natural or not-a-knot family.
3. Establish closed periodic jets separately by exact structural elimination.
   The open truncated-power representation alone does not establish the seam.
   Require exact finite endpoint agreement for a source classified as closed;
   approximate endpoint coincidence must not become exact periodic C0.
4. Keep numerical free coefficients and interpolation residuals truthfully
   numerical. Structural jet equality is exact for the represented function;
   interpolation arithmetic, evaluated coordinates and length are not thereby
   exact. Retain existing scaled-pivot, finiteness and backward-error checks.
5. Retain the structural function as authority. Rounded expanded span arrays
   may remain derived evaluation/discovery accelerators, but the interval bridge
   must enclose the structural coefficients and their actual composition.
   A certifier must not prove a different rounded-cache polynomial instead.
6. Use the bounded exact numerator/common-denominator representation described
   in the [implementation design](../architecture/g9s1_r1_structural_spline_continuity.md).
   Exact periodic elimination and outward enclosures require explicit tests;
   any unavailable proof, nonfinite result or work exhaustion fails closed.
7. Preserve input count, degree admission, weights, canonical domain/orientation,
   right-owned internal knots, source dependencies, durable object identity and
   normal reconstruction. Version derived model/capability signatures when their
   meaning changes. Do not serialize coefficients, interval boxes or render data.
8. Keep the historical independent-span constructor only for explicit diagnostic
   fixtures, with no inferred structural continuity. Exact diagnostic spans
   still require their own real jet equality.

## Mathematical and compatibility boundary

### Subsequent bounded corrective authorization

The structurally continuous representation is retained. The later historical
regression run exposed two independent failures: a formerly admitted 25-point
quintic was numerically rejected, and a structural spline against an implicit
polynomial published spurious apparently transverse roots near two double
contacts. Those failed runs and assertions remain historical evidence.

Correction A may add deterministic bounded higher-precision construction using
the exact binary64 input values and the same defining equations. Keep the
binary64 fast path when it passes the original admission checks; fallback must
have explicit precision/work caps and may not raise residual tolerances. Prefer
canonical binary64 free coefficients if they pass unchanged admission. Retained
higher-precision derived authority requires demonstrated need and must still
reconstruct from command inputs without coefficient serialization.

Correction B is limited to `REGULAR_POLYNOMIAL_IMPLICIT` stored coefficient
authority; derived radial-circle polynomial expansions and historical
line/conic/circle paths are not silently promoted. It must certify the actual
univariate structural composition Q(S(u))
and its derivative before root classification and ledger publication. Floating
composed polynomials supply discovery only. Outward exclusion is not a root;
simple/transverse publication requires rigorous local existence/uniqueness and
a derivative enclosure excluding zero. Unknown or multiple contacts remain
truthfully rich-only. Do not alter multiplicity, hide false roots in the ledger,
or broaden this bounded spline/implicit-target seam to generic LocusV2.

The [structural design](../architecture/g9s1_r1_structural_spline_continuity.md)
owns the implemented contract; the [matrix](../validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
retains all prior failures and adds the corrective perimeter. Author approval of
this scope is not itself execution evidence; the completed corrective gates are
recorded in the [current report](../validation/g9s1_r1_structural_implementation_candidate_report.md).
Technical completion does not grant author phase approval.

The [source-backed research note](../research/g9s1_r1_structural_spline_numerics.md)
separates standard spline-space facts from the repository-specific boundary
mapping and exact periodic elimination. Space equivalence does not imply that
every admitted input produces a nonsingular or well-conditioned interpolation
system. The implementation must validate the original equations and reject
unsupported numerical states without raising tolerances or hiding regressions.

The native model remains a derived immutable snapshot of the same SplineV2
command inputs. Existing files reconstruct that snapshot; model correction alone
does not allocate a new durable locus ID. Current intersection certificates are
revalidated. Their proof signatures may change; durable structural selectors do
not acquire coefficient, knot, box or revision fields.

## Rejected alternatives

| Alternative | Reason |
|---|---|
| Epsilon-based C0/C1 gluing or nearby-root merging | Does not establish equality of the represented function or unique semantic identity. |
| Certify an ideal reference instead of the native source | Proves a different object. |
| Round structural spans and make only the rounded cache authoritative | Reintroduces the original discontinuity at the proof boundary. |
| Post-solve projection of independent spans | Requires an additional projection definition and geometry-displacement analysis; not selected over the direct equivalent basis. |
| General NURBS/B-spline subsystem or changed boundary family | Unnecessary scope and potentially different product semantics. |
| Open construction plus approximately matched periodic endpoints | Does not establish exact periodic jets. |

## Required evidence and stop boundary

The [R1 validation matrix](../validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
adds structural degree/size/scale/knot cases and separate periodic seam cases,
while retaining all original pair identity, coverage, lifecycle and persistence
requirements. Preserve the failing predecessor diagnostic; the corresponding
native positive must pass through the corrected source, not be removed.

PHASE A/B, COMPOSED and clean-output FULL remain mandatory. No test success is
inferred from this decision. Failure of mandatory periodic structure, family
equivalence, interpolation validity, certificate or historical regression is a
scientific blocker requiring explicit disposition. G9U1 remains unimplemented;
the independent `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` risk stays OPEN / TRACKED.

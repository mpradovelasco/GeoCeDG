# Conversion and approximation of DXF `SPLINE` references — `dxf-spline-approximation/v1`

| Field | Value |
|---|---|
| Status | **CANDIDATE — READY FOR AUTHOR ADOPTION (revision R1).** Normative design written by `PRE-G9B-MAINT-A-N2` and refined by `PRE-G9B-MAINT-A-N2-R1`. `selfApproved=false`. It authorizes no code: `MAINT-A-DXF1-S0`, `MAINT-A-DXF1` and `MAINT-A-DXF1b` are not authorized |
| Revision | R1 (2026-11-25): no persisted record of failed attempts and no store derivation entries (D-R1-5); certified bound separated in `MAINT-A-DXF1b` (D-R1-9); mathematical justification §5.9; maturity without a novelty flag (D-R1-10). The A-N2 text is commit `874286e95` |
| Contract identifier | `dxf-spline-approximation/v1`; methods `exact-conversion/v1` and `bspline-param-lsq/v1`; provenance block `approximationProvenance version="1"` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` (second wave published) |
| Architectural layer | Pure mathematical library (`org.geocedg.common.math.bspline`) for the computation; a creation service outside the kernel packages; additive GeoCeDG code in `org.geocedg.common.kernel.bspline` only where `bspline-2d.md` §10.5 reserves it (AGENTS §4) |
| Contracts it concretes | [`dxf-spline-reference.md`](dxf-spline-reference.md) §14 and §14.1; [`bspline-2d.md`](../curves/bspline-2d.md) §10.5; [ADR 0043](../../../docs/adr/0043-explicit-2d-bspline-family-rational-ready.md) decision 11 |
| Companion candidates | [reference store `cedg-reference-store/v1`](reference-store.md); [reference DXF export `cedg-reference-export/v1`](../export/dxf-reference-export.md); [candidate amendments](../../../docs/architecture/pre_g9b_maint_a_n2_candidate_amendments.md); [validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md); [characterization report](../../../docs/validation/pre_g9b_maint_a_n2_characterization_report.md) |
| Author directions | D-A-8, D-A-9, D-A-10 (P0 decision sheet §0); D-A-N2-1, D-A-N2-4, D-A-N2-5 (A-N2 instruction); D-R1-1, D-R1-5, D-R1-9, D-R1-10 (R1 instruction); approval-record observation AP-2 |

## 0. Status and boundary

This contract defines the explicit operation that creates **a new construction** of the
family `bspline-2d/v1` from one imported reference record `dxf-spline-reference/v1` kept in
the document store `cedg-reference-store/v1`: by exact conversion when the data allow it,
or by a declared polynomial fit otherwise. It changes neither the family, its evaluation,
its guarantees (`bspline-2d.md` §15), nor its XML beyond the grammar of the passive block
that `bspline-2d.md` §10.5 reserved to this contract. Native NURBS (`MAINT-A-R`,
`bspline-2d/v2`) is out of scope: the produced construction is always polynomial with
implicit unit weights.

Three entities stay distinct (D-A-N2-1): the **reference** (external data and provenance,
no CeDG authority), the **construction** produced here (a CeDG object with its own
definition, identity and dependencies), and any **render or export** of either (derived,
no geometric authority). The operation never turns a reference into a `GeoElement`, a
`GeoLocusV2` or a DAG participant, and the new construction never depends on the
reference.

## 1. Scope

| Included | Excluded |
|---|---|
| exact conversion of compatible polynomial definitions (§4) | weights in the produced construction; native NURBS (`MAINT-A-R`) |
| parametric least-squares fit of rational references and of polynomial references outside the family limits (§5) | dynamic mode (refit when the reference changes); `R-d` |
| explicit tolerance, QR solver, point and degree limits, adaptive refinement, work limits, typed failures (§5, §8, §9) | non-parametric correspondence (Hausdorff fitting, Möbius reparametrization) |
| sample residual and held-out numerical estimate (§6); certified global bound designed here and implemented by `MAINT-A-DXF1b` (§7, D-R1-9) | any measure attributed to the reference (length, tangent, intersection) |
| provenance grammar and applicability (§10, §11) | the export disposition, fixed by `cedg-reference-export/v1` |
| numeric persistence of the inputs, bit for bit (§12) | changes of the kernel's general XML number format |

## 2. Code facts at the base

The facts below were verified on `a9ee803bf` (characterization report §4). They are the
reason for the rules of §10 to §12, not a second copy of the code.

1. `BSplineContractXml` reads the optional `approximationProvenance` child, requires
   `version="1"` (`UNSUPPORTED_PROVENANCE_VERSION` otherwise) and keeps its attributes as an
   ordered list of strings without interpreting them; `PersistedContract` offers no public
   factory that attaches a new block: `MAINT-A2` never writes one.
2. `AlgoBSpline` keeps the read block on a compatible redefinition and drops it when the
   algorithm is replaced.
3. `BSplineDefinition2D` exposes the digest of the current definition (`bspline-2d.md`
   §12.3) for the applicability rule; `MAINT-A2` computes no applicability (BS-24 limited).
4. `BSplineData.polynomialEquivalent` returns the same `p`, `U` and control points with
   implicit weights when all weights are exactly equal.
5. `BSplineExactRational` is an exact rational on `BigInteger`, package-private, converts
   binary64 values without rounding and rounds once.
6. The numeric serialization facts that decide §12 are recorded in the characterization
   report §4 and summarized in §12.1: every finite binary64 value except `-0.0` round-trips
   bit for bit on the routes a `BSpline` uses; the 15-significant-digit path exists only for
   exact-decimal numbers in a list without definition.
7. A compatible redefinition through the replacement route (for example retyping
   `BSpline(L, p, K)` with new arguments) builds a new algorithm and loses the provenance
   block, contrary to `bspline-2d.md` §10.5. Moving a control point keeps it; in the probe
   the ordinary redefinition route refused the change, so its behavior with an accepted
   compatible redefinition is **not verified** (NUM-6). No document holds a block today
   (`MAINT-A2` never writes one), so the defect is latent (§13.1).

## 3. Operation and inputs

- The operation is invoked only by an explicit user action on **one** reference of the
  store, never through construction-tool selection, dragging or proximity
  (`dxf-spline-reference.md` §14).
- The reference is identified by its store identifier `referenceId`
  (`cedg-reference-store/v1` §6), never by session index, label, layer, handle,
  coordinates or order. Without the store (session-only `R-a`) the operation is not
  offered: a construction whose provenance names a reference that cannot survive a
  reopen would make the correspondence of `cedg-reference-export/v1` unresolvable
  (decision AX-D1). The pure library part of the method does not depend on the store and
  may be developed before it.
- It reads only the record's `data`, effective weights, `nature`, domain, closure and
  provenance digests. Neither the record (`dxf-spline-reference.md` §5) nor the store is
  modified by the operation, whether it succeeds or fails (§13.2).
- User parameters for an approximation: degree `q`, tolerance `ε`, point limit `N_A`
  (§8).

## 4. Exact conversion — `exact-conversion/v1`

### 4.1 Criterion

Decided by exact binary64 comparison, in this order:

1. `nature ∈ {POLYNOMIAL, RATIONAL_EQUAL_WEIGHTS}` (`dxf-spline-reference.md` §3);
2. the transformed data satisfy the family preconditions of `bspline-2d.md` §4.1 and §7:
   `1 ≤ p ≤ 7`, `p + 1 ≤ n + 1 ≤ 128`, clamped ends, interior multiplicity `≤ p`,
   non-empty domain, finite values. With the import predicates admitted, only
   `n + 1 > 128` can fail.

A reference that meets 1 but fails 2 is **non-convertible**. It is offered for approximation
only when the initial knot vector of §5.4 fits in `N_A`; with the source knots kept, that
vector needs at least `n − p + q + 1` control points for simple source knots, so for
`q ≥ p` and `n + 1 > 128` it never fits; a lower degree `q < p` may fit when `n − p + q + 1 ≤ N_A`. A reference for which no admissible `q` fits is neither convertible
nor approximable in `v1`: it stays in the store and stays exportable (D-A-N2-4; decision
AX-D11).
A `RATIONAL` reference is never convertible in `v1`, even when a Möbius
reparametrization would make it polynomial: that equivalence changes the parametric
correspondence and is out of scope.

### 4.2 Result

`BSpline(L, p, K)` in the explicit form, with the same `p`, the same knots and the same
transformed control points, bit for bit (binary64 patterns) except the sign of zero, and
`knots="explicit"`. For `RATIONAL_EQUAL_WEIGHTS` the common weight is dropped
(`polynomialEquivalent`); weights are never normalized otherwise.

**Signed zero.** The kernel's exact writers serialize `-0.0` as `0` (§12.1). Every `-0.0` of
the converted data is therefore replaced by `+0.0` **before** creation (decision AX-D12); the
two are equal under binary64 `==` and define the same curve, so the conversion stays exact in
the sense of §4.3, and `definitionDigest` is taken from the created object. The replacement
is reported in **Details…** when it happened.

### 4.3 What «exact» means

The construction's binary64 data equal the record's `data` under binary64 `==` (bit for bit
except the sign of zero, §4.2), so the curve they define is mathematically the curve defined
by the record's `data` (for equal weights the common weight cancels in exact arithmetic,
`bspline-2d.md` §3.2). It does **not** mean equality with the decimal text of the file
(reading rounding, import §4.5) nor with the untransformed data unless the transformation
is the identity (transformation rounding, import §6). Floating evaluations of the record
(homogeneous) and of the construction (polynomial) may differ by rounding although the
curve is the same; no evaluation is compared to decide exactness.

The word «exact» is published only while §12 holds: if the persisted definition could
differ from the converted data after save, reopen, Undo/Redo or copy, the conversion is not
exact and the operation is not offered (stop condition §19.1).

### 4.4 Guarantee

Error 0 on the data by definition; no measure is computed or published; the operation is
never presented as a fit. A convertible reference is not offered for approximation in
`v1` (decision AX-D8).

## 5. Approximation — `bspline-param-lsq/v1`

Applies to `RATIONAL` references and to non-convertible `POLYNOMIAL` or
`RATIONAL_EQUAL_WEIGHTS` references. Let `C` be the curve of the record (`data`, effective
weights `w`, degree `p`, knots `U`, domain `[a, b]`, orientation of increasing `u`) and `A`
the polynomial B-spline of degree `q`, knots `T` and control points `Q_0..Q_m`.

### 5.1 Parametric correspondence

`A(u) ≈ C(u)` in the **same** parameter `u` and on the same domain `[a, b]` (D-A-8). The
domain is never normalized, reversed or trimmed; the orientation is preserved. Because the
correspondence is parametric, a bound on `sup_u ‖C(u) − A(u)‖` bounds the Hausdorff
distance between the images and nothing else (import §14.1).

### 5.2 Endpoints and closure

`Q_0 = P_0` and `Q_m = P_n`, copied from `data`, never evaluated (`C(a) = P_0` and
`C(b) = P_n` by clamping and positive weights). When the record is exactly closed, `Q_0`
and `Q_m` are the same binary64 pair and the approximation is exactly closed `C^0`
(`bspline-2d.md` §4.3); no higher seam continuity is claimed or imposed.

### 5.3 Degree

`q` is chosen by the user in `1..7`; the declared default is `3` (decision AX-D5). The
degree never changes during the computation.

### 5.4 Initial knot vector

Clamped ends of multiplicity `q + 1` at `a` and `b`. Each distinct interior knot value of
`U` with multiplicity `k` (continuity `c = p − k`) enters with multiplicity
`m_T = max(1, q − c)`, so that `A` has structural continuity `C^{min(c, q−1)}` there: it can
reproduce a corner of the source and imposes no more smoothness than the source has. No
source knot is dropped in `v1`; if the initial vector already needs more than `N_A` control
points the operation fails with `INITIAL_KNOTS_EXCEED_POINT_LIMIT` before any fit, and the
user interface does not offer «Approximate…» for a reference where this is already known
for every admissible `q` (§4.1). A knot-reduction strategy would be a later method version.

### 5.5 Sample design

The common partition is the ordered union of the distinct knot values of `U` and `T` in
`[a, b]`. On each common span `[α, β]` with `α < β`, the fit set holds
`K_FIT(q) = 2q + 2` interior samples `u_i = α + (β − α)·(i / (K_FIT + 1))`,
`i = 1..K_FIT`, computed in that order of operations. If a sample fails `α < u_i < β` or
two samples coincide, the operation fails with `SAMPLE_DESIGN_DEGENERATE`. `a` and `b` are
not samples (fixed endpoints). Every span of `A` is a union of common spans, so it holds at
least `q + 1` distinct interior samples: the Schoenberg–Whitney condition holds and the
least-squares matrix has full column rank **in exact arithmetic**. The numerical rank is
checked separately (§5.6).

### 5.6 Least squares by QR

- Unknowns `Q_1..Q_{m−1}`; rows are the samples in increasing `u`; columns are basis
  indices in increasing order; two right-hand sides (`x`, `y`):
  `C(u_i) − N_{0,q}(u_i) Q_0 − N_{m,q}(u_i) Q_m`. Unit row weights.
- Solver: Householder QR **without pivoting**, fixed column order, reflector
  `v = x + s‖x‖e_1` with `s = +1` when `x_1 ≥ 0` and `s = −1` otherwise, back
  substitution. Implemented in the pure library (decision AX-D7). The normal equations are
  never formed: they square the condition number.
- Numerical rank guard: if some `|R_jj| ≤ ρ_rank · max_i |R_ii|`, the operation fails with
  `RANK_DEFICIENT`. A rank-deficient or near-singular system is never «repaired» by
  dropping columns or regularizing in `v1`: either would change the represented knot
  structure silently.
- Conditioning: the ratio `κ̂ = max_i |R_ii| / min_i |R_ii|` (a lower estimate of the
  2-norm condition number) is reported in the advanced report as a diagnostic, never as a
  guarantee. The B-spline basis is well conditioned for a fixed degree, and the samples of
  §5.5 sit at fixed relative positions in every span of `T` (every distinct source knot is a
  knot of `T`, so the common spans are exactly the spans of `T`): the conditioning depends on
  `q` and on the relative lengths of neighboring spans, not on the absolute knot values or
  their offset from the origin. Ill conditioning is not expected from the design;
  a locally degenerate distribution (spans of a few ulps, where the sample computation
  itself degenerates) is caught by the guards of §5.5 and §5.7 and by the rank guard.
- Every sample evaluation of `C` uses the homogeneous de Boor algorithm of the library;
  the basis values use the library's span lookup and Cox–de Boor recurrence; no FMA.

### 5.7 Adaptive refinement

After each fit, the per-span indicator of `A` is `e_s = max(residual over the fit samples
of the span, maximum of e over the refinement grid of the span)`. The **refinement grid**
holds, on each common span, `K_REF(q) = 4q + 4` points `g_j = α + (β − α)·((j − 1/2) / K_REF)`;
it is disjoint from the fit samples (`(2j − 1)(2q + 3)` is odd, `2i(4q + 4)` even) and from
the verification set of §6.2 (`2(2j − 1)` is even, `2l − 1` odd), and a binary64 coincidence
is skipped and counted. The verification set is **never** used here. All spans with
`e_s > ε` are bisected in the same round, in increasing order, at
`t = α + 0.5·(β − α)`; if `t` fails `α < t < β` the operation fails with
`SPAN_NOT_SPLITTABLE`. Each new knot enters with multiplicity 1. The next fit starts from
scratch (no warm start), so the result depends only on the knot vector.

### 5.8 Stop rules

| Rule | Outcome |
|---|---|
| every `e_s ≤ ε`, then the held-out `errorEstimate` of §6.2 is `≤ ε` (and `residualMax ≤ ε`) | success |
| every `e_s ≤ ε` but the held-out `errorEstimate > ε` | `VERIFICATION_NOT_PASSED` (the verification set is not fed back into refinement) |
| the next round would exceed `N_A` control points | `TOLERANCE_NOT_REACHED_POINT_LIMIT` |
| `R_MAX` rounds completed | `TOLERANCE_NOT_REACHED_ROUND_LIMIT` |
| the global indicator has not strictly decreased for `STAG` consecutive rounds | `STAGNATION` |
| `W_EVAL_MAX` evaluations of `C` or `A` consumed | `WORK_LIMIT_EXHAUSTED` |
| user cancellation | `CANCELLED` |

Order of evaluation, so that the typed cause is deterministic: cancellation and the work limit
are checked at every evaluation and end the operation at once (`CANCELLED` before
`WORK_LIMIT_EXHAUSTED`); after each fit the rows above are tested from top to bottom in this
order: success, `VERIFICATION_NOT_PASSED`, `STAGNATION`, `TOLERANCE_NOT_REACHED_ROUND_LIMIT`,
`TOLERANCE_NOT_REACHED_POINT_LIMIT`.

Every failure reports the best global indicator reached and its number of control points;
nothing else of the failed computation survives (§9).

### 5.9 Mathematical justification

Each statement below is marked **proved** (an argument in exact arithmetic, given here),
**design rule** (a deterministic choice, not a theorem) or **pending validation** (to be
measured by `MAINT-A-DXF1-S0` / `MAINT-A-DXF1`). Nothing marked pending is claimed.

1. **Full column rank in exact arithmetic (proved).** Unknowns are the coefficients of
   `N_{1,q} .. N_{m−1,q}` on `T`. A matrix with rows `(N_{j,q}(u_i))_j` over increasing
   samples has full column rank iff there are samples `s_1 < … < s_{m−1}` with
   `N_{j,q}(s_j) ≠ 0` (Schoenberg–Whitney). Supports `(t_j, t_{j+q+1})` are intervals with
   nondecreasing ends, so it suffices (Hall's condition for interval families, which then
   admits an increasing assignment) that every block of consecutive functions `j..k` has at
   least `k − j + 1` distinct samples in the union of its supports. That union contains `s ≥ 1`
   non-degenerate spans of `T`; the functions supported inside `s` spans number at most
   `(knots in the closed range) − q − 1 ≤ ((s − 1)q + 2(q + 1)) − q − 1 = sq + 1`, using
   interior multiplicity `≤ q` (§5.4 gives `m_T ≤ q`) and clamped ends `q + 1`; each span
   holds at least `2q + 2` interior samples (§5.5, every span of `T` being a union of common
   spans), so the union holds at least `s(2q + 2) ≥ sq + 1` samples. Consecutive blocks are
   enough: because the support ends are nondecreasing in the index, any sub-family whose
   supports form a connected union covers the same interval as the consecutive block between
   its lowest and highest index, which has at least as many functions. Hence full rank. Samples
   are strictly interior to spans, so a sample never sits on a knot and no basis value is an
   ownership artefact.
2. **Fixed endpoints (proved).** With clamped `T`, `A(a) = Q_0` and `A(b) = Q_m` exactly in
   exact arithmetic; `C(a) = P_0`, `C(b) = P_n` for clamped data with positive weights. Fixing
   `Q_0 = P_0`, `Q_m = P_n` removes the two end columns and interpolates the ends exactly; it
   does not change the rank argument of item 1. When `m − 1 = 0` (degree 1, no interior knot)
   no system is solved and `A` is the segment `P_0 P_n`.
3. **Closure (proved, structural).** An exactly closed source gives the same binary64 pair
   for `Q_0` and `Q_m`, so `A` is exactly closed `C^0` (`bspline-2d.md` §4.3). No seam tangent
   or curvature continuity is imposed or claimed; a source smooth at the seam may give an
   approximation with a seam corner of size governed by the measured error.
4. **Near multiple knots (proved, structural; error behavior pending validation).** Every
   distinct source knot is a knot of `T` with multiplicity `max(1, q − c)`, so `A` has
   continuity `C^{min(c, q−1)}` there and can follow a source corner; samples exist on both
   sides because each adjacent common span carries its own samples. Whether the error near a
   corner converges within `R_MAX` rounds is pending validation (AP-2b).
5. **Numerical rank criterion (design rule).** Householder QR without pivoting; rejection when
   `|R_jj| ≤ ρ_rank · max_i |R_ii|`. The ratio of diagonal entries is a lower estimate of the
   2-norm condition number, not a bound; the threshold `ρ_rank = 2^-40` is a design rule
   pending validation. A rejected system is never repaired by regularization, column
   dropping, degree reduction or reparametrization.
6. **Conditioning (pending validation).** The B-spline basis of fixed degree is stable (its
   condition is bounded by a constant depending only on `q`), and the samples sit at fixed
   relative positions in each span of `T`; the condition of the least-squares matrix
   therefore does not depend on the absolute knot values or their offset. A quantitative
   bound in terms of `q` and of the ratios of neighboring span lengths is not claimed;
   AP-11 measures `κ̂` on representative and adversarial distributions.
7. **Determinism (proved for the implementation contract).** Java `double` arithmetic is
   IEEE 754 binary64 with round-to-nearest in every operation (strict since Java 17);
   `Math.sqrt` is correctly rounded; the operation order (samples, rows, columns,
   reflectors, back substitution, bisections, golden-section steps) is fixed by this
   contract; no FMA, parallelism or library call with unspecified rounding is used. Two runs
   on the same data and parameters therefore produce identical bits on any conforming JVM.
   Serialized decimal text written with `Double.toString` (knot lists, DXF) is identical for a
   given Java runtime version; the shortest representation is guaranteed only from JDK 19, so
   byte identity of such text is claimed per runtime (the product's packaged runtime), while
   the binary64 values round-trip on every version.
8. **Measures (design rule).** The three sets are disjoint by the odd/even arguments of §5.7
   and §6.2, and the binary64 skip rule. No ordering between their maxima is a theorem, and
   none is claimed (§6.3).

## 6. Measures and their grades

All measures are computed on `C` (the record's `data`) and `A` (the binary64 data of the
construction). The reading and transformation roundings of the import are outside every
measure and are declared separately. No measure is attributed to the reference. The pointwise
error is `e(u) = sqrt(dx·dx + dy·dy)` with `Math.sqrt` (correctly rounded), `dx`, `dy`
the binary64 differences of the two floating evaluations.

### 6.1 Sample residual — grade `SAMPLE_RESIDUAL`

`residualMax` and `residualRms = sqrt(Σ e(u_i)² / K)`, `K` the number of fit samples (sum in
increasing `u`), over the fit samples of §5.5. A fact about those samples only; it says
nothing between them.

### 6.2 Held-out numerical estimate — grade `ESTIMATED`

The verification set is distinct and denser (import §14.1) and **held out**: it is disjoint
from the fit samples and from the refinement grid, and it is evaluated only once, on the
final candidate, so that neither the fit nor the knot refinement was chosen to pass it:

1. On each common span `[α, β]`, `K_VER(q) = 8q + 8` points
   `v_j = α + (β − α)·((j − 1/2) / K_VER)`, `j = 1..K_VER`. In exact arithmetic these never
   coincide with a fit sample (`(2j − 1)(2q + 3) ≠ 8i(2q + 2)`, odd against even); a point
   whose binary64 value equals a fit sample, a refinement-grid point or a knot is skipped
   and counted.
2. At every local maximum of `e` on that grid, a golden-section search with 40 fixed
   iterations in the bracket of its two neighbors; every evaluated point joins the set
   unless it equals a fit sample or a refinement-grid point.
3. `errorEstimate` is the maximum of `e` over the set. It is an estimate, not a bound.

### 6.3 Relations between measures

- No order is asserted between `residualMax` and `errorEstimate`: they are maxima over
  disjoint sets and no mathematical relation links them (approval-record observation AP-2).
  Success requires **both** to be `≤ ε` (§5.8); this needs no ordering claim.
- The alternative of including the fit samples in the verification set would make
  `residualMax ≤ errorEstimate` true by construction, but it contradicts the distinct set
  required by import §14.1 and would let the estimate repeat the fit; it is rejected, as is
  refining on the verification set, which would make it a fitted set rather than a
  held-out one (decision AX-D3).
- A certified bound `B` (§7) bounds the exact error. Floating values of `e` may exceed `B`
  by their own evaluation error; tests compare `e ≤ B + δ_eval` with the criteria of the
  validation matrix, never `e ≤ B` strictly.

### 6.4 Acceptance and publication

Success publishes `ESTIMATED` as the operative grade: «error `≤ ε` estimated on a held-out
verification set». With a certified bound (§7), the grade becomes `CERTIFIED` only when
`B ≤ ε`; otherwise the report states «certified bound `B` > `ε`: tolerance met only as an
estimate». A high-precision evaluation (for example `BigDecimal`) is a **test reference**,
never a certification.

## 7. Certified global bound — design and staging

### 7.1 Feasibility of import §14.1

The procedure of import §14.1 is mathematically sound for this contract:

- `C − A = D / W` with `D = 𝒜 − W·A`, `𝒜 = Σ N_{i,p} w_i P_i`, `W = Σ N_{i,p} w_i`.
- Positive denominators: every effective weight is finite and `> 0` (import predicate 12
  or implicit unit weights), the basis is non-negative with partition of unity, so
  `W(u) ∈ [min w, max w]` on `[a, b]`. After knot insertion and subdivision, the Bernstein
  coefficients `ω_k` of `W` on a span are convex combinations of the weights, hence
  `min_k ω_k ≥ min_i w_i > 0`; either value is a valid lower bound of `W` on that span.
- On a common span, `D` is a vector polynomial of degree `≤ p + q`; its Bernstein
  coefficients `d_k` satisfy `‖D(u)‖ ≤ max_k ‖d_k‖` (convex hull).
- Hence `sup_{[a,b]} ‖C − A‖ ≤ max_spans (max_k ‖d_k‖ / min_k ω_k)`.

The bound is invalid if any of these quantities is computed in rounded binary64. Two
arithmetic models are admissible: (a) **exact rational arithmetic** on the binary64 data
(every binary64 value is a dyadic rational; knot insertion, degree elevation, Bernstein
products and comparisons are field operations), which is outward-rounded interval
arithmetic with zero-width intervals and therefore satisfies import §14.1 step 2; (b)
outward-rounded intervals. The square root is not a field operation: the published value
is the smallest binary64 `B` with `B·B ≥ max_s B_s²` **checked exactly**, never a rounded
`Math.sqrt` alone. The bound concerns the exact curves of the binary64 `data` and `Q` as
persisted; it says nothing about the decimal text of the file, nor about floating
evaluation of `A` by the kernel.

### 7.2 Staging (author decision D-R1-9)

The computation is feasible but its cost (rational growth through knot insertion of up to
1 024 source control points and products of degree `p + q ≤ 14`) is unmeasured and its
validation needs independent references. Fixed by the author: `MAINT-A-DXF1` publishes
`SAMPLE_RESIDUAL` and `ESTIMATED` only, with their declared grades and never presented as
rigorous bounds, and writes `boundStatus="NOT_COMPUTED"`;
`MAINT-A-DXF1b` implements §7.1 model (a) with a declared work budget, tightening by exact
de Casteljau subdivision at rational midpoints up to `B_DEPTH_MAX` levels, publishing the
last valid bound when the budget is exhausted, or `BOUND_NOT_COMPUTED_WORK_LIMIT` when no
bound was completed. The grammar of §10 reserves the attributes now, so `MAINT-A-DXF1b`
needs no new provenance version.

## 8. Tolerance, limits and constants (candidates)

| Constant | Candidate value | Rule |
|---|---|---|
| `q` | `1..7`, default 3 | family limits; `DEGREE_OUT_OF_RANGE` |
| `N_A` | `q + 1 .. 128`, default 128 | never above the family limit; `POINT_LIMIT_INVALID` |
| `ε` | explicit, finite, `> 0`, in model units of the document; the dialog proposes `max(10^-6 · diag(bbox(control polygon of data)), 2 · ε_floor)`, with a notice when the floor decides; editable, never applied silently | `TOLERANCE_INVALID` |
| `ε_floor` | `2^-40 · S · max(1, w_max / w_min)`, `S = max_i max(|x_i|, |y_i|)` over `data` | `ε < ε_floor` → `TOLERANCE_BELOW_NUMERICAL_FLOOR` |
| `K_FIT`, `K_REF`, `K_VER` | `2q + 2`, `4q + 4`, `8q + 8` | §5.5, §5.7, §6.2 |
| `ρ_rank` | `2^-40` | §5.6 |
| `R_MAX`, `STAG` | 16, 3 | §5.8 |
| `W_EVAL_MAX` | `10^7` evaluations | §5.8 |
| `B_DEPTH_MAX`, `B_WORK_MAX` | 8 levels; budget fixed by `MAINT-A-DXF1b` characterization | §7.2 |

All constants are `PENDING_VALIDATION_IN_MAINT_A_DXF1` (decision AX-D6): `MAINT-A-DXF1-S0`
and `MAINT-A-DXF1` measure success rates and costs on the matrix fixtures before the
author fixes them. The constant set is versioned as `bspline-param-lsq-constants/v1` and
named in the provenance block (§10.1); changing any value is a new constants version.

**Error model behind `ε_floor` (design rule, not a derived bound).** The floating evaluation
of `C` (homogeneous de Boor followed by a division) and of `A` (polynomial de Boor) has a
forward error of order `c_p · u · S · (w_max / w_min)`, with `u = 2^-53` and `c_p` a small
multiple of the degree; `e(u)` inherits the sum of both. `2^-40 = 2^13 · u` leaves a margin
of about three decimal orders above that noise for `p ≤ 7`, so that a requested tolerance is
never dominated by evaluation error. Because `S` is absolute, data far from the origin
(georeferenced drawings) or a large weight ratio raise the floor; this is intended: a
tolerance below it would be met or missed by rounding noise. The rule is validated, and may
be revised, with the fixtures of AP-2c and of coordinates at `10^6` to `10^9`.

## 9. Failures and atomicity

| Class | Typed causes |
|---|---|
| precondition | `REFERENCE_UNAVAILABLE`, `STORE_UNAVAILABLE`, `DEGREE_OUT_OF_RANGE`, `TOLERANCE_INVALID`, `TOLERANCE_BELOW_NUMERICAL_FLOOR`, `POINT_LIMIT_INVALID`, `INITIAL_KNOTS_EXCEED_POINT_LIMIT` |
| numerical | `SAMPLE_DESIGN_DEGENERATE`, `RANK_DEFICIENT`, `SPAN_NOT_SPLITTABLE`, `NONFINITE_RESULT` |
| stop | `TOLERANCE_NOT_REACHED_POINT_LIMIT`, `TOLERANCE_NOT_REACHED_ROUND_LIMIT`, `STAGNATION`, `WORK_LIMIT_EXHAUSTED`, `VERIFICATION_NOT_PASSED`, `CANCELLED` |
| creation | `CONSTRUCTION_REJECTED` (the family published an undefined revision or the creation batch failed) |
| persistence | `PERSISTENCE_NOT_EXACT` (the self-check of §12.3 failed) |
| bound (not a failure of the operation) | `BOUND_PRECONDITION_FAILED`, `BOUND_NOT_COMPUTED_WORK_LIMIT`, `NOT_COMPUTED` |

On any failure: no object of any kind (no point, number or list), no Undo entry,
`Construction` and its XML unchanged, the record unchanged. The reference stays in the
store indefinitely and stays exportable as an external definition (D-A-N2-4); a failed
approximation is neither a malformed file nor a loss of the reference.

**No persisted failure history (author decision D-R1-5).** A failure writes nothing to the
store or to the document, does not mark the document modified, and adds no Undo step; the
reference and its preferences are unchanged. The user sees the cause immediately in the
result of the operation (one short line, with **Details…**: the cause, the best global
indicator, its number of control points, rounds and work used). The same information is
kept in a **session report** of reference operations, in memory only: it is not saved, not
in Undo, not exported, and it is discarded with the document; it keeps the last 100 entries
(bound proposed), dropping the oldest. After saving and reopening,
the reference is simply a reference without CeDG representation.

## 10. Provenance block `approximationProvenance version="1"`

### 10.1 Grammar

One empty element, child of `<geocedgBSpline>`, attributes in this order. Reals are
binary64 bit patterns written as 16 lowercase hexadecimal digits (the pattern of
`Double.doubleToRawLongBits`). Digests are 64 lowercase hexadecimal digits. Strings are
XML-escaped. The block is the **only** persisted record of a successful conversion or
approximation: the store keeps no derivation history (§13.2).

| Attribute | Content | Presence |
|---|---|---|
| `version` | `1` | always |
| `operation` | `EXACT_CONVERSION` or `APPROXIMATION` | always |
| `method` | `exact-conversion/v1` or `bspline-param-lsq/v1` | always |
| `sourceContract` | `dxf-spline-reference/v1` | always |
| `storeContract` | `cedg-reference-store/v1` | always |
| `referenceId` | the store identifier of the reference (32 hex) | always |
| `sourceNature` | `POLYNOMIAL`, `RATIONAL_EQUAL_WEIGHTS` or `RATIONAL` | always |
| `sourceDataSha256`, `referenceDataSha256` | the record's `sourceDigest` and `dataDigest` | always |
| `sourceFileSha256`, `sourceFileName` | file digest and base name (import §9) | always |
| `sourceEntityHandle`, `sourceEntityLayer` | entity provenance, never identity | always |
| `transform` | the six coefficients `a b c d e f`, space-separated | always |
| `definitionDigest` | SHA-256 of the created definition (`bspline-2d.md` §12.3), taken from the family **after** creation | always |
| `degree`, `controlPoints`, `pointLimit`, `rounds` | `q`, `m + 1`, `N_A`, rounds performed | approximation |
| `tolerance` | `ε`, model units of the document | approximation |
| `constantsVersion` | `bspline-param-lsq-constants/v1` (§8) | approximation |
| `knotStrategy`, `sampleRule`, `refinementRule`, `verificationRule` | `source-knots-bisection/v1`, `uniform-2q2/v1`, `midgrid-4q4/v1`, `heldout-midgrid-8q8-golden40/v1` | approximation |
| `residualMax`, `residualRms` | grade `SAMPLE_RESIDUAL` | approximation |
| `errorEstimate` | grade `ESTIMATED` | approximation |
| `conditionEstimate` | `κ̂` of §5.6 (diagnostic) | approximation |
| `boundStatus` | `CERTIFIED`, `NOT_COMPUTED`, `BOUND_PRECONDITION_FAILED` or `BOUND_NOT_COMPUTED_WORK_LIMIT` | approximation |
| `certifiedBound` | `B`, only when `boundStatus="CERTIFIED"` | approximation |

The applicability state is **not stored**: it is computed at each revision (§11); a stored
state would become stale after Undo, redefinition or editing.

### 10.2 Semantics

No attribute is a DAG input, an identity, an evaluation datum, or an input of metrics,
intersections or the geometric export of the construction (`bspline-2d.md` §10.5). The only
reader outside the family is the disposition resolver of `cedg-reference-export/v1`, which
reads `referenceId`, `operation`, `referenceDataSha256` and the applicability of §11 (from
`definitionDigest`) to decide whether to include the **reference**; it never changes how the
construction itself is exported (candidate amendment A-BS-1). The same block records exact
conversions (`operation="EXACT_CONVERSION"`), although `bspline-2d.md` §10.5 names it
«approximation provenance» (candidate amendment A-BS-2).

### 10.3 Malformed block

A block with `version="1"` that violates §10.1 (missing, unknown or duplicated attribute,
illegible value) does not reject the document: the object loads, the block is preserved
byte for byte as `MAINT-A2` already does, and its state is `NOT_INTERPRETABLE`
(decision AX-D4, coherent with «degrade the guarantee, not the object»). An unknown
`version` keeps the fail-closed rule of `bspline-2d.md` §10.5.

## 11. Applicability (closes BS-24)

At every revision, for an interpretable block:

| State | Condition |
|---|---|
| `APPLICABLE` | the revision is defined and `definitionDigest` equals the digest of the current definition |
| `NOT_APPLICABLE_DEFINITION_CHANGED` | the revision is defined and the digests differ |
| `NOT_APPLICABLE_UNDEFINED` | the revision is undefined (`bspline-2d.md` §7) |
| `NOT_INTERPRETABLE` | §10.3 |

Rules: a change counts when it changes the digested data (degree, knots, control points,
weights); style, name, visibility or layer never count; redefinition compatibility
(ADR 0026) decides identity, not applicability. Applicability is a function of the current
data, not of history: returning exactly to the same binary64 values restores it; `-0.0`
against `0.0` loses it (conservative, declared). Outside `APPLICABLE`, no measure is shown
as a current guarantee: the provenance is history («derived from … in its original
definition») and the measures appear only in the advanced report under «not applicable to
the current definition». The object stays a valid B-spline.

## 12. Numeric persistence of the created construction

### 12.1 Characterized risk

The previous report warned that literal arguments could be serialized with 15 significant
digits. The characterization of A-N2 (report §4, with a scratch probe of four creation
routes through save/reopen twice, Undo/Redo, copy/paste and reopen) established:

- command XML always uses the XML string template, but for doubles `Kernel.formatRaw`
  returns `Double.toString` (integers `Long.toString`) before the 15-digit setting applies;
  typed literals keep their original text; free points (`<coords>`) and free numbers
  (`<value>`) use `XMLStringBuilder.attr(double)`, also `Double.toString`; Undo, copy and
  redefinition use the same XML and template. Every finite binary64 value round-trips bit
  for bit on these routes, **except `-0.0`, written as `0`**;
- a genuine 15-significant-digit path exists: `Kernel.format(BigDecimal, xmlTemplate)`, used
  for numbers that carry an exact decimal value inside a list **without definition**; values
  such as `0.1 + 0.2`, `1/3` and `nextUp(1)` change on reload there. No `BSpline` route uses
  it today;
- the definition digest hashes the exact bit patterns, so a one-ulp change makes recorded
  measures not applicable: a lossy route would silently degrade every guarantee.

The risk is therefore real but narrow; it does not require changing the kernel number format
or a shared persistence semantics.

### 12.2 Required representation

The inputs of the created `BSpline` travel only through exact routes (decision AX-D9):

- each control point is a **free point** set with binary64 coordinates (`setCoords(x, y, 1)`),
  never parsed from text; the point list references them by label;
- the knots are one independent list whose definition text is the shortest round-trip
  decimal (`Double.toString`) of each knot, or free numbers set by value and referenced by
  label; **never** numbers carrying an exact decimal value, `setExactValue`, or a list
  without definition;
- the degree is an integer literal;
- `-0.0` is replaced by `+0.0` before creation (§4.2).

The auxiliary objects are ordinary, auxiliary by default and hidden in the Algebra view
unless the user shows them. If `MAINT-A-DXF1-S0` shows that a chosen route is not exact in
save, reopen, Undo/Redo, copy/paste, Insert File or redefinition, or is not byte-stable on
save → reopen → save (one probed route that builds lists in code kept values but not bytes),
that route is not used; if no route qualifies, stop (§19.1).

### 12.3 Self-check

After creation and before committing the Undo step, the service serializes the new
objects with the document writer, parses the result in an isolated reader and compares the
bit patterns of degree, knots and control points with the computed data, and the
recomputed definition digest with `definitionDigest`. Any difference aborts the whole
creation atomically with `PERSISTENCE_NOT_EXACT`.

## 13. Construction, DAG, Undo and store

### 13.1 Construction

The created object is an ordinary independent construction: free inputs (§12.2), the
explicit knot form, no dependency on the reference, never refit automatically (D-A-10). No
second DAG exists. Creation is **one Undo step** containing the inputs, the `BSpline` and
its block. The pure computation may run off the event thread with progress and
cancellation; the mutation of `Construction` and the Undo store happen on the event thread
after complete success. Undo removes everything created; Redo restores the identical XML,
block included. Save/reopen, copy, compatible redefinition and replacement follow
`bspline-2d.md` §8.5 and §10.5: the copy has a new durable identity and the same block; a
replacement by another family drops the block; an R5 similarity image does not inherit it.

`MAINT-A-DXF1` must make the replacement route of a compatible redefinition keep the block,
as `bspline-2d.md` §10.5 already requires (fact 7 of §2), with an additive GeoCeDG change
and a test on both redefinition routes; it also adds the GeoCeDG-owned factory that attaches
a block to a new `BSpline` (fact 1). Both stay inside the GeoCeDG family code
(`org.geocedg.common.kernel.bspline`, `AlgoBSpline`) and change no grammar (decision AX-D13).

### 13.2 No store record of derivations (R1)

The operation never writes to the store. The minimal model that determines the current
representation and keeps traceability is the provenance block of the created construction
(§10): it names the reference by `referenceId` and records the method, parameters, measures
and definition digest. The export resolver computes whether a reference is currently
represented from `Construction` alone (`cedg-reference-export/v1` §6), so Undo, deletion,
redefinition or copy can never leave a stale store state. Nothing grows with repeated
attempts: failures leave no trace in the document (§9), and each success adds exactly one
construction with its block, removed by Undo like any construction step. The A-N2 draft of an
append-only derivation history in the store is withdrawn (author decision D-R1-5).

## 14. User interface

Compact by default (D-A-N2-5):

- Reference manager: the operations «Convert to B-spline» (convertible) or «Approximate…»
  (otherwise), and the short state from the resolver: «No CeDG representation»,
  «Converted», «Approximated», «Related construction modified» or «Association
  unresolved». A failed attempt shows its cause in the operation result and in the session
  report only (§9); it is never a state of the reference.
- Construction inspector: «Exact conversion of a DXF reference» or «Approximation of a DXF
  reference (`≤ ε`, estimated)»; «not applicable to the current definition» after an edit;
  a short warning «measures concern the approximation, not the reference».
- Approximation dialog: `q`, `ε` (proposal visible and editable), `N_A`, progress and
  cancel.
- **Details…** and the advanced report: knots, control-point count, rounds, `ε`, residual,
  estimate, bound or its status, condition estimate, digests, identifiers, typed
  diagnostics, full provenance.

Availability and maturity (author decision D-R1-10; decision AX-D10): no activation flag is
added for novelty. The operation is offered wherever `BSpline` creation is available (it uses
that existing creation path and its existing access rule) and joins the ordinary profile once
its implementation passes its tests and the author's smoke. Visible names never say
«experimental»; the guide may describe limitations. Reconciliation with AGENTS §5 (proposed):
while `MAINT-A-DXF1` is open the operation exists only on its branch and is not shipped, so no
interim state is needed; it enters the ordinary profile at the author's closeout, which declares
its formal state (`stable` bounded to the implemented contract, as for the import after
A-DXF0-R1); nothing is called `stable` before that. `experimental` behind a flag is the AGENTS §5
route that D-R1-10 excludes for mere novelty.

## 15. Determinism

binary64 without FMA, without parallelism, without `Math.pow`, `hypot` or transcendental
functions (only `+ − × ÷` and `Math.sqrt`); fixed order of samples, rows, columns, bisections
and golden-section iterations; no dependence on view, zoom, DPI, thread or time. Two runs
with the same record and parameters produce identical data and an identical block, byte
for byte (§5.9, item 7).

## 16. Validation

Cases `AP-1` to `AP-14`, `NUM-1` to `NUM-10`, `SEP-7`, `SEP-8` and the export cases `RX-7` and `RX-18` of the
[A-N2 validation matrix](../../../docs/validation/pre_g9b_maint_a_n2_validation_matrix.md).
They replace the reserved rows AP-1 to AP-6 of the A-N1 matrix, whose AP-2 asserted an
ordering that observation AP-2 of the approval record withdraws.

## 17. Smoke for the author (future)

With the author's DXF files: import; convert a polynomial reference; approximate a
rational arc with `q = 3`; read the ordinary state and the report; move a control point and
see «not applicable»; Undo; save and reopen with bit-identical inputs; cancel a long fit;
provoke a failure and check that nothing remains; export according to
`cedg-reference-export/v1`.

## 18. Kernel and upstream impact

| Change | Layer | Nature |
|---|---|---|
| fit, QR, sampling, estimate (and, in `MAINT-A-DXF1b`, the exact bound) | `org.geocedg.common.math.bspline`, new classes in the same package so that `BSplineExactRational` keeps its visibility | pure, no kernel type |
| conversion/approximation service | new package outside the kernel (proposal `org.geocedg.common.importer.dxf.approximation`) | adapter |
| attaching the block, parsing grammar `v1`, applicability, keeping the block on the replacement redefinition route | `org.geocedg.common.kernel.bspline` (additive factory on `PersistedContract`, a new provenance class) and `org.geocedg.common.kernel.algos.AlgoBSpline` | GeoCeDG, additive; the family element grammar is unchanged |
| creation | standard free objects and the existing `BSpline` creation path | no upstream change |
| Desktop | action, dialog, inspector text, report | application |

No change to kernel identity, `geocedgSpatial`, the `<geocedgBSpline>` grammar, `MyXMLHandler`,
the XML string template or any upstream path.

## 19. Stop conditions

1. `MAINT-A-DXF1-S0` shows that no representation with standard objects keeps the binary64
   inputs exact (zero sign aside) and byte-stable through save, reopen, Undo/Redo,
   copy/paste, Insert File and redefinition without changing the kernel number format or a
   shared persistence semantics: **STOP** and propose a minimal prior increment
   (characterization report §4). The A-N2 evidence found such routes; the condition stays
   as a gate of the implementation.
2. Attaching the block requires changing the family element grammar or the `MAINT-A2`
   reader beyond §10.
3. Applicability requires a new identity or dependency.
4. Weights are needed in the produced construction (`MAINT-A-R`).
5. The reference would gain a dependency, enter the DAG or be modified.
6. A certified bound cannot be justified: it is published as not computed, with its cause;
   this is not a stop.

## 20. Decisions

Status: **fixed** = decided by the author (D-R1-*); **proposed** = recommended for adoption
with this contract; **pending validation** = a value or behavior that the implementation must
measure before the author fixes it.

| Id | Question | Resolution | Status |
|---|---|---|---|
| AX-D1 | Dependency on the store | A-DXF1 requires `R-c` (no conversion of session-only references); the service and interface integrate after `MAINT-A-DXF0-RC`; the pure library may be developed in parallel | proposed |
| AX-D2 | Certified bound | `MAINT-A-DXF1` without bound (`NOT_COMPUTED`); `MAINT-A-DXF1b` with the exact rational bound of §7, its own procedure and budget | fixed (D-R1-9) |
| AX-D3 | Measure sets | three disjoint sets: fit samples, refinement grid, held-out verification set evaluated once on the final candidate; success needs residual and held-out estimate `≤ ε`; no ordering claimed; a held-out failure is `VERIFICATION_NOT_PASSED` | fixed (instruction R1 §9) |
| AX-D4 | Malformed `v1` block | load the object, keep the block, `NOT_INTERPRETABLE` (degrade the guarantee, not the object) | proposed |
| AX-D5 | Default degree | 3, user choice `1..7` | proposed |
| AX-D6 | Constants of §8 | candidate values, versioned `bspline-param-lsq-constants/v1` | pending validation |
| AX-D7 | QR implementation | own Householder without pivoting in the pure library | proposed |
| AX-D8 | Approximating a convertible reference | not in `v1` | proposed |
| AX-D9 | Input representation | free points set by value; knots as one list defined by shortest decimals (or free numbers by label); integer degree; no exact-decimal numbers (§12.2) | proposed; route confirmed by `MAINT-A-DXF1-S0` |
| AX-D10 | Availability and maturity | no novelty flag; available wherever `BSpline` creation is; ordinary profile after tests and smoke; no «experimental» in visible names; formal maturity by author decision at closeout (§14) | no flag fixed (D-R1-10); the reconciliation with AGENTS §5 proposed |
| AX-D11 | Source knots exceeding `N_A` | `INITIAL_KNOTS_EXCEED_POINT_LIMIT`; «Approximate…» not offered when known in advance; the reference stays exportable; no silent knot reduction | proposed |
| AX-D12 | Signed zero | replace `-0.0` by `+0.0` explicitly before creation; «exact» means equality under `==` | fixed (instruction R1 §9.2) |
| AX-D13 | Provenance block lost by the replacement redefinition route | an obligation of `MAINT-A-DXF1` (additive, family code), tested on both routes | fixed (instruction R1 §9.2) |
| AX-D14 | Failed attempts | nothing persisted: no store entry, no dirty mark, no Undo; immediate diagnostic and session report (§9, §13.2) | fixed (D-R1-5) |

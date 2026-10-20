# ADR 0037 — Locus V2 metric guarantee classes and admissibility

- Status: **ACCEPTED — ARCHITECTURE AUTHOR APPROVED WITH PRECISIONS N1–N5** (author
  instruction "PRE-G9B-CU1 — Author Approval of Metric Architecture and Implementation
  Authorization", 2026-10-20). The precision text below was incorporated by the
  `CU1` implementation (step S0). It belongs to the `CU1` technical candidate, which
  is pending author review.
- Date: 2026-10-20
- Phase: `PRE-G9B-CU1` (preparation and implementation authorized on 2026-10-20)
- Author decisions:
  - [`CU0` closeout record](../validation/pre_g9b_cu0_closeout_record.md) §3–§5
    (C1-0 to C1-4 and the binding H10 precision);
  - the `CU1` implementation authorization (D-CU1-1 to D-CU1-12 adopted; precisions
    N1–N5).
- Parent authority:
  - [Locus V2 metric contract](../../geocedg/specs/locus/locus-v2-metrics.md) v1.5, §25;
  - [Locus V2 semantic contract](../../geocedg/specs/locus/locus-v2-semantics.md) v1.2;
  - ADR 0007.
- Normative candidate: [`CU1` normative proposal](../architecture/pre_g9b_cu1_normative_proposal.md)
  §3 (amendment A1.2-R, metrics §25)
- Evidence: [metric aliasing analysis](../architecture/pre_g9b_cu1_metric_aliasing_analysis.md);
  `CU0` baseline pins `H10_H11`

## Context

The metric contract types numeric guarantees (`EXACT_ARITHMETIC`,
`CERTIFIED_ERROR_BOUND`, `ESTIMATED_ERROR`, `FLOATING_POINT_UNCERTIFIED`), but no
production path ever certifies a length. Every `LocusV2(Q, P)` uses an evaluator-only
chord refinement:

- it has no minimum depth;
- it uses a per-node tolerance scaled by the local chord;
- it returns an uncorrected chord sum.

The scalar adapter admits any estimated value whatever its error. `CU0` measured three
consequences:

- `LocusLength` of `y = sin(2πkx)` returns the chord, 1.0, as an admissible scalar (H10);
- the length of a circle of radius 8 or more is undefined (H11);
- the error exceeds the declared relative tolerance of `1e-9` (H12).

The author's precision of 2026-10-20 states that minimum depth and agreement of two levels
cannot certify an arc length. Indeed, for any deterministic algorithm that uses finitely
many point evaluations there is a smooth (even analytic) curve that agrees with the input
at every evaluated parameter and has an arbitrarily larger length. Guarantees therefore
need quantitative information about the curve, not more sampling.

The repository already holds such information for some families, unused by the metric:

- exact affine coefficients (`CertifiedAffineLocus2D`);
- exact similarity scales (`LocusSimilarityTransform2D.getLengthScale()`);
- outward value and first-derivative enclosures of the semantic curve, for SplineV2
  (`SplineIntervalModel2D`) and for class v1/v2 construction programs
  (`ConstructionIntervalModel2D`).

## Decision

1. Every metric contribution belongs to one of four guarantee classes, recorded with its
   method and work:
   1. `STRUCTURAL`: zero, affine image, similarity image or similarity of a closed-form
      driver, evaluated with outward rounding;
   2. `CERTIFIED_ENCLOSURE`: a rigorous enclosure from first-derivative enclosures. Chord
      lower bounds are combined with the cone bound `L ≤ chord / cos α`, which follows
      from `F′·u ≥ |F′|·cos α` for a cone of half-angle `α` around `u`;
   3. `ESTIMATED`: a refinement estimate whose error is justified only by recorded
      hypotheses;
   4. `NOT_ESTABLISHED`: a typed absence.
2. Agreement between refinement levels or sample sets never yields class 1 or 2.
3. The estimator for class 3 uses:
   - a component-level, translation-invariant tolerance distributed over leaves;
   - a minimum uniform partition;
   - Richardson correction;
   - a non-dyadic interior probe per leaf;
   - recorded hypotheses with their numerical scale.

   **N3.** The Richardson correction is justified only on leaves where the curve is
   `C⁴` with nonvanishing speed and the leaf lies in the asymptotic regime of the chord
   expansion. Piecewise `C²` regularity alone does not justify an `O(h⁴)` error. The
   estimator records these conditions as hypotheses, claims no convergence order, and
   does not apply the correction where the evidence contradicts them (for example zero
   speed).
4. **Evidence and admissibility (N2).** Rigorous evidence, its width, the requested
   tolerance and scalar admissibility are four distinct notions:
   - a certified enclosure keeps its mathematical validity however wide it is, and the
     rich result retains it;
   - a value is published as certified only when its certified error does not exceed
     the tolerance;
   - otherwise an estimate within tolerance may be published as `ESTIMATED_ERROR`,
     with the enclosure attached, and it must lie inside it;
   - an estimate that meets the tolerance remains an estimate.
5. Scalar and DAG admissibility:
   - classes 1 and 2 are certified inputs;
   - class 3 is an ordinary input under an explicit estimate contract, admissible only
     when its estimated error does not exceed the tolerance. It propagates as at most
     `ESTIMATED_ERROR` and is never a rigorous input of a certificate;
   - class 4 makes consumers undefined.
6. Budgets are versioned, benchmark-justified policy constants. They apply per
   component build and per query, with inheritance across nesting. Deterministic
   failures are memoized under their complete key with a typed work dimension; a
   failure caused by a caller's session limit is never memoized. No fixed budget is
   claimed sufficient for arbitrary curves: cost grows with total turning and with
   `1/√τ`.
7. **Transformations (N1).** A geometric transformation never improves the guarantee
   class of its source. For a similarity image with length factor `λ`:
   - the value and the absolute error scale by `λ`, plus the rounding bound of the
     product;
   - an estimated source yields an estimated image with the source hypotheses;
   - a certified source yields a certified image only when `λ` and the product are
     enclosed rigorously;
   - `λ = 0` gives an exact zero, and an undefined `λ` gives `NOT_ESTABLISHED`.
8. **Invariance (N4).** Results are independent of view, zoom, DPI and presentation
   size. Under a similarity of the construction, classes and relative errors are
   invariant only in the relative regime (`eps_metric_rel·S ≥ eps_metric_abs`); where
   the absolute floor dominates, no invariance of tolerance, class or work is
   required.
9. **Rigor of certificates (N5).** A class that publishes `CERTIFIED_ERROR_BOUND` must
   produce valid bounds through its complete computation, including norms, square
   roots, auxiliary constants and functions, and every rounding. An operation without
   a rigorous enclosure makes the certification fail closed. Adversarial tests
   demonstrate that no undue certification is attributed; they do not require the
   actual error of an estimate to lie within its estimated error for arbitrary
   curves.

## Consequences

- H10 to H12 are corrected for the characterized cases. For curves of arbitrary
  complexity, the limits are honest and stated by the result class.
- Circles and segments traced through similarity programs get exact structural lengths
  without consuming a budget.
- The author's chain `o_1 → LocusLength → d_2 → e_2` stays defined as class 3, without a
  false certification. Its certification depends on the class v3 construction model
  (`CU2`).
- Downstream certifiers (`CU2`, `CU3`) must treat class 3 metric inputs as uncertified.
- Existing tests that fix the current estimator values must be updated as declared
  conformance, never weakened silently.

## Alternatives considered

- **Minimum depth plus agreement of two levels as a certificate** (candidate A1.2 of
  `CU0`): rejected. It cannot certify (adversary argument) and would label estimates as
  guaranteed.
- **Undefined length for every non-certified metric:** rejected. It turns general metrics
  undefined unnecessarily and breaks the author's model until `CU2`.
- **The legacy GeoGebra interval package for expression enclosures:** rejected. It is not
  outward-rounded and its π bounds collapse to `Math.PI`.
- **A single fixed larger budget:** rejected. No budget suffices for arbitrary turning,
  and the cost must be stated, not hidden.

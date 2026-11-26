# ADR 0043 — Explicit 2D B-spline family, rational-ready, as a Locus V2 provider

- Status: **ACCEPTED — AUTHOR APPROVED (2026-11-24)** (proposed by `PRE-G9B-MAINT-A-N1`
  on 2026-11-23; author instruction «GeoCeDG — PRE-G9B-MAINT. Cierre autoral coordinado de
  N1, A-N1 y U2-R1», 2026-11-24;
  [approval record](../validation/pre_g9b_maint_a_n1_author_approval_record.md)). The
  accepted text is that of commit `8747c731eccc3644cd1da8b74eb3453a68511813`, with the
  precisions of that record. `selfApproved=false`. This is an architectural decision; it
  authorizes no implementation: `MAINT-A2`, `MAINT-A-DXF0`, `MAINT-A-DXF1`, `MAINT-A3` and
  the native NURBS `MAINT-A-R` remain unauthorized. The words «proposed», «candidate» and
  «pending author review» in the sections below record the stage at which each was written
  and are superseded by this status.
- Refinements accepted on 2026-11-26 (`PRE-G9B-MAINT-A-N2`,
  [approval record](../validation/pre_g9b_maint_a_n2_author_approval_record.md)): decision 10
  («session-only in the first implementation») is followed by
  [ADR 0044](0044-document-store-of-imported-references.md) (`R-c`); references stay outside
  `Construction` and the DAG. Decision 11 is refined by
  [ADR 0045](0045-separate-dxf-export-of-imported-references.md) decision 4 (informative reads
  of the provenance block to present the conversion or approximation state and to decide the
  default inclusion of a reference in the DXF export).
- Date: 2026-11-23.
- Authorization of the design work: author instruction «GeoCeDG — PRE-G9B-MAINT-A-N1.
  Autorización de diseño normativo — Familia B-spline preparada para NURBS e
  interoperabilidad DXF» (2026-11-23); execution contract
  [`pre-g9b-maint-a-n1-bspline-nurbs-normative-design.prompt.md`](../../.github/prompts/tasks/pre-g9b-maint-a-n1-bspline-nurbs-normative-design.prompt.md).
- Base: `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`.
- Author directions applied: D-A-1 to D-A-10 of the
  [P0 decision sheet](../validation/pre_g9b_maint_p0_author_decision_sheet.md) §0.
- Candidate contracts: [family specification `bspline-2d`](../../geocedg/specs/curves/bspline-2d.md);
  [DXF reference import `dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md);
  [validation matrix](../validation/pre_g9b_maint_a_n1_validation_matrix.md);
  [candidate report](../validation/pre_g9b_maint_a_n1_candidate_report.md).
- Related: ADR 0005, 0006, 0013, 0014, 0016, 0018, 0021, 0022, 0026, 0028, 0031, 0032,
  0040, 0042.

## Context

`SplineV2` (ADR 0018, [`semantic-spline-2d.md`](../../geocedg/specs/curves/semantic-spline-2d.md))
is an interpolating family: its data are points the curve passes through, its parameter
is a normalized chord parameter in `[0, 1]`, its knots are simple and its coefficients
come from a dense linear system. Its approved contract states that it is not a NURBS or
control-point B-spline contract, and its XML carries no family or version attribute.

The author approved, as architectural direction (D-A-1 to D-A-5), a new semantic family
defined by degree, control points, knots and, later, positive weights; polynomial in its
first implementation; independent of SplineV2; with no general rational interface in the
kernel until one is needed; and with the native rational extension `MAINT-A-R` designed
but not authorized. The author also approved a read-only import of DXF `SPLINE`
references outside the DAG (D-A-6, D-A-7) and an explicit polynomial approximation of a
rational reference as a distinct construction (D-A-8 to D-A-10).

The read-only characterization of `MAINT-A-N1`
([candidate report](../validation/pre_g9b_maint_a_n1_candidate_report.md)) established
the facts this decision depends on:

1. `PiecewisePolynomialLocus2D` publishes coefficients in powers of the **global**
   provider-canonical parameter, and the two polynomial intersection capabilities
   re-expand them in floating point around each span start. With the user knot parameter
   `u` (D-A-2), knots far from the origin or many spans make that re-expansion
   ill-conditioned; nothing guards against it today.
2. Every certified tier of Locus V2 (interval curve model, certified implicit targets,
   certified pairs, class-2 metric cones, certified continuation) is tied to the final
   class `SplineSemanticEvaluator2D`. Rendering, approximate export, existence and the
   class-3 metric estimate need only the evaluator interfaces.
3. The one-sided polynomial intersection capability accepts at most 64 spans; the pair
   capability at most 33 coefficients per coordinate.
4. No de Boor, NURBS or DXF reader exists in production code.
5. The command XML writer has no hook for command attributes; current readers ignore an
   unknown `<command>` attribute silently and only log an unknown `<command>` child; only
   the GeoCeDG metadata codecs whose typed exceptions `MyXMLio.doParseXML` rethrows make a
   load fail closed with rollback. An unknown `.cedg` archive entry is not re-saved.
   Details in the candidate report §2–§3.

## Decision

1. **A new family `bspline-2d`, independent of SplineV2.** Its conceptual identity is «2D
   B-spline curve defined by degree, control points, knot vector and positive weights».
   The general definition is rational; the first contract `bspline-2d/v1` is the
   polynomial subset with implicit unit weights. SplineV2, its command, its XML and its
   tests are unchanged.
2. **Publication as `GeoLocusV2` through its own provider.** One branch, one component,
   a `providerProved` existence structure with provider contract `bspline-2d/v1`, the
   user knot parameter `u` as canonical parameter (D-A-2), the knot-ownership rule shared
   with SplineV2 (right-owned interior knots), and the guarantees of the family
   specification §15. Inputs are DAG parents; render tessellation is derived only.
3. **Command surface for `MAINT-A2`.** `BSpline(<points>, <degree>, <knots>)`,
   `BSpline(<points>, <degree>)` with the persisted rule `clamped-uniform/v1`, and
   `BSpline(<points>)` with degree 3. No silent degree reduction. The form with weights is
   reserved for `MAINT-A-R`. Canonical English header per ADR 0031.
4. **Initial scope (D-A-3).** Clamped open and exactly closed `C^0` curves;
   `1 ≤ p ≤ 7`; `p + 1 ≤ n + 1 ≤ 128`; interior multiplicity `≤ p`; pairs rich-only;
   command only, no graphical tool in `MAINT-A2`.
5. **Structural continuity is exact; regularity is not claimed.** Parametric continuity
   `C^{p-k}` at an interior knot of multiplicity `k` is published from the knot structure,
   exactly. Regularity and `G^1` are distinct facts, `FLOATING_POINT_UNCERTIFIED` in `v1`;
   their certification belongs to `MAINT-A4`.
6. **Conditioning: local per-span basis.** The family's polynomial authority is a local
   representation per span, in the span variable `s ∈ [0, 1]`. `MAINT-A2` adds one
   additive, optional extension of the piecewise-polynomial publication through which the
   generic consumers read local coefficients when a source offers them; for every other
   source their arithmetic stays bit-identical. A residual fail-closed rule applies: a
   definition whose polynomial publication cannot be served within the declared accuracy
   declines the polynomial capability with a typed reason and the existing
   evaluator-only routes answer. Specification §11.3 fixes the bound and the routing.
7. **Versioned persistence from the origin.** The command XML carries the serialized
   contract identifier `bspline-2d/v1` and the knot rule in a dedicated child element
   `<geocedgBSpline>` of `<command name="BSpline">`, written before `<input>` through an
   additive writer hook and read by a GeoCeDG-owned codec whose typed exception makes the
   load fail closed with rollback, like the unit and hidden-layer codecs (specification
   §8.1). A reader fails closed on an unknown version, a missing element or an unknown
   attribute or child; it never reads a later version as `v1`. A future `bspline-2d/v2`
   reader reads `v1` as `w ≡ 1`. The identity layer (`geocedgSpatial`) is not changed.
8. **Homogeneous evaluation from the origin, no rational kernel interface.** The pure
   library evaluates in homogeneous coordinates `P_i^w = (w_i x_i, w_i y_i, w_i)`; with
   implicit unit weights it is bit-identical to polynomial de Boor. No generic Locus V2
   interface becomes rational before `MAINT-A-R`.
9. **One pure mathematical library.** Stateless code in a GeoCeDG-owned package outside the
   kernel packages (`org.geocedg.common.math.bspline`, proposed), with no dependency on
   `Construction`, `GeoElement` or a view. It is shared by the kernel family
   (`MAINT-A2`), the reference import (`MAINT-A-DXF0`) and the approximation
   (`MAINT-A-DXF1`). It is not a geometric authority; authority comes from the family
   contract in the kernel. Specification §12 fixes its API.
10. **Imported references are data, not constructions.** `dxf-spline-reference/v1` keeps
    references outside `Construction`, outside the DAG, never `GeoLocusV2`, never an input
    of tools or commands, session-only in the first implementation (`R-a`); `R-c` is the
    later priority target after its characterization; `R-d` is not authorized.
11. **Approximation provenance travels with the approximating object.** The provenance of
    an approximation (D-A-10) is an optional passive block of the family's XML, never a
    DAG input, never identity, never evaluation authority. Its measures apply only while the
    definition digest they were computed for equals the current one; otherwise they are
    declared not applicable. Copy keeps the block with a new durable identity. The
    approximating object is frozen and independent of its reference.
12. **Native NURBS designed, not authorized.** `MAINT-A-R` is the versioned extension
    `bspline-2d/v2` with persisted positive weights as DAG parents, a per-span homogeneous
    numerator interface and its own guarantees (specification §14). This ADR authorizes
    none of it.

## Consequences

- The family inherits, without kernel changes beyond decision 6 and the product wiring,
  rendering, approximate DXF and LaTeX export, existence, the class-3 metric estimate,
  the floating one-sided polynomial intersections (up to 64 spans), the rich-only pairs,
  point interaction, continuation through the polynomial path and R5 similarities.
- It does **not** inherit any certified tier: certified implicit targets, certified pair
  materialization (D2 tokens), class-1/2 metrics and certified continuation charts need a
  `CertifiedIntervalCurveModel2D` for the family (`MAINT-A4`, with an ADR 0028-type
  amendment).
- Documents containing `BSpline` do not open in GeoCeDG 1.2.2 or earlier. The refusal is a
  side effect of identity validation (the unknown command alone would load leniently and
  partially), with a generic load error rather than a cause naming the family; release
  notes must say so (specification §8.4). This corrects the P0 proposal, which expected a
  plain «unknown command» failure.
- SplineV2 non-regression is a gate of `MAINT-A2`, including bit-identical consumer
  arithmetic for SplineV2 after the additive extension of decision 6.
- `MAINT-A-DXF0` and `MAINT-A2` share only the pure library; whichever is authorized first
  creates it with the API of specification §12.

## Alternatives

| Alternative | Reason for rejection |
|---|---|
| α — extend SplineV2 with control-point forms | two incompatible semantics under one command and one type; amendment of an approved contract; the third argument is already a weight function; unversioned XML; no clean path to weights |
| β' — closed polynomial family now, a separate NURBS family later | two families, commands and contracts for one notion; migration or duplicated consumers when weights arrive; contrary to the author direction |
| γ — generic `LocusV2(point, driver)` built with de Boor formulas | evaluator over isolated construction copies; loses knots, structural continuity and the polynomial structure; never exact for `SPLINE` export |
| δ — Classic `Curve` by pieces | no semantic identity, no knots, no Locus V2 domain; ADR 0018 rejected this route |
| ε — shared internal B-spline representation, also derived from SplineV2 | the SplineV2 conversion is not exact in binary64 and needs a new fidelity class; deferred as `MAINT-A5` |
| Normalize the canonical parameter to `[0, 1]` | contradicts D-A-2; the parameter of `Point(L, u)` would differ from the user's and from an imported `SPLINE` |
| Fail-closed conditioning check alone, with global coefficients | with integer clamped-uniform knots it would decline the polynomial capability for ordinary curves (for example `p = 5` with 20 control points); see specification §11.3 |
| Approximation provenance in a document registry outside `Construction` | lost on copy; separates the measures from the definition they qualify |
| Reference inside `Construction` (`R-d`) | not authorized; requires a new kernel type and its full contract set (proposal §8.7) |

## Status boundary

This ADR stays **proposed** until the author accepts it explicitly. Acceptance of this ADR
would not by itself authorize `MAINT-A2`, `MAINT-A-DXF0`, `MAINT-A-DXF1`, `MAINT-A3`,
`MAINT-A4` or `MAINT-A-R`; each requires its own authorization on an exact base.

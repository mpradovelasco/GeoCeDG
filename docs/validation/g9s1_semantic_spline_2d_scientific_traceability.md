# G9S1 semantic Spline V2 scientific traceability

- Status: **approved implementation traceability — scholarly-source limits retained**
- Phase: G9S1

| Claim area | Repository authority | Independent evidence required | Publication status |
|---|---|---|---|
| semantic spline domain/spans/knots | [normative spec](../../geocedg/specs/curves/semantic-spline-2d.md), [ADR 0018 — Accepted](../adr/0018-semantic-spline-2d-capability.md) | analytic evaluation at endpoints/interior, continuity and knot ownership cases | G9S1 PASS authority |
| Locus V2 result authority | accepted Locus semantic/public specs and source | no render/cache dependence; durable `GeoLocusV2` round trip | existing base plus G9S1 extension |
| one-sided spanwise root discovery/isolation | [method review](../research/g9s1_semantic_spline_numerical_methods.md) | independently generated polynomials, residual checks, derivative-cell and knot/seam cases | floating estimated evidence only; scholarly sources required for stronger theorem |
| pair rich-result discovery | candidate spec and pair source authority | operand swap, non-grid roots, knot/seam deduplication, tangency/overlap and budget negatives | rich-only; no certified unique pair selector or materialization claim |
| tangency/multiple roots | intersection specs and G8 authority | even-contact analytic candidates and conservative nonmaterialization | discovery evidence only unless local isolation is separately established |
| deterministic semantic identity | ADR 0017 and accepted ADR 0018 | enumeration/path independence, knot ownership, topology transition negatives | approved G9S1 evidence |
| local admissibility vs completeness | accepted R4 contract | matrix of local/global combinations | existing invariant, new spline cases required |
| spline and Locus V2 arc length | accepted Locus metric contract | adaptive-Simpson split-at-knots and exact-semantic-endpoint route cases, analytic/reference comparison, scalar/rich separation, reported estimate/work limits and invalid-state recovery | controlled numerical claim; route-local evaluator evidence is estimated with explicit assumptions; scholarly numerical source required |
| transform covariance | accepted R5 contract | analytic geometry and new-token assertions | G9S1 evidence required |
| persistence/copy/undo | G9A/Locus persistence contracts | native `.cedg` round trip and lifecycle tests | G9S1 evidence required |
| complexity/work | candidate architecture | polynomial spans examined/rejected, raw candidates and existing evaluation/subdivision/refinement counters; timings only characterization | bounded functional claim only after final evidence freeze |

## Source policy

Current code, accepted specs/ADRs and exact tests are implementation authority.
They are not a substitute for scholarly support of numerical theorems. The
[research note](../research/g9s1_semantic_spline_numerical_methods.md) lists the
required literature work; missing sources remain explicit requirements and
must never be filled with invented citations.

## Retained cross-phase risk

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains owned by the existing G9
risk/roadmap authority. G9S1 does not duplicate or close it. If G9S1 happens to
add the exact missing native round trip, closeout must reconcile that evidence;
otherwise the required G9U1/global-G9 disposition remains.

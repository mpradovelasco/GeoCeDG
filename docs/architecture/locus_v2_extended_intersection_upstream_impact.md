# G8C upstream impact map

**Status: G8C DESIGN AND G8C1 PASS — AUTHOR APPROVED; G8C2 EDIT BOUNDARY
AUTHOR APPROVED / PRODUCTIVE IMPLEMENTATION NOT STARTED**

## 1. Source inspected

| Area | Actual source | Relevant authority / limitation |
|---|---|---|
| Conic types | `GeoConicNDConstants`, `GeoConicND`, `GeoConic` | Type, degeneracy, matrix, half axes, translation/eigenvectors and evaluation are semantic data. Matrix scale alone is not a physical residual. |
| Existing conic intersection | `AlgoIntersectConics`, `AlgoIntersectLineConic`, `AlgoIntersectPolynomialConic` | Useful analytic precedent, but existing output permutation, coordinate tolerance and view-scale behavior cannot define V2 identity/completeness. |
| Functions | `GeoFunction`, arithmetic `Function` | Evaluation, derivative and explicit interval exist. Unrestricted path bounds can derive from Euclidian view and are forbidden for G8C coverage. |
| Existing function intersection | `AlgoIntersectFunctions`, `AlgoIntersectFunctionsNewton`, `AlgoIntersectFunctionLineNewton` | Root-finding precedent only; does not provide a G8 rich result, semantic token, or coverage certificate. |
| Implicit interface | `GeoImplicit` | Polynomial coefficients, evaluation and first derivatives are exposed. |
| Implicit implementation | `GeoImplicitCurve` | Represents polynomial and broader expressions; sampled path/display behavior and internal factors are not an authoritative G8C adapter. |
| Existing implicit intersection | `AlgoIntersectImplicitPolynomials`, `AlgoIntersectImplicitpolyParametric`, `ImplicitIntersectionFinder` | Uses capabilities and tolerances not coupled to G8 completeness/identity; view boxes or coordinate dedup paths cannot be reused as authority. |
| Dispatch | `AlgoDispatcher` | Public dispatch is outside G8C authorization. No edit proposed. |
| Type system | `GeoClass` | Any future entry must be append-only; G8B already owns the rich result type. No new G8C type is presently justified. |
| G8 algorithm | `AlgoLocusIntersectionV2` | Normal-DAG publication and source/target lifecycle to preserve. |
| G8 rich Geo | `GeoLocusIntersectionResult` | Sole rich-Geo authority to extend, not duplicate. |
| G8 query/solver | `LocusIntersectionQuery2D`, `LocusIntersectionSolver2D` | Reusable one-parameter lifecycle and policy seam; pair solver must remain distinct. |
| G8 targets | `LocusIntersectionTarget2D`, `LocusIntersectionTargets2D`, `LocusIntersectionCapability2D` | Candidate extension seam for typed G8C1 adapters. |
| G8 result/solution | `LocusIntersectionResult2D`, `LocusIntersectionSolution2D` | Preserve orthogonal completeness and Option B point admissibility; pair evidence may require additive fields/types. |
| G8 continuation | `LocusIntersectionContinuation2D` | Preserve opaque tokens and explicit ambiguity; extend for canonical source pairs rather than list/coordinate matching. |
| G8 policy/work | `LocusIntersectionPolicy2D`, `LocusIntersectionWorkBudget2D`, instrumentation classes | Extend counters/budgets by phase; no hidden or global state. |
| G6 domains/session | `LocusInterval2D`, `LocusEvaluationSession2D` | Sessions can bind two coherent source revisions. Intervals require finite endpoints; unbounded pair domains are not representable. |

All paths above are under
`source/shared/common/src/main/java/org/geogebra/common/` or
`source/shared/common/src/main/java/org/geocedg/common/` according to their
package. The characterization tests are under
`source/shared/common-jre/src/test/java/org/geocedg/common/locus/`.

## 2. Actual G8C1 productive edit set

The separately authorized G8C1 execution uses the following minimum set:

- three target adapters, typed support/domain/evaluation values and one
  query-local capability in `org.geocedg.common.kernel.locus.intersection`;
- additive factory dispatch in `LocusIntersectionTargets2D`;
- narrowly additive target evaluation, policy, solver and instrumentation
  seams;
- the existing `AlgoLocusIntersectionV2` target-capture path;
- focused tests under the GeoCeDG JRE test package;
- modified-file inventory and durable documentation.

No upstream `GeoConic`, `GeoFunction`, `GeoImplicitCurve`, classic intersection
algorithm, dispatcher, command, serialization, type-system or frontend file is
changed. The rich Geo and token-selected point consumer require no edit.

## 3. Candidate G8C2 productive edit set

Subject to later separate authorization and ADR 0009 acceptance:

- a GeoCeDG-owned dual-source query/binding;
- a query-local pair-space solver and pair work ledger;
- additive immutable pair localization/Jacobian/overlap evidence;
- a normal-DAG dual-source algorithm that publishes the existing rich Geo;
- the existing token-selected point consumer with no authority change;
- focused tests and validation artifacts.

Whether `AlgoLocusIntersectionV2` can accept a second semantic source without
obscuring input semantics is an implementation audit point. A separate
`AlgoLocusLocusIntersectionV2` is preferred if it keeps two-source dependency
registration and failure atomicity explicit.

## 4. Compatibility and type-system risks

- Existing conic/implicit algorithms use coordinate matching or view-derived
  bounds in some paths; importing them wholesale would violate G8 identity and
  completeness.
- `GeoFunction.getMinParameter()/getMaxParameter()` may depend on view bounds;
  G8C must use explicit interval/domain metadata only.
- `GeoImplicitCurve` includes more than the proposed polynomial subset;
  accepting the class name alone would overclaim support.
- Singular implicit roots invalidate regular-gradient normalization.
- Pair algorithms can explode across branch/component products and cannot keep
  unbounded global token/index history.
- Operand ordering must not leak into semantic token identity.
- Existing G8 rich Geo and append-only `GeoClass` entry are sufficient unless a
  later audited requirement proves otherwise; no broader type change is planned.

## 5. Explicitly untouched surfaces

`AlgoDispatcher`, command registration/localization, public `Path`, XML and
persistence factories, legacy `GeoLocus`, Classic algorithms/outputs, frontend,
3D dispatch, G9, and Python DSL remain outside both candidate edit sets.

## 6. Modified-file governance

Every productive upstream-tree edit must be minimal, registered in
`docs/upstream/modified-files.yml`, tied to the approved extended specification
or ADR, and covered by focused plus composed verification. This design task adds
only GeoCeDG-owned docs, evidence, prompts, verifier and test-private probes.
The G8C1 author-approved edits are enumerated in the inventory and checked by
the focused verifier.

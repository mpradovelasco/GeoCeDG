# G9S1 — Semantic Spline 2D Capability

**CURRENT AUTHOR-SUPPLIED EXECUTION AUTHORITY — IMPLEMENTATION CANDIDATE.**

This canonical prompt distills the author-authorized G9S1 task. It does not
claim PASS and does not authorize G9U1.

# Objective

Implement a robust, first-class two-dimensional semantic spline capability for
GeoCeDG using **Option B**:

`SplineV2(...) -> new semantic GeoLocusV2`

The new parent owns explicit spline family, degree/order, source dependencies,
oriented domain, stable span/knot structure, evaluation and degeneration
semantics. Inherited Classic `Spline` remains unchanged.

The result must participate in the existing public Locus V2 authorities:
Point-on-Locus, total and partial rich length, rich intersections and exact
tokens, R4 active/dormant/reactivated point lifecycle, R5 transformations,
copy/undo/redo and native `.cedg` persistence.

# Authority and evidence hierarchy

Before mutation verify:

- clean published `main = origin/main = direct remote main` at the R5 closeout;
- annotated `geocedg-g9u0-r5-pass` object
  `3712595fe2b168ba494379b6b3f0051e4122cfae` peeling to
  `5952dfdbd238e71e598f4d2ca92c3e03437df41c`;
- R4 product PASS tag still peels to
  `63c291464111a5bcdbca488d6639662e46c389c4` and its post-closeout hashing
  descendant remains historical operational authority;
- R5 Option-A `COLLAPSED_IMAGE`, R4 deterministic identity, G9A durable
  identity/persistence and R2 native-document contracts remain intact;
- `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN/TRACKED;
- no G9S1 implementation already exists; and
- the branch is `feature/g9s1-semantic-spline-2d-capability` from that exact
  base.

Stop before implementation if any mandatory entry authority differs
materially.

# Scope

The bounded productive scope is the shared-kernel semantic `SplineV2`
capability, its existing Locus V2 consumers, focused persistence and command
integration, and the operational evidence required for author review.

## Required characterization

Inspect current source before selecting implementation classes:

- Classic `Spline` command forms, processors, algorithms and result types;
- its degree/order, interpolation/control, parameter and serialization
  behavior;
- existing Cartesian-curve, function, polynomial and parametric evaluators;
- Locus V2 definition/provider/evaluator, metric and intersection extension
  seams;
- R4 root identity, selector, token ledger and topology lifecycle;
- R5 semantic transform parent and command routing;
- command/localization/feature/persistence dispatch; and
- relevant upstream tests and scholarly/repository research.

Document why Classic `Spline` cannot provide stable explicit span/knot
authority without changing its established contract. Do not retrofit it.

# Architectural placement

Spline definition, evaluation, semantic partition, metrics, intersections,
identity participation and dynamic dependencies belong in the shared kernel.
Desktop is exercised only as a native-document consumer. Render, workspace and
screen state are never semantic authority.

# Required design/specification

Implement the candidates:

- [ADR 0018](../../../docs/adr/0018-semantic-spline-2d-capability.md);
- [semantic spline specification](../../../geocedg/specs/curves/semantic-spline-2d.md);
- [architecture](../../../docs/architecture/g9s1_semantic_spline_2d_capability.md);
- [numerical-method review](../../../docs/research/g9s1_semantic_spline_numerical_methods.md); and
- [validation matrix](../../../docs/validation/g9s1_semantic_spline_2d_capability_validation_matrix.md).

If source evidence contradicts a candidate decision, STOP and report before
broadening semantics.

# Compatibility and serialization

- Add one bounded experimental `SplineV2` public surface under the existing
  Locus V2 opt-in. No second spline/intersection feature flag.
- Return a new `GeoLocusV2` with a new durable ID and normal DAG inputs.
- Preserve Classic `Spline` dispatch, result, XML and behavior byte-for-byte
  where no unrelated baseline difference exists.
- Do not add `SplineV2Length`, `IntersectSplineV2`, generic `Path` conformance,
  render-based semantics or a parallel token/metric model.
- Feature-off and Classic diagnostic routes preserve supported V2 documents
  without enabling experimental creation.

# Explicitly forbidden scope

- Do not alter inherited Classic `Spline` semantics or serialization.
- Do not add generic `Path` conformance, render/sample authority, parallel
  metric/token models, viewport-dependent computation or frontend geometry.
- Do not execute G9U1, candidate-marker, workspace or branding implementation.
- Do not claim exactness, completeness, interval certification or durable pair
  tokens beyond the evidence actually established.

# Geometric invariants and degeneracies

The semantic provider must explicitly own:

- actual supported spline family and degree/order;
- interpolation/control/source dependency contract;
- oriented parameter domain;
- stable branch/component/span partition;
- knot/break values and canonical shared-knot ownership;
- continuity and derivative support;
- open/closed/periodic policy where implemented; and
- repeated/coincident points, repeated knots, zero spans, cusps,
  self-intersections, empty/invalid domains and temporary undefinedness.

Rendering is derived. Parameter identity never comes from a sampled point or
pixel.

At a shared knot, equivalent boundary representations produce one canonical
semantic root. Distinct semantic preimages remain distinct even at equal
Cartesian coordinates.

## Numerical method

Complete the source-backed comparison of:

- spanwise polynomial substitution;
- Bernstein/Bezier conversion, convex-hull/sign/variation exclusion and
  de Casteljau subdivision;
- Sturm/Descartes/root-counting methods;
- interval arithmetic and interval Newton/Krawczyk;
- Bezier clipping;
- resultants for pair problems;
- adaptive subdivision/refinement; and
- span/control-hull broad phase.

Select a deterministic bounded hybrid on correctness, local isolation/global
completeness, tangencies and multiplicity, conditioning, pair symmetry,
complexity/counters, diagnostics, dependency/licensing cost and minimal
upstream intrusion.

Do not invent citations. A missing scholarly source remains an explicit
research requirement. Do not claim interval, exact, certified or complete
evidence that the implementation does not establish.

## Rich intersection pipeline

For every supported target:

`span broad phase -> deterministic candidate cells -> isolation ->
refinement in original parameters -> residual/conditioning ->
multiplicity/contact -> semantic selector -> rich result/token`

Support and validate, where the implementation contract states support:

1. line, segment and ray;
2. circle and supported conic;
3. regular supported polynomial implicit curve;
4. bounded supported function/parametric target;
5. spline × spline; and
6. spline × Locus V2 through a symmetric pair-domain seam.

A sign change alone is insufficient for tangencies/even multiplicity.
Overlap/nonisolated families remain rich overlap or typed ambiguity, not
fabricated isolated points.

Preserve:

`local point admissibility != global completeness`

Current successful, locally isolated and deterministic roots may be
materializable while global completeness is `NOT_ESTABLISHED`. A complete but
locally ambiguous/nonisolated candidate remains forbidden.

## Deterministic identity

Deterministic semantic selection is authoritative over continuity heuristics.
Identity may use durable curve/source pair, oriented branch/component/span
lineage, canonical semantic cell, typed germ/contact and current topology
certificate.

Identity must never use:

- Cartesian coordinates or nearest previous point;
- screen/viewport state;
- solver enumeration or result-list index;
- render/sample index;
- arbitrary proximity; or
- movement history.

One-sided phase/rank follows ADR 0017. Pair-query identity is symmetric and may
not choose an arbitrary “first spline”. Regular motion should preserve a unique
selector; topology ambiguity fails closed.

Existing materialized points may become dormant and automatically reactivate
only when the same exact selector resolves uniquely. Recompute never creates a
new `GeoPoint`.

## Rich length

Total and partial length split at knots and validity boundaries and integrate
the semantic derivative in world coordinates. Analytic evidence may be used
only when established; otherwise report deterministic absolute/relative error
and work limits.

Partial endpoints are semantic addresses. Multiple Cartesian preimages are not
silently assigned. Preserve R5 metric covariance, including finite `k=0`
`COLLAPSED_IMAGE` length zero without making invalid source addresses valid.

## Transformations and closure

All seven approved R5 forms must accept the published spline locus through the
normal Locus V2 route:

- translation;
- rotation about origin/point;
- reflection in point/line; and
- dilation about origin/point.

`C'(t)=T(C(t))` preserves the source semantic domain, spans, knots and
orientation in a new durable locus identity. Point/length/intersection
covariance is geometric; transformed query tokens are new and never copied
from source queries. Chained transformations remain normal DAG composition.

## Persistence and lifecycle

Native `.cedg` save/reopen reconstructs family/version, source dependencies,
domain/spans/knots, durable ID, style and downstream point/metric/intersection
authority. Do not serialize render vertices, solver cells, mutable caches or
Java closures.

Test copy/remap only under current provenance rules, undo/redo, rename,
ordinary recompute, feature-off preservation and Classic diagnostic
preservation. The `.ggb` compatibility-input contract is unchanged.

## Efficiency and diagnostics

Build/reuse one current span structure, exclude target cells before refinement,
solve roots once per rich result and resolve child points through the selector
map. Do not solve the entire intersection once per child and do not retain
unbounded motion history.

Instrument deterministic functional counters for span bounds, exclusions,
candidate cells, subdivisions, evaluations, refinements, residual checks,
root-count/isolation evidence, selector resolution, metric intervals and work
limits. Timing is characterization only.

# Required artifacts

Maintain:

- this canonical prompt;
- ADR 0018 and normative-candidate spec;
- architecture and research-method note;
- developer API;
- validation matrix and scientific traceability;
- implementation-candidate report;
- exact scenario/evidence/hash artifacts under the established validation
  hierarchy;
- focused verifier and composed insertion after R5/before future G9U1;
- roadmap, specs index, guides, G9 traceability/public matrix and modified-file
  registry; and
- definitive prospective
  `g9u1-construction-workspace-after-g9s1.prompt.md`.

No parallel verifier or evidence system is permitted.

# Required tests and commands

Run focused G9S1 A and deterministic B with exact normalized match, analytic
evaluation/length/intersection references, knot/tangent/multiple/overlap and
degeneration matrices, enumeration/path independence, pair symmetry,
transform covariance, persistence/copy/undo and counter/work-budget tests.

Rerun relevant Classic spline tests, G6/G7/G8, G9U0/R1/R2/R3/R4/R5, G9A,
G9X1, G5, legacy/scientific Locus, Desktop I/O, Checkstyle, static G9S1
verification, `git diff --check`, `git diff --cached --check` and full:

`.\tools\agent\verify.ps1`

Require exit 0 and:

`All GeoCeDG verification gates passed.`

Generated logs remain ignored. Prepare but do not self-pass the manual author
smoke.

## Manual author smoke

Prepare a concise real-product procedure:

1. launch with the existing Locus V2 opt-in;
2. construct supported `SplineV2` examples and inspect semantic/domain
   diagnostics;
3. construct total/partial length;
4. intersect with line and circle/conic including a knot and tangent case;
5. materialize exact-token points through the R3 inspector;
6. move controls through regular and topology-changing states;
7. verify deterministic persistence/no swapping and conservative dormancy;
8. apply every R5 similarity family and repeat point/length/intersection checks;
9. save/reopen `.cedg` and verify dynamics; and
10. compare Classic `Spline` control behavior.

The agent cannot mark this smoke PASS.

## Prospective G9U1 reconciliation

Create the post-G9S1 successor prompt, but do not execute it. Preserve:

- GeoCeDG `Continuity = OFF` through the existing host option; Classic remains
  configurable;
- current-token candidate markers only;
- create one, selected multiple and all eligible points;
- persistent inspector session and coherent compound undo;
- explicit/visible/undoable frontend auto-materialization only;
- kernel auto-reactivation only for already-existing points;
- G9A-compatible free-input redefine without label-derived identity;
- R5 and `SplineV2` actions/consumers;
- professional action groups, accessibility and Classic distinction;
- `geocedg.brand.topbar` and `geocedg.brand.startup` provenance seams; and
- open periodic-quarantine risk disposition.

G9U1 remains `DESIGNED — NOT AUTHORIZED` and blocked until G9S1 PASS plus a
separate author authorization.

# Stop conditions

STOP before broadening if:

- Classic `Spline` would change;
- stable semantic span/knot authority cannot be represented truthfully;
- the selected numerical method cannot distinguish its guarantees;
- pair identity would require coordinates, enumeration or asymmetric order;
- serialization would require render/cached/executable state;
- an upstream or licensing dependency lacks authority;
- historical gates regress unexpectedly; or
- a materially different semantic decision is required.

Do not commit, push, merge, tag or self-approve.

Stop at:

```text
G9S1 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9U0-R5 = PASS — AUTHOR APPROVED
G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP = OPEN / TRACKED
```

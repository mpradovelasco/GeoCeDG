# JUMP-CONTEXT — native scalar evaluation context

Author authorization: JUMP-CONTEXT focal implementation, 2026-11-18. Base
84c72a432225707d090c9fb7e1840b7f4a462c87 / tree
7f8425421c079dbc1d740818933094a302ad8878. One initial implementation,
at most one internal corrective iteration, independent final review. P2 is not
accepted; its five pending failures and B/C jump-length policies remain outside.

## Definition and invariants

The proof describes the exact-real formula sequence of the actual native
producer. Cartesian MyVecNode coordinates call ExpressionNode.evaluateDouble:
sine/cosine are Math.sin/Math.cos without zero snapping. AlgoDependentNumber
calls evaluate(defaultTemplate), using MyDouble.sin/cos with native zero branches.
Reading a numeric geo observes that producer's value; it does not reevaluate its
expression in the consumer's context. The distinction also applies to maximal
driver-independent expressions, captured as finite binary64 data by their actual
native route. Unknown contextual equivalence remains a typed refusal.

For F(t)=(t,sin(k*t)), k captured binary64 and t in[0,1], the real formula is smooth
with nonvanishing speed. The generic numeric-geo version retains its threshold
branches. This correction changes neither native numerical values nor geometric
identities, branch selection, XML, DAG or length conventions. A numerical estimate
never supplies a rigorous geometric premise. H10 and its partial conditioned
extension retain their eligibility and error/coverage/guarantee guards.

## Minimal design

Use explicit evaluation context in the capture's inline scalar traversal. A
Cartesian tuple starts DOUBLE; numeric-geo capture has its own GENERIC producer.
Keep SCALAR_SIN/COS as generic operations, add SCALAR_SIN_DOUBLE/COS_DOUBLE for the
proved evaluateDouble route. Program signatures record native-scalar-dispatch/v1;
constant data and distinct operation names invalidate stale proof/cache keys.
The interval model uses analytic jets for DOUBLE and snappedTrig for GENERIC.
Both keep the existing interval argument limit1000 and the same conservative
proof-range eligibility guard; no new cause, metric admission or integrator.
SQRT_SHORT is a generic evaluateDouble fallback and must not propagate DOUBLE
into a nested sine. Unsupported shapes and all metric-value, dependency,
angle-style, identity and degeneracy guards remain active.

## Impact and compatibility

Three production classes: CertifiedConstructionCapture2D,
CertifiedConstructionProgram2D and intersection/ConstructionBoxModel2D. No native
arithmetic, metric engine, persistence, frontend or integration changes planned.
The certification contract receives a contextual precision of its native-formula
rule, and the metrics contract corrects its attribution to these Cartesian
fixtures. Existing generic signatures/branches retain their meanings. New
certifications of old/new documents recapture the actual producer; no old result
payload is promoted. Tests must cover reopen/Undo and stable object identity.

## Focal validation

Contrast fast and generic producers at thresholds, in mesetas, nested expressions
and independent constants; keep true generic jump negatives. Check model/value
fidelity, signatures, complete Cartesian continuity and N03/N05/N06 metric/scalar
availability without geometric authority from estimates. Keep H10 outside-class
positives and invalid/degenerate/metric-input/selection guards. Run only targeted
methods and directly affected model/composition regressions using the canonical
Gradle evidence producer. Do not execute the five blocked P2 methods or global
profiles. Independent reviewer checks definitive patch and evidence before commit.

Bootstrap: NO CHANGE (same JDK/Gradle/dependencies). GUIDE_IMPACT: minimal ES/EN
context explanation; no generated inventory updates. Integrated verification still
requires FINAL under its own future authorization. Stop on an additional normative
change, scope expansion, or exhausted single internal corrective iteration.

## Status

**JUMP-CONTEXT BLOCKED — P2 NOT YET ACCEPTED.** Initial focus:79 tests,
78 pass/1 fail. The sole internal corrective iteration:80 tests,79 pass/1 fail,
no skipped tests. All three production sources are byte-identical between runs.
The original failure was a test's unguardable literal invalidity: GeoNumeric
setUndefined retains its expression, while setValue(NaN) clears it. Explicit
assertions and the unchanged Undo/Redo/FILE checks now pass.

The added SQRT_SHORT boundary test fails before its semantic assertions because
captureCertifiedProgramV3 returns empty for that fixture. The XML records no typed
capture refusal. Root cause: NOT_ESTABLISHED; no unsupported claim that the issue
is solely a fixture or a demonstrated production defect. Its fallback context
remains unverified at runtime. The one allowed internal iteration is consumed;
no further correction or tests were run. N03/N06 and historical sine N05 are
COMPLETE geometrically with ordinary ESTIMATED numeric lengths; that does not
close this failed focal gate or any of the five pending P2 failures. No PHASE,
INTEGRATION, FINAL, PACKAGING, promotion or publication.

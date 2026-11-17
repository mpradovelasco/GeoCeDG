# P2 — Conditional numeric estimate on partial geometry

Author decision: P2 scientific decision approved; characterization and bounded implementation authorized. Base P1 0420d8ee70d830d33caede0a26135d7b88d42981 / edad272087a12d9eadd8251c8b55a7f33d2226c7. Two focused corrective iterations maximum after initial implementation. No PHASE/INTEGRATION/FINAL/PACKAGING, protected checkpoint changes, model changes or book changes.

## Characterization before implementation

N03/N06: F(t)=(t,s_epsilon(4*pi*t)), D=[0,1]; eight certified continuous pieces and seven unresolved native-zero threshold bands. N05 has frequency2*pi, four pieces and three bands. All1025 sampled semantic evaluations in each case are valid. Direct diagnostic invocation of estimator v2 on the entire domain (NOT product admission) yields8.111828618823123 / estimated error5.116634328142418e-10 / tolerance1.3305946153526085e-9 and4.188275203684875 / error4.3726132979184096e-10 / tolerance1.255617091643235e-9. These match numerical references of the smooth sine curves; they do not validate the native curve's continuity.

MyDouble.sin snaps to zero when |argument|>.1 and |sin(argument)|<1e-8. Under certified-construction-model §13.2 these are real formula branches. At n*pi±asin(1e-8) their one-sided values differ by1e-8. Thus continuity/C4 and no-narrow-feature hypotheses of v2 are contradicted. These three fixtures stay inadmissible under the author's exclusions; no estimated value is promoted on their basis. The rejected metric is not an evaluation failure of F. Whether jumps contribute to another length policy, or scalar sine gets an ideal authority, remains outside this decision.

Positive product fixture: Segment((999.99,0),(1000.01,0)), P=Point(segment), Q=(x(P),sin(x(P))). Its first half is certified, its other half is unresolved because the interval prover's argument limit is1000. Evaluator1025/1025 valid; whole-domain v2 estimate .02294582313338213, error4.7704895589362195e-18, tolerance1e-10. Here sine remains away from native-zero thresholds and the curve is smooth. Independent composite Simpson131072/262144 references are recorded externally. sqrt(x(P)^2) on[0,1] is outside class as a whole, not a partial positive.

## Extension design and invariant

Preserve historical H10 unchanged. Add §26.6 partial-conditioned-estimate/v1, a bounded single-branch, bounded closed nonperiodic total-domain mode. Do not sum certified components. Reuse the existing conditioned estimator-v2 builder for the entire declared domain and existing owner/cache, with a distinct versioned key. No new integrator or tolerance/budget change.

Do not whitelist NOT_CERTIFIED, tolerance, selection, reconstruction, metric-input, budget, degeneracy or event causes. A new typed unresolved reason distinguishes a proof-only trigonometric range limit with a uniform native branch. The prover still returns UNRESOLVED and keeps its1000 bound. Before issuing this reason, require a whitelist of total smooth steps with exactly one trig operation, no METRIC_INPUT, root, selection, geometric predicate or variable division. Division by a finite scalar constant of magnitude>=1 is permitted. This prevents an opaque trig result hiding a later pole or invalid root.

The uniform-native-branch guard uses Lipschitz1: representable centre c in[a,b], outward radius r>=max(c-a,b-c), and downward |StrictMath.sin/cos(c)| minus r minus4*ulp(1.0) >1e-8. The absolute margin covers one-ulp StrictMath and Math errors on[-1,1]; all elementary subtractions/radius operations widen outward. Refuse nonfinite data. This is a numerical-premise guard only: no image enclosure, geometric existence or continuity certificate follows. Other unresolved reasons remain excluded. Scalar-zero branches of N03/N05/N06 remain excluded.

Result: existing rich value/error/diagnostics stay authoritative. Add immutable typed numeric-traversal evidence with domain, orientation, source revision and unchanged geometric structure; numeric coverage is independently COMPLETE only after every required contribution succeeds. Public getCoverage stays INCOMPLETE for partial geometry. Explicit scalar admission additionally requires full numeric traversal, SUCCESS, RECTIFIABLE, exactly ESTIMATED_ERROR with finite error<=recorded tolerance, current matching revision and versioned conditional role. Ordinary/H10 rules are preserved; no unconditional deletion of the COMPLETE guard. The scalar adapter reads the same rich result without reintegration.

Expose metadata in API and existing metric inspector. Persist no result payload: reconstruct through existing command/DAG/XML recipes; invalidity keeps identities and normal recomputation recovers. New conditional results never supply rigorous metric evidence downstream (§25.5/26.3); numerical use in formulas remains allowed.

## Validation / impact

Focus: all eight N cases, strict H10, positive partial numeric fixture and algebra, excluded tolerances/known jumps, nonexistence, failure/budget/error, stale revisions, downstream authority, parameter loss/recovery, similarity, copy/redefine/Undo/Redo/FILE reopen. Keep original estimator adversaries and substantive cycle/token assertions. Independently review implementation and scientific admission. Count corrective iterations in a new external ledger; do not edit previous sealed evidence.

Placement: shared kernel numerical admission/provenance plus minimal existing Desktop inspector; no geometry algorithm replacement. Source of truth: approved metrics amendment and code; inventories generated by official procedures only in a separately authorized later gate. Bootstrap NO CHANGE: same toolchain/dependencies; verifier impact new focal tests only. GUIDE_IMPACT: targeted documentation of the numeric condition in the metrics contract and product guide if public scope reaches acceptance. Required integrated level remains FINAL, explicitly forbidden in this focused authorization.

## Focused disposition — P2 BLOCKED

Initial focus and R1 failed compilation. R2 compiled Shared and ran58 tests:
53 passed,5 failed,0 skipped. Both authorized corrective iterations are consumed.
No third correction or global validation is authorized by this decision.

Unresolved: invalid budget fixture (2 evaluations below constructor minimum3),
semantic-revision monotonicity assertion after compatible redefine, similarity
fixture hitting REDEFINE_CONTEXT_MISSING, and two newly added Cartesian positive
assertions expecting2 materializable roots where the interval-curve capability
leaves0 eligible. The latter must not be fixed by elevating geometric authority.
An independent review additionally identifies a defensive API coverage gap: the
partial admission loop does not explicitly verify that its eligible coverage
items exhaust the requested domain. Current hybrid producer is exhaustive;
no native reproducer was run for a sparse externally constructed definition.
This remains unresolved, not a verified fix or accepted limitation.

Important P2-GATE erratum: original P1 N08 fails at finiteSolutions.empty
(line162), after eligible.empty (line158) passes. It had rich finite roots,
not newly admitted materializable points. The earlier diagnosis conflated these.
The sealed P1 evidence is preserved; this addendum corrects the interpretation.

Positive P2 whole-domain scalar/algebra, geometry preservation, native-jump and
cause exclusions, FILE recovery, Undo/Redo and clipboard checks passed in R2.
Their evidence does not close the five failures, frontend compilation, contract
frontiers or integration gate. Inspector additions remain unverified on Desktop.
Version1.2.0/date2026-11-16 and all protected checkpoints remain unchanged.

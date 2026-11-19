# GeoCeDG 1.2.0 - Point interaction on Locus V2: focused maintenance

Status: **CORRECTIVE CANDIDATE - NOT AUTHOR APPROVED**. `selfApproved=false`.
Date: 2026-11-19 (Europe/Madrid).

Baseline: published `v1.2.0`, commit `4f6a0a9e3ae25702c8c43fef287bf5881e0cb24e`.
Branch: `maintenance/post-v1.2.0-locus-v2-point-interaction`. This is focused
maintenance under the author's request of 2026-11-19. It is not a roadmap phase,
a product version, a publication or a change of the book. It changes no Locus V2
result: evaluation, length, existence structure and persistence are untouched.

Reproduction input: the author's model `InterCilConoObliqueFinal.cedg`
(SHA-256 `de9d494e2c4883521494dfb4ce8827e2e629e2a2b77972fd5721b41c92c097e8`),
read from working copies only. Its development `s_2 = LocusV2(V_1, E_1)` reads a
length on `e_2`, whose generator reads a length on `o_1`: two nested metrics.

## 1. Diagnosis

Measured on the model with scratch probes before any change.

| Defect | Cause | State |
|---|---|---|
| A. Drag stops | Any request without an established result latched the gesture (`Context.stop()`), so every later event answered `CONTINUATION_STOPPED`. On `e_1` the 512 chart-proof passes are exhausted after about 25 small on-curve steps. On `e_2` (nested metric) no interval model exists, so no continuation route existed at all: 30 of 30 events refused. | Confirmed |
| B. No point on `s_2` | One evaluation of `s_2` charges 82 semantic misses (81 nested). The creation facade ran the same deterministic discovery three times, once per permission, each from an empty memo: 26386 misses each, against a cumulative limit of 32768. The second run exhausted it. `" / null"` was the absent local-failure detail concatenated into the cause. | Confirmed |
| C. Raw descriptors | The panel and the chooser printed the branch key, the component lineage key and the raw parameter. | Confirmed |
| D. Two coincident intersections | One mathematical root, at the included start `u = 0` of the component. The component is a full-length open arc; its excluded end shares the canonical parameter of the start. The solver sampled `nextDown(upper)`, which the periodic canonicalization rounds onto that shared parameter, read the image of the start there and published it as a second root. The end of the curve is the one-sided limit (22.693, 3.459), 5.78 units away from the line. | Confirmed |
| E. Point on Object | Not covered by Point: only Point on Object places a point in the interior of a polygon or of a filled conic. | Confirmed; tool kept |

## 2. Design

Kernel (shared semantics):

1. **A refused request no longer ends the gesture.** A request that published
   nothing (`NO_CONTINUATION_ESTABLISHED`, `FORWARD_REJECTED`, `BUDGET_EXHAUSTED`,
   `COMPONENT_NOT_ESTABLISHED`) drops the retained chart choice and keeps the
   context. The next request must establish a complete fresh proof anchored at the
   unchanged current address, exactly as a new gesture on the same point would.
   A changed source, authority or host state, a locked point and a failed
   publication still end the gesture.
2. **Forward parametric continuation** (`LocalForwardParametricContinuation2D`),
   used only when no certified chart is established or its proof passes are
   exhausted. It is a bounded Gauss-Newton descent of the squared distance to the
   request in the lifted parameter of the current certified component, by forward
   semantic evaluations. It is accepted only as a converged, well-conditioned
   projection; otherwise the request is refused and nothing is published.
3. **Discovery replay** in the creation facade. The historical discovery is
   charged once; later permissions of the same operation receive its exact-key
   values as cache hits and spend only their own proofs and forward
   confirmations out of what the operation has left.
4. **Sampling next to an excluded arc end** in the target intersection
   capabilities: the neighbour of an excluded end is stepped inward until its
   canonical form has the arc coordinate of its lifted position.

Frontend (presentation and orchestration only):

5. Address labels read `Branch n . Section m . u = value`. The ordinals are display
   positions only; the choice keeps the exact address, and the complete technical
   identifier is the tooltip of the entry.
6. Refusals are explained in English and Spanish without enum names, `null` or
   internal identifiers. The typed code stays in the tooltip of the status field.
   A work limit is not described as a discontinuity.
7. The toolbar is unchanged. `apps/geocedg/application-profile.yml` remains its
   only authority.

## 3. Candidate normative amendment

The implementation above is authorized by the request. Its normative text is
proposed here and is **not approved**; it extends the candidate contract of
`pre_g9b_cu7_local_continuation_candidate.md` and section 8 of
`geocedg/specs/locus/locus-v2-point-interaction.md` in exactly three points.

**A1. Gesture recovery.** "Failed new-chart events stop the context" is replaced
by: a refused request publishes nothing and leaves the address unchanged; the
gesture continues, and the next request needs a complete fresh proof anchored at
that address. No proof, chart or partial result survives a refusal.

**A2. `LOCAL_FORWARD_PARAMETRIC_CONTINUATION`.** An independent sufficient route,
below the certified charts in priority. Its predicates, all mandatory:

- the argument stays in the current certified continuous component, in its lifted
  parameter; component ends, certified folds and singular parameters are never
  crossed or entered;
- every iterate is a valid, non-singular semantic evaluation, and the squared
  distance to the request strictly decreases at every accepted iterate;
- the tangent never reverses between accepted iterates, and the squared speed
  never falls below 1/64 of its initial value;
- the descent converges within 16 corrections of at most 1/16 of the component
  length each, or stops at an included end of the admissible interval;
- at an interior result the second difference of the squared distance is at
  least `2 * (1/4) * |F'|^2 * h^2`: the request is clear of the centre of
  curvature and the projection is a well-conditioned strict local minimum;
- at most `maximumRefinementIterations` root evaluations, charged, with their
  nested work, to the same hard-bounded session as the chart proofs.

Guarantee: the published point is the semantic evaluation at the proposed
argument. Only the choice of the argument is `FLOATING_POINT_UNCERTIFIED`: no
argument-error bound, inverse uniqueness, distance minimum or incidence
certificate is claimed. The argument is never chosen by a search, so Cartesian
proximity cannot select another branch, component or sheet. Three tolerances
stay distinct: the interactive resolution of the request (1/1024 of the
declared radius), the unclaimed error of `u`, and the precision of the semantic
evaluation, which this route does not alter.

**A3. Cumulative creation accounting.** "A different permission never restarts
the operation budget" is kept. It is completed by: the historical discovery of
one operation is deterministic for its source revision and request; it is
debited once, and its exact-key values are replayed to the later permissions.
No candidate, permission or evidence is carried by the replay.

The sampling correction of point 4 needs no amendment: it applies
`locus-v2-existence-components.md` section 15.3, by which an excluded end is the
image of no canonical parameter.

## 4. Measurements on the author's model

Embedded Desktop host, one workstation, informative only.

| Operation | Before | After |
|---|---|---|
| Drag `W_1` on `e_1` | 27 events, then latched (512 passes) | 6.7 ms mean, 16.4 ms worst per event |
| Drag `D_2` on `e_2` | never moves (no route) | 5.1 ms mean, 8.7 ms worst per event |
| Click on `s_2` | refused after about 1240 ms, 32768 misses | offer in about 800 to 1040 ms, 21000 to 26550 misses |
| Drag on `s_2` | not reachable | 29.9 ms mean, 46.6 ms worst per event |
| Click on `e_2` | about 220 ms, 6977 misses | about 80 ms, 2557 misses |
| `Intersect(s_2, l_2)` | two solutions at `u = 0` | one solution at `u = 0` |

Event times include the resolution, the forward binding, the atomic
construction transaction and the ordinary DAG propagation.

## 5. Validation

Focal suites added: `Maintenance120PointInteractionTest` (kernel),
`Maintenance120ConeInteractionDesktopTest` (author's model, real canvas events;
skipped where the model is not installed) and
`Maintenance120PointToolsDesktopTest`. Six existing tests that pinned the
latched gesture or the refusal after an exhausted chart proof were updated to
the candidate contract; the geometric barriers they exercise (fold, gap, centre
of curvature) are still refused without mutation.

`GUIDE_IMPACT = UPDATED`: two sentences of the bilingual user guide follow the
new panel wording and the continued gesture. Bootstrap impact: none; no
workstation prerequisite or toolchain assumption changes.

Verification follows `verification-levels.md` at the level of a focused
corrective change. `INTEGRATION` and `FINAL` were not run. Before either is run,
the JUnit inventory must be regenerated with executed `final.shared` and
`final.desktop` evidence, because three test classes were added.

## 6. Limitations and open points

- The first click on `s_2` still takes about one second: the discovery samples
  the whole component and each evaluation integrates two nested lengths. It fits
  the unchanged budget with a margin of about 20 percent. Reusing metric work
  between neighbouring parameters was not attempted.
- The forward continuation refuses a request near the centre of curvature and
  past a fold or an end, as the certified charts do; it does not prove them.
- `s_2` evaluates as invalid at the single parameter `u = -pi/6`. It was observed
  and not investigated; the continuation steps over an isolated invalid sample
  only by never accepting it.
- `EvaluatorPairIntersectionCapability2D` was not reviewed for the same sampling
  pattern.
- The roadmap does not yet register 1.2.0 or this maintenance.

# GeoCeDG 1.2.0 - Point interaction on Locus V2: focused maintenance

Status: **CORRECTIVE CANDIDATE - NOT AUTHOR APPROVED**. `selfApproved=false`.
Date: 2026-11-19 (Europe/Madrid); corrective review of the same date in section 7.

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
   semantic evaluations. It is accepted only with the evidence of section 3, A2:
   a well-conditioned stationary or discretely minimizing argument, or an end
   that the component includes; otherwise the request is refused and nothing is
   published.
3. **Discovery replay** in the creation facade. The historical discovery is
   charged once; later permissions of the same operation receive its exact-key
   values as cache hits and spend only their own proofs and forward
   confirmations out of what the operation has left.
4. **Sampling next to an excluded end** in the target and pair intersection
   capabilities and in their finite-difference tangents: the neighbour of an
   excluded end is moved inward until its canonical form has the arc coordinate
   of its lifted position. When no such parameter is found the component is not
   sampled, it is left out of the covered components, and a typed diagnostic
   says so (section 7, R3).

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
- every evaluated argument addresses a parameter of the component on its own
  side of the ends: outside a certified closed periodic component, the arc
  coordinate of the canonical parameter is the lifted coordinate of the
  argument (section 7, R1);
- within 16 corrections of at most 1/16 of the component length each, the
  descent stops in exactly one of three ways:
  1. the Gauss-Newton correction is below the interactive resolution (a
     stationary argument);
  2. no trial of the correction, halved down to 1/64, strictly decreases the
     distance, and the smallest trial was read (a discrete minimizer along the
     descent direction);
  3. the correction points out of the admissible interval at its bound;
- in cases 1 and 2 the second difference of the squared distance over a window
  of width `2h` that contains the argument is at least
  `2 * (1/4) * |F'|^2 * h^2`. The window is centred at the argument; where a
  centred window leaves the admissible interval it starts at the argument and
  lies on the interior side. Either difference is `h^2` times the second
  derivative at some argument of its window, so the request is clear of the
  centre of curvature there. A window that does not fit, or an argument of it
  without a valid evaluation, is no evidence: the request is refused;
- in case 3 the bound must be an end that the component includes; the argument
  is then the constrained minimizer by its strict first-order condition. The
  neighbour of an excluded end, the limit set by an excluded fold or singular
  parameter and the half-period window of a closed component are not ends:
  the request is refused;
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

The corrective review of section 7 added nine tests to
`Maintenance120PointInteractionTest` and no test class. The `PHASE` selections
accepted for `afb70e25f` were not repeated on its corrective descendant: their
evidence belongs to that commit and is not carried over.

## 6. Limitations and open points

- The first click on `s_2` still takes about one second: the discovery samples
  the whole component and each evaluation integrates two nested lengths. It fits
  the unchanged budget with a margin of about 20 percent. Reusing metric work
  between neighbouring parameters was not attempted.
- The forward continuation refuses a request near the centre of curvature and
  past a fold or an excluded end, as the certified charts do; it does not prove
  them. Its acceptance remains `FLOATING_POINT_UNCERTIFIED`: neither uniqueness,
  nor an argument-error bound, nor a global minimum is claimed.
- A stalled descent (section 3, A2, stop 2) is accepted with second-order
  evidence although its correction is above the interactive resolution. It was
  examined in the review of section 7 and kept, now refused when its smallest
  trial could not be read. How often ordinary drags end in that stop was not
  measured.
- `s_2` evaluates as invalid at the single parameter `u = -pi/6`. It was observed
  and not investigated; the continuation steps over an isolated invalid sample
  only by never accepting it.
- An included end of a full-length open arc is sampled at its lifted parameter
  `start + period`, whose canonical form is assumed to be the shared parameter.
  No real construction with an excluded start and an included end was found, and
  that sample is not checked (section 7).
- The certified chart routes (`LocalContinuationEvidence2D`,
  `LocalStrictProjectionContinuation2D`) work on interval models in lifted
  coordinates and were not reviewed in this round.
- The roadmap does not yet register 1.2.0 or this maintenance.

## 7. Corrective review of 2026-11-19

An independent review of candidate `afb70e25f` raised three possible weaknesses
of geometric validity. Each was characterized against that commit before any
change, with the focal tests named below.

### R1. Lifted and canonical coordinates on an open periodic component

**Confirmed and corrected.** The continuation evaluated the canonical form of
its lifted argument and checked only that the canonical parameter belongs to
the component. On a full-length open arc the two ends share one canonical
parameter, and the canonicalization of the provider rounds the lifted neighbour
of the excluded end onto it; that parameter belongs to the component as its
included start. With the real provider of a circle-driven locus (domain
`[-pi, pi)`, start `0`): `canonicalize(nextDown(2 pi)) = 0`. A request
beyond the excluded end clamps the correction to exactly that neighbour, so the
situation was reached deliberately, not by chance: on the nested-metric circle
traced up to `1.6 pi`, a request in the gap moved the point from the polar
angle `-1.5` to the start `(1, 0)`, across the structural discontinuity.

Invariant adopted: outside a certified closed periodic component, an argument
is evaluated only when the arc coordinate of its canonical parameter equals its
lifted coordinate up to canonicalization rounding; across the ends the two
differ by the whole length, and half the length separates the cases. The check
is in `coherent()`, applied by `evaluate()` to every evaluation of the
predictor-corrector and again to the published argument. Closure is read from
the certificate only (`isClosedPeriodic()`), never from the provider's
periodicity; on a certified closed component the wrap is the authorized seam
crossing and is unchanged. No Cartesian quantity takes part.

### R2. Acceptance without conditioning evidence

**Confirmed and corrected.** `conditioned()` returned `true` in two cases in
which the second difference could not be read: when the forward sample left the
admissible interval and when the backward one did. Next to either end of the
half circle, a request at the centre of curvature, and one on the far side of
it where the argument is a local maximum of the distance, were answered
`CONTINUATION_ESTABLISHED`. Separately, any argument at a bound of the
admissible interval was accepted as an end, including the limit set by an
excluded fold: on a segment with a fold enclosure `[2, 2.2]`, a request toward
the fold left the argument at `nextDown(2)`.

The four cases are now distinct:

| Case | Rule |
|---|---|
| Interior stationary or discretely minimizing argument | second difference on a centred window of width `2h` |
| Same, closer than `h` to a bound | second difference on the one-sided interior window `[v, v + 2h]`; refused when no window fits |
| Bound that is an end the component includes | accepted when the correction points out of it: strict first-order condition of a constrained minimizer; otherwise as an interior argument |
| Neighbour of an excluded end, limit of an excluded fold or singular parameter, half-period window | never an end: a correction pointing out of it is refused |

The one-sided window is sufficient for the declared numerical admission for the
same reason as the centred one: each second difference is `h^2` times the
second derivative of the squared distance at some argument of its window, and
the bound it must reach is the same. An impossible reading never admits. The
correspondence stays `FLOATING_POINT_UNCERTIFIED`; no solver was added.

A request beyond an excluded end is refused. The squared distance has no
minimum on the component there, and its last binary64 argument is not offered
as one. On the half circle that request was already refused before this round,
but only because the wrong-side evaluation of R1 lay far from the request; by
the previous code an argument at that bound was otherwise accepted as an end.

### R3. Fail-safe sampling next to an excluded end

**Confirmed and corrected.** No contract bounds the rounding of the
canonicalization by a number of binary64 neighbours: it has the magnitude of
the canonical domain, not of the parameter. With `ExplicitNumericDomainProvider2D`
on `[-pi, pi)` and an excluded start near `1E-10`, the 64 neighbours of the
start all canonicalize onto it. `insideExcludedEnd` then returned the parameter
it was given, and the solver read the image of the included end there: two
solutions at one point for a single intersection.

`insideExcludedEnd` now returns `OptionalDouble`. The search doubles the
distance to the end up to half the length of the component, which is where the
comparison stops separating the two sides, and returns empty when it finds no
parameter on the component's own side, or when the extent is not a certified
component of the branch. `ExtendedTargetIntersectionCapability2D` and
`EvaluatorOnlyIntersectionCapability2D` then do not sample the component: it is
left out of the covered components, no root is produced, completeness stays
`NOT_ESTABLISHED`, the geometry kind is never `EMPTY`, and a
`NUMERICAL_FAILURE` diagnostic names the component. Roots are still identified
by branch, component and parameter; nothing is deduplicated by coordinates.

The same sample was found, and corrected with the same function, in three more
places:

- `EvaluatorPairIntersectionCapability2D.parameter(...)`: **confirmed**. The
  intersection of the half circle with a second locus through its included
  start returned that root twice. A component pair without an admissible end
  sample is left uncovered, with the same diagnostic.
- `IntersectionCapabilityContext2D.evaluateDifferential` and
  `LocusPairIntersectionContext2D.differential`: **confirmed**. The
  finite-difference tangent of a root next to an excluded end spanned the jump
  to the other end: `984332.8` for a derivative of `-1`. Without a sample on the
  own side the differential is `UNKNOWN`.

### Contract preserved

Semantic identity is independent of coordinates; branch, component and
parameter stay explicit; persistence, the dependency graph and serialization
are untouched; a refusal publishes nothing, leaves the address unchanged and
keeps the gesture alive. The normative amendment of section 3 is still a
candidate: A2 is restated above with the predicates of this round, and A1 and
A3 are unchanged.

### Focal tests of this round

All in `Maintenance120PointInteractionTest`; each of the first seven fails
against `afb70e25f` and passes with the corrections.

| Finding | Test |
|---|---|
| R1 | `argumentNeverReachesTheIncludedStartThroughTheExcludedEnd` |
| R2 | `stationaryRequestNextToAnEndNeedsConditioningEvidence` |
| R2 | `limitOfAnExcludedFoldIsNotAnEndAndAnIncludedEndIs` |
| R3 | `sampleNextToAnExcludedEndIsSoughtBeyondSixtyFourNeighbours` |
| R3 | `componentWithoutAnAdmissibleEndSampleIsNotExaminedAndNothingIsClaimed` |
| R3, pair | `pairRootAtTheIncludedStartIsNotRepeatedAtTheExcludedEnd` |
| R3, tangents | `tangentNextToAnExcludedEndIsReadOnTheComponentsOwnSide` |
| R2, ends | `includedStartIsReachedAsAConstrainedMinimizerAndTheExcludedEndIsNot` (passes before and after: the included start, and the refusal at the excluded end of this fixture) |
| R3, guard | `transverseRootNextToTheExcludedEndKeepsItsOwnTangent` (passes before and after) |

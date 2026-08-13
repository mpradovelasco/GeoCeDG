# G7 Locus V2 metric validation matrix

| Field | Value |
|---|---|
| Status | G7A/R1 author-approved baseline; productive G7B matrix executed |
| G7A | `PASS — AUTHOR APPROVED` |
| G7B | `PASS — AUTHOR APPROVED` |
| Mathematical authority | Total variation per valid-domain component |
| Normative contract | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) |
| Benchmark authority | [`g7_locus_v2_metric_benchmark_plan.md`](g7_locus_v2_metric_benchmark_plan.md) |
| Date | 2026-08-13 |

This matrix defines characterization obligations executed by G7A and candidate
hard gates for a separately authorized G7B. `A` means G7A measures/classifies the
case; `B` means the minimum G7B must implement and pass it. `B-policy` means the
normative contract permits, using the G7A evidence, an explicit
unsupported or limit-not-established outcome instead of numeric support; G7B
must still return the exact rich status and never approximate silently.

## 1. Cross-cutting assertions

Every numeric case must verify:

- total variation is the semantic reference;
- non-negative length;
- invariance under viewport, zoom and DPI;
- zero accesses to `LocusRenderCache2D`, render vertices, legacy samples and
  `myPointList`;
- one coherent semantic revision per result;
- deterministic rich output for fixed construction/revision/policy;
- defensive immutable query, route, contribution and result values;
- complete provenance, units, diagnostics, decomposition and error metadata;
- cache/index ON/OFF semantic equality where an index is involved.

The full rich result, not only its scalar, is compared. Equality includes value
kind, coverage, computation status, rectifiability, traversal outcome,
guarantee, errors, diagnostics and contribution decomposition.

## 2. Analytic and independent-reference cases

| ID | Phase | Fixture/reference | Query | Expected semantic assertion |
|---|---|---|---|---|
| M-SEG-01 | A+B | `F(t)=P+t(Q-P)`, `t∈[0,1]` | total and A/B | `\|Q-P\|` total; subarc `\|t_B-t_A\|\|Q-P\|`; forward/reverse values equal |
| M-CIR-01 | A+B | `F(t)=C+R(\cos t,\sin t)` | full total | one fundamental cycle, `2\pi R` |
| M-CIR-02 | A+B | same circle | A/B forward/reverse | selected complementary arcs; no implicit shortest |
| M-CIR-03 | A+B | same circle, A=B | ZERO/FULL | zero versus `2\pi R` selected only by explicit same-position policy |
| M-ELL-01 | A+B | ellipse `(a\cos t,b\sin t)` | total | compare with `4aE(1-b^2/a^2)` or independently computed high precision |
| M-PAR-01 | A+B | parabola `F(t)=(t,t^2)` on finite interval | total and subarc | compare with analytic integral of `\sqrt{1+4t^2}` |
| M-TRN-01 | A+B | smooth transcendental `F(t)=(t,e^t)` on `[0,1]` | total | compare analytic/high-precision independent integral of `\sqrt{1+e^{2t}}` |
| M-SCALE-01 | A+B | scale source by `\lambda` | same queries | length scales by `|\lambda|`; errors scale under approved policy |
| M-TRANS-01 | A+B | translate source by fixed vector | same queries | value and error guarantee invariant; provenance identifies transformed evaluator |
| M-REPAR-01 | A+B | regular `t=\phi(u)=(e^{cu}-1)/(e^c-1)`, `c\ne0` | total/subarcs with mapped endpoints | invariant length; orientation preserved; `\phi'(u)>0` |
| M-REPAR-02 | A+B | `t=u^3` over an approved monotone interval | total/subarcs | invariant length despite endpoint/interior derivative degeneration; guarantee truthful |
| M-POINT-01 | A+B | constant `F(t)=P` | total and A/B | finite zero, complete, success, rectifiable, collapsed-image diagnostic |

For M-ELL-01 and M-TRN-01, G7A recorded the independent implementation and
precision. The Python-generated expected constants version formula, precision,
runtime/library, script and output hash. Python is not kernel authority.

## 3. Position, branch and topology cases

| ID | Phase | Fixture/query | Expected semantic assertion |
|---|---|---|---|
| P-ID-01 | A+B | same Cartesian point with two self-intersection preimages | semantic positions remain distinct; no coordinate inference |
| P-ID-02 | A+B | same provider parameter and branch across unchanged revision | explicit rebind succeeds and preserves semantic identity |
| P-STALE-01 | A+B | query with previous-revision binding | `POSITION_STALE`, absent/inadmissible result; no automatic repair |
| P-SPLIT-01 | A+B | valid component splits after topology revision | semantic position is not automatically changed; component key re-resolves only explicitly |
| P-BRANCH-01 | A+B | bound branch disappears | stale/branch diagnostic; old point/result not retained |
| P-DIFF-01 | A+B | A and B from different constructive branches | `DIFFERENT_BRANCH`/invalid query unless a future explicit cross-branch operation exists |
| P-PROVIDER-01 | A+B | provider version mismatch | invalid/stale binding; no nearby coordinate fallback |
| T-SELF-01 | A+B | `F(t)=(\sin t,\sin 2t)` self-X | route follows selected preimages; crossing is not a shortcut |
| T-MBR-01 | A+B | two distinct constructive branches with coincident images | total counts both branches separately |
| T-MCOMP-01 | A+B | one branch with two valid components and internal gap | total adds both; A/B route never crosses gap |
| T-PER-01 | A+B | periodic circle branch | total counts exactly one fundamental cycle |
| T-CUSP-01 | A+B-policy | finite cusp, e.g. `F(t)=(t^2,t^3)` | finite variation when established; cusp diagnosis/guarantee retained |
| T-COLL-01 | A+B | collapsed image over nontrivial domain | finite zero, complete, success, diagnostic |
| T-ISO-01 | A+B | isolated valid-domain point | finite zero, complete, success, diagnostic |
| T-EMPTY-01 | A+B | no valid-domain component | finite zero, complete, success, diagnostic |
| T-CHANGE-01 | A+B | edit causing branch/component creation or loss | semantic revision changes, obsolete entries removed, normal DAG recompute |

## 4. Direction and same-position cases

| ID | Phase | Fixture/query | Expected semantic assertion |
|---|---|---|---|
| R-FWD-01 | A+B | open component, A before B, `FORWARD` | target reached; non-negative A-to-B length |
| R-REV-01 | A+B | same component, `REVERSE` | reverse-selected route; non-negative value |
| R-COMP-01 | A+B | periodic A/B, forward and reverse | complementary routes whose sum is one fundamental-cycle length |
| R-SAME-01 | A+B | A and B same semantic position, `ZERO_LENGTH` | zero, complete, target reached; no integration work beyond validation |
| R-SAME-02 | A+B | periodic A and B same, `FULL_CYCLE` | exactly one fundamental cycle |
| R-SAME-03 | A+B | nonperiodic A and B same, `FULL_CYCLE` | invalid/unsupported query; never implicit wrap |
| R-CART-01 | A+B | A/B Cartesian equal but semantic positions differ | no same-position inference |
| R-SHORT-01 | A only | candidate `SHORTEST` comparison | characterize ambiguity/ties; remains deferred and non-default |

## 5. Open-boundary policy cases

Use the oriented branch:

```text
start --- B ------ A ----- end
```

and query A to B in `FORWARD`.

| ID | Phase | Policy/situation | Expected value and axes |
|---|---|---|---|
| O-STOP-01 | A+B | `STOP_AT_END` | length A to end; `INCOMPLETE`; `STOPPED_AT_BOUNDARY`; `targetReached=false`; scalar inadmissible |
| O-STOP-02 | A+B | downstream consumes STOP result | no generic scalar consumption; rich partial remains inspectable |
| O-WRAP-01 | A+B | explicit `WRAP_TO_START` | length A-to-end + start-to-B; complete; `WRAPPED_TO_START`; wrapped and target reached |
| O-WRAP-02 | A+B | inspect route/decomposition | two component-bounded route segments; no seam chord |
| O-WRAP-03 | A+B | geometric flags | `geometricallyConnected=false`; no incidence/topology mutation |
| O-STRICT-01 | A+B | `STRICT` | `ABSENT`, `TARGET_NOT_REACHABLE`; no partial scalar |
| O-GAP-01 | A+B | target separated by internal invalid gap under STOP | `DISCONTINUITY_ENCOUNTERED`; STOP does not reinterpret gap as global end |
| O-GAP-02 | A+B | same under WRAP | no wrap across gap; discontinuity outcome |
| O-GAP-03 | A+B | same under STRICT | unreachable/discontinuity classification per approved axis mapping; absent scalar |
| O-END-01 | A+B | A/B exactly at included endpoints | endpoint openness and `eps_domain` semantics respected without reusing it as metric tolerance |
| O-END-02 | A+B-policy | requested open endpoint/limit | supported limit, unsupported or limit-not-established is explicit |
| O-REV-01 | A+B | reverse analogues of STOP/WRAP/STRICT | symmetric route selection with correct opposite global boundary |

## 6. Complete-locus aggregation cases

| ID | Phase | Fixture | Expected semantic assertion |
|---|---|---|---|
| A-TOTAL-01 | A+B | one finite component | one contribution, complete value |
| A-TOTAL-02 | A+B | two components separated by invalid gap | `L=L_1+L_2`; no chord; two contributions plus gap diagnostic |
| A-TOTAL-03 | A+B | multiple constructive branches | each component of each branch contributes exactly once |
| A-TOTAL-04 | A+B | coincident branches/retracing | constructive multiplicity retained; no geometric deduplication |
| A-TOTAL-05 | A+B | periodic branch | exactly one fundamental cycle |
| A-TOTAL-06 | A+B | empty/isolated/collapsed family | zero, complete and distinct diagnostics |
| A-TOTAL-07 | A+B | repeat unchanged revision/policy | identical rich result/decomposition; index reuse counters as expected |
| A-TOTAL-08 | A+B | invalidate one component/revision | no obsolete result/entry; recompute required affected current state |
| A-TOTAL-09 | A+B-policy | finite + unsupported component | approved partial/absent mapping, `INCOMPLETE` and unsupported diagnostic; no scalar |
| A-TOTAL-10 | A+B-policy | finite + positive-infinite component | `POSITIVE_INFINITY` distinct from failure; decomposition retained |
| A-TOTAL-11 | A+B-policy | non-rectifiable component | `NON_RECTIFIABLE` and truthful value/status mapping |
| A-TOTAL-12 | A+B-policy | limit not established | `LIMIT_NOT_ESTABLISHED`, incomplete/absent as approved; never `DIVERGENT` |
| A-TOTAL-13 | A+B | malformed attempt to use A=B as total | rejected as wrong query kind |
| A-TOTAL-14 | A+B | attempt to pass direction to total | rejected; total has no direction input |

## 7. Unbounded and improper cases

| ID | Phase | Fixture/query | Expected semantic assertion |
|---|---|---|---|
| U-FINITE-AB-01 | A+B-policy | finite A/B segment on unbounded branch | finite proper subarc when capability supports it |
| U-FINITE-TOTAL-01 | A+B-policy | unbounded parameter with finite improper total | finite plus improper-limit provenance/guarantee |
| U-INF-01 | A+B-policy | ray/curve with infinite total variation | `POSITIVE_INFINITY`, not numerical failure |
| U-NONRECT-01 | A+B-policy | bounded/nonbounded non-rectifiable curve | `NON_RECTIFIABLE` when established |
| U-UNSUP-01 | A+B-policy | evaluator lacks defensible integration capability | `UNSUPPORTED`, not guessed chord sum |
| U-LIMIT-01 | A+B-policy | convergence cannot be established within policy | `LIMIT_NOT_ESTABLISHED` |
| U-VIEW-01 | A+B | change viewport cutoff/zoom/DPI | identical rich result and counters except unrelated render work |

## 8. Result-axis and scalar-admissibility cases

| ID | Phase | State | Expected assertion |
|---|---|---|---|
| V-FIN-01 | A+B | successful finite reached route | finite scalar candidate only if all other gates pass |
| V-ZERO-01 | A+B | valid zero | scalar zero admissible; not confused with absence/failure |
| V-INF-01 | A+B | positive infinity | rich defined result; scalar inadmissible by default |
| V-ABS-01 | A+B | absent value | no scalar; explicit cause |
| V-INC-01 | A+B | incomplete coverage | no scalar, even with finite partial value |
| V-FAIL-01 | A+B | numerical exception/failure | `NUMERICAL_FAILURE`; no stale previous value |
| V-LIMIT-01 | A+B | unresolved improper limit | `LIMIT_NOT_ESTABLISHED`, not `DIVERGENT` |
| V-UNSUP-01 | A+B | unsupported contribution | `UNSUPPORTED` with provenance |
| V-STOP-01 | A+B | STOP partial | rich result remains inspectable, scalar inadmissible |
| V-WRAP-01 | A+B | explicitly valid WRAP | scalar candidate; wrap/nonconnection metadata remains visible |
| V-STALE-01 | A+B | stale position | no scalar and no automatic rebind |
| V-DEF-01 | A+B | coherent but scalar-inadmissible rich result | `isDefined()`/rich-defined policy remains distinct from scalar gate |
| V-NUM-01 | A | options A/B/C across generic numeric consumers | no consumer receives inadmissible scalar or loses rich semantics |
| V-CAS-01 | A | CAS and symbolic/numeric coercion audit | no accidental numeric authority or unsupported CAS claim |
| V-ALG-01 | A+B | Algebra View formatting | shows status/value/diagnostics truthfully without public command implication |

## 9. Lifecycle, mutation and exception cases

| ID | Phase | Event | Expected assertion |
|---|---|---|---|
| L-CREATE-01 | A+B | create algorithm/output | coherent initial payload; registered normal inputs/outputs |
| L-UPD-01 | A+B | ordinary upstream value edit | one current revision/result after DAG update |
| L-UNDEF-01 | A+B | upstream becomes undefined | old success not current; coherent failure/absence payload |
| L-RECOVER-01 | A+B | upstream recovers | new current result; no stale index/binding |
| L-TOPO-01 | A+B | topology/branch change | explicit invalidation and diagnostics |
| L-COPY-01 | A+B | copy/copyInternal | no foreign Construction, stale revision/index/binding or partial result |
| L-SET-01 | A+B | set from compatible/incompatible source | approved safe semantics or deterministic unsupported behavior |
| L-REMOVE-01 | A+B | remove metric/locus | no retained GeoElement, algorithm, Construction or obsolete index |
| L-UNDO-01 | A+B | undo/redo create/edit/remove | normal DAG reconstruction; deterministic result |
| L-LABEL-01 | A+B | rename/label | identity/provenance does not depend on label |
| L-LIST-01 | A+B | list/sequence use | no silent scalar coercion; lifecycle policy honored |
| L-STYLE-01 | A+B | defaults/style/selection | deliberate behavior; no accidental NUMERIC/LOCUS classification |
| L-EXC-01 | A+B | evaluator throws during private build | `finally` cleanup; no partial index entry; coherent current-revision rich failure snapshot |
| L-EXC-02 | A+B | integrator/aggregator throws | atomic failure payload; old success not relabeled current |
| L-ALIAS-01 | A+B | mutate source arrays/lists after construction | keys/results/decomposition unchanged |

## 10. Index strategy and bounded-state cases

Run every applicable row with `REFERENCE_NO_INDEX_REUSE`,
`EAGER_WHOLE_REVISION` and `LAZY_COMPONENT_REVISION`.

| ID | Phase | Query sequence | Hard assertion |
|---|---|---|---|
| I-SAME-01 | A | 1/10/100 same A/B | equal rich results; calls/builds/hits reported |
| I-OVER-01 | A | overlapping arcs | reusable component work measured; no semantic coupling of routes |
| I-REV-01 | A | forward then reverse | component contribution reuse only when key/capability permits |
| I-PER-01 | A | periodic arcs/full cycle | one-cycle normalization and reuse counters |
| I-OPEN-01 | A | STOP/WRAP/STRICT | route policy does not contaminate component index semantics |
| I-TOTAL-01 | A | 1/10/100 total | stable decomposition and bounded reuse |
| I-TOL-01 | A | change abs/rel tolerance | complete-key miss/invalidation; no incompatible reuse |
| I-POL-01 | A | change metric/multiplicity/improper policy | complete-key miss |
| I-REVISION-01 | A+B | semantic revision changes | no obsolete retained revision |
| I-EVICT-01 | A+B | capacity+1 deterministic components | specified victim and bounded retained entries |
| I-OFF-01 | A+B | index on versus off | exact semantic equality of full rich result |
| I-FAIL-01 | A+B | failed entry build | miss remains unpublished; retained count consistent |
| I-GLOBAL-01 | A+B | two Constructions/loci | no global/static state or cross-Construction leakage |

G7B runs the same hard assertions only for the strategy and budgets approved at
G7A closeout; the no-reuse oracle remains available to tests.

## 11. Nested metric composition

Required graph:

```text
L1
 -> metric(L1)
 -> L2
 -> metric(L2)
 -> L3
```

| ID | Phase | Scenario | Hard assertion |
|---|---|---|---|
| N-REP-01 | A+B | repeat metric(L1) at same revision/policy | no redundant whole-component/index build |
| N-DOWN-01 | A+B | L2 evaluator consumes admissible metric(L1) | normal DAG dependency; zero render/legacy access |
| N-THREE-01 | A+B | three-level graph above | no whole-locus regeneration or metric-index build per downstream point |
| N-TOTAL-01 | A+B | repeated upstream total consumed downstream | same-revision/policy reuse; scalar gate enforced |
| N-INVALID-01 | A+B | edit L1 input | normal ordered invalidation through all levels |
| N-CACHEOFF-01 | A+B | index/session on versus off | same rich semantic outputs |
| N-WASTE-01 | A | trace recomputation | label necessary evaluator work separately from redundant build/integration |
| N-STOP-01 | A+B | partial STOP reaches numeric consumer | inadmissible scalar blocks downstream numeric use without stale value |

## 12. Legacy and scientific evidence

| ID | Phase | Evidence | Required use |
|---|---|---|---|
| E-LEGACY-01 | A | `AlgoPerimeterLocus` | characterize sampled chord authority; never feed V2 |
| E-TEMPLATE-01 | A | `Templatev7.ggb` `listLength`/`listLength12`/`postLocus` | preserve original; document sampled/filter behavior |
| E-CEDG-02 | A | `InterCilConoObliqueTwoLevels.ggb` | functional two-level control and candidate bounded pilot extraction |
| E-CEDG-03 | A | `InterCilConoOblique.ggb` | pathological third-level legacy evidence; no required full migration |
| E-PILOT-01 | A+B | segment/circle | analytic minimum |
| E-PILOT-02 | A+B-policy | small deterministic cylinder development | scientific bounded pilot |
| E-PILOT-03 | A+B | traced nested metric fixture | dynamic composition pilot |
| E-PILOT-04 | A+B-policy | bounded oblique-cone subset if reproducible | scientific stress without mutating original |

Before and after characterization, verify the manifest SHA-256 values of all
legacy originals. Generated derivatives, if authorized, use new paths and
explicit provenance.

## 13. Public and packaging boundary tests

| ID | Phase | Assertion |
|---|---|---|
| B-CMD-01 | A+B | no `LocusLength` registration and no `Length`/`Perimeter` behavior change |
| B-PATH-01 | A+B | no public `Path` or point-on-metric/locus addition |
| B-XML-01 | A+B | no XML/factory registration or persistence |
| B-3D-01 | A+B | no metric 3D behavior or dispatch |
| B-G5-01 | A+B | no G5 export policy change |
| B-G8-01 | A+B | no intersection implementation |
| B-LAB-01 | B | developer laboratory is explicit opt-in and disabled by default |
| B-PKG-01 | A+B | no external numeric library/native code; ordinary package boundary remains valid |
| B-CLASS-01 | A+B | append-only candidate GeoClass; legacy ordinals and classifications preserved |

## 14. G7A evidence and promotion gate

G7A saved:

- machine/JDK/Gradle/baseline metadata;
- exact source/probe/test paths and hashes;
- raw functional-counter traces for all three index strategies;
- tolerance/error/improper-limit experiments and independent references;
- upstream `GeoClass`/GeoElement/numeric/Algebra/CAS/lifecycle audit;
- lifecycle, exception, aliasing, repeated and nested results;
- legacy original hash verification;
- a requirement-to-evidence traceability matrix;
- explicit resolved decisions and author-approved G7B budgets.

No row may be marked pass from prose alone. G7A passed by author approval after
the evidence ran. G7B now has productive test and audit evidence; final author
review remains required.

## 15. Executed G7A and R1 coverage

The executable characterization currently covers 51 test-private cases:

| Suite | Cases | Matrix areas |
|---|---:|---|
| semantic/routes/results | 10 | position, topology, direction, same-position, STOP/WRAP/STRICT, total and scalar axes |
| numerical methods | 10 | variation, analytic/differential, upstream Gauss comparison, reparameterization, tolerance grid, evaluator-only alias, improper and arc coordinate |
| index strategies | 8 | 1/10/100, route traces, total, full keys, capacity, eviction, failure, ON/OFF |
| nested composition | 4 | repeated, invalidation, cache-off and policy change |
| Geo lifecycle | 5 | creation/publication, rich-vs-scalar, copy/set, removal/recovery and atomic failure |
| R1 safe value/error/work budget | 6 | closed values, evidence states, G6 guarantee reuse and independent work ceilings |
| R1 multi-consumer ownership | 8 | N=1/3/10/100, query order, full-key isolation, lifecycle, Construction isolation and nesting |

Independent expected values cover ellipse, exponential graph, parabola, cusp
and the evaluator-alias stress case at 80 decimal digits. Legacy/source audits
cover the remaining non-executable G7A rows. Exact requirement-to-evidence
mapping is in
[`g7a_locus_v2_metric_traceability_matrix.md`](g7a_locus_v2_metric_traceability_matrix.md).

This remains author-approved characterization coverage. Productive G7B
conformance is recorded separately below.

## 16. Focused G7A-R1 matrix

The following IDs are separately emitted by the R1 probes/evidence even when a
single parameterized Java test covers several rows:

| ID | Phase | Fixture/transition | Hard assertion |
|---|---|---|---|
| R1-VALUE-ABSENT | A-R1+B | finite/infinity/absent values | closed immutable `MetricValue2D`; no NaN, sentinel or bare absent value access |
| R1-ERROR-NONE | A-R1+B | exact/certified/estimated/unknown/not-applicable | closed immutable amount variants and scope; zero/NaN never means unknown |
| R1-GUARANTEE-G6 | A-R1+B | every guarantee state | direct `LocusSemanticMetadata2D.NumericGuarantee` reuse; no metric duplicate enum |
| R1-WORK-BUDGET | A-R1+B | ellipse/parabola/transcendental/cusp/reparameterized/improper/evaluator-only | independent deterministic evaluation, subdivision and depth ceilings; exhaustion is typed `LIMIT_NOT_ESTABLISHED` |
| R1-MULTI-1 | A-R1+B | 1 compatible consumer | one unique build, zero duplicate build |
| R1-MULTI-3 | A-R1+B | 3 compatible consumers | one unique build, two cross-result hits |
| R1-MULTI-10 | A-R1+B | 10 compatible consumers | one unique build, nine cross-result hits |
| R1-MULTI-100 | A-R1+B | 100 compatible consumers | one unique component-state build, 99 cross-result hits; local comparator reports 99 duplicates |
| R1-MULTI-TOTAL-FIRST | A-R1+B | total then local queries over C1/C2/C3 | exactly three unique component builds and order-independent rich values |
| R1-MULTI-LOCAL-FIRST | A-R1+B | local queries then total over C1/C2/C3 | exactly three unique component builds and order-independent rich values |
| R1-MULTI-POLICY | A-R1+B | capability/algorithm/tolerance/multiplicity/improper/work-budget changes | same complete key shares; each result-affecting change misses |
| R1-MULTI-REVISION | A-R1+B | value-only revision/undefined/recovery | old entries unreachable; first consumer builds once, later compatible consumers hit |
| R1-MULTI-TOPOLOGY | A-R1+B | split/merge/branch disappearance/reappearance | no old component/coordinate/lineage reuse |
| R1-MULTI-CONSTRUCTION | A-R1+B | two loci in one Construction and look-alike IDs in two Constructions | zero cross-locus and cross-Construction sharing; no static registry |
| R1-MULTI-REMOVE | A-R1+B | consumer and source-locus removal | final consumer releases entries; locus removal releases owner and strong references |
| R1-MULTI-NESTED | A-R1+B | L1 multi-results → L2 multi-results → L3 | zero duplicate compatible builds, render/sample/regeneration/per-point-index counters all zero |
| CLOSEOUT-COMPONENT-STATE | A-closeout+B | overlapping subarcs and total over one component | key has no endpoints; owner returns immutable component state; distinct route contributions are derived after lookup |
| CLOSEOUT-TRAVERSAL-OPTIONAL | A-closeout+B | total and between rich results | between outcome present where required; total outcome structurally absent; no null/sentinel |
| CLOSEOUT-ERROR-CLOSED | A-closeout+B | established/not-established/not-applicable errors | sealed variants make contradictory state/amount pairs unrepresentable |

The accepted ownership is `DEDICATED_SHARED_OWNER`. G7B retains
`REFERENCE_NO_INDEX_REUSE` as the full rich
semantic oracle and shall use capacity 64 only as a provisional per-active-
locus implementation default. The productive analytic fixture reports a
deterministic logical retained-state estimate of 336 bytes; this is not a JVM
heap or object-layout measurement, is not a universal entry-size claim and does
not stabilize capacity 64.

## 17. Productive G7B conformance suites

The focused verifier executes 62 productive common-kernel cases and three
developer-laboratory contract cases:

| Suite | Tests | Productive evidence |
|---|---:|---|
| value | 8 | closed values/errors, orthogonal axes, aggregate precedence, defensive immutability |
| route | 11 | bindings, stale/rebind, FORWARD/REVERSE, ZERO/FULL, STOP/WRAP/STRICT, seam and gap exclusion |
| numerical | 17 | total/components/multiplicity, analytic/differential/evaluator-only, segment/circle/ellipse/parabola/exponential/cusp, reparameterization, work ceilings, truthful subarc guarantee and index oracle |
| improper | 6 | finite unbounded A/B, convergent improper, infinity, non-rectifiable, unsupported and limit not established |
| lifecycle | 11 | GeoClass, rich Geo/P1, scalar adapter, copy/set, XML absence, leases, thread-confined owner, undefined/recovery/removal and failed-build cleanup |
| benchmark | 7 | 1/10/100, overlap/reverse/periodic/STOP/WRAP/STRICT, repeated total, policy/revision, query order and owner isolation |
| nested | 2 | three-level normal DAG, downstream evaluation and invalidation with all forbidden-work counters zero |
| laboratory | 3 | opt-in fixtures, public-boundary isolation and temporary settings |

The 62+3 inventory is reproduced from the authoritative JUnit XML and focused
verifier. The author accepts this matrix as final G7 conformance evidence. The
[G7B report](g7b_locus_v2_metric_kernel_report.md) and
[traceability](g7b_locus_v2_metric_traceability_matrix.md) identify each
assertion and source.

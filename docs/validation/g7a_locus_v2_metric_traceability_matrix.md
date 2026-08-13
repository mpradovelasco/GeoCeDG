# G7A Locus V2 metric traceability matrix

- Status: **PASS — AUTHOR APPROVED**
- Scope: G7A test-private characterization and the implemented G7B conformance gates
- G7 specification: `NORMATIVE / AUTHOR APPROVED`
- ADR 0007: `Accepted`
- Date: 2026-08-13

The sources below are the versioned
[G7 plan](../roadmap/g7_locus_v2_metrics_plan.md),
[semantic model](../architecture/locus_v2_metric_semantic_model.md),
[architecture](../architecture/locus_v2_metric_architecture.md),
[normative specification](../../geocedg/specs/locus/locus-v2-metrics.md),
[ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md),
[validation matrix](g7_locus_v2_metric_validation_matrix.md),
[benchmark plan](g7_locus_v2_metric_benchmark_plan.md),
[G7A report](g7a_locus_v2_metric_characterization_report.md),
[focused R1 report](g7a_r1_locus_v2_metric_refinement_report.md) and
[author-approved internal API](../developer/locus_v2_metric_api.md).

### Accepted binding

Every D01–D42 and R1-1–R1-22 decision is `APPROVED` and inherits these five
mandatory targets in addition to its row-specific cells:

- [normative G7 metric spec](../../geocedg/specs/locus/locus-v2-metrics.md);
- [Accepted ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md);
- [G7A raw evidence](../../geocedg/validation/locus-v2/g7a/g7a-characterization-evidence.json);
- [G7A-R1 raw evidence](../../geocedg/validation/locus-v2/g7a-r1/g7a-r1-characterization-evidence.json);
- [executed G7B implementation gate](../../.github/prompts/tasks/g7b-locus-v2-metric-kernel.prompt.md).

| ID | requirement | source | decision | normative spec | ADR | fixture | probe | evidence | G7B gate | user guide |
|---|---|---|---|---|---|---|---|---|---|---|
| D01 | Total variation defines length | G7 plan §4; G6 normative spec | retain supremum/integral authority | §2 | boundary only | parabola partitions | numerical test | report §3 | no chord/render authority | G7A metrics section |
| D02 | Complete query is separate and endpoint-free | G7 plan §5.2 | `TotalLocusMetricQuery` | §4.2 | supports component reuse | total-query value | semantic test | report §4 | type cannot carry A/B/direction | G7A metrics section |
| D03 | Constructive multiplicity | author planning decision | `CONSTRUCTIVE_TRAVERSAL_LENGTH` | §3 | key includes multiplicity | coincident branches | aggregate test | report §4 | count every preimage/contribution | scientific semantics |
| D04 | Disconnected aggregation | semantic model §8 | sum components, no chord/route | §8, §10 | component entries only | split domain | route + aggregate tests | report §4 | gap diagnostics/decomposition | scientific semantics |
| D05 | Empty/isolated/collapsed | planning §14 | finite zero + complete + diagnostic | §8 | no special cache truth | three zero cases | aggregate test | report §4 | zero must remain admissible | edge-case table |
| D06 | Infinity/nonrectifiable/unsupported distinct | planning §15 | orthogonal axes | §9 | no conflated index status | line/oscillation/unsupported | numerical + aggregate tests | report §§3,7 | no generic `DIVERGENT` | result taxonomy |
| D07 | Durable semantic position | planning §8 | locus/branch/provider/parameter | §5 | component key excluded | self-intersection | semantic test | report §5 | immutable value contract | position explanation |
| D08 | Revision binding separate | planning §8 | revision/component/status in binding | §5 | revision-scoped key | component split | semantic test | report §5 | binding resolver required | position explanation |
| D09 | Stale requires explicit rebind | planning §8 | `POSITION_STALE`; no repair | §5 | stale key never hits | revision transition | semantic + lifecycle tests | report §5 | clear stale current state | dynamic behavior |
| D10 | FORWARD route | planning §9 | required | §6 | same component entry reusable | open line | route test | report §6 | nonnegative value | traversal table |
| D11 | REVERSE route | planning §9 | required | §6 | same component entry reusable | open/periodic | route test | report §6 | nonnegative value | traversal table |
| D12 | SHORTEST | planning §9 | defer | §6 deferred | no key policy yet | CeDG pilot review | source audit | report §6 | absent from minimum API | deferred note |
| D13 | ZERO/FULL_CYCLE explicit | planning §10 | distinct policy; FULL periodic only | §7 | one cycle contribution | same-position open/periodic | route test | report §6 | reject implicit Cartesian equality | same-position table |
| D14 | STOP | planning §11 | partial, target not reached | §7 | index may serve segment | `A -> end` | route test | report §6 | rich-only scalar state | policy table |
| D15 | WRAP | planning §11 | two segments; disconnected | §7 | reuse each segment; no gap | open branch | route test | report §6 | exact flags and no incidence | policy table |
| D16 | STRICT | planning §11 | absent unreachable result | §7 | reject before integration | reverse target | route/index tests | report §§6,11 | required minimum policy | policy table |
| D17 | Route resolver does not integrate | planning §12 | dedicated resolver/route/segment | §6 | route precedes index | route matrix | semantic model probe | report §6 | architectural separation test | future architecture |
| D18 | Rich orthogonal result axes | planning §15 | retain all axes/metadata | §9 | cached component state defensive | combined states | semantic result probe | report §7 | immutable exhaustive values | result taxonomy |
| D19 | Aggregate precedence | planning §16 | known infinity + incomplete unresolved; weakest status | §10 | deterministic decomposition | infinity + unsupported | aggregate test | report §7 | order-independent compensated sum | aggregate explanation |
| D20 | Scalar admissibility | planning §§20,34 | strict matrix and guarantee gate | §12 | not an index concern | 13 state matrix | semantic test + source audit | report §8 | explicit predicate tested | scalar table |
| D21 | Rich Geo in DAG | author decision | `GeoLocusMetricResult` authority | §11 | shared index remains non-Geo derived state | lifecycle candidate | lifecycle test | report §§8,13; R1 §7 | normal `AlgoElement` publication | future architecture |
| D22 | Dedicated GeoClass | planning §18 | append `LOCUS_METRIC_RESULT` | §11 | none | enum/switch audit | source audit | report §13 | preserve all ordinals | internal classification |
| D23 | Numeric participation | planning §§19–20 | C explicit adapter; B rejected | §12 | adapter owns no index | 219/72 source surface | source audit | report §8.2 | no rich `NumberValue` | scalar explanation |
| D24 | Analytic integration | planning §24 | first capability | §10 | cache analytic component state | segment/circle | numerical test | report §9 | truthfully classified guarantee | methods table |
| D25 | Differential integration | planning §24 | per-call deterministic adaptive integrator | §10, §15 | component-local build | ellipse/parabola/transcendental/cusp | numerical test | report §9 | typed error/ceiling/counters | methods table |
| D26 | Evaluator-only guarantee | planning §24 | uncertified/unsupported absent assumptions | §10 | no false certified entry | 64-cycle alias | numerical test | report §9 | refinement agreement is no bound | warning note |
| D27 | Metric tolerance | planning §34; R1-D | abs `1e-10`, rel `1e-9`; work policy adds eval/subdivision/depth | §15 | tolerance and work budget in full key | scale/work grid | numerical + R1 work tests | report §9; R1 §5 | independent from G6/render/G8 | tolerance table |
| D28 | Error aggregation | planning §§15,34 | sum absolute; derive relative; weakest guarantee | §10, §15 | entry stores error | mixed guarantees | aggregate test | report §7 | deterministic aggregate error | result taxonomy |
| D29 | Improper limits | planning §26 | transform/tail proof or typed uncertainty | §16 | improper policy in key | line/convergent/oscillatory | numerical test | report §10 | no viewport cutoff | unbounded note |
| D30 | Index strategy | planning §§22,36; R1-E | accept `LAZY_COMPONENT_REVISION`, separately from ownership | §14 | Accepted strategy | 1/10/100 and multi-consumer traces | index + R1 ownership tests | report §11; R1 §§6–7 | same traces for all strategies/owners | internal architecture |
| D31 | Index lifecycle | planning §23; R1-E/F | accept `DEDICATED_SHARED_OWNER`, current revision, per-locus bounded insertion-order eviction, P1/finally | §13–§15 | Accepted ownership | capacity/failure/invalidate/remove | index + R1 lifecycle tests | report §11; R1 §§7–8 | no obsolete/partial entries or stale success | internal architecture |
| D32 | Repeated-query budget | planning §28; R1 strong criterion | one build for 100 compatible consumers; 3 for total fixture | §17 | core ADR benefit | same/total/order/policies | index + R1 ownership tests | report §11; R1 §6 | functional counters hard | performance note |
| D33 | Nested metric budget | planning §29; R1 nested | one build/unique key across outputs, none per point | §17 | shared owner reuse | L1 multi-results→L2 multi-results→L3 | nested + R1 ownership tests | report §12; R1 §6.3 | zero duplicates/render/sample/regeneration | nested explanation |
| D34 | Arc coordinate | planning §27 | internal component data only | §16 | stored in component entry | 100 A/B reuse | numerical/index tests | report §10 | no Path/identity/gap bridge | internal note |
| D35 | Units | planning §15 | explicit construction length unit | §9 | units/policy provenance in entry | scaled ellipse | numerical test | report §9 | no silent conversion | units note |
| D36 | Thread confinement | planning §23, §27 | kernel thread; no concurrency/background | §14 | owner thread only | nested active-build trace | nested/index tests | report §12 | max active build 1 | internal note |
| D37 | Public boundary | planning §32 | internal Java + rich Geo + lab | §18 | no public cache | packaging/source audit | verifier | report §§13–14 | no command/XML/Path/3D | characterization warning |
| D38 | CeDG pilots | planning §31 | segment/circle, small cylinder, nested, feasible bounded cone | §17 | pilots exercise reuse | hash-pinned models | legacy/source audit | report §15 | originals not build dependencies | scientific pilots |
| D39 | Productive file/class plan | planning §28; R1 source audit | GeoCeDG values/index/shared owner + rich Geo/algo/adapter | §11, §14 | dedicated owner localized | source map | source audit | report §14; R1 §9 | narrow `GeoLocusV2` lifecycle hooks; no Construction repository | future architecture |
| D40 | Hardening from first candidate | planning §33 | immutable/bounded/atomic/ON-OFF/repeated/nested | §§14,17 | core lifecycle | failure/cache traces | all G7A probes | report §§11–12 | no deferred basic hardening | hardening note |
| D41 | ADR 0007 disposition | final author closeout | `Accepted` | §19 | entire Accepted ADR | all index traces | index test | reports §§11,16 and R1 | accepted entry gate | phase status |
| D42 | G7 spec disposition | final author closeout | normative/author-approved | §19 | Accepted ADR | complete G7A package | focused verifier | reports §§16–17 and R1 | normative entry gate | phase status |

## Focused G7A-R1 traceability

Every R1 decision is author-approved and inherits the Accepted binding above.

| ID | requirement | source | decision | normative spec | ADR | fixture | probe | evidence | G7B gate | user guide |
|---|---|---|---|---|---|---|---|---|---|---|
| R1-1 | safe finite/infinity/absent representation | R1-A | V2 closed immutable `MetricValue2D` | §9 | semantic boundary | three value variants | R1 value test | R1 report §3; raw `safeContracts` | no sentinel/bare absent access | R1 future architecture |
| R1-2 | safe metric error evidence | R1-B | typed amount states, scope and metadata | §9–§10 | immutable entry evidence | exact/certified/estimated/unknown/NA | R1 evidence test | R1 report §4; raw `safeContracts` | no NaN/negative/zero unknown sentinel | R1 future architecture |
| R1-3 | guarantee alignment | R1-C; normative G6 source | reuse `LocusSemanticMetadata2D.NumericGuarantee` directly | §9 | complete-key evidence | all four G6 values | R1 guarantee test + source audit | R1 report §§2,4 | no duplicate metric enum | R1 future architecture |
| R1-4 | deterministic work-budget shape | R1-D | independent evaluations/subdivisions/depth | §15 | full key includes budget | numeric fixture grid | R1 work test | R1 report §5; raw `deterministicWork` | deterministic typed exhaustion | R1 future architecture |
| R1-5 | initial work defaults | R1-D; measured work | 32768/16384/22 initial versioned policy | §15 | policy identity | nine work traces | R1 work test | R1 report §5 | implementation defaults; not mathematical constants | R1 future architecture |
| R1-6 | algo-local cross-result cost | R1 §§10,13 | classify N builds as duplicate work | §14 | rejected owner baseline | N=1/3/10/100 | R1 ownership test | R1 report §6.1 | local mode not optimized path | R1 future architecture |
| R1-7 | locus-attached candidate | R1-M1 | shares but couples derived index to Geo state | §14 | ownership alternative | lifecycle scorecard | R1 ownership model + source audit | R1 report §7 | reject direct index-in-Geo shape | R1 future architecture |
| R1-8 | Construction-scoped candidate | R1-M2 | shares but broadens retention/capacity surface | §14 | ownership alternative | multi-locus/Construction | R1 ownership test + source audit | R1 report §7 | no central repository | R1 future architecture |
| R1-9 | dedicated shared candidate | R1-M3 | non-Geo per-locus derived owner | §14 | accepted owner | same-key multi-consumer | R1 ownership test | R1 report §7; raw `multiConsumer` | no hidden DAG/global state | R1 future architecture |
| R1-10 | selected ownership | R1 §21 | `DEDICATED_SHARED_OWNER` | §14 | Accepted | full ownership scorecard | R1 ownership suite | R1 report §7 | share only component state | R1 future architecture |
| R1-11 | cross-result build budget | R1 §§13,37 | one component-state build/complete key until eviction/invalidation | §14, §17 | core benefit | N=100 | R1 ownership test | R1 report §6.1 | 1 build + 99 hits | R1 future architecture |
| R1-12 | memory/capacity ownership | R1 §§19,26 | one capacity per active locus owner; 64 provisional | §14 | capacity non-normative | N=1/3/10/100 bytes | R1 ownership test | R1 report §§6.1,7 | report payload/metadata/owner/consumer bytes | R1 future architecture |
| R1-13 | eviction | R1 §26 | deterministic insertion FIFO; no time LRU | §14 | lifecycle rule | bounded owner overflow | existing index + R1 owner tests | R1 report §7 | deterministic victim and bounded state | R1 future architecture |
| R1-14 | revision invalidation | R1 §17 | remove old revision; first builds, rest hit | §13–§14 | current revision only | r→r+1 | R1 ownership/lifecycle tests | R1 report §6.2 | one affected rebuild | R1 future architecture |
| R1-15 | topology invalidation | R1 §17 | no split/merge/disappearance lineage reuse | §14 | revision-scoped component key | split/merge/branch cycle | R1 ownership test | R1 report §6.2 | no coordinate repair/stale hit | R1 future architecture |
| R1-16 | multi-Construction isolation | R1 §16 | concrete locus/Construction ownership | §14 | no global repository | same-looking IDs | R1 ownership test | R1 report §6.2 | zero cross-locus/Construction share | R1 future architecture |
| R1-17 | remove/undo lifecycle | R1 §16 | lease release; final consumer/locus clears; redo reacquires | §13–§14 | owner lifecycle | consumer/source removal | R1 ownership test + source audit | R1 report §7 | no retained strong reference | R1 future architecture |
| R1-18 | nested multi-consumer | R1 §18 | zero compatible duplicate builds | §17 | shared owner per level | L1 multi→L2 multi→L3 | R1 nested ownership test | R1 report §6.3 | all forbidden counters zero | R1 future architecture |
| R1-19 | atomic failure publication | R1-F | P1 coherent current-revision absent failure | §13 | no failed index entry | success r→failure r+1 | lifecycle test | R1 report §8 | scalar undefined; no stale/hybrid/partial | R1 future architecture |
| R1-20 | ADR disposition | final author closeout | Accepted; strategy separate from owner | §19 | ADR 0007 Accepted | complete R1 evidence | focused verifier | R1 report §10 | Accepted entry gate | phase status |
| R1-21 | spec disposition | final author closeout | normative/author-approved | §19 | Accepted ADR | synchronized package | focused verifier | R1 report §10 | normative entry gate | phase status |
| R1-22 | optimized G7B source impact | R1 §§9,22 | new GeoCeDG owner/values plus narrow locus/algo/GeoClass/lab edits | §11, §14 | owner package | real-source audit | source audit | R1 report §9 | no Construction repo/command/XML/Path/3D/G8 | R1 future architecture |

## Final API-normalization traceability

| ID | requirement | source | decision | normative spec | ADR | fixture | probe | evidence | G7B gate | user guide |
|---|---|---|---|---|---|---|---|---|---|---|
| CLOSEOUT-1 | component metric state is not a route-specific contribution | final author closeout | complete component key → immutable `LocusMetricComponentState2D`; state + segment/extent → `LocusMetricContribution2D`; owner shares state only | §§10,14 | Decision; Complete key; Semantic boundaries | total-first and overlapping subarcs | R1 multi-consumer regression | G7A report §16.1; R1 report §7.1; both raw evidence files | mandatory state/contribution boundary and one-state-build budget | future architecture only |
| CLOSEOUT-2 | traversal outcome structurally optional | final author closeout | between-position contains required outcome; total contains none | §§4,9 | semantic boundary | total plus between result matrix | semantic regression | G7A report §16.1; R1 report §7.1; both raw evidence files | `Optional<TraversalOutcome>` or equivalent type split; no null/sentinel | future architecture only |
| CLOSEOUT-3 | closed error amount | final author closeout | sealed established/not-established/not-applicable variants; reuse G6 guarantee | §§9–10 | immutable component state/error evidence | exact/estimated/uncertified/NA | value/error regression | G7A report §16.1; R1 report §4; both raw evidence files | contradictory state/amount unrepresentable | future architecture only |

## Verification mapping

The focused verifier executes the five original and two R1 test suites, verifies the independent
reference generator and SHA-256 manifest, checks the executed prompt hash,
rejects productive `src/main` changes relative to the planning baseline, runs
test checkstyle and `git diff --check`, and restores generated outputs. The
composed `verify.ps1` calls it after the G6 Locus V2 authority.

```text
G7A-R1 = PASS — AUTHOR APPROVED
G7A = PASS — AUTHOR APPROVED
G7 METRIC SPEC = NORMATIVE / AUTHOR APPROVED
ADR 0007 = ACCEPTED
G7B = PASS — AUTHOR APPROVED
G7 = PASS
G7B CAPACITY 64 = PROVISIONAL NON-NORMATIVE IMPLEMENTATION DEFAULT
G8 = NOT STARTED
G9 = NOT STARTED
```

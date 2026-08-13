# G7B Locus V2 metric requirement-to-evidence traceability

| ID | Requirement | Authority | Productive implementation | Probe/evidence | Review gate |
|---|---|---|---|---|---|
| G7B-01 | total variation is mathematical authority | G7 spec §2 | capability hierarchy and component evaluation | numerical suite; independent references | algorithm never defines length |
| G7B-02 | no render/legacy/viewport metric authority | G7 spec §2 | metric package has no Euclidian dependency | nested counters and forbidden-import audit | all forbidden counters zero |
| G7B-03 | constructive traversal multiplicity | G7 spec §3 | total iterates branch/component provenance | numerical coincident-branch fixture | both constructive branches count |
| G7B-04 | separate between/total queries | G7 spec §4 | sealed query hierarchy | route/numerical suites | total has no A/B/direction |
| G7B-05 | durable position identity | G7 spec §5 | `LocusSemanticPosition2D` | route self-X/provider tests | revision/component excluded |
| G7B-06 | revision-scoped binding | G7 spec §5 | binder and `MetricPositionBinding2D` | lifecycle stale/rebind tests | explicit rebind only |
| G7B-07 | no coordinate repair | G7 spec §5 | binder uses branch/provider parameter | route topology tests | stale is typed |
| G7B-08 | route resolver separated from metric | G7 spec §6 | `LocusMetricRouteResolver2D` | route suite | resolver performs zero integration |
| G7B-09 | FORWARD and REVERSE | G7 spec §6 | traversal direction | route/benchmark tests | non-negative values |
| G7B-10 | SHORTEST deferred | G7 spec §6 | no SHORTEST enum value | source audit | absent from minimum |
| G7B-11 | ZERO/FULL_CYCLE | G7 spec §6 | same-position policy | route suite | full only periodic |
| G7B-12 | STOP partial | G7 spec §6 | boundary route/outcome | route and benchmark suites | incomplete, not scalar |
| G7B-13 | WRAP convention | G7 spec §6 | two disconnected route segments | route/benchmark suites | target reached, disconnected |
| G7B-14 | STRICT unreachable | G7 spec §6 | pre-metric route rejection | benchmark 100-query trace | zero builds/misses |
| G7B-15 | no internal-gap traversal | G7 spec §6 | component-local segments | route gap fixture | discontinuity typed |
| G7B-16 | closed metric value | G7 spec §7 | sealed finite/infinity/absent variants | value suite | no sentinel/NaN/null |
| G7B-17 | closed error amount | G7 spec §8 | three error-amount variants | value suite | contradictory state impossible |
| G7B-18 | reuse G6 NumericGuarantee | G7 spec §8 | direct metadata type import | value/numerical suites | no duplicate enum |
| G7B-19 | orthogonal rich axes | G7 spec §9 | `LocusMetricResult2D` | difficult aggregate tests | axes not collapsed |
| G7B-20 | optional traversal | G7 spec §9 | `Optional<TraversalOutcome>` | total/between tests | total empty, between present |
| G7B-21 | contribution decomposition | G7 spec §9 | immutable contribution list | total/aggregate tests | provenance retained |
| G7B-22 | analytic capability | G7 spec §10 | analytic evaluator/capability | segment/circle tests | exact evidence when proven |
| G7B-23 | differential quadrature | G7 spec §10 | deterministic adaptive Simpson | ellipse/parabola/exponential/cusp | estimated error and ceilings |
| G7B-24 | truthful evaluator-only | G7 spec §10 | evaluator-only policy | alias/assumption tests | never certified by agreement |
| G7B-25 | unsupported legitimate | G7 spec §10 | unsupported capability | improper/value suites | rich unsupported result |
| G7B-26 | independent metric tolerances | G7 spec §11 | versioned policy defaults | numerical suite | abs 1e-10, rel 1e-9 |
| G7B-27 | independent work ceilings | G7 spec §11 | `MetricWorkBudget2D` | three exhaustion fixtures | typed limit identity |
| G7B-28 | component state != contribution | ADR 0007 | state builder then segment evaluation | overlap/total/multi-consumer | owner shares state only |
| G7B-29 | complete endpoint-free key | ADR 0007 | `LocusMetricIndexKey2D` | key/policy tests | overlapping arcs reuse |
| G7B-30 | lazy component/revision strategy | ADR 0007 | index + engine mode | benchmark suite | one build per compatible key |
| G7B-31 | dedicated per-locus owner | ADR 0007 | `LocusMetricSharedOwner2D` | lifecycle/multi-consumer | no global/Construction owner |
| G7B-32 | owner leases | ADR 0007 | `LocusMetricOwnerLease2D` | lifecycle removal tests | final lease releases |
| G7B-33 | bounded deterministic eviction | ADR 0007 | capacity 64 insertion-order map | 65-key test | retained=64, evictions=1 |
| G7B-34 | current-revision retention | ADR 0007 | invalidation seam on source | revision tests | obsolete retained=0 |
| G7B-35 | transaction/exception safety | ADR 0007 | private build + finally cleanup | injected failure tests | no failed entry/active build |
| G7B-36 | cache-off oracle | ADR 0007 | `REFERENCE_NO_INDEX_REUSE` | numerical equality test | full rich equality |
| G7B-37 | aggregate precedence | G7 spec §13 | `LocusMetricAggregator2D` | finite/mixed/infinite tests | deterministic diagnostics |
| G7B-38 | empty/isolated/collapsed zero | G7 spec §14 | zero state/result factories | numerical/value tests | finite complete zero |
| G7B-39 | improper/unbounded taxonomy | G7 spec §15 | analytic typed outcomes | improper suite | no viewport cutoff |
| G7B-40 | component-local arc coordinate | G7 spec §16 | cumulative evidence in state | overlap/reverse tests | not semantic identity/Path |
| G7B-41 | rich normal-DAG Geo | G7 spec §12 | `GeoLocusMetricResult` + Algo | lifecycle/nested tests | current revision published |
| G7B-42 | append-only dedicated GeoClass | G7 spec §12 | ordinal 131 after Locus V2 | lifecycle/enum tests | no class reuse |
| G7B-43 | P1 current-revision failure | G7 spec §12 | begin/publish failure sequence | lifecycle cascade/failure | no stale success |
| G7B-44 | explicit scalar adapter C | G7 spec §12 | `AlgoLocusMetricScalarAdapter` | lifecycle/value tests | rich Geo not NumberValue |
| G7B-45 | 100-consumer budget | ADR 0007 | shared owner consumer accounting | numerical N=100 | build=1, hits=99, duplicates=0 |
| G7B-46 | repeated-query budgets | benchmark plan §19 | shared owner/index counters | benchmark suite | 1/10/100 and policy traces |
| G7B-47 | nested normal-DAG budget | benchmark plan §19 | real Geo/Algo chain | nested suite | forbidden counters zero |
| G7B-48 | labels/selection/copy/set/remove | G7A lifecycle decision | rich Geo lifecycle methods | lifecycle suite | no foreign/stale current state |
| G7B-49 | developer-only laboratory | G7 spec §18 | extended G6R controller | Desktop contract suite | opt-in/nonpersistent |
| G7B-50 | no public command/XML/Path/3D | G7 spec §18 | no registration or interface | focused verifier/source audit | all public-boundary counts zero |
| G7B-51 | Classic/legacy unchanged | G7 spec §18 | no legacy metric edit | G6/G6R verifier | regressions pass |
| G7B-52 | no external numeric dependency | G7 prompt §45 | common Java only | dependency/source audit | package boundary clean |
| G7B-53 | controlled upstream impact | repository AGENTS §3 | GeoClass plus narrow tests only | modified-files registry | no base GeoElement refactor |
| G7B-54 | subordinate verifier | G7 prompt §46 | `verify-g7b-metrics.ps1` | composed `verify.ps1` | not independent authority |
| G7B-55 | G8/G9 remain absent | roadmap | no implementation paths | status/source audit | both not started |

The raw machine-readable counterpart is
[`g7b-metric-kernel-evidence.json`](../../geocedg/validation/locus-v2/g7b/g7b-metric-kernel-evidence.json).

# G9U0 public Locus V2 traceability matrix

- Status: **G9U0 = PASS — AUTHOR APPROVED / 93 SCENARIOS EXECUTED AND PASSED**
- Machine authority:
  `../../geocedg/validation/locus-v2/g9u0/g9u0-public-surface-scenarios.json`

The machine scenario file is authoritative for exact IDs and counts. Each ID
must map to one executable JUnit method whose name starts with the lowercase
group and number, for example `c01...`, `g22...` or `t01a...`.
The author-approved phase retains the exact frozen 114-path candidate boundary;
approval does not make the surface stable or default-on.

| Group | IDs | Focused class | Count | Layer |
|---|---|---|---:|---|
| Command/creation | U0-C01–U0-C14 | `org.geocedg.common.locus.G9U0CommandSurfaceTest` | 14 | shared command/kernel |
| Generator suite | U0-G01–U0-G22 | `org.geocedg.common.locus.G9U0GeneratorSuiteTest` | 22 | shared kernel |
| Metric/positions | U0-M01–U0-M11 | `org.geocedg.common.locus.G9U0MetricPositionTest` | 11 | shared kernel |
| Intersections/tokens | U0-I01–U0-I18 | `org.geocedg.common.locus.G9U0IntersectionTokenTest` | 18 | shared kernel |
| Persistence/compatibility | U0-P01–U0-P16 | `org.geocedg.common.locus.G9U0PersistenceCompatibilityTest` | 16 | shared XML/lifecycle |
| Runtime feature | U0-F01–U0-F04 | `org.geocedg.desktop.locus.G9U0RuntimeFeatureTest` | 4 | desktop/product |
| Localization/help | U0-L01–U0-L04 | `org.geocedg.desktop.locus.G9U0LocalizationHelpTest` | 4 | desktop/resources |
| Tools/selection | U0-T01, U0-T01A, U0-T02, U0-T03 | `org.geocedg.desktop.locus.G9U0ToolSurfaceTest` | 4 | desktop/controller |

Exact required IDs:

```text
U0-C01 U0-C02 U0-C03 U0-C04 U0-C05 U0-C06 U0-C07
U0-C08 U0-C09 U0-C10 U0-C11 U0-C12 U0-C13 U0-C14
U0-G01 U0-G02 U0-G03 U0-G04 U0-G05 U0-G06 U0-G07
U0-G08 U0-G09 U0-G10 U0-G11 U0-G12 U0-G13 U0-G14
U0-G15 U0-G16 U0-G17 U0-G18 U0-G19 U0-G20 U0-G21 U0-G22
U0-M01 U0-M02 U0-M03 U0-M04 U0-M05 U0-M06 U0-M07
U0-M08 U0-M09 U0-M10 U0-M11
U0-I01 U0-I02 U0-I03 U0-I04 U0-I05 U0-I06 U0-I07
U0-I08 U0-I09 U0-I10 U0-I11 U0-I12 U0-I13 U0-I14
U0-I15 U0-I16 U0-I17 U0-I18
U0-P01 U0-P02 U0-P03 U0-P04 U0-P05 U0-P06 U0-P07
U0-P08 U0-P09 U0-P10 U0-P11 U0-P12 U0-P13 U0-P14 U0-P15 U0-P16
U0-F01 U0-F02 U0-F03 U0-F04
U0-L01 U0-L02 U0-L03 U0-L04
U0-T01 U0-T01A U0-T02 U0-T03
```

## Required functional emphasis

- C01, M10 and I02 prove legacy delegation with live outputs, not source-string
  assertions alone.
- G03 proves the live dependent state is never assigned.
- G09, G10, G16 and G17 prove normal-DAG nesting and cycle behavior.
- G11, G14 and M02 prove preimage identity independently of coordinates.
- M06 proves standard Length reuses rich authority and exercises every scalar
  inadmissibility predicate.
- I01 proves every public target dispatch and line/segment/ray/circle Option-B
  point admissibility; I05 preserves Option B and I07 keeps tangency
  inadmissible. I11 proves the point does not invoke a solver.
- I14 preserves a token only when the canonical parameter/address proof is
  unchanged even though the evaluated Cartesian point moves. I15 burns tokens
  after merge/split or a changed canonical parameter and proves the old point
  becomes undefined before a newly minted token can create a current point.
- P01–P10 exercise actual XML/copy/undo behavior rather than DTO round trips.
- P13 distinguishes fork Classic from unsupported external upstream.
- F01 ensures algebra, tools, menu/help and existing-file preservation do not
  make contradictory feature decisions.
- T01A and T03 require accessible semantic/token choice without color or
  proximity as the only selector.

I03, I04, I08 and I09 deliberately use the already approved test-private G8
analytic capability/factory. They validate `COMPLETE`, empty, finite, overlap
and mixed rich-result rows without manufacturing completeness from productive
expression introspection. Public AlgebraProcessor coverage remains in I01,
I05, I07, I11 and I17; a public `NOT_ESTABLISHED` result is never relabeled
`COMPLETE`.

## Functional counters

The final evidence must publish and assert zero for label, coordinate, ordinal,
slider-visibility, render/sample/viewport, proximity-persistence, dependent
assignment, hidden-graph, independent scalar calculation, automatic migration,
lossy downgrade and stale-publication authority. Positive counters must show
balanced evaluator transaction restoration, bounded nested semantic work,
rich-query reuse and zero token-point solver calls.

## Execution gate

All eight focused source classes are present with an exact one-to-one mapping
from the 93 scenario IDs to JUnit method prefixes. The final focused execution
and its independent deterministic rerun each produced exactly 93 clean results:
81 shared and 12 desktop, with zero failures, errors and skips. Shared main,
shared test and desktop main Checkstyle reports were clean in both runs.

- Focused command: `.\tools\agent\verify-g9u0-locus-v2-public-surface.ps1
  -KeepBuildOutputs -LogDirectory
  artifacts/g9u0/candidate/focused-final-pass-escalated`
- Deterministic rerun: `.\tools\agent\verify-g9u0-locus-v2-public-surface.ps1
  -KeepBuildOutputs -LogDirectory
  artifacts/g9u0/candidate/focused-deterministic-pass-escalated`
- Composed authority without `-SkipBuild`: `.\tools\agent\verify.ps1
  -KeepBuildOutputs -LogDirectory artifacts/g9u0/candidate/composed-final-pass`;
  exit `0`, terminal outcome `ALL_GEOCEDG_VERIFICATION_GATES_PASSED`.

These directories are the original implementation-candidate execution
provenance. The author reviewed those results and approved G9U0 as PASS. No
external upstream runtime or later G9/G10 implementation was executed.

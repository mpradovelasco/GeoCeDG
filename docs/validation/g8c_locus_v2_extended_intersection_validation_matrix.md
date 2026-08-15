# G8C extended-intersection validation matrix

**Status: G8C1 IMPLEMENTATION GATES PASS — AUTHOR APPROVED; G8C2 GATES
PROPOSED/UNEXECUTED**

| ID | Track | Case | Required assertions | Reference |
|---|---|---|---|---|
| C1-CONIC-01 | G8C1 | ellipse secant / empty | analytic roots and complete empty; normalized residual | independent manifest |
| C1-CONIC-02 | G8C1 | parabola tangent/even root | root found without sign change; classification evidence | analytic factorization |
| C1-CONIC-03 | G8C1 | hyperbola multiple intersections | both conic branches covered; semantic parameters distinct | analytic reference |
| C1-CONIC-04 | G8C1 | translated/rotated/scaled conic | coordinate transform and equation-scaling invariance | high precision + probe |
| C1-CONIC-05 | G8C1 | near tangency | no false empty/transverse; truthful guarantee | independent high precision |
| C1-CONIC-06 | G8C1 | degenerate conic types | typed unsupported/subtype result; no smooth-adapter claim | upstream type audit |
| C1-FUNC-01 | G8C1 | bounded polynomial/trig | correct roots and coverage over explicit domain | analytic/high precision |
| C1-FUNC-02 | G8C1 | rational pole | invalid boundary splits components; no gap bridging | source/probe |
| C1-FUNC-03 | G8C1 | restricted/piecewise domain | undefined differs from empty and not-established | source/probe |
| C1-FUNC-04 | G8C1 | tangent/even root | derivative/minimum method; multiplicity truthful | analytic reference |
| C1-FUNC-05 | G8C1 | domain endpoint | endpoint classification and inclusivity | analytic reference |
| C1-FUNC-06 | G8C1 | no explicit finite domain | unsupported/not-established; never viewport completeness | source audit |
| C1-IMP-01 | G8C1 | regular polynomial implicit | roots, membership, gradient-normal residual | independent manifest |
| C1-IMP-02 | G8C1 | `G` versus `cG` | identical decisions/guarantees | symbolic + probe |
| C1-IMP-03 | G8C1 | multiple components/self-intersection | constructive preimages retained; completeness honest | analytic factors |
| C1-IMP-04 | G8C1 | singular/cusp/isolated point | regular normalization absent; no residual-only promotion | source/probe |
| C1-IMP-05 | G8C1 | nonpolynomial implicit | typed unsupported, no view/sample fallback | source/probe |
| LL-01 | G8C2 | transverse line-like loci | pair root, rectangle, normalized determinant, complete coverage | independent manifest |
| LL-02 | G8C2 | circle-like periodic loci | two roots, seam canonicalization, source reversal | independent manifest |
| LL-03 | G8C2 | tangent pair | no sign-change dependence; higher-order/isolation evidence | exact elimination |
| LL-04 | G8C2 | close pair roots | semantic rectangle separation; no Cartesian dedup | high precision |
| LL-05 | G8C2 | equal coordinate, two preimages | two tokens despite one coordinate | independent manifest |
| LL-06 | G8C2 | branch/component products | all pairs enumerated or completeness not established | functional counters |
| LL-07 | G8C2 | A×B versus B×A | equivalent tokens; ordered evidence swapped | pair-symmetry probe |
| LL-08 | G8C2 | monotone/reversed reparameterization | token preserved only through explicit map | analytic maps |
| LL-09 | G8C2 | full/reverse overlap | typed established relation; zero arbitrary points | exact map |
| LL-10 | G8C2 | suspected/unsupported overlap | no sample-based overlap proof | test-private probe |
| LL-11 | G8C2 | overlap + isolated | typed decomposition or explicit unsupported | analytic fixture |
| LL-12 | G8C2 | periodic seam crossing | no duplicate/new identity from seam representation | G6 domain authority |
| LL-13 | G8C2 | unbounded combination | no viewport/window `COMPLETE` | provider contract |
| LIFE-01 | Both | only A/target changes | normal DAG invalidation; other source unaffected | lifecycle test |
| LIFE-02 | G8C2 | both loci change | coherent dual revision; no mixed revision result | lifecycle test |
| LIFE-03 | Both | root appears/disappears/recovers | strict token, undefined interval, approved recovery only | lifecycle trace |
| LIFE-04 | Both | `2 -> 1 -> 2` | explicit ambiguity/event; no universal genealogy | dynamic trace |
| LIFE-05 | G8C2 | overlap begins/ends | finite point invalidated/recovered only by semantic continuation | dynamic trace |
| LIFE-06 | Both | atomic exception | current failure; no stale rich set or point | fault injection |
| LIFE-07 | Both | Option B | admissible local root with incomplete/not-established set | G8B regression + extension |
| NEST-01 | Both | downstream depth 1/2/3 | DAG propagation, bounded evaluations, no flattening | functional probe/test |
| INV-01 | Both | zoom/DPI/viewport | identical semantic results/counters | view mutation test |
| INV-02 | Both | scale/translation | normalized physical decisions preserved | analytic transforms |
| INV-03 | Both | deterministic rerun | results, tokens, ordering/counters deterministic | repeated test |
| PERF-01 | G8C1 | 1/10/100 compatible queries | counters within approved phase budgets; zero retained state | benchmark gate |
| PERF-02 | G8C2 | 1/10/100 pair queries | explicit pair combinatorics and bounded boxes | benchmark gate |
| AUTH-01 | Both | forbidden authorities | render, legacy sample, viewport, metric-index counters zero | instrumentation/static scan |
| REG-01 | Both | G8B/G7/G6 | all existing focused gates pass unchanged | canonical verifiers |
| REG-02 | Both | Classic/public/persistence/3D/G9 | no changed surface or behavior | diff/static audit |

## Executed characterization

The design task executes 32 test-private tests:

- 13 real-upstream extended-target authority/residual/domain probes;
- 13 dual-source identity/isolation/overlap/domain probes;
- 6 deterministic pair-combinatoric/nested-work probes.

These tests characterize architecture; they do not implement the future solver.
The independent reference manifest is reproduced at 80 decimal digits with
CPython 3.12.13 and mpmath 1.4.1.

## Executed G8C1 implementation validation

The implementation adds 38 focused tests: 22 kernel/geometry tests, 10
lifecycle/identity tests and 6 deterministic functional-counter tests. They
execute the applicable C1, LIFE, NEST, INV, PERF, AUTH and REG rows above.

The default evaluator-adaptive capability does not claim complete empty results
because it has no exhaustive coverage proof. C1-CONIC-01's complete-empty arm
is therefore tested through an injected authoritative capability that supplies
explicit complete coverage evidence; the default path remains truthfully
`NOT_ESTABLISHED`. Function gaps and singular implicit candidates remain hard
typed barriers. See the
[G8C1 kernel report](g8c1_locus_v2_extended_target_intersection_kernel_report.md)
and [traceability matrix](g8c1_locus_v2_extended_target_intersection_traceability_matrix.md).
The author approved these executed G8C1 gates on 2026-08-15; the numerical
measurements remain the recorded execution evidence rather than policy values.

## Acceptance principles

No tolerance may be weakened to pass. An individually verified solution and
global completeness are reported separately. Unresolved tangency, singularity,
overlap, unbounded coverage, identity, or atomic failure produces an explicit
state rather than a false root, empty set, completeness, or stale point.

# G8C extended-intersection functional-counter plan

**Status: G8C1 COUNTER CONTRACT AUTHOR APPROVED; G8C2 COUNTERS PROPOSED;
wall clock is informational**

## 1. Counter vocabulary

### G8C1

- semantic and derivative evaluations;
- target level, residual and derivative/gradient evaluations;
- invalid-domain boundaries/components visited;
- subdivisions and candidate intervals;
- analytic/certified/numeric refinements and iterations;
- residual/membership verifications;
- deduplication and continuation comparisons;
- budget exhaustion and diagnostics.

### G8C2 additions

- branch-pair and component-pair counts;
- parameter boxes visited/rejected/retained as candidates;
- pair refinements and solver iterations;
- two-sided residual evaluations;
- tangent/Jacobian evaluations;
- overlap checks;
- seam equivalence and pair-continuation comparisons.

### Forbidden/retained-state counters

- render-cache reads;
- legacy sample/`myPointList` reads;
- viewport/zoom/DPI reads;
- G7 metric-index/cumulative-state reads;
- whole-locus regenerations;
- retained entries/bytes and evictions.

All forbidden counters are exactly zero. Query-local implementations retain
zero entries after a query.

## 2. Characterized baseline

The design-only box probe partitions `[0,1]^2` for two crossing line-like
semantic loci. At subdivision `n`, it visits exactly `n^2` rectangles,
deterministically rejects nonoverlapping boxes, retains no state and records no
forbidden reads. This is not a productive solver or budget recommendation; it
demonstrates the necessary pair-space counters and quadratic worst-case surface.

The branch/component probe confirms the exact product

```text
sum_j components(A_j) * sum_k components(B_k).
```

Nested semantic sessions at depth 1/2/3 evaluate only the requested parameter
through each chain. Ten pair queries yield `20 * depth` evaluator calls across
both chains, zero whole-locus regenerations and zero render evaluations.

## 3. Future benchmark cases

For each phase run 1, 10 and 100 identical compatible queries/consumers, then
repeat after one source change, both-source change, topology change and failure.
Record exact counters and current revisions. G8C2 additionally sweeps:

- 1×1, multi-branch and multi-component products;
- transverse, tangent, close-root and overlap cases;
- bounded×bounded, bounded×periodic and periodic×periodic;
- nested CeDG depth 1/2/3.

Cache/reference-disabled semantic equality remains mandatory if any accelerator
is later proposed.

## 4. Initial budget policy

Do not copy G8B counts blindly. G8C1 may begin from the existing versioned G8B
per-query limits only where the same operation retains the same meaning. New
target-gradient/domain counters require measured limits during implementation.
G8C2 requires separate pair-product, parameter-box, pair-refinement, Jacobian
and overlap budgets approved before productive merge.

Budget exhaustion yields a coherent result with `INCOMPLETE` or
`NOT_ESTABLISHED` completeness and preserves individually admissible solutions
only when their local evidence is uncompromised. Wall-clock measurements remain
diagnostic because machine load is not deterministic authority.

## 5. State decision trigger

Query-local state is approved for both phases. A shared owner/index may
be proposed only if the 100-consumer measurements show repeated semantic work
that violates approved deterministic budgets and the proposal demonstrates
bounded revision-scoped storage, isolation, atomicity, deterministic eviction,
and exact semantic equality with sharing disabled.

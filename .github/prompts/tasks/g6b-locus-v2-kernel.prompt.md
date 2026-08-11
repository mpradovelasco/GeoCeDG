# Objective

Execute G6B: implement the minimal experimental two-dimensional Locus V2 kernel
entity that satisfies the author-approved G6A contract.

# Entry gate

Do not execute this prompt unless G6A is committed as PASS, the author has
approved its semantic spec, validation matrix, tolerances, benchmark budgets
and pilot set, and ADR 0006 or a superseding architecture decision is Accepted.

# Authority and evidence hierarchy

Follow `AGENTS.md`, the accepted G6 semantic spec/ADR, the approved
`docs/roadmap/g6_locus_v2_plan.md`, G6A evidence and exact impact map, current
code/build, accepted earlier contracts and scientific/regression sources, in
that order.

# Scope

Implement the approved V2-only semantic value types, drivers, deterministic
dependency-slice evaluator, parallel experimental kernel element and algorithm,
approved nested semantic-composition/session-DAG strategy, derived 2D
drawable/render cache, explicit diagnostic `LEGACY`/`V2`/`DUAL` modes, tests,
benchmarks, manifests, subordinate verifier and observed documentation.

# Explicitly forbidden scope

Do not change or redirect the existing `Locus` command or legacy object meaning
in any mode. Do not add any `.ggb` V2 persistence/migration, public command,
public V2 `Path`/incidence, `Point[GeoLocusV2,...]`,
`PathParameter[GeoLocusV2]`, public length, metric index,
intersections, DXF locus export, 3D/spatial semantics, toolbar redesign,
automatic migration, concurrency framework or broad upstream refactor. Do not
start G7, G8 or G9.

# Architectural placement

Definition/evaluator/branch identity and dynamic dependencies belong in the
shared kernel. Tessellation belongs in the Euclidian drawable layer. Runtime
selection belongs in an explicit GeoCeDG application/kernel setting; the
feature manifest records maturity but is not executable state. Classic remains
legacy.

# Required design/specification

Implement only the accepted G6A spec and accepted ADR. Use
`docs/architecture/locus_v2_upstream_impact.md` as the reviewed minimum change
map. If implementation requires a larger semantic or serialization change,
stop before editing it.

# Geometric invariants and degeneracies

Apply every required Level A, selected Level B/Level C and cross-cutting row of
`docs/validation/g6_locus_v2_validation_matrix.md`. Preserve parameter
multiplicity, branch identity, explicit invalid states, deterministic
evaluation, zoom independence and semantic/render separation. Never promote a
sampled polyline to geometric authority.

Use the approved versioned driver-domain provider parameter, branch key and
valid-domain-component model. Preserve the separated state layers,
pointwise/canonical-continuation determinism and four quality axes including
numeric guarantee.

# Nested Locus V2 composition

Implement nested composition through the G6A-approved internal typed API or
factory. A downstream V2 locus may consume an upstream V2 locus only through
its branch/domain descriptors, semantic evaluator, revision, validity and
quality metadata. Never read upstream render vertices/sampled polylines,
regenerate/tessellate a complete upstream locus, rebuild a complete upstream
dependency slice per downstream point, or create a hidden callback dependency
outside the normal kernel DAG.

PASS requires at least three semantic V2 levels in the controlled fixture,
correct geometry/revisions/branch keys, no upstream render dependency, no full
upstream regeneration, correct invalidation from the innermost source, equal
results with session/cache enabled and disabled, and scaling within the budget
approved after G6A. Demonstrate cycle rejection/diagnostics. Do not expose V2
as public `Path` to achieve this.

# Compatibility and serialization

Existing `.ggb` files and Classic use V1. V2 is experimental and opt-in; dual
mode labels V1 as sampled comparison evidence. `LEGACY`, `V2` and `DUAL` do not
redirect `Locus[...]`. G6B is non-persistent and has no public command or public
V2 `Path`; never write/reuse legacy XML for it.

# Required tests and commands

Run focused V2 tests, legacy/Classic regressions, cache/session-enabled/disabled
semantic comparison, `NESTED-1/2/3` and approved depth stress including
innermost invalidation and per-level counters, the approved benchmark suite,
shared/Desktop compilation
and checkstyle, GeoCeDG and Classic launch smoke, manifests/schemas, composed
`tools/agent/verify.ps1 -RunBenchmarks`, `git diff --check`, generated-output
cleanup and residual-process checks. The focused verifier must be subordinate
to `verify.ps1`.

# Required artifacts

Maintain the accepted spec/ADR, feature/profile manifests, regression models
and expected semantic evidence, benchmark results/budgets, upstream modification
record, user guide, roadmap, focused verifier/CI and exhaustive G6B report.

# PASS criteria

Apply section 6.5 of `docs/roadmap/g6_locus_v2_plan.md`. Explicitly confirm
that G7/G8/G9 and DXF locus export did not start.

# Stop conditions

Stop before changing legacy semantics, any V2 serialization, view-independent
geometry with view data, branch identity by sample order/proximity, the live
graph outside normal dependencies, upstream V2 consumption through render/
samples, per-query whole-locus/slice regeneration, clearly superlinear nested
depth scaling, or any unreviewed dependency/concurrency model. Report the
blocker to the author.

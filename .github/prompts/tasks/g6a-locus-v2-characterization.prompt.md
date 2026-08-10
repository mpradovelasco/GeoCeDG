# Objective

Execute G6A: approve and validate the mathematical, semantic, numerical and
compatibility contract for Locus V2 without implementing a productive V2
kernel object.

# Authority and evidence hierarchy

Follow `AGENTS.md`, `docs/roadmap/geocedg_roadmap.md`, the author-approved
`docs/roadmap/g6_locus_v2_plan.md`, accepted ADRs/specs, G0–G5 reports, pinned
baseline code, cataloged CeDG research and curated legacy/regression evidence,
in that order.

ADR 0006 is **APPROVED AS G6A WORKING ARCHITECTURAL HYPOTHESIS** but remains
`Proposed`. This authorizes characterization only. Do not mark it `Accepted`
before G6A closeout and a second explicit author review.

# Scope

Characterize legacy locus behavior; refine the provider-owned semantic
parameter, branch/valid-domain/evaluator, state, four-axis quality and
degeneration semantics; establish measured tolerances; curate the minimum
scientific cases; measure legacy and nested-composition baselines; complete the
exact upstream impact (including `GeoClass`); and prepare ADR 0006 for its second
author review.

Read-only characterization tests and focused measurement probes are allowed
when required for saved evidence. No productive `GeoLocusV2`, `AlgoLocusV2` or
V2 drawable is in scope.

# Explicitly forbidden scope

Do not change existing `Locus` meaning, production Locus classes, `.ggb`
serialization, toolbar, Classic behavior, G5 locus export policy, public length,
intersections, spatial projection semantics or concurrency. Do not start G6B,
G7, G8 or G9.

# Architectural placement

Mathematical/semantic truth belongs in the approved G6 spec; dependency and
legacy facts come from the shared kernel; validation evidence belongs in
`geocedg/validation/`, focused tests/probes and `docs/validation/`. Scientific
and legacy sources are evidence, not executable authority.

# Required design/specification

Start from:

- `docs/architecture/locus_v2_semantic_model.md`;
- `docs/architecture/locus_v2_upstream_impact.md`;
- `docs/validation/g6_locus_v2_validation_matrix.md`;
- `docs/validation/g6_locus_v2_benchmark_plan.md`;
- Proposed `docs/adr/0006-parallel-locus-v2-semantic-entity.md`.

Resolve every author-review question and publish a normative spec only after
approval. Do not mark ADR 0006 Accepted without explicit author authorization.

# Geometric invariants and degeneracies

Approve versioned driver-domain providers and semantic parameters; a native
parameter is authoritative only when its provider declares it suitable/stable,
and normalized `[0,1]` `PathParameter` is never automatic identity. Approve
semantic branch keys separately from declared domains and
`validDomainComponents[]`, orientations, endpoints/periodicity, multiple
preimages and typed lineage. Execute the formal two-control topology fixture.
Separate definition status, branch properties, evaluation status, optional
regularity and topology lineage. Classify evaluators as pointwise
deterministic, canonical-continuation deterministic (with anchor/orientation/
rule), or unsupported nondeterminism. Validate the four quality axes including
numeric guarantee. Pixel tolerance must never become geometric tolerance.

# Compatibility and serialization

Legacy `Locus`, Classic and existing `.ggb` files remain unchanged. G6B is
already constrained to be non-persistent, internal-only, without a public
command, `.ggb` migration or public V2 `Path`. `LEGACY`, `V2` and `DUAL` are
diagnostic modes and do not redirect `Locus[...]`. Audit every relevant use of
`GeoClass.LOCUS`, `isGeoLocus()`, `isGeoLocusable()`, drawing, defaults, labels,
metrics, `Path`, factory/XML and 2D/3D dispatch. Prefer a distinct V2
classification if the measured impact remains reasonably localized, but leave
the final choice to the second author review.

# Nested Locus V2 requirement

Reproduce the author's experimental legacy `Locus -> Locus` degradation and,
when technically reproducible, `Locus -> Locus -> Locus`. Treat the observation
as evidence, not a causal conclusion. Instrument dependency-slice creation,
reset/synchronization/update, sampled `Path` reads, repeated calls and render/
view interaction separately.

Compare at least:

1. recursive semantic evaluator composition with a scoped shared evaluation
   session/memoization context; and
2. controlled flattening/compilation of the evaluation DAG when safe.

Determine whether a session abstraction is needed without accepting its class
name prematurely. Its semantic key must include locus identity, revision,
`branchKey` and provider-owned semantic parameter. Audit normal DAG cycle
protection and hidden evaluator-callback cycles. Run `NESTED-1/2/3` and useful
`NESTED-5` measurements, with enabled/disabled session equality. No upstream
render sample, whole-locus regeneration or dependency-slice rebuild per
downstream point is permitted in the recommended G6B strategy.

# Required tests and commands

Run reproducible legacy characterization, native/normalized/provider parameter
cases, zoom/perimeter/sample cases, fresh/warmed/forward/reverse/shuffled
evaluation, the formal topology fixture, legacy and synthetic nested fixtures,
timeout/stress characterization, the complete G6A benchmark protocol,
applicable baseline and operational verification, manifests/schemas,
`git diff --check`, generated-output cleanup and residual-process checks. Record
commands, exit codes, toolchains, logs and artifact hashes. Numeric tolerances
and performance budgets are **DEFERRED TO G6A MEASUREMENTS**.

# Required artifacts

Produce the approved semantic spec, completed characterization and benchmark
evidence, approved validation/tolerance contract, curated pilot provenance,
formal topology fixture, legacy/nested causal evidence, selected session/DAG and
cycle policy, completed `GeoClass` audit, exact G6B file/class plan, second
author ADR disposition package, subordinate verifier, G6A report,
living-roadmap update and conceptual user-guide update. State clearly that no
user-visible V2 exists.

# PASS criteria

Apply section 5.4 of `docs/roadmap/g6_locus_v2_plan.md`. No productive V2 code
is necessary or permitted to claim G6A PASS.

# Stop conditions

Stop for author review when branch/component identity, deterministic evaluation,
numeric guarantee, nested evaluation strategy/scaling, cycle protection,
`GeoClass`, tolerance, licensing/provenance or performance-budget policy cannot
be resolved from evidence. Never encode a guess in a test or specification.

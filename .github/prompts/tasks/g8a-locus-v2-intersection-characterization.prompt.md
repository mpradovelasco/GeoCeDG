# Objective

Characterize native Locus V2 2D intersections, solver capabilities,
tolerances, topology, dynamic identity, lifecycle, and bounded work; then
produce an author-decision package without implementing productive G8 kernel
code.

This is a future execution prompt. **Do not execute it without separate,
explicit author authorization.** Its presence does not start G8A, G8B, or G8.

# Mandatory entry gate

Before any edit:

1. read repository `AGENTS.md`, canonical governance/verification prompts, and
   every authority listed below from disk;
2. require a clean or explicitly accounted worktree;
3. record repository root, branch, HEAD, `origin/main`, ancestry, upstream
   baseline, and toolchain;
4. confirm G6/G6R and G7 reproduce as `PASS`, G7A-R1/G7A/G7B are
   `PASS — AUTHOR APPROVED`, ADRs 0006/0007 are Accepted, and the G6/G7 specs
   are normative;
5. confirm G8 and G9 remain `NOT STARTED`, Locus V2 remains
   experimental/internal/disabled by default, and no productive G8 source
   exists;
6. run `tools/agent/verify-operational.ps1`,
   `tools/agent/verify-locus-v2.ps1`,
   `tools/agent/verify-g7a-metrics.ps1`, and
   `tools/agent/verify-g7b-metrics.ps1`; and
7. verify scientific-catalog, model, and prior evidence hashes used by the
   characterization.

If the approved baseline does not reproduce, stop:

```text
G8A = BLOCKED — G6/G7 BASELINE NOT REPRODUCED
```

Classify environment, Windows permission, stale-test/API, missing-source, and
productive regressions before changing code. Do not patch project code for an
environment failure.

# Authority and evidence hierarchy

Apply in order:

1. current repository code, tests, build configuration, and serialization;
2. `AGENTS.md` and canonical governance/verification prompts;
3. `docs/roadmap/geocedg_roadmap.md`;
4. normative G6/G7 Locus V2 specs;
5. Accepted ADRs 0006 and 0007;
6. G6/G6R/G7 implementation/API/validation evidence;
7. actual productive Locus V2, metric, and upstream intersection source;
8. the author-reviewed G8 planning package, if still marked proposed; and
9. the hash-pinned CeDG scientific catalog, manifests, and source material.

The G8 planning package is the characterization hypothesis, not a normative
contract:

- `docs/roadmap/g8_locus_v2_intersections_plan.md`;
- `geocedg/specs/locus/locus-v2-intersections.md`;
- `docs/architecture/locus_v2_intersection_semantic_model.md`;
- `docs/architecture/locus_v2_intersection_architecture.md`;
- `docs/architecture/locus_v2_intersection_upstream_impact.md`;
- `docs/validation/g8_locus_v2_intersection_validation_matrix.md`;
- `docs/validation/g8_locus_v2_intersection_benchmark_plan.md`;
- `docs/validation/g8_locus_v2_intersection_scientific_traceability.md`; and
- Proposed ADR 0008.

Do not use external conversation or historical sampled output as authority.

# Scope

G8A may add only test-private characterization probes/fixtures, reproducible
independent-reference scripts/data, validation evidence/reports, and proposed
documentation/API refinements. It must:

- audit real target equations/incidence capabilities for line, segment, ray,
  circle, conic, function, and implicit families;
- compare analytic/exact, certified interval, derivative-aware,
  evaluator-only, and conservative broad-phase approaches where real
  capabilities exist;
- characterize complete isolation, root refinement, even-multiplicity
  tangency, residual verification, semantic-parameter deduplication, overlap,
  and bounded failure;
- measure independent root/residual/tangency/dedup/continuation tolerance
  quantities without approving values prematurely;
- prototype/test rich-result, internal-only, and bounded-point lifecycle
  alternatives without adding productive classes;
- exercise branch/component/preimage topology and deterministic root lineage;
- compare query-local behavior and characterize reusable state only if measured
  repeated-query need exists;
- execute the proposed validation/functional-counter matrices;
- build the reduced focal sphere/cone, cone–cylinder, and nested pilots where
  reproducible and within 2D scope; and
- produce an exact candidate G8B API, edit set, budgets, compatibility analysis,
  and author-decision table.

# Explicitly forbidden scope

Do not:

- add any productive intersection class under `src/main`;
- change `GeoLocusV2`, legacy `GeoLocus`, `myPointList`, Classic
  `AlgoIntersect*`, `CmdIntersect`, `AlgoDispatcher`, `Path`, or incidence
  semantics;
- add a `GeoClass`, public point result, command, toolbar/UI, persistence/XML,
  factory, migration, export, 3D, spatial/G9, or Python DSL behavior;
- use render/sample/viewport/zoom/DPI/pixel data or graphical proximity as
  geometric or identity authority;
- infer tangency from sign changes alone;
- identify roots by coordinate nearest-neighbour, output slot, label, or
  creation order;
- use the G7 metric index/owner/partitions as intersection authority;
- introduce a productive/global/unbounded cache or hidden dependency;
- make the proposed G8 spec normative or accept ADR 0008; or
- start G8B.

# Architectural placement

Future productive intersection truth belongs in the shared Java kernel because
it changes geometric incidence and must participate in the Construction DAG.
G8A itself places only test-private probes in shared test source, independent
references/evidence under `geocedg/validation/`, and reviewed reports under
`docs/validation/`. No GUI, renderer, script output, scientific model, or
generated report becomes geometric authority.

# Required design/specification

## Result and lifecycle

Compare:

1. ordinary point array only;
2. internal immutable result only; and
3. internal immutable result plus a dedicated nonnumeric rich Geo and optional
   bounded point adapter.

Probe actual `GeoElement`, `GeoClass`, `AlgoElement.OutputHandler`, copy/set,
definedness, removal, label, update, and exception paths. Record whether a rich
failure/empty/overlap snapshot can live in the normal DAG without XML or public
creation.

## Geometry and numerics

For each target family, record authoritative representation, domain/membership,
degenerations, derivative/exact capabilities, isolation completeness,
refinement method, residual normalization, attainable guarantee, and explicit
unsupported cases. A finite found set is not complete without coverage
evidence.

Even roots, near tangency, clustered roots, endpoints, periodic seams,
discontinuities, collapsed components, overlap, and equation scaling are
mandatory. Independently verify every candidate against semantic evaluation
and target authority.

## Identity and topology

Trace one/many moving roots, merge, split, seam crossing, endpoint loss,
invalid gap, branch replacement, ambiguity, failure, and recovery. Compare
opaque semantic token/lineage alternatives. No probe may treat coordinates as
the primary identity.

## Work and state

Record every counter in the benchmark plan. Measure queries and 1/3/10/100
consumers, topology revisions, removal, capacity pressure, and 1–3 nested
levels. Start query-local. Any recommended shared owner needs a complete key,
immutable payload, current-revision policy, deterministic capacity/eviction,
Construction lifecycle, cache-off equality, and separate author decision.

# Geometric invariants and degeneracies

Characterization must preserve semantic source identity/revision,
branch/component/preimage multiplicity, target authority, and normal DAG
invalidation. Mandatory degeneracies include even and higher roots, near
tangency, endpoints, periodic seams, invalid gaps, cusps, collapsed and empty
components, repeated coordinates, overlap/infinite sets, root merge/split,
branch/component creation or loss, nonfinite evaluation, work exhaustion, and
recovery. Every unsupported case is explicit; none becomes empty or stale
geometry.

# Compatibility and serialization

Legacy `GeoLocus`, Classic intersection dispatch/results/labels, existing
`.ggb` serialization, G5 export, 3D dispatch, and G9 behavior must remain
unchanged. G8A adds no `GeoClass`, factory/XML entry, public Path/incidence,
command, migration, or persisted root identity. Run current compatibility and
serialization gates as non-regression evidence.

# Required tests and commands

Execute the approved G8A characterization matrix and functional-counter plan,
plus the existing operational, Locus V2, G7A, and G7B focused verifiers. After
all saved-file work, run the canonical composed verifier and `git diff --check`.
Record exact working directory, arguments, exit code, log/evidence path, and
whether scientific/runtime evidence is static, test-private, skipped, or real.

# Required artifacts

Produce, at minimum:

- a G8A characterization report and machine-readable evidence bundle;
- an updated traceability matrix mapping every author decision to raw evidence;
- reproducible independent-reference scripts/data with formula, precision,
  runtime/library versions, and hashes;
- exact proposed Java API and smallest productive/test file set;
- measured tolerance candidates and deterministic functional budgets;
- explicit rejected strategies and unsupported coverage;
- proposed updates to the G8 spec, architecture, validation, benchmark, ADR,
  and G8B prompt; and
- one author decision table with no silently normative recommendation.

Generated reports belong under ignored artifacts unless a reviewed durable
evidence file is explicitly part of the versioned validation bundle.

# G8A exit and author gate

G8A is not complete merely because probes run. It requires:

- all planned evidence is reproducible;
- target-family and guarantee boundaries are closed;
- no stop condition remains unresolved;
- proposed tolerances/budgets have measured provenance;
- result/identity/overlap/cache/scope alternatives are reviewable; and
- canonical/focused verification passes.

Close first as:

```text
G8A = CHARACTERIZATION COMPLETE — AWAITING AUTHOR REVIEW
G8 SPEC = PROPOSED / NOT NORMATIVE
ADR 0008 = PROPOSED
G8B = NOT STARTED
```

Only an explicit later author review may record `G8A = PASS — AUTHOR APPROVED`,
make the spec normative, accept/supersede the ADR, and authorize G8B.

# Stop conditions

Stop rather than weakening evidence if:

- G6/G7 no longer reproduces or source contradicts their normative contracts;
- complete coverage requires render/legacy samples or viewport state;
- tangency can only be detected by sign changes;
- identity can only be coordinate/slot based;
- topology events cannot be represented deterministically;
- tolerances lack measured provenance;
- ordinary failures expose stale/partial results;
- a cache must be global, unbounded, or a hidden DAG;
- public Path/XML/3D/G9 or legacy behavior changes become necessary; or
- scientific requirements conflict materially with the real 2D architecture.

# Verification and closeout

Use `tools/agent/` wrappers. Run all focused G6/G7/G8A tests, documentation and
evidence validators, the canonical composed verifier at the required level,
and `git diff --check`. Confirm no productive `src/main` G8 source changed and
no public/legacy/XML/3D/G9 behavior was added. Report exact commands, exit
codes, logs, evidence hashes, files inspected/changed, skipped checks, and
risks.

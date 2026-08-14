# Objective

Characterize native Locus V2 2D intersections, solver capabilities,
tolerances, topology, dynamic identity, lifecycle, and bounded work; then
produce an author-decision package without implementing productive G8 kernel
code.

G8 planning is `PASS — AUTHOR APPROVED`, and G8A is authorized. This prompt is
still a future execution entry point: **execute it only in a separate task that
explicitly invokes G8A.** Planning closeout does not execute G8A. This prompt
never authorizes G8B or productive G8 work.

The fundamental CeDG capability under characterization is:

```text
CeDG construction -> Locus V2 geometric projection
    -> native intersection with a supported ordinary 2D entity
    -> semantically identified intersection solution(s)
    -> downstream CeDG construction -> normal dynamic propagation
```

This is a structural kernel requirement, not UI convenience. Each supported
finite solution must preserve constructive traceability, branch/component and
semantic-preimage provenance, dynamic identity, topology change, and
degeneration. It must be able to serve as a stable later-construction input
when continuation is unambiguous. Family support still grows incrementally.

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
5. confirm G8 planning is `PASS — AUTHOR APPROVED`, G8A is `AUTHORIZED`, G8B
   is `NOT AUTHORIZED / BLOCKED ON G8A PASS — AUTHOR APPROVED`, G8 productive
   implementation and G9 remain `NOT STARTED`, Locus V2 remains
   experimental/internal/disabled by default, and no productive G8 source or
   prior G8A probe/evidence exists;
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
8. the author-approved G8 planning package, whose specification/ADR and
   unresolved semantic details remain proposed; and
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
- validate the approved immutable-rich-set plus normal-DAG nonnumeric-rich-Geo
  architecture and characterize an optional bounded-point consumer without
  adding productive classes or reopening point-only authority;
- report `COMPLETE`, `INCOMPLETE`, or `NOT_ESTABLISHED` completeness separately
  from computation status, per-root residual validity, guarantee, geometry
  kind, identity, and currentness for every solver strategy;
- exercise branch/component/preimage topology, reparameterization invariance,
  and root identity; treat merge/split genealogy as a hypothesis;
- compare query-local behavior and characterize reusable state only if measured
  repeated-query need exists;
- execute the proposed validation/functional-counter matrices;
- prove in a test-private fixture that an identified intersection can drive a
  later CeDG-style construction through normal DAG propagation while
  continuation is unambiguous;
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

Validate this author-approved planning architecture:

1. immutable rich intersection-set value as semantic authority;
2. atomic publication through a dedicated nonnumeric rich Geo in the normal
   DAG; and
3. optional bounded ordinary points only as derived consumers that cannot hide
   incomplete/not-established set semantics.

Probe actual `GeoElement`, `GeoClass`, `AlgoElement.OutputHandler`, copy/set,
definedness, removal, label, update, and exception paths. Record whether a rich
failure/empty/overlap snapshot can live in the normal DAG without XML or public
creation.

## Geometry and numerics

For each target family, record authoritative representation, domain/membership,
degenerations, derivative/exact capabilities, isolation completeness,
refinement method, residual normalization, attainable guarantee, and explicit
unsupported cases. A finite found set is not complete without exhaustive
isolation/exclusion evidence. Every strategy reports verified-root count,
completeness status, the method by which completeness was established or
failed to be established, and work counters. Solver convergence and
individually verified roots do not prove
that no additional root was missed.

Include complete empty, complete finite, verified-but-incomplete finite, and
completeness-not-established cases. Explicitly probe tangencies,
evaluator-only methods, unbounded domains, difficult multiple roots, and
non-exhaustive broad phases. Any scalar/point experiment must refuse to present
an incomplete or not-established subset as the full result.

Even roots, near tangency, clustered roots, endpoints, periodic seams,
discontinuities, collapsed components, overlap, and equation scaling are
mandatory. Independently verify every candidate against semantic evaluation
and target authority.

Report contact and multiplicity independently: tangent established,
transverse established, exact multiplicity established, and classification or
multiplicity not established must remain distinguishable. Never turn uncertain
contact into transverse or no root.

## Identity and topology

Separate candidate durable/continuation identity information—source-pair,
constructive intersection lineage, applicable branch lineage,
topology/continuation context, and established continuation relation—from
revision-scoped numerical/localization evidence—semantic parameter, isolating
interval, residual, revisions, and solver state/certificate. An isolating
interval is evidence, not fundamental identity.

Trace one/many roots under ordinary continuous source motion, equivalent
monotone reparameterization, parameter reversal/orientation where semantically
allowed, and periodic-seam representations. Determine the exact invariant
subset. Where full reparameterization-invariant identity cannot be established,
return an explicit ambiguous/not-established/unsupported identity state; no
probe may fall back to coordinates.

Treat parent/child merge/split genealogy as a hypothesis. Run controlled
forward and reverse traces for `2 roots -> tangent/multiple root -> 2 roots`,
covering explicitly:

1. two roots approaching;
2. the exact tangent/merge state;
3. the subsequent split into two roots;
4. reversal of the source-parameter change;
5. symmetric cases with intrinsically ambiguous child correspondence;
6. periodic-seam interaction; and
7. branch/component changes near the same event.

At every step record root token/identity state,
semantic parameter, isolating interval, source/topology revision or epoch,
candidate/established parent-child lineage, and ambiguity event. Preserve
identity only when continuation is geometrically unambiguous; otherwise expose
ambiguity or identity discontinuity. Recommend a narrower rigorous contract if
the universal genealogy model fails.

## Work and state

Record every counter in the benchmark plan. Measure queries and 1/3/10/100
consumers, topology revisions, removal, capacity pressure, and 1–3 nested
levels. Use the author-approved query-local starting point. Any recommended
shared owner needs a complete key, immutable payload, current-revision policy,
deterministic capacity/eviction,
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
- a field-by-field durable-identity versus revision-evidence decision and the
  supported reparameterization invariance subset;
- per-strategy verified-root/completeness/method/work evidence;
- forward/reverse merge/split traces with tokens, parameters, intervals,
  revisions, candidate genealogy, and ambiguity events;
- explicit rejected strategies and unsupported completeness/family cases;
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
- completeness/identity/overlap/cache/scope decisions are reviewable; and
- canonical/focused verification passes.

Close first as:

```text
G8A = CHARACTERIZATION COMPLETE — AWAITING AUTHOR REVIEW
G8 SPEC = PROPOSED / NOT NORMATIVE
ADR 0008 = PROPOSED
G8B = NOT AUTHORIZED / BLOCKED ON G8A PASS — AUTHOR APPROVED
```

Only an explicit later author review may record `G8A = PASS — AUTHOR APPROVED`,
make the spec normative, accept/supersede the ADR, and authorize G8B.

# Stop conditions

Stop rather than weakening evidence if:

- G6/G7 no longer reproduces or source contradicts their normative contracts;
- completeness can be established only through render/legacy samples or
  viewport state;
- tangency can only be detected by sign changes;
- identity can only be coordinate/slot based;
- topology events cannot be represented explicitly as established lineage,
  ambiguity, or identity discontinuity;
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

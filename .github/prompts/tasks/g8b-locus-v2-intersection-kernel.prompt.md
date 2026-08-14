# Objective

Implement the author-approved minimum internal native Locus V2 2D
intersection kernel, using the exact result, solver, identity, tolerance,
work, scope, and lifecycle contracts approved after G8A.

This is a future execution prompt. **Do not execute it during planning or G8A.**
Current state is `G8B = NOT AUTHORIZED / BLOCKED ON G8A PASS — AUTHOR
APPROVED`. Its presence does not authorize or start productive G8B work.

# Mandatory entry gate

Before productive editing, require all of:

- G6/G6R and G7 reproduce as `PASS`;
- `G8A = PASS — AUTHOR APPROVED` is recorded in versioned closeout evidence;
- `geocedg/specs/locus/locus-v2-intersections.md` is explicitly normative and
  author-approved;
- ADR 0008 is Accepted or a superseding Accepted ADR records the chosen result,
  continuation, and state architecture;
- exact target-family support, overlap policy, tangent/multiplicity semantics,
  numeric capability hierarchy, tolerance values, work budgets, root lineage,
  rich-Geo/point boundary, and cache/index ownership are author-approved;
- the exact candidate API and upstream-owned edit set are approved;
- the roadmap records `G8B = AUTHORIZED / NOT STARTED`; and
- the current task explicitly executes this G8B prompt.

If any item is absent, stop:

```text
G8B = BLOCKED — APPROVED G8A ENTRY BASELINE NOT REPRODUCED
```

Do not infer approval from this prompt, a Proposed ADR/spec, conversation, or
unreviewed characterization recommendation.

# Authority and evidence hierarchy

Read current repository source first. Record root, branch, HEAD, origin/main,
baseline, worktree, and the approved G8A evidence hashes. Run the operational,
Locus V2, G7A, and G7B focused verifiers before editing.

Authority order:

1. current source/tests/build/serialization;
2. `AGENTS.md` and canonical governance/verification prompts;
3. living roadmap;
4. normative G6, G7, and approved G8 specs;
5. Accepted ADRs 0006, 0007, and the G8 decision ADR;
6. approved G8A report/API/traceability/evidence;
7. current G6/G7 architecture/API/source;
8. audited upstream intersection source; and
9. scientific pilots as regression requirements, never numeric authority.

If current source or the approved record disagrees materially, stop for author
review. Do not choose between conflicting normative contracts yourself.

# Scope

Implement only the object families, result architecture, and solver
capabilities explicitly approved after G8A. The planning recommendation is
line, segment, ray, and circle, with full conics conditional on evidence; that
recommendation is not authority until approved.

Expected semantic responsibilities, when approved, are:

- immutable revision-bound query and target adapters;
- per-component semantic candidate isolation;
- safeguarded refinement and independent residual/membership verification;
- tangent/even-root handling beyond sign changes;
- semantic-parameter deduplication preserving distinct preimages;
- complete finite, complete empty, finite incomplete/not-established,
  unresolved, unsupported,
  overlap/infinite, invalid, stale, and failure result states;
- an independent `COMPLETE`/`INCOMPLETE`/`NOT_ESTABLISHED` result-set
  completeness axis that no point/scalar consumer hides;
- opaque root tokens using the post-G8A approved durable-versus-revision
  evidence contract and the approved topology/ambiguity semantics;
- atomic normal-DAG publication through the approved rich result architecture;
- deterministic work limits and counters; and
- approved query-local or bounded revision-scoped state with cache-off semantic
  equality.

# Explicitly forbidden scope

Unless the author-approved G8A record separately and explicitly changes one of
these boundaries, do not:

- add public `Intersect` or other commands, dispatcher overloads, toolbar/UI,
  or default feature activation;
- add public/ordinary `Path`, point-on-V2, arbitrary incidence, or silent
  coordinate-to-parameter binding;
- add XML/factory/persistence/migration or serialize root tokens;
- change legacy `GeoLocus`, `myPointList`, Classic `AlgoIntersect*`, existing
  labels/output order, or old `.ggb` results;
- add functions, implicit curves, or locus–locus beyond the approved minimum;
- add G5 export changes, 3D behavior, spatial/projection/G9 semantics, or Python
  DSL;
- read render caches/vertices, legacy samples, viewport, zoom, DPI, pixel
  tolerance, or graphical proximity as authority;
- use coordinate nearest-neighbour, output index, label, or creation order for
  root identity;
- reuse the G7 metric index/owner/partitions as intersection authority;
- introduce a global, unbounded, multi-revision, or hidden-dependency cache; or
- weaken an unresolved case into empty or an overlap into sampled points.

# Architectural placement

Approved productive intersection truth belongs in additive GeoCeDG-owned
shared-kernel packages and the normal Construction DAG. Validation references,
scientific pilots, generated evidence, and performance analysis remain outside
productive kernel truth. Frontends may later consume approved outputs but do
not solve, identify, or repair intersections.

# Required design/specification

## Productive placement

Prefer additive GeoCeDG-owned shared-kernel packages. Modify upstream-owned
files only in the exact author-approved set, preserving notices and updating
the modified-file governance record. Do not refactor unrelated Classic code.

## Dependency lifecycle

Register both sources through `setInputOutput()`. Capture coherent source
revisions, compute privately, and publish one immutable snapshot. At recompute
start, the old result is no longer current. Exception, budget exhaustion, or
invalid input must publish the approved coherent rich outcome with no partial
current roots. Removal releases all leases/state.

## Solver truthfulness

Select capabilities exactly as approved. Record method/provenance/guarantee;
independently verify every candidate. Complete empty requires established
exhaustive isolation/exclusion evidence. Multiplicity/contact order remains
unknown unless proved to the approved standard. Target equation scaling must
not change the residual
decision.

## Identity

Use the approved semantic token/topology policy. Root parameters, isolating
intervals, residuals, and solver certificates are revision-scoped evidence,
not fundamental durable identity. Merge, split, seam, reparameterization,
termination, ambiguity, and branch-lineage behavior must match the
author-approved G8A traces and narrower contract if universal genealogy was
rejected. Coordinates may be verified but never select identity. Bound
retained topology history and any derived output slots exactly as approved.

## State and counters

Enforce every approved evaluation, derivative, candidate, subdivision, depth,
iteration, residual, continuation, output, and retention ceiling. Failed
private builds publish no reusable entry. If reuse exists, keys include every
result-affecting source revision, branch/component, target state, capability,
algorithm, tolerance, and work-policy value established by G8A.

# Geometric invariants and degeneracies

Preserve revision coherence, branch/component/preimage multiplicity,
representation independence, explicit guarantee/completeness, and atomic DAG
currentness. Implement the approved behavior for transverse/even/higher roots,
near tangency, endpoints, seams, discontinuities, repeated coordinates,
collapsed/empty components, overlap/infinite sets, root merge/split,
branch/component topology change, nonfinite evaluation, budget exhaustion,
exception, removal, and recovery. Unsupported geometry remains a closed rich
state and never stale or fabricated points.

# Compatibility and serialization

Keep legacy `GeoLocus`, Classic intersection overloads/results/labels,
existing `.ggb` XML, G5 export, 3D dispatch, and G9 behavior unchanged. Unless
the approved G8A record explicitly opens a separate boundary, add no command,
dispatcher overload, public Path/incidence, `GeoFactory`, XML tag, migration,
or persisted root token. Record and test every approved upstream-owned edit.

# Required tests and commands

Implement every selected B-core/B-policy case in
`docs/validation/g8_locus_v2_intersection_validation_matrix.md`, including:

- zero/one/multiple, endpoint, seam, transverse, tangent, near-tangent, and
  equation-scaling cases;
- branches/components, repeated coordinates/preimages, discontinuities,
  collapsed/empty components, and topology changes;
- overlap/infinite and unsupported/unresolved rich results;
- root continuation, merge, split, ambiguity, stale/failure/recovery traces;
- viewport/render/legacy independence and forbidden-access zero counters;
- deterministic bounded work, repeated/nested consumption, removal, and
  exception safety;
- cache/index enabled/disabled rich semantic equality, if applicable; and
- Classic/legacy/serialization/3D/export non-regression.

Use approved independent G8A references. Productive code must not depend on
Python, scientific PDFs, legacy `.ggb` files, or generated reports.

Run the approved focused G8 verifier, all operational/Locus V2/G7 focused
verifiers, shared-kernel tests and Checkstyle, serialization/Classic
non-regression, the canonical composed verifier, and `git diff --check`.

# Required artifacts

Produce the approved productive source and focused tests, a G8B kernel report,
machine-readable functional evidence, traceability matrix, updated internal
developer API/architecture/spec references, exact upstream-modification record,
and saved verifier logs/hashes. Update roadmap/user documentation only to the
observable status actually verified; do not add public usage instructions for
an internal-only result.

# Stop conditions

Stop and report if:

- any entry gate or approved evidence cannot be reproduced;
- an approved API conflicts with current source or normal DAG lifecycle;
- a supported root set cannot be made complete under the approved guarantee;
- tangency falls back to sign-change-only logic;
- identity requires coordinate/slot proximity;
- an exception can expose stale/partial success;
- work/state cannot be bounded under the approved policy;
- render/legacy/metric-index authority or hidden dependencies become necessary;
- an unapproved upstream file/public API/Path/XML/3D/G9 change is required; or
- scientific pilots require broader semantics than the approved 2D scope.

# Verification and closeout

Run the focused G8 verifier established by the approved work, all existing
operational/Locus V2/G7 gates, shared-kernel tests and Checkstyle, serialization
and Classic non-regression gates, the canonical composed verifier, and
`git diff --check`. Confirm generated outputs are not versioned and the
upstream-modification inventory is accurate.

Report exact commands, exit codes, logs/evidence, source/test files changed,
architectural layer, preserved geometric/compatibility contracts, unsupported
cases, skipped checks, and risks. Do not mark G8B/G8 PASS unless the saved-file
verification and author-closeout requirements actually pass.

# Objective

Implement the author-approved G8C1 internal extended one-parameter Locus V2
intersection targets: nondegenerate ellipse/parabola/hyperbola, explicitly
bounded real `GeoFunction` graphs, and the approved regular polynomial implicit
subset.

**Future execution prompt — do not execute during G8C design.** Its presence
does not authorize implementation. A later task must explicitly invoke it after
all entry gates are author-approved.

# Mandatory entry gate

Before any productive edit, require and report all of:

- a clean dedicated G8C1 feature branch created from the published G8B/G8C
  author-review baseline prescribed by the current roadmap;
- G6/G6R, G7, G8A, G8B-R1 and G8B reproduce as `PASS — AUTHOR APPROVED`;
- G8C design is `PASS — AUTHOR APPROVED` and G8C1 is explicitly authorized;
- the G8C extension specification is normative/author-approved for G8C1;
- ADR 0008 remains Accepted with R1 clarification;
- the supported target subsets, normalized residual meanings, domain policy,
  tangency hierarchy, budgets and compatibility boundary are author-approved;
- G8B focused and composed verification passes before editing; and
- the current task explicitly invokes this prompt.

If any gate is missing, stop with `G8C1 = BLOCKED — ENTRY BASELINE NOT
REPRODUCED`. Do not infer approval from the prompt file.

# Authority and evidence hierarchy

Read current source/tests/build first, then `AGENTS.md`, canonical governance and
verification prompts, living roadmap, normative G6/G7/G8 and approved G8C1
specifications, Accepted ADRs 0006/0007/0008, approved G8C design/report/API/
matrices/evidence, actual G8B source, audited upstream conic/function/implicit
source and scientific pilots. Conversation and generated reports never override
current source or normative contracts.

Record root, branch, HEAD, `main`, `origin/main`, upstream relation, worktree,
baseline tag(s), prompt SHA-256 and authoritative evidence hashes before edits.

# Scope

Implement only:

- nondegenerate ellipse, parabola and hyperbola targets from authoritative
  `GeoConicND` geometry (circle remains a G8B regression);
- explicitly bounded real-valued `GeoFunction` graphs with finite valid
  components and explicit invalid-domain barriers;
- finite-coefficient polynomial `GeoImplicitCurve` targets at regular roots;
- additive target adapter capabilities, one-parameter isolation/refinement,
  independent verification, immutable evidence and current diagnostics needed
  for these families;
- existing rich-result/Geo/token-point publication and Option B semantics;
- tests, references, evidence, docs, counters and verification.

# Explicitly forbidden scope

Do not add degenerate-conic promotion, unrestricted functions, unrelated
parametric/freehand/data curves, nonpolynomial/general implicit support,
singular-root point promotion without an already approved stronger contract,
Locus V2 × Locus V2, public commands/dispatcher, generic `Path`, persistence/
XML/migration, legacy `GeoLocus`, Classic changes, frontend, 3D, G9, Python DSL,
global/shared caches, G7 metric state, render/legacy/viewport authority, or
coordinate/list-order identity.

# Architectural placement

Productive geometry belongs only in additive GeoCeDG-owned shared Java kernel
packages and the normal Construction DAG. References, scientific pilots,
reports and benchmarks remain validation evidence. Every unavoidable
upstream-owned edit must be minimal, registered and separately justified.

# Required design/specification

## Typed target adapters

Extend the G8B one-parameter pipeline through a closed adapter contract. Each
binding must record exact target type/revision/support, explicit domain and
invalid boundaries, candidate level, normalized residual quantity/units,
membership verification, derivative/normal capability, semantic bounds,
degeneration/overlap support and diagnostics.

Candidate evaluation cannot serve as independent verification merely by being
called twice. Reject unsupported capability with typed state, not `null`, NaN,
magic numbers or ordinary exceptions.

## Residual contracts

- Conics: canonical regular first-order normal `G/||grad G||`, invariant under
  nonzero equation scaling; do not call it exact Euclidean distance. Use
  stronger canonical analytic evidence only when its guarantee is explicit.
- Functions: vertical model-length `y-f(x)` is the authoritative incidence
  residual. A finite-derivative `/(sqrt(1+f'^2))` quantity is a first-order
  normal estimate only. Never compare it as though it were the same quantity as
  another family's residual.
- Polynomial implicit: regular first-order normal `G/||grad G||`, invariant to
  equation scaling. At zero/invalid gradient, publish unsupported/undetermined
  local evidence unless an approved stronger method establishes the root.

Do not reuse render, G7 metric or raw algebraic tolerance. Version every new
quantity, normalization, value and provenance. If implementation measurements
require changing the proposed values/budgets, stop for author decision rather
than weakening evidence.

## Domain and completeness

Use only semantic source components and explicit target domains. Split function
components at poles/nonfinite/undefined/discontinuous boundaries; never bridge
them. Never obtain function search bounds from a view. `EMPTY + COMPLETE`
requires exhaustive coverage/exclusion over every declared valid component.
Solver convergence or finite candidate exhaustion alone does not establish
completeness.

## Tangency, isolation and identity

Tangency cannot rely solely on sign changes. Use analytic/certified evidence
where available, then derivative/local-minimum/safeguarded methods with explicit
weaker guarantees. Multiplicity/classification remains undetermined unless
established.

Preserve semantic-parameter isolation, constructive preimages, opaque token
identity, explicit continuation/ambiguity, no universal genealogy and no
coordinate/order repair. Root intervals, target/source revisions and residuals
remain revision evidence.

## Lifecycle and point consumer

Use normal DAG inputs, coherent source/target revisions and atomic immutable
publication. Preserve the internal strict token-selected point consumer and
Option B: an individually verified, locally established current root may drive
the point under incomplete/not-established global completeness. The point never
hides parent completeness or retargets.

## State and work

Start query-local. Extend deterministic counters for target/derivative/domain
evaluations and bounded work. Retained entries after a query are zero. No
whole-locus regeneration, render/legacy/view/G7 reads or stale state is allowed.

# Geometric invariants and degeneracies

Preserve constructive traceability, semantic parameter and branch/component
provenance, dynamic dependencies, equation-scale invariance, Option B,
coordinate-independent identity and explicit current failure. Tangency, near
tangency, endpoint/domain boundaries, discontinuities, overlap, singular
gradients, root creation/loss and merge/split ambiguity remain typed.

# Compatibility and serialization

The implementation remains experimental, internal, disabled by default and
nonpersistent. Legacy `GeoLocus`, Classic intersections/output ordering, old
`.ggb` files, public commands/Path, XML/factories, frontend, 3D and G9 semantics
must remain unchanged. No migration or serialization format is authorized.

# Required tests and commands

Implement every applicable G8C validation-matrix row, including:

- secant/tangent/near-tangent/empty/multiple/endpoint cases for every family;
- rotated, translated, large/small and equation-scaled conics;
- polynomial, trigonometric, rational-pole, restricted and conditional function
  cases over explicit domains;
- regular polynomial implicit components/self-intersections and typed rejection
  of singular/nonpolynomial cases;
- scale/translation, zoom/DPI/viewport and reparameterization invariance;
- equal-coordinate distinct semantic preimages;
- Option B under all completeness states;
- dynamic creation/loss/tangency/recovery, ambiguity and atomic failure;
- downstream DAG depth 1/2/3 and repeated 1/10/100 consumers;
- deterministic counters/budgets and all forbidden-authority counters zero;
- G6/G7/G8B and Classic/public/persistence/3D/G9 non-regression.

Use analytic or independently generated high-precision references. Version
formula, runtime, library, precision, output and hashes. Do not rewrite G8A/G8B
historical evidence. Produce a G8C1 report, traceability matrix, machine-readable
evidence and integrity manifest distinguishing measured results from policy.

# Required artifacts

Update the approved extension spec, architecture/API/matrices, roadmap and user
guide only to truthful internal implementation-candidate status. Register every
upstream-tree change in `docs/upstream/modified-files.yml`. Do not document an
observable user feature.

# Verification

Add/finalize a focused G8C1 verifier following repository conventions. Run at
minimum the current canonical operational, Locus V2, G7B, G8A, G8B and G8C1
focused authorities plus the composed verifier without `-SkipBuild`, Markdown
links/static prompts/integrity checks, `git diff --check`, and cached diff check.
Record exact commands, exit codes, counts and log paths. Do not weaken any gate.

Audit the diff explicitly for public command/dispatcher, Path, XML/persistence,
legacy/Classic, frontend, 3D, G9, locus-locus and shared/global state.

# Stop conditions

Stop for author review if a residual cannot be made meaningful and scale-safe;
domains require view windows; a root requires render/sample authority; tangency
requires sign changes only; identity requires coordinates/order; completeness
requires arbitrary windows; singular/nonpolynomial support needs an unapproved
provider architecture; G8B must be weakened; or any forbidden surface is needed.

# Final disposition

Do not self-approve, promote, tag, or start G8C2/G9. A successful execution ends
with `G8C1 = IMPLEMENTATION COMPLETE — AWAITING AUTHOR REVIEW`, G8C2 not started,
G8 in progress and G9 not started, plus the complete author-decision package and
verification evidence.

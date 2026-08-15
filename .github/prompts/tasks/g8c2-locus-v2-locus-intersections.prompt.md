# Objective

Implement the author-approved G8C2 internal native Locus V2 × Locus V2
intersection kernel over the approved finite/periodic semantic domains.

**Future execution prompt — do not execute during G8C design or G8C1.** A later
task must explicitly invoke it after every mandatory gate is author-approved.

# Mandatory entry gate

Before productive editing require and report:

- a clean dedicated G8C2 feature branch from the exact published G8C1
  author-approved completion baseline required by the living roadmap;
- G6/G6R, G7, G8A, G8B-R1, G8B and G8C1 reproduce as author-approved PASS;
- G8C design is author-approved and G8C2 explicitly authorized;
- the G8C locus-locus specification is normative/author-approved;
- ADR 0008 remains Accepted and ADR 0009 is Accepted (or a current Accepted
  superseding ADR defines the exact two-parameter/source-symmetry contract);
- source-order identity, pair local isolation, tangency, overlap, completeness,
  domain limits, budgets and upstream edit set reproduce approved evidence;
- G8B/G8C1 focused plus composed verification passes before edits; and
- the current task explicitly invokes this prompt.

If any item is missing, stop with `G8C2 = BLOCKED — ENTRY BASELINE NOT
REPRODUCED`. This file alone never authorizes implementation.

# Authority and evidence hierarchy

Read current source/tests/build first, then `AGENTS.md`, canonical governance and
verification prompts, roadmap, normative G6/G7/G8/G8C contracts, Accepted ADRs
0006–0009, approved G8A/G8B/G8C1 evidence, G8C design/semantic model/API/
matrices, actual evaluator and intersection source, audited upstream curve
algorithms and versioned scientific pilots. Current source and normative
contracts override generated evidence and conversation.

Record repository/branch/HEAD/remotes/upstream/worktree, baseline tag(s), prompt
SHA-256 and evidence hashes before edits.

# Scope

Implement only native intersection of two `GeoLocusV2` semantic sources over:

- products of declared finite valid components;
- bounded × bounded, bounded × periodic and periodic × periodic domains through
  canonical fundamental-domain semantics;
- finite isolated solutions with two-sided provenance and local pair isolation;
- source-order-symmetric durable tokens and explicit ordered evidence reversal;
- transverse/tangent/undetermined classification hierarchy;
- typed established/suspected/unsupported overlap semantics;
- immutable rich result, normal-DAG rich Geo and current strict token point;
- deterministic query-local budgets/counters, tests/evidence/docs/verifiers.

# Explicitly forbidden scope

Do not add public commands/dispatcher, generic Path or point-on-locus, XML/
persistence/migration, legacy `GeoLocus`, Classic changes, render/legacy/view
authority, coordinate/list-index identity, universal merge/split genealogy,
arbitrary-window unbounded support, shared/global owner/index/cache, G7 metric
state, conic/function/implicit expansion beyond G8C1, 3D, G9, frontend or Python
DSL. Do not strengthen G6 into a general unbounded/certified provider merely to
claim completeness.

# Architectural placement

The two-source solver, algorithm and immutable evidence belong in additive
GeoCeDG-owned shared Java kernel packages and the normal Construction DAG.
References, scientific models, diagnostics and performance analysis remain
outside productive truth. Every unavoidable upstream-owned edit is minimal,
registered and separately justified.

# Required design/specification

## Two-source DAG and query

Register both loci as explicit algorithm inputs. Capture one coherent semantic
session with both identities/revisions/topologies. Compute privately and publish
one atomic immutable result. Never publish mixed revisions, stale roots or an
old point after current failure.

Canonicalize the geometric source pair independently of caller order while
retaining ordered computation evidence. `A×B` and `B×A` must map every current
solution token equivalently; reversal swaps parameters, components, revisions,
rectangle axes and determinant sign.

## Candidate isolation/refinement

Enumerate every declared branch/component product within deterministic budgets.
Use semantic component bounds and parameter rectangles for broad-phase
exclusion. Any derived spatial bound is non-authoritative and followed by
two-variable semantic refinement plus independent two-sided verification.

Implement a safeguarded hierarchy of analytic/certified capability, interval/
box exclusion where available, derivative/Jacobian refinement and evaluator-only
adaptive methods with truthful weaker guarantees. Render tessellation and
existing view-bound algorithms cannot be authority.

## Local isolation and tangency

`LocalIsolationStatus.ESTABLISHED` requires exhaustive isolation of a semantic
parameter rectangle plus certified uniqueness or a justified regular-Jacobian
uniqueness result. Small `||F(t)-Q(u)||`, Newton convergence or one candidate is
insufficient.

Use the normalized tangent determinant
`det(F',Q')/(||F'|| ||Q'||)` only for regular tangents. Nonzero supports
transverse classification under the approved guarantee. Zero/near-zero is a
tangency candidate, not multiplicity proof. Tangent/singular roots require
analytic/certified/higher-order evidence or remain unisolated/undetermined.

## Identity and Option B

Durable identity is canonical source pair + opaque solution/constructive/
branch-pair/topology continuation lineage. Parameter pair, rectangle, revisions,
residual and certificates remain evidence. Preserve equal-coordinate distinct
preimages, explicit seam maps and semantically valid reparameterization.

No universal merge/split genealogy and no coordinate/order repair. Root
appearance, disappearance, merge/split, overlap transition and topology change
preserve identity only under unique established continuation; otherwise publish
ambiguity/discontinuity.

Option B remains mandatory: an individually verified, locally isolated,
unambiguous current pair solution may drive the point with incomplete or
not-established global completeness. Parent completeness stays visible.

## Overlap and completeness

Never sample overlap into finite roots. Establish overlap only through semantic
component-wide exact/certified evidence such as a parameter map. Matching
samples/boxes yield suspected-not-established at most. Represent full, partial,
reverse/repeated traversal and mixed finite-plus-overlap only through approved
typed decomposition.

Claim `COMPLETE` only after exhaustive coverage/exclusion of all declared
branch/component products. Budget exhaustion or unsupported overlap yields
truthful weaker completeness without invalidating uncompromised Option B roots.
No viewport or arbitrary finite search window may complete an unbounded domain.

## State and work

Start query-local. Bound branch/component pairs, boxes, evaluations,
subdivisions, candidates, refinements, iterations, Jacobians, overlap checks,
continuation comparisons and diagnostics. Retain zero entries after query. A
shared owner/index requires a separate measured proposal and author approval.

# Geometric invariants and degeneracies

Preserve two-sided constructive provenance, semantic parameter pairs,
branch/component multiplicity, source-order symmetry, Option B and explicit
current lifecycle. Tangency, close roots, seam crossing, simultaneous topology
change, merge/split, overlap entry/exit, repeated traversal and unbounded-domain
limits remain typed.

# Compatibility and serialization

The capability remains experimental, internal, disabled by default and
nonpersistent. G8B/G8C1 semantics, legacy `GeoLocus`, Classic behavior, old
`.ggb` files, commands/Path, XML/factories, frontend, 3D and G9 remain unchanged.
No serialization or migration is authorized.

# Required tests and commands

Cover all applicable validation-matrix rows, including:

- transverse line-like, circle-like periodic, tangent/higher-order and close
  pair roots;
- multiple branches/components, self-intersections, repeated traversal and
  identical coordinates with distinct parameter pairs;
- `A×B` versus `B×A`, monotone/reversed reparameterization and seam crossing;
- full/partial/reverse overlap, overlap-plus-isolated contribution, suspected
  and unsupported overlap;
- bounded/periodic products and explicit unbounded rejection;
- completeness versus individual admissibility under all set-level states;
- only-A/only-B/both-source changes, merge/split, overlap entry/exit, topology
  changes, disappearance/recovery, ambiguity and atomic exception recovery;
- downstream point/DAG nesting depth 1/2/3;
- deterministic 1/10/100 repeated query/consumer counters and component-pair
  explosion;
- zoom/DPI/viewport, source-order, reparameterization, scale and translation
  invariance;
- forbidden-authority counters exactly zero, retained state zero;
- G6/G7/G8B/G8C1 and public/Classic/persistence/3D/G9 non-regression.

Use analytic/independent high-precision pair references with versioned formula,
runtime, library, precision, output and hashes. Preserve historical evidence.
Produce G8C2 report, traceability matrix, machine-readable evidence and
integrity manifest distinguishing measurements from approved policy.

# Required artifacts

Update normative approved docs only to truthful internal candidate status.
Register every upstream-tree edit. Do not advertise a user feature. Keep rich
result authority and the internal point consumer; do not add a new public type
surface unless separately approved.

# Verification

Add/finalize a focused G8C2 verifier. Run current operational, Locus V2, G7B,
G8A, G8B, G8C1 and G8C2 focused gates and the composed verifier without
`-SkipBuild`, plus Markdown/prompt/static/integrity validation, diff checks and
generated-artifact audit. Report exact commands, exits, counts and logs. Do not
weaken tolerances, tests or budgets.

Audit explicitly for commands/dispatcher, Path, XML/persistence, legacy/
Classic, frontend, 3D/G9, unbounded windows, coordinate identity and shared
state.

# Stop conditions

Stop if finite roots cannot be locally established without render/sample
authority; source symmetry requires different tokens; identity requires
coordinate/order; overlap can only be sampled; completeness needs arbitrary
windows; tangent roots are forced to false absence; a hidden/global owner is
required; G6 or G8B must be weakened; or any forbidden scope becomes necessary.

# Final disposition

Do not self-approve, promote, tag, close global G8, or start G9. A successful
execution ends with `G8C2 = IMPLEMENTATION COMPLETE — AWAITING AUTHOR REVIEW`,
G8 in progress and G9 not started, plus complete verification and author-review
decision packages.

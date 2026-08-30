# Objective

Implement **G9U0-R5 — LOCUS V2 2D SIMILARITY TRANSFORMATIONS** only after its
planning/design has closed `PASS — AUTHOR APPROVED` and the author separately
authorizes this exact prompt.

**FUTURE CANONICAL IMPLEMENTATION PROMPT — DESIGN CANDIDATE. EXECUTION IS NOT
AUTHORIZED BY THIS FILE.**

```text
G9U0-R5 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = false
implementationAuthorized = false
```

The implementation must make the ordinary existing 2D `Translate`, `Rotate`,
`Reflect`/`Mirror` and `Dilate` command families produce a new first-class
semantic `GeoLocusV2`. It must terminate at an implementation candidate and may
not self-approve, execute G9U1 or implement any later G9/G10 gate.

# Authority and evidence hierarchy

## Mandatory entry authority

Before any productive edit, require all of the following:

- current clean author-selected `main`, local/remote equality and empty index;
- G9U0-R4 `PASS — AUTHOR APPROVED`, with its annotated PASS authority present
  and ancestral;
- G9U0-R5 planning/design `PASS — AUTHOR APPROVED` and this prompt/spec blob
  belonging to that approved authority;
- that approved R5 authority selecting exactly one finite-`k=0` policy from the
  specification (Option A or Option B);
- G9U0, G9U0-R1, G9U0-R2, G9U0-R3, G8, G7, G6, G9A, G5 and legacy Locus entry
  authorities still valid;
- a roadmap that places R5 after R4 and before G9U1; and
- a separate explicit author instruction invoking this exact canonical prompt.

Stop without implementation if any entry authority is missing, drifted or
contradictory. Planning approval is not implementation authorization.

## Normative authority

Read and follow, in order:

1. current repository source/tests/build and `AGENTS.md`;
2. accepted `geocedg/specs/locus/locus-v2-semantics.md`, Locus V2 metric,
   intersection, public persistence and R2 presentation specifications,
   including Accepted ADR 0017's intrinsic semantic phase/rank authority;
3. the author-approved final form of
   `geocedg/specs/locus/locus-v2-similarity-transformations.md`;
4. `docs/architecture/g9u0_r5_locus_v2_similarity_transformations.md` for the
   inspected upstream routing and expected boundary; and
5. `docs/validation/g9u0_r5_locus_v2_similarity_transformations_validation_matrix.md`.

Current source and accepted normative contracts outrank this operational
prompt. Stop on an unresolved conflict rather than weakening a semantic rule.

# Required design/specification

## Reconfirm the upstream host surface

Before productive design or code, re-read the current 2D command authority:

- `BasicCommandProcessorFactory`, `CmdTranslate`, `CmdRotate`, `CmdMirror` and
  `CmdDilate`;
- `AlgoDispatcher`; and
- `TransformTranslate`, `TransformRotate`, `TransformMirror` and
  `TransformDilate`.

Confirm that ordinary translation routes through the dispatcher and translation
wrapper, origin rotation/dilation use their wrappers, centered rotation/dilation
route through the dispatcher to centered wrappers, point/line reflection routes
through the dispatcher and mirror wrapper, and `Reflect`/`Mirror` still share
one processor. Preserve the non-R5 vector-at-point, rotate-text and circle/conic
inversion routes unchanged. If current source no longer matches this inspected
surface, stop and update the design for author review before implementing.

# Scope

Implement only:

- `Translate[L,v]` for finite ordinary 2D vector input;
- `Rotate[L,a]` and `Rotate[L,a,C]`;
- axial `Reflect/Mirror[L,line]` and central `Reflect/Mirror[L,point]`;
- `Dilate[L,k]` and `Dilate[L,k,C]`, applying the exact author-approved `k=0`
  policy without inference or substitution;
- one normal-DAG, reconstructible transformed Locus V2 parent and immutable
  pointwise similarity evaluator;
- new durable output identity plus exact source/transformation dependencies;
- ordinary downstream Point, metric, intersection and transformation closure;
- normal style initialization, persistence, copy/remap and undo/redo;
- focused tests, deterministic evidence, verifier/composed integration,
  modified-files registration and validated candidate documentation; and
- a definitive prospective post-R5 G9U1 prompt, prepared or superseded only
  after the R5 implementation candidate is fully green and never executed in
  this phase. Preserve its kernel-selector/exact-token-only candidate-marker
  hit testing, explicit create-one/create-all and opt-in visible frontend
  auto-materialization transaction with no UI/list/marker rank authority,
  professional menus/tools, GeoCeDG visual identity, enforced existing-host
  `Continuity = OFF` product invariant with Classic configurability, and
  `geocedg.brand.topbar` / `geocedg.brand.startup` requirements.

# Explicitly forbidden scope

Do not:

- add `TranslateLocusV2`, `RotateLocusV2` or another parallel command family;
- implement `Path`, `Transformable`, `Translateable`, `Rotatable`, `Mirrorable`
  or `Dilateable` merely to reuse upstream mutation algorithms;
- modify the generic transformation algorithms to treat an unpublished
  `GeoLocusV2` copy shell as semantic output;
- transform render vertices, samples, metric partitions or screen geometry;
- include circle inversion, shear, non-uniform affine stretch, projective or 3D
  transformations;
- serialize a render cache, point cloud, detached matrix or Java lambda;
- reuse the source locus durable ID or source intersection tokens;
- infer identity from label, XML/construction order, coordinate, list index,
  sampling index, proximity or screen position;
- merge semantic branches/components because their images coincide;
- change ZIP/XML format or `app="classic"`;
- introduce a transformation-specific runtime flag;
- implement candidate markers, workspace/profile behavior or any G9U1 action;
  or
- commit, push, merge, tag or self-approve without a separate author
  instruction.

# Architectural placement

Add narrow `GeoLocusV2` overloads to the existing four command processors.
Each must enforce the existing Locus V2 access policy and immediately delegate
to `LocusV2PublicOperations`. Command processors own argument routing only.

Use a public reconstructible `AlgoLocusV2` parent whose ordinary serialized and
update inputs are exactly source plus vector/angle/center/axis/factor geos. Use
one immutable similarity description/evaluator that captures a current source
definition and maps its valid finite evaluations. Implementation class names
are not prescribed; one semantic authority is.

Publish the output and direct dependencies through the existing atomic
participation/identity seam. Every transformed output receives a new durable ID
even for an identity map. Do not misuse branch lineage as an object-provenance
graph.

The current upstream click tools select mutable transform interfaces. Do not
broaden those interfaces or `TestGeo` predicates. Unless a later author
instruction explicitly adds bounded pre-G9U1 click-tool parity, R5 public
creation is the existing commands and G9U1 owns professional discoverability.

# Geometric invariants and degeneracies

## Semantic contract

Implement:

```text
L'(u) = T(L(u))
```

Evaluation first uses the source provider and immutable source definition at
the same canonical `(branch,u)`. Propagate invalid source evaluations and map
only valid finite points. Report transformed overflow as `NON_FINITE` without
stale coordinates.

Preserve source declared domain, valid components, branch keys, current branch
lineage, periodic canonicalization/seam and semantic traversal. Axial reflection
or negative scale changes ambient geometry, not source parameter authority.
Those preserved semantic-domain and traversal authorities are the ordering frame
from which the R4 intersection pipeline derives intrinsic phase/rank for a new
transformed query. R5 must not copy phase/rank from an untransformed query or
derive it from transformed Cartesian, solver-list or presentation order.

The semantic snapshot signature must be deterministic and depend on source
identity/revision, transformation family/version and normalized current
coefficients. It must not depend on presentation or labels. Style changes
produce no semantic revision.

## Required degenerations

- Undefined/unpublished source: undefined transformed output, no stale state.
- `EMPTY_DOMAIN`: retained exactly.
- Undefined/nonfinite/3D vector, angle, center, axis or factor: explicit invalid
  transformed state.
- Invalid line normal: explicit invalid axial-reflection state.
- Finite mapped overflow: `NON_FINITE` at that address.
- Periodic/disconnected/empty source: same seam/component/domain authority.
- Finite `k=0`: **AUTHOR DECISION REQUIRED before implementation**. Apply
  exactly one author-approved policy:
  - **Option A (recommended):** a valid semantic locus over the retained
    source-valid domain, carrying `COLLAPSED_IMAGE` and rich length zero. It
    must not become an unparameterized point, and invalid source addresses
    remain invalid.
  - **Option B:** an explicit unsupported/undefined transformed state, with no
    valid or stale semantic snapshot and deterministic recovery after a
    supported factor returns.
- Further transformations after `k=0` or a temporary undefined state: follow
  the selected policy through normal DAG behavior and deterministic recovery.

The accepted semantic and metric contracts define consequences for a valid
collapsed image but do not select the future command policy. Stop before any
productive edit if the author-approved authority does not choose exactly one
option or if implementation would contradict the selected option.

## Metric, point and intersection covariance

Prove rich total and partial length invariance for translation, rotation and
reflection and `abs(k)` scaling for dilation. The transformed locus itself must
remain the metric source; do not multiply only a presented scalar.

Prove `Point[T(L),branch,u]` geometrically corresponds to transforming the
source semantic point at the same address, while retaining distinct point and
locus identities.

Use the ordinary rich intersection pipeline. For supported invertible maps
applied to both operands, prove geometric covariance within current evidence.
Transformed result/source-pair identities and exact tokens are new. Never reuse
or transform source tokens. Do not claim bijective covariance for `k=0`.
Under Option A retain the current overlap/non-isolated policy; under Option B
fabricate no intersection from the unsupported/undefined transformed locus.

Consume the author-approved R4 deterministic current-snapshot selector,
including its intrinsic semantic phase/rank proof. The transformed locus's
preserved domain, component lineage, periodic seam and semantic orientation
drive a newly derived phase/rank in the transformed source-pair context.
Current transformed-query semantic evidence, not previous Cartesian roots,
solver/list/marker order or transformation-update history, determines token
admissibility and materialized-point binding. Prove that direct, incremental,
reverse and save/reopen paths to the same final Construction state and durable
IDs produce the same current bindings. Ordinary continuity must hold while
that deterministic selector remains uniquely valid; topology ambiguity
invalidates rather than guesses. Geometric covariance never reuses an
untransformed or previous-frame selector certificate, phase/rank allocation or
exact token.

## Closure, dynamic dependencies and style

Every transformed output must accept every supported R5 transformation again.
Source and transformation inputs remain ordinary DAG dependencies; changes
propagate to downstream points, metrics, intersections and later transforms.

Initialize output presentation with the ordinary host transformation style
convention and retain the R2 `GeoElement` style model. Style is never semantic
or token authority.

# Compatibility and serialization

Persist only the existing command and ordinary input geos plus current durable
identity/style records. Reopen must reconstruct the evaluator from those
inputs. Preserve current `.cedg`, `.ggb` compatibility-input, ZIP/XML,
`app="classic"`, copy/remap and undo/redo contracts.

Feature-on permits interactive creation. Feature-off preservation/file loading
may reconstruct supported objects, but GeoCeDG Classic gains no experimental
creation authority. No new feature flag is permitted.

# Required tests and commands

Implement every selected row in the R5 validation matrix, including:

- exact public command forms and excluded circle/3D/affine overloads;
- new durable identity and exact dependency records;
- domain/branch/component/parameter/orientation/periodic preservation;
- transformed-query intrinsic phase/rank derived from that preserved semantic
  frame, independent of Cartesian, solver/list, marker and UI order;
- translation/rotation/reflection/dilation evaluation;
- positive, negative and identity factors, plus only the conditional zero-factor
  rows belonging to the selected author-approved policy;
- metric, Point and intersection covariance;
- transformed selector/phase-rank and intersection-token non-reuse;
- transformed-intersection current-selector path independence across direct,
  incremental, reverse and save/reopen update histories, plus ordinary
  continuity while that selector remains uniquely valid;
- transformation closure and dynamic recompute;
- undefined/nonfinite/overflow/empty/disconnected/collapsed states;
- style semantic-invariance;
- `.cedg` save/reopen, copy/remap, rename and undo/redo;
- feature-off Classic preservation/no creation; and
- absence of generic `Path`, mutable transform-interface or sampled authority.

Run the focused R5 verifier twice and require identical canonical evidence.
Then run G9U0, R1, R2, R3, R4, G8 intersection, G7 metric, G6 semantic, G9X1,
G5, relevant G9A, legacy/scientific Locus, command/frontend compatibility,
Checkstyle, `git diff --check`, `git diff --cached --check` and full
`tools/agent/verify.ps1` without weakening a gate. Generated logs belong only
under ignored `artifacts/`.

# Required artifacts

Following current repository convention, produce:

- minimum shared-kernel source and focused tests;
- canonical R5 scenario/evidence/hash files;
- one focused R5 verifier and its composed insertion after R4 and before G9U1;
- exact upstream modified-files registration;
- implementation architecture and candidate report;
- living roadmap/traceability/spec-index updates;
- user/developer guide updates only for validated observable candidate
  behavior; and
- a short manual author smoke plan covering all four command families,
  downstream Point/metric/intersection, deterministic transformed-query
  path-independence, dynamic update, the selected `k=0` policy, composition and
  `.cedg` reopen.

Do not mark that manual smoke PASS.

# Stop conditions

Stop and report before broadening if:

- the mandatory authority/clean entry is absent;
- an existing command cannot be extended without a parallel command or a broad
  dispatcher rewrite;
- correctness appears to require generic `Path` or mutable transform-interface
  conformance;
- source domain/parameter/branch authority cannot be retained;
- a new XML/archive format or serialized evaluator closure appears necessary;
- the finite-`k=0` author decision is missing, ambiguous or cannot be
  implemented exactly as approved;
- metrics/intersections require render data, source selector/phase-rank reuse or
  token reuse;
- deterministic evidence cannot be reproduced;
- an unexplained historical/composed gate regresses; or
- completion would require G9U1, candidate markers, G9B, G9C, G9U2 or G10.

# Terminal state

After productive code and all automated validation are complete, stop at:

```text
G9U0-R5 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U1 = DESIGNED — NOT AUTHORIZED
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

Do not commit, push, merge, tag, claim PASS or execute G9U1 unless a later
author instruction explicitly authorizes the corresponding action. STOP FOR
AUTHOR REVIEW.

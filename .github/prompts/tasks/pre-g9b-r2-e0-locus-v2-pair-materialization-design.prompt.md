# PRE-G9B-R2-E0 — Locus V2 pair exact-token materialization extension design

**PROPOSED FUTURE PROMPT — UNEXECUTED AND NOT AUTHORIZED.**

Created under `AD-R0-9`, the separately authorized governance-layer task, from
the author-approved `PRE-G9B-R0` closeout and its `AD-R0-1` amendment. The
existence of this file is not authorization. Execution requires a new explicit
author instruction naming `PRE-G9B-R2-E0` and its exact implementation base.

```text
selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = SEMANTIC CAPABILITY EXTENSION DESIGN
PRODUCTIVE_IMPLEMENTATION_AUTHORIZED = false
```

<!-- geocedg-field: objective -->
## Objective

Design the **maximum scientifically defensible** extension of exact-token point
materialization for intersections between first-class semantic 2D curves, and
state per family exactly how far it is defensible.

The goal is the largest rigorously supportable semantic family — **not** merely
making one witness file work.

This phase is **design only**. No productive implementation is authorized in
`E0`. Its outputs are ADR and specification *candidates*, produced only when
`E0` itself is separately authorized.

Context, which must not be restated as a defect: `PRE-G9B-R0` classified `D1`
as `INTENTIONALLY_UNSUPPORTED_CURRENT_CAPABILITY`. The current rich-only
behaviour of unsupported generic semantic-curve pairs **conforms** to the
approved contracts. `AD-R0-1` schedules a capability extension; it does not
reclassify the current behaviour as a defect or as technical debt.

Declare `CHANGE_ROUTE = ORDINARY`. Freeze `VERIFICATION_CLASS` at `E0` start
from its actual footprint; design work carries no product acceptance.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE = UNRESOLVED — MUST BE FROZEN AT AUTHORIZATION
```

No future commit or tree is fixed here, and none is invented. Before execution,
replace this field with the exact commit and tree the author explicitly
authorizes for this phase. A moving branch name is not sufficient. **Execution
is forbidden while this field remains unresolved.**

Planning authority base, recorded as provenance only and **not** as this phase's
future implementation identity: the author-approved `PRE-G9B-R0` closeout,
commit `48b182b8ab09d9b13a92d641ae120cd20f47f240`, tree
`cb5eacae18d36ad5ea1dfa0972b7d01ca64e1b6d`.

## Authority and evidence hierarchy

`AGENTS.md`; current kernel source for the locus, spline and intersection
packages; `geocedg/specs/curves/spline-v2-pair-materialization.md`,
`geocedg/specs/curves/semantic-spline-2d.md`,
`geocedg/specs/locus/locus-v2-intersections.md` and
`geocedg/specs/locus/locus-v2-public-surface.md`; the accepted ADRs governing the
rich intersection result, pair semantics, token selection and spline-pair
singleton-germ materialization; then the
[PRE-G9B-R staged design](../../../docs/architecture/pre_g9b_r_final_stabilization_and_authoring_readiness_design.md)
§2.1 and §4 and the
[R0 candidate report](../../../docs/validation/pre_g9b_r0_characterization_candidate_report.md)
§3 as planning input only.

R0 planning prose is **not** an accepted specification and does not become one
by being referenced here. Where detailed normative content already exists,
reference it rather than restating it.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

Design and contract candidates only, covering the families and characterization
below.

### Families in scope

1. existing certified `SplineV2 × SplineV2` — the **approved reference
   baseline**, not to be redesigned gratuitously;
2. `SplineV2 × generic LocusV2`;
3. `generic LocusV2 × SplineV2`;
4. `generic LocusV2 × generic LocusV2`;
5. supported semantic images and compositions of those families where provenance
   remains certifiable.

**Family A — certified `SplineV2 × SplineV2`.** Treat the current `G9S1-R1`
contract as the normative baseline. Do not weaken authenticated spline
provenance, the exact structural selector, current semantic revisions, branch and
component identity, local existence and isolation, ambiguity exclusion, the
exact-token lifecycle, persistence and currentness rules, fail-closed behaviour,
or explicit point materialization. Identify which parts of that contract are
genuinely spline-specific and which are reusable semantic-pair principles.

**Family B — `SplineV2 × generic LocusV2`.** A **mandatory design target**. The
author witness `artifacts/author-input/post-p1-defects/SeveralDefects.cedg`
contains the required scenario: `d = SplineV2(…)`, `a = LocusV2(E, C)`,
`e = Intersect(d, a)`. Establish what additional semantic and certification
capability is necessary for `e` to expose exact admissible root tokens when its
roots are locally certifiable. Do **not** solve only the operand order shown in
the witness: caller reversal must preserve the same semantic root identity after
canonical normalization.

**Family C — `generic LocusV2 × generic LocusV2`.** Explicitly in scope.
Determine how far the current rich-only restriction can safely be relaxed,
presupposing neither that every generic pair can be materialized nor that only
polynomial or spline-based pairs ever can.

### Architectural rule — capability, not class name

Eligibility must **not** be defined by Java class name alone. Several semantic
families already share `GeoLocusV2` as runtime representation while carrying
different evaluators, provenance, domains, parameterizations and proof
capabilities. The governing question is not

> "is this object a `GeoLocusV2`?"

but

> "can both semantic operands provide enough authoritative information to
> define, certify, persist and re-identify this intersection root without
> coordinate matching?"

Prefer capability-based admission while retaining explicit family boundaries
wherever required for correctness. A capability is admissible only when its
mathematical **and** lifecycle guarantees are actually established.

### Required characterization, per candidate family

Each must be defined or explicitly rejected:

- **Operand provenance**, per side: durable source identity; semantic
  provider/family; branch lineage; component lineage; semantic-domain kind;
  orientation; parameterization contract and version; current semantic revision;
  transformations and compositions; whether the evaluator is authoritative or
  merely numerical.
- **Structural root identity**: a durable selector defined independently of
  coordinates. Investigate whether the existing
  `pair-singleton-transverse-germ/v1` scheme can be generalized or whether a new
  selector is required. An existing selector's meaning must never be mutated
  silently; a new selector is versioned explicitly.
- **Branch / component / domain / orientation ownership.**
- **Local proof and isolation**: what constitutes sufficient proof of existence,
  local uniqueness, isolation, regularity or transversality where applicable, and
  correct association with both semantic source addresses.
- **Ambiguity**: fail closed when the semantic data cannot distinguish multiple
  roots sharing the proposed durable class, branch or component ambiguity,
  overlapping parameterizations, singular intersections, tangency or multiplicity
  where identity is insufficiently established, unresolved topology,
  self-intersection side ambiguity, or source-side ambiguity.
- **Symmetry and caller reversal**: canonical source ordering, orientation
  effects, determinant and germ-sign normalization, source-A/source-B address
  storage, copy and remap where source order changes, persistence and reopen.
- **`S × S` self-intersections** and periodic / seam cases.
- **Currentness**, dormant and reactivation lifecycle.
- **Persistence and versioning**, copy, redefine and undo.
- **Bounded work.**
- **Capability matrix** and the explicit statement of unsupported residual
  families.

### Admissibility levels

Produce an explicit level hierarchy rather than one boolean, for example:

```text
rich intersection supported
  -> root discovered
  -> root locally verified
  -> root locally isolated
  -> semantic identity unambiguous
  -> exact durable token admissible
  -> ordinary point materializable
```

Different source families may stop at different levels, and the contract must
state those levels explicitly.

### Required capability matrix

Use the **actual** provider inventory from the live repository. Do not invent
families.

| Pair family | Rich intersection | Local root verification | Structural identity | Exact token | Point materialization |
|---|---|---|---|---|---|
| `SplineV2 × SplineV2` | current | current | current | current | current |
| `SplineV2 × reconstructible LocusV2` | determine | determine | determine | determine | design target |
| `reconstructible LocusV2 × SplineV2` | symmetric / canonical equivalent | determine | determine | determine | design target |
| `reconstructible LocusV2 × reconstructible LocusV2` | determine | determine | determine | determine | design target |
| other supported LocusV2 provider families | characterize individually | | | | |
| unsupported / uncertifiable family | truthful rich or unsupported state | fail closed | none | none | none |

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

No product or kernel implementation. No weakening of the `G9S1-R1` baseline. No
generic interface introduced merely so that types pass certification. Sampling,
render geometry, tessellation, or a floating root plus an arbitrary epsilon
neighbourhood must never be treated as proof. Python or external analysis may
validate designs but must never become the runtime semantic authority. Do not
create the ADR or specification before `E0` itself is authorized. Do not touch
`R3`–`R7`, `G9B`, `G9C`, `G9U2`, productive `G10` or further `G12`. Do not
publish, tag, release or push a publication ref.

## Architectural placement

Shared Java kernel and normative geometric contract. Materialization
admissibility is semantic truth and belongs in the kernel dependency graph; no
frontend, render, help or presentation layer participates.

## Required design/specification

The `E0` deliverables, produced only when `E0` is separately authorized:

1. a dedicated ADR for generalized semantic-pair exact-token materialization;
2. normative amendments to the Locus V2 intersection specification;
3. amendments and an explicit supersession relation to the accepted spline-pair
   materialization ADR where required;
4. any selector or token schema required;
5. the explicit capability and admissibility matrix;
6. the lifecycle and persistence contract;
7. the implementation architecture decision;
8. the focal and adversarial verification matrix;
9. the migration and backward-compatibility design;
10. an explicit statement of unsupported residual families.

`E0` may conclude that some `LocusV2` families remain rich-only. That is
acceptable **only** when justified by a missing semantic or proof capability,
never because a family merely is not `SplineV2`.

## Geometric invariants and degeneracies

**The mandatory identity model.** The public constructive chain remains:

```text
semantic curve A + semantic curve B
  -> rich semantic intersection
  -> current locally admissible semantic root
  -> exact durable token
  -> explicit ordinary-point materialization
  -> normal DAG propagation
```

The ordinary point is a derived child of the semantic intersection claim and must
never become an anonymous coordinate detached from its constructive provenance.

Root identity must not depend on Cartesian coordinates, coordinate tolerances,
nearest-root searches, render geometry, screen position, discovery order, output
index, construction order, labels, XML position, transient numerical boxes, the
current Newton seed, sampled rank, incidental subdivision structure, or Java
object identity. Parameter values may constitute current proof or address
evidence where the semantic contract allows, but must not silently become the
durable identity if they can drift or be reparameterized.

**`local admissibility != global completeness`** must be preserved. A root may
feed a downstream CeDG construction when it is semantically sourced, current,
locally verified, locally isolated, structurally identifiable, non-ambiguous and
associated with authoritative parameter evidence on **both** operands — even when
the complete rich intersection result remains `INCOMPLETE` or `NOT_ESTABLISHED`,
provided no unresolved global condition can invalidate that local root's
identity. Global completeness must not be upgraded merely because one local root
is admissible; conversely, a globally `COMPLETE` result must not make an
ambiguous local root materializable.

**Local certification.** The spline interval-polynomial mechanism must not be
assumed to be the only possible proof method, but any alternative must carry
equivalent mathematical authority for its family. Candidates to investigate,
where mathematically justified: interval evaluation over authoritative semantic
evaluators; interval Newton or Krawczyk style certification; certified implicit
or parametric residuals; monotonic or regular semantic segments; provider-issued
local uniqueness certificates; exact or outward-enclosed derivatives and
Jacobians.

**Generic Locus V2 proof capability** is a mandatory study case: determine
whether the reconstructible evaluator used by `a = LocusV2(E, C)` can support
rigorous local root certification and, if not, the *minimum additional kernel
capability* required. Possible but not predetermined outcomes: bounded interval
evaluation; derivative or Jacobian authority; provider-issued local germ
certificates; exposing exact reproducible parameter-domain structure; another
bounded proof capability compatible with the Locus V2 contract.

**Self-intersections.** `S × S` and other equal-source pairs must be addressed
explicitly. The two semantic preimages must never be inferred from the single
Cartesian intersection coordinate. Characterize at least `u != v` ordinary
self-crossing, periodic equivalents, seam aliases, tangencies, repeated
traversal, overlapping branches and components, and the `u = v` trivial diagonal
— which must not become an ordinary self-intersection root unless the
mathematical contract explicitly intends it. **Coordinate equality alone must
never merge semantic preimages.**

## Compatibility and serialization

Specify native `.cedg` behaviour: token and selector persistence; both source
identities; semantic source-address ownership; currentness revalidated on load;
saved coordinates never sufficient evidence; saved proof boxes not automatically
current proof; old documents remain loadable; no old rich-only pair silently
materialized after reopening unless explicitly requested and currently
admissible; unknown selector versions fail closed; malformed or contradictory
pair records fail closed; copy, duplicate and macro remap to new durable
identities with appropriate lineage; rename preserves identity; compatible
redefine follows the approved identity rules; incompatible replacement cannot
match by label or coordinate. Any required ledger-format migration is defined
explicitly rather than by overloading an existing version.

**Lifecycle.** Define active/current; temporary proof loss; temporary root
disappearance; ambiguity; unsupported temporary source state; semantic-revision
changes; dormant or undefined child; reactivation of the same semantic slot;
incompatible source replacement; permanent disposal. A temporary loss of
numerical or certification evidence must not automatically allocate a new point
identity. Where the same durable semantic selector later becomes valid again,
prefer reactivating the same materialized child when consistent with the approved
lifecycle model. Slot recurrence must never be read as physical trajectory
continuity through a singularity.

**Relationship to `R2`.** `R2` and `R2-E0`/`E1` stay conceptually distinct and
neither substitutes for the other. `R2` asks whether an already materialized
point can serve as a semantic endpoint; `E0`/`E1` ask which pair roots may be
materialized at all. `E0` must nevertheless ensure any new materialized pair
point exposes provenance in a form `R2` can consume **without coordinate
matching**. Two incompatible provenance mechanisms must be avoided; if necessary,
define a shared kernel provenance abstraction whose semantics are specified
before either implementation depends on it.

**Dynamic behaviour and DAG.** Materialized points remain normal dynamic GeoCeDG
construction objects. Recomputing the sources must re-evaluate the same selector
and must create **zero** new points unless the user performs a new explicit
creation action. A root solver must not run independently per materialized child
when the rich parent already owns the intersection evaluation.

**Work bounds and determinism.** Specify bounded work. An unrestricted global
topology solver must not be authorized merely to materialize every possible
generic locus pair. Characterize bounded discovery; bounded proof and
certification; budgets; failure status on exhausted budget; deterministic
partitioning; lookup complexity for existing materialized children; retained
ledger size; and the absence of unbounded trajectory or root history. **Budget
exhaustion means lack of current proof, not permission to approximate.**

<!-- geocedg-field: required_checks -->
## Required tests and commands

None — `E0` is design. No focal test is executed by this phase.

The adversarial case list below becomes `E1`'s test obligation and must be
carried into the `E0` verification matrix deliverable: no intersection; one
transverse isolated root; multiple roots; two roots in the same candidate class;
tangency; multiplicity; overlap or coincident segment; singular Jacobian;
disconnected components; periodic seam; repeated traversal; self-intersection;
source-order reversal; parameterization or orientation reversal; source motion
through `1 -> 0 -> 1`; source motion through `1 -> many -> 1`; temporary
evaluator invalidity; compatible redefine; incompatible redefine; copy;
undo/redo; native save and reopen; dormant save, reopen and reactivation;
corrupted or old ledger; incomplete global search with a locally certified
admissible root; globally complete result with a locally ambiguous root; budget
exhaustion.

**The `SeveralDefects.cedg` mixed pair is a mandatory positive-target fixture
once `E1` is authorized.**

Run `git diff --check` and whatever trivial documentary checks the design
artifacts require. Design work produces no product acceptance evidence.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. Design existence is not execution
authorization, and technical verification never creates author approval.
Execution requires a new explicit author instruction naming `PRE-G9B-R2-E0` and
its exact implementation base.

**This prompt does not authorize creating the `E0` ADR or specification now.**
Those are produced only when `E0` itself is separately authorized, and their
acceptance is a further explicit author decision.

`R2-E1` is separately and additionally blocked: it requires
`R2-E0 = AUTHOR APPROVED DESIGN` and an exact author-approved `E0`
ADR/specification candidate. `R3`–`R7` and every later gate remain unauthorized.
No tag, release, publication or commercial action is implied by this prompt's
existence.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A clean local candidate commit is authorized. Push, branch publication, merge,
promotion, tag, release, binary publication and any rewrite of published history
are forbidden, and each requires a separate explicit author instruction naming
the exact candidate SHA.

## Acceptance and closeout

The phase stops with a design candidate pending author review. Author approval
of the `E0` contract is the explicit precondition of `E1`, and is a decision
naming the exact accepted commit and the exact accepted ADR/specification
candidate.

## Required artifacts

The ten `E0` deliverables listed under *Required design/specification*, plus a
candidate report under `docs/validation/` and machine-readable evidence under
`geocedg/validation/` where the design produces machine-checkable structure.
Record `GUIDE_IMPACT` and the bootstrap- and infrastructure-impact outcomes.

## Stop conditions

Stop and report rather than guess for any family whose admission would require
coordinate, proximity or order matching; for any proposed capability whose
mathematical or lifecycle guarantees are not actually established; if the design
would weaken the `G9S1-R1` baseline; if a generic interface would have to be
introduced merely to make types pass certification; or while
`IMPLEMENTATION_BASE` remains `UNRESOLVED`.

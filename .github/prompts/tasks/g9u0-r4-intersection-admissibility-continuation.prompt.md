# Objective

**G9U0-R4 — PUBLIC LOCUS V2 INTERSECTION INITIAL ADMISSIBILITY AND
CONTINUATION CORRECTION**

**CANONICAL IMPLEMENTATION AUTHORITY — CORRECTIVE ITERATION PENDING AUTHOR
RE-REVIEW. THIS FILE DOES NOT CLAIM PASS, AUTHORIZE G9U0-R5 IMPLEMENTATION OR
AUTHORIZE G9U1.**

Correct the false first-publication deadlock and the later regular-motion
invalidation exposed by the author. A materialized exact-token point must be
resolved from the current semantic snapshot deterministically. Continuity is
desirable evidence, but movement history must never become identity authority:

```text
deterministic semantic selection
    >
continuity heuristic
```

# Authority and evidence hierarchy

- Start from `ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b`, the exact commit peeled by
  annotated tag object `1c1be8ebb58be9ad4c4e7242bc56105f9f310068`
  (`geocedg-g9u0-r3-pass`).
- Work only on `feature/g9u0-r4-intersection-admissibility-continuation`.
- The byte-exact primary fixture is
  `source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg`:
  13,301 bytes, SHA-256
  `47280a65aeec2d4f3f8edb969a934bbb40e1974c22dfe7e121011feae47abc7c`.
- The byte-exact four-solution characterization fixture is
  `source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg`:
  14,601 bytes, SHA-256
  `51dcf7a002cb3984bb4cf5843d50e100f4bc8ef91217d4502fa7987c5b1ec21c`.
  It comes from ignored author artifact
  `artifacts/smoke-test-g9u0-r2/fouSolutions.cedg`; preserve the historical
  filename typo as provenance, not as semantic authority.
- Preserve this author-smoke chronology:
  1. first R4 candidate made both midpoint/circle roots initially admissible;
  2. author smoke then found one materialized point becoming undefined during
     small regular motion;
  3. that smoke is
     `FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION`;
  4. the deterministic correction then passed ordinary-motion smoke but the
     second smoke exposed four finite roots that could not be materialized;
  5. retain that result as
     `IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE`;
  6. characterization proved a base-selector collision, not missing local
     numerical certification;
  7. the author accepted ADR 0017 and authorized intrinsic oriented-domain
     phase/rank only for repeated base selectors;
  8. the bounded phase/rank correction is implemented;
  9. pre-final replacement focused A/B execute 50/50 methods with exact
     canonical-summary SHA-256
     `c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`;
  10. final hardening rejects intrinsic-phase use of legacy singleton token
      material at runtime and import, and preserves token/key association while
      canonically ordering merge/split parent evidence;
  11. final focused A/B again execute 50/50 methods with exact canonical-summary
SHA-256 `f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`;
  12. the full composed authority exits 0 with
      `All GeoCeDG verification gates passed.`; and
  13. author re-smoke remains pending, so R4 still claims no PASS.

Stop before editing if entry/tag/fixture authority is not exact. Current code,
accepted specifications and sealed historical gates outrank generated logs or
informal summaries.

# Scope

R4 is limited to the shared-kernel path from verified current finite roots to
deterministic root selection, component-scoped unresolved-candidate evidence,
canonical target-contact orientation, bounded intrinsic phase/rank, opaque token-ledger
allocation/restoration and public exact-token point materialization. It
includes strict v3 ledger persistence, bounded import of canonical v1/v2
states and exact pre-R4/v1 singleton migration, focused tests, deterministic evidence and
living R4 documentation.

The bounded follow-up may freeze byte-exact four-root characterization,
separate root existence, deterministic identity, local evidence, global
completeness and policy eligibility, and prepare prospective G9U1 policy. It
must not implement a weaker materialization mode when deterministic identity
is ambiguous.

After the R4 candidate is technically complete, only planning/design material
may be maintained for `G9U0-R5 — LOCUS V2 2D SIMILARITY TRANSFORMATIONS`. R5
productive implementation is not authorized.

# Explicitly forbidden scope

Do not change intersection geometry merely to make a root admissible. Do not
identify a solution by Cartesian coordinate, nearest previous point, list
order, solution/solver index, sampling index, screen position, viewport or
arbitrary proximity. Do not make the previous snapshot the sole source of
current identity. A semantic parameter, isolating interval or exact equality
of parameters across revisions is revision evidence, never durable selection.

Do not relax tangent, pair, overlap, merge/split, stale, non-isolated,
same-component unresolved or otherwise ambiguous cases. Do not use extrinsic
solver/list/output/UI order as root rank. The only authorized rank is the
ADR-0017 intrinsic oriented phase of a repeated base selector inside its full
semantic frame. Do not change rendering, markers,
inspector UX, generic `Path`, archive structure, `app="classic"`, token
contents, G9 durable identity or historical reports.

Do not describe an exact opaque token as exact arithmetic. Do not call an
`ESTIMATED_ERROR` root numerically certified. No weaker materialization tier is
authorized in R4; any future relaxation requires separate author approval and
can never override a duplicate deterministic selector, absent exact token or
ambiguous lineage.

Do not implement R5, G9U1, G9B, G9C, G9U2 or productive G10. Do not commit,
push, merge, tag or self-approve without separate author authorization.

# Architectural placement

The correction belongs in shared-kernel intersection identity and token-ledger
publication. The numeric solver remains geometry, refinement and local-proof
authority. A public identity resolver executes after isolation/refinement and
before the existing atomic ledger commit.

For every eligible current root, derive one intrinsic base selector from:

```text
stable semantic component lineage
+ typed oriented transverse current-root germ
```

The germ includes the target contact-indicator identity and its established
orientation. The enclosing ledger material and binding separately certify the
durable result owner, source pair, constructive lineage, topology context,
provider/parameter contract and target contract. The selector contains no
coordinate, parameter value, candidate order or historical position.

The contact orientation must be canonical under a legitimate nonzero scalar
change of an equivalent target representation. Line and segment support
normals use one deterministic unoriented-normal convention; central conics use
their center-level sign, parabolas use their nonzero quadratic trace, and
regular polynomial implicit curves use their canonical leading nonzero
monomial. These rules remove only representation-sign/scalar freedom. A ray's
direction is geometric semantics and remains part of its oriented contact
contract; it must not be normalized away as if the ray were an unoriented
line.

A unique base selector remains unextended. Only roots sharing one exact base
selector may receive the Accepted-ADR-0017 extension:

```text
stable branch/component lineage
+ typed transverse germ/collision class
+ declared semantic orientation
+ periodic/nonperiodic domain kind
+ verified collision-group cardinality
+ intrinsic oriented phase/rank
```

Canonical semantic parameters and pairwise disjoint isolating intervals prove
the oriented order; their raw values/bits do not become durable identity. The
ordinal alone is never authority. A complete selector becomes allocation
authority only when unique in the current constructive snapshot. Complete
selectors are processed in canonical selector order so every solver
enumeration permutation produces the same association. A repeated complete
selector remains explicit ambiguity and receives only a revision-local handle.

The four-solution author fixture proves the correction concretely. Its four
current transverse roots are locally isolated with `ESTIMATED_ERROR`, no
unresolved candidates and global `NOT_ESTABLISHED`; two have the positive
circle-residual germ and two the negative germ. Each repeated base-selector
pair has disjoint intervals in one coherent oriented component, so the complete
phase selectors are unique and all four roots receive exact materializable
tokens. This changes neither certification nor global completeness.

The ordinary upstream conic-intersection lifecycle is characterization only,
not reusable authority: Continuous mode matches by Cartesian distance and
history; deterministic mode freezes output-slot/permutation state derived from
that machinery. Both violate the R4 prohibition on coordinate, history or
solver/list-slot identity.

On first publication, the ledger allocates a fresh opaque identity and the
result reports `NEW_TOPOLOGICAL_SOLUTION`, `APPEARED` and
`continuationEstablished=false`. On recomputation, the ledger resolves the
current unique selector against its exact persisted binding, updates only the
revision address proof, preserves the byte-identical token and reports
`DETERMINISTIC_SELECTION_ESTABLISHED`.

Previous/current root comparison is retained only for bounded continuity and
topology diagnostics. When it uniquely relates the same already-selected
token, ordinary motion may additionally report continuity evidence. It never
chooses or changes the current token. A regular root may change semantic
parameter without losing its selector. The same final current snapshot must
therefore produce the same binding after a direct update, many small updates,
forward/reverse motion or save/reopen, provided no real topology event is
crossed.

For a complete ranked periodic collision group, reuse of any old token requires
a complete bijection from every prior root to one current root with the same
complete selector. Missing, ambiguous, budget-exhausted, incomplete or
non-bijective evidence invalidates the whole prior group. This relation only
guards reuse; it never selects the current root. Missing bounded relation
evidence on an ordinary nonperiodic group is not identity authority and does not
override its unique current selector.

Observed tangency, merge, split, disappearance, overlap, branch ambiguity or
unresolved isolation remains conservative and may burn/invalidate an old
allocation rather than guess. After such an event only current deterministic
authority can justify any allocation; Cartesian resurrection is forbidden.

Merge/split candidate-parent evidence must be deterministic without becoming
identity. Canonicalize the complete parent records once by opaque token, then
derive parent-token and optional continuation-key evidence from that same
ordered sequence. Never sort those lists independently or lose their
association; the event remains ambiguous and point-inadmissible.

Collision-group cardinality, component/topology or declared orientation change
is an identity barrier and never shifts old tokens to a later rank occupant.
For a periodic component the declared oriented fundamental interval is the
deterministic phase frame. If a ranked interval reaches the seam, publish typed
identity discontinuity and invalidate affected tokens rather than rotate them.
This is the explicit monodromy boundary; no mutable winding history is allowed.

# Required design/specification

The bounded R4 refinement in
`geocedg/specs/locus/locus-v2-intersections.md` §§6.2, 8.2 and 10 is the living
normative candidate. Accepted ADR 0017 is the bounded phase/rank decision. It
preserves the stronger G8 identity, topology and fail-closed rules while making
current deterministic selection authoritative.

Preserve:

```text
LOCAL POINT ADMISSIBILITY != GLOBAL COMPLETENESS
```

`COMPLETE`, `INCOMPLETE` and `NOT_ESTABLISHED` describe enumeration only.
Unresolved-candidate evidence is recorded by exact semantic component. One
root may be locally admissible when unresolved candidates belong to another
component. An unresolved candidate on that root's resolved valid component is
local uncertainty and remains a veto. The root also requires current
revision/source coherence, established local isolation, established transverse
contact, unambiguous component and selector, no overlap on that component and
a valid ledger binding. Global `COMPLETE` never makes an ambiguous root
admissible.

Pair intersections retain their existing pair-isolation authority and are not
promoted by the one-parameter public selector.

Keep the following axes separate:

```text
root existence
deterministic identity
local numerical/topological evidence
global completeness
materialization eligibility under an explicit policy
```

Current R4 already permits some `VERIFIED_UNCERTIFIED` / `ESTIMATED_ERROR`
roots when local isolation and deterministic identity are established. The
four-root failure was therefore not a `CERTIFIED_ONLY` policy rejection. The
phase/rank correction establishes identity under the existing strict local
evidence contract; R4 adds no `DETERMINISTIC_LOCAL` tier or certification
relaxation.

# Geometric invariants and degeneracies

- The exact midpoint/circle fixture exposes two finite, current, transverse,
  locally isolated initial roots with truthful global completeness.
- Both roots remain defined and do not swap through broad regular motion.
- Byte-identical starting constructions reaching one final regular geometry by
  direct, incremental, forward/reverse and reopen paths have identical token
  bindings, semantic solution bindings and evaluated points.
- The R3 singleton line control remains admissible, including an existing
  materialized v1 token point after exact migration.
- Equivalent parameters at a periodic half-open seam represent one root.
  Complete-cycle seam canonicalization must not duplicate roots. A unique base
  selector retains its approved seam behavior; a seam-reaching ranked group
  publishes identity discontinuity and invalidates rather than rotates tokens.
- Tangency, multiplicity uncertainty, merge/split, overlap, non-isolation,
  same-component unresolved evidence, stale revisions and component ambiguity
  remain fail-closed.
- Disappearance invalidates rather than jumps. No root is resurrected by
  coordinate proximity.
- Repeated base selectors use only the fully framed intrinsic phase/rank when
  their current isolating intervals prove the order; repeated complete
  selectors remain fail-closed.
- The exact four-solution fixture retains all four finite roots as truthful
  geometry and gives all four unique materializable selectors without a
  certification or completeness upgrade.
- Collision cardinality, topology, declared orientation and periodic monodromy
  invalidate ranked identities rather than shifting opaque tokens.
- Target families use the same general selector contract; no circle-specific
  exception is permitted.
- Unresolved evidence on the same semantic component blocks materialization;
  unresolved evidence on another component changes global/work evidence but
  does not veto the established root.
- Multiplying an equivalent line/segment, central-conic/parabola or regular
  polynomial-implicit representation by a legitimate nonzero scalar does not
  flip its deterministic germ selector or exact token.
- Reversing or otherwise changing a ray's semantic direction is not a harmless
  scalar representation change and remains observable semantic input.
- Locus V2 × Locus V2 remains under ADR 0009's symmetric pair-domain and
  pair-isolation authority; the one-sided phase/rank selector is not applied.

# Compatibility and serialization

Keep `.cedg` native and `.ggb` compatibility-input policy, ZIP/XML structure,
`app="classic"`, exact-token opacity, normal DAG dependencies, copy/remapping,
undo/redo and transactional native-open behavior unchanged.

R4 ledger export uses strict canonical format v3. It adds a separate durable
current-root binding containing the exact continuation contract and canonical
selector. The opaque token remains unchanged after ordinary motion because its
identity material is not replaced by a parameter or coordinate.

Canonical format v1 and v2 import remains supported for authentic pre-phase
state. Format v3 is the only ledger format in which a phase/rank selector is
legal; a v3 phase state falsely relabeled as v2 must be rejected. Only an exact
historical public singleton
allocation with canonical legacy token material, the exact initial address and
exactly one finite current root on that component may acquire a current selector
binding. Migration preserves the token string byte-for-byte. Noncanonical
frames/incarnations, forged legacy material, duplicate bindings, incomplete
bindings and context mismatch fail closed. The historical exact-key `mint`
path retains its contract.

Legacy singleton token material must never bind an intrinsic-phase
selector. Runtime migration to phase/rank allocates fresh opaque material rather
than reusing the singleton. Import rejects a manipulated ledger-v3 state that
forges such a binding, independently of the phase-v2 relabel rejection.

The external `locus-root/v3` token envelope is unchanged. An older exact token
is retained only through one compatible semantic binding; otherwise import
fails closed.

The R3 inspector is only a consumer validation path. R4 adds no candidate
markers and creates no automatic persistent points.

As planning-only successor authority, keep the GeoCeDG Construction product
policy `Continuity = OFF`: the existing host kernel option is the single
authority, ordinary GeoCeDG settings cannot enable it, preferences and loaded
`.cedg`/compatibility `.ggb` files cannot override it, and GeoCeDG Classic
retains upstream configurability. Future candidate markers reflect only the
current deterministic rich-result tokens and add no tracking heuristic. Do not
implement any of that G9U1 frontend/profile work in R4.

Prospective G9U1 may expose markers and an explicitly opted-in frontend
create-all transaction. A weaker evidence/materialization policy is not part of
this authority and would require a separate future author decision. Kernel
`Intersect(L,T)` must still create only
the rich result. Auto mode may execute one separate visible, deterministic and
undoable transaction immediately after an explicit intersection action; it
must never create new DAG nodes on recompute, topology change, load/reopen,
workspace switch or preference restoration. R4 itself retains the existing
strict local-evidence threshold; it does not implement a certification-policy
relaxation.

# Required tests and commands

Implement the current source-declared 50-method inventory in
`geocedg/validation/g9u0-r4/g9u0-r4-intersection-admissibility-scenarios.json`:

- 25 public-kernel tests, including both byte-exact author fixtures, four-root
  materialization, every solver permutation, direct/incremental/reverse/reopen
  path independence, broad regular motion, component-scoped local/global
  mixed-unresolved cases, intrinsic phase/rank, cardinality/orientation
  barriers, periodic monodromy, topology negatives, target families and
  equivalent target-representation scaling;
- 23 ledger tests, including allocation/order independence, phase-selector
  framing, moved-address restoration, v1 and authentic pre-phase v2 import into
  v3, rejection of a phase-bearing state relabeled as v2, cardinality and
  orientation invalidation, strict parser/tamper/duplicate rejection, exact
  copy provenance and history-independent resolution; and
- two Desktop native `.cedg` save/reopen tests.

The pre-final replacement authority executed this exact inventory twice at
canonical SHA-256
`c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`;
retain it as pre-final evidence. After the legacy-singleton and merge-parent
assertions, final A and B each passed 50/50 with zero failures, errors or skips
and exact canonical-summary SHA-256
`f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`.

Run the focused R4 verifier twice with exact canonical-summary comparison. Then
run relevant G8 authority, G9U0, G9U0-R1, G9U0-R2, G9U0-R3, G9X1, G5, relevant
G9A, full legacy/scientific Locus, the R3 inspector, Checkstyle, both Git diff
checks and full `tools/agent/verify.ps1`. Existing gates must not be weakened.
Generated logs belong only under ignored `artifacts/`.

# Required artifacts

Maintain one canonical prompt, architecture note, scenario/evidence inventory,
candidate report and focused verifier. Insert the focused verifier after R3 in
the composed verifier only when the implementation and focused authority are
real. Record exact modified upstream files and living roadmap/traceability
without rewriting historical G8/G9U0/R1/R2/R3 reports.

The replacement composed run exposed one missing public localization value for
`DETERMINISTIC_SELECTION_ESTABLISHED`. The bounded correction adds that key to
the base, English and Spanish menu bundles; it changes no kernel semantics.
The four-solution byte-exact fixture remains the author reproduction authority.
The final corrected candidate inventory is 51 paths, 29 under `source/`, with
no generated logs or artifacts tracked. The replacement full composed authority
exited 0 with `All GeoCeDG verification gates passed.` This automated result
does not claim author approval.

Do not create a dummy PASS gate. Generated canonical summaries are candidate
evidence, not author approval.

# Stop conditions

Stop if admissibility would require identity by coordinate, extrinsic
solver/list/output/UI order, proximity, weaker isolation, invented global
completeness, pair-root relaxation, frontend identity logic or generic homotopy
authority. Stop if phase/rank cannot be established inside the full ADR-0017
component/germ/orientation/domain-kind/cardinality frame with pairwise disjoint
isolating intervals. Stop if a v1/v2 document cannot import under the exact
compatibility contract or if deterministic
current selection cannot be made path-independent. Also stop if target
representation invariance would require erasing a ray's semantic direction or
if same-component unresolved evidence would have to be ignored.

The exact four-root fixture is now a positive acceptance gate: all four roots
must retain established local evidence and global `NOT_ESTABLISHED`, receive
four unique complete phase selectors, materialize through exact tokens, ignore
every solver permutation and remain path-independent through seam-free regular
motion. Do not implement adaptive certification, `DETERMINISTIC_LOCAL` or
manual force as a workaround; the correction is identity-only.

Prepare but do not self-pass this author re-smoke:

1. open the exact midpoint fixture and create `Intersect(a,c)`;
2. materialize both exact-token points;
3. move through a broad regular interval and confirm both remain defined and
   do not swap;
4. revisit one final geometry by a different movement path;
5. traverse the periodic seam;
6. approach a genuine topology event and confirm conservative invalidation;
7. save/reopen `.cedg` and confirm deterministic recomputation.

Also inspect the four-solution fixture, confirm its four complete selectors and
unchanged evidence/completeness classes, and materialize all four roots under
the existing strict local-evidence policy. Move through regular seam-free
geometry, reach the same final state by another path, exercise a
cardinality/orientation/topology barrier and the periodic monodromy boundary,
then save/reopen. No manual token copying is allowed.

Terminal state:

```text
G9U0-R4 = IMPLEMENTATION CANDIDATE — PENDING AUTHOR RE-REVIEW
historicalAuthorSmoke = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
deterministicPolicy = AUTHOR_APPROVED_DIRECTION
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false

G9U0-R5 = DESIGN CANDIDATE — PENDING AUTHOR REVIEW
implementationStarted = false
implementationAuthorized = false

G9U1 = DESIGNED — NOT AUTHORIZED
DETERMINISTIC_CONTINUITY_OFF_REQUIRED
MATERIALIZATION_POLICY_UNCHANGED_STRICT_LOCAL_EVIDENCE
AUTO_MATERIALIZATION_FRONTEND_ONLY
blockedUntilR4Pass = true
blockedUntilR5Pass = true
G9B = NOT AUTHORIZED
G9C = NOT AUTHORIZED
G9U2 = BLOCKED
PRODUCTIVE G10 = NOT AUTHORIZED
```

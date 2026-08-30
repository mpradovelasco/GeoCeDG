# G9U0-R4 deterministic public intersection identity

- Phase: **G9U0-R4**
- State: **IMPLEMENTATION CANDIDATE — PENDING AUTHOR RE-REVIEW**
- Layer: shared-kernel intersection identity and token ledger
- Entry: `ce7f15c70d50b0639c264fc1cd3356a0d4eb5e2b`
- Normative candidate:
  [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md)
  §§6.2, 8.2 and 10
- Accepted decision:
  [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md)

```text
implementationStarted = true
selfApproved = false
authorApproved = false
passClaimed = false
historicalAuthorSmoke = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
deterministicPolicy = AUTHOR_APPROVED_DIRECTION
```

## Two characterized defects

The byte-exact author fixture
`source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r2/locusFromMidpoint.cedg`
contains periodic midpoint locus `a` and circle `c`. `Intersect(a,c)` produces
two finite, current, transverse and locally isolated roots while global
completeness truthfully remains `NOT_ESTABLISHED`.

On the R3 baseline, the G8 public shortcut supplied durable candidate lineage
only when one component had exactly one isolated root. The two author roots
share a component, so neither received an admissible token. The first R4
candidate removed that first-publication deadlock and both points could be
materialized.

The author smoke then exposed the deeper defect: after small ordinary movement,
one point became undefined. The stateful continuation path required an exact
persisted address/semantic-parameter equality before it would retain the token.
A regular moving transverse root normally changes parameter, so the
previous-snapshot relation incorrectly became the only route to current
identity. The failed smoke remains recorded; it is not rewritten as a pass.

## Authoritative interpretation

GeoCeDG is deterministic:

```text
deterministic semantic selection
    >
continuity heuristic
```

The previous root is not needed to select the current root. Continuity should
emerge when one current intrinsic selector remains uniquely valid throughout
regular motion. Previous/current evidence remains useful for topology and
continuity diagnostics, but it cannot decide the durable binding.

Local point admissibility remains independent of global enumeration
completeness. Unresolved candidates are retained with their exact semantic
component. A locally proven root can be admissible under `INCOMPLETE` or
`NOT_ESTABLISHED` when unresolved candidates are on another component. An
unresolved candidate on the root's own resolved valid component is local
uncertainty and remains a veto, as do non-isolation and selector ambiguity.

## Current-snapshot selector

`PublicIntersectionRootIdentityResolver2D` executes after numerical
isolation/refinement and before the existing atomic ledger commit. For each
eligible non-pair root it resolves the exact semantic component and consumes a
typed oriented current-root germ produced by the public target capability.

`IntersectionRootDeterministicSelector2D` first canonically frames the base
selector:

```text
stable component lineage
+ current transverse contact-indicator ID and orientation
```

When that base selector is unique, it remains unchanged. Only a repeated base
selector receives the ADR-0017 intrinsic phase extension:

```text
stable branch/component lineage
+ typed transverse germ/collision class
+ declared semantic orientation
+ periodic/nonperiodic domain kind
+ verified collision-group cardinality
+ intrinsic oriented phase/rank
```

The rank is induced by the explicit oriented semantic component after
canonical parameters and pairwise disjoint isolating intervals prove the
ordering. The parameter/interval values are revision evidence and are not
encoded as identity. The complete selector contains no coordinate, solver
enumeration/list slot, screen state or previous-root position. Its ordinal is
never meaningful outside the full semantic frame. The enclosing ledger
material and binding separately bind result owner, source pair, constructive
lineage, topology context, provider/parameter contract and target contract.

### Canonical target-contact orientation

The germ orientation removes algebraic representation freedom without erasing
geometry:

- an unoriented line or segment fixes its normal sign from the largest-magnitude
  nonzero normal coefficient, with the x coefficient winning an exact tie;
- a central conic fixes sign from its nonzero center level;
- a parabola fixes sign from its nonzero quadratic trace; and
- a regular polynomial implicit curve fixes sign from its leading nonzero
  monomial, ordered by total degree and then x degree.

These choices are invariant under a legitimate nonzero scalar change of the
same represented target, including sign reversal and exact polynomial zero
padding. They do not make algebraic sign itself an identity. A ray intentionally
does not use the unoriented-line convention: its direction is semantic and
remains in the oriented contact contract.

Selectors are grouped in the current snapshot. Repeated base-selector groups
are phase-ranked only when component, germ, orientation, domain kind and
collision cardinality are coherent and their isolating intervals are pairwise
disjoint. Only a complete selector occurring once may allocate or resume a
durable token. Complete selectors are processed in canonical selector order,
so every solver enumeration permutation produces the same association. A
duplicate complete selector still fails closed with revision-local handles.

On first allocation the result reports:

```text
IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
LineageEventKind.APPEARED
continuationEstablished = false
```

On later current-snapshot resolution of the same exact ledger binding it
reports `DETERMINISTIC_SELECTION_ESTABLISHED`. The semantic parameter and
address proof may change; the exact opaque token does not.

## Continuity and topology diagnostics

`PublicIntersectionRootTransition2D` may relate prior and current transverse
roots through component, germ and bounded semantic motion. The current-snapshot
selector remains the root identity authority. If the relation uniquely links
the same token, lineage may record that deterministic selection was also
continuous. No transition edge creates, chooses or transfers a token.

For a complete ranked periodic group, the relation also guards reuse of prior
tokens: every prior root must map bijectively to one current root with the same
complete selector. Missing, ambiguous, budget-exhausted, incomplete or
non-bijective evidence invalidates the whole prior collision group. This is a
conservative reuse barrier, not a previous-state selector. On an ordinary
nonperiodic group, a missing bounded relation is not identity authority and does
not override the unique current selector.

The direct consequence is path independence inside one regular topology
stratum. Byte-identical constructions with identical durable IDs and a
materialized point must reach the same final binding after:

- one direct update;
- many small forward updates;
- forward, partial reverse and final update; or
- save/reopen before the final update.

Observed tangency, merge, split, disappearance, overlap, branch ambiguity or
same-component unresolved isolation remains conservative. The ledger may burn
an allocation rather than guess. An unresolved candidate on another component
is still global/work evidence but cannot invalidate this independently proven
root. No coordinate-proximity resurrection exists.

Merge/split diagnostic lineage first sorts the complete parent records by
opaque root token, then emits parent tokens and their optional continuation keys
from that same ordered sequence. It never sorts those two lists independently.
The encoding is deterministic and preserves token/key association, but remains
ambiguous evidence and cannot select a child continuation.

For a complete periodic fundamental cycle, component lineage and canonical
seam handling make equivalent half-open endpoint parameters one semantic
context. The declared oriented fundamental interval is the deterministic phase
frame; no root-dependent moving anchor exists. Within a seam-free chart rank is
stable. If a repeated-selector interval reaches the seam, the affected group
publishes `IDENTITY_DISCONTINUITY` and old ranked tokens are invalidated rather
than cyclically rotated. This explicit monodromy boundary uses neither
Cartesian proximity nor a mutable winding history.

A byte-identical direct update across the seam may provide no complete
prior/current bijection even though the destination snapshot has four unique
selectors. R4 invalidates all old ranked tokens, then permits fresh exact tokens
on a subsequent stable publication. It never transfers an old token through an
unproved cyclic permutation.

## Second author smoke and ADR-0017 correction

The second author smoke used ignored source artifact
`artifacts/smoke-test-g9u0-r2/fouSolutions.cedg` (historical filename typo).
The byte-exact durable characterization copy is
`source/shared/common-jre/src/test/resources/org/geocedg/common/locus/g9u0-r4/fourSolutions.cedg`:
14,601 bytes, SHA-256
`51dcf7a002cb3984bb4cf5843d50e100f4bc8ef91217d4502fa7987c5b1ec21c`.
It contains `a=LocusV2(E,C)`, `c=Circle(A,B)` and `b=Intersect(a,c)` with the
host Continuity field false.

The current rich result publishes four roots on
`generator.main/component-0`:

| semantic parameter | component | germ | orientation | domain kind | collision cardinality | phase rank | local evidence | corrected identity outcome |
| --- | --- | --- | --- | --- | ---: | ---: | --- | --- |
| `-2.093511674974421` | `generator.main/component-0` | positive | `INCREASING` | `PERIODIC_FUNDAMENTAL_INTERVAL` | 2 | 0 | isolated, transverse, `ESTIMATED_ERROR` | unique complete phase selector; materializable |
| `-1.403606948912654` | `generator.main/component-0` | negative | `INCREASING` | `PERIODIC_FUNDAMENTAL_INTERVAL` | 2 | 0 | isolated, transverse, `ESTIMATED_ERROR` | unique complete phase selector; materializable |
| `1.4036069489126533` | `generator.main/component-0` | positive | `INCREASING` | `PERIODIC_FUNDAMENTAL_INTERVAL` | 2 | 1 | isolated, transverse, `ESTIMATED_ERROR` | unique complete phase selector; materializable |
| `2.0935116749744207` | `generator.main/component-0` | negative | `INCREASING` | `PERIODIC_FUNDAMENTAL_INTERVAL` | 2 | 1 | isolated, transverse, `ESTIMATED_ERROR` | unique complete phase selector; materializable |

All four are `SUCCESS`, `FINITE`, `CURRENT`; global completeness remains
`NOT_ESTABLISHED`, no candidates are unresolved and there is no overlap. The
positive pair and negative pair each repeat one base selector. Their local
isolating intervals are disjoint in the same oriented component, so ADR 0017
authorizes an intrinsic phase rank within each exact collision group. The four
complete selectors are unique, receive four distinct exact opaque tokens and
are point-materializable.

This is not a numerical-certification failure. Current R4 already allows a
`VERIFIED_UNCERTIFIED` / `ESTIMATED_ERROR` root to feed a point when its local
isolation and deterministic identity are established. The exact decomposition
is:

1. root existence: all four finite solutions are published;
2. deterministic identity: four unique complete selectors are established;
3. local numerical/topological evidence: established for all four;
4. global completeness: independently `NOT_ESTABLISHED`;
5. materialization: allowed through each exact complete selector/token.

The correction makes no certification claim. `ESTIMATED_ERROR` remains
`ESTIMATED_ERROR`, and global completeness remains `NOT_ESTABLISHED`. Adaptive
certification, a `DETERMINISTIC_LOCAL` tier and manual force were not needed or
implemented. A materialization policy still may not override a duplicate
complete selector or ambiguous lineage.

The upstream conic-intersection mechanism is not a reusable semantic solution.
With Continuity on it matches roots using Cartesian distance and history. With
Continuity off it retains an output-slot permutation initialized through that
machinery. Those are ordinary presentation/output slots, not exact semantic
selectors, and conflict with R4's coordinate/history/list-index prohibitions.

A generic productive correction now follows Accepted ADR 0017: intrinsic
oriented phase/rank is permitted only for a repeated base selector and only
inside its full component/germ/orientation/domain-kind/cardinality frame. This
does not make the upstream conic output-slot lifecycle semantic authority.

## Certification/materialization policy boundary

Materialization evidence remains a revision-scoped axis separate from
`IdentityStatus`, token ledger and global completeness. R4 retains the existing
strict local-evidence API and adds no weaker certification policy. The ledger
remains identity-only: an exact token means exact semantic identity, not exact
arithmetic.

Any future evidence-policy provenance would belong on
`AlgoLocusIntersectionPointV2` or a small provider attached to its
rich-result/token parentage, not as a global quality field on every `GeoPoint`.
That future work is not part of R4.

## Ledger v3 and exact v1/v2 import

`LocusIntersectionTokenLedger2D` now exports strict canonical format v3. Each
active R4 entry stores the existing opaque token material plus a separate
current-root binding:

```text
continuation contract
+ canonical deterministic selector
```

The token itself still carries no coordinate or parameter. Recompute replaces
only revision address evidence after resolving the same binding.

Format v3 prevents an intrinsic phase/rank binding from being interpreted as
the earlier schema. The importer accepts canonical format v1 and authentic
pre-phase format v2 for compatibility. Intrinsic phase selectors are legal only
in ledger v3; a v3 phase snapshot falsely relabeled as v2 is rejected. An R3
public singleton allocation can acquire a current binding
only when:

- its legacy continuation and solution lineage are byte-exact and canonical;
- its initial provider/target/parameter address is exact;
- it is the only finite current root on the exact component; and
- the current selector is unique.

Import/migration preserves the complete token string byte-for-byte only under
one compatible semantic binding; otherwise it fails closed. A real XML
regression loads a pre-R4/v1 ledger with an already materialized token point,
proves the point defined after the first R4 recompute, verifies v3 state, then
moves the target and proves resolution through the migrated selector.

The parser rejects noncanonical incarnation suffixes, forged legacy material,
incomplete or duplicate deterministic bindings, selector/component mismatch and
phase-bearing state presented under ledger v2. The focused ledger regression
also imports a fixed authentic pre-phase v2 state, emits canonical v3 and proves
that the exact opaque token remains unchanged.

Legacy singleton material has an additional fail-closed boundary: it can
never acquire an intrinsic-phase selector. Runtime migration to a phase selector
returns a fresh allocation instead of reusing the old token. Import likewise
rejects a manipulated ledger-v3 snapshot that binds legacy singleton material
to intrinsic phase. This is distinct from, and additive to, rejection of a
phase-bearing v3 state falsely relabeled as v2.
Authorized copy remains an exact provenance operation; it is not a geometric
matcher.

## Preserved boundaries

- The solver remains geometry and local-proof authority.
- Global completeness never supplies root identity.
- Unresolved-candidate evidence is component-scoped; same-component evidence
  blocks, unrelated-component evidence does not.
- Equivalent nonzero scalar forms of line/segment, central conic/parabola and
  regular polynomial implicit targets preserve the current germ; ray direction
  remains semantic.
- Pair roots retain ADR 0009's separate symmetric pair-isolation contract; the
  one-sided phase/rank selector is not applied to Locus V2 × Locus V2.
- Exact tokens, ZIP/XML structure and `app="classic"` remain unchanged.
- The rich result remains semantic authority and non-Euclidian.
- R3 inspector behavior is only a consumer validation path.
- No markers, frontend selection heuristics, rendering changes, generic `Path`
  or G9U1 implementation belong to R4.

## Explicit retained limitations

The phase/rank extension is not a universal enumerator. It is available only
for repeated public single-Locus base selectors with coherent current context
and pairwise disjoint isolating intervals. A duplicate complete selector,
cardinality/topology/orientation change, non-isolation or seam-reaching ranked
interval fails closed. Periodic monodromy is reported as identity
discontinuity, not hidden by history. A wide/direct periodic update lacking a
complete bijective same-selector relation may conservatively invalidate the
whole old ranked group; it does not claim token continuity across the seam.
Pair roots remain outside this selector. No certification-policy relaxation is
part of the candidate.

## Validation design

The focused source authority now covers both exact author fixtures, four-root
phase/rank uniqueness and materialization, every solver enumeration
permutation, direct and multi-step path independence, broad regular motion,
periodic canonicalization and monodromy discontinuity, collision-cardinality
and orientation barriers, v1/v2 import into ledger v3, strict persisted-state
rejection, component-scoped local/global evidence, target-representation
invariance, copy provenance and real `.cedg` save/reopen.

Earlier 27-, 38- and 39-method runs remain historical pre-ADR-0017 evidence.
They do not validate the current phase/rank implementation. The pre-final
replacement A/B evidence executed 50/50 methods at canonical SHA-256
`c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`.
After the final legacy-singleton and merge-parent assertions, focused A and B
again executed 50/50 methods (25 public-kernel, 23 ledger and 2 Desktop), with
zero failures, errors or skips and identical final canonical SHA-256
`f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`.
The final candidate inventory is 51 paths, 29 under `source/`. The full composed
authority exited 0 and terminated with `All GeoCeDG verification gates passed.`
Historical gates remain sealed, and no automated result or generated summary
constitutes author approval. R4 remains pending author re-review; R5 and G9U1
remain unexecuted.

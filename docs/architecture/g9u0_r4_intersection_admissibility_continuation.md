# G9U0-R4 deterministic public intersection identity

- Phase: **G9U0-R4**
- State: **PASS — AUTHOR APPROVED**
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
authorApproved = true
passClaimed = true
historicalAuthorSmoke = FAILED_POINT_INVALIDATED_DURING_REGULAR_MOTION
historicalAuthorSmoke2 = IMPROVED_BUT_FOUR_SOLUTIONS_NOT_MATERIALIZABLE
historicalAuthorSmoke3 = TWO_ROOT_PASS_FOUR_ROOT_REGULAR_MOTION_FAILURE
manualAuthorFinalSmokeFourRoot = PASS
manualAuthorFinalSmokeReactivation = PASS
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

After the phase/rank correction, the then-current author smoke Case A passed. Case B
initially exposed four unique selectors and exact tokens and materialized all
four points, but ordinary interactive movement again made the points undefined
while four finite, transverse and isolated roots remained. That third historical
failure is distinct from both earlier failures. The adaptive phase-cell/tube and
dormant-reactivation correction subsequently passed both final author re-smokes.

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
The durable selector binding is independent of the revision-scoped topology
certificate used to decide whether that allocation is current. Losing the
current certificate cannot change the selector or transfer its token.

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
tokens through an adaptive intrinsic periodic phase-tube/cell certificate.
Prior and current canonical phase points and isolating intervals define
same-rank swept tubes bounded by the nearest cyclic root gaps. The tubes must be
disjoint and produce a complete same-selector bijection. A fixed
`component span / 256` threshold was not topology evidence: it coupled identity
to the isolation work partition and made one ordinary UI-sized update fail when
many smaller updates to the same final geometry passed. That threshold is not
used by the periodic ranked-token guard. Missing, intersecting, incomplete or
non-bijective phase tubes fail closed into durable periodic quarantine. A later
unique cyclic offset zero releases/reuses that exact group; a proved unique
nonzero offset permanently retires only its affected group; no offset or
multiple offsets leave the group quarantined. Unaffected groups retain their
exact bindings. This certificate is a conservative revision-scoped topology
barrier, not a previous-state selector or durable identity field. On an ordinary nonperiodic
group, a missing bounded diagnostic relation is not identity authority and does
not override the unique current selector.

The direct consequence is path independence inside one regular topology
stratum. Byte-identical constructions with identical durable IDs and a
materialized point must reach the same final binding after:

- one direct update;
- many small forward updates;
- forward, partial reverse and final update; or
- save/reopen before the final update.

Observed tangency, merge, split, disappearance, overlap, branch ambiguity or
same-component unresolved isolation remains conservative. Current admissibility
may fail rather than guess; a claimed allocation may remain dormant unless an
intrinsically non-reactivatable transition is proved. An unresolved candidate on
another component is still global/work evidence but cannot invalidate this
independently proven root. No coordinate-proximity resurrection exists.

Merge/split diagnostic lineage first sorts the complete parent records by
opaque root token, then emits parent tokens and their optional continuation keys
from that same ordered sequence. It never sorts those two lists independently.
The encoding is deterministic and preserves token/key association, but remains
ambiguous evidence and cannot select a child continuation.

For a complete periodic fundamental cycle, component lineage and canonical
seam handling make equivalent half-open endpoint parameters one semantic
context. The declared oriented fundamental interval is the deterministic phase
frame; no root-dependent moving anchor exists. Within a seam-free chart rank is
stable. If a repeated-selector interval reaches the seam, current activation
requires intrinsic cyclic evidence. Insufficient evidence durably quarantines
the old group; unique offset zero releases it; unique proved nonzero offset
publishes `IDENTITY_DISCONTINUITY` and permanently retires it rather than
cyclically rotating its tokens. This explicit monodromy boundary uses neither
Cartesian proximity nor a mutable winding history.

A byte-identical direct update across the seam may prove a nonzero cyclic shift for one
ranked germ group even though the destination snapshot still has four unique
selectors. R4 invalidates that affected group while leaving an independently
certified group current. It never transfers an old token through an unproved or
proved-nonzero cyclic permutation.

## Four-root author smoke and ADR-0017 correction

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

Those statements describe Case B's initial state, which passed in the current
author smoke. They do not describe its motion outcome. A direct ordinary center
drag moved one root in each two-root germ group by approximately `0.032409` in a
`2*pi` phase span. The former fixed `2*pi / 256` bound was approximately
`0.024544`, so each group lost one transition edge and all four materialized
points became undefined. The same final geometry reached through smaller steps
retained them. The adaptive intrinsic periodic phase-tube/cell certificate uses
the actual cyclic root gaps and isolating intervals instead of that unrelated
work-partition threshold.

The protected-checkpoint failure is reproduced exactly by `A.x: 3.98 -> 4.18`
with `A.y = 10` and `B = (5.72, 2.22)`. In both snapshots every root remains on
`generator.main/component-0`, orientation `INCREASING`, domain
`PERIODIC_FUNDAMENTAL_INTERVAL`, topology context
`g9u0-public-topology/v1`, with cardinality two per germ, current, transverse
and locally isolated:

| slot | `u` before -> after | germ/rank and complete durable selector | publication/point before -> protected-checkpoint failure |
| --- | --- | --- | --- |
| negative outer | `-2.093511674974421 -> ~-2.12592064944333` | positive/0; unchanged `(component,germ,INCREASING,PERIODIC,2,0)` | claimed token, deterministic/unchanged/explicit-key/admissible, point defined -> revision-local, identity-discontinuity/ambiguous/no-key/inadmissible, old token unresolved and point undefined |
| negative inner | `-1.403606948912654 -> ~-1.39419682506843` | negative/0; unchanged `(component,germ,INCREASING,PERIODIC,2,0)` | same active-to-discontinuity and defined-to-undefined transition |
| positive inner | `1.4036069489126533 -> ~1.39419682506843` | positive/1; unchanged `(component,germ,INCREASING,PERIODIC,2,1)` | same active-to-discontinuity and defined-to-undefined transition |
| positive outer | `2.0935116749744207 -> ~2.12592064944333` | negative/1; unchanged `(component,germ,INCREASING,PERIODIC,2,1)` | same active-to-discontinuity and defined-to-undefined transition |

No seam, overlap, unresolved candidate, branch/component/cardinality or local
admissibility change occurred. One outer displacement exceeded the obsolete
`2*pi/256` group bound and invalidated both ranks of each germ group. The first
changed predicate was the prior-token periodic reuse guard, not root geometry
or the current deterministic selector. The candidate report retains the full
field-by-field evidence.

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

An exact token claimed by an existing materialized point may remain retained as
dormant when its selector is temporarily not current or point-admissible. The
same `GeoPoint` becomes undefined. It may reactivate, without replacement or
retargeting, only when the same complete selector again resolves uniquely and
passes the current topology/local-evidence predicate. Recompute, reactivation
and later root appearance never create a new point automatically.

Any future evidence-policy provenance would belong on
`AlgoLocusIntersectionPointV2` or a small provider attached to its
rich-result/token parentage, not as a global quality field on every `GeoPoint`.
That future work is not part of R4.

## Ledger v4 and exact v1/v2/v3 import

`LocusIntersectionTokenLedger2D` now exports strict canonical format v4. Each
R4 entry stores the existing opaque token material, current-root binding and
active/dormant/quarantined materialized-claim lifecycle:

```text
continuation contract
+ canonical deterministic selector
```

The token itself still carries no coordinate or parameter. Recompute replaces
only revision address evidence after resolving the same binding.

Format v3 first prevented an intrinsic phase/rank binding from being interpreted
as the earlier schema. Format v4 adds claimed-active, claimed-dormant,
`PERIODIC_QUARANTINE` (`q`) and `CLAIMED_PERIODIC_QUARANTINE` (`r`) state
without changing the external token envelope. The importer accepts canonical
v3 phase state plus canonical v1 and authentic pre-phase v2 state. Claimed
lifecycle and quarantine state is legal only in v4; a v3 phase snapshot falsely
relabeled as v2 and a v4 claimed/quarantined state relabeled as an older version
are rejected. An R3
public singleton allocation can acquire a current binding
only when:

- its legacy continuation and solution lineage are byte-exact and canonical;
- its initial provider/target/parameter address is exact;
- it is the only finite current root on the exact component; and
- the current selector is unique.

Import/migration preserves the complete token string byte-for-byte only under
one compatible semantic binding; otherwise it fails closed. A real XML
regression loads a pre-R4/v1 ledger with an already materialized token point,
proves the point defined after the first R4 recompute, verifies current state,
then moves the target and proves resolution through the migrated selector.

The parser rejects noncanonical incarnation suffixes, forged legacy material,
incomplete or duplicate deterministic bindings, selector/component mismatch and
phase-bearing state presented under ledger v2. The focused ledger regression
also imports a fixed authentic pre-phase v2 state, emits canonical v4 and proves
that the exact opaque token remains unchanged.

Legacy singleton material has an additional fail-closed boundary: it can
never acquire an intrinsic-phase selector. Runtime migration to a phase selector
returns a fresh allocation instead of reusing the old token. Import likewise
rejects a manipulated current snapshot that binds legacy singleton material to
intrinsic phase. This is distinct from, and additive to, the versioned phase
and claimed-lifecycle rejection rules.
Authorized copy remains an exact provenance operation; it is not a geometric
matcher. It preserves `q`/`r` and cannot certify offset-zero release. A complete
quarantined group is retained only while a real materialized claim needs it;
releasing the final claim prunes that group.

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
- Future G9U1 markers and explicit/opted-in auto-materialization may consume
  only current admissible exact tokens and remain frontend-only. Dormant tokens
  are not marker/action candidates and reactivation never auto-creates a point.
- Kernel recomputation may reactivate an already-existing exact-token point;
  only the future G9U1 frontend may explicitly and undoably materialize a root
  that has never had a `GeoPoint`.

## Explicit retained limitations

The phase/rank extension is not a universal enumerator. It is available only
for repeated public single-Locus base selectors with coherent current context
and pairwise disjoint isolating intervals. A duplicate complete selector,
cardinality/topology/orientation change, non-isolation or seam-reaching ranked
interval fails closed. Periodic monodromy is reported as identity
discontinuity, not hidden by history. A wide/direct periodic update lacking a
complete adaptive phase-tube certificate durably quarantines the affected old
ranked group; unique offset zero releases it, proved unique nonzero offset
permanently retires it, and absent/nonunique evidence leaves it quarantined.
No outcome rotates tokens. Claimed exact allocations may otherwise be
dormant, but they reactivate only through the same unique selector.
Pair roots remain outside this selector. No certification-policy relaxation is
part of the candidate.

For `R` current roots and `P` existing materialized bindings, the incremental
selector/reactivation pass is `O(R log R + P)`: current roots are canonically
ordered once and point claims use direct selector lookup. It performs no
additional global solve per child and stores no movement trajectory. Existing
whole-transition diagnostics may remain `O(R^2)` in the worst case; those
diagnostics are non-authoritative and do not change this incremental bound.

## Validation design

The sealed focused source authority covered both exact author fixtures,
four-root phase/rank uniqueness and initial materialization, solver enumeration
permutations, its then-modeled motion paths, periodic canonicalization and
monodromy discontinuity, collision-cardinality and orientation barriers,
canonical v1/v2/v3 import into ledger v4, strict persisted-state rejection,
component-scoped local/global evidence, target-representation invariance, copy
provenance and real `.cedg` save/reopen. Current author Case B proved that its
motion granularity did not cover the ordinary direct UI update.

Earlier 27-, 38- and 39-method runs remain historical pre-ADR-0017 evidence.
They do not validate the current phase/rank implementation. The pre-final
replacement A/B evidence executed 50/50 methods at canonical SHA-256
`c1d76e86d5174e406ac7bdddd4862f4ccc607d6a68df2ec23c365b9084cce83e`.
After the final legacy-singleton and merge-parent assertions, focused A and B
again executed 50/50 methods (25 public-kernel, 23 ledger and 2 Desktop), with
zero failures, errors or skips and identical final canonical SHA-256
`f909aaa28aedc63aa35d01325aa3f84d893ab8a92da64c04e9eb7a661898681c`.
Protective checkpoint `4ef2c9df433aec7c6385a488a02581358da83f60`
records that pre-current-correction state. Its final candidate inventory was 51
paths, 29 under
`source/`. Its full composed authority exited 0 and terminated with
`All GeoCeDG verification gates passed.` Those counts and hashes are historical
evidence. The adaptive phase-tube and dormant-allocation corrections now have
replacement focused A/B and composed automated PASS. No automated result or
generated summary constituted author approval; the two separate final author
re-smokes now close R4 as `PASS — AUTHOR APPROVED`. R5 and G9U1 remain
unexecuted.

The current executed authority declares 27 public-kernel, 28 ledger and 3
Desktop methods (58 total). Focused A and B each pass 58/58, with zero failures,
errors or skips and an exact normalized-summary match at SHA-256
`3e9ea0aa20d511f2828eae61e491c1b3b5d9cb86a0f02166503ee5093d6000fb`.
The full composed verifier exits 0 with
`All GeoCeDG verification gates passed.`. Ledger `q`/`r` coverage proves recompute,
canonical export/import and exact-provenance copy. The third Desktop method
separately proves native `.cedg` dormant `2 -> 4 -> 2` same-point reactivation
and reopen after reactivation; it is not a periodic-quarantine native round-trip
claim. The author accepted the final four-root and reactivation re-smokes.

Retained risk `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` records that native
`.cedg` still lacks one dedicated end-to-end periodic-quarantine round trip.
Ledger recompute/export-import/copy and nonperiodic native dormant/reactivated
round trips remain the existing evidence. This is nonblocking for R4, must be
resolved or explicitly dispositioned by global G9 closeout, must be revisited
by G9U1 validation, and is not an implicit R5 dependency.

# G9S1-R1-D2 — pair atlas trivialization, sheet identity and quarantine

- Status: **FINAL DESIGN CANDIDATE — PENDING AUTHOR REVIEW**.
- Accepted finding: **DISPOSITION C — AUTHOR ACCEPTED**, for the demonstrated
  periodic monodromy scope in the [preceding design](g9s1_r1_symmetric_atlas_design.md).
- `generalRegularPairInvalidation=REJECTED`.
- `productiveImplementationStarted=false`,
  `productiveImplementationAuthorized=false`, `selfApproved=false`,
  `authorApprovedPhase=false`, `passClaimed=false`.
- Implementability review: **B — PARTIAL IMPLEMENTATION CONTRACT**.
- This is the final bounded design iteration within R1, not a new productive
  phase. No further open-ended research phase is proposed or authorized.

## 1. Authority, accepted finding and precise scope

The author accepts the exact repeated-periodic monodromy finding, NOT a phase
PASS, generic regular-motion invalidation, or completed unrestricted pair
selector. The previous reports, tests and evidence snapshots remain unchanged;
their pending wording records their respective review checkpoints. D2 records
the subsequent acceptance here, in the roadmap and in its own evidence.

[ADR 0009](../adr/0009-locus-v2-locus-intersection-pair-semantics.md) remains
the symmetric pair authority. [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md)
does not supply a pair rank. D2 neither modifies those ADRs nor changes current
public SplineV2 pair behavior, which remains rich-only until an implementation
is separately authorized and verified.

The concrete proposed materializable subset is:

> One certified, regular, uniquely identified transverse germ class in one
> complete semantic branch/component product of two distinct durable sources.

There are only two transverse signs. This permits **at most two eligible roots
per component pair**, possibly more across distinct component pairs. Several
same-germ roots in one component pair remain rich-only. In particular D2 does
NOT promise to materialize all nine roots of the quartic counterexample or
both roots of the repeated-periodic monodromy witness. A numerical certificate
or arbitrary labeling of an atlas does not supply the missing structural name
for those sheets. This limitation requires explicit author review as a partial
scope, not silent conversion of the original general objective.

## 2. Exact structural selector and canonical source reversal

Use the current unordered source authority `LocusPairIdentity2D.sourcePair`.
Normalize the computation axes by the exact durable source-ID ordering; never
by argument order, geometry or root output. Equal-source/self-pair queries are
outside this first subset. Keep each branch/component attached to ITS source:
independently sorting two source IDs and two component strings could lose that
association and is not sufficient for this selector.

The exact semantic tuple proposed for a pair binding is:

```text
scheme = pair-singleton-transverse-germ/v1
source descriptor A = (durable source ID, stable branch lineage,
                       stable component lineage, declared orientation,
                       semantic domain kind, parameterization contract version)
source descriptor B = (same fields, associated with the other source)
germ = POSITIVE or NEGATIVE in canonical A/B axes
```

Descriptors are ordered by durable source ID. The enclosing existing ledger
material additionally binds result owner, source pair, constructive query
lineage and structural topology contract. No new mutable topology epoch is
introduced. These are structural contract/lineage identifiers, NOT current
coefficient signatures or revision counters. Component endpoints, parameter
values, knot/span numbers, isolation boxes, atlas cell IDs, discovered counts,
source labels, result order and movement history are absent from the selector.

The durable representation is a discriminated typed tuple, serialized with
strict canonical framing of each field in this fixed order and exact enum
spellings. Use the existing length-framed string convention after validating
the structural identifiers. No locale-dependent comparison, whitespace
normalization or coordinate hash is allowed. The proposed scheme identifier
is a design discriminator, not a Java class name or a second public command.
The externally consumed token stays opaque and uses exact ledger ownership;
its allocation incarnation is not geometric selection authority.

Let `epsilon_A`, `epsilon_B` be +1/-1 for the provider-declared traversal
orientation, and derivatives refer to the explicit source parameters. Define

```text
H(u,v) = C_A(u) - C_B(v)
g = sign(det[epsilon_A C_A'(u), -epsilon_B C_B'(v)])
```

The normalized determinant has the same sign and must be established under
the current regularity/residual policy. The numerical determinant magnitude is
evidence, not a selector field. On caller reversal `(u,v)->(v,u)`, the caller
sign reverses; canonical normalization reverses it back. The selector is thus
identical for the same unordered query context. Two separately created rich
results may have different owner tokens; their semantic selector mapping is
equivalent, not permission to share point ownership across result objects.

Exact closure-copy remaps each descriptor through its source provenance. If
the copied IDs reverse the canonical axis order, transpose both address proofs
and negate the caller sign before canonicalization. Never copy only one source
or sort components independently. New transformed sources have NEW identities;
geometric covariance never copies source tokens onto them.

## 3. Trivialization criterion and minimum coverage

For CURRENT source state `lambda`, fix one descriptor pair and sign g. Let
`R_g(lambda)` be ALL valid regular pair preimages of that sign in the complete
declared component product, not just discovered roots. A positive certificate
must establish:

1. Current source IDs, both revisions, domain/branch/component ownership and
   parameterization contracts are coherent.
2. One finite interior root, or an intrinsic periodic-seam equivalent, has
   exhaustive local coverage plus justified existence and uniqueness, current
   transverse sign, residual/error evidence and no local topology ambiguity.
3. No other root of this sign exists anywhere in that component product.
4. No unresolved region, overlap or singular candidate in that product can
   compete with that selector under the certificate's stated assumptions.

Use a finite current coverage partition. Every part must be certified as:

- zero-free; or
- belonging to the same established unique root by a certified common-root
  relation; or
- incapable of containing the selected germ, for example a uniformly strict
  opposite Jacobian sign throughout the region.

Any unknown region that could contain the selected sign blocks the positive
certificate. A determinant interval containing zero is not an opposite-sign
exclusion. Neither discovery count one nor unchanged discovery counts suffice.
The certificate may leave opposite-germ roots unenumerated when the strict
sign exclusion is proved, and other component products may remain unresolved.
Thus this selector needs **class-complete coverage**, not necessarily global
intersection COMPLETE. All ordinary rich-result admission checks still apply;
D2 does not bypass a globally nonfinite/unsupported publication.

This criterion supplies a concrete trivialization. Along any connected regular
deformation within this singleton scope, the implicit-function theorem gives
a local section; uniqueness in the entire class forces those sections to
agree. The structural sign labels the sheet, so neither a seed root nor a
chosen chart tree is required. Loops in this scope have identity permutation
on the singleton. No simply-connected assumption is needed once the global
singleton property is proved. This does not certify an arbitrary unsampled
movement path from its endpoints; current resolution does not need that path.

Certificates are derived deterministically from current source authority with
an explicit finite work budget. Warm state/previous roots may not change the
outcome under that budget. Budget exhaustion means missing evidence, not proof
of geometric ambiguity. It cannot trigger permanent retirement. Constructing
interval/Krawczyk evidence is future implementation work already characterized
numerically; D2 adds no production numerical infrastructure.

## 4. Certified chart, knot and seam transitions

Boxes and charts are replaceable proof objects. At one current state, two
charts certify the same sheet if they use the same structural selector and
both meet section 3. For a cheaper local match, sufficient evidence is:

- certified existence in a subregion contained in both uniqueness regions;
- one verified root enclosure contained in the other's uniqueness region; or
- existence in both plus uniqueness on a justified containing region.

Mere box overlap, equal germ without class uniqueness, nearest coordinates,
matching solver indices and chart adjacency do not suffice. Over a connected
source-state overlap, certified unique continuous sections plus one certified
common-root witness fix the transition; every disconnected overlap component
needs its own witness. Inverse and cocycle consistency are checked. The
singleton structural label fixes the permutation, avoiding arbitrary sheet IDs.

An internal spline knot is not in the selector. Crossing it preserves identity
when current semantic gluing/regularity and unique preimage ownership are
established. A half-open periodic seam is an identified domain boundary, not a
new component: canonical ownership removes its duplicate representation.
No periodic lift/winding history enters this selector. An ordinary endpoint
exit from a genuinely nonperiodic domain is different and may lose validity.

If floating polynomial glue or transformed composition cannot be certified,
report missing evidence, not "chart changed, identity lost". The exact model
controls do not certify all current floating SplineV2 knots. This is a numerical
support boundary, not permission for generic regular-pair invalidation.

## 5. Monodromy policy: canonical obstruction handling, no arbitrary cut

The accepted exact repeated-periodic witness has two roots in one same-germ
class. It is outside the singleton scope at EVERY state of its loop. The
deterministic D2 policy is to keep that class rich-only, regardless of whether
the user has taken zero, one, inverse or two circuits. No root is assigned an
arbitrary phase or cut-side label. This is the permitted **equivalent canonical
obstruction handling**, rather than inventing a general semantic cut.

Distinguish evidence levels:

- Two independently certified distinct same-class roots prove selector
  ambiguity; no loop proof is required for this negative.
- The accepted all-path regular loop, exhaustive roots and final transposition
  prove nontrivial monodromy for that scope.
- A projected-rank swap proves neither ambiguity of a structural singleton
  nor monodromy.
- Missing coverage proves only unresolved certification.

A claimed generic atlas trivialization contradicted by a certified nonidentity
loop permutation is rejected as a **typed invalid-trivialization barrier**.
Such a sheet is not activated by D2. The monodromy proof is about the semantic
scope, not "how many loops this point has travelled". No remembered path causes
irreversible retirement. The missing structural labels for same-germ sheets
are explicitly outside this partial implementation contract; no generic
monodromy detector, reference-fiber coordinate seed or cut-placement engine is
required for implementing the bounded subset.

## 6. Current-state lifecycle and recurrence

The following are contract states/reasons, not claims that Java enums with
these names already exist:

| Current evidence for an existing binding | Result | Retained authority |
|---|---|---|
| Same selector and current certificate uniquely resolves an admissible root | Active, same ordinary point defined | Exact token, selector and owner |
| Source temporarily undefined, no root, stale revision, uncertified isolation or exhausted coverage budget | Dormant/unresolved, point undefined | Same selector/token; absence is not retirement |
| Multiple certified same-class matches, relevant topology ambiguity, or a nontrivial sheet obstruction | Pair-quarantined, point undefined | Same selector/token; no competing allocation |
| Same selector later has a valid unique current certificate | Reactivated, SAME point/ID/token | No new point and no previous-position test |
| Exact owner/source/lineage contract is replaced incompatibly | Structural barrier; old binding cannot serve replacement | Never remap through labels; G9A/undo rules apply |

Quarantine is a reason for retained non-current ownership, not an extra geometric
selector. Cache/persist the reason for diagnostics if useful, but reload always
revalidates current source context and certificate. Saved ACTIVE is not proof;
saved quarantine cannot suppress a newly valid same-selector certificate just
because an earlier path was ambiguous. Proven merge/split/tangency/overlap or
loss of isolation remains typed and non-admissible; D2 never continues through
it by a guess.

**Explicit recurrence consequence requiring author review:** the durable point
denotes the unique structural germ SLOT. If the same context has cardinality
`1 -> many/0 -> 1`, it may reactivate when that same predicate is again uniquely
satisfied. This proves recurrence of the selector, NOT historical continuation
of a physical trajectory across the singular interval. Demanding the latter
while forbidding history would be a different unsupported contract. No root
retargets while the singleton scope remains regular and valid.

Permanent token retirement is reserved for explicit incompatible durable
identity/lineage/owner changes or disposal under existing lifetime rules, not
an ordinary box/rank/knot change, a failed certificate or remembered loop.
Here replacement means an actual explicit durable replacement/disposal under
the existing G9A/lifetime contract. A current lineage/contract mismatch that
can disappear during ordinary recomputation retains dormant ownership; it is
NOT an irreversible retirement event. Returning to identical durable inputs
must yield the same current-selector outcome independently of that detour.
An old token cannot bind a new source ID. Undo restoring the complete earlier
construction/ledger snapshot restores its earlier authority normally; this is
not new identity inference. Within unchanged durable inputs D2 has no persistent
"a permutation was once observed" retirement bit. The invalid-trivialization
barrier in section 5 is sufficient to reject unsupported sheet activation;
R4's one-dimensional historical offset-retirement rule is NOT transplanted.

Fresh materialization requires an active unique certificate. A never-materialized
ambiguous root receives no durable point token. Ordinary recompute resolves
current roots once and then existing bindings by selector lookup; it creates no
new GeoElements. Retain current entries and claimed dormant selectors, not an
unbounded history of all prior roots. Release the final claim through existing
ledger pruning/lifetime rules.

## 7. Actual R4 reuse boundary and future persistence contract

The inspected [ledger](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionTokenLedger2D.java)
has private `ACTIVE(a)`, `CLAIMED_ACTIVE(c)`, `CLAIMED_DORMANT(d)`,
`PERIODIC_QUARANTINE(q)` and `CLAIMED_PERIODIC_QUARANTINE(r)` states. There is
no generic public CURRENT/RETIRED enum. Retirement removes recoverable ownership
and the monotone incarnation prevents reallocation of the same exact token.
Canonical ledger version is v4, importing strict v1-v4; the public token
envelope remains `locus-root/v3/`.

Reuse:

- `begin`/`commit`/`abort` and atomic rich-result publication;
- exact owner/token framing, duplicate-binding rejection and claim counts;
- `validatesCurrentToken` versus `validatesRetainedToken`;
- retained claims while the ordinary GeoPoint is undefined;
- `retainMaterializedToken`/`releaseMaterializedToken`, pruning and high-water
  incarnation mechanics (not incarnation as root selection);
- exact immediate closure-copy provenance and dependency checks;
- the existing XML ledger attachment and normal DAG recomputation.

The current [point consumer](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionPointV2.java)
already performs exact lookup, retains ownership and changes the same point's
definedness without another solve or replacement point. Its architecture can
be preserved. The current result indexes admissible solutions by exact token.

Do NOT reuse one-dimensional geometric semantics:

- `IntersectionRootDeterministicSelector2D` accepts only the typed scalar-target
  germ and optional one-dimensional rank/cardinality;
- `IntersectionRootAddressProof2D` stores one parameter and scalar target
  contract, not a pair certificate;
- R4 phase-tube/offset/quarantine routines compare one-dimensional phase groups;
- legacy one-source singleton migration is not pair-token migration.

The current [pair algorithm](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusLocusIntersectionV2.java)
intentionally publishes no ACTIVE public-ledger entries. Its diagnostic
`locus-pair-root/...` handles include appearance epochs; they are not eligible
for silent reinterpretation as the new durable selector. Moreover,
[GeoLocusIntersectionResult](../../source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusIntersectionResult.java)
currently validates nonempty persisted ledgers against the one-source
`AlgoLocusIntersectionV2` parent. Pair integration must extend this exact
two-parent reconstruction/attachment seam, not just mint a string token.

The prospective implementation therefore needs a discriminated PAIR binding
and two-address current-certificate payload in the existing ledger, plus a typed
non-current pair reason where diagnostics require it. Reuse generic claimed
dormancy; never mislabel pair ambiguity as R4 PERIODIC_QUARANTINE. A pair-bearing
snapshot must use a new strict format (v5 if no intervening format has been
introduced), retaining v1-v4 import rules and rejecting down-versioned pair
payloads. Existing one-source serialization/behavior is unchanged. No second
coordinate-token or parallel point persistence system is introduced.

Persist exact selector, token owner/incarnation and claim/lifetime evidence.
Any saved revisions, parameter enclosures or reasons are nonauthoritative on
reload. Reconstruct both source dependencies and validate the selector before
activation; do not serialize opaque lambdas or trajectory records. Copy needs
both current source contracts/semantic addresses and exact G9A closure mapping;
uncertain mapping keeps the copied point non-current. Import cannot promote
old rich-only diagnostic pair handles. New explicit selection after a future
implementation may allocate a new proper token.

## 8. Bounded implementation plan and costs — not authorization

If the author accepts B, the shared-kernel implementation boundary is:

1. Current pair local numerical proof and class-complete coverage over finite
   supported component products; no generic state-space atlas construction.
2. Separate typed singleton-germ selector and current pair certificate.
3. Transactional pair ledger publication after both source IDs and owner attach.
4. Discriminated ledger persistence/copy/attachment support and retained reasons.
5. Existing rich-result/exact-token/ordinary-point consumer and focused tests.

Support only polynomial semantic sources/compositions for which actual current
evaluation, domain ownership and local regularity can be certified. Unbounded
products, unsupported sources, unknown glue/composition, singular/overlap
cases, same-germ collisions and insufficient work-budget evidence remain
rich-only. Existing R5 rounded-coefficient negative evidence is retained;
exact-map covariance does not certify floating transformed evaluation.

For B certified coverage cells, R published roots and P existing bindings, one
shared solve/certification pass followed by deterministic root/selector maps
costs its actual interval arithmetic plus approximately `O(B + R log R + P)`
bookkeeping. The work budget bounds B; no per-child global solve or trajectory
replay is permitted. These are design bounds, not measured product performance.
The implementation must disclose budget failures and validate useful examples;
no numerical guarantee or global completeness is upgraded by selector identity.

## 9. Diagnostic contract matrix and validation boundary

The existing 36 diagnostics are retained. A bounded D2 test-only model checks
structural keys and lifecycle decisions from declared certificate fixtures;
it is not a production interval certifier or serialization implementation.

| Case | D2 expectation |
|---|---|
| Simple two-root transverse pair, opposite germs | Both singleton classes can be eligible |
| Projected u/v ranks exchange | Same two structural selectors; no dormancy merely at the tie |
| Multiple roots in one span pair | Eligible only for a unique certified sign class; no span ordinal |
| Original cubic three-root example | Unique class can be eligible; repeated class remains rich-only |
| Original quartic nine-root example | Germ classes of cardinalities five and four (sign depends on canonical axis convention): both rich-only, not nine materialized points |
| Replacement of boxes/charts; compatible internal knot | No selector change; require current proof |
| Source reversal/copy normalization | Exact descriptor association and normalized sign; no operand priority |
| Single-traversal periodic seam | Unique sign class survives canonical wrap |
| Accepted repeated-periodic monodromy, inverse/two loops | Same ambiguous class every time; no loop counter or accidental release |
| Merge/tangency/unknown coverage | Non-current; no false unique binding |
| Same-selector recurrence after ambiguity | Same retained diagnostic point/token, no allocation; not physical-trajectory proof |
| Diagnostic snapshot save/reopen | Exact structural key/context, current certificate rechecked |

Exact-model algebra supplements the certificate-fixture model: with
`C1_t(u)=(t,u)`, `C2(v)=(v^2-1,v^3-v)` and `|t|<=0.1`, the two roots are
`v=+/-sqrt(1+t)`, `u=t*v`; `det D(C1-C2)=2v` never vanishes. Their u ranks
exchange at zero although their opposite signs remain unique. Reversing
operands gives the v-rank control. This positive example strengthens, rather
than overwrites, the earlier same-germ rank counterexample.

DEV A/B and Checkstyle are the required scoped authority; counts/hashes/logs
and limitations are recorded in [D2 evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-d2-design-evidence.json).
Both DEV runs completed 48/48 methods (12 D2 plus 36 retained controls), with
zero failures/errors/skips and fresh test-task execution. Checkstyle completed
with zero violations in 695 inspected files. The D2 contract-model trace is
identical in A/B:
`101cba5816b57b48feb2bd9a2185841809115331fc11a6421ea78550b5797354`.
This is deterministic conditional-model evidence, not a certified production
solver or actual pair-token persistence. Only documentary clarification and
evidence recording followed these runs; their test sources remain unchanged.
No PHASE/COMPOSED/FULL acceptance is claimed. Future productive implementation
needs its own required complete gates, real pair-token `.cedg` round trips,
copy/undo/redo and negative migration cases. Diagnostic state tests do not
satisfy those productive persistence gates.

`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`; `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`:
no wrapper, toolchain, dependency, numerical baseline or workstation assumption
changes. `GUIDE_IMPACT=NONE`: no released behavior or API changes. The existing
DEV wrapper, test compilation and Checkstyle suffice for this design iteration.

## 10. Final decision and G9U1 delta

**B — PARTIAL IMPLEMENTATION CONTRACT.** The singleton-germ selector, current
coverage criterion, transition mapping, obstruction handling and retained-state
mechanics are specific enough for bounded implementation IF separately
authorized. The arbitrary same-germ multi-sheet contract is not ready and is
excluded, rather than hidden behind an unnamed future sheet/cut mechanism.

The next author decision is whether to accept this explicit partial scope and
its structural-slot recurrence semantics, and authorize productive R1 under it.
If full same-germ materialization remains mandatory, this partial contract must
not be represented as sufficient implementation authority; those cases remain
rich-only without another invented phase or unauthorized semantic fallback.

G9U1 DESIGN remains author-approved at the protected checkpoint
`00982e7e148a634cd57ed928f322774df267d5e3`, unchanged. Its future reconciliation
must distinguish certified singleton-pair materialization from remaining
same-germ rich-only roots, explain current non-current reasons, and consume
exact kernel tokens. Markers, auto-materialization, Point-tool inverse resolution
and proximity cannot bypass this limitation. G9U1 implementation remains
NOT AUTHORIZED / BLOCKED UNTIL G9S1-R1 DISPOSITION.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED in the
[roadmap](../roadmap/geocedg_roadmap.md); D2 neither resolves it nor merges pair
obstruction handling with R4 periodic quarantine. No commit, push, tag or
productive G9S1-R1/G9U1 execution is part of D2.

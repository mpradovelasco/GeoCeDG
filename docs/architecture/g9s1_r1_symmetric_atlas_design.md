# G9S1-R1 symmetric pair atlas and monodromy — design iteration 2

- Status: **DESIGN CANDIDATE — PENDING AUTHOR REVIEW**.
- Current design approval: **NOT APPROVED — CONTINUE DESIGN**.
- Work: **DESIGN / RESEARCH ITERATION IN PROGRESS**; no productive implementation.
- Disposition: **C — MONODROMY-AWARE ATLAS / QUARANTINE REQUIRED** in the
  proved repeated-periodic scope, not in every spline-pair family.
- `implementationStarted=false`, `productiveImplementationStarted=false`,
  `selfApproved=false`, `authorApproved=false`, `passClaimed=false`.
- The first iteration's rank counterexamples are accepted diagnostic evidence.
  Its proposed fallback invalidation merely at projected-rank/chart changes is
  **rejected by the author**, not implemented and not the recommendation here.
- Entry remains `109f077fc5e2a40bcde45d3271eb928ee66fdfcc` on
  `codex/g9s1-r1-spline-pair-materialization`; product main is unchanged.

## 1. Authority and preserved evidence

The [first review](g9s1_r1_pair_materialization_design_review.md), its six
diagnostic methods and its [original evidence snapshot](../../geocedg/validation/g9s1-r1/g9s1-r1-design-review-evidence.json)
are retained. The old snapshot hashes describe that iteration, not later living
roadmap/research updates. Its negative results have not been weakened.

[ADR 0009](../adr/0009-locus-v2-locus-intersection-pair-semantics.md) keeps the
unordered source-pair and dual semantic preimage authority;
[ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md),
decision 12, expressly excludes extending its one-dimensional rank to pairs.
No accepted ADR, source algorithm, token, public command or serialization
contract is changed by this proposal. Current published pair roots stay rich-only.

The protected G9U1 design remains at
`00982e7e148a634cd57ed928f322774df267d5e3`; no content is imported from or written
to that branch. Its implementation remains unauthorized and blocked pending R1
disposition and separate design reconciliation.

## 2. Three different spaces and three different objects

Let `lambda` denote CURRENT semantic construction inputs, with both durable
source identities and component/orientation lineage fixed. Let

```text
H_lambda(u,v) = C1_lambda(u) - C2_lambda(v)
E = {(lambda,u,v): H_lambda(u,v)=0, current regular admitted preimages}
p : E -> X, (lambda,u,v) -> lambda
```

Here admitted means valid semantic source-domain membership, NOT the subset
that numerical discovery happened to find or currently certify.

`X` is a specified semantic construction-state domain. It is NOT the drawing,
the parameter rectangle `D1 x D2`, or a remembered mouse trajectory. These spaces
must not be conflated: a root box lives in the fiber; monodromy is over loops
of source definitions in `X`.

| Object | Purpose | Durable identity? |
|---|---|---|
| Numerical certificate | Existence/uniqueness and current revisions in a parameter box | No; its endpoints, residuals and Jacobian are revision-scoped |
| Semantic chart | A justified family of local root sections over a region of `X`, plus fiber ownership | Not by its name, subdivision number or numeric boundary |
| Durable selector | One globally coherent sheet in an explicitly certified semantic scope, bound to unordered sources | Candidate semantic authority; must survive replacement of charts/boxes |

The implicit-function theorem gives local sections at nonsingular roots. To
claim a finite covering over a larger scope also requires constant finite fibers,
no unaccounted boundary escape, correct knot/seam identifications, and suitable
continuity/properness over that scope. Local Newton convergence alone supplies
none of these global hypotheses. A proof can concern only a selected isolated
subcover and leave unrelated roots unresolved; total query completeness is not
automatically required.

## 3. What the rank counterexamples do and do not prove

The first review exhibits three-root and nine-root polynomial pairs with
nonsingular distinct preimages whose projected u/v orders exchange. These are
ordinary local root sections. Changing a projection rank must not be an identity
barrier. Likewise replacing an isolating box cannot change its enclosed root's
identity when a certified correspondence exists.

Those examples are not closed source deformations and do not demonstrate a
nonidentity permutation around a loop. They therefore do NOT prove monodromy,
nor impossibility of a stronger global selector in their local neighborhoods.
All nine regular roots of the quartic example have disjoint neighborhoods over
a sufficiently small common interval of its deformation parameter. The
implicit-function theorem supplies such an interval; its existence is proved,
but a production outward-certified bound for its full size has not been built.
An atlas can preserve those nine sections across both rank exchanges.

## 4. A genuinely regular monodromy witness

### 4.1 Exact piecewise-polynomial sources

Define on `0 <= t <= 1`

```text
q(t) = (1 - 3t^2/2 + t^3/2, 3t/2 - t^3/2).
```

Join its four quarter-turn rotations to form `Q(s)`, with period 4. This is
an exact C2 piecewise-cubic oval: positions, first derivatives and second
derivatives agree at all quarter joins and the seam. It is not an approximate
circle and no trigonometric circle identity is asserted.

On each quarter,

```text
det(q(t),q'(t)) = 3/2 + (3/4)t^2(1-t)^2 >= 3/2.
```

Coordinates remain in the corresponding quadrant, so the polar direction is
strictly increasing and covers each direction once per period. In particular
the oval is regular, nonzero, simple and star-shaped. The displayed positive
polynomial bound holds for EVERY real t in the interval, not only test samples.

Use the two semantic sources

```text
C1(u)   = Q(8u),                    u in R/Z, declared period 1;
C2_r(v) = (1/2 + v) r,              v in [0,1].
```

The first is one component with two constructive traversals of the same oval.
Its distinct preimages are not merged by equal image coordinates. The second
has a straight image and can be described with a public degree-3 collinear
SplineV2; the public command does not accept degree 1. The polynomial image
itself is affine. Repeated traversal of C1 is not an overlap solution family
between C1 and C2: their pair zeros below are finite and isolated.

Let the free vector/point `r` trace `Q(s)` once, `s:a -> a+4` (for example
`a=1/4`). **Only r's current coordinates are source inputs**. The notation s
describes the diagnostic deformation; it is not stored as an extra driver,
winding counter or input that changes between the initial and final definitions.
The same defining objects/IDs have exactly the same values at the end.

### 4.2 Complete roots, regularity and permutation

Star-shapedness proves that every equality `Q(8u)=(1/2+v)Q(s)` has `v=1/2`
and exactly these two preimages:

```text
(u0,v0) = (s/8 mod 1,       1/2)
(u1,v1) = ((s+4)/8 mod 1,   1/2).
```

They stay one half-period apart. There are no missing roots, component changes,
overlap families or pair collisions. For `H=C1-C2`,

```text
det D_(u,v) H = 8 det(Q(s),Q'(s)) >= 12.
```

Both roots have the same normalized contact sign and the same local Brouwer
degree `+1` in this ordering. The determinant remains nonzero even at internal
knots/seams because Q is exactly C2. Thus no hidden knot singularity explains
the loop. Parameter wrap is the usual circle identification, not root loss.

After the loop the source definitions are identical but continuous lifting sends
`u -> u+1/2 mod 1`: the two roots are permuted. Reverse traversal produces the
inverse permutation (the same transposition); two full circuits return each lift.
A small out-and-back loop has the identity permutation.

**This is genuine nontrivial monodromy**, unlike projected rank exchange. The
model is a separated parametric-curve pair with exact dyadic polynomial spans,
not the unrelated generic complex equation `z^2=p` used as a shortcut.

### 4.3 Relation to the actual SplineV2 constructor

The natural public input family is a closed, twice-traversed cardinal-point
SplineV2 plus a collinear four-point degree-3 SplineV2. Nonconsecutive repeated
input points are allowed; adjacent coincident points are not. A constant weight
can supply uniform parameter spacing. The current constructor solves in IEEE
double with a backward-error guard, not certified exact polynomial/C2 gluing.

The mathematical witness uses the exact coefficients above. Separate test-host
characterization checks actual constructor values, derivatives, IDs, updates
and reload. Numerical agreement is NOT upgraded to an all-parameter certificate
for the floating public construction. No native public pair materialization,
monodromy detector or persistence implementation is claimed. The exact witness
already rules out a universal cut-free contract at the intended shared
piecewise-polynomial semantic layer, including supported periodic repeated
traversals; it does not prove monodromy for every ordinary nonperiodic spline.

## 5. Why an atlas helps, and why it does not eliminate holonomy

An atlas is stronger than a fixed rank. On each certified base region `Xi`,
root sections are locally unique. A transition is a bijection of sections on
each CONNECTED overlap component. It is evidence of the same root, not a new
token. All successful transitions preserve the durable selector, even when
the chart, projected order, knot owner or isolating box changes.

Sufficient same-current-root tests include:

1. Existence certified in `W subset Bi intersection Bj`, combined with uniqueness
   in each of Bi and Bj.
2. A verified enclosure of root i contained in Bj, where uniqueness is certified.
3. Existence in both boxes plus uniqueness on a justified containing region
   (for example their rectangular hull).

Mere intersection of boxes is insufficient: two overlapping boxes may contain
different roots in their non-overlapping portions. Existence plus local degree
of the same sign in each box is also insufficient.

To extend a pointwise match across a connected BASE overlap, certify continuous
sections and their uniqueness throughout it. Equality is then open and closed:
one certified witness fixes the match on that connected component. Separate
components require separate matches. Parameterized Krawczyk/tube proofs may
supply these hypotheses; a finite set of successful samples cannot.
The inspected [Duff–Lee parametric Krawczyk theorem](https://arxiv.org/html/2402.07053v2#S3.SS2)
is a candidate numerical foundation for such a tube, not global sheet identity;
its exact-real/affine-homotopy termination analysis is not a claim about a future
finite-precision Java implementation.

Transitions must satisfy exact inverse and triple-overlap cocycle checks, with
source-reversal maps. But even consistent local transitions may have nontrivial
holonomy around a base loop. Blindly unioning all reachable chart-sheet nodes
would merge the two sheets in the witness and destroy unique token ownership.
The atlas is not automatically a global deterministic labeling.

## 6. Strongest justified selector candidate

### 6.1 Trivializable scopes: preserve ordinary motion

On a certified finite regular subcover over a simply connected, locally suitable
base region, choose a semantic sheet trivialization and verify it. The candidate
selector is conceptually:

```text
unordered durable source pair + canonical component/orientation lineage
+ certified semantic scope/trivialization authority + coherent sheet address
```

The sheet address is the glued semantic section, NOT a chart number, root-list
slot, numeric root, box or last update. Its exact serializable structural encoding
is still design work; inventing a provisional production enum or opaque hash
does not discharge the proof. A reference fiber may label sheets for a proof,
but numeric reference coordinates cannot silently become durable selectors.

For a finite certified atlas, check all connected overlaps and transition
permutations. A deterministic tree may propose labels, but every non-tree edge
and covered base cycle must agree. Unaccounted regions/overlap components or
escaping roots invalidate the claimed trivialization; a hole alone need not
have nontrivial holonomy. Refinement/reordering of the atlas must
produce an equivalent sheet certificate, not new tokens. These requirements
preserve identity at ordinary rank/box/chart changes rather than quarantine them.

### 6.2 Nontrivial scopes: a limited and explicit obstruction

For the exact witness, continuous lifting of an initial root around a regular
loop ends at the other root. A current-state, single-valued, path-independent
selector would instead return the initial root. Both requirements cannot hold
globally with no cut. This contradiction is about the proved scope, not about
failure of a numerical method.

An explicit semantic cut/restriction can make a subcover trivializable. A cut
must be based on reviewed intrinsic source-domain structure (not screen axes,
arbitrary solver subdivisions or every chart boundary). Its exact placement,
invariant structural encoding and supported construction-state scope require
author review before implementation. Ordinary certified transitions elsewhere
must continue to preserve identity.

Alternatively, if current semantic evidence only determines a nontrivial orbit
of interchangeable sheets, keep that affected orbit nonmaterializable. This is
more restrictive for the witness but honest and current-state deterministic;
it is not a requirement to reject all spline pairs.

### 6.3 Do not smuggle history into quarantine

Current-state identity cannot tell a no-op from a complete loop when all current
source definitions and the durable selector are identical. Therefore a rule
"retire this token because the last updates traced a loop" cannot simultaneously
claim history-independent definedness for those identical inputs.

Proposed pair quarantine is justified ONLY by a current ambiguous sheet/scope
certificate or an explicitly approved monodromy cut contract. It does not activate
after accumulating a winding counter. A unique same-sheet current resolution can
reactivate the same existing point; no new point is created. A permanent barrier
must be an explicit durable semantic event whose changed input is documented,
not a hidden trajectory cache. The author's suggested retirement after a proved
permutation remains a policy question if it depends on the observed path; it
cannot be silently advertised as current-state path independent.
Merely renaming a remembered winding/loop a durable event does not fix that
contradiction. Introducing a genuinely different semantic input would itself
need separate author approval and would no longer satisfy the unchanged-input
version of the nine simultaneous objectives.

No new Java state or ledger version is introduced. If implemented later, reuse
existing non-current/dormant semantics where adequate and add a typed pair reason
only if necessary. Preserve durable selector provenance, current proof separation,
exact copy/remap, undo/redo and reload validation. A saved status is not proof:
reload must recompute/verify current scope and sheet validity. Never coalesce this
design with R4 periodic quarantine or its independent native-round-trip risk.

## 7. Germ, index and source symmetry

At a nonsingular real root, local Brouwer degree is `sign(det D H)`. It is stable
and can help reject mismatches, but both roots in the witness have degree +1.
The oriented tangent pair is identical at the two repeated preimages as well.
Neither degree, normalized determinant nor the full local image germ supplies
the missing sheet distinction. Certified degree on a larger boundary constrains
signed counts, not unique ownership of every root.

Under operand reversal use `tau(u,v)=(v,u)` and
`H_reversed(tau(u,v))=-H(u,v)`. Hence the parameter Jacobian determinant changes
sign; reversing sources must transform the evidence, not change identity.
Canonical axis ordering derives from the existing unordered durable-source
authority. Charts, parameter boxes, span ownership and transitions all carry this
exact transposition. Query call order is never the primary axis. The monodromy
permutation is conjugate under this source-reversal mapping.

As an exact mathematical-map statement, invertible R5 similarities applied to
both operands preserve pair preimages and multiply the Jacobian determinant by
the transform determinant. Reflection or negative determinant changes oriented
evidence according to this rule; it does not pick a different sheet. This is
not certification of the actual floating composition: the first iteration's
large-translation counterexample remains relevant, and a future certifier must
enclose/validate that actual semantic evaluation. A newly transformed source
pair has NEW identities and tokens. At k=0 regular isolation fails and this
atlas contract does not apply.

## 8. Coverage and cost

| Claim | Minimum sufficient coverage |
|---|---|
| One current root | Existence/uniqueness neighborhood, current source/domain evidence |
| Same root in two boxes | One of the certified common-root conditions in section 5 |
| One chart section | Tube/section over its declared base region, no escape or ownership ambiguity |
| Chart transition | Every relevant connected overlap component and its unique section match |
| Durable globally glued sheet in scope | Complete relevant atlas connectivity/cocycle/trivialization evidence |
| Rank among a group | Complete ordering relation for that selector group, not found-root count |
| Global result COMPLETE | Every required component product; NOT implied by local sheet evidence |

The selected subcover must be isolated from omitted roots throughout its scope;
otherwise incomplete coverage cannot justify its durable sheet. Nothing here
requires global query completeness merely to use a genuinely isolated subcover.

For `N` certified chart-section records, `E` overlap matches and `P` existing
bindings, validating already supplied permutations/gluing and resolving bindings
can be `O(N+E+P)` plus canonical ordering cost. Constructing the certificates,
covering source-state regions and proving loops are the dominant, potentially
combinatorial work. No measured product cost or universal atlas-size bound is
claimed. Do not import R4's `O(R log R + P)` as the cost of building an R1 atlas.
No design calls for one new intersection solve per child, replayed trajectories
or an unbounded per-update history.

## 9. Alternatives and expected usability

| Option | Soundness/identity and path independence | Dormancy | Persistence/cost and ADR fit | G9U1 consequence |
|---|---|---|---|---|
| A fixed cells, fail at every boundary | Can fail closed but rejects proved unique regular transitions; author rejected | Artificially frequent; subdivision-dependent | Simple storage, unacceptable boundary-as-identity policy | Unacceptable frequent loss of points during regular editing |
| B overlapping certified atlas | Preserves all certified ordinary transitions; needs trivialization for path-independent sheets | None at box/rank/ordinary chart boundaries; unresolved proof remains non-current | Stores semantic sheet/scope, verifies gluing; fits ADR 0009, no one-sided rank | Consume certified token status, never perform frontend matching |
| C global intrinsic selector | Best in proved trivial-cover scope; impossible cut-free for the witness | None within valid scope | Strong proof/encoding burden; not established for arbitrary pairs | Ordinary materialization only in proven supported scope |
| D atlas plus justified cuts/quarantine | Necessary option for witnessed nontrivial cover; exact cut/current-state policy still reviewable | Only affected obstruction scope/cut, not generic regular boundaries | Typed reason and durable provenance; more persistence tests; separate from R4 | Explain genuine sheet ambiguity and existing-point dormancy |
| E permanently rich-only | Truthful but never supplies constructive points | Every pair root unavailable | Existing behavior; no new lifecycle cost; fallback, not preferred final goal | Inspection only; no materialization bypass |

Diagnostic counts are not a population-frequency estimate. The exact two-sheet
witness completes one loop through eight quarter-knot crossings (four per root),
with no local singularity; a fixed-cell scheme would introduce avoidable events
at these joins. A correct atlas needs zero barriers there. Nevertheless one full
loop permutes sheets, so the reviewed global scope must contain a cut/restriction
or reject that ambiguous orbit. Small contractible loops produce no nonidentity
permutation or local barrier; testing them alone does not certify a global
trivialization or release quarantine imposed by a larger nontrivial scope.
Counts and sampled host measurements are recorded separately from all-parameter proofs.

## 10. Scholarly support and provenance

- Allen Hatcher, *Algebraic Topology*, Cambridge University Press (2002),
  [author-hosted chapter 1, section 1.3, Proposition 1.30](https://pi.math.cornell.edu/~hatcher/AT/ATch1.pdf):
  unique lifting of paths/homotopies for a covering. Applicability here requires
  the covering hypotheses in section 2; IFT at isolated samples is not enough.
- Jonathan D. Hauenstein and Margaret H. Regan, *Real monodromy action*,
  Applied Mathematics and Computation 373 (2020), 124983,
  [DOI 10.1016/j.amc.2019.124983](https://doi.org/10.1016/j.amc.2019.124983),
  [NSF-hosted paper](https://par.nsf.gov/servlets/purl/10195953), section 3.1,
  Example 3.1 and Theorem 3.3: real regular-fiber loop permutations and the
  simply-connected-base restriction. Their generic polynomial example is not
  substituted for the separated spline witness proved here.
- Numerical inclusion hypotheses remain those in the
  [previous numerical review](../research/g9s1_semantic_spline_numerical_methods.md).
  No production interval infrastructure or theorem-level floating spline
  certification was introduced in this iteration.

The oval construction, positivity identity, pair equations and permutation above
are explicit derivations in this review, not attributed to those books/papers.

## 11. Validation boundary, terminal state and next decision

Diagnostic Java and actual test-host controls are test-only. Final DEV A and B
each executed **36/36** methods, with zero failures/errors/skips: 18 exact-model
atlas diagnostics, 5 public-host controls, 6 preserved first-iteration diagnostics
and 7 unchanged published pair-capability controls. Both test tasks executed;
compilation reuse is disclosed in the saved DEV summaries. Final Checkstyle
exited 0 with no reported violations. The earlier 35/35 runs remain recorded;
their five style warnings were corrected and an explicit v-rank reversal test
added before both final reruns.

Canonical case-outcome SHA-256, equal in A/B:
`b3903248badbc83663181c125a29bd17239890a766bc4d9551d2aa8066af7523`.
Numerical diagnostic trace SHA-256, equal in A/B and under reversed root
enumeration:
`666ec7734560a75467e4a1e2615eec50d4436004c0758bba5ba43ed7707fae09`.
The trace records 65 fixed model snapshots with exact floating bit encodings;
sorting is evidence normalization, not a durable root selector or an
all-parameter numerical certificate.

### 11.1 Required diagnostic fixture coverage

| Fixture | Evidence and boundary |
|---|---|
| A simple two-root transverse pair | Exact radial segment/double traversal; two exhaustive roots, determinant at least 12 |
| B projected u-rank swap | Preserved cubic three-root counterexample; no singularity |
| C projected v-rank swap | Reversed cubic counterexample; exact source-reversal map |
| D both projected ranks change | Preserved quartic nine-root example and implicit velocities |
| E multiple roots in one span pair | Same quartic pair, nine distinct regular parameter preimages at zero |
| F internal knot crossing | All exact quarter joins C2; host right-owned knot characterization kept separate |
| G periodic seam | Exact wrap with no pair collision; public provider canonicalizes endpoint 1 to 0 |
| H operand reversal | Parameter transposition, reversed determinant sign and equivalent root set |
| I closed deformation without permutation | Contractible out-and-back analytic lift; not proof of global triviality |
| J suspected nontrivial loop | Now proved: one circuit transposes two roots; reversed loop inverse; two circuits identity |
| K merge/split | Analytic fold control loses regularity at the merge; not an ordinary chart event |
| L tangency | Same fold's zero Jacobian; typed barrier rather than rank fallback |

Direct, incremental, detour/reversed and reopened-current-state diagnostics
agree on the final current root set or source coefficient bits, as applicable.
The closed-loop LIFT is deliberately different: its nonidentity permutation is
the obstruction, not a passing test of a completed durable selector. No such
production selector has been implemented or tested. Ordinary common-root chart
changes have a positive analytic uniqueness witness; mere overlapping boxes
have a negative counterexample. A general interval-certified atlas generator,
general transformed-source enclosure and structural sheet serialization remain
unimplemented research/design obligations.

### 11.2 Host evidence is not the exact mathematical witness

The actual public periodic spline has 8 spans, one branch/component, uniform
dyadic knots and `FLOATING_POINT_UNCERTIFIED` quality. Its maximum coefficient
difference from the exact model is `0x1.03p-34` (61 coefficient bit patterns
differ). Observed join defects for derivative orders 0/1/2 are respectively
`0x1.afp-38`, `0x1.c4p-37`, `0x1.3cp-36`. These measured differences are not
silently treated as exact C2 gluing. The collinear degree-3 target has maximum
higher-order coefficient magnitude `0x1.cp-50`.

At the diagnostic host snapshot the existing rich solver reported SUCCESS,
FINITE, 2 roots, global completeness NOT_ESTABLISHED and 0 materializable roots.
That observed count is not hard-coded as a new solver acceptance baseline.
Direct/incremental/control-loop updates and a **host XML round trip** preserve
source IDs and identical final coefficient bits. No native `.cedg` ZIP round trip
or pair-selector/token persistence was tested or claimed. The model's separate
snapshot round trip stores only current control data, not lift history.

Exact commands, counts, source hashes and any failed attempts belong to the
[iteration evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-atlas-design-evidence.json).
No PHASE/COMPOSED/FULL acceptance is claimed.

`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`: no wrapper/filter policy, build,
dependency or canonical numerical baseline change. `BOOTSTRAP IMPACT — NO CHANGE
REQUIRED`: existing test/toolchain prerequisites suffice. `GUIDE_IMPACT=NONE`:
published API/UX and current guides remain truthfully rich-only. The scoped DEV
authority is required by the author's design-only request and ADR 0020.

**Disposition C** is justified by an actual regular monodromy witness. This
supersedes the unapproved first-iteration fallback; it does not approve a
specific production selector or indiscriminate quarantine. Next author review
should accept/reject the atlas/trivialization contract and decide whether to
authorize designing a structural cut for the proved periodic scope, or keep
only those affected orbits rich-only while developing trivial-cover scopes.
Exact general sheet encoding, construction-state scope certification, cut policy
and history-free lifecycle remain prerequisites to productive implementation.

G9S1-R1 design is not approved; G9U1 design remains approved but implementation
not authorized. `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED
in the [living roadmap](../roadmap/geocedg_roadmap.md). No commit, push, tag,
productive R1 or G9U1 execution is part of this iteration.

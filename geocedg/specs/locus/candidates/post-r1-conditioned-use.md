# Locus V2 conditioned use under declared hypotheses — candidate contract

| | |
|---|---|
| Status | **CANDIDATE, REVISION 4 — PROPOSED, PENDING AUTHOR APPROVAL. NOT NORMATIVE.** `selfApproved=false` |
| Version | `conditioned-use/v4-candidate` (revision 3 is the text of commit `57c495cce`; revision 2 that of `83cd56c4c`; revision 1 that of `ab37fb5ff`) |
| Decision | [ADR 0042](../../../../docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md), proposed, with the author decisions D-V1 to D-V7, the focal amendment requested by the author on 2026-11-21 and the two focal precisions requested after the review of revision 3 |
| Design | [architecture and source analysis](../../../../docs/architecture/post_r1_locus_conditioned_use_design.md) |
| Layer | Shared Java kernel (evidence, addresses, operations); derived rendering; Desktop adapters; export adapters |

This file is a candidate contract. It becomes normative only by an express author decision,
through amendments of the specifications named in §12. Until then every sentence below reads
as «the proposal is that».

## 1. Scope, activation and non-scope

It applies to every Locus V2 produced from a reconstruction slice whose reconstruction is
faithful, for the part of its driver domain that no certified component and no certified
nonexistence covers. It applies by default: there is no user switch, experimental mode or
document policy (D-V1). Eligibility is decided from the construction and its evidence at
every revision, and each operation decides admissibility from the premises it needs.

It certifies nothing. `locus-v2-existence-components.md` §15.1 is unchanged: a finite
exploration never establishes `LOCAL_CERTIFIED` or `COMPLETE`. The document authority of
ADR 0040, relation integrity and relaxed admission with their positive requirements, the
semantic snapshot, and every existing token, selector and address with its text and meaning
are unchanged. The only identity material this contract adds is the parameter address of
§6.1 and the root selector of §6.5, each in its own versioned kind; the origin of a binding
(§6.5) is a parameter address. A certified component
keeps every contract and guarantee it has today, and a producer that can be certified is
certified as before.

## 2. Independent axes

| Axis | Values that this contract reads or publishes |
|---|---|
| Reconstruction fidelity | `FAITHFUL` required; otherwise nothing of this contract applies |
| Point validity | status of one evaluation at one parameter |
| Existence and continuity under hypotheses | conditioned evidence (§3), with its class |
| Existence and continuity certified | `LOCAL_CERTIFIED` components, unchanged |
| Global completeness | `COMPLETE` only by certificate; otherwise `NOT_ESTABLISHED` |
| Numeric guarantee | the existing classes of a value, unchanged |
| Incidence and membership integrity | `relation-integrity/v1` under the constructive authority, with its requirements unchanged; the existing contracts under the legacy one. Conditioned evidence is not integrity evidence (§8) |
| Identity and continuation of selection | per selecting dependency: established deterministic selection, or not established |
| Admissibility of continuation | whether a route or a following exists between two positions; distinct from the validity of each position (§6.4) |
| Coverage | which part of the requested domain each fact covers |
| Currentness | revision of the definition; content identity of the evidence (§7) |

None is inferred from another. The interval-evidence values are ordered
(`EXPLORATORY`, `CONDITIONED`, `LOCAL_CERTIFIED`); that order is not a scale for the other
axes. `CONDITIONED` is of nature `NUMERICAL_UNDER_HYPOTHESES` and never certifies. More
samples never change the nature of a fact.

## 3. Conditioned evidence

### 3.1 Structure

The producer of the locus owns one conditioned evidence per published revision and policy
version. It partitions the uncertified part of the declared driver domain of a branch into
cells whose ends are dyadic fractions of that domain, and classifies every cell:

| Kind | Meaning |
|---|---|
| `SPAN` | both ends and the midpoint evaluate valid, every selecting dependency has the same established selection at the three nodes, and the geometric criteria of the policy hold |
| `TRANSITION_BAND` | validity differs at the ends; localized to the minimum step |
| `SELECTION_BAND` | the established selection of some dependency differs between nodes, or is not established at a node |
| `DISCONTINUITY_BAND` | a geometric criterion still fails at the minimum step: a jump or a cut is suspected |
| `FAILURE_BAND` | a node failed with a computational cause, a work limit, an authority refusal or an unreproduced selection; its interior is not subdivided |
| `ABSENT_BAND` | every node of the cell is invalid with the same eligible geometric cause (§5) |
| `UNEXPLORED` | the work limit of the policy ended before the cell was classified |

A *conditioned region* is a maximal run of adjacent spans. It is not a component: it has no
lineage, no ordinal identity and no persisted form. Neither a region, a cell, a node nor the
partition is ever identity material (§6).

### 3.2 Root testimony

A span requires, at each node, the *testimony* of every selecting dependency that the
driver-dependent part of the slice reads. For a token point of a Locus V2 intersection: the
token, its identity status, the provider-canonical parameter, the isolating interval, the
local isolation status, the numeric guarantee and the separation from the nearest other root
of the same result. For an inherited line-conic selection: its settled selection state. For
a structural point: its resolution and its integrity. The evaluator of the slice returns the
testimony with the evaluation; it reads evidence that the slice computed for that evaluation
and starts no additional resolution.

A dependency whose selection is not established at a node yields a `SELECTION_BAND`. A
missing separation value lowers the class of §3.3; it never satisfies the selection
requirement. No root is ever identified by index, proximity, order of coordinates or visual
coincidence.

### 3.3 Evidence classes of a span

For a span `[a, b]` with nodes `a`, `m`, `b`. H1 to H4 are the hypotheses of §4.

| Class | Proved on the whole cell | Checked at the nodes only | Still a hypothesis | Consumers |
|---|---|---|---|---|
| `RESOLUTION_ONLY` | nothing | validity; established selection of every dependency; chord deviation and turning between the nodes | H1, H2, H3, H4 | every conditioned consumer of §8 |
| `MARGIN_ESTIMATED` | nothing | the same, and a separation of the selected root above the policy margin | H1, H2, H3, H4. The separation observed at nodes is not a bound between them | the same as `RESOLUTION_ONLY`; the class is evidence for diagnosis and for choosing proof cells, not a stronger permission |
| `MARGIN_BOUNDED` | for the dependencies that carry the bound: the roots that the bound encloses stay pairwise separated on the cell. Nothing else | the same as `MARGIN_ESTIMATED` | H1; H2 in full, for every dependency, those that carry the bound included; H3; H4 | the same as `RESOLUTION_ONLY`. It is recorded so that a partial proof is not discarded and can be completed by a promoter (§11) |

A separation of roots is a fact about the positions of roots. It is never, by itself,
evidence that a selection is stable: which root a dependency selects also depends on the
germ and its orientation, on the eligibility and local isolation of each root, on the
membership of a limited target, on the cardinality of a collision group and on roots that
the bound does not enclose. H2 is reduced on a cell only by a proof of the selection
predicate itself on that cell, with the certified sign conditions and the captured selection
that `locus-v2-existence-components.md` §15.2 requires; that proof is the work of the
certifier or of a promoter, never of this traversal.

No class of this table certifies existence or continuity of the branch, and no consumer
receives a permission from one class that it does not receive from another. A cell on which
H1 to H3 are all proved is not a conditioned span: it is a certified component, produced by
the certifier.

### 3.4 Policy

The traversal policy is versioned (`conditioned-traversal/v1`) and is part of the evidence
identity. It fixes the initial depth, the chord-deviation and turning limits in model units
and radians, the minimum step as a fraction of the declared domain, the margin threshold,
the closed list of eligible absence causes and the work limit. Its limits are
world-coordinate quantities. Zoom, DPI, viewport and render policy never enter it.

## 4. Declared hypotheses

- **H1 (validity).** The traced point is defined at every parameter of the span.
- **H2 (selection).** Every selecting dependency keeps, at every parameter of the span, the
  selection established at the nodes.
- **H3 (continuity).** The traced function is continuous on the span, including across every
  operation with a branch cut that the slice evaluates.
- **H4 (resolution).** The traced curve stays within the chord-deviation limit of the policy
  from the polyline of the nodes.

The contract states, and the product states wherever it explains the grade, that:

- a traversal detects an event only when a node falls where the event is observable, or when
  the event leaves a geometric trace above the limits. Two events inside one cell, a root
  exchange that returns and a narrow excursion between nodes can pass undetected whatever
  their size relative to the minimum step, because the minimum step is reached only where
  refinement was triggered;
- a slope observed between two nodes, multiplied by any factor, is not a Lipschitz bound;
- agreement of two traversals or of two estimates is not an error bound.

The adverse cases of revision R1 and of block 3A are kept as the validation bench of the
design (§7 of the design document) and are part of the acceptance of increment I0.

## 5. Absence, failure and totals

1. A node whose evaluation failed for a computational cause, a work limit, an authority
   refusal, an unreproduced selection or an unfaithful slice is never evidence of absence.
   Its cell is a `FAILURE_BAND`.
2. `ABSENT_BAND` requires an eligible cause at every node of the cell: an undefined
   dependency whose own status states a geometric absence. The list is closed and versioned
   with the policy. It is conditioned absence, never `CERTIFIED_DOES_NOT_EXIST`.
3. A total over a requested domain accounts for every cell of that domain. A band is never
   dropped because it is narrow. For each band, in this order: a contribution computed
   through the band when both sides have one-sided limits and the policy admits the band
   kind; else an explicit bound when one is available; else the result is a `SUBTOTAL` with
   the bands listed, or a refusal when the consumer has no subtotal. Every hypothesis used is
   recorded in the result.

## 6. Addresses, identity and persistence

### 6.1 Parameter address

A point at a parameter that no certified component contains is addressed by
`(source locus identity, branch key, provider contract, canonical parameter, periodic lift,
seam side)`, without a component selector. Its codec is
`geocedg-locus-parameter-address/v1`, distinct from `geocedg-locus-address/v1` and
`geocedg-locus-principal-selection/v1`, whose texts and meanings do not change. A sample, a
polyline index, a cell, a node or a screen position is never an address.

### 6.2 No reinterpretation and no invented migration

An address with a component selector resolves only as §15.6 says. It is never rewritten into
a parameter address, and a parameter address never acquires a component selector silently.
Converting one into the other is an explicit redefinition of the point. No migration creates
an identity or a spatial association.

### 6.3 Point validity

A parameter address resolves, and its point is defined, when the evaluation at its parameter
is valid, every selecting dependency has an established selection at that parameter and the
address is unambiguous. Nothing else decides it. In particular the kind of the cell that
contains the parameter does not: a point whose own isolated evaluation and selection are
valid is defined inside a span, inside a band of any kind and inside an unexplored cell
alike. The point reports the cell that contains it, as information.

A point is undefined, with a typed cause, only for a cause of its own: a parameter outside
the declared domain, an invalid or failed evaluation at that parameter, a selection that is
not established there, an ambiguous seam side, or a point component, which a parameter never
selects (§15.8). An undefined point never becomes a neighbouring point.

A valid evaluation at a parameter inside an `ABSENT_BAND` or a `FAILURE_BAND`, and an
invalid one inside a span, are contradictions of that cell: they are reported to the
producer as §7.3 says.

### 6.4 Admissibility of continuation

Continuation is another fact. A following, a drag along the curve, a route and a length
between positions need interval evidence along their path (§8.2 to §8.4). A point inside a
band or an unexplored cell is a valid position with no admissible continuation: following
from it and routes through it are refused with the kind of the cell as cause, and the point
keeps its address, its identity and its definition. Giving it another parameter explicitly
is a redefinition, not a continuation, and is always possible.

### 6.5 Root selectors on a conditioned region

The selector of an intersection root in `locus-v2-intersections.md` §6.2 binds the lineage
of the certified component that contains the root. A conditioned region has no lineage
(§3.1), and none is given to it: a region, its boundaries and its cells depend on which
events the traversal detected, so any identity built from them would be an identity by
sampling.

1. **Certified roots are unchanged.** A root whose source parameter lies in a certified
   component keeps the selector, the phase and rank rules and the token of the intersection
   contract, with the same text.
2. **Frame of a conditioned root.** A root whose source parameter lies in a span, and in no
   certified component, takes a selector of its own versioned kind,
   `conditioned-use/branch-frame-root/v1`:

   ```text
   selector kind
   + stable branch lineage of the source branch
   + typed transverse current-root germ, as §6.2 of the intersection contract defines it
   ```

   Its frame is the branch, which is the finest frame of the source that does not depend on
   the traversal. It contains no component lineage and no region, cell, dyadic address,
   node, parameter, coordinate, index, order or generation.
3. **No rank.** The intrinsic phase and rank need a certified oriented component and a
   verified cardinality of the collision group. Neither exists here, and no ordinal among
   the roots found replaces them. Two or more current roots of one result that share a
   branch-frame selector are ambiguous: they keep revision-local handles with the existing
   identity statuses and are not point-admissible.
4. **Allocation, with an origin.** Only a complete selector that is unique among the current
   published finite roots of the result allocates a durable token, and the allocating
   publication reports `NEW_TOPOLOGICAL_SOLUTION`, as the existing rule says. A result has at
   most one binding per branch-frame selector. The binding records, with the selector and
   the token, its **origin**: the parameter address (§6.1) of the root at the allocating
   publication. The origin is a creation datum, of the same nature as the parameter of a
   parameter address. It is not part of the selector, it is never updated by a later
   resolution, and two bindings never differ by their origin alone.
5. **Reuse guard.** The uniqueness of a branch-frame selector in the current state does not
   establish that the current root is the root that received the token: the frame is the
   whole branch, its regions are separated by bands, and the roots found are not known to be
   all the roots. A current root therefore resolves the token of its selector only when both
   conditions hold:
   - its selector is unique among the current published finite roots of the result; and
   - in the current published content of the source, the origin of the binding and the
     isolating interval of the root lie in one conditioned region: every cell from one to
     the other is a span. A band of any kind, an unexplored cell or a certified component
     between them fails the guard, and so does an origin whose own cell is not a span.

   When both hold the resolution reports `DETERMINISTIC_SELECTION_ESTABLISHED`, with the
   meaning that status has in the intersection contract: the unique root of its germ in its
   frame, which here is the region of the origin. H1 to H3 of the spans between the origin
   and the root are recorded among the premises of the resolution, and those spans are cells
   that the result used (§7, point 6). It is not a claim that an individual root was
   followed.
6. **The evidence withholds a token and never chooses one.** When the guard fails, the token
   is not current. It is not given to the root, transferred, replaced or retired; no other
   token is allocated for that selector while the binding exists; and the root is published
   with a revision-local handle, identity status `NOT_ESTABLISHED`, and is not
   point-admissible. An object that depends on the token is undefined with its existing
   cause and is defined again when a later state satisfies the guard. In particular, when a
   root disappears from one region and another root of the same branch and germ appears in
   a separate region, the second root does not receive the token of the first, whether both
   events fall in one update or a state without roots lies between them. This applies the
   sentence of `locus-v2-intersections.md` §6.1, «failure to establish current admissibility
   does not authorize another root to acquire the token», to the branch frame. A root holds
   either the single token bound to its selector or none: no content of the evidence, policy
   depth, order of contradictions or order of updates gives it another token or selects
   among roots. The origin is the only position that a binding records; it is fixed, and it
   can only withhold. Movement history, later positions and proximity never select, mint,
   transfer or replace a token, and no continuation is claimed across a band.
7. **Change of frame.** Each current root has one selector, decided by where its parameter
   lies in the current revision. When a root passes between a certified component and the
   uncertified part of its branch, its selector is another one: the earlier token is
   non-current and is never transferred, the root is published under the selector of its
   frame, and the earlier token is current again when the state returns. A branch-frame
   binding keeps its origin through such a passage.
8. **Pairs.** In an intersection of two loci each source side binds the lineage of its
   certified component, or the branch frame of that source when the root lies in a span of
   it. A pair with a branch-frame side has no rank; each branch-frame side records its own
   origin, and the guard of point 5 must hold on each of them.
9. **Persistence.** A branch-frame binding is stored in the token ledger as an entry of its
   own kind, in a new ledger format version, with its selector, its origin and its token.
   The origin is an address, not evidence: evidence is never stored. Every existing entry
   keeps its text and its meaning, and no entry is rewritten into another kind. A reader
   that does not know the version rejects the document closed, as the ledger contract
   already says; reading by GeoCeDG 1.2.1 is not required (D-V2).

Consequences, stated:

- on a conditioned source a result with two or more roots of the same germ on one branch
  publishes them as roots without durable tokens. That limit is removed only by
  certification of the component;
- a root that the configuration carries out of the region of its origin holds no token
  until a state returns in which the guard holds, and a root of the same selector found in
  another region receives no durable token in that result. Another intersection result has
  its own ledger context and allocates with its own origin;
- two documents with the same ledger resolve identically in the same state. Histories that
  allocate at different configurations record different origins: they can differ in when a
  token is withheld, never in which root holds which token;
- inside the region of its origin the token names, as on a certified component, the unique
  root of its germ. The guard removes the reuse that the branch frame would add between
  regions; it adds no proof of continuation that the intersection contract does not have.

### 6.6 Obligations of GeoCeDG 1.2.2 (D-V2)

Reading by GeoCeDG 1.2.1 is not required. Within 1.2.2 the following are required and
tested, for parameter addresses and for branch-frame selectors:

| Operation | Requirement |
|---|---|
| Durable identity | independent of coordinates, labels and sampling; unchanged by refinement, split, merge, withdrawal, reactivation and promotion of evidence, and by the policy depth |
| Reuse across configurations | a branch-frame token is resolved only under the guard of §6.5, point 5; when the guard fails it is withheld and never given to another root, and it is current again when the guard holds |
| Save and reopen | the address or selector is read back exactly; evidence is recomputed, never read from the document |
| Copy, duplicate | a new identity for the copy; the source locus is remapped by the existing exact copy rules |
| Undo, redo | the address, the identity and the definedness of each step are restored |
| Redefinition | a compatible redefinition keeps the identity under the redefine contract; an incompatible one is refused or replaces the object as that contract already says |
| Selection and continuation | a drag publishes another parameter address of the same point; it never retargets by proximity |
| Degeneracies | a parameter outside the domain, at an ambiguous seam side or on a point component gives a typed cause, never a neighbouring point; a valid parameter inside a band is a defined point without continuation (§6.3, §6.4) |
| Temporary invalidity | the point is undefined with its cause while its evaluation is invalid, and the same point is defined again when the same address evaluates |
| Existing constructions | every document used as regression reads as before; no existing selector, token or address changes text or meaning |

## 7. Evidence lifecycle

1. **Two attributes, never confused.** The producer publishes the evidence atomically, as an
   immutable value with:
   - a **content identity**: `(revision, policy version, contradiction set)`, in canonical
     form. Two evidences with the same content identity have the same cells, kinds and
     classes. It is what a result records and what currentness is decided from;
   - a **publication counter**: an integer of the revision that grows by one at each
     publication. It orders the publications of one session and wakes the dependents. It is
     never an identity, is never compared between two histories, never enters a result as
     its evidence and is never persisted.
2. **Contradiction set.** It is the set of canonical parameters of the revision at which the
   producer has confirmed a contradiction. It is a set: it has no order and no multiplicity.
3. **Determinism.** The content is a function of the traced function, the policy and the
   contradiction set, and of nothing else. A cell is split exactly when the ordinary
   criteria of the policy require it, or when it is larger than the minimum step and
   contains a parameter of the contradiction set; a minimum-step cell that contains one is
   classified with that parameter as an additional node. The partition is the one that this
   rule determines, whatever the order in which the contradictions were found, reported or
   confirmed, and whatever was queried or drawn in between. Two histories that confirm the
   same set have the same content identity; their publication counters may differ.
4. **Contradiction.** A consumer, the renderer or a later evaluation that observes, inside a
   cell, an evaluation that the kind of the cell excludes — an invalid evaluation, another
   selection or a jump above the limits in a span; a valid evaluation in an `ABSENT_BAND` or
   a `FAILURE_BAND` — *reports* the parameter to the producer. The report changes nothing by
   itself. The producer evaluates that parameter with its own evaluator; when the
   contradiction is confirmed it adds the parameter to the contradiction set and publishes
   the content of the new set. A report that is not confirmed, or whose parameter is already
   in the set, changes neither the content nor the counter. The renderer never decides a
   geometric fact and never changes admissibility.
5. **Withdrawal and invalidation.** A publication withdraws exactly the cells that are not
   present, with the same ends, kind and class, in the new content. Publication is an update
   of the producer in the dependency graph. A dependent whose result used a withdrawn cell
   is recomputed by the normal cascade and reads the new content; one that used only cells
   that remain keeps its result. A point keeps its address, its identity and, when its own
   evaluation is valid, its definition (§6.3).
6. **No stale evidence.** A result records the revision and the content identity it read,
   and the cells it used. It is current while every one of those cells is present unchanged
   in the published content of the same revision; otherwise it is recomputed before it is
   served. Evidence of a previous revision is never reused: a new revision discards the
   evidence and the contradiction set of the previous one and cancels work in progress for
   it.
7. **No promotion by density.** No number of nodes, refinements or agreeing evaluations
   changes a span into a certified component. Promotion is only §11.
8. **Work.** The traversal of a revision is charged to the work limit of the policy. Cells
   not reached are `UNEXPLORED`. What a consumer may read is the published content, the same
   whatever has been drawn; a progressive drawing never changes it.

## 8. Operations

Every operation keeps its present behaviour on certified components. This section states
its behaviour where the source has conditioned evidence. Common rules:

- **Propagation by premise.** A result is graded by the facts it used, never by the weakest
  evidence of the locus. A local result that meets its premises is usable although global
  completeness is `NOT_ESTABLISHED`.
- **Integrity under the constructive authority.** `locus-v2-constructive-authority.md` §6,
  §6.1 and §7 apply unchanged, and this contract proposes no widening of them. Conditioned
  evidence is a numerical verification under hypotheses: it is not a kind of relaxed
  admission, it is not a premise that raises an integrity, and `CONDITIONED` never grants
  `RELAXED`. An incidence or a membership decided on a conditioned region is
  `NOT_ESTABLISHED` unless that relation itself has, in the revision, the evidence that the
  authority contract requires: rigorous evidence for `STRICT`, or a positive kind of its §7
  (`SUBSTITUTE_GENERIC`, `ENCLOSURE_COMPATIBLE`) with every one of its preconditions and its
  record for `RELAXED`. A relation with such evidence lists the span hypotheses it used
  among its premises and is bounded by them as §6 of that contract bounds any derivation. A
  consumer that declares a minimum integrity, or that requires a constructive admission,
  keeps failing closed with its existing cause when it is not met; a consumer that declares
  none publishes its result with the integrity shown. Under the legacy authority the
  existing contracts apply.
- **Strict consumers.** A consumer that requires `STRICT`, a certified component or
  `COMPLETE` fails closed with its existing cause.
- **Propagation.** Every result is an ordinary node of the dependency graph, recomputed when
  the revision of its source changes or when a cell it used is withdrawn (§7).
- **Never published.** An ambiguous root, an association by proximity or by index, an
  unproved closure, `EMPTY`, or a set declared complete.

### 8.1 Parametric point — `Point(L, u)`, `Point(L, "branch", u)`

| | |
|---|---|
| Inputs and domain | locus, branch, number; the declared driver domain |
| Preconditions | point validity at `u`; established selection of every selecting dependency at `u`; unambiguous address (§6.3) |
| Evidence required | none on an interval: a point is an isolated position (§15.8) |
| Guarantee | defined; existence `POINT_VALID`, with the cell that contains `u` reported; numeric guarantee of the evaluation |
| Incidence | on the locus by construction of the address. Under the constructive authority its integrity is `NOT_ESTABLISHED` unless the relation has its own evidence (common rule) |
| Identity and continuation | parameter address (§6.1); following needs §8.2 |
| Degeneracies | a valid `u` inside a band or an unexplored cell: defined, without continuation (§6.4). Seam side, domain end, point component: typed cause |
| Persistence and recovery | §6.6 |
| Refusals | `PARAMETER_OUTSIDE_DOMAIN`, `EVALUATION_FAILED`, `SELECTION_NOT_ESTABLISHED`, `POINT_COMPONENT` |

`Point(L, D)` and `Intersect(L, r, D)` by driver correspondence keep the contract of ADR
0040 and of `locus-v2-existence-components.md` §16 unchanged: they need the constructive
authority, a correspondence in the interior of a certified continuous component and the
integrity their contract requires. On a conditioned region they are undefined with their
existing typed cause.

### 8.2 Following, drag and continuation

| | |
|---|---|
| Inputs and domain | a point with a parameter address and a target position |
| Preconditions | start and target parameters in one conditioned region, with no band between them along the route |
| Evidence required | spans of any class along the route |
| Guarantee | continuation along the region, declared conditioned |
| Identity and continuation | the same point; a new parameter address; never a jump to another region by proximity |
| Degeneracies | the gesture stops at a band, with the band kind as cause; a recoverable refusal does not end the gesture. A point that starts inside a band or an unexplored cell stays defined and is not moved by following |
| Refusals | `CONTINUATION_REACHED_BAND`, `CONTINUATION_START_IN_BAND`, `TARGET_IN_ANOTHER_REGION` |

### 8.3 Length between two positions

| | |
|---|---|
| Inputs and domain | two semantic positions of one branch and a route policy |
| Preconditions | both positions in one conditioned region; route inside it. A position inside a band is a valid position with no route |
| Evidence required | spans of any class along the route |
| Guarantee | at most `ESTIMATED`, with H1 to H4 and the hypotheses of the estimator recorded; never a rigorous enclosure |
| Degeneracies | equal positions: the existing same-position policies; a full cycle only on a region that covers the whole period with no band and whose seam nodes agree, declared closed-conditioned |
| Refusals | `ROUTE_CROSSES_BAND` with the band kinds, `POSITION_IN_BAND`, `POSITIONS_IN_DIFFERENT_REGIONS` |

### 8.4 Total length

| | |
|---|---|
| Inputs and domain | a branch or a requested subdomain |
| Preconditions | every cell of the requested domain classified |
| Guarantee | at most `ESTIMATED` when every cell is a span or a band with a computed or bounded contribution (§5.3); otherwise `SUBTOTAL` with its omitted bands |
| Relation to existing modes | H10 and P2 (`locus-v2-metrics.md` §26.5, §26.6) are unchanged and independent; this mode adds the case with bands |
| Refusals | `UNEXPLORED_DOMAIN`, `FAILURE_BAND_IN_DOMAIN` when the consumer has no subtotal |

### 8.5 Intersection of a locus with a target — `Intersect(L, target)`

| | |
|---|---|
| Inputs and domain | locus; a supported target family; the conditioned regions and the certified components of the source |
| Preconditions | the candidate search runs on the semantic evaluator of the source over those regions, never on drawing data |
| Evidence required | per root: its isolating interval inside one span or one certified component; residual within tolerance; local isolation; a sign-change bracket for a transverse root |
| Guarantee | each root `VERIFIED_UNCERTIFIED` with the evidence class of its span; completeness always `NOT_ESTABLISHED` on a source that is not `COMPLETE`; never `EMPTY` |
| Incidence | of the root on the target by the existing residual contract; on the source by its parameter. Under the constructive authority the admission of a root is the unchanged re-qualification of `locus-v2-constructive-authority.md` §7.4: `STRICT`, `RELAXED` by its own positive evidence, or `NOT_ESTABLISHED`; a root that is not admitted is published and is not point-admissible |
| Identity and continuation | §6.5: component selector inside a certified component, branch-frame selector on a span, no rank on a span; the token of a branch-frame selector only under its reuse guard |
| Degeneracies | tangency, coincident roots, overlap: the existing classifications. A root needs evidence on a neighbourhood, which a band does not give: a candidate whose isolating interval meets a band or an unexplored cell is reported unresolved and is not published as a root. This differs from a parametric point, which needs no neighbourhood (§6.3) |
| Persistence and recovery | the command and the ledger, with the entry kind of §6.5 |
| Refusals | `SOURCE_NOT_EVALUABLE`; a root whose selector is not unique, or whose reuse guard fails, keeps a revision-local handle and is not point-admissible |

### 8.6 Intersection of two loci — `Intersect(L1, L2)`

The same contract with the pair of sources: component pairs become pairs of regions or
components, each root needs its evidence on both sources, and the pair selector follows
§6.5, point 6. Completeness is `NOT_ESTABLISHED` whenever either source is not `COMPLETE`.

### 8.7 Transformations — `Translate`, `Rotate`, `Mirror`, `Dilate`

The transformed locus is, as today, the source evaluator composed with the similarity:
`L'(u) = T(L(u))`. It has its own identity and keeps the canonical parameter, the declared
domain, the branches and the orientation of the source.

| | |
|---|---|
| Inputs and domain | a locus and the parameters of a similarity |
| Preconditions | the source is defined; the parameters are defined |
| Evidence | the image *inherits* the conditioned evidence of the source cell by cell, as it already inherits the existence structure, and states it against the policy in its own world. H1 to H3 are invariant under a similarity; H4 is not: the rule «Resolution under a similarity» below says how its bound transforms and which cells are classified anew |
| Guarantee | that of the cell of the image, for every consumer of this section |
| Incidence | a point of the image at parameter `u` is the image of the source point at `u` |
| Identity and continuation | own identity; a parameter address on the image names the same parameter. A transformed query creates its own selectors and tokens, as `locus-v2-similarity-transformations.md` §8 says; none is copied from the source query |
| Propagation | recomputed when the source revision, a source cell in use or a parameter changes |
| Ratio zero | the approved `COLLAPSED_IMAGE` contract, unchanged (below) |
| Persistence | the command and its inputs, as today |
| Refusals | an undefined source or parameter gives an undefined image, as today |

**Resolution under a similarity.** Let `ρ` be the absolute ratio of the similarity: 1 for a
translation, a rotation and a reflection, `abs(k)` for a dilation of factor `k`.

1. *How the quantities of a cell transform.* A similarity multiplies every length by `ρ`,
   preserves angles and does not act on the parameter. Hence, exactly: the ends of a cell,
   the minimum step and every parameter interval are unchanged; the turning between nodes is
   unchanged; the chord deviation between nodes, and every other length that a criterion of
   the policy measures, is multiplied by `ρ`; and the bound of H4 that the source states,
   the chord-deviation limit `δ` of the policy in the world of the source, is `ρ·δ` in the
   world of the image. H1 to H3, the testimony, the margin and the evidence class are facts
   of the source slice, upstream of the similarity, and are unchanged.
2. *The policy of the image.* The limits of the policy are world quantities of the locus
   that publishes the evidence (§3.4). The image publishes its evidence against the same
   policy version with the limits in its own world: H4 of a span of the image is stated
   with `δ`, and is never stated with `ρ·δ` when that is larger.
3. *When a cell is classified anew.*
   - `ρ ≤ 1` — every isometry, every reduction and ratio zero: `ρ·δ ≤ δ`, so every span of
     the source meets the policy of the image. The cells are inherited unchanged, with the
     same ends, kinds and classes. Nothing is merged or coarsened.
   - `ρ > 1`: `ρ·δ > δ`. The bound of the source no longer meets the policy of the image
     and is not kept. Every inherited span is checked again in the world of the image, with
     the lengths at its nodes multiplied by `ρ`. A span that passes is a span of the image.
     A span that fails is classified anew by the ordinary rule of §7, point 3, applied to
     the image function inside that cell: it is subdivided until the checks pass, or to the
     minimum step, where it is a `DISCONTINUITY_BAND`. The new nodes are evaluated through
     the source evaluator. The work is charged to the work limit of the image, and a cell
     that it does not reach is `UNEXPLORED`, never a span.
   - Whatever the ratio, a band or an unexplored cell of the source is inherited with its
     kind. A ratio never turns it into a span: a jump that falls below the limit at the
     scale of the image is not evidence of continuity.
   - The image of an image applies this rule to its immediate source.
4. *No silent tolerance.* The image never publishes a span whose chord deviation at its
   nodes, in its own world, exceeds the limit of the policy, and never states to a consumer
   a resolution in the units of the source. Where its cells differ from those of the source,
   the difference is part of its published content and of the state of §9, point 3.
5. *Identity and lifecycle.* The evidence of the image has its own content identity:
   `(revision of the image, policy version, content identity of the source evidence it
   read, contradiction set of the image)`. The revision of the image identifies its traced
   function, the similarity included, so the content is a function of the current state and
   never of the order in which the ratio took its values. A contradiction of H1, H2 or H3
   observed on the image is a contradiction of the source cell and is reported to the
   producer of the source; a contradiction of a length criterion in the world of the image
   is reported to the producer of the image (§7, point 4).
6. *Consumers.* Every consumer of the image reads the cells of the image. Its regions are
   contained in those of the source: the image admits no following, route or root that the
   source refuses, and may refuse one that the source admits where a cell was classified
   anew. The tolerances that the existing contracts compute on the image (root residual,
   isolation, numeric guarantee) are computed on the image, as today. The `abs(k)`
   covariance of lengths of `locus-v2-similarity-transformations.md` §7 is unchanged; it
   relates lengths, and does not require the estimates of the image and of the source to
   come from the same cells.

**Finite ratio zero.** `locus-v2-similarity-transformations.md` §5.4, §6, §7 and §8,
`locus-v2-point-interaction.md` §11 and the collapsed-image clauses of
`locus-v2-metrics.md` govern, on a conditioned source exactly as on a certified one:

- the image is a valid semantic Locus V2 with the image property `COLLAPSED_IMAGE`. It
  preserves the source domains with their `FINITE` or `UNBOUNDED` classification, the
  branches, the semantic addresses and the invalid gaps; every source-valid address maps to
  the centre and every source-invalid address stays invalid;
- the conditioned evidence is inherited as for any other ratio not above 1: spans stay
  spans, bands stay bands, and no cell is classified anew because the image collapsed;
- an existing point keeps its selected address and its identity, evaluates at the centre and
  recovers its position when the ratio is nonzero again; a new inverse query on the image
  cannot choose one address and returns the existing degenerate, multiple result;
- total and partial lengths are the zero values that the metric contract assigns to a
  collapsed image, with the class it assigns. They follow from the image property, not from
  the evidence; what remains conditioned is the existence of the positions and of the route;
- a target that passes through the collapsed point is the existing overlap, non-isolated
  case: no isolated root is fabricated and no bijective covariance is claimed.

The image is never computed from a tessellation of the source.

### 8.8 Dependent and nested loci

| Case | Contract |
|---|---|
| A locus whose traced point is built from a point, a root or a metric of a conditioned source | it computes its own conditioned evidence on its own function and records, per span, the source hypotheses it inherits; its existence structure is `NOT_ESTABLISHED` unless it has its own certificate |
| A locus driven by a point that moves on a conditioned locus (`LOCUS_BRANCH_POINT`) | its driver domain is the declared domain of the support; the conditioned regions of the support are the pieces on which its driver is defined; bands of the support are bands of the dependent |
| A nested locus read as a function by a consumer | the consumer reads the nested evidence of the nested revision; nothing is shared between copies |

### 8.9 Selection, presentation and manipulation

A conditioned locus is selected, hit-tested, styled, shown, hidden, labelled, layered, copied
and deleted as any Locus V2. Hit-testing uses the drawn strokes; it never creates an address:
a point created by a click on the curve is created through the existing inverse query and
then addressed by parameter.

### 8.10 Export

§10.

## 9. Presentation

1. **Style.** The colour, thickness, line type and every other visual attribute of a locus
   remain the user's (`locus-v2-presentation.md`, style contract). No style is imposed on a
   conditioned region and none identifies it.
2. **Stroke.** A conditioned region is drawn as one continuous stroke with the style of the
   locus, tessellated per view from the evaluator inside the spans of the published
   content. Bands are not joined: the rule that no render segment crosses an unresolved
   band is unchanged. A region is drawn closed only as §8.3 allows.
3. **State in the properties.** The properties of the object always state the grade in
   ordinary words, localized, for example «Conditioned — existence/continuity not
   certified», and list on request the hypotheses H1 to H4, the policy version and the
   number of regions and bands with their kinds. Results derived from a conditioned region
   state it the same way. No developer diagnostic is needed to read them.
4. **System mark** (author decision of 2026-11-21). A hollow rhombus of 7 pixels, drawn by
   the system, identifies each visible conditioned region in the view. It is an overlay in
   screen pixels of fixed size, outlined with the colour of the object, with no hit area; it
   is independent of the line style, thickness and zoom; it is not serialized and is neither
   geometry, parametrization, identity, length nor evidence. It is not an arrow and cannot be
   read as an orientation. It never denotes certification or completeness.
5. **Fallback.** If the mark proves unsound to build, point 3 alone satisfies this section.
6. A drawing that looks closed is never called complete.

## 10. Export

1. Picture exports of a view export what the view draws, including the system mark where
   the exporter can draw it.
2. Semantic exports (DXF, semantic LaTeX and the others of the export specifications) omit
   conditioned regions in GeoCeDG 1.2.2. Each omission is reported with the diagnostic
   `EXISTENCE_NOT_ESTABLISHED` and the list of regions and bands, as the adapter already
   reports a source without components. Certified components are exported as today.
3. Semantic export of conditioned regions is future work, registered in the roadmap as a
   requirement of the future line on B-splines and other derived representations, DXF import
   and extended DXF export, with preservation of quality, provenance and tolerances. No
   subphase is opened for it. In that line DXF import stays read-only, and a spline, a
   polyline or another derived representation of interchange never silently replaces the
   authoritative CeDG object.
4. Exploratory evidence stays never exported.

## 11. Promotion by proof

Conditioned evidence proposes; the kernel proves. A region is promoted to a certified
component only by a certificate under `locus-existence/hybrid/v1` or a successor. Promotion
changes no identity of the locus, no function and no parameter address. A root selector is
bound to its frame (§6.5, point 5): a promoter that is to keep the tokens of the roots of a
promoted region must define that correspondence in its own contract, by structural evidence
and never by proximity; none is defined here. Two bounded promoters are identified, each a
separate later capability with its own approval:

- **K2, composition through a certified reparametrization**: for `P(θ) = L(τ(θ))` with `L`
  certified and `τ` an enclosed scalar program, a certificate of the image and, separately,
  of the position length `Λ_L(τ(θ))`. The travelled length, which involves `|τ′|`, is another
  functional.
- **K1, certified root family**: an enclosed family of the root parameter over a cell. A
  fold needs `h = 0` and `h_u = 0` together.

No promotion is part of `conditioned-use/v4`.

## 12. Amendments that adoption would require

| Specification | Amendment |
|---|---|
| `locus-v2-existence-components.md` §15.1, §15.8 | name the conditioned evidence as the producer of `NUMERICAL_UNDER_HYPOTHESES`; state the consumer admissions of §8 beside the existing exception |
| `locus-v2-public-surface.md` | parameter address codec and its resolution; result states of intersections on a conditioned source; ledger format version with the branch-frame entry kind and its origin |
| `locus-v2-point-interaction.md` | continuation along a conditioned region; a valid point inside a band has no continuation |
| `locus-v2-metrics.md` | conditioned between-position and total lengths; relation to §26.5 and §26.6 |
| `locus-v2-intersections.md`, `locus-v2-extended-intersections.md` | local roots on spans beside the rule that a locally verified root needs a certified component; the branch-frame selector kind, the origin of its binding and its reuse guard beside §6.2, whose text for certified components does not change |
| `locus-v2-similarity-transformations.md` | inheritance of conditioned evidence by the image, with the rule of resolution under a similarity; no change of the `COLLAPSED_IMAGE` clauses nor of the metric and intersection covariance |
| `locus-v2-presentation.md` | state in the properties, system mark, closure wording |
| export specifications | omission with diagnostic (§10) |
| `locus-v2-constructive-authority.md` | none: integrity and relaxed admission do not change |
| `locus-v2-semantics.md` | none: the snapshot does not change |

## 13. Differences from revision 1 (`ab37fb5ff`)

1. §1: activation by default, no switch.
2. §3.3: the classes state what is proved, what is checked at nodes, what stays a hypothesis
   and who may consume them. `MARGIN_BOUNDED` no longer withdraws H1 or H2.
3. §3.1: a failure band is not subdivided inside; `ABSENT_BAND` needs the cause at every
   node.
4. §6: the clause on earlier readers is replaced by the obligations of 1.2.2.
5. §7: canonical contradiction set, producer re-evaluation, withdrawal, invalidation,
   identity of affected points, no stale evidence, no promotion by density.
6. §8: one contract per operation, with pair intersections, transformations, nested loci and
   manipulation added.
7. §9: the mandatory stroke style is removed; state in the properties and a system mark.
8. §10: semantic export excluded from 1.2.2, with its future dependency.

## 14. Differences from revision 2 (`83cd56c4c`) and their validation

Focal amendment requested by the author on 2026-11-21. Nothing else changes.

| # | Point | Revision 2 | Revision 3 | Where |
|---|---|---|---|---|
| 1 | `RELAXED` integrity | an incidence on a conditioned span was «at most `RELAXED`» | `CONDITIONED` never grants `RELAXED`; the positive requirements of the authority contract are unchanged and no widening is proposed; without the relation's own evidence the integrity is `NOT_ESTABLISHED` | §2, §8 common rule, §8.1, §8.5 |
| 2 | Identity of intersection roots | «tokens by the existing deterministic selection», which needs a component lineage that a region does not have | a branch-frame selector of its own kind, without rank, never built from the partition; certified selectors unchanged | §1, §6.5, §8.5, §8.6, §11, §12 |
| 3 | Generations | one «generation» was both counter and identity, and depended on the order of confirmation | content identity (function of the contradiction set, a set of parameters) and publication counter are separate; staleness is decided by cells | §2, §7 |
| 4 | Points in bands | a parameter in a band gave a typed cause | point validity and admissibility of continuation are separate; a valid point in a band is defined, without continuation | §2, §6.3, §6.4, §6.6, §8.1 to §8.3, §8.5 |
| 5 | Ratio zero | «the image is one point; every span is reported collapsed» | the approved `COLLAPSED_IMAGE` contract unchanged: domains, gaps, addresses, identities and zero metrics | §8.7 |
| 6 | `MARGIN_BOUNDED` | inferred that the selected root is not exchanged | proves only the pairwise separation of the enclosed roots; H2 stays whole | §3.3 |
| — | Driver correspondence | resolved as a parametric point on a region | unchanged contract: needs a certified component | §8.1 |
| — | System mark | glyph proposed | hollow rhombus of 7 px, approved by the author | §9.4 |

Validation criteria. Each is a discriminating test of the increment named, in addition to
the bench of the design document.

| # | Criterion | Increment |
|---|---|---|
| 1a | under the constructive authority, a parametric point and a root on a span without evidence of their own have integrity or admission `NOT_ESTABLISHED`; the root is published and its token point is undefined with the existing cause | I1, I4 |
| 1b | the same relation with a positive kind of the authority contract is `RELAXED`, with its record; every existing test of the authority contract passes unchanged; no production path builds `RELAXED` from conditioned evidence | I4 |
| 2a | two roots of opposite germ on one region receive two tokens; with the same ledger, undo and redo and save and reopen give the same bindings; a direct and an incremental history differ at most as criterion 7d says | I4 |
| 2b | the same construction traversed with another policy depth, and after a new publication, gives the same selectors, and each root holds the token of its selector or none (criterion 7c) | I4 |
| 2c | two roots of the same germ on one branch receive no token and keep revision-local handles; a root that passes between a certified component and a span does not carry its token, and recovers it when the state returns | I4 |
| 2d | the selector and token texts of every root on a certified component are byte-identical to those of the base; every existing ledger document reads as before | I4 |
| 3a | every permutation of three or more confirmed contradictions, interleaved with queries and drawing, gives the same content identity and the same cells; the counters may differ; a duplicate or unconfirmed report changes nothing | I0 |
| 3b | a dependent that used a withdrawn cell is recomputed and one that did not keeps its result; after a new revision no result of the previous one is served | I0 |
| 4a | a point at a valid parameter inside each kind of band is defined, reports the band, keeps its address through publications and is refused following and routes with the typed cause; an explicit redefinition moves it | I1, I3 |
| 4b | a valid evaluation inside an `ABSENT_BAND` or a `FAILURE_BAND` is reported, confirmed and reclassified | I0 |
| 5 | with ratio zero on a conditioned source: valid image, domains, gaps and cells equal to the source, existing point with its address that recovers at a nonzero ratio, degenerate inverse query, zero lengths with the class of the metric contract, a target through the centre as overlap; the existing `COLLAPSED_IMAGE` tests pass unchanged | I4T |
| 6 | adverse case: the enclosed roots stay separated on a cell while the selection of the dependency changes inside it; the span is `MARGIN_BOUNDED`, records H2 and gives no consumer more than `RESOLUTION_ONLY`; no consumer branches on the class | I0 |

## 15. Differences from revision 3 (`57c495cce`) and their validation

Two focal precisions requested by the author on 2026-11-21, after confirming the six points
of §14. Nothing else changes; criteria 2a and 2b of §14 are restated to agree with
precision 7.

| # | Precision | Revision 3 | Revision 4 | Where |
|---|---|---|---|---|
| 7 | Identity across configurations | a branch-frame selector that was unique in the current state resolved its ledger token, so a root that appeared in another region of the branch with the same germ received the token of a root that had disappeared | one binding per selector, with a fixed origin; the token is resolved only when the origin and the root lie in one conditioned region; otherwise it is withheld, never given to another root, and no evidence chooses a token | §1, §6.5 points 4 to 9 and consequences, §6.6, §8.5, §12 |
| 8 | Resolution under similarities | the image inherited every cell and «the bound multiplied by the ratio», so for a ratio above 1 a span could be published with a deviation above the limit of the policy | the quantities of a cell transform as stated; the image states H4 with the limit of the policy in its own world; for a ratio above 1 every span is checked again and classified anew where it fails; a band is never turned into a span; the image evidence has its own content identity | §8.7, §12 |

Validation criteria. Each is a discriminating test of the increment named: the rule of
revision 3 fails it. Cases C17 and C18 of the design bench run them on evaluators of known
truth (`tools/benchmark/post-r1-locus/conditioned-use-precisions-probe.js`), with both
rules side by side.

| # | Criterion | Increment |
|---|---|---|
| 7a | **adverse case, disappearance and appearance in separate regions.** One branch with two regions A and B separated by a band; a target that has one root on A, of germ `g`. The token `T` is allocated and a point depends on it. The target changes so that A has no root and B has one root of germ `g`, in one update and, separately, through a state without roots. The selector text of the root on B is equal to that of the root on A. Required: `T` is not current; the root on B has a revision-local handle and `NOT_ESTABLISHED`; the point is undefined with its existing cause and is never at the root on B; the ledger keeps one entry with its origin and no token is allocated. When the target returns to A the point is defined at the root on A with `DETERMINISTIC_SELECTION_ESTABLISHED` | I4 |
| 7b | positive control: the root moves inside the region of the origin; `T` stays current at every step, and the spans between the origin and the root are recorded among the cells that the result used | I4 |
| 7c | the evidence never chooses: with other policy depths, with every order of confirmation of contradictions, and with a band confirmed between the origin and the root, each root holds `T` or no token; the band withholds `T` and the dependents are recomputed through §7, point 5; no second entry of the selector exists in any history | I4 |
| 7d | histories: undo and redo, and save and reopen in every state of 7a, restore the entry (selector, origin, token) and the definedness of the point exactly. A document built directly in the second state allocates a token with its origin on B, which is withheld on A. In no history does a root hold a token allocated to a root of another region | I4 |
| 7e | pairs: in an intersection of two conditioned loci the guard is required on each branch-frame side; failing it on one side withholds the token | I4 |
| 8a | for a translation, a rotation, a reflection, a ratio below 1 and ratio zero, the cells of the image are those of the source, with the same ends, kinds and classes; a `DISCONTINUITY_BAND` of the source whose jump is below the limit at the scale of the image is a band of the image | I4T |
| 8b | **adverse case, magnification.** Source with spans whose chord deviation at the nodes is `d`, limit `δ`. For a ratio `ρ` with `ρ·d ≤ δ` the cells are inherited unchanged; for `ρ·d > δ` those cells are subdivided and no published span of the image has a deviation above `δ` in its own world. On the unit circle with the limits of the bench: 128 spans of deviation 3,01·10⁻⁴; ratio 3 inherits them (9,04·10⁻⁴); ratio 4 publishes 256 spans of 3,01·10⁻⁴, where copying the cells would publish 128 spans of 1,20·10⁻³ | I4T |
| 8c | minimum step: where the checks still fail at the minimum step the cell of the image is a `DISCONTINUITY_BAND`, and where the work limit ends it is `UNEXPLORED`; following and lengths across it are refused on the image with the kind of the cell while the source admits them; a point at a valid parameter inside it is defined (§6.3) | I4T |
| 8d | the content of the image is a function of the state: a ratio that goes from 4 to 1/2 and back to 4 gives the content identity of the direct ratio 4, with free publication counters; an image of an image decides against its immediate source and never coarsens | I4T |
| 8e | a contradiction of H1 to H3 found at a node that the image added is reported to the producer of the source, appears in the content of the source and is inherited by the image; the content identity of the image records that of the source | I4T |
| 8f | the existing tests of the similarity contract, the `COLLAPSED_IMAGE` tests of criterion 5 and the `abs(k)` covariance of lengths on certified sources pass unchanged | I4T |

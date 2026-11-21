# Locus V2 conditioned use under declared hypotheses — candidate contract

| | |
|---|---|
| Status | **CANDIDATE, REVISION 2 — PROPOSED, PENDING AUTHOR REVIEW. NOT NORMATIVE.** `selfApproved=false` |
| Version | `conditioned-use/v2-candidate` (revision 1 is the text of commit `ab37fb5ff`) |
| Decision | [ADR 0042](../../../../docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md), proposed, with the author decisions D-V1 to D-V7 of 2026-11-21 |
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
ADR 0040, relation integrity, intersection tokens and selectors and the semantic snapshot
are unchanged. A certified component keeps every contract and guarantee it has today, and a
producer that can be certified is certified as before.

## 2. Independent axes

| Axis | Values that this contract reads or publishes |
|---|---|
| Reconstruction fidelity | `FAITHFUL` required; otherwise nothing of this contract applies |
| Point validity | status of one evaluation at one parameter |
| Existence and continuity under hypotheses | conditioned evidence (§3), with its class |
| Existence and continuity certified | `LOCAL_CERTIFIED` components, unchanged |
| Global completeness | `COMPLETE` only by certificate; otherwise `NOT_ESTABLISHED` |
| Numeric guarantee | the existing classes of a value, unchanged |
| Incidence and membership integrity | `relation-integrity/v1` under the constructive authority; the existing contracts under the legacy one |
| Identity and continuation of selection | per selecting dependency: established deterministic selection, or not established |
| Coverage | which part of the requested domain each fact covers |
| Currentness | revision of the definition; generation of the evidence |

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
lineage, no ordinal identity and no persisted form.

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
| `MARGIN_ESTIMATED` | nothing | the same, and a separation of the selected root above the policy margin | H1, H2, H3, H4. The separation observed at nodes is not a bound between them, so H2 is not reduced | the same as `RESOLUTION_ONLY`; the class is evidence for diagnosis and for choosing proof cells, not a stronger permission |
| `MARGIN_BOUNDED` | for the dependencies that carry the bound: the roots enclosed by the bound do not collide on the cell, so the selected root is not exchanged with another of *those* roots | the same as `MARGIN_ESTIMATED` | H1 (validity of every operation); H2 for every dependency without the bound, and for the creation or loss of roots that the bound does not enclose; H3 (continuity of the composed function); H4 | the same as `RESOLUTION_ONLY`. It is recorded so that a partial proof is not discarded and can be completed by a promoter (§11) |

No class of this table certifies existence or continuity of the branch. A cell on which H1
to H3 are all proved is not a conditioned span: it is a certified component, produced by the
certifier.

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

1. **Parameter address.** A point at a parameter that no certified component contains is
   addressed by `(source locus identity, branch key, provider contract, canonical parameter,
   periodic lift, seam side)`, without a component selector. Its codec is
   `geocedg-locus-parameter-address/v1`, distinct from `geocedg-locus-address/v1` and
   `geocedg-locus-principal-selection/v1`, whose texts and meanings do not change. A sample,
   a polyline index or a screen position is never an address.
2. **No reinterpretation and no invented migration.** An address with a component selector
   resolves only as §15.6 says. It is never rewritten into a parameter address, and a
   parameter address never acquires a component selector silently. Converting one into the
   other is an explicit redefinition of the point. No migration creates an identity or a
   spatial association.
3. **Resolution per revision.** A parameter address resolves when the evaluation at its
   parameter is valid and every selecting dependency has an established selection there. The
   evidence that contains the parameter is reported with the point; it does not decide
   whether the point is defined.
4. **Obligations of GeoCeDG 1.2.2** (D-V2). Reading by GeoCeDG 1.2.1 is not required. Within
   1.2.2 the following are required and tested:

| Operation | Requirement |
|---|---|
| Durable identity | independent of coordinates, labels and sampling; unchanged by refinement, split, merge, withdrawal, reactivation and promotion of evidence |
| Save and reopen | the address is read back exactly; evidence is recomputed, never read from the document |
| Copy, duplicate | a new identity for the copy; the source locus is remapped by the existing exact copy rules |
| Undo, redo | the address, the identity and the definedness of each step are restored |
| Redefinition | a compatible redefinition keeps the identity under the redefine contract; an incompatible one is refused or replaces the object as that contract already says |
| Selection and continuation | a drag publishes another parameter address of the same point; it never retargets by proximity |
| Degeneracies | a parameter in a band, at a seam side, at a domain end or in a point component gives a typed cause, never a neighbouring point |
| Temporary invalidity | the point is undefined with its cause while its evaluation is invalid, and the same point is defined again when the same address evaluates |
| Existing constructions | every document used as regression reads as before; no existing selector changes meaning |

## 7. Evidence lifecycle

1. **Identity.** The producer publishes the evidence atomically, as an immutable value
   identified by `(revision, policy version, generation)`.
2. **Determinism.** The content of a generation is a function of the traced function, the
   policy and the *contradiction set* of the revision: the set of dyadic cells in which a
   contradiction has been confirmed. It does not depend on the order of the queries, of the
   reports or of the drawing. Generation `g` is the evidence computed with the first `g`
   confirmed cells taken in canonical order of their dyadic address; two histories that
   confirm the same cells reach the same evidence.
3. **Contradiction.** A consumer, the renderer or a later evaluation that observes, inside a
   span, an invalid evaluation, another selection or a jump above the limits *reports* the
   parameter to the producer. The report changes nothing by itself. The producer evaluates
   that parameter with its own evaluator; when the contradiction is confirmed it adds the
   cell to the contradiction set, refines that cell by the ordinary traversal and publishes
   the next generation. The renderer never decides a geometric fact and never changes
   admissibility.
4. **Withdrawal and invalidation.** A new generation withdraws exactly the spans that its
   refinement replaced. Publication is an update of the producer in the dependency graph: a
   dependent whose result used a withdrawn cell is recomputed by the normal cascade and
   reads the new generation; one that used only other cells keeps its result. A point keeps
   its address and identity: it stays defined when its own evaluation is still valid, with
   the new evidence reported, and is undefined with a typed cause otherwise.
5. **No stale evidence.** A result records the revision and generation it read. Evidence of
   a previous revision, or of a generation that withdrew the cells in use, is never reused. A
   new revision discards the evidence and the contradiction set of the previous one and
   cancels work in progress for it.
6. **No promotion by density.** No number of nodes, refinements or agreeing evaluations
   changes a span into a certified component. Promotion is only §11.
7. **Work.** The traversal of a revision is charged to the work limit of the policy. Cells
   not reached are `UNEXPLORED`. What a consumer may read is the published generation, the
   same whatever has been drawn; a progressive drawing never changes it.

## 8. Operations

Every operation keeps its present behaviour on certified components. This section states
its behaviour where the source has conditioned evidence. Common rules:

- **Propagation by premise.** A result is graded by the facts it used, never by the weakest
  evidence of the locus. A local result that meets its premises is usable although global
  completeness is `NOT_ESTABLISHED`.
- **Authority.** Under the constructive authority an incidence or membership decided on a
  conditioned span is at most `RELAXED` and lists the span hypotheses among its premises;
  under the legacy authority the existing contracts apply. A consumer that requires
  `STRICT`, a certified component or `COMPLETE` fails closed with its existing cause.
- **Propagation.** Every result is an ordinary node of the dependency graph, recomputed when
  the revision or the relevant evidence generation of its source changes (§7).
- **Never published.** An ambiguous root, an association by proximity or by index, an
  unproved closure, `EMPTY`, or a set declared complete.

### 8.1 Parametric point — `Point(L, u)`, `Point(L, "branch", u)`

| | |
|---|---|
| Inputs and domain | locus, branch, number; the declared driver domain |
| Preconditions | point validity at `u`; established selection of every selecting dependency at `u`; unambiguous address |
| Evidence required | none on an interval: a point is an isolated position (§15.8) |
| Guarantee | defined; existence `POINT_VALID`, with the evidence that contains `u` reported; numeric guarantee of the evaluation |
| Incidence | on the locus by construction of the address; integrity at most `RELAXED` under the constructive authority |
| Identity and continuation | parameter address (§6); following needs §8.2 |
| Degeneracies | band, seam side, domain end, point component: typed cause |
| Persistence and recovery | §6.4 |
| Refusals | `PARAMETER_OUTSIDE_DOMAIN`, `EVALUATION_FAILED`, `SELECTION_NOT_ESTABLISHED`, `POINT_COMPONENT` |

`Point(L, D)` and `Intersect(L, r, D)` by driver correspondence keep the contract of ADR
0040: they need the constructive authority and a membership that transfers from a certified
component. On a conditioned region they resolve as a parametric point at the parameter of
`D` only when the correspondence itself is established, with integrity at most `RELAXED`.

### 8.2 Following, drag and continuation

| | |
|---|---|
| Inputs and domain | a point with a parameter address and a target position |
| Preconditions | start and target parameters in one conditioned region, with no band between them along the route |
| Evidence required | spans of any class along the route |
| Guarantee | continuation along the region, declared conditioned |
| Identity and continuation | the same point; a new parameter address; never a jump to another region by proximity |
| Degeneracies | the gesture stops at a band, with the band kind as cause; a recoverable refusal does not end the gesture |
| Refusals | `CONTINUATION_REACHED_BAND`, `TARGET_IN_ANOTHER_REGION` |

### 8.3 Length between two positions

| | |
|---|---|
| Inputs and domain | two semantic positions of one branch and a route policy |
| Preconditions | both positions in one conditioned region; route inside it |
| Evidence required | spans of any class along the route |
| Guarantee | at most `ESTIMATED`, with H1 to H4 and the hypotheses of the estimator recorded; never a rigorous enclosure |
| Degeneracies | equal positions: the existing same-position policies; a full cycle only on a region that covers the whole period with no band and whose seam nodes agree, declared closed-conditioned |
| Refusals | `ROUTE_CROSSES_BAND` with the band kinds, `POSITIONS_IN_DIFFERENT_REGIONS` |

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
| Evidence required | per root: membership of its parameter in a span or a certified component; residual within tolerance; local isolation; established deterministic selection |
| Guarantee | each root `VERIFIED_UNCERTIFIED` with the evidence class of its span; completeness always `NOT_ESTABLISHED` on a source that is not `COMPLETE`; never `EMPTY` |
| Incidence | of the root on the target by the existing residual contract; on the source by its parameter |
| Identity and continuation | tokens by the existing deterministic selection; the ledger and its format are unchanged; a token point may be created from an admissible root |
| Degeneracies | tangency, coincident roots, overlap: the existing classifications; a root in a band is not published |
| Persistence and recovery | as today: the command and the ledger |
| Refusals | `SOURCE_NOT_EVALUABLE`; a root whose selection is not established keeps a revision-local handle and is not point-admissible |

### 8.6 Intersection of two loci — `Intersect(L1, L2)`

The same contract with the pair of sources: component pairs become pairs of regions or
components, each root needs membership on both sources, and the pair selector and ledger are
unchanged. Completeness is `NOT_ESTABLISHED` whenever either source is not `COMPLETE`.

### 8.7 Transformations — `Translate`, `Rotate`, `Mirror`, `Dilate`

The transformed locus is, as today, the source evaluator composed with the similarity:
`L'(u) = T(L(u))`. It has its own identity and keeps the canonical parameter, the declared
domain, the branches and the orientation of the source.

| | |
|---|---|
| Inputs and domain | a locus and the parameters of a similarity |
| Preconditions | the source is defined; the parameters are defined |
| Evidence | the image *inherits* the conditioned evidence of the source cell by cell, with the same kinds, classes, hypotheses and generation, as it already inherits the existence structure. H1 to H3 are invariant under a similarity; the resolution bound of H4 is the source bound multiplied by the ratio of the similarity |
| Guarantee | that of the source region, for every consumer of this section |
| Incidence | a point of the image at parameter `u` is the image of the source point at `u` |
| Identity and continuation | own identity; a parameter address on the image names the same parameter |
| Propagation | recomputed when the source revision or generation, or a parameter, changes |
| Degeneracies | ratio zero: the image is one point; every span is reported collapsed and no length or intersection treats it as an arc |
| Persistence | the command and its inputs, as today |
| Refusals | an undefined source or parameter gives an undefined image, as today |

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
   generation. Bands are not joined: the rule that no render segment crosses an unresolved
   band is unchanged. A region is drawn closed only as §8.3 allows.
3. **State in the properties.** The properties of the object state the grade in ordinary
   words, localized, for example «Conditioned — existence/continuity not certified», and
   list on request the hypotheses H1 to H4, the policy version, the number of regions and
   bands with their kinds, and the generation. Results derived from a conditioned region
   state it the same way. No developer diagnostic is needed to read them.
4. **System mark.** A discrete mark drawn by the system identifies each conditioned region
   in the view. It is an overlay in screen pixels of fixed size, outlined with the colour of
   the object, with no hit area; it is independent of the line style, thickness and zoom; it
   is not serialized and is neither geometry, parametrization, identity, length nor
   evidence. Its glyph is not an arrow and cannot be read as an orientation. It never
   denotes certification or completeness.
5. **Fallback.** If the mark proves unsound to build, point 3 alone satisfies this section.
6. A drawing that looks closed is never called complete.

## 10. Export

1. Picture exports of a view export what the view draws, including the system mark where
   the exporter can draw it.
2. Semantic exports (DXF, semantic LaTeX and the others of the export specifications) omit
   conditioned regions in GeoCeDG 1.2.2. Each omission is reported with the diagnostic
   `EXISTENCE_NOT_ESTABLISHED` and the list of regions and bands, as the adapter already
   reports a source without components. Certified components are exported as today.
3. Semantic export of conditioned regions is future work. It depends on the line on
   B-splines and other derived representations, DXF import and extended DXF export, with
   preservation of quality, provenance and tolerances. In that line DXF import stays
   read-only, and a spline, a polyline or another derived representation of interchange
   never silently replaces the authoritative CeDG object.
4. Exploratory evidence stays never exported.

## 11. Promotion by proof

Conditioned evidence proposes; the kernel proves. A region is promoted to a certified
component only by a certificate under `locus-existence/hybrid/v1` or a successor. Promotion
changes no identity and no function. Two bounded promoters are identified, each a separate
later capability with its own approval:

- **K2, composition through a certified reparametrization**: for `P(θ) = L(τ(θ))` with `L`
  certified and `τ` an enclosed scalar program, a certificate of the image and, separately,
  of the position length `Λ_L(τ(θ))`. The travelled length, which involves `|τ′|`, is another
  functional.
- **K1, certified root family**: an enclosed family of the root parameter over a cell. A
  fold needs `h = 0` and `h_u = 0` together.

No promotion is part of `conditioned-use/v2`.

## 12. Amendments that adoption would require

| Specification | Amendment |
|---|---|
| `locus-v2-existence-components.md` §15.1, §15.8 | name the conditioned evidence as the producer of `NUMERICAL_UNDER_HYPOTHESES`; state the consumer admissions of §8 beside the existing exception |
| `locus-v2-public-surface.md` | parameter address codec and its resolution; result states of intersections on a conditioned source |
| `locus-v2-point-interaction.md` | continuation along a conditioned region |
| `locus-v2-metrics.md` | conditioned between-position and total lengths; relation to §26.5 and §26.6 |
| `locus-v2-intersections.md`, `locus-v2-extended-intersections.md` | local roots on conditioned regions, for a target and for a pair |
| `locus-v2-similarity-transformations.md` | inheritance of conditioned evidence by the image |
| `locus-v2-presentation.md` | state in the properties, system mark, closure wording |
| export specifications | omission with diagnostic (§10) |
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

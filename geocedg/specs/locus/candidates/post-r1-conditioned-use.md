# Locus V2 conditioned use under declared hypotheses — candidate contract

| | |
|---|---|
| Status | **CANDIDATE — PROPOSED, PENDING AUTHOR REVIEW. NOT NORMATIVE.** `selfApproved=false` |
| Version | `conditioned-use/v1-candidate` |
| Decision | [ADR 0042](../../../../docs/adr/0042-locus-v2-conditioned-use-under-declared-hypotheses.md), proposed |
| Design | [architecture and source analysis](../../../../docs/architecture/post_r1_locus_conditioned_use_design.md) |
| Layer | Shared Java kernel (evidence, addresses, consumers); derived rendering; export adapters |

This file is a candidate contract. It becomes normative only by an express author decision,
through amendments of the specifications named in §12. Until then every sentence below reads
as «the proposal is that».

## 1. Scope and non-scope

It applies to a Locus V2 produced from a reconstruction slice whose reconstruction is
faithful and whose existence structure is not `COMPLETE`. It defines a derived evidence
structure, the addresses and consumers that may read it, its presentation and its export.

It certifies nothing. It does not change `locus-v2-existence-components.md` §15.1: a finite
exploration never establishes `LOCAL_CERTIFIED` or `COMPLETE`. It does not change the
document authority of ADR 0040, relation integrity, tokens, selectors of intersections or
the semantic snapshot. A producer that can be certified is certified as before, and its
certified components keep every right they have today.

## 2. Independent axes

A fact about a locus, a region of it or a result derived from it is stated on these axes.
None is inferred from another.

| Axis | Values that this contract reads or publishes |
|---|---|
| Reconstruction fidelity | `FAITHFUL` required; otherwise nothing of this contract applies |
| Point validity | status of one evaluation at one parameter |
| Interval evidence of existence and continuity | `EXPLORATORY`, `CONDITIONED`, `LOCAL_CERTIFIED`, `COMPLETE` |
| Numeric guarantee | the existing classes of the value (`ESTIMATED_ERROR`, …), unchanged |
| Incidence and membership integrity | `relation-integrity/v1`, unchanged; under the legacy authority, the existing contracts |
| Identity and continuation of selection | per dependency: established deterministic selection, or not established |
| Coverage | which part of the requested domain each fact covers; completeness only by certificate |
| Currentness | revision of the definition; generation of the evidence |

`CONDITIONED` is the only new value. It is of nature `NUMERICAL_UNDER_HYPOTHESES` (§15.1)
and never certifies.

## 3. Conditioned evidence

### 3.1 Structure

The producer of the locus owns one *conditioned evidence* per published revision and policy
version. It partitions the declared driver domain of a branch into cells whose ends are
dyadic fractions of that domain, and classifies every cell:

| Kind | Meaning |
|---|---|
| `SPAN` | both ends and the midpoint evaluate valid with the same established selection of every selecting dependency, and the geometric criteria of the policy hold |
| `TRANSITION_BAND` | validity differs at the ends; localized to the minimum step |
| `SELECTION_BAND` | the established selection of some dependency differs at the ends, or is not established at an end |
| `DISCONTINUITY_BAND` | a geometric criterion still fails at the minimum step: a jump or a cut is suspected |
| `FAILURE_BAND` | an end failed with a computational cause, a work limit, an authority refusal or an unreproduced selection |
| `ABSENT_BAND` | both ends and every interior node are invalid with the same eligible geometric cause (§5) |
| `UNEXPLORED` | the work limit of the policy ended before the cell was classified |

A *conditioned region* is a maximal run of adjacent spans. It is not a component: it has no
lineage, no ordinal identity and no persisted form.

### 3.2 Root testimony

A span requires, at each of its nodes, the *testimony* of every selecting dependency that
the driver-dependent part of the slice reads: for a token point of a Locus V2 intersection,
the token, its identity status, the provider-canonical parameter, the isolating interval,
the local isolation status, the numeric guarantee and the separation from the nearest other
root of the same result; for an inherited line-conic selection, its settled selection state.
The evaluator of the slice returns the testimony with the evaluation; it reads evidence that
the slice computed for that evaluation and starts no additional resolution.

A dependency whose selection is not established at a node yields a `SELECTION_BAND`. The
absence of a separation value degrades the evidence class of §3.3; it never satisfies the
selection requirement. No root is ever identified by index, proximity, order of coordinates
or visual coincidence.

### 3.3 Evidence class of a span

| Class | What was established | What is assumed |
|---|---|---|
| `RESOLUTION_ONLY` | validity and selection at the nodes; geometric criteria between them | H1–H4 of §4 |
| `MARGIN_ESTIMATED` | the same, and at every node a separation of the selected root above the policy margin | H1–H4; the separation observed at nodes is not a bound between them |
| `MARGIN_BOUNDED` | the same, and a rigorous lower bound of the separation over the whole cell | H3–H4 only |

`MARGIN_BOUNDED` needs an interval enclosure of the root family over the cell. Where that
enclosure exists the cell is normally certifiable (§11); the class exists so that a partial
proof is not discarded.

### 3.4 Policy

The traversal policy is versioned (`conditioned-traversal/v1`) and is part of the evidence
identity. It fixes the initial depth, the chord-deviation and turning limits in model units
and radians, the minimum step as a fraction of the declared domain, the margin threshold and
the work limit. Its limits are world-coordinate quantities. Zoom, DPI, viewport and render
policy never enter it. The partition depends only on the function and on the policy: the
same revision and policy give the same cells and kinds whatever the order of the requests.

## 4. Declared hypotheses

Every conditioned span carries the hypothesis set of its policy. For a span `[a, b]`:

- **H1 (validity).** The traced point is defined at every parameter of the span.
- **H2 (selection).** Every selecting dependency keeps the selection established at the
  nodes at every parameter of the span.
- **H3 (continuity).** The traced function is continuous on the span, including across every
  operation with a branch cut that the slice evaluates.
- **H4 (resolution).** The traced curve stays within the chord-deviation limit of the policy
  from the polyline of the nodes.

The contract states, and the product must state wherever it explains the grade, that:

- a traversal detects an event only when a node falls where the event is observable, or when
  the event leaves a geometric trace above the limits; two events inside one cell, a root
  exchange that returns, and a narrow excursion between nodes can pass undetected, whatever
  their size relative to the minimum step, because the minimum step is reached only where
  refinement was triggered;
- a slope observed between two nodes, multiplied by any factor, is not a Lipschitz bound;
- agreement of two traversals or of two estimates is not an error bound.

## 5. Absence, failure and totals

1. A node whose evaluation failed for a computational cause, a work limit, an authority
   refusal, an unreproduced selection or an unfaithful slice is never evidence of absence.
   Its cell is a `FAILURE_BAND`.
2. `ABSENT_BAND` requires an eligible cause at every node of the cell: an undefined
   dependency whose own status states a geometric absence (an empty intersection of
   established inputs, a point outside an established domain). The list of eligible causes is
   closed and versioned with the policy. An `ABSENT_BAND` is conditioned absence, of the same
   nature as a span; it is never `CERTIFIED_DOES_NOT_EXIST`.
3. A total over a requested domain accounts for every cell of that domain. A band is never
   dropped because it is narrow. For each band the consumer applies, in this order: a
   contribution computed through the band when both sides have one-sided limits and the
   policy admits the band kind; else an explicit bound of the contribution when one is
   available; else the result is a `SUBTOTAL` with the bands listed, or a refusal when the
   consumer's contract has no subtotal. Every hypothesis used is recorded in the result.

## 6. Addresses, identity and persistence

1. **Parameter address.** A point on a locus without a certified component at its parameter
   is addressed by `(source locus identity, branch key, provider contract, canonical
   parameter, periodic lift, seam side)`, without a component selector. It is persisted in a
   new codec, `geocedg-locus-parameter-address/v1`, distinct from
   `geocedg-locus-address/v1` and `geocedg-locus-principal-selection/v1`, whose texts and
   meanings do not change.
2. **No reinterpretation.** An existing address with a component selector resolves only as
   §15.6 says. It is never rewritten into a parameter address, and a parameter address never
   acquires a component selector silently. Converting one into the other is an explicit
   redefinition of the point.
3. **Resolution per revision.** A parameter address resolves when the evaluation at its
   parameter is valid and every selecting dependency has an established selection there. The
   evidence that contains the parameter (a certified component, a conditioned span, a band,
   or none) is reported with the point; it does not decide whether the point is defined, and
   it decides what continuation is offered (§7).
4. **Lifecycle.** Refinement, split, merge, withdrawal, reactivation and promotion of
   evidence change no address and no identity. Copy, redefine, undo, redo and reopen follow
   the rules of the existing address codecs. Evidence is recomputed after reopening and is
   never read from the document.
5. **Earlier readers.** A reader that does not know the new codec does not resolve the
   address: the point is undefined there with the existing typed cause for an invalid
   address, and the rest of the document is read. This is a declared compatibility limit.

## 7. Evidence lifecycle

1. The producer publishes the evidence atomically, as an immutable value identified by
   `(revision, policy version, generation)`. Generation increases with every refinement,
   contradiction or promotion inside one revision. A new revision discards the evidence of the
   previous one and cancels work in progress for it.
2. A consumer records the generation it read. A later generation that changes the cells its
   result used invalidates that result through the normal dependency graph; one that only
   refines other cells does not.
3. **Contradiction.** A consumer, the renderer or a later traversal that observes a fact
   contrary to a span (an invalid evaluation, another selection, a jump) reports it to the
   producer. The producer publishes a generation in which the span is split and the
   contradicted cell is the corresponding band. The renderer never changes admissibility by
   itself.
4. **Promotion.** A certificate of a region replaces its spans by a certified component in
   the existence structure, through the certifier of §15.7. Promotion changes no identity and
   no function. The conditioned evidence never promotes itself.
5. **Work.** The traversal of a revision is charged to the policy's work limit. When the
   limit ends first, the remaining cells are `UNEXPLORED`: the semantic content available to
   consumers is the published generation, the same whatever has been drawn. A progressive
   drawing never changes what a consumer may read.

## 8. Consumers

Each consumer keeps its existing behaviour on certified components. The table states what it
reads and publishes on conditioned evidence. «Legacy» and «constructive» are the document
authorities; where they differ only in integrity, the row says so.

| Consumer | Premises read | Published grade | Typed refusals | Identity and persistence |
|---|---|---|---|---|
| Parametric point `Point(L, u)`, `Point(L, "branch", u)` | point validity and established selection at `u`; unambiguous address | defined; existence `POINT_VALID`, with the evidence containing `u` reported; numeric guarantee of the evaluation; constructive: integrity at most `RELAXED` | `PARAMETER_OUTSIDE_DOMAIN`, `EVALUATION_FAILED`, `SELECTION_NOT_ESTABLISHED`, `POINT_COMPONENT` | parameter address (§6); no component lineage |
| Following and continuation (drag, tracking) | a conditioned region containing start and target; no band between them | continuation along the region, declared conditioned | stops at a band, with its kind as cause | the address after the gesture is a parameter address |
| Length between two positions | both positions in one conditioned region; route inside it | at most `ESTIMATED`, with H1–H4 and the estimator hypotheses | `ROUTE_CROSSES_BAND` with the band kinds, `POSITIONS_IN_DIFFERENT_REGIONS` | revision and evidence generation recorded |
| Total length | every cell of the requested domain classified | at most `ESTIMATED` when every cell is a span or an admitted band with a computed or bounded contribution; `SUBTOTAL` with its omitted bands otherwise | `UNEXPLORED_DOMAIN`, `FAILURE_BAND_IN_DOMAIN` | as above; H10 and P2 (metrics §26.5, §26.6) unchanged and independent |
| Intersection with a target | evaluation of the source at the solver's parameters; residual; membership of the root in a span; local isolation | local roots `VERIFIED_UNCERTIFIED`, each with the evidence class of its span; completeness always `NOT_ESTABLISHED`; never `EMPTY` | a root in a band is not published; `SOURCE_NOT_CONDITIONED` | tokens by the existing deterministic selection; ledger unchanged |
| Dependent locus | the premises that its own slice uses of the source | its own conditioned evidence, computed on its own function; it records the source hypotheses it inherits | as its own producer | its own revision; nothing shared |
| Drawing | spans and bands of the published generation | conditioned stroke; never closed unless the region covers the whole period with no band and the seam nodes agree, and then shown as closed-conditioned | none: it draws what is published | none |
| Export | the published generation and the format capability (§10) | per format | diagnosed omission | none |

Propagation is by premise: a result is graded by the facts it used. A point at a parameter
where the evaluation is valid is defined although a distant band exists; a length between
two positions of one region does not inherit a failure band elsewhere; a total over the whole
domain does.

A consumer that requires a certified component, `COMPLETE` or `STRICT` keeps failing closed
with its existing cause.

## 9. Presentation

1. A conditioned region is drawn as a continuous stroke of the locus colour with a style
   distinct from the certified stroke and from the exploratory marks. Bands are not joined:
   a transition band ends the stroke, and a failure or discontinuity band is marked as a
   computational gap. The rule of `locus-v2-presentation.md` that no render segment crosses
   an unresolved band is unchanged.
2. The render tessellation of a region is derived per view from the evaluator, inside the
   spans of the published generation. It adds no evidence and removes none.
3. The product names the grade in ordinary terms wherever it names the object: the status
   text of the locus and of results derived from it says that the trace or the value is
   conditioned and not certified, and lists the hypotheses on request. Internal kinds and
   tokens are never dumped into the ordinary workflow.
4. A drawing that looks closed is never called complete. `COMPLETE` is shown only for the
   certified value of the coverage axis.

## 10. Export

1. A format exports a conditioned region only when it carries, for each exported entity, the
   fidelity class `CONDITIONED`, the policy version and the fact that the geometry is a
   derived approximation of a faithful procedure. Otherwise the entity is omitted and the
   omission is diagnosed in the export report.
2. Picture exports of a view (PNG, PDF, SVG, EPS, PSTricks, TikZ as pictures of the drawing)
   export what the view draws, with the conditioned style.
3. DXF and semantic LaTeX export follow their own specifications; this contract proposes for
   them only the admission rule of point 1 and leaves their encodings to their amendments.
4. Exploratory evidence stays never exported.

## 11. Promotion by proof

Conditioned evidence proposes; the kernel proves. A region is promoted to a certified
component only by a certificate under `locus-existence/hybrid/v1` or a successor. Two bounded
promoters are identified by the design, each requiring its own approved increment:

- **K2, composition through a certified reparametrization**: for `P(θ) = L(τ(θ))` with `L`
  certified and `τ` an enclosed scalar program, a certificate of the image and, separately,
  of the position length `Λ_L(τ(θ))`. The travelled length, which involves `|τ′|`, is another
  functional.
- **K1, certified root family**: for a traced point that reads a selected root of an
  intersection of a certified source with a moving target, an enclosed family of the root
  parameter over a cell. A fold needs `h = 0` and `h_u = 0` together.

The cells and bands of the conditioned evidence are candidate proof cells; a verifier may
reject or subdivide them. No promotion is part of `conditioned-use/v1`.

## 12. Amendments that adoption would require

| Specification | Amendment |
|---|---|
| `locus-v2-existence-components.md` §15.1, §15.8 | name the conditioned evidence as the producer of `NUMERICAL_UNDER_HYPOTHESES`; add the consumer admissions of §8 beside the existing exception |
| `locus-v2-public-surface.md` | parameter address codec and its resolution |
| `locus-v2-point-interaction.md` | continuation along a conditioned region |
| `locus-v2-metrics.md` | conditioned between-position and total lengths; relation to §26.5 and §26.6 |
| `locus-v2-intersections.md` | local roots on a conditioned source |
| `locus-v2-presentation.md` | conditioned stroke, closure wording, status text |
| export specifications | admission rule of §10 |
| `locus-v2-semantics.md` | none: the snapshot does not change |

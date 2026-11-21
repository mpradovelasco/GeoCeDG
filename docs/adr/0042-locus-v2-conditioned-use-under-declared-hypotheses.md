# ADR 0042 — Locus V2 conditioned use under declared hypotheses

- Status: **PROPOSED, REVISION 4 — PENDING AUTHOR APPROVAL**. The product decisions D-V1 to
  D-V7 below are fixed by the author; this text, which states them as a contract, is not
  approved, and no implementation of the new semantics is authorized by it.
  `selfApproved=false`.
- Date: 2026-11-21 (revision 1, commit `ab37fb5ff`; revision 2, commit `83cd56c4c`);
  revision 3 the same day (commit `57c495cce`), with the focal amendment that the author
  requested after reviewing revision 2; revision 4 the same day, with the two focal
  precisions that the author requested after reviewing revision 3.
- Authorization of the design work: author instructions of 2026-11-21, «Ejecución secuencial
  tras R1» (block 3A) and «Revisión normativa post-R1, continuación de implementación y
  objetivo 1.2.2».
- Base: `9ccefdcfe922d8c0e329bdeec30c12fc058de463` plus the post-R1 repairs.
- Design: [architecture and source analysis](../architecture/post_r1_locus_conditioned_use_design.md).
- Candidate contract: [conditioned use](../../geocedg/specs/locus/candidates/post-r1-conditioned-use.md).
- Author decisions and open points: [decision record](../validation/post_r1_conditioned_use_author_decision_sheet.md).

## Author decisions incorporated (2026-11-21)

| Id | Decision |
|---|---|
| D-V1 | Conditioned use is available by default for every eligible Locus V2 in GeoCeDG 1.2.2: no manual activation, no experimental mode, no per-document policy, and no user option to disable it unless a demonstrated technical or safety need requires a further decision. Admissibility is decided dynamically from the construction, the evidence and the guarantee each consumer requires |
| D-V2 | The parameter-address codec is accepted. Reading of 1.2.2 documents by GeoCeDG 1.2.1 is not required. Persistence within 1.2.2 must be complete and reproducible |
| D-V3 | The conditioned state is identified independently of the user's line style: a discrete system mark if it is sound to build, and in every case a readable state in the properties of the object, with the hypotheses available without developer diagnostics |
| D-V4 | Semantic geometric exports of conditioned regions are excluded initially, with an explicit diagnostic; picture exports of a view export what it shows. Semantic export is planned with the future line on B-splines and derived representations, DXF import and extended DXF export |
| D-V5 | A conditioned locus takes part in the same families of operations as a certified one, with the guarantees that correspond: points, following, intersections, transformations, dependent constructions, nested loci, metrics, DAG propagation, presentation and manipulation. Functional equality is not equality of certification |
| D-V6 | The cost of an update that really changes the geometry is reduced, without a shared result cache and without shared mutable copies |
| D-V7 | GeoCeDG 1.2.2 is the target version; its number is changed only when the functionality is implemented and validated |

Author responses to the open points of revision 2 (2026-11-21): the design is assessed
favourably and D-V1 to D-V7 stand; the D-V6 result is accepted technically with its
documented verification coverage; the system mark is a hollow rhombus of 7 pixels (P2); the
future semantic export is registered as a requirement of the B-spline and DXF line, with no
new subphase opened (P3); the early triage of the pre-existing defect F8 is authorized, with
its repair before increment I4 closes (P4). The normative approval of this text was withheld
until the six points of the focal amendment were incorporated; they are listed under
«Differences from revision 2».

Author response to revision 3 (2026-11-21): the six points are confirmed as correctly
incorporated and the general architecture is accepted. Before the definitive normative
approval two focal precisions are required, on the identity of roots across configurations
and on the resolution of the evidence under similarities; they are listed under
«Differences from revision 3». The review is not widened to other contracts. Preparatory
work on increments I0 to I3 that does not depend on the two precisions is allowed, kept as
candidates pending the final normative approval; I4 and I4T are not started before it.
Publication on `main`, the tag `v1.2.2` and a release remain unauthorized.

## Context

A Locus V2 can be published with a faithful reconstruction slice and no certified
component. Its existence structure is then `NOT_ESTABLISHED`, it is drawn as the sampled
runs of the exploratory evidence, and every consumer that needs a component refuses it:
`Point(L, u)` fails with `OUTSIDE_COMPONENTS`, a length between positions is not available,
an intersection publishes zero solutions, and a transformed image inherits the same empty
structure. That refusal conforms to `locus-v2-existence-components.md` §15.8. On a
`COMPLETE` locus the same consumers already publish results of lower grades on other axes
(`ESTIMATED` lengths, `VERIFIED_UNCERTIFIED` intersections).

The existence axis is the only one without a usable grade between exploratory evidence and
a certificate. The nature `NUMERICAL_UNDER_HYPOTHESES` is named in §15.1 and in
`LocusEvidenceNature2D` and has no producer and no consumer contract. AGENTS §1 principle 8
requires that any approximation be explicit, tolerance-controlled, reproducible and
distinguishable from an exact result.

## Decision (proposed text)

1. **A conditioned evidence structure, owned by the producer, published by default.** Every
   reconstructible Locus V2 whose slice is faithful publishes, beside its existence
   structure and never inside it, a *conditioned evidence* of nature
   `NUMERICAL_UNDER_HYPOTHESES` for the part of its domain that no certificate covers: a
   canonical dyadic partition into *conditioned spans* and typed *bands*, computed by a
   versioned policy from evaluations of the slice and from the root testimony the slice
   already holds. It is revision evidence: derived, never persisted, never identity, never
   part of the semantic snapshot. There is no switch: eligibility is a property of the
   construction (D-V1).

2. **Independent axes, no single ladder.** Point validity, interval evidence of existence
   and continuity (under hypotheses or certified), global completeness, numeric guarantee,
   incidence integrity, identity and continuation of selection, coverage and currentness stay
   separate facts. A result states the grade of each axis it used.

3. **Each evidence class says what it proves.** For every class the contract lists what is
   proved, what is only checked at nodes, what remains a hypothesis and which consumers may
   read it. A rigorous bound of the separation of roots proves that those roots do not
   collide on a cell; it proves neither the validity of the other operations nor the
   existence or continuity of the branch, nor that a selection is stable: the stability of a
   selection is established only by a proof of the selection predicate itself. No hypothesis
   is withdrawn without a proof that satisfies it. Density of samples never raises a class,
   and no class gives a consumer a permission that another class does not give.

4. **Failure is not absence.** A computational failure, a work limit, an authority refusal
   or an unreproduced selection yields an unresolved band. A conditioned absence needs
   positive evidence of an eligible geometric cause.

5. **A deterministic lifecycle, owned by the semantic producer.** The content identity of
   the evidence is the revision, the policy and the set of parameters at which a
   contradiction was confirmed; its content is a function of them and never of the order of
   queries, reports or drawing, of the renderer or of the view. The counter of publications
   is another attribute: it orders publications and is never an identity. A contradiction is
   reported to the producer, which re-evaluates it itself and publishes through the
   dependency graph. A result is current while the cells it used are unchanged; stale
   evidence is never reused.

6. **One contract per operation, functionally complete.** Points, following, metrics,
   intersections, transformations, dependent and nested loci each state inputs, premises,
   required evidence, published guarantee, incidence, identity, propagation, degeneracies,
   persistence and refusals (D-V5). A local result that meets its own premises is usable
   although global completeness is `NOT_ESTABLISHED`. No operation publishes an ambiguous
   root, an association by proximity, an unproved closure, `EMPTY` or a complete set. A
   consumer that requires a certificate keeps failing closed. A transformation transforms
   the parametric procedure, never the drawing polyline, and a dilation of ratio zero keeps
   the approved `COLLAPSED_IMAGE` contract. The image states its evidence against the policy
   in its own world: lengths and the bound of the resolution hypothesis are multiplied by
   the absolute ratio, and a span that a ratio above 1 leaves outside the limit of the
   policy is classified anew, never kept with a tolerance that no longer holds. Under the
   constructive authority the integrity
   and relaxed-admission requirements are unchanged: conditioned evidence never grants
   `RELAXED`, and no widening of those requirements is proposed.

7. **Identity never comes from the sampling.** A point on a region without a certified
   component persists a parameter address without a component selector, in its own versioned
   codec. An intersection root on such a region takes a selector of its own kind whose frame
   is the branch, without rank; a region, a cell and the partition are never identity. The
   token of such a selector is bound with a fixed origin and is resolved only while its root
   lies in the conditioned region of that origin: the uniqueness of the selector in the
   current state is not enough, the evidence can withhold a token, and it never chooses one
   or gives it to another root. The
   meaning of an earlier codec or selector is never reused, and no migration invents an
   identity or a spatial association. The validity of a point and the admissibility of a
   continuation are separate facts: a valid point inside a band is defined and has no
   continuation. Reading by earlier versions is not a requirement (D-V2); reproducible
   persistence within 1.2.2 is.

8. **Identification independent of style.** The line style of a locus stays the user's. The
   conditioned state is stated in the properties of the object in ordinary words, with its
   hypotheses, and is shown in the view by a discrete system mark, a hollow rhombus of 7
   pixels, that is not a style, not an orientation arrow, and neither geometry nor evidence
   (D-V3). The mark never suggests certification or completeness.

9. **Exports are explicit.** Picture exports of a view export what it shows. Semantic
   exports omit conditioned regions with a diagnostic until a format can carry their
   guarantee and provenance; that work is registered in the roadmap as a requirement of the
   future line on derived representations and DXF interchange, with no subphase opened, in
   which a spline or polyline of interchange never replaces the authoritative CeDG object
   (D-V4).

10. **Certification stays direct, and conditioned evidence feeds it.** A certifiable
    producer is certified as today and keeps its contracts. Spans and bands are proof plans
    for K2 and K1, which remain separate, later capabilities.

## Differences from revision 1 (`ab37fb5ff`)

| Topic | Revision 1 | Revision 2 |
|---|---|---|
| Activation | not stated | by default, no switch (D-V1) |
| `MARGIN_BOUNDED` | assumed H3 and H4 only | proves only the non-collision of the bounded roots on the cell; H1, H3 and H4 stay hypotheses and H2 stays one outside that fact |
| Evidence classes | table of «established / assumed» | per class: proved, checked at nodes, hypotheses, admitted consumers |
| Lifecycle | generation, contradiction reported to the producer | canonical contradiction set, producer re-evaluation, defined withdrawal, invalidation and identity of affected points; no stale evidence |
| Earlier readers | declared limit and reader test | no requirement; obligations of 1.2.2 persistence listed instead (D-V2) |
| Consumers | eight rows, intersections and dependent loci as later increments | full matrix per operation, including intersections of both kinds, transformations, nested loci and manipulation, inside the 1.2.2 scope (D-V5) |
| Presentation | mandatory long-dash stroke | style untouched; state in properties; discrete system mark (D-V3) |
| Export | admission rule, increment I5 | excluded from 1.2.2 with diagnostic; dependency on the derived-representation and DXF line (D-V4) |
| Scope of the first implementation | I0–I3 | I0–I4 and I4T are all required for 1.2.2 |

## Differences from revision 2 (`83cd56c4c`)

Focal amendment; the functional objective and the rest of the design do not change. The
exact text and the validation criteria of each point are in §14 of the candidate contract.

| # | Point | Revision 3 |
|---|---|---|
| 1 | `RELAXED` integrity | `CONDITIONED` never grants `RELAXED`; the positive requirements of the authority contract are preserved and none is widened |
| 2 | Identity of intersection roots | a branch-frame selector of its own kind for a root on a span, without rank and never built from the partition; selectors of certified roots unchanged |
| 3 | Generations | content identity and publication counter are separate; the content is a function of a set of parameters; staleness is decided by cells |
| 4 | Points in bands | point validity and admissibility of continuation are separate |
| 5 | Ratio zero | the approved `COLLAPSED_IMAGE` contract, unchanged |
| 6 | `MARGIN_BOUNDED` | proves only the pairwise separation of the enclosed roots; H2 stays whole |

## Differences from revision 3 (`57c495cce`)

Two focal precisions; nothing else changes. The exact text and the validation criteria are
in §15 of the candidate contract, and the adverse cases run in the design probe
`tools/benchmark/post-r1-locus/conditioned-use-precisions-probe.js`.

| # | Precision | Revision 4 |
|---|---|---|
| 7 | Identity across configurations | one binding per branch-frame selector, with a fixed origin; the token is resolved only when the origin and the root lie in one conditioned region, and is otherwise withheld: never transferred, replaced or retired, and no other token is allocated for the selector |
| 8 | Resolution under similarities | the quantities of a cell transform by the absolute ratio as stated; the image states the resolution hypothesis with the limit of the policy in its own world; for a ratio above 1 every span is checked again and classified anew where it fails; a band never becomes a span; the image evidence has its own content identity |

## Alternatives considered

| Alternative | Why it is not proposed |
|---|---|
| Keep the refusal and wait for K1 | Leaves existing, well-defined objects unusable; contradicts the author's criterion |
| Admit `EXPLORATORY` evidence to consumers | No declared hypothesis, no contradiction rule, no consumer contract; §15.1 forbids it |
| Promote dense sampling to `LOCAL_CERTIFIED` | A false certificate; violates §15.1 and AGENTS principle 8 |
| A document-wide relaxed authority or a user switch | The document authority decides relations, not existence; the author decided against a switch (D-V1) |
| A dash pattern as the identification | The style belongs to the user (D-V3) |
| Transform or intersect the drawing polyline | Not the geometric operation; forbidden by AGENTS §11.2 and by D-V5 |
| Give a conditioned region a lineage from its bands, and rank its roots | The bands are what the traversal detected: an identity by sampling. Rank needs a verified cardinality |
| Let conditioned evidence count toward `RELAXED` | The authority contract excludes a numerical verification under hypotheses; a widening would be its own normative decision |
| Resolve a branch-frame token by the uniqueness of its selector alone | A root that appears in another region of the branch with the same germ would take the token of a root that disappeared, and the objects built on it would jump there |
| Follow the root: update a recorded position at each resolution, or reuse by nearest position | Movement history and proximity, which the intersection contract excludes from identity |
| Several bindings per selector, each with its origin | Which origin a root is connected to would depend on which bands the traversal has found: the evidence would choose the token |
| Retire the token when its root is not found in the region of the origin | A band that is only a failure or an unexplored cell would destroy an identity for good; withholding is reversible |
| Copy the cells to the image and declare the larger bound | A consumer of the image would read a resolution in the units of the source, and one policy version would mean different limits on two loci |
| Traverse the image from scratch | It would turn a band of the source into a span when the image is small, and would coarsen the cells of a reduction |

## Consequences

- Positive: a faithful uncertified locus is drawn as a curve and takes part in the public
  operations with an explicit, propagated grade; strict consumers are unchanged.
- Cost: an evidence owner and lifecycle in the kernel, a new address codec, a consumer
  matrix to maintain, and a traversal charged once per revision. The post-R1 repairs keep
  revisions stable under driver motion, and the D-V6 optimization brings the cost of a
  revision that does change to about a third on the author model.
- Risk: a conditioned span can be wrong where an event falls between nodes. The risk is
  stated to the user, bounded only by the declared hypotheses, and removed only by proof.
- Compatibility: a 1.2.2 document with a parameter address is not required to be readable by
  1.2.1. No existing document changes its results.

## Status of the normative texts

The amendments listed in §12 of the candidate contract are proposed texts. None is applied
to the specifications by this ADR.

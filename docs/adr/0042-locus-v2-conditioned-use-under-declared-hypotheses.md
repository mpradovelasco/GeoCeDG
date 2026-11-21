# ADR 0042 — Locus V2 conditioned use under declared hypotheses

- Status: **PROPOSED, REVISION 2 — PENDING AUTHOR REVIEW**. The product decisions D-V1 to
  D-V7 below are fixed by the author; this text, which states them as a contract, is not
  approved, and no implementation of the new semantics is authorized by it.
  `selfApproved=false`.
- Date: 2026-11-21 (revision 1, commit `ab37fb5ff`); revised the same day after the author
  decisions.
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
   existence or continuity of the branch. No hypothesis is withdrawn without a proof that
   satisfies it. Density of samples never raises a class.

4. **Failure is not absence.** A computational failure, a work limit, an authority refusal
   or an unreproduced selection yields an unresolved band. A conditioned absence needs
   positive evidence of an eligible geometric cause.

5. **A deterministic lifecycle, owned by the semantic producer.** Evidence is identified by
   revision, policy and generation. Its content depends on the function, the policy and the
   canonical set of confirmed contradictions, never on the order of queries, on the renderer
   or on the view. A contradiction is reported to the producer, which re-evaluates it itself
   and publishes a new generation through the dependency graph. Stale evidence is never
   reused.

6. **One contract per operation, functionally complete.** Points, following, metrics,
   intersections, transformations, dependent and nested loci each state inputs, premises,
   required evidence, published guarantee, incidence, identity, propagation, degeneracies,
   persistence and refusals (D-V5). A local result that meets its own premises is usable
   although global completeness is `NOT_ESTABLISHED`. No operation publishes an ambiguous
   root, an association by proximity, an unproved closure, `EMPTY` or a complete set. A
   consumer that requires a certificate keeps failing closed. A transformation transforms
   the parametric procedure, never the drawing polyline.

7. **Addresses name a parameter.** A point on a region without a certified component
   persists a parameter address without a component selector, in its own versioned codec.
   The meaning of an earlier codec is never reused, and no migration invents an identity or
   a spatial association. Reading by earlier versions is not a requirement (D-V2);
   reproducible persistence within 1.2.2 is.

8. **Identification independent of style.** The line style of a locus stays the user's. The
   conditioned state is stated in the properties of the object in ordinary words, with its
   hypotheses, and is shown in the view by a discrete system mark that is not a style, not
   an orientation arrow, and neither geometry nor evidence (D-V3). The mark never suggests
   certification or completeness.

9. **Exports are explicit.** Picture exports of a view export what it shows. Semantic
   exports omit conditioned regions with a diagnostic until a format can carry their
   guarantee and provenance; that work belongs to the future line on derived representations
   and DXF interchange, in which a spline or polyline of interchange never replaces the
   authoritative CeDG object (D-V4).

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

## Alternatives considered

| Alternative | Why it is not proposed |
|---|---|
| Keep the refusal and wait for K1 | Leaves existing, well-defined objects unusable; contradicts the author's criterion |
| Admit `EXPLORATORY` evidence to consumers | No declared hypothesis, no contradiction rule, no consumer contract; §15.1 forbids it |
| Promote dense sampling to `LOCAL_CERTIFIED` | A false certificate; violates §15.1 and AGENTS principle 8 |
| A document-wide relaxed authority or a user switch | The document authority decides relations, not existence; the author decided against a switch (D-V1) |
| A dash pattern as the identification | The style belongs to the user (D-V3) |
| Transform or intersect the drawing polyline | Not the geometric operation; forbidden by AGENTS §11.2 and by D-V5 |

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

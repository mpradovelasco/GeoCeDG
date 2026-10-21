# ADR 0038 — Locus V2 hybrid existence certification and certified closure

- Status: **ACCEPTED — ARCHITECTURE AUTHOR APPROVED WITH PRECISION D-CU2-20-R** (author
  instruction "GeoCeDG — PRE-G9B-CU2. Aprobación autoral de la arquitectura y autorización
  condicionada de implementación", 2026-10-21;
  [approval record](../validation/pre_g9b_cu2_author_architecture_approval_record.md)). This is
  an architectural decision: the text incorporated into the specifications at step T0 belongs
  to the `CU2` technical candidate, which is pending author review; acceptance does not mean
  that `CU2` is implemented or verified.
- Date: 2026-10-20; revised the same day by the focused review P1–P5 requested by the author
  and by the coherence closeout C1–C3; accepted on 2026-10-21
- Phase: `PRE-G9B-CU2` (preparation authorized on 2026-10-20; implementation authorized on
  2026-10-21 on the exact base `070af6f902cd0be291247e73efb5629ef2a39eba`)
- Extends: [ADR 0027](0027-locus-v2-existence-and-continuous-valid-components.md). Its four
  axes, its consumer rules and the independence of local admissibility from global
  completeness are preserved unchanged.
- Author decisions:
  - hybrid direction DA-1 (2026-10-19; [`CU0` decision sheet](../validation/pre_g9b_cu0_author_decision_sheet.md) §1);
  - [`CU1` closeout record](../validation/pre_g9b_cu1_closeout_record.md) §7: `CU2`
    preparation authorized; a class-3 length never becomes rigorous evidence of a `CU2`
    component;
  - the author's focused review of the preparation candidate (2026-10-20): the general
    direction is favourable; five precisions P1–P5 before approval;
  - the author's coherence closeout instruction (2026-10-20): effective outputs at coincident
    roots (C1), point components (C2) and the precision of the evidence claims (C3);
  - the author's architecture approval (2026-10-21): amendments A2.1-R to A2.6 and this ADR
    approved with the mandatory precision D-CU2-20-R (decision 17), decisions D-CU2-1 to
    D-CU2-20-R and E-1 to E-3 disposed, implementation authorized on an exact base.
- Parent authority (versions that incorporate the approved text at T0):
  - [existence and continuous-valid components](../../geocedg/specs/locus/locus-v2-existence-components.md) v1.1, §15;
  - [certified construction model](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) v1.2, §10;
  - [metric contract](../../geocedg/specs/locus/locus-v2-metrics.md) v1.6, §26;
  - [semantic contract](../../geocedg/specs/locus/locus-v2-semantics.md) v1.3;
  - [presentation](../../geocedg/specs/locus/locus-v2-presentation.md) version 2;
  - ADR 0027, ADR 0028, ADR 0029 and ADR 0037.
- Normative text: [`CU2` normative proposal](../architecture/pre_g9b_cu2_normative_proposal.md)
  §2–§7 (amendments A2.1-R to A2.6), incorporated into the specifications above, into the
  scope notes of the consumer specifications, into
  [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
  §10 and into the new
  [`latex-semantic-curve-export.md`](../../geocedg/specs/export/latex-semantic-curve-export.md)
- Evidence: [author model certification analysis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md);
  [hybrid certification design](../architecture/pre_g9b_cu2_hybrid_certification_design.md);
  [oriented length event design](../architecture/pre_g9b_cu2_oriented_length_event_design.md);
  [consumption and representation](../architecture/pre_g9b_cu2_consumption_and_representation.md);
  `CU0` baseline pins `B07`, `H01`, `B06` and `B10.2`

## Context

ADR 0027 separated the declared driver domain, certified existence coverage, certified
continuous-valid components and global completeness. Its implementation added a successor
existence structure, but dependent producers still publish a compatibility certificate:
`AlgoDependentPointLocusV2` declares `COMPLETE` with one periodic component equal to the
declared domain, without proof. ADR 0027 itself requires dependent producers to fail closed
as `NOT_ESTABLISHED` until suitable evidence exists.

`CU0` measured the consequences in the author's model:

- after the cylinder bites the cone, the four first-family loci keep the compatibility
  component while their sampled runs have a real gap; the drawing joins it or leaves gaps at
  the branch ends (B07);
- when the saved driver falls inside the gap, the loci stop being published although they
  exist on an arc (H01);
- `e_2` is drawn closed, with a 13.19-unit segment across the wrap of the oriented length
  (B10.2);
- tessellation accepts turns of up to 180° (B06).

Three structural facts block a real certificate:

1. the certified construction model has no ray, no line–circle intersection with a defined
   root selection, no multi-output step, no vectors and no macros, and it cannot tell an
   undecidable box from a uniformly invalid one;
2. components are intervals inside `[−π, π)`, so a component that crosses the seam cannot be
   expressed, and five consumers decide closure with five different predicates;
3. `e_2` depends on `LocusLength`, whose value is class 3 (`ESTIMATED`) and therefore never a
   rigorous input of a certificate (ADR 0037, metrics §25.5).

The focused review added four facts:

- the discriminant of the bite factors exactly into the signed distances of the driver point to
  the two tangents from `P` to the cone base; at the critical value it has a double zero
  without sign change, so the loci keep existence and continuity with a corner. Ordinary
  simple-root isolation (a sign change with a derivative that does not vanish) cannot certify
  a double root, and verified methods for multiple roots need added structure or prove that a
  nearby problem has one; exact rational arithmetic on the captured binary64 constants decides
  it for the configuration those constants define;
- no binary64 value of the author's slider is exactly critical: the exact classification
  changes between two consecutive doubles, the double-precision closed form misclassifies one
  of them, and the evaluator returns points inside the exact gap;
- the output of a line–circle intersection is selected through a permutation that is state of
  the algorithm and is reinitialized by proximity on creation, load and construction updates,
  including in the evaluator slice of each revision;
- the folds of `e_1` exist because of an exact symmetry of the construction; an enclosure of
  `F′` that contains zero would not prove them.

The coherence closeout added three more:

- GeoGebra sets an unlabelled second intersection output undefined when it coincides with the
  first within 10⁻⁸ per coordinate. Near a coincident root the effective definition of such an
  output is scattered, depends on tolerance and rounding, and differs between the live
  construction and the evaluator slice. In the author's model both outputs of the bite are
  labelled, live and in the slices, so at the double root both are defined and equal;
- an exact certificate can reduce existence to an isolated parameter, which no interval or
  arc of positive length represents;
- the revision fingerprint and the reconstruction of the evaluator slice have no stated
  fidelity contract: the fingerprint omits the selection and label states, and nothing
  establishes that the slice reproduces exactly the authoritative inputs it reads.

The coherence closeout first reported that a one-ulp change of a captured constant published
no revision because the fingerprint serialized numbers with 15 significant digits. The
characterization at the start of the implementation (2026-10-21) showed that this attribution
was wrong: numeric element values serialize exactly, and the observed stale slice came from the
`H01` publication rule (the traced point was undefined at the driver position, so the locus was
not republished and the stale definition was evaluated, finding F-CU2-3). With the driver away
from the degeneracy every one-ulp change publishes a revision and the slice copy is exact. The
remaining gaps are the selection and label states, literals whose value has no original text,
which the XML template may write with fewer digits than an exact round trip needs, and the
absence of any fidelity validation. Decision 17 (D-CU2-20-R) closes them.

## Decision

1. **Four evidence levels.** `POINT_VALID`, `EXPLORATORY`, `LOCAL_CERTIFIED` and
   `COMPLETE`. Exploratory evidence is a separate typed structure, derived per revision and
   policy, not persisted and not part of the revision fingerprint. It never becomes a
   component, a completeness claim, an identity or an export. `COMPLETE` is a certified cover
   of the whole domain, never inferred from explored boxes.
2. **Component continuity.** A certified component requires the continuity of the traced
   map, besides existence and continuation of the captured selections. A structural
   discontinuity with valid evaluations on both sides is a boundary; a certified singular
   parameter is interior.
3. **Arcs and one-sided ends.** On a periodic domain a component is an arc (canonical start,
   length, end inclusion, arc coordinate). A full-length arc whose map is certified continuous
   at its closing point is a closed periodic component; otherwise it is open, and its two ends
   are distinct one-sided ends that canonicalization never identifies.
4. **One closure authority.** Every consumer decides closure, seam crossing and periodic
   semantics from the certificate. Neither the provider's `PERIODIC` flag nor the legacy
   component view decides topology.
5. **Class v3.** The certified construction model gains a versioned class with rays and
   segments, circles by centre and point, line–circle intersection, multi-output steps, vector
   operations, macro expansion, a structural metric input and second-order jets. The selection
   of an intersection output is read from the algorithm's selection state (permutation,
   initialization flags and label state of its outputs), which enters the revision fingerprint
   and must match the live algorithm in the evaluator slice. Boxes
   evaluate to `VALID`, `UNIFORMLY_INVALID` with a typed reason, or `UNRESOLVED`; intervals
   establish uniform invalidity only for strict inequalities.
6. **Certified transitions.** A boundary between existence and nonexistence is a simple root
   of an identified validity predicate, certified by a sign change and a derivative enclosure
   that excludes zero. End inclusion follows the predicate. Pointwise validity of the evaluator
   never certifies a boundary.
7. **Exact algebraic certificate.** First-level validity predicates (inputs rational in the
   driver, driver on a circle or a segment) are decided in exact rational arithmetic on the
   captured constants: roots, multiplicities and signs. Multiple roots, including the critical
   value of the bite, are certified this way, never by simple-root isolation; multiple roots of
   other predicates stay unresolved in `CU2`. A double zero without sign change is a certified
   singular parameter; with nonpositive sides, an isolated point of existence (decision 15).
   The certificate decides the configuration of the captured binary64 constants, never an
   ideal relation that the construction computes with rounding.
8. **Certified stationary points.** A fold is certified only by an exact structural symmetry
   (existence), an enclosure of one component of `F″` excluding zero (isolation), `F″(t₀) ≠ 0`
   (non-degeneracy), continuity, and opposite one-sided tangents. Otherwise a high turn is not
   a fold.
9. **Structural periodicity.** A program whose driver is a point of a circle and whose
   operations are functions of their current inputs defines a function on the circle and may
   claim its periodic identification. The periodicity of the driver alone never closes a
   component.
10. **Structural certificate of a metric function.** A `LocusLength` node in a dependent
    slice contributes its definition domain, its continuity and its `ORIGIN_WRAP` event with a
    certified positive jump and two distinct one-sided ends, never its value. `e_2` is
    certified as one open component cut at the origin of the oriented length; its coordinates
    remain estimates under the `CU1` estimate contract. `LocusLength` values and policies do
    not change.
11. **Existence, evaluation and representation.** Mathematical existence and certified
    topology, numerical availability and representation availability are distinct facts and
    none is inferred from another. A failed computation never yields certified nonexistence;
    a valid evaluation never yields existence; certified existence is never a guarantee of
    coordinates. Rendering and export declare their effective fidelity and fail explicitly.
12. **Publication.** A locus is published when its definition inputs are defined. The traced
    point at the current driver position never decides publication. A recompute that does not
    publish discards the previous definition.
13. **Identities, lineage and selectors.** The durable identity of a result, the semantic
    identity of its selection and the lineage of a component are distinct. Component lineage
    derives from the sources of its boundaries. A persisted selector resolves only through a
    demonstrated structural correspondence, which unique containment can validate but never
    create; ambiguity fails closed with a typed cause, and resolution never rewrites persisted
    content.
14. **Retirement of the compatibility certificate.** It is removed only after the hybrid
    certifier, the consumer migration and the closure authority are in place. A locus that
    cannot be certified falls to `NOT_ESTABLISHED`, never back to compatibility.
15. **Point components.** A certified existence set reduced to an isolated parameter `t*` is
    a point component, an entity of its own and never an interval or arc of positive length:
    certified enclosure of `t*` with its structural source, closed inclusion, trivial
    continuity, maximality from certified nonexistence on both sides, compatibility with
    `COMPLETE`, lineage with typed point–arc transitions, zero length, membership by structural
    address, and a position that is a certified enclosure or unavailable. In `CU2` it is
    declared, not drawn or exported as a path, and creates no public point object; a consumer
    that does not support it declares the limitation and fails explicitly.
16. **Effective outputs at coincident roots.** Existence and multiplicity of a root, the
    semantic identity of the selected solution, the effective definition of the output, its
    availability in the slice, the continuity of the one-sided limits and the continuity of the
    selected construction are distinct facts. A component is never declared continuous, nor
    its locus `COMPLETE`, only because the geometric equation has a double root when the
    indexed output that defines the traced point is undefined there. An unlabelled second
    output suppressed by the inherited rule gets an unresolved band around the coincident root;
    its certified limit value is evidence, never an available output, and no output identity is
    replaced by another because their points coincide. The inherited rule is not changed.
17. **Exact revision fingerprint and reconstruction fidelity (D-CU2-20-R).** The fingerprint
    of a reconstructible definition is versioned and exact: it changes with every
    semantically relevant change of a non-driver input, down to one unit in the last place of
    a binary64 value, and it covers the structural definitions, the authoritative inputs in an
    exact binary64 representation, the selection state that affects the geometry, the label
    state that decides effective outputs, and the versions of the reconstruction policy and
    contract. It is never based on runtime object identity, proximity, a circumstantial
    construction index or a graphical representation, and it never replaces durable
    identities. Driver-only moves keep the revision while the snapshot is unchanged; a real
    transition of an inherited selection state is distinguished from an equivalent recompute.
    Before a certificate is published or an evaluator reused, the isolated slice must be shown
    to reproduce the authoritative inputs exactly; an exact restoration from the live original
    into the copy is admitted only where its correctness is demonstrated, never in the opposite
    direction; an unrestorable discrepancy fails explicitly (existence `NOT_ESTABLISHED` with a
    typed cause, no certificate, evaluations failed with that cause). The fidelity of an
    internal copy and the fidelity of the persisted format are distinct; after reopening, the
    certificate corresponds to the reloaded state. The persisted format does not change. A
    certificate is valid only for the revision whose fingerprint it cites. This is a
    conformance amendment of the `CU1` snapshot contract, authorized inside `CU2`, and an early
    blocking gate of the implementation.
18. **Nature of the evidence.** Each certified fact records the nature of its proof: exact
    symbolic or algebraic, rigorous interval arithmetic, numerical verification under stated
    hypotheses, exploratory, or none. Only the first two certify; the third is an estimate
    under its contract, the fourth is exploratory evidence and the fifth yields `UNRESOLVED`
    or `NOT_ESTABLISHED`.

## Consequences

- B07, H01, B10.2 and B06 are corrected in the author's model: the first family becomes one
  closed periodic component before the bite and one arc across the seam after it; `e_1` and
  `o_1` are `COMPLETE` closed periodic components with certified folds on `e_1`; `e_2` is one
  open component.
- The critical bite value is certified when the configuration represents it exactly: one
  closed periodic component per locus with a certified singular parameter. Every binary64
  value of the author's slider is decided exactly as penetration or bite.
  `NOT_ESTABLISHED` remains the fail-closed result where the exact certificate does not apply
  or exhausts its budget. The claim holds because `{Q, R}` are labelled outputs, defined at
  the double root live and in the slices; the capture checks that precondition.
- A construction whose traced point depends on an unlabelled second output near a coincident
  root stays `NOT_ESTABLISHED` through an unresolved band. Restoring its continuity needs
  either a new kernel capability (continuous selection of coincident roots in the slice) or a
  change of the inherited rule, both outside this decision and subject to their own author
  authorization.
- Point components are declared and never drawn as curves in `CU2`; their materialization
  belongs to `CU3`.
- Revision exactness and reconstruction fidelity are an early blocking gate: characterized in
  step T1 and corrected in step T3, before any certificate of step T4 is valid or published.
  The correction touches `CU1` components as an authorized conformance amendment, without
  reopening the `CU1` closeout and without changing serialization or inherited behaviour.
- The evaluator is not an authority on existence: its tangency tolerance yields points inside
  exact gaps, which are not published as valid.
- Existing tests that depend on the compatibility certificate change as declared conformance,
  never by weakening.
- Persisted selectors that cite component endpoints resolve by structural correspondence
  without being rewritten; ambiguous ones become unresolved with a typed cause.
- The export topology of DXF and LaTeX follows the certificate. A component with an open end
  is exported instead of dropped; unevaluable ends are declared.
- Certification work is bounded by versioned budgets and visible through deterministic
  counters; the exact certificate costs milliseconds per predicate in the author's model.
- Reading the selection state needs read-only accessors in inherited GeoGebra classes.
- `CU3` receives certified boundaries, singular parameters and second-order jets as inputs.
  Structural points, tangent materialization and intersection-engine changes remain in `CU3`.

## Alternatives considered

- **Keep the compatibility certificate and correct only the drawing:** rejected. It keeps a
  false `COMPLETE` claim and a second topology authority.
- **Keep `NOT_ESTABLISHED` as the definitive result at the critical value:** rejected by the
  author's review. A double zero without sign change is neither nonexistence nor
  discontinuity, and it is certifiable exactly.
- **Interval methods for the multiple roots of first-level predicates:** not chosen.
  Ordinary simple-root isolation cannot certify a multiple root, because a double root
  disappears under arbitrarily small perturbations. Verified methods for multiple roots exist,
  but they need added structure (a known multiplicity, deflation, factorization or derivative
  information) or prove that a nearby perturbed problem has a multiple root. The capture gives
  exact rational data, and exact arithmetic decides the multiplicity of the captured
  configuration itself.
- **Treat a point component as an interval of zero length:** rejected. It would enter the arc
  machinery (tessellation, routes, closure) with a degenerate length and be confused with an
  interior singular parameter.
- **Continuous selection of coincident roots in the slice:** not adopted in `CU2`. Evaluating a
  suppressed unlabelled output by its certified limit would define the locus where the live
  object is undefined, a semantic change with respect to the inherited behaviour that needs
  its own authorization.
- **Change the inherited rule that suppresses the second output:** not adopted. It is an
  upstream behaviour change.
- **Substitute the other output when one is undefined at a coincident root:** rejected. It
  replaces an output identity because of a spatial coincidence.
- **Keep the `CU1` fingerprint and an unvalidated reconstruction:** rejected. A selection or
  label state change, or a literal written with fewer digits than an exact round trip needs,
  would let two different configurations share a revision, or let a certificate describe a
  configuration other than the live one.
- **Change the persisted format to carry exact values:** not adopted. Internal exactness is
  achieved by validation and demonstrated restoration; a format change needs its own
  authorization.
- **A tolerance that declares near-tangency as tangency:** rejected. It turns a configuration
  with a real gap into a continuous one.
- **Certify `e_2` through the value of `d_2`** (proposal E of `CU0`): rejected. It would use a
  class-3 length as a rigorous input.
- **Boundaries by bisection of pointwise validity:** rejected. Pointwise validity overshoots
  the exact root by up to 4.3·10⁻⁶ in the author's model, and sampling cannot exclude gaps.
- **Root selection captured from a sample at the current driver:** rejected. It fails when the
  driver starts in a gap and ignores the algorithm's selection state.
- **Fold certification from an enclosure of `F′` that contains zero:** rejected. It does not
  prove that a stationary point exists.
- **Selector migration by unique containment alone, or by lazy rewrite of persisted
  selectors:** rejected. Containment cannot create a correspondence, and a rewrite changes
  persisted user content without its own authorization.
- **Close a component because its driver is periodic:** rejected by the author's instruction
  and by ADR 0027.
- **A cumulative lifted parameter for `e_2`:** rejected. The winding count would depend on the
  drag history.
- **Change the default policy of `LocusLength`:** rejected as a silent semantic change.
- **Two seam intervals instead of arcs:** rejected. Five consumers would keep reconstructing
  the arc and closure from list order.

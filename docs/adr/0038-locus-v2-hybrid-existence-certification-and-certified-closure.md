# ADR 0038 — Locus V2 hybrid existence certification and certified closure

- Status: **PROPOSED** (`PRE-G9B-CU2` preparation candidate pending author review; not
  author approved; the number is provisional until acceptance)
- Date: 2026-10-20
- Phase: `PRE-G9B-CU2` (preparation authorized on 2026-10-20; implementation not authorized)
- Extends: [ADR 0027](0027-locus-v2-existence-and-continuous-valid-components.md). Its four
  axes, its consumer rules and the independence of local admissibility from global
  completeness are preserved unchanged.
- Author decisions:
  - hybrid direction DA-1 (2026-10-19; [`CU0` decision sheet](../validation/pre_g9b_cu0_author_decision_sheet.md) §1);
  - [`CU1` closeout record](../validation/pre_g9b_cu1_closeout_record.md) §7: `CU2`
    preparation authorized; a class-3 length never becomes rigorous evidence of a `CU2`
    component.
- Parent authority:
  - [existence and continuous-valid components](../../geocedg/specs/locus/locus-v2-existence-components.md) v1.0;
  - [certified construction model](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) v1.1;
  - [metric contract](../../geocedg/specs/locus/locus-v2-metrics.md) v1.5, §22, §24.5 and §25;
  - [semantic contract](../../geocedg/specs/locus/locus-v2-semantics.md) v1.2;
  - ADR 0027, ADR 0028, ADR 0029 and ADR 0037.
- Normative candidate: [`CU2` normative proposal](../architecture/pre_g9b_cu2_normative_proposal.md)
  §2–§7 (amendments A2.1-R to A2.6)
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

1. the certified construction model has no ray, no line–circle intersection with a captured
   root branch, no multi-output step, no vectors and no macros, and it cannot tell an
   undecidable box from a uniformly invalid one;
2. components are intervals inside `[−π, π)`, so a component that crosses the seam cannot be
   expressed, and five consumers decide closure with five different predicates;
3. `e_2` depends on `LocusLength`, whose value is class 3 (`ESTIMATED`) and therefore never a
   rigorous input of a certificate (ADR 0037, metrics §25.5).

## Decision

1. **Four evidence levels.** `POINT_VALID`, `EXPLORATORY`, `LOCAL_CERTIFIED` and
   `COMPLETE`. Exploratory evidence is a separate typed structure, derived per revision and
   policy, not persisted and not part of the revision fingerprint. It never becomes a
   component, a completeness claim, an identity or an export.
2. **Component continuity.** A certified component requires the continuity of the traced
   map, besides existence and continuation of the captured selections. A structural
   discontinuity with valid evaluations on both sides is a boundary.
3. **Arcs.** On a periodic domain a component is an arc (canonical start, length, end
   inclusion). A full-length arc whose map is certified continuous at its closing point is a
   closed periodic component; otherwise it is open.
4. **One closure authority.** Every consumer decides closure, seam crossing and periodic
   semantics from the certificate. Neither the provider's `PERIODIC` flag nor the legacy
   component view decides topology.
5. **Class v3.** The certified construction model gains a versioned class with rays and
   segments, circles by centre and point, line–circle intersection with a geometric root rule
   captured per revision, multi-output steps, vector operations, macro expansion, a structural
   metric input and second-order jets. Boxes evaluate to `VALID`, `UNIFORMLY_INVALID` with a
   typed reason, or `UNRESOLVED`; only the second yields certified nonexistence.
6. **Certified transitions.** A boundary between existence and nonexistence is a simple root
   of an identified validity predicate, certified by a sign change and a derivative enclosure
   that excludes zero. End inclusion follows the predicate. Pointwise validity of the evaluator
   never certifies a boundary.
7. **Structural periodicity.** A program whose driver is a point of a circle and whose
   operations are functions of their current inputs defines a function on the circle and may
   claim its periodic identification. The periodicity of the driver alone never closes a
   component.
8. **Structural certificate of a metric function.** A `LocusLength` node in a dependent
   slice contributes its definition domain, its continuity and its `ORIGIN_WRAP` event with a
   certified positive jump, never its value. `e_2` is certified as one open component cut at
   the origin of the oriented length; its coordinates remain estimates under the `CU1`
   estimate contract. `LocusLength` values and policies do not change.
9. **Computational undefinedness.** A failed computation (a class-4 metric, an estimate above
   its tolerance, an exhausted budget) yields `UNRESOLVED` coverage when it decides existence,
   and only a point evaluation status when existence is already certified. It never yields
   certified nonexistence.
10. **Publication.** A locus is published when its definition inputs are defined. The traced
    point at the current driver position never decides publication. A recompute that does not
    publish discards the previous definition.
11. **Structural lineage.** Component lineage derives from the sources of its boundaries.
    Persisted component selectors resolve by unique containment and are rewritten lazily, with
    fail-closed behaviour when containment is not unique.
12. **Retirement of the compatibility certificate.** It is removed only after the hybrid
    certifier, the consumer migration and the closure authority are in place. A locus that
    cannot be certified falls to `NOT_ESTABLISHED`, never back to compatibility.

## Consequences

- B07, H01, B10.2 and B06 are corrected in the author's model: the first family becomes one
  closed periodic component before the bite and one arc across the seam after it; `e_1` and
  `o_1` are `COMPLETE` closed periodic components; `e_2` is one open component.
- The critical bite value and its neighbourhood remain `NOT_ESTABLISHED` with an explicit
  unresolved band: the certificate fails closed at a double root.
- Existing tests that depend on the compatibility certificate change as declared conformance,
  never by weakening.
- Persisted selectors that cite component endpoints migrate by containment; ambiguous ones
  become unresolved with a typed cause.
- The export topology of DXF and LaTeX follows the certificate. A component with an open end
  is exported instead of dropped.
- Certification work is bounded by versioned budgets and visible through deterministic
  counters.
- `CU3` receives certified boundaries and second-order jets as inputs. Structural points,
  tangent materialization and intersection-engine changes remain in `CU3`.

## Alternatives considered

- **Keep the compatibility certificate and correct only the drawing:** rejected. It keeps a
  false `COMPLETE` claim and a second topology authority.
- **Certify `e_2` through the value of `d_2`** (proposal E of `CU0`): rejected. It would use a
  class-3 length as a rigorous input.
- **Boundaries by bisection of pointwise validity:** rejected. Pointwise validity overshoots
  the exact root by up to 4.3·10⁻⁶ in the author's model, and sampling cannot exclude gaps.
- **Close a component because its driver is periodic:** rejected by the author's instruction
  and by ADR 0027.
- **A cumulative lifted parameter for `e_2`:** rejected. The winding count would depend on the
  drag history.
- **Change the default policy of `LocusLength`:** rejected as a silent semantic change.
- **Two seam intervals instead of arcs:** rejected. Five consumers would keep reconstructing
  the arc and closure from list order.

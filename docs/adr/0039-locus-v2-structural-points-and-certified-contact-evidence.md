# ADR 0039 — Locus V2 structural points and certified contact evidence

- Status: **ACCEPTED — DESIGN AUTHOR APPROVED WITH THE PRECISIONS OF 2026-10-24** (author
  instruction "PRE-G9B-CU3 — Aprobación autoral del diseño e implementación por incrementos",
  2026-10-24; [approval record](../validation/pre_g9b_cu3_author_design_approval_record.md)). This
  is an architectural decision. Its implementation, authorized in two increments with blocking
  gates on the exact base `80d72a50557a82a7a9d501a26252d1758ac6d94d`, and the text incorporated
  into the specifications at step T0 are not approved as product until the author reviews the
  frozen technical candidate. The approval does not certify in advance the viability of exact
  evaluation or its budgets: gate G-M3 decides them.
- Date: 2026-10-24 (proposed; revised by the author-requested focal scientific review and by the
  final scientific coherence check; accepted the same day)
- Phase: `PRE-G9B-CU3`. Preparation was authorized on 2026-10-24 on the exact base
  `e6c908f2203cd8feb5d4938013e16b212e0fe016` and published for review at `80d72a50`;
  implementation was authorized on 2026-10-24 on that exact base.
- Extends: [ADR 0027](0027-locus-v2-existence-and-continuous-valid-components.md) and
  [ADR 0038](0038-locus-v2-hybrid-existence-certification-and-certified-closure.md). Their existence
  certificates, component queries (including the point-component rule of
  `locus-v2-existence-components.md` §15.4), single closure authority and the independence of
  local admissibility from global completeness are preserved unchanged.
- Builds on: [ADR 0008](0008-locus-v2-intersection-result-and-continuation.md) and its G8B-R1
  clarification. Its point-admissibility predicate is preserved, extended by a certified path and
  restricted only by the conflict rule of decision 5.
- Amends:
  - [ADR 0017](0017-deterministic-intersection-phase-rank-identity.md): a tangent-contact germ
    alongside the unchanged transverse germ;
  - [ADR 0010](0010-role-gated-spatial-authority-and-durable-identity.md): reserved axis references
    as non-ordinary participants, under option A of decision D-CU3-8, which the author approved.
- Superseded in part by: [ADR 0040](0040-constructive-geometric-authority-and-relation-integrity.md)
  (`PRE-G9B-CU3`, architecture approved 2026-10-25): in documents governed by
  `constructive-authority/v1`, the authority of the locus in its captured configuration and of
  the target is replaced by the constructive authority (decision D-AUT-11); in every other
  document this decision stands unchanged
- Author decisions it relies on:
  - DA-3 (2026-10-19, stage option C), H02 and H06 ([consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md) §2);
  - `OBS-CU0-CU3-TANGENT-EXISTENCE` (2026-10-20): F-2 is not approved as formulated;
  - the `CU3` preparation authorization and its precision on the driver correspondence;
  - the focal scientific review instruction;
  - the final scientific coherence check;
  - the design approval and implementation authorization (all 2026-10-24).
- Normative text: [`CU3` normative proposal](../architecture/pre_g9b_cu3_normative_proposal.md),
  amendments A3.0 to A3.5, incorporated at step T0 into `locus-v2-intersections.md` 1.3,
  `locus-v2-certified-construction-model.md` 1.3 (§11), `locus-v2-public-surface.md` §21,
  `locus-v2-point-interaction.md` §18, `locus-v2-existence-components.md` §16 and
  `g9-spatial-projection-semantics.md` §8.3.
- Evidence:
  - [initial-state and capability analysis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md);
  - [characterization](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json);
  - [validation matrix](../validation/pre_g9b_cu3_validation_matrix.md).
- Decision sheet: [`CU3` author decision sheet](../validation/pre_g9b_cu3_author_decision_sheet.md).

## Context

The author needs the three mandatory contacts of the author model as dynamic points with both
incidences established: `Intersect(e_1, n)`, `Intersect(e_1, c_1)` and `Intersect(o_1, h_2)`.
The current engine materializes only transverse roots. The `CU0` proposal F-2 would have
materialized a tangency on the evidence of a certified critical point with a residual within a
tolerance. The author rejected that evidence: a small residual at a certified critical point does
not prove a root.

The `CU3` characterization and the focal review establish seven facts.

1. **No term identity.** In the author model none of the three incidences is a syntactic term
   identity. All three follow from one theorem of the model, `J_1(K) = M_1`, proved for both
   output branches of `H_1`. The same theorem makes the seam contact of `e_1` with `c_1` a
   structural contact.
2. **Class 3 everywhere.** Interval enclosures classify all four contacts as numerically
   compatible: the residual enclosure at the certified critical point contains zero, as it must
   for an exact double root computed in binary64.
3. **Folds, not tangencies.** Only `o_1 · h_2` is a regular tangency. The contacts of `e_1` lie at
   the folds of its trace certified by `CU2` (`F′ = 0` at `t = 0` and `t = π`). The arc ends there
   and meets `n` and `c_1` at about 58° and 32°.
4. **Engine defects.** The current engine accepts and classifies roots with unenclosed binary64
   values. Results that the approved G8B-R1 predicate admits or publishes contradict certified
   evidence:
   - noise roots near contacts (H-CU3-1);
   - double-root contacts classified as simple transverse roots (H-CU3-2);
   - tangencies where absence is certified (H-CU3-3);
   - duplicated preimages (H-CU3-4).

   This conflicts with `locus-v2-intersections.md` §5.1.
5. **Correspondence must be proved.** `K = Intersect(p, i, 2)` is not a driver and has no
   parameter. Its correspondence with the driver parameter must be established, not assumed.
6. **Rounding breaks captured relations.** The binary64 state of `K` is `fl(B.x + 5)`, which
   differs from the exact `B.x + 5` by `4.44·10⁻¹⁶`. A configuration that captured intermediate
   objects at their binary64 state would lose the structural incidence, and exact evaluation would
   refute a theorem.
7. **Interval evidence is existential.** Intervals certify the existence of some root in an
   interval (sign change and continuity), but not an equality at a prescribed parameter. The
   earlier wording of this preparation blurred that distinction.
8. **Two configurations, one object.** The final coherence check points out that an incidence
   proved in a joint exact recomputation `Σ_B` does not transfer automatically to the point of `L`
   in its `CU2` captured configuration `Σ_L`. The two configurations differ: for example, the
   captured radius of `c_1` is the one of its matrix, `√(A4² + A5² − A2)`, not the exact `|HD_1|`.
   A reading of the inherited arithmetic shows that the relations the theorem needs (vertical `k`
   through `H`, horizontal `h` and `i`, the centre of `c_1` on `k`, `g ∥ k`) hold exactly in the
   binary64 states that `Σ_L` captures in the author model.
9. **Certified critical points do not contradict.** A certified critical point alone does not
   contradict a locally verified root: it leaves no root, one double root or two simple roots
   possible.

## Decision

1. **Kinds of evidence** (A3.0).
   - Interval enclosures certify existence of some root in a bracket (certified sign change on a
     certified-valid bracket, or interval Newton, Krawczyk or Miranda) and strict inequalities.
   - They never decide an equality at a prescribed parameter or point (an incidence of a given
     point, a vanishing residual at a given parameter, a double root, the equality of two points).
   - Such an equality is established structurally or by exact arithmetic in a declared
     configuration. The two are recorded as distinct evidence.
2. **Contact evidence and materialization** (A3.2-R).
   - **Classes.** Contacts without a local sign change have five classes: structural; exact;
     numerically compatible; certified critical point without intersection or with two roots; and
     not established.
   - **Materialization.** Only the first two are materialized as intersections (option A). For a
     root of even multiplicity, "established" in G8B-R1 means one of those two classes. This is a
     precision with no effect on the current product, where no one-parameter tangent root reaches
     established isolation.
   - **Evidence kept.** Classes 3 to 5 stay in the rich result with their evidence and as
     diagnostics.
   - **Not in `CU3`.** A public certified-critical-point object is a future option.
   - **Contact order.** Contact order is separate from existence and is geometric only at a
     regular source parameter. A certified fold gives `FOLD_CONTACT`, whose perturbed parameter
     roots are two preimages of one point carried as one identity.
3. **Tangent-contact germ** (A3.2-R). A new versioned germ and selector are introduced for
   materialized contacts, with the lifecycle `CURRENT`, `CONTACT_LOST`, `CONTACT_SPLIT`,
   `CONTACT_NOT_CERTIFIED` and `IDENTITY_DISCONTINUITY`. No token is transferred. Transverse germs
   and existing ledger formats are unchanged. The germ is needed only for driverless
   materialization (increment `CU3-B`).

   *Amendment of 2026-10-27 (author decisions CU3-B-R1 to R6,
   [decision sheet](../architecture/pre_g9b_cu3_b_decision_sheet.md)).*

   - **Germ composition.** The germ holds structural identity only: a versioned germ type, the
     component lineage, the target and either the exact fold label (involution and certified fixed
     point) of a fold-anchored contact or the side of contact of a regular tangency. Evidence class,
     contact order, existence grade, integrity, certificates, cells, enclosures, numeric parameters
     and the decision configuration are revision evidence and never enter it (consistent with
     decision 5).
   - **Fold contacts.** A certified fold contact is one solution anchored on the fold, whose
     identity is kept through the transition between its two simple preimages and the exact double
     root (decision 2). Transverse tokens of a certified pair are never fused or reassigned: they
     stop being current with a typed cause and stay latent until the same full selector is
     current again.
   - **Ledger.** The token ledger writes a new state version 6, with an explicit entry-kind
     discriminator, only when it holds a contact entry. Documents without contacts keep versions 4
     and 5, and earlier readers reject version 6 closed.
   - **Increments.** Fold contacts are increment `CU3-B1`; regular tangencies are increment
     `CU3-B2`, not yet authorized.
4. **Structural points by driver correspondence** (A3.3-R). `Point(L, D)` and
   `Intersect(L, r, D)` are defined by four facts checked in order, each failing closed with a
   typed cause:
   - the provenance of `D`, from a closed table, with the exact output selected by the exact
     inherited selection rule;
   - the identity of the driver support;
   - the exact correspondence `t*(D)`, known exactly for path and defining points and enclosed
     from a certified enclosure of the exact point for intersection outputs. The binary64
     coordinates never certify it, and its numerical value serves only display;
   - the enclosure in the interior of a certified continuous component, with fail-closed rules at
     domain ends, seams, component ends, transitions, unresolved bands, invalid gaps and point
     components.

   Point components are not materialized: `CU2` §15.4 forbids a public point there, and C22 and
   C23 are not introduced. The authoritative object is `L` in its `CU2` captured configuration
   `Σ_L`. `Intersect(L, r, D)` materializes **the same exact point** as `Point(L, D)`, and its
   incidence with `r` is an equality at a prescribed point, decided only by term identity or by
   exact evaluation in `Σ_L⁺` (decision 6). The live driver is never moved, and nothing numerical
   is persisted.
5. **Evidence grades, admissibility and consumption** (A3.1-R).
   - **Grades.** Every finite solution carries a grade: `LOCALLY_VERIFIED` (G8B-R1, numerical
     verification under hypotheses), `EXISTENCE_CERTIFIED`, `EXISTENCE_STRUCTURAL` or
     `EXISTENCE_EXACT`.
   - **Admissibility.** The G8B-R1 predicate and thresholds are unchanged. A certified path admits
     `EXISTENCE_CERTIFIED` transverse roots whose residual is consistent with a translation-invariant
     characteristic scale (H02, additive).
   - **Conflict rule.** A locally verified solution is withdrawn or reclassified only when a
     root-count certificate in `Σ_L⁺` really contradicts its existence, multiplicity or
     classification. The certificate gives the exact number of roots and their location on a box
     with certified continuity: none, one simple, two simple around a certified critical point, or
     one double at an exact point. A certified critical point alone, a class-3 candidate or a
     contradiction of set completeness alone never withdraws a solution; the last one lowers the
     completeness. This is the only restriction, and it is identified.
   - **Consumption.** A consumption policy fixes the minimum grade per consumer. Grades are
     revision evidence, never promoted silently, and not part of tokens.
   - **Pairs.** Pair acceptance is unchanged in `CU3`; its origin-distance scale is a recorded
     nonconformity with §8.1.
6. **Exact evaluation in the authoritative configuration** (A3.5).
   - **Configuration.** M3 decides in `Σ_L⁺`, which contains the `CU2` captured configuration of
     `L` unchanged: the same constants, with the exact meaning that the class-v3 program gives
     them, also when a constant is the target. Only the driver point, the target and their
     ancestors outside `Σ_L` are added. Shared ancestors of captured constants and boundary
     objects are taken at their binary64 states, and every other added object is recomputed
     exactly, so that structural relations among recomputed objects survive. Each object has
     exactly one exact meaning; a conflict fails closed.
   - **No transfer.** A decision in `Σ_L⁺` is a property of the authoritative point of `L`. A
     deeper joint recomputation `Σ_B` that also recomputes the captured constants is diagnostic
     only. It applies to the authoritative point only if both configurations are proved to give
     the same exact value to every object used.
   - **Exact driver point.** It is reconstructed from its provenance and the exact inherited
     selection, on the driver meaning of its support, and the slice is evaluated at that point.
   - **Outcomes.** Signs and vanishing, and first derivatives when needed, are decided exactly:
     proved, refuted, out of class or out of budget, never "equal within a tolerance". A
     refutation in `Σ_L⁺` while `Σ_B` proves is reported as
     `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`, and the intersection stays undefined.
   - **Accidental equalities.** An accidental equality is proved exactly when it holds exactly in
     that configuration.
   - **Authority of the target** (author precision of 2026-10-24). For every incidence decided,
     the exact meaning of the target is its authority under `locus-v2-intersections.md` §2.1: the
     current line coefficients up to a proved nonzero factor; for segments and rays, also their
     limited part; for circles, the representation of the target adapter, distinct from the
     captured matrix, the half axes and numerical evaluations. A reconstruction of the target
     never replaces its binary64 coefficients without a proved exact correspondence. Otherwise the
     incidence fails closed, and the meaning of a target is never redefined to make it hold.
   - **Gate.** An early blocking gate precedes the implementation of the second incidence:
     mathematical viability in `Σ_L⁺` on the five mandatory contacts and controls, including the
     adversarial case that is proved only in `Σ_B` and the arithmetic hypotheses of the captured
     constants; budget by counters, approved by the author when the contract requires it;
     correspondence with the live algorithm and exact reconstruction of the selection; coherence of
     configurations; and the authority of the target (G-M3-4).
   - **Failure.** If M3 fails, its evidence is never replaced by a residual or by a `Σ_B` proof.
     The alternatives go to the author, among them amending the `CU2` capture convention (C3),
     which this ADR does not do.
7. **Axes** (A3.4-R). Under option A, `xAxis` and `yAxis` become reserved kernel references with
   stable identity, admitted as unbounded line targets. The identity failure
   `GEO_NOT_SERIALIZABLE`, the cause of H06, is addressed in the identity registry, not by
   substituting an ordinary line.
8. **Placement.** All of the above belongs to the shared Java kernel (`AGENTS.md` §4). Probes,
   benches and symbolic cross-checks stay outside it as evidence.

## Consequences

- **Mandatory contacts.** The three mandatory contacts and the seam contact become
  materializable through `Intersect(L, r, K)` and `Intersect(L, r, J)` with structural, dynamic
  identity, provided that the M3 gate passes, including the authority of the target. Without it
  only `Point(L, D)` is materializable for them, and the author decides among the alternatives of
  the gate.
- **Driverless form.** `Intersect(L, r)` without a driver keeps the four contacts as class 3 in
  the minimal increment. Increment `CU3-B` raises them to class 2 through the certified folds of
  `CU2` and M3 at `K*` and `J*`.
- **Documents saved before `CU3`.** Previously admissible roots stay admissible, except solutions
  contradicted by a root-count certificate. Their tokens stop resolving with a typed cause:
  - certified absence;
  - duplicated preimages that do not keep the identity;
  - with M3, the seam root of `e_1 · c_1`, the noise roots near the structural contact and
    transverse-classified endpoint tangencies.

  Without M3, those last ones keep their G8B-R1 state.

  Additive admissions (H02) create new tokens. `AGENTS.md` §11.6 requires decision D-CU3-7, a
  migration note and regression evidence.
- **Current readers.** The new syntax is rejected by current readers, which fail closed (P10).
- **Cost.** Exact evaluation adds a kernel capability with versioned budgets and deterministic
  counters. Its constants come from the gate bench.

## Alternatives considered

- **F-2 as formulated in `CU0`**, and a visible "contact within ε" state as an intersection:
  rejected.
- **Restricting G8B-R1 admissibility to certified roots**, the first version of this preparation:
  withdrawn. It would have restricted an approved capability without contradiction evidence.
- **Marking contradicted solutions with a diagnostic only:** keeps full compatibility, but leaves
  transverse classifications that a proof contradicts. Not recommended.
- **Making `K` a driver**, or moving the live driver to `D`: rejected.
- **Synthetic incidence rules** (M2): not recommended.
- **The GeoGebra symbolic prover as authority** (M4): rejected as authority; possible evidence.
- **Exact evaluation over a per-object capture**, taking every intermediate object at its binary64
  state: rejected. Rounding of intermediate objects (`K.x = fl(B.x + 5)`) breaks the structural
  relations and would refute theorems.
- **A deep joint recomputation `Σ_B` as the authority**, the configuration of the focal review:
  rejected. It recomputes the captured constants of `L`, so its point is not the point that `CU2`
  certifies, and using it would silently reinterpret the `CU2` certificates.
- **Transferring a `Σ_B` proof to `Σ_L` by numerical closeness**, or by a certified bound on the
  distance between both points: rejected. A positive bound proves no equality.
- **Amending the `CU2` capture convention** so that the captured configuration of `L` is the deep
  recomputation (C3): not in `CU3`. It changes an approved contract and needs new certificates.
- **Treating a certified critical point, or a class-3 candidate, as a contradiction:** rejected.
  It leaves no root, one double root or two simple roots possible.
- **Changing the residual-adapter contract text** to carry the new scale: rejected. It would
  invalidate every persisted root token.
- **Materializing points on point components in `CU3`:** rejected. `CU2` §15.4 forbids a public
  point there, and it would introduce C22 and C23 implicitly.
- **A public certified-critical-point object in `CU3`:** deferred as a future option.
- **Substituting an ordinary line for an axis:** rejected.

## Amendment of 2026-10-28 (CU3-B2, decision 3)

Approved by the author decisions of 2026-10-28 (G-B2-0 with precisions; G-B2-2 authorized). The
regular tangent germ named in the amendment of 2026-10-27 is defined by the CU3-B2 amendment of
`locus-v2-intersections.md` (version 1.6). Its side is the certified sign of the relative curvature
with the canonical target orientation. A regular tangent contact token is never transferred at a
split. The contact is materialized only for class 1 or 2 evidence with the regularity preconditions
of that amendment; no new class of exact decisions is introduced.

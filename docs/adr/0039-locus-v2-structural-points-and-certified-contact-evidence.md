# ADR 0039 — Locus V2 structural points and certified contact evidence

- Status: **PROPOSED — PRE-G9B-CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW**, revised by the
  author-requested focal scientific review of 2026-10-24. `selfApproved=false`. It changes no
  accepted ADR or specification until the author approves it.
- Date: 2026-10-24
- Phase: `PRE-G9B-CU3`. Preparation was authorized on 2026-10-24 on the exact base
  `e6c908f2203cd8feb5d4938013e16b212e0fe016`; the preparation candidate `213ea5aa` was published for
  review, and the focal review was authorized the same day. Implementation is not authorized.
- Extends: [ADR 0027](0027-locus-v2-existence-and-continuous-valid-components.md) and
  [ADR 0038](0038-locus-v2-hybrid-existence-certification-and-certified-closure.md). Their existence
  certificates, component queries (including the point-component rule of
  `locus-v2-existence-components.md` §15.4), single closure authority and the independence of
  local admissibility from global completeness are preserved unchanged.
- Builds on: [ADR 0008](0008-locus-v2-intersection-result-and-continuation.md) and its G8B-R1
  clarification. Its point-admissibility predicate is preserved, extended by a certified path and
  restricted only by the conflict rule of decision 5.
- Amends, if accepted:
  - [ADR 0017](0017-deterministic-intersection-phase-rank-identity.md): a tangent-contact germ
    alongside the unchanged transverse germ;
  - [ADR 0010](0010-role-gated-spatial-authority-and-durable-identity.md): reserved axis references
    as non-ordinary participants, only under option A of decision D-CU3-8.
- Author decisions it relies on:
  - DA-3 (2026-10-19, stage option C), H02 and H06 ([consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md) §2);
  - `OBS-CU0-CU3-TANGENT-EXISTENCE` (2026-10-20): F-2 is not approved as formulated;
  - the `CU3` preparation authorization and its precision on the driver correspondence;
  - the focal scientific review instruction (all 2026-10-24).
- Normative text: [`CU3` normative proposal](../architecture/pre_g9b_cu3_normative_proposal.md),
  amendments A3.0 to A3.5.
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
   C23 are not introduced. The incidence with `r` is an equality at a prescribed point, decided
   only by term identity or by exact evaluation. The live driver is never moved, and nothing
   numerical is persisted.
5. **Evidence grades, admissibility and consumption** (A3.1-R).
   - **Grades.** Every finite solution carries a grade: `LOCALLY_VERIFIED` (G8B-R1, numerical
     verification under hypotheses), `EXISTENCE_CERTIFIED`, `EXISTENCE_STRUCTURAL` or
     `EXISTENCE_EXACT`.
   - **Admissibility.** The G8B-R1 predicate and thresholds are unchanged. A certified path admits
     `EXISTENCE_CERTIFIED` transverse roots whose residual is consistent with a translation-invariant
     characteristic scale (H02, additive).
   - **Conflict rule.** Certified evidence prevails over a contradicted local verification; that is
     the only restriction, and it is identified.
   - **Consumption.** A consumption policy fixes the minimum grade per consumer. Grades are
     revision evidence, never promoted silently, and not part of tokens.
   - **Pairs.** Pair acceptance is unchanged in `CU3`; its origin-distance scale is a recorded
     nonconformity with §8.1.
6. **Exact evaluation in a joint configuration** (A3.5).
   - **Configuration.** The joint subgraph of the driver point, the slice and the target is
     recomputed exactly from one capture boundary (independent elements, path parameters, and
     objects whose exact computation would leave the class), so that structural relations among
     recomputed objects survive.
   - **Exact driver point.** It is reconstructed from its provenance and the exact inherited
     selection, and the slice is evaluated at that point.
   - **Outcomes.** Signs and vanishing are decided exactly: proved, refuted, out of class or out of
     budget, never "equal within a tolerance". Its scope is that joint configuration.
   - **Accidental equalities.** An accidental equality is proved exactly when it holds exactly in
     that configuration.
   - **Gate.** An early blocking gate (mathematical viability on the five mandatory contacts and
     controls, budget by counters, correspondence with the live algorithm) precedes the
     implementation of the second incidence.
   - **Failure.** If M3 fails, its evidence is never replaced by a residual; the alternatives go to
     the author.
7. **Axes** (A3.4-R). Under option A, `xAxis` and `yAxis` become reserved kernel references with
   stable identity, admitted as unbounded line targets. The identity failure
   `GEO_NOT_SERIALIZABLE`, the cause of H06, is addressed in the identity registry, not by
   substituting an ordinary line.
8. **Placement.** All of the above belongs to the shared Java kernel (`AGENTS.md` §4). Probes,
   benches and symbolic cross-checks stay outside it as evidence.

## Consequences

- **Mandatory contacts.** The three mandatory contacts and the seam contact become
  materializable through `Intersect(L, r, K)` and `Intersect(L, r, J)` with structural, dynamic
  identity, provided that the M3 gate passes. Without it only `Point(L, D)` is materializable for
  them, and the author decides among the alternatives of the gate.
- **Driverless form.** `Intersect(L, r)` without a driver keeps the four contacts as class 3 in
  the minimal increment. Increment `CU3-B` raises them to class 2 through the certified folds of
  `CU2` and M3 at `K*` and `J*`.
- **Documents saved before `CU3`.** Previously admissible roots stay admissible, except solutions
  contradicted by certified evidence. Their tokens stop resolving with a typed cause:
  - the seam root of `e_1 · c_1` in the author model;
  - the noise roots;
  - transverse-classified endpoint tangencies;
  - duplicated preimages.

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
- **Changing the residual-adapter contract text** to carry the new scale: rejected. It would
  invalidate every persisted root token.
- **Materializing points on point components in `CU3`:** rejected. `CU2` §15.4 forbids a public
  point there, and it would introduce C22 and C23 implicitly.
- **A public certified-critical-point object in `CU3`:** deferred as a future option.
- **Substituting an ordinary line for an axis:** rejected.

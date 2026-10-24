# ADR 0039 — Locus V2 structural points and certified contact evidence

- Status: **PROPOSED — PRE-G9B-CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW**.
  `selfApproved=false`. It changes no accepted ADR or specification until the author approves it.
- Date: 2026-10-24
- Phase: `PRE-G9B-CU3`. Preparation was authorized on 2026-10-24 on the exact base
  `e6c908f2203cd8feb5d4938013e16b212e0fe016`. Implementation is not authorized.
- Extends: [ADR 0027](0027-locus-v2-existence-and-continuous-valid-components.md) and
  [ADR 0038](0038-locus-v2-hybrid-existence-certification-and-certified-closure.md). Their
  existence certificates, component queries, single closure authority and the independence of
  local admissibility from global completeness are preserved unchanged.
- Amends, if accepted:
  - [ADR 0017](0017-deterministic-intersection-phase-rank-identity.md): a tangent-contact germ
    alongside the transverse germ, which is unchanged;
  - [ADR 0010](0010-role-gated-spatial-authority-and-durable-identity.md): reserved axis references
    as non-ordinary participants, only under option A of decision D-CU3-8.
- Author decisions it relies on:
  - DA-3 (2026-10-19, stage option C), H02 and H06 ([consolidated plan](../architecture/pre_g9b_cu_corrective_usability_plan.md) §2);
  - `OBS-CU0-CU3-TANGENT-EXISTENCE` (2026-10-20): F-2 is not approved as formulated;
  - the `CU3` preparation authorization and its precision on the driver correspondence
    (2026-10-24).
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

The `CU3` characterization adds four facts.

1. In the author model none of the three incidences is a syntactic term identity. All three
   follow from one geometric theorem of the model, `J_1(K) = M_1` (proved for both output branches
   of `H_1`). The same theorem makes the seam contact of `e_1` with `c_1` a structural tangency.
2. Interval enclosures classify all four contacts as numerically compatible (class 3): the
   residual enclosure at the certified critical point contains zero, as it must for an exact
   double root computed in binary64. Only `o_1 · h_2` is a regular tangency. The contacts of
   `e_1` lie at the folds of its trace certified by `CU2` (`F′ = 0` at `t = 0` and `t = π`). The
   arc ends there and meets `n` and `c_1` at about 58° and 32°.
3. The current engine, using unenclosed binary64 sign changes and residuals:
   - publishes noise roots near tangencies, some of them admissible (H-CU3-1);
   - classifies tangencies at the periodic seam and at an included endpoint as transverse and
     admissible (H-CU3-2);
   - declares tangencies where no intersection is certified (H-CU3-3);
   - duplicates preimages (H-CU3-4).

   This contradicts the approved rule that neither tangency nor multiplicity follows from a small
   residual alone (`locus-v2-intersections.md` §5.1).
4. `K = Intersect(p, i, 2)` is not a driver and has no parameter. It lies on the driver support
   `p` by provenance only, and the correspondence `K ↦ t(K)` must be established, not assumed.

## Decision

1. **Open and closed conditions** (A3.0). Interval enclosures certify strict inequalities only.
   An incidence, a vanishing residual, a multiple root or the equality of two points is
   established only by construction provenance, an exact structural relation or exact
   arithmetic with a declared scope. No tolerance turns numerical evidence into an incidence.
2. **Contact evidence classes** (A3.2-R). The five classes are:
   - structural;
   - existence certified by exact means;
   - numerically compatible;
   - certified critical point without intersection or with two roots;
   - not established.

   The existence of the point and its contact order are separate. Only the first two classes are
   materialized as intersections. Under the recommended option B, a certified critical point may
   be materialized as a distinct point of the source that asserts no incidence.
3. **Tangent-contact germ** (A3.2-R). A new versioned germ and selector are introduced for
   tangent contacts, with the lifecycle `CURRENT`, `CONTACT_LOST`, `CONTACT_SPLIT`,
   `CONTACT_NOT_CERTIFIED` and `IDENTITY_DISCONTINUITY`. No token is ever transferred.
   Transverse germs, their versions and the existing token ledger formats are unchanged.
4. **Structural points by driver correspondence** (A3.3-R). `Point(L, D)` and
   `Intersect(L, r, D)` are defined by four facts checked in order, each failing closed with a
   typed cause:
   - the provenance of `D` from a closed table;
   - the identity of the driver support;
   - the existence, uniqueness and orientation of `D ↦ t(D)` for the provider family;
   - the evaluation in a certified component and, for `Intersect`, the incidence with `r` by
     class 1 or 2 evidence.

   The live driver is never moved, and no parameter is persisted.
5. **Certified acceptance and normalization** (A3.1-R). A transverse root is accepted on a
   certified sign change with a derivative enclosure that excludes zero. The residual normalized
   by a translation-invariant characteristic scale is a consistency check with emitted
   diagnostics. Deduplication uses overlapping certified brackets. Pair residuals no longer use
   the distance from the origin. The policy is versioned, and the residual-adapter contract text
   inside persisted tokens is unchanged.
6. **Exact evaluation** (A3.5). Ruler-and-compass class-v3 programs over captured constants may
   be evaluated exactly at an exact driver point with constructive root-separation bounds. It is
   the authority for the second incidence when term identity does not suffice.
7. **Axes** (A3.4-R). Under option A, `xAxis` and `yAxis` become reserved kernel references with
   stable identity, admitted as unbounded line targets. The identity failure
   `GEO_NOT_SERIALIZABLE` identified as the cause of H06 is addressed in the identity registry, not
   by substituting an ordinary line.
8. **Placement.** All of the above belongs to the shared Java kernel (`AGENTS.md` §4). Probes,
   benches and symbolic cross-checks stay outside it as evidence.

## Consequences

- The three mandatory contacts and the seam contact become materializable through
  `Intersect(L, r, K)` and `Intersect(L, r, J)` with structural, dynamic identity, provided that
  exact evaluation is implemented. Without it only `Point(L, D)` is materializable for them.
- `Intersect(L, r)` without a driver keeps them as class 3 unless exact symmetry and exact
  evaluation certify them (class 2).
- Documents saved before `CU3` may change in three ways, which decision D-CU3-7 governs:
  - roots previously rejected at large scale become admissible, which is additive;
  - spurious noise roots disappear;
  - tangencies misclassified as transverse lose their transverse tokens, and their derived points
    become undefined with a typed cause.

  `AGENTS.md` §11.6 requires a compatibility decision, a migration note and regression evidence.
- The new syntax is rejected by current readers, which fail closed (P10).
- Exact evaluation adds a kernel capability with versioned budgets and deterministic counters.
  Its cost must be benchmarked before its constants are fixed.

## Alternatives considered

- **F-2 as formulated in `CU0`** (`TANGENT_CERTIFIED_ISOLATED` on a residual within `ε`):
  rejected by the author and by the open/closed principle.
- **A visible "contact within ε" state as an intersection** (option C of D-CU3-5): rejected for
  the same reason.
- **Making `K` a driver**, by redefining the locus on an intersection output: rejected. It would
  change the semantics of the existing locus and assumes the correspondence instead of proving it.
- **Moving the live driver to `D`** to evaluate the point (correspondence route b): rejected. It
  mutates user state and gives the same numerical value as the canonical projection.
- **Synthetic incidence rules** (M2): not recommended. The rule base is always incomplete; the
  author model already needs reflections and symmetries.
- **The GeoGebra symbolic prover as authority** (M4): rejected as authority. It depends on an
  external computer-algebra system, gives a "generally true" result with non-degeneracy conditions
  and is not deterministic in time. It remains possible evidence.
- **Changing the residual-adapter contract text** to carry the new scale: rejected. It would
  invalidate every persisted root token.
- **Substituting an ordinary line for an axis:** rejected. It would create a hidden object and
  infer identity from coordinates.

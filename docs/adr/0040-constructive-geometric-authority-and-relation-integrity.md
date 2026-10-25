# ADR 0040 — Constructive geometric authority and relation integrity

- Status: **ACCEPTED — ARCHITECTURE AUTHOR APPROVED (2026-10-25)** (author instruction
  "PRE-G9B-CU3 — Aprobación autoral de la autoridad constructiva e implementación por
  incrementos", 2026-10-25;
  [approval record](../validation/pre_g9b_cu3_constructive_authority_approval_record.md)). This is
  an architectural decision. Its implementation, authorized in increments E1 to E4 with blocking
  gates on the exact base `3cd0f710c370c3985ea536f1cc5edcadeca930ee`, and the text incorporated
  into the specifications at increment E0 are not approved as product until the author reviews the
  frozen technical candidate. The approval does not turn the results of the research prototypes
  into a verified product implementation.
- Date: 2026-10-25 (proposed on 2026-10-24 in the focal consolidation, conciliated on 2026-10-25;
  accepted the same day)
- Phase: `PRE-G9B-CU3` (implementation stopped at step T2 by gate G-M3-4 on 2026-10-24; resumed
  from T2 by this decision)
- Amends: [ADR 0008](0008-locus-v2-intersection-result-and-continuation.md),
  [ADR 0013](0013-public-locus-v2-surface-and-token-selection.md),
  [ADR 0017](0017-deterministic-intersection-phase-rank-identity.md),
  [ADR 0028](0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md),
  [ADR 0037](0037-locus-v2-metric-guarantee-classes-and-admissibility.md) and
  [ADR 0038](0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), additively;
  supersedes the authority clauses of
  [ADR 0039](0039-locus-v2-structural-points-and-certified-contact-evidence.md) only in documents
  governed by `constructive-authority/v1`.
- Normative contracts:
  [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md)
  (texts AUT-1, AUT-2, AUT-3, AUT-5 and AUT-6); the constructive authority amendments (version 1.4)
  of [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) and
  [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md)
  (texts AUT-4 and AUT-7); the consumer note of
  [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md)
  §17.
- Design and evidence:
  [normative proposal](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md),
  conciliated on 2026-10-25 and approved with its section 14;
  [constructive authority study](../architecture/pre_g9b_cu3_constructive_authority_study.md);
  [gate G-M3 report](../validation/pre_g9b_cu3_m3_gate_report.md).

## Context

Gate G-M3 of PRE-G9B-CU3 showed that two approved contracts take a representation as geometric
authority: the target by its binary64 state and the locus by its CU2 captured configuration.
Under that authority the five mandatory contacts of the author model are exactly refuted,
while a constructive semantics makes them exact. The binary64 representation contradicts 66 of
the 160 provenance relations of the model; a circle carries three distinct exact radii; a
reconstructed target has four distinct exact meanings. A focal consolidation prototyped a
constructive identity certificate that establishes, as research evidence, the five incidences
as identities with the non-constructible input as a free indeterminate.

## Decision

1. **Single authority.** Under the versioned document policy `constructive-authority/v1`,
   every object has one geometric meaning, the ideal object its construction defines from the
   free inputs; every numerical state is a representation.
2. **Document policy.** Documents without the record keep
   `legacy-representation-authority/v1`; every document, new or existing, adopts the
   constructive policy only by the explicit event `AUTHORITY_POLICY_ADOPTION`, and making it
   the default requires a specific author decision; one document has one authority, and a
   reader that does not support the record fails closed.
3. **Inherited algorithms** are unchanged representation engines; their provenance relations
   are strict for the ideal objects whenever those exist, and the existence of an ideal output
   is decided rigorously, never by an inherited tolerance.
4. **Class table and boundary.** A versioned class table decides exactness by algorithm class;
   objects outside it enter exact evaluation as declared boundary substitutes; a point on a path
   means the point of the ideal path assigned by the inherited path map; inherited numerical
   decisions, booleans and conditioned selections are never rigorous premises, and nothing
   acquires `STRICT` through a decision that is not rigorously established.
5. **Integrity.** Relations carry `STRICT`, `RELAXED`, `NOT_ESTABLISHED` or `CONTRADICTED` in
   a type of their own, independent of numeric guarantees, propagated by premises, never
   persisted and never part of identity.
6. **Relaxed admission.** `constructive-relaxed-admission/v1` admits a relation only with
   positive evidence relative to the constructive objects — exact equality with the declared
   substitutes that persists on a deterministic, versioned set of rational perturbations of
   them, recomputed at each revision and never a proof, or, for sign-change roots
   only, a compatible enclosure of the constructive residual — and without rigorous
   contradiction; G8B-R1 is not inherited and is re-qualified once, at adoption.
7. **CU2 transfer.** CU2 certificates remain certificates of their captured configuration and
   transfer only by structural correspondence, the same revision and one of margin, structure or
   re-decision; otherwise `EVIDENCE_INSUFFICIENT` and strict consumers fail closed.
8. **Constructive identity certificates** prove relations as identities in a field of rational
   functions with an automatic boundary rule, certified sign conditions, branches that
   correspond to the live algorithm outside the driver slice, cells bound to a structural key
   of their dependency cone built on durable identities and approved revision contracts,
   rechecks and new proofs per cell.

## Consequences

Legacy documents and every identity are unchanged. The mandatory contacts become
materializable with explicit integrity and, with constructive identity certificates, strict.
The product carries two policies, distinguished per document. Certification of constructions
outside the class remains `NOT_ESTABLISHED` or `RELAXED`. Explicit adoption is transitional:
after CU3 and a general compatibility verification, a separate author decision may make the
constructive policy the default for new documents, while existing documents keep their policy
until an explicit migration. A default constructive authority never makes every relation
`STRICT`; each relation keeps the evidence it actually has.

The approval of this decision does not approve any versioned budget that the implementation
needs; a budget that the contracts leave to the author is presented for a separate decision with
its deterministic counters.

## Alternatives considered

Representation authority with structural certificates (B1), invalid: it declares incident
two incompatible exact sets. Capture recomputation alone (C3), insufficient: the binary64
target still refutes. Declared-boundary authority (A1), rejected as authority: its
contradictions at the boundary are unreliable; kept as computation engine. Tolerance
incidence, contrary to `AGENTS.md` §1. Inherited prover as authority, rejected: no real
branches. Per-relation authority, rejected: two meanings for one object.

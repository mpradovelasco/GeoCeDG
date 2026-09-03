# ADR 0021: Certified singleton-germ SplineV2 pair materialization

- Status: **Accepted — PASS — AUTHOR APPROVED**
- Date: 2026-09-03
- Decision owner: GeoCeDG author
- Phase: G9S1-R1 — SplineV2 Pair Intersection Materialization
- Disposition C: **AUTHOR ACCEPTED**, limited to the demonstrated monodromic scope
- Implementability: **B — PARTIAL IMPLEMENTATION CONTRACT — AUTHOR APPROVED**
- Specification: [bounded pair materialization](../../geocedg/specs/curves/spline-v2-pair-materialization.md)
- `implementationAuthorized=true`; `selfApproved=false`;
  `authorApprovedPhase=true`; `passClaimed=true`.
- `implementationComplete=true`; `manualAuthorSmoke=PASS`. Authorized
  corrections A/B in ADR 0022 are resolved on the reviewed technical cohort.
  PHASE A/B, COMPOSED and FULL `-CleanBuild` evidence remains bound to that exact
  technical commit; the later explicit author decision establishes phase PASS
  without relabelling those executions as closeout-commit runs.
- The earlier numerical-admission and implicit-tangency failures remain
  historical evidence; no assertion, selector or required gate is waived.

## Context and historical authority

[ADR 0009](0009-locus-v2-locus-intersection-pair-semantics.md) owns the symmetric
two-parameter problem. [ADR 0018](0018-semantic-spline-2d-capability.md) supplies
the piecewise-polynomial SplineV2 capability, but its published pair results are
rich-only. [ADR 0017](0017-deterministic-intersection-phase-rank-identity.md)
does not provide a symmetric pair-sheet selector: one-dimensional phase/rank
cannot simply be applied to either projection of a pair root.

The preserved [D2 contract](../architecture/g9s1_r1_d2_pair_sheet_contract.md)
and preceding diagnostic records establish that projected ranks can exchange
without a pair singularity and that the repeated-periodic witness has genuine
monodromy. Their former pending-approval wording records those checkpoints; it
is not rewritten. This ADR records the subsequent author approval and bounds
the productive implementation. It does not approve unrestricted same-germ
multi-sheet materialization or generic regular-motion invalidation.

## Decision

1. Materialize only authenticated SplineV2 x SplineV2 component pairs, including
   supported similarity compositions where actual current polynomial/evaluator
   coherence can be certified. Generic non-Spline LocusV2 pairs remain rich-only.
2. Use a structural source-associated selector with canonical durable source-ID
   axes, branch/component lineage, declared orientation, domain kind,
   parameterization contract and normalized transverse germ sign. Bind it to
   the existing result owner, source pair and constructive/structural context.
3. Permit one certified singleton root per germ sign in a complete component
   product. This allows at most two eligible roots per component pair, not at
   most two geometric roots. With signs `+,+,-`, the negative class may qualify
   while the positive class remains rich-only, provided its own class coverage
   is rigorous and all ordinary admission conditions hold.
4. Separate the durable selector from current numerical/topology evidence.
   Parameters, boxes, spans, knots, atlas charts, current coefficient signatures,
   revisions and root counts are certificate material, never selector fields.
5. Require bounded deterministic outward-rounded interval existence/uniqueness
   evidence and complete coverage of the selected sign class. Floating discovery,
   small residual, visual plausibility and equal discovered counts are not proof.
   Unknown regions that may contain the selected sign block admission.
6. Preserve `local point admissibility != global completeness`. A strict opposite
   Jacobian-sign exclusion may settle a selected class without enumerating the
   opposite class. Exact token identity does not imply exact arithmetic.
7. Resolve exclusively from the current snapshot. A regular certified singleton
   survives parameter drift, projected-rank swaps and compatible knot/chart
   changes. No Cartesian/proximity, list order, history, arbitrary cut or seed
   is identity authority.
8. Accept structural-slot recurrence: an existing claimed point can be dormant
   or pair-quarantined, then reactivate when the same selector is uniquely
   certified again. This is not historical physical-trajectory continuity across
   a singular interval. Missing proof, temporary mismatch, tangency or remembered
   motion does not permanently retire its recoverable selector.
9. Keep repeated same-germ sheets and the demonstrated monodromic scope rich-only
   at every current state. Pair ambiguity is distinct from R4 periodic quarantine;
   do not transplant R4 phase-tube/offset retirement semantics.
10. Reuse exact ledger ownership, atomic publication, retained claims, normal DAG
    point recomputation and exact copy provenance. Extend them with a strictly
    discriminated pair binding and two-source validation, not a second point or
    coordinate-token system. Old diagnostic pair handles are not migrated by guess.
11. Preserve one solve/certification pass for the rich result and direct selector
    lookup for its children. Ordinary recompute creates zero new GeoPoints.
12. Leave the protected G9U1 design unchanged. A future separate reconciliation
    may consume current eligible pair tokens; no frontend gesture or marker may
    bypass a rich-only classification.

## Mathematical justification and limitation

For canonically ordered sources, solve `H(u,v)=C_A(u)-C_B(v)=0` and classify
`sign(det[epsilon_A C_A'(u),-epsilon_B C_B'(v)])`. On a connected regular
deformation inside the certified singleton class, implicit-function local
sections agree by uniqueness in the entire class. Consequently the structural
sign names a sheet without a movement history, projected rank or arbitrary
atlas spanning tree. This does not certify an unobserved path from endpoints.

Two same-sign sheets need additional structural names not supplied by this
contract. The exact repeated-periodic witness disproves a claimed globally
continuous, path-independent, cut-free labeling of that demonstrated scope.
It is not a theorem that every spline pair is unmaterializable. The author
accepts this explicit partial implementation boundary.

## Persistence and compatibility

Pair-bearing ledger state requires a strict versioned discriminator and both
source descriptors. Preserve existing one-source import rules and fail closed
on unknown/down-versioned pair data. A saved active flag, quarantine reason or
old box is not current proof. Reconstruct both dependencies and revalidate the
same selector. Copy/remap uses exact two-source provenance and renormalizes the
germ if canonical source order reverses; labels never infer identity.

Explicit incompatible durable replacement/disposal remains a structural barrier
under G9A. Ordinary transient invalidity retains claimed ownership. Undo restores
the recorded graph normally; it is not trajectory replay. Native `.cedg`, copy,
undo/redo and tamper negatives must validate the implementation, not merely a
diagnostic model codec.

## Alternatives

| Alternative | Disposition |
|---|---|
| Keep every SplineV2 pair rich-only | Rejected for certified structural singleton classes; retained outside that subset. |
| Rank either parameter projection | Rejected by regular projected-rank counterexamples. |
| Name arbitrary atlas charts/sheets | Rejected without a globally coherent structural selector. |
| Nearest-root or prior-trajectory tracking | Rejected as identity authority. |
| Weaker uncertified materialization policy | Not authorized or needed by this decision. |
| Generic state-space monodromy/cut engine | Outside this bounded implementation. |

## Validation and governance

The [R1 matrix](../validation/g9s1_r1_spline_pair_materialization_validation_matrix.md)
requires actual polynomial certificates, source reversal, mixed multiplicity,
regular motion, chart/knot/seam controls, recurrence, persistence and generic
negative evidence. Preserved D2 diagnostics remain historical evidence, not
production certification. [ADR 0020](0020-verification-levels-and-current-run-evidence.md)
requires PHASE, COMPOSED and clean-output FULL after new verifier registration;
receipt reuse never replaces live phase assertions.

`G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remains OPEN / TRACKED in the
[living roadmap](../roadmap/geocedg_roadmap.md). Pair persistence does not close
that distinct R4 gap. Design acceptance does not approve the implementation:
R1 is technically complete as an `IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`,
with manual author smoke pending. At the preserved earlier stop, the mandatory
native knot test exposed a nonzero exact C0/C1 defect in independently rounded
polynomial pieces; the certifier correctly refused that neighborhood. See the immutable
[scientific blocker report](../validation/g9s1_r1_implementation_blocker_report.md)
and [saved evidence](../../geocedg/validation/g9s1-r1/g9s1-r1-implementation-blocker-evidence.json).

The subsequent author decision authorizes the bounded structural representation
correction in [ADR 0022](0022-structural-spline-continuity.md). Direct structural
solving preserves the same spline space/boundary equations; exact periodic
elimination and a structural-to-interval bridge must supply the missing
hypotheses. It does not weaken this ADR's pair selector, coverage, numerical
proof or lifecycle requirements. Preserve the old diagnostic and require the
same native positive through the corrected model. All remaining R1 and historical
gates still apply and are now satisfied by the fresh tested-cohort receipts.
Phase approval, commit, publication and tagging remain absent until their
separate author boundary; earlier failed cohorts are not relabeled.

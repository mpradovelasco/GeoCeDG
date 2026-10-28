# ADR 0041 — Locus V2 point placement and point redefinition

## Approval of 2026-10-29 (successor authority)

Author decision of 2026-10-29 approves the consolidated design and prospective normative text
identified by commit 7e7a90e4e39db04ea49ee9ab1acf69ea34c81080. CU7-D is PASS / AUTHOR APPROVED.
The same decision separately authorizes only I2a+I2b, M09 singleton. Earlier statements of
pending review or no authorization below record the preceding design stage and are superseded
by this decision. I1/I2G/I3/I4/I5-global remain excluded. No future operation is thereby
implemented. M09 remains partial; see the CU7-D closeout record for exact scope.

- Status: **ACCEPTED — DESIGN AND PROSPECTIVE NORMATIVE CONTRACT AUTHOR APPROVED (2026-10-29)**. CU7-D PASS / AUTHOR APPROVED; implementation authorized only for I2a+I2b, M09 singleton. See [closeout](../validation/pre_g9b_cu7_d_closeout_record.md).
- Date: 2026-10-28. `selfApproved=false`.
- Authorization: author instruction «PRE-G9B-CU7-D — Autorización autoral de diseño de M08 y M09».
  It authorizes design and nonproductive characterization, not CU7-I.
- Base: `72f1c069586f3cf53f26428d31bfd1cbc7014b43`, after the approved CU3 promotion.
- Design: [architecture and source analysis](../architecture/pre_g9b_cu7_d_point_placement_and_redefinition_design.md).
- Candidate contracts: [M08](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md),
  [M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md).
- Author decisions and separately reserved implementation scope: [decision sheet](../validation/pre_g9b_cu7_d_author_decision_sheet.md).

## Context

ADR 0019 permits a transient inverse query followed by a selected semantic address. The
current G9U1 Desktop consumer already creates and drags these points. Neither that gesture
nor CU3's exact driver correspondence defines a general dynamic projection of another point.
The planning candidate predates those distinctions and cannot be used as a current API map.

DA-5 has already removed geometric coincidence as a general redefine condition. ADR 0026
and the durable-frontier contract nevertheless distinguish compatible retention from true
replacement. Retaining a construction point is also different from transporting the identity
of a root selected by an intersection ledger.

## Consolidated decisions — approved direction, normative text pending review

The author adopts D-CU7-01 through D-CU7-11 as direction, and approves CU7-D subject to
reconciliation (D-CU7-12). This text reconciles it; it does not self-approve the new normative
candidate or authorize CU7-I (D-CU7-13). The following precisions prevail over the original
recommendations below:

- S normalizes an established address without fictitious proximity dependency; D requires its
  own dynamic correspondence proof and may be deferred if the affine class fails its gates.
- M09 compatible object-identity update is atomic, without root-identity transport or coincidence.
  Specialized roles, dependents and output groups remain protected.
- The incremental direction starts with M09 singleton. I2G is independent; deferral leaves
  M09 incomplete, even if the singleton increment is accepted.
- I3 is design direction only: prospective closure, atomicity, persistence and identity barriers
  require independent contracts and authorization. Requests requiring the new operation must
  be rejected explicitly with typed causes until its implementation is authorized and verified;
  already admitted routes preserve their current contracts.
- Documentary authority and real evidence grades remain. Proximity/coordinates cannot choose
  identity; estimates cannot establish STRICT.

See the [author disposition](../validation/pre_g9b_cu7_d_consolidated_candidate.md) and
[increment proposal](../architecture/pre_g9b_cu7_i_increment_proposal.md).

1. **Two explicit operations for M08.** A snapshot placement selects one current address,
   then persists the existing forward point construction without a dependency on the proposed
   position. A dynamic correspondence persists an explicit, versioned mathematical map and
   the proposed point as a DAG parent. A Boolean mode on one otherwise identical command
   would conceal materially different dependencies; it is rejected.
2. **Reuse forward authority.** Snapshot placement uses existing semantic-address inputs;
   it is not an exact Cartesian projection assertion. Dynamic placement requires a new
   provider-owned map contract and current evidence; repeatedly invoking the R6 move API from
   a frontend is not an implementation of this operation. CU3 `Point(L,D)` remains a separate
   provenance/correspondence family and is reused whenever its approved preconditions hold.
3. **Scope before uniqueness.** Complete evidence is needed for a none/unique claim over a
   declared search scope. Explicitly selecting one isolated, forward-valid candidate from a
   multiple result selects an address, without asserting global uniqueness or completeness.
   An unresolved result is not promoted by user confirmation. Gaps, uncertified point
   components and unresolved topology fail closed. Distances only bound/rank discovery.
4. **Dynamic map before continuation.** A current snapshot must uniquely resolve the selected
   map on its structural branch/component and declared domain. Prior parameters may seed work,
   but cannot select a solution. Initially recommend only a rigorously supported finite affine
   map; general local minima, nonconvex projections and singular maps require additional
   provider evidence and their own gate. The existing floating affine certificate is not
   silently upgraded to an exact geometric-projection certificate.
5. **Three identities in M09.** Keep the point object's durable ID, its definition contract,
   and the root's ledger-owned token distinct. An explicitly authorized compatible definition
   update can retain the first while replacing the second. It releases any old materialized
   claim and acquires the explicitly selected current token; it never transfers root identity.
6. **Closed point-update predicate.** Extend the explicitly selected
   `IDENTITY_PRESERVING_CONTRACT_UPDATE` only to supported, neutral, singleton `GeoPoint`
   definitions with the same provider/family/schema family/authority/binding/stable role.
   Inspect actual algorithm semantics and the complete candidate closure. Equal Java classes
   alone are insufficient. Different coordinates are allowed; role/cardinality/authority
   changes are not silently made compatible. Specialized interaction-owned roles keep their
   ownership requirements.
7. **Group integrity.** Never redefine one output by deleting its siblings before assessment.
   In the first proposed implementation perimeter, a multi-output producer is rejected with
   a specific cause unless a complete approved provider mapping exists. Detaching a single
   output is a separate author choice and requires a real group adapter; this ADR supplies
   no fictional upstream detach API. The author accepts the singleton-first direction; deferring I2G leaves the
   multi-output part of M09 explicitly deferred, not complete.
8. **Replacement and remapping.** Keep explicit legacy replacement independently assessed,
   with fresh IDs and truthful deletion impact. Do not introduce selective downstream remap
   as a fallback. Fresh-closure copy/remap retains ADR 0011 authority; a new redefine-remap
   operation would require a separate approved amendment. Lineage records provenance only.
9. **Atomic host transaction.** Assess without publishing, bind assessment to exact epochs,
   validate candidate DAG and P3-R1 placement, and snapshot before parsing/creating helpers or
   removing siblings. Publish definition, identities, dependency records and materialized
   claims together. Failure restores the complete entry state; successful execution is one
   normal Undo step. An inability to restore is a fatal typed failure, never partial success.
10. **Driver/support changes.** Do not remove the existing barriers globally. Distinguish an
    ordinary dependency-frontier change of the driver, a source redefine under the same
    semantic constructor, and a change of source lineage. A rigorous future operation must
    stage the entire affected source closure, reconstruct its actual support inputs, update
    authorized records together, and invalidate every old certificate before revalidation.
    Retention of the locus ID never authorizes retention of incompatible root bindings.
11. **Persistence.** Existing snapshot/token/structural points retain their current formats
    and authority policy. A new dynamic-map recipe requires an explicitly versioned record,
    reject-unknown behavior, copy closure rules and supported readers before implementation.
    No automatic document migration, default authority-policy adoption or ledger rewrite is
    introduced. Current evidence/caches are recomputed, not serialized as proof.
12. **Interfaces and phases.** The shared kernel owns geometry, assessment, failure causes,
    identity, persistence and transactions. Desktop chooses and presents; headless callers
    provide explicit selectors/modes; other frontends must expose the same semantics or reject
    the operation. CU7-D ends at candidate delivery. CU7-I, publication, CU4, CU10 execution
    and book edits need subsequent author instructions.

## Candidate amendments and preserved decisions

- ADR 0019 decision 10: add a separately authorized public snapshot-creation boundary; retain
  the historical statement that R6 itself added no command. Decisions 2, 3, 5–9, 11–14 remain.
  Its creation evidence text needs the scope/explicit-choice clarification of M08 §8.
- ADR 0026 decisions 1–3, 7–8 remain. Add the narrow point-definition update of decision 6
  above; decision 6 of ADR 0026 still forbids the impact report from performing a rebind.
- `durable-dependency-projection.md` §9.1–9.3: proposed explicit public-point exception,
  not removal of the V2-source exclusion or a general auto-refresh extension (§10).
- `locus-v2-public-surface.md` §20 and point-interaction §§12, 14, 17–18: add explicit new
  operation boundaries. No point family is converted implicitly.
- ADR 0017, ADR 0039 and ADR 0040: **no weakening** of token barriers, contact evidence,
  structural correspondence, integrity grades or explicit document-policy adoption.
- Exact candidate wording and compatibility conditions are in the two candidate contracts.
  Approved files are deliberately unchanged by CU7-D.

## Alternatives

| Alternative | Assessment |
|---|---|
| Only expose a wrapper around `Point(L,branch,u)` | sufficient for snapshot output, does not satisfy dynamic correspondence |
| One public operation with implicit static/dynamic inference | rejects determinism of dependencies and surprises save/reopen |
| R6 nearest-query on every update, previous coordinates choose the winner | violates map/identity authority; rejected |
| Require old/new point coincidence | contradicts DA-5; rejected |
| Broaden all same-class points and sources to RETAIN | erases roles, group contracts and identity barriers; rejected |
| Rewrite downstream references to fresh points automatically | prohibited rebind; rejected |
| Preserve multi-output producers with a future complete adapter | viable research/design direction; no existing generic API established |

## Consequences and limits

The proposed CU7-I order is M09 singleton first, then snapshot placement. I2G, I3 and the
dynamic map have independent gates. The author has decided that deferring I2G leaves M09
incomplete; implementation authorization remains separate for every proposed increment.
No generalized exact tangency work is smuggled into the dynamic map; that scientific debt remains
after CU10 and the book's later editorial work. A technical STATIC result for this document does
not approve any proposed decision or authorize implementation.

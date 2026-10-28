# CU7-D / M09 — Proposed point definition update contract

## Approval and implementation boundary

Author decision of 2026-10-29 approves the consolidated design and prospective normative text
identified by commit 7e7a90e4e39db04ea49ee9ab1acf69ea34c81080. CU7-D is PASS / AUTHOR APPROVED.
The same decision separately authorizes only I2a+I2b, M09 singleton. Earlier statements of
pending review or no authorization below record the preceding design stage and are superseded
by this decision. I1/I2G/I3/I4/I5-global remain excluded. No future operation is thereby
implemented. M09 remains partial; see the CU7-D closeout record for exact scope.

- Status: **NORMATIVE PROSPECTIVE CONTRACT — AUTHOR APPROVED (2026-10-29)**. No future capability is thereby implemented. Implementation authorization is limited to M09 singleton I2a+I2b; M08-S/D, I2G and I3 remain excluded. See [closeout](../../../../docs/validation/pre_g9b_cu7_d_closeout_record.md).
- No productive behavior, serialized identity or migration changes in CU7-D.
- Decisions: [proposed ADR 0041](../../../../docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md),
  [author decision sheet](../../../../docs/validation/pre_g9b_cu7_d_author_decision_sheet.md).
- Existing authority: [durable frontier](../../spatial/durable-dependency-projection.md),
  [public surface](../locus-v2-public-surface.md), [intersections](../locus-v2-intersections.md),
  [ADR 0026](../../../../docs/adr/0026-advanced-redefine-and-explicit-legacy-fallback.md).

## 1. Operation and identities

Author precisions 2–5 govern this reconciliation: compatible object-identity update is atomic,
without root-identity transport or geometric coincidence. Specialized roles, dependents and
output groups are protected. The incremental direction starts with singleton M09; I2G is an
independent gate and its deferral leaves M09 incomplete. I3 is design direction only, subject
to its own prospective-closure, persistence and barrier contract and implementation authorization.

DA-5 is an existing author decision: the proposed point need not coincide with the old point.
Equality/proximity of values is neither a general precondition nor evidence of continuity.

Separate three records:

1. `PersistentGeoId(P)`: identity of the explicitly targeted construction object.
2. Its committed definition: producer contract, stable output role, parents and revisions.
3. The chosen solution identity: exact opaque token owned by rich result R, with its source
   pair, selector, constructive lineage and admissibility. It is not P's identity.

A compatible explicit update may change (2), retain (1) and select a new (3). The user is
changing the construction object, not asserting that two mathematical roots are identical.
Dependents retain their references to (1); their evidence must be recomputed. Any downstream
definition whose **own** semantic identity includes the old definition/lineage must pass its
own continuity predicate or become noncurrent. Keeping P never stamps its closure valid.

## 2. Closed retention predicate — proposed

Offer the existing `IDENTITY_PRESERVING_CONTRACT_UPDATE` execution mode for a new public-point
case only when all of the following are established before publication:

- explicit, current old target; complete operation-entry snapshot and exact host/graph/runtime
  epochs; no label-discovered target fallback;
- same neutral construction provider, point family, supported schema family and allowed lazy
  version transition, construction authority, binding role and stable output role;
- exactly one old output and one candidate output, or an independently approved complete group
  adapter; no missing/duplicate/ordinal-derived roles;
- a closed recognized point-definition transition, not merely matching `GeoClass.POINT`;
- supported reconstructible candidate parents, complete prospective identities and truthful
  dependency projection under their recorded versions;
- acyclic full host DAG and identity graph, valid P3-R1 placement and surviving entry witnesses;
- a complete downstream impact assessment and admissible selected intersection as in §4;
- no unapproved change of document geometric-authority policy or role ownership;
- explicit selection of this mode after assessment; failure never implies replacement.

Proposed first transition table:

| Old/candidate definition | Retention recommendation |
|---|---|
| Neutral singleton ordinary point → exact-token intersection point (`VALUE`) | eligible subject to the whole predicate |
| `VALUE` semantic-address point → exact-token point, and reverse | eligible; address/claims replaced atomically |
| `VALUE` token point → token point of another explicitly selected R/token | eligible; no token transfer between ledgers |
| CU3 structural point (`VALUE`) → token/semantic point | explicit transition only after its provenance role and certificate consumers are characterized; separate gate, not assumed supported today |
| Interaction-owned or principal-selector specialized role → plain token `VALUE` | role mismatch: retention unavailable; explicit fresh replacement or a separately approved role migration |
| Spatial projection-defined / differently governed point → ordinary token | authority/binding mismatch: retention unavailable |
| Point → rich result `Intersect(L,target)` | wrong family: reject as a point update; select a materializable solution first |
| Multi-output ordinary intersection → one new point | group barrier until §5 adapter is approved and demonstrated |

A change of construction recipe can be compatible under this explicit object contract even
when the points are far apart. A provider/family/schema/authority/group mismatch is a real
incompatibility. User confirmation does not waive the predicate.

## 3. New point identity and remap/lineage

For an old point with no durable identity, there is no old `PersistentGeoId` to retain. The
operation needs its own entry-context envelope so helper creation/identity enrollment cannot
publish before the ordinary target is replaced. The proposed result receives a fresh durable
ID on commit. Ordinary surviving dependencies are rebuilt through the host's explicit target
operation; they are not treated as a pre-existing durable identity closure.

For an incompatible durable point, keep existing explicitly selected legacy replacement:
fresh candidate identities, retirement/removal of the old durable closure and ordinary cascade
effects reported truthfully. An old label may remain as a parser handle but carries no identity.

Do not add M09-B selective downstream remap to the first implementation. ADR 0011's complete
copy/import closure map is not a redefine adapter. A future replacement-with-remap needs an
author-approved, total typed map over the affected closure, role and external-reference checks,
lineage, atomic rollback and new persistence tests. Mere provenance (`copySourceId`, an audit
record of predecessor P or old/new IDs) does not authorize downstream association or token reuse.

## 4. Candidate intersection and admissibility

`Intersect(L,target)` returns a rich result, not a point. A point update consumes
`Intersect(R,"exact-token")`, or CU3's separately admitted structural intersection form after
its explicit transition gate. Resolve the exact token in R's **current** point-admissible
solutions. R's existence does not make every diagnostic candidate materializable.

Preserve all current admissibility rules, including local/global independence, the actual
evidence grade and `STRICT`/`RELAXED`/`NOT_ESTABLISHED`/`CONTRADICTED` integrity. For CU3
contacts only approved class-1/2 evidence materializes; residual proximity at a critical point
is insufficient. Document-policy adoption is a separate explicit event.

Revalidate immediately before commit. A disappeared, dormant, quarantined, forged or wrong-owner
token causes failure with no mutation. Multiple admissible solutions require explicit choice;
the UI may rank/label them for display but passes the exact revision-bound token. A rich result
created by the same gesture is staged in the same transaction; cancel removes all its temporary
parents/helpers. Reuse an existing R only when the caller selects it or the kernel proves an
exact equivalent binding; similar labels or coordinates do not constitute a reuse policy.

On successful definition update, release P's old materialized claim exactly once, acquire the
new claim exactly once and commit with the identities. A failed transaction restores claim
counts, retained/quarantined groups, ledger state and high-water semantics. Never substitute
the new root for an old root token. Later token loss makes P undefined; same-selector recovery
follows ADR 0017/0039, independently of the point-definition update.

## 5. Multi-output construction barrier

The existing construction provider requires a singleton; the host can remove old siblings
before an ordinary replacement snapshot. Neither property can be assumed away.

Recommended bounded CU7-I: reject the unsupported group before any removal with
`CU7_GROUP_TRANSITION_UNSUPPORTED`, preserving P, siblings and their complete dependent closure.
The author accepts this singleton-first direction; its implementation remains unauthorized.
I2G is independent and its deferral never permits declaring M09 complete.

If the author requires M09 for a selected output of `AlgoIntersectLineConic` in CU7-I, add an
independent group-adapter increment before accepting M09:

1. Establish provider-owned semantic output roles and a complete source/selection contract;
   an array position or host inherited index is not a durable mapping.
2. Stage the old producer and all outputs. Keep the old geometric solution needed by the
   producer as a distinct internal output if required; give any new object its own identity.
3. Move the explicitly targeted **construction point identity** to the new admitted producer
   only under an approved group transition. Map every sibling and consumer explicitly; no
   blanket label replacement or automatic coalescence of coincident roots.
4. Prove serialization, host placement, full rollback and Undo/Redo for the whole group,
   including identity-free siblings and arbitrary ordinary downstream objects.

This is a design obligation, not an assertion that GeoGebra supports detachable output slots.
If its inherited producer cannot supply these semantics, retain the typed rejection and seek
an author decision; do not modify a broad upstream intersection algorithm as a shortcut.

## 6. Atomic transaction and DAG reconstruction

Proposed operation phases reuse the existing G9A3 registry/host lifecycle, extending its
envelope where explicitly described; they are not a new frontend snapshot implementation.

```text
capture entry target / host XML / IDs / ledgers / epochs / placement witnesses
  -> stage candidate and complete prospective parent closure (no publication)
  -> validate family/role/group and full host + durable DAG, assess impact
  -> explicit operation choice, then revalidate epochs and token
  -> reserve IDs / install definition and records / reconcile materialized claims
  -> reconstruct and validate postconditions
  -> publish once, normal Undo boundary once
failure at any point -> exact entry restore, abort and reacquire objects
```

The rollback envelope must precede parsing, candidate helper promotion, sibling removal and
host reordering, including for an old ordinary point. Snapshot restoration may replace Java
instances; controllers must abort and reacquire by the retained IDs/explicit transaction
handles, never by whichever object now has P's label.

Reject cycles in all inputs: if R, L, target, Q or a helper depends on P, or if the rewrite
introduces an indirect cycle, the operation is `INVALID_DAG`. A finite drawable result does
not excuse a cycle. A late-created predecessor uses the existing minimum legal P3-R1
relocation; preserve unaffected relative order. Empty placement interval rejects.

Downstream geometric invalidity is different: an acyclic valid update can make, for example,
a line through P and A undefined because P now equals A. Commit with the proper undefined
state and diagnosis, rather than rejecting all updates that move P. The report distinguishes
known undefined outcomes, re-evaluation-required outcomes and removal. It must not predict
arbitrary future geometry as certainly valid.

## 7. Driver/support closure — proposed handling

Until the new coordinated operation has its own authorized implementation, requests that
require it must be rejected explicitly with a typed cause and no mutation. This future
diagnostic requirement does not claim that documentation fixes today's silent refusals.
Already admitted source redefinitions preserve their predicates; do not introduce a blanket
refusal that regresses them. I3 cannot be enabled by passing a singleton gate.

Distinguish:

- in-place changes of the same support object, normally retaining its identity;
- `N=Point(c3)`, changing the driver's durable frontier;
- `L=LocusV2(X2,N2)`, retaining the constructor family while changing source dependencies;
- incompatible generator-family, domain/orientation or constructive-lineage changes.

Rigorous support switching requires a sealed coordinated candidate closure: enroll c3 and all
required provider parents, reconstruct L's actual support/coordinate inputs from its serialized
construction, compare its new record frontier, and explicitly assess every affected public
source record. §10 ordinary refresh cannot be broadened silently to public outputs.

Retain N or L only under their approved object-contract predicate. Independently compare
component/driver/support lineage and root contracts. On a genuine identity barrier old roots
are noncurrent/retired according to their typed policy; a retained point becomes undefined.
For same-selector temporary geometric loss keep the accepted dormant/recovery semantics.
Never reactivate an old token merely because the replacement source draws the same curve.

Exact reconstruction fingerprint, CU2 certificates, constructive cells, local root proofs and
render caches all invalidate/revalidate under their existing revisions. Do not move the live
driver to force a proof or patch stored dependencies to match a guessed support.

## 8. Candidate API, failures and interfaces

Reuse current `SpatialRedefineAssessment`, `SpatialRedefineExecutionMode`, provider proposals,
candidate participation, impact reports, host rollback and P3-R1 authority. Proposed extension
points, not existing methods:

```text
assessPointDefinitionUpdate(explicitTarget, stagedPointDefinition, completeClosure)
    -> existing typed assessment + candidate transition evidence + complete impact
executeAssessedPointUpdate(currentAssessment, explicitlySelectedMode)
    -> committed point identity / fresh identity / typed failure
```

Add typed distinctions, with final enum naming left to implementation review:
`CU7_POINT_TRANSITION_UNSUPPORTED`, `CU7_GROUP_TRANSITION_UNSUPPORTED`,
`CU7_ROLE_TRANSITION_UNSUPPORTED`, `CU7_INTERSECTION_NOT_ADMISSIBLE`,
`CU7_REQUIRED_PARENT_NOT_STAGED`, `CU7_SOURCE_CLOSURE_UPDATE_REQUIRED`,
`CU7_IMPACT_INCOMPLETE`. Reuse current `INVALID_DAG`, `STALE_ASSESSMENT`,
`DURABLE_CONTRACT_CHANGE`, `REDEFINE_REJECTED` and catastrophic rollback causes.
Expose cause, subject ID and recovery class through a typed assessment, not exception-message
parsing. A cancelled operation differs from a rejected one and must not consume Undo history.

Desktop's redefine dialog, algebra editing and intersection chooser must present only offered
modes, the complete known ordinary and durable impact, and distinguish recalculation/removal.
Headless/script callers supply exact targets, tokens and modes with no modal UI. Web/other
frontends either implement the same protocol or reject unsupported authoring, while preserving
supported existing records. Workbench and UI cannot manufacture proof, continuation or identity.

## 9. Persistence, migration and compatibility

For an admitted singleton transition with existing normal-form commands, retain current
schema-v2 identity records and persist changed parents/revisions; change no ledger encoding.
The absence of a new XML element does not mean absence of a semantic compatibility decision:
the author must approve the new transition predicate and backward-read matrix first.

Save/reopen and Undo/Redo preserve point ID, committed role, complete source bindings and exact
token owner; current certificates are recomputed. A duplicate/copy has fresh IDs and an exact
full-closure map, including dormant claims; partial/missing closure fails under current rules.
Rename is harmless; delete/recreate with the same label is a new identity. Existing documents
are unchanged until an explicit edit; no batch migration or adoption of constructive authority.

Older readers may understand the final ordinary command graph but not a new group or dynamic
recipe. Test actual older supported readers; do not promise that an unknown command safely
rejects a whole document. Any new group recipe/schema requires versioning and reject-unknown
support before its gate. No bypass or rewriting of CU3 ledger v6/class F/T is allowed.

## 10. Candidate normative amendment text

Only after author approval:

1. Add to ADR 0026: «An explicitly selected point-definition contract update can retain a
   neutral singleton construction point's durable identity while changing its supported
   definition. It requires a closed provider transition, complete role/group/DAG/impact
   checks and an atomic claim update. It does not assert continuity between old/new roots,
   use coordinate coincidence or authorize downstream rebind.»
2. Amend durable-frontier §9.1/§9.3 with that closed public-**point** exception. Keep public
   **source** constructor compatibility, §10 ordinary refresh and late-participation rules
   separate. Do not replace them with a universal same-class retention predicate.
3. Extend public-surface §20 with §§1–6 above, preserving exact-token §9 and rich-result §10.
   Add operation-entry rollback for identity-free targets, complete ordinary cascade impact
   and unsupported-group diagnostics. Specialized role conversions stay unavailable.
4. Do not amend ADR 0011 to permit M09-B remapping. Do not amend ADR 0017 or CU3's identity/
   admissibility/integrity rules. A coordinated source-closure operation needs the explicit
   specification and gate of §7 before the corresponding amendments take effect.

These paragraphs are proposals; approved sources are unchanged in CU7-D.

# CU7-D / M08 — Proposed point placement contract

## Approval and implementation boundary

Author decision of 2026-10-29 approves the consolidated design and prospective normative text
identified by commit 7e7a90e4e39db04ea49ee9ab1acf69ea34c81080. CU7-D is PASS / AUTHOR APPROVED.
The same decision separately authorizes only I2a+I2b, M09 singleton. Earlier statements of
pending review or no authorization below record the preceding design stage and are superseded
by this decision. I1/I2G/I3/I4/I5-global remain excluded. No future operation is thereby
implemented. M09 remains partial; see the CU7-D closeout record for exact scope.

- Status: **NORMATIVE PROSPECTIVE CONTRACT — AUTHOR APPROVED (2026-10-29)**. No future capability is thereby implemented. Implementation authorization is limited to M09 singleton I2a+I2b; M08-S/D, I2G and I3 remain excluded. See [closeout](../../../../docs/validation/pre_g9b_cu7_d_closeout_record.md).
- No product API or command in this file is implemented by CU7-D.
- Decision: [proposed ADR 0041](../../../../docs/adr/0041-locus-v2-point-placement-and-point-redefinition.md).
- Current authorities: [point interaction](../locus-v2-point-interaction.md),
  [public surface](../locus-v2-public-surface.md), [existence](../locus-v2-existence-components.md),
  [constructive authority](../locus-v2-constructive-authority.md).

## 1. Objects and meanings

Author precisions 1 and 5 govern this reconciliation: S normalizes an established semantic
address without a fictitious dependency on the initial point. D is distinct, requires its own
dynamic correspondence proof, and may be deferred if the affine class fails its gates. An
estimate never establishes STRICT. Discovery proximity cannot choose durable identity; the
explicit admissible address selection does. The
[increment proposal](../../../../docs/architecture/pre_g9b_cu7_i_increment_proposal.md)
places M09 singleton before S. No new operation is implemented or automatically authorized.

Let `L` have durable source identity, provider contract, oriented branch `b` and certified
continuous component `c`, with semantic evaluation `F[b,c](u)`. A proposed world position `q`
may be supplied by a point `Q`, but does not identify any preimage. Multiple addresses can
evaluate to the same position.

| Operation / relation | Meaning | Dependency on Q after creation |
|---|---|---|
| Proximity query | bounded discovery of semantic addresses near q under a declared policy | no constructed point |
| Snapshot placement S | select address a; construct `P=F(a)` by existing forward authority | none |
| Dynamic correspondence D | construct `P=F(phi(Q,L,C))` for an explicit map contract C | yes |
| Membership | P is defined by forward evaluation on an admissible semantic address | independent of whether q is on L |
| Metric projection | selected global/local minimum of a declared squared-distance objective | only D with the required map evidence may claim this |
| CU3 driver correspondence | exact provenance relation `t*(D)` on the declared support | existing `Point(L,D)`; separate family |

S is static **with respect to Q**. Moving or redefining L normally recomputes its forward
point; S is not a frozen Cartesian point. Explicit dragging can edit S's address only if S is
created with the already supported interaction-owned role. It never follows Q automatically.

## 2. Snapshot placement S

Proposed operation steps, all under kernel authority:

1. Capture one coherent current source revision and transient q, a finite world radius,
   search scope and deterministic work policy. Validate source and document policy.
2. Use the current inverse resolver. Preserve its status and coverage, including
   `UNRESOLVED_NUMERICAL_SEARCH` for a general evaluator's sole discovered candidate.
3. Require a certified continuous component, canonical branch/component address, finite
   forward evaluation and adequate local isolation. Canonicalize seams/knots by the provider.
4. In a unique complete-scope result, accept that candidate. In a multiple result, require an
   explicit choice of one member of that exact current result, with local admissibility.
   A choice does not upgrade global coverage. No candidate from an unresolved/unsafe result
   is offerable; changing scope requires a new query with truthful scope evidence.
5. Revalidate membership of the choice in the result and the exact source revision. No list
   ordinal, rounded coordinate, UI caption or distance tie-break is an executable selector.
6. Atomically construct the ordinary semantic point and its explicit address state. Discard q,
   the search result and discovery residual as authority. Store one Undo event at the host.

The chosen binary64 canonical parameter is an explicit forward input, not an exact inverse
proof. A numerical seed can therefore produce an authoritative point **on the semantic
locus at that chosen parameter**, without proving that it is the exact nearest point to Q or
that Q belongs to L. Report the initial search's real numerical guarantee separately.

Durable state uses the existing source binding and address codec: branch, structural component
selector, exact stored canonical bits, periodic lift and seam rule. `PersistentGeoId` identifies
the point object. These inputs select its definition; q and its approximation never identify it.

## 3. Candidate API surface for S

The following are **operation descriptions**, not existing Java signatures or registered commands:

```text
querySnapshotPlacement(sourceRef, transientWorldRequest, scope, worldPolicy)
    -> current result + explicit candidate evidence
commitSnapshotPlacement(currentResult, chosenCandidate, pointOwnership)
    -> ordinary point or typed failure
```

Implementation should reuse `LocusPointInteractionQuery2D`,
`LocusPointInteractionResolver2D.resolve` and
`LocusV2PublicOperations.createInteractiveSemanticPoint` where their actual contracts apply.
The latter currently checks revision/address reconstruction; CU7-I must also preserve the
admissible-result/explicit-choice boundary rather than treating any constructed candidate value
as a permission token. A headless caller can use existing `Point(L,branch,u)` directly when it
already has an address.

Recommended authoring name: `PointOnLocusV2` for S, with explicit selection if required. Exact
command grammar/localization is reserved to the implementation gate after approval. Its saved
normal form must be the existing forward construction; replay must never re-run a Q query.
This avoids inventing a persistent three-parent snapshot command whose third parent is ignored.
A scriptable query/commit interface must expose revision-bound choices and cannot open a GUI
inside command evaluation. If that surface is not implemented, only the explicit UI action and
existing forward command are claimed.

## 4. Dynamic correspondence D

D is a new capability. R6 move edits state as a gesture; it does not define a dynamic function
of Q. CU3 provides a dynamic exact driver correspondence only for its closed provenance table.

A candidate dynamic map recipe contains:

- versioned map kind and provider contract;
- durable L and Q references and any explicit parameter/domain inputs;
- structural branch/component selector and declared orientation;
- declared search/admissibility domain and endpoint/periodic policy;
- semantic selection rule, numerical tolerance and deterministic work-policy versions;
- separately typed current state, numerical guarantee and relation integrity.

The recipe is the durable definition. Numeric minimizers, isolation cells, certificates, source
revisions and cached last-good coordinates are derived evidence and do not identify it. A seed
can accelerate evaluation; deleting the cache must not change a current result or select another
map sheet. Reopening must reconstruct the same map from the recipe without a motion history.

Proposed conceptual API (not an existing method):

```text
constructDynamicCorrespondence(sourceRef, proposedPointRef, versionedMapRecipe)
    -> ordinary dependent point + current typed map state, or typed failure
```

Validate/publish the recipe and references inside the kernel transaction. This operation
does not accept a list of previous coordinates as its semantic correspondence.

### 4.1 Recommended first mathematical class

For a finite non-point certified component and a noncollapsed affine map
`F(u)=A*u+B`, a metric projection candidate has

```text
u0 = dot(A, Q-B) / dot(A,A)
u* = clamp(u0, lower, upper)   // only for included finite endpoints
P  = F(u*)
```

This is a derivation: `d''(u)=2*dot(A,A)>0` establishes uniqueness on a closed interval.
For open endpoints the infimum can be unattained; then there is no projection, not a point at
an excluded endpoint. An unbounded component needs a separately declared domain policy, not
an artificial finite clamp. Distinct components require explicit component selection; the
certificate of one component makes no global claim about the others.

**Required new evidence:** the ideal affine formula, its nonzero A, domain inclusion and current
source/component correspondence must be established under the document's authority. The current
`CertifiedAffineLocus2D` coefficients are floating structural evidence; their name does not
certify this stronger exact projection contract. If the new provider cannot establish it, D
returns `MAP_NOT_ESTABLISHED`. No numerical scan is substituted.

Under constructive authority, use the constructive Q and L, or explicitly propagate their
substitutes/integrity with truthful grades. Q's screen or cached coordinates do not establish
`STRICT`. Under legacy authority preserve its existing meaning. Do not adopt a policy while
constructing the point. A finite approximation to u* must carry error/guarantee information;
strict projection is claimed only with actual relation evidence, never from the output's being
defined. Arithmetic overflow/nonfinite values fail with a typed cause.

### 4.2 Generalization boundary

For a smooth source, stationary parameters satisfy
`h(u)=dot(F(u)-Q,F'(u))=0`. These include maxima, minima and degenerate stationary points.
Local isolation alone does not establish a selected minimum or global uniqueness. A future
map can use certified `d''>0`, endpoint signs and a continuous-valid bracket, or a different
approved exact construction. A root with small residual, previous u, tangent contact evidence
from CU3 or a nearest rendered segment cannot replace this certificate.

A supplied semantic correspondence is preferable to inversion when available: use the existing
CU3 form if its provenance matches; otherwise a new map provider needs its own explicit rules.
No general continuation through folds, cusps, monodromy or self-intersections is authorized here.

## 5. Dynamic dependencies and lifecycle

All inputs of D participate in `setInputOutput()`/ordinary dependency registration of a future
kernel algorithm. The new parent family is proposed, **not** named as an existing class. Its
`compute()` consumes one coherent snapshot and publishes either a current binding or undefined
with a cause. Caches key on source/definition/domain/policy revisions and Q's relevant revision.
They carry no authority. No frontend update listener maintains a second DAG.

- Source/Q update: invalidate all prior current evidence before evaluation; recompute recipe.
- Loss, split/merge or unprovable structural component correspondence: undefined; retain recipe.
- Recovery: revalidate the same recipe; never choose a new branch because it is closer.
- Change of declared map kind, branch, orientation or support: explicit redefine assessment.
- Periodicity: only a certified closed periodic component permits seam/lift semantics; an open
  arc never wraps. A map with no unique global sheet fails closed at a seam barrier.
- Collapse: new inverse placement is degenerate. An existing S can retain its address through
  an R5 collapse as today; D requires its own map's uniqueness and becomes undefined if lost.
- Copy: new point identity and full explicit closure remap; no external Q substitution by label.
- Undo/Redo: restore committed recipe and identities, then recompute evidence.

## 6. Failures (proposed additions prefixed CU7)

Reuse existing query statuses for S. Map state needs a closed typed vocabulary, at least:
`CU7_STALE_SELECTION`, `CU7_MAP_NOT_ESTABLISHED`, `CU7_MAP_NONUNIQUE`,
`CU7_COMPONENT_CORRESPONDENCE_LOST`, `CU7_EXCLUDED_ENDPOINT`, `CU7_UNSUPPORTED_MAP`,
`CU7_NONFINITE`, `CU7_WORK_LIMIT`, `INVALID_DAG` and catastrophic rollback failure.
These proposed names are not current enum values. Component point/gap failures preserve the
existing typed causes. No failure publishes stale coordinates as the current point.

## 7. Persistence and compatibility

S can reuse current normal-DAG point persistence, including dormant address state. Creation
receipts and click/search history do not enter XML. The source is never made a generic `Path`.

D needs a new versioned reconstructible recipe with both L and Q as true parents. The exact
XML element/command encoding is an implementation detail still subject to a schema review;
there is no currently approved dynamic codec. A reader without support must reject it rather
than load S or a free point. No existing record is repurposed. Before CU7-I D, test unsupported
reader behavior with the actual host loader and establish a feature/version gate if an unknown
command alone does not reject the document safely. Existing documents are not migrated.

## 8. Candidate normative amendment text

On author approval only, incorporate the following into the governing sources:

1. **ADR 0019 decision 10, additive successor:** «R6 introduced no public inverse command.
   A separately approved snapshot placement operation may consume its current typed query and
   explicit candidate choice, publishing only the ordinary forward semantic-address point.
   A dynamic correspondence is a distinct versioned kernel definition with Q as a DAG parent;
   R6 interaction state is not its authority.»
2. **ADR 0019 evidence model and point-interaction §§3, 5, 8:** «None/unique requires complete
   evidence for the declared scope. One explicitly chosen isolated candidate in a multiple
   current result may define an address without asserting uniqueness. Unresolved/unsafe
   evidence cannot be upgraded by a chooser.» This reconciles the evidence wording with the
   already existing G9U1 multiple-candidate chooser; it does not invent global exclusion.
3. **Point-interaction §14 and public-surface creation boundary:** add S under the separately
   approved public surface. Preserve §§17–18, the certified component predicate and CU3's
   separate provenance family. Add D only with its independent provider/persistence gates.

Approved ADR/specification files remain untouched by CU7-D. Validation and author choices are
in the [matrix](../../../../docs/validation/pre_g9b_cu7_d_validation_and_implementation_gates.md)
and [decision sheet](../../../../docs/validation/pre_g9b_cu7_d_author_decision_sheet.md).

## 2026-11-02 — Accepted M08-S-R2 implementation amendment

Authority: definitive author decision, independent review FAVORABLE and author-reported
manual smoke FAVORABLE of 2026-11-02, for commit
`86438edabaed06a6509c1be37cf06278941e0e46`, tree
`96e90172201440739bfe8afb503059379c70ff70`.
This successor amendment supersedes the implementation exclusions for M08-S and the
single-route restriction in §2 steps 3–5, without rewriting the historical CU7-D text.
Only M08-S-R2 is PASS — AUTHOR APPROVED; neither all CU7 nor M08-D is closed.

### Admission and compatibility

Preserve historical R1/R6 admissions through LocusSnapshotPlacement2D, including
UNIQUE_ADMISSIBLE_PREIMAGE and explicit choice from MULTIPLE_SEMANTIC_PREIMAGES.
Only UNRESOLVED_NUMERICAL_SEARCH with typed reason
GLOBAL_UNIQUENESS_NOT_ESTABLISHED permits the separate M08SnapshotPlacement2D
R2/H1 query. Other rejections are terminal; a publication failure never triggers fallback.
Candidates, permissions, revisions, coverage and commits of the two routes are not mixed.
The historical R6 statuses and guarantees, including its 52 regressions, retain their meaning.

The independent M08-S result separates individual evidence, global coverage and permission.
An individual canonical reconstructible address may be selected without a distance minimum
(local or global) or global search completeness. UNRESOLVED, isEstablished(), small residual,
candidate count or render samples alone never grant permission. No proved offer is not a
certificate of absence. GlobalCoverage.NOT_ESTABLISHED remains truthful.

The new route is limited to the supported R2/H1 model and represented configuration:
current valid source/component, canonical address, finite valid forward evaluation,
certified continuous component and interior regularity, and no applicable unsafe barrier.
An outward enclosure of squared distance at the selected argument must lie strictly below
the lower bound of squared world radius. The existing interval C1 model on the interior
neighborhood of adjacent doubles must have a first jet excluding zero in some coordinate
throughout that neighborhood. The forward value must be finite, inside the enclosure and
within the numerical radius, and publication must reconstruct the selected address.
This is CERTIFIED_ERROR_BOUND in the represented configuration, not EXACT or STRICT and
not a transfer of constructive authority. Unsupported models, unresolved singularities,
unsafe seams/boundaries, gaps, collapsed components and exhausted proofs fail closed.
Do not apply H1 interior restrictions to previously admitted R1/R6 endpoints.

### Selection, budget and publication

Only EXPLICIT_LOCAL_SELECTION_REQUIRED offers a new-route Choice. Every new-route offer
requires explicit contextual confirmation even when only one address was discovered.
R1 multiple addresses require explicit choice; the integrated tool also confirms R1 unique.
Selection CURRENT/STALE/CONSUMED is distinct from immutable Result evidence. Commit
revalidates exact query, source ID/definition/revision, authority, component, transient
request (including current Q when supplied), policy and query-owned single-use Choice.
Stale, foreign, consumed, unsafe or budget-exhausted selections reject without publication.

H1 accounting is cumulative across discovery/refinement, semantic cache misses including
nested/composed evaluation sessions, interval passes, forward reconstruction, revalidation
and prepared publication. Units are semantic misses, interval passes and publication attempts,
not Java-call counts. Confirmation consumes the true remaining budget. BUDGET_EXHAUSTED
never yields partial permission or construction. R1/R6 retains its historical independent
budget/forward contract; no aggregate cross-route limit or H1 guarantee is imputed to it.

P is published only through the existing forward construction with legitimate semantic
parents and one host Undo operation. Q is a transient initial value, never a new persistent
parent; a legitimate dependency through L remains. Recompute follows L, not the reference Q.
The visual choice selector and its query-owned capability are ephemeral and not persisted.
The structural semantic component selector belongs to the durable address and is persisted
by the existing forward contract. No new codec, dynamic L,Q expression, coordinate identity,
label association, ordinal selector or retrospective identity is introduced. Existing Undo,
Redo, native .cedg, copy/remap and legacy-document contracts remain unchanged.

### Desktop and scope

Experimental tool 145 under cedg.locus.v2: tool → graphical L → existing Q → contextual
choice/confirmation → forward P. No habitual creation dialog; advanced explicit-address
authoring remains available in its compact dialog. The specific contextual panel sits above
algebra input, with one world radius, advanced Policy and explicit invalidation on edits.
Escape, cancellation and mode changes clear transient state without mutation. Desktop
presents evidence; the shared kernel decides admission and publishes geometry.

Search scope is fixed: all branches/components admitted by provider and policy. It is
accessible in technical Policy diagnostics and distinct from actually established coverage;
no routine fixed-scope banner or prequery coverage is required. Existing radius, tolerances
and budgets are session-configurable, with the existing initial radius 0.1. Zoom, DPI and
hit tolerances do not establish geometric authority. No Desktop postfilter simulates a
restricted query. Configurable branch/component restriction is a future unapproved extension.

M08-D, M09, I2G/I3 and general dragging are excluded. Existing direct SplineV2 creation,
Point/Point-on-object and admitted dragging remain unchanged. Maintain
DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG, CU10 and
DEBT-CU3-EXACT-TANGENCY-GENERALIZATION. A reusable geometric-authoring contextual-panel
pattern is a future proposal only, not general infrastructure authorized by this closure.

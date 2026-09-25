# PRE-G9B-R3 redefine correctness candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R3, redefine correctness (D3-a, D3-b)
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (escalated from INTEGRATED_PHASE; VERIFICATION_ESCALATION_REQUEST = AUTHOR APPROVED)
FROZEN_ACCEPTANCE         = FINAL, one physical campaign (section 7)
IMPLEMENTATION_BASE       = ef120a54f035b85df014e89c3916cb3a26f5f933 (tree 98bdd7220223bedefee0eddbb2c9ef5e6572b133)
BRANCH                    = feature/pre-g9b-r3-redefine-correctness
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a separate closeout/author-decision record is the sole
authority for current author approval status.

The phase executes the canonical
[`pre-g9b-r3-redefine-correctness.prompt.md`](../../.github/prompts/tasks/pre-g9b-r3-redefine-correctness.prompt.md),
superseded only where the author's `AD-R0-6` resume disposition (the
"resume after AD-R0-6 migration stop" instruction) decides the migration and the
verification class. The final state is described by the living normative
documents: the
[durable dependency projection contract](../../geocedg/specs/spatial/durable-dependency-projection.md),
[ADR 0029](../adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md),
the [compatible-redefine design §7](../architecture/post_g9u1_a3_v2_compatible_redefine.md),
the [frontend handoff amendment](../architecture/post_g9u1_a3_r1_frontend_handoff.md)
and the [validation matrix](pre_g9b_r3_validation_matrix.md). Machine-readable
evidence:
[`geocedg/validation/pre-g9b-r3/pre-g9b-r3-evidence.json`](../../geocedg/validation/pre-g9b-r3/pre-g9b-r3-evidence.json).

## 1. Chronology

1. `PRE-G9B-R3` started from the author-approved `PRE-G9B-R2-E0/E1` closeout
   promoted to `main` (`ef120a54…`) with `VERIFICATION_CLASS =
   INTEGRATED_PHASE`, frozen at phase start.
2. The canonical projection was established as the transitive durable frontier.
   Adopting it on both sides changes what persisted dependency lists mean, so
   the phase stopped at `AD-R0-6` and reported, before any migration was
   implemented and before any commit.
3. The author reviewed the stop report and decided the migration contract
   (versioned rules, lazy migration, certified auto-refresh, explicit
   durable-contract operations, typed vocabulary, Desktop choices), a bounded
   Construction Protocol refinement, the Ri debt dispositions and the escalation
   to `GLOBAL_IMPACT` with one `FINAL` campaign.
4. The phase resumed from the same base with no product commit, and this
   candidate implements that disposition.

## 2. D3-a and D3-b resolution

- **One projection.** `DurableDependencyProjection` implements the historical
  direct rule (version 1) and the transitive durable frontier (version 2).
  Publication, candidate description, lifted old-target assessment, load
  validation, late participation, certified refresh and copy all call it
  (contract §4–§5). The asymmetry of `D3-a` is gone: an identity-free input is
  traversed, not dropped on one side and rejected on the other.
- **Witness.** `E := Midpoint(D, C+(1,0))` is a compatible retain: `E` keeps its
  identity; `a`, `e`, `g` and the materialized points recompute; `M` stays
  identity-free and unaffected; undo/redo and reopen reconstruct the relation
  (`WIT#witnessRedefineRetainsIdentityAndRecomputesEveryDependent`, §6.4).
- **Vocabulary.** The nine former `AMBIGUOUS` sites are typed
  (`SpatialRedefineDescriptionException`): durable-contract changes, genuine
  ambiguities and undescribable proposals are distinct statuses. One test per
  cause (matrix A1–A11).
- **Durable-contract change.** A changed frontier of an ordinary participant is
  `DURABLE_CONTRACT_CHANGE` with the closed flags
  `identityPreservingUpdateAvailable` and `explicitReplacementAvailable`, each
  executable only through its own mode (contract §9).
- **Desktop.** Every unavailable status shows the kernel reason; a
  durable-contract change offers only the authorized operations among keep
  identity, replace and cancel.

## 3. Migration as decided

- Every new record is version 2. A version-1 record keeps its meaning, validates
  under version 1 and saves byte-identically in its spatial records.
- A version-1 record becomes version 2 only inside its own compatible redefine or
  accepted identity-preserving update, even when both rules give the same set,
  and atomically with that transaction. Mixed documents are valid.
- An unknown record version is rejected with `UNSUPPORTED_VERSION`. A build that
  predates version 2 rejects such a record as `MALFORMED_RECORD` and loads
  nothing (§6.3).
- An ordinary edit that moves a version-2 frontier is refreshed only under the
  closed predicate of contract §10; otherwise the whole edit is rolled back.

## 4. Construction Protocol minimal-reorder refinement

Not integrated in this candidate. Recorded, as the disposition provides, as:

```text
TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER
```

- **Desired behaviour.** An explicit redefine of `A` that introduces one direct
  dependency on a later `B` turns `… A … B …` into `… B A …` by moving only `B`,
  every other element keeping its relative order; undo/redo restore the orders;
  a failed redefine restores the exact previous order; nontrivial cases keep the
  legacy mechanism.
- **Observed legacy behaviour** (scratch probe on this candidate; legacy code
  unchanged from the base). The host never moves `B`. With
  `O, A, X, B, Y, Z` where `Z` depends on `A`, `A := Midpoint(B,O)` gives
  `O, X, B, A, Y, Z`: `A` moves right to just after its latest new predecessor.
  With the same order and no dependent `Z`, the result is `O, X, B, Y, A`: a
  childless object moves to the end of the list. An unlabeled helper in the new
  definition (`Midpoint(B,O+(1,0))`) gives the first result again. A cancelled
  participating redefine leaves the order unchanged.
- **Capability.** `Construction.moveInConstructionList(ConstructionElement,
  toIndex)` bounded by `getMinConstructionIndex()` /
  `getMaxConstructionIndex()`; the legacy order is produced by
  `Construction.prepareReplace` → `updateConstructionOrder`.
- **Reason for deferral.** The host realizes a redefine through two different
  placement strategies: an object with dependents is rebuilt in place after
  `updateConstructionOrder` moves it right, while a childless object is deleted
  and its replacement keeps the replacement's own list position. The bounded
  rule "move only `B`" cannot produce `… B A …` for the childless strategy
  without also placing the replacement element, so applying it only to objects
  with dependents would make the resulting order depend on whether the target
  has dependents. In addition, the ordinary (non-participating) replace path
  captures its failure snapshot after `prepareReplace` has already reordered;
  restoring the exact previous order on failure would require extending that
  snapshot to every eligible ordinary redefine, and retained V2 targets are
  already placed by the kernel's assessed P3-R1 plan, which a `B`-before-`A` move
  would contradict. This exceeds one bounded refinement. Current GeoGebra
  behaviour is preserved, and order is never identity or provenance evidence.

## 5. Findings during implementation

1. **Latent seal violation, fixed.** `AlgebraProcessor` extended the old
   numeric's slider range (`extendMinMax`) after the final pre-mutation
   currentness authorization, even when the old numeric was then replaced. The
   operation-entry snapshot of a participating redefine is captured after that
   call, so a failed replacement could not restore the range. The code path
   existed on the base; `D3-a` made it reachable (a numeric driver redefined to a
   dependent expression). The extension now runs under a semantic transaction
   only in the independent value-assignment branches that keep the old numeric.
2. **Soft redefine is frontier-neutral.** An in-place soft redefinition only
   copies values into unlabeled independent constant inputs; the DAG, hence
   every frontier, is unchanged. No guard was added
   (`REF#anInPlaceSoftRedefineOfAFrontierHelperIsFrontierNeutral`).
3. **Exact rollback of a failed certification.** Without further action a failed
   certified refresh would restore the host snapshot taken after the legacy
   reordering. An ordinary edit that crosses a version-2 frontier now captures
   the existing operation-entry snapshot, so the whole edit is rolled back
   (`REF#aTransitionOutsideTheCertifiedStepFailsClosedAsAWhole`).
4. **Role completeness.** The construction provider admits one-output groups
   only; a participant sharing its parent algorithm with an output without a
   stable role is never auto-refreshed
   (`REF#aRoleIncompleteParticipantIsNeverAutoRefreshed`). An explicit redefine
   of such a participant was, and stays, refused at the host's context
   validation (`REDEFINE_CONTEXT_MISSING`; directly `STALE_ASSESSMENT`).
5. **Retained evaluator debt, new manifestation.** For the inline-literal shape
   of `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY`, the evaluator's isolated
   replay binds the traced point to a duplicated driver. Version-2 validation
   rejects that replay, so a new construction of this shape now fails closed as
   an undefined locus instead of evaluating a wrong fixed locus. A document
   saved by an earlier build keeps its version-1 records and can still show the
   old manifestation. The debt is not repaired.
6. **Description failures with no proposal.** Such an assessment is never
   current, so a caller that nevertheless selects a mode is cancelled as stale
   instead of receiving an error. Nothing is mutated; the Desktop frontend never
   selects a mode for an unavailable status. Pre-existing behaviour, recorded
   only.
7. **Pre-existing clipboard defect (observation; characterization corrected by
   the `PRE-G9B-R3-R1` corrective descendant).** Copying only `a` or only `E` of
   the witness yields a payload whose closure is incomplete: an unselected
   predecessor whose command has a constant axis input (`f = Line(C, xAxis)`,
   `D = Intersect(f, yAxis)`) is renamed for the clipboard but not serialized.
   The paste then fails identity validation (`MALFORMED_RECORD`). The inner XML
   restore returns the construction objects, durable identities, identity
   records, DAG, spatial section, rich-result ledgers and undo history exactly
   to their pre-paste state. The outer clipboard rollback then fails, because it
   enters rollback mode without the runtime restore protocol ("Spatial rollback
   restore is not active"). `blockUpdateScripts=true` leaks, is saved as
   `<scripting blocked="true">` and survives reopening, so the reopened document
   keeps its scripts disabled. Related clipboard cases degrade copied helpers
   silently instead of rejecting them; they need separate repair analysis.
   Reproduced identically by scratch probes on the extracted base tree
   `ef120a54`; copying the whole construction works and is covered (P8). Not
   caused by `R3` and outside the phase's permitted scope. Recorded as two
   retained debts owned by `PRE-G9B-R3-C1` (§8).

## 6. Evidence

### 6.1 Focal tests

| Class | Methods | Result |
|---|---|---|
| `org.geocedg.common.locus.PreG9bR3DurableDependencyProjectionTest` | 8 | pass |
| `org.geocedg.common.locus.PreG9bR3RedefineWitnessTest` | 5 | pass |
| `org.geocedg.common.locus.PreG9bR3DurableContractRedefineTest` | 9 | pass |
| `org.geocedg.common.locus.PreG9bR3CertifiedFrontierRefreshTest` | 5 | pass |
| `org.geocedg.common.kernel.spatial.identity.PreG9bR3RedefineVocabularyTest` | 13 | pass |
| `org.geocedg.desktop.PreG9bR3RedefineFrontendTest` | 7 | pass |

`PreG9bR3RedefineTestBase` is an abstract helper. Three existing tests change
only their pins (matrix §8); no existing test method was added or removed.

### 6.2 Full suites and static checks

Full-suite evidence was produced with the official
`tools/agent/checks/gradle-test-evidence-producer.ps1` on the candidate working
tree (`--rerun-tasks --no-build-cache`):

| Selection | JUnit files | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| `final.shared` | 677 | 6 784 | 0 | 0 | 10 |
| `final.desktop` | 100 | 1 523 | 0 | 0 | 1 |

Skipped identities are exactly the inventory's allowlisted `DISABLED` and
`CONDITIONAL` cases. Checkstyle (`:shared:common:checkstyleMain`,
`:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`,
`:desktop:desktop:checkstyleTest`) reports no finding. The upstream boundary
check (`Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256…`) passes with 800
registered files.

### 6.3 Compatibility with a build that predates version 2

A scratch probe opened three documents written by this candidate in the base
build `ef120a54`, built from an extracted copy of its tree:

| Document | Base-build result |
|---|---|
| historical `locusFromMidpoint` fixture, reopened and saved | opened, 4 records |
| the same fixture after one compatible redefine (mixed) | rejected: `MALFORMED_RECORD` "base contract disagrees with the attached geo"; empty registry, not file-loading |
| a new witness construction (all version 2) | rejected identically |

### 6.4 Author witness replay

The author's archive `artifacts/author-input/post-p1-defects/SeveralDefects.cedg`
(untracked, SHA-256
`c0cbdb8ea218e4a07b7cd4d7bcfb4ba2b74da373bbd49711aa4657349619dff8`) was replayed
read-only by a scratch Desktop probe joined to the test source set for one
invocation. It loads the archive in a Classic 5 application, installs a
recording presentation, submits `E=Midpoint(D,C+(1,0))` through
`GeoCeDGAlgebraInputSubmission`, then undoes, redoes, saves as native `.cedg`
and reopens.

| Observation | Result |
|---|---|
| records on load | all version 1; `E` depends on `C` only |
| dialogs shown | none (compatible retain) |
| identities of `C`, `E`, `a`, `d`, `e`, `g`, `I`–`L` | unchanged; `D` and `M` stay identity-free |
| `E` after the redefine | version 2, dependencies `[C]`, definition revision 1, topology revision 0; its coordinates now coincide with `M` |
| other records | still version 1 (mixed document) |
| `g = Intersect(a,c)` | four roots recomputed, all point-admissible; `I`–`L` stay defined and follow the four new roots |
| `e = Intersect(d,a)` | two roots recomputed; before the redefine both were point-admissible, afterwards they are rich-only (see below) |
| undo | restores version 1, the former coordinates and both admissible `e` roots |
| redo | restores the redefined state byte-identically in XML |
| native save and reopen | identical identities, records, roots and coordinates |

After the redefine, `a`'s generator slice contains the expression point
`C+(1,0)`. The certified construction model (`certified-construction-program/v1`,
ADR 0028) does not admit expression points, so the pair `e` recomputes but stays
rich-only: `PRE-G9B-R2-E0/E1` fail-closed behaviour (its validation matrix B7),
not an `R3` effect. It matters for the author smoke.

### 6.5 Coupled reconciliations

**JUnit inventory.** Regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from dry-run discovery of both modules and the passing `final.shared` and
`final.desktop` runs of §6.2:

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 827 | 5 867 |
| `final.shared` | 6 744 | 6 784 |
| `discovery.desktop` / module `desktop` | 1 522 | 1 529 |
| `final.desktop` | 1 516 | 1 523 |

No narrow selection changed, in count or in identity hash; in particular
`post-g9u1-a3.narrow` keeps 114 identities and `9a45556c…`.

**Typed registry.** The `junit_inventory` catalog pin moves from `88b5abe8…` to
`bb91d7cb701d7b769cf6b25b9d72c95460608488bc7706b23fa34836d7dbfec6`, by
canonical-LF SHA-256; the method first reproduced `88b5abe8…` exactly from the
base's committed inventory. One line changes; line endings are unchanged.
`tools/agent/tests/verification-final-coverage.Tests.ps1` passes (8 cases, 308
assertions).

**Upstream modification record.** `docs/upstream/modified-files.yml` registers
the nine new files, appends the `PRE-G9B-R3` purpose to the sixteen modified
entries and completes the four `PRE-G9B-R2-E0/E1` purposes of
`TD-UPSTREAM-RECORD-E0-E1-PURPOSE`. The boundary check passes with 800 files.

## 7. Verification

`-PlanOnly` before commit (after the inventory update; the hashes equal those
resolved before it):

| Command | Exit | Coverage | Missing | Nodes | Plan hash |
|---|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile FINAL -PlanOnly` | 0 | `COMPLETE` | 0 | 67 | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |
| `tools/agent/verify.ps1 -Profile PHASE -Phase POST-G9U1-A3 -PlanOnly` | 0 | `COMPLETE` | 0 | 4 | `c2e25e0b5d28523023f88dc139f015cda004ce28cc94d1ff546a65c4c222876f` |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |

**Containment.** All 46 `INTEGRATION` nodes are `FINAL` nodes; `FINAL` adds the
21 `infra.*` verification-core checks. Of the four `PHASE POST-G9U1-A3` nodes,
`compile.shared.producer` and `compile.shared.semantic` are `FINAL` nodes with
identical content. The remaining pair, `junit.shared.post-g9u1-a3.producer` and
`.semantic`, runs the `post-g9u1-a3.narrow` selection (11 classes, 114
identities). `FINAL` runs the unfiltered `final.shared` selection instead, and
its semantic projection authenticates every one of the 6 784 pinned identities,
which include the 114. An offline cross-check with the verifier's own JUnit
reader and deterministic hash recomputes the narrow selection from the
`final.shared` evidence of §6.2: 114 identities, none failed, fingerprint
`9a45556ceade0331c9891b1e41cf32abb285d83699b4148cdb2028e222048024`, equal to the
pin (likewise `post-g9u1-a3-frontend.shared`, 3 identities). The narrow
selection's own count is also asserted inside `FINAL` by `infra.final-coverage`.
The global serialization and persistence obligations are carried by the full
suites, including every `G9A*`, `G9U0`, `POST-G9U1-A3` and `PreG9bR3*`
persistence, copy, undo and native-archive class.

**Decision.** One `FINAL` campaign completely executes and authenticates the
`POST-G9U1-A3` PHASE, `INTEGRATION` and global serialization obligations, so it
is the only campaign (disposition item 14): no PHASE or INTEGRATION run is
repeated. After the campaign the same cross-check is repeated on its own JUnit
evidence. `FINAL` runs on the committed candidate with a clean worktree, with
`-LogDirectory artifacts/agent/pre-g9b-r3-final` and the console redirected
outside `artifacts/`; its receipt binds that commit and is reported
separately.

## 8. Ri quality-debt ledger

At phase entry the retained debts were inspected against the phase footprint
(Ri policy rule 1).

| Identifier | Disposition in `R3` |
|---|---|
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | **retained**; no overlap with the evaluator machinery |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | **retained**; not repaired; its manifestation for new constructions changed to fail-closed (§5.5), recorded in both guide editions |
| `TD-UPSTREAM-RECORD-E0-E1-PURPOSE` | **resolved** synergistically: the four entries now state their E0/E1 purpose; R3 already updates the record and shares its campaign |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | **new, retained** (§4); owner and disposition due by the `PRE-G9B-R7` closeout |
| `TD-DOC-PRE-G9B-R-ROADMAP-STATUS-RECONCILIATION` | **new, retained**: the roadmap still lists `R1-R1`, `R2`, `R2-E0` and `R2-E1` as `DESIGNED`/`PROPOSED — NOT AUTHORIZED` (the "Siguiente puerta" row and the track table), although their author closeouts are recorded in `docs/validation/pre_g9b_r1_r1_closeout_record.md`, `pre_g9b_r2_closeout_record.md` and `pre_g9b_r2_e0_e1_closeout_record.md`, and `R3` is now a technical candidate. Correcting it would mark roadmap items as approved, which an agent may not do; it needs an author-directed documentation reconciliation before the `PRE-G9B-R7` closeout |
| `TD-R3-OBS-CLIPBOARD-PARTIAL-CLOSURE-PASTE` | **new, proposed** (§5.7); superseded in `PRE-G9B-R3-R1` by `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` and `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE`, both owned by `PRE-G9B-R3-C1`; not repaired in `R3` |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged; not recurring, since `R3` runs no PHASE campaign |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |

The current state of this ledger is recorded by the
[`PRE-G9B-R3-R1` corrective report](pre_g9b_r3_r1_corrective_candidate_report.md).

## 9. Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (redefine assessment, execution modes, certified refresh, Desktop dialog)
SERIALIZATION / DOCUMENT FORMAT    = CHANGED: new record value schemaVersion="2"; XML section and attribute set unchanged; version-1 records unchanged
BACKWARD COMPATIBILITY             = historical documents open and save unchanged
FORWARD COMPATIBILITY              = a build that predates version 2 rejects version-2 records cleanly
FRONTEND                           = CHANGED (Desktop reason surfacing and durable-contract choices; Web unchanged)
EVALUATOR                          = UNCHANGED (both debts retained)
CONSTRUCTION ORDER                 = UNCHANGED (refinement deferred)
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED (JUnit inventory, registry catalog pin); no verifier code change
UPSTREAM MODIFICATION RECORD       = UPDATED (9 new files; R3 purposes; the four E0/E1 purposes corrected)
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = REQUIRED — APPLIED (both user-guide editions: §3.6, §4.3, known limitations; developer guide)
```

## 10. Authorization state

The disposition authorizes this phase's implementation and one `FINAL`
campaign. It does not authorize repair of the retained evaluator debts, the
deferred reorder refinement, the clipboard defect, `R4` or any later phase,
`G9B`, `G9C`, `G9U2`, productive `G10`, or release and publication. Push, branch
publication, merge, promotion, tag, release and binary publication each require
a separate explicit author instruction naming the exact candidate SHA. The phase
stops for author review and the canonical `R3` author smoke.

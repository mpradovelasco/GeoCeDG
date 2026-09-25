# PRE-G9B-R3-R1 corrective descendant report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R3-R1, bounded corrective descendant of PRE-G9B-R3
KIND                      = NORMATIVE + TEST + DOCUMENTATION + ROADMAP ONLY
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE (frozen at the start of this descendant, section 8)
FROZEN_ACCEPTANCE         = PHASE -Phase POST-G9U1-A3, INTEGRATION
BASE_TECHNICAL_CANDIDATE  = c1f3ed61691eb351c06caf2283e90bcc28152d76 (tree ca913cb23f5303c8b802fbfd5eea9b9d403358e9), preserved unchanged
BASE_PARENT               = ef120a54f035b85df014e89c3916cb3a26f5f933
CORRECTIVE_COMMIT         = the commit that contains this artifact; not nameable inside it, reported separately
PRODUCT_IMPLEMENTATION    = UNCHANGED (section 2)
PRE_G9B_R3_AUTHOR_SMOKE   = PASS (author-provided, section 6)
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: the separate closeout/author-decision record is the sole
authority for current author approval status. `PRE-G9B-R3` is not author
approved by this descendant.

The frozen `PRE-G9B-R3` candidate is preserved unchanged. This descendant
applies only the author's `PRE-G9B-R3-R1` instruction and the Option 1 decision
with its refinement. The living normative documents are the
[durable dependency projection contract](../../geocedg/specs/spatial/durable-dependency-projection.md)
1.1, [ADR 0029](../adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md)
as amended, the extension note of
[ADR 0011](../adr/0011-g9-spatial-persistence-and-phase-gates.md), the amendment
candidates of the
[G9 spatial projection semantics](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md)
§8.2 and the
[G9U1 construction interaction](../../geocedg/specs/ui/g9u1-construction-interaction.md)
§10, and the [compatible-redefine design §7](../architecture/post_g9u1_a3_v2_compatible_redefine.md).
Machine-readable evidence:
[`geocedg/validation/pre-g9b-r3/pre-g9b-r3-r1-corrective-evidence.json`](../../geocedg/validation/pre-g9b-r3/pre-g9b-r3-r1-corrective-evidence.json).

## 1. What the author decision disposes

- **Decision A.** A change of the authoritative frontier of a version-2
  participant caused by an ordinary upstream semantic edit is the lifecycle
  event `CERTIFIED_DURABLE_FRONTIER_REFRESH`, authorized only under the R3
  compatibility predicate, where definition and topology revisions advance.
- **Decision B, Option 1 with refinement.** Late participation is a distinct
  lifecycle event, `LATE_PARTICIPATION_REPROJECTION`: the record is re-projected
  under its own version (version 1 `DIRECT`, version 2
  `TRANSITIVE_DURABLE_FRONTIER`); a valid prospective publication publishes the
  refreshed set atomically and keeps identity, roles, `definitionRevision` and
  `topologyRevision`; no migration and no R3 redefine compatibility predicate;
  a failed ordinary publication validation rejects the whole publication. The
  positive normative fixture is a fully valid single-output participant; the
  role-incomplete shape stays characterization evidence only.
- **Decision C.** Undo and redo restore the committed serialized snapshot and
  never migrate by themselves.
- Documentation corrections, the author smoke record, the roadmap
  reconciliation and the registration of `PRE-G9B-R3-C1` and `PRE-G9B-R3-X1`
  before `PRE-G9B-R4`.
- No productive behaviour change.

A stop was reported before any edit, because decision B as first written
(revisions advancing and the certified predicate applied on late participation)
contradicted the frozen implementation. The author then selected Option 1,
which matches it; nothing in this descendant changes behaviour.

## 2. Product implementation unchanged

Diff classification against `c1f3ed61…`:

| Class | Files |
|---|---|
| normative specification | `geocedg/specs/spatial/durable-dependency-projection.md` (1.0 → 1.1), `geocedg/specs/spatial/g9-spatial-projection-semantics.md` (§8.2 amendment candidate, additive), `geocedg/specs/ui/g9u1-construction-interaction.md` (§10 amendment candidate, additive) |
| ADR reconciliation | `docs/adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md`, `docs/adr/0011-g9-spatial-persistence-and-phase-gates.md` (extension note, additive; table unchanged) |
| design record | `docs/architecture/post_g9u1_a3_v2_compatible_redefine.md` (§7 bullets) |
| tests | `source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9bR3R1LateParticipationTest.java` (new) |
| Javadoc | `source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/SpatialIdentityRegistry.java` (three comment lines of one Javadoc block) |
| documentation | `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md` (§3.6), `docs/developer/geocedg_developer_guide.md` |
| validation evidence | `docs/validation/pre_g9b_r3_candidate_report.md` (§5.7, §8), `geocedg/validation/pre-g9b-r3/pre-g9b-r3-evidence.json` (clipboard characterization), this report and its evidence file |
| verification catalog data | `geocedg/specs/operations/verification-junit-inventory.json` (regenerated), `geocedg/specs/operations/verification-registry.json` (catalog pin only) |
| upstream record | `docs/upstream/modified-files.yml` (one new test file; one purpose appended) |
| roadmap | `docs/roadmap/geocedg_roadmap.md` |

The only productive source file touched is `SpatialIdentityRegistry.java`, and
only three lines of the Javadoc of `refreshNewlyParticipatingDependencies`
change, one for one, so every line number is unchanged. All 5 432 class files
of `:shared:common` main were hashed before the edit and again after
recompilation, and they are byte-identical: the SHA-256 of the sorted hash list
is `d637d0869929af8fbff734eb83235598506551100a2d2344b472022c2a4c4b5f` in both
states. No other main, build, packaging, bootstrap, verifier, registry-schema or
serialization file changes.

## 3. Normative reconciliation

- **Certified durable-frontier refresh** (contract §10, ADR 0029 Decision 4).
  An ordinary edit of an identity-free geo that changes the frontier of a
  version-2 ordinary participant keeps the identity only under the five-condition
  predicate; the record then gets the new frontier and both revisions advance,
  which records a certified semantic change of the durable dependency contract,
  not an accident of recomputation. Otherwise `REDEFINE_INCOMPATIBLE` and the
  exact operation-entry snapshot restore the whole edit. It is distinct from
  value-only recomputation, explicit redefine, replacement and late
  participation.
- **Lifecycle revision advancement** (contract §15). Ordinary recomputation
  with every frontier unchanged moves no definition or topology revision; the
  certified refresh advances both; late participation keeps both; explicit
  redefine and the contract update follow class B; replacement, copy and
  recreation create new records; undo, redo and reopen restore records exactly.
- **Version-2 late participation** (contract §14, ADR 0029 Decision 11). A geo
  on the projection path of an already-participating record (version 1: direct
  inputs; version 2: identity-free geos visited by the frontier walk) that first
  acquires a durable identity re-projects the record under its own version inside
  the same atomic publication; identity, roles, both revisions and the version
  are kept; no redefine or refresh predicate runs; a failed publication is
  rejected as a whole. Identity is read only from the DAG and durable identities.
- **Relation to the historical G9U1 rule.** G9U1 §10 remains authoritative and
  unchanged for the version-1 `DIRECT` records to which it originally applied;
  for version-2 records the frontier walk takes the place of the direct inputs,
  with the same preservation (G9U1 §10 amendment candidate). ADR 0011 classes
  A–G and the G9 §8.1 table are unchanged; the two events are added beside
  class A (ADR 0029 Decision 12, ADR 0011 extension note, G9 §8.2).
- **Undo, redo and versions** (contract §7.1, ADR 0029 Decision 3). Undo and
  redo restore the committed serialized snapshot including every record's
  version: version-1 state → semantic event → committed version-2 state → undo
  → version-1 snapshot → redo → version-2 snapshot is valid and migrates nothing.

## 4. Late-participation focal test

`org.geocedg.common.locus.PreG9bR3R1LateParticipationTest`, 3 methods, all pass
on this descendant:

| Method | Fixture and assertions |
|---|---|
| `aTraversedHelperThatBecomesDurableJoinsTheFrontierUnderTheSameIdentity` | author witness core `f=Line(C,xAxis)`, `D=Intersect(f,yAxis)`, `E=Midpoint(D,C)`, `a=LocusV2(E,C)`: `E` is version 2 with `[C]`; `f` and `D` have no identity. The real event `h=Intersect(a,f)` gives `f` a durable identity. No redefine assessment is consulted; `E` equals its former record with dependencies `[C, f]` by durable identity only, with the same identity, provider, family, schema and version, authority, binding and stable role, cardinality, copy source and both revisions; the only new records are those of `f` and `h`, and every other record is unchanged. Undo restores the exact former spatial records and `E` record, with `f` identity-free and `h` absent; redo restores the re-projected records; the saved document reopened in a fresh application yields the same records and `E` record. |
| `aLatePublicationKeepsBothRevisionsAndARejectedOneRefreshesNothing` | a standalone registry with `E` at definition revision 7 and topology revision 3: an invalid publication of the traversed helper `f` is rejected, leaving the spatial section and the `E` record untouched and `f` unattached; the valid publication re-projects `E` to `[C, f]` and keeps revisions 7 and 3, so a preserved revision is distinguishable from a reset one. |
| `aVersionOneRecordReprojectsOnlyItsDirectInputsAndIsNeverMigrated` | the byte-pinned historical fixture `locusFromMidpoint.cedg`: `f` becoming durable leaves the version-1 record of `E` exactly unchanged, because `f` is not a direct input; `D` becoming durable (`k=LocusV2(D,C)`) re-projects it under `DIRECT` to `[C, D]`, still version 1 with its revisions; a fresh reopen keeps it. |

The whole-publication rejection of a failed late publication is also pinned by
the existing `G9U1ConstructionParticipationReviewTest`
(`failedRuntimeSwitchRollsBackNewInputAndExistingDerivedRecordTogether`,
`malformedNewBatchCannotRefreshOrPartiallyPublishAnything`), whose records now
carry version 2, and its `lateParticipationPreservesExistingListIdentityAndRevisions`
pins the direct-input case with revisions 7 and 3.

**Characterization evidence, not a guarantee.** A scratch probe on the frozen
candidate made `f` durable for a role-incomplete participant `P`, the first
output of the two-output `Intersect(c,f)`: the record of `P` was re-projected
from `[C, c]` to `[c, f]` with its revisions kept, and the event succeeded. This
contract adds no guarantee for that shape (contract §14); the ordinary-edit
refresh still refuses it (`PreG9bR3CertifiedFrontierRefreshTest#aRoleIncompleteParticipantIsNeverAutoRefreshed`).

## 5. Documentation corrections

- **Clipboard characterization.** The `R3` candidate report §5.7 and its
  machine-readable evidence now state the characterized defect exactly: an
  incomplete payload closure for a predecessor whose command has a constant
  axis input; identity-validation failure on paste; an inner XML restore that
  returns construction objects, durable identities, identity records, DAG,
  spatial section, rich-result ledgers and undo history exactly; an outer
  clipboard rollback that fails because it enters rollback mode without the
  runtime restore protocol; `blockUpdateScripts=true` leaking, saved as
  `<scripting blocked="true">` and kept after reopening; and silent degradation
  of copied helpers in related cases, left to separate repair analysis. Both
  files mark the correction.
- **User guide §3.6**, both editions: a point already materialized from the
  intersection of two semantic curves follows a recomputation only while its root
  stays admissible under a certified model; if the pair becomes rich-only, its
  roots may still be listed but can no longer be materialized, an existing point
  may become undefined, it is never moved to another root, and undo can restore
  the earlier admissible state.
- **Javadoc** of `SpatialIdentityRegistry.refreshNewlyParticipatingDependencies`
  distinguishes the version-1 direct inputs from the version-2 frontier walk.
  The Javadoc of `ConstructionGeoRedefineProvider.durableDependencyGeos`, outside
  the authorized item, still describes the direct geos' identities as "this
  output's durable dependency edge set", which is exact only for version 1; it is
  recorded here, not changed.

### Superseded statements

| Former statement | Now |
|---|---|
| `R3` report §5.7: copying without the objects a participant is built from fails, "leaving the construction changed" | the construction content is restored exactly; the leaked scripts-blocked flag is the persistent change; the payload omission concerns predecessors with a constant axis input (§5) |
| `R3` ledger: `TD-R3-OBS-CLIPBOARD-PARTIAL-CLOSURE-PASTE`, new, proposed | superseded by `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` and `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE`, owned by `PRE-G9B-R3-C1` |
| contract §7.1: "undo/redo … never change a record's version" | undo and redo restore the committed snapshot, versions included, and never migrate |
| contract §5: late participation named only as a use | defined as `LATE_PARTICIPATION_REPROJECTION` (§14) |
| guide §3.6: "its intersections and the points materialized from them are recomputed" | pair-materialized points follow only while their root stays admissible |

## 6. Author smoke record

```text
PRE_G9B_R3_AUTHOR_SMOKE = PASS
```

Author-provided evidence, recorded as provided; no automated run was created
for it. Tested scope: the canonical `R3` witness; its redefine as a compatible
retain; identity preservation; the behaviour of the dependent locus and its
intersections; undo; redo; native save and reopen. The known certified
expression-point limitation (`TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT`)
is not a failure of the smoke. This record is not an author approval of
`PRE-G9B-R3`.

## 7. Roadmap reconciliation

From the existing author-approved closeout records only:

| Phase | Roadmap state now | Authority |
|---|---|---|
| `PRE-G9B-R1-R1` | `PASS — AUTHOR APPROVED` | [closeout record](pre_g9b_r1_r1_closeout_record.md) |
| `PRE-G9B-R2` | `PASS — AUTHOR APPROVED` | [closeout record](pre_g9b_r2_closeout_record.md) |
| `PRE-G9B-R2-E0/E1` | `PASS — AUTHOR APPROVED` | [closeout record](pre_g9b_r2_e0_e1_closeout_record.md) |
| `PRE-G9B-R3` | technical candidate frozen; automated verification `ACCEPTED / COMPLETE`; author smoke `PASS`; `PRE-G9B-R3-R1` in progress; author approval pending | this report |
| `PRE-G9B-R3-C1` | `DESIGNED / PLANNED — NOT AUTHORIZED FOR IMPLEMENTATION`; severity `HIGH` | author decision of this task |
| `PRE-G9B-R3-X1` | `DESIGNED / PLANNED — NOT AUTHORIZED FOR IMPLEMENTATION`; capability extension | author decision of this task |
| `PRE-G9B-R4` | `DESIGNED — NOT AUTHORIZED`, after `R3-C1` and `R3-X1`, objective unchanged | roadmap |

The header rows, the track status and the sequence table were reconciled; the
stale statement that `G9-R4-PERIODIC-QUARANTINE-NATIVE-ROUNDTRIP` remained open
now reads `RESOLVED`, as the `PRE-G9B-R1-R1` closeout records. The execution
order `R3` closeout → `R3-C1` → `R3-X1` → `R4` is an execution-order decision,
not a semantic dependency. This resolves
`TD-DOC-PRE-G9B-R-ROADMAP-STATUS-RECONCILIATION` under the author's direction.

## 8. Verification

### Frozen class and campaign

Before any edit the descendant froze `VERIFICATION_CLASS = INTEGRATED_PHASE`
with the acceptance `PHASE -Phase POST-G9U1-A3` plus `INTEGRATION`, under the
class rules of accepted ADR 0025 (verification-levels §12.8) and the `R3`
prompt's original plan. The impact facts that select it:

- the product implementation is bytecode-identical to the FINAL-accepted
  `c1f3ed61…` (section 2), so the reason for `R3`'s `GLOBAL_IMPACT` escalation
  (publication projection, persisted signatures, serialization) is absent from
  this delta;
- the Javadoc change lies inside the perimeter of the `POST-G9U1-A3` selection
  (`kernel/spatial/identity`), which `PHASE` authenticates with its pinned
  114-identity narrow selection;
- the new focal test is registered only in `final.shared` (no phase selection
  covers `PreG9bR3*`), and `INTEGRATION` is the smallest profile whose nodes
  execute `final.shared` and `final.desktop` and project the regenerated JUnit
  inventory; this is the concrete additional integration obligation;
- documentation, roadmap and upstream-record changes are covered by the
  repository-safety and diagnostic nodes that `INTEGRATION` includes.

The verifier implements no cross-run evidence reuse, so evidence bound to
`c1f3ed61…` is not reused for this commit. `FINAL` is not run: the frozen class
does not require it, and ADR 0025 forbids a campaign above the frozen plan
without a `VERIFICATION_ESCALATION_REQUEST`. A `FINAL` receipt exists only for
`c1f3ed61…`; an identity-only receipt closeout of this descendant would need a
`FINAL` on it and therefore an author escalation. The accepted precedent is the
`PRE-G9B-R2-E0/E1` corrective descendant, approved on `PHASE` and `INTEGRATION`
evidence bound to its own commit.

The frozen campaigns run on the committed descendant with a clean worktree and
are reported separately, because their results bind that commit.

### Local evidence before commit

| Check | Command or method | Result |
|---|---|---|
| focal test | `gradlew :shared:common-jre:test --tests org.geocedg.common.locus.PreG9bR3R1LateParticipationTest` | 3 of 3 pass |
| full shared suite | official `gradle-test-evidence-producer.ps1`, selection `final.shared` (`--rerun-tasks --no-build-cache`) | 678 JUnit files, 6 787 identities: 6 777 passed, the 10 allowlisted skips, 0 failed or errored; it runs every `PreG9bR3*` class, `G9U1ConstructionParticipationReviewTest`, the G9A1–G9A3 lifecycle suites and the 11 `POST-G9U1-A3` narrow classes |
| discovery | the same producer, `discovery.shared` and `discovery.desktop` with `--test-dry-run` | 5 870 and 1 529 identities |
| targeted Desktop | `gradlew :desktop:desktop:test` for `PostP1BilingualUserGuideTest`, `PreG9bR3RedefineFrontendTest`, `PreG9BP1PublicSurfaceTest`, `G9U1WorkspaceSurfaceTest`, `G9U1IconReviewTest` | 68 tests, 0 failures, including the byte-copy packaging and parity of both edited guide editions |
| verification-core tests that read the live registry or inventory | `pwsh -NoProfile -File tools/agent/tests/<name>.Tests.ps1` | all exit 0: final-coverage 8 cases / 308 assertions, registry 22 / 62, contract-boundary 5 / 38, cli-compatibility 7 / 45, gradle-producer 7 / 21, supervisor 12 / 51, runtime 119 / 119 fake-first tests |
| checkstyle | `gradlew :shared:common:checkstyleMain :shared:common-jre:checkstyleTest` | 0 findings |
| bytecode identity | SHA-256 of every `:shared:common` main class file before the Javadoc edit and after recompilation | 5 432 files byte-identical |
| upstream boundary | `Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b7df401ff056c37b502d82df4d72b1522` | passes with 801 registered files |
| documentation structure | every relative Markdown link of the changed documents and of this report | 0 unresolved |
| whitespace | `git diff --check`, new file included | clean |

`-PlanOnly` after the inventory update:

| Command | Exit | Coverage | Missing | Nodes | Plan hash |
|---|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile PHASE -Phase POST-G9U1-A3 -PlanOnly` | 0 | `COMPLETE` | 0 | 4 | `c2e25e0b5d28523023f88dc139f015cda004ce28cc94d1ff546a65c4c222876f` |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |
| `tools/agent/verify.ps1 -Profile FINAL -PlanOnly` (reference only, not run) | 0 | `COMPLETE` | 0 | 67 | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |

All three plan hashes equal those resolved for `c1f3ed61…`; the descendant leaves
the resolved plans unchanged.

### Coupled reconciliations

**JUnit inventory.** Regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from the two dry-run discoveries and the passing `final.shared` run above;
`final.desktop` keeps its pins because no Desktop test changed:

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 867 | 5 870 |
| `final.shared` | 6 784 | 6 787 |
| `discovery.desktop` / module `desktop` | 1 529 | 1 529 |
| `final.desktop` | 1 523 | 1 523 |

No narrow selection changed, in count or in identity hash; `post-g9u1-a3.narrow`
keeps 114 identities and `9a45556c…`.

**Typed registry.** The `junit_inventory` catalog pin moves from `bb91d7cb…` to
`9a7300fd8566247e03393dc74cd997b946cd7a64007a7ba1403917d2e14d21a6`, by canonical-text SHA-256
(`Get-VerificationCanonicalTextSha256`); the method first reproduced `bb91d7cb…`
exactly from the unchanged inventory. One line changes; line endings are
unchanged.

**Upstream modification record.** `docs/upstream/modified-files.yml` registers
the new test file and appends the `PRE-G9B-R3-R1` purpose to the
`SpatialIdentityRegistry.java` entry. The boundary check passes with 801 files.

## 9. Ri quality-debt ledger

Checked at the entry of this descendant (Ri policy rule 1); the footprint
overlaps no retained debt except the roadmap reconciliation.

| Identifier | State after `PRE-G9B-R3-R1` |
|---|---|
| `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` | **new, retained**; owner `PRE-G9B-R3-C1`; severity `HIGH`; supersedes, with the next row, `TD-R3-OBS-CLIPBOARD-PARTIAL-CLOSURE-PASTE` |
| `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE` | **new, retained**; owner `PRE-G9B-R3-C1` |
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | **new, retained capability identifier**; owner `PRE-G9B-R3-X1`; not a defect |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | retained, unchanged |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | retained, unchanged |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained, unchanged; owner and disposition due by the `PRE-G9B-R7` closeout |
| `TD-DOC-PRE-G9B-R-ROADMAP-STATUS-RECONCILIATION` | **resolved** in this descendant under the author's direction (section 7) |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |

The three new identifiers stay distinct: two independent clipboard defects and
one capability extension.

## 10. Impact

```text
PRODUCT_PHASE_EFFECT               = NONE (bytecode-identical product implementation)
NORMATIVE                          = CLARIFIED (lifecycle events named and reconciled; undo/redo wording; approved authorities amended additively as candidates)
SERIALIZATION / DOCUMENT FORMAT    = UNCHANGED
FRONTEND                           = UNCHANGED
VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY (JUnit inventory regenerated, registry catalog pin); no verifier code or policy change
UPSTREAM MODIFICATION RECORD       = UPDATED (one new test file; one purpose appended)
BOOTSTRAP IMPACT                   = NO_CHANGE_REQUIRED
GUIDE_IMPACT                       = REQUIRED — APPLIED (both user-guide editions §3.6; developer guide)
```

## 11. Authorization state

This descendant authorizes nothing. `PRE-G9B-R3` author approval is pending and
is not recorded here. `PRE-G9B-R3-C1` and `PRE-G9B-R3-X1` are planned and not
implemented; each requires a separate explicit author authorization with an
exact base commit. `PRE-G9B-R4` and every later phase, `G9B`, `G9C`, `G9U2` and
productive `G10` remain unauthorized. No clipboard repair, expression-point
certification, `certified-construction-program/v2` or Construction Protocol
reorder is implemented. Push, promotion, merge, tag, release and binary
publication each require a separate explicit author instruction naming the
exact commit.

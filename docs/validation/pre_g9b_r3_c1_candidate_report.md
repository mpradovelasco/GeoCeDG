# PRE-G9B-R3-C1 clipboard correctness candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R3-C1, clipboard atomic rollback and predecessor closure
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozenAtPhaseStart = true)
PLANNED_ACCEPTANCE_LEVEL  = FINAL, one campaign on the exact candidate
IMPLEMENTATION_BASE       = 89d481b1b3eef36c429f920178d5e5de6c0149e3 (tree 56a9823c602710805dfc18bd703f6a0d0b6ed5fb)
BRANCH                    = feature/pre-g9b-r3-c1-clipboard-correctness
CANDIDATE_COMMIT          = the commit that contains this artifact; not nameable inside it, reported separately
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a separate closeout/author-decision record is the sole
authority for current author approval status.

The phase executes the author's explicit `PRE-G9B-R3-C1` instruction of
2026-09-28. The [design record](../architecture/pre_g9b_r3_c1_clipboard_atomicity_and_closure_design.md)
gives the mechanism, and the [validation matrix](pre_g9b_r3_c1_validation_matrix.md)
maps every obligation to evidence. Machine-readable evidence is in
[`geocedg/validation/pre-g9b-r3-c1/pre-g9b-r3-c1-evidence.json`](../../geocedg/validation/pre-g9b-r3-c1/pre-g9b-r3-c1-evidence.json).

## 1. Entry, housekeeping and declarations

- **Entry identity.** At entry, `HEAD`, local `main`, `origin/main` and live
  remote `main` all resolved to `89d481b1…`, the published `PRE-G9B-R3` closeout
  (`PRE-G9B-R3 = PASS — AUTHOR APPROVED`). The worktree and index were clean.
- **Local branch cleanup** (authorized housekeeping, no commit):
  - `git branch --merged main` listed 54 branches besides `main`.
  - 51 were deleted with `git branch -d`.
  - 3 were retained because `tools/agent/verify-g9u1-construction-workspace.ps1`
    pins them as protected refs:
    - `codex/g9u1-construction-workspace-after-r1`
    - `codex/g9u1-author-review-stabilization-1`
    - `codex/g9u1-author-review-stabilization-2`
  - The unmerged `feature/g9u1-construction-workspace-planning` and
    `feature/g9u1-construction-workspace-planning-after-r6` were not touched.
  - No remote branch, remote-tracking ref or tag changed.
- **Branch.** `feature/pre-g9b-r3-c1-clipboard-correctness`, created from
  `89d481b1…`; not pushed.
- **Class.** `GLOBAL_IMPACT` was frozen before any productive edit, with `FINAL`
  as the planned acceptance, because the change affects:
  - shared transaction and rollback semantics;
  - durable-identity copy and paste;
  - the clipboard, import and XML restore seams;
  - persistent document state;
  - both the shared and the Desktop paths.
- **Canonical prompt.** None is created. Governance does not require a durable
  task prompt for a closable phase: the corrective phases `PRE-G9B-R2-E0/E1` and
  `PRE-G9B-R3-R1` closed without one, and closeout inspects only a commit and a
  receipt. The author instruction is the execution authority.
- **Readiness.** The current `phase-closeout.ps1 -Action READINESS` is an
  identity-only inspection that needs an existing acceptance receipt, and
  `verify.ps1` takes no readiness input. No pre-campaign readiness gate exists to
  run, and none was run for `PRE-G9B-R3`.

## 2. Authority read

In this order:
1. the current source, tests and serialization behaviour on the base;
2. `AGENTS.md`;
3. the G9A1 design (`CLIPBOARD_IMPORT` requires a complete declared closure);
4. ADR 0011 class F and the G9 spatial projection semantics copy row;
5. the G9A3 and G9U1 lifecycle rules;
6. the R3 and R3-R1 contracts;
7. the `PRE-G9B-R3` closeout record;
8. the `PRE-G9B-R3-R1` report;
9. the roadmap `R3-C1` entry.

The approved contracts already specify the required behaviour. `C1` restores it
and adds no semantic choice, ADR or serialization version.

## 3. Characterization on the base

Scratch probes ran on the exact base through both the shared clipboard and the
real Desktop key dispatcher (`Ctrl+C`/`Ctrl+V` `KeyEvent`s through
`GlobalKeyDispatcherD`).

| Case | Base result |
|---|---|
| S1 axis witness `A=(0,0)`, `c=Circle(A,1)`, `C=Point(c)`, `f=Line(C,xAxis)`, `D=Intersect(f,yAxis)`, `a=LocusV2(D,C)`; copy `[a]` | `f` renamed but not serialized; the paste throws `IllegalStateException: Clipboard identity rollback could not restore the construction`, caused by `Spatial rollback restore is not active`, with the original failure absent |
| S1 state after the throw | construction, IDs, records, DAG and spatial section exact (inner `MyXMLio` restore); leaked: `blockUpdateScripts=true`, `<scripting blocked="true">` (saved and kept on reopen), kernel `loadingMode=true`, `notifyViewsActive=false`, command lookup `XML`; undo history unchanged |
| S2 free-line control (`xl:y=0`, `yl:x=0`) | complete copy, pastes correctly |
| S3 R3 witness core, copy `[E]` | same failure as S1 (`D` and `f` not serialized) |
| S4 identity-bearing copy of `a` whose closure depends on the axis-derived helper `M=Midpoint(D,P)` | paste reports success; `M_{1}` is **free** geometry: silent degradation |
| S5 the same shape in a document without identity | upstream copy degrades `M_{1}` to free geometry |
| S6 S1 with scripts already blocked | policy stays `true`; kernel flags still leak |
| D1 Desktop `Ctrl+C`/`Ctrl+V` of S1 | same throw and leaks; the original failure is `MALFORMED_RECORD: Construction identity base contract disagrees with the attached geo` |
| D3/D4/D5 Desktop Insert File with overwrite | succeeds for complete sources; it passes its caller snapshot and overwrite mutation through the same `evalClipboardXMLAtomically` |

A closure trace of S1 identified the omission: after every closure step,
`f`'s parent `AlgoLinePointLine[C (copied), xAxis (constant)]` was missing. The
trace is in the [design record](../architecture/pre_g9b_r3_c1_clipboard_atomicity_and_closure_design.md)
§2.

## 4. Root cause and repair

**C1-A, rollback protocol** (owner: shared `InternalClipboard` import seam; Desktop
`CopyPasteD` for its own script-policy exit):
- The outer restore entered `ROLLBACK_RESTORE` without `beginRollbackRestore`.
  - `clearForRollbackRestore` threw inside `doParseXML` before its `try`, which
    leaked the kernel flags.
  - The secondary exception replaced the original failure.
  - Neither paste entry point restored the script policy on an exception.
- Repair:
  - `restoreClipboardSnapshot` follows the approved protocol (`beginRollbackRestore`,
    `ROLLBACK_RESTORE`, parse, `finishRollbackRestore` in `finally`).
  - A restore failure keeps the import failure as a suppressed exception.
  - Both entry points restore the previous script policy on every non-success exit.
- This is minimal: one private method and two call-site guards. No runtime check
  changes, and the successful path keeps the upstream timing.

**C1-B, predecessor closure** (owner: the GeoCeDG identity-bearing clipboard
closure in `InternalClipboard`, called by both copy pipelines):
- Upstream `addAlgosDependentFromInside` adds an algorithm only when every input
  is copied. Constants never are.
- The GeoCeDG predecessor pass ran only for geos new in its own pass.
- Repair:
  - `addSpatialClosureParentAlgorithms` adds the missing parent algorithm of every
    copied labeled geo, and hides its missing outputs, but only for identity-bearing
    payloads.
  - `evalClipboardXMLAtomically` rejects an identity-bearing import whose parse
    reported host rebuild errors as `INCOMPLETE_CLOSURE`. This is the fail-closed
    rule, and it follows the upstream `Construction` redefine `hasErrors()`
    precedent.
- This is minimal:
  - generic GeoGebra copy for documents without identity is untouched;
  - `Macro.buildMacroXML`, which shares `addSpatialIdentityClosure`, is untouched;
  - axes gain no identity.

The design record §3 lists the rejected alternatives.

## 5. Productive changes

| File | Upstream-owned | Change |
|---|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/util/InternalClipboard.java` | yes (already modified by G9A1) | parent-algorithm completion for identity-bearing payloads; protocol-correct snapshot restore with the kept import failure; `INCOMPLETE_CLOSURE` rejection of host rebuild errors; script-policy restore on failed import exits |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java` | yes (already modified by G9A1) | the same completion call in the Desktop copy pipeline; the previous script policy restored on every failed import exit, fast and ordinary paste |

No other product, build, packaging, verifier, schema or serialization source
changes.

## 6. Tests

| Class | Methods | Candidate tree | Extracted base tree |
|---|---|---|---|
| `org.geocedg.common.locus.PreG9bR3C1ClipboardCorrectnessTest` | 9 | 9 pass | 8 fail; only the control B passes, as it must |
| `org.geocedg.desktop.PreG9bR3C1DesktopClipboardTest` | 2 | 2 pass | 2 fail |

- **Rejection tests** compare the full application XML, the spatial section, the
  label-to-ID map, undo history size and flags, script policy, kernel loading
  mode, view notification, command lookup and file loading, before and after.
- **Desktop undo assertions** use a synchronous `UndoManagerD` baseline and store
  listeners, because Desktop stores undo points asynchronously.
- **Insert File** uses the real `CopyPasteD.insertFrom` with overwrite. An
  injected missing duplicate makes the overwrite mutation fail after it has removed
  `A`.

## 7. Compatibility

```text
IDENTITY_IMPACT                = none; fresh IDs, remap and copy lineage now complete for the constant-input closure
SERIALIZATION_FORMAT_IMPACT    = NONE (spatial XML schema, durable ID format, schemaVersion 1/2, migration unchanged)
V1/V2 RECORDS                  = unchanged; historical documents open unchanged
NATIVE SAVE/REOPEN             = pasted identities, lineage and dependencies survive; a failed paste leaves the document to be saved unchanged
COPY LINEAGE/REMAP             = unchanged semantics; the closure is now complete
DESKTOP/SHARED COHERENCE       = one shared import seam and one shared closure rule; Desktop keeps only its own script-policy exit
DOCUMENTS WITHOUT IDENTITY     = unchanged (completion and rejection apply only to identity-bearing payloads)
```

## 8. Verification

### Local evidence before commit

| Check | Command or method | Result |
|---|---|---|
| focal tests | `gradlew :shared:common-jre:test --tests …PreG9bR3C1ClipboardCorrectnessTest`, `gradlew :desktop:desktop:test --tests …PreG9bR3C1DesktopClipboardTest` | 9/9 and 2/2 pass |
| focal tests on the base | same classes copied into `git archive 89d481b1` | 8/9 and 2/2 fail (control passes) |
| affected shared regressions | every `common-jre` test class using the clipboard, `CopyPaste` or macros (G9A1, G9A2, G9U0, G9S1, G9S1-R1, POST-G9U1 and PRE-G9B-R2/R3 suites, `InternalClipboardTest`, `CopyPasteTest`, `GroupTest`) | 30 classes, 379 tests, 0 failures |
| affected Desktop regressions | `CopyPasteDTest`, `G9U0R3InspectorWorkflowTest`, `G9U1IntersectionSessionTest`, `G9U1MacroNativeArchivePersistenceTest`, `G9U1SemanticPointInteractionTest`, `G9U1UserToolLibraryTest`, `PreG9bR2E1SemanticPairNativeArchiveTest` | 7 classes, 111 tests, 0 failures |
| full shared suite | official `gradle-test-evidence-producer.ps1`, selection `final.shared` | 679 JUnit files, 6 796 tests, 0 failed or errored, the 10 allowlisted skips |
| full Desktop suite | official producer, selection `final.desktop` | 101 JUnit files, 1 525 tests, 0 failed or errored, 1 allowlisted skip |
| discovery | official producer, `discovery.shared` and `discovery.desktop`, `--test-dry-run` | 5 879 and 1 531 identities |
| checkstyle | `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings |
| verification-core tests that read the live registry or inventory | `pwsh -NoProfile -File tools/agent/tests/<name>.Tests.ps1` | all exit 0: final-coverage 8 cases / 308 assertions, registry 22 / 62, contract-boundary 5 / 38, cli-compatibility 7 / 45, gradle-producer 7 / 21, supervisor 12 / 51, runtime 119 / 119 fake-first tests |
| upstream boundary | `Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b7df401ff056c37b502d82df4d72b1522` | passes with 803 registered files |
| documentation structure | every relative Markdown link of the new and changed documents | 0 unresolved |
| whitespace | `git diff --check` | clean |

`-PlanOnly` after the inventory update:

| Command | Exit | Coverage | Missing | Nodes | Plan hash |
|---|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile PHASE -Phase POST-G9U1-A3 -PlanOnly` | 0 | `COMPLETE` | 0 | 4 | `c2e25e0b5d28523023f88dc139f015cda004ce28cc94d1ff546a65c4c222876f` |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |
| `tools/agent/verify.ps1 -Profile FINAL -PlanOnly` | 0 | `COMPLETE` | 0 | 67 | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |

The plans equal those of the base. The FINAL `final.shared` and `final.desktop`
selections include the new test classes through the regenerated inventory.

### Coupled reconciliations

**JUnit inventory.** Regenerated with the official
[`update-verification-junit-inventory.ps1`](../../tools/agent/update-verification-junit-inventory.ps1)
from the two dry-run discoveries and the two passing full runs above:

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 870 | 5 879 |
| `final.shared` | 6 787 | 6 796 |
| `discovery.desktop` / module `desktop` | 1 529 | 1 531 |
| `final.desktop` | 1 523 | 1 525 |

No narrow selection changed; `post-g9u1-a3.narrow` keeps 114 identities and
`9a45556c…`.

**Typed registry.** The `junit_inventory` catalog pin moves from `9a7300fd…` to
`9fea75de591dde7566f4af4661e0ec49f2253acd80f4bab213bd1233d9a005b2`. It is the
canonical-text SHA-256 (`Get-VerificationCanonicalTextSha256`), and the method
first reproduced `9a7300fd…` exactly from the unchanged inventory. One line
changes and line endings are unchanged.

**Upstream modification record.** `docs/upstream/modified-files.yml` appends the
`PRE-G9B-R3-C1` purpose to the `InternalClipboard.java` and `CopyPasteD.java`
entries and registers both new test files.

### Acceptance

The frozen campaign is one `FINAL` on the committed candidate with a clean
worktree:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-c1-final
```

Its console log goes outside `artifacts/`. Its receipt binds that commit and is
reported separately, because it cannot be named here.

## 9. Ri quality-debt ledger

Checked at phase entry (Ri policy rule 1). The `C1` footprint overlaps only its
own two debts. By author direction the evaluator debts belong to the `R3-X1`
inspection, and the documentary and governance items are not repaired here.

| Identifier | State after `PRE-G9B-R3-C1` |
|---|---|
| `TD-CLIPBOARD-ATOMIC-ROLLBACK-PROTOCOL` | **resolved by this candidate** (pending author approval) |
| `TD-CLIPBOARD-CONSTANT-INPUT-PREDECESSOR-CLOSURE` | **resolved by this candidate** (pending author approval), including the proven same-cause silent degradation of identity-bearing payloads |
| `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION` | **new, retained**: in a document without durable identity, upstream copy still degrades a geo whose predecessor command has a constant input to free geometry. Changing generic GeoGebra copy semantics needs an author disposition. Owner and disposition due by the `PRE-G9B-R7` closeout |
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | retained; owner `PRE-G9B-R3-X1` |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE` | retained. At the start of `R3-X1` it is inspected for synergistic repair under the Ri conditions; otherwise `PRE-G9B-R3-X2 — evaluator correctness` is registered after `X1` and before `R4` |
| `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | retained; same `R3-X1` inspection and conditional `R3-X2` rule |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained; owner and disposition due by the `PRE-G9B-R7` closeout |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN` | carried forward unchanged |
| `OBS-R2-PHASE-SELECTION-COHORT` | carried forward unchanged (no `PHASE` campaign in `C1`) |
| `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |

Recorded observations, not debts:

- `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`:
  - `CommandNotLoadedError` (web lazy command loading) is not a `MyError` and does
    not enter the outer restore;
  - the approved inner restore handles identity-bearing parses;
  - web paste has no caller mutation, and Desktop loads every command.
- `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY`: `MyXMLio.doParseXML` clears the
  construction before its `try`. The `C1` protocol repair removes the
  characterized trigger, and the upstream structure is unchanged.

## 10. Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (identity-bearing clipboard copy/paste and Insert File atomicity)
SERIALIZATION / DOCUMENT FORMAT    = UNCHANGED
FRONTEND                           = CHANGED ONLY IN THE DESKTOP ADAPTER'S SCRIPT-POLICY EXIT; no UI change
BOOTSTRAP_IMPACT                   = NO_CHANGE_REQUIRED (no workstation prerequisite, toolchain, Gradle, Conda, packaging or environment contract changes)
VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY (JUnit inventory regenerated, registry catalog pin); no verifier code or methodology change
UPSTREAM_BOUNDARY_IMPACT           = two already-registered upstream files modified; two GeoCeDG test files added; record updated
GUIDE_IMPACT                       = REVIEWED — NO CHANGE REQUIRED (neither user-guide edition nor the developer guide describes clipboard or Insert File behaviour)
```

## 11. Authorization state

The author instruction authorizes this phase's implementation and one `FINAL`
campaign.

It does not authorize:
- `PRE-G9B-R3-X1` or `PRE-G9B-R3-X2`;
- expression-point certification or any evaluator repair;
- the Construction Protocol reorder;
- `R4` or any later phase, `G9B`, `G9C` or `G9U2`;
- a change of generic GeoGebra copy semantics;
- release or publication.

Push, merge, promotion, tag and release each require a separate explicit author
instruction naming the exact candidate SHA. The phase stops for author review.

# PRE-G9B-R3-C1-U1 ordinary clipboard closure candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R3-C1-U1, ordinary clipboard constructive-closure correctness
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT (frozenAtPhaseStart = true)
PLANNED_ACCEPTANCE_LEVEL  = FINAL, one campaign on the exact candidate
IMPLEMENTATION_BASE       = 68169dc0f026eb43b3bdae41fb69c3796cc41ad0 (tree 46988e065de7d62aa37d1e29a9f1940dcf5c8f6f)
BRANCH                    = feature/pre-g9b-r3-c1-u1-ordinary-clipboard-closure
CANDIDATE_COMMIT          = the commit that contains this artifact; not nameable inside it, reported separately
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a separate closeout/author-decision record is the sole
authority for current author approval status.

The phase executes the author's explicit `PRE-G9B-R3-C1-U1` instruction of
2026-09-29. The [design record](../architecture/pre_g9b_r3_c1_u1_ordinary_clipboard_closure_design.md)
gives the mechanism, the [validation matrix](pre_g9b_r3_c1_u1_validation_matrix.md)
maps every obligation to evidence, and the machine-readable evidence is
[`geocedg/validation/pre-g9b-r3-c1-u1/pre-g9b-r3-c1-u1-evidence.json`](../../geocedg/validation/pre-g9b-r3-c1-u1/pre-g9b-r3-c1-u1-evidence.json).

## 1. Entry and declarations

- **Entry identity.** `HEAD`, local `main`, `origin/main` and live remote `main` all
  resolved to `68169dc0…`, the published `PRE-G9B-R3-C1` closeout
  (`PRE-G9B-R3-C1 = PASS — AUTHOR APPROVED`). The worktree and index were clean.
- **Branch.** `feature/pre-g9b-r3-c1-u1-ordinary-clipboard-closure`, created from
  `68169dc0…`; not pushed.
- **Class.** `GLOBAL_IMPACT` was frozen before any productive edit, with `FINAL` as
  the planned acceptance, because the change affects the generic shared clipboard
  used by CeDG and non-CeDG documents, Desktop and shared/Web consumers, and the
  machinery shared with duplicate, macros and groups.
- **Canonical prompt.** None is created, as for `PRE-G9B-R3-C1`: governance does not
  require one and the author instruction is the execution authority.
- **Readiness.** The current `READINESS` action is identity-only and needs an
  existing receipt; no pre-campaign readiness gate exists to run.

## 2. Reproduction on the exact base

Scratch probes ran on the exact base in an ordinary, non-participating construction
through the shared clipboard (`InternalClipboard.getTextToSave` and
`pasteGeoGebraXMLInternal`), on a default upstream-style application and on a
GeoCeDG-configured application (`.cedg` documents; no geo is an identity participant).

| Case | Copied geo before | Base result |
|---|---|---|
| P1: `A`, `c=Circle(A,1)`, `C=Point(c)`, `f=Line(C,xAxis)`, `D=Intersect(f,yAxis)`, `M=Midpoint(D,C)`; copy `[M]` | `M` dependent (`AlgoMidpoint`) | `f`, `D` renamed but not serialized; pasted `M` is **free** |
| P3 minimal: `P`, `f=Line(P,xAxis)`, `D`, `M=Midpoint(D,P)`; copy `[M]` | dependent | same; pasted `M` free |
| P5 `X=Intersect(c,xAxis)` with two outputs, `M=Midpoint(X_1,X_2)`; copy `[M]` | dependent | `X_1`, `X_2` not serialized; pasted `M` free |
| P4 copy `[f]` (the selected geo itself has the axis input) | dependent | correct: `addPredecessorGeos` adds a selected geo's algorithm directly |
| P2 control on free lines (`xl:y=0`, `yl:x=0`) | dependent | correct: every predecessor is complete |

Both applications give identical results, and no geo carries a durable identity
(`registry.isEmpty()`, no `geocedgId`, no spatial section). The document being
`.cedg` does not change the route: the generic host clipboard is used. The Desktop
Ctrl+C / Ctrl+V route shares the same collection functions.

## 3. Upstream comparison

```text
UPSTREAM_REPRODUCES = true
```

The pinned baseline `9b93256b7df401ff056c37b502d82df4d72b1522` was extracted with
`git archive` and the plain probe was run there: results are identical (P1, P3 and P5
paste free geometry; P4 and P2 correct). `CopyPaste.addPredecessorGeos` and
`InternalClipboard.addAlgosDependentFromInside` are **byte-identical** to the
baseline versions. GeoCeDG did not introduce the defect.

- **Cause.** `addPredecessorGeos` adds every non-constant predecessor and the
  algorithm of only the selected geos, deliberately excluding constants ("we suppose
  they are always on"). All other algorithms come from `addAlgosDependentFromInside`,
  which adds an algorithm only when `consElements.containsAll(inputs)`, and a
  constant such as `xAxis` is never in the list. The algorithm of an unselected
  predecessor with a constant input is therefore never added.
- **Consequence.** The predecessor is renamed but not serialized; the command that
  uses it fails to resolve its input; the host XML handler only records an error;
  the following `<element>` of the same label creates the geo as free geometry and
  the paste reports success.
- **Classification.** Inherited upstream behaviour. The repair is kept minimal and
  leaves both upstream functions unmodified.

## 4. Repair

`InternalClipboard.addParentAlgorithmsOfCopiedGeos(consElements, copiedMacros)`
completes a payload with the parent algorithm of every copied labeled geo, when each
non-constant input of that algorithm is already copied. It adds only parents of
copied geos and never dependents of the copied set, so it cannot over-expand: a copy
of a single point still pulls in none of its hidden axis-based dependents.
Constants are referenced by name and never copied or given an identity.

It runs after the existing collection steps in both copy pipelines
(`InternalClipboard.copyToXMLInternal` and `CopyPasteD.copyToXML`) and replaces the
C1 call that only ran for identity-bearing payloads. C1 payloads receive the same
completion as before; the condition simply no longer restricts it. The helper name
changes from the C1 `addSpatialClosureParentAlgorithms`, which had no other caller.

Alternatives rejected:

| Alternative | Reason |
|---|---|
| treat constants as satisfied inside upstream `addAlgosDependentFromInside` | would also copy every hidden axis-based dependent of a copied point, and it edits an upstream function shared with macros |
| copy the constants | forbidden; constants must not become copied objects or gain identity |
| promote the copied objects to CeDG participants | forbidden; separate layers |
| leave the free-geometry fallback | violates the central invariant |
| reject the paste on a missing input | would add a user-visible failure mode; the guard makes the case unreachable through product paths (design record §4) |

## 5. Productive changes

| File | Upstream-owned | Change |
|---|---|---|
| `source/shared/common/src/main/java/org/geogebra/common/util/InternalClipboard.java` | yes (already modified by G9A1 and C1) | the completion helper is renamed and generalized with the guard and the macro set, and it is called for every payload |
| `source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java` | yes (already modified by G9A1 and C1) | the Desktop pipeline calls the helper for every payload, ordinary and same-window |

No other product, build, packaging, verifier, schema or serialization source
changes.

## 6. Behaviour before and after

| | Before | After |
|---|---|---|
| copy `[M]` with an axis-input predecessor | `f`, `D` not serialized; pasted `M` free | the payload holds `Line(P,xAxis)` and `Intersect(f,yAxis)`; pasted `M`, `D`, `f` dependent, the axes referenced, not copied |
| two-output algorithm with an axis input | outputs not serialized; pasted `M` free | the algorithm and both outputs copied; pasted `M` dependent |
| copy `[P]` in the same construction | exactly `P` | exactly `P` (no over-expansion) |
| control on free lines | complete | identical |
| identity-bearing copy (C1) | complete closure | identical |

## 7. Identity invariants

For ordinary geometry: no durable ID is created, no spatial record or section is
synthesized, and no association is inferred from a label, coordinate, proximity or
construction order (asserted by `noGeoInvolvedIsAnIdentityParticipant`, the reopen
test and the Desktop tests). For identity-bearing payloads, C1 behaviour is unchanged
and re-verified: complete closure, fresh IDs, lineage, `INCOMPLETE_CLOSURE`, rollback
atomicity.

## 8. Tests

| Class | Tests | Candidate | Extracted base `68169dc0…` |
|---|---|---|---|
| `org.geocedg.common.spatial.PreG9bR3C1U1PlainClipboardClosureTest` (default app) | 9 | 9 pass | 8 run: 6 fail, 2 pass |
| `org.geocedg.common.spatial.PreG9bR3C1U1CedgClipboardClosureTest` (GeoCeDG app) | 9 | 9 pass | 8 run: 6 fail, 2 pass |
| `org.geocedg.desktop.PreG9bR3C1U1DesktopClipboardTest` | 2 | 2 pass | 2 run: 2 fail |

On the extracted base, the one test that calls the new helper cannot compile and is
omitted, so 8 tests of each shared class run there: **6 fail and 2 pass in each
configuration** (the two controls), and both Desktop tests fail. So the bug tests fail on the base and pass on
the candidate, and the controls prove that the unaffected behaviour is unchanged.

## 9. Compatibility

```text
SERIALIZATION_FORMAT_IMPACT = NONE
IDENTITY_SCHEMA_IMPACT      = NONE
MIGRATION_IMPACT            = NONE
SHARED / DESKTOP            = one shared collection rule; Desktop ordinary and same-window payloads both covered by real key events
WEB                         = same shared path (CopyPasteW -> InternalClipboard.getTextToSave); the GWT module is not built by the unit-test suite, so Web is covered by the shared tests only
MACROS                      = Macro.buildMacroXML does not call the helper; unchanged
GROUPS / DUPLICATE          = unchanged; duplicate uses the same copyToXMLInternal
HISTORICAL DOCUMENTS        = open unchanged
```

## 10. Verification

### Local evidence before commit

| Check | Method | Result |
|---|---|---|
| focal tests | the classes of section 8 | 9 + 9 + 2 pass |
| base causality | the same tests on `git archive 68169dc0…` | section 8 |
| upstream comparison | plain probe on `git archive 9b93256b…` | identical defect |
| affected regressions | 31 `common-jre` classes and 8 Desktop classes that use the clipboard, `CopyPaste`, macros or Insert File, including the C1 focal tests | 388 tests and 113 tests, 0 failures |
| full shared suite | official `gradle-test-evidence-producer.ps1`, selection `final.shared` | 681 JUnit files, 6 814 tests, 0 failed or errored, the 10 allowlisted skips |
| full Desktop suite | official producer, `final.desktop` | 102 JUnit files, 1 527 tests, 0 failed or errored, 1 allowlisted skip |
| discovery | official producer, `discovery.shared`, `discovery.desktop` | 5 897 and 1 533 identities |
| checkstyle | `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 findings |
| verification-core tests that read the registry or inventory | final-coverage, registry, contract-boundary, cli-compatibility, gradle-producer, supervisor, runtime | all exit 0 |
| upstream boundary | `Assert-GeoCeDGUpstreamBoundary`, baseline `9b93256b…` | passes with 807 registered files |

`-PlanOnly` after the inventory update:

| Command | Exit | Coverage | Missing | Nodes | Plan hash |
|---|---|---|---|---|---|
| `tools/agent/verify.ps1 -Profile PHASE -Phase POST-G9U1-A3 -PlanOnly` | 0 | `COMPLETE` | 0 | 4 | `c2e25e0b5d28523023f88dc139f015cda004ce28cc94d1ff546a65c4c222876f` |
| `tools/agent/verify.ps1 -Profile INTEGRATION -PlanOnly` | 0 | `COMPLETE` | 0 | 46 | `4502c4bd1bbe1a123089e9e524fd0afb354932afbcb410f93052555a16c77aea` |
| `tools/agent/verify.ps1 -Profile FINAL -PlanOnly` | 0 | `COMPLETE` | 0 | 67 | `defe8ab4692af05430464974c3d5e8e029ff7d64ecbc1822c07ff91593c744e0` |

The plans equal those of the base.

### Coupled reconciliations

**JUnit inventory**, regenerated with the official updater:

| Selection | Before | After |
|---|---|---|
| `discovery.shared` / module `shared` | 5 879 | 5 897 |
| `final.shared` | 6 796 | 6 814 |
| `discovery.desktop` / module `desktop` | 1 531 | 1 533 |
| `final.desktop` | 1 525 | 1 527 |

The narrow selection `post-g9u1-a3.narrow` keeps 114 identities and `9a45556c…`.

**Typed registry.** The `junit_inventory` pin moves from `9fea75de…` to
`acca0eef7a02c8a75eee569f2ebd5222ac58d46dac51c83d1061db04cb48a4b8`, by canonical-text
SHA-256; the method first reproduced `9fea75de…` from the unchanged inventory. One
line changes and line endings are unchanged.

**Upstream modification record.** The purposes of `InternalClipboard.java` and
`CopyPasteD.java` are extended and the four new test files are registered; the
boundary check passes with 807 files.

### Acceptance

The frozen campaign is one `FINAL` on the committed candidate with a clean worktree:

```powershell
pwsh -NoProfile -File tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\pre-g9b-r3-c1-u1-final
```

Its console log goes outside `artifacts/`. Its receipt binds that commit and is
reported separately, because it cannot be named here.

## 11. Ri quality-debt ledger

| Identifier | State after `PRE-G9B-R3-C1-U1` |
|---|---|
| `TD-CLIPBOARD-UPSTREAM-CONSTANT-INPUT-DEGRADATION` | **resolved by this candidate** (pending author approval); every case is covered by a test |
| `TD-LOCUS-CERTIFIED-CONSTRUCTION-EXPRESSION-POINT` | retained; owner `PRE-G9B-R3-X1` |
| `TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE`, `TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY` | retained unchanged; inspected at `R3-X1` entry, otherwise conditional `PRE-G9B-R3-X2` |
| `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` | retained |
| `BASELINE-G9U1-BRANDING-EVIDENCE-PIN`, `OBS-R2-PHASE-SELECTION-COHORT`, `GUIDE-A1-SECTION-9.3-STALE` | carried forward unchanged |
| `OBS-C1-COMMAND-NOT-LOADED-OUTER-RESTORE`, `OBS-C1-DOPARSEXML-CLEAR-BEFORE-TRY` | observations retained |

New observation, not a debt: `OBS-U1-PLAIN-XML-PASTE-TOLERATES-COMMAND-ERRORS`. A
non-identity payload whose command fails to rebuild still only reports through the host
XML handler and the geo is created free. The repair removes the known cause; a
generic failure policy for other, hypothetical causes would be a new externally visible
behaviour and is not decided here.

## 12. Impact

```text
PRODUCT_PHASE_EFFECT               = BOUNDED (constructive closure of the generic clipboard)
SERIALIZATION / DOCUMENT FORMAT    = UNCHANGED
BOOTSTRAP_IMPACT                   = NO_CHANGE_REQUIRED (no workstation prerequisite, toolchain, Gradle, Conda, packaging or environment contract changes)
VERIFICATION_INFRASTRUCTURE_IMPACT = CATALOG DATA ONLY (JUnit inventory regenerated, registry catalog pin); no verifier code or methodology change
UPSTREAM_BOUNDARY_IMPACT           = two already-registered upstream files modified; four GeoCeDG test files added; record updated
GUIDE_IMPACT                       = REVIEWED — NO CHANGE REQUIRED (neither user-guide edition nor the developer guide describes clipboard closure)
```

## 13. Authorization state

The author instruction authorizes this phase's implementation and one `FINAL`
campaign. It does not authorize `PRE-G9B-R3-X1`, `PRE-G9B-R3-X2`, expression-point
certification, any evaluator repair, `R4` or any later phase, a change of
serialization or identity, or release and publication. Push, merge, promotion, tag and
release each require a separate explicit author instruction naming the exact
candidate SHA. The phase stops for author review.

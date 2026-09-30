# PRE-G9B-R4 — SplineV2 and ordered-list authoring: candidate report

```text
TECHNICAL_CANDIDATE_STATE = PREPARED FOR ONE CLEAN IMMUTABLE COMMIT
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R4
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = BOUNDED_PHASE
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R4
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval, and this file never needs to change when that
decision is made. The candidate commit cannot name itself, so its exact identity
and the post-commit `PHASE` acceptance evidence are reported outside it.

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R4` for implementation and technical verification
on 2026-09-30 from an exact published base, resolving the two open execution
conditions of the canonical prompt
`.github/prompts/tasks/pre-g9b-r4-spline-v2-list-authoring-ux.prompt.md`
(blob `51abda02734bb7884c8b811b26ba10a14a56119f`, read unchanged; its historical
`PROPOSED` header is not edited).

| Identity | Value |
|---|---|
| implementation base | `62b1281b5006272d4f9b1a0ae9d413e1f7b94320` |
| base tree | `8ce2f6719f1094e300beeaafbadab4cd5135a8d5` |
| local `main` = `origin/main` = live remote `main` at entry | `62b1281b5006272d4f9b1a0ae9d413e1f7b94320` |
| worktree at entry | clean |
| local branch (not pushed; not identity) | `feature/pre-g9b-r4-spline-v2-list-authoring-ux` |

Entry states re-read from the roadmap: `PRE-G9B-R3-X1 = PASS — AUTHOR APPROVED`,
`PRE-G9B-R3-X2 = NOT NEEDED`, the input, historical-suite and generated-state
maintenance tracks `PASS — AUTHOR APPROVED`, and
`OBS-ORPHANED-HISTORICAL-VERIFICATION-SUITES = RESOLVED`.

The verification class was frozen before coding. Section 12.8 of
`geocedg/specs/operations/verification-levels.md` still maps `BOUNDED_PHASE` to
PHASE alone; `PRE-G9B-R1-R1` is the precedent for a bounded phase accepted by a
plain PHASE run without a closeout policy. No escalation was requested or run.

## 2. R0 assumptions reconfirmed against source

| R0 observation | Current source | Consequence |
|---|---|---|
| the owned SplineV2 icon exists and is bound to the action key | `mode_geocedg_splinev2.svg`, bound by `GeoCeDGToolImageResource.forIconKey("geocedg.action.SplineV2Create")` | no icon added |
| the action is already rendered in the semantic-curves flyout | `construction-semantic-curves` is `profile-flyout` and lists `semantic.spline-v2.create` | no toolbar entry added; the action changes kind only |
| mode semantics and mode-keyed help are missing | the action was the profile's only `command` kind; contextual help read the previous mode | fixed by a real mode |
| Input Help opens with nothing selected | `focusSplineInput` called `setShowInputHelpPanel(true)` without selection; the syntax keys exist in all three command bundles | fixed through the existing `InputBarHelpPanelD.focusCommand` |

Three planning-era statements proved inaccurate and are corrected here, not
silently absorbed:

1. **The command already produces a visible, labelled argument list.** The R0
   design and the prompt say that the bare-point form's wrapped list stays
   unlabelled and suppressed. In current source the durable-identity
   participation of `LocusV2PublicOperations` promotes every unlabelled direct
   command argument to a labelled construction object, for typed and tool input
   alike: `SplineV2(A,B,C,D)` creates `l1 = {A,B,C,D}` (not auxiliary, visible)
   and the degree `a = 3`. Suppressing it would change durable-identity semantics,
   which R4 forbids. The tools therefore issue the existing bare-point command and
   create **no list of their own**; the construction they produce is
   element-for-element that of the typed command (focal test
   `theToolBuildsExactlyTheConstructionOfTheTypedBarePointCommand`). Recorded as
   `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION`.
2. **Oversized default-degree input yields an undefined spline, not an error.**
   The explicit-degree forms reject a degree outside `3…point count` or beyond the
   work policy before creation. The default-degree forms skip that check; the
   polynomial model guard then leaves the created spline undefined, with no
   command error or localized message. The help text and the user guide state
   this difference.
3. **The live catalog had 112 actions**, not the historical 110 of the G9U1
   planning catalog; the approved A7 amendment added two. R4 adds three.

## 3. Delivered scope

The interaction contract is the new candidate specification
[`geocedg/specs/ui/spline-v2-and-ordered-list-authoring.md`](../../geocedg/specs/ui/spline-v2-and-ordered-list-authoring.md).
It defines no spline mathematics and defers to
[Semantic Spline V2](../../geocedg/specs/curves/semantic-spline-2d.md).

### 3.1 SplineV2 tools

| Action id | Mode | Id | Command issued |
|---|---|---|---|
| `semantic.spline-v2.create` (kind `command` → `upstream-mode`) | `MODE_SPLINE_V2` | 137 | `SplineV2(P1,…,Pn)` |
| `semantic.spline-v2.create-degree` | `MODE_SPLINE_V2_DEGREE` | 138 | `SplineV2({P1,…,Pn}, d)` |
| `semantic.spline-v2.create-closed` | `MODE_SPLINE_V2_CLOSED` | 139 | `SplineV2(P1,…,Pn,P1)` |

The ids are the next free ones after the GeoCeDG block `133–136`; the audit found
no other use of `137–140`.

- Collection reuses the host ordered selected-point list and the host polyline
  preview as a presentation-only control polygon. Finishing is the host Polyline
  gesture: select the first collected point again, with at least three points.
  Any other re-selection is the host deselection toggle.
- `GeoCeDGSplineV2Authoring` issues exactly one ordinary `SplineV2` command,
  passing the point objects by reference (never labels) and adding no degree,
  span, knot, validity or closedness rule. The default path shows no prompt. The
  explicit-degree tool opens the host number dialog prefilled with `3` after the
  finish; that entry is the second, explicit gesture, and the command validates
  it. Cancelling creates nothing.
- The construction returns through the normal end-of-mode path: one commit, one
  undo point. Escape, a tool change and a workspace switch clear the collection
  and create nothing.

### 3.2 Explicit closed form (`AD-R0-8`)

The host's established pattern for an explicit construction variant is one tool
per variant, as with Polyline and Polygon. **Closed Spline V2** is the only
closed-form authoring action; on finish it appends the **same point object** as
the first collected point, the synthesis `AD-R0-8` permits. The open tools never
append it. The finish re-click is identical in all three tools and never means
closure; the Desktop never compares coordinates.

The kernel decides periodicity by its unchanged normative rule, equal first and
last input values (`SplinePolynomialModel2D`). So if a user explicitly selects
two *distinct* points with exactly equal values as the first and last members of
an open spline, that input is periodic, exactly as the same typed command is. The
Desktop neither inspects nor alters this. Recorded as
`OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` for author disposition; any
change is a kernel/specification decision outside R4.

Two alternatives were rejected. A modifier-key finish has no established host
precedent. A closed/open question at finish would prompt on the default path.
The host number dialog's checkbox is unsuitable because it only changes the sign
of the entered number.

### 3.3 Create List from Selection

`construction.list-from-selection` (`MODE_ORDERED_LIST`, 140) sits next to
Polyline in the Polygons group. It collects any objects in exact click order and
finishes when the first object is selected again, with at least two collected.
It builds one ordinary dependent host `GeoList` through the existing
dependent-list algorithm, labelled normally. Re-selection of any other member is
the host deselection, so no member is ever repeated implicitly; the host has no
graphical duplicate gesture, and Algebra Input remains the route for a repeated
member. Mixed types, cancel, one undo point, downstream DAG use, and member
redefinition and deletion all follow host behaviour; the tests prove parity with
a typed list. No CeDG-specific list type exists.

### 3.4 Help, Input Help, localization and tooltip

- Mode text `SplineV2.Tool`, `SplineV2.Degree.Tool`, `SplineV2.Closed.Tool` and
  `OrderedList.Tool`, the matching `.Help` keys and `SplineV2.DegreePrompt` are
  added to the default, `_en` and `_es` menu bundles. They are byte-preserving
  insertions of nine LF lines per bundle; no other byte of these mixed-EOL files
  changed. Contextual help
  therefore describes the active tool.
- Invoking a SplineV2 tool action, or contextual help while one is active, shows
  Algebra Input and opens Input Help with `SplineV2` selected, rendering the
  existing localized syntax. Nothing is typed into or focused in the input field;
  the old `SplineV2(` prefill is removed. One upstream line in
  `InputBarHelpPanelD.focusCommand` scrolls the selected command into view.
- `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` is corrected. The Algebra Input tooltip
  now lists only accepted forms: `SplineV2(A,B,C)`, `SplineV2({A,B,C,D})`,
  `SplineV2({A,B,C,D},4)` and `SplineV2(A,B,C,D,A)`. A focal test evaluates each
  example in English and Spanish. The stale degree-2 example and the nonexistent
  "parameter form" are gone.
- Command names, syntax keys and command tables are unchanged, and `R5-B` is not
  touched. `SplineV2` stays in the function command table; the optional move to
  the geometry table was not taken.

### 3.5 Compatibility

No kernel, command, `CmdSplineV2`, serialization, identity or Classic `Spline`
change. A tool spline is the same construction as the typed command and reopens
unchanged from the native document (focal test
`aToolSplineReopensUnchangedFromTheNativeDocument`). The Classic diagnostic
session exposes none of the new actions.

## 4. Changed paths

| Layer | Paths |
|---|---|
| UI specification (new candidate) | `geocedg/specs/ui/spline-v2-and-ordered-list-authoring.md` |
| shared mode constants (upstream, registered) | `source/shared/common/src/main/java/org/geogebra/common/euclidian/EuclidianConstants.java` |
| localization (upstream bundles, registered) | `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties`, `menu_en.properties`, `menu_es.properties` |
| Desktop GeoCeDG | `GeoCeDGEuclidianController.java`, `GeoCeDGSplineV2Authoring.java` (new), `GeoCeDGActionRegistry.java`, `GeoCeDGProfile.java`, `resources/GeoCeDGToolImageResource.java` |
| Desktop upstream (registered) | `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/inputbar/InputBarHelpPanelD.java` |
| product profile | `apps/geocedg/application-profile.yml` |
| focal tests (new) | `PreG9BR4SplineV2AuthoringTest.java`, `PreG9BR4OrderedListToolTest.java` |
| pinned catalog counts in existing tests | `GeoCeDGProfileTest.java`, `G9U1ProfileCompilerTest.java`, `G9U1ActionRegistryTest.java`, `G9U1WorkspaceSurfaceTest.java` |
| verification registry and catalogs | `geocedg/specs/operations/verification-registry.json`, `verification-junit-inventory.json`, `verification-static-contracts.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1` |
| profile authority amendment | `tools/agent/workspace-profile-validation.ps1` |
| upstream record | `docs/upstream/modified-files.yml` |
| user guide | `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md` |
| status | `docs/roadmap/geocedg_roadmap.md`, this report, `geocedg/validation/pre-g9b-r4/pre-g9b-r4-candidate-evidence.json` |

The existing test edits change numeric literals and expected id lists only; no
test method is added, renamed or removed, so no historical selection identity
changes. `G9U1ProfileCompilerTest` keeps its identity-stable method name and
explains the new counts in a comment.

## 5. Verification registration

The canonical `PHASE` selection `PRE-G9B-R4` requires `compile.shared.semantic`,
`compile.desktop.semantic`, `workspace-profile.product`,
`junit.shared.pre-g9b-r4.semantic` and `junit.desktop.pre-g9b-r4.semantic`.

| Selection | Filters | Identities |
|---|---|---|
| `pre-g9b-r4.shared` | `EuclidianConstantsTest.allModesShouldHaveHelp`, `EuclidianConstantsTest.allModesShouldHaveName`, `GeoCeDGCommandsTest` | 5 |
| `pre-g9b-r4.desktop` | the two focal classes plus `GeoCeDGProfileTest`, `G9U1ProfileCompilerTest`, `G9U1ActionRegistryTest`, `G9U1WorkspaceSurfaceTest`, `G9U1IconReviewTest`, `GeoCeDGToolImageResourceTest`, `PostP1BilingualUserGuideTest`, `G9U1ProductPolicyTest`, `locus.G9U0ToolSurfaceTest`, `G9U1SimilarityToolsTest`, `G9S1NativeArchivePersistenceTest`, `org.geogebra.desktop.util.ResourceAvailabilityTest` | 155 |

The shared selection names its two `EuclidianConstantsTest` methods explicitly.
That keeps out the conditional help-page test, which is skipped at this baseline
and allowlisted only for `final.shared`.

The inventory was regenerated with the official updater from real producer
evidence: `--test-dry-run` discovery for both modules, plus completed passing
executed runs of `pre-g9b-r4.shared`, `pre-g9b-r4.desktop` and `final.desktop`.
None was edited by hand.

| Selection | Before | After |
|---|---|---|
| `discovery.shared` | 5 925 | 5 925 (identical hash) |
| `discovery.desktop` | 1 536 | 1 563 |
| `final.shared` | 6 842 | unchanged; no shared test identity changed |
| `final.desktop` | 1 530 | 1 557 |

The catalog pins were repinned after reproducing each previous pin exactly from
the base blob with `Get-VerificationCanonicalTextSha256`:

| Pin | Before | After |
|---|---|---|
| `junit_inventory` | `8b9d2719…` | `19a8c1cd…` |
| `static_contracts` | `d4fec7c6…` | `f32de70b…` |
| `workspace.profile-authority` → `workspace-profile-validation.ps1` | `7dfe8f45…` | `44d9c8d7…` |

`workspace-profile-validation.ps1` gains the bounded `PRE-G9B-R4` amendment list
of the three new action ids, as the A7 amendment did. The historical planning
catalog `application-profile-v2.candidate.yml` is not edited.
`verification-final-coverage.Tests.ps1` records 29 selections and 40 PHASE
selections, plus the two R4 selection counts.

## 6. Pre-candidate evidence (development diagnostics, not acceptance)

All of the following ran on the uncommitted working tree. They are
`DEVELOPMENT_DIAGNOSTIC` evidence for correctness before freezing, never
acceptance or closeout evidence.

| Check | Result |
|---|---|
| scratch characterization probes of the existing command, kept outside the tree | confirmed the argument promotion, the per-form oversized behaviour and value-equality closure recorded in section 2 |
| focal classes `PreG9BR4SplineV2AuthoringTest` and `PreG9BR4OrderedListToolTest` | 27/27 passed after correcting test-harness faults (mode setting through the application, the host release callback, the application panel needed by Input Help) |
| focal plus adjacent Desktop regressions (20 classes) | 247 run; one pinned toolbar expectation updated, then passing |
| shared `EuclidianConstantsTest`, `GeoCeDGCommandsTest`, `CommandsValidationTest`, `CommandDispatcherTest` | passed; the only skip is the known conditional help-page test |
| Checkstyle `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 violations, after six baseline test variables were made `final` |
| producer `pre-g9b-r4.shared` (executed) | 5/5 passed |
| producer `pre-g9b-r4.desktop` (executed) | 155/155 passed |
| producer `final.desktop` (executed, full Desktop suite) | 1 557 identities: 1 556 passed, 1 allowlisted disabled case; inner exit 0 |
| producers `discovery.shared` / `discovery.desktop` (`--test-dry-run`) | 5 925 / 1 563 identities |
| `verify.ps1 -Profile PHASE -Phase PRE-G9B-R4 -PlanOnly` | 9 nodes, coverage `COMPLETE` |
| `verify.ps1 -Profile FINAL -PlanOnly` | 68 nodes, coverage `COMPLETE`, plan hash `3d8752b9…` unchanged from the base |
| `verify.ps1 -Profile INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-e562dffac806488089a7201ab0e4a608`) |
| `verify.ps1 -Profile STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3. The first run's whitespace diagnostic flagged the CR of the 27 inserted bundle lines under Git's default policy; those lines were rewritten with LF, like the earlier GeoCeDG additions to the same mixed-EOL bundles. The rerun `verification-9c44ca4f876646f1952157e456130996` leaves only the standing governance finding and the unavailable historical-consistency projection |
| `Assert-GeoCeDGUpstreamBoundary` against baseline `9b93256b…` | OK, 817 registered files |
| `git diff --cached --check`, default and `cr-at-eol` policy | clean |

The frozen acceptance, `tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R4`,
runs once on the exact clean committed candidate. Its result is reported outside
this artifact.

## 7. Impact statements

```text
PRODUCT_PHASE_EFFECT               = DESKTOP AUTHORING UX (new tools, help, Input Help)
KERNEL / COMMAND / SPLINE SEMANTICS = NONE
SERIALIZATION / DURABLE IDENTITY    = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED — one new PHASE selection, its two
                                     node pairs and inventory selections, regenerated
                                     inventory counts, three repinned hashes, updated
                                     selection/phase counts in the final-coverage suite
                                     and the bounded profile-authority amendment; no
                                     verifier, projection, schema or policy logic changed
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, Conda, packaging,
download or build/verification entry point changed; the registry additions reuse
the existing producer, projection and profile machinery.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md; docs/user/geocedg_user_guide_es.md
```

The guide update is bounded. §7.2 of both editions describes the three Spline V2
tools, the default degree, the explicit degree, the explicit closed form, the
different failure paths and the named argument objects. A new §5.8 describes
Create List from Selection, and §16.1 gains the two workflow rows. The 17 stable
section markers are unchanged. The guide index and navigation redesign (`R5-A`)
is not performed.

## 8. Retained debt and observations

Entry overlap check against the latest ledger (the generated-state maintenance
closeout record): none of `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER`,
`BASELINE-G9U1-BRANDING-EVIDENCE-PIN`, `OBS-R2-PHASE-SELECTION-COHORT`,
`GUIDE-A1-SECTION-9.3-STALE` or the four retained observations touches SplineV2
authoring, lists, help or localization, so R4 absorbs none. All carry forward
unchanged.

| Identifier | State |
|---|---|
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | resolved by this candidate, pending author review |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` | new observation: every SplineV2 form shows its promoted argument list and degree as named objects; unchanged kernel behaviour; author disposition |
| `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` | new observation: an open selection whose distinct first and last points have exactly equal values is periodic by the kernel rule; author disposition |
| `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | new observation: the SplineV2 tools, unlike Polyline, create no points on empty canvas, which keeps cancel side-effect-free; a future usability choice |

## 9. Authorization state

`PRE-G9B-R4` stops with this technical candidate for author review and the
required author smoke. `R5-A`, `R5-B`, `R6`, `R7`, `G9B`, `G9C`, `G9U2`,
productive `G10` and further `G12` remain unauthorized. Push, branch publication,
merge, promotion, tag, release and binary publication are outside this phase; each
requires a separate explicit author instruction naming the exact candidate SHA.

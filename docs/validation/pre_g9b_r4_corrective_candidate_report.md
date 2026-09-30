# PRE-G9B-R4 — author-smoke corrective candidate report

```text
TECHNICAL_CANDIDATE_STATE = PREPARED FOR ONE CLEAN IMMUTABLE CORRECTIVE COMMIT
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R4, corrective descendant T_R4C of the initial candidate
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = BOUNDED_PHASE (unchanged; no escalation)
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R4
T_R4                      = 558c15c9537fdde8c1915fd34e810d7ade969f93
T_R4 tree                 = 43f6946113beb8436d73321071958686cbc39323
T_R4 state                = initial technically accepted candidate, preserved unchanged
T_R4 PHASE                = verification-133e6b111d9d48a29ced957f81069e4c, ACCEPTED / COMPLETE
AUTHOR_SMOKE              = CORRECTIVE FINDINGS
CORRECTIVE_COMMIT (T_R4C) = the commit that contains this artifact; its parent is T_R4;
                            not nameable inside it, reported separately
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical commit
is the sole authority for approval. The
[initial candidate report](pre_g9b_r4_candidate_report.md) and its
[evidence](../../geocedg/validation/pre-g9b-r4/pre-g9b-r4-candidate-evidence.json)
stay unchanged as history. Machine-readable evidence of this descendant:
[`geocedg/validation/pre-g9b-r4/pre-g9b-r4-corrective-evidence.json`](../../geocedg/validation/pre-g9b-r4/pre-g9b-r4-corrective-evidence.json).

## 1. Entry identity

| Identity | Value |
|---|---|
| initial candidate `T_R4` (parent of this descendant, not amended) | `558c15c9537fdde8c1915fd34e810d7ade969f93`, tree `43f6946113beb8436d73321071958686cbc39323` |
| implementation base of `PRE-G9B-R4` | `62b1281b5006272d4f9b1a0ae9d413e1f7b94320`, tree `8ce2f6719f1094e300beeaafbadab4cd5135a8d5` |
| local `main` = `origin/main` = live remote `main` at entry | `62b1281b5006272d4f9b1a0ae9d413e1f7b94320` |
| worktree at entry | clean at `T_R4` |
| local branch (not pushed; not identity) | `feature/pre-g9b-r4-spline-v2-list-authoring-ux` |

The frozen governance is unchanged: the canonical prompt
`.github/prompts/tasks/pre-g9b-r4-spline-v2-list-authoring-ux.prompt.md` (blob
`51abda02734bb7884c8b811b26ba10a14a56119f`) is not edited, the class stays
`BOUNDED_PHASE` and the acceptance stays the one `PHASE` selection. No FULL and
no `GOV-R` run.

## 2. Author-smoke findings and dispositions

### 2.1 Finding A — SplineV2 tools not shown as selected

**Cause.** Selecting a mode sets the toolbar selection through the host mode
(`ProfileToolbar.setMode`), and `app.setMode(137|138|139)` did select the
semantic-curves flyout. The initial candidate then opened Input Help, whose
`setShowAlgebraInput` refreshes the menu bar and so the action registry. That
refresh writes `SELECTED_KEY = null` for every mode action; Swing fires a
property event even for `null → null`, and the flyout's listener treated it as
"deselected" and cleared the flyout. The listener is G9U1 code: any menu-bar
refresh while a flyout mode was active cleared it, for Locus V2 too; R4 only
made it fire on every SplineV2 activation.

**Fix (minimum seam).** In `GeoCeDGWorkspaceController`, mode entries of a
mixed flyout take their selected state only from the host mode. The registry's
toggle state (`SELECTED_KEY`) keeps its authority for the non-mode toggle
actions only. No parallel selection state, no new action, icon or toolbar entry.

**Evidence.** Focal test
`theToolbarShowsEachSplineVariantSelectedThroughTheHostMode` builds the real
toolbar. It chooses each variant through the registry and through the flyout
popup, checks the selected flyout and its active entry before and after a
menu-bar refresh, and checks that choosing Move clears the flyout and selects
Move. It also checks that the popup holds exactly one entry per variant, bound to
the one registry action.

### 2.2 Finding B — activation opened Input Help

**Fix.** Choosing a SplineV2 tool now only sets the mode: it neither opens nor
closes Input Help and does not change Algebra Input visibility. The SplineV2
selection moved to one place, `AppGeoCeDG.setShowInputHelpPanel`. Whenever the
user shows Input Help while a SplineV2 tool is active, it selects the existing
localized `SplineV2` syntax, scrolls it into view (the retained one-line
`InputBarHelpPanelD.focusCommand` change) and applies the corrected Algebra Input
tooltip. So every explicit route behaves alike: **Help → Input Help**, the
command-list help action, **Help → Current tool help**, and the Input Help button
of Algebra Input, which is host code that the registry never reaches. Outside a
SplineV2 tool, Input Help keeps its inherited behaviour.

**Evidence.** `invokingTheToolSetsItsModeWithoutChangingInputHelpVisibility`
(replaces `invokingTheToolSetsItsModeAndOpensInputHelpOnTheExistingSyntax`)
checks all three tools with Input Help initially hidden and initially shown.
`explicitInputHelpDuringASplineModeShowsTheExistingLocalizedSyntax` checks the
three request routes for each tool in `en` and `es`. It checks the selected
`SplineV2` entry, the localized syntax, the tooltip, the empty input field, the
unchanged construction, and that another tool keeps the host help. The contextual
help test additionally checks that activation alone left Input Help hidden.

### 2.3 Finding C — List tools toolbar group

A new presentation group `construction-lists` (name key
`Group.Construction.Lists`: "List tools" / "Herramientas de listas") contains
only `construction.list-from-selection`. It is the second entry of
`toolbar_group_ids`, immediately right of `edit-selection` (Move), and it is
rendered as an ordinary native mode group. It has one Construction menu entry,
right after Polygons. The action is removed from `construction-polygons`. Its
id, mode 140, icon, availability and catalog cluster (`linear-geometry-tools`,
of the fixed 18) are unchanged. Only the profile manifest changed; the profile
compiler, schema and validation rules needed no change. The two new strings are
profile `localized_text`.

**Evidence.** `theToolOwnsTheListToolbarGroupRightOfSelectionAndLeavesPolygons`
checks the rendered order, the single placement, the native group, the en/es
name and the selected state after invocation. The pinned toolbar-order and
Polygons expectations of `G9U1ProfileCompilerTest` and `G9U1WorkspaceSurfaceTest`,
the Construction-menu presence check and the native group count of
`GeoCeDGProfileTest` (9 → 10) are updated to the new layout. No existing test
method was added, renamed or removed in those three classes.

### 2.4 Finding D — clicking a Spline V2 selects its containing list

**Diagnosis (scratch probes outside the tree; the author fixture
`artifacts/preg-g9b-r4/TestNewTools.cedg`, SHA-256
`fbff3f70c7a9cf5f11baa3d5e39f763b9274b1278c1f2efa1aa15a345f3a2dad`, was only
read from a temporary copy and left byte-unchanged).**

| Scenario | Base `62b1281b` | `T_R4` |
|---|---|---|
| author fixture: click on Spline V2 `f`, member of visible list `l4` | hits `[l4]`, selects `l4` | identical |
| author fixture: click on `g`, `h`, `j` | each selects itself | identical |
| typed `l4={g,h,f}`: click on `f` | selects `l4` | identical |
| same, `l4` hidden | selects `f` | identical |
| same, `f` moved to layer 5 | the hit becomes `f` | identical |
| list built by Create List from Selection instead of typed | — (tool absent) | identical to the typed list |
| control: a Locus V2 `L` in a typed list | selects the list | identical |
| isolated empty preferences (max layer 0) | `f` selects itself | identical |

**Mechanism.** A fresh application in this environment reaches
`maxLayerUsed = 5` while the host loads its factory settings XML: view-bound
numbers are created through the construction defaults. New ordinary objects,
including lists, then take layer 5. `GeoLocusV2` has no default geo type, so a
Locus V2 family output (Locus V2 and Spline V2 alike) stays on layer 0. The host
hit filter keeps only top-layer hits and drops a list only when other hits
remain, so the list wins.

**Classification.** `PRE_EXISTING_HOST_BEHAVIOUR` together with a pre-existing
Locus V2-family default-layer gap. It is not an R4 regression, not specific to
SplineV2 and not specific to tool-built lists. **No fix**: it is recorded as
`OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` (section 8).

## 3. Preserved behaviour

Unchanged: modes 137–140 and their text keys, default degree `3`, the
explicit-degree second gesture, the closed form only through Closed Spline V2
(`AD-R0-8`), the ordinary dependent `GeoList`, cancel, one undo point, contextual
help text, the corrected tooltip text, localization, and every other R4 focal
behaviour. `EuclidianConstants`, the menu bundles, `GeoCeDGEuclidianController`,
`GeoCeDGSplineV2Authoring`, `GeoCeDGProfile` and `InputBarHelpPanelD` are
byte-unchanged from `T_R4`. No kernel, command, serialization or identity change.

Retained unchanged: `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` (a retained
observation, not an R4 defect) and
`OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` (no kernel change).

## 4. Changed paths

| Layer | Paths |
|---|---|
| Desktop GeoCeDG | `AppGeoCeDG.java`, `GeoCeDGActionRegistry.java`, `GeoCeDGWorkspaceController.java` |
| product profile | `apps/geocedg/application-profile.yml` |
| focal tests | `PreG9BR4SplineV2AuthoringTest.java`, `PreG9BR4OrderedListToolTest.java` |
| pinned layout expectations | `G9U1ProfileCompilerTest.java`, `G9U1WorkspaceSurfaceTest.java`, `GeoCeDGProfileTest.java` |
| specification | `geocedg/specs/ui/spline-v2-and-ordered-list-authoring.md` |
| user guide | `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md` |
| verification catalogs | `verification-junit-inventory.json`, the `junit_inventory` pin in `verification-registry.json`, `tools/agent/tests/verification-final-coverage.Tests.ps1` |
| upstream record | `docs/upstream/modified-files.yml` (purposes only; no new path) |
| status | `docs/roadmap/geocedg_roadmap.md`, this report, `pre-g9b-r4-corrective-evidence.json` |

## 5. Verification registration

The `PRE-G9B-R4` PHASE selection, its nodes, filters and required checks are
unchanged. Only test identities changed. `PreG9BR4SplineV2AuthoringTest` went
from 19 to 21 methods (one replaced, two added) and `PreG9BR4OrderedListToolTest`
from 8 to 9. The inventory was regenerated with the official updater from real
producer evidence on this tree: `--test-dry-run` discovery for both modules and
completed passing executed runs of `pre-g9b-r4.desktop` and `final.desktop`. No
hand edit; no historical selection was touched.

| Selection | `T_R4` | `T_R4C` |
|---|---|---|
| `discovery.shared` | 5 925 | 5 925 |
| `discovery.desktop` | 1 563 | 1 566 |
| `pre-g9b-r4.shared` | 5 | 5 (not rerun; no shared identity changed) |
| `pre-g9b-r4.desktop` | 155 | 158 |
| `final.shared` | 6 842 | unchanged (not rerun) |
| `final.desktop` | 1 557 | 1 560 |

| Pin | `T_R4` | `T_R4C` |
|---|---|---|
| `junit_inventory` | `19a8c1cd9c457a79c5be5a6ac09746d24e582687c694323f60c48a7129f9d05a` | `5980e9850220ac9b791583b2422c70ac2f5e59f0264e9cf124a744ed2cf6d707` |
| `static_contracts` | `f32de70b01b1315a65ed9ebed8623ae85e36cc6def873385bb8a18561a1f481f` | unchanged |
| `workspace-profile-validation.ps1` | `44d9c8d708143fa16cf6f0e839936a3e54e26e3436c334a5363c5627f87ff23a` | unchanged (no action id changed) |

The previous `junit_inventory` pin was first reproduced exactly with
`Get-VerificationCanonicalTextSha256`. `verification-final-coverage.Tests.ps1`
records the new `pre-g9b-r4.desktop` count; its 29 selections and 40 PHASE
selections are unchanged.

## 6. Pre-candidate evidence (development diagnostics, not acceptance)

All of the following ran on the uncommitted working tree of this descendant. They
are `DEVELOPMENT_DIAGNOSTIC` evidence, never acceptance or closeout evidence, and
no evidence bound to `T_R4` is reused.

| Check | Result |
|---|---|
| scratch probes, kept outside the tree (finding A on `T_R4`; finding D on the base and on `T_R4`; menu-only mode on the base and on this tree) | established the causes and classifications of section 2 and the observation of section 8 |
| focal and adjacent Desktop classes of `pre-g9b-r4.desktop` | 158/158 passed |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | 0 violations, after one import-order correction in `AppGeoCeDG` |
| producer `pre-g9b-r4.desktop` (executed) | 158/158 passed; inner exit 0 |
| producer `final.desktop` (executed, full Desktop suite) | 1 560 identities: 1 559 passed, 1 allowlisted disabled case; inner exit 0 |
| producers `discovery.shared` / `discovery.desktop` (`--test-dry-run`) | 5 925 / 1 566 identities |
| `Assert-GeoCeDGUpstreamBoundary` against baseline `9b93256b…` | OK, 817 registered files |
| `verify.ps1 -Profile PHASE -Phase PRE-G9B-R4 -PlanOnly` | 9 nodes, coverage `COMPLETE`, plan hash `f6ae0452…` |
| `verify.ps1 -Profile INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, 0 diagnostics (`verification-4994a9625a2f4b7fb36b16c7325f9466`) |
| `verify.ps1 -Profile STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3 (`verification-7a7a8ddbaf3742bda723db0023d236f8`); the whitespace, documentation and style diagnostics are clear, leaving only the standing governance finding and the unavailable historical-consistency projection |
| `git diff --cached --check`, default and `cr-at-eol` policy | clean |

The frozen acceptance, `tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R4`,
runs once on the exact clean committed `T_R4C`. Its result is reported outside
this artifact.

## 7. Impact statements

```text
PRODUCT_PHASE_EFFECT               = DESKTOP AUTHORING UX CORRECTION (toolbar selected
                                     state, user-controlled Input Help, List tools group)
KERNEL / COMMAND / SPLINE SEMANTICS = NONE
SERIALIZATION / DURABLE IDENTITY    = NONE
VERIFICATION_INFRASTRUCTURE_IMPACT = MODIFIED — regenerated inventory counts for three
                                     selections, one repinned hash and one updated
                                     selection count in the final-coverage suite; no
                                     selection, node, verifier, projection, schema or
                                     policy logic changed
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, JDK, Gradle, toolchain, packaging, download or
build/verification entry point changed.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md; docs/user/geocedg_user_guide_es.md
```

The guide update is bounded. It covers the Construction menu table and toolbar
order of §3.2–3.3, the location of the list tool in §5.8 and §16.1, and the
user-controlled Input Help in §7.2 of both editions. Section markers are
unchanged; `R5-A` is not performed.

## 8. Retained debt and observations

| Identifier | State |
|---|---|
| `TD-R0-SPLINEV2-TOOLTIP-INVALID-SYNTAX` | resolved by the candidate, pending author review (unchanged) |
| `OBS-R4-SPLINEV2-DURABLE-ARGUMENT-PROMOTION` | retained unchanged; not an R4 defect |
| `OBS-R4-KERNEL-CLOSURE-BY-ENDPOINT-VALUE-EQUALITY` | retained unchanged; no kernel change |
| `OBS-R4-TOOLS-SELECT-EXISTING-POINTS-ONLY` | retained unchanged |
| `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST` | new observation (finding D): a Locus V2-family output stays on layer 0 while ordinary objects and lists take the application's maximum used layer, so a click on the curve selects a visible list that contains it; pre-existing on the base with a typed list; not fixed; author disposition |
| `OBS-R4C-PROFILE-TOOLBAR-MENU-ONLY-MODE-KEEPS-SELECTION` | new observation: choosing a mode that has no toolbar button (for example Hyperbola from the menu) leaves the previous group selected; the host toolbar would show Move. Measured identical on the base for native groups and flyouts; unchanged here, and SplineV2 now behaves like every other tool; author disposition |

Generic `GeoList` selection debt is not fixed here.

## 9. Shortened author smoke

1. The toolbar shows **List tools** as the second group, right of Move, holding
   only **Create List from Selection**. Polygons no longer contains it, and
   **Construction → List tools** holds it (English and Spanish).
2. Choose **Spline V2**, **Spline V2 with degree** and **Closed Spline V2** in
   turn from the semantic-curves flyout. Each time the flyout is shown selected
   with the chosen variant. Choose Move: the flyout is cleared and Move is
   selected.
3. With Input Help closed, choose each SplineV2 tool: Input Help stays closed and
   Algebra Input visibility does not change. With Input Help open, choosing a tool
   leaves it open.
4. With a SplineV2 tool active, open Input Help through **Help → Input Help**,
   through the Input Help button of Algebra Input, and through **Help → Current
   tool help**. Each time `SplineV2` is selected, visible and shows the localized
   syntax, in English and in Spanish.
5. Build one spline with each tool (three or more points; degree 4 in the degree
   tool; the closed tool). Each is one undo step, and Escape cancels without
   creating anything.
6. Build a list with **Create List from Selection** from its new group, in click
   order, finishing by selecting the first object again. It is one undo step.
7. Reopen the author fixture: clicking the Spline V2 inside the visible list still
   selects the list. That is the recorded pre-existing observation, not a
   regression.

## 10. Authorization state

`PRE-G9B-R4` stops with this corrective technical candidate for author review and
the shortened author smoke. `R5-A`, `R5-B`, `R6`, `R7`, `G9B`, `G9C`, `G9U2`,
productive `G10` and further `G12` remain unauthorized. Push, branch publication,
merge, promotion, tag, release and binary publication are outside this phase;
each requires a separate explicit author instruction naming the exact candidate
SHA.

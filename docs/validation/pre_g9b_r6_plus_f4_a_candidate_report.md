# PRE-G9B-R6-plus-F4-A — compact style-bar thickness entry (scope amendment 01): candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-F4-A (scope amendment 01; not a new phase)
CHANGE_ROUTE              = ORDINARY
ARCHITECTURE              = A1 — GeoCeDG-owned Desktop (no inherited source change)
VERIFICATION_CLASS        = BOUNDED_PHASE — MAINTAINED by the amendment's impact analysis
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-F4-A,
                            -Profile INFRA_UNIT, -Profile STATIC (once each on the exact
                            candidate commit and tree; reported outside this file)
INTEGRATION / FINAL       = NOT RUN (above the frozen class)
F4-A E5 FOCUS FEASIBILITY = PASS (real-window gate, §3)
PRODUCT CHANGE            = YES — GeoCeDG Desktop presentation only:
                            GeoCeDGEuclidianStyleBar (new), GeoCeDGEuclidianView,
                            GuiManagerGeoCeDG, GeoCeDGLineThicknessField
SHARED / KERNEL CHANGE        = NONE
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
GLOBAL SWING / LAF SETTINGS   = UNCHANGED
GUIDE_IMPACT              = UPDATED (EN/ES user guide sentence)
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
F4-A TECHNICAL ACCEPTANCE = PENDING
F4-A AUTHOR SMOKE         = PENDING (not performed or attributed by the agent)
F4-A AUTHOR APPROVAL      = NOT GRANTED
F4-A PUBLICATION          = NOT AUTHORIZED
F4 AGGREGATE CLOSEOUT     = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
passClaimed               = false
```

This artifact records only facts fixed when it was written. A later author decision naming
the exact technical commit is the sole authority for acceptance and publication. The
candidate commit cannot name itself; its identity and the acceptance runs are reported
outside this file. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-a-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-a-candidate-evidence.json).

## 1. Authority and lineage

| Commit | Role |
|---|---|
| `e968245ef329a86108df81a49cbdb05601cefe9e` | published F4-D closeout, F4-A implementation base |
| `1b944afbc88ef46402c20fccd49eea5e66d64371` | Operation 1 (F4-D published status, direct-publication preference) |
| `e7b235dca9afc68f051faefa133e2db585bfea4c` | F4-A focus gate STOP ([report](pre_g9b_r6_plus_f4_a_focus_gate_stop_report.md)), historical |
| `72f6c5af24bd1651e19764c8bdb02814949bb4b9` | [scope amendment 01](pre_g9b_r6_plus_f4_a_scope_amendment_01_record.md) (STATIC `verification-70b649a2a5f74a9fbc0a5b8e07ab252a`) |
| this candidate | on `72f6c5af` |

The WIP `9b755225eadf3c2040029f6bee0b455f1d5bb89c` was reused selectively and is not an
ancestor. Its failed focusability fallback is not part of the candidate. The STOP report,
its evidence (`SHA256SUMS.txt` `3c432fcd438673237a731ec9f47cd665bb1c9190c285a164f1638a77426728e7`)
and the WIP stay unchanged.
[Canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md)
and [design §2](../architecture/pre_g9b_r6_plus_f4_two_track_design_successor_candidate.md)
as amended.

## 2. Product delta

- **`GeoCeDGEuclidianStyleBar` (new).** A GeoCeDG subclass of the inherited
  `EuclidianStyleBarD`. After every `createButtons()` (construction, language and font
  changes), for the line-style button only, it:
  - moves the inherited slider, unchanged and with its listeners, into a GeoCeDG row
    (`GridBagLayout`) with a fresh `GeoCeDGLineThicknessField` at its right. The field is
    vertically centred, sized from its font (columns = digits of the slider maximum) and
    separated by the width of a space in that font. The line-type table stays above;
  - shows that one popup light-weight (`setLightWeightPopupEnabled(true)`) without the
    FlatLaf drop shadow (`Popup.dropShadowPainted = false`). The inherited popup stays
    non-focusable;
  - on opening:
    - dismisses the button's tooltip, which a press dismisses anyway;
    - after placement, enables the field only if its window can take the keyboard;
    - shows the popup once more in place if it landed in a separate window only because
      a tooltip window, hidden by FlatLaf one event later, still overlapped it;
  - on closing, ends a pending entry as focus loss ends it in Object Properties. A valid
    integer is applied; anything else is restored. This happens before the closing click
    can change the selection;
  - leaves the single undo step of an accepted entry to the field.
- **Installation.** Graphics 1 through `GeoCeDGEuclidianView.newEuclidianStyleBar()`.
  Graphics 2 through a `GuiManagerGeoCeDG.newEuclidianView` override that returns the
  inherited `EuclidianViewFor3DD` with only its style-bar factory overridden.
- **`GeoCeDGLineThicknessField`.** Adds `isApplying()`, which is true while an accepted
  entry moves the slider. The Properties field behaves exactly as before.
- **Docs and registration.** The EN/ES user guide gains one sentence; the provenance
  manifest gains entries for the new source and test files.
- **Unchanged.** No change to `PopupMenuButtonD`, `EuclidianStyleBarD`, `GuiManager3D`,
  `PropertiesPanelD` or any inherited class, to global Swing settings, or to Classic.

## 3. Real-window gate (scope amendment 01)

**Procedure.**
- Native `java.awt.Robot` input in the real application (`GeoCeDG.main`, isolated
  settings), on the product runtime Temurin 25.0.4 with the `runGeoCeDG` JVM arguments.
- Windows 11, FlatLaf Light.
- Display scale 1.25, the Windows setting during the runs, and also forced to 1.0 and 1.5
  with `sun.java2d.uiScale`.
- A key is sent only after the last click has reached the JVM, the pointer is where it was
  moved and the field is the focus owner; otherwise the probe aborts.
- The gate ran on the working tree before the freeze. The four product source files of the
  candidate have the same blob identities as the gated tree (§11).

| Item | Result |
|---|---|
| 1 popup opens reliably and stays visible | PASS (Graphics 1 and 2; also after Enter) |
| 2 field receives real keyboard focus | PASS |
| 3 digits entered and edited (Backspace, Home, Delete, End, a letter) | PASS |
| 4 Enter commits | PASS |
| 5 Escape cancels the edit (value restored; popup stays open) | PASS |
| 6 focus loss as in Properties: outside click applies a valid entry and restores a refused one; Tab applies | PASS |
| 7 slider still works (native drag 6 → 10) | PASS |
| 8 field and slider synchronized both ways | PASS |
| 9 line-type table still applies line types | PASS |
| 10 no accelerator while typing (mode, selection, objects, view, windows unchanged) | PASS |
| 11 exactly one undo step per accepted value | PASS |
| 12 Graphics 1 and Graphics 2 | PASS (full list in both) |
| 13 Classic unchanged (inherited bar, popup = table and slider, native slider drag works) | PASS |
| 14 no duplicated controls after language and font-size changes; the field works after both | PASS |
| 15 horizontal arrangement under scaling (centre offset 0 px, gap = font space, preferred size) at 1.0, 1.25 and 1.5 | PASS |
| hover until the button tooltip shows, then click | PASS (light-weight, field usable) |

**3D compatibility.**

| Configuration | Result |
|---|---|
| 3D Graphics docked beside Graphics | popup light-weight, focus, typing, Enter, one undo: PASS |
| Graphics narrowed so the popup overlaps the 3D view (`intersects=true`) | popup drawn above the 3D view, interactive: PASS |
| window resized to 1100 × 700, popup overlapping and not overlapping the 3D view | PASS |

**Window bounds.**

| Configuration | Result |
|---|---|
| window 900 × 330 | light-weight, PASS |
| window at the bottom-right screen edge | light-weight, PASS |

The runtime chose a separate (heavy-weight) popup window only when another window of the
application covered the popup area.
- The probe placed such a window.
- In that case the field is **disabled**: it is greyed out and a click gives it no focus.
  The slider and table still work.
- After the covering window closes, the next opening is light-weight again.

This is the restricted availability characterized for the author (§9).

Evidence: `artifacts/pre-g9b-r6-plus-f4-a/e5-gate-evidence/` (ignored), 101 files, with
`SHA256SUMS.txt`. The final runs are `f4a-e5-gate-run3` (full list), `-scale1.0`,
`-scale1.5` and `f4a-e5-classic`. The earlier runs 1–2 and the diagnostics `f4a-e5-diag-*`
established and fixed the tooltip case.

## 4. Tests

`PreG9BR6PlusF4AStyleBarThicknessTest` (in-JVM, 12 tests, each one obligation):

| Test | Obligation |
|---|---|
| `graphics1AndGraphics2StyleBarsCarryOneFieldBesideTheSlider` | Graphics 1 and 2: table, then one row with the inherited slider and the field; light-weight, no drop shadow, inherited non-focusable popup; the field text |
| `theFieldSitsRightOfTheSliderOnOneRowSizedFromItsFont` | layout: centred, right of the slider, preferred size, columns from the maximum, font-based gap, width follows the font |
| `theFieldIsAttachedAgainAfterLanguageAndFontChanges` | new button and one field after a language and a font change |
| `classicStyleBarsHaveNoField` | Classic Graphics 1 and 2: inherited bar, table and slider, heavy-weight popup, no field |
| `theFieldFollowsTheSliderTheSelectionAndTheCreationModeDefaults` | following, polygon minimum 0, creation-mode default equals moving the slider |
| `refusedEntriesRestoreTheValueAndChangeNothing` | `""`, `abc`, `2.5`, `-1`, `0`, `14`, `7x` refused, no change, no undo |
| `anAcceptedEntryEqualsMovingTheSliderAndStoresOneUndoStep` | multi-selection with differing line types: same targets and line type as the slider, exactly one undo step |
| `undoRedoSaveAndReopenKeepTheEnteredThickness` | Undo, Redo, save and reopen |
| `thePropertiesFieldKeepsItsOwnBehaviour` | one Properties field under its own name, still applying |
| `closingThePopupEndsTheEditAsFocusLossDoesInProperties` | close applies a valid entry, restores a refused one, one undo step |
| `aPopupWindowIsCoveredOnlyByAnotherVisibleWindowOverIt` | cover rule |
| `theFieldIsOnlyEditableWhereTheKeyboardReachesIt` | keyboard-reachability rule |

`PostE2P1R2SmokeFollowUpTest` (Properties field) is unchanged and passes (7 of 7). Phase
selection `pre-g9b-r6-plus-f4-a.desktop`: these two classes plus the Graphics 1/2 lifecycle,
navigation, presentation, layer, orientation-cue, dual-activation and guide classes
(159 identities).

## 5. Regression

- **Graphics 1 and Graphics 2.**
  - In-JVM: lifecycle, navigation and presentation classes in the selection.
  - Real window: §3.
- **Classic.**
  - In-JVM: `classicStyleBarsHaveNoField`.
  - Real window: item 13.
  - Classic constructs neither the GeoCeDG views nor the GeoCeDG GUI manager.
- **Properties.**
  - `PostE2P1R2SmokeFollowUpTest` and `thePropertiesFieldKeepsItsOwnBehaviour`.
  - The field's only change is the read-only `isApplying()`.
- **Desktop suite.** `final.desktop` producer: 2127 tests, 0 failures, 1 skipped (the allow-listed disabled upstream case), no heap failure.

## 6. Registration

| Element | Content |
|---|---|
| phase | `PRE-G9B-R6-PLUS-F4-A`: `compile.desktop.semantic`, `junit.desktop.pre-g9b-r6-plus-f4-a.semantic` |
| nodes | `junit.desktop.pre-g9b-r6-plus-f4-a.producer` and `.semantic`, with impact paths on the four product files, the inherited style-bar, popup and Properties files, the guides, the manifest and the selection's test classes |
| inventory | selection, discovery and `final.desktop` refreshed by the official updater; `junit_inventory` catalog pin and final-coverage pins (62 selections, 67 phases) updated |
| provenance | `docs/upstream/modified-files.yml`: two added files and three purposes extended; upstream boundary green |

## 7. Unchanged semantics

The following are as before:
- the thickness property, range, minimum, validation, serialization, `applyLineStyle` and
  the inherited line-type coupling;
- construction defaults;
- kernel, geometry, units, layers and DAG.

Every other popup of GeoCeDG and Classic keeps the application's heavy-weight popups.

## 8. Accepted design deviations from the WIP

- The failed focusability fallback is removed.
- The field moved from below the slider to its right.
- New, required by the gate:
  - the close-commit;
  - the reachability rule;
  - tooltip dismissal with a single in-place re-show.

## 9. Risks and limitations

| Id | Description | Disposition |
|---|---|---|
| `F4A-R-HEAVYWEIGHT-RESTRICTED` | when another visible window of the application overlaps the popup area, Swing shows the popup in a separate window, and the field is disabled there (slider and table usable) | **restricted availability, presented for author disposition** |
| `F4A-R-TOOLTIP-RESHOW` | after hover-and-click the popup can appear once in a separate window and be shown again in place on the next event (a possible one-frame flicker) | characterized; relies on FlatLaf 3.7 hiding a reused tooltip window one event later |
| `F4A-R-NO-SHADOW` | this one popup has no drop shadow; the table spans the wider popup | visual, approved by the amendment |
| `F4A-R-LAF-VERSION` | the reachability rule and the re-show depend on Swing and FlatLaf popup behaviour (JDK 25.0.4, FlatLaf 3.7) | a later JDK or FlatLaf update needs the real-window gate again |
| `F4R-LINETYPE-COUPLING` | an entered thickness re-applies the selected line type (inherited) | unchanged, as decided (`DQ-F4-4`) |
| `F4R-DEFAULTS-UNDO` | creation-mode defaults are outside the undo XML (inherited) | unchanged |
| in-JVM limits | in-JVM tests cannot observe native focus or popup windows | covered by the gate (§3) and the author smoke |

## 10. Author-smoke checklist (not performed or attributed by the agent)

1. GeoCeDG, a segment, Move tool, select it. In the Graphics style bar, open the
   line-style menu. Line types should show above, and below them the slider with a
   numeric field at its right, on one row.
2. Click the field, type `9`, press Enter. The line thickens, the slider moves to 9 and the
   menu stays open. Edit → Undo once restores the old thickness; Redo.
3. Type `3`, press Escape: the field shows the current value again, nothing changes.
4. Type `4`, click an empty spot of the drawing: 4 is applied. Type `abc` and click outside:
   nothing changes.
5. Drag the slider: the field follows. Pick a dashed line type: it applies.
6. Rest the pointer on the line-style button until its tooltip appears, then click it. The
   field is still usable.
7. Repeat 1–4 in Graphics 2. Open 3D Graphics beside Graphics and narrow Graphics so that
   the menu overlaps the 3D view. The menu is drawn above it and works.
8. Change the product language and the font size: one field, still working.
9. Choose the Segment tool, set a thickness in the field, draw a segment: it uses that
   thickness.
10. Save, reopen: the thickness is kept. Object Properties → Style: its own field behaves
    as before.
11. Classic diagnostic session: the line-style menu has no numeric field.

## 11. Pre-freeze evidence

These runs preceded the freeze. They are development and inventory evidence, not
acceptance.

| Run | Result |
|---|---|
| focused classes on the gated tree | `PreG9BR6PlusF4AStyleBarThicknessTest` 12 of 12, `PostE2P1R2SmokeFollowUpTest` 7 of 7 |
| Checkstyle (`checkstyleMain`, `checkstyleTest`) | no finding on the new or changed files |
| upstream boundary | green |
| producers | `discovery.shared` 6075 and `discovery.desktop` 2133 identities (`-TestDryRun`); `pre-g9b-r6-plus-f4-a.desktop` 159 of 159; `final.desktop` 2127 tests, 0 failures, 1 skipped |
| gated product blobs | `GeoCeDGEuclidianStyleBar` `cedfc164`, `GeoCeDGEuclidianView` `4dd8d9eb`, `GeoCeDGLineThicknessField` `3defa6c1`, `GuiManagerGeoCeDG` `33567950` (`gated-product-blobs.txt` in the evidence folder) |

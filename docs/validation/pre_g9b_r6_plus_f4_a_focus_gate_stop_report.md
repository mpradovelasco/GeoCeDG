# PRE-G9B-R6-plus-F4-A — focus feasibility gate stop report

```text
RECORD_KIND               = FEASIBILITY GATE STOP REPORT (documentary)
PHASE                     = PRE-G9B-R6-plus-F4-A
AUTHORIZATION             = author instruction "PRE-G9B-R6-plus-F4-D-PUBLISHED + F4-A
                            IMPLEMENTATION" (2026-10-14), Operation 2
IMPLEMENTATION BASE       = e968245ef329a86108df81a49cbdb05601cefe9e
                            tree 516f28dfe75dd1e35954f1e1e224dcf10edd4d2b (published F4-D
                            closeout); intermediate ancestor 1b944afbc88ef46402c20fccd49eea5e66d64371
                            (Operation 1, documentary)
ARCHITECTURE              = A1 — GeoCeDG-owned Desktop (no inherited change)
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen by the authorization; no acceptance run)
GATE RESULT               = STOP — F4-A FOCUS FEASIBILITY BLOCKED
  preferred design        = FAILED (the field never receives keyboard focus)
  approved fallback       = FAILED (the focusable popup is not shown on screen)
F4-A TECHNICAL CANDIDATE  = NOT ESTABLISHED (no product change on this branch)
WORK IN PROGRESS          = local branch wip/pre-g9b-r6-plus-f4-a-focus-blocked,
                            commit 9b755225eadf3c2040029f6bee0b455f1d5bb89c
                            tree 69b6677219d82e8c3af81d7f5b44475e3421b052
                            parent 1b944afb (unverified; not a candidate)
F4-A AUTHOR SMOKE         = NOT APPLICABLE (no candidate)
F4-A PUBLICATION          = NOT AUTHORIZED
F4 AGGREGATE CLOSEOUT     = NOT AUTHORIZED
PHASE G                   = NOT AUTHORIZED
selfApproved              = false
```

The canonical prompt
[`pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md`](../../.github/prompts/tasks/pre-g9b-r6-plus-f4a-stylebar-thickness.prompt.md)
makes the real-window keyboard-focus gate the first, blocking step: native input
(`java.awt.Robot`) in the real GeoCeDG window must show that the field in the
line-style popup takes keyboard focus. The only admissible fallback is "a GeoCeDG-owned
focusability of the popup that contains the field, reached through the component ancestry
while the field is attached"; otherwise `STOP — F4-A FOCUS FEASIBILITY BLOCKED`, with no
architecture change. Both the preferred design and that fallback failed. This report
records the evidence and the options for the author's disposition. The canonical prompt,
the F4 design records and the F4-D records are unchanged. Machine-readable mirror:
[`pre-g9b-r6-plus-f4-a-focus-gate-stop.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f4-a-focus-gate-stop.json).

## 1. Entry gate

- Published `main` = live remote `main` = `e968245e` (tree `516f28df`), clean worktree,
  index and stash; branch `phase/pre-g9b-r6-plus-f4-a` from that commit; Operation 1
  (`1b944afb`, STATIC `verification-51ca8c0a54174edcb3faa66cd105f790` `ACCEPTED / COMPLETE`)
  completed first.
- `EuclidianStyleBarD.java`, `PopupMenuButtonD.java` and `GuiManager3D.java` are
  byte-identical to the baseline `9b93256b`. `EuclidianViewD.java` is not: it carries the
  registered, approved `PRE-G9B-R6-plus-B` and `-C` print-route modifications
  (2026-10-02/04, earlier than the F4 design). Reconciled: the file is unchanged since the
  F4 preparation base `847bb20a`, and the seam used by F4-A, the protected
  `newEuclidianStyleBar()`, is identical to the baseline. The prompt's wording is
  inaccurate for this file; no change was made to it.

## 2. What was built (development evidence, on the WIP branch only)

Architecture A1 as approved:
- `GeoCeDGEuclidianStyleBar` subclasses `EuclidianStyleBarD`; after every
  `createButtons()` (construction, language and font changes) it attaches a fresh
  `GeoCeDGLineThicknessField` bound to the line-style slider in its popup.
- Graphics 1 uses it through `GeoCeDGEuclidianView.newEuclidianStyleBar()`. Graphics 2
  uses it through a `GuiManagerGeoCeDG.newEuclidianView` override that returns the
  inherited view with only its style-bar factory overridden.
- An accepted entry moves the slider. `GeoCeDGLineThicknessField.isApplying()` lets the bar
  leave the single undo point to the field. Without it, a multi-selection whose objects
  differ in line type would get two undo points.
- The WIP also contains the fallback (the popup holding the field made focusable).

Development results, on the uncommitted tree with the fallback:

| Check | Result |
|---|---|
| `PreG9BR6PlusF4AStyleBarThicknessTest` (new, in-JVM) | 8 of 8 |
| `PostE2P1R2SmokeFollowUpTest` (Properties field) | 7 of 7 |
| selection `pre-g9b-r6-plus-f4-a.desktop` | 155 of 155 |
| `final.desktop` producer | 2123 tests, 0 failures, 1 skipped |
| inventory | updated by the official updater; catalog and coverage pins re-pinned |
| upstream boundary | green, 1064 registered files |
| Checkstyle | no finding on the touched files |
| development `INFRA_UNIT` / `STATIC` | accepted |

The in-JVM tests use synthetic events; they cannot see keyboard focus and are not gate
evidence. None of these results is acceptance evidence.

## 3. Gate evidence (native input, real window)

**Procedure.**
- The probe is a scratch JUnit class run from an init script. It starts
  `GeoCeDG.main --settingsfile=<isolated> --language=en`, creates and selects a segment,
  shows the Graphics style bar, and opens the line-style popup by a native click.
- Every click and keystroke is a `java.awt.Robot` event. A key is sent only after the last
  click has been received in the JVM, the pointer is where it was moved and the field owns
  the focus.
- Runs on Windows 11:
  - JVM 17.0.10 (the Desktop test JVM);
  - repeated on the product runtime Temurin 25.0.4, with the `runGeoCeDG` JVM arguments.
- Look-and-feel: FlatLaf Light.

**Environment block (resolved, not a result).**
- First attempts sent no input at all: the author's elevated "Administrator: PowerShell"
  window held the foreground, and Windows (UIPI) silently discarded `Robot` and
  `SetCursorPos` input. No key was sent.
- After the author minimized it (2026-10-14), native input reached the window.

**Results.**

| Case | Popup window | Field on screen | Native click on the field | Keyboard |
|---|---|---|---|---|
| preferred design, Graphics 1 and 2 (JDK 17 and 25) | heavy-weight `Popup$HeavyWeightWindow`, `focusableWindowState=false` | yes | focus stays in the drawing pad (or goes to a dock split pane) | goes to the drawing pad |
| approved fallback (popup focusable), Graphics 1 (JDK 17 and 25) | heavy-weight `###focusableSwingPopup###`, `focusableWindowState=true` | **no**: `JPopupMenu.isVisible()` is true, the field is not showing and the screenshot shows no popup | impossible | not reached |
| Classic (JDK 17) | inherited style bar `EuclidianStyleBarD`; popup holds table and slider only | — | — | — |

Observed hazard of the preferred design: in the first run, keystrokes meant for the field
reached the drawing pad of the probe's isolated application. `Delete` deleted the selected
segment, `Home` moved the view, and `Enter` moved focus to the Input bar. A field that is
shown but cannot take focus is therefore not only useless but unsafe.

**Cause.**
- The inherited `GuiManager3D` calls `JPopupMenu.setDefaultLightWeightPopupEnabled(false)`
  (for the 3D view), so GeoCeDG shows its popups in heavy-weight windows.
- `PopupMenuButtonD` creates the popup non-focusable, so that window refuses the keyboard.
- Making the popup focusable (the fallback) gives a focusable popup window. Swing's popup
  keyboard helper then returns focus to the frame, and the window is not displayed. The
  internal reason for the missing display (FlatLaf and AWT on Windows) was not
  established.
- The gate items after focus could not be reached:
  - typing and editing;
  - Enter, Escape and focus loss;
  - popup usability;
  - the slider;
  - accelerators while typing.

**Probe-only experiments** (not product code; for the author's options):

| Experiment | Change on the one popup, from the probe | Result (JDK 17 and 25, Graphics 1) |
|---|---|---|
| E5 | light-weight popup (`setLightWeightPopupEnabled(true)`) without the FlatLaf drop shadow (`Popup.dropShadowPainted=false`), popup left non-focusable | the popup is drawn inside the frame. A native click gives the field real focus, typing `8` and Enter applies thickness 8 with exactly one undo step, and the popup stays open |
| E5f | E5 plus the focusable popup | same result (typing `6`, one undo step) |

E5 and E5f were not tested for Escape, focus loss, slider, table, Graphics 2, the
language/font rebuild or a popup that does not fit inside the window. In that last case
Swing falls back to a heavy-weight window.

## 4. Why this is a stop

The prompt's stop condition is met exactly: the gate fails and the only admissible
fallback does not work. Proceeding would need a change the author has not authorized:
- a different popup presentation (E5);
- a different field placement;
- an inherited change (A2).

No candidate was frozen, and no PHASE, INFRA_UNIT or STATIC acceptance run was made for
F4-A.

## 5. Options for the author

| Option | Content | Architecture and class | Evidence | Open points |
|---|---|---|---|---|
| **A — light-weight popup (recommended, scratch-proven)** | GeoCeDG-owned presentation of the one line-style popup that holds the field: light-weight, without the FlatLaf drop shadow, through the same component ancestry. No inherited change | A1, `BOUNDED_PHASE` | E5/E5f: real focus, typing, Enter, one undo step | the full gate on the amended design (Escape, focus loss, slider, table, Graphics 2, rebuilds, Classic); visual change (no drop shadow on that popup); behaviour when the popup does not fit the window (heavy-weight fallback; the field must then be made safe, e.g. disabled) |
| B — field outside the popup | A small numeric field directly in the Graphics style bar beside the line-style button. The bar lives in the frame, so focus is ordinary | A1, `BOUNDED_PHASE`; changes the approved placement (`DQ-F4-2`) | none yet | the inherited bar rebuilds its buttons in a private method on every update; needs a design and its own gate |
| C — A2 | profile-gated inherited change in `PopupMenuButtonD` (focusable or light-weight popup) | A2, `INTEGRATED_PHASE`; inherited modification | none | not authorized by the F4 decisions |
| D — withdraw or defer F4-A | keep the numeric entry in Object Properties only; `OBS-POST-E2-R2-COMPACT-STYLEBAR-THICKNESS-INPUT` stays open or is re-dispositioned | — | — | F4 aggregate closeout records F4-A as not implemented |

## 6. Preserved evidence

- WIP branch `wip/pre-g9b-r6-plus-f4-a-focus-blocked` (`9b755225`): implementation,
  fallback, focused tests, registry, inventory, pins, manifest and guide sentences.
  Unverified.
- `artifacts/pre-g9b-r6-plus-f4-a/gate-evidence/` (ignored). It holds:
  - the probe sources;
  - the init scripts for JDK 17 and JDK 25;
  - the Gradle logs;
  - the run logs and screenshots of runs 1–12;
  - the `RobotCheck` diagnostic.
  - `SHA256SUMS.txt` (SHA-256 `3c432fcd438673237a731ec9f47cd665bb1c9190c285a164f1638a77426728e7`)
    lists all 91 files.
- Key runs:

  | Run | Content |
  |---|---|
  | 5 | preferred design, first native run; keystrokes reach the drawing pad |
  | 6 | preferred design, Graphics 1 and 2 |
  | 7–8 | fallback |
  | 9–10 | fallback and experiments |
  | 11 | Classic |
  | 12 | everything on JDK 25 |

## 7. State after this report

```text
PRE-G9B-R6-plus-F4-D       = PASS — AUTHOR APPROVED — PUBLISHED (unchanged)
F4-A DESIGN                = AUTHOR APPROVED (unchanged)
F4-A FOCUS GATE            = STOP — F4-A FOCUS FEASIBILITY BLOCKED
F4-A IMPLEMENTATION        = NOT ESTABLISHED; awaiting author disposition (options A–D)
F4-A PUBLICATION           = NOT AUTHORIZED
F4 AGGREGATE CLOSEOUT      = NOT AUTHORIZED
PHASE G                    = NOT AUTHORIZED
selfApproved               = false
```

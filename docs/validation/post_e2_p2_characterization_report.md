# POST-E2-P2 — unexpected zoom during Algebra Input: characterization report

```text
TECHNICAL_CANDIDATE_STATE = CHARACTERIZATION CANDIDATE, FROZEN with the commit that
                            contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P2
ACTIVITY                  = CHARACTERIZATION ONLY
VERIFICATION_CLASS        = BOUNDED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P2
BASELINE                  = b6747a1bf829051ff95a29c281558e00d3f82459
                            tree b525a285e1b5b1552b0c7c48922b9834c41df79b
                            (POST-E2-P1-R2 closeout, published main)
OBSERVATION               = OBS-E2-3 (zoom-out while typing in Algebra Input; author
                            refinement: when the first argument is introduced)
CLASSIFICATION            = GEOCEDG-SPECIFIC, CONFIGURATION-DEPENDENT
                            (a user-assigned printable factor-zoom shortcut fires as a
                            window-scope menu accelerator while the text is typed)
PRODUCT CHANGE            = NONE
implementationAuthorized  = false
selfApproved              = false
passClaimed               = false
```

This artifact records only facts fixed when it was written; it carries no author
approval. The candidate commit cannot name itself; its identity and its `PHASE`
run are reported outside this file. Authority:
[R2 closeout record](post_e2_p1_r2_closeout_record.md); plan:
[POST-E2 planning proposal, P2](../architecture/post_e2_planning_proposal.md). Mirror:
[`post-e2-p2-characterization-evidence.json`](../../geocedg/validation/post-e2/post-e2-p2-characterization-evidence.json).
Statements are labelled **[observed]**, **[code]**, **[hypothesis]** or
**[recommendation]**.

## 1. Result

**[observed]** The Graphics view zooms when the user types a capital letter whose
Shift chord is assigned to a GeoCeDG factor-zoom action. The author's preference
file assigns **Shift+A** to *Zoom Factor Out* and **Shift+Z** to *Zoom Factor In*,
factor 10. A first argument is usually a point label such as `A`, typed with
Shift+A, so the zoom-out appears "when the first argument is introduced". The
argument position is incidental: `A` as a second argument (`Segment(B,A)`), `Z`
anywhere and a command name starting with `A` (`Angle`) trigger the same action.

Causal chain (runtime stack, §4): the `KEY_PRESSED` Shift+A event reaches the
focused Algebra Input field, which has no binding for it and does not consume it;
Swing then processes window-scope bindings, finds the menu accelerator of
**View → Zoom Factor Out**, clicks the item, and the GeoCeDG action calls
`EuclidianController.zoomInOut` (an animated zoom by 1/10). The `KEY_TYPED` event
still inserts `A` into the text.

```text
Root cause      = a printable chord (Shift + letter) accepted as a factor-zoom
                  shortcut and installed as a JMenuItem accelerator, i.e. a
                  WHEN_IN_FOCUSED_WINDOW binding active in every text field of the
                  window
Affected layer  = GeoCeDG Desktop navigation-shortcut policy (POST-G9U1-A7):
                  GeoCeDGNavigationShortcutPreferences.valid() accepts
                  SHIFT_DOWN_MASK as the only modifier
Introduced      = POST-G9U1-A7 b249a62a (2026-09-12); not E2, not R2
Upstream        = not present (no such actions or chords); the Swing routing that
                  carries the chord is inherited and unchanged
Deterministic   = yes: 4 of 4 bound presses zoomed (3 x Shift+A, 1 x Shift+Z);
                  0 of 94 other keystrokes changed the view
Preferences     = decisive: default (unassigned) and the author's file without the
                  two chords never zoom; the two chords alone always zoom
```

## 2. Method

**[observed]** Real windowed application (`GeoCeDG.main`, Temurin 25 as the
product launcher), from a scratch JUnit probe kept outside the tree (init-script
source set; not a product test):

- isolated preference files: a fresh file, a byte-identical copy of the author's
  `%APPDATA%\GeoCeDG\5.4\preferences.properties` (+ tool-library files; SHA-256
  `ab02841d…` equal to the source, which was never written), the same copy without
  the two shortcut keys, and a file holding only `Shift+A` for zoom-out;
- objects `A=(1,1)`, `B=(4,2)`, `C=(2,4)`, `s=Segment((0,3),(2,3))`; before each
  sequence the view is reset to `xscale = yscale = 50`, `xZero = 215`, `yZero = 315`
  and the Algebra Input field gets the focus (focus owner
  `AutoCompleteTextFieldD` in every row);
- each character is typed as `KEY_PRESSED` (with `VK_SHIFT` pressed first for
  shifted characters), `KEY_TYPED` and `KEY_RELEASED`, dispatched to the field
  through the keyboard focus manager, so global key dispatchers, the field's key
  listeners and key map, window key bindings and menu accelerators all run as for
  real input; Spanish layout key codes (`(` = Shift+8, `)` = Shift+9);
- after every keystroke (350 ms settle, one event-thread turn): `xscale`, `yscale`,
  `xZero`, `yZero`, `xmin`, `xmax`, `ymin`, `ymax`, view size, mode, focus owner,
  input text and Input Help visibility;
- ByteBuddy (agent on the test JVM) instruments every `EuclidianView` method that
  can change the coordinate system (`setCoordSystem` overloads, `setXscale`,
  `setYscale`, `setXZero`, `setYZero`, `updateBounds`, `keepCenter`, `updateSize`,
  `updateSizeKeepCenter`, `zoom`, `zoomAroundCenter`, `setRealWorldCoordSystem`,
  `setStandardView`, `setAnimatedCoordSystem`, `setCoordSystemFromMouseMove`) and
  records a stack trace for every actual change and for every zoom or animation
  start.

Sequences: `Segment(A,B)`, `Circle(A,2)` (existing labels), `Circle((1,2),3)` (new
point expression), `Slider(0,10)` (numeric literal), `Length(s)` (lowercase
label), `Midpoint(B,C)` (labels without a chord), `Segment(B,A)` (label in the
second position), `Polygon(B,C,Z)` (Z), and `Angle(A,B,C)` in a first run.

Limitations: synthetic events after the operating system (no OS keyboard, IME or
dead keys); the `KEY_PRESSED`/`KEY_TYPED` pairing is the one Windows delivers for
these keys. The probe closed the autocompletion popups it saw open, which
plays no part in the view state.

## 3. Keystroke trace (author configuration, `Segment(A,B)`)

| key | stroke | text | xscale / yscale | xZero / yZero | xmin … xmax | ymin … ymax |
|---|---|---|---|---|---|---|
| start | — | `` | 50 / 50 | 215 / 315 | −4.30 … 7.64 | −2.16 … 6.30 |
| `S`…`t` | Shift+S, e, g, m, e, n, t | `Segment` | 50 / 50 | 215 / 315 | unchanged | unchanged |
| `(` | Shift+8 | `Segment()` | 50 / 50 | 215 / 315 | unchanged | unchanged |
| **`A`** | **Shift+A** | `Segment(A)` | **5 / 5** | **289.7 / 221.4** | **−57.94 … 61.46** | **−40.32 … 44.28** |
| `,` | comma | `Segment(A,)` | 5 / 5 | 289.7 / 221.4 | unchanged | unchanged |
| `B` | Shift+B | `Segment(A,B)` | 5 / 5 | 289.7 / 221.4 | unchanged | unchanged |
| `)` | Shift+9 | `Segment(A,B)` | 5 / 5 | 289.7 / 221.4 | unchanged | unchanged |

Mode 0 (Move), view size constant (597 × 423), Input Help hidden, focus in the
field throughout. The same row pattern holds for every sequence of the run:

| run | keystrokes | Shift+letter presses | view changes | changing keys |
|---|---:|---:|---:|---|
| R2 `b6747a1b`, author settings copy | 98 | 18 | 4 | Shift+A ×3 (50 → 5), Shift+Z ×1 (50 → 500) |
| R2, fresh settings (chords unassigned) | 98 | 18 | 0 | — |
| R2, Classic (`GeoGebra3D`, fresh settings) | 98 | 18 | 0 | — |
| R2, author copy without the two chords | 50 | 14 | 0 | — |
| R2, only `Shift+A` = zoom-out | 50 | 14 | 3 | Shift+A ×3 (50 → 5) |
| E2 `86fdc908`, author settings copy | 49 | 12 | 4 | Shift+A ×3, Shift+Z ×1 |
| A5 `71483011` (before A7), author settings copy | 49 | 12 | 0 | — (feature absent) |

`Circle((1,2),3)`, `Slider(0,10)`, `Length(s)` and `Midpoint(B,C)` never changed
the view in any run: a new point expression, a numeric literal, a lowercase label
or a label without an assigned chord does not trigger it.

## 4. Event that changes the viewport

**[observed]** Call that starts the zoom, at the Shift+A press (identical for
every occurrence; Swing frames shortened):

```text
EuclidianView.zoom(EuclidianView.java:5341)
EuclidianView.setAnimatedCoordSystem(EuclidianView.java:5463/5469)
EuclidianController.zoomInOut(EuclidianController.java:11249)
GeoCeDGEuclidianController.zoomByFactor(GeoCeDGEuclidianController.java:424)
GeoCeDGActionRegistry.execute(GeoCeDGActionRegistry.java:288)        (285 for zoom-in)
GeoCeDGActionRegistry.invoke(GeoCeDGActionRegistry.java:236)
GeoCeDGActionRegistry$1.actionPerformed(GeoCeDGActionRegistry.java:98)
javax.swing.AbstractButton.doClick
javax.swing.plaf.basic.BasicMenuItemUI$Actions.actionPerformed        (accelerator)
javax.swing.JComponent.processKeyBinding
javax.swing.JMenuBar.processBindingForKeyStrokeRecursive (×5) / processKeyBinding
javax.swing.KeyboardManager.fireKeyboardAction                         (WHEN_IN_FOCUSED_WINDOW)
javax.swing.JComponent.processKeyBindingsForAllComponents / processKeyBindings
javax.swing.JComponent.processKeyEvent
org.geogebra.desktop.gui.inputfield.MyTextFieldD.processKeyEvent(MyTextFieldD.java:343)
java.awt.DefaultKeyboardFocusManager.dispatchKeyEvent … (KEY_PRESSED, SHIFT_DOWN_MASK, VK_A)
```

The coordinate change itself is applied about 0.1 s later by the animation timer
(`CoordSystemAnimationD.actionPerformed` → `CoordSystemAnimation.step` →
`stopAnimation` → `EuclidianView.setCoordSystem(double,double,double,double)`,
`xscale 50 → 5`, `xZero 215 → 289.7`, `yZero 315 → 221.4`). No other path changed
the view.

## 5. Hypotheses examined

| Hypothesis (planning proposal P2) | Evidence | Result |
|---|---|---|
| Ctrl+Z / Ctrl+Y forwarded from the field (undo of a view state) | no Ctrl key in the sequences; no undo frame in any stack | not the cause |
| Ctrl+- / Ctrl+M forwarded from the field | no Ctrl key; the zoom stack has no `GlobalKeyDispatcher` frame | not the cause |
| **user-assigned GeoCeDG factor-zoom chord** | stack §4; decisive preference variants §3 | **cause** |
| armed Zoom Window applied by a click | no mouse event; mode stays 0 | not the cause |
| command recognition, first-argument resolution, label lookup, highlighting | 0 view changes with fresh settings over the same sequences | not the cause |
| autocompletion and Input Help callbacks | suggestion popups opened and closed; Input Help hidden; no view frame | not the cause |
| input preview (`ScheduledPreviewFromInputBar`) | no preview frame in any stack; 0 changes with fresh settings | not the cause |
| resize / `keepCenter` / `EuclidianSettings` updates | view size constant; no `updateSize`/`keepCenter` change recorded | not the cause |

## 6. Ownership and upstream comparison

**[code]**

- `GeoCeDGNavigationShortcutPreferences` (GeoCeDG, added by POST-G9U1-A7): `valid()`
  accepts any `KEY_PRESSED` chord whose modifiers include one of Ctrl, Alt, Shift or
  Meta, so Shift alone with a letter or digit is valid; `applyAccelerators()` puts
  the chord on the registry action as `ACCELERATOR_KEY`.
- `GeoCeDGMenuBar` builds the View menu item with `new JMenuItem(action)`, so the
  chord becomes a JMenuItem accelerator, which Swing (`BasicMenuItemUI`) registers
  as a `WHEN_IN_FOCUSED_WINDOW` binding of the main window.
- `MyTextFieldD.processKeyEvent` (upstream, unchanged; the pinned baseline
  `9b93256b` has the same body) delegates to `JComponent.processKeyEvent`; neither
  the field's key map nor its focused/ancestor input maps bind Shift+A, so the
  press reaches the window bindings. This is standard Swing behavior.
- Specification `cedg-workspaces.md` (A7 navigation paragraph) and the user guide
  §12.4 describe the two chords as "independent, initially unassigned" with
  Available / Invalid / conflict feedback; they set no rule against printable
  chords.

Upstream comparison: GeoGebra Classic at the pinned baseline has no factor-zoom
actions or chords (no `org.geocedg` file at `9b93256b`); its menu accelerators use
Ctrl or Ctrl+Shift. The in-tree Classic path (`GeoGebra3D`) typed the same 98
keystrokes with no view change. The defect is **GeoCeDG-specific** and
**configuration-dependent**; it is reproduced at E2 (`86fdc908`) and absent before
A7 (`71483011`), consistent with "observed in earlier phases".

The author's file also holds an obsolete `geocedg.navigation.zoom-window.shortcut.v1`
(Shift+Z) from the first A7 iteration; the product has ignored that key since
`ee0f6acd` and it plays no part.

## 7. Invariant and distinction

Intended invariant: *entering or resolving command arguments must not change the
Graphics view scale or origin.* A key that produces text in a focused text field
is text entry, not a navigation request. Explicit navigation stays legitimate:
menu clicks, toolbar tools, mouse wheel and gestures, and chords that cannot
produce text (with Ctrl, Alt or Meta, or a non-printing key such as F1–F24). The
inherited `Ctrl`+`+`/`Ctrl`+`-` forwarding from the input field is a separate,
explicitly modified chord already documented in the guide; it was not observed
here and is not part of this defect.

## 8. Minimal corrective alternatives

**[recommendation]** (none implemented):

| Option | Change | Effect on the author's configuration | Owner and size |
|---|---|---|---|
| **A — reject printable chords** (recommended) | `valid()` requires Ctrl, Alt or Meta, or a non-printing key; Shift alone (or none) with a letter, digit, punctuation or space is *Invalid* in the dialog; a stored chord that is no longer valid loads as unassigned (the existing `decode` → `valid` path) | Shift+A / Shift+Z become unassigned; the dialog shows them unassigned and accepts e.g. Ctrl+Alt+… or F-keys | `GeoCeDGNavigationShortcutPreferences` only; spec and guide sentence; BOUNDED_PHASE |
| B — suppress while editing text | the factor-zoom actions ignore activations whose focus owner is an editable text component (keep printable chords for the Graphics view) | Shift+A still zooms when the view has the focus | registry or action guard; must distinguish accelerator activation from a menu click; larger test surface |
| C — view-scoped dispatch | keep the accelerator only as a menu label and dispatch the chord from the Graphics view key handling | Shift+A works only in the view | menu/dispatcher rewiring; larger |

Option A states the invariant at the single place where chords are admitted,
needs no event-source heuristics and leaves menus, toolbar and inherited
shortcuts untouched. A residual check belongs to its implementation (**[hypothesis]**, not tested here):
if AltGr arrives as Ctrl+Alt on Windows, a Ctrl+Alt chord on a key that produces a
character with AltGr on the active layout (for example `@`, `#`, `[`, `]`, `{`,
`}`, `\` on the Spanish layout) would fire while typing that character; the
validation should treat such chords as printable too, or the verification must
show they cannot fire.

Recommended implementation class: **BOUNDED_PHASE** (GeoCeDG-owned preference
policy, no kernel, document, serialization or upstream change), acceptance PHASE,
with a windowed regression that types the reproducing sequence with a previously
stored Shift+A and asserts the view scale and origin unchanged, plus
validation-matrix tests and the AltGr check. A correction needs a separate author
authorization and an amendment of the A7 navigation paragraph of
`cedg-workspaces.md`.

## 9. Committed characterization test

`PostE2P2AlgebraInputZoomCharacterizationTest` (4 tests, selection
`post-e2-p2.desktop` with `PostG9U1A7NavigationTest`) pins the current behavior,
one link per test: a Shift-letter chord is accepted as *Available*; the real
`GeoCeDGMenuBar` item binds it in the window scope; the Algebra Input field leaves
the press unconsumed; activating the item zooms the view by 1/10. The windowed
end-to-end path cannot run in the embedded host (Swing fires window-scope bindings
only for showing components); it is evidenced by the scratch probe (§2–§4). These
tests describe the defect; a correction will invert links 1 and 2.

## 10. Verification

Before the freeze: the two classes of the selection pass (4 + 7 tests, twice);
Checkstyle reports no violation in the new test; the official producers and the
inventory updater ran (all `COMPLETED`, exit 0): discovery 6046 / 1937,
`post-e2-p2.desktop` 11, `final.shared` 6963, `final.desktop` 1927 → 1931, 48
selections; registry `junit_inventory` pin `fc712b3e…` →
`c0a363827443dafbcc6ed8aef3515061a8dc37f3b170655c6855752616d46e24`; coverage
literals 48 selections, 55 PHASE selections, `post-e2-p2.desktop` 11. `PHASE POST-E2-P2` runs on the frozen commit and is reported
outside this artifact.

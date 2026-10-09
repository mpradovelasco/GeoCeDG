# POST-E2-P2-R1 — text-entry-safe navigation shortcuts: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = POST-E2-P2-R1
ACTIVITY                  = BOUNDED DESKTOP CORRECTION (Option A)
VERIFICATION_CLASS        = BOUNDED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase POST-E2-P2-R1
BASELINE                  = 32c3efc4a3ab9119d66b5b63f5e56a8394318b9e
                            tree 4d97967e5079ca187762e077dd12a7ebdda73ddc
                            (POST-E2-P2 characterization closeout, published main)
ROOT CAUSE                = GLOBAL TEXT-PRODUCING ACCELERATOR CONFLICT
PRODUCT CHANGE            = YES — KEYBOARD SHORTCUT VALIDATION
GEOMETRIC SEMANTICS       = UNCHANGED
SERIALIZATION             = UNCHANGED
CLASSIC                   = UNCHANGED
OPEN AUTHOR DECISION      = Ctrl+Alt chords on character keys (Windows AltGr), §4
AUTHOR_SMOKE              = PENDING
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written; it carries no author
approval. Authority: [P2 closeout record](post_e2_p2_closeout_record.md). The P2
[characterization report](post_e2_p2_characterization_report.md) and evidence are
unchanged. Mirror:
[`post-e2-p2-r1-candidate-evidence.json`](../../geocedg/validation/post-e2/post-e2-p2-r1-candidate-evidence.json).
Normative amendment: `geocedg/specs/ui/cedg-workspaces.md`, *POST-E2-P2-R1
amendment* after the A7 navigation paragraph (`PROPOSED`).

## 1. Change

Single owner: `GeoCeDGNavigationShortcutPreferences` (GeoCeDG, POST-G9U1-A7). The
existing pipeline is reused: `normalize` → validation → conflict detection →
`applyAccelerators`; loading is `decode` → `valid`. No second validator.

- `valid()` = `wellFormed()` (the unchanged A7 checks: `KEY_PRESSED`, a real key,
  no modifier key, no AltGraph, at least one of Ctrl, Alt, Shift, Meta) **and not**
  `entersText()`.
- `entersText(stroke)` on the normalized chord:
  - modifiers ⊆ {Shift}: text entry unless the key is a function key F1–F24.
    The key code is the physical key; its character depends on the layout, so
    every character key counts whatever it types (letters, digits, punctuation,
    dead keys, extended codes such as `ñ`, space, numeric keypad). Editing and
    navigation keys (Home, End, arrows, Delete, Insert, Enter, Tab) with Shift
    edit text in a field and count as well.
  - exactly Alt with a numeric-keypad digit: a Windows character code (Alt+0169).
- New status `ShortcutStatus.TEXT_ENTRY`; `DraftValidation.result()` maps it to
  `INVALID_SHORTCUT`, so `setConfiguration` commits nothing (factor, chords,
  accelerators, preference store unchanged; no undo point).
- `GeoCeDGNavigationSettingsPanel`: the text-entry status shows
  `Navigation.Shortcut.TextEntry` (EN/ES), Apply disabled.
- Stored preferences: `decode` already returns `null` for a chord that is not
  `valid`, so a stored Shift+A / Shift+Z loads as **unassigned** and binds no
  accelerator. Each chord is decoded on its own, so a valid second chord and the
  factor are kept. Loading writes nothing; the stored text stays as it was (it is
  rewritten only by an explicit Apply or Reset in the dialog).

Changed product files: `GeoCeDGNavigationShortcutPreferences.java`,
`GeoCeDGNavigationSettingsPanel.java`, `apps/geocedg/application-profile.yml`
(one text key). Not touched: `MyTextFieldD`, `AlgebraInputD`, the global key
dispatcher, `EuclidianView`, kernel, parsing, preview, serialization, Classic.

**Admissibility after R1.** Refused: Shift with any non-function key (Shift+A,
Shift+Z, Shift+B, Shift+1, Shift+2, Shift+`,` …, Shift+Space, Shift+keypad,
Shift+Home/arrow/Delete/Enter/Tab), Alt+keypad digit; a plain key stays refused by
the A7 rule. Retained under the existing reserved-key and conflict rules: Shift+F1–
F24, any chord with Ctrl or Meta (for example Ctrl+Shift+K, Meta+K,
Ctrl+Alt+Shift+F11), Alt or Alt+Shift with a non-keypad key (Alt+Shift+K).

Two restrictions go beyond the letter/digit/punctuation examples of the
instruction and are reported for the author's review: Shift with editing or
navigation keys (they edit text in a field) and Alt with keypad digits (Windows
character codes). Both only refuse chords; neither can cause a zoom.

## 2. Verification of the rule (focused tests)

| Test | Covers |
|---|---|
| `PostE2P2R1ShortcutValidationTest` (6) | Shift with A–Z, 0–9, keypad 0–9 and keypad operators, 14 punctuation and 2 dead keys, space, 7 editing and navigation keys and `ñ` → `TEXT_ENTRY`, old-style `SHIFT_MASK` normalized; Alt+keypad digits → `TEXT_ENTRY`; Shift+F1–F24, Ctrl+Alt+Shift+F11, Ctrl+Shift+K, Alt+Shift+K, Meta+K, Ctrl+keypad 5 → `AVAILABLE`; plain keys and modifier keys `INVALID`, Ctrl+Z / Ctrl+- `CONFLICT` (A7 rules unchanged); Ctrl+Alt+E `AVAILABLE` (open decision); a refused draft commits nothing (factor, stored chord, active accelerator, no undo point) and the dialog shows the EN and ES explanation; the author's stored `v1:90:64` / `v1:65:64` load as unassigned, factor 10 kept, no accelerator on the real menu items, the store and the obsolete zoom-window key not rewritten; an invalid stored chord leaves a valid stored second chord and factor 7.5 active, in both directions |
| `PostE2P2AlgebraInputZoomCharacterizationTest` (4, reconciled) | link 1: Shift+A refused; link 2: a stored Shift+A binds no window accelerator on the real menu item; link 3 (inherited, unchanged): the field leaves the Shift+A press to the window bindings; link 4: a legitimate chord (Shift+F7) is bound and its activation still zooms by the factor |
| `PostG9U1A7NavigationTest` (7), `G9U0R3MenuLifecycleTest` | A7 navigation, dialog, persistence and menu lifecycle unchanged |

The P2 candidate `216964dc` and its report keep the assertions of the defect
(Shift+A accepted, bound and zooming); the windowed baseline runs of §3 reproduce
it on the published baseline.

## 3. Windowed Desktop verification

Real `GeoCeDG.main` window, Temurin 25, ByteBuddy traces of every coordinate
change (P2 probe method, scratch, outside the tree), key events through the focus
manager into the real Algebra Input, Spanish layout; a fresh copy of the author's
preferences per run (the real `%APPDATA%` file kept its SHA-256 `ab02841d…`).
Sequences: `Segment(A,B)`, `Circle(A,2)`, `Angle(A,B,C)`, `Segment(B,A)`,
`Polygon(B,C,Z)`, `ZA=(1,2)`, `Text("AZ")` — capitals at the start, as first and
later arguments, inside a label and inside a text.

| run | keystrokes | unexpected view changes | texts typed as intended | Shift+F7 bound legitimately | menu Zoom Factor In |
|---|---:|---:|---|---|---|
| published baseline `32c3efc4`, author settings | 79 | **10** (every Shift+A / Shift+Z) | 7/7 | zooms (view and field) | zooms |
| candidate, author settings | 79 | **0** | 7/7 | zooms (view and field) | zooms |
| candidate, Classic, fresh settings | 35 | 0 | 3/3 | — | — |
| candidate, author settings, no reconfiguration | 12 | 0 | 1/1 | — | — |

The last run reloads a copy of the author's file and exits: the copy is
byte-identical afterwards (SHA-256 `ab02841d…`), so the stored Shift chords are
neither applied nor rewritten. Mouse-wheel, toolbar and menu navigation do not
pass through the changed validation; the A7 tests and the menu click above
exercise them.

## 4. Windows AltGr — characterization and open decision

- **[code]** `wellFormed` already refuses chords carrying `ALT_GRAPH_DOWN_MASK`, and
  `normalize` keeps that mask, so a chord recorded from an AltGr key event that
  carries AltGraph cannot be stored.
- **[code, JDK 25]** `SunToolkit.isPrintableCharacterModifiersMask` — the rule
  Swing's `DefaultKeyTypedAction` uses to accept typed characters — states that "on
  Windows, pressing ctrl + alt allows user to enter characters from the extended
  character set" and treats Ctrl+Alt as a printable modifier set. On the Spanish
  layout (active here) AltGr+2 = `@`, AltGr+E = `€`, AltGr+º = `\`, and physical
  Ctrl+Alt produces the same characters.
- **[observed]** An automated real-keyboard test was attempted with `java.awt.Robot`
  into the active probe window (Spanish layout): mouse injection worked, but no
  keyboard event reached the JVM, not even a plain letter, so the AltGr event
  modifiers could not be observed here. No keyboard input was injected after that.
- **Not established:** whether, for an AltGr character, the `KEY_PRESSED` event
  carries `ALT_GRAPH_DOWN_MASK` in addition to Ctrl+Alt (then it cannot match a
  stored Ctrl+Alt chord) or only Ctrl+Alt (then a stored Ctrl+Alt+E would fire
  while typing `€`).

R1 therefore implements only the characterized safe subset and changes nothing
for Ctrl+Alt chords: it introduces no new Ctrl+Alt case (they were already
accepted by A7). **Author decision needed** for Ctrl+Alt (with or without Shift) on
character keys:

| Option | Rule | Cost |
|---|---|---|
| D1 | refuse Ctrl+Alt (± Shift) with any character key, mirroring the JDK printable-mask rule | forbids some legitimate Ctrl+Alt chords |
| D2 | keep them and add a manual Windows smoke: bind Ctrl+Alt+E, type `€` with AltGr and with Ctrl+Alt in Input, observe the view | depends on the platform result |
| D3 | keep them (current R1 behavior) and document the residual risk | residual risk on layouts with AltGr |

## 5. Verification

Before the freeze: focused tests pass; Checkstyle (`:desktop:desktop:checkstyleMain`,
`checkstyleTest`) reports no new violation (one pre-existing `LineLength` in
`PreG9BR6PlusA1HiddenLayerTest`); upstream boundary passes; official producers and
the inventory updater as recorded in the evidence. `STATIC`, `INFRA_UNIT` and
`PHASE POST-E2-P2-R1` run on the frozen commit and are reported outside this
artifact.

## 6. Author smoke checklist

1. With your current preferences (Shift+A / Shift+Z stored): type `Segment(A,B)`,
   `Circle(A,2)`, `Angle(A,B,C)` and `ZA=(1,2)` in Input; the view does not move and
   the text is exact.
2. **View → Configure navigation zoom…**: both shortcuts show *Unassigned*, factor
   10. Press Shift+A in a shortcut field: the explanation appears and Apply stays
   disabled.
3. Assign Shift+F7 (or Ctrl+Alt+Shift+F11) to Zoom Factor Out, Apply: the shortcut
   zooms from the Graphics view; typing capitals in Input still does not zoom.
4. Optional (decision §4): bind Ctrl+Alt+E, type `€` with AltGr+E in Input and
   note whether the view zooms.

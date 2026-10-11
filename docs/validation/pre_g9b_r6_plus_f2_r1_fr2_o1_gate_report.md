# PRE-G9B-R6-plus-F2-R1 — revised FR-2 gate (Option O1)

```text
TASK                  = PRE-G9B-R6-plus-F2-R1-FR2-AUTHOR-DISPOSITION (conditional resumption
                        of PRE-G9B-R6-plus-F2-R1)
AUTHORITY             = F2-R1 decision record v1.3 §7 (disposition commit
                        fdb5517970d7fcf94f6fea43c2a6078f83ab19e4, tree
                        42303f97d2b274809cc4bea83643bfdb2910a2ba)
GATE BASE             = fdb55179 (child of the blocked gate commit 7027cf93); probe run
                        against an uncommitted working-tree prototype (reviewed WIP plus
                        the O1 override); no product commit before the gate
FR-2 (API subclass)   = FAIL — HISTORICAL EVIDENCE (feasibility gate report; not re-marked)
FR-2 (revised O1)     = PASS
FR-1 / FR-3 / FR-4    = still valid (re-exercised below)
VERIFICATION_CLASS    = BOUNDED_PHASE — frozen by this report (phase PRE-G9B-R6-PLUS-F2-R1)
PUBLICATION           = NOT AUTHORIZED
selfApproved          = false
```

This report records the revised `FR-2` gate required by the author disposition
(record v1.3 §7) separately from the
[original feasibility gate report](pre_g9b_r6_plus_f2_r1_feasibility_gate_report.md),
whose `FR-2` failure of the `GgbAPID`-subclass seam stays historical evidence.
Mirror: [`pre-g9b-r6-plus-f2-r1-fr2-o1-gate.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-fr2-o1-gate.json).

## 1. Mechanism evaluated

- `AppGeoCeDG.setMode(int)` (new override of the single-argument
  `App3D.setMode(int)`) runs the inherited call inside
  `GeoCeDGToolActivation.runAsSingleArgumentRequest`: without an enclosing
  explicit request or host-synchronization scope it is a genuine `NORMAL`
  request; otherwise it adds nothing.
- The native-toolbar marker (`ProfileToolbar.addNativeGroup` listener) records
  `{mode, behaviour from the ActionEvent's own Ctrl, current AWT event}` and is
  resolved **first** in `AppGeoCeDG.setMode(int, ModeSetter)`, so a marked Ctrl
  activation is never overridden by the single-argument default; any resolution
  discards the mark, and it applies only to the same AWT event and mode.
- Scoped requests are consumed by the first activation they decide; nested host
  activations inside them resolve as unmarked.
- Direct two-argument `setMode(int, ModeSetter)` calls (`setMoveMode`, dock
  panels, toolbar-panel switching, CAS) are host synchronization: same mode
  unchanged, another mode a departure.
- `GgbAPID`, `CallJavaScript.SandboxClassShutter` and `ScriptUtil.ALLOWED` are
  not touched; the former `GeoCeDGScriptingApi` subclass is not used.

## 2. Static routes (source)

| Step | Evidence |
|---|---|
| scripting entry | `GgbAPI.setMode(int)` → `app.setMode(mode)` (`shared/common/.../plugin/GgbAPI.java:918-921`); `GgbAPID` and `GgbAPIJre` do not override it |
| single-argument chain | `App3D.setMode(int)` (`desktop/geogebra3D/App3D.java:139-146`) → `App.setMode(int)` → `setMode(mode, ModeSetter.TOOLBAR)` (`shared/common/.../main/App.java:1660-1662`) |
| toolbar | `ToolToggleButton.actionPerformed` → `app.setMode(int)` (`desktop/gui/toolbar/ToolToggleButton.java:93`) |
| single-argument callers with a variable mode | toolbar button, `GeoCeDGActionRegistry.invoke`, `GeoCeDGUserTools`, `GeoCeDGUserToolLibrary`, `GgbAPI.setMode`, tool-creation dialogs (`ToolCreationDialogModel`, `ToolCreationDialogD`) — all user routes |
| single-argument callers with a fixed mode | Move, Text, checkbox, CAS, selection listener, MOW modes: none is Parallel or Perpendicular, so each is at most a departure |
| two-argument host callers | `setMoveMode(ModeSetter)`, `EuclidianController` dock-panel calls, `ToolbarContainer` toolbar switching (`:462`), `CASTableCellController`, `CASView` |

Residual risk (recorded, accepted by the O1 selection): a future inherited
single-argument caller with a variable Parallel/Perpendicular mode would be
treated as a genuine `NORMAL` request.

## 3. Runtime probe

Scratch JUnit `F2R1O1GateProbeTest` (one method) through a Gradle init script,
never tracked; shown `JFrame` with the real GeoCeDG toolbar; toolbar activations
by posted `MOUSE_PRESSED`/`MOUSE_RELEASED` on the real button or flyout item
(Ctrl as the event modifier); JavaScript through the real `ScriptManagerD`
(`ggbApplet.setMode`); dispatch origins captured by a probe-only `SET_MODE`
listener. `compileTestJava` rerun without the init script; probe result XML and
compile-stash copies removed. Two earlier runs had probe setup defects (a
memory-enabled empty-space click that completed a construction instead of a
partial gesture; a same-mode check started in `MEMORY_ENABLED`); the setup was
corrected and the recorded output is the final run.

| Output | SHA-256 |
|---|---|
| `f2r1o.gate.txt` | `a885d08964e27ec45374a9d8dc5694717483d9cd0e3be8cc5cde1af8028cc8d1` |
| probe source | `07c5ed381b1dc743aa13617496b05d26ced3797ac07732b61b5f9f64803b3c7b` |
| init script | `0c733680b8c192234b2ee8db76337245ec83f837e2a9b5b9e50ae4992d0dce27` |

| # | Check | Result |
|---|---|---|
| G1, G2 | `GgbAPI.setMode` → `AppGeoCeDG.setMode(int)` → `runAsSingleArgumentRequest` → `App3D.setMode` → `App.setMode(int)` → `AppGeoCeDG.setMode(int, ModeSetter)`; same for JavaScript (`NativeJavaMethod.call` → `GgbAPI.setMode:920`) | PASS (dynamic dispatch observed) |
| G3 | toolbar: `ToolToggleButton.actionPerformed:93` → `AppGeoCeDG.setMode(int)`; the GeoCeDG marker is notified before `ToolToggleButton` (listener order); Ctrl click → Parallel `MEMORY_ENABLED` | PASS |
| G4 | same mode: Ctrl reactivation keeps `MEMORY_ENABLED` (S4); normal reactivation → `NORMAL` (S5); scripted reselection → `NORMAL` (S2, Perpendicular via flyout too) | PASS |
| G5 | host synchronization on Perpendicular `MEMORY_ENABLED`: two-argument same mode (`DOCK_PANEL`, `TOOLBAR`), toolbar rebuild, language ES and back to EN (`FR-1`) → unchanged; two-argument other mode → departure `NORMAL` | PASS |
| G6 | `app.getGgbApi()` is exactly `GgbAPID`, same instance after a document load | PASS |
| G7 | clearing document load plus library JavaScript (global-scope initialization) → no exception; a library function calling `ggbApplet.evalCommand` and `ggbApplet.setMode` works; `evalCommand` `Line(P,l)` → `AlgoLinePointLine(P,l)`, `PerpendicularLine(Q,m)` → `AlgoOrthoLinePointLine(Q,m)` | PASS |
| S1 | `NORMAL` → script Parallel (JavaScript and Java API) → `NORMAL` | PASS |
| S2 | `MEMORY_ENABLED` Parallel with a reference-first partial selection → script reselects Parallel → `NORMAL`; selection 1 → 0, objects 10 → 10, undo history 2 → 2, saved flag unchanged (`FR-3`) | PASS |
| S2c | `MEMORY_ENABLED` Perpendicular (empty slot), provisional empty-space point → script reselect → `NORMAL`; objects 10 → 11 → 10, history 2 → 2, saved flag unchanged (`FR-3`) | PASS |
| S2b | `NORMAL` Parallel with a partial selection → script reselect → `NORMAL`, selection kept (1 → 1) | PASS |
| S3 | `MEMORY_ENABLED` Parallel → script Perpendicular → both `NORMAL` | PASS |
| S6 | `NORMAL` Parallel construction with `m` → `AlgoLinePointLine(Q,m)`; Parallel slot still `l`; point-first click waits for a reference (memory not read) | PASS |
| S7 | Ctrl toolbar Parallel after scripted selections → point-first click reuses `l`: `AlgoLinePointLine(R,l)` | PASS |
| M | a Ctrl mark recorded without activation does not survive: a later script or plain single-argument `setMode` gives `NORMAL` | PASS |
| status (`FR-4`) | segment shows "Parallel Line · Memory", "Perpendicular Line · Memory" / "Perpendicular · Memoria" only in `MEMORY_ENABLED`; hidden otherwise | PASS |

## 4. Freeze

All planned changes are GeoCeDG-owned Desktop code, profile texts, tests,
registration and documentation; `BOUNDED_PHASE` is frozen for F2-R1 with phase
`PRE-G9B-R6-PLUS-F2-R1` (`compile.desktop.semantic`,
`junit.desktop.pre-g9b-r6-plus-f2-r1.semantic`, `workspace-profile.product`). No
`INTEGRATION` or `FINAL`.

```text
FR-2                = PASS (REVISED O1 CONTRACT)
VERIFICATION_CLASS  = BOUNDED_PHASE (frozen)
IMPLEMENTATION      = RESUMED (local candidate only)
PUBLICATION         = NOT AUTHORIZED
selfApproved        = false
```

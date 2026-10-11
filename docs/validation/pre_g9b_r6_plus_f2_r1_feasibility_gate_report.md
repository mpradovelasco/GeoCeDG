# PRE-G9B-R6-plus-F2-R1 — feasibility gate report (BLOCKED at FR-2)

```text
TASK                  = PRE-G9B-R6-plus-F2-R1 (conditional bounded corrective implementation)
AUTHORIZATION         = F2-R1 decision record v1.2 §6 (authorization commit
                        de5c267767693d1051d7089cf4d19cc7d4bfe7a4, tree
                        31e20c2607cc821e0dc968f829f2f5ca5a4406e2)
BASE                  = e13bfbd231700019a8f81c115b22a5bb8c44f7bb
                        tree 2e8ee23addebcb05c4505c30e1fa593c8a3d4fa7
ENTRY_GATE            = PASS
FEASIBILITY GATE      = FR-1 PASS (probe); FR-2 FAIL; FR-3 PASS; FR-4 PASS
VERIFICATION_CLASS    = NOT FROZEN (the gate did not pass)
TECHNICAL CANDIDATE   = NONE
WORK IN PROGRESS      = local branch wip/pre-g9b-r6-plus-f2-r1-fr2-blocked,
                        commit 654f7021a8fcf207ace595033ed71d618cb0ba34,
                        tree b0137fabf545d79824394b24a4ab68b16d1a9d36 — NOT A CANDIDATE
F2 ORIGINAL CANDIDATE = FROZEN — HISTORICAL EVIDENCE
AUTHOR DISPOSITION    = REQUIRED (FR-2)
PUBLICATION           = NOT AUTHORIZED
selfApproved          = false
```

The authorization requires the work to stop when a feasibility condition fails
and to request author disposition rather than implement an alternative
mechanism or reduce the approved contract. This report records that stop.
Decisions: [F2-R1 decision record](pre_g9b_r6_plus_f2_r1_author_decisions_record.md)
v1.2; design: [dual-activation design](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md);
contract: [corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md).
Mirror: [`pre-g9b-r6-plus-f2-r1-feasibility-gate.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-feasibility-gate.json).

## 1. Entry gate

Linear chain `6d9bafb0` → `bdb9b6a9` → `6af85ee3` → `b7faad3a` → `d630b8dd` →
`e13bfbd2`, every tree verified; worktree clean; `main`, `origin/main` and the
live remote `main` at `6d9bafb0`; F2 `PHASE`/`STATIC`/`INFRA_UNIT` and the three
F2-R1 `STATIC` receipts `ACCEPTED / COMPLETE` on their exact commits; no product
change after `6af85ee3`. Branch `phase/pre-g9b-r6-plus-f2-r1` from `e13bfbd2`;
first tracked change `de5c2677` (authorization, prompt authorized, roadmap 5.20).

## 2. Gate probe

Scratch JUnit `F2R1GateProbeTest` (four methods, exit 0) through a Gradle init
script; never tracked; `compileTestJava` rerun without it; probe XML and
compile-stash copies removed.

| Output | SHA-256 |
|---|---|
| `f2r1g.fr1.txt` | `0eaa745bf8e3c31a550852f9276f02e38858d62538cee103caa32328d6da0c5f` |
| `f2r1g.fr2.txt` | `52cd65d3d8bdfc0c36c788dbf2400e299feba9f25f6a6dc623f8078435b143d5` |
| `f2r1g.fr3.txt` | `2253a4a1b2e92eb842520d4bc732b568db1ddc2c1b04259edcef8eee6bf2b492` |
| `f2r1g.fr4.txt` | `c93d43213d7214700e7139bb8fc9096d72019e2c097674820c5e29c18c8427c1` |
| probe source | `f723d01cd253f142a3bc7f040fc0c34c7e35e6d97d4dfecfe595db42ee98967e` |
| init script | `23893142bfc936e9b45acc1843b053f6032062c03f126473bfc783001947bebd` |

## 3. Findings

### FR-1 — language change: PASS (probe; not yet implemented)

- Routes (source): `AppD.setLanguage(Locale)` (`desktop/main/AppD.java:2171-2195`)
  calls `setMoveMode()` (`:2179`), then `setLocale`, `updateConstructionLanguage`,
  `setUnsaved()`, `setLabels()`, `setOrientation()`; `AppD.setLanguage(String)`
  (`:2164-2166`) only calls `setLocale`; `AppGeoCeDG.setLocale` restricts to EN/ES
  and refreshes the status bar; the product route is the language action
  (`GeoCeDGActionRegistry.chooseLanguage`), which calls `setLanguage(Locale)` and
  restores the saved flag of an unmodified document.
- Probe: with Parallel active and a remembered line, the inherited
  `setLanguage(Locale es)` switches to Move (mode 3 → 0), keeps the reference
  current and the undo history (2) and marks the document unsaved (inherited);
  an emulated restoration `setMode(PARALLEL)` returns to Parallel without changing
  the undo history or the saved flag, and the next point click reuses the memory;
  `setLanguage(String)` and `setLocale` change neither the mode nor the memory.
- Resolution: an `AppGeoCeDG.setLanguage(Locale)` override running the inherited
  call as host synchronization and restoring the captured tool and behaviour
  unless a genuine activation intervened — no inherited change.

### FR-2 — scripting activation: **FAIL**

- Route (source): `GgbAPI.setMode(int)` (`common/plugin/GgbAPI.java:918-921`) →
  `app.setMode(mode)`; the Desktop API object is created only in
  `AppD.getGgbApi()` (`desktop/main/AppD.java:4146-4152`, private field `ggbapi`
  used only there; public, non-final getter; `GgbAPID` and its constructor public).
- Evaluated seam (as instructed): a GeoCeDG subclass of `GgbAPID` returned by an
  `AppGeoCeDG.getGgbApi()` override, marking `setMode` as a genuine `NORMAL`
  request. The probe confirmed a single stable instance and correct routing of
  `setMode`, but did **not** exercise JavaScript initialization; that was a gap
  in the gate probe.
- Runtime failure (implementation test, F2 class `rebuildsAndDocumentBoundariesClearBothSlots`):
  `org.mozilla.javascript.EvaluatorException: Access to Java class
  "org.geocedg.desktop.GeoCeDGScriptingApi" is prohibited.` Every document load
  (clearing `setXML`, `loadXML`) initializes the JavaScript global scope through
  `CallJavaScript.evalGlobalScript` → `GeoGebraGlobal.initStandardObjects`, which
  exposes `app.getGgbApi()`; the sandbox
  `CallJavaScript.SandboxClassShutter.visibleToScripts` (`desktop/plugin/CallJavaScript.java:116-124`)
  admits exactly `GgbAPID` and the fixed set `ScriptUtil.ALLOWED`
  (`shared/common-jre/.../plugin/ScriptUtil.java:58-67`). A product subclass can
  therefore not be the effective API instance without changing an inherited
  Desktop class or a shared class — both forbidden.
- Isolation: with only the `getGgbApi` override removed, the adapted F2 suite
  passes 19 of 19; the failure is specific to the API replacement.

### FR-3 — cancellation of partial constructions: PASS

Probe `fr3`: for a selected reference, a selected existing point and a
provisional empty-space point after a completed construction, the inherited
protected `initNewMode(mode)` discarded only the provisional point (8 → 7
objects), kept every completed construction as the identical instances in the
same order, left the undo history (2), undo/redo availability and the saved flag
unchanged, kept the mode, and left the remembered reference current (exact
membership revalidated); a later undo invalidated it through the F2 lifecycle
(no stale reuse). The inherited tool-change cancellation gives the same document
effects, with the mode moved to Move.

### FR-4 — status bar: PASS

The profile schema accepts free `localized_text` keys with `en` and `es`
(`geocedg/specs/ui/application-profile.schema.json:238-258`; precedents
`IsoA.Status.*`, `Units.Status.*`); the probe laid out an added segment in the
single-row bar at 640, 800, 1024, 1280 and 1920 px: same row, inside the bar; from
800 px the full "Perpendicular Line · Memory" fits; at 640 px the bar's preferred
width (678 px) exceeds the window and the last segment is truncated (inherited
`SingleRowLayout` behaviour).

## 4. Options for author disposition (not implemented)

| Option | Mechanism | Contract | Scope |
|---|---|---|---|
| **O1** — signature-based origin | override the single-argument `App3D.setMode(int)` in `AppGeoCeDG`: an unmarked single-argument activation is a genuine indirect request (`NORMAL`), the two-argument host form (`setMode(int, ModeSetter)`: `setMoveMode`, dock-panel toolbar switching, CAS) stays host synchronization. Evidence: every caller of the single-argument form with a variable mode is a user route (toolbar button, registry, `GgbAPI.setMode`, user tools, tool-creation dialogs); the remaining single-argument callers pass fixed non-Parallel/Perpendicular modes | DQ-F2R1-4 met (scripts `NORMAL`, host sync unchanged) | GeoCeDG-owned; no inherited change; relies on the overload convention of the inherited code |
| O2 — scripting as host synchronization | keep the inherited API; a scripted same-mode activation leaves the behaviour unchanged (a scripted mode change still departs → `NORMAL`) | narrows DQ-F2R1-4 for scripting (same-mode only) | GeoCeDG-owned |
| O3 — admit a GeoCeDG API class to the script sandbox | change `CallJavaScript.SandboxClassShutter` or `ScriptUtil.ALLOWED` | DQ-F2R1-4 met | inherited/shared change → reclassification above `BOUNDED_PHASE` |

Recommendation (agent proposal, not a decision): **O1**, because it meets the
approved contract without inherited changes; its residual risk is that a future
inherited caller of the single-argument form would be treated as a user request.

## 5. Work in progress (not a candidate)

The partial implementation is preserved, unverified, on the local branch
`wip/pre-g9b-r6-plus-f2-r1-fr2-blocked` (`654f7021`): `GeoCeDGToolActivation`,
the `AppGeoCeDG` `setMode` and `setLanguage` overrides, the native-toolbar
listener, registry markers, the controller `NORMAL` gate and `initNewMode`
cancellation, the status-bar segment, the profile texts and the adapted F2 tests;
the failing `getGgbApi` override is removed there and `GeoCeDGScriptingApi` is
kept unused as evidence. Patch copy: `a7ed141345b66d8c2ac1ef31bab65d179707564802538bfc0c28c609f7d98911`
(scratchpad). The phase branch carries only documentation.

## 6. State

```text
F2-R1 IMPLEMENTATION  = STOPPED AT THE FEASIBILITY GATE (FR-2) — AUTHOR DISPOSITION REQUIRED
VERIFICATION_CLASS    = NOT FROZEN
PHASE / INFRA_UNIT    = NOT RUN (no candidate)
AUTHOR_SMOKE          = NOT APPLICABLE
PUBLICATION           = NOT AUTHORIZED
selfApproved          = false
```

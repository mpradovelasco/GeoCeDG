# PRE-G9B-R6-plus-F2-R1 author-decision and design-reconciliation record

```text
ARTIFACT_KIND             = AUTHOR-DECISION RECORD AND DESIGN RECONCILIATION
RECORD VERSION            = 1.1
VERSION HISTORY           = 1.0 Stage 2 reconciliation (DQ-F2R1-1 direction, DQ-F2R1-6
                                scope; DQ-F2R1-2..5 proposals)
                            1.1 PRE-G9B-R6-plus-F2-R1-DESIGN-CLOSEOUT: author dispositions
                                of DQ-F2R1-1..6 with refinements; design AUTHOR APPROVED;
                                feasibility items FR-1..FR-4 (§5); §1-§4 kept as the
                                v1.0 state
STATE AT VERSION 1.1      = F2-R1 DESIGN = DESIGN — AUTHOR APPROVED
                            F2-R1 PROMPT = PREPARED — NOT AUTHORIZED
                            F2-R1 IMPLEMENTATION = NOT AUTHORIZED
                            F2 ORIGINAL CANDIDATE = FROZEN — NOT AUTHOR APPROVED
                            PUBLICATION = NOT AUTHORIZED
                            selfApproved = false
CLOSEOUT INSTRUCTION      = author instruction "PRE-G9B-R6-plus-F2-R1-DESIGN-CLOSEOUT — F2-R1
                            Author Decisions and Design Closeout" (AUTHOR AUTHORIZED —
                            AUTHOR-DECISION RECORDING AND DOCUMENTARY RECONCILIATION ONLY),
                            on the Stage 2 candidate d630b8dd3ace0cafd28f9896a8964778dd346e2e
                            tree 3ed8c9689f11a7a877eaae08c1b97f7fbaf54c12 (STATIC
                            verification-9d6384e236fc428db3883b01a0d23a19, ACCEPTED /
                            COMPLETE 3/3), kept as the parent
TASK                      = PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION (Stage 2)
AUTHOR_DECISION_SOURCE    = author instruction "PRE-G9B-R6-plus-F2-R1-DESIGN-RECONCILIATION —
                            Stage 2 — Dual-Mode Activation, Author Decisions and Corrective
                            Design" (AUTHOR AUTHORIZED — DESIGN RECONCILIATION AND FOCUSED
                            CHARACTERIZATION ONLY)
PUBLISHED MAIN            = 6d9bafb04235a71c8dfadef9631cd6057964b1f9
                            tree 6bc65715cd37f5872b804eea7949d2f9fea96092
F2 ORIGINAL CANDIDATE     = 6af85ee3dc2cbb82ab95aeb070e765d2fbdaa873
                            tree 3d412a801024411ebfcbaf8469691ec00f7c4fee
                            FROZEN — NOT AUTHOR APPROVED — NOT PUBLISHED
                            (PHASE verification-ac975f8c79ec4d58ba018ca4eb2a4cbd, STATIC
                            verification-49e939c13b71429bbcf246ca08480c3f, INFRA_UNIT
                            verification-2a4c7afa3282477ca3412c0a8de49f33, all ACCEPTED /
                            COMPLETE; historical evidence for F2-R1)
STAGE 1 CANDIDATE         = b7faad3af2cbce0c2254cbdc7366c96a3df5ad48
                            tree 980207638c8bd045621879b2a28f407104b08bc4
                            (STATIC verification-5d571ee293c641b7b9259a33885e8e7e,
                            ACCEPTED / COMPLETE 3/3); kept as the parent
STAGE 2 CANDIDATE         = the commit that contains this record (direct child of
                            b7faad3a); identity and STATIC reported outside this record
F2-R1 DESIGN              = CANDIDATE PENDING AUTHOR REVIEW
CANONICAL CORRECTIVE PROMPT = PREPARED — NOT AUTHORIZED
F2-R1 IMPLEMENTATION      = NOT AUTHORIZED
PUBLICATION               = NOT AUTHORIZED
selfApproved              = false
```

The entries from `PUBLISHED MAIN` to `selfApproved` in the block above and
§1–§4 below record the state at version 1.0; `STATE AT VERSION 1.1` and §5
supersede them.

This record separates what the author **decided** in the Stage 2 instruction,
what the author **proposed** there for confirmation, and what this
reconciliation **established** or **recommends**. Nothing here approves the
F2-R1 design, the F2 candidate or any implementation. Machine-readable mirror:
[`pre-g9b-r6-plus-f2-r1-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-r1-author-decisions.json).
Design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md);
tests and smoke: [test matrix and author-smoke plan](pre_g9b_r6_plus_f2_r1_test_matrix_and_smoke_plan.md);
contract: [canonical corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md).
Stage 1 artifacts ([characterization report](pre_g9b_r6_plus_f2_r1_activation_characterization_report.md),
[alternatives candidate](../architecture/pre_g9b_r6_plus_f2_r1_activation_alternatives_candidate.md),
[decision preparation record](pre_g9b_r6_plus_f2_r1_author_decision_preparation_record.md))
are frozen evidence.

## Classification vocabulary

| Tag | Meaning |
|---|---|
| **AUTHOR DECISION** | decided in the Stage 2 instruction |
| **AUTHOR PROPOSAL** | a rule the instruction proposes for confirmation; pending author confirmation |
| **CHARACTERIZATION EVIDENCE** | read or probed (Stage 1 `R*`, Stage 2 §3) |
| **RECONCILED DESIGN** | resolution by this reconciliation; reviewable; not an author decision |

## 1. Decisions and proposals

### `DQ-F2R1-1` — activation mechanism

- **AUTHOR DECISION:** Candidate A — Ctrl at tool activation — is the preferred
  design direction, conditional on the activation and lifecycle gates; Candidate
  B1 (explicit session toggle) remains the fallback and needs a separate author
  decision. On Windows/Linux Ctrl + toolbar activation requests `MEMORY_ENABLED`;
  a normal toolbar activation requests `NORMAL`; on macOS Command is the intended
  equivalent but support must not be claimed without verification. The modifier
  is captured from the originating activation event, not from global keyboard
  state; the behaviour is latched on activation; later Ctrl presses or releases
  change nothing; no new tool id or command.
- CHARACTERIZATION EVIDENCE: Stage 1 `R3`, `R4`; Stage 2 `F01` (real popup).
- RECONCILED DESIGN: toolbar listener in `ProfileToolbar.addNativeGroup` reads
  the `ActionEvent` modifier and records a request bound to the current AWT
  event; design §2–§3. Gate status: **met** for toolbar, reclick, real flyout and
  keyboard routes; macOS and Linux **NOT_ESTABLISHED**.

### `DQ-F2R1-2` — reactivating an active tool

- **AUTHOR PROPOSAL:** every qualifying toolbar activation selects `NORMAL` or
  `MEMORY_ENABLED`; the latest wins; repeating without a behaviour change keeps
  the inherited partial selection; changing the behaviour cancels the incomplete
  gesture before the next construction; cancelling never changes the remembered
  reference and creates no object or undo point.
- CHARACTERIZATION EVIDENCE: reclicks reach `App.setMode` but not the controller
  (Stage 1 `R5`); Stage 2 cancellation probe (§3 `C`): the inherited protected
  `initNewMode(mode)`, called from the GeoCeDG controller subclass, clears the
  selection, removes an unfinished free point, stores no undo point, keeps the
  mode and the memory, and the next click behaves normally; `clearSelections()`
  alone leaves the unfinished free point behind.
- RECONCILED DESIGN: implementable as proposed through `AppGeoCeDG.setMode` →
  `GCEC.applyActivationBehaviour` → `initNewMode(mode)` only when the behaviour
  changes. No synthetic tool change and no inherited internal mutation. Design
  §2, §4.

### `DQ-F2R1-3` — duration and reset

- **AUTHOR PROPOSAL:** the behaviour persists until the user leaves the tool or
  reactivates it; leaving resets to `NORMAL`; returning without Ctrl is `NORMAL`,
  with Ctrl `MEMORY_ENABLED`; slots are independent of the behaviour; Escape
  cancels the gesture and returns to `NORMAL` without clearing valid memories;
  New, Open, undo/redo and rebuilds invalidate per F2 regardless of behaviour.
- CHARACTERIZATION EVIDENCE: Escape, language change and New go through
  `setMoveMode` (Stage 2 `O05`, `O11`, `O12`); dock focus, Graphics View 2
  creation, undo, redo and clearing `setXML` call no `setMode` (`O06`–`O10`).
- RECONCILED DESIGN: the coordinator resets both tools to `NORMAL` on any
  activation of another mode; consistent with the proposal. Design §4.

### `DQ-F2R1-4` — activation routes

- **AUTHOR PROPOSAL:** activations without a proven toolbar modifier default to
  `NORMAL`, also when the tool is already active.
- CHARACTERIZATION EVIDENCE: menu/registry and API same-mode activations reach
  `App.setMode` (Stage 2 `O03`, `O04`); Ctrl+Space fires no action (`K01`).
- RECONCILED DESIGN: route contract of design §3 — direct toolbar, reclick
  (observation), real flyout, keyboard: **CONFIRMED**; menu/registry, scripting,
  user tools: **CONDITIONALLY SUPPORTED** (coordinator default, to be tested);
  undocked toolbars, window focus, modal dialogs, macOS, Linux: **NOT_ESTABLISHED**
  (default `NORMAL` by construction; Ctrl support not claimed). No route needs a
  change outside GeoCeDG-owned code.

### `DQ-F2R1-5` — user-visible indication

- **AUTHOR PROPOSAL:** a discreet, temporary status-bar indicator (e.g.
  "Paralela · Memoria", "Perpendicular · Memoria") reflecting the latched
  behaviour, absent in `NORMAL`, updated immediately, removed when leaving the
  tool, never implying a valid reference, no document state, no new action or
  icon.
- CHARACTERIZATION EVIDENCE: `GeoCeDGStatusBar` has ordered, id-keyed segments
  with hidden-by-default precedents (`sheet-coherence`, `paste-notice`) and
  profile-localized texts (`app.layerText`).
- RECONCILED DESIGN: one hidden segment plus separator, localized text with the
  tool name, synchronous EDT refresh; two to four profile text keys; no catalog,
  action or icon. Proportionate. Design §5.

### `DQ-F2R1-6` — architecture and verification

- **AUTHOR DECISION:** the correction belongs in GeoCeDG Desktop (toolbar
  coordination, controller behaviour, memory gate, transient status indication,
  tests, documentation); no change to `source/shared/**`, inherited toolbar or
  controller classes, commands, algorithms, DAG, identity, XML, Classic, Graphics
  View 2, Angle with Given Size or the working-layer entry; a separate registered
  corrective `PHASE` if implementation is authorized; the F2 receipt is not
  inherited.
- RECONCILED DESIGN (recommendation): `BOUNDED_PHASE`, phase
  `PRE-G9B-R6-PLUS-F2-R1` with `compile.desktop.semantic`,
  `junit.desktop.pre-g9b-r6-plus-f2-r1.semantic` and `workspace-profile.product`.
  Design §7.

## 2. Original F2 decisions: amended and in force

| Original | Status under F2-R1 |
|---|---|
| `DQ-F2-1` memory across tool changes | **amended**: slots still survive tool changes; reuse requires `MEMORY_ENABLED`; the *behaviour* resets to `NORMAL` when leaving the tool |
| `DQ-F2-2` independent slots, replacement after success | **amended**: replacement only by a certified construction in `MEMORY_ENABLED`; a `NORMAL` construction never writes |
| `DQ-F2-3` first-use behaviour | **amended**: point-first or empty-space reuse only in `MEMORY_ENABLED`; `NORMAL` = inherited tool |
| `DQ-F2-6` preview, cancellation, undo; no indicator in v1 | **amended** for the indicator (status-bar segment, `DQ-F2R1-5`); preview, cancellation and undo rules in force, plus the behaviour-change cancellation of `DQ-F2R1-2` |
| `DQ-F2-10` architecture and class | **refined**: adds GeoCeDG-owned toolbar coordination and `AppGeoCeDG.setMode` coordination; still Desktop-only, `BOUNDED_PHASE` |
| `DQ-F2-4` admissible references | in force |
| `DQ-F2-5` identity and lifecycle | in force (applies in both behaviours) |
| `DQ-F2-7`, `DQ-F2-8` Angle with Given Size | in force, unchanged |
| `DQ-F2-9` working-layer entry | in force (excluded) |

The original F2 record, design, specification (`PROPOSED`), candidate and receipts
are not edited.

## 3. Stage 2 probe

Scratch JUnit class `F2R1DesignProbeTest` (three methods) through a Gradle init
script; never tracked; `compileTestJava` rerun without the init script; probe
XML and compile-stash copies removed.

| Output | Content | SHA-256 |
|---|---|---|
| `f2r1d.cancel.txt` | `C`: pending point, pending reference, pending empty-space point × {no action, `clearSelections`, `initNewMode`} | `5f7168d8813e70796f420a65d78f59698f27e461ed3bc69755d1c83184af6303` |
| `f2r1d.origins.txt` | `O01`–`O12`: stack origin of every `SET_MODE` | `3492a05c88ffe85837373d579ba8ed4c72e123578c8d780f7ac8e0bc540fc990` |
| `f2r1d.flyout.txt` | `F01` real popup item with/without Ctrl; `K01` Space/Ctrl+Space on the focused button | `21100f92c28f3034664117e830198f6219bb9030ccddd34cd17ec9ff925cfbe7` |
| probe source | | `4336ca16c397c5a6ca13eb052874b6ccdcf71725c89c3ad49ff1206ebb1d7851` |
| init script | | `69bd0869d6a4e53ebfbec080a9a542054d3a01f14f21ac5c3acaa3ccde3c4dc0` |

Key results:

- `C` (no action): a pending point, reference or empty-space point survives a
  same-mode reactivation; the next click completes the inherited gesture.
  `clearSelections`: selection cleared, the unfinished empty-space point stays
  (orphan). `initNewMode(mode)`: selection cleared, unfinished point removed
  (7 → 6 objects), no undo store, memory and mode unchanged, next click selects
  the reference as a fresh gesture.
- `O`: toolbar, registry and `GgbAPI.setMode` same-mode activations all reach
  `App.setMode(int, ModeSetter)` (`App3D.setMode:141` → `App.setMode:1661` →
  `App.setMode:2392` → `GuiManager.setMode:527`); Escape, language change and New
  reach it through `setMoveMode`; dock focus, Graphics View 2 creation, undo,
  redo and clearing `setXML` do not call it.
- `F01`: in a shown window the real flyout popup item, receiving a posted mouse
  press/release, makes the toolbar button fire with the item's `MouseEvent` as
  current event; Ctrl propagates (`ctrl=true`) and the mode becomes
  Perpendicular. `K01`: Space on the focused button activates without modifier;
  Ctrl+Space fires no action.
- Earlier run of the cancellation method: its setup clicked the same point twice
  quickly, opening the inherited Redefine dialog (a real window, rejected by the
  harness); the setup was corrected and the method rerun; the recorded output is
  the corrected run.

## 4. State

```text
AUTHOR_DECISION   = DQ-F2R1-1 direction (Candidate A, conditional) and DQ-F2R1-6 scope
                    recorded from the Stage 2 instruction
AUTHOR_PROPOSALS  = DQ-F2R1-2, -3, -4, -5 reconciled; PENDING AUTHOR CONFIRMATION
DESIGN            = CANDIDATE PENDING AUTHOR REVIEW
IMPLEMENTATION    = NOT AUTHORIZED
PUBLICATION       = NOT AUTHORIZED
selfApproved      = false
```

## 5. Author dispositions (v1.1, `PRE-G9B-R6-plus-F2-R1-DESIGN-CLOSEOUT`)

The author approves the reconciled dual-mode design with the refinements below.
The [reconciled design](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md)
is `DESIGN — AUTHOR APPROVED`; the [canonical corrective prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-r1-dual-activation.prompt.md)
stays `PREPARED — NOT AUTHORIZED`; implementation and publication are not
authorized.

### `DQ-F2R1-1` — mechanism (AUTHOR DECISION)

Ctrl at toolbar activation **on Windows** is the preferred mechanism; the
modifier is taken from the specific activation event, never from a global Ctrl
flag; normal activation selects `NORMAL`, Ctrl activation `MEMORY_ENABLED`;
releasing or pressing Ctrl during construction does not modify the latched
behaviour. An explicit session toggle is a separately authorized fallback, not
part of the approved design.

### `DQ-F2R1-2` — reactivation (AUTHOR DECISION)

Every qualifying toolbar activation establishes the requested behaviour,
including reactivation of the selected tool; the latest wins; an unchanged
behaviour preserves the inherited partial selection; a changed behaviour cancels
the incomplete gesture with the characterized protected `initNewMode`, subject
to verification that only provisional geometry of that gesture is discarded,
completed constructions are intact, no undo or redo step is introduced, the
saved/unsaved state stays coherent and remembered references are preserved
unless invalidated by the F2 lifecycle (`FR-3`). A behaviour change is never a
geometric construction.

### `DQ-F2R1-3` — lifecycle (AUTHOR DECISION, refined)

The behaviour persists until tool departure or explicit reactivation; leaving
Parallel or Perpendicular resets it to `NORMAL`; ordinary tool changes and
Escape do not erase valid remembered references; **a language change by itself
must not reset the active behaviour or the remembered references**; New/Open and
rebuilds keep the F2 invalidation rules; undo/redo never restore a stale
reference.

- Difference from v1.0: v1.0 treated a language change as a departure (the
  inherited `AppD.setLanguage(Locale)` calls `setMoveMode`, Stage 2 `O05`). The
  approved design (§3) preserves and restores the active tool and behaviour
  through a GeoCeDG-owned `AppGeoCeDG.setLanguage` override; feasibility `FR-1`.

### `DQ-F2R1-4` — activation origins (AUTHOR DECISION, refined)

Direct toolbar activations, reactivations and the native Parallel/Perpendicular
flyout use the originating event; indirect activations without a validated
modifier origin default to `NORMAL`, including same-mode activations; **internal
mode resynchronization must not silently change the behaviour, and genuine
activation requests are distinguished from host UI synchronization**; keyboard
Space without Ctrl may activate `NORMAL`; Ctrl+Space is not claimed; macOS and
Linux remain `NOT_ESTABLISHED`.

- Difference from v1.0: v1.0 resolved every unmarked same-mode `setMode` to
  `NORMAL`. The approved design (§2) marks genuine requests at their GeoCeDG-owned
  origins (toolbar listener, action registry, user tools) and treats an
  unmarked same-mode `setMode` as host synchronization (unchanged). Scripting
  activations are genuine only if a GeoCeDG-owned API seam can mark them
  (`FR-2`).

### `DQ-F2R1-5` — presentation (AUTHOR DECISION)

A discreet, localized GeoCeDG-owned status-bar indication while
`MEMORY_ENABLED` is active — "Parallel Line · Memory" / "Paralela · Memoria",
"Perpendicular Line · Memory" / "Perpendicular · Memoria"; no toolbar icon or
action; it reflects the activation state, not reference usability; hidden in
`NORMAL` and for other tools; compatible with the single-row status bar,
localization and lifecycle (`FR-4`).

### `DQ-F2R1-6` — architecture and verification (AUTHOR DECISION)

GeoCeDG-owned Desktop classes and resources only; proposed class
`BOUNDED_PHASE`, conditioned on the implementation entry gate; corrective phase
`PRE-G9B-R6-PLUS-F2-R1` with `compile.desktop.semantic`,
`junit.desktop.pre-g9b-r6-plus-f2-r1.semantic` and `workspace-profile.product`.
No inherited toolbar or controller modification, shared kernel change, new mode
id, XML change or geometric command.

### Normative relationship to F2

F2-R1 refines the interaction conditions of `DQ-F2-1`, `DQ-F2-2`, `DQ-F2-3`,
`DQ-F2-6` and `DQ-F2-10`. Geometric admissibility, reference identity,
currentness, suspension, lifecycle and the successful-construction certificate
are unchanged; Angle with Given Size is unchanged. The original F2 candidate
`6af85ee3` is retained as historical technical evidence and is **not** promoted
to an author-approved implementation; the F2 specification stays `PROPOSED`.

### Feasibility items for the implementation entry gate (unresolved risks)

| Id | Item | Risk |
|---|---|---|
| `FR-1` | restore the active Parallel/Perpendicular tool and behaviour around the inherited language switch (`AppD.setLanguage` → `setMoveMode`) in an `AppGeoCeDG.setLanguage` override | interaction with the GUI rebuild, Input Help and saved state not yet exercised; if infeasible, the decision cannot be met without author disposition |
| `FR-2` | mark scripting `setMode` through a GeoCeDG-owned seam (the scripting API object is created by the inherited `AppD.getGgbApi()` as `GgbAPID`) | if no safe seam exists, scripting same-mode activations stay host synchronization instead of `NORMAL` |
| `FR-3` | `initNewMode` cancellation: provisional-only discard, completed constructions intact, no undo/redo step, coherent saved state | the probe established the first and third points; saved-state coherence is not yet verified |
| `FR-4` | status texts accepted by the profile validator; segment laid out in the single-row bar | profile-validation behaviour for free text keys not yet run |

Also not established (no claim made): macOS and Linux modifiers, undocked
toolbars, window focus and modal dialogs.

```text
F2-R1 DESIGN          = DESIGN — AUTHOR APPROVED
F2-R1 PROMPT          = PREPARED — NOT AUTHORIZED
F2-R1 IMPLEMENTATION  = NOT AUTHORIZED
F2 ORIGINAL CANDIDATE = FROZEN — NOT AUTHOR APPROVED
PUBLICATION           = NOT AUTHORIZED
selfApproved          = false
```

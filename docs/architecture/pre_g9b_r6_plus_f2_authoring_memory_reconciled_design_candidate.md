# PRE-G9B-R6-plus-F2 — authoring-memory refinements: reconciled design

- Status: **DESIGN — AUTHOR APPROVED** (author decisions of 2026-10-11 on
  `DQ-F2-1` to `DQ-F2-10`, recorded in the
  [F2 author-decision record](../validation/pre_g9b_r6_plus_f2_author_decisions_record.md)
  v1.0, which prevails over this design wherever they differ)
- Produced by: `PRE-G9B-R6-plus-F2-PREP` (characterization and preparation),
  reconciled by `PRE-G9B-R6-plus-F2-PREP-CLOSEOUT`
- Base: published `main` `24985f14c565b9ba6282ec80fa0cb7be20a2e459`, tree
  `1c683ebb3e303b7692915feae3417cf51e4cce22`
- Evidence: the frozen
  [F2 preparation characterization report](../validation/pre_g9b_r6_plus_f2_preparation_characterization_report.md)
  (`K0`–`K16`; corrections `ER-F2-1` to `ER-F2-9` in `K14`, `ER-F2-10` to
  `ER-F2-12` in the decision record §5); the `P0`
  [F2 design candidate](pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md)
  stays unchanged as historical evidence
- Contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-authoring-memory.prompt.md)
  (`PREPARED — NOT AUTHORIZED`)
- The approval covers this design only. **F2 implementation is not
  authorized**; publication is not authorized. `selfApproved = false`.
  Statements tagged *implementation design* are resolutions inside the decided
  scope, reviewable by the author; open items `OTQ-F2-1` to `OTQ-F2-3` are
  listed in the decision record §4 (none blocking).

Path abbreviations follow the report: **EC** = inherited
`EuclidianController`; **GCEC** = `GeoCeDGEuclidianController`.

## 1. Scope

| Target | Content | Decisions |
|---|---|---|
| A | Parallel and Perpendicular keep independent session-only remembered references in GeoCeDG Graphics View 1 | `DQ-F2-1` to `DQ-F2-6` |
| B | Angle with Given Size created through GeoCeDG Graphics View 1: the angle is hidden in the graphical views by ordinary visibility; the rotated point stays visible | `DQ-F2-7`, `DQ-F2-8` |
| excluded | `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`: separate pending enhancement, not part of F2 (`DQ-F2-9`) | `DQ-F2-9` |
| all | Desktop-only orchestration; GeoCeDG-owned controller companion; `BOUNDED_PHASE` | `DQ-F2-10` |

## 2. Invariants

1. Geometry algorithms unchanged: the created objects are the ones the
   unchanged upstream `parallel`, `orthogonal` and `AlgoDispatcher.angle`
   create (`AlgoLinePointLine`, `AlgoLinePointVector`, `AlgoOrthoLinePointLine`,
   `AlgoOrthoLinePointVector`, `AlgoRotatePoint`, `AlgoAnglePoints`).
2. Kernel command semantics unchanged: `Line(P, l)`, `PerpendicularLine`,
   `Angle(B, A, α)`, GGBScript, API and Algebra input create exactly what they
   create today, visible as today.
3. The dependency graph changes only by the objects a completed tool gesture
   creates.
4. Object identity unchanged: no durable ID, document association, kernel
   identity infrastructure or label-based resolution.
5. No new XML element or attribute.
6. The memory is `SESSION` state only: never serialized, never in undo
   snapshots, cleared by undo/redo rebuilds, New, Open and document
   replacement.
7. Remembering, replacing, suspending or clearing a reference never stores an
   undo point; a completed creation stores exactly one, as today.
8. The angle's visibility is the ordinary object property, persisted as today.
9. Graphics View 2's tool controller and Classic are not modified.
10. No inherited class or `source/shared/**` file is modified; a need to modify
    one stops the work for author review (`DQ-F2-10`).

## 3. Memory model (target A)

### 3.1 Owner and slots (`DQ-F2-1`, `DQ-F2-2`)

A GeoCeDG-owned Desktop helper (proposed `GeoCeDGLineReferenceMemory`) owned
by the GV1 GeoCeDG controller, one per window, with two independent slots,
`PARALLEL` and `PERPENDICULAR`. Each slot holds one exact Java reference or
nothing. The memory is **not** reset in the controller's `setMode`: it
survives ordinary tool changes, including Escape (`setMoveMode`), within the
same document session.

### 3.2 Admissibility (`DQ-F2-4`)

A reference is admissible for a slot iff it is:

- a 2D (non-3D) `GeoLine` — line, segment, ray or polygon edge;
- a 2D `GeoVector`;
- a `GeoFunction` whose kernel linear interpretation is defined:
  `Lineable2D.getX()` of `GeoFunction` (`GeoFunction.java:2895-2913`) is finite
  only for degree ≤ 1, and is the route `AlgoLinePointLine` consumes.

Parallel 3D vectors and every 3D input keep their inherited tool behavior and
are never remembered. Non-linear functions, conics, polygons and points are
never remembered. Hit priority is the inherited one.

### 3.3 Currentness and usability (`DQ-F2-5`)

For a slot value `ref`:

- **current** iff `ref` is an exact-instance member of
  `kernel.getConstruction().getGeoSetConstructionOrder()` (the
  `ExportAreaSession.isLive` and `GeoCeDGSimilarityTools` precedent). A label
  lookup may corroborate (`lookupLabel(ref.getLabelSimple()) == ref`) but never
  resolves another object; `isInConstructionList`,
  `Construction.isInConstructionList`, `isLabelSet` and `getConstruction()` are
  never currentness tests (report `K8` finding 1);
- **usable** iff current, admissible **now**, defined, and visible in GV1
  (implementation design: Euclidian-visible, visible in GV1, layer shown; exact
  predicate `OTQ-F2-3`).

A non-current slot is cleared. A current slot that is not usable is
**suspended**: kept, not reused, and usable again when it becomes defined,
admissible and visible.

### 3.4 State machine (per slot)

States: `EMPTY`, `HELD(r)` (usable), `SUSPENDED(r)` (current, not usable).
`HELD`/`SUSPENDED` is evaluated at every use; a non-current value goes to
`EMPTY` whenever detected.

| Event | From | To | Notes |
|---|---|---|---|
| successful committed construction by this tool, certificate of §4.3 met, `input[1] = r'` admissible | any | `HELD(r')` | the **only** write of a slot |
| point-first click (existing point or empty space), slot usable | `HELD(r)` | `HELD(r)` | memory injected at release; creation as upstream |
| point-first click, slot `EMPTY` or `SUSPENDED` | unchanged | unchanged | inherited flow: the point is selected and the tool waits for an explicit reference (report `K4` T03, T06) |
| reference-first click (selection only) | any | unchanged | replaced only when a construction then succeeds |
| unsuccessful construction (no certificate) | any | unchanged | e.g. non-linear function (T13) |
| preview (mouse move) | any | unchanged | preview never reads or writes memory |
| Escape, right click, tool change, switch to the other tool and back | any | unchanged | pending upstream selection discarded as today (T08) |
| the other tool constructs | any | unchanged | independent slots |
| recompute, drag of inputs | `HELD(r)` | `HELD(r)` | same instance (L01) |
| becomes undefined / recovers | `HELD` ↔ `SUSPENDED` | | same instance (L02, L03) |
| hidden or layer hidden / shown again | `HELD` ↔ `SUSPENDED` | | |
| function becomes non-linear / linear again | `HELD` ↔ `SUSPENDED` | | e.g. a slider coefficient |
| rename | `HELD(r)` | `HELD(r)` | same instance (L04) |
| delete of `r` (`REMOVE`), or delete and recreate of the label | any | `EMPTY` | L09, L10 |
| redefine of `r` replacing the object (`REDEFINE` with a new object) | any | `EMPTY` | L07, L08; a soft or identical redefinition that keeps the instance keeps the state |
| undo, redo, any rebuild of the construction (incl. redefinition with children) | any | `EMPTY` (both slots) | eager (`OTQ-F2-1`); L05, L06 |
| File → New, Open, full document replacement, clearing `setXML` | any | `EMPTY` (both slots) | eager; L11, L12 |
| macro-produced admissible reference | | as any reference | `AlgoMacro` output, stable identity (L13, L14) |
| window closed | | discarded | |

Eager clearing (rebuilds, document boundaries, `REMOVE`/`REDEFINE`) and the lazy
check at use are both required; the lazy check alone guarantees that no stale
instance is ever reused, whatever path made it stale (report `K9`).

## 4. Gestures (target A)

### 4.1 Release-time injection (`DQ-F2-3`)

In the GCEC `switchModeForProcessMode` override, for `MODE_PARALLEL` and
`MODE_ORTHOGONAL` only, immediately before the existing `super` call (GCEC:688):

1. `selectionPreview` true → no injection, no read-back (preview);
2. any of the tool's selection lists (points, lines, vectors, functions) not
   empty → no injection (a gesture is in progress; inherited rules);
3. hits contain a point — an existing point or the free point created by the
   press on empty space — **and** the tool's slot is usable → add the
   remembered reference to the matching selection list (line, vector or
   function), then call `super`; the unchanged `parallel`/`orthogonal`
   selects the point and constructs in the same call;
4. otherwise → `super` unchanged.

Nothing is injected at press, at mode entry or across events. The selection
list holds the injected reference only inside this one call.

### 4.2 Inherited gestures kept exactly

- **Point-on-path (issue #2610).** At press, with one line already selected
  by the user in this gesture and a path hit, the host creates a point on that
  path and the release constructs through it with the selected reference (T04).
  Because the selection is empty at press for a memory click, the gesture is
  never triggered by the memory and is unchanged when the user selects a line
  explicitly.
- **Same line clicked again** after the double-click interval: point on that
  line and the construction through it (T15), unchanged.
- **Double click** (two quick clicks on the same object): the inherited
  Redefine dialog opens (T15q), unchanged; no memory event.
- **Point on a line** (point and line both hit): the point wins, as upstream.
- **Two points** (T14): the second is ignored, unchanged.

### 4.3 Commit moment and success certificate (`DQ-F2-2`, `DQ-F2-4`, `DQ-F2-6`)

The slot is written only after `super.switchModeForProcessMode` returns, with
`!selectionPreview`, when all of:

- the inherited process step returned a created line (read back through
  `getJustCreatedGeos()` or a wrapped release callback, `OTQ-F2-2`);
- the line is an exact-instance member of the current construction order and
  is defined;
- its parent algorithm is exactly `AlgoLinePointLine` or
  `AlgoLinePointVector` (Parallel), `AlgoOrthoLinePointLine` or
  `AlgoOrthoLinePointVector` (Perpendicular);
- its `input[1]` is admissible (§3.2); that object — never an inferred one —
  becomes the slot value.

A label is never the certificate. Cancellation, a failed or partial gesture,
and preview never write.

### 4.4 Missing reference (`DQ-F2-3`)

With the slot `EMPTY` or `SUSPENDED`, the inherited flow runs: the point is
selected (or created on empty space) and the tool waits for an explicit
reference. No reference is invented; no geometry beyond the inherited flow is
created.

## 5. Cancellation, preview and undo (`DQ-F2-6`)

- No preview of the remembered reference and no memory indicator in v1.
- One completed creation = one undo point (the inherited release callback);
  an empty-space click that creates a point and, through the memory, the line
  stores one undo point for both, as the inherited empty-space flow (T06).
- Memory operations store no undo point; undo and redo clear the memory
  (§3.4).

## 6. Angle with Given Size hidden by default (target B)

### 6.1 Hook (`DQ-F2-7`)

GCEC overrides `newCompanion()` to return a GeoCeDG-owned subclass of
`EuclidianControllerFor3DCompanion` overriding only the public
`createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)`: it calls
`super`, and — when its controller is in `MODE_ANGLE_FIXED` (implementation
design, defense in depth) — sets the returned `GeoAngle` hidden with the
ordinary `setEuclidianVisible(false)` and repaints, then returns it. It reads
no controller field (it is constructed inside the superclass constructor).

- Call sites (report `K10`): only `DialogManager.doAngleFixed`
  (`DialogManager.java:163,166`), two-point and segment routes; the
  implementation entry gate re-establishes this census.
- Ordering: `doAngleFixed` stores the undo state after `createAngle` returns
  (`:170`), so the stored state, undo and redo hold the hidden angle.
- Never reached by `Angle(...)`, `CmdAngle3D`, GGBScript, API, Algebra input,
  macros or XML reload. `AlgoDispatcher.angle` and the kernel are untouched.

### 6.2 Visibility and persistence (`DQ-F2-8`)

- The angle stays a labelled, defined, dependent object, algebra-visible and
  accessible in the Algebra View; the rotated point is untouched (visible).
- The hidden state is the ordinary Euclidian visibility property, persisted as
  `<show object="false">`; save, reload, undo and redo apply unchanged; the
  user shows the angle with the ordinary controls.
- **Controller scope versus object property.** The interception happens only
  in the GeoCeDG Graphics View 1 controller. The property it sets is one object
  property, not a per-view session overlay: an angle created through GV1 is
  hidden in every graphics view. An angle created with the tool in Graphics
  View 2 (upstream controller) stays visible.

## 7. Graphics View 1 limits (`DQ-F2-10`)

Both targets are implemented in the GeoCeDG GV1 controller only. Graphics
View 2 runs the upstream `EuclidianControllerFor3DD` (report `K11`) and keeps
inherited behavior: no memory, visible angle. No GeoCeDG GV2 controller and no
`GuiManagerGeoCeDG.newEuclidianView` override are part of F2. The limitation is
documented in the specification and the user guides. Classic never constructs
the GeoCeDG controller and is unchanged.

## 8. Working-layer direct entry (excluded)

`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` is not part of F2 (`DQ-F2-9`). Its
tracking and state (`ENHANCEMENT — NOT A-2 BLOCKING — IMPLEMENTATION NOT
AUTHORIZED`) are unchanged; the chooser, its texts and the A-2 tests are out of
F2's scope.

## 9. Test matrix

Desktop tests on the real GV1 controller with press/release events (report
`K2`), asserting the created algorithm class and inputs:

| Id | Obligation |
|---|---|
| `T-F2-PAR-POINT-FIRST` | remembered line, segment, ray, polygon edge, 2D vector and linear function: an existing-point click constructs the parallel with that exact reference |
| `T-F2-PERP-POINT-FIRST` | idem for Perpendicular (2D vectors only) |
| `T-F2-REF-FIRST-REPLACES` | reference then point constructs with the new reference and replaces the slot; the next point-first click uses it |
| `T-F2-NO-MEMORY` | empty or suspended slot: the point is selected and the tool waits; nothing else is created |
| `T-F2-SLOTS-INDEPENDENT` | the Parallel slot never feeds Perpendicular and vice versa; both survive switching between the tools and to other tools |
| `T-F2-EMPTY-SPACE` | with a usable memory an empty-space click creates the point and the line, one undo point; without memory it creates only the point, as today |
| `T-F2-2610-UNCHANGED` | T04, T15 and T15q reproduce exactly, with and without a memory |
| `T-F2-TYPES` | non-linear functions, 3D objects, failed and partial constructions never write a slot; a function made non-linear suspends its slot |
| `T-F2-CERTIFICATE` | the slot is written only from the certificate of §4.3, never from a label |
| `T-F2-LIFECYCLE` | every row of §3.4 |
| `T-F2-CANCEL` | Escape, right click and tool changes keep the slot and discard the pending selection; a reference-first gesture abandoned keeps the previous slot |
| `T-F2-PREVIEW-NO-CREATION` | mouse moves with and without memory never create an object and never write memory |
| `T-F2-UNDO` | one undo point per creation; memory operations store none; undo and redo clear both slots |
| `T-F2-NO-SERIALIZATION` | document, macro and preferences XML contain no memory trace |
| `T-F2-COMMANDS-UNCHANGED` | `Line(P, l)`, `PerpendicularLine(P, l)` and the vector forms unchanged |
| `T-F2-ANGLE-HIDDEN` | two-point and segment routes, clockwise and counter-clockwise: angle hidden, rotated point visible, both labelled and defined, angle algebra-visible |
| `T-F2-ANGLE-ROUTE-ONLY` | the companion hides only on the tool route; the call-site census is asserted |
| `T-F2-ANGLE-UNDO-REDO` | undo removes both; redo restores the hidden angle |
| `T-F2-ANGLE-PERSISTENCE` | save and reopen keep the angle hidden; ordinary `<show object="false">` |
| `T-F2-ANGLE-USER-SHOW` | the user can show the angle; it then saves visible |
| `T-F2-ANGLE-COMMAND-UNCHANGED` | `Angle(B, A, α)`, GGBScript and Algebra input create a visible angle |
| `T-F2-VIEWS` | GV2 keeps the upstream controller and companion; a GV1-created hidden angle is hidden in GV2 too |
| `T-F2-CLASSIC-UNCHANGED` | Classic constructs no GeoCeDG controller or companion |
| `T-SMOKE` | author smoke (not performed by the agent) |

## 10. Verification class

`BOUNDED_PHASE`, author-accepted, conditioned on the implementation entry gate:
one registered `PHASE` `PRE-G9B-R6-PLUS-F2` covering `compile.desktop.semantic`
and a focused `junit.desktop.pre-g9b-r6-plus-f2.semantic` selection. No
`INTEGRATION` or `FINAL`. Any change in `source/shared/**`, in an inherited
class, in a command or algorithm or in XML stops the work for author review
and classification.

## 11. Stop conditions for the implementation

- Any target requires a change to an inherited class or to `source/shared/**`,
  or an override of `parallel(Hits, boolean)`.
- The reference actually used cannot be read back with the certificate of
  §4.3.
- Release-time injection changes the point-on-path, double-click, preview or
  hit-priority behavior.
- The companion is not reached by the tool, or is reached by a command,
  script, API call, macro or reload.
- The memory would need a durable identity, label resolution, document
  association or serialization.
- The eager-clearing hook (`OTQ-F2-1`) needs an inherited change.

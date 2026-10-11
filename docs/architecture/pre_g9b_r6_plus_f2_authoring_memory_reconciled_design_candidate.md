# PRE-G9B-R6-plus-F2 — authoring-memory refinements: reconciled design candidate

- Status: **PROPOSED — RECONCILED CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-F2-PREP` (author instruction of 2026-10-11;
  characterization and documentary preparation only)
- Base: published `main` `24985f14c565b9ba6282ec80fa0cb7be20a2e459`, tree
  `1c683ebb3e303b7692915feae3417cf51e4cce22`
- Reconciles: the [`P0` F2 design candidate](pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md)
  (unchanged, historical evidence) with the
  [F2 preparation characterization report](../validation/pre_g9b_r6_plus_f2_preparation_characterization_report.md)
  (`K0`–`K16`; corrections `ER-F2-1` to `ER-F2-9` in `K14`)
- Questions: [author-decision preparation record](../validation/pre_g9b_r6_plus_f2_author_decision_preparation_record.md)
  (`DQ-F2-1` to `DQ-F2-10`, all pending)
- Claim vocabulary: **proposed / not normative**. Where a section depends on a
  pending question, it states the **recommended** alternative and names the
  question; the author's disposition replaces the recommendation.
  `selfApproved = false`.

Path abbreviations follow the report: **EC** = inherited
`EuclidianController`; **GCEC** = `GeoCeDGEuclidianController`.

## 1. Scope

| Target | Content | Questions |
|---|---|---|
| A | Parallel and Perpendicular keep independent session-only references: point first reuses a valid remembered reference; reference first, then a point uses the new reference and replaces the memory; nothing valid remembered: the tool requests the reference (today's flow) | `DQ-F2-1` to `DQ-F2-6` |
| B | Angle with Given Size: the created angle is hidden by default, the rotated point stays visible, the user can show the angle later; the `Angle` command and scripting are unchanged | `DQ-F2-7`, `DQ-F2-8` |
| ENH | `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` | `DQ-F2-9` — **not included** by this design unless the author decides so |
| all | architecture, views and verification class | `DQ-F2-10` |

## 2. Invariants

1. Geometry algorithms unchanged: the created objects are the ones the
   unchanged upstream `parallel`, `orthogonal` and `AlgoDispatcher.angle`
   create today (`AlgoLinePointLine`, `AlgoLinePointVector`,
   `AlgoOrthoLinePointLine`, `AlgoOrthoLinePointVector`, `AlgoRotatePoint`,
   `AlgoAnglePoints`).
2. Kernel command semantics unchanged: `Line(P, l)`, `PerpendicularLine`,
   `Angle(B, A, α)`, scripts, API and Algebra input create exactly what they
   create today, visible as today.
3. The dependency graph changes only by the objects a completed tool gesture
   creates, as with any tool.
4. Object identity unchanged: no durable ID, no document metadata, no new
   kernel identity, no label-based resolution.
5. No new XML element or attribute; documents, macros and preferences XML are
   unchanged except for ordinary objects and the ordinary `<show
   object="false">` of a hidden tool angle.
6. The parallel/perpendicular memory is `SESSION` state only: never serialized,
   never in undo snapshots, never restored by undo, redo, Open or New.
7. Remembering, replacing or clearing a reference never stores an undo point;
   a completed creation stores exactly one, as today.
8. The angle's visibility is the ordinary object property, editable and
   persisted as for any object.
9. Classic is unchanged (it never constructs the GeoCeDG controller).
10. No inherited class is modified. A need to modify one is an **authorization
    blocker**: the work stops and the author is asked.

## 3. Memory model (target A)

### 3.1 Owner and slots

A GeoCeDG-owned Desktop helper (proposed name `GeoCeDGLineReferenceMemory`)
with two independent slots, `PARALLEL` and `PERPENDICULAR` (`DQ-F2-2`). Each
slot holds one Java reference to a `GeoElement` or nothing. Recommended owner:
the GV1 GeoCeDG controller, created eagerly as a field and **not** reset in
`setMode` (unlike `GeoCeDGSimilarityTools`, `DQ-F2-1`). One memory per window
(each window has its own controller).

### 3.2 Currentness predicate

A slot value `ref` is **usable** iff all of:

1. **current** — `ref` is the very instance present in
   `kernel.getConstruction().getGeoSetConstructionOrder()` (identity scan,
   the `ExportAreaSession.isLive` and `GeoCeDGSimilarityTools` precedent);
   optionally confirmed by `kernel.lookupLabel(ref.getLabelSimple()) == ref`,
   which only tests the held instance and never resolves a different one
   (`DQ-F2-5`);
2. **admissible** for the slot's tool (`§5`);
3. **defined** (`ref.isDefined()`) — recommended, `DQ-F2-5`;
4. **visible in GV1** (`isEuclidianVisible`, `isVisibleInView(GV1)` and its
   layer shown) — recommended, `DQ-F2-5`.

Never used as currentness: `isInConstructionList()`,
`Construction.isInConstructionList(geo)`, `isLabelSet()`,
`getConstruction() == …`, a stored label, coordinates or position (report
`K8` finding 1).

A slot whose value fails test 1 is cleared when tested (stale forever). A slot
failing only test 3 or 4 is kept but not reused while it fails (recommended,
`DQ-F2-5`), so an undefined-then-recovered or hidden-then-shown reference is
usable again.

### 3.3 State-transition matrix (per slot)

States: `EMPTY`; `HELD(r)` with `r` usable; `HELD-SUSPENDED(r)` (current but
undefined or not visible); `HELD-STALE(r)` (no longer current, detected lazily).

| Event | From | To | Notes (evidence) |
|---|---|---|---|
| creation by this tool completes, reference `r'` read back from `input[1]` | any | `HELD(r')` | replace on success (`DQ-F2-2`); reference-first or memory reuse |
| point-first click, slot usable | `HELD(r)` | `HELD(r)` | memory reused (`§4`) |
| point-first click, slot stale | `HELD-STALE` | `EMPTY` | upstream flow: the point is selected and the tool waits for a reference (report `K4` T03) |
| point-first click, slot suspended | `HELD-SUSPENDED` | unchanged | upstream flow; memory kept for later |
| reference-first click (selection only) | any | unchanged | the new reference replaces the slot only when the creation completes |
| Escape, right click, tool re-selection, switch to another tool and back | any | unchanged | recommended `DQ-F2-1`; the pending upstream selection is discarded as today (T08) |
| the other tool creates | any | unchanged | slots are independent |
| recompute, drag of inputs | `HELD(r)` | `HELD(r)` | same instance (L01) |
| becomes undefined / recovers | `HELD` ↔ `HELD-SUSPENDED` | | same instance (L02, L03) |
| hidden / shown, its layer hidden / shown | `HELD` ↔ `HELD-SUSPENDED` | | if `DQ-F2-5` adopts test 4 |
| rename | `HELD(r)` | `HELD(r)` | same instance (L04) |
| undo, redo (any step) | `HELD(r)` | `HELD-STALE` → `EMPTY` at next use or eagerly | new objects (L05, L06); report `K8` finding 5 |
| redefine (compatible or incompatible) of `r` | `HELD(r)` | `HELD-STALE` | new object (L07, L08); a soft or identical redefinition keeps `HELD` |
| redefine with children anywhere | `HELD(r)` | `HELD-STALE` | full rebuild (S) |
| delete `r` (and delete-and-recreate the label) | `HELD(r)` | `HELD-STALE` | L09, L10 |
| File → New, Open, document replacement, clearing `setXML` | any | `EMPTY` | eagerly through the existing `AppGeoCeDG` document-boundary overrides (recommended) or lazily (L11, L12) |
| macro-produced reference | | as any reference | `AlgoMacro` output, stable identity (L13, L14) |
| window closed | | discarded with the controller | |

Eager clearing is an optimisation for document boundaries; the lazy predicate
of §3.2 is the authority (report `K9`).

## 4. Gestures (target A)

**Interception I1 — release-time seeding** (report `K6`). In the GCEC
`switchModeForProcessMode` override, for `MODE_PARALLEL` and
`MODE_ORTHOGONAL` only, before the existing `super` call (GCEC:688):

1. if `selectionPreview` is true: do nothing (upstream preview);
2. if the tool's selection lists (points, lines, vectors, functions) are not
   all empty: do nothing (a gesture is already in progress, upstream rules);
3. if the hits contain a point (an existing point, or the free point the press
   created on empty space, `DQ-F2-3`) and the slot is usable: add the
   remembered reference to the matching selection list (line, vector or
   function), then call `super`. The unchanged `parallel`/`orthogonal`
   selects the point and creates the line in the same call;
4. otherwise call `super` unchanged (reference-first click, a point with no
   usable memory, empty memory).

After `super` returns with `!selectionPreview` and a created line, read the
used reference from `getJustCreatedGeos()` → `getParentAlgorithm().getInput()[1]`
and store it in the slot if it is admissible (`§5`). Nothing else writes a
slot.

Consequences (I, to be proven by the tests of `§10`):

- The press sees an empty selection, so issue #2610 behaves exactly as today
  (T04, T15) and needs no suppression (corrects `ER-F2-4`).
- Nothing is seeded across events, so the preview-creation hazard (report
  `K3`) cannot trigger.
- A point on a line (hits: point and line) is a point-first click: the point
  wins, as upstream.
- A click on a reference while a memory exists is a reference-first click: it
  selects the reference and waits for the point; the new reference replaces the
  memory when the line is created (T02, T16 pattern).
- No `DrawLine` preview of the remembered reference before the click and no
  visual indication of the memory (`DQ-F2-6`).

## 5. Admissible references (no broadening)

Recommended (`DQ-F2-4`): the slot stores only what the tool already accepts
**and** what produced a labelled 2D output:

| Reference | Parallel slot | Perpendicular slot |
|---|---|---|
| 2D line, segment, ray, polygon edge | yes | yes |
| 2D vector | yes | yes |
| linear function | yes | yes |
| non-linear function | not stored (no labelled output, T13) | idem |
| 3D vector or 3D line | not stored (GV1 2D authoring) | not stored |
| conic, polygon, point | never (not accepted today) | idem |

Hit priority is unchanged: upstream `parallel`/`orthogonal` decide.

## 6. Cancellation, preview, success and undo

- Escape and tool changes discard the pending upstream selection exactly as
  today; the slots survive (recommended, `DQ-F2-1`).
- A gesture that fails to create (e.g. a non-linear function) leaves the slot
  unchanged.
- One completed creation = one undo point (the inherited release callback);
  an empty-space click that creates a point and, through the memory, the line
  stores one undo point for both (as upstream T06).
- Undo/redo make every held reference stale (report `K8`); after an undo the
  next point-first click asks for a reference. This is the only behavior
  compatible with invariant 4.

## 7. Angle with Given Size hidden by default (target B)

**Seam C — companion override** (report `K10`): GCEC overrides
`newCompanion()` to return a GeoCeDG-owned subclass of
`EuclidianControllerFor3DCompanion` whose public
`createAngle(GeoPointND, GeoPointND, GeoNumberValue, boolean)` calls `super`,
then sets the returned `GeoAngle` hidden in the Euclidian views
(`setEuclidianVisible(false)` and a repaint) and returns it. The rotated point
is untouched (visible). The companion reads no controller field (it is
constructed inside the superclass constructor).

- Runs before `doAngleFixed` stores the undo point, so the stored state, undo
  and redo hold the hidden angle.
- Reached only by `DialogManager.doAngleFixed` (both the two-point and the
  segment route); never by `Angle(…)`, `CmdAngle3D`, scripts, API, Algebra
  input, macros or XML reload.
- The angle remains a valid dependent object, labelled, algebra-visible
  (recommended, `DQ-F2-8`), selectable in the Algebra View, and shown again by
  the ordinary visibility controls.
- Persisted as ordinary `<show object="false">`; no new serialization
  semantics; older readers and Classic show it hidden.

Rejected: the `DialogManagerD` seam (undo ordering and inherited-code
duplication) and hiding after `doAngleFixed` (`ER-F2-6`).

## 8. Views

Recommended (`DQ-F2-10`): **GV1 only** for A and B, as F1. GV2 keeps upstream
behavior (no memory; visible angle), recorded as a known limitation in the
specification and the guides. Alternative: a GeoCeDG-owned GV2 controller
subclass installed by a `GuiManagerGeoCeDG.newEuclidianView` override, carrying
the same memory (shared or per view) and companion; it changes GV2's controller
class and needs GV2 regression tests.

## 9. Working-layer direct entry

Not part of this design. If the author includes it (`DQ-F2-9`), it is a
separate work item **F2-WL** of the same phase: a chooser dialog with the
existing list plus a numeric field validated against
`app.getLayerWorkspace().getMaxLayer()`, rejecting (never clamping) values
outside `0..maxLayer`, both routes ending in `AppGeoCeDG.chooseWorkingLayer` →
`setWorkingLayer`; A-2 contracts, the layer domain and the status bar
unchanged; the two pinned A-2 tests rewritten and EN/ES texts added.

## 10. Test matrix (proposed)

Desktop tests on the real GV1 controller with synthetic press/release events
(report `K2`), each also asserting the created algorithm class and inputs:

| Id | Obligation |
|---|---|
| `T-F2-PAR-POINT-FIRST` | with a remembered line, segment, polygon edge, vector and linear function: point click creates the parallel with that reference |
| `T-F2-PERP-POINT-FIRST` | idem for Perpendicular |
| `T-F2-REF-FIRST-REPLACES` | reference then point creates with the new reference and replaces the slot; the replaced slot is used by the next point-first click |
| `T-F2-NO-MEMORY` | empty or stale slot: the point is selected and the tool waits (today's T03) |
| `T-F2-SLOTS-INDEPENDENT` | the Parallel slot never feeds Perpendicular and vice versa; both survive switching between the tools |
| `T-F2-EMPTY-SPACE` | per `DQ-F2-3` |
| `T-F2-2610-UNCHANGED` | T04 and T15 reproduce exactly, with and without a memory |
| `T-F2-TYPES` | non-linear function, 3D objects and failed creations are never stored |
| `T-F2-LIFECYCLE` | every row of design §3.3 (recompute, undefined/recovered, hidden/shown, rename, undo, redo, compatible and incompatible redefinition, redefinition with children, delete, delete-and-recreate, New, Open, document replacement, macro reference) |
| `T-F2-CANCEL` | Escape, right click and tool switches keep the slot and discard the pending selection |
| `T-F2-PREVIEW-NO-CREATION` | mouse moves with and without memory never create an object |
| `T-F2-UNDO` | one undo point per creation; memory changes store none; after undo the slot is stale |
| `T-F2-NO-SERIALIZATION` | document, macro and preferences XML contain no memory trace |
| `T-F2-COMMANDS-UNCHANGED` | `Line(P, l)`, `PerpendicularLine(P, l)` and the vector forms unchanged |
| `T-F2-ANGLE-HIDDEN` | two-point and segment routes, clockwise and counter-clockwise: angle hidden, rotated point visible, both labelled and defined |
| `T-F2-ANGLE-UNDO-REDO` | undo removes both; redo restores the hidden angle |
| `T-F2-ANGLE-PERSISTENCE` | save and reopen keep the angle hidden; XML shows ordinary `<show object="false">` |
| `T-F2-ANGLE-USER-SHOW` | the user can show the angle; it then saves visible |
| `T-F2-ANGLE-COMMAND-UNCHANGED` | `Angle(B, A, α)`, scripts and Algebra input create a visible angle |
| `T-F2-VIEWS` | per `DQ-F2-10` (GV2 unchanged, or GV2 covered) |
| `T-F2-CLASSIC-UNCHANGED` | Classic constructs no GeoCeDG controller or companion |
| `T-SMOKE` | author smoke (not performed by the agent) |

## 11. Verification class (proposed)

`BOUNDED_PHASE` with one registered `PHASE` `PRE-G9B-R6-PLUS-F2` covering
`compile.desktop.semantic` and a focused `junit.desktop.pre-g9b-r6-plus-f2.semantic`
selection (`DQ-F2-10`). It escalates — and the work stops for the author — on
any change in `source/shared/**`, in an inherited class, in a command or
algorithm, in XML, or (if the author chooses GV2 coverage) on GV2 regressions
beyond the bounded surface. The working-layer item, if included, adds
`workspace-profile.product` and the two A-2 test classes.

## 12. Stop conditions for the implementation

- Any target requires a change to an inherited class (`EuclidianController`,
  `DialogManager`, `GuiManager3D`, companions) or to `source/shared/**`.
- The reference actually used cannot be read back from the created algorithm.
- Release-time seeding changes #2610, preview or hit-priority behavior.
- The companion seam is not reached by the tool, or is reached by a command,
  script or reload.
- Memory would need a durable identity, label resolution or serialization.

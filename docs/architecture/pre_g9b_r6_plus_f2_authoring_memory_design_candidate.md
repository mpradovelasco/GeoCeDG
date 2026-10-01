# PRE-G9B-R6-plus-F2 — authoring-memory refinements: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Subphase `F2` needs its own
  explicit author authorization.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  finding 18 and probe P6

Path abbreviations: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`.

## 1. Author-fixed scope

- **Angle with Given Size:** the generated angle is hidden by default; the user
  may show it afterwards.
- **Parallel and perpendicular tools:** independent remembered references.
  - Point first: use the remembered reference.
  - Reference first, then a point: use that reference and replace the
    remembered one.
  - Nothing remembered: request the reference first.

This is authoring-mode and session behavior. Command semantics do not change.

## 2. Current behavior (finding 18)

- `parallel(Hits, boolean)` is `protected final`; `orthogonal(Hits, boolean)` is
  overridable (`common/euclidian/EuclidianController.java:2424`, `:2507`).
- Both accept a point plus a vector, any line-like object (lines, segments,
  rays; polygon edges through their segment hits) or a function, in either
  order. The perpendicular tool accepts only 2D vectors.
- They fire as soon as both are selected. The selection getters empty the lists
  as they read them (`EuclidianController.java:919-1044`), so nothing is
  remembered.
- On press, with one line already selected, a click on another path creates a
  point on that path (issue #2610, `EuclidianController.java:9171-9177`). This
  is the gesture conflict for "reference first" while a remembered reference
  is pre-selected.
- `GeoCeDGEuclidianController` already overrides `wrapMousePressed`, `setMode`
  and `switchModeForProcessMode` (`geocedg-desktop/GeoCeDGEuclidianController.java:81`,
  `:431`, `:484`). Parallel and perpendicular fall through to `super`
  (`:517-518`).
- Precedents: `GeoCeDGSimilarityTools` (one session field, reset on mode change)
  and `GeoCeDGIntersectionSession`, which re-resolves through a persistent ID
  after a rebuild.

**Lifecycle probe (P6).** An ordinary line has **no persistent geo ID**. After
undo, redo, a redefinition and a reopen, the line is a **different Java
object**. A deleted object is gone. `Construction.isInConstructionList(oldRef)`
still answers `true` for the stale pre-undo reference, because it reads a
per-object flag (`common/kernel/Construction.java:1273-1278`), so it cannot
detect staleness.

## 3. Candidate: a session helper per tool

- `GeoCeDGLineReferenceMemory`, with two independent slots
  (`PARALLEL`, `PERPENDICULAR`), each holding a reference or nothing. `SESSION`
  only; never serialized; never label-resolved.
- **Validity check before every use:** the reference is defined, and it is the
  very object currently registered under its label in the current construction
  (`kernel.lookupLabel(ref.getLabelSimple()) == ref`). This is an identity check
  that rejects stale objects; it never resolves a different object by label.
- **Clearing:**
  - every construction rebuild, through the kernel clear-view notification
    that `EventDispatcher` already fans out as `reset()`
    (`common/plugin/EventDispatcher.java:256-263`, triggered by
    `Kernel.notifyClearView`). INFERRED from source, not probed: this covers
    New, Open, undo, redo and a rebuilding redefinition. The identity check
    below keeps the design safe even if one route is missed;
  - the `REMOVE` of the referenced object;
  - and a failed validity check.

  After a clear, the next point-first click asks for a reference again, as the
  author requires.
- **Gestures**, in `switchModeForProcessMode` and the press path for
  `MODE_PARALLEL` and `MODE_ORTHOGONAL`:
  1. a click on a valid reference type while no point is selected replaces the
     slot and selects the reference. The press-time point-on-path creation is
     suppressed for that click, which resolves the issue #2610 conflict;
  2. a click on a point, or on empty space that creates a free point, with a
     valid remembered reference pre-seeds the line selection and lets the
     upstream algorithm run unchanged;
  3. with no valid reference, the upstream flow runs and the reference that the
     new algorithm actually used is stored afterwards. It is read back from the
     created algorithm's input, never inferred.
- **Valid reference types:** `GeoLineND` (line, segment, ray) and 2D vectors for
  both tools; linear functions for both, as upstream accepts them; nothing else.
- **Undo:** creating the parallel or perpendicular line remains the single
  ordinary undo point. Remembering is not an undo step.

## 4. Angle with Given Size hidden by default

- Today: `angleFixed` opens the dialog
  (`EuclidianController.java:3154-3177`). `DialogManager.createAngleFixed`
  evaluates only the number (`common/main/DialogManager.java:1016-1057`).
  `doAngleFixed` calls `createAngle` (`:159-171`), which goes through the
  companion to `AlgoDispatcher.angle` (`common/kernel/algos/AlgoDispatcher.java:768-801`).
  `AlgoAnglePointsND` makes the angle visible with `setDrawableNoSlider()`
  (`common/kernel/algos/AlgoAnglePointsND.java:92-96`). The `Angle(B, A, α)`
  command shares the same `AlgoDispatcher.angle` (`common/kernel/commands/CmdAngle.java:249-253`).
- Candidate: hide the created angle **on the tool path only**, after
  `doAngleFixed` returns. Either a GeoCeDG `DialogManagerD` subclass supplied by
  `GuiManagerGeoCeDG`, or a GeoCeDG controller companion override of
  `createAngle`; `F2` picks the smaller seam. The command and scripts stay
  unchanged. The rotated point stays visible (the author named the angle only).
  The angle's visibility is then ordinary object state, serialized as today.

## 5. Serialization

None.

## 6. Verification-class recommendation

`BOUNDED_PHASE` (registered `PHASE`): Desktop mode orchestration only. It
escalates if a command or algorithm changes.

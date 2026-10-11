# GeoCeDG authoring memory

| Field | Value |
|---|---|
| Status | **PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW**; not author approved |
| Version | `1.0` |
| Owner phase | `PRE-G9B-R6-plus-F2` (authoring-memory refinements) |
| Governing author decisions | [F2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_f2_author_decisions_record.md) v1.1 (`DQ-F2-1` to `DQ-F2-10`; implementation authorization and entry gate §8) |
| Approved design transcribed | [F2 reconciled design](../../../docs/architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md) (`DESIGN — AUTHOR APPROVED`) |
| Evidence | [F2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_f2_preparation_characterization_report.md) `K0`–`K16`; entry-gate probe (record §8) |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §17.2 (state classes); `geocedg/specs/operations/verification-levels.md` §12.8 |
| Serialization effect | none (the hidden tool angle uses the existing `<show object="false">`) |

## 0. Status and normative language

This specification is the durable contract of the F2 technical candidate. It
becomes normative only through an author closeout record; until then it binds
the candidate and nothing else. It transcribes the approved design without
semantic change; §10 records how the candidate implements it. `MUST`,
`MUST NOT`, `SHOULD` and `MAY` have their usual meaning. Where an author
decision and this text differ, the decision prevails and this text is
defective.

Provenance markers: **[AD]** author decision, **[NI]** normative inheritance,
**[CE]** characterization evidence, **[ID]** implementation design inside the
approved scope (reviewable, not an author decision).

## 1. Scope

1.1 **[AD]** The Parallel Line and Perpendicular Line tools of GeoCeDG
Graphics View 1 each keep one remembered reference for the document session
(`DQ-F2-1`, `DQ-F2-2`).

1.2 **[AD]** The angle created by the Angle with Given Size tool through
GeoCeDG Graphics View 1 is hidden in the graphical views by the ordinary object
visibility; the rotated point stays visible (`DQ-F2-7`, `DQ-F2-8`).

1.3 **[AD]** `ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY` is not part of this
specification (`DQ-F2-9`).

1.4 **[AD]** Commands (`Line`, `PerpendicularLine`, `Angle`), GGBScript, API,
Algebra input, macros and document loading MUST behave as before. Graphics
View 2 and Classic MUST behave as before (`DQ-F2-8`, `DQ-F2-10`).

## 2. Invariants

2.1 **[AD]** Every created object is the one the unchanged upstream tool or
algorithm creates for the same point and reference: `AlgoLinePointLine`,
`AlgoLinePointVector`, `AlgoOrthoLinePointLine`, `AlgoOrthoLinePointVector`,
`AlgoRotatePoint`, `AlgoAnglePoints`, with the same inputs.

2.2 **[AD]** The memory is `SESSION` state: it MUST NOT be serialized, MUST NOT
appear in undo snapshots, documents, macros or preferences, and MUST NOT
introduce durable identifiers, document associations or kernel identity
infrastructure (`DQ-F2-5`).

2.3 **[AD]** Remembering, replacing, suspending or clearing a reference MUST
NOT store an undo point; a completed construction stores exactly one, as
before (`DQ-F2-6`).

2.4 **[AD]** No inherited class and no `source/shared/**` file is modified
(`DQ-F2-10`).

## 3. Reference memory

3.1 **[AD]** Two independent slots, `PARALLEL` and `PERPENDICULAR`. A slot is
`EMPTY` or holds one exact object reference.

3.2 **[AD]** A slot survives ordinary tool changes, including Escape, within
the same document session (`DQ-F2-1`).

3.3 **Admissible reference** **[AD]**, made exact by the entry gate **[CE]**
(record §8.2, §8.4): a non-3D `GeoLine` (line, segment, ray, polygon edge); a
non-3D `GeoVector` (both tools); a non-3D `GeoFunction` whose kernel linear
interpretation `GeoFunction.getX()` is finite. `getX()` is finite exactly for
functions that expand to a polynomial of degree 1 (for example `2x + 1`, `x`,
`(x + 1)² − x²`, `a x² + x` while `a = 0`).

3.4 **Constant functions** **[CE]**: `getX()` is NaN for a degree-0 polynomial
(`3`, `0`, `0x² + 5`), and the inherited tools create no line from them; they
are not admissible. This is an inherited kernel limitation, not broadened.
Non-polynomial and higher-degree functions, 3D objects, conics, polygons and
points are not admissible.

3.5 **Currentness** **[AD]**: a slot value is current iff it is an
exact-instance member of the current construction order
(`getGeoSetConstructionOrder()`). A label lookup MAY corroborate the same
instance and MUST NOT resolve another object. `isInConstructionList`,
`Construction.isInConstructionList`, `isLabelSet`, `getConstruction()`, labels,
coordinates and positions MUST NOT be used as currentness tests (they remain
true or matching for stale instances after undo, redo, Open and New) **[CE]**.

3.6 **Usability** **[AD]**, predicate fixed by the entry gate **[ID]** (record
§8.3): a current slot value is usable iff it is admissible now (§3.3),
`isDefined()`, `isEuclidianVisible()`, `isVisibleInView(GV1)` and its layer is
shown (`App.isLayerShown`, A-2 hidden layers). This visibility conjunction is
the one of the approved orientation cue
([orientation-cue specification](../presentation/orientation-cue.md)).

3.7 **States** **[AD]**: `EMPTY` (no value, or a non-current value, which is
cleared); `HELD` (current and usable); `SUSPENDED` (current, not usable).
`SUSPENDED` keeps the value and MUST NOT bind another object; it becomes `HELD`
again when the object is usable.

## 4. State machine (per slot)

| Event | Result |
|---|---|
| successful committed construction by this tool (§6) | the slot holds the certified reference |
| point-first click with the slot `HELD` | the remembered reference is used (§5) |
| point-first click with the slot `EMPTY` or `SUSPENDED` | inherited flow: the point waits for an explicit reference |
| reference-first click, abandoned gesture, cancellation, failed construction, preview | unchanged |
| the other tool constructs | unchanged |
| tool change, Escape, right click | unchanged |
| recompute, rename, soft or identical redefinition (same instance) | unchanged |
| object undefined, hidden, shown only in GV2, layer hidden, function no longer lineable | `SUSPENDED` while it lasts |
| object deleted, or replaced by a redefinition (`REDEFINE` with a new object) | `EMPTY` |
| undo, redo, any construction rebuild | both slots `EMPTY` |
| File → New, Open, full document replacement, clearing XML load | both slots `EMPTY` |

## 5. Gestures

5.1 **[AD]** With the slot `HELD`, a click on an existing point or on empty
space starts a construction with the remembered reference. On empty space the
inherited press creates the free point; the line is created at release; one
undo point covers both.

5.2 **[AD]** With the slot `EMPTY` or `SUSPENDED`, the inherited flow runs: an
explicit reference is required. No reference is invented and no geometry
beyond the inherited flow is created.

5.3 **[AD]** A click on a reference is reference-first: it selects the
reference and waits for the point; the new reference replaces the slot only
when the construction succeeds.

5.4 **[AD] [ID]** Injection happens only at release, inside the GeoCeDG
process step, for non-preview steps, when the tool's point, line, vector and
function selections are empty and the hits contain a point (existing, or
created by the press on empty space). The injected reference lives only inside
that step; if no construction results it is removed again. Nothing is
pre-selected at press, at tool entry or across events.

5.5 **[AD]** Preserved inherited gestures: with a line selected, a click on
another path (or on the same line after the double-click interval) creates a
point on that path and the construction through it (issue #2610); a quick
second click on the same object opens the Redefine dialog; when a point and a
line are hit, the point wins; a second point is ignored; hit priority and
preview are unchanged.

5.6 **[AD]** No preview of the remembered reference and no memory indicator
in v1 (`DQ-F2-6`).

## 6. Success certificate

6.1 **[AD]** A slot is written only after a successful committed construction,
from the reference actually used by the kernel algorithm, never from a label or
a non-null output alone (`DQ-F2-4`).

6.2 **[ID]** (record §8.2) After a non-preview process step that reports a
creation, the just-created list holds only that step's output. The certificate
holds iff: it contains exactly one line; the line is an exact-instance member
of the construction order and is defined; its parent algorithm is exactly
`AlgoLinePointLine` or `AlgoLinePointVector` (Parallel), or
`AlgoOrthoLinePointLine` or `AlgoOrthoLinePointVector` (Perpendicular); and its
`input[1]` is admissible (§3.3). `input[1]` then becomes the slot value.
`input[0]` is the point and `input[1]` the reference in all four algorithms
**[CE]**.

## 7. Lifecycle and invalidation

7.1 **[AD]** Currentness (§3.5) MUST be verified before every implicit reuse.

7.2 **[ID]** (record §8.1) Eager invalidation uses a GeoCeDG-owned client
`EventListener` registered on the application `EventDispatcher` when a slot is
first written: `reset()` (called by `EventDispatcher.clearView`), `UNDO` and
`REDO` clear both slots; `REMOVE` and `REDEFINE` clear every slot whose value is
no longer current. **[CE]** `reset()` is called on undo, redo, a redefinition
that rebuilds the construction, clearing XML loads (Open, document
replacement, clearing `setXML`) and File → New, and not on store-undo, mode
changes, recomputation, visibility or layer changes, language changes,
creation of Graphics View 2, or non-clearing XML evaluation.

## 8. Angle with Given Size

8.1 **[AD]** The GeoCeDG Graphics View 1 controller uses a GeoCeDG-owned
controller companion whose `createAngle(GeoPointND, GeoPointND,
GeoNumberValue, boolean)` invokes the inherited construction, hides the
returned angle with the ordinary `setEuclidianVisible(false)`, leaves the
rotated point unchanged and returns the angle without changing its geometry
or dependencies.

8.2 **[CE] [ID]** The four-argument `createAngle` is called only by
`DialogManager.doAngleFixed` (two points; segment end and start points), which
stores the undo state after it returns, so the stored state holds the hidden
angle. The companion hides only while its controller is in the Angle with
Given Size mode.

8.3 **[AD]** The angle stays a defined, labelled, dependent object visible in
the Algebra View; the user can show it with the ordinary controls; save,
reload, undo and redo use the ordinary visibility (`<show object="false">`).

8.4 **[AD]** Controller scope versus object property: the interception exists
only in the GeoCeDG Graphics View 1 controller; the property it sets is one
object property, so an angle created through Graphics View 1 is hidden in every
graphical view. The tool used in Graphics View 2 (upstream controller) creates
a visible angle.

## 9. Graphics View 1 limitation

9.1 **[AD]** Both features exist only in GeoCeDG Graphics View 1. Graphics
View 2 runs the upstream `EuclidianControllerFor3DD` and keeps the inherited
behavior (no memory; visible angle). Classic never constructs the GeoCeDG
controller.

## 10. Implementation (technical candidate)

| Element | Implementation |
|---|---|
| memory, admissibility, currentness, usability, invalidation listener | `org.geocedg.desktop.GeoCeDGLineReferenceMemory` |
| injection, read-back and certificate | `GeoCeDGEuclidianController.switchModeForProcessMode` (before and after its existing `super` call) |
| angle hook | `org.geocedg.desktop.GeoCeDGControllerCompanion` (extends `EuclidianControllerFor3DCompanion`), returned by `GeoCeDGEuclidianController.newCompanion()` |
| tests | `PreG9BR6PlusF2AuthoringMemoryTest` |

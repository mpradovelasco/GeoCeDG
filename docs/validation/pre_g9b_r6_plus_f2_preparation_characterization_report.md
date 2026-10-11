# PRE-G9B-R6-plus-F2 — preparation characterization report

- Activity: `PRE-G9B-R6-plus-F2-PREP` (author instruction of 2026-10-11,
  "Authoring-Memory Refinements — Characterization and Design Preparation")
- Status: **CHARACTERIZATION COMPLETE — DOCUMENTARY PREPARATION CANDIDATE —
  PENDING AUTHOR REVIEW**
- Base: published `main` `24985f14c565b9ba6282ec80fa0cb7be20a2e459`, tree
  `1c683ebb3e303b7692915feae3417cf51e4cce22` (`P_F1`, the F1 closeout)
- Phase state: `PRE-G9B-R6-plus-F2` = **PREPARED — NOT AUTHORIZED**
- Preparation class: `DOCUMENTATION_STATUS_ONLY` (acceptance `STATIC`)
- Machine-readable mirror:
  [`pre-g9b-r6-plus-f2-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f2-preparation-characterization.json)
- Design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f2_authoring_memory_reconciled_design_candidate.md);
  questions: [author-decision preparation record](pre_g9b_r6_plus_f2_author_decision_preparation_record.md);
  contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f2-authoring-memory.prompt.md)
- Superseded evidence (unchanged, historical):
  [`P0` F2 design candidate](../architecture/pre_g9b_r6_plus_f2_authoring_memory_design_candidate.md)
  (`PROPOSED — CANDIDATE — NOT AUTHOR APPROVED`)
- `selfApproved = false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`

This report characterizes the current product at the base. It decides nothing.
Every `DQ-F2-*` question stays pending until the author disposes of it.

Evidence classes: **S** = proven from source at the base; **P** = proven by the
scratch probe of `K15`; **I** = inferred from reading, not executed.

Path abbreviations: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`;
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`;
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`;
**EC** = `common/euclidian/EuclidianController.java`; **GCEC** =
`geocedg-desktop/GeoCeDGEuclidianController.java`.

## K0 — Scope, authority and entry gate

**Author-fixed targets** (author instruction of 2026-10-11; mini-track plan §2;
`P0` candidate §1):

- **A — parallel and perpendicular memory.** Each tool keeps an independent,
  session-only reference. Point first: reuse the valid remembered reference.
  Reference first, then a point: use the new reference and replace the memory.
  Nothing remembered: request the reference.
- **B — Angle with Given Size.** The created angle is hidden by default; the
  rotated point stays visible; the user can show the angle later; the ordinary
  `Angle` command and scripting are unchanged.

**Entry gate (2026-10-11).**

| Check | Result |
|---|---|
| local `main`, `origin/main`, live remote `main` | all `24985f14c565b9ba6282ec80fa0cb7be20a2e459` |
| tree | `1c683ebb3e303b7692915feae3417cf51e4cce22` |
| worktree | clean |
| F1 | `PASS — AUTHOR APPROVED — PUBLISHED` ([closeout record](pre_g9b_r6_plus_f1_closeout_record.md)) |
| F2 before this activity | `NOT AUTHORIZED` (roadmap, plan) |
| F2 code at the base | none: no memory, slot, companion or angle-hiding code in `geocedg-desktop/` (grep: `newCompanion`, `createAngle`, `MODE_PARALLEL`, `MODE_ORTHOGONAL`, `MODE_ANGLE_FIXED` have no GeoCeDG main-source hit) |
| preparation branch | `phase/pre-g9b-r6-plus-f2-prep` (local), from the base |

**Authorities read from disk.** `AGENTS.md`; the canonical governance and
verification prompts; the roadmap (version 5.14) and the mini-track plan
(§2, §9.2, §9.3, §13, status block); `geocedg/specs/operations/verification-levels.md`
§12.8; the `P0` report (finding 18, probe P6) and the `P0` F2 candidate
(evidence only); the A-2 closeout record (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`)
and the POST-E2 P4 closeout note assigning that enhancement to `F2`.

## K1 — F1 prerequisite

F1 is published (`24985f14`, closeout of `1cea3c89`). F1 changed
`GeoCeDGEuclidianView.paint` and the presentation surface only; it touched no
controller, companion, dialog or selection code (S: F1 candidate report file
list). F2 has no technical coupling to F1 (roadmap row: "predecesor
recomendado: F1 … sin acoplamiento técnico"). The open observation
`OBS-R6PLUS-F1-G9U1-SEMANTIC-DRAG-UNDO-INTERMITTENT` concerns a Locus V2 drag
test and is unrelated to the F2 code paths (I).

## K2 — Method

- Source reading at the base of the inherited click path, selection lists,
  companion, dialog manager, undo, XML and lifecycle code, with every cited line
  re-established at `24985f14`.
- One scratch-only JUnit probe (`F2PrepProbeTest`, four methods) in the Desktop
  test source set through a Gradle init script; never in a tracked path; after
  the runs `compileTestJava` was rerun without the init script and the probe
  test-result XML and compile-stash copies were removed (`K15`).
- Tool gestures were driven through the real controller:
  `app.setMode(mode)` and `MouseEventD`-wrapped `MOUSE_PRESSED` events passed
  to `wrapMousePressed` and `wrapMouseReleased` of the GV1 controller, on an
  800 × 600 view at scale 40 (the G9U1 semantic-point precedent).

## K3 — Parallel and perpendicular: click path

**Press (S).** GCEC `wrapMousePressed` (GCEC:87) handles only its own gestures
and otherwise calls `super`. EC `wrapMousePressed` (EC:9287) returns early on a
right click (EC:9396-9399) and calls `switchModeForMousePressed` (EC:9429). GCEC
overrides that method (GCEC:333-349) for the ISO A and dimension tools only and
then calls `super`. The inherited case for `MODE_PARALLEL`, `MODE_ORTHOGONAL`
and `MODE_ORTHOGONAL_THREE_D` (EC:9164-9178):

- removes polygons from the hits;
- on empty space creates a free point (`createNewPoint`, EC:9172);
- **issue #2610:** if exactly one line is already selected and the first hit is
  a path, creates a point on that path (`createNewPointForModeOther`,
  EC:9173-9176).

**Release (S).** GCEC `wrapMouseReleased` (GCEC:241) calls `super` for these
modes. EC adds a point created at press to empty hits (EC:10499-10501), calls
`processMode` (EC:10534, `public final`), whose callback stores the undo point
when it receives `true` (EC:10527-10531). `processMode` calls
`switchModeForProcessMode`. GCEC overrides it (GCEC:640); for these modes no
GeoCeDG branch applies and control reaches `super` at **GCEC:688**. EC dispatches
to `parallel(hits, selPreview)` (EC:5157-5159) and `orthogonal(hits, selPreview)`
(EC:5167-5169); `endOfSwitchModeForProcessMode` (EC:5449-5484, `final
protected`) memorizes the created geos and calls the callback.

**Selection logic (S).** `parallel(Hits, boolean)` is `protected final`
(EC:2424-2482): it selects one point; if no point was hit it adds one vector
(any `GeoVectorND`) if no line is selected, one line (`GeoLineND`) if no vector is
selected, and one function. With one point it creates, in priority order,
vector → line → function. `orthogonal(Hits, boolean)` is `protected` and
overridable (EC:2507-2515) but delegates to the `final protected`
three-argument form (EC:2517-2561), identical except that the vector filter is
2D `GeoVector` only (EC:2522). The selection getters copy and clear the lists
(EC:919-1044), so nothing survives a creation.

**Algorithms (S).** Parallel: `AlgoLinePointVector` (vector), `AlgoLinePointLine`
(line, segment, ray, linear function as `Lineable2D`); 3D inputs go to
`Manager3D.line3D`. Perpendicular: `AlgoOrthoLinePointVector` (2D vector),
`AlgoOrthoLinePointLine` (line and function, through the For3D companion,
`EuclidianControllerFor3DCompanion.java:265-268`). In all four 2D algorithms
`input[0]` is the point and `input[1]` the reference (S, algorithm constructors).

**Undo (S).** The single undo point of a creation is the release callback. A
free point created on empty space without a completed line stores no undo point
(EC:10478; these modes are not in `modeCreatesHelperPoints`, EC:597-622).

**Preview (S).** Mouse moves reach `switchModeForProcessMode(…, true)`
(EC:5578-5586). In preview, `handleAddSelected` only highlights (EC:1659-1661).
**Hazard:** the creation branches of `parallel` and `orthogonal` have no
`selPreview` guard (EC:2442-2479, 2532-2558): if the real selection lists
already held one point and one reference, a mouse move would create the line.
The `DrawLine` preview reads the live point, line and function lists, not
vectors (EC:11021-11044).

## K4 — Current behavior matrix (probe `tools`)

Scene: `l = Line(A,B)` (y = −4), `m = Line(C,D)`, free points `P`, `Q`, vector
`v`, segment `s`, polygon `tri` with edge `i`, linear `g(x) = x + 9`, non-linear
`h(x) = 0.5x² − 7`. Identical results for both tools (P), except the algorithm
class.

| Id | Gesture | Selection after each click | Created |
|---|---|---|---|
| T01 | point `P`, then `l` | `[P]`, `[]` | line through `P` (`AlgoLinePointLine(P,l)` / `AlgoOrthoLinePointLine(P,l)`) |
| T02 | `l`, then `P` | `[l]`, `[]` | same |
| T03 | `l`, `P`, then `Q` | `[l]`, `[]`, `[Q]` | one line; `Q` waits for a reference (**no memory today**) |
| T04 | `l`, then line `m` (#2610) | `[l]`, `[]` | point `E` on `m` (`AlgoPointOnPath(m)`) **and** the line through `E` with reference `l` |
| T05 | `l`, `m`, then `Q` | … `[Q]` | as T04; `Q` waits |
| T06 | empty space, then `l` | `[E]`, `[]` | free point `E` and the line through it |
| T07 | `l`, then empty space | `[l]`, `[]` | free point `E` and the line through it |
| T08 | `l`, tool re-selection (Escape route), `P` | `[l]`, `[]`, `[P]` | nothing; the pending reference is discarded |
| T09 | vector `v`, then `P` | `[v]`, `[]` | `AlgoLinePointVector` / `AlgoOrthoLinePointVector` |
| T10 | segment `s`, then `P` | `[s]`, `[]` | line with reference `s` |
| T11 | polygon edge `i`, then `P` | `[i]`, `[]` | line with reference `i` (the edge is a segment hit; the polygon is removed from the hits) |
| T12 | linear function `g`, then `P` | `[g]`, `[]` | line with reference `g` |
| T13 | non-linear `h`, then `P` | `[h]`, `[]` | **nothing labelled**: the function is selectable, the algorithm output is undefined and unlabelled (diagnostic: direct `line(null, P, h)` gives `defined=false`, `labelSet=false`) |
| T14 | `P`, then `Q` | `[P]`, `[P]` | nothing; the second point is ignored (one-point limit) |
| T15 | `l`, then `l` again after 1.5 s | `[l]`, `[]` | point `E` **on `l`** (#2610 path) and the line through `E` with reference `l` (for Parallel this coincides with `l`) |
| T15q | `l`, then `l` again immediately | `[l]`, `[l]` | nothing created; the host opens the inherited **Redefine** dialog (`InputDialogD`), i.e. a double click |
| T16 | `l`, `P`, `m`, `Q` | `[l]`, `[]`, `[m]`, `[]` | two lines: `(P,l)` and `(Q,m)` |

No window opened in any row except T15q (P: `Window.getWindows()` after each
flow).

## K5 — Reference types (current selection filters)

| Reference | Parallel | Perpendicular | Evidence |
|---|---|---|---|
| 2D line, segment, ray | yes (`GeoLineND`) | yes | S EC:2435, 2525; P T01, T10 |
| polygon edge | yes, as its segment | yes | S `Hits.removePolygons` (Hits:393-407); P T11 |
| 2D vector | yes | yes | P T09 |
| 3D vector | yes (`GEOVECTORND`, then `line3D`) | **no** (2D `GeoVector` only) | S EC:2432, 2452, 2522, 2539-2541 |
| linear function | yes (`Lineable2D`) | yes | P T12 |
| non-linear function | selectable, output undefined and unlabelled | same | P T13 |
| undefined line | not drawn, therefore not hittable (I); as a remembered reference see `K8` L02 | same | P L02 |
| 3D line | `Manager3D.line3D` | `Manager3D.orthogonalLine3D` with the view direction | S EC:2465; For3DCompanion:265-268 |
| conic, polygon interior, point | not a reference | not a reference | S (no filter) |
| object no longer present | not hittable; as a remembered instance see `K8` | same | P L09 |

## K6 — The issue #2610 gesture and the interception options

**Behavior (S, P T04, T15).** At press, with one line already in the selection
list and a path as the first hit, the host creates a point on that path; the
release then completes the line with the **already selected** reference. Vectors
and functions do not trigger it (`selLines() == 1` only).

**Where it would conflict with memory (I).** Only if the remembered reference
were placed in the real selection list **before the press** (pre-selection at
mode entry, or after the previous creation). Then a "reference first" click on a
new line would become a point on the new line with a line parallel to the old
reference, a click on the same line would toggle it off, and a remembered vector
would block the line filter (EC:2434).

**Interception options, inherited source unchanged:**

| Option | Mechanism | Assessment |
|---|---|---|
| I1 — seed at release only | in the GCEC `switchModeForProcessMode` override, immediately before `super` (GCEC:688), when `!selectionPreview`, the selection lists are empty, and the hits contain a point (including the point created at press) but no point-first conflict: add the remembered reference to the selection list, then let `parallel`/`orthogonal` run | press stage sees an empty selection, so #2610 never fires for a memory click; nothing persists between events, so the preview hazard of `K3` cannot trigger; upstream creation code runs unchanged; **no `DrawLine` preview of the remembered reference before the click** |
| I2 — pre-select at mode entry or after creation | seed after `super.setMode` or in `switchPreviewableForInitNewMode` (GCEC:925) | gives a preview; meets #2610, the toggle and the preview-creation hazard; would need a press-stage suppression in `switchModeForMousePressed` (GCEC:333) and a guard for mouse moves; no inherited change is strictly required, but the interaction surface is wider |
| I3 — override `createNewPointForModeOther` | mode-guarded override (EC:8967) | also used by other modes and by the drag-release path (EC:10193); rejected as riskier |

The smallest Desktop interception that needs **no** #2610 suppression is I1.
The validations it needs are listed in the design candidate §4 and the test
matrix (`T-F2-2610-UNCHANGED`, `T-F2-PREVIEW-NO-CREATION`).

**Read-back of the used reference (S).** After `super.switchModeForProcessMode`
returns with `!selectionPreview`, the created line is available through
`getJustCreatedGeos()` (`public`, EC:1178, filled at EC:5481) and its
`getParentAlgorithm().getInput()[1]` is the reference actually used; the
GCEC intersect branch already wraps the release callback (GCEC:672-686), a
second read-back route.

## K7 — Hooks and GeoCeDG precedents

| Hook | Modifier | Use |
|---|---|---|
| GCEC `switchModeForProcessMode` | own override (GCEC:640) | seed and read-back (I1) |
| GCEC `switchModeForMousePressed` | own override (GCEC:333) | press-stage work (precedent: ISO A and dimension points) |
| GCEC `setMode` | own override (GCEC:586-610) | where `GeoCeDGSimilarityTools.reset()` runs on every tool change (GCEC:603-605) |
| `parallel(Hits, boolean)` | `protected final` | **not overridable; not proposed** |
| `orthogonal(Hits, boolean)` | `protected` | overridable but adds nothing over I1 |
| `getSelected*List()` | `protected`, not final (EC:12209-12265) | the subclass can add to the live lists |
| `SelectionManager.addToSelectionList` | `public` | idem |
| `newCompanion()` | `protected` (EC:690; `EuclidianControllerFor3DD.java:34`) | companion seam for B (`K10`) |

**Precedents.** `GeoCeDGSimilarityTools` keeps one session field, resets it on
every `setMode` and validates operands by **exact-instance membership** in
`getGeoSetConstructionOrder()` (`GeoCeDGSimilarityTools.java:105-113`).
`ExportAreaSession.isLive` uses the same identity scan and documents that
`isInConstructionList` answers true for replaced objects
(`geocedg-desktop/export/ExportAreaSession.java:254-272`).
`GeoCeDGIntersectionSession` re-resolves a durable `PersistentGeoId`; ordinary
lines have none, so that precedent does not apply (S/I). No GeoCeDG class
registers an `EventListener`, a kernel `View` or an undo listener for
lifecycle purposes (S, grep).

## K8 — Reference identity and currentness (mandatory gate)

Probe `lifecycle` held a Java reference `ref` to a line and, after each
transition, evaluated five candidate currentness tests (P):

- **LK** `kernel.lookupLabel(ref.getLabelSimple()) == ref` (label lookup used
  only to test the held instance);
- **MB** exact-instance membership of `ref` in
  `getConstruction().getGeoSetConstructionOrder()`;
- **IC** `Construction.isInConstructionList(ref)` (`common/kernel/Construction.java:1295-1300`);
- **LS** `ref.isLabelSet()`;
- **CC** `ref.getConstruction() == kernel.getConstruction()`.

| Id | Transition | Same instance? | LK | MB | IC | LS | CC | defined | Events seen by a client `EventListener` |
|---|---|---|---|---|---|---|---|---|---|
| L00 | base | — | T | T | T | T | T | T | — |
| L01 | recompute (input moved) | yes | T | T | T | T | T | T | `UPDATE` |
| L02 | becomes undefined (`B` on `A`) | yes | T | T | T | T | T | **F** | `UPDATE` |
| L03 | recovers | yes | T | T | T | T | T | T | `UPDATE` |
| L04 | rename `l → l2` | yes | T | T | T | T | T | T | `RENAME`, `UPDATE`, `RENAME_COMPLETE` |
| L05 | undo (of an unrelated step) | **no** | F | F | **T** | **T** | T | T | `reset()` ×2, `UNDO` |
| L06 | redo | **no** | F | F | **T** | **T** | T | T | `reset()` ×2, `REDO` |
| L07 | redefine, compatible (`Line(A,Q)`) | **no** | F | F | F | F | T | T | `UPDATE`, `REDEFINE` (new object) |
| L08 | redefine, incompatible (`Segment(A,B)`) | **no** | F | F | F | F | T | T | `UPDATE`, `REDEFINE` (new object) |
| L09 | delete | dead | F (null) | F | F | F | T | T | `REMOVE` |
| L10 | recreate the same label | **no** (new object) | F | F | F | F | T | T | `ADD` (new object) |
| L11 | Open (clearing `setXML`) | **no** | F | F | **T** | **T** | T | T | `reset()` ×2, `ADD`… |
| L12 | File → New | **no** (gone) | F (null) | F | **T** | **T** | T | T | `reset()`, `SET_MODE` |
| L13 | macro output `ml = F2Join(M1,M2)` (`AlgoMacro`) | — | T | T | T | T | T | T | — |
| L14 | macro output recomputed | yes | T | T | T | T | T | T | `UPDATE` |

Findings:

1. **`isInConstructionList` and `isLabelSet` are not currentness tests.** After
   undo, redo, Open and New they still answer true for the stale instance
   (L05, L06, L11, L12). `Construction.clearConstruction` clears the lists
   without resetting per-element indices (S, `Construction.java:4133-4178`);
   the P0 observation `OBS-R6P0-STALE-REFERENCE-FLAG` is confirmed at the base.
   `getConstruction()` is the same object throughout (CC) and only separates
   macro-kernel objects.
2. **LK and MB agree on every transition probed.** Both reject every stale
   instance and accept every live one, including the renamed and the
   macro-produced reference.
3. **LK caveats (S, I).** `lookupLabel` can return null for a live object when
   construction-protocol navigation sits before its step
   (`Construction.java:3500-3506`), and can be shadowed by the local-variable
   table (`:3353-3357`). Both are false negatives (reuse refused), never a
   wrong object. MB has no such caveat and is the established GeoCeDG
   precedent (`K7`).
4. **Compatible redefinition replaces the object.** Even `Line(A,B) →
   Line(A,Q)` produced a new instance (L07). Only a redefinition that the
   kernel resolves as identical or soft (`AlgoElement.setFrom`) keeps the
   instance (S, `Construction.java:1983-2010`); a redefinition of an object
   **with children** rebuilds the whole construction, replacing every object,
   including an unrelated remembered reference (S, `Construction.java:2122-2172`;
   not probed).
5. **Undo forgets.** Any undo or redo, even of an unrelated step, replaces every
   object; without a durable identity (forbidden) a remembered ordinary line
   cannot survive undo or redo. This is a fixed consequence of the gate, not a
   design choice.
6. **Undefined is not stale.** L02 keeps the same instance and passes LK and
   MB; only `isDefined()` reports it. Whether an undefined reference may be
   reused is an author question (`DQ-F2-5`).

## K9 — Lifecycle notifications

| Event | Mechanism at the base | Seen by |
|---|---|---|
| undo, redo | `Kernel.undo/redo` → `UndoManagerD.loadUndoInfo` → clearing XML load → `clearView` → `EventDispatcher.clearView()` → `listener.reset()` (`common/plugin/EventDispatcher.java:256-263`), then `UNDO`/`REDO` | P L05, L06 |
| Open, clearing `setXML`, document replacement | `doParseXML(clear)`; GeoCeDG `AppGeoCeDG` overrides (`loadXML`, `setXML`, `commitOpenedDocument`, `documentReplacementCommitted`) | P L11; S |
| File → New | `AppD.fileNew` → `clearConstruction` → `clearView`; GeoCeDG `AppGeoCeDG.clearConstruction` override (`AppGeoCeDG.java:1231-1270`) | P L12; S |
| redefine | `REDEFINE` with the **new** object; client events are disabled during the change itself (`AlgebraProcessor.java:673-695`) | P L07, L08 |
| delete | `REMOVE` (may be suppressed inside a spatial-adapter scope) | P L09 |
| rename | `RENAME`, `RENAME_COMPLETE` | P L04 |
| redefine with children | full rebuild with `notifyReset` | S only |

Undo and some redefinitions invalidate a held instance **without** a `REMOVE`
for it (S, P), so event hooks cannot be the authority. The P0 inference that
`reset()` fans out on New, Open, undo and redo is now **proven** (P), but a
lazy identity check at the point of use remains mandatory. GeoCeDG has
precedents for document-boundary resets through `AppGeoCeDG` overrides
(layer workspace, export area), and none for a client `EventListener`.

## K10 — Angle with Given Size

**Route (S).**

1. Clicks: EC `angleFixed` (`protected final`, EC:3154-3177) takes two points or
   one segment and calls `getDialogManager().showNumberInputDialogAngleFixed(…)`;
   it returns `null`, so the click itself stores no undo point.
2. Dialog: GeoCeDG runs `DialogManager3D` (P: `dialogManager =
   org.geogebra.desktop.geogebra3D.gui.dialogs.DialogManager3D`), a
   `DialogManagerD` subclass that does not override the angle method
   (`desktop/gui/dialog/DialogManagerD.java:377-385` builds
   `InputDialogAngleFixedD`).
3. Confirm: `InputDialogAngleFixedD.processInput` (private, :95-111) calls
   `DialogManager.createAngleFixed` (`common/main/DialogManager.java:1016-1057`),
   which evaluates the size with labels suppressed, negates it when clockwise,
   and calls `doAngleFixed` (:159-171).
4. `doAngleFixed` calls `ec.getCompanion().createAngle(points[0], points[1],
   num, clockWise)` (segment form: end point, start point) and **then**
   `storeUndoInfoAndStateForModeStarting()` (:170).
5. Companion: the GeoCeDG controller extends `EuclidianControllerFor3DD`, whose
   `newCompanion()` returns `EuclidianControllerFor3DCompanion` (P: GV1
   companion class). Its `public GeoAngle createAngle(GeoPointND, GeoPointND,
   GeoNumberValue, boolean)` (`common/geogebra3D/euclidianFor3D/EuclidianControllerFor3DCompanion.java:117-134`,
   public, non-final) delegates for 2D points in the xOy view to
   `EuclidianControllerCompanion.createAngle` → `AlgoDispatcher.angle(null, A,
   B, num, !clockWise)[0]` (`common/kernel/algos/AlgoDispatcher.java:768-801`),
   which builds the rotated point `AlgoRotatePoint` and the angle
   `AlgoAnglePoints`, labels both and sets `AngleStyle.ANTICLOCKWISE`.

**Where the angle becomes visible (S).** `AlgoAnglePointsND` creates the angle
through `newGeoAngle` → `AlgoAngle.java:75-79` → `setDrawableNoSlider()`, which
sets Euclidian visibility unconditionally (`GeoNumeric.java:275-279`).
Construction defaults do not touch numeric visibility
(`ConstructionDefaults.java:973-977`). The `P0` citation
`AlgoAnglePointsND.java:92-96` is the helper-algorithm constructor, not this
path (correction `ER-F2-5`). Nothing re-shows the angle after creation:
`angleFixed` returns `null`, and the dialog callback only closes the dialog.

**Probe (P, `angle`).** Driving the confirm path
(`DialogManager.createAngleFixed(kernel, "40°", false, …, {B, A}, null, GV1
controller)` in `MODE_ANGLE_FIXED`): one undo point is stored; created
`B' = AlgoRotatePoint(B, A)` (visible, label hidden) and `α =
AlgoAnglePoints(B, A, B')` (**visible**, label visible); XML `<show
object="true" label="true"/>`. After `setEuclidianVisible(false)` and a
clearing reopen the angle stays hidden (algebra-visible); the user can show it
again. `Angle(Q, P, 30°)` creates `Q'` and `β` both visible (unchanged path).

**Seams compared.**

| Seam | Feasibility at the base | Undo ordering | Reach | Size |
|---|---|---|---|---|
| **C — companion override** (`GCEC.newCompanion()` returns a GeoCeDG subclass of `EuclidianControllerFor3DCompanion` whose `createAngle` calls `super` and hides the returned `GeoAngle`) | S: all members public or protected, class non-final, public constructor | hide happens **before** the `doAngleFixed` undo store, so undo/redo restore the hidden state | tool path only: the 4-argument `createAngle` has no non-test caller other than `doAngleFixed` (S: `DialogManager.java:163,166`); commands, scripts, API, Algebra input and XML reload never reach it | one small GeoCeDG class + one override |
| D — `DialogManagerD` subclass via `GuiManagerD.dialogManagerFactory` (protected field, `GuiManagerD.java:173`) | S possible | the callback fires **after** the undo store (`DialogManager.java:170` before `:1053-1054`); hiding there leaves a visible angle in the stored state | every view | requires re-implementing `createAngleFixed`/`doAngleFixed` and a dialog subclass (`processInput` and dialog fields are private), ≈40 inherited lines duplicated |
| E — existing GeoCeDG hooks (`switchModeForProcessMode`, `App.storeUndoInfoAndStateForModeStarting`) | S: no fit (the former ends before the dialog confirms; the latter is called by every tool and does not know the created object) | — | — | — |

Constraint (S): `newCompanion()` runs inside the superclass constructor
(EC:521), so the GeoCeDG companion must not read controller fields.

**Persistence (S).** The writer emits `<show object="…">` from
`isSetEuclidianVisible()` for drawable angles (`XMLBuilder.java:57-60`;
`GeoAngle.isDrawable` true for an `AlgoAngle` parent, `GeoAngle.java:516-522`);
the reader applies it after the command rebuilds the angle
(`ConsElementXMLHandler.java:2098-2105`). A hidden tool angle is therefore
ordinary persisted visibility; **no new serialization semantics** are needed.
Older GeoCeDG and Classic read the same `<show object="false">` (I).

**Direction (S).** Clockwise is applied twice: the size is negated
(`DialogManager.java:1030`) and `posOrientation = !clockWise`, so the clockwise
angle is `C–A–B`; the seam does not touch it.

## K11 — Graphics View 2, other views and windows

- **GV2 uses the plain upstream controller.** P: GV2 controller =
  `org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianControllerFor3DD`
  (S: `GuiManager3D.newEuclidianView`, `desktop/geogebra3D/gui/GuiManager3D.java:258-264`;
  `GuiManagerGeoCeDG` does not override it). GV2 is reachable from the GeoCeDG
  View menu (`GeoCeDGHostMenuFactory.java:48`).
- Consequence: both targets implemented in the GeoCeDG controller cover **GV1
  only**. Covering GV2 needs a GeoCeDG-owned GV2 controller (a new subclass of
  `EuclidianControllerFor3DD`) installed by a `GuiManagerGeoCeDG` override of
  the protected `newEuclidianView`; that changes the controller class of GV2
  and needs its own regression (`DQ-F2-10`). F1 is GV1-only for comparable
  reasons (`DQ-F1-2`).
- The 3D view uses `EuclidianController3D` (`final orthogonal`, separate
  companion) and is outside the GeoCeDG perspectives (not characterized
  further).
- Each GeoCeDG window has its own `AppGeoCeDG`, kernel and GV1 controller (S:
  `AppGeoCeDG.newEuclidianController`, `AppGeoCeDG.java:1338-1341`), so any
  controller-owned memory is per window.
- **Classic** constructs upstream controllers (S: only `AppGeoCeDG` creates
  `GeoCeDGEuclidianController`); a GCEC-only change cannot reach Classic.

## K12 — Working-layer direct entry (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`)

**Recorded requirement** (A-2 closeout record, row of the enhancement): keep
the existing layer list/scroll and add a direct numeric entry, validated against
the same configured `0..L_MAX`; both routes modify the same authoritative
working-layer state; preferred owner `F2`; state `ENHANCEMENT — NOT A-2
BLOCKING — IMPLEMENTATION NOT AUTHORIZED`. The POST-E2 P4 closeout repeats the
assignment to `F2`.

**Current surface (S).**

- `GeoCeDGWorkingLayerChooser.dialog(app)` (`GeoCeDGWorkingLayerChooser.java:31-45`)
  shows `JOptionPane.showInputDialog` with the `Integer[]` of `0..maxLayer`
  (`maxLayer` from `app.getLayerWorkspace().getMaxLayer()`); its Javadoc states
  that no value outside the domain can be entered (:28-29). With 100 values
  Swing renders a scrolling list (I, JDK behavior).
- Entry points: `AppGeoCeDG.chooseWorkingLayer()` (`AppGeoCeDG.java:1166-1173`),
  the status-bar layer segment (`GeoCeDGStatusBar.java:171-178`) and
  `MODE_WORKING_LAYER` on empty space (GCEC:663-669, 882-901).
- Authority: `GeoCeDGLayerWorkspace.setWorkingLayer` rejects values outside
  `0..maxLayer` and never clamps (`GeoCeDGLayerWorkspace.java:64-79`, 233-237);
  `L_MAX = 99` (`AppConfigGeoCeDG.java:43,121-125`); session-only, reset on New.
- Pinned by tests: `PreG9BR6PlusA1LayerWorkspaceTest.theProductDialogOffersExactlyTheDomainAndNeverWraps`
  and `PreG9BR6PlusA2LayerDomainDesktopTest.theChooserAndTheStatusBarOfferTheConfiguredDomain`
  (both mock the 7-argument `showInputDialog`); the literal scan
  `theProductBoundIsTheOnlyNinetyNineLiteralOfTheLayerSites` constrains new
  code.

**Impact if included (I).** One Desktop production file (the chooser dialog),
localized texts in `application-profile.yml` (EN, ES) — a profile text change
that falls under the `workspace-profile-validation` leaf — two pinned tests
rewritten and one new test; no kernel, layer-domain, status-bar, mode or A-2
contract change; catalog unchanged (no new action). It shares no code path,
test or lifecycle concern with targets A and B. Disposition: `DQ-F2-9`.

## K13 — Affected tests and verification facts

- No GeoCeDG Desktop test references `MODE_PARALLEL`, `MODE_ORTHOGONAL` or
  `MODE_ANGLE_FIXED` (S, grep of `source/desktop/desktop/src/test`). The
  upstream `common-jre` `EuclidianControllerTest` exercises them through the
  headless upstream controller, which F2 does not reach.
- No profile action, catalog count, registry or inventory is affected by A or B
  (S: neither adds an action or text). `ENH` (`K12`) would touch profile texts
  and two pinned tests.
- `verification-levels.md` §12.8: `BOUNDED_PHASE` = one registered `PHASE` is
  sufficient; `DOCUMENTATION_STATUS_ONLY` = static checks only.

## K14 — Corrections to the `P0` F2 candidate

| Id | `P0` statement | At the base |
|---|---|---|
| ER-F2-1 | GCEC overrides at `:81`, `:431`, `:484`; parallel falls through at `:517-518` | line drift: `wrapMousePressed` :87, `setMode` :586, `switchModeForProcessMode` :640, `super` at :688 |
| ER-F2-2 | `Construction.isInConstructionList` at `:1273-1278` reads a per-object flag | now `:1295-1300`; for dependent geos it reads the parent algorithm's flag; the staleness conclusion is **confirmed** (L05, L06, L11, L12) |
| ER-F2-3 | `reset()` covers New, Open, undo, redo — INFERRED | **proven** for undo, redo, Open (`setXML`) and New; still insufficient alone (redefinitions replace objects with `REDEFINE`, not `reset()`) |
| ER-F2-4 | the reference-first click must suppress the press-time point-on-path creation | needed only if the memory is pre-selected; with release-time seeding (`K6` I1) the press sees an empty selection and #2610 is untouched |
| ER-F2-5 | angle visibility from `AlgoAnglePointsND.java:92-96` | that is the helper constructor; the tool path is `newGeoAngle` → `AlgoAngle.java:75-79` (plan §9.3 is correct) |
| ER-F2-6 | hide the angle "after `doAngleFixed` returns"; dialog-manager or companion seam | after `doAngleFixed` the undo point already holds the visible angle; only the companion seam hides it before the store; the dialog-manager seam would duplicate inherited code |
| ER-F2-7 | valid types: 2D vectors for both tools | Parallel also accepts 3D vectors (`line3D`); Perpendicular 2D only; non-linear functions are selectable but produce no labelled output |
| ER-F2-8 | (silent) | both targets in the GeoCeDG controller cover GV1 only; GV2 runs the upstream controller (`K11`) |
| ER-F2-9 | validity = defined and `lookupLabel(label) == ref` | LK is valid but has false-negative caveats; MB (identity scan) is the GeoCeDG precedent; "defined" is a separate author choice (`DQ-F2-5`) |

## K15 — Probe record

Route: scratch JUnit class `org.geocedg.desktop.F2PrepProbeTest` compiled into
the Desktop test source set by a Gradle init script; extensions
`G9U1TestApp.Lifecycle`, `EmptyUnitPreferences`, `DesktopLogger`; undo store
synchronized with `prepareUndoBaseline`/`commitUndoBaseline` and an
`UndoInfoStoredListener`; inputs through
`processAlgebraCommandNoExceptionHandling` with `ErrorHelper.silent()`.

| Output | Content | SHA-256 |
|---|---|---|
| `f2-probe.lifecycle.txt` | `K8` L00–L15 | `3eadde9c919f009fcc17a2ee5e0f1353fc11d164ec63440a50d9249504d4ff72` |
| `f2-probe.tools.txt` | `K4` T01–T16 (both tools) | `2b463edf262f365a185422a3a5a35e266f883b901edcee8aeaf3b03eee66f59c` |
| `f2-probe.tools.quick-repeat-run10.txt` | T15q (Redefine dialog on a quick repeat) | `3209584b0856197c97efbcc83aebd0accfba68ab1c33e986fd73dd73d5d8c90a` |
| `f2-probe.angle.txt` | `K10` probe, controller and companion classes of GV1/GV2 | `1dc3bca4558d65b013e53543f8d54019674b55c73555d16d6e77f2d79be11047` |
| `f2-probe.diag.txt` | T11/T13 diagnostics | `ade50f8be4f1b8d126386ea574b9a88304288601750879d83615e84ecee8b4d2` |
| probe source | `F2PrepProbeTest.java` | `e33c0b342975ad0f1e8eade41056e6e5dfdc73935b39031df4c4d7f16b998ea6` |
| init script | `f2probe.gradle` | `6474c22036f1d1cb2f457d25a3bffeb09b31732e9d727b68c462df4a672e21c9` |

Final run: 4 tests, 0 failures (exit 0). Earlier runs failed only in the
harness teardown (`A real window requires its own lifecycle`) after T15q opened
the Redefine dialog, and in a scene whose polygon auto-labelled its edges
`f, g, h` before the functions `g`, `h` were defined; both were probe defects,
fixed before the recorded outputs. Static mocking of `JOptionPane` was dropped
because it prevented `Polygon` creation in the probe host. After the runs:
`:desktop:desktop:compileTestJava` without the init script (exit 0); the probe
test-result XML and two compile-stash copies removed; `git status` clean.

## K16 — Points left to the author

The ten questions `DQ-F2-1` to `DQ-F2-10` of the
[decision preparation record](pre_g9b_r6_plus_f2_author_decision_preparation_record.md).
No inherited-class change is required by any recommended alternative; the GV2
alternatives of `DQ-F2-10` add a GeoCeDG-owned GV2 controller class and a
`GuiManagerGeoCeDG` override, and would be the first F2 change outside GV1.

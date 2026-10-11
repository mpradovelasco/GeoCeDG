# PRE-G9B-R6-plus-F2-R1 — test matrix and author-smoke plan

- Status: **PART OF THE AUTHOR-APPROVED F2-R1 DESIGN** (record v1.1 §5); implementation not authorized
- Design: [reconciled dual-activation design candidate](../architecture/pre_g9b_r6_plus_f2_r1_dual_activation_reconciled_design_candidate.md)
- Decisions: [F2-R1 author-decision record](pre_g9b_r6_plus_f2_r1_author_decisions_record.md) v1.1
- Nothing here is implemented or authorized. `selfApproved = false`.

## 1. Automated test matrix (for an authorized implementation)

Toolbar activations use the real GeoCeDG toolbar and events **posted to the
system `EventQueue`** (so Swing fills the `ActionEvent` modifiers, Stage 1
probe precedent); flyouts use a shown popup where the host allows it; gestures
drive the real Graphics View 1 controller with press/release events; undo uses
the synchronous baseline and store listeners.

| Id | Obligation |
|---|---|
| `T-R1-ACT-NORMAL` | a plain toolbar click on Parallel and on Perpendicular selects `NORMAL`; a point click waits for an explicit reference although a valid slot exists |
| `T-R1-ACT-MEMORY` | Ctrl + click selects `MEMORY_ENABLED`; a point click reuses the valid slot (F2 obligations re-run in this behaviour) |
| `T-R1-LATCH` | Ctrl released after activation and Ctrl pressed during construction change nothing |
| `T-R1-REACT-SAME` | reclick with the same behaviour keeps a pending point, reference and empty-space point (inherited) |
| `T-R1-REACT-CHANGE` | reclick that changes the behaviour cancels the pending gesture: selection empty, unfinished free point removed, completed constructions intact, no undo or redo step, saved/unsaved state coherent (`FR-3`), slots unchanged, mode unchanged |
| `T-R1-FLYOUT` | flyout selection of Perpendicular with and without Ctrl (shown popup or posted item release) |
| `T-R1-KEYBOARD` | Space on the focused toolbar button → `NORMAL`; Ctrl+Space → no activation |
| `T-R1-INDIRECT` | menu / registry and pinned user-tool activations → `NORMAL`, **including** when the tool is already active in `MEMORY_ENABLED` (pending gesture cancelled) |
| `T-R1-SCRIPTING` | scripting `setMode` → `NORMAL` also for the active tool if `FR-2` provides a GeoCeDG-owned seam; otherwise documented as host synchronization (behaviour unchanged) |
| `T-R1-SYNC` | an unmarked same-mode `app.setMode` (host synchronization) changes neither the behaviour nor the partial selection |
| `T-R1-TOKEN` | an unused toolbar request (press on the flyout triangle) never leaks into a later indirect activation |
| `T-R1-RESET` | another tool, Escape and New reset both tools to `NORMAL`; valid slots survive tool changes and Escape; the F2 lifecycle alone clears them |
| `T-R1-LANGUAGE` | a language change (EN ↔ ES) keeps the active Parallel/Perpendicular tool, its behaviour and both slots; the status text is re-rendered in the new language (`FR-1`) |
| `T-R1-NO-RESET` | dock focus changes, Graphics View 2 creation, undo, redo and clearing `setXML` do not change the behaviour |
| `T-R1-NORMAL-NO-READ` | in `NORMAL` a valid slot is not injected for point-first or empty-space clicks |
| `T-R1-NORMAL-NO-WRITE` | in `NORMAL` a certified construction with another reference leaves the slot unchanged |
| `T-R1-NORMAL-NO-CLEAR` | in `NORMAL` constructions never clear a valid slot |
| `T-R1-STALE` | a slot invalidated while `NORMAL` (undo, delete, redefine, New/Open) is not reused after a later Ctrl activation |
| `T-R1-SLOTS` | Parallel and Perpendicular slots and behaviours are independent |
| `T-R1-STATUS` | the status-bar segment is shown (laid-out bounds in the single-row bar, not only `isVisible`) only while the active tool is `MEMORY_ENABLED`, with "Parallel Line · Memory" / "Paralela · Memoria" and "Perpendicular Line · Memory" / "Perpendicular · Memoria"; hidden in `NORMAL`, for other tools and after a behaviour change; never depends on whether a slot is held; profile validation accepts the texts (`FR-4`) |
| `T-R1-NO-DOC` | behaviour changes create no object, no undo point and no XML difference |
| `T-R1-INHERITED` | issue #2610, double-click Redefine, preview and hit priority unchanged in both behaviours |
| `T-R1-UNCHANGED` | Angle with Given Size, commands, Graphics View 2 and Classic unchanged |
| `T-F2-*` | the existing F2 obligations (`PreG9BR6PlusF2AuthoringMemoryTest`) executed with `MEMORY_ENABLED` activation |

## 2. Author-smoke plan (not performed by the agent)

Windows 11, Graphics 1, English and Spanish (repeat items 1, 3, 6 and 9 in the
other language).

1. Click **Parallel line** normally. Click a line, then a point: a parallel
   appears. Click another point: it waits for a reference (no memory used). The
   status bar shows nothing new.
2. **Ctrl + click** Parallel line. The status bar shows "Parallel Line · Memory"
   (or the Spanish equivalent). Click a line, then a point; click two more
   points: each gets a parallel to that line at once.
3. Release Ctrl, press and release Ctrl while clicking points: the behaviour does
   not change.
4. Click the active Parallel tool normally (no Ctrl): the indicator disappears;
   a point click waits for a reference. Ctrl-click it again: the indicator
   returns and the remembered line is reused.
5. With memory enabled, click one point (or empty space) and then reclick the
   tool **without** Ctrl: the half-made gesture disappears (an empty-space point
   is removed), and Undo is not affected. Reclick with Ctrl when already in
   memory mode: a pending point stays.
6. Open the flyout and choose **Perpendicular** normally: no indicator, normal
   tool. Choose it again with Ctrl held: indicator for Perpendicular; its own
   memory, independent of Parallel's.
7. Change the language (EN ↔ ES) while Parallel is memory-enabled: the tool,
   the memory mode and the remembered line stay; the indicator switches language.
7b. Press Esc or choose another tool: the indicator
   disappears; returning without Ctrl is normal; returning with Ctrl reuses the
   still-valid reference.
8. In normal mode build a parallel to a different line, then Ctrl-activate: the
   old remembered line is still used (normal mode did not replace it).
9. Undo, Redo, File → New and Open while in memory mode: the next point click
   asks for a reference (memory invalidated); the indicator only reflects the
   mode, not a reference.
10. Activate Parallel from the Construction menu while it is already active in
    memory mode: it becomes normal.
11. Tab to the toolbar and press Space on the Parallel button: normal mode.
12. Angle with Given Size, `Angle(B, A, 40°)` and Graphics 2: unchanged.
13. Optional (if available): macOS with Cmd + click; Linux with Ctrl + click —
    report the outcome; the design does not claim these.

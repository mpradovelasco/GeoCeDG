# GeoCeDG document insertion and style application

| Field | Value |
|---|---|
| Status | **PROPOSED — TECHNICAL CANDIDATE PENDING AUTHOR REVIEW** (not normative until an author closeout approves it) |
| Version | `1.0` |
| Owner phase | `PRE-G9B-R6-plus-F3` (File → Insert surface; `DEBT-R6PLUS-FILE-INSERT-SURFACE`) |
| Governing author decisions | [F3 author design closeout record](../../../docs/validation/pre_g9b_r6_plus_f3_author_design_closeout_record.md) (Q1–Q7, `DQ-F3-1` to `DQ-F3-24`, `P1`–`P7`, FR-F3-1 to FR-F3-8); [implementation authorization record](../../../docs/validation/pre_g9b_r6_plus_f3_implementation_authorization_record.md) |
| Approved design transcribed | [recovery design addendum successor](../../../docs/architecture/pre_g9b_r6_plus_f3_recovery_design_addendum_successor_candidate.md); [restricted insertion design successor](../../../docs/architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_successor_candidate.md) and [candidate](../../../docs/architecture/pre_g9b_r6_plus_f3_restricted_insertion_design_candidate.md); [reconciled amendment texts](../../../docs/validation/pre_g9b_r6_plus_f3_normative_amendment_proposals_reconciled.md) (`P1`, `P6`, `P7`) and [proposals](../../../docs/validation/pre_g9b_r6_plus_f3_normative_amendment_proposals.md) (`P2`–`P5`); all `DESIGN — AUTHOR APPROVED` or `APPROVED` |
| Normative inputs used, not restated | [unit-system specification](../units/unit-system.md) §11 (`D1` provenance and notice); `geocedg/specs/operations/verification-levels.md` §12.8 |
| Serialization effect | none (no format, XML, parser, preference or unit change) |

## 0. Status and normative language

This specification transcribes the approved F3 design into one durable contract
for the technical candidate. It changes no approved decision. `MUST`, `MUST NOT`,
`SHOULD` and `MAY` have their usual meaning. Where an author decision and this
text differ, the decision prevails and this text is defective. Provenance
markers: **[AD]** author decision, **[NI]** normative inheritance, **[ID]**
implementation design inside the approved scope (reviewable, not an author
decision), **[OBS]** recorded observation.

## 1. Surface

1.1 **[AD]** (`DQ-F3-1` N1, `DQ-F3-23`) The File menu has an **Insert /
Insertar** submenu, directly after the Open group, with two independent actions:
`document.insert-file` "Insert File… / Insertar archivo…" and
`document.apply-styles` "Apply Styles… / Aplicar estilos…". No accelerator.
Catalog: 133 actions.

1.2 **[AD]** (`DQ-F3-2` A1, `DQ-F3-11` F-1) The GeoCeDG Open dialog offers one
filter, the document filter (`.cedg`, `.ggb`, `.ggt`, `.html`, `.htm`); Insert
File and Apply Styles are not file types of Open. A selected name that does not
exist resolves to `.cedg`, `.ggb`, `.ggt`, `.html`, `.htm` in that order; Cancel
does nothing. Classic and the v1 fallback keep the inherited dialog.

1.3 **[AD]** (`DQ-F3-4` OFF-B) OFF is not offered in the GeoCeDG UI: an explicit
`.off` file in Open, Insert File, Apply Styles, a file drop or any other
`doOpenFiles` caller is refused with a message; a stem never resolves to
`.off`. The internal OFF support is unchanged and reachable from Classic.

1.4 **[AD]** (`DQ-F3-3` X1) Insert File and Apply Styles use a single-selection
chooser with one filter "GeoCeDG documents (.cedg, .ggb)" in the current
directory, without a save prompt; a missing name tries `.cedg`, then `.ggb`.
Accepting a container does not admit its contents.

1.5 **[AD]** (`DQ-F3-10` H-1, H-3) The source is loaded into a hidden helper
document that never publishes its file to the recent-file list and is released
after every outcome (animation stopped, AWT and keyboard registrations
removed). The inherited `AppD.insertFile` and `AppD.applyTemplate` are never
called by GeoCeDG.

## 2. Insert File: restricted native insertion R

2.1 **[AD]** (`P1` item 4) Insert File inserts an admissible source construction
completely or not at all. Admission and a strict dry run precede any mutation;
existing objects are never overwritten, replaced or rebound.

2.2 **[AD]** Admission (`P4`, `DQ-F3-14`), evaluated on the loaded helper.
Every object of the source construction must match a row:

| Family | Rows | Condition |
|---|---|---|
| elementary | free and dependent 2D points, lines, rays, segments, vectors, circles and ellipses, functions and function chains, polygons with their edges, angles from three points, numbers, booleans, dependent texts, lists and sequences of admitted elements, show conditions inside the insertion | none |
| Locus V2 family | Locus V2, translated locus, semantic points, lengths, intersection results and token-selected points (algorithms of the Locus V2 kernel package) | the target may create Locus V2; no macros in the source; each participant receives a new durable identity |
| Spline V2 | `SplineV2` with its promoted list and degree | as Locus V2 |
| native dimensions | `LinearDimension`, `AlignedDimension` outputs | texts follow the target units; values never scaled |
| IsoABorder | sheet outputs | the session export link is never imported |
| macro-generated objects | objects of a macro whose output class is elementary | M1 (§3) |

Rejected before mutation: objects with any script; 3D objects; projection
bindings and spatial objects; static (free) texts, arcs, images, buttons, input
boxes, CAS cells and every class or algorithm not listed; a source with identity
records and macros (`DQ-F3-24`); an empty source; a source that fails the native
preflight. **[ID]** Rows are matched by exact object class and parent-algorithm
family (`GeoCeDGInsertionAdmission`).

2.3 **[AD]** (`DQ-F3-5` L1) Labels of inserted objects that exist in the target
are renamed deterministically by the kernel's indexed labels; no label dialog.
Labels inside static strings and JavaScript are not rewritten (scripts are
rejected).

2.4 **[AD]** Dry run: the same insertion on a scratch helper loaded from the
target's native archive, strictly verified (§2.6); any failure rejects before
mutation.

2.5 **[AD]** Commit: the kernel's construction copy (`insertFrom(helper, target,
conflicts, overwrite = false)`) through the operation-scoped insertion buffer
(§4). **[NI]** The inherited paste clears the selection and activates the Move
tool; F3 does not claim them unchanged.

2.6 **[AD]** Strict verification: count; pairwise object class and parent
algorithm class; layer; value (points, numbers, booleans, list sizes,
definedness); closure (every labeled input of an inserted object is inserted);
identity records (a source participant yields a participant with a fresh
durable identity, different from the source's and from every target identity);
macro bindings (an inserted macro object is bound to the target's registered
macro of that name) and macro digests (§3).

2.7 **[AD]** Success: exactly one undo step on the restored history prefix (the
Redo states held aside are discarded as for any new action); the destination
document identity (UUID) is kept; the document becomes unsaved; the `D1`
notice is shown from the inserted document's provenance.

## 3. Macros: M1 and MU-1

3.1 **[AD]** (`P3`) The required macros are those of the macro-generated objects
and, transitively, every macro used inside their definitions; unused source
macros are not inserted. For each required macro: absent in the target →
transferred once; present with an equal product definition digest
(`GeoCeDGUserToolLibrary`) → the target's macro is reused; otherwise →
rejected before mutation, naming it. Name equality alone never establishes
equivalence; a malformed definition is rejected.

3.2 **[ID]** The buffer registers the transferred closure in the source's own
registration (dependency) order before the construction is evaluated; the
inherited paste then has no macro left to add.

3.3 **[AD]** (`DQ-F3-22` MU-1) After Undo of a successful insertion, transferred
macros stay registered and are saved with the document. Macros transferred by a
failed insertion are removed.

## 4. Operation-scoped buffer and `D1` (`P2`)

4.1 **[AD]** Insert File never uses or replaces the window copy buffer or the
global clipboard. It copies through a buffer of its own whose provenance is the
inserted document's unit state, recorded by the `D1` rule, consumed once by that
insertion's notice and discarded when the operation ends. Geometry is never
rescaled, coordinates never converted, target units never modified. After an
insertion Ctrl+V pastes what the user last copied, with that copy's provenance.

## 5. Transaction, recovery and identity (`DQ-F3-21`, `P6`, `P7`)

5.1 **[AD]** The GeoCeDG undo manager (`GeoCeDGUndoManager extends UndoManagerD`)
serves every GeoCeDG document through a stateless
`AppGeoCeDG.getUndoManager(Construction)`; its overrides observe and delegate
unchanged. Classic keeps the inherited manager.

5.2 **[AD]** Accounting C1: `storeUndoInfo` counts a store before scheduling it
(a scheduling failure degrades); synchronous callers (`storeAndNotifyUnsaved`,
`commitUndoBaseline`) are marked and never counted; only the store body's
success path (an unmarked `pruneStateList`) allows a decrement in
`onStoreUndo`; an exception exit degrades without decrementing; an `Error`
leaves the store counted; only `commitUndoBaseline` resets. Invariant:
`pending == 0 && !degraded ⇒ no store of the current history generation can
still change the history`.

5.3 **[AD]** Barrier block, on the event dispatch thread, synchronized on the
undo manager, before any mutation: quiescence (`pending == 0`, not degraded;
otherwise a bounded wait outside the monitor, then rejection); the verified
native recovery snapshot (§6); the original history detached (commands moved,
never copied); a committed baseline (every earlier store cancelled) captured by
reference as the reload source and preflighted.

5.4 **[AD]** After the commit, the paste's own store is identified by
generation and content (identity-bearing payloads); the outcome is decided by
the strict verification.

5.5 **[AD]** Recovery:

| Outcome | When | Result |
|---|---|---|
| REJECTED BEFORE MUTATION | preflight, admission, dry run, uncertain accounting, snapshot failure | nothing changed |
| FAILED — RECOVERY CERTIFIED (K1-S) | payload without identity records | created objects removed in reverse order, transferred macros removed, UUID and saved state restored, held history re-attached; certificate equal and live instances, Parallel/Perpendicular memory and export link kept |
| FAILED — RECOVERY CERTIFIED WITH DECLARED SESSION RESET (K1-R-H) | identity-bearing payload, a failed removal, or an inherited rollback that rebuilt the drawing | guarded reload of the barrier baseline (no history access, no inherited fallback, one retry), macros, UUID and saved state restored, history re-attached; certificate equal; declared reset of instances, selection, remembered references and, when it existed, the export link |
| SERIOUS RECOVERY FAILURE — STATE NOT CERTIFIED | inherited rollback failure, two failed reloads, a missing or unexpected store, a failed certificate | history re-attached (or kept aside when attaching is unsafe), snapshot owned by the isolation (§7) |

The certificate compares the normalized full document XML (root UUID, units and
hidden layers included), durable identities, macro definition digests, saved
state and file association, and the history (same command objects, cursor,
labels, grouping, snapshot files); runtime instances are compared separately.

5.6 **[AD]** Attach block: re-attachment only with `pending == 0`, not degraded
and exactly the expected temporary list; otherwise a closing barrier first. The
history is installed first; the action refresh is best effort. A cleanup failure
is added as suppressed to the original failure and never replaces it.

5.7 **[ID]** The orchestration's own undo store is made and identified before
the history is re-attached, so the success step is certified before the attach.

## 6. Native recovery snapshot (`P7`, G1)

6.1 **[AD]** Captured inside the barrier with the public native writer
(`writeGeoGebraFile(OutputStream, false)`): construction, macros, images,
library JavaScript, construction defaults, GUI and document settings, hidden
layers, units and the document UUID. Verified by the native preflight and the
root identity; provenance: operation id, capture time, UUID, current file, saved
state. Never the undo archive; never written over the current file.

6.2 **[AD]** Released after a success or a certified recovery; after a serious
failure ownership moves to the isolation and the snapshot is released only after
Reopen or Close completed.

## 7. Serious-failure isolation (`P6`, `P7`)

7.1 **[AD]** While isolated: modal dialog; no editing (**[ID]** pointer edits in
the GeoCeDG Graphics controller and Algebra input submissions are also refused in
code, not only by the modality), Save, Save As, export,
Insert File, Apply Styles, copy or paste, New, Open, file drop, global keys;
update scripts blocked and global listeners suppressed; animation stopped;
Undo and Redo suspended (`Kernel.setUndoActive(false)`, history kept); every
inherited save-changes prompt route is refused; the drawing is marked unsaved.

7.2 **[AD]** Exits, none through an inherited save prompt: **Save verified
copy…** (repeatable) writes the snapshot to a new path chosen by the user
(refused for the current file, explicit confirmation for another existing
file), atomically, with the published bytes re-validated and the original UUID;
the session's file association is unchanged. **Reopen document** re-enables
Undo first, then opens the existing file natively; a failure re-isolates.
**Close document** closes the window without a prompt (an embedded host
discards the drawing). The uncertified drawing is never written or exported.

7.3 **[AD]** Messages (`P6`): rejection "*name* was not inserted: *reason*.";
K1-S "… The drawing is unchanged."; reload "… The drawing was restored to its
state before the insertion; the selection and the remembered references of
Parallel/Perpendicular were reset[, and the export area was unlinked from its
ISO A sheet]."; serious "The drawing could not be verified after a failed
insertion of *name*. You can save the last verified version to a new file,
reopen the document from its file, or close it." No message claims partial
success.

## 8. Apply Styles (T0, `P5`)

8.1 **[AD]** Copies object colour, line style, line thickness, point style,
point size and fill opacity from the source document's construction default of
each object's default type to the selected objects, or to every object when
nothing is selected, except native-dimension, IsoABorder and Locus V2 family
outputs.

8.2 **[AD]** Nothing else changes: no geometry, label, layer, hidden-layer
membership, caption, label offset, label visibility, fixed flag, visibility,
show condition, dynamic colour, construction default, labeling style or unit.
One undoable step; the document becomes unsaved; a source that cannot be
validated and loaded changes nothing. `TemplateHelper` is not used.

## 9. Observations

9.1 **[OBS]** The inherited construction copy reserves the source labels as
protected labels of the target construction (session state, never serialized,
cleared at the next New, Open or reload); K1-S does not reset them.

9.2 **[OBS]** Text objects keep line thickness 0 in memory but reload with the
default 5: line thickness of texts is not serialized (inherited; affects Undo,
Redo and reopen equally, not T0).

9.3 **[OBS]** Reopen and Close are verified on embedded hosts; the modal dialog,
window close and file drop onto a real window are author-smoke items.

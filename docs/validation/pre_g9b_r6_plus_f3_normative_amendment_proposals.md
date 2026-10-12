# PRE-G9B-R6-plus-F3 — normative amendment proposals

- Activity: `PRE-G9B-R6-plus-F3-AUTHOR-DECISION-RECONCILIATION` (author
  instruction of 2026-10-12)
- Status: **PROPOSALS — NOT APPROVED — NOT APPLIED**
- Base: `T_F3_RISK` `8b41af11316f90d7a068ce6af4859213240ce6a6`
- Approved texts these proposals would amend (unchanged): `F3-D2` in the
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md);
  `DQ-D1-7` in the [D1 prompt closeout record](pre_g9b_r6_plus_d1_prompt_closeout_record.md)
  (line 105)
- Decisions: [final decision preparation record](pre_g9b_r6_plus_f3_final_author_decision_preparation_record.md)
- `selfApproved = false`

Each block below is candidate text. It becomes normative only by an explicit
author decision naming it; until then the approved texts govern. Bracketed
alternatives depend on the named pending decision.

## P1 — `F3-D2` (replacement text for items 1–5; items 6–9 unchanged)

> 1. GeoCeDG provides a **File → Insert / Archivo → Insertar** surface.
> 2. It holds two independent actions, **Insert File… / Insertar archivo…** and
>    **[Apply Styles… / Aplicar estilos… — `DQ-F3-23` S1 | Apply Template… /
>    Aplicar plantilla… — S2]**, removed from the GeoCeDG Open dialog.
> 3. Both accept the `.cedg` and `.ggb` containers only. Accepting a container
>    does not admit its contents: contents are admitted only by the contracts of
>    items 4 and 5.
> 4. Insert File inserts an admissible source construction into the current
>    document completely or not at all, under the restricted insertion contract
>    (P3, P4, P6): admission and a strict dry run precede any mutation; existing
>    objects are never overwritten, replaced or rebound; conflicting labels of
>    inserted objects are renamed deterministically; required macros follow P3;
>    the `D1` provenance and notice are kept through an operation-scoped buffer
>    (P2) and coordinates are never scaled; failures follow P6.
> 5. The style operation follows P5: it imports no geometry, never changes the
>    target units and is one undoable step.

## P2 — `DQ-D1-7` (replacement text)

> Ordinary Copy, Cut and Paste are unchanged: the window copy buffer carries the
> source document's unit provenance, is replaced and cleared with it, and a later
> Ctrl+V of that buffer keeps it. Document insertion (F3 Insert File) does not
> use or replace the window copy buffer. It copies the inserted document through
> an operation-scoped insertion buffer whose provenance is the inserted
> document's unit state, recorded by the same `D1` provenance mechanism and
> consumed once by that insertion's notice; the buffer and its provenance are
> discarded when the operation ends, whether it succeeds or fails. Different
> physical construction meanings show the same non-blocking notice as an
> ordinary cross-document paste. Geometry is never rescaled, coordinates are
> never converted and the target units are never modified. After an insertion,
> Ctrl+V pastes what the user last copied, with that copy's provenance.

## P3 — M1 macro policy

> The macros required by an inserted construction are its macro-generated
> objects' macros and, transitively, every macro used inside their definitions.
> Unused source macros are not inserted. For each required macro:
> (a) if the target has no macro with that command name, it is transferred once;
> (b) if the target has one whose product definition digest
> (`GeoCeDGUserToolLibrary` definition digest) is equal, the target's macro is
> reused and nothing is transferred; (c) otherwise the insertion is rejected
> before any mutation, naming the macro. Name equality alone never establishes
> equivalence. Every inserted macro-generated object must be bound to the
> transferred or reused macro and recompute to the source value; a degraded
> (free) result rejects the insertion. A source that needs both identity records
> and macros is rejected (`DQ-F3-24`). Macros transferred by a failed insertion
> are removed. [After Undo of a successful insertion, transferred macros remain
> registered as document tools and are saved with the document — `DQ-F3-22`
> MU-1.]

## P4 — Admissibility (summary of the matrix)

> Admitted without condition: free and dependent 2D points, lines, rays,
> segments, vectors, circles, ellipses, functions and function chains, polygons
> with their edges, angles from points, numbers, booleans, dependent texts,
> lists, sequences, and show conditions whose condition object is inserted.
> Admitted with conditions: the Locus V2 family (locus, translated locus,
> semantic point, length, intersection result, token-selected intersection
> point) and Spline V2, when the target allows Locus V2 creation and the source
> needs no macros, each inserted participant receiving a new durable identity;
> Linear and Aligned dimensions, whose texts follow the target document's units
> without scaling the value; IsoABorder, whose session export link is never
> imported; macro-generated objects under P3. Rejected: objects with scripts;
> 3D objects; projection bindings and spatial objects; anything not listed.

## P5 — Style operation (T0)

> The style operation copies object colour, line style, line thickness, point
> style, point size and fill opacity from the source document's construction
> defaults of the object's default type to the selected objects, or to every
> object when nothing is selected, except native-dimension, IsoABorder and
> Locus V2 family outputs. It changes no other property — in particular no
> geometry, label, layer, hidden-layer membership, caption, label offset, label
> visibility, fixed flag, visibility, show condition, dynamic colour,
> construction default, labeling style or unit — and it is one undoable step
> that marks the document modified. A source that cannot be validated and loaded
> changes nothing.

## P6 — Insertion failure and recovery diagnostics

> **Rejected before change** (admission, dry run): "*name* was not inserted:
> *reason*." Nothing in the drawing, its undo history or the session changed.
> **Failed after commit, compensated without reload** (K1-S): "*name* was not
> inserted: *reason*. The drawing is unchanged." **Failed after commit,
> restored by reload** (K1-R, if approved): "*name* was not inserted: *reason*.
> The drawing was restored to its state before the insertion; the selection and
> the remembered references of Parallel/Perpendicular were reset[, and the
> export area was unlinked from its ISO A sheet]." **Serious failure** (the
> restoration failed or could not be verified): "The drawing could not be
> verified after a failed insertion of *name*. Save it under a new name or
> reopen it." The document is marked modified; no further automatic change is
> made. No message after a change ever claims partial success.

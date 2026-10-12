# PRE-G9B-R6-plus-F3 — author-decision preparation record

- Activity: `PRE-G9B-R6-plus-F3-PREP` (author instruction of 2026-10-12)
- Status: **QUESTIONS PREPARED — ALL PENDING — NO DECISION RECORDED**
- Base: published `main` `e70718fec3612814144cd405d46f6c08864ceadd`, tree
  `26817bfc6a72b378e4babc1cd08efd0d734473c9`
- Evidence: [characterization report](pre_g9b_r6_plus_f3_preparation_characterization_report.md)
  (`K0`–`K15`, anomalies `CF-F3-1` to `CF-F3-12`, open items `OTI-F3-1` to
  `OTI-F3-5`); design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md);
  contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md)
- Prior author decisions on F3: `F3-D1` to `F3-D6` of the
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md)
  (2026-10-03), unchanged by this record, and the scope restated by the
  instruction of 2026-10-12. They are cited below as **author-fixed**.

This record formulates questions and recommendations. It decides nothing. A
recommendation is the agent's proposal; only an explicit author instruction
disposes of a question. `selfApproved = false`.

Each entry is tagged by its nature — **AUTHOR DECISION** (a choice only the
author makes), **CHARACTERIZATION EVIDENCE** (what the product does at the
base), **IMPLEMENTATION DESIGN** (how a choice would be built) and
**OPEN TECHNICAL ITEM** (feasibility the implementation entry gate must
establish) — and gives the author-fixed requirements, the observed behavior,
the evidence, the alternatives, the recommendation, the architectural and
compatibility consequences, the tests (IDs of design §11) and the effect on the
verification class.

## Summary

| Id | Subject | Recommendation (pending) | Class effect |
|---|---|---|---|
| `DQ-F3-1` | submenu structure and action identities | group `file-insert` ("Insert"/"Insertar") after the Open group; actions `document.insert-file`, `document.apply-template`; targets `geocedg.document.*` | none |
| `DQ-F3-2` | chooser architecture and inherited seams | GeoCeDG-owned Open override and GeoCeDG insertion chooser; no inherited change | none |
| `DQ-F3-3` | formats, default extension, fallback | one filter `.cedg`+`.ggb`; stem → `.cedg`, then `.ggb`; refuse other extensions; single selection | none |
| `DQ-F3-4` | OFF UI policy | OFF-B: no filter, refuse explicit and fallback `.off` in GeoCeDG Open with a message; internal support unchanged | none |
| `DQ-F3-5` | objects, macros, label conflicts | keep inherited Rename/Overwrite/Cancel; fail closed on a conflicting macro name (subject to `OTI-F3-2`) | none |
| `DQ-F3-6` | undo, cancellation, partial failure | one undo point per successful insertion; failures and Cancel leave the document unchanged and report no success | none |
| `DQ-F3-7` | `D1` provenance and notice | existing mechanism unchanged; end-to-end tests | none |
| `DQ-F3-8` | Apply Template unit, geometry and state invariants | invariants asserted; one undo point and unsaved state; inherited style semantics kept and documented | none |
| `DQ-F3-9` | Apply Template and layers | L-A preserve each object's layer (GeoCeDG snapshot and restore) | none |
| `DQ-F3-10` | helper, recent list, clipboard | helper never publishes to the recent list; helper registrations released; buffer replacement kept; post-insert placeholder labels recorded outside F3 | none (P2 would add an inherited change) |
| `DQ-F3-11` | Classic and legacy profile | Classic unchanged; fallback keeps the inherited Open dialog | none |
| `DQ-F3-12` | architecture, class and acceptance | Desktop-only; `INTEGRATED_PHASE`, registered `PHASE` + `INTEGRATION` | confirms the planned class |

## DQ-F3-1 — File → Insert submenu structure and action identities

**Nature.** AUTHOR DECISION (identities, placement, texts) with IMPLEMENTATION
DESIGN.

**Author-fixed.** File → Insert / Archivo → Insertar holding Insert File /
Insertar archivo and Apply Template / Aplicar plantilla, moved out of the Open
dialog, with independent, identifiable actions (`F3-D2` items 1–2).

**Observed.** No File submenu; 19 flat File actions; no insertion action or
target; a `group` menu entry already renders a submenu (report `K3`).

| Alternative | Consequence |
|---|---|
| **N1 — presentation group `file-insert` (`name_key` `Group.File.Insert`: "Insert" / "Insertar") as a `group` entry directly after the `file-open` actions entry, before the first separator; actions `document.insert-file` and `document.apply-template` (kind `document-action`, cluster `document-lifecycle`), targets `geocedg.document.insert-file` and `geocedg.document.apply-template`, localization refs `geocedg.document.InsertFile` and `geocedg.document.ApplyTemplate` (names "Insert File…" / "Insertar archivo…", "Apply Template…" / "Aplicar plantilla…"); no accelerator, no toolbar (recommended)** | the submenu sits with Open, which the operations leave; catalog 131 → 133; the product namespace says the orchestration is GeoCeDG-owned (precedent `geocedg.document.units`) |
| N2 — the same group as its own section between two separators after Open | visually separate; one more separator pair; same pins |
| N3 — targets `host.document.insert-file` / `host.document.apply-template` | stresses reuse of the inherited operations; misnames the GeoCeDG chooser and policy |

**Recommendation.** N1. The ellipsis follows the Open… convention because both
actions open a chooser.

**Consequences.** Profile (one group, one File menu entry, two actions, a
cluster placement, ten action texts and one group text in EN and ES),
`GeoCeDGActionRegistry` (allow-list and `execute`), `GeoCeDGProfile.java:340`
pin and message, the eleven 131-pins (report `K3`), the File-flatness test
`G9U1WorkspaceSurfaceTest.java:182-191` amended to admit exactly the declared
`file-insert` submenu, the validator amendment `PRE-G9B-R6-plus-F3` with
re-pinned hashes. No new command, mode or geometric identifier.

**Tests.** `T-F3-MENU`, `T-F3-L10N`, `T-F3-PINS`. **Compatibility.** Classic
and the v1 fallback have no profile menus. **Class effect.** None.

## DQ-F3-2 — GeoCeDG chooser architecture and minimal inherited seams

**Nature.** AUTHOR DECISION (whether an inherited change is acceptable) with
IMPLEMENTATION DESIGN.

**Author-fixed.** Product surface and policy in the GeoCeDG Desktop layer where
possible; shared and upstream seams minimal and upstream-identical by default
(`F3-D3`); Classic unchanged.

**Observed.** Insert File, Apply Template and OFF are filters of the shared
`GuiManagerD.openFile(File)` and are dispatched on the selected filter, not on
the file; Cancel with one of them throws an NPE (`CF-F3-6`); every route first
asks to save an unsaved document (`CF-F3-11`). `openFile(File)` and
`doOpenFiles(File[], boolean)` are public and non-final; the filter-driven
`doOpenFiles(..., FileExtensions)` is private (report `K4`).

| Alternative | Consequence |
|---|---|
| **A1 — GeoCeDG-owned routes, no inherited change (recommended):** `GuiManagerGeoCeDG` overrides `openFile(File)` with a product Open dialog holding only the existing document filter (`.cedg .ggb .ggt .html .htm`), resolves typed names with the GeoCeDG policy (no `.off` in the fallback chain) and calls the public `doOpenFiles(existing files, true)`; it overrides `doOpenFiles(File[], boolean)` to refuse `.off` for every other caller; a new GeoCeDG orchestration class shows the insertion chooser for the two new actions and calls the unchanged `AppD.insertFile` / `AppD.applyTemplate` | no inherited file changes; Classic untouched; the product Open dialog duplicates about sixty lines of chooser setup from `GuiManagerD` (current directory, multiple selection, selected file, default filter, save prompt) that must follow upstream on a future synchronization; the macOS sandbox `FileDialog` branch is not reproduced (the product is packaged for Windows) |
| A2 — upstream-identical hooks in `GuiManagerD` (for example `offersDocumentInsertionFilters()` and `isOpenExtensionAllowed(FileExtensions)`, true by default) overridden by `GuiManagerGeoCeDG` | smallest code; follows the existing `getDocumentOpenExtensions()` hook precedent; an inherited change, recorded in `docs/upstream/modified-files.yml`, permitted by the rules only if A1 is judged insufficient |
| A3 — a GeoCeDG `GeoGebraFileChooser` subclass that drops filters by description | brittle (descriptions are localized and suffixed), leaves the typed `.off` routes; rejected |

**Recommendation.** A1. Insert File and Apply Template are not preceded by the
save-changes dialog, because neither replaces the document (`CF-F3-11` does
not carry over).

**Consequences.** `GuiManagerGeoCeDG`, `GeoCeDGDocumentPolicy` and the new
orchestration class change; `GuiManagerD`, `AppD`, `FileMenuD` and
`FileDropTargetListener` do not. A2 is the fallback if the author prefers less
duplication, with an inherited-file record.

**Tests.** `T-F3-OPEN-UNCHANGED`, `T-F3-OFF`, `T-F3-CLASSIC`, `T-F3-CANCEL`.
**Compatibility.** Ordinary Open of `.cedg`, `.ggb`, `.ggt`, `.html`, `.htm`
unchanged in behavior. **Class effect.** None (A2 adds an inherited Desktop
change but no serialization or parser change).

## DQ-F3-3 — `.cedg`/`.ggb` selection, default extension and fallback rules

**Nature.** AUTHOR DECISION.

**Author-fixed.** Both operations accept `.cedg` and `.ggb`; no other format;
no 3D import (`F3-D2` items 3, 8).

**Observed.** The inherited filters show only `*.ggb`; a selected or typed
`.cedg` works; an extension-less name gets `.ggb` appended, which turns a
`.cedg` stem into a missing file and then into `CF-F3-1`; several files can be
selected and are processed one after another (report `K4`, `K8`).

| Alternative | Consequence |
|---|---|
| **X1 — one filter "GeoCeDG documents (.cedg, .ggb)", selected by default; a name without extension resolves to `.cedg` if it exists, else `.ggb`; an existing file with any other extension (including `.off`, `.ggt`, `.html`) is refused with a localized message before any helper is created; a missing file shows a localized "not found" and nothing else; single selection (recommended)** | one operation = one conflict dialog, one notice and one undo point; matches `GeoCeDGDocumentPolicy.documentOpenExtensions()` order |
| X2 — as X1 with multiple selection, each file a separate operation | keeps the inherited capability; several notices and dialogs in a row; several undo points |
| X3 — two filters (`.cedg`, `.ggb`) | more clicks, no gain |

**Recommendation.** X1.

**Tests.** `T-F3-INSERT-CEDG`, `T-F3-INSERT-GGB`, `T-F3-TEMPLATE-CEDG`,
`T-F3-TEMPLATE-GGB`, `T-F3-FAILURES`. **Compatibility.** None. **Class
effect.** None.

## DQ-F3-4 — OFF file UI policy, including explicit typed extensions

**Nature.** AUTHOR DECISION, on CHARACTERIZATION EVIDENCE.

**Author-fixed.** OFF is no longer presented as a normal opening option in the
GeoCeDG UI during the mini-track; the internal upstream OFF support stays and
is not rewritten (`F3-D2` items 6–7; `D1` closeout: OFF "opens an explicitly 3D
route not wanted in this mini-track").

**Observed (report `K4`).**

| Route | Today in GeoCeDG |
|---|---|
| OFF offered in the UI | the "OFF file (.off)" filter of Open (unlocalized) |
| OFF selected by typing its extension | an explicit `.off` under any filter imports the polyhedron into the **current** document on the working layer and leaves it saved (`CF-F3-10`) |
| OFF accepted through an inherited fallback | an extension-less name resolves to `.off` last in the chain; also under the OFF filter |
| OFF through an internal API | `AppD.loadOffFile`, `GuiManagerD.loadOffFile`, `OFFHandler`: no GeoCeDG caller; drag and drop and the command line are not OFF routes |

| Alternative | Consequence |
|---|---|
| OFF-A — remove the filter only | the typed and fallback routes stay; OFF is still reachable from the normal GeoCeDG Open; does not meet the intent of the debt |
| **OFF-B — remove the filter and refuse `.off` in the GeoCeDG Open routes (explicit name, fallback, any `doOpenFiles` caller) with a localized message that OFF files are not supported in GeoCeDG; Insert File and Apply Template refuse it by `DQ-F3-3`; internal support, Classic and the v1 fallback (`DQ-F3-11`) unchanged (recommended)** | OFF unreachable from the GeoCeDG UI; a user typing `.off` learns why; no OFF code changed |
| OFF-C — as OFF-B but ignore silently | less transparent; a typed name appears to do nothing |

**Recommendation.** OFF-B.

**Tests.** `T-F3-OFF` (filter absent; explicit `.off`, stem with only `.off`,
`doOpenFiles` caller, Insert/Template with `.off`; Classic still offers the
filter and loads OFF). **Compatibility.** Documents unaffected. **Class
effect.** None.

## DQ-F3-5 — Insert File object, macro and label-conflict behavior

**Nature.** AUTHOR DECISION, on CHARACTERIZATION EVIDENCE; OPEN TECHNICAL ITEM
`OTI-F3-2`.

**Author-fixed.** Insert File keeps its semantics: it imports geometry and
construction into the current document; geometry, dependencies and identity
semantics unchanged (`F3-D2` item 4; instruction §4.6).

**Observed (report `K5` C1–C3, C6).** Objects, algorithms and dependencies are
imported in construction order; layers are kept; free labels kept. Label
conflicts: Rename (source objects get index labels), Overwrite (the target
object **and its dependents** are removed; `CF-F3-5`), Cancel. Macros: every
source macro is imported twice (`CF-F3-4`); on a command-name collision with a
different target macro the inserted objects bind to the **target's** macro and
silently change value (`CF-F3-3`).

| Alternative (labels) | Consequence |
|---|---|
| **R-A — keep the inherited Rename / Overwrite / Cancel dialog and semantics; the guides state that Overwrite deletes the replaced object's dependents (recommended)** | no semantic change; identical to Classic |
| R-B — Rename and Cancel only (GeoCeDG override of `shouldRenameObjectsOnInsertFile`, public, called on the helper's `GuiManagerGeoCeDG`) | avoids destructive replacement; removes an inherited capability |
| R-C — Overwrite asks a second confirmation naming the dependents | more dialogs; product-owned |

| Alternative (macros) | Consequence |
|---|---|
| M-A — keep the inherited import; document the collision | silent value change of inserted objects persists |
| **M-B — fail closed: before any target mutation, refuse the insertion with a localized message naming the macros when a source macro command exists in the target with a different definition; identical definitions proceed as today (recommended, subject to `OTI-F3-2`)** | no silent substitution (constructive traceability); needs the source macros before `AppD.insertFile` mutates the target, i.e. a GeoCeDG pre-load of the source or an inherited seam |
| M-C — rename the colliding source macro and rebind the inserted objects | a new import algorithm; outside "keep semantics"; rejected |

The duplicate import of `CF-F3-4` is inherited and is not changed by any
recommended alternative; it is documented.

**Recommendation.** R-A and M-B. If the entry gate shows that M-B needs an
inherited change (`OTI-F3-2`), the implementation stops and the author chooses
between M-A and an inherited seam.

**Tests.** `T-F3-LABELS`, `T-F3-MACROS`, `T-F3-REPEAT`, `T-F3-DEPENDENCIES`.
**Compatibility.** Classic unchanged. **Class effect.** None.

## DQ-F3-6 — Insert File undo, cancellation and partial-failure policy

**Nature.** AUTHOR DECISION with IMPLEMENTATION DESIGN; OPEN TECHNICAL ITEM
`OTI-F3-1`.

**Author-fixed.** Insert File is not Open and does not replace the document
(`F3-D2` item 4).

**Observed (report `K5` C4, C8).** No undo point; the insertion is merged into
the next stored step (one Undo after the next action removes both); Cancel
leaves the source macros imported and the helper undisposed; a missing,
unreadable, unsupported or empty source **re-pastes the previous copy buffer**
with placeholder labels and the "Import successful" dialog (`CF-F3-1`);
outside the identity-bearing path nothing rolls back the macro import, the
protected labels or an Overwrite removal.

| Alternative | Consequence |
|---|---|
| **U-1 — one undo point per successful insertion, stored by the GeoCeDG orchestration after `AppD.insertFile` returns with a changed construction; no undo point and no document change for Cancel or a failure: pre-validation (existence, extension, archive preflight) before the helper exists, an empty-source refusal in `GeoCeDGCopyPaste.insertFrom` (public, non-final) and no success dialog after a refusal (override of `showRenamedObjectsDialog`, public); macros added by a cancelled or refused insertion removed again through `Kernel.removeMacro` (recommended)** | undo removes exactly the insertion (consistent with `unit-system.md` §11 "undoing the paste undoes only the paste"); the inherited success dialog stays after a real insertion |
| U-2 — keep the inherited behavior | `CF-F3-1` and `CF-F3-2` reachable from the new File → Insert action |
| U-3 — U-1 without the macro cleanup | a cancelled insertion still adds macros |

**Recommendation.** U-1. Rollback guarantees beyond the identity-bearing path
are not claimed: a failure inside the plain paste after an Overwrite removal is
reported as an open limitation unless the entry gate proves the snapshot covers
it.

**Tests.** `T-F3-UNDO`, `T-F3-CANCEL`, `T-F3-FAILURES`. **Compatibility.**
Classic keeps `CF-F3-1`/`CF-F3-2`. **Class effect.** None.

## DQ-F3-7 — `D1` unit provenance and physical-mismatch integration

**Nature.** CHARACTERIZATION EVIDENCE confirming the author-fixed contract;
AUTHOR DECISION only on the buffer question.

**Author-fixed.** Insert File keeps the `D1` provenance and physical-mismatch
notice policy and never scales coordinates by units (`F3-D2` item 4;
`unit-system.md` §11; hard dependency `D1 → F3`, `F3-D5`).

**Observed (report `K6`).** For every successful insertion: provenance = the
helper's loaded unit state, captured when the target's `GeoCeDGCopyPaste`
replaces the buffer; one notice at the end of the paste only for two different
physical meanings; target units, coordinates and drawing scale unchanged; no
provenance in the XML. The failure paths re-paste the previous buffer with its
previous provenance (fixed by `DQ-F3-6` U-1).

| Alternative | Consequence |
|---|---|
| **P-A — keep the mechanism and the buffer replacement unchanged; prove them end to end through the File → Insert action (recommended)** | no second provenance mechanism; no GUI unit logic |
| P-B — restore the previous buffer after an insertion | changes the `D1`-approved behavior ("copy, cut or Insert File" replace the buffer); rejected |

**Recommendation.** P-A. A need for a shared or kernel change to keep these
invariants would stop the work; none was found.

**Tests.** `T-F3-D1-PROVENANCE` (all eight pairs of report `K6`),
`T-F3-NO-UNIT-CONVERSION`, `T-F3-CLIPBOARD`. **Class effect.** None.

## DQ-F3-8 — Apply Template unit and geometry invariants

**Nature.** CHARACTERIZATION EVIDENCE (invariants) and AUTHOR DECISION
(document state and inherited style resets).

**Author-fixed.** Apply Template applies labeling, construction defaults and
object styles; imports no geometry; never replaces the target `UnitState` or
applies the template's units (`F3-D2` item 5); `TemplateHelper` and
`ConstructionDefaults` unchanged (instruction §9, §17).

**Observed (report `K7`).** Units unchanged; no object imported; identities and
dependencies unchanged; construction defaults and (for a `.cedg` template) the
labeling style replaced; with nothing selected **every** object is restyled,
including captions cleared, fixed flags cleared and label offsets reset
(`CF-F3-8`); no undo point and the document stays saved (`CF-F3-2`); a failed
template load would still apply the helper's factory defaults (`CF-F3-12`,
inferred), which the pre-validation of `DQ-F3-3` prevents for missing,
unsupported and unreadable files.

| Alternative | Consequence |
|---|---|
| **T-1 — keep the inherited style semantics (documented in the guides, including the caption, fixed-flag and offset resets and the "selection or everything" rule); the GeoCeDG orchestration stores one undo point after the template and marks the document unsaved (recommended)** | undoable, saved-state correct; no shared change |
| T-2 — keep everything inherited, including no undo point and the saved state | silent unsaved modification persists |
| T-3 — T-1 plus GeoCeDG restoration of captions, fixed flags and offsets | product deviation from template semantics; scope expansion |

**Recommendation.** T-1.

**Tests.** `T-F3-TEMPLATE-UNITS`, `T-F3-TEMPLATE-NO-GEOMETRY`,
`T-F3-TEMPLATE-STATE`. **Class effect.** None.

## DQ-F3-9 — Apply Template effects on working and hidden layers

**Nature.** AUTHOR DECISION (mandatory characterization item of the
instruction), on CHARACTERIZATION EVIDENCE.

**Author-fixed.** None beyond "Apply Template keeps its semantics"; the `A-1`
working layer and the `A-2` layer domain and hidden set are approved contracts.

**Observed (report `K7` E4).** In GeoCeDG every restyled object moves to the
**working layer** — the whole document, or the selection — including hidden
objects and objects of hidden layers, which thereby become **visible**; Classic
moves them to the capped highest used layer. The hidden set and the working
layer are unchanged; the domain is respected. Cause: the inherited non-reset
restyle calls the `A-1` creation seam, which treats it as interactive creation.

| Alternative | Consequence |
|---|---|
| **L-A — preserve each restyled object's layer: the GeoCeDG orchestration records the layers before `AppD.applyTemplate` and restores them afterwards, before its undo point (recommended)** | hidden-layer membership and the user's layer organization survive; no inherited change; Classic keeps its behavior |
| L-B — accept the inherited re-layering (to the working layer) and document it | objects leave hidden layers silently |
| L-C — run the template inside a layer-workspace document transition so the seam returns the host layer | Classic semantics (capped highest used layer): still re-layers; rejected |
| L-D — change the seam to receive the object | inherited shared change; rejected |

**Recommendation.** L-A.

**Tests.** `T-F3-TEMPLATE-LAYERS` (objects on several layers, active working
layer different, hidden and visible layers, hidden objects, selection and
whole document, domain bounds). **Class effect.** None.

## DQ-F3-10 — Hidden-helper, recent-file and clipboard side effects

**Nature.** AUTHOR DECISION, with OPEN TECHNICAL ITEM `OTI-F3-3`.

**Author-fixed.** A hidden helper must not replace the user's document or leak
session state; global recent-file behavior is not modified without explicit
authorization (instruction §11).

**Observed (report `K9`, `K5` C7).** The helper never replaces the document. It
publishes the source or template to the shared recent-file list; leaves its
dock-manager listener (and, by reading, key dispatcher) registered; keeps its
frame after Cancel (`CF-F3-9`). A successful insertion replaces the window
buffer (D1-approved); a later Ctrl+V then creates `CLIPBOARDmagicSTRING…`
labels, in GeoCeDG and Classic, caused by the private `duplicateLabels` field
of `CopyPasteD` (`CF-F3-7`).

| Alternative (recent list) | Consequence |
|---|---|
| **H-1 — the helper does not publish to the recent list: `AppGeoCeDG` constructs helpers with a flag and its `setCurrentFile` override (public in `AppD`, `currentFile` protected) skips the recent-list publication for helpers only (recommended; this is a GeoCeDG helper change, not a change of global recent-file behavior)** | the recent list shows documents the user opened, not inserted sources or templates |
| H-2 — keep the publication | inserted sources and templates appear as recent documents |

| Alternative (registrations) | Consequence |
|---|---|
| **H-3 — after every outcome the orchestration disposes the helper's frame and removes its dock-manager AWT listener and key dispatcher (helper reachable through the GeoCeDG `newAppForTemplateOrInsertFile` override; recommended, subject to `OTI-F3-3`)** | no accumulation per operation |
| H-4 — keep | one retained helper per operation |

| Alternative (later Paste) | Consequence |
|---|---|
| **P1 — record `CF-F3-7` as a pre-existing upstream observation outside F3; the guides do not promise a later paste of inserted objects (recommended)** | no inherited change; the defect remains in GeoCeDG and Classic |
| P2 — reset `duplicateLabels` at the end of `CopyPasteD.insertFrom` (one line; inherited Desktop change; also corrects Classic) | requires explicit authorization and an upstream-modification record; changes Classic behavior |
| P3 — clear the buffer after an insertion | contradicts the `D1`-approved buffer replacement and provenance for a later paste; rejected |

**Recommendation.** H-1, H-3, P1.

**Tests.** `T-F3-HELPER`, `T-F3-CLIPBOARD`. **Compatibility.** Classic
unchanged under H-1/H-3/P1. **Class effect.** None (P2 adds an inherited change,
still no serialization or parser change).

## DQ-F3-11 — Classic and legacy-profile compatibility

**Nature.** AUTHOR DECISION (fallback) and CHARACTERIZATION EVIDENCE (Classic).

**Author-fixed.** Preserve Classic behavior (instruction §4.6).

**Observed (report `K10`, `K3`).** Classic shares every inherited route and
anomaly. The v1 fallback shows the upstream `FileMenuD` and still uses
`GuiManagerGeoCeDG`.

| Alternative | Consequence |
|---|---|
| **F-1 — Classic unchanged (no inherited change under the recommended set); the GeoCeDG Open override and OFF refusal are gated off in the v1 fallback, which keeps the inherited Open dialog with its filters (recommended; precedent of the fallback gates in `GuiManagerGeoCeDG`)** | the fallback stays the diagnostic upstream surface; OFF remains visible there |
| F-2 — apply the GeoCeDG Open policy in the fallback too | Insert File and Apply Template become unreachable in the fallback (it has no profile menus) |

**Recommendation.** F-1.

**Tests.** `T-F3-CLASSIC`, `T-F3-FALLBACK`. **Class effect.** None.

## DQ-F3-12 — Architecture, phase classification and acceptance verification

**Nature.** AUTHOR DECISION (class frozen at authorization) with
IMPLEMENTATION DESIGN.

**Author-fixed.** `VERIFICATION_CLASS = INTEGRATED_PHASE`, planned acceptance
registered `PHASE` + `INTEGRATION`, frozen only at authorization (`F3-D4`);
`D1 → F3` hard dependency; `F3` closes before `G` (`F3-D5`, `F3-D6`).

**Observed.** The recommended set changes only GeoCeDG Desktop code, the
profile, the registry, the validator allow-list, tests, guides and records; no
shared, kernel, serialization, parser, unit-semantics or `TemplateHelper`/
`ConstructionDefaults` change (report `K12`).

| Alternative | Consequence |
|---|---|
| **C-1 — `INTEGRATED_PHASE`: registered phase `PRE-G9B-R6-PLUS-F3` with `compile.desktop.semantic`, `workspace-profile.product` and `junit.desktop.pre-g9b-r6-plus-f3.semantic`, plus `INTEGRATION` on the same commit and tree (recommended)** | concrete integration coverage: the File surface and its pins cross the profile compiler, menu bar and registry; Insert File crosses the helper load, the copy buffer and `D1`; Apply Template crosses `A-1`/`A-2`; the Open route crosses existing document-lifecycle suites |
| C-2 — `BOUNDED_PHASE` | contradicts the author planning assignment and leaves the cross-cutting suites unexercised |

**Reclassification triggers** (stop for the author): any change in
`source/shared/**`; a change of serialization, parsing or unit semantics; a
change of `TemplateHelper` or `ConstructionDefaults`; a global
verification-infrastructure change beyond the phase-local allow-list amendment
and registration. An inherited Desktop change (A2, P2, an `OTI-F3-2` seam) does
not by itself change the class but needs explicit authorization.

**Recommendation.** C-1, with the author smoke of design §12.

**Tests.** All of design §11. **Class effect.** Confirms the planned class.

## What stays open

- `OTI-F3-1` to `OTI-F3-5` (report `K15`) for the implementation entry gate.
- The pre-existing anomalies `CF-F3-1` to `CF-F3-12` keep their dispositions as
  proposed above until the author decides.
- No `DQ-F3-*` is resolved by this record; `F3` implementation and publication
  are not authorized.

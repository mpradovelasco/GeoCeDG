# PRE-G9B-R6-plus-F3 — File / Insert surface: reconciled design candidate

- Status: **NOT AUTHOR APPROVED AS A WHOLE**; its §3, §4, §8 and §9 are part of the approved design by reference (**DESIGN — AUTHOR APPROVED**) (author decision of 2026-10-13, [F3 author design closeout record](../validation/pre_g9b_r6_plus_f3_author_design_closeout_record.md); implementation NOT AUTHORIZED)
- Produced by: `PRE-G9B-R6-plus-F3-PREP` (author instruction of 2026-10-12;
  characterization and documentary preparation only)
- Base: published `main` `e70718fec3612814144cd405d46f6c08864ceadd`, tree
  `26817bfc6a72b378e4babc1cd08efd0d734473c9`
- Reconciles: the author planning decisions `F3-D1` to `F3-D6`
  ([F3 author-decision record](../validation/pre_g9b_r6_plus_f3_author_decisions_record.md),
  unchanged) with the
  [F3 preparation characterization report](../validation/pre_g9b_r6_plus_f3_preparation_characterization_report.md)
  (`K0`–`K15`; corrections `ER-F3-1` to `ER-F3-9`; anomalies `CF-F3-1` to
  `CF-F3-12`)
- Questions: [author-decision preparation record](../validation/pre_g9b_r6_plus_f3_author_decision_preparation_record.md)
  (`DQ-F3-1` to `DQ-F3-12`, all pending)
- Claim vocabulary: **proposed / not normative**. Where a section depends on a
  pending question, it states the **recommended** alternative and names the
  question; the author's disposition replaces the recommendation.
  `selfApproved = false`.

Abbreviations follow the report (`GMD`, `AppD`, `CPD`, `AGC`).

## 1. Scope

| Target | Content | Questions |
|---|---|---|
| Surface | File → Insert / Archivo → Insertar with Insert File / Insertar archivo and Apply Template / Aplicar plantilla, removed from the Open dialog | `DQ-F3-1`, `DQ-F3-2` |
| Formats | both operations accept `.cedg` and `.ggb` only | `DQ-F3-3` |
| OFF | not offered or reachable in the GeoCeDG Open UI; internal support unchanged | `DQ-F3-4` |
| Insert File | existing import semantics through `AppD.insertFile`, made safe on failure and undoable | `DQ-F3-5`, `DQ-F3-6`, `DQ-F3-7` |
| Apply Template | existing template semantics through `AppD.applyTemplate`; units and geometry untouched; layers preserved; undoable | `DQ-F3-8`, `DQ-F3-9` |
| Helper | no recent-list publication, no leaked registrations | `DQ-F3-10` |
| Compatibility | Classic unchanged; v1 fallback keeps the inherited dialog | `DQ-F3-11` |
| Verification | `INTEGRATED_PHASE`, registered `PHASE` + `INTEGRATION` | `DQ-F3-12` |

Out of scope: other formats, 3D import, document format or serialization,
parsing, unit semantics, `TemplateHelper`, `ConstructionDefaults`,
`GuiManagerD`, `AppD`, `CopyPasteD`, OFF code, persistent identity, Classic,
`F4`, `G`.

## 2. Invariants

1. Geometry, algorithms and the dependency graph of an inserted construction
   are those `CopyPasteD.insertFrom` produces today; no new import algorithm.
2. Coordinates are never converted, normalized or scaled because of units;
   coordinate authority stays with the kernel construction.
3. Insert File never changes the target `UnitState`, current file, existing
   object instances, working layer, hidden-layer set or drawing scale.
4. Source unit provenance travels only through the existing
   `GeoCeDGCopyPaste` buffer provenance; the notice is the `D1` notice
   (`unit-system.md` §11). No second provenance mechanism; no unit logic in
   the GUI.
5. Apply Template imports no object, never replaces the target `UnitState`
   and never applies the template's units.
6. No document-format, XML, parser, preference-format or serialization change.
7. A refused or failed operation leaves the target construction, macros, undo
   history, saved state and copy buffer exactly as they were (recommended
   `DQ-F3-6`).
8. A successful operation is exactly one undo step (recommended `DQ-F3-6`,
   `DQ-F3-8`).
9. Classic is unchanged: no inherited class changes under the recommended set.
10. No identity is inferred from labels by product code; the inherited
    Overwrite remains a user-chosen, label-addressed replacement.

## 3. Surface (`DQ-F3-1`, recommended N1)

```text
File
  New, Open…, Open Recent, Open Classic diagnostic      (file-open, unchanged)
  Insert ▸                                               (file-insert, new group)
      Insert File…        document.insert-file   → geocedg.document.insert-file
      Apply Template…     document.apply-template → geocedg.document.apply-template
  ─────
  Save, Save As…                                         (unchanged)
  …
```

Profile deltas: presentation group `file-insert` (`name_key`
`Group.File.Insert`: "Insert" / "Insertar"; `action_ids` in the order above; no
toolbar ids); File menu entry `{"kind": "group", "group_id": "file-insert"}`
directly after the `file-open` actions entry; two `document-action` actions,
`maturity` `stable`, cluster `document-lifecycle` (`menu_action_ids`),
availability `document-product`, localization refs
`geocedg.document.InsertFile` and `geocedg.document.ApplyTemplate` with the five
texts each in EN and ES (names "Insert File…" / "Insertar archivo…",
"Apply Template…" / "Aplicar plantilla…"). Other field values follow
`document.open` unless a schema rule requires a new reference; the
implementation re-establishes the referenced profiles against the schema.
Registry: both targets in `TARGETS` and `execute`, dispatching to the
orchestration of §5 and §6; no accelerator. Catalog 131 → 133.

## 4. GeoCeDG Open route and OFF (`DQ-F3-2` A1, `DQ-F3-4` OFF-B, `DQ-F3-11` F-1)

`GuiManagerGeoCeDG` overrides, unless `GeoCeDGProfile.isLegacyFallback()`:

- `openFile(File)`: the inherited save prompt, then the shared
  `GeoGebraFileChooser` (`DialogManagerD`) in `MODE_GEOGEBRA` with the current
  directory, multiple selection and the current file pre-selected, and **one**
  filter: the existing document filter (`getDocumentOpenExtensions()` +
  `.ggt`, `.html`, `.htm`, `getDocumentOpenDescription()`). After approval each
  selected file is resolved by `GeoCeDGDocumentPolicy`: an existing file
  keeps its name; a missing name tries `.cedg`, `.ggb`, `.ggt`, `.html`, `.htm`
  (never `.off`); an existing `.off` or an unresolved name is refused with a
  localized message; the resolved files go to the public
  `doOpenFiles(files, true)`, which then never reaches its fallback.
- `doOpenFiles(File[], boolean)`: refuses any `.off` element with the same
  message and passes the rest to `super`.

The inherited `GuiManagerD.openFile`, its four filters, its OFF dispatch, its
Cancel NPE (`CF-F3-6`), `loadOffFile`, `AppD.loadOffFile` and `OFFHandler` are
unchanged and remain reachable in Classic and in the v1 fallback.

Rejected: A3 (filter removal by description); OFF-A (filter only). Alternative:
A2 (upstream-identical hooks in `GuiManagerD`), only by explicit decision.

## 5. Insert File orchestration (`DQ-F3-3`, `DQ-F3-5`, `DQ-F3-6`, `DQ-F3-7`)

A new GeoCeDG class (proposed `GeoCeDGDocumentInsertion`, package
`org.geocedg.desktop`) owns both operations.

### 5.1 Selection

Chooser in single-selection mode with one filter "GeoCeDG documents (.cedg,
.ggb)" (localized), starting in the current path; no save prompt. Name
resolution: an existing file keeps its name; a missing name tries `.cedg`,
then `.ggb`; the resolved file must exist and have extension `.cedg` or `.ggb`,
otherwise a localized refusal and no further step (no helper is created).

### 5.2 Pre-validation (`OTI-F3-1`)

The archive is read and validated with the existing
`DocumentArchivePreflight.validate(bytes, app.createDocumentPreflightConfig())`
(`desktop/io/DocumentArchivePreflight.java:36`; config AGC `:1760-1766`). A
rejection uses the existing product load-failure message path
(`AGC.showDocumentLoadFailure`) and stops. The entry gate establishes whether
this, together with §5.4, covers every `C8` case without a second full load.

### 5.3 Macro collision (`DQ-F3-5` M-B, `OTI-F3-2`)

Before `AppD.insertFile`, the source macros are compared with the target's by
command name; a name present in both with a different definition (macro XML
from `getFullMacroXML`) refuses the insertion with a localized message naming
the macros. Obtaining the source macros before `AppD.insertFile` needs either a
GeoCeDG pre-load of the source in a separate helper (product-owned, double
load) or an inherited seam; the entry gate decides feasibility and stops for
the author if only an inherited seam works. Identical definitions proceed as
today (including the inherited duplicate import of `CF-F3-4`).

### 5.4 Execution and outcome

1. Record the target macro list and arm an insertion outcome in
   `GeoCeDGCopyPaste`.
2. `app.setMoveMode()`; `app.insertFile(file)` (unchanged `final` method).
3. `GeoCeDGCopyPaste.insertFrom` override: when the source construction has no
   element, record `REFUSED_EMPTY` and return without touching the buffer or
   the target (prevents `CF-F3-1`); otherwise call `super` and record
   `INSERTED` (or `FAILED` when nothing was created).
4. `GuiManagerGeoCeDG.showRenamedObjectsDialog` override: shows the inherited
   dialog only for `INSERTED`; after `REFUSED_EMPTY`/`FAILED` it shows a
   localized "nothing was inserted" message instead.
5. After return: `INSERTED` → one `app.storeUndoInfo()` (the document is
   unsaved); Cancel of the label dialog (no outcome recorded) or a refusal →
   remove the macros added since step 1 (`Kernel.removeMacro`), no undo point,
   saved state restored if it changed.
6. Release the helper (§8).

Labels follow the inherited dialog (`DQ-F3-5` R-A). Rollback inside the plain
paste after an Overwrite removal is not claimed (report `K5` C8); the entry gate
records whether the inherited snapshot covers it.

### 5.5 `D1` (`DQ-F3-7` P-A)

Unchanged: provenance from the helper's loaded state at buffer replacement,
the notice at paste completion, the buffer replaced by a successful insertion.
§5.4 step 3 guarantees that a failed insertion never re-pastes the previous
buffer or its provenance.

## 6. Apply Template orchestration (`DQ-F3-8` T-1, `DQ-F3-9` L-A)

1. Selection and pre-validation as §5.1–§5.2 (prevents `CF-F3-12` for missing,
   unsupported and unreadable files).
2. Record each target object's layer (all objects; the template restyles the
   selection or everything).
3. `app.setMoveMode()`; `app.applyTemplate(file)` (unchanged `final` method;
   `TemplateHelper` and `ConstructionDefaults` unchanged).
4. Restore every recorded layer that changed (`setLayer`), then repaint.
5. One `app.storeUndoInfo()`; the document is unsaved.
6. Release the helper (§8).

Inherited semantics kept and documented: labeling style and construction
defaults replaced; selected objects or all objects restyled; captions, fixed
flags, label offsets, colour functions and show conditions taken from the
defaults; Euclidian visibility untouched; units, geometry, labels, identities
and dependencies untouched.

## 7. `D1` and layer contracts

| Contract | Insert File | Apply Template |
|---|---|---|
| `unit-system.md` §11 provenance and notice | unchanged mechanism (§5.5) | not involved (no paste) |
| §8.8 non-clearing parse ignores units | paste fragment carries no unit element | helper's own parse only |
| target `UnitState` | unchanged (P) | unchanged (P) |
| `drawingScale` (§12, session) | unchanged | unchanged |
| `A-1` working layer | not applied to inserted objects (paste exclusion) | not applied: layers restored (§6) |
| `A-2` hidden set and domain | not imported; target set unchanged | unchanged; objects stay on their (possibly hidden) layers |

## 8. Hidden helper lifecycle (`DQ-F3-10` H-1, H-3)

`AppGeoCeDG.newAppForTemplateOrInsertFile()` constructs the helper with a
private helper flag and keeps a reference for the orchestration. For a helper,
the `AppGeoCeDG.setCurrentFile` override sets the current file without
publishing it to the shared recent-file list (the public `AppD.setCurrentFile`
behavior is unchanged for every non-helper window). After every outcome the
orchestration disposes the helper's frame (also after Cancel), removes its
`DockManagerD` AWT listener and its key event dispatcher, and drops the
reference (`OTI-F3-3`). The helper keeps the target's runtime-feature flags and
no new-document unit defaults, as today. The post-insertion Paste placeholder
labels (`CF-F3-7`) stay a recorded pre-existing observation (`DQ-F3-10` P1).

## 9. Classic and fallback (`DQ-F3-11` F-1)

Classic constructs none of the GeoCeDG classes involved. In the v1 fallback the
two `GuiManagerGeoCeDG` overrides of §4 and the dialog override of §5.4 defer
to `super`; the fallback has no profile menu, so the new actions do not exist
there.

## 10. Architecture and files

| Path | Change (recommended set) |
|---|---|
| `apps/geocedg/application-profile.yml` | §3 |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java` | two targets |
| `…/GeoCeDGProfile.java` | catalog pin 131 → 133 and its message |
| `…/GeoCeDGDocumentInsertion.java` (new) | §5, §6 |
| `…/GeoCeDGDocumentPolicy.java` | insertion extensions, Open-name resolution without `.off` |
| `…/GuiManagerGeoCeDG.java` | `openFile(File)`, `doOpenFiles(File[], boolean)`, `showRenamedObjectsDialog` overrides |
| `…/GeoCeDGCopyPaste.java` | `insertFrom` override and outcome |
| `…/AppGeoCeDG.java` | helper flag and reference, helper `setCurrentFile` |
| `tools/agent/workspace-profile-validation.ps1` | `PRE-G9B-R6-plus-F3` amendment (two ids) |
| `geocedg/specs/operations/verification-static-contracts.json`, `verification-registry.json` | re-pins; phase `PRE-G9B-R6-PLUS-F3` and its JUnit selection; inventory through the official updater |
| tests (§11) and the pinned tests of report `K3` | new classes; pin and flatness amendments |
| `docs/upstream/modified-files.yml` | records for every new or changed file under `source/` |
| `docs/user/geocedg_user_guide_en.md`, `_es.md` | File table, Insert section, D1 notice sentence, OFF note; outline pins if a heading is added |
| `geocedg/specs/ui/document-insertion.md` (proposed, new) | the durable contract, `PROPOSED` until approved |

No path under `source/shared/**`; no change of `GuiManagerD`, `AppD`,
`CopyPasteD`, `FileMenuD`, `TemplateHelper`, `ConstructionDefaults`, OFF code or
any XML handler. Every GeoCeDG override used is of a public or protected,
non-final member: `GuiManagerD.openFile(File)`, `doOpenFiles(File[], boolean)`,
`showRenamedObjectsDialog`, `CopyPasteD.insertFrom`, `AppD.setCurrentFile`,
`AppD.newAppForTemplateOrInsertFile`.

## 11. Test matrix

| Id | Obligation (instruction §14 item) | Kind | New or extended |
|---|---|---|---|
| `T-F3-MENU` | File → Insert group, order, ids, targets, no accelerator (1) | Desktop | new; `G9U1WorkspaceSurfaceTest` flatness amended |
| `T-F3-L10N` | EN/ES names, group, refusals, language switch keeps identity (2) | Desktop | new; `G9U1ActionRegistryTest` pattern |
| `T-F3-CLASSIC` | Classic `GuiManagerD.openFile` still offers the four filters and OFF; GeoCeDG classes not constructed (3) | Desktop | new |
| `T-F3-FALLBACK` | v1 fallback keeps the inherited dialog (3, 11) | Desktop | new; `GeoCeDGProfile` fallback precedent |
| `T-F3-OPEN-UNCHANGED` | GeoCeDG Open of `.cedg`, `.ggb`, `.ggt`, `.html`; stem resolution; one document filter; Cancel without exception (4) | Desktop | new; `GeoCeDGDocumentLifecycleTest` regressions |
| `T-F3-INSERT-CEDG`, `T-F3-INSERT-GGB` | Insert File through the action for both formats; current file and instances unchanged (5) | Desktop | new |
| `T-F3-TEMPLATE-CEDG`, `T-F3-TEMPLATE-GGB` | Apply Template for both formats (6) | Desktop | new |
| `T-F3-LABELS` | Rename, Overwrite (dependents removed, documented), Cancel (7) | Desktop | new |
| `T-F3-MACROS` | identical-definition macros proceed; conflicting definition refused before mutation (7) | Desktop | new |
| `T-F3-REPEAT` | same source twice (8) | Desktop | new |
| `T-F3-DEPENDENCIES` | algorithm classes and inputs re-bound to inserted objects; Locus V2/Spline V2 source (9) | Desktop | new |
| `T-F3-UNDO` | one undo step per insertion and per template; redo; undo of a later action leaves the insertion (10) | Desktop | new; undo baseline and store listeners |
| `T-F3-CANCEL`, `T-F3-FAILURES` | chooser Cancel, label Cancel, missing, unsupported, `.off`, garbage, malformed, empty source: no mutation, no buffer re-paste, no success dialog, no undo point (11) | Desktop | new |
| `T-F3-ARCHIVES` | `.cedg` with units, layers, hidden layers, macros; Classic `.ggb`; rejected archives (12) | Desktop | new |
| `T-F3-D1-PROVENANCE` | the eight unit pairs of report `K6` through the action (13) | Desktop | new; extends `PreG9BR6PlusD1UnitClipboardTest` scenarios end to end |
| `T-F3-NO-UNIT-CONVERSION` | coordinates and target state unchanged; drawing scale kept (14) | Desktop | new |
| `T-F3-TEMPLATE-UNITS` | template units never applied (15) | Desktop | new |
| `T-F3-TEMPLATE-NO-GEOMETRY` | no template object imported (16) | Desktop | new |
| `T-F3-TEMPLATE-LAYERS` | layers preserved for all and selected objects, hidden layers and hidden objects; working layer different (17) | Desktop | new; extends the `A-2` hidden-set assertion |
| `T-F3-HELPER` | no recent-list entry, no residual frame, listener or dispatcher, no preference write, repeated operations (18) | Desktop | new |
| `T-F3-CLIPBOARD` | buffer replaced by a successful insertion only; later Paste provenance and notice (19) | Desktop | new |
| `T-F3-OFF` | no OFF filter; explicit `.off`, stem, `doOpenFiles` caller, Insert/Template refuse; Classic loads OFF (20) | Desktop | new |
| `T-F3-PINS` | catalog 133 and every moved pin; validator amendment (21) | Desktop + PowerShell | extended |
| `T-F3-GUIDES` | guides describe File → Insert; outline pins (22) | Desktop | extended if headings change |
| `T-SMOKE` | author smoke (§12) | author | — |

Tests that drive the real chooser run on the EDT and answer dialogs through
their own components (report `K2`); Desktop tests that add hosts use
`G9U1TestApp.Lifecycle`, the per-test Desktop logger and isolated preferences.

## 12. Verification classification and author smoke (`DQ-F3-12` C-1)

`VERIFICATION_CLASS = INTEGRATED_PHASE`, frozen at authorization: registered
phase `PRE-G9B-R6-PLUS-F3` with `compile.desktop.semantic`,
`workspace-profile.product` and `junit.desktop.pre-g9b-r6-plus-f3.semantic`
(the new classes plus every pinned class of report `K3` and the `D1` clipboard,
`A-2` hidden-layer and guide-outline classes it touches), then `INTEGRATION` on
the same commit and tree. Concrete integration coverage: the profile compiler,
menu bar and registry; the helper load, copy buffer and `D1` provenance; the
`A-1`/`A-2` layer workspace; the document-lifecycle and Open suites.
Reclassification triggers: report `K12` and the decision record `DQ-F3-12`.

Author smoke (EN and ES): File → Insert shows Insert File… and Apply
Template…; Open shows only the document filter; insert a `.cedg` in m into a
document in mm (notice, coordinates unchanged, one Undo removes it, current
file unchanged); insert a Classic `.ggb`; insert with a label conflict
(Rename, then Overwrite, then Cancel); insert a document using a tool whose
name exists with another definition (refusal); insert a missing name and a
non-document file (refusal, nothing pasted); Ctrl+V afterwards (recorded
`CF-F3-7`); Apply Template from a `.cedg` with other units onto objects on
several layers including a hidden layer, whole document and selection (units
unchanged, layers kept, one Undo restores); type `x.off` and `x` (only
`x.off` exists) in Open (refusal); the recent-file list after these operations;
Classic: the inherited Open filters still present.

## 13. Compatibility and rollback

Documents: unchanged format; documents written after F3 are byte-identical in
structure to documents written before for the same construction. Classic and
the v1 fallback: unchanged. Rollback of the phase: revert its commits; no data
migration. The only user-visible removals are the Open-dialog filters and the
OFF route in GeoCeDG, both replaced or intentionally retired by `F3-D2`.

## 14. Rejected alternatives

- A new generic import subsystem or a GeoCeDG copy of `AppD.insertFile`
  (forbidden scope; duplicates kernel-adjacent semantics).
- Unit conversion or normalization on insertion (`F3-D2` item 4).
- Applying the template's units or construction defaults beyond the inherited
  ones (`F3-D2` item 5).
- Changing `TemplateHelper`, `ConstructionDefaults` or the `A-1` seam
  signature (forbidden; `DQ-F3-9` L-D).
- Clearing the copy buffer after an insertion (`DQ-F3-10` P3).
- Removing or rewriting OFF support (`F3-D2` item 7).

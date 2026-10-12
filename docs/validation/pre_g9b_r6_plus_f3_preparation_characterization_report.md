# PRE-G9B-R6-plus-F3 — preparation characterization report

- Activity: `PRE-G9B-R6-plus-F3-PREP` (author instruction of 2026-10-12,
  "File / Insert Surface and Document Insertion Compatibility";
  AUTHOR AUTHORIZED — CHARACTERIZATION, DESIGN RECONCILIATION, AUTHOR-DECISION
  PREPARATION AND CANONICAL PROMPT PREPARATION ONLY)
- Status: **CHARACTERIZATION COMPLETE — DOCUMENTARY PREPARATION CANDIDATE —
  PENDING AUTHOR REVIEW**
- Base: published `main` `e70718fec3612814144cd405d46f6c08864ceadd`, tree
  `26817bfc6a72b378e4babc1cd08efd0d734473c9` (`P_F2_F2R1_CLOSEOUT`)
- Phase state: `PRE-G9B-R6-plus-F3` = **PLANNED — AUTHOR APPROVED;
  PREPARED — IMPLEMENTATION NOT AUTHORIZED**
- Preparation class: `DOCUMENTATION_STATUS_ONLY` (acceptance `STATIC`)
- Machine-readable mirror:
  [`pre-g9b-r6-plus-f3-preparation-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-preparation-characterization.json)
- Design: [reconciled design candidate](../architecture/pre_g9b_r6_plus_f3_file_insert_reconciled_design_candidate.md);
  questions: [author-decision preparation record](pre_g9b_r6_plus_f3_author_decision_preparation_record.md);
  contract: [canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-f3-file-insert.prompt.md)
- Governing planning authority (unchanged):
  [F3 author-decision record](pre_g9b_r6_plus_f3_author_decisions_record.md)
  (`F3-D1` to `F3-D6`, 2026-10-03); its characterization at `P_R6PLUS_D1` is
  historical evidence and is re-established below
- `selfApproved = false`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`

This report characterizes the current product at the base. It decides nothing.
Every `DQ-F3-*` question stays pending until the author disposes of it.

Evidence classes: **S** = proven from source at the base; **P** = proven by
the scratch probes of `K14`; **I** = inferred from reading, not executed.

Path abbreviations: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`;
`common-jre/` = `source/shared/common-jre/src/main/java/org/geogebra/common/jre/`;
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`;
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`;
`tests/` = `source/desktop/desktop/src/test/java/org/geocedg/desktop/`;
**GMD** = `desktop/gui/GuiManagerD.java`; **AppD** = `desktop/main/AppD.java`;
**CPD** = `desktop/util/CopyPasteD.java`; **AGC** =
`geocedg-desktop/AppGeoCeDG.java`; **profile** =
`apps/geocedg/application-profile.yml`.

## K0 — Scope, authority and entry gate

**Author-fixed scope** (`F3-D2`, restated by the instruction of 2026-10-12):
File → Insert (Archivo → Insertar) with Insert File / Insertar archivo and
Apply Template / Aplicar plantilla, moved out of the Open dialog's file-type
list; both accept `.cedg` and `.ggb` only; Insert File imports into the current
document, is not Open, keeps the `D1` provenance and physical-mismatch notice
and never converts coordinates by units; Apply Template applies labeling,
construction defaults and object styles, imports no geometry and never replaces
the target `UnitState`; OFF is no longer presented as a normal opening option in
the GeoCeDG UI while the internal upstream OFF support stays; Classic,
commands, algorithms, dependencies and document formats unchanged; `F3` closes
before the global `PRE-G9B-R6-plus` closeout.

**Entry gate (2026-10-12).**

| Check | Result |
|---|---|
| live remote `main` (`git ls-remote origin refs/heads/main`) | `e70718fec3612814144cd405d46f6c08864ceadd` |
| local `main`, `origin/main` | both `e70718fec3612814144cd405d46f6c08864ceadd` |
| tree | `26817bfc6a72b378e4babc1cd08efd0d734473c9` |
| worktree | clean |
| `F2`, `F2-R1` | `PASS — AUTHOR APPROVED — PUBLISHED` ([joint closeout record](pre_g9b_r6_plus_f2_f2r1_closeout_record.md); roadmap row; plan status block) |
| `D1`, `A-2` | `PASS — AUTHOR APPROVED — PUBLISHED` (roadmap; plan §3) |
| `F3` | `PLANNED — AUTHOR APPROVED — PUBLISHED — IMPLEMENTATION NOT AUTHORIZED` (roadmap row; plan §3 and status block; F3 decision record) |
| `F3` already implemented? | no: no File → Insert group, no insertion action or target in the profile or `GeoCeDGActionRegistry`; the only insertion routes are the shared Open-dialog filters (S, `K3`, `K4`) |
| preparation branch | `phase/pre-g9b-r6-plus-f3-prep` (local), from the base |

**Authorities read from disk.** `AGENTS.md`; `CLAUDE.md` (read only); the
canonical governance and verification prompts; the roadmap (version 5.21) and
the mini-track plan (§2, §3, F3 placement and class rows, status block); the F3
decision record; the `D1` closeout record (`DEBT-R6PLUS-FILE-INSERT-SURFACE`,
nine requirements); `geocedg/specs/units/unit-system.md` §8.8, §9, §11, §12,
§17.2; `geocedg/specs/operations/verification-levels.md` §12.8.

## K1 — Re-establishment of the 2026-10-03 characterization

The twelve findings of the F3 decision record were re-read at the base. They
remain valid in substance; the citations and counts below replace them
(corrections `ER-F3-*`; the record itself is not amended).

| Id | Record finding | At the base |
|---|---|---|
| `ER-F3-1` | 1, 10: action-count pin `125`; File pinned with 17 direct items | pin is **131** (`geocedg-desktop/GeoCeDGProfile.java:340`); File holds **19** direct actions pinned in order by `tests/G9U1WorkspaceSurfaceTest.java:142-149`; the File menu is at profile `:3663-3683`, its groups at `:3463-3499` (S) |
| `ER-F3-2` | 2–4: `GuiManagerD.openFile` `:2002-2015`, `:2038-2098`, OFF `:2163-2166`, `:2196-2197`, `:2291-2294` | unchanged at those lines; `openFile(File)` spans GMD `:1928-2102` (S) |
| `ER-F3-3` | 5–6: `AppD.insertFile` `:4734-4787`, `applyTemplate` `:4796-4815`, helper `:4789-4791`; AGC helper `:664-672` | AppD `:4775-4828`, `:4837-4856`, `:4830-4832`; AGC `:1332-1340` (S) |
| `ER-F3-4` | 7: re-layering "to the capped highest used layer" (inferred) | **confirmed and stronger** in GeoCeDG: every restyled object moves to the **working layer**, which also reveals objects of hidden layers; Classic moves them to the capped highest used layer (P, `K7`) |
| `ER-F3-5` | 7: `ConstructionDefaults.java:917-926`, `:998-1001` | `common/kernel/ConstructionDefaults.java:922-931`, `:1003-1006` (S) |
| `ER-F3-6` | 8: helper load and recent list `AppD.java:3284`, `:1095` | load path AppD `:3105-3118`, `:3175-3192`, `:3284-3344`; recent list AppD `:1088-1117`; **confirmed at runtime** (P, `K9`) |
| `ER-F3-7` | 9: Insert File replaces the copy buffer | confirmed for a successful insertion; a failed or empty insertion does **not** replace it and **re-pastes the previous buffer** (P, `K5` C8) |
| `ER-F3-8` | 4: removing the OFF filter alone would not close the typed route (by reading) | **confirmed at runtime** for an explicit `.off` and for an extension-less name (P, `K4`) |
| `ER-F3-9` | 12: no test drives the Open filters, `AppD.insertFile`, `AppD.applyTemplate` or OFF | still true; additionally `tests/PreG9BR6PlusA2HiddenLayerPersistenceTest.java:472-485` calls `insertFrom` and `new TemplateHelper(app).applyTemplate(helper)` directly (hidden set only) (S) |

## K2 — Method

- Source reading at the base of every cited seam, with each line number
  re-established at `e70718fe`.
- A read-only survey of the profile, registry, menu bar, validator, pins,
  guides and drag-and-drop route, every citation spot-checked.
- Two scratch-only JUnit classes (`F3PrepProbeTest`, seven methods;
  `F3PrepProbe2Test`, four methods) in the Desktop test source set through a
  Gradle init script; never in a tracked path; isolated preferences
  (`GeoGebraPreferencesD.setPropertyFileName` to a scratch file before any
  host) and the in-memory unit-preference store; `JOptionPane` statically
  mocked on the test thread and its calls recorded; modal EDT dialogs found
  through `Window.getWindows()` and closed after their text was recorded. The
  real Open dialog was driven on the EDT through its own `JFileChooser`
  (`setFileFilter`, `setSelectedFiles`, `approveSelection`,
  `cancelSelection`). Record in `K14`.
- Documents were real archives written by the hosts: `.cedg` by
  `AppGeoCeDG.saveGeoGebraFile`, `.ggb` by an embedded Classic `AppD`.

## K3 — Characterization A: File menu and action architecture

**Declarative structure (S).** The profile is JSON with 131 actions, 30
presentation groups and 7 menu sections. The File section (profile
`:3663-3683`) holds only `actions` entries and separators over the groups
`file-open` (`document.new`, `document.open`, `document.open-recent`,
`diagnostic.open-classic`; `:3463-3466`), `file-save`, `file-print`,
`file-import-export`, `file-export-area`, `file-close`. A menu entry of kind
`group` becomes a nested `JMenu` titled from the group's `name_key` with the
client property `geocedg.presentation.group.id`
(`geocedg-desktop/GeoCeDGMenuBar.java:97-103`); kind `actions` splices the
group's items (`:104-105`). Nesting is one level: a presentation group cannot
contain a group (schema `geocedg/specs/ui/application-profile.schema.json:924-953`).
`group` entries are used today in View, Construction and Options, not in File.
Order is purely declarative (menu order, entry order, `action_ids` order).
Every action must appear in exactly one presentation group and once in the menu
projection (`GeoCeDGMenuBar.java:153-155`; `GeoCeDGProfile.java:396-417`,
`:476-515`).

**Action → code (S).** Non-mode actions bind to an allow-listed target
(`geocedg-desktop/GeoCeDGActionRegistry.java:54-80`; unbound targets throw at
construction, `:91-94`) and dispatch in `execute` (`:284-474`).
`document.open` (profile `:2126-2142`) → `host.document.open` → `gui.openFile()`
(`:343-345`). There is no generic target mechanism: a new target needs the
allow-list, an `execute` case and, if gated, `unavailableReason`
(`:185-223`). No accelerator is given to `document.open`; Ctrl+O comes from the
upstream key dispatcher.

**Localization (S).** Profile labels come only from the profile's
`localized_text` (`en`, `es`) through `GeoCeDGProfile.getText`
(`GeoCeDGProfile.java:102-112`); a non-mode action needs the five keys
`.name/.short_help/.long_help/.status/.error` (`:320-322`) and a group its
`name_key`. Upstream `menu*.properties` hold `InsertFile` ("Insert File" /
"Inserta Archivo") and `ApplyTemplate` ("Apply Template" / "Aplicar estilos");
they label only the shared chooser filters (GMD `:2004`, `:2009`) and do not
give the author-fixed Spanish names.

**Workspaces (S).** Two workspaces (`cedg-construction`, available;
`cedg-dihedral-procedures`, disabled with reason). The menu bar is built once
from `menu_sections` without consulting the workspace
(`GeoCeDGMenuBar.java:79-162`); the File menu is the same in every workspace.

**Legacy fallback (S).** A profile load failure selects the v1 profile, which
has no menus; `GuiManagerGeoCeDG.newMenuBar` then returns the upstream
`GeoGebraMenuBar` (`geocedg-desktop/GuiManagerGeoCeDG.java:43-64`), whose
`FileMenuD` Open action calls `openFile()` (`desktop/gui/menubar/FileMenuD.java:113-114`,
`:315-323`). The fallback keeps `GuiManagerGeoCeDG` as GUI manager, so any
`GuiManagerGeoCeDG` chooser override also applies there unless it is gated on
`GeoCeDGProfile.isLegacyFallback()` (the existing precedent at `:45`, `:82`,
`:113`, `:163`).

**Pins that a File → Insert group moves (S).**

| Pin | Location | Value |
|---|---|---|
| runtime catalog size | `GeoCeDGProfile.java:340` | 131 |
| catalog size in tests | `tests/G9U1ActionRegistryTest.java:65`; `G9U1ProfileCompilerTest.java:44`, `:212`, `:384`; `G9U1WorkspaceSurfaceTest.java:124`, `:704`; `GeoCeDGProfileTest.java:45`; `PreG9BR6PlusA1LayerWorkspaceTest.java:450`; `PreG9BR6PlusE1LCuratedLibraryTest.java:380`; `PreG9BR6PlusE2NativeDimensionDesktopTest.java:423`; `PreG9BR6PlusE3IsoABorderDesktopTest.java:407`; `PreG9BR6PlusF1OrientationCueTest.java:655` | 131 |
| File direct action order | `G9U1WorkspaceSurfaceTest.java:142-149` | 19 ids (unchanged if the new actions sit in a submenu: `directActionIds` skips submenus) |
| File and Edit are flat | `G9U1WorkspaceSurfaceTest.java:182-191` (`fileAndEditAreFlatWhileConstructionUsesDeclaredSemanticGroups`) | **broken by any File submenu**; must be amended |
| approved-action allow-list | `tools/agent/workspace-profile-validation.ps1:34-99` (110-action candidate baseline plus eight amendments) | a `PRE-G9B-R6-plus-F3` amendment is needed |
| validator hash | `geocedg/specs/operations/verification-static-contracts.json:21` (contract `workspace.profile-authority`) and the catalog hash in `verification-registry.json:7` | re-pin with `Get-VerificationCanonicalTextSha256` |

The profile file itself has no hash pin; it is an impact path of
`workspace-profile.product` (registry `:165-181`).

## K4 — Characterization B: Open-dialog routes

**`GuiManagerD.openFile(File)` (S, GMD `:1928-2102`).**

| Step | Lines | Behavior |
|---|---|---|
| unsaved document | `:1929` | `isSaved() \|\| saveCurrentFile()`: the save-changes dialog precedes **every** route of the dialog, including Insert File and Apply Template |
| macOS sandbox | `:1932-1978` | `FileDialog` with a name filter accepting `.ggb .cedg .ggt .html .htm .off`; then `doOpenFiles(files, true)`; not reached on Windows (I) |
| filters | `:1991-2015` | product document filter (`getDocumentOpenExtensions()` + `.ggt .html .htm`; GeoCeDG order `.cedg .ggb`, `GuiManagerGeoCeDG.java:178-181`); Insert File `*.ggb`; Apply Template `*.ggb`; OFF `*.off` with the unlocalized description `"OFF file"` (`:2013-2014`) |
| default filter | `:2017-2025` | the document filter when the current file is new, `.cedg`, `.ggb` or `.ggt` |
| multiple selection | `:1987`, `:2100` | enabled during the dialog |
| dispatch | `:2036-2098` | by the chooser's selected filter, not by the file's extension |

Probe `B2` confirmed the chooser presents five filters: All Files, "GeoCeDG
Files (.cedg, .ggb, .ggt, .html, .htm)", "Insert File (.ggb)", "Apply Template
(.ggb)", "OFF file (.off)" (P).

**Route matrix (P unless marked).**

| Route | Input | Result |
|---|---|---|
| document filter | `.cedg`/`.ggb` | ordinary Open in the window (`doOpenFiles(files, true, ext)` → `loadFile`); the transactional native path |
| document filter | name without extension | private `doOpenFiles` fallback chain (GMD `:2135-2166`): `ext` (GeoCeDG: `.cedg`), then `.ggb`, then `.ggt`, `.html`, `.htm`, **`.off`**; a missing file shows "File not found" (`:2168-2180`) (S) |
| document filter | explicit `chooser-shape.off` | **OFF imported into the current document** (`loadOffFile`): a 3D polyhedron (`A..D`, `t1`) added to the existing construction on the working layer; document stays **saved**, current file unchanged |
| any `doOpenFiles` caller | name `shape` (only `shape.off` exists) | OFF imported through the fallback (probes `O-typed-stem-public` and `O-typed-stem-cedg-route`) |
| Insert File filter | explicit `.cedg` | Insert File of the `.cedg` (the filter only hides `.cedg` in the listing); `D1` notice shown; success dialog |
| Insert File filter | `.ggb` | Insert File |
| Insert File filter | name without extension (only `x.cedg` exists) | `.ggb` appended (GMD `:2069-2071`) → "File not found: x.ggb" → the **previous copy buffer is pasted** with a placeholder label and "Import successful: No objects were renamed" (`CF-F3-1`) |
| Apply Template filter | explicit `.cedg` | Apply Template of the `.cedg` (all objects re-layered, `K7`) |
| OFF filter | name without extension | OFF imported; the loop computes `file0` with `.off` and discards it (GMD `:2083-2091`), and the fallback chain reaches `.off` anyway |
| Insert File / Apply Template / OFF filter | **Cancel** | `NullPointerException` on the EDT at GMD `:2065` / `:2046` / `:2083` (`files` is `null`); no document change (`CF-F3-6`) |
| document filter | Cancel | no change |
| drag and drop | `.off`, `.html`, `.htm` | rejected; only `.ggb`/`.cedg`/`.ggt` reach `doOpenFiles` (`desktop/gui/FileDropTargetListener.java:102-157`) (S) |
| command line | `x.off` | `handleFileArg` → `loadFile` → non-transactional XML load (AppD `:1215-1294`, `:3039-3047`, `:3105-3118`); not an OFF route (I) |
| internal | `AppD.loadOffFile(File)` (`:3056-3077`), `GuiManagerD.loadOffFile` (`:2291-2294`, private), `OFFHandler` | upstream support; no GeoCeDG caller (S) |

**Extension policy facts (S).** `doOpenFiles(File[], boolean)` is public and
non-final (GMD `:2121-2124`, default extension `.ggb`); the three-argument
variant used by the document filter is private (`:2126`); `openFile(File)` is
public and non-final. `GuiManagerGeoCeDG` already overrides the two protected
document-filter hooks (`:178-186`). Removing the OFF filter alone leaves the
explicit-`.off` and the extension-less fallback routes open.

## K5 — Characterization C: Insert File semantics

**Code path (S).** `AppD.insertFile` (`:4775-4828`, `final`):
`newAppForTemplateOrInsertFile()` → `helper.getGuiManager().loadFile(file, false)`
(`:4782`; the load result is ignored) → `getXMLio().processXMLString(helper.getAllMacrosXML(), false, true)`
(`:4788-4797`) → duplicate labels = target labels ∩ helper labels
(`:4799-4805`) → if any, `helper.getGuiManager().shouldRenameObjectsOnInsertFile`
(Rename / Overwrite / Cancel, GMD `:3128-3141`; Cancel returns at `:4811-4813`
**without disposing the helper**) → `getCopyPaste().insertFrom(helper, this, duplicates, overwrite)`
(`:4819`) → `showRenamedObjectsDialog` (`:4821`, GMD `:3144-3154`, title
"Import successful", always shown) → `helper.exitFrame()` (`:4822`) →
`helper.getFrame().dispose()` (`:4826`). `CPD.insertFrom` (`:644-684`) copies
every helper object in construction order with `putdown = true`
(`copyToXML`, `:279-434`), preflights the fragment, records the duplicate set
in the private field `duplicateLabels` (`:658`), protects every source label in
the target (`:659-662`), removes overwritten target objects (`:663-668`) and
pastes (`pasteFromXML(toApp, true)`, `:457-560`); identity-bearing payloads run
the mutation inside the atomic rollback boundary (`:670-681`,
`common/util/InternalClipboard.java:620-657`).

**C1 — construction content (P).** All objects and algorithms are imported in
construction order with their dependencies re-bound to the inserted objects
(`M` depends on the inserted `s`, `N = MyMid(B, A)` is an `AlgoMacro` over the
inserted `A`, `B`); a `SplineV2`/Locus V2 source (`S = SplineV2({A,B,C,D},3)`)
is imported as a defined `GeoLocusV2`. Inserted objects keep their source
layers (`A` on 3, `s` on 6), never the working layer (paste is excluded by
`geocedg-desktop/GeoCeDGLayerWorkspace.java:150-163`). The source's hidden-layer
set is not imported (as `A-2` requires), so objects of a source-hidden layer are
shown in the target unless the target hides that layer.

**C2 — macros (P).** Every source macro is imported **twice**: once by
`insertFile` (`:4789`) and once by `pasteFromXML` for the copied macros
(CPD `:513-532`); a source with `MyMid` yields `MyMid` and `MyMid1` in the
target. This also happens in Classic (`I-classic`). With a **name collision**
(target already defines a different `MyMid`) the source macro is imported under
renamed commands (`MyMid1`, `MyMid2`) while the inserted `N` binds to the
**target's** `MyMid`: its value becomes `(3.67, 4)` instead of the source's
`(5.5, 6)`, a silent semantic substitution (`CF-F3-3`). A failure of the macro
import is logged and ignored (`:4794-4797`) (S). Cancelling the label dialog
leaves the already imported source macro in the target (`C-cancel`:
`MyMid MyMid1`) (`CF-F3-4`).

**C3 — labels (P).** Free labels are kept. With duplicates the inherited dialog
offers:

| Choice | Target object `A` | Target dependent `c = Circle(A, 1)` | Inserted `A` |
|---|---|---|---|
| Rename | unchanged, same instance | unchanged | renamed `A_{1}`; its dependents re-bound |
| Overwrite | **removed** | **removed** (`GeoElement.remove` deletes dependents) | takes label `A` |
| Cancel | unchanged | unchanged | nothing inserted |

Overwrite identifies the "same" object only by label equality (`CPD:663-668`);
it is a user-chosen replacement, not an identity inference by the product, but
it deletes the target's dependent constructions without further warning
(`CF-F3-5`). Identity-bearing (G9A spatial) sources were not probed (`OTI-F3-4`).

**C4 — undo (P).** A successful Insert File stores **no undo point**
(`round1.stored = false`, history unchanged). The insertion is merged into the
next stored step: after a following action and one Undo, both the following
action and the whole insertion disappear (`U2-insert`); the imported macros
remain after that Undo. The document becomes unsaved (`saved = false`).
Cancellation stores nothing (`CF-F3-2`).

**C5 — document state (P).** Current file, existing object instances, units,
working layer, hidden-layer set and drawing scale of the target are unchanged;
the document is marked unsaved. The source file is added to the **shared
recent-file list** (`K9`).

**C6 — repeated insertion (P).** The second insertion of the same source finds
all its labels duplicated (`Duplicate objects: A, B, s, M, N`); Rename adds
`A_{1}` … `N_{1}`; each insertion adds two more macro copies (`MyMid` …
`MyMid3` after two insertions); no undo point either time.

**C7 — clipboard and later Paste (P).** A successful insertion replaces the
window copy buffer with the inserted document and its provenance (D1 design,
`GeoCeDGCopyPaste` Javadoc). A later Ctrl+V (`GlobalKeyDispatcherD.handleCtrlV`
→ `pasteFromXML(app)`, `desktop/main/GlobalKeyDispatcherD.java:263-274`) pastes
the inserted document again, keeps the `D1` notice, but creates objects
labelled **`CLIPBOARDmagicSTRINGA`**, `…B`, `…s`, `…M`, `…N`. Cause (S): the
stale `duplicateLabels` set left by `insertFrom` makes `handleLabels` treat
every label as free and `setLabel` to an existing label keeps the placeholder
(`common/util/CopyPaste.java:461-472`). Identical in Classic
(`K-paste-after-insert`); an ordinary copy and paste without a prior insertion
labels correctly (`R_{1}`) (`CF-F3-7`).

**C8 — failures (P).** With a non-empty copy buffer (`R` copied):

| Source | Visible result | Construction afterwards |
|---|---|---|
| missing `.cedg` | "File not found" dialog, then "Import successful: No objects were renamed" | `R` + **`CLIPBOARDmagicSTRINGR`** |
| `not a zip` as `.cedg` / `.ggb` | "GeoGebra - Error: Opening file failed" (EDT modal), then success dialog | same |
| `.txt` | load failure, success dialog | same |
| malformed archive (truncated `geogebra.xml`) | load failure, success dialog | same |
| `.off` | load failure (read as XML), success dialog | same |
| valid empty `.cedg` | success dialog only | same |

Cause (S): the helper load result is ignored (AppD `:4782`); with an empty
helper, `copyToXML` returns before resetting the buffer (CPD `:281-283`), so
`insertFrom` finds the **old** buffer non-empty (`:650-652`) and pastes it with
the old provenance; the success dialog is unconditional. The document is marked
unsaved. Classic shows the same (`K-missing`). Nothing guarantees rollback of
the macro import, the protected labels or an Overwrite removal outside the
identity-bearing path; the plain path runs the Overwrite removal before the
paste and its rollback snapshot is taken after it (`InternalClipboard.java:623`)
(S/I) (`CF-F3-1`).

## K6 — Characterization D: `D1` units and provenance

**Mechanism (S).** The target's `GeoCeDGCopyPaste`
(`geocedg-desktop/GeoCeDGCopyPaste.java:22-57`) records, whenever `copyToXML`
replaces the buffer (`:31-38`), the unit state of the construction it copies
**from**: during `insertFrom` that is the helper, which holds the inserted
document's loaded `<geocedgUnits>`. The notice is emitted once at the end of the
paste through `onPasteCompleted` (`:47-49`, CPD `:559`, `:570-572`) by
`AGC.unitPasteCompleted` (`:302-313`) on the target's status bar.

**Matrix (P, probe `units`; target drawing scale 1:50).**

| Case | Source | Target | Notice | Target units after | `A` |
|---|---|---|---|---|---|
| D0 | mm | mm | none | mm | (5, 5) |
| D1 | m | mm | "Pasted from a document in m into a document in mm; coordinates were not converted." | mm | (5, 5) |
| D2 | mm | unspecified | none | unspecified | (5, 5) |
| D3 | unspecified | mm | none | mm | (5, 5) |
| D4 | unspecified | unspecified | none | unspecified | (5, 5) |
| D5 | mm, presentation cm | mm | none (same physical meaning) | mm | (5, 5) |
| D6 | usm 0.001 m | mm | none (same metre factor) | mm | (5, 5) |
| D7 | usm 0.0254 m ("in") | mm | "… in in (1 usm = 0.0254 m) into a document in mm …" | mm | (5, 5) |

In every case the provenance equals the source state, the target `UnitState` is
unchanged, the coordinates are not scaled, the drawing scale stays 1:50, and
the document XML carries no provenance. `.ggb` (Classic, no unit metadata)
yields an unspecified provenance and no notice. The invariants of the
instruction (no silent rescaling, construction-copy semantics, target state
unchanged, provenance through the existing session mechanism, the approved
notice policy, kernel coordinate authority) hold for every successful
insertion. They **do not hold on the failure paths** of `C8`, which re-paste the
previous buffer with its previous provenance. No shared or kernel change is
needed to keep them (`CF-F3-1` is a Desktop-layer orchestration defect).

## K7 — Characterization E: Apply Template

**Code path (S).** `AppD.applyTemplate` (`:4837-4856`, `final`):
`helper.loadFile(file, false)` (result ignored) →
`new TemplateHelper(this).applyTemplate(helper)` → `helper.exit()` →
`helper.getFrame().dispose()`. `TemplateHelper.applyTemplate`
(`common-jre/main/TemplateHelper.java:43-48`) sets the labeling style, copies the
construction defaults (`ConstructionDefaults.setConstructionDefaults`,
`common/kernel/ConstructionDefaults.java:1293-1301`: `setVisualStyle` of each
default type present in the template), restyles the **selected objects, or all
objects when nothing is selected** (`:53-71`) through
`setDefaultVisualStyles(geo, false, false, false)` while keeping label
visibility, and updates the construction.

**E1 — construction defaults (P).** The template's default free point (red,
size 9) becomes the target default; a point created afterwards is red, size 9,
on the working layer. A `.cedg` template changes the labeling style (0 → 2 in
the probe); the Classic `.ggb` template carried style 0.

**E2 — object styles (P).** With nothing selected every object is restyled:
colour and point size from the defaults (segments keep the black segment
default), and also **caption cleared** (`cap` → empty), **fixed flag cleared**,
**label offset reset** (17 → 0) (P). `setAllVisualPropertiesExceptEuclidianVisible`
with `keepAdvanced = false` copies from the default object the label offsets
and caption (`common/kernel/geos/GeoElement.java:1143-1146`) and, through
`setAdvancedVisualStyleNoAuxiliary` (`:1356-1373`), the basic style, the fixed
and selection flags, the layer, the colour function and colour space and the
show-object condition (S); existing dynamic colours and show conditions are
therefore replaced by the defaults' (I, not probed). Hidden
objects stay hidden (Euclidian visibility is not applied) but are restyled.
With one object selected only that object changes. Independent and dependent
objects are treated alike; identities and dependencies are unchanged (`R` same
instance; `seg` still depends on `R`). GeoCeDG-specialized objects (native
dimension outputs, Locus V2 without a default type) were not probed; by reading
they receive the line defaults (`ConstructionDefaults.java:961-967`) (I,
`OTI-F3-5`).

**E3 — units and geometry (P).** The target `UnitState` is unchanged for `.cedg`
templates with other units and for `.ggb`; no template object (`T`) is
imported; no coordinate changes.

**E4 — working and hidden layers (P).** Target: `R` on 0, `P2`/`seg`/`H` on 2,
`P4` on hidden layer 4, `Q` on 5; working layer 7.

| Variant | Layers afterwards |
|---|---|
| GeoCeDG, whole document (`.cedg` or `.ggb` template) | **every object on 7** (the working layer), including `H` (hidden object) and `P4` (from hidden layer 4, now **shown**) |
| GeoCeDG, `P2` selected | only `P2` → 7 |
| GeoCeDG through the real Open dialog, working layer 4 | `R`, `P2` → 4 |
| Classic | every object → 4 (the capped highest used layer) |

Cause (S): the non-reset branch calls `setMaxLayerUsed`
(`ConstructionDefaults.java:1003-1006`), which routes through the `A-1` seam
`App.getLayerForNewObject` (`common/main/App.java:1404-1406`, `ConstructionDefaults.java:922-931`);
`GeoCeDGLayerWorkspace.layerForNewObject` returns the working layer for any
change in the document construction outside loads and document transitions
(`GeoCeDGLayerWorkspace.java:150-163`). The hidden-layer set and the working
layer themselves are unchanged. Because restyled objects leave hidden layers,
Apply Template reveals objects the user had hidden by layer (`CF-F3-8`). The
layer domain is not exceeded (the working layer is always in `0..maxLayer`).

**E5 — document state and undo (P).** No undo point; the document **stays
saved** after the restyling (closing then loses it without a prompt); labels,
identities and dependencies unchanged; the change is merged into the next
stored undo step (`U2-template`: one Undo after a following action restores the
pre-template styles and layers) (`CF-F3-2`). The template file is added to the
recent-file list.

**E6 — failed template load (S/I).** `AppD.applyTemplate` ignores the result of
`helper.loadFile` (`:4845`); after a missing or rejected template the shared
`TemplateHelper` still runs against the empty helper, whose defaults and
labeling style are the factory ones, so every target object would be restyled
to factory defaults (inferred, not probed) (`CF-F3-12`).

## K8 — Characterization F: `.cedg` and `.ggb` compatibility

| Operation | `.cedg` | `.ggb` |
|---|---|---|
| Insert File (route `AppD.insertFile`) | works (P): GeoCeDG-native features (units, layers, macros, Locus V2/Spline V2) | works (P): Classic objects and macros; no unit metadata → no notice |
| Apply Template | works (P) | works (P) |
| reachable today | only by typing or selecting a `.cedg` under the `*.ggb` Insert/Template filter, or through "All Files" | yes |

The helper loads both through the transactional native path:
`AGC.loadExistingFile` (`:1276-1289`) → AppD `:3105-3118` →
`readAndPreflightNativeArchive` (`:3175-3192`, `DocumentArchivePreflight` with a
fresh GeoCeDG parser config, AGC `:1760-1766`) → `loadNativeArchive`
(`:3284-3344`); `.ggb` is transactional in GeoCeDG (AGC `:1795-1798`). A
rejected archive (not a zip, malformed XML, fail-closed unit or hidden-layer
metadata) leaves the helper empty with an error dialog; the insertion then
continues (`C8`). Features the product rejects on load are therefore rejected,
never partially imported, by the helper; the defect is only that the caller
does not stop. Disabled runtime features follow the target's flags because the
helper copies them (AGC `:1336-1339`).

## K9 — Hidden helper lifecycle

| Aspect | Fact |
|---|---|
| construction | `new AppGeoCeDG(args null, new JPanel(), new AppConfigGeoCeDG(target LocusV2 flag, target extended-DXF flag), newDocumentDefaults = false)` (AGC `:1332-1340`, private constructor `:184-192`): full Desktop GUI manager, layer workspace, theme, presentation and unit listeners (S) |
| unit defaults | none applied (`initializeDocumentUnits(false)`, `:196-202`); the loaded document's state replaces the empty one (S) |
| loading | its own `loadExistingFile` (transactional), so it sets **its** current file and publishes it to the **static** recent-file list `AppD.fileList` (AppD `:273`, `:1088-1117`); every successful insert or template adds the source to the user's recent list (P) |
| clipboard | the insertion uses the **target's** `GeoCeDGCopyPaste` with the helper as source; the helper's own buffer is unused (S) |
| dialogs | the label-conflict dialog is parented to the helper's hidden panel (GMD `:3131`); load errors are EDT-modal "GeoGebra - Error" dialogs of the helper (AppD `:4023-4060`) (S/P) |
| windows | `getFrame()` creates a `GeoGebraFrame` lazily (AppD `:2640-2646`, GMD `:2562-2568`); disposed on the success path; **not disposed after Cancel** (+1 frame instance) (P) |
| global registrations | each helper leaves its `DockManagerD` registered as an AWT event listener (+1 per operation, never removed, P) and, by the test-host comment, its key event dispatcher (`tests/G9U1TestApp.java:163-174`), retaining the helper (I) (`CF-F3-9`) |
| preferences | `exitFrame` → `GeoCeDGWorkspaceController.saveCurrentLayout(helper)` (AGC `:1316-1320`, `GeoCeDGWorkspaceController.java:84-111`); in the probe no preference key was written (the helper layout is not measurable) (P) |
| error paths | see `C8`; the helper is created for every attempt, including a missing file |
| repeated operations | one new helper per operation; registrations accumulate (P) |

The helper never replaces the user's document (P). Its observable session
effects are the recent-file list, the leaked registrations and, after a Cancel,
the undisposed frame.

## K10 — Classic and legacy fallback

Classic (`GeoGebra3D` → `App3D` → `GuiManager3D`; its helper is an `App3D`,
`desktop/geogebra3D/App3D.java:443-446`) shares every inherited route above:
four filters, OFF, the Cancel NPE, the stale-buffer re-paste
(`K-missing`), the placeholder labels after an insertion and the duplicate macro
import (`I-classic`), and Apply Template re-layering to the capped highest used
layer (`T-classic`). None of these is introduced by GeoCeDG. A GeoCeDG-only
product route keeps Classic unchanged provided it adds no inherited-class
change. The v1 fallback reaches the inherited Open dialog through `FileMenuD`
(`K3`).

## K11 — Anomalies and degenerations

| Id | Anomaly | Scope | Classic | F3 relevance |
|---|---|---|---|---|
| `CF-F3-1` | a failed, unsupported or empty Insert File re-pastes the previous copy buffer with placeholder labels and reports success | Insert File | same | the new route must not reproduce it (`DQ-F3-6`) |
| `CF-F3-2` | Insert File and Apply Template store no undo point; merged into the next step; Apply Template leaves the document saved | both | same | `DQ-F3-6`, `DQ-F3-8` |
| `CF-F3-3` | a macro-name collision rebinds inserted objects to the target's macro | Insert File | same mechanism | `DQ-F3-5` |
| `CF-F3-4` | source macros imported twice; kept after Cancel | Insert File | same | `DQ-F3-5`, `DQ-F3-6` |
| `CF-F3-5` | Overwrite deletes the target object's dependents | Insert File | same | `DQ-F3-5` |
| `CF-F3-6` | Cancel with the Insert/Template/OFF filter throws an NPE on the EDT | Open dialog | same | disappears from GeoCeDG if the filters leave its Open dialog (`DQ-F3-2`) |
| `CF-F3-7` | Ctrl+V after Insert File creates `CLIPBOARDmagicSTRING…` labels | later Paste | same | `DQ-F3-10` |
| `CF-F3-8` | Apply Template moves restyled objects to the working layer and reveals hidden-layer objects; also clears captions, fixed flags and label offsets | Apply Template | capped highest layer | `DQ-F3-9`, `DQ-F3-8` |
| `CF-F3-9` | helper publishes to the recent list; leaks registrations; frame undisposed after Cancel | both | same | `DQ-F3-10` |
| `CF-F3-10` | OFF imports a 3D polyhedron into the current document and leaves it saved | Open routes | same | `DQ-F3-4` |
| `CF-F3-11` | the save-changes dialog precedes Insert File and Apply Template | Open dialog | same | `DQ-F3-2` |
| `CF-F3-12` | a failed template load still applies the helper's factory defaults to the target (inferred) | Apply Template | same | `DQ-F3-8` (pre-validation) |

No geometric degeneration was found: coordinates, dependencies and algorithm
classes are preserved by a successful insertion; the only geometric-semantic
change is the macro substitution of `CF-F3-3`.

## K12 — Architecture assessment

| Behavior | Owner today | Proposed owner (design §10) |
|---|---|---|
| File → Insert surface, action identities, EN/ES texts | — | profile + `GeoCeDGActionRegistry` + `GeoCeDGMenuBar` (Desktop product) |
| GeoCeDG chooser presentation and extension policy | shared `GuiManagerD.openFile` | GeoCeDG-owned override in `GuiManagerGeoCeDG` plus a GeoCeDG insertion chooser; policy in `GeoCeDGDocumentPolicy` |
| orchestration of Insert File / Apply Template (pre-validation, undo point, layer preservation, helper cleanup) | `AppD` (`final`) | a GeoCeDG orchestration class calling the unchanged `AppD.insertFile` / `AppD.applyTemplate` |
| copy, paste, labels, macros, object identity | shared `CopyPaste`/`CopyPasteD`, kernel | unchanged; GeoCeDG `GeoCeDGCopyPaste` may refuse an empty source (product seam, `insertFrom` is public, non-final) |
| unit authority, provenance, notice | kernel unit system + `D1` Desktop | unchanged |
| construction defaults and template restyling | `ConstructionDefaults`, `TemplateHelper` | unchanged (forbidden) |
| document loading and parsing | `AppD`, shared XML | unchanged |

Every recommended alternative is implementable without an inherited-class
change (design §10). The two inherited-change options that exist are named,
with their Classic effect, as non-recommended alternatives: `GuiManagerD`
hooks (`DQ-F3-2` A2) and a one-line `CopyPasteD` correction for `CF-F3-7`
(`DQ-F3-10` P2). No recommendation needs a shared-kernel, serialization,
parser or unit-semantics change; the architectural proposal is therefore not
stopped (`K6`).

## K13 — Verification-planning facts

- `verification-levels.md` §12.8: `INTEGRATED_PHASE` = `PHASE`, plus
  `COMPOSED`/`INTEGRATION` only when the frozen plan identifies concrete
  additional integration coverage; a request above the frozen plan fails with
  `VERIFICATION_ESCALATION_REQUEST`.
- Precedents: `F1` registers `compile.shared.semantic`,
  `compile.desktop.semantic`, `workspace-profile.product` and its JUnit
  selection; `F2-R1` `compile.desktop.semantic`, `workspace-profile.product` and
  its selection (`verification-registry.json:88-90`).
- Pins and allow-list of `K3`; registry-shape pins in
  `tools/agent/tests/verification-final-coverage.Tests.ps1` move when a phase
  selection is registered.
- Guides: Insert File is described as a file type of File → Open in
  `docs/user/geocedg_user_guide_en.md:539-542` and
  `docs/user/geocedg_user_guide_es.md:567-569`; the File tables at EN
  `:330-340`, ES `:342-352` and the quick references at EN `:1985-1986`, ES
  `:2082-2083`; Apply Template and OFF are not documented. Outline pins:
  `tests/PreG9BR5AGuideOutlineTest.java:55-58`, `:224-226`.

## K14 — Probe record

Route: scratch JUnit classes `org.geocedg.desktop.F3PrepProbeTest` and
`org.geocedg.desktop.F3PrepProbe2Test` compiled into the Desktop test source set
by a Gradle init script; extensions `G9U1TestApp.Lifecycle`,
`EmptyUnitPreferences`, `DesktopLogger`; isolated preference file in the
scratch directory; `JOptionPane` mocked on the test thread.

| Output | Content | SHA-256 |
|---|---|---|
| `f3-probe.insert.txt` | `K5` C1, C5, C7 (`.cedg`, `.ggb`) | `bbd11b67c20958fdf5bb03a6698cae20e3ae9482dfcd1988e40a11a4b90982b5` |
| `f3-probe.conflicts.txt` | `K5` C2, C3 (Rename, Overwrite, Cancel) | `ac8b06f009662d08a87f23d541051b9c68a3eacdc094c6412d3309e9bb76cee4` |
| `f3-probe.undo.txt` | `K5` C4, C6 | `ba9057aa875c33957aeca805ccab165aae88625a2468f58800312331b0feb358` |
| `f3-probe.undo2.txt` | `K5` C4, `K7` E5 (merge into the next step) | `5980a9496467d0c68c84a6792d6f294b692c346c7a08e9d10f6366c85a83f4da` |
| `f3-probe.failures.txt` | `K5` C8 | `beca18cc300e05d7b30df7c49ed27e436558486ab52c24e645b40f6e1263f559` |
| `f3-probe.units.txt` | `K6` D0–D7 | `03498bde93c168f6a46fe0229f4fb23a7a2324daeed921513c4d2172565d3dd2` |
| `f3-probe.template.txt` | `K7`, Classic Apply Template and Insert File | `4353037ad67d0f6c098170d7a2022f49b7ae19bdd3a1b4c7a6fd9cd4868bb4e3` |
| `f3-probe.open.txt` | `K4` OFF through `doOpenFiles` | `b04aa852bbfb507b91eb8073d3baf0fce6ae4df988119b961980d5d343c1d450` |
| `f3-probe.chooser.txt` | `K4` filter list (its filter selection did not take: superseded by `chooser2`) | `4c415d00df35bca00663c58ce95b232a604ba4a982631db14a0a36e2988897bd` |
| `f3-probe.chooser2.txt` | `K4` real chooser routes, Cancel NPE | `e012e17c0d293e03b4c212851b29000db90fa80f044de348f0cad29abbb28cf5` |
| `f3-probe.classic.txt` | `K10` Classic stale buffer and placeholder labels | `6640c14ee16a75e75a513f96dd310aab08bef04a841026cd42644fe59b318b46` |
| `f3-probe.native.txt` | `K5` C1 Spline V2 / Locus V2 source | `5474121bf17edab637d5dcbdc431efd374c105f5ac548bcfd952020d6baf57b5` |
| probe source | `F3PrepProbeTest.java` | `4ff803136704f4dcb87b359895e46ee2c086c4d16ccde2be37838ed3837c8a61` |
| probe source | `F3PrepProbe2Test.java` | `19118189fad689de6801e13a75dcfea58f42d3363f8de28d861ecac27cd47f4e` |
| init script | `f3probe.gradle` | `363e58541e963713244496751f791a5af6e400f1cc96f01890026744a6883851` |

Runs: the first attempt did not compile (a probe used a non-existent
`setMaxLayerUsed`); the second (`F3PrepProbeTest`, 7 tests) and the third
(`F3PrepProbe2Test`, 4 tests) exited 0. In the first `chooserFilters` method
the filter was looked up by its bare name while the chooser appends the
extension list ("Insert File (.ggb)"), so its approve cases ran ordinary Open;
`chooser2` repeats them correctly and is the evidence used. After the runs:
`:desktop:desktop:compileTestJava` without the init script (exit 0); the probe
test-result XML and two compile-stash copies removed; `git status` clean.

## K15 — Open technical items and points left to the author

- `OTI-F3-1` — whether a GeoCeDG pre-validation of a source (existence,
  extension, `DocumentArchivePreflight`) plus an empty-source refusal in
  `GeoCeDGCopyPaste.insertFrom` covers every failure path of `C8` without a
  second full load (design §5.2; implementation entry gate).
- `OTI-F3-2` — whether a macro-collision guard can fail closed before any
  target mutation without an inherited change (the macro import precedes the
  label dialog inside the `final` `AppD.insertFile`) (`DQ-F3-5`).
- `OTI-F3-3` — removing the helper's global registrations after the operation
  without touching `AppD` (`DQ-F3-10`).
- `OTI-F3-4` — identity-bearing (G9A spatial) sources and Overwrite inside the
  atomic boundary were not probed.
- `OTI-F3-5` — Apply Template on native dimension outputs, Locus V2 and Spline
  V2 objects was not probed.
- The twelve questions `DQ-F3-1` to `DQ-F3-12` of the
  [decision preparation record](pre_g9b_r6_plus_f3_author_decision_preparation_record.md).

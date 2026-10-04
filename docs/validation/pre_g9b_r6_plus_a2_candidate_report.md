# PRE-G9B-R6-plus-A-2 — layer domain 0..99 and persistent hidden layers: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN (revision 2: non-native document replacement
                            commit, §17)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PREVIOUS_CANDIDATE        = e3cbdc60743b9f91490f7bb6a80f789d87a8f814
                            tree 235869d7c127dc9d90b104cbd10a5c461ee0690c
                            FINAL verification-b7942a8be41c44728a1a6b6289ecf08a
                            ACCEPTED / COMPLETE (historical evidence for that
                            candidate only; superseded for acceptance)

PHASE                     = PRE-G9B-R6-plus-A-2
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile FINAL -PlanOnly   (plan resolution only)
                            tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\<fresh-name>
                            (exactly one FINAL, on the exact candidate commit and tree)
PRODUCT CHANGE            = YES (GeoCeDG layer domain 0..99 read from the product
                            configuration; document-wide hidden layers persisted with
                            the document, never an undo point; Open working-layer rule)
SERIALIZATION CHANGE      = YES: the admissible <layer val> of GeoCeDG documents
                            widens to 0..99; a new document-level
                            <geocedgHiddenLayers version="1" layers="..."/> element
GUIDE_IMPACT              = UPDATED
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval. The candidate commit cannot name
itself, so its identity and the single `FINAL` made on it are reported outside
this file. The machine-readable mirror is
[`pre-g9b-r6-plus-a2-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-candidate-evidence.json).
The execution contract is the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md)
as amended by the authorization edit, with the `DQ-A2-1`–`DQ-A2-11` dispositions
of the [preparation closeout and authorization record](pre_g9b_r6_plus_a2_prompt_closeout_record.md);
this report restates neither.

## 1. Authorization and entry identity

The author approved the `F3` planning reconciliation (`PRE-G9B-R6-plus-F3 =
PLANNED — AUTHOR APPROVED — IMPLEMENTATION NOT AUTHORIZED`) and the `A-2`
preparation package, and authorized `PRE-G9B-R6-plus-A-2` for implementation
on 2026-10-03 (`GLOBAL_IMPACT`, one `FINAL`, `selfApproved = false`).

| Identity | Value |
|---|---|
| published base `P_R6PLUS_D1` | `2941ddf2f2712340e831d292bdc2687b879850d6`, tree `158184b150a42e6dcae2946c39620d92cbfcd888` |
| `F3` planning `T_R6PLUS_F3_PLAN` (local) | `f467eb4563de7a7eb162da1b74954f9f847fa9d3`, tree `ece51c96117d6cbd55bc200d29c5240686516da9` |
| prepared candidate `T_R6PLUS_A2_PROMPT` (implementation branch start, immutable) | `0e50626f70107376e691df6dea7bfcfe06de5c0a`, tree `fefc57cece8090941ad0ac16098e58a679a290ec` |
| implementation branch (local, never pushed; not identity) | `phase/pre-g9b-r6-plus-a2-layer-domain-persistence` |
| preparation branches (kept, unchanged) | `phase/pre-g9b-r6-plus-f3-planning` at `f467eb45`, `phase/pre-g9b-r6-plus-a2-prompt` at `0e50626f` |

The implementation branch was created from the exact prepared candidate and was
never rebased. The prepared candidate was neither modified nor amended.

## 2. Canonical prompt amendment

One tracked edit of
`.github/prompts/tasks/pre-g9b-r6-plus-a2-layer-domain-and-hidden-layer-persistence.prompt.md`,
in its own local commit before any product change:

| Commit | Tree | Content | Prompt blob |
|---|---|---|---|
| `0e50626f` | `fefc57ce` | prepared prompt, `PREPARED — NOT AUTHORIZED` | `459dbf5dc620a966d8dd593ae899010470789043` |
| `bc7436e8d4eb8c35c9bec7ab107c7fc13f63f67c` (`A_R6PLUS_A2_PROMPT`) | `8df989d752f7cc3b640a40fb461f3f6a571bdccb` | `AUTHORIZED FOR IMPLEMENTATION`; the `DQ-A2-1`–`DQ-A2-11` dispositions; the superseded defaults corrected (`DQ-A2-2` saved state, `DQ-A2-6` commit context, `DQ-A2-7` cap wording, `DQ-A2-9` renderer-only adapter); the twelve regression obligations mapped to test IDs; the added stop conditions; `GLOBAL_IMPACT` / `FINAL` frozen | `0e87c8e72091d44f3eb0a1d0e7fc0ceb68b07e2a` |

The same commit adds the closeout and authorization record and its JSON mirror.
The prompt is unchanged after that commit.

## 3. What changed

### 3.1 Configured layer domain (`AQ-L1a`, `DQ-A2-10`, `DQ-A2-11`)

The bound is product configuration, read by shared validation:

- `AppConfig.getMaxLayer()` — default method returning the inherited
  `EuclidianStyleConstants.MAX_LAYERS` (9, unchanged);
  `AppConfigGeoCeDG.getMaxLayer()` returns `AppConfigGeoCeDG.L_MAX = 99`, the
  only `99` literal in product code.
- `LayerDomain.maxLayer(app)` and `LayerDomain.clamp(layer, maxLayer)` in the new
  package `org.geocedg.common.kernel.layers`; a detached element without an
  application or configuration keeps the inherited bound.
- Enforcement sites that read it: `GeoElement.setLayer` (clamp `< 0 → 0`,
  `> bound → bound`, no wrap), `App.updateMaxLayerUsed`,
  `ConstructionDefaults` (`min(bound − 1, maxLayerUsed)`, `DQ-A2-7`: a cap,
  never a default layer; GeoCeDG 98, Classic 8), `CmdShowHideLayer` and
  `GgbAPI.setLayerVisible` (`DQ-A2-8`: range only, object-visibility semantics
  unchanged, not rerouted), `LayerModel` and `LayerProperty` (`DQ-A2-11`: the
  index is the layer; 100 entries in GeoCeDG, 10 in Classic).
- Desktop: `GeoCeDGLayerWorkspace` takes its bound from `LayerDomain`; the
  working-layer chooser offers `0..L_MAX` and its text carries the bound as a
  parameter (`Workspace.Layer.ChooserMessage`, `%0`); no `99` literal in Desktop.

Every other consumer already used the layer value generically (draw order by the
priority comparator, the hit-test top-layer rule, the Algebra View numeric
order, macro and Locus V2 outputs through the `A-1` seam, `GeoList` and
polyhedron propagation); the tests of §10 prove each with layers above 9.

### 3.2 Classic separation (`DQ-A2-5`)

`AppConfigDefault` inherits both default methods, so Classic Desktop, the
Classic diagnostic process, headless Classic tests and Web keep the bound 9 and
persistence off. Every changed site is value-for-value identical for them:
`setLayer`, `SetLayer`, the API and the XML reader clamp `10, 50, 99, 100, 1000`
to 9 as at the base; `HideLayer(42)` and `setLayerVisible(42)` are no-ops; the
Properties combo and `LayerProperty` have 10 entries; the default cap is 8; the
hidden-layer element is logged, ignored and never re-written; a Classic
re-save of a GeoCeDG document writes `9` and no element (`T-CLASSIC`, shared and
Desktop). `EuclidianStyleConstants.MAX_LAYERS` is unchanged.

### 3.3 Hidden-layer owner and value types (`C4`)

`GeoCeDGLayerWorkspace` (Desktop, one per `AppGeoCeDG`) stays the single runtime
owner of the hidden set and the working layer; the `A-1` predicate
(`EuclidianView.isOnShownLayer` → `App.isLayerShown` → the workspace) is
unchanged. The shared layer gets only `HiddenLayerSet` (immutable, strictly
ascending), the codec `HiddenLayersXml`, the dedicated unchecked
`HiddenLayerMetadataException` with its structured `Code`, and three `App`
hooks with upstream-identical defaults:

| Hook | Host default | GeoCeDG |
|---|---|---|
| `getDocumentHiddenLayers()` | empty set | the workspace set |
| `documentHiddenLayersParsed(set)` | ignore | record for the next document transition |
| `hasSaveRelevantDocumentPresentation()` | `false` | the set is non-empty |

Hidden layers are never geometric, DAG, construction or object state; nothing
is added to `Construction`.

### 3.4 XML grammar and placement (`DQ-A2-3`, `DQ-A2-4`)

The prepared grammar applies unchanged:

```text
hidden-element = "<geocedgHiddenLayers" SP 'version="1"' SP 'layers="' layer-list '"' "/>"
layer-list     = layer *( SP layer )          ; strictly ascending, no duplicates
layer          = "0" / ( %x31-39 *DIGIT )     ; ASCII, no sign, no leading zero, <= L_MAX
```

- **Writer.** `MyXMLio.getFullXML` calls `HiddenLayersXml.write` after the
  construction and before `</geogebra>`: one canonical line, written exactly
  when the set is non-empty, exactly as held, with locale-independent bytes.
  `getUndoXML`, `getPreferencesXML`, `getFullMacroXML` and the clipboard never
  call it, so undo, preferences, macro, `.ggt` and clipboard XML never contain
  it.
- **Reader.** One case in `MyXMLHandler.startGeoGebraElement`, active only when
  the configuration persists hidden layers (`persistsDocumentHiddenLayers()`),
  on a non-macro kernel, for a parse whose effective load purpose (the `D1`
  rule) is `NATIVE_OR_UNDO_RESTORE`, `REDEFINE_REBUILD`, `ORDINARY_EDIT_REBUILD`
  or `ROLLBACK_RESTORE`. Every other parse (merge, paste, `evalXML`, macro,
  `.ggt`) logs and ignores it.
- **Fail closed.** `HiddenLayerMetadataException` with
  `UNSUPPORTED_VERSION` (a newer version), `MALFORMED_ELEMENT` (missing or
  non-integer version, an attribute outside the grammar, a missing or empty
  list, a sign, a leading zero, other separators, not ascending, a duplicate, a
  child or text), `OUT_OF_DOMAIN_LAYER` (above the reader's bound),
  `ALL_LAYERS_HIDDEN` (the set covers `0..L_MAX`), `DUPLICATE_ELEMENT` and
  `MISPLACED_ELEMENT` (anywhere but directly under `<geogebra>`). `MyXMLio`
  rethrows it and restores the complete pre-parse construction exactly as for a
  rejected spatial or unit parse; the Open preflight
  (`DocumentArchivePreflight`, GeoCeDG configuration) rejects the document
  before the live document is touched; `AppGeoCeDG` names the defect through
  the `D1` diagnostic route with localized `Workspace.Layer.LoadError.<CODE>`
  texts (`en`, `es`).

### 3.5 Persistence without undo (`C6`, `DQ-A2-6`)

The parser reports and never applies: `MyXMLio.doParseXML` classifies the parse
at its start (`beginHiddenLayerLoad`) and, only after the whole parse
completed, reports the set — the empty set when the element is absent — through
`App.documentHiddenLayersParsed` (`reportHiddenLayerLoad`). A rollback restore
validates but does not report (§4). `AppGeoCeDG` commits the last report only in
a document transition established by the live architecture:

| Commit context | Mechanism |
|---|---|
| every native `.cedg`/`.ggb` Open: File → Open, Open Recent, reset reload, archives with macros, the final `loadXML(File)` and `loadXML(URL)` (API `openFile`) | the new `AppD.nativeDocumentLoadCommitted()` hook, called once after the native load transaction committed; a failed load never reaches it |
| `loadXML(String)` and `loadExistingFile` | `runDocumentLoad`: clears the report, runs the transition, commits only when it succeeded |
| clearing `App.setXML(xml, true)` (API `setXML`, tool-creation reload, preferences reload of New) | overridden `setXML`: clears the report and commits only when the document parse reported |
| New | `resetForNewDocument` (`A-1`), then the preferences reload commits the empty set |
| startup with a file | the host constructor parses before the workspace exists; the report field has no initializer and `initializeLayerWorkspace` commits it; a failed startup ends blank with an empty set |

Undo and redo snapshots never contain the element, so an undo restore reports
the empty set, which no transition follows and the next transition discards.
Undo, redo, redefine rebuilds, atomic and paste rollbacks, rejected-parse
restores, paste, `evalXML`, merges, macro loads, Insert File and Apply Template
therefore never change the set. No undo-architecture change was needed.

### 3.6 Saved state (`DQ-A2-2`)

A user edit of the persisted set — an Algebra View eye, `setLayerHidden`, or a
working-layer choice that shows a hidden layer (`AQ-L7(2)`) — notifies the
workspace's persisted-set listener, which `AppGeoCeDG` binds to `setUnsaved`;
nothing is stored in the undo manager. A working-layer change alone and every
commit do not mark the document. `App.isSaved()` became
`isSaved || cons == null || !(cons.hasSaveRelevantContent() || hasSaveRelevantDocumentPresentation())`:
a geometrically empty document with a non-empty set is save-relevant and New
and closing prompt; an untouched empty document with an empty set stays saved.
`Construction.hasSaveRelevantContent()` and `isStarted()` are unchanged and the
set is not duplicated.

### 3.7 New, Open and fail-closed lifecycle (`C7`)

New: empty set, working layer 0. Open and every commit context: the report is
cleared when the transition starts; after success the workspace takes the
reported set (empty for a legacy document) and chooses the working layer by
`DQ-A2-1`; Open never writes the set, never unhides a persisted hidden layer,
never infers hidden layers from object visibility and never marks the document
modified. A failed Open (preflight rejection or a live-load failure) leaves the
previous set, working layer, file, saved state, recent list and undo history
exactly as they were.

### 3.8 `AQ-L7(3)` (`DQ-A2-1`)

`GeoCeDGLayerWorkspace.commitOpenedDocument(persisted)`: the hidden set becomes
exactly the persisted set, never modified; the working layer is the highest
layer used by a drawable object that is not hidden, else the lowest layer of
`0..L_MAX` that is not hidden. A GeoCeDG writer never produces case 5, because
the working layer can never be hidden. `openTakesTheDocumentSetAndChoosesAShownWorkingLayer`
opens each document below into a session whose working layer is 9 and whose
set is `{8}`; after Open the set equals the persisted set, the document is
saved and every object keeps its visibility:

| C8 case | Used drawable layers | Persisted set | Working layer after Open |
|---|---|---|---|
| 1 | 2, 6 | — | 6 |
| 2 | 2, 6 | 6 | 2 |
| 3 | 0, 4 | 0 | 4 |
| 3 and 6 | — | 0 | 1 |
| 4 | 2, 6 | 2, 6 | 0 |
| 4 (0 hidden) | 2, 6 | 0, 1, 2, 6 | 3 |
| 6 | — | — | 0 |
| 7 | 5 | 5 | 0 |
| above 9 | 3, 50 | — | 50 |
| above 9, hidden top | 3, 99 | 99 | 3 |
| 5 | any | `0..99` | rejected, `ALL_LAYERS_HIDDEN` (§6) |

### 3.9 3D renderer (`DQ-A2-9`)

`Drawable3D.getLayer()` returns `Drawable3D.renderCodingLayer(model layer)`,
`min(layer, MAX_LAYERS)`: only the renderer input is clamped; the model layer,
its XML, the API layer and the domain are never written. Layers 9..99 share the
last 3D draw-order code (residual presentation limitation, §12). Hidden layers
still do not apply in 3D (`OBS-A1-3D-VIEW`).

### 3.10 GGBScript capability matrix (`DQ-A2-8`)

`CmdShowHideLayer` reads the bound through
`kernel.getApplication().getConfig().getMaxLayer()` and contains no GeoCeDG
semantic reference, so the matrix rule places it in `processor_exclusions`
(`NO_GEOCEDG_SEMANTIC_REFERENCE`, like `CmdExecute`). The instance
`geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json` gained that one
exclusion; its static-contract input pin and the registry `static_contracts`
pin were recomputed with `Get-VerificationCanonicalTextSha256`. No row, form or
probe changed.

## 4. Re-established seams and corrections to C1–C12

Every citation of the characterization was re-established on the
implementation base before use; between `P_R6PLUS_D1` and the branch start only
documentation changed. Chosen names: `AppConfig.getMaxLayer()`,
`AppConfig.persistsDocumentHiddenLayers()`, `AppConfigGeoCeDG.L_MAX`, package
`org.geocedg.common.kernel.layers` (`LayerDomain`, `HiddenLayerSet`,
`HiddenLayersXml`, `HiddenLayerMetadataException`), the `App` hooks of §3.3,
`AppD.nativeDocumentLoadCommitted()`, `Drawable3D.renderCodingLayer(int)` and
`GeoCeDGLayerWorkspace.commitOpenedDocument(HiddenLayerSet)` (which replaces
`resetForOpenedDocument`). Corrections and precisions:

1. **C6, rejected-parse and paste/Insert File rollback rows.** The table
   predicted "present = current value, unchanged (reported only)". A restore
   runs with the `ROLLBACK_RESTORE` purpose; it validates the element but does
   not report it. Reason: `App.setXML` swallows the failure of its own parse, so
   a report from the restore would be indistinguishable from the report of a
   successful clearing `setXML` and would re-run the working-layer rule after a
   failure. The required outcome — set and working layer unchanged — holds
   (`aRejectedClearingSetXmlRestoresTheSetAndTheWorkingLayer`, shared
   `everyRecognizedDefectFailsClosedAndRestoresTheEntryDocument`). No scope,
   owner, serialization or class changes.
2. **C6, failed live load.** The host rollback of a failed native load re-reads
   its full-archive snapshot as a native parse and therefore reports the entry
   set; no commit follows, because `nativeDocumentLoadCommitted` is reached only
   after success and `runDocumentLoad` discards the report
   (`aLoadThatFailsAfterTheParseReportedNeverReachesTheCommitHook`,
   `everyRejectedOpenRestoresTheSetAndTheWorkingLayerExactly`).
3. **C6/C7, File → New.** `clearConstruction` resets the workspace (`A-1`);
   the preferences reload that `AppD.fileNew` performs afterwards is a clearing
   `setXML` and commits the empty set again: same result.
4. **C8.** Implemented as disposed; case 5 never reaches the workspace (the
   reader rejects it), and `commitOpenedDocument` keeps an explicit guard.
5. **C12, Locus V2.** The command surface yields one branch per `LocusV2`; a
   two-interval domain gives one branch with two valid components. The
   invariance corpus uses that locus and compares branch keys and component
   bounds (§11 item 3).

## 5. Route table

| Route (C6) | Result | Evidence |
|---|---|---|
| save, `App.getXML()` | element written when non-empty, after `</construction>` | `saveAndOpenRoundTripLayersAboveNineAndTheSet`, shared writer test |
| undo store; undo, redo | never written; set unchanged | `undoAndRedoNeverChangeTheSetBeforeOrAfterSaveAndReopen`, shared `undoAndRedoNeverReadOrRewindTheDocumentSet` |
| redefine rebuild | set unchanged | `undoAndRedoNeverChangeTheSetBeforeOrAfterSaveAndReopen` |
| rejected-parse restore | validated, not reported; set and working layer unchanged | §4 item 1 tests |
| paste, `evalXML`, Insert File, Apply Template | never imported; Insert File helper holds its own set | `onlyDocumentTransitionsCommitAndEveryOtherRouteLeavesTheSet`, `aPasteAcrossDocumentsNeverCarriesTheSourceSet`, shared `mergeMacroAndPasteContextsIgnoreTheElement` |
| File → Open `.cedg`/`.ggb`, archives with macros, reset reload | commit | `saveAndOpenRoundTripLayersAboveNineAndTheSet`, `anArchiveWithMacrosKeepsItsSet`, `onlyDocumentTransitions…` (reset) |
| failed live load after preflight | set and working layer unchanged | `everyRejectedOpenRestoresTheSetAndTheWorkingLayerExactly`, `aLoadThatFailsAfterTheParseReportedNeverReachesTheCommitHook` |
| `loadXML(String)` | commit | `onlyDocumentTransitions…` |
| startup with a file; failed startup | commit; blank with an empty set | `startupWithADocumentTakesItsSetAndAFailedStartupEndsBlank` |
| File → New | empty set, working layer 0 | `onlyDocumentTransitions…`, `A-1` `newStartsAtZeroAndClearsHiddenLayers` |
| clearing `App.setXML` (API) | commit; a legacy document commits the empty set | `onlyDocumentTransitions…` |
| non-clearing `setXML` (merge) | never read | `onlyDocumentTransitions…` |
| `loadXML(URL)`/`loadXML(File)` (API `openFile`) | commit through the native transaction | `onlyDocumentTransitions…` |
| Base64 archives, non-native URL documents | revision 1: no commit (a defect, §17); revision 2: commit through `App.documentReplacementCommitted` | §17 |
| macro part of an archive, `.ggt`, `addMacroXML` | ignored with a log | `onlyDocumentTransitions…` (tool file), shared `mergeMacroAndPasteContextsIgnoreTheElement` |
| Open preflight | validated in the GeoCeDG scratch app | `everyRejectedOpenRestoresTheSetAndTheWorkingLayerExactly` |
| a report outside a transition | discarded by the next transition | `onlyDocumentTransitions…` |

## 6. Fail-closed matrix

| Defect | Code | Evidence |
|---|---|---|
| `version="2"` | `UNSUPPORTED_VERSION` | shared and Desktop (`.cedg` and `.ggb`) |
| missing, non-integer or `0` version; an extra attribute; missing or empty list; `3,7`, `07`, `+3`, `-3`, leading, trailing or double space, `7 3`, `3 3`; a child element; text | `MALFORMED_ELEMENT` | shared (17 cases), Desktop (malformed list, text) |
| `100`, an overlong number | `OUT_OF_DOMAIN_LAYER` | shared, Desktop |
| every layer `0..99` | `ALL_LAYERS_HIDDEN` | shared, Desktop (Open, startup, clearing `setXML`) |
| a second element | `DUPLICATE_ELEMENT` | shared, Desktop |
| inside `<construction>`, `<euclidianView>` or `<gui>` | `MISPLACED_ELEMENT` | shared (three parents), Desktop (construction) |

On every Desktop rejection the previous document XML, hidden set, working
layer, current file, recent list, saved state, undo history and both archive
files are unchanged, and the message names the defect in the current language;
for example "No se puede abrir defecto.cedg: sus metadatos de capas ocultas
ocultan todas las capas de 0 a 99." A Classic configuration reads none of them
(§3.2). Unit and hidden-layer defects keep their own diagnostics
(`unitsAndTheSetRoundTripAndUndoIndependently`).

## 7. Older-reader evidence (`T-OLDER-READER`)

Scratch-probe evidence, not a committed test. A candidate document was written
by the candidate build's full-document writer (`MyXMLio.getFullXML`, the writer
of every `.cedg` save; points on layers 0, 5, 9, 10, 50, 99 and the set
`10 50`; SHA-256 `4e8fa862e26b527ead8c232f4afab0634833f5d33f90273cb97d91387fe61ddc`)
and read by a build of a `git archive` of `P_R6PLUS_D1` (`2941ddf2`) with the
GeoCeDG and the Classic configuration (result file SHA-256
`9f39c5125dcd3f39365c73eccf3da372cc29f671a100b68a70df95f018abb761`):

| Older reader | Loaded layers | `maxLayerUsed` | Re-saved layers | Element re-saved | Every object visible |
|---|---|---|---|---|---|
| base GeoCeDG configuration | 0, 5, 9, 9, 9, 9 | 9 | 0, 5, 9, 9, 9, 9 | no | yes |
| base Classic configuration | 0, 5, 9, 9, 9, 9 | 9 | 0, 5, 9, 9, 9, 9 | no | yes |

This is the accepted `AQ-L1c` loss, documented in both user guides; `A-2` adds
no migration, warning or workaround.

## 8. Compatibility corpus and legacy bytes (`C11`)

| Corpus item | Evidence |
|---|---|
| legacy `.cedg` with layers `0..9`; Classic `.ggb` | the committed `D1` base fixture (captured by `D1` on a `git archive` of its base; every tracked `.ggb`/`.cedg`/`.ggt` in both configurations) is reproduced unchanged by the candidate (`PreG9BR6PlusD1LegacyByteIdentityTest`); `A-2` adds no fixture, and documents with an empty set write no element |
| objects on layers 10, 50 and 99 | shared routes test; `saveAndOpenRoundTripLayersAboveNineAndTheSet` |
| values below 0 and above 99 (XML, `SetLayer`, API, Properties) | shared `geocedgKeepsLayersTenFiftyAndNinetyNineOnEveryRoute`, `layerPickersExposeTheConfiguredDomain` (the Properties combo cannot offer them) |
| a persistent hidden-layer document; all used layers hidden; all layers hidden (invalid) | `openTakesTheDocumentSetAndChoosesAShownWorkingLayer`, §6 |
| a document with Graphics 2 | `effectiveVisibilityIsTheSameBeforeSavingAndAfterReopening` |
| macros and a `.ggt` | `anArchiveWithMacrosKeepsItsSet`, the tool file of `onlyDocumentTransitions…` |
| clipboard and paste across documents with different sets | `aPasteAcrossDocumentsNeverCarriesTheSourceSet`, Insert File in `onlyDocumentTransitions…` |
| an undo snapshot; redefine and rollback routes | §5 |
| a unit-bearing `D1` document with hidden layers | `unitsAndTheSetRoundTripAndUndoIndependently`, shared `unitStateAndHiddenLayersRoundTripIndependently` |
| older readers | §7 |

## 9. Effective visibility after reopen (`C9`)

`effectiveVisibilityIsTheSameBeforeSavingAndAfterReopening` saves a document
with a red segment on hidden layer 3 and a blue one on layer 0, both visible in
Graphics 1 and Graphics 2, reopens it in a fresh application and compares 20
facts: Graphics 1 and 2 painting, hit testing at both segments, the Algebra View
group text (`Layer 3 (hidden)`), the PNG export, printing, the SVG layer groups,
the Save preview of PNG, PDF, SVG and EMF, the three LaTeX dialects and DXF.
All are equal; the hidden layer is omitted everywhere `B` and `A-1` omit it, and
LaTeX and DXF are unchanged (they belong to `C`). Showing the layer after the
reopen paints it in both views again. On this evidence
`OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` is closed under persistent reopen.
No exporter changed.

## 10. Obligation → test map

New classes: shared `org.geocedg.common.layers.PreG9BR6PlusA2LayerDomainTest`
(8 tests) and `PreG9BR6PlusA2HiddenLayerXmlTest` (10); Desktop
`org.geocedg.desktop.PreG9BR6PlusA2LayerDomainDesktopTest` (7) and
`PreG9BR6PlusA2HiddenLayerPersistenceTest` (15). Updated: `A-1`
`PreG9BR6PlusA1HiddenLayerTest` and `PreG9BR6PlusA1LayerWorkspaceTest` (§11
item 4).

| Author regression obligation | Test |
|---|---|
| 1. a toggle changes the saved state but adds no undo point | `aToggleMarksTheDocumentUnsavedButNeverStoresAnUndoPoint` (2-second latch, history size, `undoPossible`); `A-1` `togglingIsPersistentButNeverAnUndoPointAndSurvivesUndoAndRedo` |
| 2. an empty document with a non-empty set is save-relevant | `aNonEmptySetIsSaveRelevantEvenInAGeometricallyEmptyDocument` |
| 3. undo and redo never change the set | `undoAndRedoNeverChangeTheSetBeforeOrAfterSaveAndReopen`; shared `undoAndRedoNeverReadOrRewindTheDocumentSet`; `geometryIsInvariantUnderEveryLayerRoute` |
| 4. a failed Open restores exactly | `everyRejectedOpenRestoresTheSetAndTheWorkingLayerExactly`, `aLoadThatFailsAfterTheParseReportedNeverReachesTheCommitHook`, `aRejectedClearingSetXmlRestoresTheSetAndTheWorkingLayer` |
| 5. Open never unhides | `openTakesTheDocumentSetAndChoosesAShownWorkingLayer` |
| 6. an all-domain set fails closed | the `ALL_LAYERS_HIDDEN` rows of §6 |
| 7. Classic stays `0..9` | shared `classicStaysClampedToZeroToNineOnEveryRoute`, `layerScriptingOutsideTheDomain…`, `theDefaultLayerRuleIsACap…`, `layerPickersExposeTheConfiguredDomain`, `listAndPolyhedronPropagation…`, `anUnrecognizingConfigurationIgnoresAndDropsTheElement`; Desktop `theClassicDiagnosticConfigurationClampsAndDropsTheElement` |
| 8. GeoCeDG persists 10, 50 and 99 | shared `geocedgKeepsLayersTenFiftyAndNinetyNineOnEveryRoute`; `saveAndOpenRoundTripLayersAboveNineAndTheSet` |
| 9. the `L_MAX − 1` rule is a cap | shared `theDefaultLayerRuleIsACapOnTheHighestUsedLayerNeverADefault` (0 → 0, 50 → 50, 99 → 98; Classic 9 → 8) |
| 10. the 3D adapter never mutates the model | shared `theRendererAdapterClampsOnlyItsInputNeverTheModelLayer` |
| 11. units and the set round-trip independently | `unitsAndTheSetRoundTripAndUndoIndependently`; shared `unitStateAndHiddenLayersRoundTripIndependently` |
| 12. `B` preview and export stay consistent after save and reopen | `effectiveVisibilityIsTheSameBeforeSavingAndAfterReopening` |

| Prompt test ID | Test |
|---|---|
| `T-DOMAIN-CONFIG` | shared `theDomainIsTheConfiguredProductBound`; Desktop `theProductBoundIsTheOnlyNinetyNineLiteralOfTheLayerSites` |
| `T-DOMAIN-ROUTES` | shared `geocedgKeepsLayersTenFiftyAndNinetyNineOnEveryRoute`, `layerPickersExposeTheConfiguredDomain` (Properties route); Desktop `pasteKeepsACopiedLayerAboveNine` |
| `T-DOMAIN-CONSUMERS` | shared cap, scripting, pickers and propagation tests; Desktop `macroAndLocusV2OutputsTakeAWorkingLayerAboveNine`, `hitTestingAndTheDrawOrderFollowLayersAboveNine`, `theAlgebraViewOrdersAndHidesGroupsAboveNine` |
| `T-CLASSIC` | obligation 7 |
| `T-WORKSPACE-DOMAIN` | `theChooserAndTheStatusBarOfferTheConfiguredDomain`, `theAlgebraViewOrdersAndHidesGroupsAboveNine` |
| `T-3D-CODING` | obligation 10 |
| `T-XML-WRITER` | shared `theWriterEmitsOneCanonicalLineAfterTheConstructionOnlyWhenNonEmpty` (four default locales), `undoPreferencesMacroAndClipboardXmlNeverCarryTheElement` |
| `T-XML-READER` | shared `aDocumentParseReportsItsSetOnlyAfterTheParseCompleted`; Desktop round trips |
| `T-FAIL-CLOSED` | §6 |
| `T-PERSIST-NO-UNDO` | obligations 1 and 3; §4 item 1 |
| `T-ROUTES`, `T-NEW-OPEN` | §5 |
| `T-OPEN-WORKING-LAYER` | `openTakesTheDocumentSetAndChoosesAShownWorkingLayer` (ten documents over the seven cases, layers above 9 included) |
| `T-SAVED` | obligations 1 and 2 |
| `T-EFFECTIVE-REOPEN` | §9 |
| `T-INVARIANCE` | `geometryIsInvariantUnderEveryLayerRoute` |
| `T-UNITS-INDEPENDENCE` | obligation 11 |
| `T-LEGACY-BYTES`, `T-COMPAT-CORPUS` | §8 |
| `T-OLDER-READER` | §7 (scratch probe) |
| `T-GGBSCRIPT` | §3.10; `PreG9BR6CapabilityMatrixTest` and `ggbscript-matrix.product` |
| `T-SMOKE` | §13 |

## 11. Deviations and interpretations

1. **Rollback restores do not report** (§4 item 1): an implementation precision
   of the C6 mechanism that keeps the required outcome; reported for author
   review.
2. **Matrix reconciliation.** `CmdShowHideLayer` becomes a registered modified
   processor without a GeoCeDG semantic reference; per the matrix rule it is a
   `processor_exclusions` entry (`DQ-A2-8` foresaw a reconciliation through
   the official mechanism). Its pins were recomputed, never typed.
3. **"A Locus V2 with several branches"** (C12) is realized as a Locus V2 whose
   branch has two valid components, because the command surface creates one
   branch per locus; both branch and component identity are compared.
4. **One `A-1` test method renamed**: `togglingIsSessionOnlyAndSurvivesUndoAndRedo`
   → `togglingIsPersistentButNeverAnUndoPointAndSurvivesUndoAndRedo`, because
   its contract changed by `DQ-A2-2`. The registered selections that contain it
   (`pre-g9b-r6-plus-a1.desktop`, `pre-g9b-r6-plus-b.desktop`) were refreshed
   through the official updater with executed evidence (§14); their counts are
   unchanged. Other `A-1` edits: the chooser now offers `0..99` and
   `setWorkingLayer(100)` throws, the cap expression reads the configuration,
   paste iterates the configured domain.
5. **Diagnostic route.** Hidden-layer rejections reuse the `D1` message route
   (`showUnitLoadError`, test sink `setUnitLoadErrorSink`), whose names predate
   `A-2`; the texts are the `Workspace.Layer.LoadError.*` keys.
6. **`T-OLDER-READER`** used a document written by the shared full-document
   writer of the candidate (the writer of every `.cedg` save) rather than a
   Desktop-saved archive; the parse is the same.

## 12. Observations and residual risks

1. **3D draw-order range (`DQ-A2-9`).** Layers 9..99 share the last 3D
   draw-order code; the model layer is unchanged. Hidden layers still do not
   apply in 3D (`OBS-A1-3D-VIEW`). Owner: author.
2. **Non-transactional document routes (`OBS-A1-URL-OPEN` remainder).**
   Revision 1 recorded that Base64 archives and non-native URL documents commit
   no set. The focal characterization showed that they are full-document
   replacements, so this was a `DQ-A2-6` defect, not a residual; revision 2
   corrects it (§17). The session-state part of these routes is the separate
   `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (§17.6).
3. **An opened empty document is "started".** The host counts an opened
   document as started (`Construction.isStarted()`), so clearing the set of an
   opened set-only document prompts; a never-saved blank document whose set
   returns to empty does not prompt (its state equals the blank document).
4. **Locus V2 value string.** It carries the semantic revision counter, which a
   reopen advances (1 → 2); this is session state, not geometry, and not caused
   by `A-2`; C12 compares the semantic content instead.
5. **`OBS-A1-NON-HIT-SELECTION`**: Select All and keyboard selection still reach
   objects on hidden layers, now more often because sets persist. Unchanged.
6. **`OBS-D1-ROLLBACK-CONSPROT-GUI`**: a failed live Open can still leave
   additive GUI-section differences; the construction, the set and the working
   layer are restored exactly. Unchanged; not `A-2` scope.
7. **Apply Template re-layering** stays an open `F3` question; Insert File and
   Apply Template keep their current behavior with the widened domain.
8. **Pre-existing Checkstyle warning** `PreG9BR6PlusA1HiddenLayerTest.java:188`
   (line length) is unchanged.

## 13. Author-smoke checklist (not performed or attributed by the agent)

1. **Layers 0, 10, 50, 99.** Choose each as working layer (Working Layer tool
   and **Layer: n**), create an object on it, select it; Properties → Advanced
   → Layer offers 0 to 99 and shows its layer; the status bar shows the layer.
2. **Chooser bounds.** The chooser lists exactly 0 to 99; its message says
   "0 to 99" / "de 0 a 99"; Cancel changes nothing.
3. **Classic stays 0..9.** In the Classic diagnostic session the layer choices
   are 0 to 9; opening a GeoCeDG document there shows layers above 9 as 9 and
   every layer.
4. **Hide, save, close, reopen.** Hide two layers in the Algebra View (Sort by
   Layer), save, close, reopen: the same layers are hidden (eye and Graphics
   view).
5. **Working layer after Open, hidden highest layer.** Objects on 2 and 6, hide
   6 (working layer elsewhere), save, reopen: the working layer is 2 and 6
   stays hidden.
6. **All used layers hidden.** Objects on 2 and 6, both hidden, save, reopen:
   the working layer is the lowest layer that is not hidden (0) and both stay
   hidden.
7. **Rejected metadata.** Open author-made copies with malformed hidden-layer
   metadata and with every layer 0..99 hidden: each is refused with its
   message and the current document stays as it was.
8. **Not an undo step.** Hide or show a layer, then Undo and Redo: the hidden
   layers never change; Undo affects only construction changes.
9. **Unsaved document.** Hide a layer in a saved document: it is marked
   modified and closing asks to save; the same in an empty new document.
10. **Legacy documents.** Open an existing document without hidden layers:
    every layer is shown; re-saving it adds nothing.
11. **`B` preview and export after reopen.** With a hidden layer reopened,
    File → Export picture preview and the exported PNG/PDF/SVG/EMF omit it;
    LaTeX and DXF still write it (until `C`).
12. **`D1` units with hidden layers.** Set document units and hide a layer,
    save, reopen: both persist; undoing a unit change leaves the hidden layers
    as they are.

## 14. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance. The single `FINAL` on the frozen candidate is reported with the
candidate, outside this file.

| Run | Result |
|---|---|
| focal shared `org.geocedg.common.layers.*` and `PreG9BR6PlusA1LayerSeamTest` | 3 classes, 25 tests, 0 failures/errors |
| focal Desktop `PreG9BR6PlusA2*` and `PreG9BR6PlusA1*` | 4 classes, 56 tests, 0 failures/errors (after the test-setup fixes of the first runs: a working-layer refusal, per-view flags, a merge without identity data, the Locus V2 component count) |
| adjacent shared (`org.geocedg.common.*`, `CommandsTest*`, `org.geogebra.common.io.*`, `…main.*`, `…properties.*`, option models) | 2 932 tests, 0 failures/errors |
| adjacent Desktop (the `PreG9BR6*` family with `A-1`, `B`, `D1` and the capability matrix, `G9U0R5*`, `G9S1*`, profile, guide, clipboard and script suites) | 39 classes, 364 tests, 0 failures/errors |
| Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest`, XML reports) | no finding in any `A-2` file after the fixes (two declaration distances, one line length); the remaining warning `PreG9BR6PlusA1HiddenLayerTest.java:188` is pre-existing |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522` | OK, 932 registered files |
| `Assert-GeoCeDGLiveWorkspaceProfile` | OK (no action change) |
| guide structure (`tools/agent/diagnostics/guide-structure-diagnostic.ps1`) | `DIAGNOSTIC_CLEAR` |
| GGBScript matrix schema checker (`tools/agent/checks/ggbscript-capability-matrix.ps1`) | `CONTRACT_SATISFIED` |
| producer `discovery.shared` (`--test-dry-run`) | completed, 5 994 identities |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 789 identities |
| producer `final.shared` (executed, complete shared suite) | 6 911 tests, 0 failures/errors, 10 skips |
| producer `final.desktop` (executed, complete Desktop suite) | 1 783 tests, 0 failures/errors, 1 skip |
| producer `pre-g9b-r6-plus-a1.desktop` (executed) | 173 tests, 0 failures/errors |
| producer `pre-g9b-r6-plus-b.desktop` (executed) | 241 tests, 0 failures/errors |
| scratch base probe (`git archive 2941ddf2`) | older reader (§7) |
| `git diff --check` (tracked and new files) | clean |
| development `STATIC` and `INFRA_UNIT` on the staged, uncommitted candidate tree | `ACCEPTED / COMPLETE` (`verification-aef38a77a49f415da6c428817a02ee0c`, 3/3; `verification-26867e0d66964247a4e5bc238dd33598`, 22/22); the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; documentation, guide-structure, style and whitespace diagnostics clear; not acceptance evidence |

The skips are the pre-existing allowlisted ones. The executed `final.shared` and
`final.desktop` runs include every adjacent class the prompt lists
(`GeoCeDGDocumentLifecycleTest`, `G9U1MacroNativeArchivePersistenceTest`,
`G9U1UserToolLibraryTest`, the clipboard suites, `RedefineTest`,
`TemplateHelperTest`, `G9A1SpatialIdentityXmlTest`,
`G9A3SpatialSnapshotRecoveryTest`,
`PostG9U1A4ConstructionPositionCharacterizationTest`, `CommandsTest`,
`GeometryExportFoundationTest`, `G9X1G5CorpusCompatibilityTest`,
`G9U0PersistenceCompatibilityTest` and the rest).

**JUnit inventory and pins.** The current pins were reproduced first
(`junit_inventory` `3895083f…`, `static_contracts` `567c0958…`, matrix input
`24344e93…`). Discovery evidence came from the two `--test-dry-run` producer
runs and executed evidence from the four passing executed runs above;
`tools/agent/update-verification-junit-inventory.ps1`, run in-session with
absolute paths, wrote every count and hash: shared discovery 5 976 → 5 994
(`7e215c6e…`), Desktop discovery 1 767 → 1 789 (`f4fa9c91…`), `final.shared`
6 893 → 6 911 (`5ad08d36…`), `final.desktop` 1 761 → 1 783 (`3ae4ac1c…`),
`pre-g9b-r6-plus-a1.desktop` 173 → 173 (`0100420f…`, the renamed `A-1`
method) and `pre-g9b-r6-plus-b.desktop` 241 → 241 (`56ea89f4…`). Every other
selection is unchanged; no selection was added. The matrix input was re-pinned
to `91906cd7…` with `Get-VerificationCanonicalTextSha256`, then the registry
catalog pins: `junit_inventory`
`df9e65e131dad3959493d4af4b9ac193804abe01be450692bd7e79d6a3408761` and
`static_contracts` `e51b2f18a5870e6d100eec93e5bc44a2d399bce4e1a0f3f88d05a43a76d7237d`.
No hash was typed by hand. No registry-shape pin changed.

## 15. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes.
VERIFICATION_INFRASTRUCTURE_IMPACT = INVENTORY_AND_MATRIX_PIN
Rationale: the JUnit inventory (discovery, final and the two refreshed phase
selections) and its registry pin are refreshed through the official updater
with executed evidence; the GGBScript capability-matrix instance gains one
processor exclusion through the matrix rule, with its static-contract input pin
and the static_contracts registry pin; no new selection, PHASE registration,
verifier logic, schema, registry shape or profile-composition change.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md;
  geocedg/specs/ui/cedg-workspaces.md
SERIALIZATION CHANGE = YES: GeoCeDG <layer val> admissible 0..99;
  <geocedgHiddenLayers version="1" layers="..."/> after </construction>
PRODUCT CHANGE = YES
```

## 16. Changed paths

- Shared product, new: `source/shared/common/src/main/java/org/geocedg/common/kernel/layers/`
  (`LayerDomain`, `HiddenLayerSet`, `HiddenLayersXml`,
  `HiddenLayerMetadataException`).
- Shared configuration: `AppConfig` (two default methods),
  `AppConfigGeoCeDG` (`L_MAX`, two overrides).
- Shared upstream, modified (bound, serialization and save seams):
  `GeoElement`, `App`, `ConstructionDefaults`, `CmdShowHideLayer`, `GgbAPI`,
  `LayerModel`, `LayerProperty`, `Drawable3D`, `MyXMLHandler`, `MyXMLio` (all
  under `source/shared/common/src/main/java/org/geogebra/common/`).
- Desktop upstream, modified: `source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java`
  (one host-identical hook).
- Desktop GeoCeDG, modified: `AppGeoCeDG`, `GeoCeDGLayerWorkspace`,
  `GeoCeDGWorkingLayerChooser` (in `source/desktop/desktop/src/main/java/org/geocedg/desktop/`).
- Profile: `apps/geocedg/application-profile.yml` (the chooser message carries
  the bound; six localized `Workspace.Layer.LoadError.*` texts).
- Tests, new: the two shared classes in
  `source/shared/common-jre/src/test/java/org/geocedg/common/layers/`; the two
  Desktop classes in `source/desktop/desktop/src/test/java/org/geocedg/desktop/`.
- Tests, updated: `PreG9BR6PlusA1HiddenLayerTest`,
  `PreG9BR6PlusA1LayerWorkspaceTest` (§11 item 4).
- Upstream boundary: `docs/upstream/modified-files.yml`.
- Verification data: `geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json`,
  `geocedg/specs/operations/verification-static-contracts.json`,
  `geocedg/specs/operations/verification-junit-inventory.json`,
  `geocedg/specs/operations/verification-registry.json`.
- Documentation: `docs/user/geocedg_user_guide_en.md`,
  `docs/user/geocedg_user_guide_es.md`, `docs/developer/geocedg_developer_guide.md`,
  `geocedg/specs/ui/cedg-workspaces.md`, `docs/roadmap/geocedg_roadmap.md`,
  `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, this report and
  `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-a2-candidate-evidence.json`.

Not changed: `AGENTS.md`, `CLAUDE.md`, `.github/prompts/**` after the
authorization edit, `ai-shell/prompts/**`, the verifier (`tools/agent/**`), its
schemas and `prompt-contracts.json`, `EuclidianStyleConstants.MAX_LAYERS`,
`Construction`, the `A-1` hidden-layer predicate, `ShowLayer`/`HideLayer`
semantics, every exporter and export specification, the unit system, the
Insert File and Apply Template surfaces, and `artifacts/author-input/**`.

## 17. Revision 2: commit on non-native document replacement

### 17.1 Trigger and authorization

A focal, read-only characterization of the frozen candidate `e3cbdc60` showed
that the residual observation "Base64 and non-native URL loads do not commit
the persistent hidden-layer set" was a `DQ-A2-6` defect, not a correct route
classification (`A2_FOCAL_REMEDIATION_REQUIRED`). On 2026-10-04 the author
accepted that conclusion and authorized a focal correction limited to this
defect. The previous candidate and its `FINAL` stay historical evidence for
that commit only (header).

### 17.2 Characterization

Scratch probe on a `git archive` of `e3cbdc60`. Live document B: hidden `{5}`,
working layer 6, units cm, view scale 25, saved to a file. Source A: layers
3, 7 and 4, hidden `{3, 7}`, units mm, view scale 40; legacy A0 without hidden
layers.

| Route | Revision 1 result |
|---|---|
| `GgbAPI.setBase64` (API; File → Open URL with a Base64 text), A and A0 | construction, `D1` units (mm), view settings, undo baseline, saved state and current file replaced; set `[5]` and working layer 6 kept; the next save wrote `{5}` into A |
| `loadBase64File` / `loadFromHtml` with `ggbBase64` (opening or dragging an `.html` page) | the same |
| `AppD.loadXML(File)` of a non-native extension; remote URL with a non-native path (API `openFile`) | the same |
| native file and remote `.cedg`/`.ggb` (API, Open URL), `loadXML(String)`, clearing `setXML`, every startup route | correct: the document's set, the `DQ-A2-1` working layer |
| `evalXML`, non-clearing merge | correct: set unchanged |
| failures of the defective routes | construction, set, working layer and units restored |

The `D1` units transitioned on the defective routes while the hidden set did
not: an `A-2` lifecycle omission.

### 17.3 Root cause

The two host success endings that perform these replacements had no commit
context: `App.loadXML(ZipFile)` (Base64 archives) and
`GFileHandler.loadXML(App, InputStream, boolean)` (non-native file and URL
streams). Their parse already reported the document's set; the report stayed
uncommitted and the next transition discarded it.

### 17.4 Correction and hook semantics

- `App.documentReplacementCommitted()`: public, a no-op in the host. Called as
  the last step of a successful `App.loadXML(ZipFile)` (after the new undo
  baseline, the saved state, the current-file reset and the command
  dictionary) and of a successful non-macro `GFileHandler.loadXML`. Failure
  paths (the `catch` of `App.loadXML(ZipFile)`, the `MyError` branch of
  `GFileHandler`, every exception propagating from the parse) never reach it;
  macro and `.ggt` loads never call it; the native transaction parses through
  `GFileHandler.loadPreflightedNativeXML`, which has no such ending, and keeps
  `AppD.nativeDocumentLoadCommitted`; the Open preflight's scratch `AppDNoGui`
  inherits the no-op.
- `AppGeoCeDG.documentReplacementCommitted()`:
  `if (layerWorkspaceActive) commitLoadedDocument();`, the same commit as every
  other transition. During startup the workspace is not active yet, so
  `initializeLayerWorkspace` stays the startup commit; inside `runDocumentLoad`
  (File → Open of a non-native extension) the commit marks the transition
  committed, so `runDocumentLoad` does not commit again.
- Unchanged: the parser, the grammar, undo, the native Open transaction,
  Insert File, Apply Template, `evalXML` and merges, macros and `.ggt`, `D1`.

### 17.5 Regression tests

Three tests in `PreG9BR6PlusA2HiddenLayerPersistenceTest`, each starting from a
live B (`{5}`, working layer 6, cm); a workspace listener counts commits:

| Test | Route | Result |
|---|---|---|
| `nonNativeDocumentReplacementsCommitTheDocumentSetWithItsUnitsOnce` | `setBase64(A)` | `[3, 7]`, working layer 4 |
| | `setBase64(A0)`, legacy | `[]`, working layer 7 |
| | `loadBase64File(html)` | `[3, 7]`, 4 |
| | API `openFile(file:` non-native`)` | `[3, 7]`, 4 |
| | `loadXML(File)` non-native | `[3, 7]`, 4 |
| | `loadFile` non-native (`runDocumentLoad`) | `[3, 7]`, 4 |
| | every route above | A's objects and mm units (the `D1` cross-check), saved, exactly one commit, the next save writes A's set and never B's |
| `aRejectedNonNativeReplacementLeavesTheSetTheWorkingLayerAndTheUnits` | all-domain Base64; all-domain non-native file | load fails; `[5]`, 6, cm and B's construction unchanged; no commit |
| `mergesStayNegativeAndNativeAndStartupLoadsCommitExactlyOnce` | `evalXML`, non-clearing `setXML` | `[5]`, 6; no commit |
| | native `loadFile` and API `openFile` of A | `[3, 7]`, 4; exactly one commit |
| | startup `base64://` A and A0 | `[3, 7]`, 4, mm; `[]`, 7 |

On a scratch copy of `e3cbdc60` the revised class fails exactly at
`setBase64(A)` ("expected: <[3, 7]> but was: <[5]>"); the other two new tests
pass there, as the characterization predicted.

### 17.6 New observation, recorded and not fixed

`OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET`: `AppGeoCeDG` resets
the `B` export-area session (`ExportAreaSession.resetForDocument`) and clears
the `D1` paste notice only in `clearConstruction`, `loadExistingFile` and
`loadXML(String)`. The Base64 and non-native replacement routes keep the
previous document's manual export area, explicit producer, overlay and any
visible paste notice; by the same code, so do the routes that bypass
`loadExistingFile` (API `openFile` of native files and URLs, the clearing
`setXML`). This is outside the `A-2` correction and needs its own disposition
before the global `PRE-G9B-R6-plus` closeout. Owner: author.

### 17.7 Revised author-smoke checklist

The checklist of §13 stays, with three additions:

13. **Base64 document.** In a document with hidden layers, Ctrl+Shift+B copies
    it as Base64; File → New with other hidden layers, then File → Open URL…
    and paste the text: the copied document's hidden layers and working layer
    come back, not the previous ones; save, reopen: they persist.
14. **HTML page with an embedded document.** Open (or drag) an `.html` page
    with a `ggbBase64` document that has hidden layers: they are restored; with
    a page whose document has none, every layer is shown.
15. **No carry-over.** Steps 13 and 14 started from a document with other
    hidden layers never keep those layers, and the next save never writes them.

### 17.8 Pre-freeze evidence of revision 2

Development and inventory evidence, not acceptance; the single `FINAL` on the
revised candidate is reported outside this file.

| Run | Result |
|---|---|
| focal Desktop `PreG9BR6PlusA2*` and `PreG9BR6PlusA1*` | 4 classes, 59 tests (persistence class 18), 0 failures/errors |
| revised persistence class on a scratch copy of `e3cbdc60` | 18 tests, 1 failure at `setBase64(A)`, as expected |
| Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest`) | no finding in a revision file; the pre-existing `PreG9BR6PlusA1HiddenLayerTest.java:188` only |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522` | OK, 932 registered files (`App`, `GFileHandler`, `AppGeoCeDG` and the test were already registered; their purposes gained the revision text) |
| producers `discovery.shared` / `discovery.desktop` (`--test-dry-run`) | 5 994 (unchanged) / 1 792 identities |
| producer `final.shared` (executed) | 6 911 tests, 0 failures/errors, 10 skips (identities unchanged) |
| producer `final.desktop` (executed) | 1 786 tests, 0 failures/errors, 1 skip |
| updater (in-session, absolute paths) | `discovery.desktop` 1 789 → 1 792 (`8bff1f1e…`), `final.desktop` 1 783 → 1 786 (`2b2fbe7d…`); shared selections unchanged; `junit_inventory` pin `df9e65e1…` → `d06c3ea62a4b1d792350007f1d2ee3dbef64c257df2efd1f8449bd5f29b25b7b`, reproduced first; `static_contracts` unchanged |
| `git diff --check` | clean; roadmap 2 912 and manifest 145 CR bytes preserved |
| development `STATIC` and `INFRA_UNIT` on the staged revision | `ACCEPTED / COMPLETE` (`verification-53b2e17954254934a8cd103c2e7908b7`, 3/3; `verification-7feb870d5f66490e955ff9ebcf48bc92`, 22/22); the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE` only |

### 17.9 Impact and changed paths of revision 2

```text
PRODUCT CHANGE = YES (one host-identical App hook, its call in
  GFileHandler, one AppGeoCeDG override)
SERIALIZATION CHANGE relative to revision 1 = NONE
GUIDE_IMPACT = UPDATED (developer guide only)
BOOTSTRAP IMPACT = NO CHANGE REQUIRED
VERIFICATION_INFRASTRUCTURE_IMPACT = INVENTORY_PIN (discovery.desktop and
  final.desktop through the official updater; no selection, registry-shape or
  static-contract change)
```

Changed paths: `source/shared/common/src/main/java/org/geogebra/common/main/App.java`,
`source/desktop/desktop/src/main/java/org/geogebra/desktop/headless/GFileHandler.java`,
`source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java`,
`source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusA2HiddenLayerPersistenceTest.java`,
`docs/upstream/modified-files.yml`, `docs/developer/geocedg_developer_guide.md`,
`geocedg/specs/operations/verification-junit-inventory.json`,
`geocedg/specs/operations/verification-registry.json`,
`docs/roadmap/geocedg_roadmap.md`,
`docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, this report and its
evidence mirror. Not changed: F3, C, the rollback Construction Protocol debt,
other session-reset behavior, every other file of revision 1.

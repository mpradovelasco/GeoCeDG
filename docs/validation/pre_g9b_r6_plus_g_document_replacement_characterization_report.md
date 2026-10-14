# PRE-G9B-R6-plus-G — document-replacement characterization report (SC-01 to SC-27)

```text
REPORT_KIND                   = RUNTIME CHARACTERIZATION (scratch probe; no product change)
AUTHORIZATION                 = G implementation authorization, area D (2026-10-14)
OBSERVATIONS                  = OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET (B-1)
                                OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT (B-2)
PROBE BASE                    = 6e09eee99 (authorization record on ceb5a7e9; product identical to ceb5a7e9)
SCENARIOS EXECUTED            = SC-01 to SC-27 (27/27; three with a documented in-process emulation, §2.3)
REPRODUCIBILITY               = two complete runs, 57 rows each, identical except Java identity hashes
CONTRACT VIOLATIONS SHOWN     = YES — SC-01, SC-02 (ExportArea) and SC-11 (paste notice) on approved Open routes
PRODUCT CORRECTION            = NOT AUTHORIZED — NOT MADE — CORRECTIVE BRANCH STOPPED FOR AUTHOR DECISION
B-1 DISPOSITION               = PENDING AUTHOR DECISION (mandatory before the global closeout)
B-2 DISPOSITION               = PENDING AUTHOR DECISION (mandatory before the global closeout)
selfApproved                  = false
```

This report executes the 27 scenarios of the
[G author decision reconciliation](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md)
§5.3, as authorized by the
[G implementation authorization record](pre_g9b_r6_plus_g_implementation_authorization_record.md)
(area D). It records observed state transitions, the expected invariants, mismatches,
route identities and reproducibility. It changes no product code and adds no tracked
test. It decides nothing: both observations keep their mandatory author disposition.
Machine-readable mirror:
[`pre-g9b-r6-plus-g-document-replacement-characterization.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-document-replacement-characterization.json).

## 1. Distinctions kept

The report keeps the four notions of the reconciliation (§5.1) apart:

| Notion | Routes in this report |
|---|---|
| complete native document replacement (Open) | R01–R04, R09, R11; controls R12 (New), R20 (blank reset) |
| complete non-native replacement (Base64 is transport, not a format) | R05–R08, R10, R17, R18 |
| insertion or merging into the current document | R15 Insert File, R16 Apply Styles |
| tool replacement (same document, rebuilt by a clearing reload) | R13, R14, the macro-edit overwrite |
| other session transitions | R19 embedded close, startup and the New Window constructor (M6) |

## 2. Method

### 2.1 Vehicle

- A scratch JUnit 5 class in package `org.geocedg.desktop`, compiled into the existing
  Desktop test source set through a Gradle init script from the session scratchpad
  (`gradlew :desktop:desktop:test --tests …GReplacementCharacterizationProbe
  --init-script …`). It is **never committed**; its build residue was removed after the
  runs.
- Embedded hosts (`G9U1TestApp`, `PreG9BR6PlusF3Fixtures.app`), the test lifecycle owner
  and in-memory stores for unit and orientation-cue preferences, so no real preference
  is read or written.
- Each route runs on a **fresh primed window**, so no route inherits another's state.

### 2.2 Primed window (before every route)

| State | Primed value |
|---|---|
| document | construction unit `mm`; `A=(1,1)`, `B=(4,3)`, `lw=Line(A,B)`, `K=(2,6)` on layer 6; saved; one-state undo baseline |
| working layer | `2` (below the highest used layer `6`) |
| `ExportArea` | group M1: `MANUAL` `[-2,5]×[-1,4]` on Graphics 1 with the overlay shown; group M2: an `IsoABorder` A4 sheet linked as `ISO_A_BORDER` |
| paste notice | visible, text `PROBE PASTE NOTICE` |
| drawing scale | `1:2` |
| reference memory | Parallel slot holds `lw` |
| orientation cue | shown (in-memory preference store) |

The replacement document `docB` is a saved `mm` document with `Z=(7,7)` on layer 0 and
`Y=(8,1)` on layer 5, layer 5 hidden. It is reached as `docB.cedg`, as its raw bytes
under the non-native name `docB.bin`, as Base64, as an `.html` page with a `ggbBase64`
parameter, through a `file:` URI and through a stream `jar:file:…!/docB.cedg` URL. The
insertion source `docC` is a saved document (unit `m`) with `W=(3,3)`.

### 2.3 Routes and their identity

| Id | Route | Product entry exercised |
|---|---|---|
| R01 | File → Open `.html` | `GuiManagerGeoCeDG.openResolved` → inherited `doOpenFiles` → `loadBase64File` |
| R02 | API `openFile`, native `file:` URI | `GgbAPID.openFileUnsafe` → `AppD.loadXML(URL)` → `loadXML(File)` |
| R03 | URL drop, `file:` URI `.cedg` | `GuiManagerD.loadURL` (the `FileDropTargetListener` URL-list call) → API `openFile` |
| R04 | URL drop, stream URL `.cedg` | `GuiManagerD.loadURL` → API `openFile` → `AppD.loadXML(URL)` stream → native archive transaction |
| R05 | API `setBase64` | `GgbAPIJre.setBase64` → `loadXML(ZipFile)` |
| R06 | API `openFile`, non-native `file:` URI | as R02 with `docB.bin` |
| R07 | `loadXML(File)` non-native | `AppD.loadXML(File, false)` |
| R08 | API `setXML` | `runApiDocumentReplacement` → clearing `setXML` with the API marker |
| R09 | control: `loadFile` native | `AppGeoCeDG.loadExistingFile` (File → Open, Open Recent, file drop) |
| R10 | control: `loadFile` non-native | `AppGeoCeDG.loadExistingFile` with `docB.bin` |
| R11 | control: `loadXML(String)` | `AppGeoCeDG.loadXML(String)` |
| R12 | control: File → New on a saved document | `AppGeoCeDG.fileNew` |
| R13 | tool replacement | `ToolCreationDialogModel.finish` twice (replacing `WorkLine`) |
| R14 | clearing `setXML` without the API marker | `app.setXML(app.getXML(), true)` (the reload of a tool replacement) |
| R15 | Insert File (K1-S success) | `GeoCeDGDocumentInsertion.insert` |
| R16 | Apply Styles | `GeoCeDGStyleApplication.apply` |
| R17 | v1 Open URL with the pre-clear save prompt **cancelled**, then Base64 | `clearConstruction` (prompt answered Cancel) → `GuiManagerD.loadURL("UEs…")` → `setBase64` |
| R18 | v1 Open URL on a saved document, then Base64 | `clearConstruction` (no prompt) → `GuiManagerD.loadURL("UEs…")` |
| R19 | embedded `closeWithoutSavePrompt` | window-less branch of `AppGeoCeDG.closeWithoutSavePrompt` |
| R20 | control: API `reset` to a blank document | `AppGeoCeDG.reset` (E6) |

**Documented emulations** (stated, not hidden):
- R17/R18: the v1 Open URL route first calls `clearConstruction()` and ignores its result,
  then shows a modal URL dialog whose Base64 answer reaches `GuiManagerD.loadURL`. The
  probe answers the save prompt with Cancel (R17) and replaces the modal dialog by the
  same `loadURL` call.
- SC-18: the macro-edit window is a second embedded host on which `openEditMacro` runs;
  `ToolCreationDialogModel.overwriteMacro` then reloads the **original** window, exactly
  as in the product. No real second frame is opened.
- SC-27 New Window: a fresh embedded host built through the same constructor path as
  `GeoCeDGFrame.createNewWindow`; no real frame is opened.

### 2.4 Observables

`ExportArea` explicit producer, MANUAL rectangle and overlay; the `ISO_A_BORDER` link
before and after identity validation and `isLinkLost`; the paste notice; the sheet,
unit, drawing-scale and layer status segments; drawing scale; working layer and hidden
set; Parallel memory state and held reference; tool mode and memory activation; the
orientation cue; object count, labels, current file, saved flag, undo availability and
document units.

Headless limitation: the embedded views have no laid-out size, so the **fallback**
resolution of an export area (visible viewport) resolves to null in this probe. The
explicit producer and the MANUAL rectangle are the relevant observables and are
unaffected.

## 3. Route × state outcome (observed)

Before every route: `MANUAL` + overlay, notice visible, `1:2`, working layer `2`, hidden
`[]`, Parallel `HELD`, cue shown. **Bold** marks state left over from the previous
document after a complete replacement.

| Route | `ExportArea` (producer, rectangle, overlay) | ISO link (M2) | paste notice | scale | working layer / hidden | Parallel memory | document after |
|---|---|---|---|---|---|---|---|
| R01 `.html` via File → Open | **kept** | stale → dropped, link-lost shown | **kept** | 1:1 | 0 / [5] | EMPTY | docB |
| R02 API `openFile` native | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB, file docB.cedg |
| R03 URL drop `file:` | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB, file docB.cedg |
| R04 URL drop stream | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB |
| R05 `setBase64` | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB |
| R06 API `openFile` non-native | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB, file docB.bin |
| R07 `loadXML(File)` non-native | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB, file docB.bin |
| R08 API `setXML` | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB |
| R09 `loadFile` native | reset | cleared, no link-lost | cleared | 1:1 | 0 / [5] | EMPTY | docB |
| R10 `loadFile` non-native | reset | cleared | cleared | 1:1 | 0 / [5] | EMPTY | docB |
| R11 `loadXML(String)` | reset | cleared | cleared | 1:1 | 0 / [5] | EMPTY | docB |
| R12 New | reset | cleared | cleared | 1:1 | 0 / [] | EMPTY | blank |
| R13 tool replacement | kept | stale → link-lost | kept | **1:2 kept** | **6** / [] (recomputed) | EMPTY | same document |
| R14 clearing `setXML`, no marker | kept | stale → link-lost | kept | 1:2 kept | **6** / [] | EMPTY | same document |
| R15 Insert File | kept | **live** (kept) | **new notice** (unit paste notice) | 1:2 kept | 2 / [] kept | HELD (same object) | document + `W`, unsaved |
| R16 Apply Styles | kept | live (kept) | kept | 1:2 kept | 2 / [] kept | HELD (same object) | same document |
| R17 v1 Open URL, prompt cancelled | **kept** | stale → link-lost | **kept** | 1:1 | 0 / [5] | EMPTY | docB |
| R18 v1 Open URL, saved | reset (by the pre-clear) | cleared | cleared | 1:1 | 0 / [5] | EMPTY | docB |
| R19 embedded close | **kept** | stale → link-lost | **kept** | **1:2 kept** | **2** / [] kept | EMPTY | blank, file null |
| R20 API blank reset | reset | cleared | cleared | 1:1 | 0 / [] | EMPTY | blank |

No route raised an exception. The orientation cue stayed shown on every route.

## 4. Scenario results SC-01 to SC-27

Expected values follow the reconciliation: **R** reset, **P** preserved, **C** recomputed,
**N** no approved contract (the bracket gives the analogue-consistent expectation, for
information only). Verdicts:
- `MATCH` — the observation equals an approved contract;
- `MISMATCH — CONTRACT VIOLATION` — an approved contract is not met;
- `NO CONTRACT — OBSERVED` — recorded for the author's decision; no verdict;
- `CONFIRMS SOURCE CHARACTERIZATION` — no contract; the runtime confirms the source
  reading of the reconciliation.

| Id | Route(s) | State | Expected | Observed | Verdict |
|---|---|---|---|---|---|
| SC-01 | R01 | `ExportArea` | R (`AQ-X2`; `.html` is an Open file type, `DQ-F3-2`) | MANUAL rectangle and overlay kept | **MISMATCH — CONTRACT VIOLATION** |
| SC-02 | R02, R03, R04 | `ExportArea` | R (`AQ-X2` + `DQ-A2-6`: API file and URL routes are native Open) | kept on all three | **MISMATCH — CONTRACT VIOLATION** |
| SC-03 | R05 | `ExportArea` | N [R] | kept | NO CONTRACT — OBSERVED (differs from [R]) |
| SC-04 | R06, R07 | `ExportArea` | N [R] | kept | NO CONTRACT — OBSERVED (differs from [R]) |
| SC-05 | R08 | `ExportArea` | N [R] | kept | NO CONTRACT — OBSERVED (differs from [R]) |
| SC-06 | R09, R10 (and R11) | `ExportArea`, notice | R | reset and cleared on all; a non-native **file** through `loadExistingFile` resets | MATCH (confirms the §5.1 correction of the A-2 and C records) |
| SC-07 | R13, R14 | `ExportArea` | N [P] | kept | NO CONTRACT — OBSERVED (equals [P]) |
| SC-08 | R15, R16 | `ExportArea`, link | link P; manual N [P] | link live and kept; MANUAL kept | MATCH (link); NO CONTRACT — OBSERVED (manual, equals [P]) |
| SC-09 | R01–R08, R17 (M2) | link, `linkLost`, sheet segment | stale with notice (`iso-a-border.md:424`) | link dropped as stale at the next validation; `isLinkLost` true; sheet segment "ISO A sheet link released: the export area fell back" | MATCH |
| SC-10 | R13, R14 (M2) | link | stale | stale, dropped, link-lost shown | MATCH |
| SC-11 | R01–R08, R17 | paste notice | R (`DQ-D1-9` "next document transition", with `DQ-A2-6` and C E2/E3/E5) | notice kept on every one | **MISMATCH — CONTRACT VIOLATION** |
| SC-12 | R13, R14 | notice | N [P] | kept | NO CONTRACT — OBSERVED (equals [P]) |
| SC-13 | R16 (R15 for comparison) | notice | N [P] | Apply Styles keeps it; Insert File replaces it by its own unit paste notice | NO CONTRACT — OBSERVED (equals [P]) |
| SC-14 | R01, R03, R04 | drawing scale | R | 1:1 | MATCH |
| SC-15 | R15, R16 | drawing scale | P | 1:2 kept | MATCH |
| SC-16 | R17 (R18 for comparison) | `ExportArea`, notice, scale | scale R; others N | scale 1:1; with the save prompt cancelled, `ExportArea`, notice and the stale link are kept; when the pre-clear runs (R18) they are reset by it | MATCH (scale); NO CONTRACT — OBSERVED (others) |
| SC-17 | R13 (M4) | working layer | N (source: recomputed 2 → 6) | 2 → 6, the highest shown layer used by a drawable object | CONFIRMS SOURCE CHARACTERIZATION |
| SC-18 | macro-edit overwrite (M4) | working layer of the original window | N | original window 2 → 6; `ExportArea` and scale kept; memory emptied | NO CONTRACT — OBSERVED (in-process emulation, §2.3) |
| SC-19 | R15, R16 | working layer | P (`AQ-L8`, `DQ-A2-6`) | 2 kept | MATCH |
| SC-20 | R01, R03, R04 | working layer, hidden set | C (`DQ-A2-1`) | hidden set `[5]` from the document; working layer 0 | MATCH |
| SC-21 | R01–R08, R17 | memory slots | R (EMPTY, `authoring-memory.md:124`) | EMPTY on every route | MATCH |
| SC-22 | R05, R08 with Parallel memory active | activation | P (`authoring-memory.md:274-276`) | Parallel mode and memory activation kept; slot EMPTY | MATCH |
| SC-23 | R02, R03, R09 with Parallel memory active | activation | N | the tool returns to Move; memory activation off; slot EMPTY | NO CONTRACT — OBSERVED |
| SC-24 | every route, startup | orientation cue | P (`orientation-cue.md:179-181`) | shown on all 20 routes and the 4 startup hosts | MATCH |
| SC-25 | R02, R05, R08 on and off the EDT | status segments | C (`unit-system.md:788`; D1 EDT rule) | identical recomputed unit, scale and layer segments on and off the EDT; no exception | MATCH (no race reproduced in two runs) |
| SC-26 | R19 | session states | N (embedded host only; `document-insertion.md:222-223` silent) | construction cleared, file null, saved; `ExportArea`, notice, scale `1:2`, working layer `2` and hidden set kept; memory emptied; ISO link stale | NO CONTRACT — OBSERVED |
| SC-27 | startup `.cedg`, `.html`, `base64://`; New Window constructor | all | fresh | no explicit producer, overlay off, no notice, 1:1, working layer 0 with the document's hidden set `[5]`, memory EMPTY, cue preference kept | MATCH (New Window by constructor emulation, §2.3) |

## 5. Findings

**F-1 — approved Open routes keep the previous export area (B-1).** File → Open of an
`.html` page (SC-01), the API `openFile` of a native document and the URL drop
(`file:` and stream URLs, SC-02) replace the whole document but keep the MANUAL
rectangle, its producer and the overlay. `AQ-X2` states that New and Open reset the
export area, `DQ-F3-2` makes `.html` a File → Open type and `DQ-A2-6` includes the API
file and URL routes in the native Open transaction. These are **demonstrated contract
violations**. Root location (source, confirmed at runtime): the reset exists only in
`AppGeoCeDG.clearConstruction`, `loadExistingFile` and `loadXML(String)`; the routes
above enter through `loadBase64File`, `AppD.loadXML(URL)` and `loadXML(File)` and commit
through `nativeDocumentLoadCommitted` or `documentReplacementCommitted`, which reset
only the layer workspace and the drawing scale. Consequence: the next picture, LaTeX or
DXF export of the new document uses the previous document's MANUAL rectangle.

**F-2 — the paste notice survives a document transition (B-1).** On every route of F-1
and on the non-native routes of F-3 the D1 paste notice of the previous document stays
visible (SC-11), against `DQ-D1-9` ("clears on the next document transition"). This is a
**demonstrated contract violation** with the same root location.

**F-3 — non-native replacements keep the session state (B-1).** `setBase64`, the
non-native API `openFile`, `loadXML(File)` and the API `setXML` (SC-03 to SC-05) keep the
export area and the notice. No approved text fixes the expected behaviour; the
transport-independence of `DQ-C7` (the drawing scale does reset, SC-14) suggests R.
Recorded for the author's decision.

**F-4 — the ISO A link is protected by identity (B-1 mitigation).** On every
non-resetting route the `ISO_A_BORDER` link is dropped as stale at the next validation,
and the sheet segment reports it (SC-09, SC-10), as `iso-a-border.md` §11.3 requires.
The MANUAL rectangle, the explicit `EXPORT_POINTS` producer, the overlay and the notice
have no such protection.

**F-5 — tool replacement recomputes the working layer (B-2).** The clearing reload of a
tool replacement (`ToolCreationDialogModel.finish` and `overwriteMacro`) runs the A-2
document commit, so the working layer is recomputed from the document (2 → 6 here),
also in the original window of a macro-edit session (SC-17, SC-18). Export area, notice
and drawing scale are kept, as for an unchanged document. No approved text fixes the
working-layer behaviour of this route; the C labels disagree (reconciliation §5.3).

**F-6 — other transitions.** The window-less `closeWithoutSavePrompt` keeps the whole
session state while the construction becomes blank (SC-26; embedded hosts only). The v1
Open URL route resets through its pre-clear only when the save prompt is not cancelled
(SC-16). A native Open or API `openFile` returns an active Parallel tool to Move (SC-23),
whereas `setBase64` and `setXML` keep it (SC-22).

**Corrections of earlier records confirmed.** A non-native **file** opened through
`loadExistingFile` (File → Open, Open Recent, file drop) resets (SC-06), as the
reconciliation §5.1 stated against the broader A-2 and C wording.

## 6. Stop and author decision request

The authorization does not permit a productive correction. Because F-1 and F-2 are
contract violations whose correction needs code, the **corrective branch is stopped**
here, the evidence is retained and a separate author decision is requested. Independent
authorized G work continues; this stop is not a technical failure of that work.

Decisions requested before the global `PRE-G9B-R6-plus` closeout:

| Observation | Options (reconciliation `DQ-G-5`) | Evidence |
|---|---|---|
| `OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET` (B-1) | (B) accept as a characterized limitation; (C) defer with a named owner; (D) a separately authorized correction. A split outcome is possible: D for the approved Open routes (SC-01, SC-02, SC-11), B or C for the routes without a contract (SC-03 to SC-05, SC-16, SC-26). | F-1 to F-4, F-6 |
| `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` (B-2) | accept the recomputed working layer, defer it, or authorize a correction; disposed of separately from B-1 | F-5 |

Information for a later D (not a proposal to implement), unchanged from the
reconciliation §2 `DQ-G-5`: a correction would be one session reset of `ExportArea` and
the paste notice at the existing successful-transition events E2, E3 and E5
(`nativeDocumentLoadCommitted`, `documentReplacementCommitted` and the API branch of
`setXML`), never in `commitLoadedDocument` or the generic clearing `setXML`, which also
serve tool replacement. If D is authorized, the rows of §4 become its regression
fingerprint.

## 7. Evidence

| Item | Value |
|---|---|
| probe source (scratch, untracked) | `GReplacementCharacterizationProbe.java`, SHA-256 `5f3c7d9c49d1602489a72bd29e1731646089ed8c8b7f67666dec0e3ae74c70fc` |
| init script | `probe.gradle`, SHA-256 `bb260003112386845306e3d14913ee328266f05ea59a62026b412e1c42b697cd` |
| run 1 results (57 rows) | `probe-results-run1.json`, SHA-256 `a9fa785828a51fa78e7e7acc6aed4a938580af4f7f1b68a6e74c283977828065` |
| run 2 results (57 rows) | `probe-results-run2.json`, SHA-256 `9f882810daa664649d88b20d6cd8c98bcc3361035fa324039199ffdcd2d96f45` |
| reproducibility | all 57 rows equal after removing Java identity hashes from the held-reference field |
| location | git-ignored `artifacts/agent/pre-g9b-r6-plus-g-characterization/` (local evidence, not authority) |
| command | `gradlew.bat :desktop:desktop:test --tests org.geocedg.desktop.GReplacementCharacterizationProbe --init-script <scratch>/probe.gradle --no-daemon --console=plain`; exit 0 on both runs |

No product, build, packaging, test or verification path changed for this report.

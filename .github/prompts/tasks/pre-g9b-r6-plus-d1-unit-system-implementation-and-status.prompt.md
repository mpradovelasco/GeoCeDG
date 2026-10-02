# PRE-G9B-R6-plus-D1 — unit-system implementation and status integration

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-03 approves the documentary
preparation package of `PRE-G9B-R6-plus-D1` (`T_R6PLUS_D1_PROMPT`), names it as
the exact start of the implementation branch, gives the author dispositions on
`DQ-D1-1` to `DQ-D1-11`, accepts the characterization `C1`–`C11` as
implementation constraints, keeps `GLOBAL_IMPACT / FINAL`, and authorizes the
implementation of `D1`. The instruction is recorded, versioned, in the
[D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md).
It also requires this amendment, as the first tracked edit of the phase, so
that the prompt becomes the executable contract of the phase. The amendment
replaces the prepared prompt of `T_R6PLUS_D1_PROMPT` (blob
`96f6e612cce1c43e2043abafe17ead3e897f4ef8`): it records the authorized branch
start, integrates the `DQ-D1` dispositions, corrects the boundaries they
supersede (the saved-state seam of `DQ-D1-5` and the Classic/Web boundary of
`DQ-D1-11`), and freezes the class. Where the prepared prompt and the record
differ, the record prevails; this amendment carries that precedence into the
text. Every other scope, forbidden-scope and stop rule of the prepared prompt
is kept. The preparation decisions are recorded in the
[D1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d1_author_decisions_record.md);
the evidence behind the characterization below is in the
[D1 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_d1_preparation_characterization_report.md).

This file is an execution contract, not a second policy document: the unit
semantics, grammar and matrices are stated once in the
[unit-system specification](../../../geocedg/specs/units/unit-system.md) v1.0
and decided in
[ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md).
This prompt cites their clauses as `§n.m` and does not restate them.

```text
PRE-G9B-R6-plus-D1 =
AUTHORIZED FOR IMPLEMENTATION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (D1 implementation and technical verification only)
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — SHARED DOCUMENT UNIT STATE,
                           <geocedgUnits> PERSISTENCE, UNIT LIFECYCLE, NEW-DOCUMENT
                           DEFAULTS, PASTE NOTICE AND STATUS INTEGRATION
DEPENDS_ON               = PRE-G9B-R6-plus-D0  = PASS — AUTHOR APPROVED — PUBLISHED
                           PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (status bar; AUTHOR_SMOKE = PASS)
                           PRE-G9B-R6-plus-B   = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (export regressions; AUTHOR_SMOKE = PASS)
DEPENDS_ON_PACKAGE       = D1 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
                                                 (not published)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-A-2   (operational order; not authorized)
STOP_STATE               = PRE-G9B-R6-plus-D1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
```

`implementationAuthorized = true` authorizes only the implementation and
technical verification defined below. It authorizes no later subphase.
`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
The phase stops with one exact technically verified candidate pending author
review and author smoke, with `selfApproved = false`, `authorApproved = false`
and `passClaimed = false`. The agent does not perform or claim author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement the `D1` consumer contract of the unit specification (§18.1), and
nothing else:

1. one shared, document-scoped unit state (§5.1) with its single effective-unit
   function (§5.2), conversions (§5.3) and physical-meaning equality (§5.4);
2. the `<geocedgUnits>` writer and reader (§8.1–§8.4), including the
   deterministic canonical binary64 factor (§8.4);
3. reset and application through clearing loads only, on every route of §9,
   and the ignored contexts of §8.8;
4. fail-closed reading of recognized invalid or future unit metadata (§8.7),
   with a restore that never leaves a half-loaded document;
5. the document operations and their undo, redo, redefine and rollback
   behavior (§7);
6. the document-scoped `usm` definition and lifecycle (§4);
7. new-document defaults from portable GeoCeDG preferences (§10);
8. transient copy/paste provenance and the non-blocking mismatch notice (§11);
9. GeoCeDG status segments for the construction and presentation units,
   extending the `A-1` status bar (§6.2);
10. the minimal user-facing configuration needed to operate the above;
11. deterministic tests and global compatibility evidence, accepted by one
    `FINAL` on one exact frozen candidate.

```text
UnitState := { c : unit-token?, p : unit-token?, usm : NONE | DEFINED(k, name?, symbol?) }
             owner: the shared document construction; never geometry (ADR 0032, decision 1)
```

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = GLOBAL_IMPACT          (author-frozen on 2026-10-02; kept at the
                                               authorization of 2026-10-03)
frozenAtPhaseStart   = true
PLANNED_ACCEPTANCE   = FINAL — one and only one, on one exact frozen clean candidate,
                       after FINAL -PlanOnly; no downgrade because focal tests pass
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` maps
`GLOBAL_IMPACT` to FULL, whose canonical profile is `FINAL`. The impact facts
that select it: a new document-semantic element in the shared
`<construction>` XML, read on every load and written on every save, undo,
redefine and rollback snapshot; a fail-closed rule in the shared parser used by
every document route; a legacy byte-identity obligation across the whole
document corpus; and a change of the saved-state predicate consumed by every
New, Open and close prompt. The new tests run canonically only in
`final.shared` and `final.desktop`.

Escalation triggers. Stop and report, rather than widen scope silently, when
any stop condition of the author (see *Stop conditions*) or of this prompt is
met. A `VERIFICATION_ESCALATION_REQUEST` does not apply upwards, because
`FINAL` is already the highest acceptance level; a request to run more than one
`FINAL` on an unchanged candidate is refused.

### Authorities and author decisions this prompt implements

| Authority | Effect in `D1` |
|---|---|
| [ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md) decisions 1–7 | implemented; decision 8 (export) belongs to `C` |
| [unit specification](../../../geocedg/specs/units/unit-system.md) §3–§11, §16, §17, §18.1 | implemented; §12 (`drawingScale`), §13, §15 (`C`), §14 (`E2`), §18.4 (`E3`) are not |
| [D0 closeout](../../../docs/validation/pre_g9b_r6_plus_d0_closeout_record.md) findings 1, 2, 3, 5 | mandatory characterization and tests in `D1` (C3, C5, C6–C9 below) |
| D0 closeout finding 4 | `drawingScale` belongs to `C`; the conditional `D1` clauses of §18.1 and §19.1 do not apply |
| D0 closeout findings 6, 7 | not `D1` (`C` preparation; `G` backlog) |
| [D1 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d1_author_decisions_record.md) | the frozen class, product boundary, architecture rule and stop conditions |
| [D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md) | the authorization, the `DQ-D1-1`–`DQ-D1-11` dispositions (they prevail over the prepared defaults), the accepted characterization constraints and the additional stop conditions |
| `AQ-U1`–`AQ-U6`, `DQ-D0-1`–`DQ-D0-13` | through the clauses that carry them (§19) |

The specification controls over the `P0` recommendations and over older
planning text wherever they differ. No open decision of any other subphase is
resolved here.

### Architecture decision: shared versus Desktop

Decided before any implementation path (author architecture rule):

| Concern | Owner | Why |
|---|---|---|
| unit state, constraints, effective-unit function, conversions, equality, canonical factor writer and reader | shared kernel-document layer (`geocedg-common`, held by `Construction`) | document semantics every frontend must read identically (`AGENTS.md` §4); persisted by the shared serializer |
| persistence, reset, application, fail-closed reading, ignored contexts | shared `Construction`, `MyXMLHandler`, `MyXMLio` (minimal seams) | the only XML authority; every route of §9 passes through it |
| undo-visible document state | the ordinary construction-XML snapshot | §7.3; no second undo channel |
| document operations (validated mutation, one undo point) | a shared operation API on the unit state, invoked by Desktop | validation and no-op detection must be identical for every caller |
| presentation-change notification | shared listener on the unit state | §18.1; no construction recompute |
| configuration UI, portable new-document defaults, status presentation, paste-notice orchestration, clipboard provenance | Desktop GeoCeDG layer (`geocedg-desktop`) | presentation and session state; never authoritative |

No geometry algorithm, command or construction element reads units during
`compute()`; no second semantic unit graph exists; the state is not duplicated
in Desktop or Python. A Desktop component that needs a unit asks the shared
effective-unit function at the moment it presents.

### Characterization results at the base

The preparation re-read every seam below at the base, with scratch-only probes
where useful; the evidence and the probe identities are in the
[characterization report](../../../docs/validation/pre_g9b_r6_plus_d1_preparation_characterization_report.md).
Paths use the abbreviations of the specification (§6.4) plus `jre/` =
`source/shared/common-jre/src/main/java/org/geogebra/common/jre/`,
`jre-test/` = `source/shared/common-jre/src/test/java/org/geocedg/common/` and
`dtest/` = `source/desktop/desktop/src/test/java/org/geocedg/desktop/`. The
phase re-establishes each citation before relying on it. A result marked
**contract** is binding on the implementation; a result marked **finding** is
evidence that a contract rests on. The author accepted `C1`–`C11` on
2026-10-03 as implementation constraints, subject to that re-establishment;
where a `DQ-D1` disposition below changes a contract, the disposition governs.

#### C1. State owner and lifecycle

- **Owner (contract).** One `final` unit-state holder per `Construction`,
  created in the constructor beside the spatial registry
  (`common/kernel/Construction.java:242`, `:275-283`, getter precedent
  `:316-318`), in a new `org.geocedg.common` package. A `MacroConstruction`
  (`common/kernel/MacroConstruction.java:31-61`) has one too, but it stays
  `EMPTY`: no operation targets it and its parses ignore the element.
  Reachable from shared code as `kernel.getConstruction()` and from Desktop as
  `app.getKernel().getConstruction()`; Desktop never caches a copy.
- **Reset (contract).** `Construction.clearConstruction` (`:4094-4143`) resets
  the state to `EMPTY` for every purpose, beside title, author, date and
  worksheet text (`:4132-4137`), and notifies listeners.
- **Clearing load (finding → contract).** The parse flag does not identify a
  clearing load of the document construction. Every archive that contains
  macros parses `geogebra.xml` with `clearConstruction = false`, after the macro
  parse has cleared the construction, and forces the purpose
  `NATIVE_OR_UNDO_RESTORE` instead (`jre/io/MyXMLioJre.java:200-223`: macro
  parse `:203`, forced purpose `:214-220`, construction parse `:221`). The
  handler receives only a load purpose
  (`common/io/MyXMLio.java:419-421`; `common/io/MyXMLHandler.java:2925-2928`),
  and the construction's pending purpose is a one-shot consumed by the
  `geocedgSpatial` section (`MyXMLHandler.java:2984-2985`;
  `Construction.java:454-458`). **Contract:** the reader fixes the effective
  purpose of a document construction parse once, when the document
  `<construction>` starts, from the construction's pending purpose (read, not
  consumed) or else the parser default, and uses it for the unit element at any
  child position. It applies the element only for `NATIVE_OR_UNDO_RESTORE`,
  `REDEFINE_REBUILD`, `ORDINARY_EDIT_REBUILD` and `ROLLBACK_RESTORE` on a
  non-macro kernel; it ignores it for `CLIPBOARD_IMPORT`, `GENERIC_MERGE` and
  every `MacroKernel` parse (`kernel instanceof MacroKernel`, precedent
  `MyXMLHandler.java:2575`). No consumer of the spatial one-shot changes
  behavior.

Lifecycle map (every row is an obligation of `T-ROUTES`):

| Route | Path at the base | Clearing | Effective purpose | Unit state |
|---|---|---|---|---|
| save `.cedg`, full XML | `MyXMLio.java:307-320` (`getConstructionXML` `:316`) | — | — | written per §8.3 |
| undo snapshot | `MyXMLio.getUndoXML` `:139-168` (`:156`) | — | — | written per §8.3 |
| File → Open `.cedg`/`.ggb` (GeoCeDG) | `GuiManagerD.openFile` `:1928-2102` → `doOpenFiles` `:2126-2229` → `AppGeoCeDG.loadExistingFile` `:387-400` → `AppD.loadExistingFile` `:3065-3078`; preflight `AppD.readAndPreflightNativeArchive` `:3135-3152` → `DocumentArchivePreflight.validate` (`desktop/io/DocumentArchivePreflight.java:36-52`); live `loadNativeArchive` `:3222-3281` | yes | `NATIVE_OR_UNDO_RESTORE` | the document's own; defaults never; fail closed per C3 |
| archive with macros | `MyXMLioJre.java:200-223` | macro parse | forced `NATIVE_OR_UNDO_RESTORE` | applied (C1 contract) |
| startup with a file | `AppD.handleFileArg` `:1213-1292` → `loadFile` `:1279`; failure → blank document `:505-508` | yes | as Open | the document's own; a failed load ends blank (C6) |
| `setXML` API | `GgbAPI.java:1474-1477` → `App.setXML(xml, true)` `App.java:5115-5134` | yes | `NATIVE_OR_UNDO_RESTORE` | applied; fail closed per C3 |
| undo, redo | `UndoManagerD.loadUndoInfo` `:183-244` → `MyXMLioJre.readZipFromMemory` `:280-297`; headless `DefaultUndoManager` `:62-69` → `Construction.processXML` `:4230-4245` | yes | `NATIVE_OR_UNDO_RESTORE` | restored from the snapshot |
| redefine rebuild, batch redefine, CAS change | `Construction.java:2098-2146`, `:2760-2861`, `:2913-2935` → `buildConstruction` `:4177-4203` → `processXML` `:4184` | yes | `REDEFINE_REBUILD` / `ORDINARY_EDIT_REBUILD` | restored from `getCurrentUndoXML` |
| failed redefine restore | `restoreAfterRedefine` `:4205-4209` | yes | default | restored |
| atomic mutation rollback | `runAtomicConstructionMutation` `:2671-2688` → `restoreSpatialRedefineSnapshot` `:2690-2705` | yes | `ROLLBACK_RESTORE` | restored |
| rejected-parse restore | `MyXMLio.doParseXML` `:404-405`, `:435-449`; `restoreRejectedSpatialParse` `:504-529` | yes | `ROLLBACK_RESTORE` | restored (extended in C3) |
| paste | `CopyPasteD.pasteFromXML` (`desktop/util/CopyPasteD.java:457-559`) → `evalClipboardXML` `:561-573` → `InternalClipboard.evalClipboardXMLAtomically` (`common/util/InternalClipboard.java:620-657`, `CLIPBOARD_IMPORT` `:639`) | no | `CLIPBOARD_IMPORT` | never in clipboard XML; ignored; target unchanged; provenance (C9) |
| paste rollback | `restoreClipboardSnapshot` `InternalClipboard.java:663-685` (snapshot `app.getXML()` `:623`) | yes | `ROLLBACK_RESTORE` | restored |
| Insert File | `AppD.java:4747` → `CopyPasteD.insertFrom` `:631-671` (hidden app `AppGeoCeDG.java:444-450` loads the file; `copyToXML` `:634`) | no (target) | `CLIPBOARD_IMPORT` | target unchanged; provenance per `DQ-D1-7` |
| `evalXML`, action replay | `GgbAPI.java:169-179` → `App.setXML(xml, false)` `:5126`; `ConstructionActionExecutor.java:57`, `:89`, `:131-132` | no | `GENERIC_MERGE` | ignored with a text log |
| document macros, `.ggt`, `addMacroXML`, macro construction load | `MyXMLioJre.java:200-208`; `MyXMLHandler.java:2629-2641`; `MacroConstruction.java:59-61`; `App.java:3664-3681` | macro | `MacroKernel` | ignored with a text log |
| macro and tool XML write | `Macro.initMacro` `:175`, `getXML` `:792-796`, `buildMacroXML` `:463-516` | — | — | never written (macro construction stays `EMPTY`; writer guard) |
| tool editing | `App.openEditMacro` `:1734-1746` in a new frame (`desktop/gui/dialog/ToolManagerDialogD.java:283`) | yes | `NATIVE_OR_UNDO_RESTORE` | the macro construction's (`EMPTY`) |
| preferences XML | `MyXMLio.getPreferencesXML` `:346-359`; `App.getCompleteUserInterfaceXML` `:1880-1906` | — | — | never written |
| File → New | `AppD.fileNew` `:1149-1177` → `AppGeoCeDG.clearConstruction` `:377-384` → `AppD.clearConstruction` `:2931-2945` (`initUndoInfo` `:2936`) → preferences XML `:1174` (product: a second clearing `setXML(xml, true)`) | yes | — | reset; then defaults (C6) |
| startup without a file, New Window (`Ctrl+N`) | `GeoGebraFrame.createNewWindow` `:448-467` → `GeoCeDGFrame.createApplication` `:28-30` → `AppD` constructor `:479-547` (preferences `:505-508`, undo baseline `:534`, `setSaved` `:547`) | fresh | — | `EMPTY`; then defaults (C6) |
| `AppD.reset` | `:562-568`: reload of the current file, else `clearConstruction` | yes | — | reload = Open; else blank (C6) |
| `openURL` | `GuiManagerD.java:1838-1842`: `clearConstruction`, then the URL dialog | yes | — | blank, then the loaded document's own |
| DXF staleness fingerprint | `geocedg-common/export/G9X1GeometryExportAdapter.java:1049-1060` | — | — | unchanged (`C`) |

#### C2. XML persistence seams

- **Writer (contract).** `Construction.getConstructionXML` (`:1517-1550`)
  writes the element immediately after `<worksheetText>` (`:1528-1533`) and
  before the `geocedgSpatial` section (`:1535-1538`), the elements (`:1540`)
  and the groups (`:1542`), on its own line, with the existing attribute
  writer's escaping, in exactly the order of §8.2, flat and self-closing, only
  when the state is not `EMPTY` (§8.3), and never for a `MacroConstruction`
  (defense in depth, since this method also serializes macro constructions,
  `Macro.java:175`, `:795`). The method swallows every exception into
  `Log.debug` (`:1545-1546`), so the unit writer **must not be able to throw**:
  every value it writes was validated when it was stored.
- **Reader (contract).** A `geocedgUnits` branch in
  `MyXMLHandler.startConstructionElement` (`:3062-3122`, the
  `MODE_CONSTRUCTION` dispatch `:3087-3094`) with its own construction mode on
  the `geocedgSpatial` precedent (`:3089-3091`, `:3104-3111`, `:3167-3178`,
  free text `:311-316`), so that a child element or non-white-space text is
  detected and rejected (§8.7.2). A second element in the same construction is
  rejected (§8.7.4). The element has no `label` attribute, so the redefine
  string surgery (`Construction.java:3013`, `:3026`) never matches it.
- **Older readers (finding).** At the base an unknown construction child is
  logged and skipped (`MyXMLHandler.java:3092-3094`). Probe `P-OLDER-READER`
  confirmed it on the base build: a clearing load, a `version="99"` element and
  an `evalXML` fragment all load, log `unknown tag in <construction>:
  geocedgUnits`, and the element is absent on re-save (§8.9).
- **Misplaced element.** The spatial precedent rejects a section outside a
  document construction (`MyXMLHandler.java:712-714`, `:753-755`); §8.7 does
  not name that case. See `DQ-D1-3`.

#### C3. Fail-closed transaction and restore

| Phase of a failed load | At the base | Contract |
|---|---|---|
| pre-load state | the live document, its file, path, recent list, saved flag and undo history | unchanged by any rejected unit metadata |
| failure point | the unit branch of the shared handler, during the parse of the document `<construction>` | throws a dedicated unchecked exception (not `MyError`) that carries a structured unit diagnostic naming the §8.7 case |
| Desktop File → Open, `.cedg` and `.ggb` | `DocumentArchivePreflight.validate` parses on a scratch `AppDNoGui` before the live app is touched; a non-`MyError` exception or a `false` result shows `Errors.LoadFileFailed` and returns `false` (`AppD.java:3135-3152`, `:3067-3068`) | the unit rejection is raised in the preflight, so the live document is never mutated; result: the previous document, file, path, recent list, `isSaved` and undo history exactly as before, the file on disk only read (precedent `dtest/GeoCeDGDocumentLifecycleTest.corruptNativeArchiveIsRejectedBeforeLiveLoad` `:261-457`, which already covers a `geocedgSpatial version="999"` fixture) |
| a live-load failure after a passing preflight | `restoreNativeDocumentLoadState` re-reads the previous archive and restores path, `isSaved` and the recent list; the new undo baseline is never committed (`AppD.java:3222-3337`) | unchanged; covered by an injected failure |
| startup with a file | a failed load ends in the blank document (`AppD.java:505-508`) | unchanged; the blank document then follows C6 |
| `setXML` API and other clearing parses through `MyXMLio` | the entry snapshot `app.getXML()` (`MyXMLio.java:404-405`) is restored only for a `SpatialIdentityException` or an identity-bearing parse (`:495-502`); otherwise the exception propagates after a cleared, partially parsed construction | **extended**: a unit rejection also triggers `restoreRejectedSpatialParse` (`:504-529`), so the construction returns to its entry snapshot; the exception still propagates and `App.setXML` shows its error (`App.java:5130-5133`) |
| undo, redo, redefine rebuild, rollback | the XML is produced by the same build and is always valid; a unit rejection there is a writer defect | no new recovery path; the existing undo fallback (`UndoManagerD.java:236-239`) and redefine restore (`Construction.java:4205-4209`) apply; a test proves that every valid state round-trips through these routes without reaching the reject path |
| error reporting | every failure shows the generic `LoadFileFailed` with the file name | the user-visible message names the unit defect (`DQ-D1-4`), at least in `en` and `es`; the diagnostic is logged as text, never as a `RuntimeException` object (the headless logger turns logged runtime exceptions into `AssertionError`) |
| dirty and undo state | `MyXMLio` never touches undo or `isSaved`; the native transaction restores them | unchanged; asserted in every fail-closed test |

**Load-architecture finding.** Fail-closed unit parsing needs no material
broadening: the Desktop native transaction already preflights both GeoCeDG
document formats, and the `MyXMLio` restore needs one additional trigger
condition. If the implementation finds otherwise, it stops before changing
product (author stop rule).

#### C4. Undo, redo and redefine

- **Snapshot (finding).** The undo snapshot, the redefine snapshot, every
  rollback snapshot and the paste-rollback snapshot all serialize through
  `getConstructionXML` (C1 table), so the element rides every route without a
  second channel.
- **No de-duplication on Desktop (finding).** `FileAppState.equalsTo` always
  returns `false` (`desktop/main/undo/FileAppState.java:74-77`), so every
  `storeUndoInfo` creates an undo point; headless de-duplicates equal strings
  (`StringAppState.java:46-48`). **Contract:** a document operation (§7.1)
  validates first, compares the would-be state with the current one, and only
  then mutates and calls `storeUndoInfo()` exactly once. A rejected or no-op
  operation stores nothing.
- **Kernel-settings pattern (finding).** Angle unit and rounding changes call
  only `setUnsaved()` and ride the next snapshot
  (`desktop/gui/dialog/options/OptionsAdvancedD.java:598-608`;
  `common/properties/impl/general/AngleUnitProperty.java:58-61`). Unit
  operations must not copy that pattern. Precedent for one undo point per
  metadata change: `desktop/gui/TitlePanel.java:215-227`,
  `geocedg-desktop/GeoCeDGActionRegistry.java:474-482`.
- **Asynchronous store (finding).** `UndoManagerD.storeUndoInfo` stores on a
  new thread (`:57-70`); `initUndoInfo` does not bump the generation
  (`:125-139` does). Desktop tests set a baseline with
  `prepareUndoBaseline`/`commitUndoBaseline` and wait with
  `addUndoInfoStoredListener` latches (precedent
  `dtest/PreG9BR6PlusA1LayerWorkspaceTest.java:586-597`); "no store" is
  asserted with a 2-second latch timeout (`dtest/PreG9BR6PlusA1HiddenLayerTest.java:179-202`).
- **Saved state (finding).** `App.isSaved()` returns `true` for a construction
  that is not started (`common/main/App.java:1672-1675`), and
  `Construction.isStarted()` counts only used geo types and macros (`:4003-4005`);
  `isSaved()` is its only consumer. A unit change in an otherwise empty
  document would therefore never prompt on New or close. Resolved by the
  replaced `DQ-D1-5`: a save-relevant-content seam, `isStarted()` unchanged.
- **Rebuild identity (finding).** Undo, redo, rebuild and rollback re-create
  every instance (`jre-test/spatial/PostG9U1A4ConstructionPositionCharacterizationTest.java:93-96`),
  so object identity is checked by label, `PersistentGeoId` and order, never by
  `assertSame`, except across a pure unit operation, which re-parses nothing.
- **First undo (finding).** With a tool preview active, the first undo only
  cancels the preview (`common/kernel/Kernel.java:4390-4397`); tests start in
  Move mode.

#### C5. Binary64 canonical representation

- **Supported runtimes.** The developer guide requires complete JDKs 17 and 25
  and launches Gradle with Java 22 (`docs/developer/geocedg_developer_guide.md:71-73`,
  `:100`). Shared code compiles with toolchain 17
  (`source/build-logic/convention/src/main/kotlin/java-conventions.gradle.kts:6-10`);
  shared and Desktop tests run on 17; the Desktop product launcher and the
  package runtime are 25 (`source/desktop/desktop/build.gradle.kts:137-148`;
  `tools/release/build-windows-package.ps1:467-478`).
- **Probe `P-BINARY64` (finding).** A scratch probe compared
  `Double.toString` with an exact `BigDecimal` implementation of the Java SE
  19+ `Double.toString` specification, on 48 fixed reference entries (45
  distinct values: ordinary decimals, the plain/scientific boundaries at
  `10^-3` and `10^7` and their binary64 neighbours, powers of two,
  `MIN_VALUE`, `MIN_NORMAL`, `MAX_VALUE`, very small and large values, values
  with 16–17-digit shortest forms) and a seeded sweep of 400 000 values, under a
  non-English default locale, on Corretto 17.0.10, OpenJDK 17.0.8, Java 22.0.2
  and Temurin 25.0.4:
  - the exact reference writer produced byte-identical output on every runtime;
  - `Double.toString` on 22 and 25 equals it on every entry and sweep value;
  - `Double.toString` on **17 differs on 8 of 48 entries and 581 of 400 000
    sweep values** (for example `1.0E23` → `9.999999999999999E22`,
    `2^60` → `1.15292150460684698E18`, `8.41E21` → `8.409999999999999E21`);
  - a naive "shortest `HALF_EVEN` rounding that round-trips" writer differs at
    `MIN_VALUE` (`5.0E-324` instead of `4.9E-324`), because of the
    specification's length-1-or-2 rule;
  - `Double.parseDouble` round-trips every reference string on every runtime.
- **Contract.** `D1` must not use `Double.toString`, `String.valueOf(double)`,
  string concatenation of a `double` or any kernel number formatter for
  `usmMetersPerUnit` (§8.4: JDK 17 does not meet the specification). It provides
  a small deterministic canonical writer in shared code that implements the
  §8.4 selection exactly, including the length-1-or-2 rule and the ties-to-even
  interval ends, using only exact arithmetic already used in shared GeoCeDG code
  (`java.math.BigDecimal`, precedent
  `geocedg-common/kernel/spline/SplinePrecisionSolve2D.java`), ASCII output and
  no locale. The reader checks the §8.4 lexical grammar before conversion
  (rejecting a sign, white space, hexadecimal, `NaN`, `Infinity`, a Java type
  suffix and a decimal comma, which `Double.parseDouble` alone would accept or
  misread) and requires a finite result greater than zero. The reference table
  of the report is the fixed fixture; the sweep property is
  `parse(write(k)) == k` and `write(parse(write(k))) == write(k)`.

#### C6. Preferences, New and Open

- **Store (finding).** GeoCeDG stores portable preferences in an isolated
  properties file (`geocedg-desktop/GeoCeDG.java:37-61`;
  `desktop/main/GeoGebraPortablePreferences.java:73`, `:127-130`, `:240`) behind
  the `GeoCeDGPresentationPreferences.Store` interface
  (`geocedg-desktop/GeoCeDGPresentationPreferences.java:23-29`, `:66-74`,
  `:181-199`). The file reaches disk only when a window closes
  (`desktop/gui/app/GeoGebraFrame.java:222-228`) or settings are saved
  explicitly. Tests inject a
  `MemoryStore` (`dtest/PreG9BS4PresentationSizingTest.java:538-555`); app-level
  tests otherwise touch the developer's real store.
- **New (finding).** `AppGeoCeDG` has no `fileNew` override; `AppD.fileNew`
  takes the undo baseline inside `clearConstruction` (`:2936`) and re-applies
  the preferences XML afterwards (`:1174`), which in the product is a second
  clearing load. Startup and New Window never call `fileNew` or the app-level
  `clearConstruction`; their baseline is taken at `AppD.java:534`, before the
  `AppGeoCeDG` constructor body runs (`geocedg-desktop/AppGeoCeDG.java:100-104`),
  and the constructor's `fileLoaded` is a local variable (`AppD.java:489`).
- **Contract.**
  - Defaults are read from two keys, `geocedg.units.new-document-construction.v1`
    ∈ {`unspecified`, `mm`, `cm`, `m`} and
    `geocedg.units.new-document-presentation.v1` ∈ {`none`, `mm`, `cm`, `m`}
    (§10; key names fixed here, `DQ-D1-6`); an absent or invalid stored value
    reads as unset, is never written back on read, and is never `usm`.
  - Defaults apply exactly once at the end of each transition that leaves a new
    blank document: after `super.fileNew()` completes (so after the
    preferences XML), at the end of the startup or New Window constructor when
    no document was opened (including a failed startup load), and after an
    `AppD.reset` or `openURL` clear that leaves a blank document. They never
    apply on Open, a reset reload, undo, redo, redefine, rollback, paste,
    Insert File, `setXML`, `evalXML`, a hidden helper app before its load, or
    tool editing.
  - The startup discriminator is a protected no-op hook called by the `AppD`
    constructor once it knows that no document was opened (the `fileLoaded`
    decision `:489`; the blank-startup branch `!fileLoaded && !ggtloading`
    `:505-508`), upstream-identical by default and registered in
    `docs/upstream/modified-files.yml`; a `.ggt`-only startup also leaves a
    blank document and receives the defaults. A `currentFile == null` test is
    insufficient, because a URL or Base64 startup leaves no current file.
  - When applying defaults changes the state (physical construction default),
    the undo baseline is re-taken afterwards and the document stays saved, so
    the first undo of a later unit operation returns to the defaults and no
    save prompt appears for an untouched new document. With an unspecified
    construction default the state stays `EMPTY` and the New lifecycle is
    upstream-identical.
  - Changing a preference is not a document operation (§7.3, §10): it never
    alters the current document's `UnitState`, XML, undo stack or saved/dirty
    state (`DQ-D1-2`).
- **Restore default settings (finding).** "Restore default settings" and
  `--resetSettings` exist only in the v1 fallback and do not clear GeoCeDG keys
  (`desktop/main/GeoGebraPreferencesD.java:596-613`); `D1` does not change that.

#### C7. `usm` user interface and lifecycle

- **Placement (finding).** The product menu has no Document section; Options
  holds `options-product` (`apps/geocedg/application-profile.yml:3479-3484`,
  `:3604-3620`), whose contents no test pins. A new action needs a registry
  target (`geocedg-desktop/GeoCeDGActionRegistry.java:53-77`, `:241-416`), five
  localized texts (`geocedg-desktop/GeoCeDGProfile.java:320-322`) and a cluster
  among the fixed 18; the production pin `actions.size() != 124`
  (`GeoCeDGProfile.java:340-344`) and the test pins in
  `G9U1ActionRegistryTest:65`, `G9U1ProfileCompilerTest:44`, `:211`, `:383`,
  `G9U1WorkspaceSurfaceTest:124`, `:702`, `GeoCeDGProfileTest:45` and
  `PreG9BR6PlusA1LayerWorkspaceTest:448` change by exactly one.
- **Dialog precedent (finding).** `GeoCeDGExportAreaPrompt` (an injectable
  prompt with text fields, `:24-94`) and its modal warning
  (`AppGeoCeDG.java:266-284`, `:311-316`), driven by tests through an injected
  prompt and `Mockito.mockStatic(JOptionPane.class)`
  (`dtest/PreG9BR6PlusBExportSurfaceTest.java:221-238`).
- **Contract (the least invasive v1 UI).** One product action
  `document.units`, appended to `options-product`, opening one injectable
  modal **Document Units** dialog:
  - construction unit: `unspecified`, `mm`, `cm`, `m`, `usm`; presentation unit:
    *follows construction*, `mm`, `cm`, `m`, `usm`, disabled while the
    construction unit is unspecified (§5.1);
  - custom unit: factor (metres per `usm`), optional name, optional symbol;
    *define* is available while `usm` is `NONE`; the factor, name and symbol are
    editable while it is `DEFINED`; *remove* is enabled only while neither
    stored unit is `usm` (§4.3);
  - validation per §4.2 and §4.4 with the §8.4 reader grammar (`DQ-D1-8`: `.`
    as decimal separator, no grouping separators, a decimal comma rejected with
    a localized hint and never converted); a failed edit shows a localized
    warning and leaves the document state, and any previous valid definition,
    unchanged;
  - OK applies one validated combined operation, which is one undo point when
    it changes the state and none otherwise (§7.1); Cancel changes nothing;
  - one `usm` per document; no registry, list or library of custom units.
- **Lifecycle tests** cover define, select, rename, set and clear symbol,
  change factor (active and inactive), switch away and back (the definition
  persists, §4.3, §8.6), remove when unreferenced and refusal while referenced.

#### C8. Status integration

- **Finding.** `GeoCeDGStatusBar` (`geocedg-desktop/GeoCeDGStatusBar.java:20-81`)
  is a `FlowLayout` of `JLabel` segments keyed by stable id, with `addSegment`
  as its extension point and the Javadoc note "D1 adds the unit segments"
  (`:24-25`). Only the layer segment is clickable (`:40-47`); `updateText`
  refreshes only the layer (`:75-80`); it is refreshed by the layer-workspace
  listener and `setLocale` (`AppGeoCeDG.java:129-135`, `:463-471`), never on
  undo or redo, and exists only with the full GUI (`:366-374`). Its exact texts
  (`Layer: 0`, `Capa: 4`) are pinned
  (`dtest/PreG9BR6PlusA1LayerWorkspaceTest.java:458-485`, `:615-617`).
- **Contract.** `D1` extends this bar; it is the only status authority.
  - Segments in order: `layer` (unchanged text), `construction-unit`,
    `presentation-unit`, separated by separate separator components so the
    layer label text stays exact.
  - Construction segment: the effective construction unit, or the localized
    *unspecified*. Presentation segment: the effective presentation unit, or
    the localized *none* when `effP = NONE` (§5.2). `usm` shows its symbol,
    else `usm`; the tooltip shows the name when present, `1 usm = k m` with
    the canonical `k`, and whether the presentation unit is explicit or follows
    the construction unit.
  - The text is derived presentation only, computed from the shared
    effective-unit function at refresh time.
  - Refresh on the shared unit-state notification (every operation, reset and
    application, so also undo, redo, rebuild, rollback, New and Open), on
    locale change and after layout rebuilds; a notification raised off the EDT
    is marshalled to the EDT; a refresh never touches the construction.
  - Interaction: a click on either unit segment invokes the same
    `document.units` action. The status bar gets no editing controls, because
    a second editing surface would duplicate validation and undo handling.

#### C9. Clipboard provenance and two windows

- **Findings.**
  - Each `AppD` owns one lazily created `CopyPasteD`
    (`desktop/main/AppD.java:355`, `:5036-5044`); every buffer field is an
    instance field (`CopyPasteD.java:57-70`). The JVM-static buffers of
    `InternalClipboard` (`:67-70`) are used only by Web and tests.
  - The buffer is replaced only by copy, cut and Insert File
    (`CopyPasteD.java:279-434`, `:631-671`); an empty selection keeps the old
    buffer (`:281-283`). `clearClipboard` (`:607-616`) has no Desktop caller,
    so the buffer survives File → New and Open.
  - Ctrl+C never writes the system clipboard. Ctrl+V pastes from the internal
    buffer and then tries an equation paste from the system clipboard
    (`desktop/main/GlobalKeyDispatcherD.java:262-296`), which is an
    external route.
  - An ordinary Desktop paste stores no undo point inside `pasteFromXML`; an
    identity-bearing paste stores exactly one (`CopyPasteD.java:555-558`).
  - `pasteFromXML(app, putdown)` is the only paste entry: `putdown = false` for
    the keyboard, the Edit menu, duplicate and the shared context menu
    (`desktop/main/GlobalKeyDispatcherD.java:270`,
    `desktop/gui/menubar/EditMenuD.java:285`, `CopyPasteD.java:598`, `:604`),
    `putdown = true` only for Insert File (`CopyPasteD.java:662`, `:670`).
  - There is no GeoCeDG override of copy or paste.
- **Probe `P-TWO-WINDOWS` (finding).** Two `AppGeoCeDG` instances on the
  `G9U1TestApp` host, driven through the real Ctrl+C/Ctrl+V dispatcher:
  - distinct `CopyPasteD` instances;
  - after a copy in window A, B's buffer is empty, and Ctrl+V in B adds no
    object and leaves B's construction XML unchanged;
  - a same-window paste adds the copy and stores no undo point within 2 s;
  - after File → New in A, its buffer is still filled and Ctrl+V pastes the
    copy into the new document.
  Hence no Desktop route pastes one window's buffer into another; the
  cross-document route is copy, New or Open, paste in the same window.
- **Contract.**
  - Provenance is a Desktop-only value (effective construction unit and metre
    factor, or `UNSPECIFIED_MODEL_UNIT`) held beside the window's buffer, by a
    GeoCeDG subclass of `CopyPasteD` that `AppGeoCeDG.getCopyPaste()` returns.
  - It is recorded only when the buffer is actually replaced, from the source
    app of that copy (for Insert File, the hidden app of the inserted
    document), and cleared by an override of `clearClipboard`. Never in
    `InternalClipboard`, clipboard XML or document state.
  - At the successful end of `pasteFromXML` (a protected no-op hook after
    `CopyPasteD.java:558`, receiving `putdown`, registered as an upstream
    modification), compare provenance with the target's effective units by
    metre factor (§5.4): same physical meaning → nothing; different → one
    non-blocking notice (`DQ-D1-9`); either side unspecified, or no provenance
    → nothing. A `putdown = true` paste is Insert File and follows `DQ-D1-7`.
  - The notice creates no undo point, changes no XML and tolerates being raised
    twice. Paste semantics, coordinates and the target unit never depend on
    provenance (§11).
  - The equation paste, text fields, the spreadsheet, drag and drop, scripting
    and API insertion carry no provenance and give no notice.

#### C10. Unit-independence invariant

- **Finding.** The geometry-only fingerprint
  `G9X1GeometryExportAdapter.constructionFingerprint`
  (`geocedg-common/export/G9X1GeometryExportAdapter.java:1049-1060`) serializes
  the spatial section and the construction elements only, so it excludes the
  unit element by construction; labels, order, `PersistentGeoId` and values are
  the other observables in use (C4).
- **Contract.** Focal tests compare, before and after every operation of §7.1
  and after its undo and redo, on a corpus with points, lines, conics,
  polygons, a dependent measurement, a Locus V2 with several branches, a
  Spline V2 and a slider-driven parameter: the construction-element XML, the
  construction order, labels and `PersistentGeoId`s, the numeric values (points,
  lengths, `LocusLength`, Spline V2 parameters), Locus V2 branch and component
  identity, parameter domains, and the DAG. Across a pure unit operation the
  same Java instances survive and no geo receives an update; across undo or
  redo the identities above are equal. No new code path reads units in an
  algorithm.

#### C11. Legacy and forward compatibility

- **Corpus (finding).** 69 tracked `.ggb`/`.cedg` documents and 2 `.ggt` files
  (`git ls-files`), including the canonical and regression models under
  `models/`.
- **Contract.** The mandatory corpus is: a legacy `.cedg` without metadata; a
  Classic `.ggb` input; a new physical-unit document; a `usm` document; a
  `usm`-only document (§8.2 last example); a malformed recognized version-1
  element for every §8.7 case; a future version; macro XML and a `.ggt`; an undo
  snapshot; a paste payload and an `evalXML` wrapper containing the element.
  Byte identity: before any product edit, base fingerprints of the construction
  XML of every corpus document that loads deterministically are captured on the
  unchanged base, reproduced on a `git archive` of the base, and committed as a
  test fixture; the candidate must reproduce them exactly and write no element
  for any of them (no migration on load, §16.1). Exclusions are listed with
  their reason.
- **Older readers.** The documented limitation (§8.9) is evidenced by
  `P-OLDER-READER` at preparation and by a base-tree probe of a candidate
  document in the candidate report. The user-guide wording of that limitation is
  `G`'s (D0 closeout finding 7); `D1` adds no UI warning for it (`DQ-D1-10`).

#### Contradictions with earlier documents

| Source | Statement | At the base | Effect |
|---|---|---|---|
| specification §9, row *File → New, new window, startup without a file* | evidence `AppD.java:1149-1177` for all three | New window and startup go through the `AppD` constructor (C1, C6) | citation drift only; the rule (reset, then §10 defaults) is kept; `D1` records it and does not edit the specification |
| specification §11 | provenance is "cleared with" the buffer (`CopyPasteD.java:607-616`) | that method has no Desktop caller; the buffer is only replaced | the contract clears provenance in the same method and replaces it with the buffer |
| specification §17.3 *two windows* | a cross-window route gives no notice | no cross-window buffer route exists (`P-TWO-WINDOWS`) | consistent; tested as a negative case |
| specification §11 | undoing the paste undoes only the paste | an ordinary Desktop paste stores no undo point in `pasteFromXML` | the notice is outside undo either way; paste undo behavior is not changed by `D1` |
| D0 unit-system inputs, lines 72 and 157 | old citations `AppD.java:354`, `:4988-4992`, `InternalClipboard.java:624-634` | `:355`, `:5036-5044`, `:626-632` | citation drift; no effect |
| `AGENTS.md` §3.2 | translations in `geocedg/resources/` | GeoCeDG Desktop texts live in the profile's `localized_text`; kernel texts in the upstream bundles | `D1` follows the live convention |

A contradiction that would change scope, owner, serialization or class stops
the phase.

### Author dispositions on `DQ-D1-1` to `DQ-D1-11`

The prepared prompt fixed a default contract for each requested decision. The
author disposed of all of them on 2026-10-03; the
[D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md)
is their authority and prevails over the prepared defaults. In substance:

| ID | Outcome | Contract |
|---|---|---|
| `DQ-D1-1` | accepted | one `document.units` action under `options-product` opens the single Document Units dialog (C7); the two permanent unit status segments are presentation-only and invoke that action on click (C8); the status bar has no editing authority |
| `DQ-D1-2` | accepted with precision | a GeoCeDG-owned *New document units* panel in Preferences → *Layout & Presentation* stores defaults only for future blank documents; changing them never alters the current document's `UnitState`, XML, undo stack or saved/dirty state; they reach a document only through the C6 lifecycle |
| `DQ-D1-3` | accepted | a recognized `geocedgUnits` element outside the document `<construction>` fails closed (its document semantics cannot be established); in a macro context it stays ignored with a text diagnostic (§8.8); misplaced metadata is never reinterpreted |
| `DQ-D1-4` | accepted | localized (`en`, `es`) user-visible diagnostics name the file and the defect: unsupported newer version, malformed element, invalid or missing `usm` factor, duplicate element, misplaced element; a failed File → Open leaves the previous live document and its saved, undo and file state unchanged |
| `DQ-D1-5` | **replaced** | `Construction.isStarted()` keeps its meaning (geometric or macro construction content exists). A narrow shared seam, `Construction.hasSaveRelevantContent()` or an equivalent justified name, is `constructionStarted OR persistentDocumentMetadataPresent`, where for `D1` `persistentDocumentMetadataPresent` includes `UnitState != EMPTY`; `App.isSaved()` uses it. An otherwise empty document whose unit state changes becomes unsaved and prompts on New and close; an untouched new document with applied defaults stays saved after the baseline is retaken; Classic documents without unit metadata keep the upstream saved-state behavior. Presentation and session state never become save-relevant content. If the separation is impossible without material widening, stop and report |
| `DQ-D1-6` | accepted | `geocedg.units.new-document-construction.v1` ∈ {`unspecified`, `mm`, `cm`, `m`} and `geocedg.units.new-document-presentation.v1` ∈ {`none`, `mm`, `cm`, `m`}; never `usm`; invalid stored values behave as unset and are not rewritten by reading |
| `DQ-D1-7` | accepted | Insert File carries the inserted document's provenance; different physical construction meanings show the same non-blocking notice as an ordinary cross-document paste; never rescale geometry, convert coordinates or modify target units; a later Ctrl+V keeps that provenance until the buffer is replaced or cleared |
| `DQ-D1-8` | accepted | the persisted factor and the dialog input use the §8.4 grammar with `.` as decimal separator; a decimal comma is rejected with a localized hint (the equivalent of "Use "." as the decimal separator."), never converted; no grouping separators; the canonical writer stays locale-independent |
| `DQ-D1-9` | accepted | a transient `paste-notice` segment of the GeoCeDG status bar names the source and target units; no serialization, undo, geometry, sound or modal dialog; it clears after about 10 s, on the next paste or on the next document transition; it is EDT-safe and distinct from the permanent `layer | construction-unit | presentation-unit` segments; its timer is injectable so that tests need no real 10-second sleep |
| `DQ-D1-10` | accepted | no immediate older-reader warning; the limitation is recorded in the candidate evidence and the author-smoke checklist; its user-guide treatment stays `G`'s |
| `DQ-D1-11` | accepted with a boundary correction | `geocedgUnits` is shared document semantics: the shared reader, writer, validation and fail-closed rules also apply to the Classic diagnostic application built from this tree. In a Classic session: no unit UI and no unit geometry behavior; an `EMPTY` document changes no byte; a valid unit-bearing document is read and preserved per `D0`; malformed or future unit metadata fails closed. Forbidden are only Classic-specific unit UI, geometry semantics, orchestration or feature expansion beyond that shared behavior. Web: no new frontend or product claim, shared code within its build constraints, no Web-specific unit UI or behavior; a Web semantic fork or a material frontend broadening stops the phase |
<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BRANCH_START =
3fb7542af3ef36db150291ca77e03b0bf4b6f888          (T_R6PLUS_D1_PROMPT, approved
                                                   preparation package; not published)

IMPLEMENTATION_BRANCH_START_TREE =
de71421e6adbd9a9b5e6acc8051dd5154f6a5f63

PUBLISHED_BASE =
3268f9b99d20c5d9cf62f25d8066ee0a8e47765c          (P_R6PLUS_D0, published D0 closeout;
                                                   tree 83b32b6cb579dc28162f990160c3eef0b1928be6)

IMPLEMENTATION_BRANCH =
phase/pre-g9b-r6-plus-d1-unit-system              (local; created from the exact
                                                   branch start; never rebased)

D0_STATE =
PASS — AUTHOR APPROVED — PUBLISHED
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal `P_R6PLUS_D0` and its tree; `T_R6PLUS_D1_PROMPT` exists
with its tree and has `P_R6PLUS_D0` as its only parent; the worktree is clean.
The phase works on its implementation branch from the exact branch start and is
not rebased onto any later commit without a new author instruction. The
prepared candidate stays immutable: this amendment is a new commit on top of
it. `T_R6PLUS_D1_PROMPT` differs from `P_R6PLUS_D0` only in documentation, so
the citations of this prompt still apply; the phase re-establishes every one it
relies on before using it.
## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source, tests, build and serialization at the base.
3. [ADR 0032](../../../docs/adr/0032-unit-system-semantics-and-persistence-ownership.md)
   and the [unit specification](../../../geocedg/specs/units/unit-system.md)
   v1.0; the [D0 closeout record](../../../docs/validation/pre_g9b_r6_plus_d0_closeout_record.md);
   the `D0` author-decision records.
4. The [D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md)
5. Evidence, re-established before use: the
   [D1 characterization report](../../../docs/validation/pre_g9b_r6_plus_d1_preparation_characterization_report.md)
   and its mirror; the [A-1](../../../docs/validation/pre_g9b_r6_plus_a1_closeout_record.md)
   and [B](../../../docs/validation/pre_g9b_r6_plus_b_closeout_record.md)
   closeout records.
6. Generated artifacts, earlier reports and previous agent output are evidence,
   not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Shared kernel-document layer

- A new `org.geocedg.common` unit package: the immutable unit-state value, the
  state holder with its validated operations and listener notification, the
  effective-unit function, conversions, physical-meaning equality, the
  canonical factor writer and lexical reader, the XML writer of the element, and
  the structured unit diagnostic and its unchecked exception.
- `Construction` (minimal): the holder field and getter, reset in
  `clearConstruction`, the writer call in `getConstructionXML` with its macro
  guard, a non-consuming read of the pending load purpose, and the
  save-relevant-content seam of `DQ-D1-5` (`hasSaveRelevantContent()` or an
  equivalent justified name); `isStarted()` is not changed.
- `App` (minimal): `isSaved()` through the save-relevant-content seam, and the
  narrowest error-message seam for a rejected `setXML` (`DQ-D1-4`), each
  upstream-identical for a document without unit metadata.
- `MyXMLHandler` (minimal): the `geocedgUnits` branch, its construction mode,
  the effective-purpose capture at document-construction start, duplicate,
  child, text and misplacement detection, and the ignored contexts.
- `MyXMLio` (minimal): the additional restore trigger for a unit rejection.
- Kernel localization keys for the defect messages, if the message is raised
  from shared code (`menu*.properties` are mixed-EOL; new lines with LF).

### Desktop GeoCeDG layer

- `AppGeoCeDG`: the `fileNew` override and the blank-startup application of
  defaults with the baseline re-take, the reset and `openURL` handling of C6,
  the `getCopyPaste()` override, the dialog and preference wiring, and the
  error-message path of `DQ-D1-4`.
- The protected no-op hooks in `AppD` (blank startup) and `CopyPasteD`
  (successful paste), and the narrowest preflight error seam; each
  upstream-identical by default and registered.
- A GeoCeDG `CopyPasteD` subclass holding provenance.
- The Document Units dialog with an injectable prompt, the new-document units
  preference panel, and a units preference store on the
  `GeoCeDGPresentationPreferences.Store` model with an injectable store.
- `GeoCeDGStatusBar`: the two unit segments, separators, the transient notice
  segment and their refresh.
- The `document.units` profile action, its registry target, its five localized
  texts in `en` and `es`, and the action-count pins (`124` → `125`) in
  production and tests, changed together and only by this action.

### Supporting changes

- Tests for every obligation below, the base-fingerprint fixture and the fixture
  documents, all registered in `docs/upstream/modified-files.yml` where they
  live under `source/`.
- Registration of every modified upstream file in
  `docs/upstream/modified-files.yml` with the narrowest rationale.
- The JUnit inventory and registry pin through the official updater with
  executed selection evidence; no new phase selection is registered.
- Living documentation of the delivered behavior: the bilingual user guide
  (structure rules of `documentation-maintenance.md` §10), the developer guide,
  and the profile and UI specifications the action touches; the candidate report
  and its machine-readable evidence.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- Any unit serialization outside the `D0`-approved element: no `<kernel>`,
  `<construction>` attribute, `<euclidianView>`, `<geocedgSpatial>`,
  preferences XML, macro XML, `.ggt` or clipboard content (§16.2).
- Any change of coordinates, numeric geometry, dependencies, construction
  order, identities, Locus V2 or Spline V2 semantics or parameter domains
  caused by a unit operation, the application of a default, or the unit state
  of a paste source or a loaded document (§6.1); any read of units in
  `compute()`; any geometric scaling during paste.
- `drawingScale` in any form (holder, UI, serialization) — `C`.
- Any exporter, export specification, G9X1 specification, verifier pin or
  sidecar change: DXF `$INSUNITS`, the neutral export model unit, picture
  physical sizing, LaTeX `xunit` and "Scale in cm" stay exactly as at the base
  (§15.1) — `C`.
- Native dimensions and any dimensional presentation of `Distance`, `Length`,
  `LocusLength` or `GeoNumeric` (§14) — `E2`; `IsoABorder` — `E3`.
- Layer-domain widening or layer persistence — `A-2`; the GGT library — `E1`;
  the orientation cue — `F1`; authoring memory — `F2`.
- A registry of several custom units, unit libraries, unit conversion of
  geometry, area or volume units, and synchronization with the axis
  `unitLabel` (§16.3).
- A new command, command processor change or GGBScript surface: the derived
  GGBScript inventory stays unchanged.
- Classic-specific unit UI, geometry semantics, orchestration or feature
  expansion beyond the shared document-semantic reader/writer behavior required
  by `D0` (`DQ-D1-11`). The shared reader, writer, validation and fail-closed
  rules do apply to the Classic diagnostic application: a valid unit-bearing
  document is read and preserved, malformed or future metadata fails closed,
  and an `EMPTY` document changes no byte.
- Web-specific unit UI or behavior, a Web semantic fork, or a new Web frontend
  or product claim; shared code stays within its build constraints.
- Changes to the meaning of `Construction.isStarted()`, or any presentation or
  session state treated as save-relevant document content (`DQ-D1-5`).
- Changes to the meaning of the `A-1` layer workspace or the `B` export area.
- Edits of the normative `D0` documents (ADR 0032, the unit specification), of
  the governance layer, of `AGENTS.md`, `CLAUDE.md`, `.github/prompts/**`
  other than this prompt's authorized amendment, `ai-shell/prompts/**`, the
  verifier, its schemas or `prompt-contracts.json`, beyond inventory and
  registry data updated through existing mechanisms.
- Any commit, move, rename, rewrite or copy of `artifacts/author-input/**`.

## Architectural placement

- Unit state and everything that defines its meaning, persistence and undo:
  shared kernel-document layer (*Architecture decision* above). It is document
  metadata, not geometry (`AGENTS.md` §4; ADR 0032 decision 1).
- Defaults, configuration UI, status, provenance and notice: Desktop GeoCeDG
  application layer; never authoritative, never serialized.
- Each upstream seam is the narrowest that serves the behavior, upstream-identical
  by default, and recorded in `docs/upstream/modified-files.yml`. If an owner is
  wrong at the base, stop and report instead of choosing another layer.

## Required design/specification

The unit specification v1.0 and ADR 0032 are the design; this prompt's
characterization and the `DQ-D1` dispositions complete it at the implementation
level. Before product edits, the phase records in its candidate report the
re-established seams, the chosen class and package names, the exception and
diagnostic shape, the hooks and their defaults, and every correction to C1–C11.
A correction that would change scope, owner, serialization or class stops the
phase. `D1` does not amend the specification; citation drift is reported.

## Geometric invariants and degeneracies

`D1` changes no geometric definition, algorithm, dependency or numeric value
(C10). Unit cases with required behavior:

| Case | Required behavior |
|---|---|
| `EMPTY` state | no element; every route byte-identical to the base |
| `c` absent, `usm` `DEFINED` | element with factor only (§8.2, §8.3); `UNSPECIFIED_MODEL_UNIT` |
| `p` without `c` | impossible by operation; fail closed on read (§5.1, §8.7) |
| `c = usm` or `p = usm` without a factor | impossible by operation; fail closed on read |
| `k` = `MIN_VALUE`, `MAX_VALUE`, subnormal | valid; canonical form per C5 |
| `k` zero, negative, NaN, infinite, unparseable | rejected; previous state kept (§4.2) |
| `usm` with `k = fb(mm)` versus `mm` | same physical meaning; no paste notice (§5.4) |
| two `usm` with different `k` | different physical meaning; notice |
| unspecified source or target | never a notice; never a conversion |
| conversion overflow | presentation failure state, never an exception (§5.3); not consumed in `D1` beyond the status tooltip |
| name or symbol with 64 scalar values, with 65, with a supplementary character, with a control character, with surrounding space | accepted, rejected, accepted, rejected, rejected (§4.4) |

## Compatibility and serialization

The serialization change is exactly the `<geocedgUnits>` element of §8, written
only for non-`EMPTY` state. Legacy `.cedg` and Classic `.ggb` documents load as
`EMPTY` and re-save byte-identically; no migration and no feature flag exist.
Documents with units opened by an older GeoCeDG build or Classic lose the
element on re-save (§8.9), an accepted limitation recorded in the candidate
report. A build implementing this prompt fails closed on recognized invalid or
newer unit metadata (§8.7). Preferences XML, macros, `.ggt` and clipboard XML
never contain unit state. GeoCeDG still saves only `.cedg`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation. Each runs canonically in `final.shared`
or `final.desktop`:

| ID | Obligation |
|---|---|
| `T-STATE` | value type, constraints (§5.1), effective-unit function for every combination (§5.2), conversions and overflow (§5.3), equality including `usm` versus built-in and unspecified versus itself (§5.4) |
| `T-USM-VALIDATION` | §4.2 and §4.4 accept/reject cases; old state preserved on every failed edit; undefined `usm` not selectable; remove refused while referenced |
| `T-CANONICAL-FACTOR` | the fixed reference table of the report reproduced byte for byte by the writer under several default locales; the seeded round-trip properties; non-canonical valid input (for example `0.02540`) read to the same binary64 and written canonically; no `Double.toString` path (C5) |
| `T-LEXICAL` | §8.4 reader grammar: accepted and rejected forms, including sign, white space, hexadecimal, `NaN`, `Infinity`, type suffix, decimal comma, empty, missing digits, bare exponent |
| `T-XML-WRITER` | every §8.2 case, attribute order, escaping, position after `worksheetText` and before `geocedgSpatial` and the elements, own line; omission rule (§8.3) including the `DQ-D0-3` and `DQ-D0-4` states; never for a macro construction |
| `T-XML-READER` | round trip of every writer case; any child position accepted; effective-purpose capture independent of position (C1) |
| `T-FAIL-CLOSED` | every §8.7 case and `DQ-D1-3`: dedicated exception, structured diagnostic, no partially applied state |
| `T-LOAD-RESTORE` | File → Open of every fail-closed fixture as `.cedg`, `.ggb` and an archive with macros: preflight rejection; document XML, unit state, unique id, file, path, recent list, `isSaved`, undo history and file bytes unchanged; message per `DQ-D1-4` in `en` and `es`; an injected live-load failure; a failed startup load; `setXML` restored to its entry snapshot (C3) |
| `T-IGNORED` | element in a paste payload, `evalXML`, action replay, document macros, `.ggt`, `addMacroXML`, `MacroConstruction.loadXML`: ignored, text log, target state unchanged; macro, `.ggt`, clipboard and preferences XML never contain it |
| `T-ROUTES` | every row of the C1 lifecycle map, including the archive-with-macros row |
| `T-UNDO` | each §7.1 and §4.3 operation exactly one undo point (Desktop latch); no-op and rejected operations none (2-second latch); combined define-and-select one; undo and redo restore the exact state; operations mark the document modified |
| `T-REBUILD` | redefine rebuild, batch redefine, failed-redefine restore, atomic-mutation rollback, paste rollback and rejected-parse restore carry the exact state |
| `T-NEW-OPEN` | defaults on File → New, startup without a file, New Window, failed startup load, reset without a file, `openURL` clear; never on Open, legacy Open, reset reload, undo, redo, rebuild, rollback, paste, Insert File, `setXML`, `evalXML`; baseline includes the defaults; no undo point and saved after applying them; unspecified default plus presentation default gives `EMPTY`; Open never carries the previous state; a preference change changes no document |
| `T-PREFS` | keys and values of C6 through an injected store; invalid stored values unset and not written back; no unit state in preferences XML or documents; a preference change leaves the current `UnitState`, XML, undo stack and saved/dirty state unchanged (`DQ-D1-2`); the real developer store never touched |
| `T-SAVED` | `DQ-D1-5`: `isStarted()` unchanged for every case; `hasSaveRelevantContent()` true exactly for a started construction or a non-`EMPTY` unit state; a unit change on an empty document makes `isSaved()` false and New prompts; an untouched new document with applied defaults stays saved; an `EMPTY` Classic document keeps the upstream behavior; no presentation or session state (layers, export area, provenance, notice) is save-relevant |
| `T-USM-LIFECYCLE` | the lifecycle cases of C7 |
| `T-INVARIANCE` | C10 on its corpus, for every operation, undo and redo |
| `T-NOTIFY` | the notification fires on every operation, reset and application without any construction recompute or geo update |
| `T-STATUS` | segment order and texts in `en` and `es`, unspecified and `usm` displays, tooltips, refresh on every transition and after layout rebuilds, EDT marshalling, a click on a unit segment invoking `document.units`; the `A-1` layer texts unchanged; the `paste-notice` lifecycle (shown, replaced on the next paste, cleared on a document transition and on expiry) through an injected timer, without real sleeps (`DQ-D1-9`) |
| `T-UI` | the Document Units dialog through an injected prompt: every field, validation warnings with `JOptionPane` mocked, OK as one undo point, Cancel as none |
| `T-CLIPBOARD` | provenance recorded only on buffer replacement; same meaning no notice; copy, New with a different default, paste: notice; copy, Open, paste across `mm`, `cm`, `m`, `usm` with equal and different factors; unspecified sides; Insert File with a different physical meaning gives the same notice and a later Ctrl+V keeps its provenance (`DQ-D1-7`); numbers and target units unchanged; no undo point and no XML change by the notice; two windows: distinct buffers and no cross-window paste; the equation paste and API routes give no notice |
| `T-LEGACY-BYTES` | the committed base-fingerprint fixture reproduced for the corpus (C11) |
| `T-COMPAT-CORPUS` | the mandatory corpus of C11 |
| `T-CLASSIC` | a Classic headless app (`AppDNoGui` with the default config): an `EMPTY` document is byte-identical; a valid unit-bearing document is read and re-written with the same element; malformed and future metadata fail closed; no unit UI exists (`DQ-D1-11`) |
| `T-EXPORT-REGRESSION` | with each unit state, picture export sizes, DXF `$INSUNITS = 0`, the `UNITLESS` fidelity contract and LaTeX output are byte-identical to the `EMPTY` state |
| `T-GGBSCRIPT` | the derived GGBScript inventory and its pin unchanged |
| `T-DETERMINISM` | writing the same state twice, and under different default locales, yields identical bytes |
| `T-SMOKE` | an explicit author-smoke checklist; the agent does not perform author smoke |

Harness rules: Desktop tests mock `JOptionPane`, inject the spatial-redefine
presentation and the dialog prompt, use an injected preference store, wait for
the asynchronous undo store with latches, start in Move mode, never fire
`Ctrl+Shift+C`, `Ctrl+Shift+M` or `Ctrl+Shift+B`, and never open a real modal
dialog (a New step in a test sets the document saved first). Shared tests log
diagnostics as text.

Adjacent regressions before freezing (development evidence, not acceptance):
`GeoCeDGDocumentLifecycleTest`, `G9U1MacroNativeArchivePersistenceTest`,
`G9U1UserToolLibraryTest`, `G9A1SpatialIdentityXmlTest`,
`G9A1SpatialIdentityLifecycleTest`, `G9A3SpatialSnapshotRecoveryTest`,
`PostG9U1A4ConstructionPositionCharacterizationTest`,
`PreG9bR3C1DesktopClipboardTest`, `PreG9bR3C1U1DesktopClipboardTest`,
`PreG9bR3C1ClipboardCorrectnessTest`, `CopyPasteDTest`, `RedefineTest`,
`PreG9BR6PlusA1LayerWorkspaceTest`, `PreG9BR6PlusA1HiddenLayerTest`,
`PreG9BR6PlusBExportSurfaceTest`, `PreG9BR6PlusBPictureFidelityTest`,
`GeometryExportFoundationTest`, `G9X1G5CorpusCompatibilityTest`,
`PreG9BR6CapabilityMatrixTest`, `G9U1ActionRegistryTest`,
`G9U1ProfileCompilerTest`, `G9U1WorkspaceSurfaceTest`, `GeoCeDGProfileTest`,
`PreG9BS4PresentationSizingTest`, `PreG9BP0PresentationThemeTest`,
`PostG9U1A7NavigationTest`, `G9U0PersistenceCompatibilityTest`; Checkstyle
`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`,
`:desktop:desktop:checkstyleMain` and `:desktop:desktop:checkstyleTest` (read the
XML reports; ASCII escapes in non-test sources); `Assert-GeoCeDGUpstreamBoundary
-ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522`; `git diff --check`.

Catalog: discovery dry-run evidence for both modules and **executed**
`final.shared` and `final.desktop` selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand. If any registry-shape pin changes,
run a development `INFRA_UNIT` on the staged tree before freezing.

Acceptance (the frozen `GLOBAL_IMPACT` contract), on one clean immutable
committed candidate:

```text
tools/agent/verify.ps1 -Profile FINAL -PlanOnly                (plan resolution only)
tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\<fresh-name>
```

```text
FINAL -LogDirectory : artifacts\agent\<fresh-name> inside the repository, not
                      pre-existing (packaging.repository-safety requires it)
console log         : the session scratchpad, never under artifacts\
```

Exactly one `FINAL`, `ACCEPTED / COMPLETE`, with its receipt bound to the exact
candidate commit and tree; no further commit afterwards. If product or test
code changes after `FINAL`, that candidate is no longer the accepted candidate:
stop and report instead of claiming the old receipt. A focal, adjacent,
`DEV` or `INTEGRATION` PASS never substitutes for it, and it is not repeated for
an unchanged candidate. The two standing diagnostics
(`diagnostic.governance` `DIAGNOSTIC_FINDING`, `diagnostic.historical-consistency`
`DIAGNOSTIC_UNAVAILABLE`) are pre-existing and do not affect acceptance. A
failure proven to predate the candidate and lie outside its delta is retained
baseline debt per the task template, not phase scope. Report exact commands,
exit codes, run ids, plan, result and receipt hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's instruction of 2026-10-03, recorded in the
[D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md),
authorizes only the scope above: local implementation on the implementation
branch, local commits, technical verification, the single `FINAL` and one
frozen technical candidate for author review and smoke. It does not authorize
self-approval, author smoke by the agent, or any change of the frozen class.

`D1` authorizes nothing that follows it. The operational order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`; it is the order the
author chose. `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` and
`G9B` stay unauthorized. Author approval is never created by technical
verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local implementation branch from the exact branch start and local commits
only. Push, branch publication, merge, promotion to `main`, rebase, squash,
amend after freeze, force push, tag, release and binary publication are
forbidden; each needs a separate explicit author instruction naming the exact
candidate SHA. Acceptance evidence never grants publication authority.

## Acceptance and closeout

`D1` stops with one technically verified candidate pending author review and
author smoke: `PRE-G9B-R6-plus-D1 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`,
with `selfApproved = false`. Author approval is an explicit decision naming the exact accepted
commit. The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. After approval,
`tools/agent/phase-closeout.ps1 -Action INSPECT` may confirm only the receipt
and candidate identities. Closeout and publication are separately authorized
documentary steps.

## Required artifacts

- The implementation, tests, fixtures and registrations above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-established seams and every correction to C1–C11; the chosen names, hooks
  and defaults; the base-fingerprint capture and its `git archive`
  reproduction; the obligation-to-test map; the canonical factor table and
  sweep result on the test runtime; the route table with evidence; the
  fail-closed matrix with the restore observations; the two-window and
  cross-document clipboard results; the older-reader base-tree evidence; the
  export-regression evidence; residual risks and observations; the
  author-smoke checklist.
- The author-smoke checklist covers at least: new-document defaults; the
  Document Units dialog; standard-unit changes; define, edit, switch and remove
  `usm`; status segments and tooltips; undo and redo; the unsaved prompt on an
  otherwise empty document after a unit change; Open of valid unit-bearing
  documents; rejection of malformed or newer metadata; paste after New/Open
  with matching and mismatching units; the Insert File mismatch notice; legacy
  document behavior; Classic diagnostic preservation; confirmation that the
  current picture, DXF and LaTeX export behavior has not changed; and the
  older-reader limitation (`DQ-D1-10`).
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale, the
  verification-infrastructure-impact assessment, `GUIDE_IMPACT` with paths,
  `SERIALIZATION CHANGE = <geocedgUnits> element per unit-system v1.0 §8`, and
  the exact `FINAL` commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- correctness requires serializing units outside the `D0`-approved
  construction element;
- correctness requires changing document geometry or coordinates on a unit
  change;
- units would become live dependencies of general algorithms;
- `drawingScale` would be embedded in document serialization;
- a `C`-owned exporter, specification or verifier would change;
- `geocedgSpatial` would be used as unit storage;
- malformed or future recognized unit metadata would be accepted silently;
- paste would scale geometry;
- a general multi-custom-unit registry would be introduced;
- satisfying `DQ-D1-11` would require frontend-specific Classic or Web unit
  semantics instead of shared document semantics, a Web semantic fork, or a
  material broadening of frontend scope;
- implementing `DQ-D1-5` would require materially redefining general save
  semantics beyond a narrow document-metadata seam, or changing the meaning of
  `Construction.isStarted()`;
- the normative `D0` contract would have to change;
- fail-closed unit parsing would need a material broadening of the load
  architecture (C3);
- a route of the C1 map cannot keep the rules of §8 and §9;
- the canonical factor cannot be produced deterministically on JDK 17 and 25;
- the base-fingerprint fixture cannot be reproduced on the unchanged base;
- the work would belong to `C`, `E2`, `E3`, `A-2` or later;
- current governance requires a verification class other than
  `GLOBAL_IMPACT`;
- the `FINAL` is rejected for a cause attributable to the candidate, or its
  coverage is incomplete or untrusted;
- product or test code would change after `FINAL`.

# PRE-G9B-R6-plus-D1 — unit-system implementation and status integration: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN (revision 2: author-smoke remediation, §20)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PREVIOUS_CANDIDATE        = b55aa8173045d6d6bf735e70eb22302979b2b26a
                            tree b07fad69c73f226ed1922465ac9c358ce5979a6a
                            FINAL verification-c9c0501a068a40738a3a9333b27dcb50
                            ACCEPTED / COMPLETE (historical evidence for that
                            candidate only; superseded for acceptance)

PHASE                     = PRE-G9B-R6-plus-D1
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = GLOBAL_IMPACT
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile FINAL -PlanOnly   (plan resolution only)
                            tools/agent/verify.ps1 -Profile FINAL -LogDirectory artifacts\agent\<fresh-name>
                            (exactly one FINAL, on the exact candidate commit and tree)
PRODUCT CHANGE            = YES (shared document unit state, persistence and
                            fail-closed parsing; Desktop Document units action,
                            dialog, status segments, new-document defaults and
                            paste notice)
SERIALIZATION CHANGE      = <geocedgUnits> element per unit-system v1.0 §8
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
[`pre-g9b-r6-plus-d1-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-candidate-evidence.json).
The normative contract is the
[unit-system specification](../../geocedg/specs/units/unit-system.md) v1.0 and
[ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md); this
report restates none of it.

## 1. Authorization and entry identity

The author approved the documentary preparation package and authorized
`PRE-G9B-R6-plus-D1` for implementation and technical verification on
2026-10-03, exclusively for the scope of the canonical prompt and of the
[preparation closeout and authorization record](pre_g9b_r6_plus_d1_prompt_closeout_record.md),
whose `DQ-D1-1` to `DQ-D1-11` dispositions prevail over the prompt's defaults.

| Identity | Value |
|---|---|
| prepared candidate `T_R6PLUS_D1_PROMPT` (implementation branch start, immutable) | `3fb7542af3ef36db150291ca77e03b0bf4b6f888` |
| its tree | `de71421e6adbd9a9b5e6acc8051dd5154f6a5f63` |
| its parent `P_R6PLUS_D0` = local `main` = `origin/main` at entry | `3268f9b99d20c5d9cf62f25d8066ee0a8e47765c` |
| implementation branch (local, never pushed; not identity) | `phase/pre-g9b-r6-plus-d1-unit-system` |
| preparation branch (kept, unchanged) | `phase/pre-g9b-r6-plus-d1-prompt` at `3fb7542a` |

The implementation branch was created from the exact prepared candidate and was
never rebased. The prepared candidate was neither modified nor amended.

## 2. Canonical prompt amendment

One tracked edit of
`.github/prompts/tasks/pre-g9b-r6-plus-d1-unit-system-implementation-and-status.prompt.md`,
in its own local commit before any product change:

| Commit | Tree | Content | Prompt blob |
|---|---|---|---|
| `3fb7542a` | `de71421e` | prepared prompt, `PREPARED — NOT AUTHORIZED` | `96f6e612cce1c43e2043abafe17ead3e897f4ef8` |
| `a246d74d1999a0018899c40157e939845babe91e` | `eae86c2b2d791d979bb761a69f1497e4c5ec07eb` | `AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL VERIFICATION ONLY`; the `DQ-D1-1`–`DQ-D1-11` dispositions; the implementation base; the corrected `DQ-D1-5` and `DQ-D1-11` boundaries; `GLOBAL_IMPACT` / `FINAL` frozen; one and only one `FINAL`; the author-smoke list; the added stop conditions | `008c397c55d34401dda645969281f2eef76d3937` |

The same commit adds the closeout record and its JSON mirror. `selfApproved`,
`authorApproved` and `passClaimed` stay `false` throughout.

## 3. What changed

### 3.1 Shared state owner and API

New package `org.geocedg.common.kernel.units` in `source/shared/common`:

| Type | Role |
|---|---|
| `UnitToken` | `mm`, `cm`, `m`, `usm`; case-sensitive tokens; built-in factors `0.001`, `0.01`, `1` |
| `UsmDefinition` | immutable factor, optional name and symbol; label rules of §4.4 |
| `UnitState` | immutable `{c, p, usm}` with `EMPTY`; constraints of §5.1, effective units (§5.2), conversions (§5.3), physical meaning (§5.4) and the §7.1 operations as pure `with…` methods |
| `UnitQuantity` | conversion result with `FINITE`, `UNSPECIFIED` and `NOT_FINITE` |
| `DocumentUnitSystem` | the per-construction owner: `getState`, `hasPersistentMetadata`, `replace`, `applyLoaded`, `reset`, listeners; a macro construction's owner stays `EMPTY` and refuses replacement |
| `UnitDocumentOperations.commit(app, next)` | the only user operation: replace, then one undo point and the modified flag only when the state changed |
| `UnitStateXml` | version-1 writer and validating attribute reader |
| `UnitMetadataException` | the five fail-closed codes |
| `CanonicalBinary64` | canonical factor writer and §8.4 lexical reader |

`Construction` creates one `final DocumentUnitSystem` in its constructor
(`getUnitSystem()`), resets it in `clearConstruction` after the worksheet text,
and writes it after `worksheetText` in `getConstructionXML` for a non-macro
construction. Nothing in the kernel reads the state: a unit operation
recomputes, updates and repaints no geo.

### 3.2 XML reader, writer and lifecycle

- Writer: nothing for `EMPTY` (lazy, §8.3); otherwise one own-line
  `<geocedgUnits version="1" …/>` with the attributes in §8.2 order, written by
  `XMLStringBuilder` escaping; the full document XML and the undo snapshot share
  `getConstructionXML`; macro, tool, clipboard and preferences XML never carry
  it.
- Reader: `MyXMLHandler.beginUnitLoad()` fixes the effective purpose once at the
  start of every parse (`MyXMLio.doParseXML`), from the construction's pending
  purpose (read through the new non-consuming
  `Construction.peekSpatialIdentityLoadPurpose()`) or else the parser default.
  The element applies for `NATIVE_OR_UNDO_RESTORE`, `REDEFINE_REBUILD`,
  `ORDINARY_EDIT_REBUILD` and `ROLLBACK_RESTORE` on a non-`MacroKernel`, at any
  child position of the document `<construction>`; it is ignored with a text
  log for `CLIPBOARD_IMPORT`, `GENERIC_MERGE` and every macro parse. An archive
  with macros parses `geogebra.xml` with `clearConstruction = false` under the
  forced `NATIVE_OR_UNDO_RESTORE` and therefore applies it (C1).
- A bare `<geocedgUnits version="1"/>` is grammatical and denotes `EMPTY`.

### 3.3 Fail-closed behavior

The reader checks the version first (`UNSUPPORTED_VERSION` above 1;
`MALFORMED_ELEMENT` when missing, non-integer or below 1), then unknown
attributes, tokens, `presentation` without `construction`, the factor
(`INVALID_USM_FACTOR`), the labels, name or symbol without a factor and a
selected `usm` without a factor (`INVALID_USM_FACTOR`); a second element is
`DUPLICATE_ELEMENT`, an element outside the document construction in an
applying context is `MISPLACED_ELEMENT`, and text or a child inside the element
is `MALFORMED_ELEMENT`. `MyXMLio` rethrows `UnitMetadataException` and treats
it like a rejected spatial parse, restoring the complete pre-parse
construction. In ignored contexts (`DQ-D1-3`) a misplaced or malformed element
is ignored with a text diagnostic. The Desktop preflight rejects the archive
before the live document is touched; the localized message per code
(`DQ-D1-4`, `en` and `es`) comes through the new
`AppD.showDocumentLoadFailure` and `App.showXMLLoadFailure` seams, whose host
defaults keep the upstream message.

### 3.4 Saved-state seam (`DQ-D1-5`)

`Construction.hasSaveRelevantContent()` returns
`isStarted() || unitSystem.hasPersistentMetadata()`; `App.isSaved()` consults it
instead of `isStarted()`. `isStarted()` is unchanged. No presentation or session
state (layers, export area, clipboard provenance, paste notice) is
save-relevant. The seam is two lines; the `DQ-D1-5` stop did not trigger.

### 3.5 Canonical factor

`CanonicalBinary64.write(double)` implements the Java SE 19 shortest
representation with exact `BigDecimal` arithmetic: the rounding interval from
the binary64 neighbours (inclusive when the significand is even), the minimal
digit count, the length-1-or-2 exception, the closest candidate with ties to an
even last digit, and the plain layout for `10^-3 ≤ |v| < 10^7`, scientific
otherwise. `Double.toString`, `String.valueOf(double)`, implicit concatenation
and locale formatters are never used. `isLexical` scans the §8.4 grammar
manually (`.` only, no sign, white space, hexadecimal, `NaN`, `Infinity`, type
suffix or grouping).

### 3.6 Desktop: action, dialog and `usm` lifecycle

One profile action `document.units` (`DQ-D1-1`): kind `product-action`, target
`geocedg.document.units`, a new `document-metadata` effect profile, maturity
`experimental`, listed in `options-product` (Options → GeoCeDG options →
Document units…) and as the settings action of the `document-lifecycle`
cluster; the catalog grows from 124 to 125 actions. `AppGeoCeDG.editDocumentUnits`
asks the injectable `GeoCeDGDocumentUnitsPrompt` (default: one Swing confirm
dialog), validates the request in `GeoCeDGDocumentUnits.validate(request,
current)` and either shows one localized warning and changes nothing or commits
once. Refusals: decimal comma (`DQ-D1-8`), invalid factor or grouping, invalid
label, presentation without construction, `usm` selected while undefined,
removal of a referenced `usm`. Cancel and an unchanged request store nothing.
39 localized texts in `en` and `es` live in the profile.

### 3.7 Preferences, New and Open

`GeoCeDGUnitPreferences` owns the two keys
`geocedg.units.new-document-construction.v1` (`unspecified`, `mm`, `cm`, `m`)
and `geocedg.units.new-document-presentation.v1` (`none`, `mm`, `cm`, `m`);
`usm` is never a value and an invalid stored value behaves as unset and is not
rewritten (`DQ-D1-6`). `GeoCeDGNewDocumentUnitsPanel`, hosted by
`OptionsLayoutD` through a null-returning `AppD` hook, saves the preference
only (`DQ-D1-2`). Defaults are applied to new blank documents only: startup and
New Window without a file (`AppD.recordStartupDocument`, called after the blank
startup preferences), File → New (after the host has re-applied the preferences
XML, itself a clearing load) and `AppD.reset`/`openURL` without a file; the
undo baseline is re-taken with `prepareUndoBaseline`/`commitUndoBaseline` and
the document is left saved. Open, legacy Open, reset with a file, undo, redo,
rebuild, rollback, paste, Insert File, `setXML` and `evalXML` never apply them.

### 3.8 Status integration

`GeoCeDGStatusBar` shows `layer | construction-unit | presentation-unit` and a
hidden-until-used `paste-notice` segment, separated by named `|` labels. The two
unit segments are presentation only and invoke the same action on click; their
tooltips distinguish an explicit presentation unit from one that follows the
construction unit and give the `usm` factor. Refresh is marshalled to the EDT
on every unit-state notification, including reset, loads, undo and redo. The
notice (`DQ-D1-9`) shows for `PASTE_NOTICE_MILLIS = 10 000` through an
injectable timer, is replaced by the next notice and cleared by the next paste
and by every document transition.

### 3.9 Clipboard and Insert File provenance (`DQ-D1-7`)

`GeoCeDGCopyPaste` (the product's per-window `CopyPasteD`) records the source
`UnitState` only when `copyToXML` replaces the buffer and clears it with the
buffer. The new no-op hook `CopyPasteD.onPasteCompleted(app, putdown)` runs once
after each successful internal-buffer paste; the product compares the physical
meanings (§5.4) and shows the notice only when both sides are physical and
differ. Insert File fills the buffer from the hidden source application, so it
carries that document's provenance and shows the same notice; a later Ctrl+V of
the same buffer keeps it. Numbers and the target units never change; the notice
is no undo point, no modification and no document state.

### 3.10 Classic diagnostic behavior (`DQ-D1-11`)

The reader, writer, validation and fail-closed rules are shared and therefore
apply unchanged to the Classic diagnostic application; it has no unit UI, no
Classic-specific semantics and no geometry or orchestration change. No Web
claim is made and no Web-specific code exists.

## 4. Re-established seams and corrections to C1–C11

Every characterization line cited by the prompt was re-read on the branch
start before use. The seams held, with these corrections:

| Item | Correction |
|---|---|
| C1 effective purpose | captured at the start of the parse (`MyXMLio.doParseXML` → `beginUnitLoad`) rather than when `<construction>` starts; equivalent, because the pending purpose is set before the parse and is never consumed by the capture |
| C1 Insert File row | the target receives `CLIPBOARD_IMPORT` through `CopyPasteD.insertFrom` → `pasteFromXML(app, true)`; the notice hook sits at the end of `pasteFromXML` |
| C2 position | `getConstructionXML` writes the element after `worksheetText` and before `geocedgSpatial` and the elements, as planned |
| C3 restore | the rejected-parse restore needed only the additional `UnitMetadataException` predicate; no material broadening of the load architecture |
| C6 startup | the hook is a field set by the `AppD` constructor (no initializer in `AppGeoCeDG`, which runs later); New Window uses the same constructor path |
| C9 provenance clearing | `CopyPasteD.clearClipboard` has no Desktop caller; provenance is cleared in the override and replaced with the buffer, as the contract states |
| C11 corpus | 68 tracked `.ggb`/`.cedg` documents outside `source/web`; one exclusion (§8) |

No contradiction changed scope, owner, serialization or class.

## 5. Route table

| Route (C1 map) | Evidence |
|---|---|
| save `.cedg`, full XML | `UnitXmlTest.writerEmitsEveryGrammarCaseInTheSpecifiedOrder`, `…EscapesNamesAndPlacesTheElement…`; `UnitLoadTest.unitBearingDocumentsRoundTripThroughSaveAndOpen` |
| undo snapshot | `UnitXmlTest.undoSnapshotCarriesTheElement`, `…eachOperationIsOneUndoPoint…` |
| File → Open `.cedg`/`.ggb` | `UnitLoadTest.unitBearingDocuments…`, `…recognizedDefectsFailClosed…` |
| archive with macros | `UnitLoadTest.anArchiveWithMacrosKeepsItsDocumentUnits`; fail-closed archive 5 of §6 |
| startup with a file, failed startup load | `UnitLoadTest.startupWithADocumentKeepsItsUnits…`, `…aFailedStartupLoadEndsBlank…` |
| `setXML` | `UnitLoadTest.aRejectedSetXmlRestores…`; `DocumentUnitsTest.defaultsApply…` (no defaults) |
| undo, redo | `UnitXmlTest.eachOperation…`; `DocumentUnitsTest.eachUnitOperation…`, `statusFollowsUndoAndRedo` |
| redefine rebuild, failed redefine, atomic rollback, paste rollback, construction `processXML` | `UnitXmlTest.redefineAndRollbackRoutesCarryTheState` |
| rejected-parse restore | `UnitXmlTest.everyRecognizedDefectFailsClosedAndRestoresTheEntryState` |
| paste | `UnitXmlTest.nonClearingParses…` (`InternalClipboard`); `UnitClipboardTest` |
| Insert File | `UnitClipboardTest.insertFileCarriesTheInsertedDocumentsProvenance` |
| `evalXML`, action replay | `UnitXmlTest.nonClearingParses…` (`evalXML`, `GENERIC_MERGE`); action replay uses the same `App.setXML(xml, false)` route |
| document macros, `.ggt`, `addMacroXML` | `UnitXmlTest.macroConstructionsNeverCarry…`; `UnitLoadTest.aToolFileIgnoresTheElement…` |
| macro and tool XML write | `UnitXmlTest.macroConstructions…`; `UnitLoadTest.aToolFile…` |
| tool editing | loads a macro construction (macro kernel): ignored, `EMPTY`; covered by the macro rows |
| preferences XML | `UnitXmlTest.preferencesXmlNeverContainsUnitState` |
| File → New, startup without a file, New Window, reset | `DocumentUnitsTest.defaultsApplyToNewBlankDocumentsOnlyAndBelongToTheBaseline` |
| `openURL` | the `AppGeoCeDG.clearConstruction` branch shared with reset without a file (tested) |
| DXF staleness fingerprint | unchanged; `ExportRegressionTest` |

## 6. Fail-closed matrix

Shared (`UnitXmlTest.everyRecognizedDefect…`, `misplacedElements…`): every §8.7
case on a clearing load restores the exact entry XML and unit state and names
its code. Desktop (`UnitLoadTest.recognizedDefectsFailClosed…`), each archive
as `.cedg` and as `.ggb`:

| # | Archive | Code |
|---|---|---|
| 0 | `version="2"` | `UNSUPPORTED_VERSION` |
| 1 | unknown attribute | `MALFORMED_ELEMENT` |
| 2 | `construction="usm"` without a factor | `INVALID_USM_FACTOR` |
| 3 | two elements | `DUPLICATE_ELEMENT` |
| 4 | element before `<construction>` | `MISPLACED_ELEMENT` |
| 5 | archive with macros, `version="3"` | `UNSUPPORTED_VERSION` |

For each of the twelve attempts: `loadFile` returns `false`; the rejected file
and the live document's file are byte-identical; the document XML, the unit
state, the current file and path, the unique id, the recent-file list, the
unsaved flag and the undo history are unchanged; exactly one localized message
naming the file and the defect is reported (checked verbatim in `en`, and in
`es` for one case). An injected live-load failure after the element was parsed
(`aLiveLoadFailure…`) restores the previous unit state and construction; a
rejected `setXML` restores its entry state and reports the defect; a failed
startup load ends in a blank document with the defaults and one message.

## 7. Canonical factor evidence

`CanonicalFactorTest` on the test runtime (Java 17.0.10, the Gradle test
toolchain): the 48-entry reference table of the
[preparation report](pre_g9b_r6_plus_d1_preparation_characterization_report.md)
§4 is reproduced byte for byte under the default locales `ROOT`, `es-ES`,
`de-DE`, `ar-EG`, `hi-IN` and `fr-FR` (the eight JDK-17-divergent entries 25,
29, 30, 31, 38, 40, 42 and 43 included); the seeded sweep of 400 000 values
(`SplittableRandom(20261002)`) reproduces the preparation digest
`5f2cec099ad0f70c4a2398e1ae676f47911c02e7e10b453876e209ba73352303` and round
trips; no shorter rounding round-trips; `0.02540` and other valid
non-canonical inputs read to the same binary64 and write canonically; the
lexical grammar and the writer's refusals are exact; writing is deterministic.

The product runtime is JDK 25. A scratch probe compiled the exact candidate
source `CanonicalBinary64.java` (SHA-256
`43e04b971cee835c28ddfda63c3427db9b631df64a806d100dcdf988d5e6d662`) with
`--release 17` and ran it on Temurin 25.0.4+7 and on Oracle 22.0.2+9 (the
Gradle launcher): on both, the 48 reference entries under the six locales
(288 checks) gave 0 mismatches, and the 400 000-value sweep gave 0 round-trip
failures and the digest `5f2cec09…352303`. The writer is deterministic on
JDK 17, 22 and 25.

## 8. Legacy byte identity

Before any product edit, `PreG9BR6PlusD1LegacyFingerprints` loaded each of the
68 corpus documents twice, each time in a fresh headless application (the two
loads must agree), in the GeoCeDG
preflight configuration and in the Classic default configuration, and recorded
the SHA-256 of its construction XML and full XML. The capture on the unchanged
tree was reproduced byte for byte on a `git archive` of the base, and the
committed fixture
`source/desktop/desktop/src/test/resources/org/geocedg/desktop/pre-g9b-r6-plus-d1/legacy-construction-fingerprints.json`
(SHA-256 `c0a80d66124c4385be85038d991fb4d33e53ad4419cee210aefbcdf9ce86c413`)
holds 67 fingerprinted documents per configuration. The one exclusion,
`source/desktop/desktop/src/test/resources/org/geocedg/desktop/g9u1-review/TestBasic1.cedg`,
does not load at the base (`SpatialIdentityException`, the immutable malformed
author archive). The helper was changed twice after the capture, and after
each change it was run again on the base archive and reproduced the fixture
hash exactly: the Checkstyle line wrapping of its corpus list (path prefixes
only; the 68 paths are unchanged), and loading, reading and stopping the
animation of each document inside one event-thread task (§15 item 7). The
committed helper is SHA-256
`7d26430f01596eddd611dbb0ad197633889155aa411bb44a687397be41d8d228`. An
intermediate variant that only stopped the animation after the read exposed a
race: `spring.ggb` starts an animation whose timer ticks on the event thread,
and a tick between the load and the read made its two fresh loads differ.
Inside one event-thread task no tick can interleave.
`LegacyByteIdentityTest` requires the candidate to reproduce every fingerprint
and to write no unit element for any corpus document (no migration on load).

## 9. Clipboard and two-window results

| Case | Result |
|---|---|
| copy in `mm`, paste in the same document | no notice |
| copy in `mm`, New with `cm`, paste | notice `mm` → `cm`; `(1, 2)` unchanged; target state unchanged |
| target `usm` with k = 0.001 (same metre factor as `mm`) | no notice |
| unspecified target or source | no notice |
| target `m` with presentation `mm` | notice `mm` → `m` (construction unit decides) |
| `usm` 0.0254 → `usm` 0.3048 | notice naming both factors; a second paste keeps it |
| Insert File of an `m` document into an `mm` document | notice `m` → `mm`; inserted numbers unchanged; a later Ctrl+V keeps the provenance |
| notice itself | no undo point (2-second latch), document stays saved, XML unchanged |
| two windows | distinct `CopyPasteD` buffers; no cross-window paste route; no provenance in the second window |
| `evalXML` and command insertion | no notice, no provenance |

## 10. Older-reader evidence (`DQ-D1-10`)

A document written by the candidate (`A`, `B`, `s = Segment(A, B)`;
`<geocedgUnits version="1" construction="usm" presentation="cm"
usmMetersPerUnit="0.0254" usmName="inch" usmSymbol="in"/>`) was opened by the
unchanged base build in a scratch probe on the `git archive`: it loads, the
three objects and their values are intact, the live XML no longer contains the
element and a re-save drops it. This is the documented §8.9 limitation; `D1`
adds no warning for it and the guide wording belongs to `G`.

## 11. Export regression

`ExportRegressionTest`: for the unit states `mm`; `cm` with presentation `mm`;
`m`; `usm` 0.0254 with presentation `cm`; and a `usm`-only state, the PNG
pixels and size, the SVG (clip ids normalized), the DXF, the DXF model's
`UNITLESS`/`UNITLESS` units and the PGF/TikZ, PSTricks and Asymptote code equal
those of the unit-free document; the DXF header keeps `$INSUNITS = 0`. No
exporter, export specification or verifier owned by `C` changed.

## 12. Obligation → test map

| ID | Tests |
|---|---|
| `T-STATE` | `UnitStateTest` (all nine) |
| `T-USM-VALIDATION` | `UnitStateTest.usmFactorValidity`, `usmNameAndSymbolValidity`; `DocumentUnitsTest.rejectedEditsWarnAndChangeNothing`, `validationCoversEveryRefusal` |
| `T-CANONICAL-FACTOR` | `CanonicalFactorTest` reference, locales, divergent entries, sweep, minimality, non-canonical input, determinism |
| `T-LEXICAL` | `CanonicalFactorTest.lexicalGrammarIsExact`, `writerRejectsInvalidFactors` |
| `T-XML-WRITER` | `UnitXmlTest.emptyStateWritesNothing`, `writerEmits…`, `writerEscapes…`, `macroConstructions…` |
| `T-XML-READER` | `UnitXmlTest.everyWriterCaseRoundTrips…`, `anyChildPosition…`; `UnitLoadTest.anArchiveWithMacros…` |
| `T-FAIL-CLOSED` | `UnitXmlTest.everyRecognizedDefect…`, `misplacedElements…` |
| `T-LOAD-RESTORE` | `UnitLoadTest.recognizedDefectsFailClosed…`, `aLiveLoadFailure…`, `aFailedStartupLoad…`, `aRejectedSetXml…` |
| `T-IGNORED` | `UnitXmlTest.nonClearingParses…`, `macroConstructions…`, `preferencesXml…`; `UnitLoadTest.aToolFile…` |
| `T-ROUTES` | §5 |
| `T-UNDO` | `UnitXmlTest.eachOperation…`; `DocumentUnitsTest.eachUnitOperationIsExactlyOneDesktopUndoPoint` (twelve operations including a combined define-and-select, then a no-op and Cancel; modified flag; undo and redo) |
| `T-REBUILD` | `UnitXmlTest.redefineAndRollbackRoutesCarryTheState` |
| `T-NEW-OPEN` | `DocumentUnitsTest.defaultsApplyToNewBlankDocumentsOnly…`; `UnitLoadTest.startupWith…`, `aFailedStartupLoad…` |
| `T-PREFS` | `DocumentUnitsTest.preferencesUseTheirKeys…`, `thePreferencePanelNeverTouches…`; every D1 Desktop class runs on an empty in-memory store |
| `T-SAVED` | `UnitXmlTest.saveRelevantContentIsANarrowSeamBesideIsStarted`; `DocumentUnitsTest.aUnitChangeMakesAnEmptyDocumentUnsaved…`, `defaultsApply…`; `UnitClipboardTest.theNoticeIs…` |
| `T-USM-LIFECYCLE` | `DocumentUnitsTest.eachUnitOperation…` (define, edit factor, name and symbol, switch, deselect, remove), `rejectedEdits…` |
| `T-INVARIANCE` | `UnitInvarianceTest.unitOperationsNeverTouchGeometryAndUpdateNoGeo`, `undoAndRedoOfUnitOperations…` |
| `T-NOTIFY` | `UnitXmlTest.notificationFiresOnEveryChangeAndNeverOnANoOp`; `UnitInvarianceTest` (no geo update) |
| `T-STATUS` | `DocumentUnitsTest.statusSegmentsPresent…`, `statusFollowsUndoAndRedo` (layout rebuild), `pasteNoticeLifecycleNeedsNoRealSleep`; `PasteNoticeVisibilityTest` (notice inside the laid-out live bar, revision 2) |
| `T-UI` | `DocumentUnitsTest.theDefaultDialogReturnsTheFieldsOrNull`, `eachUnitOperation…`, `rejectedEdits…` |
| `T-CLIPBOARD` | `UnitClipboardTest` (all seven); `PasteNoticeVisibilityTest` (P1–P5 with the visible notice, revision 2) |
| `T-LEGACY-BYTES` | `LegacyByteIdentityTest` |
| `T-COMPAT-CORPUS` | §8; `UnitXmlTest`; `UnitLoadTest` (physical, `usm`, `usm`-only, malformed, future, macro, `.ggt`); `UnitXmlTest.undoSnapshot…`, `nonClearingParses…` |
| `T-CLASSIC` | `UnitLoadTest.theClassicDiagnosticConfigurationSharesTheDocumentSemantics`; `UnitInvarianceTest.classicConfiguration…`; `LegacyByteIdentityTest` (Classic) |
| `T-EXPORT-REGRESSION` | `ExportRegressionTest` |
| `T-GGBSCRIPT` | `PreG9BR6CapabilityMatrixTest`; no command processor changed |
| `T-DETERMINISM` | `CanonicalFactorTest.writingIsDeterministic`, locale reproduction |
| `T-SMOKE` | §16 |

Shared tests live in `source/shared/common-jre/src/test/java/org/geocedg/common/units/`
(36 tests), Desktop tests in `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusD1*`
(29 tests in revision 1, 34 with `PasteNoticeVisibilityTest` in revision 2); class names above omit the `PreG9BR6PlusD1` prefix.

## 13. Deviations and interpretations

1. **Test isolation (harness).** Every D1 Desktop class installs an empty
   in-memory unit-preference store and the Desktop logger for each test, and
   restores both. Without them a D1 test would read the machine's installed
   preferences, and a headless application created earlier in the same JVM may
   leave the `AppCommon` logger, which rejects the logged exceptions of the
   host's fail-closed load paths (observed in a combined run; §15).
2. **Insert File route in tests.** The tests call
   `GeoCeDGCopyPaste.insertFrom(source, target, …)`, the method the inherited
   File → Open *Insert File* filter reaches through `AppD.insertFile`; the
   chooser itself is upstream.
3. **`usm` display.** The status bar shows the `usm` symbol, else `usm`; the
   name appears in the tooltip with the factor.
4. **Live-load failure comparison.** The construction section is compared; the
   GUI section is subject to the pre-existing behavior of §15 item 1.
5. **Outline pins.** The guide outline test pins (101 → 102 entries, 84 → 85
   level-3 headings) follow the new section 4.6, as `B` did for 4.5.
6. **Profile-authority allow-list.** The `workspace-profile.product` gate
   (`tools/agent/workspace-profile-validation.ps1`) accepts only the G9U1
   catalog plus listed approved amendments, so the authorized `document.units`
   action (`DQ-D1-1`) required one approved action id in that list, following
   the `R4`, `A-1` and `B` precedent, which the author accepted with those
   phases. No validator logic changed. Its input pin in
   `verification-static-contracts.json` and the `static_contracts` catalog pin
   were re-computed with `Get-VerificationCanonicalTextSha256` (existing
   mechanism). This sits next to the prompt's prohibition of verifier changes
   and is therefore reported here for the author's review.

## 14. Author dispositions applied

| Decision | Implementation |
|---|---|
| `DQ-D1-1` | one action, two presentation-only status segments (§3.6, §3.8) |
| `DQ-D1-2` | the panel writes preferences only; current state, XML, undo stack and saved flag unchanged (`thePreferencePanelNeverTouches…`) |
| `DQ-D1-3` | `MISPLACED_ELEMENT` in applying contexts; ignored with a text diagnostic in macro and other ignored contexts |
| `DQ-D1-4` | five localized `en`/`es` messages; a failed Open leaves the previous document unchanged |
| `DQ-D1-5` | `hasSaveRelevantContent()` (§3.4) |
| `DQ-D1-6` | §3.7 |
| `DQ-D1-7` | §3.9 |
| `DQ-D1-8` | §8.4 grammar with `.`; comma refused with its own hint; no grouping |
| `DQ-D1-9` | §3.8; injected timer, no real sleep |
| `DQ-D1-10` | no warning; §10 |
| `DQ-D1-11` | §3.10 |

## 15. Observations and residual risks

1. `OBS-D1-ROLLBACK-CONSPROT-GUI` (pre-existing, outside `D1`): after an
   injected live-load failure on a product-saved `.cedg`, the construction is
   restored exactly but the live GUI XML gains the construction-protocol
   column, protocol and navigation-bar elements of the rejected file. A scratch
   probe on the `git archive` of the base, with a unit-free document, shows the
   same three lines; the existing lifecycle test avoids it by showing the
   navigation first.
2. `OBS-D1-DEFAULTS-SAVE-RELEVANT`: by `DQ-D1-5`, a new document whose defaults
   set a physical unit is save-relevant. It stays saved while untouched; after
   any change that marks it modified, New and Close prompt where an upstream
   empty document would not.
3. `OBS-D1-INSTALLED-PREFERENCES-IN-TESTS`: test classes outside `D1` that
   create blank `AppGeoCeDG` documents read the installed preferences, as they
   already do for the presentation preferences. Setting a unit default in the
   installed product before a verification run would give their blank documents
   unit metadata. At freeze the installed Java preferences contain no unit key.
4. `OBS-D1-INHERITED-HEADLESS-LOGGER`: a headless application created by one
   test class can leave the `AppCommon` logger installed for later classes in
   the same JVM; host load paths that log exceptions then fail there. `D1`
   tests are isolated from it (§13 item 1); the hazard itself is pre-existing.
5. Older readers drop the element on re-save (§10, `DQ-D1-10`).
6. The Checkstyle warning `PreG9BR6PlusA1HiddenLayerTest.java:188` is
   pre-existing.
7. `OBS-D1-ANIMATING-DOCUMENT-RETENTION` (pre-existing host behavior): loading
   a document that starts an animation (`colored-cube.ggb`, `polyhedron.ggb`
   and `spring.ggb` of the corpus) into a headless application starts the
   animation timer, which keeps that application reachable for the rest of the
   JVM. The first `final.desktop` producer run of this candidate (before the
   helper fix) ended with one `OutOfMemoryError` in the unrelated
   `SvgLoadTest.testLoadSvgsGGB`: the corpus test had left six such
   applications behind (50 MB retained after a full GC, against 20–25 MB for
   other application-creating classes). The helper now loads, reads and stops
   the animation in one event-thread task (27 MB retained, two headless
   applications); the remaining heap headroom of the single Desktop test JVM
   (default 512 MB) stays a pre-existing property of the suite. The same timer
   makes any fingerprint of an animating document taken off the event thread
   timing-dependent; the original capture and all its reproductions agreed.

## 16. Author-smoke checklist (not performed or attributed by the agent)

1. **New-document defaults.** Options → Preferences… → Layout & Presentation →
   New document units: set `mm` and presentation `cm`; File → New and a new
   window show them in the status bar; the new document is not marked
   modified; an opened document keeps its own units; the current document did
   not change when the preference was set.
2. **Document Units dialog.** Options → GeoCeDG options → Document units… opens
   it; Cancel and OK without a change do nothing.
3. **Standard units.** Switch the construction unit between unspecified, `mm`,
   `cm` and `m` and the presentation unit between the follow option and each
   unit; coordinates and values never change.
4. **`usm`.** Define one (factor `0.0254`, name `inch`, symbol `in`), edit its
   factor, name and symbol, select it, switch away, remove it; try `0,0254`,
   `1 000`, `abc`, a leading space and removal while selected: each shows a
   warning and changes nothing.
5. **Status segments and tooltips.** Both unit segments show the current units
   in English and Spanish; tooltips explain them and give the `usm` factor; a
   click opens Document units.
6. **Undo and redo.** Each change is one undo step; undo and redo restore the
   units and the status bar.
7. **Unsaved prompt.** In an empty new document, change only the units, then
   New or close: GeoCeDG asks to save.
8. **Open valid unit documents.** Save, reopen: units restored; the same with an
   archive containing a user tool.
9. **Rejection.** Open a document with malformed unit metadata and one with
   `version="2"` (author-made copies): each is refused with its message and the
   current document stays as it was.
10. **Paste.** Copy from an `mm` document, File → New with `cm` (or Open a `cm`
    document), paste: the notice appears for about ten seconds and the numbers
    are unchanged; with matching units no notice.
11. **Insert File.** File → Open… with the Insert File type, inserting an `m`
    document into an `mm` document: the same notice.
12. **Legacy documents.** Open and re-save an existing document without units:
    it stays without them; no prompt appears for an untouched legacy document.
13. **Classic preservation.** Open a unit document in the Classic diagnostic
    session and re-save it: the units survive (no unit UI there).
14. **Exports unchanged.** Picture, DXF and LaTeX exports of a document with
    units equal those without; DXF stays unitless.
15. **Older-reader limitation.** An older GeoCeDG build opens a unit document
    and drops the units on re-save (documented; no warning in `D1`).

## 17. Pre-freeze evidence

These runs preceded the freeze; they are development and inventory evidence, not
acceptance. The single `FINAL` on the frozen candidate is reported with the
candidate, outside this file.

| Run | Result |
|---|---|
| focal shared `org.geocedg.common.units.*` | 4 classes, 36 tests, 0 failures/errors |
| focal Desktop `PreG9BR6PlusD1*` | 5 classes, 29 tests, 0 failures/errors (after the fixes below) |
| first combined adjacent run (shared and Desktop lists of the prompt) | shared green; Desktop 292 tests, 5 D1 failures from the inherited headless logger in that order (§13 item 1), fixed by the per-test logger isolation |
| second combined adjacent run, with the guide suites | shared 13 classes, 150 tests, 0 failures/errors; Desktop 365 tests, 2 failures = the two outline pins of §13 item 5, then `PreG9BR5AGuideOutlineTest` 20 tests, 0 failures/errors |
| guide structure (`tools/agent/diagnostics/guide-structure.psm1`) | 0 findings per edition; English and Spanish vectors equal |
| Checkstyle (`:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest`, XML reports) | no finding in any D1 file after the fixes (corpus line lengths, two final newlines, one declaration distance); the one remaining warning, `PreG9BR6PlusA1HiddenLayerTest.java:188`, is pre-existing |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522` | OK, 918 registered files |
| `Assert-GeoCeDGLiveWorkspaceProfile` (amended allow-list) | OK, 125 actions |
| producer `discovery.shared` (`--test-dry-run`) | completed, 5 976 identities |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 762 identities |
| producer `final.shared` (executed, complete shared suite) | 6 893 tests, 0 failures/errors, 10 skips |
| producer `final.desktop` (executed), first run | 1 756 tests, 1 failure: `OutOfMemoryError` in `SvgLoadTest.testLoadSvgsGGB` (§15 item 7); not used as evidence |
| producer `final.desktop` (executed), after the helper fix | 1 756 tests, 0 failures/errors, 1 skip |
| heap probe (scratch, retained heap after a full GC) | `LegacyByteIdentityTest` 50 MB before the fix, 27 MB after; other D1 classes 20–24 MB; `PreG9BR6PlusA1LayerWorkspaceTest` 25 MB |
| scratch base probes (`git archive 3268f9b9`) | fingerprint fixture reproduced by the original, the wrapped and the event-thread helper (`c0a80d66…`); older reader (§10); rollback GUI behavior (§15 item 1) |
| scratch canonical-factor probe | Temurin 25.0.4 and Oracle 22.0.2: 288/288 reference checks, sweep digest `5f2cec09…` (§7) |
| `git diff --check` (tracked and new files) | clean |
| development `STATIC` and `INFRA_UNIT` on the staged, uncommitted candidate tree | `ACCEPTED / COMPLETE` (`verification-a226ac4b947a4509bc65d20932ea448c`, 3/3; `verification-a908120178b2470f928e094f9b3a85fa`, 22/22); the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; documentation, guide-structure, style and whitespace diagnostics clear; not acceptance evidence |

The skips are the pre-existing allowlisted ones.

**JUnit inventory and pins.** The current pins were reproduced first from the
`HEAD` blobs (`junit_inventory` `4ebae708…`, `static_contracts` `eaba0ba5…`,
`workspace-profile-validation.ps1` input `2e5b328d…`). Discovery evidence came
from the two `--test-dry-run` producer runs, executed evidence from the passing
`final.shared` run and the passing second `final.desktop` run, and
`tools/agent/update-verification-junit-inventory.ps1`, run in-session with
absolute paths, wrote every count and hash: shared module discovery 5 940 →
5 976 (`7d26d560…`), Desktop module discovery 1 733 → 1 762 (`fa18bc0b…`),
`final.shared` 6 857 → 6 893 (`3f60a048…`) and `final.desktop` 1 727 → 1 756
(`d3faaeb0…`). Every other selection is unchanged; no selection was added. The
profile-validation input was re-pinned to `92c61af0…` with
`Get-VerificationCanonicalTextSha256`, then the registry catalog pins:
`junit_inventory` `1cbb3bf18b29bd43a871e1c5596d47b1ace047cf7b8a17075df3784ea9684709`
and `static_contracts` `567c095813739497c6d63e12ed4a74b90700a4574d709484ef592ffc833a880c`.
No hash was typed by hand. No registry-shape pin changed.

## 18. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes.
VERIFICATION_INFRASTRUCTURE_IMPACT = INVENTORY_AND_PROFILE_AUTHORITY_PIN
Rationale: the JUnit inventory (discovery and final selections) and its
registry pin are refreshed through the official updater with executed
evidence; the bounded profile-authority amendment (one approved action id,
§13 item 6) and its static-contract input and catalog pins follow the R4/A-1/B
precedent; no new selection, PHASE registration, verifier logic, schema,
registry shape or profile-composition change.
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/user/geocedg_user_guide_en.md;
  docs/user/geocedg_user_guide_es.md;
  docs/developer/geocedg_developer_guide.md;
  geocedg/specs/ui/cedg-workspaces.md
SERIALIZATION CHANGE = <geocedgUnits> element per unit-system v1.0 §8
PRODUCT CHANGE = YES
```

## 19. Changed paths

- Shared product, new: `source/shared/common/src/main/java/org/geocedg/common/kernel/units/`
  (`CanonicalBinary64`, `DocumentUnitSystem`, `UnitDocumentOperations`,
  `UnitMetadataException`, `UnitQuantity`, `UnitState`, `UnitStateXml`,
  `UnitToken`, `UsmDefinition`).
- Shared upstream, modified (serialization and save seams):
  `source/shared/common/src/main/java/org/geogebra/common/kernel/Construction.java`,
  `source/shared/common/src/main/java/org/geogebra/common/io/MyXMLHandler.java`,
  `source/shared/common/src/main/java/org/geogebra/common/io/MyXMLio.java`,
  `source/shared/common/src/main/java/org/geogebra/common/main/App.java`.
- Desktop upstream, modified (host-identical hooks):
  `source/desktop/desktop/src/main/java/org/geogebra/desktop/main/AppD.java`,
  `source/desktop/desktop/src/main/java/org/geogebra/desktop/util/CopyPasteD.java`,
  `source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/dialog/options/OptionsLayoutD.java`.
- Desktop GeoCeDG, new: `GeoCeDGCopyPaste`, `GeoCeDGDocumentUnits`,
  `GeoCeDGDocumentUnitsPrompt`, `GeoCeDGNewDocumentUnitsPanel`,
  `GeoCeDGUnitPreferences`; modified: `AppGeoCeDG`, `GeoCeDGActionRegistry`,
  `GeoCeDGProfile`, `GeoCeDGStatusBar` (all in
  `source/desktop/desktop/src/main/java/org/geocedg/desktop/`).
- Profile: `apps/geocedg/application-profile.yml` (action, effect profile,
  group and cluster entries, 39 localized texts).
- Tests, new: the four shared classes in
  `source/shared/common-jre/src/test/java/org/geocedg/common/units/`; the five
  Desktop classes and the helper `PreG9BR6PlusD1LegacyFingerprints` in
  `source/desktop/desktop/src/test/java/org/geocedg/desktop/`; the fixture
  `source/desktop/desktop/src/test/resources/org/geocedg/desktop/pre-g9b-r6-plus-d1/legacy-construction-fingerprints.json`.
- Tests, pins only: `G9U1ActionRegistryTest`, `G9U1ProfileCompilerTest`,
  `G9U1WorkspaceSurfaceTest`, `GeoCeDGProfileTest`,
  `PreG9BR6PlusA1LayerWorkspaceTest` (124 → 125) and `PreG9BR5AGuideOutlineTest`
  (section 4.6).
- Upstream boundary: `docs/upstream/modified-files.yml`.
- Verification data: `geocedg/specs/operations/verification-junit-inventory.json`,
  `geocedg/specs/operations/verification-registry.json`,
  `geocedg/specs/operations/verification-static-contracts.json`,
  `tools/agent/workspace-profile-validation.ps1` (§13 item 6).
- Documentation: `docs/user/geocedg_user_guide_en.md`,
  `docs/user/geocedg_user_guide_es.md`, `docs/developer/geocedg_developer_guide.md`,
  `geocedg/specs/ui/cedg-workspaces.md`, `docs/roadmap/geocedg_roadmap.md`,
  `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`, this report and
  `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-d1-candidate-evidence.json`.

Not changed: ADR 0032, the unit specification, `AGENTS.md`, `CLAUDE.md`,
`.github/prompts/**` after the authorization edit, `ai-shell/prompts/**`, the
verifier (`tools/agent/verify.ps1`, its modules and checks; the only
`tools/agent` change is the allow-list entry of §13 item 6) and its schemas,
`prompt-contracts.json`, every exporter and export
specification, and `artifacts/author-input/**`.

## 20. Revision 2: author-smoke remediation of the paste notice

The author smoke of `b55aa817` found one functional defect: after copying from a
document with one physical construction unit and pasting into a document with
another, the geometry was pasted with unchanged numbers and target unit, but no
notice was visible. The author authorized a bounded correction of that defect
only; this revision is the resulting candidate. The previous candidate and its
`FINAL` (header) remain historical evidence for that commit only.

**Reproduction.** Scratch probes ran the real windowed product
(`GeoCeDG.main`, isolated copies of the author's settings file and of the
author's smoke documents `Circle.cedg` (`mm`) and `Vacio.cedg`): open, `Ctrl+A`,
`Ctrl+C`, open the target, switch its construction unit to `cm` through the
Document units operation, `Ctrl+V` through the focus manager's dispatch. The
whole chain worked: provenance `mm` present at paste time, target `cm`, the
comparison true, the hook fired on the event thread (paste 26–92 ms), the
notice label of the window's own bar set visible with the localized text, no
premature clearing. In Spanish, at the author's 800 × 600 window, the notice was
nevertheless laid out at `y = 19` in a bar 19 pixels high: entirely below the
visible bar. The screenshot shows the permanent segments, a separator and no
notice. Pasting into `Vacio.cedg` as saved (construction `mm`) correctly shows
no notice.

**Root cause.** The bar used a `FlowLayout`, which moves a component that does
not fit into a further row. The bar sits in the one-row `BorderLayout.SOUTH` slot
of the application panel, so that row receives no height. The notice is the last
segment and the longest: whenever the bar's content was wider than the window
(925 pixels needed against 786 available in Spanish; English at 800 pixels fit
by 5 pixels in the real look and feel and did not fit in the test look and feel),
the notice disappeared while Swing still reported it visible and showing.

**Why the tests missed it.** The D1 tests asserted the notice segment's
`isVisible()` and text on a bar that was never laid out inside the application
panel; they never checked its bounds against the bar.

**Fix.** `GeoCeDGStatusBar` lays its segments out with `SingleRowLayout`: one
row that never wraps; every segment keeps its preferred size; the last visible
segment (the notice) receives only the remaining width, so a long notice is
elided by the label and its tooltip carries the full text. Nothing else changed:
the provenance, the shared physical-meaning comparison, the notice text,
lifetime, clearing, timer and the permanent segments are those of revision 1.
The user guide (section 4.6, both editions) and the developer guide mention the
shortening.

**Evidence.**

- The same Spanish replay after the fix: notice at `x = 394, y = 2`, 384 × 16
  pixels inside the 786 × 19 bar; a real screen capture (`java.awt.Robot`) of
  the window's bottom row shows `Capa: 0 | Unidad de construcción: cm | Unidad de
  presentación: km | Pegado desde un documento en mm a un documento en cm; las
  coorden…`.
- `PreG9BR6PlusD1PasteNoticeVisibilityTest` (5 tests) lays the real application
  panel out with the real status bar and pastes through the global key
  dispatcher: P1 (copy in `mm`, File > New, `cm`, paste) in English and Spanish at
  a width 200 pixels below the bar's content, P2 (Open between copy and paste),
  P3 (`usm` k = 0.001 against `mm`: no notice), P4 (unspecified source or target:
  no notice), P5 (Insert File, `m` into `mm`); the notice clears on the next
  paste, on File > New and on the injected timer's expiry; the permanent
  segments keep their text, bounds and preferred widths; units, coordinates and
  XML are unchanged by the notice. Run against the `b55aa817` status bar (file
  swapped temporarily and restored byte-identical), four of its five tests fail
  with the notice at `y = 19` below a 19-pixel bar; the no-notice test passes.
- `T-CLIPBOARD`, `T-STATUS` and every other D1 test are unchanged and pass.

**Observation.** The offscreen paint used by the probes clips the last
characters of the permanent labels; the real screen capture does not, so it is
an artifact of painting into an image, not a product defect.

**Revised author-smoke step.** In an 800 × 600 window, in English and in
Spanish: copy from a document whose construction unit is `mm`, File > New (or
Open) a document whose construction unit is `cm`, paste: the notice appears at
the right of the status bar, shortened if the window is narrow, with the whole
text in its tooltip, and disappears after about ten seconds, on the next paste
or on New/Open.

**Pre-freeze evidence of revision 2** (development and inventory evidence, not
acceptance):

| Run | Result |
|---|---|
| D1 Desktop focal `PreG9BR6PlusD1*` | 6 classes, 34 tests, 0 failures/errors |
| Desktop adjacent (status bar, layers, export surface, lifecycle, clipboard, profile and guide suites) | 278 tests, 0 failures/errors |
| guide suites after the guide edits; guide structure | `PreG9BR5AGuideOutlineTest`, `PostP1BilingualUserGuideTest`, `PostP1GuideRenderingTest` green; 0 findings, vectors equal |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` | no finding in D1 files; the pre-existing `PreG9BR6PlusA1HiddenLayerTest.java:188` warning only |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b…` | OK, 919 registered files (the new test added; the status-bar entry annotated) |
| producer `discovery.desktop` (`--test-dry-run`) | completed, 1 767 identities |
| producer `final.desktop` (executed) | 1 761 tests, 0 failures/errors, 1 skip |
| inventory updater (in-session; the revision-1 `discovery.shared` dry run reused, no shared source changed) | `discovery.desktop` 1 762 → 1 767 (`cc9bb17c…`), `final.desktop` 1 756 → 1 761 (`7b70e68c…`); every other selection unchanged; `junit_inventory` pin `3895083f57f02e836e658e073f52324192793d235e89658d3ea741e8c0a41374`; no registry-shape pin changed |
| `git diff --check` (staged) | clean |
| development `STATIC` and `INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE` (`verification-2273c2a422dc48568be8dba658d54ec4`, 3/3; `verification-1a03e8dc0ca949eb9fa47fcc1a215602`, 22/22); standing diagnostics only |

The single `FINAL` of revision 2 is made on the frozen revision-2 commit and
reported outside this file; the revision-1 receipt does not certify it.

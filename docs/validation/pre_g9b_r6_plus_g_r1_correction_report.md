# PRE-G9B-R6-plus-G-R1 — Open-route session correction and tool-replacement report

```text
REPORT_KIND                  = BOUNDED CORRECTION, REGRESSION FINGERPRINT AND
                               TOOL-REPLACEMENT CHARACTERIZATION (G-R1, operations 2 and 3)
AUTHORIZATION                = G-R1 authorization record (2026-10-15)
CORRECTED VIOLATIONS         = SC-01, SC-02 (ExportArea) and SC-11 (paste notice)
PRODUCT CHANGE               = AppGeoCeDG only (GeoCeDG-owned Desktop lifecycle)
INHERITED / SHARED / KERNEL  = NONE
SERIALIZATION / GEOMETRY     = NONE
REGRESSION TESTS             = PreG9BR6PlusGR1OpenRouteSessionTest, 9 tests (4 corrective,
                               5 preservation); the corrective ones fail on the uncorrected code
RE-CHARACTERIZATION          = 57 probe rows x 2 runs on the corrected tree; 28 rows change,
                               exactly the authorized ones; both runs identical
OBS-R6PLUS-NONNATIVE-DOCUMENT-REPLACEMENT-SESSION-RESET (B-1)
                             = OPEN ROUTES CORRECTED BY G-R1; ROUTES WITHOUT A CONTRACT
                               PENDING AUTHOR DECISION (§7)
OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT (B-2)
                             = UNCHANGED — CORRECTION NOT AUTHORIZED — DISPOSITION PENDING
                               AUTHOR REVIEW (proposal in §6)
selfApproved                 = false
```

This report records the correction authorized by the
[G-R1 authorization record](pre_g9b_r6_plus_g_r1_authorization_record.md) and its
evidence. It does not rewrite the
[SC-01 to SC-27 characterization report](pre_g9b_r6_plus_g_document_replacement_characterization_report.md),
whose rows and raw evidence stay the historical fingerprint of the uncorrected product.
Machine-readable mirror:
[`pre-g9b-r6-plus-g-r1-correction.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-r1-correction.json).

## 1. Contract and executable route set

Rule (authorization §5): a successful full-document Open transaction applies the
approved document-transition session reset, once, after commitment, independently of
transport. The approved inputs, as the
[G author decision reconciliation](../architecture/pre_g9b_r6_plus_g_author_decision_reconciliation.md)
§5.3 states them for the scenarios the author approved:

- `AQ-X2`: `ExportArea` is a session state that "resets on New and Open"; `DQ-F3-2` makes
  `.html`/`.htm` an Open file type; `DQ-A2-6` places the API file and URL routes inside the
  native Open transaction (SC-01, SC-02).
- `DQ-D1-9`: the paste notice clears "on the next document transition", anchored on the
  successful-transition events E2, E3 and E5 (SC-11, characterized on R01–R08 and R17).
- SC-03 to SC-05 (`ExportArea` on `setBase64`, non-native `openFile`/`loadXML(File)` and API
  `setXML`), SC-16 (the v1 Open URL after a cancelled pre-clear) and SC-26 (embedded close)
  have no approved `ExportArea` contract; they are excluded by the authorization §6.

The reconciliation's `DQ-G-5` text also says that no approved text fixes "the expected
`ExportArea` or notice behaviour" of `setBase64`, non-native `openFile` and `setXML`, while its
§5.3 row SC-11 expects the notice to reset on those routes through E3 and E5. G-R1 follows
the approved scenario expectation SC-11, which the authorization names as a confirmed
violation, and leaves the `ExportArea` of those routes unchanged. The author may review this
reading in the smoke (§8).

| Route | Entry | Commit event | `ExportArea` | Paste notice | G-R1 change |
|---|---|---|---|---|---|
| R01 File > Open `.html` (also file drop) | `openResolved` → `doOpenFiles` → `loadBase64File` | E3 (E2 when the page names a native file) | **reset** (Open route) | **cleared** | yes |
| R02 API `openFile` native | `openFileUnsafe` → `loadXML(URL)` → `loadXML(File)` | E2 | **reset** | **cleared** | yes |
| R03 URL drop `file:` | `GuiManagerD.loadURL` → API `openFile` | E2 | **reset** | **cleared** | yes |
| R04 URL drop stream | `loadURL` → `loadXML(URL)` native archive | E2 | **reset** | **cleared** | yes |
| R05 API `setBase64`; R17 v1 Open URL, prompt cancelled | `loadXML(ZipFile)` | E3 | kept (no contract) | **cleared** | notice only |
| R06 API `openFile` non-native; R07 `loadXML(File)` non-native | `GFileHandler.loadXML` | E3 | kept (no contract) | **cleared** | notice only |
| R08 API `setXML` | `runApiDocumentReplacement` → clearing `setXML` | E5 | kept (no contract) | **cleared** | notice only |
| R09, R10 `loadFile`; R11 `loadXML(String)` | `loadExistingFile`, `loadXML(String)` | E2, E3 or none | reset (unchanged) | cleared (unchanged) | one reset point |
| R12 New; R18 v1 Open URL saved; R20 blank reset | `clearConstruction` | — | reset (unchanged) | cleared (unchanged) | none |
| R13, R14 tool replacement; macro-edit overwrite | clearing `setXML` without marker | none | kept | kept | none |
| R15 Insert File; R16 Apply Styles | insertion transaction | none | kept | Insert File: own notice; Apply Styles: kept | none |
| R19 embedded close | window-less `closeWithoutSavePrompt` | none | kept (no contract) | kept | none |

## 2. Implementation

Only `AppGeoCeDG` changes (the hooks it overrides are the existing A-2 seams; no inherited
line changes):

- `resetDocumentSession(openRoute)` is the single reset point: it clears the paste notice of
  an existing status bar (a notice can only be visible there) and, for an Open route, calls the
  approved `ExportAreaSession.resetForDocument()` (MANUAL rectangle, explicit producer,
  overlay and ISO A link; no stale-link notice).
- E2 `nativeDocumentLoadCommitted`: after the existing layer commit and drawing-scale reset,
  `resetDocumentSession(true)`.
- E3 `documentReplacementCommitted`: `resetDocumentSession(openRouteRunning)` — the export
  area resets only when an Open route runs the replacement.
- E5 (API branch of the clearing `setXML`): `resetDocumentSession(false)`.
- `runOpenRoute` marks `loadExistingFile`, `loadXML(String)` and the new `loadBase64File`
  override as Open routes; when the route succeeded without reaching a commit event
  (`loadXML(String)`) it resets once; a commit event inside the route makes it skip its own
  reset, so no transition resets twice. A failed route resets nothing.
- Guards as before: no reset while the host constructor loads a startup document
  (`layerWorkspaceActive` false); the reset runs on the thread that commits, as the existing
  `loadExistingFile` reset did.
- Untouched: `commitLoadedDocument`, the generic clearing `setXML` (tool replacement),
  `clearConstruction`, Insert File, Apply Styles, the embedded close, the hidden-layer,
  working-layer and drawing-scale lifecycles, the ISO A identity validation, document
  identity, undo, macros, serialization and Classic.

## 3. Regression tests

`PreG9BR6PlusGR1OpenRouteSessionTest` (embedded hosts; one primed window per route: MANUAL
area or ISO A link with overlay, visible paste notice, drawing scale 1:2, working layer 2 of
a document whose highest layer is 6):

| Test | Routes | Asserts |
|---|---|---|
| `approvedOpenRoutesStartANewExportAreaSessionExactlyOnce` | R01–R04, R09–R11 | no producer, no MANUAL area, overlay off, **exactly one** session change, notice cleared, document B, scale 1:1, working layer 0, hidden set `[5]` |
| `openRoutesReleaseTheIsoALinkWithoutALostLinkNotice` | R01–R04 (ISO link) | link released, `isLinkLost` false |
| `everyOtherDocumentTransitionClearsOnlyThePasteNotice` | R05–R08, R17 | notice cleared; MANUAL area, overlay kept, no session change; document B |
| `apiRoutesBehaveAlikeOnTheEventThread` | R02, R05, R08 on the EDT | as off the EDT |
| `cancelledAndFailedOpensKeepTheDocumentAndItsSession` | chooser cancelled; `.html` without an archive; corrupt `.cedg` through File > Open and API `openFile` | document A, MANUAL area, notice, scale 1:2 and working layer 2 kept; no session change |
| `toolReplacementAndSameDocumentReloadsKeepTheSession` | R13, R14 | session kept; working layer recomputed to 6 (SC-17, unchanged) |
| `macroEditOverwriteKeepsTheOriginalWindowSession` | SC-18 emulation | original window: session, notice and scale kept; working layer 6 |
| `insertFileAndApplyStylesKeepTheirSessionBehaviour` | R15, R16 | session and scale kept, working layer 2; Insert File shows its own unit notice, Apply Styles keeps the notice |
| `classicKeepsTheInheritedLoadRoutes` | Classic `AppD` | `.html` and API `openFile` load with the inherited routes |

Results on the corrected tree: 9/9 pass; with 119 tests of 13 related classes (drawing
scale, hidden layers, base identity, D1 units and notice, F3 surface, E3 ISO A border, B
export surface, base fingerprint, smoke follow-up, E3-R1 interoperability) 128/128 pass.
Negative control: with `AppGeoCeDG` restored to its uncorrected bytes the four corrective
tests fail at the characterized violations (`MANUAL` kept, notice kept, link lost) and the
five preservation tests pass; the corrected bytes were restored hash-identical.

## 4. Re-characterization of SC-01 to SC-27 on the corrected tree

The unchanged scratch probe (`GReplacementCharacterizationProbe.java`, SHA-256
`5f3c7d9c49d1602489a72bd29e1731646089ed8c8b7f67666dec0e3ae74c70fc`; init script
`bb260003112386845306e3d14913ee328266f05ea59a62026b412e1c42b697cd`) was run twice on the
corrected working tree and its build residue removed. Field-by-field against the original
run 1:

- the two corrected runs are identical (57/57 rows, Java identity hashes normalized);
- every primed starting state equals the original one;
- **28 of 57 rows change**, all of them the authorized ones:
  - R01–R04 (groups M1, M2, M3, M5): producer, MANUAL rectangle, overlay and resolved source
    reset; with an ISO link, `isLinkLost` false and no "ISO A sheet link released" sheet
    segment; notice cleared;
  - R05–R08 and R17 (M1, M2, M3, M5): notice cleared only;
- unchanged: R09–R16, R18–R20, the tool replacement and macro-edit rows (M4), startup and New
  Window (M6), drawing scale, working layer, hidden set, memory slots and activation
  (SC-21 to SC-23), orientation cue, status segments, document identity, saved state and
  undo.

## 5. Characterization limits (stated, not hidden)

- File > Open runs through `openResolved` after the chooser; no real chooser or window.
- URL drops run through `GuiManagerD.loadURL`, the call the drop listener makes; no real
  drag gesture. File drops of `.html` share `doOpenFiles` → `loadBase64File` with R01 and are
  covered by source, not by a drop test.
- R17's cancelled save prompt is a mocked `JOptionPane`; SC-18 uses a second embedded host
  as the macro-edit window; the New Window case is a constructor emulation.
- Embedded views have no laid-out size, so the fallback (visible viewport) resolution is not
  observable; the explicit producer and the MANUAL rectangle are.
- An `.html` page naming a remote file or an iframe source, and URL drops of non-native or
  `.html` URLs, were not exercised; by source they commit through E2 or E3 like R02/R04 or
  R05.
- Interactive GUI confirmation belongs to the author smoke (§8).

## 6. Tool replacement — `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` (B-2)

No product change was made to tool replacement. Evidence:

| Scenario | Before G-R1 (characterization) | After G-R1 (probe and tests) |
|---|---|---|
| SC-07 `ExportArea` (R13, R14) | kept | kept, no session change |
| SC-10 ISO link (R13, R14) | stale → dropped, link lost shown | unchanged |
| SC-12 paste notice (R13, R14) | kept | kept |
| SC-17 working layer (R13) | recomputed 2 → 6 | recomputed 2 → 6 |
| SC-18 macro-edit overwrite, original window | working layer 2 → 6; `ExportArea`, scale kept; memory emptied | unchanged |
| drawing scale (R13, R14) | 1:2 kept | 1:2 kept |

The tool-replacement reload is a clearing `setXML` without the API marker, so it reaches
none of E2, E3 or E5 and acquires no Open semantics.

**Proposed disposition (author decision required):**

1. Record that tool replacement is not a document transition: `ExportArea`, paste notice and
   drawing scale are preserved, and the ISO A link is revalidated by identity (SC-07, SC-12
   equal to their analogue `[P]`; SC-10 matches `iso-a-border.md:424`).
2. Defer the working-layer recomputation (SC-17, SC-18) with **G11 — Capas y estados de
   vista** as owner: G11 already owns layer organization, saveable states and the direct
   working-layer entry (`ENH-R6PLUS-WORKING-LAYER-DIRECT-ENTRY`, P-2). Until G11 it would
   stand as a characterized behaviour of the R6-plus product (the working layer becomes the
   highest shown layer used by a drawable object after a tool is created or replaced).
3. Alternatives: accept 2 as a permanent limitation, or authorize a bounded correction that
   restores the session working layer after the reload.

The observation stays `OPEN` with its mandatory disposition before the global closeout.

## 7. B-1 after G-R1

Corrected: the approved Open routes (SC-01, SC-02) and the paste notice on every
characterized transition (SC-11). Still without a contract, recorded for the author's
decision (`NO CONTRACT — OBSERVED`, unchanged by G-R1): `ExportArea` on `setBase64`,
non-native `openFile`/`loadXML(File)` and API `setXML` (SC-03 to SC-05); `ExportArea` after
a cancelled v1 Open URL pre-clear (SC-16); the embedded close (SC-26); the Move switch on a
native Open (SC-23). A proposal is in the consolidated decision sheet.

## 8. Author smoke (Open routes)

In a development run or an app-image of the candidate: define a MANUAL export area (or link
an ISO A border) and show its overlay; paste between documents with different units to show
the paste notice; then File > Open a `.cedg` and an `.html` page, and drop a `.cedg` file or
URL. After each successful Open the overlay is gone, the next Picture/PDF export proposes no
previous rectangle and the notice is gone. Cancelling the chooser or the save prompt, or
opening a corrupt file, keeps both. A tool replacement (Tools > Create New Tool replacing an
existing tool) and Insert File keep the export area.

## 9. Evidence

Git-ignored `artifacts/agent/pre-g9b-r6-plus-g-r1-characterization/` (local evidence, not
authority; `SHA256SUMS.txt` lists every file):

| File | SHA-256 |
|---|---|
| `r1-probe-results-run1.json` (57 rows) | `8d97e90a0b47519f6db295bc0d876f46c80118afce62d1e93f80380f95b30b0f` |
| `r1-probe-results-run2.json` (57 rows) | `1cbe67438462b5dafa4b5a5cf3da7abb93517eecd2a002b013b610265127aa55` |
| `original-probe-results-run1.json` (characterization run 1) | `a9fa785828a51fa78e7e7acc6aed4a938580af4f7f1b68a6e74c283977828065` |
| `r1-probe-compare.txt` (field comparison) | `9214e1a2f08156027702bc590f074bb9d3a0ae9c8e86d2ee314dec271e420ff1` |
| `r1-negative.log` (negative control) | `ac0a86d31aeb50225ef14e1fc0fc5e44c01078b32cceabfce85e65f8e295dc42` |
| `r1-related1.log` (128 related tests) | `fb5a4457421921f64d2e63581f629ea3889ce0d02db4833a8f149dbd1be2cd67` |

Commands: `gradlew.bat :desktop:desktop:test --tests …PreG9BR6PlusGR1OpenRouteSessionTest`
(and the 13 related classes); the probe as in the characterization report §7 with `--rerun`.
The candidate's acceptance results are reported outside the candidate.

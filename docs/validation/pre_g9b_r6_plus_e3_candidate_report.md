# PRE-G9B-R6-plus-E3 — native `IsoABorder` and `ExportArea` integration: candidate report

```text
TECHNICAL_CANDIDATE_STATE = FROZEN with the candidate commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-E3
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE (DQ-E3-16)
frozenAtPhaseStart        = true
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E3
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)
REPRESENTATION            = R2 (existing GeoElement types only: two GeoPolyLine, one GeoText)
COMMAND                   = IsoABorder (Spanish alias MarcoISOA), seven arguments, no overload
CLASSIC SURFACE           = shared command available in GeoCeDG and Classic (OTQ-E3-1);
                            GUI and export-area orchestration GeoCeDG Desktop only
NUMERICAL CONTRACT        = obligations A / B / C, validity versus reliability (amended 2026-10-10)
PRODUCT CHANGE            = YES (one command, one menu-only tool, two File actions, two
                            export-area producers, transient coherence indicators)
SERIALIZATION CHANGE      = ADDITIVE ONLY (one <command> name; element types polyline and
                            text; no new element type, attribute or reader change)
GUIDE_IMPACT              = UPDATED (EN and ES section 4.7)
BOOTSTRAP IMPACT          = NO CHANGE REQUIRED
AUTHOR_SMOKE              = PENDING (required; section 15)
selfApproved              = false
authorApproved            = false
implementationAuthorized  = true
passClaimed               = false
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact technical
commit is the sole authority for approval. The candidate commit cannot name
itself, so its identity and the two acceptance runs made on it are reported
outside this file. The machine-readable mirror is
[`pre-g9b-r6-plus-e3-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e3-candidate-evidence.json).

## 1. Authorization and entry identity

The author authorized `PRE-G9B-R6-plus-E3` for implementation and technical
verification on 2026-10-10 (`PRE-G9B-R6-plus-E3-IMPLEMENTATION`): Stage A, the
documentary reconciliation that resolves `OTQ-E3-1` and corrects the
numerical-accuracy contract, then Stage B, the implementation. The decisions
`DQ-E3-1` to `DQ-E3-16` and the amendment of §5 of the
[author-decision record](pre_g9b_r6_plus_e3_author_decisions_record.md) prevail
over the summary of the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e3-iso-a-border-and-export-area.prompt.md).

| Identity | Value |
|---|---|
| published implementation base `P_R6PLUS_E3_DESIGN` (`main` at the entry gate) | `ff56099184544e5988c63e0ea3339e3d0fe01fac`, tree `1cdd1e986df785f5f16e7887300d9034e5e4a677` |
| Stage A documentary reconciliation (local, not published) | `3a577ea44a0bc93492a0d74c6f6c68b561b3ff63`, tree `1cc021a3d9e26850c5e0546e4ee89b036332525a` |
| Stage A checkpoint | `STATIC` `verification-08557d0523e64bef9ee92e9a6448f291` `ACCEPTED` / `COMPLETE` (3/3; standing diagnostics only) |
| local branch (not pushed; not identity) | `phase/pre-g9b-r6-plus-e3-iso-a-border` |

The implementation is one commit on top of the Stage A commit: the candidate
commit that contains this report. No push, merge, tag or release was made.

## 2. What changed

### 2.1 Shared kernel

- `org.geocedg.common.kernel.sheet.IsoASheet`: ISO 216 integer table, margins
  20/10/10/10 mm, exact-integer frame validity, the canonical conversion
  `conv(L) = ((L·b)/a)·u`, the capture `u = fl(fl(10^-3)/fb(c))`, the
  representability and reliability classifications, the gcd label, and the
  physical-size and scale-label coherence with its uncertainty bound.
- `AlgoIsoABorder`: the three fixed-arity outputs `PAPER`, `FRAME`, `LABEL`
  (closed `GeoPolyLine`, closed `GeoPolyLine`, `GeoText`), geometry only in
  `compute()`, every degeneration of the specification §15 as undefined
  outputs with kept identities, the initial presentation (paper hidden and
  dotted, frame solid black thickness 3, label black), `ownerOf`.
- `CmdIsoABorder` (exactly seven typed arguments), `Commands.IsoABorder` in
  `TABLE_GEOMETRY`, the dispatcher case and processor creation,
  `EuclidianConstants.MODE_ISO_A_BORDER = 144` with `IsoABorder.Tool`; command
  and menu bundles (EN, ES `MarcoISOA` / "Marco ISO A").
- `GeometryExportArea.Producer` gains `ISO_A_SELECTION` and `ISO_A_BORDER`.

### 2.2 Desktop

- `GeoCeDGIsoABorderTool` and `GeoCeDGIsoABorderPrompt`: the unit gate, the
  dialog of the specification §12 (local presets, frame default and
  impossibility, show label), one-shot capture of `u` and `a:b`, one undo
  point, cleanup of a click-created point, the activation question.
- `GeoCeDGEuclidianController`: mode 144 picks or creates the point, issues the
  sheet and returns to Move after renewing the mode-start state (§3).
- `ExportArea.Source` and `ExportAreaSession.Producer` gain both producers;
  `ExportAreaSession` stores `ISO_A_SELECTION` values and holds the
  identity-only `ISO_A_BORDER` link, validated at every resolve and on every
  Graphics 1 paint, dropped when stale and reported as released.
- `AppGeoCeDG`: activation with the coherence warning, `ISO_A_SELECTION`,
  "Use selected ISO A border", the two coherence states, "Use sheet scale" and
  the sheet status; `GeoCeDGStatusBar` (`sheet-coherence` segment) and
  `GeoCeDGExportScalePresentation` (sheet notice in the export-scale control of
  the picture, print and LaTeX dialogs); `GeoCeDGDxfExportController` maps both
  sources.
- `GeoCeDGActionRegistry`: the two File actions; the unit gate through
  `unavailableReason` for the tool action and `export.area.iso-a`.
- Profile: 127 → 130 actions (`presentation.iso-a-border` menu-only in
  `construction-annotations-media`, `export.area.iso-a` and
  `export.area.use-iso-a-border` in `file-export-area`), localized texts, the
  `GeoCeDGProfile` count pin, the `workspace-profile-validation.ps1` allow-list.
- `GeoCeDGToolImageResource`: explicit reuse of the inherited rectangle
  artwork `mode_shaperectangle` for mode 144; no new asset.

### 2.3 Documents and verification data

- Specification [`geocedg/specs/sheets/iso-a-border.md`](../../geocedg/specs/sheets/iso-a-border.md)
  (`PROPOSED`), transcribed from the approved design §2–§15 with an
  implementation record (§16); [ADR 0036](../adr/0036-iso-a-border-representation-and-export-area-producers.md)
  (`PROPOSED`).
- Proposed amendments of the producer lists in
  [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
  and [`geometry-export-foundation.md`](../../geocedg/specs/export/geometry-export-foundation.md),
  and the catalog paragraph of [`cedg-workspaces.md`](../../geocedg/specs/ui/cedg-workspaces.md),
  each marked as a proposal of this candidate.
- EN and ES user guides, section 4.7 plus the menu table, the export-area
  rules, the limitations and the reference tables; outline pins 103 → 104
  entries and 86 → 87 level-3 headings.
- `docs/upstream/modified-files.yml`: nine added and 37 extended records.
- GGBScript matrix fixture `iso-a-border`, six rows `IsoABorder#1` and 14
  probes; ADR 0031 pins; regenerated R5-B fingerprint fixture; phase
  registration, JUnit inventory and catalog pins (§10).

## 3. Deviations and decisions taken during implementation

No semantic deviation from the approved design. Resolved implementation
details, all inside the approved scope:

1. **Mode change after creation.** The first tool run lost the sheet: the
   controller returned to Move inside the tool step, before the host stored the
   mode-start state, and the inherited `initNewMode` →
   `restoreStateForInitNewMode` removed every element created since the mode
   started. The controller now returns to Move only after the step and after
   `storeStateForModeStarting()`; the Desktop test covers click creation, one
   undo point and undo/redo.
2. **Unit gate presentation.** The design (§12.1) and the prompt require the
   action to be unavailable with its reason. The tool action and
   `export.area.iso-a` are disabled through
   `GeoCeDGActionRegistry.unavailableReason` (tooltip = reason); an invocation
   shows the reason with **Open Document Units…**; a valid `usm` enables them;
   a `usm` whose millimetre factor overflows gives the "not representable"
   reason.
3. **Literal of the captured factor.** The tool writes `u` as its shortest
   round-trip decimal (`1.0` for `mm`, `0.1`, `0.001`, `0.03937007874015748`),
   in plain notation when that form has an exponent (subnormal `1e-308`), so
   the literal parses back to the identical binary64; the design example
   `a5="1"` is the typed form.
4. **Byte-stable reopen.** The reader gives every polyline
   `visibleInView3D = false`; the algorithm sets the same state at creation,
   so a saved document re-saves byte-identically after reopening.
5. **Classic 3D application.** Classic normalizes the 3D visibility flags of
   every object on its first reload (also of the reference point); the Classic
   test compares the command block and the stability of the XML after the
   first reload, not the first save.
6. **Link staleness signal.** No construction-rebuild event exists; the link
   is validated by an identity scan of `getGeoSetConstructionOrder()` at every
   resolve and on every Graphics 1 paint (`isInConstructionList` is not
   reliable, `K9`). An undefined but live `PAPER` keeps the link and makes the
   producer unavailable.
7. **Icon.** Mode 144 had no artwork and the host logged a missing PNG; it
   reuses the inherited rectangle artwork explicitly (A-1 precedent).

## 4. Numerical contract evidence

| Obligation | Evidence (`PreG9BR6PlusE3IsoABorderTest`) |
|---|---|
| A — bit-exact evaluation | `conversionIsBitIdenticalToTheCanonicalExpression` (every ISO side, margin, frame side and label offset × scales × units, bit for bit against `((L*b)/a)*u`); `equivalentScalePairsAreBitIdentical` (`2:100` = `1:50`); `numericReproducibilityAcrossSaveReopenAndUndo`; `determinism` |
| B — `< 2^-51.99` against the captured `u` | `numericNormalBoundsAgainstExactReferences`: exact `BigDecimal` reference from the exact binary64 inputs, never the binary64 expression, for built-in units and scales including `1:10^9` and `10^9:1` |
| C — `< 2^-50` against the physical definition | same test against exact SI decimals; `numericCaptureError`: `u` equals `fl(fl(10^-3)/fb(c))` bit for bit, capture error within the table of the specification §7.4 |
| subnormal: defined, `UNGUARANTEED` | `numericSubnormalIsDefinedButUnguaranteed` (subnormal `u` with a normal product; normal `u` with a subnormal product) |
| extreme `usm` | `numericExtremeUsmFactors`: `k = 10^300` normal, `10^305` subnormal (`UNGUARANTEED`), `10^-311` normal, `10^-313` refused (`∞`), nothing clamped |
| not representable | `numericOverflowAndUnderflowAreUndefinedAndRecover`; `numericCoordinateAbsorption` (`P = (10^20, 0)` absorbed → undefined; `P = (10^15, 10^15)` defined, physical coherence `NOT_DETERMINABLE`) |
| coherence | `coherenceExamplesAreTwoIndependentStates` (the four examples of §9); `coherenceIsNeverClaimedWhenUndeterminable` |

Desktop capture (`PreG9BR6PlusE3IsoABorderDesktopTest.creationTimeCaptureForEveryUnit`):
`cm` → `u = 0.1`, `m` → `0.001`, `usm` inch → `0.03937007874015748`
(`GUARANTEED`); `usm` `k = 10^305` → `u = 1.0E-308` (`UNGUARANTEED`); every
case reopens with bit-identical paper bounds, and later unit or scale changes
change nothing. Reference case: A3 landscape `mm` 1:50 → `21000 × 14850`
exactly, label `A3 — 1:50`, anchor `(x_P + 20500, y_P − 14350)`.

## 5. ADR 0031 gate and the regenerated fingerprint fixture

The gate inventory moves exactly as declared: 566 → 567 stored names,
515 → 516 with an English head, 488 → 489 displayed, in both languages and
both table states; the negative control (French retarget) still fails closed.

The R5-B fixture was regenerated by a reconstruction of the original R5-B
probe (the probe itself is not committed; R5-B report §7): a scratch JUnit
probe outside the tree, using the canonical serializations of
`PreG9BR5BFingerprints` exactly as the gate tests compute them, run on the
candidate source tree. For every regime-independent summary (`tokenUniverse`,
`scriptResolution`, `xmlExactResolution`, the four `reverseTables`, the four
`userResolution`, `syntax.en.full`) and for `dictionaries.en.algebra`, the
candidate value with the lines naming `IsoABorder` or `MarcoISOA` removed
equals the committed E2 value (1, 2 or 2 lines per summary). The Classic
Spanish syntax without the `IsoABorder` line equals the committed
`syntax.es.text`, and the committed `syntax.es.full` is the hash of that text.
The fixture then gains only the additive entries: `commandNames.en.IsoABorder`,
`IsoABorder` in `en.algebra`, `en.cas`, `en.tables.00`, `MarcoISOA` in
`es.algebra` and `es.cas`, the `IsoABorder` entry of `syntax.es.text`, and the
new summary hashes; every display, script, error and historical-document
block is unchanged.

## 6. Tests

| Obligation | Evidence |
|---|---|
| `T-E3-GEOMETRY`, `T-E3-FRAME`, `T-E3-CONVERSION`, `T-E3-NUMERIC`, `T-E3-SIGNATURE`, `T-E3-DYNAMICS`, `T-E3-UNITS` (kernel), `T-E3-DEGENERACY`, `T-E3-SERIALIZATION` (kernel), `T-E3-PRESENTATION` (kernel) | `PreG9BR6PlusE3IsoABorderTest` (23 tests) |
| `T-E3-CLASSIC` | `PreG9BR6PlusE3IsoABorderClassicTest` (Classic application: parse, evaluate, persist, reopen); Desktop `classicSurfaceHasNoSheetGui` (no mode 144 in the Classic toolbar, ungated command); the ES alias in `englishAndSpanishNames` |
| `T-E3-COMMANDS` | `GeoCeDGCommandsTest.cmdIsoABorder`, `SelfTest`, `SyntaxLocalizationTest`, `CommandsValidationTest`, `CommandDispatcherTest`; `PreG9BR5BCanonicalCommandGateTest`, `PreG9BR5BCanonicalDisplayTest`, `PreG9BR5BR1RuntimeLanguageSwitchTest` |
| `T-E3-TOOL`, `T-E3-UNITS` (gate), `T-E3-COHERENCE`, `T-E3-PRODUCERS`, `T-E3-EXPORT`, `T-E3-PRESENTATION`, `T-E3-PROFILE`, `T-E3-LEGACY`, `T-E3-SERIALIZATION` (Desktop) | `PreG9BR6PlusE3IsoABorderDesktopTest` (13 scenarios, each in a child JVM with a thread-dump watchdog): profile and menu-only placement; tool with one undo point and the activation question; unit gate (disabled action, reason, Document Units route, `usm`, overflowing factor, typed command `NOT_DETERMINABLE`); capture for every unit with save/reopen; cancel and frame rules (A10 portrait); link lifecycle (live, undefined-but-live, deletion, undo rebuild by identity, New); producer precedence and `ISO_A_SELECTION`; coherence, "Use sheet scale" and the activation warning; physical export (PDF 420 × 297 mm at 1:50 and 210 mm at 1:100, SVG 42 cm, PNG size of the paper, PGF bounds of the paper, DXF producer `ISO_A_BORDER` with the paper bounds); indicators; Classic surface; legacy macros with the `Templatev7.ggb` hash; EN/ES names and syntax |
| `T-E3-GGBSCRIPT` | `PreG9BR6CapabilityMatrixTest`: completeness with `IsoABorder` in the derived inventory, the negative control, `isoABorderRows` (six rows in a child JVM), `lookupProbesEnglish` / `lookupProbesSpanish` |
| `T-E3-PROFILE` | `GeoCeDGProfileTest`, `G9U1ProfileCompilerTest` (catalog modes 73 → 74, toolbar still 49), `G9U1ActionRegistryTest`, `G9U1WorkspaceSurfaceTest` (File menu order), `G9U1IconReviewTest`, `GeoCeDGToolImageResourceTest`, `PreG9BR6PlusA1LayerWorkspaceTest`, `PreG9BR6PlusE1LCuratedLibraryTest`, `PreG9BR6PlusE2NativeDimensionDesktopTest` |
| `T-E3-GUIDE` | `PreG9BR5AGuideOutlineTest` (104 entries), `PostP1BilingualUserGuideTest`, `PreG9BP1PublicSurfaceTest` |
| `T-SMOKE` | pending (author), §15 |

Complete suites before freezing: `:shared:common-jre:test` 6 988 tests,
0 failures, 10 skipped; the executed producer evidence of every refreshed
selection is listed in §10 and in the evidence JSON.

## 7. GGBScript capability matrix

Fixture `iso-a-border` (`A=(1,1)`, recompute driver `A`); six rows
`IsoABorder#1` (Algebra input, GGBScript, GGBScript `Execute` × EN/ES) with
`X=IsoABorder(A,3,true,1,50,1,true)` — the single label names `PAPER`; 14
lookup probes (case variant, foreign alias `MarcoISOA` in EN, localized alias
in ES). The derived inventory includes `IsoABorder` because its processor
references `org.geocedg.` and it is registered as added in the provenance
manifest. Static-contract pin of the matrix re-established.

## 8. Profile, allow-list and icon

130 actions; the tool has no toolbar entry (toolbar modes stay 49; catalog
modes 73 → 74). The `workspace-profile-validation.ps1` allow-list gains the
three E3 action ids; its static-contract pin was re-established. No new icon
asset; the asset manifest is unchanged.

## 9. Export

The two producers feed the single `ExportArea` authority and therefore every
picture route, print, clipboard, command-line export, `ExportImage`, the
LaTeX exporters and the DXF context unchanged; the manifest reports
`iso_a_selection` / `iso_a_border`. DXF writes the paper boundary and the frame
as `LWPOLYLINE` with the closing vertex repeated and writes no label text
(durable limitation).

## 10. Verification data

- Phase `PRE-G9B-R6-PLUS-E3` registered with `compile.shared.semantic`,
  `compile.desktop.semantic`, `workspace-profile.product`,
  `junit.shared.pre-g9b-r6-plus-e3.semantic`,
  `junit.desktop.pre-g9b-r6-plus-e3.semantic`,
  `static.ggbscript-matrix-inputs.semantic` and `ggbscript-matrix.product`,
  and four new nodes (two producers, two projections).
- JUnit inventory regenerated with the official updater from executed
  producer evidence: `discovery.shared`, `discovery.desktop` (dry runs) and
  the executed selections `final.shared`, `final.desktop`,
  `pre-g9b-r4.shared`, `pre-g9b-r6.shared`, `pre-g9b-r6.desktop`,
  `pre-g9b-r6-plus-a1.shared`, `pre-g9b-r6-plus-b.desktop`,
  `pre-g9b-r6-plus-c.desktop`, `pre-g9b-r6-plus-e2.shared`,
  `pre-g9b-r6-plus-e2.desktop`, `pre-g9b-r6-plus-e3.shared` and
  `pre-g9b-r6-plus-e3.desktop` — the same set E2 refreshed, because
  `GeoCeDGCommandsTest` and `PreG9BR6CapabilityMatrixTest` gained a method.
  Counts: discovery.shared 6071, discovery.desktop 1985, final.shared 6988, final.desktop 1979, pre-g9b-r4.shared 7 → 8, pre-g9b-r6.shared 13 → 14, pre-g9b-r6.desktop 56 → 57, pre-g9b-r6-plus-a1.shared 19 → 20, pre-g9b-r6-plus-b.desktop 243 → 244, pre-g9b-r6-plus-c.desktop 271 → 272, pre-g9b-r6-plus-e2.shared 75 → 76, pre-g9b-r6-plus-e2.desktop 282 → 283, pre-g9b-r6-plus-e3.shared 39, pre-g9b-r6-plus-e3.desktop 325 identities; the first executed runs of the Desktop selections exposed three existing pins of the status-bar order and of the File export surface (`PostE2P1R2SmokeFollowUpTest`, `PreG9BR6PlusD1DocumentUnitsTest`, `PreG9BR6PlusBExportSurfaceTest`), updated for the hidden `sheet-coherence` segment and the two File actions, after which every refreshed selection passed.
- Catalog pins: `junit_inventory`, `static_contracts`; registry shape: 58 → 59
  phase selections; `verification-final-coverage.Tests.ps1` pins moved
  accordingly.

## 11. Checks

| Check | Result |
|---|---|
| focused Gradle runs | all E3, gate, matrix, profile, guide and export classes pass (§6) |
| checkstyle (shared main/test, Desktop main/test) | no finding in an E3 file; one pre-existing warning in `PreG9BR6PlusA1HiddenLayerTest` |
| development `STATIC` (uncommitted) | `ACCEPTED` / `COMPLETE`, standing governance diagnostic only |
| `INFRA_UNIT` | `ACCEPTED` / `COMPLETE`, 0 diagnostic findings (`verification-bea096aab82e4737982cf1193863f387`, uncommitted development run) |
| `git diff --check` | clean |

## 12. Observations

| Id | State |
|---|---|
| `OBS-R6PLUS-E3-LEGACY-DOC-ES-LOOKUP` | OPEN — OUTSIDE E3 — NOT CHARACTERIZED FURTHER: in the Desktop test harness, after `loadXML` of `Templatev7.ggb` and `setLanguage("es")`, no Spanish command name resolves (`Punto`, `CotaAlineada` and `MarcoISOA` alike; `getReverseCommand` returns null); in a fresh application they resolve. Independent of E3; recorded for a separate characterization |
| standing verifier diagnostics | governance `FINDING` (recovery protocol) and historical-consistency `UNAVAILABLE` appear on every `STATIC`/`INTEGRATION` run; pre-existing, not acceptance |
| `ENH-R6PLUS-E3-ISOA-LABEL-SCALE-COHERENCE` | implemented by this candidate (scale-label coherence, "Use sheet scale"); disposition belongs to the author closeout |

## 13. Known limitations

DXF writes no sheet label; versions that do not know `IsoABorder` lose sheets
on re-save; no title block, sheet set, paper space or viewport; no coherence
indicator for an `ISO_A_SELECTION` rectangle (`OTQ-E3-3`); a frameless sheet
is moved through its point or by showing its paper (`OTQ-E3-4`); the frame of
a disabled or impossible frame is undefined and auxiliary (`OTQ-E3-2`).

## 14. Classic check (separate from the author smoke)

In the Classic diagnostic session of this build: type
`P=(0,0)` and `IsoABorder(P,3,true,1,50,1,true)`; three outputs appear (paper
hidden, frame, label `A3 — 1:50`); with Spanish UI `MarcoISOA(P,4,false,1,1,1,false)`
works and the Algebra definition reads `IsoABorder(…)`; save as `.ggb` and
reopen — the command and outputs are restored; the Classic toolbar and menus
show no ISO A tool, no export-area actions and no sheet indicator.

## 15. Author smoke checklist (EN and ES)

1. Options → Document units: construction unit `mm`. Construction →
   Annotations and media → **ISO A Border**; click an empty position; choose
   A3, landscape, 1:50, inner frame on, show label: the paper boundary and
   every corner point are hidden; the frame (20/10/10/10 mm) and the label
   `A3 — 1:50` are shown. Repeat with the Spanish UI (**Marco ISO A**).
2. Create an A5 sheet: the frame is off by default and can be enabled.
3. Choose A10 portrait: the frame option is unavailable with its reason.
4. Drag the frame: the whole sheet moves.
5. Answer **Yes** to "Use this sheet as the export area?" and export PDF at
   session scale 1:50: the page is 420 × 297 mm.
6. Set the session scale to 1:100: the status bar and the export dialogs show
   the page size 210 × 148.5 mm, both states and **Use sheet scale**; use it.
7. Change the construction unit to `cm`: the physical state changes, the scale
   state stays, the geometry does not change, and the message names the unit
   change as the cause.
8. Undo and redo: the link is released, the export area falls back to the
   default rule and the status bar says so; link again with **File → Export
   area → Use selected ISO A border**.
9. Save and reopen: the sheet is restored; the export area is reset.
10. With no construction unit the tool and **Define ISO A export area…** are
    unavailable with the reason; with a `usm` (for example inch) they work.
11. **Define ISO A export area…**: an ISO-sized area at the session scale.
12. Export SVG, EMF, PNG, PSTricks, PGF/TikZ, Asymptote and DXF of a linked
    sheet: the area is the paper; DXF reports `iso_a_border` and has no label
    text.

## 16. Changed paths

See the evidence JSON `changedPaths` (product, tests, verification data and
documents by layer).

## 17. Final state

```text
PRE-G9B-R6-plus-E3      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
AUTOMATED VERIFICATION  = reported outside this file (PHASE + INTEGRATION on the candidate)
AUTHOR_SMOKE            = PENDING (required)
PUBLICATION             = NOT AUTHORIZED
selfApproved = false, authorApproved = false (candidate), passClaimed = false
```

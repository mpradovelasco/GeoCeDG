# PRE-G9B-R6-plus-E2 — author smoke follow-up: legacy document macros and dimension presentation

```text
TECHNICAL_CANDIDATE_STATE = REVISED CANDIDATE, FROZEN with the commit that contains this report
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                     = PRE-G9B-R6-plus-E2 (author smoke follow-up A/B)
ORIGINAL CANDIDATE        = T_R6PLUS_E2 878ff66b8839aeee44dfe1e557ac37b7c349f74b
                            tree e096b857e1ce7e1e959c0c637f3e8b3b87cd82fe
                            PHASE verification-47c1a96df96440e59db84dd5098f1b66 ACCEPTED
                            INTEGRATION verification-e1d28044ba814ed8ab216b676e1cbe06 ACCEPTED
                            (unchanged; historical evidence)
AUTHOR DECISIONS          = 5a55ba241 (record of 2026-10-08)
VERIFICATION_CLASS        = INTEGRATED_PHASE
FROZEN_ACCEPTANCE         = tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-E2
                            tools/agent/verify.ps1 -Profile INTEGRATION
                            (both on the same exact revised commit and tree)
FINAL                     = NOT RUN (not required by INTEGRATED_PHASE)

PART A                    = A-INTENTIONAL-PROFILE-BEHAVIOR (accepted by the author)
                            correction performed: NONE in product code; user-guide
                            discrepancy corrected (authorized documentary scope)
PART B1                   = dimension segment thickness 2; creation preference
PART B2                   = dimension unit suffix policy; geocedgUnits version 2
SERIALIZATION CHANGE      = BOUNDED: geocedgUnits version 2 (one Boolean attribute)
GUIDE_IMPACT              = UPDATED (EN/ES 2.4, 4.6, 9.5, 13.2)
AUTHOR_SMOKE              = PASS WITH OBSERVATIONS — ORIGINAL E2
AUTHOR_REVIEW             = PENDING — REVISED CANDIDATE (focused re-smoke of B1/B2, checklist below)
selfApproved              = false
authorApproved            = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

This artifact records only facts fixed when it was written. It carries no
author-approval field: a later author decision naming the exact revised commit
is the sole authority for approval. The revised commit cannot name itself, so
its identity and the two acceptance runs made on it are reported outside this
file. The machine-readable mirror is
[`pre-g9b-r6-plus-e2-smoke-followup-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e2-smoke-followup-evidence.json).
The governing instructions are in the
[author decisions record](pre_g9b_r6_plus_e2_smoke_followup_author_decisions_record.md).

## Part A — legacy document macros (characterization only)

### A.1 Question and method

The author's smoke reported that the macros embedded in
`models/legacy/template-v7/original/Templatev7.ggb` seemed unavailable in
GeoCeDG. The behavior was compared on Classic GeoGebra, the published base
`P_R6PLUS_E1_P_X1` `dc63b0e5` (a detached scratch worktree, removed afterwards)
and the original `E2` candidate `878ff66b`, on byte-identical copies of the
template (SHA-256 `f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113`),
with isolated preferences. Two probes ran from the session scratchpad and never
entered the tree: a headless kernel probe and a windowed probe that starts the
real `GeoCeDG.main` and `GeoGebra3D.main` entry points, opens the file through
the real open route and walks the real menus and dialogs.

### A.2 Evidence (identical on the base and on `878ff66b`)

| Observation | GeoCeDG product | Classic |
|---|---|---|
| macros read and registered | 24 of 24 | 24 of 24 |
| document command authority bound | 24 of 24 | not applicable (0) |
| invocation by command name (`CirclebyD`, `SquarebyDiagonal`, `directDimension`, `axisDimension`) | works; outputs from `AlgoMacro` | works |
| stored document toolbar definition (macro modes `100001`–`100024`) | read, identical string | read, identical string |
| toolbar buttons that name a macro | none | 7 (`SymmSymbol`, `PoliLineVisibility`, `postLocus`, `EllipseAxis`, `directDimension`, `IFPositiveSelectPoint`, `sheetISOAnLand`) |
| **User tools** menu | "Manage user tools…", "No installed user tools" | — |
| route to the macros | Manage user tools… → Document tools (local only)… lists 24 | Tools → Manage Tools… lists 24 |
| save as `.cedg` and reopen | 24 macros kept and executable | — |

After save and reopen five macro digests differ (`SplineLength`,
`circArcbyAngle`, `dummyRotate`, `listLength12`, `postLocus`); each difference
is a serializer normalization of the macro construction (`labelMode val="2"`,
`opacity="100"`), identical on the base. The native `AlignedDimension` is
refused on the base (no such command) and coexists with the legacy macros on
`878ff66b`.

### A.3 Classification and root cause

```text
OBS-R6PLUS-E2-SMOKE-LEGACY-DOCUMENT-MACROS = A-INTENTIONAL-PROFILE-BEHAVIOR (accepted)
ROOT CAUSE      = the GeoCeDG toolbar is compiled from the product profile; a document's
                  toolbar and perspective are presentation evidence only (ADR 0012;
                  cedg-workspaces.md), and the User tools menu lists only the installed
                  library and the bundled catalog. Embedded macros are therefore loaded,
                  registered, executable and preserved, but not offered as toolbar or
                  menu tools.
AFFECTED LAYER  = product presentation policy (toolbar and User tools menu); not the
                  kernel, not macro registration, not command resolution, not E2
E2 REGRESSION   = NO (identical on the published base)
CORRECTION      = NONE in product code (characterization only)
```

A separate observation remains open for the author:
`OBS-R6PLUS-E2-DOCUMENT-MACRO-DISCOVERY-UX` — embedded macros cannot be chosen
interactively as tools, and the guide did not say so.

### A.4 Documentary correction (authorized scope)

The user guide §13.2 described document tools without saying that embedded
macros appear neither in the toolbar nor in the User tools menu, or how to run
and find them. Both editions now say so: run a document macro by its command
name in Algebra Input; list the macros through **Manage user tools… → Document
tools (local only)…**; interactive selection as a toolbar tool is not
available. No macro registration, command resolution, toolbar policy or kernel
change was made.

### A.5 Recommended correction (not authorized)

An enhancement that offers the macros of the open document as interactive tools
(for example a "Document tools" submenu of User tools whose entries activate the
macro mode, without reconstructing the legacy toolbar) would close the UX
observation; it requires a separate author authorization.

## Part B — dimension presentation (implemented)

### B.1 B1: initial line thickness

- `AlgoNativeDimension.INITIAL_LINE_THICKNESS = 2` is applied to the dimension
  line and both extension lines when the algorithm creates them, beside the
  existing arrow endings; typed commands therefore start at 2.
- The two tools apply the creation preference
  `geocedg.dimensions.initial-line-thickness.v1` (GeoCeDG properties store; 1 to
  the GeoGebra maximum; default 2; an invalid value means the default) once,
  when they create the dimension.
- Each segment keeps and serializes its own `lineStyle`; a load restores the
  saved style over the default; changing the preference never touches an
  existing dimension. No argument, geometric rule or dependency changed.
- Preferences → Layout & Presentation → **Dimension presentation** holds the
  spinner (and the B2 default), through one null-returning `AppD` hook hosted by
  `OptionsLayoutD` exactly like the D1 new-document units group; Classic keeps
  the inherited tab.
- Agent interpretation for author review: the author's answer did not settle
  whether typed commands follow the tool preference. They start at 2, the
  specified default; only the tools read the preference.

### B.2 B2: dimension unit suffix policy

- `UnitState` gains the document presentation field "dimension unit suffix"
  (shown or hidden), kept by every unit operation. `EMPTY` is "no unit metadata
  and shown"; `hasUnitMetadata` separates the unit metadata from the policy.
- `DimensionPresentation` omits `" " + suffix(effP)` (and the suffix of the
  failure marker) when the policy is hidden. The number, the unspecified case,
  the conversion and the geometry are unchanged; a policy change refreshes the
  texts through the existing D1 listener without a geometric recompute.
- Persistence: `geocedgUnits` version 2 is version 1 plus the mandatory
  `dimensionUnitSuffix` attribute. The writer emits version 2 only for the
  hidden policy (`dimensionUnitSuffix="hidden"`, last); the shown policy keeps
  the exact version-1 element. The reader maps version 1 and a missing element to
  shown (historical behavior), rejects version 1 with the attribute and version 2
  without it or with another value, and fails closed on version 3.
- Defaults: new blank documents take the preference
  `geocedg.units.new-document-dimension-unit-suffix.v1` (unset means hidden);
  opened documents keep their own policy. The policy alone is never
  save-relevant (`DQ-D1-5` applies to unit metadata only), so an untouched new
  document stays saved.
- The document units dialog gains **Show the unit on dimension values**; its
  commit, including a policy change, stays one undo point.
- No new `GeoElement`, XML element family, command signature, global `GeoText`
  change or geometric-unit dependency.

### B.3 Normative amendments (history preserved)

- [Unit-system specification](../../geocedg/specs/units/unit-system.md): new
  §20 "Amendment 1.1" (state, presentation, version-2 grammar, routes, defaults,
  matrix rows, consumer contract, traceability) with amendment pointers in §8.2,
  §8.3, §8.7, §8.9, §10, §14.2, §17 and §18.3. The approved `1.0` text is
  unchanged; §20 is `PROPOSED` and binds only this revised candidate.
- [ADR 0032](../adr/0032-unit-system-semantics-and-persistence-ownership.md):
  appended amendment note; the approved decision is unchanged.
- [Native-dimensions specification](../../geocedg/specs/dimensions/native-dimensions.md)
  `1.1` (§4.2, §6.4, §8.5, §9.1) and an
  [ADR 0034](../adr/0034-native-dimension-representation-and-presentation-seams.md)
  revision note; both stay `PROPOSED`.

### B.4 Forward compatibility (for author review)

A document with the hidden policy carries a version-2 element. Builds that
implement only version 1 — `D1` through the original `E2` candidate — refuse it
as a newer version (fail closed, by the approved §8.7). Because the hidden
policy is the default, an untouched new document saved by the revised candidate
is refused by those builds, also when it has no unit metadata. Documents with
the shown policy, legacy documents and documents written before this revision
keep their version-1 bytes and stay readable by every `D1`-or-later build.
Builds before `D1` and Classic ignore the element and lose it on re-save, as
already recorded in §8.9.

## Tests

| Class | Methods | Subject |
|---|---|---|
| `PreG9BR6PlusE2PresentationFollowUpTest` (shared) | 12 | thickness of both commands; individual serialized styles; suffix for every effective unit and `usm` with and without symbol; unspecified; failure marker; policy kept by unit operations; one undo step and redo; version-2 writer and reader; version-1 byte identity; version-2 malformations; save relevance |
| `PreG9BR6PlusE2PresentationFollowUpDesktopTest` | 8 | tools and the thickness preference; existing dimensions unchanged; saved style precedence; preference store and invalid values; hidden default for new documents; dialog toggle as one undo step; save and reopen; historical E2 documents; PGF text; panel stores preferences only |

Changed expectations, without weakening: `PreG9BR6PlusD1UnitXmlTest`,
`PreG9BR6PlusD1UnitLoadTest` and `PreG9BR6PlusA2HiddenLayerPersistenceTest`
move their "newer version" cases from 2 to 3 because version 2 now exists;
`PreG9BR6PlusD1DocumentUnitsTest`, `PreG9BR6PlusD1UnitLoadTest` and
`PreG9BR6PlusA2NewDocumentBaselineTest` expect the hidden policy in new-document
defaults; two undo and round-trip tests whose subject is the unit operations pin
the shown default through the test preference store, so they keep asserting
exactly one undo point and the same round trip.

## Verification data

- Full suites on the final code (512 MB Desktop test heap unchanged): shared
  6963 tests, 0 failures, 10 skipped; Desktop 1904 tests, 0 failures, 1 skipped;
  no OutOfMemoryError.
- The E2 selections gain the two new classes; the Desktop E2 selection also runs
  `PreG9BR6PlusD1UnitLoadTest`, `PreG9BR6PlusA2NewDocumentBaselineTest` and
  `PreG9BR6PlusA2HiddenLayerPersistenceTest`. Impact paths of the E2 producers add
  `UnitState`, `UnitStateXml`, `AppD`, `OptionsLayoutD` and the changed tests.
- JUnit inventory refreshed in-session through the official updater from the
  official producers on the final code, all passing: discovery dry runs (shared
  6046, Desktop 1904) and executed `final.shared` 6951 → 6963, `final.desktop`
  1890 → 1898, `pre-g9b-r6-plus-e2.shared` 63 → 75 and
  `pre-g9b-r6-plus-e2.desktop` 242 → 282. Registry `junit_inventory` pin
  `eb1d061c…` → `e8d1f122…`; coverage pins for the two E2 selections; selection
  and phase counts unchanged (44, 51).
- Checkstyle: no finding in a changed file. Upstream boundary: passes; new files
  registered in `docs/upstream/modified-files.yml`, purposes amended for the
  modified ones.

## Changed paths

Shared: `UnitState`, `UnitStateXml`, `DocumentUnitSystem`,
`DimensionPresentation`, `AlgoNativeDimension`. Desktop: new
`GeoCeDGDimensionPreferences`, `GeoCeDGDimensionPresentationPanel`; changed
`GeoCeDGDimensionTools`, `GeoCeDGUnitPreferences`, `GeoCeDGDocumentUnits`,
`GeoCeDGDocumentUnitsPrompt`, `AppGeoCeDG`, `AppD`, `OptionsLayoutD`; profile
texts in `apps/geocedg/application-profile.yml`. Tests as above. Documents:
specifications, ADR notes, EN/ES guides, upstream register, inventory, registry,
coverage pins, roadmap and mini-track status, this report and its evidence JSON.

## Focused author re-smoke checklist (EN and ES)

1. New document, set the construction unit to `mm`, `AlignedDimension` tool: the three
   segments are thickness 2; the value has no unit.
2. Options → GeoCeDG options → Document units…: tick **Show the unit on
   dimension values**; the text gains the unit; one Undo removes it again.
3. Save, reopen: the choice is kept; save with the unit hidden, reopen: hidden.
4. Preferences → Layout & Presentation → Dimension presentation: set the initial
   thickness to 5; a new dimension from the tool uses 5; the earlier dimensions
   keep 2; a typed `AlignedDimension` uses 2.
5. Tick the new-document unit option in Preferences; File → New: dimensions show
   the unit; the current document did not change.
6. Open a document saved by the original `E2` candidate: its dimensions keep the
   unit.
7. Export PGF/TikZ with the unit hidden and shown: the exported text matches the
   view.
8. Repeat 1–5 in Spanish ("Mostrar la unidad en los valores de las cotas",
   "Presentación de cotas").

## Final state

```text
A classification          = A-INTENTIONAL-PROFILE-BEHAVIOR
A root cause              = profile-compiled toolbar and library-only User tools menu
A affected layer          = product presentation policy
A correction performed    = NONE — characterization only (guide clarified)
A recommended correction  = interactive document-tool entry; separate authorization
B lineThickness default   = 2
B lineThickness           = configurable (creation preference, new dimensions only)
B unit suffix default     = OFF for new documents; ON for documents without a policy
B unit suffix             = configurable per document; preference for new documents
B unit conversion         = UNCHANGED
B numeric semantics       = UNCHANGED
B geometry and DAG        = UNCHANGED
B serialization           = geocedgUnits version 2 (one Boolean attribute, hidden only)
B normative amendment     = unit-system 1.1 §20 PROPOSED; ADR 0032 note; native-dimensions 1.1
AUTHOR_SMOKE              = PASS WITH OBSERVATIONS — ORIGINAL E2
AUTHOR_REVIEW             = PENDING — REVISED CANDIDATE
selfApproved              = false
passClaimed               = false
publication               = NOT AUTHORIZED
```

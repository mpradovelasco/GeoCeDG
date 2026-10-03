# PRE-G9B-R6-plus-F3 author-decision record

```text
RECORD_KIND              = AUTHOR_DECISIONS (planning: debt assignment)
DECISION_DATE            = 2026-10-03
DECIDED_ON_BASE          = 2941ddf2f2712340e831d292bdc2687b879850d6
                           tree 158184b150a42e6dcae2946c39620d92cbfcd888
                           (P_R6PLUS_D1, published D1 closeout)

PRE-G9B-R6-plus-F3       = PLANNED — NOT AUTHORIZED
selfApproved             = false
authorApproved           = false   (no F3 candidate exists)
implementationAuthorized = false   (F3, A-2 and every later subphase)
passClaimed              = false
productPhaseEffect       = NONE
```

This record preserves, versioned, the author planning decision of 2026-10-03
that assigns the recorded debt `DEBT-R6PLUS-FILE-INSERT-SURFACE` to a new
subphase of the mini-track. It is the authority for that assignment. Its
machine-readable mirror is
[`pre-g9b-r6-plus-f3-author-decisions.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-f3-author-decisions.json).

The decision is a planning decision. It **does not authorize the
implementation of `F3`**, and it does not prepare the `F3` implementation
prompt. Implementation needs a later explicit author authorization that names
an exact base. The debt itself, with its nine requirements, is recorded in the
[D1 closeout record](pre_g9b_r6_plus_d1_closeout_record.md); this record does
not restate a different debt and does not amend that frozen record.

## Decisions

| ID | Author decision |
|---|---|
| `F3-D1` | `DEBT-R6PLUS-FILE-INSERT-SURFACE` is assigned to the new subphase **`PRE-G9B-R6-plus-F3` — File / Insert surface and document insertion compatibility**. |
| `F3-D2` | Scope, when authorized: (1) a GeoCeDG **File → Insert** surface; (2) **Insert File** and **Apply Template** move to that surface; (3) both accept `.cedg` and `.ggb`; (4) Insert File keeps its semantics — it imports geometry/construction into the current document, is not Open, does not replace the document, keeps the `D1` provenance and physical-mismatch notice policy, and never scales coordinates by units; (5) Apply Template keeps its semantics — it applies labeling, defaults and styles, imports no geometry, does not replace the `UnitState` and does not apply the template document's units to the target; (6) *OFF file* is no longer presented in the GeoCeDG UI during this mini-track; (7) the internal upstream OFF support is neither removed nor rewritten by this decision; (8) no widening to other formats or to 3D import; (9) `F3` is closed before the global closeout of `PRE-G9B-R6-plus`. |
| `F3-D3` | Target architecture rule: product-specific surface and policy stay in the GeoCeDG Desktop layer where possible; shared and upstream seams are minimal and upstream-identical by default; no new geometry semantics; no document-format change. |
| `F3-D4` | Planning class assignment: `VERIFICATION_CLASS = INTEGRATED_PHASE`, planned acceptance registered `PHASE` + `INTEGRATION`. The class is frozen only when `F3` is authorized (`verification-levels.md` §12.8). |
| `F3-D5` | Dependencies: hard contract dependency `D1 → F3`, because Insert File must preserve the `D1` physical-unit and provenance contract. No dependency is created for `A-2 → F3`, `C → F3` or `F3 → C`. `G` must include the accepted `F3` in its integrated closeout. |
| `F3-D6` | Operational order: `P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`. It is the order the author chose, not a dependency. |

## Characterization at the base

The characterization below was read from the live tree at the decision base,
with every line number re-read. It records the seams a future `F3` contract
must start from. It is **not** an implementation contract, and the future `F3`
preparation re-establishes every citation at its own base. Paths use
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`,
`common/` = `source/shared/common/src/main/java/org/geogebra/common/` and
`common-jre/` = `source/shared/common-jre/src/main/java/org/geogebra/common/jre/`.

| # | Seam | Finding |
|---|---|---|
| 1 | GeoCeDG File menu | The v2 profile has **no** Insert File, Apply Template or OFF action. The `file` menu (`apps/geocedg/application-profile.yml:3545-3565`) holds only `actions` sections; the `file-open` group holds `document.new`, `document.open`, `document.open-recent` and `diagnostic.open-classic` (`:3346-3349`). `document.open` reaches `gui.openFile()` through the registry target `host.document.open` (`geocedg-desktop/GeoCeDGActionRegistry.java:299-301`). |
| 2 | Where the operations live today | Insert File, Apply Template and OFF exist only as file-type filters of the shared upstream Open dialog, `GuiManagerD.openFile` (`desktop/gui/GuiManagerD.java:2002-2015`), dispatched on the chosen filter (`:2038-2098`). GeoCeDG reaches them through `document.open`, and so does the v1 fallback, which uses the upstream `FileMenuD` Open action (`desktop/gui/menubar/FileMenuD.java:113`, `:315-323`); the fallback profile `application-profile-v1.yml` has no menus. |
| 3 | Accepted formats | The Insert and Template filters accept `*.ggb` only (`GuiManagerD.java:2003`, `:2008`) and append `.ggb` to a typed name that does not exist (`:2051`, `:2070`). The GeoCeDG document filter accepts `.cedg` and `.ggb` through `GuiManagerGeoCeDG.getDocumentOpenExtensions` (`geocedg-desktop/GuiManagerGeoCeDG.java:96-99`) and `GeoCeDGDocumentPolicy`. |
| 4 | OFF route | `offFilter` with the unlocalized description `"OFF file"` (`GuiManagerD.java:2012-2015`), the macOS filter list (`:1948-1959`), a typed-name `.off` fallback (`:2163-2166`) and extension dispatch to `loadOffFile` (`:2196-2197`, `:2291-2294`) → `AppD.loadOffFile` (`desktop/main/AppD.java:3025-3046`) → shared `OFFHandler` (`common/geogebra3D/io/OFFHandler.java:40`). By reading only: removing the filter alone would not close the typed-extension route. |
| 5 | Insert File | `AppD.insertFile` (`AppD.java:4734-4787`, `final`) loads the file into a hidden helper from `newAppForTemplateOrInsertFile()` (`:4789-4791`), which `AppGeoCeDG` overrides to create an `AppGeoCeDG` with a GeoCeDG configuration and no new-document defaults (`geocedg-desktop/AppGeoCeDG.java:664-672`), then calls `getCopyPaste().insertFrom(...)` (`AppD.java:4778`). |
| 6 | Apply Template | `AppD.applyTemplate` (`AppD.java:4796-4815`, `final`) loads the template into the same kind of hidden helper and calls the shared `TemplateHelper.applyTemplate` (`common-jre/main/TemplateHelper.java:43-48`): labeling style, construction defaults, styles of the selected or all objects, construction update. It copies no construction element; by code path the target `UnitState` is untouched, since the template's `<geocedgUnits>` is applied only to the helper's own construction. No test asserts it. |
| 7 | Apply Template and layers (inferred, untested) | `TemplateHelper` restyles objects through `ConstructionDefaults.setDefaultVisualStyles(..., isReset = false, ...)`, whose non-reset branch calls `setMaxLayerUsed` (`common/kernel/ConstructionDefaults.java:917-926`, `:998-1001`), which routes through the `A-1` working-layer seam `getLayerForNewObject` (`AppGeoCeDG.java:343-347`). By reading only, Apply Template therefore re-layers every restyled target object (in Classic to the capped highest used layer). `F3` must characterize this before fixing its Apply Template contract; it touches the `A-1` working layer and the `A-2` domain. |
| 8 | `.cedg` and `.ggb` loading in the helper | The helper loads through the transactional native-archive path (`AppGeoCeDG.loadExistingFile` `:605-620` → `AppD.loadExistingFile` `:3074-3087` → `readAndPreflightNativeArchive` `:3144-3161`; `.ggb` is transactional in GeoCeDG, `AppGeoCeDG.java:1061-1064`), so both formats load by code path. No test inserts or applies a `.cedg` from disk. Side effects to characterize (inferred): the helper's load records the file as current and adds it to the shared recent list (`AppD.java:3284`, `:1095`). |
| 9 | `D1` provenance | `GeoCeDGCopyPaste` (`geocedg-desktop/GeoCeDGCopyPaste.java:22`) records the source document's unit state whenever the buffer is replaced and reports at the end of a paste through the upstream-identical hook `CopyPasteD.onPasteCompleted` (`desktop/util/CopyPasteD.java:562-572`, called at `:559`). `insertFrom` (`:644-684`) copies from the helper and pastes with `putdown = true`, so Insert File carries the inserted document's provenance and shows the same non-blocking notice (`AppGeoCeDG.java:254-265`; `DQ-D1-7`). Insert File also replaces the window's copy buffer. |
| 10 | Profile mechanics | A `"kind": "group"` menu entry becomes a nested submenu (`geocedg-desktop/GeoCeDGMenuBar.java:97-103`); File has no submenu today, and `G9U1WorkspaceSurfaceTest` pins its 17 direct items. The action-count pin is `125` (`geocedg-desktop/GeoCeDGProfile.java:340-345`, and the test pins of `G9U1ActionRegistryTest`, `GeoCeDGProfileTest`, `G9U1ProfileCompilerTest`, `G9U1WorkspaceSurfaceTest` and `PreG9BR6PlusA1LayerWorkspaceTest`). The approved-action allow-list of `tools/agent/workspace-profile-validation.ps1` would need the phase-local amendment that the `D1` closeout accepted for `D1` only. |
| 11 | Classic | Classic (`GeoGebra3D`, `App3D` with the default `GuiManagerD`) shows the same four filters in its Open dialog. Because the filters live in the shared `GuiManagerD.openFile`, keeping Classic unchanged needs GeoCeDG-only overrides of upstream-identical hooks. |
| 12 | Documentation and tests | The user guides describe Insert File as a file type of File → Open (`docs/user/geocedg_user_guide_en.md:485-486`, `docs/user/geocedg_user_guide_es.md:508-509`). The `D1` tests call `insertFrom` directly; no test drives the Open filters, `AppD.insertFile`, `AppD.applyTemplate` or OFF end to end. |

## Verification-class check

The planned class `INTEGRATED_PHASE` matches the characterization. `F3`
crosses the GeoCeDG File surface (profile, action registry, the first File
submenu, the action-count and surface pins), the chooser and its routes in the
shared upstream `GuiManagerD`, two distinct document-consuming operations with
their hidden helper and the shared `TemplateHelper`, and the `D1` Insert File
provenance. That is concrete integration coverage. It changes no
serialization, no shared parser rule read on every load and no kernel
geometry, so it does not meet the `GLOBAL_IMPACT` facts of `D1` or `A-2`.

No reclassification is proposed. The class would need author review, and the
future preparation would stop rather than choose another class, if `F3` had to
change shared document serialization or the shared parser, change
`ConstructionDefaults` or `TemplateHelper` semantics for Classic, or change
global verification infrastructure beyond the phase-local allow-list amendment
precedent.

## What stays open

- The `F3` implementation prompt is not prepared; its preparation is a separate
  author instruction.
- The questions the characterization raises for that preparation: the exact
  submenu and action identities; whether the OFF route must also be closed for
  typed `.off` names in the GeoCeDG UI; the hidden helper's effect on the recent
  list; and the re-layering of finding 7 and its interaction with the `A-1`
  working layer and the `A-2` domain.

## Authorization state

```text
PRE-G9B-R6-plus-P0, A-1, B, D0, D1 = PASS — AUTHOR APPROVED — PUBLISHED
PRE-G9B-R6-plus-A-2                = NOT AUTHORIZED   (next operational subphase)
PRE-G9B-R6-plus-C, E1, E2, E3, F1, F2, G = NOT AUTHORIZED
PRE-G9B-R6-plus-F3                 = PLANNED — NOT AUTHORIZED
DEBT-R6PLUS-FILE-INSERT-SURFACE    = ASSIGNED TO PRE-G9B-R6-plus-F3 — NOT AUTHORIZED
                                     (closes with the author approval of F3)
PRE-G9B-R7                         = DESIGNED — NOT AUTHORIZED
                                     BLOCKED UNTIL PRE-G9B-R6-plus IS PASS — AUTHOR APPROVED
G9B                                = NOT AUTHORIZED
```

No push, merge, tag or release is authorized.

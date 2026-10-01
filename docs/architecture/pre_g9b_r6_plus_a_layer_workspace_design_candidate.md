# PRE-G9B-R6-plus-A — layer workspace and status bar: design candidate

- Status: **PROPOSED — CANDIDATE — NOT AUTHOR APPROVED**
- Produced by: `PRE-G9B-R6-plus-P0` (characterization and design only)
- Base: `P_R6PLUS_PLAN` = `babf20c7c03d0454e2da6760bf5d1fc3fa546f6b`, tree
  `3f7abded9163324a3142ad7446fb4e6c85407809`
- Claim vocabulary: **proposed / not normative**. Nothing here is implemented
  or authorized. Subphase `A` needs its own explicit author authorization.
- Evidence: [P0 report](../validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
  findings 1–4 and probe P3

Path abbreviations: `common/` = `source/shared/common/src/main/java/org/geogebra/common/`,
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`.

## 1. Author-fixed scope this candidate serves

An explicit working layer that can be changed at any time; new objects use it;
a working-layer entry in the Move group as a GeoCeDG mode; the working layer
in a status bar; Algebra View `Sort by Layer` kept; layers hidden and shown
without overwriting object visibility:

```text
effectiveVisible(object) = objectVisible(object) AND NOT layerHidden(object.layer)
AUTHOR TARGET            = working layer not restricted by the current 0–9 UX/domain
```

## 2. What the base does today (summary of P0 evidence)

- The domain is `0..9`. `GeoElement.setLayer` clamps silently
  (`common/kernel/geos/GeoElement.java:1001-1018`). Probe P3 confirms
  `setLayer(12) → 9` and `setLayer(-3) → 0`.
- New objects take `min(MAX_LAYERS - 1, app.getMaxLayerUsed())`
  (`common/kernel/ConstructionDefaults.java:907-924`). Macro outputs take
  `getMaxLayerUsed()` uncapped and discard the layer stored in the macro
  (`common/kernel/algos/AlgoMacro.java:291`, `:301`, `:306`).
- Probe P3: `GeoLocusV2` (Locus V2 and Spline V2) is created on layer 0 while
  every other new object takes the current default layer. Paste keeps the
  copied layer. Reload and undo restore stored layers. A dependent numeric
  with no `<layer>` tag (`common/kernel/geos/XMLBuilder.java:139-147`) takes
  the current `getMaxLayerUsed()` on every reload and undo.
- No status bar exists. `AppD.buildApplicationPanel` leaves `SOUTH` of the
  application panel free, and `updateApplicationLayout` calls `removeAll()` on
  the north, south, east and west panels on every layout change
  (`desktop/main/AppD.java:2270-2378`, `:2417-2420`).
- Native toolbar groups accept modes only, and a profile flyout cannot precede
  the first native group (`geocedg-desktop/GeoCeDGToolbarContainer.java:173-178`,
  `:203-208`). The first group, `edit-selection`, holds `construction.move` and
  `construction.move-rotate` (`apps/geocedg/application-profile.yml:3189-3194`).
  The GeoCeDG-owned mode precedent is `MODE_ORDERED_LIST = 140`
  (`common/euclidian/EuclidianConstants.java:465-466`); 141 is free.
- Visibility: `drawableNeeded` is private (`common/euclidian/EuclidianView.java:2227-2240`).
  `isVisibleInThisView` is also read by slope fields and ODE integrals to
  compute geometry (`common/kernel/advanced/AlgoSlopeField.java:212-233`,
  `common/kernel/algos/AlgoIntegralODE.java:151-160`). A drawable is never
  removed when visibility changes and is not re-added unless it was queued
  (`EuclidianView.java:2037-2066`, `:2084-2091`). Painting uses cached drawable
  visibility; hit testing reads the live geo predicate
  (`common/euclidian/HitDetector.java:61-63`). Graphics 2 is not a GeoCeDG view
  class (`desktop/geogebra3D/gui/GuiManager3D.java:257-265`).
- Upstream `ShowLayer`/`HideLayer` write every object's own visibility
  (`common/kernel/scripting/CmdShowHideLayer.java:58-75`).

## 3. Layer-domain extension (author target `AQ-L1`)

### 3.1 Every consumer of the bounded domain

| Consumer | Evidence | Behavior beyond 0..9 if only the clamp were removed |
|---|---|---|
| clamp | `GeoElement.java:1009-1012` | the value is lost |
| `updateMaxLayerUsed` clamp | `common/main/App.java:1282-1290` | the implicit default never exceeds 9 |
| default-path cap | `ConstructionDefaults.java:919-920` | layer `MAX-1` reserved |
| draw order comparator | `common/kernel/geos/DefaultGeoPriorityComparator.java:25-26` | `a - b` overflows only for unbounded ints |
| hit testing | `HitDetector.java:102-116` (`maxlayer = 0`) | negative layers never win a hit |
| `chooseGeo` | `common/euclidian/EuclidianController.java:1344` (`maxLayer = -1`) | negative layers ambiguous |
| selection sentinels | `common/main/SelectionManager.java:376-402` (`-1`, `-2`) | negative layers collide |
| API sentinel | `common/plugin/GgbAPI.java:386-392` (`getLayer` returns `-1`) | negative layers collide |
| `ShowLayer`/`HideLayer`, `setLayerVisible` | `CmdShowHideLayer.java:60-62`; `GgbAPI.java:398-412` | silently ignored |
| Properties combo | `common/gui/dialog/options/model/LayerModel.java:42-57`; `common/properties/impl/objects/LayerProperty.java:49-52` | combo index is the layer |
| Algebra View groups | `desktop/gui/view/algebra/AlgebraViewD.java:520-531` | lexicographic: `10` before `2` |
| 3D renderer | `common/geogebra3D/euclidian3D/openGL/BufferPack.java:218-224`; `common/geogebra3D/main/VertexShader.java:133`, `:152-153` | alpha coding and depth shift grow with the layer |
| XML read | `common/io/ConsElementXMLHandler.java:610-618` | goes through the clamp |
| XML write | `XMLBuilder.java:139-147` | writes any value |
| SVG grouping | `desktop/export/GraphicExportDialog.java:895-907` | `layer<n>` works for any n |
| DXF naming | `GeoElementGeometryExportAdapter.java:284-286` | `GEOCEDG_L<n>` works for any n |

### 3.2 Candidate: a bounded, non-negative, profile-supplied domain

```text
GeoCeDG layer domain = { 0, 1, …, L_MAX }      (integers)
L_MAX                = author decision AQ-L1a  (recommended 99)
Classic / Web        = unchanged (0..9)
negative layers      = not admitted
```

**Scope note for the author.** The authorizing instruction states the target as
"not restricted to the current 0–9 UX/domain"; plan §2 speaks of "an arbitrary
layer number". Any finite `L_MAX` satisfies the first and **narrows** the
second. A bounded domain is recommended for the reasons below, but it is an
interpretation of author-fixed scope that needs explicit author acceptance in
`AQ-L1a`. An unbounded domain is technically possible only with the
comparator, 3D-coding and UI changes the rejected alternative lists.

- **One domain bound, supplied by the application configuration**, not by
  changing the upstream constant: `GeoElement.setLayer`, `App.updateMaxLayerUsed`,
  `CmdShowHideLayer`, `GgbAPI.setLayerVisible` and the property models read a
  single `maxLayer()` of the app configuration (`AppConfig` default 9;
  `AppConfigGeoCeDG` returns `L_MAX`). The Classic diagnostic process, which
  launches upstream `GeoGebra3D` from the same build, keeps 0..9.
- **Algebra View order becomes numeric** (integer compare instead of
  `String.compareTo`).
- **Properties:** a numeric spinner or field instead of an index-coupled combo
  in the GeoCeDG properties panel.
- **3D renderer:** the layer used for alpha packing and depth shift is clamped
  to the renderer's coding range. This is render coding only, not semantics.
- **Comparator, DXF and SVG:** no change is needed for a bounded domain.

Rejected alternatives:

- *Unbounded integers.* They overflow the comparator subtraction, break the 3D
  coding and leave the UI with no bound to validate against.
- *Negative layers.* They collide with three sentinel conventions and are
  unclickable under the hit-test rule.
- *Raising `EuclidianStyleConstants.MAX_LAYERS` globally.* It changes Classic,
  Web and the diagnostic session.
- *A second GeoCeDG "logical layer" attribute mapped onto upstream draw
  layers.* It creates two layer notions and duplicates semantics.

### 3.3 Compatibility consequence

- Legacy documents use 0..9. They load and round-trip byte-identically; no
  migration is needed.
- A document that uses a layer above 9 is read by Classic GeoGebra and by every
  older GeoCeDG build through the clamp, so the value becomes 9 and is lost on
  re-save. This is **forward-compatibility loss for older readers**, not a
  migration of existing documents. It needs an explicit author acceptance
  (`AQ-L1c`); no agent chooses it.
- The `<layer val>` element does not change shape, but its admitted range
  widens in shared serialization.

## 4. Working layer

- **Owner:** Desktop application session (an `AppGeoCeDG`-owned working-layer
  service). Not geometric truth (`AGENTS.md` §4).
- **Persistence:** `SESSION` (recommended, `AQ-L2`). Initial value: `0` after
  New; after Open, the maximum layer used by the opened document's drawable
  objects, which reproduces the upstream "new objects on top" default once and
  then stays under user control. Rejected: `DOCUMENT_PRESENTATION` (the CAD
  current-layer precedent): it adds a document element for a value the user
  sets explicitly anyway. Alternative kept open for the author.
- **Creation seam (corrected by probe P3).** Overriding `App.getMaxLayerUsed()`
  in `AppGeoCeDG`, as planning inferred, is **insufficient**:
  1. the defaults path still caps the result at `MAX_LAYERS - 1`;
  2. dependent numerics without a `<layer>` tag would follow the session
     working layer on every reload and undo, so `Sort by Layer` would depend on
     the session;
  3. `GeoLocusV2` never reaches the defaults path and stays on layer 0.

  Candidate: a new overridable app hook, `getLayerForNewObject()` (default:
  the upstream rule), called by `ConstructionDefaults.setMaxLayerUsed` and
  `AlgoMacro.createOutputObjects`. `AppGeoCeDG` returns the working layer only
  for interactive creation. While a file, an undo point or a redefinition
  rebuild is being read, it returns the upstream value, so rebuilt documents
  are unchanged. The GeoCeDG Locus V2 family applies the same hook at output
  creation, which closes the layer-0 gap recorded as
  `OBS-R4C-LOCUS-V2-FAMILY-LAYER-BELOW-CONTAINING-LIST`.
- **Routes covered:** tools, Algebra Input, scripts and `Execute`, macro outputs
  and Locus V2/Spline V2. **Paste** keeps the copied layer (probe P3;
  recommended `AQ-L8`: paste is a copy, not a creation).
- **Degeneracy — hidden working layer (`AQ-L7`).** Recommended: the working
  layer cannot be hidden; its eye is disabled in the Algebra View, and choosing
  a hidden layer as working layer shows it. This avoids creating invisible
  objects.

## 5. Working-layer mode in the Move group

- New GeoCeDG mode `MODE_WORKING_LAYER` (next free id, 141), added to the native
  `edit-selection` group after `construction.move` and
  `construction.move-rotate`, on the `MODE_ORDERED_LIST` precedent: constant,
  name key, `menu*.properties` strings, `upstream-mode` profile action with
  `audited_numeric_id`, controller dispatch in
  `GeoCeDGEuclidianController.switchModeForProcessMode`, and an owned icon.
- Gestures: a click on an object adopts its layer; a click on empty space opens
  a numeric chooser bounded by the domain; Esc cancels. Recommended: the mode
  returns to Move after a successful choice (`AQ-L4` detail).
- Profile impact: one action. The 115-action pin
  (`geocedg-desktop/GeoCeDGProfile.java:340-343`) and its tests, the
  `edit-selection` toolbar pins and the group-order pins change together.

## 6. Status bar

- A GeoCeDG status-bar component in the application panel's `SOUTH` slot,
  added by an `AppGeoCeDG` override of `updateApplicationLayout`, which runs
  on every layout rebuild.
- Extensible segment API. `A` delivers `Layer: n`. `D1` adds
  `Construction: <unit>` and `Dimensions: <unit>` (frontend-scoped dependency
  `A → D1`).
- Presentation state only; never read by the kernel. A click on the layer
  segment may open the same chooser (recommended interaction).

## 7. Hidden layers and effective visibility

- **Rule placement (corrected by P0).** Not `isVisibleInThisView`. That
  predicate also feeds slope-field and ODE geometry, and drawables are neither
  removed nor re-added when it changes. Candidate: one presentation predicate,
  `isLayerShown(layer)`, consulted by:
  1. painting: a protected hook in the shared `EuclidianView` drawing of the
     drawable list (the private `drawGeometricObjects`,
     `EuclidianView.java:3599`), so that Graphics 1 **and** Graphics 2 obey it;
  2. hit testing: a post-filter of `EuclidianView.setHits` results
     (`EuclidianView.java:2328`);
  3. export painting: automatic through `drawObjects`; the SVG loop over
     `getAllDrawableList()` (`GraphicExportDialog.java:895-907`) needs the same
     predicate (`B`);
  4. LaTeX and DXF exporters (`C`, `AQ-L3`).
- Scripts, commands, `IsVisible`, the XML `<show object>` flag and the Algebra
  View marble keep reading **object** visibility. The Show/Hide tool keeps
  writing object visibility. Objects on hidden layers are not hittable.
- **Algebra View.** In `Sort by Layer`, each group node gets an eye that toggles
  the layer's hidden state. Implemented in `GeoCeDGAlgebraView`: a renderer
  override created with the `AppD` parameter (the override runs inside the
  `AlgebraTree` constructor), plus a click listener, because the tree
  controller has no factory hook. The group label shows the hidden state.
- **Persistence (`AQ-L3`).** Recommended `DOCUMENT_PRESENTATION`,
  **document-wide**. The Algebra View toggle is document-level, and per-view
  state would need an upstream Graphics 2 subclass.
  - **Where:** a flat, versioned GeoCeDG element at document level, written
    only when the set is non-empty. It is excluded from the preferences XML,
    the macro XML and the clipboard. This touches **shared serialization**:
    the writer is in `common/io/MyXMLio.java` and the reader in
    `common/io/MyXMLHandler.java`. Today an unknown `<geogebra>` child is only
    logged (`MyXMLHandler.java:715-716`).
  - **Undo rule:** toggling is not an undo step. The element is written only by
    the document writer, never into `getUndoXML`
    (`common/io/MyXMLio.java:138-166`), and an undo or redo reload neither
    reads nor resets the session set. File → Open reads it, and File → New
    clears it.
  - **Older builds:** older builds and Classic log and ignore it, so layers
    simply appear visible.
  - **Alternative:** `SESSION` (no serialization).
  - This changes the planning recommendation (per view, captured by undo); see
    the P0 report §14.

## 8. Effect on `ShowLayer` / `HideLayer` (`AQ-L5`)

Unchanged. They remain upstream commands that write object visibility, distinct
from hidden layers, and they keep their place in the GGBScript capability
matrix. Rerouting them would make them GeoCeDG-modified commands.

## 9. Degeneracies

| Case | Candidate behavior |
|---|---|
| layer outside the domain (typed, XML, API) | clamped to `[0, L_MAX]`, as upstream clamps today |
| hidden working layer | not allowed (§4) |
| every layer hidden | the view is empty; the status bar shows the working layer |
| object with no drawable (numeric) | the layer is irrelevant to painting; `Sort by Layer` grouping unchanged |
| reload or undo of a dependent numeric without a `<layer>` tag | the upstream value, never the session working layer |

## 10. Serialization impact

| State | Serialized | Where | Legacy `.cedg` / `.ggb` | Older GeoCeDG / Classic |
|---|---|---|---|---|
| layer values above 9 | yes | existing `<layer val>` | unaffected (0..9) | clamped to 9, lost on re-save (`AQ-L1c`) |
| working layer | no | — | — | — |
| hidden layers (recommended) | yes, lazily | new document-level GeoCeDG element | absent, so nothing is hidden | ignored, so layers visible |

## 11. Verification-class recommendation

- **`A` unsplit: `GLOBAL_IMPACT` (`FINAL`).** It widens a shared serialization
  domain in `GeoElement.setLayer`, read by every object on every load, and adds
  a document-level element.
- **Recommended split (`AQ-L1b`):**
  - `A-1`: working layer within 0..9, Move-group mode, status bar, numeric
    Algebra View order, per-layer eye and the effective-visibility predicate,
    with session-only hidden layers. `INTEGRATED_PHASE` (registered `PHASE` plus
    `INTEGRATION`): every creation route and the export paint path are concrete
    integration coverage.
  - `A-2`: domain widening and hidden-layer persistence. `GLOBAL_IMPACT`.
    `A-2` and `D1` both need `FINAL` for new shared serialization; the author
    may schedule them adjacently.

# PRE-G9B-R6-plus-A-2 — layer-domain widening and persistent hidden layers

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-03. That
instruction named the published base and the exact preparation base below,
kept the author-accepted verification class, fixed the normative scope, the
mandatory characterization, the forbidden scope and the stop conditions of
`PRE-G9B-R6-plus-A-2`, and authorized only the documentary preparation of this
prompt, a characterization report, the record of those decisions and the
status updates they require. It stated that it **does not authorize the
implementation of `A-2`**. The decisions are recorded, versioned, in the
[A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md);
the evidence behind the characterization below is in the
[A-2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-A-2` and the exact prepared candidate or base, and that
disposes of the requested decisions `DQ-A2-1` to `DQ-A2-11`. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `A-1`, `B`, `D0` and `D1`
precedent. This file is an execution contract, not a second policy document:
the layer decisions are stated once in the
[A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md)
(`AQ-L1a`–`AQ-L8`), which this prompt cites and does not restate differently.

```text
PRE-G9B-R6-plus-A-2 =
PREPARED — NOT AUTHORIZED

selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = PRODUCT IMPLEMENTATION — GEOCEDG LAYER DOMAIN 0..99,
                           DOCUMENT-WIDE PERSISTENT HIDDEN LAYERS (NOT UNDOABLE),
                           OPEN/NEW LIFECYCLE AND COMPATIBILITY
DEPENDS_ON               = PRE-G9B-R6-plus-A-1 = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (hard dependency; AUTHOR_SMOKE = PASS)
                           PRE-G9B-R6-plus-B   = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (export-preview regressions)
                           PRE-G9B-R6-plus-D1  = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (serialization landscape; recommended
                                                 predecessor)
PRECONDITION             = author disposition of AQ-L7(3) (requested as DQ-A2-1)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-C     (operational order; not authorized)
```

`authorApproved = false` means that no technical candidate of this phase has
been author-approved. Technical verification never creates author approval.
Once authorized, the phase stops with one exact technically verified candidate
pending author review and author smoke.

<!-- geocedg-field: objective -->
## Objective

Implement only:

1. the GeoCeDG layer-domain widening from `0..9` to `0..99`;
2. `L_MAX = 99` as the current GeoCeDG product/configuration limit, supplied by
   one product-configuration authority, never as a permanent CeDG conceptual
   limit and never as a magic number repeated across layers;
3. document-wide, persistent, not undoable hidden-layer state;
4. the compatibility behavior and serialization that persistence requires;
5. the interaction between persistent hidden layers and the accepted `A-1`
   layer workspace, including the author's disposition of `AQ-L7(3)`;
6. the tests and compatibility evidence of its class.

```text
GeoCeDG layer domain   = { 0, 1, …, L_MAX },  L_MAX = 99   (AQ-L1a)
Classic layer domain   = { 0, 1, …, 9 }                     (AQ-L1a, unchanged)
hiddenLayers           = DOCUMENT_PRESENTATION, document-wide, persistent,
                         not undoable                        (AQ-L3)
workingLayer           = SESSION, never serialized           (AQ-L2)
effectiveVisible(g)    = objectVisible(g) AND NOT layerHidden(g.layer)
```

Changing the hidden-layer set never changes object visibility flags,
geometry, the DAG, identity, `PersistentGeoId`, numeric values, Locus V2 or
Spline V2 semantics. The only truths that may differ after `A-2` are the
admissible layer number, the persistent hidden-layer presentation state, and
the effective normal-view visibility derived from it.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = GLOBAL_IMPACT          (author-accepted on 2026-10-02; kept by the
                                               preparation instruction of 2026-10-03)
frozenAtPhaseStart   = true when authorized
PLANNED_ACCEPTANCE   = FINAL — one and only one, on one exact frozen clean candidate,
                       after FINAL -PlanOnly; no downgrade because edits look small
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` maps
`GLOBAL_IMPACT` to FULL, whose canonical profile is `FINAL`. The impact facts
that select it: the admissible range of the shared `<layer val>` element, read
on every load and written on every save, undo and rebuild snapshot, widens for
GeoCeDG; a new document-level element enters the shared full-document writer
and the shared top-level reader; a fail-closed rule (if `DQ-A2-4` keeps the
default) enters the shared parser used by the Open preflight; the shared
domain consumers (`GeoElement`, `App`, `ConstructionDefaults`, scripting, API,
property models, 3D render coding) change for one product configuration and
must be proven unchanged for Classic; a forward-compatibility loss for older
readers (`AQ-L1c`) must be made explicit and tested across the corpus. The new
tests run canonically only in `final.shared` and `final.desktop`.

Escalation triggers. Stop and report, rather than widen scope silently, when
any stop condition (see *Stop conditions*) is met. A
`VERIFICATION_ESCALATION_REQUEST` does not apply upwards, because `FINAL` is
already the highest acceptance level; a request to run more than one `FINAL`
on an unchanged candidate is refused.

### Authorities and author decisions this prompt implements

| Authority | Effect in `A-2` |
|---|---|
| [A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md) `AQ-L1a`, `AQ-L1c` | the `0..99` GeoCeDG domain, Classic `0..9`, the accepted and documented forward-compatibility loss |
| same record `AQ-L1b` | the split; `A-2` carries only domain widening and persistence |
| same record `AQ-L3` | document-wide, persistent, not undoable hidden layers; the LaTeX exclusion and the DXF policy stay with `C` |
| same record `AQ-L2`, `AQ-L4`, `AQ-L5`, `AQ-L7(1)`, `AQ-L7(2)`, `AQ-L8` | kept unchanged: session working layer, one-shot mode, upstream `ShowLayer`/`HideLayer` semantics, the working layer is never hidden by interaction, choosing a hidden layer shows it, paste keeps layers |
| same record `AQ-L7(3)` | still open; requested as `DQ-A2-1` |
| [A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md) | the kept class, normative scope, mandatory characterization, forbidden scope and stop conditions |
| [A-1 closeout record](../../../docs/validation/pre_g9b_r6_plus_a1_closeout_record.md) and the accepted `A-1` implementation | the workspace, predicate and invariants `A-2` extends; `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` to revalidate under persistence |
| [D1 closeout record](../../../docs/validation/pre_g9b_r6_plus_d1_closeout_record.md) and the [D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md) | the current serialization landscape, the Open transaction, `DQ-D1-5` (presentation never save-relevant content) and `DQ-D1-4` (localized load diagnostics) |
| [layer-workspace design candidate](../../../docs/architecture/pre_g9b_r6_plus_a_layer_workspace_design_candidate.md) §3, §7 | evidence; where it differs from the author records, the records prevail |

No open decision of any other subphase is resolved here.

### Architecture decision: shared versus Desktop

| Concern | Owner | Why |
|---|---|---|
| the layer-domain bound `L_MAX` | the product configuration: one `AppConfig` default method returning the Classic constant, overridden only by `AppConfigGeoCeDG` | the only seam that every shared consumer reaches (`GeoElement` through its kernel, `App`, commands, API, property models) and that headless, preflight and helper apps inherit; precedent `AppConfig.java:388-402` overridden at `AppConfigGeoCeDG.java:105-113` |
| validation of an object's layer | shared `GeoElement.setLayer`, reading the configured bound | the single clamp every route already passes through |
| the immutable hidden-layer value, its grammar, canonical writer, strict reader and diagnostic | shared, in a new `org.geocedg.common` layers package | the shared writer, the shared reader and the Open preflight must agree byte for byte |
| writing and reading the element | shared `MyXMLio.getFullXML` and `MyXMLHandler.startGeoGebraElement` (minimal seams), with `App` hooks that are upstream-identical by default | the only full-document writer and the only top-level reader |
| the runtime hidden-layer set and the working layer | Desktop `GeoCeDGLayerWorkspace` owned by `AppGeoCeDG` (unchanged owner from `A-1`) | presentation; the only runtime holder; never in `Construction`, which every undo restore clears |
| transient staging of a parsed set until a commit point | `AppGeoCeDG` (an `App` hook) | startup files are parsed in the host constructor before the workspace exists |
| commit points, the working-layer rule after Open, save state, chooser, Algebra View eye, status | Desktop `AppGeoCeDG` and its workspace classes | orchestration and presentation |

No geometry algorithm, command or construction element reads the hidden-layer
set. No second hidden-layer authority exists: the parser stages, the
workspace commits, and the predicate `App.isLayerShown` stays the only
consumer interface.

### Characterization results at the base

The preparation re-read every seam below at the base, with scratch-only probes
where useful; the evidence and the probe identities are in the
[characterization report](../../../docs/validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md).
Paths use `common/` = `source/shared/common/src/main/java/org/geogebra/common/`,
`geocedg-common/` = `source/shared/common/src/main/java/org/geocedg/common/`,
`common-jre/` = `source/shared/common-jre/src/main/java/org/geogebra/common/jre/`,
`desktop/` = `source/desktop/desktop/src/main/java/org/geogebra/desktop/`,
`geocedg-desktop/` = `source/desktop/desktop/src/main/java/org/geocedg/desktop/`,
`jre-test/` = `source/shared/common-jre/src/test/java/org/geocedg/common/` and
`dtest/` = `source/desktop/desktop/src/test/java/org/geocedg/desktop/`. The
phase re-establishes each citation before relying on it. A result marked
**contract** is binding on the implementation once authorized; a result marked
**finding** is evidence that a contract rests on.

#### C1. Layer-domain authority map (`A2-C1`)

There is one constant, `EuclidianStyleConstants.MAX_LAYERS = 9`
(`common/plugin/EuclidianStyleConstants.java:186`). Eight places enforce it;
every other consumer works with any non-negative bounded integer.

| Site | Evidence | Class | Under GeoCeDG `0..99` |
|---|---|---|---|
| clamp of every route (XML, `SetLayer`, API, properties, the `A-1` seam) | `common/kernel/geos/GeoElement.java:1001-1018` | semantic authority / validation | reads the configured bound |
| highest-used bookkeeping | `common/main/App.java:1283-1291` (via `common/kernel/Kernel.java:4899-4900`) | validation (second clamp) | reads the configured bound |
| default layer of non-interactive new objects, top layer reserved | `common/kernel/ConstructionDefaults.java:907-926` | semantic rule | `DQ-A2-7` |
| `ShowLayer`/`HideLayer` argument range | `common/kernel/scripting/CmdShowHideLayer.java:58-62` | validation (silent no-op) | `DQ-A2-8` |
| API `setLayerVisible` range | `common/plugin/GgbAPI.java:398-401` | validation (silent no-op) | `DQ-A2-8` |
| Properties layer combo (index is the layer) | `common/gui/dialog/options/model/LayerModel.java:40-58`; `desktop/gui/dialog/ComboPanel.java` | UI bound | `DQ-A2-11`; a layer above the list length makes `setSelectedIndex` throw |
| property collection of the newer UI | `common/properties/impl/objects/LayerProperty.java:48-53` | UI bound (shared, used by Web) | reads the configured bound |
| GeoCeDG session bound | `geocedg-desktop/GeoCeDGLayerWorkspace.java:29-31`, `:34`, `:72`, `:171`, `:186-190`; chooser `geocedg-desktop/GeoCeDGWorkingLayerChooser.java:31-44`; message `apps/geocedg/application-profile.yml:5232-5235` | UI/session bound | reads the configured bound; message parameterized |
| 3D render coding (layer packed into alpha, depth bias) | `common/geogebra3D/euclidian3D/openGL/BufferPack.java:218-224`; `common/geogebra3D/main/VertexShader.java:105-110`, `:133`, `:152-153`; Desktop GL2 fallback `desktop/geogebra3D/euclidian3D/opengl/RendererImplGL2.java:711-713` | renderer encoding | decodes correctly on Desktop float32; depth bias grows about tenfold at 99; `DQ-A2-9` |
| `<layer val>` read and write | `common/io/ConsElementXMLHandler.java:610-618`, `:2384-2386`; `common/kernel/geos/XMLBuilder.java:139-147` (written for every drawable, including 0) | compatibility | read through the clamp; writer unchanged |
| draw order, hit testing, selection sentinels | `common/kernel/geos/DefaultGeoPriorityComparator.java:25-26`; `common/euclidian/HitDetector.java:61-65`, `:105-119`; `common/main/SelectionManager.java:376-389` | ordering | unchanged; non-negative bounded values are safe |
| Algebra View group order | `desktop/gui/view/algebra/AlgebraViewD.java:513-537` (numeric since `A-1`) | UI ordering | unchanged |
| DXF and SVG layer names | `geocedg-common/export/GeoElementGeometryExportAdapter.java:284-285`; `geocedg-common/export/G9X1GeometryExportAdapter.java:951-952`; `geocedg-desktop/export/PictureExportService.java:215-229` | export naming | no code change; `GEOCEDG_L<n>` already works for any `n` (`C` owns the exporters) |
| Web Algebra View order | `source/web/web/src/main/java/org/geogebra/web/full/gui/view/algebra/AlgebraViewW.java:1066-1079` | UI ordering (Web) | unchanged; Web stays `0..9` |
| stale documentation | `common/kernel/kernelND/GeoElementND.java:1106`, `:1120`; `SelectionManager.java:396`; user guides (C11) | documentation | living guides updated; upstream Javadoc left as recorded drift |
| test pins | `dtest/PreG9BR6PlusA1LayerWorkspaceTest.java:400-421` (chooser offers exactly 0..9, 10 rejected), `:192` (upstream cap 8 on a GeoCeDG app); `jre-test/kernel/PreG9BR6PlusA1LayerSeamTest.java:86-92` (cap 8 on the Classic host, unchanged) | test-only | updated with explicit justification |

No array is sized by `MAX_LAYERS` anywhere in the tree, including Web and 3D,
and no verifier under `tools/agent` pins `0..9`.

**Contract.** The single authority is a new default method of `AppConfig`
(for example `getMaxLayer()`), returning `EuclidianStyleConstants.MAX_LAYERS`,
overridden only in `AppConfigGeoCeDG` to return `L_MAX = 99`. That override is
the only place where `99` appears in product code. Every enforcement site above
reads the configured bound through the object's kernel or app; none copies the
number. `EuclidianStyleConstants.MAX_LAYERS` keeps its value. Negative layers
stay inadmissible (clamped to `0`).

#### C2. GeoCeDG versus Classic (`A2-C2`)

- **Finding.** Every app receives its configuration before its kernel exists
  (`desktop/main/AppD.java:410-411` before `initKernel` at `:456`; the headless
  `AppCommon` constructor likewise), every `GeoElement` reaches its app through
  its kernel (`common/kernel/algos/ConstructionElement.java:47`;
  `common/kernel/Kernel.java:569`), and a `MacroKernel` shares its parent app.
  Shared code already branches on configuration default methods (`AppConfig.java:388-402`,
  overridden at `geocedg-common/main/settings/config/AppConfigGeoCeDG.java:105-113`).
- **Finding.** The GeoCeDG hidden helpers already carry the GeoCeDG
  configuration: the Insert File and Apply Template helper
  (`geocedg-desktop/AppGeoCeDG.java:664-672`) and the Open preflight scratch app
  (`AppGeoCeDG.java:1026-1032`, used by `desktop/io/DocumentArchivePreflight.java:36-52`).
  Classic uses `AppConfigDefault` for both (`desktop/main/AppD.java:3187-3189`).
- **Probe `P-GEOCEDG-CONFIG` (finding).** On the unchanged base, a headless app
  with `AppConfigGeoCeDG` clamps exactly like Classic (`setLayer` of 10, 50, 99,
  100 gives 9), reserves layer 9 for new objects (a new object takes 8) and
  ignores `HideLayer(42)`.
- **Contract.** With `AppConfigDefault` (Classic Desktop, the Classic
  diagnostic process, headless Classic tests, Web), the bound is 9 and every
  enforcement site behaves byte for byte and value for value as at the base.
  Classic does not acquire `0..99` because shared code changed; `T-CLASSIC`
  proves it on every route of C1 and C3. No material upstream or kernel
  redesign is needed: the existing configuration seam distinguishes the
  products, so the prompt does not stop here.

#### C3. `<layer val>` lifecycle and older readers (`A2-C3`)

| Route | Evidence | Behavior at the base |
|---|---|---|
| save, full XML | `common/kernel/geos/XMLBuilder.java:139-147` | `<layer val="n"/>` for every drawable (also 0); dependent numerics with a definition omit it |
| load | `common/io/ConsElementXMLHandler.java:610-618` | through `setLayer`: silent clamp; an unparsable value is ignored |
| undo, redo, redefine, rollback | construction XML (C6 route table) | same element, same clamp |
| macros, `.ggt` | macro construction elements; the macro kernel shares the app | same element, same clamp |
| clipboard, paste, Insert File | copied element XML; paste keeps the copied layer (`AQ-L8`) | same element, same clamp |
| `SetLayer`, API `setLayer` | `common/kernel/scripting/CmdSetLayer.java:53`; `common/plugin/GgbAPI.java:371-377` | through `setLayer`: clamp |

- **Probe `P-LAYER-XML-READ` (finding).** The unchanged base, acting as an
  older reader: a document whose points carry `-5`, `-1`, `0`, `5`, `9`, `10`,
  `50`, `99`, `100`, `1000` loads as `0`, `0`, `0`, `5`, `9`, `9`, `9`, `9`, `9`,
  `9`; the highest used layer becomes 9; the re-saved document writes `0` and
  `9`. The original values are lost. `SetLayer` and the API behave the same.
- **Contract.** `AQ-L1c` accepts this loss for older Classic and GeoCeDG readers.
  `A-2` adds no migration, no warning on save and no older-reader
  workaround; it documents the loss in the user guides and proves it with a
  base-tree read of a candidate document (`T-OLDER-READER`). An `A-2` build
  reading a value outside its own domain follows `DQ-A2-10`.

#### C4. Persistent hidden-layer owner (`A2-C4`)

- **Finding.** `A-1` owns the working layer and the hidden set in the Desktop
  `GeoCeDGLayerWorkspace` (`geocedg-desktop/GeoCeDGLayerWorkspace.java:27-37`),
  one per `AppGeoCeDG` (`AppGeoCeDG.java:319-340`). The predicate is the shared
  `EuclidianView.isOnShownLayer` (`common/euclidian/EuclidianView.java:3661-3663`)
  → `App.isLayerShown` → the workspace (`AppGeoCeDG.java:349-352`). Toggling an
  Algebra View eye writes only the workspace (`geocedg-desktop/GeoCeDGAlgebraView.java:110-111`).
  Nothing is serialized and no undo point is stored
  (`dtest/PreG9BR6PlusA1HiddenLayerTest.java:179-211`).
- **Finding.** `Construction.clearConstruction` (`common/kernel/Construction.java:4133-4183`)
  runs on every clearing parse through `Kernel.clearConstruction`
  (`common/kernel/Kernel.java:3061-3082`), including every undo restore, so a
  holder in `Construction` would be reset by undo.
- **Contract.** The workspace stays the only runtime owner of the hidden set
  and the working layer. The shared layer gets only the value type, its codec
  and diagnostic, the two serialization seams and the `App` hooks. Hidden layers
  never become geometric, DAG, construction or object state.

#### C5. Persistence shape (`A2-C5`)

The `P0` design candidate (a flat, versioned, document-level GeoCeDG element,
written only when non-empty, excluded from preferences, macro and clipboard XML,
never in undo XML) was re-characterized against the `D1` architecture:

| Placement | Undo snapshots | Full document | Preferences | Older readers | Verdict |
|---|---|---|---|---|---|
| inside `<construction>`, like `<geocedgUnits>` (`Construction.java:1557-1560`) | yes (`common/io/MyXMLio.java:157`) | yes | no | log and drop | rejected: undoable by construction, which `D1` wants for units and `AQ-L3` forbids here |
| inside `<euclidianView>` | yes (`MyXMLio.java:151`) | yes | yes | log and drop | rejected: undoable, preferences, per view |
| inside `<gui>` | no | yes | yes, needs a guard | log and drop | rejected: mixes into layout parsing and preferences |
| direct child of `<geogebra>`, written only by `MyXMLio.getFullXML` (`MyXMLio.java:308-321`) | **no** | **yes** | **no** | log and drop | **kept** |

- **Probe `P-XML-PARTS` (finding).** The top-level children of the undo XML
  are `euclidianView`, `kernel`, `tableview`, `construction`; of the full XML
  and `App.getXML()` `gui`, `euclidianView`, `algebraView`, `kernel`,
  `tableview`, `scripting`, `construction`; of the preferences XML the same
  without `construction`. Save uses `getFullXML` (`common-jre/io/MyXMLioJre.java:392`).
- **Probe `P-UNKNOWN-ELEMENT` (finding).** The base build loads a document with
  an unknown GeoCeDG element as the first or last child of `<geogebra>`, inside
  `<euclidianView>`, `<construction>` or `<gui>`, logs
  `unknown tag in <…>` (`common/io/MyXMLHandler.java:729-730` for the top level),
  keeps every object, and drops the element on re-save.
- **Contract.** The persistence shape is the accepted candidate; `D1` offers no
  more appropriate equivalent, because its element is construction-scoped and
  undoable by design. The element name, grammar and position are requested as
  `DQ-A2-3` with this default:

  ```text
  hidden-element = "<geocedgHiddenLayers" SP 'version="1"' SP 'layers="' layer-list '"' "/>"
  layer-list     = layer *( SP layer )          ; strictly ascending, no duplicates
  layer          = "0" / ( %x31-39 *DIGIT )     ; ASCII, no sign, no leading zero, ≤ L_MAX
  ```

  - written exactly when the set is non-empty, on its own line, as a direct
    child of `<geogebra>` after `</construction>` and before `</geogebra>`, by
    `MyXMLio.getFullXML` only, from an `App` hook whose upstream default
    returns the empty set (Classic writes nothing and stays byte-identical);
  - the set is written exactly as held: never pruned to used layers, never
    inferred from object visibility, from the working layer or from labels;
  - never written by `getUndoXML`, `getPreferencesXML`, `getFullMacroXML`
    (`MyXMLio.java:331-340`), copy or any clipboard route;
  - read by one `geocedgHiddenLayers` case in
    `MyXMLHandler.startGeoGebraElement` (`MyXMLHandler.java:661-731`), active
    only when the configuration enables persistent hidden layers (`DQ-A2-5`),
    which validates (`DQ-A2-4`) and hands the value to an `App` staging hook
    (upstream default: nothing);
  - ignored with a text log in macro parses (the macro part of an archive,
    `common-jre/io/MyXMLioJre.java:200-208`; `.ggt`; `addMacroXML`), in
    defaults and preferences XML, and in every non-clearing parse.

#### C6. Persistence without undo (`A2-C6`)

The reader cannot tell an undo restore from a document load: both are clearing
parses with `NATIVE_OR_UNDO_RESTORE` (`MyXMLio.java:420-422`). The mechanism
therefore never decides at the parser:

```text
parser       : element present → validate → stage (transient);  absent → nothing
commit point : staged value, or the empty set when nothing was staged,
               replaces the workspace set after a successful load (DQ-A2-6)
any other route: no commit; the workspace set is untouched
```

| Route | Evidence | XML parsed | Element | Hidden set after |
|---|---|---|---|---|
| save, `App.getXML()` | `MyXMLioJre.java:331-401`; `common/main/App.java:1851-1853` | — | written when non-empty | — |
| undo store | `common/main/undo/UndoManager.java:284-287` → `Construction.getCurrentUndoXML` → `MyXMLio.getUndoXML` (`:140-169`) | — | never written | — |
| undo, redo | `desktop/main/undo/UndoManagerD.java:184-244` → `MyXMLioJre.readZipFromMemory`; headless `DefaultUndoManager` | undo | absent | unchanged |
| redefine rebuild, CAS change, collected redefines, failed-redefine restore | `Construction.java:2126`, `:2146`, `:4217-4249` | undo | absent | unchanged |
| atomic mutation rollback | `Construction.java:2698-2732` | undo | absent | unchanged |
| rejected-parse restore | `MyXMLio.java:405-406`, `:511-536` | full (`app.getXML()`) | present = current value | unchanged (staged only) |
| paste rollback, Insert File spatial rollback | `common/util/InternalClipboard.java:623`, `:663-685`; `desktop/util/CopyPasteD.java:672` | full | present = current value | unchanged (staged only) |
| paste, `evalXML`, action replay | `InternalClipboard.java:626-640`; `common/plugin/GgbAPI.java:168-180` | construction-wrapped | cannot occur at top level | unchanged |
| File → Open `.cedg`/`.ggb`, archives with macros, Open Recent, reset reload | `geocedg-desktop/AppGeoCeDG.java:605-620` → `desktop/main/AppD.java:3074-3087`, `:3144-3161`, `:3253-3312` | full | present or absent | **commit** (C7) |
| failed live load after preflight | `AppD.java:3301-3307`, `:3333-3368` (the rollback snapshot is a full archive) | full | previous value | previous set and working layer unchanged; staging discarded |
| `loadXML(String)` | `AppGeoCeDG.java:622-634` | full | present or absent | **commit** |
| startup with a file | the host constructor parses before the workspace exists (`AppGeoCeDG.java:607-611`, `:319-332`) | full | present or absent | **commit** in `initializeLayerWorkspace` |
| File → New, startup without a file | `AppGeoCeDG.java:586-603` | — | — | empty |
| clearing `App.setXML` (public API, tool-creation reload, edit-macro switch, preferences loads) | `common/main/App.java:5114-5134`, `:1733-1747`; `common/plugin/GgbAPI.java:1473-1477`; `desktop/gui/dialog/ToolCreationDialogD.java:294`; `desktop/main/AppD.java:483`, `:506`, `:1175` | full or preferences | present or absent | `DQ-A2-6` |
| `loadXML(URL)`, Base64 open, `openURL` | `AppD.java:3218-3251`; `desktop/gui/GuiManagerD.java:1838-1842`, `:1864`, `:1888`, `:1893` | full | present or absent | `DQ-A2-6` (`OBS-A1-URL-OPEN`) |
| Insert File, Apply Template | `AppD.java:4734-4787`, `:4796-4815`; helper `AppGeoCeDG.java:664-672` | the source loads into the helper | the helper's own business | target unchanged: never imported |
| macro part of an archive, `.ggt`, `addMacroXML` | `MyXMLioJre.java:200-208`; `App.java:3664-3670` | macro | ignored with a log | unchanged |
| Open preflight | `DocumentArchivePreflight.java:36-52` | full | validated; staged into the scratch app's no-op hook | live state untouched |

**Contract.** Save and Open persist the set; undo and redo never rewind it;
there is no second document authority and no change to the general undo
architecture. A route that cannot keep this table stops the phase.

#### C7. New and Open lifecycle (`A2-C7`)

- **Finding.** New clears the hidden set and sets the working layer to 0
  (`AppGeoCeDG.java:586-592`; `GeoCeDGLayerWorkspace.java:152-157`). Open,
  `loadXML(String)` and a startup file call `resetForOpenedDocument`, which sets
  the working layer to the highest drawable layer capped at 9 and **clears the
  hidden set after the load** (`GeoCeDGLayerWorkspace.java:164-174`;
  `AppGeoCeDG.java:612-618`, `:627-631`, `:322`). Unchanged, it would wipe every
  persisted set.
- **Contract.**
  - New: empty set; working layer 0 (`A-1`).
  - Open and every commit point of `DQ-A2-6`: staging is cleared when the
    transition starts; after a successful load the workspace takes the staged
    set or the empty set, then chooses the working layer by `DQ-A2-1`. A legacy
    document therefore opens with an empty set.
  - Open never writes the persisted set merely to satisfy the working-layer
    initialization, never infers hidden layers from object visibility, and never
    marks the document modified.
  - A failed load leaves the previous set, working layer, file, saved state and
    undo history unchanged.
  - The staging field of `AppGeoCeDG` has no field initializer, because the
    host constructor parses a startup file before the subclass initializers
    run (the precedent of `AppGeoCeDG.java:83-89`).

#### C8. `AQ-L7(3)`: Open and persistent hidden layers (`A2-C8`)

`A-1` fixes: the working layer cannot be hidden through normal interaction
(`AQ-L7(1)`, `GeoCeDGLayerWorkspace.java:90-101`), and choosing a hidden layer
as working shows it (`AQ-L7(2)`, `:56-65`). After Open, the `A-1` rule takes the
highest drawable layer. With persistence, that layer can be hidden.

| Case | `A-1` rule unchanged | Default of `DQ-A2-1` |
|---|---|---|
| 1. highest used drawable layer is shown | it | it |
| 2. highest used drawable layer is hidden | a hidden working layer (breaks `AQ-L7(1)`) | the highest used **shown** layer |
| 3. layer 0 is hidden | unaffected unless 0 is the fallback | the fallback skips it |
| 4. all used layers are hidden | a hidden working layer | the lowest shown layer of `0..L_MAX` (possibly unused) |
| 5. all layers `0..L_MAX` are hidden | a hidden working layer | cannot be opened: a GeoCeDG writer never produces it, because the working layer is never hidden when the set is saved; the element is invalid (`DQ-A2-4`) |
| 6. no drawables | 0 | the lowest shown layer (0 unless 0 is hidden) |
| 7. the only candidate is on a hidden layer | a hidden working layer | as 2 or 4 |

The persisted set is never modified by this rule. The status bar shows the
chosen working layer; nothing else changes.

#### C9. Effective visibility after reopen (`A2-C9`)

- **Finding.** Graphics 1 (`geocedg-desktop/GeoCeDGEuclidianView.java`) and the
  upstream Graphics 2 class obey the shared predicate through painting
  (`EuclidianView.java:3606-3650`) and hit testing (`HitDetector.java:61-65`);
  the Algebra View greys hidden groups (`GeoCeDGAlgebraView.java:171-191`); the
  picture service handles Graphics 1 and 2 only
  (`geocedg-desktop/export/PictureExportService.java:87-92`), paints through the
  filtered export paint (`:302-308`) and filters SVG explicitly (`:213-221`);
  the Save preview of a pending picture export is rendered by that service
  (`AppGeoCeDG.java:413-429`). The 3D view ignores hidden layers
  (`OBS-A1-3D-VIEW`). LaTeX (`common/export/pstricks/GeoGebraExport.java:356-357`)
  and DXF (`geocedg-common/export/G9X1GeometryExportAdapter.java:473`) still
  write objects on hidden layers; they belong to `C`.
- **`OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` (finding).** `B` corrected the
  stale Save preview for session state (B candidate report §5; tests
  `dtest/PreG9BR6PlusBExportSurfaceTest.java:316-336`, `:354-388`), and the B
  closeout records the author smoke of the regenerated preview. No record
  closes the observation explicitly, and the `A-1` closeout asks for its
  revalidation once hidden layers persist.
- **Contract.** After a save and reopen, every consumer above shows the same
  effective visibility as before the save (`T-EFFECTIVE-REOPEN`), including the
  Save preview of every picture format. The candidate report records the
  observation as closed under persistent reopen only with that evidence. No
  exporter changes in `A-2`.

#### C10. `A-1` observations (`A2-C10`)

| Observation | After `A-2` | Owner |
|---|---|---|
| `OBS-A1-GRAPHICS2-GEOCEDG-MODES` | unaffected: GeoCeDG modes still do not act in the upstream Graphics 2 controller; painting and hit filtering do | later frontend phase |
| `OBS-A1-BACKGROUND-IMAGE-LAYER` | unaffected: the background pass stays unlayered; persistence makes no difference (B §12) | author / future |
| `OBS-A1-3D-VIEW` | interaction: the domain widening reaches the 3D render coding (`DQ-A2-9`); hidden layers still do not apply in 3D | author |
| `OBS-A1-NON-HIT-SELECTION` | interaction: persistent sets make reopened documents with hidden objects common, so Select All and keyboard selection reach them more often; no change in `A-2` | author |
| `OBS-A1-RECOMPUTE-OUTPUTS` | unaffected in nature; outputs may now take a working layer in `10..99` | none required |
| `OBS-A1-URL-OPEN` | interaction: with persistence a stale set would survive the API URL route; the default of `DQ-A2-6` makes that route a commit point | `A-2` by `DQ-A2-6`, or kept as observation |

Nothing else is absorbed because `A-2` touches layers.

#### C11. Compatibility corpus (`A2-C11`)

- **Finding.** The tracked corpus is the `.ggb`/`.cedg` documents and `.ggt`
  files of `git ls-files`, including `models/`; `D1` committed a base
  fingerprint fixture of their construction XML. The user guides state `0..9`
  (`docs/user/geocedg_user_guide_en.md:1408`, `:1421`, `:1621`;
  `docs/user/geocedg_user_guide_es.md:1464`, `:1479`, `:1688`); the living
  workspace specification and the developer guide state that hidden layers are
  session-only (`geocedg/specs/ui/cedg-workspaces.md:467-469`;
  `docs/developer/geocedg_developer_guide.md:629-634`).
- **Contract.** The mandatory corpus: a legacy `.cedg` with layers `0..9`; a
  Classic `.ggb`; objects on layers 10, 50 and 99; attempted values below 0 and
  above 99 (XML, `SetLayer`, API, properties); a persistent hidden-layer
  document; an all-used-layers-hidden document; an all-layers-hidden document
  (invalid); a document with Graphics 2; a document with macros and a `.ggt`;
  clipboard and paste across documents with different sets; an undo snapshot;
  redefine and rollback routes; a unit-bearing `D1` document combined with
  hidden layers. Byte identity: every corpus document that loads
  deterministically and uses no layer above 9 and no hidden layer re-saves
  byte-identically to the base (the `D1` fixture is reproduced; a full-document
  fixture, if added, is captured on the unchanged base and reproduced on a
  `git archive` of it). Unit persistence and layer persistence are independent
  in every direction (`T-UNITS-INDEPENDENCE`).

#### C12. Invariants (`A2-C12`)

Before and after every domain-widening route and every hidden-layer change,
save, reopen, undo and redo, on a corpus with points, lines, conics, polygons,
a dependent measurement, a Locus V2 with several branches, a Spline V2 and a
slider-driven parameter: coordinates and numeric values, geometry, the DAG,
construction order, labels, `PersistentGeoId`, Locus V2 branch and component
identity, Spline V2 parameters, and every object's own visibility flag are
equal. Only the admissible layer number, the persistent hidden-layer state and
the effective normal-view visibility derived from it may differ.

#### Contradictions with earlier documents

| Source | Statement | At the base | Effect |
|---|---|---|---|
| mini-track plan §11, row `hiddenLayers` | per view, in `<euclidianView>`, captured by undo | superseded by `AQ-L3` (A author-decision record) and the `P0` candidate | the record prevails; `A-2` follows C5 |
| `geocedg/specs/ui/cedg-workspaces.md:467-469`; developer guide `:629-634` | hidden layers are session state, never serialized | true for `A-1` | `A-2` updates both living documents |
| user guides (C11) | the layer domain is `0..9` | true at the base | `A-2` updates both guides and documents the older-reader loss |
| `GeoElementND.java:1106`, `:1120`; `SelectionManager.java:396` | Javadoc "0 to 9" | upstream text | recorded drift; not edited |
| `A-1` tests (C1) | the chooser offers exactly `0..9`; the GeoCeDG default cap is 8; a toggle leaves the document saved | true for `A-1` | changed only as `DQ-A2-2`, `DQ-A2-7` and `DQ-A2-11` decide |
| `P0` persistence matrix, row `workingLayer` | after Open, the highest drawable layer | `A-1` implements it capped at 9 | refined by `DQ-A2-1` |

A contradiction that would change scope, owner, serialization or class stops
the phase.

### Decisions requested before authorization

Each row fixes a default contract. None is an author decision until the
author disposes of it; the authorizing instruction is expected to record the
dispositions, which then prevail over these defaults.

| ID | Question | Default contract | Main alternative |
|---|---|---|---|
| `DQ-A2-1` | `AQ-L7(3)`: working layer after Open with a persisted hidden set | C8: with `H` the persisted set and `U` the layers of drawables, `workingLayer = max(U \ H)`, else `min({0..L_MAX} \ H)`; `H` is never modified; a full-domain `H` is invalid | keep the `A-1` rule and show the chosen layer with a visible notice (an explicit mutation of the persisted set) |
| `DQ-A2-2` | does a hidden-set change mark the document modified? | yes: an Algebra View eye toggle, or a working-layer choice that shows a hidden layer, calls `setUnsaved`, stores no undo point, and does not make presentation save-relevant content (`DQ-D1-5` kept: an otherwise empty document never prompts); a working-layer change alone, and every commit point, do not mark it | never mark it (the `A-1` behavior); the set is saved only with another change |
| `DQ-A2-3` | element name, grammar and position | C5: `<geocedgHiddenLayers version="1" layers="…"/>`, ascending canonical list, written only when non-empty, after `</construction>` by `getFullXML` only | another name, or the position before `<construction>` (it would enter the header that `App.openEditMacro` copies, `App.java:1733-1747`) |
| `DQ-A2-4` | recognized invalid hidden-layer metadata | fail closed, as `D1` §8.7 does for units, with localized (`en`, `es`) diagnostics naming the defect and the existing Open transaction (preflight first): a newer `version`; a missing or non-integer `version`; an attribute outside the grammar; a malformed list (sign, leading zero, not ascending, duplicate, empty, other separators); a value above the reader's `L_MAX`; a set covering the whole domain; a child element or text; a second element; the element anywhere but directly under `<geogebra>`. Macro, `.ggt`, defaults and preferences contexts ignore it with a text log | open with every layer shown and a non-blocking notice, dropping the element on the next save |
| `DQ-A2-5` | Classic diagnostic and Web | older readers: a configuration default method keeps persistence off, so the reader keeps the upstream log-and-ignore behavior and the writer writes nothing; no Classic or Web hidden-layer UI | Classic validates and preserves the element without presenting it (incoherent with the Classic clamp of layers above 9) |
| `DQ-A2-6` | commit points | New, Open (`.cedg`, `.ggb`, archives with macros, Open Recent, reset reload), `loadXML(String)`, startup with or without a file, every clearing `App.setXML` in GeoCeDG, and the `loadXML(URL)`, Base64 and `openURL` routes; never undo, redo, rebuild, rollback, paste, `evalXML`, action replay, macro loads, preflight, Insert File or Apply Template, which never import the source document's set | keep the API, URL and Base64 routes outside, as `OBS-A1-URL-OPEN` recorded |
| `DQ-A2-7` | default layer of non-interactive new objects | `min(L_MAX − 1, highestUsed)` through the configured bound: GeoCeDG 98, Classic 8 unchanged; interactive creation keeps the `A-1` working layer | keep the GeoCeDG cap at 8 |
| `DQ-A2-8` | range of `ShowLayer`, `HideLayer` and API `setLayerVisible` | the configured domain (GeoCeDG `0..99`, Classic `0..9`); semantics unchanged (`AQ-L5`): they still write individual object visibility and never touch the hidden set; the GGBScript capability matrix rule is re-applied and its derived inventory reconciled through the official mechanism if it changes | keep `0..9` in GeoCeDG, so `HideLayer(42)` stays a silent no-op although objects live on 42 |
| `DQ-A2-9` | 3D render coding | clamp the layer fed to the 3D alpha coding and depth bias to the Classic range before the per-type shifts; render coding only; Classic unchanged | no 3D change, accepting a depth bias up to about ten times larger at layer 99 |
| `DQ-A2-10` | an object layer outside the reader's domain in an `A-2` build | keep the upstream silent clamp on read (above `L_MAX` → `L_MAX`, below 0 → 0) | fail closed, which changes the upstream reader read by Classic too |
| `DQ-A2-11` | layer pickers for `0..99` | the Properties combo (index is the layer) and the working-layer chooser list `0..L_MAX` from the configuration; texts carry the bound as a parameter | a numeric spinner or field in the GeoCeDG Properties panel |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
PUBLISHED_BASE =
2941ddf2f2712340e831d292bdc2687b879850d6          (P_R6PLUS_D1, published D1 closeout;
                                                   tree 158184b150a42e6dcae2946c39620d92cbfcd888)

PREPARATION_BASE =
f467eb4563de7a7eb162da1b74954f9f847fa9d3          (T_R6PLUS_F3_PLAN, local F3 planning
                                                   commit; tree
                                                   ece51c96117d6cbd55bc200d29c5240686516da9)

IMPLEMENTATION_BRANCH_START =
named by the authorizing instruction (the prepared candidate that carries this
prompt, or a later published commit that contains it)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal the published base the authorizing instruction names,
its tree matches, the named branch start exists with its tree, and the
worktree is clean. The phase works on a new local branch from the named commit
and is not rebased onto any later commit without a new author instruction.
Between `P_R6PLUS_D1` and the preparation candidate only documentation changes,
so the citations above still apply; the phase re-establishes every one it
relies on before using it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source, tests, build and serialization at the base.
3. The [A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md),
   the [A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md)
   and the author's dispositions of `DQ-A2-1` to `DQ-A2-11`.
4. The accepted `A-1`, `B` and `D1` closeout records and their accepted
   implementations.
5. Evidence, re-established before use: the
   [A-2 characterization report](../../../docs/validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md)
   and its mirror; the `P0` report and the layer-workspace design candidate.
6. Generated artifacts, earlier reports and previous agent output are evidence,
   not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

### Shared kernel, configuration and document layer

- `AppConfig`: one default method for the layer bound and one for enabling
  persistent hidden layers, upstream-identical; `AppConfigGeoCeDG` overrides
  both.
- `GeoElement.setLayer`, `App.updateMaxLayerUsed`, `ConstructionDefaults`
  (`DQ-A2-7`), `CmdShowHideLayer` and `GgbAPI.setLayerVisible` (`DQ-A2-8`),
  `LayerModel` and `LayerProperty` (`DQ-A2-11`): read the configured bound,
  nothing else.
- A new `org.geocedg.common` layers package: the immutable hidden-layer value,
  its grammar, canonical writer, strict reader, structured diagnostic and
  dedicated unchecked exception.
- `MyXMLio.getFullXML`: one writer call; `MyXMLHandler.startGeoGebraElement`:
  one reader case with misplacement detection (`DQ-A2-4`); `App`: the save and
  staging hooks with upstream-identical defaults; the narrowest error-message
  seam for a rejected element, reusing the `D1` diagnostic route.
- The 3D render coding clamp, if `DQ-A2-9` keeps the default.
- Kernel localization keys for the diagnostics, if raised from shared code
  (`menu*.properties` are mixed-EOL; new lines with LF).

### Desktop GeoCeDG layer

- `GeoCeDGLayerWorkspace`: the configured bound, the commit of a staged set,
  the `DQ-A2-1` rule, the `DQ-A2-2` save-state notification.
- `AppGeoCeDG`: the save and staging hooks, the commit points of `DQ-A2-6`, the
  startup commit, the failure path, the error messages.
- `GeoCeDGWorkingLayerChooser`, `GeoCeDGAlgebraView`, `GeoCeDGStatusBar` and the
  profile text `Workspace.Layer.ChooserMessage`: the configured bound only.

### Supporting changes

- Tests for every obligation below and the fixture documents, registered in
  `docs/upstream/modified-files.yml` where they live under `source/`.
- Registration of every modified upstream file in
  `docs/upstream/modified-files.yml` with the narrowest rationale.
- The JUnit inventory and registry pin through the official updater with
  executed selection evidence; no new phase selection is registered.
- Living documentation of the delivered behavior: the bilingual user guide
  (structure rules of `documentation-maintenance.md` §10), the developer guide,
  `geocedg/specs/ui/cedg-workspaces.md` (the hidden-layer element and its
  grammar, recorded from this contract), the candidate report and its
  machine-readable evidence.

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

- `F3` (File → Insert surface) and every change of the Insert File or Apply
  Template surface or semantics beyond keeping their current behavior with the
  widened domain.
- `C`-owned exporters, export specifications and verifier pins: LaTeX
  exclusion, DXF hidden-layer policy, `drawingScale`, engineering scale.
- `E1`, `E2`, `E3`, `F1`, `F2`, `G`; any unit-system redesign or change of
  `<geocedgUnits>` semantics.
- `G11` scope: layer hierarchy, roles, locking, filters, named layer states;
  per-view persistent layer state beyond the document-wide set.
- Rewriting object visibility to emulate hidden layers; changing
  `ShowLayer`/`HideLayer` semantics (`AQ-L5`); making hidden layers geometric,
  DAG, construction or object state; serializing `workingLayer`; making a
  hidden-set change undoable.
- Widening Classic or Web to `0..99`; raising
  `EuclidianStyleConstants.MAX_LAYERS`; repeating `99` outside the GeoCeDG
  configuration.
- The rollback GUI debt `OBS-D1-ROLLBACK-CONSPROT-GUI`; any other `A-1`
  observation beyond C10.
- Opportunistic 3D-layer behavior beyond the characterized render-coding
  decision of `DQ-A2-9`.
- Edits of the governance layer, `AGENTS.md`, `CLAUDE.md`,
  `.github/prompts/**` other than this prompt's authorized amendment,
  `ai-shell/prompts/**`, the verifier, its schemas or `prompt-contracts.json`,
  beyond inventory and registry data updated through existing mechanisms.
- Any commit, move, rename, rewrite or copy of `artifacts/author-input/**`.

## Architectural placement

- The layer bound is product configuration read by shared validation; the
  hidden-layer set is document presentation held by the Desktop workspace and
  persisted through minimal shared serialization seams; the working layer is
  Desktop session state (`AGENTS.md` §4: layers are document/application
  model, not geometric truth).
- Each upstream seam is the narrowest that serves the behavior,
  upstream-identical for `AppConfigDefault`, and recorded in
  `docs/upstream/modified-files.yml`. If an owner is wrong at the base, stop and
  report instead of choosing another layer.

## Required design/specification

The author decisions, their `DQ-A2` dispositions and this prompt are the
design. Before product edits, the phase records in its candidate report the
re-established seams, the chosen class, method and element names, the hooks
and their defaults, and every correction to C1–C12. It records the delivered
element and grammar in `geocedg/specs/ui/cedg-workspaces.md` from this
contract, without adding semantics. A correction that would change scope,
owner, serialization or class stops the phase.

## Geometric invariants and degeneracies

`A-2` changes no geometric definition, algorithm, dependency or numeric value
(C12). Layer cases with required behavior:

| Case | Required behavior |
|---|---|
| layer 10, 50, 99 in GeoCeDG | kept on every route (C3) |
| layer above 99 or below 0 in GeoCeDG | `DQ-A2-10` |
| any layer above 9 in Classic | clamped to 9, as at the base |
| empty hidden set | no element; byte identity |
| hidden set containing the working layer at save | impossible by `AQ-L7(1)` |
| hidden set covering `0..L_MAX` | invalid (`DQ-A2-4`) |
| hidden layer with no object | kept and persisted; never pruned |
| object visible flag false on a shown layer | stays hidden; its flag is never written by `A-2` |
| undo after a hidden-set change | the set is unchanged |

## Compatibility and serialization

Two serialization facts change. The admissible value of the existing shared
`<layer val>` widens to `0..99` for GeoCeDG documents; older Classic and GeoCeDG
readers clamp values above 9 to 9 and lose them on re-save (`AQ-L1c`,
evidenced at preparation). A new document-level element carries a non-empty
hidden set; older readers log and drop it and show every layer. Legacy `.cedg`
and Classic `.ggb` documents with layers `0..9` and no hidden set re-save
byte-identically; no migration and no feature flag exist. Undo, preferences,
macro, `.ggt` and clipboard XML never contain the element. GeoCeDG still saves
only `.cedg`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

Focal tests mechanize every obligation. Each runs canonically in `final.shared`
or `final.desktop`:

| ID | Obligation |
|---|---|
| `T-DOMAIN-CONFIG` | the configuration bound: 9 by default, 99 in `AppConfigGeoCeDG`; the only `99` literal in product code |
| `T-DOMAIN-ROUTES` | GeoCeDG keeps 10, 50, 99 through `setLayer`, `SetLayer`, the API, the XML reader, the Properties model and paste; `DQ-A2-10` outside the domain |
| `T-DOMAIN-CONSUMERS` | `updateMaxLayerUsed`; the default cap (`DQ-A2-7`); macro and Locus V2 outputs; `GeoList` and polyhedron propagation; `ShowLayer`, `HideLayer`, `setLayerVisible` (`DQ-A2-8`); `LayerProperty`; numeric Algebra View order over `0..99`; the hit-test top-layer rule and the draw order with layers above 9 |
| `T-CLASSIC` | with `AppConfigDefault`: every probe value of C3 gives the base result; `ShowLayer(42)` is a no-op; the Properties combo has 10 entries; the hidden-layer element is logged, ignored and not re-written (`DQ-A2-5`); a corpus document is byte-identical |
| `T-WORKSPACE-DOMAIN` | the workspace bound, the chooser offering `0..L_MAX`, `requireLayer`, the eye on groups above 9, the status text, the parameterized chooser message |
| `T-3D-CODING` | `DQ-A2-9` on the coding arithmetic, without a GL context |
| `T-XML-WRITER` | grammar, canonical order, omission when empty, position after `</construction>`, own line; never in undo, preferences, macro or clipboard XML; deterministic bytes under several default locales |
| `T-XML-READER` | round trip through save and reopen; staging only; ignored contexts with a text log |
| `T-FAIL-CLOSED` | every `DQ-A2-4` case: dedicated exception, structured diagnostic, preflight rejection, localized message in `en` and `es`, previous document, file, saved state and undo history unchanged |
| `T-PERSIST-NO-UNDO` | a change stores no undo point (2-second latch); undo and redo of construction changes, redefine rebuild, atomic rollback, paste rollback and rejected-parse restore leave the set unchanged; save, reopen, then undo: the set stays |
| `T-ROUTES` | every row of the C6 table |
| `T-NEW-OPEN` | New clears; Open commits; a legacy document opens empty; a failed Open keeps the previous set and working layer; startup with a file; `loadXML(String)`; the `DQ-A2-6` routes; Insert File and Apply Template never import the source set |
| `T-OPEN-WORKING-LAYER` | the seven cases of C8 under the `DQ-A2-1` disposition; the persisted set is unchanged by Open |
| `T-SAVED` | `DQ-A2-2`; `hasSaveRelevantContent` unchanged; an otherwise empty document never prompts |
| `T-EFFECTIVE-REOPEN` | Graphics 1, Graphics 2, hit testing, the Algebra View and every picture export and its Save preview give the same effective visibility before saving and after reopening; LaTeX and DXF output unchanged |
| `T-INVARIANCE` | C12 |
| `T-UNITS-INDEPENDENCE` | a unit-bearing document with a hidden set round-trips both; a unit change and its undo leave the hidden set unchanged; a hidden-set change leaves the unit state, its XML and its undo stack unchanged; each fail-closed family reports its own diagnostic |
| `T-LEGACY-BYTES` | the committed base fixtures reproduced for the corpus (C11) |
| `T-OLDER-READER` | a candidate document read by a build of a `git archive` of the base: layers above 9 become 9 and the element is dropped (recorded in the candidate report) |
| `T-COMPAT-CORPUS` | the mandatory corpus of C11 |
| `T-GGBSCRIPT` | the derived GGBScript inventory and its pin, reconciled only as `DQ-A2-8` decides |
| `T-SMOKE` | an explicit author-smoke checklist; the agent does not perform author smoke |

Harness rules: Desktop tests mock `JOptionPane`, inject the spatial-redefine
presentation and the dialog prompts, use an injected preference store, wait for
the asynchronous undo store with latches, install `LoggerD` per test, start in
Move mode, never fire `Ctrl+Shift+C`, `Ctrl+Shift+M` or `Ctrl+Shift+B`, and never
open a real modal dialog (a New step in a test sets the document saved first).
Shared tests log diagnostics as text.

Adjacent regressions before freezing (development evidence, not acceptance):
`PreG9BR6PlusA1LayerWorkspaceTest`, `PreG9BR6PlusA1HiddenLayerTest`,
`PreG9BR6PlusA1LayerSeamTest`, `PreG9BR6PlusBExportSurfaceTest`,
`PreG9BR6PlusBPictureFidelityTest`, the `PreG9BR6PlusD1*` classes,
`GeoCeDGDocumentLifecycleTest`, `G9U1MacroNativeArchivePersistenceTest`,
`G9U1UserToolLibraryTest`, `CopyPasteDTest`, `PreG9bR3C1DesktopClipboardTest`,
`PreG9bR3C1U1DesktopClipboardTest`, `PreG9bR3C1ClipboardCorrectnessTest`,
`RedefineTest`, `TemplateHelperTest`, `G9A1SpatialIdentityXmlTest`,
`G9A3SpatialSnapshotRecoveryTest`,
`PostG9U1A4ConstructionPositionCharacterizationTest`, `CommandsTest`,
`PreG9BR6CapabilityMatrixTest`, `GeometryExportFoundationTest`,
`G9X1G5CorpusCompatibilityTest`, `G9U1ActionRegistryTest`,
`G9U1ProfileCompilerTest`, `G9U1WorkspaceSurfaceTest`, `GeoCeDGProfileTest`,
`G9U0PersistenceCompatibilityTest`; Checkstyle `:shared:common:checkstyleMain`,
`:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleMain` and
`:desktop:desktop:checkstyleTest` (read the XML reports; ASCII escapes in
non-test sources); `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline
9b93256b7df401ff056c37b502d82df4d72b1522`; `git diff --check`.

Catalog: discovery dry-run evidence for both modules and **executed**
`final.shared` and `final.desktop` selection evidence through
`tools/agent/checks/gradle-test-evidence-producer.ps1`, then
`tools/agent/update-verification-junit-inventory.ps1` in-session with
`-DiscoveryEvidencePath` and `-SelectionEvidencePath`, the canonical pin
reproduced with `Get-VerificationCanonicalTextSha256` before repinning, and
absolute paths. No hash is entered by hand. If any registry-shape pin changes,
run a development `INFRA_UNIT` on the staged tree before freezing.

Acceptance (the `GLOBAL_IMPACT` contract), on one clean immutable committed
candidate:

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
stop and report instead of claiming the old receipt. A focal, adjacent, `DEV`,
`PHASE` or `INTEGRATION` PASS never substitutes for it, and it is not repeated
for an unchanged candidate. The two standing diagnostics
(`diagnostic.governance` `DIAGNOSTIC_FINDING`,
`diagnostic.historical-consistency` `DIAGNOSTIC_UNAVAILABLE`) are pre-existing
and do not affect acceptance. A failure proven to predate the candidate and lie
outside its delta is retained baseline debt per the task template, not phase
scope. Report exact commands, exit codes, run ids, plan, result and receipt
hashes and log paths.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

This prompt authorizes nothing. Its existence records a prepared contract.
Execution requires an explicit author instruction naming
`PRE-G9B-R6-plus-A-2`, the exact branch start and the dispositions of
`DQ-A2-1` to `DQ-A2-11`; that instruction may authorize local implementation,
local commits, technical verification, the single `FINAL` and one frozen
technical candidate for author review and smoke. No instruction derived from
this file authorizes self-approval, author smoke by the agent, or any change of
the class.

`A-2` authorizes nothing that follows it. The operational order is
`P0 → A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → F3 → G`; it is
the order the author chose, not a dependency. `F3`, `C`, `E1`, `E2`, `E3`,
`F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized. Author approval is
never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local implementation branch from the exact branch start and local commits
only. Push, branch publication, merge, promotion to `main`, rebase, squash,
amend after freeze, force push, tag, release and binary publication are
forbidden; each needs a separate explicit author instruction naming the exact
candidate SHA. Acceptance evidence never grants publication authority.

## Acceptance and closeout

`A-2` stops with one technically verified candidate pending author review and
author smoke: `PRE-G9B-R6-plus-A-2 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW`,
with `selfApproved = false`. Author approval is an explicit decision naming
the exact accepted commit. The candidate report and its evidence record
`AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT`; a later closeout record is
the sole authority for approval. After approval,
`tools/agent/phase-closeout.ps1 -Action INSPECT` may confirm only the receipt
and candidate identities. Closeout and publication are separately authorized
documentary steps.

## Required artifacts

- The implementation, tests, fixtures and registrations above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-established seams and every correction to C1–C12; the chosen names, hooks
  and defaults; the `DQ-A2` dispositions as implemented; the domain map with its
  Classic proof; the route table of C6 with evidence; the fail-closed matrix;
  the C8 case table with evidence; the effective-visibility-after-reopen
  evidence and the disposition of
  `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS`; the base fixtures and their
  `git archive` reproduction; the older-reader base-tree evidence; the
  obligation-to-test map; residual risks and observations; the author-smoke
  checklist.
- The author-smoke checklist covers at least: choosing working layers 10, 50
  and 99; Properties on an object of layer 99; hiding layers, saving, closing
  and reopening; undo and redo after hiding; the working layer after opening
  each `DQ-A2-1` case; New after a document with hidden layers; the save prompt
  of `DQ-A2-2`; opening a legacy document; rejection of invalid hidden-layer
  metadata; paste and Insert File between documents with different sets; the
  picture export and its Save preview after reopen; Graphics 2; a unit-bearing
  document with hidden layers; the Classic diagnostic application; and the
  documented older-reader loss.
- Machine-readable evidence beside it, following existing conventions.
- The bootstrap-impact outcome and rationale, the
  verification-infrastructure-impact assessment, `GUIDE_IMPACT` with paths,
  `SERIALIZATION CHANGE` stating the widened `<layer val>` domain and the new
  element, and the exact `FINAL` commands, exit codes and log paths.
- Technical verification distinguished from author approval; incomplete gates
  reported explicitly.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- correctness requires widening Classic or Web to `0..99`, or raising
  `EuclidianStyleConstants.MAX_LAYERS`;
- correctness requires changing object visibility to persist hidden layers;
- correctness requires making hidden layers geometric, DAG, construction or
  object state;
- correctness requires making hidden-layer changes undoable, or a material
  restructuring of the general undo architecture;
- correctness requires serializing `workingLayer`;
- correctness requires per-view hidden sets instead of the document-wide set;
- a `C`-owned exporter, specification or verifier would change;
- unit semantics or the `<geocedgUnits>` contract would change;
- the accepted `AQ-L1a`, `AQ-L1c`, `AQ-L3` or `AQ-L7` decisions would change,
  or a `DQ-A2` question is reached without an author disposition;
- a route of the C6 table cannot keep its row;
- the product bound cannot be supplied by configuration without a material
  upstream or kernel redesign;
- the persistence shape would have to differ materially from C5;
- the base fixtures cannot be reproduced on the unchanged base;
- the work would belong to `F3`, `C`, `E1`, `E2`, `E3`, `F1`, `F2` or `G`, or to
  the rollback GUI debt;
- current governance requires a verification class other than
  `GLOBAL_IMPACT`;
- the `FINAL` is rejected for a cause attributable to the candidate, or its
  coverage is incomplete or untrusted;
- product or test code would change after `FINAL`.

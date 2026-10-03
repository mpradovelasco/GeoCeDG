# PRE-G9B-R6-plus-A-2 — layer-domain widening and persistent hidden layers

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR IMPLEMENTATION AND TECHNICAL
VERIFICATION ONLY.**

The author's explicit instruction of 2026-10-03 approves the documentary
preparation package of `PRE-G9B-R6-plus-A-2` (`T_R6PLUS_A2_PROMPT`), names it as
the exact start of the implementation branch, gives the author dispositions on
`DQ-A2-1` to `DQ-A2-11`, accepts the prepared architecture, adds regression
obligations and stop conditions, keeps `GLOBAL_IMPACT / FINAL`, and authorizes
the implementation of `A-2`. The same instruction approves the `F3` planning
reconciliation as planning only. The instruction is recorded, versioned, in the
[A-2 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md).
It also requires this amendment, as the first tracked edit of the phase, so
that the prompt becomes the executable contract of the phase. The amendment
replaces the prepared prompt of `T_R6PLUS_A2_PROMPT` (blob
`459dbf5dc620a966d8dd593ae899010470789043`): it records the authorized branch
start, integrates the `DQ-A2` dispositions, corrects the defaults they
supersede (the saved state of `DQ-A2-2`, the commit context of `DQ-A2-6`, the
cap wording of `DQ-A2-7` and the renderer-adapter boundary of `DQ-A2-9`), and
freezes the class. Where the prepared prompt and the record differ, the record
prevails; this amendment carries that precedence into the text. Every other
scope, forbidden-scope and stop rule of the prepared prompt is kept. The
preparation decisions are recorded in the
[A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md);
the evidence behind the characterization below is in the
[A-2 preparation characterization report](../../../docs/validation/pre_g9b_r6_plus_a2_preparation_characterization_report.md).

This file is an execution contract, not a second policy document: the layer
decisions are stated once in the
[A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md)
(`AQ-L1a`–`AQ-L8`) and the `DQ-A2` dispositions once in the authorization
record; this prompt cites them and does not restate them differently.

```text
PRE-G9B-R6-plus-A-2 =
AUTHORIZED FOR IMPLEMENTATION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (A-2 implementation and technical verification only)
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
DEPENDS_ON_PACKAGE       = A-2 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
                                                 (not published)
AQ-L7(3)                 = decided by the author as DQ-A2-1 (2026-10-03)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-C     (operational order; not authorized)
STOP_STATE               = PRE-G9B-R6-plus-A-2 = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
```

`implementationAuthorized = true` authorizes only the implementation and
technical verification defined below. It authorizes no later subphase and no
`F3` work. `authorApproved = false` means that no technical candidate of this
phase has been author-approved. Technical verification never creates author
approval. The phase stops with one exact technically verified candidate
pending author review and author smoke, with `selfApproved = false`,
`authorApproved = false` and `passClaimed = false`. The agent does not perform
or claim author smoke.

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
                                               preparation instruction and the
                                               authorization of 2026-10-03)
frozenAtPhaseStart   = true
PLANNED_ACCEPTANCE   = FINAL — one and only one, on one exact frozen clean candidate,
                       after FINAL -PlanOnly; no downgrade because edits look small
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` maps
`GLOBAL_IMPACT` to FULL, whose canonical profile is `FINAL`. The impact facts
that select it: the admissible range of the shared `<layer val>` element, read
on every load and written on every save, undo and rebuild snapshot, widens for
GeoCeDG; a new document-level element enters the shared full-document writer
and the shared top-level reader; a fail-closed rule (`DQ-A2-4`) enters the
shared parser used by the Open preflight; the saved-state predicate consumed
by every New, Open and close prompt gains a document-presentation seam
(`DQ-A2-2`); the shared
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
| same record `AQ-L7(3)` | decided by the author as `DQ-A2-1` (2026-10-03) |
| [A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md) | the kept class, normative scope, mandatory characterization, forbidden scope and stop conditions |
| [A-2 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md) | the authorization, the `DQ-A2-1`–`DQ-A2-11` dispositions (they prevail over the prepared defaults), the accepted architecture, the twelve additional regression obligations, the author-smoke checklist content and the additional stop conditions |
| [A-1 closeout record](../../../docs/validation/pre_g9b_r6_plus_a1_closeout_record.md) and the accepted `A-1` implementation | the workspace, predicate and invariants `A-2` extends; `OBS-R6PLUS-A1-EXPORT-PREVIEW-HIDDEN-LAYERS` to revalidate under persistence |
| [D1 closeout record](../../../docs/validation/pre_g9b_r6_plus_d1_closeout_record.md) and the [D1 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d1_prompt_closeout_record.md) | the current serialization landscape, the Open transaction, the `DQ-D1-5` save-relevance seam (which `DQ-A2-2` extends for a non-empty hidden set through a separate document-presentation seam) and `DQ-D1-4` (localized load diagnostics) |
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
| save relevance of a non-empty hidden set (`DQ-A2-2`) | one `App` document-presentation seam consulted by `App.isSaved`, upstream default `false`, answered by `AppGeoCeDG` from the workspace | no duplicate of the set in `Construction` |
| commit points, the working-layer rule after Open, save-state notification, chooser, Algebra View eye, status | Desktop `AppGeoCeDG` and its workspace classes | orchestration and presentation |

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
**contract** is binding on the implementation; a result marked **finding** is
evidence that a contract rests on. The author accepted the prepared
architecture on 2026-10-03; where a `DQ-A2` disposition below changes a
contract, the disposition governs.

#### C1. Layer-domain authority map (`A2-C1`)

There is one constant, `EuclidianStyleConstants.MAX_LAYERS = 9`
(`common/plugin/EuclidianStyleConstants.java:186`). Eight places enforce it;
every other consumer works with any non-negative bounded integer.

| Site | Evidence | Class | Under GeoCeDG `0..99` |
|---|---|---|---|
| clamp of every route (XML, `SetLayer`, API, properties, the `A-1` seam) | `common/kernel/geos/GeoElement.java:1001-1018` | semantic authority / validation | reads the configured bound |
| highest-used bookkeeping | `common/main/App.java:1283-1291` (via `common/kernel/Kernel.java:4899-4900`) | validation (second clamp) | reads the configured bound |
| default layer of non-interactive new objects, top layer reserved | `common/kernel/ConstructionDefaults.java:907-926` | semantic rule | `DQ-A2-7`: the structural cap `min(L_MAX − 1, maxLayerUsed)` with the configured bound; a cap, not a default layer |
| `ShowLayer`/`HideLayer` argument range | `common/kernel/scripting/CmdShowHideLayer.java:58-62` | validation (silent no-op) | `DQ-A2-8` |
| API `setLayerVisible` range | `common/plugin/GgbAPI.java:398-401` | validation (silent no-op) | `DQ-A2-8` |
| Properties layer combo (index is the layer) | `common/gui/dialog/options/model/LayerModel.java:40-58`; `desktop/gui/dialog/ComboPanel.java` | UI bound | `DQ-A2-11`; a layer above the list length makes `setSelectedIndex` throw |
| property collection of the newer UI | `common/properties/impl/objects/LayerProperty.java:48-53` | UI bound (shared, used by Web) | reads the configured bound |
| GeoCeDG session bound | `geocedg-desktop/GeoCeDGLayerWorkspace.java:29-31`, `:34`, `:72`, `:171`, `:186-190`; chooser `geocedg-desktop/GeoCeDGWorkingLayerChooser.java:31-44`; message `apps/geocedg/application-profile.yml:5232-5235` | UI/session bound | reads the configured bound; message parameterized |
| 3D render coding (layer packed into alpha, depth bias) | `common/geogebra3D/euclidian3D/openGL/BufferPack.java:218-224`; `common/geogebra3D/main/VertexShader.java:105-110`, `:133`, `:152-153`; Desktop GL2 fallback `desktop/geogebra3D/euclidian3D/opengl/RendererImplGL2.java:711-713` | renderer encoding | decodes correctly on Desktop float32; depth bias grows about tenfold at 99; `DQ-A2-9`: a presentation adapter may clamp the renderer input only, never the model layer |
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
  undoable by design. The element name, grammar and position are accepted by the
  author (`DQ-A2-3`); the grammar applies unless implementation evidence reveals
  a contradiction, which stops the phase for author review:

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
    which validates (`DQ-A2-4`, fail closed) and records the value for the app
    (C6);
  - ignored with a text log in macro-kernel parses (`.ggt`, `addMacroXML`), in
    generic merges, paste and every other non-document parse; Classic and Web
    keep the upstream log-and-ignore behavior (`DQ-A2-5`); the macro part of an
    archive (`common-jre/io/MyXMLioJre.java:200-208`) never carries the element,
    and a value recorded there is superseded by the archive's own document
    parse, which always follows it.

#### C6. Persistence without undo (`A2-C6`)

The reader cannot tell an undo restore from a document load: both are clearing
parses with `NATIVE_OR_UNDO_RESTORE` (`MyXMLio.java:420-422`), and the lazy
writer makes an absent element ambiguous. The decision therefore combines the
effective load purpose of the parse with the document-replacement context
established by the live architecture, never a method name alone (`DQ-A2-6`):

```text
parser          : a parse whose effective load purpose (the D1 rule: the construction's
                  pending purpose read without consuming it, else the parser default) is
                  NATIVE_OR_UNDO_RESTORE, REDEFINE_REBUILD, ORDINARY_EDIT_REBUILD or
                  ROLLBACK_RESTORE, on a non-macro kernel whose configuration persists
                  hidden layers, validates a present element (fail closed) and, only when
                  the whole parse completes, reports its set to the app (empty when absent)
commit context  : a document replacement established by the live architecture — the
                  committed native-document load transaction of AppD (every .cedg/.ggb
                  Open route, including the final loadXML(File) and loadXML(URL) routes),
                  the clearing replacements App.setXML(xml, true) and AppD.loadXML(String)
                  (both reset or set the current file), New, and startup; after it
                  succeeds, the last reported set (empty when none) replaces the workspace
                  set and the working layer follows DQ-A2-1
any other route : no commit; undo, redo, rebuild, rollback and every merge leave the
                  workspace set untouched; a reported set outside a transition is discarded
                  when the next transition starts
failure         : no commit; the previous set and working layer stay exactly
```

| Route | Evidence | XML parsed | Element | Hidden set after |
|---|---|---|---|---|
| save, `App.getXML()` | `MyXMLioJre.java:331-401`; `common/main/App.java:1851-1853` | — | written when non-empty | — |
| undo store | `common/main/undo/UndoManager.java:284-287` → `Construction.getCurrentUndoXML` → `MyXMLio.getUndoXML` (`:140-169`) | — | never written | — |
| undo, redo | `desktop/main/undo/UndoManagerD.java:184-244` → `MyXMLioJre.readZipFromMemory`; headless `DefaultUndoManager` | undo | absent | unchanged |
| redefine rebuild, CAS change, collected redefines, failed-redefine restore | `Construction.java:2126`, `:2146`, `:4217-4249` | undo | absent | unchanged |
| atomic mutation rollback | `Construction.java:2698-2732` | undo | absent | unchanged |
| rejected-parse restore | `MyXMLio.java:405-406`, `:511-536` | full (`app.getXML()`) | present = current value | unchanged (reported only) |
| paste rollback, Insert File spatial rollback | `common/util/InternalClipboard.java:623`, `:663-685`; `desktop/util/CopyPasteD.java:672` | full | present = current value | unchanged (reported only) |
| paste, `evalXML`, action replay | `InternalClipboard.java:626-640`; `common/plugin/GgbAPI.java:168-180` | construction-wrapped | cannot occur at top level | unchanged |
| File → Open `.cedg`/`.ggb`, archives with macros, Open Recent, reset reload | `geocedg-desktop/AppGeoCeDG.java:605-620` → `desktop/main/AppD.java:3074-3087`, `:3144-3161`, `:3253-3312` | full | present or absent | **commit** (C7) |
| failed live load after preflight | `AppD.java:3301-3307`, `:3333-3368` (the rollback snapshot is a full archive) | full | previous value | previous set and working layer unchanged exactly; the report is discarded |
| `loadXML(String)` | `AppGeoCeDG.java:622-634` | full | present or absent | **commit** |
| startup with a file | the host constructor parses before the workspace exists (`AppGeoCeDG.java:607-611`, `:319-332`) | full | present or absent | **commit** in `initializeLayerWorkspace` |
| File → New, startup without a file | `AppGeoCeDG.java:586-603` | — | — | empty |
| clearing `App.setXML` (public API, tool-creation reload, edit-macro switch, preferences loads) | `common/main/App.java:5114-5134`, `:1733-1747`; `common/plugin/GgbAPI.java:1473-1477`; `desktop/gui/dialog/ToolCreationDialogD.java:294`; `desktop/main/AppD.java:483`, `:506`, `:1175` | full or preferences | present or absent | **commit** when a document parse completed (`DQ-A2-6`); a reload of the same document re-commits its own set |
| `loadXML(URL)` and `loadXML(File)` of a `.cedg`/`.ggb` (API open, `openURL`) | `AppD.java:3094-3100`, `:3218-3251`; `desktop/gui/GuiManagerD.java:1838-1842`, `:1864`, `:1888`, `:1893` | full | present or absent | **commit** through the native-document transaction (`DQ-A2-6`; `OBS-A1-URL-OPEN` for hidden layers and the working layer) |
| Base64 archives and non-native URL documents (`App.loadXML(byte[])`, the non-transactional branch of `loadXML(URL)`) | `common/main/App.java:4233`; `AppD.java:3231-3241` | full | present or absent | not a native-document transaction: no commit, recorded as a residual observation; a Base64 startup argument commits at startup |
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
  - Open and every commit context of C6: the recorded report is cleared when the
    transition starts; after a successful load the workspace takes the reported
    set or the empty set, then chooses the working layer by `DQ-A2-1`. A legacy
    document therefore opens with an empty set.
  - Open never writes the persisted set merely to satisfy the working-layer
    initialization, never unhides a persisted hidden layer, never infers hidden
    layers from object visibility, and never marks the document modified.
  - A failed load leaves the previous set, working layer, file, saved state and
    undo history exactly unchanged.
  - The report fields of `AppGeoCeDG` have no field initializer, because the
    host constructor parses a startup file before the subclass initializers
    run (the precedent of `AppGeoCeDG.java:83-89`); a failed startup load leaves
    a blank document whose set is empty.

#### C8. `AQ-L7(3)`: Open and persistent hidden layers (`A2-C8`)

`A-1` fixes: the working layer cannot be hidden through normal interaction
(`AQ-L7(1)`, `GeoCeDGLayerWorkspace.java:90-101`), and choosing a hidden layer
as working shows it (`AQ-L7(2)`, `:56-65`). After Open, the `A-1` rule takes the
highest drawable layer. With persistence, that layer can be hidden.

| Case | `A-1` rule unchanged | `DQ-A2-1` (author disposition) |
|---|---|---|
| 1. highest used drawable layer is shown | it | it |
| 2. highest used drawable layer is hidden | a hidden working layer (breaks `AQ-L7(1)`) | the highest used **shown** layer |
| 3. layer 0 is hidden | unaffected unless 0 is the fallback | the fallback skips it |
| 4. all used layers are hidden | a hidden working layer | the lowest non-hidden layer of `0..L_MAX` (possibly unused) |
| 5. all layers `0..L_MAX` are hidden | a hidden working layer | invalid persisted metadata: the document fails closed with an explicit diagnostic (`DQ-A2-1`, `DQ-A2-4`); a GeoCeDG writer never produces it, because the working layer is never hidden when the set is saved |
| 6. no drawables | 0 | the lowest non-hidden layer (0 unless 0 is hidden) |
| 7. the only candidate is on a hidden layer | a hidden working layer | as 2 or 4 |

**Contract.** The persisted set is never modified by this rule; the working
layer is session state and is never serialized. The status bar shows the
chosen working layer; nothing else changes. The `A-1` session degeneracy of
every layer being hidden never authorizes writing an invalid persisted set.

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
| `OBS-A1-3D-VIEW` | interaction: the domain widening reaches the 3D render coding; `DQ-A2-9` allows only a renderer-input adapter, never the model layer; layers above the renderer's distinguishable range may lose distinct 3D draw-order encoding (residual presentation limitation); hidden layers still do not apply in 3D | author |
| `OBS-A1-NON-HIT-SELECTION` | interaction: persistent sets make reopened documents with hidden objects common, so Select All and keyboard selection reach them more often; no change in `A-2` | author |
| `OBS-A1-RECOMPUTE-OUTPUTS` | unaffected in nature; outputs may now take a working layer in `10..99` | none required |
| `OBS-A1-URL-OPEN` | interaction: with persistence a stale set would survive the API URL route; under `DQ-A2-6` the native-document transaction commits for `.cedg`/`.ggb` URL and file routes; Base64 and non-native URL documents stay outside (C6) | `A-2` for the native routes; the remainder kept as observation |

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
| `A-1` tests (C1) | the chooser offers exactly `0..9`; the GeoCeDG default cap is 8; a toggle leaves the document saved and writes nothing | true for `A-1` | changed only as `DQ-A2-2`, `DQ-A2-3`, `DQ-A2-7` and `DQ-A2-11` decide |
| `D1` record `DQ-D1-5`; `Construction.java:4034-4044` | presentation and session state never count as save-relevant content | true at the base | `DQ-A2-2` makes a non-empty persistent hidden set save-relevant through a separate `App` document-presentation seam; `Construction` and its unit seam are unchanged |
| `P0` persistence matrix, row `workingLayer` | after Open, the highest drawable layer | `A-1` implements it capped at 9 | refined by `DQ-A2-1` |

A contradiction that would change scope, owner, serialization or class stops
the phase.

### Author dispositions on `DQ-A2-1` to `DQ-A2-11`

The prepared prompt fixed a default contract for each requested decision. The
author disposed of all of them on 2026-10-03; the
[A-2 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md)
is their authority and prevails over the prepared defaults. In substance:

| ID | Outcome | Contract |
|---|---|---|
| `DQ-A2-1` | accepted with precision | C8: after a successful Open, the highest used drawable layer that is not hidden; if none is visible, the lowest non-hidden layer of `0..L_MAX`; the persisted set is never modified to choose a working layer; the working layer is session state and never serialized; a persisted set containing every layer of `0..L_MAX` is invalid and fails closed with an explicit diagnostic; hidden layers are never inferred from object state |
| `DQ-A2-2` | **replaced in part** | a hidden-layer toggle is not undoable and creates no undo point, but it is persistent, so it marks the document unsaved whenever it changes the persisted set (an Algebra View eye toggle, or a working-layer choice that shows a hidden layer); a working-layer change alone, and every commit, do not mark it. A non-empty hidden set is save-relevant document content: an otherwise geometrically empty document prompts when its hidden state changed and was not saved; an untouched empty document with an empty set stays saved. `persistent != undoable`. The set is not duplicated in `Construction`: `App.isSaved` consults one `App` document-presentation seam (upstream default `false`) that `AppGeoCeDG` answers from the workspace, the single runtime owner. If this needs duplicating the authoritative set or materially redesigning general save semantics, stop and report |
| `DQ-A2-3` | accepted | C5: `<geocedgHiddenLayers version="1" layers="…"/>`, a direct child of the full document XML outside `<construction>`, after `</construction>`, written only by `getFullXML`; lazy; canonical; document-wide; never in preferences, macro, `.ggt`, clipboard or undo XML; the prepared grammar unless evidence reveals a contradiction (stop for author review) |
| `DQ-A2-4` | accepted | fail closed, with the `D1` transaction (preflight first) and localized (`en`, `es`) diagnostics naming the defect, on: a newer `version`; a missing or non-integer `version`; an attribute outside the grammar; a malformed list (sign, leading zero, not ascending, duplicate, empty, other separators); a value above the reader's `L_MAX`; a set covering the whole domain; a child element or text; a second element; the element anywhere but directly under `<geogebra>` in a document parse. Recognized `A-2` metadata is never silently discarded; older readers may drop it |
| `DQ-A2-5` | accepted | Classic stays `0..9`; Classic and Web acquire no hidden-layer product semantics: a configuration default method keeps persistence off, so they keep the upstream log-and-ignore behavior and write nothing; no Classic or Web hidden-layer UI or runtime authority; no frontend broadening; stop if a shared change would make Classic accept layers above 9 as authoritative |
| `DQ-A2-6` | accepted with precision | C6: the set is applied only in an effective full-document, clearing document transition whose effective load purpose denotes replacement of the document — New, the native-document Open transaction (all `.cedg`/`.ggb` routes, including the API file and URL routes), the clearing `App.setXML` and `AppD.loadXML(String)` replacements, and startup; never from `evalXML`, generic merges, paste, Insert File, Apply Template, macro loads, `.ggt` or non-clearing fragments; decided from the load purpose and context, not from a method name alone; a failed Open restores the previous hidden state exactly |
| `DQ-A2-7` | **wording replaced** | the upstream structural rule `min(MAX_LAYERS − 1, app.getMaxLayerUsed())` is kept with its bound read from the configured authority: `min(L_MAX − 1, maxLayerUsed)`, so GeoCeDG's cap is 98 and Classic's stays 8 — caps, not default layers; no object lands on 98 merely because the domain is `0..99`; the `A-1` creation hook stays authoritative for interactive GeoCeDG creation and is neither bypassed nor redefined |
| `DQ-A2-8` | accepted | `ShowLayer`, `HideLayer` and API `setLayerVisible` accept the configured domain (GeoCeDG `0..99`, Classic `0..9`) with unchanged object-visibility semantics (`AQ-L5`); never rerouted to the hidden-layer presentation; the GGBScript capability matrix rule is re-applied and its instance reconciled through the official mechanism if the derived inventory changes |
| `DQ-A2-9` | accepted only as a presentation compatibility adapter | the 3D renderer input may be clamped to its current Classic-compatible range; the adapter never writes `GeoElement.layer`, XML, the API-visible layer or the domain, and never creates a semantic layer value; layers above the renderer's distinguishable range may lose distinct 3D draw-order encoding (a recorded residual presentation limitation); no hidden-layer behavior in 3D; stop if safe adaptation needs geometric or spatial semantics |
| `DQ-A2-10` | accepted | the clamp on read is kept against the effective product domain: GeoCeDG `< 0 → 0`, `> 99 → 99`; Classic `< 0 → 0`, `> 9 → 9`; no wrap, no inferred migration; distinct from malformed hidden-layer metadata, which fails closed |
| `DQ-A2-11` | accepted | every GeoCeDG layer picker or chooser that represents the product domain (the Properties combo, whose index is the layer, and the working-layer chooser) derives `0..L_MAX` from the configured authority; texts carry the bound as a parameter; no literal `99` in Desktop; Classic keeps its domain |

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BRANCH_START =
0e50626f70107376e691df6dea7bfcfe06de5c0a          (T_R6PLUS_A2_PROMPT, approved
                                                   preparation package; not published)

IMPLEMENTATION_BRANCH_START_TREE =
fefc57cece8090941ad0ac16098e58a679a290ec

PREPARATION_BASE =
f467eb4563de7a7eb162da1b74954f9f847fa9d3          (T_R6PLUS_F3_PLAN, approved F3 planning;
                                                   tree ece51c96117d6cbd55bc200d29c5240686516da9)

PUBLISHED_BASE =
2941ddf2f2712340e831d292bdc2687b879850d6          (P_R6PLUS_D1, published D1 closeout;
                                                   tree 158184b150a42e6dcae2946c39620d92cbfcd888)

IMPLEMENTATION_BRANCH =
phase/pre-g9b-r6-plus-a2-layer-domain-persistence (local; created from the exact
                                                   branch start; never rebased)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal `P_R6PLUS_D1` and its tree; `T_R6PLUS_A2_PROMPT` exists
with its tree and descends from `P_R6PLUS_D1` through `T_R6PLUS_F3_PLAN` only;
the worktree is clean. The phase works on its implementation branch from the
exact branch start and is not rebased onto any later commit without a new
author instruction. The prepared candidate stays immutable: this amendment is a
new commit on top of it. Between `P_R6PLUS_D1` and the branch start only
documentation changes, so the citations above still apply; the phase
re-establishes every one it relies on before using it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source, tests, build and serialization at the base.
3. The [A author-decision record](../../../docs/validation/pre_g9b_r6_plus_a_author_decisions_record.md),
   the [A-2 author-decision record](../../../docs/validation/pre_g9b_r6_plus_a2_author_decisions_record.md)
   and the [A-2 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md)
   with the author's dispositions of `DQ-A2-1` to `DQ-A2-11`.
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
- `MyXMLio.getFullXML`: one writer call; `MyXMLio.doParseXML`: the parse
  classification, success report and rejected-parse restore trigger beside the
  `D1` unit calls; `MyXMLHandler`: one top-level reader case with misplacement,
  duplicate, child and text detection (`DQ-A2-4`); `App`: the writer, report
  and save-relevance hooks with upstream-identical defaults, `App.isSaved`
  through the document-presentation seam (`DQ-A2-2`); the narrowest
  error-message seam for a rejected element, reusing the `D1` diagnostic route.
- A renderer-input adapter for the 3D layer coding (`DQ-A2-9`), presentation
  only, never writing the model layer.
- Kernel localization keys for the diagnostics, if raised from shared code
  (`menu*.properties` are mixed-EOL; new lines with LF).

### Desktop host and GeoCeDG layer

- `AppD`: one protected hook, upstream-identical by default, after a committed
  native-document load transaction (the commit context of C6 for every
  `.cedg`/`.ggb` route, including the final `loadXML(File)`/`loadXML(URL)`
  routes); registered in `docs/upstream/modified-files.yml`.
- `GeoCeDGLayerWorkspace`: the configured bound, the commit of a reported set,
  the `DQ-A2-1` rule, the `DQ-A2-2` notification of user changes of the
  persisted set.
- `AppGeoCeDG`: the writer, report and save-relevance hooks, the commit contexts
  of `DQ-A2-6` (Open, `loadXML(String)`, the clearing `setXML`, the
  native-transaction hook, New, startup), the failure path, `setUnsaved` for
  user changes of the persisted set, the error messages.
- `GeoCeDGWorkingLayerChooser`, `GeoCeDGAlgebraView`, `GeoCeDGStatusBar` and the
  profile text `Workspace.Layer.ChooserMessage`: the configured bound only.

### Supporting changes

- Tests for every obligation below and the fixture documents, registered in
  `docs/upstream/modified-files.yml` where they live under `source/`.
- The GGBScript capability-matrix instance, its static-contract input pin and
  the registry `static_contracts` pin, through the official mechanism, if the
  `DQ-A2-8` edit of `CmdShowHideLayer` changes the derived inventory.
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
  `ShowLayer`/`HideLayer` semantics (`AQ-L5`) or rerouting them to the
  hidden-layer presentation; making hidden layers geometric, DAG, construction
  or object state; duplicating the hidden set in `Construction`; serializing
  `workingLayer`; making a hidden-set change undoable; changing the `A-1`
  hidden-layer predicate.
- Resolving or implementing the open `F3` question on Apply Template
  re-layering, and any further `F3` preparation or implementation.
- Widening Classic or Web to `0..99`; raising
  `EuclidianStyleConstants.MAX_LAYERS`; repeating `99` outside the GeoCeDG
  configuration.
- The rollback GUI debt `OBS-D1-ROLLBACK-CONSPROT-GUI`; any other `A-1`
  observation beyond C10.
- Opportunistic 3D-layer behavior beyond the renderer-input adapter of
  `DQ-A2-9`; any write of the 3D adaptation back to the model layer; hidden-layer
  behavior in the 3D view.
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
| hidden set covering `0..L_MAX` | invalid persisted metadata; fails closed (`DQ-A2-1`, `DQ-A2-4`) |
| hidden layer with no object | kept and persisted; never pruned |
| object visible flag false on a shown layer | stays hidden; its flag is never written by `A-2` |
| hidden-set change | document unsaved; no undo point (`DQ-A2-2`) |
| undo after a hidden-set change | the set is unchanged |
| object on a layer above the 3D renderer's distinguishable range | its model layer is unchanged; its 3D draw-order coding may coincide with the range end (`DQ-A2-9`) |

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
| `T-DOMAIN-CONSUMERS` | `updateMaxLayerUsed`; the default cap (`DQ-A2-7`: a cap on `maxLayerUsed`, never a default layer; no object lands on 98 unless the highest used layer is at least 98); macro and Locus V2 outputs; `GeoList` and polyhedron propagation; `ShowLayer`, `HideLayer`, `setLayerVisible` (`DQ-A2-8`); `LayerProperty`; numeric Algebra View order over `0..99`; the hit-test top-layer rule and the draw order with layers above 9 |
| `T-CLASSIC` | with `AppConfigDefault`: every probe value of C3 gives the base result; `ShowLayer(42)` is a no-op; the Properties combo has 10 entries; the hidden-layer element is logged, ignored and not re-written (`DQ-A2-5`); a corpus document is byte-identical |
| `T-WORKSPACE-DOMAIN` | the workspace bound, the chooser offering `0..L_MAX`, `requireLayer`, the eye on groups above 9, the status text, the parameterized chooser message |
| `T-3D-CODING` | `DQ-A2-9` on the coding arithmetic, without a GL context: the renderer input is clamped, the model layer, its XML and the API layer are never changed |
| `T-XML-WRITER` | grammar, canonical order, omission when empty, position after `</construction>`, own line; never in undo, preferences, macro or clipboard XML; deterministic bytes under several default locales |
| `T-XML-READER` | round trip through save and reopen; report only, never applied by the parser; ignored contexts with a text log |
| `T-FAIL-CLOSED` | every `DQ-A2-4` case, including the all-domain set: dedicated exception, structured diagnostic, preflight rejection, localized message in `en` and `es`, previous document, hidden set, working layer, file, saved state and undo history unchanged |
| `T-PERSIST-NO-UNDO` | a change stores no undo point (2-second latch); undo and redo of construction changes, redefine rebuild, atomic rollback, paste rollback and rejected-parse restore leave the set unchanged; save, reopen, then undo: the set stays |
| `T-ROUTES` | every row of the C6 table |
| `T-NEW-OPEN` | New clears; Open commits; a legacy document opens empty; a failed Open restores the previous set and working layer exactly; startup with a file; `loadXML(String)`; the clearing `setXML`; the native API file and URL routes; Insert File, Apply Template, paste, `evalXML` and macro loads never import a set |
| `T-OPEN-WORKING-LAYER` | the seven cases of C8 under the `DQ-A2-1` disposition; Open never unhides a persisted hidden layer; the persisted set is unchanged by Open |
| `T-SAVED` | `DQ-A2-2`: a toggle that changes the persisted set marks the document unsaved and stores no undo point; a geometrically empty document with a non-empty set is save-relevant and prompts; an untouched empty document with an empty set stays saved; a working-layer change alone and every commit do not mark it; `Construction.hasSaveRelevantContent` and `isStarted` unchanged |
| `T-EFFECTIVE-REOPEN` | Graphics 1, Graphics 2, hit testing, the Algebra View and every picture export and its Save preview give the same effective visibility before saving and after reopening; LaTeX and DXF output unchanged |
| `T-INVARIANCE` | C12 |
| `T-UNITS-INDEPENDENCE` | a unit-bearing document with a hidden set round-trips both; a unit change and its undo leave the hidden set unchanged; a hidden-set change leaves the unit state, its XML and its undo stack unchanged; each fail-closed family reports its own diagnostic |
| `T-LEGACY-BYTES` | the committed base fixtures reproduced for the corpus (C11) |
| `T-OLDER-READER` | a candidate document read by a build of a `git archive` of the base: layers above 9 become 9 and the element is dropped (recorded in the candidate report) |
| `T-COMPAT-CORPUS` | the mandatory corpus of C11 |
| `T-GGBSCRIPT` | the derived GGBScript inventory and its pin, reconciled only as `DQ-A2-8` decides |
| `T-SMOKE` | an explicit author-smoke checklist; the agent does not perform author smoke |

The twelve additional regression obligations of the authorization record are
each proven explicitly and mapped in the candidate report: (1) a toggle changes
the saved state but adds no undo point (`T-SAVED`, `T-PERSIST-NO-UNDO`); (2) a
geometrically empty document with a non-empty set is save-relevant
(`T-SAVED`); (3) undo and redo never change the set (`T-PERSIST-NO-UNDO`); (4)
a failed Open restores the set exactly (`T-NEW-OPEN`, `T-FAIL-CLOSED`); (5) Open
never silently unhides a persisted hidden layer (`T-OPEN-WORKING-LAYER`); (6)
all-domain persisted metadata fails closed (`T-FAIL-CLOSED`); (7) Classic stays
clamped to `0..9` (`T-CLASSIC`); (8) GeoCeDG accepts and persists 10, 50 and 99
(`T-DOMAIN-ROUTES`); (9) the `L_MAX − 1` rule is a cap on `maxLayerUsed`
(`T-DOMAIN-CONSUMERS`); (10) the 3D adapter never mutates the model layer
(`T-3D-CODING`); (11) `UnitState` and the hidden set round-trip independently
(`T-UNITS-INDEPENDENCE`); (12) `B`'s preview/final-export consistency holds after
save and reopen (`T-EFFECTIVE-REOPEN`).

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

The author's instruction of 2026-10-03, recorded in the
[A-2 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_a2_prompt_closeout_record.md),
authorizes only the scope above: local implementation on the implementation
branch, local commits, technical verification, the single `FINAL` and one
frozen technical candidate for author review and smoke. It does not authorize
self-approval, author smoke by the agent, or any change of the frozen class.

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
- The author-smoke checklist is concise and covers at least the content the
  authorization record requires: creation and selection of layers 0, 10, 50 and
  99; chooser bounds; Classic stays `0..9`; hide layers, save, close and reopen;
  hidden state survives reopen; the working layer after Open in the
  hidden-highest-layer case; the all-used-hidden case; rejection of malformed
  and all-domain hidden metadata; a hidden-layer toggle takes no part in
  undo/redo; a hidden-layer change makes the document unsaved; a legacy
  document opens with an empty hidden set; `B`'s picture preview and export stay
  coherent after reopen; `D1` units coexist with persistent hidden layers.
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
- implementing the saved-state semantics of `DQ-A2-2` requires duplicating the
  hidden set in `Construction` or materially redesigning general save
  semantics;
- an all-domain hidden persisted document cannot be rejected cleanly without
  materially changing the load architecture;
- renderer compatibility requires modifying the authoritative model layer, or
  safe 3D adaptation requires geometric or spatial semantics;
- correctness requires changing the `A-1` hidden-layer predicate or
  object-visibility semantics;
- `F3` or `C` work becomes necessary;
- correctness requires widening Classic or Web to `0..99`, making Classic accept
  layers above 9 as authoritative, or raising
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
- the accepted `AQ-L1a`, `AQ-L1c`, `AQ-L3` or `AQ-L7` decisions or a `DQ-A2`
  disposition would change, or the `DQ-A2-3` grammar meets contradicting
  implementation evidence;
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

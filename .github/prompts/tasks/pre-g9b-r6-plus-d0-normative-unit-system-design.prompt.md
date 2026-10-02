# PRE-G9B-R6-plus-D0 — normative unit-system design

**CANONICAL PROMPT — PREPARED ON AN EXACT BASE — UNEXECUTED AND NOT AUTHORIZED.**

This prompt was prepared at the author's instruction of 2026-10-02. That
instruction fixed the unit decisions for `PRE-G9B-R6-plus-D0`, named the exact
published base below, and authorized only the documentary preparation of this
prompt, the record of the decisions and the status updates they strictly
require. It stated that it **does not authorize the execution of `D0` or
`D1`**. The decisions are recorded, versioned, in the
[D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md).
The existence of this file is not authorization.

Execution requires a new explicit author instruction that names
`PRE-G9B-R6-plus-D0` and confirms or replaces the base below. That instruction
may authorize, as the first tracked edit of the phase, an amendment of this
prompt to the authorized state, following the `P0`, `A-1` and `B` precedent.
This file is an execution contract, not a second policy document: the unit
semantics live in the mini-track plan §4 and the author-decision record now,
and in the ADR and specification `D0` writes once they are author-accepted.
The design input is the `P0`
[D0 unit-system inputs](../../../docs/architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md),
as amended by the author decisions and re-characterized below against the
base.

```text
PRE-G9B-R6-plus-D0 =
PREPARED — NOT AUTHORIZED

selfApproved             = false
authorApproved           = false
implementationAuthorized = false
passClaimed              = false
PHASE_KIND               = NORMATIVE DESIGN — ADR, VERSIONED SPECIFICATION AND
                           EXPORT-FOUNDATION AMENDMENT; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-R6-plus-P0  = PASS — AUTHOR APPROVED — PUBLISHED
                           PRE-G9B-R6-plus-B   = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (AUTHOR_SMOKE = PASS; operational
                                                 order only, not a dependency)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-D1   (hard dependency on the
                                                 author-accepted D0 output)
```

`authorApproved = false` means that no candidate of this phase has been
author-approved. Technical verification never creates author approval. Once
authorized, the phase stops with one exact technically verified documentary
candidate pending author review. Its ADR and specification become normative
only when the author accepts them explicitly.

<!-- geocedg-field: objective -->
## Objective

Turn the author-fixed unit semantics and the author decisions `AQ-U1` to
`AQ-U6` into the durable, versioned, normative unit contract that `D1`, `C`,
`E2` and `E3` implement against:

- an ADR on unit semantics and on the persistence ownership of the unit state;
- a versioned normative specification of the GeoCeDG unit system: the unit
  set and exact factors, the document-scoped user-defined unit `usm`, the
  effective-unit rules, the unit-independence invariant, the persistence
  element and its lexical grammar, undo, copy and paste, the unspecified
  state, `drawingScale`, the physical-export relation and the
  forward-compatibility limitation;
- the normative amendment of the
  [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md)
  for the future DXF unit contract;
- invariant, persistence and compatibility matrices;
- `AQ-U1` to `AQ-U6` traceability;
- an explicit consumer contract for `D1`, `C`, `E2` and `E3`;
- a candidate report and its machine-readable mirror.

```text
constructionUnit := UNSPECIFIED_MODEL_UNIT | mm | cm | m | usm
presentationUnit := mm | cm | m | usm            (absent -> constructionUnit)
usm              := one document-scoped definition  1 usm = k m,  k finite, k > 0
drawingScale     := SESSION ratio, default 1:1

metreFactor(mm) = 10^-3    metreFactor(cm) = 10^-2    metreFactor(m) = 1
metreFactor(usm) = k

displayValue(L) = L · metreFactor(constructionUnit) / metreFactor(presentationUnit)
                  (both physical; L in model units)
printedLength(L, 1:n) = L · metreFactor(constructionUnit) / n   metres on paper,
                  independent of presentationUnit          (AQ-U1)
```

`D0` writes documents only. It adds no Java, no product behavior, no
serialization, no GUI, no status bar, no exporter change, no native dimension
and no `IsoABorder`.

```text
CHANGE_ROUTE         = ORDINARY
VERIFICATION_CLASS   = DOCUMENTATION_STATUS_ONLY   (author-frozen 2026-10-02)
frozenAtPhaseStart   = true
PRODUCT_PHASE_EFFECT = NONE
PLANNED_ACCEPTANCE   = STATIC on the exact frozen documentary candidate;
                       no PHASE, no INTEGRATION, no FINAL
```

Section 12.8 of `geocedg/specs/operations/verification-levels.md` defines
`DOCUMENTATION_STATUS_ONLY` as "Static validation only; no FULL". The class is
correct only while `D0` touches documentation, specification, ADR, validation
and status paths. If the work would need a change to `source/**`, `apps/**`,
`packaging/**`, a product resource, `geocedg/specs/operations/verification-*`,
`prompt-contracts.json`, any `tools/agent/**` script or any verifier evidence
contract, stop and report a `VERIFICATION_ESCALATION_REQUEST` instead of
widening scope.

### Author decisions this prompt implements

The author decisions of 2026-10-02, recorded in the
[D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md),
which is their authority, together with the author-fixed semantics of the
[mini-track plan](../../../docs/architecture/pre_g9b_r6_plus_minitrack_plan.md)
§4:

| ID | Decision (substance) | Effect in `D0` |
|---|---|---|
| plan §4.1–§4.4 | `constructionUnit` = physical meaning of one model unit; a change is reinterpretation, never scaling, coordinate transformation or redefine; legacy without metadata = `UNSPECIFIED_MODEL_UNIT`, `cm` never inferred; `presentationUnit` expresses dimensional presentation; `drawingScale` dimensionless | normative text |
| `AQ-U1` | printed and exported physical size invariant under an equivalent `presentationUnit` change | normative text |
| `AQ-U2` | only `E2` native dimensions present physical units in v1; `Distance`, `Length`, `LocusLength` and other numeric results stay bare model values; no general `GeoNumeric` becomes dimensional; areas, volumes and unit powers out of v1; angles keep `angleUnit` | normative text |
| `AQ-U3` | units `mm`, `cm`, `m`, `usm` and the state `UNSPECIFIED_MODEL_UNIT`; exact factors; one document-scoped `usm` with explicit `k > 0`, finite; name and symbol are presentation metadata; a factor change while active is an undoable reinterpretation; invalid attempts rejected; tokens exactly `mm`, `cm`, `m`, `usm`; unspecified = absence; factor serialized in the same flat versioned element, locale-independent and round-trip reproducible; never in `geocedgSpatial`; older builds lenient, loss on re-save documented | normative text; element and attribute names decided by `D0` |
| `AQ-U4` | paste keeps numbers, never converts or scales; differing effective metre factors → non-blocking notice; unspecified on either side → no notice, no inference; the notice is outside geometry and outside the paste undo | normative text |
| `AQ-U5` | `drawingScale` `SESSION`, default `1:1`, not serialized, not undoable, reset on New and Open | normative text |
| `AQ-U6` | unspecified → no engineering-scale physical export, unit declaration required first, no `1 unit = 1 cm` fallback, bare values; a valid `usm` enables physical export and `IsoABorder`; an undefined `usm` cannot be selected | normative text |
| undo | `constructionUnit`, `presentationUnit` and an active `usm` factor change are undoable document operations; `drawingScale` and the new-document preference are not | normative text |
| invariant | no live dependency of coordinates, authoritative numeric geometry, DAG topology, dependencies, identities, construction order, Locus V2, Spline V2, parameter domains or numeric geometric outputs on units or scale; live consumers only dimension presentation strings, status/presentation UI and export adapters; creation-time capture only | normative text |
| persistence target | flat, versioned, self-closing GeoCeDG element under `<construction>`; lazy; absence = unspecified; reset in `clearConstruction`; paste and macros never apply it; no preferences XML; coherent undo; legacy byte identity | normative contract for `D1` |
| export foundation | `$INSUNITS` from `constructionUnit` in `C`/`D1`; `0` while unspecified; no coordinate conversion; screen scale, `printingScale` and DPI never the model unit | normative amendment, not implementation |

No other open decision of the
[P0 candidate report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15 is resolved by this prompt or by `D0`. In particular `AQ-E3a`, `AQ-E3c` and
`AQ-E4` stay with `E3` and `E2`; `D0` states only the unit contract they rely
on.

### Re-characterization of the inputs against the base

The preparation re-read, at the base below, every seam this prompt turns into
a normative contract. Paths use the `P0` abbreviations (`common/`,
`geocedg-common/`, `desktop/`, `geocedg-desktop/`). Citation drift caused by
`A-1` and `B` is corrected; substantive differences from the `P0` inputs are
marked **contradiction** or **new**. The phase re-establishes each one before
relying on it.

| Seam | At the base | Relation to the `P0` inputs |
|---|---|---|
| unit vocabulary | no `constructionUnit`, `presentationUnit`, `drawingScale`, `geocedgUnits` or `UNSPECIFIED_MODEL_UNIT` anywhere under `source/` or `geocedg/specs/` | confirmed: no unit exists yet |
| construction writer | `getConstructionXML` opens `<construction>` with only `title`, `author`, `date`, then `worksheetText`, then `<geocedgSpatial>` when the registry is non-empty, then the elements (`common/kernel/Construction.java:1517-1540`) | confirmed |
| construction reader | `handleConstruction` reads only `title`, `author`, `date` and guards the app update with `!(kernel instanceof MacroKernel)` (`common/io/MyXMLHandler.java:2573-2595`); an unknown child logs `unknown tag in <construction>` and stays in construction mode (`:3088-3094`) | confirmed; lines unchanged |
| `clearConstruction` | resets the spatial state by load purpose, elements, layers, step, title/author/date, worksheet text, macros and groups; no kernel or unit state (`Construction.java:4094-4143`) | confirmed |
| undo restore | Desktop: `UndoManagerD.loadUndoInfo` → `MyXMLioJre.readZipFromMemory` → `doParseXML(…, true, …)` → `kernel.clearConstruction(false)` (`common/io/MyXMLio.java:415-417`); common: `Construction.processXML` → `processXMLString(strXML, true, …)` (`Construction.java:4230-4241`); the snapshot is `MyXMLio.getUndoXML` (`MyXMLio.java:139-168`) | confirmed; an undo restore clears and re-reads the snapshot |
| redefine rebuild and rollback restore | `buildConstruction` re-parses `getCurrentUndoXML(false)` with a clearing load (`Construction.java:2099`, `:4177-4184`); rollback restore likewise (`:2673-2692`) | **new**: the unit element must be part of the undo XML, or every redefine and rollback would reset the unit |
| File → New | `AppD.fileNew` calls `clearConstruction()` (`desktop/main/AppD.java:1154`, defined `:2931-2934`), then re-applies the preferences XML (`:1174`) | confirmed; lines moved; the new-document default (`DQ-D0-1`) applies after this point |
| `<kernel>` and preferences | `<kernel>` is not reset per load (`common/kernel/Kernel.java:3061-3082`) and is written into the preferences XML (`MyXMLio.java:345-358`; `common/main/App.java:1880-1906`, kernel XML `:1903`) | confirmed; `App` lines moved by `B`; still a rejected owner |
| clipboard | carries element XML, the spatial closure, groups, images and embeds, and **no** document or unit metadata (`common/util/InternalClipboard.java:137-175`); a paste is wrapped in a bare `<construction>` (`:626-632`); one `CopyPasteD` per `AppD` (`AppD.java:355`, `:5037-5043`; `desktop/util/CopyPasteD.java:57-70`, `:607-616`) | confirmed; lines moved; see `DQ-D0-8` |
| macros | `Macro.getXML` emits `getConstructionXML` for each macro (`common/kernel/Macro.java:175`, `:792-796`; `buildMacroXML` `:477-481`) | confirmed |
| `<geocedgSpatial>` fail-closed precedent | `Unknown geocedgSpatial record` rejected (`geocedg-common/kernel/spatial/identity/SpatialRecordXmlCodec.java:255`; `MyXMLio.java:435-449`) | confirmed; not the unit owner (`AQ-U3`) |
| `printingScale` | field `common/euclidian/EuclidianView.java:311`; zoom-derived `calcPrintingScale` `:1933-1936`; accessors `:3054-3064`; never serialized | confirmed; lines moved |
| picture physical size after `B` | the GeoCeDG route sizes from the `ExportArea` pixel size times `printingScale` factors: PDF points per pixel `printingScale·72/(2.54·xscale)` (`geocedg-desktop/export/PictureExportService.java:180-185`), a zoom-derived fallback for a zero scale (`geocedg-desktop/export/ExportViewport.java:120-127`), command-line PNG (`PictureExportCommandLine.java:52-58`), dialog `Scale in cm` (`desktop/export/PrintScalePanel.java:118`, `:140-145`, `:285-289`; `desktop/export/GraphicExportDialog.java:535-600`, `:576-578`) | **new**: `B` added consumers of the zoom-derived mapping; it is the historical behavior from which `cm` must never be inferred (`DQ-D0-11`) |
| LaTeX units | `xunit = yunit = 1` (cm) by default (`common/export/pstricks/GeoGebraExport.java:241-242`), written as `cm` (`GeoGebraToPstricks.java:1656-1659`; `GeoGebraToPgf.java:149-153`), user-editable in the frame (`GeoGebraToPstricks.java:108-109`; `GeoGebraToPgf.java:113-114`) | confirmed; see `DQ-D0-11` |
| DXF unit | `enum Unit { UNITLESS }` is the only value (`geocedg-common/export/GeometryExportModel.java:27-31`, `:564-565`); `$INSUNITS` written as `0` (`geocedg-common/export/DxfExporter.java:109-110`) | confirmed |
| DXF staleness fingerprint | spatial section plus element XML only (`geocedg-common/export/G9X1GeometryExportAdapter.java:1049-1060`) | confirmed; must include the unit state in `C`/`D1` |
| export foundation | `Status: Experimental`, ADR 0005 `Accepted for G5 experimental implementation`; `UNITLESS` (`geometry-export-foundation.md:39`, `:72-75`), the future contract clause (`:77-78`), the G5 PASS clause `$INSUNITS = 0` (`:118`) | **contradiction** of wording: the `P0` inputs call it "accepted"; it is an experimental contract under an accepted ADR |
| extended DXF fidelity | `NORMATIVE / AUTHOR APPROVED`; unitless coordinates and `$INSUNITS=0` (`geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:48`), unitless dialog (`:180`), sidecar units (`:192`); the G9X1 verifier evidence check requires `fidelityContract.units == "UNITLESS"` (`tools/agent/verify-g9x1-extended-dxf.ps1:449`) | **new**: not in the `P0` inputs; the amendment must name this contract and the verifier pin as `C`-owned consequences; `D0` changes neither |
| axis unit label | free text per view (`common/main/settings/EuclidianSettings.java:1197`), options `""`, °, π, `mm`, `cm`, `m`, `km`, `$` (`common/gui/dialog/options/model/AxisModel.java:113-121`), tick suffix (`common/euclidian/DrawAxis.java:572-573`) | confirmed; see `DQ-D0-13` |
| model-unit notions | `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT` (`geocedg-common/kernel/locus/metric/MetricUnit2D.java:9-10`); residual unit `"model-coordinate"` (`geocedg-common/kernel/locus/intersection/LocusIntersectionPolicy2D.java:79`, `:129`) | confirmed; stay model-unit |
| spatial frame `units` token | free text, required on version-2 frame records (`geocedg-common/kernel/spatial/identity/ProjectionFrameRecord.java:81`, `:138-140`), written and parsed by `SpatialRecordXmlCodec` (`:354-361`, `:90`), compared by string equality (`SpatialIdentityRegistry.java:1925`, also `:1130`, `:1943`, `:2021`, `:2622`) | confirmed; opaque and unrelated to `constructionUnit` in v1 |
| `angleUnit` precedent | output formatting `common/kernel/Kernel.java:2345`, XML `:4558`; input parsing `common/kernel/commands/AlgebraProcessor.java:2630-2636`; rounding `common/kernel/arithmetic/MyDouble.java:596-666` | confirmed; only the output half is a precedent |
| numeric results | `Distance` is a bare model value (`common/kernel/algos/AlgoDistancePoints.java:95-98`), formatted without suffix (`common/kernel/geos/GeoNumeric.java:662-711`) | confirmed (`AQ-U2`) |
| excluded formats | STL `EDGE_FOR_PRINT = 40` and `STLscale` (`common/geogebra3D/euclidian3D/EuclidianView3DForExport.java:50`, `:55`, `:222-252`); Collada `centimeter` (`common/geogebra3D/euclidian3D/printer3D/FormatCollada.java:76`) | confirmed; outside the surface (`AQ-X3`) |
| preference key precedent | `geocedg.presentation.<name>.v1` (`geocedg-desktop/GeoCeDGPresentationPreferences.java:23-29`) | confirmed |
| native document | `.cedg` and `.ggb` share the ZIP/XML format (`geocedg/specs/ui/native-document-identity.md:14-17`, `:77-85`); GeoCeDG saves only `.cedg` (`geocedg-desktop/AppGeoCeDG.java:795-801`; `GeoCeDGDocumentPolicy.java:19-36`) | confirmed; `AppGeoCeDG` lines moved by `B` |
| ADR numbering | highest is `docs/adr/0031-…` | the next free number is `0032` |

A contradiction that would change an author decision, the persistence owner,
the verification class or the scope stops the phase.

### Contradictions found against the `P0` inputs

None of these changes an author decision, the persistence owner or the
verification class. Each is carried into `D0` as stated.

1. **Export-foundation status.** The `P0` inputs §6 call the foundation
   "accepted". At the base it is `Status: Experimental` under ADR 0005. The
   amendment is written against an experimental contract and must say so.
2. **A second DXF unit contract.** The inputs name only the foundation. The
   author-approved G9X1 fidelity contract and the G9X1 verifier evidence check
   also pin `UNITLESS`. The amendment cross-references them; changing them
   is `C`'s work, and the verifier pin is verification infrastructure that
   `C` must reconcile through an authorized route.
3. **Reset routes.** The inputs justify "reset in `clearConstruction`, not on
   parse" by paste and macros only. Undo restore, redefine rebuild and
   rollback restore also clear and re-parse the current undo XML. The rule
   holds only if the element is emitted in `getCurrentUndoXML`.
4. **Paste transport.** `AQ-U4` compares the source and target metre factors
   at paste time, but the clipboard carries no document metadata. The source
   unit needs a transport the inputs do not describe (`DQ-D0-8`).
5. **New-document preference.** The inputs define a `USER_PREFERENCE` default
   for `presentationUnit` only. A new document starts
   `UNSPECIFIED_MODEL_UNIT`, so under `AQ-U6` that preference alone has no
   effect; the author's undo rule names "the user preference" without naming
   the unit (`DQ-D0-1`).
6. **Physical-export wording.** Plan §4.4 says "physical model-to-output unit
   = presentationUnit" and the inputs §1 that `presentationUnit` expresses
   "dimensional and export quantities". By `AQ-U1` and the inputs §7, printed
   size depends on `constructionUnit` and `drawingScale` only;
   `presentationUnit` only expresses quantities. `D0` writes the relation in
   that form and does not edit the plan.
7. **Malformed metadata.** The inputs offer only lenient or fail-closed for
   **older** builds. The author fixed lenient for them, and also that an
   absent or invalid factor never turns `usm` into `UNSPECIFIED_MODEL_UNIT`.
   For a malformed element read by a build that implements v1, the lenient
   rule would do exactly that (`DQ-D0-7`).
8. **`usm` consequences.** The inputs recommended `mm`, `cm`, `m`. The author
   added `usm`, which the inputs do not cover: DXF `$INSUNITS` has a closed
   code table (`DQ-D0-10`); the factor needs a grammar and a numeric meaning
   (`DQ-D0-6`); the lifetime of an unreferenced definition (`DQ-D0-4`) and the
   storage of its name and symbol (`DQ-D0-5`) are open; `E3`'s captured
   model-units-per-millimetre factor becomes `10^-3 / k`.
9. **Dimension text as a kernel value.** The inputs §4a allow only
   presentation strings to read units. The `E2` candidate makes the dimension
   text a kernel `GeoText` output (`docs/architecture/pre_g9b_r6_plus_e2_native_dimensions_design_candidate.md:90`),
   whose string other objects and scripts can consume (`DQ-D0-9`).
10. **More zoom-derived consumers.** After `B`, every GeoCeDG picture route
    sizes physical output from `printingScale`. The inputs §7 describe one
    replacement for `C`; the base has more consumers to replace, and the
    unspecified case needs an explicit non-physical mode (`DQ-D0-11`).

### Decisions requested before authorization

The author decisions do not settle the details below. For each, this prompt
fixes a **default contract**. The authorizing instruction may confirm or
replace it; without an explicit replacement the default applies. None is an
author decision until the author makes it.

| ID | Question | Default contract of this prompt |
|---|---|---|
| `DQ-D0-1` | Which units does the new-document `USER_PREFERENCE` cover, and when does it apply? | one optional preference for both the construction and the presentation unit of **new** documents, standard tokens only (`usm` is document-scoped and is never a preference default); unset by default, so a new document is `UNSPECIFIED_MODEL_UNIT` as today; applied only at File → New and to the initial empty document, after the preferences XML is re-applied; never on Open, undo, redo, redefine, paste or insert; applying it creates no undo point and does not mark the document modified; a physical default so applied is a declaration and is written on save; the presentation default applies only together with a physical construction default |
| `DQ-D0-2` | Can a `presentationUnit` exist while `constructionUnit` is unspecified? | no: presentation cannot be selected while the construction unit is unspecified; the unit element exists only when `constructionUnit` is physical and never carries a presentation unit, or a `usm` definition, without a construction unit |
| `DQ-D0-3` | Can the user return a document to `UNSPECIFIED_MODEL_UNIT`? | yes, as one explicit undoable document operation ("clear unit declaration") that removes the whole element, including the `usm` definition; the document then re-saves as if no unit had ever been declared; it is never a side effect of another operation |
| `DQ-D0-4` | What happens to a valid `usm` definition when neither unit references `usm`? | it stays document state and is written in the element while the element exists, so undo snapshots stay coherent and no definition is lost silently on save; it ends only with `DQ-D0-3`, New or Open; changing the factor while `usm` is unreferenced is an undoable document operation without any reinterpretation |
| `DQ-D0-5` | Where are the `usm` name and symbol stored, and what suffix is shown? | optional attributes of the same element (names fixed by `D0`), XML-escaped Unicode with a bounded length; the dimension suffix is the symbol, else the literal `usm`; a rename or symbol change is an undoable presentation operation; neither ever takes part in compatibility, paste or export comparisons |
| `DQ-D0-6` | Numeric meaning and lexical form of the factor `k`. | the semantic value is the IEEE-754 double parsed from the serialized string; the writer emits the shortest decimal that round-trips that double, with `.` as the separator and an optional `E` exponent, independent of locale; the reader accepts exactly that grammar; metre-factor comparisons (paste, export) are exact equality on the parsed doubles, with no tolerance |
| `DQ-D0-7` | A build that implements v1 reads a newer `version` or a malformed v1 element (unknown token, `usm` referenced without a valid factor, a non-finite or non-positive factor, presentation without construction). | a newer `version`: ignored like an older build would, with a visible notice that the unit metadata was not understood; the document loads `UNSPECIFIED_MODEL_UNIT`; a malformed v1 element: the document is **not** opened and an explicit error names the defect, because the build that owns the format may neither infer a value nor drop declared physical semantics silently |
| `DQ-D0-8` | How does paste learn the source unit, and which routes show the notice? | at copy time the source's effective construction unit and metre factor (or `UNSPECIFIED_MODEL_UNIT`) are captured as in-memory metadata beside the clipboard buffer, never in the clipboard XML; every Desktop route that pastes that buffer compares it with the target and shows the notice; a paste without such metadata (external text, script or API XML insertion, a buffer from an earlier build) counts as source unspecified: no notice, no inference |
| `DQ-D0-9` | The dimension text is a kernel string that other objects and scripts can consume. | the output-only `angleUnit` precedent applies: the unit-dependent presentation string is a permitted live consumer; numeric outputs never depend on units; the specification states that an object or script consuming a dimension's text string observes presentation changes and lies outside the numeric-geometric invariant, and `E2` must not route that string into a geometric input |
| `DQ-D0-10` | DXF `$INSUNITS` for `usm`, whose factor has no general DXF code. | `$INSUNITS = 0` for `usm`, even when `k` equals a coded unit, with an explicit export notice that states the declared factor; `mm`, `cm` and `m` map to their DXF codes, which `D0` cites from the DXF reference; coordinates are never converted |
| `DQ-D0-11` | Export of documents whose unit is unspecified, given that every picture route and the LaTeX exporters size output from a zoom-derived or default centimetre mapping today. | picture, print and LaTeX export stay available in a **non-physical mode**: picture formats keep the device mapping they have after `B`, and LaTeX keeps its explicit, user-visible `xunit`/`yunit` device parameter; neither is offered as an engineering scale or presented as a model unit, and no `1:n` scale is shown; only engineering-scale export and `IsoABorder` require a declared unit; `C` implements |
| `DQ-D0-12` | Domain and notation of `drawingScale`. | `a:b` with `a` and `b` positive integers (`1:n` reduction, `n:1` enlargement), default `1:1`, held per application window; consistent with `E3`'s integer pair (`AQ-E3a` stays `E3`'s decision) |
| `DQ-D0-13` | Relation between the axis `unitLabel` and the unit system. | none in v1: the axis label stays an independent per-view presentation label that the unit system never reads, writes or synchronizes |

### Contract details fixed by this prompt

Where the author decisions and the requested decisions above leave a detail
open, the following are contracts of the prepared prompt, not author
decisions; the authorizing instruction may change them.

- **Document set and locations.** One ADR, `docs/adr/<next free number>-…`
  (`0032` is the next free number at the base; `D0` takes the next free number
  at its own base), with status `PROPOSED` until the author accepts it. One
  specification, proposed at `geocedg/specs/units/unit-system.md` with an
  explicit header (`Status: NORMATIVE CANDIDATE — NOT AUTHOR APPROVED`,
  `Version: 1.0`, owner phase, implementing phases), so that acceptance later
  changes only the status line. The matrices live in the specification; the
  candidate report under `docs/validation/` and its JSON mirror under
  `geocedg/validation/pre-g9b-r6-plus/` reference them and do not restate
  them.
- **Single source of truth.** The specification owns the unit semantics. The
  ADR records the decision, its alternatives and its consequences and points
  to the specification for the rules. The export amendment, the candidate
  report, the roadmap and the plan reference the specification and never
  restate its rules.
- **Names and grammar.** The specification fixes the element name, every
  attribute name, the `version` value, the token grammar (case-sensitive, the
  exact tokens `mm`, `cm`, `m`, `usm`), the factor grammar (`DQ-D0-6`), the
  attribute order and the writer's placement relative to `<geocedgSpatial>`
  in `Construction.getConstructionXML`, so that `D1` writes byte-deterministic
  output. The conceptual name `geocedgUnits` is the default.
- **Routes that parse a `<construction>`.** The specification enumerates
  every route that parses or emits construction XML — full document load,
  undo and redo restore, redefine rebuild, rollback restore, File → New,
  paste, macro construction, user-tool (`.ggt`) load, the preferences XML, and
  every scripting or API route that inserts or replaces XML — and states for
  each whether the unit element is written, read, ignored or reset. Only the
  clearing loads of a full document or of the current undo XML (load, undo,
  redo, redefine rebuild, rollback restore) apply it, after
  `clearConstruction` has reset the state; the element is therefore part of
  `getCurrentUndoXML`.
- **Effective-unit function.** One pure function of the stored state defines
  the effective construction unit, the effective presentation unit and their
  metre factors; every consumer (dimension text, status UI, paste notice,
  export adapters, creation-time tool capture) uses it. No consumer derives a
  unit on its own.
- **Unit-independence invariant.** Stated as a normative MUST with the full
  list of the decision record, the permitted live consumers, the creation-time
  capture rule and its consequence for `E2` (`AQ-E4`) and `E3` (`AQ-E3c`),
  and a list of the existing model-unit notions that stay model-unit:
  `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT`, the intersection residual unit
  `"model-coordinate"`, the export model's coordinate unit, and the spatial
  frame `units` token, which stays an opaque identity token unrelated to
  `constructionUnit` in v1.
- **Physical export relation.** The specification states the printed-length
  formula above, shows its invariance under `presentationUnit` (`AQ-U1`), and
  states that `presentationUnit` is never a second scale factor. It corrects
  the ambiguous plan §4.4 wording "physical model-to-output unit =
  presentationUnit" by reference to `AQ-U1`, without editing the author-fixed
  plan text.
- **Export amendment form.** The amendment is a delimited, versioned section
  of `geometry-export-foundation.md` that states the future rule, its
  effective condition (implementation by `C`/`D1` and author acceptance) and
  its owners. The in-force G5 statements (`UNITLESS`, `$INSUNITS = 0`, the G5
  PASS clause) stay unchanged and are annotated as the rule that holds until
  then, so the experimental contract never describes behavior the exporter
  does not have. The G9X1 fidelity contract receives only a cross-reference
  note.
- **Excluded formats.** STL, Collada and the AR unit keep their own output
  units, are outside the export surface (`AQ-X3`) and are not governed by the
  unit system in v1.
- **Undo.** Each undoable unit operation stores exactly one undo point,
  restores through the ordinary construction-XML snapshot, and leaves object
  identity and every geometric value unchanged on undo and redo; the
  specification states which session state (`drawingScale`, `ExportArea`)
  survives an undo restore unchanged.
- **Forward compatibility.** The specification states the lenient behavior of
  older GeoCeDG builds and Classic as observed at the base (unknown tag logged,
  rest loaded, element lost on re-save), the resulting silent semantic loss,
  and the rule for a newer `version` and a malformed element in a build that
  implements v1 (`DQ-D0-7`).
- **Consumer contract.** One section per consumer: `D1` (persistence, effective
  units, undo, paste notice, new-document default, status segments,
  byte-identity proof obligations), `C` (engineering scale, LaTeX units, DXF
  header, the non-physical mode, the G9X1 fidelity contract), `E2` (text
  presentation, suffix, bare values, creation-time capture), `E3` (`u` capture
  from the effective metre factor, unspecified-unit gate). Each lists the
  normative clauses it must implement and the evidence its own phase must
  produce. `D0` sets no verification class for them.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
0ef616bb7bb8efd3e4f19762f38936ff4742d1c3          (P_R6PLUS_B, published B closeout)

IMPLEMENTATION_BASE_TREE =
be7ef7acbc3503002f3dac2bc33f047795f7a644

B_STATE =
PASS — AUTHOR APPROVED — PUBLISHED                 (AUTHOR_SMOKE = PASS)

B_APPROVED_TECHNICAL_CANDIDATE =
848206c413eb4f78694c9149e153f6e867eb9525          (T_R6PLUS_B)
```

A moving branch is not a base. Entry gate: local `main`, `origin/main` and the
live remote `main` equal the base the authorizing instruction names, its tree
matches, and the worktree is clean. If the documentary commit that publishes
this prompt and the author-decision record is published first, the authorizing
instruction names that commit instead; this prompt assumes no later base on its
own. The phase works on a new local branch from the named commit and is not
rebased onto any later commit without a new author instruction. Between the
base and that documentary commit only documentation changes, so the citations
above still apply; the phase re-establishes every one it relies on before using
it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at the base.
3. The author-fixed unit semantics of the mini-track plan §4 and the
   [D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md).
4. The accepted specifications the amendment touches: the
   [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md)
   and ADR 0005, and the
   [extended DXF fidelity contract](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md).
5. Design input, re-characterized before use: the
   [D0 unit-system inputs](../../../docs/architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md),
   the [P0 report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
   findings 9–13 and its persistence matrix, the `C`, `E2` and `E3` design
   candidates (unit clauses only) and the
   [B closeout record](../../../docs/validation/pre_g9b_r6_plus_b_closeout_record.md)
   (observations with a later owner).
6. Generated artifacts, earlier reports and previous agent output are
   evidence, not authority.

<!-- geocedg-field: allowed_scope -->
## Allowed scope

`D0` may perform only:

- focused reading of current source, tests, specifications and the pinned
  upstream source, to re-establish every seam it relies on;
- scratch probes executed from the session scratchpad, never written into
  tracked source paths or `artifacts/`, to settle a characterization question
  (for example the undo-restore and paste parse paths). A probe result is
  characterization evidence, not acceptance evidence;
- the new ADR under `docs/adr/` (status `PROPOSED`);
- the new versioned specification under `geocedg/specs/units/` (or another
  `geocedg/specs/` location the phase justifies), with candidate status;
- the normative amendment of
  `geocedg/specs/export/geometry-export-foundation.md` (`DQ-D0-10`, *Export
  amendment form*), and a cross-reference note in
  `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md` that names
  the amendment and its future owner, without changing that contract's
  in-force rules;
- a candidate report under `docs/validation/` and its machine-readable mirror
  under `geocedg/validation/pre-g9b-r6-plus/` (plain JSON with
  `schemaVersion`, no new schema, no static-contract registration);
- roadmap and mini-track plan status updates limited to `D0`'s state and to
  corrections the evidence forces outside the author-fixed parts;
- the amendment of this prompt to the authorized state, when the authorizing
  instruction permits it.

```text
PRODUCT_PHASE_EFFECT = NONE
```

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

`D0` must not perform:

- any change under `source/**`, `apps/**`, `packaging/**` or product
  resources: no Java, no product behavior, no localized string;
- any serialization or document-format implementation, including a sample
  `.cedg` or `.ggb` with unit metadata committed as a model or fixture;
- any GUI, status-bar, preference-store or menu implementation;
- any exporter change, including `$INSUNITS`, LaTeX units, picture scale, the
  DXF staleness fingerprint or the G9X1 sidecar;
- native dimensions (`E2`), `IsoABorder` or the ISO producers (`E3`);
- any change to the meaning of the in-force G5 and G9X1 export rules before
  `C`/`D1` implement the amendment;
- edits of the author-fixed mini-track plan §4 or of the frozen `P0`
  artifacts;
- creating or editing any prompt file other than this canonical `D0` prompt,
  including the prompts of `D1`, `C`, `E2` and `E3`;
- publication of any kind.

It must also not: edit `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md` or
`ai-shell/prompts/**`; edit `geocedg/specs/operations/verification-*`,
`prompt-contracts.json` or `tools/agent/**`; register a verification phase or a
schema; mark any gate, phase, specification or ADR approved, `PASS` or
authorized; reopen an author-fixed semantic or an author decision; or start
`D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`, `PRE-G9B-R7` or `G9B`.

## Architectural placement

`D0` itself is normative design. The placement it specifies follows
`AGENTS.md` §4:

- `constructionUnit` and the `usm` definition: shared document semantics,
  serialized in the shared `<construction>` XML, because they change the
  physical meaning of the document and must be consumed coherently by every
  frontend; they are not geometric truth and no algorithm reads them live.
- `presentationUnit`: shared document presentation, in the same element.
- The effective-unit function: shared, read-only, beside the document state.
- Dimension presentation strings: produced by `E2`'s kernel objects through
  the effective-unit function, with the output-only half of the `angleUnit`
  precedent and none of its parsing or rounding effects.
- `drawingScale`: Desktop/application session export state, never document
  state (`AQ-U5`).
- The new-document default: a GeoCeDG `USER_PREFERENCE` on the
  `geocedg.<area>.<name>.v1` key precedent (`DQ-D0-1`).
- Export adapters (`C`): read-only consumers outside the kernel
  (`AGENTS.md` §13).

If an owner proves wrong at the base, stop and report instead of choosing
another layer.

## Required design/specification

The minimum `D0` output set is the author-fixed list of the decision record:

1. the ADR on unit semantics and persistence ownership, with the rejected
   owners of the `P0` inputs §3 (`<kernel>`, a `<construction>` attribute,
   `<euclidianView>`, inside `<geocedgSpatial>`) and the rejected
   alternatives for each requested decision;
2. the versioned normative specification;
3. the amendment of the geometry export foundation;
4. the invariant matrix (every protected property × every unit operation:
   set, change and clear `constructionUnit`, change `presentationUnit`, define
   and change `usm`, rename `usm`, change `drawingScale`, undo, redo, paste,
   load, New), the persistence matrix (every unit state × its class, owner,
   writer, reader, reset point, undo and preference behavior) and the
   compatibility matrix (legacy `.cedg`, Classic `.ggb`, older GeoCeDG,
   Classic reading a new document, a newer `version`, a malformed element,
   paste across documents and windows, macros and user tools);
5. `AQ-U1` to `AQ-U6` traceability: each decision → the specification clause
   that carries it → the consumer phase that implements it → the evidence that
   phase must produce;
6. the consumer contract for `D1`, `C`, `E2` and `E3`;
7. the candidate report and its machine-readable mirror.

## Geometric invariants and degeneracies

`D0` changes no geometry. The specification states the unit-independence
invariant normatively and the degeneracies below:

| Case | Required normative behavior |
|---|---|
| `constructionUnit` changed between physical units | reinterpretation; every coordinate, value, dependency and identity unchanged; dimension texts and export interpretation update |
| `usm` factor changed while active | the same reinterpretation; undoable |
| attempted `usm` factor non-finite, zero, negative or unparsable | rejected; the previous valid definition stays |
| `usm` selected without a valid definition | not selectable; no fallback |
| `presentationUnit` absent | effective `presentationUnit = constructionUnit` |
| `constructionUnit = UNSPECIFIED_MODEL_UNIT` | bare values, no suffix; no engineering-scale export; `IsoABorder` and physical operations require a declaration first |
| a conversion whose result is not finite | a defined presentation failure state, never an exception, a silent infinity or a changed model value |
| paste between differing effective metre factors | numbers unchanged; non-blocking notice |
| paste with either side unspecified | numbers unchanged; no notice; no inference |
| undo or redo of a unit operation | unit and presentation restored coherently; identity and geometry unchanged |
| a tool that captured a unit-derived input at creation | the input stays an ordinary parameter after any later unit change |

## Compatibility and serialization

`D0` changes no serialization. Its specification must state, for every unit
state, whether it serializes, where, how a legacy document without it loads
(`UNSPECIFIED_MODEL_UNIT`, nothing written until the user declares a unit),
how a Classic `.ggb` loads, how an older GeoCeDG build and Classic treat a new
document (lenient, element lost on re-save), how a newer `version` and a
malformed element are treated (`DQ-D0-7`), and that no migration is needed.
Legacy documents round-trip byte-identically while no unit is declared. It
never infers `cm`.

<!-- geocedg-field: required_checks -->
## Required tests and commands

`D0` adds no executable test. Its acceptance, for the frozen
`DOCUMENTATION_STATUS_ONLY` class:

1. focused reading and scratch probes;
2. before freezing: JSON validity of every new machine-readable file;
   resolution of every repository-relative Markdown link the phase adds;
   validation of this amended prompt (below); `git diff --check`; proof that
   no forbidden or product path changed (`git diff --name-only` against
   `IMPLEMENTATION_BASE` lists only allowed paths);
3. one clean immutable candidate commit whose parent is
   `IMPLEMENTATION_BASE`, staging only authorized `D0` paths;
4. on that exact candidate:

   ```text
   tools/agent/verify.ps1 -Profile STATIC -LogDirectory <fresh root; may be outside the repository>
   ```

   with the verifier console redirected outside `artifacts/`; the required
   result is `ACCEPTED / COMPLETE` for the applicable `STATIC` plan. The two
   standing `STATIC` diagnostics are reported, not waived;
5. `git diff --check`.

This prompt is changed only under the explicit authorization of the
authorizing instruction. Validate it after amendment and again before
freezing with `Test-PromptContractDocument` from
`tools/agent/prompt-contract-parser.psm1` and the `task` profile of
`geocedg/specs/operations/prompt-contracts.json`; `execution_safe` must be
`true`.

No `PHASE`, `INTEGRATION` or `FINAL` run is part of `D0`. Report the profile,
exact command, exit code, report path, acceptance verdict, coverage verdict,
diagnostic count and execution identity of every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

Nothing in this file is authorized. The author decisions it records do not
authorize execution. Execution requires a new explicit author instruction
naming `PRE-G9B-R6-plus-D0` and its exact base. That instruction would
authorize only the documentary scope above: the amendment of this prompt, the
`D0` documents, local commits, one frozen documentary candidate and its
`STATIC` verification.

`D0` authorizes nothing that follows it. The operational order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`; `D1` additionally
needs the author-accepted `D0` output. `D1`, `A-2`, `C`, `E1`, `E2`, `E3`,
`F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized. A specification or
ADR produced by `D0` does not become normative until the author accepts it
explicitly. Author approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

Once authorized: a local branch from the base and local commits only. Push,
branch publication, merge, promotion to `main`, rebase, squash, amend after
freeze, force push, tag, release and binary publication are forbidden; each
needs a separate explicit author instruction naming the exact candidate SHA.
Acceptance evidence never grants publication authority.

## Acceptance and closeout

`D0` stops with one documentary candidate pending author review. Author
approval is an explicit decision naming the exact accepted commit; it may
accept the ADR and the specification, amend them, or return them. The
candidate report and its mirror record only invariant facts:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

A later closeout record is the sole authority for approval and for the
status change of the ADR and the specification. The candidate is never
amended after freeze; if a correction is needed afterwards, stop and report
rather than create an unrequested descendant.

## Required artifacts

- The ADR, the specification and the export-foundation amendment above, with
  the G9X1 cross-reference note.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-characterized seams and any correction to the `P0` inputs; the
  enumeration of construction-XML routes; the matrices (by reference to the
  specification); the `AQ-U1`–`AQ-U6` traceability; the disposition of every
  `DQ-D0` item as received from the authorizing instruction; the consumer
  contract summary; residual risks; the questions that need an author
  decision before `D1` can be authorized.
- Machine-readable evidence beside it, following the existing conventions.
- `PRODUCT_PHASE_EFFECT = NONE`, `VERIFICATION_INFRASTRUCTURE_IMPACT`,
  `BOOTSTRAP IMPACT` and `GUIDE_IMPACT` declarations, and the exact `STATIC`
  command, exit code and log path.
- Technical verification distinguished from author approval.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- an author-fixed semantic or an author decision cannot be carried without
  contradiction, or two of them contradict each other at the base;
- the persistence target meets a demonstrated compatibility blocker at the
  base (for example an undo-restore or paste route that cannot preserve the
  reset-in-`clearConstruction` rule);
- a normative rule would need a product, serialization, exporter, verifier or
  evidence-contract change to be stated truthfully;
- a design would make a geometric output, number, dependency, identity or
  parameter domain depend live on a unit or on `drawingScale`;
- a consumer contract for `C`, `E2` or `E3` would need an author decision
  owned by that phase (`AQ-E3a`, `AQ-E3c`, `AQ-E4`);
- the work needs a verification level above `DOCUMENTATION_STATUS_ONLY`;
- source evidence contradicts an author decision in a way that requires an
  author choice.

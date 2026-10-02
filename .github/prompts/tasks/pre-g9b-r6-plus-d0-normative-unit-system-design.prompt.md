# PRE-G9B-R6-plus-D0 — normative unit-system design

**CANONICAL EXECUTION PROMPT — AUTHORIZED FOR NORMATIVE/DOCUMENTARY EXECUTION
ONLY.**

The author's explicit instruction of 2026-10-02 approves the documentary
preparation package of `PRE-G9B-R6-plus-D0` (`T_R6PLUS_D0_PROMPT`), names it as
the exact execution base, gives the author dispositions on `DQ-D0-1` to
`DQ-D0-13`, and authorizes the execution of `D0`. The instruction is recorded,
versioned, in the
[D0 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md).
It also requires this amendment, as the first tracked edit of the phase, so
that the prompt becomes the executable contract of the phase. The amendment
replaces the prepared prompt of `T_R6PLUS_D0_PROMPT` (blob
`beb4d6d97dd6c0e5d27b11d3876fa8647223e38b`): it records the authorized base,
integrates the `DQ-D0` dispositions, the existing-specification boundary, the
Picture-export boundary and the living-plan supersession, and removes the
G9X1 cross-reference note the prepared prompt allowed. Where the prepared
prompt and the record differed, the record prevailed; this amendment carries
that precedence into the text. Every other scope, forbidden-scope and stop
rule of the prepared prompt is kept. The unit decisions are recorded, versioned,
in the
[D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md).
This file is an execution contract, not a second policy document: the unit
semantics live in the mini-track plan §4 and the two records now, and in the
ADR and specification `D0` writes once they are author-accepted. The design
input is the `P0`
[D0 unit-system inputs](../../../docs/architecture/pre_g9b_r6_plus_d0_unit_system_inputs.md),
as amended by the author decisions and re-characterized below against the
base.

```text
PRE-G9B-R6-plus-D0 =
AUTHORIZED FOR EXECUTION

selfApproved             = false
authorApproved           = false
implementationAuthorized = true    (normative/documentary execution only)
passClaimed              = false
PHASE_KIND               = NORMATIVE DESIGN — ADR, VERSIONED SPECIFICATION AND
                           FUTURE EXPORT AMENDMENT CONTRACT; NO PRODUCT CHANGE
DEPENDS_ON               = PRE-G9B-R6-plus-P0  = PASS — AUTHOR APPROVED — PUBLISHED
                           PRE-G9B-R6-plus-B   = PASS — AUTHOR APPROVED — PUBLISHED
                                                 (AUTHOR_SMOKE = PASS; operational
                                                 order only, not a dependency)
DEPENDS_ON_PACKAGE       = D0 PREPARATION PACKAGE = PASS — AUTHOR APPROVED
                                                 (not published)
NEXT_SUBPHASE            = PRE-G9B-R6-plus-D1   (hard dependency on the
                                                 author-accepted D0 output)
```

`implementationAuthorized = true` authorizes only the normative and
documentary execution defined below: no product code, Java, implemented
serialization, GUI, status bar, copy/paste, exporter, verifier change,
native dimension, `IsoABorder`, layer work or later subphase.
`authorApproved = false` means that no candidate of this phase has been
author-approved. Technical verification never creates author approval. The
phase stops with one exact technically verified documentary candidate pending
author review:
`PRE-G9B-R6-plus-D0 = DOCUMENTARY CANDIDATE PENDING AUTHOR REVIEW`, with
`selfApproved = false`, `authorApproved = false` and `passClaimed = false`. Its
ADR and specification become normative only when the author accepts them
explicitly.

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
- the proposed future amendment contract for geometry export, recorded
  without promoting the experimental
  [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md)
  and without changing the author-approved
  [extended DXF fidelity contract](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md);
- the persistence and XML grammar contract for `D1`, including the exact
  `usm` grammar and lifecycle;
- the invariant matrix and the persistence, undo and compatibility matrix;
- `AQ-U1` to `AQ-U6` traceability and the `DQ-D0-1` to `DQ-D0-13`
  dispositions;
- an explicit consumer contract for `D1`, `C`, `E2` and `E3`;
- the correction of the living-plan wording on physical output;
- a candidate report and its machine-readable mirror.

```text
constructionUnit := UNSPECIFIED_MODEL_UNIT | mm | cm | m | usm
presentationUnit := mm | cm | m | usm            (absent -> constructionUnit)
usm              := one document-scoped definition  1 usm = k m,
                    k a finite binary64, k > 0
drawingScale     := SESSION pair a:b of positive integers, gcd-normalized,
                    physical output / physical model = a / b, default 1:1

metreFactor(mm) = 10^-3    metreFactor(cm) = 10^-2    metreFactor(m) = 1
metreFactor(usm) = k

displayValue(L) = L · metreFactor(constructionUnit) / metreFactor(presentationUnit)
                  (both physical; L in model units)
printedLength(L, a:b) = L · metreFactor(constructionUnit) · a / b   metres on paper,
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
| export foundation | `$INSUNITS` from `constructionUnit` in `C`/`D1`; `0` while unspecified; no coordinate conversion; screen scale, `printingScale` and DPI never the model unit | future amendment contract, not implementation |
| `DQ-D0-1`–`DQ-D0-13` | the author dispositions below | normative text |
| existing specifications | the foundation stays `Experimental` and is not promoted; the G9X1 fidelity contract stays unitless and unchanged; `C` amends both, the verification pins, the exporter and the sidecar metadata together | recorded boundary and obligation for `C` |
| Picture export | after `B` it still sizes through `printingScale`; `D0` does not alter it; `printingScale`, zoom, DPI and viewport are not a model-unit authority | recorded boundary; `C` replaces it |
| living plan | `physical model-to-output unit = presentationUnit` is superseded: physical output derives from `constructionUnit + drawingScale` | specification clause and plan supersession note |

No other open decision of the
[P0 candidate report](../../../docs/validation/pre_g9b_r6_plus_p0_characterization_design_candidate_report.md)
§15 is resolved by this prompt or by `D0`. In particular `AQ-E3a`, `AQ-E3c` and
`AQ-E4` stay with `E3` and `E2`; `D0` states only the unit contract they rely
on.

### Re-characterization of the inputs against the base

The preparation re-read, at `P_R6PLUS_B` `0ef616bb7`, every seam this prompt
turns into a normative contract. The authorized base differs from it only by
documentation, so the citations below still apply. Paths use the `P0`
abbreviations (`common/`, `geocedg-common/`, `desktop/`, `geocedg-desktop/`).
Citation drift caused by `A-1` and `B` is corrected; substantive differences
from the `P0` inputs are marked **contradiction** or **new**. The phase
re-establishes each one before relying on it.

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
| extended DXF fidelity | `NORMATIVE / AUTHOR APPROVED`; unitless coordinates and `$INSUNITS=0` (`geocedg/specs/export/dxf-curve-fidelity-and-approximation.md:48`), unitless dialog (`:180`), sidecar units (`:192`); the G9X1 verifier evidence check requires `fidelityContract.units == "UNITLESS"` (`tools/agent/verify-g9x1-extended-dxf.ps1:449`) | **new**: not in the `P0` inputs; `D0` records this contract and the verifier pin as `C`-owned obligations and edits neither (`DQ-D0-10`, existing-specification boundary) |
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
   also pin `UNITLESS`. By author disposition `D0` does not edit them: the
   specification records the obligation, and changing them is `C`'s work,
   together with the verifier pin, which is verification infrastructure that
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
   that form and, by author instruction, adds an explicit supersession note
   to plan §4.4 without deleting the author-fixed text.
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

### Author dispositions on `DQ-D0-1` to `DQ-D0-13`

The prepared prompt fixed a default contract for each requested decision. The
author disposed of all of them on 2026-10-02; the
[D0 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md)
is their authority and prevails over the prepared defaults. In substance:

| ID | Outcome | Disposition (substance) |
|---|---|---|
| `DQ-D0-1` | accepted with precision | preferences may default both units, applied only when a **new blank document** is created; construction preference in {`UNSPECIFIED_MODEL_UNIT`, `mm`, `cm`, `m`}, never `usm`; never applied to an opened document; with an unspecified effective construction unit the presentation preference stays inert |
| `DQ-D0-2` | accepted with precision | no effective document `presentationUnit` while `constructionUnit = UNSPECIFIED_MODEL_UNIT`; a preference may exist but is inert; no presentation unit is serialized as if it gave an unspecified model physical meaning |
| `DQ-D0-3` | **replaced** | explicit `constructionUnit = UNSPECIFIED_MODEL_UNIT` is an undoable document operation that removes the physical interpretation and any dependent presentation interpretation; a valid unused `usm` definition may stay stored; the element is omitted only when no unit metadata remains; legacy documents that never acquired metadata stay byte-identical with no element |
| `DQ-D0-4` | accepted | a valid `usm` definition persists while its element exists, even when unselected |
| `DQ-D0-5` | accepted | optional name and symbol are presentation metadata; identity = token `usm` + declared `metersPerUnit`; symbol fallback `usm` |
| `DQ-D0-6` | accepted with an exact numerical contract | `k` a finite binary64 `> 0`; locale-independent deterministic shortest round-trip decimal; invalid, zero, negative, NaN or infinite values rejected, previous definition kept; `usm` equality = binary64 equality; built-ins exact SI; an arbitrary `k` never called symbolically exact |
| `DQ-D0-7` | **replaced** | a build that recognizes `<geocedgUnits>` **fails closed** with an explicit user-visible load error on a newer version, a malformed v1 element, a selected `usm` without a valid factor, or ambiguous physical semantics; older readers stay lenient, recorded as a forward-compatibility limitation; never moved into `geocedgSpatial` |
| `DQ-D0-8` | accepted with precision | source unit context is transient clipboard provenance beside the internal copy buffer, never geometry, construction XML, clipboard element content or document state; paste never depends on it; notice only with reliable provenance and different physical meanings |
| `DQ-D0-9` | accepted with precision | a native dimension keeps a stable model-unit number separate from its `GeoText` presentation; the text may change with either unit and explicit consumers may observe it; presentation dependency, not geometric-unit dependency; no parsing of the string back into geometry or numbers |
| `DQ-D0-10` | **replaced** | `mm`, `cm`, `m` may map to native `$INSUNITS`; `usm`: unchanged coordinates, `$INSUNITS = 0`, custom-unit warning, **required** paired GeoCeDG sidecar recording at least `usm` and the canonical `metersPerUnit`; `D0` records the future contract only and changes no G9X1 product, verifier or fidelity specification; `C` amends them coherently |
| `DQ-D0-11` | accepted with precision | unspecified forbids any physical engineering-scale claim; a clearly non-physical legacy/device-scale picture/vector mode may stay where existing functionality requires it, never labelled or read as `1:n`; `IsoABorder` and every model-to-physical conversion unavailable; no `1 model unit = 1 cm` fallback |
| `DQ-D0-12` | accepted with precision | positive integer pair `a:b`, `physical output / physical model = a / b`, gcd-normalized, default `1:1`, `SESSION`, per application/document window export context, reset on New/Open, not serialized, not undoable |
| `DQ-D0-13` | accepted | the axis `unitLabel` stays an independent view presentation facility; no unit, dimensional or export authority; no synchronization in `D0` or `D1` |

### Existing-specification and Picture-export boundaries

- `geometry-export-foundation.md` is `Status: Experimental` under ADR 0005,
  accepted for the G5 experimental implementation. `D0` does not promote it.
- `dxf-curve-fidelity-and-approximation.md` is `NORMATIVE / AUTHOR APPROVED`
  and fixes the DXF baseline as unitless. `D0` does not change it while the
  product and the G9X1 verifier enforce `UNITLESS`.
- `D0` defines the future unit semantics and an explicit obligation for `C` to
  amend, together and consistently: the applicable DXF normative
  specification, the geometry export foundation where appropriate, the G9X1
  and future verification pins, exporter behavior, and fidelity and sidecar
  unit metadata. Until `C`, the implemented DXF baseline stays unitless.
- After `B`, Picture export still derives its existing physical sizing
  through `printingScale`. `D0` does not alter it, and establishes that
  `printingScale`, zoom, DPI and the viewport are not a model-unit authority.
  `C` replaces the physical engineering-scale interpretation with the
  `D0`/`D1` contract.

### Contract details fixed by this prompt

Where the author decisions and dispositions leave a detail open, the
following are contracts of this prompt, not author decisions.

- **Document set and locations.** One ADR, `docs/adr/0032-…` (the next free
  number at the base), with status `PROPOSED` until the author accepts it. One
  specification at `geocedg/specs/units/unit-system.md` with an explicit
  header (`Status: NORMATIVE CANDIDATE — NOT AUTHOR APPROVED`,
  `Version: 1.0`, owner phase, implementing phases), so that acceptance later
  changes only the status line. The matrices, the traceability and the
  consumer contracts live in the specification; the candidate report under
  `docs/validation/` and its JSON mirror under
  `geocedg/validation/pre-g9b-r6-plus/` reference them and do not restate
  their rules.
- **Single source of truth.** The specification owns the unit semantics. The
  ADR records the decision, its alternatives and its consequences and points
  to the specification for the rules. The export pointer, the candidate
  report, the roadmap and the plan reference the specification and never
  restate its rules.
- **Names and grammar.** The specification fixes the element name, every
  attribute name, the `version` value, the token grammar (case-sensitive, the
  exact tokens `mm`, `cm`, `m`, `usm`), the factor grammar (`DQ-D0-6`), the
  name and symbol grammar (`DQ-D0-5`), the attribute order, the omission rule
  (`DQ-D0-3`) and the writer's placement relative to `<worksheetText>`,
  `<geocedgSpatial>` and the elements in `Construction.getConstructionXML`, so
  that `D1` writes byte-deterministic output. The element name is
  `geocedgUnits`.
- **Routes that parse a `<construction>`.** The specification enumerates
  every route that parses or emits construction XML — full document load,
  undo and redo restore, redefine rebuild, rollback restore (including the
  paste rollback and the rejected-spatial-parse rollback), File → New, paste,
  scripting and API XML insertion and replacement, macro construction,
  user-tool (`.ggt`) load and the preferences XML — and states for each
  whether the unit element is written, read, ignored or reset. Only the
  clearing loads of a full document or of the current undo XML apply it, after
  `clearConstruction` has reset the state; the element is therefore part of
  `getCurrentUndoXML`.
- **Effective-unit function.** One pure function of the stored state defines
  the effective construction unit, the effective presentation unit and their
  metre factors; every consumer (dimension text, status UI, paste notice,
  export adapters, creation-time tool capture) uses it. No consumer derives a
  unit on its own.
- **Unit-independence invariant.** Stated as a normative MUST with the full
  list of the decision record, the permitted live consumers, the creation-time
  capture rule and its consequence for `E2` (`AQ-E4`) and `E3` (`AQ-E3c`), the
  `DQ-D0-9` precision on presentation strings, and the existing model-unit
  notions that stay model-unit: `MetricUnit2D.CONSTRUCTION_LENGTH_UNIT`, the
  intersection residual unit `"model-coordinate"`, the export model's
  coordinate unit, and the spatial frame `units` token, which stays an opaque
  identity token unrelated to `constructionUnit` in v1.
- **Physical export relation.** The specification states the printed-length
  formula above, shows its invariance under `presentationUnit` (`AQ-U1`), and
  states that `presentationUnit` is never a second scale factor. It
  supersedes the plan §4.4 wording "physical model-to-output unit =
  presentationUnit"; the plan receives an explicit supersession note after
  §4.4 that keeps the author-fixed text and points to the specification.
- **Export amendment form.** The future amendment contract lives in the
  specification, with the replacement rules for the foundation and the G9X1
  fidelity contract, their effective condition (implementation by `C`/`D1`
  and author acceptance) and their owner. The foundation receives only a
  delimited pointer note that states it is not in force, keeps every G5
  statement (`UNITLESS`, `$INSUNITS = 0`, the G5 PASS clause) unchanged, and
  does not change its `Experimental` status. The G9X1 fidelity contract is
  not edited.
- **Excluded formats.** STL, Collada and the AR unit keep their own output
  units, are outside the export surface (`AQ-X3`) and are not governed by the
  unit system in v1.
- **Undo.** Each undoable unit operation stores exactly one undo point,
  restores through the ordinary construction-XML snapshot, and leaves object
  identity and every geometric value unchanged on undo and redo; the
  specification states which session state (`drawingScale`, `ExportArea`,
  clipboard provenance) survives an undo restore unchanged.
- **Forward compatibility.** The specification states the lenient behavior of
  older GeoCeDG builds and Classic as observed at the base (unknown tag logged,
  rest loaded, element lost on re-save) as an explicit limitation, and the
  fail-closed rule of `DQ-D0-7` for builds that recognize the family.
- **Consumer contract.** One section per consumer: `D1` (persistence, effective
  units, undo, fail-closed load, paste provenance and notice, new-document
  defaults, status segments, byte-identity proof obligations), `C`
  (engineering scale, LaTeX units, DXF header and sidecar, the non-physical
  mode, the G9X1 and foundation amendments, the verification pins), `E2`
  (number and text separation, suffix, bare values, creation-time capture),
  `E3` (`u` capture from the effective metre factor, `drawingScale` pair,
  unspecified-unit gate). Each lists the normative clauses it must implement
  and the evidence its own phase must produce. `D0` sets no verification class
  for them.

<!-- geocedg-field: implementation_base -->
## Implementation base

```text
IMPLEMENTATION_BASE =
8814468101e3caef45d9f6cbad4bf563523c3478          (T_R6PLUS_D0_PROMPT, approved D0
                                                   preparation package; not published)

IMPLEMENTATION_BASE_TREE =
edbe623604330e62db11ff3d3b05b7d73671dbd0

IMPLEMENTATION_BASE_PARENT =
0ef616bb7bb8efd3e4f19762f38936ff4742d1c3          (P_R6PLUS_B, published B closeout;
                                                   tree be7ef7acbc3503002f3dac2bc33f047795f7a644)

D0_PREPARATION_PACKAGE =
PASS — AUTHOR APPROVED                             (2026-10-02; not published)

B_STATE =
PASS — AUTHOR APPROVED — PUBLISHED                 (AUTHOR_SMOKE = PASS)
```

A moving branch is not a base. The authorizing instruction names the
unpublished approved package `T_R6PLUS_D0_PROMPT` as the exact execution base,
so the entry gate is: `HEAD` of the local branch
`phase/pre-g9b-r6-plus-d0-prompt` equals `IMPLEMENTATION_BASE`, its tree
matches, its parent is `P_R6PLUS_B`, the worktree is clean, and local `main`,
`origin/main` and the live remote `main` still equal `P_R6PLUS_B`. The phase
continues on that branch with new commits: first the authorization edit
(this amendment and the closeout and authorization record), then the frozen
documentary candidate, whose parent is the authorization edit. The approved
package is never amended, rebased or squashed. Between `P_R6PLUS_B` and
`IMPLEMENTATION_BASE` only five documentation files differ, so the citations
above still apply; the phase re-establishes every one it relies on before using
it.

## Authority and evidence hierarchy

1. `AGENTS.md`, the canonical governance and verification prompts, and the
   verification contract (`verification-levels.md`, the typed registry and its
   schemas, `tools/agent/verify.ps1`).
2. Current source and tests at the base.
3. The author-fixed unit semantics of the mini-track plan §4, the
   [D0 author-decision record](../../../docs/validation/pre_g9b_r6_plus_d0_author_decisions_record.md)
   and the
   [D0 preparation closeout and authorization record](../../../docs/validation/pre_g9b_r6_plus_d0_prompt_closeout_record.md),
   which prevails over the prepared defaults.
4. The existing export specifications and their status, which `D0` records
   and does not change in force: the
   [geometry export foundation](../../../geocedg/specs/export/geometry-export-foundation.md)
   (`Experimental`) and ADR 0005, and the
   [extended DXF fidelity contract](../../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
   (`NORMATIVE / AUTHOR APPROVED`).
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
  tracked source paths or `artifacts/`, to settle a characterization question.
  A probe result is characterization evidence, not acceptance evidence;
- the new ADR `docs/adr/0032-…` (status `PROPOSED`);
- the new versioned specification `geocedg/specs/units/unit-system.md`, with
  candidate status, carrying the future export amendment contract;
- a delimited, not-in-force pointer note in
  `geocedg/specs/export/geometry-export-foundation.md` that keeps every G5
  statement and the `Experimental` status unchanged (*Export amendment form*);
- an explicit supersession note after the mini-track plan §4.4 that keeps the
  author-fixed text and points to the specification;
- a candidate report under `docs/validation/` and its machine-readable mirror
  under `geocedg/validation/pre-g9b-r6-plus/` (plain JSON with
  `schemaVersion`, no new schema, no static-contract registration);
- roadmap and mini-track plan status updates limited to `D0`'s state and to
  corrections the evidence forces outside the author-fixed parts;
- this amendment of the prompt and the closeout and authorization record with
  its mirror, as the first tracked edit.

```text
PRODUCT_PHASE_EFFECT = NONE
```

<!-- geocedg-field: forbidden_scope -->
## Explicitly forbidden scope

`D0` must not perform:

- any change under `source/**`, `apps/**`, `packaging/**` or product
  resources: no Java, no product behavior, no localized string;
- any serialization or document-format implementation, including an XML
  reader or writer, and a sample `.cedg` or `.ggb` with unit metadata committed
  as a model or fixture;
- any GUI, status-bar, preference-store, copy/paste or menu implementation;
- any exporter change, including `$INSUNITS`, LaTeX units, picture scale, the
  DXF staleness fingerprint or the G9X1 sidecar;
- any edit of `geocedg/specs/export/dxf-curve-fidelity-and-approximation.md`,
  any G9X1 verifier or evidence change, and any verifier change that
  implements `C` (`DQ-D0-10`);
- promoting `geometry-export-foundation.md` from `Experimental`, or changing
  the meaning of any in-force G5 or G9X1 export rule before `C`/`D1` implement
  the amendment;
- native dimensions (`E2`), `IsoABorder` or the ISO producers (`E3`), and
  layer work;
- edits of the author-fixed mini-track plan §4 other than the supersession
  note, and edits of the frozen `P0` artifacts;
- amending the approved preparation candidate `T_R6PLUS_D0_PROMPT`;
- creating or editing any prompt file other than this canonical `D0` prompt,
  including the prompts of `D1`, `C`, `E2` and `E3`;
- publication of any kind.

It must also not: edit `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md` or
`ai-shell/prompts/**`; edit `geocedg/specs/operations/verification-*`,
`prompt-contracts.json` or `tools/agent/**`; register a verification phase or a
schema; mark any gate, phase, specification or ADR approved, `PASS` or
authorized; reopen an author-fixed semantic, an author decision or an author
disposition; or start `D1`, `A-2`, `C`, `E1`, `E2`, `E3`, `F1`, `F2`, `G`,
`PRE-G9B-R7` or `G9B`.

## Architectural placement

`D0` itself is normative design. The placement it specifies follows
`AGENTS.md` §4:

- `constructionUnit` and the `usm` definition: shared document semantics,
  serialized in the shared `<construction>` XML, because they change the
  physical meaning of the document and must be consumed coherently by every
  frontend; they are not geometric truth and no algorithm reads them live.
- `presentationUnit` and the `usm` name and symbol: shared document
  presentation, in the same element.
- The effective-unit function: shared, read-only, beside the document state.
- Dimension presentation strings: produced by `E2`'s kernel objects through
  the effective-unit function, with the output-only half of the `angleUnit`
  precedent and none of its parsing or rounding effects (`DQ-D0-9`).
- `drawingScale`: Desktop/application session export state, never document
  state (`AQ-U5`, `DQ-D0-12`).
- The new-document defaults: GeoCeDG `USER_PREFERENCE` entries on the
  `geocedg.<area>.<name>.v1` key precedent (`DQ-D0-1`).
- Clipboard provenance: transient Desktop state beside the internal copy
  buffer (`DQ-D0-8`).
- Export adapters (`C`): read-only consumers outside the kernel
  (`AGENTS.md` §13).

If an owner proves wrong at the base, stop and report instead of choosing
another layer.

## Required design/specification

The minimum `D0` output set is the author-fixed list of the closeout and
authorization record:

1. the unit-system ADR, with the rejected owners of the `P0` inputs §3
   (`<kernel>`, a `<construction>` attribute, `<euclidianView>`, inside
   `<geocedgSpatial>`) and the rejected alternatives of each disposition;
2. the normative unit-system specification;
3. the proposed future amendment contract for geometry export;
4. the persistence and XML grammar contract for `D1`;
5. the exact `usm` grammar and lifecycle;
6. the invariant matrix (every protected property × every unit operation:
   set, change and clear `constructionUnit`, change `presentationUnit`, define,
   change, rename and remove `usm`, change `drawingScale`, undo, redo, paste,
   load, New, redefine);
7. the persistence, undo and compatibility matrix (every unit state × its
   class, owner, writer, reader, reset point, undo and preference behavior;
   legacy `.cedg`, Classic `.ggb`, older GeoCeDG, Classic reading a new
   document, a newer `version`, a malformed element, paste across documents
   and windows, macros and user tools);
8. `AQ-U1` to `AQ-U6` traceability: each decision → the specification clause
   that carries it → the consumer phase that implements it → the evidence that
   phase must produce;
9. the `DQ-D0-1` to `DQ-D0-13` dispositions → specification clauses;
10. the consumer contracts for `D1`, `C`, `E2` and `E3`;
11. the candidate report;
12. its machine-readable evidence mirror.

## Geometric invariants and degeneracies

`D0` changes no geometry. The specification states the unit-independence
invariant normatively and the degeneracies below:

| Case | Required normative behavior |
|---|---|
| `constructionUnit` changed between physical units | reinterpretation; every coordinate, value, dependency and identity unchanged; dimension texts and export interpretation update |
| `usm` factor changed while active | the same reinterpretation; undoable |
| attempted `usm` factor non-finite, zero, negative or unparsable | rejected; the previous valid definition stays |
| `usm` selected without a valid definition | not selectable; no fallback |
| `presentationUnit` absent with a physical construction unit | effective `presentationUnit = constructionUnit` |
| `constructionUnit = UNSPECIFIED_MODEL_UNIT` | no effective presentation unit; bare values, no suffix; no engineering-scale export; `IsoABorder` and physical operations require a declaration first |
| a conversion whose result is not finite | a defined presentation failure state, never an exception, a silent infinity or a changed model value |
| paste between differing effective metre factors with reliable provenance | numbers unchanged; non-blocking notice |
| paste with either side unspecified, or without provenance | numbers unchanged; no notice; no inference |
| undo or redo of a unit operation | unit and presentation restored coherently; identity and geometry unchanged |
| a tool that captured a unit-derived input at creation | the input stays an ordinary parameter after any later unit change |

## Compatibility and serialization

`D0` changes no serialization. Its specification must state, for every unit
state, whether it serializes, where, how a legacy document without it loads
(`UNSPECIFIED_MODEL_UNIT`, nothing written until unit metadata is acquired),
how a Classic `.ggb` loads, how an older GeoCeDG build and Classic treat a new
document (lenient, element lost on re-save, an explicit limitation), how a
build that recognizes the family treats a newer `version` and a malformed
element (fail closed, `DQ-D0-7`), and that no migration is needed. Legacy
documents that never acquired unit metadata round-trip byte-identically. It
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
3. one clean immutable documentary candidate commit whose parent is the
   authorization edit, whose parent is `IMPLEMENTATION_BASE`, staging only
   authorized `D0` paths;
4. on that exact candidate:

   ```text
   tools/agent/verify.ps1 -Profile STATIC -LogDirectory <fresh root; may be outside the repository>
   ```

   with the verifier console redirected outside `artifacts/`; the required
   result is `ACCEPTED / COMPLETE` for the applicable `STATIC` plan. The two
   standing `STATIC` diagnostics are reported, not waived;
5. `git diff --check`.

Validate this amended prompt, after amendment and again before freezing, with
`Test-PromptContractDocument` from `tools/agent/prompt-contract-parser.psm1`
and the `task` profile of `geocedg/specs/operations/prompt-contracts.json`;
`execution_safe` must be `true`.

No `PHASE`, `INTEGRATION` or `FINAL` run is part of `D0`. Report the profile,
exact command, exit code, report path, acceptance verdict, coverage verdict,
diagnostic count and execution identity of every run.

<!-- geocedg-field: authorization_boundary -->
## Authorization boundary

The author's explicit instruction of 2026-10-02 names `PRE-G9B-R6-plus-D0` and
its exact base above. It authorizes only the normative and documentary scope of
this file: this amendment and the closeout and authorization record, the `D0`
documents, local commits, one frozen documentary candidate and its `STATIC`
verification.

`D0` authorizes nothing that follows it. The operational order is
`A-1 → B → D0 → D1 → A-2 → C → E1 → E2 → E3 → F1 → F2 → G`; `D1` additionally
needs the author-accepted `D0` output. `D1`, `A-2`, `C`, `E1`, `E2`, `E3`,
`F1`, `F2`, `G`, `PRE-G9B-R7` and `G9B` stay unauthorized. A specification or
ADR produced by `D0` does not become normative until the author accepts it
explicitly. Author approval is never created by technical verification.

<!-- geocedg-field: publication_boundary -->
## Publication boundary

A local branch and local commits only. Push, branch publication, merge,
promotion to `main`, rebase, squash, amend after freeze, force push, tag,
release and binary publication are forbidden; each needs a separate explicit
author instruction naming the exact candidate SHA. Acceptance evidence never
grants publication authority.

## Acceptance and closeout

`D0` stops with one documentary candidate pending author review:

```text
PRE-G9B-R6-plus-D0 = DOCUMENTARY CANDIDATE PENDING AUTHOR REVIEW
selfApproved       = false
authorApproved     = false
passClaimed        = false
```

Author approval is an explicit decision naming the exact accepted commit; it
may accept the ADR and the specification, amend them, or return them. The
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

- The ADR, the specification with its future export amendment contract, and
  the foundation pointer note and plan supersession note above.
- A candidate report under `docs/validation/` with: entry-gate evidence; the
  re-characterized seams and any correction to the `P0` inputs; the
  enumeration of construction-XML routes; the matrices, traceability and
  consumer contracts (by reference to the specification); the disposition of
  every `DQ-D0` item and the specification clause that carries it; the
  inherited contracts and supersessions; residual risks; the questions that
  need an author decision before `D1` can be authorized.
- Machine-readable evidence beside it, following the existing conventions.
- `PRODUCT_PHASE_EFFECT = NONE`, `VERIFICATION_INFRASTRUCTURE_IMPACT`,
  `BOOTSTRAP IMPACT` and `GUIDE_IMPACT` declarations, and the exact `STATIC`
  command, exit code and log path.
- Technical verification distinguished from author approval.

## Stop conditions

Stop and report rather than improvise when:

- the entry gate fails, or the base differs from the authorizing instruction;
- an author-fixed semantic, an author decision or an author disposition cannot
  be carried without contradiction, or two of them contradict each other at
  the base;
- the persistence target meets a demonstrated compatibility blocker at the
  base (for example an undo-restore, rollback or paste route that cannot
  preserve the reset-in-`clearConstruction` rule);
- a normative rule would need a product, serialization, exporter, verifier,
  evidence-contract or G9X1-specification change to be stated truthfully;
- a design would make a geometric output, an authoritative number, a
  dependency, an identity or a parameter domain depend live on a unit or on
  `drawingScale`;
- a consumer contract for `C`, `E2` or `E3` would need an author decision
  owned by that phase (`AQ-E3a`, `AQ-E3c`, `AQ-E4`);
- the work needs a verification level above `DOCUMENTATION_STATUS_ONLY`;
- source evidence contradicts an author decision in a way that requires an
  author choice.

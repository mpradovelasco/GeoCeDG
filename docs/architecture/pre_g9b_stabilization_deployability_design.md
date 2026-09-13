# PRE-G9B stabilization, deployability and public-promotion design

- Status: **PRE-G9B ROADMAP EXTENSION — DESIGN CANDIDATE — PENDING AUTHOR REVIEW**
- Recorded: 2026-09-13
- Published base commit: `5869f9b3b8acb6950d3317c9927693716fecf6f2`
- Published base tree: `733ddaa2675e826cdfffdd24de1a1ca135784c19`
- Product implementation: **NOT AUTHORIZED**
- Release/license disposition: **NOT AUTHORIZED**
- Feature-default or version promotion: **NOT AUTHORIZED**
- G9B/G9C/G9U2 and productive G10: **NOT AUTHORIZED**
- Self approval: **false**

This note designs the author-requested roadmap extension between the closed
post-G9U1 refinement track and any later return to G9B. It records
characterization from current repository authority; it does not classify
unreceived author artifacts as reproduced failures and does not authorize an
implementation, distribution, license grant, feature-default change or public
version promotion.

## 1. Canonical phase structure

The canonical top-level identifiers are:

| ID | Purpose | Current status |
|---|---|---|
| `PRE-G9B-S1` | DXF correctness and bounded product stabilization | `DESIGNED — PRODUCT IMPLEMENTATION NOT YET AUTHORIZED` |
| `PRE-G9B-D1` | Licensing, assets and deployability | `DESIGNED — RESEARCH/IMPLEMENTATION NOT YET AUTHORIZED` |
| `PRE-G9B-P1` | Public surface and product-version 1.0 promotion | `DESIGNED — PROMOTION NOT YET AUTHORIZED` |

The `S`, `D` and `P` families distinguish stabilization, deployability and
promotion without occupying a G9 semantic identifier such as G9U2. Bounded S1
implementation work may be authorized separately as `PRE-G9B-S1-DXF`,
`PRE-G9B-S1-POLISH`, `PRE-G9B-S1-TEXT-DIALOG`,
`PRE-G9B-S1-PRESENTATION-PREFERENCES` and `PRE-G9B-S1-TEXT-ZOOM`. A conditional
`PRE-G9B-S1-SPLINE-DXF` exists only if artifact-backed characterization proves
that SplineV2 needs a new export contract rather than a repair of the existing
Locus V2 adapter. These are proposed bounded selectors/slices, not registered
verification phases or implementation authorizations.

```text
POST-G9U1 REFINEMENT TRACK — COMPLETE — AUTHOR APPROVED
    |
    | AUTHOR_SELECTED_EXECUTION_PREDECESSOR
    v
PRE-G9B-S1  stabilization
    |
    | AUTHOR_SELECTED_EXECUTION_PREDECESSOR
    v
PRE-G9B-D1  licensing/assets/deployability
    |
    | GLOBAL_PRE-PROMOTION_GATE internal to this track
    v
PRE-G9B-P1  public surface / version 1.0 promotion
    |
    | later explicit author decision; no authorization here
    v
G9B

HARD_DEPENDENCY retained: G9A3 + approved primitive-projection contract -> G9B
GLOBAL_CLOSEOUT_GATE retained: G9B -> G9C -> global G9 closeout -> G9U2
```

This calendar order is author-selected. S1, D1 and P1 do not become semantic
dependencies of G9B merely by preceding it. G9B remains designed but
unauthorized, and its existing hard product dependency remains G9A3 plus the
approved primitive-projection contract. The post-G9U1 track stays closed.

## 2. Characterized repository authority

### 2.1 DXF boundary

The approved G5 path remains:

```text
GeoCeDG Desktop export controller
    -> GeometryExportService
    -> GeoElementGeometryExportAdapter
    -> immutable GeometryExportModel
    -> DxfExporter
```

[`GeoElementGeometryExportAdapter`](../../source/shared/common/src/main/java/org/geocedg/common/export/GeoElementGeometryExportAdapter.java)
has an exact ellipse/elliptic-arc mapping and
[`DxfExporter`](../../source/shared/common/src/main/java/org/geocedg/common/export/DxfExporter.java)
emits an AC1015 `ELLIPSE`. Therefore an artifact-backed
successful-but-empty ellipse export is presumptively a regression against G5,
not a reason to redesign the ellipse contract.

The approved [G9X1 contract](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md)
and [ADR 0014](../adr/0014-export-only-dxf-approximation-and-sidecar.md) add
`G9X1GeometryExportAdapter`, typed preflight,
component outcomes and the conditional fidelity sidecar. Its Locus V2 adapter
reads the current semantic revision, branches, components and explicit domains;
it must not consume render vertices, the viewport, zoom or DPI. The Desktop
controller currently chooses this path only when the separate extended-DXF
feature is enabled; the ordinary path remains G5. This feature-routing boundary
must be part of reproduction rather than treated as proof of a geometric defect.

SplineV2 is produced as a semantic `GeoLocusV2` with explicit domain/spans. The
approved G9X1 matrix promises one export-only `LWPOLYLINE` per valid Locus V2
branch/component, but does not promise a native exact DXF `SPLINE` entity.
Consequently:

- failure of an admissible SplineV2 to traverse the existing semantic Locus V2
  adapter is a bounded G9X1 regression;
- a producer-specific adapter is justified only if the common semantic source
  cannot supply the already approved component/domain evidence; and
- any new exact `SPLINE` fidelity claim or new approximation semantics require
  `PRE-G9B-S1-SPLINE-DXF` and separate author review.

### 2.2 Product/view characterization

Current code establishes these boundaries:

- `GeoLocusV2.translatedTypeString()` presents every semantic curve as
  `LocusV2`, even when its parent is `AlgoSplineV2`; command translations
  already distinguish SplineV2.
- the embedded text editor is integrated with `TextInputDialogD` and the
  Properties lifecycle; Apply/OK/Cancel semantics interact with redefine,
  draft ownership, selection changes and Undo.
- `Util.MENU_FONT_SIZES` starts at 12 pt; shared application and GUI font
  settings are only partially separated, and Desktop GUI font changes also
  influence maximum icon size.
- `DrawText` anchors a construction Text object in world coordinates but chooses
  its glyph size from text/application font settings rather than the Euclidian
  world scale. Making glyph size scale with zoom changes an inherited
  presentation contract, including bounds, hit testing and LaTeX rendering.
- `apps/geocedg/application-profile.yml` is the generated menu/toolbar source of
  truth. Midpoint is presently grouped with linear/derived tools and Text with
  presentation tools, while the parameter group contains slider and related
  controls.
- persistent user-tool buttons use a separate FlowLayout container and cloned
  native button dimensions/icon presentation. The reported 1–2 pixel offset is
  not established without a real component/icon-layout capture.
- About content is composed by `GeoCeDGActionRegistry` from localized product
  text, package metadata, the pinned upstream baseline and author/license data.
- `packaging/windows/package.yml` is the current version authority (`0.9.0`);
  package names, generated provenance and About/title surfaces derive from it.

The current version surfaces are deliberately distinguished:

| Surface | Current authority/effect |
|---|---|
| product/package version | [`packaging/windows/package.yml`](../../packaging/windows/package.yml) `application.version = 0.9.0` |
| ZIP/MSI/EXE names and installer version | `tools/release/build-windows-package.ps1` consumes the package profile; it must not gain a second hand-maintained version |
| runtime/About/title and DXF provenance | `GeoCeDGProductInfo` / generated build provenance consume the same package version |
| upgrade identity | `application.upgrade_uuid` is a separate stable installer identity and must not be regenerated for the 1.0 promotion |
| application profile/manifests | schema versions describe their own formats, not the public product version; tests/docs may assert the package value but are derived consumers |
| upstream baseline | GeoGebra `5.4.928.0` is provenance, not the GeoCeDG product version |

### 2.3 Licensing and packaging authority

The [G4 packaging report](../validation/g4_standalone_packaging_report.md)
establishes technical app-image, ZIP, MSI and EXE packaging. It does not
establish public redistribution rights. The root `LICENSE` is intentionally not
an invented grant; [`LICENSES/`](../../LICENSES/README.md), the
[component matrix](../licensing/component-matrix.md) and
[asset manifest](../../geocedg/resources/assets-manifest.yml) remain
incomplete; an SBOM enumerates software but does not determine copyright,
asset, trademark or redistribution rights. The current package profile remains
`internal-evaluation` with public redistribution blocked.

## 3. PRE-G9B-S1 — DXF correctness and bounded stabilization

### 3.1 Artifact intake and provenance

No named author artifact was present during this design task. The proposed intake
location is a repository-adjacent, non-versioned directory:

```text
../GeoCeDG-author-inbox/PRE-G9B-S1-DXF/
```

Only these expected files are in scope:

```text
TestExport1.cedg
geocedg-export.dxf
Elipse.cedg
Elipse.dxf
```

The execution protocol is:

1. treat each author original as read-only and record filename, byte size,
   SHA-256, received timestamp, role and author-observed failure before opening
   or transforming it;
2. distinguish source constructions from observed generated DXF outputs in the
   intake ledger;
3. unpack/read copies only in an ignored run directory such as
   `artifacts/pre-g9b-s1/dxf/<run-id>/`; `artifacts/` remains derived evidence,
   never canonical source authority;
4. reproduce without rewriting the originals and hash every generated output;
5. after diagnosis, copy only the minimum byte-exact source fixtures needed for
   regression into `models/regression/pre-g9b-s1-dxf/original/`, with a
   versioned model manifest recording original name/hash, provenance, observed
   failure, expected behavior and license-review state;
6. place normalized/synthetic expectations beside that corpus only when their
   derivation is explicit, and register the corpus in the validation catalog;
7. never silently rewrite a native/binary fixture, and never version unrelated
   user files or make the author's generated DXF the semantic oracle.

### 3.2 DXF reproduction and classification matrix

| Case | Reproduction axes | Expected authority | Classification rule |
|---|---|---|---|
| `DXF-2` ellipse | native file load; source defined/visible state; selected/all export population; neutral model entity count; DXF entity count | exact G5 ellipse mapping, independent of extended flag | successful-but-empty result reproduced -> G5 regression; invalid source or user-flow mismatch -> record exact cause, no redesign |
| `DXF-1` Locus V2 | feature off/on; source semantic currentness; Desktop population; preflight entry; branch/component/domain conversion; approximation; model/writer/sidecar/report | approved G9X1 semantic Locus V2 adapter and strict/sidecar policy | admissible source omitted at any stage -> G9X1 regression localized to that stage |
| `DXF-1` SplineV2 | same pipeline plus producer/definition inspection | common `GeoLocusV2` branch/component contract, not render samples | common contract sufficient but skipped -> G9X1 regression; insufficient contract/new fidelity -> stop and propose `PRE-G9B-S1-SPLINE-DXF` |
| strict mixed export | supported + unsupported/invalid source | no silent partial output; paired sidecar where fidelity is reduced | partial publication without admitted policy -> regression |
| determinism | repeated fixed revision/request | byte-stable model/entity order and evidence | difference unexplained by declared metadata -> defect candidate |

For each failure, instrumentation follows the read-only path in order: source
traversal, type recognition, feature gating, preflight, component/domain
conversion, approximation, neutral-model population, DXF serialization,
Desktop selection/population and reporting. The first failed boundary owns the
defect. Screen polylines, render tessellation and proximity are forbidden as
repairs.

### 3.3 Stabilization triage

| # | Request | Classification | Smallest reviewable slice / rationale |
|---|---|---|---|
| 1 | SplineV2 described as “semantic LocusV2” | `BOUNDED_MINOR` | `PRE-G9B-S1-POLISH`: make presentation producer-aware while retaining `GeoLocusV2` as the real semantic class; no label/type spoofing |
| 2 | Text Properties Apply/OK/Cancel | `BOUNDED_BUT_SEPARATE` | `PRE-G9B-S1-TEXT-DIALOG`: characterize one draft/commit/cancel transaction across embedded dialog, redefine, selection/tab changes and Undo before changing buttons |
| 3 | construction-object font sizes including 10 pt, independent of menus/icons | `BOUNDED_BUT_SEPARATE` | `PRE-G9B-S1-PRESENTATION-PREFERENCES`: adding 10 to the global list would couple object and UI fonts, so the object/view preference must first be separated |
| 4 | construction Text scales visually with zoom | `NONTRIVIAL_FOLLOWUP` | `PRE-G9B-S1-TEXT-ZOOM`: specify screen-font versus world-scaled modes, scale reference, bounds/hit/LaTeX, multiple views and persistence before implementation |
| 5 | independent menu, toolbar-icon and Algebra/Protocol/Graphics UI sizes | `NONTRIVIAL_FOLLOWUP` | `PRE-G9B-S1-PRESENTATION-PREFERENCES`: reuse existing preference infrastructure, but add a bounded ownership/schema design; never construction semantics |
| 6 | move Midpoint; add Text to parameter group | `BOUNDED_MINOR` | `PRE-G9B-S1-POLISH`: manifest-only taxonomy change plus compiler/menu/toolbar tests; one authoritative action placement |
| 7 | persistent-tool buttons vertically displaced | `NOT_REPRODUCED` | capture real component bounds, insets, baseline and painted icon bounds in `PRE-G9B-S1-POLISH`; repair only if a bounded container/icon cause is reproduced |
| 8 | author About text and baseline/author | `BOUNDED_MINOR` | `PRE-G9B-S1-POLISH`: localized/profile-backed text. Preserve the requested wording below; author must decide the likely `GeoGeobra` typo before implementation |
| 9 | product version 1.0 | `BOUNDED_BUT_SEPARATE` | `PRE-G9B-P1`, after D1 disposition; update the single package-version authority and all derived surfaces atomically, preserving the upgrade identifier |

The author-provided About text is preserved literally for review:

```text
GeoCeDG — Computational extended Descriptive Geometry based on GeoGeobra. Geometry and identity belong to the shared kernel. Upstream compatibility is preserved; this is an author-review implementation candidate.
GeoGebra upstream baseline: 5.4.928.0
Manuel Prado-Velasco, Universidad de Sevilla
```

`GeoGeobra` appears likely to be an unintended spelling of `GeoGebra`; this
design does not silently correct it. Exact final wording is an unresolved author
decision.

### 3.4 S1 implementation and verification slices

Each slice requires separate productive authorization. `PRE-G9B-S1-DXF` owns
artifact intake, reproducible diagnosis and only proven G5/G9X1 corrections.
`PRE-G9B-S1-POLISH` owns only items 1, 6, 8 and item 7 if reproduced as bounded.
The three cross-cutting UI requests retain their separate identifiers above.

A future additive `PHASE -Phase PRE-G9B-S1-<slice>` selection should include
only focused tests, directly affected G5/G9X1 or Desktop regressions, and the
versioned artifact-backed cases. The umbrella S1 closeout must show every
required slice accepted or explicitly deferred by the author. It must also
record an author smoke for actual external DXF usability and the bounded UI
items; automated parsing is not a substitute for that smoke. Registry work is
not authorized or needed in this design candidate.

S1 stops before implementation when:

- an author artifact hash/source provenance is unavailable;
- SplineV2 requires new exact/fidelity semantics rather than the existing
  Locus V2 export contract;
- a “minor” request requires a new dialog, styling or text-rendering architecture;
- a fix would consume viewport/render samples as geometric export authority;
- strict export would need to publish an unexplained partial file; or
- a change would modify geometric identity, construction XML or unrelated
  Classic behavior.

```text
PRE-G9B-S1 = DESIGNED — PRODUCT IMPLEMENTATION NOT YET AUTHORIZED
```

## 4. PRE-G9B-D1 — licensing, assets and deployability

D1 is a separate major phase. Its gates are sequential and fail closed:

| Gate | Deliverable | Decision boundary |
|---|---|---|
| L1 — inventory/provenance | exact source and packaged-runtime inventory for GeoCeDG-authored and modified/unmodified upstream sources/resources; dependencies/native libraries; fonts/icons/images/styles/translations; Java runtime; WiX/jpackage resources; installer metadata; names/trademarks; hashes and package origin where practical | evidence only; SBOM is input, not a rights conclusion |
| L2 — authoritative research | primary license texts and official upstream/dependency/distributor asset, font, trademark and redistribution terms current at execution | distinguish software, notices, assets, trademarks, redistribution and commercial scope; no autonomous legal advice |
| L3 — disposition proposal | per-component `ALLOW`, `ALLOW_WITH_NOTICE`, `REPLACE`, `EXCLUDE`, `REQUIRES_PERMISSION`, or `UNKNOWN / BLOCKED`, plus alternative package compositions | stop for explicit author/human legal/branding decision before any consequential change |
| L4 — authorized remediation | only separately approved notice/license files, replacements/exclusions, package composition, SBOM and installer records | no work until L3 decision names allowed scope and content |
| L5 — deployability verification | rebuilt package composition, hashes, notices, absence of excluded assets, SBOM correspondence, launcher, install/uninstall, file association, private-artifact exclusion and reproducibility evidence | technical status is distinct from legal/branding approval |

L5 may conclude `DEPLOYABILITY TECHNICALLY READY — PENDING HUMAN
LEGAL/BRANDING APPROVAL`. Only an explicit human decision may conclude
`DEPLOYABILITY APPROVED FOR <explicit scope>`. Existing G4 technical PASS and
public-redistribution blocker remain unchanged throughout this design.

A future D1 verification plan should use deterministic inventory/license/static
checks for L1–L4 and the repository's canonical packaging/workstation gates for
L5. A bounded PHASE selector may aggregate D1-specific assertions, but it must
not redefine FINAL, package acceptance or legal approval. Any package build
must bind its manifest/SBOM to the exact candidate and runtime hashes.

## 5. PRE-G9B-P1 — public surface and version 1.0 promotion

P1 is a small, separately authorized gate after successful S1 and the explicit
D1 disposition. It must evaluate, not assume:

1. enabling the already approved Locus V2/SplineV2 public construction surface
   by default only in `AppConfigGeoCeDG`;
2. preserving Classic behavior and historical-file reconstruction independently
   of new-object creation policy;
3. retaining the old `--enableLocusV2` argument as a diagnostic override,
   compatibility syntax or removing it only through an explicit decision;
4. keeping experimental/internal-only capabilities gated;
5. deciding the independent `cedg.export.dxf.extended` default from S1 evidence,
   rather than enabling it transitively with Locus V2;
6. aligning package/profile/About/version documentation from the single version
   authority; and
7. promoting package/product version `1.0.0` and public display `1.0` only at this
   release/readiness boundary, if the author approves that convention.

Licensing status is not a geometric feature flag. P1 requires focused startup,
creation-filter, Classic-containment, historical-load, package and interactive
smoke evidence. It does not enable every experimental feature and does not
authorize G9B.

## 6. Compatibility and authority matrix

| Concern | S1 | D1 | P1 |
|---|---|---|---|
| geometric/source authority | unchanged; DXF reads immutable semantic snapshots | unchanged | unchanged |
| render/view authority | forbidden for export semantics; UI polish only | none | startup/presentation only |
| old `.ggb`/`.cedg` | preserve; no inferred associations | package must include required loaders/notices | reconstruction preserved even if creation default changes |
| Classic profile | regressions only, no GeoCeDG policy leakage | package composition explicitly inventoried | V2 default remains unchanged in Classic |
| feature flags | diagnose Locus/DXF flags independently | not a rights mechanism | decide each public default independently |
| public redistribution | remains blocked | may become technically ready; human decision required | only within explicitly approved D1 scope |
| G9B/G9C/G9U2/G10 | not authorized | not authorized | not authorized |

## 7. Validation strategy and author evidence

Before productive work, each authorized slice must publish a validation matrix.
The minimum combined plan is:

- artifact hashes, load/reopen validity and exact source inventories;
- exact ellipse neutral-model/DXF assertions;
- LocusV2/SplineV2 branch/component/domain, preflight, sidecar, strict-failure,
  feature-off/on and deterministic-output cases;
- Desktop selection/population/reporting and author external-application smoke;
- focused UI state tests for each admitted polish slice, avoiding pixel timing
  except for the reproducible toolbar-layout measurement;
- no geometric side effects from presentation preferences/text behavior;
- deterministic license/package inventory and primary-source evidence links;
- L5 install/uninstall/association/composition/reproducibility smoke;
- P1 GeoCeDG default-on, Classic default-preservation, old-file reconstruction,
  diagnostic override, package/About/version and interactive creation smoke.

Acceptance receipts are technical evidence and never author approval. Earlier
G5, G9X1, G4 and post-G9U1 evidence is preserved; it is not mechanically
reclassified by this design.

## 8. Open author decisions

1. Approve the proposed inbox, provide the four exact DXF artifacts there and confirm their
   redistribution/fixture-review status.
2. Confirm whether `GeoGeobra` in the supplied About sentence is intentional.
3. After S1 characterization, decide whether a conditional SplineV2 export
   capability subphase is needed and which nontrivial UI slices are mandatory
   before D1.
4. At L3, select the permitted deployment scope and every consequential
   replace/exclude/permission/branding disposition.
5. At P1, decide feature-flag compatibility, independent extended-DXF default,
   exact `1.0.0`/`1.0` presentation and whether public distribution is in scope.
6. Only after this track, decide whether and when to authorize G9B.

No ADR is created by this candidate. Calendar sequencing and bounded phase
boundaries fit the roadmap; future new DXF fidelity semantics, Text rendering
semantics, or a durable cross-product licensing/package policy may independently
warrant an ADR when their actual choices are known.

## 9. Authorization state

```text
PRE_G9B_ROADMAP_EXTENSION = PRE-G9B ROADMAP EXTENSION — DESIGN CANDIDATE — PENDING AUTHOR REVIEW
PRE_G9B_S1_IMPLEMENTATION_AUTHORIZED = false
PRE_G9B_D1_RESEARCH_OR_IMPLEMENTATION_AUTHORIZED = false
PRE_G9B_P1_PROMOTION_AUTHORIZED = false
G9B_AUTHORIZED = false
G9C_AUTHORIZED = false
G9U2_AUTHORIZED = false
FURTHER_G12_AUTHORIZED = false
G10_PRODUCT_IMPLEMENTATION_AUTHORIZED = false
```

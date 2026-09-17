# PRE-G9B-P1 validation matrix

- Phase: `PRE-G9B-P1` — public surface promotion, NC packaging readiness
- Status: **MATRIX — SATISFIED BY THE AUTHOR-APPROVED CANDIDATE**
  `cac345ae2ba29bb983613f7fb66461b4d063c87b`, tree
  `71153c3b95b974225c16e52fd177501ec7537663`; `PRE-G9B-P1 = PASS — AUTHOR
  APPROVED`, `AUTHOR SMOKE = PASS`
- Entry gates: `PRE-G9B-D1 = PASS — AUTHOR APPROVED`, `PROFILE NC = APPROVED`,
  `PRE-G9B-P0 = PASS — AUTHOR APPROVED`, canonical version `1.0.0`,
  package still `internal-evaluation`
- `selfApproved=false`, `authorApproved=true`, `passClaimed=true`

P1 changes **policy and distribution packaging only**. It changes no geometric
semantics, no serialization, no version, and nothing closed by P0.

## 0. Characterized entry state

| Fact | Evidence |
|---|---|
| Both promoted flags are constructor-injected, immutable, default `false` | `AppConfigGeoCeDG:27-45`, `RuntimeFeatureService:36-56` |
| The only CLI seam is `AppGeoCeDG.createConfig` | `AppGeoCeDG:433-438` |
| Classic containment rests on `instanceof AppConfigGeoCeDG` inside `mayCreateLocusV2` | `RuntimeFeatureService:96-97` |
| Creation and preservation are already separate contracts | `RuntimeFeatureService:19-23,109-111`; `Construction.isFileLoading()` |
| `cedg.locus.v2` covers Locus V2 **and** SplineV2; a second flag is schema-forbidden | `application-profile.schema.json:415-427` |
| Extended DXF is independent, consumed only by the export controller | `GeoCeDGDxfExportController:80-91` |
| No licensing-delta/component-drift tooling exists | repo-wide search: zero hits |
| Package profile is single, closed, all-`const`, `internal-evaluation` | `package-profile.schema.json`, `package.yml` |
| Exactly one `"version"` may appear in `package.yml` | `build.gradle.kts:90-99` |

## A. Public surface

| # | Assertion | Method |
|---|---|---|
| A1 | GeoCeDG default config enables Locus V2 creation | unit on `AppConfigGeoCeDG()` |
| A2 | GeoCeDG default config enables extended DXF | unit on `AppConfigGeoCeDG()` |
| A3 | The two defaults are independently settable, neither implies the other | unit, all four combinations |
| A4 | `LocusV2`, `LocusLength`, `SplineV2` are command-visible by default | `createCommandFilter()` |
| A5 | Dedicated commands dispatch without a flag | `requireLocusV2Access` on a default GeoCeDG construction |
| A6 | V2-operand overloads reachable by default | `Intersect`/`Length`/`Point`/transform gates |
| A7 | Locus V2 profile actions are enabled, not merely visible | `GeoCeDGActionRegistry.unavailableReason == null` |
| A8 | `--enableLocusV2=false` still disables creation (diagnostic override retained) | arg parsing through `createConfig` |
| A9 | `--enableExtendedDxf=false` still selects the legacy G5 export path | idem |
| A10 | `--enableLocusV2` remains accepted syntax and changes no durable identity | arg parsing; no serialization delta |
| A11 | Every other feature gate keeps its current default | full manifest inventory assertion |
| A12 | `cedg.laboratory.legacy`, `cedg.spatial.semantics`, `gated-g9u2` stay gated | inventory assertion |

## B. Persistence and compatibility

| # | Assertion | Method |
|---|---|---|
| B1 | Classic config still refuses Locus V2 creation | `mayCreateLocusV2` with a non-GeoCeDG config |
| B2 | Classic keeps upstream command set and defaults | Classic `AppD` command filter |
| B3 | Historical files reconstruct regardless of creation policy | load with creation explicitly off |
| B4 | Disabling creation never deletes or degrades persisted objects | feature-off round trip, byte-identical re-serialization |
| B5 | `.cedg` remains the native document identity | `GeoCeDGDocumentPolicy` |
| B6 | `.ggb` remains compatibility input only | `GeoCeDGDocumentPolicy`, packaging negative assertion |
| B7 | Serialization and persistent identity unchanged by the promotion | archive round-trip |

## C. DXF

| # | Assertion | Method |
|---|---|---|
| C1 | Extended DXF is the default GeoCeDG export path | `DxfExportPreflightPresentation.isExtendedDxfEnabled` |
| C2 | The gate remains independent of Locus V2 | A3 combinations |
| C3 | Exact G5 ellipse path unchanged | existing G5 regression |
| C4 | LocusV2/SplineV2 export unchanged under the approved G9X1 contract | existing G9X1 suites |
| C5 | Strict failure, sidecar, preflight, determinism unchanged | existing G9X1 suites |
| C6 | Classic cannot reach the GeoCeDG export policy | `isExtendedDxfEnabled` on non-GeoCeDG config |
| C7 | No new DXF fidelity or `SPLINE` entity introduced | no change to export contract files |

## D. P0 regression

| # | Assertion | Method |
|---|---|---|
| D1 | Version remains exactly `1.0.0`, single authority | existing P0 suite, unchanged |
| D2 | Three themes, `Original` default, paint-only canvas | existing P0 suite, unchanged |
| D3 | `Layout & Presentation` and presentation sizes unchanged | existing P0 suite, unchanged |
| D4 | `application.upgrade_uuid` unchanged | package profile assertion |

## E. Packaging and distribution profiles

| # | Assertion | Method |
|---|---|---|
| E1 | Product metadata and distribution policy are separate concerns | profile structure |
| E2 | Exactly one product-version authority survives | `build.gradle.kts` guard still passes |
| E3 | `INTERNAL` keeps its marker and stays fail-closed for redistribution | profile + builder |
| E4 | `NC` is redistributable within the D1-approved composition | profile + builder |
| E5 | `NC` does not carry `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION` | builder output |
| E6 | `NC` identifies the composite non-commercial nature and ships the full legal bundle | builder output |
| E7 | `COMMERCIAL` fails explicitly and names the pending external terms | negative test |
| E8 | `.cedg` association preserved; no `.ggb` claim, in every profile | MSI inspection |
| E9 | `upgrade_uuid` stable across profiles | profile assertion |
| E10 | Outputs regenerable from authority + code, no hand-edited names | builder derives every name |

## F. Licensing-impact delta

| # | Assertion | Method |
|---|---|---|
| F1 | Unchanged composition reports `UNCHANGED` against the D1 baseline | delta run on current tree |
| F2 | Own-code rebuild is provenance-only, not a new external dependency | project-output JAR handling |
| F3 | A new external component fails closed | synthetic exercise |
| F4 | Changed source identity fails closed | synthetic exercise |
| F5 | Changed license/notice metadata fails closed | synthetic exercise |
| F6 | Machine-readable output plus short human summary | output shape |
| F7 | Baseline is the D1 overlay, not a competing inventory | consumes `component-disposition.json` |

### F design constraints discovered

- Effective disposition = overlay value when `component_id` is present, else the
  audit `disposition_candidate` (`component-disposition.json:32-38`).
- Three **incompatible identifier namespaces** exist and must be aliased
  explicitly, not joined naively: the audit (`jar:*`, `font:*`, bare class ids),
  the overlay (which introduces `runtime:temurin-25.0.4+7` and
  `installer-tool:wix-5.0.2` with no audit counterpart), and
  `source-access-manifest.json` (a third namespace of 12 ids).
- Ten project-output JARs legitimately retain `UNKNOWN / BLOCKED` in the audit;
  a naive "blockers == 0" assertion would produce a false alarm.
- Byte identity is `actual_distribution_evidence.sha256` for JARs and `sha256`
  for fonts. Filenames and filename-derived versions are never identity.
- The single machine-readable NC↔COMMERCIAL discriminator in the payload layer
  is one `commercial_compatible: false` (OpenGeoProver).

## G. Continuation: legal-bundle reconciliation and guide audit

Added after the first P1 candidate obtained PHASE and FINAL. The continuation
is corrective and bounded; it reopens no D1 investigation and redesigns no
packaging, Locus V2, SplineV2 or DXF behavior.

| # | Assertion | Method |
|---|---|---|
| G1 | No general legal document shipped by every profile asserts `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION` | `PreG9BP1PublicSurfaceTest.generalLegalDocumentsAreProfileNeutral`; `packaging.marker.*` |
| G2 | Each profile notice carries exactly its own marker and not the other's | `packaging.notice.internal_evaluation_only`, `packaging.notice.nc_distribution_notice` |
| G3 | PROFILE INTERNAL keeps its internal identity through its notice and manifest | `repositoryDefaultNoticeStillCarriesTheInternalMarker`; INTERNAL regression build |
| G4 | A redistributable build fails closed if a general legal document still denies redistribution | builder assert; `packaging.legal-bundle-profile` |
| G5 | Dated D1 evidence that records a past internal-evaluation state is preserved verbatim | `datedD1EvidenceKeepsItsHistoricalDistributionMarker` |
| G6 | `LICENSES/manifest.json` still pins every bundled legal text exactly once, with the reconciled hash | `legalBundleManifestPinsTheReconciledBundleText`; `packaging.legal-bundle-manifest` |
| G7 | `build-manifest.legal_bundle.public_profile` reports the built profile, not a hardcoded `PROFILE NC` | NC and INTERNAL manifests |
| G8 | The user guide states both promotions, both retained overrides and their independence | `userGuideDescribesThePromotedDefaultsAndTheDxfBoundary` |
| G9 | The guide states `G9X1 exact DXF SPLINE = NOT IMPLEMENTED` and the approximate `LWPOLYLINE` representation of SplineV2 | same test |
| G10 | The guide attributes approximation to geometric semantics, never to render, viewport, zoom or DPI, and does not claim a certified global error | same test |
| G11 | Exactly one tracked guide authority exists; the packaged copy is a byte copy of it | `packagedGuideIsAByteCopyOfTheTrackedSourceGuide` |
| G12 | Version authority stays `1.0.0` and `upgrade_uuid` is unchanged | `packageProfileKeepsOneVersionAuthorityAndUpgradeIdentity` |
| G13 | The two promoted defaults remain independent in all four combinations | `promotedSurfacesAreOnByDefaultAndIndependent` |

### Deliberately preserved

`geocedg/validation/pre-g9b-d1/component-audit.json` (immutable D1 factual
closure), `geocedg/resources/assets-manifest.yml` and
`geocedg/resources/source-access-manifest.json` are dated D1 evidence
snapshots. Their `distribution_marker` records the package state at their
evidence date and is not a statement of the conditions of a later build, so it
is preserved. Historical D1/G4/P0 reports keep their original wording for the
same reason.

## Verification plan

Focal suites first, then canonical gates. P1 classifies as
`RELEASE_OR_MILESTONE` and additionally changes verification infrastructure and
packaging, so `FINAL` is required and is not substitutable. A bootstrap-impact
review is required because packaging prerequisites are touched.

`PACKAGING`, `STATIC`, `INFRA_UNIT`, `PHASE PRE-G9B-P1`, then `FINAL`.

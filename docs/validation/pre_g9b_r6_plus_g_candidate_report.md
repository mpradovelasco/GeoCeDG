# PRE-G9B-R6-plus-G — help, product metadata and version transition: candidate report

```text
TECHNICAL_CANDIDATE_STATE  = FROZEN AT THE G-R1 VERSION-TRANSITION COMMIT T' (the commit that
                             adds this version of this file; its identity is reported outside
                             this file)
SUPERSEDES                 = original technical candidate T dd3937cfba7092877f48e7f927dd6213959292cf
                             (tree b45311d2b78d9c3625e379835b8d76031df9530a) — VERIFIED —
                             SUPERSEDED FOR FINAL REVIEW; unchanged on its own branch
AUTHOR_DECISION            = NOT_RECORDED_IN_THIS_ARTIFACT

PHASE                      = PRE-G9B-R6-plus-G (with the G-R1 corrective integration)
CHANGE_ROUTE               = ORDINARY
VERIFICATION_CLASS         = INTEGRATED_PHASE — FROZEN BY THE AUTHOR
CLOSEOUT_MODE              = AUTHOR_OPERATED
FROZEN_ACCEPTANCE          = STATIC; PHASE -Phase PRE-G9B-R6-PLUS-G (PM-1 to PM-12 focused tests and
                             the G-R1 regression class); INTEGRATION (Classic compatibility
                             included); INFRA_UNIT; PACKAGING -CheckToolchain;
                             PACKAGING -VerifyPackagingArtifacts on an INTERNAL set and on an NC
                             set built from this candidate; COMMERCIAL refusal evidence with an
                             unchanged output root (each once on the exact candidate; reported
                             outside this file)
FINAL                      = NOT RUN (not authorized; no global verification-infrastructure
                             change was found, §10.3)
VERSION / VERSION_DATE     = 1.1.0 / 2026-10-15 — IN THE CANDIDATE ONLY — NOT APPROVED —
                             NOT PUBLISHED — NOT A RELEASE
PRODUCT CHANGE             = YES — GeoCeDG Desktop presentation, document-session lifecycle
                             (G-R1), build provenance and packaging (no inherited, shared or
                             kernel source)
SHARED / KERNEL CHANGE     = NONE
SERIALIZATION CHANGE       = NONE (no document, XML, preference or unit format)
GEOMETRIC SEMANTICS CHANGE = NONE
DOCUMENT-REPLACEMENT FIX   = OPEN ROUTES CORRECTED (SC-01, SC-02, SC-11; authorized by G-R1);
                             uncontracted routes and tool replacement unchanged (§7)
D-1                        = ACCEPTED RECOVERY — AUTHOR DISPOSITION (evidence of T, historical)
GUIDE_IMPACT               = UPDATED (EN and ES user guides, guide index)
BOOTSTRAP IMPACT           = NO CHANGE REQUIRED (§10.1)
INFRASTRUCTURE IMPACT      = PHASE_LOCAL (§10.3)
G TECHNICAL APPROVAL       = PENDING AUTHOR REVIEW
G AUTHOR SMOKE             = PENDING (not performed or attributed by the agent)
PRE-G9B-R6-plus CLOSEOUT   = NOT AUTHORIZED
PRE-G9B-R7 / G9B           = NOT AUTHORIZED
PUBLICATION / RELEASE      = NOT AUTHORIZED
selfApproved               = false
authorApproved             = false
passClaimed                = false
```

This artifact records only facts fixed when it was written. A later author decision
naming the exact technical commit is the sole authority for acceptance, for the adoption
of `1.1.0` and `2026-10-15`, and for publication. Machine-readable mirror:
[`pre-g9b-r6-plus-g-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-g-candidate-evidence.json).

## 1. Authority and lineage

| Commit | Role | Version in product bytes |
|---|---|---|
| `ceb5a7e9ef8bafcfabb10aeba5162451d88f79c3` (tree `fa748fd1…`) | published G design closeout; the authorized base | `1.0.0`, no version date |
| `6e09eee9…` | implementation authorization record and entry gate | `1.0.0`, no version date |
| `ebd6accb…` | document-replacement characterization report (SC-01 to SC-27) | `1.0.0`, no version date |
| `5962569e…` | development step: About, distribution condition, bilingual help, focused tests | `1.0.0`, no version date |
| `f52cf5b3…` | structured debt register and class-1 status reconciliation; last pre-transition commit | `1.0.0`, no version date |
| (`dd3937cf…`, not an ancestor) | the original version transition and candidate T, verified, superseded for final review | `1.1.0` / `2026-10-15` |
| `9854a75f…` | G-R1 authorization record, entry gate and D-1 disposition | `1.0.0`, no version date |
| `051a5173…` | G-R1 Open-route correction and its regression tests | `1.0.0`, no version date |
| `2696d855…` | G-R1 correction and tool-replacement report | `1.0.0`, no version date |
| `e7076658…` | consolidated author decision sheet; S3 and ADR 0029 status | `1.0.0`, no version date |
| this commit | **the rebuilt atomic version transition and frozen technical candidate T'** | `1.1.0` / `2026-10-15` |

Authorities: the [implementation authorization](pre_g9b_r6_plus_g_implementation_authorization_record.md),
the [G-R1 authorization](pre_g9b_r6_plus_g_r1_authorization_record.md), the
[design closeout](pre_g9b_r6_plus_g_author_design_closeout_record.md), the
[canonical prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-g-help-verification-and-closeout.prompt.md)
and the approved metadata contract, transcribed into the
[Windows packaging contract](../../geocedg/specs/packaging/windows-packaging.md) and the
[application profile](../../geocedg/specs/ui/application-profile.md).

## 2. Changes by architectural layer

| Layer | Change |
|---|---|
| Desktop presentation (GeoCeDG-owned) | `GeoCeDGAboutText` (About content and order, EN/ES); `GeoCeDGDistributionCondition` (four runtime states); `GeoCeDGStrictJson` (strict record reader); `GeoCeDGActionRegistry` (the existing About dialog shows the composition); `GeoCeDGProductInfo` (name constant, provenance, version date) |
| Desktop document-session lifecycle (GeoCeDG-owned, G-R1) | `AppGeoCeDG`: one session reset per committed transition — paste notice on every transition, `ExportArea` only on approved Open routes (E2, E3 inside an Open route, `loadBase64File`, the existing `loadFile`/`loadXML(String)` routes); E5 notice only ([correction report](pre_g9b_r6_plus_g_r1_correction_report.md)) |
| Build provenance | `build.gradle.kts`: version-date guard, canonical package-profile digest, profile table, provenance schema `3` in ASCII; `GeoCeDGBuildProvenance` loads schema `3` only |
| Package profile | `package.yml` schema `2`, `1.1.0`, `version_date` `2026-10-15`; `package-profile.schema.json` `package-profile-v2` with the required `version_date` |
| Packaging | `build-windows-package.ps1`: schema `2` only, binding to the provenance in the staged `desktop.jar`, `app/geocedg-distribution.json`, manifest `application.version_date` and `distribution_record`; `packaging-product.ps1`: schema `v2` constants and three phase-local subcontracts |
| Profile texts | About keys EN/ES; `Workspace.About` without "author-review implementation candidate"; shortcut and guide-tooltip corrections; Spanish ISO A "lámina" |
| Documentation | EN/ES user guides and index; normative transcription; register, characterization and correction reports, decision sheet, reconciliation, S3 and ADR 0029 status, roadmap and plan status |
| Verification registration | phase selection `PRE-G9B-R6-PLUS-G`; JUnit nodes; inventory selection (with the G-R1 regression class) and refresh; coverage-test pins; static-contract re-pins; packaging fixture and record case |
| Classic | none (`GeoGebraMenuBar`, `HelpMenuD`, `MacApplicationListener` untouched; inherited load routes covered by a G-R1 test) |

## 3. Metadata contract PM-1 to PM-12 as implemented

| Item | Implementation | Evidence |
|---|---|---|
| PM-1 identity | `GeoCeDGProductInfo.PRODUCT_NAME` equals `package.yml` and the schema constant | `productNameIsThePackageProfileConstant` |
| PM-2 version | `package.yml` the single authority; one `"version"` key; full `1.1.0` | P0/P1 version tests; `theSingleAuthority…` |
| PM-3 version date | required `application.version_date` `2026-10-15`; schema `2`; Gradle guard (one key, valid date, schema `2`); provenance schema `3`; never a clock or commit time | `theSingleAuthority…`, `theSchemasMoveTogether…` |
| PM-4 build identity | `Build: <12 hex> (clean)` / `(modified)` / `(state unavailable)` / `Build: unavailable`; never next to a date | `buildIdentityRendersEveryRepositoryState` |
| PM-5 distribution | builder-written record; four states; runtime-checked canonical digest; consistency only; COMMERCIAL never verifies; `desktop.jar` profile-neutral | fixture tests; packaging subcontracts; contract-boundary case; the T campaign runtime probe (§6) |
| PM-6 licensing | composite statement separated from component permissions; routes to the full texts; never "solely EUPL", Open Source or Free Software | `aboutContentAndOrder…`, `theLicensingSummaryStaysWithinItsAuthorities` |
| PM-7 About surface | existing `geocedg.help.about` text dialog; order 1–8 identical in EN and ES | `aboutContentAndOrder…`, `theAboutActionComposesTheRunningCopy`, `aboutShowsTheDesignatedVersionDate…` |
| PM-8 other surfaces | window and guide titles, `--app-version`, artifact names, `upgrade_uuid`, guides | `GeoCeDGProfileTest`, P1 guide test, artifact names `GeoCeDG-1.1.0-…` |
| PM-9 Classic | inherited About route unchanged; no GeoCeDG metadata | `classicHelpStillReachesTheInheritedAboutDialog`; upstream boundary |
| PM-10 determinism | provenance a pure function of `package.yml` and commit; record of profile, `package.yml`, commit and notice bytes; no clock | Gradle task inputs; `theSingleAuthority…` (no time key) |
| PM-11 tests | focused class `PreG9BR6PlusGProductMetadataTest` (17) and the updated version pins | §9 |
| PM-12 compatibility | no document, XML, preference or unit change; DXF sidecar schema unchanged | `G9X1DxfManifestTest` unchanged and passing |

## 4. Version transition (M1), rebuilt

Every commit before this one keeps `1.0.0` and no version-date field (`package.yml`
`schema_version` `1`). This commit replays the version-transition delta of T
(`f52cf5b3..dd3937cf`) onto the corrected lineage: 24 of its 25 files are byte-identical to
their T versions, and `docs/upstream/modified-files.yml` differs only by the G-R1 entries.
It also extends the JUnit selection with the G-R1 regression class, refreshes the inventory
through the official updater, moves the coverage pin and rewrites this report, its mirror,
the roadmap and the plan for T'. It changes together: `application.version` `1.1.0` and
`application.version_date` `2026-10-15`; package-profile schema `2`; the Gradle guard;
provenance schema `3`; the About date line; the guide version strings (EN, ES, index); the
version tests; the builder, the packaging check and its fixture; the static-contract
re-pins; the normative transcription. It is the last productive commit. `2026-10-15` is the
author-designated date; it is not this commit's date or any build or approval date.

## 5. About and licensing presentation

Content and order (EN; ES is line-for-line equivalent):

```text
GeoCeDG 1.1.0
Version date: 2026-10-15
Build: <first 12 hex digits> (clean)
Distribution condition: <verified marker> (<localized meaning>) | development build — … | distribution condition not verified — …

GeoCeDG — Computational extended Descriptive Geometry based on GeoGebra. …

GeoGebra upstream baseline: 5.4.928.0
Manuel Prado-Velasco, Universidad de Sevilla

Licensing. GeoCeDG is a composite work: no single licence covers the whole product. …
- GeoCeDG-authored software: EUPL-1.2.
- GeoCeDG-authored documentation and ordinary artwork: CC BY 4.0, … brand policy.
- Inherited GeoGebra source code keeps its EUPL-1.2 grant. … keep their own terms.
- Distribution notice of this copy: app/<notice>. | (development / unverified statement)
- Full texts: app/legal/ … | Full texts: LICENSE, NOTICE.md, THIRD_PARTY.md and LICENSES/ …
```

## 6. Distribution states

Fixture tests cover INTERNAL, NC, DEVELOPMENT and UNVERIFIED, and as UNVERIFIED: a
mismatched version, date, name, revision, digest, notice hash, marker, status or flag;
COMMERCIAL; a combined or undeclared profile; an unsupported schema or kind; extra or
missing members; a path in the notice name; an absent notice; a missing, malformed,
non-UTF-8, BOM-prefixed, duplicate-key, lenient or oversized record; a build without a
binding.

Historical runtime evidence of T (the same distribution code): the shipped `desktop.jar` of
T, byte-identical in its INTERNAL and NC sets, evaluated the official INTERNAL record as
`INTERNAL`, its NC app-image as `NC`, no launcher as `DEVELOPMENT`, a launcher without a
record and a tampered record as `UNVERIFIED`, and composed the EN and ES About. It is not
acceptance evidence for T'; the official INTERNAL and NC builds and checks run on T'.

## 7. Document replacement: characterization and correction

[Characterization](pre_g9b_r6_plus_g_document_replacement_characterization_report.md)
(historical fingerprint of the uncorrected product): 27/27 scenarios, contract violations
SC-01, SC-02 and SC-11. [G-R1 correction](pre_g9b_r6_plus_g_r1_correction_report.md):
the approved Open routes (File > Open of `.html`, API `openFile` of native documents, URL
drops) reset the export-area session once after commitment, and every committed document
transition clears the previous paste notice; `setBase64`, non-native `openFile`/`loadXML(File)`
and API `setXML` keep their export area (no contract); tool replacement, Insert File, Apply
Styles and the embedded close are unchanged. Regression class: 9 tests (4 corrective fail on
the uncorrected code); re-characterization: 28 of 57 rows change, exactly the authorized
ones. `OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT` keeps its mandatory disposition (proposal in
the correction report §6).

## 8. Help coverage as implemented

| Capability | EN / ES section | Change in G |
|---|---|---|
| About, version date, build, distribution condition, licensing | §1.3 | new paragraphs |
| Installed-only capabilities (library, verified condition) | §2.1 | qualifier |
| Orientation cue in Preferences | §2.4 | editorial (selected) |
| Physical PDF line widths and point markers | §4.5 | sentence |
| Export Text as Shapes and embedded Type 1 fonts | §4.5 | paragraph "Text in PDF" |
| Numeric thickness field of the style bar | §12.3 | sentence |
| Numeric order of the layer groups | §12.5 | editorial (selected) |
| Curated GeoCeDG library and its caveats | §13.2 | paragraph "The GeoCeDG library" |
| TD-PDF-1, AutoCAD 2026 only, F4-A-L1 | §15 | three limitation bullets |
| Spanish ISO A terminology "lámina" | §4.2 (ES) and profile texts | editorial (selected) |
| Shortcut description; guide-window tooltip | profile texts | editorial (selected) |
| Version strings | title lines, §1.3, §2.1, footer, index | `1.1.0` in this commit |

Structure unchanged: 1 / 16 / 87 headings and 17 section markers in both editions; no
new heading, window or help framework; no Classic-shared help resource changed.
`OBS-R6P-SAVE-WARNING` is not claimed: the guides describe no save warning. G-R1 makes the
product conform to the existing export-area description and needs no guide change.

## 9. Development verification evidence (before the freeze)

| Check | Result |
|---|---|
| `PreG9BR6PlusGR1OpenRouteSessionTest` | 9/9 pass; negative control: the 4 corrective tests fail on the uncorrected code, the 5 preservation tests pass |
| 13 related classes (drawing scale, hidden layers, base identity, D1 units and notice, F3 surface, E3 ISO A border, B export surface, base fingerprint, smoke follow-up, E3-R1 interoperability) | 119/119 pass (128 with the regression class) |
| Re-characterization (scratch probe, twice) | 28 of 57 rows change, exactly the authorized ones; both runs identical |
| Checkstyle (Desktop main and test) | no finding in the changed files |
| Upstream boundary (`Assert-GeoCeDGUpstreamBoundary`) | OK (1069 registered files) |
| JSON mirrors; Markdown links of the new and changed G-R1 documents | parse; 0 broken links |
| JUnit inventory | refreshed by the official updater from dry-run discovery of both modules and executed, passing evidence on this working tree: `final.shared` 6992 (unchanged), `final.desktop` 2153 (T: 2144; +9 G-R1 tests, 1 skipped), `pre-g9b-r6-plus-g.desktop` 127 (T: 118); Desktop discovery 2159 (T: 2150); `junit_inventory` pin `d1cfacfbfd53df712312878d62af4586783629e61ba7720eb7aee484b86533ac` |
| Development `STATIC` on the working tree | `ACCEPTED / COMPLETE`; standing diagnostics only (governance `FINDING`, historical-consistency `UNAVAILABLE`) |
| Development `INFRA_UNIT` on the working tree | `ACCEPTED / COMPLETE`, 22 leaves, no diagnostic finding |

## 10. Verification design

### 10.1 Bootstrap impact

**No change required.** No workstation prerequisite, toolchain, runtime or environment
contract changes: the packaging toolchain (JDK 25 `jpackage`, .NET 8, WiX 5.0.2) and the
Gradle toolchains are unchanged.

### 10.2 Registration

- Phase selection `PRE-G9B-R6-PLUS-G`: `compile.desktop.semantic`,
  `workspace-profile.product`, `junit.desktop.pre-g9b-r6-plus-g.semantic`,
  `packaging.product`, `infra.contract-boundary`.
- JUnit selection `pre-g9b-r6-plus-g.desktop`: `PreG9BR6PlusGProductMetadataTest`, the
  version, About, guide and DXF-sidecar classes, and `PreG9BR6PlusGR1OpenRouteSessionTest`;
  impact paths include `AppGeoCeDG.java` and the regression class.
- Pins: inventory selections 63, phase selections 68, G selection identities 127;
  static-contract inputs `package.yml`, `package-profile.schema.json`,
  `build-windows-package.ps1`, `build.gradle.kts` and the `static_contracts` catalog as in
  T (byte-identical inputs); the `junit_inventory` pin through
  `Get-VerificationCanonicalTextSha256`.

### 10.3 Infrastructure impact: phase-local

The builder and the packaging check change under the E1-P precedent: the schema
constants follow the approved migration, and `packaging.version-date`,
`packaging.distribution-record` and `packaging.distribution-record.build-manifest` are
phase-local contract additions inside `packaging.product`, exercised by a new fixture
case of `infra.contract-boundary`. `verify.ps1`, the shared verification modules, the
registry and inventory schemas, the output adapters, the receipt and closeout flow and
every `infra.*` check implementation are unchanged. No escalation is requested.

### 10.4 Frozen acceptance plan (run once each on this exact candidate)

```text
tools/agent/verify.ps1 -Profile STATIC        -LogDirectory artifacts/agent/<fresh>
tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-PLUS-G -LogDirectory artifacts/agent/<fresh>
tools/agent/verify.ps1 -Profile INTEGRATION   -LogDirectory artifacts/agent/<fresh>
tools/agent/verify.ps1 -Profile INFRA_UNIT    -LogDirectory artifacts/agent/<fresh>
tools/agent/verify.ps1 -Profile PACKAGING -CheckToolchain -LogDirectory artifacts/agent/<fresh>
tools/release/build-windows-package.ps1 -Target All -DistributionProfile INTERNAL
tools/agent/verify.ps1 -Profile PACKAGING -VerifyPackagingArtifacts -LogDirectory artifacts/agent/<fresh>
tools/release/build-windows-package.ps1 -Target All -DistributionProfile NC
tools/agent/verify.ps1 -Profile PACKAGING -VerifyPackagingArtifacts -LogDirectory artifacts/agent/<fresh>
tools/release/build-windows-package.ps1 -DistributionProfile COMMERCIAL   (must refuse; output root unchanged)
```

Each artifact set is verified before the next build replaces it; the campaign script keeps
scalar exit codes and records every step it runs or skips (the D-1 defect of T's campaign).
Results, receipts, exit codes and log paths are reported outside this file. Recorded
intermittent observations (O-1, O-2, O-27) are monitored there.

## 11. Author smoke checklist

1. Development run (`gradlew :desktop:desktop:runGeoCeDG`): title `GeoCeDG 1.1.0`;
   **Help → About GeoCeDG** in English and Spanish: order of §5, `Version date:
   2026-10-15`, a build line, "development build — not a distribution; not for
   redistribution", the licensing summary with the repository route.
2. INTERNAL app-image built from this candidate: About shows the INTERNAL marker,
   "internal evaluation — not for redistribution", the notice `app/INTERNAL_EVALUATION_ONLY.txt`
   and `app/legal/`; the build line says `(clean)`.
3. NC app-image or ZIP built from this candidate: the NC marker, "non-commercial
   distribution under PROFILE NC", `app/NC_DISTRIBUTION_NOTICE.txt` and `app/legal/`.
4. Optional negative check on a copy: delete or edit `app/geocedg-distribution.json`
   in a scratch copy of an app-image; About must show "distribution condition not
   verified — not for redistribution".
5. Help entry points: **Help → GeoCeDG user guide** opens the edition of the product
   language with `GeoCeDG 1.1.0` and the new paragraphs (§1.3, §4.5, §12.3, §13.2,
   §15); the guide-window and keyboard-shortcut texts.
6. Classic (**File → Open Classic diagnostic session**): Classic Help → About shows the
   inherited GeoGebra Classic dialog, unchanged, with no GeoCeDG metadata.
7. Open replaces the export session: define a MANUAL export area (or link an ISO A border)
   and show its overlay; **File → Open** another `.cedg` (and an `.html` page with a
   GeoCeDG archive, and a dropped `.cedg`): the overlay is gone and the next Picture/PDF
   export proposes no previous rectangle.
8. Paste notice: paste between two documents with different units to show the notice;
   after a successful Open it is gone; a cancelled chooser or save prompt, or a corrupt
   file, keeps it.
9. Preserved session: a tool replacement (**Tools → Create New Tool** replacing an
   existing tool) and **File → Insert** keep the export area and its overlay.

## 12. Remaining blockers and author decisions

- Approval of this exact candidate (technical review and author smoke); adoption of
  `1.1.0` / `2026-10-15` happens only through that approval. T is superseded for final
  review and stays unchanged.
- The [consolidated author decision sheet](pre_g9b_r6_plus_g_r1_author_decision_sheet.md):
  24 rows gating the global closeout, in five groups, including the B-1 residue without a
  contract and the B-2 proposal (not a document transition; working layer to G11).
- Class 3 and class 4 items stay retained unless separately authorized.
- Publication, the global `PRE-G9B-R6-plus` closeout, `PRE-G9B-R7`, `G9B` and any release
  remain separate author decisions.

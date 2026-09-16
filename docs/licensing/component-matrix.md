# Licensing and asset component matrix

Status: PRE-G9B-D1 **PASS — AUTHOR APPROVED**; **not legal advice**
Baseline: `9b93256b7df401ff056c37b502d82df4d72b1522`
Current disposition date: 2026-09-16

## Current PRE-G9B-D1 disposition

The immutable byte-level audit is
`geocedg/validation/pre-g9b-d1/component-audit.json`; current dispositions are
the overlay `component-disposition.json`. Package construction exports the
authoritative Gradle runtime graph and joins it to staged JARs by SHA-256.

| Material class | Current candidate treatment | NC | Commercial |
|---|---|---|---|
| GeoCeDG-authored software/scripts | EUPL-1.2, except valid file-level third-party terms | compatible | compatible |
| GeoCeDG-authored documentation | CC BY 4.0, with `docs/LICENSE.md` exceptions | compatible | compatible |
| GeoCeDG ordinary artwork | CC BY 4.0 by manifest licensing class | compatible | compatible |
| official GeoCeDG product artwork | CC BY 4.0 copyright treatment plus adopted separate brand policy | compatible subject to brand policy | compatible subject to brand policy |
| GeoGebra-authored code | EUPL-1.2, preserved notices/modifications | compatible | independently compatible under EUPL |
| GeoGebra Language Files/UI images/styles/Materials | applicable official CC BY-NC-SA 4.0-or-later and GeoGebra terms | compatible only within compliant NC product | GeoGebra agreement or independent replacement |
| independent runtime dependencies | 39/39 staged external JARs resolved for PROFILE NC with texts/notices/source access | compatible after obligations | compatible except OpenGeoProver needs clarification/permission |
| former exact unresolved JARs | Giac 70501, math-cross-platform 3.6.3, OpenGeoProver 20120725 | closed for NC | OGP permission/clarification remains |
| renderer fonts | 46/46 connected to primary evidence | compatible after texts/source access | compatible after texts/source access |
| former exact unresolved fonts | `jlm_cmmib10.ttf`, `jlm_cmssi10.ttf`, `jlm_cmti10.ttf` | closed | closed |
| Temurin 25.0.4+7 runtime | GPL-2.0-only with Classpath Exception and module terms; retain runtime legal tree/source access | compatible | compatible |
| WiX 5.0.2 | CLI/build tool excluded; MSI embeds MS-RL custom-action/UI payload | compatible with text/source handling | compatible with text/source handling |

The complete binary is composite and cannot be labelled solely EUPL or
globally Open Source/Free Software while GeoGebra NC Materials remain. PROFILE
NC has no remaining exact-payload technical provenance blocker. It remains
approved for the exact audited composition after professional review. The
current `1.0.0` package remains an internal-evaluation artifact pending the
separate P1/release materialization decision. Commercial OpenGeoProver and
GeoGebra-Materials questions are indexed in `LICENSES/UNRESOLVED.md` and
`geocedg_profile_commercial.md`.

## Historical baseline snapshot (2026-08-09)

The sections below preserve the first-pass G4/source triage. Where its
“future” statements differ from the current disposition table above, the
current D1 overlay controls; the historical text is not rewritten as if the
later evidence had existed then.

## Authorities inspected

1. Upstream `docs/upstream/GEOGEBRA_README.md:7-8`, which delegates licensing
   to the official [GeoGebra license page](https://www.geogebra.org/license).
2. The official license page as retrieved on 2026-08-09; the page says its
   terms were last updated in November 2025.
3. The checked-out source header template at
   `source/build-logic/convention/src/main/resources/fileTemplates/includes/License.java:1-15`.
4. Checked-in component and font license files, especially
   `source/shared/renderer-base/LICENSE` and the renderer font `licences/`
   directories.

The official page distinguishes component classes. It states that GeoGebra
source code is under EUPL 1.2; language files, documentation, and UI image/style
files are under CC BY-NC-SA 4.0 or later; installers, web services, and other
materials have separate GeoGebra terms; and trademarks remain separate. These
statements do not eliminate third-party notices embedded in the source tree.

## Component matrix

| Component | Baseline locations/evidence | Recorded terms or status | GeoCeDG treatment at this stage |
|---|---|---|---|
| GeoGebra-authored Java/source build code | `source/**/*.java`, `*.kt`, `*.kts`; standard header template says EUPL 1.2 at `License.java:6-10` | Official page identifies source code as EUPL 1.2. Individual third-party files may differ. | Preserve notices; keep a modified-upstream file register once code changes begin. Do not infer that non-code resources share this license. |
| Overall upstream product/package | Header template notes the overall package is for non-commercial use and points to the official page (`License.java:12-14`) | Official page applies component-specific and product-level terms. | GeoCeDG must not redistribute the upstream product bundle unchanged or claim the complete package is solely EUPL. Human review required before distribution. |
| Language/translation resources | Predominantly `.properties` below `source/shared/common-jre/src/main/resources` and Desktop resources | Official page classifies language files separately under CC BY-NC-SA 4.0 or later. | Do not assume they can ship in an unrestricted code-only distribution. Inventory each selected language resource and attribution before packaging. |
| UI images, icons, and styles | PNG/SVG/GIF and CSS/SCSS resources across shared/Desktop/Web | Official page classifies UI image and style files, including logos/icons/style sheets, with the language-file terms. | Default plan is GeoCeDG-owned branding/resources. Any retained upstream asset needs an entry in a future `geocedg/resources/assets-manifest.yml`. |
| G9U1 author-supplied GeoCeDG branding | Byte-exact promoted `helixTopBar.png` and `helixSnapshot.png` plus deterministic derivatives under `source/desktop/desktop/src/main/resources/org/geocedg/desktop/branding/v1/`; hashes/provenance in `geocedg/resources/assets-manifest.yml` | Author explicitly authorizes product use for the current internal candidate; no project-wide/public redistribution grant is inferred. | Use only for GeoCeDG frame/package and splash roles. Keep ignored ingestion originals unchanged, preserve deterministic provenance, and retain **BLOCKED** public redistribution pending human license/release review. |
| GeoGebra name and marks | `GeoGebraConstants.APPLICATION_NAME` and many upstream resources | Official page reserves trademark and other IP rights. | Redistributed product name is **GeoCeDG**. No upstream logo or mark is approved as GeoCeDG branding. |
| Upstream installers and web services | No `jpackage`/installer task is checked in here; official downloads/services are external | Official page gives installers/services separate non-commercial terms. | Do not reuse or redistribute the upstream installer. A later GeoCeDG installer must use own resources and pass a fresh audit. |
| JLaTeXMath/ReTeX renderer code | `source/shared/renderer-base/LICENSE:1-43` | GPL v2 or later with an explicit linking exception. | Preserve this license and exception; include it in the future third-party notice bundle. Modifications require component-specific review. |
| Renderer fonts | `source/desktop/renderer-desktop/.../fonts`, corresponding Web resources; `renderer-base/LICENSE:46-59` | Mixed OFL, GPLv2, Knuth-license, public-domain, and other/free-font statements. An OFL 1.1 text is checked in under `fonts/licences/OFL.txt`. | Treat every font family/file as a separately inventoried asset. Do not collapse the set to one license. Missing referenced texts must be resolved before packaging. |
| Cyrillic/Greek renderer resources | `source/web/renderer-web/.../resources/xml/{cyrillic,greek}/{COPYING,LICENSE}` | Component-local license files are checked in. | Preserve and surface the notices in the release inventory. |
| Maven/Gradle dependencies and native libraries | `gradle/libs.versions.toml`, Gradle dependency graphs, JOGL/Giac/native variants | Not audited by this first-pass source inventory. Each dependency retains its own terms. | Generate an exact resolved dependency/SBOM and license report in a dedicated audit task before release. |
| GeoCeDG-authored documentation in this branch | `AGENTS.md`, `FIRST_AGENT_TASK.md`, `docs/**`, `UPSTREAM.md`, `tools/agent/**` | No project-level GeoCeDG license has yet been approved. The G4 root `LICENSE` records that absence and is explicitly not a license grant. | Preserve authorship/provenance. Do not make a public licensing claim until maintainers approve a project license and complete the legal bundle. |
| Author-supplied CeDG scientific references | `docs/references/cedg/**/*.pdf`; per-file evidence and hashes in `docs/references/cedg/catalog.yml` | Mixed: three supplied articles identify CC BY 4.0; book/proceedings/publisher chapters include restrictions; remaining manuscripts/preprints are unreviewed. | Retain as local knowledge sources. Do not assume repository presence authorizes republication or release packaging. |
| Historical `Templatev7.ggb` | `models/legacy/template-v7/original/Templatev7.ggb`; SHA-256 and provenance in its manifest | Rights review is blocked: the container includes author-created macros plus embedded GeoGebra resources/code and no complete redistribution clearance. | Preserve for research and explicit Laboratory loading only; exclude from release packaging until human review. |

## Reproducible file-count snapshot

These counts are a triage aid, not a license classification. They were produced
with `git ls-files` at the pinned commit plus the bootstrap documentation:

| Pattern/category | Tracked count |
|---|---:|
| `*.java` | 7,237 |
| `*.kt` | 13 |
| `*.kts` | 52 |
| `*.properties` | 428 |
| `.properties` under shared `common-jre` resources | 390 |
| `.properties` under Desktop resources | 28 |
| `*.png` | 1,129 |
| `*.svg` | 703 |
| `*.gif` | 67 |
| `*.css` + `*.scss` | 44 |
| `*.ttf` | 48 |

The 1,899 raster/vector image files split, by broad source area, into 1,490
under shared common resources, 397 under Desktop, and 12 under Web. Counts do
not identify author, trademark status, or applicable license; the future asset
manifest must do so file by file for anything shipped.

## G4 legal-record infrastructure

G4 creates `LICENSE`, `LICENSES/README.md`, `NOTICE.md`, `THIRD_PARTY.md`, and
`geocedg/resources/assets-manifest.yml` as explicit status/inventory records.
They all identify packages as `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`.
The root `LICENSE` is deliberately a no-grant status notice because no
project-wide license has been approved; `LICENSES/` remains incomplete.

The package pipeline adds these records to the app image and emits an exact
JAR/hash SBOM. This closes the missing-file infrastructure gap only. It does
not close the substantive release blockers: approved GeoCeDG license, exact
third-party texts/attributions, embedded translation/UI/font rights, owned
final branding, or trademark review.

## Unresolved questions and gates

1. Confirm the intended license for GeoCeDG-authored code and documentation.
2. Reconcile the standard EUPL source header, the official component-based
   terms, and any exceptional third-party headers in every shipped source file.
3. Decide which upstream translations, icons, styles, fonts, documentation,
   and sample materials—if any—will be redistributed.
4. Inventory Gradle-resolved Java and native dependencies, including license
   texts and attribution obligations.
5. Identify every missing font license text referenced by
   `source/shared/renderer-base/LICENSE:46-59`.
6. Audit the final product name, application identifiers, screenshots, icons,
   and installer resources for trademark/asset provenance.
7. Repeat the audit for any commercial or public redistribution scenario.

No conclusion about distribution legality is made here. The matrix records
facts and the review work still required.

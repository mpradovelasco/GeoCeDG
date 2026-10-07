# PRE-G9B-R6-plus-E1-L technical candidate report

```text
PRE-G9B-R6-plus-E1-L      = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
VERIFICATION_CLASS        = BOUNDED_PHASE (frozen by the author, DQ-E1-2)
PLANNED ACCEPTANCE        = one registered PHASE PRE-G9B-R6-PLUS-E1-L on the exact candidate
AUTHOR_SMOKE              = PENDING (required)
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
PRE-G9B-R6-plus-E1-P      = PREPARED — NOT AUTHORIZED
selfApproved              = false
authorApproved            = false
passClaimed               = false
PRODUCT CHANGE            = YES (GeoCeDG Desktop application layer only)
SERIALIZATION CHANGE          = NONE (documents)
GEOMETRIC SEMANTICS CHANGE    = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE for documents; NEW application sidecar
                                <preferences>.bundled-tools-v1.json; NEW generated
                                library manifest
```

The execution contract is the
[`E1-L` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-l-curated-ggt-library.prompt.md),
under the
[E1 author decisions and E1-L authorization record](pre_g9b_r6_plus_e1_prompt_closeout_record.md)
and the [rights record version 1](../licensing/curated-ggt-library-rights-record.md).
This report preserves the pre-freeze evidence; the registered `PHASE` on the
frozen candidate is reported with the candidate, outside this file, because
this file is part of that candidate. Its machine-readable mirror is
[`pre-g9b-r6-plus-e1-l-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-l-candidate-evidence.json).

## 1. Identities and entry gate

```text
P_R6PLUS_C_X1    = 23b971f5a967958f93e745ca8c39fce1162706e6  tree 5f564c8391828dc781dee0b1be93ef223431af58
T_R6PLUS_E1_PREP = 0ce52a965bcf8b9de37a1572738c0ad79db52354  tree e500e80fddf957078249383ae61e28e6f707d455
AUTHORIZATION    = 904778b34704d1f395e70e27f265914dc3ffb500  tree 41484cb39c4d2d39628c8622fa01e018cfb8b943
BRANCH_START     = 942aebe0f9c83afb3952d62e87783e92a7125bd7  tree eaf9e828c6070078b1f8b486ec1581b760d2d796
                   (governance: the AGENTS.md §3.2 models/curated/ amendment, alone)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-e1-l-curated-ggt-library (local)
```

Entry gate, verified on 2026-10-07 after a fresh `git fetch origin`: local
`main`, `origin/main` and the live remote `main` all `23b971f5`; the worktree
clean; `C-X1` `PASS — AUTHOR APPROVED — PUBLISHED`; `Templatev7.ggb` SHA-256
`f62e5b7a…ed4113`. `AUTHORIZATION` is the child of `T_R6PLUS_E1_PREP` and
`BRANCH_START` the child of `AUTHORIZATION` with the single `AGENTS.md` line
change. Nothing was pushed.

## 2. Delta

- **Curated content (new).** `models/curated/ggt-library/`: `README.md`,
  authored `curation.yml`, generated `library-manifest.json`, generated
  `tools/<command>.ggt` for the twelve tools; `.gitattributes`
  `models/curated/**/*.ggt binary`.
- **Owned icons (new).** `geocedg/resources/icons/ggt-library/<command>.svg`
  (twelve); `geocedg/resources/assets-manifest.yml` gains the top-level array
  `ggt_library_assets` (twelve entries); status string, schema version,
  exclusions and existing entries unchanged.
- **Desktop application layer.** New `GeoCeDGBundledToolCatalog`;
  `GeoCeDGUserToolLibrary` (bundled packages, precedence, sidecar pins,
  `definitionDigests` helper, shared atomic writer); `GeoCeDGUserTools`
  (catalog wiring, read-only section in manager and menu, owned icons and
  statements). Eight `UserTools.*` texts (English and Spanish) in
  `apps/geocedg/application-profile.yml`; no action, group, toolbar or feature
  change in the profile (125 actions, unchanged).
- **Specification, decision, feature.** `geocedg/specs/legacy/curated-ggt-library.md`
  and `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md` (both
  candidates, approved only through the author closeout);
  `cedg.library.curated-ggt` in `geocedg/features/experimental.yml`
  (`experimental`, enabled by default).
- **Tooling.** `tools/legacy/curate-ggt-library.ps1 [-VerifyOnly]`.
- **Tests.** New `CuratedGgtLibraryGenerator` (test source),
  `PreG9BR6PlusE1LCuratedLibraryTest` (9), `PreG9BR6PlusE1LBundledCatalogTest`
  (10); one existing pin updated in `PreG9BP1PublicSurfaceTest` (§8).
- **Registration.** Phase selection `PRE-G9B-R6-PLUS-E1-L`, JUnit selection
  `pre-g9b-r6-plus-e1-l.desktop`, the inventory and registry pin through the
  official updater, the registry-shape pins; `docs/upstream/modified-files.yml`
  (three purposes extended, four files added).
- **Records.** This report and its mirror; roadmap and mini-track status lines.

No change under `models/legacy/**`, `source/shared/**`, `org/geogebra/**`,
build scripts, `packaging/**`, `tools/release/**`, `tools/agent/checks/**`,
`windows-packaging.md`, `NOTICE.md`, `THIRD_PARTY.md`, `component-matrix.md`
or `LICENSES/**`.

## 3. Curated library

Generated from `Templatev7.ggb` (`geogebra_macro.xml` SHA-256
`f3744e13…fc94f4`) by `CuratedGgtLibraryGenerator` v1 on the recorded Desktop
test JVM `17.0.10+7-LTS` (Amazon.com Inc.). `library-manifest.json` SHA-256
`1b304be27ea2c90ea21e824bae25e663949039f60cd772a801ffb1886c281fcb`; `curation.yml` SHA-256 `bfd9e9d1d7793f1806095eda92851cfc7a9bf81e186d8211f9aa4d25ddf97fa0`.

| Tool | Bytes | `.ggt` SHA-256 | Definition digest (v1) | Styled outputs | Icon PNG SHA-256 |
|---|---:|---|---|---|---|
| `SquarebyDiagonal` | 6478 | `96d07292989c5b939ce9fd444f8f73cb8bb54196fa1dfcae5a207c46b8077ca7` | `ffb9d85c98945eea2bb24d7dc6e934fb122dbc0902a3e042ff7cec7ea190a621` | `k`, `j`, `i`, `h` | `13e33ca6233435b42608d03faff87d1656bea7d67e63f7a639b0085741b6894b` |
| `CirclebyD` | 2986 | `fb3bee9f9e1e584b38200205ed4f7e62a5de0e86600ff14c43e15b249362d5fb` | `6867d8af9fc6c48f44ec4522e29bcf33142b70ecc46f1030b5837d1201f10115` | `g` | `0b0dd0ff730dfee13ce814119fe4b3ec1198b7037729a89538f5c14ce32402c8` |
| `circArcbyAngle` | 3760 | `b7503305388aa0b91e2febd2b83b9199829eb26f82f7c436f97d12f6dc6733ef` | `679d21e651ea2cb7f2730d6932f8d03b42e13ec73c8c66eed18661245e40a042` | `c` | `9fb468c16d20cd6d93963374ae07979c65902566ed2b63034a4ce952d5e6a698` |
| `ellipseLength12` | 3841 | `7d49518c528e6b6bcf2e0ada25accc93400417c4d960c3107eab7a5790c45ed8` | `8fb6f5c604bdba40cc72b785b618e3e2e0076c49b82924cd92b33eb0dd091ae4` | `g` | `ac1044e15c820f00b377239e4798b2066cd3154460e06d85db89875b456fd253` |
| `IFPositiveSelectPoint` | 2815 | `cff4496b0428a28e5e860656b412b5d1443c3ae3a95730c1f1a43ff940bdaf26` | `52e7975752e29eb579aca106c4b9b354e178fe7b1d60b3640752324a3b251a4f` | none | `0b14c2cdad40964cc4d355ac252aff2960f53a05607df2196cb5f2eda7780923` |
| `EllipseAxis` | 6256 | `3cb1a996474aaf52925b5cf7edfe1778b574ad3ab367b799d505e655e04a48bf` | `623ef51cac60038e6a23f69e2060f033ebf226f0a2299871e901a9549a690e33` | `e` | `fb6789cf2d607c8d98aaf859ef63af8c4051c94a2ce0a93518d8d8bb77561028` |
| `conj2mainAxesEllipse` | 14763 | `977afd80364c40203b19535dbb8abaaa4cb9c32ab7589b5de839974542734d71` | `6e00aacaef968a9f7527095898743ac55e164dbb2d1340144f7d23ca51319cb8` | none | `e74ddbc81e1e0fc458b88356013d3b7240de272243311901f08719f482600179` |
| `pointJump` | 3289 | `dcb815708f8c957833ffbe6564f10c52518fb49c01414129409ccb1f4c0680e8` | `3dd2b91567e53375493a8bfb80aa407e1232694b3d756e8c06d3073433d5bc40` | none | `4307eb0958be9e0f8fcaa9e3e98992c2ef393c0b7bad099fa8f9074fcb403766` |
| `relCoor` | 4453 | `5e7bdbf5f8736ceabf1af3ffdecabbe42b3e72b0e9bc952c5428b720c691a53e` | `ddd00d2660ab79c695c2cda06735e05e6149416476203f34731356012d9229ae` | none | `20ceb31cb4215b5473f13d020caf6e52d040d0c9cdcf85c7ebd01e2a46d9e28f` |
| `translationCoor` | 6069 | `2b3fd3787951eb3cd41fd4bafc26f3f75c84abc95041f38bb9fdb65e0c0775f0` | `bcfbc33bef0e66e6b7a270d3cb963f7d612a80b75bb973e00e384e8db3a73c70` | none | `c73ad0d9b453aa9d36418ee784f7b8088a05f63b99cb3359b7f42458cd754fe5` |
| `DuctSymbol` | 15362 | `b1146d5c75ae40f3eb5510a424d024e8435a856a752f67dfc03e2029c4def2aa` | `32a5d5132b8edac33776b2a20c51cffbca0af0a7f41c2b1073d57a48ce25699a` | `m`, `d'`, `k'`, `p'`, `l`, `f` | `6ea1d706c7dbf27a4e56d26acd343385497081a6e9cd8fc3ff6ac74739a170fe` |
| `SymmSymbol` | 6473 | `4efa9f8e7c44c40acd3d818d236ead9443afe7388b8bb4c47a0860de1a7cac89` | `e58c369bc7a3128d1aa7fc77d3fe5c0793e86bd69c07c2bf2ef74c412c017586` | `h'`, `h` | `730145e0632e9104477418afcf51399a6b789357d126bb896852b6996e091c5c` |

Every archive holds exactly `geogebra_macro.xml` and
`<md5>/geocedg-ggt-<command>.png`, `STORED`, DOS time 1980-01-01T00:00. Seven
tools have linear outputs that `T-STYLE` sets to black and `lineThickness` 3
with their line type kept; `IFPositiveSelectPoint`, `conj2mainAxesEllipse`,
`pointJump`, `relCoor` and `translationCoor` return only points or a number,
so `T-STYLE` changes nothing in them. Command names, tool names and help texts
are byte-identical to the source macros.

## 4. Four-way record per tool

| Aspect | Record (identical for the twelve tools) |
|---|---|
| Behavioral reference | the source macro in `Templatev7.ggb`; construction identical after `T-STYLE` normalization (test `e1l05`) |
| Source / provenance | source macro SHA-256 per tool in the rights record and the manifest; the author's 2024 export (name, bytes, SHA-256, writer, date) in `curation.yml`; equivalence proven by the E1 preparation |
| Asset ownership | curated definition `geocedg-software` (EUPL-1.2); owned icon `geocedg-documentation-or-ordinary-art` (CC-BY-4.0), drawn for this library, no stock or template pixels (test `e1l07`) |
| Redistribution permission | rights record version 1: `INTERNAL` and `NC` authorized, `COMMERCIAL` not authorized / pending; no packaging in `E1-L` |

Statements recorded in `curation.yml`, the manifest, the specification and the
catalog tooltips: `EllipseAxis` (validity domain); `pointJump`, `relCoor`,
`translationCoor` (no spatial-frame or projection authority).

## 5. Determinism

- The golden test `e1l02` regenerates the whole library in memory twice and
  byte-compares it with the tracked files; on any difference it writes the
  regenerated set under `source/desktop/desktop/build/geocedg-curated-ggt-library/`
  and fails with the running and recorded JVM identities.
- `tools/legacy/curate-ggt-library.ps1` produced the tracked set from that
  output and re-verified it; `-VerifyOnly` only runs the golden test.
- Rendering uses the shipped EchoSVG route at scale 1 into a fixed 32 × 32
  ARGB raster with fixed hints; no screen, DPI, theme or application state is
  read. Reproduction is claimed on the recorded JVM.
- The definition digest is the user library's own (version 1) and is
  recomputed in `e1l04`.

## 6. Runtime behavior

- `GeoCeDGBundledToolCatalog.defaultDirectory()` is
  `<parent of jpackage.app-path>/app/ggt-library`; with no `jpackage.app-path`
  (every Gradle, IDE or test launch) the catalog is empty and nothing is
  created. The derivation is unit-tested; its value in an installed launcher is
  `INFERRED` from the jpackage launcher contract and is first exercised by the
  `E1-P` packaging smoke.
- Integrity is all-or-nothing (`e1lCatalog03`: extra tool, extra top-level
  file, missing archive, hash mismatch, definition-digest mismatch, unshipped
  archive present, wrong library id, malformed manifest).
- Bundled packages pass the same `inspect` and definition-digest validation as
  user installs; user packages shadow colliding bundled commands; documents
  keep precedence (mismatch and adoption, `e1lCatalog05`).
- Bundled pins go to the sidecar only; version-1, -2 and -3 user stores stay
  byte-identical through bundled activation, pin, unpin and refresh
  (`e1lCatalog06`); a bundled-only action never creates the user store and
  dormant sidecar entries are kept (`e1lCatalog07`); an unreadable sidecar
  disables bundled pinning only (`e1lCatalog08`).
- Saving after using a bundled tool writes the macro with the owned `iconFile`
  and no icon bytes; reopening adopts the curated definition (`e1lCatalog09`).

## 7. Tests

| Prompt obligation | Test |
|---|---|
| `T-E1L-ORIGINAL` | `e1l01OriginalProvenanceSourceIsUnchangedAndOnlyRead` |
| `T-E1L-REPRO` | `e1l02LibraryRegeneratesByteForByte` |
| `T-E1L-SELECTION` | `e1l03SelectionIsExactlyTheTwelveAuthorApprovedTools` |
| `T-E1L-MANIFEST` | `e1l04ManifestMatchesFilesDigestsAndRightsRecord` |
| `T-E1L-EQUIV` | `e1l05CuratedDefinitionsDifferOnlyByIconAndStyleValues` |
| `T-E1L-STYLE` | `e1l06StylePolicyIsBlackThicknessThreeWithPreservedLineTypes` |
| `T-E1L-ASSETS` | `e1l07ArchivesCarryOnlyTheOwnedIconAndNoThirdPartyImage` |
| `T-E1L-ICONS` | `e1l08OwnedSvgSourcesAreInTheAllowedSubsetAndHashesAreRegistered` |
| `T-E1L-BOUNDARY` | `e1l09NoPackagingRouteNoBuildChangeAndUnchangedActionCatalog` |
| `T-E1L-CATALOG` | `e1lCatalog01`, `e1lCatalog02`, `e1lCatalog03` |
| `T-E1L-ACTIVATE` | `e1lCatalog02` (all twelve), `e1lCatalog04` (`CirclebyD`, `SquarebyDiagonal` outputs and style) |
| `T-E1L-PRECEDENCE` | `e1lCatalog05` |
| `T-E1L-STORE` | `e1lCatalog01`, `e1lCatalog06`, `e1lCatalog07`, `e1lCatalog08` |
| `T-E1L-ROUNDTRIP` | `e1lCatalog09` |
| presentation | `e1lCatalog10` (manager and menu section, owned icons, statements, bundled pin icon, disabled custom icon, unavailable catalog line) |

## 8. Notes and deviations

- **Existing pin moved.** `PreG9BP1PublicSurfaceTest.onlyTheTwoAuthorizedFeaturesArePromoted`
  pins the experimental feature-set size. The author decision `DQ-E1-14` adds a
  sixth, default-enabled experimental feature, so the pin moves from 4 to 5 and
  the test now asserts `cedg.library.curated-ggt = true` explicitly; it remains
  a non-stable feature.
- **Profile features list.** Unchanged, as recorded in the authorization record
  §6 (no runtime reader; validated against an approved candidate).
- **Checkstyle.** Main: no finding. Test: no finding in the new or changed
  classes; the remaining `PreG9BR6PlusA1HiddenLayerTest.java:188` is
  pre-existing (recorded by `C-X1`).
- **Escaping.** An editor converted one `\u2014` escape into a literal character
  during development; it was restored, and the three changed main classes
  contain only ASCII.

## 9. Pre-freeze evidence

| Run | Result |
|---|---|
| focal `PreG9BR6PlusE1LCuratedLibraryTest` + `PreG9BR6PlusE1LBundledCatalogTest` (final tree) | 9 + 10 tests, 0 failures/errors |
| adjacent `G9U1UserToolLibraryTest`, `G9U1MacroNativeArchivePersistenceTest`, `GeoCeDGProfileTest`, `G9U1ActionRegistryTest`, `PreG9BP1PublicSurfaceTest`, `G9U0RuntimeFeatureTest`, `G9U0LocalizationHelpTest` | 38 + 2 + 5 + 14 + 18 + 5 + 4 tests, 0 failures (P1 after the pin move of §8) |
| `tools/legacy/curate-ggt-library.ps1` (generation, then `-VerifyOnly` behaviour inside) | regenerated 13 files; golden test passed on the copied set |
| Checkstyle `:desktop:desktop:checkstyleMain`, `:desktop:desktop:checkstyleTest` (XML reports) | main: no finding; test: only the pre-existing `PreG9BR6PlusA1HiddenLayerTest.java:188` |
| `Assert-GeoCeDGUpstreamBoundary -ExpectedBaseline 9b93256b7df401ff056c37b502d82df4d72b1522` | OK, 970 registered files (966 + 4 added) |
| `tools/agent/verify-legacy.ps1` (standalone, diagnostic) | passed; `Templatev7.ggb` `f62e5b7a…ed4113`, legacy pins untouched |
| producer `discovery.shared` (`-TestDryRun`) | completed, inner exit 0; 6 008 identities, unchanged |
| producer `discovery.desktop` (`-TestDryRun`) | completed, inner exit 0; 1 842 → 1 861 identities |
| producer `pre-g9b-r6-plus-e1-l.desktop` (executed) | 96 tests, 0 failures/errors |
| producer `final.desktop` (executed, complete Desktop suite) | 1 855 cases, 0 failures/errors, 1 skip (the pre-existing allowlisted one), inner exit 0, 506 s; no `OutOfMemoryError` |
| `update-verification-junit-inventory.ps1` (in-session, absolute paths) | `pre-g9b-r6-plus-e1-l.desktop` 96 (`da710659…`), `final.desktop` 1 836 → 1 855 (`81899ab1…`), discovery Desktop 1 861 (`fa9613d8…`); shared unchanged |
| registry pin `junit_inventory` | `860d1fcd…` (reproduced from the HEAD blob) → `3a63be8b6a6ad60bc09aa89a6f9a95b1b840928f9bf9e3049e1ca466a7d3c4dd` |
| static contract `packaging.source-authority` input `geocedg/resources/assets-manifest.yml` | `c3c80f30…` (reproduced from the HEAD blob) → `b8f9860da273cea0c703d263657f02bfdab7c0d38637d854724553dd72fe249c`; registry pin `static_contracts` `e51b2f18…` → `9b733a967bccd35ec2184f9bbce35a7abc8b17c5f842864e7382ec26169314f4` (computed with `Get-VerificationCanonicalTextSha256`, precedent `B`, `D1`, `A-2`) |
| registry-shape pins (`verification-final-coverage.Tests.ps1`) | 40 selections, 47 PHASE selections, `pre-g9b-r6-plus-e1-l.desktop` = 96 |
| first development `INFRA_UNIT` on the staged tree | `REJECTED_VERIFICATION_CORE`, `verification-7c0b76e6bc2840608ab3813c93e89bf4`: `infra.static-semantic` reported the moved `assets-manifest.yml` identity before the static-contract pin was moved (development evidence only) |
| development `INFRA_UNIT` on the staged tree | `ACCEPTED / COMPLETE`, 22/22, `verification-e983c78dccd24e7daf49fd95129bc1fe` |
| development `STATIC` on the staged tree | `ACCEPTED / COMPLETE`, 3/3, `verification-22e40f637d32435ab5e3c2f32a621bca`; the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE`; other diagnostics clear |
| `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-L -PlanOnly` | `COMPLETE`, plan `f3f8c969b9b8221fdd7248c60736200958cec87a4c2f20333c97f6c759e7f572` |
| `git diff --cached --check` | clean |

`.gitattributes` also pins `models/curated/**/*.json`, `models/curated/**/*.yml`
and `geocedg/resources/icons/ggt-library/*.svg` to `text eol=lf`, so that a
`core.autocrlf=true` checkout cannot break the byte-exact golden comparison or
the LF-only icon rule.

## 10. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain, Gradle, Conda,
packaging or environment contract changes; the generator runs on the existing
Desktop test JVM with the shipped EchoSVG dependency.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: one registered PHASE selection and one Desktop JUnit selection through
the existing registry, inventory and updater; registry-shape pins follow the
R5-A/C/C-X1 precedent; no verifier, check script, schema or profile-composition
change.
GUIDE_IMPACT = NONE
GUIDE_JUSTIFICATION: the author keeps the bilingual guide work in G (DQ-E1-15);
the in-product texts of the new section are profile texts.
SERIALIZATION CHANGE = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
OUTPUT FORMAT / SCHEMA CHANGE = NONE for documents
```

## 11. Author smoke checklist

No packaged library exists before `E1-P`, so the smoke stages an
installation-like directory by hand. Nothing below changes the repository.

1. Create `C:\GeoCeDG-E1L-smoke\app\ggt-library\` and copy into it exactly
   `models/curated/ggt-library/library-manifest.json` and the folder
   `models/curated/ggt-library/tools/` (not `README.md` or `curation.yml`).
2. In a PowerShell at the repository root, set the jpackage launcher property
   for the next launch and start the product:
   `$env:JAVA_TOOL_OPTIONS='-Djpackage.app-path=C:\GeoCeDG-E1L-smoke\GeoCeDG.exe'`
   then `.\gradlew.bat :desktop:desktop:runGeoCeDG`; afterwards
   `Remove-Item Env:JAVA_TOOL_OPTIONS`.
3. Automation → User tools: a disabled "GeoCeDG library" header followed by the
   twelve tools with their icons; `pointJump` and `EllipseAxis` show their
   statements as tooltips.
4. Use `CirclebyD` (centre, diameter) and `SquarebyDiagonal` (two points): the
   linework is black at GeoGebra thickness 3; the tool names and help are the
   historical ones.
5. Open the manager: the bundled entries are labelled "GeoCeDG library", Remove
   and Icon are disabled for them; pin `EllipseAxis` and see its owned icon in
   the toolbar.
6. Save as `.cedg`, close, reopen: the construction and the tool are intact.
7. Optional: delete one file from the staged `tools` folder and restart; the
   menu shows the single "integrity" line and no bundled tool.
8. Confirm that your existing user tools still work and that the only new
   preferences file is `<preferences>.bundled-tools-v1.json`, created by the
   pin in step 5.

## 12. Changed paths

- `.gitattributes`
- `apps/geocedg/application-profile.yml`
- `docs/adr/0033-curated-ggt-library-and-distribution-boundary.md`
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`
- `docs/roadmap/geocedg_roadmap.md`
- `docs/upstream/modified-files.yml`
- `docs/validation/pre_g9b_r6_plus_e1_l_candidate_report.md`
- `geocedg/features/experimental.yml`
- `geocedg/resources/assets-manifest.yml`
- `geocedg/resources/icons/ggt-library/circArcbyAngle.svg`
- `geocedg/resources/icons/ggt-library/CirclebyD.svg`
- `geocedg/resources/icons/ggt-library/conj2mainAxesEllipse.svg`
- `geocedg/resources/icons/ggt-library/DuctSymbol.svg`
- `geocedg/resources/icons/ggt-library/EllipseAxis.svg`
- `geocedg/resources/icons/ggt-library/ellipseLength12.svg`
- `geocedg/resources/icons/ggt-library/IFPositiveSelectPoint.svg`
- `geocedg/resources/icons/ggt-library/pointJump.svg`
- `geocedg/resources/icons/ggt-library/relCoor.svg`
- `geocedg/resources/icons/ggt-library/SquarebyDiagonal.svg`
- `geocedg/resources/icons/ggt-library/SymmSymbol.svg`
- `geocedg/resources/icons/ggt-library/translationCoor.svg`
- `geocedg/specs/legacy/curated-ggt-library.md`
- `geocedg/specs/operations/verification-junit-inventory.json`
- `geocedg/specs/operations/verification-registry.json`
- `geocedg/specs/operations/verification-static-contracts.json`
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-l-candidate-evidence.json`
- `models/curated/ggt-library/curation.yml`
- `models/curated/ggt-library/library-manifest.json`
- `models/curated/ggt-library/README.md`
- `models/curated/ggt-library/tools/circArcbyAngle.ggt`
- `models/curated/ggt-library/tools/CirclebyD.ggt`
- `models/curated/ggt-library/tools/conj2mainAxesEllipse.ggt`
- `models/curated/ggt-library/tools/DuctSymbol.ggt`
- `models/curated/ggt-library/tools/EllipseAxis.ggt`
- `models/curated/ggt-library/tools/ellipseLength12.ggt`
- `models/curated/ggt-library/tools/IFPositiveSelectPoint.ggt`
- `models/curated/ggt-library/tools/pointJump.ggt`
- `models/curated/ggt-library/tools/relCoor.ggt`
- `models/curated/ggt-library/tools/SquarebyDiagonal.ggt`
- `models/curated/ggt-library/tools/SymmSymbol.ggt`
- `models/curated/ggt-library/tools/translationCoor.ggt`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGBundledToolCatalog.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserToolLibrary.java`
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGUserTools.java`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/CuratedGgtLibraryGenerator.java`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BP1PublicSurfaceTest.java`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusE1LBundledCatalogTest.java`
- `source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusE1LCuratedLibraryTest.java`
- `tools/agent/tests/verification-final-coverage.Tests.ps1`
- `tools/legacy/curate-ggt-library.ps1`

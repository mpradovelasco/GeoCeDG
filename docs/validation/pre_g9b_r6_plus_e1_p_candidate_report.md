# PRE-G9B-R6-plus-E1-P technical candidate report

```text
PRE-G9B-R6-plus-E1-P          = TECHNICAL CANDIDATE PENDING AUTHOR REVIEW
VERIFICATION_CLASS            = OPERATIONAL_VERIFICATION_INFRASTRUCTURE, impact PHASE_LOCAL
                                (frozen by the author on 2026-10-07)
PLANNED ACCEPTANCE            = PHASE PRE-G9B-R6-PLUS-E1-P, INFRA_UNIT,
                                PACKAGING -CheckToolchain, PACKAGING -VerifyPackagingArtifacts
                                for INTERNAL and for NC, technical packaging smoke;
                                no FINAL
AUTHOR_SMOKE                  = PENDING
AUTHOR_DECISION               = NOT_RECORDED_IN_THIS_ARTIFACT
PRE-G9B-R6-plus-E1            = NOT YET CLOSED
selfApproved                  = false
authorApproved                = false
passClaimed                   = false
SERIALIZATION CHANGE          = NONE
GEOMETRIC SEMANTICS CHANGE    = NONE
DOCUMENT FORMAT CHANGE        = NONE
E1-L LIBRARY CONTENT CHANGE   = NONE
PACKAGING CHANGE              = YES
```

The execution contract is the
[`E1-P` prompt](../../.github/prompts/tasks/pre-g9b-r6-plus-e1-p-ggt-library-packaging.prompt.md)
in its authorized state, under the
[E1 author decisions record](pre_g9b_r6_plus_e1_prompt_closeout_record.md)
(`DQ-E1-3`, `DQ-E1-12`, `DQ-E1-13`, `DQ-E1-16`), the
[E1-L closeout record](pre_g9b_r6_plus_e1_l_closeout_record.md) and the
[rights record version 1](../licensing/curated-ggt-library-rights-record.md).
This report preserves the pre-freeze evidence; the acceptance runs and the
technical packaging smoke on the frozen candidate are reported with the
candidate, outside this file, because this file is part of that candidate. Its
machine-readable mirror is
[`pre-g9b-r6-plus-e1-p-candidate-evidence.json`](../../geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-p-candidate-evidence.json).

No package or installer built for this report is published or distributed.
Building them locally for verification is not publication.

## 1. Identities and entry gate

```text
P_R6PLUS_E1_L         = 47b39e5f6d51e51b8d08ec5622305f11c1dab10f  tree d48c86c6f65b642a53609fb15f917bc587ff2ce7
                        (PRE-G9B-R6-plus-E1-L = PASS — AUTHOR APPROVED — PUBLISHED)
AUTHORIZATION         = 6bce8cf56f064f1e347c6362d5d6238ea1ae4782  tree 94198c3dd9a53b623c17c73997923eb48ad99581
                        (first tracked edit: the E1-P prompt in the authorized state,
                        roadmap 4.71, mini-track status line)
IMPLEMENTATION_BRANCH = phase/pre-g9b-r6-plus-e1-p-ggt-library-packaging (local)
RIGHTS RECORD         = version 1, unchanged: docs/licensing/curated-ggt-library-rights-record.md
                        blob e9f4c1189c477b2dd91b0fd77bff7a8d420a2c04; JSON mirror
                        geocedg/validation/pre-g9b-r6-plus/curated-ggt-library-rights-record.json
                        blob 6f0df1bdfccfc60cbd3245b56c4bd51ec698ed5c
```

Entry gate, verified on 2026-10-07: local `main`, `origin/main` and the live
remote `main` all `47b39e5f`; clean worktree; `E1-L` `PASS — AUTHOR APPROVED —
PUBLISHED`; the rights record blob identical to the version named by the
authorization. `AUTHORIZATION` is the only commit between the base and the
implementation. Nothing was pushed.

## 2. Contract delta

- **Builder** (`tools/release/build-windows-package.ps1`). New
  `Get-CuratedGgtLibraryPayload -ProfileId`: reads the canonical
  `models/curated/ggt-library/library-manifest.json` and the rights record JSON
  mirror; requires schema version 1, library id `geocedg.curated-ggt-library`
  and `rightsRecord.version` equal to the rights `recordVersion`; returns no
  library when the rights record does not mark the selected profile
  `AUTHORIZED`; otherwise requires, for every shipped tool, a rights entry, the
  path `tools/<command>.ggt`, the file and its exact SHA-256, and requires the
  canonical `tools/` directory to equal the shipped set. After the `legal/` copy
  and before `jpackage` it stages `ggt-library/library-manifest.json` and
  `ggt-library/tools/<command>.ggt` (app-image path `app/ggt-library/`). The
  SBOM gains one `file` component per staged tool (SHA-256, EUPL-1.2, packaging
  path, source-macro hash, rights record version, embedded owned icon CC-BY-4.0);
  `build-manifest.json` gains `ggt_library` (path, library id and version,
  manifest SHA-256, tool count, command/SHA-256 list, rights record path and
  version, admitted profile) only when the library is staged, and one more
  deliberate exclusion line. The library content is read, never rewritten.
- **Packaging product leaf** (`tools/agent/checks/packaging-product.ps1`).
  `packaging.required-files` names the canonical manifest and both rights
  records. `packaging.portable-boundary` keeps every `.ggb`, `.pdf`,
  `Templatev7.ggb` and non-Windows native forbidden everywhere, and forbids any
  `.ggt` whose app-image path is not `app/ggt-library/tools/<name>.ggt`. Four
  new subcontracts read the built profile from `build-manifest.json`
  (`distribution_profile.id`, default `INTERNAL`):
  `packaging.ggt-library.rights`, `packaging.ggt-library.membership`,
  `packaging.ggt-library.hashes`, `packaging.ggt-library.build-manifest`
  (§3). Optional `-CuratedLibraryRoot` and `-RightsRecordPath` exist for
  fixtures; the registered command line does not pass them.
- **Verification-core fixtures**
  (`tools/agent/tests/verification-contract-boundary.Tests.ps1`,
  `infra.contract-boundary`). The packaging fixture stages the real canonical
  library and its build-manifest record; one new case covers the admission
  rules (§7).
- **Specification** (`geocedg/specs/packaging/windows-packaging.md`): the
  declared library input, pipeline stage 4, the "Required exclusions"
  admission rules and profiles, and the verification subcontracts.
- **Licensing records**: `NOTICE.md` (one paragraph),
  `docs/licensing/component-matrix.md` (one row),
  `geocedg/resources/assets-manifest.yml` (the `redistribution_status` text of
  the twelve `ggt_library_assets` entries).
- **Registration**: phase selection `PRE-G9B-R6-PLUS-E1-P` =
  `packaging.product` + `infra.contract-boundary`; registry-shape pin 47 → 48
  PHASE selections; static-contract input pins of
  `tools/release/build-windows-package.ps1` and
  `geocedg/resources/assets-manifest.yml` and the registry `static_contracts`
  pin (§10).
- **Records**: this report and its mirror; roadmap and mini-track status lines.

No change under `models/**`, `geocedg/resources/icons/**`, `source/**`,
`packaging/**`, `apps/**`, `LICENSES/**`, the rights records, `THIRD_PARTY.md`,
`AGENTS.md`, the governance prompts, the verifier, the schemas or any other
registry node.

## 3. Admission contract

`INV-E1P-1`: the app-image contains a `.ggt` if and only if it is a shipped,
hash-matching member of the canonical curated library with an approved rights
disposition for the built profile, located below `app/ggt-library/`; any other
`.ggt` or any `.ggb` anywhere in the app-image fails the packaging product
leaf. Each rule is its own subcontract, so a failure names its rule:

| Rule | Subcontract | Violated when |
|---|---|---|
| location | `packaging.portable-boundary` | a `.ggt` outside `app/ggt-library/tools/`; any `.ggb`, `.pdf`, `Templatev7.ggb` or non-Windows native anywhere |
| membership | `packaging.ggt-library.membership` | the staged file set differs from `library-manifest.json` plus the shipped files (admitted profile) or is not empty (other profiles); the staged manifest differs from the canonical one |
| hash | `packaging.ggt-library.hashes` | a staged shipped tool differs from its canonical SHA-256 |
| rights / profile | `packaging.ggt-library.rights` | a shipped tool has no rights entry, the record version differs, or the library is present for a profile the rights record does not authorize |
| record | `packaging.ggt-library.build-manifest` | the `ggt_library` record is absent, incomplete or inconsistent for an admitted profile, or present for another |

GGT content is never placed or searched inside a JAR and JAR entries are not
inspected.

## 4. Profiles and COMMERCIAL exclusion

```text
PROFILE INTERNAL   = library included (rights record: AUTHORIZED)
PROFILE NC         = library included (rights record: AUTHORIZED)
PROFILE COMMERCIAL = library excluded (rights record: NOT AUTHORIZED / PENDING);
                     the profile itself fails closed before any build work
```

`INTERNAL` and `NC` stage the same functional library, byte for byte; no
geometry or tool behavior depends on the profile. COMMERCIAL exclusion has
three independent layers: the builder's existing fail-closed refusal of the
profile (exercised, §10), the builder returning no library for a profile the
rights record does not authorize, and the product leaf rejecting a COMMERCIAL
composition that contains the library (`packaging.ggt-library.rights`,
fixture case in §7) while accepting one without it.

## 5. Payload inventory (INTERNAL and NC)

Canonical manifest `models/curated/ggt-library/library-manifest.json`, SHA-256
`1b304be27ea2c90ea21e824bae25e663949039f60cd772a801ffb1886c281fcb`, library
version 1, rights record version 1. Both built profiles stage exactly these
thirteen files at `app/ggt-library/`, and the MSI carries them (decompiled
`File` rows):

| Staged file | SHA-256 |
|---|---|
| `library-manifest.json` | `1b304be27ea2c90ea21e824bae25e663949039f60cd772a801ffb1886c281fcb` |
| `tools/SquarebyDiagonal.ggt` | `96d07292989c5b939ce9fd444f8f73cb8bb54196fa1dfcae5a207c46b8077ca7` |
| `tools/CirclebyD.ggt` | `fb3bee9f9e1e584b38200205ed4f7e62a5de0e86600ff14c43e15b249362d5fb` |
| `tools/circArcbyAngle.ggt` | `b7503305388aa0b91e2febd2b83b9199829eb26f82f7c436f97d12f6dc6733ef` |
| `tools/ellipseLength12.ggt` | `7d49518c528e6b6bcf2e0ada25accc93400417c4d960c3107eab7a5790c45ed8` |
| `tools/IFPositiveSelectPoint.ggt` | `cff4496b0428a28e5e860656b412b5d1443c3ae3a95730c1f1a43ff940bdaf26` |
| `tools/EllipseAxis.ggt` | `3cb1a996474aaf52925b5cf7edfe1778b574ad3ab367b799d505e655e04a48bf` |
| `tools/conj2mainAxesEllipse.ggt` | `977afd80364c40203b19535dbb8abaaa4cb9c32ab7589b5de839974542734d71` |
| `tools/pointJump.ggt` | `dcb815708f8c957833ffbe6564f10c52518fb49c01414129409ccb1f4c0680e8` |
| `tools/relCoor.ggt` | `5e7bdbf5f8736ceabf1af3ffdecabbe42b3e72b0e9bc952c5428b720c691a53e` |
| `tools/translationCoor.ggt` | `2b3fd3787951eb3cd41fd4bafc26f3f75c84abc95041f38bb9fdb65e0c0775f0` |
| `tools/DuctSymbol.ggt` | `b1146d5c75ae40f3eb5510a424d024e8435a856a752f67dfc03e2029c4def2aa` |
| `tools/SymmSymbol.ggt` | `4efa9f8e7c44c40acd3d818d236ead9443afe7388b8bb4c47a0860de1a7cac89` |

The values equal the `E1-L` closeout identities. No other `.ggt`, no `.ggb` and
no `.pdf` exists in either app-image, ZIP or MSI file table.

## 6. Rights evidence

Rights record version 1 (unchanged) lists the twelve commands in `macros[]`
and `perMacroDisposition.profiles` = `INTERNAL: AUTHORIZED`,
`NC: AUTHORIZED`, `COMMERCIAL: NOT AUTHORIZED / PENDING`; the manifest's
`rightsRecord.version` is 1. Every shipped tool has a rights entry for both
built profiles. Licensing classes as recorded there: tool definitions
`geocedg-software` (EUPL-1.2), owned icons
`geocedg-documentation-or-ordinary-art` (CC-BY-4.0). The records updated by
`E1-P` state the profile-limited inclusion without a legal conclusion and do not
describe the package as EUPL-only; the general documents stay profile-neutral
(`packaging.marker.*` and `packaging.legal-bundle-profile` satisfied for both
profiles).

## 7. Tests

`verification-contract-boundary.Tests.ps1` case "curated GGT library admission
separates location, membership, hash and rights" builds a fresh fixture per
sub-case and runs the real `packaging-product.ps1 -RequireArtifacts`:

| Required demonstration | Sub-case | Asserted subcontracts |
|---|---|---|
| positive INTERNAL | exact twelve-tool library, profile INTERNAL | all five `SATISFIED`, exit 0 |
| positive NC | same library, profile NC | all five `SATISFIED` |
| COMMERCIAL accidental inclusion | profile COMMERCIAL with the library | `rights` `VIOLATED`, exit 1 |
| COMMERCIAL without library | profile COMMERCIAL, no library, no record | all five `SATISFIED` |
| unlisted `.ggt` | `tools/Unlisted.ggt` added | `membership` `VIOLATED`; `hashes`, `portable-boundary`, `rights` `SATISFIED` |
| hash mismatch | one byte of `CirclebyD.ggt` flipped | `hashes` `VIOLATED`; `membership`, `portable-boundary` `SATISFIED` |
| `.ggt` outside `app/ggt-library/` | `app/CirclebyD.ggt` | `portable-boundary` `VIOLATED`; `membership` `SATISFIED` |
| listed but `shipped=false` | canonical override marks `relCoor` unshipped, staged tools unchanged | `membership` `VIOLATED`; `rights` `SATISFIED` |
| tool without rights for the profile | rights override without `pointJump` | `rights` `VIOLATED`; `membership`, `hashes` `SATISFIED` |
| any `.ggb` | `app/ggt-library/tools/model.ggb` | `portable-boundary` `VIOLATED` |
| forbidden PDF reference payload | `app/reference.pdf` | `portable-boundary` `VIOLATED` |
| admitted build missing the library | library removed, profile INTERNAL | `membership` `VIOLATED` |

The existing `.pdf`/`.ggb` boundary assertions of the packaging projection case
are unchanged. The real builds (§10) are the positive evidence on actual
artifacts.

## 8. Technical packaging smoke

Agent technical evidence, not `AUTHOR_SMOKE`. The smoke runs the **unmodified**
built app-image through its own `GeoCeDG.exe` with an isolated
`--settingsfile` (never the author's `%APPDATA%\GeoCeDG`), and observes the
real product through a scratch Java agent injected with `JAVA_TOOL_OPTIONS`
(outside the repository). Two launches:

1. **first**: the launcher's `jpackage.app-path` resolves the bundled catalog to
   `<app-image>/app/ggt-library`; the real "User tools" menu shows the
   disabled "GeoCeDG library" header followed by exactly the twelve tools in
   manifest order, each with its owned icon and its bilingual-profile hover
   tip; the catalog reports no failure and no user packages; each embedded icon
   entry and PNG SHA-256 equal the manifest; the real `SquarebyDiagonal` menu
   item activates the macro mode; two clicks on the Graphics view build the
   square (four black thickness-3 segments and two vertices, all from
   `AlgoMacro`); pinning `EllipseAxis` writes only
   `<settings>.bundled-tools-v1.json` and never the user store; the document is
   saved as `.cedg` through the product save route;
2. **second**: the launcher reopens that document from its command line; the
   construction is intact (six macro outputs, styling kept), the document
   carries the curated macro with its owned `iconFile`, and activating the
   bundled tool adopts the document macro instead of duplicating it.

Both launches leave the app-image tree byte-identical (whole-tree digest) and
the bundled directory unchanged. Development run on the NC worktree build:
both phases exit 0, all facts as above.

**Install / uninstall.** Not executed by the agent. The governing smoke rule
(`AGENTS.md`: "packaging smoke test when packaging changes") has no install or
uninstall requirement, and the prompt allows the portable route. Running the
MSI would also replace the author's installed GeoCeDG (same upgrade code).
Structural evidence instead: the MSI installs per user into
`INSTALLDIR = %LOCALAPPDATA%\GeoCeDG`, and its only uninstall-time
`RemoveFolderEx` targets `[INSTALLDIR]`; the preferences file, the user store
`<preferences>.user-tools-v1.json` and the bundled-pin sidecar
`<preferences>.bundled-tools-v1.json` live under `%APPDATA%\GeoCeDG\5.4\`
(`GeoCeDG.getDefaultPreferencesFile`), a different tree. This packaging
behavior is unchanged by `E1-P`.

## 9. Observations

- **OBS-E1P-SMOKE-READER-LOG.** The launched product writes screen-reader
  announcements ("Reading text: Point A = …") to stderr with the `ERROR` log
  prefix when a point is created. Pre-existing logging behavior, unrelated to
  packaging; no action proposed by `E1-P`.
- **OBS-E1P-USERSTORE-LOCK.** Opening the user-tool library creates the empty
  lock file `<preferences>.user-tools-v1.json.lock` beside the preferences even
  when no user store exists. Pre-existing `G9U1`/`E1-L` behavior, outside the
  installation directory; the user store itself is not created.
- **OBS-E1P-SOURCE-REVISION.** `build-manifest.json` records `source_revision`
  = `HEAD` without a dirty-tree flag, so a worktree build carries the parent
  commit's identity. Pre-freeze builds are therefore development evidence only;
  the acceptance sets are rebuilt from the clean frozen candidate.
- **Working directory.** One development INTERNAL build failed at Gradle
  configuration because the invoking shell's working directory was inside the
  output root; rerun from the repository root it passed. Operator error, not a
  builder defect.
- **`THIRD_PARTY.md` unchanged.** The register covers third-party components;
  the curated library is author-owned GeoCeDG material recorded in `NOTICE.md`,
  the component matrix and the asset manifest. `LICENSES/manifest.json`
  unchanged: no licence text was added.

## 10. Pre-freeze evidence

| Run | Result |
|---|---|
| `verification-contract-boundary.Tests.ps1` (direct) | 6 cases, 63 assertions, 0 failures |
| `verification-final-coverage.Tests.ps1` (direct) | 8 cases, 384 assertions, 0 failures (48 PHASE selections) |
| `packaging-product.ps1` without artifacts | `CONTRACT_SATISFIED` |
| development `STATIC` on the worktree | `ACCEPTED / COMPLETE`, 3/3, `verification-b1d4dbbdf2fd48d5aa35713b9f1122b7`; the standing governance `DIAGNOSTIC_FINDING` and historical-consistency `DIAGNOSTIC_UNAVAILABLE` |
| development `INFRA_UNIT` on the worktree | `ACCEPTED / COMPLETE`, 22/22, `verification-08a8b5cb21de47549362d2c90383c7e5` |
| development NC build `-Target All` | exit 0; twelve tools staged; ZIP, MSI, EXE, SBOM (12 library components), build manifest |
| `packaging-product.ps1 -RequireArtifacts` on the development NC set | `CONTRACT_SATISFIED`, 42/42 subcontracts |
| development NC MSI decompile (WiX 5.0.2) | 432 `File` rows (= app-image files), exactly the twelve `.ggt` |
| development technical smoke on the NC app-image | both launches exit 0; app-image unchanged |
| development INTERNAL build `-Target All` | exit 0; twelve tools staged; staged library byte-identical to the NC one (13 files) |
| `packaging-product.ps1 -RequireArtifacts` on the development INTERNAL set | `CONTRACT_SATISFIED`, 42/42 subcontracts |
| development INTERNAL MSI decompile | 432 `File` rows, exactly the twelve `.ggt` and one `library-manifest.json`, no `.ggb`/`.pdf` |
| development COMMERCIAL attempt `-Target All -DistributionProfile COMMERCIAL` | exit 1 after 0.5 s: "Distribution profile COMMERCIAL is not authorized…"; output root unchanged (same file list, sizes and timestamps) |
| `STATIC` on the final staged tree | `ACCEPTED / COMPLETE`, 3/3, `verification-de6e7b954bd346b2b0873b728cb24300` (same standing diagnostics) |
| `INFRA_UNIT` on the final staged tree | `ACCEPTED / COMPLETE`, 22/22, `verification-c4ad7ff5c5844bda99a5ce4e16b6c9ba` |
| `verify.ps1 -Profile PHASE -Phase PRE-G9B-R6-plus-E1-P -PlanOnly` | `COMPLETE`, plan `175dbb6b3c7f87b800133d8ea170119e9126d50602f7602234141bf09799eee6` (`infra.contract-boundary`, `packaging.repository-baseline.producer`, `packaging.product`) |
| `git diff --cached --check` | clean |
| static contract `packaging.source-authority` inputs | `tools/release/build-windows-package.ps1` `2b158b0f…` → `0743edff8cb99c378c450287e0f086f4476d34f76973f28614d37b73e3dc785d`; `geocedg/resources/assets-manifest.yml` `b8f9860d…` → `d8375884a3ecb29793744e653b13e09c5a1c1b4fc340789e1da52e24ed6b9f58` (old values reproduced from the HEAD blobs); registry pin `static_contracts` `9b733a96…` → `31fa0b3b04b77076e7999d601d463e09f06c6f3f5838302dfe036fee95cd1947` (`Get-VerificationCanonicalTextSha256`) |

## 11. Impact statements

```text
BOOTSTRAP IMPACT — NO CHANGE REQUIRED
Rationale: no workstation prerequisite, runtime, toolchain or environment
contract changes; the builder uses the existing JDK 25 jpackage and WiX 5.0.2.
VERIFICATION_INFRASTRUCTURE_IMPACT = PHASE_LOCAL
Rationale: the packaging product leaf gains four subcontracts and a tighter
location rule for its own artifacts; one fixture case in an existing leaf; one
registered PHASE selection; registry-shape and static-contract pins. No verifier,
supervisor, schema, profile composition or other leaf changes.
GUIDE_IMPACT = NONE
SERIALIZATION CHANGE = NONE
GEOMETRIC SEMANTICS CHANGE = NONE
DOCUMENT FORMAT CHANGE = NONE
E1-L LIBRARY CONTENT CHANGE = NONE
PACKAGING CHANGE = YES
```

## 12. Author smoke checklist

Use a package built from the frozen candidate (the agent's sets stay local and
unpublished; rebuild with the commands below if preferred).

1. `tools/release/build-windows-package.ps1 -Target All -DistributionProfile NC`
   (or `INTERNAL`) from the repository root.
2. Portable route: extract the ZIP anywhere and start `GeoCeDG\GeoCeDG.exe`; or
   install the MSI/EXE (this replaces an installed GeoCeDG 1.0.0).
3. Automation → User tools: the disabled "GeoCeDG library" header and exactly
   the twelve tools with their owned icons and hover tips.
4. Use one tool (for example `SquarebyDiagonal`: two points), save as `.cedg`,
   close, reopen: construction and tool intact.
5. Pin one library tool from the manager and see it in the toolbar.
6. If installed: uninstall and confirm that `%APPDATA%\GeoCeDG\5.4\` still holds
   your preferences, user tools and `*.bundled-tools-v1.json`.
7. Optionally build `-DistributionProfile COMMERCIAL`: it must refuse before any
   build work.

## 13. Changed paths

- `NOTICE.md`
- `docs/architecture/pre_g9b_r6_plus_minitrack_plan.md`
- `docs/licensing/component-matrix.md`
- `docs/roadmap/geocedg_roadmap.md`
- `docs/validation/pre_g9b_r6_plus_e1_p_candidate_report.md`
- `geocedg/resources/assets-manifest.yml`
- `geocedg/specs/operations/verification-registry.json`
- `geocedg/specs/operations/verification-static-contracts.json`
- `geocedg/specs/packaging/windows-packaging.md`
- `geocedg/validation/pre-g9b-r6-plus/pre-g9b-r6-plus-e1-p-candidate-evidence.json`
- `tools/agent/checks/packaging-product.ps1`
- `tools/agent/tests/verification-contract-boundary.Tests.ps1`
- `tools/agent/tests/verification-final-coverage.Tests.ps1`
- `tools/release/build-windows-package.ps1`

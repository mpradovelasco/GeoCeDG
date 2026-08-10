# G4 standalone packaging and installer report

- Date: 2026-08-10
- Branch: `feature/g4-geocedg-installer`
- Baseline: GeoGebra 5.4.928.0,
  `9b93256b7df401ff056c37b502d82df4d72b1522`
- Package profile version: `0.4.0` (internal G4 technical identifier)
- Distribution marker: `INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`

## Final status

**PACKAGING TECHNICAL STATUS = PASS**

**PUBLIC REDISTRIBUTION STATUS = BLOCKED PENDING LICENSE/ASSET APPROVAL**

G4 proves that GeoCeDG can be built as a self-contained Windows app-image,
portable ZIP, MSI, and EXE; installed, launched without an external Java
runtime, associated with `.ggb`, and uninstalled cleanly. It does not authorize
publication of any generated binary.

## Scope and architectural analysis

The baseline Desktop project already applies Gradle's `application` plugin and
provides `installDist`, but its generated start scripts select
`org.geogebra.desktop.GeoGebra3D`. G2 added the explicit sustainable launcher
`org.geocedg.desktop.GeoCeDG` and the required Java 25 toolchain/JVM options.
No upstream `jpackage` task or approved installer resource existed.

ADR 0004 therefore keeps packaging outside `source/` and the upstream Gradle
build. `tools/release/build-windows-package.ps1` invokes the repository wrapper
for `:desktop:desktop:installDist`, consumes only its `lib/` directory, filters
non-Windows native JARs, and selects the G2 launcher directly. The inherited
Classic start scripts are never package inputs. This is the smallest layout
that reuses the resolved application distribution without coupling release
logic to upstream build files.

No geometric behavior, kernel code, `Locus`, serialization, G2 toolbar, G3
Laboratory behavior, or upstream source/build file was changed.

## Durable files

Created:

- `docs/adr/0004-standalone-windows-packaging.md`;
- `geocedg/specs/packaging/windows-packaging.md`;
- `geocedg/specs/operations/package-profile.schema.json`;
- `packaging/windows/package.yml`, package metadata, restricted
  `NuGet.Config`, and the internal-evaluation notice;
- `tools/release/build-windows-package.ps1`;
- `tools/agent/verify-packaging.ps1`;
- `LICENSE`, `LICENSES/README.md`, `NOTICE.md`, `THIRD_PARTY.md`;
- `geocedg/resources/assets-manifest.yml`;
- the G4 canonical task prompt and this report.

Updated:

- composed/operational verification and the stable infrastructure manifest;
- Windows bootstrap prerequisite detection and opt-in installation;
- root README, user guide, and component/license matrix;
- legacy-ingest text normalization and the derived generator hash, so the
  existing G3 inventory remains reproducible across LF/CRLF worktrees;
- packaging-verifier cleanup, so Gradle toolchain discovery removes only the
  regenerable outputs it created and preserves pre-existing user outputs.

There are no changes below `source/`.

## Pipeline design

The package profile is JSON-compatible YAML under ADR 0002. It pins:

- entry point `org.geocedg.desktop.GeoCeDG`;
- application version `0.4.0` and stable upgrade UUID;
- Gradle Java 22, Desktop/jpackage Java 25;
- WiX 5.0.2 plus Util/UI extensions 5.0.2;
- the four required Desktop JVM options;
- installer-only `.ggb` association;
- all four output kinds and the public-release blocker.

One build performs:

1. `installDist` through `gradlew.bat`;
2. isolated staging of 52 runtime JARs;
3. exclusion of six Linux/macOS native JARs;
4. addition of internal/legal-status records;
5. JDK 25 `jpackage app-image`;
6. normalized-order/fixed-timestamp ZIP creation;
7. MSI and EXE creation from the same image through WiX;
8. CycloneDX 1.5 JAR/hash SBOM, build manifest, app-image file hashes, and
   artifact SHA-256 list.

Generated evidence is ignored below `artifacts/packaging/windows/` and is not
source authority. Exact output hashes are always recorded. The portable ZIP
normalizes entry ordering and timestamps, but a consecutive-build check found
that JDK 25 `jlink` can vary the content of `runtime/lib/modules`; end-to-end
byte identity is therefore not claimed for ZIP, MSI, or EXE.

## Workstation toolchain

Existing components were inspected before installation:

- Gradle launcher: Oracle JDK 22.0.2;
- Desktop/package JDK: Eclipse Temurin 25.0.4+7-LTS at the Gradle toolchain
  location;
- `jpackage`: 25.0.4;
- .NET SDK: 8.0.303, with .NET 6.0.36 and 8.0.7 runtimes;
- no global .NET tools and no usable WiX installation.

No .NET SDK, Visual Studio, workload, Gradle, or JDK was installed. The pinned
WiX CLI was installed with:

```powershell
dotnet tool install --global wix --version 5.0.2 `
  --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources
```

Effective WiX version: `5.0.2+aa65968c`. The first .NET CLI invocation executed
Microsoft's normal first-run initialization, including its developer HTTPS
certificate setup; no project or global NuGet source was changed.

JDK 25 `jpackage` requires the official WiX Util/UI extensions. They were
installed from `packaging/windows`, whose local `NuGet.Config` clears other
feeds, permits only nuget.org, and maps only `WixToolset.*` packages:

```powershell
wix extension add -g WixToolset.Util.wixext/5.0.2
wix extension add -g WixToolset.UI.wixext/5.0.2
```

The bootstrap detects all versions. Its default remains detection-only.
`-InstallPackagingPrerequisites` is explicit, idempotent, and limited to a
missing compatible .NET 8 SDK, WiX 5.0.2, and these two extensions. It never
installs a JDK or modifies persistent PATH/Git configuration.

## Package composition and licensing

The app image contains the selected Desktop runtime JARs, linked Java runtime,
internal notice, and legal-status records. Automated inspection confirmed that
it contains no PDF, `.ggb`, `.ggt`, `Templatev7.ggb`, legacy model, repository
knowledge document, or Linux/macOS native JAR.

No upstream installer, logo, or explicit branding icon was supplied. The
launcher uses textual GeoCeDG identity and the JDK's provisional default icon.
The runtime JARs still embed inherited translations, UI images/styles, fonts,
and third-party/native resources. Their rights and required notices are not
fully classified.

The new root `LICENSE` is deliberately a no-grant status notice, not an
invented GeoCeDG license. `LICENSES/` is an incomplete destination contract.
The generated SBOM provides exact JAR/hash evidence but does not infer license
terms. These conditions are why public redistribution remains blocked despite
the technical PASS.

## Validation evidence

| Validation | Result | Evidence |
|---|---|---|
| Package toolchain | PASS | Temurin/jpackage 25.0.4; .NET SDK 8.0.303; WiX 5.0.2 and Util/UI 5.0.2 |
| `build-windows-package.ps1 -Target All` | PASS | app-image, 88,892,208-byte ZIP, 82,268,652-byte MSI, 82,842,112-byte EXE, SBOM/manifests/hashes |
| Focused artifact gate | PASS | `verify-packaging.ps1 -CheckToolchain -RequireArtifacts`, exit 0 |
| Bootstrap idempotency | PASS | two consecutive `bootstrap-windows.ps1 -SkipFetch -SkipBuild` executions, exit 0; both `PASS WITH WARNINGS` solely for the explicitly skipped fetch/build |
| app-image launcher | PASS | launcher child was responsive with window title `GeoCeDG`; bundled `runtime/bin/server/jvm.dll`; controlled close; no residual process |
| MSI install | PASS | `msiexec /i ... /qn /norestart`, exit 0; log `%TEMP%\geocedg-g4-msi-install.log` |
| Installed launch | PASS | installed `GeoCeDG.exe` opened a responsive `GeoCeDG` window using the installed bundled JVM |
| `.ggb` association | PASS | effective association changed from existing `GeoGebra.File` to the generated GeoCeDG ProgID and command during install |
| MSI uninstall | PASS | `msiexec /x ... /qn /norestart`, exit 0; log `%TEMP%\geocedg-g4-msi-uninstall.log` |
| Post-uninstall state | PASS | install directory/ARP entry/HKCU association removed; original `GeoGebra.File` association restored; zero GeoCeDG processes |
| Forbidden package content | PASS | no PDFs, Templatev7, `.ggb`/`.ggt`, knowledge corpus, or non-Windows natives |
| ZIP reproducibility characterization | PASS WITH DOCUMENTED VARIATION | equal size (88,892,208 bytes) and identical 375/376 app-image files; only `runtime/lib/modules` varied, producing ZIP SHA-256 `7b30ddd9940340016112a34f0e3aedf79362ad87aec08277a4fc7ea53e842eac` versus `ab5a6aaf12a4caea951c8d37f484264f3a1791a4e98299287ad4ab09d4d96363`; per-build hashes remain authoritative |
| Composed authority | PASS | `verify.ps1 -RunBenchmarks -VerifyPackagingArtifacts`, exit 0; shared/Desktop compilation, 3 shared profile tests, 4 Desktop profile tests, schemas/manifests, G3 and benchmark passed |
| Composed logs | PASS | `%TEMP%\geocedg-g4-final-verify`; shared compile 27 s, Desktop compile 61 s, shared profile tests 99 s, Desktop profile tests 76 s |
| Repository integrity | PASS | composed initial/final status comparison and `git diff --check`, exit 0 |
| Final cleanup audit | PASS | zero Gradle output directories, zero relevant residual processes, no installed GeoCeDG directory, generated packages removed, and `.ggb` restored to the pre-existing `GeoGebra.File` association |

The first restricted-sandbox `installDist --rerun-tasks` reproduced the G0
Windows worker-access symptom (`renderer-base` could not read local compiled
classes). The same unmodified command passed under the managed host boundary
in 65 seconds with 41 executed tasks. It is classified as environment/sandbox
evidence, not a baseline or source regression; no Gradle workaround was added.

All regenerable Gradle directories created by the composed authority were
removed by their owning verifiers. The validated package outputs were then
removed under the repository artifact policy. Their sizes and validation
results are recorded here, the artifacts and hashes can be regenerated, and
the detailed install/uninstall logs remain outside the repository. No GeoCeDG,
GeoGebra, or Gradle process remained after validation.

## Limitations and pending work

- Windows x64 is the only validated package platform.
- Package version `0.4.0` is an internal G4 identifier, not a public release.
- Final GeoCeDG icon, translations, style, signing certificate, upgrade policy,
  public release channel, and packaging localization remain pending.
- EXE generation was validated; MSI was selected for the reversible installed
  smoke test, so a separate EXE install/uninstall pass is not required by G4.
- SBOM license fields remain intentionally incomplete pending component review.
- Equivalent app-image/ZIP builds are not guaranteed byte-identical because
  JDK 25 `jlink` may vary `runtime/lib/modules`; use the emitted hash files for
  exact artifact identity.
- Public/commercial redistribution needs a fresh human legal/asset/trademark
  decision even after all technical checks pass.

G5 was not started.

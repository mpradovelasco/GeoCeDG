# GeoCeDG operations manual — workstation, build, packaging and references

This document holds the workstation, clone, build, packaging, installation,
application-identification, developer quick-reference and technical-reference
material that previously lived in `docs/user/geocedg_user_guide.md`. It was
relocated unchanged when the user-facing product documentation moved to the two
official bilingual user guides.

It is operational and developer-facing. It is not a source of geometric,
architectural or verification authority: the normative sources remain
`AGENTS.md`, `geocedg/specs/`, the accepted ADRs and the living roadmap.

Product behaviour for a user is documented in:

- [GeoCeDG user guide (English)](../user/geocedg_user_guide_en.md)
- [Guía de usuario de GeoCeDG (español)](../user/geocedg_user_guide_es.md)

Implementation internals remain in the
[developer guide](geocedg_developer_guide.md) and the
[agent prompt guide](geocedg_agent_prompt_guide.md). Phase status, gates and
authorization remain in the [living roadmap](../roadmap/geocedg_roadmap.md).

## 0. Run GeoCeDG now

From an already prepared clone, open PowerShell 7.2 or newer at the repository root and
run:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
```


## 1. Workstation requirements

The validated workstation profile is:

| Requirement | Current contract |
|---|---|
| Operating system | Windows 11 is validated; Linux and macOS are not currently claimed as validated |
| Git | Git for Windows, available as `git` |
| Shell | PowerShell 7.2 or newer, available as `pwsh`; the floor supports redirected native-stderr handling |
| Gradle launcher JVM | JDK 22 selected by the wrapper: `JAVA_HOME` takes precedence over `PATH`; the validated installation was Oracle JDK 22.0.2 |
| Compiler/test toolchain | A full JDK 17 discoverable by Gradle, including `java` and `javac` |
| Desktop toolchain | A full JDK 25 discoverable by Gradle; the validated runtime was Eclipse Temurin 25.0.4 |
| Numerical references | Conda with the named environment `cedg_env`, CPython **3.12.13** and mpmath **1.4.1**, with interpreter and import origins inside that environment |
| Gradle | Use only `gradlew.bat` from the repository; do not install or invoke a system Gradle |
| G4 packaging | JDK 25 `jpackage`; MSI/EXE additionally require .NET SDK 6+ and WiX 5.0.2 with Util/UI extensions 5.0.2 |
| Network | Required by the normal first bootstrap to fetch `origin`, `upstream`, and tags |

The shared Java convention uses JDK 17 for compilation and tests; the Desktop
runtime task requests Java 25, while Gradle itself is launched with Java 22.
These are distinct roles, not interchangeable version requirements. Automatic
toolchain download is disabled by the normal verification path. The bootstrap
detects prerequisites; it does not install Java or create the Conda environment.

The wrapper uses `JAVA_HOME` when it is nonempty and falls back to `PATH` only
when it is empty or absent. An invalid `JAVA_HOME` is an error, even if a valid
Java is available on `PATH`. Bootstrap also requires a `java` command on `PATH`
for its separate baseline diagnostic; that diagnostic does not identify the
wrapper-selected launcher. Numerical checks use `conda run -n cedg_env`, not an
activated or global Python interpreter; preflight checks the exact versions
and executable/import origins.

Conda is operational Python verification infrastructure, not a Java runtime
dependency. Java compilation and product execution remain independent of Conda
when Python verification is not requested. From the repository root, create the
exact environment for the first time with:

```powershell
conda env create --file .\cedg_env.yml
```

Update an existing environment and prune packages outside the manifest with:

```powershell
conda env update --name cedg_env --file .\cedg_env.yml --prune
```

Verify the implementation, exact versions and import origins with:

```powershell
conda run --no-capture-output -n cedg_env python -c "import json,platform,sys,mpmath; print(json.dumps({'implementation':platform.python_implementation(),'python':platform.python_version(),'executable':sys.executable,'prefix':sys.prefix,'mpmath':mpmath.__version__,'mpmath_file':mpmath.__file__}))"
```

The result must report exactly CPython `3.12.13` and mpmath `1.4.1`.
`executable`, `prefix` and `mpmath_file` must resolve inside the same
`cedg_env` prefix. The canonical live post-installation check is:

```powershell
.\tools\agent\verify.ps1 -Profile WORKSTATION
```

The retained compatibility command is:

```powershell
.\tools\agent\verify-workstation.ps1
```

Both commands resolve to the same live plan and contract result. WORKSTATION
checks Gradle Wrapper 9.4.1, launcher Java 22, complete JDKs 17 and 25,
`cedg_env`, CPython 3.12.13, mpmath 1.4.1 and executable/import-prefix
provenance. It does not compile GeoCeDG or run fixtures or governance
diagnostics.

WORKSTATION discovers those capabilities on the current machine. A different
user, JDK location, valid `cedg_env` prefix, `TEMP` root, cache or
`GRADLE_USER_HOME` does not change acceptance. These values remain visible as
traceability observations, while only portable capabilities, exact versions
and provenance participate in compatibility identity. When
`GRADLE_USER_HOME` is unset, Gradle's normal user-home default is used.

Earlier instructions used the external `om_env`. Create and use `cedg_env`; do
not rename, update, prune, or remove `om_env` as part of this migration.

If `cedg_env` does not satisfy the contract, never remove it solely because its
name matches. Resolve and validate its name, absolute prefix, Python prefix,
executable origin, Conda inventory membership and separation from `base` first:

```powershell
$probeJson = conda run -n cedg_env python -c `
  "import json,os,sys; print(json.dumps({'environment_name':os.environ.get('CONDA_DEFAULT_ENV',''),'environment_prefix':os.environ.get('CONDA_PREFIX',''),'python_prefix':sys.prefix,'python_executable':sys.executable}))"
if ($LASTEXITCODE -ne 0) {
    throw "Cannot resolve cedg_env safely; no environment will be removed."
}

$facts = $probeJson | ConvertFrom-Json
$rawEnvironmentPrefix = [string]$facts.environment_prefix
$rawPythonPrefix = [string]$facts.python_prefix
$rawPythonExecutable = [string]$facts.python_executable
if ([string]::IsNullOrWhiteSpace($rawEnvironmentPrefix) -or
    [string]::IsNullOrWhiteSpace($rawPythonPrefix) -or
    [string]::IsNullOrWhiteSpace($rawPythonExecutable) -or
    -not [IO.Path]::IsPathFullyQualified($rawEnvironmentPrefix) -or
    -not [IO.Path]::IsPathFullyQualified($rawPythonPrefix) -or
    -not [IO.Path]::IsPathFullyQualified($rawPythonExecutable)) {
    throw "Probe did not return absolute paths; no environment will be removed."
}
$resolvedPrefix = [IO.Path]::GetFullPath(
    $rawEnvironmentPrefix).TrimEnd('\', '/')
$pythonPrefix = [IO.Path]::GetFullPath(
    $rawPythonPrefix).TrimEnd('\', '/')
$pythonExecutable = [IO.Path]::GetFullPath(
    $rawPythonExecutable)
$basePrefix = [IO.Path]::GetFullPath(
    ((conda info --base).Trim())).TrimEnd('\', '/')
$condaInventory = conda env list --json | ConvertFrom-Json -AsHashtable
$knownPrefixes = @(
    $condaInventory['envs'] |
        ForEach-Object {
            [IO.Path]::GetFullPath([string]$_).TrimEnd('\', '/')
        }
)
$prefixBoundary = $resolvedPrefix + [IO.Path]::DirectorySeparatorChar
$listedExactlyOnce = @($knownPrefixes | Where-Object {
    $_.Equals($resolvedPrefix, [StringComparison]::OrdinalIgnoreCase)
}).Count -eq 1
$validIdentity =
    $facts.environment_name -ceq 'cedg_env' -and
    $pythonPrefix.Equals($resolvedPrefix,
        [StringComparison]::OrdinalIgnoreCase) -and
    $pythonExecutable.StartsWith($prefixBoundary,
        [StringComparison]::OrdinalIgnoreCase) -and
    $listedExactlyOnce -and
    -not $resolvedPrefix.Equals($basePrefix,
        [StringComparison]::OrdinalIgnoreCase)
if (-not $validIdentity) {
    throw "Resolved cedg_env identity or origin is inconsistent; no environment will be removed."
}

Write-Host "Verified cedg_env prefix: $resolvedPrefix"
conda env remove --prefix $resolvedPrefix --yes
if ($LASTEXITCODE -ne 0) {
    throw "Removal of the verified cedg_env prefix failed."
}
conda env create --file .\cedg_env.yml
```

If the identity probe cannot run, stop and inspect the Conda inventory manually;
do not guess a prefix and do not remove another environment.

Bootstrap is a separate preparation/preflight operation, not the live
installation verifier. It never installs Git, PowerShell, Java, Gradle, Conda,
or Python. By default it only inspects prerequisites. The explicit
`-InstallPackagingPrerequisites` option may install only a missing .NET 8 SDK
and pinned WiX 5.0.2 plus required extensions. It does not
modify global environment variables, global Git configuration, credentials,
`origin`, branches, or history.

This option is an action mode, not an acceptance gate. It delegates immediately
to `tools/bootstrap/install-packaging-prerequisites.ps1` and exits without
fetching remotes, running Gradle, or executing G3/G5/frontend/repository
verification. Run `tools/agent/verify.ps1` with an explicit profile separately
when verification is required.

The typed verifier reports exact commit/tree and profile results. Diagnostic
findings remain separate from acceptance; incomplete profiles fail explicitly.

### Packaging prerequisite checks

Run these commands from the repository root to characterize the workstation:

```powershell
java -version
.\gradlew.bat --version
.\gradlew.bat -q javaToolchains
dotnet --info
wix --version
wix extension list -g
.\tools\agent\verify-packaging.ps1 -CheckToolchain
```

The Java toolchain inventory must contain a full JDK 25. Its reported
`Location` must contain `bin\jpackage.exe`; verify it directly when needed:

```powershell
$jdk25 = "<Location reported for the Java 25 toolchain>"
& (Join-Path $jdk25 "bin\jpackage.exe") --version
```

There is no repository-approved automatic JDK installation command. Install a
full JDK 25 manually and make it discoverable by Gradle. For the remaining
packaging prerequisites, the validated recommended commands are:

```powershell
winget install --id Microsoft.DotNet.SDK.8 --exact
dotnet tool install --global wix --version 5.0.2 `
  --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources
Push-Location .\packaging\windows
wix extension add -g WixToolset.Util.wixext/5.0.2
wix extension add -g WixToolset.UI.wixext/5.0.2
Pop-Location
```

If WiX is already installed globally at a different version, replace
`tool install` with the pinned update command:

```powershell
dotnet tool update --global wix --version 5.0.2 `
  --add-source https://api.nuget.org/v3/index.json --ignore-failed-sources
```

Alternatively, opt in to the idempotent repository orchestration:

```powershell
.\tools\bootstrap\bootstrap-windows.ps1 -InstallPackagingPrerequisites
.\tools\agent\verify-packaging.ps1 -CheckToolchain
```

The first command installs only approved missing .NET/WiX components and never
installs a JDK. The second command is the focused, read-only confirmation that
Gradle can resolve JDK 25 `jpackage` and that .NET, WiX, and both pinned
extensions are usable together. Neither command replaces the composed
repository acceptance authority. See the compact
[Windows packaging prerequisites](../../README.md#requisitos-de-packaging-windows)
and the [packaging contract](../../geocedg/specs/packaging/windows-packaging.md)
for the authoritative boundary.

## 2. Clone update and prerequisite preparation

For a new checkout, clone the repository and enter it. For an existing clone,
the normal path is the status/switch/fast-forward sequence shown above.
Bootstrap is optional prerequisite preparation and remote inspection:

```powershell
.\tools\bootstrap\bootstrap-windows.ps1
```

It does not compile GeoCeDG and does not launch product acceptance, governance
diagnostics or fixtures. `-SkipBuild` is retained only for command
compatibility; it does not change that separation. `-SkipFetch` avoids remote
fetching. The explicit `-InstallPackagingPrerequisites` action may prepare
only its existing .NET/WiX packaging scope; bootstrap never mutates
`cedg_env`, `om_env` or JDK installations.

Use `WORKSTATION`, not bootstrap, to verify the live installation.

## 3. Verify build and run

Run independent operations explicitly from the repository root:

```powershell
# Live installation only
.\tools\agent\verify.ps1 -Profile WORKSTATION

# Compatibility spelling for the identical live plan
.\tools\agent\verify-workstation.ps1

# Product compilation
.\gradlew.bat :desktop:desktop:compileJava

# GeoCeDG execution
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

Java compilation and execution do not require Conda. The canonical verifier also
exposes `STATIC`, `INFRA_UNIT`, `OPERATIONAL`, `DEV`, `PHASE`,
`INTEGRATION` and `FINAL` profiles. Legacy `COMPOSED` maps to
`INTEGRATION`; `FULL` and `FullTests` map to `FINAL`. A profile whose
pure semantic coverage is incomplete fails explicitly and never falls back to a
mixed wrapper.

Acceptance results are separate from governance, documentation, historical,
style and duration diagnostics. Diagnostic findings remain visible but do not
change product acceptance, coverage or exit code. A technical acceptance result
does not confer author approval or publication permission.

## 4. Package and install for internal evaluation

Development execution remains `runGeoCeDG`; it recompiles/runs from the
checkout and uses a workstation Java toolchain. G4 packaging creates a
self-contained application with its own Java 25 runtime.

### Distribution status

```text
PACKAGING TECHNICAL STATUS = PASS
PUBLIC REDISTRIBUTION STATUS = BLOCKED PENDING LICENSE/ASSET APPROVAL
```

An app-image, ZIP, MSI or EXE built with the repository default profile is
`INTERNAL EVALUATION — NOT FOR REDISTRIBUTION`. A build requested with
`-DistributionProfile NC` is instead `NON-COMMERCIAL DISTRIBUTION — PROFILE NC`
and carries its own notice. Technical generation and installation are
validated capabilities; producing either package is not a release. Do not
publish or share these binaries as a release without a separate author
decision.

### Generate app-image, ZIP, MSI, and EXE

First check the optional package toolchain:

```powershell
.\tools\agent\verify-packaging.ps1 -CheckToolchain
```

Bootstrap is not a packaging or installation-verification gate.

The release script exposes exactly these targets:

```powershell
# Self-contained unpacked application only
.\tools\release\build-windows-package.ps1 -Target AppImage

# Self-contained app-image plus portable ZIP
.\tools\release\build-windows-package.ps1 -Target Zip

# Self-contained app-image plus MSI
.\tools\release\build-windows-package.ps1 -Target Msi

# Self-contained app-image plus EXE installer
.\tools\release\build-windows-package.ps1 -Target Exe

# Complete app-image, ZIP, MSI, and EXE set
.\tools\release\build-windows-package.ps1 -Target All
```

Every invocation recreates the dedicated
`artifacts/packaging/windows/` output tree. Use `-Target All` when all formats
must coexist. `-SkipInstallDist` may reuse the existing `installDist` layout
only after the same source revision has already been built.

Generated, regenerable outputs are:

- `app-image/GeoCeDG/GeoCeDG.exe`: unpacked self-contained executable;
- `GeoCeDG-0.4.0-windows-x64-internal.zip`: portable app-image archive;
- `packages/GeoCeDG-0.4.0-windows-x64-internal.msi`: MSI installer;
- `packages/GeoCeDG-0.4.0-windows-x64-internal.exe`: EXE installer;
- `geocedg-windows.cdx.json`: CycloneDX 1.5 runtime SBOM;
- `build-manifest.json`: source, toolchain, composition, and exclusion record;
- `app-image.SHA256SUMS.txt` and `SHA256SUMS.txt`: exact build hashes.

These outputs are ignored evidence, not durable source, and may be deleted
after validation.

### Run without installing

Run the generated app-image directly:

```powershell
& .\artifacts\packaging\windows\app-image\GeoCeDG\GeoCeDG.exe
```

The ZIP contains the same app-image layout. Extract and run it without
installing or creating a `.cedg` association:

```powershell
$portable = Join-Path $env:TEMP `
  ("GeoCeDG-0.4.0-internal-" + [Guid]::NewGuid().ToString("N"))
Expand-Archive `
  -LiteralPath .\artifacts\packaging\windows\GeoCeDG-0.4.0-windows-x64-internal.zip `
  -DestinationPath $portable
& (Join-Path $portable "GeoCeDG\GeoCeDG.exe")
```

The expected window title is `GeoCeDG`. Both routes use the bundled runtime
and do not require the workstation Java installation at execution time.

### Install and uninstall with MSI

The accepted G4 installation smoke test used the per-user MSI silently. It
requires no GUI choices and writes a verbose log:

```powershell
$msi = (Resolve-Path `
  .\artifacts\packaging\windows\packages\GeoCeDG-0.4.0-windows-x64-internal.msi).Path
$installArgs = "/i `"$msi`" /qn /norestart /L*v `"$env:TEMP\geocedg-msi-install.log`""
$install = Start-Process msiexec.exe -ArgumentList $installArgs -Wait -PassThru
$install.ExitCode
```

Expected exit code: `0`. The validated default per-user location is:

```powershell
$installedGeoCeDG = Join-Path $env:LOCALAPPDATA "GeoCeDG\GeoCeDG.exe"
Test-Path -LiteralPath $installedGeoCeDG
& $installedGeoCeDG
```

For an interactive MSI evaluation, run `Start-Process msiexec.exe
-ArgumentList "/i `"$msi`"" -Wait` and respond only to controls actually
shown. The exact interactive page sequence is not part of the G4 acceptance
evidence.

Keep the generated MSI until the evaluation is complete. Uninstall using that
same MSI and verify exit code `0`:

```powershell
$uninstallArgs = "/x `"$msi`" /qn /norestart /L*v `"$env:TEMP\geocedg-msi-uninstall.log`""
$uninstall = Start-Process msiexec.exe -ArgumentList $uninstallArgs -Wait -PassThru
$uninstall.ExitCode
Test-Path -LiteralPath (Join-Path $env:LOCALAPPDATA "GeoCeDG")
```

The final `Test-Path` is expected to return `False`.

### Install with EXE

EXE generation is validated, but G4 used MSI for the reversible installed
smoke test. Launch the EXE interactively; no unattended EXE switches or exact
dialog sequence are claimed:

```powershell
$exe = (Resolve-Path `
  .\artifacts\packaging\windows\packages\GeoCeDG-0.4.0-windows-x64-internal.exe).Path
Start-Process -FilePath $exe -Wait
```

After installation, locate and run GeoCeDG using `$installedGeoCeDG` above.
Uninstall the EXE-installed application through Windows **Installed apps** by
selecting `GeoCeDG` and its displayed **Uninstall** action. The MSI procedure
remains the exact automated acceptance path.

### Check the `.cedg` association

The current package source declares only the GeoCeDG-owned `.cedg` association
for MSI and EXE; app-image and ZIP do not create associations. G9U0-R2 validated
this contract statically, but did not request a real installed MSI/registry
smoke. If an installer artifact is explicitly built and installed, inspect its
registration and open command with:

```powershell
$extensionKeyPath = "Registry::HKEY_CURRENT_USER\Software\Classes\.cedg"
$extensionKey = Get-Item -LiteralPath $extensionKeyPath
$progId = $extensionKey.GetValue("")
$openCommand = (Get-Item -LiteralPath `
  "Registry::HKEY_CURRENT_USER\Software\Classes\$progId\shell\open\command").GetValue("")
$progId
$openCommand
cmd /c assoc .cedg
```

The open command must reference the installed `GeoCeDG.exe`, the ProgID must be
GeoCeDG-owned and no `.ggb` association may be claimed. Verify the actual
post-uninstall state rather than assuming registry cleanup; no G9U0-R2 installed
artifact result is claimed here.

### Package composition and exclusions

The package includes the selected Windows Desktop runtime JARs, linked Java
runtime, internal-evaluation marker, and current legal-status records. G4
verified that it does not include:

- scientific PDFs;
- `Templatev7.ggb`, `.ggt` files, or the legacy/model corpus;
- Linux or macOS native JAR variants;
- the upstream GeoGebra installer, logo, or an explicit unauthorized branding
  asset.

Inherited translations, UI resources, fonts, and third-party/native resources
inside the required runtime JARs remain under audit; they are a reason public
redistribution is blocked. Inspect
`artifacts/packaging/windows/build-manifest.json`,
`geocedg-windows.cdx.json`, and
[the asset manifest](../../geocedg/resources/assets-manifest.yml) for detail.

### Verify artifacts, manifests, SBOM, and hashes

After `-Target All`, run the focused or composed artifact gate:

```powershell
.\tools\agent\verify-packaging.ps1 -CheckToolchain -RequireArtifacts
.\tools\agent\verify.ps1 -VerifyPackagingArtifacts
```

Inspect the generated evidence manually when needed:

```powershell
Get-Content .\artifacts\packaging\windows\SHA256SUMS.txt
Get-Content .\artifacts\packaging\windows\app-image.SHA256SUMS.txt
$packageManifest = Get-Content -Raw `
  .\artifacts\packaging\windows\build-manifest.json | ConvertFrom-Json
$sbom = Get-Content -Raw `
  .\artifacts\packaging\windows\geocedg-windows.cdx.json | ConvertFrom-Json
$packageManifest | Format-List
$sbom | Select-Object bomFormat, specVersion, version
```

The generated hashes are the authority for the exact artifacts from each
build. ZIP entry ordering and timestamps are normalized, but JDK 25 `jlink`
may vary `runtime/lib/modules` between equivalent runs. Byte-for-byte identity
of ZIP, MSI, or EXE is therefore not guaranteed.

### Packaging troubleshooting

- **JDK 25 or `jpackage` missing:** run `.\gradlew.bat -q javaToolchains`, install
  a full JDK 25 manually, and confirm its `bin\jpackage.exe`. The bootstrap
  never installs Java.
- **.NET SDK missing:** run
  `winget install --id Microsoft.DotNet.SDK.8 --exact`, open a new PowerShell,
  and rerun `dotnet --info`.
- **WiX missing or wrong version:** use the pinned `dotnet tool install` or
  `dotnet tool update` command above, then check `wix --version`.
- **WiX extensions missing:** from `packaging/windows`, run the two pinned
  `wix extension add -g .../5.0.2` commands and verify with
  `wix extension list -g`.
- **A recent global WiX install is not on `PATH`:** open a new PowerShell or
  update only the current process before retrying:

  ```powershell
  $env:PATH = "$env:USERPROFILE\.dotnet\tools;$env:PATH"
  wix --version
  ```

The focused installer updates `PATH` only for its current process after an
official installer/tool command. It does not persist an environment-variable
change. Opening a new PowerShell lets the normal user environment expose a
recent global tool installation.

The focused verifier prints actionable installation commands for missing
requirements. Do not patch Gradle or application sources to compensate for a
workstation or restricted-sandbox failure.

## 5. Run GeoCeDG and Classic

### GeoCeDG

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
```

This launches `org.geocedg.desktop.GeoCeDG`, uses the GeoCeDG profile and the
`geocedg` preference namespace, and suppresses the inherited branded splash
and frame icon.

### GeoGebra Classic diagnostic reference

```powershell
.\gradlew.bat :desktop:desktop:run
```

This launches the unchanged baseline entry point
`org.geogebra.desktop.GeoGebra3D`. Use it for regression, upstream comparison,
and diagnosis. GeoCeDG and Classic use separate launch paths and preference
contexts; selecting a Classic-looking perspective inside GeoCeDG is not a
substitute for this process-level reference launch.

### Known Gradle documentation discrepancy

Some historical/upstream documentation, including the archived upstream
README and an explanatory section of the roadmap, shows `:desktop:run` from
the composite root. That selector is stale for the pinned build. The actual
root commands are:

```powershell
.\gradlew.bat :desktop:desktop:runGeoCeDG
.\gradlew.bat :desktop:desktop:run
```

No source or Gradle file has been changed merely to hide that discrepancy.

## 6. Identify the running application

| Signal | GeoCeDG | Baseline Classic |
|---|---|---|
| Window title | `GeoCeDG` | `GeoGebra Classic 5` |
| Windows application ID in diagnostics | `org.geocedg.desktop` | `geogebra.AppId` |
| First-run layout | Algebra + 2D Graphics | Upstream Classic layout/preferences |
| Default toolbar | Six conservative GeoCeDG groups | Full upstream Classic toolbar |
| Branding | Textual GeoCeDG name; inherited splash/icon suppressed | Upstream Classic identity |
| Preferences | GeoCeDG `geocedg` namespace | Classic namespace |

A loaded document or saved preferences can override the first-run perspective.
When the title says `GeoCeDG` but the panels or toolbar differ, first determine
whether a document or saved preference supplied that layout; do not infer the
application identity from icons alone.

## 7. Development quick reference

```powershell
# Repository state
git status --short

# Canonical gate
.\tools\agent\verify.ps1

# Canonical gate plus informational benchmark
.\tools\agent\verify.ps1 -RunBenchmarks

# Locus V2 focused gate and developer laboratory
.\tools\agent\verify-locus-v2.ps1
.\tools\locus-v2\open-locus-v2-laboratory.ps1 -ValidateOnly
.\tools\locus-v2\open-locus-v2-laboratory.ps1

# Focused G3 catalog/ingest validation
.\tools\agent\verify-legacy.ps1

# Focused G4 contracts and generated package validation
.\tools\agent\verify-packaging.ps1
.\tools\agent\verify-packaging.ps1 -CheckToolchain -RequireArtifacts

# Focused G5 geometry/DXF tests, manifests and architecture boundary
.\tools\agent\verify-dxf.ps1

# Focused G9X1 exact/approximate DXF authority
.\tools\agent\verify-g9x1-extended-dxf.ps1

# Focused G6A controls plus productive G6B semantic/render gates
.\tools\agent\verify-locus-v2.ps1

# Focused G7/G8 Locus V2 metric and intersection gates
.\tools\agent\verify-g7a-metrics.ps1
.\tools\agent\verify-g7b-metrics.ps1
.\tools\agent\verify-g8a-intersections.ps1 -RequireFinalEvidence
.\tools\agent\verify-g8b-intersections.ps1

# Generate all internal Windows package formats
.\tools\release\build-windows-package.ps1 -Target All

# Validate the Laboratory resource without GUI
.\tools\legacy\open-laboratory.ps1 -ValidateOnly

# Desktop compilation
.\gradlew.bat :desktop:desktop:compileJava

# GeoCeDG
.\gradlew.bat :desktop:desktop:runGeoCeDG

# Baseline Classic
.\gradlew.bat :desktop:desktop:run

# Whitespace before handoff
git diff --check
```

Do not commit Gradle outputs, package binaries, or generated benchmark
evidence. Generated evidence belongs outside durable sources or under the ignored `artifacts/`
boundary defined by the operational contracts.

## 8. Technical references

- Governance: [AGENTS.md](../../AGENTS.md)
- Baseline provenance: [UPSTREAM.md](../../UPSTREAM.md) and
  [G0 report](../validation/baseline_report.md)
- Roadmap: [GeoCeDG — Living Technical Roadmap](../roadmap/geocedg_roadmap.md)
- Operational authority: [ADR 0002](../adr/0002-g1-operational-authority.md),
  [G1 report](../validation/g1_operational_layer_report.md), and
  [G1R report](../validation/g1r_repository_onboarding_report.md)
- Product profile: [ADR 0001](../adr/0001-geocedg-product-profile.md),
  [profile specification](../../geocedg/specs/ui/application-profile.md),
  [runtime manifest](../../apps/geocedg/application-profile.yml), and
  [G2 report](../validation/g2_frontend_foundation_report.md)
- Legacy integration: [ADR 0003](../adr/0003-controlled-legacy-integration.md),
  [integration specification](../../geocedg/specs/legacy/controlled-integration.md),
  [Templatev7 manifest](../../models/legacy/template-v7/manifest.yml), and
  [G3 report](../validation/g3_controlled_legacy_integration_report.md)
- Windows packaging: [ADR 0004](../adr/0004-standalone-windows-packaging.md),
  [packaging contract](../../geocedg/specs/packaging/windows-packaging.md),
  [package profile](../../packaging/windows/package.yml), and
  [G4 report](../validation/g4_standalone_packaging_report.md)
- Native 2D geometry/DXF export:
  [ADR 0005](../adr/0005-neutral-2d-geometry-export.md),
  [architecture](../architecture/native_2d_geometry_export.md),
  [normative export contract](../../geocedg/specs/export/geometry-export-foundation.md),
  [regression evidence](../../models/regression/g5-dxf-foundation/expected-entities.yml),
  [G5 report](../validation/g5_native_2d_dxf_export_report.md),
  [G9X1 fidelity contract](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md),
  [Accepted ADR 0014](../adr/0014-export-only-dxf-approximation-and-sidecar.md),
  and [G9X1 closeout report](../validation/g9x1_extended_dxf_implementation_candidate_report.md)
- Locus V2 characterization and experimental kernel:
  [G6 plan](../roadmap/g6_locus_v2_plan.md),
  [semantic model](../architecture/locus_v2_semantic_model.md),
  [upstream impact audit](../architecture/locus_v2_upstream_impact.md),
  [Accepted ADR 0006](../adr/0006-parallel-locus-v2-semantic-entity.md),
  [normative contract](../../geocedg/specs/locus/locus-v2-semantics.md),
  [G6A report](../validation/g6a_locus_v2_characterization_report.md),
  [G6B report](../validation/g6b_locus_v2_kernel_report.md), and
  [functional evidence](../../geocedg/validation/locus-v2/g6b-functional-evidence.yml)
- Locus V2 G6R hardening and developer references:
  [implementation architecture](../architecture/locus_v2_implementation.md),
  [internal API](../developer/locus_v2_api.md),
  [repository map](../developer/repository_map.md),
  [traceability](../validation/g6r_locus_v2_traceability_matrix.md),
  [G6R report](../validation/g6r_locus_v2_hardening_report.md), and
  [hardening evidence](../../geocedg/validation/locus-v2/g6r-hardening-evidence.yml)
- Locus V2 G7 author-approved internal metric architecture and evidence:
  [G7 plan](../roadmap/g7_locus_v2_metrics_plan.md),
  [metric semantic model](../architecture/locus_v2_metric_semantic_model.md),
  [metric architecture](../architecture/locus_v2_metric_architecture.md),
  [normative metric spec](../../geocedg/specs/locus/locus-v2-metrics.md),
  [Accepted ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md),
  [validation matrix](../validation/g7_locus_v2_metric_validation_matrix.md),
  [benchmark plan](../validation/g7_locus_v2_metric_benchmark_plan.md),
  [G7A report](../validation/g7a_locus_v2_metric_characterization_report.md),
  [focused R1 report](../validation/g7a_r1_locus_v2_metric_refinement_report.md),
  [developer API](../developer/locus_v2_metric_api.md),
  [G7A traceability](../validation/g7a_locus_v2_metric_traceability_matrix.md),
  [G7B report](../validation/g7b_locus_v2_metric_kernel_report.md), and
  [G7B traceability](../validation/g7b_locus_v2_metric_traceability_matrix.md)
- Locus V2 G8 author-approved planning/G8A/G8B/G8C design/G8C1/G8C2 and
  global closeout; no observable extended
  feature:
  [G8 plan](../roadmap/g8_locus_v2_intersections_plan.md),
  [semantic model](../architecture/locus_v2_intersection_semantic_model.md),
  [architecture](../architecture/locus_v2_intersection_architecture.md),
  [upstream impact map](../architecture/locus_v2_intersection_upstream_impact.md),
  [normative specification](../../geocedg/specs/locus/locus-v2-intersections.md),
  [validation matrix](../validation/g8_locus_v2_intersection_validation_matrix.md),
  [functional-counter plan](../validation/g8_locus_v2_intersection_benchmark_plan.md),
  [scientific traceability](../validation/g8_locus_v2_intersection_scientific_traceability.md),
  [G8A report](../validation/g8a_locus_v2_intersection_characterization_report.md),
  [G8A traceability](../validation/g8a_locus_v2_intersection_traceability_matrix.md),
  [G8B report](../validation/g8b_locus_v2_intersection_kernel_report.md),
  [G8B traceability](../validation/g8b_locus_v2_intersection_traceability_matrix.md),
  [Accepted ADR 0008](../adr/0008-locus-v2-intersection-result-and-continuation.md),
  [G8C design](../roadmap/g8c_locus_v2_extended_intersections_design.md),
  [G8C1/G8C2 normative spec](../../geocedg/specs/locus/locus-v2-extended-intersections.md),
  [G8C report](../validation/g8c_locus_v2_extended_intersection_characterization_report.md),
  [G8C1 kernel report](../validation/g8c1_locus_v2_extended_target_intersection_kernel_report.md),
  [G8C1 traceability](../validation/g8c1_locus_v2_extended_target_intersection_traceability_matrix.md), and
  [ADR 0009 Accepted](../adr/0009-locus-v2-locus-intersection-pair-semantics.md), and
  [G8C2 contract review](../validation/g8c2_locus_v2_locus_intersection_contract_review.md)
- Current feature state:
  [stable manifest](../../geocedg/features/stable.yml) and
  [experimental manifest](../../geocedg/features/experimental.yml)
- Scientific sources and public models:
  [CeDG reference catalog](../references/cedg/catalog.yml) and
  [public model corpus](../references/cedg/public-model-corpus.yml)
- Build topology:
  [upstream module map](../architecture/upstream_module_map.md)
- Redistribution constraints:
  [component/license matrix](../licensing/component-matrix.md)

## 9. Maintenance authority

The authoritative update gate is the roadmap's
[transversal documentary closure rule](../roadmap/geocedg_roadmap.md#reglas-de-mantenimiento-y-cierre-documental).
This guide does not redefine that rule.

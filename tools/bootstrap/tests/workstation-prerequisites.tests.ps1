#requires -Version 7.2
[CmdletBinding()]
param([Parameter(Mandatory)] [string]$RepositoryRoot)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Import-Module (Join-Path $RepositoryRoot "tools/bootstrap/workstation-prerequisites.psm1") -Force
Import-Module (Join-Path $RepositoryRoot "tools/bootstrap/packaging-prerequisites.psm1") -Force
$script:Checks = 0
$fixtureRoot = $null

function Assert-Fixture {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
    $script:Checks++
}
function Assert-FixtureFailure {
    param([scriptblock]$Action, [string]$MessagePattern)
    $observed = $false
    try { & $Action | Out-Null }
    catch {
        if ($_.Exception.Message -notmatch $MessagePattern) { throw "Unexpected fixture failure: $($_.Exception.Message); expected $MessagePattern" }
        $observed = $true
    }
    Assert-Fixture $observed "Fixture unexpectedly succeeded; expected $MessagePattern"
}
function New-WorkstationFixture {
    param([object]$Requirements)
    $prefix = "C:\fixture\envs\$($Requirements.CondaEnvironment)"
    $state = [pscustomobject]@{
        Requirements = $Requirements
        Calls = [Collections.Generic.List[object]]::new()
        EffectiveMajor = $Requirements.GradleJava
        WrapperMajor = $Requirements.GradleJava
        CondaExit = 0
        PadNativeOutput = $false
        OmitCompiler = $false
        OmitDesktop = $false
        InvalidToolchain = $false
        Probe = [pscustomobject]@{
            python = $Requirements.Python; implementation = "CPython"
            python_executable = "$prefix\python.exe"; python_prefix = $prefix
            environment_name = $Requirements.CondaEnvironment; environment_prefix = $prefix
            mpmath = $Requirements.Mpmath; mpmath_file = "$prefix\Lib\site-packages\mpmath\__init__.py"
        }
    }
    $runner = {
        param($Command, $Arguments, $Description)
        $state.Calls.Add([pscustomobject]@{ Command = [string]$Command; Arguments = $Arguments; Description = $Description })
        $code = 0
        $output = switch ($Description) {
            "effective Gradle Java version" { 'openjdk version "{0}.0.1"' -f $state.EffectiveMajor }
            "Conda named-environment version and import-origin probe" {
                if ($state.CondaExit -ne 0) { $code = $state.CondaExit; "EnvironmentNameNotFound: fixture" }
                else { "fixture banner"; "GEOCE_WORKSTATION:" + ($state.Probe | ConvertTo-Json -Compress) }
            }
            "Gradle wrapper launcher inventory" { "Launcher JVM: $($state.WrapperMajor).0.1 (fixture)"; "Daemon JVM: C:\fixture\launcher (fixture)" }
            "Gradle JDK inventory" {
                # JRE before JDK exercises full-JDK selection and custom locations.
                " + fixture JRE"; "     | Language Version: $($state.Requirements.CompilerJava)"; "     | Location: C:\fixture\jre"; "     | Vendor: fixture"; "     | Is JDK: false"
                foreach ($version in @($state.Requirements.CompilerJava, $state.Requirements.DesktopJava)) {
                    if (($state.OmitCompiler -and $version -eq $state.Requirements.CompilerJava) -or ($state.OmitDesktop -and $version -eq $state.Requirements.DesktopJava)) { continue }
                    " + fixture JDK"; "     | Language Version: $version"; "     | Location: C:\custom-cache\jdk-$version"; "     | Vendor: fixture"
                    if ($state.InvalidToolchain) { "     | Is JDK: false" } else { "     | Is JDK: true" }
                }
            }
            default {
                if ($Description -match "^JDK (\d+) (java|javac) version$") {
                    if ($Matches[2] -eq "javac") { "javac {0}.0.1" -f $Matches[1] }
                    else { 'openjdk version "{0}.0.1"' -f $Matches[1] }
                } else { throw "Unexpected preflight native command: $Description" }
            }
        }
        if ($state.PadNativeOutput) { $output = @('') + @($output | ForEach-Object { $_; '' }) }
        [pscustomobject]@{ ExitCode = $code; Output = @($output) }
    }.GetNewClosure()
    return [pscustomobject]@{ State = $state; Runner = $runner }
}
function Invoke-WorkstationFixture {
    param([object]$Fixture, [string]$JavaHome = "C:\fixture\home22", [object]$Conda = "C:\fixture\conda.exe")
    Invoke-WorkstationPrerequisiteCheck -RepositoryRoot $RepositoryRoot -JavaHome $JavaHome -Commands @{ Java = "C:\fixture\path25\java.exe"; Conda = $Conda } -CommandRunner $Fixture.Runner -TestFile { param($Path) $true } -Requirements $Fixture.State.Requirements
}
function New-PackagingFixture {
    param([bool]$Sdk = $true, [string]$Wix = "5.0.2", [string[]]$Extensions = @("WixToolset.Util.wixext 5.0.2", "WixToolset.UI.wixext 5.0.2"), [bool]$Winget = $true)
    $state = [pscustomobject]@{
        Sdk = $Sdk; Wix = $Wix; Extensions = @($Extensions); Winget = $Winget
        Calls = [Collections.Generic.List[string]]::new()
        Actions = [Collections.Generic.List[string]]::new()
    }
    $probe = {
        param($Name, $Value)
        $state.Calls.Add($Name)
        switch ($Name) {
            "DotNet" { "C:\fixture\dotnet.exe" }
            "CompatibleSdk" { if ($state.Sdk) { "8.0.0 [fixture]" } }
            "WixVersion" { if (-not $state.Sdk) { throw "SDK-dependent inventory was called without SDK" }; $state.Wix }
            "WixPath" { "C:\fixture\wix.exe" }
            "WixExtensions" { $state.Extensions }
            "Jpackage" { [pscustomobject]@{ Version = "25.0.1"; Path = "C:\fixture\jpackage.exe" } }
            "Winget" { if ($state.Winget) { [pscustomobject]@{ Source = "C:\fixture\winget.exe" } } }
            default { throw "Unknown fixture probe $Name" }
        }
    }.GetNewClosure()
    $inventory = { Get-PackagingPrerequisiteInventory -Probe $probe }.GetNewClosure()
    $apply = {
        param($Action, $Inventory, $Value)
        $state.Actions.Add($Action)
        switch ($Action) {
            "InstallDotNetSdk" { $state.Sdk = $true }
            { $_ -in @("InstallWix", "UpdateWix") } { $state.Wix = "5.0.2" }
            "AddWixExtension" { $state.Extensions += "$Value 5.0.2" }
            default { throw "Unexpected fixture install action $Action" }
        }
    }.GetNewClosure()
    return [pscustomobject]@{ State = $state; Inventory = $inventory; Apply = $apply }
}

try {
    Write-Host "==> Derived workstation requirements and fake native preflight"
    $requirements = Get-WorkstationRequirements -RepositoryRoot $RepositoryRoot
    Assert-Fixture ($requirements.CompilerJava -gt 0 -and $requirements.GradleJava -gt 0 -and $requirements.DesktopJava -gt 0) "Java requirements were not derived."
    Assert-Fixture ($requirements.Authorities.Count -ge 6) "Prerequisite authority paths were not retained."
    Assert-Fixture ($requirements.PythonImplementation -ceq "CPython") "Declared CPython provenance was not derived from the generators."
    foreach ($relative in @("tools/bootstrap/bootstrap-windows.ps1", "tools/bootstrap/install-packaging-prerequisites.ps1", "tools/bootstrap/workstation-prerequisites.psm1", "tools/bootstrap/packaging-prerequisites.psm1", "tools/bootstrap/tests/workstation-prerequisites.tests.ps1", "tools/agent/verify-workstation.ps1")) {
        $floorTokens = $null; $floorErrors = $null
        $floorAst = [Management.Automation.Language.Parser]::ParseFile((Join-Path $RepositoryRoot $relative), [ref]$floorTokens, [ref]$floorErrors)
        Assert-Fixture ($floorErrors.Count -eq 0 -and $floorAst.ScriptRequirements.RequiredPSVersion -eq [version]"7.2") "Current operational PowerShell floor must be explicit 7.2: $relative"
    }
    $contradictoryReader = {
        param($Path)
        $content = Get-Content -Raw -LiteralPath $Path
        if ($Path.Replace('\', '/').EndsWith("/g8a/generate_intersection_references.py", [StringComparison]::OrdinalIgnoreCase)) {
            $content = [regex]::Replace($content, '(?m)^EXPECTED_MPMATH\s*=.*$', 'EXPECTED_MPMATH = "0.0.0"')
        }
        $content
    }
    Assert-FixtureFailure { Get-WorkstationRequirements -RepositoryRoot $RepositoryRoot -ReadSource $contradictoryReader } "disagree"
    $selected = Get-EffectiveGradleJavaPath -JavaHome '"C:\fixture\home22"' -PathJava "C:\fixture\path25\java.exe" -WorkingDirectory $RepositoryRoot
    Assert-Fixture ($selected.Selection -eq "JAVA_HOME" -and $selected.Path -eq "C:\fixture\home22\bin\java.exe") "Wrapper JAVA_HOME precedence/quote normalization changed."
    Assert-FixtureFailure { Get-EffectiveGradleJavaPath -JavaHome "  " -PathJava "C:\fixture\path22\java.exe" -WorkingDirectory $RepositoryRoot } "defined but blank"
    $selected = Get-EffectiveGradleJavaPath -JavaHome "" -PathJava "C:\fixture\path22\java.exe" -WorkingDirectory $RepositoryRoot
    Assert-Fixture ($selected.Selection -eq "PATH") "Unset JAVA_HOME must use PATH."

    $fixture = New-WorkstationFixture $requirements
    $facts = Invoke-WorkstationFixture $fixture
    Assert-Fixture ($facts.CompilerToolchain.Location -eq "C:\custom-cache\jdk-$($requirements.CompilerJava)") "A JRE was selected instead of the custom-location JDK."
    Assert-Fixture ($facts.Conda.mpmath_file -eq $fixture.State.Probe.mpmath_file) "Import-origin evidence was not retained."
    Assert-Fixture ($fixture.State.Calls[0].Command -eq "C:\fixture\home22\bin\java.exe") "PATH25 incorrectly replaced JAVA_HOME22."
    $condaCalls = @($fixture.State.Calls | Where-Object { $_.Description -like "Conda*" })
    Assert-Fixture ($condaCalls.Count -eq 1 -and ($condaCalls[0].Arguments -join " ") -match ([regex]::Escape("-n " + $requirements.CondaEnvironment + " python"))) "Probe must use the named Conda environment exactly once."
    $generatedProbe = Get-CondaPrerequisiteProbe
    Assert-Fixture ($condaCalls[0].Arguments[-2] -ceq "-c" -and $condaCalls[0].Arguments[-1] -ceq $generatedProbe -and -not $generatedProbe.Contains([char]34) -and $generatedProbe.Contains("print('GEOCE_WORKSTATION:'")) "Actual generated Python argument must retain its single-quote-only native marshalling contract."
    $inventoryCall = @($fixture.State.Calls | Where-Object { $_.Description -eq "Gradle JDK inventory" })[0]
    Assert-Fixture ($inventoryCall.Arguments -contains "-Dorg.gradle.java.installations.auto-download=false") "Preflight must not opt into JDK download."

    $fixture = New-WorkstationFixture $requirements
    $fixture.State.EffectiveMajor = $requirements.GradleJava + 1
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "Effective Gradle launcher"
    Assert-Fixture ($fixture.State.Calls.Count -eq 1) "Mismatch must stop before Conda/Gradle."
    $fixture = New-WorkstationFixture $requirements
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture -Conda $null } "Conda is unavailable"
    Assert-Fixture (@($fixture.State.Calls | Where-Object { $_.Description -like "Gradle*" }).Count -eq 0) "Missing Conda reached Gradle."
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.CondaExit = 23
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "exit code 23"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.python = "0.0.0"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "requires Python"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.mpmath = "0.0.0"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "requires Python"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.implementation = "PyPy"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "requires Python implementation CPython"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.mpmath_file = "C:\global-python\mpmath\__init__.py"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "origin is inconsistent"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.environment_name = "base"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "origin is inconsistent"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.Probe.python_prefix = "C:\global-python"
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "origin is inconsistent"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.OmitCompiler = $true
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "usable full JDK"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.OmitDesktop = $true
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "usable full JDK"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.InvalidToolchain = $true
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "usable full JDK"
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.WrapperMajor = $requirements.GradleJava + 1
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "Actual wrapper Launcher"
    $fixture = New-WorkstationFixture $requirements
    Assert-FixtureFailure { Invoke-WorkstationPrerequisiteCheck -RepositoryRoot $RepositoryRoot -JavaHome "C:\missing" -Commands @{ Java = "C:\fixture\java.exe"; Conda = "conda" } -CommandRunner $fixture.Runner -Requirements $requirements -TestFile { param($Path) $false } } "Selected Gradle Java is missing"

    Write-Host "==> Native output blank-line parsing and retained content requirements"
    $nativePaddingChecks = $script:Checks
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.PadNativeOutput = $true
    $facts = Invoke-WorkstationFixture $fixture
    Assert-Fixture ($facts.CompilerToolchain.LanguageVersion -eq $requirements.CompilerJava -and
        $facts.DesktopToolchain.LanguageVersion -eq $requirements.DesktopJava -and
        $facts.Conda.python -ceq $requirements.Python -and $facts.Conda.mpmath -ceq $requirements.Mpmath) "Valid padded native output lost required JDK or Conda facts."
    Assert-Fixture ($facts.GradleVersionOutput[0] -ceq '' -and $facts.GradleVersionOutput[-1] -ceq '' -and
        $facts.ToolchainOutput[0] -ceq '' -and $facts.ToolchainOutput[-1] -ceq '') "Blank-line parsing must not strip or rewrite retained native output."
    Assert-FixtureFailure { ConvertFrom-JavaVersionOutput -Output @('', ' ', "`t") } "Unable to parse Java version output"
    Assert-FixtureFailure { ConvertFrom-CondaProbeOutput -Output @('', ' ', "`t") -Requirements $requirements } "exactly one structured environment record"
    $duplicateRecord = 'GEOCE_WORKSTATION:' + ($fixture.State.Probe | ConvertTo-Json -Compress)
    Assert-FixtureFailure { ConvertFrom-CondaProbeOutput -Output @('', $duplicateRecord, '', $duplicateRecord, '') -Requirements $requirements } "exactly one structured environment record"
    Assert-Fixture (@(ConvertFrom-GradleToolchainOutput -Output @('', ' ', "`t")).Count -eq 0) "Blank-only Gradle output must not manufacture a JDK candidate."
    $fixture = New-WorkstationFixture $requirements
    $fixture.State.PadNativeOutput = $true
    $fixture.State.OmitCompiler = $true
    Assert-FixtureFailure { Invoke-WorkstationFixture $fixture } "usable full JDK $($requirements.CompilerJava)"
    Assert-Fixture (@($fixture.State.Calls | Where-Object { $_.Description -match '^JDK \d+ (java|javac) version$' }).Count -eq 0) "Missing required JDK must stop before probing another JDK candidate."
    Write-Host "Native output padding fixtures passed: $($script:Checks - $nativePaddingChecks) assertions."

    Write-Host "==> Fake-first Conda name-or-prefix identity and retained origin guards"
    $condaIdentityChecks = $script:Checks
    $customPrefix = "C:\fixture\custom locations\$($requirements.CondaEnvironment)"
    $positiveIdentities = @($customPrefix, ($customPrefix.ToUpperInvariant().Replace('\', '/') + '/'))
    foreach ($identity in $positiveIdentities) {
        $fixture = New-WorkstationFixture $requirements
        $fixture.State.Probe.environment_prefix = $customPrefix
        $fixture.State.Probe.python_prefix = $customPrefix
        $fixture.State.Probe.python_executable = "$customPrefix\python.exe"
        $fixture.State.Probe.mpmath_file = "$customPrefix\Lib\site-packages\mpmath\__init__.py"
        $fixture.State.Probe.environment_name = $identity
        $facts = Invoke-WorkstationFixture $fixture
        Assert-Fixture ($facts.Conda.environment_name -ceq $identity -and $facts.Conda.environment_prefix -ceq $customPrefix -and
            $facts.Conda.python_prefix -ceq $customPrefix -and $facts.Conda.python_executable -ceq $fixture.State.Probe.python_executable -and
            $facts.Conda.mpmath_file -ceq $fixture.State.Probe.mpmath_file) "Custom-prefix Conda acceptance changed or discarded raw identity/origin evidence: $identity"
        $condaCalls = @($fixture.State.Calls | Where-Object { $_.Description -like "Conda*" })
        Assert-Fixture ($condaCalls.Count -eq 1 -and $condaCalls[0].Command -ceq "C:\fixture\conda.exe" -and
            $condaCalls[0].Arguments.Count -eq 7 -and
            ($condaCalls[0].Arguments[0..5] -join '|') -ceq "run|--no-capture-output|-n|$($requirements.CondaEnvironment)|python|-c" -and
            $condaCalls[0].Arguments[6] -ceq (Get-CondaPrerequisiteProbe)) "Custom prefix changed named Conda selection, native argv or probe payload."
    }
    $negativeOrigins = @(
        @{ Name = "different absolute identity"; Field = "environment_name"; Value = "D:\other\different_env"; Pattern = "origin is inconsistent" },
        @{ Name = "different absolute identity with same basename"; Field = "environment_name"; Value = "D:\other\$($requirements.CondaEnvironment)"; Pattern = "origin is inconsistent" },
        @{ Name = "absolute prefix lookalike"; Field = "environment_name"; Value = "$customPrefix-other"; Pattern = "origin is inconsistent" },
        @{ Name = "relative identity"; Field = "environment_name"; Value = "custom locations\$($requirements.CondaEnvironment)"; Pattern = "origin is inconsistent" },
        @{ Name = "root-relative identity"; Field = "environment_name"; Value = "\fixture\custom locations\$($requirements.CondaEnvironment)"; Pattern = "origin is inconsistent" },
        @{ Name = "drive-relative identity"; Field = "environment_name"; Value = "C:fixture\custom locations\$($requirements.CondaEnvironment)"; Pattern = "origin is inconsistent" },
        @{ Name = "empty identity"; Field = "environment_name"; Value = ""; Pattern = "missing environment_name" },
        @{ Name = "wrong short name"; Field = "environment_name"; Value = "base"; Pattern = "origin is inconsistent" },
        @{ Name = "custom identity with mismatched sys.prefix"; Field = "python_prefix"; Value = "C:\global-python"; Pattern = "origin is inconsistent" },
        @{ Name = "custom identity with global executable"; Field = "python_executable"; Value = "C:\global-python\python.exe"; Pattern = "origin is inconsistent" },
        @{ Name = "custom identity with external mpmath"; Field = "mpmath_file"; Value = "C:\global-python\mpmath\__init__.py"; Pattern = "origin is inconsistent" },
        @{ Name = "custom identity with wrong Python"; Field = "python"; Value = "0.0.0"; Pattern = "requires Python" },
        @{ Name = "custom identity with wrong implementation"; Field = "implementation"; Value = "PyPy"; Pattern = "requires Python implementation CPython" },
        @{ Name = "custom identity with wrong mpmath"; Field = "mpmath"; Value = "0.0.0"; Pattern = "requires Python" }
    )
    foreach ($case in $negativeOrigins) {
        $fixture = New-WorkstationFixture $requirements
        $fixture.State.Probe.environment_prefix = $customPrefix
        $fixture.State.Probe.python_prefix = $customPrefix
        $fixture.State.Probe.python_executable = "$customPrefix\python.exe"
        $fixture.State.Probe.mpmath_file = "$customPrefix\Lib\site-packages\mpmath\__init__.py"
        $fixture.State.Probe.environment_name = $customPrefix
        $fixture.State.Probe.PSObject.Properties[$case.Field].Value = $case.Value
        $failure = $null
        try { Invoke-WorkstationFixture $fixture | Out-Null } catch { $failure = $_.Exception }
        $originFailure = $case.Pattern -ceq "origin is inconsistent"
        $expectedStage = if ($originFailure) { "Conda import origin" } else { "Conda environment probe" }
        $expectedClassification = if ($case.Field -in @("python", "implementation", "mpmath")) { "toolchain-incompatibility" } else { "workstation-environment" }
        Assert-Fixture ($null -ne $failure -and $failure.Message -match $case.Pattern -and
            $failure.Data["Stage"] -ceq $expectedStage -and $failure.Data["FailureClassification"] -ceq $expectedClassification -and
            $null -eq $failure.Data["NativeExitCode"]) "Conda identity/origin case did not retain its required rejection, classification or non-native failure: $($case.Name)"
        Assert-Fixture ($fixture.State.Calls.Count -eq 2 -and
            @($fixture.State.Calls | Where-Object { $_.Description -like "Gradle*" }).Count -eq 0) "Rejected Conda identity/origin reached Gradle: $($case.Name)"
        if ($originFailure) {
            Assert-Fixture ($failure.Message.Contains("CONDA_DEFAULT_ENV=" + $fixture.State.Probe.environment_name + ";")) "Origin diagnostic omitted the exact raw Conda identity: $($case.Name)"
        }
    }
    Write-Host "Conda identity regression fixtures passed: $($script:Checks - $condaIdentityChecks) assertions across $($positiveIdentities.Count + $negativeOrigins.Count) cases."

    Write-Host "==> Fake-first pinned packaging inventory and action sequence"
    Assert-Fixture (Test-PinnedWixVersion "5.0.2") "Pinned WiX rejected."
    Assert-Fixture (Test-PinnedWixVersion "5.0.2+fixture") "WiX build metadata rejected."
    Assert-Fixture (-not (Test-PinnedWixVersion "5.0.20")) "WiX prefix false positive."
    Assert-Fixture (-not (Test-PinnedWixVersion "5.0.2-preview")) "WiX prerelease false positive."
    Assert-Fixture (@(ConvertFrom-WixExtensionInventory -ExitCode 2 -Output @()).Count -eq 0) "Exit2-empty must be empty inventory."
    Assert-FixtureFailure { ConvertFrom-WixExtensionInventory -ExitCode 2 -Output @("real failure") } "exit code 2"
    Assert-FixtureFailure { ConvertFrom-WixExtensionInventory -ExitCode 17 -Output @() } "exit code 17"
    $fixture = New-PackagingFixture -Sdk $false
    $result = Invoke-PackagingPrerequisiteWorkflow -Inventory $fixture.Inventory -Apply $fixture.Apply
    Assert-Fixture ($result.Inventory.WixInventoryDeferred -and $fixture.State.Actions.Count -eq 0) "Runtime-only inspect must defer WiX and never install."
    Assert-Fixture (-not $fixture.State.Calls.Contains("WixVersion") -and -not $fixture.State.Calls.Contains("WixExtensions")) "SDK-dependent inspection ran without SDK."
    $fixture = New-PackagingFixture -Sdk $false
    $result = Invoke-PackagingPrerequisiteWorkflow -Install -Inventory $fixture.Inventory -Apply $fixture.Apply
    Assert-Fixture ($result.PackagingReady -and ($fixture.State.Actions -join ",") -eq "InstallDotNetSdk") "SDK repair failed to discover existing WiX/extensions before planning."
    $fixture = New-PackagingFixture -Sdk $false -Winget $false
    Assert-FixtureFailure { Invoke-PackagingPrerequisiteWorkflow -Install -Inventory $fixture.Inventory -Apply $fixture.Apply } "WinGet is unavailable"
    Assert-Fixture ($fixture.State.Actions.Count -eq 0) "Blocked manual SDK path performed an install."
    $fixture = New-PackagingFixture -Wix "6.0.2"
    $result = Invoke-PackagingPrerequisiteWorkflow -Install -Inventory $fixture.Inventory -Apply $fixture.Apply
    Assert-Fixture ($result.PackagingReady -and ($fixture.State.Actions -join ",") -eq "UpdateWix") "WiX update failed to retain installed extension inventory."
    $fixture = New-PackagingFixture -Extensions @()
    $result = Invoke-PackagingPrerequisiteWorkflow -Install -Inventory $fixture.Inventory -Apply $fixture.Apply
    Assert-Fixture ($result.PackagingReady -and $fixture.State.Actions.Count -eq 2) "Missing extensions were not installed exactly once."
    $fixture = New-PackagingFixture -Wix "" -Extensions @()
    $result = Invoke-PackagingPrerequisiteWorkflow -Inventory $fixture.Inventory -Apply $fixture.Apply
    Assert-Fixture ($fixture.State.Actions.Count -eq 0 -and -not $result.PackagingReady) "Inspect-only unexpectedly installed missing tooling."

    Write-Host "==> Bootstrap native diagnostics in isolated local fixture"
    $fixtureRoot = Join-Path ([IO.Path]::GetTempPath()) ("geocedg-bootstrap-diagnostics-" + [Guid]::NewGuid().ToString("N"))
    [void](New-Item -ItemType Directory -Path (Join-Path $fixtureRoot "preflight") -Force)
    # Load only named function declarations, never bootstrap's top-level commands.
    $tokens = $null; $parseErrors = $null
    $ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $RepositoryRoot "tools/bootstrap/bootstrap-windows.ps1"), [ref]$tokens, [ref]$parseErrors)
    Assert-Fixture ($parseErrors.Count -eq 0) "Bootstrap source did not parse."
    foreach ($name in @("Invoke-NativeResult", "Invoke-Native", "Complete-BootstrapDiagnostics")) {
        $definition = $ast.FindAll({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] }, $true) | Where-Object { $_.Name -eq $name }
        Assert-Fixture (@($definition).Count -eq 1) "Expected exactly one bootstrap diagnostic function $name."
        . ([scriptblock]::Create($definition.Extent.Text))
    }
    $LogDirectory = $fixtureRoot
    $CurrentStage = "fake-first native diagnostic fixture"
    $NativeRecords = [Collections.Generic.List[object]]::new()
    $pwsh = Join-Path $PSHOME "pwsh.exe"
    # -File may execute the script root in global scope. A child scope is
    # required to create the distinct caller shadow exercised by this case.
    & {
        $LASTEXITCODE = 97
        $native = Invoke-NativeResult -Command $pwsh -ArgumentList @("-NoLogo", "-NoProfile", "-Command", "Write-Output 'fixture-output'; exit 23") -Description "isolated native fixture"
        Assert-Fixture ($native.ExitCode -eq 23 -and -not $NativeRecords[-1].InvocationFailed -and (Get-Content -Raw -LiteralPath $native.LogPath).Contains("fixture-output")) "Native output/exit code was not preserved."
        $next = Invoke-NativeResult -Command $pwsh -ArgumentList @("-NoLogo", "-NoProfile", "-Command", "exit 0") -Description "isolated native fixture"
        Assert-Fixture ($next.ExitCode -eq 0 -and -not $NativeRecords[-1].InvocationFailed -and $next.LogPath -ne $native.LogPath -and $NativeRecords.Count -eq 2) "Native zero/repeated description lost its result or overwrote native logs."
        $nativeOneFailure = $null
        try { Invoke-Native -FilePath $pwsh -ArgumentList @("-NoLogo", "-NoProfile", "-Command", "exit 1") -Description "actual native exit one fixture" | Out-Null }
        catch { $nativeOneFailure = $_.Exception }
        Assert-Fixture ($null -ne $nativeOneFailure -and $nativeOneFailure.Data["NativeExitCode"] -eq 1 -and $nativeOneFailure.Data["FailureClassification"] -ceq "unknown-native-failure" -and $NativeRecords[-1].ExitCode -eq 1 -and -not $NativeRecords[-1].InvocationFailed) "Actual native exit one was confused with a failed invocation."
        Assert-FixtureFailure { Invoke-Native -FilePath $pwsh -ArgumentList @("-NoLogo", "-NoProfile", "-Command", "Write-Output 'java.nio.file.AccessDeniedException fixture'; exit 31") -Description "fixture access failure" } "exit code 31"
        Assert-Fixture ($NativeRecords[0].Stage -eq $CurrentStage) "Native log lost stage identity."
        Assert-Fixture ($LASTEXITCODE -eq 97 -and $global:LASTEXITCODE -eq 31) "The fixture did not retain a distinct caller shadow and actual native result."
    }

    # A PowerShell adapter that emits output but never invokes a native process
    # must not inherit the prior native exit as evidence of its own success.
    function Invoke-FixtureWithoutNativeExit { param([string]$Mode) Write-Output "fixture adapter: $Mode" }
    $uncapturedFailure = $null
    try { Invoke-NativeResult -Command "Invoke-FixtureWithoutNativeExit" -ArgumentList @("no-exit") -Description "uncaptured native exit fixture" | Out-Null }
    catch { $uncapturedFailure = $_.Exception }
    Assert-Fixture ($null -ne $uncapturedFailure -and $null -eq $uncapturedFailure.Data["NativeExitCode"] -and $uncapturedFailure.Data["FailureClassification"] -ceq "native-invocation-failure" -and $NativeRecords[-1].InvocationFailed) "An invocation without a native exit inherited a previous code or became success."

    Write-Host "==> Fake-first absent command and direct WiX consumer boundary"
    $absentCommand = Join-Path $fixtureRoot "absent-native-command.exe"
    Assert-Fixture (-not (Test-Path -LiteralPath $absentCommand)) "Absent-command fixture unexpectedly exists."
    $global:LASTEXITCODE = 97
    $absentFailure = $null
    try { Invoke-Native -FilePath $absentCommand -ArgumentList @("--fixture") -Description "absent command fixture" | Out-Null }
    catch { $absentFailure = $_.Exception }
    Assert-Fixture ($null -ne $absentFailure -and $null -eq $absentFailure.Data["NativeExitCode"] -and $absentFailure.Data["FailureClassification"] -ceq "native-invocation-failure" -and $absentFailure.Data["Stage"] -ceq "absent command fixture") "Absent command inherited a stale or invented native exit code."
    Assert-Fixture ($null -ne $absentFailure.InnerException -and $absentFailure.Data["NativeOutput"].Contains("absent-native-command.exe")) "Invocation failure lost its original exception/output."
    Assert-Fixture ($null -eq $NativeRecords[-1].ExitCode -and $NativeRecords[-1].InvocationFailed -and $NativeRecords[-1].LogSaved -and (Get-Content -Raw -LiteralPath $NativeRecords[-1].LogPath).Contains("absent-native-command.exe")) "Invocation evidence was not retained with a null native exit and saved log."

    # Mirror the actual direct WiX result/consumer sequence. The sentinel proves
    # the integer consumer never receives null, which it could coerce to zero.
    $wixCommand = [pscustomobject]@{ Source = $absentCommand }
    $extensionResult = $null
    $extensionOutput = $null
    $wixConsumerReached = $false
    $wixInvocationFailure = $null
    try {
        $extensionResult = Invoke-NativeResult -Command $wixCommand.Source -ArgumentList @("extension", "list", "-g") -Description "WiX global extension inventory"
        $wixConsumerReached = $true
        $extensionOutput = @(ConvertFrom-WixExtensionInventory -ExitCode $extensionResult.ExitCode -Output $extensionResult.Output)
    } catch { $wixInvocationFailure = $_.Exception }
    Assert-Fixture ($null -ne $wixInvocationFailure -and $null -eq $wixInvocationFailure.Data["NativeExitCode"] -and $wixInvocationFailure.Data["FailureClassification"] -ceq "native-invocation-failure" -and -not $wixConsumerReached -and $null -eq $extensionResult -and $null -eq $extensionOutput) "An absent WiX command reached the integer exit consumer or became a successful inventory."

    Write-Host "==> Actual installer native capture under caller-scope shadow"
    & {
        param($InstallerPath, $Executable, $FixtureDirectory)
        $installerTokens = $null; $installerErrors = $null
        $installerAst = [Management.Automation.Language.Parser]::ParseFile($InstallerPath, [ref]$installerTokens, [ref]$installerErrors)
        Assert-Fixture ($installerErrors.Count -eq 0) "Installer source did not parse."
        foreach ($name in @("Invoke-Native", "Get-WixExtensions")) {
            $definition = @($installerAst.FindAll({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] }, $true) | Where-Object { $_.Name -eq $name })
            Assert-Fixture ($definition.Count -eq 1) "Expected one installer native function $name."
            . ([scriptblock]::Create($definition[0].Extent.Text))
        }
        $Install = $false
        $LASTEXITCODE = 97
        foreach ($expected in @(0, 1, 23)) {
            $failure = $null
            try { Invoke-Native -FilePath $Executable -ArgumentList @("-NoProfile", "-Command", "exit $expected") -Description "installer isolated native exit" | Out-Null }
            catch { $failure = $_.Exception }
            if ($expected -eq 0) { Assert-Fixture ($null -eq $failure -and $global:LASTEXITCODE -eq 0) "Installer rejected real native zero." }
            else { Assert-Fixture ($null -ne $failure -and $failure.Data["NativeExitCode"] -eq $expected) "Installer lost a real nonzero native exit." }
            Assert-Fixture ($LASTEXITCODE -eq 97) "Installer fixture caller shadow was overwritten."
        }
        $wixNative = Join-Path $FixtureDirectory "fixture-wix-exit.cmd"
        [IO.File]::WriteAllText($wixNative, "@echo WixToolset.Util.wixext 5.0.2" + [Environment]::NewLine + "@exit /b 23" + [Environment]::NewLine, [Text.Encoding]::ASCII)
        $wixFailure = $null
        try { Get-WixExtensions -WixPath $wixNative | Out-Null } catch { $wixFailure = $_.Exception }
        Assert-Fixture ($null -ne $wixFailure -and $wixFailure.Data["NativeExitCode"] -eq 23 -and $wixFailure.Data["NativeOutput"].Contains("WixToolset.Util.wixext")) "Installer WiX inventory consumed the caller shadow instead of the native failure."
        function Invoke-FixtureInstallerWithoutNativeExit { Write-Output "no native process" }
        foreach ($operation in @("native", "wix")) {
            $missingExitFailure = $null
            try {
                if ($operation -ceq "native") { Invoke-Native -FilePath "Invoke-FixtureInstallerWithoutNativeExit" -ArgumentList @("fixture") -Description "installer no native exit" | Out-Null }
                else { Get-WixExtensions -WixPath "Invoke-FixtureInstallerWithoutNativeExit" | Out-Null }
            } catch { $missingExitFailure = $_.Exception }
            Assert-Fixture ($null -ne $missingExitFailure -and $null -eq $missingExitFailure.Data["NativeExitCode"] -and $missingExitFailure.Data["FailureClassification"] -ceq "packaging-native-launch-failure") "Installer accepted an invocation without a native exit."
        }
    } (Join-Path $RepositoryRoot "tools/bootstrap/install-packaging-prerequisites.ps1") $pwsh $fixtureRoot

    Write-Host "==> Fake-first pre-write log ancestry guard"
    $guardTokens = $null; $guardErrors = $null
    $guardAst = [Management.Automation.Language.Parser]::ParseFile((Join-Path $RepositoryRoot "tools/agent/repository-generated-state.ps1"), [ref]$guardTokens, [ref]$guardErrors)
    $guardDefinition = @($guardAst.FindAll({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq "Assert-VerificationLogDirectoryOutsideGeneratedState" }, $true))
    $startDefinition = @($ast.FindAll({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq "Start-BootstrapDiagnostics" }, $true))
    Assert-Fixture ($guardErrors.Count -eq 0 -and $guardDefinition.Count -eq 1 -and $startDefinition.Count -eq 1) "Expected actual shared guard/bootstrap initialization definitions."
    $guardScope = New-Module -ScriptBlock {
        param($GuardText, $StartText, $Root)
        $script:RepositoryRoot = $Root
        $script:RunId = "fake-run"
        $script:LinkedPath = Join-Path $Root "artifacts/fixture-linked-logs"
        $script:WriteAttempts = 0
        function Get-Item {
            param($LiteralPath, [switch]$Force, $ErrorAction)
            if ([IO.Path]::GetFullPath($LiteralPath).Equals([IO.Path]::GetFullPath($script:LinkedPath), [StringComparison]::OrdinalIgnoreCase)) {
                return [pscustomobject]@{ Attributes = [IO.FileAttributes]::ReparsePoint }
            }
            throw [Management.Automation.ItemNotFoundException]::new("Pure fixture: path absent.")
        }
        function New-Item { $script:WriteAttempts++; throw "PREWRITE_GUARD_BYPASSED" }
        function Start-Transcript { $script:WriteAttempts++; throw "PREWRITE_GUARD_BYPASSED" }
        . ([scriptblock]::Create($GuardText))
        . ([scriptblock]::Create($StartText))
        # New-Module otherwise exports these fake cmdlets into the caller.
        Export-ModuleMember -Function @() -Alias @()
    } -ArgumentList $guardDefinition[0].Extent.Text, $startDefinition[0].Extent.Text, $RepositoryRoot
    Assert-Fixture ((Get-Command Get-Item).CommandType -eq [Management.Automation.CommandTypes]::Cmdlet -and
        (Get-Command New-Item).CommandType -eq [Management.Automation.CommandTypes]::Cmdlet -and
        (Get-Command Start-Transcript).CommandType -eq [Management.Automation.CommandTypes]::Cmdlet) "Pure guard fixture leaked fake commands into its caller."
    foreach ($blocked in @(
        [pscustomobject]@{ Path = (Join-Path $RepositoryRoot "build/fixture-logs"); Pattern = "outside generated-state" },
        [pscustomobject]@{ Path = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-generated-state/fixture-logs"); Pattern = "backup tree" },
        [pscustomobject]@{ Path = (Join-Path $RepositoryRoot "artifacts/fixture-linked-logs"); Pattern = "linked repository/log ancestry" }
    )) {
        $guardResult = & $guardScope {
            param($Requested)
            $script:RequestedLogDirectory = $Requested
            $script:LogDirectory = $null
            $script:WriteAttempts = 0
            $message = $null
            try { Start-BootstrapDiagnostics } catch { $message = $_.Exception.Message }
            [pscustomobject]@{ Message = $message; WriteAttempts = $script:WriteAttempts; LogDirectory = $script:LogDirectory }
        } $blocked.Path
        Assert-Fixture ($guardResult.Message -match $blocked.Pattern -and $guardResult.WriteAttempts -eq 0 -and $null -eq $guardResult.LogDirectory) "Rejected log path reached creation/transcript or remained available to finalization."
    }
    Remove-Module -ModuleInfo $guardScope

    Write-Host "==> Actual installer-to-bootstrap failure propagation with fake packaging module"
    # Copy unchanged entry scripts into a disposable tree. The ONLY replacement
    # is the packaging provider: it invokes the real installer's SDK probe against
    # a benign fixture command. No real inventory, Apply, network or install runs.
    $fakePackaging = @'
function Get-PackagingPrerequisiteInventory {
    param([scriptblock]$Probe)
    & $Probe "CompatibleSdk" (Join-Path $PSScriptRoot "fixture-native.cmd")
}
function Invoke-PackagingPrerequisiteWorkflow {
    param([switch]$Install, [scriptblock]$Inventory, [scriptblock]$Apply, [string]$ExpectedWixVersion, [string[]]$RequiredWixExtensions)
    & $Inventory
    throw "FIXTURE_MUST_FAIL_BEFORE_APPLY"
}
Export-ModuleMember -Function Get-PackagingPrerequisiteInventory, Invoke-PackagingPrerequisiteWorkflow
'@
    $driver = @'
$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $true
& (Join-Path $PSScriptRoot "tools/bootstrap/bootstrap-windows.ps1") -InstallPackagingPrerequisites -LogDirectory (Join-Path $PSScriptRoot "logs")
exit $LASTEXITCODE
'@
    foreach ($scenario in @("native-exit", "launch-failure")) {
        $fixtureRepository = Join-Path $fixtureRoot ("installer-provenance-" + $scenario)
        $bootstrapDirectory = Join-Path $fixtureRepository "tools/bootstrap"
        $agentDirectory = Join-Path $fixtureRepository "tools/agent"
        [void](New-Item -ItemType Directory -Path $bootstrapDirectory, $agentDirectory)
        foreach ($name in @("bootstrap-windows.ps1", "install-packaging-prerequisites.ps1", "workstation-prerequisites.psm1")) {
            Copy-Item -LiteralPath (Join-Path $RepositoryRoot ("tools/bootstrap/" + $name)) -Destination (Join-Path $bootstrapDirectory $name)
        }
        Copy-Item -LiteralPath (Join-Path $RepositoryRoot "tools/agent/repository-generated-state.ps1") -Destination (Join-Path $agentDirectory "repository-generated-state.ps1")
        [IO.File]::WriteAllText((Join-Path $bootstrapDirectory "packaging-prerequisites.psm1"), $fakePackaging, [Text.UTF8Encoding]::new($false))
        [IO.File]::WriteAllText((Join-Path $fixtureRepository "driver.ps1"), $driver, [Text.UTF8Encoding]::new($false))
        if ($scenario -ceq "native-exit") {
            [IO.File]::WriteAllText((Join-Path $bootstrapDirectory "fixture-native.cmd"), "@echo INSTALLER_NATIVE_FAILURE_SENTINEL 1>&2" + [Environment]::NewLine + "@exit /b 73" + [Environment]::NewLine, [Text.Encoding]::ASCII)
        }
        $PSNativeCommandUseErrorActionPreference = $false
        $global:LASTEXITCODE = $null
        & $pwsh -NoLogo -NoProfile -File (Join-Path $fixtureRepository "driver.ps1") *> (Join-Path $fixtureRepository "driver.log")
        $childExit = $global:LASTEXITCODE
        Assert-Fixture ($childExit -eq 1) "Actual bootstrap must fail after its actual installer fails."
        $runs = @(Get-ChildItem -LiteralPath (Join-Path $fixtureRepository "logs") -Directory)
        Assert-Fixture ($runs.Count -eq 1) "Actual bootstrap failed to create exactly one diagnostic run."
        $actualSummary = Get-Content -Raw -LiteralPath (Join-Path $runs[0].FullName "bootstrap-result.json") | ConvertFrom-Json
        Assert-Fixture ($actualSummary.Outcome -ceq "FAIL" -and $actualSummary.Failure.Stage -ceq ".NET SDK inventory" -and $actualSummary.Failure.InstallerReturnedNormally -eq $false -and $null -eq $actualSummary.Failure.InstallerExitCode) "Installer throw was flattened, mislabeled as a returned exit, or lost its stage."
        if ($scenario -ceq "native-exit") {
            Assert-Fixture ($actualSummary.Failure.NativeExitCode -eq 73 -and $actualSummary.Failure.Classification -ceq "explicit-packaging-installation-failure" -and $actualSummary.Failure.NativeOutput.Contains("INSTALLER_NATIVE_FAILURE_SENTINEL")) "Native code/output/classification did not survive real installer catch and bootstrap handler."
        } else {
            Assert-Fixture ($null -eq $actualSummary.Failure.NativeExitCode -and $actualSummary.Failure.Classification -ceq "packaging-native-launch-failure") "A launch failure invented a native exit code."
        }
        Assert-Fixture ($actualSummary.Diagnostics.TranscriptStopped -and $actualSummary.NativeCommands.Count -eq 0) "Fixture escaped installer-only scope or required diagnostic finalization failed."
    }
    Write-Host "==> Fake-first required diagnostic finalization"
    $capture = [pscustomobject]@{ Json = $null; Path = $null; Stops = 0 }
    $saveFake = { param($Path, $Json) $capture.Path = $Path; $capture.Json = $Json }.GetNewClosure()
    $stopFake = { $capture.Stops++ }.GetNewClosure()
    $summary = [ordered]@{ Outcome = "PASS"; Failure = $null }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $true -StopTranscript $stopFake -SaveSummary $saveFake
    Assert-Fixture ($finalized.Outcome -eq "PASS" -and $finalized.SummarySaved -and $capture.Stops -eq 1) "Successful required diagnostic finalization did not pass."
    Assert-Fixture (($capture.Json | ConvertFrom-Json).Diagnostics.TranscriptStopped) "Summary did not record transcript closure."

    $summary = [ordered]@{ Outcome = "PASS"; Failure = $null }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $true -StopTranscript { throw "STOP_TRANSCRIPT_SENTINEL" } -SaveSummary $saveFake
    $saved = $capture.Json | ConvertFrom-Json
    Assert-Fixture ($finalized.Outcome -eq "FAIL" -and $finalized.SummarySaved -and $saved.Outcome -eq "FAIL" -and $saved.Failure.Classification -eq "diagnostic-finalization-failure") "Transcript failure was converted to success or omitted from saved summary."

    $summary = [ordered]@{ Outcome = "FAIL"; Failure = [ordered]@{ Message = "PRIMARY_INSTALLER_FAILURE"; Classification = "explicit-packaging-installation-failure"; NativeExitCode = 23 } }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $true -StopTranscript { throw "STOP_TRANSCRIPT_SENTINEL" } -SaveSummary { param($Path,$Json) throw "SUMMARY_WRITE_SENTINEL" }
    Assert-Fixture ($finalized.Outcome -eq "FAIL" -and -not $finalized.SummarySaved -and $finalized.FinalizationErrors.Count -eq 2) "Both required diagnostic errors were not retained."
    Assert-Fixture ($finalized.Failure.NativeExitCode -eq 23 -and $finalized.Failure.Message -ceq "PRIMARY_INSTALLER_FAILURE") "Diagnostic cleanup masked primary installer error/code."

    $summary = [ordered]@{ Outcome = "PASS"; Failure = $null }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $true -StopTranscript {} -SaveSummary { param($Path,$Json) throw "SUMMARY_WRITE_SENTINEL" }
    Assert-Fixture ($finalized.Outcome -eq "FAIL" -and -not $finalized.SummarySaved -and $finalized.Failure.Message -match "SUMMARY_WRITE_SENTINEL") "Required summary-write failure preserved a success outcome."
    $summary = [ordered]@{ Outcome = "PASS"; Failure = $null }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $false -SaveSummary $saveFake
    Assert-Fixture ($finalized.Outcome -eq "FAIL" -and ($capture.Json | ConvertFrom-Json).Outcome -eq "FAIL") "Missing required transcript produced successful evidence."

    $summary = [ordered]@{ Outcome = "PASS"; Failure = $null }
    $finalized = Complete-BootstrapDiagnostics -Summary $summary -LogDirectory $fixtureRoot -TranscriptStarted $true -StopTranscript {}
    Assert-Fixture ($finalized.SummarySaved -and (Test-Path -LiteralPath $finalized.SummaryPath) -and -not (Test-Path -LiteralPath ($finalized.SummaryPath + ".pending"))) "Default summary publisher left incomplete evidence instead of an atomic final file."
    Assert-Fixture ((Get-Content -Raw -LiteralPath $finalized.SummaryPath | ConvertFrom-Json).Outcome -eq "PASS") "Published summary differs from the finalized outcome."

    $LogDirectory = Join-Path $fixtureRoot "missing-log-parent"
    $nativeLogFailure = $null
    try { Invoke-NativeResult -Command $pwsh -ArgumentList @("-NoLogo", "-NoProfile", "-Command", "Write-Output 'fixture-native-failure'; exit 47") -Description "unwritable log fixture" | Out-Null }
    catch { $nativeLogFailure = $_.Exception }
    Assert-Fixture ($null -ne $nativeLogFailure -and $nativeLogFailure.Data["NativeExitCode"] -eq 47 -and $nativeLogFailure.Data["FailureClassification"] -eq "diagnostic-write-failure") "Native log failure lost native exit/classification."
    Assert-Fixture ($NativeRecords[-1].ExitCode -eq 47 -and -not $NativeRecords[-1].LogSaved -and $nativeLogFailure.Data["NativeOutput"].Contains("fixture-native-failure")) "Native metadata was dropped when its log write failed."
    $global:LASTEXITCODE = 83
    $doubleFailure = $null
    try { Invoke-NativeResult -Command $absentCommand -ArgumentList @("--fixture") -Description "absent command with unwritable log fixture" | Out-Null }
    catch { $doubleFailure = $_.Exception }
    Assert-Fixture ($null -ne $doubleFailure -and $null -eq $doubleFailure.Data["NativeExitCode"] -and $doubleFailure.Data["FailureClassification"] -ceq "native-invocation-failure" -and $doubleFailure.Data["Stage"] -ceq "absent command with unwritable log fixture") "Diagnostic publication replaced the primary invocation error or invented a native exit."
    Assert-Fixture ($null -ne $doubleFailure.InnerException -and $doubleFailure.InnerException.ToString().Contains("absent-native-command.exe") -and $doubleFailure.Data["NativeOutput"].Contains("absent-native-command.exe")) "Double failure lost the original invocation exception/output."
    Assert-Fixture ($doubleFailure.Data["DiagnosticFailure"].Classification -ceq "diagnostic-write-failure" -and $doubleFailure.Data["DiagnosticFailure"].LogPath -ceq $NativeRecords[-1].LogPath -and -not [string]::IsNullOrWhiteSpace($doubleFailure.Data["DiagnosticFailure"].Message)) "Secondary log-publication failure was not retained separately."
    Assert-Fixture ($null -eq $NativeRecords[-1].ExitCode -and $NativeRecords[-1].InvocationFailed -and -not $NativeRecords[-1].LogSaved -and $NativeRecords[-1].DiagnosticFailure.Classification -ceq "diagnostic-write-failure" -and $NativeRecords[-1].InvocationError.Contains("absent-native-command.exe")) "Native record failed to preserve both invocation and publication failures."
    $LogDirectory = $fixtureRoot
    Write-Host "Workstation prerequisite fixtures passed: $script:Checks assertions."
} catch {
    Write-Error $_.Exception.Message -ErrorAction Continue
    exit 1
} finally {
    if ($null -ne $fixtureRoot -and (Test-Path -LiteralPath $fixtureRoot)) {
        $resolved = [IO.Path]::GetFullPath($fixtureRoot)
        $tempPrefix = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
        if (-not $resolved.StartsWith($tempPrefix, [StringComparison]::OrdinalIgnoreCase) -or [IO.Path]::GetFileName($resolved) -notlike "geocedg-bootstrap-diagnostics-*") {
            throw "Refusing to remove unexpected fixture directory: $resolved"
        }
        $ancestor = $resolved
        while ($ancestor.StartsWith($tempPrefix, [StringComparison]::OrdinalIgnoreCase) -or $ancestor.Equals($tempPrefix.TrimEnd('\', '/'), [StringComparison]::OrdinalIgnoreCase)) {
            if ((Test-Path -LiteralPath $ancestor) -and ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw "Refusing to remove linked fixture ancestry: $ancestor" }
            $ancestor = Split-Path -Parent $ancestor
        }
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}
exit 0

#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) "geocedg-verification-infrastructure"),
    [switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "../.."))
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$pwshCommand = Join-Path $PSHOME $(if ($IsWindows) { "pwsh.exe" } else { "pwsh" })
if (-not (Test-Path -LiteralPath $pwshCommand -PathType Leaf)) {
    throw "The current PowerShell host executable is unavailable: $pwshCommand"
}
$results = [Collections.Generic.List[object]]::new()
$failure = $null
[void](New-Item -ItemType Directory -Path $LogDirectory -Force)

try {
    # Separate processes keep fixture module resets and injected functions out of
    # any top-level verification module. There is no Gradle/Java/network work here.
    $fixtures = @(
        [ordered]@{
            Name = "verification-runtime"
            Script = "tests/verification-runtime.Tests.ps1"
            PathParameter = "-ModulePath"
            Source = "verification-runtime.psm1"
        },
        [ordered]@{
            Name = "generated-state"
            Script = "tests/generated-state.tests.ps1"
            PathParameter = "-HelperPath"
            Source = "repository-generated-state.ps1"
        }
    )
    foreach ($fixture in $fixtures) {
        $logPath = Join-Path $LogDirectory ($fixture.Name + ".log")
        $watch = [Diagnostics.Stopwatch]::StartNew()
        if (-not $Quiet) { Write-Host "==> $($fixture.Name) fake-first operational fixtures" }
        $arguments = @(
            "-NoProfile", "-File", (Join-Path $PSScriptRoot $fixture.Script),
            $fixture.PathParameter, (Join-Path $PSScriptRoot $fixture.Source),
            "-LogDirectory", (Join-Path $LogDirectory $fixture.Name)
        )
        $global:LASTEXITCODE = $null
        & $pwshCommand @arguments *>&1 | Tee-Object -FilePath $logPath |
            ForEach-Object { if (-not $Quiet) { Write-Host $_ } }
        $code = $global:LASTEXITCODE
        $watch.Stop()
        $results.Add([ordered]@{
            fixture = $fixture.Name
            command = $pwshCommand
            arguments = $arguments
            exitCode = $code
            elapsedSeconds = [math]::Round($watch.Elapsed.TotalSeconds, 3)
            logPath = $logPath
            evidenceKind = "FAKE_FIRST_OPERATIONAL_CONTRACT"
        })
        if ($null -eq $code) { throw "$($fixture.Name) fixtures returned without a captured native exit; log: $logPath" }
        if ($code -ne 0) { throw "$($fixture.Name) fixtures failed with exit $code; log: $logPath" }
    }
} catch {
    $failure = $_.Exception.Message
}

$summary = [ordered]@{
    schemaVersion = 1
    state = $(if ($failure) { "FAILED" } else { "PASS_FAKE_FIRST_OPERATIONAL_ONLY" })
    fixtures = @($results)
    productRuntimeExecuted = $false
    authorApproved = $false
    failure = $failure
}
[IO.File]::WriteAllText((Join-Path $LogDirectory "verification-infrastructure.json"),
    (($summary | ConvertTo-Json -Depth 20).Replace("`r`n", "`n") + "`n"),
    [Text.UTF8Encoding]::new($false))
if ($failure) {
    Write-Error -Message $failure -ErrorAction Continue
    exit 1
}
if (-not $Quiet) { Write-Host "Verification-infrastructure fixtures passed; no product-runtime claim." }
exit 0

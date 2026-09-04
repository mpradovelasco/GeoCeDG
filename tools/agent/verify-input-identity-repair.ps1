#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$LogDirectory,
    [string]$PolicyPath = (Join-Path $PSScriptRoot '../../geocedg/validation/operations/input-identity-repair-policy.json'),
    [string]$CandidateCommit
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$RepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
. (Join-Path $PSScriptRoot 'verification-repair-equivalence.ps1')
. (Join-Path $PSScriptRoot 'repository-generated-state.ps1')
Assert-VerificationLogDirectoryOutsideGeneratedState -RepositoryRoot $RepositoryRoot -LogDirectory $LogDirectory
$summaryPath = Join-Path $LogDirectory 'canonical-summary.json'
if (Test-Path -LiteralPath $LogDirectory) { throw 'Use a fresh identity-repair verification log directory.' }
$relativeLog = [IO.Path]::GetRelativePath($RepositoryRoot, $LogDirectory).Replace('\', '/')
if (-not $relativeLog.StartsWith('../', [StringComparison]::Ordinal)) {
    [void](Invoke-GeoCeDGRepairGit $RepositoryRoot @('check-ignore', '--quiet', '--', "$relativeLog/probe.log"))
}
[void][IO.Directory]::CreateDirectory($LogDirectory)
$runtime = Import-Module (Join-Path $PSScriptRoot 'verification-runtime.psm1') -PassThru
$before = & $runtime { param($root) Get-RawInputInventory $root } $RepositoryRoot
[void](Get-GeoCeDGRepairWorkingTree $RepositoryRoot)
$initialHead = (Invoke-GeoCeDGRepairGit $RepositoryRoot @('rev-parse', 'HEAD')).Trim()
$initialIndex = Get-GeoCeDGRepairTextHash (Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-files', '--stage', '-z'))
$initialStatus = Get-GeoCeDGRepairTextHash (Invoke-GeoCeDGRepairGit $RepositoryRoot @('status', '--porcelain=v1', '--untracked-files=all'))
$policyText = [IO.File]::ReadAllText([IO.Path]::GetFullPath($PolicyPath), [Text.UTF8Encoding]::new($false, $true))
$policy = ConvertFrom-Json -InputObject $policyText -Depth 100
if ($policy.schemaVersion -ne 1 -or $policy.kind -cne 'BOUNDED_INPUT_IDENTITY_REPAIR_POLICY') {
    throw 'Unsupported identity-repair policy.'
}

function New-GeoCeDGIdentityFixtureProcessStartInfo {
    param([Parameter(Mandatory)] [string]$ScriptPath,
        [Parameter(Mandatory)] [string]$OutputDirectory,
        [Parameter(Mandatory)] [string]$WorkingDirectory)
    $start = [Diagnostics.ProcessStartInfo]::new((Join-Path $PSHOME 'pwsh.exe'))
    if (-not (Test-Path -LiteralPath $start.FileName)) { $start.FileName = Join-Path $PSHOME 'pwsh' }
    $start.UseShellExecute = $false; $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true; $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false, $true)
    $start.StandardErrorEncoding = [Text.UTF8Encoding]::new($false, $true)
    $start.WorkingDirectory = $WorkingDirectory
    # A headless Windows child otherwise inherits OEM console decoding (850 on
    # the validated host), corrupting Git's UTF-8 paths in independent textual
    # fixture oracles. Only infrastructure child transport is normalized here.
    $scriptLiteral = "'" + [IO.Path]::GetFullPath($ScriptPath).Replace("'", "''") + "'"
    $logLiteral = "'" + [IO.Path]::GetFullPath($OutputDirectory).Replace("'", "''") + "'"
    $bootstrap = '$ErrorActionPreference = ''Stop''; ' +
        '[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false); ' +
        '$OutputEncoding = [Text.UTF8Encoding]::new($false); ' +
        '$global:LASTEXITCODE = $null; & ' + $scriptLiteral + ' -LogDirectory ' + $logLiteral + '; ' +
        'if ($null -ne $global:LASTEXITCODE) { exit $global:LASTEXITCODE }'
    $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($bootstrap))
    foreach ($argument in @('-NoProfile', '-OutputFormat', 'Text', '-EncodedCommand', $encoded)) {
        $start.ArgumentList.Add($argument)
    }
    return $start
}

function Resolve-GeoCeDGIdentityFixtureSummary {
    param([Parameter(Mandatory)] [string]$OutputDirectory,
        [Parameter(Mandatory)] [string]$SummaryFileName)
    if ($SummaryFileName -cnotmatch '^[A-Za-z0-9][A-Za-z0-9._-]*\.json$') {
        throw 'Fixture summary must be one exact JSON leaf filename.'
    }
    $root = [IO.Path]::GetFullPath($OutputDirectory)
    function Assert-UnlinkedSummaryPath {
        param([string]$Path)
        $ancestors = [Collections.Generic.List[string]]::new()
        $cursor = $Path
        while (-not [string]::IsNullOrWhiteSpace($cursor)) {
            $ancestors.Add($cursor)
            $cursor = Split-Path -Parent $cursor
        }
        for ($index = $ancestors.Count - 1; $index -ge 0; $index--) {
            $item = Get-Item -LiteralPath $ancestors[$index] -Force -ErrorAction Stop
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw 'Fixture summary refuses reparse-point ancestry or content.'
            }
        }
    }
    Assert-UnlinkedSummaryPath $root
    if (-not (Get-Item -LiteralPath $root -Force).PSIsContainer) {
        throw 'Fixture summary output root is not a directory.'
    }
    # Existing suites publish either directly or in one uniquely generated run
    # directory. Never recursively search their retained nested fixture trees.
    $directories = @($root) + @(Get-ChildItem -LiteralPath $root -Directory -Force |
        Where-Object Name -CMatch '^[0-9a-f]{32}$' | ForEach-Object FullName)
    $matches = [Collections.Generic.List[string]]::new()
    foreach ($directory in $directories) {
        Assert-UnlinkedSummaryPath $directory
        $path = [IO.Path]::GetFullPath((Join-Path $directory $SummaryFileName))
        $relative = [IO.Path]::GetRelativePath($root, $path).Replace('\', '/')
        if ($relative.StartsWith('../', [StringComparison]::Ordinal) -or [IO.Path]::IsPathRooted($relative)) {
            throw 'Fixture summary escaped its output root.'
        }
        if (-not (Test-Path -LiteralPath $path)) { continue }
        Assert-UnlinkedSummaryPath $path
        if ((Get-Item -LiteralPath $path -Force).PSIsContainer) {
            throw 'Fixture summary is not a regular file.'
        }
        $matches.Add($path)
    }
    if ($matches.Count -ne 1) {
        throw "Fixture summary must resolve uniquely; found $($matches.Count) matches for $SummaryFileName."
    }
    return $matches[0]
}

function Invoke-IdentityFixtureSuite {
    param([string]$Name, [string]$Script, [string]$SummaryRelativePath)
    $outputDirectory = Join-Path $LogDirectory $Name
    $start = New-GeoCeDGIdentityFixtureProcessStartInfo -ScriptPath (Join-Path $RepositoryRoot $Script) `
        -OutputDirectory $outputDirectory -WorkingDirectory $RepositoryRoot
    $process = [Diagnostics.Process]::new(); $process.StartInfo = $start
    try {
        [void]$process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $code = $process.ExitCode
        $output = $stdout.GetAwaiter().GetResult()
        $errors = $stderr.GetAwaiter().GetResult()
        [IO.File]::WriteAllText((Join-Path $LogDirectory "$Name.log"), $output + $errors,
            [Text.UTF8Encoding]::new($false))
        if ($code -ne 0) { throw "Identity-repair suite $Name failed with exit $code. See $Name.log." }
        $path = Resolve-GeoCeDGIdentityFixtureSummary -OutputDirectory $outputDirectory `
            -SummaryFileName $SummaryRelativePath
        $document = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json -Depth 100
        $projection = if ($Name -ceq 'repository-identity') {
            [ordered]@{ suite = $Name; tests = $document.total; passed = $document.passed
                failed = $document.total - $document.passed; results = @($document.cases | ForEach-Object {
                    [ordered]@{ name = $_.name; status = $_.outcome }
                }) }
        } elseif ($Name -ceq 'generated-state') {
            if ($document.status -cne 'PASS' -or @($document.cleanup_errors).Count -ne 0) {
                throw 'Generated-state fixture cleanup or assertions failed.'
            }
            [ordered]@{ suite = $Name; tests = @($document.cases).Count
                passed = @($document.cases | Where-Object Outcome -CEQ 'PASS').Count
                failed = @($document.cases | Where-Object Outcome -CNE 'PASS').Count
                results = @($document.cases | ForEach-Object { [ordered]@{ name = $_.Name; status = $_.Outcome } }) }
        } else {
            [ordered]@{ suite = $Name; tests = $document.tests; passed = $document.passed
                failed = $document.failed; results = @($document.results | ForEach-Object {
                    [ordered]@{ name = $_.name; status = $_.status }
                }) }
        }
        if ($projection.failed -ne 0 -or $projection.tests -le 0 -or
                $projection.tests -ne $projection.passed) { throw "Incomplete focused fixture evidence: $Name" }
        return $projection
    } finally { $process.Dispose() }
}

try {
    $suites = @(
        Invoke-IdentityFixtureSuite 'repository-identity' 'tools/agent/tests/repository-input-identity.Tests.ps1' 'canonical-summary.json'
        Invoke-IdentityFixtureSuite 'repair-equivalence' 'tools/agent/tests/verification-repair-equivalence.Tests.ps1' 'verification-repair-equivalence-tests.json'
        Invoke-IdentityFixtureSuite 'phase-lifecycle' 'tools/agent/tests/phase-lifecycle.Tests.ps1' 'phase-lifecycle-tests.json'
        Invoke-IdentityFixtureSuite 'verification-runtime' 'tools/agent/tests/verification-runtime.Tests.ps1' 'verification-runtime-tests.json'
        Invoke-IdentityFixtureSuite 'generated-state' 'tools/agent/tests/generated-state.tests.ps1' 'generated-state-tests.json'
    )
    foreach ($record in @($policy.repairPaths)) {
        if ([string]$record.path -cnotmatch '\.ps(m)?1$') { continue }
        $tokens = $null; $parseErrors = $null
        [void][Management.Automation.Language.Parser]::ParseFile((Join-Path $RepositoryRoot $record.path),
            [ref]$tokens, [ref]$parseErrors)
        if ($parseErrors.Count -ne 0) { throw "PowerShell parser rejected $($record.path)." }
    }
    $parameters = @{ RepositoryRoot = $RepositoryRoot
        ReviewedTechnicalCommit = [string]$policy.reviewedTechnicalCommit
        CloseoutCommit = [string]$policy.closeoutCommit
        CloseoutPaths = [string[]]@($policy.closeoutPaths)
        RepairPaths = @($policy.repairPaths) }
    if ($CandidateCommit) { $parameters.CandidateCommit = $CandidateCommit }
    else { $parameters.WorkingTree = $true }
    $comparison = Get-GeoCeDGVerificationRepairEquivalence @parameters
    [void](Get-GeoCeDGRepairWorkingTree $RepositoryRoot)
    foreach ($arguments in @(
        @('-c', 'core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol', 'diff', '--check'),
        @('-c', 'core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol', 'diff', '--cached', '--check')
    )) { [void](Invoke-GeoCeDGRepairGit $RepositoryRoot $arguments) }
    $after = & $runtime { param($root) Get-RawInputInventory $root } $RepositoryRoot
    if ($before.Sha256 -cne $after.Sha256 -or
            $initialHead -cne (Invoke-GeoCeDGRepairGit $RepositoryRoot @('rev-parse', 'HEAD')).Trim() -or
            $initialIndex -cne (Get-GeoCeDGRepairTextHash (Invoke-GeoCeDGRepairGit $RepositoryRoot @('ls-files', '--stage', '-z'))) -or
            $initialStatus -cne (Get-GeoCeDGRepairTextHash (Invoke-GeoCeDGRepairGit $RepositoryRoot @('status', '--porcelain=v1', '--untracked-files=all')))) {
        throw 'Input bytes or independent HEAD/index/status changed during the focused verification run.'
    }
    $summary = [ordered]@{
        schemaVersion = 1; kind = 'INPUT_IDENTITY_REPAIR_FOCUSED_INFRASTRUCTURE'
        reviewedTechnicalCommit = $policy.reviewedTechnicalCommit; closeoutCommit = $policy.closeoutCommit
        policyCanonicalLfSha256 = Get-GeoCeDGRepairTextHash ($policyText.Replace("`r`n", "`n"))
        suites = $suites; tests = ($suites.tests | Measure-Object -Sum).Sum
        passed = ($suites.passed | Measure-Object -Sum).Sum; failed = 0
        executionPlanEquivalent = $comparison.executionPlanEquivalent
        reviewedExecutionPlanSha256 = $comparison.reviewedExecutionPlanSha256
        candidateExecutionPlanSha256 = $comparison.candidateExecutionPlanSha256
        unchangedExecutionInputCount = $comparison.unchangedExecutionInputCount
        sameRunPhysicalInputsUnchanged = $true; parser = 'PASS'; whitespace = 'PASS'
        scientificExecutionRepeated = $false; heavyEvidenceReuseApprovedByThisRunAlone = $false
        authorApprovalInferred = $false
    }
    $json = (ConvertTo-Json -InputObject $summary -Depth 100).Replace("`r`n", "`n") + "`n"
    [IO.File]::WriteAllText($summaryPath, $json, [Text.UTF8Encoding]::new($false))
    $hash = Get-GeoCeDGRepairTextHash $json
    [IO.File]::WriteAllText((Join-Path $LogDirectory 'canonical-summary.sha256'), "$hash`n", [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText((Join-Path $LogDirectory 'execution-plan-proof.json'),
        ((ConvertTo-Json -InputObject $comparison -Depth 100).Replace("`r`n", "`n") + "`n"), [Text.UTF8Encoding]::new($false))
    Write-Host "Input identity repair focused authority: $($summary.passed)/$($summary.tests) PASS; SHA-256 $hash"
    Write-Host 'This run is infrastructure evidence, not a newly executed PHASE/COMPOSED/FULL or author approval.'
} finally { Remove-Module $runtime -Force }

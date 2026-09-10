#requires -Version 7.2
[CmdletBinding()]
param(
    [ValidateSet('INSPECT', 'AUDIT', 'READINESS', 'PREPARE', 'FINALIZE')]
    [string]$Action = 'INSPECT',
    [Alias('TechnicalCommit')]
    [Parameter(Mandatory)] [string]$CandidateCommit,
    [Alias('ReadinessReceiptPath')]
    [Parameter(Mandatory)] [string]$ReceiptPath,
    [string]$ApprovedCommit,
    [string]$ResultPath,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../..'),
    [string]$CloseoutMode,
    [string]$PolicyPath,
    [string]$TechnicalCampaignPath,
    [string]$AuthorApprovalPath,
    [string]$PreparationResultPath,
    [string]$CloseoutCommit
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = (Resolve-Path -LiteralPath ([IO.Path]::GetFullPath($RepositoryRoot)) `
    -ErrorAction Stop).Path
$git = (Get-Command git -CommandType Application -ErrorAction Stop |
    Select-Object -First 1).Path
Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot 'verification-receipt.psm1') -Force

if ($Action -cin @('PREPARE', 'FINALIZE')) {
    throw "$Action mutation is retired. Closeout performs identity inspection only."
}
foreach ($legacy in @($CloseoutMode, $PolicyPath, $TechnicalCampaignPath,
        $AuthorApprovalPath, $PreparationResultPath, $CloseoutCommit)) {
    if (-not [string]::IsNullOrWhiteSpace($legacy)) {
        throw 'Legacy policy, mutation and approval arguments are not accepted by identity-only closeout.'
    }
}
function Invoke-GitRead {
    param([string[]]$Arguments)
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $git
    $start.WorkingDirectory = $repository
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = [Text.UTF8Encoding]::new($false)
    $start.StandardErrorEncoding = [Text.UTF8Encoding]::new($false)
    foreach ($argument in @('-C', $repository) + $Arguments) {
        [void]$start.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    try {
        if (-not $process.Start()) { throw 'Git inspection did not start.' }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) { throw "Git inspection failed: $stderr" }
        return $stdout.Trim()
    } finally { $process.Dispose() }
}

function Resolve-ExternalResultPath {
    param([Parameter(Mandatory)] [string]$Path)

    $full = [IO.Path]::GetFullPath($Path)
    $cursor = $full
    while (-not (Test-Path -LiteralPath $cursor)) {
        $parent = [IO.Path]::GetDirectoryName($cursor)
        if ([string]::IsNullOrWhiteSpace($parent) -or $parent -ceq $cursor) {
            throw "Cannot resolve an existing ancestor for closeout result path: $full"
        }
        $cursor = $parent
    }
    $resolvedAncestor = (Resolve-Path -LiteralPath $cursor -ErrorAction Stop).Path
    $relative = [IO.Path]::GetRelativePath($cursor, $full)
    $resolved = [IO.Path]::GetFullPath((Join-Path $resolvedAncestor $relative))
    $comparison = if ($IsWindows) {
        [StringComparison]::OrdinalIgnoreCase
    } else {
        [StringComparison]::Ordinal
    }
    $root = $repository.TrimEnd([IO.Path]::DirectorySeparatorChar,
        [IO.Path]::AltDirectorySeparatorChar)
    $prefix = $root + [IO.Path]::DirectorySeparatorChar
    if ($resolved.Equals($root, $comparison) -or
            $resolved.StartsWith($prefix, $comparison)) {
        throw 'Closeout result output must remain outside the inspected repository.'
    }
    return $full
}

$candidate = Invoke-GitRead @('rev-parse', '--verify', "$CandidateCommit^{commit}")
$tree = Invoke-GitRead @('rev-parse', '--verify', "$candidate^{tree}")
$receipt = Read-VerificationJson ([IO.Path]::GetFullPath($ReceiptPath))
[void](Assert-VerificationAcceptanceReceipt $receipt)
$mismatches = [Collections.Generic.List[string]]::new()
if ([string]$receipt.candidate_commit -cne $candidate) { $mismatches.Add('candidate_commit') }
if ([string]$receipt.candidate_tree -cne $tree) { $mismatches.Add('candidate_tree') }
if (-not [string]::IsNullOrWhiteSpace($ApprovedCommit) -and
        $candidate -cne (Invoke-GitRead @('rev-parse', '--verify', "$ApprovedCommit^{commit}"))) {
    $mismatches.Add('approved_commit')
}
$result = [ordered]@{
    schema_version = 1
    operation = 'IDENTITY_ONLY_CLOSEOUT'
    candidate_commit = $candidate
    candidate_tree = $tree
    receipt_id = [string]$receipt.receipt_id
    receipt_path = [IO.Path]::GetFullPath($ReceiptPath)
    approved_commit = $(if ([string]::IsNullOrWhiteSpace($ApprovedCommit)) {
        $null
    } else { $ApprovedCommit })
    identity_confirmed = $mismatches.Count -eq 0
    mismatches = [string[]]$mismatches.ToArray()
    verification_executed = $false
    repository_mutated = $false
    author_approval_recorded = $false
    publication_performed = $false
}
if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
    $externalResultPath = Resolve-ExternalResultPath -Path $ResultPath
    [void](Write-VerificationAtomicJson -Path $externalResultPath -Value $result)
}
if (-not $result.identity_confirmed) {
    Write-Error ("Closeout identity mismatch: " + ($result.mismatches -join ', ')) -ErrorAction Continue
    exit 3
}
$result | ConvertTo-Json -Depth 10
exit 0

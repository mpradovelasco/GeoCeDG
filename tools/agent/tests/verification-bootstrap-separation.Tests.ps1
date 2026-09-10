#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$bootstrapPath = Join-Path $repository 'tools/bootstrap/bootstrap-windows.ps1'
$source = [IO.File]::ReadAllText($bootstrapPath, [Text.UTF8Encoding]::new($false, $true))
$tokens = $null
$errors = $null
$ast = [Management.Automation.Language.Parser]::ParseInput(
    $source, $bootstrapPath, [ref]$tokens, [ref]$errors)
$script:Cases = 0
$script:Assertions = 0

function Assert-Case([bool]$Condition, [string]$Message) {
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}
function Invoke-Case([string]$Name, [scriptblock]$Body) {
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}

Invoke-Case 'bootstrap source parses and retains preparation entry points' {
    Assert-Case ($errors.Count -eq 0) 'Bootstrap has PowerShell parse errors.'
    foreach ($required in @('Get-WorkstationRequirements',
            'Invoke-WorkstationPrerequisiteCheck', 'Pinned refs and tags',
            'Preparation boundary')) {
        Assert-Case $source.Contains($required, [StringComparison]::Ordinal) "Bootstrap lost prerequisite behavior: $required"
    }
}

Invoke-Case 'bootstrap command graph has no acceptance or closeout edge' {
    $forbidden = @(
        'verify.ps1', 'verify-operational.ps1',
        'verify-verification-infrastructure.ps1', 'phase-closeout.ps1',
        '-Level COMPOSED', '-Level FULL',
        '-Profile FINAL', '-Profile PHASE', '-Profile OPERATIONAL'
    )
    $commands = @($ast.FindAll({
        param($node)
        $node -is [Management.Automation.Language.CommandAst]
    }, $true) | Where-Object {
        $_.GetCommandName() -cnotin @('Write-Host', 'Write-Warning', 'Write-Verbose')
    })
    foreach ($commandAst in $commands) {
        $command = $commandAst.Extent.Text
        foreach ($value in $forbidden) {
            Assert-Case (-not $command.Contains($value, [StringComparison]::OrdinalIgnoreCase)) "Bootstrap has a forbidden executable edge: $command"
        }
    }
    Assert-Case (-not $source.Contains('$Verifier', [StringComparison]::Ordinal)) 'Bootstrap retains a verifier command variable.'
    Assert-Case (-not $source.Contains('Invoke-VerificationSupervisor',
        [StringComparison]::Ordinal)) 'Bootstrap calls the typed supervisor.'
}

Invoke-Case 'legacy execution switches fail before prerequisite work' {
    $guard = 'if ($RunBenchmarks -or $LaunchDesktop)'
    $guardIndex = $source.IndexOf($guard, [StringComparison]::Ordinal)
    $workIndex = $source.IndexOf('Write-Step "External commands"', [StringComparison]::Ordinal)
    Assert-Case ($guardIndex -ge 0 -and $guardIndex -lt $workIndex) 'RunBenchmarks or LaunchDesktop can reach preparation work.'
    Assert-Case $source.Contains('Bootstrap no longer runs benchmarks or the product.',
        [StringComparison]::Ordinal) 'Retired execution switches lack an explicit failure.'
}

Invoke-Case 'SkipBuild is compatibility-only and cannot launch verification' {
    $skipStatements = @($ast.FindAll({
        param($node)
        $node -is [Management.Automation.Language.IfStatementAst] -and
        @($node.Clauses | Where-Object {
                $_.Item1.Extent.Text.Trim() -ceq '$SkipBuild'
            }).Count -gt 0
    }, $true))
    Assert-Case ($skipStatements.Count -ge 1) 'SkipBuild compatibility behavior is absent.'
    foreach ($statement in $skipStatements) {
        Assert-Case (-not $statement.Extent.Text.Contains('&', [StringComparison]::Ordinal)) 'SkipBuild branch invokes another command.'
    }
    Assert-Case $source.Contains('bootstrap never compiles or launches verification',
        [StringComparison]::Ordinal) 'SkipBuild compatibility boundary is not explicit.'
}

Write-Host "verification-bootstrap-separation.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

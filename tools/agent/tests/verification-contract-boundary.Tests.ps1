#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-registry.psm1') -Force
Import-Module (Join-Path $PSScriptRoot '../verification-supervisor.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$pwsh = (Get-Process -Id $PID).Path
$git = (Get-Command git -CommandType Application -ErrorAction Stop | Select-Object -First 1).Path

function Assert-Case {
    param([bool]$Condition, [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw $Message }
}

function Invoke-Case {
    param([string]$Name, [scriptblock]$Body)
    $script:Cases++
    & $Body
    Write-Host "PASS: $Name"
}

function Invoke-Git {
    param([string]$Root, [string[]]$Arguments)
    $global:LASTEXITCODE = $null
    $output = & $git -C $Root @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Git failed: $($output -join [Environment]::NewLine)" }
    return $output
}

function New-NodeBase {
    param([string]$Id, [string]$Kind, [string]$Contract)
    $node = [ordered]@{
        check_id = $Id
        node_kind = $Kind
        tier = 'STATIC'
        command = [ordered]@{ kind = 'BUILTIN_PROJECTION'; identity = $Id; projection = 'STRUCTURED_CONTRACT' }
        argv = [string[]]@()
        working_directory = '.'
        dependencies = [string[]]@('repository.producer')
        platforms = [string[]]@('WINDOWS', 'LINUX', 'MACOS')
        impact_paths = [string[]]@('.')
        environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
        resource_locks = [string[]]@()
        side_effects = [string[]]@('NONE')
        output_adapter = 'PROJECTION_STRUCTURED_CONTRACT_V1'
        required_for_profiles = [string[]]@('STATIC')
        producer_dependency = 'repository.producer'
        contract_class = $Contract
    }
    return $node
}

Invoke-Case 'real repository producer separates safety acceptance and style diagnostic' {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
    $root = Join-Path $tempBase ('geocedg-contract-boundary-' + [guid]::NewGuid().ToString('N'))
    $repo = Join-Path $root 'repo'
    $output = Join-Path $root 'evidence'
    [void][IO.Directory]::CreateDirectory($repo)
    $marker = Join-Path $root '.fixture-owner'
    [IO.File]::WriteAllText($marker, 'verification-contract-boundary', [Text.UTF8Encoding]::new($false))
    try {
        [void](Invoke-Git $repo @('init', '-q'))
        [void](Invoke-Git $repo @('config', 'user.name', 'Fixture'))
        [void](Invoke-Git $repo @('config', 'user.email', 'fixture@example.invalid'))
        [IO.File]::WriteAllText((Join-Path $repo 'tracked.txt'), "clean`n", [Text.UTF8Encoding]::new($false))
        [void](Invoke-Git $repo @('add', 'tracked.txt'))
        [void](Invoke-Git $repo @('commit', '-q', '-m', 'fixture'))
        [IO.File]::WriteAllText((Join-Path $repo 'tracked.txt'), "style finding   `n", [Text.UTF8Encoding]::new($false))

        $producer = [ordered]@{
            check_id = 'repository.producer'
            node_kind = 'PROCESS_PRODUCER'
            tier = 'STATIC'
            command = [ordered]@{ kind = 'PROCESS'; identity = 'repository-safety-test'; executable = $pwsh }
            argv = @('-NoLogo', '-NoProfile', '-File', (Join-Path $repositoryRoot 'tools/agent/checks/repository-safety.ps1'), '-RepositoryRoot', '{repository_root}', '-SafetyResultPath', '{evidence_REPOSITORY_SAFETY_RESULT}', '-DiagnosticResultPath', '{evidence_REPOSITORY_STYLE_RESULT}')
            working_directory = '.'
            dependencies = [string[]]@()
            platforms = [string[]]@('WINDOWS', 'LINUX', 'MACOS')
            impact_paths = [string[]]@('.')
            environment_contract = [ordered]@{ inherit = $true; variables = [ordered]@{} }
            resource_locks = [string[]]@('git-index-read')
            side_effects = [string[]]@('REPOSITORY_READ', 'TASK_OWNED_OUTPUT')
            output_adapter = 'RAW_PROCESS_V1'
            required_for_profiles = [string[]]@('STATIC')
            produces_evidence = [string[]]@('PROCESS_RESULT', 'REPOSITORY_SAFETY_RESULT', 'REPOSITORY_STYLE_RESULT')
        }
        $safety = New-NodeBase repository.boundary-safety EVIDENCE_PROJECTION SAFETY
        $safety.command.evidence_name = 'REPOSITORY_SAFETY_RESULT'
        $style = New-NodeBase repository.style EVIDENCE_PROJECTION STYLE_DIAGNOSTIC
        $style.command.evidence_name = 'REPOSITORY_STYLE_RESULT'
        $registry = [ordered]@{
            schema_version = 1
            registry_id = 'fixture.boundary'
            profiles = @([ordered]@{ profile_id = 'STATIC'; coverage_state = 'COMPLETE'; missing_check_ids = [string[]]@() })
            compatibility_mappings = [object[]]@()
            phase_selections = [object[]]@()
            nodes = @($producer, $safety, $style)
        }
        $head = ([string](Invoke-Git $repo @('rev-parse', 'HEAD'))).Trim()
        $tree = ([string](Invoke-Git $repo @('rev-parse', 'HEAD^{tree}'))).Trim()
        $identity = [pscustomobject]@{
            base_commit = $null; base_tree = $null; candidate_commit = $head;
            candidate_tree = $tree; environment_fingerprint = '9' * 64
        }
        $run = Invoke-VerificationSupervisor -Registry $registry -Profile STATIC `
            -RepositoryRoot $repo -OutputDirectory $output -Identity $identity
        Assert-Case ($run.report.process_producers[0].exit_code -ne 0) 'Style producer did not exercise nonzero legacy exit.'
        Assert-Case ($run.report.process_producers[0].PSObject.Properties.Name -cnotcontains 'contract_class') 'Producer acquired a verdict contract.'
        Assert-Case ($run.report.acceptance_results[0].status -ceq 'CONTRACT_SATISFIED') 'Repository safety projection failed.'
        Assert-Case ($run.report.diagnostic_findings[0].outcome -ceq 'DIAGNOSTIC_FINDING') 'Style finding was not projected diagnostically.'
        Assert-Case ($run.report.acceptance_verdict -ceq 'ACCEPTED' -and $run.report.coverage_verdict -ceq 'COMPLETE') 'Diagnostic finding altered acceptance.'
        Assert-Case ($run.exit_code -eq 0) 'Diagnostic finding altered campaign exit.'
    } finally {
        $resolved = [IO.Path]::GetFullPath($root)
        $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
        if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
                (Test-Path -LiteralPath $marker) -and
                [IO.File]::ReadAllText($marker) -ceq 'verification-contract-boundary') {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        } else {
            throw "Fixture cleanup refused unexpected path: $resolved"
        }
    }
}

Invoke-Case 'WORKSTATION is a single live installation leaf with no unrelated calls' {
    $registry = Read-VerificationJson (Join-Path $repositoryRoot 'geocedg/specs/operations/verification-registry.json')
    $plan = Resolve-VerificationRegistryPlan $registry WORKSTATION
    Assert-Case (@($plan.nodes).Count -eq 1) 'WORKSTATION contains more than one leaf.'
    Assert-Case ($plan.nodes[0].check_id -ceq 'workstation.live-safety') 'WORKSTATION does not select the live safety checker.'
    $argv = [string[]]$plan.nodes[0].argv
    Assert-Case (@($argv | Where-Object { $_ -match 'fixture|bootstrap|operational|governance|compile|test' }).Count -eq 0) 'WORKSTATION invokes unrelated verification.'
    $scriptText = [IO.File]::ReadAllText((Join-Path $repositoryRoot 'tools/agent/checks/workstation-safety.ps1'))
    Assert-Case (-not $scriptText.Contains('om_env')) 'WORKSTATION mentions the external environment.'
    Assert-Case (-not $scriptText.Contains('--%')) 'WORKSTATION uses stop-parsing syntax.'
    Assert-Case ($scriptText.Contains('org.gradle.java.installations.auto-download=false')) 'WORKSTATION does not disable toolchain download.'
    Assert-Case (-not $scriptText.Contains("'javaToolchains'")) 'WORKSTATION configures the Gradle build to discover toolchains.'
    Assert-Case (-not $scriptText.Contains('Invoke-NativeCapture ''gradle.wrapper''')) 'WORKSTATION executes the wrapper and could download Gradle.'
    Assert-Case ($scriptText.Contains("@('--version')")) 'WORKSTATION Conda probe is not read-only.'
    foreach ($forbiddenCondaArgument in @("'env'", "'create'", "'update'", "'remove'", "'prune'", "'rename'")) {
        Assert-Case (-not $scriptText.Contains($forbiddenCondaArgument)) `
            "WORKSTATION contains forbidden Conda argument $forbiddenCondaArgument."
    }
}

Write-Host "verification-contract-boundary.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

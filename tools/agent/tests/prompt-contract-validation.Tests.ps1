#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Import-Module (Join-Path $repository 'tools/agent/prompt-contract-parser.psm1') -Force
$catalogPath = Join-Path $repository 'geocedg/specs/operations/prompt-contracts.json'
$schemaPath = Join-Path $repository 'geocedg/specs/operations/prompt-contracts.schema.json'
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-prompt-contract-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'prompt-contract-validation',
    [Text.UTF8Encoding]::new($false))
$script:Cases = 0
$script:Assertions = 0

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

try {
    Invoke-Case 'schema and catalog are valid' {
        $json = [IO.File]::ReadAllText($catalogPath,
            [Text.UTF8Encoding]::new($false, $true))
        Assert-Case (Test-Json -Json $json -SchemaFile $schemaPath) 'Catalog failed its schema.'
        $catalog = Read-PromptContractCatalog $catalogPath
        Assert-Case (@($catalog.profiles).Count -eq 3) 'Prompt profiles differ.'
    }
    Invoke-Case 'all active prompt profiles are execution-safe' {
        $catalog = Read-PromptContractCatalog $catalogPath
        $results = Test-PromptContractCatalog $repository $catalog
        Assert-Case (@($results).Count -eq 3) 'Active prompt count differs.'
        Assert-Case (@($results | Where-Object { -not $_.execution_safe }).Count -eq 0) 'An active prompt is unsafe.'
    }
    Invoke-Case 'heading typography is advisory' {
        $catalog = Read-PromptContractCatalog $catalogPath
        $profile = @($catalog.profiles | Where-Object profile_id -CEQ 'task')[0]
        $source = Join-Path $repository '.github/prompts/tasks/task-template.prompt.md'
        $copy = Join-Path $root 'heading-advisory.md'
        $text = [IO.File]::ReadAllText($source).Replace(
            '# GeoCeDG task template', '## GeoCeDG task template')
        [IO.File]::WriteAllText($copy, $text, [Text.UTF8Encoding]::new($false))
        $result = Test-PromptContractDocument $copy $profile
        Assert-Case $result.execution_safe 'H1/H2 typography changed execution safety.'
        Assert-Case ($result.title_count -eq 0) 'Typography probe did not remove H1.'
    }
    Invoke-Case 'missing duplicate and order findings accumulate' {
        $catalog = Read-PromptContractCatalog $catalogPath
        $profile = @($catalog.profiles | Where-Object profile_id -CEQ 'task')[0]
        $copy = Join-Path $root 'multiple-findings.md'
        $text = @'
# Synthetic task
<!-- geocedg-field: objective -->
## Objective
<!-- geocedg-field: objective -->
## Repeated objective
<!-- geocedg-field: allowed_scope -->
## Allowed
<!-- geocedg-field: implementation_base -->
## Base
<!-- geocedg-field: forbidden_scope -->
## Forbidden
<!-- geocedg-field: required_checks -->
## Checks
<!-- geocedg-field: authorization_boundary -->
## Authorization
'@
        [IO.File]::WriteAllText($copy, $text, [Text.UTF8Encoding]::new($false))
        $result = Test-PromptContractDocument $copy $profile
        Assert-Case (-not $result.execution_safe) 'Malformed semantic fields were accepted.'
        Assert-Case ($result.missing_fields -ccontains 'publication_boundary') 'Missing field was not reported.'
        Assert-Case ($result.duplicate_fields -ccontains 'objective') 'Duplicate field was not reported.'
        Assert-Case ($result.out_of_order_fields.Count -gt 0) 'Out-of-order fields were not reported.'
    }
    Invoke-Case 'catalog semantic validation rejects duplicate profiles' {
        $catalog = Read-PromptContractCatalog $catalogPath
        $mutated = [ordered]@{
            '$schema' = $catalog.'$schema'
            schema_version = $catalog.schema_version
            profiles = @($catalog.profiles) + @($catalog.profiles[0])
            documents = @($catalog.documents)
        }
        $copy = Join-Path $root 'duplicate-profile.json'
        [IO.File]::WriteAllText($copy, (ConvertTo-Json $mutated -Depth 30),
            [Text.UTF8Encoding]::new($false))
        $rejected = $false
        try { [void](Read-PromptContractCatalog $copy) } catch { $rejected = $true }
        Assert-Case $rejected 'Duplicate prompt profile IDs were accepted.'
    }
    Invoke-Case 'schema rejects an unknown profile' {
        $catalog = [IO.File]::ReadAllText($catalogPath) |
            ConvertFrom-Json -Depth 30 -AsHashtable
        $catalog.profiles[0].profile_id = 'unknown'
        $json = ConvertTo-Json $catalog -Depth 30
        Assert-Case (-not (Test-Json -Json $json -SchemaFile $schemaPath -ErrorAction SilentlyContinue)) `
            'Schema accepted an unknown profile.'
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker) -and
            [IO.File]::ReadAllText($marker) -ceq 'prompt-contract-validation') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else { throw "Unsafe prompt fixture cleanup target: $resolved" }
}

Write-Host "prompt-contract-validation.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

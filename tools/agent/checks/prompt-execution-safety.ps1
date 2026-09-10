#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$catalogPath = Join-Path $repository 'geocedg/specs/operations/prompt-contracts.json'
$schemaPath = Join-Path $repository 'geocedg/specs/operations/prompt-contracts.schema.json'
$result = [ordered]@{
    contract_class = 'SAFETY'
    outcome = 'EVIDENCE_UNTRUSTED'
    cause = $null
    documents = @()
}
try {
    $text = [IO.File]::ReadAllText($catalogPath, [Text.UTF8Encoding]::new($false, $true))
    if (-not (Test-Json -Json $text -SchemaFile $schemaPath -ErrorAction Stop)) {
        throw 'Prompt contract catalog does not satisfy its schema.'
    }
    Import-Module (Join-Path $repository 'tools/agent/prompt-contract-parser.psm1') -Force
    $catalog = Read-PromptContractCatalog $catalogPath
    $documents = Test-PromptContractCatalog -RepositoryRoot $repository -Catalog $catalog
    $result.documents = [object[]]$documents
    $result.outcome = if (@($documents | Where-Object { -not $_.execution_safe }).Count -eq 0) {
        'CONTRACT_SATISFIED'
    } else { 'CONTRACT_VIOLATED' }
    if ($result.outcome -ceq 'CONTRACT_VIOLATED') {
        $result.cause = 'One or more active prompts lack required execution-safety fields.'
    }
} catch {
    $result.cause = $_.Exception.Message
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 50).Replace("`r`n", "`n") + "`n"),
    [Text.UTF8Encoding]::new($false))
if ($result.outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
if ($result.outcome -ceq 'CONTRACT_VIOLATED') { exit 2 }
exit 3

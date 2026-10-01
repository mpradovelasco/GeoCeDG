#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$RepositoryRoot,
    [Parameter(Mandatory)] [string]$ResultPath
)

# PRE-G9B-R6: schema conformance of the GGBScript capability matrix. Execution and
# structural completeness are proven by the JUnit identities the rows name.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$instance = Join-Path $repository 'geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json'
$schema = Join-Path $repository 'geocedg/specs/operations/ggbscript-capability-matrix.schema.json'
$result = [ordered]@{
    contract_class = 'SEMANTIC'
    semantic_domain = 'PRODUCT'
    outcome = 'EVIDENCE_UNTRUSTED'
    cause = $null
    contract_id = 'pre-g9b-r6.ggbscript-capability-matrix.schema'
}
try {
    Import-Module (Join-Path $repository 'tools/agent/verification-io.psm1') -Force
    $value = Read-VerificationJson -Path $instance
    try {
        [void](Assert-VerificationJsonSchema -Value $value -SchemaPath $schema)
        $result.outcome = 'CONTRACT_SATISFIED'
    } catch {
        $result.outcome = 'CONTRACT_VIOLATED'
        $result.cause = $_.Exception.Message
    }
} catch {
    $result.cause = $_.Exception.Message
}
$full = [IO.Path]::GetFullPath($ResultPath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $result -Depth 10).Replace("`r`n", "`n") + "`n"), $utf8)
if ($result.outcome -ceq 'CONTRACT_SATISFIED') { exit 0 }
if ($result.outcome -ceq 'CONTRACT_VIOLATED') { exit 1 }
exit 3

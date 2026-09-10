#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string]$EvidencePath,
    [Parameter(Mandatory)] [string]$InventoryPath,
    [Parameter(Mandatory)] [string]$SelectionId,
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '../../..'),
    [string]$OutputDirectory = (Join-Path ([IO.Path]::GetTempPath()) ('geocedg-gradle-test-' + [guid]::NewGuid().ToString('N'))),
    [switch]$TestDryRun
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
$repository = [IO.Path]::GetFullPath($RepositoryRoot)
$inventory = [IO.File]::ReadAllText([IO.Path]::GetFullPath($InventoryPath),
    [Text.UTF8Encoding]::new($false,$true)) | ConvertFrom-Json -Depth 100
$matches = @($inventory.selections | Where-Object selection_id -CEQ $SelectionId)
if ($matches.Count -ne 1) { throw "Inventory selection is not unique: $SelectionId" }
$selection = $matches[0]
$output = [IO.Path]::GetFullPath($OutputDirectory)
$junitRoot = Join-Path $output 'junit'
[void][IO.Directory]::CreateDirectory($junitRoot)
$initScript = Join-Path $output 'junit-output.init.gradle'
$gradlePath = $junitRoot.Replace('\','/')
$excludedTestFilters = [object[]]@()
if ($selection.PSObject.Properties['excluded_test_filters']) {
    $excludedTestFilters = [object[]]$selection.excluded_test_filters
}
$exclusionLines = [Collections.Generic.List[string]]::new()
foreach ($filter in $excludedTestFilters) {
    $escapedFilter = ([string]$filter).Replace('\', '\\').Replace("'", "\'")
    $exclusionLines.Add("            excludeTestsMatching '$escapedFilter'")
}
$filterBlock = if ($exclusionLines.Count -eq 0) { '' } else {
    "        filter {`n" + ($exclusionLines -join "`n") + "`n        }`n"
}
$init = @"
allprojects {
    tasks.withType(org.gradle.api.tasks.testing.Test).configureEach {
$filterBlock        reports.junitXml.required = true
        reports.junitXml.outputLocation = file('$gradlePath')
        reports.html.required = false
    }
}
"@
[IO.File]::WriteAllText($initScript, $init.Replace("`r`n","`n"), $utf8)
$arguments = [Collections.Generic.List[string]]::new()
foreach ($filter in [object[]]$selection.test_filters) {
    $arguments.Add('--tests'); $arguments.Add([string]$filter)
}
if ($TestDryRun) { $arguments.Add('--test-dry-run') }
$arguments.Add('--init-script'); $arguments.Add($initScript)
$commandProducer = Join-Path $PSScriptRoot 'gradle-command-evidence-producer.ps1'
$innerEvidence = Join-Path $output 'process-evidence.json'
& $commandProducer -EvidencePath $innerEvidence -GradleTask ([string]$selection.gradle_task) `
    -AdditionalArguments ([string[]]$arguments.ToArray()) `
    -RepositoryRoot $repository -OutputDirectory (Join-Path $output 'process')
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $innerEvidence -PathType Leaf)) {
    throw 'Neutral Gradle command producer did not preserve process evidence.'
}
$processEvidence = [IO.File]::ReadAllText($innerEvidence,
    [Text.UTF8Encoding]::new($false,$true)) | ConvertFrom-Json -Depth 100
$files = @()
if (Test-Path -LiteralPath $junitRoot -PathType Container) {
    $files = @(Get-ChildItem -LiteralPath $junitRoot -Recurse -File -Filter '*.xml' |
        Sort-Object FullName | ForEach-Object {
            [ordered]@{ path=$_.FullName; sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
        })
}
$evidence = [ordered]@{
    schema_version = 1; producer_kind = 'GRADLE_TEST'; selection_id = $SelectionId
    selection = $selection; completion_state = $processEvidence.completion_state
    completion_cause = $processEvidence.completion_cause; command = $processEvidence.command
    arguments = $processEvidence.arguments; working_directory = $processEvidence.working_directory
    environment = $processEvidence.environment; init_script = $initScript
    init_script_sha256 = (Get-FileHash $initScript -Algorithm SHA256).Hash.ToLowerInvariant()
    inner_exit_code = $processEvidence.inner_exit_code; stdout_log = $processEvidence.stdout_log
    stderr_log = $processEvidence.stderr_log; junit_files = [object[]]$files
    test_dry_run = [bool]$TestDryRun; duration_ms = $processEvidence.duration_ms
}
$full = [IO.Path]::GetFullPath($EvidencePath)
[void][IO.Directory]::CreateDirectory((Split-Path -Parent $full))
[IO.File]::WriteAllText($full, ((ConvertTo-Json $evidence -Depth 100).Replace("`r`n","`n")+"`n"), $utf8)
exit 0

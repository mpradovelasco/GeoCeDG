#requires -Version 7.2
[CmdletBinding()]
param([string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) (
    'geocedg-package-project-tests-' + [guid]::NewGuid().ToString('N'))))
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$realRepositoryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$builder = Join-Path $realRepositoryRoot 'tools/release/build-windows-package.ps1'
$tokens = $null; $parseErrors = $null
$syntax = [Management.Automation.Language.Parser]::ParseFile($builder, [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count -ne 0) { throw 'Cannot parse the canonical packaging builder.' }
$nativeFunction = @($syntax.FindAll({ param($node)
    $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -ceq 'Invoke-Native'
}, $true))
if ($nativeFunction.Count -ne 1) { throw 'Native invocation function must be unique.' }
. ([scriptblock]::Create($nativeFunction[0].Extent.Text))
$calls = @($syntax.FindAll({ param($node)
    $node -is [Management.Automation.Language.CommandAst] -and
        $node.GetCommandName() -ceq 'Invoke-Native' -and
        $node.Extent.Text -cmatch '-FilePath\s+\$GradleWrapper\b'
}, $true) | Sort-Object { $_.Extent.StartOffset })
if ($calls.Count -eq 0) { throw 'No Gradle invocation was tested.' }
$fixture = Join-Path ([IO.Path]::GetTempPath()) ('geocedg-120-package-root-' + [guid]::NewGuid().ToString('N'))
$RepositoryRoot = Join-Path $fixture 'intended project'
$foreign = Join-Path $fixture 'foreign project'
$GradleWrapper = Join-Path $fixture 'fake-gradle.ps1'
$ResolvedComponentsExporter = Join-Path $RepositoryRoot 'exporter.gradle'
$ResolvedComponentsBuildPath = Join-Path $RepositoryRoot 'resolved.json'
$taskPackageDefinition = Get-Content -LiteralPath (Join-Path $realRepositoryRoot 'packaging/windows/package.yml') -Raw |
    ConvertFrom-Json -Depth 30
$taskInstallTask = [string]$taskPackageDefinition.input.gradle_task
$rows = [Collections.Generic.List[object]]::new()
try {
    [void][IO.Directory]::CreateDirectory($RepositoryRoot)
    [void][IO.Directory]::CreateDirectory($foreign)
    # A command receiver models Gradle's project-dir/default-cwd semantics;
    # it does not read, compile or package any product and never changes cwd.
    [IO.File]::WriteAllText($GradleWrapper, @'
$selected = [IO.Path]::GetFullPath((Get-Location).Path)
for ($i = 0; $i -lt $args.Count; $i++) {
    if ($args[$i] -cin @('--project-dir', '-p')) {
        $i++
        if ($i -ge $args.Count) { throw 'Missing project argument.' }
        $selected = [IO.Path]::GetFullPath([string]$args[$i])
    }
}
[ordered]@{ selected_project = $selected; cwd = (Get-Location).Path; arguments = @($args) } |
    ConvertTo-Json -Depth 5 -Compress
exit 0
'@, [Text.UTF8Encoding]::new($false))
    Push-Location -LiteralPath $foreign
    try {
        foreach ($call in $calls) {
            $arrays = @($call.CommandElements | Where-Object { $_ -is [Management.Automation.Language.ArrayExpressionAst] })
            if ($arrays.Count -ne 1) { throw 'Gradle argument array is not uniquely inspectable.' }
            # Bind the task from the canonical profile without shadowing PowerShell's PROFILE variable.
            $argumentSource = $arrays[0].Extent.Text.Replace('[string]$profile.input.gradle_task', '[string]$taskInstallTask')
            $arguments = [string[]]@(& ([scriptblock]::Create($argumentSource)))
            $capture = @($call.CommandElements | Where-Object {
                $_ -is [Management.Automation.Language.CommandParameterAst] -and $_.ParameterName -ceq 'Capture'
            }).Count -ne 0
            $received = @(Invoke-Native -FilePath $GradleWrapper -ArgumentList $arguments -Description 'Project selection fixture' -Capture:$capture)
            $actual = ($received -join "`n") | ConvertFrom-Json -Depth 10
            $sameProject = [IO.Path]::GetFullPath([string]$actual.selected_project).Equals(
                [IO.Path]::GetFullPath($RepositoryRoot), [StringComparison]::OrdinalIgnoreCase)
            $sameCwd = (Get-Location).Path.Equals($foreign, [StringComparison]::OrdinalIgnoreCase)
            $sameInvocationCwd = [IO.Path]::GetFullPath([string]$actual.cwd).Equals(
                [IO.Path]::GetFullPath($foreign), [StringComparison]::OrdinalIgnoreCase)
            $rows.Add([ordered]@{ name = 'Gradle invocation ' + ($rows.Count + 1)
                source_line = $call.Extent.StartLineNumber; selected_project = $actual.selected_project
                invocation_cwd = $actual.cwd; arguments = $actual.arguments
                result = $(if ($sameProject -and $sameCwd -and $sameInvocationCwd) { 'PASS' } else { 'FAIL' }) })
        }
    } finally { Pop-Location }
    $failures = @($rows | Where-Object result -CNE 'PASS').Count
    [void][IO.Directory]::CreateDirectory([IO.Path]::GetFullPath($LogDirectory))
    $result = [ordered]@{ schemaVersion = 1; tests = $rows.Count; failures = $failures; cases = @($rows) }
    [IO.File]::WriteAllText((Join-Path $LogDirectory 'packaging-project-selection-tests.json'),
        ((ConvertTo-Json $result -Depth 15).Replace("`r`n", "`n") + "`n"), [Text.UTF8Encoding]::new($false))
    Write-Host "Packaging project selection: $($rows.Count - $failures)/$($rows.Count) PASS"
    if ($failures -ne 0) { exit 1 }
} finally {
    $absolute = [IO.Path]::GetFullPath($fixture)
    $tempBoundary = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $absolute.StartsWith($tempBoundary, [StringComparison]::OrdinalIgnoreCase) -or
            (Split-Path -Leaf $absolute) -notmatch '^geocedg-120-package-root-[0-9a-f]{32}$') {
        throw 'Unsafe project-selection fixture cleanup target.'
    }
    if (Test-Path -LiteralPath $absolute) { Remove-Item -LiteralPath $absolute -Recurse -Force }
}

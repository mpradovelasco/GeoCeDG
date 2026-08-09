[CmdletBinding()]
param(
    [string]$Suite = "benchmarks/suites/operational-smoke.yml",
    [string]$OutputPath,
    [switch]$NoWrite
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RootPrefix = $RepositoryRoot + [IO.Path]::DirectorySeparatorChar

function Resolve-RepositoryFile {
    param([Parameter(Mandatory)] [string]$RelativePath)

    if ([IO.Path]::IsPathRooted($RelativePath)) {
        throw "Benchmark paths must be repository-relative: $RelativePath"
    }
    $platformPath = $RelativePath.Replace("/", [IO.Path]::DirectorySeparatorChar)
    $absolute = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $platformPath))
    if (-not $absolute.StartsWith($RootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Benchmark path escapes the repository: $RelativePath"
    }
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "Benchmark file does not exist: $RelativePath"
    }
    return $absolute
}

function Get-Median {
    param([Parameter(Mandatory)] [double[]]$Values)

    $ordered = @($Values | Sort-Object)
    $middle = [int][Math]::Floor($ordered.Count / 2)
    if ($ordered.Count % 2 -eq 1) {
        return [double]$ordered[$middle]
    }
    return ([double]$ordered[$middle - 1] + [double]$ordered[$middle]) / 2
}

function Invoke-MeasuredScript {
    param(
        [Parameter(Mandatory)] [string]$ScriptPath,
        [AllowEmptyCollection()] [string[]]$Arguments,
        [Parameter(Mandatory)] [int]$TimeoutSeconds
    )

    $pwsh = (Get-Command pwsh -ErrorAction Stop).Source
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $pwsh
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in @(
            "-NoLogo", "-NoProfile", "-NonInteractive", "-File", $ScriptPath
        ) + @($Arguments)) {
        $startInfo.ArgumentList.Add($argument)
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $stopwatch = [Diagnostics.Stopwatch]::StartNew()
    try {
        if (-not $process.Start()) {
            throw "Benchmark command could not be started."
        }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
            $process.Kill($true)
            throw "Benchmark command timed out after $TimeoutSeconds seconds."
        }
        $process.WaitForExit()
        $stopwatch.Stop()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            throw "Benchmark command exited $($process.ExitCode).`n$stdout`n$stderr"
        }
        return [Math]::Round($stopwatch.Elapsed.TotalMilliseconds, 3)
    } finally {
        $stopwatch.Stop()
        $process.Dispose()
    }
}

try {
    $suitePath = Resolve-RepositoryFile -RelativePath $Suite
    $suiteDefinition = Get-Content -Raw -LiteralPath $suitePath |
        ConvertFrom-Json -Depth 100 -NoEnumerate
    if ($suiteDefinition.schema_version -ne 1) {
        throw "Unsupported benchmark schema_version: $($suiteDefinition.schema_version)"
    }
    if ($suiteDefinition.budget_mode -ne "informational") {
        throw "G1 benchmark budgets must be informational."
    }

    $operationalVerifier = Join-Path $RepositoryRoot "tools\agent\verify-operational.ps1"
    & $operationalVerifier -Quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Operational verification failed before benchmark execution."
    }

    $caseResults = [Collections.Generic.List[object]]::new()
    foreach ($case in @($suiteDefinition.cases)) {
        $scriptPath = Resolve-RepositoryFile -RelativePath ([string]$case.script)
        if (-not $scriptPath.StartsWith(
                (Join-Path $RepositoryRoot "tools\agent") +
                [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
            throw "Benchmark script is outside tools/agent: $($case.script)"
        }

        Write-Host "==> Benchmark $($case.id)"
        for ($iteration = 0; $iteration -lt $case.warmup_iterations; $iteration++) {
            [void](Invoke-MeasuredScript -ScriptPath $scriptPath `
                -Arguments @($case.arguments) -TimeoutSeconds $case.timeout_seconds)
        }

        $durations = [Collections.Generic.List[double]]::new()
        for ($iteration = 0; $iteration -lt $case.measurement_iterations; $iteration++) {
            $elapsed = Invoke-MeasuredScript -ScriptPath $scriptPath `
                -Arguments @($case.arguments) -TimeoutSeconds $case.timeout_seconds
            $durations.Add($elapsed)
        }

        $values = [double[]]$durations.ToArray()
        $median = [Math]::Round((Get-Median -Values $values), 3)
        $mean = [Math]::Round((($values | Measure-Object -Average).Average), 3)
        $minimum = [Math]::Round((($values | Measure-Object -Minimum).Minimum), 3)
        $maximum = [Math]::Round((($values | Measure-Object -Maximum).Maximum), 3)
        $threshold = [double]$case.budget.warning_threshold_ms
        $budgetStatus = if ($median -le $threshold) {
            "within-informational-budget"
        } else {
            "informational-exceeded"
        }

        $caseResults.Add([ordered]@{
                id = [string]$case.id
                script = [string]$case.script
                script_sha256 = (Get-FileHash -LiteralPath $scriptPath `
                    -Algorithm SHA256).Hash.ToLowerInvariant()
                warmup_iterations = [int]$case.warmup_iterations
                measurement_iterations = [int]$case.measurement_iterations
                durations_ms = $values
                median_elapsed_ms = $median
                mean_elapsed_ms = $mean
                minimum_elapsed_ms = $minimum
                maximum_elapsed_ms = $maximum
                warning_threshold_ms = $threshold
                budget_status = $budgetStatus
            })
        Write-Host "    median=${median}ms; budget=$budgetStatus"
    }

    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to record benchmark source revision."
    }
    $repositoryStatus = @(& git -C $RepositoryRoot status --porcelain=v1 `
        --untracked-files=all)
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to record benchmark repository status."
    }
    $result = [ordered]@{
        schema_version = 1
        suite = [string]$suiteDefinition.id
        suite_sha256 = (Get-FileHash -LiteralPath $suitePath `
            -Algorithm SHA256).Hash.ToLowerInvariant()
        runner_sha256 = (Get-FileHash -LiteralPath $PSCommandPath `
            -Algorithm SHA256).Hash.ToLowerInvariant()
        source_revision = $head
        repository_clean = ($repositoryStatus.Count -eq 0)
        repository_status = $repositoryStatus
        generated_at_utc = [DateTime]::UtcNow.ToString("o")
        environment = [ordered]@{
            os = [Environment]::OSVersion.VersionString
            architecture = [Runtime.InteropServices.RuntimeInformation]::OSArchitecture.ToString()
            powershell = $PSVersionTable.PSVersion.ToString()
        }
        budget_mode = "informational"
        cases = $caseResults
    }
    $json = $result | ConvertTo-Json -Depth 20

    if ($NoWrite) {
        Write-Output $json
    } else {
        if ([string]::IsNullOrWhiteSpace($OutputPath)) {
            $OutputPath = Join-Path $RepositoryRoot `
                "artifacts\benchmarks\operational-smoke-latest.json"
        }
        $absoluteOutput = [IO.Path]::GetFullPath($OutputPath)
        $outputDirectory = Split-Path -Parent $absoluteOutput
        New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
        [IO.File]::WriteAllText(
            $absoluteOutput,
            $json + [Environment]::NewLine,
            [Text.UTF8Encoding]::new($false))
        Write-Host "Benchmark result: $absoluteOutput"
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}

exit 0

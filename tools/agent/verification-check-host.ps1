#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$RequestPath,
    [string]$ResultPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'verification-io.psm1') -Force

function Get-VerificationHostProperty {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string]$Name,
        [switch]$Required
    )
    if ($Object -is [Collections.IDictionary]) {
        if ($Object.Contains($Name)) { return $Object[$Name] }
    } else {
        $property = $Object.PSObject.Properties[$Name]
        if ($null -ne $property) { return $property.Value }
    }
    if ($Required) { throw "Missing child-host property '$Name'." }
    return $null
}

function ConvertFrom-VerificationCapturedUtf8 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    try {
        return [pscustomobject]@{
            state = 'UTF8'
            text = [Text.UTF8Encoding]::new($false, $true).GetString($Bytes)
            cause = $null
        }
    } catch [Text.DecoderFallbackException] {
        return [pscustomobject]@{
            state = 'INVALID_UTF8'
            text = $null
            cause = $_.Exception.Message
        }
    }
}

function Invoke-VerificationChildProcess {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$CheckId,
        [Parameter(Mandatory)] [ValidateSet(
            'PROCESS_PRODUCER', 'ACCEPTANCE_LEAF', 'DIAGNOSTIC_LEAF')] [string]$NodeKind,
        [Parameter(Mandatory)] [string]$Command,
        [string[]]$Arguments = @(),
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [object]$EnvironmentContract,
        [Parameter(Mandatory)] [string]$OutputDirectory,
        [string]$StructuredOutputPath,
        [Collections.IDictionary]$EvidencePaths = @{}
    )

    $workingFull = [IO.Path]::GetFullPath($WorkingDirectory)
    if (-not (Test-Path -LiteralPath $workingFull -PathType Container)) {
        throw "Child working directory does not exist: $workingFull"
    }
    $outputFull = [IO.Path]::GetFullPath($OutputDirectory)
    if (Test-Path -LiteralPath $outputFull) {
        throw "Child output directory must be unique and initially absent: $outputFull"
    }
    [void][IO.Directory]::CreateDirectory($outputFull)
    $structuredFull = $null
    if (-not [string]::IsNullOrWhiteSpace($StructuredOutputPath)) {
        $structuredFull = Resolve-VerificationContainedPath -Root $outputFull -Path $StructuredOutputPath
    }
    $evidenceFullPaths = [ordered]@{}
    foreach ($name in $EvidencePaths.Keys) {
        $evidenceFullPaths[[string]$name] = Resolve-VerificationContainedPath -Root $outputFull -Path ([string]$EvidencePaths[$name])
    }
    $stdoutPath = Join-Path $outputFull 'stdout.bin'
    $stderrPath = Join-Path $outputFull 'stderr.bin'
    $inherit = [bool](Get-VerificationHostProperty $EnvironmentContract 'inherit' -Required)
    $variables = Get-VerificationHostProperty $EnvironmentContract 'variables' -Required
    $environmentRecord = [ordered]@{ inherit = $inherit; variables = [ordered]@{} }
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $Command
    $startInfo.WorkingDirectory = $workingFull
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    if (-not $inherit) { $startInfo.Environment.Clear() }
    foreach ($argument in [string[]]$Arguments) { [void]$startInfo.ArgumentList.Add($argument) }
    $variableProperties = if ($variables -is [Collections.IDictionary]) {
        @($variables.Keys | ForEach-Object {
            [pscustomobject]@{ Name = [string]$_; Value = [string]$variables[$_] }
        })
    } else {
        @($variables.PSObject.Properties | ForEach-Object {
            [pscustomobject]@{ Name = $_.Name; Value = [string]$_.Value }
        })
    }
    $variableList = [Collections.Generic.List[object]]::new()
    foreach ($variable in $variableProperties) { $variableList.Add($variable) }
    $variableList.Sort([Comparison[object]]{
        param($left, $right)
        return [StringComparer]::Ordinal.Compare([string]$left.Name, [string]$right.Name)
    })
    foreach ($variable in $variableList) {
        $startInfo.Environment[$variable.Name] = $variable.Value
        $environmentRecord.variables[$variable.Name] = $variable.Value
    }
    $commandIdentity = Get-VerificationDeterministicHash -Value ([ordered]@{
        command = [IO.Path]::GetFullPath($Command)
        arguments = [string[]]$Arguments
        working_directory = $workingFull
        environment = $environmentRecord
    })

    $stdoutMemory = [IO.MemoryStream]::new()
    $stderrMemory = [IO.MemoryStream]::new()
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $started = [datetime]::UtcNow
    $completionState = 'LAUNCH_FAILED'
    $childExitCode = $null
    $completionCause = $null
    try {
        try {
            if (-not $process.Start()) { throw "Child process did not start: $Command" }
            $stdoutCopy = $process.StandardOutput.BaseStream.CopyToAsync($stdoutMemory)
            $stderrCopy = $process.StandardError.BaseStream.CopyToAsync($stderrMemory)
            $process.WaitForExit()
            [void]$stdoutCopy.GetAwaiter().GetResult()
            [void]$stderrCopy.GetAwaiter().GetResult()
            $childExitCode = $process.ExitCode
            $completionState = 'COMPLETED'
        } catch {
            $completionCause = $_.Exception.Message
        }
    } finally {
        $finished = [datetime]::UtcNow
        $process.Dispose()
    }
    $stdoutBytes = $stdoutMemory.ToArray()
    $stderrBytes = $stderrMemory.ToArray()
    $stdoutMemory.Dispose()
    $stderrMemory.Dispose()
    [IO.File]::WriteAllBytes($stdoutPath, $stdoutBytes)
    [IO.File]::WriteAllBytes($stderrPath, $stderrBytes)
    $stdoutDecoded = ConvertFrom-VerificationCapturedUtf8 -Bytes $stdoutBytes
    $stderrDecoded = ConvertFrom-VerificationCapturedUtf8 -Bytes $stderrBytes

    $structured = [ordered]@{
        state = 'NOT_DECLARED'
        path = $null
        sha256 = $null
        value = $null
        cause = $null
    }
    if (-not [string]::IsNullOrWhiteSpace($structuredFull)) {
        $structuredPath = $structuredFull
        $structured.path = $structuredPath
        if (-not (Test-Path -LiteralPath $structuredPath -PathType Leaf)) {
            $structured.state = 'ABSENT'
            $structured.cause = 'Declared structured output was not produced.'
        } else {
            $structured.sha256 = Get-VerificationFileSha256 $structuredPath
            try {
                $structured.value = Read-VerificationJson $structuredPath
                $structured.state = 'PRESENT'
            } catch {
                $structured.state = 'MALFORMED'
                $structured.cause = $_.Exception.Message
            }
        }
    }

    $evidence = [Collections.Generic.List[object]]::new()
    $evidenceNames = [string[]]@($evidenceFullPaths.Keys | ForEach-Object { [string]$_ })
    [Array]::Sort($evidenceNames, [StringComparer]::Ordinal)
    foreach ($name in $evidenceNames) {
        $path = [string]$evidenceFullPaths[$name]
        $present = Test-Path -LiteralPath $path -PathType Leaf
        $evidence.Add([pscustomobject]@{
            name = $name
            state = $(if ($present) { 'PRESENT' } else { 'ABSENT' })
            path = $path
            sha256 = $(if ($present) { Get-VerificationFileSha256 $path } else { $null })
        })
    }

    return [pscustomobject]@{
        check_id = $CheckId
        node_kind = $NodeKind
        command = [IO.Path]::GetFullPath($Command)
        arguments = [string[]]$Arguments
        working_directory = $workingFull
        environment = $environmentRecord
        command_identity = $commandIdentity
        completion_state = $completionState
        exit_code = $childExitCode
        completion_cause = $completionCause
        stdout_log = $stdoutPath
        stderr_log = $stderrPath
        stdout_sha256 = Get-VerificationSha256 -Bytes $stdoutBytes
        stderr_sha256 = Get-VerificationSha256 -Bytes $stderrBytes
        stdout_encoding = $stdoutDecoded.state
        stderr_encoding = $stderrDecoded.state
        stdout_text = $stdoutDecoded.text
        stderr_text = $stderrDecoded.text
        structured_output = [pscustomobject]$structured
        generated_evidence = [object[]]$evidence.ToArray()
        started_at = $started.ToString('o')
        finished_at = $finished.ToString('o')
        duration_ms = [math]::Max(0.0, ($finished - $started).TotalMilliseconds)
    }
}

function Invoke-VerificationChildHostMain {
    param(
        [Parameter(Mandatory)] [string]$Request,
        [Parameter(Mandatory)] [string]$Result
    )
    $requestValue = Read-VerificationJson -Path $Request
    $evidenceMap = @{}
    $requestedEvidence = Get-VerificationHostProperty $requestValue 'evidence_paths'
    if ($null -ne $requestedEvidence) {
        foreach ($property in $requestedEvidence.PSObject.Properties) {
            $evidenceMap[$property.Name] = [string]$property.Value
        }
    }
    $arguments = @{
        CheckId = [string](Get-VerificationHostProperty $requestValue 'check_id' -Required)
        NodeKind = [string](Get-VerificationHostProperty $requestValue 'node_kind' -Required)
        Command = [string](Get-VerificationHostProperty $requestValue 'command' -Required)
        Arguments = [string[]]@(Get-VerificationHostProperty $requestValue 'arguments' -Required)
        WorkingDirectory = [string](Get-VerificationHostProperty $requestValue 'working_directory' -Required)
        EnvironmentContract = Get-VerificationHostProperty $requestValue 'environment' -Required
        OutputDirectory = [string](Get-VerificationHostProperty $requestValue 'output_directory' -Required)
        StructuredOutputPath = [string](Get-VerificationHostProperty $requestValue 'structured_output_path')
        EvidencePaths = $evidenceMap
    }
    $resultValue = Invoke-VerificationChildProcess @arguments
    [void](Write-VerificationAtomicJson -Path $Result -Value $resultValue)
}

if ($MyInvocation.InvocationName -cne '.') {
    if ([string]::IsNullOrWhiteSpace($RequestPath) -or [string]::IsNullOrWhiteSpace($ResultPath)) {
        throw 'Direct child-host execution requires -RequestPath and -ResultPath.'
    }
    Invoke-VerificationChildHostMain -Request $RequestPath -Result $ResultPath
}

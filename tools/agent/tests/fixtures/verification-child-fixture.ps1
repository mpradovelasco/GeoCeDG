#requires -Version 7.2
[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet(
        'Structured',
        'Throw',
        'Malformed',
        'Absent',
        'InvalidUtf8',
        'Simultaneous',
        'Environment',
        'Arguments')]
    [string]$Mode,
    [string]$ResultPath,
    [string]$EvidencePath,
    [string]$ContractClass = 'SCIENTIFIC_SEMANTIC',
    [string]$Outcome = 'CONTRACT_SATISFIED',
    [string]$SemanticPath,
    [string]$Payload,
    [string]$EnvironmentName = 'GEOCEDG_FIXTURE_VALUE',
    [int]$ExitCode = 0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8

function Write-FixtureJson {
    param([Parameter(Mandatory)] [object]$Value)
    if ([string]::IsNullOrWhiteSpace($ResultPath)) {
        throw 'This fixture mode requires -ResultPath.'
    }
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath))
    $json = (ConvertTo-Json -InputObject $Value -Depth 20).Replace(
        [Environment]::NewLine, [string][char]10) + [char]10
    [IO.File]::WriteAllText([IO.Path]::GetFullPath($ResultPath), $json, $utf8)
}

if (-not [string]::IsNullOrWhiteSpace($EvidencePath)) {
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $EvidencePath))
    [IO.File]::WriteAllText([IO.Path]::GetFullPath($EvidencePath), 'fixture evidence', $utf8)
}

switch ($Mode) {
    'Structured' {
        $result = [ordered]@{
            contract_class = $ContractClass
            outcome = $Outcome
        }
        if (-not [string]::IsNullOrWhiteSpace($SemanticPath)) {
            $result.path = $SemanticPath
        }
        Write-FixtureJson $result
    }
    'Throw' {
        throw 'VERIFICATION_CHILD_EXCEPTION_SENTINEL'
    }
    'Malformed' {
        if ([string]::IsNullOrWhiteSpace($ResultPath)) { throw 'Malformed mode requires -ResultPath.' }
        [void][IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath))
        [IO.File]::WriteAllText([IO.Path]::GetFullPath($ResultPath), '{"broken":', $utf8)
    }
    'Absent' {
        Write-Output 'Declared output intentionally absent.'
    }
    'InvalidUtf8' {
        $bytes = [byte[]](0x66, 0x6f, 0x80, 0x6f)
        $stream = [Console]::OpenStandardOutput()
        $stream.Write($bytes, 0, $bytes.Length)
        $stream.Flush()
    }
    'Simultaneous' {
        foreach ($index in 1..64) {
            [Console]::Out.WriteLine("stdout-café-$index")
            [Console]::Error.WriteLine("stderr-café-$index")
        }
        if (-not [string]::IsNullOrWhiteSpace($ResultPath)) {
            Write-FixtureJson ([ordered]@{
                contract_class = $ContractClass
                outcome = $Outcome
            })
        }
    }
    'Environment' {
        Write-FixtureJson ([ordered]@{
            contract_class = $ContractClass
            outcome = $Outcome
            environment_value = [Environment]::GetEnvironmentVariable($EnvironmentName)
        })
    }
    'Arguments' {
        Write-FixtureJson ([ordered]@{
            contract_class = $ContractClass
            outcome = $Outcome
            payload = $Payload
        })
    }
}

exit $ExitCode

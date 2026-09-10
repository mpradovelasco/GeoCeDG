#requires -Version 7.2
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot '../verification-io.psm1') -Force

$script:Cases = 0
$script:Assertions = 0
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/')
$root = Join-Path $tempBase ('geocedg-verification-io-' + [guid]::NewGuid().ToString('N'))
[void][IO.Directory]::CreateDirectory($root)
$marker = Join-Path $root '.fixture-owner'
[IO.File]::WriteAllText($marker, 'verification-io', [Text.UTF8Encoding]::new($false))

function Assert-Case {
    param([Parameter(Mandatory)] [bool]$Condition, [Parameter(Mandatory)] [string]$Message)
    $script:Assertions++
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-CaseThrows {
    param([Parameter(Mandatory)] [scriptblock]$Action, [Parameter(Mandatory)] [string]$Pattern)
    $observed = $null
    try { & $Action | Out-Null } catch { $observed = $_.Exception }
    Assert-Case ($null -ne $observed) "Expected an exception matching $Pattern."
    Assert-Case ($observed.Message -match $Pattern) "Unexpected exception: $($observed.Message)"
}

function Invoke-Case {
    param([Parameter(Mandatory)] [string]$Name, [Parameter(Mandatory)] [scriptblock]$Action)
    $script:Cases++
    & $Action
    Write-Host "PASS: $Name"
}

try {
    Invoke-Case 'strict UTF-8 with and without BOM' {
        $plain = Join-Path $root 'plain-café.txt'
        $bom = Join-Path $root 'bom.txt'
        $content = 'café' + [char]10
        [IO.File]::WriteAllText($plain, $content, [Text.UTF8Encoding]::new($false))
        [IO.File]::WriteAllText($bom, $content, [Text.UTF8Encoding]::new($true))
        Assert-Case ((Read-VerificationUtf8Text $plain) -ceq $content) 'UTF-8 without BOM changed.'
        Assert-Case ((Read-VerificationUtf8Text $bom) -ceq $content) 'UTF-8 BOM was not handled.'
        $written = Join-Path $root 'written.txt'
        [void](Write-VerificationUtf8Text $written $content)
        $writtenBytes = [IO.File]::ReadAllBytes($written)
        Assert-Case (-not ($writtenBytes.Length -ge 3 -and $writtenBytes[0] -eq 0xef -and
            $writtenBytes[1] -eq 0xbb -and $writtenBytes[2] -eq 0xbf)) 'UTF-8 writer added a BOM.'
        Assert-Case ((Read-VerificationUtf8Text $written) -ceq $content) 'UTF-8 writer changed content.'
    }
    Invoke-Case 'invalid UTF-8 is rejected' {
        $invalid = Join-Path $root 'invalid.txt'
        [IO.File]::WriteAllBytes($invalid, [byte[]](0x66, 0x80, 0x6f))
        Assert-CaseThrows { Read-VerificationUtf8Text $invalid } 'valid UTF-8'
    }
    Invoke-Case 'semantic newline normalization and byte identity remain distinct' {
        $lf = 'alpha' + [char]10 + 'beta' + [char]10
        $crlf = $lf.Replace([string][char]10, ([string][char]13 + [char]10))
        Assert-Case (Test-VerificationSemanticTextEqual $lf $crlf) 'LF and CRLF were not semantically equal.'
        $lfPath = Join-Path $root 'lf.txt'
        $crlfPath = Join-Path $root 'crlf.txt'
        [IO.File]::WriteAllText($lfPath, $lf, [Text.UTF8Encoding]::new($false))
        [IO.File]::WriteAllText($crlfPath, $crlf, [Text.UTF8Encoding]::new($false))
        Assert-Case (-not (Test-VerificationByteIdentity $lfPath $crlfPath)) 'Byte comparison ignored newline bytes.'
        Assert-Case ((ConvertTo-VerificationCanonicalLf $crlf) -ceq $lf) 'Canonical LF conversion was incorrect.'
    }
    Invoke-Case 'Git and filesystem paths are normalized and contained' {
        Assert-Case ((ConvertTo-VerificationGitPath 'docs\café point.txt') -ceq 'docs/café point.txt') 'Unicode Git path changed.'
        Assert-CaseThrows { ConvertTo-VerificationGitPath '../escape.txt' } 'traversal'
        $safe = Resolve-VerificationContainedPath -Root $root -Path 'nested/café.txt'
        Assert-Case ($safe.StartsWith($root, [StringComparison]::OrdinalIgnoreCase)) 'Safe child escaped its root.'
        Assert-CaseThrows { Resolve-VerificationContainedPath -Root $root -Path '../escape.txt' } 'escapes'
    }
    Invoke-Case 'canonical hashing is deterministic and ignores declared telemetry' {
        $left = [ordered]@{ beta = 2; alpha = 1; duration_ms = 1.0 }
        $right = [ordered]@{ duration_ms = 9000.0; alpha = 1; beta = 2 }
        $leftHash = Get-VerificationDeterministicHash $left -ExcludeProperties duration_ms
        $rightHash = Get-VerificationDeterministicHash $right -ExcludeProperties duration_ms
        Assert-Case ($leftHash -ceq $rightHash) 'Property order or duration changed a deterministic hash.'
        $right.beta = 3
        Assert-Case ($leftHash -cne (Get-VerificationDeterministicHash $right -ExcludeProperties duration_ms)) 'Semantic change did not change hash.'
    }
    Invoke-Case 'canonical property ordering is ordinal and culture independent' {
        $value = [pscustomobject][ordered]@{ z = 1; I = 2; ([string][char]305) = 3; a = 4 }
        $previousCulture = [Globalization.CultureInfo]::CurrentCulture
        try {
            [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('en-US')
            $english = Get-VerificationDeterministicHash $value
            [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('tr-TR')
            $turkish = Get-VerificationDeterministicHash $value
        } finally {
            [Globalization.CultureInfo]::CurrentCulture = $previousCulture
        }
        Assert-Case ($english -ceq $turkish) 'Current culture changed deterministic hashing.'
        $canonical = ConvertTo-VerificationCanonicalJson $value
        Assert-Case ($canonical.IndexOf('"I"', [StringComparison]::Ordinal) -lt
            $canonical.IndexOf('"a"', [StringComparison]::Ordinal)) 'Canonical keys are not in ordinal order.'
    }
    Invoke-Case 'empty JSON objects retain object identity at every depth' {
        $empty = '{}' | ConvertFrom-Json
        $nested = '{"outer":{},"items":[{},{}]}' | ConvertFrom-Json -Depth 20
        Assert-Case ((ConvertTo-VerificationCanonicalJson $empty) -ceq '{}') `
            'A root empty PSCustomObject was not canonicalized as an object.'
        Assert-Case ((ConvertTo-VerificationCanonicalJson $nested) -ceq `
                '{"items":[{},{}],"outer":{}}') `
            'Nested or array-contained empty objects changed shape.'
        $roundTrip = (ConvertTo-VerificationCanonicalJson $nested) | ConvertFrom-Json -Depth 20
        Assert-Case ((ConvertTo-VerificationCanonicalJson $roundTrip) -ceq `
                '{"items":[{},{}],"outer":{}}') `
            'JSON round-trip changed empty-object identity.'
        $previousCulture = [Globalization.CultureInfo]::CurrentCulture
        try {
            [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('en-US')
            $english = Get-VerificationDeterministicHash $nested
            [Globalization.CultureInfo]::CurrentCulture = [Globalization.CultureInfo]::GetCultureInfo('tr-TR')
            $turkish = Get-VerificationDeterministicHash $nested
        } finally {
            [Globalization.CultureInfo]::CurrentCulture = $previousCulture
        }
        Assert-Case ($english -ceq $turkish) 'Culture changed an empty-object hash.'
        Assert-Case ($english -cne (Get-VerificationDeterministicHash @())) `
            'An empty object collided with an empty array.'
        Assert-Case ($english -cne (Get-VerificationDeterministicHash $null)) `
            'An empty object collided with null.'
    }
    Invoke-Case 'atomic JSON ignores an incomplete sibling and preserves its committed value' {
        $target = Join-Path $root 'report.json'
        [void](Write-VerificationAtomicJson -Path $target -Value ([ordered]@{ value = 'committed' }))
        $targetBytes = [IO.File]::ReadAllBytes($target)
        Assert-Case (-not ($targetBytes.Length -ge 3 -and $targetBytes[0] -eq 0xef -and
            $targetBytes[1] -eq 0xbb -and $targetBytes[2] -eq 0xbf)) 'Atomic JSON writer added a BOM.'
        $orphan = Join-Path $root '.report.json.interrupted.tmp'
        [IO.File]::WriteAllText($orphan, '{"value":', [Text.UTF8Encoding]::new($false))
        $read = Read-VerificationJson $target
        Assert-Case ($read.value -ceq 'committed') 'Committed atomic output was not recoverable.'
        Assert-Case (@(Get-ChildItem -LiteralPath $root -Filter '.report.json.*.tmp').Count -eq 1) 'Reader mutated an incomplete sibling.'
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($root)
    $prefix = $tempBase + [IO.Path]::DirectorySeparatorChar
    if ($resolved.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase) -and
            (Test-Path -LiteralPath $marker -PathType Leaf) -and
            [IO.File]::ReadAllText($marker) -ceq 'verification-io') {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    } else {
        throw "Fixture cleanup refused unexpected path: $resolved"
    }
}

Write-Host "verification-io.Tests: $script:Cases cases, $script:Assertions assertions, 0 failures"

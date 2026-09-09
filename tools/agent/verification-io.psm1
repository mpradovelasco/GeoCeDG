#requires -Version 7.2

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$script:Utf8Strict = [Text.UTF8Encoding]::new($false, $true)
$script:Utf8NoBom = [Text.UTF8Encoding]::new($false)

function Get-VerificationSha256 {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)

    return [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
}

function Get-VerificationFileSha256 {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    $fullPath = [IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        throw "Verification input file does not exist: $fullPath"
    }
    return Get-VerificationSha256 -Bytes ([IO.File]::ReadAllBytes($fullPath))
}

function Read-VerificationUtf8Text {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    $fullPath = [IO.Path]::GetFullPath($Path)
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        throw "Verification text file does not exist: $fullPath"
    }
    $bytes = [IO.File]::ReadAllBytes($fullPath)
    $offset = 0
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xef -and $bytes[1] -eq 0xbb -and $bytes[2] -eq 0xbf) {
        $offset = 3
    } elseif ($bytes.Length -ge 2 -and
            (($bytes[0] -eq 0xff -and $bytes[1] -eq 0xfe) -or
             ($bytes[0] -eq 0xfe -and $bytes[1] -eq 0xff))) {
        throw "Verification text must be UTF-8, not UTF-16: $fullPath"
    }
    try {
        return $script:Utf8Strict.GetString($bytes, $offset, $bytes.Length - $offset)
    } catch [Text.DecoderFallbackException] {
        throw [IO.InvalidDataException]::new("Verification text is not valid UTF-8: $fullPath", $_.Exception)
    }
}

function Write-VerificationUtf8Text {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text
    )

    $fullPath = [IO.Path]::GetFullPath($Path)
    $parent = Split-Path -Parent $fullPath
    if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
        [void][IO.Directory]::CreateDirectory($parent)
    }
    [IO.File]::WriteAllText($fullPath, $Text, $script:Utf8NoBom)
    return $fullPath
}

function ConvertTo-VerificationCanonicalLf {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)

    $cr = [string][char]13
    $lf = [string][char]10
    return $Text.Replace($cr + $lf, $lf).Replace($cr, $lf)
}

function Test-VerificationSemanticTextEqual {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Left,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Right
    )

    return (ConvertTo-VerificationCanonicalLf $Left) -ceq (ConvertTo-VerificationCanonicalLf $Right)
}

function Test-VerificationByteIdentity {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$LeftPath,
        [Parameter(Mandatory)] [string]$RightPath
    )

    $left = [IO.File]::ReadAllBytes([IO.Path]::GetFullPath($LeftPath))
    $right = [IO.File]::ReadAllBytes([IO.Path]::GetFullPath($RightPath))
    if ($left.Length -ne $right.Length) { return $false }
    for ($index = 0; $index -lt $left.Length; $index++) {
        if ($left[$index] -ne $right[$index]) { return $false }
    }
    return $true
}

function ConvertTo-VerificationGitPath {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path) -or $Path.IndexOf([char]0) -ge 0) {
        throw 'Git path must be a nonempty text value without NUL.'
    }
    $candidate = $Path.Replace('\', '/')
    if ($candidate.StartsWith('/', [StringComparison]::Ordinal) -or
            $candidate -match '^[A-Za-z]:' -or
            $candidate.EndsWith('/', [StringComparison]::Ordinal)) {
        throw "Git path must be repository-relative: $Path"
    }
    $segments = @($candidate.Split('/'))
    if ($segments.Count -eq 0 -or @($segments | Where-Object { $_ -in @('', '.', '..') }).Count -gt 0) {
        throw "Git path contains an empty or traversal segment: $Path"
    }
    return $segments -join '/'
}

function Resolve-VerificationContainedPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Path,
        [switch]$AllowRoot
    )

    $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd(
        [IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $rootFull -PathType Container)) {
        throw "Containment root does not exist: $rootFull"
    }
    $rootItem = Get-Item -LiteralPath $rootFull -Force
    if ($rootItem.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "Verification containment root refuses linked materialization: $rootFull"
    }
    $candidate = if ([IO.Path]::IsPathRooted($Path)) {
        [IO.Path]::GetFullPath($Path)
    } else {
        [IO.Path]::GetFullPath((Join-Path $rootFull $Path))
    }
    $relative = [IO.Path]::GetRelativePath($rootFull, $candidate)
    if ([IO.Path]::IsPathRooted($relative) -or $relative -eq '..' -or
            $relative.StartsWith('..' + [IO.Path]::DirectorySeparatorChar, [StringComparison]::Ordinal) -or
            $relative.StartsWith('../', [StringComparison]::Ordinal)) {
        throw "Path escapes its verification root: $candidate"
    }
    if (-not $AllowRoot -and $relative -eq '.') {
        throw "A child path, not the verification root itself, is required: $candidate"
    }

    $current = $rootFull
    $parts = if ($relative -eq '.') {
        @()
    } else {
        @($relative.Split([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar))
    }
    foreach ($part in $parts) {
        if ([string]::IsNullOrEmpty($part)) { continue }
        $current = Join-Path $current $part
        if (Test-Path -LiteralPath $current) {
            $item = Get-Item -LiteralPath $current -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Verification path refuses linked materialization: $current"
            }
        }
    }
    return $candidate
}

function ConvertTo-VerificationCanonicalValue {
    [CmdletBinding()]
    param(
        [AllowNull()] [object]$Value,
        [Parameter(Mandatory)] [AllowEmptyCollection()]
        [Collections.Generic.HashSet[string]]$ExcludedNames
    )

    if ($null -eq $Value) { return $null }
    if ($Value -is [datetime]) { return $Value.ToUniversalTime().ToString('o') }
    if ($Value -is [datetimeoffset]) { return $Value.ToUniversalTime().ToString('o') }
    if ($Value -is [byte[]]) { return [Convert]::ToBase64String($Value) }
    if ($Value -is [Collections.IDictionary]) {
        $ordered = [ordered]@{}
        $keys = [string[]]@($Value.Keys | ForEach-Object { [string]$_ })
        [Array]::Sort($keys, [StringComparer]::Ordinal)
        foreach ($key in $keys) {
            if ($ExcludedNames.Contains($key)) { continue }
            $ordered[$key] = ConvertTo-VerificationCanonicalValue -Value $Value[$key] -ExcludedNames $ExcludedNames
        }
        return $ordered
    }
    if ($Value -is [Management.Automation.PSCustomObject]) {
        $ordered = [ordered]@{}
        $propertyNames = [string[]]@($Value.PSObject.Properties.Name)
        [Array]::Sort($propertyNames, [StringComparer]::Ordinal)
        foreach ($propertyName in $propertyNames) {
            if ($ExcludedNames.Contains($propertyName)) { continue }
            $property = $Value.PSObject.Properties[$propertyName]
            $ordered[$propertyName] = ConvertTo-VerificationCanonicalValue -Value $property.Value -ExcludedNames $ExcludedNames
        }
        return $ordered
    }
    if ($Value -is [Collections.IEnumerable] -and $Value -isnot [string]) {
        $items = [Collections.Generic.List[object]]::new()
        foreach ($item in $Value) {
            $items.Add((ConvertTo-VerificationCanonicalValue -Value $item -ExcludedNames $ExcludedNames))
        }
        return ,([object[]]$items.ToArray())
    }
    return $Value
}

function ConvertTo-VerificationCanonicalJson {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [AllowNull()] [object]$Value,
        [string[]]$ExcludeProperties = @()
    )

    $excluded = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($name in $ExcludeProperties) { [void]$excluded.Add($name) }
    $canonical = ConvertTo-VerificationCanonicalValue -Value $Value -ExcludedNames $excluded
    return (ConvertTo-Json -InputObject $canonical -Depth 100 -Compress).Replace(
        [Environment]::NewLine, [string][char]10)
}

function Get-VerificationDeterministicHash {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [AllowNull()] [object]$Value,
        [string[]]$ExcludeProperties = @()
    )

    $json = ConvertTo-VerificationCanonicalJson -Value $Value -ExcludeProperties $ExcludeProperties
    return Get-VerificationSha256 -Bytes $script:Utf8NoBom.GetBytes($json)
}

function Get-VerificationIdentityProperty {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string]$Name
    )
    if ($Object -is [Collections.IDictionary]) {
        if ($Object.Contains($Name)) { return $Object[$Name] }
    } else {
        $property = $Object.PSObject.Properties[$Name]
        if ($null -ne $property) { return $property.Value }
    }
    throw "Missing verification identity property '$Name'."
}

function Sort-VerificationIdentityObjects {
    param(
        [object[]]$Values = @(),
        [Parameter(Mandatory)] [string]$PropertyName
    )

    $sorted = [Collections.Generic.List[object]]::new()
    foreach ($value in $Values) { $sorted.Add($value) }
    $comparison = [Comparison[object]]{
        param($left, $right)
        return [StringComparer]::Ordinal.Compare(
            [string](Get-VerificationIdentityProperty $left $PropertyName),
            [string](Get-VerificationIdentityProperty $right $PropertyName))
    }
    $sorted.Sort($comparison)
    return [object[]]$sorted.ToArray()
}

function ConvertTo-VerificationEvidenceIdentity {
    param([object[]]$Evidence = @())

    return [object[]]@(Sort-VerificationIdentityObjects $Evidence name | ForEach-Object {
        [ordered]@{
            name = [string](Get-VerificationIdentityProperty $_ 'name')
            state = [string](Get-VerificationIdentityProperty $_ 'state')
            sha256 = Get-VerificationIdentityProperty $_ 'sha256'
        }
    })
}

function ConvertTo-VerificationObservationIdentity {
    param([Parameter(Mandatory)] [object]$Observation)

    $identity = [ordered]@{
        check_id = [string](Get-VerificationIdentityProperty $Observation 'check_id')
        node_kind = [string](Get-VerificationIdentityProperty $Observation 'node_kind')
        contract_class = [string](Get-VerificationIdentityProperty $Observation 'contract_class')
        command_identity = [string](Get-VerificationIdentityProperty $Observation 'command_identity')
        exit_code = Get-VerificationIdentityProperty $Observation 'exit_code'
        evidence = ConvertTo-VerificationEvidenceIdentity @(
            Get-VerificationIdentityProperty $Observation 'evidence')
        dependencies = [string[]]@(Get-VerificationIdentityProperty $Observation 'dependencies')
    }
    if ($null -ne $Observation.PSObject.Properties['status']) {
        $identity.status = [string](Get-VerificationIdentityProperty $Observation 'status')
    } else {
        $identity.outcome = [string](Get-VerificationIdentityProperty $Observation 'outcome')
    }
    return $identity
}

function ConvertTo-VerificationProducerIdentity {
    param([Parameter(Mandatory)] [object]$Producer)

    $structured = Get-VerificationIdentityProperty $Producer 'structured_output'
    [object]$structuredValue = $null
    if ($structured -is [Collections.IDictionary]) {
        $structuredValue = $structured['value']
    } else {
        $structuredValue = $structured.PSObject.Properties['value'].Value
    }
    return [ordered]@{
        check_id = [string](Get-VerificationIdentityProperty $Producer 'check_id')
        node_kind = [string](Get-VerificationIdentityProperty $Producer 'node_kind')
        command = [string](Get-VerificationIdentityProperty $Producer 'command')
        arguments = [string[]]@(Get-VerificationIdentityProperty $Producer 'arguments')
        working_directory = [string](Get-VerificationIdentityProperty $Producer 'working_directory')
        environment = Get-VerificationIdentityProperty $Producer 'environment'
        command_identity = [string](Get-VerificationIdentityProperty $Producer 'command_identity')
        completion_state = [string](Get-VerificationIdentityProperty $Producer 'completion_state')
        exit_code = Get-VerificationIdentityProperty $Producer 'exit_code'
        stdout_sha256 = Get-VerificationIdentityProperty $Producer 'stdout_sha256'
        stderr_sha256 = Get-VerificationIdentityProperty $Producer 'stderr_sha256'
        stdout_encoding = Get-VerificationIdentityProperty $Producer 'stdout_encoding'
        stderr_encoding = Get-VerificationIdentityProperty $Producer 'stderr_encoding'
        stdout_text = Get-VerificationIdentityProperty $Producer 'stdout_text'
        stderr_text = Get-VerificationIdentityProperty $Producer 'stderr_text'
        structured_output = [ordered]@{
            state = [string](Get-VerificationIdentityProperty $structured 'state')
            sha256 = Get-VerificationIdentityProperty $structured 'sha256'
            value = $structuredValue
        }
        generated_evidence = ConvertTo-VerificationEvidenceIdentity @(
            Get-VerificationIdentityProperty $Producer 'generated_evidence')
    }
}

function Get-VerificationObservationIdentityHash {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Observation)

    return Get-VerificationDeterministicHash -Value (
        ConvertTo-VerificationObservationIdentity -Observation $Observation)
}

function Get-VerificationResultIdentityHash {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [object]$Report)

    $producers = [object[]]@(Get-VerificationIdentityProperty $Report 'process_producers')
    $acceptance = [object[]]@(Get-VerificationIdentityProperty $Report 'acceptance_results')
    $diagnostics = [object[]]@(Get-VerificationIdentityProperty $Report 'diagnostic_findings')
    $payload = [ordered]@{
        '$schema' = [string](Get-VerificationIdentityProperty $Report '$schema')
        schema_version = [int](Get-VerificationIdentityProperty $Report 'schema_version')
        profile = [string](Get-VerificationIdentityProperty $Report 'profile')
        run_state = [string](Get-VerificationIdentityProperty $Report 'run_state')
        base_commit = Get-VerificationIdentityProperty $Report 'base_commit'
        base_tree = Get-VerificationIdentityProperty $Report 'base_tree'
        candidate_commit = [string](Get-VerificationIdentityProperty $Report 'candidate_commit')
        candidate_tree = [string](Get-VerificationIdentityProperty $Report 'candidate_tree')
        execution_plan_hash = [string](Get-VerificationIdentityProperty $Report 'execution_plan_hash')
        environment_fingerprint = [string](Get-VerificationIdentityProperty $Report 'environment_fingerprint')
        process_producers = [object[]]@(Sort-VerificationIdentityObjects $producers check_id | ForEach-Object {
            ConvertTo-VerificationProducerIdentity $_
        })
        acceptance_results = [object[]]@(Sort-VerificationIdentityObjects $acceptance check_id | ForEach-Object {
            ConvertTo-VerificationObservationIdentity $_
        })
        diagnostic_findings = [object[]]@(Sort-VerificationIdentityObjects $diagnostics check_id | ForEach-Object {
            ConvertTo-VerificationObservationIdentity $_
        })
        coverage = Get-VerificationIdentityProperty $Report 'coverage'
        acceptance_verdict = [string](Get-VerificationIdentityProperty $Report 'acceptance_verdict')
        coverage_verdict = [string](Get-VerificationIdentityProperty $Report 'coverage_verdict')
        diagnostic_summary = Get-VerificationIdentityProperty $Report 'diagnostic_summary'
        summary = Get-VerificationIdentityProperty $Report 'summary'
    }
    return Get-VerificationDeterministicHash -Value $payload
}

function Read-VerificationJson {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Path)

    $text = Read-VerificationUtf8Text -Path $Path
    try {
        return ConvertFrom-Json -InputObject $text -Depth 100
    } catch {
        throw [IO.InvalidDataException]::new(
            "Verification JSON is malformed: $([IO.Path]::GetFullPath($Path))", $_.Exception)
    }
}

function Assert-VerificationJsonSchema {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$Value,
        [Parameter(Mandatory)] [string]$SchemaPath
    )

    $schemaFull = [IO.Path]::GetFullPath($SchemaPath)
    [void](Read-VerificationJson -Path $schemaFull)
    $json = ConvertTo-Json -InputObject $Value -Depth 100
    if (-not (Test-Json -Json $json -SchemaFile $schemaFull -ErrorAction Stop)) {
        throw "Verification value does not satisfy schema: $schemaFull"
    }
    return $true
}

function Write-VerificationAtomicJson {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowNull()] [object]$Value
    )

    $fullPath = [IO.Path]::GetFullPath($Path)
    $parent = Split-Path -Parent $fullPath
    if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
        [void][IO.Directory]::CreateDirectory($parent)
    }
    $leaf = Split-Path -Leaf $fullPath
    $temporary = Join-Path $parent (".$leaf." + [guid]::NewGuid().ToString('N') + '.tmp')
    if (Test-Path -LiteralPath $temporary) { throw "Atomic output collision: $temporary" }
    $json = (ConvertTo-Json -InputObject $Value -Depth 100).Replace(
        [Environment]::NewLine, [string][char]10) + [char]10
    $bytes = $script:Utf8NoBom.GetBytes($json)
    try {
        $stream = [IO.FileStream]::new(
            $temporary, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
        try {
            $stream.Write($bytes, 0, $bytes.Length)
            $stream.Flush($true)
        } finally {
            $stream.Dispose()
        }
        [IO.File]::Move($temporary, $fullPath, $true)
    } finally {
        if (Test-Path -LiteralPath $temporary) {
            Remove-Item -LiteralPath $temporary -Force
        }
    }
    return $fullPath
}

Export-ModuleMember -Function @(
    'Get-VerificationSha256',
    'Get-VerificationFileSha256',
    'Read-VerificationUtf8Text',
    'Write-VerificationUtf8Text',
    'ConvertTo-VerificationCanonicalLf',
    'Test-VerificationSemanticTextEqual',
    'Test-VerificationByteIdentity',
    'ConvertTo-VerificationGitPath',
    'Resolve-VerificationContainedPath',
    'ConvertTo-VerificationCanonicalJson',
    'Get-VerificationDeterministicHash',
    'Get-VerificationObservationIdentityHash',
    'Get-VerificationResultIdentityHash',
    'Read-VerificationJson',
    'Assert-VerificationJsonSchema',
    'Write-VerificationAtomicJson'
)

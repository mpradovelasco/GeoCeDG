#requires -Version 7.2

# Generic, declarative phase-closeout workflow.
#
# The schema-v2 policy is always read as a blob from the explicit reviewed
# technical commit T.  T, C, a branch tip, or "latest" are never policy
# selectors. READINESS is read-only and mode-neutral: it validates exactly both
# routes and seals one reusable acceptance plan. The author selects a mode only
# after technical evidence/review. PREPARE may only materialize and stage the
# exact status-only delta reconstructed from the frozen policy. FINALIZE is the
# additional explicit VERIFIED-only commit/tag/fast-forward/push authority;
# AUDIT remains read-only apart from an optional generated result written by
# its CLI wrapper.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

. (Join-Path $PSScriptRoot 'phase-lifecycle.ps1')
. (Join-Path $PSScriptRoot 'repository-input-identity.ps1')
Import-Module (Join-Path $PSScriptRoot 'verification-runtime.psm1') -ErrorAction Stop

function Assert-GeoCeDGCloseoutWorkflow {
    param([bool]$Condition, [Parameter(Mandatory)] [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Get-GeoCeDGCloseoutRawSha256 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
}

function Get-GeoCeDGCloseoutTextSha256 {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    return Get-GeoCeDGCloseoutRawSha256 `
        ([Text.UTF8Encoding]::new($false).GetBytes($Text))
}

function ConvertTo-GeoCeDGCloseoutUtcTimestamp {
    param(
        [Parameter(Mandatory)] [object]$Value,
        [Parameter(Mandatory)] [string]$Description
    )
    $parsed = [datetimeoffset]::MinValue
    $valid = if ($Value -is [datetimeoffset]) {
        $parsed = [datetimeoffset]$Value
        $true
    } elseif ($Value -is [datetime]) {
        $dateTime = [datetime]$Value
        if ($dateTime.Kind -eq [DateTimeKind]::Unspecified) {
            $false
        } else {
            $parsed = [datetimeoffset]$dateTime
            $true
        }
    } else {
        $text = [string]$Value
        $text -cmatch '(?:Z|[+-][0-9]{2}:[0-9]{2})$' -and
            [datetimeoffset]::TryParseExact($text, 'o',
                [Globalization.CultureInfo]::InvariantCulture,
                [Globalization.DateTimeStyles]::RoundtripKind, [ref]$parsed)
    }
    Assert-GeoCeDGCloseoutWorkflow $valid `
        "$Description is not an exact offset-bearing round-trip timestamp."
    return $parsed.UtcDateTime.ToString('o',
        [Globalization.CultureInfo]::InvariantCulture)
}

function ConvertTo-GeoCeDGCloseoutMode {
    param([Parameter(Mandatory)] [string]$CloseoutMode)
    $normalized = $CloseoutMode.ToUpperInvariant()
    Assert-GeoCeDGCloseoutWorkflow ($normalized -cin @('VERIFIED', 'AUTHOR_OPERATED')) `
        "Unsupported closeout mode: $CloseoutMode"
    return $normalized
}

function ConvertTo-GeoCeDGCloseoutJsonBytes {
    param([Parameter(Mandatory)] [object]$Value)
    $text = (($Value | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n")
    return ,([Text.UTF8Encoding]::new($false).GetBytes($text))
}

function Compress-GeoCeDGCloseoutJsonWhitespace {
    param(
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text,
        [Parameter(Mandatory)] [string]$Description
    )
    $builder = [Text.StringBuilder]::new($Text.Length)
    $inString = $false
    $escaped = $false
    foreach ($character in $Text.ToCharArray()) {
        if ($inString) {
            [void]$builder.Append($character)
            if ($escaped) {
                $escaped = $false
            } elseif ($character -eq '\') {
                $escaped = $true
            } elseif ($character -eq '"') {
                $inString = $false
            }
        } elseif ($character -eq '"') {
            $inString = $true
            [void]$builder.Append($character)
        } elseif (-not [char]::IsWhiteSpace($character)) {
            [void]$builder.Append($character)
        }
    }
    Assert-GeoCeDGCloseoutWorkflow (-not $inString -and -not $escaped) `
        "$Description contains an unterminated JSON string."
    return $builder.ToString()
}

function Get-GeoCeDGCloseoutRawMinifiedJsonPropertyText {
    param(
        [Parameter(Mandatory)] [byte[]]$Bytes,
        [Parameter(Mandatory)] [string]$PropertyName,
        [Parameter(Mandatory)] [string]$Description
    )
    $text = ConvertFrom-GeoCeDGStrictUtf8 -Bytes $Bytes
    $document = [Text.Json.JsonDocument]::Parse($text)
    try {
        $property = [Text.Json.JsonElement]::new()
        $decodedNameCount = 0
        if ($document.RootElement.ValueKind -eq [Text.Json.JsonValueKind]::Object) {
            foreach ($candidate in $document.RootElement.EnumerateObject()) {
                if ($candidate.Name -ceq $PropertyName) { $decodedNameCount++ }
            }
        }
        Assert-GeoCeDGCloseoutWorkflow `
            ($document.RootElement.ValueKind -eq [Text.Json.JsonValueKind]::Object -and
                $decodedNameCount -eq 1 -and
                $document.RootElement.TryGetProperty($PropertyName, [ref]$property)) `
            "$Description does not contain exactly one '$PropertyName' property."
        $expectedNameToken = $PropertyName | ConvertTo-Json -Compress
        $canonicalNameCount = 0
        $depth = 0
        $inString = $false
        $escaped = $false
        $stringStart = -1
        for ($index = 0; $index -lt $text.Length; $index++) {
            $character = $text[$index]
            if ($inString) {
                if ($escaped) {
                    $escaped = $false
                } elseif ($character -eq '\') {
                    $escaped = $true
                } elseif ($character -eq '"') {
                    $inString = $false
                    if ($depth -eq 1) {
                        $cursor = $index + 1
                        while ($cursor -lt $text.Length -and
                                [char]::IsWhiteSpace($text[$cursor])) { $cursor++ }
                        if ($cursor -lt $text.Length -and $text[$cursor] -eq ':') {
                            $nameToken = $text.Substring($stringStart,
                                $index - $stringStart + 1)
                            if ($nameToken -ceq $expectedNameToken) {
                                $canonicalNameCount++
                            }
                        }
                    }
                }
            } else {
                switch ($character) {
                    '"' { $inString = $true; $stringStart = $index }
                    '{' { $depth++ }
                    '[' { $depth++ }
                    '}' { $depth-- }
                    ']' { $depth-- }
                }
            }
        }
        Assert-GeoCeDGCloseoutWorkflow ($canonicalNameCount -eq 1) `
            "$Description re-encoded the '$PropertyName' property name."
        # GetRawText retains the original string escapes, property order and
        # numeric tokens. Remove only insignificant JSON whitespace so an
        # equivalent re-encoding cannot masquerade as canonical producer output.
        return Compress-GeoCeDGCloseoutJsonWhitespace -Text $property.GetRawText() `
            -Description $Description
    } finally { $document.Dispose() }
}

function Invoke-GeoCeDGCloseoutGitText {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [switch]$AllowFailure
    )
    $result = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments $Arguments -AllowFailure:$AllowFailure
    $text = if ($result.Bytes.Length -eq 0) { '' } else {
        ConvertFrom-GeoCeDGStrictUtf8 -Bytes $result.Bytes
    }
    return [pscustomobject][ordered]@{
        ExitCode = [int]$result.ExitCode
        Text = $text.TrimEnd("`r", "`n")
        StandardError = [string]$result.StandardError
    }
}

function Get-GeoCeDGCloseoutCohortSnapshot {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    $head = Get-GeoCeDGCloseoutExactRef $RepositoryRoot 'HEAD' 'Campaign HEAD'
    $status = Invoke-GeoCeDGGitByteCommand -RepositoryRoot $RepositoryRoot `
        -Arguments @('status', '--porcelain=v1', '-z', '--untracked-files=all')
    return [pscustomobject][ordered]@{
        head = $head
        statusSha256 = Get-GeoCeDGCloseoutRawSha256 $status.Bytes
        clean = ($status.Bytes.Length -eq 0)
    }
}

function ConvertTo-GeoCeDGCloseoutRemoteUrlAuthority {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Url
    )
    Assert-GeoCeDGCloseoutWorkflow (-not [string]::IsNullOrWhiteSpace($Url) -and
        $Url -notmatch '[\r\n]') 'Remote URL authority is empty or malformed.'

    # The configured URL is sealed byte-for-byte but never persisted.  The
    # canonical identity deliberately removes user-info, query and fragment so
    # an audit result cannot disclose embedded credentials.  The raw URL hash
    # still makes any credential/URL configuration change fail closed.
    $kind = 'OPAQUE'
    $canonical = $Url
    $localLike = [IO.Path]::IsPathRooted($Url) -or $Url.StartsWith('./') -or
        $Url.StartsWith('../') -or $Url.StartsWith('.\') -or $Url.StartsWith('..\')
    if ($localLike) {
        $kind = 'LOCAL_PATH'
        $candidate = if ([IO.Path]::IsPathRooted($Url)) {
            $Url
        } else { Join-Path $RepositoryRoot $Url }
        $canonical = [IO.Path]::GetFullPath($candidate).Replace('\', '/')
        if ($IsWindows) { $canonical = $canonical.ToLowerInvariant() }
    } elseif (-not $Url.Contains('://') -and
            $Url -cmatch '^(?:[^@/:]+@)?(?<host>[^/:]+):(?<path>.+)$') {
        $kind = 'SSH_SCP'
        $hostName = $Matches['host'].ToLowerInvariant()
        $remotePath = $Matches['path'].Replace('\', '/').TrimEnd('/')
        $canonical = "ssh-scp://$hostName/$remotePath"
    } else {
        $uri = $null
        if ([Uri]::TryCreate($Url, [UriKind]::Absolute, [ref]$uri)) {
            $scheme = $uri.Scheme.ToLowerInvariant()
            $kind = $scheme.ToUpperInvariant()
            $hostName = $uri.IdnHost.ToLowerInvariant()
            $port = if ($uri.IsDefaultPort) { '' } else { ":$($uri.Port)" }
            $remotePath = $uri.GetComponents([UriComponents]::Path,
                [UriFormat]::UriEscaped).TrimEnd('/')
            $canonical = "${scheme}://${hostName}${port}/$remotePath"
        }
    }
    return [pscustomobject][ordered]@{
        urlKind = $kind
        configuredUrlSha256 = Get-GeoCeDGCloseoutTextSha256 $Url
        canonicalIdentitySha256 = Get-GeoCeDGCloseoutTextSha256 $canonical
        credentialMaterialPersisted = $false
    }
}

function Get-GeoCeDGCloseoutRemoteIdentity {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Remote
    )
    Assert-GeoCeDGCloseoutWorkflow ($Remote -cmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$') `
        'Remote name is malformed.'
    $fetchResult = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('remote', 'get-url', '--all', $Remote)
    $pushResult = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('remote', 'get-url', '--push', '--all', $Remote)
    $fetchUrls = @($fetchResult.Text -split "`n" | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        })
    $pushUrls = @($pushResult.Text -split "`n" | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        })
    Assert-GeoCeDGCloseoutWorkflow ($fetchUrls.Count -eq 1 -and $pushUrls.Count -eq 1) `
        'Closeout requires exactly one configured fetch URL and one push URL for the promotion remote.'
    $fetchAuthority = ConvertTo-GeoCeDGCloseoutRemoteUrlAuthority `
        $RepositoryRoot $fetchUrls[0]
    $pushAuthority = ConvertTo-GeoCeDGCloseoutRemoteUrlAuthority `
        $RepositoryRoot $pushUrls[0]
    Assert-GeoCeDGCloseoutWorkflow ([string]$fetchAuthority.configuredUrlSha256 -ceq
        [string]$pushAuthority.configuredUrlSha256) `
        'Promotion remote has a divergent push URL; the schema-v2 live audit route is not single-authority.'
    $identity = [pscustomobject][ordered]@{
        remote = $Remote
        fetch = $fetchAuthority
        push = $pushAuthority
        fetchAndPushUrlIdentical = $true
        credentialMaterialPersisted = $false
    }
    return [pscustomobject][ordered]@{
        remote = $identity.remote
        fetch = $identity.fetch
        push = $identity.push
        fetchAndPushUrlIdentical = $true
        credentialMaterialPersisted = $false
        authoritySha256 = Get-GeoCeDGCloseoutRawSha256 `
            (ConvertTo-GeoCeDGCloseoutJsonBytes $identity)
    }
}

function Assert-GeoCeDGCloseoutBoundedPushConfiguration {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Remote
    )
    $mirror = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('config', '--type=bool', '--get-all', "remote.$Remote.mirror") `
        -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($mirror.ExitCode -in @(0, 1)) `
        'Unable to inspect effective remote mirror configuration.'
    if ($mirror.ExitCode -eq 0) {
        $values = @($mirror.Text -split "`n" | Where-Object {
                -not [string]::IsNullOrWhiteSpace($_)
            })
        Assert-GeoCeDGCloseoutWorkflow ($values.Count -gt 0 -and
            @($values | Where-Object { $_ -cne 'false' }).Count -eq 0) `
            'Closeout promotion rejects remote mirror configuration because it can expand the pushed ref set.'
    }
    $pushOptions = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('config', '--get-all', 'push.pushOption') -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($pushOptions.ExitCode -in @(0, 1)) `
        'Unable to inspect effective configured push options.'
    Assert-GeoCeDGCloseoutWorkflow ($pushOptions.ExitCode -eq 1 -and
        [string]::IsNullOrWhiteSpace($pushOptions.Text)) `
        'Closeout promotion rejects configured push options because Git cannot portably clear their inherited values.'
    return [pscustomobject][ordered]@{
        remoteMirrorEnabled = $false
        explicitRefspecsOnly = $true
        followTagsDisabled = $true
        recursiveSubmodulePushDisabled = $true
        configuredPushOptionsAbsent = $true
        prePushHookDisabled = $true
    }
}

function Assert-GeoCeDGCloseoutRemoteIdentityShape {
    param(
        [Parameter(Mandatory)] [object]$Identity,
        [Parameter(Mandatory)] [string]$Remote
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Identity @('remote', 'fetch', 'push',
        'fetchAndPushUrlIdentical', 'credentialMaterialPersisted',
        'authoritySha256') 'Readiness remote identity'
    foreach ($name in @('fetch', 'push')) {
        $entry = $Identity.$name
        Assert-GeoCeDGPhaseLifecycleProperties $entry @('urlKind',
            'configuredUrlSha256', 'canonicalIdentitySha256',
            'credentialMaterialPersisted') "Readiness remote $name URL identity"
        Assert-GeoCeDGCloseoutWorkflow (-not [string]::IsNullOrWhiteSpace(
                [string]$entry.urlKind) -and
            [string]$entry.configuredUrlSha256 -cmatch '^[0-9a-f]{64}$' -and
            [string]$entry.canonicalIdentitySha256 -cmatch '^[0-9a-f]{64}$' -and
            $entry.credentialMaterialPersisted -is [bool] -and
            -not $entry.credentialMaterialPersisted) `
            "Readiness remote $name URL identity is invalid."
    }
    Assert-GeoCeDGCloseoutWorkflow ([string]$Identity.remote -ceq $Remote -and
        $Identity.fetchAndPushUrlIdentical -is [bool] -and
        $Identity.fetchAndPushUrlIdentical -and
        [string]$Identity.fetch.configuredUrlSha256 -ceq
            [string]$Identity.push.configuredUrlSha256 -and
        $Identity.credentialMaterialPersisted -is [bool] -and
        -not $Identity.credentialMaterialPersisted -and
        [string]$Identity.authoritySha256 -cmatch '^[0-9a-f]{64}$') `
        'Readiness remote identity binding is invalid.'
}

function Get-GeoCeDGCloseoutExactRef {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Ref,
        [Parameter(Mandatory)] [string]$Description
    )
    $result = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('rev-parse', '--verify', "${Ref}^{commit}")
    Assert-GeoCeDGCloseoutWorkflow ($result.Text -cmatch '^[0-9a-f]{40}$') `
        "$Description is not an exact commit ref."
    return $result.Text
}

function Get-GeoCeDGCloseoutLiveBranchRef {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Remote,
        [Parameter(Mandatory)] [string]$Branch
    )
    $ref = "refs/heads/$Branch"
    $result = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('ls-remote', '--exit-code', '--heads', $Remote, $ref) -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($result.ExitCode -eq 0) `
        "Unable to resolve the live $Remote/$Branch promotion ref."
    $lines = @($result.Text -split "`n" | Where-Object {
        -not [string]::IsNullOrWhiteSpace($_)
    })
    $pattern = '^([0-9a-f]{40})\t' + [regex]::Escape($ref) + '$'
    Assert-GeoCeDGCloseoutWorkflow ($lines.Count -eq 1 -and $lines[0] -cmatch $pattern) `
        "Live $Remote/$Branch promotion ref is ambiguous or malformed."
    return $Matches[1]
}

function Test-GeoCeDGCloseoutTagAbsent {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Remote,
        [Parameter(Mandatory)] [string]$TagName
    )
    $tagRef = "refs/tags/$TagName"
    $local = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('show-ref', '--verify', '--quiet', $tagRef) -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($local.ExitCode -in @(0, 1)) `
        'Unable to determine whether the expected local phase tag exists.'
    $remoteQuery = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('ls-remote', '--exit-code', '--tags', $Remote, $tagRef, "${tagRef}^{}") `
        -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($remoteQuery.ExitCode -in @(0, 2)) `
        'Unable to determine whether the expected live phase tag exists.'
    return [pscustomobject][ordered]@{
        local = ($local.ExitCode -eq 1)
        remote = ($remoteQuery.ExitCode -eq 2 -and
            [string]::IsNullOrWhiteSpace($remoteQuery.Text))
    }
}

function Assert-GeoCeDGCloseoutStatusOnlyPath {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [switch]$Record
    )
    $safe = ConvertTo-GeoCeDGPhaseLifecyclePath $Path 'schema-v2 closeout'
    $allowedRoot = $safe.StartsWith('docs/', [StringComparison]::Ordinal) -or
        $safe.StartsWith('geocedg/specs/operations/', [StringComparison]::Ordinal) -or
        ($Record -and $safe.StartsWith(
                'geocedg/validation/', [StringComparison]::Ordinal))
    Assert-GeoCeDGCloseoutWorkflow $allowedRoot `
        "Closeout path is outside operational status/documentation authority: $safe"
    Assert-GeoCeDGCloseoutWorkflow ($safe -cnotmatch `
        '^(source|tools|gradle|buildSrc|packaging|python|models|benchmarks)/') `
        "Closeout path reaches an executable/product tree: $safe"
    Assert-GeoCeDGCloseoutWorkflow ($safe -cnotmatch `
        '(?i)(^|/)(test|tests|fixture|fixtures|baseline|baselines|reference|references|tolerance|tolerances)(/|$)') `
        "Closeout path reaches a test, tolerance, baseline, or reference authority: $safe"
    Assert-GeoCeDGCloseoutWorkflow ($safe -cnotmatch `
        '(?i)(scenario|baseline|tolerance|reference|\.sha256$|\.(java|kt|kts|gradle|ps1|psm1|py|js|ts|c|cc|cpp|h|hpp|ggb|ggt)$)') `
        "Closeout path is not status/documentation-only: $safe"
    if ($Record) {
        Assert-GeoCeDGCloseoutWorkflow ($safe.StartsWith(
                'geocedg/validation/', [StringComparison]::Ordinal) -and
            $safe.EndsWith('.json', [StringComparison]::Ordinal)) `
            'The only new closeout JSON must be the decision record under geocedg/validation/.'
    } else {
        Assert-GeoCeDGCloseoutWorkflow ($safe.EndsWith(
                '.md', [StringComparison]::Ordinal)) `
            "Schema-v2 status replacements are restricted to Markdown: $safe"
    }
    return $safe
}

function Read-GeoCeDGCloseoutWorkflowPolicy {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $relative = ConvertTo-GeoCeDGRepositoryPath -RepositoryRoot $root -Path $PolicyPath
    $relative = ConvertTo-GeoCeDGPhaseLifecyclePath $relative 'schema-v2 policy'
    $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $root $technical $relative
    $text = ConvertFrom-GeoCeDGStrictUtf8 -Bytes $bytes
    Assert-GeoCeDGCloseoutWorkflow (-not $text.Contains(
            $technical, [StringComparison]::OrdinalIgnoreCase)) `
        'Frozen closeout policy contains an impossible self-reference to T.'
    $policy = ConvertFrom-GeoCeDGPhaseLifecycleJson $bytes 'schema-v2 closeout policy'
    Assert-GeoCeDGPhaseLifecycleProperties $policy @('schemaVersion', 'phase',
        'technicalEvidence', 'promotion', 'closeout') 'Schema-v2 closeout policy'
    Assert-GeoCeDGCloseoutWorkflow ($policy.schemaVersion -is [long] -and
        $policy.schemaVersion -eq 2) 'Unsupported closeout-workflow policy schema.'
    Assert-GeoCeDGCloseoutWorkflow ($policy.phase -is [string] -and
        [string]$policy.phase -cmatch
        '^[A-Z0-9][A-Z0-9.-]+$') 'Invalid canonical PHASE identifier in policy.'

    Assert-GeoCeDGPhaseLifecycleProperties $policy.technicalEvidence `
        @('requiredLevels', 'campaignStrategy', 'physicalCampaignLevel',
            'requiredClaims', 'complements', 'fullRequiresCleanBuild',
            'lifecycleRepairRequired') `
        'Closeout technical-evidence policy'
    Assert-GeoCeDGCloseoutWorkflow ($policy.technicalEvidence.requiredLevels -is [Array] -and
        $policy.technicalEvidence.requiredClaims -is [Array] -and
        $policy.technicalEvidence.complements -is [Array]) `
        'Schema-v2 technical evidence levels, claims, and complements must be JSON arrays.'
    Assert-GeoCeDGCloseoutWorkflow (@($policy.technicalEvidence.requiredLevels |
            Where-Object { $_ -isnot [string] }).Count -eq 0) `
        'Schema-v2 technical evidence levels must be JSON strings.'
    $requiredLevels = @($policy.technicalEvidence.requiredLevels | ForEach-Object {
        [string]$_
    })
    Assert-GeoCeDGCloseoutWorkflow (($requiredLevels -join "`n") -ceq
        (@('PHASE', 'COMPOSED', 'FULL') -join "`n")) `
        'Closeout acceptance must retain exact PHASE, COMPOSED, and FULL coverage.'
    Assert-GeoCeDGCloseoutWorkflow ($policy.technicalEvidence.campaignStrategy -is [string] -and
        $policy.technicalEvidence.physicalCampaignLevel -is [string] -and
        [string]$policy.technicalEvidence.campaignStrategy -ceq
        'SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS' -and
        [string]$policy.technicalEvidence.physicalCampaignLevel -ceq 'FULL') `
        'Schema-v2 acceptance requires one physical FULL campaign with authenticated coverage claims.'
    $claims = @($policy.technicalEvidence.requiredClaims)
    Assert-GeoCeDGCloseoutWorkflow ($claims.Count -eq 3) `
        'Schema-v2 acceptance requires exactly PHASE, COMPOSED, and FULL claims.'
    Assert-GeoCeDGPhaseLifecycleProperties $claims[0] @('level', 'fulfillment',
        'orchestratorPath', 'verifierPath', 'evidencePath', 'expectedState') `
        'Schema-v2 PHASE coverage claim'
    Assert-GeoCeDGPhaseLifecycleProperties $claims[1] @('level', 'fulfillment',
        'sourceLevel', 'canonicalSelection') 'Schema-v2 COMPOSED coverage claim'
    Assert-GeoCeDGPhaseLifecycleProperties $claims[2] @('level', 'fulfillment',
        'sourceLevel', 'canonicalSelection') 'Schema-v2 FULL coverage claim'
    $claimStringProperties = @(
        @('level', 'fulfillment', 'orchestratorPath', 'verifierPath',
            'evidencePath', 'expectedState'),
        @('level', 'fulfillment', 'sourceLevel', 'canonicalSelection'),
        @('level', 'fulfillment', 'sourceLevel', 'canonicalSelection')
    )
    for ($claimIndex = 0; $claimIndex -lt $claims.Count; $claimIndex++) {
        Assert-GeoCeDGCloseoutWorkflow (@($claimStringProperties[$claimIndex] |
                Where-Object { $claims[$claimIndex].$_ -isnot [string] }).Count -eq 0) `
            "Schema-v2 coverage claim $claimIndex values must be JSON strings."
    }
    $phaseIntegration = Get-GeoCeDGAcceptancePhaseIntegration `
        -Phase ([string]$policy.phase)
    $phaseOrchestrator = ConvertTo-GeoCeDGPhaseLifecyclePath `
        ([string]$claims[0].orchestratorPath) 'PHASE claim orchestrator'
    $phaseVerifier = ConvertTo-GeoCeDGPhaseLifecyclePath `
        ([string]$claims[0].verifierPath) 'PHASE claim verifier'
    $phaseEvidence = ConvertTo-GeoCeDGPhaseLifecyclePath `
        ([string]$claims[0].evidencePath) 'PHASE claim evidence'
    Assert-GeoCeDGCloseoutWorkflow ([string]$claims[0].level -ceq 'PHASE' -and
        [string]$claims[0].fulfillment -ceq 'NESTED_EXECUTED' -and
        $phaseOrchestrator -ceq [string]$phaseIntegration.orchestratorPath -and
        $phaseVerifier -ceq [string]$phaseIntegration.verifierPath -and
        $phaseEvidence -ceq [string]$phaseIntegration.evidencePath -and
        [string]$claims[0].expectedState -ceq
            [string]$phaseIntegration.expectedState -and
        $phaseOrchestrator.StartsWith('tools/agent/', [StringComparison]::Ordinal) -and
        $phaseOrchestrator.EndsWith('.ps1', [StringComparison]::Ordinal) -and
        $phaseVerifier.StartsWith('tools/agent/', [StringComparison]::Ordinal) -and
        $phaseVerifier.EndsWith('.ps1', [StringComparison]::Ordinal) -and
        $phaseEvidence.EndsWith('.json', [StringComparison]::Ordinal) -and
        [string]$claims[0].expectedState -cmatch '^[A-Z][A-Z0-9_]*$') `
        'Schema-v2 PHASE claim does not match the canonical phase-to-verifier mapping.'
    Assert-GeoCeDGCloseoutWorkflow ([string]$claims[1].level -ceq 'COMPOSED' -and
        [string]$claims[1].fulfillment -ceq 'NORMATIVE_SUBSUMPTION' -and
        [string]$claims[1].sourceLevel -ceq 'FULL' -and
        [string]$claims[1].canonicalSelection -ceq
            'UNFILTERED_SHARED_AND_DESKTOP' -and
        [string]$claims[2].level -ceq 'FULL' -and
        [string]$claims[2].fulfillment -ceq 'ROOT_PHYSICAL_CAMPAIGN' -and
        [string]$claims[2].sourceLevel -ceq 'FULL' -and
        [string]$claims[2].canonicalSelection -ceq
            'UNFILTERED_SHARED_AND_DESKTOP') `
        'Schema-v2 COMPOSED/FULL claim semantics are invalid.'
    Assert-GeoCeDGCloseoutWorkflow (@($policy.technicalEvidence.complements).Count -eq 0) `
        'This closeout policy declares no acceptance complement; none may be inferred.'
    Assert-GeoCeDGCloseoutWorkflow ($policy.technicalEvidence.fullRequiresCleanBuild -is [bool] -and
        $policy.technicalEvidence.fullRequiresCleanBuild) `
        'Schema-v2 acceptance requires clean-output FULL.'
    Assert-GeoCeDGCloseoutWorkflow ($policy.technicalEvidence.lifecycleRepairRequired -is [bool] -and
        -not $policy.technicalEvidence.lifecycleRepairRequired) `
        'Known lifecycle repair blocks CLOSEOUT_READINESS.'

    Assert-GeoCeDGPhaseLifecycleProperties $policy.promotion @('branch', 'remote',
        'tagName', 'tagMessage', 'closeoutCommitMessage') 'Closeout promotion policy'
    Assert-GeoCeDGCloseoutWorkflow (@(@('branch', 'remote', 'tagName', 'tagMessage',
            'closeoutCommitMessage') | Where-Object {
                $policy.promotion.$_ -isnot [string]
            }).Count -eq 0) `
        'Schema-v2 promotion values must be JSON strings.'
    Assert-GeoCeDGCloseoutWorkflow ([string]$policy.promotion.branch -ceq 'main' -and
        [string]$policy.promotion.remote -ceq 'origin') `
        'Schema-v2 closeout promotion must target explicit main/origin authority.'
    $tagName = [string]$policy.promotion.tagName
    Assert-GeoCeDGCloseoutWorkflow ($tagName -cmatch
        '^(?!.*\.\.)(?!.*\.lock$)[a-z0-9](?:[a-z0-9.-]*[a-z0-9-])?$') `
        'Invalid expected annotated phase tag name.'
    $tagRefCheck = Invoke-GeoCeDGCloseoutGitText $root `
        @('check-ref-format', "refs/tags/$tagName") -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($tagRefCheck.ExitCode -eq 0) `
        'Expected annotated phase tag is not a valid exact Git ref.'
    foreach ($name in @('tagMessage', 'closeoutCommitMessage')) {
        $value = [string]$policy.promotion.$name
        Assert-GeoCeDGCloseoutWorkflow (-not [string]::IsNullOrWhiteSpace($value) -and
            $value -notmatch '[\x00-\x1f\x7f]') "Invalid promotion $name."
    }

    Assert-GeoCeDGPhaseLifecycleProperties $policy.closeout @('supportedModes',
        'recordPath', 'literalReplacements', 'canonicalLfHashManifests') `
        'Schema-v2 closeout declaration'
    Assert-GeoCeDGCloseoutWorkflow ($policy.closeout.supportedModes -is [Array] -and
        $policy.closeout.literalReplacements -is [Array] -and
        $policy.closeout.canonicalLfHashManifests -is [Array]) `
        'Schema-v2 supported modes, replacements, and manifests must be JSON arrays.'
    Assert-GeoCeDGCloseoutWorkflow (@($policy.closeout.supportedModes |
            Where-Object { $_ -isnot [string] }).Count -eq 0) `
        'Schema-v2 supported modes must be JSON strings.'
    $supported = @($policy.closeout.supportedModes | ForEach-Object { [string]$_ })
    Assert-GeoCeDGPhaseLifecycleSet $supported @('VERIFIED', 'AUTHOR_OPERATED') `
        'Schema-v2 closeout modes'
    Assert-GeoCeDGCloseoutWorkflow ($policy.closeout.recordPath -is [string]) `
        'Schema-v2 closeout recordPath must be a JSON string.'
    $recordPath = Assert-GeoCeDGCloseoutStatusOnlyPath `
        ([string]$policy.closeout.recordPath) -Record
    Assert-GeoCeDGCloseoutWorkflow ($relative -cne $recordPath) `
        'Closeout policy cannot be its own status-only output.'
    foreach ($rule in @($policy.closeout.literalReplacements)) {
        Assert-GeoCeDGPhaseLifecycleProperties $rule @('path', 'before', 'after',
            'occurrences') 'Schema-v2 literal replacement'
        Assert-GeoCeDGCloseoutWorkflow ($rule.path -is [string] -and
            $rule.before -is [string] -and $rule.after -is [string]) `
            'Schema-v2 literal replacement path/before/after values must be JSON strings.'
        $path = Assert-GeoCeDGCloseoutStatusOnlyPath ([string]$rule.path)
        Assert-GeoCeDGCloseoutWorkflow ($path -cne $relative -and $path -cne $recordPath) `
            "Closeout replacement collides with policy or record: $path"
        Assert-GeoCeDGCloseoutWorkflow (-not [string]::IsNullOrEmpty([string]$rule.before) -and
            [string]$rule.before -cne [string]$rule.after -and
            $rule.occurrences -is [long] -and $rule.occurrences -gt 0) `
            "Invalid schema-v2 literal replacement: $path"
    }
    Assert-GeoCeDGCloseoutWorkflow (@($policy.closeout.canonicalLfHashManifests).Count -eq 0) `
        'Schema-v2 status closeout cannot rewrite executable/reference hash manifests.'

    return [pscustomobject][ordered]@{
        policy = $policy
        policyPath = $relative
        policyBlobSha256 = Get-GeoCeDGCloseoutRawSha256 $bytes
        policyBytes = $bytes
        reviewedTechnicalCommit = $technical
    }
}

function Get-GeoCeDGCloseoutAcceptanceExecutionPlan {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [object]$PolicyContext
    )
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $TechnicalCommit
    $claims = @($PolicyContext.policy.technicalEvidence.requiredClaims)
    $phase = $claims[0]
    $phaseIntegration = Get-GeoCeDGAcceptancePhaseIntegration `
        -Phase ([string]$PolicyContext.policy.phase)
    $composedRuntimePlan = Get-GeoCeDGVerificationExecutionPlan `
        -Level COMPOSED -Phase ([string]$PolicyContext.policy.phase)
    $fullRuntimePlan = Get-GeoCeDGVerificationExecutionPlan `
        -Level FULL -Phase ([string]$PolicyContext.policy.phase)
    $composedSelection = $composedRuntimePlan.canonicalBuild.selection
    $fullSelection = $fullRuntimePlan.canonicalBuild.selection
    $authorityPaths = [ordered]@{
        root = 'tools/agent/verify.ps1'
        baseline = 'tools/agent/verify-baseline.ps1'
        phaseOrchestrator = [string]$phase.orchestratorPath
        phaseVerifier = [string]$phase.verifierPath
        canonicalRuntime = 'tools/agent/verification-runtime.psm1'
    }
    $authorities = [ordered]@{}
    foreach ($entry in $authorityPaths.GetEnumerator()) {
        $path = ConvertTo-GeoCeDGPhaseLifecyclePath ([string]$entry.Value) `
            "acceptance execution authority $($entry.Key)"
        $bytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $technical $path
        $authorities[[string]$entry.Key] = [ordered]@{
            path = $path
            blobSha256 = Get-GeoCeDGCloseoutRawSha256 $bytes
        }
    }
    $normalizedClaims = @(
        [ordered]@{
            level = 'PHASE'
            fulfillment = 'NESTED_EXECUTED'
            independentPhysicalCampaign = $false
            orchestratorPath = [string]$phase.orchestratorPath
            verifierPath = [string]$phase.verifierPath
            evidencePath = [string]$phase.evidencePath
            expectedState = [string]$phase.expectedState
        },
        [ordered]@{
            level = 'COMPOSED'
            fulfillment = 'NORMATIVE_SUBSUMPTION'
            independentPhysicalCampaign = $false
            sourceLevel = 'FULL'
            canonicalSelection = 'UNFILTERED_SHARED_AND_DESKTOP'
        },
        [ordered]@{
            level = 'FULL'
            fulfillment = 'ROOT_PHYSICAL_CAMPAIGN'
            independentPhysicalCampaign = $true
            sourceLevel = 'FULL'
            canonicalSelection = 'UNFILTERED_SHARED_AND_DESKTOP'
        }
    )
    $commonGateIds = [object[]]@($fullRuntimePlan.commonGateIds)
    $composedNeutral = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $composedRuntimePlan) `
        'neutralized COMPOSED runtime execution plan'
    $fullNeutral = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $fullRuntimePlan) `
        'neutralized FULL runtime execution plan'
    foreach ($candidate in @($composedNeutral, $fullNeutral)) {
        $candidate.level = 'LEVEL_ROLE'
        $candidate.canonicalBuild.level = 'LEVEL_ROLE'
        $candidate.canonicalBuild.selection = 'CANONICAL_SELECTION_ROLE'
        $candidate.baseline.fullTests = 'BASELINE_FULL_TESTS_ROLE'
    }
    $composedNeutralBytes = ConvertTo-GeoCeDGCloseoutJsonBytes $composedNeutral
    $fullNeutralBytes = ConvertTo-GeoCeDGCloseoutJsonBytes $fullNeutral
    Assert-GeoCeDGCloseoutWorkflow ([Convert]::ToBase64String(
            $composedNeutralBytes) -ceq [Convert]::ToBase64String(
            $fullNeutralBytes)) `
        'Central COMPOSED/FULL execution plans differ outside declared dimensions.'
    Assert-GeoCeDGCloseoutWorkflow (-not [bool]$composedRuntimePlan.baseline.fullTests `
        -and [bool]$fullRuntimePlan.baseline.fullTests) `
        'Central COMPOSED/FULL baseline FullTests distinction is invalid.'
    $commonGatePlanBasis = [ordered]@{
        authority = $authorities.root
        runtimeAuthority = $authorities.canonicalRuntime
        gateIds = $commonGateIds
    }
    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_SINGLE_FULL_ACCEPTANCE_EXECUTION_PLAN'
        strategy = 'SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS'
        reviewedTechnicalCommit = $technical
        phase = [string]$PolicyContext.policy.phase
        policyPath = [string]$PolicyContext.policyPath
        policyBlobSha256 = [string]$PolicyContext.policyBlobSha256
        requiredLevels = @('PHASE', 'COMPOSED', 'FULL')
        physicalCampaign = [ordered]@{
            level = 'FULL'
            count = 1
            cleanGeneratedOutputs = $true
            independentBuilds = $false
            canonicalSelection = $fullSelection
        }
        normativeSelections = [ordered]@{
            COMPOSED = $composedSelection
            FULL = $fullSelection
        }
        runtimeExecutionPlans = [ordered]@{
            COMPOSED = $composedRuntimePlan
            FULL = $fullRuntimePlan
        }
        claims = $normalizedClaims
        complements = [object[]]::new(0)
        expectedEvidenceInventory = @(
            [ordered]@{ role = 'FULL_CANONICAL_RECEIPT'; cardinality = 1 },
            [ordered]@{ role = 'PHASE_CHILD_SUMMARY'; cardinality = 1 },
            [ordered]@{ role = 'PHASE_CHILD_ARTIFACT'; minimumCardinality = 1 },
            [ordered]@{ role = 'BASELINE_FULL_CONFIRMATION'; cardinality = 1 },
            [ordered]@{ role = 'ROOT_COMMON_GATE_COMPLETION'; cardinality = 1 }
        )
        authorities = $authorities
        commonGatePlan = $commonGateIds
        commonGatePlanSha256 = Get-GeoCeDGCloseoutStructuredSha256 `
            $commonGatePlanBasis
        coverageComparison = [ordered]@{
            phaseIntegration = $phaseIntegration
            composedAndFullCommonGateAuthorityIdentical = $true
            centralRuntimePlansCompared = $true
            neutralizedRuntimePlanSha256 = `
                Get-GeoCeDGCloseoutRawSha256 $fullNeutralBytes
            allowedComposedToFullDifferences = [object[]]@(
                'CANONICAL_SELECTION_FILTERED_TO_UNFILTERED',
                'BASELINE_FULL_TESTS_ENABLED'
            )
            undeclaredComplements = [object[]]::new(0)
        }
        selection = [ordered]@{
            algorithm = 'MINIMUM_DECLARED_PHYSICAL_CAMPAIGNS'
            declaredCandidateLevels = @('PHASE', 'COMPOSED', 'FULL')
            selectedPhysicalLevels = @('FULL')
            selectedPhysicalCampaignCount = 1
            coverageReduced = $false
            independentRunsClaimedForDerivedLevels = $false
        }
    }
}

function Get-GeoCeDGCloseoutPlan {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [object]$PolicyContext
    )
    $expected = Get-GeoCeDGPhaseExpectedCloseoutBytes $RepositoryRoot `
        $TechnicalCommit $PolicyContext.policy
    $recordPath = [string]$PolicyContext.policy.closeout.recordPath
    Assert-GeoCeDGCloseoutWorkflow (-not $expected.Contains($recordPath)) `
        'Closeout record collides with a generated status path.'
    $paths = [string[]]@($expected.Keys)
    [Array]::Sort($paths, [StringComparer]::Ordinal)
    Assert-GeoCeDGCloseoutWorkflow ($paths.Count -eq @($paths |
            Sort-Object -Unique -CaseSensitive).Count) `
        'Closeout plan contains duplicate status paths.'
    $lines = [Collections.Generic.List[string]]::new()
    $modes = [Collections.Generic.List[object]]::new()
    foreach ($path in $paths) {
        [void](Assert-GeoCeDGCloseoutStatusOnlyPath $path)
        $before = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot $TechnicalCommit $path
        Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 $before) -cne
            (Get-GeoCeDGCloseoutRawSha256 ([byte[]]$expected[$path]))) `
            "Closeout route does not change declared status path: $path"
        $treeLine = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
            @('ls-tree', $TechnicalCommit, '--', $path)).Text
        $match = [regex]::Match($treeLine, '^([0-7]{6}) blob [0-9a-f]{40}\t(.+)$')
        Assert-GeoCeDGCloseoutWorkflow ($match.Success -and
            $match.Groups[2].Value -ceq $path -and
            $match.Groups[1].Value -ceq '100644') `
            "Closeout status path has unsupported Git identity: $path"
        $hash = Get-GeoCeDGCloseoutRawSha256 ([byte[]]$expected[$path])
        $lines.Add("$path`0$($match.Groups[1].Value)`0$hash")
        $modes.Add([pscustomobject][ordered]@{
            path = $path
            mode = $match.Groups[1].Value
            expectedSha256 = $hash
        })
    }
    $recordExists = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('cat-file', '-e', "${TechnicalCommit}:$recordPath") -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($recordExists.ExitCode -ne 0) `
        'The schema-v2 closeout record already exists at T.'
    $recordParent = Split-Path -Parent (Resolve-GeoCeDGPhaseLifecycleChild `
        $RepositoryRoot $recordPath 'schema-v2 closeout record')
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $recordParent -PathType Container) `
        'Closeout record parent directory does not exist at readiness.'
    $lines.Add("$recordPath`0100644`0AUTHOR_APPROVAL_AND_EVIDENCE_BOUND_AT_PREPARE")
    $planHash = Get-GeoCeDGCloseoutRawSha256 `
        ([Text.UTF8Encoding]::new($false).GetBytes(($lines -join "`n") + "`n"))
    return [pscustomobject][ordered]@{
        expectedBytes = $expected
        statusPaths = $paths
        recordPath = $recordPath
        allPaths = @($paths + $recordPath)
        trackedModes = @($modes)
        contentPlanSha256 = $planHash
    }
}

function Get-GeoCeDGCloseoutRecordCreationAuthority {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$RecordPath,
        [switch]$RequireAbsent
    )
    $physical = Resolve-GeoCeDGPhaseLifecycleChild $RepositoryRoot $RecordPath `
        'schema-v2 closeout record'
    if ($RequireAbsent) {
        Assert-GeoCeDGCloseoutWorkflow (-not (Test-Path -LiteralPath $physical)) `
            'CLOSEOUT_READINESS requires the future decision-record path to be physically absent.'
        $tracked = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
            @('ls-files', '--error-unmatch', '--', $RecordPath) -AllowFailure
        Assert-GeoCeDGCloseoutWorkflow ($tracked.ExitCode -eq 1) `
            'CLOSEOUT_READINESS requires the future decision-record path to be absent from the index.'
    } elseif (Test-Path -LiteralPath $physical) {
        Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $physical -PathType Leaf) `
            'Closeout decision-record materialization is not a regular file.'
    }
    $ignored = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('check-ignore', '--quiet', '--no-index', '--', $RecordPath) -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($ignored.ExitCode -eq 1) `
        'Closeout decision-record path is ignored or its ignore authority cannot be proven.'

    $config = Get-GeoCeDGMaterializationConfig $RepositoryRoot
    $cachedAttributes = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot `
        @($RecordPath) -ConfiguredFilterDrivers $config.configuredFilterDrivers -Cached
    $physicalAttributes = Assert-GeoCeDGMaterializationAttributes $RepositoryRoot `
        @($RecordPath) -ConfiguredFilterDrivers $config.configuredFilterDrivers
    Assert-GeoCeDGCloseoutWorkflow ($cachedAttributes -ceq $physicalAttributes) `
        'Index and worktree attribute authorities differ for the future decision record.'
    return [pscustomobject][ordered]@{
        path = $RecordPath
        notIgnored = $true
        expectedMode = '100644'
        cachedAttributesSha256 = $cachedAttributes
        physicalAttributesSha256 = $physicalAttributes
        materializationConfigurationSha256 = [string]$config.relevantConfigurationSha256
        activeCleanFilter = $false
        workingTreeEncoding = $false
        identExpansion = $false
        pathTraversalReparseFree = $true
        regularBlobCreationReproducible = $true
    }
}

function Assert-GeoCeDGCloseoutLinearTechnicalHistory {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$AnchorCommit,
        [Parameter(Mandatory)] [string]$TechnicalCommit
    )
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $AnchorCommit)
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $TechnicalCommit)
    $ancestor = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('merge-base', '--is-ancestor', $AnchorCommit, $TechnicalCommit) -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($ancestor.ExitCode -eq 0) `
        'Technical commit T is not a fast-forward descendant of the promotion anchor.'
    $cursor = $TechnicalCommit
    $count = 0
    while ($cursor -cne $AnchorCommit) {
        $count++
        Assert-GeoCeDGCloseoutWorkflow ($count -le 10000) `
            'Technical ancestry exceeded the bounded linear-history readiness audit.'
        $line = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
            @('rev-list', '--parents', '-n', '1', $cursor)).Text
        $parts = @($line -split ' ')
        Assert-GeoCeDGCloseoutWorkflow ($parts.Count -eq 2 -and
            $parts[0] -ceq $cursor) `
            'Promotion anchor M0..T contains a merge, multiple parents, or malformed replacement history.'
        $cursor = $parts[1]
    }
    return [pscustomobject][ordered]@{
        anchorCommit = $AnchorCommit
        technicalCommit = $TechnicalCommit
        commitsAfterAnchor = $count
        topology = 'LINEAR_SINGLE_PARENT_FAST_FORWARD'
        approvedTechnicalCommitPreserved = $true
    }
}

function New-GeoCeDGCloseoutReadinessReceipt {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $head = Get-GeoCeDGCloseoutExactRef $root 'HEAD' 'Current HEAD'
    Assert-GeoCeDGCloseoutWorkflow ($head -ceq $technical) `
        'CLOSEOUT_READINESS requires HEAD to be the exact technical commit T.'
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $root
    Assert-GeoCeDGCloseoutWorkflow ($status.Staged.Count -eq 0 -and
        $status.Unstaged.Count -eq 0 -and $status.Untracked.Count -eq 0) `
        'CLOSEOUT_READINESS requires a clean worktree and index; dirty/precommit cohorts are diagnostic only.'
    $materialization = Assert-GeoCeDGWorktreeMaterialization `
        -RepositoryRoot $root -ExpectedCommit $technical
    $policyContext = Read-GeoCeDGCloseoutWorkflowPolicy $root $technical $PolicyPath
    $validatedModes = @('VERIFIED', 'AUTHOR_OPERATED')
    Assert-GeoCeDGPhaseLifecycleSet `
        @($policyContext.policy.closeout.supportedModes | ForEach-Object { [string]$_ }) `
        $validatedModes 'CLOSEOUT_READINESS closeout routes'
    $plan = Get-GeoCeDGCloseoutPlan $root $technical $policyContext
    $recordCreation = Get-GeoCeDGCloseoutRecordCreationAuthority $root `
        $plan.recordPath -RequireAbsent

    $branch = [string]$policyContext.policy.promotion.branch
    $remote = [string]$policyContext.policy.promotion.remote
    [void](Assert-GeoCeDGCloseoutBoundedPushConfiguration $root $remote)
    $remoteIdentity = Get-GeoCeDGCloseoutRemoteIdentity $root $remote
    $localMain = Get-GeoCeDGCloseoutExactRef $root "refs/heads/$branch" `
        'Local promotion branch'
    $tracking = Get-GeoCeDGCloseoutExactRef $root "refs/remotes/$remote/$branch" `
        'Remote-tracking promotion branch'
    $live = Get-GeoCeDGCloseoutLiveBranchRef $root $remote $branch
    Assert-GeoCeDGCloseoutWorkflow ($localMain -ceq $tracking -and $tracking -ceq $live) `
        'Local main, origin/main, and live remote main must share one readiness anchor.'
    $technicalHistory = Assert-GeoCeDGCloseoutLinearTechnicalHistory $root `
        $localMain $technical
    $tagAbsence = Test-GeoCeDGCloseoutTagAbsent $root $remote `
        ([string]$policyContext.policy.promotion.tagName)
    Assert-GeoCeDGCloseoutWorkflow ($tagAbsence.local -and $tagAbsence.remote) `
        'Expected phase tag already exists locally or on the live remote.'

    $technicalCampaignPlan = Get-GeoCeDGCloseoutAcceptanceExecutionPlan `
        -RepositoryRoot $root -TechnicalCommit $technical -PolicyContext $policyContext
    $technicalCampaignPlanSha256 = Get-GeoCeDGCloseoutRawSha256 `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $technicalCampaignPlan)
    $acceptancePlan = [pscustomobject][ordered]@{
        schemaVersion = 1
        reviewedTechnicalCommit = $technical
        policyPath = [string]$policyContext.policyPath
        policyBlobSha256 = [string]$policyContext.policyBlobSha256
        requiredTechnicalLevels = @('PHASE', 'COMPOSED', 'FULL')
        technicalCampaignPlanSha256 = $technicalCampaignPlanSha256
        validatedCloseoutModes = $validatedModes
        contentPlanSha256 = [string]$plan.contentPlanSha256
        promotionBranch = $branch
        promotionRemote = $remote
        promotionAnchor = $localMain
        remoteAuthoritySha256 = [string]$remoteIdentity.authoritySha256
        recordCreationAuthoritySha256 = Get-GeoCeDGCloseoutRawSha256 `
            (ConvertTo-GeoCeDGCloseoutJsonBytes $recordCreation)
    }
    $acceptancePlanSha256 = Get-GeoCeDGCloseoutRawSha256 `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $acceptancePlan)

    $receipt = [pscustomobject][ordered]@{
        schemaVersion = [long]1
        kind = 'GEOCEDG_CLOSEOUT_READINESS'
        state = 'CLOSEOUT_READINESS_PASSED'
        phase = [string]$policyContext.policy.phase
        closeoutMode = $null
        validatedCloseoutModes = $validatedModes
        acceptancePlanSha256 = $acceptancePlanSha256
        reviewedTechnicalCommit = $technical
        policyPath = [string]$policyContext.policyPath
        policyBlobSha256 = [string]$policyContext.policyBlobSha256
        requiredTechnicalLevels = @('PHASE', 'COMPOSED', 'FULL')
        technicalCampaignPlan = $technicalCampaignPlan
        technicalCampaignPlanSha256 = $technicalCampaignPlanSha256
        technicalCampaignExecuted = $false
        acceptanceEvidenceConsumable = $false
        expectedTag = [ordered]@{
            name = [string]$policyContext.policy.promotion.tagName
            message = [string]$policyContext.policy.promotion.tagMessage
        }
        promotion = [ordered]@{
            branch = $branch
            remote = $remote
            anchorCommit = $localMain
            technicalCommit = $technical
            commitsAfterAnchor = [long]$technicalHistory.commitsAfterAnchor
            historyTopology = [string]$technicalHistory.topology
        }
        repository = [ordered]@{
            head = $head
            worktreeClean = $true
            indexClean = $true
            localPromotionBranchCommit = $localMain
            remoteTrackingCommit = $tracking
            liveRemoteCommit = $live
            expectedTagAbsentLocal = [bool]$tagAbsence.local
            expectedTagAbsentRemote = [bool]$tagAbsence.remote
            materializationTrackedSha256 = [string]$materialization.trackedSha256
            remoteIdentity = $remoteIdentity
        }
        route = [ordered]@{
            prepareSupported = $true
            auditSupported = $true
            validatedCloseoutModes = $validatedModes
            gitFinalizationAuthorities = [ordered]@{
                VERIFIED = 'VERIFIED_WORKFLOW'
                AUTHOR_OPERATED = 'AUTHOR'
            }
            recordPath = [string]$plan.recordPath
            statusPaths = @($plan.statusPaths)
            contentPlanSha256 = [string]$plan.contentPlanSha256
            recordCreation = [ordered]@{
                physicallyAbsentAtReadiness = $true
                notIgnored = [bool]$recordCreation.notIgnored
                expectedMode = [string]$recordCreation.expectedMode
                cachedAttributesSha256 = [string]$recordCreation.cachedAttributesSha256
                physicalAttributesSha256 = [string]$recordCreation.physicalAttributesSha256
                materializationConfigurationSha256 = `
                    [string]$recordCreation.materializationConfigurationSha256
                activeCleanFilter = [bool]$recordCreation.activeCleanFilter
                workingTreeEncoding = [bool]$recordCreation.workingTreeEncoding
                identExpansion = [bool]$recordCreation.identExpansion
                pathTraversalReparseFree = `
                    [bool]$recordCreation.pathTraversalReparseFree
                regularBlobCreationReproducible = `
                    [bool]$recordCreation.regularBlobCreationReproducible
            }
            lifecycleRepairRequired = $false
        }
        selfApproved = $false
    }
    return ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $receipt) `
        'fresh closeout-readiness receipt'
}

function Assert-GeoCeDGCloseoutReadinessReceiptShape {
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [string]$PolicyBlobSha256
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt @('schemaVersion', 'kind',
        'state', 'phase', 'closeoutMode', 'validatedCloseoutModes',
        'acceptancePlanSha256', 'reviewedTechnicalCommit', 'policyPath',
        'policyBlobSha256', 'requiredTechnicalLevels', 'technicalCampaignPlan',
        'technicalCampaignPlanSha256', 'technicalCampaignExecuted',
        'acceptanceEvidenceConsumable', 'expectedTag', 'promotion', 'repository',
        'route', 'selfApproved') 'Closeout-readiness receipt'
    Assert-GeoCeDGCloseoutWorkflow ($Receipt.schemaVersion -is [long] -and
        $Receipt.schemaVersion -eq 1 -and
        [string]$Receipt.kind -ceq 'GEOCEDG_CLOSEOUT_READINESS' -and
        [string]$Receipt.state -ceq 'CLOSEOUT_READINESS_PASSED' -and
        $null -eq $Receipt.closeoutMode -and
        [string]$Receipt.acceptancePlanSha256 -cmatch '^[0-9a-f]{64}$' -and
        [string]$Receipt.reviewedTechnicalCommit -ceq $TechnicalCommit -and
        [string]$Receipt.policyPath -ceq $PolicyPath -and
        [string]$Receipt.policyBlobSha256 -ceq $PolicyBlobSha256 -and
        [string]$Receipt.technicalCampaignPlanSha256 -cmatch '^[0-9a-f]{64}$') `
        'Closeout-readiness receipt binding is invalid.'
    Assert-GeoCeDGPhaseLifecycleSet @($Receipt.validatedCloseoutModes | ForEach-Object {
            [string]$_
        }) @('VERIFIED', 'AUTHOR_OPERATED') 'Readiness validated closeout modes'
    Assert-GeoCeDGCloseoutWorkflow ((@($Receipt.requiredTechnicalLevels) -join "`n") -ceq
        (@('PHASE', 'COMPOSED', 'FULL') -join "`n")) `
        'Closeout-readiness receipt reduced required technical coverage.'
    Assert-GeoCeDGCloseoutWorkflow ($Receipt.technicalCampaignExecuted -is [bool] -and
        -not $Receipt.technicalCampaignExecuted -and
        $Receipt.acceptanceEvidenceConsumable -is [bool] -and
        -not $Receipt.acceptanceEvidenceConsumable -and
        $Receipt.selfApproved -is [bool] -and -not $Receipt.selfApproved) `
        'Readiness cannot claim technical execution, consumable evidence, or self-approval.'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.expectedTag @('name', 'message') `
        'Readiness expected tag'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.promotion @('branch', 'remote',
        'anchorCommit', 'technicalCommit', 'commitsAfterAnchor', 'historyTopology') `
        'Readiness promotion anchor'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.repository @('head',
        'worktreeClean', 'indexClean', 'localPromotionBranchCommit',
        'remoteTrackingCommit', 'liveRemoteCommit', 'expectedTagAbsentLocal',
        'expectedTagAbsentRemote', 'materializationTrackedSha256', 'remoteIdentity') `
        'Readiness repository identity'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.route @('prepareSupported',
        'auditSupported', 'validatedCloseoutModes', 'gitFinalizationAuthorities',
        'recordPath', 'statusPaths', 'contentPlanSha256', 'recordCreation',
        'lifecycleRepairRequired') 'Readiness closeout route'
    Assert-GeoCeDGPhaseLifecycleSet @($Receipt.route.validatedCloseoutModes | ForEach-Object {
            [string]$_
        }) @('VERIFIED', 'AUTHOR_OPERATED') 'Readiness route modes'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.route.gitFinalizationAuthorities `
        @('VERIFIED', 'AUTHOR_OPERATED') 'Readiness Git-finalization authorities'
    Assert-GeoCeDGPhaseLifecycleProperties $Receipt.route.recordCreation @(
        'physicallyAbsentAtReadiness', 'notIgnored', 'expectedMode',
        'cachedAttributesSha256', 'physicalAttributesSha256',
        'materializationConfigurationSha256', 'activeCleanFilter',
        'workingTreeEncoding', 'identExpansion', 'pathTraversalReparseFree',
        'regularBlobCreationReproducible') 'Readiness record-creation authority'
    Assert-GeoCeDGCloseoutRemoteIdentityShape $Receipt.repository.remoteIdentity `
        ([string]$Receipt.promotion.remote)
    Assert-GeoCeDGCloseoutWorkflow ([string]$Receipt.promotion.anchorCommit -cmatch
        '^[0-9a-f]{40}$' -and [string]$Receipt.promotion.technicalCommit -ceq
        $TechnicalCommit -and $Receipt.promotion.commitsAfterAnchor -is [long] -and
        [long]$Receipt.promotion.commitsAfterAnchor -ge 0 -and
        [string]$Receipt.promotion.historyTopology -ceq
        'LINEAR_SINGLE_PARENT_FAST_FORWARD' -and
        [string]$Receipt.route.contentPlanSha256 -cmatch
        '^[0-9a-f]{64}$' -and $Receipt.route.prepareSupported -eq $true -and
        $Receipt.route.auditSupported -eq $true -and
        [string]$Receipt.route.gitFinalizationAuthorities.VERIFIED -ceq
        'VERIFIED_WORKFLOW' -and
        [string]$Receipt.route.gitFinalizationAuthorities.AUTHOR_OPERATED -ceq
        'AUTHOR' -and
        $Receipt.route.recordCreation.physicallyAbsentAtReadiness -is [bool] -and
        $Receipt.route.recordCreation.physicallyAbsentAtReadiness -and
        $Receipt.route.recordCreation.notIgnored -is [bool] -and
        $Receipt.route.recordCreation.notIgnored -and
        [string]$Receipt.route.recordCreation.expectedMode -ceq '100644' -and
        [string]$Receipt.route.recordCreation.cachedAttributesSha256 -cmatch
        '^[0-9a-f]{64}$' -and
        [string]$Receipt.route.recordCreation.physicalAttributesSha256 -cmatch
        '^[0-9a-f]{64}$' -and
        [string]$Receipt.route.recordCreation.materializationConfigurationSha256 -cmatch
        '^[0-9a-f]{64}$' -and
        $Receipt.route.recordCreation.activeCleanFilter -is [bool] -and
        -not $Receipt.route.recordCreation.activeCleanFilter -and
        $Receipt.route.recordCreation.workingTreeEncoding -is [bool] -and
        -not $Receipt.route.recordCreation.workingTreeEncoding -and
        $Receipt.route.recordCreation.identExpansion -is [bool] -and
        -not $Receipt.route.recordCreation.identExpansion -and
        $Receipt.route.recordCreation.pathTraversalReparseFree -is [bool] -and
        $Receipt.route.recordCreation.pathTraversalReparseFree -and
        $Receipt.route.recordCreation.regularBlobCreationReproducible -is [bool] -and
        $Receipt.route.recordCreation.regularBlobCreationReproducible -and
        $Receipt.route.lifecycleRepairRequired -eq $false) `
        'Readiness route or promotion anchor is invalid.'
}

function Read-GeoCeDGCloseoutReadinessReceipt {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [string]$ReadinessReceiptPath
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $policyContext = Read-GeoCeDGCloseoutWorkflowPolicy $root $technical $PolicyPath
    $path = [IO.Path]::GetFullPath($ReadinessReceiptPath)
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $path -PathType Leaf) `
        "Closeout-readiness receipt is missing: $path"
    $bytes = [IO.File]::ReadAllBytes($path)
    $receipt = ConvertFrom-GeoCeDGPhaseLifecycleJson $bytes 'closeout-readiness receipt'
    Assert-GeoCeDGCloseoutReadinessReceiptShape $receipt $technical `
        $policyContext.policyPath $policyContext.policyBlobSha256
    $plan = Get-GeoCeDGCloseoutPlan $root $technical $policyContext
    $validatedModes = @('VERIFIED', 'AUTHOR_OPERATED')
    $remote = [string]$policyContext.policy.promotion.remote
    $currentRemoteIdentity = Get-GeoCeDGCloseoutRemoteIdentity $root $remote
    $currentRemoteJson = $currentRemoteIdentity | ConvertTo-Json -Depth 20 -Compress
    $receiptRemoteJson = $receipt.repository.remoteIdentity |
        ConvertTo-Json -Depth 20 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($currentRemoteJson -ceq $receiptRemoteJson) `
        'Promotion remote URL/identity changed after CLOSEOUT_READINESS.'
    $technicalHistory = Assert-GeoCeDGCloseoutLinearTechnicalHistory $root `
        ([string]$receipt.promotion.anchorCommit) $technical
    $recordCreation = Get-GeoCeDGCloseoutRecordCreationAuthority $root $plan.recordPath
    $recordCreationHash = Get-GeoCeDGCloseoutRawSha256 `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $recordCreation)
    $technicalCampaignPlan = Get-GeoCeDGCloseoutAcceptanceExecutionPlan `
        -RepositoryRoot $root -TechnicalCommit $technical -PolicyContext $policyContext
    $technicalCampaignPlanSha256 = Get-GeoCeDGCloseoutRawSha256 `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $technicalCampaignPlan)
    $acceptancePlan = [pscustomobject][ordered]@{
        schemaVersion = 1
        reviewedTechnicalCommit = $technical
        policyPath = [string]$policyContext.policyPath
        policyBlobSha256 = [string]$policyContext.policyBlobSha256
        requiredTechnicalLevels = @('PHASE', 'COMPOSED', 'FULL')
        technicalCampaignPlanSha256 = $technicalCampaignPlanSha256
        validatedCloseoutModes = $validatedModes
        contentPlanSha256 = [string]$plan.contentPlanSha256
        promotionBranch = [string]$policyContext.policy.promotion.branch
        promotionRemote = $remote
        promotionAnchor = [string]$receipt.promotion.anchorCommit
        remoteAuthoritySha256 = [string]$currentRemoteIdentity.authoritySha256
        recordCreationAuthoritySha256 = $recordCreationHash
    }
    $acceptancePlanSha256 = Get-GeoCeDGCloseoutRawSha256 `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $acceptancePlan)
    Assert-GeoCeDGCloseoutWorkflow ([string]$receipt.phase -ceq
        [string]$policyContext.policy.phase -and
        [string]$receipt.expectedTag.name -ceq
            [string]$policyContext.policy.promotion.tagName -and
        [string]$receipt.expectedTag.message -ceq
            [string]$policyContext.policy.promotion.tagMessage -and
        [string]$receipt.promotion.branch -ceq
            [string]$policyContext.policy.promotion.branch -and
        [string]$receipt.promotion.remote -ceq
            [string]$policyContext.policy.promotion.remote -and
        [string]$receipt.promotion.technicalCommit -ceq $technical -and
        [long]$receipt.promotion.commitsAfterAnchor -eq
            [long]$technicalHistory.commitsAfterAnchor -and
        [string]$receipt.promotion.historyTopology -ceq
            [string]$technicalHistory.topology -and
        [string]$receipt.repository.head -ceq $technical -and
        [string]$receipt.repository.localPromotionBranchCommit -ceq
            [string]$receipt.promotion.anchorCommit -and
        [string]$receipt.repository.remoteTrackingCommit -ceq
            [string]$receipt.promotion.anchorCommit -and
        [string]$receipt.repository.liveRemoteCommit -ceq
            [string]$receipt.promotion.anchorCommit -and
        $receipt.repository.worktreeClean -eq $true -and
        $receipt.repository.indexClean -eq $true -and
        $receipt.repository.expectedTagAbsentLocal -eq $true -and
        $receipt.repository.expectedTagAbsentRemote -eq $true -and
        [string]$receipt.route.recordPath -ceq [string]$plan.recordPath -and
        (@($receipt.route.statusPaths) -join "`n") -ceq
            (@($plan.statusPaths) -join "`n") -and
        [string]$receipt.route.contentPlanSha256 -ceq
            [string]$plan.contentPlanSha256 -and
        [string]$receipt.route.recordCreation.cachedAttributesSha256 -ceq
            [string]$recordCreation.cachedAttributesSha256 -and
        [string]$receipt.route.recordCreation.physicalAttributesSha256 -ceq
            [string]$recordCreation.physicalAttributesSha256 -and
        [string]$receipt.route.recordCreation.materializationConfigurationSha256 -ceq
            [string]$recordCreation.materializationConfigurationSha256 -and
        [string]$receipt.technicalCampaignPlanSha256 -ceq
            $technicalCampaignPlanSha256 -and
        [string]$receipt.acceptancePlanSha256 -ceq $acceptancePlanSha256) `
        'Closeout-readiness receipt does not match its frozen T policy and route.'
    $receiptCampaignPlanJson = $receipt.technicalCampaignPlan |
        ConvertTo-Json -Depth 100 -Compress
    $expectedCampaignPlanJson = $technicalCampaignPlan |
        ConvertTo-Json -Depth 100 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($receiptCampaignPlanJson -ceq
        $expectedCampaignPlanJson) `
        'Closeout-readiness technical campaign plan differs from the exact T policy.'
    $tracked = Get-GeoCeDGRepositoryTrackedIdentity $root $technical
    Assert-GeoCeDGCloseoutWorkflow ([string]$receipt.repository.materializationTrackedSha256 -ceq
        [string]$tracked.sha256) 'Readiness tracked-tree identity does not match T.'
    return [pscustomobject][ordered]@{
        receipt = $receipt
        path = $path
        sha256 = Get-GeoCeDGCloseoutRawSha256 $bytes
        policyContext = $policyContext
    }
}

function Test-GeoCeDGCloseoutAcceptancePreflight {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [string]$ReadinessReceiptPath
    )
    $fresh = New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $RepositoryRoot `
        -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath
    if ([string]::IsNullOrWhiteSpace($ReadinessReceiptPath)) { return $fresh }
    $stored = Read-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $RepositoryRoot `
        -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath
    $freshJson = $fresh | ConvertTo-Json -Depth 100 -Compress
    $storedJson = $stored.receipt | ConvertTo-Json -Depth 100 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($freshJson -ceq $storedJson) `
        'CLOSEOUT_READINESS receipt no longer matches the exact live pre-promotion state.'
    return $fresh
}

function Assert-GeoCeDGCloseoutRequiredProperties {
    param(
        [Parameter(Mandatory)] [object]$Object,
        [Parameter(Mandatory)] [string[]]$Names,
        [Parameter(Mandatory)] [string]$Description
    )
    $actual = @($Object.PSObject.Properties.Name)
    foreach ($name in $Names) {
        Assert-GeoCeDGCloseoutWorkflow ($actual -ccontains $name) `
            "$Description is missing required property '$name'."
    }
}

function Read-GeoCeDGExplicitAuthorApproval {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Phase,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode
    )
    $CloseoutMode = ConvertTo-GeoCeDGCloseoutMode $CloseoutMode
    $fullPath = [IO.Path]::GetFullPath($Path)
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $fullPath -PathType Leaf) `
        "Explicit author approval is missing: $fullPath"
    $bytes = [IO.File]::ReadAllBytes($fullPath)
    $approval = ConvertFrom-GeoCeDGPhaseLifecycleJson $bytes 'explicit author approval'
    Assert-GeoCeDGPhaseLifecycleProperties $approval @('schemaVersion', 'kind',
        'phase', 'reviewedTechnicalCommit', 'closeoutMode', 'decision', 'authority',
        'selfApproved') 'Explicit author approval'
    Assert-GeoCeDGCloseoutWorkflow ($approval.schemaVersion -is [long] -and
        $approval.schemaVersion -eq 1 -and
        [string]$approval.kind -ceq 'GEOCEDG_EXPLICIT_AUTHOR_APPROVAL' -and
        [string]$approval.phase -ceq $Phase -and
        [string]$approval.reviewedTechnicalCommit -ceq $TechnicalCommit -and
        [string]$approval.closeoutMode -ceq $CloseoutMode -and
        [string]$approval.decision -ceq 'PASS_AUTHOR_APPROVED' -and
        [string]$approval.authority -ceq 'AUTHOR' -and
        $approval.selfApproved -is [bool] -and -not $approval.selfApproved) `
        'Explicit author approval does not authorize the exact T/mode pair.'
    return [pscustomobject][ordered]@{
        approval = $approval
        path = $fullPath
        sha256 = Get-GeoCeDGCloseoutRawSha256 $bytes
    }
}

function Resolve-GeoCeDGCloseoutEvidenceChildPath {
    param(
        [Parameter(Mandatory)] [string]$EvidenceResultPath,
        [Parameter(Mandatory)] [string]$RecordedPath
    )
    if ([IO.Path]::IsPathRooted($RecordedPath)) {
        return [IO.Path]::GetFullPath($RecordedPath)
    }
    return [IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $EvidenceResultPath) `
        $RecordedPath))
}

function Assert-GeoCeDGCloseoutArchivedFile {
    param(
        [Parameter(Mandatory)] [object]$Reference,
        [Parameter(Mandatory)] [string]$Description
    )
    Assert-GeoCeDGCloseoutRequiredProperties $Reference @('sha256') $Description
    $pathProperty = if ('archivePath' -cin @($Reference.PSObject.Properties.Name)) {
        'archivePath'
    } elseif ('path' -cin @($Reference.PSObject.Properties.Name)) { 'path' } else { $null }
    Assert-GeoCeDGCloseoutWorkflow ($null -ne $pathProperty) `
        "$Description has no archived path."
    $path = [IO.Path]::GetFullPath([string]$Reference.$pathProperty)
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $path -PathType Leaf) `
        "$Description is missing: $path"
    $hash = Get-GeoCeDGCloseoutRawSha256 ([IO.File]::ReadAllBytes($path))
    Assert-GeoCeDGCloseoutWorkflow ([string]$Reference.sha256 -cmatch '^[0-9a-f]{64}$' -and
        $hash -ceq [string]$Reference.sha256) "$Description hash mismatch: $path"
}

function Get-GeoCeDGCloseoutPlanSha256 {
    param([Parameter(Mandatory)] [object]$Plan)
    return Get-GeoCeDGCloseoutRawSha256 (ConvertTo-GeoCeDGCloseoutJsonBytes $Plan)
}

function Compare-GeoCeDGCloseoutPreHeavyExecutionPlan {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [object]$ReadinessReceipt,
        [Parameter(Mandatory)] [string]$RequestedLevel,
        [Parameter(Mandatory)] [bool]$CleanGeneratedOutputs,
        [Parameter(Mandatory)] [bool]$IndependentBuilds
    )
    Assert-GeoCeDGCloseoutReadinessReceiptShape $ReadinessReceipt `
        ([string]$ReadinessReceipt.reviewedTechnicalCommit) `
        ([string]$ReadinessReceipt.policyPath) `
        ([string]$ReadinessReceipt.policyBlobSha256)
    $level = $RequestedLevel.ToUpperInvariant()
    Assert-GeoCeDGCloseoutWorkflow ($level -ceq 'FULL' -and
        $CleanGeneratedOutputs -and -not $IndependentBuilds) `
        'Final acceptance selects exactly one clean, non-independent FULL physical campaign.'
    $expected = $ReadinessReceipt.technicalCampaignPlan
    $expectedHash = Get-GeoCeDGCloseoutPlanSha256 $expected
    Assert-GeoCeDGCloseoutWorkflow ($expectedHash -ceq
        [string]$ReadinessReceipt.technicalCampaignPlanSha256) `
        'Readiness technical campaign plan hash is invalid.'
    $runtimePlan = Get-GeoCeDGVerificationExecutionPlan -Level FULL `
        -Phase ([string]$ReadinessReceipt.phase)
    $runtimePlanJson = $runtimePlan | ConvertTo-Json -Depth 100 -Compress
    $sealedRuntimePlanJson = $expected.runtimeExecutionPlans.FULL |
        ConvertTo-Json -Depth 100 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($runtimePlanJson -ceq
        $sealedRuntimePlanJson -and (@($runtimePlan.commonGateIds) -join "`n") -ceq
        (@($expected.commonGatePlan) -join "`n")) `
        'Current executable FULL plan differs from the readiness-sealed runtime plan.'

    # Selection is rebuilt from the actual CLI choice. It is intentionally not a
    # relabel of three requested runs: FULL is the only physical campaign and the
    # other levels remain coverage obligations in the selected plan.
    $selected = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $expected) `
        'selected pre-heavy technical campaign plan'
    $selected.physicalCampaign.level = $level
    $selected.physicalCampaign.count = [long]1
    $selected.physicalCampaign.cleanGeneratedOutputs = $CleanGeneratedOutputs
    $selected.physicalCampaign.independentBuilds = $IndependentBuilds
    $selected.physicalCampaign.canonicalSelection = `
        $runtimePlan.canonicalBuild.selection
    $selectedHash = Get-GeoCeDGCloseoutPlanSha256 $selected
    Assert-GeoCeDGCloseoutWorkflow ($selectedHash -ceq $expectedHash) `
        'Selected pre-heavy technical campaign plan differs from CLOSEOUT_READINESS.'
    return [pscustomobject][ordered]@{
        expectedPlan = $expected
        selectedPlan = $selected
        expectedPlanSha256 = $expectedHash
        selectedPlanSha256 = $selectedHash
        comparedBeforeHeavy = $true
        expectedEqualsSelected = $true
    }
}

function ConvertTo-GeoCeDGCloseoutCampaignRelativePath {
    param(
        [Parameter(Mandatory)] [string]$CampaignRoot,
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [string]$Description
    )
    $root = [IO.Path]::GetFullPath($CampaignRoot).TrimEnd('/', '\')
    $full = [IO.Path]::GetFullPath($Path)
    $comparison = if ($IsWindows) {
        [StringComparison]::OrdinalIgnoreCase
    } else { [StringComparison]::Ordinal }
    $prefix = $root + [IO.Path]::DirectorySeparatorChar
    Assert-GeoCeDGCloseoutWorkflow ($full.StartsWith($prefix, $comparison)) `
        "$Description is outside the single FULL campaign root: $full"
    return ConvertTo-GeoCeDGPhaseLifecyclePath `
        ([IO.Path]::GetRelativePath($root, $full).Replace('\', '/')) $Description
}

function Resolve-GeoCeDGCloseoutCampaignArtifactPath {
    param(
        [Parameter(Mandatory)] [string]$CampaignRoot,
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$Description
    )
    Assert-GeoCeDGCloseoutWorkflow (-not [IO.Path]::IsPathRooted($RelativePath)) `
        "$Description must be campaign-relative."
    $safe = ConvertTo-GeoCeDGPhaseLifecyclePath $RelativePath $Description
    $root = [IO.Path]::GetFullPath($CampaignRoot).TrimEnd('/', '\')
    $full = [IO.Path]::GetFullPath((Join-Path $root $safe))
    $comparison = if ($IsWindows) {
        [StringComparison]::OrdinalIgnoreCase
    } else { [StringComparison]::Ordinal }
    Assert-GeoCeDGCloseoutWorkflow ($full.StartsWith(
            $root + [IO.Path]::DirectorySeparatorChar, $comparison)) `
        "$Description escapes the single FULL campaign root."
    return $full
}

function Get-GeoCeDGCloseoutStructuredSha256 {
    param([Parameter(Mandatory)] [object]$Value)
    return Get-GeoCeDGCloseoutTextSha256 `
        ($Value | ConvertTo-Json -Depth 100 -Compress)
}

function New-GeoCeDGCloseoutSingleFullCampaignEnvelope {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [string]$ReadinessReceiptPath,
        [Parameter(Mandatory)] [string]$CampaignRoot,
        [Parameter(Mandatory)] [string]$CanonicalReceiptPath,
        [Parameter(Mandatory)] [object]$PreHeavyPlanComparison,
        [Parameter(Mandatory)] [object]$PhaseExecution,
        [Parameter(Mandatory)] [object]$BaselineExecution,
        [Parameter(Mandatory)] [object]$CommonGateCompletion
    )
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $readiness = Read-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $root `
        -TechnicalCommit $technical -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath
    $policy = $readiness.policyContext.policy
    $plan = $readiness.receipt.technicalCampaignPlan
    Assert-GeoCeDGCloseoutWorkflow ($PreHeavyPlanComparison.comparedBeforeHeavy -eq
        $true -and $PreHeavyPlanComparison.expectedEqualsSelected -eq $true -and
        [string]$PreHeavyPlanComparison.expectedPlanSha256 -ceq
            [string]$readiness.receipt.technicalCampaignPlanSha256 -and
        [string]$PreHeavyPlanComparison.selectedPlanSha256 -ceq
            [string]$readiness.receipt.technicalCampaignPlanSha256) `
        'Single FULL campaign lacks the exact pre-heavy plan comparison.'

    $campaignDirectory = [IO.Path]::GetFullPath($CampaignRoot)
    $receiptPath = [IO.Path]::GetFullPath($CanonicalReceiptPath)
    $receiptRelative = ConvertTo-GeoCeDGCloseoutCampaignRelativePath $campaignDirectory `
        $receiptPath 'FULL canonical receipt'
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $receiptPath -PathType Leaf) `
        'Single FULL campaign canonical receipt is missing.'
    $receiptBytes = [IO.File]::ReadAllBytes($receiptPath)
    $receipt = ConvertFrom-GeoCeDGPhaseLifecycleJson $receiptBytes `
        'single FULL canonical receipt'
    $commitIndex = Get-GeoCeDGPhaseCommitIndexAuthority $root $technical
    $emptyHash = Get-GeoCeDGCloseoutRawSha256 ([byte[]]::new(0))
    $canonicalProof = Assert-GeoCeDGArchivedCanonicalBuildReceipt `
        -Receipt $receipt -Level FULL -RepositoryRoot $root `
        -TechnicalCommit $technical -ExpectedIndexSha256 $commitIndex.IndexSha256 `
        -ExpectedStatusSha256 $emptyHash
    $fullSelection = Get-GeoCeDGCanonicalSelectionPlan -Level FULL
    $composedSelection = Get-GeoCeDGCanonicalSelectionPlan -Level COMPOSED
    foreach ($module in @('shared', 'desktop')) {
        $actual = $receipt.selections.$module
        $expectedFull = $fullSelection.$module
        Assert-GeoCeDGCloseoutWorkflow ([string]$actual.task -ceq
            [string]$expectedFull.task -and $actual.unfiltered -is [bool] -and
            $actual.unfiltered -and @($actual.filters).Count -eq 0) `
            "Single campaign FULL $module selection is not exact and unfiltered."
        $expectedComposed = $composedSelection.$module
        Assert-GeoCeDGCloseoutWorkflow (-not [bool]$expectedComposed.unfiltered -and
            @($expectedComposed.filters).Count -gt 0) `
            "Normative COMPOSED $module selection cannot be proven as a subset of FULL."
        $testTask = [string]$expectedFull.task
        $testRuns = @($receipt.nativeRuns | Where-Object {
                @($_.arguments | ForEach-Object { [string]$_ }) -ccontains $testTask
            })
        Assert-GeoCeDGCloseoutWorkflow ($testRuns.Count -eq 1 -and
            @($testRuns[0].arguments | ForEach-Object { [string]$_ }) -ccontains
                '--no-build-cache' -and
            @($testRuns[0].arguments | ForEach-Object { [string]$_ }) -ccontains
                '--rerun-tasks' -and
            -not (@($testRuns[0].arguments | ForEach-Object { [string]$_ }) `
                -ccontains '--build-cache')) `
            "Acceptance FULL $module was not the declared clean rebuild execution."
    }

    Assert-GeoCeDGPhaseLifecycleProperties $PhaseExecution @('startedUtc',
        'finishedUtc', 'exitCode', 'program', 'arguments', 'beforeHead',
        'afterHead', 'beforeStatusSha256', 'afterStatusSha256') `
        'Nested PHASE execution envelope'
    Assert-GeoCeDGCloseoutWorkflow ([int]$PhaseExecution.exitCode -eq 0 -and
        [string]$PhaseExecution.beforeHead -ceq $technical -and
        [string]$PhaseExecution.afterHead -ceq $technical -and
        [string]$PhaseExecution.beforeStatusSha256 -ceq $emptyHash -and
        [string]$PhaseExecution.afterStatusSha256 -ceq $emptyHash) `
        'Nested PHASE execution did not remain in the exact clean-T cohort.'
    $phaseDeclaration = @($policy.technicalEvidence.requiredClaims)[0]
    Assert-GeoCeDGCloseoutWorkflow ([string]$PhaseExecution.program -ceq
        [string]$phaseDeclaration.orchestratorPath) `
        'Nested PHASE was not executed through its declared orchestrator.'
    $expectedInvocationJson = $plan.coverageComparison.phaseIntegration.rootInvocation |
        ConvertTo-Json -Depth 20 -Compress
    $actualInvocationJson = @($PhaseExecution.arguments) |
        ConvertTo-Json -Depth 20 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($actualInvocationJson -ceq
        $expectedInvocationJson) `
        'Nested PHASE orchestrator argv differs from the central FULL integration map.'
    $phaseStartedText = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
        $PhaseExecution.startedUtc 'Nested PHASE start'
    $phaseFinishedText = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
        $PhaseExecution.finishedUtc 'Nested PHASE finish'
    $phaseStarted = [datetimeoffset]::ParseExact($phaseStartedText, 'o',
        [Globalization.CultureInfo]::InvariantCulture)
    $phaseFinished = [datetimeoffset]::ParseExact($phaseFinishedText, 'o',
        [Globalization.CultureInfo]::InvariantCulture)
    Assert-GeoCeDGCloseoutWorkflow ($phaseFinished -ge $phaseStarted) `
        'Nested PHASE execution window is malformed.'
    $phaseExecutionBasis = [ordered]@{
        startedUtc = $phaseStartedText
        finishedUtc = $phaseFinishedText
        exitCode = [long]$PhaseExecution.exitCode
        program = [string]$PhaseExecution.program
        # The central plan records named PowerShell bindings, not a lossy argv
        # display string. The equality check above authenticates the supplied
        # execution; rebuild its durable projection from that frozen plan.
        arguments = [object[]]@($plan.coverageComparison.phaseIntegration.
            rootInvocation | ForEach-Object {
                [ordered]@{ name = [string]$_.name; value = [string]$_.value }
            })
        beforeHead = [string]$PhaseExecution.beforeHead
        afterHead = [string]$PhaseExecution.afterHead
        beforeStatusSha256 = [string]$PhaseExecution.beforeStatusSha256
        afterStatusSha256 = [string]$PhaseExecution.afterStatusSha256
    }
    $phaseSummaryPath = Resolve-GeoCeDGCloseoutCampaignArtifactPath $campaignDirectory `
        ([string]$phaseDeclaration.evidencePath) 'nested PHASE summary'
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $phaseSummaryPath -PathType Leaf) `
        'Declared nested PHASE summary is missing from the FULL campaign.'
    $phaseSummaryBytes = [IO.File]::ReadAllBytes($phaseSummaryPath)
    $phaseSummary = ConvertFrom-GeoCeDGPhaseLifecycleJson $phaseSummaryBytes `
        'nested PHASE summary'
    Assert-GeoCeDGCloseoutRequiredProperties $phaseSummary @('schemaVersion', 'state',
        'fixtures', 'productRuntimeExecuted', 'authorApproved', 'failure') `
        'Nested PHASE summary'
    Assert-GeoCeDGCloseoutWorkflow ($phaseSummary.schemaVersion -is [long] -and
        $phaseSummary.schemaVersion -eq 1 -and [string]$phaseSummary.state -ceq
        [string]$phaseDeclaration.expectedState -and
        $phaseSummary.productRuntimeExecuted -is [bool] -and
        -not $phaseSummary.productRuntimeExecuted -and
        $phaseSummary.authorApproved -is [bool] -and
        -not $phaseSummary.authorApproved -and $null -eq $phaseSummary.failure -and
        @($phaseSummary.fixtures).Count -gt 0) `
        'Nested PHASE summary is not a successful non-product phase result.'
    $phaseArtifacts = [Collections.Generic.List[object]]::new()
    $artifactPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($fixture in @($phaseSummary.fixtures)) {
        Assert-GeoCeDGCloseoutRequiredProperties $fixture @('fixture', 'command',
            'arguments', 'exitCode', 'elapsedSeconds', 'logPath', 'evidenceKind') `
            'Nested PHASE fixture result'
        Assert-GeoCeDGCloseoutWorkflow ([int]$fixture.exitCode -eq 0 -and
            [string]$fixture.evidenceKind -ceq 'FAKE_FIRST_OPERATIONAL_CONTRACT') `
            'Nested PHASE fixture is not successful current-run operational evidence.'
        $relative = ConvertTo-GeoCeDGCloseoutCampaignRelativePath $campaignDirectory `
            ([string]$fixture.logPath) 'nested PHASE artifact'
        Assert-GeoCeDGCloseoutWorkflow ($artifactPaths.Add($relative)) `
            'Nested PHASE artifact path is duplicated.'
        $artifactPath = Resolve-GeoCeDGCloseoutCampaignArtifactPath $campaignDirectory `
            $relative 'nested PHASE artifact'
        Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $artifactPath -PathType Leaf) `
            "Nested PHASE artifact is missing: $relative"
        $phaseArtifacts.Add([ordered]@{
            role = 'PHASE_CHILD_ARTIFACT'
            fixture = [string]$fixture.fixture
            path = $relative
            sha256 = Get-GeoCeDGCloseoutRawSha256 ([IO.File]::ReadAllBytes($artifactPath))
            exitCode = [long]$fixture.exitCode
        })
    }

    Assert-GeoCeDGPhaseLifecycleProperties $BaselineExecution @('startedUtc',
        'finishedUtc', 'exitCode', 'fullTests', 'canonicalReceiptPath') `
        'Baseline FULL confirmation'
    Assert-GeoCeDGCloseoutWorkflow ([int]$BaselineExecution.exitCode -eq 0 -and
        $BaselineExecution.fullTests -is [bool] -and $BaselineExecution.fullTests -and
        [IO.Path]::GetFullPath([string]$BaselineExecution.canonicalReceiptPath).Equals(
            $receiptPath, [StringComparison]::OrdinalIgnoreCase)) `
        'Baseline did not confirm FULL using the same canonical receipt.'
    Assert-GeoCeDGPhaseLifecycleProperties $CommonGateCompletion @('state', 'exitCode',
        'completedUtc', 'expectedGateIds', 'actualGateIds',
        'expectedGatePlanSha256', 'executedGatePlanSha256') `
        'Root common-gate completion'
    Assert-GeoCeDGCloseoutWorkflow ([string]$CommonGateCompletion.state -ceq 'PASS' -and
        [int]$CommonGateCompletion.exitCode -eq 0 -and
        (@($CommonGateCompletion.expectedGateIds) -join "`n") -ceq
            (@($plan.commonGatePlan) -join "`n") -and
        (@($CommonGateCompletion.actualGateIds) -join "`n") -ceq
            (@($plan.commonGatePlan) -join "`n") -and
        [string]$CommonGateCompletion.expectedGatePlanSha256 -ceq
            [string]$plan.commonGatePlanSha256 -and
        [string]$CommonGateCompletion.executedGatePlanSha256 -ceq
            [string]$plan.commonGatePlanSha256) `
        'Root COMPOSED/FULL common-gate plan did not complete.'

    $receiptSha = Get-GeoCeDGCloseoutRawSha256 $receiptBytes
    $baselineBasis = [ordered]@{
        state = 'PASS'
        exitCode = [long]$BaselineExecution.exitCode
        fullTests = $true
        canonicalReceiptSha256 = $receiptSha
        startedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $BaselineExecution.startedUtc 'Baseline FULL start'
        finishedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $BaselineExecution.finishedUtc 'Baseline FULL finish'
    }
    $commonGateCompletionBasis = [ordered]@{
        state = 'PASS'
        exitCode = [long]$CommonGateCompletion.exitCode
        completedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $CommonGateCompletion.completedUtc 'Root common-gate completion'
        expectedGateIds = @($CommonGateCompletion.expectedGateIds)
        actualGateIds = @($CommonGateCompletion.actualGateIds)
        expectedGatePlanSha256 = [string]$CommonGateCompletion.expectedGatePlanSha256
        executedGatePlanSha256 = [string]$CommonGateCompletion.executedGatePlanSha256
    }
    $commonGatePlanSha256 = [string]$plan.commonGatePlanSha256
    $phaseClaimBasis = [ordered]@{
        level = 'PHASE'
        fulfillment = 'NESTED_EXECUTED'
        physicalRole = 'NESTED_WITHIN_ROOT_FULL'
        independentPhysicalCampaign = $false
        phase = [string]$policy.phase
        orchestratorAuthority = $plan.authorities.phaseOrchestrator
        verifierAuthority = $plan.authorities.phaseVerifier
        verifierMappingConfirmed = $true
        execution = $phaseExecutionBasis
        summary = [ordered]@{
            path = [string]$phaseDeclaration.evidencePath
            sha256 = Get-GeoCeDGCloseoutRawSha256 $phaseSummaryBytes
            state = [string]$phaseSummary.state
            exitCode = [long]$PhaseExecution.exitCode
        }
        artifacts = @($phaseArtifacts)
        cohort = [ordered]@{
            reviewedTechnicalCommit = $technical
            readinessReceiptSha256 = [string]$readiness.sha256
            inputFingerprint = [string]$receipt.inputFingerprint
        }
    }
    $phaseClaim = [ordered]@{}
    foreach ($entry in $phaseClaimBasis.GetEnumerator()) {
        $phaseClaim[$entry.Key] = $entry.Value
    }
    $phaseClaim.claimSha256 = Get-GeoCeDGCloseoutStructuredSha256 $phaseClaimBasis

    $composedClaimBasis = [ordered]@{
        level = 'COMPOSED'
        fulfillment = 'NORMATIVE_SUBSUMPTION'
        physicalRole = 'NO_SEPARATE_PHYSICAL_CAMPAIGN'
        independentPhysicalCampaign = $false
        sourceLevel = 'FULL'
        canonicalReceiptSha256 = $receiptSha
        expectedSelection = $composedSelection
        actualSourceSelection = $receipt.selections
        subsetRelation = 'FILTERED_COMPOSED_STRICT_SUBSET_OF_FULL_UNFILTERED'
        commonGatePlanSha256 = $commonGatePlanSha256
        separateExecutionClaimed = $false
    }
    $composedClaim = [ordered]@{}
    foreach ($entry in $composedClaimBasis.GetEnumerator()) {
        $composedClaim[$entry.Key] = $entry.Value
    }
    $composedClaim.claimSha256 = Get-GeoCeDGCloseoutStructuredSha256 $composedClaimBasis

    $fullClaimBasis = [ordered]@{
        level = 'FULL'
        fulfillment = 'ROOT_PHYSICAL_CAMPAIGN'
        physicalRole = 'SINGLE_ROOT_PHYSICAL_CAMPAIGN'
        independentPhysicalCampaign = $true
        canonicalReceiptSha256 = $receiptSha
        canonicalSelection = $receipt.selections
        cleanGeneratedOutputs = $true
        baselineFullConfirmation = $baselineBasis
        baselineFullConfirmationSha256 = `
            Get-GeoCeDGCloseoutStructuredSha256 $baselineBasis
        commonGatePlanSha256 = $commonGatePlanSha256
    }
    $fullClaim = [ordered]@{}
    foreach ($entry in $fullClaimBasis.GetEnumerator()) {
        $fullClaim[$entry.Key] = $entry.Value
    }
    $fullClaim.claimSha256 = Get-GeoCeDGCloseoutStructuredSha256 $fullClaimBasis

    # Actual values have now been independently read and checked. Rebuild the
    # execution-plan projection from those values and prove that it is byte-for-
    # byte the selected pre-heavy plan; no result-level relabel is accepted.
    $executedPlan = ConvertFrom-GeoCeDGPhaseLifecycleJson `
        (ConvertTo-GeoCeDGCloseoutJsonBytes $plan) 'executed technical campaign plan'
    $executedPlan.physicalCampaign.canonicalSelection = $receipt.selections
    $executedPlanSha = Get-GeoCeDGCloseoutPlanSha256 $executedPlan
    Assert-GeoCeDGCloseoutWorkflow ($executedPlanSha -ceq
        [string]$PreHeavyPlanComparison.selectedPlanSha256) `
        'Post-run technical campaign plan differs from the selected pre-heavy plan.'
    $inventory = @(
        [ordered]@{
            role = 'FULL_CANONICAL_RECEIPT'; path = $receiptRelative
            sha256 = $receiptSha; exitCode = [long]0
        },
        [ordered]@{
            role = 'PHASE_CHILD_SUMMARY'; path = [string]$phaseDeclaration.evidencePath
            sha256 = Get-GeoCeDGCloseoutRawSha256 $phaseSummaryBytes
            exitCode = [long]$PhaseExecution.exitCode
        }
    ) + @($phaseArtifacts) + @(
        [ordered]@{
            role = 'BASELINE_FULL_CONFIRMATION'; path = $null
            sha256 = Get-GeoCeDGCloseoutStructuredSha256 $baselineBasis
            exitCode = [long]$BaselineExecution.exitCode
        },
        [ordered]@{
            role = 'ROOT_COMMON_GATE_COMPLETION'; path = $null
            sha256 = Get-GeoCeDGCloseoutStructuredSha256 $commonGateCompletionBasis
            exitCode = [long]$CommonGateCompletion.exitCode
        }
    )
    $physicalCampaign = [ordered]@{
        level = 'FULL'
        count = [long]1
        cleanGeneratedOutputs = $true
        independentBuilds = $false
        canonicalReceiptPath = $receiptRelative
        canonicalReceiptSha256 = $receiptSha
        inputFingerprint = [string]$receipt.inputFingerprint
        inputIdentity = $receipt.inputIdentity
    }
    $manifestBasis = [ordered]@{
        physicalCampaign = $physicalCampaign
        claims = @($phaseClaim, $composedClaim, $fullClaim)
        executionConfirmations = [ordered]@{
            baselineFull = $baselineBasis
            rootCommonGate = $commonGateCompletionBasis
        }
        actualEvidenceInventory = @($inventory)
    }
    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_SINGLE_FULL_TECHNICAL_CAMPAIGN'
        coverageStrategy = 'SINGLE_PHYSICAL_FULL'
        reviewedTechnicalCommit = $technical
        readinessReceiptSha256 = [string]$readiness.sha256
        acceptancePlanSha256 = [string]$readiness.receipt.acceptancePlanSha256
        requiredLevels = @('PHASE', 'COMPOSED', 'FULL')
        physicalCampaignCount = [long]1
        planComparison = [ordered]@{
            expectedPlanSha256 = [string]$PreHeavyPlanComparison.expectedPlanSha256
            selectedPlanSha256 = [string]$PreHeavyPlanComparison.selectedPlanSha256
            executedPlanSha256 = $executedPlanSha
            comparedBeforeHeavy = $true
            expectedEqualsSelectedBeforeHeavy = $true
            selectedEqualsExecutedAfterRun = $true
        }
        physicalCampaign = $physicalCampaign
        claims = @($phaseClaim, $composedClaim, $fullClaim)
        complements = [object[]]::new(0)
        executionConfirmations = [ordered]@{
            baselineFull = $baselineBasis
            rootCommonGate = $commonGateCompletionBasis
        }
        actualEvidenceInventory = @($inventory)
        evidenceManifestSha256 = Get-GeoCeDGCloseoutStructuredSha256 $manifestBasis
        complete = $true
    }
}

function Read-GeoCeDGCloseoutTechnicalEvidenceCampaign {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [object]$PolicyContext,
        [Parameter(Mandatory)] [string]$ReadinessReceiptSha256,
        [Parameter(Mandatory)] [string]$AcceptancePlanSha256,
        [Parameter(Mandatory)] [string]$TechnicalCampaignPath,
        [switch]$RequireExactCurrentMaterialization
    )
    $path = [IO.Path]::GetFullPath($TechnicalCampaignPath)
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $path -PathType Leaf) `
        "Single FULL technical campaign result is missing: $path"
    $resultBytes = [IO.File]::ReadAllBytes($path)
    $result = ConvertFrom-GeoCeDGPhaseLifecycleJson $resultBytes `
        'single FULL technical campaign result'
    Assert-GeoCeDGPhaseLifecycleProperties $result @('schemaVersion', 'level',
        'repositoryCommit', 'phase', 'module', 'testFilters', 'state', 'exitCode',
        'independentBuilds', 'cleanGeneratedOutputs', 'dependencyCacheResetRequested',
        'keepBuildOutputs', 'canonicalReceipt', 'devEvidence', 'requestedEvidenceUse',
        'evidenceUse', 'repositoryCohort', 'reviewedCandidate', 'closeoutMode',
        'validatedCloseoutModes', 'acceptancePlanSha256', 'readinessReceiptPath',
        'readinessReceiptSha256', 'closeoutConsumable', 'reason',
        'heavyCampaignStarted', 'technicalCampaign', 'startedUtc', 'finishedUtc',
        'elapsedSeconds', 'requestedOptionalGates', 'failure', 'authorApproved',
        'selfApproved') 'Single FULL technical campaign result'
    Assert-GeoCeDGCloseoutWorkflow ($result.schemaVersion -is [long] -and
        $result.schemaVersion -eq 1 -and [string]$result.level -ceq 'FULL' -and
        [string]$result.repositoryCommit -ceq $TechnicalCommit -and
        $null -eq $result.phase -and
        [string]$result.state -ceq 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL' -and
        [int]$result.exitCode -eq 0 -and
        $result.independentBuilds -is [bool] -and -not $result.independentBuilds -and
        $result.cleanGeneratedOutputs -is [bool] -and $result.cleanGeneratedOutputs -and
        [string]$result.requestedEvidenceUse -ceq 'FINAL_ACCEPTANCE' -and
        [string]$result.evidenceUse -ceq 'FINAL_ACCEPTANCE' -and
        [string]$result.repositoryCohort -ceq 'CLEAN_COMMIT' -and
        [string]$result.reviewedCandidate -ceq $TechnicalCommit -and
        $null -eq $result.closeoutMode -and
        [string]$result.acceptancePlanSha256 -ceq $AcceptancePlanSha256 -and
        [string]$result.readinessReceiptSha256 -ceq $ReadinessReceiptSha256 -and
        $result.closeoutConsumable -is [bool] -and $result.closeoutConsumable -and
        [string]$result.reason -ceq 'READINESS_BOUND_TECHNICAL_GATES_PASSED' -and
        $result.heavyCampaignStarted -is [bool] -and $result.heavyCampaignStarted -and
        $null -eq $result.failure -and
        $result.authorApproved -is [bool] -and -not $result.authorApproved -and
        $result.selfApproved -is [bool] -and -not $result.selfApproved) `
        'Technical root is not one closeout-consumable clean FULL campaign for exact T.'
    Assert-GeoCeDGPhaseLifecycleSet @($result.validatedCloseoutModes |
            ForEach-Object { [string]$_ }) @('VERIFIED', 'AUTHOR_OPERATED') `
        'Technical root changed the mode-neutral acceptance plan.'

    $campaign = $result.technicalCampaign
    Assert-GeoCeDGPhaseLifecycleProperties $campaign @('schemaVersion', 'kind',
        'coverageStrategy', 'reviewedTechnicalCommit', 'readinessReceiptSha256',
        'acceptancePlanSha256', 'requiredLevels', 'physicalCampaignCount',
        'planComparison', 'physicalCampaign', 'claims', 'complements',
        'executionConfirmations', 'actualEvidenceInventory',
        'evidenceManifestSha256', 'complete') 'Single FULL campaign envelope'
    Assert-GeoCeDGCloseoutWorkflow ($campaign.schemaVersion -is [long] -and
        $campaign.schemaVersion -eq 1 -and [string]$campaign.kind -ceq
        'GEOCEDG_SINGLE_FULL_TECHNICAL_CAMPAIGN' -and
        [string]$campaign.coverageStrategy -ceq 'SINGLE_PHYSICAL_FULL' -and
        [string]$campaign.reviewedTechnicalCommit -ceq $TechnicalCommit -and
        [string]$campaign.readinessReceiptSha256 -ceq $ReadinessReceiptSha256 -and
        [string]$campaign.acceptancePlanSha256 -ceq $AcceptancePlanSha256 -and
        $campaign.physicalCampaignCount -is [long] -and
        $campaign.physicalCampaignCount -eq 1 -and
        $campaign.complete -is [bool] -and $campaign.complete -and
        (@($campaign.requiredLevels) -join "`n") -ceq
            (@('PHASE', 'COMPOSED', 'FULL') -join "`n") -and
        @($campaign.complements).Count -eq 0) `
        'Single FULL campaign envelope reduced, duplicated, or relabelled technical coverage.'
    Assert-GeoCeDGPhaseLifecycleProperties $campaign.planComparison @(
        'expectedPlanSha256', 'selectedPlanSha256', 'executedPlanSha256',
        'comparedBeforeHeavy', 'expectedEqualsSelectedBeforeHeavy',
        'selectedEqualsExecutedAfterRun') 'Single FULL campaign plan comparison'
    $planHash = [string]$campaign.planComparison.expectedPlanSha256
    Assert-GeoCeDGCloseoutWorkflow ($planHash -ceq
        [string]$campaign.planComparison.selectedPlanSha256 -and
        $planHash -ceq [string]$campaign.planComparison.executedPlanSha256) `
        'Single FULL campaign plan hashes are not identical.'
    $expectedPlan = Get-GeoCeDGCloseoutAcceptanceExecutionPlan `
        -RepositoryRoot $RepositoryRoot -TechnicalCommit $TechnicalCommit `
        -PolicyContext $PolicyContext
    $expectedPlanHash = Get-GeoCeDGCloseoutPlanSha256 $expectedPlan
    Assert-GeoCeDGCloseoutWorkflow ($planHash -ceq $expectedPlanHash -and
        $campaign.planComparison.comparedBeforeHeavy -is [bool] -and
        $campaign.planComparison.comparedBeforeHeavy -and
        $campaign.planComparison.expectedEqualsSelectedBeforeHeavy -is [bool] -and
        $campaign.planComparison.expectedEqualsSelectedBeforeHeavy -and
        $campaign.planComparison.selectedEqualsExecutedAfterRun -is [bool] -and
        $campaign.planComparison.selectedEqualsExecutedAfterRun) `
        'Single FULL campaign did not prove the exact pre-heavy/post-run plan equality.'

    Assert-GeoCeDGCloseoutWorkflow (@($campaign.claims).Count -eq 3 -and
        [string]$campaign.claims[0].level -ceq 'PHASE' -and
        [string]$campaign.claims[1].level -ceq 'COMPOSED' -and
        [string]$campaign.claims[2].level -ceq 'FULL') `
        'Single FULL campaign claim roles are incomplete, duplicated, or out of order.'
    Assert-GeoCeDGPhaseLifecycleProperties $campaign.executionConfirmations @(
        'baselineFull', 'rootCommonGate') 'Single FULL execution confirmations'
    $campaignRoot = Split-Path -Parent $path
    $receiptPath = Resolve-GeoCeDGCloseoutCampaignArtifactPath $campaignRoot `
        ([string]$campaign.physicalCampaign.canonicalReceiptPath) `
        'single FULL canonical receipt'
    $topReceiptPath = Resolve-GeoCeDGCloseoutEvidenceChildPath $path `
        ([string]$result.canonicalReceipt)
    Assert-GeoCeDGCloseoutWorkflow ($receiptPath.Equals($topReceiptPath,
            [StringComparison]::OrdinalIgnoreCase)) `
        'Root canonicalReceipt differs from the single physical FULL campaign receipt.'
    $baseline = $campaign.executionConfirmations.baselineFull
    $common = $campaign.executionConfirmations.rootCommonGate
    $preHeavy = [pscustomobject][ordered]@{
        expectedPlan = $expectedPlan
        selectedPlan = $expectedPlan
        expectedPlanSha256 = $expectedPlanHash
        selectedPlanSha256 = $expectedPlanHash
        comparedBeforeHeavy = $true
        expectedEqualsSelected = $true
    }
    $baselineExecution = [pscustomobject][ordered]@{
        startedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $baseline.startedUtc 'Archived baseline FULL start'
        finishedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $baseline.finishedUtc 'Archived baseline FULL finish'
        exitCode = [long]$baseline.exitCode
        fullTests = [bool]$baseline.fullTests
        canonicalReceiptPath = $receiptPath
    }
    $commonCompletion = [pscustomobject][ordered]@{
        state = [string]$common.state
        exitCode = [long]$common.exitCode
        completedUtc = ConvertTo-GeoCeDGCloseoutUtcTimestamp `
            $common.completedUtc 'Archived root common-gate completion'
        expectedGateIds = @($common.expectedGateIds)
        actualGateIds = @($common.actualGateIds)
        expectedGatePlanSha256 = [string]$common.expectedGatePlanSha256
        executedGatePlanSha256 = [string]$common.executedGatePlanSha256
    }
    $expectedCampaign = New-GeoCeDGCloseoutSingleFullCampaignEnvelope `
        -RepositoryRoot $RepositoryRoot -TechnicalCommit $TechnicalCommit `
        -PolicyPath $PolicyContext.policyPath `
        -ReadinessReceiptPath $result.readinessReceiptPath `
        -CampaignRoot $campaignRoot -CanonicalReceiptPath $receiptPath `
        -PreHeavyPlanComparison $preHeavy `
        -PhaseExecution $campaign.claims[0].execution `
        -BaselineExecution $baselineExecution `
        -CommonGateCompletion $commonCompletion
    # Compare against the original JSON token spelling, not ConvertFrom-Json's
    # host-version-specific DateTime projection. Only insignificant whitespace
    # is removed; string escapes, numbers and property order remain exact.
    $actualJson = Get-GeoCeDGCloseoutRawMinifiedJsonPropertyText `
        -Bytes $resultBytes -PropertyName 'technicalCampaign' `
        -Description 'single FULL technical campaign result'
    $expectedWrapperBytes = ConvertTo-GeoCeDGCloseoutJsonBytes `
        ([ordered]@{ technicalCampaign = $expectedCampaign })
    $expectedJson = Get-GeoCeDGCloseoutRawMinifiedJsonPropertyText `
        -Bytes $expectedWrapperBytes -PropertyName 'technicalCampaign' `
        -Description 'reconstructed single FULL technical campaign'
    Assert-GeoCeDGCloseoutWorkflow ($actualJson -ceq $expectedJson) `
        'Single FULL campaign envelope or authenticated evidence manifest is not canonical.'

    $receiptBytes = [IO.File]::ReadAllBytes($receiptPath)
    $receipt = ConvertFrom-GeoCeDGPhaseLifecycleJson $receiptBytes `
        'single FULL canonical receipt'
    $commitIndex = Get-GeoCeDGPhaseCommitIndexAuthority $RepositoryRoot $TechnicalCommit
    $emptyHash = Get-GeoCeDGCloseoutRawSha256 ([byte[]]::new(0))
    $archivedCanonicalProof = Assert-GeoCeDGArchivedCanonicalBuildReceipt `
        -Receipt $receipt -Level FULL -RepositoryRoot $RepositoryRoot `
        -TechnicalCommit $TechnicalCommit `
        -ExpectedIndexSha256 $commitIndex.IndexSha256 `
        -ExpectedStatusSha256 $emptyHash `
        -RequireExactCurrentMaterialization:$RequireExactCurrentMaterialization
    $physicalInputClosureVerified = $RequireExactCurrentMaterialization -and
        $archivedCanonicalProof.inputInventory.exactPhysicalMaterializationVerified
    if ($RequireExactCurrentMaterialization) {
        Assert-GeoCeDGCloseoutWorkflow $physicalInputClosureVerified `
            'PREPARE did not prove the single FULL archived inputs against physical clean T.'
    }
    $physicalInputAuthorities = [object[]]@(
        [pscustomobject][ordered]@{
            level = 'FULL'
            reviewedTechnicalCommit = $TechnicalCommit
            canonicalReceiptSha256 = Get-GeoCeDGCloseoutRawSha256 $receiptBytes
            inputInventoryArtifactSha256 = [string]$archivedCanonicalProof.
                inputInventory.auditArtifactSha256
            inputInventoryRawTreeSha256 = [string]$archivedCanonicalProof.
                inputInventory.rawTreeSha256
            technicalTreePathAndBlobAuthoritySha256 = [string]$archivedCanonicalProof.
                inputInventory.technicalTreePathAndBlobAuthoritySha256
        }
    )
    return [pscustomobject][ordered]@{
        rootResult = $result
        rootResultPath = $path
        rootResultSha256 = Get-GeoCeDGCloseoutRawSha256 $resultBytes
        envelope = $campaign
        claims = @($campaign.claims)
        full = [pscustomobject][ordered]@{
            level = 'FULL'
            result = $result
            resultPath = $path
            resultSha256 = Get-GeoCeDGCloseoutRawSha256 $resultBytes
            canonicalReceipt = $receipt
            canonicalReceiptPath = $receiptPath
            canonicalReceiptSha256 = Get-GeoCeDGCloseoutRawSha256 $receiptBytes
            archivedCanonicalProof = $archivedCanonicalProof
        }
        requiredLevels = @('PHASE', 'COMPOSED', 'FULL')
        reviewedTechnicalCommit = $TechnicalCommit
        closeoutMode = $null
        validatedCloseoutModes = @('VERIFIED', 'AUTHOR_OPERATED')
        acceptancePlanSha256 = $AcceptancePlanSha256
        readinessReceiptSha256 = $ReadinessReceiptSha256
        physicalInputAuthorities = $physicalInputAuthorities
        physicalInputClosureVerified = $physicalInputClosureVerified
        complete = $true
    }
}

function New-GeoCeDGCloseoutDecisionRecord {
    param(
        [Parameter(Mandatory)] [object]$PolicyContext,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Approval,
        [Parameter(Mandatory)] [object]$Campaign,
        [switch]$ReconstructHistoricalPreparationClosure
    )
    $CloseoutMode = ConvertTo-GeoCeDGCloseoutMode $CloseoutMode
    Assert-GeoCeDGCloseoutWorkflow ($Campaign.physicalInputClosureVerified -eq
        $true -or $ReconstructHistoricalPreparationClosure) `
        'A new closeout decision record requires physical input closure against exact clean T.'
    $claimAuthorities = @($Campaign.claims | ForEach-Object {
            [ordered]@{
                level = [string]$_.level
                fulfillment = [string]$_.fulfillment
                claimSha256 = [string]$_.claimSha256
            }
        })
    $technicalCampaign = [ordered]@{
        coverageStrategy = [string]$Campaign.envelope.coverageStrategy
        rootResultSha256 = [string]$Campaign.rootResultSha256
        executionPlanSha256 = [string]$Campaign.envelope.planComparison.executedPlanSha256
        evidenceManifestSha256 = [string]$Campaign.envelope.evidenceManifestSha256
        physicalCampaignCount = [long]$Campaign.envelope.physicalCampaignCount
        fullCanonicalReceiptSha256 = [string]$Campaign.full.canonicalReceiptSha256
        claims = @($claimAuthorities)
    }
    # PREPARE reaches this serializer only after comparing both archived raw
    # inventories with the physical clean T worktree. AUDIT deliberately does
    # not repeat that impossible comparison from C; it reconstructs this exact
    # historical assertion from the immutable receipt hashes and T binding.
    $physicalAuthorities = foreach ($authority in @($Campaign.physicalInputAuthorities)) {
        [ordered]@{
            level = [string]$authority.level
            canonicalReceiptSha256 = [string]$authority.canonicalReceiptSha256
            inputInventoryArtifactSha256 = `
                [string]$authority.inputInventoryArtifactSha256
            inputInventoryRawTreeSha256 = `
                [string]$authority.inputInventoryRawTreeSha256
            technicalTreePathAndBlobAuthoritySha256 = `
                [string]$authority.technicalTreePathAndBlobAuthoritySha256
        }
    }
    $closureBasis = [ordered]@{
        reviewedTechnicalCommit = $TechnicalCommit
        state = 'VERIFIED_AGAINST_EXACT_CLEAN_T'
        authorities = @($physicalAuthorities)
    }
    $physicalInputClosure = [ordered]@{
        reviewedTechnicalCommit = $TechnicalCommit
        state = 'VERIFIED_AGAINST_EXACT_CLEAN_T'
        authorities = @($physicalAuthorities)
        proofSha256 = Get-GeoCeDGCloseoutTextSha256 `
            ($closureBasis | ConvertTo-Json -Depth 30 -Compress)
    }
    return [pscustomobject][ordered]@{
        schemaVersion = 2
        kind = 'GEOCEDG_PHASE_CLOSEOUT'
        phase = [string]$PolicyContext.policy.phase
        closeoutMode = $CloseoutMode
        reviewedTechnicalCommit = $TechnicalCommit
        authorDecision = 'PASS_AUTHOR_APPROVED'
        authorApprovalSha256 = [string]$Approval.sha256
        evidence = [ordered]@{
            readinessReceiptSha256 = [string]$Readiness.sha256
            acceptancePlanSha256 = [string]$Readiness.receipt.acceptancePlanSha256
            validatedCloseoutModes = @('VERIFIED', 'AUTHOR_OPERATED')
            technicalCampaign = $technicalCampaign
            physicalInputClosureAtPreparation = $physicalInputClosure
        }
        selfApproved = $false
    }
}

function Assert-GeoCeDGCloseoutDecisionRecord {
    param(
        [Parameter(Mandatory)] [object]$Actual,
        [Parameter(Mandatory)] [object]$Expected
    )
    Assert-GeoCeDGPhaseLifecycleProperties $Actual @('schemaVersion', 'kind',
        'phase', 'closeoutMode', 'reviewedTechnicalCommit', 'authorDecision',
        'authorApprovalSha256', 'evidence', 'selfApproved') `
        'Schema-v2 closeout decision'
    Assert-GeoCeDGPhaseLifecycleProperties $Actual.evidence `
        @('readinessReceiptSha256', 'acceptancePlanSha256',
            'validatedCloseoutModes', 'technicalCampaign',
            'physicalInputClosureAtPreparation') `
        'Schema-v2 closeout evidence binding'
    $actualJson = $Actual | ConvertTo-Json -Depth 100 -Compress
    $expectedJson = $Expected | ConvertTo-Json -Depth 100 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($actualJson -ceq $expectedJson -and
        $Actual.selfApproved -is [bool] -and -not $Actual.selfApproved) `
        'Closeout decision does not exactly bind author approval and technical evidence to T.'
}

function Get-GeoCeDGCloseoutBoundedPushArguments {
    param(
        [Parameter(Mandatory)] [string]$Remote,
        [Parameter(Mandatory)] [string]$Branch,
        [Parameter(Mandatory)] [string]$TagName
    )
    # Every configuration-sensitive ref-expansion or local hook path is
    # explicitly disabled. Only the two literal refspecs below may be offered
    # to the remote, and the remote must accept them atomically.
    return [object[]]@(
        'push', '--atomic', '--no-all', '--no-mirror', '--no-tags',
        '--no-delete', '--no-force', '--no-force-with-lease',
        '--no-force-if-includes', '--recurse-submodules=no', '--no-prune',
        '--no-follow-tags', '--no-signed',
        '--no-set-upstream', '--no-verify', $Remote,
        "refs/heads/${Branch}:refs/heads/${Branch}",
        "refs/tags/${TagName}:refs/tags/${TagName}"
    )
}

function New-GeoCeDGCloseoutGitOperationPlan {
    param(
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$CommitMessage,
        [Parameter(Mandatory)] [string]$TagName,
        [Parameter(Mandatory)] [string]$TagMessage,
        [Parameter(Mandatory)] [string]$Branch,
        [Parameter(Mandatory)] [string]$Remote
    )
    $closeoutToken = 'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA'
    return [object[]]@(
        [ordered]@{ order = 1; operation = 'CREATE_STATUS_ONLY_CLOSEOUT_COMMIT'
            program = 'git'; arguments = @('commit', '--no-verify',
                '--no-gpg-sign', '-m', $CommitMessage)
            consumesPlaceholders = @(); producesPlaceholder = $null
            expectedParent = $TechnicalCommit },
        [ordered]@{ order = 2; operation = 'CAPTURE_EXACT_CLOSEOUT_SHA'
            program = 'git'; arguments = @('rev-parse', '--verify', 'HEAD^{commit}')
            consumesPlaceholders = @(); producesPlaceholder = $closeoutToken
            expectedParent = $null },
        [ordered]@{ order = 3; operation = 'VERIFY_EXACT_CLOSEOUT_PARENT'
            program = 'git'; arguments = @('rev-list', '--parents', '-n', '1',
                $closeoutToken)
            consumesPlaceholders = @($closeoutToken); producesPlaceholder = $null
            expectedParent = $TechnicalCommit },
        [ordered]@{ order = 4; operation = 'CREATE_ANNOTATED_PHASE_TAG'
            program = 'git'; arguments = @('tag', '--no-sign', '-a', $TagName,
                '-m', $TagMessage, $closeoutToken)
            consumesPlaceholders = @($closeoutToken); producesPlaceholder = $null
            expectedParent = $null },
        [ordered]@{ order = 5; operation = 'SWITCH_TO_PROMOTION_BRANCH'
            program = 'git'; arguments = @('switch', $Branch)
            consumesPlaceholders = @(); producesPlaceholder = $null
            expectedParent = $null },
        [ordered]@{ order = 6; operation = 'FAST_FORWARD_PROMOTION_BRANCH'
            program = 'git'; arguments = @('merge', '--ff-only', $closeoutToken)
            consumesPlaceholders = @($closeoutToken); producesPlaceholder = $null
            expectedParent = $null },
        [ordered]@{ order = 7; operation = 'PUSH_BRANCH_AND_TAG'
            program = 'git'; arguments = Get-GeoCeDGCloseoutBoundedPushArguments `
                -Remote $Remote -Branch $Branch -TagName $TagName
            consumesPlaceholders = @(); producesPlaceholder = $null
            expectedParent = $null }
    )
}

function Invoke-GeoCeDGCloseoutPreparation {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [string]$ReadinessReceiptPath,
        [Parameter(Mandatory)] [string]$TechnicalCampaignPath,
        [Parameter(Mandatory)] [string]$AuthorApprovalPath
    )
    $CloseoutMode = ConvertTo-GeoCeDGCloseoutMode $CloseoutMode
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    [void](Test-GeoCeDGCloseoutAcceptancePreflight -RepositoryRoot $root `
        -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath)
    $readiness = Read-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $root `
        -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath
    $policyContext = $readiness.policyContext
    $plan = Get-GeoCeDGCloseoutPlan $root $TechnicalCommit $policyContext
    $approval = Read-GeoCeDGExplicitAuthorApproval -Path $AuthorApprovalPath `
        -Phase ([string]$policyContext.policy.phase) `
        -TechnicalCommit $TechnicalCommit -CloseoutMode $CloseoutMode
    $campaign = Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
        -RepositoryRoot $root -TechnicalCommit $TechnicalCommit `
        -PolicyContext $policyContext `
        -ReadinessReceiptSha256 $readiness.sha256 `
        -AcceptancePlanSha256 ([string]$readiness.receipt.acceptancePlanSha256) `
        -TechnicalCampaignPath $TechnicalCampaignPath `
        -RequireExactCurrentMaterialization
    Assert-GeoCeDGCloseoutWorkflow $campaign.physicalInputClosureVerified `
        'PREPARE requires exact physical clean-T input closure before any mutation.'
    $record = New-GeoCeDGCloseoutDecisionRecord -PolicyContext $policyContext `
        -TechnicalCommit $TechnicalCommit -CloseoutMode $CloseoutMode `
        -Readiness $readiness -Approval $approval -Campaign $campaign
    $recordBytes = ConvertTo-GeoCeDGCloseoutJsonBytes $record
    $recordSha256 = Get-GeoCeDGCloseoutRawSha256 $recordBytes
    $expectedDelta = @($plan.trackedModes | ForEach-Object {
        [pscustomobject][ordered]@{
            path = [string]$_.path
            mode = [string]$_.mode
            expectedSha256 = [string]$_.expectedSha256
        }
    }) + @([pscustomobject][ordered]@{
        path = [string]$plan.recordPath
        mode = '100644'
        expectedSha256 = $recordSha256
    })
    $recordPhysical = Resolve-GeoCeDGPhaseLifecycleChild $root $plan.recordPath `
        'schema-v2 closeout record'
    Assert-GeoCeDGCloseoutWorkflow (-not (Test-Path -LiteralPath $recordPhysical)) `
        'Refusing to overwrite a pre-existing closeout decision record.'

    # Close the validation-to-write window. No archived evidence parsing or
    # author decision work is permitted between this fresh preflight and the
    # first bounded write.
    [void](Test-GeoCeDGCloseoutAcceptancePreflight -RepositoryRoot $root `
        -TechnicalCommit $TechnicalCommit -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath)
    $mutationStarted = $false
    try {
        # From this point the only permitted mutations are exact expected status
        # bytes, one decision record, and an index update restricted to that
        # exhaustive path set.
        $mutationStarted = $true
        foreach ($path in $plan.statusPaths) {
            $physical = Resolve-GeoCeDGPhaseLifecycleChild $root $path `
                'prepared closeout status'
            [IO.File]::WriteAllBytes($physical, [byte[]]$plan.expectedBytes[$path])
        }
        [IO.File]::WriteAllBytes($recordPhysical, $recordBytes)
        $add = Invoke-GeoCeDGCloseoutGitText $root (@('add', '--') + @($plan.allPaths))
        Assert-GeoCeDGCloseoutWorkflow ($add.ExitCode -eq 0) `
            'Unable to stage the exact closeout delta.'
        $headAfterStage = Get-GeoCeDGCloseoutExactRef $root 'HEAD' `
            'PREPARE post-stage HEAD'
        Assert-GeoCeDGCloseoutWorkflow ($headAfterStage -ceq $TechnicalCommit) `
            'Technical HEAD changed while PREPARE staged the closeout delta.'
        $status = Get-GeoCeDGPhaseLifecycleStatusPaths $root
        Assert-GeoCeDGPhaseLifecycleSet $status.Staged $plan.allPaths `
            'Prepared closeout staged paths'
        Assert-GeoCeDGCloseoutWorkflow ($status.Unstaged.Count -eq 0 -and
            $status.Untracked.Count -eq 0) `
            'PREPARE produced an unstaged or unauthorized repository change.'
        foreach ($path in $plan.statusPaths) {
            $index = (Invoke-GeoCeDGGitByteCommand -RepositoryRoot $root `
                -Arguments @('show', ":$path")).Bytes
            Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 $index) -ceq
                (Get-GeoCeDGCloseoutRawSha256 ([byte[]]$plan.expectedBytes[$path]))) `
                "Prepared index content mismatch: $path"
        }
        $recordIndex = (Invoke-GeoCeDGGitByteCommand -RepositoryRoot $root `
            -Arguments @('show', ":$($plan.recordPath)")).Bytes
        Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 $recordIndex) -ceq
            (Get-GeoCeDGCloseoutRawSha256 $recordBytes)) `
            'Prepared closeout record differs in the index.'
        Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $root `
            -ReviewedTechnicalCommit $TechnicalCommit -ExistingPaths $plan.statusPaths `
            -RecordPath $plan.recordPath -CloseoutCommit $TechnicalCommit `
            -PendingCloseout
    $tag = [string]$policyContext.policy.promotion.tagName
    $tagMessage = [string]$policyContext.policy.promotion.tagMessage
    $commitMessage = [string]$policyContext.policy.promotion.closeoutCommitMessage
    $exactTransformations = @($policyContext.policy.closeout.literalReplacements |
        ForEach-Object {
            [ordered]@{
                path = [string]$_.path
                operation = 'EXACT_LITERAL_REPLACEMENT'
                before = [string]$_.before
                after = [string]$_.after
                occurrences = [long]$_.occurrences
                expectedMode = '100644'
                expectedSha256 = $null
            }
        })
    for ($i = 0; $i -lt $exactTransformations.Count; $i++) {
        $rulePath = [string]$exactTransformations[$i].path
        $deltaEntry = @($expectedDelta | Where-Object {
                [string]$_.path -ceq $rulePath
            })
        Assert-GeoCeDGCloseoutWorkflow ($deltaEntry.Count -eq 1) `
            "Prepared transformation has no unique expected delta: $rulePath"
        $exactTransformations[$i].expectedSha256 = [string]$deltaEntry[0].expectedSha256
    }
    $exactTransformations += [ordered]@{
        path = [string]$plan.recordPath
        operation = 'CREATE_CANONICAL_AUTHOR_DECISION_RECORD'
        before = $null
        after = $null
        occurrences = 1
        expectedMode = '100644'
        expectedSha256 = $recordSha256
    }
    $branch = [string]$policyContext.policy.promotion.branch
    $remote = [string]$policyContext.policy.promotion.remote
    $closeoutToken = 'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA'
    $gitOperationPlan = New-GeoCeDGCloseoutGitOperationPlan `
        -TechnicalCommit $TechnicalCommit -CommitMessage $commitMessage `
        -TagName $tag -TagMessage $tagMessage -Branch $branch -Remote $remote
    $postPromotionAudit = [ordered]@{
        required = $true
        authority = 'AUTOMATED_READ_ONLY_AUDIT'
        operation = 'RUN_POST_PROMOTION_AUDIT'
        program = 'phase-closeout.ps1'
        sequenceAfterGitOperation = 7
        parameters = [ordered]@{
            Action = 'AUDIT'
            CloseoutMode = $CloseoutMode
            TechnicalCommit = $TechnicalCommit
            CloseoutCommit = $closeoutToken
            PolicyPath = [string]$policyContext.policyPath
            ReadinessReceiptPath = [string]$readiness.path
            TechnicalCampaignPath = [string]$campaign.rootResultPath
            AuthorApprovalPath = [string]$approval.path
            ResultPath = 'POST_PROMOTION_AUDIT_RESULT_PATH'
            RepositoryRoot = $root
        }
        consumesPlaceholders = @($closeoutToken, 'POST_PROMOTION_AUDIT_RESULT_PATH')
        automaticExecutionPerformed = $false
    }
    $modeState = if ($CloseoutMode -ceq 'VERIFIED') {
        'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION_PASSED_STATUS_ONLY_DELTA_STAGED'
    } else { 'AUTHOR_OPERATED_CLOSEOUT_PREPARED_AWAITING_HUMAN_GIT' }
    $gitAuthority = if ($CloseoutMode -ceq 'AUTHOR_OPERATED') {
        'AUTHOR'
    } else { 'VERIFIED_WORKFLOW' }
    $humanOperations = [object[]]::new(0)
    $verifiedWorkflowOperations = [object[]]::new(0)
    if ($CloseoutMode -ceq 'AUTHOR_OPERATED') {
        $humanOperations = [object[]]$gitOperationPlan
    } else {
        $verifiedWorkflowOperations = [object[]]$gitOperationPlan
    }
    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_CLOSEOUT_PREPARATION'
        state = 'STATUS_ONLY_CLOSEOUT_STAGED_AWAITING_GIT_FINALIZATION'
        modeState = $modeState
        phase = [string]$policyContext.policy.phase
        closeoutMode = $CloseoutMode
        validatedCloseoutModes = @('VERIFIED', 'AUTHOR_OPERATED')
        acceptancePlanSha256 = [string]$readiness.receipt.acceptancePlanSha256
        reviewedTechnicalCommit = $TechnicalCommit
        expectedCloseoutPaths = @($plan.allPaths)
        expectedCloseoutDelta = @($expectedDelta)
        exactTransformations = @($exactTransformations)
        closeoutRecordPath = [string]$plan.recordPath
        closeoutRecordSha256 = $recordSha256
        physicalInputClosureAtPreparation = `
            $record.evidence.physicalInputClosureAtPreparation
        promotion = [ordered]@{
            branch = $branch
            remote = $remote
            remoteAuthoritySha256 = `
                [string]$readiness.receipt.repository.remoteIdentity.authoritySha256
            prePromotionTip = [string]$readiness.receipt.promotion.anchorCommit
            technicalTarget = $TechnicalCommit
            expectedCloseoutParent = $TechnicalCommit
            closeoutCommitIdentity = $closeoutToken
            closeoutCommitMessage = $commitMessage
        }
        expectedTag = [ordered]@{
            name = $tag
            message = $tagMessage
            targetIdentity = $closeoutToken
        }
        verifiedAuthorCloseout = [ordered]@{
            gate = 'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION'
            state = $(if ($CloseoutMode -ceq 'VERIFIED') {
                    'PASSED_PROJECTED_DELTA_AWAITING_C'
                } else {
                    'NOT_APPLICABLE_AUTHOR_OPERATED'
                })
            exactTechnicalCommitApproved = $true
            technicalEvidenceCampaignVerified = $true
            boundedStatusOnlyDeltaVerified = $true
            selfApproved = $false
        }
        gitFinalizationAuthority = $gitAuthority
        remainingGitOperations = @($gitOperationPlan)
        humanOperations = $humanOperations
        verifiedWorkflowOperations = $verifiedWorkflowOperations
        postPromotionAudit = $postPromotionAudit
        automaticCommitPerformed = $false
        automaticTagPerformed = $false
        automaticFastForwardPerformed = $false
        automaticPushPerformed = $false
        technicalExecutionRepeated = $false
        selfApproved = $false
    }
    } catch {
        $preparationFailure = $_
        if ($mutationStarted) {
            throw "PREPARE failed after its first bounded mutation; repository state was preserved for inspection: $($preparationFailure.Exception.Message)"
        }
        throw $preparationFailure
    }
}

function Assert-GeoCeDGCloseoutLinearPromotionHistory {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$AnchorCommit,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit
    )
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $AnchorCommit)
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $TechnicalCommit)
    [void](Resolve-GeoCeDGPhaseLifecycleCommit $RepositoryRoot $CloseoutCommit)
    $ancestor = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('merge-base', '--is-ancestor', $AnchorCommit, $CloseoutCommit) -AllowFailure
    Assert-GeoCeDGCloseoutWorkflow ($ancestor.ExitCode -eq 0) `
        'Published closeout is not a fast-forward descendant of the readiness anchor.'
    $cursor = $CloseoutCommit
    $seenTechnical = $false
    $count = 0
    while ($cursor -cne $AnchorCommit) {
        $count++
        Assert-GeoCeDGCloseoutWorkflow ($count -le 10000) `
            'Promotion ancestry exceeded the bounded linear-history audit.'
        if ($cursor -ceq $TechnicalCommit) { $seenTechnical = $true }
        $line = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
            @('rev-list', '--parents', '-n', '1', $cursor)).Text
        $parts = @($line -split ' ')
        Assert-GeoCeDGCloseoutWorkflow ($parts.Count -eq 2 -and
            $parts[0] -ceq $cursor) `
            'Promotion path contains a merge or malformed replacement history.'
        $cursor = $parts[1]
    }
    if ($AnchorCommit -ceq $TechnicalCommit) { $seenTechnical = $true }
    Assert-GeoCeDGCloseoutWorkflow $seenTechnical `
        'Approved technical commit T was substituted on the promotion path.'
    return [pscustomobject][ordered]@{
        anchorCommit = $AnchorCommit
        technicalCommit = $TechnicalCommit
        closeoutCommit = $CloseoutCommit
        commitsAfterAnchor = $count
        topology = 'LINEAR_SINGLE_PARENT_FAST_FORWARD'
        approvedTechnicalCommitPreserved = $true
    }
}

function Assert-GeoCeDGCreatedCloseoutCommitBeforePromotion {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory)] [object]$PolicyContext,
        [Parameter(Mandatory)] [object]$Plan,
        [Parameter(Mandatory)] [byte[]]$ExpectedRecordBytes
    )
    $head = Get-GeoCeDGCloseoutExactRef $RepositoryRoot 'HEAD' `
        'created closeout HEAD'
    Assert-GeoCeDGCloseoutWorkflow ($head -ceq $CloseoutCommit) `
        'Created closeout validation requires HEAD to equal exact C.'
    $line = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('rev-list', '--parents', '-n', '1', $CloseoutCommit)).Text
    Assert-GeoCeDGCloseoutWorkflow ($line -ceq
        "$CloseoutCommit $TechnicalCommit") `
        'Created C is not the direct, single-parent child of exact T.'
    $message = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('show', '-s', '--format=%B', $CloseoutCommit)).Text
    Assert-GeoCeDGCloseoutWorkflow ($message -ceq
        [string]$PolicyContext.policy.promotion.closeoutCommitMessage) `
        'Created closeout commit message differs from frozen policy.'
    $policyAtCloseout = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
        $CloseoutCommit $PolicyContext.policyPath
    Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 `
            $policyAtCloseout) -ceq [string]$PolicyContext.policyBlobSha256) `
        'Created C changed the frozen schema-v2 closeout policy reviewed at T.'
    $treeProof = Assert-GeoCeDGRepositoryTreeDelta `
        -RepositoryRoot $RepositoryRoot -ReviewedCommit $TechnicalCommit `
        -CloseoutCommit $CloseoutCommit -AllowedPaths $Plan.allPaths
    foreach ($path in $Plan.statusPaths) {
        $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
            $CloseoutCommit $path
        Assert-GeoCeDGCloseoutWorkflow ([Convert]::ToBase64String($actual) -ceq
            [Convert]::ToBase64String([byte[]]$Plan.expectedBytes[$path])) `
            "Created status-only content differs from frozen policy: $path"
    }
    $recordBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $RepositoryRoot `
        $CloseoutCommit $Plan.recordPath
    Assert-GeoCeDGCloseoutWorkflow ($recordBytes.Length -eq
        $ExpectedRecordBytes.Length -and
        [Convert]::ToBase64String($recordBytes) -ceq
            [Convert]::ToBase64String($ExpectedRecordBytes)) `
        'Created closeout decision-record bytes differ from PREPARE authority.'
    Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $RepositoryRoot `
        -ReviewedTechnicalCommit $TechnicalCommit `
        -ExistingPaths $Plan.statusPaths -RecordPath $Plan.recordPath `
        -CloseoutCommit $CloseoutCommit
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $RepositoryRoot
    Assert-GeoCeDGCloseoutWorkflow ($status.Staged.Count -eq 0 -and
        $status.Unstaged.Count -eq 0 -and $status.Untracked.Count -eq 0) `
        'Created closeout commit left a dirty index or worktree.'
    $materialization = Assert-GeoCeDGWorktreeMaterialization `
        -RepositoryRoot $RepositoryRoot -ExpectedCommit $CloseoutCommit
    return [pscustomobject][ordered]@{
        closeoutCommit = $CloseoutCommit
        directSingleParent = $true
        exactCommitMessage = $true
        exactAllowedTreeDelta = $true
        exactContentAndModes = $true
        executableAndVerificationInputsUnchanged = $true
        cleanMaterialization = $true
        tree = $treeProof
        trackedSha256 = [string]$materialization.trackedSha256
    }
}

function Get-GeoCeDGCloseoutLocalAnnotatedTagAuthority {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TagName,
        [Parameter(Mandatory)] [string]$ExpectedMessage,
        [Parameter(Mandatory)] [string]$CloseoutCommit
    )
    $tagRef = "refs/tags/$TagName"
    $tagObject = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('rev-parse', '--verify', "${tagRef}^{tag}")).Text
    Assert-GeoCeDGCloseoutWorkflow ($tagObject -cmatch '^[0-9a-f]{40}$') `
        'Expected local phase tag is missing or lightweight.'
    Assert-GeoCeDGCloseoutWorkflow ((Invoke-GeoCeDGCloseoutGitText `
            $RepositoryRoot @('cat-file', '-t', $tagObject)).Text -ceq 'tag') `
        'Expected local phase tag is not annotated.'
    $payload = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('cat-file', '-p', $tagObject)).Text
    $parts = [regex]::Split($payload, '\r?\n\r?\n', 2)
    Assert-GeoCeDGCloseoutWorkflow ($parts.Count -eq 2 -and
        $parts[1] -ceq $ExpectedMessage) `
        'Annotated phase-tag message differs from frozen policy.'
    $headers = @($parts[0] -split '\r?\n')
    Assert-GeoCeDGCloseoutWorkflow (@($headers | Where-Object {
                $_ -ceq "object $CloseoutCommit"
            }).Count -eq 1 -and @($headers | Where-Object {
                $_ -ceq 'type commit'
            }).Count -eq 1 -and @($headers | Where-Object {
                $_ -ceq "tag $TagName"
            }).Count -eq 1) `
        'Annotated phase-tag headers differ from expected C authority.'
    $peeled = (Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('rev-parse', '--verify', "${tagRef}^{}")).Text
    Assert-GeoCeDGCloseoutWorkflow ($peeled -ceq $CloseoutCommit) `
        'Local annotated phase tag does not peel to C.'
    return [pscustomobject][ordered]@{
        name = $TagName
        tagObject = $tagObject
        targetCommit = $CloseoutCommit
        message = $ExpectedMessage
        annotated = $true
    }
}

function Get-GeoCeDGCloseoutAnnotatedTagAuthority {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$Remote,
        [Parameter(Mandatory)] [string]$TagName,
        [Parameter(Mandatory)] [string]$ExpectedMessage,
        [Parameter(Mandatory)] [string]$CloseoutCommit
    )
    $local = Get-GeoCeDGCloseoutLocalAnnotatedTagAuthority `
        -RepositoryRoot $RepositoryRoot -TagName $TagName `
        -ExpectedMessage $ExpectedMessage -CloseoutCommit $CloseoutCommit
    $tagRef = "refs/tags/$TagName"
    $tagObject = [string]$local.tagObject

    $remoteResult = Invoke-GeoCeDGCloseoutGitText $RepositoryRoot `
        @('ls-remote', '--tags', $Remote, $tagRef, "${tagRef}^{}")
    $remoteMap = @{}
    foreach ($line in @($remoteResult.Text -split "`n" | Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        })) {
        $match = [regex]::Match($line, '^([0-9a-f]{40})\t(.+)$')
        Assert-GeoCeDGCloseoutWorkflow ($match.Success -and
            -not $remoteMap.ContainsKey($match.Groups[2].Value)) `
            'Live remote phase-tag advertisement is malformed or duplicate.'
        $remoteMap[$match.Groups[2].Value] = $match.Groups[1].Value
    }
    Assert-GeoCeDGCloseoutWorkflow ($remoteMap.Count -eq 2 -and
        $remoteMap.ContainsKey($tagRef) -and
        $remoteMap.ContainsKey("${tagRef}^{}") -and
        [string]$remoteMap[$tagRef] -ceq $tagObject -and
        [string]$remoteMap["${tagRef}^{}"] -ceq $CloseoutCommit) `
        'Live remote annotated tag object or peeled C target diverges.'
    return [pscustomobject][ordered]@{
        name = $TagName
        tagObject = $tagObject
        targetCommit = $CloseoutCommit
        message = $ExpectedMessage
        annotated = $true
        localAndRemoteObjectsIdentical = $true
    }
}

function Invoke-GeoCeDGVerifiedCloseoutFinalization {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [string]$ReadinessReceiptPath,
        [Parameter(Mandatory)] [string]$TechnicalCampaignPath,
        [Parameter(Mandatory)] [string]$AuthorApprovalPath,
        [Parameter(Mandatory)] [string]$PreparationResultPath
    )
    $CloseoutMode = ConvertTo-GeoCeDGCloseoutMode $CloseoutMode
    Assert-GeoCeDGCloseoutWorkflow ($CloseoutMode -ceq 'VERIFIED') `
        'FINALIZE is available only for explicit CLOSEOUT_MODE=VERIFIED.'
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $readiness = Read-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $root `
        -TechnicalCommit $technical -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath
    $policyContext = $readiness.policyContext
    $plan = Get-GeoCeDGCloseoutPlan $root $technical $policyContext
    $approval = Read-GeoCeDGExplicitAuthorApproval -Path $AuthorApprovalPath `
        -Phase ([string]$policyContext.policy.phase) `
        -TechnicalCommit $technical -CloseoutMode VERIFIED
    $campaign = Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
        -RepositoryRoot $root -TechnicalCommit $technical `
        -PolicyContext $policyContext `
        -ReadinessReceiptSha256 $readiness.sha256 `
        -AcceptancePlanSha256 ([string]$readiness.receipt.acceptancePlanSha256) `
        -TechnicalCampaignPath $TechnicalCampaignPath
    $expectedRecord = New-GeoCeDGCloseoutDecisionRecord `
        -PolicyContext $policyContext -TechnicalCommit $technical `
        -CloseoutMode VERIFIED -Readiness $readiness -Approval $approval `
        -Campaign $campaign -ReconstructHistoricalPreparationClosure
    $expectedRecordBytes = ConvertTo-GeoCeDGCloseoutJsonBytes $expectedRecord

    $preparationPath = [IO.Path]::GetFullPath($PreparationResultPath)
    Assert-GeoCeDGCloseoutWorkflow (Test-Path -LiteralPath $preparationPath -PathType Leaf) `
        "VERIFIED preparation receipt is missing: $preparationPath"
    $preparationBytes = [IO.File]::ReadAllBytes($preparationPath)
    $preparation = ConvertFrom-GeoCeDGPhaseLifecycleJson $preparationBytes `
        'VERIFIED closeout preparation receipt'
    Assert-GeoCeDGPhaseLifecycleProperties $preparation @('schemaVersion', 'kind',
        'state', 'modeState', 'phase', 'closeoutMode', 'validatedCloseoutModes',
        'acceptancePlanSha256', 'reviewedTechnicalCommit', 'expectedCloseoutPaths',
        'expectedCloseoutDelta', 'exactTransformations', 'closeoutRecordPath',
        'closeoutRecordSha256', 'physicalInputClosureAtPreparation', 'promotion',
        'expectedTag', 'verifiedAuthorCloseout', 'gitFinalizationAuthority',
        'remainingGitOperations', 'humanOperations', 'verifiedWorkflowOperations',
        'postPromotionAudit', 'automaticCommitPerformed', 'automaticTagPerformed',
        'automaticFastForwardPerformed', 'automaticPushPerformed',
        'technicalExecutionRepeated', 'selfApproved') `
        'VERIFIED closeout preparation receipt'
    Assert-GeoCeDGCloseoutWorkflow ($preparation.schemaVersion -is [long] -and
        $preparation.schemaVersion -eq 1 -and [string]$preparation.kind -ceq
        'GEOCEDG_CLOSEOUT_PREPARATION' -and [string]$preparation.state -ceq
        'STATUS_ONLY_CLOSEOUT_STAGED_AWAITING_GIT_FINALIZATION' -and
        [string]$preparation.closeoutMode -ceq 'VERIFIED' -and
        [string]$preparation.reviewedTechnicalCommit -ceq $technical -and
        [string]$preparation.acceptancePlanSha256 -ceq
            [string]$readiness.receipt.acceptancePlanSha256 -and
        [string]$preparation.closeoutRecordPath -ceq [string]$plan.recordPath -and
        [string]$preparation.closeoutRecordSha256 -ceq
            (Get-GeoCeDGCloseoutRawSha256 $expectedRecordBytes) -and
        [string]$preparation.gitFinalizationAuthority -ceq 'VERIFIED_WORKFLOW' -and
        @($preparation.humanOperations).Count -eq 0 -and
        @($preparation.verifiedWorkflowOperations).Count -eq 7 -and
        @($preparation.remainingGitOperations).Count -eq 7 -and
        -not [bool]$preparation.automaticCommitPerformed -and
        -not [bool]$preparation.automaticTagPerformed -and
        -not [bool]$preparation.automaticFastForwardPerformed -and
        -not [bool]$preparation.automaticPushPerformed -and
        -not [bool]$preparation.technicalExecutionRepeated -and
        -not [bool]$preparation.selfApproved) `
        'VERIFIED preparation receipt is not the exact pending finalization authority.'
    Assert-GeoCeDGPhaseLifecycleProperties $preparation.promotion @('branch',
        'remote', 'remoteAuthoritySha256', 'prePromotionTip', 'technicalTarget',
        'expectedCloseoutParent', 'closeoutCommitIdentity',
        'closeoutCommitMessage') 'VERIFIED preparation promotion authority'
    Assert-GeoCeDGPhaseLifecycleProperties $preparation.expectedTag @('name',
        'message', 'targetIdentity') 'VERIFIED preparation tag authority'
    Assert-GeoCeDGPhaseLifecycleProperties $preparation.verifiedAuthorCloseout `
        @('gate', 'state', 'exactTechnicalCommitApproved',
            'technicalEvidenceCampaignVerified', 'boundedStatusOnlyDeltaVerified',
            'selfApproved') 'VERIFIED AUTHOR_CLOSEOUT preparation gate'
    $preparedBranch = [string]$policyContext.policy.promotion.branch
    $preparedRemote = [string]$policyContext.policy.promotion.remote
    $preparedTag = [string]$policyContext.policy.promotion.tagName
    $preparedCommitMessage = `
        [string]$policyContext.policy.promotion.closeoutCommitMessage
    $preparedTagMessage = [string]$policyContext.policy.promotion.tagMessage
    $closeoutToken = 'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA'
    $expectedOperationPlan = New-GeoCeDGCloseoutGitOperationPlan `
        -TechnicalCommit $technical -CommitMessage $preparedCommitMessage `
        -TagName $preparedTag -TagMessage $preparedTagMessage `
        -Branch $preparedBranch -Remote $preparedRemote
    $expectedOperationsJson = ConvertTo-Json -InputObject `
        ([object[]]$expectedOperationPlan) -Depth 30 -Compress
    $remainingOperationsJson = ConvertTo-Json -InputObject `
        ([object[]]@($preparation.remainingGitOperations)) -Depth 30 -Compress
    $verifiedOperationsJson = ConvertTo-Json -InputObject `
        ([object[]]@($preparation.verifiedWorkflowOperations)) -Depth 30 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($remainingOperationsJson -ceq
        $expectedOperationsJson -and $verifiedOperationsJson -ceq
        $expectedOperationsJson -and [string]$preparation.phase -ceq
        [string]$policyContext.policy.phase -and [string]$preparation.modeState -ceq
        'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION_PASSED_STATUS_ONLY_DELTA_STAGED' -and
        [string]$preparation.promotion.branch -ceq $preparedBranch -and
        [string]$preparation.promotion.remote -ceq $preparedRemote -and
        [string]$preparation.promotion.remoteAuthoritySha256 -ceq
            [string]$readiness.receipt.repository.remoteIdentity.authoritySha256 -and
        [string]$preparation.promotion.prePromotionTip -ceq
            [string]$readiness.receipt.promotion.anchorCommit -and
        [string]$preparation.promotion.technicalTarget -ceq $technical -and
        [string]$preparation.promotion.expectedCloseoutParent -ceq $technical -and
        [string]$preparation.promotion.closeoutCommitIdentity -ceq $closeoutToken -and
        [string]$preparation.promotion.closeoutCommitMessage -ceq
            $preparedCommitMessage -and [string]$preparation.expectedTag.name -ceq
            $preparedTag -and [string]$preparation.expectedTag.message -ceq
            $preparedTagMessage -and
        [string]$preparation.expectedTag.targetIdentity -ceq $closeoutToken -and
        [string]$preparation.verifiedAuthorCloseout.gate -ceq
            'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION' -and
        [string]$preparation.verifiedAuthorCloseout.state -ceq
            'PASSED_PROJECTED_DELTA_AWAITING_C' -and
        [bool]$preparation.verifiedAuthorCloseout.exactTechnicalCommitApproved -and
        [bool]$preparation.verifiedAuthorCloseout.technicalEvidenceCampaignVerified -and
        [bool]$preparation.verifiedAuthorCloseout.boundedStatusOnlyDeltaVerified -and
        -not [bool]$preparation.verifiedAuthorCloseout.selfApproved) `
        'VERIFIED preparation receipt changed its exact Git finalization authority.'
    Assert-GeoCeDGPhaseLifecycleSet @($preparation.validatedCloseoutModes |
            ForEach-Object { [string]$_ }) @('VERIFIED', 'AUTHOR_OPERATED') `
        'VERIFIED preparation validated modes'
    $expectedDelta = @($plan.trackedModes | ForEach-Object {
            [ordered]@{
                path = [string]$_.path
                mode = [string]$_.mode
                expectedSha256 = [string]$_.expectedSha256
            }
        }) + @([ordered]@{
            path = [string]$plan.recordPath
            mode = '100644'
            expectedSha256 = Get-GeoCeDGCloseoutRawSha256 $expectedRecordBytes
        })
    $expectedTransformations = @($policyContext.policy.closeout.literalReplacements |
        ForEach-Object {
            $path = [string]$_.path
            $delta = @($expectedDelta | Where-Object {
                    [string]$_.path -ceq $path
                })
            Assert-GeoCeDGCloseoutWorkflow ($delta.Count -eq 1) `
                "VERIFIED transformation has no unique delta: $path"
            [ordered]@{
                path = $path
                operation = 'EXACT_LITERAL_REPLACEMENT'
                before = [string]$_.before
                after = [string]$_.after
                occurrences = [long]$_.occurrences
                expectedMode = '100644'
                expectedSha256 = [string]$delta[0].expectedSha256
            }
        }) + @([ordered]@{
            path = [string]$plan.recordPath
            operation = 'CREATE_CANONICAL_AUTHOR_DECISION_RECORD'
            before = $null
            after = $null
            occurrences = [long]1
            expectedMode = '100644'
            expectedSha256 = Get-GeoCeDGCloseoutRawSha256 $expectedRecordBytes
        })
    $expectedPostPromotionAudit = [ordered]@{
        required = $true
        authority = 'AUTOMATED_READ_ONLY_AUDIT'
        operation = 'RUN_POST_PROMOTION_AUDIT'
        program = 'phase-closeout.ps1'
        sequenceAfterGitOperation = [long]7
        parameters = [ordered]@{
            Action = 'AUDIT'
            CloseoutMode = 'VERIFIED'
            TechnicalCommit = $technical
            CloseoutCommit = $closeoutToken
            PolicyPath = [string]$policyContext.policyPath
            ReadinessReceiptPath = [string]$readiness.path
            TechnicalCampaignPath = [string]$campaign.rootResultPath
            AuthorApprovalPath = [string]$approval.path
            ResultPath = 'POST_PROMOTION_AUDIT_RESULT_PATH'
            RepositoryRoot = $root
        }
        consumesPlaceholders = @($closeoutToken,
            'POST_PROMOTION_AUDIT_RESULT_PATH')
        automaticExecutionPerformed = $false
    }
    $expectedDeltaJson = ConvertTo-Json -InputObject ([object[]]$expectedDelta) `
        -Depth 30 -Compress
    $actualDeltaJson = ConvertTo-Json -InputObject `
        ([object[]]@($preparation.expectedCloseoutDelta)) -Depth 30 -Compress
    $expectedTransformationsJson = ConvertTo-Json -InputObject `
        ([object[]]$expectedTransformations) -Depth 30 -Compress
    $actualTransformationsJson = ConvertTo-Json -InputObject `
        ([object[]]@($preparation.exactTransformations)) -Depth 30 -Compress
    $expectedAuditJson = $expectedPostPromotionAudit |
        ConvertTo-Json -Depth 30 -Compress
    $actualAuditJson = $preparation.postPromotionAudit |
        ConvertTo-Json -Depth 30 -Compress
    $expectedClosureJson = $expectedRecord.evidence.
        physicalInputClosureAtPreparation | ConvertTo-Json -Depth 30 -Compress
    $actualClosureJson = $preparation.physicalInputClosureAtPreparation |
        ConvertTo-Json -Depth 30 -Compress
    Assert-GeoCeDGCloseoutWorkflow ($actualDeltaJson -ceq $expectedDeltaJson -and
        $actualTransformationsJson -ceq $expectedTransformationsJson -and
        $actualAuditJson -ceq $expectedAuditJson -and
        $actualClosureJson -ceq $expectedClosureJson) `
        'VERIFIED preparation receipt differs from its canonical staged authority.'
    Assert-GeoCeDGPhaseLifecycleSet @($preparation.expectedCloseoutPaths |
            ForEach-Object { [string]$_ }) $plan.allPaths `
        'VERIFIED prepared closeout paths'

    $head = Get-GeoCeDGCloseoutExactRef $root 'HEAD' 'VERIFIED FINALIZE HEAD'
    Assert-GeoCeDGCloseoutWorkflow ($head -ceq $technical) `
        'VERIFIED FINALIZE requires HEAD to remain exact T.'
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $root
    Assert-GeoCeDGPhaseLifecycleSet $status.Staged $plan.allPaths `
        'VERIFIED FINALIZE staged paths'
    Assert-GeoCeDGCloseoutWorkflow ($status.Unstaged.Count -eq 0 -and
        $status.Untracked.Count -eq 0) `
        'VERIFIED FINALIZE requires only the exact staged closeout delta.'
    foreach ($path in $plan.statusPaths) {
        $indexBytes = (Invoke-GeoCeDGGitByteCommand -RepositoryRoot $root `
            -Arguments @('show', ":$path")).Bytes
        Assert-GeoCeDGCloseoutWorkflow ([Convert]::ToBase64String($indexBytes) -ceq
            [Convert]::ToBase64String([byte[]]$plan.expectedBytes[$path])) `
            "VERIFIED staged status content differs: $path"
    }
    $recordIndexBytes = (Invoke-GeoCeDGGitByteCommand -RepositoryRoot $root `
        -Arguments @('show', ":$($plan.recordPath)")).Bytes
    Assert-GeoCeDGCloseoutWorkflow ([Convert]::ToBase64String($recordIndexBytes) -ceq
        [Convert]::ToBase64String($expectedRecordBytes)) `
        'VERIFIED staged decision record differs from the canonical PREPARE record.'
    Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $root `
        -ReviewedTechnicalCommit $technical -ExistingPaths $plan.statusPaths `
        -RecordPath $plan.recordPath -CloseoutCommit $technical -PendingCloseout
    $pendingTechnicalMaterialization = Assert-GeoCeDGWorktreeMaterialization `
        -RepositoryRoot $root -ExpectedCommit $technical `
        -AllowedStatusPaths $plan.allPaths

    $branch = [string]$policyContext.policy.promotion.branch
    $remote = [string]$policyContext.policy.promotion.remote
    $anchor = [string]$readiness.receipt.promotion.anchorCommit
    [void](Assert-GeoCeDGCloseoutBoundedPushConfiguration $root $remote)
    $remoteIdentity = Get-GeoCeDGCloseoutRemoteIdentity $root $remote
    Assert-GeoCeDGCloseoutWorkflow ([string]$remoteIdentity.authoritySha256 -ceq
        [string]$readiness.receipt.repository.remoteIdentity.authoritySha256) `
        'VERIFIED FINALIZE promotion remote identity changed after PREPARE.'
    $localMain = Get-GeoCeDGCloseoutExactRef $root "refs/heads/$branch" `
        'VERIFIED pre-promotion main'
    $tracking = Get-GeoCeDGCloseoutExactRef $root "refs/remotes/$remote/$branch" `
        'VERIFIED pre-promotion origin/main'
    $live = Get-GeoCeDGCloseoutLiveBranchRef $root $remote $branch
    Assert-GeoCeDGCloseoutWorkflow ($localMain -ceq $anchor -and
        $tracking -ceq $anchor -and $live -ceq $anchor) `
        'VERIFIED FINALIZE promotion anchor changed after PREPARE.'
    $tagName = [string]$policyContext.policy.promotion.tagName
    $tagAbsence = Test-GeoCeDGCloseoutTagAbsent $root $remote $tagName
    Assert-GeoCeDGCloseoutWorkflow ($tagAbsence.local -and $tagAbsence.remote) `
        'VERIFIED FINALIZE expected tag is no longer absent.'

    $operations = [Collections.Generic.List[object]]::new()
    $commitMessage = [string]$policyContext.policy.promotion.closeoutCommitMessage
    $tagMessage = [string]$policyContext.policy.promotion.tagMessage
    $commitArguments = @('commit', '--no-verify', '--no-gpg-sign', '-m',
        $commitMessage)
    $commitResult = Invoke-GeoCeDGCloseoutGitText $root $commitArguments
    $operations.Add([ordered]@{ order = 1; operation = 'CREATE_STATUS_ONLY_CLOSEOUT_COMMIT'
        program = 'git'; arguments = $commitArguments
        exitCode = [long]$commitResult.ExitCode })
    $closeout = Get-GeoCeDGCloseoutExactRef $root 'HEAD' 'Created closeout commit C'
    $operations.Add([ordered]@{ order = 2; operation = 'CAPTURE_EXACT_CLOSEOUT_SHA'
        program = 'git'; arguments = @('rev-parse', '--verify', 'HEAD^{commit}')
        exitCode = [long]0; capturedCommit = $closeout })
    # C is exhaustively validated before any tag, branch movement, or push.
    # Hooks are bypassed for commit-message/content policy, but this proof is
    # still mandatory and catches any mutation by an unexpected post-commit
    # hook or concurrent process.
    $createdCommitProof = Assert-GeoCeDGCreatedCloseoutCommitBeforePromotion `
        -RepositoryRoot $root -TechnicalCommit $technical `
        -CloseoutCommit $closeout -PolicyContext $policyContext -Plan $plan `
        -ExpectedRecordBytes $expectedRecordBytes
    $operations.Add([ordered]@{ order = 3; operation = 'VERIFY_EXACT_CLOSEOUT_PARENT'
        program = 'git'; arguments = @('rev-list', '--parents', '-n', '1', $closeout)
        exitCode = [long]0; expectedParent = $technical
        prePromotionCommitProof = $createdCommitProof })
    $tagArguments = @('tag', '--no-sign', '-a', $tagName, '-m', $tagMessage,
        $closeout)
    $tagResult = Invoke-GeoCeDGCloseoutGitText $root $tagArguments
    $operations.Add([ordered]@{ order = 4; operation = 'CREATE_ANNOTATED_PHASE_TAG'
        program = 'git'; arguments = $tagArguments
        exitCode = [long]$tagResult.ExitCode })
    $switchResult = Invoke-GeoCeDGCloseoutGitText $root @('switch', $branch)
    $operations.Add([ordered]@{ order = 5; operation = 'SWITCH_TO_PROMOTION_BRANCH'
        program = 'git'; arguments = @('switch', $branch)
        exitCode = [long]$switchResult.ExitCode })
    $mergeResult = Invoke-GeoCeDGCloseoutGitText $root `
        @('merge', '--ff-only', $closeout)
    $operations.Add([ordered]@{ order = 6; operation = 'FAST_FORWARD_PROMOTION_BRANCH'
        program = 'git'; arguments = @('merge', '--ff-only', $closeout)
        exitCode = [long]$mergeResult.ExitCode })
    # Revalidate every observable publication precondition immediately before
    # the only remote mutation.  The local branch is now C, while the tracking
    # and live tips must still be the sealed readiness anchor and the remote tag
    # must remain absent.
    $beforePushProof = Assert-GeoCeDGCreatedCloseoutCommitBeforePromotion `
        -RepositoryRoot $root -TechnicalCommit $technical `
        -CloseoutCommit $closeout -PolicyContext $policyContext -Plan $plan `
        -ExpectedRecordBytes $expectedRecordBytes
    [void](Assert-GeoCeDGCloseoutBoundedPushConfiguration $root $remote)
    $remoteIdentityBeforePush = Get-GeoCeDGCloseoutRemoteIdentity $root $remote
    Assert-GeoCeDGCloseoutWorkflow ([string]$remoteIdentityBeforePush.authoritySha256 `
        -ceq [string]$readiness.receipt.repository.remoteIdentity.authoritySha256) `
        'VERIFIED promotion remote identity changed immediately before push.'
    $localMainBeforePush = Get-GeoCeDGCloseoutExactRef $root `
        "refs/heads/$branch" 'VERIFIED main immediately before push'
    $trackingBeforePush = Get-GeoCeDGCloseoutExactRef $root `
        "refs/remotes/$remote/$branch" 'VERIFIED origin/main immediately before push'
    $liveBeforePush = Get-GeoCeDGCloseoutLiveBranchRef $root $remote $branch
    Assert-GeoCeDGCloseoutWorkflow ($localMainBeforePush -ceq $closeout -and
        $trackingBeforePush -ceq $anchor -and $liveBeforePush -ceq $anchor) `
        'VERIFIED promotion anchor changed immediately before atomic push.'
    $localTagBeforePush = Get-GeoCeDGCloseoutLocalAnnotatedTagAuthority `
        -RepositoryRoot $root -TagName $tagName -ExpectedMessage $tagMessage `
        -CloseoutCommit $closeout
    $tagStateBeforePush = Test-GeoCeDGCloseoutTagAbsent $root $remote $tagName
    Assert-GeoCeDGCloseoutWorkflow (-not $tagStateBeforePush.local -and
        $tagStateBeforePush.remote) `
        'VERIFIED remote tag appeared or local tag disappeared before atomic push.'
    $branchRefspec = "refs/heads/${branch}:refs/heads/${branch}"
    $tagRefspec = "refs/tags/${tagName}:refs/tags/${tagName}"
    $pushArguments = Get-GeoCeDGCloseoutBoundedPushArguments `
        -Remote $remote -Branch $branch -TagName $tagName
    $pushResult = Invoke-GeoCeDGCloseoutGitText $root $pushArguments
    $operations.Add([ordered]@{ order = 7; operation = 'PUSH_BRANCH_AND_TAG'
        program = 'git'; arguments = $pushArguments
        exitCode = [long]$pushResult.ExitCode; atomic = $true; force = $false })

    $audit = Test-GeoCeDGCloseoutPostPromotionAudit -RepositoryRoot $root `
        -TechnicalCommit $technical -CloseoutCommit $closeout `
        -PolicyPath $PolicyPath -CloseoutMode VERIFIED `
        -ReadinessReceiptPath $ReadinessReceiptPath `
        -TechnicalCampaignPath $TechnicalCampaignPath `
        -AuthorApprovalPath $AuthorApprovalPath
    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_VERIFIED_CLOSEOUT_FINALIZATION'
        state = 'VERIFIED_CLOSEOUT_FINALIZED_AND_AUDITED'
        closeoutMode = 'VERIFIED'
        reviewedTechnicalCommit = $technical
        closeoutCommit = $closeout
        preparationReceiptSha256 = Get-GeoCeDGCloseoutRawSha256 $preparationBytes
        prePromotionCommitProof = $createdCommitProof
        pendingTechnicalMaterialization = $pendingTechnicalMaterialization
        immediatelyBeforePush = [ordered]@{
            commitProof = $beforePushProof
            remoteAuthoritySha256 = `
                [string]$remoteIdentityBeforePush.authoritySha256
            localMain = $localMainBeforePush
            remoteTrackingMain = $trackingBeforePush
            liveRemoteMain = $liveBeforePush
            localTag = $localTagBeforePush
            remoteTagAbsent = $true
        }
        operations = @($operations)
        push = [ordered]@{
            remote = $remote
            remoteAuthoritySha256 = [string]$remoteIdentity.authoritySha256
            atomic = $true
            force = $false
            branchRefspec = $branchRefspec
            tagRefspec = $tagRefspec
            exitCode = [long]$pushResult.ExitCode
        }
        postPromotionAudit = $audit
        automaticCommitPerformed = $true
        automaticTagPerformed = $true
        automaticFastForwardPerformed = $true
        automaticPushPerformed = $true
        partialMutationPossible = $false
        technicalExecutionRepeated = $false
        authorApproved = $true
        selfApproved = $false
    }
}

function Test-GeoCeDGCloseoutPostPromotionAudit {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$CloseoutCommit,
        [Parameter(Mandatory)] [string]$PolicyPath,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [string]$ReadinessReceiptPath,
        [Parameter(Mandatory)] [string]$TechnicalCampaignPath,
        [Parameter(Mandatory)] [string]$AuthorApprovalPath
    )
    $CloseoutMode = ConvertTo-GeoCeDGCloseoutMode $CloseoutMode
    $root = [IO.Path]::GetFullPath($RepositoryRoot)
    $technical = Resolve-GeoCeDGPhaseLifecycleCommit $root $TechnicalCommit
    $closeout = Resolve-GeoCeDGPhaseLifecycleCommit $root $CloseoutCommit
    Assert-GeoCeDGCloseoutWorkflow ($technical -cne $closeout) `
        'Post-promotion audit requires distinct exact T and C commits.'
    $readiness = Read-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $root `
        -TechnicalCommit $technical -PolicyPath $PolicyPath `
        -ReadinessReceiptPath $ReadinessReceiptPath
    $policyContext = $readiness.policyContext
    $plan = Get-GeoCeDGCloseoutPlan $root $technical $policyContext
    $approval = Read-GeoCeDGExplicitAuthorApproval -Path $AuthorApprovalPath `
        -Phase ([string]$policyContext.policy.phase) `
        -TechnicalCommit $technical -CloseoutMode $CloseoutMode
    $campaign = Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
        -RepositoryRoot $root -TechnicalCommit $technical `
        -PolicyContext $policyContext `
        -ReadinessReceiptSha256 $readiness.sha256 `
        -AcceptancePlanSha256 ([string]$readiness.receipt.acceptancePlanSha256) `
        -TechnicalCampaignPath $TechnicalCampaignPath

    $line = (Invoke-GeoCeDGCloseoutGitText $root `
        @('rev-list', '--parents', '-n', '1', $closeout)).Text
    $parents = @($line -split ' ')
    Assert-GeoCeDGCloseoutWorkflow ($parents.Count -eq 2 -and
        $parents[0] -ceq $closeout -and $parents[1] -ceq $technical) `
        'C must be the direct, single-parent status-only child of exact T.'
    $commitMessage = (Invoke-GeoCeDGCloseoutGitText $root `
        @('show', '-s', '--format=%B', $closeout)).Text
    Assert-GeoCeDGCloseoutWorkflow ($commitMessage -ceq
        [string]$policyContext.policy.promotion.closeoutCommitMessage) `
        'Closeout commit message differs from frozen policy.'
    $history = Assert-GeoCeDGCloseoutLinearPromotionHistory $root `
        ([string]$readiness.receipt.promotion.anchorCommit) $technical $closeout

    $policyAtCloseout = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout `
        $policyContext.policyPath
    Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 $policyAtCloseout) -ceq
        [string]$policyContext.policyBlobSha256) `
        'C changed the frozen schema-v2 closeout policy reviewed at T.'
    $treeProof = Assert-GeoCeDGRepositoryTreeDelta -RepositoryRoot $root `
        -ReviewedCommit $technical -CloseoutCommit $closeout `
        -AllowedPaths $plan.allPaths
    foreach ($path in $plan.statusPaths) {
        $actual = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout $path
        Assert-GeoCeDGCloseoutWorkflow ((Get-GeoCeDGCloseoutRawSha256 $actual) -ceq
            (Get-GeoCeDGCloseoutRawSha256 ([byte[]]$plan.expectedBytes[$path]))) `
            "Published status-only content differs from frozen policy: $path"
    }
    Assert-GeoCeDGPhaseCloseoutPathModes -RepositoryRoot $root `
        -ReviewedTechnicalCommit $technical -ExistingPaths $plan.statusPaths `
        -RecordPath $plan.recordPath -CloseoutCommit $closeout

    $actualRecordBytes = Get-GeoCeDGPhaseLifecycleBlobBytes $root $closeout `
        $plan.recordPath
    $actualRecord = ConvertFrom-GeoCeDGPhaseLifecycleJson $actualRecordBytes `
        'published schema-v2 closeout record'
    $expectedRecord = New-GeoCeDGCloseoutDecisionRecord -PolicyContext $policyContext `
        -TechnicalCommit $technical -CloseoutMode $CloseoutMode `
        -Readiness $readiness -Approval $approval -Campaign $campaign `
        -ReconstructHistoricalPreparationClosure
    $expectedRecordBytes = ConvertTo-GeoCeDGCloseoutJsonBytes $expectedRecord
    Assert-GeoCeDGCloseoutWorkflow ($actualRecordBytes.Length -eq
        $expectedRecordBytes.Length -and
        [Convert]::ToBase64String($actualRecordBytes) -ceq
        [Convert]::ToBase64String($expectedRecordBytes)) `
        'Published closeout decision-record bytes differ from the canonical expected serialization.'
    Assert-GeoCeDGCloseoutDecisionRecord $actualRecord $expectedRecord

    $branch = [string]$policyContext.policy.promotion.branch
    $remote = [string]$policyContext.policy.promotion.remote
    $head = Get-GeoCeDGCloseoutExactRef $root 'HEAD' 'Post-promotion HEAD'
    $localMain = Get-GeoCeDGCloseoutExactRef $root "refs/heads/$branch" `
        'Post-promotion local main'
    $tracking = Get-GeoCeDGCloseoutExactRef $root "refs/remotes/$remote/$branch" `
        'Post-promotion remote-tracking main'
    $live = Get-GeoCeDGCloseoutLiveBranchRef $root $remote $branch
    Assert-GeoCeDGCloseoutWorkflow ($head -ceq $closeout -and
        $localMain -ceq $closeout -and $tracking -ceq $closeout -and
        $live -ceq $closeout) `
        'HEAD, main, origin/main, and live remote main must all equal exact C.'
    $materialization = Assert-GeoCeDGWorktreeMaterialization `
        -RepositoryRoot $root -ExpectedCommit $closeout
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $root
    Assert-GeoCeDGCloseoutWorkflow ($status.Staged.Count -eq 0 -and
        $status.Unstaged.Count -eq 0 -and $status.Untracked.Count -eq 0) `
        'Post-promotion worktree and index are not clean.'
    $tag = Get-GeoCeDGCloseoutAnnotatedTagAuthority -RepositoryRoot $root `
        -Remote $remote -TagName ([string]$policyContext.policy.promotion.tagName) `
        -ExpectedMessage ([string]$policyContext.policy.promotion.tagMessage) `
        -CloseoutCommit $closeout

    return [pscustomobject][ordered]@{
        schemaVersion = 1
        kind = 'GEOCEDG_CLOSEOUT_POST_PROMOTION_AUDIT'
        state = 'POST_PROMOTION_AUDIT_PASSED'
        phase = [string]$policyContext.policy.phase
        closeoutMode = $CloseoutMode
        reviewedTechnicalCommit = $technical
        closeoutCommit = $closeout
        readinessReceiptSha256 = [string]$readiness.sha256
        technicalEvidence = [ordered]@{
            rootFullResultSha256 = [string]$campaign.rootResultSha256
            evidenceManifestSha256 = `
                [string]$campaign.envelope.evidenceManifestSha256
            claimAuthorities = @($campaign.claims | ForEach-Object {
                    [ordered]@{
                        level = [string]$_.level
                        fulfillment = [string]$_.fulfillment
                        claimSha256 = [string]$_.claimSha256
                    }
                })
            attributedTo = $technical
            repeatedAfterCloseout = $false
        }
        physicalInputClosureAtPreparation = `
            $actualRecord.evidence.physicalInputClosureAtPreparation
        repositoryIdentity = $treeProof
        promotionHistory = $history
        tag = $tag
        publishedRefs = [ordered]@{
            head = $head
            main = $localMain
            originMain = $tracking
            liveRemoteMain = $live
        }
        promotionRemoteIdentity = $readiness.receipt.repository.remoteIdentity
        worktree = [ordered]@{
            clean = $true
            indexClean = $true
            trackedSha256 = [string]$materialization.trackedSha256
        }
        guarantees = [ordered]@{
            directStatusOnlyChild = $true
            closeoutCommitMessageExact = $true
            executableProductTestVerifierBuildToleranceReferenceInputsUnchanged = $true
            annotatedTagExact = $true
            promotionFastForwardFromReadinessAnchor = $true
            promotionHistoryAuditScope = 'OBSERVABLE_REFS_AND_READINESS_ANCHOR_TO_CLOSEOUT_ANCESTRY'
            observableForcePushOrUnexpectedMergeDetected = $false
            historicalRemoteReflogAudited = $false
            approvedTechnicalCommitSubstituted = $false
            evidenceReattributedToCloseoutCommit = $false
        }
        authorApproved = $true
        selfApproved = $false
    }
}

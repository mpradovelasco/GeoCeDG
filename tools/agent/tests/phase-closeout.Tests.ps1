#requires -Version 7.2
[CmdletBinding()]
param(
    [string]$HelperPath = (Join-Path $PSScriptRoot '../closeout-workflow.ps1'),
    [string]$LogDirectory = (Join-Path ([IO.Path]::GetTempPath()) `
        'geocedg-phase-closeout-tests')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

# Fake-first operational tests.  Every mutable Git authority is a disposable
# local repository with a local bare origin.  No Gradle, Java, product runtime,
# network service, real remote, author decision, or real repository ref is used.
$HelperPath = (Resolve-Path -LiteralPath $HelperPath).Path
$AgentRoot = Split-Path -Parent $HelperPath
$CliPath = Join-Path $AgentRoot 'phase-closeout.ps1'
$LifecyclePath = Join-Path $AgentRoot 'phase-lifecycle.ps1'
$IdentityPath = Join-Path $AgentRoot 'repository-input-identity.ps1'
$LegacyCloseoutPath = Join-Path $AgentRoot 'verify-phase-author-closeout.ps1'
$LogDirectory = [IO.Path]::GetFullPath($LogDirectory)
$PwshCommand = Join-Path $PSHOME $(if ($IsWindows) { 'pwsh.exe' } else { 'pwsh' })
foreach ($required in @($HelperPath, $CliPath, $LifecyclePath, $IdentityPath,
        $LegacyCloseoutPath, $PwshCommand)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required phase-closeout test input is missing: $required"
    }
}
. $HelperPath

$RunId = [guid]::NewGuid().ToString('N')
$RunRoot = Join-Path ([IO.Path]::GetTempPath()) ("geocedg-closeout-$RunId")
$EvidenceRoot = Join-Path $LogDirectory $RunId
$EmptyHooks = Join-Path $RunRoot 'empty-hooks'
[void][IO.Directory]::CreateDirectory($RunRoot)
[void][IO.Directory]::CreateDirectory($EvidenceRoot)
[void][IO.Directory]::CreateDirectory($EmptyHooks)
$Results = [Collections.Generic.List[object]]::new()
$CaseNumber = 0

function Assert-Case {
    param(
        [Parameter(Mandatory)] [bool]$Condition,
        [Parameter(Mandatory)] [string]$Message
    )
    if (-not $Condition) { throw "TEST FAILURE: $Message" }
}

function Assert-Throws {
    param(
        [Parameter(Mandatory)] [scriptblock]$Action,
        [Parameter(Mandatory)] [string]$Pattern,
        [Parameter(Mandatory)] [string]$Message
    )
    try { & $Action | Out-Null } catch {
        Assert-Case ($_.Exception.Message -match $Pattern) `
            "$Message failed for the wrong reason: $($_.Exception.Message)"
        return $_.Exception.Message
    }
    throw "TEST FAILURE: $Message unexpectedly succeeded."
}

function Write-Text {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [AllowEmptyString()] [string]$Text
    )
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    [IO.File]::WriteAllText($Path, $Text, [Text.UTF8Encoding]::new($false))
}

function Write-Json {
    param(
        [Parameter(Mandatory)] [string]$Path,
        [Parameter(Mandatory)] [object]$Value
    )
    Write-Text $Path (($Value | ConvertTo-Json -Depth 100).Replace("`r`n", "`n") + "`n")
}

function Read-Json {
    param([Parameter(Mandatory)] [string]$Path)
    return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json -Depth 100
}

function Get-BytesSha256 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [byte[]]$Bytes)
    return [Convert]::ToHexString(
        [Security.Cryptography.SHA256]::HashData($Bytes)).ToLowerInvariant()
}

function Get-FileSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    return Get-BytesSha256 ([IO.File]::ReadAllBytes($Path))
}

function Invoke-WithFixtureGitEnvironment {
    param(
        [Parameter(Mandatory)] [Collections.IDictionary]$Variables,
        [Parameter(Mandatory)] [scriptblock]$Action
    )
    $names = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::Ordinal)
    foreach ($name in @([Environment]::GetEnvironmentVariables(
                    [EnvironmentVariableTarget]::Process).Keys |
                ForEach-Object { [string]$_ } | Where-Object {
                    $_ -cmatch '^GIT_CONFIG_(?:COUNT|KEY_[0-9]+|VALUE_[0-9]+)$'
                })) {
        [void]$names.Add($name)
    }
    foreach ($name in @($Variables.Keys | ForEach-Object { [string]$_ })) {
        [void]$names.Add($name)
    }
    $saved = [ordered]@{}
    foreach ($name in $names) {
        $saved[$name] = [Environment]::GetEnvironmentVariable(
            $name, [EnvironmentVariableTarget]::Process)
        # On newer .NET hosts SetEnvironmentVariable(name, $null, Process)
        # can leave an empty-but-present variable. Git still interprets that as
        # an authority override, so remove the provider entry exactly.
        Remove-Item -LiteralPath "Env:$name" -ErrorAction SilentlyContinue
    }
    try {
        foreach ($entry in $Variables.GetEnumerator()) {
            [Environment]::SetEnvironmentVariable([string]$entry.Key,
                [string]$entry.Value, [EnvironmentVariableTarget]::Process)
        }
        & $Action
    } finally {
        $restoreNames = [Collections.Generic.HashSet[string]]::new(
            [StringComparer]::Ordinal)
        foreach ($name in @([Environment]::GetEnvironmentVariables(
                        [EnvironmentVariableTarget]::Process).Keys |
                    ForEach-Object { [string]$_ } | Where-Object {
                        $_ -cmatch '^GIT_CONFIG_(?:COUNT|KEY_[0-9]+|VALUE_[0-9]+)$'
                    })) {
            [void]$restoreNames.Add($name)
        }
        foreach ($name in $names) { [void]$restoreNames.Add($name) }
        foreach ($name in $restoreNames) {
            Remove-Item -LiteralPath "Env:$name" -ErrorAction SilentlyContinue
        }
        foreach ($entry in $saved.GetEnumerator()) {
            if ($null -eq $entry.Value) {
                Remove-Item -LiteralPath "Env:$([string]$entry.Key)" `
                    -ErrorAction SilentlyContinue
            } else {
                [Environment]::SetEnvironmentVariable([string]$entry.Key,
                    [string]$entry.Value, [EnvironmentVariableTarget]::Process)
            }
        }
    }
}

function Invoke-RawGit {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments,
        [switch]$AllowFailure
    )
    $savedAuthorDate = $env:GIT_AUTHOR_DATE
    $savedCommitterDate = $env:GIT_COMMITTER_DATE
    $env:GIT_AUTHOR_DATE = '2001-02-03T04:05:06Z'
    $env:GIT_COMMITTER_DATE = '2001-02-03T04:05:06Z'
    try {
        $output = @(& git --no-optional-locks -c "core.hooksPath=$EmptyHooks" `
            -c commit.gpgSign=false -c tag.gpgSign=false `
            -c user.name='GeoCeDG closeout fixture' `
            -c user.email='fixture@example.invalid' @Arguments 2>&1 |
            ForEach-Object { $_.ToString() })
        $code = $LASTEXITCODE
    } finally {
        $env:GIT_AUTHOR_DATE = $savedAuthorDate
        $env:GIT_COMMITTER_DATE = $savedCommitterDate
    }
    if (-not $AllowFailure -and $code -ne 0) {
        throw "Fixture git failed ($code): git $($Arguments -join ' '): $($output -join ' ')"
    }
    return [pscustomobject]@{ ExitCode = [int]$code; Output = @($output) }
}

function Invoke-FixtureGit {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [switch]$AllowFailure
    )
    return Invoke-RawGit -Arguments (@('-C', $Root) + $Arguments) `
        -AllowFailure:$AllowFailure
}

function Get-FixtureGitText {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string[]]$Arguments
    )
    return ((Invoke-FixtureGit $Root $Arguments).Output -join "`n").Trim()
}

function Commit-Fixture {
    param(
        [Parameter(Mandatory)] [string]$Root,
        [Parameter(Mandatory)] [string]$Message,
        [switch]$AllowEmpty
    )
    [void](Invoke-FixtureGit $Root @('add', '--all'))
    $arguments = @('commit', '--quiet', '-m', $Message)
    if ($AllowEmpty) { $arguments = @('commit', '--quiet', '--allow-empty', '-m', $Message) }
    [void](Invoke-FixtureGit $Root $arguments)
    return Get-FixtureGitText $Root @('rev-parse', 'HEAD')
}

function New-Policy {
    param(
        [string]$Variant = 'VALID',
        [string]$TagName = 'geocedg-fixture-pass'
    )
    $requiredLevels = @('PHASE', 'COMPOSED', 'FULL')
    $supportedModes = @('VERIFIED', 'AUTHOR_OPERATED')
    $repairRequired = $false
    $replacementPath = 'docs/status.md'
    $verificationClass = 'OPERATIONAL_VERIFICATION_INFRASTRUCTURE'
    $planFrozen = $true
    $plannedAcceptanceLevel = 'FULL'
    $globalInfrastructureChange = $true
    $defaultCloseoutMode = 'AUTHOR_OPERATED'
    [object]$tagValue = $TagName
    [object]$replacementAfter = 'STATE = CLOSED'
    switch ($Variant) {
        'MISSING_AUTHOR_ROUTE' { $supportedModes = @('VERIFIED') }
        'REDUCED_COVERAGE' { $requiredLevels = @('FULL') }
        'REPAIR_REQUIRED' { $repairRequired = $true }
        'FORBIDDEN_PATH' { $replacementPath = 'tools/phase-verifier.ps1' }
        'NON_OPERATIONAL_SPEC_PATH' {
            $replacementPath = 'geocedg/specs/curves/semantic-spline-2d.md'
        }
        'VALIDATION_MARKDOWN_PATH' {
            $replacementPath = 'geocedg/validation/operations/forbidden-status.md'
        }
        'INVALID_TAG' { $tagValue = 'bad..tag' }
        'NUMERIC_TAG' { $tagValue = [long]123 }
        'NULL_REPLACEMENT_AFTER' { $replacementAfter = $null }
        'UNFROZEN_VERIFICATION_PLAN' { $planFrozen = $false }
        'UNAUTHORIZED_FULL_ESCALATION' {
            $verificationClass = 'BOUNDED_PHASE'
            $plannedAcceptanceLevel = 'PHASE'
            $globalInfrastructureChange = $false
        }
        'INVALID_SINGLE_INTEGRATOR_DEFAULT' { $defaultCloseoutMode = 'VERIFIED' }
        'VALID' { }
        default { throw "Unknown fixture policy variant: $Variant" }
    }
    return [ordered]@{
        schemaVersion = 2
        phase = 'VERIFICATION-INFRASTRUCTURE'
        verificationPlan = [ordered]@{
            verificationClass = $verificationClass
            frozenAtPhaseStart = $planFrozen
            plannedAcceptanceLevel = $plannedAcceptanceLevel
            integratedCoverageAdditional = $false
            globalInfrastructureChange = $globalInfrastructureChange
            focusedEvidenceRequired = $true
            escalationRequestKind = 'VERIFICATION_ESCALATION_REQUEST'
        }
        singleIntegratorDefaults = [ordered]@{
            verificationClass = 'BOUNDED_PHASE'
            closeoutMode = $defaultCloseoutMode
            closeoutModeRequiresExplicitSelection = $true
        }
        technicalEvidence = [ordered]@{
            requiredLevels = @($requiredLevels)
            campaignStrategy = 'SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS'
            physicalCampaignLevel = 'FULL'
            requiredClaims = @(
                [ordered]@{
                    level = 'PHASE'
                    fulfillment = 'NESTED_EXECUTED'
                    orchestratorPath = 'tools/agent/verify-operational.ps1'
                    verifierPath = 'tools/agent/verify-verification-infrastructure.ps1'
                    evidencePath = 'operational/verification-infrastructure/verification-infrastructure.json'
                    expectedState = 'PASS_FAKE_FIRST_OPERATIONAL_ONLY'
                },
                [ordered]@{
                    level = 'COMPOSED'
                    fulfillment = 'NORMATIVE_SUBSUMPTION'
                    sourceLevel = 'FULL'
                    canonicalSelection = 'UNFILTERED_SHARED_AND_DESKTOP'
                },
                [ordered]@{
                    level = 'FULL'
                    fulfillment = 'ROOT_PHYSICAL_CAMPAIGN'
                    sourceLevel = 'FULL'
                    canonicalSelection = 'UNFILTERED_SHARED_AND_DESKTOP'
                }
            )
            complements = @()
            fullRequiresCleanBuild = $true
            lifecycleRepairRequired = $repairRequired
        }
        promotion = [ordered]@{
            branch = 'main'
            remote = 'origin'
            tagName = $tagValue
            tagMessage = 'GeoCeDG fixture phase accepted'
            closeoutCommitMessage = 'Close GeoCeDG fixture phase'
        }
        closeout = [ordered]@{
            supportedModes = @($supportedModes)
            recordPath = 'geocedg/validation/operations/fixture-closeout.json'
            literalReplacements = @(
                [ordered]@{
                    path = $replacementPath
                    before = 'STATE = AUTHOR_REVIEW_READY'
                    after = $replacementAfter
                    occurrences = 1
                }
            )
            canonicalLfHashManifests = @()
        }
    }
}

function New-WorkflowFixture {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [string]$PolicyVariant = 'VALID',
        [switch]$MergeTechnicalCommit,
        [switch]$PreexistingRecord,
        [switch]$IgnoredRecord,
        [switch]$IdentRecord,
        [switch]$FilteredRecord
    )
    $safeName = ($Name -replace '[^A-Za-z0-9_.-]', '-')
    $container = Join-Path $RunRoot $safeName
    $origin = Join-Path $container 'origin.git'
    $root = Join-Path $container 'work'
    [void][IO.Directory]::CreateDirectory($container)
    [void](Invoke-RawGit @('init', '--bare', '--quiet', '--initial-branch=main', $origin))
    [void](Invoke-RawGit @('clone', '--quiet', $origin, $root))
    [void](Invoke-FixtureGit $root @('config', 'core.autocrlf', 'false'))

    Write-Text (Join-Path $root '.gitignore') "artifacts/`n"
    Write-Text (Join-Path $root 'source/product.java') `
        "final class Product { int value = 1; }`n"
    Write-Text (Join-Path $root 'gradlew.bat') `
        "@echo off`r`necho Fake-first fixture wrapper must never execute.`r`nexit /b 99`r`n"
    foreach ($authorityPath in @('tools/agent/verify.ps1',
            'tools/agent/verify-baseline.ps1',
            'tools/agent/verify-operational.ps1',
            'tools/agent/verify-verification-infrastructure.ps1',
            'tools/agent/verification-runtime.psm1')) {
        Write-Text (Join-Path $root $authorityPath) `
            "# frozen fixture authority: $authorityPath`n"
    }
    Write-Text (Join-Path $root 'tools/phase-verifier.ps1') "# fixture verifier`n"
    Write-Text (Join-Path $root 'tests/product.Tests.txt') "fixture test authority`n"
    Write-Text (Join-Path $root 'docs/status.md') `
        "# Fixture phase`n`nSTATE = AUTHOR_REVIEW_READY`n"
    Write-Text (Join-Path $root 'geocedg/validation/operations/.keep') "fixture parent`n"
    $m0 = Commit-Fixture $root 'Fixture promotion anchor M0'
    [void](Invoke-FixtureGit $root @('push', '--quiet', '-u', 'origin', 'main'))

    $branch = "technical/$safeName"
    [void](Invoke-FixtureGit $root @('switch', '--quiet', '-c', $branch))
    $policyPath = 'geocedg/specs/operations/fixture-closeout-policy.json'
    $recordPath = 'geocedg/validation/operations/fixture-closeout.json'
    Write-Json (Join-Path $root $policyPath) (New-Policy -Variant $PolicyVariant)
    Write-Text (Join-Path $root 'docs/technical-change.md') `
        "Operational fixture technical implementation.`n"
    if ($PreexistingRecord) {
        Write-Json (Join-Path $root $recordPath) ([ordered]@{
                schemaVersion = 0
                kind = 'PREEXISTING_FORBIDDEN_RECORD'
            })
    }
    if ($IgnoredRecord) {
        [IO.File]::AppendAllText((Join-Path $root '.gitignore'),
            "$recordPath`n", [Text.UTF8Encoding]::new($false))
    }
    if ($IdentRecord -or $FilteredRecord) {
        $attribute = if ($IdentRecord) { 'ident' } else { 'filter=fixture-clean' }
        Write-Text (Join-Path $root '.gitattributes') "$recordPath $attribute`n"
    }
    if ($FilteredRecord) {
        [void](Invoke-FixtureGit $root @('config', 'filter.fixture-clean.clean', 'cat'))
        [void](Invoke-FixtureGit $root @('config', 'filter.fixture-clean.smudge', 'cat'))
        [void](Invoke-FixtureGit $root @('config', 'filter.fixture-clean.required', 'true'))
    }
    $technical = Commit-Fixture $root 'Fixture technical commit T'
    if ($MergeTechnicalCommit) {
        $sideBranch = "technical-side/$safeName"
        [void](Invoke-FixtureGit $root @('switch', '--quiet', '-c', $sideBranch, $m0))
        [void](Commit-Fixture $root 'Unexpected technical side commit' -AllowEmpty)
        [void](Invoke-FixtureGit $root @('switch', '--quiet', $branch))
        [void](Invoke-FixtureGit $root @('merge', '--quiet', '--no-ff', '-m',
            'Unexpected merged technical candidate', $sideBranch))
        $technical = Get-FixtureGitText $root @('rev-parse', 'HEAD')
    }
    return [pscustomobject][ordered]@{
        Name = $safeName
        Root = $root
        Origin = $origin
        Branch = $branch
        MainAnchor = $m0
        TechnicalCommit = $technical
        PolicyPath = $policyPath
        StatusPath = 'docs/status.md'
        RecordPath = $recordPath
        TagName = 'geocedg-fixture-pass'
        TagMessage = 'GeoCeDG fixture phase accepted'
        CloseoutMessage = 'Close GeoCeDG fixture phase'
        ExternalRoot = (Join-Path $container 'external-evidence')
    }
}

function Write-ReadinessReceipt {
    param([Parameter(Mandatory)] [object]$Fixture)
    [void][IO.Directory]::CreateDirectory($Fixture.ExternalRoot)
    $receipt = New-GeoCeDGCloseoutReadinessReceipt `
        -RepositoryRoot $Fixture.Root -TechnicalCommit $Fixture.TechnicalCommit `
        -PolicyPath $Fixture.PolicyPath
    $path = Join-Path $Fixture.ExternalRoot 'readiness.json'
    Write-Json $path $receipt
    return [pscustomobject]@{
        Path = $path
        Receipt = Read-Json $path
        Sha256 = Get-FileSha256 $path
    }
}

function Get-CleanTechnicalInputInventory {
    param([Parameter(Mandatory)] [object]$Fixture)
    $paths = @((Invoke-FixtureGit $Fixture.Root @('ls-tree', '-r', '--name-only',
                $Fixture.TechnicalCommit)).Output | Sort-Object -Unique -CaseSensitive)
    $records = [Collections.Generic.List[object]]::new()
    [long]$totalBytes = 0
    foreach ($path in $paths) {
        $physicalPath = Join-Path $Fixture.Root ([string]$path)
        Assert-Case (Test-Path -LiteralPath $physicalPath -PathType Leaf) `
            "Exact T path has no clean physical materialization: $path"
        $bytes = [IO.File]::ReadAllBytes($physicalPath)
        $totalBytes += $bytes.Length
        $records.Add([ordered]@{
                path = [string]$path
                exists = $true
                bytes = [long]$bytes.Length
                sha256 = Get-BytesSha256 $bytes
            })
    }
    $canonicalJson = ConvertTo-Json -InputObject ([object[]]$records) `
        -Depth 10 -Compress
    return [pscustomobject]@{
        Records = @($records)
        Sha256 = Get-BytesSha256 `
            ([Text.UTF8Encoding]::new($false).GetBytes($canonicalJson))
        Files = [long]$records.Count
        Bytes = $totalBytes
    }
}

function New-CanonicalReceipt {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [ValidateSet('COMPOSED', 'FULL')]
        [string]$Level,
        [Parameter(Mandatory)] [string]$ArchiveRoot
    )
    $commitIndex = Get-GeoCeDGPhaseCommitIndexAuthority `
        -RepositoryRoot $Fixture.Root -Commit $Fixture.TechnicalCommit
    $prefix = $Level.ToLowerInvariant()
    $archive = Join-Path $ArchiveRoot "$prefix-archive"
    [void][IO.Directory]::CreateDirectory($archive)
    $inputInventory = Get-CleanTechnicalInputInventory $Fixture
    $auditPath = Join-Path $archive 'input-inventory.json'
    Write-Json $auditPath $inputInventory.Records
    $fixtureGradleHome = Join-Path $archive 'fixture-gradle-user-home'
    $selectedJvmPath = Join-Path $archive 'fixture-jdk/bin/java.exe'
    Write-Text $selectedJvmPath 'FAKE-FIRST selected Test JVM; never executed.'
    $selectedJvmSha256 = Get-FileSha256 $selectedJvmPath
    $externalConfiguration = @(
        [ordered]@{
            path = [IO.Path]::GetFullPath((Join-Path $fixtureGradleHome 'init.d'))
            kind = 'absent'
            sha256 = $null
        },
        [ordered]@{
            path = [IO.Path]::GetFullPath($selectedJvmPath)
            kind = 'file'
            sha256 = $selectedJvmSha256
        }
    )
    $externalConfigurationPath = Join-Path $archive 'external-configuration.json'
    Write-Json $externalConfigurationPath $externalConfiguration
    $externalConfigurationJson = ConvertTo-Json -InputObject `
        ([object[]]$externalConfiguration) -Depth 10 -Compress
    $identity = [ordered]@{
        head = $Fixture.TechnicalCommit
        indexSha256 = [string]$commitIndex.IndexSha256
        statusSha256 = Get-BytesSha256 ([byte[]]::new(0))
        rawTreeSha256 = [string]$inputInventory.Sha256
        rawFiles = [long]$inputInventory.Files
        rawBytes = [long]$inputInventory.Bytes
        environmentSha256 = Get-BytesSha256 `
            ([Text.UTF8Encoding]::new($false).GetBytes('FAKE_FIRST_ENVIRONMENT'))
        externalConfigurationSha256 = Get-BytesSha256 `
            ([Text.UTF8Encoding]::new($false).GetBytes($externalConfigurationJson))
        gradleUserHome = $fixtureGradleHome
    }
    $identityJson = $identity | ConvertTo-Json -Depth 20 -Compress
    $fingerprint = Get-BytesSha256 `
        ([Text.UTF8Encoding]::new($false).GetBytes($identityJson))
    $nativeRuns = [Collections.Generic.List[object]]::new()
    $auditArtifacts = [Collections.Generic.List[object]]::new()
    $auditArtifacts.Add([ordered]@{
            path = $auditPath
            sha256 = Get-FileSha256 $auditPath
        })
    $auditArtifacts.Add([ordered]@{
            path = $externalConfigurationPath
            sha256 = Get-FileSha256 $externalConfigurationPath
        })
    $wrapperPath = [IO.Path]::GetFullPath((Join-Path $Fixture.Root 'gradlew.bat'))
    $versionArguments = @('--version', '--no-daemon', '--no-problems-report')
    $toolchainArguments = @('-q', 'javaToolchains', '--no-daemon',
        '--no-configuration-cache', '--no-problems-report', '--console=plain',
        '-Dorg.gradle.java.installations.auto-download=false')
    $moduleArguments = [ordered]@{}
    foreach ($module in @('shared', 'desktop')) {
        $baseArguments = @($(if ($module -ceq 'shared') {
                    ':shared:common-jre:test'
                } else { ':desktop:desktop:test' }))
        $moduleStyleTasks = if ($module -ceq 'shared') {
            @(':shared:common:checkstyleMain',
                ':shared:common-jre:checkstyleTest')
        } else {
            @(':desktop:desktop:checkstyleMain',
                ':desktop:desktop:checkstyleTest')
        }
        foreach ($styleTask in $moduleStyleTasks) {
            $baseArguments += [string]$styleTask
        }
        $baseArguments += @('--info', '--profile', '--console=plain',
            '--no-problems-report',
            '-Dorg.gradle.java.installations.auto-download=false')
        $incrementalArguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments `
                -Arguments $baseArguments)
        $moduleArguments[$module] = @($incrementalArguments | Where-Object {
                [string]$_ -cne '--build-cache'
            }) + @('--no-build-cache', '--rerun-tasks')
    }
    $runSpecs = @(
        [ordered]@{ name = 'gradle-version'; arguments = $versionArguments;
            body = "Gradle 9.4.1 fake-first wrapper identity`n" },
        [ordered]@{ name = 'java-toolchains'; arguments = $toolchainArguments;
            body = "Location: $([IO.Path]::GetDirectoryName([IO.Path]::GetDirectoryName($selectedJvmPath)))`n" },
        [ordered]@{ name = 'shared-gradle'; arguments = $moduleArguments.shared;
            body = "Starting process 'Gradle Test Executor 1'. Command: `"$selectedJvmPath`" -Dfixture=shared`n" },
        [ordered]@{ name = 'desktop-gradle'; arguments = $moduleArguments.desktop;
            body = "Starting process 'Gradle Test Executor 2'. Command: `"$selectedJvmPath`" -Dfixture=desktop`n" }
    )
    for ($runIndex = 0; $runIndex -lt $runSpecs.Count; $runIndex++) {
        $spec = $runSpecs[$runIndex]
        $logPath = Join-Path $archive "$($spec.name).log"
        Write-Text $logPath ([string]$spec.body)
        $auditArtifacts.Add([ordered]@{
                path = $logPath
                sha256 = Get-FileSha256 $logPath
            })
        $nativeRuns.Add([ordered]@{
                file = $wrapperPath
                arguments = @($spec.arguments)
                workingDirectory = $Fixture.Root
                logPath = $logPath
                startedUtc = "2001-02-03T04:05:0$runIndex.0000000Z"
                finishedUtc = "2001-02-03T04:05:0$($runIndex + 1).0000000Z"
                elapsedSeconds = 1.0
                exitCode = 0
            })
    }
    $junit = [Collections.Generic.List[object]]::new()
    foreach ($module in @('shared', 'desktop')) {
        $class = "org.geocedg.fixture.$($module.Substring(0, 1).ToUpperInvariant())$($module.Substring(1))AcceptanceTest"
        $junitPath = Join-Path $archive "TEST-$class.xml"
        Write-Text $junitPath `
            "<testsuite name=`"$class`" tests=`"1`" failures=`"0`" errors=`"0`" skipped=`"0`"><testcase classname=`"$class`" name=`"acceptance`" /></testsuite>`n"
        $junit.Add([ordered]@{
                module = $module
                class = $class
                livePath = $(if ($module -ceq 'shared') {
                        "source/shared/common-jre/build/test-results/test/TEST-$class.xml"
                    } else {
                        "source/desktop/desktop/build/test-results/test/TEST-$class.xml"
                    })
                archivePath = $junitPath
                sha256 = Get-FileSha256 $junitPath
                tests = 1
                failures = 0
                errors = 0
                skipped = 0
                cases = @([ordered]@{
                        class = $class
                        name = 'acceptance'
                        status = 'PASS'
                    })
            })
    }
    $checkstyle = [Collections.Generic.List[object]]::new()
    $stylePaths = [ordered]@{
        ':shared:common:checkstyleMain' =
            'source/shared/common/build/reports/checkstyle/main.xml'
        ':shared:common-jre:checkstyleTest' =
            'source/shared/common-jre/build/reports/checkstyle/test.xml'
        ':desktop:desktop:checkstyleMain' =
            'source/desktop/desktop/build/reports/checkstyle/main.xml'
        ':desktop:desktop:checkstyleTest' =
            'source/desktop/desktop/build/reports/checkstyle/test.xml'
    }
    foreach ($styleEntry in $stylePaths.GetEnumerator()) {
        $task = [string]$styleEntry.Key
        $safeTask = $task.TrimStart(':').Replace(':', '-')
        $checkstylePath = Join-Path $archive "$safeTask.xml"
        Write-Text $checkstylePath '<checkstyle version="10.12" />'
        $checkstyle.Add([ordered]@{
                task = $task
                livePath = [string]$styleEntry.Value
                archivePath = $checkstylePath
                sha256 = Get-FileSha256 $checkstylePath
            })
    }
    $sharedSelection = if ($Level -ceq 'FULL') {
        [ordered]@{ task = ':shared:common-jre:test'; unfiltered = $true;
            filters = @() }
    } else {
        [ordered]@{ task = ':shared:common-jre:test'; unfiltered = $false;
            filters = @('org.geocedg.*',
                'org.geogebra.common.kernel.commands.RedefineTest',
                'org.geogebra.common.euclidian.DrawablesTest',
                'org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest') }
    }
    $desktopSelection = if ($Level -ceq 'FULL') {
        [ordered]@{ task = ':desktop:desktop:test'; unfiltered = $true;
            filters = @() }
    } else {
        [ordered]@{ task = ':desktop:desktop:test'; unfiltered = $false;
            filters = @('org.geocedg.*') }
    }
    return [ordered]@{
        schemaVersion = 1
        kind = 'CURRENT_RUN_BUILD_EVIDENCE'
        runId = '0123456789abcdef0123456789abcdef'
        level = $Level
        repositoryRoot = $Fixture.Root
        state = 'TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING'
        authorApproved = $false
        selfApproved = $false
        inputFingerprint = $fingerprint
        inputIdentity = $identity
        initialFingerprintSeconds = 0.0
        allowToolchainDownload = $false
        testResultReuseAcrossRuns = $false
        configurationCache = $false
        newTestParallelism = $false
        selections = [ordered]@{
            shared = $sharedSelection
            desktop = $desktopSelection
        }
        selectedTestJvms = [ordered]@{
            shared = @([ordered]@{
                    path = [IO.Path]::GetFullPath($selectedJvmPath)
                    sha256 = $selectedJvmSha256
                })
            desktop = @([ordered]@{
                    path = [IO.Path]::GetFullPath($selectedJvmPath)
                    sha256 = $selectedJvmSha256
                })
        }
        tasks = @(
            [ordered]@{ task = ':shared:common-jre:test'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':desktop:desktop:test'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':shared:common:checkstyleMain'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':shared:common-jre:checkstyleTest'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':desktop:desktop:checkstyleMain'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':desktop:desktop:checkstyleTest'; outcome = 'EXECUTED';
                headingOccurrences = 1; observedOutcomes = @('EXECUTED') },
            [ordered]@{ task = ':shared:canvas-base:classes'; outcome = 'UP-TO-DATE';
                headingOccurrences = 1; observedOutcomes = @('UP-TO-DATE') },
            [ordered]@{ task = ':shared:canvas-base:classes'; outcome = 'UP-TO-DATE';
                headingOccurrences = 1; observedOutcomes = @('UP-TO-DATE') }
        )
        auditArtifacts = @($auditArtifacts)
        junit = @($junit)
        checkstyle = @($checkstyle)
        nativeRuns = @($nativeRuns)
        tests = 2
        skippedUpstreamTests = 0
        sealedUtc = '2001-02-03T04:05:08.0000000Z'
    }
}

function New-TechnicalEvidenceCampaign {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [object]$Readiness
    )
    $root = Join-Path $Fixture.ExternalRoot 'campaign-mode-neutral'
    [void][IO.Directory]::CreateDirectory($root)
    $receiptName = 'full-canonical-receipt.json'
    $receiptPath = Join-Path $root $receiptName
    Write-Json $receiptPath (New-CanonicalReceipt -Fixture $Fixture -Level FULL `
            -ArchiveRoot $root)

    $phaseLog = Join-Path $root `
        'operational/verification-infrastructure/fixture-contract.log'
    Write-Text $phaseLog "Single FULL nested PHASE fake-first contract PASS`n"
    $phaseSummaryPath = Join-Path $root `
        'operational/verification-infrastructure/verification-infrastructure.json'
    Write-Json $phaseSummaryPath ([ordered]@{
            schemaVersion = 1
            state = 'PASS_FAKE_FIRST_OPERATIONAL_ONLY'
            fixtures = @([ordered]@{
                    fixture = 'phase-closeout.Tests.ps1'
                    command = $PwshCommand
                    arguments = @('-NoProfile', '-File', 'phase-closeout.Tests.ps1')
                    exitCode = 0
                    elapsedSeconds = 1.0
                    logPath = $phaseLog
                    evidenceKind = 'FAKE_FIRST_OPERATIONAL_CONTRACT'
                })
            productRuntimeExecuted = $false
            authorApproved = $false
            failure = $null
        })
    $preHeavy = Compare-GeoCeDGCloseoutPreHeavyExecutionPlan `
        -ReadinessReceipt $Readiness.Receipt -RequestedLevel FULL `
        -CleanGeneratedOutputs $true -IndependentBuilds $false
    $emptyHash = Get-BytesSha256 ([byte[]]::new(0))
    $phaseExecution = [pscustomobject][ordered]@{
        startedUtc = '2001-02-03T04:05:08.0000000Z'
        finishedUtc = '2001-02-03T04:05:09.0000000Z'
        exitCode = 0
        program = 'tools/agent/verify-operational.ps1'
        arguments = @($Readiness.Receipt.technicalCampaignPlan.
            coverageComparison.phaseIntegration.rootInvocation)
        beforeHead = $Fixture.TechnicalCommit
        afterHead = $Fixture.TechnicalCommit
        beforeStatusSha256 = $emptyHash
        afterStatusSha256 = $emptyHash
    }
    $baselineExecution = [pscustomobject][ordered]@{
        startedUtc = '2001-02-03T04:05:09.0000000Z'
        finishedUtc = '2001-02-03T04:05:10.0000000Z'
        exitCode = 0
        fullTests = $true
        canonicalReceiptPath = $receiptPath
    }
    $commonGatePlan = @($Readiness.Receipt.technicalCampaignPlan.commonGatePlan)
    $commonGateHash = [string]$Readiness.Receipt.technicalCampaignPlan.
        commonGatePlanSha256
    $commonGateCompletion = [pscustomobject][ordered]@{
        state = 'PASS'
        exitCode = 0
        completedUtc = '2001-02-03T04:05:11.0000000Z'
        expectedGateIds = $commonGatePlan
        actualGateIds = $commonGatePlan
        expectedGatePlanSha256 = $commonGateHash
        executedGatePlanSha256 = $commonGateHash
    }
    $envelope = New-GeoCeDGCloseoutSingleFullCampaignEnvelope `
        -RepositoryRoot $Fixture.Root -TechnicalCommit $Fixture.TechnicalCommit `
        -PolicyPath $Fixture.PolicyPath -ReadinessReceiptPath $Readiness.Path `
        -CampaignRoot $root -CanonicalReceiptPath $receiptPath `
        -PreHeavyPlanComparison $preHeavy -PhaseExecution $phaseExecution `
        -BaselineExecution $baselineExecution `
        -CommonGateCompletion $commonGateCompletion
    $result = [ordered]@{
        schemaVersion = 1
        level = 'FULL'
        repositoryCommit = $Fixture.TechnicalCommit
        phase = $null
        module = $null
        testFilters = @()
        state = 'TECHNICAL_GATES_PASSED_NOT_AUTHOR_APPROVAL'
        exitCode = 0
        independentBuilds = $false
        cleanGeneratedOutputs = $true
        dependencyCacheResetRequested = $false
        keepBuildOutputs = $false
        canonicalReceipt = $receiptName
        devEvidence = $null
        requestedEvidenceUse = 'FINAL_ACCEPTANCE'
        evidenceUse = 'FINAL_ACCEPTANCE'
        repositoryCohort = 'CLEAN_COMMIT'
        reviewedCandidate = $Fixture.TechnicalCommit
        closeoutMode = $null
        validatedCloseoutModes = @('VERIFIED', 'AUTHOR_OPERATED')
        acceptancePlanSha256 = [string]$Readiness.Receipt.acceptancePlanSha256
        readinessReceiptPath = $Readiness.Path
        readinessReceiptSha256 = $Readiness.Sha256
        closeoutConsumable = $true
        reason = 'READINESS_BOUND_TECHNICAL_GATES_PASSED'
        heavyCampaignStarted = $true
        technicalCampaign = $envelope
        startedUtc = '2001-02-03T04:05:06.0000000Z'
        finishedUtc = '2001-02-03T04:05:12.0000000Z'
        elapsedSeconds = 6.0
        requestedOptionalGates = @()
        failure = $null
        authorApproved = $false
        selfApproved = $false
    }
    $resultPath = Join-Path $root 'full-verification-result.json'
    Write-Json $resultPath $result
    return [pscustomobject]@{
        Path = $resultPath
        Paths = @($resultPath)
        FullPath = $resultPath
        FullReceiptPath = $receiptPath
        PhaseSummaryPath = $phaseSummaryPath
    }
}

function Rebuild-TechnicalEvidenceCampaignEnvelope {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Campaign
    )
    $rootResult = Read-Json $Campaign.Path
    $existing = $rootResult.technicalCampaign
    $plan = $Readiness.Receipt.technicalCampaignPlan
    $preHeavy = [pscustomobject][ordered]@{
        expectedPlan = $plan
        selectedPlan = $plan
        expectedPlanSha256 = [string]$Readiness.Receipt.technicalCampaignPlanSha256
        selectedPlanSha256 = [string]$Readiness.Receipt.technicalCampaignPlanSha256
        comparedBeforeHeavy = $true
        expectedEqualsSelected = $true
    }
    $baseline = $existing.executionConfirmations.baselineFull
    $baselineExecution = [pscustomobject][ordered]@{
        startedUtc = $baseline.startedUtc
        finishedUtc = $baseline.finishedUtc
        exitCode = [long]$baseline.exitCode
        fullTests = [bool]$baseline.fullTests
        canonicalReceiptPath = $Campaign.FullReceiptPath
    }
    $common = $existing.executionConfirmations.rootCommonGate
    $commonGateCompletion = [pscustomobject][ordered]@{
        state = [string]$common.state
        exitCode = [long]$common.exitCode
        completedUtc = $common.completedUtc
        expectedGateIds = @($common.expectedGateIds)
        actualGateIds = @($common.actualGateIds)
        expectedGatePlanSha256 = [string]$common.expectedGatePlanSha256
        executedGatePlanSha256 = [string]$common.executedGatePlanSha256
    }
    $rootResult.technicalCampaign = New-GeoCeDGCloseoutSingleFullCampaignEnvelope `
        -RepositoryRoot $Fixture.Root -TechnicalCommit $Fixture.TechnicalCommit `
        -PolicyPath $Fixture.PolicyPath -ReadinessReceiptPath $Readiness.Path `
        -CampaignRoot (Split-Path -Parent $Campaign.Path) `
        -CanonicalReceiptPath $Campaign.FullReceiptPath `
        -PreHeavyPlanComparison $preHeavy `
        -PhaseExecution $existing.claims[0].execution `
        -BaselineExecution $baselineExecution `
        -CommonGateCompletion $commonGateCompletion
    Write-Json $Campaign.Path $rootResult
}

function Assert-TechnicalEvidenceCampaignRejected {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Campaign,
        [Parameter(Mandatory)] [string]$Pattern,
        [Parameter(Mandatory)] [string]$Description
    )
    $policy = Read-GeoCeDGCloseoutWorkflowPolicy $Fixture.Root `
        $Fixture.TechnicalCommit $Fixture.PolicyPath
    [void](Assert-Throws {
            Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
                -RepositoryRoot $Fixture.Root `
                -TechnicalCommit $Fixture.TechnicalCommit `
                -PolicyContext $policy `
                -ReadinessReceiptSha256 $Readiness.Sha256 `
                -AcceptancePlanSha256 $Readiness.Receipt.acceptancePlanSha256 `
                -TechnicalCampaignPath $Campaign.Path
        } $Pattern $Description)
}

function Update-CanonicalReceiptFingerprint {
    param([Parameter(Mandatory)] [object]$Receipt)
    $identityJson = $Receipt.inputIdentity | ConvertTo-Json -Depth 20 -Compress
    $Receipt.inputFingerprint = Get-BytesSha256 `
        ([Text.UTF8Encoding]::new($false).GetBytes($identityJson))
}

function Get-CanonicalArraySha256 {
    param([Parameter(Mandatory)] [AllowEmptyCollection()] [object[]]$Value)
    $json = ConvertTo-Json -InputObject ([object[]]$Value) -Depth 20 -Compress
    return Get-BytesSha256 ([Text.UTF8Encoding]::new($false).GetBytes($json))
}

function Get-CanonicalAuditArtifact {
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [string]$FileName
    )
    $matches = @($Receipt.auditArtifacts | Where-Object {
            [IO.Path]::GetFileName([string]$_.path) -ceq $FileName
        })
    Assert-Case ($matches.Count -eq 1) `
        "Canonical fixture has no unique $FileName audit artifact"
    return $matches[0]
}

function Update-CanonicalAuditArtifactHash {
    param(
        [Parameter(Mandatory)] [object]$Receipt,
        [Parameter(Mandatory)] [string]$FileName
    )
    $artifact = Get-CanonicalAuditArtifact $Receipt $FileName
    $artifact.sha256 = Get-FileSha256 ([string]$artifact.path)
}

function Write-AuthorApproval {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode
    )
    $path = Join-Path $Fixture.ExternalRoot "approval-$($CloseoutMode.ToLowerInvariant()).json"
    Write-Json $path ([ordered]@{
            schemaVersion = 1
            kind = 'GEOCEDG_EXPLICIT_AUTHOR_APPROVAL'
            phase = 'VERIFICATION-INFRASTRUCTURE'
            reviewedTechnicalCommit = $Fixture.TechnicalCommit
            closeoutMode = $CloseoutMode
            decision = 'PASS_AUTHOR_APPROVED'
            authority = 'AUTHOR'
            selfApproved = $false
        })
    return $path
}

function Get-RepositorySnapshot {
    param([Parameter(Mandatory)] [object]$Fixture)
    $tags = Invoke-FixtureGit $Fixture.Root @('show-ref', '--tags') -AllowFailure
    $live = Get-FixtureGitText $Fixture.Root @('ls-remote', '--heads', 'origin',
        'refs/heads/main')
    return [ordered]@{
        head = Get-FixtureGitText $Fixture.Root @('rev-parse', 'HEAD')
        main = Get-FixtureGitText $Fixture.Root @('rev-parse', 'refs/heads/main')
        originMain = Get-FixtureGitText $Fixture.Root @('rev-parse', 'refs/remotes/origin/main')
        liveMain = ($live -split "`t")[0]
        tags = @($tags.Output)
    }
}

function New-AlternateBareRemote {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [string]$Name
    )
    $path = Join-Path (Split-Path -Parent $Fixture.Origin) "$Name.git"
    [void](Invoke-RawGit @('clone', '--bare', '--quiet', $Fixture.Origin, $path))
    return $path
}

function Assert-StructuredOperationPlan {
    param(
        [Parameter(Mandatory)] [AllowEmptyCollection()] [object[]]$Plan,
        [Parameter(Mandatory)] [int]$ExpectedCount,
        [Parameter(Mandatory)] [string]$Description
    )
    Assert-Case ($Plan.Count -eq $ExpectedCount) `
        "$Description operation count changed"
    for ($i = 0; $i -lt $Plan.Count; $i++) {
        $operation = $Plan[$i]
        Assert-Case ($operation -isnot [string]) `
            "$Description contains an executable command string"
        $propertyNames = if ($operation -is [Collections.IDictionary]) {
            @($operation.Keys | ForEach-Object { [string]$_ })
        } else { @($operation.PSObject.Properties.Name) }
        Assert-GeoCeDGPhaseLifecycleSet $propertyNames `
            @('order', 'operation', 'program', 'arguments', 'consumesPlaceholders',
                'producesPlaceholder', 'expectedParent') `
            "$Description operation schema"
        Assert-Case ([int]$operation.order -eq ($i + 1) -and
            [string]$operation.operation -cmatch '^[A-Z][A-Z0-9_]+$' -and
            [string]$operation.program -cin @('git', 'phase-closeout.ps1')) `
            "$Description operation identity is malformed"
        foreach ($argument in @($operation.arguments)) {
            Assert-Case ($argument -is [string] -and
                [string]$argument -notmatch '[;\r\n]' -and
                -not ([string]$argument).Contains('commandTemplate',
                    [StringComparison]::OrdinalIgnoreCase)) `
                "$Description contains shell-composed or multiline argv"
        }
        foreach ($placeholder in @($operation.consumesPlaceholders)) {
            Assert-Case ([string]$placeholder -ceq
                'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA') `
                "$Description consumes an unknown placeholder"
        }
        if ($null -ne $operation.producesPlaceholder) {
            Assert-Case ([string]$operation.producesPlaceholder -ceq
                'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA') `
                "$Description produces an unknown placeholder"
        }
    }
    $push = @($Plan | Where-Object {
            [string]$_.operation -ceq 'PUSH_BRANCH_AND_TAG'
        })
    $expectedPrefix = @('push', '--atomic', '--no-all', '--no-mirror', '--no-tags',
        '--no-delete', '--no-force', '--no-force-with-lease',
        '--no-force-if-includes', '--recurse-submodules=no', '--no-prune',
        '--no-follow-tags', '--no-signed', '--no-set-upstream', '--no-verify',
        'origin', 'refs/heads/main:refs/heads/main')
    Assert-Case ($push.Count -eq 1 -and @($push[0].arguments).Count -eq 18 -and
        (@($push[0].arguments[0..16]) -join "`n") -ceq
            ($expectedPrefix -join "`n") -and
        [string]$push[0].arguments[17] -cmatch
            '^refs/tags/([^:]+):refs/tags/\1$') `
        "$Description does not prescribe the exact non-force atomic push"
}

function Assert-PostPromotionAuditPlan {
    param(
        [Parameter(Mandatory)] [object]$Plan,
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [string]$CloseoutMode,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Campaign,
        [Parameter(Mandatory)] [string]$ApprovalPath,
        [Parameter(Mandatory)] [string]$Description
    )
    Assert-Case ($Plan -isnot [string]) `
        "$Description is an executable command string"
    $planNames = if ($Plan -is [Collections.IDictionary]) {
        @($Plan.Keys | ForEach-Object { [string]$_ })
    } else { @($Plan.PSObject.Properties.Name) }
    $parameterNames = if ($Plan.parameters -is [Collections.IDictionary]) {
        @($Plan.parameters.Keys | ForEach-Object { [string]$_ })
    } else { @($Plan.parameters.PSObject.Properties.Name) }
    Assert-GeoCeDGPhaseLifecycleSet $planNames `
        @('required', 'authority', 'operation', 'program',
            'sequenceAfterGitOperation', 'parameters', 'consumesPlaceholders',
            'automaticExecutionPerformed') "$Description schema"
    Assert-GeoCeDGPhaseLifecycleSet $parameterNames `
        @('Action', 'CloseoutMode', 'TechnicalCommit', 'CloseoutCommit',
            'PolicyPath', 'ReadinessReceiptPath', 'TechnicalCampaignPath',
            'AuthorApprovalPath', 'ResultPath', 'RepositoryRoot') `
        "$Description parameter schema"
    Assert-Case ($Plan.required -is [bool] -and $Plan.required -and
        [string]$Plan.authority -ceq 'AUTOMATED_READ_ONLY_AUDIT' -and
        [string]$Plan.operation -ceq 'RUN_POST_PROMOTION_AUDIT' -and
        [string]$Plan.program -ceq 'phase-closeout.ps1' -and
        [int]$Plan.sequenceAfterGitOperation -eq 7 -and
        $Plan.automaticExecutionPerformed -is [bool] -and
        -not $Plan.automaticExecutionPerformed -and
        [string]$Plan.parameters.Action -ceq 'AUDIT' -and
        [string]$Plan.parameters.CloseoutMode -ceq $CloseoutMode -and
        [string]$Plan.parameters.TechnicalCommit -ceq $Fixture.TechnicalCommit -and
        [string]$Plan.parameters.CloseoutCommit -ceq
            'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA' -and
        [string]$Plan.parameters.PolicyPath -ceq $Fixture.PolicyPath -and
        [string]$Plan.parameters.ReadinessReceiptPath -ceq $Readiness.Path -and
        [string]$Plan.parameters.TechnicalCampaignPath -ceq $Campaign.Path -and
        [string]$Plan.parameters.AuthorApprovalPath -ceq
            [IO.Path]::GetFullPath($ApprovalPath) -and
        [string]$Plan.parameters.ResultPath -ceq
            'POST_PROMOTION_AUDIT_RESULT_PATH' -and
        [string]$Plan.parameters.RepositoryRoot -ceq
            [IO.Path]::GetFullPath($Fixture.Root) -and
        (@($Plan.consumesPlaceholders) -join ',') -ceq
            'EXACT_CLOSEOUT_SHA_CREATED_FROM_STAGED_DELTA,POST_PROMOTION_AUDIT_RESULT_PATH') `
        "$Description is incomplete, executable, reordered, or targets mutable authority"
    $serialized = $Plan | ConvertTo-Json -Depth 20 -Compress
    Assert-Case (-not $serialized.Contains('commandTemplate',
            [StringComparison]::OrdinalIgnoreCase) -and
        -not $serialized.Contains('latest', [StringComparison]::OrdinalIgnoreCase)) `
        "$Description contains command composition or mutable selectors"
}

function Assert-PhysicalInputClosureBinding {
    param(
        [Parameter(Mandatory)] [object]$Closure,
        [Parameter(Mandatory)] [string]$TechnicalCommit,
        [Parameter(Mandatory)] [string]$Description
    )
    $authorities = @($Closure.authorities)
    $basis = [ordered]@{
        reviewedTechnicalCommit = $TechnicalCommit
        state = 'VERIFIED_AGAINST_EXACT_CLEAN_T'
        authorities = $authorities
    }
    $basisJson = $basis | ConvertTo-Json -Depth 30 -Compress
    $expectedProof = Get-BytesSha256 `
        ([Text.UTF8Encoding]::new($false).GetBytes($basisJson))
    $levels = @($authorities | ForEach-Object { [string]$_.level })
    $invalidHashes = @($authorities | Where-Object {
            [string]$_.canonicalReceiptSha256 -cnotmatch '^[0-9a-f]{64}$' -or
            [string]$_.inputInventoryArtifactSha256 -cnotmatch '^[0-9a-f]{64}$' -or
            [string]$_.inputInventoryRawTreeSha256 -cnotmatch '^[0-9a-f]{64}$' -or
            [string]$_.technicalTreePathAndBlobAuthoritySha256 -cnotmatch
                '^[0-9a-f]{64}$'
        })
    Assert-Case ([string]$Closure.reviewedTechnicalCommit -ceq $TechnicalCommit -and
        [string]$Closure.state -ceq 'VERIFIED_AGAINST_EXACT_CLEAN_T' -and
        ($levels -join ',') -ceq 'FULL' -and
        $invalidHashes.Count -eq 0 -and
        [string]$Closure.proofSha256 -ceq $expectedProof) `
        "$Description does not exactly bind the physical clean-T proof"
}

function Invoke-PwshFixture {
    param(
        [Parameter(Mandatory)] [string[]]$Arguments
    )
    $output = @(& $PwshCommand -NoProfile @Arguments *>&1 |
        ForEach-Object { $_.ToString() })
    return [pscustomobject]@{ ExitCode = [int]$LASTEXITCODE; Output = @($output) }
}

function ConvertTo-PowerShellSingleQuotedLiteral {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Value)
    return "'" + $Value.Replace("'", "''") + "'"
}

function New-PrepareReceiptPersistenceFaultCli {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)]
        [ValidateSet('BEFORE_TEMP_CREATE', 'AFTER_PARTIAL_TEMP_WRITE',
            'AFTER_SAME_PATH_RESTAGE')]
        [string]$FaultMode
    )
    $source = [IO.File]::ReadAllText($CliPath)
    $newline = if ($source.Contains("`r`n", [StringComparison]::Ordinal)) {
        "`r`n"
    } else { "`n" }
    $dotSource = ". (Join-Path `$PSScriptRoot 'closeout-workflow.ps1')"
    Assert-Case ([regex]::Matches($source, [regex]::Escape($dotSource)).Count -eq 1) `
        'PREPARE persistence fault fixture cannot uniquely bind the real helper'
    $source = $source.Replace($dotSource,
        ('. ' + (ConvertTo-PowerShellSingleQuotedLiteral $HelperPath)))

    if ($FaultMode -ceq 'BEFORE_TEMP_CREATE') {
        $needle = '    $bytes = ConvertTo-GeoCeDGCloseoutJsonBytes $Value'
        $fault = @'
    if ([string]$Value.state -ceq 'STATUS_ONLY_CLOSEOUT_STAGED_AWAITING_GIT_FINALIZATION') {
        throw 'FIXTURE_PREPARE_RECEIPT_FAILURE_BEFORE_TEMP_CREATE'
    }
'@
    } elseif ($FaultMode -ceq 'AFTER_PARTIAL_TEMP_WRITE') {
        $needle = '        $temporaryOwned = $true'
        $fault = @'
        if ([string]$Value.state -ceq 'STATUS_ONLY_CLOSEOUT_STAGED_AWAITING_GIT_FINALIZATION') {
            $faultPrefixLength = [Math]::Min(11, $bytes.Length)
            $stream.Write($bytes, 0, $faultPrefixLength)
            $stream.Flush($true)
            throw 'FIXTURE_PREPARE_RECEIPT_FAILURE_AFTER_PARTIAL_TEMP_WRITE'
        }
'@
    } else {
        $needle = '    $bytes = ConvertTo-GeoCeDGCloseoutJsonBytes $Value'
        $fault = @'
    if ([string]$Value.state -ceq 'STATUS_ONLY_CLOSEOUT_STAGED_AWAITING_GIT_FINALIZATION') {
        $statusAuthorities = @($Value.expectedCloseoutDelta | Where-Object {
                [string]$_.path -cne [string]$Value.closeoutRecordPath
            })
        if ($statusAuthorities.Count -ne 1) {
            throw 'FIXTURE_EXPECTED_ONE_STATUS_AUTHORITY_BEFORE_RESTAGE'
        }
        $tamperedPath = Join-Path $root ([string]$statusAuthorities[0].path)
        [IO.File]::AppendAllText($tamperedPath,
            "FIXTURE_UNAUTHENTICATED_SAME_PATH_BYTES`n",
            [Text.UTF8Encoding]::new($false))
        $restage = Invoke-GeoCeDGCloseoutGitText $root `
            @('add', '--', [string]$statusAuthorities[0].path)
        if ($restage.ExitCode -ne 0) {
            throw 'FIXTURE_UNABLE_TO_RESTAGE_SAME_PATH_TAMPER'
        }
        throw 'FIXTURE_PREPARE_RECEIPT_FAILURE_AFTER_SAME_PATH_RESTAGE'
    }
'@
    }
    Assert-Case ([regex]::Matches($source, [regex]::Escape($needle)).Count -eq 1) `
        "PREPARE persistence fault fixture has no unique $FaultMode injection point"
    $source = $source.Replace($needle, "$needle$newline$fault")
    $path = Join-Path $Fixture.ExternalRoot `
        "phase-closeout-$($FaultMode.ToLowerInvariant()).ps1"
    Write-Text $path $source
    return $path
}

function Invoke-CloseoutCli {
    param(
        [Parameter(Mandatory)] [ValidateSet('PREPARE', 'FINALIZE', 'AUDIT')]
        [string]$Action,
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Campaign,
        [Parameter(Mandatory)] [string]$ApprovalPath,
        [Parameter(Mandatory)] [string]$ResultPath,
        [string]$CloseoutCommit,
        [string]$PreparationResultPath,
        [string]$ScriptPath = $CliPath
    )
    Assert-Case (Test-Path -LiteralPath $ScriptPath -PathType Leaf) `
        "Closeout CLI fixture script is missing: $ScriptPath"
    $parts = @(
        '&', (ConvertTo-PowerShellSingleQuotedLiteral $ScriptPath),
        '-Action', (ConvertTo-PowerShellSingleQuotedLiteral $Action),
        '-CloseoutMode', (ConvertTo-PowerShellSingleQuotedLiteral $CloseoutMode),
        '-TechnicalCommit', (ConvertTo-PowerShellSingleQuotedLiteral $Fixture.TechnicalCommit),
        '-PolicyPath', (ConvertTo-PowerShellSingleQuotedLiteral $Fixture.PolicyPath),
        '-ReadinessReceiptPath', (ConvertTo-PowerShellSingleQuotedLiteral $Readiness.Path),
        '-TechnicalCampaignPath', (ConvertTo-PowerShellSingleQuotedLiteral $Campaign.Path),
        '-AuthorApprovalPath', (ConvertTo-PowerShellSingleQuotedLiteral $ApprovalPath)
    )
    if ($Action -ceq 'AUDIT') {
        Assert-Case (-not [string]::IsNullOrWhiteSpace($CloseoutCommit)) `
            'AUDIT fixture invocation requires C'
        $parts += @('-CloseoutCommit',
            (ConvertTo-PowerShellSingleQuotedLiteral $CloseoutCommit))
    }
    if ($Action -ceq 'FINALIZE') {
        Assert-Case (-not [string]::IsNullOrWhiteSpace($PreparationResultPath)) `
            'FINALIZE fixture invocation requires a PREPARE receipt'
        $parts += @('-PreparationResultPath',
            (ConvertTo-PowerShellSingleQuotedLiteral $PreparationResultPath))
    }
    $parts += @('-ResultPath', (ConvertTo-PowerShellSingleQuotedLiteral $ResultPath),
        '-RepositoryRoot', (ConvertTo-PowerShellSingleQuotedLiteral $Fixture.Root))
    $command = $parts -join ' '
    $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))
    return Invoke-PwshFixture @('-EncodedCommand', $encoded)
}

function Invoke-PrepareDirect {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [Parameter(Mandatory)] [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode,
        [Parameter(Mandatory)] [object]$Readiness,
        [Parameter(Mandatory)] [object]$Campaign,
        [Parameter(Mandatory)] [string]$ApprovalPath
    )
    return Invoke-GeoCeDGCloseoutPreparation -RepositoryRoot $Fixture.Root `
        -TechnicalCommit $Fixture.TechnicalCommit -PolicyPath $Fixture.PolicyPath `
        -CloseoutMode $CloseoutMode -ReadinessReceiptPath $Readiness.Path `
        -TechnicalCampaignPath $Campaign.Path -AuthorApprovalPath $ApprovalPath
}

function Complete-Promotion {
    param(
        [Parameter(Mandatory)] [object]$Fixture,
        [string]$Topology = 'DIRECT',
        [switch]$ProductChange,
        [switch]$ExtraCloseoutDelta
    )
    if ($ProductChange) {
        Write-Text (Join-Path $Fixture.Root 'source/product.java') `
            "final class Product { int value = 2; }`n"
        [void](Invoke-FixtureGit $Fixture.Root @('add', '--', 'source/product.java'))
    }
    if ($ExtraCloseoutDelta) {
        Write-Text (Join-Path $Fixture.Root 'docs/extra-closeout.md') `
            "Unauthorized extra closeout path.`n"
        [void](Invoke-FixtureGit $Fixture.Root @('add', '--', 'docs/extra-closeout.md'))
    }
    $closeout = Commit-Fixture $Fixture.Root $Fixture.CloseoutMessage
    if ($Topology -ceq 'NON_DIRECT') {
        $closeout = Commit-Fixture $Fixture.Root 'Unexpected intervening commit' -AllowEmpty
    } elseif ($Topology -ceq 'MERGE') {
        [void](Invoke-FixtureGit $Fixture.Root @('switch', '--quiet', '-c',
            "side/$($Fixture.Name)", $Fixture.TechnicalCommit))
        [void](Commit-Fixture $Fixture.Root 'Unexpected side commit' -AllowEmpty)
        [void](Invoke-FixtureGit $Fixture.Root @('switch', '--quiet', $Fixture.Branch))
        [void](Invoke-FixtureGit $Fixture.Root @('merge', '--quiet', '--no-ff',
            '-m', 'Unexpected merge closeout', "side/$($Fixture.Name)"))
        $closeout = Get-FixtureGitText $Fixture.Root @('rev-parse', 'HEAD')
    } elseif ($Topology -cne 'DIRECT') {
        throw "Unknown fixture topology: $Topology"
    }
    [void](Invoke-FixtureGit $Fixture.Root @('tag', '-a', $Fixture.TagName,
        '-m', $Fixture.TagMessage, $closeout))
    [void](Invoke-FixtureGit $Fixture.Root @('switch', '--quiet', 'main'))
    [void](Invoke-FixtureGit $Fixture.Root @('merge', '--quiet', '--ff-only', $closeout))
    [void](Invoke-FixtureGit $Fixture.Root @('push', '--quiet', '--atomic', 'origin',
        'refs/heads/main:refs/heads/main',
        "refs/tags/$($Fixture.TagName):refs/tags/$($Fixture.TagName)"))
    return $closeout
}

function New-PublishedFixture {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [string]$Topology = 'DIRECT',
        [switch]$ProductChange,
        [switch]$ExtraCloseoutDelta,
        [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode = 'AUTHOR_OPERATED'
    )
    $fixture = New-WorkflowFixture $Name
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture $CloseoutMode
    $preparation = Invoke-PrepareDirect $fixture $CloseoutMode $readiness $campaign $approval
    $closeout = Complete-Promotion $fixture -Topology $Topology `
        -ProductChange:$ProductChange -ExtraCloseoutDelta:$ExtraCloseoutDelta
    return [pscustomobject]@{
        Fixture = $fixture
        Readiness = $readiness
        Campaign = $campaign
        ApprovalPath = $approval
        Preparation = $preparation
        CloseoutCommit = $closeout
        CloseoutMode = $CloseoutMode
    }
}

function New-PreparedFixture {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [ValidateSet('VERIFIED', 'AUTHOR_OPERATED')]
        [string]$CloseoutMode = 'AUTHOR_OPERATED'
    )
    $fixture = New-WorkflowFixture $Name
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture $CloseoutMode
    $preparation = Invoke-PrepareDirect $fixture $CloseoutMode $readiness `
        $campaign $approval
    return [pscustomobject]@{
        Fixture = $fixture
        Readiness = $readiness
        Campaign = $campaign
        ApprovalPath = $approval
        Preparation = $preparation
        CloseoutCommit = $null
        CloseoutMode = $CloseoutMode
    }
}

function New-CliPreparedVerifiedFixture {
    param([Parameter(Mandatory)] [string]$Name)
    $fixture = New-WorkflowFixture $Name
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture VERIFIED
    $preparationPath = Join-Path $fixture.ExternalRoot `
        'verified-preparation-result.json'
    $execution = Invoke-CloseoutCli -Action PREPARE -Fixture $fixture `
        -CloseoutMode VERIFIED -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -ResultPath $preparationPath
    Assert-Case ($execution.ExitCode -eq 0 -and
        (Test-Path -LiteralPath $preparationPath -PathType Leaf)) `
        "VERIFIED CLI PREPARE failed: $($execution.Output -join ' ')"
    return [pscustomobject]@{
        Fixture = $fixture
        Readiness = $readiness
        Campaign = $campaign
        ApprovalPath = $approval
        PreparationPath = $preparationPath
        Preparation = Read-Json $preparationPath
        CloseoutMode = 'VERIFIED'
    }
}

function Assert-CliPrepareReceiptPersistencePreservation {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)]
        [ValidateSet('BEFORE_TEMP_CREATE', 'AFTER_PARTIAL_TEMP_WRITE')]
        [string]$FaultMode,
        [Parameter(Mandatory)] [string]$ExpectedFailure
    )
    $fixture = New-WorkflowFixture $Name
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture VERIFIED
    $faultCli = New-PrepareReceiptPersistenceFaultCli $fixture $FaultMode
    $resultPath = Join-Path $fixture.ExternalRoot 'failed-preparation-result.json'
    $statusPath = Join-Path $fixture.Root $fixture.StatusPath
    $recordPath = Join-Path $fixture.Root $fixture.RecordPath
    $before = Get-RepositorySnapshot $fixture
    $remoteTagsBeforeResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsBefore = @($remoteTagsBeforeResult.Output)
    $statusShaBefore = Get-FileSha256 $statusPath
    $execution = Invoke-CloseoutCli -Action PREPARE -Fixture $fixture `
        -CloseoutMode VERIFIED -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -ResultPath $resultPath -ScriptPath $faultCli
    $after = Get-RepositorySnapshot $fixture
    $remoteTagsAfterResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsAfter = @($remoteTagsAfterResult.Output)
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match [regex]::Escape($ExpectedFailure)) -and
        (($execution.Output -join ' ') -match
            'PREPARE receipt persistence failed after the exact delta was staged; repository state was preserved for inspection') -and
        (Test-Path -LiteralPath $resultPath -PathType Leaf)) `
        "Injected $FaultMode PREPARE persistence failure was not reported and saved"
    $failure = Read-Json $resultPath
    $temporaryReceipts = @(Get-ChildItem -LiteralPath `
            (Split-Path -Parent $resultPath) -File -Filter `
            "$([IO.Path]::GetFileName($resultPath)).geocedg-tmp-*")
    Assert-Case ([string]$failure.kind -ceq 'GEOCEDG_CLOSEOUT_PREPARATION' -and
        [string]$failure.state -ceq 'CLOSEOUT_PREPARATION_FAILED' -and
        [string]$failure.closeoutMode -ceq 'VERIFIED' -and
        [string]$failure.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit -and
        [string]$failure.mutationState -ceq 'UNKNOWN_REQUIRES_INSPECTION' -and
        $failure.partialMutationPossible -and
        -not $failure.automaticCommitPerformed -and
        -not $failure.automaticTagPerformed -and
        -not $failure.automaticFastForwardPerformed -and
        -not $failure.automaticPushPerformed -and
        [string]$failure.requiredRecovery -ceq
            'INSPECT_EXACT_T_STATUS_PATHS_AND_DECISION_RECORD' -and
        $null -eq $failure.preparationRollback -and
        ([string]$failure.failure -match [regex]::Escape($ExpectedFailure))) `
        "$FaultMode failure receipt concealed its preserved PREPARE mutation"
    Assert-GeoCeDGPhaseLifecycleSet $status.Staged `
        @($fixture.StatusPath, $fixture.RecordPath) `
        "$FaultMode preserved PREPARE staged paths"
    Assert-Case ((($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($remoteTagsBefore -join "`n") -ceq ($remoteTagsAfter -join "`n")) -and
        [string]$statusShaBefore -cne (Get-FileSha256 $statusPath) -and
        $status.Unstaged.Count -eq 0 -and
        $status.Untracked.Count -eq 0 -and
        (Test-Path -LiteralPath $recordPath -PathType Leaf) -and
        $temporaryReceipts.Count -eq 0) `
        "$FaultMode did not preserve exact staged delta, refs/tags/remotes, or atomic receipt state"
}

function Assert-CliPrepareTamperedStatePreservation {
    $fixture = New-WorkflowFixture 'prepare-receipt-same-path-tamper'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture VERIFIED
    $faultCli = New-PrepareReceiptPersistenceFaultCli $fixture `
        AFTER_SAME_PATH_RESTAGE
    $resultPath = Join-Path $fixture.ExternalRoot 'unknown-preparation-result.json'
    $statusPath = Join-Path $fixture.Root $fixture.StatusPath
    $recordPath = Join-Path $fixture.Root $fixture.RecordPath
    $before = Get-RepositorySnapshot $fixture
    $remoteTagsBeforeResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsBefore = @($remoteTagsBeforeResult.Output)
    $cleanStatusSha = Get-FileSha256 $statusPath
    $execution = Invoke-CloseoutCli -Action PREPARE -Fixture $fixture `
        -CloseoutMode VERIFIED -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -ResultPath $resultPath -ScriptPath $faultCli
    $after = Get-RepositorySnapshot $fixture
    $remoteTagsAfterResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsAfter = @($remoteTagsAfterResult.Output)
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match
            'FIXTURE_PREPARE_RECEIPT_FAILURE_AFTER_SAME_PATH_RESTAGE') -and
        (($execution.Output -join ' ') -match
            'PREPARE receipt persistence failed after the exact delta was staged; repository state was preserved for inspection') -and
        (Test-Path -LiteralPath $resultPath -PathType Leaf)) `
        'Same-path PREPARE tamper did not fail closed with state preservation'
    $failure = Read-Json $resultPath
    $physicalStatusBytes = [IO.File]::ReadAllBytes($statusPath)
    $indexStatusBytes = (Invoke-GeoCeDGGitByteCommand `
        -RepositoryRoot $fixture.Root -Arguments @('show', ":$($fixture.StatusPath)")).Bytes
    $temporaryReceipts = @(Get-ChildItem -LiteralPath `
            (Split-Path -Parent $resultPath) -File -Filter `
            "$([IO.Path]::GetFileName($resultPath)).geocedg-tmp-*")
    Assert-Case ([string]$failure.kind -ceq 'GEOCEDG_CLOSEOUT_PREPARATION' -and
        [string]$failure.state -ceq 'CLOSEOUT_PREPARATION_FAILED' -and
        [string]$failure.closeoutMode -ceq 'VERIFIED' -and
        [string]$failure.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit -and
        [string]$failure.mutationState -ceq 'UNKNOWN_REQUIRES_INSPECTION' -and
        $failure.partialMutationPossible -and
        [string]$failure.requiredRecovery -ceq
            'INSPECT_EXACT_T_STATUS_PATHS_AND_DECISION_RECORD' -and
        $null -eq $failure.preparationRollback -and
        -not $failure.automaticCommitPerformed -and
        -not $failure.automaticTagPerformed -and
        -not $failure.automaticFastForwardPerformed -and
        -not $failure.automaticPushPerformed -and
        ([string]$failure.failure -match
            'FIXTURE_PREPARE_RECEIPT_FAILURE_AFTER_SAME_PATH_RESTAGE')) `
        'Same-path tamper failure receipt concealed uncertain PREPARE mutation state'
    Assert-GeoCeDGPhaseLifecycleSet $status.Staged `
        @($fixture.StatusPath, $fixture.RecordPath) `
        'Preserved same-path tampered PREPARE state'
    Assert-Case ($status.Unstaged.Count -eq 0 -and $status.Untracked.Count -eq 0 -and
        [string]$cleanStatusSha -cne (Get-BytesSha256 $physicalStatusBytes) -and
        (Get-BytesSha256 $physicalStatusBytes) -ceq
            (Get-BytesSha256 $indexStatusBytes) -and
        ([Text.UTF8Encoding]::new($false).GetString($physicalStatusBytes)).Contains(
            'FIXTURE_UNAUTHENTICATED_SAME_PATH_BYTES', [StringComparison]::Ordinal) -and
        (Test-Path -LiteralPath $recordPath -PathType Leaf) -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($remoteTagsBefore -join "`n") -ceq ($remoteTagsAfter -join "`n")) -and
        $temporaryReceipts.Count -eq 0) `
        'PREPARE erased unauthenticated same-path evidence instead of preserving it'
}

function Invoke-AuditDirect {
    param([Parameter(Mandatory)] [object]$Published)
    return Test-GeoCeDGCloseoutPostPromotionAudit `
        -RepositoryRoot $Published.Fixture.Root `
        -TechnicalCommit $Published.Fixture.TechnicalCommit `
        -CloseoutCommit $Published.CloseoutCommit `
        -PolicyPath $Published.Fixture.PolicyPath `
        -CloseoutMode $Published.CloseoutMode `
        -ReadinessReceiptPath $Published.Readiness.Path `
        -TechnicalCampaignPath $Published.Campaign.Path `
        -AuthorApprovalPath $Published.ApprovalPath
}

function Invoke-CloseoutCase {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [scriptblock]$Action
    )
    $script:CaseNumber++
    try {
        & $Action
        $Results.Add([ordered]@{ ordinal = $script:CaseNumber; name = $Name; status = 'PASS' })
        Write-Host "PASS: $Name"
    } catch {
        $Results.Add([ordered]@{ ordinal = $script:CaseNumber; name = $Name; status = 'FAIL' })
        Write-Host "FAIL: $Name -- $($_.Exception.Message)"
    }
}

Invoke-CloseoutCase 'readiness is mode-neutral and validates both routes without reducing coverage' {
    $fixture = New-WorkflowFixture 'readiness-both-modes'
    $receipt = New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
        -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
    Assert-Case ([string]$receipt.state -ceq 'CLOSEOUT_READINESS_PASSED' -and
        [string]$receipt.verificationClass -ceq
            'OPERATIONAL_VERIFICATION_INFRASTRUCTURE' -and
        [string]$receipt.plannedAcceptanceLevel -ceq 'FULL' -and
        $null -eq $receipt.closeoutMode -and
        (@($receipt.validatedCloseoutModes) -join ',') -ceq
            'VERIFIED,AUTHOR_OPERATED' -and
        (@($receipt.requiredTechnicalLevels) -join ',') -ceq 'PHASE,COMPOSED,FULL' -and
        [string]$receipt.acceptancePlanSha256 -cmatch '^[0-9a-f]{64}$' -and
        [string]$receipt.technicalCampaignPlan.kind -ceq
            'GEOCEDG_SINGLE_FULL_ACCEPTANCE_EXECUTION_PLAN' -and
        [string]$receipt.technicalCampaignPlan.strategy -ceq
            'SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS' -and
        [string]$receipt.technicalCampaignPlanSha256 -cmatch '^[0-9a-f]{64}$' -and
        [long]$receipt.technicalCampaignPlan.physicalCampaign.count -eq 1 -and
        [string]$receipt.technicalCampaignPlan.physicalCampaign.level -ceq 'FULL' -and
        -not $receipt.technicalCampaignPlan.selection.coverageReduced -and
        -not $receipt.technicalCampaignPlan.selection.
            independentRunsClaimedForDerivedLevels -and
        [string]$receipt.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit -and
        $receipt.repository.remoteIdentity.fetchAndPushUrlIdentical -eq $true -and
        $receipt.route.recordCreation.pathTraversalReparseFree -eq $true -and
        $receipt.acceptanceEvidenceConsumable -eq $false -and
        $receipt.selfApproved -eq $false) 'Mode-neutral readiness contract changed'
}

Invoke-CloseoutCase 'impact classification freezes the minimum planned acceptance level' {
    $cases = @(
        @('BOUNDED_PHASE', $false, $false, $true, 'PHASE'),
        @('INTEGRATED_PHASE', $false, $false, $true, 'PHASE'),
        @('INTEGRATED_PHASE', $true, $false, $true, 'COMPOSED'),
        @('GLOBAL_IMPACT', $false, $false, $true, 'FULL'),
        @('RELEASE_OR_MILESTONE', $false, $false, $true, 'FULL'),
        @('OPERATIONAL_VERIFICATION_INFRASTRUCTURE', $false, $false, $true, 'PHASE'),
        @('OPERATIONAL_VERIFICATION_INFRASTRUCTURE', $false, $true, $true, 'FULL'),
        @('DOCUMENTATION_STATUS_ONLY', $false, $false, $false, 'STATIC')
    )
    foreach ($case in $cases) {
        $plan = [pscustomobject][ordered]@{
            verificationClass = [string]$case[0]
            frozenAtPhaseStart = $true
            plannedAcceptanceLevel = [string]$case[4]
            integratedCoverageAdditional = [bool]$case[1]
            globalInfrastructureChange = [bool]$case[2]
            focusedEvidenceRequired = [bool]$case[3]
            escalationRequestKind = 'VERIFICATION_ESCALATION_REQUEST'
        }
        $validated = Get-GeoCeDGVerificationImpactPlan $plan
        Assert-Case ([string]$validated.plannedAcceptanceLevel -ceq
            [string]$case[4]) "Impact class $($case[0]) mapped to the wrong level"
    }
}

Invoke-CloseoutCase 'frozen bounded plan emits escalation request instead of running FULL' {
    $fixture = New-WorkflowFixture 'readiness-impact-escalation' `
        'UNAUTHORIZED_FULL_ESCALATION'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'VERIFICATION_ESCALATION_REQUEST|wait for explicit author authorization' `
        'Unauthorized verification escalation')
}

Invoke-CloseoutCase 'readiness rejects an unfrozen plan or inferred single-integrator mode' {
    foreach ($variant in @('UNFROZEN_VERIFICATION_PLAN',
            'INVALID_SINGLE_INTEGRATOR_DEFAULT')) {
        $fixture = New-WorkflowFixture "readiness-$($variant.ToLowerInvariant())" $variant
        [void](Assert-Throws {
                New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                    -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
            } 'frozen at phase start|BOUNDED_PHASE/AUTHOR_OPERATED' $variant)
    }
}

Invoke-CloseoutCase 'fresh readiness object is shape-valid for pre-heavy comparison' {
    $fixture = New-WorkflowFixture 'pre-heavy-fresh-readiness-shape'
    $receipt = New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
        -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
    $comparison = Compare-GeoCeDGCloseoutPreHeavyExecutionPlan `
        -ReadinessReceipt $receipt -RequestedLevel FULL `
        -CleanGeneratedOutputs $true -IndependentBuilds $false
    Assert-Case ($receipt.schemaVersion -is [long] -and
        $comparison.comparedBeforeHeavy -and $comparison.expectedEqualsSelected) `
        'Fresh readiness constructor output is not accepted by the pre-heavy shape gate'
}

Invoke-CloseoutCase 'stored readiness preflight selects one clean FULL without repository mutation' {
    $fixture = New-WorkflowFixture 'pre-heavy-single-full'
    $readiness = Write-ReadinessReceipt $fixture
    $before = Get-RepositorySnapshot $fixture
    $beforeStatus = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    $validatedReadiness = Test-GeoCeDGCloseoutAcceptancePreflight `
        -RepositoryRoot $fixture.Root -TechnicalCommit $fixture.TechnicalCommit `
        -PolicyPath $fixture.PolicyPath -ReadinessReceiptPath $readiness.Path
    $comparison = Compare-GeoCeDGCloseoutPreHeavyExecutionPlan `
        -ReadinessReceipt $validatedReadiness -RequestedLevel FULL `
        -CleanGeneratedOutputs $true -IndependentBuilds $false
    $after = Get-RepositorySnapshot $fixture
    $afterStatus = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ($validatedReadiness.schemaVersion -is [long] -and
        $comparison.comparedBeforeHeavy -and
        $comparison.expectedEqualsSelected -and
        [string]$comparison.expectedPlanSha256 -ceq
            [string]$readiness.Receipt.technicalCampaignPlanSha256 -and
        [string]$comparison.selectedPlanSha256 -ceq
            [string]$readiness.Receipt.technicalCampaignPlanSha256 -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($beforeStatus | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($afterStatus | ConvertTo-Json -Depth 20 -Compress))) `
        'Pre-heavy comparison changed plan authority or repository state'
}

foreach ($invalidPhysicalLevel in @('PHASE', 'COMPOSED')) {
    $requestedPhysicalLevel = $invalidPhysicalLevel
    Invoke-CloseoutCase "pre-heavy comparison rejects $requestedPhysicalLevel as physical acceptance root" {
        $fixture = New-WorkflowFixture `
            "pre-heavy-reject-$($requestedPhysicalLevel.ToLowerInvariant())"
        $readiness = Write-ReadinessReceipt $fixture
        [void](Assert-Throws {
                Compare-GeoCeDGCloseoutPreHeavyExecutionPlan `
                    -ReadinessReceipt $readiness.Receipt `
                    -RequestedLevel $requestedPhysicalLevel `
                    -CleanGeneratedOutputs $true -IndependentBuilds $false
            } 'exactly one clean, non-independent FULL' `
            "$requestedPhysicalLevel pre-heavy physical campaign")
    }
}

Invoke-CloseoutCase 'readiness rejects dirty or precommit worktree cohorts' {
    $fixture = New-WorkflowFixture 'readiness-dirty'
    [IO.File]::AppendAllText((Join-Path $fixture.Root $fixture.StatusPath),
        "DIRTY`n", [Text.UTF8Encoding]::new($false))
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'dirty/precommit|clean worktree' 'Dirty/precommit readiness')
}

Invoke-CloseoutCase 'readiness rejects symbolic HEAD instead of an exact T SHA' {
    $fixture = New-WorkflowFixture 'readiness-symbolic-head'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit HEAD -PolicyPath $fixture.PolicyPath
        } 'full lowercase commit SHA|resolve exactly|exact' 'Symbolic technical selector')
}

Invoke-CloseoutCase 'closeout mode converter rejects unsupported values' {
    [void](Assert-Throws { ConvertTo-GeoCeDGCloseoutMode AUTOMATIC } `
        'Unsupported closeout mode|AUTOMATIC' 'Invalid closeout mode')
}

Invoke-CloseoutCase 'lowercase mode is canonicalized while READINESS remains mode-neutral' {
    Assert-Case ((ConvertTo-GeoCeDGCloseoutMode 'verified') -ceq 'VERIFIED' -and
        (ConvertTo-GeoCeDGCloseoutMode 'author_operated') -ceq 'AUTHOR_OPERATED') `
        'Lowercase mode canonicalization changed'
    $fixture = New-WorkflowFixture 'readiness-rejects-mode-selection'
    $failurePath = Join-Path $fixture.ExternalRoot 'readiness-with-mode.json'
    [void][IO.Directory]::CreateDirectory($fixture.ExternalRoot)
    $execution = Invoke-PwshFixture @('-File', $CliPath, '-Action', 'READINESS',
        '-CloseoutMode', 'verified', '-TechnicalCommit', $fixture.TechnicalCommit,
        '-PolicyPath', $fixture.PolicyPath, '-ReadinessReceiptPath', $failurePath,
        '-RepositoryRoot', $fixture.Root)
    Assert-Case ($execution.ExitCode -ne 0 -and
        (($execution.Output -join ' ') -match 'mode-neutral|omit.*CloseoutMode') -and
        -not (Test-Path -LiteralPath $failurePath)) `
        'READINESS accepted a premature closeout-mode selection'
}

Invoke-CloseoutCase 'readiness blocks a phase without both declared closeout routes' {
    $fixture = New-WorkflowFixture 'readiness-missing-route' 'MISSING_AUTHOR_ROUTE'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'closeout modes|mismatch|declared route' 'Missing declarative closeout route')
}

Invoke-CloseoutCase 'readiness rejects reduced PHASE COMPOSED FULL coverage' {
    $fixture = New-WorkflowFixture 'readiness-reduced-coverage' 'REDUCED_COVERAGE'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'PHASE, COMPOSED, and FULL|coverage' 'Reduced acceptance coverage')
}

Invoke-CloseoutCase 'readiness blocks known lifecycle repair requirements' {
    $fixture = New-WorkflowFixture 'readiness-repair-required' 'REPAIR_REQUIRED'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'lifecycle repair|CLOSEOUT_READINESS' 'Known lifecycle repair')
}

Invoke-CloseoutCase 'readiness rejects policy output in executable verifier paths' {
    $fixture = New-WorkflowFixture 'readiness-forbidden-policy-path' 'FORBIDDEN_PATH'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'outside status|executable|status/documentation' 'Executable closeout policy path')
}

Invoke-CloseoutCase 'readiness rejects a scientific spec as a status replacement' {
    $fixture = New-WorkflowFixture 'readiness-scientific-spec-policy-path' `
        'NON_OPERATIONAL_SPEC_PATH'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'outside operational status/documentation authority' `
        'Scientific spec closeout policy path')
}

Invoke-CloseoutCase 'readiness rejects validation Markdown as a status replacement' {
    $fixture = New-WorkflowFixture 'readiness-validation-markdown-policy-path' `
        'VALIDATION_MARKDOWN_PATH'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'outside operational status/documentation authority' `
        'Validation Markdown closeout policy path')
}

Invoke-CloseoutCase 'readiness rejects a tag name Git cannot construct' {
    $fixture = New-WorkflowFixture 'readiness-invalid-tag-name' 'INVALID_TAG'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'Invalid expected annotated phase tag name|check-ref-format' `
        'Invalid schema-v2 expected annotated phase tag name')
}

Invoke-CloseoutCase 'readiness rejects a non-string annotated tag name' {
    $fixture = New-WorkflowFixture 'readiness-numeric-tag-name' 'NUMERIC_TAG'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'promotion values must be JSON strings' `
        'Numeric schema-v2 annotated phase tag name')
}

Invoke-CloseoutCase 'readiness rejects a null literal replacement target value' {
    $fixture = New-WorkflowFixture 'readiness-null-replacement-after' `
        'NULL_REPLACEMENT_AFTER'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'literal replacement path/before/after values must be JSON strings' `
        'Null schema-v2 literal replacement after value')
}

Invoke-CloseoutCase 'readiness rejects a merged technical history from M0 to T' {
    $fixture = New-WorkflowFixture 'readiness-merged-technical' -MergeTechnicalCommit
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'Promotion anchor M0\.\.T contains a merge|multiple parents' `
        'Merged technical candidate')
}

Invoke-CloseoutCase 'readiness rejects a decision record already present at T' {
    $fixture = New-WorkflowFixture 'readiness-preexisting-record' -PreexistingRecord
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'record already exists at T|decision-record.*absent' `
        'Pre-existing closeout decision record')
}

Invoke-CloseoutCase 'readiness rejects an ignored future decision-record path' {
    $fixture = New-WorkflowFixture 'readiness-ignored-record' -IgnoredRecord
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'decision-record path is ignored|ignore authority' `
        'Ignored closeout decision-record path')
}

Invoke-CloseoutCase 'readiness rejects ident expansion on the future decision record' {
    $fixture = New-WorkflowFixture 'readiness-ident-record' -IdentRecord
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'Unsupported materialization attribute.*ident' `
        'Decision-record ident expansion')
}

Invoke-CloseoutCase 'readiness rejects an active clean filter on the future decision record' {
    $fixture = New-WorkflowFixture 'readiness-filtered-record' -FilteredRecord
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'Unsupported materialization attribute.*filter' `
        'Decision-record clean filter')
}

Invoke-CloseoutCase 'readiness rejects multiple configured fetch URLs' {
    $fixture = New-WorkflowFixture 'readiness-multiple-fetch-urls'
    $alternate = New-AlternateBareRemote $fixture 'alternate-fetch'
    [void](Invoke-FixtureGit $fixture.Root @('remote', 'set-url', '--add',
        'origin', $alternate))
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'exactly one configured fetch URL and one push URL' `
        'Multiple promotion fetch URLs')
}

Invoke-CloseoutCase 'readiness rejects multiple configured push URLs' {
    $fixture = New-WorkflowFixture 'readiness-multiple-push-urls'
    $alternate = New-AlternateBareRemote $fixture 'alternate-push'
    [void](Invoke-FixtureGit $fixture.Root @('remote', 'set-url', '--push',
        'origin', $fixture.Origin))
    [void](Invoke-FixtureGit $fixture.Root @('remote', 'set-url', '--add', '--push',
        'origin', $alternate))
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'exactly one configured fetch URL and one push URL' `
        'Multiple promotion push URLs')
}

Invoke-CloseoutCase 'readiness rejects mirror promotion remotes before acceptance' {
    $fixture = New-WorkflowFixture 'readiness-remote-mirror'
    [void](Invoke-FixtureGit $fixture.Root @('config', 'remote.origin.mirror', 'true'))
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'rejects remote mirror configuration|expand the pushed ref set' `
        'Mirror promotion remote')
}

Invoke-CloseoutCase 'readiness rejects inherited push options before acceptance' {
    $fixture = New-WorkflowFixture 'readiness-push-option'
    [void](Invoke-FixtureGit $fixture.Root @('config', '--add', 'push.pushOption',
        'fixture-server-option'))
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath
        } 'rejects configured push options|cannot portably clear' `
        'Inherited configured push option')
}

Invoke-CloseoutCase 'stored readiness rejects a fetch remote identity swap with unchanged refs' {
    $fixture = New-WorkflowFixture 'readiness-fetch-identity-swap'
    $readiness = Write-ReadinessReceipt $fixture
    $alternate = New-AlternateBareRemote $fixture 'alternate-fetch-swap'
    [void](Invoke-FixtureGit $fixture.Root @('remote', 'set-url', 'origin', $alternate))
    [void](Assert-Throws {
            Test-GeoCeDGCloseoutAcceptancePreflight -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath `
                -ReadinessReceiptPath $readiness.Path
        } 'Promotion remote URL/identity changed' 'Fetch remote identity swap')
}

Invoke-CloseoutCase 'readiness rejects a divergent push remote identity with unchanged refs' {
    $fixture = New-WorkflowFixture 'readiness-push-identity-swap'
    $readiness = Write-ReadinessReceipt $fixture
    $alternate = New-AlternateBareRemote $fixture 'alternate-push-swap'
    [void](Invoke-FixtureGit $fixture.Root @('remote', 'set-url', '--push',
        'origin', $alternate))
    [void](Assert-Throws {
            Test-GeoCeDGCloseoutAcceptancePreflight -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit -PolicyPath $fixture.PolicyPath `
                -ReadinessReceiptPath $readiness.Path
        } 'Promotion remote has a divergent push URL|Promotion remote URL/identity changed' `
        'Divergent push remote identity')
}

foreach ($gitOverride in @('GIT_DIR', 'GIT_SHALLOW_FILE')) {
    $overrideName = $gitOverride
    Invoke-CloseoutCase "readiness rejects $overrideName before invoking Git" {
        $fixture = New-WorkflowFixture `
            "readiness-env-$($overrideName.ToLowerInvariant().Replace('_', '-'))"
        $variables = [ordered]@{}
        $variables[$overrideName] = Join-Path $fixture.ExternalRoot 'attacker-authority'
        Invoke-WithFixtureGitEnvironment $variables {
            [void](Assert-Throws {
                    New-GeoCeDGCloseoutReadinessReceipt `
                        -RepositoryRoot $fixture.Root `
                        -TechnicalCommit $fixture.TechnicalCommit `
                        -PolicyPath $fixture.PolicyPath
                } "Unsupported Git authority environment override: $overrideName" `
                "$overrideName authority override")
        }
    }
}

Invoke-CloseoutCase 'readiness rejects injected url rewrite configuration before Git' {
    $fixture = New-WorkflowFixture 'readiness-env-url-insteadof'
    Invoke-WithFixtureGitEnvironment ([ordered]@{
            GIT_CONFIG_COUNT = '1'
            GIT_CONFIG_KEY_0 = 'url.file:///attacker/.insteadOf'
            GIT_CONFIG_VALUE_0 = 'fixture://origin/'
        }) {
        [void](Assert-Throws {
                New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                    -TechnicalCommit $fixture.TechnicalCommit `
                    -PolicyPath $fixture.PolicyPath
            } 'Unsupported injected Git config at index 0' `
            'Injected url.*.insteadOf authority')
    }
}

Invoke-CloseoutCase 'absolute injected safe.directory remains an allowed Git trust input' {
    $fixture = New-WorkflowFixture 'readiness-env-safe-directory'
    $receipt = Invoke-WithFixtureGitEnvironment ([ordered]@{
            GIT_CONFIG_COUNT = '1'
            GIT_CONFIG_KEY_0 = 'safe.directory'
            GIT_CONFIG_VALUE_0 = [IO.Path]::GetFullPath($fixture.Root)
        }) {
        New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
            -TechnicalCommit $fixture.TechnicalCommit `
            -PolicyPath $fixture.PolicyPath
    }
    Assert-Case ([string]$receipt.state -ceq 'CLOSEOUT_READINESS_PASSED' -and
        [string]$receipt.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit) `
        'Allowed absolute safe.directory changed readiness authority'
}

Invoke-CloseoutCase 'readiness rejects replace objects without observing substituted T tree' {
    $fixture = New-WorkflowFixture 'readiness-replace-object'
    $technicalSelector = "$($fixture.TechnicalCommit)^{tree}"
    $exactTree = ((Invoke-RawGit @('--no-replace-objects', '-C', $fixture.Root,
                'rev-parse', $technicalSelector)).Output -join '').Trim()
    [void](Invoke-FixtureGit $fixture.Root @('replace', $fixture.TechnicalCommit,
        $fixture.MainAnchor))
    $substitutedTree = Get-FixtureGitText $fixture.Root @('rev-parse', $technicalSelector)
    $stillExactTree = ((Invoke-RawGit @('--no-replace-objects', '-C', $fixture.Root,
                'rev-parse', $technicalSelector)).Output -join '').Trim()
    Assert-Case ($substitutedTree -cne $exactTree -and
        $stillExactTree -ceq $exactTree) `
        'Replace-object fixture did not distinguish substituted and exact T trees'
    [void](Assert-Throws {
            New-GeoCeDGCloseoutReadinessReceipt -RepositoryRoot $fixture.Root `
                -TechnicalCommit $fixture.TechnicalCommit `
                -PolicyPath $fixture.PolicyPath
        } 'Git replace objects are unsupported for immutable repository identity' `
        'Replace-object technical authority')
}

Invoke-CloseoutCase 'schema-v2 Git paths share no-replace guarded launchers' {
    $evidencePath = Join-Path $AgentRoot 'evidence-integrity.ps1'
    $closeoutSource = [IO.File]::ReadAllText($HelperPath)
    $identitySource = [IO.File]::ReadAllText($IdentityPath)
    $evidenceSource = [IO.File]::ReadAllText($evidencePath)
    $tokens = $null
    $errors = $null
    $ast = [Management.Automation.Language.Parser]::ParseFile(
        $HelperPath, [ref]$tokens, [ref]$errors)
    $directGit = @($ast.FindAll({ param($node)
                $node -is [Management.Automation.Language.CommandAst] -and
                $node.GetCommandName() -ceq 'git'
            }, $true))
    Assert-Case (@($errors).Count -eq 0 -and $directGit.Count -eq 0 -and
        $closeoutSource.Contains('Invoke-GeoCeDGGitByteCommand',
            [StringComparison]::Ordinal) -and
        $closeoutSource.Contains('Get-GeoCeDGRepositoryTrackedIdentity',
            [StringComparison]::Ordinal) -and
        $evidenceSource.Contains('--no-replace-objects',
            [StringComparison]::Ordinal) -and
        $evidenceSource.Contains('--no-optional-locks',
            [StringComparison]::Ordinal) -and
        $identitySource.Contains('--no-replace-objects',
            [StringComparison]::Ordinal) -and
        $identitySource.Contains('--no-optional-locks',
            [StringComparison]::Ordinal)) `
        'A schema-v2 Git path bypasses the centralized no-replace launchers'
}

Invoke-CloseoutCase 'repository reform policy has only projectable schema-v2 status replacements' {
    $repositoryRoot = [IO.Path]::GetFullPath((Join-Path $AgentRoot '../..'))
    $policyPath = Join-Path $repositoryRoot `
        'geocedg/validation/operations/dual-closeout-reform-policy.json'
    $schemaPath = Join-Path $repositoryRoot `
        'geocedg/specs/operations/phase-closeout-policy.schema.json'
    Assert-Case (Test-Path -LiteralPath $policyPath -PathType Leaf) `
        'Repository dual-closeout reform policy is missing'
    Assert-Case (Test-Path -LiteralPath $schemaPath -PathType Leaf) `
        'Repository schema-v2 policy contract is missing'
    $policyText = [IO.File]::ReadAllText($policyPath)
    $policy = $policyText | ConvertFrom-Json -Depth 100
    $schema = Read-Json $schemaPath
    Assert-GeoCeDGPhaseLifecycleSet @($policy.PSObject.Properties.Name) `
        @($schema.required | ForEach-Object { [string]$_ }) `
        'Repository policy/schema top-level contract'
    Assert-GeoCeDGPhaseLifecycleSet @($policy.technicalEvidence.PSObject.Properties.Name) `
        @($schema.properties.technicalEvidence.required | ForEach-Object { [string]$_ }) `
        'Repository technical-evidence policy/schema contract'
    Assert-GeoCeDGPhaseLifecycleSet @($policy.verificationPlan.PSObject.Properties.Name) `
        @($schema.properties.verificationPlan.required | ForEach-Object { [string]$_ }) `
        'Repository verification-impact plan/schema contract'
    Assert-GeoCeDGPhaseLifecycleSet @($policy.singleIntegratorDefaults.PSObject.Properties.Name) `
        @($schema.properties.singleIntegratorDefaults.required | ForEach-Object { [string]$_ }) `
        'Repository single-integrator defaults/schema contract'
    Assert-GeoCeDGPhaseLifecycleSet @($policy.promotion.PSObject.Properties.Name) `
        @($schema.properties.promotion.required | ForEach-Object { [string]$_ }) `
        'Repository promotion policy/schema contract'
    Assert-GeoCeDGPhaseLifecycleSet @($policy.closeout.PSObject.Properties.Name) `
        @($schema.properties.closeout.required | ForEach-Object { [string]$_ }) `
        'Repository closeout policy/schema contract'
    Assert-Case ($policy.schemaVersion -eq 2 -and
        [string]$policy.verificationPlan.verificationClass -ceq
            'OPERATIONAL_VERIFICATION_INFRASTRUCTURE' -and
        $policy.verificationPlan.frozenAtPhaseStart -eq $true -and
        [string]$policy.verificationPlan.plannedAcceptanceLevel -ceq 'FULL' -and
        $policy.verificationPlan.globalInfrastructureChange -eq $true -and
        [string]$policy.singleIntegratorDefaults.verificationClass -ceq
            'BOUNDED_PHASE' -and
        [string]$policy.singleIntegratorDefaults.closeoutMode -ceq
            'AUTHOR_OPERATED' -and
        $policy.singleIntegratorDefaults.closeoutModeRequiresExplicitSelection -eq $true -and
        (@($policy.technicalEvidence.requiredLevels) -join ',') -ceq
            'PHASE,COMPOSED,FULL' -and
        [string]$policy.technicalEvidence.campaignStrategy -ceq
            'SINGLE_FULL_WITH_AUTHENTICATED_CLAIMS' -and
        [string]$policy.technicalEvidence.physicalCampaignLevel -ceq 'FULL' -and
        (@($policy.technicalEvidence.requiredClaims | ForEach-Object {
                    [string]$_.level
                }) -join ',') -ceq 'PHASE,COMPOSED,FULL' -and
        @($policy.technicalEvidence.complements).Count -eq 0 -and
        (@($policy.closeout.supportedModes | Sort-Object) -join ',') -ceq
            'AUTHOR_OPERATED,VERIFIED' -and
        $policy.technicalEvidence.fullRequiresCleanBuild -eq $true -and
        $policy.technicalEvidence.lifecycleRepairRequired -eq $false -and
        @($policy.closeout.literalReplacements).Count -gt 0 -and
        @($policy.closeout.canonicalLfHashManifests).Count -eq 0 -and
        [int]$schema.properties.closeout.properties.canonicalLfHashManifests.maxItems -eq 0 -and
        $policyText -notmatch '(?i)\b[0-9a-f]{40}\b' -and
        $policyText -notmatch '(?i)"latest"') `
        'Repository reform policy reduced coverage, changed modes, or embedded mutable identity'
    foreach ($replacement in @($policy.closeout.literalReplacements)) {
        Assert-GeoCeDGPhaseLifecycleSet @($replacement.PSObject.Properties.Name) `
            @($schema.'$defs'.literalReplacement.required | ForEach-Object { [string]$_ }) `
            'Repository literal replacement/schema contract'
        $target = Join-Path $repositoryRoot ([string]$replacement.path)
        Assert-Case (Test-Path -LiteralPath $target -PathType Leaf) `
            "Repository replacement target is missing: $($replacement.path)"
        $targetText = [IO.File]::ReadAllText($target)
        $beforeCount = [regex]::Matches($targetText,
            [regex]::Escape([string]$replacement.before)).Count
        $afterCount = [regex]::Matches($targetText,
            [regex]::Escape([string]$replacement.after)).Count
        Assert-Case ([string]$replacement.path -cmatch
                '^(docs|geocedg/specs|geocedg/validation)/.*\.md$' -and
            [long]$replacement.occurrences -eq 1 -and
            (($beforeCount -eq 1 -and $afterCount -eq 0) -or
                ($beforeCount -eq 0 -and $afterCount -eq 1))) `
            "Repository closeout replacement is not uniquely projectable: $($replacement.path)"
    }
}

Invoke-CloseoutCase 'exact final evidence campaign remains bound to clean T and all three levels' {
    $fixture = New-WorkflowFixture 'evidence-exact-t'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $policy = Read-GeoCeDGCloseoutWorkflowPolicy $fixture.Root `
        $fixture.TechnicalCommit $fixture.PolicyPath
    $result = Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
        -RepositoryRoot $fixture.Root -TechnicalCommit $fixture.TechnicalCommit `
        -PolicyContext $policy `
        -ReadinessReceiptSha256 $readiness.Sha256 `
        -AcceptancePlanSha256 $readiness.Receipt.acceptancePlanSha256 `
        -TechnicalCampaignPath $campaign.Path -RequireExactCurrentMaterialization
    Assert-Case ($result.complete -and
        (@($result.requiredLevels) -join ',') -ceq 'PHASE,COMPOSED,FULL' -and
        $null -eq $result.closeoutMode -and
        (@($result.validatedCloseoutModes) -join ',') -ceq
            'VERIFIED,AUTHOR_OPERATED' -and
        [string]$result.acceptancePlanSha256 -ceq
            [string]$readiness.Receipt.acceptancePlanSha256 -and
        [string]$result.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit -and
        $result.physicalInputClosureVerified -and
        [string]$result.rootResult.repositoryCommit -ceq $fixture.TechnicalCommit -and
        [string]$result.envelope.coverageStrategy -ceq 'SINGLE_PHYSICAL_FULL' -and
        [long]$result.envelope.physicalCampaignCount -eq 1 -and
        (@($result.claims | ForEach-Object { [string]$_.level }) -join ',') -ceq
            'PHASE,COMPOSED,FULL' -and
        [string]$result.claims[0].fulfillment -ceq 'NESTED_EXECUTED' -and
        [string]$result.claims[1].fulfillment -ceq 'NORMATIVE_SUBSUMPTION' -and
        [string]$result.claims[2].fulfillment -ceq 'ROOT_PHYSICAL_CAMPAIGN' -and
        -not $result.claims[0].independentPhysicalCampaign -and
        -not $result.claims[1].independentPhysicalCampaign -and
        $result.claims[2].independentPhysicalCampaign -and
        @($result.envelope.complements).Count -eq 0 -and
        $result.full.archivedCanonicalProof.archivedContentVerified -and
        [int]$result.full.archivedCanonicalProof.junitReports -eq 2 -and
        [int]$result.full.archivedCanonicalProof.checkstyleReports -eq 4 -and
        [int]$result.full.archivedCanonicalProof.nativeRuns -eq 4 -and
        [int]$result.full.archivedCanonicalProof.auditArtifacts -eq 6 -and
        $result.full.archivedCanonicalProof.inputInventory.exactTechnicalTreePathSet -and
        $result.full.archivedCanonicalProof.inputInventory.recordSchemaAndSummaryVerified -and
        $result.full.archivedCanonicalProof.inputInventory.exactPhysicalMaterializationVerified -and
        $result.full.archivedCanonicalProof.externalConfiguration.canonicalContentVerified -and
        @($result.full.canonicalReceipt.tasks | Where-Object {
                [string]$_.task -ceq ':shared:canvas-base:classes' -and
                [long]$_.headingOccurrences -eq 1 -and
                [string]$_.outcome -ceq 'UP-TO-DATE'
            }).Count -eq 2 -and
        [string]$result.full.canonicalReceipt.inputIdentity.head -ceq
            $fixture.TechnicalCommit) 'Exact technical evidence attribution changed'
}

Invoke-CloseoutCase 'persisted single FULL envelope round-trips without host DateTime rehashing' {
    $fixture = New-WorkflowFixture 'evidence-persisted-roundtrip'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $beforeSha256 = Get-FileSha256 $campaign.Path
    $policy = Read-GeoCeDGCloseoutWorkflowPolicy $fixture.Root `
        $fixture.TechnicalCommit $fixture.PolicyPath
    $result = Read-GeoCeDGCloseoutTechnicalEvidenceCampaign `
        -RepositoryRoot $fixture.Root -TechnicalCommit $fixture.TechnicalCommit `
        -PolicyContext $policy `
        -ReadinessReceiptSha256 $readiness.Sha256 `
        -AcceptancePlanSha256 $readiness.Receipt.acceptancePlanSha256 `
        -TechnicalCampaignPath $campaign.Path
    $afterSha256 = Get-FileSha256 $campaign.Path
    $rawCampaign = Get-GeoCeDGCloseoutRawMinifiedJsonPropertyText `
        -Bytes ([IO.File]::ReadAllBytes($campaign.Path)) `
        -PropertyName 'technicalCampaign' `
        -Description 'persisted single FULL round-trip fixture'
    Assert-Case ($beforeSha256 -ceq $afterSha256 -and $result.complete -and
        [string]$result.claims[0].level -ceq 'PHASE' -and
        [string]$result.claims[0].claimSha256 -cmatch '^[0-9a-f]{64}$' -and
        $rawCampaign.Contains('"startedUtc":"2001-02-03T04:05:09.0000000Z"',
            [StringComparison]::Ordinal) -and
        $rawCampaign.Contains('"completedUtc":"2001-02-03T04:05:11.0000000Z"',
            [StringComparison]::Ordinal)) `
        'Persisted campaign was rewritten or its canonical timestamp-bound claims changed'
}

Invoke-CloseoutCase 'single FULL campaign rejects a semantically equivalent re-escaped envelope name' {
    $fixture = New-WorkflowFixture 'evidence-reescaped-envelope-name'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $text = [IO.File]::ReadAllText($campaign.Path)
    Assert-Case ([regex]::Matches($text, '"technicalCampaign"').Count -eq 1) `
        'Canonical fixture does not have one exact technicalCampaign name token'
    Write-Text $campaign.Path ($text.Replace('"technicalCampaign"',
            '"\u0074echnicalCampaign"'))
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        "re-encoded the 'technicalCampaign' property name" `
        'Re-escaped top-level technicalCampaign name'
}

Invoke-CloseoutCase 'single FULL campaign rejects a missing authenticated claim' {
    $fixture = New-WorkflowFixture 'evidence-missing-level'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $result = Read-Json $campaign.Path
    $result.technicalCampaign.claims = @($result.technicalCampaign.claims[0..1])
    Write-Json $campaign.Path $result
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'claim roles are incomplete, duplicated, or out of order' `
        'Incomplete single-FULL authenticated claims'
}

foreach ($canonicalLevel in @('PHASE', 'COMPOSED', 'FULL')) {
    $expectedLevel = $canonicalLevel
    Invoke-CloseoutCase "evidence campaign rejects lowercase $expectedLevel classification" {
        $fixture = New-WorkflowFixture "evidence-lowercase-$($expectedLevel.ToLowerInvariant())"
        $readiness = Write-ReadinessReceipt $fixture
        $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
        $pathIndex = [Array]::IndexOf(@('PHASE', 'COMPOSED', 'FULL'), $expectedLevel)
        $result = Read-Json $campaign.Path
        $result.technicalCampaign.claims[$pathIndex].level =
            $expectedLevel.ToLowerInvariant()
        Write-Json $campaign.Path $result
        Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
            'claim roles are incomplete, duplicated, or out of order' `
            "Lowercase $expectedLevel technical evidence classification"
    }
}

foreach ($nonFullRoot in @('PHASE', 'COMPOSED')) {
    $selectedRoot = $nonFullRoot
    Invoke-CloseoutCase "closeout rejects a $selectedRoot root instead of one physical FULL" {
        $fixture = New-WorkflowFixture "evidence-root-$($selectedRoot.ToLowerInvariant())"
        $readiness = Write-ReadinessReceipt $fixture
        $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
        $result = Read-Json $campaign.Path
        $result.level = $selectedRoot
        Write-Json $campaign.Path $result
        Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
            'not one closeout-consumable clean FULL campaign' `
            "$selectedRoot cannot replace the single physical FULL root"
    }
}

Invoke-CloseoutCase 'closeout rejects lowercase full as a root classification' {
    $fixture = New-WorkflowFixture 'evidence-root-lowercase-full'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $result = Read-Json $campaign.Path
    $result.level = 'full'
    Write-Json $campaign.Path $result
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'not one closeout-consumable clean FULL campaign' `
        'Lowercase FULL root classification'
}

Invoke-CloseoutCase 'single FULL campaign rejects a synthetic lower-level physical run claim' {
    $fixture = New-WorkflowFixture 'evidence-synthetic-independent-phase'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $result = Read-Json $campaign.Path
    $result.technicalCampaign.claims[0].independentPhysicalCampaign = $true
    Write-Json $campaign.Path $result
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'envelope or authenticated evidence manifest is not canonical' `
        'Synthetic independent PHASE execution claim'
}

Invoke-CloseoutCase 'single FULL campaign rejects an undeclared acceptance complement' {
    $fixture = New-WorkflowFixture 'evidence-undeclared-complement'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $result = Read-Json $campaign.Path
    $result.technicalCampaign.complements = @([ordered]@{
            obligation = 'SYNTHETIC_EXTRA_RUN'
            result = 'PASS'
        })
    Write-Json $campaign.Path $result
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'reduced, duplicated, or relabelled technical coverage' `
        'Undeclared acceptance complement'
}

Invoke-CloseoutCase 'canonical campaign rejects an empty archived audit inventory' {
    $fixture = New-WorkflowFixture 'evidence-empty-audit-inventory'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.auditArtifacts = @()
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'audit inventory must contain exact input/external inventories and every native log' `
        'Empty archived audit inventory'
}

Invoke-CloseoutCase 'canonical campaign rejects an empty JUnit inventory' {
    $fixture = New-WorkflowFixture 'evidence-empty-junit'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.junit = @()
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'has no shared JUnit evidence' 'Empty archived JUnit inventory'
}

Invoke-CloseoutCase 'canonical campaign rejects inconsistent aggregate test counters' {
    $fixture = New-WorkflowFixture 'evidence-aggregate-counter-mismatch'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.tests = 3
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'aggregate JUnit counters are inconsistent' `
        'Canonical aggregate counter mismatch'
}

Invoke-CloseoutCase 'canonical campaign rejects inconsistent JUnit cases' {
    $fixture = New-WorkflowFixture 'evidence-case-counter-mismatch'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.junit[0].cases = @()
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'shared JUnit counters/cases are inconsistent' `
        'Canonical JUnit case mismatch'
}

Invoke-CloseoutCase 'canonical campaign rejects string-valued unfiltered authority' {
    $fixture = New-WorkflowFixture 'evidence-string-unfiltered'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.selections.shared.unfiltered = 'true'
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'FULL/shared selection is not exact' `
        'String-valued FULL unfiltered authority'
}

Invoke-CloseoutCase 'single physical FULL rejects a narrowed selection' {
    $fixture = New-WorkflowFixture 'evidence-narrowed-composed'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.selections.shared.filters = @('org.geocedg.*')
    $receipt.selections.shared.unfiltered = $false
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'FULL/shared selection is not exact' `
        'Narrowed single physical FULL shared selection'
}

Invoke-CloseoutCase 'canonical campaign rejects a native log outside the audit inventory' {
    $fixture = New-WorkflowFixture 'evidence-native-log-uninventoried'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $nativeLog = [string]$receipt.nativeRuns[0].logPath
    $receipt.auditArtifacts = @($receipt.auditArtifacts | Where-Object {
            [string]$_.path -cne $nativeLog
        })
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'audit inventory must contain exact input/external inventories and every native log' `
        'Native log absent from canonical audit inventory'
}

Invoke-CloseoutCase 'canonical campaign rejects incomplete Checkstyle authority' {
    $fixture = New-WorkflowFixture 'evidence-incomplete-checkstyle'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.checkstyle = @($receipt.checkstyle | Where-Object {
            [string]$_.task -cne ':desktop:desktop:checkstyleTest'
        })
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'Checkstyle evidence set is incomplete' `
        'Incomplete canonical Checkstyle authority'
}

Invoke-CloseoutCase 'canonical campaign rejects Checkstyle task casing substitution' {
    $fixture = New-WorkflowFixture 'evidence-checkstyle-task-casing'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.checkstyle[0].task = ':shared:common:CheckstyleMain'
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'Checkstyle authority is invalid' `
        'Case-substituted Checkstyle task authority'
}

Invoke-CloseoutCase 'canonical campaign rejects a duplicate report archive path' {
    $fixture = New-WorkflowFixture 'evidence-duplicate-report-archive'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.checkstyle[1].archivePath = $receipt.checkstyle[0].archivePath
    $receipt.checkstyle[1].sha256 = $receipt.checkstyle[0].sha256
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'report archive path is duplicated' `
        'Duplicate Checkstyle report archive path'
}

Invoke-CloseoutCase 'canonical campaign rejects empty selected Test JVM evidence' {
    $fixture = New-WorkflowFixture 'evidence-empty-selected-test-jvm'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.selectedTestJvms.shared = @()
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'selected Test JVM evidence is empty or non-array: shared' `
        'Empty selected Test JVM authority'
}

Invoke-CloseoutCase 'canonical campaign rejects malformed selected Test JVM evidence' {
    $fixture = New-WorkflowFixture 'evidence-malformed-selected-test-jvm'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.selectedTestJvms.desktop[0] | Add-Member -NotePropertyName invented `
        -NotePropertyValue 'not producer evidence'
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'selected Test JVM desktop entry has unsupported, missing, or case-mismatched properties' `
        'Malformed selected Test JVM authority'
}

Invoke-CloseoutCase 'canonical campaign rejects one synthetic native run claiming both Test tasks' {
    $fixture = New-WorkflowFixture 'evidence-synthetic-combined-native-run'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.nativeRuns[2].arguments = @($receipt.nativeRuns[2].arguments) +
        @(':desktop:desktop:test')
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'shared producer argv is invalid' `
        'Synthetic combined shared/desktop native execution'
}

Invoke-CloseoutCase 'canonical campaign rejects an incorrect wrapper executable' {
    $fixture = New-WorkflowFixture 'evidence-wrong-wrapper'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.nativeRuns[0].file = Join-Path $fixture.ExternalRoot 'other-wrapper.bat'
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'native-run provenance is invalid' `
        'Incorrect canonical Gradle wrapper executable'
}

Invoke-CloseoutCase 'canonical campaign rejects an absent wrapper identity probe' {
    $fixture = New-WorkflowFixture 'evidence-missing-wrapper-probe'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.nativeRuns = @($receipt.nativeRuns[1..3])
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'native-run provenance has an unexpected producer run count' `
        'Missing canonical wrapper identity probe'
}

Invoke-CloseoutCase 'canonical campaign rejects a narrowed native Test filter argv' {
    $fixture = New-WorkflowFixture 'evidence-native-filter-argv'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.nativeRuns[2].arguments = @($receipt.nativeRuns[2].arguments) +
        @('--tests', 'org.geocedg.SyntheticOnly')
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'shared producer argv is invalid' `
        'Narrowed shared native Test filter argv'
}

Invoke-CloseoutCase 'canonical campaign rejects divergent native execution flags' {
    $fixture = New-WorkflowFixture 'evidence-native-flag-argv'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.nativeRuns[3].arguments = @($receipt.nativeRuns[3].arguments |
        Where-Object { [string]$_ -cne '--no-parallel' })
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'desktop producer argv is invalid' `
        'Divergent desktop native execution flags'
}

Invoke-CloseoutCase 'canonical campaign rejects Test JVM evidence not bound to its native log' {
    $fixture = New-WorkflowFixture 'evidence-test-jvm-log-unbound'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $sharedLog = [string]$receipt.nativeRuns[2].logPath
    $unboundJvm = Join-Path $fixture.ExternalRoot 'invented-jdk/bin/java.exe'
    Write-Text $sharedLog `
        "Starting process 'Gradle Test Executor 1'. Command: `"$unboundJvm`" -Dfixture=shared`n"
    Update-CanonicalAuditArtifactHash $receipt `
        ([IO.Path]::GetFileName($sharedLog))
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'selected Test JVMs differ from the shared native log' `
        'Unbound shared Test JVM native log'
}

Invoke-CloseoutCase 'PREPARE rejects an inventory reauthored away from clean physical T' {
    $fixture = New-WorkflowFixture 'evidence-inventory-not-physical-t'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $receipt = Read-Json $campaign.FullReceiptPath
    $artifact = Get-CanonicalAuditArtifact $receipt 'input-inventory.json'
    $inventory = @((Read-Json ([string]$artifact.path)))
    $inventory[0].bytes = [long]$inventory[0].bytes + 1
    $inventory[0].sha256 = '0' * 64
    Write-Json ([string]$artifact.path) $inventory
    $receipt.inputIdentity.rawTreeSha256 = Get-CanonicalArraySha256 $inventory
    $receipt.inputIdentity.rawFiles = [long]$inventory.Count
    $receipt.inputIdentity.rawBytes = [long](($inventory | Measure-Object `
                -Property bytes -Sum).Sum)
    Update-CanonicalAuditArtifactHash $receipt 'input-inventory.json'
    Update-CanonicalReceiptFingerprint $receipt
    Write-Json $campaign.FullReceiptPath $receipt
    Rebuild-TechnicalEvidenceCampaignEnvelope $fixture $readiness $campaign
    $recordPhysical = Join-Path $fixture.Root $fixture.RecordPath
    $statusPhysical = Join-Path $fixture.Root $fixture.StatusPath
    $repositoryBefore = Get-RepositorySnapshot $fixture
    $statusBefore = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    $statusBytesBefore = Get-FileSha256 $statusPhysical
    Assert-Case (-not (Test-Path -LiteralPath $recordPhysical)) `
        'Reauthored-inventory fixture unexpectedly began with a closeout record'
    [void](Assert-Throws {
            Invoke-PrepareDirect $fixture AUTHOR_OPERATED $readiness $campaign $approval
        } 'Physical clean-T input closure differs in file/byte totals' `
        'Reauthored clean-T physical input inventory')
    $repositoryAfter = Get-RepositorySnapshot $fixture
    $statusAfter = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ((($repositoryBefore | ConvertTo-Json -Depth 10 -Compress) -ceq
            ($repositoryAfter | ConvertTo-Json -Depth 10 -Compress)) -and
        (($statusBefore | ConvertTo-Json -Depth 10 -Compress) -ceq
            ($statusAfter | ConvertTo-Json -Depth 10 -Compress)) -and
        [string]$statusBytesBefore -ceq (Get-FileSha256 $statusPhysical) -and
        $statusAfter.Staged.Count -eq 0 -and $statusAfter.Unstaged.Count -eq 0 -and
        $statusAfter.Untracked.Count -eq 0 -and
        -not (Test-Path -LiteralPath $recordPhysical)) `
        'PREPARE mutated files, index, refs, tags, or remote before physical closure failed'
}

foreach ($rawSummaryField in @('rawTreeSha256', 'rawFiles', 'rawBytes')) {
    $expectedRawSummaryField = $rawSummaryField
    Invoke-CloseoutCase "canonical campaign rejects divergent $expectedRawSummaryField summary" {
        $fixture = New-WorkflowFixture "evidence-divergent-$expectedRawSummaryField"
        $readiness = Write-ReadinessReceipt $fixture
        $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
        $receipt = Read-Json $campaign.FullReceiptPath
        if ($expectedRawSummaryField -ceq 'rawTreeSha256') {
            $receipt.inputIdentity.$expectedRawSummaryField = '0' * 64
        } else {
            $receipt.inputIdentity.$expectedRawSummaryField =
                [long]$receipt.inputIdentity.$expectedRawSummaryField + 1
        }
        Update-CanonicalReceiptFingerprint $receipt
        Write-Json $campaign.FullReceiptPath $receipt
        Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
            'input inventory rawTree/rawFiles/rawBytes summary is inconsistent' `
            "Divergent $expectedRawSummaryField summary"
    }
}

Invoke-CloseoutCase 'canonical campaign rejects divergent externalConfigurationSha256' {
    $fixture = New-WorkflowFixture 'evidence-divergent-external-digest'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $receipt.inputIdentity.externalConfigurationSha256 = '0' * 64
    Update-CanonicalReceiptFingerprint $receipt
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'externalConfigurationSha256 is inconsistent with canonical content' `
        'Divergent external configuration digest'
}

Invoke-CloseoutCase 'canonical campaign rejects noncanonical external configuration content' {
    $fixture = New-WorkflowFixture 'evidence-noncanonical-external-content'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $receipt = Read-Json $campaign.FullReceiptPath
    $artifact = Get-CanonicalAuditArtifact $receipt 'external-configuration.json'
    $configuration = @((Read-Json ([string]$artifact.path)))
    [Array]::Reverse($configuration)
    Write-Json ([string]$artifact.path) $configuration
    $receipt.inputIdentity.externalConfigurationSha256 =
        Get-CanonicalArraySha256 $configuration
    Update-CanonicalAuditArtifactHash $receipt 'external-configuration.json'
    Update-CanonicalReceiptFingerprint $receipt
    Write-Json $campaign.FullReceiptPath $receipt
    Assert-TechnicalEvidenceCampaignRejected $fixture $readiness $campaign `
        'external configuration record is invalid' `
        'Noncanonical external configuration ordering'
}

Invoke-CloseoutCase 'VERIFIED preparation keeps legacy authority and performs no implicit Git finalization' {
    $fixture = New-WorkflowFixture 'verified-preparation'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture VERIFIED
    $before = Get-RepositorySnapshot $fixture
    $prepared = Invoke-PrepareDirect $fixture VERIFIED $readiness $campaign $approval
    $after = Get-RepositorySnapshot $fixture
    Assert-Case ([string]$prepared.gitFinalizationAuthority -ceq 'VERIFIED_WORKFLOW' -and
        [string]$prepared.closeoutMode -ceq 'VERIFIED' -and
        [string]$prepared.modeState -ceq
            'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION_PASSED_STATUS_ONLY_DELTA_STAGED' -and
        [string]$prepared.verifiedAuthorCloseout.gate -ceq
            'VERIFIED_AUTHOR_CLOSEOUT_PREPARATION' -and
        [string]$prepared.verifiedAuthorCloseout.state -ceq
            'PASSED_PROJECTED_DELTA_AWAITING_C' -and
        @($prepared.humanOperations).Count -eq 0 -and
        @($prepared.verifiedWorkflowOperations).Count -eq 7 -and
        -not $prepared.automaticCommitPerformed -and -not $prepared.automaticTagPerformed -and
        -not $prepared.automaticFastForwardPerformed -and -not $prepared.automaticPushPerformed -and
        (($before | ConvertTo-Json -Depth 10 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 10 -Compress))) `
        'VERIFIED preparation weakened explicit finalization authority'
    Assert-StructuredOperationPlan @($prepared.verifiedWorkflowOperations) 7 `
        'VERIFIED workflow plan'
    Assert-StructuredOperationPlan @($prepared.remainingGitOperations) 7 `
        'VERIFIED remaining Git plan'
    Assert-PostPromotionAuditPlan $prepared.postPromotionAudit $fixture VERIFIED `
        $readiness $campaign $approval 'VERIFIED post-promotion audit plan'
}

Invoke-CloseoutCase 'PREPARE performs a fresh preflight immediately before bounded writes' {
    $source = [IO.File]::ReadAllText($HelperPath)
    $start = $source.IndexOf('function Invoke-GeoCeDGCloseoutPreparation',
        [StringComparison]::Ordinal)
    $end = $source.IndexOf('function New-GeoCeDGCloseoutDecisionRecord',
        $start, [StringComparison]::Ordinal)
    if ($end -lt 0) {
        $end = $source.IndexOf('function Invoke-GeoCeDGVerifiedCloseoutFinalization',
            $start, [StringComparison]::Ordinal)
    }
    Assert-Case ($start -ge 0 -and $end -gt $start) `
        'PREPARE function boundary is missing or ambiguous'
    $body = $source.Substring($start, $end - $start)
    $preflights = @([regex]::Matches($body,
            'Test-GeoCeDGCloseoutAcceptancePreflight'))
    $lastPreflight = $body.LastIndexOf(
        'Test-GeoCeDGCloseoutAcceptancePreflight', [StringComparison]::Ordinal)
    $mutation = $body.IndexOf('$mutationStarted = $true',
        [StringComparison]::Ordinal)
    $firstWrite = $body.IndexOf('[IO.File]::WriteAllBytes',
        [StringComparison]::Ordinal)
    Assert-Case ($preflights.Count -eq 2 -and $lastPreflight -gt 0 -and
        $mutation -gt $lastPreflight -and $firstWrite -gt $mutation) `
        'PREPARE no longer closes TOCTOU with a fresh preflight before its first write'
}

Invoke-CloseoutCase 'PREPARE has no automatic destructive rollback after its first mutation' {
    $source = [IO.File]::ReadAllText($HelperPath)
    $start = $source.IndexOf('function Invoke-GeoCeDGCloseoutPreparation',
        [StringComparison]::Ordinal)
    $end = $source.IndexOf('function Assert-GeoCeDGCloseoutLinearPromotionHistory',
        $start, [StringComparison]::Ordinal)
    Assert-Case ($start -ge 0 -and $end -gt $start) `
        'PREPARE preservation function boundary is missing or ambiguous'
    $body = $source.Substring($start, $end - $start)
    Assert-Case (-not $body.Contains(
            'Restore-GeoCeDGCloseoutPreparationMutation',
            [StringComparison]::Ordinal) -and
        -not [regex]::IsMatch($body,
            "@\('(?:restore|update-index)'",
            [Text.RegularExpressions.RegexOptions]::CultureInvariant)) `
        'PREPARE regained destructive restore/update-index authority after mutation'
}

Invoke-CloseoutCase 'PREPARE preserves bounded unstaged writes when staging fails' {
    $fixture = New-WorkflowFixture 'prepare-staging-failure-preservation'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $before = Get-RepositorySnapshot $fixture
    $statusPath = Join-Path $fixture.Root $fixture.StatusPath
    $statusShaBefore = Get-FileSha256 $statusPath
    $recordPath = Join-Path $fixture.Root $fixture.RecordPath
    $originalGitText = (Get-Command -Name Invoke-GeoCeDGCloseoutGitText `
        -CommandType Function -ErrorAction Stop).ScriptBlock
    $script:FixtureOriginalCloseoutGitText = $originalGitText
    $script:FixtureStagingFailureInjected = $false
    $script:FixtureDestructiveRollbackObserved = $false
    $stagingFailureInjected = $false
    Set-Item Function:script:Invoke-GeoCeDGCloseoutGitText -Value {
        param(
            [Parameter(Mandatory)] [string]$RepositoryRoot,
            [Parameter(Mandatory)] [string[]]$Arguments,
            [switch]$AllowFailure
        )
        if (-not $script:FixtureStagingFailureInjected -and
                $Arguments.Count -gt 0 -and [string]$Arguments[0] -ceq 'add') {
            $script:FixtureStagingFailureInjected = $true
            throw 'FIXTURE_SYNTHETIC_STAGE_FAILURE_AFTER_BOUNDED_WRITES'
        }
        if ($Arguments.Count -gt 0 -and
                [string]$Arguments[0] -cin @('restore', 'update-index')) {
            $script:FixtureDestructiveRollbackObserved = $true
        }
        & $script:FixtureOriginalCloseoutGitText `
            -RepositoryRoot $RepositoryRoot -Arguments $Arguments `
            -AllowFailure:$AllowFailure
    }
    try {
        $preAddFailure = Assert-Throws {
                Invoke-PrepareDirect $fixture AUTHOR_OPERATED $readiness `
                    $campaign $approval
            } 'PREPARE failed after its first bounded mutation; repository state was preserved for inspection:.*FIXTURE_SYNTHETIC_STAGE_FAILURE_AFTER_BOUNDED_WRITES' `
            'Synthetic PREPARE staging failure'
    } finally {
        $stagingFailureInjected = [bool]$script:FixtureStagingFailureInjected
        $destructiveRollbackObserved =
            [bool]$script:FixtureDestructiveRollbackObserved
        Set-Item Function:script:Invoke-GeoCeDGCloseoutGitText `
            -Value $originalGitText
        Remove-Variable -Scope Script -Name FixtureOriginalCloseoutGitText,
            FixtureStagingFailureInjected,FixtureDestructiveRollbackObserved `
            -ErrorAction SilentlyContinue
    }
    $after = Get-RepositorySnapshot $fixture
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case $stagingFailureInjected `
        'Synthetic PREPARE fault injection did not execute'
    Assert-GeoCeDGPhaseLifecycleSet $status.Unstaged @($fixture.StatusPath) `
        'Preserved PREPARE pre-add status path'
    Assert-GeoCeDGPhaseLifecycleSet $status.Untracked @($fixture.RecordPath) `
        'Preserved PREPARE pre-add decision record'
    Assert-Case ((($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$statusShaBefore -cne (Get-FileSha256 $statusPath) -and
        $status.Staged.Count -eq 0 -and
        (Test-Path -LiteralPath $recordPath -PathType Leaf) -and
        -not $destructiveRollbackObserved -and
        $preAddFailure -match 'repository state was preserved for inspection') `
        'PREPARE staging failure erased bounded evidence or invoked destructive rollback'
}

Invoke-CloseoutCase 'PREPARE preserves the staged delta when result construction fails' {
    $fixture = New-WorkflowFixture 'prepare-post-stage-result-failure-preservation'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $before = Get-RepositorySnapshot $fixture
    $remoteTagsBeforeResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsBefore = @($remoteTagsBeforeResult.Output)
    $statusPath = Join-Path $fixture.Root $fixture.StatusPath
    $statusShaBefore = Get-FileSha256 $statusPath
    $recordPath = Join-Path $fixture.Root $fixture.RecordPath
    $originalOperationPlan = (Get-Command -Name New-GeoCeDGCloseoutGitOperationPlan `
        -CommandType Function -ErrorAction Stop).ScriptBlock
    $script:FixturePlanFailureRoot = $fixture.Root
    $script:FixturePlanFailureExpectedPaths = @($fixture.StatusPath, $fixture.RecordPath)
    $script:FixturePlanFailureObservedExactStage = $false
    Set-Item Function:script:New-GeoCeDGCloseoutGitOperationPlan -Value {
        param(
            [Parameter(Mandatory)] [string]$TechnicalCommit,
            [Parameter(Mandatory)] [string]$CommitMessage,
            [Parameter(Mandatory)] [string]$TagName,
            [Parameter(Mandatory)] [string]$TagMessage,
            [Parameter(Mandatory)] [string]$Branch,
            [Parameter(Mandatory)] [string]$Remote
        )
        $observed = Get-GeoCeDGPhaseLifecycleStatusPaths `
            $script:FixturePlanFailureRoot
        $script:FixturePlanFailureObservedExactStage =
            $observed.Unstaged.Count -eq 0 -and
            $observed.Untracked.Count -eq 0 -and
            (@($observed.Staged | Sort-Object -CaseSensitive) -join "`n") -ceq
            (@($script:FixturePlanFailureExpectedPaths |
                    Sort-Object -CaseSensitive) -join "`n")
        throw 'FIXTURE_SYNTHETIC_RESULT_CONSTRUCTION_FAILURE_AFTER_STAGE'
    }
    try {
        $postStageFailure = Assert-Throws {
                Invoke-PrepareDirect $fixture AUTHOR_OPERATED $readiness `
                    $campaign $approval
            } 'PREPARE failed after its first bounded mutation; repository state was preserved for inspection:.*FIXTURE_SYNTHETIC_RESULT_CONSTRUCTION_FAILURE_AFTER_STAGE' `
            'Synthetic PREPARE post-stage result construction failure'
    } finally {
        $observedExactStage = [bool]$script:FixturePlanFailureObservedExactStage
        Set-Item Function:script:New-GeoCeDGCloseoutGitOperationPlan `
            -Value $originalOperationPlan
        Remove-Variable -Scope Script -Name FixturePlanFailureRoot,
            FixturePlanFailureExpectedPaths,FixturePlanFailureObservedExactStage `
            -ErrorAction SilentlyContinue
    }
    $after = Get-RepositorySnapshot $fixture
    $remoteTagsAfterResult = Invoke-FixtureGit $fixture.Root `
        @('ls-remote', '--tags', 'origin')
    $remoteTagsAfter = @($remoteTagsAfterResult.Output)
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-GeoCeDGPhaseLifecycleSet $status.Staged `
        @($fixture.StatusPath, $fixture.RecordPath) `
        'Preserved PREPARE post-stage delta'
    Assert-Case ($observedExactStage -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($remoteTagsBefore -join "`n") -ceq ($remoteTagsAfter -join "`n")) -and
        [string]$statusShaBefore -cne (Get-FileSha256 $statusPath) -and
        $status.Unstaged.Count -eq 0 -and
        $status.Untracked.Count -eq 0 -and
        (Test-Path -LiteralPath $recordPath -PathType Leaf) -and
        $postStageFailure -match 'repository state was preserved for inspection') `
        'PREPARE result construction failure did not preserve its exact staged evidence'
}

Invoke-CloseoutCase 'PREPARE preserves the exact staged delta when success receipt creation fails' {
    Assert-CliPrepareReceiptPersistencePreservation `
        -Name 'prepare-receipt-create-failure-preservation' `
        -FaultMode BEFORE_TEMP_CREATE `
        -ExpectedFailure 'FIXTURE_PREPARE_RECEIPT_FAILURE_BEFORE_TEMP_CREATE'
}

Invoke-CloseoutCase 'PREPARE removes a partial atomic receipt and preserves the staged delta' {
    Assert-CliPrepareReceiptPersistencePreservation `
        -Name 'prepare-receipt-partial-write-preservation' `
        -FaultMode AFTER_PARTIAL_TEMP_WRITE `
        -ExpectedFailure 'FIXTURE_PREPARE_RECEIPT_FAILURE_AFTER_PARTIAL_TEMP_WRITE'
}

Invoke-CloseoutCase 'PREPARE preserves unauthenticated same-path bytes and reports unknown state' {
    Assert-CliPrepareTamperedStatePreservation
}

Invoke-CloseoutCase 'one mode-neutral campaign supports either post-review mode without technical rerun' {
    $fixture = New-WorkflowFixture 'mode-neutral-reuse'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $evidenceHashesBefore = @($campaign.Paths | ForEach-Object { Get-FileSha256 $_ })

    $authorApproval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $authorPrepared = Invoke-PrepareDirect $fixture AUTHOR_OPERATED $readiness `
        $campaign $authorApproval
    $authorRecord = Read-Json (Join-Path $fixture.Root $fixture.RecordPath)
    $authorEvidenceJson = $authorRecord.evidence | ConvertTo-Json -Depth 100 -Compress
    [void](Invoke-FixtureGit $fixture.Root @('reset', '--hard', $fixture.TechnicalCommit))
    $recordPhysical = Join-Path $fixture.Root $fixture.RecordPath
    if (Test-Path -LiteralPath $recordPhysical) { [IO.File]::Delete($recordPhysical) }
    $resetStatus = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ($resetStatus.Staged.Count -eq 0 -and
        $resetStatus.Unstaged.Count -eq 0 -and $resetStatus.Untracked.Count -eq 0) `
        'Disposable mode-neutral fixture did not return to exact clean T'

    $verifiedApproval = Write-AuthorApproval $fixture VERIFIED
    $verifiedPrepared = Invoke-PrepareDirect $fixture VERIFIED $readiness `
        $campaign $verifiedApproval
    $verifiedRecord = Read-Json (Join-Path $fixture.Root $fixture.RecordPath)
    $verifiedEvidenceJson = $verifiedRecord.evidence | ConvertTo-Json -Depth 100 -Compress
    $evidenceHashesAfter = @($campaign.Paths | ForEach-Object { Get-FileSha256 $_ })
    Assert-Case ([string]$authorPrepared.acceptancePlanSha256 -ceq
            [string]$verifiedPrepared.acceptancePlanSha256 -and
        [string]$authorPrepared.acceptancePlanSha256 -ceq
            [string]$readiness.Receipt.acceptancePlanSha256 -and
        [string]$authorRecord.closeoutMode -ceq 'AUTHOR_OPERATED' -and
        [string]$verifiedRecord.closeoutMode -ceq 'VERIFIED' -and
        $authorEvidenceJson -ceq $verifiedEvidenceJson -and
        ($evidenceHashesBefore -join ',') -ceq ($evidenceHashesAfter -join ',') -and
        $null -eq (Read-Json $campaign.FullPath).closeoutMode -and
        -not $authorPrepared.technicalExecutionRepeated -and
        -not $verifiedPrepared.technicalExecutionRepeated) `
        'Mode selection changed or reran the mode-neutral technical campaign'
}

Invoke-CloseoutCase 'legacy VERIFIED ADR 0023 and 0024 exact-SHA closeout guards remain present' {
    $lifecycleSource = [IO.File]::ReadAllText($LifecyclePath)
    $identitySource = [IO.File]::ReadAllText($IdentityPath)
    $legacySource = [IO.File]::ReadAllText($LegacyCloseoutPath)
    foreach ($functionName in @('Get-GeoCeDGPhaseAuthorCloseoutTargetContext',
            'Assert-GeoCeDGTechnicalEvidenceLink',
            'Get-GeoCeDGPhasePublishedTagRegressionContext',
            'Assert-GeoCeDGRepositoryTreeDelta')) {
        Assert-Case (($lifecycleSource + $identitySource).Contains("function $functionName")) `
            "Legacy VERIFIED guard disappeared: $functionName"
    }
    foreach ($literal in @('[Parameter(Mandatory)] [string]$ReviewedTechnicalCommit',
            '[Parameter(Mandatory)] [string]$CloseoutCommit',
            'Get-GeoCeDGPhaseAuthorCloseoutTargetContext',
            'verificationCodeStatus = $verificationStatus')) {
        Assert-Case ($legacySource.Contains($literal)) `
            "Legacy VERIFIED exact-target wrapper changed: $literal"
    }
    Assert-Case (-not $legacySource.Contains('selfApproved = $true')) `
        'Legacy VERIFIED wrapper introduced self-approval'
}

Invoke-CloseoutCase 'AUTHOR_OPERATED CLI PREPARE stages only the expected delta and never commits tags or pushes' {
    $fixture = New-WorkflowFixture 'author-operated-cli-prepare'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $before = Get-RepositorySnapshot $fixture
    $resultPath = Join-Path $fixture.ExternalRoot 'author-operated-preparation.json'
    $execution = Invoke-CloseoutCli -Action PREPARE -Fixture $fixture `
        -CloseoutMode author_operated -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -ResultPath $resultPath
    Assert-Case ($execution.ExitCode -eq 0) `
        "AUTHOR_OPERATED CLI PREPARE failed: $($execution.Output -join ' ')"
    $after = Get-RepositorySnapshot $fixture
    $prepared = Read-Json $resultPath
    $decisionRecord = Read-Json (Join-Path $fixture.Root $fixture.RecordPath)
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    $expectedPaths = @($fixture.StatusPath, $fixture.RecordPath)
    Assert-GeoCeDGPhaseLifecycleSet $status.Staged $expectedPaths `
        'AUTHOR_OPERATED prepared staged paths'
    Assert-Case ($status.Unstaged.Count -eq 0 -and $status.Untracked.Count -eq 0 -and
        (($before | ConvertTo-Json -Depth 10 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 10 -Compress)) -and
        [string]$prepared.reviewedTechnicalCommit -ceq $fixture.TechnicalCommit -and
        [string]$prepared.expectedTag.name -ceq $fixture.TagName -and
        [string]$prepared.closeoutMode -ceq 'AUTHOR_OPERATED' -and
        [string]$prepared.modeState -ceq
            'AUTHOR_OPERATED_CLOSEOUT_PREPARED_AWAITING_HUMAN_GIT' -and
        [string]$prepared.gitFinalizationAuthority -ceq 'AUTHOR' -and
        -not $prepared.automaticCommitPerformed -and -not $prepared.automaticTagPerformed -and
        -not $prepared.automaticFastForwardPerformed -and -not $prepared.automaticPushPerformed -and
        @($prepared.remainingGitOperations).Count -eq 7 -and
        @($prepared.humanOperations).Count -eq 7 -and
        @($prepared.verifiedWorkflowOperations).Count -eq 0 -and
        @($prepared.exactTransformations).Count -eq 2 -and
        [string]$prepared.promotion.technicalTarget -ceq $fixture.TechnicalCommit -and
        [string]$prepared.promotion.expectedCloseoutParent -ceq
            $fixture.TechnicalCommit) `
        'AUTHOR_OPERATED PREPARE performed or concealed a final Git operation'
    Assert-StructuredOperationPlan @($prepared.humanOperations) 7 `
        'AUTHOR_OPERATED human plan'
    Assert-StructuredOperationPlan @($prepared.remainingGitOperations) 7 `
        'AUTHOR_OPERATED remaining Git plan'
    Assert-PostPromotionAuditPlan $prepared.postPromotionAudit $fixture `
        AUTHOR_OPERATED $readiness $campaign $approval `
        'AUTHOR_OPERATED post-promotion audit plan'
    Assert-PhysicalInputClosureBinding `
        $decisionRecord.evidence.physicalInputClosureAtPreparation `
        $fixture.TechnicalCommit 'AUTHOR_OPERATED prepared decision record'
}

Invoke-CloseoutCase 'AUTHOR_OPERATED post-promotion CLI audit accepts exact publication and preserves T evidence' {
    $published = New-PublishedFixture 'author-operated-audit-positive'
    $auditPath = Join-Path $published.Fixture.ExternalRoot 'audit-result.json'
    $execution = Invoke-CloseoutCli -Action AUDIT -Fixture $published.Fixture `
        -CloseoutMode $published.CloseoutMode -Readiness $published.Readiness `
        -Campaign $published.Campaign -ApprovalPath $published.ApprovalPath `
        -ResultPath $auditPath -CloseoutCommit $published.CloseoutCommit
    Assert-Case ($execution.ExitCode -eq 0) `
        "AUTHOR_OPERATED CLI AUDIT failed: $($execution.Output -join ' ')"
    $audit = Read-Json $auditPath
    $decisionRecord = Read-Json `
        (Join-Path $published.Fixture.Root $published.Fixture.RecordPath)
    Assert-Case ([string]$audit.state -ceq 'POST_PROMOTION_AUDIT_PASSED' -and
        [string]$audit.reviewedTechnicalCommit -ceq $published.Fixture.TechnicalCommit -and
        [string]$audit.closeoutCommit -ceq $published.CloseoutCommit -and
        [string]$audit.technicalEvidence.attributedTo -ceq
            $published.Fixture.TechnicalCommit -and
        -not $audit.technicalEvidence.repeatedAfterCloseout -and
        -not $audit.guarantees.evidenceReattributedToCloseoutCommit -and
        -not $audit.guarantees.approvedTechnicalCommitSubstituted -and
        (($audit.physicalInputClosureAtPreparation | ConvertTo-Json -Depth 30 -Compress) `
            -ceq ($decisionRecord.evidence.physicalInputClosureAtPreparation |
                ConvertTo-Json -Depth 30 -Compress)) -and
        $audit.tag.annotated -and -not $audit.selfApproved) `
        'Positive post-promotion audit lost exact T/evidence/provenance guarantees'
    Assert-PhysicalInputClosureBinding $audit.physicalInputClosureAtPreparation `
        $published.Fixture.TechnicalCommit 'AUTHOR_OPERATED post-promotion audit'
}

Invoke-CloseoutCase 'schema-v2 VERIFIED CLI FINALIZE commits tags fast-forwards atomically pushes and audits' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-cli-finalize-positive'
    $before = Get-RepositorySnapshot $prepared.Fixture
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'verified-finalization-result.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    Assert-Case ($execution.ExitCode -eq 0 -and
        (Test-Path -LiteralPath $resultPath -PathType Leaf)) `
        "VERIFIED CLI FINALIZE failed: $($execution.Output -join ' ')"
    $result = Read-Json $resultPath
    $closeout = [string]$result.closeoutCommit
    $after = Get-RepositorySnapshot $prepared.Fixture
    $operationNames = @($result.operations | ForEach-Object {
            [string]$_.operation
        })
    $pushOperation = @($result.operations | Where-Object {
            [string]$_.operation -ceq 'PUSH_BRANCH_AND_TAG'
        })
    $tagType = Get-FixtureGitText $prepared.Fixture.Root @('cat-file', '-t',
        "refs/tags/$($prepared.Fixture.TagName)")
    $tagTarget = Get-FixtureGitText $prepared.Fixture.Root @('rev-parse',
        "refs/tags/$($prepared.Fixture.TagName)^{}")
    $remoteTag = Get-FixtureGitText $prepared.Fixture.Root @('ls-remote', '--tags',
        'origin', "refs/tags/$($prepared.Fixture.TagName)^{}")
    $changedPaths = @(Invoke-FixtureGit $prepared.Fixture.Root @('diff',
            '--name-only', $prepared.Fixture.TechnicalCommit, $closeout)).Output
    $status = Get-GeoCeDGPhaseLifecycleStatusPaths $prepared.Fixture.Root
    Assert-GeoCeDGPhaseLifecycleSet $changedPaths `
        @($prepared.Fixture.StatusPath, $prepared.Fixture.RecordPath) `
        'VERIFIED FINALIZE status-only tree delta'
    Assert-Case ([string]$result.state -ceq
            'VERIFIED_CLOSEOUT_FINALIZED_AND_AUDITED' -and
        [string]$result.closeoutMode -ceq 'VERIFIED' -and
        [string]$result.reviewedTechnicalCommit -ceq
            $prepared.Fixture.TechnicalCommit -and
        $closeout -cmatch '^[0-9a-f]{40}$' -and
        (@($operationNames) -join ',') -ceq
            'CREATE_STATUS_ONLY_CLOSEOUT_COMMIT,CAPTURE_EXACT_CLOSEOUT_SHA,VERIFY_EXACT_CLOSEOUT_PARENT,CREATE_ANNOTATED_PHASE_TAG,SWITCH_TO_PROMOTION_BRANCH,FAST_FORWARD_PROMOTION_BRANCH,PUSH_BRANCH_AND_TAG' -and
        $pushOperation.Count -eq 1 -and
        (@($pushOperation[0].arguments) -join ',') -ceq
            "push,--atomic,--no-all,--no-mirror,--no-tags,--no-delete,--no-force,--no-force-with-lease,--no-force-if-includes,--recurse-submodules=no,--no-prune,--no-follow-tags,--no-signed,--no-set-upstream,--no-verify,origin,refs/heads/main:refs/heads/main,refs/tags/$($prepared.Fixture.TagName):refs/tags/$($prepared.Fixture.TagName)" -and
        $result.push.atomic -and -not $result.push.force -and
        [string]$result.push.branchRefspec -ceq
            'refs/heads/main:refs/heads/main' -and
        [string]$result.push.tagRefspec -ceq
            "refs/tags/$($prepared.Fixture.TagName):refs/tags/$($prepared.Fixture.TagName)" -and
        $result.automaticCommitPerformed -and $result.automaticTagPerformed -and
        $result.automaticFastForwardPerformed -and $result.automaticPushPerformed -and
        -not $result.partialMutationPossible -and
        -not $result.technicalExecutionRepeated -and
        [string]$result.postPromotionAudit.state -ceq
            'POST_PROMOTION_AUDIT_PASSED' -and
        [string]$result.postPromotionAudit.technicalEvidence.attributedTo -ceq
            $prepared.Fixture.TechnicalCommit -and
        -not $result.postPromotionAudit.guarantees.
            observableForcePushOrUnexpectedMergeDetected -and
        -not $result.postPromotionAudit.guarantees.historicalRemoteReflogAudited -and
        $tagType -ceq 'tag' -and $tagTarget -ceq $closeout -and
        ($remoteTag -split "`t")[0] -ceq $closeout -and
        [string]$before.head -ceq $prepared.Fixture.TechnicalCommit -and
        [string]$before.main -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.head -ceq $closeout -and [string]$after.main -ceq $closeout -and
        [string]$after.originMain -ceq $closeout -and
        [string]$after.liveMain -ceq $closeout -and
        $status.Staged.Count -eq 0 -and $status.Unstaged.Count -eq 0 -and
        $status.Untracked.Count -eq 0 -and -not $result.selfApproved) `
        'VERIFIED CLI FINALIZE lost commit/tag/atomic-push/audit guarantees'
    Assert-PhysicalInputClosureBinding `
        $result.postPromotionAudit.physicalInputClosureAtPreparation `
        $prepared.Fixture.TechnicalCommit 'VERIFIED CLI FINALIZE audit'
    Assert-PostPromotionAuditPlan $prepared.Preparation.postPromotionAudit `
        $prepared.Fixture VERIFIED $prepared.Readiness $prepared.Campaign `
        $prepared.ApprovalPath 'VERIFIED CLI FINALIZE planned audit'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE disables followTags and publishes only its exact two refspecs' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-no-follow-tags'
    $extraTag = 'geocedg-fixture-unrelated-annotated'
    [void](Invoke-FixtureGit $prepared.Fixture.Root @('tag', '--no-sign', '-a',
        $extraTag, '-m', 'Unrelated annotated fixture tag',
        $prepared.Fixture.TechnicalCommit))
    [void](Invoke-FixtureGit $prepared.Fixture.Root @('config', 'push.followTags',
        'true'))
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'verified-finalization-no-follow-tags.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    Assert-Case ($execution.ExitCode -eq 0) `
        "VERIFIED no-follow-tags FINALIZE failed: $($execution.Output -join ' ')"
    $result = Read-Json $resultPath
    $push = @($result.operations | Where-Object {
            [string]$_.operation -ceq 'PUSH_BRANCH_AND_TAG'
        })
    $remoteExtra = Get-FixtureGitText $prepared.Fixture.Root @('ls-remote', '--tags',
        'origin', "refs/tags/$extraTag", "refs/tags/$extraTag^{}")
    $localExtraType = Get-FixtureGitText $prepared.Fixture.Root @('cat-file', '-t',
        "refs/tags/$extraTag")
    Assert-Case ($push.Count -eq 1 -and
        @($push[0].arguments).Count -eq 18 -and
        @($push[0].arguments | Where-Object { [string]$_ -ceq '--no-follow-tags' }).Count -eq 1 -and
        [string]::IsNullOrWhiteSpace($remoteExtra) -and
        $localExtraType -ceq 'tag' -and $result.push.atomic -and
        -not $result.push.force -and
        [string]$result.postPromotionAudit.state -ceq 'POST_PROMOTION_AUDIT_PASSED') `
        'FINALIZE inherited followTags or published an undeclared annotated tag'
}

Invoke-CloseoutCase 'AUTHOR_OPERATED rejects FINALIZE and retains its staged human-owned state' {
    $fixture = New-WorkflowFixture 'author-operated-finalize-rejected'
    $readiness = Write-ReadinessReceipt $fixture
    $campaign = New-TechnicalEvidenceCampaign $fixture $readiness
    $approval = Write-AuthorApproval $fixture AUTHOR_OPERATED
    $preparationPath = Join-Path $fixture.ExternalRoot `
        'author-operated-preparation-result.json'
    $preparationExecution = Invoke-CloseoutCli -Action PREPARE -Fixture $fixture `
        -CloseoutMode AUTHOR_OPERATED -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -ResultPath $preparationPath
    Assert-Case ($preparationExecution.ExitCode -eq 0) `
        "AUTHOR_OPERATED PREPARE failed: $($preparationExecution.Output -join ' ')"
    $before = Get-RepositorySnapshot $fixture
    $statusBefore = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    $resultPath = Join-Path $fixture.ExternalRoot 'forbidden-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $fixture `
        -CloseoutMode AUTHOR_OPERATED -Readiness $readiness -Campaign $campaign `
        -ApprovalPath $approval -PreparationResultPath $preparationPath `
        -ResultPath $resultPath
    $after = Get-RepositorySnapshot $fixture
    $statusAfter = Get-GeoCeDGPhaseLifecycleStatusPaths $fixture.Root
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match 'FINALIZE is available only.*VERIFIED') -and
        -not (Test-Path -LiteralPath $resultPath) -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($statusBefore | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($statusAfter | ConvertTo-Json -Depth 20 -Compress))) `
        'AUTHOR_OPERATED entered FINALIZE or changed its human-owned staged state'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE rejects an altered PREPARE receipt before mutation' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-tampered-prepare'
    $receipt = Read-Json $prepared.PreparationPath
    $receipt.promotion.closeoutCommitMessage = 'Tampered closeout message'
    Write-Json $prepared.PreparationPath $receipt
    $before = Get-RepositorySnapshot $prepared.Fixture
    $statusBefore = Get-GeoCeDGPhaseLifecycleStatusPaths $prepared.Fixture.Root
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'tampered-preparation-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    $statusAfter = Get-GeoCeDGPhaseLifecycleStatusPaths $prepared.Fixture.Root
    $failure = Read-Json $resultPath
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match
            'preparation receipt changed|finalization authority|canonical staged authority') -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        (($statusBefore | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($statusAfter | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$failure.state -ceq 'VERIFIED_CLOSEOUT_FINALIZATION_FAILED' -and
        $null -eq $failure.automaticCommitPerformed -and
        $null -eq $failure.automaticTagPerformed -and
        $null -eq $failure.automaticFastForwardPerformed -and
        $null -eq $failure.automaticPushPerformed -and
        [string]$failure.mutationState -ceq 'UNKNOWN_REQUIRES_INSPECTION' -and
        $failure.partialMutationPossible -and -not $failure.selfApproved) `
        'Altered PREPARE receipt reached mutation or produced a dishonest failure receipt'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE rejects a changed promotion anchor before commit' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-anchor-changed'
    $anchorTree = Get-FixtureGitText $prepared.Fixture.Root @('rev-parse',
        "$($prepared.Fixture.MainAnchor)^{tree}")
    $alternateAnchor = ((Invoke-FixtureGit $prepared.Fixture.Root @('commit-tree',
                $anchorTree, '-p', $prepared.Fixture.MainAnchor, '-m',
                'Synthetic changed main anchor')).Output -join '').Trim()
    [void](Invoke-FixtureGit $prepared.Fixture.Root @('update-ref',
        'refs/heads/main', $alternateAnchor, $prepared.Fixture.MainAnchor))
    $before = Get-RepositorySnapshot $prepared.Fixture
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'changed-anchor-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match 'promotion anchor changed after PREPARE') -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$after.head -ceq $prepared.Fixture.TechnicalCommit -and
        [string]$after.originMain -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.liveMain -ceq $prepared.Fixture.MainAnchor -and
        @($after.tags).Count -eq 0) `
        'Changed promotion anchor was published or mutated further'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE rejects an occupied expected tag before commit' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-tag-occupied'
    [void](Invoke-FixtureGit $prepared.Fixture.Root @('tag', '-a',
        $prepared.Fixture.TagName, '-m', 'Unexpected pre-existing tag',
        $prepared.Fixture.TechnicalCommit))
    $before = Get-RepositorySnapshot $prepared.Fixture
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'occupied-tag-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match 'expected tag is no longer absent') -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$after.head -ceq $prepared.Fixture.TechnicalCommit -and
        [string]$after.main -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.liveMain -ceq $prepared.Fixture.MainAnchor) `
        'Occupied expected tag reached VERIFIED commit or promotion'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE rejects promotion remote identity replacement before commit' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-remote-changed'
    $alternate = New-AlternateBareRemote $prepared.Fixture `
        'finalize-alternate-origin'
    [void](Invoke-FixtureGit $prepared.Fixture.Root @('remote', 'set-url',
        'origin', $alternate))
    $before = Get-RepositorySnapshot $prepared.Fixture
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'changed-remote-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match
            'promotion remote identity changed after PREPARE|Promotion remote URL/identity changed') -and
        (($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$after.head -ceq $prepared.Fixture.TechnicalCommit -and
        [string]$after.main -ceq $prepared.Fixture.MainAnchor -and
        @($after.tags).Count -eq 0) `
        'Changed promotion remote reached VERIFIED commit or publication'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE detects a post-commit product hook before tag or publication' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-product-hook'
    $hookPath = Join-Path $prepared.Fixture.Root '.git/hooks/post-commit'
    Write-Text $hookPath ("#!/bin/sh`n" +
        "printf '%s\n' 'final class Product { int value = 99; }' > source/product.java`n" +
        "git add -- source/product.java`n")
    if (-not $IsWindows) {
        [IO.File]::SetUnixFileMode($hookPath,
            [IO.UnixFileMode]::UserRead -bor [IO.UnixFileMode]::UserWrite -bor
            [IO.UnixFileMode]::UserExecute)
    }
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'product-hook-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    $failure = Read-Json $resultPath
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match 'allowlist|changed path|tree|status') -and
        [string]$after.head -cmatch '^[0-9a-f]{40}$' -and
        [string]$after.head -cne $prepared.Fixture.TechnicalCommit -and
        [string]$after.main -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.originMain -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.liveMain -ceq $prepared.Fixture.MainAnchor -and
        @($after.tags).Count -eq 0 -and
        [string]$failure.state -ceq 'VERIFIED_CLOSEOUT_FINALIZATION_FAILED' -and
        [string]$failure.mutationState -ceq 'UNKNOWN_REQUIRES_INSPECTION' -and
        $failure.partialMutationPossible -and
        -not (Test-Path -LiteralPath (Join-Path $prepared.Fixture.Origin `
                "refs/tags/$($prepared.Fixture.TagName)"))) `
        'Post-commit product mutation escaped prepublication verification'
}

Invoke-CloseoutCase 'VERIFIED FINALIZE reports remote rejection without claiming rollback or partial push' {
    $prepared = New-CliPreparedVerifiedFixture 'verified-finalize-remote-reject'
    $hookPath = Join-Path $prepared.Fixture.Origin 'hooks/pre-receive'
    Write-Text $hookPath "#!/bin/sh`nexit 1`n"
    if (-not $IsWindows) {
        [IO.File]::SetUnixFileMode($hookPath,
            [IO.UnixFileMode]::UserRead -bor [IO.UnixFileMode]::UserWrite -bor
            [IO.UnixFileMode]::UserExecute)
    }
    $resultPath = Join-Path $prepared.Fixture.ExternalRoot `
        'remote-rejected-finalization.json'
    $execution = Invoke-CloseoutCli -Action FINALIZE -Fixture $prepared.Fixture `
        -CloseoutMode VERIFIED -Readiness $prepared.Readiness `
        -Campaign $prepared.Campaign -ApprovalPath $prepared.ApprovalPath `
        -PreparationResultPath $prepared.PreparationPath -ResultPath $resultPath
    $after = Get-RepositorySnapshot $prepared.Fixture
    $failure = Read-Json $resultPath
    $remoteTag = Invoke-FixtureGit $prepared.Fixture.Root @('ls-remote', '--tags',
        'origin', "refs/tags/$($prepared.Fixture.TagName)")
    Assert-Case ($execution.ExitCode -eq 1 -and
        (($execution.Output -join ' ') -match 'rejected|Git byte command failed|failed') -and
        [string]$after.head -cmatch '^[0-9a-f]{40}$' -and
        [string]$after.head -cne $prepared.Fixture.TechnicalCommit -and
        [string]$after.main -ceq $after.head -and
        [string]$after.originMain -ceq $prepared.Fixture.MainAnchor -and
        [string]$after.liveMain -ceq $prepared.Fixture.MainAnchor -and
        @($after.tags).Count -eq 1 -and @($remoteTag.Output).Count -eq 0 -and
        [string]$failure.state -ceq 'VERIFIED_CLOSEOUT_FINALIZATION_FAILED' -and
        $null -eq $failure.automaticPushPerformed -and
        [string]$failure.mutationState -ceq 'UNKNOWN_REQUIRES_INSPECTION' -and
        $failure.partialMutationPossible -and
        [string]$failure.requiredRecovery -ceq
            'INSPECT_EXACT_REPOSITORY_REFS_AND_RUN_POST_PROMOTION_AUDIT') `
        'Remote rejection was partially published or reported as safely rolled back'
}

Invoke-CloseoutCase 'post-promotion audit rejects a substituted technical SHA' {
    $published = New-PublishedFixture 'audit-wrong-technical-sha'
    $published.Fixture.TechnicalCommit = $published.Fixture.MainAnchor
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'blob|policy|technical|exact|Git byte command failed' 'Substituted technical SHA')
}

Invoke-CloseoutCase 'post-promotion audit rejects a lightweight expected tag' {
    $published = New-PublishedFixture 'audit-lightweight-tag'
    [void](Invoke-FixtureGit $published.Fixture.Root @('push', '--quiet', 'origin',
        ":refs/tags/$($published.Fixture.TagName)"))
    [void](Invoke-FixtureGit $published.Fixture.Root @('tag', '-d',
        $published.Fixture.TagName))
    [void](Invoke-FixtureGit $published.Fixture.Root @('tag', $published.Fixture.TagName,
        $published.CloseoutCommit))
    [void](Invoke-FixtureGit $published.Fixture.Root @('push', '--quiet', 'origin',
        "refs/tags/$($published.Fixture.TagName)"))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'tag|lightweight|Git byte command failed' 'Lightweight phase tag')
}

Invoke-CloseoutCase 'post-promotion audit rejects an annotated tag with the wrong target' {
    $published = New-PublishedFixture 'audit-wrong-tag-target'
    [void](Invoke-FixtureGit $published.Fixture.Root @('push', '--quiet', 'origin',
        ":refs/tags/$($published.Fixture.TagName)"))
    [void](Invoke-FixtureGit $published.Fixture.Root @('tag', '-d',
        $published.Fixture.TagName))
    [void](Invoke-FixtureGit $published.Fixture.Root @('tag', '-a',
        $published.Fixture.TagName, '-m', $published.Fixture.TagMessage,
        $published.Fixture.TechnicalCommit))
    [void](Invoke-FixtureGit $published.Fixture.Root @('push', '--quiet', 'origin',
        "refs/tags/$($published.Fixture.TagName)"))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'tag|target|headers|authority' 'Wrong annotated tag target')
}

Invoke-CloseoutCase 'post-promotion audit rejects a non-direct closeout commit' {
    $published = New-PublishedFixture 'audit-non-direct-closeout' -Topology NON_DIRECT
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'direct, single-parent|direct.*child' 'Non-direct closeout commit')
}

Invoke-CloseoutCase 'post-promotion audit rejects an unexpected merge closeout' {
    $published = New-PublishedFixture 'audit-merge-closeout' -Topology MERGE
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'direct, single-parent|merge|single-parent' 'Merge closeout')
}

Invoke-CloseoutCase 'post-promotion audit rejects changed product input' {
    $published = New-PublishedFixture 'audit-product-change' -ProductChange
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'allowlist|tree|changed path|delta' 'Product change in closeout delta')
}

Invoke-CloseoutCase 'post-promotion audit rejects extra status-only closeout delta' {
    $published = New-PublishedFixture 'audit-extra-closeout-delta' -ExtraCloseoutDelta
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'allowlist|tree|changed path|delta' 'Extra closeout path')
}

Invoke-CloseoutCase 'post-promotion audit rejects CRLF-normalized decision-record bytes' {
    $published = New-PreparedFixture 'audit-record-crlf'
    $recordPath = Join-Path $published.Fixture.Root $published.Fixture.RecordPath
    $canonical = [IO.File]::ReadAllText($recordPath,
        [Text.UTF8Encoding]::new($false, $true))
    $crlf = $canonical.Replace("`r`n", "`n").Replace("`r", "`n").Replace(
        "`n", "`r`n")
    [IO.File]::WriteAllText($recordPath, $crlf, [Text.UTF8Encoding]::new($false))
    $published.CloseoutCommit = Complete-Promotion $published.Fixture
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'decision-record bytes differ from the canonical expected serialization' `
        'CRLF-normalized decision record')
}

Invoke-CloseoutCase 'post-promotion audit rejects semantically inert record whitespace' {
    $published = New-PreparedFixture 'audit-record-whitespace'
    $recordPath = Join-Path $published.Fixture.Root $published.Fixture.RecordPath
    $canonical = [IO.File]::ReadAllText($recordPath,
        [Text.UTF8Encoding]::new($false, $true))
    $whitespace = $canonical.TrimEnd("`r", "`n") + " `n"
    [IO.File]::WriteAllText($recordPath, $whitespace,
        [Text.UTF8Encoding]::new($false))
    $published.CloseoutCommit = Complete-Promotion $published.Fixture
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'decision-record bytes differ from the canonical expected serialization' `
        'Whitespace-mutated decision record')
}

Invoke-CloseoutCase 'post-promotion audit rejects dirty worktree state' {
    $published = New-PublishedFixture 'audit-dirty-poststate'
    $dirtyPath = Join-Path -Path $published.Fixture.Root `
        -ChildPath $published.Fixture.StatusPath
    [IO.File]::AppendAllText($dirtyPath, "DIRTY`n",
        [Text.UTF8Encoding]::new($false))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'materialization|worktree|Index|tracked' 'Dirty post-promotion state')
}

Invoke-CloseoutCase 'post-promotion audit rejects live remote main divergence' {
    $published = New-PublishedFixture 'audit-remote-divergence'
    [void](Invoke-FixtureGit $published.Fixture.Root @('switch', '--quiet', '-c',
        'fixture-remote-divergence'))
    $divergent = Commit-Fixture $published.Fixture.Root 'Remote-only successor' -AllowEmpty
    [void](Invoke-FixtureGit $published.Fixture.Root @('push', '--quiet', 'origin',
        "$divergent`:refs/heads/fixture-remote-divergence"))
    [void](Invoke-RawGit @('--git-dir', $published.Fixture.Origin, 'update-ref',
        'refs/heads/main', $divergent))
    [void](Invoke-FixtureGit $published.Fixture.Root @('switch', '--quiet', 'main'))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'HEAD, main, origin/main, and live remote main|live remote' `
        'Divergent live remote main')
}

Invoke-CloseoutCase 'post-promotion audit rejects promotion remote identity replacement' {
    $published = New-PublishedFixture 'audit-remote-identity-replacement'
    $alternate = New-AlternateBareRemote $published.Fixture 'published-remote-copy'
    [void](Invoke-FixtureGit $published.Fixture.Root @('remote', 'set-url',
        'origin', $alternate))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'Promotion remote URL/identity changed' 'Post-promotion remote identity replacement')
}

Invoke-CloseoutCase 'post-promotion audit rejects replace objects without accepting substituted history' {
    $published = New-PublishedFixture 'audit-replace-object'
    $before = Get-RepositorySnapshot $published.Fixture
    [void](Invoke-FixtureGit $published.Fixture.Root @('replace',
        $published.Fixture.TechnicalCommit, $published.Fixture.MainAnchor))
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'Git replace objects are unsupported for immutable repository identity' `
        'Post-promotion replace-object authority')
    $after = Get-RepositorySnapshot $published.Fixture
    Assert-Case ((($before | ConvertTo-Json -Depth 20 -Compress) -ceq
            ($after | ConvertTo-Json -Depth 20 -Compress)) -and
        [string]$after.head -ceq $published.CloseoutCommit -and
        [string]$after.liveMain -ceq $published.CloseoutCommit) `
        'Replace-object audit rejection changed published refs or accepted substitution'
}

Invoke-CloseoutCase 'post-promotion audit rejects evidence reattributed from T to C' {
    $published = New-PublishedFixture 'audit-evidence-reattributed'
    $rootResult = Read-Json $published.Campaign.Path
    $rootResult.repositoryCommit = $published.CloseoutCommit
    $rootResult.reviewedCandidate = $published.CloseoutCommit
    Write-Json $published.Campaign.Path $rootResult
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'closeout-consumable clean FULL campaign for exact T|evidence' `
        'Evidence reattributed to C')
}

Invoke-CloseoutCase 'post-promotion audit rejects self-approved author authority' {
    $published = New-PublishedFixture 'audit-self-approved'
    $approval = Read-Json $published.ApprovalPath
    $approval.selfApproved = $true
    Write-Json $published.ApprovalPath $approval
    [void](Assert-Throws { Invoke-AuditDirect $published } `
        'author approval|authorize|self' 'Self-approved author authority')
}

$failed = @($Results | Where-Object { [string]$_.status -ceq 'FAIL' }).Count
$passed = @($Results | Where-Object { [string]$_.status -ceq 'PASS' }).Count
$summary = [ordered]@{
    schemaVersion = 1
    evidenceKind = 'FAKE_FIRST_DUAL_CLOSEOUT_OPERATIONAL_TESTS_NOT_BUILD_OR_AUTHOR_EVIDENCE'
    helperPath = $HelperPath
    helperSha256 = Get-FileSha256 $HelperPath
    cliPath = $CliPath
    cliSha256 = Get-FileSha256 $CliPath
    fixtureRoot = $RunRoot
    tests = $Results.Count
    passed = $passed
    failed = $failed
    localBareRemoteOnly = $true
    networkAccessed = $false
    productRuntimeExecuted = $false
    authorApproved = $false
    selfApproved = $false
    results = @($Results)
}
$summaryPath = Join-Path $EvidenceRoot 'phase-closeout-tests.json'
Write-Json $summaryPath $summary
$canonical = [ordered]@{
    schemaVersion = 1
    evidenceKind = 'DETERMINISTIC_DUAL_CLOSEOUT_INFRASTRUCTURE_FIXTURES'
    tests = $summary.tests
    passed = $summary.passed
    failed = $summary.failed
    localBareRemoteOnly = $true
    networkAccessed = $false
    productRuntimeExecuted = $false
    authorApproved = $false
    selfApproved = $false
    results = @($Results | ForEach-Object {
            [ordered]@{ ordinal = $_.ordinal; name = $_.name; status = $_.status }
        })
}
$canonicalPath = Join-Path $EvidenceRoot 'canonical-summary.json'
Write-Json $canonicalPath $canonical
$canonicalHash = Get-FileSha256 $canonicalPath
Write-Text (Join-Path $EvidenceRoot 'canonical-summary.sha256') ($canonicalHash + "`n")
Write-Host "Phase closeout fake-first tests: $passed/$($Results.Count) passed."
Write-Host "Deterministic phase-closeout fixture SHA-256: $canonicalHash"
Write-Host "Saved phase-closeout test evidence: $summaryPath"
Write-Host "Retained local-only fixtures: $RunRoot"
if ($failed -ne 0) { exit 1 }
exit 0
